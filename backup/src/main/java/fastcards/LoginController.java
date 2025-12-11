package fastcards;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private final AuthService auth = new AuthService();

    @FXML
    public void initialize() {
        // nothing special
    }

    @FXML
    private void onLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pw = passwordField.getText() == null ? "" : passwordField.getText().trim();
        if (email.isEmpty() || pw.isEmpty()) {
            showError("Enter email and password.");
            return;
        }
        setBusy(true);
        new Thread(() -> {
            try {
                Session s = auth.signInWithEmail(email, pw);
                Session.set(s);
                javafx.application.Platform.runLater(() -> {
                    setBusy(false);
                    Navigator.navigate("HomeView.fxml");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    setBusy(false);
                    showError("Login failed: " + clip(ex.getMessage()));
                });
            }
        }).start();
    }

    @FXML
    private void onRegister() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pw = passwordField.getText() == null ? "" : passwordField.getText().trim();
        if (email.isEmpty() || pw.isEmpty()) {
            showError("Enter email and password.");
            return;
        }
        setBusy(true);
        new Thread(() -> {
            try {
                Session s = auth.registerWithEmail(email, pw);
                Session.set(s);
                javafx.application.Platform.runLater(() -> {
                    setBusy(false);
                    Navigator.navigate("HomeView.fxml");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    setBusy(false);
                    showError("Register failed: " + clip(ex.getMessage()));
                });
            }
        }).start();
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        registerButton.setDisable(busy);
        emailField.setDisable(busy);
        passwordField.setDisable(busy);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.showAndWait();
    }

    private String clip(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + " ..." : s;
    }
}
