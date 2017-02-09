package com.kkontus.cloudcamera.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kkontus.cloudcamera.interfaces.OnLocationChange;

public class LocationChangeReceiver extends BroadcastReceiver {

    private static OnLocationChange locationChanger = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Intent Received - photos moved", Toast.LENGTH_SHORT).show();

        String action = intent.getAction();
        String uri = intent.getStringExtra("uri");

        System.out.println("action: " + action);
        System.out.println("uri: " + uri);

        if (locationChanger != null) {
            locationChanger.onLocationChange(action, uri);
        }
    }

    public void setOnLocationChangeListener(Context context) {
        LocationChangeReceiver.locationChanger = (OnLocationChange) context;
    }
}