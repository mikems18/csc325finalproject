package fastcards;

import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

/**
 * Firebase Authentication: email + password only.
 */
public class AuthService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public Session signInWithEmail(String email, String password) throws Exception {
        String url = FirebaseConfig.IDP_BASE + "/accounts:signInWithPassword?key=" + FirebaseConfig.API_KEY;

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        body.addProperty("returnSecureToken", true);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("Login failed: " + res.body());
        }

        JsonObject json = gson.fromJson(res.body(), JsonObject.class);
        String idToken = json.get("idToken").getAsString();
        String uid = json.get("localId").getAsString();
        String mail = json.has("email") ? json.get("email").getAsString() : email;

        return new Session(uid, mail, idToken);
    }

    public Session registerWithEmail(String email, String password) throws Exception {
        String url = FirebaseConfig.IDP_BASE + "/accounts:signUp?key=" + FirebaseConfig.API_KEY;

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        body.addProperty("returnSecureToken", true);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("Register failed: " + res.body());
        }

        JsonObject json = gson.fromJson(res.body(), JsonObject.class);
        String idToken = json.get("idToken").getAsString();
        String uid = json.get("localId").getAsString();
        String mail = json.has("email") ? json.get("email").getAsString() : email;

        return new Session(uid, mail, idToken);
    }
}
