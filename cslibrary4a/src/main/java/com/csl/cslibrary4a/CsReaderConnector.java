package com.csl.cslibrary4a;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CsReaderConnector {
    final boolean appendToLogViewDisable = false;
    final boolean DEBUG = false; final boolean DEBUGTHREAD = false;
    boolean DEBUG_CONNECT, DEBUG_SCAN;
    boolean DEBUG_APDATA;
    public boolean sameCheck = true;

    String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    void appendToLog(String s) { utility.appendToLog(s); }
    public boolean connectBle(ReaderDevice readerDevice) {
        boolean result = false;
        if (DEBUG_CONNECT) appendToLog("ConnectBle(" + readerDevice.getCompass() + ")");
        result = bluetoothGatt.connectBle(readerDevice);
        if (result) writeDataCount = 0;
        return result;
    }

    public boolean isBleConnected() { return bluetoothGatt.isBleConnected(); }

    public void disconnect() {
        bluetoothGatt.disconnect();
        appendToLog("abcc done");
        if (rfidConnector != null) rfidConnector.mRfidToWrite.clear();
        if (rfidReader != null) rfidReader.mRx000ToWrite.clear();
    }

    public long getStreamInRate() { return bluetoothGatt.getStreamInRate(); }

    int writeDataCount; int btSendTimeOut = 0; long btSendTime = 0; int BTSENDDELAY = 20;
    boolean writeData(byte[] buffer, int timeout) {
        if (rfidReader.isInventoring()) {
            utility.appendToLogView("BtData: isInventoring is true when writeData " + byteArrayToString(buffer));
        }
        boolean result = bluetoothGatt.writeBleStreamOut(buffer);
        if (result == false) appendToLog("!!! failure to writeData with previous btSendTimeout = " + btSendTimeOut + ", btSendTime = " + btSendTime);
        if (true) {
            btSendTime = System.currentTimeMillis();
            btSendTimeOut = timeout + BTSENDDELAY;
            if (bluetoothGatt.isCharacteristicListRead() == false) btSendTimeOut += 3000;
        }
        return result;
    }

    int[] crc_lookup_table = new int[]{
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78};

    boolean dataRead = false; int dataReadDisplayCount = 0; boolean mCs108DataReadRequest = false;
    int inventoryLength = 0;
    int iSequenceNumber; boolean bDifferentSequence = false, bFirstSequence = true;
    public int invalidata, invalidUpdata, validata;
    public void clearInvalidata() {
        invalidata = 0;
        invalidUpdata = 0;
        validata = 0;
    }
    //boolean dataInBufferResetting;

    void processBleStreamInData() {
        final boolean DEBUG = false;
        int cs108DataReadStartOld = 0;
        int cs108DataReadStart = 0;
        boolean validHeader = false;

        if (cs108DataLeft == null) return;
        /*if (false && dataInBufferResetting) {
            if (utility.DEBUG_FMDATA) appendToLog("FmData: RESET.");
            dataInBufferResetting = false;
            cs108DataLeftOffset = 0;
            connectorDataList.clear();
        }*/
        int iStreamInBufferSize = bluetoothGatt.getStreamInBufferSize();
        //boolean bFirst = true;
        long lTime = System.currentTimeMillis();
        boolean bLooping = false;
        while (bluetoothGatt.getStreamInBufferSize() != 0) {
            if (utility.DEBUG_FMDATA && bLooping == false) appendToLog("FmData: Enter loop with cs108DataLeftOffset=" + cs108DataLeftOffset + ", streamInBufferSize=" + iStreamInBufferSize);
            bLooping = true;

            if (System.currentTimeMillis() - lTime > (bluetoothGatt.getIntervalProcessBleStreamInData()/2)) {
                utility.writeDebug2File("Up2  " + bluetoothGatt.getIntervalProcessBleStreamInData()/2 + "ms Timeout");
                utility.appendToLogView("FmData: Timeout !!!");
                break;
            }

            long streamInOverflowTime = bluetoothGatt.getStreamInOverflowTime();
            int streamInMissing = bluetoothGatt.getStreamInBytesMissing();
            if (streamInMissing != 0) utility.appendToLogView("FmData: processCs108DataIn(" + bluetoothGatt.getStreamInTotalCounter() + ", " + bluetoothGatt.getStreamInAddCounter() + "): len=0, getStreamInOverflowTime()=" + streamInOverflowTime + ", MissBytes=" + streamInMissing + ", Offset=" + cs108DataLeftOffset);
            int len = readData(cs108DataLeft, cs108DataLeftOffset, cs108DataLeft.length);
            if (utility.DEBUG_FMDATA && len != 0) {
                byte[] debugData = new byte[len];
                System.arraycopy(cs108DataLeft, cs108DataLeftOffset, debugData, 0, len);
                appendToLog("FmData: dataIn " + len + " = " + byteArrayToString(debugData));
            }
            //if (len != 0 && bFirst) { bFirst = false; } //writeDebug2File("B" + String.valueOf(getIntervalProcessBleStreamInData()) + ", " + System.currentTimeMillis()); }
            cs108DataLeftOffset += len;
            if (len == 0) {
                appendToLog("FmData: len is zero !!!");
                if (zeroLenDisplayed == false) {
                    zeroLenDisplayed = true;
                    if (bluetoothGatt.getStreamInTotalCounter() != bluetoothGatt.getStreamInAddCounter() || bluetoothGatt.getStreamInAddTime() != 0 || cs108DataLeftOffset != 0) {
                        if (DEBUG) appendToLog("FmData: processCs108DataIn(" + bluetoothGatt.getStreamInTotalCounter() + "," + bluetoothGatt.getStreamInAddCounter() + "): len=0, getStreamInAddTime()=" + bluetoothGatt.getStreamInAddTime() + ", Offset=" + cs108DataLeftOffset);
                    }
                }
                if (cs108DataLeftOffset == cs108DataLeft.length) {
                    if (DEBUG) appendToLog("FmData: cs108DataLeftOffset=" + cs108DataLeftOffset + ", cs108DataLeft=" + byteArrayToString(cs108DataLeft));
                }
                break;
            } else {
                dataRead = true;
                zeroLenDisplayed = false;

                if (utility.DEBUG_FMDATA) appendToLog("FmData: cs108DataReadStart = " + cs108DataReadStart + ", cs108DataLeftOffset = " + cs108DataLeftOffset);
                while (cs108DataLeftOffset >= cs108DataReadStart + 8) {
                    validHeader = false;
                    byte[] dataIn = cs108DataLeft;
                    int iPayloadLength = (dataIn[cs108DataReadStart + 2] & 0xFF);
                    if ((dataIn[cs108DataReadStart + 0] == (byte) 0xA7)
                            && (dataIn[cs108DataReadStart + 1] == (byte) 0xB3)
                            && (dataIn[cs108DataReadStart + 3] == (byte) 0xC2
                            || dataIn[cs108DataReadStart + 3] == (byte) 0x6A
                            || dataIn[cs108DataReadStart + 3] == (byte) 0xD9
                            || dataIn[cs108DataReadStart + 3] == (byte) 0xE8
                            || dataIn[cs108DataReadStart + 3] == (byte) 0x5F)
                            //&& ((dataIn[cs108DataReadStart + 4] == (byte) 0x82) || ((dataIn[cs108DataReadStart + 3] == (byte) 0xC2) && (dataIn[cs108DataReadStart + 8] == (byte) 0x81)))
                            && (dataIn[cs108DataReadStart + 5] == (byte) 0x9E)) {
                        if (cs108DataLeftOffset - cs108DataReadStart < (iPayloadLength + 8))
                            break;

                        boolean bcheckChecksum = true;
                        int checksum = ((byte) dataIn[cs108DataReadStart + 6] & 0xFF) * 256 + ((byte) dataIn[cs108DataReadStart + 7] & 0xFF);
                        int checksum2 = 0;
                        if (bcheckChecksum) {
                            for (int i = cs108DataReadStart; i < cs108DataReadStart + 8 + iPayloadLength; i++) {
                                if (i != (cs108DataReadStart + 6) && i != (cs108DataReadStart + 7)) {
                                    int index = (checksum2 ^ ((byte) dataIn[i] & 0x0FF)) & 0x0FF;
                                    int table_value = crc_lookup_table[index];
                                    checksum2 = (checksum2 >> 8) ^ table_value;
                                }
                            }
                            if (false) appendToLog("FmData: checksum = " + String.format("%04X", checksum) + ", checksum2 = " + String.format("%04X", checksum2));
                        }
                        if (bcheckChecksum && checksum != checksum2) {
                            if (utility.DEBUG_FMDATA) {
                                if (iPayloadLength < 0) {
                                    appendToLog("FmData: CheckSum ERROR, iPayloadLength=" + iPayloadLength + ", cs108DataLeftOffset=" + cs108DataLeftOffset + ", dataIn=" + byteArrayToString(dataIn));
                                }
                                byte[] invalidPart = new byte[8 + iPayloadLength];
                                System.arraycopy(dataIn, cs108DataReadStart, invalidPart, 0, invalidPart.length);
                                appendToLog("FmData: processCs108DataIn_ERROR, INCORRECT RevChecksum=" + Integer.toString(checksum, 16) + ", CalChecksum2=" + Integer.toString(checksum2, 16) + ",data=" + byteArrayToString(invalidPart));
                            }
                        } else {
                            validHeader = true;
                            if (cs108DataReadStart > cs108DataReadStartOld) {
                                if (utility.DEBUG_FMDATA) {
                                    byte[] invalidPart = new byte[cs108DataReadStart - cs108DataReadStartOld];
                                    System.arraycopy(dataIn, cs108DataReadStartOld, invalidPart, 0, invalidPart.length);
                                    appendToLog("FmData: processCs108DataIn_ERROR, before valid data, invalid unused data: " + invalidPart.length + ", " + byteArrayToString(invalidPart));
                                }
                            } else if (cs108DataReadStart < cs108DataReadStartOld)
                                if (utility.DEBUG_FMDATA) appendToLog("FmData: processCs108DataIn_ERROR, invalid cs108DataReadStartdata=" + cs108DataReadStart + " < cs108DataReadStartOld=" + cs108DataReadStartOld);
                            cs108DataReadStartOld = cs108DataReadStart;

                            ConnectorData connectorData = new ConnectorData();
                            byte[] dataValues = new byte[iPayloadLength];
                            System.arraycopy(dataIn, cs108DataReadStart + 8, dataValues, 0, dataValues.length);
                            connectorData.dataValues = dataValues;
                            connectorData.milliseconds = System.currentTimeMillis();
                            if (utility.DEBUG_FMDATA) {
                                byte[] headerbytes = new byte[8];
                                System.arraycopy(dataIn, cs108DataReadStart, headerbytes, 0, headerbytes.length);
                                appendToLog("FmData: Got formatted dataIn = " + byteArrayToString(headerbytes) + " " + byteArrayToString(dataValues));
                            }
                            switch (dataIn[cs108DataReadStart + 3]) {
                                case (byte) 0xC2:
                                case (byte) 0x6A:
                                    if (dataIn[cs108DataReadStart + 3] == (byte) 0xC2) connectorData.connectorTypes = ConnectorData.ConnectorTypes.RFID;
                                    else connectorData.connectorTypes = ConnectorData.ConnectorTypes.BARCODE;
                                    if (dataIn[cs108DataReadStart + 8] == (byte) 0x81 || (bis108 == false && dataIn[cs108DataReadStart + 8] == (byte) 0x91)) {
                                        int iSequenceNumber = (int) (dataIn[cs108DataReadStart + 4] & 0xFF);
                                        int itemp = iSequenceNumber;
                                        if (itemp < this.iSequenceNumber) {
                                            itemp += 256;
                                        }
                                        itemp -= (this.iSequenceNumber + 1);
                                        if (DEBUG) appendToLog("iSequenceNumber = " + iSequenceNumber + ", old iSequenceNumber = " + this.iSequenceNumber + ", difference = " + itemp);
                                        if (itemp != 0) {
                                            if (DEBUG) appendToLog("Non-zero iSequenceNumber difference = " + itemp);
                                            connectorData.invalidSequence = true;
                                            if (bFirstSequence == false) {
                                                invalidata += itemp;
                                                String stringSequenceList = "";
                                                for (int i = 0; i < itemp; i++) {
                                                    int iMissedNumber = (iSequenceNumber - i - 1);
                                                    if (iMissedNumber < 0) iMissedNumber += 256;
                                                    stringSequenceList += (i != 0 ? ", " : "") + String.format("%X", iMissedNumber);
                                                }
                                                if (DEBUG) utility.appendToLogView(String.format("ERROR !!!: invalidata = %d, %X - %X, miss %d: ", invalidata, iSequenceNumber, this.iSequenceNumber, itemp) + stringSequenceList);
                                            }
                                        }
                                        bFirstSequence = false;
                                        this.iSequenceNumber = iSequenceNumber;
                                    }
                                    if (DEBUG) utility.appendToLogView("Rin: " + (connectorData.invalidSequence ? "invalid sequence" : "ok") + "," + byteArrayToString(connectorData.dataValues));
                                    validata++;
                                    break;
                                case (byte) 0xD9:
                                    if (DEBUG) appendToLog("BARTRIGGER NotificationData = " + byteArrayToString(connectorData.dataValues));
                                    connectorData.connectorTypes = ConnectorData.ConnectorTypes.NOTIFICATION;
                                    break;
                                case (byte) 0xE8:
                                    connectorData.connectorTypes = ConnectorData.ConnectorTypes.SILICONLAB;
                                    break;
                                case (byte) 0x5F:
                                    connectorData.connectorTypes = ConnectorData.ConnectorTypes.BLUETOOTH;
                                    break;
                            }
                            this.connectorDataList.add(connectorData);
                            if (utility.DEBUG_FMDATA) appendToLog("FmData: Got PackageIn " + connectorData.connectorTypes.toString() + ", " + byteArrayToString(connectorData.dataValues));
                            utility.writeDebug2File("Up2  " + connectorData.connectorTypes.toString() + ", " + byteArrayToString(connectorData.dataValues));
                            cs108DataReadStart += ((8 + iPayloadLength));

                            byte[] cs108DataLeftNew = new byte[CS108DATALEFT_SIZE];
                            if (cs108DataLeftOffset - cs108DataReadStart < 0) {
                                if (utility.DEBUG_FMDATA) appendToLog("FmData: cs108DataLeftOffset = " + cs108DataLeftOffset + ", cs108DataReadStart = " + cs108DataReadStart + ", buffer = " + byteArrayToString(cs108DataLeft));
                                break;
                            }
                            System.arraycopy(cs108DataLeft, cs108DataReadStart, cs108DataLeftNew, 0, cs108DataLeftOffset - cs108DataReadStart);
                            cs108DataLeft = cs108DataLeftNew;
                            cs108DataLeftOffset -= cs108DataReadStart;
                            cs108DataReadStart = 0;
                            cs108DataReadStart = -1;
                            if (true || mCs108DataReadRequest == false) {
                                mCs108DataReadRequest = true;
                                if (DEBUGTHREAD && DEBUG) appendToLog("ready2Write: start immediate mReadWriteRunnable");
                                //appendToLog("post mReadWriteRunnable within processBleStreamInData");
                                mHandler.removeCallbacks(mReadWriteRunnable); mHandler.post(mReadWriteRunnable);
                                if (utility.DEBUG_BTDATA && DEBUG) appendToLog("BtData: CsReaderConnector.processBleStreamOut starts mReadWriteRunnable as mCs108DataReadRequest");
                            } //appendToLog("BtData: processBleStreamOut cannot start mReadWriteRunnable as mCs108DataReadRequest is true");
                        }
                    }
                    if (validHeader && cs108DataReadStart < 0) {
                        cs108DataReadStart = 0;
                        cs108DataReadStartOld = 0;
                    } else {
                        cs108DataReadStart++;
                    }
                }
                if (cs108DataReadStart != 0 && cs108DataLeftOffset >= 8) {
                    if (utility.DEBUG_FMDATA) {
                        byte[] invalidPart = new byte[cs108DataReadStart];
                        System.arraycopy(cs108DataLeft, 0, invalidPart, 0, invalidPart.length);
                        byte[] validPart = new byte[cs108DataLeftOffset - cs108DataReadStart];
                        System.arraycopy(cs108DataLeft, cs108DataReadStart, validPart, 0, validPart.length);
                        appendToLog("FmData: processCs108DataIn_ERROR, ENDLOOP invalid unused data: " + invalidPart.length + ", " + byteArrayToString(invalidPart) + ", with valid data length=" + validPart.length + ", " + byteArrayToString(validPart));
                        utility.writeDebug2File("Up2  Invalid " + invalidPart.length + ", " + byteArrayToString(invalidPart));
                    }

                    byte[] cs108DataLeftNew = new byte[CS108DATALEFT_SIZE];
                    System.arraycopy(cs108DataLeft, cs108DataReadStart, cs108DataLeftNew, 0, cs108DataLeftOffset - cs108DataReadStart);
                    cs108DataLeft = cs108DataLeftNew;
                    cs108DataLeftOffset -= cs108DataReadStart; cs108DataReadStart = 0;
                }
            }
        }
        if (utility.DEBUG_FMDATA && bLooping) appendToLog("FmData: Exit loop with cs108DataLeftOffset=" + cs108DataLeftOffset + ", streamInBufferSize=" + bluetoothGatt.getStreamInBufferSize());
    }

    private int readData(byte[] buffer, int byteOffset, int byteCount) { return bluetoothGatt.readBleSteamIn(buffer, byteOffset, byteCount); }

    public class Cs108ConnectorData {
        public int getVoltageMv() { return notificationConnector.mVoltageValue; }
        public int getVoltageCnt() { return notificationConnector.mVoltageCount; }
        boolean getTriggerButtonStatus() { return notificationConnector.triggerButtonStatus; }
        public int getTriggerCount() { return notificationConnector.iTriggerCount; }
        Date timeStamp;
        String getTimeStamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            return sdf.format(mCs108ConnectorData.timeStamp);
        }

    }
    public Cs108ConnectorData mCs108ConnectorData;
    public SettingData settingData;

    RfidConnector rfidConnector; public RfidReader rfidReader;
    public BarcodeConnector barcodeConnector; public BarcodeNewland barcodeNewland;
    public NotificationConnector notificationConnector;
    public ControllerConnector controllerConnector;
    public BluetoothConnector bluetoothConnector;

    private Handler mHandler = new Handler();

    public void cs108ConnectorDataInit() {
        connectorDataList = new ArrayList<>();
        cs108DataLeft = new byte[CS108DATALEFT_SIZE];
        cs108DataLeftOffset = 0;
        zeroLenDisplayed = false;

        invalidata = 0;
        validata = 0;
        //dataInBufferResetting = false;

        writeDataCount = 0;

        mCs108ConnectorData = new Cs108ConnectorData();
        notificationConnector = new NotificationConnector(context, utility, settingData.triggerReporting, settingData.triggerReportingCountSetting);
        controllerConnector = new ControllerConnector(context, utility);
        bluetoothConnector = new BluetoothConnector(context, utility, settingData.userDebugEnable);
        //settingData = new SettingData(context, utility);

        rfidReader = new RfidReader(context, utility, this, bis108, bluetoothGatt, settingData, notificationConnector);
        rfidConnector = rfidReader.rfidConnector;
        barcodeConnector = new BarcodeConnector(context, utility);
        barcodeNewland = new BarcodeNewland(context, utility, barcodeConnector, settingData.barcode2TriggerMode);
        barcodeConnector.barcodeConnectorCallback = new BarcodeConnector.BarcodeConnectorCallback(){
            @Override
            public boolean callbackMethod(byte[] dataValues, BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData) {
                barcodeNewland.decodeBarcodeUplinkData(dataValues, csReaderBarcodeData);
                return false;
            }
        };
        settingData.setConnectedConnectors(notificationConnector, rfidReader);
        mHandler.removeCallbacks(mReadWriteRunnable); mHandler.post(mReadWriteRunnable);
        appendToLog("!!! all major classes are initialised");
    }

    final int CS108DATALEFT_SIZE = 300; //4000;    //100;
    private ArrayList<ConnectorData> connectorDataList;
    byte[] cs108DataLeft;
    int cs108DataLeftOffset;
    boolean zeroLenDisplayed;

    public BluetoothGatt bluetoothGatt;
    Context context; TextView mLogView; Utility utility; boolean bis108;
    public CsReaderConnector(Context context, TextView mLogView, Utility utility, boolean bis108) {
        this.context = context;
        this.mLogView = mLogView;
        this.utility = utility;
        this.bis108 = bis108;

        DEBUG_APDATA = utility.DEBUG_APDATA;
        bluetoothGatt = new BluetoothGatt(context, utility, (bis108 ? "9800" : "9802"));
        bluetoothGatt.bluetoothGattConnectorCallback = new BluetoothGatt.BluetoothGattConnectorCallback(){
            @Override
            public void callbackMethod() {
                //appendToLog("going to processBleStreamInData with bis108 " + bis108 + " and connected " + isBleConnected());
                processBleStreamInData();
            }
        };
        DEBUG_CONNECT = bluetoothGatt.DEBUG_CONNECT; DEBUG_SCAN = bluetoothGatt.DEBUG_SCAN;

        //cs108ConnectorDataInit();
        //mHandler.removeCallbacks(bluetoothGatt.runnableProcessBleStreamInData); mHandler.post(bluetoothGatt.runnableProcessBleStreamInData);
        //if (DEBUGTHREAD) appendToLog("start immediate mReadWriteRunnable");
        //mHandler.removeCallbacks(mReadWriteRunnable); mHandler.post(mReadWriteRunnable);
        //mHandler.removeCallbacks(runnableRx000UplinkHandler); mHandler.post(runnableRx000UplinkHandler);
        appendToLog("foregroundReader: new SettingData for bis108 as " + bis108);
        settingData = new SettingData(context, utility, bluetoothGatt, this);
    }

    long timeReady; boolean aborting = false, sendFailure = false;
    private final Runnable mReadWriteRunnable = new Runnable() {
        boolean ready2Write = false, DEBUG = false;
        int timer2Write = 0;
        boolean validBuffer;

        @Override
        public void run() {
            if (DEBUGTHREAD || utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable starts");
            if (rfidConnector == null) {
                mHandler.postDelayed(mReadWriteRunnable, 500);
                if (utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable restart after 500ms");
                return;
            }
            if (timer2Write != 0 || bluetoothGatt.getStreamInBufferSize() != 0 || rfidConnector.mRfidToRead.size() != 0) {
                validBuffer = true;
                if (DEBUG) appendToLog("mReadWriteRunnable(): START, timer2Write=" + timer2Write + ", streamInBufferSize = " + bluetoothGatt.getStreamInBufferSize() + ", mRfidToRead.size=" + rfidConnector.mRfidToRead.size() + ", mRx000ToRead.size=" + rfidReader.mRx000ToRead.size());
            } else  validBuffer = false;
            int intervalReadWrite = 250; //50;   //50;    //500;   //500, 100;
            if (rfidConnector.rfidPowerOnTimeOut >= intervalReadWrite) {
                rfidConnector.rfidPowerOnTimeOut -= intervalReadWrite;
                if (rfidConnector.rfidPowerOnTimeOut <= 0) {
                    rfidConnector.rfidPowerOnTimeOut = 0;
                }
            }
            if (barcodeConnector.barcodePowerOnTimeOut >= intervalReadWrite) {
                barcodeConnector.barcodePowerOnTimeOut -= intervalReadWrite;
                if (barcodeConnector.barcodePowerOnTimeOut <= 0) {
                    barcodeConnector.barcodePowerOnTimeOut = 0;
                }
            }
            if (barcodeConnector.barcodePowerOnTimeOut != 0)
                if (DEBUG) appendToLog("mReadWriteRunnable(): barcodePowerOnTimeOut = " + barcodeConnector.barcodePowerOnTimeOut);

            long lTime = System.currentTimeMillis();
            if (DEBUGTHREAD) appendToLog("start new mReadWriteRunnable after " + intervalReadWrite + " ms");
            //appendToLog("postDelayed mReadWriteRunnable within mReadWriteRunnable");
            mHandler.removeCallbacks(mReadWriteRunnable); mHandler.postDelayed(mReadWriteRunnable, intervalReadWrite);
            if (utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable restart after 250ms");
            if (rfidReader == null) return;

            boolean bFirst = true;
            boolean bLooping = false;
            mCs108DataReadRequest = false;
            while (connectorDataList.size() != 0) {
                if (utility.DEBUG_PKDATA && bLooping == false) appendToLog("PkData: Entering loop with connectorDataList.size = " + connectorDataList.size());
                bLooping = true;

                if (isBleConnected() == false) {
                    connectorDataList.clear();
                } else if (System.currentTimeMillis() - lTime > (intervalRx000UplinkHandler / 2)) {
                    utility.writeDebug2File("Up3  " + "Timeout");
                    utility.appendToLogView("PkData: mReadWriteRunnable: TIMEOUT !!! mCs108DataRead.size() = " + connectorDataList.size());
                    break;
                } else {
                    if (bFirst) { bFirst = false; } //writeDebug2File("C" + String.valueOf(intervalReadWrite) + ", " + System.currentTimeMillis()); }
                    try {
                        ConnectorData connectorData = connectorDataList.get(0);
                        connectorDataList.remove(0);
                        boolean bValid = true;
                        if (utility.DEBUG_PKDATA) appendToLog("PkData: connectorData.type = " + connectorData.connectorTypes.toString() + ", connectorData.dataValues = " + byteArrayToString(connectorData.dataValues));
                        if (rfidConnector.isMatchRfidToWrite(connectorData)) {
                            if (false) {
                                for (int i = 0; i < rfidReader.mRx000ToRead.size(); i++) {
                                    if (rfidReader.mRx000ToRead.get(i).responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END)
                                        if (DEBUG) appendToLog("mRx0000ToRead with COMMAND_END is removed");
                                }
                                if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                            }
                            rfidReader.mRx000ToRead.clear(); if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                            if (writeDataCount > 0) writeDataCount--; if (bis108) ready2Write = true; //btSendTime = 0; aborting = false;
                        } else if (barcodeConnector.isMatchBarcodeToWrite(connectorData)) {
                            if (writeDataCount > 0) writeDataCount--; if (bis108) ready2Write = true; //btSendTime = 0;
                        } else if (notificationConnector.isMatchNotificationToWrite(connectorData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; if (false) appendToLog("ready2Write is set true after true isMatchNotificationToWrite "); btSendTime = 0; if (utility.DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: matched notification. btSendTime is set to 0 to allow new sending.");
                        } else if (controllerConnector.isMatchControllerToWrite(connectorData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; if (false) appendToLog("ready2Write is set true after true isMatchSiliconLabIcToWrite "); btSendTime = 0; if (utility.DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: matched AtmelIc. btSendTime is set to 0 to allow new sending.");
                        } else if (bluetoothConnector.isMatchBluetoothIcToWrite(connectorData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; appendToLog("ready2Write is set true after true isMatchBluetoothIcToWrite "); btSendTime = 0; if (utility.DEBUG_PKDATA) appendToLog("PKData: mReadWriteRunnable: matched bluetoothIc. btSendTime is set to 0 to allow new sending.");
                        } else if (rfidConnector.isRfidToRead(connectorData)) { rfidConnector.rfidValid = true;
                        } else if (barcodeConnector.isBarcodeToRead(connectorData)) {
                        } else if (notificationConnector.isNotificationToRead(connectorData)) {
                            /* if (mRfidDevice.mRfidToWrite.size() != 0 && mNotificationDevice.mNotificationToRead.size() != 0) {
                                mNotificationDevice.mNotificationToRead.remove(0);
                                mRfidDevice.mRfidToWrite.clear();
                                mSiliconLabIcDevice.mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.RESET);

                                timeReady = System.currentTimeMillis() - 1500;
                                appendToLog("mReadWriteRunnable: endingMessage: changed timeReady");
                            }*/
                        } else bValid = false;
                        if (bValid) {
                            //writeDebug2File("Up33 " + cs108ReadData.cs108ConnectedDevices.toString() + ", " + byteArrayToString(cs108ReadData.dataValues));
                        } else {
                            appendToLog("mReadWriteRunnable: !!! CANNOT process " + byteArrayToString(connectorData.dataValues) + " with mDataToWriteRemoved = " + barcodeConnector.mDataToWriteRemoved);
                            utility.writeDebug2File("Up3  Invalid " + connectorData.dataValues.length + ", " + byteArrayToString(connectorData.dataValues));
                        }
                        if (barcodeConnector.mDataToWriteRemoved)  {
                            barcodeConnector.mDataToWriteRemoved = false; ready2Write = true; btSendTime = 0;
                            appendToLog("ready2Write is set true after true mBarcodeDevice.mDataToWriteRemoved ");
                            if (utility.DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: processed barcode. btSendTime is set to 0 to allow new sending.");
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            if (utility.DEBUG_PKDATA && bLooping) appendToLog("PkData: Exiting loop with connectorDataList.size = " + connectorDataList.size());

            lTime = System.currentTimeMillis();
            if (rfidConnector.mRfidToWriteRemoved)  {
                rfidConnector.mRfidToWriteRemoved = false; ready2Write = true; btSendTime = 0; if (false) appendToLog("ready2Write is set true after true mRfidDevice.mRfidToWriteRemoved ");
                btSendTime = (lTime - btSendTimeOut + BTSENDDELAY);
                if (DEBUGTHREAD) appendToLog("ready2Write: start new mReadWriteRunnable after " + BTSENDDELAY + " ms");
                //appendToLog("postDelayed mReadWriteRunnable within mReadWriteRunnable 2");
                mHandler.removeCallbacks(mReadWriteRunnable); mHandler.postDelayed(mReadWriteRunnable, BTSENDDELAY + 2);
                if (utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable restart after " + (BTSENDDELAY + 2) +"ms") ;
                if (utility.DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: processed Rfidcode. btSendTime is set to 0 to allow new sending with systime = " + lTime);
            }
            if (bis108) {
                int timeout2Ready = 2000;
                if (aborting || sendFailure) timeout2Ready = 200;
                if (System.currentTimeMillis() > timeReady + timeout2Ready) ready2Write = true;
            } else {
                if (ready2Write == false && lTime - btSendTime > btSendTimeOut) {
                    appendToLog("ready2Write is set to true from false with difference = " + (lTime - btSendTime) + ", systime = " + lTime + ", btSendTime = " + btSendTime + ", btSendTimeOut = " + btSendTime);
                    ready2Write = true;
                }
            }
            if (DEBUG) appendToLog("BtData: ready2Write = " + ready2Write);
            if (ready2Write) {
                timeReady = System.currentTimeMillis();
                timer2Write = 0;
                if (rfidConnector.rfidFailure) rfidConnector.mRfidToWrite.clear();
                //if (barcodeConnector.barcodeFailure) { barcodeConnector.barcodeToWrite.clear(); appendToLog("barcodeToWrite is clear"); }
                if (rfidReader.mRx000ToWrite.size() != 0 && rfidConnector.mRfidToWrite.size() == 0) {
                    if (DEBUG)
                        appendToLog("mReadWriteRunnable(): mRx000ToWrite.size=" + rfidReader.mRx000ToWrite.size() + ", mRfidToWrite.size=" + rfidConnector.mRfidToWrite.size());
                    rfidReader.addRfidToWrite(rfidReader.mRx000ToWrite.get(0));
                }
                boolean bisRfidCommandStop = false, bisRfidCommandExecute = false;
                if (rfidConnector.mRfidToWrite.size() != 0 && DEBUG)
                    appendToLog("mRfidToWrite = " + rfidConnector.mRfidToWrite.get(0).rfidPayloadEvent.toString() + "." + byteArrayToString(rfidConnector.mRfidToWrite.get(0).dataValues) + ", ready2write = " + ready2Write);
                if (rfidConnector.mRfidToWrite.size() != 0) {
                    RfidConnector.CsReaderRfidData csReaderRfidData = rfidConnector.mRfidToWrite.get(0);
                    if (csReaderRfidData.rfidPayloadEvent == RfidConnector.RfidPayloadEvents.RFID_COMMAND) {
                        int ii;
                        if (false) {
                            byte[] byCommandExeccute = new byte[]{0x70, 1, 0, (byte) 0xF0};
                            for (ii = 0; ii < 4; ii++) {
                                if (byCommandExeccute[ii] != csReaderRfidData.dataValues[ii]) break;
                            }
                            if (ii == 4) bisRfidCommandExecute = true;
                        }

                        byte[] byCommandStop = new byte[]{(byte) 0x40, 3, 0, 0, 0, 0, 0, 0};
                        for (ii = 0; ii < 4; ii++) {
                            if (byCommandStop[ii] != csReaderRfidData.dataValues[ii]) break;
                        }
                        if (ii == 4) bisRfidCommandStop = true;
                        if (DEBUG)
                            appendToLog("mRfidToWrite(0).dataValues = " + byteArrayToString(rfidConnector.mRfidToWrite.get(0).dataValues) + ", bisRfidCommandExecute = " + bisRfidCommandExecute + ", bisRfidCommandStop = " + bisRfidCommandStop);
                    }
                }
                if (barcodeConnector.barcodeToWrite.size() != 0 && true)
                    appendToLog("AAA 1 barcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size() + ", bisRfidCommandStop = " + bisRfidCommandStop + ", barcodePowerOnTimeOut = " + barcodeConnector.barcodePowerOnTimeOut);
                if (DEBUG) appendToLog("BtData: bisRfidCommandStop is " + bisRfidCommandStop);
                if (bisRfidCommandStop) {
                    if (rfidConnector.rfidPowerOnTimeOut != 0) {
                        if (DEBUG) appendToLog("rfidPowerOnTimeOut = " + rfidConnector.rfidPowerOnTimeOut + ", mRfidToWrite.size() = " + rfidConnector.mRfidToWrite.size());
                    } else if (rfidConnector.rfidFailure == false && rfidConnector.mRfidToWrite.size() != 0) {
                        if (isBleConnected() == false) {
                            rfidConnector.mRfidToWrite.clear();
                        } else {
                            if (utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable 1: currentTime = " + System.currentTimeMillis() + ", btSendTime = " + btSendTime + ", difference = " + (System.currentTimeMillis() - btSendTime) + ", btSendTimeOut = " + btSendTimeOut);
                            if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                                boolean retValue = false;
                                byte[] dataOut = rfidConnector.sendRfidToWrite();
                                if (dataOut != null) {
                                    retValue = writeData(dataOut, (rfidConnector.mRfidToWrite.get(0).waitUplinkResponse ? 500 : 0));
                                    if (false) appendToLog("BtData: done writeData with waitUplinkResponse = " + rfidConnector.mRfidToWrite.get(0).waitUplinkResponse);
                                }
                                appendToLog("BtData: done writeRfid with size = " + rfidConnector.mRfidToWrite.size() + ", PayloadEvents = " + rfidConnector.mRfidToWrite.get(0).rfidPayloadEvent.toString() + ", data=" + byteArrayToString(rfidConnector.mRfidToWrite.get(0).dataValues));
                                rfidConnector.sendRfidToWriteSent++;
                                if (retValue)   {
                                    rfidConnector.mRfidToWriteRemoved = false;
                                    if (DEBUG) appendToLog("writeRfid() with sendRfidToWriteSent = " + rfidConnector.sendRfidToWriteSent);
                                    sendFailure = false;
                                    //bValue = true;
                                } else sendFailure = true;
                                ready2Write = false;    //
                                appendToLog("ready2Write is set false after sendRfidToWrite");
                            }
                        }
                    }
                } else if (!bis108 && rfidReader.isInventoring()) {
                    if (rfidConnector.rfidPowerOnTimeOut != 0) {
                        if (DEBUG) appendToLog("rfidPowerOnTimeOut = " + rfidConnector.rfidPowerOnTimeOut + ", mRfidToWrite.size() = " + rfidConnector.mRfidToWrite.size());
                    } else if (rfidConnector.rfidFailure == false && rfidConnector.mRfidToWrite.size() != 0) {
                        if (isBleConnected() == false) {
                            rfidConnector.mRfidToWrite.clear();
                        } else {
                            if (DEBUG)
                                appendToLog("BtDataOut 2: currentTime = " + System.currentTimeMillis() + ", btSendTime = " + btSendTime + ", difference = " + (System.currentTimeMillis() - btSendTime) + ", btSendTimeOut = " + btSendTimeOut);
                            if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                                boolean retValue = false;
                                byte[] dataOut = rfidConnector.sendRfidToWrite();
                                if (dataOut != null) {
                                    retValue = writeData(dataOut, (rfidConnector.mRfidToWrite.get(0).waitUplinkResponse ? 500 : 0));
                                    if (false) appendToLog("done writeData with waitUplinkResponse = " + rfidConnector.mRfidToWrite.get(0).waitUplinkResponse);
                                }
                                if (DEBUG) appendToLog("BtDataOut: done writeRfid with size = " + rfidConnector.mRfidToWrite.size() + ", PayloadEvents = " + rfidConnector.mRfidToWrite.get(0).rfidPayloadEvent.toString() + ", data=" + byteArrayToString(rfidConnector.mRfidToWrite.get(0).dataValues));
                                rfidConnector.sendRfidToWriteSent++;
                                if (retValue)   {
                                    rfidConnector.mRfidToWriteRemoved = false;
                                    if (DEBUG) appendToLog("writeRfid() with sendRfidToWriteSent = " + rfidConnector.sendRfidToWriteSent);
                                    sendFailure = false;
                                    //bValue = true;
                                } else sendFailure = true;

                                if (retValue) {
                                    ready2Write = false;
                                    if (false) appendToLog("ready2Write is set false after true sendRfidToWrite");
                                }
                            }
                        }
                    }
                } else if (notificationConnector.notificationToWrite.size() != 0) {
                    if (isBleConnected() == false) {
                        notificationConnector.notificationToWrite.clear(); appendToLog("notificationToWrite is clear"); }
                    else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        byte[] dataOut = notificationConnector.sendNotificationToWrite();
                        boolean retValue = false;

                        if (utility.DEBUG_PKDATA && notificationConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! notificationToWrite.sendDataToWriteSent = " + controllerConnector.sendDataToWriteSent);
                        if (utility.DEBUG_PKDATA)
                            appendToLog(String.format("PkData: write notificationToWrite.%s with notificationConnector.sendDataToWriteSent = %d",
                                    notificationConnector.notificationToWrite.get(0).notificationPayloadEvent.toString(),
                                    notificationConnector.sendDataToWriteSent));
                        if (false && notificationConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! mSiliconLabIcDevice.sendDataToWriteSent = " + notificationConnector.sendDataToWriteSent);

                        if (dataOut != null) retValue = writeData(dataOut, 0);
                        if (retValue) {
                            //notificationController.sendDataToWriteSent++;
                        } else {
                            //if (DEBUG) appendToLogView("failure to send " + notificationController.notificationToWrite.get(0).toString());
                            //notificationController.notificationToWrite.remove(0); notificationController.sendDataToWriteSent = 0; appendToLog("notificationToWrite remove0 with length = " + notificationToWrite.size());
                        }
                    }
                    ready2Write = false;    //
                    if (false) appendToLog("ready2Write is set false after true sendSiliconLabIcToWrite");
                } else if (controllerConnector.controllerToWrite.size() != 0) {
                    appendToLog("AAA 5");
                    if (isBleConnected() == false) controllerConnector.controllerToWrite.clear();
                    else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        byte[] dataOut = controllerConnector.sendControllerToWrite();
                        boolean retValue = false;

                        if (utility.DEBUG_PKDATA && controllerConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! siliconLabIcDevice.sendDataToWriteSent = " + controllerConnector.sendDataToWriteSent);
                        if (utility.DEBUG_PKDATA)
                            appendToLog(String.format("PkData: write mSiliconLabIcDevice.%s with mSiliconLabIcDevice.sendDataToWriteSent = %d",
                                    controllerConnector.controllerToWrite.get(0).toString(),
                                    controllerConnector.sendDataToWriteSent));
                        if (false && controllerConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! mSiliconLabIcDevice.sendDataToWriteSent = " + controllerConnector.sendDataToWriteSent);

                        if (dataOut != null) retValue = writeData(dataOut, 0);
                        if (retValue) {
                            //controllerConnector.sendDataToWriteSent++;
                        } else {
                            //if (DEBUG) appendToLogView("failure to send " + controllerConnector.controllerToWrite.get(0).toString());
                            //controllerConnector.controllerToWrite.remove(0); controllerConnector.sendDataToWriteSent = 0;
                        }
                    }
                    ready2Write = false;    //
                    if (false) appendToLog("ready2Write is set false after true sendSiliconLabIcToWrite");
                } else if (bluetoothConnector.bluetoothIcToWrite.size() != 0) {   //Bluetooth version affects Barcode operation
                    appendToLog("AAA 6");
                    if (isBleConnected() == false) bluetoothConnector.bluetoothIcToWrite.clear();
                    else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        byte[] dataOut = bluetoothConnector.sendBluetoothIcToWrite();
                        boolean retValue = false;

                        if (utility.DEBUG_PKDATA && bluetoothConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! mBluetoothIcDevice.sendDataToWriteSent = " + bluetoothConnector.sendDataToWriteSent);
                        if (utility.DEBUG_PKDATA)
                            appendToLog(String.format("PkData: write mBluetoothIcDevice.%s.%s with mBluetoothIcDevice.sendDataToWriteSent = %d",
                                    bluetoothConnector.bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString(),
                                    byteArrayToString(bluetoothConnector.bluetoothIcToWrite.get(0).dataValues),
                                    bluetoothConnector.sendDataToWriteSent));
                        if (bluetoothConnector.sendDataToWriteSent != 0)
                            appendToLog("!!! mBluetoothIcDevice.sendDataToWriteSent = " + bluetoothConnector.sendDataToWriteSent);

                        if (dataOut != null) retValue = writeData(dataOut, 0);
                        if (retValue) {
                            //bluetoothConnector.sendDataToWriteSent++;
                        } else {
                            //if (DEBUG) appendToLogView("failure to send " + bluetoothConnector.bluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString());
                            //bluetoothConnector.bluetoothIcToWrite.remove(0); bluetoothConnector.sendDataToWriteSent = 0;
                        }
                    }
                    ready2Write = false;
                    appendToLog("ready2Write is set false after non-zero mBluetoothIcToWrite.size()");
                } else if (barcodeConnector.barcodeToWrite.size() != 0 && barcodeConnector.barcodePowerOnTimeOut == 0) {
                    appendToLog("AAA 7 barcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size());
                    if (isBleConnected() == false) { barcodeConnector.barcodeToWrite.clear(); appendToLog("barcodeToWrite is clear"); }
                    else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        byte[] dataOut = barcodeConnector.sendBarcodeToWrite();
                        if (dataOut != null)
                            writeData(dataOut, (barcodeConnector.barcodeToWrite.get(0).waitUplinkResponse ? 500 : 0));
                    }
                    ready2Write = false;
                    appendToLog("ready2Write is set false after true sendBarcodeToWrite");
                } else if (rfidConnector.rfidPowerOnTimeOut != 0) {
                    if (DEBUG || true) appendToLog("rfidPowerOnTimeOut = " + rfidConnector.rfidPowerOnTimeOut + ", mRfidToWrite.size() = " + rfidConnector.mRfidToWrite.size());
                } else if (rfidConnector.rfidFailure == false && rfidConnector.mRfidToWrite.size() != 0) {
                    if (utility.DEBUG_BTDATA) appendToLog("BtData: CsReaderConnector.mReadWriteRunnable rfidFailure is false and mRfidToWrite.size is " + rfidConnector.mRfidToWrite.size());
                    if (isBleConnected() == false) {
                        rfidConnector.mRfidToWrite.clear();
                    } else {
                        if (utility.DEBUG_BTDATA)
                            appendToLog("BtData: CsReaderConnector.mReadWriteRunnable 3 currentTime = " + System.currentTimeMillis() + ", btSendTime = " + btSendTime + ", difference = " + (System.currentTimeMillis() - btSendTime) + ", btSendTimeOut = " + btSendTimeOut);
                        if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                            boolean retValue = false;
                            byte[] dataOut = rfidConnector.sendRfidToWrite();
                            if (dataOut != null) {
                                retValue = writeData(dataOut, (rfidConnector.mRfidToWrite.get(0).waitUplinkResponse ? 500 : 0));
                                if (false) appendToLog("done writeData with waitUplinkResponse = " + rfidConnector.mRfidToWrite.get(0).waitUplinkResponse);

                                if (false) appendToLog("AAA sending rifd data = " + byteArrayToString(dataOut));
                                String string = byteArrayToString(dataOut).substring(16);
                                String stringCompare = "800280B310A";
                                if (bis108) stringCompare = "8002700100F00F000000";
                                if (false) appendToLog("AAA sending rifd data portion = " + string + ", " + string.indexOf(stringCompare));
                                if (string.indexOf(stringCompare) == 0)
                                    rfidReader.setInventoring(true);
                            }
                            if (DEBUG)
                                appendToLog("BtDataOut: done writeRfid with size = " + rfidConnector.mRfidToWrite.size() + ", PayloadEvents = " + rfidConnector.mRfidToWrite.get(0).rfidPayloadEvent.toString() + ", data=" + byteArrayToString(rfidConnector.mRfidToWrite.get(0).dataValues));
                            rfidConnector.sendRfidToWriteSent++;
                            if (retValue) {
                                rfidConnector.mRfidToWriteRemoved = false;
                                if (DEBUG)
                                    appendToLog("writeRfid() with sendRfidToWriteSent = " + rfidConnector.sendRfidToWriteSent);
                                sendFailure = false;
                                //bValue = true;
                            } else sendFailure = true;

                            if (retValue) {
                                if (false) appendToLog("ready2Write is set false after true sendRfidToWrite");
                                ready2Write = false;
                            }
                        }
                    }
                }
            }
            /*if (validBuffer) {
                if (DEBUG)  appendToLog("mReadWriteRunnable: END, timer2Write=" + timer2Write + ", streamInBufferSize = " + bluetoothGatt.getStreamInBufferSize() + ", mRfidToRead.size=" + rfidConnector.mRfidToRead.size() + ", mRx000ToRead.size=" + rfidReader.mRx000ToRead.size());
            }*/
            //appendToLog("mRfidDevice is " + (mRfidDevice == null ? "null" : "valid"));
            //appendToLog("mRfidDevice.mRfidReaderChip is " + (mRfidDevice.mRfidReaderChip == null ? "null" : "valid"));
            //appendToLog("mRfidDevice.mRfidReaderChip.mRfidReaderChip is " + (mRfidDevice.mRfidReaderChip.mRfidReaderChip == null ? "null" : "valid"));
            if (rfidReader != null) rfidReader.uplinkHandler();
            if (DEBUGTHREAD) appendToLog("mReadWriteRunnable: mReadWriteRunnable ends");
        }
    };

    int intervalRx000UplinkHandler = 250;
    /*private final Runnable runnableRx000UplinkHandler = new Runnable() {
        @Override
        public void run() {
//            mRfidDevice.mRx000Device.mRx000UplinkHandler();
            mHandler.postDelayed(runnableRx000UplinkHandler, intervalRx000UplinkHandler);
        }
    };
    */
}

