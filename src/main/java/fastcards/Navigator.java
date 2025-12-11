package fastcards;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void navigate(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/fastcards/" + fxml));
            Parent root = loader.load();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1200, 800);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to " + fxml + ": " + e.getMessage(), e);
        }
    }
}
