package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.Config;
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
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final int HTTP_OK = 200;
    public static final int UNAUTHORIZED_API_CODE = 401;
    public static final int TOO_MANY_REQUESTS = 429;

    public List<CryptoOffering> fetchAllCryptoOfferings() throws IOException, InterruptedException {
        System.out.println("Sending request to CoinAPI...");

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("X-CoinAPI-Key", API_KEY)
            .header("Accept", "application/json").build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending request to CoinAPI: " + e.getMessage()); // Debug log
            throw e;
        }

        System.out.println("Received response from CoinAPI with status code: " + response.statusCode());

        if (response.statusCode() == TOO_MANY_REQUESTS) {
            throw new IOException("Rate limit exceeded. Try again later.");
        } else if (response.statusCode() == UNAUTHORIZED_API_CODE) {
            throw new SecurityException("Invalid API key for CoinAPI.");
        } else if (response.statusCode() != HTTP_OK) {
            throw new IOException("Error fetching crypto data. Response code: " + response.statusCode());
        }

        return parseCryptoOfferings(response.body());
    }

    private List<CryptoOffering> parseCryptoOfferings(String jsonResponse) {
        System.out.println("Parsing CoinAPI response...");
        List<CryptoOffering> offerings = new ArrayList<>();

        JsonArray assetsArray = GSON.fromJson(jsonResponse, JsonArray.class);
        System.out.println("Number of assets received: " + assetsArray.size());
        System.out.println("First asset: " + assetsArray.get(0));
        System.out.println("Last asset: " + assetsArray.get(assetsArray.size() - 1));

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
                System.err.println("❌ Error parsing asset at index " + i + ": " + assetsArray.get(i));
                e.printStackTrace();
            }
        }

        System.out.println("✅ Total crypto assets parsed: " + offerings.size());
        return offerings;
    }

    // Still not sure if I will need it
    public CryptoOffering fetchCryptoById(String assetId) throws IOException, InterruptedException {
        // First, check the cache
        List<CryptoOffering> cached = fetchAllCryptoOfferings();
        for (CryptoOffering offering : cached) {
            if (offering.assetId().equalsIgnoreCase(assetId)) {
                return offering; // Found in cache
            }
        }

        // If not found, fetch from API
        return fetchCryptoByIdFromAPI(assetId);
    }

    private CryptoOffering fetchCryptoByIdFromAPI(String assetId) throws IOException, InterruptedException {
        String urlString = "https://rest.coinapi.io/v1/assets/" + assetId;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).header("X-CoinAPI-Key", API_KEY)
            .header("Accept", "application/json").build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == TOO_MANY_REQUESTS) {
            throw new IOException("Rate limit exceeded. Try again later.");
        } else if (response.statusCode() == UNAUTHORIZED_API_CODE) {
            throw new SecurityException("Invalid API key for CoinAPI.");
        } else if (response.statusCode() != HTTP_OK) {
            throw new IOException(
                "Error fetching crypto data for " + assetId + ". Response code: " + response.statusCode());
        }

        JsonArray assetsArray = GSON.fromJson(response.body(), JsonArray.class);
        if (assetsArray.isEmpty()) {
            return null;
        }

        JsonObject asset = assetsArray.get(0).getAsJsonObject();
        return new CryptoOffering(asset.get("asset_id").getAsString(), asset.get("name").getAsString(),
            asset.has("price_usd") ? asset.get("price_usd").getAsDouble() : 0.0,
            asset.has("volume_1day_usd") ? asset.get("volume_1day_usd").getAsDouble() : 0.0);
    }
}
