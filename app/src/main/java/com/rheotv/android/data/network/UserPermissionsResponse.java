package com.rheotv.android.data.network;

import com.google.gson.annotations.SerializedName;

public class UserPermissionsResponse {
    @SerializedName("toggle_video_call_feature")
    boolean canEnableVideoCalling;

    public boolean isCanEnableVideoCalling() {
        return canEnableVideoCalling;
    }
}
