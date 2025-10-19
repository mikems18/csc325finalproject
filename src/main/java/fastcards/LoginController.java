package fastcards;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    void onLogin(ActionEvent e){
        try {
            Integer uid = UserDAO.loginWithEmail(emailField.getText(), passwordField.getText());
            if (uid != null){
                AppState.userId = uid;
                Navigator.navigate("HomeView.fxml");
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid email or password").showAndWait();
            }
        } catch (Exception ex){
            new Alert(Alert.AlertType.ERROR, "Login failed: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onRegister(ActionEvent e){
        Navigator.navigate("RegisterView.fxml");
    }
}
