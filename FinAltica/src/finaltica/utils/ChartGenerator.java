package finaltica.utils;

import finaltica.model.Transaction;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.JFrame; // Added import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartGenerator {
    public static void generateChart(List<Transaction> transactions) {
        Map<String, Double> incomeData = new HashMap<>();
        Map<String, Double> expenseData = new HashMap<>();

        for (Transaction t : transactions) {
            String date = t.getDate().toString();
            if (t.getType().equals("income")) {
                incomeData.put(date, incomeData.getOrDefault(date, 0.0) + t.getAmount());
            } else {
                expenseData.put(date, expenseData.getOrDefault(date, 0.0) + t.getAmount());
            }
        }

        // Convert data to lists for XChart
        List<String> dates = new ArrayList<>(incomeData.keySet());
        dates.addAll(expenseData.keySet());
        dates = dates.stream().distinct().sorted().collect(Collectors.toList()); // Ensure unique, sorted dates

        List<Double> incomeValues = new ArrayList<>();
        List<Double> expenseValues = new ArrayList<>();
        for (String date : dates) {
            incomeValues.add(incomeData.getOrDefault(date, 0.0));
            expenseValues.add(expenseData.getOrDefault(date, 0.0));
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Income vs Expenses")
                .xAxisTitle("Date")
                .yAxisTitle("Amount")
                .build();

        // Customize chart styling
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setXAxisLabelRotation(45); // Rotate x-axis labels for readability
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setChartBackgroundColor(java.awt.Color.WHITE);
        chart.getStyler().setAxisTitlesVisible(true);

        // Add series with lists
        chart.addSeries("Income", dates, incomeValues).setFillColor(new java.awt.Color(33, 150, 243)); // Blue
        chart.addSeries("Expenses", dates, expenseValues).setFillColor(new java.awt.Color(244, 67, 54)); // Red

        // Create and configure the SwingWrapper
        SwingWrapper<CategoryChart> swingWrapper = new SwingWrapper<>(chart);
        JFrame chartFrame = swingWrapper.displayChart();
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Set to dispose instead of exit
    }
}