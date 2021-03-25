package com.rheotv.android.data.network.models.story;

public interface Media {

    int VIDEO = 0;
    int IMAGE = 1;
    int TEMPLATE = 2;
    int ADD_MORE = 3;

    String mediaId();

    void setMediaId(String id);

    int mimeType();

    String path();

    default String getTypeForMimeType(int mimeType) {
        switch (mimeType) {
            case IMAGE:
                return "story";

            case VIDEO:
                return "video";

            default:
                return null;
        }
    }

}
