package fastcards;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    @FXML
    void onCreate(ActionEvent e){
        if (usernameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()){
            new Alert(Alert.AlertType.WARNING, "Username, email and password are required").showAndWait(); return;
        }
        if (!passwordField.getText().equals(confirmField.getText())){
            new Alert(Alert.AlertType.WARNING, "Passwords do not match").showAndWait(); return;
        }
        try {
            Integer uid = UserDAO.register(usernameField.getText(), emailField.getText(), passwordField.getText());
            if (uid != null){
                AppState.userId = uid;
                new Alert(Alert.AlertType.INFORMATION, "Account created!").showAndWait();
                Navigator.navigate("HomeView.fxml");
            }
        } catch (Exception ex){
            String msg = ex.getMessage();
            if (msg != null && msg.contains("UNIQUE constraint failed: users.email")){
                new Alert(Alert.AlertType.ERROR, "That email is already registered.").showAndWait();
            } else if (msg != null && msg.contains("UNIQUE constraint failed: users.username")){
                new Alert(Alert.AlertType.ERROR, "That username is already taken.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Registration failed: " + msg).showAndWait();
            }
        }
    }

    @FXML
    void onBack(ActionEvent e){
        Navigator.navigate("LoginView.fxml");
    }
}
