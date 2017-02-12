package com.kkontus.cloudcamera.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkontus.cloudcamera.R;
import com.kkontus.cloudcamera.domain.User;
import com.kkontus.cloudcamera.helpers.AppSettings;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

public class FacebookActivity extends AppCompatActivity {
    public static final String TAG = "FacebookActivity";

    private TextView resultTextView;
    private ImageView imgProfilePicFacebook;
    private TextView txtNameFacebook, txtEmailFacebook;
    private LinearLayout llProfileLayoutFacebook;
    //private ProgressDialog progressDialog;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private GraphRequest graphRequestProfile;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        resultTextView = (TextView) findViewById(R.id.resultTextViewFacebook);
        imgProfilePicFacebook = (ImageView) findViewById(R.id.imgProfilePicFacebook);
        txtNameFacebook = (TextView) findViewById(R.id.txtNameFacebook);
        txtEmailFacebook = (TextView) findViewById(R.id.txtEmailFacebook);
        llProfileLayoutFacebook = (LinearLayout) findViewById(R.id.llProfileFacebook);

        handleViewVisibility();

        callbackManager = CallbackManager.Factory.create();

        Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Bold.ttf");
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setTypeface(tf);
        loginButton.setTextSize(14);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                System.out.println(TAG + " onSuccess(LoginResult loginResult)");
                System.out.println(loginResult.getAccessToken().toString());
                System.out.println(loginResult.getAccessToken().getToken());
                System.out.println(loginResult.getAccessToken().getUserId());

                resultTextView.setText(loginResult.getAccessToken().getToken());
                getProfileData(loginResult);
            }

            @Override
            public void onCancel() {
                // App code

                System.out.println(TAG + " onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code

                System.out.println(TAG + " onError");
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // TODO Auto-generated method stub
                System.out.println("FB onCurrentAccessTokenChanged");

                if (currentAccessToken != null) {
                    showLoggedInScreen();
                } else {
                    showLoggedOutScreen();
                }
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                // TODO Auto-generated method stub
                System.out.println("FB onCurrentProfileChanged");
//				System.out.println("FB currentProfile.getFirstName()" + currentProfile.getFirstName());
//				System.out.println("FB currentProfile.getLastName()" + currentProfile.getLastName());
//				System.out.println("FB currentProfile.getName()" + currentProfile.getName());
//				System.out.println("FB currentProfile.getLinkUri()" + currentProfile.getLinkUri());
//				System.out.println("FB currentProfile.getId()" + currentProfile.getId());
//				System.out.println("FB currentProfile.getProfilePictureUri()" + currentProfile.getProfilePictureUri(100, 100));

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
    }

    private void handleViewVisibility() {
        if (AccessToken.getCurrentAccessToken() != null) {
            SharedPreferences prefs = getSharedPreferences(AppSettings.FACEBOOK_PREFS_NAME, Context.MODE_PRIVATE);
            String rawResponse = prefs.getString(AppSettings.FACEBOOK_PROFILE_DATA, null);
            parseProfileData(rawResponse);

            showLoggedInScreen();
        } else {
            showLoggedOutScreen();
        }
    }

    private void showLoggedInScreen() {
        llProfileLayoutFacebook.setVisibility(View.VISIBLE);
    }

    private void showLoggedOutScreen() {
        llProfileLayoutFacebook.setVisibility(View.GONE);
        resultTextView.setText("Logged out");
        txtNameFacebook.setText("");
        txtEmailFacebook.setText("");
        imgProfilePicFacebook.setImageBitmap(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(getApplication());
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    private void getProfileData(LoginResult result) {
        graphRequestProfile = GraphRequest.newMeRequest(result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                System.out.println("RESPONSE: " + response.toString());
                String rawResponse = response.getRawResponse();

                SharedPreferences settings = getSharedPreferences(AppSettings.FACEBOOK_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString(AppSettings.FACEBOOK_PROFILE_DATA, rawResponse);
                editor.apply();

                parseProfileData(rawResponse);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email, picture");
        graphRequestProfile.setParameters(parameters);
        graphRequestProfile.executeAsync();
    }

    private void parseProfileData(String rawResponse) {
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(rawResponse, User.class);
        System.out.println(user.getFacebookId());
        System.out.println(user.getName());
        System.out.println(user.getEmail());
        System.out.println(user.getFacebookPicture().getData().getUrl());

        txtNameFacebook.setText(user.getName());
        txtEmailFacebook.setText(user.getEmail());

        String url = "https://graph.facebook.com/" + user.getFacebookId() + "/picture?height=100&width=100";
        new LoadImage().execute(url);
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = new ProgressDialog(FacebookActivity.this);
            //progressDialog.setMessage("Loading Image ....");
            //progressDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                imgProfilePicFacebook.setImageBitmap(image);
                //progressDialog.dismiss();
            } else {
                //progressDialog.dismiss();
                Toast.makeText(FacebookActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
