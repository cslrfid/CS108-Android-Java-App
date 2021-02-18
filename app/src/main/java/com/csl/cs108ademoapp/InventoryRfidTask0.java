package com.csl.cs108ademoapp;

import android.os.AsyncTask;
import com.csl.cs108library4a.Cs108Connector;
import com.csl.cs108library4a.Cs108Library4A;

import java.util.ArrayList;

public class InventoryRfidTask0 extends AsyncTask<Void, String, String> {
    final boolean DEBUG = false;
    public enum TaskCancelRReason {
        NULL, INVALD_REQUEST, DESTORY, STOP, BUTTON_RELEASE, TIMEOUT, RFID_RESET
    }
    public TaskCancelRReason taskCancelReason;
    long timeMillis, runTimeMillis;
    long firstTime;
    int batteryCountInventory_old;

    boolean requestSound = false;
    private ArrayList<Cs108Connector.Rx000pkgData> rx000pkgDataArrary = new ArrayList<Cs108Connector.Rx000pkgData>();
    private String endingMessaage;
    long lastTime;
    boolean continousRequest = false;
    long timeMillisSound = 0;
    boolean popRequest = false;

    @Override
    protected void onPreExecute() { }

    @Override
    protected String doInBackground(Void... a) {
        boolean ending = false;
        Cs108Connector.Rx000pkgData rx000pkgData;
        while (MainActivity.mCs108Library4a.isBleConnected() && isCancelled() == false && ending == false) {
            int batteryCount = MainActivity.mCs108Library4a.getBatteryCount();
            if (batteryCountInventory_old != batteryCount) {
                batteryCountInventory_old = batteryCount;
                publishProgress("VV");
            }
            if (System.currentTimeMillis() > runTimeMillis + 1000) {
                runTimeMillis = System.currentTimeMillis();
                publishProgress("WW");
            }
            rx000pkgData = MainActivity.mCs108Library4a.onRFIDEvent();
            if (rx000pkgData != null && MainActivity.mCs108Library4a.mrfidToWriteSize() == 0) {
                if (rx000pkgData.responseType == null) {
                    publishProgress("null response");
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_INVENTORY) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            else lastTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_ANTENNA_CYCLE_END) {
                    timeMillis = System.currentTimeMillis();
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_COMMAND_END) {
                    if (rx000pkgData.decodedError != null) endingMessaage = rx000pkgData.decodedError;
                    if (continousRequest) {
                        MainActivity.mCs108Library4a.batteryLevelRequest();
                        MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
                    } else  ending = true;
                }
            }
            if (false) {
                if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   timeMillis = System.currentTimeMillis();
            } else {
                //suspend the current thread up to 5 seconds until all the commands on the output buffer got sent out
                long toCnt = System.currentTimeMillis();
                if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    while (System.currentTimeMillis() - toCnt < 50000 && MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask0: send commands elapsed time: " + String.format("%d", System.currentTimeMillis() - toCnt));
                    timeMillis = System.currentTimeMillis();
                }
            }
            if (System.currentTimeMillis() - timeMillis > 10000 && false) { //no tag timeout handling during inventory
                if (true) taskCancelReason = TaskCancelRReason.TIMEOUT;
                else {
                    timeMillisSound = System.currentTimeMillis();
                    requestSound = true;
                }
            }
            if (taskCancelReason != TaskCancelRReason.NULL) {
                MainActivity.mCs108Library4a.abortOperation();
                publishProgress("XX");
                if(popRequest) publishProgress("P");
                timeMillis = 0; boolean endStatus = true;
                cancel(true);
            }
        }
        return "End of Asynctask()";
    }

    @Override
    protected void onProgressUpdate(String... output) {
        if (output[0] != null) {
            if (output[0].length() == 1) inventoryHandler_endReason();
            else if (output[0].length() == 2) {
                if (output[0].contains("XX")) MainActivity.mCs108Library4a.appendToLogView("CANCELLING. sent abortOperation");
                else if (output[0].contains("WW")) inventoryHandler_runtime();
                else if (output[0].contains("VV")) inventoryHandler_voltage();
            } else
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask0.InventoryRfidTask0.onProgressUpdate(): " + output[0]);
        } else inventoryHandler_tag();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask0.InventoryRfidTask0.onCancelled()");
        DeviceConnectTask4InventoryEnding(taskCancelReason);
    }

    @Override
    protected void onPostExecute(String result) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask0.InventoryRfidTask0.onPostExecute(): " + result);
        DeviceConnectTask4InventoryEnding(taskCancelReason);
    }

    public InventoryRfidTask0() { }

    void inventoryHandler_tag() { }
    void inventoryHandler_runtime() { }
    void inventoryHandler_voltage() { }
    void inventoryHandler_endReason() { }
    void DeviceConnectTask4InventoryEnding(TaskCancelRReason taskCancelRReason) { }
}