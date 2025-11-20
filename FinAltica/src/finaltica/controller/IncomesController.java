package finaltica.controller;

import finaltica.database.DatabaseHandler;
import finaltica.model.Account;
import finaltica.model.Transaction;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public class IncomesController {
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> accountCombo;
    @FXML private ComboBox<String> sourceCombo;
    @FXML private TextField newSourceField;
    @FXML private TextField amountField;
    @FXML private Label notificationLabel;

    private DatabaseHandler dbHandler = new DatabaseHandler();
    private HashMap<String, Integer> accountMap = new HashMap<>();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    // Initialize dropdowns and load account list
    @FXML
    private void initialize() {
        System.out.println("IncomesController initialized");
        sourceCombo.getItems().addAll("Salary", "Freelance", "Investment");
        refreshAccounts();
    }
    // Load user accounts into ComboBox using HashMap for mapping
    public void refreshAccounts() {
        try {
            System.out.println("Refreshing accounts in IncomesController...");
            if (LoginController.loggedInUser == null) {
                System.out.println("Logged-in user is null in IncomesController!");
                return;
            }
            System.out.println("Logged-in user ID: " + LoginController.loggedInUser.getId());
            accountCombo.getItems().clear();
            accountMap.clear();
            // Retrieve account list using List
            List<Account> accounts = dbHandler.getAccounts(LoginController.loggedInUser.getId());
            System.out.println("Accounts retrieved in IncomesController: " + accounts);
            for (Account account : accounts) {
                accountCombo.getItems().add(account.getName());
                accountMap.put(account.getName(), account.getId());
                System.out.println("Added account to dropdown: " + account.getName());
            }
            if (accounts.isEmpty()) {
                System.out.println("No accounts found for user ID: " + LoginController.loggedInUser.getId());
            }
        } catch (Exception e) {
            System.out.println("Exception in refreshAccounts: ");
            e.printStackTrace();
            showNotification("Error refreshing accounts: " + e.getMessage());
        }
    }
    // Add a new income source to ComboBox
    @FXML
    private void addSource() {
        String newSource = newSourceField.getText();
        if (newSource == null || newSource.trim().isEmpty()) {
            showNotification("Please enter a source name!");
            return;
        }
        sourceCombo.getItems().add(newSource);
        newSourceField.clear();
        showNotification("Source added successfully!");
    }
    // Create and store a new income transaction
    @FXML
    private void addIncome() {
        LocalDate date = datePicker.getValue();
        String accountName = accountCombo.getValue();
        String source = sourceCombo.getValue();
        String amountText = amountField.getText();

        if (date == null) {
            showNotification("Please select a valid date!");
            return;
        }
        if (accountName == null || source == null || amountText == null || amountText.trim().isEmpty()) {
            showNotification("Please fill all required fields!");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showNotification("Amount must be a positive number!");
                return;
            }
            int accountId = accountMap.get(accountName);
            Transaction transaction = new Transaction(0, accountId, "income", source, amount,
                    "Income recorded: " + source, java.sql.Date.valueOf(date), null);
            dbHandler.addTransaction(transaction);
            reset();
            showNotification("Income added successfully!");
            if (mainController != null) {
                mainController.notifyTransactionAdded();
            } else {
                System.out.println("mainController is null in IncomesController!");
            }
        } catch (NumberFormatException e) {
            showNotification("Please enter a valid number for the amount!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error adding income: " + e.getMessage());
        }
    }

    @FXML
    private void reset() {
        datePicker.setValue(null);
        accountCombo.setValue(null);
        sourceCombo.setValue(null);
        amountField.clear();
    }

    private void showNotification(String message) {
        if (notificationLabel != null) {
            notificationLabel.setText(message);
            notificationLabel.setVisible(true);
            notificationLabel.getStyleClass().removeAll("success", "error");
            if (message.toLowerCase().contains("error")) {
                notificationLabel.getStyleClass().add("error");
            } else {
                notificationLabel.getStyleClass().add("success");
            }
            FadeTransition fade = new FadeTransition(Duration.seconds(3), notificationLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(event -> notificationLabel.setVisible(false));
            fade.play();
        } else {
            System.out.println("Notification: " + message);
        }
    }
}