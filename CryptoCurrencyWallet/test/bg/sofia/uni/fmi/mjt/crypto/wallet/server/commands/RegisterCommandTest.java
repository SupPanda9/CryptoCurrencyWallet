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
public class RegisterCommandTest {

    @Mock
    private UserService userService;

    @Test
    void testExecute_SuccessfulRegistration() {
        String[] args = {"user1", "password123"};

        when(userService.register("user1", "password123")).thenReturn(true);

        RegisterCommand registerCommand = new RegisterCommand(args, userService);

        String result = registerCommand.execute();

        assertEquals("Registration successful!", result);
    }

    @Test
    void testConstructor_MissingArguments() {
        String[] args = {"user1"};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new RegisterCommand(args, userService);
        });
        assertEquals("Usage: register <username> <password>", thrown.getMessage());
    }

    @Test
    void testExecute_FailedRegistration() {
        String[] args = {"user1", "takenUsername"};

        when(userService.register("user1", "takenUsername")).thenReturn(false);

        RegisterCommand registerCommand = new RegisterCommand(args, userService);

        String result = registerCommand.execute();

        assertEquals("Username already taken. Try another one.", result);
    }
}
