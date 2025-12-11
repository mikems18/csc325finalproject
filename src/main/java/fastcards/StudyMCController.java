package fastcards;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.List;

public class StudyMCController {

    @FXML
    private Label questionLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private Button optionAButton;

    @FXML
    private Button optionBButton;

    @FXML
    private Button optionCButton;

    @FXML
    private Button optionDButton;

    // Top pill progress UI
    @FXML
    private StackPane progressPill;

    @FXML
    private Region progressFill;

    @FXML
    private Label progressText;

    private static final double PROGRESS_MAX_WIDTH = 260.0;

    private final FirestoreService fs = new FirestoreService();
    private MultipleChoiceStudySession session;

    private List<String> currentChoices;
    private String correctAnswer;
    private int answeredCount = 0;

    @FXML
    public void initialize() {
        if (AppState.currentDeck == null || Session.get() == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }

        // init pill to empty
        if (progressFill != null) {
            progressFill.setPrefWidth(0);
            progressFill.setMinWidth(0);
            progressFill.setMaxWidth(PROGRESS_MAX_WIDTH);
        }
        if (progressText != null) {
            progressText.setText("0 / 0");
        }

        loadCards();
    }

    private void loadCards() {
        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }

        questionLabel.setText("Loading cards...");
        disableOptions(true);

        new Thread(() -> {
            try {
                List<Card> cards = fs.listCards(s.uid, d.getId(), s.idToken);
                d.setCards(cards);

                Platform.runLater(() -> {
                    if (cards.isEmpty()) {
                        questionLabel.setText("This deck has no cards.");
                        feedbackLabel.setText("");
                        if (progressLabel != null) {
                            progressLabel.setText("0 / 0");
                        }
                    } else {
                        session = new MultipleChoiceStudySession(d);
                        answeredCount = 0;
                        loadNextQuestion();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to load cards: " + e.getMessage()));
            }
        }).start();
    }

    private void disableOptions(boolean disable) {
        optionAButton.setDisable(disable);
        optionBButton.setDisable(disable);
        optionCButton.setDisable(disable);
        optionDButton.setDisable(disable);
    }

    private void loadNextQuestion() {
        if (session == null) return;

        if (!session.hasNext()) {
            showSummary();
            return;
        }

        Card card = session.current();
        questionLabel.setText(card.getFront());
        correctAnswer = card.getBack();
        currentChoices = session.buildChoices();

        Button[] buttons = { optionAButton, optionBButton, optionCButton, optionDButton };
        for (int i = 0; i < buttons.length; i++) {
            if (i < currentChoices.size()) {
                buttons[i].setVisible(true);
                buttons[i].setManaged(true);
                buttons[i].setText(currentChoices.get(i));
                buttons[i].getStyleClass().remove("mc-option-correct");
                buttons[i].getStyleClass().remove("mc-option-wrong");
            } else {
                buttons[i].setVisible(false);
                buttons[i].setManaged(false);
            }
        }

        feedbackLabel.setText("");
        disableOptions(false);
        updateProgressUI();
    }

    private void handleOption(int index) {
        if (session == null || currentChoices == null) return;
        if (index < 0 || index >= currentChoices.size()) return;

        String chosen = currentChoices.get(index);
        boolean isCorrect = chosen.equals(correctAnswer);

        session.markAnswer(isCorrect);
        answeredCount++;

        feedbackLabel.setText(
                isCorrect ? "Correct!" : "Incorrect. Answer: " + correctAnswer
        );

        updateProgressUI();
        loadNextQuestion();
    }

    private void updateProgressUI() {
        if (session == null) return;

        int total = session.getTotal();
        int completed = Math.min(answeredCount, total);

        if (progressLabel != null) {
            progressLabel.setText(completed + " / " + total);
        }

        if (progressText != null && progressFill != null) {
            progressText.setText(completed + " / " + total);

            double fraction = (total == 0) ? 0.0 : (double) completed / total;
            fraction = Math.max(0, Math.min(1, fraction));

            double width = PROGRESS_MAX_WIDTH * fraction;
            progressFill.setPrefWidth(width);
            progressFill.setMinWidth(width);
            progressFill.setMaxWidth(PROGRESS_MAX_WIDTH);
        }
    }

    private void showSummary() {
        if (session == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }

        int correctCount = session.getCorrectCount();
        int total = session.getTotal();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Multiple choice complete");
        a.setContentText("You got " + correctCount + " out of " + total + " correct.");
        a.showAndWait();

        Navigator.navigate("HomeView.fxml");
    }

    @FXML
    private void onOptionA() {
        handleOption(0);
    }

    @FXML
    private void onOptionB() {
        handleOption(1);
    }

    @FXML
    private void onOptionC() {
        handleOption(2);
    }

    @FXML
    private void onOptionD() {
        handleOption(3);
    }

    @FXML
    private void onBack() {
        Navigator.navigate("HomeView.fxml");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
