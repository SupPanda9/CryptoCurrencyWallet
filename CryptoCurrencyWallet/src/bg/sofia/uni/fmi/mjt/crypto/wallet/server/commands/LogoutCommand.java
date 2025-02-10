package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection.ClientHandler;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

public class LogoutCommand implements Command {
    private final String username;
    private final String sessionId;

    public LogoutCommand(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;

        LoggerUtil.logInfo("Logout attempt for username: " + username);
    }

    @Override
    public String execute() {
        String storedSessionId = ClientHandler.getSessionIdForUser(username);

        if (storedSessionId == null) {
            LoggerUtil.logWarning("Logout failed: user " + username + " is not logged in.");
            return "You are not logged in.";
        }

        if (!storedSessionId.equals(sessionId)) {
            LoggerUtil.logWarning(
                "Logout failed: user " + username + " attempted to log out from a different session.");
            return "You can only log out from your own session!";
        }

        ClientHandler.removeLoggedInUser(username, sessionId);
        LoggerUtil.logInfo("User logged out successfully: " + username);
        return "Logout successful!";
    }
}
