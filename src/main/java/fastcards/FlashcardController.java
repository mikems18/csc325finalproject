package fastcards;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FlashcardController {
    @FXML private Label titleLabel;
    @FXML private TextArea contentArea;
    private boolean showingQuestion = true;

    @FXML
    public void initialize(){
        titleLabel.setText(AppState.currentDeck.getName());
        render();
    }

    private void render(){
        var c = AppState.session.current();
        if (showingQuestion){
            contentArea.setText("Q: " + c.question());
        } else {
            contentArea.setText("A: " + c.answer());
        }
    }

    @FXML
    void onToggle(){
        showingQuestion = !showingQuestion;
        render();
    }

    @FXML
    void onPrev(){
        AppState.session.prev();
        showingQuestion = true;
        render();
    }

    @FXML
    void onNext(){
        AppState.session.next();
        showingQuestion = true;
        render();
    }

    @FXML
    void onFinish(){
        Navigator.navigate("ResultsView.fxml");
    }

    @FXML
    void onBack(){
        Navigator.navigate("HomeView.fxml");
    }
}
