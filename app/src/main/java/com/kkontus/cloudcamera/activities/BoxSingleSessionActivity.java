package com.kkontus.cloudcamera.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.kkontus.cloudcamera.R;
import com.kkontus.cloudcamera.helpers.AppSettings;

import java.io.IOException;
import java.io.InputStream;

public class BoxSingleSessionActivity extends AppCompatActivity implements BoxAuthentication.AuthListener {
    public static final String TAG = "BoxActivity";

    private BoxSession mSession = null; //CloudCameraApplication.mSession;
    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;
    private static final String OAUTH_2 = "oauth2:";
    public final static String ACCESS_USER_ID = "ACCESS_USER_ID";
    public final static String ACCESS_KEY_NAME = "ACCESS_KEY_BOX";
    public final static String ACCESS_SECRET_NAME = "ACCESS_SECRET_BOX";
    private Button btnAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        btnAuth = (Button) findViewById(R.id.box_login);

        SharedPreferences prefs = getSharedPreferences(AppSettings.BOX_PREFS_NAME, Context.MODE_PRIVATE);
        String keyName = prefs.getString(ACCESS_KEY_NAME, null);
        String secretName = prefs.getString(ACCESS_SECRET_NAME, null);
        String userId = prefs.getString(ACCESS_USER_ID, null);

        System.out.println(TAG + " onCreate");
        System.out.println(keyName);
        System.out.println(secretName);
        System.out.println(userId);

        //CloudCameraApplication.configureClient();
        configureClient();
        initSession();

        BoxAuthentication.BoxAuthenticationInfo info = BoxAuthentication.getInstance().getAuthInfo(userId, getApplicationContext());
        if (info != null && info.accessToken() != null) {
            System.out.println(TAG + " getAuthInfo != null");
            showLoggedOutScreen();
        } else {
            System.out.println(TAG + " getAuthInfo == null");
            showLoggedInScreen();
        }

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = getSharedPreferences(AppSettings.BOX_PREFS_NAME, Context.MODE_PRIVATE);
                String keyName = prefs.getString(ACCESS_KEY_NAME, null);
                String secretName = prefs.getString(ACCESS_SECRET_NAME, null);
                String userId = prefs.getString(ACCESS_USER_ID, null);

                System.out.println(TAG + " onClick");
                System.out.println(keyName);
                System.out.println(secretName);
                System.out.println(userId);


                final BoxAuthentication.BoxAuthenticationInfo info = BoxAuthentication.getInstance().getAuthInfo(userId, getApplicationContext());
                if (info != null && info.accessToken() != null) {
                    if (mSession != null) {
                        System.out.println(TAG + " mSession != null");

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BoxAuthentication.getInstance().logout(mSession);
                                info.wipeOutAuth();
                            }
                        });
                        thread.start();

                        SharedPreferences settings = getSharedPreferences(AppSettings.BOX_PREFS_NAME, Context.MODE_PRIVATE);
                        settings.edit().clear().commit();
                        showLoggedInScreen();
                    } else {
                        System.out.println(TAG + " mSession == null");

                        // this should't happen, but in case it does then we will logout all users
                        // BoxAuthentication.getInstance().logoutAllUsers(getApplicationContext());
                        // info.wipeOutAuth();
                    }
                } else {
                    //  CloudCameraApplication.configureClient();
                    // initSession();
                    System.out.println(TAG + " onClick authenticate");
                    authenticate();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println(TAG + " onDestroy");

        if (mSession != null) {
            System.out.println(TAG + " onDestroy setSessionAuthListener");
            mSession.setSessionAuthListener(null);
        } else {
            System.out.println(TAG + " onDestroy SRANJE");
        }
    }

    public static void configureClient() {
        BoxConfig.IS_LOG_ENABLED = true;

        BoxConfig.CLIENT_ID = "38ofrooqp8q9fzvhvefewy8r9kfmfm8i";
        BoxConfig.CLIENT_SECRET = "LncTLpWhHUtNEV4AQeOTpKhBrFMcQQCX";
        // must match the redirect_uri set in your developer account if one has been set. Redirect uri should not be of type file:// or content://.
        BoxConfig.REDIRECT_URL = "http://localhost";
    }

    private void initSession() {

//        if (mSession != null)s {
//            System.out.println(TAG + " initSession OK");
//            mSession.setSessionAuthListener(null);
//        } else {
//            System.out.println(TAG + " initSession SRANJE");
//        }


        mSession = new BoxSession(this);
        //mSession.setSessionAuthListener(this);
        //mSession.authenticate(this);
    }

    private void authenticate() {
        System.out.println(TAG + " authenticate");
        System.out.println(mSession);
        mSession.setSessionAuthListener(null);
        mSession = new BoxSession(this);
        mSession.setSessionAuthListener(this);
        mSession.authenticate(this);
    }


    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
        System.out.println(TAG + " onRefreshed");

        // do nothing when auth info is refreshed
        storeAuth(info.accessToken(), info.getUser().getId());
    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        System.out.println(TAG + " onAuthCreated");

        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);
        //loadRootFolder();
        //uploadSampleFile();

        showLoggedOutScreen();
        storeAuth(info.accessToken(), info.getUser().getId());
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        System.out.println(TAG + " onAuthFailure");

        if (ex != null) {

        } else if (info == null) {

        }
    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        System.out.println(TAG + " onLoggedOut");

        // unable to change btnAuth from here when going back to MainActivity
        // so we added showLoggedInScreen(); to btnAuth onClick
        //showLoggedInScreen();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    public void storeAuth(String oauth2AccessToken, String userId) {
        System.out.println(TAG + " storeAuth");
        System.out.println(TAG + oauth2AccessToken);

        // Store the OAuth 2 access token, if there is one.
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(AppSettings.BOX_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, OAUTH_2);
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.putString(ACCESS_USER_ID, userId);
            edit.commit();
        }
    }

    private void showLoggedInScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnAuth.setText("Log in");
            }
        });
    }

    private void showLoggedOutScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnAuth.setText("Log out");
            }
        });
    }

    //Method to demonstrate fetching folder items from the root folder
    private void loadRootFolder() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //Api to fetch root folder
                    final BoxIteratorItems folderItems = mFolderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID).send();
                    System.out.println("LIST ROOT FOLDER");
                    for (BoxItem boxItem : folderItems) {
                        System.out.println(boxItem.getName());
                    }
                } catch (BoxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Method demonstrates a sample file being uploaded using the file api
     */
    private void uploadSampleFile() {
        new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(TAG + " uploadSampleFile");
                    String uploadFileName = "CloudCamera.sqlite";
                    InputStream uploadStream = getResources().getAssets().open(uploadFileName);
                    String destinationFolderId = "0";
                    String uploadName = "CloudCamera.sqlite";
                    BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadStream, uploadName, destinationFolderId);
                    final BoxFile uploadFileInfo = request.send();
                    System.out.println("Uploaded " + uploadFileInfo.getName());
                    loadRootFolder();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BoxException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        }.start();
    }

}