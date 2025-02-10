package bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;
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

    public static synchronized Map<String, User> loadUsers() {
        if (!Files.exists(Path.of(USERS_FILE))) {
            LoggerUtil.logInfo("Users file does not exist, returning empty user map.");
            return new ConcurrentHashMap<>();
        }

        try (FileReader reader = new FileReader(USERS_FILE)) {
            LoggerUtil.logInfo("Loading users from file: " + USERS_FILE);
            Type type = new TypeToken<Map<String, User>>() { }.getType();
            Map<String, User> users = GSON.fromJson(reader, type);
            LoggerUtil.logInfo("Users successfully loaded.");
            return users;
        } catch (IOException e) {
            LoggerUtil.logError("Error loading users from file: " + USERS_FILE, e);
            return new ConcurrentHashMap<>();
        }
    }

    public static synchronized void saveUsers(Map<String, User> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            LoggerUtil.logInfo("Saving users to file: " + USERS_FILE);
            GSON.toJson(users, writer);
            LoggerUtil.logInfo("Users successfully saved.");
        } catch (IOException e) {
            LoggerUtil.logError("Error saving users to file: " + USERS_FILE, e);
        }
    }
}
