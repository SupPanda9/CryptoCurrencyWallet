package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Wallet;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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
            LoggerUtil.logWarning("Registration failed: User already exists with username: " + username);
            return false;
        }

        String hashedPassword = hashPassword(password);
        Wallet emptyWallet = new Wallet(0.0, new HashMap<>(), new ArrayList<>());

        users.put(username, new User(username, hashedPassword, emptyWallet));
        LoggerUtil.logInfo("New user registered: " + username);
        Storage.saveUsers(users);
        Storage.loadUsers();
        WalletService.getInstance().reloadUsers();
        return true;
    }

    public synchronized boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            LoggerUtil.logWarning("Login failed: User not found: " + username);
            return false;
        }

        User user = users.get(username);
        boolean isValid = user.password().equals(hashPassword(password));

        if (isValid) {
            LoggerUtil.logInfo("User logged in successfully: " + username);
        } else {
            LoggerUtil.logWarning("Login failed: Incorrect password for user: " + username);
        }

        return isValid;
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
