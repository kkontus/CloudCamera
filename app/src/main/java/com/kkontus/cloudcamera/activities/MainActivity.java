package com.kkontus.cloudcamera.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kkontus.cloudcamera.R;
import com.kkontus.cloudcamera.adapters.ListViewCloudChooserAdapter;
import com.kkontus.cloudcamera.adapters.ListViewImageAlbumAdapter;
import com.kkontus.cloudcamera.consumers.SenderSelector;
import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.helpers.AppSettings;
import com.kkontus.cloudcamera.helpers.ImageHelper;
import com.kkontus.cloudcamera.helpers.SharedPreferencesHelper;
import com.kkontus.cloudcamera.interfaces.Consumer;
import com.kkontus.cloudcamera.interfaces.OnDataPass;
import com.kkontus.cloudcamera.interfaces.OnLocationChange;
import com.kkontus.cloudcamera.interfaces.OnNetworkTypeChange;
import com.kkontus.cloudcamera.receivers.CameraReceiver;
import com.kkontus.cloudcamera.receivers.LocationChangeReceiver;
import com.kkontus.cloudcamera.receivers.NetworkTypeChangeReceiver;
import com.kkontus.cloudcamera.repository.DatabaseManager;
import com.kkontus.cloudcamera.repository.ImageUploadStatus;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDataPass, OnLocationChange, OnNetworkTypeChange {
    public static final String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1010;

    ListView listView;
    ListViewImageAlbumAdapter adapter;
    ListView listViewDialogChooser;
    ListViewCloudChooserAdapter cloudChooserAdapter;
    ArrayList<ImageAlbumItem> imageAlbumItems = new ArrayList<ImageAlbumItem>();
    EditText edittextAlbumName;
    Button buttonTakeMorePhoto;
    Button buttonUploadAlbum;
    private AlertDialog dialog;
    private boolean isUploadAllowed = false;

    private CameraReceiver cameraReceiver = new CameraReceiver();
    private LocationChangeReceiver locationChangeReceiver = new LocationChangeReceiver();
    private NetworkTypeChangeReceiver networkTypeChangeReceiver = new NetworkTypeChangeReceiver();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("imagesState", imageAlbumItems);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        //when exiting the app reset taken photos to 0
        SharedPreferencesHelper.setImagesTaken(getApplicationContext(), 0);

        int imagesTaken = SharedPreferencesHelper.getImagesTaken(getApplicationContext());
        Toast.makeText(this, "Reset images taken: " + imagesTaken, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean wifiOnly = SharedPreferencesHelper.getWifiOnly(this);
        if (wifiOnly) {
            if (mWifi != null && !mWifi.isRoaming() && mWifi.isAvailable() && mWifi.isConnected()) {
                isUploadAllowed = true;
            } else {
                isUploadAllowed = false;
            }
        } else {
            if ((mWifi != null && !mWifi.isRoaming() && mWifi.isAvailable() && mWifi.isConnected()) ||
                    (mMobile != null && !mMobile.isRoaming() && mMobile.isAvailable() && mMobile.isConnected())) {
                isUploadAllowed = true;
            } else {
                isUploadAllowed = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraReceiver.setOnDataPassListener(this);
        locationChangeReceiver.setOnLocationChangeListener(this);
        networkTypeChangeReceiver.setOnNetworkTypeChangeListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangeReceiver, new IntentFilter("ImageLocationChange"));


        //for the first time that application starts we want to start camera
        //but after config change, ie. rotation we don't want to start camera automatically,
        //we want to show list of images in portrait/landscape mode and if user wants to take
        //more images it can click Take more photos
        if (savedInstanceState != null) {
            imageAlbumItems = savedInstanceState.getParcelableArrayList("imagesState");
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

        }


        edittextAlbumName = (EditText) findViewById(R.id.edittextAlbumName);
        buttonUploadAlbum = (Button) findViewById(R.id.buttonUploadAlbum);
        buttonTakeMorePhoto = (Button) findViewById(R.id.buttonTakeMorePhoto);
        listView = (ListView) findViewById(R.id.listviewImagesTaken);


        buttonTakeMorePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                realCameraHelper();
            }
        });

        buttonUploadAlbum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Num of items in list: " + imageAlbumItems.size(), Toast.LENGTH_SHORT).show();

                //NEW LOGIC IS IMPLEMENTING, SO THIS IS COMMENTED - STILL TESTING 06.04.2015

//				ImageUploadStatus imageUploadStatus = new ImageUploadStatus(getApplicationContext());
//				DatabaseManager.initializeInstance(imageUploadStatus);
//				SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
//
//				//just for testing, so we don't populate database until sending is implented
//				imageUploadStatus.deleteAllImageUploadStatus(database);
//
//				List<Uri> itemsSelectedForUpload = new ArrayList<Uri>();
//				for (int i = 0; i < imageAlbumItems.size(); i++) {
////					Log.i(TAG, "SELECTED: " + imageAlbumItems.get(i).getIsSelected());
////					Log.i(TAG, "SELECTED NAME: " + imageAlbumItems.get(i).getImageName());
//					if(imageAlbumItems != null && imageAlbumItems.get(i).getIsSelected()) {
//						itemsSelectedForUpload.add(imageAlbumItems.get(i).getImageUri());
//						imageUploadStatus.addImageUploadStatus(database, imageAlbumItems.get(i));
//					}
//				}
//
//				//DatabaseManager.getInstance().closeDatabase();
//				//database = DatabaseManager.getInstance().openDatabase();
//				int itemsSavedToDb = imageUploadStatus.getImageUploadStatusCount(database);
//				DatabaseManager.getInstance().closeDatabase();
//
//				Log.i(TAG, itemsSelectedForUpload.size());
//				Toast.makeText(MainActivity.this, "ITEMS IN DB: " + itemsSavedToDb, Toast.LENGTH_SHORT).show();
//				Toast.makeText(MainActivity.this, "ITEMS TO UPLOAD: " + itemsSelectedForUpload.size(), Toast.LENGTH_SHORT).show();

                //NEW LOGIC IS IMPLEMENTING, SO THIS IS COMMENTED - STILL TESTING 06.04.2015


                // ALERT DIALOG FOR SELECTING TYPE OF ULPOAD SERVICE - START
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.upload_image_using);

                //########## UNCOMMENT CODE BELOW FOR LIST WITHOUT IMAGES - START
//				builder.setItems(AppSettings.SERVICE_TYPES, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//
//						String selectedService = AppSettings.SERVICE_TYPES[which];
//						SenderSelector senderSelector = new SenderSelector(selectedService);
//						Consumer consumer = senderSelector.getSelectedConsumer();
//
//						String albumName = AppSettings.DEFAULT_ALBUM_NAME;
//						if (edittextAlbumName != null && edittextAlbumName.getText() != null && !edittextAlbumName.getText().toString().trim().equals("")) {
//							albumName = edittextAlbumName.getText().toString();
//						}
//
//						// this is used to create no copy of images arraylist because image (Bitmap) is to large to sent via parcelable
//						List<ImageAlbumItem> imageAlbumItemsNoBitmap = new ArrayList<ImageAlbumItem>();
//						for (ImageAlbumItem imageItemOrig : imageAlbumItems)  {
//							imageAlbumItemsNoBitmap.add(imageItemOrig.cloneWithoutBitmap());
//						}
//						consumer.processMessages(getApplicationContext(), albumName, imageAlbumItemsNoBitmap);
//
//						Toast.makeText(MainActivity.this, "Selected type: " + selectedService +  " Album name: " + albumName, Toast.LENGTH_SHORT).show();
//					}
//				});
                //########## UNCOMMENT CODE ABOVE FOR LIST WITHOUT IMAGES - END


                //########## COMMENT CODE BELOW FOR LIST WITHOUT IMAGES - START
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.list_view_dialog_chooser, null);
                builder.setView(convertView);

                listViewDialogChooser = (ListView) convertView.findViewById(R.id.listviewCloud);
                listViewDialogChooser.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedService = AppSettings.SERVICE_TYPES[position];

                        Consumer consumer = null;
                        if (!selectedService.equals(AppSettings.SENDER_SERVICE_LOCAL_FOLDER)) {
                            SenderSelector senderSelector = new SenderSelector(selectedService);
                            consumer = senderSelector.getSelectedConsumer();
                        }

                        String albumName = AppSettings.DEFAULT_ALBUM_NAME;
                        if (edittextAlbumName != null && edittextAlbumName.getText() != null && !edittextAlbumName.getText().toString().trim().equals("")) {
                            albumName = edittextAlbumName.getText().toString();
                        }

                        // this is used to create new copy of images arraylist because image (Bitmap) is to large to sent via parcelable
                        List<ImageAlbumItem> imageAlbumItemsNoBitmap = new ArrayList<ImageAlbumItem>();
                        String serviceName;
                        for (ImageAlbumItem imageItemOrig : imageAlbumItems) {
                            //added for mapping album name and service name to each image that needs to be uploaded - START
                            imageItemOrig.setAlbumName(albumName);
                            serviceName = AppSettings.SERVICE_TYPE_MAPPER.get(selectedService);
                            imageItemOrig.setServiceName(serviceName);
                            //added for mapping album name and service name to each image that needs to be uploaded - END

                            moveImageToAlbum(albumName, imageItemOrig);
                            if (imageItemOrig.getIsSelected()) {
                                imageAlbumItemsNoBitmap.add(imageItemOrig.cloneWithoutBitmap());
                            }
                        }


                        if (!selectedService.equals(AppSettings.SENDER_SERVICE_LOCAL_FOLDER)) {
                            addSelectedImagesToDatabase(imageAlbumItemsNoBitmap);
                            if (isUploadAllowed) {
                                Log.i(TAG, "READY FOR UPLOAD - NEED TO IMPLEMENT");
                                consumer.processMessages(getApplicationContext(), albumName, imageAlbumItemsNoBitmap);
                            } else {
                                Log.i(TAG, "NOT READY FOR UPLOAD - NEED TO IMPLEMENT");
                            }
                        }


                        dialog.dismiss();

                        Toast.makeText(MainActivity.this, "Selected type: " + selectedService + " Album name: " + albumName, Toast.LENGTH_SHORT).show();
                    }
                });
                cloudChooserAdapter = new ListViewCloudChooserAdapter(getBaseContext(), R.layout.list_view_dialog_chooser_item, AppSettings.SERVICE_TYPES);
                listViewDialogChooser.setAdapter(cloudChooserAdapter);
                //########## COMMENT CODE ABOVE FOR LIST WITHOUT IMAGES - START

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.show();
                // ALERT DIALOG FOR SELECTING TYPE OF ULPOAD SERVICE - END
            }
        });


        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri imageUri = imageAlbumItems.get(position).getImageUri();
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra(AppSettings.IMAGE_URI, imageUri.toString());
                startActivity(intent);
            }
        });

        adapter = new ListViewImageAlbumAdapter(this, R.layout.list_view_album_item, imageAlbumItems);
        listView.setAdapter(adapter);
    }

    private void moveImageToAlbum(String albumName, ImageAlbumItem imageItemOrig) {
        File picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File albumFolder = new File(picturesFolder, albumName);
        File sourceFile = new File(imageItemOrig.getImageUri().getPath());

        try {
            if (sourceFile.exists() && !sourceFile.toString().contains(albumFolder.toString())) {

                FileUtils.moveFileToDirectory(sourceFile, albumFolder, true);

                String destionationFilePath = albumFolder + "/" + sourceFile.getName();
                File destionationFile = new File(destionationFilePath);
                imageItemOrig.setImageUri(Uri.parse("file://" + destionationFilePath));
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

                imageItemOrig.setIsMoved(true);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
    }

    private void addSelectedImagesToDatabase(List<ImageAlbumItem> imageAlbumItemsNoBitmap) {
        ImageUploadStatus imageUploadStatus = new ImageUploadStatus(getApplicationContext());
        DatabaseManager.initializeInstance(imageUploadStatus);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        //just for testing, so we don't populate database until sending is implented
        //imageUploadStatus.deleteAllImageUploadStatus(database);

        for (int i = 0; i < imageAlbumItemsNoBitmap.size(); i++) {
            if (imageAlbumItemsNoBitmap != null && imageAlbumItemsNoBitmap.get(i).getIsSelected()) {
                imageUploadStatus.addImageUploadStatus(database, imageAlbumItemsNoBitmap.get(i));
            }
        }

        int itemsSavedToDb = imageUploadStatus.getImageUploadStatusCount(database);
        DatabaseManager.getInstance().closeDatabase();

        Toast.makeText(MainActivity.this, "ITEMS IN DB: " + itemsSavedToDb, Toast.LENGTH_SHORT).show();
    }

    private void realCameraHelper() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            PackageManager pm = getApplicationContext().getPackageManager();
            final ResolveInfo mInfo = pm.resolveActivity(i, 0);

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            //reset taken images on starting camera activity
            SharedPreferencesHelper.setImagesTaken(getApplicationContext(), 0);
            startActivityForResult(intent, AppSettings.CAMERA_REQUEST);
        } catch (Exception e) {
            Log.i(TAG, "Unable to launch camera: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettings.CAMERA_REQUEST) {

            // BELOW - THIS PART OF CODE IS USED TO UNSELECT PREVIOUS IMAGES IN LIST
            // EITHER THAT ARE UPLOADED TO CLOUD OR NOT SELECTED FOR UPLOADING TO CLAUD BEFORE TAKING NEW IMAGE
            for (ImageAlbumItem imageItemOrig : imageAlbumItems) {
                imageItemOrig.setIsSelected(false);
            }
            adapter.notifyDataSetChanged();
            // ABOVE - THIS PART OF CODE IS USED TO UNSELECT PREVIOUS IMAGES IN LIST
            // EITHER THAT ARE UPLOADED TO CLOUD OR NOT SELECTED FOR UPLOADING TO CLAUD BEFORE TAKING NEW IMAGE


            int imagesTaken = SharedPreferencesHelper.getImagesTaken(getApplicationContext());
            if (imagesTaken <= 0 && data != null) {
                //if imagesTaken not updated in CameraBroadcast onReceive (problem with some devices)
                //then take just last one and put it into list
                imagesTaken = 1;
            }

            Cursor cursor = getContentResolver().query(
                    Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            Media.DATA,
                            Media.DATE_ADDED,
                            MediaStore.Images.ImageColumns.ORIENTATION
                    },
                    Media.DATE_ADDED,
                    null,
                    "date_added DESC LIMIT " + imagesTaken);


            Toast.makeText(this, "Number of images to fetch (limit): " + imagesTaken, Toast.LENGTH_SHORT).show();


            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String picturePath = cursor.getString(cursor.getColumnIndex(Media.DATA));
                    String picturePathFile = "file://" + picturePath;
                    Uri selectedImageUri = Uri.parse(picturePathFile);
                    File f = new File(picturePathFile);
                    String imageName = f.getName();
                    Bitmap selectedImageBitmap = null;
                    try {
                        //selectedImageBitmap = ImageHelper.decodeUri(getApplicationContext(), selectedImageUri, 180);
                        selectedImageBitmap = ImageHelper.decodeUriV2(getApplicationContext(), selectedImageUri, 800, 800);
                        selectedImageBitmap = ImageHelper.findImageOrientation(picturePath, selectedImageBitmap);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //this constructor accepts default values, album name and service name is updated later when added to database
                    ImageAlbumItem imageAlbum = new ImageAlbumItem(selectedImageUri, selectedImageBitmap, imageName, true, false, null, null);
                    imageAlbumItems.add(imageAlbum);
                    adapter.notifyDataSetChanged();
                } while (cursor.moveToNext());

                cursor.close();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    realCameraHelper();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

//	private static String getDeviceManufacturerAndName() {
//		String deviceName = android.os.Build.MODEL;
//		String deviceManufacturer = android.os.Build.MANUFACTURER;
//
//		return "Build.MANUFACTURER: " + deviceManufacturer + " Build.MODEL: " + deviceName;
//	}
//
//	private static boolean isExternalStorageReadOnly() {
//		String extStorageState = Environment.getExternalStorageState();
//		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
//			return true;
//		}
//		return false;
//	}
//
//	private static boolean isExternalStorageAvailable() {
//		String extStorageState = Environment.getExternalStorageState();
//		if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
//			return true;
//		}
//		return false;
//	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_dropbox) {
            Toast.makeText(this, "Dropbox", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, DropboxActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_google_plus) {
            Toast.makeText(this, "Google+", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, GooglePlusActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_one_drive) {
            Toast.makeText(this, "OneDrive", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, OneDriveActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_box) {
            Toast.makeText(this, "Box", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, BoxActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_facebook) {
            Toast.makeText(this, "Facebook", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, FacebookActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//	public static Uri resIdToUri(Context context, int resId) {
//	    return Uri.parse(AppSettings.ANDROID_RESOURCE + context.getPackageName() + AppSettings.FORESLASH + resId);
//	}
//
//	public static Uri resIdToUri (Context context,int resID) {
//        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
//                context.getResources().getResourcePackageName(resID) + '/' +
//                context.getResources().getResourceTypeName(resID) + '/' +
//                context.getResources().getResourceEntryName(resID) );
//    }
//
//	private boolean isMyServiceRunning(Class<?> serviceClass) {
//	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//	        if (serviceClass.getName().equals(service.service.getClassName())) {
//	            return true;
//	        }
//	    }
//	    return false;
//	}


    @Override
    public void onDataPass(int count) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Data received from onDataPass interface");
        Log.i(TAG, "Number of images taken: " + count);
    }

    @Override
    public void onLocationChange(String imagePath, String imageUri) {
        // TODO Auto-generated method stub
        adapter.clear();

        Log.i(TAG, "MainActivity onLocationChange: " + imagePath);
        Log.i(TAG, "MainActivity onLocationChange: " + imageUri);
    }

    @Override
    public void onNetworkTypeChange(boolean isConnected) {
        // TODO Auto-generated method stub
        isUploadAllowed = isConnected;

        Log.i(TAG, "MainActivity onNetworkTypeChange: " + isConnected);
    }

}



