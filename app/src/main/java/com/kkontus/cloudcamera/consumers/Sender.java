package com.kkontus.cloudcamera.consumers;

import android.content.Context;

import com.kkontus.cloudcamera.domain.ImageAlbumItem;
import com.kkontus.cloudcamera.interfaces.Consumer;
import com.kkontus.cloudcamera.interfaces.MessageService;

import java.util.List;

public class Sender implements Consumer {

    private MessageService service;

    public Sender(MessageService svc) {
        this.service = svc;
    }

    public Sender() {
    }

    public void setService(MessageService service) {
        this.service = service;
    }

    @Override
    public void processMessages(Context context, String albumName, List<ImageAlbumItem> imageAlbumItems) {
        // do some msg validation, manipulation logic etc
        this.service.sendMessage(context, albumName, imageAlbumItems);
    }

}
