package com.kkontus.cloudcamera.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.helpers.AppConnectionSettings;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import java.util.ArrayList;


public class ImageUploadStatus extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = AppConnectionSettings.DATABASE_VERSION;
    private static final String DATABASE_NAME = AppConnectionSettings.DATABASE_NAME;

    private static final String TABLE_IMAGE_UPLOAD_STATUS = "ImageUploadStatus";
    private static final String KEY_ID = "_id";
    private static final String KEY_IMAGE_NAME = "image_name";
    private static final String KEY_IMAGE_URI = "image_uri";
    private static final String KEY_IS_SELECTED = "is_selected";
    private static final String KEY_IS_MOVED = "is_moved";
    private static final String KEY_ALBUM_NAME = "album_name";
    private static final String KEY_SERVICE_NAME = "service_name";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + TABLE_IMAGE_UPLOAD_STATUS
            + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_IMAGE_NAME + " VARCHAR(50) DEFAULT NULL,"
            + KEY_IMAGE_URI + " VARCHAR(100) DEFAULT NULL,"
            + KEY_IS_SELECTED + " INTEGER,"
            + KEY_IS_MOVED + " INTEGER,"
            + KEY_ALBUM_NAME + " VARCHAR(100) DEFAULT NULL,"
            + KEY_SERVICE_NAME + " VARCHAR(100) DEFAULT NULL"
            + ");";

    private static final String SQL_DELETE_ENTRIES = "DELETE FROM " + TABLE_IMAGE_UPLOAD_STATUS + ";";

    public ImageUploadStatus(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy
        // is to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addImageUploadStatus(SQLiteDatabase database, ImageAlbumItem imageAlbumItem) {

        //SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IMAGE_NAME, imageAlbumItem.getImageName());
        values.put(KEY_IMAGE_URI, imageAlbumItem.getImageUri().toString());
        values.put(KEY_IS_SELECTED, imageAlbumItem.getIsSelected());
        values.put(KEY_IS_MOVED, imageAlbumItem.getIsMoved());
        values.put(KEY_ALBUM_NAME, imageAlbumItem.getAlbumName());
        values.put(KEY_SERVICE_NAME, imageAlbumItem.getServiceName());

        database.insert(TABLE_IMAGE_UPLOAD_STATUS, null, values);

        //DatabaseManager.getInstance().closeDatabase();
    }

    public ArrayList<ImageAlbumItem> getAllImageUploadStatusItems(SQLiteDatabase database, String serviceCondition, String albumCondition) {

        //SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String countQuery;
        if (serviceCondition == null || albumCondition == null) {
            countQuery = "SELECT * FROM " + TABLE_IMAGE_UPLOAD_STATUS;
        } else {
            countQuery = "SELECT * FROM " + TABLE_IMAGE_UPLOAD_STATUS + " WHERE " + KEY_SERVICE_NAME + "='" + serviceCondition + "' AND " + KEY_ALBUM_NAME + "='" + albumCondition + "'";
        }
        Cursor cursor = database.rawQuery(countQuery, null);

        ArrayList<ImageAlbumItem> imageAlbumItems = new ArrayList<ImageAlbumItem>();
        while (cursor.moveToNext()) {
            String imageUriString = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URI));
            Uri imageUri = Uri.parse(imageUriString);

            Bitmap image = null;
            String imageName = cursor.getString(cursor.getColumnIndex(KEY_IMAGE_NAME));

            String isSelectedString = cursor.getString(cursor.getColumnIndex(KEY_IS_SELECTED));
            boolean isSelected = Boolean.valueOf(isSelectedString);

            String isMovedString = cursor.getString(cursor.getColumnIndex(KEY_IS_MOVED));
            boolean isMoved = Boolean.valueOf(isMovedString);

            String albumName = cursor.getString(cursor.getColumnIndex(KEY_ALBUM_NAME));
            String serviceName = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_NAME));

            ImageAlbumItem imageAlbumItem = new ImageAlbumItem(imageUri, image, imageName, isSelected, isMoved, albumName, serviceName);

            imageAlbumItems.add(imageAlbumItem);
        }

        cursor.close();
        //DatabaseManager.getInstance().closeDatabase();

        return imageAlbumItems;
    }

    public MultiMap<String, String> getServiceAlbumsCombinations(SQLiteDatabase database) {

        //SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String countQuery = "SELECT DISTINCT " + KEY_SERVICE_NAME + ", " + KEY_ALBUM_NAME + " FROM " + TABLE_IMAGE_UPLOAD_STATUS;
        Cursor cursor = database.rawQuery(countQuery, null);

        MultiMap<String, String> multiMap = new MultiValueMap<String, String>();
        while (cursor.moveToNext()) {
            String serviceName = cursor.getString(cursor.getColumnIndex(KEY_SERVICE_NAME));
            String albumName = cursor.getString(cursor.getColumnIndex(KEY_ALBUM_NAME));

            multiMap.put(serviceName, albumName);
        }

        cursor.close();
        //DatabaseManager.getInstance().closeDatabase();

        return multiMap;
    }

    public int getImageUploadStatusCount(SQLiteDatabase database) {

        //SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String countQuery = "SELECT * FROM " + TABLE_IMAGE_UPLOAD_STATUS;
        Cursor cursor = database.rawQuery(countQuery, null);

        int numOfRecords = cursor.getCount();

        cursor.close();

        //DatabaseManager.getInstance().closeDatabase();

        return numOfRecords;
    }

    public void deleteAllImageUploadStatus(SQLiteDatabase database) {
        //String deleteQuery = "DELETE FROM " + TABLE_IMAGE_UPLOAD_STATUS;
        String deleteQuery = SQL_DELETE_ENTRIES;
        database.execSQL(deleteQuery);
    }

    public void deleteImageUploadStatusByUri(SQLiteDatabase database, ImageAlbumItem imageAlbumItem) {
        database.delete(TABLE_IMAGE_UPLOAD_STATUS, KEY_IMAGE_URI + " = ?", new String[]{String.valueOf(imageAlbumItem.getImageUri())});
    }

    public void deleteImageUploadStatusByUri(SQLiteDatabase database, Uri uri) {
        database.delete(TABLE_IMAGE_UPLOAD_STATUS, KEY_IMAGE_URI + " = ?", new String[]{String.valueOf(uri)});
    }

    public void deleteImageUploadStatusByName(SQLiteDatabase database, ImageAlbumItem imageAlbumItem) {
        database.delete(TABLE_IMAGE_UPLOAD_STATUS, KEY_IMAGE_NAME + " = ?", new String[]{String.valueOf(imageAlbumItem.getImageName())});
    }

    public void deleteImageUploadStatusByName(SQLiteDatabase database, String name) {
        database.delete(TABLE_IMAGE_UPLOAD_STATUS, KEY_IMAGE_NAME + " = ?", new String[]{name});
    }
}


