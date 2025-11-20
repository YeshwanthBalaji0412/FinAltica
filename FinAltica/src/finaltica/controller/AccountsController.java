package finaltica.controller;

import finaltica.database.DatabaseHandler;
import finaltica.model.Account;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsController {
    @FXML private TextField createAccountField;
    @FXML private ComboBox<String> deleteAccountCombo;
    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> accountNameColumn;
    @FXML private TableColumn<Account, Double> balanceColumn;
    @FXML private TableColumn<Account, Double> totalExpensesColumn;
    @FXML private Label notificationLabel;

    private DatabaseHandler dbHandler = new DatabaseHandler();
    // Implementation of HashMap used to map account name to their IDs
    private Map<String, Integer> accountMap = new HashMap<>();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    // Initializes table columns and loads account data
    @FXML
    private void initialize() {
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        totalExpensesColumn.setCellValueFactory(new PropertyValueFactory<>("totalExpenses"));

        refreshAccounts();
    }
    // Loads user accounts and populates the table and combo box
    public void refreshAccounts() {
        try {
            System.out.println("Refreshing accounts in AccountsController...");
            if (LoginController.loggedInUser == null) {
                System.out.println("Logged-in user is null in AccountsController!");
                return;
            }
            // List implementation to hold multiple account objects
            List<Account> accounts = dbHandler.getAccounts(LoginController.loggedInUser.getId());
            System.out.println("Accounts retrieved in AccountsController: " + accounts);
            accountsTable.getItems().clear();
            accountsTable.getItems().setAll(accounts);
            deleteAccountCombo.getItems().clear();
            accountMap.clear();
            for (Account account : accounts) {
                deleteAccountCombo.getItems().add(account.getName());
                accountMap.put(account.getName(), account.getId());
            }
            accountsTable.refresh();
        } catch (Exception e) {
            System.out.println("Exception in refreshAccounts: ");
            e.printStackTrace();
            showNotification("Error refreshing accounts: " + e.getMessage());
        }
    }
    // creation of a new account
    @FXML
    private void createAccount() {
        String name = createAccountField.getText();
        if (name == null || name.trim().isEmpty()) {
            showNotification("Please enter an account name!");
            return;
        }
        try {
        	// List implementation to check existing accounts for duplicates
            List<Account> accounts = dbHandler.getAccounts(LoginController.loggedInUser.getId()); // Implementation of List
            for (Account account : accounts) {
                if (account.getName().equalsIgnoreCase(name)) {
                    showNotification("Account name already exists!");
                    return;
                }
            }
            Account account = new Account(0, LoginController.loggedInUser.getId(), name, 0.0, 0.0);
            dbHandler.addAccount(account);
            refreshAccounts();
            createAccountField.clear();
            showNotification("Account created successfully!");
            if (mainController != null) {
                mainController.notifyAccountChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error creating account: " + e.getMessage());
        }
    }
    // Deletion of a created account
    @FXML
    private void deleteAccount() {
        String selectedAccount = deleteAccountCombo.getValue();
        if (selectedAccount == null) {
            showNotification("Please select an account to delete!");
            return;
        }
        try {
            int accountId = accountMap.get(selectedAccount);
            dbHandler.deleteAccount(accountId);
            refreshAccounts();
            showNotification("Account deleted successfully!");
            if (mainController != null) {
                mainController.notifyAccountChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error deleting account: " + e.getMessage());
        }
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