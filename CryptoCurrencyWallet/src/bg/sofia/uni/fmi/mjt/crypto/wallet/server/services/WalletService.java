package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Transaction;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.TransactionType;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Wallet;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletService {
    private static WalletService instance;
    private final Map<String, User> users;

    private WalletService() {
        this.users = Storage.loadUsers();
    }

    public static synchronized WalletService getInstance() {
        if (instance == null) {
            instance = new WalletService();
        }
        return instance;
    }

    public synchronized boolean depositMoney(String username, double amount) {
        User user = users.get(username);
        if (user == null || amount <= 0) {
            return false; // User not found or invalid amount
        }

        Wallet oldWallet = user.wallet();
        List<Transaction> updatedTransactions = new ArrayList<>(oldWallet.transactionHistory());
        updatedTransactions.add(new Transaction(TransactionType.DEPOSIT, amount));

        Wallet newWallet = new Wallet(oldWallet.balance() + amount, oldWallet.cryptoHoldings(), updatedTransactions);

        users.put(username, new User(user.username(), user.password(), newWallet));
        Storage.saveUsers(users);
        return true;
    }

    public synchronized boolean buyCrypto(String username, String assetId, double amount, double pricePerUnit) {
        User user = users.get(username);
        if (user == null || amount <= 0 || pricePerUnit <= 0) {
            return false;
        }

        double totalCost = amount * pricePerUnit;
        Wallet oldWallet = user.wallet();

        if (oldWallet.balance() < totalCost) {
            return false; // Not enough balance
        }

        Map<String, Double> newCryptoHoldings = new HashMap<>(oldWallet.cryptoHoldings());
        newCryptoHoldings.put(assetId, newCryptoHoldings.getOrDefault(assetId, 0.0) + amount);

        List<Transaction> updatedTransactions = new ArrayList<>(oldWallet.transactionHistory());
        updatedTransactions.add(new Transaction(TransactionType.BUY, assetId, totalCost, amount, pricePerUnit));

        Wallet newWallet = new Wallet(oldWallet.balance() - totalCost, newCryptoHoldings, updatedTransactions);

        users.put(username, new User(user.username(), user.password(), newWallet));
        Storage.saveUsers(users);
        return true;
    }

    public synchronized boolean sellCrypto(String username, String assetId, double pricePerUnit) {
        User user = users.get(username);
        if (user == null || pricePerUnit <= 0) {
            return false;
        }

        Wallet oldWallet = user.wallet();
        double ownedAmount = oldWallet.cryptoHoldings().getOrDefault(assetId, 0.0);
        if (ownedAmount == 0) {
            return false; // No crypto to sell
        }

        double totalSaleValue = ownedAmount * pricePerUnit;
        Map<String, Double> newCryptoHoldings = new HashMap<>(oldWallet.cryptoHoldings());
        newCryptoHoldings.remove(assetId); // Selling everything

        double totalInvestmentReduction = calculateInvestmentReduction(oldWallet, assetId);
        List<Transaction> updatedTransactions =
            updateTransactionHistory(oldWallet, assetId, totalSaleValue, ownedAmount, pricePerUnit);

        Wallet newWallet = new Wallet(oldWallet.balance() + totalSaleValue, newCryptoHoldings, updatedTransactions);
        users.put(username, new User(user.username(), user.password(), newWallet));
        Storage.saveUsers(users);
        return true;
    }

    private double calculateInvestmentReduction(Wallet wallet, String assetId) {
        double totalInvestmentReduction = 0;
        for (Transaction t : wallet.transactionHistory()) {
            if (t.type() == TransactionType.BUY && t.assetId().equals(assetId)) {
                totalInvestmentReduction += t.amount();
            }
        }
        return totalInvestmentReduction;
    }

    private List<Transaction> updateTransactionHistory(Wallet wallet, String assetId, double totalSaleValue,
                                                       double ownedAmount, double pricePerUnit) {
        List<Transaction> updatedTransactions = new ArrayList<>(wallet.transactionHistory());
        updatedTransactions.add(
            new Transaction(TransactionType.SELL, assetId, totalSaleValue, ownedAmount, pricePerUnit));
        return updatedTransactions;
    }

    public synchronized String getWalletSummary(String username) {
        User user = users.get(username);
        if (user == null) {
            return "User not found.";
        }

        Wallet wallet = user.wallet();

        String transactionsFormatted = wallet.transactionHistory().stream()
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
            return "User not found.";
        }

        Wallet wallet = user.wallet();
        double totalInvestment = calculateTotalInvestment(wallet);
        double totalSellInvestment = calculateTotalSellInvestment(wallet);
        double currentPortfolioValue = calculateCurrentPortfolioValue(wallet, currentPrices);

        double profitLoss = totalSellInvestment - totalInvestment;

        return formatWalletSummary(totalInvestment, currentPortfolioValue, profitLoss);
    }

    private double calculateTotalInvestment(Wallet wallet) {
        return wallet.transactionHistory().stream()
            .filter(t -> t.type() == TransactionType.BUY)
            .mapToDouble(Transaction::amount)
            .sum();
    }

    private double calculateTotalSellInvestment(Wallet wallet) {
        double totalSellInvestment = 0;
        Map<String, Double> remainingHoldings = new HashMap<>();

        for (Transaction t : wallet.transactionHistory()) {
            if (t.type() == TransactionType.BUY) {
                remainingHoldings.put(t.assetId(),
                    remainingHoldings.getOrDefault(t.assetId(), 0.0) + t.cryptoQuantity());
            } else if (t.type() == TransactionType.SELL) {
                double amountSold = t.cryptoQuantity();
                double initialInvestment = 0;

                if (remainingHoldings.containsKey(t.assetId())) {
                    double remaining = remainingHoldings.get(t.assetId());
                    if (remaining >= amountSold) {
                        initialInvestment = (amountSold / remaining) * calculateTotalInvestment(wallet);
                        remainingHoldings.put(t.assetId(), remaining - amountSold);
                    } else {
                        initialInvestment = calculateTotalInvestment(wallet);
                        remainingHoldings.put(t.assetId(), 0.0);
                    }
                }
                totalSellInvestment += initialInvestment;
            }
        }
        return totalSellInvestment;
    }

    private double calculateCurrentPortfolioValue(Wallet wallet, Map<String, Double> currentPrices) {
        return wallet.cryptoHoldings().entrySet().stream()
            .mapToDouble(entry -> entry.getValue() * currentPrices.getOrDefault(entry.getKey(), 0.0))
            .sum();
    }

    private String formatWalletSummary(double totalInvestment, double currentPortfolioValue, double profitLoss) {
        return "Total Investment: $" + totalInvestment + "\n" +
            "Current Portfolio Value: $" + currentPortfolioValue + "\n" +
            "Profit/Loss: $" + profitLoss;
    }

    private String formatTransaction(Transaction t) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        String timestamp = formatter.format(Instant.ofEpochSecond(t.timestamp()));

        if (t.type() == TransactionType.DEPOSIT) {
            return String.format("[DEPOSIT] $%.2f (Timestamp: %s)", t.amount(), timestamp);
        } else if (t.type() == TransactionType.BUY) {
            return String.format("[BUY] %.6f %s for $%.2f at $%.2f per unit (Timestamp: %s)",
                t.cryptoQuantity(), t.assetId(), t.amount(), t.priceAtTransaction(), timestamp);
        } else if (t.type() == TransactionType.SELL) {
            return String.format("[SELL] %.6f %s for $%.2f at $%.2f per unit (Timestamp: %s)",
                t.cryptoQuantity(), t.assetId(), t.amount(), t.priceAtTransaction(), timestamp);
        }

        return "[UNKNOWN TRANSACTION]";
    }

}
