package com.kkontus.cloudcamera.consumers;

import com.kkontus.cloudcamera.helpers.AppSettings;
import com.kkontus.cloudcamera.injectors.BoxSenderServiceInjector;
import com.kkontus.cloudcamera.injectors.DropboxSenderServiceInjector;
import com.kkontus.cloudcamera.injectors.FacebookSenderServiceInjector;
import com.kkontus.cloudcamera.injectors.GoogleDriveSenderServiceInjector;
import com.kkontus.cloudcamera.injectors.LocalFolderSenderServiceInjector;
import com.kkontus.cloudcamera.injectors.OneDriveSenderServiceInjector;
import com.kkontus.cloudcamera.interfaces.Consumer;
import com.kkontus.cloudcamera.interfaces.MessageServiceInjector;

public class SenderSelector {

    private String selectedService = null;

    public SenderSelector(String selectedService) {
        super();
        this.selectedService = selectedService;
    }

    public Consumer getSelectedConsumer() {

        MessageServiceInjector injector = null;
        Consumer consumer = null;

        switch (this.selectedService) {
            case AppSettings.SENDER_SERVICE_DROPBOX:
                injector = new DropboxSenderServiceInjector();
                break;
            case AppSettings.SENDER_SERVICE_LOCAL_FOLDER:
                injector = new LocalFolderSenderServiceInjector();
                break;
            case AppSettings.SENDER_SERVICE_BOX:
                injector = new BoxSenderServiceInjector();
                break;
            case AppSettings.SENDER_SERVICE_GOOGLE_DRIVE:
                injector = new GoogleDriveSenderServiceInjector();
                break;
            case AppSettings.SENDER_SERVICE_ONE_DRIVE:
                injector = new OneDriveSenderServiceInjector();
                break;
            case AppSettings.SENDER_SERVICE_FACEBOOK:
                injector = new FacebookSenderServiceInjector();
                break;
            default:
                injector = new DropboxSenderServiceInjector();
        }

        consumer = injector.getConsumer();

        return consumer;
    }

}
