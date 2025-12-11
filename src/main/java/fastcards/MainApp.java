package fastcards;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Navigator.setStage(stage);
        stage.setTitle("FastCards");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fastcards/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1600, 1000);
        stage.setScene(scene);
        stage.setMinWidth(1600);
        stage.setMinHeight(900);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
