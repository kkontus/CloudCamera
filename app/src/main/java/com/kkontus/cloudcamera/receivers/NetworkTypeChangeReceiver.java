package com.kkontus.cloudcamera.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.kkontus.cloudcamera.helpers.ServiceHelper;
import com.kkontus.cloudcamera.helpers.SharedPreferencesHelper;
import com.kkontus.cloudcamera.interfaces.OnNetworkTypeChange;

import org.apache.commons.collections4.MultiMap;

public class NetworkTypeChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkTypeChangeRec";
    private static OnNetworkTypeChange networkTypeChanger = null;
    private static boolean isUploadStateAllowed = false;
    private static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Intent Received - Network State Changed", Toast.LENGTH_SHORT).show();

        ServiceHelper serviceHelper = new ServiceHelper();
        MultiMap<String, String> albumsPerService = serviceHelper.loadServiceAlbumsCombinationsFromDatabase(context);

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        final NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        //check if we have any kind of connection
        if (activeNetInfo != null) {
            //if we have connection to network and intent is not duplicate go ahead and check if upload statuses are valid
            if (firstConnect) {
                firstConnect = false; //set firstConnect to false so we can skip duplicate

                boolean wifiOnly = SharedPreferencesHelper.getWifiOnly(context);
                //if Wi-Fi Only checked then check if we are on Wi-Fi, if we are then start service, otherwise stop service
                if (wifiOnly) {
                    if (mWifi != null && !mWifi.isRoaming() && mWifi.isAvailable() && mWifi.isConnected()) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                        if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                            String ssid = connectionInfo.getSSID();
                            isUploadStateAllowed = true;
                            serviceHelper.serviceIteratorStart(context, albumsPerService);

                            Log.i(TAG, "Upload available only when connected to WIFI, currently connected to SSID: " + ssid);
                        }
                    } else {
                        isUploadStateAllowed = false;
                        serviceHelper.serviceIteratorStop(context, albumsPerService);

                        Log.i(TAG, "Upload available only when connected to WIFI, but currently you are not connected to WIFI");
                    }
                }
                //if Wi-Fi Only is not checked then just check if we have any network connection, and start service
                else {
                    if ((mWifi != null && !mWifi.isRoaming() && mWifi.isAvailable() && mWifi.isConnected()) ||
                            (mMobile != null && !mMobile.isRoaming() && mMobile.isAvailable() && mMobile.isConnected())) {

                        String connectionType = null;
                        if (mWifi.isConnected()) {
                            connectionType = "WIFI";
                        }
                        if (mMobile.isConnected()) {
                            connectionType = "Mobile";
                        }

                        isUploadStateAllowed = true;
                        serviceHelper.serviceIteratorStart(context, albumsPerService);

                        Log.i(TAG, "Upload available both on WIFI or Mobile, currently connected via: " + connectionType);
                    }
                }
            } else {
                //if we have connection to network but it is received as duplicate just reset firstConnect flag so next intent can be used
                firstConnect = true;
            }
        }
        //if we don't have any kind of connection then stop service, reset firstConnect flag and set isUploadStateAllowed flag to false (no upload)
        else {
            firstConnect = true;
            isUploadStateAllowed = false;
            serviceHelper.serviceIteratorStop(context, albumsPerService);

            Log.i(TAG, "Upload available both on WIFI or Mobile, but currently you are not connected on WIFI nor Mobile");
        }


        if (networkTypeChanger != null) {
            networkTypeChanger.onNetworkTypeChange(isUploadStateAllowed);
        }
    }

    public void setOnNetworkTypeChangeListener(Context context) {
        NetworkTypeChangeReceiver.networkTypeChanger = (OnNetworkTypeChange) context;
    }

}

