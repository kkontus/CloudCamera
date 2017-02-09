package com.kkontus.cloudcamera.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String IMAGES_TAKEN = "imagesTaken";
    private static final String WIFI_ONLY = "wifiOnly";

    public static int getImagesTaken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(AppSettings.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(IMAGES_TAKEN, 0);
    }

    public static void setImagesTaken(Context context, int numberOfImagesTaken) {
        SharedPreferences settings = context.getSharedPreferences(AppSettings.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(IMAGES_TAKEN, numberOfImagesTaken);
        editor.apply();
    }

    public static boolean getWifiOnly(Context context) {
        SharedPreferences settings = context.getSharedPreferences(AppSettings.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(WIFI_ONLY, false);
    }

    public static void setWifiOnly(Context context, boolean wifiStatus) {
        SharedPreferences settings = context.getSharedPreferences(AppSettings.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(WIFI_ONLY, wifiStatus);
        editor.apply();
    }

}