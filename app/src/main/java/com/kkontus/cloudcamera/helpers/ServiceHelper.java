package com.kkontus.cloudcamera.helpers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kkontus.cloudcamera.repository.DatabaseManager;
import com.kkontus.cloudcamera.repository.ImageUploadStatus;

import org.apache.commons.collections4.MultiMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ServiceHelper {

    public static final String TAG = "ServiceHelper";

    public void serviceIteratorStart(Context context, MultiMap<String, String> albumsPerService) {
        Log.i(TAG, "serviceIteratorStart");

        Set<String> keySet = albumsPerService.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        String serviceClass;
        String albumName;

        while (keyIterator.hasNext()) {
            serviceClass = keyIterator.next();
            @SuppressWarnings("unchecked")
            Collection<String> values = (Collection<String>) albumsPerService.get(serviceClass);
            Iterator<String> valuesIterator = values.iterator();
            while (valuesIterator.hasNext()) {
                albumName = (String) valuesIterator.next();
                Log.i(TAG, "serviceIteratorStart KEY service: " + serviceClass + " VALUE album: " + albumName);

                boolean running = isMyServiceRunning(context, serviceClass);
                if (running) {
                    Log.i(TAG, "Everything running, skip it in serviceIteratorStart");
                    continue;
                } else {
                    Log.i(TAG, "We should start service: " + serviceClass + " for album: " + albumName);
                    startServiceAutomatically(context, serviceClass, albumName);
                }
            }
        }
    }

    public void serviceIteratorStop(Context context, MultiMap<String, String> albumsPerService) {
        Log.i(TAG, "serviceIteratorStop");

        Set<String> keySet = albumsPerService.keySet();
        Iterator<String> keyIterator = keySet.iterator();
        String serviceClass;
        String albumName;

        while (keyIterator.hasNext()) {
            serviceClass = keyIterator.next();
            @SuppressWarnings("unchecked")
            Collection<String> values = (Collection<String>) albumsPerService.get(serviceClass);
            Iterator<String> valuesIterator = values.iterator();
            while (valuesIterator.hasNext()) {
                albumName = (String) valuesIterator.next();
                Log.i(TAG, "serviceIteratorStop KEY service: " + serviceClass + " VALUE album: " + albumName);

                stopServiceAutomatically(context, serviceClass, albumName);

//		        boolean running = isMyServiceRunning(context, serviceClass);
//		        if(running) {
//		        	Log.i(TAG, "We should stop service: " + serviceClass + " for album: " + albumName);
//		        	stopServiceAutomatically(context, serviceClass, albumName);
//				}
//				else {
//					Log.i(TAG, "Everything running, skip it in serviceIteratorStop");
//					//continue;				
//					stopServiceAutomatically(context, serviceClass, albumName);
//				}
            }
        }
    }

    private void startServiceAutomatically(Context context, String serviceClass, String albumName) {
        Class<?> cls;
        try {
            cls = Class.forName(AppSettings.FULLY_QUALIFIED_SERVICE_PATH + serviceClass);
            Intent intentService = new Intent(context, cls);
            intentService.putExtra(AppSettings.RESUME_UPLOAD, true);
            intentService.putExtra(AppSettings.ALBUM_NAME, albumName);
            context.startService(intentService);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopServiceAutomatically(Context context, String serviceClass, String albumName) {
        Class<?> cls;
        try {
            cls = Class.forName(AppSettings.FULLY_QUALIFIED_SERVICE_PATH + serviceClass);
            Intent intentService = new Intent(context, cls);
            intentService.putExtra(AppSettings.STOP_UPLOAD, true);
            //context.stopService(intentService);
            //we use startService with AppSettings.STOP_UPLOAD == true because we need to stop threads inside service
            //not only service (if we don't stop threads inside service they will continue with upload)
            context.startService(intentService);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MultiMap<String, String> loadServiceAlbumsCombinationsFromDatabase(Context context) {
        ImageUploadStatus imageUploadStatus = new ImageUploadStatus(context);
        DatabaseManager.initializeInstance(imageUploadStatus);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        MultiMap<String, String> albumNamesFromDb = imageUploadStatus.getServiceAlbumsCombinations(database);
        DatabaseManager.getInstance().closeDatabase();

        return albumNamesFromDb;
    }

    private boolean isMyServiceRunning(Context context, String serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//	private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
//    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//        if (serviceClass.getName().equals(service.service.getClassName())) {
//            return true;
//        }
//    }
//    return false;
//}

}
