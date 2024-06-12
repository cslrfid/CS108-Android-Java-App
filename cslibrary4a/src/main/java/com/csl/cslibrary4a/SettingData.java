package com.csl.cslibrary4a;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingData {
    final boolean DEBUG_FILE = false;

    public int channel = -1;
    int antennaPower = -1;
    Context context; Utility utility; BluetoothGatt bluetoothGatt; NotificationConnector notificationConnector; RfidReader rfidReader;
    //CsReaderConnector csReaderConnector;

    public SettingData(Context context, Utility utility, BluetoothGatt bluetoothGatt, NotificationConnector notificationConnector, RfidReader rfidReader) {
        this.context = context;
        this.utility = utility;
        this.bluetoothGatt = bluetoothGatt;
        this.notificationConnector = notificationConnector;
        this.rfidReader = rfidReader;
        //this.csReaderConnector = csReaderConnector;
        loadSettingFile0();
    }

    private void appendToLog(String s) {
        utility.appendToLog(s);
    }

    //Operation -- regulatory region, power order, fixed channel,
    //          -- power level (assume 1), enable (assume enable), power, dwell, intraPacket delay, duplication elimination,
    //          -- tag population, Q value
    //          -- session, target, tag focus, fast id,
    //          -- query algorithm
    //          -- minimum minQ cycles
    //          -- reader mode

    //Administration    -- reader model, reader name,
    //                  -- battery level format
    //                  -- inventory rssi unit, beep trigger enable/count, vibration enble/time, vibrate for, vibrate window
    //                  -- saving format, cloud enable, file saving enable, cloud address, cloud connect timeout
    //                  -- impinj verificaion address, name, password
    //                  -- foreground service enable, foreground reader
    //                  -- barcode trigger mode
    //                  -- enable debugging
    //public boolean inventoryBeepDefault = true;
    //boolean inventoryBeep = inventoryBeepDefault;
    //public boolean getInventoryBeep() {
    //    return inventoryBeep;
    //}
    //public boolean setInventoryBeep(boolean inventoryBeep) {
    //    this.inventoryBeep = inventoryBeep;
    //    return true;
    //}


    public int batteryDisplaySelectDefault = 1, batteryDisplaySelect = batteryDisplaySelectDefault;
    public int rssiDisplaySelectDefault = 1, rssiDisplaySelect = rssiDisplaySelectDefault;
    public boolean triggerReportingDefault = true, triggerReporting = triggerReportingDefault;
    public short triggerReportingCountSettingDefault = 1, triggerReportingCountSetting = triggerReportingCountSettingDefault;
    public boolean inventoryBeepDefault = true, inventoryBeep = inventoryBeepDefault;
    public int beepCountSettingDefault = 8, beepCountSetting = beepCountSettingDefault;
    public boolean inventoryVibrateDefault = false, inventoryVibrate = inventoryVibrateDefault;
    public int vibrateTimeSettingDefault = 300, vibrateTimeSetting = vibrateTimeSettingDefault;
    public int vibrateModeSelectDefault = 1, vibrateModeSelect = vibrateModeSelectDefault;
    public int vibrateWindowSettingDefault = 2, vibrateWindowSetting = vibrateWindowSettingDefault;

    public int savingFormatSelectDefault = 0, savingFormatSelect = savingFormatSelectDefault;
    public int csvColumnSelectDefault = 0, csvColumnSelect = csvColumnSelectDefault;
    public boolean saveFileEnableDefault = true, saveFileEnable = saveFileEnableDefault;
    public boolean saveCloudEnableDefault = false, saveCloudEnable = saveCloudEnableDefault;
    public boolean saveNewCloudEnableDefault = false, saveNewCloudEnable = saveNewCloudEnableDefault;
    public boolean saveAllCloudEnableDefault = false, saveAllCloudEnable = saveAllCloudEnableDefault;
    public String serverLocationDefault = "", serverLocation = serverLocationDefault;
    public int serverTimeoutDefault = 6, serverTimeout = serverTimeoutDefault;
    //String serverImpinjLocationDefault = "https://h9tqczg9-7275.asse.devtunnels.ms", serverImpinjLocation = serverImpinjLocationDefault;

    public String serverMqttLocationDefault = "", serverMqttLocation = serverMqttLocationDefault;
    public String topicMqttDefault = "", topicMqtt = topicMqttDefault;
    public int inventoryCloudSaveDefault = 0, inventoryCloudSave = inventoryCloudSaveDefault;
    public String serverImpinjLocationDefault = "https://democloud.convergence.com.hk/ias", serverImpinjLocation = serverImpinjLocationDefault;
    //String serverImpinjNameDefault = "wallace.sit@cne.com.hk", serverImpinjName = serverImpinjNameDefault;
    public String serverImpinjNameDefault = "", serverImpinjName = serverImpinjNameDefault;
    //String serverImpinjPasswordDefault = "Cne12345678?", serverImpinjPassword = serverImpinjPasswordDefault;
    public String serverImpinjPasswordDefault = "", serverImpinjPassword = serverImpinjPasswordDefault;

    public String strForegroundReaderDefault = "", strForegroundReader = strForegroundReaderDefault;
    public int iForegroundDupElimDefault = 1, iForegroundDupElim = iForegroundDupElimDefault;

    public boolean barcode2TriggerModeDefault = true, barcode2TriggerMode = barcode2TriggerModeDefault;

    public boolean userDebugEnableDefault = false, userDebugEnable = userDebugEnableDefault;

    //Filter
    public static class PreFilterData {
        public boolean enable; public int target;
        public int action;
        public int bank;
        public int offset; public String mask; public boolean maskbit;
        public PreFilterData() { }
        public PreFilterData(boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
            this.enable = enable;
            this.target = target;
            this.action = action;
            this.bank = bank;
            this.offset = offset;
            this.mask = mask;
            this.maskbit = maskbit;
        }
    }
    public PreFilterData preFilterData;

    //Wedge -- Power, Prefix, Suffix, Delimiter
    public String wedgePrefix = null;
    public String wedgeSuffix = null; public int wedgeDelimiter = 0x0a;

    //File handling
    File file0;
    public File file;
    boolean loadSetting1File() {
        File path = context.getFilesDir();
        String fileName = bluetoothGatt.getmBluetoothDevice().getAddress();

        fileName = "cs108A_" + fileName.replaceAll(":", "");
        file = new File(path, fileName);
        boolean bNeedDefault = true, DEBUG = false;
        if (DEBUG_FILE) utility.appendToLogView("FileName = " + fileName + ".exits = " + file.exists() + ", with beepEnable = " + inventoryBeep);
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(instream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    int queryTarget = -1; int querySession = -1; int querySelect = -1;
                    int startQValue = -1; int maxQValue = -1; int minQValue = -1; int retryCount = -1;
                    int fixedQValue = -1; int fixedRetryCount = -1;
                    int population = -1;
                    boolean invAlgo = true; int retry = -1;
                    preFilterData = new PreFilterData();
                    while ((line = bufferedReader.readLine()) != null) {
                        appendToLog("line = " + line);
                        if (DEBUG_FILE || true) appendToLog("Data read = " + line);
                        String[] dataArray = line.split(",");
                        if (dataArray.length == 2) {
                            if (dataArray[0].matches("appVersion")) {
//                                if (dataArray[1].matches(super.getlibraryVersion())) bNeedDefault = false;
                            } else if (bNeedDefault == true) {
                            } else if (dataArray[0].matches("countryInList")) {
                                rfidReader.getRegionList();
                                int countryInListNew = Integer.valueOf(dataArray[1]);
//                                if (countryInList != countryInListNew && countryInListNew >= 0) setCountryInList(countryInListNew);
//                                channelOrderType = -1;
                            } else if (dataArray[0].matches("channel")) {
                                int channelNew = Integer.valueOf(dataArray[1]);
//                                if (getChannelHoppingStatus() == false && channelNew >= 0) setChannel(channelNew);
                            } else if (dataArray[0].matches("antennaPower")) {
                                long lValue = Long.valueOf(dataArray[1]);
//                                if (lValue >= 0) setPowerLevel(lValue);
                            } else if (dataArray[0].matches("population")) {
                                population = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("querySession")) {
                                int iValue = Integer.valueOf(dataArray[1]);
                                if (iValue >= 0) querySession = iValue;
                            } else if (dataArray[0].matches("queryTarget")) {
                                queryTarget = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("tagFocus")) {
                                int iValue = Integer.valueOf(dataArray[1]);
//                                if (iValue >= 0) csReaderConnector.rfidReader.tagFocus = iValue;
                            } else if (dataArray[0].matches("fastId")) {
                                int iValue = Integer.valueOf(dataArray[1]);
//                                if (iValue >= 0) csReaderConnector.rfidReader.fastId = iValue;
                            } else if (dataArray[0].matches("invAlgo")) {
                                invAlgo = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches("retry")) {
                                retry = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("currentProfile")) {
                                int iValue = Integer.valueOf(dataArray[1]);
//                                if (iValue >= 0) setCurrentLinkProfile(iValue);
                            } else if (dataArray[0].matches("rxGain")) {
//                                setRxGain(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("deviceName")) {
//                                bluetoothConnector.deviceName = dataArray[1].getBytes();
                            } else if (dataArray[0].matches("batteryDisplay")) {
//                                setBatteryDisplaySetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("rssiDisplay")) {
//                                setRssiDisplaySetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("tagDelay")) {
//                                setTagDelay(Byte.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("cycleDelay")) {
//                                setCycleDelay(Long.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("intraPkDelay")) {
//                                setIntraPkDelay(Byte.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("dupDelay")) {
//                                setDupDelay(Byte.valueOf(dataArray[1]));

                            } else if (dataArray[0].matches(("triggerReporting"))) {
                                notificationConnector.setTriggerReporting(dataArray[1].matches("true") ? true : false);
                            } else if (dataArray[0].matches(("triggerReportingCount"))) {
                                notificationConnector.setTriggerReportingCount(Short.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("inventoryBeep"))) {
                                inventoryBeep = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("inventoryBeepCount"))) {
                                beepCountSetting = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("inventoryVibrate"))) {
                                inventoryVibrate = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("inventoryVibrateTime"))) {
                                vibrateTimeSetting = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("inventoryVibrateMode"))) {
                                vibrateModeSelect = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("savingFormat"))) {
                                savingFormatSelect = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("csvColumnSelect"))) {
                                csvColumnSelect = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("inventoryVibrateWindow"))) {
                                vibrateWindowSetting = Integer.valueOf(dataArray[1]);

                            } else if (dataArray[0].matches(("saveFileEnable"))) {
                                saveFileEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveCloudEnable"))) {
                                saveCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveNewCloudEnable"))) {
                                saveNewCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveAllCloudEnable"))) {
                                saveAllCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("serverLocation"))) {
                                serverLocation = dataArray[1];
                            } else if (dataArray[0].matches("serverTimeout")) {
                                serverTimeout = Integer.valueOf(dataArray[1]);

                            } else if (dataArray[0].matches(("serverImpinjLocation"))) {
                                serverImpinjLocation = dataArray[1];
                            } else if (dataArray[0].matches(("serverImpinjName"))) {
                                serverImpinjName = dataArray[1];
                            } else if (dataArray[0].matches(("serverImpinjPassword"))) {
                                serverImpinjPassword = dataArray[1];

                            } else if (dataArray[0].matches("barcode2TriggerMode")) {
                                if (dataArray[1].matches("true")) barcode2TriggerMode = true;
                                else barcode2TriggerMode = false;
//                            } else if (dataArray[0].matches("wedgePrefix")) {
//                                setWedgePrefix(dataArray[1]);
//                            } else if (dataArray[0].matches("wedgeSuffix")) {
//                                setWedgeSuffix(dataArray[1]);
//                            } else if (dataArray[0].matches("wedgeDelimiter")) {
//                                setWedgeDelimiter(Integer.valueOf(dataArray[1]));

                            } else if (dataArray[0].matches("preFilterData.enable")) {
                                if (dataArray[1].matches("true")) preFilterData.enable = true;
                                else preFilterData.enable  = false;
                            } else if (dataArray[0].matches("preFilterData.target")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.target = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.action")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.action = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.bank")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.bank = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.offset")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.offset = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.mask")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.mask = dataArray[1];
                            } else if (dataArray[0].matches("preFilterData.maskbit")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                if (dataArray[1].matches("true")) preFilterData.maskbit = true;
                                else preFilterData.maskbit = false;

                            } else if (dataArray[0].matches(("userDebugEnable"))) {
                                userDebugEnable = dataArray[1].matches("true") ? true : false;
                            }
                        }
                    }
//                    setInvAlgo(invAlgo); setPopulation(population); setRetryCount(retry); setTagGroup(querySelect, querySession, queryTarget); setTagFocus(csReaderConnector.rfidReader.tagFocus > 0 ? true : false);
                    if (preFilterData != null && preFilterData.enable) {
                        appendToLog("preFilterData is valid. Going to setSelectCriteria");
//                        setSelectCriteria(0, preFilterData.enable, preFilterData.target, preFilterData.action, preFilterData.bank, preFilterData.offset, preFilterData.mask, preFilterData.maskbit);
                    } else {
                        appendToLog("preFilterData is null or disabled. Going to setSelectCriteriaDisable");
//                        setSelectCriteriaDisable(0);
                    }
                }
                instream.close();
                if (DEBUG_FILE) appendToLog("Data is read from FILE.");
            } catch (Exception ex) {
                //
            }
        }
        if (bNeedDefault) {
            appendToLog("saveSetting2File default !!!");
//            setReaderDefault();
//            saveSetting2File();
        }
        return bNeedDefault;
    }
    boolean loadSettingFile0() {
        File path = context.getFilesDir();
        String fileName = "cs108A_standby";
        file0 = new File(path, fileName);
        appendToLog("file0.exists = " + file0.exists());
        if (file0.exists()) {
            try {
                InputStream instream = new FileInputStream(file0);
                appendToLog("file0.instream is " + (instream == null ? "null" : "valid"));
                if (instream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(instream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        appendToLog("file0.line: " + line);
                        String[] dataArray = line.split(",");
                        if (dataArray.length == 2) {
                            if (dataArray[0].matches("foreground.reader")) {
                                strForegroundReader = dataArray[1];
                                appendToLog("file0.foreground.reader = " + strForegroundReader);
                                appendToLog("loaded strForegroundReader is " + this.strForegroundReader);
                            }
                        }
                    }
                }
                instream.close();
            } catch (Exception ex) {
                //
            }
        }
        return true;
    }
    public void saveSetting2File0() {
        boolean DEBUG = true;
        if (DEBUG) appendToLog("Start");

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file0);
            write2FileStream(stream, "Start of File0 data\n");

            write2FileStream(stream, "foreground.enable," + "true" + "\n");
            appendToLog("strForegroundReader = " + strForegroundReader);
            write2FileStream(stream, "foreground.reader," + strForegroundReader + "\n");
            write2FileStream(stream, "End of File0 data\n");
            stream.close();
        } catch (Exception ex) {
            //
        }
    }
    public void write2FileStream(FileOutputStream stream, String string) {
        boolean DEBUG = false;
        try {
            stream.write(string.getBytes());
            if (true) appendToLog("outData = " + string);
        } catch (Exception ex) {
        }
    }
}
