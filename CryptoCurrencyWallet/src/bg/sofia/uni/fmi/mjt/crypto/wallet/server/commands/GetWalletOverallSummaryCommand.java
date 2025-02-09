package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GetWalletOverallSummaryCommand implements Command {
    private final WalletService walletService;
    private final CachedCoinAPIService cachedCoinAPIService;
    private final String username;

    public GetWalletOverallSummaryCommand(WalletService walletService, String[] args,
                                          CachedCoinAPIService cachedCoinAPIService) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: get-wallet-overall-summary <username>");
        }

        this.walletService = walletService;
        this.cachedCoinAPIService = cachedCoinAPIService;
        this.username = args[0];
    }

    @Override
    public String execute() {
        try {
            Map<String, Double> currentPrices = cachedCoinAPIService.getCryptoOfferings()
                .stream()
                .collect(Collectors.toMap(o -> o.assetId(), o -> o.priceUsd()));

            return walletService.getWalletOverallSummary(username, currentPrices);
        } catch (IOException e) {
            return "Failed to fetch current prices. Try again later.";
        }
    }
}
