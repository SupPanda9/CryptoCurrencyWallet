package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ListOfferingsCommand implements Command {

    private static final int MAX_LIST_SIZE = 100;
    private static final double MIN_VOLUME_USD = 1_000_000;
    private static final double MIN_PRICE_USD = 0.01;

    private final CachedCoinAPIService cachedCoinAPIService;

    public ListOfferingsCommand(CachedCoinAPIService cachedCoinAPIService) {
        this.cachedCoinAPIService = cachedCoinAPIService;
        LoggerUtil.logInfo("ListOfferingsCommand initialized with CachedCoinAPIService.");
    }

    @Override
    public String execute() {
        LoggerUtil.logInfo("Executing ListOfferingsCommand...");
        try {
            List<CryptoOffering> allOfferings = cachedCoinAPIService.getCryptoOfferings();
            LoggerUtil.logInfo("Fetched " + allOfferings.size() + " crypto offerings from the API.");

            List<CryptoOffering> filteredOfferings = allOfferings.stream()
                .filter(o -> o.priceUsd() >= MIN_PRICE_USD && o.volumeUsd() >= MIN_VOLUME_USD)
                .limit(MAX_LIST_SIZE)
                .collect(Collectors.toList());

            LoggerUtil.logInfo("Filtered offerings: " + filteredOfferings.size() + " offerings match criteria.");

            if (filteredOfferings.isEmpty()) {
                LoggerUtil.logWarning("No high-value cryptocurrency offerings found.");
                return "No high-value cryptocurrency offerings available.";
            }

            return formatOfferings(filteredOfferings);

        } catch (IOException e) {
            LoggerUtil.logError("Error fetching crypto offerings: ", e);
            return "Error fetching crypto offerings: " + e.getMessage();
        }
    }

    private String formatOfferings(List<CryptoOffering> offerings) {
        StringBuilder result = new StringBuilder("\nAvailable High-Value Crypto Offerings:\n");
        result.append("----------------------------------------------------------------------\n");
        result.append(String.format("%-10s | %-25s | %12s | %15s%n", "Symbol", "Name", "Price (USD)", "Volume (USD)"));
        result.append("----------------------------------------------------------------------\n");

        for (CryptoOffering offering : offerings) {
            result.append(String.format("%-10s | %-25s | $%,12.2f | $%,15.2f%n",
                offering.assetId(), offering.name(), offering.priceUsd(), offering.volumeUsd()));
        }

        result.append("----------------------------------------------------------------------\n");
        result.append("Showing ").append(offerings.size()).append(" high-value crypto assets.");
        LoggerUtil.logInfo("Successfully formatted " + offerings.size() + " offerings.");
        return result.toString();
    }
}
