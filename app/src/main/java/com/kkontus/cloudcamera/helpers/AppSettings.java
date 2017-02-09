package com.kkontus.cloudcamera.helpers;

import java.util.HashMap;
import java.util.Map;

public class AppSettings {

    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";

    public static final String PREFS_NAME = "SmartCameraPrefs";
    public static final String DROPBOX_PREFS_NAME = "DropboxPrefs";
    public static final String GOOGLE_PREFS_NAME = "GooglePrefs";
    public static final String ONE_DRIVE_PREFS_NAME = "OneDrivePrefs";
    public static final String FACEBOOK_PREFS_NAME = "Facebook";

    public static final String FACEBOOK_PROFILE_DATA = "facebookProfileData";

    public static final String DROPBOX_MAIN_FOLDER = "/Photos/";

    public static final String DEFAULT_ALBUM_NAME = "SmartCam";

    public static final int CHECK_UPLOAD_STATUS_GOOGLE = 3000;
    public static final int CHECK_UPLOAD_PROGRESS = 1000;

    public static final int CAMERA_REQUEST = 1;

    public static final String IMAGE_URI = "imageUri";

    public static final String ALBUM_NAME = "albumName";
    public static final String IMAGE_ALBUM_ITEMS = "imageAlbumItems";
    public static final String RESUME_UPLOAD = "resumeUpload";
    public static final String STOP_UPLOAD = "stopUpload";

    public static final String SENDER_SERVICE_LOCAL_FOLDER = "LocalFolder";
    public static final String SENDER_SERVICE_BOX = "Box";
    public static final String SENDER_SERVICE_DROPBOX = "Dropbox";
    public static final String SENDER_SERVICE_GOOGLE_DRIVE = "GoogleDrive";
    public static final String SENDER_SERVICE_ONE_DRIVE = "OneDrive";
    public static final String SENDER_SERVICE_FACEBOOK = "Facebook";
    public static final String[] SERVICE_TYPES = {
            SENDER_SERVICE_LOCAL_FOLDER,
            SENDER_SERVICE_BOX,
            SENDER_SERVICE_DROPBOX,
            SENDER_SERVICE_GOOGLE_DRIVE,
            SENDER_SERVICE_ONE_DRIVE,
            SENDER_SERVICE_FACEBOOK
    };

//	public static final String[] SERVICE_TYPES_CLASSES = {
//		//we don't have to use LocalFolderSenderService service here
//		"BoxSenderService",
//		"DropboxSenderService", 
//		"GoogleDriveSenderService",
//		"OneDriveSenderService",
//		"FacebookSenderService"
//	};	

    public static final String FULLY_QUALIFIED_SERVICE_PATH = "com.android.services.";

    public static Map<String, String> SERVICE_TYPE_MAPPER = new HashMap<String, String>() {{
        put(SENDER_SERVICE_BOX, "BoxSenderService");
        put(SENDER_SERVICE_DROPBOX, "DropboxSenderService");
        put(SENDER_SERVICE_GOOGLE_DRIVE, "GoogleDriveSenderService");
        put(SENDER_SERVICE_ONE_DRIVE, "OneDriveSenderService");
        put(SENDER_SERVICE_FACEBOOK, "FacebookSenderService");
    }};

}


