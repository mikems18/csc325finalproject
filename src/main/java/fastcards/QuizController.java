package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class QuizController {
    @FXML private Label positionLabel;
    @FXML private Label questionLabel;
    @FXML private ListView<String> choicesList;

    @FXML
    public void initialize(){
        render();
        choicesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void render(){
        var s = AppState.session;
        positionLabel.setText(s.posLabel());
        questionLabel.setText(s.current().question());
        choicesList.setItems(FXCollections.observableArrayList(s.buildChoices()));
    }

    @FXML
    void onNext(){
        var sel = choicesList.getSelectionModel().getSelectedItem();
        if (sel == null){ new Alert(Alert.AlertType.WARNING, "Pick an answer").showAndWait(); return; }
        AppState.session.markAnswer(sel);
        if (AppState.session.isLast()){
            Navigator.navigate("ResultsView.fxml");
        } else {
            AppState.session.next();
            render();
        }
    }

    @FXML
    void onBack(){
        Navigator.navigate("HomeView.fxml");
    }
}
