package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.UserService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

public class LoginCommand implements Command {
    public static final int ARGS_NUM = 2;
    private final UserService userService;
    private final String username;
    private final String password;

    public LoginCommand(String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning("Login command usage error. Missing arguments: login <username> <password>");
            throw new IllegalArgumentException("Usage: login <username> <password>");
        }

        this.userService = UserService.getInstance();
        this.username = args[0];
        this.password = args[1];

        LoggerUtil.logInfo("Login attempt for username: " + username);
    }

    @Override
    public String execute() {
        boolean success = userService.login(username, password);

        if (success) {
            LoggerUtil.logInfo("Login successful for username: " + username);
            return "Login successful!";
        } else {
            LoggerUtil.logWarning("Login failed for username: " + username);
            return "Invalid username or password.";
        }
    }
}
