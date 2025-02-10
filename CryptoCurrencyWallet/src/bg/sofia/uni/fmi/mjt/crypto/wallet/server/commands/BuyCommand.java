package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.util.Map;

public class BuyCommand implements Command {
    public static final int ARGS_NUM = 3;
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;
    private final String offeringCode;
    private final double amount;

    public BuyCommand(WalletService walletService, CachedCoinAPIService cachedCoinAPIService, String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning(
                "Buy usage error. Missing arguments: buy <username> --offering=<offering_code> --money=<amount>");
            throw new IllegalArgumentException("Usage: buy <username> --offering=<offering_code> --money=<amount>");
        }

        this.walletService = walletService;
        this.cachedCoinAPIService = cachedCoinAPIService;
        this.username = args[0];
        this.offeringCode = extractValue(args, "--offering=");
        this.amount = Double.parseDouble(extractValue(args, "--money="));

        if (this.amount <= 0) {
            LoggerUtil.logWarning("Amount must be a positive number.");
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
    }

    @Override
    public String execute() {
        double pricePerUnit = getCurrentPrice(offeringCode);
        if (pricePerUnit == 0) {
            LoggerUtil.logWarning("Failed to retrieve the price for " + offeringCode);
            return "Failed to retrieve the price for " + offeringCode;
        }

        double cryptoAmount = amount / pricePerUnit;

        boolean success = walletService.buyCrypto(username, offeringCode, cryptoAmount, pricePerUnit);
        if (success) {
            LoggerUtil.logInfo("Successfully bought " + cryptoAmount + " of " + offeringCode + " for $" + amount);
            return "Successfully bought " + cryptoAmount + " of " + offeringCode + " for $" + amount;
        } else {
            LoggerUtil.logWarning("Purchase failed. Check your balance and input.");
            return "Purchase failed. Check your balance and input.";
        }
    }

    private String extractValue(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        LoggerUtil.logWarning("Missing argument: " + prefix);
        throw new IllegalArgumentException("Missing argument: " + prefix);
    }

    private double getCurrentPrice(String assetId) {
        try {
            Map<String, Double> prices = cachedCoinAPIService.getCryptoOfferings().stream()
                .collect(java.util.stream.Collectors.toMap(CryptoOffering::assetId, CryptoOffering::priceUsd));

            double price = prices.getOrDefault(assetId, 0.0);
            if (price == 0.0) {
                LoggerUtil.logWarning("Price for asset " + assetId + " is not available.");
            }
            return price;
        } catch (IOException e) {
            LoggerUtil.logError("Error fetching prices for " + assetId, e);
            return 0;
        }
    }
}
