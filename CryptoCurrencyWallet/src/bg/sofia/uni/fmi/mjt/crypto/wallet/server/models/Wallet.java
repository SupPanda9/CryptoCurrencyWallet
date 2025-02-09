package bg.sofia.uni.fmi.mjt.crypto.wallet.server.models;

import java.util.List;
import java.util.Map;

public record Wallet(double balance, Map<String, Double> cryptoHoldings, List<Transaction> transactionHistory) {

}
