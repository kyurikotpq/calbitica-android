package com.calbitica.app.Auth;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;

// Singleton class to have a consistent GSO client
// Technically, this is a model.... but I'll put it under Auth for now
public class GoogleAuth {
    // Singleton instance
    private static GoogleAuth instance = null;
    private GoogleSignInClient mGoogleSignInClient;

    // constant: API Client ID
    private final String API_CLIENT_ID = "464289376160-6in84jb9816ui0eea7uietultj9u9shl.apps.googleusercontent.com";

    // Constant: Google sign in options
    // Configure sign-in to request the user's ID, email address, and basic
    // profile ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
            .requestProfile()
            .requestScopes(
                    new Scope("https://www.googleapis.com/auth/calendar.readonly"),
                    new Scope("https://www.googleapis.com/auth/calendar.events")
            )
            .requestServerAuthCode(API_CLIENT_ID)
            .build();


    private GoogleAuth(Context context) {
        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static GoogleAuth getInstance(Context context) {
        if(instance == null)
            instance = new GoogleAuth(context);

        return instance;
    }

    public GoogleSignInClient getClient() {
        return mGoogleSignInClient;
    }

}
