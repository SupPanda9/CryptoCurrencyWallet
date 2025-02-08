package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.UserService;

public class LoginCommand implements Command {
    private final UserService userService;
    private final String username;
    private final String password;

    public LoginCommand(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: login <username> <password>");
        }

        this.userService = UserService.getInstance();
        this.username = args[0];
        this.password = args[1];
    }

    @Override
    public String execute() {
        boolean success = userService.login(username, password);
        return success ? "Login successful!" : "Invalid username or password.";
    }
}
