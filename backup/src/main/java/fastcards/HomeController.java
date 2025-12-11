package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Deck> decksList;

    private final FirestoreService fs = new FirestoreService();

    @FXML
    public void initialize() {
        var s = Session.get();
        if (s == null) {
            Navigator.navigate("LoginView.fxml");
            return;
        }
        welcomeLabel.setText("Welcome, " + s.email);
        refreshDecks();
    }

    private void refreshDecks() {
        var s = Session.get();
        if (s == null) return;
        new Thread(() -> {
            try {
                var list = fs.listDecks(s.uid, s.idToken);
                javafx.application.Platform.runLater(() -> {
                    decksList.setItems(FXCollections.observableArrayList(list));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to load decks: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onAddDeck() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Deck");
        dialog.setHeaderText("Create a new deck");
        dialog.setContentText("Title:");
        var res = dialog.showAndWait();
        if (res.isEmpty()) return;
        String title = res.get().trim();
        if (title.isEmpty()) return;

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Deck Description");
        descDialog.setHeaderText("Optional description for this deck");
        descDialog.setContentText("Description:");
        var descRes = descDialog.showAndWait();
        String description = descRes.isEmpty() ? "" : descRes.get().trim();

        var s = Session.get(); if (s == null) return;
        new Thread(() -> {
            try {
                fs.createDeck(s.uid, title, description, s.idToken);
                javafx.application.Platform.runLater(this::refreshDecks);
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to create deck: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onDeleteDeck() {
        Deck deck = decksList.getSelectionModel().getSelectedItem();
        if (deck == null) return;
        var s = Session.get(); if (s == null) return;
        new Thread(() -> {
            try {
                fs.deleteDeck(s.uid, deck.getId(), s.idToken);
                javafx.application.Platform.runLater(this::refreshDecks);
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete deck: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onEditDeck() {
        Deck deck = decksList.getSelectionModel().getSelectedItem();
        if (deck == null) return;
        AppState.currentDeck = deck;
        Navigator.navigate("CardsView.fxml");
    }

    @FXML
    private void onStudyFlashcards() {
        Deck deck = decksList.getSelectionModel().getSelectedItem();
        if (deck == null) return;
        AppState.currentDeck = deck;
        Navigator.navigate("StudyFlashView.fxml");
    }

    @FXML
    private void onStudyMultipleChoice() {
        Deck deck = decksList.getSelectionModel().getSelectedItem();
        if (deck == null) return;
        AppState.currentDeck = deck;
        Navigator.navigate("StudyMCView.fxml");
    }

    @FXML
    private void onLogout() {
        Session.set(null);
        Navigator.navigate("LoginView.fxml");
    }
}
