package fastcards;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ResultsController {
    @FXML private Label scoreLabel;

    @FXML
    public void initialize(){
        int correct = AppState.session.correct();
        int total = AppState.session.total();
        int pct = (int) Math.round(100.0 * correct / Math.max(total,1));
        scoreLabel.setText("Score: " + correct + " / " + total + "  (" + pct + "%)");
    }

    @FXML
    void onHome(){
        Navigator.navigate("HomeView.fxml");
    }
}
