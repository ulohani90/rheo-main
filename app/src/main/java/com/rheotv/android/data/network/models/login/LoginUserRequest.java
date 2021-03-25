package com.rheotv.android.data.network.models.login;

import android.net.Uri;

public class LoginUserRequest {
    private String name;
    private String phone;
    private String email;
    private Uri photoUrl;
    private String uid;

    public LoginUserRequest(String name, String phone, String email, Uri photoUrl, String uid) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.photoUrl = photoUrl;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
