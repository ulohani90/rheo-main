package com.rheotv.android.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ChatLogs {

    public ChatLogs() {
        createEventFile();
    }


    static ChatLogs mInstance;

    public static ChatLogs getInstance() {
        if (mInstance == null) {
            mInstance = new ChatLogs();
        }
        return mInstance;
    }

    File logFile;

    public void createEventFile() {
        int currentFileNum = CommonUtils.getAnalyticsFileCount() + 1;
        logFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/rheo_analytics", "chat_log_" +
                currentFileNum + ".txt");
        CommonUtils.setAnalyticsFileCount(currentFileNum);
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }

    /**
     * uncomment this only for debug apk
     * */
    public void addEventToFile(String data, long ts, String username) {
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(logFile, true));
//            outputStreamWriter.write("\n" + TimeUtils.getDateTimeFromLongMS(ts) + ":: Username " + username + "::" + data + "\n");
//            outputStreamWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    /*public void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
}
