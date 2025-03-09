package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class BarcodeConnector {
    public boolean userDebugEnableDefault = false;
    public boolean userDebugEnable = userDebugEnableDefault;

    Context context; Utility utility;
    public BarcodeConnector(Context context, Utility utility) {
        this.context = context;
        this.utility = utility;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }

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
                Logger.trace("BarStreamOut: {}", byteArrayToString(dataOut));
                Logger.toLogView("BOut: {}",  byteArrayToString(dataOut)).trace();
            }
            Logger.pkData("PkData: write Barcode.{}.{} with mBarcodeDevice.sendDataToWriteSent = {}", data.barcodePayloadEvent.toString(), byteArrayToString(data.dataValues), sendDataToWriteSent);
            if (sendDataToWriteSent != 0) Logger.pkData("!!! mBarcodeDevice.sendDataToWriteSent = {}", sendDataToWriteSent);
            return dataOut;
        }
        return null;
    }
    public int barcodePowerOnTimeOut = 0;
    public boolean isMatchBarcodeToWrite(ConnectorData connectorData) {
        boolean match = false;
        if (!barcodeToWrite.isEmpty() && connectorData.dataValues[0] == (byte)0x90) {
            Logger.debug("csReadData = {}", byteArrayToString(connectorData.dataValues));
            if (!barcodeToWrite.isEmpty()) Logger.debug("barcodeToWrite(0) = {}, {}", barcodeToWrite.get(0).barcodePayloadEvent, byteArrayToString(barcodeToWrite.get(0).dataValues));
            byte[] dataInCompare = new byte[]{(byte) 0x90, 0};
            if (arrayTypeSet(dataInCompare, 1, barcodeToWrite.get(0).barcodePayloadEvent) && (connectorData.dataValues.length == dataInCompare.length + 1)) {
                if (match = compareArray(connectorData.dataValues, dataInCompare, dataInCompare.length)) {
                    boolean bprocessed = false;
                    byte[] data1 = new byte[connectorData.dataValues.length - 2]; System.arraycopy(connectorData.dataValues, 2, data1, 0, data1.length);
                    Logger.pkData("PkData: matched Barcode.Reply with payload = {} for writeData Barcode. {}", byteArrayToString(connectorData.dataValues), barcodeToWrite.get(0).barcodePayloadEvent);
                    if (connectorData.dataValues[2] != 0) {
                        Logger.debug("Barcode.reply data is found with error");
                    } else { //testing bluetoothConnector.getCsModel() == 108) {
                        if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_ON) {
                            barcodePowerOnTimeOut = 1000;
                            Logger.debug("tempDisconnect: BARCODE_POWER_ON");
                            onStatus = true;
                            if (connectorData.dataValues[2] != 0) Logger.pkData("PkData: matched Barcode.Reply.PowerOn with result = {} and onStatus = {}", connectorData.dataValues[2], onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_OFF) {
                            Logger.debug("tempDisconnect: BARCODE_POWER_OFF");
                            onStatus = false;
                            if (connectorData.dataValues[2] != 0) Logger.pkData("PkData: matched Barcode.Reply.PowerOff with result = {} and onStatus = {}", connectorData.dataValues[2], onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_ON) {
                            vibrateStatus = true;
                            if (connectorData.dataValues[2] != 0) Logger.pkData("PkData: matched Barcode.Reply.VibrateOn with result = {} and vibrateStatus = {}", connectorData.dataValues[2], vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_OFF) {
                            vibrateStatus = false;
                            if (connectorData.dataValues[2] != 0) Logger.pkData("PkData: matched Barcode.Reply.VibrateOff with result = {} and vibrateStatus = {}", connectorData.dataValues[2], vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_COMMAND) {
                            barcodePowerOnTimeOut = 500;
                            if (connectorData.dataValues[2] != 0) Logger.pkData("PkData: matched Barcode.Reply.Command with result = {}, and barcodePowerOnTimeOut = {}", connectorData.dataValues[2], barcodePowerOnTimeOut);
                            bprocessed = true;
                        } else {
                            bprocessed = true;
                            Logger.pkData("matched Barcode.Other.Reply data is found.");
                        }

                        CsReaderBarcodeData csReaderBarcodeData = barcodeToWrite.get(0);
                        if (csReaderBarcodeData.waitUplinkResponse) {
                            csReaderBarcodeData.downlinkResponsed = true; iOkCount = 0;
                            barcodeToWrite.set(0, csReaderBarcodeData);
                            Logger.pkData("PkData: barcodeToWrite.downlinkResponsed is set and waiting uplink data");
                            utility.writeDebug2File("Up31 " + barcodeToWrite.get(0).barcodePayloadEvent.toString() + ", " + byteArrayToString(data1));
                            return true;
                        }
                    }
                    String string = "Up31 " + (bprocessed ? "" : "Unprocessed, ") + barcodeToWrite.get(0).barcodePayloadEvent.toString() + ", " + byteArrayToString(data1);
                    utility.writeDebug2File(string);
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    Logger.info("barcodeToWrite remove0 with length = {}", barcodeToWrite.size());
                    Logger.pkData("PkData: new barcodeToWrite size = {}", barcodeToWrite.size());
                }
            }
        }
        return match;
    }

    public int sendDataToWriteSent = 0; public boolean mDataToWriteRemoved = false;
    public boolean barcodeFailure = false;
    public byte[] sendBarcodeToWrite() {
        /*if (barcodePowerOnTimeOut != 0) {
            Logger.debug("barcodePowerOnTimeOut = " + barcodePowerOnTimeOut + ", barcodeToWrite.size() = " + barcodeToWrite.size());
            return false;
        }
        if (barcodeToWrite.size() != 0) {*/
            //Logger.debug("barcodeToWrite.size = " + barcodeToWrite.size());
            //Logger.trace("testing 3"); Logger.trace("testing 4");
            //if (false) { //testing isBleConnected() == false) {
            //    barcodeToWrite.clear();
            //} else if (false) { //testing System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                BarcodePayloadEvents barcodePayloadEvents = barcodeToWrite.get(0).barcodePayloadEvent;
                //Logger.debug("barcodePayloadEvents = " + barcodePayloadEvents.toString());
                boolean isBarcodeData = false;
                if (barcodePayloadEvents == BarcodePayloadEvents.BARCODE_SCAN_START || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_COMMAND) isBarcodeData = true;
                //Logger.trace("BarcodePayloadEvents = " + barcodePayloadEvents.toString() + ", barcodeFailure = " + barcodeFailure + ", isBarcodeData = " + isBarcodeData + ", sendDataToWriteSent = " + sendDataToWriteSent);
                if (barcodeFailure && isBarcodeData) {
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    Logger.info("barcodeToWrite remove0 with length = {}", barcodeToWrite.size());
                } else if (sendDataToWriteSent >= 5 && isBarcodeData) {
                    int oldSize = barcodeToWrite.size();
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    Logger.info("barcodeToWrite remove0 with length = {}", barcodeToWrite.size());
                    Logger.debug("Removed after sending count-out with oldSize = {}, updated barcodeToWrite.size() = {}", oldSize, barcodeToWrite.size());
                    Logger.debug("Removed after sending count-out.");
                    String string = "Problem in sending data to Barcode Module. Removed data sending after count-out";
                    if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                    else Logger.info(string);
                    barcodeFailure = true; // disconnect(false);
                } else {
                    Logger.debug("size = {}, PayloadEvents = {}", barcodeToWrite.size(), barcodeToWrite.get(0).barcodePayloadEvent);
                    sendDataToWriteSent++;
                    return writeBarcode(barcodeToWrite.get(0));
                    /*if (retValue) {
                        sendDataToWriteSent++;
                        mDataToWriteRemoved = false;
                    } else {
                        //Logger.debug("failure to send " + barcodeToWrite.get(0).barcodePayloadEvent.toString());
                        barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true; Logger.trace("barcodeToWrite remove0 with length = " + barcodeToWrite.size());
                    }
                    return true;*/
                }
            //}
        //}
        return null;
    }

    int iOkCount = 0;
    public boolean isBarcodeToRead(ConnectorData connectorData) {
        boolean found = false;

        if (connectorData.dataValues[0] == (byte) 0x91) {
            Logger.pkData("PkData: found Barcode.Uplink with payload = {}", byteArrayToString(connectorData.dataValues));
            CsReaderBarcodeData csReaderBarcodeData = new CsReaderBarcodeData();
            switch (connectorData.dataValues[1]) {
                case 0:
                    csReaderBarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_DATA_READ;
                    byte[] dataValues = new byte[connectorData.dataValues.length - 2];
                    System.arraycopy(connectorData.dataValues, 2, dataValues, 0, dataValues.length);
                    Logger.pkData("PkData: found Barcode.Uplink.DataRead with payload = {}", byteArrayToString(dataValues));
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
                    Logger.pkData("PkData: uplink data Barcode.DataRead. {} is added to mBarcodeToRead", byteArrayToString(dataValues));
                    found = true;
                    break;
                case 1:
                    Logger.debug("BarStream: matched Barcode.good data is found");
                    csReaderBarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_GOOD_READ;
                    csReaderBarcodeData.dataValues = null;
                    mBarcodeToRead.add(csReaderBarcodeData);
                    Logger.pkData("PkData: uplink data Barcode.GoodRead is added to mBarcodeToRead");
                    found = true;
                    break;
            }
        }
        if (found) Logger.debug("found Barcode.read data = {}", byteArrayToString(connectorData.dataValues));
        return found;
    }
}