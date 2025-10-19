package fastcards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;
import java.sql.*;

public class HomeController {
    @FXML private Label titleLabel;
    @FXML private ListView<Deck> deckList;

    @FXML
    public void initialize(){
        refreshDecks();
    }

    private void refreshDecks(){
        try {
            var decks = DeckDAO.allDecksForUser(AppState.userId);
            deckList.setItems(FXCollections.observableArrayList(decks));
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, "Failed to load decks: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onStudyFlashcards(){
        Deck d = deckList.getSelectionModel().getSelectedItem();
        if (d == null){ new Alert(Alert.AlertType.WARNING, "Please select a deck").showAndWait(); return; }
        AppState.currentDeck = d;
        AppState.session = new QuizSession(d, QuizSession.Mode.FLASHCARDS, 4);
        Navigator.navigate("FlashcardView.fxml");
    }

    @FXML
    void onStudyMultipleChoice(){
        Deck d = deckList.getSelectionModel().getSelectedItem();
        if (d == null){ new Alert(Alert.AlertType.WARNING, "Please select a deck").showAndWait(); return; }
        AppState.currentDeck = d;
        AppState.session = new QuizSession(d, QuizSession.Mode.MULTIPLE_CHOICE, 4);
        Navigator.navigate("QuizView.fxml");
    }

    @FXML
    void onEditDecks(){
        Navigator.navigate("DecksView.fxml");
    }

    @FXML
    void onLogout(){
        AppState.userId = null;
        Navigator.navigate("LoginView.fxml");
    }
}
