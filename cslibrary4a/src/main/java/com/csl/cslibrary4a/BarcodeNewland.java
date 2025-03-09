package com.csl.cslibrary4a;

import android.content.Context;

import androidx.annotation.Keep;

import java.util.Arrays;

public class BarcodeNewland {
    public enum BarcodeCommandTypes {
        COMMAND_COMMON, COMMAND_SETTING, COMMAND_QUERY
    }
    BarcodeCommandTypes commandType;

    Context context; Utility utility; BarcodeConnector barcodeConnector; boolean barcode2TriggerMode;
    public BarcodeNewland(Context context, Utility utility, BarcodeConnector barcodeConnector, boolean barcode2TriggerMode) {
        this.context = context;
        this.barcodeConnector = barcodeConnector;
        this.barcode2TriggerMode = barcode2TriggerMode;
        this.utility = utility;
    }

    String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }

    public boolean barcodeSendCommand(byte[] barcodeCommandData) {
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_COMMAND;
        csReaderBarcodeData.waitUplinkResponse = true;
        csReaderBarcodeData.dataValues = barcodeCommandData;
        barcodeConnector.barcodeToWrite.add(csReaderBarcodeData);
        Logger.info("barcodeToWrite added with size = {}", barcodeConnector.barcodeToWrite.size());
        Logger.pkData("PkData: add {}.{} to barcodeToWrite with length = ", csReaderBarcodeData.barcodePayloadEvent, byteArrayToString(csReaderBarcodeData.dataValues), barcodeConnector.barcodeToWrite.size());
        return true;
    }

    public byte bBarcodeTriggerMode = (byte)0xff;
    //public boolean barcode2TriggerModeDefault = true, barcode2TriggerMode = barcode2TriggerModeDefault;
    boolean barcodeReadTriggerStart() {
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_SCAN_START;
        csReaderBarcodeData.waitUplinkResponse = false;
        barcode2TriggerMode = false;
        boolean bValue = barcodeConnector.barcodeToWrite.add(csReaderBarcodeData);
        Logger.info("barcodeToWrite added with size = {}", barcodeConnector.barcodeToWrite.size());
        Logger.pkData("add {} to barcodeToWrite with length = {}", csReaderBarcodeData.barcodePayloadEvent, barcodeConnector.barcodeToWrite.size());
        return bValue;
    }
    public boolean barcodeSendCommandTrigger() {
        boolean retValue = true;
        barcode2TriggerMode = true; bBarcodeTriggerMode = 0x30; Logger.trace("Set trigger reading mode to TRIGGER");
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
        Logger.info("BarStream: BarcodePrefix BarcodeSuffix are SET");
        if (retValue) retValue = barcodeSendCommand("nls0006010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0311010;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0317040;".getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0305010;".getBytes());
        String string = "nls0300000=0x" + byteArrayToString(prefixRef) + ";";
        Logger.info("Set Prefix string = {}", string);
        if (retValue) retValue = barcodeSendCommand(string.getBytes());
        if (retValue) retValue = barcodeSendCommand("nls0306010;".getBytes());
        string = "nls0301000=0x" + byteArrayToString(suffixRef) + ";";
        Logger.info("Set Suffix string = {}", string);
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
        Logger.trace(String.format("BarStream: bytelrc = %02X, last = %02X", bytelrc, data[data.length-1]));
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
        Logger.info("decodeBarcodeUplinkData starts");
        boolean found = false;
        int count = 0; boolean matched = true;
        if (barcodeConnector.barcodeToWrite.get(0).dataValues[0] == 0x1b) {
            commandType = BarcodeCommandTypes.COMMAND_COMMON;
            count = 1;
            Logger.debug("0x1b, Common response with  count = {}", count);
        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[0] == 0x7E) {
            Logger.debug("0x7E, Barcode response with 0x7E barcodeToWrite.get(0).dataValues[0] and response data = {}", byteArrayToString(dataValues));
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
                        Logger.pkData("PkData: found Barcode.Uplink.DataRead.QueryResponse with payload data1 = {} for QueryInput data1 = {}", byteArrayToString(bytes), byteArrayToString(requestBytes));
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
                                Logger.debug("BarStream: BarcodePrefix = {}, BarcodeSuffix = {}", byteArrayToString(bytesBarcodePrefix), byteArrayToString(bytesBarcodeSuffix));
                            }
                            Logger.pkData("PkData: Barcode.Uplink.DataRead.QueryResponse.SelfPrefix_SelfSuffix is processed as Barcode Prefix = {}, Suffix = ", byteArrayToString(bytesBarcodePrefix), byteArrayToString(bytesBarcodeSuffix));
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
                            Logger.debug("versionNumber is detected with length = {}", length);
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
                            Logger.pkData("PkData: uplink data {} is processsed as version = {}", byteArrayToString(byteVersion), versionNumber);
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                            if (dataValues[index+5] == barcodeConnector.barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeConnector.barcodeToWrite.get(0).dataValues[7]) {
                                matched = true; //for ESN, S/N or Date
                                byte[] byteSN = new byte[length - 3];
                                System.arraycopy(dataValues, index + 7, byteSN, 0, byteSN.length);
                                String serialNumber;
                                try {
                                    serialNumber = new String(byteSN, "UTF-8");
                                    int snLength = Integer.parseInt(serialNumber.substring(0, 2));
                                    Logger.debug("BarStream: serialNumber = {}, snLength = {}, serialNumber.length = {}", serialNumber, snLength, serialNumber.length());
                                    if (snLength + 2 == serialNumber.length()) {
                                        serialNumber = serialNumber.substring(2);
                                    } else serialNumber = null;
                                } catch (Exception e) {
                                    serialNumber = null;
                                }
                                Logger.trace("debug index = {}, {}", index, byteArrayToString(dataValues));
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
                                Logger.trace("strResponseType = {}", strResponseType);
                                Logger.pkData("PkData: Barcode.Uplink.DataRead.QueryResponse.{} is processed as {}[{}]", strResponseType, byteArrayToString(byteSN).substring(4), serialNumber);
                            } else Logger.info("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                        } else if (barcodeConnector.barcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                            Logger.debug("BarStream: dataValue = {}, writeDataValue = {}", byteArrayToString(dataValues), byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
                            if (dataValues[index+5] == barcodeConnector.barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeConnector.barcodeToWrite.get(0).dataValues[7]) {
                                matched = true;
                                if (barcodeConnector.barcodeToWrite.get(0).dataValues[6] == 0x30 && barcodeConnector.barcodeToWrite.get(0).dataValues[7] == 0x30  && barcodeConnector.barcodeToWrite.get(0).dataValues[8] == 0x30) {
                                    bBarcodeTriggerMode = dataValues[7];
                                    String strModeType = "";
                                    if (dataValues[index+7] == 0x30) strModeType = "trigger";
                                    else if (dataValues[index+7] == 0x31) strModeType = "auto_Scan";
                                    else if (dataValues[index+7] == 0x32) strModeType = "continue_Scan";
                                    else if (dataValues[index+7] == 0x33) strModeType = "batch_Scan";
                                    Logger.pkData(String.format("PkData: Barcode.Uplink.DataRead.QueryResponse.ReadingMode is processed as last 0x%X[%s]", dataValues[index+7], strModeType));
                                } else Logger.info("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                            } else Logger.info("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                        } else Logger.info("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                        index += (length + 5);
                    } else break;
                } else index++;
            }
            if (matched) { Logger.debug("Matched Query response"); }
            else { Logger.debug("Mis-matched Query response"); }
        } else {
            Logger.info("BarStream: Barcode response with barcodeToWrite.get(0).dataValues[0] =  Others");
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
            Logger.info("Setting strData = {}, count = {}", strData, count);
        }
        if (count != 0) {
            Logger.trace("dataValues.length = {}, okCount = {}, count = {} for barcodeToWrite data = {}", dataValues.length, barcodeConnector.iOkCount, count, byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
            matched = false; boolean foundOk = false;
            for (int k = 0; k < dataValues.length; k++) {
                boolean match06 = false;
                if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++barcodeConnector.iOkCount == count) matched = true; }
                if (match06 == false) break;
                foundOk = true; found = true;
            }
            Logger.trace("00 matcched = {}", matched);
            if (matched) { Logger.pkData("PkData: Barcode.Uplink.DataRead.{} is processed with matched = {}, OkCount = {}, expected count = {} for {}", byteArrayToString(dataValues), matched, barcodeConnector.iOkCount, count, byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues)); }
            else if (foundOk) { Logger.pkData("PkData: Barcode.Uplink.DataRead.{} is processed with matched = {}, but OkCount = {}, expected count = {} for {}", byteArrayToString(dataValues), matched, barcodeConnector.iOkCount, count, byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues)); }
            else {
                barcodeConnector.mBarcodeToRead.add(csReaderBarcodeData);
                Logger.pkData("PkData: uplink data Barcode.DataRead.{} is added to mBarcodeToRead", byteArrayToString(csReaderBarcodeData.dataValues));
            }
        }
        if (matched) {
            found = true;
            barcodeConnector.barcodeToWrite.remove(0); barcodeConnector.sendDataToWriteSent = 0; barcodeConnector.mDataToWriteRemoved = true;
            Logger.info("barcodeToWrite remove0 with length = {}", barcodeConnector.barcodeToWrite.size());
            Logger.pkData("PkData: new barcodeToWrite size = {}", barcodeConnector.barcodeToWrite.size());
        }
        Logger.info("decodeBarcodeUplinkData found = {}", found);
        return found;
    }
}
