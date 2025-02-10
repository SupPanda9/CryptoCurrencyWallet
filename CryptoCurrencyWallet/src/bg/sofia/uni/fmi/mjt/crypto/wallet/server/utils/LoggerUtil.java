package bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    private static final String LOG_FILE_PATH = "server.log";
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;

    static {
        try {
            fileHandler = new FileHandler(LOG_FILE_PATH, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);

            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);
            LOGGER.setUseParentHandlers(false);

        } catch (IOException e) {
            System.err.println("Failed to initialize LOGGER: " + e.getMessage());
        }
    }

    public static void logError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logWarning(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    public static void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }

    // Затваряме лог обработчиците при затваряне на ресурса
    public static void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
        if (consoleHandler != null) {
            consoleHandler.close();
        }
    }
}
