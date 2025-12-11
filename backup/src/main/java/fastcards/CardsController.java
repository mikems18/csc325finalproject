package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CardsController {

    @FXML private Label deckTitleLabel;
    @FXML private Label deckDescriptionLabel;
    @FXML private ListView<Card> cardsList;
    @FXML private TextField frontField;
    @FXML private TextField backField;

    private final FirestoreService fs = new FirestoreService();

    @FXML
    public void initialize() {
        if (AppState.currentDeck == null || Session.get() == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }
        deckTitleLabel.setText(AppState.currentDeck.getTitle());
        deckDescriptionLabel.setText(AppState.currentDeck.getDescription());
        cardsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Card item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Q: " + item.getFront() + " | A: " + item.getBack());
            }
        });
        refreshCards();
    }

    private void refreshCards() {
        var s = Session.get();
        var deck = AppState.currentDeck;
        if (s == null || deck == null) return;
        new Thread(() -> {
            try {
                var list = fs.listCards(s.uid, deck.getId(), s.idToken);
                deck.setCards(list);
                javafx.application.Platform.runLater(() -> {
                    cardsList.setItems(FXCollections.observableArrayList(list));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to load cards: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onAddCard() {
        String front = frontField.getText();
        String back = backField.getText();
        if (front == null || front.isBlank() || back == null || back.isBlank()) return;
        var s = Session.get(); var deck = AppState.currentDeck;
        new Thread(() -> {
            try {
                fs.addCard(s.uid, deck.getId(), front, back, s.idToken);
                javafx.application.Platform.runLater(() -> {
                    frontField.clear();
                    backField.clear();
                    refreshCards();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to add card: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onDeleteCard() {
        Card card = cardsList.getSelectionModel().getSelectedItem();
        if (card == null) return;
        var s = Session.get(); var deck = AppState.currentDeck;
        new Thread(() -> {
            try {
                fs.deleteCard(s.uid, deck.getId(), card.getId(), s.idToken);
                javafx.application.Platform.runLater(this::refreshCards);
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete card: " + ex.getMessage()).showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onBack() {
        Navigator.navigate("HomeView.fxml");
    }
}
