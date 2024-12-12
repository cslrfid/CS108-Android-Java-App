package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BarcodeConnector {
    boolean DEBUG_PKDATA;
    public boolean userDebugEnableDefault = false;
    public boolean userDebugEnable = userDebugEnableDefault;

    Context context; Utility utility;
    public BarcodeConnector(Context context, Utility utility) {
        this.context = context;
        this.utility = utility; DEBUG_PKDATA = utility.DEBUG_PKDATA;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }

    public enum BarcodePayloadEvents {
        BARCODE_NULL,
        BARCODE_POWER_ON, BARCODE_POWER_OFF, BARCODE_SCAN_START, BARCODE_COMMAND, BARCODE_VIBRATE_ON, BARCODE_VIBRATE_OFF,
        BARCODE_DATA_READ, BARCODE_GOOD_READ,
    }
    public static class CsReaderBarcodeData {
        public boolean waitUplinkResponse = false;
        boolean downlinkResponsed = false;
        public BarcodePayloadEvents barcodePayloadEvent;
        public byte[] dataValues;
    }

    private boolean onStatus = false; public boolean getOnStatus() { return onStatus; }
    private boolean vibrateStatus = false; boolean getVibrateStatus() { return vibrateStatus; }

    public interface BarcodeConnectorCallback {
        boolean callbackMethod(byte[] dataValues, CsReaderBarcodeData csReaderBarcodeData);
    }
    public BarcodeConnectorCallback barcodeConnectorCallback = null;

    public ArrayList<CsReaderBarcodeData> barcodeToWrite = new ArrayList<>();
    public ArrayList<CsReaderBarcodeData> mBarcodeToRead = new ArrayList<>();

    private boolean arrayTypeSet(byte[] dataBuf, int pos, BarcodePayloadEvents event) {
        boolean validEvent = false;
        switch (event) {
            case BARCODE_POWER_ON:
                validEvent = true;
                break;
            case BARCODE_POWER_OFF:
                dataBuf[pos] = 1;
                validEvent = true;
                break;
            case BARCODE_SCAN_START:
                dataBuf[pos] = 2;
                validEvent = true;
                break;
            case BARCODE_COMMAND:
                dataBuf[pos] = 3;
                validEvent = true;
                break;
            case BARCODE_VIBRATE_ON:
                dataBuf[pos] = 4;
                validEvent = true;
                break;
            case BARCODE_VIBRATE_OFF:
                dataBuf[pos] = 5;
                validEvent = true;
                break;
        }
        return validEvent;
    }
    private byte[] writeBarcode(CsReaderBarcodeData data) {
        int datalength = 0;
        if (data.dataValues != null)    datalength = data.dataValues.length;
        byte[] dataOutRef = new byte[] { (byte) 0xA7, (byte) 0xB3, 2, (byte) 0x6A, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0x90, 0};

        byte[] dataOut = new byte[10 + datalength];
        if (datalength != 0)    {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
            dataOutRef[2] += datalength;
        }
        System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);

        if (arrayTypeSet(dataOut, 9, data.barcodePayloadEvent)) {
            if (false) {
                appendToLog("BarStreamOut: " + byteArrayToString(dataOut));
                appendToLogView("BOut: " + byteArrayToString(dataOut));
            }
            if (DEBUG_PKDATA) appendToLog(String.format("PkData: write Barcode.%s.%s with mBarcodeDevice.sendDataToWriteSent = %d", data.barcodePayloadEvent.toString(), byteArrayToString(data.dataValues), sendDataToWriteSent));
            if (sendDataToWriteSent != 0) appendToLog("!!! mBarcodeDevice.sendDataToWriteSent = " + sendDataToWriteSent);
            return dataOut;
        }
        return null;
    }
    public int barcodePowerOnTimeOut = 0;
    public boolean isMatchBarcodeToWrite(ConnectorData connectorData) {
        boolean match = false, DEBUG = false;
        if (barcodeToWrite.size() != 0 && connectorData.dataValues[0] == (byte)0x90) {
            if (DEBUG) appendToLog("csReadData = " + byteArrayToString(connectorData.dataValues));
            //if (DEBUG) appendToLog("tempDisconnect: icsModel = " + bluetoothConnector.getCsModel() + ", mBarcodeToWrite.size = " + mBarcodeToWrite.size());
            if (barcodeToWrite.size() != 0) if (DEBUG) appendToLog("mBarcodeToWrite(0) = " + barcodeToWrite.get(0).barcodePayloadEvent.toString() + "," + byteArrayToString(barcodeToWrite.get(0).dataValues));
            byte[] dataInCompare = new byte[]{(byte) 0x90, 0};
            if (arrayTypeSet(dataInCompare, 1, barcodeToWrite.get(0).barcodePayloadEvent) && (connectorData.dataValues.length == dataInCompare.length + 1)) {
                if (match = compareArray(connectorData.dataValues, dataInCompare, dataInCompare.length)) {
                    boolean bprocessed = false;
                    byte[] data1 = new byte[connectorData.dataValues.length - 2]; System.arraycopy(connectorData.dataValues, 2, data1, 0, data1.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: matched Barcode.Reply with payload = " + byteArrayToString(connectorData.dataValues) + " for writeData Barcode." + barcodeToWrite.get(0).barcodePayloadEvent.toString());
                    if (connectorData.dataValues[2] != 0) {
                        if (DEBUG) appendToLog("Barcode.reply data is found with error");
                    } else if (true) { //testing bluetoothConnector.getCsModel() == 108) {
                        if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_ON) {
                            barcodePowerOnTimeOut = 1000;
                            if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_ON");
                            onStatus = true;
                            if (DEBUG_PKDATA | connectorData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOn with result = " + connectorData.dataValues[2] + " and onStatus = " + onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_OFF) {
                            if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_OFF");
                            onStatus = false;
                            if (DEBUG_PKDATA | connectorData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOff with result = " + connectorData.dataValues[2] + " and onStatus = " + onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_ON) {
                            vibrateStatus = true;
                            if (DEBUG_PKDATA | connectorData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOn with result = " + connectorData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_OFF) {
                            vibrateStatus = false;
                            if (DEBUG_PKDATA | connectorData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOff with result = " + connectorData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_COMMAND) {
                            barcodePowerOnTimeOut = 500;
                            if (DEBUG_PKDATA | connectorData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.Command with result = " + connectorData.dataValues[2] + " and barcodePowerOnTimeOut = " + barcodePowerOnTimeOut);
                            bprocessed = true;
                        } else {
                            bprocessed = true;
                            if (DEBUG_PKDATA) appendToLog("matched Barcode.Other.Reply data is found.");
                        }

                        CsReaderBarcodeData csReaderBarcodeData = barcodeToWrite.get(0);
                        if (csReaderBarcodeData.waitUplinkResponse) {
                            csReaderBarcodeData.downlinkResponsed = true; iOkCount = 0;
                            barcodeToWrite.set(0, csReaderBarcodeData);
                            if (DEBUG_PKDATA) appendToLog("PkData: mBarcodeToWrite.downlinkResponsed is set and waiting uplink data");
                            utility.writeDebug2File("Up31 " + barcodeToWrite.get(0).barcodePayloadEvent.toString() + ", " + byteArrayToString(data1));
                            return true;
                        }
                    } else {
                        barcodeFailure = true;
                        appendToLog("Not matched Barcode.Reply");
                    }
                    String string = "Up31 " + (bprocessed ? "" : "Unprocessed, ") + barcodeToWrite.get(0).barcodePayloadEvent.toString() + ", " + byteArrayToString(data1);
                    utility.writeDebug2File(string);
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true; appendToLog("barcodeToWrite remove0 with length = " + barcodeToWrite.size());
                    if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + barcodeToWrite.size());
                }
            }
        }
        return match;
    }

    public int sendDataToWriteSent = 0; public boolean mDataToWriteRemoved = false;
    public boolean barcodeFailure = false;
    public byte[] sendBarcodeToWrite() {
        boolean DEBUG = false;
        /*if (barcodePowerOnTimeOut != 0) {
            if (DEBUG) appendToLog("barcodePowerOnTimeOut = " + barcodePowerOnTimeOut + ", mBarcodeToWrite.size() = " + barcodeToWrite.size());
            return false;
        }
        if (barcodeToWrite.size() != 0) {*/
            //if (DEBUG) appendToLog("mBarcodeToWrite.size = " + barcodeToWrite.size());
            //appendToLog("testing 3"); appendToLog("testing 4");
            //if (false) { //testing isBleConnected() == false) {
            //    barcodeToWrite.clear();
            //} else if (false) { //testing System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                BarcodePayloadEvents barcodePayloadEvents = barcodeToWrite.get(0).barcodePayloadEvent;
                //if (DEBUG)  appendToLog("barcodePayloadEvents = " + barcodePayloadEvents.toString());
                boolean isBarcodeData = false;
                if (barcodePayloadEvents == BarcodePayloadEvents.BARCODE_SCAN_START || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_COMMAND) isBarcodeData = true;
                //appendToLog("BarcodePayloadEvents = " + barcodePayloadEvents.toString() + ", barcodeFailure = " + barcodeFailure + ", isBarcodeData = " + isBarcodeData + ", sendDataToWriteSent = " + sendDataToWriteSent);
                if (barcodeFailure && isBarcodeData) {
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true; appendToLog("barcodeToWrite remove0 with length = " + barcodeToWrite.size());
                } else if (sendDataToWriteSent >= 5 && isBarcodeData) {
                    int oldSize = barcodeToWrite.size();
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true; appendToLog("barcodeToWrite remove0 with length = " + barcodeToWrite.size());
                    if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated barcodeToWrite.size() = " + barcodeToWrite.size());
                    if (DEBUG) appendToLog("Removed after sending count-out.");
                    String string = "Problem in sending data to Barcode Module. Removed data sending after count-out";
                    if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                    else appendToLogView(string);
                    //appendToLog("testing 5"); //testing if (bluetoothConnector.getCsModel() == 108) Toast.makeText(context, "No barcode present on Reader", Toast.LENGTH_LONG).show();
                    barcodeFailure = true; // disconnect(false);
                } else {
                    if (DEBUG) appendToLog("size = " + barcodeToWrite.size() + ", PayloadEvents = " + barcodeToWrite.get(0).barcodePayloadEvent.toString());
                    sendDataToWriteSent++;
                    return writeBarcode(barcodeToWrite.get(0));
                    /*if (retValue) {
                        sendDataToWriteSent++;
                        mDataToWriteRemoved = false;
                    } else {
                        //if (DEBUG) appendToLogView("failure to send " + barcodeToWrite.get(0).barcodePayloadEvent.toString());
                        barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true; appendToLog("barcodeToWrite remove0 with length = " + barcodeToWrite.size());
                    }
                    return true;*/
                }
            //}
        //}
        return null;
    }

    int iOkCount = 0;
    public boolean isBarcodeToRead(ConnectorData connectorData) {
        boolean found = false, DEBUG = false;

        if (connectorData.dataValues[0] == (byte) 0x91) {
            if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink with payload = " + byteArrayToString(connectorData.dataValues));
            CsReaderBarcodeData csReaderBarcodeData = new CsReaderBarcodeData();
            switch (connectorData.dataValues[1]) {
                case 0:
                    csReaderBarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_DATA_READ;
                    byte[] dataValues = new byte[connectorData.dataValues.length - 2];
                    System.arraycopy(connectorData.dataValues, 2, dataValues, 0, dataValues.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead with payload = " + byteArrayToString(dataValues));
                    //commandType = null;
                    if (barcodeToWrite.size() > 0) {
                        if (barcodeToWrite.get(0).downlinkResponsed) {
                            if (barcodeConnectorCallback != null) found = barcodeConnectorCallback.callbackMethod(dataValues, csReaderBarcodeData);
                            break;
                        }
                    }
                    /*for (int i=0; false && commandType == null && i < dataValues.length; i++) {
                        if (dataValues[i] == 0x28 || dataValues[i] == 0x29    //  ( )
                                || dataValues[i] == 0x5B || dataValues[i] == 0x5D || dataValues[i] == 0x5C
                                || dataValues[i] == 0x7B || dataValues[i] == 0x7D
                        ) dataValues[i] = 0x20;
                    }*/
                    csReaderBarcodeData.dataValues = dataValues;
                    mBarcodeToRead.add(csReaderBarcodeData);
                    if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.DataRead." + byteArrayToString(dataValues) + " is added to mBarcodeToRead");
                    found = true;
                    break;
                case 1:
                    if (DEBUG) appendToLog("BarStream: matched Barcode.good data is found");
                    csReaderBarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_GOOD_READ;
                    csReaderBarcodeData.dataValues = null;
                    mBarcodeToRead.add(csReaderBarcodeData);
                    if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.GoodRead is added to mBarcodeToRead");
                    found = true;
                    break;
            }
        }
        if (found && DEBUG)  appendToLog("found Barcode.read data = " + byteArrayToString(connectorData.dataValues));
        return found;
    }
}