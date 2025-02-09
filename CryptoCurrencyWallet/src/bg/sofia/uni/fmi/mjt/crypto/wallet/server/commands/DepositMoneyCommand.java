package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

public class DepositMoneyCommand implements Command {
    private final WalletService walletService;
    private final String username;
    private final double amount;

    public DepositMoneyCommand(WalletService walletService, String[] args) {
        if (args.length < 2) {
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
            throw new IllegalArgumentException("Invalid amount format. Please enter a valid number.");
        }
    }

    @Override
    public String execute() {
        boolean success = walletService.depositMoney(username, amount);
        return success ? "Successfully deposited $" + amount + " into your wallet."
            : "Deposit failed. Check your input and try again.";
    }
}
