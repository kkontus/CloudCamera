package com.kkontus.cloudcamera.interfaces;

import android.content.Context;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;

import java.util.List;

public interface Consumer {
    public void processMessages(Context context, String albumName, List<ImageAlbumItem> imageAlbumItems);
}
