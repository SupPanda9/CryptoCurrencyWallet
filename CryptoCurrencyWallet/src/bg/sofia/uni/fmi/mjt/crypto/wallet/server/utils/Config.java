package bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.env";
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(Files.newBufferedReader(Paths.get(CONFIG_FILE)));
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration file: " + CONFIG_FILE, e);
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}
