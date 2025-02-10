package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;

public class UserServiceTest {

    private UserService userService;
    private MockedStatic<Storage> mockStorage;

    @BeforeEach
    void setUp() {
        mockStorage = mockStatic(Storage.class);
        doNothing().when(Storage.class);
        Storage.saveUsers(anyMap());

        userService = UserService.getInstance();
    }


    @Test
    void testRegister_Success() {
        String username = "newUser";
        String password = "securePassword123";

        boolean result = userService.register(username, password);

        assertTrue(result, "Registration should be successful.");
    }

    @Test
    void testRegister_Failure_ExistingUser() {
        String username = "existingUser";
        String password = "password123";

        doNothing().when(Storage.class);
        Storage.saveUsers(anyMap());
        userService.register(username, password);

        boolean result = userService.register(username, "newPassword123");

        assertFalse(result, "Registration should fail if the user already exists.");
    }

    @Test
    void testLogin_Success() {
        String username = "validUser";
        String password = "correctPassword123";

        userService.register(username, password);

        boolean result = userService.login(username, password);

        assertTrue(result, "Login should be successful with correct credentials.");
    }

    @Test
    void testLogin_Failure_UserNotFound() {
        String username = "nonExistentUser";
        String password = "password123";

        boolean result = userService.login(username, password);

        assertFalse(result, "Login should fail if the user does not exist.");
    }

    @Test
    void testLogin_Failure_InvalidPassword() {
        String username = "validUser";
        String correctPassword = "correctPassword123";
        String incorrectPassword = "wrongPassword456";

        userService.register(username, correctPassword);

        boolean result = userService.login(username, incorrectPassword);

        assertFalse(result, "Login should fail with incorrect password.");
    }

    @AfterEach
    void tearDown() {
        mockStorage.close();
    }
}
