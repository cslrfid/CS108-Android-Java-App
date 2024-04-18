package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothConnector {
    boolean DEBUG_PKDATA;
    final boolean DEBUG = false;
    public boolean userDebugEnableDefault = false;
    public boolean userDebugEnable = userDebugEnableDefault;

    Context context; Utility utility;
    public BluetoothConnector(Context context, Utility utility) {
        this.context = context;
        this.utility = utility; DEBUG_PKDATA = utility.DEBUG_PKDATA;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }

    private int icsModel = -1;
    public int getCsModel() {
        if (false) appendToLog("icsModel = " + icsModel);
        return icsModel;
    }

    public enum BluetoothIcPayloadEvents {
        BLUETOOTH_GET_VERSION, BLUETOOTH_SET_DEVICE_NAME, BLUETOOTH_GET_DEVICE_NAME, BLUETOOTH_FORCE_BT_DISCONNECT
    }

    public class BluetoothIcData {
        public BluetoothIcPayloadEvents bluetoothIcPayloadEvent;
        public byte[] dataValues;
    }

    private byte[] mBluetoothIcVersion = new byte[]{-1, -1, -1};
    private boolean mBluetoothIcVersionUpdated = false;
    public String getBluetoothIcVersion() {
        boolean DEBUG = false;
        if (mBluetoothIcVersionUpdated == false) {
            if (DEBUG) appendToLog("mBluetoothIcVersionUpdated is false");
            boolean repeatRequest = false;
            if (bluetoothIcToWrite.size() != 0) {
                if (bluetoothIcToWrite.get(bluetoothIcToWrite.size() - 1).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.BLUETOOTH_GET_VERSION) {
                    repeatRequest = true;
                }
            }
            if (repeatRequest == false) {
                BluetoothIcData cs108BluetoothIcData = new BluetoothIcData();
                cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.BLUETOOTH_GET_VERSION;
                bluetoothIcToWrite.add(cs108BluetoothIcData);
                if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent.toString() + " to mBluetoothIcToWrite with length = " + bluetoothIcToWrite.size());
            }
            return "";
        } else {
            String retValue = String.valueOf(mBluetoothIcVersion[0]) + "." + String.valueOf(mBluetoothIcVersion[1]) + "." + String.valueOf(mBluetoothIcVersion[2]);
            if (DEBUG) appendToLog("mBluetoothIcVersionUpdated is true with data = " + byteArrayToString(mBluetoothIcVersion) + ", icsModel = " + icsModel);
            return retValue;
        }
    }

    public byte[] deviceName = null;
    public String getBluetoothIcName() {
        boolean DEBBUG = false;
        if (DEBBUG) appendToLog("3 deviceName = " + (deviceName == null ? "null" : byteArrayToString(deviceName)));
        if (deviceName == null) {
            boolean repeatRequest = false;
            if (DEBBUG) appendToLog("3A mBluetoothIcToWrite.size = " + bluetoothIcToWrite.size());
            if (bluetoothIcToWrite.size() != 0) {
                if (bluetoothIcToWrite.get(bluetoothIcToWrite.size() - 1).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.BLUETOOTH_GET_DEVICE_NAME) {
                    repeatRequest = true;
                }
            }
            if (DEBBUG) appendToLog("3b repeatRequest = " + repeatRequest);
            if (repeatRequest == false) {
                BluetoothIcData cs108BluetoothIcData = new BluetoothIcData();
                cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.BLUETOOTH_GET_DEVICE_NAME;
                bluetoothIcToWrite.add(cs108BluetoothIcData);
                if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent.toString() + " to mBluetoothIcToWrite with length = " + bluetoothIcToWrite.size());
            }
            return "";
        } else {
            return new String(deviceName).trim();
        }
    }
    public boolean setBluetoothIcName(String name) {
        if (name == null)   return false;
        if (name.length() == 0) return  false;
        if (name.length() > 20) return false;
        BluetoothIcData cs108BluetoothIcData = new BluetoothIcData();
        cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.BLUETOOTH_SET_DEVICE_NAME;
        if (DEBUG) appendToLog("deviceName.length = " + deviceName.length + ", name.getBytes = " + byteArrayToString(name.getBytes()));
        cs108BluetoothIcData.dataValues = name.getBytes();
        if (bluetoothIcToWrite.add(cs108BluetoothIcData) == false) return false;
        deviceName = name.getBytes();
        return true;
    }

    public boolean forceBTdisconnect() {
        BluetoothIcData cs108BluetoothIcData = new BluetoothIcData();
        cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.BLUETOOTH_FORCE_BT_DISCONNECT;
        if (bluetoothIcToWrite.add(cs108BluetoothIcData) == false) return false;
        return true;
    }

    public ArrayList<BluetoothIcData> bluetoothIcToWrite = new ArrayList<>();
    private ArrayList<BluetoothIcData> mBluetoothIcToRead = new ArrayList<>();

    private boolean arrayTypeSet(byte[] dataBuf, int pos, BluetoothIcPayloadEvents event) {
        boolean validEvent = false;
        switch (event) {
            case BLUETOOTH_GET_VERSION:
                validEvent = true;
                break;
            case BLUETOOTH_SET_DEVICE_NAME:
                dataBuf[pos] = 3;
                validEvent = true;
                break;
            case BLUETOOTH_GET_DEVICE_NAME:
                dataBuf[pos] = 4;
                validEvent = true;
                break;
            case BLUETOOTH_FORCE_BT_DISCONNECT:
                dataBuf[pos] = 5;
                validEvent = true;
                break;
        }
        return validEvent;
    }

    private byte[] writeBluetoothIc(BluetoothIcData data) {
        int datalength = 0;
        if (DEBUG) appendToLog("data.bluetoothIcPayloadEvent=" + data.bluetoothIcPayloadEvent.toString() + ", data.dataValues=" + byteArrayToString(data.dataValues));
        if (data.dataValues != null)    datalength = data.dataValues.length;
        byte[] dataOutRef = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0x5F, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xC0, 0};
        byte[] dataOut = new byte[10 + datalength];
        if (datalength != 0)    {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
            dataOutRef[2] += datalength;
        }
        System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);
        if (DEBUG) appendToLog("dataOut=" + byteArrayToString(dataOut));
        if (arrayTypeSet(dataOut, 9, data.bluetoothIcPayloadEvent)) {
            if (data.bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.BLUETOOTH_SET_DEVICE_NAME && data.dataValues.length < 21) {
                byte[] dataOut1 = new byte[10+21];
                System.arraycopy(dataOut, 0, dataOut1, 0, dataOut.length);
                dataOut1[2] = 23;
                dataOut = dataOut1;
            }
            if (DEBUG) appendToLog(byteArrayToString(dataOut));
            return dataOut;
        }
        return null;
    }

    public boolean isMatchBluetoothIcToWrite(ConnectorData connectorData) {
        boolean match = false;
        if (bluetoothIcToWrite.size() != 0 && connectorData.dataValues[0] == (byte)0xC0) {
            byte[] dataInCompare = new byte[]{(byte) 0xC0, 0};
            if (arrayTypeSet(dataInCompare, 1, bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent) && (connectorData.dataValues.length >= dataInCompare.length + 1)) {
                if (match = compareArray(connectorData.dataValues, dataInCompare, dataInCompare.length)) {
                    boolean bprocessed = false;
                    byte[] data1 = new byte[connectorData.dataValues.length - 2]; System.arraycopy(connectorData.dataValues, 2, data1, 0, data1.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: matched BluetoothIc.Reply with payload = " + byteArrayToString(connectorData.dataValues) + " for writeData BluetoothIc." + bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString());
                    if (bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.BLUETOOTH_GET_VERSION) {
                        if (connectorData.dataValues.length > 2) {
                            int length = mBluetoothIcVersion.length;
                            if (connectorData.dataValues.length - 2 < length) length = connectorData.dataValues.length - 2;
                            System.arraycopy(connectorData.dataValues, 2, mBluetoothIcVersion, 0, length);
                            if (mBluetoothIcVersion[0] == 3) icsModel = 463;
                            else if (mBluetoothIcVersion[0] == 1) icsModel = 108;
                            mBluetoothIcVersionUpdated = true;
                            if (DEBUG) appendToLog("mBluetoothIcVersionUpdated is true");
                            bprocessed = true;
                        }
                        if (DEBUG_PKDATA) appendToLog("PkData: matched BluetoothIc.Reply.GetVersion with version = " + byteArrayToString(mBluetoothIcVersion));
                    } else if (bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.BLUETOOTH_GET_DEVICE_NAME) {
                        if (connectorData.dataValues.length > 2) {
                            byte[] deviceName1 = new byte[connectorData.dataValues.length - 2];
                            System.arraycopy(connectorData.dataValues, 2, deviceName1, 0, connectorData.dataValues.length - 2);
                            deviceName = deviceName1;
                            bprocessed = true;
                        }
                        if (DEBUG_PKDATA) appendToLog("PkData: matched mBluetoothIc.GetDeviceName.Reply data is found with name=" + byteArrayToString(deviceName) + ", dataValues.length=" + connectorData.dataValues.length + ", deviceName.length=" + deviceName.length);
                    } else {
                        bprocessed = true;
                        if (DEBUG) appendToLog("matched mBluetoothIc.Other.Reply data is found.");
                    }
                    String string = "Up3  " + (bprocessed ? "" : "Unprocessed, ") + bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString() + ", " + byteArrayToString(data1);
                    utility.writeDebug2File(string);
                    bluetoothIcToWrite.remove(0); sendDataToWriteSent = 0;
                    if (DEBUG_PKDATA) appendToLog("PkData: new mBluetoothIcToWrite size = " + bluetoothIcToWrite.size());
                }
            }
        }
        return match;
    }

    public int sendDataToWriteSent = 0;
    boolean bluetoothFailure = false;
    public byte[] sendBluetoothIcToWrite() {
        if (bluetoothFailure) {
            bluetoothIcToWrite.remove(0); sendDataToWriteSent = 0;
        } else if (sendDataToWriteSent >= 5) {
            int oldSize = bluetoothIcToWrite.size();
            bluetoothIcToWrite.remove(0); sendDataToWriteSent = 0;
            if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mBluetoothIcToWrite.size() = " + bluetoothIcToWrite.size());
            if (DEBUG) appendToLog("Removed after sending count-out.");
            String string = "Problem in sending data to Bluetooth Module. Removed data sending after count-out";
            if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
            else appendToLogView(string);
            bluetoothFailure = true;
        } else {
            if (DEBUG) appendToLog("size = " + bluetoothIcToWrite.size() + ", PayloadEvents = " + bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString());
            sendDataToWriteSent++;
            return writeBluetoothIc(bluetoothIcToWrite.get(0));
        }
        return null;
    }

    void addBluetoothToWrite(BluetoothIcData cs108BluetoothIcData) {
        boolean repeatRequest = false;
        if (bluetoothIcToWrite.size() != 0) {
            BluetoothIcData cs108BluetoothIcData1 = bluetoothIcToWrite.get(bluetoothIcToWrite.size() - 1);
            if (cs108BluetoothIcData.bluetoothIcPayloadEvent == cs108BluetoothIcData1.bluetoothIcPayloadEvent) {
                if (cs108BluetoothIcData.dataValues == null && cs108BluetoothIcData1.dataValues == null) {
                    repeatRequest = true;
                } else if (cs108BluetoothIcData.dataValues != null && cs108BluetoothIcData1.dataValues != null) {
                    if (cs108BluetoothIcData.dataValues.length == cs108BluetoothIcData1.dataValues.length) {
                        if (compareArray(cs108BluetoothIcData.dataValues, cs108BluetoothIcData1.dataValues, cs108BluetoothIcData.dataValues.length)) {
                            repeatRequest = true;
                        }
                    }
                }
            }
        }
        if (repeatRequest == false) {
            bluetoothIcToWrite.add(cs108BluetoothIcData);
            appendToLog("2b GET_DEVICE_NAME");
            if (DEBUG_PKDATA) appendToLog("add " + cs108BluetoothIcData.bluetoothIcPayloadEvent.toString() + " to mBluetoothIcToWrite with length = " + bluetoothIcToWrite.size());
        }
    }
}
