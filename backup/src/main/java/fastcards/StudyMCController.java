package fastcards;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class StudyMCController {

    @FXML private Label deckTitleLabel;
    @FXML private Label questionLabel;
    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;
    @FXML private ToggleGroup answersGroup;

    private MultipleChoiceStudySession session;
    private List<String> currentChoices;
    private String correctAnswer;

    @FXML
    public void initialize() {
        if (AppState.currentDeck == null || Session.get() == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }
        deckTitleLabel.setText(AppState.currentDeck.getTitle());
        session = new MultipleChoiceStudySession(AppState.currentDeck);

        answersGroup = new ToggleGroup();
        optionA.setToggleGroup(answersGroup);
        optionB.setToggleGroup(answersGroup);
        optionC.setToggleGroup(answersGroup);
        optionD.setToggleGroup(answersGroup);

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        if (!session.hasNext()) {
            showSummary();
            return;
        }
        var card = session.current();
        questionLabel.setText(card.getFront());
        correctAnswer = card.getBack();
        currentChoices = session.buildChoices();

        RadioButton[] opts = { optionA, optionB, optionC, optionD };
        for (int i = 0; i < opts.length; i++) {
            if (i < currentChoices.size()) {
                opts[i].setVisible(true);
                opts[i].setText(currentChoices.get(i));
            } else {
                opts[i].setVisible(false);
            }
        }
        answersGroup.selectToggle(null);
    }

    @FXML
    private void onNext() {
        Toggle selected = answersGroup.getSelectedToggle();
        if (selected == null) {
            new Alert(Alert.AlertType.INFORMATION, "Select an answer first.").showAndWait();
            return;
        }
        RadioButton rb = (RadioButton) selected;
        boolean correct = rb.getText().equals(correctAnswer);
        session.markAnswer(correct);
        loadNextQuestion();
    }

    private void showSummary() {
        int correct = session.getCorrectCount();
        int total = session.getTotal();
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Multiple choice complete");
        a.setContentText("You got " + correct + " out of " + total + " correct.");
        a.showAndWait();
        Navigator.navigate("HomeView.fxml");
    }

    @FXML
    private void onBack() {
        Navigator.navigate("HomeView.fxml");
    }
}
