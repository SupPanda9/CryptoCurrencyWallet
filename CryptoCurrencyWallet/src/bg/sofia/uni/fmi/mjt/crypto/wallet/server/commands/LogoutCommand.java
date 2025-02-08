package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection.ClientHandler;

public class LogoutCommand implements Command {
    private final String username;

    public LogoutCommand(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Username is required for logout.");
        }
        this.username = args[0];
    }

    @Override
    public String execute() {
        if (!ClientHandler.isUserLoggedIn(username)) {
            return "You are not logged in.";
        }

        ClientHandler.removeLoggedInUser(username);
        return "Logout successful!";
    }
}
