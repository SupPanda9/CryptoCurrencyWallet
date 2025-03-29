package bg.sofia.uni.fmi.mjt.crypto.wallet.server;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection.ClientHandler;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int PORT = 8080;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        initShutdownHook();
        startServer();
    }

    private static void startServer() {
        CommandFactory commandFactory =
            new CommandFactory(new CachedCoinAPIService(new CoinAPIService()), WalletService.getInstance());

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            LoggerUtil.logInfo("Server started on port " + PORT);

            while (running) {
                acceptClient(serverSocket, executor, commandFactory);
            }

            LoggerUtil.logInfo("Server is shutting down...");
        } catch (IOException e) {
            LoggerUtil.logError("Server error", e);
        }
    }

    private static void acceptClient(ServerSocket serverSocket, ExecutorService executor,
                                     CommandFactory commandFactory) {
        try {
            Socket clientSocket = serverSocket.accept();
            LoggerUtil.logInfo("New client connected: " + clientSocket.getInetAddress());
            executor.submit(new ClientHandler(clientSocket, commandFactory));
        } catch (IOException e) {
            if (running) {
                LoggerUtil.logError("Error accepting client connection", e);
            }
        }
    }

    private static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.logInfo("Shutdown hook triggered. Stopping server...");
            stopServer();
            LoggerUtil.close();
        }));
    }

    public static void stopServer() {
        running = false;
        LoggerUtil.logInfo("Server is stopping...");
    }
}
