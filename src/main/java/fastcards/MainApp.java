package fastcards;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final double PHONE_WIDTH = 390;   // iPhone 12 logical width
    public static final double PHONE_HEIGHT = 844;  // iPhone 12 logical height

    @Override
    public void start(Stage stage) throws Exception {
        Navigator.setStage(stage);
        stage.setTitle("FastCards");
        Navigator.navigate("LoginView.fxml");
        stage.setMinWidth(PHONE_WIDTH);
        stage.setMinHeight(PHONE_HEIGHT);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
