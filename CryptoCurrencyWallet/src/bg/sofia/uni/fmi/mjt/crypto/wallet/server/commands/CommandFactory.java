package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandFactory {
    private final Map<String, Function<String[], Command>> commands;

    public CommandFactory(CachedCoinAPIService cachedCoinAPIService) {
        this.commands = new HashMap<>();

        commands.put("register", RegisterCommand::new);
        commands.put("login", LoginCommand::new);
        commands.put("logout", LogoutCommand::new);
        commands.put("list-offerings", _ -> new ListOfferingsCommand(cachedCoinAPIService));
    }

    /* COMMANDS.put("deposit", DepositMoneyCommand::new);
    COMMANDS.put("buy", BuyCommand::new);
    COMMANDS.put("sell", SellCommand::new);
    COMMANDS.put("summary", GetWalletSummaryCommand::new);
    COMMANDS.put("overall-summary", GetWalletOverallSummaryCommand::new);
    */

    public Command createCommand(String input, boolean isLoggedIn, String username) {
        String[] tokens = input.strip().split("\\s+");
        String commandName = tokens[0].toLowerCase();
        String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[0];

        System.out.println("input = " + commandName);

        if (!isLoggedIn && !isLoginOrRegister(commandName)) {
            return () -> "You must be logged in to use this command!";
        }

        if (isLoggedIn && isLoginOrRegister(commandName)) {
            return () -> getRestrictedMessage(commandName);
        }

        if (isLoggedIn && commandName.equals("logout")) {
            return new LogoutCommand(new String[]{username});
        }

        return getCommand(commandName, args);
    }

    private static boolean isLoginOrRegister(String commandName) {
        return commandName.equals("register") || commandName.equals("login");
    }

    private static String getRestrictedMessage(String commandName) {
        return commandName.equals("register")
            ? "You are already logged in. Log out to register a new account."
            : "You are already logged in. Log out to log into another account.";
    }

    private Command getCommand(String commandName, String[] args) {
        Function<String[], Command> commandFunction = commands.get(commandName);
        if (commandFunction == null) {
            return () -> "Unknown command: " + commandName;
        }

        try {
            return commandFunction.apply(args);
        } catch (IllegalArgumentException e) {
            return () -> e.getMessage();
        } catch (Exception e) {
            return () -> "An unexpected error occurred: " + e.getMessage();
        }
    }
}
