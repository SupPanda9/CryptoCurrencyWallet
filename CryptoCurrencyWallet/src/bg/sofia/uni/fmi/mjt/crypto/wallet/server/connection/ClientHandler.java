package bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.Command;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static final Map<String, String> LOGGED_USERS = new ConcurrentHashMap<>();
    private static final Set<ClientHandler> ACTIVE_CLIENTS = ConcurrentHashMap.newKeySet();
    private final Socket clientSocket;
    private final CommandFactory commandFactory;
    private String username = null;
    private String sessionId = null;
    private static final String END_OF_RESPONSE = "<END_OF_RESPONSE>";

    public ClientHandler(Socket clientSocket, CommandFactory commandFactory) {
        this.clientSocket = clientSocket;
        this.commandFactory = commandFactory;
        ACTIVE_CLIENTS.add(this);
        LoggerUtil.logInfo("New client connected: " + clientSocket.getInetAddress());
    }

    @Override
    public void run() {
        sessionId = clientSocket.toString();
        LoggerUtil.logInfo("Handling new client session: " + sessionId);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {

            writer.println("Welcome to the Crypto Wallet Server!");
            handleClientCommands(reader, writer);

        } catch (IOException e) {
            LoggerUtil.logError("Client communication error with session: " + sessionId, e);
        } finally {
            removeLoggedInUser(username, sessionId);
            LoggerUtil.logInfo("Client session " + sessionId + " has been closed.");
        }
    }

    private void handleClientCommands(BufferedReader reader, PrintWriter writer) throws IOException {
        String input;
        while ((input = reader.readLine()) != null) {
            input = input.strip();
            LoggerUtil.logInfo("Received command from client: " + input);

            if (input.equalsIgnoreCase("exit")) {
                writer.println("Goodbye!");
                writer.println(END_OF_RESPONSE);
                writer.flush();
                LoggerUtil.logInfo("Client " + sessionId + " exited the session.");
                break;
            }

            if (input.startsWith("login ") && !handleLoginCommand(input, writer)) {
                continue;
            }

            if (!input.startsWith("login")) {
                Command command = commandFactory.createCommand(input, isLoggedIn(), username);
                String response = command.execute();
                LoggerUtil.logInfo("Sending response to client: \n" + response);

                writer.println(response);
                writer.println(END_OF_RESPONSE);
                writer.flush();
            }
        }
    }

    private boolean handleLoginCommand(String input, PrintWriter writer) {
        String attemptedUsername = input.split("\\s+")[1];
        LoggerUtil.logInfo("Attempting to log in with username: " + attemptedUsername);
        if (LOGGED_USERS.containsKey(attemptedUsername)) {
            writer.println("User is already logged in from another session.");
            writer.println(END_OF_RESPONSE);
            writer.flush();
            LoggerUtil.logWarning("Login failed for user " + attemptedUsername + ": already logged in.");
            return false;
        }

        Command command = commandFactory.createCommand(input, isLoggedIn(), username);
        String response = command.execute();

        if (response.startsWith("Login successful")) {
            username = attemptedUsername;
            LOGGED_USERS.put(username, sessionId);
            writer.println(response);
            writer.println(END_OF_RESPONSE);
            writer.flush();
            LoggerUtil.logInfo("Login successful for user: " + username);
            return true;
        }

        writer.println(response);
        writer.println(END_OF_RESPONSE);
        writer.flush();
        LoggerUtil.logWarning("Login failed for user " + attemptedUsername + ": " + response);
        return false;
    }

    private boolean isLoggedIn() {
        return username != null && LOGGED_USERS.containsKey(username);
    }

    public static void removeLoggedInUser(String username, String sessionId) {
        if (username != null && LOGGED_USERS.containsKey(username)) {
            if (LOGGED_USERS.get(username).equals(sessionId)) {
                LOGGED_USERS.remove(username);

                for (ClientHandler handler : ACTIVE_CLIENTS) {
                    if (handler.username != null && handler.username.equals(username) &&
                        handler.sessionId.equals(sessionId)) {
                        handler.username = null;
                        break;
                    }
                }
                LoggerUtil.logInfo("User " + username + " logged out.");
            }
        }
    }

    public static String getSessionIdForUser(String username) {
        return LOGGED_USERS.get(username);
    }
}
