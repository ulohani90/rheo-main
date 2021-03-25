package com.rheotv.android.helpers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class S3FileUploader implements UploaderService {
    private UploadProgressListener mUploadProgressListener;
    private StatusCode mStatusCode = StatusCode.UPLOADING;
    private String TAG = getClass().getSimpleName();

    @Override
    public void startUpload(File videoFileToUpload, String urlToUpload) throws IOException {
        Log.d(getClass().getSimpleName(), "uploadVideoHTTP: upload video http");

        try {
            Log.d(getClass().getSimpleName(), "uploadVideoHTTP_ upload video http try");
            final URL url = new URL(urlToUpload);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                OutputStream outputStream = connection.getOutputStream();
                InputStream is = new FileInputStream(videoFileToUpload);
                long totalBytes = is.available();
                int packetSize = 1024 * 100;
                byte[] byteArray = new byte[packetSize];
                int readResult = is.read(byteArray);
                int counter = 1;
                long percentageCompletion;
                int total = 0;
                while (readResult > 0) {
                    Log.d(getClass().getSimpleName(), "upload in progress");
                    try {
                        outputStream.write(byteArray);
                        percentageCompletion = (counter * packetSize * 100L) / totalBytes;
                        Long longPer = new Long(percentageCompletion);
                        int percentageConverted = longPer.intValue();
                        onProgress(percentageConverted);
                    } catch (OutOfMemoryError | ArithmeticException e) {
                        e.printStackTrace();
                        return;
                    }

                    readResult = is.read(byteArray);
                    total += counter;
                    counter++;
                }
                outputStream.close();
                String responseMSG = connection.getResponseMessage();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "url:" + url);
                if (responseCode == 200) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                        rd.close();
                    } catch (IOException ioex) {
                        Log.d(TAG, "fail_on_ioex......." + responseCode + " and " + responseMSG + " and " + connection.getPermission());
                        mStatusCode = StatusCode.FAILURE;
                    }
                    Log.d(TAG, "der " + sb.toString());
                    Log.d(TAG, "Completed......." + responseCode + " and " + responseMSG);
                    mStatusCode = StatusCode.SUCCESS;
                } else {
                    Log.d(TAG, "fail_on_responseCode......." + responseCode + " and " + responseMSG + " and " + connection.getPermission());
                    mStatusCode = StatusCode.FAILURE;
                }
            } catch (Exception e) {
                Log.d(TAG, "failed.......");
                mStatusCode = StatusCode.FAILURE;
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.d(TAG, "failed.......");
            mStatusCode = StatusCode.FAILURE;
            e.printStackTrace();
        }
    }

    @Override
    public void onProgress(int progress) {
        if (mUploadProgressListener != null) {
            mUploadProgressListener.onProgress(progress);
        }
    }

    @Override
    public void setUploadProgressListener(UploadProgressListener uploadProgressListener) {
        mUploadProgressListener = uploadProgressListener;
    }

    @Override
    public StatusCode getStatusCode() {
        return mStatusCode;
    }
}

//2020-11-26 12:14:26.655 23464-23869/com.rheotv.android D/utl: https://rheovideos.blob.core.windows.net/rheovideos/rheoclips/20/2020-11-26_12:14:25.752599/original.mp4?ss=b&srt=o&se=2020-11-26T12%3A24%3A25Z&sp=wc&sig=Knp9NeURdmY66%2BmltmYxbaXCZibFPUdER58gmtJcWPU%3D&sv=2019-02-02
//2020-11-26 12:14:26.656 23464-23869/com.rheotv.android D/S3FileUploader: fail_on_responseCode.......400 and An HTTP header that's mandatory for this request is not specified.
//2020-11-26 12:14:26.656 23464-23464/com.rheotv.android I/UploadVideoTask: Reached onPost

//https://s3.ap-south-1.amazonaws.com/rheovideos/live/chat/4a44e290-e5d3-4c8b-9673-31e6357e6beb/image/20/2020-11-26_19:31:46.932881.jpeg?x-amz-credential=AKIAJT6QN7GY3AWP4U2Q/20201126/ap-south-1/s3/aws4_request?policy=eyJleHBpcmF0aW9uIjogIjIwMjAtMTEtMjZUMTU6MDE6NDZaIiwgImNvbmRpdGlvbnMiOiBbeyJidWNrZXQiOiAicmhlb3ZpZGVvcyJ9LCB7ImtleSI6ICJsaXZlL2NoYXQvNGE0NGUyOTAtZTVkMy00YzhiLTk2NzMtMzFlNjM1N2U2YmViL2ltYWdlLzIwLzIwMjAtMTEtMjZfMTk6MzE6NDYuOTMyODgxLmpwZWcifSwgeyJ4LWFtei1hbGdvcml0aG0iOiAiQVdTNC1ITUFDLVNIQTI1NiJ9LCB7IngtYW16LWNyZWRlbnRpYWwiOiAiQUtJQUpUNlFON0dZM0FXUDRVMlEvMjAyMDExMjYvYXAtc291dGgtMS9zMy9hd3M0X3JlcXVlc3QifSwgeyJ4LWFtei1kYXRlIjogIjIwMjAxMTI2VDE0MDE0NloifV19?x-amz-algorithm=AWS4-HMAC-SHA256?x-amz-date=20201126T140146Z?x-amz-signature=6e91829d7a915d891f95b403db3e83a7a4ffbc5af7d8b3d7c35b1714c72b119e
//        2020-11-26 19:31:49.094 19504-20107/com.rheotv.android D/S3FileUploader: fail_on_responseCode.......405 and Method Not Allowed and