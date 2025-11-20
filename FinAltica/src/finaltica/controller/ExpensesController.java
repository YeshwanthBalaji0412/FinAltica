package finaltica.controller;

import finaltica.database.DatabaseHandler;
import finaltica.datastructures.BST;
import finaltica.datastructures.StackADT;
import finaltica.datastructures.UndoStack;
import finaltica.model.Account;
import finaltica.model.Transaction;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpensesController {
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> accountCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField newCategoryField;
    @FXML private TextField amountField;
    @FXML private TextField remarkField;
    @FXML private Label notificationLabel;
    @FXML private TableView<Transaction> expensesTable;
    @FXML private TableColumn<Transaction, String> accountColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, java.util.Date> dateColumn;
    @FXML private TableColumn<Transaction, String> remarkColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private DatabaseHandler dbHandler = new DatabaseHandler();
    private HashMap<String, Integer> accountMap = new HashMap<>();
    // Custom stack to support undoing the last transaction
    private StackADT<Transaction> undoStack = new UndoStack();
    // List to hold all expense transactions
    private List<Transaction> expenses = new ArrayList<>();
    private MainController mainController;
    // Custom Binary Search Tree to store and retrieve categories efficiently
    private BST categoryBST = new BST();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        accountColumn.setCellValueFactory(cellData -> {
            int accountId = cellData.getValue().getAccountId();
            String accountName = accountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == accountId)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(String.valueOf(accountId));
            return new javafx.beans.property.SimpleStringProperty(accountName);
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        remarkColumn.setCellValueFactory(new PropertyValueFactory<>("remark"));

        // Enable sorting for columns
        accountColumn.setSortable(true);
        categoryColumn.setSortable(true);
        amountColumn.setSortable(true);
        dateColumn.setSortable(true);
        remarkColumn.setSortable(true);

        // Call refreshAccounts and loadExpenses before setting sort policy
        try {
            refreshAccounts();
            categoryBST.insert("Food");
            categoryBST.insert("Transport");
            categoryBST.insert("Entertainment");
            refreshCategories();
            loadExpenses();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error initializing Expenses tab: " + e.getMessage());
        }

        // Custom sort policy for the TableView
        expensesTable.setSortPolicy(new Callback<TableView<Transaction>, Boolean>() {
            @Override
            public Boolean call(TableView<Transaction> tableView) {
                if (expenses == null) {
                    return true; // Skip sorting if expenses is null
                }
                Comparator<Transaction> comparator = (t1, t2) -> {
                    for (TableColumn<Transaction, ?> column : tableView.getSortOrder()) {
                        if (column == dateColumn) {
                            int result = t1.getDate().compareTo(t2.getDate());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == amountColumn) {
                            int result = Double.compare(t1.getAmount(), t2.getAmount());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == categoryColumn) {
                            int result = t1.getCategory().compareTo(t2.getCategory());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == accountColumn) {
                            int result = t1.getAccountId() - t2.getAccountId();
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == remarkColumn) {
                            String r1 = t1.getRemark() != null ? t1.getRemark() : "";
                            String r2 = t2.getRemark() != null ? t2.getRemark() : "";
                            int result = r1.compareTo(r2);
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        }
                    }
                    return 0;
                };
                expenses.sort(comparator);
                expensesTable.getItems().setAll(expenses);
                return true;
            }
        });
    }

    public void loadExpenses() throws Exception {
        expenses = new ArrayList<>(); // ArrayList to hold expense entries
        for (Transaction t : dbHandler.getTransactions(LoginController.loggedInUser.getId())) {
            if (t.getType().equals("expense")) {
                expenses.add(t);
            }
        }
        expensesTable.getItems().setAll(expenses);
    }

    public void refreshAccounts() {
        try {
            System.out.println("Refreshing accounts in ExpensesController...");
            if (LoginController.loggedInUser == null) {
                System.out.println("Logged-in user is null in ExpensesController!");
                return;
            }
            accountCombo.getItems().clear();
            accountMap.clear();
            List<Account> accounts = dbHandler.getAccounts(LoginController.loggedInUser.getId());
            for (Account account : accounts) {
                accountCombo.getItems().add(account.getName());
                accountMap.put(account.getName(), account.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error refreshing accounts: " + e.getMessage());
        }
    }

    public void refreshCategories() {
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(categoryBST.getCategories()); // BST traversal
    }

    @FXML
    private void addCategory() {
        String newCategory = newCategoryField.getText();
        if (newCategory == null || newCategory.trim().isEmpty()) {
            showNotification("Please enter a category name!");
            return;
        }
        try {
            List<String> categories = categoryBST.getCategories(); // BST to List
            if (categories.stream().anyMatch(cat -> cat.equalsIgnoreCase(newCategory))) {
                showNotification("Category already exists!");
                return;
            }
            categoryBST.insert(newCategory);
            categoryCombo.getItems().add(newCategory);
            newCategoryField.clear();
            showNotification("Category added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error adding category: " + e.getMessage());
        }
    }

    @FXML
    private void addExpense() {
        LocalDate date = datePicker.getValue();
        String accountName = accountCombo.getValue();
        String category = categoryCombo.getValue();
        String amountText = amountField.getText();
        String remark = remarkField.getText();

        if (date == null) {
            showNotification("Please select a valid date!");
            return;
        }
        if (accountName == null || category == null || amountText == null || amountText.trim().isEmpty()) {
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
            Transaction transaction = new Transaction(0, accountId, "expense", category, amount,
                    "Expense recorded: " + category, java.sql.Date.valueOf(date), remark);
            dbHandler.addTransaction(transaction);
            undoStack.push(transaction);
            loadExpenses();
            reset();
            showNotification("Expense added successfully!");
            if (mainController != null) {
                mainController.notifyTransactionAdded();
            } else {
                System.out.println("mainController is null in ExpensesController!");
            }
        } catch (NumberFormatException e) {
            showNotification("Please enter a valid number for the amount!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error adding expense: " + e.getMessage());
        }
    }

    @FXML
    private void applyFilter() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            List<Transaction> filteredExpenses = new ArrayList<>(expenses);

            if (startDate != null) {
                filteredExpenses = filteredExpenses.stream()
                        .filter(t -> !t.getDate().before(java.sql.Date.valueOf(startDate)))
                        .collect(Collectors.toList());
            }
            if (endDate != null) {
                filteredExpenses = filteredExpenses.stream()
                        .filter(t -> !t.getDate().after(java.sql.Date.valueOf(endDate)))
                        .collect(Collectors.toList());
            }

            expensesTable.getItems().setAll(filteredExpenses);
            showNotification("Filter applied successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error applying filter: " + e.getMessage());
        }
    }

    @FXML
    private void undo() {
        Transaction lastTransaction = undoStack.pop();
        if (lastTransaction == null) {
            showNotification("No actions to undo!");
            return;
        }
        try {
            dbHandler.deleteTransaction(lastTransaction.getId());
            loadExpenses();
            showNotification("Expense undone successfully!");
            if (mainController != null) {
                mainController.notifyTransactionAdded();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error undoing expense: " + e.getMessage());
        }
    }

    @FXML
    private void reset() {
        datePicker.setValue(null);
        accountCombo.setValue(null);
        categoryCombo.setValue(null);
        amountField.clear();
        remarkField.clear();
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