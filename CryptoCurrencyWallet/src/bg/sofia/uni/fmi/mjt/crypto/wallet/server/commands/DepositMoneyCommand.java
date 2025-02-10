package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

public class DepositMoneyCommand implements Command {
    public static final int ARGS_NUM = 2;
    private final WalletService walletService;
    private final String username;
    private final double amount;

    public DepositMoneyCommand(WalletService walletService, String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning(
                "Deposit-money command usage error. Missing arguments: deposit-money <username> <amount>");
            throw new IllegalArgumentException("Usage: deposit-money <username> <amount>");
        }

        this.walletService = walletService;
        this.username = args[0];

        try {
            this.amount = Double.parseDouble(args[1]);
            if (this.amount <= 0) {
                throw new IllegalArgumentException("Amount must be a positive number.");
            }
        } catch (NumberFormatException e) {
            LoggerUtil.logError("Invalid amount format. Amount for deposit is not double parseable.", e);
            throw new IllegalArgumentException("Invalid amount format. Please enter a valid number.");
        }
    }

    @Override
    public String execute() {
        boolean success = walletService.depositMoney(username, amount);

        if (success) {
            LoggerUtil.logInfo("Deposit successful for user: " + username + " with amount: " + amount);
            return "Successfully deposited $" + amount + " into your wallet.";
        } else {
            LoggerUtil.logWarning("Deposit failed for user: " + username + " with amount: " + amount);
            return "Deposit failed. Check your input and try again.";
        }
    }
}
