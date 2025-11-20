package finaltica.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Tab homeTab;
    @FXML private Tab accountsTab;
    @FXML private Tab incomesTab;
    @FXML private Tab expensesTab;
    @FXML private Button logoutButton;

    @FXML private HomeController homeViewController;
    @FXML private AccountsController accountsViewController;
    @FXML private IncomesController incomesViewController;
    @FXML private ExpensesController expensesViewController;

    @FXML
    private void initialize() {
        System.out.println("MainController initialized. Controllers: " +
                "homeViewController=" + homeViewController +
                ", accountsViewController=" + accountsViewController +
                ", incomesViewController=" + incomesViewController +
                ", expensesViewController=" + expensesViewController);

        if (homeViewController != null) {
            // No need to inject into HomeController
        }
        if (accountsViewController != null) {
            accountsViewController.setMainController(this);
        }
        if (incomesViewController != null) {
            incomesViewController.setMainController(this);
        }
        if (expensesViewController != null) {
            expensesViewController.setMainController(this);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            System.out.println("Tab selected: " + (newTab != null ? newTab.getText() : "null"));
            if (newTab != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(500), newTab.getContent());
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();

                if (newTab == homeTab && homeViewController != null) {
                    System.out.println("Home tab selected. Refreshing data...");
                    homeViewController.refreshData();
                }

                if (newTab == accountsTab && accountsViewController != null) {
                    System.out.println("Accounts tab selected. Refreshing accounts...");
                    accountsViewController.refreshAccounts();
                }

                if (newTab == incomesTab && incomesViewController != null) {
                    System.out.println("Incomes tab selected. Refreshing accounts...");
                    incomesViewController.refreshAccounts();
                }

                if (newTab == expensesTab && expensesViewController != null) {
                    System.out.println("Expenses tab selected. Refreshing accounts...");
                    expensesViewController.refreshAccounts();
                }
            }
        });
    }

    @FXML
    private void handleLogout() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../view/LoginView.fxml"));
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getRoot().setOpacity(0);
        stage.setScene(scene);
        FadeTransition fade = new FadeTransition(Duration.millis(500), scene.getRoot());
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
        stage.setTitle("FinAltica - Login");
        LoginController.loggedInUser = null;
    }
    // Called by sub-controllers when a new transaction is added
    public void notifyTransactionAdded() {
        System.out.println("Notifying controllers of new transaction...");
        if (homeViewController != null) {
            homeViewController.refreshData();
        }
        if (accountsViewController != null) {
            accountsViewController.refreshAccounts();
        }
        if (expensesViewController != null) {
            try {
                expensesViewController.loadExpenses();
            } catch (Exception e) {
                System.out.println("Error refreshing expenses: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void notifyAccountChanged() {
        System.out.println("Notifying controllers of account change...");
        if (incomesViewController != null) {
            incomesViewController.refreshAccounts();
        }
        if (expensesViewController != null) {
            expensesViewController.refreshAccounts();
        }
    }
}