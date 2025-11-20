package finaltica.controller;

import finaltica.database.DatabaseHandler;
import finaltica.model.Transaction;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {
    @FXML private Label balanceLabel;
    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label notificationLabel;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> transIdColumn;
    @FXML private TableColumn<Transaction, Integer> accountIdColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> statementColumn;
    @FXML private TableColumn<Transaction, java.util.Date> dateColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    private DatabaseHandler dbHandler = new DatabaseHandler();
    private List<Transaction> transactions; // Store all transactions for sorting and filtering
    private long lastRefreshTime = 0; 
    private static final long REFRESH_COOLDOWN = 1000; 

    @FXML
    private void initialize() {
        transIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        accountIdColumn.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statementColumn.setCellValueFactory(new PropertyValueFactory<>("statement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Enable sorting for columns
        transIdColumn.setSortable(true);
        accountIdColumn.setSortable(true);
        typeColumn.setSortable(true);
        amountColumn.setSortable(true);
        statementColumn.setSortable(true);
        dateColumn.setSortable(true);

        
        typeFilterCombo.setItems(FXCollections.observableArrayList("All", "Income", "Expense"));

        // Call refreshData() to populate transactions before setting sort policy
        if (LoginController.loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + LoginController.loggedInUser.getFullName() + "!");
            refreshData();
        }

        // Custom sort policy for the TableView
        transactionTable.setSortPolicy(new Callback<TableView<Transaction>, Boolean>() {
            @Override
            public Boolean call(TableView<Transaction> tableView) {
                if (transactions == null) {
                    return true; 
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
                        } else if (column == typeColumn) {
                            int result = t1.getType().compareTo(t2.getType());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == transIdColumn) {
                            int result = Integer.compare(t1.getId(), t2.getId());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == accountIdColumn) {
                            int result = Integer.compare(t1.getAccountId(), t2.getAccountId());
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        } else if (column == statementColumn) {
                            String s1 = t1.getStatement() != null ? t1.getStatement() : "";
                            String s2 = t2.getStatement() != null ? t2.getStatement() : "";
                            int result = s1.compareTo(s2);
                            if (column.getSortType() == TableColumn.SortType.DESCENDING) {
                                result = -result;
                            }
                            if (result != 0) return result;
                        }
                    }
                    return 0;
                };
                transactions.sort(comparator);
                transactionTable.getItems().setAll(transactions);
                return true;
            }
        });
    }

    public void refreshData() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_COOLDOWN) {
            System.out.println("Skipping redundant refresh in HomeController...");
            return;
        }
        lastRefreshTime = currentTime;

        try {
            System.out.println("Refreshing data in HomeController...");
            if (LoginController.loggedInUser == null) {
                System.out.println("Logged-in user is null in HomeController!");
                return;
            }
            double totalBalance = 0.0;
            double totalIncome = 0.0;
            double totalExpense = 0.0;

            transactions = dbHandler.getTransactions(LoginController.loggedInUser.getId());
            for (Transaction t : transactions) {
                if (t.getType().equals("income")) {
                    totalIncome += t.getAmount();
                } else {
                    totalExpense += t.getAmount();
                }
            }
            totalBalance = totalIncome - totalExpense;

            balanceLabel.setText(String.format("%.2f", totalBalance));
            incomeLabel.setText(String.format("%.2f", totalIncome));
            expenseLabel.setText(String.format("%.2f", totalExpense));
            transactionTable.getItems().setAll(transactions);
            
            
            updateBarChart();
            updatePieChart();
            
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error refreshing data: " + e.getMessage());
        }
    }
    
    private void updateBarChart() {
        barChart.getData().clear();
        
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        
        
        for (Transaction t : transactions) {
            if (t.getType().equals("income")) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }
        
       
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Financial Overview");
        
        
        series.getData().add(new XYChart.Data<>("Income", totalIncome));
        series.getData().add(new XYChart.Data<>("Expenses", totalExpense));
        
        
        barChart.getData().add(series);
        
        
        javafx.application.Platform.runLater(() -> {
           
            series.getData().get(0).getNode().setStyle("-fx-bar-fill: #4CAF50;"); 
            series.getData().get(1).getNode().setStyle("-fx-bar-fill: #F44336;");
            
            
            barChart.setCategoryGap(50);
            barChart.setBarGap(10);
        });
    }
    
    private void updatePieChart() {
        pieChart.getData().clear();
        
        // Group expenses by category
        Map<String, Double> expensesByCategory = new HashMap<>();
        
        for (Transaction t : transactions) {
            if (t.getType().equals("expense")) {
                String category = t.getCategory();
                expensesByCategory.put(category, 
                    expensesByCategory.getOrDefault(category, 0.0) + t.getAmount());
            }
        }
        
        // Create pie chart data
        for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
        }
        
        // If no expense data, add a placeholder
        if (pieChart.getData().isEmpty()) {
            pieChart.getData().add(new PieChart.Data("No expenses", 1));
        }
        
       
        applyPieChartStyling();
    }

    private void applyPieChartStyling() {
        
        final String[] COLORS = {
            "#3498db", 
            "#2ecc71", 
            "#e74c3c", 
            "#9b59b6", 
            "#f1c40f", 
            "#1abc9c", 
            "#d35400", 
            "#34495e", 
            "#16a085", 
            "#c0392b", 
            "#8e44ad", 
            "#f39c12", 
            "#27ae60", 
            "#2980b9", 
            "#e67e22"  
        };
        
        // Map to store category-to-color assignments for consistent coloring
        Map<String, String> categoryColorMap = new HashMap<>();
        
        // Pre-assign colors to common categories if they exist
        String[] commonCategories = {"Food", "Transport", "Entertainment", "Utilities", "Housing"};
        for (int i = 0; i < commonCategories.length; i++) {
            if (i < COLORS.length) {
                categoryColorMap.put(commonCategories[i], COLORS[i]);
            }
        }
        
        javafx.application.Platform.runLater(() -> {
            int colorIndex = commonCategories.length; 
            
            for (PieChart.Data data : pieChart.getData()) {
                String category = data.getName();
                String color;
                
                
                if (categoryColorMap.containsKey(category)) {
                    color = categoryColorMap.get(category);
                } else {
                    
                    color = COLORS[colorIndex % COLORS.length];
                    categoryColorMap.put(category, color);
                    colorIndex++;
                }
                
               
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
                
            
                for (Node legendItem : pieChart.lookupAll(".chart-legend-item")) {
                    if (legendItem.toString().contains(category)) {
                        Node legendSymbol = legendItem.lookup(".chart-legend-item-symbol");
                        if (legendSymbol != null) {
                            legendSymbol.setStyle("-fx-background-color: " + color + ";");
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void applyFilter() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String typeFilter = typeFilterCombo.getValue();

            List<Transaction> filteredTransactions = new ArrayList<>(transactions);

            // Filter by date range
            if (startDate != null) {
                filteredTransactions = filteredTransactions.stream()
                        .filter(t -> !t.getDate().before(java.sql.Date.valueOf(startDate)))
                        .collect(Collectors.toList());
            }
            if (endDate != null) {
                filteredTransactions = filteredTransactions.stream()
                        .filter(t -> !t.getDate().after(java.sql.Date.valueOf(endDate)))
                        .collect(Collectors.toList());
            }

            // Filter by type
            if (typeFilter != null && !typeFilter.equals("All")) {
                filteredTransactions = filteredTransactions.stream()
                        .filter(t -> t.getType().equalsIgnoreCase(typeFilter))
                        .collect(Collectors.toList());
            }

            
            transactionTable.getItems().setAll(filteredTransactions);
            
            showNotification("Filter applied successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error applying filter: " + e.getMessage());
        }
    }

    @FXML
    private void exportToCSV() {
        try {
            List<Transaction> currentTransactions = transactionTable.getItems();
            if (currentTransactions == null || currentTransactions.isEmpty()) {
                showNotification("No transactions to export!");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Transactions as CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("ID,Account ID,Type,Category,Amount,Statement,Date,Remark");
                    for (Transaction t : currentTransactions) {
                        writer.println(String.format("%d,%d,%s,%s,%.2f,%s,%s,%s",
                                t.getId(),
                                t.getAccountId(),
                                t.getType(),
                                t.getCategory(),
                                t.getAmount(),
                                t.getStatement() != null ? t.getStatement() : "",
                                t.getDate(),
                                t.getRemark() != null ? t.getRemark() : ""));
                    }
                    showNotification("Transactions exported to CSV successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error exporting to CSV: " + e.getMessage());
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