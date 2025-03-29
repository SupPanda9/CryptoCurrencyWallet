package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.Config;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CoinAPIService {

    private static final String API_URL = "https://rest.coinapi.io/v1/assets";
    private static final String API_KEY = Config.get("COIN_API_KEY");
    private static final Gson GSON = new Gson();
    private static final int HTTP_OK = 200;
    public static final int UNAUTHORIZED_API_CODE = 401;
    public static final int TOO_MANY_REQUESTS = 429;

    private final HttpClient httpClient ;

    public CoinAPIService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CoinAPIService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<CryptoOffering> fetchAllCryptoOfferings() throws IOException, InterruptedException {
        LoggerUtil.logInfo("Sending request to CoinAPI...");
        validateApiKey();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
            .header("X-CoinAPI-Key", API_KEY)
            .header("Accept", "application/json")
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LoggerUtil.logError("Error sending request to CoinAPI", e);
            throw e;
        }
        LoggerUtil.logInfo("Received response from CoinAPI with status code: " + response.statusCode());

        if (response.statusCode() == TOO_MANY_REQUESTS) {
            LoggerUtil.logWarning("Rate limit exceeded. Try again later.");
            throw new IOException("Rate limit exceeded. Try again later.");
        } else if (response.statusCode() == UNAUTHORIZED_API_CODE) {
            LoggerUtil.logError("Invalid API key for CoinAPI.", null);
            throw new SecurityException("Invalid API key for CoinAPI.");
        } else if (response.statusCode() != HTTP_OK) {
            LoggerUtil.logError("Error fetching crypto data. Response code: " + response.statusCode(), null);
            throw new IOException("Error fetching crypto data. Response code: " + response.statusCode());
        }

        return parseCryptoOfferings(response.body());
    }

    private void validateApiKey() throws IOException {
        String apiKey = Config.get("COIN_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("API key is invalid or missing.");
        }
    }

    private List<CryptoOffering> parseCryptoOfferings(String jsonResponse) {
        LoggerUtil.logInfo("Parsing CoinAPI response...");
        List<CryptoOffering> offerings = new ArrayList<>();

        JsonArray assetsArray = GSON.fromJson(jsonResponse, JsonArray.class);

        for (int i = 0; i < assetsArray.size(); i++) {
            try {
                JsonObject asset = assetsArray.get(i).getAsJsonObject();

                if (asset.get("type_is_crypto").getAsInt() == 1) {
                    String assetId = asset.has("asset_id") ? asset.get("asset_id").getAsString() : "UNKNOWN";
                    String name = asset.has("name") ? asset.get("name").getAsString() : "UNKNOWN";
                    double price = asset.has("price_usd") ? asset.get("price_usd").getAsDouble() : 0.0;
                    double volumeUsd = asset.has("volume_1day_usd") ? asset.get("volume_1day_usd").getAsDouble() : 0.0;

                    offerings.add(new CryptoOffering(assetId, name, price, volumeUsd));
                }
            } catch (Exception e) {
                LoggerUtil.logError("Error parsing asset at index " + i, e);
            }
        }

        LoggerUtil.logInfo("Total crypto assets parsed: " + offerings.size());
        return offerings;
    }

}
