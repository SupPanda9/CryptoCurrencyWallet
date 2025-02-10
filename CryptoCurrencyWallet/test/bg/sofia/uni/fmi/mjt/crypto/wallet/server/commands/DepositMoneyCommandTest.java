package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepositMoneyCommandTest {

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
    }

    @Test
    void testExecute_SuccessfulDeposit() throws IllegalArgumentException {
        String[] args = {"user1", "100.50"};
        DepositMoneyCommand command = new DepositMoneyCommand(walletService, args);

        when(walletService.depositMoney("user1", 100.50)).thenReturn(true);

        String result = command.execute();

        assertEquals("Successfully deposited $100.5 into your wallet.", result);
        verify(walletService).depositMoney("user1", 100.50);
    }

    @Test
    void testExecute_InvalidAmountFormat() {
        String[] args = {"user1", "invalidAmount"};

        assertThrows(IllegalArgumentException.class, () -> new DepositMoneyCommand(walletService, args));
    }

    @Test
    void testExecute_ZeroOrNegativeAmount() {
        String[] argsNegative = {"user1", "-10"};
        String[] argsZero = {"user1", "0"};

        assertThrows(IllegalArgumentException.class, () -> new DepositMoneyCommand(walletService, argsNegative));
        assertThrows(IllegalArgumentException.class, () -> new DepositMoneyCommand(walletService, argsZero));
    }

    @Test
    void testExecute_FailedDeposit() throws IllegalArgumentException {
        String[] args = {"user1", "100.50"};
        DepositMoneyCommand command = new DepositMoneyCommand(walletService, args);

        when(walletService.depositMoney("user1", 100.50)).thenReturn(false);

        String result = command.execute();

        assertEquals("Deposit failed. Check your input and try again.", result);
        verify(walletService).depositMoney("user1", 100.50);
    }

    @Test
    void testConstructor_MissingArguments() {
        String[] args = {"user1"};

        assertThrows(IllegalArgumentException.class, () -> new DepositMoneyCommand(walletService, args));
    }
}
