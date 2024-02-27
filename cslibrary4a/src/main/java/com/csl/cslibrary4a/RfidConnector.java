package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RfidConnector {
    boolean DEBUG_PKDATA;
    Context context; TextView mLogView;
    public RfidConnector(Context context, TextView mLogView, Utility utility) {
        this.context = context;
        this.mLogView = mLogView;
        this.utility = utility; DEBUG_PKDATA = utility.DEBUG_PKDATA;
    }

    Utility utility;
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }

    public enum RfidPayloadEvents {
        RFID_POWER_ON, RFID_POWER_OFF, RFID_COMMAND,
        RFID_DATA_READ
    }
    public static class CsReaderRfidData {
        public boolean waitUplinkResponse = false;
        public boolean downlinkResponded = false;

        public boolean uplinkResponded = false;
        public boolean waitUplink1Response = false;

        public RfidPayloadEvents rfidPayloadEvent;
        public byte[] dataValues;
        public boolean invalidSequence;
        public long milliseconds;
    }

    public boolean onStatus = false; public boolean getOnStatus() { return onStatus; }

    public interface RfidConnectorCallback {
        boolean callbackMethod(byte[] dataValues);
    }
    public RfidConnectorCallback rfidConnectorCallback = null;

    public ArrayList<CsReaderRfidData> mRfidToWrite = new ArrayList<>();
    public ArrayList<CsReaderRfidData> mRfidToRead = new ArrayList<>();

    public boolean arrayTypeSet(byte[] dataBuf, int pos, RfidPayloadEvents event) {
        boolean validEvent = false;
        switch (event) {
            case RFID_POWER_ON:
                validEvent = true;
                break;
            case RFID_POWER_OFF:
                dataBuf[pos] = 1;
                validEvent = true;
                break;
            case RFID_COMMAND:
                dataBuf[pos] = 2;
                validEvent = true;
                break;
        }
        return validEvent;
    }
    public byte[] writeRfid(RfidConnector.CsReaderRfidData data) {
        int datalength = 0;
        if (data.dataValues != null)    datalength = data.dataValues.length;
        byte[] dataOutRef = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xC2, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0x80, 0};

        byte[] dataOut = new byte[10 + datalength];
        if (datalength != 0)    {
            System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
            dataOutRef[2] += datalength;
        }
        System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);
            /*if (data.rfidPayloadEvent == RfidConnector.RfidPayloadEvents.RFID_COMMAND) {
                if (data.dataValues != null) {
                    byte[] dataOut1 = new byte[dataOut.length + data.dataValues.length];
                    System.arraycopy(dataOut, 0, dataOut1, 0, dataOut.length);
                    dataOut1[2] += data.dataValues.length;
                    System.arraycopy(data.dataValues, 0, dataOut1, dataOut.length, data.dataValues.length);
                    dataOut = dataOut1;
                }
            }*/
        if (arrayTypeSet(dataOut, 9, data.rfidPayloadEvent)) {
            if (false) appendToLogView(byteArrayToString(dataOut));
            if (DEBUG_PKDATA) appendToLog(String.format("PkData: write Rfid.%s.%s with mRfidDevice.sendRfidToWriteSent = %d", data.rfidPayloadEvent.toString(), byteArrayToString(data.dataValues), sendRfidToWriteSent));
            if (sendRfidToWriteSent != 0) appendToLog("!!! mRfidDevice.sendRfidToWriteSent = " + sendRfidToWriteSent);
            return dataOut;
        }
        return null;
    }
    public int rfidPowerOnTimeOut = 0; //int barcodePowerOnTimeOut = 0;
    public boolean isMatchRfidToWrite(CsReaderData csReaderData) {
        boolean match = false, DEBUG = false;
        if (mRfidToWrite.size() != 0 && csReaderData.dataValues[0] == (byte)0x80) {
            byte[] dataInCompare = new byte[]{(byte) 0x80, 0};
            if (arrayTypeSet(dataInCompare, 1, mRfidToWrite.get(0).rfidPayloadEvent) && (csReaderData.dataValues.length == dataInCompare.length + 1)) {
                if (match = compareArray(csReaderData.dataValues, dataInCompare, dataInCompare.length)) {
                    boolean bprocessed = false;
                    byte[] data1 = new byte[csReaderData.dataValues.length - 2]; System.arraycopy(csReaderData.dataValues, 2, data1, 0, data1.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply with payload = " + byteArrayToString(csReaderData.dataValues) + " for writeData Rfid." + mRfidToWrite.get(0).rfidPayloadEvent.toString() + "." + byteArrayToString(mRfidToWrite.get(0).dataValues));
                    if (csReaderData.dataValues[2] != 0) {
                        if (DEBUG) appendToLog("Rfid.reply data is found with error");
                    } else {
                        if (mRfidToWrite.get(0).rfidPayloadEvent == RfidConnector.RfidPayloadEvents.RFID_POWER_ON) {
                            rfidPowerOnTimeOut = 3000;
                            onStatus = true;
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOn with result 0 and onStatus = " + onStatus);
                            bprocessed = true;
                        } else if (mRfidToWrite.get(0).rfidPayloadEvent == RfidConnector.RfidPayloadEvents.RFID_POWER_OFF) {
                            onStatus = false;
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOff with result 0 and onStatus = " + onStatus);
                            bprocessed = true;
                        } else {
                            bprocessed = true;
                            if (DEBUG_PKDATA) appendToLog("matched Rfid.Other.Reply data is found.");
                        }
                        RfidConnector.CsReaderRfidData csReaderRfidData = mRfidToWrite.get(0);
                        if (csReaderRfidData.waitUplinkResponse) {
                            csReaderRfidData.downlinkResponded = true;
                            mRfidToWrite.set(0, csReaderRfidData);
                            if (DEBUG_PKDATA) appendToLog("PkData: mRfidToWrite.downlinkResponsed is set and waiting uplink data");
                    /*if (false) {
                        for (int i = 0; i < rfidReaderChip.mRfidReaderChip.mRx000ToRead.size(); i++) {
                            if (rfidReaderChip.mRfidReaderChip.mRx000ToRead.get(i).responseType == Cs710Library4A.HostCmdResponseTypes.TYPE_COMMAND_END)
                                if (DEBUG) appendToLog("mRx0000ToRead with COMMAND_END is removed");
                        }
                        if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                    }
                    rfidReaderChip.mRfidReaderChip.mRx000ToRead.clear(); if (DEBUG) appendToLog("mRx000ToRead.clear !!!");*/
                            utility.writeDebug2File("Up31 " + mRfidToWrite.get(0).rfidPayloadEvent.toString() + ", " + byteArrayToString(data1));
                            return true;
                        }
                        if (DEBUG) appendToLog("matched Rfid.reply data is found with mRfidToWrite.size=" + mRfidToWrite.size());
                    }

                    String string = "Up31 " + (bprocessed ? "" : "Unprocessed, ") + mRfidToWrite.get(0).rfidPayloadEvent.toString() + ", " + byteArrayToString(data1);
                    utility.writeDebug2File(string);
                    mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true; if (DEBUG) appendToLog("mmRfidToWrite remove 1 with remained write size = " + mRfidToWrite.size());
                    if (DEBUG_PKDATA) appendToLog("PkData: new mRfidToWrite size = " + mRfidToWrite.size());
                    /*if (false) {
                        for (int i = 0; i < rfidReaderChip.mRfidReaderChip.mRx000ToRead.size(); i++) {
                            if (rfidReaderChip.mRfidReaderChip.mRx000ToRead.get(i).responseType == Cs710Library4A.HostCmdResponseTypes.TYPE_COMMAND_END)
                                if (DEBUG) appendToLog("mRx0000ToRead with COMMAND_END is removed");
                        }
                        if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                    }
                    rfidReaderChip.mRfidReaderChip.mRx000ToRead.clear(); if (DEBUG) appendToLog("mRx000ToRead.clear !!!");*/
                }
            }
        }
        return match;
    }

    public int sendRfidToWriteSent = 0; public boolean mRfidToWriteRemoved = false;
    public boolean rfidFailure = false; public boolean rfidValid = false;
    public byte[] sendRfidToWrite() {
        boolean DEBUG = false;
        boolean bValue = false;
        //if (DEBUG) appendToLog("Timeout: btSendTimeOut = " + btSendTimeOut);
        RfidConnector.RfidPayloadEvents rfidPayloadEvents = mRfidToWrite.get(0).rfidPayloadEvent;
        int sendRfidToWriteSentMax = 5;
        if (rfidPayloadEvents == RfidConnector.RfidPayloadEvents.RFID_COMMAND /*&& mRfidToWrite.get(0).dataValues[0] == 0x40*/) sendRfidToWriteSentMax = 5;
        if (sendRfidToWriteSent >= sendRfidToWriteSentMax) {
            mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true; if (DEBUG) appendToLog("mmRfidToWrite remove 2");
            if (DEBUG) appendToLog("Removed after sending count-out.");
            if (true) {
                appendToLog("Rfdid data transmission failure !!! clear mRfidToWrite buffer !!!");
                utility.writeDebug2File("Down fails to transmit " + byteArrayToString(mRfidToWrite.get(0).dataValues));
                rfidFailure = true;
                mRfidToWrite.clear();
            } else if (rfidValid == false) {
                Toast.makeText(context, "Problem in sending data to Rfid Module. Rfid is disabled.", Toast.LENGTH_SHORT).show();
                rfidFailure = true;
            } /*else {
                Toast.makeText(context, "Problem in Sending Commands to RFID Module.  Bluetooth Disconnected.  Please Reconnect", Toast.LENGTH_SHORT).show();
                appendToLog("disconnect d");
                disconnect();
            }*/
            if (DEBUG) appendToLog("done");
            mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true; if (DEBUG) appendToLog("mmRfidToWrite remove 2");
        } else {
            if (DEBUG)
                appendToLog("size = " + mRfidToWrite.size() + ", PayloadEvents = " + rfidPayloadEvents.toString() + ", data=" + byteArrayToString(mRfidToWrite.get(0).dataValues));
            boolean retValue = false;
            return writeRfid(mRfidToWrite.get(0));
        }
        return null;
    }

    public boolean found;
    public int invalidUpdata;
    public boolean isRfidToRead(CsReaderData csReaderData) {
        boolean DEBUG = false;
        found = false;
        if (csReaderData.dataValues[0] == (byte) 0x81) {
            if (DEBUG_PKDATA) appendToLog("PkData: found Rfid.Uplink with payload = " + byteArrayToString(csReaderData.dataValues));
            RfidConnector.CsReaderRfidData cs108RfidReadData = new RfidConnector.CsReaderRfidData();
            byte[] dataValues = new byte[csReaderData.dataValues.length - 2];
            System.arraycopy(csReaderData.dataValues, 2, dataValues, 0, dataValues.length);
            if (DEBUG_PKDATA) appendToLog("PkData: found Rfid.Uplink.DataRead with payload = " + byteArrayToString(dataValues));
            switch (csReaderData.dataValues[1]) {
                case 0:
                    if (rfidConnectorCallback != null) {
                        if (rfidConnectorCallback.callbackMethod(dataValues)) break;
                    }
                    cs108RfidReadData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_DATA_READ;
                    cs108RfidReadData.dataValues = dataValues;
                    cs108RfidReadData.invalidSequence = csReaderData.invalidSequence;
                    cs108RfidReadData.milliseconds = csReaderData.milliseconds;
                    mRfidToRead.add(cs108RfidReadData);
                    if (DEBUG_PKDATA) appendToLog("PkData: uplink data Rfid.Uplink.DataRead is uploaded to mRfidToRead");
                    found = true;
                    break;
                default:
                    invalidUpdata++;
                    appendToLog("!!! found INVALID Rfid.Uplink with payload = " + byteArrayToString(csReaderData.dataValues));
                    break;
            }
            if (found) {
                String string = "Up32 " + (found ? "" : "Unprocessed, ") + cs108RfidReadData.rfidPayloadEvent.toString() + ", " + byteArrayToString(dataValues);
                utility.writeDebug2File(string);
            }
        }
        return found;
    }
}
