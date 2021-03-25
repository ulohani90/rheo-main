package com.rheotv.android.helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileUtilityHelper {
    private static final String TAG = FileUtilityHelper.class.getSimpleName();
    public enum FileType{
        Video,
        Image,
        Audio,
        Other;
        public String getLocalDirectoryName(){
            return this.name()+"s";
        }
    }

    public static String getMimeType(String url, FileType fileType){
        String extention = getExtenstionFromURL(url);
        String type = fileType.name().toLowerCase();
        if (fileType==FileType.Video){
            return type+"/"+extention;
        }
        return "";
    }
    public static String getExtenstionFromURL(String url){
        String fileName = getFileNameFromURL(url);
        return fileName.substring(fileName.indexOf('.')+1);
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        /*try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        }
        catch(MalformedURLException e) {
            return "";
        }*/

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    public static String getFilename(Context context, String type, String fileName) {
        File file = new File(context.getExternalCacheDir(), type);
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + fileName);
        return uriSting;
    }

    public static String downloadAndGetLocalPath(Context context, String fileToDownloadURL, FileType fileType){
        String fileName = FileUtilityHelper.getFileNameFromURL(fileToDownloadURL);
        String type = fileType.getLocalDirectoryName();
        try {
            //final java.net.URL url = new URL(url);
            URL url = new URL(fileToDownloadURL);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream
            String outputFileName = getFilename(context, type, fileName);
            OutputStream output = new FileOutputStream(outputFileName);
            byte data[] = new byte[1024*100];
            int count;
//            int progress = 0;
            while ((count = input.read(data)) != -1) {
                Log.d(TAG, "Started writing " + count);
                output.write(data, 0, count);
//                ViewUtils.setSnackbarProgress((progress++)/30, context);
            }
            Log.d(TAG, "Done writing");
            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();
            return outputFileName;
        } catch (Exception e) {
            Log.e(TAG, "Got error while downloading");
            e.printStackTrace();
        }
        return null;
    }

}
