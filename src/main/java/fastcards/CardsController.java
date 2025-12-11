package fastcards;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;


public class CardsController {

    @FXML
    private Label deckTitleLabel;

    @FXML
    private ListView<Card> cardListView;

    @FXML
    private TextField frontField;

    @FXML
    private TextField backField;

    private final FirestoreService fs = new FirestoreService();

    @FXML
    public void initialize() {
        if (Session.get() == null || AppState.currentDeck == null) {
            Navigator.navigate("HomeView.fxml");
            return;
        }

        Deck d = AppState.currentDeck;
        deckTitleLabel.setText(d.getTitle());

        // Show card front instead of fastcards.Card@xxxx
        cardListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Card card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setText(null);
                } else {
                    setText(card.getFront());
                }
            }
        });

        // When you click a card, fill the text fields
        cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldCard, newCard) -> {
            if (newCard != null) {
                frontField.setText(newCard.getFront());
                backField.setText(newCard.getBack());
            } else {
                frontField.clear();
                backField.clear();
            }
        });

        loadCards();
    }

    private void loadCards() {
        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) return;

        cardListView.getItems().clear();

        new Thread(() -> {
            try {
                List<Card> cards = fs.listCards(s.uid, d.getId(), s.idToken);
                d.setCards(cards);
                Platform.runLater(() ->
                        cardListView.setItems(FXCollections.observableArrayList(cards)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to load cards: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onAddCard() {
        String front = frontField.getText().trim();
        String back = backField.getText().trim();

        if (front.isEmpty() || back.isEmpty()) {
            showError("Both front and back are required.");
            return;
        }

        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) return;

        new Thread(() -> {
            try {
                fs.addCard(s.uid, d.getId(), front, back, s.idToken);
                Platform.runLater(() -> {
                    frontField.clear();
                    backField.clear();
                    loadCards();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to add card: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onUpdateCard() {
        Card selected = cardListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a card to update.");
            return;
        }

        String front = frontField.getText().trim();
        String back = backField.getText().trim();

        if (front.isEmpty() || back.isEmpty()) {
            showError("Both front and back are required.");
            return;
        }

        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) return;

        final Card cardToUpdate = selected;

        new Thread(() -> {
            try {
                fs.updateCard(s.uid, d.getId(), cardToUpdate.getId(), front, back, s.idToken);
                Platform.runLater(() -> {
                    cardToUpdate.setFront(front);
                    cardToUpdate.setBack(back);
                    cardListView.refresh();
                    showInfo("Card updated.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to update card: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onDeleteCard() {
        Card selected = cardListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a card to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Card");
        confirm.setHeaderText("Delete this card?");
        confirm.setContentText(selected.getFront());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) return;

        final Card cardToDelete = selected;

        new Thread(() -> {
            try {
                fs.deleteCard(s.uid, d.getId(), cardToDelete.getId(), s.idToken);
                Platform.runLater(() -> {
                    cardListView.getItems().remove(cardToDelete);
                    frontField.clear();
                    backField.clear();
                    showInfo("Card deleted.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to delete card: " + e.getMessage()));
            }
        }).start();
    }

    // =============================
    // CSV IMPORT
    // =============================

    @FXML
    private void onImportCsv() {
        // Choose a CSV file
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Cards from CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = chooser.showOpenDialog(cardListView.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        Session s = Session.get();
        Deck d = AppState.currentDeck;
        if (s == null || d == null) return;

        new Thread(() -> {
            int imported = 0;
            int skipped = 0;

            try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        // blank line, count as skipped
                        skipped++;
                        continue;
                    }

                    // Split into at most 2 parts (front, back)
                    String[] parts = line.split(",", 2);
                    if (parts.length < 2) {
                        skipped++;
                        continue;
                    }

                    String front = parts[0].trim();
                    String back = parts[1].trim();

                    // 🔹 Silent header skip: do NOT import, do NOT increment skipped
                    if (front.equalsIgnoreCase("front") && back.equalsIgnoreCase("back")) {
                        continue;
                    }

                    // If both sides are still empty, skip & count
                    if (front.isEmpty() && back.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // Add to Firestore
                    fs.addCard(s.uid, d.getId(), front, back, s.idToken);
                    imported++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                int finalImported = imported;
                int finalSkipped = skipped;
                Platform.runLater(() ->
                        showError("CSV import failed after importing "
                                + finalImported + " cards. Skipped "
                                + finalSkipped + " rows.\n\n" + e.getMessage())
                );
                return;
            }

            int finalImported = imported;
            int finalSkipped = skipped;
            Platform.runLater(() -> {
                loadCards();
                showInfo("CSV import complete.\n"
                        + "Imported: " + finalImported + " cards\n"
                        + "Skipped: " + finalSkipped + " rows");
            });
        }).start();
    }

    /**
     * Very small CSV parser:
     * - splits on commas
     * - supports quotes: "Front, with comma","Back"
     */
    private String[] parseCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // toggle quoted mode
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());

        return parts.toArray(new String[0]);
    }

    // =============================
    // NAV
    // =============================

    @FXML
    private void onBackToHome() {
        Navigator.navigate("HomeView.fxml");
    }

    // =============================
    // HELPERS
    // =============================

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
