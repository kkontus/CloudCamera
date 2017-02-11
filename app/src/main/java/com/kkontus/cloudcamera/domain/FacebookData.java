package com.kkontus.cloudcamera.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kontus on 11.2.2017..
 */

public class FacebookData {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
