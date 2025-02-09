package bg.sofia.uni.fmi.mjt.crypto.wallet.server;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection.ClientHandler;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CoinAPIService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    private static final int PORT = 8080;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        CachedCoinAPIService cachedCoinAPIService = new CachedCoinAPIService(new CoinAPIService());
        CommandFactory commandFactory = new CommandFactory(cachedCoinAPIService);

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.println("Crypto Wallet Server started on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    executor.submit(new ClientHandler(clientSocket, commandFactory));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }

            System.out.println("Server is shutting down...");
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void stopServer() {
        running = false;
    }

}
