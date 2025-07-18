package com.csl.cslibrary4a;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingData {
    final boolean DEBUG_FILE = true;

    public int channel = -1;
    int antennaPower = -1;
    Context context; Utility utility; NotificationConnector notificationConnector; RfidReader rfidReader; CsReaderConnector csReaderConnector;
    //CsReaderConnector csReaderConnector;

    public SettingData(Context context, Utility utility, CsReaderConnector csReaderConnector) {
        this.context = context;
        this.utility = utility;
        this.csReaderConnector = csReaderConnector;
        appendToLog("SettingData:"
                + "\nthis.context is " + (this.context == null ? "null" : "valid")
                + "\nthis.utility is " + (this.utility == null ? "null" : "valid")
                + "\nthis.csReaderConnector is " + (this.csReaderConnector == null ? "null" : "valid")
        );
        loadForegroundSettingFile();
    }
    public void setConnectedConnectors(NotificationConnector notificationConnector, RfidReader rfidReader) {
        this.notificationConnector = notificationConnector;
        this.rfidReader = rfidReader;
        appendToLog("SettingData: this.notificationConnector is " + (this.notificationConnector == null ? "null" : "valid")
                + "\nthis.rfidReader is " + (this.rfidReader == null ? "null" : "valid")
        );
    }

    private void appendToLog(String s) {
        utility.appendToLog(s);
    }

    public void write2FileStream(FileOutputStream stream, String string) {
        boolean DEBUG = true;
        try {
            stream.write(string.getBytes());
            if (true) appendToLog("FileA outData = " + string);
        } catch (Exception ex) {
        }
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
    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        if (batteryDisplaySelect < 0 || batteryDisplaySelect > 1)   return false;
        this.batteryDisplaySelect = batteryDisplaySelect;
        return true;
    }
    public int rssiDisplaySelectDefault = 1, rssiDisplaySelect = rssiDisplaySelectDefault;
    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        if (rssiDisplaySelect < 0 || rssiDisplaySelect > 1)   return false;
        this.rssiDisplaySelect = rssiDisplaySelect;
        return true;
    }

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

    public File fileSetting;
    boolean loadSettingFile(String stringMacAddress, String strlibraryVersion, boolean bChannelHoppingStatus, int iCurrentProfile) {
        boolean DEBUG = true;
        appendToLog("SettingData.loadSettingFile:"
                + "\nstringMacAddress = " + stringMacAddress
                + "\nstrlibraryVersion = " + strlibraryVersion
                + "\nbChannelHoppingStatus = " + bChannelHoppingStatus
                + "\niCurrentProfile = " + iCurrentProfile);

        File path = context.getFilesDir();
        String fileName = stringMacAddress; //bluetoothGatt.getmBluetoothDevice().getAddress(); //stringMacAddress

        fileName = "csReaderA_" + fileName.replaceAll(":", "").replaceAll(" ", "");
        fileSetting = new File(path, fileName);
        boolean bNeedDefault = true;
        if (DEBUG_FILE) utility.appendToLogView("FileName = " + fileName + ".exits = " + fileSetting.exists() + ", with beepEnable = " + inventoryBeep);
        if (fileSetting.exists()) {
            InputStream instream = null;
            try {
                instream = new FileInputStream(fileSetting);
            } catch (Exception ex) {
                //
            }
            if (instream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(instream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                int queryTarget = -1; int querySession = -1; int querySelect = -1;
                int startQValue = -1; int maxQValue = -1; int minQValue = -1; int retryCount = -1;
                int fixedQValue = -1; int fixedRetryCount = -1;
                int population = -1;
                boolean invAlgo = true; int retry = -1;
                preFilterData = new SettingData.PreFilterData();
                while(true) {
                    line = null;
                    try {
                        line = bufferedReader.readLine();
                    } catch (Exception ex) {
                        //
                    }
                    if (line == null) break;
                    if (DEBUG_FILE || true) appendToLog("FileA Data read = " + line);
                    String[] dataArray = line.split(",");
                    if (dataArray.length == 2) {
                        if (dataArray[0].matches("appVersion")) {
                            appendToLog("datArray[1] = " + dataArray[1] + ", strlibraryVersion = " + strlibraryVersion);
                            if (dataArray[1].matches(strlibraryVersion)) bNeedDefault = false;
                        } else if (bNeedDefault == true) {
                        } else if (dataArray[0].matches("countryInList")) {
                            csReaderConnector.rfidReader.getRegionList(); //getRegionList();
                            int countryInListNew = Integer.valueOf(dataArray[1]);
                            if (csReaderConnector.rfidReader.countryInList != countryInListNew && countryInListNew >= 0) csReaderConnector.rfidReader.setCountryInList(countryInListNew);
                            csReaderConnector.rfidReader.channelOrderType = -1;
                        } else if (dataArray[0].matches("channel")) {
                            int channelNew = Integer.valueOf(dataArray[1]);
                            if (bChannelHoppingStatus == false && channelNew >= 0) csReaderConnector.rfidReader.setChannel(channelNew);
                        } else if (dataArray[0].matches("antennaPower")) {
                            long lValue = Long.valueOf(dataArray[1]);
                            if (lValue >= 0) csReaderConnector.rfidReader.setPowerLevel(lValue);
                        } else if (dataArray[0].matches("antennaDwell")) {
                            long lValue = Long.valueOf(dataArray[1]);
                            if (lValue >= 0) {
                                csReaderConnector.rfidReader.setAntennaDwell(lValue);
                            }
                        } else if (dataArray[0].matches("population")) {
                            population = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("querySession")) {
                            int iValue = Integer.valueOf(dataArray[1]);
                            if (iValue >= 0) querySession = iValue;
                        } else if (dataArray[0].matches("queryTarget")) {
                            queryTarget = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("tagFocus")) {
                            int iValue = Integer.valueOf(dataArray[1]);
                            if (iValue >= 0) csReaderConnector.rfidReader.tagFocus = iValue;
                        } else if (dataArray[0].matches("fastId")) {
                            int iValue = Integer.valueOf(dataArray[1]);
                            if (iValue >= 0) csReaderConnector.rfidReader.fastId = iValue;
                        } else if (dataArray[0].matches("invAlgo")) {
                            invAlgo = dataArray[1].matches("true") ? true : false;
                        } else if (dataArray[0].matches("retry")) {
                            retry = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("currentProfile")) {
                            int iValue = Integer.valueOf(dataArray[1]);
                            appendToLog("SettingData.loadSettingFile setCurrentLinkProfile as " + iValue);
                            if (iValue >= 0) csReaderConnector.rfidReader.setCurrentLinkProfile(iValue);
                        } else if (dataArray[0].matches("rxGain")) {
                            csReaderConnector.rfidReader.setRxGain(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("deviceName")) {
                            csReaderConnector.bluetoothConnector.deviceName = dataArray[1].getBytes();
                        } else if (dataArray[0].matches("batteryDisplay")) {
                            setBatteryDisplaySetting(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("rssiDisplay")) {
                            setRssiDisplaySetting(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("tagDelay")) {
                            csReaderConnector.rfidReader.setTagDelay(Byte.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("cycleDelay")) {
                            csReaderConnector.rfidReader.setCycleDelay(Long.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("intraPkDelay")) {
                            csReaderConnector.rfidReader.setIntraPkDelay(Byte.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches("dupDelay")) {
                            csReaderConnector.rfidReader.setDupDelay(Byte.valueOf(dataArray[1]));

                        } else if (dataArray[0].matches(("triggerReporting"))) {
                            appendToLog("FileA: going to setTriggerReporting with notificationConnector as " + (notificationConnector == null ? "null" : "valid"));
                            notificationConnector.setTriggerReporting(dataArray[1].matches("true") ? true : false);
                            appendToLog("FileA: setTriggerReporting is done");
                        } else if (dataArray[0].matches(("triggerReportingCount"))) {
                            notificationConnector.setTriggerReportingCount(Short.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("inventoryBeep"))) {
                            inventoryBeep = dataArray[1].matches("true") ? true : false; //setInventoryBeep(dataArray[1].matches("true") ? true : false);
                        } else if (dataArray[0].matches(("inventoryBeepCount"))) {
                            beepCountSetting = Integer.valueOf(dataArray[1]); //setBeepCount(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("inventoryVibrate"))) {
                            inventoryVibrate = dataArray[1].matches("true") ? true : false; //setInventoryVibrate(dataArray[1].matches("true") ? true : false);
                        } else if (dataArray[0].matches(("inventoryVibrateTime"))) {
                            vibrateTimeSetting = Integer.valueOf(dataArray[1]); //setVibrateTime(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("inventoryVibrateMode"))) {
                            vibrateModeSelect = Integer.valueOf(dataArray[1]); //setVibrateModeSetting(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("savingFormat"))) {
                            savingFormatSelect = Integer.valueOf(dataArray[1]); //setSavingFormatSetting(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("csvColumnSelect"))) {
                            csvColumnSelect = Integer.valueOf(dataArray[1]); //setCsvColumnSelectSetting(Integer.valueOf(dataArray[1]));
                        } else if (dataArray[0].matches(("inventoryVibrateWindow"))) {
                            vibrateWindowSetting = Integer.valueOf(dataArray[1]); //setVibrateWindow(Integer.valueOf(dataArray[1]));

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
                        } else if (dataArray[0].matches(("serverMqttLocation"))) {
                            serverMqttLocation = dataArray[1];
                        } else if (dataArray[0].matches(("topicMqtt"))) {
                            topicMqtt = dataArray[1];
                        } else if (dataArray[0].matches(("foregroundDupElim"))) {
                            iForegroundDupElim = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches(("inventoryCloudSave"))) {
                            inventoryCloudSave = Integer.parseInt(dataArray[1]);
                        } else if (dataArray[0].matches(("serverImpinjLocation"))) {
                            serverImpinjLocation = dataArray[1];
                        } else if (dataArray[0].matches(("serverImpinjName"))) {
                            serverImpinjName = dataArray[1];
                        } else if (dataArray[0].matches(("serverImpinjPassword"))) {
                            serverImpinjPassword = dataArray[1];

                        } else if (dataArray[0].matches("barcode2TriggerMode")) {
                            if (dataArray[1].matches("true")) barcode2TriggerMode = true;
                            else barcode2TriggerMode = false;
                        } else if (dataArray[0].matches("preFilterData.enable")) {
                            if (dataArray[1].matches("true")) preFilterData.enable = true;
                            else preFilterData.enable  = false;
                        } else if (dataArray[0].matches("preFilterData.target")) {
                            preFilterData.target = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("preFilterData.action")) {
                            preFilterData.action = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("preFilterData.bank")) {
                            preFilterData.bank = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("preFilterData.offset")) {
                            preFilterData.offset = Integer.valueOf(dataArray[1]);
                        } else if (dataArray[0].matches("preFilterData.mask")) {
                            preFilterData.mask = dataArray[1];
                        } else if (dataArray[0].matches("preFilterData.maskbit")) {
                            if (dataArray[1].matches("true")) preFilterData.maskbit = true;
                            else preFilterData.maskbit = false;
                        } else if (dataArray[0].matches(("userDebugEnable"))) {
                            userDebugEnable = dataArray[1].matches("true") ? true : false;
                        }
                    }
                }
                csReaderConnector.rfidReader.setInvAlgo(invAlgo);
                csReaderConnector.rfidReader.setPopulation(population);
                csReaderConnector.rfidReader.setRetryCount(retry);
                csReaderConnector.rfidReader.setTagGroup(querySelect, querySession, queryTarget);
                csReaderConnector.rfidReader.setTagFocus(csReaderConnector.rfidReader.tagFocus > 0 ? true : false);
                if (preFilterData != null && preFilterData.enable) {
                    if (utility.DEBUG_SELECT) appendToLog("Debug_Select: SettingData.loadingSettingFile. preFilterData is valid. Going to setSelectCriteria");
                    appendToLog("BtDataOut BBB 5");
                    csReaderConnector.rfidReader.setSelectCriteria(0, preFilterData.enable, preFilterData.target, preFilterData.action, preFilterData.bank, preFilterData.offset, preFilterData.mask, preFilterData.maskbit);
                } else {
                    if (utility.DEBUG_SELECT) appendToLog("Debug_Select: SettingData.loadingSettingFile. preFilterData is null or disabled. Going to setSelectCriteriaDisable");
                    csReaderConnector.rfidReader.setSelectCriteriaDisable(0);
                }
            }
            try {
                instream.close();
            } catch (Exception ex) { }
            if (DEBUG_FILE) appendToLog("Data is read from FILE.");
        }
        if (bNeedDefault) {
            appendToLog("saveSetting2File default !!!");
            csReaderConnector.rfidReader.setReaderDefault();
            saveSetting2File(strlibraryVersion, bChannelHoppingStatus, iCurrentProfile);
        }
        return bNeedDefault;
    }
    public void saveSetting2File(String strLibraryVersion, boolean bChannelHoppingStatus, int iCurrentProfile) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("Start");
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(fileSetting);
            write2FileStream(stream, "Start of data\n");

            write2FileStream(stream, "appVersion," + strLibraryVersion + "\n");
            write2FileStream(stream, "countryInList," + String.valueOf(csReaderConnector.rfidReader.countryInList + "\n"));
            if (!bChannelHoppingStatus) write2FileStream(stream, "channel," + String.valueOf(channel + "\n"));

            write2FileStream(stream, "antennaPower," + String.valueOf(csReaderConnector.rfidReader.getPwrlevel() + "\n"));
            write2FileStream(stream, "antennaDwell," + String.valueOf(csReaderConnector.rfidReader.getAntennaDwell() + "\n"));
            write2FileStream(stream, "population," + String.valueOf(csReaderConnector.rfidReader.getPopulation() +"\n"));
            write2FileStream(stream, "querySession," + String.valueOf(csReaderConnector.rfidReader.getQuerySession() + "\n"));
            write2FileStream(stream, "queryTarget," + String.valueOf(csReaderConnector.rfidReader.getQueryTarget() + "\n"));
            write2FileStream(stream, "tagFocus," + String.valueOf(csReaderConnector.rfidReader.getTagFocus() + "\n"));
            write2FileStream(stream, "fastId," + String.valueOf(csReaderConnector.rfidReader.getFastId() + "\n"));
            write2FileStream(stream, "invAlgo," + String.valueOf(csReaderConnector.rfidReader.getInvAlgo() + "\n"));
            write2FileStream(stream, "retry," + String.valueOf(csReaderConnector.rfidReader.getRetryCount() + "\n"));
            int iValue = csReaderConnector.rfidReader.getCurrentProfile();
            appendToLog("SettingData.saveSettingFile getCurrentProfile as " + iValue);
            write2FileStream(stream, "currentProfile," + String.valueOf(iValue + "\n"));
            write2FileStream(stream, "rxGain," + String.valueOf(csReaderConnector.rfidReader.getRxGain() + "\n"));

            write2FileStream(stream, "deviceName," + csReaderConnector.bluetoothConnector.getBluetoothIcName() + "\n");
            write2FileStream(stream, "batteryDisplay," + String.valueOf(batteryDisplaySelect + "\n"));
            write2FileStream(stream, "rssiDisplay," + String.valueOf(rssiDisplaySelect + "\n"));
            write2FileStream(stream, "tagDelay," + String.valueOf(csReaderConnector.rfidReader.getTagDelay() + "\n"));
            write2FileStream(stream, "cycleDelay," + String.valueOf(csReaderConnector.rfidReader.getCycleDelay() + "\n"));
            write2FileStream(stream, "intraPkDelay," + String.valueOf(csReaderConnector.rfidReader.getIntraPkDelay() + "\n"));
            write2FileStream(stream, "dupDelay," + String.valueOf(csReaderConnector.rfidReader.getDupDelay() + "\n"));

            write2FileStream(stream, "triggerReporting," + String.valueOf(notificationConnector.getTriggerReporting() + "\n"));
            write2FileStream(stream, "triggerReportingCount," + String.valueOf(notificationConnector.getTriggerReportingCount() + "\n"));
            write2FileStream(stream, "inventoryBeep," + String.valueOf(inventoryBeep + "\n"));
            write2FileStream(stream, "inventoryBeepCount," + String.valueOf(beepCountSetting + "\n"));
            write2FileStream(stream, "inventoryVibrate," + String.valueOf(inventoryVibrate + "\n"));
            write2FileStream(stream, "inventoryVibrateTime," + String.valueOf(vibrateTimeSetting + "\n"));
            write2FileStream(stream, "inventoryVibrateMode," + String.valueOf(vibrateModeSelect + "\n"));
            write2FileStream(stream, "inventoryVibrateWindow," + String.valueOf(vibrateWindowSetting + "\n"));

            write2FileStream(stream, "savingFormat," + String.valueOf(savingFormatSelect + "\n"));
            write2FileStream(stream, "csvColumnSelect," + String.valueOf(csvColumnSelect + "\n"));
            write2FileStream(stream, "saveFileEnable," + String.valueOf(saveFileEnable + "\n"));
            write2FileStream(stream, "saveCloudEnable," + String.valueOf(saveCloudEnable + "\n"));
            write2FileStream(stream, "saveNewCloudEnable," + String.valueOf(saveNewCloudEnable + "\n"));
            write2FileStream(stream, "saveAllCloudEnable," + String.valueOf(saveAllCloudEnable + "\n"));
            write2FileStream(stream, "serverLocation," + serverLocation + "\n");
            write2FileStream(stream, "serverTimeout," + String.valueOf(serverTimeout + "\n"));
            write2FileStream(stream, "serverMqttLocation," + serverMqttLocation + "\n");
            write2FileStream(stream, "topicMqtt," + topicMqtt + "\n");
            write2FileStream(stream, "foregroundDupElim," + String.valueOf(iForegroundDupElim + "\n"));
            write2FileStream(stream, "inventoryCloudSave," + inventoryCloudSave + "\n");
            write2FileStream(stream, "serverImpinjLocation," + serverImpinjLocation + "\n");
            write2FileStream(stream, "serverImpinjName," + serverImpinjName + "\n");
            write2FileStream(stream, "serverImpinjPassword," + serverImpinjPassword + "\n");
            write2FileStream(stream, "barcode2TriggerMode," + String.valueOf(barcode2TriggerMode + "\n"));

//            write2FileStream(stream, "wedgePrefix," + getWedgePrefix() + "\n");
//            write2FileStream(stream, "wedgeSuffix," + getWedgeSuffix() + "\n");
//            write2FileStream(stream, "wedgeDelimiter," + String.valueOf(getWedgeDelimiter()) + "\n");

            write2FileStream(stream, "userDebugEnable," + String.valueOf(userDebugEnable + "\n"));
            if (preFilterData != null) {
                write2FileStream(stream, "preFilterData.enable," + String.valueOf(preFilterData.enable + "\n"));
                write2FileStream(stream, "preFilterData.target," + String.valueOf(preFilterData.target + "\n"));
                write2FileStream(stream, "preFilterData.action," + String.valueOf(preFilterData.action + "\n"));
                write2FileStream(stream, "preFilterData.bank," + String.valueOf(preFilterData.bank + "\n"));
                write2FileStream(stream, "preFilterData.offset," + String.valueOf(preFilterData.offset + "\n"));
                write2FileStream(stream, "preFilterData.mask," + String.valueOf(preFilterData.mask + "\n"));
                write2FileStream(stream, "preFilterData.maskbit," + String.valueOf(preFilterData.maskbit + "\n"));
            }

            write2FileStream(stream, "End of data\n"); //.getBytes()); if (DEBUG) appendToLog("outData = " + outData);
            stream.close();
        } catch (Exception ex){
            //
        }
        saveForegroundSetting2File();
    }

    public String strForegroundReaderDefault = "", strForegroundReader = strForegroundReaderDefault;
    File fileForegroundSetting;
    boolean loadForegroundSettingFile() {
        File path = context.getFilesDir();
        String fileName = "csReaderA_Foreground";
        fileForegroundSetting = new File(path, fileName);
        appendToLog("file0.exists = " + fileForegroundSetting.exists());
        if (fileForegroundSetting.exists()) {
            try {
                InputStream instream = new FileInputStream(fileForegroundSetting);
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
                                appendToLog("foregroundReader: set 5");
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
    public void saveForegroundSetting2File() {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("Start");

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(fileForegroundSetting);
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

    public static String wedgeDeviceName;
    public static String wedgeDeviceAddress;
    public static int wedgeDeviceUUID2p1;
    public String wedgePrefix = null, wedgeSuffix = null;
    public int wedgeOutput = 0, wedgeDelimiter = 0x0a, wedgePower = 300;
    public String fileNameWedgeSetting = "csReaderA_SimpleWedge";
    void loadWedgeSettingFile() {
        appendToLog("KKK: loadWedgeSettingFile starts");
        File path = context.getFilesDir();
        File file = new File(path, fileNameWedgeSetting);
        boolean bNeedDefault = true, DEBUG = false;
        appendToLog(fileNameWedgeSetting + "file.exists = " + file.exists());
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(instream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (true) appendToLog("Data read = " + line);
                        String[] dataArray = line.split(",");
                        if (dataArray.length == 2) {
                            if (dataArray[0].matches("wedgeDeviceName")) {
                                wedgeDeviceName = dataArray[1];
                            } else if (dataArray[0].matches("wedgeDeviceAddress")) {
                                wedgeDeviceAddress = dataArray[1];
                            } else if (dataArray[0].matches("wedgeDeviceUUID2p1")) {
                                wedgeDeviceUUID2p1 = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("wedgePower")) {
                                wedgePower = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("wedgePrefix")) {
                                wedgePrefix = dataArray[1];
                            } else if (dataArray[0].matches("wedgeSuffix")) {
                                wedgeSuffix = dataArray[1];
                            } else if (dataArray[0].matches("wedgeDelimiter")) {
                                wedgeDelimiter = Integer.valueOf(dataArray[1]);
                                appendToLog("MainActivity, loadWedgeSettingFile: wedgeDelimiter = " + wedgeDelimiter);
                            } else if (dataArray[0].matches("wedgeOutput")) {
                                wedgeOutput = Integer.valueOf(dataArray[1]);
                            }
                        }
                    }
                }
                instream.close();
            } catch (Exception ex) {
                //
            }
        }
    }
    void saveWedgeSetting2File() {
        appendToLog("KKK: saveWedgeSetting2File starts");
        File path = context.getFilesDir();
        File file = new File(path, fileNameWedgeSetting);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            write2FileStream(stream, "Start of data\n");
            write2FileStream(stream, "wedgeDeviceName," + wedgeDeviceName + "\n");
            write2FileStream(stream, "wedgeDeviceAddress," + wedgeDeviceAddress + "\n");
            write2FileStream(stream, "wedgeDeviceUUID2p1," + String.valueOf(wedgeDeviceUUID2p1) + "\n");
            write2FileStream(stream, "wedgePower," + wedgePower + "\n");
            write2FileStream(stream, "wedgePrefix," + wedgePrefix + "\n");
            write2FileStream(stream, "wedgeSuffix," + wedgeSuffix + "\n");
            write2FileStream(stream, "wedgeDelimiter," + String.valueOf(wedgeDelimiter) + "\n"); appendToLog("SettingWedgeFragment, saveWedgeFragment: wedgeDelimiter = " + wedgeDelimiter);
            write2FileStream(stream, "wedgeOutput," + String.valueOf(wedgeOutput) + "\n");
            write2FileStream(stream, "End of data\n");
            stream.close();
        } catch (Exception ex){
            //
        }
    }
}
