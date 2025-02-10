package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BuyCommandTest {

    @Mock
    private WalletService walletService;

    @Mock
    private CachedCoinAPIService cachedCoinAPIService;

    private BuyCommand buyCommand;

    private BuyCommand createBuyCommand(String[] args) {
        return new BuyCommand(walletService, cachedCoinAPIService, args);
    }

    @Test
    void testExecute_SuccessfulPurchase() throws IOException {
        String[] args = {"user1", "--offering=BTC", "--money=100.0"};
        buyCommand = createBuyCommand(args);

        Map<String, Double> offerings = new HashMap<>();
        offerings.put("BTC", 50.0);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 50.0, 1000000.0))
        );
        when(walletService.buyCrypto(eq("user1"), eq("BTC"), eq(2.0), eq(50.0))).thenReturn(true);

        String result = buyCommand.execute();
        assertEquals("Successfully bought 2.0 of BTC for $100.0", result);
    }

    @Test
    void testConstructor_MissingArguments() {
        String[] args = {"user1", "--offering=BTC"};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            createBuyCommand(args);
        });
        assertEquals("Usage: buy <username> --offering=<offering_code> --money=<amount>", thrown.getMessage());
    }

    @Test
    void testConstructor_InvalidAmount() {
        String[] args = {"user1", "--offering=BTC", "--money=-100.0"};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            createBuyCommand(args);
        });
        assertEquals("Amount must be a positive number.", thrown.getMessage());
    }

    @Test
    void testExecute_FailedToRetrievePrice() throws IOException {
        String[] args = {"user1", "--offering=BTC", "--money=100.0"};
        buyCommand = createBuyCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 0.0, 1000000.0))
        );

        String result = buyCommand.execute();

        assertEquals("Failed to retrieve the price for BTC", result);
    }

    @Test
    void testExecute_FailedPurchase() throws IOException {
        String[] args = {"user1", "--offering=BTC", "--money=100.0"};
        buyCommand = createBuyCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 50.0, 1000000.0))
        );
        when(walletService.buyCrypto(eq("user1"), eq("BTC"), eq(2.0), eq(50.0))).thenReturn(false);

        String result = buyCommand.execute();

        assertEquals("Purchase failed. Check your balance and input.", result);
    }

    @Test
    void testExecute_FailedToFetchPriceIOException() throws IOException {
        String[] args = {"user1", "--offering=BTC", "--money=100.0"};
        buyCommand = createBuyCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenThrow(new IOException("Network error"));

        String result = buyCommand.execute();

        assertEquals("Failed to retrieve the price for BTC", result);
    }
}
