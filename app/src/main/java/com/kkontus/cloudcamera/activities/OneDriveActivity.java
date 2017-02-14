package com.kkontus.cloudcamera.activities;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kkontus.cloudcamera.CloudCameraApplication;
import com.kkontus.cloudcamera.R;
import com.microsoft.graph.authentication.IAuthenticationAdapter;
import com.microsoft.graph.authentication.MSAAuthAndroidAdapter;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.extensions.GraphServiceClient;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.extensions.ProfilePhoto;

public class OneDriveActivity extends AppCompatActivity {
    public static final String TAG = "OneDriveActivity";

    private TextView resultTextView;
    private ImageView imgProfilePicOneDrive;
    private TextView txtNameOneDrive, txtEmailOneDrive;
    private LinearLayout llProfileLayoutOneDrive;
    private ProgressDialog progressDialog;
    private Button oneDriveLogIn;

    IAuthenticationAdapter mAuthenticationAdapter = null;
    IClientConfig mClientConfig = null;

    @NonNull
    private IAuthenticationAdapter getIAuthenticationAdapter() {
        return new MSAAuthAndroidAdapter(getApplication()) {
            @Override
            public String getClientId() {
                return "7ae2a76a-c91d-49a8-8e00-aae2f3258217";
            }

            @Override
            public String[] getScopes() {
                return new String[]{
                        // An example set of scopes your application could use
                        "https://graph.microsoft.com/Calendars.ReadWrite",
                        "https://graph.microsoft.com/Contacts.ReadWrite",
                        "https://graph.microsoft.com/Files.ReadWrite",
                        "https://graph.microsoft.com/Mail.ReadWrite",
                        "https://graph.microsoft.com/Mail.Send",
                        "https://graph.microsoft.com/User.ReadBasic.All",
                        "https://graph.microsoft.com/User.ReadWrite",
                        "offline_access",
                        "openid"
                };
            }
        };
    }

    /**
     * Get an instance of the service
     *
     * @return The Service
     */
    synchronized IGraphServiceClient getGraphClient() {
        if (CloudCameraApplication.mGraphClient.get() == null) {
            return null;
        }
        return CloudCameraApplication.mGraphClient.get();
    }

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


        mAuthenticationAdapter = getIAuthenticationAdapter();
        mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationAdapter);

        if (getGraphClient() != null) {
            showLoggedOutScreen();
        } else {
            showLoggedInScreen();
        }


        if (getGraphClient() != null) {
            CloudCameraApplication.mGraphClient.get().getMe().getPhoto().buildRequest().get(new ICallback<ProfilePhoto>() {
                @Override
                public void success(ProfilePhoto profilePhoto) {
                    System.out.println(TAG + " success ProfilePhoto profilePhoto");
                    System.out.println(profilePhoto.getRawObject());
                }

                @Override
                public void failure(ClientException ex) {
                    System.out.println(TAG + " failure ProfilePhoto profilePhoto");
                }
            });
        }


        oneDriveLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getGraphClient() != null) {
                    signOut();
                } else {
                    signIn();
                }
            }
        });
    }

    private void signIn() {
        mAuthenticationAdapter.login(OneDriveActivity.this, new ICallback<Void>() {
            @Override
            public void success(final Void aVoid) {
                // Handle successful login
                System.out.println(TAG + " login success");
                IGraphServiceClient mGraphClient = new GraphServiceClient
                        .Builder()
                        .fromConfig(mClientConfig)
                        .buildClient();

                CloudCameraApplication.mGraphClient.set(mGraphClient);
                showLoggedOutScreen();
            }

            @Override
            public void failure(final ClientException ex) {
                // Handle failed login
                System.out.println(TAG + " login failure");
                showLoggedInScreen();
            }
        });
    }

    private void signOut() {
        mAuthenticationAdapter.logout(new ICallback<Void>() {
            @Override
            public void success(final Void aVoid) {
                // Handle successful logout
                System.out.println(TAG + " logout success");
                CloudCameraApplication.mGraphClient.set(null);
                showLoggedInScreen();
            }

            @Override
            public void failure(final ClientException ex) {
                // Handle failed logout
                System.out.println(TAG + " logout failure");
            }
        });
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
