package bg.sofia.uni.fmi.mjt.crypto.wallet.server.models;

import java.time.Instant;

public record Transaction(TransactionType type, String assetId, double amount, double cryptoQuantity,
                          double priceAtTransaction, long timestamp) {

    public Transaction(TransactionType type, double amount) {
        this(type, null, amount, 0, 0, Instant.now().getEpochSecond());
    }

    public Transaction(TransactionType type, String assetId, double amount, double cryptoQuantity,
                       double priceAtTransaction) {
        this(type, assetId, amount, cryptoQuantity, priceAtTransaction, Instant.now().getEpochSecond());
    }

    public Instant getTimestamp() {
        return Instant.ofEpochSecond(timestamp);
    }

}
