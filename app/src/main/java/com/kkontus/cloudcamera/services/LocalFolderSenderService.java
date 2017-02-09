package com.kkontus.cloudcamera.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.interfaces.MessageService;

import java.util.List;

/**
 * Created by Kontus on 9.2.2017..
 */

public class LocalFolderSenderService extends IntentService implements MessageService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LocalFolderSenderService(String name) {
        super(name);
    }

    public LocalFolderSenderService() {
        super("LocalFolderSenderService");
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void sendMessage(Context context, String albumName, List<ImageAlbumItem> imageAlbumItems) {

    }
}
