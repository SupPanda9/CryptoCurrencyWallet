package bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.Command;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.CommandFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static final Map<String, String> LOGGED_USERS = new ConcurrentHashMap<>();
    private final Socket clientSocket;
    private final CommandFactory commandFactory;
    private String username = null;
    private static final String END_OF_RESPONSE = "<END_OF_RESPONSE>";

    public ClientHandler(Socket clientSocket, CommandFactory commandFactory) {
        this.clientSocket = clientSocket;
        this.commandFactory = commandFactory;
    }

    @Override
    public void run() {
        System.out.println("Handling new client: " + clientSocket.getInetAddress());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {

            writer.println("Welcome to the Crypto Wallet Server!");
            handleClientCommands(reader, writer);

        } catch (IOException e) {
            System.err.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            removeLoggedInUser(username);
        }
    }

    private void handleClientCommands(BufferedReader reader, PrintWriter writer) throws IOException {
        String input;
        while ((input = reader.readLine()) != null) {
            input = input.strip();
            if (input.equalsIgnoreCase("exit")) {
                writer.println("Goodbye!");
                writer.println(END_OF_RESPONSE);
                writer.flush();
                break;
            }

            if (input.startsWith("login ") && !handleLoginCommand(input, writer)) {
                continue;
            }

            if (!input.startsWith("login")) {
                Command command = commandFactory.createCommand(input, isLoggedIn(), username);
                String response = command.execute();
                System.out.println("Sending response to client: \n" + response); // 🔍 Debug log

                writer.println(response);
                writer.println(END_OF_RESPONSE);
                writer.flush();
            }
        }
    }

    private boolean handleLoginCommand(String input, PrintWriter writer) {
        String attemptedUsername = input.split("\\s+")[1];

        if (LOGGED_USERS.containsKey(attemptedUsername)) {
            writer.println("User is already logged in from another session.");
            writer.println(END_OF_RESPONSE);
            writer.flush();

            return false;
        }

        Command command = commandFactory.createCommand(input, isLoggedIn(), username);
        String response = command.execute();

        if (response.startsWith("Login successful")) {
            username = attemptedUsername;
            LOGGED_USERS.put(username, clientSocket.toString());
            writer.println(response);
            writer.println(END_OF_RESPONSE);
            writer.flush();
            return true;
        }

        writer.println(response);
        writer.println(END_OF_RESPONSE);
        writer.flush();
        return false;
    }

    private boolean isLoggedIn() {
        return username != null && LOGGED_USERS.containsKey(username);
    }

    public static boolean isUserLoggedIn(String username) {
        return LOGGED_USERS.containsKey(username);
    }

    public static void removeLoggedInUser(String username) {
        if (username != null) {
            LOGGED_USERS.remove(username);
        }
    }
}
