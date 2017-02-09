package com.kkontus.cloudcamera.injectors;

import com.kkontus.cloudcamera.consumers.Sender;
import com.kkontus.cloudcamera.interfaces.Consumer;
import com.kkontus.cloudcamera.interfaces.MessageServiceInjector;
import com.kkontus.cloudcamera.services.LocalFolderSenderService;

public class LocalFolderSenderServiceInjector implements MessageServiceInjector {

    @Override
    public Consumer getConsumer() {
        Sender app = new Sender();
        app.setService(new LocalFolderSenderService());
        return app;
    }

}
