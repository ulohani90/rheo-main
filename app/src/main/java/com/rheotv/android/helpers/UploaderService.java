package com.rheotv.android.helpers;

import java.io.File;
import java.io.IOException;

public interface UploaderService {
    void startUpload(File file, String uploadUrl) throws IOException;

    void onProgress(int progress);

    void setUploadProgressListener(UploadProgressListener uploadProgressListener);

    StatusCode getStatusCode();

    enum StatusCode {
        SUCCESS,
        FAILURE,
        UPLOADING
    }

    interface UploadProgressListener {
        void onProgress(int progress);
    }
}
