package fastcards;

import com.google.gson.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FirestoreService {

    private static final Gson gson = new Gson();

    private String decksCollection(String uid) {
        return String.format("projects/%s/databases/(default)/documents/users/%s/decks",
                FirebaseConfig.PROJECT_ID, uid);
    }

    private String cardsCollection(String uid, String deckId) {
        return String.format("projects/%s/databases/(default)/documents/users/%s/decks/%s/cards",
                FirebaseConfig.PROJECT_ID, uid, deckId);
    }

    // DECKS

    public List<Deck> listDecks(String uid, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + idToken);

        try (InputStream is = conn.getInputStream()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            List<Deck> decks = new ArrayList<>();
            if (obj.has("documents")) {
                for (JsonElement el : obj.getAsJsonArray("documents")) {
                    JsonObject d = el.getAsJsonObject();
                    String name = d.get("name").getAsString();
                    String id = name.substring(name.lastIndexOf('/') + 1);
                    JsonObject fields = d.getAsJsonObject("fields");
                    String title = fields.getAsJsonObject("title").get("stringValue").getAsString();
                    String desc = fields.has("description")
                            ? fields.getAsJsonObject("description").get("stringValue").getAsString()
                            : "";
                    decks.add(new Deck(id, title, desc));
                }
            }
            return decks;
        }
    }

    public String createDeck(String uid, String title, String description, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks";

        JsonObject fields = new JsonObject();

        JsonObject titleField = new JsonObject();
        titleField.addProperty("stringValue", title);
        fields.add("title", titleField);

        JsonObject descField = new JsonObject();
        descField.addProperty("stringValue", description == null ? "" : description);
        fields.add("description", descField);

        JsonObject root = new JsonObject();
        root.add("fields", fields);
        String json = gson.toJson(root);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = conn.getInputStream()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject resp = JsonParser.parseString(body).getAsJsonObject();
            String name = resp.get("name").getAsString();
            return name.substring(name.lastIndexOf('/') + 1);
        }
    }

    public void deleteDeck(String uid, String deckId, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        try (InputStream ignored = conn.getInputStream()) {
            // consume
        }
    }

    /**
     * Get a single deck (used for importing from another account).
     */
    public Deck getDeck(String uid, String deckId, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + idToken);

        try (InputStream is = conn.getInputStream()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject d = JsonParser.parseString(body).getAsJsonObject();
            if (!d.has("fields")) return null;
            JsonObject fields = d.getAsJsonObject("fields");
            String title = fields.getAsJsonObject("title").get("stringValue").getAsString();
            String desc = fields.has("description")
                    ? fields.getAsJsonObject("description").get("stringValue").getAsString()
                    : "";
            return new Deck(deckId, title, desc);
        }
    }

    /**
     * Rename deck title in Firestore (cloud sync for rename).
     */
    public void renameDeck(String uid, String deckId, String newTitle, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE +
                "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId +
                "?updateMask.fieldPaths=title";

        JsonObject fields = new JsonObject();
        JsonObject titleField = new JsonObject();
        titleField.addProperty("stringValue", newTitle);
        fields.add("title", titleField);

        JsonObject root = new JsonObject();
        root.add("fields", fields);
        String json = gson.toJson(root);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        // HttpURLConnection doesn't support PATCH, so we POST with override
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream ignored = conn.getInputStream()) {
            // ignore body
        }
    }

    // For any old calls named updateDeckTitle
    public void updateDeckTitle(String uid, String deckId, String newTitle, String idToken) throws IOException {
        renameDeck(uid, deckId, newTitle, idToken);
    }

    /**
     * Mark/unmark a deck as shared in Firestore (simple boolean flag).
     */
    public void updateDeckShared(String uid, String deckId, boolean shared, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE +
                "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId +
                "?updateMask.fieldPaths=shared";

        JsonObject fields = new JsonObject();
        JsonObject sharedField = new JsonObject();
        sharedField.addProperty("booleanValue", shared);
        fields.add("shared", sharedField);

        JsonObject root = new JsonObject();
        root.add("fields", fields);
        String json = gson.toJson(root);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream ignored = conn.getInputStream()) {
            // ignore body
        }
    }

    // ------------------------------------------------------------
    // CARDS
    // ------------------------------------------------------------

    public List<Card> listCards(String uid, String deckId, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId + "/cards";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + idToken);

        try (InputStream is = conn.getInputStream()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            List<Card> cards = new ArrayList<>();
            if (obj.has("documents")) {
                for (JsonElement el : obj.getAsJsonArray("documents")) {
                    JsonObject d = el.getAsJsonObject();
                    String name = d.get("name").getAsString();
                    String id = name.substring(name.lastIndexOf('/') + 1);
                    JsonObject fields = d.getAsJsonObject("fields");
                    String front = fields.getAsJsonObject("front").get("stringValue").getAsString();
                    String back = fields.getAsJsonObject("back").get("stringValue").getAsString();
                    cards.add(new Card(id, front, back));
                }
            }
            return cards;
        }
    }

    public void addCard(String uid, String deckId, String front, String back, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId + "/cards";

        JsonObject fields = new JsonObject();

        JsonObject f = new JsonObject();
        f.addProperty("stringValue", front);
        fields.add("front", f);

        JsonObject b = new JsonObject();
        b.addProperty("stringValue", back);
        fields.add("back", b);

        JsonObject root = new JsonObject();
        root.add("fields", fields);
        String json = gson.toJson(root);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream ignored = conn.getInputStream()) {
            // ignore
        }
    }

    /**
     * Cloud update of an existing card.
     */
    public void updateCard(String uid,
                           String deckId,
                           String cardId,
                           String front,
                           String back,
                           String idToken) throws IOException {

        String url = FirebaseConfig.FIRESTORE_BASE +
                "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId +
                "/cards/" + cardId +
                "?updateMask.fieldPaths=front&updateMask.fieldPaths=back";

        JsonObject fields = new JsonObject();

        JsonObject f = new JsonObject();
        f.addProperty("stringValue", front);
        fields.add("front", f);

        JsonObject b = new JsonObject();
        b.addProperty("stringValue", back);
        fields.add("back", b);

        JsonObject root = new JsonObject();
        root.add("fields", fields);
        String json = gson.toJson(root);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream ignored = conn.getInputStream()) {
            // ignore body
        }
    }

    /**
     * Cloud delete of an existing card.
     */
    public void deleteCard(String uid, String deckId, String cardId, String idToken) throws IOException {
        String url = FirebaseConfig.FIRESTORE_BASE +
                "/projects/" + FirebaseConfig.PROJECT_ID +
                "/databases/(default)/documents/users/" + uid + "/decks/" + deckId +
                "/cards/" + cardId;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        try (InputStream ignored = conn.getInputStream()) {
            // ignore
        }
    }
}
