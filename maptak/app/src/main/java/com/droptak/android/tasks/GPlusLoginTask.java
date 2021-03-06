package com.droptak.android.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;

import com.droptak.android.activities.MainActivity;
import com.droptak.android.interfaces.OnGPlusLoginListener;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class GPlusLoginTask
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    /** Constant which indicates the request code that starts a sign-in activity */
    public static final int RC_SIGN_IN = 32356;

    /** Static method that logs the user out. Technically. */
    public static void logout(Context c) {

        SharedPreferences prefs = c.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        // Get the user's ID
        final String id = prefs.getString(MainActivity.PREF_USER_MAPTAK_TOKEN, "");
        if (id.equals("")) {
            return;
        }

        // Make the app forget about their OAUTH token and personal information
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_NAME, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_EMAIL, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_ID, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_TOKEN, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_MAPTAK_TOKEN, "").commit();

        // Set the flag that the user is logged out
        prefs.edit().putBoolean(MainActivity.PREF_USER_GPLUS_ISLOGGEDIN, false).commit();

        // Log out from the droptak server
        new Thread(new Runnable() {
            public void run() {
                String url = "http://mapitapps.appspot.com/api/v1/logout/" + id;
                HttpClient c = new DefaultHttpClient();
                try {
                    c.execute(new HttpPost(url));
                } catch (IOException e) {}
            }
        }).start();

    }

    private Activity activity;
    private GoogleApiClient apiClient = null;
    private OnGPlusLoginListener listener;

    /** Constructor. Calling this automatically initiates the task and sets appropriate fields in
     *  in the shared preferences */
    public GPlusLoginTask(Activity a, OnGPlusLoginListener listener) {
        Log.d(MainActivity.LOG_TAG, "Starting Google+ signin.");
        this.activity = a;
        this.listener = listener;
        createAPIClient();
    }

    /** Sets an OnGPLusLoginListener to call when the login has completed */
    public void setOnGPlusLoginListener(OnGPlusLoginListener listener) {
        this.listener = listener;
    }

    /** Connects the client */
    public void connect() {
        if (apiClient != null) {
            apiClient.connect();
        }
    }

    /** Disconnects the client. The only way to recreate it is to create a new GPlusLoginTask */
    public void disconnect() {
        if (apiClient != null) {
            apiClient.disconnect();
        }
    }

    /** Creates the google plus API client and attempts to authenticate the user with google+ signon */
    private void createAPIClient() {
        apiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, null)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        apiClient.connect();
    }

    /** Gets the user's oauth token and sets it in the shared preferences.
     *  This is done off the UI thread, as required by all-mighty google. */
    private void setOAUTHToken() {

        new Thread(new Runnable() {
            public void run() {

                // String which holds the scope of our oauth token
                String scope = "oauth2:https://www.googleapis.com/auth/plus.login";

                // Bundle which holds some stuff Google needs
                Bundle bundle = new Bundle();
                bundle.putString(GoogleAuthUtil.KEY_REQUEST_VISIBLE_ACTIVITIES, "http://schemas.google.com/AddActivity");

                try {

                    // Get the OAUTH token
                    String token = GoogleAuthUtil.getToken(
                            activity, Plus.AccountApi.getAccountName(apiClient), scope, bundle);

                    // And add it to the shared prefs
                    SharedPreferences prefs = activity.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putString(MainActivity.PREF_USER_GPLUS_TOKEN, token).commit();

                    // Set the flag that the user is officially logged in
                    Log.d(MainActivity.LOG_TAG, "All information obtained from Google+ API");
                    prefs.edit().putBoolean(MainActivity.PREF_USER_GPLUS_ISLOGGEDIN, true).commit();

                    // Call the listener
                    // Note that this might cause future problems because it is being called on a worker thread
                    // Be aware.
                    if (listener != null) {
                        Log.d(MainActivity.LOG_TAG, "Google+ signin complete.");
                        listener.onGooglePlusLogin();
                    }

                } catch (UserRecoverableAuthException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    /** The client has successfully connected and we can get all the user's information. */
    public void onConnected(Bundle bundle) {

        // Get the person currently signed in
        Person person = Plus.PeopleApi.getCurrentPerson(apiClient);

        // Prepare the shared preferences
        SharedPreferences prefs = activity.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        // Get the user's information and store it in the shared preferences
        String name = person.getDisplayName();
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_NAME, name).commit();
        String email = Plus.AccountApi.getAccountName(apiClient);
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_EMAIL, email).commit();
        String gplusID = person.getId();
        prefs.edit().putString(MainActivity.PREF_USER_GPLUS_ID, gplusID).commit();

        // Get the user's oauth token and put it in the shared preferences
        Log.d(MainActivity.LOG_TAG, "Basic information parsed. Requesting OAUTH id.");
        setOAUTHToken();
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(MainActivity.LOG_TAG, "Google+ connection failed. Attempting to resolve.");
        if (connectionResult.hasResolution()) {
            try {
                activity.startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                apiClient.connect();
            }
        }
    }

}
