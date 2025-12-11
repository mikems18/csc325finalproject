package fastcards;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final AuthService auth = new AuthService();

    @FXML
    private void onLogin() {
        String email = emailField.getText();
        String pw = passwordField.getText();
        new Thread(() -> {
            try {
                Session s = auth.signInWithEmail(email, pw);
                Session.set(s);
                Platform.runLater(() -> Navigator.navigate("HomeView.fxml"));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setHeaderText("Login failed");
                    a.setContentText(ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }
    @FXML
    private void onGoogleSignIn() {
        try {
            String googleIdToken = GoogleOAuth.signInAndGetIdToken(); // your helper method
            AuthService auth = new AuthService();
            Session s = auth.signInWithGoogle(googleIdToken);
            Session.set(s);
            Navigator.navigate("HomeView.fxml");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Google sign-in failed: " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    private void onRegister() {
        String email = emailField.getText();
        String pw = passwordField.getText();
        new Thread(() -> {
            try {
                Session s = auth.registerWithEmail(email, pw);
                Session.set(s);
                Platform.runLater(() -> Navigator.navigate("HomeView.fxml"));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setHeaderText("Register failed");
                    a.setContentText(ex.getMessage());
                    a.showAndWait();


                });
            }
        }).start();
    }
}
