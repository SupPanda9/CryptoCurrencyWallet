package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetWalletOverallSummaryCommandTest {

    private WalletService walletService;
    private CachedCoinAPIService cachedCoinAPIService;
    private GetWalletOverallSummaryCommand command;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        cachedCoinAPIService = mock(CachedCoinAPIService.class);
    }

    @Test
    void testExecute_SuccessfulFetch() throws IOException {
        CryptoOffering offering1 = mock(CryptoOffering.class);
        when(offering1.assetId()).thenReturn("BTC");
        when(offering1.priceUsd()).thenReturn(50000.0);

        CryptoOffering offering2 = mock(CryptoOffering.class);
        when(offering2.assetId()).thenReturn("ETH");
        when(offering2.priceUsd()).thenReturn(1500.0);

        List<CryptoOffering> offerings = List.of(offering1, offering2);
        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(offerings);

        Map<String, Double> walletSummary = new HashMap<>();
        walletSummary.put("BTC", 0.5);
        walletSummary.put("ETH", 5.0);
        when(walletService.getWalletOverallSummary(eq("user1"), anyMap())).thenReturn("Your wallet contains: 0.5 BTC, 5 ETH.");

        String[] args = {"user1"};
        command = new GetWalletOverallSummaryCommand(walletService, args, cachedCoinAPIService);

        String result = command.execute();
        assertTrue(result.contains("Your wallet contains: 0.5 BTC, 5 ETH"));
        verify(cachedCoinAPIService).getCryptoOfferings();
        verify(walletService).getWalletOverallSummary(eq("user1"), anyMap());
    }

    @Test
    void testExecute_MissingArgument() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new GetWalletOverallSummaryCommand(walletService, new String[] {}, cachedCoinAPIService);
        });
        assertEquals("Usage: get-wallet-overall-summary <username>", thrown.getMessage());
    }

    @Test
    void testExecute_APIError() throws IOException {
        when(cachedCoinAPIService.getCryptoOfferings()).thenThrow(new IOException("API error"));

        String[] args = {"user1"};
        command = new GetWalletOverallSummaryCommand(walletService, args, cachedCoinAPIService);

        String result = command.execute();
        assertTrue(result.contains("Failed to fetch current prices"));
        verify(cachedCoinAPIService).getCryptoOfferings();
    }
}
