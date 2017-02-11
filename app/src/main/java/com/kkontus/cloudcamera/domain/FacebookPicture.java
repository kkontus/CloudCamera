package com.kkontus.cloudcamera.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kontus on 11.2.2017..
 */

public class FacebookPicture {
    @SerializedName("data")
    private FacebookData data;

    public FacebookData getData() {
        return data;
    }

    public void setData(FacebookData data) {
        this.data = data;
    }
}
