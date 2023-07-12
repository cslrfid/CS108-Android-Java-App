package com.csl.cs108ademoapp;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingTask extends AsyncTask<Void, String, String> {
    final boolean DEBUG = false;
    private enum TaskCancelRReason {
        SAME_SETTING, INVALD_REQUEST, TIMEOUT
    }
    private TaskCancelRReason taskCancelReason;

    Button button;
    boolean sameSetting = false;
    boolean invalidRequest = false;

    public SettingTask(Button button, boolean sameSetting, boolean invalidRequest) {
        button.setVisibility(View.INVISIBLE);
        this.button = button;
        this.sameSetting = sameSetting;
        this.invalidRequest = invalidRequest;
    }

    @Override
    protected void onPreExecute() {
        if (sameSetting) {
            taskCancelReason = TaskCancelRReason.SAME_SETTING;
            cancel(true);
        } else if (invalidRequest) {
            taskCancelReason = TaskCancelRReason.INVALD_REQUEST;
            cancel(true);
        }
    }

    @Override
    protected String doInBackground(Void... a) {
        long timeMillis = System.currentTimeMillis();
        int writeSize = MainActivity.csLibrary4A.mrfidToWriteSize();
        while (true) {
            int writeSizeN = MainActivity.csLibrary4A.mrfidToWriteSize();
            if (writeSizeN == 0)   break;
            if (writeSizeN < writeSize) {
                writeSize = writeSizeN;
                timeMillis = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - timeMillis > 5000) {
                taskCancelReason = TaskCancelRReason.TIMEOUT;
                cancel(true);
            }
        }
        return "End of Asynctask()";
    }

    @Override
    protected void onProgressUpdate(String... output) { }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("Setting0Fragment.SettingTask.onCancelled()");
        if (taskCancelReason != null) {
            switch (taskCancelReason) {
                case SAME_SETTING:
                    Toast.makeText(MainActivity.mContext, R.string.toast_same_setting, Toast.LENGTH_SHORT).show();
                    break;
                case INVALD_REQUEST:
                    Toast.makeText(MainActivity.mContext, R.string.toast_invalid_sendHostRequest, Toast.LENGTH_SHORT).show();
                    break;
                case TIMEOUT:
                    Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_TIMEOUT, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(String result) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("Setting0Fragment.SettingTask.onPostExecute(): " + result);

        Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        button.setVisibility(View.VISIBLE);
    }
}
