# 🚀 Cryptocurrency Wallet Manager 💰 💸

## 📖 Project Overview
The **Cryptocurrency Wallet Manager** is a client-server console application that simulates a personal cryptocurrency wallet. It allows users to **register, deposit money, buy and sell cryptocurrencies**, and **track their investments** using real-time data from **CoinAPI**.

## ✨ Features
- 🔐 **User Authentication**: Register and login with a secure password.
- 💵 **Deposit Money**: Add funds to your wallet (in USD).
- 📊 **View Cryptocurrency Offerings**: Fetch live crypto prices via **CoinAPI**.
- 🛒 **Buy & Sell Cryptocurrencies**: Manage your investments efficiently.
- 📄 **Wallet Summary**: View all active investments and wallet balance.
- 📈 **Profit & Loss Calculation**: Get an overview of total earnings and losses.
- ⚡ **Multi-user Support**: The server handles multiple clients concurrently.
- 💾 **Persistent Storage**: Saves user data securely in a JSON file.
- 🔄 **Cached API Calls**: Optimized API requests with 30-minute caching.

## 🏗️ Project Structure

```plaintext
com.crypto.wallet
├── client
│   ├── ClientMain.java            // Entry point for the client app (handles user input/output)
│   └── CommandParser.java         // Parses user input into commands for the server
│
├── server
│   ├── ServerMain.java            // Entry point for the server; sets up the server socket and uses ExecutorService with virtual threads
│   │
│   ├── connection                 // Handles client connections
│   │   └── ClientHandler.java     // Processes a single client connection using blocking I/O on a virtual thread
│   │
│   ├── commands                   // Implements the Command pattern to encapsulate each client request
│   │   ├── Command.java               // Interface with an execute() method for all commands
│   │   ├── CommandFactory.java        // Maps input strings to concrete Command objects (avoiding many if/else statements)
│   │   ├── RegisterCommand.java       // Processes user registration requests
│   │   ├── LoginCommand.java          // Processes login requests
│   │   ├── DepositMoneyCommand.java   // Processes deposit money requests
│   │   ├── BuyCommand.java            // Processes cryptocurrency purchase requests
│   │   ├── SellCommand.java           // Processes cryptocurrency sale requests
│   │   ├── GetWalletSummaryCommand.java       // Returns the current state of a wallet
│   │   └── GetWalletOverallSummaryCommand.java  // Returns the overall profit/loss summary
│   │
│   ├── models                     // Domain models representing the core data of the system
│   │   ├── User.java                // Contains username, hashed password, etc.
│   │   ├── Wallet.java              // Contains balance and current investments
│   │   ├── Transaction.java         // Represents a single transaction (deposit, buy, sell)
│   │   └── CryptoOffering.java      // **Record**: Immutable representation of a cryptocurrency asset (from CoinAPI)
│   │
│   ├── services                   // Business logic layer applying SOLID principles
│   │   ├── UserService.java         // Handles registration, login, and other user-related operations
│   │   ├── WalletService.java       // Processes deposits, buys, sells, and generates wallet summaries
│   │   ├── CoinAPIService.java      // Makes HTTP calls to CoinAPI for crypto data
│   │   └── CachedCoinAPIService.java// Wraps CoinAPIService with a 30-minute caching mechanism
│   │
│   ├── persistence                // Manages data persistence (reading/writing JSON files)
│   │   └── Storage.java             // Uses Gson or Jackson to handle JSON persistence of user and wallet data
│   │
│   └── utils                      // Shared utilities for configuration and logging
│       ├── Config.java              // Singleton that loads configuration (.env variables, API key, etc.)
│       └── Logger.java              // Utility for logging errors (writes technical details and stack traces to a log file)
```

## 🛠️ Setup & Installation

### 📥 Prerequisites
- **Java 17+**
- **CoinAPI key** (register [here](https://www.coinapi.io/))
- **Maven** (for dependency management)

### 🔧 Running the Project

1️⃣ Clone the repository:
```sh
git clone https://github.com/your-username/crypto-wallet.git
cd crypto-wallet
```

2️⃣ Add your CoinAPI Key in Config.java:

```java
public static final String COIN_API_KEY = "your-api-key-here";
```

3️⃣ Compile & run the server:

```sh
cd server
javac -d bin $(find . -name "*.java")
java -cp bin com.crypto.wallet.server.ServerMain
```

4️⃣ In another terminal, start the client:

```sh
cd client
javac -d bin $(find . -name "*.java")
java -cp bin com.crypto.wallet.client.ClientMain
```

## 📜 Supported Commands
📝 Authentication

```sh
register <username> <password>
login <username> <password>
```

💰 Wallet Management

```sh
deposit-money <amount>
list-offerings
buy --offering=<crypto_code> --money=<amount>
sell --offering=<crypto_code>
```

📈 Summary

```sh
get-wallet-summary
get-wallet-overall-summary
```

🆘 Help

```sh
help
```

## 🏆 Extra Features & Considerations
- ✔ Secure Password Storage - Uses hashing for user credentials.
- ✔ Thread Management - Uses Java's virtual threads for handling multiple clients.
- ✔ Logging System - Stores technical errors in a log file.
- ✔ Data Persistence - Reloads user data on restart.


## 📜 License
This project is licensed under the MIT License.