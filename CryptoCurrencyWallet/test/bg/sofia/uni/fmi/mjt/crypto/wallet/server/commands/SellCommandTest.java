package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SellCommandTest {

    @Mock
    private WalletService walletService;

    @Mock
    private CachedCoinAPIService cachedCoinAPIService;

    private SellCommand sellCommand;

    private SellCommand createSellCommand(String[] args) {
        return new SellCommand(walletService, cachedCoinAPIService, args);
    }

    @Test
    void testExecute_SuccessfulSale() throws IOException {
        String[] args = {"user1", "--offering=BTC"};
        sellCommand = createSellCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 50.0, 1000000.0))
        );
        when(walletService.sellCrypto("user1", "BTC", 50.0)).thenReturn(true);

        String result = sellCommand.execute();

        assertEquals("Successfully sold all holdings of BTC at $50.0 per unit.", result);
    }

    @Test
    void testConstructor_MissingArguments() {
        String[] args = {"user1"};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            createSellCommand(args);
        });
        assertEquals("Usage: sell --offering=<offering_code>", thrown.getMessage());
    }

    @Test
    void testExecute_FailedToRetrievePrice() throws IOException {
        String[] args = {"user1", "--offering=BTC"};
        sellCommand = createSellCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 0.0, 1000000.0))
        );

        String result = sellCommand.execute();

        assertEquals("Failed to retrieve the price for BTC", result);
    }

    @Test
    void testExecute_FailedSale() throws IOException {
        String[] args = {"user1", "--offering=BTC"};
        sellCommand = createSellCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(
            List.of(new CryptoOffering("BTC", "Bitcoin", 50.0, 1000000.0))
        );
        when(walletService.sellCrypto("user1", "BTC", 50.0)).thenReturn(false);

        String result = sellCommand.execute();

        assertEquals("Sell failed. Check your holdings and try again.", result);
    }

    @Test
    void testExecute_FailedToFetchPriceIOException() throws IOException {
        String[] args = {"user1", "--offering=BTC"};
        sellCommand = createSellCommand(args);

        when(cachedCoinAPIService.getCryptoOfferings()).thenThrow(new IOException("Network error"));

        String result = sellCommand.execute();

        assertEquals("Failed to retrieve the price for BTC", result);
    }
}
