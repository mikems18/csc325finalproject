package fastcards;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class StudyFlashController {

    @FXML
    private StackPane cardContainer;

    @FXML
    private VBox frontSide;

    @FXML
    private VBox backSide;

    @FXML
    private Label frontLabel;

    @FXML
    private Label backLabel;

    @FXML
    private Button showAnswerButton;

    @FXML
    private Button gotItButton;

    @FXML
    private Button missedButton;

    @FXML
    private Label progressLabel;

    @FXML
    private Label statsLabel;

    // Top pill progress UI
    @FXML
    private StackPane progressPill;

    @FXML
    private Region progressFill;

    @FXML
    private Label progressText;

    private static final double PROGRESS_MAX_WIDTH = 260.0;

    private final FirestoreService fs = new FirestoreService();
    private final List<Card> cards = new ArrayList<>();

    // queue of card indices still in rotation
    private final List<Integer> queue = new ArrayList<>();
    private int queuePos = 0;

    // flip / animation state
    private boolean showingFront = true;
    private boolean animating = false;

    // stats
    private int attempts = 0;
    private int correct = 0;

    @FXML
    public void initialize() {
        // flip around X axis (up/down)
        if (cardContainer != null) {
            cardContainer.setRotationAxis(Rotate.X_AXIS);
        }

        // initialise pill to empty (0 width)
        if (progressFill != null) {
            progressFill.setPrefWidth(0);
            progressFill.setMinWidth(0);
            progressFill.setMaxWidth(PROGRESS_MAX_WIDTH);
        }
        if (progressText != null) {
            progressText.setText("0 / 0");
        }

        Deck deck = AppState.currentDeck;
        if (deck == null) {
            showError("No deck selected.");
            Navigator.navigate("HomeView.fxml");
            return;
        }

        loadCards(deck);
    }

    private void loadCards(Deck deck) {
        Session s = Session.get();
        if (s == null) {
            Navigator.navigate("LoginView.fxml");
            return;
        }

        disableAllButtons(true);

        new Thread(() -> {
            try {
                List<Card> loaded = fs.listCards(s.uid, deck.getId(), s.idToken);
                Platform.runLater(() -> {
                    cards.clear();
                    cards.addAll(loaded);

                    queue.clear();
                    for (int i = 0; i < cards.size(); i++) {
                        queue.add(i);
                    }
                    queuePos = 0;

                    attempts = 0;
                    correct = 0;
                    showingFront = true;

                    if (cards.isEmpty()) {
                        frontLabel.setText("This deck has no cards yet.");
                        backLabel.setText("");
                        disableAllButtons(true);
                    } else {
                        disableAllButtons(false);
                        updateCardText();
                    }

                    updateProgress();
                    updateStats();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to load cards: " + e.getMessage()));
            }
        }).start();
    }

    private void disableAllButtons(boolean disable) {
        if (showAnswerButton != null) showAnswerButton.setDisable(disable);
        if (gotItButton != null) gotItButton.setDisable(disable);
        if (missedButton != null) missedButton.setDisable(disable);
    }

    private boolean hasCurrentCard() {
        return !cards.isEmpty()
                && !queue.isEmpty()
                && queuePos >= 0
                && queuePos < queue.size();
    }

    private int getCurrentCardIndex() {
        return queue.get(queuePos);
    }

    private void updateCardText() {
        if (!hasCurrentCard()) {
            // no cards left in the queue
            frontLabel.setText("All cards mastered!");
            backLabel.setText("");
            frontSide.setVisible(true);
            frontSide.setOpacity(1);
            backSide.setVisible(false);
            backSide.setOpacity(0);
            if (showAnswerButton != null) {
                showAnswerButton.setText("Show Answer");
                showAnswerButton.setDisable(true);
            }
            return;
        }

        int idx = getCurrentCardIndex();
        Card c = cards.get(idx);

        frontLabel.setText(c.getFront());
        backLabel.setText(c.getBack());

        if (showingFront) {
            frontSide.setVisible(true);
            frontSide.setOpacity(1);
            backSide.setVisible(false);
            backSide.setOpacity(0);
            if (showAnswerButton != null) {
                showAnswerButton.setText("Show Answer");
            }
        } else {
            frontSide.setVisible(false);
            frontSide.setOpacity(0);
            backSide.setVisible(true);
            backSide.setOpacity(1);
            if (showAnswerButton != null) {
                showAnswerButton.setText("Show Question");
            }
        }
    }

    private void updateProgress() {
        if (progressLabel != null) {
            if (cards.isEmpty()) {
                progressLabel.setText("0 / 0");
            } else {
                int total = cards.size();
                int remaining = queue.size();
                progressLabel.setText("Cards left: " + remaining + " / " + total);
            }
        }
        updateProgressBar();
    }

    private void updateStats() {
        if (statsLabel != null) {
            statsLabel.setText("Correct: " + correct + " / Attempts: " + attempts);
        }
    }

    /**
     * Updates the pill progress bar at the top.
     * Progress = (mastered cards) / (total cards)
     * where mastered = total - remaining in queue.
     * Fills left-to-right by changing width 0..PROGRESS_MAX_WIDTH.
     */
    private void updateProgressBar() {
        if (progressFill == null || progressText == null) return;

        int total = cards.size();
        int remaining = queue.size();
        int mastered = Math.max(0, total - remaining);

        if (total <= 0) {
            progressText.setText("0 / 0");
            progressFill.setPrefWidth(0);
            progressFill.setMinWidth(0);
        } else {
            double fraction = (double) mastered / total;
            fraction = Math.max(0, Math.min(1, fraction));

            double width = PROGRESS_MAX_WIDTH * fraction;
            progressText.setText(mastered + " / " + total);

            progressFill.setPrefWidth(width);
            progressFill.setMinWidth(width);
            progressFill.setMaxWidth(PROGRESS_MAX_WIDTH);
        }
    }

    private void markDeckComplete() {
        disableAllButtons(true);
        updateCardText();
        updateProgress();
        updateStats();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Deck Complete");
        a.setContentText("You’ve mastered all cards in this deck!\n\nCorrect: "
                + correct + " / Attempts: " + attempts);
        a.showAndWait();
        Navigator.navigate("HomeView.fxml");
    }

    /* =========================
       FLIP ANIMATION (up/down)
       ========================= */

    private void flipCard() {
        if (animating || cards.isEmpty() || queue.isEmpty()) return;
        animating = true;

        double durationMs = 160;

        cardContainer.setRotationAxis(Rotate.X_AXIS);

        RotateTransition firstHalf =
                new RotateTransition(Duration.millis(durationMs), cardContainer);
        firstHalf.setFromAngle(0);
        firstHalf.setToAngle(90);

        RotateTransition secondHalf =
                new RotateTransition(Duration.millis(durationMs), cardContainer);
        secondHalf.setFromAngle(-90);
        secondHalf.setToAngle(0);

        firstHalf.setOnFinished(e -> {
            // swap side at half flip
            showingFront = !showingFront;
            if (showingFront) {
                frontSide.setVisible(true);
                frontSide.setOpacity(1);
                backSide.setVisible(false);
                backSide.setOpacity(0);
                showAnswerButton.setText("Show Answer");
            } else {
                frontSide.setVisible(false);
                frontSide.setOpacity(0);
                backSide.setVisible(true);
                backSide.setOpacity(1);
                showAnswerButton.setText("Show Question");
            }

            cardContainer.setRotate(-90);
            secondHalf.play();
        });

        secondHalf.setOnFinished(e -> {
            cardContainer.setRotate(0);
            animating = false;
        });

        firstHalf.play();
    }

    /* =========================
       EVENT HANDLERS
       ========================= */

    @FXML
    private void onCardClicked() {
        flipCard();
    }

    @FXML
    private void onShowAnswer() {
        flipCard();
    }

    // scoring logic:
    //  - Got It  => remove card from queue
    //  - Missed  => move card to end of queue

    @FXML
    private void onGotIt() {
        if (!hasCurrentCard()) return;

        attempts++;
        correct++;

        int idx = getCurrentCardIndex();
        queue.remove(queuePos);

        if (queue.isEmpty()) {
            updateStats();
            updateProgress();
            markDeckComplete();
            return;
        }

        if (queuePos >= queue.size()) {
            queuePos = 0;
        }

        showingFront = true;
        updateCardText();
        updateProgress();
        updateStats();
    }

    @FXML
    private void onMissed() {
        if (!hasCurrentCard()) return;

        attempts++;

        int idx = getCurrentCardIndex();
        queue.remove(queuePos);
        queue.add(idx); // put this card at end of rotation

        if (queuePos >= queue.size()) {
            queuePos = 0;
        }

        showingFront = true;
        updateCardText();
        updateProgress();
        updateStats();
    }

    @FXML
    private void onBack() {
        Navigator.navigate("HomeView.fxml");
    }

    /* =========================
       HELPERS
       ========================= */

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
