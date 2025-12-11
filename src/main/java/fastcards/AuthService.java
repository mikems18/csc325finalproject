package fastcards;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthService {

    private static final Gson gson = new Gson();

    // Email/password request
    private static class SignInRequest {
        String email;
        String password;
        boolean returnSecureToken = true;
    }

    // IdP (Google) request
    private static class IdpRequest {
        String postBody;
        String requestUri;
        boolean returnSecureToken = true;
    }

    // Common response fields from Firebase Auth
    private static class SignInResponse {
        @SerializedName("idToken")
        String idToken;

        @SerializedName("localId")
        String localId;

        String email;
    }

    // ------------ EMAIL / PASSWORD SIGN-IN ------------

    public Session signInWithEmail(String email, String password) throws IOException {
        String url = FirebaseConfig.IDP_BASE
                + "/accounts:signInWithPassword?key=" + FirebaseConfig.API_KEY;

        SignInRequest req = new SignInRequest();
        req.email = email;
        req.password = password;
        String json = gson.toJson(req);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (code >= 200 && code < 300) {
            SignInResponse resp = gson.fromJson(body, SignInResponse.class);
            return new Session(resp.localId, resp.idToken, resp.email);
        } else {
            throw new RuntimeException("Login failed: " + body);
        }
    }

    public Session registerWithEmail(String email, String password) throws IOException {
        String url = FirebaseConfig.IDP_BASE
                + "/accounts:signUp?key=" + FirebaseConfig.API_KEY;

        SignInRequest req = new SignInRequest();
        req.email = email;
        req.password = password;
        String json = gson.toJson(req);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (code >= 200 && code < 300) {
            SignInResponse resp = gson.fromJson(body, SignInResponse.class);
            return new Session(resp.localId, resp.idToken, resp.email);
        } else {
            throw new RuntimeException("Register failed: " + body);
        }
    }

    // ------------ GOOGLE SIGN-IN (WITH GOOGLE ID TOKEN) ------------

    /**
     * Complete Firebase sign-in using a Google ID token that you already obtained
     * from Google's OAuth flow.
     *
     * @param googleIdToken the ID token given by Google after user signs in
     */
    public Session signInWithGoogle(String googleIdToken) throws IOException {
        // Firebase REST endpoint for federated sign-in
        String url = FirebaseConfig.IDP_BASE
                + "/accounts:signInWithIdp?key=" + FirebaseConfig.API_KEY;

        // The postBody must be URL-encoded
        String postBody = "id_token="
                + URLEncoder.encode(googleIdToken, StandardCharsets.UTF_8)
                + "&providerId=google.com";

        IdpRequest req = new IdpRequest();
        req.postBody = postBody;
        // This must be an authorized domain in your Firebase Auth settings.
        // "http://localhost" usually works.
        req.requestUri = "http://localhost";

        String json = gson.toJson(req);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (code >= 200 && code < 300) {
            SignInResponse resp = gson.fromJson(body, SignInResponse.class);
            // Create a Session from Firebase's response
            Session s = new Session(resp.localId, resp.idToken, resp.email);
            Session.set(s);
            return s;
        } else {
            throw new RuntimeException("Google sign-in failed: " + body);
        }
    }
}
