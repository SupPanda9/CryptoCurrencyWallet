package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.UserService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

public class RegisterCommand implements Command {
    public static final int ARGS_NUM = 2;
    private final UserService userService;
    private final String username;
    private final String password;

    public RegisterCommand(String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning("Register command usage error. Missing arguments: register <username> <password>");
            throw new IllegalArgumentException("Usage: register <username> <password>");
        }

        this.userService = UserService.getInstance();
        this.username = args[0];
        this.password = args[1];

        LoggerUtil.logInfo("Registration attempt for username: " + username);
    }

    // for testing
    public RegisterCommand(String[] args, UserService userService) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning("Register command usage error. Missing arguments: register <username> <password>");
            throw new IllegalArgumentException("Usage: register <username> <password>");
        }

        this.userService = userService;
        this.username = args[0];
        this.password = args[1];

        LoggerUtil.logInfo("Registration attempt for username: " + username);
    }

    @Override
    public String execute() {
        boolean success = userService.register(username, password);

        if (success) {
            LoggerUtil.logInfo("User registered successfully: " + username);
            return "Registration successful!";
        } else {
            LoggerUtil.logWarning("Registration failed: username already taken: " + username);
            return "Username already taken. Try another one.";
        }
    }
}
