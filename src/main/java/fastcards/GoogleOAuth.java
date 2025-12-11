package fastcards;

public class GoogleOAuth {

    // Your desktop client ID (the one you pasted before)
    public static final String CLIENT_ID =
            "496973212531-ptc0e5ps5rpp38bitgku3bv7617rb5tj.apps.googleusercontent.com";

    /**
     * Placeholder for the Google OAuth flow.
     *
     * This method needs to:
     *  1) Open a browser and let the user sign in with Google
     *  2) Receive an authorization code from Google
     *  3) Exchange that code at https://oauth2.googleapis.com/token
     *     for an ID token
     *  4) Return that ID token string
     *
     * For now, this is just a stub so the project compiles.
     */
    public static String signInAndGetIdToken() throws Exception {
        throw new UnsupportedOperationException(
                "Google sign-in flow is not implemented yet. " +
                        "Implement OAuth here and return the Google ID token.");
    }
}
