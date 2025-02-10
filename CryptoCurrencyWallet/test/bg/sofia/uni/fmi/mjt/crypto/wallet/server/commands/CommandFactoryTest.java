package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.CachedCoinAPIService;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class CommandFactoryTest {

    private CommandFactory commandFactory;
    private CachedCoinAPIService cachedCoinAPIService;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        cachedCoinAPIService = mock(CachedCoinAPIService.class);
        walletService = mock(WalletService.class);
        commandFactory = new CommandFactory(cachedCoinAPIService, walletService);
    }

    @Test
    void testCreateCommand_Register() {
        String input = "register user1 password123";
        Command command = commandFactory.createCommand(input, false, null);

        assertInstanceOf(RegisterCommand.class, command);
    }

    @Test
    void testCreateCommand_Login() {
        String input = "login user1 password123";
        Command command = commandFactory.createCommand(input, false, null);

        assertInstanceOf(LoginCommand.class, command);
    }

    @Test
    void testCreateCommand_UnknownCommand() {
        String input = "unknown-command";
        Command command = commandFactory.createCommand(input, true, null);

        assertEquals("Unknown command: unknown-command", command.execute());
    }

    @Test
    void testCreateCommand_DepositMoney_LoggedIn() {
        String input = "deposit-money 1000";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertInstanceOf(DepositMoneyCommand.class, command);
    }

    @Test
    void testCreateCommand_DepositMoney_NotLoggedIn() {
        String input = "deposit-money 1000";
        Command command = commandFactory.createCommand(input, false, null);

        assertEquals("You must be logged in to use this command!", command.execute());
    }

    @Test
    void testCreateCommand_Buy_LoggedIn() {
        String input = "buy --offering=BTC --money=0.5";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertTrue(command instanceof BuyCommand, "Expected BuyCommand but got " + command.getClass().getName());
    }

    @Test
    void testCreateCommand_GetWalletSummary_LoggedIn() {
        String input = "get-wallet-summary user1";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertInstanceOf(GetWalletSummaryCommand.class, command);
    }

    @Test
    void testCreateCommand_GetWalletOverallSummary_LoggedIn() {
        String input = "get-wallet-overall-summary user1";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertInstanceOf(GetWalletOverallSummaryCommand.class, command);
    }

    @Test
    void testCreateCommand_Logout_LoggedIn() {
        String input = "logout";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertInstanceOf(LogoutCommand.class, command);
    }

    @Test
    void testCreateCommand_RestrictedCommandWhileLoggedIn() {
        String input = "register user2 password123";
        Command command = commandFactory.createCommand(input, true, "user1");

        assertEquals("You are already logged in. Log out to register a new account.", command.execute());
    }

    @Test
    void testCreateCommand_LoginRequired() {
        String input = "deposit-money 1000";
        Command command = commandFactory.createCommand(input, false, null);

        assertEquals("You must be logged in to use this command!", command.execute());
    }
}
