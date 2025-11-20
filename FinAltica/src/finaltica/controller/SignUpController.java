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

public class SignUpController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label notificationLabel;
    private DatabaseHandler dbHandler = new DatabaseHandler();

    @FXML
    private void handleSignUp() throws Exception {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (fullName == null || fullName.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            showNotification("Please fill all fields!");
            return;
        }
        try {
            if (dbHandler.usernameExists(username)) {
                showNotification("Username already exists!");
                return;
            }
            User user = new User(0, fullName, username, password);
            dbHandler.addUser(user);
            showNotification("Sign-up successful! Please log in.");
            handleLogin();
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Error signing up: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../view/LoginView.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("FinAltica - Login");
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