package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.User;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.Wallet;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

public class WalletServiceTest {

    private static WalletService walletService;
    private static Map<String, User> users;
    private static MockedStatic<Storage> mockedStorage;

    private static final String TEST_USER = "user1";
    private static final String TEST_ASSET = "BTC";

    @BeforeEach
    void setUp() {
        users = new HashMap<>();
        users.put(TEST_USER, new User(TEST_USER, "password", new Wallet(1000.0, new HashMap<>(), new ArrayList<>())));

        mockedStorage = mockStatic(Storage.class);
        mockedStorage.when(Storage::loadUsers).thenReturn(users);

        walletService = WalletService.getInstance();
        walletService.setUsers(users);
    }

    @AfterEach
    void tearDown() {
        mockedStorage.close();
    }

    @Test
    void testDepositMoney_Success() {
        boolean result = walletService.depositMoney(TEST_USER, 500.0);

        assertTrue(result);
        assertEquals(1500.0, users.get(TEST_USER).wallet().balance(), 0.01);
    }

    @Test
    void testDepositMoney_InvalidAmount() {
        boolean result = walletService.depositMoney(TEST_USER, -100.0);
        assertFalse(result);
    }

    @Test
    void testBuyCrypto_InsufficientFunds() {
        boolean result = walletService.buyCrypto(TEST_USER, TEST_ASSET, 1.0, 50000.0);
        assertFalse(result);
    }

    @Test
    void testSellCrypto_NoCryptoToSell() {
        boolean result = walletService.sellCrypto(TEST_USER, TEST_ASSET, 35000.0);
        assertFalse(result);
    }

    @Test
    void testGetWalletSummary() {
        walletService.depositMoney(TEST_USER, 500.0);
        String summary = walletService.getWalletSummary(TEST_USER);

        assertTrue(summary.contains("Balance: $1500,00"));
    }

}
