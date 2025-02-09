package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandFactory {
    private final Map<String, Function<String[], Command>> commands;
    private final WalletService walletService;

    public CommandFactory(CachedCoinAPIService cachedCoinAPIService, WalletService walletService) {
        this.commands = new HashMap<>();
        this.walletService = walletService;

        commands.put("register", RegisterCommand::new);
        commands.put("login", LoginCommand::new);
        commands.put("logout", LogoutCommand::new);
        commands.put("list-offerings", _ -> new ListOfferingsCommand(cachedCoinAPIService));
        commands.put("deposit-money", args -> new DepositMoneyCommand(walletService, args));
        commands.put("buy", args -> new BuyCommand(walletService, cachedCoinAPIService, args));
        commands.put("sell", args -> new SellCommand(walletService, cachedCoinAPIService, args));
        commands.put("get-wallet-summary", args -> new GetWalletSummaryCommand(walletService, args));
        commands.put("get-wallet-overall-summary",
            args -> new GetWalletOverallSummaryCommand(walletService, args, cachedCoinAPIService));
        commands.put("help", _ -> new HelpCommand());
    }

    public Command createCommand(String input, boolean isLoggedIn, String username) {
        String[] tokens = input.strip().split("\\s+");
        String commandName = tokens[0].toLowerCase();
        String[] args = tokens.length > 1 ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[0];

        System.out.println("input = " + commandName);

        if (commandName.equals("help")) {
            return new HelpCommand();
        }

        if (isLoggedIn && isRestrictedWhileLoggedIn(commandName)) {
            return () -> getRestrictedMessage(commandName);
        }

        if (!isLoggedIn && isLoginRequired(commandName)) {
            return () -> "You must be logged in to use this command!";
        }

        if (isLoggedIn && commandName.equals("logout")) {
            return new LogoutCommand(new String[]{username});
        }

        return getCommand(commandName, args, username);
    }

    private static boolean isRestrictedWhileLoggedIn(String commandName) {
        return commandName.equals("register") || commandName.equals("login");
    }

    private static boolean isLoginRequired(String commandName) {
        return !isRestrictedWhileLoggedIn(commandName);
    }

    private static String getRestrictedMessage(String commandName) {
        return commandName.equals("register")
            ? "You are already logged in. Log out to register a new account."
            : "You are already logged in. Log out to log into another account.";
    }

    private Command getCommand(String commandName, String[] args, String username) {
        Function<String[], Command> commandFunction = commands.get(commandName);
        if (commandFunction == null) {
            return () -> "Unknown command: " + commandName;
        }

        try {
            // If the command needs a username, prepend it to args
            if (commandNeedsUsername(commandName)) {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = username;
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return commandFunction.apply(newArgs);
            }

            return commandFunction.apply(args);
        } catch (IllegalArgumentException e) {
            return () -> e.getMessage();
        } catch (Exception e) {
            return () -> "An unexpected error occurred: " + e.getMessage();
        }
    }

    private boolean commandNeedsUsername(String commandName) {
        return commandName.equals("deposit-money") || commandName.equals("buy")
            || commandName.equals("sell") || commandName.equals("get-wallet-summary")
            || commandName.equals("get-wallet-overall-summary");
    }
}
