package com.kkontus.cloudcamera;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.onedrive.sdk.extensions.IOneDriveClient;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Kontus on 11.2.2017..
 */

public class CloudCameraApplication extends Application {

    /**
     * The service instance
     */
    public static AtomicReference<IOneDriveClient> mClient = new AtomicReference<>();
    public static AtomicReference<IGraphServiceClient> mGraphClient = new AtomicReference<>();

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        // Facebook
        AppEventsLogger.activateApp(this);
    }

}