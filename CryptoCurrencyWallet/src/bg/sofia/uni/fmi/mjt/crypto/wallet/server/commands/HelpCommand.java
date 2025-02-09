package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

public class HelpCommand implements Command {

    @Override
    public String execute() {
        return """
               Available commands:
               
               User Commands:
               - register <username> <password>   → Register a new account.
               - login <username> <password>      → Log in to an existing account.
               - logout                           → Log out from your account.
               
               Wallet Commands:
               - deposit-money --money=<amount>   → Deposit money into your wallet.
               - get-wallet-summary               → View your wallet balance, crypto holdings, and transactions.
               - get-wallet-overall-summary       → Get an overview of your total investment and profit/loss.
               
               Trading Commands:
               - list-offerings                   → View available cryptocurrencies for trading.
               - buy --offering=<asset_id> --money=<amount> → Buy cryptocurrency using your balance.
               - sell --offering=<asset_id>       → Sell all your holdings of a given cryptocurrency.
               
               Miscellaneous:
               - help                             → Show this list of commands.
               """;
    }

}
