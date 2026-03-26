package data;

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Auth {

    private UserRecord user;

    public Auth() {
    }

    public String authorize() {

        try {
            InputStream in = getClass()
                    .getClassLoader()
                    .getResourceAsStream("db/oauth.json");

            assert in != null;
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(
                            GsonFactory.getDefaultInstance(),
                            new InputStreamReader(in)
                    );

            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            clientSecrets,
                            List.of("openid", "email", "profile")
                    )
                            .setAccessType("offline")
                            .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(-1)
                    .build();

            String redirectUri = receiver.getRedirectUri();

            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .build();

            java.awt.Desktop.getDesktop().browse(new java.net.URI(authorizationUrl));

            String code = receiver.waitForCode();

            GoogleTokenResponse tokenResponse =
                    flow.newTokenRequest(code)
                            .setRedirectUri(redirectUri)
                            .execute();

            return tokenResponse.getIdToken();
        }
        catch (Exception e) {
            System.out.println("Error authorizing: " + e.getMessage());
            return null;
        }
    }

    public void login(String idToken) throws Exception {

        GoogleIdToken idTokenObj = GoogleIdToken.parse(
                GsonFactory.getDefaultInstance(),
                idToken
        );

        GoogleIdToken.Payload payload = idTokenObj.getPayload();
        String email = payload.getEmail();

        try {
            user = FirebaseAuth.getInstance().getUserByEmail(email);
        }
        catch (FirebaseAuthException e) {
            user = FirebaseAuth.getInstance().createUser(
                    new UserRecord.CreateRequest().setEmail(email)
            );
        }
    }

    public String getUserId() {
        if (user != null) {
            return user.getUid();
        }

        return null;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
    }
}