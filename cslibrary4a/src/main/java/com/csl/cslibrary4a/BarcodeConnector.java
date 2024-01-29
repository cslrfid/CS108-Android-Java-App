package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class BarcodeConnector {
    boolean DEBUG_PKDATA;
    public boolean userDebugEnableDefault = false;
    public boolean userDebugEnable = userDebugEnableDefault;

    Context context; TextView mLogView;
    public BarcodeConnector(Context context, TextView mLogView) {
        this.context = context;
        this.mLogView = mLogView;
        utility = new Utility(context, mLogView); DEBUG_PKDATA = utility.DEBUG_PKDATA;
    }

    Utility utility;
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }
    public enum BarcodePayloadEvents {
        BARCODE_NULL,
        BARCODE_POWER_ON, BARCODE_POWER_OFF, BARCODE_SCAN_START, BARCODE_COMMAND, BARCODE_VIBRATE_ON, BARCODE_VIBRATE_OFF,
        BARCODE_DATA_READ, BARCODE_GOOD_READ,
    }
    public enum BarcodeCommandTypes {
        COMMAND_COMMON, COMMAND_SETTING, COMMAND_QUERY
    }
    public static class CsReaderBarcodeData {
        public boolean waitUplinkResponse = false;
        boolean downlinkResponsed = false;
        public BarcodePayloadEvents barcodePayloadEvent;
        public byte[] dataValues;
    }

    private boolean onStatus = false; public boolean getOnStatus() { return onStatus; }
    private boolean vibrateStatus = false; boolean getVibrateStatus() { return vibrateStatus; }
    private String strVersion, strESN, strSerialNumber, strDate;
    public String getVersion() { return strVersion; }
    public String getESN() { return strESN; }
    public String getSerialNumber() { return strSerialNumber; }
    public String getDate() { return strDate; }
    public byte[] bytesBarcodePrefix = null;
    public byte[] bytesBarcodeSuffix = null;
    public byte[] getPrefix() { return bytesBarcodePrefix; }
    public byte[] getSuffix() { return bytesBarcodeSuffix; }
    public boolean checkPreSuffix(byte[] prefix1, byte[] suffix1) {
        boolean result = false;
        if (prefix1 != null && bytesBarcodePrefix != null && suffix1 != null && bytesBarcodeSuffix != null) {
            result = Arrays.equals(prefix1, bytesBarcodePrefix);
            if (result) result = Arrays.equals(suffix1, bytesBarcodeSuffix);
        }
        return result;
    }

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
    public boolean isMatchBarcodeToWrite(CsReaderData csReaderData) {
        boolean match = false, DEBUG = false;
        if (barcodeToWrite.size() != 0 && csReaderData.dataValues[0] == (byte)0x90) {
            if (DEBUG) appendToLog("csReadData = " + byteArrayToString(csReaderData.dataValues));
            //if (DEBUG) appendToLog("tempDisconnect: icsModel = " + bluetoothConnector.getCsModel() + ", mBarcodeToWrite.size = " + mBarcodeToWrite.size());
            if (barcodeToWrite.size() != 0) if (DEBUG) appendToLog("mBarcodeToWrite(0) = " + barcodeToWrite.get(0).barcodePayloadEvent.toString() + "," + byteArrayToString(barcodeToWrite.get(0).dataValues));
            byte[] dataInCompare = new byte[]{(byte) 0x90, 0};
            if (arrayTypeSet(dataInCompare, 1, barcodeToWrite.get(0).barcodePayloadEvent) && (csReaderData.dataValues.length == dataInCompare.length + 1)) {
                if (match = compareArray(csReaderData.dataValues, dataInCompare, dataInCompare.length)) {
                    boolean bprocessed = false;
                    byte[] data1 = new byte[csReaderData.dataValues.length - 2]; System.arraycopy(csReaderData.dataValues, 2, data1, 0, data1.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: matched Barcode.Reply with payload = " + byteArrayToString(csReaderData.dataValues) + " for writeData Barcode." + barcodeToWrite.get(0).barcodePayloadEvent.toString());
                    if (csReaderData.dataValues[2] != 0) {
                        if (DEBUG) appendToLog("Barcode.reply data is found with error");
                    } else if (true) { //testing bluetoothConnector.getCsModel() == 108) {
                        if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_ON) {
                            barcodePowerOnTimeOut = 1000;
                            if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_ON");
                            onStatus = true;
                            if (DEBUG_PKDATA | csReaderData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOn with result = " + csReaderData.dataValues[2] + " and onStatus = " + onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_OFF) {
                            if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_OFF");
                            onStatus = false;
                            if (DEBUG_PKDATA | csReaderData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOff with result = " + csReaderData.dataValues[2] + " and onStatus = " + onStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_ON) {
                            vibrateStatus = true;
                            if (DEBUG_PKDATA | csReaderData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOn with result = " + csReaderData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_OFF) {
                            vibrateStatus = false;
                            if (DEBUG_PKDATA | csReaderData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOff with result = " + csReaderData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            bprocessed = true;
                        } else if (barcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_COMMAND) {
                            barcodePowerOnTimeOut = 500;
                            if (DEBUG_PKDATA | csReaderData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.Command with result = " + csReaderData.dataValues[2] + " and barcodePowerOnTimeOut = " + barcodePowerOnTimeOut);
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
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + barcodeToWrite.size());
                }
            }
        }
        return match;
    }

    private int sendDataToWriteSent = 0; public boolean mDataToWriteRemoved = false;
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
                if (true || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_SCAN_START || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_COMMAND) isBarcodeData = true;
                if (barcodeFailure && isBarcodeData) {
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                } else if (sendDataToWriteSent >= 5 && isBarcodeData) {
                    int oldSize = barcodeToWrite.size();
                    barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
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
                        barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    }
                    return true;*/
                }
            //}
        //}
        return null;
    }

    public byte bBarcodeTriggerMode = (byte)0xff;
    int iOkCount = 0;
    public boolean isBarcodeToRead(CsReaderData csReaderData) {
        boolean found = false, DEBUG = false;

        if (csReaderData.dataValues[0] == (byte) 0x91) {
            if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink with payload = " + byteArrayToString(csReaderData.dataValues));
            CsReaderBarcodeData csReaderBarcodeData = new CsReaderBarcodeData();
            switch (csReaderData.dataValues[1]) {
                case 0:
                    csReaderBarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_DATA_READ;
                    byte[] dataValues = new byte[csReaderData.dataValues.length - 2];
                    System.arraycopy(csReaderData.dataValues, 2, dataValues, 0, dataValues.length);
                    if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead with payload = " + byteArrayToString(dataValues));
                    BarcodeCommandTypes commandType = null;
                    if (barcodeToWrite.size() > 0) {
                        if (barcodeToWrite.get(0).downlinkResponsed) {
                            int count = 0; boolean matched = true;
                            if (barcodeToWrite.get(0).dataValues[0] == 0x1b) {
                                commandType = BarcodeCommandTypes.COMMAND_COMMON;
                                count = 1;
                                if (DEBUG) appendToLog("0x1b, Common response with  count = " + count);
                            } else if (barcodeToWrite.get(0).dataValues[0] == 0x7E) {
                                if (DEBUG) appendToLog("0x7E, Barcode response with 0x7E mBarcodeToWrite.get(0).dataValues[0] and response data = " + byteArrayToString(dataValues));
                                matched = true;
                                commandType = BarcodeCommandTypes.COMMAND_QUERY;
                                int index = 0;
                                while (dataValues.length - index >= 5 + 1) {
                                    if (dataValues[index+0] == 2 && dataValues[index+1] == 0 && dataValues[index+4] == 0x34) {
                                        int length = dataValues[index+2] * 256 + dataValues[index+3];
                                        if (dataValues.length - index >= length + 4 + 1) {
                                            matched = true;
                                            byte[] bytes = new byte[length-1];
                                            System.arraycopy(dataValues, index + 5, bytes, 0, bytes.length);
                                            byte[] requestBytes = new byte[barcodeToWrite.get(0).dataValues.length - 6];
                                            System.arraycopy(barcodeToWrite.get(0).dataValues, 5, requestBytes, 0, requestBytes.length);
                                            if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead.QueryResponse with payload data1 = " + byteArrayToString(bytes) + " for QueryInput data1 = " + byteArrayToString(requestBytes));
                                            if (barcodeToWrite.get(0).dataValues[5] == 0x37 && length >= 5) {
                                                matched = true;
                                                int prefixLength = dataValues[index+6];
                                                int suffixLength = 0;
                                                if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + 1) {
                                                    suffixLength = dataValues[index + 6 + prefixLength + 2];
                                                }
                                                if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + suffixLength + 1) {
                                                    bytesBarcodePrefix = null;
                                                    bytesBarcodeSuffix = null;
                                                    if (dataValues[index+5] == 1) {
                                                        bytesBarcodePrefix = new byte[prefixLength];
                                                        System.arraycopy(dataValues, index + 7, bytesBarcodePrefix, 0, bytesBarcodePrefix.length);
                                                    }
                                                    if (dataValues[index + 6 + prefixLength + 1] == 1) {
                                                        bytesBarcodeSuffix = new byte[suffixLength];
                                                        System.arraycopy(dataValues, index + 7 + prefixLength + 2, bytesBarcodeSuffix, 0, bytesBarcodeSuffix.length);
                                                    }
                                                    if (DEBUG) appendToLog("BarStream: BarcodePrefix = " + byteArrayToString(bytesBarcodePrefix) + ", BarcodeSuffix = " + byteArrayToString(bytesBarcodeSuffix));
                                                }
                                                if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead.QueryResponse.SelfPrefix_SelfSuffix is processed as Barcode Prefix = " + byteArrayToString(bytesBarcodePrefix) + ", Suffix = " + byteArrayToString(bytesBarcodeSuffix));
                                            } else if (barcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
                                                if (DEBUG) appendToLog("versionNumber is detected with length = " + length);
                                                matched = true;
                                                byte[] byteVersion = new byte[length - 1];
                                                System.arraycopy(dataValues, index + 5, byteVersion, 0, byteVersion.length);
                                                String versionNumber;
                                                try {
                                                    versionNumber = new String(byteVersion, "UTF-8");
                                                } catch (Exception e) {
                                                    versionNumber = null;
                                                }
                                                strVersion = versionNumber;
                                                if (DEBUG_PKDATA) appendToLog("PkData: uplink data " + byteArrayToString(byteVersion) + " is processsed as version = " + versionNumber);
                                            } else if (barcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                                                if (dataValues[index+5] == barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeToWrite.get(0).dataValues[7]) {
                                                    matched = true; //for ESN, S/N or Date
                                                    byte[] byteSN = new byte[length - 3];
                                                    System.arraycopy(dataValues, index + 7, byteSN, 0, byteSN.length);
                                                    String serialNumber;
                                                    try {
                                                        serialNumber = new String(byteSN, "UTF-8");
                                                        int snLength = Integer.parseInt(serialNumber.substring(0, 2));
                                                        if (DEBUG)
                                                            appendToLog("BarStream: serialNumber = " + serialNumber + ", snLength = " + snLength + ", serialNumber.length = " + serialNumber.length());
                                                        if (snLength + 2 == serialNumber.length()) {
                                                            serialNumber = serialNumber.substring(2);
                                                        } else serialNumber = null;
                                                    } catch (Exception e) {
                                                        serialNumber = null;
                                                    }
                                                    if (false) appendToLog("debug index = " + index + ", " + byteArrayToString(dataValues));
                                                    String strResponseType = "";
                                                    if (dataValues[index+6] == (byte)0x32) {
                                                        strESN = serialNumber;
                                                        strResponseType = "EquipmentSerialNumber";
                                                    } else if (dataValues[index+6] == (byte)0x33) {
                                                        strSerialNumber = serialNumber;
                                                        strResponseType = "SerialNumber";
                                                    } else if (dataValues[index+6] == (byte)0x34) {
                                                        strDate = serialNumber;
                                                        strResponseType = "DataCode";
                                                    }
                                                    if (false) appendToLog("strResponseType = " + strResponseType);
                                                    if (DEBUG_PKDATA) appendToLog(String.format("PkData: Barcode.Uplink.DataRead.QueryResponse.%s is processed as %s[%s]", strResponseType, byteArrayToString(byteSN).substring(4), serialNumber));
                                                } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                            } else if (barcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                                                if (DEBUG) appendToLog("BarStream: dataValue = " + byteArrayToString(dataValues) + ", writeDataValue = " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                                if (dataValues[index+5] == barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeToWrite.get(0).dataValues[7]) {
                                                    matched = true;
                                                    if (barcodeToWrite.get(0).dataValues[6] == 0x30 && barcodeToWrite.get(0).dataValues[7] == 0x30  && barcodeToWrite.get(0).dataValues[8] == 0x30) {
                                                        bBarcodeTriggerMode = dataValues[7];
                                                        String strModeType = "";
                                                        if (dataValues[index+7] == 0x30) strModeType = "trigger";
                                                        else if (dataValues[index+7] == 0x31) strModeType = "auto_Scan";
                                                        else if (dataValues[index+7] == 0x32) strModeType = "continue_Scan";
                                                        else if (dataValues[index+7] == 0x33) strModeType = "batch_Scan";
                                                        if (DEBUG_PKDATA) appendToLog(String.format("PkData: Barcode.Uplink.DataRead.QueryResponse.ReadingMode is processed as last 0x%X[%s]", dataValues[index+7], strModeType));
                                                    } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                                } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                            } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                            index += (length + 5);
                                        } else break;
                                    } else index++;
                                }
                                if (matched) { if (DEBUG) appendToLog("Matched Query response"); }
                                else { if (DEBUG) appendToLog("Mis-matched Query response"); }
                            } else {
                                if (DEBUG) appendToLog("BarStream: Barcode response with mBarcodeToWrite.get(0).dataValues[0] =  Others");
                                String strData = null;
                                try {
                                    strData = new String(barcodeToWrite.get(0).dataValues, "UTF-8");
                                } catch (Exception ex) {
                                    strData = "";
                                }
                                String findStr = "nls";
                                int lastIndex = 0;
                                while (lastIndex != -1) {
                                    lastIndex = strData.indexOf(findStr, lastIndex);
                                    if (lastIndex != -1) {
                                        count++;
                                        lastIndex += findStr.length();
                                    }
                                }
                                if (DEBUG) appendToLog("Setting strData = " + strData + ", count = " + count);
                            }
                            if (count != 0) {
                                if (false) appendToLog("dataValues.length = " + dataValues.length + ", okCount = " + iOkCount + ", count = " + count + " for mBarcodeToWrite data = " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                matched = false; boolean foundOk = false;
                                for (int k = 0; k < dataValues.length; k++) {
                                    boolean match06 = false;
                                    if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++iOkCount == count) matched = true; }
                                    if (match06 == false) break;
                                    foundOk = true; found = true;
                                }
                                if (false) appendToLog("00 matcched = " + matched);
                                if (matched) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeToWrite.get(0).dataValues)); }
                                else if (foundOk) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", but OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeToWrite.get(0).dataValues)); }
                                else {
                                    mBarcodeToRead.add(csReaderBarcodeData);
                                    if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.DataRead." + byteArrayToString(csReaderBarcodeData.dataValues) + " is added to mBarcodeToRead");
                                }
                            }
                            if (matched) {
                                found = true;
                                barcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                                if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + barcodeToWrite.size());
                            }
                            break;
                        }
                    }
                    for (int i=0; false && commandType == null && i < dataValues.length; i++) {
                        if (dataValues[i] == 0x28 || dataValues[i] == 0x29    //  ( )
                                || dataValues[i] == 0x5B || dataValues[i] == 0x5D || dataValues[i] == 0x5C
                                || dataValues[i] == 0x7B || dataValues[i] == 0x7D
                        ) dataValues[i] = 0x20;
                    }
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
        if (found && DEBUG)  appendToLog("found Barcode.read data = " + byteArrayToString(csReaderData.dataValues));
        return found;
    }
}