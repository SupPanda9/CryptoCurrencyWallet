package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListOfferingsCommandTest {

    private CachedCoinAPIService cachedCoinAPIService;
    private ListOfferingsCommand listOfferingsCommand;

    @BeforeEach
    void setUp() {
        cachedCoinAPIService = mock(CachedCoinAPIService.class);
        listOfferingsCommand = new ListOfferingsCommand(cachedCoinAPIService);
    }

    @Test
    void testExecute_SuccessfulFetch() throws IOException {
        CryptoOffering offering1 = mock(CryptoOffering.class);
        when(offering1.assetId()).thenReturn("BTC");
        when(offering1.name()).thenReturn("Bitcoin");
        when(offering1.priceUsd()).thenReturn(50000.0);
        when(offering1.volumeUsd()).thenReturn(2000000.0);

        CryptoOffering offering2 = mock(CryptoOffering.class);
        when(offering2.assetId()).thenReturn("ETH");
        when(offering2.name()).thenReturn("Ethereum");
        when(offering2.priceUsd()).thenReturn(1500.0);
        when(offering2.volumeUsd()).thenReturn(1500000.0);

        List<CryptoOffering> offerings = Arrays.asList(offering1, offering2);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(offerings);

        String result = listOfferingsCommand.execute();

        assertTrue(result.contains("Bitcoin"));
        assertTrue(result.contains("Ethereum"));
        assertTrue(result.contains("50 000,00"));
        assertTrue(result.contains("1 500 000,00"));
        verify(cachedCoinAPIService).getCryptoOfferings();
    }

    @Test
    void testExecute_NoOfferingsMeetCriteria() throws IOException {
        CryptoOffering offering1 = mock(CryptoOffering.class);
        when(offering1.assetId()).thenReturn("BTC");
        when(offering1.name()).thenReturn("Bitcoin");
        when(offering1.priceUsd()).thenReturn(0.005);
        when(offering1.volumeUsd()).thenReturn(500000.0);

        List<CryptoOffering> offerings = List.of(offering1);

        when(cachedCoinAPIService.getCryptoOfferings()).thenReturn(offerings);

        String result = listOfferingsCommand.execute();

        assertEquals("No high-value cryptocurrency offerings available.", result);
        verify(cachedCoinAPIService).getCryptoOfferings();
    }

    @Test
    void testExecute_APIError() throws IOException {
        when(cachedCoinAPIService.getCryptoOfferings()).thenThrow(new IOException("API error"));

        String result = listOfferingsCommand.execute();

        assertTrue(result.startsWith("Error fetching crypto offerings:"));
        verify(cachedCoinAPIService).getCryptoOfferings();
    }
}
