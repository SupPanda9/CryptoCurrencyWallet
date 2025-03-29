package bg.sofia.uni.fmi.mjt.crypto.wallet.server.connection;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.Command;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands.LoginCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ClientHandlerTest {

    private Socket mockSocket;
    private BufferedReader mockReader;
    private PrintWriter mockWriter;
    private CommandFactory mockCommandFactory;
    private ClientHandler clientHandler;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockReader = mock(BufferedReader.class);
        mockWriter = mock(PrintWriter.class);
        mockCommandFactory = mock(CommandFactory.class);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        clientHandler = new ClientHandler(mockSocket, mockCommandFactory);
    }

    @Test
    void testRun_SuccessfulLogin() throws IOException {
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("login user1 password1\n".getBytes()));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        String input = "login user1 password1";
        Command mockCommand = mock(LoginCommand.class);
        when(mockCommandFactory.createCommand(input, false, "user1")).thenReturn(mockCommand);
        when(mockCommand.execute()).thenReturn("Login successful");
        when(mockReader.readLine()).thenReturn(input);

        when(mockSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        clientHandler.run();

        String output = byteArrayOutputStream.toString();
        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("<END_OF_RESPONSE>"));
    }

    @Test
    void testRun_ExitCommand() throws IOException {
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("exit\n".getBytes()));
        String input = "exit";

        when(mockReader.readLine()).thenReturn(input);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        clientHandler = new ClientHandler(mockSocket, mockCommandFactory);

        clientHandler.run();

        String output = byteArrayOutputStream.toString();
        assertTrue(output.contains("Goodbye!"));
        assertTrue(output.contains("<END_OF_RESPONSE>"));
    }

    @Test
    void testRun_FailedLogin_AlreadyLoggedIn() throws IOException {
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("login user1 password1\n".getBytes()));
        String input = "login user1 password1";
        Command mockCommand = mock(Command.class);

        ClientHandler.addLoggedInUser("user1", "12345");

        when(mockCommandFactory.createCommand(input, false, null)).thenReturn(mockCommand);
        when(mockCommand.execute()).thenReturn("User is already logged in from another session.");
        when(mockReader.readLine()).thenReturn(input);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(byteArrayOutputStream);

        clientHandler = new ClientHandler(mockSocket, mockCommandFactory);

        clientHandler.run();

        String output = byteArrayOutputStream.toString();
        assertTrue(output.contains("User is already logged in from another session."));
        assertTrue(output.contains("<END_OF_RESPONSE>"));
    }

    @Test
    void testRemoveLoggedInUser() {
        String username = "user1";
        String sessionId = "session1";
        ClientHandler.addLoggedInUser(username, sessionId);
        ClientHandler.removeLoggedInUser(username, sessionId);
        assertNull(ClientHandler.getSessionIdForUser(username));
    }
}
