package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

import java.io.IOException;
import java.util.Map;

public class BuyCommand implements Command {
    public static final int ARGS_COUNT = 3;
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;
    private final String offeringCode;
    private final double amount;

    public BuyCommand(WalletService walletService, CachedCoinAPIService cachedCoinAPIService, String[] args) {
        if (args.length < ARGS_COUNT) {
            throw new IllegalArgumentException("Usage: buy <username> --offering=<offering_code> --money=<amount>");
        }

        this.walletService = walletService;
        this.cachedCoinAPIService = cachedCoinAPIService;
        this.username = args[0];
        this.offeringCode = extractValue(args, "--offering=");
        this.amount = Double.parseDouble(extractValue(args, "--money="));

        if (this.amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number.");
        }
    }

    @Override
    public String execute() {
        double pricePerUnit = getCurrentPrice(offeringCode);
        if (pricePerUnit == 0) {
            return "Failed to retrieve the price for " + offeringCode;
        }

        double cryptoAmount = amount / pricePerUnit;
        boolean success = walletService.buyCrypto(username, offeringCode, cryptoAmount, pricePerUnit);

        return success
            ? "Successfully bought " + cryptoAmount + " of " + offeringCode + " for $" + amount
            : "Purchase failed. Check your balance and input.";
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
