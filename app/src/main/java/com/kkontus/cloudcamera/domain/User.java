package com.kkontus.cloudcamera.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kontus on 11.2.2017..
 */

public class User {
    @SerializedName("id")
    private String facebookId;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("picture")
    private FacebookPicture facebookPicture;

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FacebookPicture getFacebookPicture() {
        return facebookPicture;
    }

    public void setFacebookPicture(FacebookPicture facebookPicture) {
        this.facebookPicture = facebookPicture;
    }
}
