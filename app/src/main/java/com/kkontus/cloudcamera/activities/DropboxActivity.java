package com.kkontus.cloudcamera.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.v2.DbxRawClientV2;
import com.dropbox.core.v2.auth.DbxUserAuthRequests;
import com.dropbox.core.v2.users.FullAccount;
import com.kkontus.cloudcamera.APIutils.DropboxClientFactory;
import com.kkontus.cloudcamera.APIutils.GetCurrentAccountTask;
import com.kkontus.cloudcamera.R;
import com.kkontus.cloudcamera.helpers.AppSettings;

import java.util.List;

public class DropboxActivity extends AppCompatActivity {
    public static final String TAG = "DropboxActivity";

    private Button loginButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Bold.ttf");
        resultTextView = (TextView) findViewById(R.id.resultTextViewDropbox);
        loginButton = (Button) findViewById(R.id.dropbox_login);
        loginButton.setTypeface(tf);
        loginButton.setTextSize(14);

        if (hasToken()) {
            System.out.println(TAG + " has a token");
            showLoggedOutScreen();
        } else {
            System.out.println(TAG + " hasn't got a token");
            showLoggedInScreen();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasToken()) {
                    SharedPreferences prefs = getSharedPreferences(AppSettings.DROPBOX_PREFS_NAME, MODE_PRIVATE);
                    final String accessToken = prefs.getString(AppSettings.DROPBOX_ACCESS_TOKEN, null);

                    DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Cloud Camera/1.0").build();
                    DbxHost host = DbxHost.DEFAULT;
                    final DbxRawClientV2 dbxRawClientV2 = new DbxRawClientV2(requestConfig, host) {
                        @Override
                        protected void addAuthHeaders(List<HttpRequestor.Header> headers) {
                            HttpRequestor.Header header = new HttpRequestor.Header("Authorization", "Bearer" + " " + accessToken);

                            headers.add(header);

                            for (HttpRequestor.Header h : headers) {
                                System.out.println(h.getKey() + " " + h.getValue());
                            }
                        }
                    };
                    final Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DbxUserAuthRequests requests = new DbxUserAuthRequests(dbxRawClientV2);
                            try {
                                requests.tokenRevoke();
                                showLoggedInScreen();
                                System.out.println(TAG + " DbxUserAuthRequests token revoked");
                                DropboxClientFactory.clearClient();
                                AuthActivity.result = null;
                                clearKeys();
                            } catch (DbxException e) {
                                System.out.println(TAG + " DbxUserAuthRequests token revoke error");
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } else {
                    Auth.startOAuth2Authentication(DropboxActivity.this, getString(R.string.app_key));
                    showLoggedOutScreen();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(AppSettings.DROPBOX_PREFS_NAME, MODE_PRIVATE);
        String accessToken = prefs.getString(AppSettings.DROPBOX_ACCESS_TOKEN, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(AppSettings.DROPBOX_ACCESS_TOKEN, accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
    }

    private void initAndLoadData(String accessToken) {
        System.out.println(TAG + " initAndLoadData token " + accessToken);
        DropboxClientFactory.init(accessToken);
        loadData();
    }

    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                System.out.println(TAG + " loadData onComplete");

                System.out.println(result.getEmail());
                System.out.println(result.getName().getDisplayName());
                System.out.println(result.getAccountType().name());
            }

            @Override
            public void onError(Exception e) {
                System.out.println(TAG + " loadData onError");
            }
        }).execute();
    }

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.DROPBOX_PREFS_NAME, MODE_PRIVATE);
        String accessToken = prefs.getString(AppSettings.DROPBOX_ACCESS_TOKEN, null);
        return accessToken != null;
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.DROPBOX_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private void showLoggedInScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginButton.setText("Log in");
            }
        });
    }

    private void showLoggedOutScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginButton.setText("Log out");
            }
        });
    }

}
