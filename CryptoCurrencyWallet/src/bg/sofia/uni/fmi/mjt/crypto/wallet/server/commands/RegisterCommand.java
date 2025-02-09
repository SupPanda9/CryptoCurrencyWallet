package bg.sofia.uni.fmi.mjt.crypto.wallet.server.commands;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.services.UserService;

public class RegisterCommand implements Command {
    private final UserService userService;
    private final String username;
    private final String password;

    public RegisterCommand(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: register <username> <password>");
        }

        this.userService = UserService.getInstance();
        this.username = args[0];
        this.password = args[1];
    }

    @Override
    public String execute() {
        System.out.println("Executing register command for user: " + username); // 🔍 Debug log

        boolean success = userService.register(username, password);
        if (success) {
            System.out.println("User registered successfully: " + username); // 🔍 Debug log
            return "Registration successful!";
        } else {
            System.out.println("Registration failed: user " + username + " already exists."); // 🔍 Debug log
            return "Username already taken. Try another one.";
        }
    }
}
