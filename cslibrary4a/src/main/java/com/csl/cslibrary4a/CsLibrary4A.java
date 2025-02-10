package com.csl.cslibrary4a;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class CsLibrary4A {
    boolean DEBUG = false, DEBUG2 = false;
    String stringVersion = "14.10";
    Utility utility;
    Cs710Library4A cs710Library4A;
    Cs108Library4A cs108Library4A;

    public CsLibrary4A(Context context, TextView mLogView) {
        //utility = new Utility(context, mLogView);
        cs710Library4A = new Cs710Library4A(context, mLogView); utility = cs710Library4A.utility;
        cs108Library4A = new Cs108Library4A(context, mLogView);
        stringNOTCONNECT = " is called before Connection !!!";
        dBuV_dBm_constant = RfidReader.dBuV_dBm_constant;
        iNO_SUCH_SETTING = cs108Library4A.iNO_SUCH_SETTING;
    }
    public String getlibraryVersion() {
        if (DEBUG) Log.i("Hello2", "getlibraryVersion");
        String string710 = cs710Library4A.getlibraryVersion(); appendToLog("string710 = " + string710);
        int iPos0 = string710.indexOf(".");
        int iPos1 = string710.substring(iPos0 + 1).indexOf(".");
        String string108 = cs108Library4A.getlibraryVersion(); appendToLog("string108 = " + string108);
        int iPos2 = string108.indexOf(".");
        int iPos3 = string108.substring(iPos2 + 1).indexOf(".");
        return stringVersion + "-" + string710.substring(iPos0 + iPos1 + 2) + "-" + string108.substring(iPos2 + iPos3 + 2);
    }
    public String checkVersion() {
        if (DEBUG) Log.i("Hello2", "checkVersion");
        if (isCs108Connected()) return cs108Library4A.checkVersion();
        else if (isCs710Connected()) return cs710Library4A.checkVersion();
        else Log.i("Hello2", "checkVersion" + stringNOTCONNECT);
        return null;
    }

    //============ utility ============
    public String byteArrayToString(byte[] packet) {
        return utility.byteArrayToString(packet);
    }
    public void appendToLog(String s) {
        utility.appendToLog(s);
    }
    public void appendToLogView(String s) {
        utility.appendToLogView(s);
    }
    public String strFloat16toFloat32(String strData) {
        return utility.strFloat16toFloat32(strData);
    }
    public String str2float16(String strData) {
        return utility.str2float16(strData);
    }
    public float decodeCtesiusTemperature(String strActData, String strCalData) {
        return utility.decodeCtesiusTemperature(strActData, strCalData);
    }
    public float decodeMicronTemperature(int iTag35, String strActData, String strCalData) {
        return utility.decodeMicronTemperature(iTag35, strActData, strCalData);
    }
    public float decodeAsygnTemperature(String string) {
        return utility.decodeAsygnTemperature(string);
    }
    public String temperatureC2F(String strValue) {
        return utility.temperatureC2F(strValue);
    }
    public String temperatureF2C(String strValue) {
        return utility.temperatureF2C(strValue);
    }
    public String getUpcSerial(String strEpc) {
        return utility.getUpcSerial(strEpc);
    }
    public String getUpcSerialDetail(String strUpcSerial) {
        return utility.getUpcSerialDetail(strUpcSerial);
    }
    public String getEpc4upcSerial(Utility.EpcClass epcClass, String filter, String companyPrefix, String itemReference, String serialNumber) {
        return utility.getEpc4upcSerial(epcClass, filter, companyPrefix, itemReference, serialNumber);
    }
    public boolean checkHostProcessorVersion(String version, int majorVersion, int minorVersion, int buildVersion) {
        return utility.checkHostProcessorVersion(version, majorVersion, minorVersion, buildVersion);
    }

    //============ android bluetooth ============
    public boolean isBleScanning() {
        if (DEBUG) Log.i("Hello2", "isBleScanning");
        boolean bValue = false, bValue1 = false, bValue7 = false;
        bValue1 = cs108Library4A.isBleScanning();
        bValue7 = cs710Library4A.isBleScanning();
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Log.i("Hello2", "isBleScanning: bVAlue1 = " + bValue1 + ", bValue7 = " + bValue7);
        return bValue;
    }
    public boolean scanLeDevice(final boolean enable) {
        boolean bValue = false, bValue1 = false, bValue7 = false;
        if (DEBUG) Log.i("Hello2", "scanLeDevice");
        bValue1 = cs108Library4A.scanLeDevice(enable);
        bValue7 = cs710Library4A.scanLeDevice(enable);
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Log.i("Hello2", "scanLeDevice: bValue1 = " + bValue1 + ", bValue7 = " + bValue7);
        return bValue;
    }
    public BluetoothGatt.CsScanData getNewDeviceScanned() {
        if (DEBUG2) Log.i("Hello2", "getNewDeviceScanned");
        BluetoothGatt.CsScanData csScanData1;
        BluetoothGatt.CsScanData csScanData7 = cs710Library4A.getNewDeviceScanned();
        BluetoothGatt.CsScanData csScanData = null;
        if (csScanData7 == null) {
            csScanData1 = cs108Library4A.getNewDeviceScanned();
            if (csScanData1 != null) {
                csScanData = new BluetoothGatt.CsScanData(csScanData1.getDevice(), csScanData1.rssi, csScanData1.getScanRecord());
                csScanData.serviceUUID2p2 = csScanData1.serviceUUID2p2;
            }
        } else {
            csScanData = new BluetoothGatt.CsScanData(csScanData7.getDevice(), csScanData7.rssi, csScanData7.getScanRecord());
            csScanData.serviceUUID2p2 = csScanData7.serviceUUID2p2;
        }
        return csScanData;
    }
    public String getBluetoothDeviceAddress() {
        if (DEBUG) Log.i("Hello2", "getBluetoothDeviceAddress");
        if (isCs108Connected()) return cs108Library4A.getBluetoothDeviceAddress();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothDeviceAddress();
        else Log.i("Hello2", "getBluetoothDeviceAddress" + stringNOTCONNECT);
        return null;
    }
    public String getBluetoothDeviceName() {
        if (DEBUG) Log.i("Hello2", "getBluetoothDeviceName");
        if (isCs108Connected()) return cs108Library4A.getBluetoothDeviceName();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothDeviceName();
        else Log.i("Hello2", "getBluetoothDeviceName" + stringNOTCONNECT);
        return null;
    }
    public boolean isBleConnected() {
        boolean bValue = false;
        if (DEBUG2) Log.i("Hello2", "isBleConnected");
        if (isCs108Connected()) {
            bValue = cs108Library4A.isBleConnected();
            if (bValue == false) bConnectStatus = 0;
        } else if (isCs710Connected()) {
            bValue = cs710Library4A.isBleConnected();
            if (bValue == false) bConnectStatus = 0;
        } else {
            bValue = cs108Library4A.isBleConnected();
            if (bValue) bConnectStatus = 1;
            else {
                bValue = cs710Library4A.isBleConnected();
                if (bValue) bConnectStatus = 7;
                else bConnectStatus = 0;
            }
        }
        return bValue;
    }
    public void connect(ReaderDevice readerDevice) {
        if (DEBUG || true) Log.i("Hello2", "connect with readerDevice as " + (readerDevice != null ? "valid" : "null") + ", and iServiceUuidConnectedBefore = " + iServiceUuidConnectedBefore);
        int iServiceUuid = -1;
        if (readerDevice == null) iServiceUuid = iServiceUuidConnectedBefore;
        else iServiceUuid = readerDevice.getServiceUUID2p1();
        if (iServiceUuid == 0) {
            ReaderDevice readerDevice1 = null;
            if (readerDevice != null) readerDevice1 = new ReaderDevice(
                    readerDevice.getName(), readerDevice.getAddress(), readerDevice.getSelected(),
                    readerDevice.getDetails(), readerDevice.getCount(), readerDevice.getRssi(),
                    readerDevice.getServiceUUID2p1());
            cs108Library4A.connect(readerDevice1); iServiceUuidConnectedBefore = 0;
        } else if (iServiceUuid == 2) {
            cs710Library4A.connect(readerDevice); iServiceUuidConnectedBefore = 2;
        } else appendToLog("invalid serviceUUID = " + (readerDevice == null ? "null" : readerDevice.getServiceUUID2p1()));
    }
    public void disconnect(boolean tempDisconnect) {
        if (DEBUG) Log.i("Hello2", "disconnect");
        if (isCs108Connected()) cs108Library4A.disconnect(tempDisconnect);
        else if (isCs710Connected()) cs710Library4A.disconnect(tempDisconnect);
    }
    public boolean forceBTdisconnect() {
        if (DEBUG) Log.i("Hello2", "forceBTdisconnect");
        if (isCs108Connected()) return cs108Library4A.forceBTdisconnect();
        else if (isCs710Connected()) return cs710Library4A.forceBTdisconnect();
        else Log.i("Hello2", "forceBTdisconnect" + stringNOTCONNECT);
        return false;
    }
    public int getRssi() {
        if (DEBUG) Log.i("Hello2", "getRssi");
        if (isCs108Connected()) return cs108Library4A.getRssi();
        else if (isCs710Connected()) return cs710Library4A.getRssi();
        else Log.i("Hello2", "getRssi" + stringNOTCONNECT);
        return -1;
    } //411
    public long getStreamInRate() {
        if (DEBUG) Log.i("Hello2", "getStreamInRate");
        if (isCs108Connected()) return cs108Library4A.getStreamInRate();
        else if (isCs710Connected()) return cs710Library4A.getStreamInRate();
        else Log.i("Hello2", "getStreamInRate" + stringNOTCONNECT);
        return -1;
    }
    public int get98XX() {
        if (DEBUG) Log.i("Hello2", "get98XX");
        if (isCs108Connected()) return cs108Library4A.get98XX();
        else if (isCs710Connected()) return cs710Library4A.get98XX();
        else Log.i("Hello2", "get98XX" + stringNOTCONNECT);
        return -1;
    }


    //============ Rfid ============
    public String getAuthMatchData() {
        if (DEBUG) Log.i("Hello2", "getAuthMatchData");
        if (isCs108Connected()) return cs108Library4A.getAuthMatchData();
        else if (isCs710Connected()) return cs710Library4A.getAuthMatchData();
        else Log.i("Hello2", "getAuthMatchData" + stringNOTCONNECT);
        return null;
    }
    public boolean setAuthMatchData(String mask) {
        if (DEBUG) Log.i("Hello2", "setAuthMatchData");
        if (isCs108Connected()) return cs108Library4A.setAuthMatchData(mask);
        else if (isCs710Connected()) return cs710Library4A.setAuthMatchData(mask);
        else Log.i("Hello2", "setAuthMatchData" + stringNOTCONNECT);
        return false;
    }
    public int getStartQValue() {
        if (DEBUG) Log.i("Hello2", "getStartQValue");
        if (isCs108Connected()) return cs108Library4A.getStartQValue();
        else if (isCs710Connected()) return cs710Library4A.getStartQValue();
        else Log.i("Hello2", "getStartQValue" + stringNOTCONNECT);
        return -1;
    }
    public int getMaxQValue() {
        if (DEBUG) Log.i("Hello2", "getMaxQValue");
        if (isCs108Connected()) return cs108Library4A.getMaxQValue();
        else if (isCs710Connected()) return cs710Library4A.getMaxQValue();
        else Log.i("Hello2", "getMaxQValue" + stringNOTCONNECT);
        return -1;
    }
    public int getMinQValue() {
        if (DEBUG) Log.i("Hello2", "getMinQValue");
        if (isCs108Connected()) return cs108Library4A.getMinQValue();
        else if (isCs710Connected()) return cs710Library4A.getMinQValue();
        else Log.i("Hello2", "getMinQValue" + stringNOTCONNECT);
        return -1;
    }
    public boolean setDynamicQParms(int startQValue, int minQValue, int maxQValue, int retryCount) {
        if (DEBUG) Log.i("Hello2", "setDynamicQParms");
        if (isCs108Connected()) return cs108Library4A.setDynamicQParms(startQValue, minQValue, maxQValue, retryCount);
        else if (isCs710Connected()) return cs710Library4A.setDynamicQParms(startQValue, minQValue, maxQValue, retryCount);
        else Log.i("Hello2", "setDynamicQParms" + stringNOTCONNECT);
        return false;
    }
    public int getFixedQValue() {
        if (DEBUG) Log.i("Hello2", "getFixedQValue");
        if (isCs108Connected()) return cs108Library4A.getFixedQValue();
        else if (isCs710Connected()) return cs710Library4A.getFixedQValue();
        else Log.i("Hello2", "getFixedQValue" + stringNOTCONNECT);
        return -1;
    }
    public int getFixedRetryCount() {
        if (DEBUG) Log.i("Hello2", "getFixedRetryCount");
        if (isCs108Connected()) return cs108Library4A.getFixedRetryCount();
        else if (isCs710Connected()) return cs710Library4A.getFixedRetryCount();
        else Log.i("Hello2", "getFixedRetryCount" + stringNOTCONNECT);
        return -1;
    }
    public boolean getRepeatUnitNoTags() {
        if (DEBUG) Log.i("Hello2", "getRepeatUnitNoTags");
        if (isCs108Connected()) return cs108Library4A.getRepeatUnitNoTags();
        else if (isCs710Connected()) return cs710Library4A.getRepeatUnitNoTags();
        else Log.i("Hello2", "getRepeatUnitNoTags" + stringNOTCONNECT);
        return false;
    }
    public boolean setFixedQParms(int qValue, int retryCount, boolean repeatUnitNoTags) {
        if (DEBUG) Log.i("Hello2", "setFixedQParms");
        if (isCs108Connected()) return cs108Library4A.setFixedQParms(qValue, retryCount, repeatUnitNoTags);
        else if (isCs710Connected()) return cs710Library4A.setFixedQParms(qValue, retryCount, repeatUnitNoTags);
        else Log.i("Hello2", "setFixedQParms" + stringNOTCONNECT);
        return false;
    }
    public boolean getChannelHoppingDefault() {
        if (DEBUG) Log.i("Hello2", "getChannelHoppingDefault");
        if (isCs108Connected()) return cs108Library4A.getChannelHoppingDefault();
        else if (isCs710Connected()) return cs710Library4A.getChannelHoppingDefault();
        else Log.i("Hello2", "getChannelHoppingDefault" + stringNOTCONNECT);
        return false;
    }
    public boolean getRfidOnStatus() {
        if (DEBUG) Log.i("Hello2", "getRfidOnStatus");
        if (isCs108Connected()) return cs108Library4A.getRfidOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getRfidOnStatus();
        else Log.i("Hello2", "getRfidOnStatus" + stringNOTCONNECT);
        return false;
    }
    public boolean isRfidFailure() {
        if (DEBUG2) Log.i("Hello2", "isRfidFailure");
        if (isCs108Connected()) return cs108Library4A.isRfidFailure();
        else if (isCs710Connected()) return cs710Library4A.isRfidFailure();
        return false;
    }
    public void setReaderDefault() {
        if (DEBUG) Log.i("Hello2", "setReaderDefault");
        if (isCs108Connected()) cs108Library4A.setReaderDefault();
        else if (isCs710Connected()) cs710Library4A.setReaderDefault();
        else Log.i("Hello2", "setReaderDefault" + stringNOTCONNECT);
    }
    public String getMacVer() {
        if (DEBUG) Log.i("Hello2", "getMacVer");
        if (isCs108Connected()) return cs108Library4A.getMacVer();
        else if (isCs710Connected()) return cs710Library4A.getMacVer();
        else Log.i("Hello2", "getMacVer" + stringNOTCONNECT);
        return null;
    }
    public String getRadioSerial() {
        if (DEBUG) Log.i("Hello2", "getRadioSerial");
        if (isCs108Connected()) return cs108Library4A.getRadioSerial();
        else if (isCs710Connected()) return cs710Library4A.getRadioSerial();
        else Log.i("Hello2", "getRadioSerial" + stringNOTCONNECT);
        return null;
    }
    public String getRadioBoardVersion() {
        if (DEBUG) Log.i("Hello2", "getRadioBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getRadioBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getRadioBoardVersion();
        else Log.i("Hello2", "getRadioBoardVersion" + stringNOTCONNECT);
        return null;
    }
    public int getPortNumber() {
        if (DEBUG) Log.i("Hello2", "getPortNumber");
        if (isCs108Connected()) return cs108Library4A.getPortNumber();
        else if (isCs710Connected()) return cs710Library4A.getPortNumber();
        else Log.i("Hello2", "getPortNumber" + stringNOTCONNECT);
        return -1;
    }
    public int getAntennaSelect() {
        if (DEBUG) Log.i("Hello2", "getAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.getAntennaSelect();
        else if (isCs710Connected()) return cs710Library4A.getAntennaSelect();
        else Log.i("Hello2", "getAntennaSelect" + stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaSelect(int number) {
        if (DEBUG) Log.i("Hello2", "setAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.setAntennaSelect(number);
        else if (isCs710Connected()) return cs710Library4A.setAntennaSelect(number);
        else Log.i("Hello2", "setAntennaSelect" + stringNOTCONNECT);
        return false;
    }
    public boolean getAntennaEnable() {
        if (DEBUG) Log.i("Hello2", "getAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.getAntennaEnable();
        else if (isCs710Connected()) return cs710Library4A.getAntennaEnable();
        else Log.i("Hello2", "getAntennaEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setAntennaEnable(boolean enable) {
        if (DEBUG) Log.i("Hello2", "setAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.setAntennaEnable(enable);
        else if (isCs710Connected()) return cs710Library4A.setAntennaEnable(enable);
        else Log.i("Hello2", "setAntennaEnable" + stringNOTCONNECT);
        return false;
    }
    public long getAntennaDwell() {
        if (DEBUG) Log.i("Hello2", "getAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.getAntennaDwell();
        else if (isCs710Connected()) return cs710Library4A.getAntennaDwell();
        else Log.i("Hello2", "getAntennaDwell" + stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaDwell(long antennaDwell) {
        if (DEBUG) Log.i("Hello2", "setAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.setAntennaDwell(antennaDwell);
        else if (isCs710Connected()) return cs710Library4A.setAntennaDwell(antennaDwell);
        else Log.i("Hello2", "setAntennaDwell" + stringNOTCONNECT);
        return false;
    }
    public long getPwrlevel() {
        if (DEBUG) Log.i("Hello2", "getPwrlevel");
        if (isCs108Connected()) return cs108Library4A.getPwrlevel();
        else if (isCs710Connected()) return cs710Library4A.getPwrlevel();
        else Log.i("Hello2", "getPwrlevel" + stringNOTCONNECT);
        return -1;
    }
    public boolean setPowerLevel(long pwrlevel) {
        if (DEBUG) Log.i("Hello2", "setPowerLevel");
        if (isCs108Connected()) return cs108Library4A.setPowerLevel(pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setPowerLevel(pwrlevel);
        else Log.i("Hello2", "setPowerLevel" + stringNOTCONNECT);
        return false;
    }
    public int getQueryTarget() {
        if (DEBUG) Log.i("Hello2", "getQueryTarget");
        if (isCs108Connected()) return cs108Library4A.getQueryTarget();
        else if (isCs710Connected()) return cs710Library4A.getQueryTarget();
        else Log.i("Hello2", "getQueryTarget" + stringNOTCONNECT);
        return -1;
    }
    public int getQuerySession() {
        if (DEBUG) Log.i("Hello2", "getQuerySession");
        if (isCs108Connected()) return cs108Library4A.getQuerySession();
        else if (isCs710Connected()) return cs710Library4A.getQuerySession();
        else Log.i("Hello2", "getQuerySession" + stringNOTCONNECT);
        return -1;
    }
    public int getQuerySelect() {
        if (DEBUG) Log.i("Hello2", "getQuerySelect");
        if (isCs108Connected()) return cs108Library4A.getQuerySelect();
        else if (isCs710Connected()) return cs710Library4A.getQuerySelect();
        else Log.i("Hello2", "getQuerySelect" + stringNOTCONNECT);
        return -1;
    }
    public boolean setTagGroup(int sL, int session, int target1) {
        if (DEBUG) Log.i("Hello2", "setTagGroup");
        if (isCs108Connected()) return cs108Library4A.setTagGroup(sL, session, target1);
        else if (isCs710Connected()) return cs710Library4A.setTagGroup(sL, session, target1);
        else Log.i("Hello2", "setTagGroup" + stringNOTCONNECT);
        return false;
    }
    public int getTagFocus() {
        if (DEBUG) Log.i("Hello2", "getTagFocus");
        if (isCs108Connected()) return cs108Library4A.getTagFocus();
        else if (isCs710Connected()) return cs710Library4A.getTagFocus();
        else Log.i("Hello2", "getTagFocus" + stringNOTCONNECT);
        return -1;
    }
    public boolean setTagFocus(boolean tagFocusNew) {
        if (DEBUG) Log.i("Hello2", "setTagFocus");
        if (isCs108Connected()) return cs108Library4A.setTagFocus(tagFocusNew);
        else if (isCs710Connected()) return cs710Library4A.setTagFocus(tagFocusNew);
        else Log.i("Hello2", "setTagFocus" + stringNOTCONNECT);
        return false;
    }
    public int getFastId() {
        if (DEBUG) Log.i("Hello2", "getFastId");
        if (isCs108Connected()) return cs108Library4A.getFastId();
        else if (isCs710Connected()) return cs710Library4A.getFastId();
        else Log.i("Hello2", "getFastId" + stringNOTCONNECT);
        return -1;
    }
    public boolean setFastId(boolean fastIdNew) {
        if (DEBUG) Log.i("Hello2", "setFastId");
        if (isCs108Connected()) return cs108Library4A.setFastId(fastIdNew);
        else if (isCs710Connected()) return cs710Library4A.setFastId(fastIdNew);
        else Log.i("Hello2", "setFastId" + stringNOTCONNECT);
        return false;
    }
    public boolean getInvAlgo() {
        if (DEBUG) Log.i("Hello2", "getInvAlgo");
        if (isCs108Connected()) return cs108Library4A.getInvAlgo();
        else if (isCs710Connected()) return cs710Library4A.getInvAlgo();
        else Log.i("Hello2", "getInvAlgo" + stringNOTCONNECT);
        return false;
    }
    public boolean setInvAlgo(boolean dynamicAlgo) {
        if (DEBUG) Log.i("Hello2", "setInvAlgo");
        if (isCs108Connected()) return cs108Library4A.setInvAlgo(dynamicAlgo);
        else if (isCs710Connected()) return cs710Library4A.setInvAlgo(dynamicAlgo);
        else Log.i("Hello2", "setInvAlgo" + stringNOTCONNECT);
        return false;
    }
    public List<String> getProfileList() {
        if (DEBUG) Log.i("Hello2", "getProfileList");
        if (isCs108Connected()) return cs108Library4A.getProfileList();
        else if (isCs710Connected()) return cs710Library4A.getProfileList();
        else Log.i("Hello2", "getProfileList" + stringNOTCONNECT);
        return null;
    }
    public int getCurrentProfile() {
        if (DEBUG) Log.i("Hello2", "getCurrentProfile");
        if (isCs108Connected()) return cs108Library4A.getCurrentProfile();
        else if (isCs710Connected()) return cs710Library4A.getCurrentProfile();
        else Log.i("Hello2", "getCurrentProfile" + stringNOTCONNECT);
        return -1;
    }
    public boolean setBasicCurrentLinkProfile() {
        if (DEBUG) Log.i("Hello2", "setBasicCurrentLinkProfile");
        if (isCs108Connected()) return true;
        else if (isCs710Connected()) return cs710Library4A.setBasicCurrentLinkProfile();
        else Log.i("Hello2", "setBasicCurrentLinkProfile" + stringNOTCONNECT);
        return false;
    }
    public boolean setCurrentLinkProfile(int profile) {
        if (DEBUG) Log.i("Hello2", "setCurrentLinkProfile to " + profile);
        if (isCs108Connected()) return cs108Library4A.setCurrentLinkProfile(profile);
        else if (isCs710Connected()) return cs710Library4A.setCurrentLinkProfile(profile);
        else Log.i("Hello2", "setCurrentLinkProfile" + stringNOTCONNECT);
        return false;
    }
    public void resetEnvironmentalRSSI() {
        if (DEBUG) Log.i("Hello2", "resetEnvironmentalRSSI");
        if (isCs108Connected()) cs108Library4A.resetEnvironmentalRSSI();
        else if (isCs710Connected()) cs710Library4A.resetEnvironmentalRSSI();
        else Log.i("Hello2", "resetEnvironmentalRSSI" + stringNOTCONNECT);
    }
    public String getEnvironmentalRSSI() {
        if (DEBUG) Log.i("Hello2", "getEnvironmentalRSSI");
        if (isCs108Connected()) return cs108Library4A.getEnvironmentalRSSI();
        else if (isCs710Connected()) return cs710Library4A.getEnvironmentalRSSI();
        else Log.i("Hello2", "getEnvironmentalRSSI" + stringNOTCONNECT);
        return null;
    }
    public int getHighCompression() {
        if (DEBUG) Log.i("Hello2", "getHighCompression");
        if (isCs108Connected()) return cs108Library4A.getHighCompression();
        else if (isCs710Connected()) return cs710Library4A.getHighCompression();
        else Log.i("Hello2", "getHighCompression" + stringNOTCONNECT);
        return -1;
    }
    public int getRflnaGain() {
        if (DEBUG) Log.i("Hello2", "getRflnaGain");
        if (isCs108Connected()) return cs108Library4A.getRflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getRflnaGain();
        else Log.i("Hello2", "getRflnaGain" + stringNOTCONNECT);
        return -1;
    }
    public int getIflnaGain() {
        if (DEBUG) Log.i("Hello2", "getIflnaGain");
        if (isCs108Connected()) return cs108Library4A.getIflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getIflnaGain();
        else Log.i("Hello2", "getIflnaGain" + stringNOTCONNECT);
        return -1;
    }
    public int getAgcGain() {
        if (DEBUG) Log.i("Hello2", "getAgcGain");
        if (isCs108Connected()) return cs108Library4A.getAgcGain();
        else if (isCs710Connected()) return cs710Library4A.getAgcGain();
        else Log.i("Hello2", "getAgcGain" + stringNOTCONNECT);
        return -1;
    }
    public int getRxGain() {
        if (DEBUG) Log.i("Hello2", "getRxGain");
        if (isCs108Connected()) return cs108Library4A.getRxGain();
        else if (isCs710Connected()) return cs710Library4A.getRxGain();
        else Log.i("Hello2", "getRxGain" + stringNOTCONNECT);
        return -1;
    }
    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        if (DEBUG) Log.i("Hello2", "setRxGain");
        if (isCs108Connected()) return cs108Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else if (isCs710Connected()) return cs710Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else Log.i("Hello2", "setRxGain" + stringNOTCONNECT);
        return false;
    }
    public boolean setRxGain(int rxGain) {
        if (DEBUG) Log.i("Hello2", "setRxGain");
        if (isCs108Connected()) return cs108Library4A.setRxGain(rxGain);
        else if (isCs710Connected()) return cs710Library4A.setRxGain(rxGain);
        else Log.i("Hello2", "setRxGain" + stringNOTCONNECT);
        return false;
    }
    public int FreqChnCnt() {
        if (DEBUG) Log.i("Hello2", "FreqChnCnt");
        if (isCs108Connected()) return cs108Library4A.FreqChnCnt();
        else if (isCs710Connected()) return cs710Library4A.FreqChnCnt();
        else Log.i("Hello2", "FreqChnCnt" + stringNOTCONNECT);
        return -1;
    }
    public double getLogicalChannel2PhysicalFreq(int channel) {
        if (DEBUG) Log.i("Hello2", "getLogicalChannel2PhysicalFreq");
        if (isCs108Connected()) return cs108Library4A.getLogicalChannel2PhysicalFreq(channel);
        else if (isCs710Connected()) return cs710Library4A.getLogicalChannel2PhysicalFreq(channel);
        else Log.i("Hello2", "getLogicalChannel2PhysicalFreq" + stringNOTCONNECT);
        return -1;
    }
    public byte getTagDelay() {
        if (DEBUG) Log.i("Hello2", "getTagDelay");
        if (isCs108Connected()) return cs108Library4A.getTagDelay();
        else if (isCs710Connected()) return cs710Library4A.getTagDelay();
        else Log.i("Hello2", "getTagDelay" + stringNOTCONNECT);
        return -1;
    }
    public boolean setTagDelay(byte tagDelay) {
        if (DEBUG) Log.i("Hello2", "setTagDelay");
        if (isCs108Connected()) return cs108Library4A.setTagDelay(tagDelay);
        else if (isCs710Connected()) return cs710Library4A.setTagDelay(tagDelay);
        else Log.i("Hello2", "setTagDelay" + stringNOTCONNECT);
        return false;
    }
    public byte getIntraPkDelay() {
        if (DEBUG) Log.i("Hello2", "getIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.getIntraPkDelay();
        else if (isCs710Connected()) return cs710Library4A.getIntraPkDelay();
        else Log.i("Hello2", "getIntraPkDelay" + stringNOTCONNECT);
        return -1;
    }
    public boolean setIntraPkDelay(byte intraPkDelay) {
        if (DEBUG) Log.i("Hello2", "setIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.setIntraPkDelay(intraPkDelay);
        else if (isCs710Connected()) return cs710Library4A.setIntraPkDelay(intraPkDelay);
        else Log.i("Hello2", "setIntraPkDelay" + stringNOTCONNECT);
        return false;
    }
    public byte getDupDelay() {
        if (DEBUG) Log.i("Hello2", "getDupDelay");
        if (isCs108Connected()) return cs108Library4A.getDupDelay();
        else if (isCs710Connected()) return cs710Library4A.getDupDelay();
        else Log.i("Hello2", "getDupDelay" + stringNOTCONNECT);
        return -1;
    }
    public boolean setDupDelay(byte dupElim) {
        if (DEBUG) Log.i("Hello2", "setDupDelay");
        if (isCs108Connected()) return cs108Library4A.setDupDelay(dupElim);
        else if (isCs710Connected()) return cs710Library4A.setDupDelay(dupElim);
        else Log.i("Hello2", "setDupDelay" + stringNOTCONNECT);
        return false;
    }
    public long getCycleDelay() {
        if (DEBUG) Log.i("Hello2", "getCycleDelay");
        if (isCs108Connected()) return cs108Library4A.getCycleDelay();
        else if (isCs710Connected()) return cs710Library4A.getCycleDelay();
        else Log.i("Hello2", "getCycleDelay" + stringNOTCONNECT);
        return -1;
    }
    public boolean setCycleDelay(long cycleDelay) {
        if (DEBUG) Log.i("Hello2", "setCycleDelay");
        if (isCs108Connected()) return cs108Library4A.setCycleDelay(cycleDelay);
        else if (isCs710Connected()) return cs710Library4A.setCycleDelay(cycleDelay);
        return false;
    }
    public void getAuthenticateReplyLength() {
        if (DEBUG) Log.i("Hello2", "getAuthenticateReplyLength");
        if (isCs108Connected()) cs108Library4A.getAuthenticateReplyLength();
        else if (isCs710Connected()) cs710Library4A.getAuthenticateReplyLength();
        else Log.i("Hello2", "getAuthenticateReplyLength" + stringNOTCONNECT);
    }
    public boolean setTamConfiguration(boolean header, String matchData) {
        if (DEBUG | true) Log.i("Hello2", "setTamConfiguration with header = " + header + ", matchData = " + matchData);
        if (isCs108Connected()) return cs108Library4A.setTamConfiguration(header, matchData);
        else if (isCs710Connected()) return cs710Library4A.setTamConfiguration(header, matchData);
        else Log.i("Hello2", "setTam1Configuration");
        return false;
    }
    public boolean setTam1Configuration(int keyId, String matchData) {
        if (DEBUG | true) Log.i("Hello2", "setTam1Configuration with KeyId = " + keyId + ", matchData = " + matchData);
        if (isCs108Connected()) return cs108Library4A.setTam1Configuration(keyId, matchData);
        else if (isCs710Connected()) return cs710Library4A.setTam1Configuration(keyId, matchData);
        else Log.i("Hello2", "setTam1Configuration");
        return false;
    }
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        if (DEBUG) Log.i("Hello2", "setTam2Configuration");
        if (isCs108Connected()) return cs108Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else if (isCs710Connected()) return cs710Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else Log.i("Hello2", "setTam2Configuration");
        return false;
    }
    public int getUntraceableEpcLength() {
        if (DEBUG) Log.i("Hello2", "getUntraceableEpcLength");
        if (isCs108Connected()) return cs108Library4A.getUntraceableEpcLength();
        else if (isCs710Connected()) return cs710Library4A.getUntraceableEpcLength();
        else Log.i("Hello2", "getUntraceableEpcLength" + stringNOTCONNECT);
        return -1;
    }
    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        Log.i("Hello2", "setUntraceable 1");
        return false;
    }
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        if (DEBUG) Log.i("Hello2", "setUntraceable");
        if (isCs108Connected()) return cs108Library4A.setUntraceable(range, user, tid, epcLength, epc, uxpc);
        else if (isCs710Connected()) return false;
        else Log.i("Hello2", "setUntraceable" + stringNOTCONNECT);
        return false;
    }
    public boolean setAuthenticateConfiguration() {
        if (DEBUG) Log.i("Hello2", "setAuthenticateConfiguration");
        if (isCs108Connected()) return cs108Library4A.setAuthenticateConfiguration();
        else if (isCs710Connected()) return cs710Library4A.setAuthenticateConfiguration();
        else Log.i("Hello2", "setAuthenticateConfiguration" + stringNOTCONNECT);
        return false;
    }
    public int getRetryCount() {
        if (DEBUG) Log.i("Hello2", "getRetryCount");
        if (isCs108Connected()) return cs108Library4A.getRetryCount();
        else if (isCs710Connected()) return cs710Library4A.getRetryCount();
        else Log.i("Hello2", "getRetryCount" + stringNOTCONNECT);
        return -1;
    }
    public boolean setRetryCount(int retryCount) {
        if (DEBUG) Log.i("Hello2", "setRetryCount");
        if (isCs108Connected()) return cs108Library4A.setRetryCount(retryCount);
        else if (isCs710Connected()) return cs710Library4A.setRetryCount(retryCount);
        else Log.i("Hello2", "setRetryCount" + stringNOTCONNECT);
        return false;
    }
    public int getInvSelectIndex() {
        if (DEBUG) Log.i("Hello2", "getInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.getInvSelectIndex();
        else if (isCs710Connected()) return cs710Library4A.getInvSelectIndex();
        else Log.i("Hello2", "getInvSelectIndex" + stringNOTCONNECT);
        return -1;
    } //2286
    public boolean getSelectEnable() {
        if (DEBUG) Log.i("Hello2", "getSelectEnable");
        if (isCs108Connected()) return cs108Library4A.getSelectEnable();
        else if (isCs710Connected()) return cs710Library4A.getSelectEnable();
        else Log.i("Hello2", "getSelectEnable" + stringNOTCONNECT);
        return false;
    }
    public int getSelectTarget() {
        if (DEBUG) Log.i("Hello2", "getSelectTarget");
        if (isCs108Connected()) return cs108Library4A.getSelectTarget();
        else if (isCs710Connected()) return cs710Library4A.getSelectTarget();
        else Log.i("Hello2", "getSelectTarget" + stringNOTCONNECT);
        return -1;
    }
    public int getSelectAction() {
        if (DEBUG) Log.i("Hello2", "getSelectAction");
        if (isCs108Connected()) return cs108Library4A.getSelectAction();
        else if (isCs710Connected()) return cs710Library4A.getSelectAction();
        else Log.i("Hello2", "getSelectAction" + stringNOTCONNECT);
        return -1;
    }
    public int getSelectMaskBank() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskBank");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskBank();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskBank();
        else Log.i("Hello2", "getSelectMaskBank" + stringNOTCONNECT);
        return -1;
    }
    public int getSelectMaskOffset() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskOffset");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskOffset();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskOffset();
        else Log.i("Hello2", "getSelectMaskOffset" + stringNOTCONNECT);
        return -1;
    }
    public String getSelectMaskData() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskData");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskData();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskData();
        else Log.i("Hello2", "getSelectMaskData" + stringNOTCONNECT);
        return null;
    }
    public boolean setInvSelectIndex(int invSelect) {
        if (DEBUG) Log.i("Hello2", "setInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.setInvSelectIndex(invSelect);
        else if (isCs710Connected()) return cs710Library4A.setInvSelectIndex(invSelect);
        else Log.i("Hello2", "setInvSelectIndex" + stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteriaDisable(int index) {
        if (DEBUG || true) appendToLog("csLibrary4A: setSelectCriteria Disable with index = " + index);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteriaDisable(index);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteriaDisable(index);
        else Log.i("Hello2", "setSelectCriteriaDisable" + stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        appendToLog("csLibrary4A: setSelectCriteria 1 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask + ", maskbit = " + maskbit);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else Log.i("Hello2", "setSelectCriteria 1" + stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        appendToLog("csLibrary4A: setSelectCriteria 2 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else Log.i("Hello2", "setSelectCriteria" + stringNOTCONNECT);
        return false;
    }
    public boolean getRssiFilterEnable() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterEnable");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterEnable();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterEnable();
        else Log.i("Hello2", "getRssiFilterEnable" + stringNOTCONNECT);
        return false;
    }
    public int getRssiFilterType() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterType");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterType();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterType();
        else Log.i("Hello2", "getRssiFilterType" + stringNOTCONNECT);
        return -1;
    }
    public int getRssiFilterOption() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterOption");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterOption();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterOption();
        return -1;
    }
    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        if (DEBUG) Log.i("Hello2", "setRssiFilterConfig");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else Log.i("Hello2", "setRssiFilterConfig" + stringNOTCONNECT);
        return false;
    }
    public double getRssiFilterThreshold1() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterThreshold1");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold1();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold1();
        else Log.i("Hello2", "getRssiFilterThreshold1" + stringNOTCONNECT);
        return -1;
    }
    public double getRssiFilterThreshold2() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterThreshold2");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold2();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold2();
        else Log.i("Hello2", "getRssiFilterThreshold2" + stringNOTCONNECT);
        return -1;
    }
    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        if (DEBUG) Log.i("Hello2", "setRssiFilterThreshold");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else Log.i("Hello2", "setRssiFilterThreshold" + stringNOTCONNECT);
        return false;
    }
    public long getRssiFilterCount() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterCount");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterCount();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterCount();
        else Log.i("Hello2", "getRssiFilterCount" + stringNOTCONNECT);
        return -1;
    }
    public boolean setRssiFilterCount(long rssiFilterCount) {
        Log.i("Hello2", "setRssiFilterCount");
        return false;
    }
    public boolean getInvMatchEnable() {
        if (DEBUG) Log.i("Hello2", "getInvMatchEnable");
        if (isCs108Connected()) return cs108Library4A.getInvMatchEnable();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchEnable();
        else Log.i("Hello2", "getInvMatchEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean getInvMatchType() {
        if (DEBUG) Log.i("Hello2", "getInvMatchType");
        if (isCs108Connected()) return cs108Library4A.getInvMatchType();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchType();
        else Log.i("Hello2", "getInvMatchType" + stringNOTCONNECT);
        return false;
    }
    public int getInvMatchOffset() {
        if (DEBUG) Log.i("Hello2", "getInvMatchOffset");
        if (isCs108Connected()) return cs108Library4A.getInvMatchOffset();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchOffset();
        else Log.i("Hello2", "getInvMatchOffset" + stringNOTCONNECT);
        return -1;
    }
    public String getInvMatchData() {
        if (DEBUG) Log.i("Hello2", "getInvMatchData");
        if (isCs108Connected()) return cs108Library4A.getInvMatchData();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchData();
        else Log.i("Hello2", "getInvMatchData" + stringNOTCONNECT);
        return null;
    }
    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        if (DEBUG) Log.i("Hello2", "setPostMatchCriteria");
        if (isCs108Connected()) return cs108Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else Log.i("Hello2", "setPostMatchCriteria" + stringNOTCONNECT);
        return false;
    }
    public int mrfidToWriteSize() {
        if (DEBUG2) Log.i("Hello2", "mrfidToWriteSize");
        if (isCs108Connected()) return cs108Library4A.mrfidToWriteSize();
        else if (isCs710Connected()) return cs710Library4A.mrfidToWriteSize();
        else Log.i("Hello2", "mrfidToWriteSize" + stringNOTCONNECT);
        return -1;
    }
    public void mrfidToWritePrint() {
        Log.i("Hello2", "mrfidToWritePrint");
    }
    public long getTagRate() {
        if (DEBUG) Log.i("Hello2", "getTagRate");
        if (isCs108Connected()) return cs108Library4A.getTagRate();
        else if (isCs710Connected()) return cs710Library4A.getTagRate();
        else Log.i("Hello2", "getTagRate" + stringNOTCONNECT);
        return -1;
    }
    public boolean startOperation(RfidReaderChipData.OperationTypes operationTypes) {
        if (DEBUG) Log.i("Hello2", "startOperation");
        if (isCs108Connected()) {
            /*RfidReaderChipData.OperationTypes operationTypes1 = null;
            switch (operationTypes) {
                case TAG_RDOEM:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_RDOEM;
                    break;
                case TAG_INVENTORY_COMPACT:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT;
                    break;
                case TAG_INVENTORY:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_INVENTORY;
                    break;
                case TAG_SEARCHING:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_SEARCHING;
                    break;
            }*/
            return cs108Library4A.startOperation(operationTypes);
        } else if (isCs710Connected()) {
            /*RfidReaderChipData.OperationTypes operationTypes1 = null;
            switch (operationTypes) {
                case TAG_RDOEM:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_RDOEM;
                    break;
                case TAG_INVENTORY_COMPACT:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT;
                    break;
                case TAG_INVENTORY:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_INVENTORY;
                    break;
                case TAG_SEARCHING:
                    operationTypes1 = RfidReaderChipData.OperationTypes.TAG_SEARCHING;
                    break;
            }*/
            return cs710Library4A.startOperation(operationTypes);
        }
        else Log.i("Hello2", "startOperation" + stringNOTCONNECT);
        return false;
    }
    public boolean abortOperation() {
        if (DEBUG) Log.i("Hello2", "abortOperation");
        if (isCs108Connected()) return cs108Library4A.abortOperation();
        else if (isCs710Connected()) return cs710Library4A.abortOperation();
        else Log.i("Hello2", "abortOperation" + stringNOTCONNECT);
        return false;
    }
    public void restoreAfterTagSelect() {
        if (DEBUG | true) Log.i("Hello2", "restoreAfterTagSelect");
        if (isCs108Connected()) cs108Library4A.restoreAfterTagSelect();
        else if (isCs710Connected()) cs710Library4A.restoreAfterTagSelect();
        else Log.i("Hello2", "restoreAfterTagSelect" + stringNOTCONNECT);
    }
    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        appendToLog("csLibrary4A: setSelectCriteria setSelectedByTID strTagId = " + strTagId + ", pwrlevel = " + pwrlevel);
        if (isCs108Connected()) return cs108Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else Log.i("Hello2", "setSelectedTagByTID" + stringNOTCONNECT);
        return false;
    }
    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        if (DEBUG) Log.i("Hello2", "setSelectedTag 1");
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else Log.i("Hello2", "setSelectedTag 1" + stringNOTCONNECT);
        return false;
    }
    public boolean setSelectedTag(boolean selectOne, String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        appendToLog("csLibraryA: setSelectCriteria strTagId = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(selectOne, selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else Log.i("Hello2", "setSelectedTag 2" + stringNOTCONNECT);
        return false;
    }
    public boolean setMatchRep(int matchRep) {
        if (DEBUG) Log.i("Hello2", "setMatchRep");
        if (isCs108Connected()) return cs108Library4A.setMatchRep(matchRep);
        else if (isCs710Connected()) return cs710Library4A.setMatchRep(matchRep);
        else Log.i("Hello2", "setMatchRep" + stringNOTCONNECT);
        return false;
    }
    public String[] getCountryList() {
        if (DEBUG) Log.i("Hello2", "getCountryList");
        if (isCs108Connected()) return cs108Library4A.getCountryList();
        else if (isCs710Connected()) return cs710Library4A.getCountryList();
        else Log.i("Hello2", "getCountryList" + stringNOTCONNECT);
        return null;
    }
    public int getCountryNumberInList() {
        if (DEBUG) Log.i("Hello2", "getCountryNumberInList");
        if (isCs108Connected()) return cs108Library4A.getCountryNumberInList();
        else if (isCs710Connected()) return cs710Library4A.getCountryNumberInList();
        else Log.i("Hello2", "getCountryNumberInList" + stringNOTCONNECT);
        return -1;
    }
    public boolean setCountryInList(int countryInList) {
        if (DEBUG || true) Log.i("Hello2", "setCountryInList");
        if (isCs108Connected()) return cs108Library4A.setCountryInList(countryInList);
        else if (isCs710Connected()) return cs710Library4A.setCountryInList(countryInList);
        else Log.i("Hello2", "setCountryInList" + stringNOTCONNECT);
        return false;
    }
    public boolean getChannelHoppingStatus() {
        if (DEBUG) Log.i("Hello2", "getChannelHoppingStatus");
        if (isCs108Connected()) return cs108Library4A.getChannelHoppingStatus();
        else if (isCs710Connected()) return cs710Library4A.getChannelHoppingStatus();
        else Log.i("Hello2", "getChannelHoppingStatus" + stringNOTCONNECT);
        return false;
    }
    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        Log.i("Hello2", "setChannelHoppingStatus");
        return false;
    }
    public String[] getChannelFrequencyList() {
        if (isCs108Connected()) return cs108Library4A.getChannelFrequencyList();
        else if (isCs710Connected()) return cs710Library4A.getChannelFrequencyList();
        else Log.i("Hello2", "getChannelFrequencyList" + stringNOTCONNECT);
        return null;
    }
    public int getChannel() {
        if (DEBUG) Log.i("Hello2", "getChannel");
        if (isCs108Connected()) return cs108Library4A.getChannel();
        else if (isCs710Connected()) return cs710Library4A.getChannel();
        else Log.i("Hello2", "getChannel" + stringNOTCONNECT);
        return -1;
    }
    public boolean setChannel(int channelSelect) {
        if (DEBUG) Log.i("Hello2", "setChannel");
        if (isCs108Connected()) return cs108Library4A.setChannel(channelSelect);
        else if (isCs710Connected()) return cs710Library4A.setChannel(channelSelect);
        else Log.i("Hello2", "setChannel" + stringNOTCONNECT);
        return false;
    }
    public byte getPopulation2Q(int population) {
        if (DEBUG) Log.i("Hello2", "getPopulation2Q");
        if (isCs108Connected()) return cs108Library4A.getPopulation2Q(population);
        else if (isCs710Connected()) return cs710Library4A.getPopulation2Q(population);
        else Log.i("Hello2", "getPopulation2Q" + stringNOTCONNECT);
        return -1;
    }
    public int getPopulation() {
        if (DEBUG) Log.i("Hello2", "getPopulation");
        if (isCs108Connected()) return cs108Library4A.getPopulation();
        else if (isCs710Connected()) return cs710Library4A.getPopulation();
        else Log.i("Hello2", "getPopulation" + stringNOTCONNECT);
        return -1;
    } //3348
    public boolean setPopulation(int population) {
        if (DEBUG || true) Log.i("Hello2", "setPopulation " + population);
        if (isCs108Connected()) return cs108Library4A.setPopulation(population);
        else if (isCs710Connected()) return cs710Library4A.setPopulation(population);
        else Log.i("Hello2", "setPopulation" + stringNOTCONNECT);
        return false;
    }
    public byte getQValue() {
        if (DEBUG) Log.i("Hello2", "getQValue");
        if (isCs108Connected()) return cs108Library4A.getQValue();
        else if (isCs710Connected()) return cs710Library4A.getQValue();
        else Log.i("Hello2", "getQValue" + stringNOTCONNECT);
        return -1;
    }
    public boolean setQValue(byte byteValue) {
        if (DEBUG) Log.i("Hello2", "setQValue");
        if (isCs108Connected()) return cs108Library4A.setQValue(byteValue);
        else if (isCs710Connected()) return cs710Library4A.setQValue(byteValue);
        else Log.i("Hello2", "setQValue" + stringNOTCONNECT);
        return false;
    }
    public RfidReaderChipData.Rx000pkgData onRFIDEvent() {
        if (DEBUG2) Log.i("Hello2", "onRFIDEvent");
        if (isCs108Connected()) {
            RfidReaderChipData.Rx000pkgData rx000pkgData = null;
            RfidReaderChipData.Rx000pkgData rx000pkgData1 = cs108Library4A.onRFIDEvent();
            if (rx000pkgData1 != null) {
                rx000pkgData = new RfidReaderChipData.Rx000pkgData();
                switch (rx000pkgData1.responseType) {
                    case TYPE_18K6C_INVENTORY_COMPACT:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                        break;
                    case TYPE_18K6C_INVENTORY:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                        break;
                    case TYPE_COMMAND_ABORT_RETURN:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN;
                        break;
                    case TYPE_COMMAND_END:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END;
                        break;
                    case TYPE_18K6C_TAG_ACCESS:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                        break;
                    default:
                        Log.i("Hello2", "onRFIDEvent: responseType = " + rx000pkgData1.responseType.toString());
                }
                rx000pkgData.flags = rx000pkgData1.flags;
                rx000pkgData.dataValues = rx000pkgData1.dataValues;
                rx000pkgData.decodedTime = rx000pkgData1.decodedTime;
                rx000pkgData.decodedRssi = rx000pkgData1.decodedRssi;
                rx000pkgData.decodedPhase = rx000pkgData1.decodedPhase;
                rx000pkgData.decodedChidx = rx000pkgData1.decodedChidx;
                rx000pkgData.decodedPort = rx000pkgData1.decodedPort;
                rx000pkgData.decodedPc = rx000pkgData1.decodedPc;
                rx000pkgData.decodedEpc = rx000pkgData1.decodedEpc;
                rx000pkgData.decodedCrc = rx000pkgData1.decodedCrc;
                rx000pkgData.decodedData1 = rx000pkgData1.decodedData1;
                rx000pkgData.decodedData2 = rx000pkgData1.decodedData2;
                rx000pkgData.decodedResult = rx000pkgData1.decodedResult;
                rx000pkgData.decodedError = rx000pkgData1.decodedError;
            }
            return rx000pkgData;
        } else if (isCs710Connected()) {
            RfidReaderChipData.Rx000pkgData rx000pkgData = null;
            RfidReaderChipData.Rx000pkgData rx000pkgData1 = cs710Library4A.onRFIDEvent();
            if (rx000pkgData1 != null) {
                rx000pkgData = new RfidReaderChipData.Rx000pkgData();
                switch (rx000pkgData1.responseType) {
                    case TYPE_18K6C_INVENTORY_COMPACT:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                        break;
                    case TYPE_18K6C_INVENTORY:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                        break;
                    case TYPE_COMMAND_ABORT_RETURN:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN;
                        break;
                    case TYPE_COMMAND_END:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END;
                        break;
                    case TYPE_18K6C_TAG_ACCESS:
                        rx000pkgData.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                        break;
                    default:
                        Log.i("Hello2", "onRFIDEvent: responseType = " + rx000pkgData1.responseType.toString());
                }
                rx000pkgData.flags = rx000pkgData1.flags;
                rx000pkgData.dataValues = rx000pkgData1.dataValues;
                rx000pkgData.decodedTime = rx000pkgData1.decodedTime;
                rx000pkgData.decodedRssi = rx000pkgData1.decodedRssi;
                rx000pkgData.decodedPhase = rx000pkgData1.decodedPhase;
                rx000pkgData.decodedChidx = rx000pkgData1.decodedChidx;
                rx000pkgData.decodedPort = rx000pkgData1.decodedPort;
                rx000pkgData.decodedPc = rx000pkgData1.decodedPc;
                rx000pkgData.decodedEpc = rx000pkgData1.decodedEpc;
                rx000pkgData.decodedCrc = rx000pkgData1.decodedCrc;
                rx000pkgData.decodedData1 = rx000pkgData1.decodedData1;
                rx000pkgData.decodedData2 = rx000pkgData1.decodedData2;
                rx000pkgData.decodedResult = rx000pkgData1.decodedResult;
                rx000pkgData.decodedError = rx000pkgData1.decodedError;
            }
            if (rx000pkgData != null) appendToLog("response0 = " + rx000pkgData.responseType.toString() + ", " + byteArrayToString(rx000pkgData.dataValues));
            return rx000pkgData;
        }
        else Log.i("Hello2", "onRFIDEvent" + stringNOTCONNECT);
        return null;
    }
    public String getModelNumber() {
        if (DEBUG) Log.i("Hello2", "getModelNumber");
        if (isCs108Connected()) return cs108Library4A.getModelNumber();
        else if (isCs710Connected()) return cs710Library4A.getModelNumber();
        else Log.i("Hello2", "getModelNumber" + stringNOTCONNECT);
        return null;
    }
    public boolean setRx000KillPassword(String password) {
        if (DEBUG) Log.i("Hello2", "setRx000KillPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000KillPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000KillPassword(password);
        else Log.i("Hello2", "setRx000KillPassword" + stringNOTCONNECT);
        return false;
    }
    public boolean setRx000AccessPassword(String password) {
        if (DEBUG) Log.i("Hello2", "setRx000AccessPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000AccessPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000AccessPassword(password);
        else Log.i("Hello2", "setRx000AccessPassword" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        if (DEBUG) Log.i("Hello2", "setAccessRetry");
        if (isCs108Connected()) return cs108Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else if (isCs710Connected()) return cs710Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else Log.i("Hello2", "setAccessRetry" + stringNOTCONNECT);
        return false;
    }
    public boolean setInvModeCompact(boolean invModeCompact) {
        if (DEBUG) Log.i("Hello2", "setInvModeCompact");
        if (isCs108Connected()) return cs108Library4A.setInvModeCompact(invModeCompact);
        else if (isCs710Connected()) return cs710Library4A.setInvModeCompact(invModeCompact);
        else Log.i("Hello2", "setInvModeCompact" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        if (DEBUG) Log.i("Hello2", "setAccessLockAction");
        if (isCs108Connected()) return cs108Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else if (isCs710Connected()) return cs710Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else Log.i("Hello2", "setAccessLockAction" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessBank(int accessBank) {
        if (DEBUG) Log.i("Hello2", "setAccessBank 1");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank);
        else Log.i("Hello2", "setAccessBank 1" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessBank(int accessBank, int accessBank2) {
        if (DEBUG) Log.i("Hello2", "setAccessBank 2");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank, accessBank2);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank, accessBank2);
        else Log.i("Hello2", "setAccessBank 2" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessOffset(int accessOffset) {
        if (DEBUG) Log.i("Hello2", "setAccessOffset 1");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset);
        else Log.i("Hello2", "setAccessOffset 1" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        if (DEBUG) Log.i("Hello2", "setAccessOffset 2");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset, accessOffset2);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset, accessOffset2);
        else Log.i("Hello2", "setAccessOffset 2" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessCount(int accessCount) {
        if (DEBUG) Log.i("Hello2", "setAccessCount 1");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount);
        else Log.i("Hello2", "setAccessCount 1" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessCount(int accessCount, int accessCount2) {
        if (DEBUG) Log.i("Hello2", "setAccessCount 2");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount, accessCount2);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount, accessCount2);
        else Log.i("Hello2", "setAccessCount 2" + stringNOTCONNECT);
        return false;
    }
    public boolean setAccessWriteData(String dataInput) {
        if (DEBUG) Log.i("Hello2", "setAccessWriteData");
        if (isCs108Connected()) return cs108Library4A.setAccessWriteData(dataInput);
        else if (isCs710Connected()) return cs710Library4A.setAccessWriteData(dataInput);
        else Log.i("Hello2", "setAccessWriteData" + stringNOTCONNECT);
        return false;
    }
    public boolean setResReadNoReply(boolean resReadNoReply) {
        if (DEBUG) Log.i("Hello2", "setResReadNoReply");
        if (isCs108Connected()) return false;
        else if (isCs710Connected()) return cs710Library4A.setResReadNoReply(resReadNoReply);
        else Log.i("Hello2", "setResReadNoReply" + stringNOTCONNECT);
        return false;
    }
    public boolean setTagRead(int tagRead) {
        if (DEBUG) Log.i("Hello2", "setTagRead");
        if (isCs108Connected()) return cs108Library4A.setTagRead(tagRead);
        else if (isCs710Connected()) return cs710Library4A.setTagRead(tagRead);
        else Log.i("Hello2", "setTagRead" + stringNOTCONNECT);
        return false;
    }
    public boolean setInvBrandId(boolean invBrandId) {
        if (DEBUG) Log.i("Hello2", "setInvBrandId");
        if (isCs108Connected()) return cs108Library4A.setInvBrandId(invBrandId);
        else if (isCs710Connected()) return cs710Library4A.setInvBrandId(invBrandId);
        else Log.i("Hello2", "setInvBrandId" + stringNOTCONNECT);
        return false;
    }
    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        if (DEBUG | true) Log.i("Hello2", "sendHostRegRequestHST_CMD with hostCommand = " + hostCommand.toString());
        if (isCs108Connected()) {
            RfidReaderChipData.HostCommands hostCommands1 = null;
            switch (hostCommand) {
                case CMD_18K6CREAD:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CREAD;
                    break;
                case CMD_18K6CWRITE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CWRITE;
                    break;
                case CMD_18K6CLOCK:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CLOCK;
                    break;
                case CMD_18K6CKILL:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CKILL;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE;
                    break;
                case CMD_UNTRACEABLE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_UNTRACEABLE;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_GETSENSORDATA;
                    break;
                case CMD_FDM_RDMEM:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_RDMEM;
                    break;
                case CMD_FDM_WRMEM:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_WRMEM;
                    break;
                case CMD_FDM_AUTH:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_AUTH;
                    break;
                case CMD_FDM_GET_TEMPERATURE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_GET_TEMPERATURE;
                    break;
                case CMD_FDM_START_LOGGING:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_START_LOGGING;
                    break;
                case CMD_FDM_STOP_LOGGING:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_STOP_LOGGING;
                    break;
                case CMD_FDM_WRREG:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_WRREG;
                    break;
                case CMD_FDM_RDREG:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_RDREG;
                    break;
                case CMD_FDM_DEEP_SLEEP:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_DEEP_SLEEP;
                    break;
                case CMD_FDM_OPMODE_CHECK:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_OPMODE_CHECK;
                    break;
                case CMD_FDM_INIT_REGFILE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_INIT_REGFILE;
                    break;
                case CMD_FDM_LED_CTRL:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_FDM_LED_CTRL;
                    break;
                default:
                    Log.i("Hello2", "Skip sendHostRegRequestHST_CMD: hostCommand = " + hostCommand.toString());
                    break;
            }
            if (hostCommands1 == null) return false;
            else return cs108Library4A.sendHostRegRequestHST_CMD(hostCommands1);
        } else if (isCs710Connected()) {
            RfidReaderChipData.HostCommands hostCommands1 = null;
            switch (hostCommand) {
                case CMD_18K6CREAD:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CREAD;
                    break;
                case CMD_18K6CWRITE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CWRITE;
                    break;
                case CMD_18K6CLOCK:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CLOCK;
                    break;
                case CMD_18K6CKILL:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CKILL;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE;
                    break;
                case CMD_UNTRACEABLE:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_UNTRACEABLE;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommands1 = RfidReaderChipData.HostCommands.CMD_GETSENSORDATA;
                default:
                    Log.i("Hello2", "sendHostRegRequestHST_CMD: hostCommand = " + hostCommand.toString());
                    break;
            }
            if (hostCommands1 == null) return false;
            return cs710Library4A.sendHostRegRequestHST_CMD(hostCommands1);
        }
        else Log.i("Hello2", "sendHostRegRequestHST_CMD" + stringNOTCONNECT);
        return false;
    }
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        if (DEBUG) Log.i("Hello2", "setPwrManagementMode");
        if (isCs108Connected()) return cs108Library4A.setPwrManagementMode(bLowPowerStandby);
        else if (isCs710Connected()) return cs710Library4A.setPwrManagementMode(bLowPowerStandby);
        return false;
    }
    public void macWrite(int address, long value) {
        if (DEBUG) Log.i("Hello2", "macWrite");
        if (isCs108Connected()) cs108Library4A.macWrite(address, value);
        else if (isCs710Connected()) { }
    }
    public void set_fdCmdCfg(int value) {
        if (DEBUG) Log.i("Hello2", "set_fdCmdCfg");
        if (isCs108Connected()) cs108Library4A.set_fdCmdCfg(value);
        else if (isCs710Connected()) { }
    }
    public void set_fdRegAddr(int addr) {
        if (DEBUG) Log.i("Hello2", "set_fdRegAddr");
        if (isCs108Connected()) cs108Library4A.set_fdRegAddr(addr);
        else if (isCs710Connected()) { }
    }
    public void set_fdWrite(int addr, long value) {
        if (DEBUG) Log.i("Hello2", "set_fdWrite");
        if (isCs108Connected()) cs108Library4A.set_fdWrite(addr, value);
        else if (isCs710Connected()) { }
    }
    public void set_fdPwd(int value) {
        if (DEBUG) Log.i("Hello2", "set_fdPwd");
        if (isCs108Connected()) cs108Library4A.set_fdPwd(value);
        else if (isCs710Connected()) { }
    }
    public void set_fdBlockAddr4GetTemperature(int addr) {
        if (DEBUG) Log.i("Hello2", "set_fdBlockAddr4GetTemperature");
        if (isCs108Connected()) cs108Library4A.set_fdBlockAddr4GetTemperature(addr);
        else if (isCs710Connected()) { }
    }
    public void set_fdReadMem(int addr, long len) {
        if (DEBUG) Log.i("Hello2", "set_fdReadMem");
        if (isCs108Connected()) cs108Library4A.set_fdReadMem(addr, len);
        else if (isCs710Connected()) { }
    }
    public void set_fdWriteMem(int addr, int len, long value) {
        if (DEBUG) Log.i("Hello2", "set_fdWriteMem");
        if (isCs108Connected()) cs108Library4A.set_fdWriteMem(addr, len, value);
        else if (isCs710Connected()) { }
    }
    public void setImpinJExtension(boolean tagFocus, boolean fastId) {
        if (DEBUG) Log.i("Hello2", "setImpinJExtension with tagFocus = " + tagFocus + ", fastId = " + fastId);
        if (isCs108Connected()) cs108Library4A.setImpinJExtension(tagFocus, fastId);
        else if (isCs710Connected()) cs710Library4A.setImpinJExtension(tagFocus, fastId);
        else Log.i("Hello2", "setImpinJExtension" + stringNOTCONNECT);
    }

    //============ Barcode ============
    public void getBarcodePreSuffix() {
        if (DEBUG) Log.i("Hello2", "getBarcodePreSuffix");
        if (isCs108Connected()) cs108Library4A.getBarcodePreSuffix();
        else if (isCs710Connected()) cs710Library4A.getBarcodePreSuffix();
        else Log.i("Hello2", "getBarcodePreSuffix" + stringNOTCONNECT);
    }
    public void getBarcodeReadingMode() {
        if (DEBUG) Log.i("Hello2", "getBarcodeReadingMode");
        if (isCs108Connected()) cs108Library4A.getBarcodeReadingMode();
        else if (isCs710Connected()) cs710Library4A.getBarcodeReadingMode();
        else Log.i("Hello2", "getBarcodeReadingMode" + stringNOTCONNECT);
    }
    public boolean isBarcodeFailure() {
        if (DEBUG) Log.i("Hello2", "isBarcodeFailure");
        if (isCs108Connected()) return cs108Library4A.isBarcodeFailure();
        else if (isCs710Connected()) return cs710Library4A.isBarcodeFailure();
        else Log.i("Hello2", "isBarcodeFailure" + stringNOTCONNECT);
        return false;
    }
    public String getBarcodeDate() {
        if (DEBUG) Log.i("Hello2", "getBarcodeDate");
        if (isCs108Connected()) return cs108Library4A.getBarcodeDate();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeDate();
        else Log.i("Hello2", "getBarcodeDate" + stringNOTCONNECT);
        return null;
    }
    public boolean getBarcodeOnStatus() {
        if (DEBUG) Log.i("Hello2", "getBarcodeOnStatus");
        if (isCs108Connected()) return cs108Library4A.getBarcodeOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeOnStatus();
        else Log.i("Hello2", "getBarcodeOnStatus" + stringNOTCONNECT);
        return false;
    }
    public boolean setBarcodeOn(boolean on) {
        if (DEBUG) Log.i("Hello2", "setBarcodeOn");
        if (isCs108Connected()) return cs108Library4A.setBarcodeOn(on);
        else if (isCs710Connected()) return cs710Library4A.setBarcodeOn(on);
        else Log.i("Hello2", "setBarcodeOn" + stringNOTCONNECT);
        return false;
    }
    public boolean setVibrateOn(int mode) {
        if (DEBUG || true) Log.i("Hello2", "setVibrateOn with mode = " + mode);
        if (isCs108Connected()) return cs108Library4A.setVibrateOn(mode);
        else if (isCs710Connected()) return cs710Library4A.setVibrateOn(mode);
        else Log.i("Hello2", "setVibrateOn" + stringNOTCONNECT);
        return false;
    }
    public boolean getInventoryVibrate() {
        if (DEBUG) Log.i("Hello2", "getInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.getInventoryVibrate();
        else if (isCs710Connected()) return cs710Library4A.getInventoryVibrate();
        else Log.i("Hello2", "getInventoryVibrate" + stringNOTCONNECT);
        return false;
    }
    public boolean setInventoryVibrate(boolean inventoryVibrate) {
        if (DEBUG) Log.i("Hello2", "setInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.setInventoryVibrate(inventoryVibrate);
        else if (isCs710Connected()) return cs710Library4A.setInventoryVibrate(inventoryVibrate);
        else Log.i("Hello2", "setInventoryVibrate" + stringNOTCONNECT);
        return false;
    }
    public int getVibrateTime() {
        if (DEBUG) Log.i("Hello2", "getVibrateTime");
        if (isCs108Connected()) return cs108Library4A.getVibrateTime();
        else if (isCs710Connected()) return cs710Library4A.getVibrateTime();
        else Log.i("Hello2", "getVibrateTime" + stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateTime(int vibrateTime) {
        if (DEBUG) Log.i("Hello2", "setVibrateTime");
        if (isCs108Connected()) return cs108Library4A.setVibrateTime(vibrateTime);
        else if (isCs710Connected()) return cs710Library4A.setVibrateTime(vibrateTime);
        else Log.i("Hello2", "setVibrateTime" + stringNOTCONNECT);
        return false;
    }
    public int getVibrateWindow() {
        if (DEBUG) Log.i("Hello2", "getVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.getVibrateWindow();
        else if (isCs710Connected()) return cs710Library4A.getVibrateWindow();
        else Log.i("Hello2", "getVibrateWindow" + stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateWindow(int vibrateWindow) {
        if (DEBUG) Log.i("Hello2", "setVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.setVibrateWindow(vibrateWindow);
        else if (isCs710Connected()) return cs710Library4A.setVibrateWindow(vibrateWindow);
        else Log.i("Hello2", "setVibrateWindow" + stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandTrigger() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandTrigger");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandTrigger();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandTrigger();
        else Log.i("Hello2", "barcodeSendCommandTrigger" + stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandSetPreSuffix() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandSetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandSetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandSetPreSuffix();
        else Log.i("Hello2", "barcodeSendCommandSetPreSuffix" + stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandResetPreSuffix() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandResetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandResetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandResetPreSuffix();
        else Log.i("Hello2", "barcodeSendCommandResetPreSuffix" + stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandConinuous() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandConinuous");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandConinuous();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandConinuous();
        else Log.i("Hello2", "barcodeSendCommandConinuous" + stringNOTCONNECT);
        return false;
    }
    public String getBarcodeVersion() {
        if (DEBUG) Log.i("Hello2", "getBarcodeVersion");
        if (isCs108Connected()) return cs108Library4A.getBarcodeVersion();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeVersion();
        else Log.i("Hello2", "getBarcodeVersion" + stringNOTCONNECT);
        return null;
    }
    public String getBarcodeSerial() {
        if (DEBUG) Log.i("Hello2", "getBarcodeSerial");
        if (isCs108Connected()) return cs108Library4A.getBarcodeSerial();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeSerial();
        else Log.i("Hello2", "getBarcodeSerial" + stringNOTCONNECT);
        return null;
    }
    public boolean barcodeInventory(boolean start) {
        if (DEBUG) Log.i("Hello2", "barcodeInventory");
        if (isCs108Connected()) return cs108Library4A.barcodeInventory(start);
        else if (isCs710Connected()) return cs710Library4A.barcodeInventory(start);
        else Log.i("Hello2", "barcodeInventory" + stringNOTCONNECT);
        return false;
    }
    public byte[] onBarcodeEvent() {
        if (DEBUG) Log.i("Hello2", "onBarcodeEvent");
        if (isCs108Connected()) return cs108Library4A.onBarcodeEvent();
        else if (isCs710Connected()) return cs710Library4A.onBarcodeEvent();
        else Log.i("Hello2", "onBarcodeEvent" + stringNOTCONNECT);
        return null;
    }

    //============ Android General ============
    public void setSameCheck(boolean sameCheck1) {
        if (DEBUG) Log.i("Hello2", "setSameCheck");
        if (isCs108Connected()) cs108Library4A.setSameCheck(sameCheck1);
        else if (isCs710Connected()) cs710Library4A.setSameCheck(sameCheck1);
        else Log.i("Hello2", "setSameCheck" + stringNOTCONNECT);
    }
    public void saveSetting2File() {
        if (DEBUG) Log.i("Hello2", "saveSetting2File");
        if (isCs108Connected()) cs108Library4A.saveSetting2File();
        else if (isCs710Connected()) cs710Library4A.saveSetting2File();
        else Log.i("Hello2", "saveSetting2File" + stringNOTCONNECT);
    }
    public int getBeepCount() {
        if (DEBUG) Log.i("Hello2", "getBeepCount");
        if (isCs108Connected()) return cs108Library4A.getBeepCount();
        else if (isCs710Connected()) return cs710Library4A.getBeepCount();
        else Log.i("Hello2", "getBeepCount" + stringNOTCONNECT);
        return -1;
    }
    public boolean setBeepCount(int beepCount) {
        if (DEBUG) Log.i("Hello2", "setBeepCount");
        if (isCs108Connected()) return cs108Library4A.setBeepCount(beepCount);
        else if (isCs710Connected()) return cs710Library4A.setBeepCount(beepCount);
        else Log.i("Hello2", "setBeepCount" + stringNOTCONNECT);
        return false;
    }
    public boolean getInventoryBeep() {
        if (DEBUG) Log.i("Hello2", "getInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.getInventoryBeep();
        else if (isCs710Connected()) return cs710Library4A.getInventoryBeep();
        else Log.i("Hello2", "getInventoryBeep" + stringNOTCONNECT);
        return false;
    }
    public boolean setInventoryBeep(boolean inventoryBeep) {
        if (DEBUG) Log.i("Hello2", "setInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.setInventoryBeep(inventoryBeep);
        else if (isCs710Connected()) return cs710Library4A.setInventoryBeep(inventoryBeep);
        else Log.i("Hello2", "setInventoryBeep" + stringNOTCONNECT);
        return false;
    }
    public boolean getSaveFileEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveFileEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveFileEnable();
        else Log.i("Hello2", "getSaveFileEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setSaveFileEnable(boolean saveFileEnable) {
        if (DEBUG) Log.i("Hello2", "setSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveFileEnable(saveFileEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveFileEnable(saveFileEnable);
        else Log.i("Hello2", "setSaveFileEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean getSaveCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveCloudEnable();
        else Log.i("Hello2", "getSaveCloudEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setSaveCloudEnable(boolean saveCloudEnable) {
        if (DEBUG) Log.i("Hello2", "setSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveCloudEnable(saveCloudEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveCloudEnable(saveCloudEnable);
        else Log.i("Hello2", "setSaveCloudEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean getSaveNewCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveNewCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveNewCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveNewCloudEnable();
        else Log.i("Hello2", "getSaveNewCloudEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setSaveNewCloudEnable(boolean saveNewCloudEnable) {
        Log.i("Hello2", "setSaveNewCloudEnable");
        return false;
    }
    public boolean getSaveAllCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveAllCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveAllCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveAllCloudEnable();
        else Log.i("Hello2", "getSaveAllCloudEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setSaveAllCloudEnable(boolean saveAllCloudEnable) {
        Log.i("Hello2", "setSaveAllCloudEnable");
        return false;
    }
    public boolean getUserDebugEnable() {
        if (DEBUG) Log.i("Hello2", "getUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.getUserDebugEnable();
        else if (isCs710Connected()) return cs710Library4A.getUserDebugEnable();
        else Log.i("Hello2", "getUserDebugEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setUserDebugEnable(boolean userDebugEnable) {
        if (DEBUG) Log.i("Hello2", "setUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.setUserDebugEnable(userDebugEnable);
        else if (isCs710Connected()) return cs710Library4A.setUserDebugEnable(userDebugEnable);
        else Log.i("Hello2", "getUserDebugEnable" + stringNOTCONNECT);
        return false;
    }
    public String getForegroundReader() {
        String string108 = cs108Library4A.getForegroundReader().trim();
        String string710 = cs710Library4A.getForegroundReader().trim();
        appendToLog("foregroundReader108 = " + string108 + ", foregroundReader710 = " + string710);
        if (isCs108Connected()) return string108;
        return string710;
    }
    public boolean getForegroundServiceEnable() {
        if (DEBUG) Log.i("Hello2", "getForegroundEnable");
        if (isCs108Connected()) return cs108Library4A.getForegroundServiceEnable();
        else if (isCs710Connected()) return cs710Library4A.getForegroundServiceEnable();
        else Log.i("Hello2", "getForegroundEnable" + stringNOTCONNECT);
        return false;
    }
    public boolean setForegroundServiceEnable(boolean forgroundServiceEnable) {
        if (DEBUG) Log.i("Hello2", "setForegroundServiceEnable");
        if (isCs108Connected()) return cs108Library4A.setForegroundServiceEnable(forgroundServiceEnable);
        else if (isCs710Connected()) return cs710Library4A.setForegroundServiceEnable(forgroundServiceEnable);
        else Log.i("Hello2", "setForegroundServiceEnable" + stringNOTCONNECT);
        return false;
    }
    public String getServerLocation() {
        if (DEBUG) Log.i("Hello2", "getServerLocation");
        if (isCs108Connected()) return cs108Library4A.getServerLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerLocation();
        else Log.i("Hello2", "getServerLocation" + stringNOTCONNECT);
        return null;
    }
    public boolean setServerLocation(String serverLocation) {
        if (DEBUG) Log.i("Hello2", "setServerLocation");
        if (isCs108Connected()) return cs108Library4A.setServerLocation(serverLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerLocation(serverLocation);
        else Log.i("Hello2", "setServerLocation" + stringNOTCONNECT);
        return false;
    }
    public int getServerTimeout() {
        if (DEBUG) Log.i("Hello2", "getServerTimeout");
        if (isCs108Connected()) return cs108Library4A.getServerTimeout();
        else if (isCs710Connected()) return cs710Library4A.getServerTimeout();
        else Log.i("Hello2", "getServerTimeout" + stringNOTCONNECT);
        return -1;
    }
    public boolean setServerTimeout(int serverTimeout) {
        if (DEBUG) Log.i("Hello2", "setServerTimeout");
        if (isCs108Connected()) return cs108Library4A.setServerTimeout(serverTimeout);
        else if (isCs710Connected()) return cs710Library4A.setServerTimeout(serverTimeout);
        else Log.i("Hello2", "setServerTimeout" + stringNOTCONNECT);
        return false;
    }
    public String getServerMqttLocation() {
        if (DEBUG) Log.i("Hello2", "getServerMqttLocation");
        if (isCs108Connected()) return cs108Library4A.getServerMqttLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerMqttLocation();
        else Log.i("Hello2", "getServerMqttLocation" + stringNOTCONNECT);
        return null;
    }
    public boolean setServerMqttLocation(String serverLocation) {
        if (DEBUG) Log.i("Hello2", "setServerMqttLocation");
        if (isCs108Connected()) return cs108Library4A.setServerMqttLocation(serverLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerMqttLocation(serverLocation);
        else Log.i("Hello2", "setServerMqttLocation" + stringNOTCONNECT);
        return false;
    }
    public String getTopicMqtt() {
        if (DEBUG) Log.i("Hello2", "getServerTopicMqtt");
        if (isCs108Connected()) return cs108Library4A.getTopicMqtt();
        else if (isCs710Connected()) return cs710Library4A.getTopicMqtt();
        else Log.i("Hello2", "getServerTopicMqtt" + stringNOTCONNECT);
        return null;
    }
    public boolean setTopicMqtt(String topic) {
        if (DEBUG) Log.i("Hello2", "setServerTopicMqtt");
        if (isCs108Connected()) return cs108Library4A.setTopicMqtt(topic);
        else if (isCs710Connected()) return cs710Library4A.setTopicMqtt(topic);
        else Log.i("Hello2", "setServerTopicMqtt" + stringNOTCONNECT);
        return false;
    }
    public int getForegroundDupElim() {
        if (DEBUG) Log.i("Hello2", "getForegroundDupElim");
        if (isCs108Connected()) return cs108Library4A.getForegroundDupElim();
        else if (isCs710Connected()) return cs710Library4A.getForegroundDupElim();
        else Log.i("Hello2", "getForegroundDupElim" + stringNOTCONNECT);
        return -1;
    }
    public boolean setForegroundDupElim(int iForegroundDupElim) {
        if (DEBUG) Log.i("Hello2", "setForegroundDupElim");
        if (isCs108Connected()) return cs108Library4A.setForegroundDupElim(iForegroundDupElim);
        else if (isCs710Connected()) return cs710Library4A.setForegroundDupElim(iForegroundDupElim);
        else Log.i("Hello2", "setForegroundDupElim" + stringNOTCONNECT);
        return false;
    }
    public int getInventoryCloudSave() {
        if (DEBUG) Log.i("Hello2", "getInventoryCloudSave");
        if (isCs108Connected()) return cs108Library4A.getInventoryCloudSave();
        else if (isCs710Connected()) return cs710Library4A.getInventoryCloudSave();
        else Log.i("Hello2", "getInventoryCloudSave" + stringNOTCONNECT);
        return -1;
    }
    public boolean setInventoryCloudSave(int inventoryCloudSave) {
        if (DEBUG) Log.i("Hello2", "setInventoryCloudSave");
        if (isCs108Connected()) return cs108Library4A.setInventoryCloudSave(inventoryCloudSave);
        else if (isCs710Connected()) return cs710Library4A.setInventoryCloudSave(inventoryCloudSave);
        else Log.i("Hello2", "setInventoryCloudSave" + stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjLocation() {
        if (DEBUG) Log.i("Hello2", "getServerImpinjLocation");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjLocation();
        else Log.i("Hello2", "getServerImpinjLocation" + stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjLocation(String serverImpinjLocation) {
        if (DEBUG) Log.i("Hello2", "setServerImpinjLocation");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjLocation(serverImpinjLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjLocation(serverImpinjLocation);
        else Log.i("Hello2", "setServerImpinjLocation" + stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjName() {
        if (DEBUG) Log.i("Hello2", "getServerImpinjName");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjName();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjName();
        else Log.i("Hello2", "getServerImpinjName" + stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjName(String serverImpinjName) {
        if (DEBUG) Log.i("Hello2", "setServerImpinjName");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjName(serverImpinjName);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjName(serverImpinjName);
        else Log.i("Hello2", "setServerImpinjName" + stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjPassword() {
        if (DEBUG) Log.i("Hello2", "getServerImpinjPassword");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjPassword();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjPassword();
        else Log.i("Hello2", "getServerImpinjPassword" + stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjPassword(String serverImpinjPassword) {
        if (DEBUG) Log.i("Hello2", "setServerImpinjPassword");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjPassword(serverImpinjPassword);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjPassword(serverImpinjPassword);
        else Log.i("Hello2", "setServerImpinjPassword" + stringNOTCONNECT);
        return false;
    }

    public int getBatteryDisplaySetting() {
        if (DEBUG) Log.i("Hello2", "getBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplaySetting();
        else Log.i("Hello2", "getBatteryDisplaySetting" + stringNOTCONNECT);
        return -1;
    }
    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        if (DEBUG) Log.i("Hello2", "setBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else Log.i("Hello2", "setBatteryDisplaySetting" + stringNOTCONNECT);
        return false;
    }
    public double dBuV_dBm_constant = RfidReader.dBuV_dBm_constant; //106.98;
    public int getRssiDisplaySetting() {
        if (DEBUG) Log.i("Hello2", "getRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getRssiDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getRssiDisplaySetting();
        return 0;
    }
    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        if (DEBUG) Log.i("Hello2", "setRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else Log.i("Hello2", "setRssiDisplaySetting" + stringNOTCONNECT);
        return false;
    }
    public int getVibrateModeSetting() {
        if (DEBUG) Log.i("Hello2", "getVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.getVibrateModeSetting();
        else if (isCs710Connected()) return cs710Library4A.getVibrateModeSetting();
        else Log.i("Hello2", "getVibrateModeSetting" + stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateModeSetting(int vibrateModeSelect) {
        if (DEBUG) Log.i("Hello2", "setVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.setVibrateModeSetting(vibrateModeSelect);
        else if (isCs710Connected()) return cs710Library4A.setVibrateModeSetting(vibrateModeSelect);
        else Log.i("Hello2", "setVibrateModeSetting" + stringNOTCONNECT);
        return false;
    }
    public int getSavingFormatSetting() {
        if (DEBUG) Log.i("Hello2", "getSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.getSavingFormatSetting();
        else if (isCs710Connected()) return cs710Library4A.getSavingFormatSetting();
        else Log.i("Hello2", "getSavingFormatSetting" + stringNOTCONNECT);
        return -1;
    }
    public boolean setSavingFormatSetting(int savingFormatSelect) {
        if (DEBUG) Log.i("Hello2", "setSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.setSavingFormatSetting(savingFormatSelect);
        else if (isCs710Connected()) return cs710Library4A.setSavingFormatSetting(savingFormatSelect);
        else Log.i("Hello2", "setSavingFormatSetting" + stringNOTCONNECT);
        return false;
    }
    public int getCsvColumnSelectSetting() {
        if (DEBUG) Log.i("Hello2", "getCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.getCsvColumnSelectSetting();
        else if (isCs710Connected()) return cs710Library4A.getCsvColumnSelectSetting();
        else Log.i("Hello2", "getCsvColumnSelectSetting" + stringNOTCONNECT);
        return -1;
    }
    public boolean setCsvColumnSelectSetting(int csvColumnSelect) {
        if (DEBUG) Log.i("Hello2", "setCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else if (isCs710Connected()) return cs710Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else Log.i("Hello2", "setCsvColumnSelectSetting" + stringNOTCONNECT);
        return false;
    }
    public String getWedgeDeviceName() {
        if (isCs108Connected()) return cs108Library4A.getWedgeDeviceName();
        else return cs710Library4A.getWedgeDeviceName();
    }
    public String getWedgeDeviceAddress() {
        if (isCs108Connected()) return cs108Library4A.getWedgeDeviceAddress();
        else return cs710Library4A.getWedgeDeviceAddress();
    }
    public int getWedgeDeviceUUID2p1() {
        if (isCs108Connected()) return cs108Library4A.getWedgeDeviceUUID2p1();
        else return cs710Library4A.getWedgeDeviceUUID2p1();
    }
    public int getWedgePower() {
        if (isCs108Connected()) return cs108Library4A.getWedgePower();
        else return cs710Library4A.getWedgePower();
    }
    public String getWedgePrefix() {
        if (isCs108Connected()) return cs108Library4A.getWedgePrefix();
        else return cs710Library4A.getWedgePrefix();
    }
    public String getWedgeSuffix() {
        if (isCs108Connected()) return cs108Library4A.getWedgeSuffix();
        else return cs710Library4A.getWedgeSuffix();
    }
    public int getWedgeDelimiter() {
        if (isCs108Connected()) return cs108Library4A.getWedgeDelimiter();
        else return cs710Library4A.getWedgeDelimiter();
    }
    public int getWedgeOutput() {
        if (isCs108Connected()) return cs108Library4A.getWedgeOutput();
        else return cs710Library4A.getWedgeOutput();
    }
    public void setWedgeDeviceName(String wedgeDeviceName) {
        if (isCs108Connected()) cs108Library4A.setWedgeDeviceName(wedgeDeviceName);
        else cs710Library4A.setWedgeDeviceName(wedgeDeviceName);
    }
    public void setWedgeDeviceAddress(String wedgeDeviceAddress) {
        if (isCs108Connected()) cs108Library4A.setWedgeDeviceAddress(wedgeDeviceAddress);
        else cs710Library4A.setWedgeDeviceAddress(wedgeDeviceAddress);
    }
    public void setWedgeDeviceUUID2p1(int wedgeDeviceUUID2p1) {
        if (isCs108Connected()) cs108Library4A.setWedgeDeviceUUID2p1(wedgeDeviceUUID2p1);
        else cs710Library4A.setWedgeDeviceUUID2p1(wedgeDeviceUUID2p1);
    }
    public void setWedgePower(int iPower) {
        if (isCs108Connected()) cs108Library4A.setWedgePower(iPower);
        else cs710Library4A.setWedgePower(iPower);
    }
    public void setWedgePrefix(String string) {
        if (isCs108Connected()) cs108Library4A.setWedgePrefix(string);
        else cs710Library4A.setWedgePrefix(string);
    }
    public void setWedgeSuffix(String string) {
        if (isCs108Connected()) cs108Library4A.setWedgeSuffix(string);
        else cs710Library4A.setWedgeSuffix(string);
    }
    public void setWedgeDelimiter(int iValue) {
        if (isCs108Connected()) cs108Library4A.setWedgeDelimiter(iValue);
        else cs710Library4A.setWedgeDelimiter(iValue);
    }
    public void setWedgeOutput(int iOutput) {
        if (isCs108Connected()) cs108Library4A.setWedgeOutput(iOutput);
        else cs710Library4A.setWedgeOutput(iOutput);
    }
    public void saveWedgeSetting2File() {
        if (isCs108Connected()) cs108Library4A.saveWedgeSetting2File();
        else cs710Library4A.saveWedgeSetting2File();
    }

    //============ Bluetooth ============
    public String getBluetoothICFirmwareVersion() {
        if (DEBUG) Log.i("Hello2", "getBluetoothICFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareVersion();
        else Log.i("Hello2", "getBluetoothICFirmwareVersion" + stringNOTCONNECT);
        return null;
    }
    public String getBluetoothICFirmwareName() {
        if (DEBUG) Log.i("Hello2", "getBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareName();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareName();
        else Log.i("Hello2", "getBluetoothICFirmwareName" + stringNOTCONNECT);
        return null;
    }
    public boolean setBluetoothICFirmwareName(String name) {
        if (DEBUG) Log.i("Hello2", "setBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.setBluetoothICFirmwareName(name);
        else if (isCs710Connected()) return cs710Library4A.setBluetoothICFirmwareName(name);
        else Log.i("Hello2", "setBluetoothICFirmwareName" + stringNOTCONNECT);
        return false;
    }

    //============ Controller ============
    public String hostProcessorICGetFirmwareVersion() {
        if (DEBUG) Log.i("Hello2", "hostProcessorICGetFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.hostProcessorICGetFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.hostProcessorICGetFirmwareVersion();
        else Log.i("Hello2", "hostProcessorICGetFirmwareVersion" + stringNOTCONNECT);
        return null;
    }
    public String getHostProcessorICSerialNumber() {
        if (DEBUG) Log.i("Hello2", "getHostProcessorICSerialNumber");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICSerialNumber();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICSerialNumber();
        else Log.i("Hello2", "getHostProcessorICSerialNumber" + stringNOTCONNECT);
        return null;
    }
    public String getHostProcessorICBoardVersion() {
        if (DEBUG) Log.i("Hello2", "getHostProcessorICBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICBoardVersion();
        else Log.i("Hello2", "getHostProcessorICBoardVersion" + stringNOTCONNECT);
        return null;
    }

    //============ Controller notification ============
    public int getBatteryLevel() {
        if (DEBUG) Log.i("Hello2", "getBatteryLevel");
        if (isCs108Connected()) return cs108Library4A.getBatteryLevel();
        else if (isCs710Connected()) return cs710Library4A.getBatteryLevel();
        else Log.i("Hello2", "getBatteryLevel" + stringNOTCONNECT);
        return -1;
    }
    public boolean setAutoTriggerReporting(byte timeSecond) {
        if (DEBUG) Log.i("Hello2", "setAutoTriggerReporting");
        if (isCs108Connected()) return cs108Library4A.setAutoTriggerReporting(timeSecond);
        else if (isCs710Connected()) return cs710Library4A.setAutoTriggerReporting(timeSecond);
        else Log.i("Hello2", "setAutoTriggerReporting" + stringNOTCONNECT);
        return false;
    }
    public boolean getAutoBarStartSTop() {
        if (DEBUG) Log.i("Hello2", "getAutoBarStartSTop");
        if (isCs108Connected()) return cs108Library4A.getAutoBarStartSTop();
        else if (isCs710Connected()) return cs710Library4A.getAutoBarStartSTop();
        else Log.i("Hello2", "getAutoBarStartSTop" + stringNOTCONNECT);
        return false;
    }
    public boolean batteryLevelRequest() {
        if (DEBUG) Log.i("Hello2", "batteryLevelRequest");
        if (isCs108Connected()) return cs108Library4A.batteryLevelRequest();
        else if (isCs710Connected()) return cs710Library4A.batteryLevelRequest();
        else Log.i("Hello2", "batteryLevelRequest" + stringNOTCONNECT);
        return false;
    }
    public boolean setAutoBarStartSTop(boolean enable) {
        if (DEBUG) Log.i("Hello2", "setAutoBarStartSTop");
        if (isCs108Connected()) return cs108Library4A.setAutoBarStartSTop(enable);
        else if (isCs710Connected()) return cs710Library4A.setAutoBarStartSTop(enable);
        else Log.i("Hello2", "setAutoBarStartSTop" + stringNOTCONNECT);
        return false;
    }
    public boolean getTriggerReporting() {
        if (DEBUG) Log.i("Hello2", "getTriggerReporting");
        if (isCs108Connected()) cs108Library4A.getTriggerReporting();
        else if (isCs710Connected()) cs710Library4A.getTriggerReporting();
        else Log.i("Hello2", "getTriggerReporting" + stringNOTCONNECT);
        return false;
    }
    public boolean setTriggerReporting(boolean triggerReporting) {
        if (DEBUG) Log.i("Hello2", "setTriggerReporting");
        if (isCs108Connected()) return cs108Library4A.setTriggerReporting(triggerReporting);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReporting(triggerReporting);
        else Log.i("Hello2", "setTriggerReporting" + stringNOTCONNECT);
        return false;
    }
    public int iNO_SUCH_SETTING = -1;
    public short getTriggerReportingCount() {
        if (DEBUG) Log.i("Hello2", "getTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerReportingCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerReportingCount();
        else Log.i("Hello2", "getTriggerReportingCount" + stringNOTCONNECT);
        return 5;
    }
    public boolean setTriggerReportingCount(short triggerReportingCount) {
        if (DEBUG) Log.i("Hello2", "setTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.setTriggerReportingCount(triggerReportingCount);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReportingCount(triggerReportingCount);
        else Log.i("Hello2", "setTriggerReportingCount" + stringNOTCONNECT);
        return false;
    }
    public String getBatteryDisplay(boolean voltageDisplay) {
        if (DEBUG) Log.i("Hello2", "getBatteryDisplay");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplay(voltageDisplay);
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplay(voltageDisplay);
        else Log.i("Hello2", "getBatteryDisplay is called befoe connection !!!");
        return null;
    }
    String stringNOTCONNECT;
    public String isBatteryLow() {
        if (DEBUG) Log.i("Hello2", "isBatteryLow");
        if (isCs108Connected()) return cs108Library4A.isBatteryLow();
        else if (isCs710Connected()) return cs710Library4A.isBatteryLow();
        else Log.i("Hello2", "isBatteryLow" + stringNOTCONNECT);
        return null;
    }
    public int getBatteryCount() {
        if (DEBUG2) Log.i("Hello2", "getBatteryCount");
        if (isCs108Connected()) return cs108Library4A.getBatteryCount();
        else if (isCs710Connected()) return cs710Library4A.getBatteryCount();
        else Log.i("Hello2", "getBatteryCount" + stringNOTCONNECT);
        return -1;
    }
    public boolean getTriggerButtonStatus() {
        if (DEBUG2) Log.i("Hello2", "getTriggerButtonStatus");
        if (isCs108Connected()) return cs108Library4A.getTriggerButtonStatus();
        else if (isCs710Connected()) return cs710Library4A.getTriggerButtonStatus();
        else Log.i("Hello2", "getTriggerButtonStatus" + stringNOTCONNECT);
        return false;
    }
    public int getTriggerCount() {
        if (DEBUG2) Log.i("Hello2", "getTriggerCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerCount();
        else Log.i("Hello2", "getTriggerCount" + stringNOTCONNECT);
        return -1;
    }
    //public interface NotificationListener { void onChange(); }
    public void setNotificationListener(NotificationConnector.NotificationListener listener) {
        if (DEBUG) Log.i("Hello2", "setNotificationListener");
        if (isCs108Connected()) {
            cs108Library4A.setNotificationListener(new NotificationConnector.NotificationListener() {
                @Override
                public void onChange() {
                    listener.onChange();
                }
            });
        } else if (isCs710Connected()) {
            cs710Library4A.setNotificationListener(new NotificationConnector.NotificationListener() {
                @Override
                public void onChange() {
                    listener.onChange();
                }
            });
        }
        else Log.i("Hello2", "setNotificationListener" + stringNOTCONNECT);
    }
    public byte[] onNotificationEvent() {
        if (DEBUG2) Log.i("Hello2", "onNotificationEvent");
        if (isCs108Connected()) return cs108Library4A.onNotificationEvent();
        else if (isCs710Connected()) return cs710Library4A.onNotificationEvent();
        else Log.i("Hello2", "onNotificationEvent" + stringNOTCONNECT);
        return null;
    }

    //============ to be modified ============
    public String getSerialNumber() {
        if (DEBUG2) Log.i("Hello2", "getSerialNumber");
        if (isCs108Connected()) return cs108Library4A.getSerialNumber();
        else if (isCs710Connected()) return cs710Library4A.getSerialNumber();
        else Log.i("Hello2", "getSerialNumber" + stringNOTCONNECT);
        return null;
    }
    public boolean setRfidOn(boolean onStatus) {
        if (DEBUG2) Log.i("Hello2", "setRfidOn");
        if (isCs108Connected()) return cs108Library4A.setRfidOn(onStatus);
        else if (isCs710Connected()) return cs710Library4A.setRfidOn(onStatus);
        else Log.i("Hello2", "setRfidOn" + stringNOTCONNECT);
        return false;
    }
    public int getcsModel() {
        if (DEBUG2) Log.i("Hello2", "getcsModel");
        if (isCs108Connected()) return cs108Library4A.getcsModel();
        else if (isCs710Connected()) return cs710Library4A.getcsModel();
        else Log.i("Hello2", "getcsModel" + stringNOTCONNECT);
        return -1;
    }
    public int getAntennaCycle() {
        if (DEBUG2) Log.i("Hello2", "getAntennaCycle");
        if (isCs108Connected()) return cs108Library4A.getAntennaCycle();
        else if (isCs710Connected()) return cs710Library4A.getAntennaCycle();
        else Log.i("Hello2", "getAntennaCycle" + stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaCycle(int antennaCycle) {
        if (DEBUG2) Log.i("Hello2", "setAntennaCycle");
        if (isCs108Connected()) return cs108Library4A.setAntennaCycle(antennaCycle);
        else if (isCs710Connected()) return cs710Library4A.setAntennaCycle(antennaCycle);
        else Log.i("Hello2", "setAntennaCycle" + stringNOTCONNECT);
        return false;
    }
    public boolean setAntennaInvCount(long antennaInvCount) {
        if (DEBUG2) Log.i("Hello2", "setAntennaInvCount");
        if (isCs108Connected()) return cs108Library4A.setAntennaInvCount(antennaInvCount);
        else if (isCs710Connected()) return cs710Library4A.setAntennaInvCount(antennaInvCount);
        else Log.i("Hello2", "setAntennaInvCount" + stringNOTCONNECT);
        return false;
    }
    public void clearInvalidata() {
        if (DEBUG2) Log.i("Hello2", "clearInvalidata");
        if (isCs108Connected()) cs108Library4A.clearInvalidata();
        else if (isCs710Connected()) cs710Library4A.clearInvalidata();
        else Log.i("Hello2", "clearInvalidata" + stringNOTCONNECT);
    }
    public int getInvalidata() {
        if (DEBUG2) Log.i("Hello2", "getInvalidata");
        if (isCs108Connected()) return cs108Library4A.getInvalidata();
        else if (isCs710Connected()) return cs710Library4A.getInvalidata();
        else Log.i("Hello2", "getInvalidata" + stringNOTCONNECT);
        return -1;
    }
    public int getInvalidUpdata() {
        if (DEBUG2) Log.i("Hello2", "getInvalidUpdata");
        if (isCs108Connected()) return cs108Library4A.getInvalidUpdata();
        else if (isCs710Connected()) return cs710Library4A.getInvalidUpdata();
        else Log.i("Hello2", "getInvalidUpdata" + stringNOTCONNECT);
        return -1;
    }
    public int getValidata() {
        if (DEBUG2) Log.i("Hello2", "getValidata");
        if (isCs108Connected()) return cs108Library4A.getValidata();
        else if (isCs710Connected()) return cs710Library4A.getValidata();
        else Log.i("Hello2", "getValidata" + stringNOTCONNECT);
        return -1;
    }

    //============ not public ============
    int bConnectStatus = 0;
    int iServiceUuidConnectedBefore = -1;
    private boolean isCs108Connected() { return (bConnectStatus == 1); }
    private boolean isCs710Connected() { return (bConnectStatus == 7); }

    public int setSelectData(RfidReader.TagType tagType, String mDid, boolean bNeedSelectedTagByTID, String stringProtectPassword, int selectFor, int selectHold) {
        if (DEBUG2) Log.i("Hello2", "setSelectData");
        if (isCs108Connected()) return cs108Library4A.setSelectData(tagType, mDid, bNeedSelectedTagByTID, stringProtectPassword, selectFor, selectHold);
        else if (isCs710Connected()) return cs710Library4A.setSelectData(tagType, mDid, bNeedSelectedTagByTID, stringProtectPassword, selectFor, selectHold);
        else Log.i("Hello2", "setSelectData" + stringNOTCONNECT);
        return -1;
    }
}
