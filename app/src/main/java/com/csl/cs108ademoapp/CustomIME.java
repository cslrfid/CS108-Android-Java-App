package com.csl.cs108ademoapp;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.csl.cs108library4a.Cs108Library4A;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CustomIME extends InputMethodService { //implements KeyboardView.OnKeyboardActionListener {
    Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        appendToLog("CustomIME.onCreate()");
    }
    @Override
    public View onCreateInputView() {
        super.onCreateInputView();;
        mHandler.post(serviceRunnable);
        KeyboardView keyboardView = null;
        if (false) {
            keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
            Keyboard keyboard = new Keyboard(this, R.xml.number_pad);
            keyboardView.setKeyboard(keyboard);
            //keyboardView.setOnKeyboardActionListener(this);
        }
        return keyboardView;
    }
    @Override
    public void onDestroy() {
        appendToLog("CustomIME.onDestroy()");
        mHandler.removeCallbacks(serviceRunnable);
        super.onDestroy();
    }

    ArrayList<String> epcArrayList = new ArrayList<String>();
    InventoryRfidTask inventoryRfidTask;
    boolean inventoring = false;
    private Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            String strCurrentIME = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            appendToLog("CustomIME Debug 0 with " + strCurrentIME);
            if (strCurrentIME.contains("com.csl.cs108ademoapp") == false) return;

            mHandler.postDelayed(serviceRunnable, 1000);
            if (MainActivity.sharedObjects == null) return;
            if (MainActivity.csLibrary4A == null) return;

            if (inventoring == false) { MainActivity.sharedObjects.serviceArrayList.clear(); epcArrayList.clear(); }
            if (MainActivity.mContext == null) return;
            appendToLog("CustomIME Debug 1 with activityActive = " + MainActivity.activityActive + ", wedged = " + MainActivity.wedged + ", isBleConnected = " + MainActivity.csLibrary4A.isBleConnected());
            if (MainActivity.activityActive == false /*&& MainActivity.wedged*/ && MainActivity.csLibrary4A.isBleConnected()) {
                if (MainActivity.csLibrary4A.getTriggerButtonStatus() == false) {
                    appendToLog("CustomIME Debug 2");
                    startStopHandler();
                    inventoring = false;
                } else if (inventoring == false) {
                    appendToLog("CustomIME Debug 3");
                    if (MainActivity.sharedObjects.runningInventoryRfidTask == false && MainActivity.sharedObjects.runningInventoryBarcodeTask == false && MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                        startStopHandler();
                        inventoring = true;
                    }
                } else {
                    appendToLog("CustomIME Debug 4");
                    while (MainActivity.sharedObjects.serviceArrayList.size() != 0) {
                        String strEpc = MainActivity.sharedObjects.serviceArrayList.get(0); MainActivity.sharedObjects.serviceArrayList.remove(0);
                        boolean matched = false;
                        for (int i = 0; i < epcArrayList.size(); i++) {
                            if (epcArrayList.get(i).matches(strEpc)) {
                                matched = true;
                                break;
                            }
                        }
                        if (matched == false && strEpc != null) {
                            epcArrayList.add(strEpc);
                            InputConnection ic = getCurrentInputConnection();
                            strEpc = (MainActivity.wedgePrefix != null ? MainActivity.wedgePrefix : "") + strEpc
                                    + (MainActivity.wedgeSuffix != null ? MainActivity.wedgeSuffix : "");
                            switch (MainActivity.wedgeDelimiter) {
                                default:
                                    strEpc += "\n";
                                    break;
                                case 0x09:
                                    strEpc += "\t";
                                    break;
                                case 0x2C:
                                    strEpc += ",";
                                    break;
                                case 0x20:
                                    strEpc += " ";
                                    break;
                                case -1:
                                    break;
                            }
                            ic.commitText(strEpc, 1);
                        }
                    }
                }
            }
        }
    };

    public void appendToLog(String s) {
        Log.i(TAG + ".Hello", s);
    }
    void startStopHandler() {
        boolean started = false;
        if (inventoryRfidTask != null) {
            if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        }
        appendToLog("CustomIME Debug 10");
        if ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false)) return;
        if (started == false) {
            appendToLog("CustomIME Debug 11");
            MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY);
            inventoryRfidTask = new InventoryRfidTask();
            inventoryRfidTask.execute();
        }
        else {
            appendToLog("CustomIME Debug 11");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
        }
    }
}