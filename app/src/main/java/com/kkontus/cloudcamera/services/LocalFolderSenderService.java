package com.kkontus.cloudcamera.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.helpers.AppSettings;
import com.kkontus.cloudcamera.interfaces.MessageService;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kontus on 9.2.2017..
 */
public class LocalFolderSenderService extends IntentService implements MessageService {

    private static final String TAG = "LocalFolderSenderService";
    private String albumName;
    ArrayList<ImageAlbumItem> imageAlbumItems;
    private File picturesFolder;
    private File albumFolder;


    public LocalFolderSenderService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public LocalFolderSenderService() {
        super("LocalFolderSenderService");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void sendMessage(Context context, String albumName, List<ImageAlbumItem> imageAlbumItems) {
        // logic to send SMS
        System.out.println("LocalFolder post sent with Message=" + albumName);

        Intent intent = new Intent(context, LocalFolderSenderService.class);
        intent.putExtra(AppSettings.ALBUM_NAME, albumName);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(AppSettings.IMAGE_ALBUM_ITEMS, (ArrayList<? extends Parcelable>) imageAlbumItems);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub

        Bundle bundle = intent.getExtras();
        this.imageAlbumItems = bundle.getParcelableArrayList(AppSettings.IMAGE_ALBUM_ITEMS);
        this.albumName = intent.getStringExtra(AppSettings.ALBUM_NAME);

        this.picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        this.albumFolder = new File(picturesFolder, albumName);

        //we don't need this because FileUtils.moveFileToDirectory(sourceFile, albumFolder, true); with true parameter creates folder if it doesn't exist
//		if (!albumFolder.exists()) {
//			albumFolder.mkdir();
//		}

        for (int i = 0; i < imageAlbumItems.size(); i++) {
            Runnable r = new UploadLocalThread(imageAlbumItems.get(i));
            new Thread(r).start();
        }
    }

    public class UploadLocalThread implements Runnable {
        private Uri imageUri;

        public UploadLocalThread(ImageAlbumItem imageAlbumItem) {
            // since we need only Uri we won't create other properties,
            // but for each case we pass entire object to UploadThread constructor
            this.imageUri = imageAlbumItem.getImageUri();
        }

        public void run() {
            File sourceFile = new File(imageUri.getPath());

            boolean safeToDelete = false;
            try {
                FileUtils.moveFileToDirectory(sourceFile, albumFolder, true);
                safeToDelete = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                safeToDelete = false;
                e.printStackTrace();
            } finally {
                //delete source file at the end if successful
                if (safeToDelete) {
                    sourceFile.delete();

                    String destionationFilePath = albumFolder + "/" + sourceFile.getName();
                    File destionationFile = new File(destionationFilePath);

                    try {
                        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "='" + sourceFile.getPath() + "'", null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, destionationFile.getPath());
                    values.put(MediaStore.Images.Media.DATE_TAKEN, destionationFile.lastModified());

                    Uri mImageCaptureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    // to notify change
                    //getContentResolver().notifyChange(Uri.parse("file://" + destionationFile.getPath()), null);
                    getContentResolver().notifyChange(Uri.fromFile(destionationFile), null);

                    Intent intent = new Intent("ImageLocationChange");
                    intent.putExtra("uri", Uri.fromFile(destionationFile).toString());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }
        }

    }

}


