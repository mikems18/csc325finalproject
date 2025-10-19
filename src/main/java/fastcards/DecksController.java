package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;

public class DecksController {
    @FXML private ListView<Deck> myDecksList;
    @FXML private TextField deckNameField;

    @FXML
    public void initialize(){
        loadMyDecks();
    }

    private void loadMyDecks(){
        try {
            var all = DeckDAO.allDecksForUser(AppState.userId);
            var mine = all.stream().filter(d -> d.getOwnerId()!=null && d.getOwnerId().equals(AppState.userId)).toList();
            myDecksList.setItems(FXCollections.observableArrayList(mine));
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to load decks: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onAddDeck(){
        String name = deckNameField.getText();
        if (name.isBlank()){ return; }
        try {
            Deck d = DeckDAO.createDeck(AppState.userId, name);
            if (d != null){
                d.setCards(new ArrayList<>());
                myDecksList.getItems().add(d);
                deckNameField.clear();
            }
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to add deck: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onEditCards(){
        Deck d = myDecksList.getSelectionModel().getSelectedItem();
        if (d == null){ new Alert(Alert.AlertType.WARNING, "Select a deck").showAndWait(); return; }
        AppState.currentDeck = d;
        Navigator.navigate("CardsView.fxml");
    }

    @FXML
    void onBack(){
        Navigator.navigate("HomeView.fxml");
    }
}
