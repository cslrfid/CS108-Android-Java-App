package com.csl.cslibrary4a;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class RfidReaderChipR2000 {
    boolean DEBUG_PKDATA, DEBUG;
    boolean sameCheck = true;
    //RfidReaderChip mRfidReaderChip;
    boolean DEBUGTHREAD = false;
    int intervalRx000UplinkHandler;
    public int invalidUpdata; //invalidata, invalidUpdata, validata;
    boolean aborting = false;
    Context context; Utility utility; CsReaderConnector csReaderConnector;
    public RfidReaderChipR2000(Context context, Utility utility, CsReaderConnector csReaderConnector) {
        this.context = context;
        this.utility = utility;
        appendToLog("csReaderConnector 1 is " + (csReaderConnector == null ? "null" : "valid"));
        this.csReaderConnector = csReaderConnector;
        this.DEBUGTHREAD = csReaderConnector.DEBUGTHREAD;
        this.intervalRx000UplinkHandler = csReaderConnector.intervalRx000UplinkHandler;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }
    void writeDebug2File(String stringDebug) { utility.writeDebug2File(stringDebug); }
    public enum ControlCommands {
        NULL,
        CANCEL, SOFTRESET, ABORT, PAUSE, RESUME, GETSERIALNUMBER, RESETTOBOOTLOADER
    }
    enum HostRegRequests {
        MAC_OPERATION,
        //MAC_VER, MAC_LAST_COMMAND_DURATION,
        //HST_CMNDIAGS,
        //HST_MBP_ADDR, HST_MBP_DATA,
        //HST_OEM_ADDR, HST_OEM_DATA,
        HST_ANT_CYCLES, HST_ANT_DESC_SEL, HST_ANT_DESC_CFG, MAC_ANT_DESC_STAT, HST_ANT_DESC_PORTDEF, HST_ANT_DESC_DWELL, HST_ANT_DESC_RFPOWER, HST_ANT_DESC_INV_CNT,
        HST_TAGMSK_DESC_SEL, HST_TAGMSK_DESC_CFG, HST_TAGMSK_BANK, HST_TAGMSK_PTR, HST_TAGMSK_LEN, HST_TAGMSK_0_3,
        HST_QUERY_CFG, HST_INV_CFG, HST_INV_SEL, HST_INV_ALG_PARM_0, HST_INV_ALG_PARM_1, HST_INV_ALG_PARM_2, HST_INV_ALG_PARM_3, HST_INV_RSSI_FILTERING_CONFIG, HST_INV_RSSI_FILTERING_THRESHOLD, HST_INV_RSSI_FILTERING_COUNT, HST_INV_EPC_MATCH_CFG, HST_INV_EPCDAT_0_3,
        HST_TAGACC_DESC_CFG, HST_TAGACC_BANK, HST_TAGACC_PTR, HST_TAGACC_CNT, HST_TAGACC_LOCKCFG, HST_TAGACC_ACCPWD, HST_TAGACC_KILLPWD, HST_TAGWRDAT_SEL, HST_TAGWRDAT_0,
        HST_RFTC_CURRENT_PROFILE,
        HST_RFTC_FRQCH_SEL, HST_RFTC_FRQCH_CFG, HST_RFTC_FRQCH_DESC_PLLDIVMULT, HST_RFTC_FRQCH_DESC_PLLDACCTL, HST_RFTC_FRQCH_CMDSTART,
        HST_AUTHENTICATE_CFG, HST_AUTHENTICATE_MSG, HST_READBUFFER_LEN, HST_UNTRACEABLE_CFG,
        HST_CMD
    }
    public class Rx000Setting {
        Rx000Setting(boolean set_default_setting) {
            if (set_default_setting) {
                macVer = mDefault.macVer;
                //diagnosticCfg = mDefault.diagnosticCfg;
                oemAddress = mDefault.oemAddress;

                //RFTC block paramters
                currentProfile = mDefault.currentProfile;

                // Antenna block parameters
                antennaCycle = mDefault.antennaCycle;
                antennaFreqAgile = mDefault.antennaFreqAgile;
                antennaSelect = mDefault.antennaSelect;
            }
            antennaSelectedData = new AntennaSelectedData[ANTSELECT_MAX + 1];
            for (int i = 0; i < antennaSelectedData.length; i++) {
                int default_setting_type = 0;
                if (set_default_setting) {
                    if (i == 0) default_setting_type = 1;
                    else if (i >= 1 && i <= 3)  default_setting_type = 2;
                    else if (i >= 4 && i <= 7)  default_setting_type = 3;
                    else if (i >= 8 && i <= 11) default_setting_type = 4;
                    else    default_setting_type = 5;
                }
                antennaSelectedData[i] = new AntennaSelectedData(set_default_setting, default_setting_type);
            }

            //Tag select block parameters
            if (set_default_setting)    invSelectIndex = 0;
            invSelectData = new InvSelectData[INVSELECT_MAX + 1];
            for (int i = 0; i < invSelectData.length; i++) {
                invSelectData[i] = new InvSelectData(set_default_setting);
            }

            if (set_default_setting) {
                //Inventtory block paraameters
                //queryTarget = mDefault.queryTarget;
                //querySession = mDefault.querySession;
                //querySelect = mDefault.querySelect;
                //appendToLog("BtDataOut: RfidReaderChipR2000.Rx000Setting new querySelect = " + querySelect);
                //invAlgo = mDefault.invAlgo;
                matchRep = mDefault.matchRep;
                tagSelect = mDefault.tagSelect;
                noInventory = mDefault.noInventory;
                tagDelay = mDefault.tagDelay;
                invModeCompact = mDefault.tagJoin;
                invBrandId = mDefault.brandid;
                invAuthenticate = mDefault.invAuthenticate;
            }

            if (set_default_setting)    algoSelect = 3;
            algoSelectedData = new AlgoSelectedData[ALGOSELECT_MAX + 1];
            for (int i = 0; i < algoSelectedData.length; i++) {//0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
                int default_setting_type = 0;
                if (set_default_setting) {
                    default_setting_type = i + 1;
                }
                algoSelectedData[i] = new AlgoSelectedData(set_default_setting, default_setting_type);
            }

            if (set_default_setting) {
                rssiFilterType = mDefault.rssiFilterType;
                rssiFilterOption = mDefault.rssiFilterOption;
                rssiFilterThreshold1 = mDefault.rssiFilterThreshold;
                rssiFilterThreshold2 = mDefault.rssiFilterThreshold;
                rssiFilterCount = mDefault.rssiFilterCount;

                matchEnable = mDefault.matchEnable;
                matchType = mDefault.matchType;
                matchLength = mDefault.matchLength;
                matchOffset = mDefault.matchOffset;
                invMatchDataReady = mDefault.invMatchDataReady;

                //Tag access block parameters
                accessRetry = mDefault.accessRetry;
                accessBank = mDefault.accessBank; accessBank2 = mDefault.accessBank2;
                accessOffset = mDefault.accessOffset; accessOffset2 = mDefault.accessOffset2;
                //accessCount = mDefault.accessCount; accessCount2 = mDefault.accessCount2;
                accessLockAction = mDefault.accessLockAction;
                accessLockMask = mDefault.accessLockMask;
                //long accessPassword = 0;
                //long killPassword = 0;
                accessWriteDataSelect = mDefault.accessWriteDataSelect;
                accWriteDataReady = mDefault.accWriteDataReady;

                authMatchDataReady = mDefault.authMatchDataReady;
            }

            invMatchData0_63 = new byte[4 * 16];
            accWriteData0_63 = new byte[4 * 16 * 2];
            authMatchData0_63 = new byte[4 * 4];
        }

        class Rx000Setting_default {
            String macVer;
            int diagnosticCfg = 0x210;
            int mbpAddress = 0; // ?
            int mbpData = 0; // ?
            int oemAddress = 4; // ?
            int oemData = 0; // ?

            //RFTC block paramters
            int currentProfile = 1;
            int freqChannelSelect = 0;

            // Antenna block parameters
            int antennaCycle = 1;
            int antennaFreqAgile = 0;
            int antennaSelect = 0;

            //Tag select block parameters
            int invSelectIndex = 0;

            //Inventtory block paraameters
            int queryTarget = 0;
            int querySession = 2;
            int querySelect = 1;
            int invAlgo = 3;
            int matchRep = 0;
            int tagSelect = 0;
            int noInventory = 0;
            int tagRead = 0;
            int tagDelay = 0;
            int tagJoin = 0;
            int brandid = 0;
            int invAuthenticate = 0;
            int algoSelect = 3;

            int rssiFilterType = 0;
            int rssiFilterOption = 0;
            int rssiFilterThreshold = 0;
            long rssiFilterCount = 0;

            int matchEnable = 0;
            int matchType = 0;
            int matchLength = 0;
            int matchOffset = 0;
            byte[] invMatchData0_63; int invMatchDataReady = 0;

            //Tag access block parameters
            int accessRetry = 3;
            int accessBank = 1; int accessBank2 = 0;
            int accessOffset = 2; int accessOffset2 = 0;
            int accessCount = 1; int accessCount2 = 0;
            int accessLockAction = 0;
            int accessLockMask = 0;
            //long accessPassword = 0;
            // long killPassword = 0;
            int accessWriteDataSelect = 0;
            byte[] accWriteData0_63; int accWriteDataReady = 0;

            byte[] authMatchData; int authMatchDataReady = 0;
        }
        Rx000Setting_default mDefault = new Rx000Setting_default();

        public boolean readMAC(int address) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 0, 0, 0, 0, 0};
            msgBuffer[2] = (byte) (address % 256);
            msgBuffer[3] = (byte) ((address >> 8) % 256);
            if (false) appendToLog("readMac buffer = " + byteArrayToString(msgBuffer));
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, false, msgBuffer);
        }
        public boolean writeMAC(int address, long value) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 0, 0, 0, 0, 0};
            msgBuffer[2] = (byte) (address % 256);
            msgBuffer[3] = (byte) ((address >> 8) % 256);
            msgBuffer[4] = (byte) (value % 256);
            msgBuffer[5] = (byte) ((value >> 8) % 256);
            msgBuffer[6] = (byte) ((value >> 16) % 256);
            msgBuffer[7] = (byte) ((value >> 24) % 256);
            if (false) appendToLog("writeMac buffer = " + byteArrayToString(msgBuffer));
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, true, msgBuffer);
        }

        String macVer = null; int macVerBuild = 0;
        public String getMacVer() {
            if (macVer == null) {
                readMAC(0);
                return "";
            } else {
                return macVer;
            }
        }

        long mac_last_command_duration;
        long getMacLastCommandDuration(boolean request) {
            if (request) {
                if (true) readMAC(9);
                //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, 0, 0, 0, 0, 0};
                //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.MAC_LAST_COMMAND_DURATION, false, msgBuffer);
            }
            return mac_last_command_duration;
        }

        final int DIAGCFG_INVALID = -1; final int DIAGCFG_MIN = 0; final int DIAGCFG_MAX = 0x3FF;
        int diagnosticCfg = DIAGCFG_INVALID;
        public int getDiagnosticConfiguration() {
            if (diagnosticCfg < DIAGCFG_MIN || diagnosticCfg > DIAGCFG_MAX) {
                if (true) readMAC(0x201);
                //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 2, 0, 0, 0, 0};
                //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, false, msgBuffer);
            }
            return diagnosticCfg;
        }
        public boolean setDiagnosticConfiguration(boolean bCommmandActive) {
            int diagnosticCfgNew = diagnosticCfg;
            diagnosticCfgNew &= ~0x0200; if (bCommmandActive) diagnosticCfgNew |= 0x200;
            if (diagnosticCfg == diagnosticCfgNew && sameCheck) return true;
            diagnosticCfg = diagnosticCfgNew;
            return writeMAC(0x201, diagnosticCfgNew); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, true, msgBuffer);
        }

        int impinjExtensionValue = -1;
        public int getImpinjExtension() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                {
                    if (impinjExtensionValue < 0) readMAC(0x203);
                    return impinjExtensionValue;
                }
            }
        }
        public boolean setImpinjExtension(boolean tagFocus, boolean fastId) {
            int iValue = (tagFocus ? 0x10 : 0) | (fastId ? 0x20 : 0);
            if (impinjExtensionValue == iValue && sameCheck) return true;
            appendToLog("BtDataOut 11: ivalue = " + iValue + ", impinjExtensionValue = " + rx000Setting.impinjExtensionValue);
            boolean bRetValue = writeMAC(0x203, iValue);
            if (bRetValue) impinjExtensionValue = iValue;
            return bRetValue;
        }

        int pwrMgmtStatus = -1;
        void getPwrMgmtStatus() {
            if (false) appendToLog("pwrMgmtStatus: getPwrMgmtStatus ");
            pwrMgmtStatus = -1; readMAC(0x204);
        }

        final int MBPADDR_INVALID = -1; final int MBPADDR_MIN = 0; final int MBPADDR_MAX = 0x1FFF;
        long mbpAddress = MBPADDR_INVALID;
        boolean setMBPAddress(long mbpAddress) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 4, 0, 0, 0, 0};
            if (mbpAddress < MBPADDR_MIN || mbpAddress > MBPADDR_MAX) return false;
            //mbpAddress = mDefault.mbpAddress;
            if (this.mbpAddress == mbpAddress && sameCheck)  return true;
            //msgBuffer[4] = (byte) (mbpAddress % 256);
            //msgBuffer[5] = (byte) ((mbpAddress >> 8) % 256);
            this.mbpAddress = mbpAddress;
            if (false) appendToLog("Going to writeMAC");
            return writeMAC(0x400, (int) mbpAddress); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_ADDR, true, msgBuffer);
        }

        final int MBPDATA_INVALID = -1; final int MBPDATA_MIN = 0; final int MBPDATA_MAX = 0x1FFF;
        long mbpData = MBPDATA_INVALID;
        boolean setMBPData(long mbpData) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 4, 0, 0, 0, 0};
            if (mbpData < MBPADDR_MIN || mbpData > MBPADDR_MAX) return false;
            //mbpData = mDefault.mbpData;
            if (this.mbpData == mbpData && sameCheck)  return true;
            //msgBuffer[4] = (byte) (mbpData % 256);
            //msgBuffer[5] = (byte) ((mbpData >> 8) % 256);
            this.mbpData = mbpData;
            return writeMAC(0x401, (int) mbpData); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_DATA, true, msgBuffer);
        }

        final int OEMADDR_INVALID = -1; final int OEMADDR_MIN = 0; final int OEMADDR_MAX = 0x1FFF;
        long oemAddress = OEMADDR_INVALID;
        boolean setOEMAddress(long oemAddress) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 5, 0, 0, 0, 0};
            if (oemAddress < OEMADDR_MIN || oemAddress > OEMADDR_MAX) return false;
            //oemAddress = mDefault.oemAddress;
            if (this.oemAddress == oemAddress && sameCheck)  return true;
            //msgBuffer[4] = (byte) (oemAddress % 256);
            //msgBuffer[5] = (byte) ((oemAddress >> 8) % 256);
            this.oemAddress = oemAddress;
            return writeMAC(0x500, (int) oemAddress); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_ADDR, true, msgBuffer);
        }

        final int OEMDATA_INVALID = -1; final int OEMDATA_MIN = 0; final int OEMDATA_MAX = 0x1FFF;
        long oemData = OEMDATA_INVALID;
        boolean setOEMData(long oemData) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 5, 0, 0, 0, 0};
            if (oemData < OEMADDR_MIN || oemData > OEMADDR_MAX) return false;
            //oemData = mDefault.oemData;
            if (this.oemData == oemData && sameCheck)  return true;
            //msgBuffer[4] = (byte) (oemData % 256);
            //msgBuffer[5] = (byte) ((oemData >> 8) % 256);
            this.oemData = oemData;
            return writeMAC(0x501, (int) oemData); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_DATA, true, msgBuffer);
        }

        // Antenna block parameters
        final int ANTCYCLE_INVALID = -1; final int ANTCYCLE_MIN = 0; final int ANTCYCLE_MAX = 0xFFFF;
        int antennaCycle = ANTCYCLE_INVALID;
        public int getAntennaCycle() {
            if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) getHST_ANT_CYCLES();
            return antennaCycle;
        }
        public boolean setAntennaCycle(int antennaCycle) {
            return setAntennaCycle(antennaCycle, antennaFreqAgile);
        }
        boolean setAntennaCycle(int antennaCycle, int antennaFreqAgile) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 7, 0, 0, 0, 0};
            if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) antennaCycle = mDefault.antennaCycle;
            if (antennaFreqAgile < FREQAGILE_MIN || antennaFreqAgile > FREQAGILE_MAX)   antennaFreqAgile = mDefault.antennaFreqAgile;
            if (this.antennaCycle == antennaCycle && this.antennaFreqAgile == antennaFreqAgile  && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaCycle % 256);
            msgBuffer[5] = (byte) ((antennaCycle >> 8) % 256);
            if (antennaFreqAgile != 0) {
                msgBuffer[7] |= 0x01;
            }
            this.antennaCycle = antennaCycle;
            this.antennaFreqAgile = antennaFreqAgile;
            return sendHostRegRequest(HostRegRequests.HST_ANT_CYCLES, true, msgBuffer);
        }

        final int FREQAGILE_INVALID = -1; final int FREQAGILE_MIN = 0; final int FREQAGILE_MAX = 1;
        int antennaFreqAgile = FREQAGILE_INVALID;
        int getAntennaFreqAgile() {
            if (antennaFreqAgile < FREQAGILE_MIN || antennaFreqAgile > FREQAGILE_MAX)
                getHST_ANT_CYCLES();
            return antennaFreqAgile;
        }
        public boolean setAntennaFreqAgile(int freqAgile) {
            return setAntennaCycle(antennaCycle, freqAgile);
        }

        private boolean getHST_ANT_CYCLES() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 7, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_ANT_CYCLES, false, msgBuffer);
        }

        final int ANTSELECT_INVALID = -1; final int ANTSLECT_MIN = 0; final int ANTSELECT_MAX = 15;
        int antennaSelect = ANTSELECT_INVALID;  //default value = 0
        public int getAntennaSelect() {
            appendToLog("AntennaSelect = " + antennaSelect);
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_SEL, false, msgBuffer);
            }
            return antennaSelect;
        }
        public boolean setAntennaSelect(int antennaSelect) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 7, 0, 0, 0, 0};
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  antennaSelect = mDefault.antennaSelect;
            if (this.antennaSelect == antennaSelect && sameCheck) return true;
            this.antennaSelect = antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect);
            msgBuffer[4] = (byte) (antennaSelect);
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_SEL, true, msgBuffer);
        }

        AntennaSelectedData[] antennaSelectedData;
        public int getAntennaEnable() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaEnable();
            }
        }
        public boolean setAntennaEnable(int antennaEnable) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaEnable(antennaEnable);
        }
        boolean setAntennaEnable(int antennaEnable, int antennaInventoryMode, int antennaLocalAlgo, int antennaLocalStartQ,
                                 int antennaProfileMode, int antennaLocalProfile, int antennaFrequencyMode, int antennaLocalFrequency) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        int getAntennaInventoryMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaInventoryMode();
            }
        }
        boolean setAntennaInventoryMode(int antennaInventoryMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaInventoryMode(antennaInventoryMode);
        }

        int getAntennaLocalAlgo() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalAlgo();
            }
        }
        boolean setAntennaLocalAlgo(int antennaLocalAlgo) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalAlgo(antennaLocalAlgo);
        }

        int getAntennaLocalStartQ() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalStartQ();
            }
        }
        boolean setAntennaLocalStartQ(int antennaLocalStartQ) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalStartQ(antennaLocalStartQ);
        }

        int getAntennaProfileMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaProfileMode();
            }
        }
        boolean setAntennaProfileMode(int antennaProfileMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaProfileMode(antennaProfileMode);
        }

        int getAntennaLocalProfile() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalProfile();
            }
        }
        boolean setAntennaLocalProfile(int antennaLocalProfile) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalProfile(antennaLocalProfile);
        }

        int getAntennaFrequencyMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaFrequencyMode();
            }
        }
        boolean setAntennaFrequencyMode(int antennaFrequencyMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaFrequencyMode(antennaFrequencyMode);
        }

        int getAntennaLocalFrequency() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalFrequency();
            }
        }
        boolean setAntennaLocalFrequency(int antennaLocalFrequency) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalFrequency(antennaLocalFrequency);
        }

        int getAntennaStatus() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaStatus();
            }
        }

        int getAntennaDefine() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaDefine();
            }
        }

        public long getAntennaDwell() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaDwell();
            }
        }
        public boolean setAntennaDwell(long antennaDwell) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaDwell(antennaDwell);
        }

        public long getAntennaPower(int portNumber) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                if (portNumber < 0 || portNumber > 15) portNumber = antennaSelect;
                long lValue;
                lValue = antennaSelectedData[portNumber].getAntennaPower();
                return lValue;
            }
        }
        public boolean setAntennaPower(long antennaPower) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaPower(antennaPower);
        }

        long getAntennaInvCount() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaInvCount();
            }
        }
        public boolean setAntennaInvCount(long antennaInvCount) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaInvCount(antennaInvCount);
        }

        //Tag select block parameters
        final int INVSELECT_INVALID = -1; final int INVSELECT_MIN = 0; final int INVSELECT_MAX = 7;
        public int invSelectIndex = INVSELECT_INVALID;
        public int getInvSelectIndex() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) {
                {
                    byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 8, 0, 0, 0, 0};
                    sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_SEL, false, msgBuffer);
                }
            }
            return invSelectIndex;
        }
        public boolean setInvSelectIndex(int invSelect) {
            if (utility.DEBUG_SELECT) appendToLog("Debug_Select: RfidReaderChipR2000.setInvSelectIndex[" + invSelect);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 8, 0, 0, 0, 0};
            if (invSelect < INVSELECT_MIN || invSelect > INVSELECT_MAX) invSelect = mDefault.invSelectIndex;
            if (this.invSelectIndex == invSelect && sameCheck) return true;
            msgBuffer[4] = (byte) (invSelect & 0x07);
            this.invSelectIndex = invSelect;
            return sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_SEL, true, msgBuffer);
        }

        InvSelectData[] invSelectData;
        public int getSelectEnable() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].getSelectEnable();
        }
        public boolean setSelectEnable(int enable, int selectTarget, int selectAction, int selectDelay) {
            if (utility.DEBUG_SELECT) appendToLog("Debug_Select: RfidReaderChipR2000.Rx000Setting.setSelectEnable[" + enable);
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (false) appendToLog("BtDataOut: RfidReaderChipR200.setSelectEnable goes to setRx000HostReg_HST_TAGMSK_DESC_CFG with index = " + invSelectIndex);
            return invSelectData[invSelectIndex].setRx000HostReg_HST_TAGMSK_DESC_CFG(enable, selectTarget, selectAction, selectDelay);
        }

        public int getSelectTarget() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].getSelectTarget();
        }

        public int getSelectAction() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].getSelectAction();
        }

        public int getSelectMaskBank() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].getSelectMaskBank();
        }
        public boolean setSelectMaskBank(int selectMaskBank) {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].setSelectMaskBank(selectMaskBank);
        }

        public int getSelectMaskOffset() {
            int dataIndex = invSelectIndex;
            if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
                return INVSELECT_INVALID;
            } else {
                return invSelectData[dataIndex].getSelectMaskOffset();
            }
        }
        public boolean setSelectMaskOffset(int selectMaskOffset) {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].setSelectMaskOffset(selectMaskOffset);
        }

        public int getSelectMaskLength() {
            int dataIndex = invSelectIndex;
            if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
                return INVSELECT_INVALID;
            } else {
                return invSelectData[dataIndex].getSelectMaskLength();
            }
        }
        public boolean setSelectMaskLength(int selectMaskLength) {
            if (false) appendToLog("btDataOut: setSelectMaskLength with selectMaskLength = " + selectMaskLength);
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            return invSelectData[invSelectIndex].setSelectMaskLength(selectMaskLength);
        }

        public String getSelectMaskData() {
            int dataIndex = invSelectIndex;
            if (dataIndex < INVSELECT_MIN || dataIndex > INVSELECT_MAX) {
                return null;
            } else {
                return invSelectData[dataIndex].getRx000SelectMaskData();
            }
        }
        public boolean setSelectMaskData(String maskData) {
            if (maskData == null)   return false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (invSelectData[invSelectIndex].selectMaskDataReady != 0) {
                String maskDataOld = getSelectMaskData();
                if (maskData != null && maskDataOld != null) {
                    if (maskData.matches(maskDataOld) && sameCheck) return true;
                }
            }
            return invSelectData[invSelectIndex].setRx000SelectMaskData(maskData);
        }

        //Inventtory block paraameters
        final int QUERYTARGET_INVALID = -1; final int QUERYTARGET_MIN = 0; final int QUERYTARGET_MAX = 1;
        int queryTarget = QUERYTARGET_INVALID;
        public int getQueryTarget() {
            if (DEBUG) appendToLog("RfidReaderChipR2000.getQueryTarget with queryTarget = " + queryTarget);
            if (queryTarget == QUERYTARGET_INVALID) getHST_QUERY_CFG();
            return queryTarget;
        }
        boolean setQueryTarget(int queryTarget) {
            if (DEBUG || true) appendToLog("3 setQueryTarget is callled from RfidReaderChipR2000.setQueryTarget[" + queryTarget);
            return setQueryTarget(queryTarget, querySession, querySelect);
        }
        public boolean setQueryTarget(int queryTarget, int querySession, int querySelect) {
            if (false) appendToLog("setQueryTarget[" + queryTarget + ", " + querySession + ", " + querySelect + "] with sameCheck = " + sameCheck + ", old queryTarget/Session/Select = " + this.queryTarget + ", " + this.querySession + ", " + this.querySelect);
            if (queryTarget >= 2) { rx000Setting.setAlgoAbFlip(1); }
            else if (queryTarget >= 0) { rx000Setting.setAlgoAbFlip(0); }

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 9, 0, 0, 0, 0};

            if ((this.queryTarget == queryTarget || queryTarget == 2 || queryTarget == -1)
                    && (this.querySession == querySession || querySession == -1)
                    && (this.querySelect == querySelect || querySelect == -1) && sameCheck) return true;
            msgBuffer[4] |= ((queryTarget == 2 ? 0 : queryTarget) << 4);
            msgBuffer[4] |= (byte) (querySession << 5);
            if ((querySelect & 0x01) != 0) {
                msgBuffer[4] |= (byte) 0x80;
            }
            if ((querySelect & 0x02) != 0) {
                msgBuffer[5] |= (byte) 0x01;
            }
            this.queryTarget = queryTarget;
            this.querySession = querySession;
            this.querySelect = querySelect;
            return sendHostRegRequest(HostRegRequests.HST_QUERY_CFG, true, msgBuffer);
        }

        final int QUERYSESSION_INVALID = -1; final int QUERYSESSION_MIN = 0; final int QUERYSESSION_MAX = 3;
        int querySession = QUERYSESSION_INVALID;
        public int getQuerySession() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                {
                    //appendToLog("BtDataOut: RfidReaderChipR2000.getQuerySession with querySession = " + querySession);
                    if (querySession < QUERYSESSION_MIN || querySession > QUERYSESSION_MAX)
                        getHST_QUERY_CFG();
                    return querySession;
                }
            }

        }
        boolean setQuerySession(int querySession) {
            if (true) appendToLog("4 setQueryTargetis callled from BtDataOut: RfidReaderChipR2000.setQuerySession[" + querySession);
            return setQueryTarget((getAlgoAbFlip() > 0 ? 2 : queryTarget), querySession, querySelect);
        }

        final int QUERYSELECT_INVALID = -1; final int QUERYSELECT_MIN = 0; final int QUERYSELECT_MAX = 3;
        int querySelect = QUERYSELECT_INVALID;
        public int getQuerySelect() {
            if (false) appendToLog("BtDataOut: RfidReaderChipR2000.getQuerySelect with querySelect = " + querySelect);
            if (querySelect < QUERYSELECT_MIN || querySelect > QUERYSELECT_MAX) getHST_QUERY_CFG();
            if (false) appendToLog("Stream querySelect = " + querySelect);
            return querySelect;
        }
        public boolean setQuerySelect(int querySelect) {
            if (false) appendToLog("5 setQueryTarget is callled from RfidReaderChipR2000.setQuerySelect[" + querySelect);
            if (querySelect == this.querySelect) return true;
            return setQueryTarget((getAlgoAbFlip() > 0 ? 2 : queryTarget), querySession, querySelect);
        }

        private boolean getHST_QUERY_CFG() {
            //appendToLog("BtDataOut: RfidReaderChipR2000.getHST_QUERY_CFG");
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_QUERY_CFG, false, msgBuffer);
        }

        final int INVALGO_INVALID = -1; final int INVALGO_MIN = 0; final int INVALGO_MAX = 3;
        int invAlgo = INVALGO_INVALID;
        public int getInvAlgo() {
            if (invAlgo < INVALGO_MIN || invAlgo > INVALGO_MAX) getHST_INV_CFG();
            return invAlgo;
        }
        public boolean setInvAlgo(int invAlgo) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setInvAlog goes to setInvAlgo with invAlgo = " + invAlgo);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        final int MATCHREP_INVALID = -1; final int MATCHREP_MIN = 0; final int MATCHREP_MAX = 255;
        int matchRep = MATCHREP_INVALID;
        int getMatchRep() {
            if (matchRep < MATCHREP_MIN || matchRep > MATCHREP_MAX) getHST_INV_CFG();
            return matchRep;
        }
        public boolean setMatchRep(int matchRep) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setMatchRep goes to setInvAlgo with matchRep = " + matchRep);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        final int TAGSELECT_INVALID = -1; final int TAGSELECT_MIN = 0; final int TAGSELECT_MAX = 1;
        int tagSelect = TAGSELECT_INVALID;
        int getTagSelect() {
            if (tagSelect < TAGSELECT_MIN || tagSelect > TAGSELECT_MAX) getHST_INV_CFG();
            return tagSelect;
        }
        public boolean setTagSelect(int tagSelect) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setTagSelect goes to setInvAlgo with tagSelect = " + tagSelect);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        final int NOINVENTORY_INVALID = -1; final int NOINVENTORY_MIN = 0; final int NOINVENTORY_MAX = 1;
        int noInventory = NOINVENTORY_INVALID;
        int getNoInventory() {
            if (noInventory < NOINVENTORY_MIN || noInventory > NOINVENTORY_MAX) getHST_INV_CFG();
            return noInventory;
        }
        boolean setNoInventory(int noInventory) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setNoInventory goes to setInvAlgo with noInventory = " + noInventory);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        final int TAGREAD_INVALID = -1; final int TAGREAD_MIN = 0; final int TAGREAD_MAX = 2;
        int tagRead = TAGREAD_INVALID;
        int getTagRead() {
            if (tagRead < TAGREAD_MIN || tagRead > TAGREAD_MAX) getHST_INV_CFG();
            return tagRead;
        }
        public boolean setTagRead(int tagRead) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setTagRead goes to setInvAlgo with tagRead = " + tagRead);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        final int TAGDELAY_INVALID = -1; final int TAGDELAY_MIN = 0; final int TAGDELAY_MAX = 63;
        int tagDelay = TAGDELAY_INVALID;
        int getTagDelay() {
            if (tagDelay < TAGDELAY_MIN || tagDelay > TAGDELAY_MAX) getHST_INV_CFG();
            return tagDelay;
        }
        public boolean setTagDelay2RfidReader(int tagDelay) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setTagDelay goes to setInvAlgo with tagDelay = " + tagDelay);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, invAuthenticate);
        }

        byte intraPacketDelay = 4;
        public byte getIntraPacketDelay() {
            appendToLog("intraPacketDelay = " + intraPacketDelay);
            return intraPacketDelay;
        }
        public boolean setIntraPacketDelay(byte intraPkDelay) {
            if (intraPacketDelay == intraPkDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with intraPkDelay = " + intraPkDelay);
                return true;
            }
            appendToLog("Skip setDupElim with intraPkDelay = " + intraPkDelay);
            intraPacketDelay = intraPkDelay;
            return true;
        }

        byte dupElimRollWindow = 0;
        public byte getDupElimRollWindow() {
            appendToLog("dupElim = " + dupElimRollWindow);
            return dupElimRollWindow;
        }
        public boolean setDupElimRollWindow(byte dupElimDelay) {
            if (dupElimRollWindow == dupElimDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with dupElimDelay = " + dupElimDelay);
                return true;
            }
            appendToLog("Skip setDupElim with dupElimDelay = " + dupElimDelay);
            dupElimRollWindow = dupElimDelay;
            return true;
        }

        long cycleDelay = 0;
        public long getCycleDelay() {
            return cycleDelay;
        }
        public boolean setCycleDelay(long cycleDelay) {
            if (this.cycleDelay == cycleDelay && sameCheck) return true;

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, (byte)0x0F, (byte)0x0F, 0, 0, 0, 0};
            msgBuffer[4] |= (cycleDelay & 0xFF);
            msgBuffer[5] |= (byte) ((cycleDelay & 0xFF00) >> 8);
            msgBuffer[6] |= (byte) ((cycleDelay & 0xFF0000) >> 16);
            msgBuffer[7] |= (byte) ((cycleDelay & 0xFF000000) >> 24);
            this.cycleDelay = cycleDelay;
            boolean bResult = sendHostRegRequest(HostRegRequests.HST_INV_CFG, true, msgBuffer);
            //msgBuffer = new byte[]{(byte) 0x70, 0, (byte)0x0F, (byte)0x0F, 0, 0, 0, 0};
            //sendHostRegRequest(HostRegRequests.HST_INV_CFG, false, msgBuffer);
            return bResult;
        }

        final int AUTHENTICATE_CFG_INVALID = -1; final int AUTHENTICATE_CFG_MIN = 0; final int AUTHENTICATE_CFG_MAX = 4095;
        boolean authenticateSendReply;
        boolean authenticateIncReplyLength;
        int authenticateLength = AUTHENTICATE_CFG_INVALID;
        public int getAuthenticateReplyLength() {
            if (authenticateLength < AUTHENTICATE_CFG_MIN || authenticateLength > AUTHENTICATE_CFG_MAX) getHST_AUTHENTICATE_CFG();
            return authenticateLength;
        }
        private boolean getHST_AUTHENTICATE_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, (byte) 0x0F, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_CFG, false, msgBuffer);
        }
        public boolean setHST_AUTHENTICATE_CFG(boolean sendReply, boolean incReplyLenth, int csi, int length) {
            appendToLog("sendReply = " + sendReply + ", incReplyLenth = " + incReplyLenth + ", length = " + length);
            if (length < 0 || length > 0x3FF) return false;

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, (byte) 0x0F, 0, 0, 0, 0};
            if (sendReply) msgBuffer[4] |= 0x01; authenticateSendReply = sendReply;
            if (incReplyLenth) msgBuffer[4] |= 0x02; authenticateIncReplyLength = incReplyLenth;
            msgBuffer[4] |= ((csi & 0x3F) << 2);
            msgBuffer[5] |= ((csi >> 6) & 0x03);
            msgBuffer[5] |= ((length & 0x3F) << 2);
            msgBuffer[6] |= ((length & 0xFC0) >> 6); authenticateLength = length;
            return sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_CFG, true, msgBuffer);
        }

        byte[] authMatchData0_63; int authMatchDataReady = 0;
        public String getAuthMatchData() {
            int length = 96;
            String strValue = "";
            for (int i = 0; i < 3; i++) {
                if (length > 0) {
                    appendToLog("i = " + i + ", authMatchDataReady = " + authMatchDataReady);
                    if ((authMatchDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, (byte)0x0F, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_MSG, false, msgBuffer);
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", authMatchData0_63[i * 4 + j]);
                        }
                    }
                    length -= 32;
                }
            }
            if (strValue.length() < 16) strValue = null;
            return strValue;
        }
        public boolean setAuthMatchData(String matchData) {
            int length = matchData.length();
            appendToLog("matchData is " + length + ", " + matchData);
            for (int i = 0; i < 6; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, (byte)0x0F, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= matchData.length()) {
                            String subString = matchData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
                            if ((j / 2) * 2 == j) {
                                msgBuffer[7 - j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[7 - j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_MSG, true, msgBuffer) == false)
                        return false;
                    else {
                        //authMatchDataReady |= (0x01 << i);
                        System.arraycopy(msgBuffer, 4, authMatchData0_63, i * 4, 4); //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
//                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
                    }
                }
            }
            return true;
        }

        final int UNTRACEABLE_CFG_INVALID = -1; final int UNTRACEABLE_CFG_MIN = 0; final int UNTRACEABLE_CFG_MAX = 3;
        int untraceableRange = UNTRACEABLE_CFG_INVALID;
        boolean untraceableUser;
        int untraceableTid = UNTRACEABLE_CFG_INVALID;
        int untraceableEpcLength = UNTRACEABLE_CFG_INVALID;
        boolean untraceableEpc;
        boolean untraceableUXpc;
        public int getUntraceableEpcLength() {
            if (untraceableRange < UNTRACEABLE_CFG_MIN || untraceableRange > UNTRACEABLE_CFG_MAX) getHST_UNTRACEABLE_CFG();
            return untraceableEpcLength;
        }
        private boolean getHST_UNTRACEABLE_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, (byte) 0x0F, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG, false, msgBuffer);
        }
        public boolean setHST_UNTRACEABLE_CFG(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
            appendToLog("range1 = " + range + ", user = " + user + ", tid = " + tid + ", epc = " + epc + ", epcLength = " + epcLength + ", xcpc = " + uxpc);
            if (range < 0 || range > 3) return false;
            if (tid < 0 || tid > 2) return false;
            if (epcLength < 0 || epcLength > 31) return false;

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, (byte) 0x0F, 0, 0, 0, 0};
            msgBuffer[4] |= (range); untraceableRange = range;
            if (user) msgBuffer[4] |= 0x04; untraceableUser = user;
            msgBuffer[4] |= (tid << 3); untraceableTid = tid;
            msgBuffer[4] |= ((epcLength & 0x7) << 5);
            msgBuffer[5] |= ((epcLength & 0x18) >> 3); untraceableEpcLength = epcLength;
            if (epc) msgBuffer[5] |= 0x04; untraceableEpc = epc;
            if (uxpc) msgBuffer[5] |= 0x08; untraceableUXpc = uxpc;
            appendToLog("msgbuffer = " + byteArrayToString(msgBuffer));
            return sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG, true, msgBuffer);
        }

        final int TAGJOIN_INVALID = -1; final int TAGJOIN_MIN = 0; final int TAGJOIN_MAX = 1;
        int invModeCompact = TAGJOIN_INVALID;
        boolean getInvModeCompact() {
            if (invModeCompact < TAGDELAY_MIN || invModeCompact > TAGDELAY_MAX) { getHST_INV_CFG(); return false; }
            return (invModeCompact == 1);
        }
        public boolean setInvModeCompact(boolean invModeCompact) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setInvModeCompact goes to setInvAlgo with invModeCompact = " + invModeCompact);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, (invModeCompact ? 1 : 0), invBrandId, invAuthenticate);
        }

        final int BRAND_INVALID = -1; final int BRANDID_MIN = 0; final int BRANDID_MAX = 1;
        int invBrandId = BRAND_INVALID;
        boolean getInvBrandId() {
            if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) { getHST_INV_CFG(); return false; }
            return (invModeCompact == 1);
        }
        public boolean setInvBrandId(boolean invBrandId) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setInvBrandId goes to setInvAlgo with invBrandId = " + invBrandId);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, (invBrandId ? 1 : 0), invAuthenticate);
        }

        final int INVAUTHENTICATE_INVALID = -1; final int INVAUTHENTICATE_MIN = 0; final int INVAUTHENTICATE_MAX = 1;
        int invAuthenticate = INVAUTHENTICATE_INVALID;
        boolean getInvAuthenticate() {
            if (invAuthenticate < INVAUTHENTICATE_MIN || invAuthenticate > INVAUTHENTICATE_MAX) { getHST_INV_CFG(); return false; }
            return (invAuthenticate == 1);
        }
        public boolean setInvAuthenticate(boolean invAuthenticate) {
            if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: RfidReaderChipR2000.Rx000setting.setInvBrandId goes to setInvAlgo with invBrandId = " + invBrandId);
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId, (invAuthenticate ? 1: 0));
        }

        private boolean getHST_INV_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_CFG, false, msgBuffer);
        }
        boolean setInvAlgo(int invAlgo, int matchRep, int tagSelect, int noInventory, int tagRead, int tagDelay, int invModeCompact, int invBrandId, int invAuthenticate) {
            boolean DEBUG = utility.DEBUG_INVCFG;
            if (DEBUG) appendToLog("Debug_InvCfg: 0 tagRead is " + tagRead);
            if (invAlgo < INVALGO_MIN || invAlgo > INVALGO_MAX) invAlgo = mDefault.invAlgo;
            if (matchRep < MATCHREP_MIN || matchRep > MATCHREP_MAX) matchRep = mDefault.matchRep;
            if (tagSelect < TAGSELECT_MIN || tagSelect > TAGSELECT_MAX) tagSelect = mDefault.tagSelect;
            if (noInventory < NOINVENTORY_MIN || noInventory > NOINVENTORY_MAX) noInventory = mDefault.noInventory;
            if (tagDelay < TAGDELAY_MIN || tagDelay > TAGDELAY_MAX) tagDelay = mDefault.tagDelay;
            if (invModeCompact < TAGJOIN_MIN || invModeCompact > TAGJOIN_MAX) invModeCompact = mDefault.tagJoin;
            if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) invBrandId = mDefault.brandid;
            if (invAuthenticate < INVAUTHENTICATE_MIN || invAuthenticate > INVAUTHENTICATE_MAX) invAuthenticate = mDefault.invAuthenticate;
            if (tagRead < TAGREAD_MIN || tagRead > TAGREAD_MAX) tagRead = mDefault.tagRead;
            if (DEBUG) appendToLog("Debug_InvCfg: Old invAlgo = " + this.invAlgo + ", matchRep = " + this.matchRep + ", tagSelect = " + this.tagSelect + ", noInventory = " + this.noInventory + ", tagRead = " + this.tagRead + ", tagDelay = " + this.tagDelay + ", invModeCompact = " + this.invModeCompact + ", invBrandId = " + this.invBrandId + ", invAuthenticate = " + this.invAuthenticate);
            if (DEBUG) appendToLog("Debug_InvCfg: New invAlgo = " + invAlgo + ", matchRep = " + matchRep + ", tagSelect = " + tagSelect + ", noInventory = " + noInventory + ", tagRead = " + tagRead + ", tagDelay = " + tagDelay + ", invModeCompact = " + invModeCompact + ", invBrandId = " + invBrandId + ", invAuthenticate = " + invAuthenticate + ", sameCheck = " + sameCheck);
            boolean bool1 = this.invAlgo == invAlgo;
            boolean bool2 = this.matchRep == matchRep;
            boolean bool3 = this.tagSelect == tagSelect;
            boolean bool4 = this.noInventory == noInventory;
            if (DEBUG) appendToLog("Debug_InvCfg: tagRead = " + tagRead + ", this.tagRead = " + this.tagRead);
            boolean bool5 = (this.tagRead == tagRead) || (tagRead == -1);
            boolean bool6 = this.tagDelay == tagDelay;
            boolean bool7 = this.invModeCompact == invModeCompact;
            boolean bool8 = this.invBrandId == invBrandId;
            boolean bool9 = this.invAuthenticate == invAuthenticate;
            if (bool1 && bool2 && bool3 && bool4 && bool5 && bool6 && bool7 && bool8 && bool9 && sameCheck) return true;
            if (DEBUG) appendToLog("Debug_InvCfg: There is difference with " + bool1 + "," + bool2 + "," + bool3 + "," + bool4 + "," + bool5 + "," + bool6 + "," + bool7 + "," + bool8 + "," + bool9);
            this.invAlgo = invAlgo; if (DEBUG) appendToLog("Hello6: invAlgo = " + invAlgo + ", queryTarget = " + queryTarget);
            this.matchRep = matchRep;
            this.tagSelect = tagSelect;
            this.noInventory = noInventory;
            this.tagRead = tagRead;
            this.tagDelay = tagDelay;
            this.invModeCompact = invModeCompact;
            this.invBrandId = invBrandId;
            this.invAuthenticate = invAuthenticate;
            appendToLog("BtDataOut: 1, invAuthenticate = " + rx000Setting.invAuthenticate);
            if (DEBUG) appendToLog("Debug_InvCfg: Stored tagDelay = " + this.tagDelay);

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 9, 0, 0, 0, 0};
            msgBuffer[4] |= invAlgo;
            msgBuffer[4] |= (byte) ((matchRep & 0x03) << 6);
            msgBuffer[5] |= (byte) (matchRep >> 2);
            if (tagSelect != 0) {
                msgBuffer[5] |= 0x40;
            }
            if (noInventory != 0) {
                msgBuffer[5] |= 0x80;
            }
            if ((tagRead & 0x03) != 0) {
                msgBuffer[6] |= (tagRead & 0x03);
            }
            if ((tagDelay & 0x0F) != 0) {
                msgBuffer[6] |= ((tagDelay & 0x0F) << 4);
            }
            if ((tagDelay & 0x30) != 0) {
                msgBuffer[7] |= ((tagDelay & 0x30) >> 4);
            }
            if (invModeCompact == 1) {
                msgBuffer[7] |= 0x04;
            }
            if (invBrandId == 1) {
                msgBuffer[7] |= 0x08;
            }
            if (invAuthenticate == 1) {
                msgBuffer[7] |= 0x10;
            }
            return sendHostRegRequest(HostRegRequests.HST_INV_CFG, true, msgBuffer);
        }

        final int ALGOSELECT_INVALID = -1; final int ALGOSELECT_MIN = 0; final int ALGOSELECT_MAX = 3;   //DataSheet says Max=1
        int algoSelect = ALGOSELECT_INVALID;
        public int getAlgoSelect() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 9, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_INV_SEL, false, msgBuffer);
            }
            return algoSelect;
        }
        boolean dummyAlgoSelected = false;
        public boolean setAlgoSelect(int algoSelect) {
            if (false) appendToLog("setTagGroup: algoSelect = " + algoSelect + ", this.algoSelct = " + this.algoSelect + ", dummyAlgoSelected = " + dummyAlgoSelected);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 9, 0, 0, 0, 0};
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX)
                algoSelect = mDefault.algoSelect;
            if (algoSelect == this.algoSelect && dummyAlgoSelected == false)  return true;
            msgBuffer[4] = (byte) (algoSelect & 0xFF);
            msgBuffer[5] = (byte) ((algoSelect & 0xFF00) >> 8);
            msgBuffer[6] = (byte) ((algoSelect & 0xFF0000) >> 16);
            msgBuffer[7] = (byte) ((algoSelect & 0xFF000000) >> 24);
            this.algoSelect = algoSelect;
            return sendHostRegRequest(HostRegRequests.HST_INV_SEL, true, msgBuffer);
        }

        AlgoSelectedData[] algoSelectedData;
        public int getAlgoStartQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoStartQ(false);
            }
        }
        public int getAlgoStartQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoStartQ(true);
            }
        }
        public boolean setAlgoStartQ(int algoStartQ) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoStartQ(algoStartQ);
        }
        public boolean setAlgoStartQ(int startQ, int algoMaxQ, int algoMinQ, int algoMaxRep, int algoHighThres, int algoLowThres) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoStartQ(startQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        public int getAlgoMaxQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxQ();
            }
        }
        int getAlgoMaxQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxQ();
            }
        }
        boolean setAlgoMaxQ(int algoMaxQ) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMaxQ(algoMaxQ);
        }

        public int getAlgoMinQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMinQ();
            }
        }
        int getAlgoMinQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMinQ();
            }
        }
        boolean setAlgoMinQ(int algoMinQ) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMinQ(algoMinQ);
        }

        int getAlgoMaxRep() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxRep();
            }
        }
        boolean setAlgoMaxRep(int algoMaxRep) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMaxRep(algoMaxRep);
        }

        int getAlgoHighThres() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoHighThres();
            }
        }
        boolean setAlgoHighThres(int algoHighThre) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoHighThres(algoHighThre);
        }

        int getAlgoLowThres() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoLowThres();
            }
        }
        boolean setAlgoLowThres(int algoLowThre) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoLowThres(algoLowThre);
        }

        public int getAlgoRetry(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoRetry();
            }
        }
        public boolean setAlgoRetry(int algoRetry) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoRetry(algoRetry);
        }

        int getAlgoAbFlip(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoAbFlip();
            }
        }
        public int getAlgoAbFlip() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoAbFlip();
            }
        }
        public boolean setAlgoAbFlip(int algoAbFlip) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoAbFlip(algoAbFlip);
        }
        boolean setAlgoAbFlip(int algoAbFlip, int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            appendToLog("algoSelect = " + algoSelect + ", algoAbFlip = " + algoAbFlip + ", algoRunTilZero = " + algoRunTilZero);
            return algoSelectedData[algoSelect].setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        public int getAlgoRunTilZero(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoRunTilZero();
            }
        }
        int getAlgoRunTilZero() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoRunTilZero();
            }
        }
        public boolean setAlgoRunTilZero(int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoRunTilZero(algoRunTilZero);
        }

        int rssiFilterConfig = -1;
        final int RSSIFILTERTYPE_INVALID = -1, RSSIFILTERTYPE_MIN = 0, RSSIFILTERTYPE_MAX = 2;
        int rssiFilterType = RSSIFILTERTYPE_INVALID;
        final int RSSIFILTEROPTION_INVALID = -1, RSSIFILTEROPTION_MIN = 0, RSSIFILTEROPTION_MAX = 4;
        int rssiFilterOption = RSSIFILTEROPTION_INVALID;
        public int getRssiFilterType() {
            if (rssiFilterType < 0) getHST_INV_RSSI_FILTERING_CONFIG();
            return rssiFilterType;
        }
        public int getRssiFilterOption() {
            if (rssiFilterOption < 0) getHST_INV_RSSI_FILTERING_CONFIG();
            return rssiFilterOption;
        }
        private boolean getHST_INV_RSSI_FILTERING_CONFIG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 7, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG, false, msgBuffer);
        }
        public boolean setHST_INV_RSSI_FILTERING_CONFIG(int rssiFilterType, int rssiFilterOption) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 7, 9, 0, 0, 0, 0};
            if (rssiFilterType < RSSIFILTERTYPE_MIN || rssiFilterType > RSSIFILTERTYPE_MAX)
                rssiFilterType = mDefault.rssiFilterType;
            if (rssiFilterOption < RSSIFILTEROPTION_MIN || matchType > RSSIFILTEROPTION_MAX)
                rssiFilterOption = mDefault.rssiFilterOption;
            if (this.rssiFilterType == rssiFilterType && this.rssiFilterOption == rssiFilterOption && sameCheck) return true;
            msgBuffer[4] |= (byte) (rssiFilterType & 0xF);
            msgBuffer[4] |= (byte) ((rssiFilterOption & 0xF) << 4);
            this.rssiFilterType = rssiFilterType;
            this.rssiFilterOption = rssiFilterOption;
            boolean bValue = sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG, true, msgBuffer);
            if (false) getHST_INV_RSSI_FILTERING_CONFIG();
            return bValue;
        }

        final int RSSIFILTERTHRESHOLD_INVALID = -1, RSSIFILTERTHRESHOLD_MIN = 0, RSSIFILTERTHRESHOLD_MAX = 0xFFFF;
        int rssiFilterThreshold1 = RSSIFILTERTHRESHOLD_INVALID;
        public int getRssiFilterThreshold1() {
            if (rssiFilterThreshold1 < 0) getHST_INV_RSSI_FILTERING_THRESHOLD();
            return rssiFilterThreshold1;
        }
        int rssiFilterThreshold2 = RSSIFILTERTHRESHOLD_INVALID;
        public int getRssiFilterThreshold2() {
            if (rssiFilterThreshold2 < 0) getHST_INV_RSSI_FILTERING_THRESHOLD();
            return rssiFilterThreshold2;
        }
        private boolean getHST_INV_RSSI_FILTERING_THRESHOLD() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 8, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, false, msgBuffer);
        }
        public boolean setHST_INV_RSSI_FILTERING_THRESHOLD(int rssiFilterThreshold1, int rssiFilterThreshold2) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, 9, 0, 0, 0, 0};
            if (rssiFilterThreshold1 < RSSIFILTERTHRESHOLD_MIN || rssiFilterThreshold1 > RSSIFILTERTHRESHOLD_MAX)
                rssiFilterThreshold1 = mDefault.rssiFilterThreshold;
            if (rssiFilterThreshold2 < RSSIFILTERTHRESHOLD_MIN || rssiFilterThreshold2 > RSSIFILTERTHRESHOLD_MAX)
                rssiFilterThreshold2 = mDefault.rssiFilterThreshold;
            if (this.rssiFilterThreshold1 == rssiFilterThreshold1 && this.rssiFilterThreshold2 == rssiFilterThreshold2 && sameCheck) return true;
            msgBuffer[4] |= (byte) (rssiFilterThreshold1 & 0xFF);
            msgBuffer[5] |= (byte) ((rssiFilterThreshold1 >> 8) & 0xFF);
            msgBuffer[6] |= (byte) (rssiFilterThreshold2 & 0xFF);
            msgBuffer[7] |= (byte) ((rssiFilterThreshold2 >> 8) & 0xFF);
            this.rssiFilterThreshold1 = rssiFilterThreshold1;
            this.rssiFilterThreshold2 = rssiFilterThreshold2;
            boolean bValue = sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, true, msgBuffer);
            if (false) getHST_INV_RSSI_FILTERING_THRESHOLD();
            return bValue;
        }

        final long RSSIFILTERCOUNT_INVALID = -1, RSSIFILTERCOUNT_MIN = 0, RSSIFILTERCOUNT_MAX = 1000000;
        long rssiFilterCount = RSSIFILTERCOUNT_INVALID;
        public long getRssiFilterCount() {
            if (rssiFilterCount < 0) getHST_INV_RSSI_FILTERING_COUNT();
            return rssiFilterCount;
        }
        private boolean getHST_INV_RSSI_FILTERING_COUNT() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, false, msgBuffer);
        }
        public boolean setHST_INV_RSSI_FILTERING_COUNT(long rssiFilterCount) {
            appendToLog("entry: rssiFilterCount = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 9, 9, 0, 0, 0, 0};
            if (rssiFilterCount < RSSIFILTERCOUNT_MIN || rssiFilterCount > RSSIFILTERCOUNT_MAX)
                rssiFilterCount = mDefault.rssiFilterCount;
            appendToLog("rssiFilterCount 1 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            if (this.rssiFilterCount == rssiFilterCount && sameCheck) return true;
            appendToLog("rssiFilterCount 2 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            msgBuffer[4] |= (byte) (rssiFilterCount & 0xFF);
            msgBuffer[5] |= (byte) ((rssiFilterCount >> 8) & 0xFF);
            msgBuffer[6] |= (byte) ((rssiFilterCount >> 16) & 0xFF);
            msgBuffer[7] |= (byte) ((rssiFilterCount >> 24) & 0xFF);
            this.rssiFilterCount = rssiFilterCount;
            appendToLog("entering to sendHostRegRequest: rssiFilterCount = " + rssiFilterCount);
            boolean bValue = sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_COUNT, true, msgBuffer);
            appendToLog("after sendHostRegRequest: rssiFilterCount = " + rssiFilterCount);
            return bValue;
        }

        final int MATCHENABLE_INVALID = -1; final int MATCHENABLE_MIN = 0; final int MATCHENABLE_MAX = 1;
        int matchEnable = MATCHENABLE_INVALID;
        public int getInvMatchEnable() {
            getHST_INV_EPC_MATCH_CFG();
            return matchEnable;
        }
        boolean setInvMatchEnable(int matchEnable) {
            return setHST_INV_EPC_MATCH_CFG(matchEnable, this.matchType, this.matchLength, this.matchOffset);
        }
        public boolean setInvMatchEnable(int matchEnable, int matchType, int matchLength, int matchOffset) {
            return setHST_INV_EPC_MATCH_CFG(matchEnable, matchType, matchLength, matchOffset);
        }

        final int MATCHTYPE_INVALID = -1; final int MATCHTYPE_MIN = 0; final int MATCHTYPE_MAX = 1;
        int matchType = MATCHTYPE_INVALID;
        public int getInvMatchType() {
            getHST_INV_EPC_MATCH_CFG();
            return matchType;
        }

        final int MATCHLENGTH_INVALID = 0; final int MATCHLENGTH_MIN = 0; final int MATCHLENGTH_MAX = 496;
        int matchLength = MATCHLENGTH_INVALID;
        public int getInvMatchLength() {
            getHST_INV_EPC_MATCH_CFG();
            return matchLength;
        }

        final int MATCHOFFSET_INVALID = -1; final int MATCHOFFSET_MIN = 0; final int MATCHOFFSET_MAX = 496;
        int matchOffset = MATCHOFFSET_INVALID;
        public int getInvMatchOffset() {
            getHST_INV_EPC_MATCH_CFG();
            return matchOffset;
        }

        private boolean getHST_INV_EPC_MATCH_CFG() {
            if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX
                    || matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX
                    || matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX
                    || matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX
            ) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0x11, 9, 0, 0, 0, 0};
                return sendHostRegRequest(HostRegRequests.HST_INV_EPC_MATCH_CFG, false, msgBuffer);
            } else {
                return false;
            }
        }
        private boolean setHST_INV_EPC_MATCH_CFG(int matchEnable, int matchType, int matchLength, int matchOffset) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0x11, 9, 0, 0, 0, 0};
            if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX)
                matchEnable = mDefault.matchEnable;
            if (matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX)
                matchType = mDefault.matchType;
            if (matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX)
                matchLength = mDefault.matchLength;
            if (matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX)
                matchOffset = mDefault.matchOffset;
            if (this.matchEnable == matchEnable && this.matchType == matchType && this.matchLength == matchLength && this.matchOffset == matchOffset && sameCheck) return true;
            if (matchEnable != 0) {
                msgBuffer[4] |= 0x01;
            }
            if (matchType != 0) {
                msgBuffer[4] |= 0x02;
            }
            msgBuffer[4] |= (byte) ((matchLength % 64) << 2);
            msgBuffer[5] |= (byte) ((matchLength / 64));
            msgBuffer[5] |= (byte) ((matchOffset % 32) << 3);
            msgBuffer[6] |= (byte) (matchOffset / 32);
            this.matchEnable = matchEnable;
            this.matchType = matchType;
            this.matchLength = matchLength;
            this.matchOffset = matchOffset;
            return sendHostRegRequest(HostRegRequests.HST_INV_EPC_MATCH_CFG, true, msgBuffer);
        }

        byte[] invMatchData0_63; int invMatchDataReady = 0;
        public String getInvMatchData() {
            int length = matchLength;
            String strValue = "";
            for (int i = 0; i < 16; i++) {
                if (length > 0) {
                    if ((invMatchDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0x12, 9, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        sendHostRegRequest(HostRegRequests.HST_INV_EPCDAT_0_3, false, msgBuffer);

                        strValue = null;
                        break;
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", invMatchData0_63[i * 4 + j]);
                        }
                    }
                    length -= 32;
                }
            }
            return strValue;
        }
        public boolean setInvMatchData(String matchData) {
            int length = matchData.length();
            for (int i = 0; i < 16; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0x12, 9, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= matchData.length()) {
                            String subString = matchData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
                            if ((j / 2) * 2 == j) {
                                msgBuffer[4 + j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[4 + j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (sendHostRegRequest(HostRegRequests.HST_INV_EPCDAT_0_3, true, msgBuffer) == false)
                        return false;
                    else {
                        invMatchDataReady |= (0x01 << i);
                        System.arraycopy(msgBuffer, 4, invMatchData0_63, i * 4, 4); //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
//                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
                    }
                }
            }
            return true;
        }

        //Tag access block parameters
        boolean accessVerfiy;
        final int ACCRETRY_INVALID = -1; final int ACCRETRY_MIN = 0; final int ACCRETRY_MAX = 7;
        int accessRetry = ACCRETRY_INVALID;
        int getAccessRetry() {
            if (accessRetry < ACCRETRY_MIN || accessRetry > ACCRETRY_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, (byte) 0x0A, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGACC_DESC_CFG, false, msgBuffer);
            }
            return accessRetry;
        }
        public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 0x0A, 0, 0, 0, 0};
            if (DEBUG) appendToLog("setAccessRetry[" + accessVerfiy + ", " + accessRetry + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessVerfiy = " + this.accessVerfiy + ", accessRetry = " + this.accessRetry);
            if (accessRetry < ACCRETRY_MIN || accessRetry > ACCRETRY_MAX)
                accessRetry = mDefault.accessRetry;
            if (this.accessVerfiy == accessVerfiy && this.accessRetry == accessRetry && sameCheck) return true;
            msgBuffer[4] |= (byte) (accessRetry << 1);
            if (accessVerfiy)   msgBuffer[4] |= 0x01;
            this.accessVerfiy = accessVerfiy;
            this.accessRetry = accessRetry;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_DESC_CFG, true, msgBuffer);
        }

        final int ACCBANK_INVALID = -1; final int ACCBANK_MIN = 0; final int ACCBANK_MAX = 3;
        int accessBank = ACCBANK_INVALID; int accessBank2 = ACCBANK_INVALID;
        int getAccessBank() {
            if (accessBank < ACCBANK_MIN || accessBank > ACCBANK_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, (byte) 0x0A, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGACC_BANK, false, msgBuffer);
            }
            return accessBank;
        }
        public boolean setAccessBank(int accessBank) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 0x0A, 0, 0, 0, 0};
            if (DEBUG) appendToLog("setAccessBank[" + accessBank + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessBank = " + this.accessBank);
            if (accessBank < ACCBANK_MIN || accessBank > ACCBANK_MAX)
                accessBank = mDefault.accessBank;
            if (this.accessBank == accessBank && this.accessBank2 == 0 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessBank & 0x03);
            this.accessBank = accessBank; this.accessBank2 = 0;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_BANK, true, msgBuffer);
        }
        public boolean setAccessBank(int accessBank, int accessBank2) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 0x0A, 0, 0, 0, 0};
            if (DEBUG) appendToLog("setAccessBank[" + accessBank + ", " + accessBank2 + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessBank = " + this.accessBank + ", " + this.accessBank2);
            if (tagRead != 2)  accessBank2 = 0;
            if (accessBank < ACCBANK_MIN || accessBank > ACCBANK_MAX)
                accessBank = mDefault.accessBank;
            if (accessBank2 < ACCBANK_MIN || accessBank2 > ACCBANK_MAX)
                accessBank2 = mDefault.accessBank2;
            if (this.accessBank == accessBank && this.accessBank2 == accessBank2 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessBank & 0x03);
            msgBuffer[4] |= (byte) ((accessBank2 & 0x03) << 2);
            this.accessBank = accessBank; this.accessBank2 = accessBank2;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_BANK, true, msgBuffer);
        }

        final int ACCOFFSET_INVALID = -1; final int ACCOFFSET_MIN = 0; final int ACCOFFSET_MAX = 0xFFFF;
        int accessOffset = ACCOFFSET_INVALID; int accessOffset2 = ACCOFFSET_INVALID;
        int getAccessOffset() {
            if (accessOffset < ACCOFFSET_MIN || accessOffset > ACCOFFSET_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, (byte) 0x0A, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGACC_PTR, false, msgBuffer);
            }
            return accessOffset;
        }
        public boolean setAccessOffset(int accessOffset) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0A, 0, 0, 0, 0};
            if (false) appendToLog("setAccessOffset[" + accessOffset + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessOffset = " + this.accessOffset);
            if (accessOffset < ACCOFFSET_MIN || accessOffset > ACCOFFSET_MAX)
                accessOffset = mDefault.accessOffset;
            if (this.accessOffset == accessOffset && this.accessOffset2 == 0 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessOffset & 0xFF);
            msgBuffer[5] = (byte) ((accessOffset >> 8) & 0xFF);
            msgBuffer[6] = (byte) ((accessOffset >> 16) & 0xFF);
            msgBuffer[7] = (byte) ((accessOffset >> 24) & 0xFF);
            this.accessOffset = accessOffset; this.accessOffset2 = 0;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_PTR, true, msgBuffer);
        }
        public boolean setAccessOffset(int accessOffset, int accessOffset2) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0A, 0, 0, 0, 0};
            if (false) appendToLog("setAccessOffset[" + accessOffset + ", " + accessOffset2 + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessOffset = " + this.accessOffset + ", " + this.accessOffset2);
            if (tagRead != 2)   accessOffset2 = 0;
            if (accessOffset < ACCOFFSET_MIN || accessOffset > ACCOFFSET_MAX)
                accessOffset = mDefault.accessOffset;
            if (accessOffset2 < ACCOFFSET_MIN || accessOffset2 > ACCOFFSET_MAX)
                accessOffset2 = mDefault.accessOffset2;
            if (this.accessOffset == accessOffset && this.accessOffset2 == accessOffset2 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessOffset & 0xFF);
            msgBuffer[5] = (byte) ((accessOffset >> 8) & 0xFF);
            msgBuffer[6] = (byte) (accessOffset2 & 0xFF);
            msgBuffer[7] = (byte) ((accessOffset2 >> 8) & 0xFF);
            this.accessOffset = accessOffset; this.accessOffset2 = accessOffset2;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_PTR, true, msgBuffer);
        }

        final int ACCCOUNT_INVALID = -1; final int ACCCOUNT_MIN = 0; final int ACCCOUNT_MAX = 255;
        int accessCount = ACCCOUNT_INVALID; int accessCount2 = ACCCOUNT_INVALID;
        public int getAccessCount() {
            if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, (byte) 0x0A, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGACC_CNT, false, msgBuffer);
            }
            return accessCount;
        }
        public boolean setAccessCount(int accessCount) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 0x0A, 0, 0, 0, 0};
            if (DEBUG) appendToLog("BtDataOut: SetAccessCount[" + accessCount + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessCount = " + this.accessCount);
            if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX)
                accessCount = mDefault.accessCount;
            if ((this.accessCount == accessCount || accessCount == -1) && this.accessCount2 == 0 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessCount & 0xFF);
            this.accessCount = accessCount; this.accessCount2 = 0;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_CNT, true, msgBuffer);
        }
        public boolean setAccessCount(int accessCount, int accessCount2) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 0x0A, 0, 0, 0, 0};
            if (DEBUG) appendToLog("setAccessCount[" + accessCount + ", " + accessCount2 + "] with tagRead = " + tagRead + ", sameCheck = " + sameCheck + ", old accessCount = " + this.accessCount + ", " + this.accessCount2);
            if (tagRead != 2)   accessCount2 = 0;
            if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX)
                accessCount = mDefault.accessCount;
            if (accessCount2 < ACCCOUNT_MIN || accessCount2 > ACCCOUNT_MAX)
                accessCount2 = mDefault.accessCount2;
            if (this.accessCount == accessCount && this.accessCount2 == accessCount2 && sameCheck) return true;
            msgBuffer[4] = (byte) (accessCount & 0xFF);
            msgBuffer[5] = (byte) (accessCount2 & 0xFF);
            this.accessCount = accessCount; this.accessCount2 = accessCount2;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_CNT, true, msgBuffer);
        }

        final int ACCLOCKACTION_INVALID = -1; final int ACCLOCKACTION_MIN = 0; final int ACCLOCKACTION_MAX = 0x3FF;
        int accessLockAction = ACCLOCKACTION_INVALID;
        int getAccessLockAction() {
            if (accessLockAction < ACCLOCKACTION_MIN || accessLockAction > ACCLOCKACTION_MAX)
                getHST_TAGACC_LOCKCFG();
            return accessLockAction;
        }
        boolean setAccessLockAction(int accessLockAction) {
            return setAccessLockAction(accessLockAction, accessLockMask);
        }

        final int ACCLOCKMASK_INVALID = -1; final int ACCLOCKMASK_MIN = 0; final int ACCLOCKMASK_MAX = 0x3FF;
        int accessLockMask = ACCLOCKMASK_INVALID;
        int getAccessLockMask() {
            if (accessLockMask < ACCLOCKMASK_MIN || accessLockMask > ACCLOCKMASK_MAX)
                getHST_TAGACC_LOCKCFG();
            return accessLockMask;
        }
        boolean setAccessLockMask(int accessLockMask) {
            return setAccessLockAction(accessLockAction, accessLockMask);
        }

        boolean getHST_TAGACC_LOCKCFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, (byte) 0x0A, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_LOCKCFG, false, msgBuffer);
        }
        public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 0x0A, 0, 0, 0, 0};
            if (accessLockAction < ACCLOCKACTION_MIN || accessLockAction > ACCLOCKACTION_MAX)
                accessLockAction = mDefault.accessLockAction;
            if (accessLockMask < ACCLOCKMASK_MIN || accessLockMask > ACCLOCKMASK_MAX)
                accessLockMask = mDefault.accessLockMask;
            if (this.accessLockAction == accessLockAction && this.accessLockMask == accessLockMask && sameCheck) return true;
            msgBuffer[4] = (byte) (accessLockAction & 0xFF);
            msgBuffer[5] |= (byte) ((accessLockAction & 0x3FF) >> 8);

            msgBuffer[5] |= (byte) ((accessLockMask & 0x3F) << 2);
            msgBuffer[6] |= (byte) ((accessLockMask & 0x3FF) >> 6);
            this.accessLockAction = accessLockAction;
            this.accessLockMask = accessLockMask;
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_LOCKCFG, true, msgBuffer);
        }

        final int ACCPWD_INVALID = 0; final long ACCPWD_MIN = 0; final long ACCPWD_MAX = 0x0FFFFFFFF;
        String stringAccessPasword = "00000000";
        public boolean setRx000AccessPassword(String password) {
            //appendToLog("BtDataOut: setRx000AccessPassword with password = " + password + ", stringAccessPasword = " + stringAccessPasword);
            if (stringAccessPasword.matches(password) && sameCheck) return true;
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 6, (byte) 0x0A, 0, 0, 0, 0};
            if (password == null) password = "";
            String hexString = "0123456789ABCDEF";
            for (int j = 0; j < 16; j++) {
                if (j + 1 <= password.length()) {
                    String subString = password.substring(j, j + 1).toUpperCase();
                    int k = 0;
                    for (k = 0; k < 16; k++) {
                        if (subString.matches(hexString.substring(k, k + 1))) {
                            break;
                        }
                    }
                    if (k == 16) return false;
                    if ((j / 2) * 2 == j) {
                        msgBuffer[7 - j / 2] |= (byte) (k << 4);
                    } else {
                        msgBuffer[7 - j / 2] |= (byte) (k);
                    }
                }
            }
            boolean retValue = sendHostRegRequest(HostRegRequests.HST_TAGACC_ACCPWD, true, msgBuffer);
            if (DEBUG) appendToLog("sendHostRegRequest(): retValue = " + retValue);
            if (retValue) stringAccessPasword = password;
            return retValue;
        }

        public boolean setRx000KillPassword(String password) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 7, (byte) 0x0A, 0, 0, 0, 0};
            String hexString = "0123456789ABCDEF";
            for (int j = 0; j < 16; j++) {
                if (j + 1 <= password.length()) {
                    String subString = password.substring(j, j + 1).toUpperCase();
                    int k = 0;
                    for (k = 0; k < 16; k++) {
                        if (subString.matches(hexString.substring(k, k + 1))) {
                            break;
                        }
                    }
                    if (k == 16) return false;
                    if ((j / 2) * 2 == j) {
                        msgBuffer[7 - j / 2] |= (byte) (k << 4);
                    } else {
                        msgBuffer[7 - j / 2] |= (byte) (k);
                    }
                }
            }
            boolean retValue = sendHostRegRequest(HostRegRequests.HST_TAGACC_KILLPWD, true, msgBuffer);
            if (DEBUG) appendToLog("sendHostRegRequest(): retValue = " + retValue);
            return retValue;
        }

        final int ACCWRITEDATSEL_INVALID = -1; final int ACCWRITEDATSEL_MIN = 0; final int ACCWRITEDATSEL_MAX = 7;
        int accessWriteDataSelect = ACCWRITEDATSEL_INVALID;
        int getAccessWriteDataSelect() {
            if (accessWriteDataSelect < ACCWRITEDATSEL_MIN || accessWriteDataSelect > ACCWRITEDATSEL_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 8, (byte) 0x0A, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_SEL, false, msgBuffer);
            }
            return accessWriteDataSelect;
        }
        boolean setAccessWriteDataSelect(int accessWriteDataSelect) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, 0x0A, 0, 0, 0, 0};
            if (accessWriteDataSelect < ACCWRITEDATSEL_MIN || accessWriteDataSelect > ACCWRITEDATSEL_MAX)
                accessWriteDataSelect = mDefault.accessWriteDataSelect;
            if (this.accessWriteDataSelect == accessWriteDataSelect && sameCheck) return true;
            accWriteDataReady = 0;
            msgBuffer[4] = (byte) (accessWriteDataSelect & 0x07);
            this.accessWriteDataSelect = accessWriteDataSelect;
            return sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_SEL, true, msgBuffer);
        }

        byte[] accWriteData0_63; int accWriteDataReady = 0;
        String getAccessWriteData() {
            int length = accessCount;
            if (length > 32) {
                length = 32;
            }
            String strValue = "";
            for (int i = 0; i < 32; i++) {
                if (length > 0) {
                    if ((accWriteDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, (byte) 0x0A, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_0, false, msgBuffer);

                        strValue = null;
                        break;
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", accWriteData0_63[i * 4 + j]);
                        }
                    }
                    length -= 2;
                }
            }
            return strValue;
        }

        public boolean setAccessWriteData(String dataInput) {
            dataInput = dataInput.trim();
            int writeBufLength = 16 * 2; //16
            int wrieByteSize = 4;   //8
            int length = dataInput.length(); appendToLog("length = " + length);
            if (length > wrieByteSize * writeBufLength) { appendToLog("1"); return false; }
            for (int i = 0; i < writeBufLength; i++) {
                if (length > 0) {
                    length -= wrieByteSize;
                    if ((i / 16) * 16 == i) {
                        if (true) {
                            if (setAccessWriteDataSelect(i/16) == false) return false;
                        }
                        else {
                            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, (byte) 0x0A, 0, 0, 0, 0};
                            msgBuffer[4] = (byte) (i / 16);
                            if (sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_SEL, true, msgBuffer) == false) {
                                appendToLog("23");
                                return false;
                            }
                        }
                    }
                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 9, (byte) 0x0A, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < wrieByteSize; j++) {
//                        if (i * wrieByteSize + j + 1 <= dataInput.length()) {
                        appendToLog("dataInput = " + dataInput + ", i = " + i + ", wrieByteSize = " + wrieByteSize + ", j = " + j);
                        if (i * wrieByteSize + j >= dataInput.length()) break;
                        String subString = dataInput.substring(i * wrieByteSize + j, i * wrieByteSize + j + 1).toUpperCase();
                        appendToLog("subString = " + subString);
                        if (DEBUG) appendToLog(subString);
                        int k = 0;
                        for (k = 0; k < 16; k++) {
                            if (subString.matches(hexString.substring(k, k + 1))) {
                                break;
                            }
                        }
                        if (k == 16) { appendToLog("2: i= " + i + ", j=" + j + ", subString = " + subString); return false; }
                        if ((j / 2) * 2 == j) {
                            msgBuffer[5- j / 2] |= (byte) (k << 4);
                        } else {
                            msgBuffer[5 - j / 2] |= (byte) (k);
                        }
//                        }
                    }
                    appendToLog("complete 4 bytes: " + byteArrayToString(msgBuffer));
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + (i % 16));
                    if (wrieByteSize == 4) {
                        msgBuffer[6] = (byte)(i);
                    }
                    byte[] debugBuffer = new byte[4];
                    int k = 0;
                    for (; k < 4; k++) {
                        debugBuffer[k] = accWriteData0_63[i * 4 + k];
                        if (accWriteData0_63[i * 4 + k] != msgBuffer[7 - k]) break;
                    }
                    if (k != 4) {
                        appendToLog("BtDataOut: RfidReaderChipR2000.setAccessWriteData msgBuffer = " + byteArrayToString(msgBuffer) + ", debugBuffer = " + byteArrayToString(debugBuffer));
                        if (sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_0, true, msgBuffer) == false) {
                            appendToLog("3");
                            return false;
                        } else {
                            rx000Setting.accWriteDataReady |= (0x01 << i);
                            if (DEBUG) appendToLog("accWriteReady=" + accWriteDataReady);
                            for (int k1 = 0; k1 < 4; k1++) {
                                accWriteData0_63[i * 4 + k1] = msgBuffer[7 - k1];
                            }
                            if (DEBUG) appendToLog("Data=" + byteArrayToString(accWriteData0_63));
                        }
                    }
                } else break;
            }
            return true;
        }

        //RFTC block paramters
        final int PROFILE_INVALID = -1; final int PROFILE_MIN = 0; final int PROFILE_MAX = 5;   //profile 4 and 5 are custom profiles.
        int currentProfile = PROFILE_INVALID;
        public int getCurrentProfile() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                if (currentProfile < PROFILE_MIN || currentProfile > PROFILE_MAX) {
                    byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0x60, 0x0B, 0, 0, 0, 0};
                    sendHostRegRequest(HostRegRequests.HST_RFTC_CURRENT_PROFILE, false, msgBuffer);
                }
                return currentProfile;
            }
        }

        public boolean setCurrentProfile(int currentProfile) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) return false;
            else {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0x60, 0x0B, 0, 0, 0, 0};
                if (currentProfile < PROFILE_MIN || currentProfile > PROFILE_MAX)
                    currentProfile = mDefault.currentProfile;
                if (this.currentProfile == currentProfile && sameCheck) return true;
                msgBuffer[4] = (byte) (currentProfile);
                this.currentProfile = currentProfile;
                return sendHostRegRequest(HostRegRequests.HST_RFTC_CURRENT_PROFILE, true, msgBuffer);
            }
        }

        final int COUNTRYENUM_INVALID = -1; final int COUNTRYENUM_MIN = 1; final int COUNTRYENUM_MAX = 109;
        final int COUNTRYCODE_INVALID = -1; final int COUNTRYCODE_MIN = 1; final int COUNTRYCODE_MAX = 9;
        int countryEnumOem = COUNTRYENUM_INVALID; int countryEnum = COUNTRYENUM_INVALID; int countryCode = COUNTRYCODE_INVALID;   // OemAddress = 0x02
        String modelCode = null;

        final int FREQCHANSEL_INVALID = -1; final int FREQCHANSEL_MIN = 0; final int FREQCHANSEL_MAX = 49;
        int freqChannelSelect = FREQCHANSEL_INVALID;
        public int getFreqChannelSelect() {
            appendToLog("freqChannelSelect = " + freqChannelSelect);
            if (freqChannelSelect < FREQCHANSEL_MIN || freqChannelSelect > FREQCHANSEL_MAX) {
                {
                    byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 0x0C, 0, 0, 0, 0};
                    sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_SEL, false, msgBuffer);
                }
            }
            return freqChannelSelect;
        }
        public boolean setFreqChannelSelect(int freqChannelSelect) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 0x0C, 0, 0, 0, 0};
            if (freqChannelSelect < FREQCHANSEL_MIN || freqChannelSelect > FREQCHANSEL_MAX)   freqChannelSelect = mDefault.freqChannelSelect;
            //if (this.freqChannelSelect == freqChannelSelect && sameCheck)  return true;
            appendToLog("freqChannelSelect = " + freqChannelSelect);
            msgBuffer[4] = (byte) (freqChannelSelect);
            this.freqChannelSelect = freqChannelSelect;
            freqChannelSelect = FREQCHANCONFIG_INVALID; freqPllMultiplier = FREQPLLMULTIPLIER_INVALID;
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_SEL, true, msgBuffer);
        }

        final int FREQCHANCONFIG_INVALID = -1; final int FREQCHANCONFIG_MIN = 0; final int FREQCHANCONFIG_MAX = 1;
        int freqChannelConfig = FREQCHANCONFIG_INVALID;
        public int getFreqChannelConfig() {
            if (freqChannelConfig < FREQCHANCONFIG_MIN || freqChannelConfig > FREQCHANCONFIG_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 0x0C, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_CFG, false, msgBuffer);
            }
            return freqChannelConfig;
        }
        public boolean setFreqChannelConfig(boolean on) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 0x0C, 0, 0, 0, 0};
            boolean onCurrent = false;
            if (freqChannelConfig != 0) onCurrent = true;
//            if (onCurrent == on && sameCheck)  return true;
            if (on) {
                msgBuffer[4] = 1;
                freqChannelConfig = 1;
            } else {
                freqChannelConfig = 0;
            }
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_CFG, true, msgBuffer);
        }

        final int FREQPLLMULTIPLIER_INVALID = -1;
        int freqPllMultiplier = FREQPLLMULTIPLIER_INVALID;
        int getFreqPllMultiplier() {
            if (freqPllMultiplier == FREQPLLMULTIPLIER_INVALID) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 0x0C, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, false, msgBuffer);
            }
            return freqPllMultiplier;
        }
        public boolean setFreqPllMultiplier(int freqPllMultiplier) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0C, 0, 0, 0, 0};
            msgBuffer[4] = (byte)(freqPllMultiplier & 0xFF);
            msgBuffer[5] = (byte)((freqPllMultiplier >> 8) & 0xFF);
            msgBuffer[6] = (byte)((freqPllMultiplier >> 16) & 0xFF);
            msgBuffer[7] = (byte)((freqPllMultiplier >> 24) & 0xFF);
            this.freqPllMultiplier = freqPllMultiplier;
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, true, msgBuffer);
        }

        final int FREQPLLDAC_INVALID = -1;
        int freqPllDac = FREQPLLDAC_INVALID;
        int getFreqPllDac() {
            if (freqPllDac == FREQPLLDAC_INVALID) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 0x0C, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDACCTL, false, msgBuffer);
            }
            return freqPllDac;
        }

        boolean setFreqChannelOverride(int freqStart) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, 0x0C, 0, 0, 0, 0};
            msgBuffer[4] = (byte)(freqStart & 0xFF);
            msgBuffer[5] = (byte)((freqStart >> 8) & 0xFF);
            msgBuffer[6] = (byte)((freqStart >> 16) & 0xFF);
            msgBuffer[7] = (byte)((freqStart >> 24) & 0xFF);
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_CMDSTART, true, msgBuffer);
        }
    }
    class AntennaSelectedData {
        AntennaSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0)    default_setting_type = 0;
            if (default_setting_type > 5)    default_setting_type = 5;
            mDefault = new AntennaSelectedData_default(default_setting_type);
            if (false && set_default_setting) {
                antennaEnable = mDefault.antennaEnable;
                antennaInventoryMode = mDefault.antennaInventoryMode;
                antennaLocalAlgo = mDefault.antennaLocalAlgo;
                antennaLocalStartQ = mDefault.antennaLocalStartQ;
                antennaProfileMode = mDefault.antennaProfileMode;
                antennaLocalProfile = mDefault.antennaLocalProfile;
                antennaFrequencyMode = mDefault.antennaFrequencyMode;
                antennaLocalFrequency = mDefault.antennaLocalFrequency;
                antennaStatus = mDefault.antennaStatus;
                antennaDefine = mDefault.antennaDefine;
                antennaDwell = mDefault.antennaDwell;
                antennaPower = mDefault.antennaPower; appendToLog("antennaPower is set to default " + antennaPower);
                antennaInvCount = mDefault.antennaInvCount;
            }
        }

        class AntennaSelectedData_default {
            AntennaSelectedData_default(int set_default_setting) {
                antennaEnable = mDefaultArray.antennaEnable[set_default_setting];
                antennaInventoryMode = mDefaultArray.antennaInventoryMode[set_default_setting];
                antennaLocalAlgo = mDefaultArray.antennaLocalAlgo[set_default_setting];
                antennaLocalStartQ = mDefaultArray.antennaLocalStartQ[set_default_setting];
                antennaProfileMode = mDefaultArray.antennaProfileMode[set_default_setting];
                antennaLocalProfile = mDefaultArray.antennaLocalProfile[set_default_setting];
                antennaFrequencyMode = mDefaultArray.antennaFrequencyMode[set_default_setting];
                antennaLocalFrequency = mDefaultArray.antennaLocalFrequency[set_default_setting];
                antennaStatus = mDefaultArray.antennaStatus[set_default_setting];
                antennaDefine = mDefaultArray.antennaDefine[set_default_setting];
                antennaDwell = mDefaultArray.antennaDwell[set_default_setting];
                antennaPower = mDefaultArray.antennaPower[set_default_setting];
                antennaInvCount = mDefaultArray.antennaInvCount[set_default_setting];
            }

            int antennaEnable;
            int antennaInventoryMode;
            int antennaLocalAlgo;
            int antennaLocalStartQ;
            int antennaProfileMode;
            int antennaLocalProfile;
            int antennaFrequencyMode;
            int antennaLocalFrequency;
            int antennaStatus;
            int antennaDefine;
            long antennaDwell;
            long antennaPower;
            long antennaInvCount;
        }
        AntennaSelectedData_default mDefault;

        private class AntennaSelectedData_defaultArray { //0 for invalid default,    1  for 0,       2 for 1 to 3,       3 for 4 to 7,       4 for 8 to   11,        5 for 12 to 15
            int[] antennaEnable =         { -1, 1, 0, 0, 0, 0 };
            int[] antennaInventoryMode = { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalAlgo =     { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalStartQ =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaProfileMode =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalProfile =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaFrequencyMode = { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalFrequency = { -1, 0, 0, 0, 0, 0 };
            int[] antennaStatus =           { -1, 0, 0, 0, 0, 0 };
            int[] antennaDefine =         { -1, 0, 0, 1, 2, 3 };
            long[] antennaDwell =      { -1, 2000, 2000, 2000, 2000, 2000 };
            long[] antennaPower =       { -1, 300, 0, 0, 0, 0 };
            long[] antennaInvCount =   { -1, 8192, 8192, 8192, 8192, 8192 };
        }
        AntennaSelectedData_defaultArray mDefaultArray = new AntennaSelectedData_defaultArray();

        final int ANTENABLE_INVALID = -1; final int ANTENABLE_MIN = 0; final int ANTENABLE_MAX = 1;
        int antennaEnable = ANTENABLE_INVALID;
        int getAntennaEnable() {
            if (antennaEnable < ANTENABLE_MIN || antennaEnable > ANTENABLE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaEnable;
        }
        boolean setAntennaEnable(int antennaEnable) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTINVMODE_INVALID = 0; final int ANTINVMODE_MIN = 0; final int ANTINVMODE_MAX = 1;
        int antennaInventoryMode = ANTINVMODE_INVALID;
        int getAntennaInventoryMode() {
            if (antennaInventoryMode < ANTPROFILEMODE_MIN || antennaInventoryMode > ANTPROFILEMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaInventoryMode;
        }
        boolean setAntennaInventoryMode(int antennaInventoryMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ, antennaProfileMode,
                    antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALALGO_INVALID = 0; final int ANTLOCALALGO_MIN = 0; final int ANTLOCALALGO_MAX = 5;
        int antennaLocalAlgo = ANTLOCALALGO_INVALID;
        int getAntennaLocalAlgo() {
            if (antennaLocalAlgo < ANTLOCALALGO_MIN || antennaLocalAlgo > ANTLOCALALGO_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalAlgo;
        }
        boolean setAntennaLocalAlgo(int antennaLocalAlgo) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALSTARTQ_INVALID = 0; final int ANTLOCALSTARTQ_MIN = 0; final int ANTLOCALSTARTQ_MAX = 15;
        int antennaLocalStartQ = ANTLOCALSTARTQ_INVALID;
        int getAntennaLocalStartQ() {
            if (antennaLocalStartQ < ANTLOCALSTARTQ_MIN || antennaLocalStartQ > ANTLOCALSTARTQ_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalStartQ;
        }
        boolean setAntennaLocalStartQ(int antennaLocalStartQ) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTPROFILEMODE_INVALID = 0; final int ANTPROFILEMODE_MIN = 0; final int ANTPROFILEMODE_MAX = 1;
        int antennaProfileMode = ANTPROFILEMODE_INVALID;
        int getAntennaProfileMode() {
            if (antennaProfileMode < ANTPROFILEMODE_MIN || antennaProfileMode > ANTPROFILEMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaProfileMode;
        }
        boolean setAntennaProfileMode(int antennaProfileMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALPROFILE_INVALID = 0; final int ANTLOCALPROFILE_MIN = 0; final int ANTLOCALPROFILE_MAX = 5;
        int antennaLocalProfile = ANTLOCALPROFILE_INVALID;
        int getAntennaLocalProfile() {
            if (antennaLocalProfile < ANTLOCALPROFILE_MIN || antennaLocalProfile > ANTLOCALPROFILE_MIN)
                getHST_ANT_DESC_CFG();
            return antennaLocalProfile;
        }
        boolean setAntennaLocalProfile(int antennaLocalProfile) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTFREQMODE_INVALID = 0; final int ANTFREQMODE_MIN = 0; final int ANTFREQMODE_MAX = 1;
        int antennaFrequencyMode = ANTFREQMODE_INVALID;
        int getAntennaFrequencyMode() {
            if (antennaFrequencyMode < ANTFREQMODE_MIN || antennaFrequencyMode > ANTFREQMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaFrequencyMode;
        }
        boolean setAntennaFrequencyMode(int antennaFrequencyMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALFREQ_INVALID = 0; final int ANTLOCALFREQ_MIN = 0; final int ANTLOCALFREQ_MAX = 49;
        int antennaLocalFrequency = ANTLOCALFREQ_INVALID;
        int getAntennaLocalFrequency() {
            if (antennaLocalFrequency < ANTLOCALFREQ_MIN || antennaLocalFrequency > ANTLOCALFREQ_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalFrequency;
        }
        boolean setAntennaLocalFrequency(int antennaLocalFrequency) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        private boolean getHST_ANT_DESC_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 7, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_CFG, false, msgBuffer);
        }
        boolean setAntennaEnable(int antennaEnable, int antennaInventoryMode, int antennaLocalAlgo, int antennaLocalStartQ,
                                 int antennaProfileMode, int antennaLocalProfile,
                                 int antennaFrequencyMode, int antennaLocalFrequency) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 7, 0, 0, 0, 0};
            if (antennaEnable < ANTENABLE_MIN || antennaEnable > ANTENABLE_MAX)
                antennaEnable = mDefault.antennaEnable;
            if (antennaInventoryMode < ANTINVMODE_MIN || antennaInventoryMode > ANTINVMODE_MAX)
                antennaInventoryMode = mDefault.antennaInventoryMode;
            if (antennaLocalAlgo < ANTLOCALALGO_MIN || antennaLocalAlgo > ANTLOCALALGO_MAX)
                antennaLocalAlgo = mDefault.antennaLocalAlgo;
            if (antennaLocalStartQ < ANTLOCALSTARTQ_MIN || antennaLocalStartQ > ANTLOCALSTARTQ_MAX)
                antennaLocalStartQ = mDefault.antennaLocalStartQ;
            if (antennaProfileMode < ANTPROFILEMODE_MIN || antennaProfileMode > ANTPROFILEMODE_MAX)
                antennaProfileMode = mDefault.antennaProfileMode;
            if (antennaLocalProfile < ANTLOCALPROFILE_MIN || antennaLocalProfile > ANTLOCALPROFILE_MAX)
                antennaLocalProfile = mDefault.antennaLocalProfile;
            if (antennaFrequencyMode < ANTFREQMODE_MIN || antennaFrequencyMode > ANTFREQMODE_MAX)
                antennaFrequencyMode = mDefault.antennaFrequencyMode;
            if (antennaLocalFrequency < ANTLOCALFREQ_MIN || antennaLocalFrequency > ANTLOCALFREQ_MAX)
                antennaLocalFrequency = mDefault.antennaLocalFrequency;
            if (this.antennaEnable == antennaEnable && this.antennaInventoryMode == antennaInventoryMode && this.antennaLocalAlgo == antennaLocalAlgo
                    && this.antennaLocalStartQ == antennaLocalStartQ && this.antennaProfileMode == antennaProfileMode && this.antennaLocalProfile == antennaLocalProfile
                    && this.antennaFrequencyMode == antennaFrequencyMode && this.antennaLocalFrequency == antennaLocalFrequency
                    && sameCheck)
                return true;
            msgBuffer[4] |= antennaEnable;
            msgBuffer[4] |= (antennaInventoryMode << 1);
            msgBuffer[4] |= (antennaLocalAlgo << 2);
            msgBuffer[4] |= (antennaLocalStartQ << 4);
            msgBuffer[5] |= antennaProfileMode;
            msgBuffer[5] |= (antennaLocalProfile << 1);
            msgBuffer[5] |= (antennaFrequencyMode << 5);
            msgBuffer[5] |= ((antennaLocalFrequency & 0x03) << 6);
            msgBuffer[6] |= (antennaLocalFrequency >> 2);
            this.antennaEnable = antennaEnable;
            this.antennaInventoryMode = antennaInventoryMode;
            this.antennaLocalAlgo = antennaLocalAlgo;
            this.antennaLocalStartQ = antennaLocalStartQ;
            this.antennaProfileMode = antennaProfileMode;
            this.antennaLocalProfile = antennaLocalProfile;
            this.antennaFrequencyMode = antennaFrequencyMode;
            this.antennaLocalFrequency = antennaLocalFrequency;
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_CFG, true, msgBuffer);
        }

        final int ANTSTATUS_INVALID = -1; final int ANTSTATUS_MIN = 0; final int ANTSTATUS_MAX = 0xFFFFF;
        int antennaStatus = ANTSTATUS_INVALID;
        int getAntennaStatus() {
            if (antennaStatus < ANTSTATUS_MIN || antennaStatus > ANTSTATUS_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.MAC_ANT_DESC_STAT, false, msgBuffer);
            }
            return antennaStatus;
        }

        final int ANTDEFINE_INVALID = -1; final int ANTDEFINE_MIN = 0; final int ANTDEFINE_MAX = 3;
        int antennaDefine = ANTDEFINE_INVALID;
        int getAntennaDefine() {
            if (antennaDefine < ANTDEFINE_MIN || antennaDefine > ANTDEFINE_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_PORTDEF, false, msgBuffer);
            }
            return antennaDefine;
        }

        final long ANTDWELL_INVALID = -1; final long ANTDWELL_MIN = 0; final long ANTDWELL_MAX = 0xFFFF;
        long antennaDwell = ANTDWELL_INVALID;
        long getAntennaDwell() {
            if (antennaDwell < ANTDWELL_MIN || antennaDwell > ANTDWELL_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_DWELL, false, msgBuffer);
            }
            return antennaDwell;
        }
        boolean setAntennaDwell(long antennaDwell) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 7, 0, 0, 0, 0};
            if (antennaDwell < ANTDWELL_MIN || antennaDwell > ANTDWELL_MAX)
                antennaDwell = mDefault.antennaDwell;
            if (this.antennaDwell == antennaDwell && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaDwell % 256);
            msgBuffer[5] = (byte) ((antennaDwell >> 8) % 256);
            msgBuffer[6] = (byte) ((antennaDwell >> 16) % 256);
            msgBuffer[7] = (byte) ((antennaDwell >> 24) % 256);
            this.antennaDwell = antennaDwell;
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_DWELL, true, msgBuffer);
        }

        final int ANTARGET_INVALID = -1; final int ANTARGET_MIN = 0; final int ANTARGET_MAX = 1;
        int antennaTarget = ANTARGET_INVALID;
        byte[] antennaInventoryRoundControl = null;
        final int ANTOGGLE_INVALID = -1; final int ANTOGGLE_MIN = 0; final int ANTOGGLE_MAX = 100;
        int antennaToggle = ANTOGGLE_INVALID;
        final int ANTRFMODE_INVALID = -1; final int ANTRFMODE_MIN = 1; final int ANTRFMODE_MAX = 15;
        int antennaRfMode = ANTRFMODE_INVALID;

        final long ANTPOWER_INVALID = -1; final long ANTPOWER_MIN = 0; final long ANTPOWER_MAX = 330; //Maximum 330\
        long antennaPower = ANTPOWER_INVALID;   //default value = 300
        long getAntennaPower() {
            if (antennaPower < ANTPOWER_MIN || antennaPower > ANTPOWER_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 6, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_RFPOWER, false, msgBuffer);
            }
            return antennaPower;
        }
        boolean antennaPowerSet = false;
        boolean setAntennaPower(long antennaPower) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 6, 7, 0, 0, 0, 0};
            if (antennaPower < ANTPOWER_MIN || antennaPower > ANTPOWER_MAX)
                antennaPower = mDefault.antennaPower;
            if (this.antennaPower == antennaPower && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaPower % 256);
            msgBuffer[5] = (byte) ((antennaPower >> 8) % 256);
            this.antennaPower = antennaPower;
            antennaPowerSet = true;
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_RFPOWER, true, msgBuffer);
        }

        final long ANTINVCOUNT_INVALID = -1; final long ANTINVCOUNT_MIN = 0; final long ANTINVCOUNT_MAX = 0xFFFFFFFFL;
        long antennaInvCount = ANTINVCOUNT_INVALID;
        long getAntennaInvCount() {
            if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 7, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_INV_CNT, false, msgBuffer);
            }
            return antennaInvCount;
        }
        boolean setAntennaInvCount(long antennaInvCount) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 7, 7, 0, 0, 0, 0};
            if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX)
                antennaInvCount = mDefault.antennaInvCount;
            if (this.antennaInvCount == antennaInvCount && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaInvCount % 256);
            msgBuffer[5] = (byte) ((antennaInvCount >> 8) % 256);
            msgBuffer[6] = (byte) ((antennaInvCount >> 16) % 256);
            msgBuffer[7] = (byte) ((antennaInvCount >> 24) % 256);
            this.antennaInvCount = antennaInvCount;
            return sendHostRegRequest(HostRegRequests.HST_ANT_DESC_INV_CNT, true, msgBuffer);
        }
    }
    class InvSelectData {
        InvSelectData(boolean set_default_setting) {
            if (set_default_setting) {
                selectEnable = mDefault.selectEnable;
                selectTarget = mDefault.selectTarget;
                selectAction = mDefault.selectAction;
                selectDelay = mDefault.selectDelay;
                selectMaskBank = mDefault.selectMaskBank;
                selectMaskOffset = mDefault.selectMaskOffset;
                selectMaskLength = mDefault.selectMaskLength;
                selectMaskDataReady = mDefault.selectMaskDataReady;
                //Log.i("Hello", "BtDataOut 123b");
            }
        }

        private class InvSelectData_default {
            int selectEnable = 0;
            int selectTarget = 0;
            int selectAction = 0;
            int selectDelay = 0;
            int selectMaskBank = 0;
            int selectMaskOffset = 0;
            int selectMaskLength = 0;
            byte[] selectMaskData0_31 = new byte[4 * 8]; byte selectMaskDataReady = 0;
        }
        InvSelectData_default mDefault = new InvSelectData_default();

        final int INVSELENABLE_INVALID = 0; final int INVSELENABLE_MIN = 0; final int INVSELENABLE_MAX = 1;
        int selectEnable = INVSELENABLE_INVALID;
        int getSelectEnable() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectEnable;
        }
        boolean setSelectEnable(int selectEnable) {
            appendToLog("BtDataOut: RfidReaderChipR200.setSelectEnable");
            return setRx000HostReg_HST_TAGMSK_DESC_CFG(selectEnable, this.selectTarget, this.selectAction, this.selectDelay);
        }

        final int INVSELTARGET_INVALID = -1; final int INVSELTARGET_MIN = 0; final int INVSELTARGET_MAX = 7;
        int selectTarget = INVSELTARGET_INVALID;
        int getSelectTarget() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectTarget;
        }

        final int INVSELACTION_INVALID = -1; final int INVSELACTION_MIN = 0; final int INVSELACTION_MAX = 7;
        int selectAction = INVSELACTION_INVALID;
        int getSelectAction() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectAction;
        }

        final int INVSELDELAY_INVALID = -1; final int INVSELDELAY_MIN = 0; final int INVSELDELAY_MAX = 255;
        int selectDelay = INVSELDELAY_INVALID;
        int getSelectDelay() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectDelay;
        }

        boolean getRx000HostReg_HST_TAGMSK_DESC_CFG() {
            if (selectEnable < INVSELENABLE_MIN || selectEnable > INVSELENABLE_MAX
                    || selectTarget < INVSELTARGET_MIN || selectTarget > INVSELTARGET_MAX
                    || selectAction < INVSELACTION_MIN || selectAction > INVSELACTION_MAX
                    || selectDelay < INVSELDELAY_MIN || selectDelay > INVSELDELAY_MAX
            ) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 8, 0, 0, 0, 0};
                return sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_CFG, false, msgBuffer);
            } else {
                return false;
            }
        }
        boolean setRx000HostReg_HST_TAGMSK_DESC_CFG(int selectEnable, int selectTarget, int selectAction, int selectDelay) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 8, 0, 0, 0, 0};
            if (utility.DEBUG_SELECT) appendToLog("Debug_Select: RfidReaderR2000.setRx000HostReg_HST_TAGMSK_DESC_CFG[" +
                    selectEnable + "," + selectTarget + "," + selectAction + "," + selectDelay + "] with old SelectEnable/Target/Action/Delay =" +
                    this.selectEnable + "," + this.selectTarget + "," + this.selectAction + "," + this.selectDelay);
            if (selectEnable < INVSELENABLE_MIN || selectEnable > INVSELENABLE_MAX)
                selectEnable = mDefault.selectEnable;
            if (selectTarget < INVSELTARGET_MIN || selectTarget > INVSELTARGET_MAX)
                selectTarget = mDefault.selectTarget;
            if (selectAction < INVSELACTION_MIN || selectAction > INVSELACTION_MAX)
                selectAction = mDefault.selectAction;
            int selectDalay0 = selectDelay;
            if (selectDelay < INVSELDELAY_MIN || selectDelay > INVSELDELAY_MAX)
                selectDelay = mDefault.selectDelay;
            if (this.selectEnable == selectEnable && this.selectTarget == selectTarget && this.selectAction == selectAction && this.selectDelay == selectDelay && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectEnable & 0x1);
            msgBuffer[4] |= (byte) ((selectTarget & 0x07) << 1);
            msgBuffer[4] |= (byte) ((selectAction & 0x07) << 4);
            msgBuffer[5] |= (byte) (selectDelay & 0xFF);
            this.selectEnable = selectEnable;
            this.selectTarget = selectTarget;
            this.selectAction = selectAction;
            this.selectDelay = selectDelay;
            return sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_CFG, true, msgBuffer);
        }

        final int INVSELMBANK_INVALID = -1; final int INVSELMBANK_MIN = 0; final int INVSELMBANK_MAX = 3;
        int selectMaskBank = INVSELMBANK_INVALID;
        int getSelectMaskBank() {
            if (selectMaskBank < INVSELMBANK_MIN || selectMaskBank > INVSELMBANK_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 8, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGMSK_BANK, false, msgBuffer);
            }
            return selectMaskBank;
        }
        boolean setSelectMaskBank(int selectMaskBank) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 8, 0, 0, 0, 0};
            if (selectMaskBank < INVSELMBANK_MIN || selectMaskBank > INVSELMBANK_MAX)
                selectMaskBank = mDefault.selectMaskBank;
            if (this.selectMaskBank == selectMaskBank && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskBank & 0x3);
            this.selectMaskBank = selectMaskBank;
            return sendHostRegRequest(HostRegRequests.HST_TAGMSK_BANK, true, msgBuffer);
        }

        final int INVSELMOFFSET_INVALID = -1; final int INVSELMOFFSET_MIN = 0; final int INVSELMOFFSET_MAX = 0xFFFF;
        int selectMaskOffset = INVSELMOFFSET_INVALID;
        int getSelectMaskOffset() {
            if (selectMaskOffset < INVSELMOFFSET_MIN || selectMaskOffset > INVSELMOFFSET_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 8, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, false, msgBuffer);
            }
            return selectMaskOffset;
        }
        boolean setSelectMaskOffset(int selectMaskOffset) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 8, 0, 0, 0, 0};
            if (selectMaskOffset < INVSELMOFFSET_MIN || selectMaskOffset > INVSELMOFFSET_MAX)
                selectMaskOffset = mDefault.selectMaskOffset;
            if (this.selectMaskOffset == selectMaskOffset && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskOffset & 0xFF);
            msgBuffer[5] |= (byte) ((selectMaskOffset >> 8) & 0xFF);
            this.selectMaskOffset = selectMaskOffset;
            return sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, true, msgBuffer);
        }

        final int INVSELMLENGTH_INVALID = -1; final int INVSELMLENGTH_MIN = 0; final int INVSELMLENGTH_MAX = 255;
        int selectMaskLength = INVSELMLENGTH_INVALID;
        int getSelectMaskLength() {
            appendToLog("getSelectMaskData with selectMaskLength = " + selectMaskLength);
            if (selectMaskLength < INVSELMLENGTH_MIN || selectMaskOffset > INVSELMLENGTH_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 8, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_TAGMSK_LEN, false, msgBuffer);
            }
            return selectMaskLength;
        }
        boolean setSelectMaskLength(int selectMaskLength) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 8, 0, 0, 0, 0};
            if (selectMaskLength < INVSELMLENGTH_MIN) selectMaskLength = INVSELMLENGTH_MIN;
            else if (selectMaskLength > INVSELMLENGTH_MAX) selectMaskLength = INVSELMLENGTH_MAX;
            if (this.selectMaskLength == selectMaskLength && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskLength & 0xFF);
            if (selectMaskLength == INVSELMLENGTH_MAX) msgBuffer[5] = 1;
            this.selectMaskLength = selectMaskLength;
            return sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, true, msgBuffer);
        }

        byte[] selectMaskData0_31 = new byte[4 * 8]; byte selectMaskDataReady = 0;
        String getRx000SelectMaskData() {
            if (false) appendToLog("getSelectMaskData with selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
            int length = selectMaskLength;
            String strValue = "";
            if (length < 0) {
                getSelectMaskLength();
            } else {
                for (int i = 0; i < 8; i++) {
                    if (length > 0) {
                        if ((selectMaskDataReady & (0x01 << i)) == 0) {
                            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 8, 0, 0, 0, 0};
                            msgBuffer[2] += i;
                            sendHostRegRequest(HostRegRequests.HST_TAGMSK_0_3, false, msgBuffer);

                            strValue = null;
                            break;
                        } else {
                            for (int j = 0; j < 4; j++) {
                                if (DEBUG) appendToLog("i = " + i + ", j = " + j + ", selectMaskData0_31 = " + selectMaskData0_31[i * 4 + j]);
                                strValue += String.format("%02X", selectMaskData0_31[i * 4 + j]);
                            }
                        }
                        length -= 32;
                    }
                }
            }
            return strValue;
        }
        boolean setRx000SelectMaskData(String maskData) {
            int length = maskData.length();
            for (int i = 0; i < 8; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 8, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= maskData.length()) {
                            String subString = maskData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
//                                appendToLog("setSelectMaskData(" + maskData +"): i=" + i + ", j=" + j + ", k=" + k);
                            if ((j / 2) * 2 == j) {
                                msgBuffer[4 + j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[4 + j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (sendHostRegRequest(HostRegRequests.HST_TAGMSK_0_3, true, msgBuffer) == false)
                        return false;
                    else {
                        selectMaskDataReady |= (0x01 << i);
                        if (DEBUG) appendToLog("Old selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
                        System.arraycopy(msgBuffer, 4, selectMaskData0_31, i * 4, 4);
                        if (DEBUG) appendToLog("New selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
                    }
                }
            }
            return true;
        }
    }
    class AlgoSelectedData {
        AlgoSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0) default_setting_type = 0;
            if (default_setting_type > 4) default_setting_type = 4;
            mDefault = new AlgoSelectedData_default(default_setting_type);
            if (set_default_setting) {
                algoStartQ = mDefault.algoStartQ;
                //Log.i("Hello", "BtDataOut: algoStartQ is set to default " + algoStartQ);
                algoMaxQ = mDefault.algoMaxQ;
                algoMinQ = mDefault.algoMinQ;
                algoMaxRep = mDefault.algoMaxRep;
                algoHighThres = mDefault.algoHighThres;
                algoLowThres = mDefault.algoLowThres;
                algoRetry = mDefault.algoRetry;
                algoAbFlip = mDefault.algoAbFlip;
                algoRunTilZero = mDefault.algoRunTilZero;
            }
        }

        class AlgoSelectedData_default {
            AlgoSelectedData_default(int set_default_setting) {
                algoStartQ = mDefaultArray.algoStartQ[set_default_setting];
                algoMaxQ = mDefaultArray.algoMaxQ[set_default_setting];
                algoMinQ = mDefaultArray.algoMinQ[set_default_setting];
                algoMaxRep = mDefaultArray.algoMaxRep[set_default_setting];
                algoHighThres = mDefaultArray.algoHighThres[set_default_setting];
                algoLowThres = mDefaultArray.algoLowThres[set_default_setting];
                algoRetry = mDefaultArray.algoRetry[set_default_setting];
                algoAbFlip = mDefaultArray.algoAbFlip[set_default_setting];
                algoRunTilZero = mDefaultArray.algoRunTilZero[set_default_setting];
            }

            int algoStartQ = -1;
            int algoMaxQ = -1;
            int algoMinQ = -1;
            int algoMaxRep = -1;
            int algoHighThres = -1;
            int algoLowThres = -1;
            int algoRetry = -1;
            int algoAbFlip = -1;
            int algoRunTilZero = -1;
        }
        AlgoSelectedData_default mDefault;

        class AlgoSelectedData_defaultArray { //0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
            int[] algoStartQ =     { -1, 0, 0, 0, 7 };
            int[] algoMaxQ =      { -1, 0, 0, 0, 15 };
            int[] algoMinQ =      { -1, 0, 0, 0, 0 };
            int[] algoMaxRep =    { -1, 0, 0, 0, 4 };
            int[] algoHighThres =  { -1, 0, 5, 5, 5 };
            int[] algoLowThres =  { -1, 0, 3, 3, 3 };
            int[] algoRetry =      { -1, 0, 0, 0, 0 };
            int[] algoAbFlip =     { -1, 0, 1, 1, 1 };
            int[] algoRunTilZero = { -1, 0, 0, 0, 0 };
        }
        AlgoSelectedData_defaultArray mDefaultArray = new AlgoSelectedData_defaultArray();

        final int ALGOSTARTQ_INVALID = -1; final int ALGOSTARTQ_MIN = 0; final int ALGOSTARTQ_MAX = 15;
        int algoStartQ = ALGOSTARTQ_INVALID;
        int getAlgoStartQ(boolean getInvalid) {
            if (getInvalid && (algoStartQ < ALGOSTARTQ_MIN || algoStartQ > ALGOSTARTQ_MAX)) getHST_INV_ALG_PARM_0();
            return algoStartQ;
        }
        boolean setAlgoStartQ(int algoStartQ) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMAXQ_INVALID = -1; final int ALGOMAXQ_MIN = 0; final int ALGOMAXQ_MAX = 15;
        int algoMaxQ = ALGOMAXQ_INVALID;
        int getAlgoMaxQ() {
            if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMaxQ;
        }
        boolean setAlgoMaxQ(int algoMaxQ) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMINQ_INVALID = -1; final int ALGOMINQ_MIN = 0; final int ALGOMINQ_MAX = 15;
        int algoMinQ = ALGOMINQ_INVALID;
        int getAlgoMinQ() {
            if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMinQ;
        }
        boolean setAlgoMinQ(int algoMinQ) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMAXREP_INVALID = -1; final int ALGOMAXREP_MIN = 0; final int ALGOMAXREP_MAX = 255;
        int algoMaxRep = ALGOMAXREP_INVALID;
        int getAlgoMaxRep() {
            if (algoMaxRep < ALGOMAXREP_MIN || algoMaxRep > ALGOMAXREP_MAX) getHST_INV_ALG_PARM_0();
            return algoMaxRep;
        }
        boolean setAlgoMaxRep(int algoMaxRep) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOHIGHTHRES_INVALID = -1; final int ALGOHIGHTHRES_MIN = 0; final int ALGOHIGHTHRES_MAX = 15;
        int algoHighThres = ALGOHIGHTHRES_INVALID;
        int getAlgoHighThres() {
            if (algoHighThres < ALGOHIGHTHRES_MIN || algoHighThres > ALGOHIGHTHRES_MAX)
                getHST_INV_ALG_PARM_0();
            return algoHighThres;
        }
        boolean setAlgoHighThres(int algoHighThres) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOLOWTHRES_INVALID = -1; final int ALGOLOWTHRES_MIN = 0; final int ALGOLOWTHRES_MAX = 15;
        int algoLowThres = ALGOLOWTHRES_INVALID;
        int getAlgoLowThres() {
            if (algoLowThres < ALGOLOWTHRES_MIN || algoLowThres > ALGOLOWTHRES_MAX)
                getHST_INV_ALG_PARM_0();
            return algoLowThres;
        }
        boolean setAlgoLowThres(int algoLowThres) {
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        private boolean getHST_INV_ALG_PARM_0() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, false, msgBuffer);
        }
        boolean setAlgoStartQ(int startQ, int algoMaxQ, int algoMinQ, int algoMaxRep, int algoHighThres, int algoLowThres) {
            boolean DEBUG = false;
            if (DEBUG) appendToLog("BtDataOut: startQ = " + startQ + ", algoMaxQ = " + algoMaxQ + ", algoMinQ = " + algoMinQ + ", algoMaxRep = " + algoMaxRep +
                    ", algoHighThres = " + algoHighThres + ", algoLowThres = " + algoLowThres +
                    ", Old = " + this.algoStartQ + ", " + this.algoMaxQ + ", " + this.algoMinQ + ", " + this.algoMaxRep +
                    ", " + this.algoHighThres + ", " + this.algoLowThres);

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 9, 0, 0, 0, 0};
            if (startQ < ALGOSTARTQ_MIN || startQ > ALGOSTARTQ_MAX) startQ = mDefault.algoStartQ;
            if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) algoMaxQ = mDefault.algoMaxQ;
            if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) algoMinQ = mDefault.algoMinQ;
            if (algoMaxRep < ALGOMAXREP_MIN || algoMaxRep > ALGOMAXREP_MAX)
                algoMaxRep = mDefault.algoMaxRep;
            if (algoHighThres < ALGOHIGHTHRES_MIN || algoHighThres > ALGOHIGHTHRES_MAX)
                algoHighThres = mDefault.algoHighThres;
            if (algoLowThres < ALGOLOWTHRES_MIN || algoLowThres > ALGOLOWTHRES_MAX)
                algoLowThres = mDefault.algoLowThres;
            if (this.algoStartQ == startQ && this.algoMaxQ == algoMaxQ && this.algoMinQ == algoMinQ
                    && this.algoMaxRep == algoMaxRep && this.algoHighThres == algoHighThres && this.algoLowThres == algoLowThres
                    && sameCheck)
                return true;
            if (DEBUG) appendToLog("algoMaxRep = " + algoMaxRep + ", algoMaxRep = " + algoMaxRep + ", algoLowThres = " + algoLowThres);
            msgBuffer[4] |= (byte) (startQ & 0x0F);
            msgBuffer[4] |= (byte) ((algoMaxQ & 0x0F) << 4);
            msgBuffer[5] |= (byte) (algoMinQ & 0x0F);
            msgBuffer[5] |= (byte) ((algoMaxRep & 0xF) << 4);
            msgBuffer[6] |= (byte) ((algoMaxRep & 0xF0) >> 4);
            msgBuffer[6] |= (byte) ((algoHighThres & 0x0F) << 4);
            msgBuffer[7] |= (byte) (algoLowThres & 0x0F);
            this.algoStartQ = startQ;
            if (false) appendToLog("this.algoStartQ is updated as " + this.algoStartQ);
            this.algoMaxQ = algoMaxQ;
            this.algoMinQ = algoMinQ;
            this.algoMaxRep = algoMaxRep;
            this.algoHighThres = algoHighThres;
            this.algoLowThres = algoLowThres;
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, true, msgBuffer);
        }

        final int ALGORETRY_INVALID = -1; final int ALGORETRY_MIN = 0; final int ALGORETRY_MAX = 255;
        int algoRetry = ALGORETRY_INVALID;
        int getAlgoRetry() {
            if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 9, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_1, false, msgBuffer);
            }
            return algoRetry;
        }
        boolean setAlgoRetry(int algoRetry) {
            if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX)
                algoRetry = mDefault.algoRetry;
            if (this.algoRetry == algoRetry && sameCheck) return true;
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 9, 0, 0, 0, 0};
            msgBuffer[4] = (byte) algoRetry;
            this.algoRetry = algoRetry;
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_1, true, msgBuffer);
        }

        final int ALGOABFLIP_INVALID = -1; final int ALGOABFLIP_MIN = 0; final int ALGOABFLIP_MAX = 1;
        int algoAbFlip = ALGOABFLIP_INVALID;
        int getAlgoAbFlip() {
            if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX) getHST_INV_ALG_PARM_2();
            return algoAbFlip;
        }
        boolean setAlgoAbFlip(int algoAbFlip) {
            return setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        final int ALGORUNTILZERO_INVALID = -1; final int ALGORUNTILZERO_MIN = 0; final int ALGORUNTILZERO_MAX = 1;
        int algoRunTilZero = ALGORUNTILZERO_INVALID;
        int getAlgoRunTilZero() {
            if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX) getHST_INV_ALG_PARM_2();
            return algoRunTilZero;
        }
        boolean setAlgoRunTilZero(int algoRunTilZero) {
            return setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        private boolean getHST_INV_ALG_PARM_2() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_2, false, msgBuffer);
        }
        boolean setAlgoAbFlip(int algoAbFlip, int algoRunTilZero) {
            if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX)
                algoAbFlip = mDefault.algoAbFlip;
            if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX)
                algoRunTilZero = mDefault.algoRunTilZero;
            if (true) appendToLog("this.algoAbFlip  = " + this.algoAbFlip + ", algoAbFlip = " + algoAbFlip + ", this.algoRunTilZero = " + this.algoRunTilZero + ", algoRunTilZero = " + algoRunTilZero);
            if (this.algoAbFlip == algoAbFlip && this.algoRunTilZero == algoRunTilZero && sameCheck) return true;
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 9, 0, 0, 0, 0};
            if (algoAbFlip != 0) {
                msgBuffer[4] |= 0x01;
            }
            if (algoRunTilZero != 0) {
                msgBuffer[4] |= 0x02;
            }
            this.algoAbFlip = algoAbFlip;
            this.algoRunTilZero = algoRunTilZero;
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_2, true, msgBuffer);
        }
    }
    public class Rx000EngSetting {
        int narrowRSSI = -1, wideRSSI = -1;
        public int getwideRSSI() {
            if (wideRSSI < 0) {
                setPwrManagementMode(false);
                rx000Setting.writeMAC(0x100, 0x05); //sub-command: 0x05, Arg0: reserved
                rx000Setting.writeMAC(0x101,  3 + 0x20000); //Arg1: 15-0: number of RSSI sample
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_ENGTEST);
            } else appendToLog("Hello123: wideRSSI = " + wideRSSI);
            return wideRSSI;
        }
        int getnarrowRSSI() {
            if (narrowRSSI < 0) {
                setPwrManagementMode(false);
                rx000Setting.writeMAC(0x100, 0x05); //sub-command: 0x05, Arg0: reserved
                rx000Setting.writeMAC(0x101,  3 + 0x20000); //Arg1: 15-0: number of RSSI sample
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_ENGTEST);
            } else appendToLog("Hello123: narrowRSSI = " + wideRSSI);
            return wideRSSI;
        }
        public void resetRSSI() {
            narrowRSSI = -1; wideRSSI = -1;
        }
    }
    public class Rx000MbpSetting {
        final int RXGAIN_INVALID = -1; final int RXGAIN_MIN = 0; final int RXGAIN_MAX = 0x1FF;
        int rxGain = RXGAIN_INVALID;
        public int getHighCompression() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setMBPAddress(0x450); appendToLog("70010004: getHighCompression");
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPRDREG);
            } else iRetValue = (rxGain >> 8);
            return iRetValue;
        }
        public int getRflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setMBPAddress(0x450); appendToLog("70010004: getRflnaGain");
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPRDREG);
            } else iRetValue = ((rxGain & 0xC0) >> 6);
            return iRetValue;
        }
        public int getIflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setMBPAddress(0x450); appendToLog("70010004: getIflnaGain");
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPRDREG);
            } else iRetValue = ((rxGain & 0x38) >> 3);
            return iRetValue;
        }
        public int getAgcGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setMBPAddress(0x450); appendToLog("70010004: getAgcGain");
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPRDREG);
            } else iRetValue = (rxGain & 0x07);
            return iRetValue;
        }
        public int getRxGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setMBPAddress(0x450);
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPRDREG);
            } else iRetValue = rxGain;
            return iRetValue;
        }
        public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
            int rxGain_new = ((highCompression & 0x01) << 8) | ((rflnagain & 0x3) << 6) | ((iflnagain & 0x7) << 3) | (agcgain & 0x7);
            return setRxGain(rxGain_new);
        }
        public boolean setRxGain(int rxGain_new) {
            boolean bRetValue = true;
            if ((rxGain_new != rxGain) || (sameCheck == false)) {
                setPwrManagementMode(false);
                bRetValue = rx000Setting.setMBPAddress(0x450);
                if (bRetValue != false) bRetValue = rx000Setting.setMBPData(rxGain_new);
                if (bRetValue != false) bRetValue = sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_MBPWRREG);
                if (bRetValue != false) rxGain = rxGain_new;
            }
            return bRetValue;
        }
    }
    public class Rx000OemSetting {
        final int COUNTRYCODE_INVALID = -1; final int COUNTRYCODE_MIN = 1; final int COUNTRYCODE_MAX = 9;
        int countryCode = COUNTRYCODE_INVALID;   // OemAddress = 0x02
        public int getCountryCode() {
            if (countryCode < COUNTRYCODE_MIN || countryCode > COUNTRYCODE_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setOEMAddress(2);
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
            }
            return countryCode;
        }

        final int SERIALCODE_INVALID = -1;
        byte[] serialNumber = new byte[] { SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0 };
        public String getSerialNumber() {
            boolean invalid = false;
            int length = serialNumber.length / 4;
            if (serialNumber.length % 4 != 0)   length++;
            for (int i = 0; i < length; i++) {
                if (serialNumber[4 * i] == SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
                    invalid = true;
                    setPwrManagementMode(false);
                    rx000Setting.setOEMAddress(0x04 + i);
                    sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
                }
            }
            if (invalid)    return null;
            appendToLog("retValue = " + byteArrayToString(serialNumber));
            byte[] retValue = new byte[serialNumber.length];
            for (int i = 0; i < retValue.length; i++) {
                int j = (i/4)*4 + 3 - i%4;
                if (j >= serialNumber.length)   retValue[i] = serialNumber[i];
                else    retValue[i] = serialNumber[j];
                if (retValue[i] == 0) retValue[i] = 0x30;
            }
            appendToLog("retValue = " + byteArrayToString(retValue) + ", String = " + new String(retValue));
            return new String(retValue);
        }

        final int PRODUCT_SERIALCODE_INVALID = -1;
        byte[] productserialNumber = new byte[] { SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0 };
        public String getProductSerialNumber() {
            boolean invalid = false;
            int length = productserialNumber.length / 4;
            if (productserialNumber.length % 4 != 0)   length++;
            for (int i = 0; i < length; i++) {
                if (productserialNumber[4 * i] == PRODUCT_SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
                    invalid = true;
                    setPwrManagementMode(false);
                    rx000Setting.setOEMAddress(0x08 + i);
                    sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
                }
            }
            if (invalid)    return null;
            appendToLog("retValue = " + byteArrayToString(productserialNumber));
            byte[] retValue = new byte[productserialNumber.length];
            for (int i = 0; i < retValue.length; i++) {
                int j = (i/4)*4 + 3 - i%4;
                if (j >= productserialNumber.length)   retValue[i] = productserialNumber[i];
                else    retValue[i] = productserialNumber[j];
                if (retValue[i] == 0) retValue[i] = 0x30;
            }
            appendToLog("retValue = " + byteArrayToString(retValue) + ", String = " + new String(retValue));
            return new String(retValue);
        }

        final int VERSIONCODE_INVALID = -1; final int VERSIONCODE_MIN = 1; final int VERSIONCODE_MAX = 9;
        int versionCode = VERSIONCODE_INVALID;   // OemAddress = 0x02
        public int getVersionCode() {
            if (versionCode < VERSIONCODE_MIN || versionCode > VERSIONCODE_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setOEMAddress(0x0B);
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
            }
            return versionCode;
        }

        String spcialCountryVersion = null;
        public String getSpecialCountryVersion() {
            if (spcialCountryVersion == null) {
                setPwrManagementMode(false);
                rx000Setting.setOEMAddress(0x8E);
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
                return "";
            }
            return spcialCountryVersion.replaceAll("[^A-Za-z0-9]", "");
        }

        final int FREQMODIFYCODE_INVALID = -1; final int FREQMODIFYCODE_MIN = 0; final int FREQMODIFYCODE_MAX = 0xAA;
        int freqModifyCode = FREQMODIFYCODE_INVALID;   // OemAddress = 0x8A
        public int getFreqModifyCode() {
            if (freqModifyCode < FREQMODIFYCODE_MIN || freqModifyCode > FREQMODIFYCODE_MAX) {
                setPwrManagementMode(false);
                rx000Setting.setOEMAddress(0x8F);
                sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_RDOEM);
            }
            return freqModifyCode;
        }

        void writeOEM(int address, int value) {
            setPwrManagementMode(false);
            rx000Setting.setOEMAddress(address);
            rx000Setting.setOEMData(value);
            sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_WROEM);
        }
    }
    //public boolean bFirmware_reset_before = false;
    final int RFID_READING_BUFFERSIZE = 1024;
    enum RfidDataReadTypes {
        RFID_DATA_READ_SOFTRESET, RFID_DATA_READ_ABORT, RFID_DATA_READ_RESETTOBOOTLOADER,
        RFID_DATA_READ_REGREAD,
        RFID_DATA_READ_COMMAND_BEGIN, RFID_DATA_READ_COMMAND_END,
        RFID_DATA_READ_COMMAND_INVENTORY, RFID_DATA_READ_COMMAND_COMPACT, RFID_DATA_READ_COMMAND_ACCESS,
        RFID_DATA_READ_COMMAND_MBPREAD, RFID_DATA_READ_COMMAND_OEMREAD,
        RFID_DATA_READ_COMMAND_RSSI, RFID_DATA_READ_COMMAND_ACTIVE
    };
    //class RfidReaderChip {
        byte[] mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
        int mRfidToReadingOffset = 0;
        ArrayList<RfidConnector.CsReaderRfidData> mRx000ToWrite = new ArrayList<>();

        public Rx000Setting rx000Setting = new Rx000Setting(true);
        public Rx000EngSetting rx000EngSetting = new Rx000EngSetting();
        public Rx000MbpSetting rx000MbpSetting = new Rx000MbpSetting();
        public Rx000OemSetting rx000OemSetting = new Rx000OemSetting();

        public ArrayList<RfidReaderChipData.Rx000pkgData> mRx000ToRead = new ArrayList<>();
        private boolean clearTempDataIn_request = false;
        boolean commandOperating;

        public double decodeNarrowBandRSSI(byte byteRSSI) {
            byte mantissa = byteRSSI;
            mantissa &= 0x07;
            byte exponent = byteRSSI;
            exponent >>= 3;
            double dValue = 20 * log10(pow(2, exponent) * (1 + (mantissa / pow(2, 3))));
            if (false)
                appendToLog("byteRSSI = " + String.format("%X", byteRSSI) + ", mantissa = " + mantissa + ", exponent = " + exponent + "dValue = " + dValue);
            return dValue;
        }

        public int encodeNarrowBandRSSI(double dRSSI) {
            double dValue = dRSSI / 20;
            dValue = pow(10, dValue);
            int exponent = 0;
            if (false) appendToLog("exponent = " + exponent + ", dValue = " + dValue);
            while ((dValue + 0.062) >= 2) {
                dValue /= 2;
                exponent++;
                if (false) appendToLog("exponent = " + exponent + ", dValue = " + dValue);
            }
            dValue--;
            int mantissa = (int) ((dValue * 8) + 0.5);
            while (mantissa >= 8) {
                mantissa -= 8;
                exponent++;
            }
            int iValue = ((exponent & 0x1F) << 3) | (mantissa & 0x7);
            if (false)
                appendToLog("dRssi = " + dRSSI + ", exponent = " + exponent + ", mantissa = " + mantissa + ", iValue = " + String.format("%X", iValue));
            return iValue;
        }

        long firmware_ontime_ms = 0;
        long date_time_ms = 0;
        public boolean bRx000ToReading = false;

        void uplinkHandler() {
            boolean DEBUG = false;
            if (bRx000ToReading) return;
            bRx000ToReading = true;
            int startIndex = 0;
            int startIndexOld = 0;
            int startIndexNew = 0;
            boolean packageFound = false;
            int packageType = 0;
            long lTime = System.currentTimeMillis();
            boolean bdebugging = false;
            if (csReaderConnector.rfidConnector.mRfidToRead.size() != 0) {
                bdebugging = true;
                if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): START");
            } else if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): START AAA");
            boolean bFirst = true;
            byte[] data1 = null;
            RfidDataReadTypes rfidDataReadTypes = null;
            boolean bLooping = false;
            while (csReaderConnector.rfidConnector.mRfidToRead.size() != 0) {
                if (utility.DEBUG_APDATA && bLooping == false) appendToLog("ApData: Entering loop with mRfidToRead.size as " + csReaderConnector.rfidConnector.mRfidToRead.size());
                bLooping = true;

                if (csReaderConnector.isConnected() == false) {
                    csReaderConnector.rfidConnector.mRfidToRead.clear();
                } else if (System.currentTimeMillis() - lTime > (intervalRx000UplinkHandler / 2)) {
                    writeDebug2File("Up4  " + String.valueOf(intervalRx000UplinkHandler) + "ms Timeout");
                    if (utility.DEBUG_APDATA)
                        appendToLogView("ApData: TIMEOUT !!! mRfidToRead.size() = " + csReaderConnector.rfidConnector.mRfidToRead.size());
                    break;
                } else {
                    if (bFirst) {
                        bFirst = false;
                    }
                    byte[] dataIn = csReaderConnector.rfidConnector.mRfidToRead.get(0).dataValues;
                    long tagMilliSeconds = csReaderConnector.rfidConnector.mRfidToRead.get(0).milliseconds;
                    boolean invalidSequence = csReaderConnector.rfidConnector.mRfidToRead.get(0).invalidSequence;
                    if (utility.DEBUG_APDATA)
                        appendToLog("ApData: mRfidToReadingOffset=" + mRfidToReadingOffset  + ", mRfidToReading.length=" + mRfidToReading.length + ", dataIn.length=" + dataIn.length + ", dataIn=" + byteArrayToString(dataIn));
                    csReaderConnector.rfidConnector.mRfidToRead.remove(0);

                    if (dataIn.length >= mRfidToReading.length - mRfidToReadingOffset) {
                        byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                        System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                        if (utility.DEBUG_APDATA)
                            appendToLogView("ApData: ERROR insufficient buffer, mRfidToReadingOffset=" + mRfidToReadingOffset + ", dataIn.length=" + dataIn.length + ", clear mRfidToReading: " + byteArrayToString(unhandledBytes));
                        byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                        mRfidToReading = mRfidToReadingNew;
                        mRfidToReadingOffset = 0;
                        invalidUpdata++;
                        writeDebug2File("Up4  insufficient buffer. missed " + byteArrayToString(unhandledBytes));
                    }
                    if (mRfidToReadingOffset != 0 && invalidSequence) {
                        byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                        System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                        if (utility.DEBUG_APDATA)
                            appendToLog("ApData: ERROR invalidSequence with nonzero mRfidToReadingOffset=" + mRfidToReadingOffset + ", throw invalid unused data=" + unhandledBytes.length + ", " + byteArrayToString(unhandledBytes));
                        mRfidToReadingOffset = 0;
                        startIndex = 0;
                        startIndexNew = 0;
                        writeDebug2File("Up4  Invalid sequence, " + byteArrayToString(unhandledBytes));
                    }
                    System.arraycopy(dataIn, 0, mRfidToReading, mRfidToReadingOffset, dataIn.length);
                    mRfidToReadingOffset += dataIn.length;
                    if (utility.DEBUG_APDATA) {
                        byte[] bufferData = new byte[mRfidToReadingOffset];
                        System.arraycopy(mRfidToReading, 0, bufferData, 0, bufferData.length);
                        appendToLog("ApData: new mRfidToReadingOffset=" + mRfidToReadingOffset + ", mRfidToReading=" + byteArrayToString(bufferData));
                    }

                    int iPayloadSizeMin = 8; boolean bLooping2 = false;
                    while (mRfidToReadingOffset - startIndex >= iPayloadSizeMin) {
                        if (utility.DEBUG_APDATA && bLooping2 == false) appendToLog("ApData: Entering second loop with mRfidToReadingOffset = " + mRfidToReadingOffset + ", startIndex = " + startIndex);
                        bLooping2 = true;

                        {
                            int packageLengthRead = (mRfidToReading[startIndex + 5] & 0xFF) * 256 + (mRfidToReading[startIndex + 4] & 0xFF);
                            int expectedLength = 8 + packageLengthRead * 4;
                            if (mRfidToReading[startIndex + 0] == 0x04)
                                expectedLength = 8 + packageLengthRead;
                            if (utility.DEBUG_APDATA)
                                appendToLog("ApData: loop: 1Byte=" + mRfidToReading[startIndex + 0] + ", mRfidToReadingOffset=" + mRfidToReadingOffset + ", expectedLength=" + expectedLength);
                            if (mRfidToReadingOffset - startIndex >= 8) {
                                if (mRfidToReading[startIndex + 0] == (byte) 0x40
                                        && (mRfidToReading[startIndex + 1] == 2 || mRfidToReading[startIndex + 1] == 3 || mRfidToReading[startIndex + 1] == 7)) {   //input as Control Command Response
                                    dataIn = mRfidToReading;
                                    if (DEBUG) appendToLog("decoding CONTROL data");
                                    if (csReaderConnector.rfidConnector.mRfidToWrite.size() == 0) {
                                        if (DEBUG)
                                            appendToLog("Control Response is received with null mRfidToWrite");
                                    } else if (csReaderConnector.rfidConnector.mRfidToWrite.get(0) == null) {
                                        if (DEBUG)
                                            appendToLog("Control Response is received with null mRfidToWrite.get(0)");
                                    } else if (csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues == null) {
                                        csReaderConnector.rfidConnector.mRfidToWrite.remove(0);
                                        if (DEBUG) appendToLog("mmRfidToWrite remove 5");
                                        if (DEBUG)
                                            appendToLog("Control Response is received with null mRfidToWrite.dataValues");
                                    } else if (!(csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[0] == dataIn[startIndex + 0] && csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[1] == dataIn[startIndex + 1])) {
                                        if (DEBUG)
                                            appendToLog("Control Response is received with Mis-matched mRfidToWrite, " + startIndex + ", " + byteArrayToString(dataIn));
                                    } else {
                                        byte[] dataInCompare = null;
                                        switch (csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[1]) {
                                            case 2: //SOFTRESET
                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_SOFTRESET;
                                                dataInCompare = new byte[]{0x40, 0x02, (byte) 0xbf, (byte) 0xfd, (byte) 0xbf, (byte) 0xfd, (byte) 0xbf, (byte) 0xfd};
                                                break;
                                            case 3: //ABORT
                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_ABORT;
                                                dataInCompare = new byte[]{0x40, 0x03, (byte) 0xbf, (byte) 0xfc, (byte) 0xbf, (byte) 0xfc, (byte) 0xbf, (byte) 0xfc};
                                                break;
                                            case 7: //RESETTOBOOTLOADER
                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_RESETTOBOOTLOADER;
                                                dataInCompare = new byte[]{0x40, 0x07, (byte) 0xbf, (byte) 0xf8, (byte) 0xbf, (byte) 0xf8, (byte) 0xbf, (byte) 0xf8};
                                                break;
                                        }
                                        byte[] dataIn8 = new byte[8];
                                        System.arraycopy(dataIn, startIndex, dataIn8, 0, dataIn8.length);
                                        if (!(compareArray(dataInCompare, dataIn8, 8))) {
                                            if (DEBUG)
                                                appendToLog("Control response with invalid data: " + byteArrayToString(dataIn8));
                                        } else {
                                            csReaderConnector.rfidConnector.mRfidToWrite.remove(0);
                                            csReaderConnector.rfidConnector.sendRfidToWriteSent = 0;
                                            csReaderConnector.rfidConnector.mRfidToWriteRemoved = true;
                                            if (DEBUG) appendToLog("mmRfidToWrite remove 6");
                                            if (DEBUG)
                                                appendToLog("matched control command with mRfidToWrite.size=" + csReaderConnector.rfidConnector.mRfidToWrite.size());
                                        }
                                    }
                                    if (true) {
                                        byte[] dataIn8 = new byte[8];
                                        System.arraycopy(dataIn, startIndex, dataIn8, 0, dataIn8.length);
                                        byte[] dataInCompare = new byte[]{0x40, 0x03, (byte) 0xbf, (byte) 0xfc, (byte) 0xbf, (byte) 0xfc, (byte) 0xbf, (byte) 0xfc};
                                        if (compareArray(dataInCompare, dataIn8, 8)) {
                                            RfidReaderChipData.Rx000pkgData dataA = new RfidReaderChipData.Rx000pkgData();
                                            dataA.dataValues = dataIn8;
                                            dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN;
                                            mRx000ToRead.add(dataA);
                                            if (DEBUG)
                                                appendToLog("Abort Return data is found wth type = " + dataA.responseType.toString());
                                            setInventoring(false);
                                        }
                                    }
                                    packageFound = true;
                                    packageType = 1; //0x40
                                    startIndexNew = startIndex + iPayloadSizeMin;
                                } else if ((mRfidToReading[startIndex + 0] == (byte) 0x00 || mRfidToReading[startIndex + 0] == (byte) 0x70)
                                        && mRfidToReading[startIndex + 1] == 0
                                        && csReaderConnector.rfidConnector.mRfidToWrite.size() != 0
                                        && csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues != null
                                        && csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[0] == 0x70
                                        && csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[1] == 0
                                ) {   //if input as HOST_REG_RESP
                                    if (DEBUG)
                                        appendToLog("loop: decoding HOST_REG_RESP data with startIndex = " + startIndex + ", mRfidToReading=" + byteArrayToString(mRfidToReading));
                                    dataIn = mRfidToReading;
                                    byte[] dataInPayload = new byte[4];
                                    System.arraycopy(dataIn, startIndex + 4, dataInPayload, 0, dataInPayload.length);
                                    //if (mRfidDevice.mRfidToWrite.size() == 0) {
                                    //    if (true) appendToLog("mRx000UplinkHandler(): HOST_REG_RESP is received with null mRfidToWrite: " + byteArrayToString(dataInPayload));
                                    //} else if (mRfidDevice.mRfidToWrite.get(0).dataValues == null) {
                                    //    if (true) appendToLog("mRx000UplinkHandler(): NULL mRfidToWrite.get(0).dataValues"); //.length = " + mRfidDevice.mRfidToWrite.get(0).dataValues.length);
                                    //} else if (!(mRfidDevice.mRfidToWrite.get(0).dataValues[0] == 0x70 && mRfidDevice.mRfidToWrite.get(0).dataValues[1] == 0)) {
                                    //    if (true) appendToLog("mRx000UplinkHandler(): HOST_REG_RESP is received with invalid mRfidDevice.mRfidToWrite.get(0).dataValues=" + byteArrayToString(mRfidDevice.mRfidToWrite.get(0).dataValues));
                                    //} else
                                    {
                                        int addressToWrite = csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[2] + csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues[3] * 256;
                                        int addressToRead = dataIn[startIndex + 2] + dataIn[startIndex + 3] * 256;
                                        if (addressToRead != addressToWrite) {
                                            if (DEBUG)
                                                appendToLog("mRx000UplinkHandler(): HOST_REG_RESP is received with misMatch address: addressToRead=" + addressToRead + ", " + startIndex + ", " + byteArrayToString(dataInPayload) + ", addressToWrite=" + addressToWrite);
                                        } else {
                                            switch (addressToRead) {
                                                case 0:
                                                    int patchVersion = dataIn[startIndex + 4] + (dataIn[startIndex + 5] & 0x0F) * 256;
                                                    int minorVersion = (dataIn[startIndex + 5] >> 4) + dataIn[startIndex + 6] * 256;
                                                    int majorVersion = dataIn[startIndex + 7];
                                                    rx000Setting.macVer = String.valueOf(majorVersion) + "." + String.valueOf(minorVersion) + "." + String.valueOf(patchVersion);
                                                    if (DEBUG)
                                                        appendToLog("found MacVer =" + rx000Setting.macVer);
                                                    break;
                                                case 9:
                                                    rx000Setting.mac_last_command_duration = (dataIn[startIndex + 4] & 0xFF)
                                                            + (dataIn[startIndex + 5] & 0xFF) * 256
                                                            + (dataIn[startIndex + 6] & 0xFF) * 256 * 256
                                                            + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found mac_last_command_duration =" + rx000Setting.mac_last_command_duration);
                                                    break;
                                                case 0x0201:
                                                    rx000Setting.diagnosticCfg = (dataIn[startIndex + 4] & 0x0FF) + ((dataIn[startIndex + 5] & 0x03) * 256);
                                                    if (DEBUG)
                                                        appendToLog("found diagnostic configuration: " + byteArrayToString(dataInPayload) + ", diagnosticCfg=" + rx000Setting.diagnosticCfg);
                                                    break;
                                                case 0x0203:
                                                    rx000Setting.impinjExtensionValue = (dataIn[startIndex + 4] & 0x03F);
                                                    break;
                                                case 0x204:
                                                    rx000Setting.pwrMgmtStatus = (dataIn[startIndex + 4] & 0x07);
                                                    if (DEBUG)
                                                        appendToLog("pwrMgmtStatus = " + rx000Setting.pwrMgmtStatus);
                                                    break;
                                                case 0x0700:
                                                    rx000Setting.antennaCycle = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256;
                                                    rx000Setting.antennaFreqAgile = 0;
                                                    if ((dataIn[startIndex + 7] & 0x01) != 0)
                                                        rx000Setting.antennaFreqAgile = 1;
                                                    if (DEBUG)
                                                        appendToLog("found antenna cycle: " + byteArrayToString(dataInPayload) + ", cycle=" + rx000Setting.antennaCycle + ", frequencyAgile=" + rx000Setting.antennaFreqAgile);
                                                    break;
                                                case 0x0701:
                                                    rx000Setting.antennaSelect = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0xFF) * 256 * 256 + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found antenna select, select=" + rx000Setting.antennaSelect);
                                                    break;
                                                case 0x0702:
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaEnable = (dataIn[startIndex + 4] & 0x01);
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaInventoryMode = (dataIn[startIndex + 4] & 0x02) >> 1;
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalAlgo = (dataIn[startIndex + 4] & 0x0C) >> 2;
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalStartQ = (dataIn[startIndex + 4] & 0xF0) >> 4;
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaProfileMode = (dataIn[startIndex + 5] & 0x01);
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalProfile = ((dataIn[startIndex + 5] & 0x1E) >> 1);
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaFrequencyMode = ((dataIn[startIndex + 5] & 0x20) >> 5);
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalFrequency = (dataIn[startIndex + 5] & 0x0F) * 4 + ((dataIn[startIndex + 5] & 0xC0) >> 6);
                                                    if (DEBUG)
                                                        appendToLog("found antenna selectEnable: " + byteArrayToString(dataInPayload)
                                                                + ", selectEnable=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaEnable
                                                                + ", inventoryMode=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaInventoryMode
                                                                + ", localAlgo=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalAlgo
                                                                + ", localStartQ=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalStartQ
                                                                + ", profileMode=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaProfileMode
                                                                + ", localProfile=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalProfile
                                                                + ", frequencyMode=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaFrequencyMode
                                                                + ", localFrequency=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaLocalFrequency
                                                        );
                                                    break;
                                                case 0x0703:
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaStatus = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0x0F) * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found antenna status: " + byteArrayToString(dataInPayload) + ", status=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaStatus);
                                                    break;
                                                case 0x0704:
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaDefine = (dataIn[startIndex + 4] & 0x3);
                                                    //      mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect].antennaRxDefine = (dataIn[startIndex + 6] & 0x3);
                                                    if (DEBUG)
                                                        appendToLog("found antenna define: " + byteArrayToString(dataInPayload)
                                                                        + ", define=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaDefine
                                                                //        + ", RxDefine=" + mRx000Setting.antennaSelectedData[mRx000Setting.antennaSelect].antennaRxDefine
                                                        );
                                                    break;
                                                case 0x0705:
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaDwell = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0xFF) * 256 * 256 + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found antenna dwell=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaDwell);
                                                    break;
                                                case 0x0706:
                                                    if (rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaPowerSet == false)
                                                        rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaPower = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0xFF) * 256 * 256 + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    break;
                                                case 0x0707:
                                                    rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaInvCount = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0xFF) * 256 * 256 + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found antenna InvCount=" + rx000Setting.antennaSelectedData[rx000Setting.antennaSelect].antennaInvCount);
                                                    break;
                                                case 0x0800:
                                                    rx000Setting.invSelectIndex = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256 + (dataIn[startIndex + 6] & 0xFF) * 256 * 256 + (dataIn[startIndex + 7] & 0xFF) * 256 * 256 * 256;
                                                    if (DEBUG)
                                                        appendToLog("found inventory select: " + byteArrayToString(dataInPayload) + ", select=" + rx000Setting.invSelectIndex);
                                                    break;
                                                case 0x0801: {
                                                    int dataIndex = rx000Setting.invSelectIndex;
                                                    if (dataIndex < rx000Setting.INVSELECT_MIN || dataIndex > rx000Setting.INVSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select configuration: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        appendToLog("BtDataOut 123A");
                                                        rx000Setting.invSelectData[dataIndex].selectEnable = (dataIn[startIndex + 4] & 0x01);
                                                        rx000Setting.invSelectData[dataIndex].selectTarget = ((dataIn[startIndex + 4] & 0x0E) >> 1);
                                                        rx000Setting.invSelectData[dataIndex].selectAction = ((dataIn[startIndex + 4] & 0x70) >> 4);
                                                        rx000Setting.invSelectData[dataIndex].selectDelay = dataIn[startIndex + 5];
                                                        if (DEBUG)
                                                            appendToLog("found inventory select configuration: " + byteArrayToString(dataInPayload)
                                                                    + ", selectEnable=" + rx000Setting.invSelectData[dataIndex].selectEnable
                                                                    + ", selectTarget=" + rx000Setting.invSelectData[dataIndex].selectTarget
                                                                    + ", selectAction=" + rx000Setting.invSelectData[dataIndex].selectAction
                                                                    + ", selectDelay=" + rx000Setting.invSelectData[dataIndex].selectDelay
                                                            );
                                                    }
                                                    break;
                                                }
                                                case 0x0802: {
                                                    int dataIndex = rx000Setting.invSelectIndex;
                                                    if (dataIndex < rx000Setting.INVSELECT_MIN || dataIndex > rx000Setting.INVSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask bank: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        rx000Setting.invSelectData[dataIndex].selectMaskBank = (dataIn[startIndex + 4] & 0x03);
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask bank: " + byteArrayToString(dataInPayload)
                                                                    + ", selectMaskBank=" + rx000Setting.invSelectData[dataIndex].selectMaskBank
                                                            );
                                                    }
                                                    break;
                                                }
                                                case 0x0803: {
                                                    int dataIndex = rx000Setting.invSelectIndex;
                                                    if (dataIndex < rx000Setting.INVSELECT_MIN || dataIndex > rx000Setting.INVSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask offset: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        rx000Setting.invSelectData[dataIndex].selectMaskOffset = (dataIn[startIndex + 4] & 0x0FF) + (dataIn[startIndex + 5] & 0x0FF) * 256 + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask offset: " + byteArrayToString(dataInPayload)
                                                                    + ", selectMaskOffset=" + rx000Setting.invSelectData[dataIndex].selectMaskOffset
                                                            );
                                                    }
                                                    break;
                                                }
                                                case 0x0804: {
                                                    int dataIndex = rx000Setting.invSelectIndex;
                                                    if (dataIndex < rx000Setting.INVSELECT_MIN || dataIndex > rx000Setting.INVSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask length: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        rx000Setting.invSelectData[dataIndex].selectMaskLength = (dataIn[startIndex + 4] & 0x0FF);
                                                        if (DEBUG)
                                                            appendToLog("getSelectMaskData with read selectMaskLength = " + rx000Setting.invSelectData[dataIndex].selectMaskLength);
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask length: " + byteArrayToString(dataInPayload)
                                                                    + ", selectMaskLength=" + rx000Setting.invSelectData[dataIndex].selectMaskLength
                                                            );
                                                    }
                                                    break;
                                                }
                                                case 0x0805:
                                                case 0x0806:
                                                case 0x0807:
                                                case 0x0808:
                                                case 0x0809:
                                                case 0x080A:
                                                case 0x080B:
                                                case 0x080C: {
                                                    int dataIndex = rx000Setting.invSelectIndex;
                                                    if (dataIndex < rx000Setting.INVSELECT_MIN || dataIndex > rx000Setting.INVSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask 0-3: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        int maskDataIndex = addressToRead - 0x0805;
                                                        if (DEBUG)
                                                            appendToLog("Old selectMaskData0_31 = " + byteArrayToString(rx000Setting.invSelectData[dataIndex].selectMaskData0_31));
                                                        System.arraycopy(dataIn, startIndex + 4, rx000Setting.invSelectData[dataIndex].selectMaskData0_31, maskDataIndex * 4, 4);
                                                        if (DEBUG)
                                                            appendToLog("Old selectMaskData0_31 = " + byteArrayToString(rx000Setting.invSelectData[dataIndex].selectMaskData0_31));
                                                        rx000Setting.invSelectData[dataIndex].selectMaskDataReady |= (0x01 << maskDataIndex);
                                                        if (DEBUG)
                                                            appendToLog("found inventory select mask 0-3: " + byteArrayToString(dataInPayload));
                                                    }
                                                    break;
                                                }
                                                case 0x0900:
                                                    if (rx000Setting.queryTarget != 2)
                                                        rx000Setting.queryTarget = (dataIn[startIndex + 4] >> 4) & 0x01;
                                                    rx000Setting.querySession = (dataIn[startIndex + 4] >> 5) & 0x03;
                                                    rx000Setting.querySelect = (dataIn[startIndex + 4] >> 7) & 0x01 + ((dataIn[startIndex + 5] & 0x01) * 2);
                                                    //appendToLog("BtDataOut: found query configuration: " + byteArrayToString(dataInPayload) + ", target=" + rx000Setting.queryTarget + ", session=" + rx000Setting.querySession + ", select=" + rx000Setting.querySelect);
                                                    break;
                                                case 0x0901:
                                                    rx000Setting.invAlgo = dataIn[startIndex + 4] & 0x3F;
                                                    rx000Setting.matchRep = ((dataIn[startIndex + 4] & 0xC0) >> 6) + (dataIn[startIndex + 5] & 0x3F) * 4;
                                                    rx000Setting.tagSelect = ((dataIn[startIndex + 5] & 0x40) >> 6);
                                                    rx000Setting.noInventory = ((dataIn[startIndex + 5] & 0x80) >> 7);
                                                    rx000Setting.tagRead = dataIn[startIndex + 6] & 0x03;
                                                    rx000Setting.tagDelay = ((dataIn[startIndex + 7] & 0x03) * 16 + ((dataIn[startIndex + 6] & 0xF0) >> 4));
                                                    rx000Setting.invModeCompact = ((dataIn[startIndex + 7] & 0x04) >> 2);
                                                    appendToLog("BtDataOut: invAuthenticate = " + rx000Setting.invAuthenticate);
                                                    rx000Setting.invAuthenticate = ((dataIn[startIndex + 7] & 0x10) >> 4);
                                                    appendToLog("BtDataOut: invAuthenticate = " + rx000Setting.invAuthenticate);
                                                    if (DEBUG)
                                                        appendToLog("found inventory configuration: " + byteArrayToString(dataInPayload) + ", algorithm=" + rx000Setting.invAlgo + ", matchRep=" + rx000Setting.matchRep + ", tagSelect=" + rx000Setting.tagSelect + ", noInventory=" + rx000Setting.noInventory + ", tagRead=" + rx000Setting.tagRead + ", tagDelay=" + rx000Setting.tagDelay);
                                                    break;
                                                case 0x0902:
                                                    if (dataIn[startIndex + 6] != 0 || dataIn[startIndex + 7] != 0) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory select, but too big: " + byteArrayToString(dataInPayload));
                                                    } else {
                                                        rx000Setting.algoSelect = (dataIn[startIndex + 4] & 0xFF) + (dataIn[startIndex + 5] & 0xFF) * 256;
                                                        if (DEBUG)
                                                            appendToLog("found inventory algorithm select=" + rx000Setting.algoSelect);
                                                    }
                                                    break;
                                                case 0x0903: {
                                                    int dataIndex = rx000Setting.algoSelect;
                                                    if (dataIndex < rx000Setting.ALGOSELECT_MIN || dataIndex > rx000Setting.ALGOSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 0: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        rx000Setting.algoSelectedData[dataIndex].algoStartQ = (dataIn[startIndex + 4] & 0x0F);
                                                        rx000Setting.algoSelectedData[dataIndex].algoMaxQ = ((dataIn[startIndex + 4] & 0xF0) >> 4);
                                                        rx000Setting.algoSelectedData[dataIndex].algoMinQ = (dataIn[startIndex + 5] & 0x0F);
                                                        rx000Setting.algoSelectedData[dataIndex].algoMaxRep = ((dataIn[startIndex + 5] & 0xF0) >> 4) + ((dataIn[startIndex + 6] & 0x0F) << 4);
                                                        rx000Setting.algoSelectedData[dataIndex].algoHighThres = ((dataIn[startIndex + 6] & 0xF0) >> 4);
                                                        rx000Setting.algoSelectedData[dataIndex].algoLowThres = (dataIn[startIndex + 7] & 0x0F);
                                                        if (DEBUG || true)
                                                            appendToLog("BtDataOut: found inventory algo parameter 0: " + byteArrayToString(dataInPayload)
                                                                    + ", algoStartQ=" + rx000Setting.algoSelectedData[dataIndex].algoStartQ
                                                                    + ", algoMaxQ=" + rx000Setting.algoSelectedData[dataIndex].algoMaxQ
                                                                    + ", algoMinQ=" + rx000Setting.algoSelectedData[dataIndex].algoMinQ
                                                                    + ", algoMaxRep=" + rx000Setting.algoSelectedData[dataIndex].algoMaxRep
                                                                    + ", algoHighThres=" + rx000Setting.algoSelectedData[dataIndex].algoHighThres
                                                                    + ", algoLowThres=" + rx000Setting.algoSelectedData[dataIndex].algoLowThres
                                                            );
                                                    }
                                                    break;
                                                }
                                                case 0x0904: {
                                                    int dataIndex = rx000Setting.algoSelect;
                                                    if (dataIndex < rx000Setting.ALGOSELECT_MIN || dataIndex > rx000Setting.ALGOSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 1: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        rx000Setting.algoSelectedData[dataIndex].algoRetry = dataIn[startIndex + 4] & 0x0FF;
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 1: " + byteArrayToString(dataInPayload) + ", algoRetry=" + rx000Setting.algoSelectedData[dataIndex].algoRetry);
                                                    }
                                                    break;
                                                }
                                                case 0x0905: {
                                                    int dataIndex = rx000Setting.algoSelect;
                                                    if (dataIndex < rx000Setting.ALGOSELECT_MIN || dataIndex > rx000Setting.ALGOSELECT_MAX) {
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 2: " + byteArrayToString(dataInPayload) + ", but invalid index=" + dataIndex);
                                                    } else {
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 2: " + byteArrayToString(dataInPayload) + ", dataIndex=" + dataIndex + ", algoAbFlip=" + rx000Setting.algoSelectedData[dataIndex].algoAbFlip + ", algoRunTilZero=" + rx000Setting.algoSelectedData[dataIndex].algoRunTilZero);
                                                        rx000Setting.algoSelectedData[dataIndex].algoAbFlip = dataIn[startIndex + 4] & 0x01;
                                                        rx000Setting.algoSelectedData[dataIndex].algoRunTilZero = (dataIn[startIndex + 4] & 0x02) >> 1;
                                                        if (DEBUG)
                                                            appendToLog("found inventory algo parameter 2: " + byteArrayToString(dataInPayload) + ", algoAbFlip=" + rx000Setting.algoSelectedData[dataIndex].algoAbFlip + ", algoRunTilZero=" + rx000Setting.algoSelectedData[dataIndex].algoRunTilZero);
                                                    }
                                                    break;
                                                }
                                                case 0x0907:
                                                    rx000Setting.rssiFilterType = dataIn[startIndex + 4] & 0xF;
                                                    rx000Setting.rssiFilterOption = (dataIn[startIndex + 4] >> 4) & 0xF;
                                                    break;
                                                case 0x0908:
                                                    rx000Setting.rssiFilterThreshold1 = dataIn[startIndex + 4];
                                                    rx000Setting.rssiFilterThreshold1 += (dataIn[startIndex + 5] << 8);
                                                    rx000Setting.rssiFilterThreshold2 = dataIn[startIndex + 6];
                                                    rx000Setting.rssiFilterThreshold2 += (dataIn[startIndex + 7] << 8);
                                                    break;
                                                case 0x0909:
                                                    rx000Setting.rssiFilterCount = dataIn[startIndex + 4];
                                                    rx000Setting.rssiFilterCount += (dataIn[startIndex + 5] << 8);
                                                    rx000Setting.rssiFilterCount += (dataIn[startIndex + 6] << 16);
                                                    rx000Setting.rssiFilterCount += (dataIn[startIndex + 7] << 24);
                                                    break;
                                                case 0x0911:
                                                    rx000Setting.matchEnable = dataIn[startIndex + 4] & 0x01;
                                                    rx000Setting.matchType = ((dataIn[startIndex + 4] & 0x02) >> 1);
                                                    rx000Setting.matchLength = ((dataIn[startIndex + 4] & 0x0FF) >> 2) + (dataIn[startIndex + 5] & 0x07) * 64;
                                                    rx000Setting.matchOffset = ((dataIn[startIndex + 5] & 0x0FF) >> 3) + (dataIn[startIndex + 6] & 0x1F) * 32;
                                                    if (DEBUG)
                                                        appendToLog("found inventory match configuration: " + byteArrayToString(dataInPayload) + ", selectEnable=" + rx000Setting.matchEnable + ", matchType=" + rx000Setting.matchType + ", matchLength=" + rx000Setting.matchLength + ", matchOffset=" + rx000Setting.matchOffset);
                                                    break;
                                                case 0x0912:
                                                case 0x0913:
                                                case 0x0914:
                                                case 0x0915:
                                                case 0x0916:
                                                case 0x0917:
                                                case 0x0918:
                                                case 0x0919:
                                                case 0x091A:
                                                case 0x091B:
                                                case 0x091C:
                                                case 0x091D:
                                                case 0x091E:
                                                case 0x091F:
                                                case 0x0920:
                                                case 0x0921: {
                                                    int maskDataIndex = addressToRead - 0x0912;
                                                    System.arraycopy(dataIn, startIndex + 4, rx000Setting.invMatchData0_63, maskDataIndex * 4, 4);
                                                    rx000Setting.invMatchDataReady |= (0x01 << maskDataIndex);
                                                    if (DEBUG)
                                                        appendToLog("found inventory match Data 0-3: " + byteArrayToString(dataInPayload));
                                                    break;
                                                }
                                                case 0x0A01:
                                                    rx000Setting.accessRetry = (dataIn[startIndex + 4] & 0x0E) >> 1;
                                                    if (DEBUG)
                                                        appendToLog("found access algoRetry: " + byteArrayToString(dataInPayload) + ", accessRetry=" + rx000Setting.accessRetry);
                                                    break;
                                                case 0x0A02:
                                                    rx000Setting.accessBank = (dataIn[startIndex + 4] & 0x03);
                                                    rx000Setting.accessBank2 = ((dataIn[startIndex + 4] >> 2) & 0x03);
                                                    if (DEBUG)
                                                        appendToLog("found access bank: " + byteArrayToString(dataInPayload) + ", accessBank=" + rx000Setting.accessBank + ", accessBank2=" + rx000Setting.accessBank2);
                                                    break;
                                                case 0x0A03:
                                                    if (rx000Setting.tagRead != 0) {
                                                        rx000Setting.accessOffset = (dataIn[startIndex + 4] & 0x0FF) + (dataIn[startIndex + 5] & 0x0FF) * 256;     // + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                                        rx000Setting.accessOffset2 = (dataIn[startIndex + 6] & 0x0FF) + (dataIn[startIndex + 7] & 0x0FF) * 256;    // + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                                    } else {
                                                        rx000Setting.accessOffset = (dataIn[startIndex + 4] & 0x0FF) + (dataIn[startIndex + 5] & 0x0FF) * 256 + (dataIn[startIndex + 6] & 0x0FF) * 256 * 256 + (dataIn[startIndex + 7] & 0x0FF) * 256 * 256 * 256;
                                                    }
                                                    if (DEBUG)
                                                        appendToLog("found access offset: " + byteArrayToString(dataInPayload) + ", accessOffset=" + rx000Setting.accessOffset + ", accessOffset2=" + rx000Setting.accessOffset2);
                                                    break;
                                                case 0x0A04:
                                                    rx000Setting.accessCount = (dataIn[startIndex + 4] & 0x0FF);
                                                    rx000Setting.accessCount2 = (dataIn[startIndex + 5] & 0x0FF);
                                                    if (DEBUG)
                                                        appendToLog("found access count: " + byteArrayToString(dataInPayload) + ", accessCount=" + rx000Setting.accessCount + ", accessCount2=" + rx000Setting.accessCount2);
                                                    break;
                                                case 0x0A05:
                                                    rx000Setting.accessLockAction = (dataIn[startIndex + 4] & 0x0FF) + ((dataIn[startIndex + 5] & 0x03) * 256);
                                                    rx000Setting.accessLockMask = ((dataIn[startIndex + 5] & 0x0FF) >> 2) + ((dataIn[startIndex + 6] & 0x0F) * 64);
                                                    if (DEBUG)
                                                        appendToLog("found access lock configuration: " + byteArrayToString(dataInPayload) + ", accessLockAction=" + rx000Setting.accessLockAction + ", accessLockMask=" + rx000Setting.accessLockMask);
                                                    break;
                                                case 0x0A08:
                                                    rx000Setting.accessWriteDataSelect = (dataIn[startIndex + 4] & 0x07);
                                                    if (DEBUG)
                                                        appendToLog("found write data select: " + byteArrayToString(dataInPayload) + ", accessWriteDataSelect=" + rx000Setting.accessWriteDataSelect);
                                                    break;
                                                case 0x0A09:
                                                case 0x0A0A:
                                                case 0x0A0B:
                                                case 0x0A0C:
                                                case 0x0A0D:
                                                case 0x0A0E:
                                                case 0x0A0F:
                                                case 0x0A10:
                                                case 0x0A11:
                                                case 0x0A12:
                                                case 0x0A13:
                                                case 0x0A14:
                                                case 0x0A15:
                                                case 0x0A16:
                                                case 0x0A17:
                                                case 0x0A18: {
                                                    int maskDataIndex = addressToRead - 0x0A09;
                                                    int maskDataIndexH = 0;
                                                    if (rx000Setting.accessWriteDataSelect != 0)
                                                        maskDataIndexH = 16;
                                                    for (int k = 0; k < 4; k++) {
                                                        rx000Setting.accWriteData0_63[(maskDataIndexH + maskDataIndex) * 4 + k] = dataIn[startIndex + 7 - k];
                                                    }
                                                    rx000Setting.accWriteDataReady |= (0x01 << (maskDataIndexH + maskDataIndex));
                                                    if (DEBUG)
                                                        appendToLog("accessWriteData=" + rx000Setting.accWriteData0_63);
                                                    if (DEBUG)
                                                        appendToLog("found access write data 0-3: " + byteArrayToString(dataInPayload));
                                                    break;
                                                }
                                                case 0x0b60:
                                                    rx000Setting.currentProfile = dataIn[startIndex + 4];
                                                    if (DEBUG)
                                                        appendToLog("found current profile: " + byteArrayToString(dataInPayload) + ", profile=" + rx000Setting.currentProfile);
                                                    break;
                                                case 0x0c01:
                                                    rx000Setting.freqChannelSelect = dataIn[startIndex + 4];
                                                    if (DEBUG)
                                                        appendToLog("setFreqChannelSelect: found frequency channel select: " + byteArrayToString(dataInPayload) + ", freqChannelSelect=" + rx000Setting.freqChannelSelect);
                                                    break;
                                                case 0x0c02:
                                                    rx000Setting.freqChannelConfig = dataIn[startIndex + 4] & 0x01;
                                                    if (DEBUG)
                                                        appendToLog("found frequency channel configuration: " + byteArrayToString(dataInPayload) + ", channelConfig=" + rx000Setting.freqChannelConfig);
                                                    break;
                                                case 0x0f00:
                                                    rx000Setting.authenticateSendReply = ((dataIn[startIndex + 4] & 1) != 0) ? true : false;
                                                    rx000Setting.authenticateIncReplyLength = ((dataIn[startIndex + 4] & 2) != 0) ? true : false;
                                                    rx000Setting.authenticateLength = ((dataIn[startIndex + 5] & 0xFC) >> 3) + (dataIn[startIndex + 6] & 0x3F);
                                                    if (DEBUG)
                                                        appendToLog("found authenticate configuration: " + byteArrayToString(dataInPayload));
                                                    break;
                                                case 0x0f01:
                                                case 0x0f02:
                                                case 0x0f03:
                                                case 0x0f04: {
                                                    int maskDataIndex = addressToRead - 0x0f01;
                                                    System.arraycopy(dataIn, startIndex + 4, rx000Setting.authMatchData0_63, maskDataIndex * 4, 4);
                                                    //mRx000Setting.authMatchDataReady |= (0x01 << maskDataIndex);
                                                    if (DEBUG)
                                                        appendToLog("found authenticate match Data 0-3: " + byteArrayToString(dataInPayload));
                                                    break;
                                                }
                                                case 0x0f05:
                                                    rx000Setting.untraceableRange = dataIn[startIndex + 4] & 0x03;
                                                    rx000Setting.untraceableUser = ((dataIn[startIndex + 4] & 0x04) != 0) ? true : false;
                                                    rx000Setting.untraceableTid = ((dataIn[startIndex + 4] & 0x18) >> 3);
                                                    rx000Setting.untraceableEpcLength = ((dataIn[startIndex + 4] & 0xE0) >> 5) + ((dataIn[startIndex + 5] & 0x3) << 3);
                                                    rx000Setting.untraceableEpc = ((dataIn[startIndex + 5] & 4) != 0) ? true : false;
                                                    rx000Setting.untraceableUXpc = ((dataIn[startIndex + 5] & 8) != 0) ? true : false;
                                                    if (DEBUG)
                                                        appendToLog("found untraceable configuration: " + byteArrayToString(dataInPayload));
                                                    break;
                                                default:
                                                    if (DEBUG)
                                                        appendToLog("found OTHERS with addressToWrite=" + addressToWrite + ", addressToRead=" + addressToRead + ", " + byteArrayToString(dataInPayload));
                                                    break;
                                            }
                                            rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_REGREAD;
                                            csReaderConnector.rfidConnector.mRfidToWrite.remove(0);
                                            csReaderConnector.rfidConnector.sendRfidToWriteSent = 0;
                                            csReaderConnector.rfidConnector.mRfidToWriteRemoved = true;
                                            if (DEBUG) appendToLog("mmRfidToWrite remove 7");
                                        }
                                    }
                                    packageFound = true;
                                    packageType = 2; //0x00, 0x70
                                    startIndexNew = startIndex + 8;
                                } else if ((mRfidToReading[startIndex + 0] >= 1 && mRfidToReading[startIndex + 0] <= 4) //02 for begin and end, 03 for inventory, 01 for access
                                        && (expectedLength >= 0 && expectedLength < mRfidToReading.length)
                                        && (mRfidToReading[startIndex + 2] == 0 || mRfidToReading[startIndex + 2] == 1 || (mRfidToReading[startIndex + 2] >= 5 && mRfidToReading[startIndex + 2] <= 14))
                                        && (mRfidToReading[startIndex + 3] == 0 || mRfidToReading[startIndex + 3] == 0x30 || mRfidToReading[startIndex + 3] == (byte) 0x80)
//                                    && mRfidToReading[startIndex + 6] == 0    //for packageTypeRead = 0x3007, this byte is 0x20. Others are 0
                                        && mRfidToReading[startIndex + 7] == 0) {  //if input as command response
                                    {
                                        if (DEBUG) appendToLog("loop: decoding 1_4 data");
                                        if (mRfidToReadingOffset - startIndex < expectedLength)
                                            break;
                                        dataIn = mRfidToReading;
                                        byte[] dataInPayload = new byte[expectedLength - 4];
                                        System.arraycopy(dataIn, startIndex + 4, dataInPayload, 0, dataInPayload.length);
                                        //if ((dataIn[startIndex + 3] == (byte) 0x80 && dataIn[startIndex + 6] == 0 && dataIn[startIndex + 7] == 0) == false) {
                                        //    appendToLog("mRx000UplinkHandler(): invalid command response is received with incorrect byte3= " + dataIn[startIndex + 3] + ", byte6=" + dataIn[startIndex + 6] + ", byte7=" + dataIn[startIndex + 7]);
                                        //}

                                        int packageTypeRead = dataIn[startIndex + 2] + (dataIn[startIndex + 3] & 0xFF) * 256;
                                        RfidReaderChipData.Rx000pkgData dataA = new RfidReaderChipData.Rx000pkgData();
                                        if (packageTypeRead == 6 && (dataIn[startIndex + 1] & 0x02) != 0 && dataIn[startIndex + 13] == 0) {
                                            dataIn[startIndex + 13] = (byte) 0xFF;
                                        }
                                        int padCount = ((dataIn[startIndex + 1] & 0x0FF) >> 6);
                                        if (packageTypeRead == 6) {
                                            dataA.dataValues = new byte[8 + packageLengthRead * 4 - padCount];
                                            System.arraycopy(dataIn, startIndex, dataA.dataValues, 0, dataA.dataValues.length);
                                        } else if (packageTypeRead == 0x8005 || packageTypeRead == 5) {
                                            if (dataIn[startIndex + 0] == 0x04) {
                                                dataA.dataValues = new byte[packageLengthRead];
                                                dataA.decodedPort = dataIn[startIndex + 6];
                                            } else
                                                dataA.dataValues = new byte[packageLengthRead * 4 - padCount];
                                            System.arraycopy(dataIn, startIndex + 8, dataA.dataValues, 0, dataA.dataValues.length);
                                        } else {
                                            dataA.dataValues = new byte[packageLengthRead * 4];
                                            System.arraycopy(dataIn, startIndex + 8, dataA.dataValues, 0, dataA.dataValues.length);
                                        }
                                        dataA.flags = (dataIn[startIndex + 1] & 0xFF);
                                        switch (packageTypeRead) {
                                            case 0x0000:
                                            case 0x8000: //RFID_PACKET_TYPE_COMMAND_BEGIN  //original 0
                                                if (dataIn[startIndex + 0] != 1 && dataIn[startIndex + 0] != 2) {
                                                    if (DEBUG)
                                                        appendToLog("command COMMAND_BEGIN is found without first byte as 0x01 or 0x02, " + byteArrayToString(dataInPayload));
                                                } else if (csReaderConnector.rfidConnector.mRfidToWrite.size() == 0) {
                                                    if (DEBUG)
                                                        appendToLog("command COMMAND_BEGIN is found without mRfidToWrite");
                                                } else {
                                                    byte[] dataWritten = csReaderConnector.rfidConnector.mRfidToWrite.get(0).dataValues;
                                                    if (dataWritten == null) {
                                                    } else if (!(dataWritten[0] == (byte) 0x70 && dataWritten[1] == 1 && dataWritten[2] == 0 && dataWritten[3] == (byte) 0xF0)) {
                                                        if (DEBUG)
                                                            appendToLog("command COMMAND_BEGIN is found with invalid mRfidToWrite: " + byteArrayToString(dataWritten));
                                                    } else {
                                                        boolean matched = true;
                                                        for (int i = 0; i < 4; i++) {
                                                            if (dataWritten[4 + i] != dataIn[startIndex + 8 + i]) {
                                                                matched = false;
                                                                break;
                                                            }
                                                        }
                                                        long lValue = 0;
                                                        int multipler = 1;
                                                        for (int i = 0; i < 4; i++) {
                                                            lValue += (dataIn[startIndex + 12 + i] & 0xFF) * multipler;
                                                            multipler *= 256;
                                                        }
                                                        if (matched == false) {
                                                            if (DEBUG)
                                                                appendToLog("command COMMAND_BEGIN is found with mis-matched command:" + byteArrayToString(dataWritten));
                                                        } else {
                                                            csReaderConnector.rfidConnector.mRfidToWrite.remove(0);
                                                            csReaderConnector.rfidConnector.sendRfidToWriteSent = 0;
                                                            csReaderConnector.rfidConnector.mRfidToWriteRemoved = true;
                                                            if (DEBUG)
                                                                appendToLog("mmRfidToWrite remove 8");
                                                            setInventoring(true);
                                                            Date date = new Date();
                                                            long date_time = date.getTime();
                                                            long expected_firmware_ontime_ms = firmware_ontime_ms;
                                                            if (date_time_ms != 0) {
                                                                long firmware_ontime_ms_difference = date_time - date_time_ms;
                                                                if (firmware_ontime_ms_difference > 2000) {
                                                                    expected_firmware_ontime_ms += (firmware_ontime_ms_difference - 2000);
                                                                }
                                                            }
                                                            if (lValue < expected_firmware_ontime_ms) {
                                                                csReaderConnector.rfidReader.bFirmware_reset_before = true;
                                                                if (DEBUG)
                                                                    appendToLogView("command COMMAND_BEGIN --- Firmware reset before !!!");
                                                            }
                                                            firmware_ontime_ms = lValue;
                                                            date_time_ms = date_time;
                                                            if (DEBUG)
                                                                appendToLog("command COMMAND_BEGIN is found with packageLength=" + packageLengthRead + ", with firmware count=" + lValue + ", date_time=" + date_time + ", expected firmware count=" + expected_firmware_ontime_ms);
                                                            rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_BEGIN;
                                                        }
                                                    }
                                                }
                                                break;
                                            case 0x0001:
                                            case 0x8001:    //RFID_PACKET_TYPE_COMMAND_END  //original 1
                                                if (dataIn[startIndex + 0] != 1 && dataIn[startIndex + 0] != 2) {
                                                    if (DEBUG)
                                                        appendToLog("command COMMAND_END is found without first byte as 0x01 or 0x02, " + byteArrayToString(dataInPayload));
                                                    break;
                                                } else {
                                                    dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END;
                                                    setInventoring(false);
                                                    if (DEBUG)
                                                        appendToLog("command COMMAND_END is found with packageLength=" + packageLengthRead + ", length = " + dataA.dataValues.length + ", dataValues=" + byteArrayToString(dataA.dataValues));
                                                    if (dataA.dataValues.length >= 8) {
                                                        int status = dataA.dataValues[12 - 8] + dataA.dataValues[13 - 8] * 256;
                                                        if (status != 0)
                                                            dataA.decodedError = "Received COMMAND_END with status=" + String.format("0x%X", status) + ", error_port=" + dataA.dataValues[14 - 8];
                                                        if (dataA.decodedError != null)
                                                            if (DEBUG)
                                                                appendToLog(dataA.decodedError);
                                                        rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_END;
                                                    }
                                                }
                                                int oldSize = mRx000ToRead.size();
                                                mRx000ToRead.add(dataA);
                                                if (DEBUG)
                                                    appendToLog("oldSize = " + oldSize + ", after adding 8001 mRx000ToRead.size = " + mRx000ToRead.size());
                                                commandOperating = false;
                                                break;
                                            case 0x0005:
                                            case 0x8005:    //RFID_PACKET_TYPE_18K6C_INVENTORY  //original 5
                                                if (dataIn[startIndex + 0] != 3 && dataIn[startIndex + 0] != 4) {
                                                    if (DEBUG)
                                                        appendToLog("command 18K6C_INVENTORY is found without first byte as 0x03, 0x04, " + byteArrayToString(dataInPayload));
                                                    break;
                                                } else {
                                                    if (dataIn[startIndex + 0] == 3) {
                                                        dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                                                        if (true) {
                                                            boolean crcError;
                                                            if (dataA.dataValues.length < 12 + 4)
                                                                dataA.decodedError = "Received TYPE_18K6C_INVENTORY with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                                            else {
                                                                int epcLength = (dataA.dataValues[12] >> 3) * 2;
                                                                if (dataA.dataValues.length < 12 + 2 + epcLength + 2)
                                                                    dataA.decodedError = "Received TYPE_18K6C_INVENTORY with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                                                else {
                                                                    setInventoring(true);
                                                                    long time1 = dataA.dataValues[3] & 0x00FF;
                                                                    time1 = time1 << 8;
                                                                    time1 |= dataA.dataValues[2] & 0x00FF;
                                                                    time1 = time1 = time1 << 8;
                                                                    time1 |= dataA.dataValues[1] & 0x00FF;
                                                                    time1 = time1 = time1 << 8;
                                                                    time1 |= dataA.dataValues[0] & 0x00FF;
                                                                    dataA.decodedTime = time1;
                                                                    dataA.decodedRssi = decodeNarrowBandRSSI(dataA.dataValues[13 - 8]);

                                                                    byte bValue = dataA.dataValues[14 - 8];
                                                                    bValue &= 0x7F;
                                                                    if ((bValue & 0x40) != 0)
                                                                        bValue |= 0x80;
                                                                    dataA.decodedPhase = bValue;
                                                                    if (true) {
                                                                        int iValue = dataA.dataValues[14 - 8] & 0x3F; //0x7F;
                                                                        boolean b7 = false;
                                                                        if ((dataA.dataValues[14 - 8] & 0x80) != 0)
                                                                            b7 = true;
                                                                        iValue *= 90;
                                                                        iValue /= 32;
                                                                        dataA.decodedPhase = iValue;
                                                                    }

                                                                    dataA.decodedChidx = dataA.dataValues[15 - 8];
                                                                    dataA.decodedPort = dataA.dataValues[18 - 8];
                                                                    int data1_count = (dataA.dataValues[16 - 8] & 0xFF);
                                                                    data1_count *= 2;
                                                                    int data2_count = (dataA.dataValues[17 - 8] & 0xFF);
                                                                    data2_count *= 2;

                                                                    if (dataA.dataValues.length >= 12 + 2) {
                                                                        dataA.decodedPc = new byte[2];
                                                                        System.arraycopy(dataA.dataValues, 12, dataA.decodedPc, 0, dataA.decodedPc.length);
                                                                    }
                                                                    if (dataA.dataValues.length >= 12 + 2 + 2) {
                                                                        dataA.decodedEpc = new byte[dataA.dataValues.length - 12 - 4];
                                                                        System.arraycopy(dataA.dataValues, 12 + 2, dataA.decodedEpc, 0, dataA.decodedEpc.length);
                                                                        dataA.decodedCrc = new byte[2];
                                                                        System.arraycopy(dataA.dataValues, dataA.dataValues.length - 2, dataA.decodedCrc, 0, dataA.decodedCrc.length);
                                                                    }
                                                                    if (data1_count != 0 && dataA.dataValues.length - 2 - data1_count - data2_count >= 0) {
                                                                        dataA.decodedData1 = new byte[data1_count];
                                                                        System.arraycopy(dataA.dataValues, dataA.dataValues.length - 2 - data1_count - data2_count, dataA.decodedData1, 0, dataA.decodedData1.length);
                                                                    }
                                                                    if (data2_count != 0 && dataA.dataValues.length - 2 - data2_count >= 0) {
                                                                        dataA.decodedData2 = new byte[data2_count];
                                                                        System.arraycopy(dataA.dataValues, dataA.dataValues.length - 2 - data2_count, dataA.decodedData2, 0, dataA.decodedData2.length);
                                                                    }
                                                                    int iData12 = 0;
                                                                    if (dataA.decodedData1 != null) iData12 += dataA.decodedData1.length;
                                                                    if (dataA.decodedData2 != null) iData12 += dataA.decodedData2.length;
                                                                    if (iData12 != 0) {
                                                                        byte[] newEpc = new byte[dataA.decodedEpc.length - iData12];
                                                                        System.arraycopy(dataA.decodedEpc, 0, newEpc, 0, newEpc.length);
                                                                        dataA.decodedEpc = newEpc;
                                                                    }
                                                                    rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_INVENTORY;
                                                                }
                                                            }
                                                        }
                                                        int oldSize2 = mRx000ToRead.size();
                                                        mRx000ToRead.add(dataA);
                                                        if (utility.DEBUG_APDATA) {
                                                            appendToLog("ApData: dataValues = " + byteArrayToString(dataA.dataValues) + ", 1 decodedRssi = " + dataA.decodedRssi + ", decodedPhase = " + dataA.decodedPhase + ", decodedChidx = " + dataA.decodedChidx + ", decodedPort = " + dataA.decodedPort);
                                                            appendToLog("ApData: decodedPc/Epc/Crc = " + byteArrayToString(dataA.decodedPc) + ", " + byteArrayToString(dataA.decodedEpc) + ", " + byteArrayToString(dataA.decodedCrc)
                                                                    + ", decodedData1/2 = " + byteArrayToString(dataA.decodedData1) + ", " + byteArrayToString(dataA.decodedData2));
                                                        }
                                                        if (DEBUG)
                                                            appendToLog("oldSize = " + oldSize2 + ", after adding 8005 mRx000ToRead.size = " + mRx000ToRead.size());
                                                    } else {
                                                        dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                                        if (true) {
                                                            if (dataA.dataValues.length < 3)
                                                                dataA.decodedError = "Received TYPE_18K6C_INVENTORY with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                                            else {
                                                                int index = 0;
                                                                byte[] dataValuesFull = dataA.dataValues;
                                                                while (index < dataValuesFull.length) {
                                                                    dataA.decodedTime = System.currentTimeMillis();
                                                                    if (dataValuesFull.length >= index + 2) {
                                                                        dataA.decodedPc = new byte[2];
                                                                        System.arraycopy(dataValuesFull, index, dataA.decodedPc, 0, dataA.decodedPc.length);
                                                                        index += 2;
                                                                    } else {
                                                                        break;
                                                                    }
                                                                    int epcLength = ((dataA.decodedPc[0] & 0xFF) >> 3) * 2;
                                                                    if (dataValuesFull.length >= index + epcLength) {
                                                                        dataA.decodedEpc = new byte[epcLength];
                                                                        System.arraycopy(dataValuesFull, index, dataA.decodedEpc, 0, epcLength);
                                                                        index += epcLength;
                                                                    }
                                                                    if (dataValuesFull.length >= index + 1) {
                                                                        dataA.decodedRssi = decodeNarrowBandRSSI(dataValuesFull[index]);
                                                                        index++;
                                                                    }
                                                                    if (DEBUG)
                                                                        appendToLog((dataA.dataValues != null ? "mRfidToRead.size() = " + csReaderConnector.rfidConnector.mRfidToRead.size() + ", dataValues = " + byteArrayToString(dataA.dataValues) + ", " : "") + "2 decodedRssi = " + dataA.decodedRssi + ", decodedPc = " + byteArrayToString(dataA.decodedPc) + ", decodedEpc = " + byteArrayToString(dataA.decodedEpc));
                                                                    if (dataValuesFull.length > index) {
                                                                        mRx000ToRead.add(dataA);
                                                                        if (utility.DEBUG_APDATA) appendToLog("ApData: Got data to mRx000ToRead " + mRx000ToRead.size() + ", with decodedEpc = " + byteArrayToString(dataA.decodedEpc));

                                                                        int iDecodedPortOld = dataA.decodedPort;
                                                                        dataA = new RfidReaderChipData.Rx000pkgData();
                                                                        dataA.decodedPort = iDecodedPortOld;
                                                                        dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                                                        rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_COMPACT;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        int oldSize3 = mRx000ToRead.size();
                                                        mRx000ToRead.add(dataA);
                                                        if (utility.DEBUG_APDATA) appendToLog("ApData: Got data to mRx000ToRead " + mRx000ToRead.size() + ", with decodedEpc = " + byteArrayToString(dataA.decodedEpc));
                                                        if (DEBUG)
                                                            appendToLog("oldSize = " + oldSize3 + ", after adding 8005 mRx000ToRead.size = " + mRx000ToRead.size());
                                                    }
                                                    if (DEBUG)
                                                        appendToLog("command 18K6C_INVENTORY is found with data=" + byteArrayToString(dataA.dataValues));
                                                }
                                                break;
                                            case 6: //RFID_PACKET_TYPE_18K6C_TAG_ACCESS
                                                if (dataIn[startIndex + 0] != 1) {
                                                    if (DEBUG)
                                                        appendToLog("command 18K6C_TAG_ACCESS is found without first byte as 0x02, " + byteArrayToString(dataInPayload));
                                                    break;
                                                } else {
                                                    dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                                                    if (true) {
                                                        byte[] dataInPayload_full = new byte[expectedLength];
                                                        System.arraycopy(dataIn, startIndex, dataInPayload_full, 0, dataInPayload_full.length);
                                                        if (DEBUG)
                                                            appendToLog("command TYPE_18K6C_TAG_ACCESS is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(dataInPayload_full));
                                                    }
                                                    if (true) {
                                                        int accessError, backscatterError;
                                                        boolean timeoutError, crcError;
                                                        if (dataA.dataValues.length >= 8 + 12) {
                                                            backscatterError = 0;
                                                            accessError = 0;
                                                            timeoutError = false;
                                                            crcError = false;
                                                            if ((dataA.dataValues[1] & 8) != 0)
                                                                crcError = true;
                                                            else if ((dataA.dataValues[1] & 4) != 0)
                                                                timeoutError = true;
                                                            else if ((dataA.dataValues[1] & 2) != 0)
                                                                backscatterError = (dataA.dataValues[13] & 0xFF);
                                                            else if ((dataA.dataValues[1] & 1) != 0 && dataA.dataValues.length >= 8 + 12 + 4)
                                                                accessError = (dataA.dataValues[20] & 0xFF) + (dataA.dataValues[21] & 0xFF) * 256;

                                                            byte[] dataRead = new byte[dataA.dataValues.length - 20];
                                                            System.arraycopy(dataA.dataValues, 20, dataRead, 0, dataRead.length);
                                                            if (backscatterError == 0 && accessError == 0 && timeoutError == false && crcError == false) {
                                                                if ((dataA.dataValues[12] == (byte) 0xC3) || (dataA.dataValues[12] == (byte) 0xC4) || (dataA.dataValues[12] == (byte) 0xC5)
                                                                        || (dataA.dataValues[12] == (byte) 0xD5) || (dataA.dataValues[12] == (byte) 0xE2))
                                                                    dataA.decodedResult = "";
                                                                else if ((dataA.dataValues[12] == (byte) 0xC2) || (dataA.dataValues[12] == (byte) 0xE0))
                                                                    dataA.decodedResult = byteArrayToString(dataRead);
                                                                else
                                                                    dataA.decodedError = "Received TYPE_18K6C_TAG_ACCESS with unhandled command = " + String.valueOf(dataA.dataValues[12]) + ", data = " + byteArrayToString(dataA.dataValues);
                                                            } else {
                                                                dataA.decodedError = "Received TYPE_18K6C_TAG_ACCESS with Error ";
                                                                if (crcError)
                                                                    dataA.decodedError += "crcError=" + crcError + ", ";
                                                                if (timeoutError)
                                                                    dataA.decodedError += "timeoutError=" + timeoutError + ", ";
                                                                if (backscatterError != 0) {
                                                                    dataA.decodedError += "backscatterError:";
                                                                    String strErrorMessage = String.valueOf(backscatterError);
                                                                    switch (backscatterError) {
                                                                        case 3:
                                                                            strErrorMessage = "Specified memory location does not exist or the PC value is not supported by the tag";
                                                                            break;
                                                                        case 4:
                                                                            strErrorMessage = "Specified memory location is locked and/or permalocked and is not writeable";
                                                                            break;
                                                                        case 0x0B:
                                                                            strErrorMessage = "Tag has insufficient power to perform the memory write";
                                                                            break;
                                                                        case 0x0F:
                                                                            strErrorMessage = "Tag does not support error-specific codes";
                                                                        default:
                                                                            break;
                                                                    }
                                                                    dataA.decodedError += strErrorMessage + ", ";
                                                                }
                                                                if (accessError != 0) {
                                                                    dataA.decodedError += "accessError: ";
                                                                    String strErrorMessage = String.valueOf(accessError);
                                                                    switch (accessError) {
                                                                        case 0x01:
                                                                            strErrorMessage = "Read after write verify failed";
                                                                            break;
                                                                        case 0x02:
                                                                            strErrorMessage = "Problem transmitting tag command";
                                                                            break;
                                                                        case 0x03:
                                                                            strErrorMessage = "CRC error on tag response to a write";
                                                                            break;
                                                                        case 0x04:
                                                                            strErrorMessage = "CRC error on the read packet when verifying the write";
                                                                            break;
                                                                        case 0x05:
                                                                            strErrorMessage = "Maximum retries on the write exceeded";
                                                                            break;
                                                                        case 0x06:
                                                                            strErrorMessage = "Failed waiting for read data from tag, possible timeout";
                                                                            break;
                                                                        case 0x07:
                                                                            strErrorMessage = "Failure requesting a new tag handle";
                                                                            break;
                                                                        case 0x09:
                                                                            strErrorMessage = "Out of retries";
                                                                            break;
                                                                        case 0x0A:
                                                                            strErrorMessage = "Error waiting for tag response, possible timeout";
                                                                            break;
                                                                        case 0x0B:
                                                                            strErrorMessage = "CRC error on tag response to a kill";
                                                                            break;
                                                                        case 0x0C:
                                                                            strErrorMessage = "Problem transmitting 2nd half of tag kill";
                                                                            break;
                                                                        case 0x0D:
                                                                            strErrorMessage = "Tag responded with an invalid handle on first kill command";
                                                                            break;
                                                                        default:
                                                                            break;
                                                                    }
                                                                    dataA.decodedError += strErrorMessage + ", ";
                                                                }
                                                                dataA.decodedError += "data = " + byteArrayToString(dataA.dataValues);
                                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_ACCESS;
                                                            }
                                                        } else {
                                                            dataA.decodedError = "Received TYPE_18K6C_TAG_ACCESS with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                                        }
                                                    }
                                                }
                                                int oldSize4 = mRx000ToRead.size();
                                                mRx000ToRead.add(dataA);
                                                if (DEBUG)
                                                    appendToLog("oldSize = " + oldSize4 + ", after adding 0006 mRx000ToRead.size = " + mRx000ToRead.size());
                                                if (DEBUG) {
                                                    appendToLog("mRx000UplinkHandler(): package read = " + byteArrayToString(dataA.dataValues));
                                                }
                                                break;
                                            case 0x0007:
                                            case 0x8007:    //RFID_PACKET_TYPE_ANTENNA_CYCLE_END    //original 7
                                                if (dataIn[startIndex + 0] != 1 && dataIn[startIndex + 0] != 2) {
                                                    if (DEBUG)
                                                        appendToLog("command TYPE_ANTENNA_CYCLE_END is found without first byte as 0x01 or 0x02, " + byteArrayToString(dataInPayload));
                                                    break;
                                                } else {
                                                    dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_ANTENNA_CYCLE_END;
                                                    if (DEBUG)
                                                        appendToLog("command TYPE_ANTENNA_CYCLE_END is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(dataInPayload));
                                                    rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_END;
                                                }
                                                mRx000ToRead.add(dataA);
                                                break;
                                            case 0x000E:
                                                if (dataIn[startIndex + 0] != 1 && dataIn[startIndex + 0] != 2) {
                                                    if (DEBUG)
                                                        appendToLog("command TYPE_COMMAND_ACTIVE is found without first byte as 0x01 or 0x02, " + byteArrayToString(dataInPayload));
                                                    break;
                                                } else {
                                                    dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_ACTIVE;
                                                    if (DEBUG)
                                                        appendToLog("command TYPE_COMMAND_ACTIVE is found with packageLength=" + packageLengthRead + ", " + byteArrayToString(dataInPayload));
                                                    rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_ACTIVE;
                                                }
                                                mRx000ToRead.add(dataA);
                                                break;
                                            case 0x3005:    //RFID_PACKET_TYPE_MBP_READ
                                                int address = (dataIn[startIndex + 8] & 0xFF) + (dataIn[startIndex + 9] & 0xFF) * 256;
                                                switch (address) {
                                                    case 0x450:
                                                        rx000MbpSetting.rxGain = (dataIn[startIndex + 10] & 0xFF) + (dataIn[startIndex + 11] & 0xFF) * 256;
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_MBPREAD;
                                            case 0x3007:    //RFID_PACKET_TYPE_OEMCFG_READ
                                                address = (dataIn[startIndex + 8] & 0xFF) + (dataIn[startIndex + 9] & 0xFF) * 256 + (dataIn[startIndex + 10] & 0xFF) * 256 * 256 + (dataIn[startIndex + 11] & 0xFF) * 256 * 256 * 256;
                                                switch (address) {
                                                    case 0x02:
//                                                    dataIn[startIndex + 12] = 3;
                                                        rx000OemSetting.countryCode = (dataIn[startIndex + 12] & 0xFF) + (dataIn[startIndex + 13] & 0xFF) * 256 + (dataIn[startIndex + 14] & 0xFF) * 256 * 256 + (dataIn[startIndex + 15] & 0xFF) * 256 * 256 * 256;
                                                        if (DEBUG)
                                                            appendToLog("countryCode = " + rx000OemSetting.countryCode);
                                                        break;
                                                    case 0x04:
                                                    case 0x05:
                                                    case 0x06:
                                                    case 0x07:
                                                        System.arraycopy(dataIn, startIndex + 12, rx000OemSetting.serialNumber, 4 * (address - 4), 4);
                                                        break;
                                                    case 0x08:
                                                    case 0x09:
                                                    case 0x0A:
                                                        if (true) {
                                                            byte[] bytes = new byte[4];
                                                            System.arraycopy(dataIn, startIndex + 12, bytes, 0, 4);
                                                            appendToLog("product serial number, " + address + ": " + byteArrayToString(bytes));
                                                        }
                                                        System.arraycopy(dataIn, startIndex + 12, rx000OemSetting.productserialNumber, 4 * (address - 8), 4);
                                                        break;
                                                    case 0x0B:  //VERSIONCODE_MAX
                                                        System.arraycopy(dataIn, startIndex + 12, rx000OemSetting.productserialNumber, 4 * (address - 8), 4);
                                                        if (dataIn[startIndex + 12] == 0 && dataIn[startIndex + 13] == 0 && dataIn[startIndex + 14] == 0 && dataIn[startIndex + 15] == 0) {
                                                            rx000OemSetting.versionCode = 0;
                                                        } else if (dataIn[startIndex + 12] == 0x20 && dataIn[startIndex + 13] == 0x17 && dataIn[startIndex + 14] == 0) {
                                                            rx000OemSetting.versionCode = (dataIn[startIndex + 14] & 0xFF) + (dataIn[startIndex + 15] & 0xFF) * 256;
                                                        }
                                                        if (DEBUG)
                                                            appendToLog("versionCode = " + rx000OemSetting.versionCode);
                                                        break;
                                                    case 0x8E:
                                                /*dataIn[startIndex + 12] = 0x2A; //0x4F;
                                                dataIn[startIndex + 13] = 0x2A; //0x46;
                                                dataIn[startIndex + 14] = 0x4E; //0x41; //0x43;
                                                dataIn[startIndex + 15] = 0x5A; //0x53; //0x41; */
                                                        if (dataIn[startIndex + 12] == 0 || dataIn[startIndex + 13] == 0 || dataIn[startIndex + 14] == 0 || dataIn[startIndex + 15] == 0) {
                                                            rx000OemSetting.spcialCountryVersion = "";
                                                        } else {
                                                            rx000OemSetting.spcialCountryVersion = String.valueOf((char) dataIn[startIndex + 15])
                                                                    + String.valueOf((char) dataIn[startIndex + 14])
                                                                    + String.valueOf((char) dataIn[startIndex + 13])
                                                                    + String.valueOf((char) dataIn[startIndex + 12]);
                                                        }
                                                        byte[] dataInPart = new byte[4];
                                                        System.arraycopy(dataIn, startIndex + 12, dataInPart, 0, dataInPart.length);
                                                        if (DEBUG)
                                                            appendToLog("spcialCountryVersion = " + rx000OemSetting.spcialCountryVersion + ", data = " + byteArrayToString(dataInPart));
                                                        break;
                                                    case 0x8F:
                                                        //dataIn[startIndex + 12] = (byte)0xAA;
                                                        rx000OemSetting.freqModifyCode = (dataIn[startIndex + 12] & 0xFF) + (dataIn[startIndex + 13] & 0xFF) * 256 + (dataIn[startIndex + 14] & 0xFF) * 256 * 256 + (dataIn[startIndex + 15] & 0xFF) * 256 * 256 * 256;
                                                        if (DEBUG)
                                                            appendToLog("freqModifyCode = " + rx000OemSetting.freqModifyCode);
                                                        break;
                                                    default:
                                                        break;
                                                }
/*                                            if (address >= 4 && address <= 7) {
                                            for (int i = 0; i < 4; i++) {
                                                mRx000OemSetting.serialNumber[(address - 4) * 4 + i] = dataIn[startIndex + 12 + i];
                                            }
                                        }*/
                                                if (DEBUG)
                                                    appendToLog("command OEMCFG_READ is found with address = " + address + ", packageLength=" + packageLengthRead + ", " + byteArrayToString(dataInPayload));
                                                rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_OEMREAD;
                                                break;
                                            case 0x3008:    //RFID_PACKET_TYPE_ENG_RSSI
                                                if (DEBUG)
                                                    appendToLog("Hello123: RFID_PACKET_TYPE_ENG_RSSI S is found: " + byteArrayToString(dataInPayload));
                                                if ((dataIn[startIndex + 8] & 0x02) != 0) {
                                                    rx000EngSetting.narrowRSSI = (dataIn[startIndex + 28] & 0xFF) + (dataIn[startIndex + 29] & 0xFF) * 256;
                                                    rx000EngSetting.wideRSSI = (dataIn[startIndex + 30] & 0xFF) + (dataIn[startIndex + 31] & 0xFF) * 256;
                                                    if (DEBUG)
                                                        appendToLog("Hello123: narrorRSSI = " + String.format("%04X", rx000EngSetting.narrowRSSI) + ", wideRSSI = " + String.format("%04X", rx000EngSetting.wideRSSI));
                                                    rfidDataReadTypes = RfidDataReadTypes.RFID_DATA_READ_COMMAND_RSSI;
                                                }
                                                break;
                                            default:
                                                if (DEBUG)
                                                    appendToLog("command OTHERS is found: " + byteArrayToString(dataInPayload) + ", with packagelength=" + packageLengthRead + ", packageTypeRead=" + packageTypeRead);
                                                break;
                                        }
                                        packageFound = true;
                                        packageType = 3; //0x01 to 0x04
                                        startIndexNew = startIndex + expectedLength;
                                    }
                                }
                            }
                        }

                        if (packageFound) {
                            packageFound = false;
                            if (false && utility.DEBUG_APDATA)
                                appendToLog("ApData: found packageType " + packageType + " with mRfidToReadingOffset=" + mRfidToReadingOffset + ", startIndexOld= " + startIndexOld + ", startIndex= " + startIndex + ", startIndexNew=" + startIndexNew);
                            if (startIndex != startIndexOld) {
                                byte[] unhandledBytes = new byte[startIndex - startIndexOld];
                                System.arraycopy(mRfidToReading, startIndexOld, unhandledBytes, 0, unhandledBytes.length);
                                if (utility.DEBUG_APDATA)
                                    appendToLog("ApData: packageFound with invalid unused data: " + unhandledBytes.length + ", " + byteArrayToString(unhandledBytes));
                                invalidUpdata++;
                                writeDebug2File("Up4  invalid " + unhandledBytes.length + ", " + byteArrayToString(unhandledBytes));
                            } else if (startIndexNew != startIndex) {
                                data1 = new byte[startIndexNew - startIndex];
                                System.arraycopy(mRfidToReading, startIndex, data1, 0, data1.length);
                                String string = "Up4  " + (rfidDataReadTypes != null ? (rfidDataReadTypes.toString() + ", ") : "Unprocessed, ");
                                if (data1.length <= 8 && data1[0] == 0x40)
                                    string += (byteArrayToString(data1).substring(0, 4) + " " + byteArrayToString(data1).substring(4, 16));
                                else {
                                    string += (byteArrayToString(data1).substring(0, 8) + " " + byteArrayToString(data1).substring(8, 16) + " " + (byteArrayToString(data1).substring(16)));
                                }
                                utility.writeDebug2File(string);
                                if (utility.DEBUG_APDATA) appendToLog("ApData: Found startIndexNew=" + startIndexNew + ", string=" + string);
                                if (false && utility.DEBUG_APDATA) {
                                    byte[] usedBytes = new byte[startIndexNew - startIndex];
                                    System.arraycopy(mRfidToReading, startIndex, usedBytes, 0, usedBytes.length);
                                    appendToLog("ApData: used data = " + usedBytes.length + ", " + byteArrayToString(usedBytes));
                                }

                            }
                            byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                            System.arraycopy(mRfidToReading, startIndexNew, mRfidToReadingNew, 0, mRfidToReadingOffset - startIndexNew);
                            mRfidToReading = mRfidToReadingNew;
                            mRfidToReadingOffset -= startIndexNew;
                            startIndex = 0;
                            startIndexNew = 0;
                            startIndexOld = 0;
                            if (mRfidToReadingOffset != 0) {
                                byte[] remainedBytes = new byte[mRfidToReadingOffset];
                                System.arraycopy(mRfidToReading, 0, remainedBytes, 0, remainedBytes.length);
                                if (utility.DEBUG_APDATA)
                                    appendToLog("ApData: moved with remained bytes=" + byteArrayToString(remainedBytes));
                            }
                        } else {
                            startIndex++;
                        }
                    }
                    if (utility.DEBUG_APDATA && bLooping2) appendToLog("ApData: Exiting second loop with mRfidToReadingOffset = " + mRfidToReadingOffset + ", startIndex = " + startIndex);
                    if (startIndex != 0 && mRfidToReadingOffset != 0) {
                        if (utility.DEBUG_APDATA)
                            appendToLog("ApData: exit while(-8) loop with startIndex = " + startIndex + (startIndex == 0 ? "" : "(NON-ZERO)") + ", mRfidToReadingOffset=" + mRfidToReadingOffset);
                    }
                }
            }
            if (utility.DEBUG_APDATA && bLooping) appendToLog("ApData: Exiting loop with mRfidToRead.size as " + csReaderConnector.rfidConnector.mRfidToRead.size());
            if (mRfidToReadingOffset == startIndexNew && mRfidToReadingOffset != 0) {
                byte[] unusedData = new byte[mRfidToReadingOffset];
                System.arraycopy(mRfidToReading, 0, unusedData, 0, unusedData.length);
                if (DEBUG)
                    appendToLog("Up4  Invalid " + mRfidToReadingOffset + ", " + byteArrayToString(unusedData));
                mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
                mRfidToReadingOffset = 0;
                utility.writeDebug2File("Up4  Invalid " + byteArrayToString(unusedData));
            }
            if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): END");
            bRx000ToReading = false;
        }

        public boolean turnOn(boolean onStatus) {
            RfidConnector.CsReaderRfidData csReaderRfidData = new RfidConnector.CsReaderRfidData();
            if (onStatus) {
                csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_POWER_ON;
                csReaderRfidData.waitUplinkResponse = false;
                clearTempDataIn_request = true;
                addRfidToWrite(csReaderRfidData);
                return true;
            } else if (onStatus == false) {
                csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_POWER_OFF;
                csReaderRfidData.waitUplinkResponse = false;
                clearTempDataIn_request = true;
                addRfidToWrite(csReaderRfidData);
                return true;
            } else {
                return false;
            }
        }

        public boolean sendControlCommand(ControlCommands controlCommands) {
            byte[] msgBuffer = new byte[]{(byte) 0x40, 6, 0, 0, 0, 0, 0, 0};
            boolean needResponse = false;
            if (csReaderConnector.isConnected() == false) return false;
            switch (controlCommands) {
                default:
                    msgBuffer = null;
                case CANCEL:
                    msgBuffer[1] = 1;
                    commandOperating = false;
                    break;
                case SOFTRESET:
                    msgBuffer[1] = 2;
                    needResponse = true;
                    break;
                case ABORT:
                    msgBuffer[1] = 3;
                    needResponse = true;
                    commandOperating = false;
                    break;
                case PAUSE:
                    msgBuffer[1] = 4;
                    break;
                case RESUME:
                    msgBuffer[1] = 5;
                    break;
                case GETSERIALNUMBER:
                    msgBuffer = new byte[]{(byte) 0xC0, 0x06, 0, 0, 0, 0, 0, 0};
                    needResponse = true;
                    break;
                case RESETTOBOOTLOADER:
                    msgBuffer[1] = 7;
                    needResponse = true;
                    break;
            }

            if (msgBuffer == null) {
                if (DEBUG) appendToLog("Invalid control commands");
                return false;
            } else {
                clearTempDataIn_request = true;

                RfidConnector.CsReaderRfidData csReaderRfidData = new RfidConnector.CsReaderRfidData();
                csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_COMMAND;
                csReaderRfidData.dataValues = msgBuffer;
                if (needResponse) {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRx000ToWrite");
                    csReaderRfidData.waitUplinkResponse = needResponse;
                    addRfidToWrite(csReaderRfidData);
//                    mRx000ToWrite.add(cs108RfidData);
                } else {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRfidToWrite");
                    csReaderRfidData.waitUplinkResponse = needResponse;
                    addRfidToWrite(csReaderRfidData);
                }
                if (controlCommands == ControlCommands.ABORT) aborting = true;
                return true;
            }
        }

        boolean sendHostRegRequestHST_RFTC_FRQCH_DESC_PLLDIVMULT(int freqChannel) {
            long fccFreqTable[] = new long[]{
                    0x00180E4F, //915.75 MHz
                    0x00180E4D, //915.25 MHz
                    0x00180E1D, //903.25 MHz
                    0x00180E7B, //926.75 MHz
                    0x00180E79, //926.25 MHz
                    0x00180E21, //904.25 MHz
                    0x00180E7D, //927.25 MHz
                    0x00180E61, //920.25 MHz
                    0x00180E5D, //919.25 MHz
                    0x00180E35, //909.25 MHz
                    0x00180E5B, //918.75 MHz
                    0x00180E57, //917.75 MHz
                    0x00180E25, //905.25 MHz
                    0x00180E23, //904.75 MHz
                    0x00180E75, //925.25 MHz
                    0x00180E67, //921.75 MHz
                    0x00180E4B, //914.75 MHz
                    0x00180E2B, //906.75 MHz
                    0x00180E47, //913.75 MHz
                    0x00180E69, //922.25 MHz
                    0x00180E3D, //911.25 MHz
                    0x00180E3F, //911.75 MHz
                    0x00180E1F, //903.75 MHz
                    0x00180E33, //908.75 MHz
                    0x00180E27, //905.75 MHz
                    0x00180E41, //912.25 MHz
                    0x00180E29, //906.25 MHz
                    0x00180E55, //917.25 MHz
                    0x00180E49, //914.25 MHz
                    0x00180E2D, //907.25 MHz
                    0x00180E59, //918.25 MHz
                    0x00180E51, //916.25 MHz
                    0x00180E39, //910.25 MHz
                    0x00180E3B, //910.75 MHz
                    0x00180E2F, //907.75 MHz
                    0x00180E73, //924.75 MHz
                    0x00180E37, //909.75 MHz
                    0x00180E5F, //919.75 MHz
                    0x00180E53, //916.75 MHz
                    0x00180E45, //913.25 MHz
                    0x00180E6F, //923.75 MHz
                    0x00180E31, //908.25 MHz
                    0x00180E77, //925.75 MHz
                    0x00180E43, //912.75 MHz
                    0x00180E71, //924.25 MHz
                    0x00180E65, //921.25 MHz
                    0x00180E63, //920.75 MHz
                    0x00180E6B, //922.75 MHz
                    0x00180E1B, //902.75 MHz
                    0x00180E6D, //923.25 MHz
            };
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0C, 0, 0, 0, 0};
            if (freqChannel >= 50) {
                freqChannel = 49;
            }
            long freqData = fccFreqTable[freqChannel];
            msgBuffer[4] = (byte) (freqData % 256);
            msgBuffer[5] = (byte) ((freqData >> 8) % 256);
            msgBuffer[6] = (byte) ((freqData >> 16) % 256);
            msgBuffer[7] = (byte) ((freqData >> 24) % 256);
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, true, msgBuffer);
        }

        boolean bLowPowerStandby = false;

        public boolean setPwrManagementMode(boolean bLowPowerStandby) {
            if (false) appendToLog("pwrMgmtStatus: setPwrManagementMode(" + bLowPowerStandby + ")");
            if (true) return false; //ignore this setting
            if (this.bLowPowerStandby == bLowPowerStandby) return true;
            boolean result = rx000Setting.writeMAC(0x200, (bLowPowerStandby ? 1 : 0));
            if (result) {
                result = sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_SETPWRMGMTCFG);
                this.bLowPowerStandby = bLowPowerStandby;
                rx000Setting.getPwrMgmtStatus();
            }
            return result;
        }

        public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
            long hostCommandData = -1;
            switch (hostCommand) {
                case CMD_WROEM:
                    hostCommandData = 0x02;
                    break;
                case CMD_RDOEM:
                    hostCommandData = 0x03;
                    break;
                case CMD_ENGTEST:
                    hostCommandData = 0x04;
                    break;
                case CMD_MBPRDREG:
                    hostCommandData = 0x05;
                    break;
                case CMD_MBPWRREG:
                    hostCommandData = 0x06;
                    break;
                case CMD_18K6CINV:
                    hostCommandData = 0x0F;
                    break;
                case CMD_18K6CREAD:
                    hostCommandData = 0x10;
                    break;
                case CMD_18K6CWRITE:
                    hostCommandData = 0x11;
                    break;
                case CMD_18K6CLOCK:
                    hostCommandData = 0x12;
                    break;
                case CMD_18K6CKILL:
                    hostCommandData = 0x13;
                    break;
                case CMD_SETPWRMGMTCFG:
                    hostCommandData = 0x14;
                    break;
                case CMD_UPDATELINKPROFILE:
                    hostCommandData = 0x19;
                    break;
                case CMD_18K6CBLOCKWRITE:
                    hostCommandData = 0x1F;
                    break;
                case CMD_CHANGEEAS:
                    hostCommandData = 0x26;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommandData = 0x3b;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommandData = 0x50;
                    break;
                case CMD_READBUFFER:
                    hostCommandData = 0x51;
                    break;
                case CMD_UNTRACEABLE:
                    hostCommandData = 0x52;
                    break;
                case CMD_FDM_RDMEM:
                    hostCommandData = 0x53;
                    break;
                case CMD_FDM_WRMEM:
                    hostCommandData = 0x54;
                    break;
                case CMD_FDM_AUTH:
                    hostCommandData = 0x55;
                    break;
                case CMD_FDM_GET_TEMPERATURE:
                    hostCommandData = 0x56;
                    break;
                case CMD_FDM_START_LOGGING:
                    hostCommandData = 0x57;
                    break;
                case CMD_FDM_STOP_LOGGING:
                    hostCommandData = 0x58;
                    break;
                case CMD_FDM_WRREG:
                    hostCommandData = 0x59;
                    break;
                case CMD_FDM_RDREG:
                    hostCommandData = 0x5A;
                    break;
                case CMD_FDM_DEEP_SLEEP:
                    hostCommandData = 0x5B;
                    break;
                case CMD_FDM_OPMODE_CHECK:
                    hostCommandData = 0x5C;
                    break;
                case CMD_FDM_INIT_REGFILE:
                    hostCommandData = 0x5d;
                    break;
                case CMD_FDM_LED_CTRL:
                    hostCommandData = 0x5e;
                    break;
            }
            if (hostCommandData == -1) {
                return false;
            } else {
                commandOperating = true;
                byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, (byte) 0xf0, 0, 0, 0, 0};
                msgBuffer[4] = (byte) (hostCommandData % 256);
                msgBuffer[5] = (byte) ((hostCommandData >> 8) % 256);
                msgBuffer[6] = (byte) ((hostCommandData >> 16) % 256);
                msgBuffer[7] = (byte) ((hostCommandData >> 24) % 256);
                return sendHostRegRequest(HostRegRequests.HST_CMD, true, msgBuffer);
            }
        }

        ArrayList<byte[]> macAccessHistory = new ArrayList<>();

        boolean bifMacAccessHistoryData(byte[] msgBuffer) {
            if (sameCheck == false) return false;
            if (msgBuffer.length != 8) return false;
            if (msgBuffer[0] != (byte) 0x70 || msgBuffer[1] != 1) return false;
            if (msgBuffer[2] == 0 && msgBuffer[3] == (byte) 0xF0) return false;
            return true;
        }

        int findMacAccessHistory(byte[] msgBuffer) {
            int i = -1;
            for (i = 0; i < macAccessHistory.size(); i++) {
//                appendToLog("macAccessHistory(" + i + ")=" + byteArrayToString(macAccessHistory.get(i)));
                if (Arrays.equals(macAccessHistory.get(i), msgBuffer)) break;
            }
            if (i == macAccessHistory.size()) i = -1;
            if (i >= 0)
                appendToLog("macAccessHistory: returnValue = " + i + ", msgBuffer=" + byteArrayToString(msgBuffer));
            return i;
        }

        void addMacAccessHistory(byte[] msgBuffer) {
            boolean DEBUG = false;
            byte[] msgBuffer4 = Arrays.copyOf(msgBuffer, 4);
            for (int i = 0; i < macAccessHistory.size(); i++) {
                byte[] macAccessHistory4 = Arrays.copyOf(macAccessHistory.get(i), 4);
                if (Arrays.equals(msgBuffer4, macAccessHistory4)) {
                    if (DEBUG)
                        appendToLog("macAccessHistory: deleted old record=" + byteArrayToString(macAccessHistory4));
                    macAccessHistory.remove(i);
                    break;
                }
            }
            if (DEBUG)
                appendToLog("macAccessHistory: added msgbuffer=" + byteArrayToString(msgBuffer));
            macAccessHistory.add(msgBuffer);
        }

        boolean sendHostRegRequest(HostRegRequests hostRegRequests, boolean writeOperation, byte[] msgBuffer) {
            boolean needResponse = false;
            boolean validRequest = false;

            if (csReaderConnector.isConnected() == false) return false;
            addMacAccessHistory(msgBuffer);
            switch (hostRegRequests) {
                case MAC_OPERATION:
                case HST_ANT_CYCLES:
                case HST_ANT_DESC_SEL:
                case HST_ANT_DESC_CFG:
                case MAC_ANT_DESC_STAT:
                case HST_ANT_DESC_PORTDEF:
                case HST_ANT_DESC_DWELL:
                case HST_ANT_DESC_RFPOWER:
                case HST_ANT_DESC_INV_CNT:
                    validRequest = true;
                    break;
                case HST_TAGMSK_DESC_SEL:
                case HST_TAGMSK_DESC_CFG:
                case HST_TAGMSK_BANK:
                case HST_TAGMSK_PTR:
                case HST_TAGMSK_LEN:
                case HST_TAGMSK_0_3:
                    validRequest = true;
                    break;
                case HST_QUERY_CFG:
                case HST_INV_CFG:
                case HST_INV_SEL:
                case HST_INV_ALG_PARM_0:
                case HST_INV_ALG_PARM_1:
                case HST_INV_ALG_PARM_2:
                case HST_INV_ALG_PARM_3:
                case HST_INV_RSSI_FILTERING_CONFIG:
                case HST_INV_RSSI_FILTERING_THRESHOLD:
                case HST_INV_RSSI_FILTERING_COUNT:
                case HST_INV_EPC_MATCH_CFG:
                case HST_INV_EPCDAT_0_3:
                    validRequest = true;
                    break;
                case HST_TAGACC_DESC_CFG:
                case HST_TAGACC_BANK:
                case HST_TAGACC_PTR:
                case HST_TAGACC_CNT:
                case HST_TAGACC_LOCKCFG:
                case HST_TAGACC_ACCPWD:
                case HST_TAGACC_KILLPWD:
                case HST_TAGWRDAT_SEL:
                case HST_TAGWRDAT_0:
                    validRequest = true;
                    break;
                case HST_RFTC_CURRENT_PROFILE:
                case HST_RFTC_FRQCH_SEL:
                case HST_RFTC_FRQCH_CFG:
                case HST_RFTC_FRQCH_DESC_PLLDIVMULT:
                case HST_RFTC_FRQCH_DESC_PLLDACCTL:
                case HST_RFTC_FRQCH_CMDSTART:
                    validRequest = true;
                    break;
                case HST_AUTHENTICATE_CFG:
                case HST_AUTHENTICATE_MSG:
                case HST_READBUFFER_LEN:
                case HST_UNTRACEABLE_CFG:
                    validRequest = true;
                    break;
                case HST_CMD:
                    validRequest = true;
                    needResponse = true;
                    break;
            }

            boolean DEBUG = false;
            if (DEBUG)
                appendToLog("checking msgbuffer = " + (msgBuffer == null ? "NULL" : "Valid") + ", validRequst = " + validRequest);
            if (msgBuffer == null || validRequest == false) {
                if (DEBUG) appendToLog("Invalid HST_REQ_REQ or null message");
                return false;
            } else {
                if (DEBUG) appendToLog("True Ending 0");
                RfidConnector.CsReaderRfidData csReaderRfidData = new RfidConnector.CsReaderRfidData();
                csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_COMMAND;
                csReaderRfidData.dataValues = msgBuffer;
                if (needResponse || writeOperation == false) {
                    csReaderRfidData.waitUplinkResponse = (needResponse || writeOperation == false);
//                    mRx000ToWrite.add(cs108RfidData);
                    addRfidToWrite(csReaderRfidData);
                } else {
                    csReaderRfidData.waitUplinkResponse = (needResponse || writeOperation == false);
                    addRfidToWrite(csReaderRfidData);
                }
                if (DEBUG) appendToLog("True Ending");
                return true;
            }
        }

        void addRfidToWrite(RfidConnector.CsReaderRfidData csReaderRfidData) {
            boolean repeatRequest = false;
            if (csReaderConnector.rfidConnector.mRfidToWrite.size() != 0) {
                RfidConnector.CsReaderRfidData csReaderRfidData1 = csReaderConnector.rfidConnector.mRfidToWrite.get(csReaderConnector.rfidConnector.mRfidToWrite.size() - 1);
                if (csReaderRfidData.rfidPayloadEvent == csReaderRfidData1.rfidPayloadEvent) {
                    if (csReaderRfidData.dataValues == null && csReaderRfidData1.dataValues == null) {
                        repeatRequest = true;
                    } else if (csReaderRfidData.dataValues != null && csReaderRfidData1.dataValues != null) {
                        if (csReaderRfidData.dataValues.length == csReaderRfidData1.dataValues.length) {
                            if (compareArray(csReaderRfidData.dataValues, csReaderRfidData1.dataValues, csReaderRfidData.dataValues.length)) {
                                repeatRequest = true;
                            }
                        }
                    }
                }
            }
            if (repeatRequest == false) {
                byte[] bytesCmd = new byte[] { 0x70, 1, 0, 0x09 };
                byte[] bytesCmd1 = new byte[] { 0x70, 1, 1, 0x09 };
                if (csReaderRfidData.dataValues != null) {
                    byte[] bytesNew = new byte[4];
                    System.arraycopy(csReaderRfidData.dataValues, 0, bytesNew, 0, bytesNew.length);
                    if (Arrays.equals(bytesCmd, bytesNew) || Arrays.equals(bytesCmd1, bytesNew)) {
                        int i = 0;
                        for (; i < csReaderConnector.rfidConnector.mRfidToWrite.size(); i++) {
                            if (csReaderConnector.rfidConnector.mRfidToWrite.get(i).dataValues != null) {
                                byte[] bytesOld = new byte[4];
                                System.arraycopy(csReaderConnector.rfidConnector.mRfidToWrite.get(i).dataValues, 0, bytesOld, 0, bytesOld.length);
                                if (Arrays.equals(bytesNew, bytesOld)) {
                                    //appendToLog("BtDataOut: matched data " + byteArrayToString(csReaderRfidData.dataValues));
                                    csReaderConnector.rfidConnector.mRfidToWrite.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            csReaderConnector.rfidConnector.mRfidToWrite.add(csReaderRfidData);
        }
    //}

    boolean inventoring = false;
    public boolean isInventoring() { return  inventoring; }
    void setInventoring(boolean enable) { inventoring = enable; utility.debugFileEnable(false); if (false) appendToLog("setInventoring R2000 is set as " + inventoring);}
}