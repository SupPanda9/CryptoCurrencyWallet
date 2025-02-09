package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.Config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;

public class CoinAPITest {
//    private static final String API_URL = "https://rest.coinapi.io/v1/assets";
//    private static final String API_KEY = Config.get("COIN_API_KEY"); // Loaded from Config
//    private static final Gson GSON = new Gson();
//    private static final int HTTP_OK = 200;
//    private static final int MAX_OFFERINGS = 100;
//    private static final double MIN_VOLUME_USD = 1_000_000;
//
//    private static List<CryptoOffering> cache = new ArrayList<>();
//    private static Instant cacheTimestamp = Instant.EPOCH;
//
//    public static void main(String[] args) throws IOException {
//        System.out.println("Fetching crypto offerings...");
//
//        List<CryptoOffering> offerings = getCryptoOfferings();
//        System.out.println("✅ Received " + offerings.size() + " crypto assets.");
//
//        for (CryptoOffering offering : offerings) {
//            System.out.println(offering);
//        }
//    }
//
//    private static List<CryptoOffering> getCryptoOfferings() throws IOException {
//        if (!cache.isEmpty() && Instant.now().isBefore(cacheTimestamp.plusSeconds(180))) {
//            System.out.println("🟢 Using cached data (valid until: " + cacheTimestamp.plusSeconds(180) + ")");
//            return cache;
//        }
//
//        System.out.println("🔴 Cache expired or empty. Fetching fresh data...");
//        URL url = URI.create(API_URL).toURL();
//
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("X-CoinAPI-Key", API_KEY);
//        connection.setRequestProperty("Accept", "application/json");
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode != HTTP_OK) {
//            throw new IOException("Error fetching crypto data. Response code: " + responseCode);
//        }
//
//        Scanner scanner = new Scanner(connection.getInputStream());
//        StringBuilder jsonResponse = new StringBuilder();
//        while (scanner.hasNext()) {
//            jsonResponse.append(scanner.nextLine());
//        }
//        scanner.close();
//
//        cache = parseCryptoOfferings(jsonResponse.toString());
//        cacheTimestamp = Instant.now();
//
//        System.out.println("✅ New data cached at " + cacheTimestamp);
//        return cache;
//    }
//
//    private static List<CryptoOffering> parseCryptoOfferings(String jsonResponse) {
//        List<CryptoOffering> offerings = new ArrayList<>();
//        JsonArray assetsArray = GSON.fromJson(jsonResponse, JsonArray.class);
//
//        for (var element : assetsArray) {
//            JsonObject asset = element.getAsJsonObject();
//
//            if (!asset.has("type_is_crypto") || asset.get("type_is_crypto").getAsInt() != 1) {
//                continue;
//            }
//
//            if (!asset.has("volume_1day_usd") || asset.get("volume_1day_usd").isJsonNull()) {
//                continue;
//            }
//
//            double volumeUsd = asset.get("volume_1day_usd").getAsDouble();
//            if (volumeUsd < MIN_VOLUME_USD) {
//                continue; // Пропускаме валути с ниска ликвидност
//            }
//
//            String assetId = asset.has("asset_id") ? asset.get("asset_id").getAsString() : "UNKNOWN";
//            String name = asset.has("name") ? asset.get("name").getAsString() : "Unknown Coin";
//            double priceUsd = asset.has("price_usd") ? asset.get("price_usd").getAsDouble() : 0.0;
//
//            offerings.add(new CryptoOffering(assetId, name, priceUsd, ));
//
//            if (offerings.size() >= MAX_OFFERINGS) break; // Ограничаваме до 100
//        }
//
//        return offerings;
//    }
}
