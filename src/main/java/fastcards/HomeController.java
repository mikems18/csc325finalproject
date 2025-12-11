package fastcards;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.util.List;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<Deck> deckListView;

    private final FirestoreService fs = new FirestoreService();

    @FXML
    public void initialize() {
        Session s = Session.get();
        if (s == null) {
            Navigator.navigate("LoginView.fxml");
            return;
        }

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + s.email);
        }

        /* ======================================================
               CUSTOM PILL-STYLE LIST CELLS + RIGHT CLICK MENU
         ====================================================== */
        deckListView.getStyleClass().add("deck-list");

        deckListView.setCellFactory(list -> {

            ListCell<Deck> cell = new ListCell<>() {

                private final Label titleLabel = new Label();
                private final Label descLabel = new Label();
                private final VBox box = new VBox(titleLabel, descLabel);

                {
                    // Use CSS classes instead of inline colors
                    titleLabel.getStyleClass().add("deck-title");
                    descLabel.getStyleClass().add("deck-desc");
                    box.getStyleClass().add("deck-pill");

                    setPadding(new Insets(6));
                }

                @Override
                protected void updateItem(Deck deck, boolean empty) {
                    super.updateItem(deck, empty);

                    if (empty || deck == null) {
                        setGraphic(null);
                    } else {
                        titleLabel.setText(deck.getTitle());
                        descLabel.setText(
                                (deck.getDescription() == null || deck.getDescription().isBlank())
                                        ? "No description"
                                        : deck.getDescription()
                        );
                        setGraphic(box);
                    }
                }
            };

            /* ==============================
               CONTEXT MENU (RIGHT CLICK)
               rename / duplicate / delete
            ============================== */
            ContextMenu menu = new ContextMenu();

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(e -> {
                Deck d = cell.getItem();
                if (d != null) {
                    deckListView.getSelectionModel().select(d);
                    onRenameDeck();
                }
            });

            MenuItem duplicateItem = new MenuItem("Duplicate");
            duplicateItem.setOnAction(e -> {
                Deck d = cell.getItem();
                if (d != null) {
                    deckListView.getSelectionModel().select(d);
                    onDuplicateDeck();
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                Deck d = cell.getItem();
                if (d != null) {
                    deckListView.getSelectionModel().select(d);
                    onDeleteDeck();
                }
            });


            menu.getItems().addAll(renameItem, duplicateItem, deleteItem);

            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                cell.setContextMenu(isEmpty ? null : menu);
            });

            return cell;
        });

        loadDecks();
    }

    private void loadDecks() {
        Session s = Session.get();
        if (s == null) return;

        deckListView.getItems().clear();

        new Thread(() -> {
            try {
                List<Deck> decks = fs.listDecks(s.uid, s.idToken);
                Platform.runLater(() ->
                        deckListView.setItems(FXCollections.observableArrayList(decks)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to load decks: " + e.getMessage()));
            }
        }).start();
    }

    private Deck getSelectedDeckOrShowError(String msg) {
        Deck d = deckListView.getSelectionModel().getSelectedItem();
        if (d == null) showError(msg);
        return d;
    }

    /* =============================
        STUDY ACTIONS
     ============================= */

    @FXML
    private void onStudyFlash() {
        Deck d = getSelectedDeckOrShowError("Select a deck to study.");
        if (d == null) return;
        AppState.currentDeck = d;
        Navigator.navigate("StudyFlashView.fxml");
    }

    @FXML
    private void onStudyMultipleChoice() {
        Deck d = getSelectedDeckOrShowError("Select a deck to study.");
        if (d == null) return;
        AppState.currentDeck = d;
        Navigator.navigate("StudyMCView.fxml");
    }

    @FXML
    private void onEditDeck() {
        Deck d = getSelectedDeckOrShowError("Select a deck to edit.");
        if (d == null) return;
        AppState.currentDeck = d;
        Navigator.navigate("CardsView.fxml");
    }

    /* =============================
        CRUD
     ============================= */

    @FXML
    private void onAddDeck() {
        TextInputDialog t = new TextInputDialog();
        t.setHeaderText("Create new deck");
        t.setContentText("Deck title:");
        String title = t.showAndWait().orElse(null);
        if (title == null || title.isBlank()) return;

        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Deck description (optional)");
        d.setContentText("Description:");
        String desc = d.showAndWait().orElse("");

        Session s = Session.get();
        new Thread(() -> {
            try {
                fs.createDeck(s.uid, title.trim(), desc.trim(), s.idToken);
                Platform.runLater(() -> {
                    loadDecks();
                    showInfo("Deck created.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onDeleteDeck() {
        Deck d = getSelectedDeckOrShowError("Select a deck to delete.");
        if (d == null) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Delete deck");
        a.setContentText("Are you sure?");
        if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Session s = Session.get();
        new Thread(() -> {
            try {
                fs.deleteDeck(s.uid, d.getId(), s.idToken);
                Platform.runLater(this::loadDecks);
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onRenameDeck() {
        Deck d = getSelectedDeckOrShowError("Select a deck to rename.");
        if (d == null) return;

        TextInputDialog t = new TextInputDialog(d.getTitle());
        t.setHeaderText("Rename deck");
        t.setContentText("New name:");
        String name = t.showAndWait().orElse("");
        if (name.isBlank()) return;

        Session s = Session.get();
        new Thread(() -> {
            try {
                fs.renameDeck(s.uid, d.getId(), name, s.idToken);
                d.setTitle(name);
                Platform.runLater(() -> {
                    deckListView.refresh();
                    showInfo("Deck renamed.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onDuplicateDeck() {
        Deck d = getSelectedDeckOrShowError("Select a deck to duplicate.");
        if (d == null) return;

        Session s = Session.get();
        new Thread(() -> {
            try {
                fs.createDeck(s.uid, d.getTitle() + " (Copy)", d.getDescription(), s.idToken);
                Platform.runLater(() -> {
                    loadDecks();
                    showInfo("Deck duplicated.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    /* =============================
        SHARING
     ============================= */

    @FXML
    private void onShareDeck() {
        Deck d = getSelectedDeckOrShowError("Select a deck to share.");
        if (d == null) return;

        Session s = Session.get();
        new Thread(() -> {
            try {
                fs.updateDeckShared(s.uid, d.getId(), true, s.idToken);
                String shareCode = s.uid + ":" + d.getId();

                Platform.runLater(() -> {
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Share Deck");
                    dialog.setHeaderText("Share this deck with another account");

                    ButtonType copyBtn = new ButtonType("Copy", ButtonBar.ButtonData.LEFT);
                    dialog.getDialogPane().getButtonTypes().addAll(copyBtn, ButtonType.CLOSE);

                    TextField tf = new TextField(shareCode);
                    tf.setEditable(false);
                    dialog.getDialogPane().setContent(tf);

                    ((Button) dialog.getDialogPane().lookupButton(copyBtn))
                            .setOnAction(ev -> {
                                ClipboardContent cc = new ClipboardContent();
                                cc.putString(shareCode);
                                Clipboard.getSystemClipboard().setContent(cc);
                            });

                    dialog.showAndWait();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onImportShared() {
        TextInputDialog t = new TextInputDialog();
        t.setHeaderText("Import shared deck");
        t.setContentText("Enter share code (uid:deckId):");
        String code = t.showAndWait().orElse(null);
        if (code == null || !code.contains(":")) {
            showError("Invalid code.");
            return;
        }

        String[] parts = code.split(":");
        String ownerUid = parts[0];
        String deckId = parts[1];

        Session s = Session.get();

        new Thread(() -> {
            try {
                Deck source = fs.getDeck(ownerUid, deckId, s.idToken);
                if (source == null) {
                    Platform.runLater(() -> showError("Deck not found."));
                    return;
                }

                List<Card> cards = fs.listCards(ownerUid, deckId, s.idToken);

                String newDeckId = fs.createDeck(
                        s.uid,
                        source.getTitle() + " (Shared)",
                        source.getDescription(),
                        s.idToken
                );

                for (Card c : cards) {
                    fs.addCard(s.uid, newDeckId, c.getFront(), c.getBack(), s.idToken);
                }

                Platform.runLater(() -> {
                    loadDecks();
                    showInfo("Shared deck imported.");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> showError("Failed: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onLogout() {
        Session.clear();
        AppState.currentDeck = null;
        Navigator.navigate("LoginView.fxml");
    }

    /* =============================
        Alerts
     ============================= */

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
