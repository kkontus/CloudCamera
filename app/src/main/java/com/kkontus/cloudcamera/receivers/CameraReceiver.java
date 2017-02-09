package com.kkontus.cloudcamera.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kkontus.cloudcamera.helpers.SharedPreferencesHelper;
import com.kkontus.cloudcamera.interfaces.OnDataPass;

public class CameraReceiver extends BroadcastReceiver {

    private static OnDataPass dataPasser = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Intent Received - New photo taken", Toast.LENGTH_SHORT).show();

        if (dataPasser != null) {
            int imagesTaken = SharedPreferencesHelper.getImagesTaken(context);
            int updateImagesTakenValue = imagesTaken + 1;
            SharedPreferencesHelper.setImagesTaken(context, updateImagesTakenValue);
            dataPasser.onDataPass(updateImagesTakenValue);
        }
    }

    public void setOnDataPassListener(Context context) {
        CameraReceiver.dataPasser = (OnDataPass) context;
    }

}
