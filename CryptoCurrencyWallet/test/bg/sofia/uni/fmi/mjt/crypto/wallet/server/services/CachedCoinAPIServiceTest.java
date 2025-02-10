package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachedCoinAPIServiceTest {
    private static final long CACHE_EXPIRY_TIME_MS = 30 * 60 * 1000;

    private CachedCoinAPIService cachedCoinAPIService;

    @Mock
    private CoinAPIService mockCoinAPIService;
    @Mock private CryptoOffering mockOffering;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        cachedCoinAPIService = new CachedCoinAPIService(mockCoinAPIService);
    }

    @Test
    public void testGetCryptoOfferings_WithValidCache() throws IOException, InterruptedException {
        List<CryptoOffering> mockOfferings = List.of(mockOffering);
        when(mockCoinAPIService.fetchAllCryptoOfferings()).thenReturn(mockOfferings);

        cachedCoinAPIService.getCryptoOfferings();
        cachedCoinAPIService.getCryptoOfferings();

        verify(mockCoinAPIService, times(1)).fetchAllCryptoOfferings();
    }

    @Test
    public void testGetCryptoOfferings_RefreshCacheWhenExpired() throws IOException, InterruptedException {
        List<CryptoOffering> mockOfferings = List.of(mockOffering);
        when(mockCoinAPIService.fetchAllCryptoOfferings()).thenReturn(mockOfferings);

        cachedCoinAPIService.getCryptoOfferings();

        cachedCoinAPIService.setLastFetchTimeForTesting(Instant.now().minusMillis(CACHE_EXPIRY_TIME_MS + 1000));

        cachedCoinAPIService.getCryptoOfferings();

        verify(mockCoinAPIService, times(2)).fetchAllCryptoOfferings();
    }


    @Test
    public void testGetCryptoOfferings_WhenFetchFails() throws IOException, InterruptedException {
        when(mockCoinAPIService.fetchAllCryptoOfferings()).thenThrow(new IOException("API error"));

        assertThrows(IOException.class, () -> cachedCoinAPIService.getCryptoOfferings());

        verify(mockCoinAPIService, times(1)).fetchAllCryptoOfferings();
    }

    @Test
    public void testGetCryptoOfferings_WhenCacheIsEmpty() throws IOException, InterruptedException{
        when(mockCoinAPIService.fetchAllCryptoOfferings()).thenReturn(List.of());

        List<CryptoOffering> offerings = cachedCoinAPIService.getCryptoOfferings();
        assertTrue(offerings.isEmpty());

        verify(mockCoinAPIService, times(1)).fetchAllCryptoOfferings();
    }
}
