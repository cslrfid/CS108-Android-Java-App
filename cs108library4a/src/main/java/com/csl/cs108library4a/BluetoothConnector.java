package com.csl.cs108library4a;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothConnector {
    final boolean DEBUG = false;

    Context mContext; TextView mLogView;
    BluetoothConnector(Context context, TextView mLogView) {
        mContext = context;
        this.mLogView = mLogView;
        utility = new Utility(context, mLogView);
        mBluetoothIcDevice = new BluetoothIcDevice();
    }

    Utility utility;
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }

    private int icsModel = -1;
    int getCsModel() {
        if (false) appendToLog("icsModel = " + icsModel);
        return icsModel;
    }

    enum BluetoothIcPayloadEvents {
        GET_VERSION, SET_DEVICE_NAME, GET_DEVICE_NAME, FORCE_BT_DISCONNECT
    }

    static class Cs108BluetoothIcData {
        BluetoothIcPayloadEvents bluetoothIcPayloadEvent;
        byte[] dataValues;
    }

    class BluetoothIcDevice {
        private byte[] mBluetoothIcVersion = new byte[]{-1, -1, -1};
        private boolean mBluetoothIcVersionUpdated = false;
        String getBluetoothIcVersion() {
            if (mBluetoothIcVersionUpdated == false) {
                appendToLog("mBluetoothIcVersionUpdated is false");
                boolean repeatRequest = false;
                if (mBluetoothIcToWrite.size() != 0) {
                    if (mBluetoothIcToWrite.get(mBluetoothIcToWrite.size() - 1).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_VERSION) {
                        repeatRequest = true;
                    }
                }
                if (repeatRequest == false) {
                    Cs108BluetoothIcData cs108BluetoothIcData = new Cs108BluetoothIcData();
                    cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.GET_VERSION;
                    mBluetoothIcToWrite.add(cs108BluetoothIcData);
                }
                return "";
            } else {
                String retValue = String.valueOf(mBluetoothIcVersion[0]) + "." + String.valueOf(mBluetoothIcVersion[1]) + "." + String.valueOf(mBluetoothIcVersion[2]);
                appendToLog("mBluetoothIcVersionUpdated is true with data = " + byteArrayToString(mBluetoothIcVersion) + ", icsModel = " + icsModel);
                return retValue;
            }
        }

        byte[] deviceName = null;
        String getBluetoothIcName() {
            if (deviceName == null) {
                boolean repeatRequest = false;
                if (mBluetoothIcToWrite.size() != 0) {
                    if (mBluetoothIcToWrite.get(mBluetoothIcToWrite.size() - 1).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_DEVICE_NAME) {
                        repeatRequest = true;
                    }
                }
                if (repeatRequest == false) {
                    Cs108BluetoothIcData cs108BluetoothIcData = new Cs108BluetoothIcData();
                    cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.GET_DEVICE_NAME;
                    mBluetoothIcToWrite.add(cs108BluetoothIcData);
                }
                return "";
            } else {
                return new String(deviceName).trim();
            }
        }
        boolean setBluetoothIcName(String name) {
            if (name == null)   return false;
            if (name.length() == 0) return  false;
            if (name.length() > 20) return false;
            Cs108BluetoothIcData cs108BluetoothIcData = new Cs108BluetoothIcData();
            cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.SET_DEVICE_NAME;
            if (DEBUG) appendToLog("deviceName.length = " + deviceName.length + ", name.getBytes = " + byteArrayToString(name.getBytes()));
            cs108BluetoothIcData.dataValues = name.getBytes();
            if (mBluetoothIcToWrite.add(cs108BluetoothIcData) == false) return false;
            deviceName = name.getBytes();
            return true;
        }

        boolean forceBTdisconnect() {
            Cs108BluetoothIcData cs108BluetoothIcData = new Cs108BluetoothIcData();
            cs108BluetoothIcData.bluetoothIcPayloadEvent = BluetoothIcPayloadEvents.FORCE_BT_DISCONNECT;
            if (mBluetoothIcToWrite.add(cs108BluetoothIcData) == false) return false;
            return true;
        }

        public ArrayList<Cs108BluetoothIcData> mBluetoothIcToWrite = new ArrayList<>();
        private ArrayList<Cs108BluetoothIcData> mBluetoothIcToRead = new ArrayList<>();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, BluetoothIcPayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case GET_VERSION:
                    validEvent = true;
                    break;
                case SET_DEVICE_NAME:
                    dataBuf[pos] = 3;
                    validEvent = true;
                    break;
                case GET_DEVICE_NAME:
                    dataBuf[pos] = 4;
                    validEvent = true;
                    break;
                case FORCE_BT_DISCONNECT:
                    dataBuf[pos] = 5;
                    validEvent = true;
                    break;
            }
            return validEvent;
        }

        private byte[] writeBluetoothIc(Cs108BluetoothIcData data) {
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
                if (data.bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.SET_DEVICE_NAME && data.dataValues.length < 21) {
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

        boolean isMatchBluetoothIcToWrite(Cs108Connector.Cs108ReadData cs108ReadData) {
            boolean match = false;
            if (mBluetoothIcToWrite.size() != 0) {
                appendToLog(" mBluetoothIcToWrite.size = " + mBluetoothIcToWrite.size());
                byte[] dataInCompare = new byte[]{(byte) 0xC0, 0};
                if (arrayTypeSet(dataInCompare, 1, mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent) && (cs108ReadData.dataValues.length >= dataInCompare.length + 1)) {
                    if (match = compareArray(cs108ReadData.dataValues, dataInCompare, dataInCompare.length)) {
                        if (true) appendToLog("found BluetoothIc.read data = " + byteArrayToString(cs108ReadData.dataValues) + ", bluetoothIcPayloadEvent = " + mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent);
                        if (mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_VERSION) {
                            if (cs108ReadData.dataValues.length > 2) {
                                int length = mBluetoothIcVersion.length;
                                if (cs108ReadData.dataValues.length - 2 < length) length = cs108ReadData.dataValues.length - 2;
                                System.arraycopy(cs108ReadData.dataValues, 2, mBluetoothIcVersion, 0, length);
                                if (mBluetoothIcVersion[0] == 3) icsModel = 463;
                                else if (mBluetoothIcVersion[0] == 1) icsModel = 108;
                                mBluetoothIcVersionUpdated = true;
                                if (true) appendToLog("mBluetoothIcVersionUpdated is true");
                            }
                            if (DEBUG) appendToLog("matched mBluetoothIc.GetVersion.Reply data is found with version=" + byteArrayToString(mBluetoothIcVersion));
                            if (DEBUG) if (mBluetoothIcVersion[0] == -1) appendToLog("mBluetoothIcVersion[0] == -1");
                            if (DEBUG) appendToLog("the value is " + String.valueOf(mBluetoothIcVersion[0]));
                        } else if (mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.GET_DEVICE_NAME) {
                            if (cs108ReadData.dataValues.length > 2) {
                                byte[] deviceName1 = new byte[cs108ReadData.dataValues.length - 2];
                                System.arraycopy(cs108ReadData.dataValues, 2, deviceName1, 0, cs108ReadData.dataValues.length - 2);
                                deviceName = deviceName1;
                            }
                            if (true) appendToLog("matched mBluetoothIc.SetDeviceName.Reply data is found with name=" + byteArrayToString(deviceName) + ", dataValues.length=" + cs108ReadData.dataValues.length + ", deviceName.length=" + deviceName.length);
                        } else if (mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.SET_DEVICE_NAME) {
                            if (cs108ReadData.dataValues.length >= 3) {
                                if (cs108ReadData.dataValues[2] != 0) {
                                    //do if false
                                }
                            }
                            if (DEBUG) appendToLog("matched mBluetoothIc/.SetDeviceName.Reply data is found.");
                        } else if (mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent == BluetoothIcPayloadEvents.FORCE_BT_DISCONNECT) {
                            if (cs108ReadData.dataValues.length >= 3) {
                                if (cs108ReadData.dataValues[2] != 0) {
                                    //do if false
                                }
                            }
                            if (DEBUG) appendToLog("matched mBluetoothIc.ForceBTDisconnect.Reply data is found.");
                        } else {
                            if (DEBUG) appendToLog("matched mBluetoothIc.Other.Reply data is found.");
                        }
                        mBluetoothIcToWrite.remove(0); sendDataToWriteSent = 0;
                    }
                }
            }
            return match;
        }

        public int sendDataToWriteSent = 0;
        public byte[] sendBluetoothIcToWrite() {
            if (sendDataToWriteSent >= 5) {
                int oldSize = mBluetoothIcToWrite.size();
                mBluetoothIcToWrite.remove(0); sendDataToWriteSent = 0;
                if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mBluetoothIcToWrite.size() = " + mBluetoothIcToWrite.size());
                if (DEBUG) appendToLog("Removed after sending count-out.");
                Toast.makeText(mContext, "Problem in sending data to Bluetooth Module. Removed data sending after count-out", Toast.LENGTH_SHORT).show();
            } else {
                if (DEBUG) appendToLog("size = " + mBluetoothIcToWrite.size() + ", PayloadEvents = " + mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString());
                return writeBluetoothIc(mBluetoothIcToWrite.get(0));
            }
            return null;
        }

        void addBluetoothToWrite(Cs108BluetoothIcData cs108BluetoothIcData) {
            boolean repeatRequest = false;
            if (mBluetoothIcDevice.mBluetoothIcToWrite.size() != 0) {
                Cs108BluetoothIcData cs108BluetoothIcData1 = mBluetoothIcDevice.mBluetoothIcToWrite.get(mBluetoothIcDevice.mBluetoothIcToWrite.size() - 1);
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
                mBluetoothIcDevice.mBluetoothIcToWrite.add(cs108BluetoothIcData);
            }
        }
    }
    BluetoothIcDevice mBluetoothIcDevice;
}
