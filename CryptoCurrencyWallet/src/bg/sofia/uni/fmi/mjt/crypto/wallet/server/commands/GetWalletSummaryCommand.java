package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;

public class GetWalletSummaryCommand implements Command {
    private final WalletService walletService;
    private final String username;

    public GetWalletSummaryCommand(WalletService walletService, String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: get-wallet-summary <username>");
        }

        this.walletService = walletService;
        this.username = args[0];
    }

    @Override
    public String execute() {
        return walletService.getWalletSummary(username);
    }
}
