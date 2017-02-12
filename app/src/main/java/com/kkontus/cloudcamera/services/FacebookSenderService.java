package com.kkontus.cloudcamera.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.helpers.AppSettings;
import com.kkontus.cloudcamera.interfaces.MessageService;
import com.kkontus.cloudcamera.repository.DatabaseManager;
import com.kkontus.cloudcamera.repository.ImageUploadStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FacebookSenderService extends IntentService implements MessageService {
    private static final String TAG = "FacebookSenderService";
    private String albumName;
    private String mFolderId = null;
    private String pathForUpload = null;
    AccessToken accessToken = null;
    ArrayList<ImageAlbumItem> imageAlbumItems;
    private boolean resumeUpload = false;
    private boolean stopUpload = false;
    public static volatile boolean stopUploadThread = false;
    //public ExecutorService executor = Executors.newSingleThreadExecutor();
    public ExecutorService executor = Executors.newFixedThreadPool(1);

    public FacebookSenderService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public FacebookSenderService() {
        super("FacebookSenderService");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void sendMessage(Context context, String albumName, List<ImageAlbumItem> imageAlbumItems) {
        // logic to send email
        System.out.println("Facebook post sent with Message=" + albumName);

        Intent intent = new Intent(context, FacebookSenderService.class);
        intent.putExtra(AppSettings.ALBUM_NAME, albumName);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(AppSettings.IMAGE_ALBUM_ITEMS, (ArrayList<? extends Parcelable>) imageAlbumItems);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        if (isLoggedIn()) {
            Log.i(TAG, "Signed in to Facebook");

            AccessToken.refreshCurrentAccessTokenAsync();
            uploadImage();
        } else {
            Log.i(TAG, "Not signed in to Facebook");
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		// TODO Auto-generated method stub
//		return super.onStartCommand(intent, flags, startId);
//	}

    private ArrayList<ImageAlbumItem> loadAllPendingUploadsFromDatabase(String albumCondition) {
        ImageUploadStatus imageUploadStatus = new ImageUploadStatus(getApplicationContext());
        DatabaseManager.initializeInstance(imageUploadStatus);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String serviceCondition = getClass().getSimpleName();
        ArrayList<ImageAlbumItem> imageAlbumItemsFromDb = imageUploadStatus.getAllImageUploadStatusItems(database, serviceCondition, albumCondition);
        DatabaseManager.getInstance().closeDatabase();

        return imageAlbumItemsFromDb;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub

        Bundle bundle = intent.getExtras();
        //this.imageAlbumItems = bundle.getParcelableArrayList(AppSettings.IMAGE_ALBUM_ITEMS);
        //this.albumName = intent.getStringExtra(AppSettings.ALBUM_NAME);
        this.resumeUpload = intent.getBooleanExtra(AppSettings.RESUME_UPLOAD, false);
        this.stopUpload = intent.getBooleanExtra(AppSettings.STOP_UPLOAD, false);

        if (stopUpload == false) {
            stopUploadThread = stopUpload;
            if (resumeUpload == true) {
                Log.i(TAG, "Take album name from database");

                this.imageAlbumItems = loadAllPendingUploadsFromDatabase(albumName);
                this.albumName = intent.getStringExtra(AppSettings.ALBUM_NAME);
            } else {
                Log.i(TAG, "Take album name from intent");

                this.imageAlbumItems = bundle.getParcelableArrayList(AppSettings.IMAGE_ALBUM_ITEMS);
                this.albumName = intent.getStringExtra(AppSettings.ALBUM_NAME);
            }
        } else {
            System.out.println("Should stop service immediately");
            stopUploadThread = stopUpload; //always true
            executor.shutdownNow();
            stopSelf();
            return;
        }

        Log.i(TAG, "ALBUM NAME: " + albumName);
        Log.i(TAG, "RESUME UPLOAD: " + resumeUpload);
        Log.i(TAG, "STOP UPLOAD: " + stopUpload);
        Log.i(TAG, "imageAlbumItems SIZE: " + imageAlbumItems.size());
    }

    public boolean isLoggedIn() {
        accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void uploadImage() {
        getAlbumMetadata(accessToken);
    }

    private void getAlbumMetadata(AccessToken accessToken) {
        Bundle parametersAlbums = new Bundle();
        parametersAlbums.putString("fields", "id, name");

        GraphRequest graphRequestAlbums = new GraphRequest(accessToken,
                "/" + accessToken.getUserId() + "/albums",
                parametersAlbums,
                HttpMethod.GET,
                new GraphRequest.Callback() {

                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        System.out.println("graphRequestAlbums onCompleted: " + graphResponse);

                        final JSONObject jsonObject;
                        final JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(graphResponse.getRawResponse());
                            jsonArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String id = obj.getString("id");
                                String name = obj.getString("name");

                                if (name.equals(albumName)) {
                                    System.out.println("Found: " + id + " " + name);
                                    mFolderId = id;
                                }

                                //System.out.println(id + " " + name);
                            }

                            getPhotosMetadata();

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
        graphRequestAlbums.executeAsync();
    }

    private void getPhotosMetadata() {
        if (mFolderId != null) {
            pathForUpload = mFolderId + "/photos";

            if (imageAlbumItems == null) {
                return;
            }

            for (int i = 0; i < imageAlbumItems.size(); i++) {
                Runnable r = new UploadFacebookThread(imageAlbumItems.get(i));
                //new Thread(r).start();
                executor.execute(r);
            }
            executor.shutdown();

        } else {
            pathForUpload = "/me/albums";

            getNewAlbumMetadata();
        }

        Log.i(TAG, "pathForUpload: " + pathForUpload);
    }

    private void getNewAlbumMetadata() {
        Bundle bundle = new Bundle();
        bundle.putString("name", albumName);

        GraphRequest graphRequestPhotos = new GraphRequest(AccessToken.getCurrentAccessToken(),
                pathForUpload,
                bundle,
                HttpMethod.POST,
                new GraphRequest.Callback() {

                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        System.out.println("graphRequestNewAlbumPhotos onCompleted: " + graphResponse);

                        try {
                            JSONObject jsonObject = new JSONObject(graphResponse.getRawResponse());
                            String newFolderId = jsonObject.getString("id");

                            pathForUpload = newFolderId + "/photos";

                            for (int i = 0; i < imageAlbumItems.size(); i++) {
                                Runnable r = new UploadFacebookThread(imageAlbumItems.get(i));
                                //new Thread(r).start();
                                executor.execute(r);
                            }
                            executor.shutdown();

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (Looper.getMainLooper() == Looper.myLooper()) {
                            Log.i(TAG, "Inside: We are on main thread");
                        } else {
                            Log.i(TAG, "Inside: We are off main thread");
                        }
                    }
                }
        );
        graphRequestPhotos.executeAsync();

        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.i(TAG, "Outside: We are on main thread");
        } else {
            Log.i(TAG, "Outside: We are off main thread");
        }
    }

    public class UploadFacebookThread implements Runnable {
        private Uri imageUri;

        public UploadFacebookThread(ImageAlbumItem imageAlbumItem) {
            // since we need only Uri we won't create other properties,
            // but for each case we pass entire object to UploadThread constructor
            this.imageUri = imageAlbumItem.getImageUri();
        }

        public byte[] convertToByteArray(Uri data) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(data.getPath()));
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            byte[] bbytes = baos.toByteArray();
            return bbytes;
        }

        public void run() {
            File file = new File(imageUri.getPath());
            String imageName = file.getName();

            byte[] byteArray = convertToByteArray(imageUri);

            Bundle bundle = new Bundle();
            bundle.putByteArray("object_attachment", byteArray);// object attachment must be either byteArray or bitmap image
            bundle.putString("message", imageName);
            bundle.putString("name", albumName);

            GraphRequest graphRequestPhotos = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    //"{page_id}/photos",
                    //"{album_id}/photos",
                    pathForUpload,
                    bundle,
                    HttpMethod.POST,
                    new GraphRequest.Callback() {

                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            System.out.println("graphRequestPhotos onCompleted: " + graphResponse);

                            deleteImageFromDBonSuccess(getApplicationContext(), imageUri);

                            if (Looper.getMainLooper() == Looper.myLooper()) {
                                Log.i(TAG, "Inside: We are on main thread");
                            } else {
                                Log.i(TAG, "Inside: We are off main thread");
                            }
                        }
                    }
            );
            graphRequestPhotos.executeAndWait();

            if (Looper.getMainLooper() == Looper.myLooper()) {
                Log.i(TAG, "Outside: We are on main thread");
            } else {
                Log.i(TAG, "Outside: We are off main thread");
            }
        }

        private void deleteImageFromDBonSuccess(Context context, Uri uri) {
            ImageUploadStatus imageUploadStatus = new ImageUploadStatus(context);
            DatabaseManager.initializeInstance(imageUploadStatus);
            SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

            imageUploadStatus.deleteImageUploadStatusByUri(database, uri);
            DatabaseManager.getInstance().closeDatabase();
        }
    }

}