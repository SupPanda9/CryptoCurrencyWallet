package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CoinAPIServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private CoinAPIService coinAPIService;
    private MockedStatic<Config> mockConfig;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);

        mockConfig = Mockito.mockStatic(Config.class);
        mockConfig.when(() -> Config.get("COIN_API_KEY")).thenReturn("mock-api-key");

        coinAPIService = new CoinAPIService(mockHttpClient);

        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
            .thenReturn(mockHttpResponse);
    }

    @Test
    public void testFetchAllCryptoOfferings_Success() throws IOException, InterruptedException {
        String mockResponse = "[{\"asset_id\": \"BTC\", \"name\": \"Bitcoin\", \"price_usd\": 40000.0, \"volume_1day_usd\": 5000000, \"type_is_crypto\": 1}]";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(mockResponse);

        List<CryptoOffering> offerings = coinAPIService.fetchAllCryptoOfferings();

        assertNotNull(offerings);
        assertEquals(1, offerings.size());
        assertEquals("BTC", offerings.getFirst().assetId());
        assertEquals("Bitcoin", offerings.getFirst().name());
        assertEquals(40000.0, offerings.getFirst().priceUsd());
        assertEquals(5000000, offerings.getFirst().volumeUsd());
    }

    @Test
    public void testFetchAllCryptoOfferings_TooManyRequests() {
        when(mockHttpResponse.statusCode()).thenReturn(CoinAPIService.TOO_MANY_REQUESTS);

        assertThrows(IOException.class, () -> coinAPIService.fetchAllCryptoOfferings());
    }

    @Test
    public void testFetchAllCryptoOfferings_Unauthorized() {
        when(mockHttpResponse.statusCode()).thenReturn(CoinAPIService.UNAUTHORIZED_API_CODE);

        assertThrows(SecurityException.class, () -> coinAPIService.fetchAllCryptoOfferings());
    }

    @Test
    public void testFetchAllCryptoOfferings_FailedResponse() {
        when(mockHttpResponse.statusCode()).thenReturn(500);

        assertThrows(IOException.class, () -> coinAPIService.fetchAllCryptoOfferings());
    }

    @AfterEach
    public void tearDown() {
        mockConfig.close();
    }
}
