package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GetWalletOverallSummaryCommand implements Command {
    public static final int ARGS_NUM = 1;
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;

    public GetWalletOverallSummaryCommand(WalletService walletService, String[] args,
                                          CachedCoinAPIService cachedCoinAPIService) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning(
                "Get-wallet-overall-summary command usage error. " +
                    "Missing arguments: get-wallet-overall-summary <username>");
            throw new IllegalArgumentException("Usage: get-wallet-overall-summary <username>");
        }

        this.walletService = walletService;
        this.cachedCoinAPIService = cachedCoinAPIService;
        this.username = args[0];
    }

    @Override
    public String execute() {
        try {
            Map<String, Double> currentPrices = cachedCoinAPIService.getCryptoOfferings().stream()
                .collect(Collectors.toMap(CryptoOffering::assetId, CryptoOffering::priceUsd));

            LoggerUtil.logInfo("Successfully fetched current prices for cryptocurrencies.");

            String overallSummary = walletService.getWalletOverallSummary(username, currentPrices);
            LoggerUtil.logInfo(
                "Successfully fetched overall wallet summary for user: " + username);
            return overallSummary;
        } catch (IOException e) {
            LoggerUtil.logError("Failed to fetch current prices for cryptocurrencies: " + e.getMessage(), e);
            return "Failed to fetch current prices. Try again later.";
        }
    }
}
