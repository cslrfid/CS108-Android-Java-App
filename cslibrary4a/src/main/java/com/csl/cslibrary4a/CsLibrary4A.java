package com.csl.cslibrary4a;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class CsLibrary4A {
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
        Logger.debug("getlibraryVersion");
        String string710 = cs710Library4A.getlibraryVersion();
        Logger.trace("string710 = {}", string710);
        int iPos0 = string710.indexOf(".");
        int iPos1 = string710.substring(iPos0 + 1).indexOf(".");
        String string108 = cs108Library4A.getlibraryVersion();
        Logger.trace("string108 = {}", string108);
        int iPos2 = string108.indexOf(".");
        int iPos3 = string108.substring(iPos2 + 1).indexOf(".");
        return stringVersion + "-" + string710.substring(iPos0 + iPos1 + 2) + "-" + string108.substring(iPos2 + iPos3 + 2);
    }
    public String checkVersion() {
        Logger.debug("checkVersion");
        if (isCs108Connected()) return cs108Library4A.checkVersion();
        else if (isCs710Connected()) return cs710Library4A.checkVersion();
        else Logger.trace("Hello2", "checkVersion" + stringNOTCONNECT);
        return null;
    }

    //============ utility ============
    public String byteArrayToString(byte[] packet) {
        return utility.byteArrayToString(packet);
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
        Logger.trace("isBleScanning");
        boolean bValue = false, bValue1 = false, bValue7 = false;
        bValue1 = cs108Library4A.isBleScanning();
        bValue7 = cs710Library4A.isBleScanning();
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Logger.trace("isBleScanning: bVAlue1 = {}, bValu7 = {}", bValue1, bValue7);
        return bValue;
    }
    public boolean scanLeDevice(final boolean enable) {
        boolean bValue = false, bValue1 = false, bValue7 = false;
        Logger.trace("scanLeDevice");
        bValue1 = cs108Library4A.scanLeDevice(enable);
        bValue7 = cs710Library4A.scanLeDevice(enable);
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Logger.trace("scanLeDevice: bValue1 = {}, bValue7 = {}", bValue1, bValue7);
        return bValue;
    }
    public BluetoothGatt.CsScanData getNewDeviceScanned() {
        Logger.trace("getNewDeviceScanned");
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
        Logger.trace("getBluetoothDeviceAddress");
        if (isCs108Connected()) return cs108Library4A.getBluetoothDeviceAddress();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothDeviceAddress();
        else Logger.trace("getBluetoothDeviceAddress {}", stringNOTCONNECT);
        return null;
    }
    public String getBluetoothDeviceName() {
        Logger.trace("getBluetoothDeviceName");
        if (isCs108Connected()) return cs108Library4A.getBluetoothDeviceName();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothDeviceName();
        else Logger.trace("getBluetoothDeviceName {}", stringNOTCONNECT);
        return null;
    }
    public boolean isBleConnected() {
        boolean bValue = false;
        Logger.trace("isBleConnected");
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
        Logger.trace("connect with readerDevice as {}, and iServiceUuidConnectedBefore = {}", readerDevice != null ? "valid" : "null", iServiceUuidConnectedBefore);
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
        } else {
            Logger.trace("invalid serviceUUID = {}", readerDevice == null ? "null" : readerDevice.getServiceUUID2p1());
        }
    }
    public void disconnect(boolean tempDisconnect) {
        Logger.trace("disconnect");
        if (isCs108Connected()) cs108Library4A.disconnect(tempDisconnect);
        else if (isCs710Connected()) cs710Library4A.disconnect(tempDisconnect);
    }
    public boolean forceBTdisconnect() {
        Logger.trace("forceBTdisconnect");
        if (isCs108Connected()) return cs108Library4A.forceBTdisconnect();
        else if (isCs710Connected()) return cs710Library4A.forceBTdisconnect();
        else Logger.trace("forceBTdisconnect {}", stringNOTCONNECT);
        return false;
    }
    public int getRssi() {
        Logger.trace("getRssi");
        if (isCs108Connected()) return cs108Library4A.getRssi();
        else if (isCs710Connected()) return cs710Library4A.getRssi();
        else Logger.trace("getRssi {}", stringNOTCONNECT);
        return -1;
    } //411
    public long getStreamInRate() {
        Logger.trace("getStreamInRate");
        if (isCs108Connected()) return cs108Library4A.getStreamInRate();
        else if (isCs710Connected()) return cs710Library4A.getStreamInRate();
        else Logger.trace("getStreamInRate {}", stringNOTCONNECT);
        return -1;
    }
    public int get98XX() {
        Logger.trace("get98XX");
        if (isCs108Connected()) return cs108Library4A.get98XX();
        else if (isCs710Connected()) return cs710Library4A.get98XX();
        else Logger.trace("get98XX {}", stringNOTCONNECT);
        return -1;
    }


    //============ Rfid ============
    public String getAuthMatchData() {
        Logger.trace("getAuthMatchData");
        if (isCs108Connected()) return cs108Library4A.getAuthMatchData();
        else if (isCs710Connected()) return cs710Library4A.getAuthMatchData();
        else Logger.trace("getAuthMatchData {}", stringNOTCONNECT);
        return null;
    }
    public boolean setAuthMatchData(String mask) {
        Logger.trace("setAuthMatchData");
        if (isCs108Connected()) return cs108Library4A.setAuthMatchData(mask);
        else if (isCs710Connected()) return cs710Library4A.setAuthMatchData(mask);
        else Logger.trace("setAuthMatchData {}", stringNOTCONNECT);
        return false;
    }
    public int getStartQValue() {
        Logger.trace("getStartQValue");
        if (isCs108Connected()) return cs108Library4A.getStartQValue();
        else if (isCs710Connected()) return cs710Library4A.getStartQValue();
        else Logger.trace("getStartQValue {}", stringNOTCONNECT);
        return -1;
    }
    public int getMaxQValue() {
        Logger.trace("getMaxQValue");
        if (isCs108Connected()) return cs108Library4A.getMaxQValue();
        else if (isCs710Connected()) return cs710Library4A.getMaxQValue();
        else Logger.trace("getMaxQValue {}", stringNOTCONNECT);
        return -1;
    }
    public int getMinQValue() {
        Logger.trace("getMinQValue");
        if (isCs108Connected()) return cs108Library4A.getMinQValue();
        else if (isCs710Connected()) return cs710Library4A.getMinQValue();
        else Logger.trace("getMinQValue {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setDynamicQParms(int startQValue, int minQValue, int maxQValue, int retryCount) {
        Logger.trace("setDynamicQParms");
        if (isCs108Connected()) return cs108Library4A.setDynamicQParms(startQValue, minQValue, maxQValue, retryCount);
        else if (isCs710Connected()) return cs710Library4A.setDynamicQParms(startQValue, minQValue, maxQValue, retryCount);
        else Logger.trace("setDynamicQParms {}", stringNOTCONNECT);
        return false;
    }
    public int getFixedQValue() {
        Logger.trace("getFixedQValue");
        if (isCs108Connected()) return cs108Library4A.getFixedQValue();
        else if (isCs710Connected()) return cs710Library4A.getFixedQValue();
        else Logger.trace("getFixedQValue {}", stringNOTCONNECT);
        return -1;
    }
    public int getFixedRetryCount() {
        Logger.trace("getFixedRetryCount");
        if (isCs108Connected()) return cs108Library4A.getFixedRetryCount();
        else if (isCs710Connected()) return cs710Library4A.getFixedRetryCount();
        else Logger.trace("getFixedRetryCount {}", stringNOTCONNECT);
        return -1;
    }
    public boolean getRepeatUnitNoTags() {
        Logger.trace("getRepeatUnitNoTags");
        if (isCs108Connected()) return cs108Library4A.getRepeatUnitNoTags();
        else if (isCs710Connected()) return cs710Library4A.getRepeatUnitNoTags();
        else Logger.trace("getRepeatUnitNoTags {}", stringNOTCONNECT);
        return false;
    }
    public boolean setFixedQParms(int qValue, int retryCount, boolean repeatUnitNoTags) {
        Logger.trace("setFixedQParms");
        if (isCs108Connected()) return cs108Library4A.setFixedQParms(qValue, retryCount, repeatUnitNoTags);
        else if (isCs710Connected()) return cs710Library4A.setFixedQParms(qValue, retryCount, repeatUnitNoTags);
        else Logger.trace("setFixedQParms {}", stringNOTCONNECT);
        return false;
    }
    public boolean getChannelHoppingDefault() {
        Logger.trace("getChannelHoppingDefault");
        if (isCs108Connected()) return cs108Library4A.getChannelHoppingDefault();
        else if (isCs710Connected()) return cs710Library4A.getChannelHoppingDefault();
        else Logger.trace("getChannelHoppingDefault {}", stringNOTCONNECT);
        return false;
    }
    public boolean getRfidOnStatus() {
        Logger.trace("getRfidOnStatus");
        if (isCs108Connected()) return cs108Library4A.getRfidOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getRfidOnStatus();
        else Logger.trace("getRfidOnStatus {}", stringNOTCONNECT);
        return false;
    }
    public boolean isRfidFailure() {
        Logger.trace("isRfidFailure");
        if (isCs108Connected()) return cs108Library4A.isRfidFailure();
        else if (isCs710Connected()) return cs710Library4A.isRfidFailure();
        return false;
    }
    public void setReaderDefault() {
        Logger.trace("setReaderDefault");
        if (isCs108Connected()) cs108Library4A.setReaderDefault();
        else if (isCs710Connected()) cs710Library4A.setReaderDefault();
        else Logger.trace("setReaderDefault {}", stringNOTCONNECT);
    }
    public String getMacVer() {
        Logger.trace("getMacVer");
        if (isCs108Connected()) return cs108Library4A.getMacVer();
        else if (isCs710Connected()) return cs710Library4A.getMacVer();
        else Logger.trace("getMacVer {}", stringNOTCONNECT);
        return null;
    }
    public String getRadioSerial() {
        Logger.trace("getRadioSerial");
        if (isCs108Connected()) return cs108Library4A.getRadioSerial();
        else if (isCs710Connected()) return cs710Library4A.getRadioSerial();
        else Logger.trace("getRadioSerial {}", stringNOTCONNECT);
        return null;
    }
    public String getRadioBoardVersion() {
        Logger.trace("getRadioBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getRadioBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getRadioBoardVersion();
        else Logger.trace("getRadioBoardVersion {}", stringNOTCONNECT);
        return null;
    }
    public int getPortNumber() {
        Logger.trace("getPortNumber");
        if (isCs108Connected()) return cs108Library4A.getPortNumber();
        else if (isCs710Connected()) return cs710Library4A.getPortNumber();
        else Logger.trace("getPortNumber {}", stringNOTCONNECT);
        return -1;
    }
    public int getAntennaSelect() {
        Logger.trace("getAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.getAntennaSelect();
        else if (isCs710Connected()) return cs710Library4A.getAntennaSelect();
        else Logger.trace("getAntennaSelect {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaSelect(int number) {
        Logger.trace("setAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.setAntennaSelect(number);
        else if (isCs710Connected()) return cs710Library4A.setAntennaSelect(number);
        else Logger.trace("setAntennaSelect {}", stringNOTCONNECT);
        return false;
    }
    public boolean getAntennaEnable() {
        Logger.trace("getAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.getAntennaEnable();
        else if (isCs710Connected()) return cs710Library4A.getAntennaEnable();
        else Logger.trace("getAntennaEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAntennaEnable(boolean enable) {
        Logger.trace("setAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.setAntennaEnable(enable);
        else if (isCs710Connected()) return cs710Library4A.setAntennaEnable(enable);
        else Logger.trace("setAntennaEnable {}", stringNOTCONNECT);
        return false;
    }
    public long getAntennaDwell() {
        Logger.trace("getAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.getAntennaDwell();
        else if (isCs710Connected()) return cs710Library4A.getAntennaDwell();
        else Logger.trace("getAntennaDwell {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaDwell(long antennaDwell) {
        Logger.trace("setAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.setAntennaDwell(antennaDwell);
        else if (isCs710Connected()) return cs710Library4A.setAntennaDwell(antennaDwell);
        else Logger.trace("setAntennaDwell {}", stringNOTCONNECT);
        return false;
    }
    public long getPwrlevel() {
        Logger.trace("getPwrlevel");
        if (isCs108Connected()) return cs108Library4A.getPwrlevel();
        else if (isCs710Connected()) return cs710Library4A.getPwrlevel();
        else Logger.trace("getPwrlevel {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setPowerLevel(long pwrlevel) {
        Logger.trace("setPowerLevel");
        if (isCs108Connected()) return cs108Library4A.setPowerLevel(pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setPowerLevel(pwrlevel);
        else Logger.trace("setPowerLevel {}", stringNOTCONNECT);
        return false;
    }
    public int getQueryTarget() {
        Logger.trace("getQueryTarget");
        if (isCs108Connected()) return cs108Library4A.getQueryTarget();
        else if (isCs710Connected()) return cs710Library4A.getQueryTarget();
        else Logger.trace("getQueryTarget {}", stringNOTCONNECT);
        return -1;
    }
    public int getQuerySession() {
        Logger.trace("getQuerySession");
        if (isCs108Connected()) return cs108Library4A.getQuerySession();
        else if (isCs710Connected()) return cs710Library4A.getQuerySession();
        else Logger.trace("getQuerySession {}", stringNOTCONNECT);
        return -1;
    }
    public int getQuerySelect() {
        Logger.trace("getQuerySelect");
        if (isCs108Connected()) return cs108Library4A.getQuerySelect();
        else if (isCs710Connected()) return cs710Library4A.getQuerySelect();
        else Logger.trace("getQuerySelect {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setTagGroup(int sL, int session, int target1) {
        Logger.trace("setTagGroup");
        if (isCs108Connected()) return cs108Library4A.setTagGroup(sL, session, target1);
        else if (isCs710Connected()) return cs710Library4A.setTagGroup(sL, session, target1);
        else Logger.trace("setTagGroup {}", stringNOTCONNECT);
        return false;
    }
    public int getTagFocus() {
        Logger.trace("getTagFocus");
        if (isCs108Connected()) return cs108Library4A.getTagFocus();
        else if (isCs710Connected()) return cs710Library4A.getTagFocus();
        else Logger.trace("getTagFocus {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setTagFocus(boolean tagFocusNew) {
        Logger.trace("setTagFocus");
        if (isCs108Connected()) return cs108Library4A.setTagFocus(tagFocusNew);
        else if (isCs710Connected()) return cs710Library4A.setTagFocus(tagFocusNew);
        else Logger.trace("setTagFocus {}", stringNOTCONNECT);
        return false;
    }
    public int getFastId() {
        Logger.trace("getFastId");
        if (isCs108Connected()) return cs108Library4A.getFastId();
        else if (isCs710Connected()) return cs710Library4A.getFastId();
        else Logger.trace("getFastId {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setFastId(boolean fastIdNew) {
        Logger.trace("setFastId");
        if (isCs108Connected()) return cs108Library4A.setFastId(fastIdNew);
        else if (isCs710Connected()) return cs710Library4A.setFastId(fastIdNew);
        else Logger.trace("setFastId {}", stringNOTCONNECT);
        return false;
    }
    public boolean getInvAlgo() {
        Logger.trace("getInvAlgo");
        if (isCs108Connected()) return cs108Library4A.getInvAlgo();
        else if (isCs710Connected()) return cs710Library4A.getInvAlgo();
        else Logger.trace("getInvAlgo {}", stringNOTCONNECT);
        return false;
    }
    public boolean setInvAlgo(boolean dynamicAlgo) {
        Logger.trace("setInvAlgo");
        if (isCs108Connected()) return cs108Library4A.setInvAlgo(dynamicAlgo);
        else if (isCs710Connected()) return cs710Library4A.setInvAlgo(dynamicAlgo);
        else Logger.trace("setInvAlgo {}", stringNOTCONNECT);
        return false;
    }
    public List<String> getProfileList() {
        Logger.trace("getProfileList");
        if (isCs108Connected()) return cs108Library4A.getProfileList();
        else if (isCs710Connected()) return cs710Library4A.getProfileList();
        else Logger.trace("getProfileList {}", stringNOTCONNECT);
        return null;
    }
    public int getCurrentProfile() {
        Logger.trace("getCurrentProfile");
        if (isCs108Connected()) return cs108Library4A.getCurrentProfile();
        else if (isCs710Connected()) return cs710Library4A.getCurrentProfile();
        else Logger.trace("getCurrentProfile {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setBasicCurrentLinkProfile() {
        Logger.trace("setBasicCurrentLinkProfile");
        if (isCs108Connected()) return true;
        else if (isCs710Connected()) return cs710Library4A.setBasicCurrentLinkProfile();
        else Logger.trace("setBasicCurrentLinkProfile {}", stringNOTCONNECT);
        return false;
    }
    public boolean setCurrentLinkProfile(int profile) {
        Logger.trace("setCurrentLinkProfile to " + profile);
        if (isCs108Connected()) return cs108Library4A.setCurrentLinkProfile(profile);
        else if (isCs710Connected()) return cs710Library4A.setCurrentLinkProfile(profile);
        else Logger.trace("setCurrentLinkProfile {}", stringNOTCONNECT);
        return false;
    }
    public void resetEnvironmentalRSSI() {
        Logger.trace("resetEnvironmentalRSSI");
        if (isCs108Connected()) cs108Library4A.resetEnvironmentalRSSI();
        else if (isCs710Connected()) cs710Library4A.resetEnvironmentalRSSI();
        else Logger.trace("resetEnvironmentalRSSI {}", stringNOTCONNECT);
    }
    public String getEnvironmentalRSSI() {
        Logger.trace("getEnvironmentalRSSI");
        if (isCs108Connected()) return cs108Library4A.getEnvironmentalRSSI();
        else if (isCs710Connected()) return cs710Library4A.getEnvironmentalRSSI();
        else Logger.trace("getEnvironmentalRSSI {}", stringNOTCONNECT);
        return null;
    }
    public int getHighCompression() {
        Logger.trace("getHighCompression");
        if (isCs108Connected()) return cs108Library4A.getHighCompression();
        else if (isCs710Connected()) return cs710Library4A.getHighCompression();
        else Logger.trace("getHighCompression {}", stringNOTCONNECT);
        return -1;
    }
    public int getRflnaGain() {
        Logger.trace("getRflnaGain");
        if (isCs108Connected()) return cs108Library4A.getRflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getRflnaGain();
        else Logger.trace("getRflnaGain {}", stringNOTCONNECT);
        return -1;
    }
    public int getIflnaGain() {
        Logger.trace("getIflnaGain");
        if (isCs108Connected()) return cs108Library4A.getIflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getIflnaGain();
        else Logger.trace("getIflnaGain {}", stringNOTCONNECT);
        return -1;
    }
    public int getAgcGain() {
        Logger.trace("getAgcGain");
        if (isCs108Connected()) return cs108Library4A.getAgcGain();
        else if (isCs710Connected()) return cs710Library4A.getAgcGain();
        else Logger.trace("getAgcGain {}", stringNOTCONNECT);
        return -1;
    }
    public int getRxGain() {
        Logger.trace("getRxGain");
        if (isCs108Connected()) return cs108Library4A.getRxGain();
        else if (isCs710Connected()) return cs710Library4A.getRxGain();
        else Logger.trace("getRxGain {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        Logger.trace("setRxGain");
        if (isCs108Connected()) return cs108Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else if (isCs710Connected()) return cs710Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else Logger.trace("setRxGain {}", stringNOTCONNECT);
        return false;
    }
    public boolean setRxGain(int rxGain) {
        Logger.trace("setRxGain");
        if (isCs108Connected()) return cs108Library4A.setRxGain(rxGain);
        else if (isCs710Connected()) return cs710Library4A.setRxGain(rxGain);
        else Logger.trace("setRxGain {}", stringNOTCONNECT);
        return false;
    }
    public int FreqChnCnt() {
        Logger.trace("FreqChnCnt");
        if (isCs108Connected()) return cs108Library4A.FreqChnCnt();
        else if (isCs710Connected()) return cs710Library4A.FreqChnCnt();
        else Logger.trace("FreqChnCnt {}", stringNOTCONNECT);
        return -1;
    }
    public double getLogicalChannel2PhysicalFreq(int channel) {
        Logger.trace("getLogicalChannel2PhysicalFreq");
        if (isCs108Connected()) return cs108Library4A.getLogicalChannel2PhysicalFreq(channel);
        else if (isCs710Connected()) return cs710Library4A.getLogicalChannel2PhysicalFreq(channel);
        else Logger.trace("getLogicalChannel2PhysicalFreq {}", stringNOTCONNECT);
        return -1;
    }
    public byte getTagDelay() {
        Logger.trace("getTagDelay");
        if (isCs108Connected()) return cs108Library4A.getTagDelay();
        else if (isCs710Connected()) return cs710Library4A.getTagDelay();
        else Logger.trace("getTagDelay {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setTagDelay(byte tagDelay) {
        Logger.trace("setTagDelay");
        if (isCs108Connected()) return cs108Library4A.setTagDelay(tagDelay);
        else if (isCs710Connected()) return cs710Library4A.setTagDelay(tagDelay);
        else Logger.trace("setTagDelay {}", stringNOTCONNECT);
        return false;
    }
    public byte getIntraPkDelay() {
        Logger.trace("getIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.getIntraPkDelay();
        else if (isCs710Connected()) return cs710Library4A.getIntraPkDelay();
        else Logger.trace("getIntraPkDelay {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setIntraPkDelay(byte intraPkDelay) {
        Logger.trace("setIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.setIntraPkDelay(intraPkDelay);
        else if (isCs710Connected()) return cs710Library4A.setIntraPkDelay(intraPkDelay);
        else Logger.trace("setIntraPkDelay {}", stringNOTCONNECT);
        return false;
    }
    public byte getDupDelay() {
        Logger.trace("getDupDelay");
        if (isCs108Connected()) return cs108Library4A.getDupDelay();
        else if (isCs710Connected()) return cs710Library4A.getDupDelay();
        else Logger.trace("getDupDelay {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setDupDelay(byte dupElim) {
        Logger.trace("setDupDelay");
        if (isCs108Connected()) return cs108Library4A.setDupDelay(dupElim);
        else if (isCs710Connected()) return cs710Library4A.setDupDelay(dupElim);
        else Logger.trace("setDupDelay {}", stringNOTCONNECT);
        return false;
    }
    public long getCycleDelay() {
        Logger.trace("getCycleDelay");
        if (isCs108Connected()) return cs108Library4A.getCycleDelay();
        else if (isCs710Connected()) return cs710Library4A.getCycleDelay();
        else Logger.trace("getCycleDelay {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setCycleDelay(long cycleDelay) {
        Logger.trace("setCycleDelay");
        if (isCs108Connected()) return cs108Library4A.setCycleDelay(cycleDelay);
        else if (isCs710Connected()) return cs710Library4A.setCycleDelay(cycleDelay);
        return false;
    }
    public void getAuthenticateReplyLength() {
        Logger.trace("getAuthenticateReplyLength");
        if (isCs108Connected()) cs108Library4A.getAuthenticateReplyLength();
        else if (isCs710Connected()) cs710Library4A.getAuthenticateReplyLength();
        else Logger.trace("getAuthenticateReplyLength {}", stringNOTCONNECT);
    }
    public boolean setTamConfiguration(boolean header, String matchData) {
        Logger.trace("setTamConfiguration with header = {}, matchData = {}", header, matchData);
        if (isCs108Connected()) return cs108Library4A.setTamConfiguration(header, matchData);
        else if (isCs710Connected()) return cs710Library4A.setTamConfiguration(header, matchData);
        else Logger.trace("setTam1Configuration");
        return false;
    }
    public boolean setTam1Configuration(int keyId, String matchData) {
        Logger.trace("setTam1Configuration with KeyId = {}, matchData = {}", keyId, matchData);
        if (isCs108Connected()) return cs108Library4A.setTam1Configuration(keyId, matchData);
        else if (isCs710Connected()) return cs710Library4A.setTam1Configuration(keyId, matchData);
        else Logger.trace("setTam1Configuration");
        return false;
    }
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        Logger.trace("setTam2Configuration");
        if (isCs108Connected()) return cs108Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else if (isCs710Connected()) return cs710Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else Logger.trace("setTam2Configuration");
        return false;
    }
    public int getUntraceableEpcLength() {
        Logger.trace("getUntraceableEpcLength");
        if (isCs108Connected()) return cs108Library4A.getUntraceableEpcLength();
        else if (isCs710Connected()) return cs710Library4A.getUntraceableEpcLength();
        else Logger.trace("getUntraceableEpcLength {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        Logger.trace("setUntraceable 1");
        return false;
    }
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        Logger.trace("setUntraceable");
        if (isCs108Connected()) return cs108Library4A.setUntraceable(range, user, tid, epcLength, epc, uxpc);
        else if (isCs710Connected()) return false;
        else Logger.trace("setUntraceable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAuthenticateConfiguration() {
        Logger.trace("setAuthenticateConfiguration");
        if (isCs108Connected()) return cs108Library4A.setAuthenticateConfiguration();
        else if (isCs710Connected()) return cs710Library4A.setAuthenticateConfiguration();
        else Logger.trace("setAuthenticateConfiguration {}", stringNOTCONNECT);
        return false;
    }
    public int getRetryCount() {
        Logger.trace("getRetryCount");
        if (isCs108Connected()) return cs108Library4A.getRetryCount();
        else if (isCs710Connected()) return cs710Library4A.getRetryCount();
        else Logger.trace("getRetryCount {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setRetryCount(int retryCount) {
        Logger.trace("setRetryCount");
        if (isCs108Connected()) return cs108Library4A.setRetryCount(retryCount);
        else if (isCs710Connected()) return cs710Library4A.setRetryCount(retryCount);
        else Logger.trace("setRetryCount {}", stringNOTCONNECT);
        return false;
    }
    public int getInvSelectIndex() {
        Logger.trace("getInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.getInvSelectIndex();
        else if (isCs710Connected()) return cs710Library4A.getInvSelectIndex();
        else Logger.trace("getInvSelectIndex {}", stringNOTCONNECT);
        return -1;
    } //2286
    public boolean getSelectEnable() {
        Logger.trace("getSelectEnable");
        if (isCs108Connected()) return cs108Library4A.getSelectEnable();
        else if (isCs710Connected()) return cs710Library4A.getSelectEnable();
        else Logger.trace("getSelectEnable {}", stringNOTCONNECT);
        return false;
    }
    public int getSelectTarget() {
        Logger.trace("getSelectTarget");
        if (isCs108Connected()) return cs108Library4A.getSelectTarget();
        else if (isCs710Connected()) return cs710Library4A.getSelectTarget();
        else Logger.trace("getSelectTarget {}", stringNOTCONNECT);
        return -1;
    }
    public int getSelectAction() {
        Logger.trace("getSelectAction");
        if (isCs108Connected()) return cs108Library4A.getSelectAction();
        else if (isCs710Connected()) return cs710Library4A.getSelectAction();
        else Logger.trace("getSelectAction {}", stringNOTCONNECT);
        return -1;
    }
    public int getSelectMaskBank() {
        Logger.trace("getSelectMaskBank");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskBank();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskBank();
        else Logger.trace("getSelectMaskBank {}", stringNOTCONNECT);
        return -1;
    }
    public int getSelectMaskOffset() {
        Logger.trace("getSelectMaskOffset");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskOffset();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskOffset();
        else Logger.trace("getSelectMaskOffset {}", stringNOTCONNECT);
        return -1;
    }
    public String getSelectMaskData() {
        Logger.trace("getSelectMaskData");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskData();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskData();
        else Logger.trace("getSelectMaskData {}", stringNOTCONNECT);
        return null;
    }
    public boolean setInvSelectIndex(int invSelect) {
        Logger.trace("setInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.setInvSelectIndex(invSelect);
        else if (isCs710Connected()) return cs710Library4A.setInvSelectIndex(invSelect);
        else Logger.trace("setInvSelectIndex {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteriaDisable(int index) {
        Logger.trace("csLibrary4A: setSelectCriteria Disable with index = {}", index);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteriaDisable(index);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteriaDisable(index);
        else Logger.trace("setSelectCriteriaDisable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        Logger.trace("csLibrary4A: setSelectCriteria 1 with index = {}, enable = {}, target = {}, action = {}, bank = {}, offset = {}, mask = {}, maskbit = {}", index, enable, target, action, bank, offset, mask, maskbit);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else Logger.trace("setSelectCriteria 1 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        Logger.trace("csLibrary4A: setSelectCriteria 2 with index = {}, enable = {}, target = {}, action = {}, delay = {}, bank = {}, offset = {}, mask = {}", index, enable, target, action, delay, bank, offset, mask);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else Logger.trace("setSelectCriteria {}", stringNOTCONNECT);
        return false;
    }
    public boolean getRssiFilterEnable() {
        Logger.trace("getRssiFilterEnable");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterEnable();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterEnable();
        else Logger.trace("getRssiFilterEnable {}", stringNOTCONNECT);
        return false;
    }
    public int getRssiFilterType() {
        Logger.trace("getRssiFilterType");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterType();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterType();
        else Logger.trace("getRssiFilterType {}", stringNOTCONNECT);
        return -1;
    }
    public int getRssiFilterOption() {
        Logger.trace("getRssiFilterOption");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterOption();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterOption();
        return -1;
    }
    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        Logger.trace("setRssiFilterConfig");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else Logger.trace("setRssiFilterConfig {}", stringNOTCONNECT);
        return false;
    }
    public double getRssiFilterThreshold1() {
        Logger.trace("getRssiFilterThreshold1");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold1();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold1();
        else Logger.trace("getRssiFilterThreshold1 {}", stringNOTCONNECT);
        return -1;
    }
    public double getRssiFilterThreshold2() {
        Logger.trace("getRssiFilterThreshold2");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold2();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold2();
        else Logger.trace("getRssiFilterThreshold2 {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        Logger.trace("setRssiFilterThreshold");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else Logger.trace("setRssiFilterThreshold {}", stringNOTCONNECT);
        return false;
    }
    public long getRssiFilterCount() {
        Logger.trace("getRssiFilterCount");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterCount();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterCount();
        else Logger.trace("getRssiFilterCount {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setRssiFilterCount(long rssiFilterCount) {
        Logger.trace("setRssiFilterCount");
        return false;
    }
    public boolean getInvMatchEnable() {
        Logger.trace("getInvMatchEnable");
        if (isCs108Connected()) return cs108Library4A.getInvMatchEnable();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchEnable();
        else Logger.trace("getInvMatchEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean getInvMatchType() {
        Logger.trace("getInvMatchType");
        if (isCs108Connected()) return cs108Library4A.getInvMatchType();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchType();
        else Logger.trace("getInvMatchType {}", stringNOTCONNECT);
        return false;
    }
    public int getInvMatchOffset() {
        Logger.trace("getInvMatchOffset");
        if (isCs108Connected()) return cs108Library4A.getInvMatchOffset();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchOffset();
        else Logger.trace("getInvMatchOffset {}", stringNOTCONNECT);
        return -1;
    }
    public String getInvMatchData() {
        Logger.trace("getInvMatchData");
        if (isCs108Connected()) return cs108Library4A.getInvMatchData();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchData();
        else Logger.trace("getInvMatchData {}", stringNOTCONNECT);
        return null;
    }
    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        Logger.trace("setPostMatchCriteria");
        if (isCs108Connected()) return cs108Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else Logger.trace("setPostMatchCriteria {}", stringNOTCONNECT);
        return false;
    }
    public int mrfidToWriteSize() {
        Logger.trace("mrfidToWriteSize");
        if (isCs108Connected()) return cs108Library4A.mrfidToWriteSize();
        else if (isCs710Connected()) return cs710Library4A.mrfidToWriteSize();
        else Logger.trace("mrfidToWriteSize {}", stringNOTCONNECT);
        return -1;
    }
    public void mrfidToWritePrint() {
        Logger.trace("mrfidToWritePrint");
    }
    public long getTagRate() {
        Logger.trace("getTagRate");
        if (isCs108Connected()) return cs108Library4A.getTagRate();
        else if (isCs710Connected()) return cs710Library4A.getTagRate();
        else Logger.trace("getTagRate {}", stringNOTCONNECT);
        return -1;
    }
    public boolean startOperation(RfidReaderChipData.OperationTypes operationTypes) {
        Logger.trace("startOperation");
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
        else Logger.trace("startOperation {}", stringNOTCONNECT);
        return false;
    }
    public boolean abortOperation() {
        Logger.trace("abortOperation");
        if (isCs108Connected()) return cs108Library4A.abortOperation();
        else if (isCs710Connected()) return cs710Library4A.abortOperation();
        else Logger.trace("abortOperation {}", stringNOTCONNECT);
        return false;
    }
    public void restoreAfterTagSelect() {
        Logger.trace("restoreAfterTagSelect");
        if (isCs108Connected()) cs108Library4A.restoreAfterTagSelect();
        else if (isCs710Connected()) cs710Library4A.restoreAfterTagSelect();
        else Logger.trace("restoreAfterTagSelect {}", stringNOTCONNECT);
    }
    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        Logger.trace("csLibrary4A: setSelectCriteria setSelectedByTID strTagId = {}, pwrlevel = {}", strTagId, pwrlevel);
        if (isCs108Connected()) return cs108Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else Logger.trace("setSelectedTagByTID {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        Logger.trace("setSelectedTag 1");
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else Logger.trace("setSelectedTag 1 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSelectedTag(boolean selectOne, String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        Logger.trace("csLibraryA: setSelectCriteria strTagId = {}, selectBank = {}, selectOffset = {}, pwrlevel = {}, qValue = {}, matchRep = {}", selectOne, selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(selectOne, selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else Logger.trace("setSelectedTag 2 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setMatchRep(int matchRep) {
        Logger.trace("setMatchRep");
        if (isCs108Connected()) return cs108Library4A.setMatchRep(matchRep);
        else if (isCs710Connected()) return cs710Library4A.setMatchRep(matchRep);
        else Logger.trace("setMatchRep {}", stringNOTCONNECT);
        return false;
    }
    public String[] getCountryList() {
        Logger.trace("getCountryList");
        if (isCs108Connected()) return cs108Library4A.getCountryList();
        else if (isCs710Connected()) return cs710Library4A.getCountryList();
        else Logger.trace("getCountryList {}", stringNOTCONNECT);
        return null;
    }
    public int getCountryNumberInList() {
        Logger.trace("getCountryNumberInList");
        if (isCs108Connected()) return cs108Library4A.getCountryNumberInList();
        else if (isCs710Connected()) return cs710Library4A.getCountryNumberInList();
        else Logger.trace("getCountryNumberInList {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setCountryInList(int countryInList) {
        Logger.trace("setCountryInList");
        if (isCs108Connected()) return cs108Library4A.setCountryInList(countryInList);
        else if (isCs710Connected()) return cs710Library4A.setCountryInList(countryInList);
        else Logger.trace("setCountryInList {}", stringNOTCONNECT);
        return false;
    }
    public boolean getChannelHoppingStatus() {
        Logger.trace("getChannelHoppingStatus");
        if (isCs108Connected()) return cs108Library4A.getChannelHoppingStatus();
        else if (isCs710Connected()) return cs710Library4A.getChannelHoppingStatus();
        else Logger.trace("getChannelHoppingStatus {}", stringNOTCONNECT);
        return false;
    }
    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        Logger.trace("setChannelHoppingStatus");
        return false;
    }
    public String[] getChannelFrequencyList() {
        if (isCs108Connected()) return cs108Library4A.getChannelFrequencyList();
        else if (isCs710Connected()) return cs710Library4A.getChannelFrequencyList();
        else Logger.trace("getChannelFrequencyList {}", stringNOTCONNECT);
        return null;
    }
    public int getChannel() {
        Logger.trace("getChannel");
        if (isCs108Connected()) return cs108Library4A.getChannel();
        else if (isCs710Connected()) return cs710Library4A.getChannel();
        else Logger.trace("getChannel {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setChannel(int channelSelect) {
        Logger.trace("setChannel");
        if (isCs108Connected()) return cs108Library4A.setChannel(channelSelect);
        else if (isCs710Connected()) return cs710Library4A.setChannel(channelSelect);
        else Logger.trace("setChannel {}", stringNOTCONNECT);
        return false;
    }
    public byte getPopulation2Q(int population) {
        Logger.trace("getPopulation2Q");
        if (isCs108Connected()) return cs108Library4A.getPopulation2Q(population);
        else if (isCs710Connected()) return cs710Library4A.getPopulation2Q(population);
        else Logger.trace("getPopulation2Q {}", stringNOTCONNECT);
        return -1;
    }
    public int getPopulation() {
        Logger.trace("getPopulation");
        if (isCs108Connected()) return cs108Library4A.getPopulation();
        else if (isCs710Connected()) return cs710Library4A.getPopulation();
        else Logger.trace("getPopulation {}", stringNOTCONNECT);
        return -1;
    } //3348
    public boolean setPopulation(int population) {
        Logger.trace("setPopulation {}", population);
        if (isCs108Connected()) return cs108Library4A.setPopulation(population);
        else if (isCs710Connected()) return cs710Library4A.setPopulation(population);
        else Logger.trace("setPopulation {}", stringNOTCONNECT);
        return false;
    }
    public byte getQValue() {
        Logger.trace("getQValue");
        if (isCs108Connected()) return cs108Library4A.getQValue();
        else if (isCs710Connected()) return cs710Library4A.getQValue();
        else Logger.trace("getQValue {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setQValue(byte byteValue) {
        Logger.trace("setQValue");
        if (isCs108Connected()) return cs108Library4A.setQValue(byteValue);
        else if (isCs710Connected()) return cs710Library4A.setQValue(byteValue);
        else Logger.trace("setQValue {}", stringNOTCONNECT);
        return false;
    }
    public RfidReaderChipData.Rx000pkgData onRFIDEvent() {
        Logger.trace("onRFIDEvent");
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
                        Logger.trace("onRFIDEvent: responseType = {}", rx000pkgData1.responseType);
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
                        Logger.trace("onRFIDEvent: responseType = {}", rx000pkgData1.responseType);
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
            if (rx000pkgData != null) Logger.trace("response0 = {}, {}", rx000pkgData.responseType, byteArrayToString(rx000pkgData.dataValues));
            return rx000pkgData;
        } else Logger.trace("onRFIDEvent {}", stringNOTCONNECT);
        return null;
    }
    public String getModelNumber() {
        Logger.trace("getModelNumber");
        if (isCs108Connected()) return cs108Library4A.getModelNumber();
        else if (isCs710Connected()) return cs710Library4A.getModelNumber();
        else Logger.trace("getModelNumber {}", stringNOTCONNECT);
        return null;
    }
    public boolean setRx000KillPassword(String password) {
        Logger.trace("setRx000KillPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000KillPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000KillPassword(password);
        else Logger.trace("setRx000KillPassword {}", stringNOTCONNECT);
        return false;
    }
    public boolean setRx000AccessPassword(String password) {
        Logger.trace("setRx000AccessPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000AccessPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000AccessPassword(password);
        else Logger.trace("setRx000AccessPassword {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        Logger.trace("setAccessRetry");
        if (isCs108Connected()) return cs108Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else if (isCs710Connected()) return cs710Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else Logger.trace("setAccessRetry {}", stringNOTCONNECT);
        return false;
    }
    public boolean setInvModeCompact(boolean invModeCompact) {
        Logger.trace("setInvModeCompact");
        if (isCs108Connected()) return cs108Library4A.setInvModeCompact(invModeCompact);
        else if (isCs710Connected()) return cs710Library4A.setInvModeCompact(invModeCompact);
        else Logger.trace("setInvModeCompact {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        Logger.trace("setAccessLockAction");
        if (isCs108Connected()) return cs108Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else if (isCs710Connected()) return cs710Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else Logger.trace("setAccessLockAction {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessBank(int accessBank) {
        Logger.trace("setAccessBank 1");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank);
        else Logger.trace("setAccessBank 1 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessBank(int accessBank, int accessBank2) {
        Logger.trace("setAccessBank 2");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank, accessBank2);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank, accessBank2);
        else Logger.trace("setAccessBank 2 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessOffset(int accessOffset) {
        Logger.trace("setAccessOffset 1");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset);
        else Logger.trace("setAccessOffset 1 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        Logger.trace("setAccessOffset 2");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset, accessOffset2);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset, accessOffset2);
        else Logger.trace("setAccessOffset 2 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessCount(int accessCount) {
        Logger.trace("setAccessCount 1");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount);
        else Logger.trace("setAccessCount 1 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessCount(int accessCount, int accessCount2) {
        Logger.trace("setAccessCount 2");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount, accessCount2);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount, accessCount2);
        else Logger.trace("setAccessCount 2 {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAccessWriteData(String dataInput) {
        Logger.trace("setAccessWriteData");
        if (isCs108Connected()) return cs108Library4A.setAccessWriteData(dataInput);
        else if (isCs710Connected()) return cs710Library4A.setAccessWriteData(dataInput);
        else Logger.trace("setAccessWriteData {}", stringNOTCONNECT);
        return false;
    }
    public boolean setResReadNoReply(boolean resReadNoReply) {
        Logger.trace("setResReadNoReply");
        if (isCs108Connected()) return false;
        else if (isCs710Connected()) return cs710Library4A.setResReadNoReply(resReadNoReply);
        else Logger.trace("setResReadNoReply {}", stringNOTCONNECT);
        return false;
    }
    public boolean setTagRead(int tagRead) {
        Logger.trace("setTagRead");
        if (isCs108Connected()) return cs108Library4A.setTagRead(tagRead);
        else if (isCs710Connected()) return cs710Library4A.setTagRead(tagRead);
        else Logger.trace("setTagRead {}", stringNOTCONNECT);
        return false;
    }
    public boolean setInvBrandId(boolean invBrandId) {
        Logger.trace("setInvBrandId");
        if (isCs108Connected()) return cs108Library4A.setInvBrandId(invBrandId);
        else if (isCs710Connected()) return cs710Library4A.setInvBrandId(invBrandId);
        else Logger.trace("setInvBrandId {}", stringNOTCONNECT);
        return false;
    }
    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        Logger.trace("sendHostRegRequestHST_CMD with hostCommand = {}", hostCommand);
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
                    Logger.trace("Skip sendHostRegRequestHST_CMD: hostCommand =  {}", hostCommand);
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
                    Logger.trace("sendHostRegRequestHST_CMD: hostCommand =  {}", hostCommand);
                    break;
            }
            if (hostCommands1 == null) return false;
            return cs710Library4A.sendHostRegRequestHST_CMD(hostCommands1);
        }
        else Logger.trace("sendHostRegRequestHST_CMD {}", stringNOTCONNECT);
        return false;
    }
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        Logger.trace("setPwrManagementMode");
        if (isCs108Connected()) return cs108Library4A.setPwrManagementMode(bLowPowerStandby);
        else if (isCs710Connected()) return cs710Library4A.setPwrManagementMode(bLowPowerStandby);
        return false;
    }
    public void macWrite(int address, long value) {
        Logger.trace("macWrite");
        if (isCs108Connected()) cs108Library4A.macWrite(address, value);
        else if (isCs710Connected()) { }
    }
    public void set_fdCmdCfg(int value) {
        Logger.trace("set_fdCmdCfg");
        if (isCs108Connected()) cs108Library4A.set_fdCmdCfg(value);
        else if (isCs710Connected()) { }
    }
    public void set_fdRegAddr(int addr) {
        Logger.trace("set_fdRegAddr");
        if (isCs108Connected()) cs108Library4A.set_fdRegAddr(addr);
        else if (isCs710Connected()) { }
    }
    public void set_fdWrite(int addr, long value) {
        Logger.trace("set_fdWrite");
        if (isCs108Connected()) cs108Library4A.set_fdWrite(addr, value);
        else if (isCs710Connected()) { }
    }
    public void set_fdPwd(int value) {
        Logger.trace("set_fdPwd");
        if (isCs108Connected()) cs108Library4A.set_fdPwd(value);
        else if (isCs710Connected()) { }
    }
    public void set_fdBlockAddr4GetTemperature(int addr) {
        Logger.trace("set_fdBlockAddr4GetTemperature");
        if (isCs108Connected()) cs108Library4A.set_fdBlockAddr4GetTemperature(addr);
        else if (isCs710Connected()) { }
    }
    public void set_fdReadMem(int addr, long len) {
        Logger.trace("set_fdReadMem");
        if (isCs108Connected()) cs108Library4A.set_fdReadMem(addr, len);
        else if (isCs710Connected()) { }
    }
    public void set_fdWriteMem(int addr, int len, long value) {
        Logger.trace("set_fdWriteMem");
        if (isCs108Connected()) cs108Library4A.set_fdWriteMem(addr, len, value);
        else if (isCs710Connected()) { }
    }
    public void setImpinJExtension(boolean tagFocus, boolean fastId) {
        Logger.trace("setImpinJExtension with tagFocus = {}, fastId = {}", tagFocus, fastId);
        if (isCs108Connected()) cs108Library4A.setImpinJExtension(tagFocus, fastId);
        else if (isCs710Connected()) cs710Library4A.setImpinJExtension(tagFocus, fastId);
        else Logger.trace("setImpinJExtension {}", stringNOTCONNECT);
    }

    //============ Barcode ============
    public void getBarcodePreSuffix() {
        Logger.trace("getBarcodePreSuffix");
        if (isCs108Connected()) cs108Library4A.getBarcodePreSuffix();
        else if (isCs710Connected()) cs710Library4A.getBarcodePreSuffix();
        else Logger.trace("getBarcodePreSuffix {}", stringNOTCONNECT);
    }
    public void getBarcodeReadingMode() {
        Logger.trace("getBarcodeReadingMode");
        if (isCs108Connected()) cs108Library4A.getBarcodeReadingMode();
        else if (isCs710Connected()) cs710Library4A.getBarcodeReadingMode();
        else Logger.trace("getBarcodeReadingMode {}", stringNOTCONNECT);
    }
    public boolean isBarcodeFailure() {
        Logger.trace("isBarcodeFailure");
        if (isCs108Connected()) return cs108Library4A.isBarcodeFailure();
        else if (isCs710Connected()) return cs710Library4A.isBarcodeFailure();
        else Logger.trace("isBarcodeFailure {}", stringNOTCONNECT);
        return false;
    }
    public String getBarcodeDate() {
        Logger.trace("getBarcodeDate");
        if (isCs108Connected()) return cs108Library4A.getBarcodeDate();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeDate();
        else Logger.trace("getBarcodeDate {}", stringNOTCONNECT);
        return null;
    }
    public boolean getBarcodeOnStatus() {
        Logger.trace("getBarcodeOnStatus");
        if (isCs108Connected()) return cs108Library4A.getBarcodeOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeOnStatus();
        else Logger.trace("getBarcodeOnStatus {}", stringNOTCONNECT);
        return false;
    }
    public boolean setBarcodeOn(boolean on) {
        Logger.trace("setBarcodeOn");
        if (isCs108Connected()) return cs108Library4A.setBarcodeOn(on);
        else if (isCs710Connected()) return cs710Library4A.setBarcodeOn(on);
        else Logger.trace("setBarcodeOn {}", stringNOTCONNECT);
        return false;
    }
    public boolean setVibrateOn(int mode) {
        Logger.trace("setVibrateOn with mode = {}", mode);
        if (isCs108Connected()) return cs108Library4A.setVibrateOn(mode);
        else if (isCs710Connected()) return cs710Library4A.setVibrateOn(mode);
        else Logger.trace("setVibrateOn {}", stringNOTCONNECT);
        return false;
    }
    public boolean getInventoryVibrate() {
        Logger.trace("getInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.getInventoryVibrate();
        else if (isCs710Connected()) return cs710Library4A.getInventoryVibrate();
        else Logger.trace("getInventoryVibrate {}", stringNOTCONNECT);
        return false;
    }
    public boolean setInventoryVibrate(boolean inventoryVibrate) {
        Logger.trace("setInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.setInventoryVibrate(inventoryVibrate);
        else if (isCs710Connected()) return cs710Library4A.setInventoryVibrate(inventoryVibrate);
        else Logger.trace("setInventoryVibrate {}", stringNOTCONNECT);
        return false;
    }
    public int getVibrateTime() {
        Logger.trace("getVibrateTime");
        if (isCs108Connected()) return cs108Library4A.getVibrateTime();
        else if (isCs710Connected()) return cs710Library4A.getVibrateTime();
        else Logger.trace("getVibrateTime {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateTime(int vibrateTime) {
        Logger.trace("setVibrateTime");
        if (isCs108Connected()) return cs108Library4A.setVibrateTime(vibrateTime);
        else if (isCs710Connected()) return cs710Library4A.setVibrateTime(vibrateTime);
        else Logger.trace("setVibrateTime {}", stringNOTCONNECT);
        return false;
    }
    public int getVibrateWindow() {
        Logger.trace("getVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.getVibrateWindow();
        else if (isCs710Connected()) return cs710Library4A.getVibrateWindow();
        else Logger.trace("getVibrateWindow {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateWindow(int vibrateWindow) {
        Logger.trace("setVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.setVibrateWindow(vibrateWindow);
        else if (isCs710Connected()) return cs710Library4A.setVibrateWindow(vibrateWindow);
        else Logger.trace("setVibrateWindow {}", stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandTrigger() {
        Logger.trace("barcodeSendCommandTrigger");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandTrigger();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandTrigger();
        else Logger.trace("barcodeSendCommandTrigger {}", stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandSetPreSuffix() {
        Logger.trace("barcodeSendCommandSetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandSetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandSetPreSuffix();
        else Logger.trace("barcodeSendCommandSetPreSuffix {}", stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandResetPreSuffix() {
        Logger.trace("barcodeSendCommandResetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandResetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandResetPreSuffix();
        else Logger.trace("barcodeSendCommandResetPreSuffix {}", stringNOTCONNECT);
        return false;
    }
    public boolean barcodeSendCommandConinuous() {
        Logger.trace("barcodeSendCommandConinuous");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandConinuous();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandConinuous();
        else Logger.trace("barcodeSendCommandConinuous {}", stringNOTCONNECT);
        return false;
    }
    public String getBarcodeVersion() {
        Logger.trace("getBarcodeVersion");
        if (isCs108Connected()) return cs108Library4A.getBarcodeVersion();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeVersion();
        else Logger.trace("getBarcodeVersion {}", stringNOTCONNECT);
        return null;
    }
    public String getBarcodeSerial() {
        Logger.trace("getBarcodeSerial");
        if (isCs108Connected()) return cs108Library4A.getBarcodeSerial();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeSerial();
        else Logger.trace("getBarcodeSerial {}", stringNOTCONNECT);
        return null;
    }
    public boolean barcodeInventory(boolean start) {
        Logger.trace("barcodeInventory");
        if (isCs108Connected()) return cs108Library4A.barcodeInventory(start);
        else if (isCs710Connected()) return cs710Library4A.barcodeInventory(start);
        else Logger.trace("barcodeInventory {}", stringNOTCONNECT);
        return false;
    }
    public byte[] onBarcodeEvent() {
        Logger.trace("onBarcodeEvent");
        if (isCs108Connected()) return cs108Library4A.onBarcodeEvent();
        else if (isCs710Connected()) return cs710Library4A.onBarcodeEvent();
        else Logger.trace("onBarcodeEvent {}", stringNOTCONNECT);
        return null;
    }

    //============ Android General ============
    public void setSameCheck(boolean sameCheck1) {
        Logger.trace("setSameCheck");
        if (isCs108Connected()) cs108Library4A.setSameCheck(sameCheck1);
        else if (isCs710Connected()) cs710Library4A.setSameCheck(sameCheck1);
        else Logger.trace("setSameCheck {}", stringNOTCONNECT);
    }
    public void saveSetting2File() {
        Logger.trace("saveSetting2File");
        if (isCs108Connected()) cs108Library4A.saveSetting2File();
        else if (isCs710Connected()) cs710Library4A.saveSetting2File();
        else Logger.trace("saveSetting2File {}", stringNOTCONNECT);
    }
    public int getBeepCount() {
        Logger.trace("getBeepCount");
        if (isCs108Connected()) return cs108Library4A.getBeepCount();
        else if (isCs710Connected()) return cs710Library4A.getBeepCount();
        else Logger.trace("getBeepCount {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setBeepCount(int beepCount) {
        Logger.trace("setBeepCount");
        if (isCs108Connected()) return cs108Library4A.setBeepCount(beepCount);
        else if (isCs710Connected()) return cs710Library4A.setBeepCount(beepCount);
        else Logger.trace("setBeepCount {}", stringNOTCONNECT);
        return false;
    }
    public boolean getInventoryBeep() {
        Logger.trace("getInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.getInventoryBeep();
        else if (isCs710Connected()) return cs710Library4A.getInventoryBeep();
        else Logger.trace("getInventoryBeep {}", stringNOTCONNECT);
        return false;
    }
    public boolean setInventoryBeep(boolean inventoryBeep) {
        Logger.trace("setInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.setInventoryBeep(inventoryBeep);
        else if (isCs710Connected()) return cs710Library4A.setInventoryBeep(inventoryBeep);
        else Logger.trace("setInventoryBeep {}", stringNOTCONNECT);
        return false;
    }
    public boolean getSaveFileEnable() {
        Logger.trace("getSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveFileEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveFileEnable();
        else Logger.trace("getSaveFileEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSaveFileEnable(boolean saveFileEnable) {
        Logger.trace("setSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveFileEnable(saveFileEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveFileEnable(saveFileEnable);
        else Logger.trace("setSaveFileEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean getSaveCloudEnable() {
        Logger.trace("getSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveCloudEnable();
        else Logger.trace("getSaveCloudEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSaveCloudEnable(boolean saveCloudEnable) {
        Logger.trace("setSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveCloudEnable(saveCloudEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveCloudEnable(saveCloudEnable);
        else Logger.trace("setSaveCloudEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean getSaveNewCloudEnable() {
        Logger.trace("getSaveNewCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveNewCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveNewCloudEnable();
        else Logger.trace("getSaveNewCloudEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSaveNewCloudEnable(boolean saveNewCloudEnable) {
        Logger.trace("setSaveNewCloudEnable");
        return false;
    }
    public boolean getSaveAllCloudEnable() {
        Logger.trace("getSaveAllCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveAllCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveAllCloudEnable();
        else Logger.trace("getSaveAllCloudEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setSaveAllCloudEnable(boolean saveAllCloudEnable) {
        Logger.trace("setSaveAllCloudEnable");
        return false;
    }
    public boolean getUserDebugEnable() {
        Logger.trace("getUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.getUserDebugEnable();
        else if (isCs710Connected()) return cs710Library4A.getUserDebugEnable();
        else Logger.trace("getUserDebugEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setUserDebugEnable(boolean userDebugEnable) {
        Logger.trace("setUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.setUserDebugEnable(userDebugEnable);
        else if (isCs710Connected()) return cs710Library4A.setUserDebugEnable(userDebugEnable);
        else Logger.trace("getUserDebugEnable {}", stringNOTCONNECT);
        return false;
    }
    public String getForegroundReader() {
        String string108 = cs108Library4A.getForegroundReader().trim();
        String string710 = cs710Library4A.getForegroundReader().trim();
        Logger.trace("foregroundReader108 = {}, foregroundReader710 = {}", string108, string710);
        if (isCs108Connected()) return string108;
        return string710;
    }
    public boolean getForegroundServiceEnable() {
        Logger.trace("getForegroundEnable");
        if (isCs108Connected()) return cs108Library4A.getForegroundServiceEnable();
        else if (isCs710Connected()) return cs710Library4A.getForegroundServiceEnable();
        else Logger.trace("getForegroundEnable {}", stringNOTCONNECT);
        return false;
    }
    public boolean setForegroundServiceEnable(boolean forgroundServiceEnable) {
        Logger.trace("setForegroundServiceEnable");
        if (isCs108Connected()) return cs108Library4A.setForegroundServiceEnable(forgroundServiceEnable);
        else if (isCs710Connected()) return cs710Library4A.setForegroundServiceEnable(forgroundServiceEnable);
        else Logger.trace("setForegroundServiceEnable {}", stringNOTCONNECT);
        return false;
    }
    public String getServerLocation() {
        Logger.trace("getServerLocation");
        if (isCs108Connected()) return cs108Library4A.getServerLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerLocation();
        else Logger.trace("getServerLocation {}", stringNOTCONNECT);
        return null;
    }
    public boolean setServerLocation(String serverLocation) {
        Logger.trace("setServerLocation");
        if (isCs108Connected()) return cs108Library4A.setServerLocation(serverLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerLocation(serverLocation);
        else Logger.trace("setServerLocation {}", stringNOTCONNECT);
        return false;
    }
    public int getServerTimeout() {
        Logger.trace("getServerTimeout");
        if (isCs108Connected()) return cs108Library4A.getServerTimeout();
        else if (isCs710Connected()) return cs710Library4A.getServerTimeout();
        else Logger.trace("getServerTimeout {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setServerTimeout(int serverTimeout) {
        Logger.trace("setServerTimeout");
        if (isCs108Connected()) return cs108Library4A.setServerTimeout(serverTimeout);
        else if (isCs710Connected()) return cs710Library4A.setServerTimeout(serverTimeout);
        else Logger.trace("setServerTimeout {}", stringNOTCONNECT);
        return false;
    }
    public String getServerMqttLocation() {
        Logger.trace("getServerMqttLocation");
        if (isCs108Connected()) return cs108Library4A.getServerMqttLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerMqttLocation();
        else Logger.trace("getServerMqttLocation {}", stringNOTCONNECT);
        return null;
    }
    public boolean setServerMqttLocation(String serverLocation) {
        Logger.trace("setServerMqttLocation");
        if (isCs108Connected()) return cs108Library4A.setServerMqttLocation(serverLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerMqttLocation(serverLocation);
        else Logger.trace("setServerMqttLocation {}", stringNOTCONNECT);
        return false;
    }
    public String getTopicMqtt() {
        Logger.trace("getServerTopicMqtt");
        if (isCs108Connected()) return cs108Library4A.getTopicMqtt();
        else if (isCs710Connected()) return cs710Library4A.getTopicMqtt();
        else Logger.trace("getServerTopicMqtt {}", stringNOTCONNECT);
        return null;
    }
    public boolean setTopicMqtt(String topic) {
        Logger.trace("setServerTopicMqtt");
        if (isCs108Connected()) return cs108Library4A.setTopicMqtt(topic);
        else if (isCs710Connected()) return cs710Library4A.setTopicMqtt(topic);
        else Logger.trace("setServerTopicMqtt {}", stringNOTCONNECT);
        return false;
    }
    public int getForegroundDupElim() {
        Logger.trace("getForegroundDupElim");
        if (isCs108Connected()) return cs108Library4A.getForegroundDupElim();
        else if (isCs710Connected()) return cs710Library4A.getForegroundDupElim();
        else Logger.trace("getForegroundDupElim {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setForegroundDupElim(int iForegroundDupElim) {
        Logger.trace("setForegroundDupElim");
        if (isCs108Connected()) return cs108Library4A.setForegroundDupElim(iForegroundDupElim);
        else if (isCs710Connected()) return cs710Library4A.setForegroundDupElim(iForegroundDupElim);
        else Logger.trace("setForegroundDupElim {}", stringNOTCONNECT);
        return false;
    }
    public int getInventoryCloudSave() {
        Logger.trace("getInventoryCloudSave");
        if (isCs108Connected()) return cs108Library4A.getInventoryCloudSave();
        else if (isCs710Connected()) return cs710Library4A.getInventoryCloudSave();
        else Logger.trace("getInventoryCloudSave {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setInventoryCloudSave(int inventoryCloudSave) {
        Logger.trace("setInventoryCloudSave");
        if (isCs108Connected()) return cs108Library4A.setInventoryCloudSave(inventoryCloudSave);
        else if (isCs710Connected()) return cs710Library4A.setInventoryCloudSave(inventoryCloudSave);
        else Logger.trace("setInventoryCloudSave {}", stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjLocation() {
        Logger.trace("getServerImpinjLocation");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjLocation();
        else Logger.trace("getServerImpinjLocation {}", stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjLocation(String serverImpinjLocation) {
        Logger.trace("setServerImpinjLocation");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjLocation(serverImpinjLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjLocation(serverImpinjLocation);
        else Logger.trace("setServerImpinjLocation {}", stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjName() {
        Logger.trace("getServerImpinjName");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjName();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjName();
        else Logger.trace("getServerImpinjName {}", stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjName(String serverImpinjName) {
        Logger.trace("setServerImpinjName");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjName(serverImpinjName);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjName(serverImpinjName);
        else Logger.trace("setServerImpinjName {}", stringNOTCONNECT);
        return false;
    }
    public String getServerImpinjPassword() {
        Logger.trace("getServerImpinjPassword");
        if (isCs108Connected()) return cs108Library4A.getServerImpinjPassword();
        else if (isCs710Connected()) return cs710Library4A.getServerImpinjPassword();
        else Logger.trace("getServerImpinjPassword {}", stringNOTCONNECT);
        return null;
    }
    public boolean setServerImpinjPassword(String serverImpinjPassword) {
        Logger.trace("setServerImpinjPassword");
        if (isCs108Connected()) return cs108Library4A.setServerImpinjPassword(serverImpinjPassword);
        else if (isCs710Connected()) return cs710Library4A.setServerImpinjPassword(serverImpinjPassword);
        else Logger.trace("setServerImpinjPassword {}", stringNOTCONNECT);
        return false;
    }

    public int getBatteryDisplaySetting() {
        Logger.trace("getBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplaySetting();
        else Logger.trace("getBatteryDisplaySetting {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        Logger.trace("setBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else Logger.trace("setBatteryDisplaySetting {}", stringNOTCONNECT);
        return false;
    }
    public double dBuV_dBm_constant = RfidReader.dBuV_dBm_constant; //106.98;
    public int getRssiDisplaySetting() {
        Logger.trace("getRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getRssiDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getRssiDisplaySetting();
        return 0;
    }
    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        Logger.trace("setRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else Logger.trace("setRssiDisplaySetting {}", stringNOTCONNECT);
        return false;
    }
    public int getVibrateModeSetting() {
        Logger.trace("getVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.getVibrateModeSetting();
        else if (isCs710Connected()) return cs710Library4A.getVibrateModeSetting();
        else Logger.trace("getVibrateModeSetting {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setVibrateModeSetting(int vibrateModeSelect) {
        Logger.trace("setVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.setVibrateModeSetting(vibrateModeSelect);
        else if (isCs710Connected()) return cs710Library4A.setVibrateModeSetting(vibrateModeSelect);
        else Logger.trace("setVibrateModeSetting {}", stringNOTCONNECT);
        return false;
    }
    public int getSavingFormatSetting() {
        Logger.trace("getSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.getSavingFormatSetting();
        else if (isCs710Connected()) return cs710Library4A.getSavingFormatSetting();
        else Logger.trace("getSavingFormatSetting {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setSavingFormatSetting(int savingFormatSelect) {
        Logger.trace("setSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.setSavingFormatSetting(savingFormatSelect);
        else if (isCs710Connected()) return cs710Library4A.setSavingFormatSetting(savingFormatSelect);
        else Logger.trace("setSavingFormatSetting {}", stringNOTCONNECT);
        return false;
    }
    public int getCsvColumnSelectSetting() {
        Logger.trace("getCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.getCsvColumnSelectSetting();
        else if (isCs710Connected()) return cs710Library4A.getCsvColumnSelectSetting();
        else Logger.trace("getCsvColumnSelectSetting {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setCsvColumnSelectSetting(int csvColumnSelect) {
        Logger.trace("setCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else if (isCs710Connected()) return cs710Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else Logger.trace("setCsvColumnSelectSetting {}", stringNOTCONNECT);
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
        Logger.trace("getBluetoothICFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareVersion();
        else Logger.trace("getBluetoothICFirmwareVersion {}", stringNOTCONNECT);
        return null;
    }
    public String getBluetoothICFirmwareName() {
        Logger.trace("getBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareName();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareName();
        else Logger.trace("getBluetoothICFirmwareName {}", stringNOTCONNECT);
        return null;
    }
    public boolean setBluetoothICFirmwareName(String name) {
        Logger.trace("setBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.setBluetoothICFirmwareName(name);
        else if (isCs710Connected()) return cs710Library4A.setBluetoothICFirmwareName(name);
        else Logger.trace("setBluetoothICFirmwareName {}", stringNOTCONNECT);
        return false;
    }

    //============ Controller ============
    public String hostProcessorICGetFirmwareVersion() {
        Logger.trace("hostProcessorICGetFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.hostProcessorICGetFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.hostProcessorICGetFirmwareVersion();
        else Logger.trace("hostProcessorICGetFirmwareVersion {}", stringNOTCONNECT);
        return null;
    }
    public String getHostProcessorICSerialNumber() {
        Logger.trace("getHostProcessorICSerialNumber");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICSerialNumber();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICSerialNumber();
        else Logger.trace("getHostProcessorICSerialNumber {}", stringNOTCONNECT);
        return null;
    }
    public String getHostProcessorICBoardVersion() {
        Logger.trace("getHostProcessorICBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICBoardVersion();
        else Logger.trace("getHostProcessorICBoardVersion {}", stringNOTCONNECT);
        return null;
    }

    //============ Controller notification ============
    public int getBatteryLevel() {
        Logger.trace("getBatteryLevel");
        if (isCs108Connected()) return cs108Library4A.getBatteryLevel();
        else if (isCs710Connected()) return cs710Library4A.getBatteryLevel();
        else Logger.trace("getBatteryLevel {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setAutoTriggerReporting(byte timeSecond) {
        Logger.trace("setAutoTriggerReporting");
        if (isCs108Connected()) return cs108Library4A.setAutoTriggerReporting(timeSecond);
        else if (isCs710Connected()) return cs710Library4A.setAutoTriggerReporting(timeSecond);
        else Logger.trace("setAutoTriggerReporting {}", stringNOTCONNECT);
        return false;
    }
    public boolean getAutoBarStartSTop() {
        Logger.trace("getAutoBarStartSTop");
        if (isCs108Connected()) return cs108Library4A.getAutoBarStartSTop();
        else if (isCs710Connected()) return cs710Library4A.getAutoBarStartSTop();
        else Logger.trace("getAutoBarStartSTop {}", stringNOTCONNECT);
        return false;
    }
    public boolean batteryLevelRequest() {
        Logger.trace("batteryLevelRequest");
        if (isCs108Connected()) return cs108Library4A.batteryLevelRequest();
        else if (isCs710Connected()) return cs710Library4A.batteryLevelRequest();
        else Logger.trace("batteryLevelRequest {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAutoBarStartSTop(boolean enable) {
        Logger.trace("setAutoBarStartSTop");
        if (isCs108Connected()) return cs108Library4A.setAutoBarStartSTop(enable);
        else if (isCs710Connected()) return cs710Library4A.setAutoBarStartSTop(enable);
        else Logger.trace("setAutoBarStartSTop {}", stringNOTCONNECT);
        return false;
    }
    public boolean getTriggerReporting() {
        Logger.trace("getTriggerReporting");
        if (isCs108Connected()) cs108Library4A.getTriggerReporting();
        else if (isCs710Connected()) cs710Library4A.getTriggerReporting();
        else Logger.trace("getTriggerReporting {}", stringNOTCONNECT);
        return false;
    }
    public boolean setTriggerReporting(boolean triggerReporting) {
        Logger.trace("setTriggerReporting");
        if (isCs108Connected()) return cs108Library4A.setTriggerReporting(triggerReporting);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReporting(triggerReporting);
        else Logger.trace("setTriggerReporting {}", stringNOTCONNECT);
        return false;
    }
    public int iNO_SUCH_SETTING = -1;
    public short getTriggerReportingCount() {
        Logger.trace("getTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerReportingCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerReportingCount();
        else Logger.trace("getTriggerReportingCount {}", stringNOTCONNECT);
        return 5;
    }
    public boolean setTriggerReportingCount(short triggerReportingCount) {
        Logger.trace("setTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.setTriggerReportingCount(triggerReportingCount);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReportingCount(triggerReportingCount);
        else Logger.trace("setTriggerReportingCount {}", stringNOTCONNECT);
        return false;
    }
    public String getBatteryDisplay(boolean voltageDisplay) {
        Logger.trace("getBatteryDisplay");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplay(voltageDisplay);
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplay(voltageDisplay);
        else Logger.trace("getBatteryDisplay is called befoe connection !!!");
        return null;
    }
    String stringNOTCONNECT;
    public String isBatteryLow() {
        Logger.trace("isBatteryLow");
        if (isCs108Connected()) return cs108Library4A.isBatteryLow();
        else if (isCs710Connected()) return cs710Library4A.isBatteryLow();
        else Logger.trace("isBatteryLow {}", stringNOTCONNECT);
        return null;
    }
    public int getBatteryCount() {
        Logger.trace("getBatteryCount");
        if (isCs108Connected()) return cs108Library4A.getBatteryCount();
        else if (isCs710Connected()) return cs710Library4A.getBatteryCount();
        else Logger.trace("getBatteryCount {}", stringNOTCONNECT);
        return -1;
    }
    public boolean getTriggerButtonStatus() {
        Logger.trace("getTriggerButtonStatus");
        if (isCs108Connected()) return cs108Library4A.getTriggerButtonStatus();
        else if (isCs710Connected()) return cs710Library4A.getTriggerButtonStatus();
        else Logger.trace("getTriggerButtonStatus {}", stringNOTCONNECT);
        return false;
    }
    public int getTriggerCount() {
        Logger.trace("getTriggerCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerCount();
        else Logger.trace("getTriggerCount {}", stringNOTCONNECT);
        return -1;
    }
    //public interface NotificationListener { void onChange(); }
    public void setNotificationListener(NotificationConnector.NotificationListener listener) {
        Logger.trace("setNotificationListener");
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
        else Logger.trace("setNotificationListener {}", stringNOTCONNECT);
    }
    public byte[] onNotificationEvent() {
        Logger.trace("onNotificationEvent");
        if (isCs108Connected()) return cs108Library4A.onNotificationEvent();
        else if (isCs710Connected()) return cs710Library4A.onNotificationEvent();
        else Logger.trace("onNotificationEvent {}", stringNOTCONNECT);
        return null;
    }

    //============ to be modified ============
    public String getSerialNumber() {
        Logger.trace("getSerialNumber");
        if (isCs108Connected()) return cs108Library4A.getSerialNumber();
        else if (isCs710Connected()) return cs710Library4A.getSerialNumber();
        else Logger.trace("getSerialNumber {}", stringNOTCONNECT);
        return null;
    }
    public boolean setRfidOn(boolean onStatus) {
        Logger.trace("setRfidOn");
        if (isCs108Connected()) return cs108Library4A.setRfidOn(onStatus);
        else if (isCs710Connected()) return cs710Library4A.setRfidOn(onStatus);
        else Logger.trace("setRfidOn {}", stringNOTCONNECT);
        return false;
    }
    public int getcsModel() {
        Logger.trace("getcsModel");
        if (isCs108Connected()) return cs108Library4A.getcsModel();
        else if (isCs710Connected()) return cs710Library4A.getcsModel();
        else Logger.trace("getcsModel {}", stringNOTCONNECT);
        return -1;
    }
    public int getAntennaCycle() {
        Logger.trace("getAntennaCycle");
        if (isCs108Connected()) return cs108Library4A.getAntennaCycle();
        else if (isCs710Connected()) return cs710Library4A.getAntennaCycle();
        else Logger.trace("getAntennaCycle {}", stringNOTCONNECT);
        return -1;
    }
    public boolean setAntennaCycle(int antennaCycle) {
        Logger.trace("setAntennaCycle");
        if (isCs108Connected()) return cs108Library4A.setAntennaCycle(antennaCycle);
        else if (isCs710Connected()) return cs710Library4A.setAntennaCycle(antennaCycle);
        else Logger.trace("setAntennaCycle {}", stringNOTCONNECT);
        return false;
    }
    public boolean setAntennaInvCount(long antennaInvCount) {
        Logger.trace("setAntennaInvCount");
        if (isCs108Connected()) return cs108Library4A.setAntennaInvCount(antennaInvCount);
        else if (isCs710Connected()) return cs710Library4A.setAntennaInvCount(antennaInvCount);
        else Logger.trace("setAntennaInvCount {}", stringNOTCONNECT);
        return false;
    }
    public void clearInvalidata() {
        Logger.trace("clearInvalidata");
        if (isCs108Connected()) cs108Library4A.clearInvalidata();
        else if (isCs710Connected()) cs710Library4A.clearInvalidata();
        else Logger.trace("clearInvalidata {}", stringNOTCONNECT);
    }
    public int getInvalidata() {
        Logger.trace("getInvalidata");
        if (isCs108Connected()) return cs108Library4A.getInvalidata();
        else if (isCs710Connected()) return cs710Library4A.getInvalidata();
        else Logger.trace("getInvalidata {}", stringNOTCONNECT);
        return -1;
    }
    public int getInvalidUpdata() {
        Logger.trace("getInvalidUpdata");
        if (isCs108Connected()) return cs108Library4A.getInvalidUpdata();
        else if (isCs710Connected()) return cs710Library4A.getInvalidUpdata();
        else Logger.trace("getInvalidUpdata {}", stringNOTCONNECT);
        return -1;
    }
    public int getValidata() {
        Logger.trace("getValidata");
        if (isCs108Connected()) return cs108Library4A.getValidata();
        else if (isCs710Connected()) return cs710Library4A.getValidata();
        else Logger.trace("getValidata {}", stringNOTCONNECT);
        return -1;
    }

    //============ not public ============
    int bConnectStatus = 0;
    int iServiceUuidConnectedBefore = -1;
    private boolean isCs108Connected() { return (bConnectStatus == 1); }
    private boolean isCs710Connected() { return (bConnectStatus == 7); }

    public int setSelectData(RfidReader.TagType tagType, String mDid, boolean bNeedSelectedTagByTID, String stringProtectPassword, int selectFor, int selectHold) {
        Logger.trace("setSelectData");
        if (isCs108Connected()) return cs108Library4A.setSelectData(tagType, mDid, bNeedSelectedTagByTID, stringProtectPassword, selectFor, selectHold);
        else if (isCs710Connected()) return cs710Library4A.setSelectData(tagType, mDid, bNeedSelectedTagByTID, stringProtectPassword, selectFor, selectHold);
        else Logger.trace("setSelectData {}", stringNOTCONNECT);
        return -1;
    }
}
