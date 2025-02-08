package bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private static final String USERS_FILE = "users.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized void saveUsers(Map<String, User> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            GSON.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static synchronized Map<String, User> loadUsers() {
        if (!Files.exists(Path.of(USERS_FILE))) {
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(USERS_FILE)) {
            Type type = new TypeToken<Map<String, User>>() { }.getType();
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
}
