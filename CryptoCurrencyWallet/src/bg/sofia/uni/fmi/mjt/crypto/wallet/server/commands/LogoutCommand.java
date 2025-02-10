package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection.ClientHandler;

public class LogoutCommand implements Command {
    private final String username;
    private final String sessionId;

    public LogoutCommand(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    @Override
    public String execute() {
        String storedSessionId = ClientHandler.getSessionIdForUser(username);

        if (storedSessionId == null) {
            return "You are not logged in.";
        }

        if (!storedSessionId.equals(sessionId)) {
            return "You can only log out from your own session!";
        }

        ClientHandler.removeLoggedInUser(username, sessionId);
        return "Logout successful!";
    }
}
