package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Transaction;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.TransactionType;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Wallet;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletService {

    private static WalletService instance;
    private Map<String, User> users;

    private WalletService() {
        this.users = Storage.loadUsers();
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public static synchronized WalletService getInstance() {
        if (instance == null) {
            instance = new WalletService();
        }
        return instance;
    }

    private boolean isUserValid(String username) {
        if (users.get(username) == null) {
            LoggerUtil.logWarning("User not found: " + username);
            return false;
        }
        return true;
    }

    private boolean isAmountValid(double amount) {
        if (amount <= 0) {
            LoggerUtil.logWarning("Invalid amount: " + amount);
            return false;
        }
        return true;
    }

    private boolean isPricePerUnitValid(double pricePerUnit) {
        if (pricePerUnit <= 0) {
            LoggerUtil.logWarning("Invalid price per unit: " + pricePerUnit);
            return false;
        }
        return true;
    }

    private void updateWallet(User user, Map<String, Double> newCryptoHoldings,
                              List<Transaction> updatedTransactions, double newBalance) {
        Wallet newWallet = new Wallet(newBalance, newCryptoHoldings, updatedTransactions);
        users.put(user.username(), new User(user.username(), user.password(), newWallet));
        Storage.saveUsers(users);
    }

    private void logTransactionSuccess(String transactionType, String username, String assetId, double amount) {
        LoggerUtil.logInfo(
            String.format("%s successful for user %s: %.6f %s", transactionType, username, amount, assetId));
    }

    public synchronized boolean depositMoney(String username, double amount) {
        if (!isUserValid(username) || !isAmountValid(amount)) {
            return false;
        }

        User user = users.get(username);
        Wallet oldWallet = user.wallet();
        List<Transaction> updatedTransactions = new ArrayList<>(oldWallet.transactionHistory());
        updatedTransactions.add(new Transaction(TransactionType.DEPOSIT, amount));

        updateWallet(user, oldWallet.cryptoHoldings(), updatedTransactions, oldWallet.balance() + amount);
        logTransactionSuccess("Deposit", username, "", amount);
        return true;
    }

    public synchronized boolean buyCrypto(String username, String assetId, double amount, double pricePerUnit) {
        if (!isUserValid(username) || !isAmountValid(amount) || !isPricePerUnitValid(pricePerUnit)) {
            return false;
        }

        User user = users.get(username);
        Wallet oldWallet = user.wallet();
        double totalCost = amount * pricePerUnit;

        if (oldWallet.balance() < totalCost) {
            LoggerUtil.logWarning("Insufficient balance for user " + username + " to buy " + assetId);
            return false;
        }

        Map<String, Double> newCryptoHoldings = new HashMap<>(oldWallet.cryptoHoldings());
        newCryptoHoldings.put(assetId, newCryptoHoldings.getOrDefault(assetId, 0.0) + amount);

        List<Transaction> updatedTransactions = new ArrayList<>(oldWallet.transactionHistory());
        updatedTransactions.add(new Transaction(TransactionType.BUY, assetId, totalCost, amount, pricePerUnit));

        updateWallet(user, newCryptoHoldings, updatedTransactions, oldWallet.balance() - totalCost);
        logTransactionSuccess("Buy", username, assetId, amount);
        return true;
    }

    public synchronized boolean sellCrypto(String username, String assetId, double pricePerUnit) {
        if (!isUserValid(username) || !isPricePerUnitValid(pricePerUnit)) {
            return false;
        }

        User user = users.get(username);
        Wallet oldWallet = user.wallet();
        double ownedAmount = oldWallet.cryptoHoldings().getOrDefault(assetId, 0.0);
        if (ownedAmount == 0) {
            LoggerUtil.logWarning("No crypto to sell for user " + username + ": " + assetId);
            return false;
        }

        double totalSaleValue = ownedAmount * pricePerUnit;
        Map<String, Double> newCryptoHoldings = new HashMap<>(oldWallet.cryptoHoldings());
        newCryptoHoldings.remove(assetId);
        List<Transaction> updatedTransactions =
            updateTransactionHistory(oldWallet, assetId, totalSaleValue, ownedAmount, pricePerUnit);

        updateWallet(user, newCryptoHoldings, updatedTransactions, oldWallet.balance() + totalSaleValue);
        logTransactionSuccess("Sell", username, assetId, ownedAmount);
        return true;
    }

    private List<Transaction> updateTransactionHistory(Wallet wallet, String assetId, double totalSaleValue,
                                                       double ownedAmount, double pricePerUnit) {
        List<Transaction> updatedTransactions = new ArrayList<>(wallet.transactionHistory());
        updatedTransactions.add(
            new Transaction(TransactionType.SELL, assetId, totalSaleValue, ownedAmount, pricePerUnit));
        return updatedTransactions;
    }

    /*private double calculateInvestmentReduction(Wallet wallet, String assetId) {
        double totalInvestmentReduction = 0;
        for (Transaction t : wallet.transactionHistory()) {
            if (t.type() == TransactionType.BUY && t.assetId().equals(assetId)) {
                totalInvestmentReduction += t.amount();
            }
        }
        return totalInvestmentReduction;
    }*/

    public synchronized String getWalletSummary(String username) {
        if (!isUserValid(username)) {
            return "User not found.";
        }

        User user = users.get(username);
        Wallet wallet = user.wallet();
        String transactionsFormatted =
            wallet.transactionHistory().stream()
                .map(this::formatTransaction)
                .reduce("", (a, b) -> a + "\n" + b);

        return String.format(
            "Balance: $%.2f\nCrypto Holdings: %s\nTransaction History:\n%s",
            wallet.balance(), wallet.cryptoHoldings(), transactionsFormatted
        );
    }

    public synchronized String getWalletOverallSummary(String username, Map<String, Double> currentPrices) {
        User user = users.get(username);
        if (user == null) {
            LoggerUtil.logWarning("User not found: " + username);
            return "User not found.";
        }

        Wallet wallet = user.wallet();
        double totalInvestment = calculateTotalInvestment(wallet);
        double totalSellInvestment = calculateTotalSellInvestment(wallet);
        double currentPortfolioValue = calculateCurrentPortfolioValue(wallet, currentPrices);

        double profitLoss = currentPortfolioValue + totalSellInvestment - totalInvestment;

        return formatWalletSummary(totalInvestment, currentPortfolioValue, profitLoss);
    }

    private double calculateTotalInvestment(Wallet wallet) {
        Map<String, Double> remainingHoldingsInvestment = new HashMap<>();

        for (Transaction t : wallet.transactionHistory()) {
            String asset = t.assetId();
            if (t.type() == TransactionType.BUY) {
                remainingHoldingsInvestment.put(asset,
                    remainingHoldingsInvestment.getOrDefault(asset, 0.0) + t.amount());
            } else if (t.type() == TransactionType.SELL) {
                double amountSold = t.cryptoQuantity();
                if (remainingHoldingsInvestment.containsKey(asset) && amountSold > 0) {
                    double totalInvestmentForAsset = remainingHoldingsInvestment.get(asset);
                    double currentHolding = wallet.cryptoHoldings().getOrDefault(asset, 0.0) + amountSold;

                    if (currentHolding > 0) {
                        double investmentReduction = (amountSold / currentHolding) * totalInvestmentForAsset;
                        remainingHoldingsInvestment.put(asset, totalInvestmentForAsset - investmentReduction);
                    }
                }
            }
        }

        return remainingHoldingsInvestment.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private double calculateTotalSellInvestment(Wallet wallet) {
        Map<String, Double> totalSpent = new HashMap<>();
        Map<String, Double> totalEarned = new HashMap<>();

        for (Transaction t : wallet.transactionHistory()) {
            String asset = t.assetId();
            if (t.type() == TransactionType.BUY) {
                totalSpent.put(asset, totalSpent.getOrDefault(asset, 0.0) + t.amount());
            } else if (t.type() == TransactionType.SELL) {
                totalEarned.put(asset, totalEarned.getOrDefault(asset, 0.0) + t.amount());
            }
        }

        double profit = 0;
        for (String asset : totalSpent.keySet()) {
            if (totalEarned.containsKey(asset)) {
                profit += totalEarned.get(asset) - totalSpent.get(asset);
            }
        }

        return profit;
    }

    private double calculateCurrentPortfolioValue(Wallet wallet, Map<String, Double> currentPrices) {
        return wallet.cryptoHoldings().entrySet().stream()
            .mapToDouble(entry -> entry.getValue() * currentPrices.getOrDefault(entry.getKey(), 0.0)).sum();
    }

    private String formatWalletSummary(double totalInvestment, double currentPortfolioValue, double profitLoss) {
        return "Total Investment: $" + totalInvestment + "\n" + "Current Portfolio Value: $" + currentPortfolioValue +
            "\n" + "Profit/Loss: $" + profitLoss;
    }

    private String formatTransaction(Transaction t) {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        String timestamp = formatter.format(Instant.ofEpochSecond(t.timestamp()));

        return switch (t.type()) {
            case DEPOSIT -> String.format("[DEPOSIT] $%.2f (Timestamp: %s)", t.amount(), timestamp);
            case BUY -> String.format("[BUY] %.6f %s for $%.2f at $%.5f per unit (Timestamp: %s)",
                t.cryptoQuantity(), t.assetId(), t.amount(), t.priceAtTransaction(), timestamp);
            case SELL -> String.format("[SELL] %.6f %s for $%.2f at $%.5f per unit (Timestamp: %s)",
                t.cryptoQuantity(), t.assetId(), t.amount(), t.priceAtTransaction(), timestamp);
            default -> "[UNKNOWN TRANSACTION]";
        };
    }

    public void reloadUsers() {
        this.users = Storage.loadUsers();
    }

}
