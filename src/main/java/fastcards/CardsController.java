package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CardsController {
    @FXML private ListView<Flashcard> cardsList;
    @FXML private TextField qField;
    @FXML private TextField aField;

    @FXML
    public void initialize(){
        refresh();
        cardsList.setCellFactory(lv -> new ListCell<>(){
            @Override protected void updateItem(Flashcard item, boolean empty){
                super.updateItem(item, empty);
                setText(empty || item==null ? null : "Q: " + item.question() + " | A: " + item.answer());
            }
        });
    }

    private void refresh(){
        try {
            var deck = AppState.currentDeck;
            if (deck == null){ return; }
            deck.setCards(DeckDAO.loadCards(deck.getId()));
            cardsList.setItems(FXCollections.observableArrayList(deck.getCards()));
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to load cards: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onAdd(){
        try {
            if (!qField.getText().isBlank() && !aField.getText().isBlank()){
                DeckDAO.addCard(AppState.currentDeck.getId(), qField.getText(), aField.getText());
                qField.clear(); aField.clear();
                refresh();
            }
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to add card: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onDelete(){
        var sel = cardsList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            DeckDAO.deleteCard(sel.id());
            refresh();
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to delete card: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onImportCsv(){
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Import Deck CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showOpenDialog(cardsList.getScene().getWindow());
            if (file == null) return;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null){
                    if (line.trim().isEmpty()) continue;
                    // optional header "question,answer"
                    if (first && line.toLowerCase().startsWith("question")) { first = false; continue; }
                    first = false;
                    String[] parts = line.split(",", 2);
                    String q = parts.length>0 ? parts[0].trim() : "";
                    String a = parts.length>1 ? parts[1].trim() : "";
                    if (!q.isEmpty() && !a.isEmpty()){
                        DeckDAO.addCard(AppState.currentDeck.getId(), q, a);
                    }
                }
            }
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Import complete").showAndWait();
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Import failed: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onExportCsv(){
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Deck CSV");
            fc.setInitialFileName(AppState.currentDeck.getName().replaceAll("[\\/:*?\"<>|]", "_") + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showSaveDialog(cardsList.getScene().getWindow());
            if (file == null) return;
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                bw.write("question,answer\n");
                for (var c : AppState.currentDeck.getCards()){
                    String q = c.question().replaceAll(",", " ");
                    String a = c.answer().replaceAll(",", " ");
                    bw.write(q + "," + a + "\n");
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Export complete").showAndWait();
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onBack(){
        Navigator.navigate("DecksView.fxml");
    }
}
