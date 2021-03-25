package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StreamEndedResponse {
    @SerializedName("is_stream_ended")
    @Expose
    private boolean streamEnded;

    public boolean isStreamEnded() {
        return streamEnded;
    }
}
