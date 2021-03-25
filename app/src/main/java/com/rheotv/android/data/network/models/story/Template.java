package com.rheotv.android.data.network.models.story;

public class Template implements Media {

    String id;

    String uri;

    @Override
    public int mimeType() {
        return Media.TEMPLATE;
    }

    @Override
    public String path() {
        return uri;
    }

    @Override
    public String mediaId() {
        return id;
    }

    @Override
    public void setMediaId(String id) {
        this.id = id;
    }
}
