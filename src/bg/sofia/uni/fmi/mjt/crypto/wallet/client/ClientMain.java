package bg.sofia.uni.fmi.mjt.crypto.wallet.client;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final String END_OF_RESPONSE = "<END_OF_RESPONSE>";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            String welcome = reader.readLine();
            if (welcome != null) {
                System.out.println(welcome);
            }

            startClientInteraction(reader, writer, console);

        } catch (IOException e) {
            LoggerUtil.logError("Error connecting to server", e);
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            LoggerUtil.logInfo("Client terminated.");
            System.out.println("Client terminated.");
        }
    }

    private static void startClientInteraction(BufferedReader reader, PrintWriter writer, BufferedReader console) {
        System.out.println("Type 'exit' to disconnect.");

        String input;
        try {
            while (true) {
                System.out.print("> ");
                input = console.readLine();

                if (input == null || input.equalsIgnoreCase("exit")) {
                    LoggerUtil.logInfo("Client is disconnecting...");
                    System.out.println("Disconnecting from server...");
                    break;
                }

                writer.println(input);
                writer.flush();
                LoggerUtil.logInfo("Sent command to server: " + input);

                String response = readServerResponse(reader);
                System.out.println(response);
            }
        } catch (IOException e) {
            LoggerUtil.logError("Error during client-server interaction", e);
            System.err.println("Error communicating with the server: " + e.getMessage());
        }
    }

    private static String readServerResponse(BufferedReader reader) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(END_OF_RESPONSE)) {
                break;
            }
            responseBuilder.append(line).append("\n");
        }
        return responseBuilder.toString().strip();
    }

}
