package fastcards;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {
    private static Stage stage;

    public static void setStage(Stage s){ stage = s; }

    public static void navigate(String fxml){
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource(fxml));
            Scene scene = new Scene(root, MainApp.PHONE_WIDTH, MainApp.PHONE_HEIGHT);
            scene.getStylesheets().add(Navigator.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
