package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.Keep;

import java.util.Arrays;

public class BarcodeNewland {
    boolean DEBUG_PKDATA;
    BarcodeConnector barcodeConnector;
    Utility utility;
    public enum BarcodeCommandTypes {
        COMMAND_COMMON, COMMAND_SETTING, COMMAND_QUERY
    }
    BarcodeCommandTypes commandType;

    Context context; TextView mLogView;
    public BarcodeNewland(Context context, TextView mLogView, BarcodeConnector barcodeConnector, Utility utility) {
        this.context = context;
        this.mLogView = mLogView;
        this.barcodeConnector = barcodeConnector;
        this.utility = utility; DEBUG_PKDATA = utility.DEBUG_PKDATA;
    }

    void appendToLog(String s) { utility.appendToLog(s); }
    String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }

    public boolean barcodeSendCommand(byte[] barcodeCommandData) {
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_COMMAND;
        csReaderBarcodeData.waitUplinkResponse = true;
        csReaderBarcodeData.dataValues = barcodeCommandData;
        barcodeConnector.barcodeToWrite.add(csReaderBarcodeData);
        if (DEBUG_PKDATA) {
            //if (barcodeCommandData[0] == 'n')
            appendToLog("PkData: add " + csReaderBarcodeData.barcodePayloadEvent.toString() + "." + byteArrayToString(csReaderBarcodeData.dataValues) + " to mBarcodeToWrite with length = " + barcodeConnector.barcodeToWrite.size());
        }
        return true;
    }

    public byte bBarcodeTriggerMode = (byte)0xff;
    public boolean barcode2TriggerModeDefault = true, barcode2TriggerMode = barcode2TriggerModeDefault;
    boolean barcodeReadTriggerStart() {
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_SCAN_START;
        csReaderBarcodeData.waitUplinkResponse = false;
        barcode2TriggerMode = false;
        boolean bValue = barcodeConnector.barcodeToWrite.add(csReaderBarcodeData);
        appendToLog("add " + csReaderBarcodeData.barcodePayloadEvent.toString() + " to mBarcodeToWrite with length = " + barcodeConnector.barcodeToWrite.size());
        return bValue;
    }
    public boolean barcodeSendCommandTrigger() {
        boolean retValue = true;
        barcode2TriggerMode = true; bBarcodeTriggerMode = 0x30; if (false) appendToLog("Set trigger reading mode to TRIGGER");
        if (retValue) retValue = barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0302000;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0313000=3000;nls0313010=1000;nls0313040=1000;nls0302000;nls0007010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0001150;nls0006000;".getBytes());
        return retValue;
    }

    public byte[] prefixRef = { 0x02, 0x00, 0x07, 0x10, 0x17, 0x13 };
    public byte[] suffixRef = { 0x05, 0x01, 0x11, 0x16, 0x03, 0x04 };
    public boolean barcodeSendCommandSetPreSuffix() {
        boolean retValue = true;
        appendToLog("BarStream: BarcodePrefix BarcodeSuffix are SET");
        if (retValue) retValue = barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0311010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0317040;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0305010;".getBytes());
        String string = "nls0300000=0x" + byteArrayToString(prefixRef) + ";"; appendToLog("Set Prefix string = " + string);
        if (retValue) retValue = barcodeSendCommand(string.getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0306010;".getBytes());
        string = "nls0301000=0x" + byteArrayToString(suffixRef) + ";"; appendToLog("Set Suffix string = " + string);
        if (retValue) retValue = barcodeSendCommand(string.getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0308030;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0307010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0309010;nls0310010;".getBytes());   //enable terminator, set terminator as 0x0D
        if (retValue) retValue = barcodeSendCommand("nls0502110;".getBytes());
        if (retValue) barcodeSendCommand("nls0001150;nls0006000;".getBytes());
        if (retValue) {
            bytesBarcodePrefix = prefixRef;
            bytesBarcodeSuffix = suffixRef;
        }
        return retValue;
    }

    public boolean barcodeSendCommandResetPreSuffix() {
        boolean retValue = true;
        if (retValue) barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) barcodeSendCommand("nls0311000;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0300000=;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0301000=;".getBytes());
        if (retValue) barcodeSendCommand("nls0006000;".getBytes());
        if (retValue) {
            bytesBarcodePrefix = null;
            bytesBarcodeSuffix = null;
        }
        return retValue;
    }

    boolean barcodeSendCommandLoadUserDefault() {
        boolean retValue = barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0001160;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0006000;".getBytes());
        return retValue;
    }

    public boolean barcodeSendCommandConinuous() {
        boolean retValue = barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0302020;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0006000;".getBytes());
        return retValue;
    }

    boolean barcodeSendQuerySystem() {
        byte[] datatt = new byte[] { 0x7E, 0x01, 0x30, 0x30, 0x30, 0x30, 0x40, 0x5F, 0x5F, 0x5F, 0x3F, 0x3B, 0x03 };
        barcodeSendCommand(datatt);

        byte[] datat = new byte[] { 0x7E, 0x01,
                0x30, 0x30, 0x30, 0x30,
                0x40, 0x51, 0x52, 0x59, 0x53, 0x59, 0x53, 0x2C, 0x50, 0x44, 0x4E, 0x2C, 0x50, 0x53, 0x4E, 0x3B,
                0X03 };
//        return barcodeSendQuery(datat);
        return barcodeSendCommand(datat);
    }
    public boolean barcodeSendCommandItf14Cksum() { return barcodeSendCommand("nls0006010;nls0405100;nls0006000".getBytes()); }

    boolean barcodeSendQuery(byte[] data) {
        byte bytelrc = (byte)0xff;
        for (int i = 2; i < data.length - 1; i++) {
            bytelrc ^= data[i];
        }
        if (false) appendToLog(String.format("BarStream: bytelrc = %02X, last = %02X", (byte)bytelrc, data[data.length-1]));
        data[data.length-1] = bytelrc;
        return barcodeSendCommand(data);
    }

    boolean barcodeSendQueryVersion() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x02,
                0x33, 0x47,
                0 };
        return barcodeSendQuery(data);
    }

    public String getBarcodeVersion() {
        String strValue = getVersion();
        if (strValue == null) barcodeSendQueryVersion();
        return strValue;
    }

    boolean barcodeSendQueryESN() {
        byte[] datat = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x48, 0x30, 0x32, 0x30,
                (byte)0xb2 };
        return barcodeSendQuery(datat);
    }

    public String getBarcodeESN() {
        String strValue = getESN();
        if (strValue == null) barcodeSendQueryESN();
        return strValue;
    }

    boolean barcodeSendQuerySerialNumber() {
        byte[] datat = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x48, 0x30, 0x33, 0x30,
                (byte)0xb2 };
        return barcodeSendQuery(datat);
    }
    public String getBarcodeSerial() {
        String strValue = getSerialNumber();
        if (strValue == null)   barcodeSendQuerySerialNumber();
        return strValue;
    }

    boolean barcodeSendQueryDate() {
        byte[] datat = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x48, 0x30, 0x34, 0x30,
                (byte)0xb2 };
        return barcodeSendQuery(datat);
    }
    public String getBarcodeDate() {
        String strValue = getDate();
        if (strValue == null)   barcodeSendQueryDate();
        String strValue1 = getBarcodeESN();
        if (strValue1 != null && strValue1.length() != 0) strValue += (", " + strValue1);
        return strValue;
    }
    public boolean barcodeSendQuerySelfPreSuffix() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x02,
                0x33, 0x37,
                (byte)0xf9 };
        return barcodeSendQuery(data);
    }
    public boolean barcodeSendQueryReadingMode() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x44, 0x30, 0x30, 0x30,
                (byte)0xbd };
        return barcodeSendQuery(data);
    }
    public boolean barcodeSendQueryPrefixOrder() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x02,
                0x33, 0x42,
                0 };
        return barcodeSendQuery(data);
    }
    public boolean barcodeSendQueryEnable2dBarCodes() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x02,
                0x33, 0x33,
                0 };
        return barcodeSendQuery(data);
    }
    public boolean barcodeSendQueryDelayTimeOfEachReading() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x44, 0x30, 0x33, 0x30,
                0 };
        return barcodeSendQuery(data);
    }
    public boolean barcodeSendQueryNoDuplicateReading() {
        byte[] data = new byte[] { 0x7E, 0x00,
                0x00, 0x05,
                0x33, 0x44, 0x30, 0x33, 0x31,
                0 };
        return barcodeSendQuery(data);
    }

    @Keep public void getBarcodePreSuffix() {
        if (getPrefix() == null || getSuffix() == null) barcodeSendQuerySelfPreSuffix();
    }

    public String strVersion, strESN, strSerialNumber, strDate;
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
    public boolean decodeBarcodeUplinkData(byte[] dataValues, BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData) {
        appendToLog("decodeBarcodeUplinkData starts");
        boolean found = false, DEBUG = false;
        int count = 0; boolean matched = true;
        if (barcodeConnector.barcodeToWrite.get(0).dataValues[0] == 0x1b) {
            commandType = BarcodeCommandTypes.COMMAND_COMMON;
            count = 1;
            if (DEBUG) appendToLog("0x1b, Common response with  count = " + count);
        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[0] == 0x7E) {
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
                        byte[] requestBytes = new byte[barcodeConnector.barcodeToWrite.get(0).dataValues.length - 6];
                        System.arraycopy(barcodeConnector.barcodeToWrite.get(0).dataValues, 5, requestBytes, 0, requestBytes.length);
                        if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead.QueryResponse with payload data1 = " + byteArrayToString(bytes) + " for QueryInput data1 = " + byteArrayToString(requestBytes));
                        if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x37 && length >= 5) {
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
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
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
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                            if (dataValues[index+5] == barcodeConnector.barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeConnector.barcodeToWrite.get(0).dataValues[7]) {
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
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                            if (DEBUG) appendToLog("BarStream: dataValue = " + byteArrayToString(dataValues) + ", writeDataValue = " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
                            if (dataValues[index+5] == barcodeConnector.barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeConnector.barcodeToWrite.get(0).dataValues[7]) {
                                matched = true;
                                if (barcodeConnector.barcodeToWrite.get(0).dataValues[6] == 0x30 && barcodeConnector.barcodeToWrite.get(0).dataValues[7] == 0x30  && barcodeConnector.barcodeToWrite.get(0).dataValues[8] == 0x30) {
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
                strData = new String(barcodeConnector.barcodeToWrite.get(0).dataValues, "UTF-8");
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
            if (false) appendToLog("dataValues.length = " + dataValues.length + ", okCount = " + barcodeConnector.iOkCount + ", count = " + count + " for mBarcodeToWrite data = " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
            matched = false; boolean foundOk = false;
            for (int k = 0; k < dataValues.length; k++) {
                boolean match06 = false;
                if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++barcodeConnector.iOkCount == count) matched = true; }
                if (match06 == false) break;
                foundOk = true; found = true;
            }
            if (false) appendToLog("00 matcched = " + matched);
            if (matched) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", OkCount = " + barcodeConnector.iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues)); }
            else if (foundOk) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", but OkCount = " + barcodeConnector.iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues)); }
            else {
                barcodeConnector.mBarcodeToRead.add(csReaderBarcodeData);
                if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.DataRead." + byteArrayToString(csReaderBarcodeData.dataValues) + " is added to mBarcodeToRead");
            }
        }
        if (matched) {
            found = true;
            barcodeConnector.barcodeToWrite.remove(0); barcodeConnector.sendDataToWriteSent = 0; barcodeConnector.mDataToWriteRemoved = true;
            if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + barcodeConnector.barcodeToWrite.size());
        }
        appendToLog("decodeBarcodeUplinkData found = " + found);
        return found;
    }
}
