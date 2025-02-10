package bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.env";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CONFIG_FILE))) {
            LoggerUtil.logInfo("Loading configuration from file: " + CONFIG_FILE);
            PROPERTIES.load(reader);
            LoggerUtil.logInfo("Configuration file loaded successfully.");
        } catch (IOException e) {
            LoggerUtil.logError("Error loading configuration file: " + CONFIG_FILE, e);
            throw new RuntimeException("Error loading configuration file: " + CONFIG_FILE, e);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            LoggerUtil.logWarning("Property not found: " + key);
        }
        return value;
    }
}
