package fastcards;

import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Firestore wrapper:
 *   users/{uid}/decks/{deckId}
 *   users/{uid}/decks/{deckId}/cards/{cardId}
 */
public class FirestoreService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    private static String decksBase(String uid) {
        return FirebaseConfig.FIRESTORE_BASE + "/projects/" + FirebaseConfig.PROJECT_ID
                + "/databases/(default)/documents/users/" + uid + "/decks";
    }

    private static String cardsBase(String uid, String deckId) {
        return decksBase(uid) + "/" + deckId + "/cards";
    }

    public List<Deck> listDecks(String uid, String idToken) throws Exception {
        String url = decksBase(uid);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("listDecks failed: " + res.body());
        }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        List<Deck> decks = new ArrayList<>();
        if (!root.has("documents")) return decks;

        for (JsonElement e : root.getAsJsonArray("documents")) {
            JsonObject doc = e.getAsJsonObject();
            String name = doc.get("name").getAsString();
            String id = name.substring(name.lastIndexOf('/') + 1);
            JsonObject fields = doc.getAsJsonObject("fields");
            String title = getString(fields, "title");
            String desc = getString(fields, "description");
            decks.add(new Deck(id, title, desc));
        }
        return decks;
    }

    public String createDeck(String uid, String title, String description, String idToken) throws Exception {
        String url = decksBase(uid);
        JsonObject fields = new JsonObject();
        JsonObject t = new JsonObject(); t.addProperty("stringValue", title);
        JsonObject d = new JsonObject(); d.addProperty("stringValue", description);
        fields.add("title", t);
        fields.add("description", d);
        JsonObject doc = new JsonObject(); doc.add("fields", fields);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(doc.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("createDeck failed: " + res.body());
        }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        String name = root.get("name").getAsString();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    public void deleteDeck(String uid, String deckId, String idToken) throws Exception {
        String url = decksBase(uid) + "/" + deckId;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .DELETE()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300 && res.statusCode() != 404) {
            throw new RuntimeException("deleteDeck failed: " + res.body());
        }
    }

    public List<Card> listCards(String uid, String deckId, String idToken) throws Exception {
        String url = cardsBase(uid, deckId);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("listCards failed: " + res.body());
        }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        List<Card> cards = new ArrayList<>();
        if (!root.has("documents")) return cards;

        for (JsonElement e : root.getAsJsonArray("documents")) {
            JsonObject doc = e.getAsJsonObject();
            String name = doc.get("name").getAsString();
            String id = name.substring(name.lastIndexOf('/') + 1);
            JsonObject fields = doc.getAsJsonObject("fields");
            String front = getString(fields, "front");
            String back = getString(fields, "back");
            cards.add(new Card(id, front, back));
        }
        return cards;
    }

    public void addCard(String uid, String deckId, String front, String back, String idToken) throws Exception {
        String url = cardsBase(uid, deckId);
        JsonObject fields = new JsonObject();
        JsonObject f = new JsonObject(); f.addProperty("stringValue", front);
        JsonObject b = new JsonObject(); b.addProperty("stringValue", back);
        fields.add("front", f);
        fields.add("back", b);
        JsonObject doc = new JsonObject(); doc.add("fields", fields);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(doc.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("addCard failed: " + res.body());
        }
    }

    public void deleteCard(String uid, String deckId, String cardId, String idToken) throws Exception {
        String url = cardsBase(uid, deckId) + "/" + cardId;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + idToken)
                .DELETE()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300 && res.statusCode() != 404) {
            throw new RuntimeException("deleteCard failed: " + res.body());
        }
    }

    static String getString(JsonObject fields, String key) {
        if (fields == null || !fields.has(key)) return "";
        JsonObject o = fields.getAsJsonObject(key);
        return o.has("stringValue") ? o.get("stringValue").getAsString() : "";
    }
}
