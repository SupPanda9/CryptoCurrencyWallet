package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetWalletSummaryCommandTest {

    private WalletService walletService;
    private GetWalletSummaryCommand getWalletSummaryCommand;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
    }

    @Test
    void testExecute_SuccessfulFetch() {
        String username = "user123";
        String expectedSummary = "Wallet Summary for user123: Balance: $500.00";

        when(walletService.getWalletSummary(username)).thenReturn(expectedSummary);

        getWalletSummaryCommand = new GetWalletSummaryCommand(walletService, new String[] { username });

        String result = getWalletSummaryCommand.execute();

        assertEquals(expectedSummary, result);

        verify(walletService).getWalletSummary(username);
    }

    @Test
    void testExecute_MissingArgument() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new GetWalletSummaryCommand(walletService, new String[] {});
        });

        assertEquals("Usage: get-wallet-summary <username>", thrown.getMessage());
    }

    @Test
    void testExecute_WalletServiceError() {
        String username = "user123";
        when(walletService.getWalletSummary(username)).thenThrow(new RuntimeException("Service error"));

        getWalletSummaryCommand = new GetWalletSummaryCommand(walletService, new String[] { username });

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            getWalletSummaryCommand.execute();
        });

        assertEquals("Service error", thrown.getMessage());

        verify(walletService).getWalletSummary(username);
    }
}
