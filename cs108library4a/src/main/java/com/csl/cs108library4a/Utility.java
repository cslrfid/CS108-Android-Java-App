package com.csl.cs108library4a;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class Utility {
    private Context mContext; private TextView mLogView;
    Utility(Context context, TextView mLogView) {
        mContext = context;
        this.mLogView = mLogView;
    }

    private static long mReferenceTimeMs;
    void setReferenceTimeMs() {
        mReferenceTimeMs = System.currentTimeMillis();
    }
    long getReferencedCurrentTimeMs() { return System.currentTimeMillis() - mReferenceTimeMs; }

    boolean compareByteArray(byte[] array1, byte[] array2, int length) {
        int i = 0;
        if (array1 == null) return false;
        if (array2 == null) return false;
        if (array1.length < length || array2.length < length) {
            return false;
        }
        for (; i < length; i++) {
            if (array1[i] != array2[i]) {
                break;
            }
        }
        return (i == length);
    }
    String byteArrayToString(byte[] packet) {
        if (packet == null) return "";
        StringBuilder sb = new StringBuilder(packet.length * 2);
        for (byte b : packet) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static Handler mHandler = new Handler();
    void appendToLogRunnable(final String s) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                appendToLog(s);
            }
        });
    }
    String appendToLog(String s) {
        String TAG = "";
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        boolean logged = false;
        boolean foundMe = false;
        for(int i=0; i<stacktrace.length; i++) {
            StackTraceElement e = stacktrace[i];
            String methodName = e.getMethodName();
            if (methodName.contains("appendToLog")) {
                foundMe = true;
            } else if (foundMe) {
                if (!methodName.startsWith("access$")) {
                    TAG = String.format(Locale.US, "%s.%s", e.getClassName(), methodName);
                    logged = true;
                    break;
                }
            }
        }
        Log.i(TAG + ".Hello", s);
        String string = "\n" + getReferencedCurrentTimeMs() + "." + s;
        return (string);
    }
    void appendToLogView(String s) {
        appendToLog(s);
        String string = "\n" + getReferencedCurrentTimeMs() + "." + s;
        if (Looper.myLooper() == Looper.getMainLooper() && mLogView != null && string != null)   mLogView.append(string);
    }

    private static File fileDebug; private static boolean enableFileDebug = false;
    void debugFileSetup() {
        boolean writeExtPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                writeExtPermission = false;
                appendToLog("requestPermissions WRITE_EXTERNAL_STORAGE 1");
                requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Toast.makeText(mContext, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String errorDisplay = null;
        if (writeExtPermission == false) {
            errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!";
        } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) errorDisplay = "Error in mouting external storage !!!";
        else {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/cs108Java");
            if (path.exists() == false) path.mkdirs();
            if (path.exists() == false) errorDisplay = "Error in making directory !!!";
            else {
                String dateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                String fileName = "cs108JavaDebug_" + dateTime + ".txt";
                fileDebug = new File(path, fileName);
                if (fileDebug == null) errorDisplay = "Error in making directory !!!";
            }
        }
        if (errorDisplay != null) appendToLog("Error in saving file with " + errorDisplay);
    }
    void debugFileClose() {
        if (fileDebug != null) {
            try {
                MediaScannerConnection.scanFile(mContext, new String[]{fileDebug.getAbsolutePath()}, null, null);
            } catch (Exception ex) {
            }
        }
    }
    void debugFileEnable(boolean enable) { enableFileDebug = enable; }
    void writeDebug2File(String stringDebug) {
        if (fileDebug != null && enableFileDebug) {
            try {
                FileOutputStream outputStreamDebug = new FileOutputStream(fileDebug, true);
                PrintWriter printWriterDebug = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(outputStreamDebug), "UTF-8"));
                printWriterDebug.println(stringDebug);
                printWriterDebug.flush(); printWriterDebug.close();
                outputStreamDebug.close();
            } catch (Exception ex) {
            }
        }
    }
}
