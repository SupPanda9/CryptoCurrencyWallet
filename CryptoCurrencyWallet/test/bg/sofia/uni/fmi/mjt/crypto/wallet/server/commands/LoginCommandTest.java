package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginCommandTest {

    @Mock
    private UserService userService;

    @Test
    void testExecute_SuccessfulLogin() {
        String[] args = {"user1", "password123"};

        when(userService.login("user1", "password123")).thenReturn(true);
        LoginCommand loginCommand = new LoginCommand(args, userService);

        String result = loginCommand.execute();

        assertEquals("Login successful!", result);
    }

    @Test
    void testConstructor_MissingArguments() {
        String[] args = {"user1"};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new LoginCommand(args);
        });
        assertEquals("Usage: login <username> <password>", thrown.getMessage());
    }

    @Test
    void testExecute_FailedLogin() {
        String[] args = {"user1", "wrongpassword"};

        when(userService.login("user1", "wrongpassword")).thenReturn(false);

        LoginCommand loginCommand = new LoginCommand(args, userService);

        String result = loginCommand.execute();

        assertEquals("Invalid username or password.", result);
    }

}
