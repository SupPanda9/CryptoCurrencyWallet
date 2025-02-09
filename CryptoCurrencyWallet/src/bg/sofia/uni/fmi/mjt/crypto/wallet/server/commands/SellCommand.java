package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

import java.io.IOException;
import java.util.Map;

public class SellCommand implements Command {
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;
    private final String offeringCode;

    public SellCommand(WalletService walletService, CachedCoinAPIService cachedCoinAPIService, String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: sell <username> --offering=<offering_code>");
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
            return "Failed to retrieve the price for " + offeringCode;
        }

        boolean success = walletService.sellCrypto(username, offeringCode, pricePerUnit);
        return success ? "Successfully sold all holdings of " + offeringCode + " at $" + pricePerUnit + " per unit."
            : "Sell failed. Check your holdings and try again.";
    }

    private String extractValue(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        throw new IllegalArgumentException("Missing argument: " + prefix);
    }

    private double getCurrentPrice(String assetId) {
        try {
            Map<String, Double> prices = cachedCoinAPIService.getCryptoOfferings()
                .stream()
                .collect(java.util.stream.Collectors.toMap(o -> o.assetId(), o -> o.priceUsd()));

            return prices.getOrDefault(assetId, 0.0);
        } catch (IOException e) {
            return 0;
        }
    }
}
