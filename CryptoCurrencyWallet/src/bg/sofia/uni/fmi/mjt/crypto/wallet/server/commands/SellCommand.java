package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.util.Map;

public class SellCommand implements Command {
    public static final int ARGS_NUM = 2;
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;
    private final String offeringCode;

    public SellCommand(WalletService walletService, CachedCoinAPIService cachedCoinAPIService, String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning("Sell000 command usage error. Missing arguments: sell --offering=<offering_code>");
            throw new IllegalArgumentException("Usage: sell --offering=<offering_code>");
        }

        this.walletService = walletService;
        this.cachedCoinAPIService = cachedCoinAPIService;
        this.username = args[0];
        this.offeringCode = extractValue(args, "--offering=");
    }

    @Override
    public String execute() {
        double pricePerUnit = getCurrentPrice(offeringCode);
        if (pricePerUnit == 0) {
            LoggerUtil.logWarning("Failed to retrieve the price for " + offeringCode);
            return "Failed to retrieve the price for " + offeringCode;
        }

        boolean success = walletService.sellCrypto(username, offeringCode, pricePerUnit);
        if (success) {
            LoggerUtil.logInfo(
                "Successfully sold all holdings of " + offeringCode + " at $" + pricePerUnit + " per unit.");
            return "Successfully sold all holdings of " + offeringCode + " at $" + pricePerUnit + " per unit.";
        } else {
            LoggerUtil.logWarning("Sell failed. Check your holdings and try again.");
            return "Sell failed. Check your holdings and try again.";
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
            Map<String, Double> prices = cachedCoinAPIService.getCryptoOfferings()
                .stream()
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
