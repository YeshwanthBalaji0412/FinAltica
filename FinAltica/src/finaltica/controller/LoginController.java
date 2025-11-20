package finaltica.controller;

import finaltica.database.DatabaseHandler;
import finaltica.model.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label notificationLabel;
    private DatabaseHandler dbHandler = new DatabaseHandler();
    public static User loggedInUser;

    @FXML
    private void handleLogin() throws Exception {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showNotification("Please enter username and password!");
            return;
        }
        try {
        	// fetch user from database
            User user = dbHandler.getUser(username, password);
            if (user != null) {
                loggedInUser = user;
                System.out.println("User logged in: ID=" + loggedInUser.getId() + ", Username=" + loggedInUser.getUsername());
                Parent root = FXMLLoader.load(getClass().getResource("../view/MainView.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("FinAltica");
            } else {
                showNotification("Invalid credentials!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Database error: " + e.getMessage());
        }
    }
    // Handles the "Sign Up" link/button action to redirect to registration page
    @FXML
    private void handleSignUp() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../view/SignUpView.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("FinAltica - Sign Up");
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