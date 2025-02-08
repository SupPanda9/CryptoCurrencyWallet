package bg.sofia.uni.fmi.mjt.crypto.wallet.server.models;

import java.util.HashMap;
import java.util.Map;

public class Wallet {
    private final String username;
    private double balance;
    private final Map<String, Double> cryptoHoldings;
    private final Map<String, Double> purchasePrices;

    public Wallet(String username) {
        this.username = username;
        this.balance = 0.0;
        this.cryptoHoldings = new HashMap<>();
        this.purchasePrices = new HashMap<>();
    }

    public synchronized void deposit(double amount) {
        balance += amount;
    }

    public synchronized void decreaseBalance(double amount) {
        if (balance >= amount) {
            balance -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient balance.");
        }
    }

    public synchronized void addCrypto(String crypto, double amount) {
        cryptoHoldings.put(crypto, cryptoHoldings.getOrDefault(crypto, 0.0) + amount);
        purchasePrices.putIfAbsent(crypto, 0.0); // Default price if missing
    }

    public synchronized boolean sellCrypto(String crypto, double amount, double totalEarnings) {
        if (!cryptoHoldings.containsKey(crypto) || cryptoHoldings.get(crypto) < amount) {
            return false;
        }

        cryptoHoldings.put(crypto, cryptoHoldings.get(crypto) - amount);
        if (cryptoHoldings.get(crypto) <= 0) {
            cryptoHoldings.remove(crypto);
            purchasePrices.remove(crypto);
        }

        balance += totalEarnings;
        return true;
    }

    public double getBalance() {
        return balance;
    }

    public synchronized double calculateProfitLoss(Map<String, Double> currentPrices) {
        double profitLoss = 0.0;
        for (Map.Entry<String, Double> entry : cryptoHoldings.entrySet()) {
            String crypto = entry.getKey();
            double amount = entry.getValue();
            double avgPurchasePrice = purchasePrices.getOrDefault(crypto, 0.0);
            double currentPrice = currentPrices.getOrDefault(crypto, avgPurchasePrice);

            profitLoss += (currentPrice - avgPurchasePrice) * amount;
        }
        return profitLoss;
    }

    @Override
    public String toString() {
        return "Balance: $" + balance + ", Crypto Holdings: " + cryptoHoldings;
    }
}
