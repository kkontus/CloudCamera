package com.kkontus.cloudcamera.activities;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kkontus.cloudcamera.CloudCameraApplication;
import com.kkontus.cloudcamera.R;
import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.logger.LoggerLevel;

public class OneDriveWithOneDriveApiActivity extends AppCompatActivity {
    public static final String TAG = "OneDriveActivity";

    private TextView resultTextView;
    private ImageView imgProfilePicOneDrive;
    private TextView txtNameOneDrive, txtEmailOneDrive;
    private LinearLayout llProfileLayoutOneDrive;
    private Button oneDriveLogIn;
    private ProgressDialog progressDialog;
    private boolean isLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_drive);

        Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Bold.ttf");
        resultTextView = (TextView) findViewById(R.id.resultTextViewOneDrive);
        imgProfilePicOneDrive = (ImageView) findViewById(R.id.imgProfilePicOneDrive);
        txtNameOneDrive = (TextView) findViewById(R.id.txtNameOneDrive);
        txtEmailOneDrive = (TextView) findViewById(R.id.txtEmailOneDrive);
        llProfileLayoutOneDrive = (LinearLayout) findViewById(R.id.llProfileOneDrive);

        oneDriveLogIn = (Button) findViewById(R.id.one_drive_login);
        oneDriveLogIn.setTypeface(tf);
        oneDriveLogIn.setTextSize(14);

        if (getOneDriveClient() != null) {
            showLoggedOutScreen();
        } else {
            showLoggedInScreen();
        }

        oneDriveLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOneDriveClient() != null) {
                    signOut();
                } else {
                    createOneDriveClient();
                }
            }
        });


//        if (getOneDriveClient() != null) {
//            signOut();
//        } else {
//            createOneDriveClient();
//        }
    }

    /**
     * Create the client configuration
     *
     * @return the newly created configuration
     */
    private IClientConfig createConfig() {
        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
            @Override
            public String getClientId() {
                return "7ae2a76a-c91d-49a8-8e00-aae2f3258217";
            }

            @Override
            public String[] getScopes() {
                return new String[]{"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};
            }
        };

        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
        config.getLogger().setLoggingLevel(LoggerLevel.Debug);

        return config;
    }

    synchronized void createOneDriveClient() {
        final ICallback<IOneDriveClient> callback = new ICallback<IOneDriveClient>() {
            @Override
            public void success(IOneDriveClient iOneDriveClient) {
                System.out.println(TAG + " success");

                CloudCameraApplication.mClient.set(iOneDriveClient);

                showLoggedOutScreen();
            }

            @Override
            public void failure(ClientException ex) {
                System.out.println(TAG + " failure");
            }
        };

        new OneDriveClient.Builder()
                .fromConfig(createConfig())
                .loginAndBuildClient(this, callback);
    }

    /**
     * Clears out the auth token from the application store
     */
    void signOut() {
        if (CloudCameraApplication.mClient.get() == null) {
            return;
        }
        CloudCameraApplication.mClient.get().getAuthenticator().logout(new ICallback<Void>() {
            @Override
            public void success(final Void result) {
                CloudCameraApplication.mClient.set(null);

                System.out.println(TAG + " Logout success");

                showLoggedInScreen();
            }

            @Override
            public void failure(final ClientException ex) {
                System.out.println(TAG + " Logout error");

                showLoggedOutScreen();
            }
        });
    }

    /**
     * Get an instance of the service
     *
     * @return The Service
     */
    synchronized IOneDriveClient getOneDriveClient() {
        if (CloudCameraApplication.mClient.get() == null) {
            return null;
        }
        return CloudCameraApplication.mClient.get();
    }

    private void showLoggedInScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                oneDriveLogIn.setText("Log in");
            }
        });
    }

    private void showLoggedOutScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                oneDriveLogIn.setText("Log out");
            }
        });
    }

}