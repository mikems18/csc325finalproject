package fastcards;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudyFlashController {

    @FXML private Label deckTitleLabel;
    @FXML private Label questionLabel;
    @FXML private Label answerLabel;
    @FXML private Button showAnswerButton;
    @FXML private Button gotItButton;
    @FXML private Button didntGetItButton;

    private FlashStudySession session;
    private Card currentCard;

    @FXML
    public void initialize() {
        if (AppState.currentDeck == null || Session.get() == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }
        deckTitleLabel.setText(AppState.currentDeck.getTitle());
        session = new FlashStudySession(AppState.currentDeck);
        loadNextCard();
    }

    private void loadNextCard() {
        if (!session.hasNext()) {
            showSummary();
            return;
        }
        currentCard = session.nextCard();
        questionLabel.setText(currentCard.getFront());
        answerLabel.setText("Tap 'Show Answer' to reveal.");
        showAnswerButton.setDisable(false);
        gotItButton.setDisable(true);
        didntGetItButton.setDisable(true);
    }

    @FXML
    private void onShowAnswer() {
        if (currentCard == null) return;
        answerLabel.setText(currentCard.getBack());
        showAnswerButton.setDisable(true);
        gotItButton.setDisable(false);
        didntGetItButton.setDisable(false);
    }

    @FXML
    private void onGotIt() {
        session.answer(true);
        loadNextCard();
    }

    @FXML
    private void onDidntGetIt() {
        session.answer(false);
        loadNextCard();
    }

    private void showSummary() {
        int correct = session.getTotalCorrect();
        int attempts = session.getTotalAttempts();
        int unique = session.getUniqueCardCount();
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Session complete");
        a.setContentText("You studied " + unique + " cards.\n"
                + "Total attempts: " + attempts + "\n"
                + "Correct answers: " + correct);
        a.showAndWait();
        Navigator.navigate("HomeView.fxml");
    }

    @FXML
    private void onBack() {
        Navigator.navigate("HomeView.fxml");
    }
}
