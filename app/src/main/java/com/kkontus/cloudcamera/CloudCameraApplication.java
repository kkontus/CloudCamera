package com.kkontus.cloudcamera;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;

/**
 * Created by Kontus on 11.2.2017..
 */

public class CloudCameraApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        AppEventsLogger.activateApp(this);
    }

}
