package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

public class UserService {
    private static UserService instance;
    private final Map<String, User> users;

    private UserService() {
        this.users = Storage.loadUsers();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public synchronized boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }

        String hashedPassword = hashPassword(password);
        users.put(username, new User(username, hashedPassword));
        Storage.saveUsers(users);
        return true;
    }

    public synchronized boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }

        User user = users.get(username);
        return user.password().equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
}
