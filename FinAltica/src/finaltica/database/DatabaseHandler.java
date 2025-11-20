package finaltica.database;

import finaltica.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/finaltica?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private Connection connection;

    public DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // User operations
    public void addUser(User user) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "INSERT INTO users (full_name, username, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.executeUpdate();
        }
    }

    public User getUser(String username, String password) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("full_name"), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Account operations
    public void addAccount(Account account) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "INSERT INTO accounts (user_id, name, balance, total_expenses) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getName());
            stmt.setDouble(3, account.getBalance());
            stmt.setDouble(4, account.getTotalExpenses());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Account> getAccounts(int userId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT * FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new Account(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"),
                            rs.getDouble("balance"), rs.getDouble("total_expenses")));
                }
            }
        }
        return accounts;
    }

    public void deleteAccount(int accountId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "DELETE FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        }
    }

    // Transaction operations
    public void addTransaction(Transaction transaction) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String query = "INSERT INTO transactions (account_id, type, category, amount, statement, date, remark) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getAccountId());
            stmt.setString(2, transaction.getType());
            stmt.setString(3, transaction.getCategory());
            stmt.setDouble(4, transaction.getAmount());
            stmt.setString(5, transaction.getStatement());
            stmt.setDate(6, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(7, transaction.getRemark());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    transaction.setId(rs.getInt(1));
                }
            }
        }

        String updateQuery = transaction.getType().equals("income") ?
                "UPDATE accounts SET balance = balance + ? WHERE id = ?" :
                "UPDATE accounts SET balance = balance - ?, total_expenses = total_expenses + ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            if (transaction.getType().equals("income")) {
                updateStmt.setDouble(1, transaction.getAmount());
                updateStmt.setInt(2, transaction.getAccountId());
            } else {
                updateStmt.setDouble(1, transaction.getAmount());
                updateStmt.setDouble(2, transaction.getAmount());
                updateStmt.setInt(3, transaction.getAccountId());
            }
            updateStmt.executeUpdate();
        }
    }

    public List<Transaction> getTransactions(int userId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT t.* FROM transactions t JOIN accounts a ON t.account_id = a.id WHERE a.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("account_id"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getDouble("amount"),
                            rs.getString("statement"),
                            rs.getDate("date"),
                            rs.getString("remark")
                    ));
                }
            }
        }
        return transactions;
    }

    public void deleteTransaction(int transactionId) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }
        String selectQuery = "SELECT * FROM transactions WHERE id = ?";
        String type = null;
        double amount = 0.0;
        int accountId = 0;
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, transactionId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Transaction not found: ID=" + transactionId);
                }
                type = rs.getString("type");
                amount = rs.getDouble("amount");
                accountId = rs.getInt("account_id");
            }
        }

        String deleteQuery = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, transactionId);
            deleteStmt.executeUpdate();
        }

        String updateQuery = type.equals("income") ?
                "UPDATE accounts SET balance = balance - ? WHERE id = ?" :
                "UPDATE accounts SET balance = balance + ?, total_expenses = total_expenses - ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            if (type.equals("income")) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, accountId);
            } else {
                updateStmt.setDouble(1, amount);
                updateStmt.setDouble(2, amount);
                updateStmt.setInt(3, accountId);
            }
            updateStmt.executeUpdate();
        }
    }
}