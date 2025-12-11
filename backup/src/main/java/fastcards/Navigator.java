package fastcards;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void navigate(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/fastcards/" + fxml));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to navigate to " + fxml + ": " + e.getMessage(), e);
        }
    }
}
