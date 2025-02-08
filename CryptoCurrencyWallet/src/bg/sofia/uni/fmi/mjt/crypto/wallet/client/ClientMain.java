package bg.sofia.uni.fmi.mjt.crypto.wallet.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

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
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            System.out.println("Client terminated.");
        }
    }

    private static void startClientInteraction(BufferedReader reader, PrintWriter writer, BufferedReader console)
        throws IOException {

        System.out.println("Type 'exit' to disconnect.");

        String input;
        while (true) {
            System.out.print("> ");
            input = console.readLine();

            if (input == null || input.equalsIgnoreCase("exit")) {
                System.out.println("Disconnecting from server...");
                break;
            }

            System.out.println("Sending to server: " + input); // Лог
            writer.println(input);
            writer.flush();

            String response = reader.readLine();
            if (response != null) {
                System.out.println("Server response: " + response);
            }
        }
    }
}
