package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

public class GetWalletSummaryCommand implements Command {
    public static final int ARGS_NUM = 1;
    private final WalletService walletService;
    private final String username;

    public GetWalletSummaryCommand(WalletService walletService, String[] args) {
        if (args.length < ARGS_NUM) {
            LoggerUtil.logWarning(
                "Get-wallet-summary command usage error. Missing argument: get-wallet-summary <username>");
            throw new IllegalArgumentException("Usage: get-wallet-summary <username>");
        }

        this.walletService = walletService;
        this.username = args[0];
    }

    @Override
    public String execute() {
        String walletSummary = walletService.getWalletSummary(username);
        LoggerUtil.logInfo("Successfully fetched wallet summary for user: " + username);
        return walletSummary;
    }
}
