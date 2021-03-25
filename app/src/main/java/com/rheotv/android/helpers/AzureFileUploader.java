package com.rheotv.android.helpers;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AzureFileUploader implements UploaderService {

    private static final int MAX_BLOCK_SIZE = 4 * 1024 * 1024;
    private static final int DEFAULT_BLOCK_SIZE = 1024 * 1024;
    private static final String TAG = "AzureFileUploader";
    private UploadProgressListener mUploadProgressListener;
    private StatusCode mStatusCode = StatusCode.UPLOADING;

    @Override
    public void startUpload(File file, String uploadUrl) throws IOException {
        if (file == null || file.length() == 0)
            throw new IllegalStateException("Invalid File Please select valid file");
        try {
            InputStream inStream = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(inStream);
            List<String> blockIds = new ArrayList<String>();

            int counter = 1;
            long totalBytes = file.length();
            long blockSizeMB = totalBytes / (1024 * 1024);
            int blockSize = DEFAULT_BLOCK_SIZE;
            if (blockSizeMB <= 50000) {
            } else if (blockSizeMB <= 100000) {
                blockSize = DEFAULT_BLOCK_SIZE * 2;
            } else if (blockSizeMB <= 150000) {
                blockSize = DEFAULT_BLOCK_SIZE * 3;
            } else {
                blockSize = MAX_BLOCK_SIZE;
            }
            print("Start time ---> " + System.currentTimeMillis());
            while (bis.available() > 0) {
                int bufferLength = Math.min(bis.available(), blockSize);

                byte[] buffer = new byte[bufferLength];
                bis.read(buffer, 0, buffer.length);
                String bid = String.format(Locale.getDefault(), "Block-%05d", counter++);
                String blockId = Base64.encodeToString(bid.getBytes(), Base64.DEFAULT);
                uploadBlock(uploadUrl, buffer, blockId);
                long percentageCompletion = (totalBytes - bis.available()) * 100L / totalBytes;
                blockIds.add(blockId);
                onProgress((int) percentageCompletion);
            }
            commitBlockList(uploadUrl, blockIds);
            print("Finish time ---> " + System.currentTimeMillis());
            inStream.close();
            bis.close();
            Log.d("AzureFileUploader", "Completed.......");
            mStatusCode = StatusCode.SUCCESS;
        } catch (Exception e) {
            mStatusCode = StatusCode.FAILURE;
            Log.d("AzureFileUploader", "ERRORRRR.......");
            throw e;
        }
    }

    private void uploadBlock(String baseUri, byte[] blockContents, String blockId) throws IOException {

        OkHttpClient client = new OkHttpClient();

        MediaType mime = MediaType.parse("");
        RequestBody body = RequestBody.create(mime, blockContents);

        String uploadBlockUri = baseUri + "&comp=block&blockId=" + blockId;

        Request request = new Request.Builder()
                .url(uploadBlockUri)
                .put(body)
                .addHeader("x-ms-version", "2015-12-11")
                .addHeader("x-ms-blob-type", "BlockBlob")
                .build();

        Response response = client.newCall(request).execute();
        print(response.toString());
        if (!response.isSuccessful()) {
            if (response.code() >= 400 && response.code() < 500) {
                throw new IOException("The file name length is different. ");
            }
        }
    }

    private void commitBlockList(String baseUri, List<String> blockIds) throws IOException {

        OkHttpClient client = new OkHttpClient();

        StringBuilder blockIdsPayload = new StringBuilder();
        blockIdsPayload.append("<?xml version='1.0' ?><BlockList>");
        for (String blockId : blockIds) {
            blockIdsPayload.append("<Latest>").append(blockId).append("</Latest>");
        }
        blockIdsPayload.append("</BlockList>");

        String putBlockListUrl = baseUri + "&comp=blocklist";
        MediaType contentType = MediaType.parse("");
        RequestBody body = RequestBody.create(contentType, blockIdsPayload.toString());

        Request request = new Request.Builder()
                .url(putBlockListUrl)
                .put(body)
                .addHeader("x-ms-version", "2015-12-11")
                .build();

        Response response = client.newCall(request).execute();
        print(response.toString());
        if (!response.isSuccessful()) {
            if (response.code() >= 400 && response.code() < 500) {
                throw new IOException("The file name length is different. ");
            }
        }
    }

    private void print(String message) {
        Log.d(TAG, message);
    }

    @Override
    public void onProgress(int progress) {
        if (mUploadProgressListener != null) {
            mUploadProgressListener.onProgress(progress);
        }
    }

    @Override
    public StatusCode getStatusCode() {
        return mStatusCode;
    }

    @Override
    public void setUploadProgressListener(UploadProgressListener uploadProgressListener) {
        mUploadProgressListener = uploadProgressListener;
    }
}
