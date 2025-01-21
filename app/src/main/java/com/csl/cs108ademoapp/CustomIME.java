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

import com.csl.cslibrary4a.RfidReaderChipData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CustomIME extends InputMethodService { //implements KeyboardView.OnKeyboardActionListener {
    Handler mHandler = new Handler();
    InputConnection ic;
    DatagramSocket datagramSocket;
    InetAddress hostAddress = null;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            hostAddress = hostAddress = InetAddress.getByName("127.0.0.1");
            Log.i("Hello", "udpSocket hostAddress is valid");
            datagramSocket = new DatagramSocket(9394, hostAddress);
            if (datagramSocket == null) Log.i("Hello", "udpSocket is null");
            else Log.i("Hello", "udpSocket is valid");
            //byte[] buffer = new byte[16];
            //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 14552);
            //udpsocket.send(packet);
        } catch (SocketException e) {
            Log.e("UDP: ", "udpSocket Socket Error: ", e);
        } catch (IOException e) {
            Log.e("UDP Send: ", "udpSocket IO Error", e);
        } catch (Exception ex) {
            Log.i("Hello", "udpSocket Exception Error: " + ex.getMessage());
        }
        appendToLog("CustomIME.onCreate()");
    }
    @Override
    public View onCreateInputView() {
        super.onCreateInputView();;
        mHandler.post(serviceRunnable);
        KeyboardView keyboardView = null;
        if (true) {
            keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
            Keyboard keyboard = new Keyboard(this, R.xml.number_pad1);
            keyboardView.setKeyboard(keyboard);
            //keyboardView.setOnKeyboardActionListener(this);
        }
        ic = getCurrentInputConnection();
        Log.i("Hello", "udpSocket inputConnection is " + (ic == null ? "null" : "valid"));
        return keyboardView;
    }
    @Override
    public void onDestroy() {
        appendToLog("CustomIME.onDestroy()");
        mHandler.removeCallbacks(serviceRunnable);
        datagramSocket.close();
        super.onDestroy();
    }

    Runnable yourRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer  = new byte[250];
            Log.i("Hello", "udpSocket yourRunnable starts");
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            ic = getCurrentInputConnection();
            try {
                Log.i("Hello", "udpSocket try starts");
//                    String outString = "Client say: bye bye";
//                    buffer = outString.getBytes();
//                    DatagramPacket outDatagramPackat = new DatagramPacket(buffer, buffer.length, hostAddress, 9394);
//                    datagramSocket.send(outDatagramPackat);
//                    Log.i("Hello", "udpSocket sent data");

                String strDataReceived;
                do {
                    strDataReceived = null;
                    datagramSocket.receive(datagramPacket);
                    strDataReceived = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    Log.i("Hello", "udpSocket received data: " + strDataReceived);
                    ic.commitText(strDataReceived, 1);
                } while (strDataReceived != null && strDataReceived.length() != 0);
            } catch (IOException ex) {
                Log.i("Hello", "udpSocket receive IOException Error: " + ex.toString());
            }
        }
    };

    ArrayList<String> epcArrayList = new ArrayList<String>();
    InventoryRfidTask inventoryRfidTask;
    InventoryBarcodeTask inventoryBarcodeTask;
    boolean inventoring = false;
    private Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            String strCurrentIME = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            String strCompare = getPackageName();
            appendToLog("CustomIME Debug 0 with strCurrentIME = " + strCurrentIME + ", strCompare = " + strCompare);
            if (strCurrentIME.contains(strCompare) == false) { }
            else if (MainActivity.sharedObjects == null || MainActivity.csLibrary4A == null) {
                if (false) { new Thread(yourRunnable).start(); }
            }
            else if (MainActivity.mContext == null) return;
            else {
                if (inventoring == false) {
                    MainActivity.sharedObjects.serviceArrayList.clear();
                    epcArrayList.clear();
                }
                appendToLog("CustomIME Debug 1 with activityActive = " + MainActivity.activityActive + ", wedged = " + MainActivity.wedged + ", isBleConnected = " + MainActivity.csLibrary4A.isBleConnected());
                if (MainActivity.activityActive == false /*&& MainActivity.wedged*/ && MainActivity.csLibrary4A.isBleConnected()) {
                    if (MainActivity.csLibrary4A.getTriggerButtonStatus() == false) {
                        appendToLog("CustomIME Debug 2 with runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask);
                        appendToLog("CustomIME Debug 2 with runningInventoryBarcodeTask = " + MainActivity.sharedObjects.runningInventoryBarcodeTask);
                        startStopHandler();
                        inventoring = false;
                    } else if (inventoring == false) {
                        appendToLog("CustomIME Debug 3 with runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask + ", and mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
                        appendToLog("CustomIME Debug 3 with runningInventoryBarcodeTask = " + MainActivity.sharedObjects.runningInventoryBarcodeTask);
                        if (MainActivity.sharedObjects.runningInventoryRfidTask == false && MainActivity.sharedObjects.runningInventoryBarcodeTask == false && MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                            startStopHandler();
                            inventoring = true;
                        }
                    } else {
                        appendToLog("CustomIME Debug 4");
                        while (MainActivity.sharedObjects.serviceArrayList.size() != 0) {
                            String strEpc = MainActivity.sharedObjects.serviceArrayList.get(0);
                            MainActivity.sharedObjects.serviceArrayList.remove(0);
                            appendToLog("CustomIME Debug 4A with strEpc = " + strEpc);
                            String strSgtin = null;
                            if (MainActivity.csLibrary4A.getWedgeOutput() == 1) {
                                strSgtin = MainActivity.csLibrary4A.getUpcSerial(strEpc);
                                appendToLog("strSgtin = " + (strSgtin == null ? "null" : strSgtin));
                                if (strSgtin == null) strEpc = null;
                            }
                            boolean matched = false;
                            if (epcArrayList != null && strEpc != null) {
                                for (int i = 0; i < epcArrayList.size(); i++) {
                                    if (epcArrayList.get(i).matches(strEpc)) {
                                        matched = true;
                                        break;
                                    }
                                }
                            }
                            if (matched == false && strEpc != null) {
                                epcArrayList.add(strEpc);
                                InputConnection ic = getCurrentInputConnection();
                                String strValue = strEpc;
                                if (strSgtin != null) strValue = strSgtin;
                                if (MainActivity.csLibrary4A.getWedgePrefix() != null)
                                    strValue = MainActivity.csLibrary4A.getWedgePrefix() + strValue;
                                if (MainActivity.csLibrary4A.getWedgeSuffix() != null)
                                    strValue += MainActivity.csLibrary4A.getWedgeSuffix();
                                appendToLog("CustomIME, serviceRunnable: wedgeDelimiter = " + MainActivity.csLibrary4A.getWedgeDelimiter());
                                switch (MainActivity.csLibrary4A.getWedgeDelimiter()) {
                                    default:
                                        strValue += "\n";
                                        break;
                                    case 0x09:
                                        strValue += "\t";
                                        break;
                                    case 0x2C:
                                        strValue += ",";
                                        break;
                                    case 0x20:
                                        strValue += " ";
                                        break;
                                    case -1:
                                        break;
                                }
                                appendToLog("CustomIME BtData to Keyboard: " + strValue);
                                ic.commitText(strValue, 1);
                            }
                        }
                    }
                }
            }
            int iDelayms = 500;
            if (inventoring) iDelayms = 100;
            appendToLog("CustomIME BtData set next time as " + iDelayms);
            mHandler.postDelayed(serviceRunnable, iDelayms);
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
        if (inventoryBarcodeTask != null) {
            if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        }
        appendToLog("CustomIME Debug 10");
        if ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false)) return;
        if (started == false) {
            appendToLog("CustomIME Debug 11 with BtData wedgeOutput = " + MainActivity.csLibrary4A.getWedgeOutput());
            if (MainActivity.csLibrary4A.getWedgeOutput() == 2) {
                inventoryBarcodeTask = new InventoryBarcodeTask();
                inventoryBarcodeTask.execute();
            } else {
                MainActivity.csLibrary4A.setPowerLevel(MainActivity.csLibrary4A.getWedgePower());
                MainActivity.csLibrary4A.appendToLog("Debug_Compact: CustomIME.startStopHandler");
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
                inventoryRfidTask = new InventoryRfidTask();
                inventoryRfidTask.execute();
            }
        } else {
            appendToLog("CustomIME Debug 11");
            if (inventoryRfidTask != null) inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            if (inventoryBarcodeTask != null) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
        }
    }
}