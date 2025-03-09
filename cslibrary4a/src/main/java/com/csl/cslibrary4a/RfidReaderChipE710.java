package com.csl.cslibrary4a;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

import android.content.Context;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class RfidReaderChipE710 {
    boolean sameCheck = true;
    //RfidReaderChip mRfidReaderChip;
    int intervalRx000UplinkHandler;
    boolean aborting = false;
    Context context; Utility utility; CsReaderConnector csReaderConnector;
    public RfidReaderChipE710(Context context, Utility utility, CsReaderConnector csReaderConnector) {
        this.context = context;
        this.utility = utility;
        //mRfidReaderChip = new RfidReaderChip();
        this.csReaderConnector = csReaderConnector;
        this.intervalRx000UplinkHandler = csReaderConnector.intervalRx000UplinkHandler;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    void writeDebug2File(String stringDebug) { utility.writeDebug2File(stringDebug); }
    private String byteArray2DisplayString(byte[] byteData) { return utility.byteArray2DisplayString(byteData); }
    private int byteArrayToInt(byte[] bytes) { return utility.byteArrayToInt(bytes); }
    private double get2BytesOfRssi(byte[] bytes, int index) { return utility.get2BytesOfRssi(bytes, index); }
    enum ControlCommands {
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
                diagnosticCfg = mDefault.diagnosticCfg;
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
                invAlgo = mDefault.invAlgo;
                matchRep = mDefault.matchRep;
                tagSelect = mDefault.tagSelect;
                noInventory = mDefault.noInventory;
                tagDelay = mDefault.tagDelay;
                invModeCompact = mDefault.tagJoin;
                invBrandId = mDefault.brandid;
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
                //accessRetry = mDefault.accessRetry;
                //accessBank = mDefault.accessBank; accessBank2 = mDefault.accessBank2;
                //accessOffset = mDefault.accessOffset; accessOffset2 = mDefault.accessOffset2;
                //accessCount = mDefault.accessCount; accessCount2 = mDefault.accessCount2;
                //accessLockAction = mDefault.accessLockAction;
                //accessLockMask = mDefault.accessLockMask;
                //long accessPassword = 0;
                //long killPassword = 0;
                //accessWriteDataSelect = mDefault.accessWriteDataSelect;
                //accWriteDataReady = mDefault.accWriteDataReady;

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

        boolean readMAC(int address, int length) {
            byte[] msgBuffer = new byte[]{(byte) 0x80, (byte)0xb3, 0x14, 0x71, 0, 0, 4,   1, 0, 8, 0};
            msgBuffer[8] = (byte) ((address >> 8) % 256);
            msgBuffer[9] = (byte) (address % 256);
            msgBuffer[10] = (byte) (length & 0xFF);
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, false, msgBuffer);
        }
        boolean writeMAC(int address, byte[] bytes, boolean bReady) {
            //if (address != 0x3031
            //        && address != 0x3014
            //        && address != 0x3033
            //        && address != 0x303E
            //        && address != 0x3038
            //        && address != 0x3140
            //)
            if (false && address == 0x3035) {
                Logger.trace(String.format("0 writeMAC[address = 0x%X, bytes = %s with antennaPortConfig = %s", address, byteArrayToString(bytes), byteArrayToString(rx000Setting.getAntennaPortConfig(0))));
                //bytes[1] = 0x1E; //(byte)0x86; //orginal 6, new 0x9E
                //bytes[8] = 1; //original 1, new 8
                Logger.trace(String.format("0A writeMAC[address = 0x%X, bytes = %s with antennaPortConfig = %s", address, byteArrayToString(bytes), byteArrayToString(rx000Setting.getAntennaPortConfig(0))));
                //return true;
            }
            byte[] header = new byte[] {(byte) 0x80, (byte)0xb3, (byte)0x9A, 6, 0, 0, 4,   1, 0, 8, 0 };
            byte[] msgBuffer = new byte[header.length + bytes.length];
            int iPayloadLength = 4 + bytes.length;
            System.arraycopy(header, 0, msgBuffer, 0, header.length);
            msgBuffer[5] = (byte)((iPayloadLength / 256) & 0xFF);
            msgBuffer[6] = (byte)((iPayloadLength % 256) & 0xFF);
            msgBuffer[8] = (byte) ((address >> 8) & 0xFF);
            msgBuffer[9] = (byte) (address & 0xFF);
            msgBuffer[10] = (byte) (bytes.length & 0XFF);
            System.arraycopy(bytes, 0, msgBuffer, 11, bytes.length);
            Logger.trace(String.format("3 writeMAC with address = 0x%X, msgBuffer = ", address) + byteArrayToString(msgBuffer));
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, true, msgBuffer);
        }
        public boolean readMAC(int address) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 0, 0, 0, 0, 0};
            msgBuffer[2] = (byte) (address % 256);
            msgBuffer[3] = (byte) ((address >> 8) % 256);
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, false, msgBuffer);
        }
        public boolean writeMAC(int address, long value) {
            byte[] msgBuffer = null;
            Logger.trace(String.format("3A setTagFocus with address = 0x%X, value = 0x%X ", address, value));
            return sendHostRegRequest(HostRegRequests.MAC_OPERATION, true, msgBuffer);
        }

        String macVer = null; int macVerBuild = -1;
        public String getMacVer() {
            if (macVerBuild < 0) readMAC(0x28, 4);
            if (macVer == null) readMAC(8, 0x20);
            if (macVerBuild < 0 || macVer == null) return null;
            String strValue = macVer + " b" + macVerBuild;
            Logger.trace("2 getMacVer = {}", strValue);
            return strValue;
        }

        int authenticateConfig = -1;
        public boolean setAuthenticateConfig(int authenticateConfig) {
            byte[] data = new byte[3];
            data[0] = (byte) ((authenticateConfig >> 16) & 0xFF);
            data[1] = (byte) ((authenticateConfig >> 8) & 0xFF);
            data[2] = (byte) (authenticateConfig & 0xFF);
            Logger.trace("AAA: writing 390E with data {}", byteArrayToString(data));
            boolean bValue = writeMAC(0x390E, data, true);
            if (bValue) this.authenticateConfig = authenticateConfig;
            //readMAC3(0x390E, 3);
            return bValue;
        }
        public boolean setAuthenticateMessage(byte[] authenticateMessage) {
            int length = authenticateMessage.length;
            if (length > 32) length = 32;
            byte[] data = new byte[length];
            System.arraycopy(authenticateMessage, 0, data, 0, length);
            Logger.trace("AAA: writing 3912 with data {}", byteArrayToString(data));
            boolean bValue = writeMAC(0x3912, data, true);
            //readMAC3(0x3912, 32);
            return bValue;
        }
        public boolean setAuthenticateResponseLen(int authenticateResponseLen) {
            byte[] data = new byte[2];
            data[0] = (byte) ((authenticateResponseLen >> 8) & 0xFF);
            data[1] = (byte) (authenticateResponseLen & 0xFF);
            Logger.trace("AAA: writing 3944 with data {}", byteArrayToString(data));
            boolean bValue = writeMAC(0x3944, data, true);
            //readMAC3(0x3944, 2);
            return bValue;
        }

        byte[] currentPort = null;
        public byte getCurrentPort() {
            byte byValue = (byte)-1;
            if (currentPort != null && currentPort.length == 1) byValue = currentPort[0];
            else readMAC(0x3948, 1);
            Logger.trace("byValue = {}", byValue);
            return byValue;
        }
        boolean setCurrentPort(byte currentPortNew) {
            if (currentPortNew >= 0 && currentPort.length == 1 && currentPort[0] == currentPortNew && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with currentPortNew = {}", currentPortNew);
                return true;
            }
            byte[] bytes = new byte[1];
            bytes[0] = currentPortNew;
            boolean bValue;
            bValue = rx000Setting.writeMAC(0x3948, bytes, true);
            Logger.trace("new currentPort = {}, old currentPort = {}", byteArrayToString(bytes), byteArrayToString(currentPort));
            if (bValue) currentPort = bytes;
            return true;
        }
        public boolean updateCurrentPort() {
            byte currentPortOld = getCurrentPort();
            byte currentPortNew = 0;
            for (int i = 0; i < 16; i++) {
                if (rx000Setting.antennaPortConfig[i] != null) {
                    if (rx000Setting.antennaPortConfig[i][0] != 0) {
                        currentPortNew = (byte)(i & 0xFF);
                    }
                }
            }
            Logger.trace("currentPortOld = {}, currentPortNew = {}", currentPortOld, currentPortNew);
            boolean bValue = false;
            if (currentPortOld != currentPortNew) bValue = setCurrentPort(currentPortNew);
            return bValue;
        }

        byte[] modelCode;
        public String getModelCode() {
            String strValue = null;
            if (modelCode == null) readMAC(0x5000, 32);
            else {
                strValue = byteArray2DisplayString(modelCode);
                if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(modelCode).substring(0, 5);
            }
            return strValue;
        }

        byte[] productSerialNumber;
        public String getProductSerialNumber() {
            String strValue = null;
            if (productSerialNumber == null) {
                readMAC(0x5020, 32);
            } else {
                strValue = byteArray2DisplayString(productSerialNumber);
                if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(productSerialNumber).substring(0, 5);
                //string.substring(string.length() - 8, string.length());
            }
            return strValue;
        }

        byte[] countryEnum;
        public int getCountryEnum() {
            int iValue = -1;
            if (countryEnum == null) readMAC(0x3014, 2);
            else iValue = byteArrayToInt(countryEnum);
            Logger.trace("countryEnum = {}", iValue);
            return iValue;
        }
        public boolean setCountryEnum(short countryEnum) {
            byte[] data = new byte[2];
            data[0] = 0;
            data[1] = (byte) ((countryEnum) & 0xFF);
            if (this.countryEnum != null &&  compareArray(this.countryEnum, data, data.length) && sameCheck) return true;
            boolean bValue = writeMAC(0x3014, data, true);
            Logger.trace("new countryEnum = {}, with bValue = {}", byteArrayToString(data), bValue);
            if (bValue) this.countryEnum = data;
            return bValue;
        }

        byte[] frequencyChannelIndex;
        public int getFrequencyChannelIndex() {
            int iValue = -1;
            if (frequencyChannelIndex == null) readMAC(0x3018, 1);
            else iValue = byteArrayToInt(frequencyChannelIndex);
            Logger.trace("frequencyChannelIndex = {}", iValue);
            return iValue;
        }
        public boolean setFrequencyChannelIndex(byte frequencyChannelIndex) {
            byte[] data = new byte[1];
            data[0] = frequencyChannelIndex;
            if (this.frequencyChannelIndex != null && compareArray(this.frequencyChannelIndex, data, data.length) && sameCheck) return true;
            boolean bValue = writeMAC(0x3018, data, true);
            Logger.trace("new frequencyChannelIndex = {}, old frequencyChannelIndex = {}, with bValue = {}, sameCheck = {}", byteArrayToString(data), byteArrayToString(this.frequencyChannelIndex), bValue, sameCheck);
            if (bValue) this.frequencyChannelIndex = data;
            return bValue;
        }

        byte[] countryEnumOem;
        public int getCountryEnumOem() {
            int iValue = -1;
            if (countryEnumOem == null) readMAC(0x5040, 2);
            else iValue = byteArrayToInt(countryEnumOem);
            Logger.trace("countryEnumOem = {}", iValue);
            return iValue;
        }

        byte[] countryCodeOem;
        public int getCountryCodeOem() {
            int iValue = -1;
            if (countryCodeOem == null) readMAC(0xef98, 4);
            else iValue = byteArrayToInt(countryCodeOem);
            return iValue;
        }
        byte[] boardSerialNumber;
        public String getBoardSerialNumber() {
            if (boardSerialNumber == null) {
                readMAC(0xef9c, 16);
                return null;
            } else if (boardSerialNumber.length < 13) return null;
            else {
                byte[] retValue = new byte[boardSerialNumber.length];
                System.arraycopy(boardSerialNumber, 0, retValue, 0, boardSerialNumber.length);
                String string = new String(retValue).trim().replaceAll("[^\\x00-\\x7F]", "");
                if (string == null || string.length() == 0) return byteArrayToString(retValue).substring(0, 13);
                Logger.trace("String = {}, length = {}", string, string.length());
                if (retValue.length > 13) {
                    for (int i = 13; i < retValue.length; i++) {
                        if (retValue[i] < 0x30) retValue[i] += 0x30;
                    }
                }
                return (retValue == null ? null : new String(retValue).trim().replaceAll("[^\\x00-\\x7F]", ""));
            }
        }

        byte[] specialcountryCodeOem;
        public String getSpecialCountryCodeOem() {
            String strValue = null;
            if (specialcountryCodeOem == null) readMAC(0xefac, 4);
            else {
                strValue = byteArray2DisplayString(specialcountryCodeOem);
                //if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(modelCode).substring(0, 5);
            }
            return strValue;
        }

        byte[] freqModifyCode;
        public int getFreqModifyCode() {
            int iValue = -1;
            if (freqModifyCode == null) readMAC(0xefb0, 4);
            else iValue = byteArrayToInt(freqModifyCode);
            return iValue;
        }
        long mac_last_command_duration;
        final int DIAGCFG_INVALID = -1; final int DIAGCFG_MIN = 0; final int DIAGCFG_MAX = 0x3FF;
        int diagnosticCfg = DIAGCFG_INVALID;
        public int getAntennaPort() {
            Logger.trace("2 iAntennaPort = {}", antennaSelect);
            return antennaSelect;
        }
        byte[][] antennaPortConfig = new byte[16][];
        public byte[] getAntennaPortConfig(int iAntennaPort) {
            byte[] bytes = null;
            if (antennaPortConfig[iAntennaPort] == null) {
                Logger.trace("getAntennaPortConfig starts readMAC");
                readMAC(0x3030 + iAntennaPort * 16, 16);
            }
            else {
                bytes = new byte[antennaPortConfig[iAntennaPort].length];
                System.arraycopy(antennaPortConfig[iAntennaPort], 0, bytes, 0, bytes.length);
            }
            Logger.trace("getAntennaPortConfig[{}] = {}", iAntennaPort, byteArrayToString(antennaPortConfig[iAntennaPort]));
            return bytes;
        }

        int impinjExtensionValue = -1;
        public int getImpinjExtension() {
            int iValue = -1;
            Logger.debug("2 getImpinjExtension: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2A getImpinjExtension: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][5] & 0x06);
                Logger.debug(String.format("2b getImpinjExtension: iValue = 0x%X", iValue));
            }
            return iValue;
        }

        public boolean setImpinjExtension(boolean tagFocus, boolean fastId) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2 setImpinjExtension: tagFocus = {}", tagFocus);
                Logger.debug("2A setImpinjExtension: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][5];

                if (tagFocus) bytes[0] |= 0x04;
                else bytes[0] &= ~0x04;
                if (fastId) bytes[0] |= 0x02;
                else bytes[0] &= ~0x02;
                Logger.debug(String.format("2A1 setImpinjExtension: bytes = 0x%X", bytes[0]));
                boolean bSame = false;
                if (sameCheck | true) {
                    if (antennaPortConfig[antennaSelect][5] == bytes[0]) bSame = true;
                    Logger.debug("2ab setImpinjExtension: the array is the same = {}", bSame);
                }
                if (bSame) {
                    Logger.pkData(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 5));
                    bValue = true;
                } else {
                    Logger.trace("test 1");
                    bValue = writeMAC(0x3030 + antennaSelect * 16 + 5, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][5] = bytes[0];
                }
                Logger.debug("2b setImpinjExtension: with updated {}", byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }

        int iSelectPort = 0;
        public byte[][] selectConfiguration = new byte[7][];

        public byte[] getSelectConfiguration(int iSelectPort) {
            byte[] bytes = null;
            if (selectConfiguration[iSelectPort] == null) {
                Logger.trace("getSelectConfiguration starts readMAC");
                readMAC(0x3140 + iSelectPort * 42, 42);
            }
            else {
                bytes = new byte[selectConfiguration[iSelectPort].length];
                System.arraycopy(selectConfiguration[iSelectPort], 0, bytes, 0, bytes.length);
            }
            Logger.trace("getSelectConfiguration[{}] = {}", iSelectPort, byteArrayToString(selectConfiguration[iSelectPort]));
            return bytes;
        }
        public boolean setSelectConfiguration(int index, boolean enable, int bank, int offset, byte[] mask, int target, int action, int delay) {
            boolean bValue = false;
            byte[] bytes = new byte[42];
            if (selectConfiguration[index] != null) System.arraycopy(selectConfiguration[index], 0, bytes, 0, bytes.length);
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            if (mask != null) {
                int iWdith = mask.length;
                if (iWdith > 32) iWdith = 32;
                System.arraycopy(mask, 0, bytes, 7, iWdith);
                bytes[6] = (byte) ((iWdith * 8) & 0xFF);
            } else bytes[6] = 0;
            bytes[39] = (byte) (target & 0xFF);
            bytes[40] = (byte) (action & 0xFF);
            bytes[41] = (byte) (delay & 0xFF);
            Logger.trace("1A writeMAC 0x3140");
            if (compareArray(selectConfiguration[index], bytes, bytes.length) && sameCheck) {
                Logger.trace("!!! Skip sending repeated data {} to address 0x3140", byteArrayToString(bytes));
                return true;
            }
            bValue = writeMAC(0x3140 + index * 42, bytes, true);
            if (bValue) selectConfiguration[index] = bytes;
            else Logger.trace("!!! Failed to send data {} to address 0x3140", byteArrayToString(bytes));
            return bValue;
        }

        byte[][] multibankReadConfig = new byte[3][];
        int getMultibankEnable(int iSelectPort) {
            int iValue = 0;
            if (multibankReadConfig[iSelectPort] != null) {
                if (multibankReadConfig[iSelectPort][0] != 0) iValue = multibankReadConfig[iSelectPort][0];
            }
            return iValue;
        }
        int getMultibankReadLength(int iSelectPort) {
            int iValue = 0;
            if (multibankReadConfig[iSelectPort] != null) {
                if (multibankReadConfig[iSelectPort][0] != 0) iValue = multibankReadConfig[iSelectPort][6];
            }
            return iValue;
        }
        public byte[] getMultibankReadConfig(int iSelectPort) {
            int iPortSize = 7;
            byte[] bytes = null;
            if (multibankReadConfig[iSelectPort] == null) {
                Logger.debug("getMultibankReadConfig starts readMAC");
                readMAC(0x3270 + iSelectPort * iPortSize, iPortSize);
            }
            else {
                bytes = new byte[multibankReadConfig[iSelectPort].length];
                System.arraycopy(multibankReadConfig[iSelectPort], 0, bytes, 0, bytes.length);
            }
            Logger.debug("getMultibankReadConfig[{}] = {}", iSelectPort, byteArrayToString(multibankReadConfig[iSelectPort]));
            return bytes;
        }
        byte[][] multibankWriteConfig = new byte[3][];
        boolean setMultibankReadConfig(int index, boolean enable, int bank, int offset, int length) {
            boolean bValue = false;
            byte[] bytes = new byte[7];
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            bytes[6] = (byte) (length & 0xFF);
            bValue = writeMAC(0x3270 + index * 7, bytes, true);
            if (bValue) multibankReadConfig[index] = bytes;
            return bValue;
        }

        boolean setMultibankWriteConfig(int index, boolean enable, int bank, int offset, int length, byte[] data) {
            boolean bValue = false;
            Logger.debug("Start with index = {}, enable = {}, bank = {}, offset = {}, length = {}, data = {}", index, enable, bank, offset, length, byteArrayToString(data));
            byte[] bytes = new byte[7+data.length];
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            bytes[6] = (byte) (length & 0xFF);
            System.arraycopy(data, 0, bytes, 7, data.length);
            Logger.debug("bytes = {}", byteArrayToString(bytes));
            bValue = writeMAC(0x3290 + index * 519, bytes, true);
            Logger.debug("After writeMAC, bValue = {}", bValue);
            if (bValue) multibankWriteConfig[index] = bytes;
            return bValue;
        }
        int pwrMgmtStatus = -1;
        void getPwrMgmtStatus() {
            Logger.trace("pwrMgmtStatus: getPwrMgmtStatus ");
            pwrMgmtStatus = -1; readMAC(0x204);
        }

        final int MBPADDR_INVALID = -1; final int MBPADDR_MIN = 0; final int MBPADDR_MAX = 0x1FFF;
        long mbpAddress = MBPADDR_INVALID;
        final int MBPDATA_INVALID = -1; final int MBPDATA_MIN = 0; final int MBPDATA_MAX = 0x1FFF;
        long mbpData = MBPDATA_INVALID;
        final int OEMADDR_INVALID = -1; final int OEMADDR_MIN = 0; final int OEMADDR_MAX = 0x1FFF;
        long oemAddress = OEMADDR_INVALID;
        final int OEMDATA_INVALID = -1; final int OEMDATA_MIN = 0; final int OEMDATA_MAX = 0x1FFF;
        long oemData = OEMDATA_INVALID;

        // Antenna block parameters
        final int ANTCYCLE_INVALID = -1; final int ANTCYCLE_MIN = 0; final int ANTCYCLE_MAX = 0xFFFF;
        int antennaCycle = ANTCYCLE_INVALID;
        public int getAntennaCycle() {
            if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) getHST_ANT_CYCLES();
            return antennaCycle;
        }
        public boolean setAntennaCycle(int antennaCycle) {
            if (antennaCycle == this.antennaCycle) return true;
            this.antennaCycle = antennaCycle;
            Logger.trace(String.format("!!! Skip setAntennaCycle[0x%X]", antennaCycle));
            return true;
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
            Logger.trace("3 Set HST_ANT_CYCLES");
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
            Logger.trace("Set HST_ANT_CYCLES");
            return setAntennaCycle(antennaCycle, freqAgile);
        }

        private boolean getHST_ANT_CYCLES() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 7, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_ANT_CYCLES, false, msgBuffer);
        }

        final int ANTSELECT_INVALID = -1; final int ANTSLECT_MIN = 0; final int ANTSELECT_MAX = 15;
        int antennaSelect = ANTSELECT_INVALID;  //default value = 0
        public boolean setAntennaSelect(int antennaSelect) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  antennaSelect = mDefault.antennaSelect;
            if (this.antennaSelect == antennaSelect && sameCheck) return true;
            this.antennaSelect = antennaSelect;
            Logger.trace("antennaSelect is set to {}", antennaSelect);
            return true;
        }

        AntennaSelectedData[] antennaSelectedData;
        public int getAntennaEnable() {
            int iValue = -1;
            if (antennaPortConfig[antennaSelect] == null) {
                rx000Setting.getAntennaPortConfig(antennaSelect);
                readMAC(0x3030 + antennaSelect * 16, 16);
                Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            } else {
                Logger.debug("2 getAntennaEnable");
                Logger.debug("2A getAntennaEnable: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][0] & 0xFF;
            }
            Logger.debug("2 getAntennaEnable: iValue = {}", iValue);
            return iValue;
        }
        public boolean setAntennaEnable(int antennaEnable) {
            boolean bValue = false;

            Logger.trace("antennaEnable is {}", antennaEnable);
            if (antennaEnable == 0) {
                boolean disableInvalid = true;
                for (int i = 0; i < 16; i++) {
                    Logger.trace("i = {}, antennaSelect = {}", i, antennaSelect);
                    if (i != antennaSelect && antennaPortConfig[i] != null) {
                        if (antennaPortConfig[i][0] != 0) disableInvalid = false;
                        Logger.trace("i = {}, disableInvalid = {}", i, disableInvalid);
                    }
                }
                Logger.trace("disableInvalid is {}", disableInvalid);
                if (disableInvalid) return false;
            }

            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2 setAntennaEnable with antennaEnable = {}", antennaEnable);
                Logger.debug("2A setAntennaEnable: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                if (antennaEnable > 0) bytes[0] = 1;
                else bytes[0] = 0;
                Logger.debug("2b setAntennaEnable: bytes = {}", byteArrayToString(bytes));
                bValue = writeMAC(0x3030 + antennaSelect * 16, bytes, true);
                if (bValue) antennaPortConfig[antennaSelect][0] = bytes[0];
                Logger.debug("2C setAntennaEnable: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            Logger.debug("2d getAntennaEnable: bValue = {}", bValue);
            return bValue;
        }

        public long getAntennaDwell() {
            long lValue = -1;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2 getAntennaDwell");
                Logger.debug("2A getAntennaDwell: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                lValue = (antennaPortConfig[antennaSelect][1] & 0xFF) << 8;
                lValue += (antennaPortConfig[antennaSelect][2] & 0xFF);
            }
            Logger.debug("2C getAntennaDwell: iValue = {}", lValue);
            return lValue;
        }

        public boolean setAntennaDwell(long antennaDwell) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2 setAntennaDwell: antennaDwell = {}", antennaDwell);
                Logger.debug("2A setAntennaDwell: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[2];
                bytes[0] = (byte)((antennaDwell/256) & 0xFF);
                bytes[1] = (byte)((antennaDwell%256) & 0xFF);
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[2];
                    System.arraycopy(antennaPortConfig[antennaSelect], 1, bytesOld, 0, bytesOld.length);
                    Logger.debug("2A2 setAntennaDwell: bytesOld = {}", byteArrayToString(bytesOld));
                    boolean bValue1 = compareArray(bytes, bytesOld, bytes.length);
                    Logger.debug("2ab setAntennaDwell: the array is the same = {}", bValue1);
                }
                bValue = writeMAC(0x3030 + antennaSelect * 16 + 1, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 1, bytes.length);
                Logger.debug("2b setAntennaDwell: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }

        public long getAntennaPower(int portNumber) {
            long lValue = -1;
            Logger.debug("2 getAntennaPower: portNumber = {}", portNumber);
            if (portNumber < 0) portNumber = antennaSelect;
            if (antennaPortConfig[portNumber] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", portNumber);
            else {
                Logger.debug("2A getAntennaPower: getAntennaPortConfig[{}] = {}", portNumber, byteArrayToString(antennaPortConfig[portNumber]));
                lValue = (antennaPortConfig[portNumber][3] & 0xFF) * 256;
                lValue += (antennaPortConfig[portNumber][4] & 0xFF);
                Logger.debug(String.format("2b getAntennaPower: lValue = 0x%X", lValue));
                lValue /= 10;
            }
            return lValue;
        }
        public boolean setAntennaPower(long antennaPower) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) {
                Logger.debug("4 bValue = {}", bValue);
                Logger.trace("!!! CANNOT continue as antennaPortConfig[{}] is null", antennaSelect);
            }
            else if (getAntennaPower(antennaSelect) == antennaPower && sameCheck) {
                Logger.debug("3 bValue = {}", bValue);
                return true;
            }
            else {
                Logger.debug("2 setAntennaPower: antennaPower = {}", antennaPower);
                Logger.debug("2A setAntennaPower: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                antennaPower *= 10;
                byte[] bytes = new byte[2];
                bytes[0] = (byte)((antennaPower/256) & 0xFF);
                bytes[1] = (byte)((antennaPower%256) & 0xFF);
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[2];
                    System.arraycopy(antennaPortConfig[antennaSelect], 3, bytesOld, 0, bytesOld.length);
                    Logger.debug("2A2 setAntennaPower: bytesOld = {}", byteArrayToString(bytesOld));
                    boolean bValue1 = compareArray(bytes, bytesOld, bytes.length);
                    Logger.debug("2ab setAntennaPower: the array is the same = {}", bValue1);
                }

                bValue = writeMAC(0x3030 + antennaSelect * 16 + 3, bytes, true);
                Logger.debug("2 bValue = {}", bValue);
                if (bValue) System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 3, bytes.length);
                Logger.debug("2b setAntennaPower: with updated {}", byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            Logger.debug("1 bValue = {}", bValue);
            return bValue;
        }

        long antennaInvCount = -1;
        public boolean setAntennaInvCount(long antennaInvCount) {
            if (antennaInvCount == this.antennaInvCount) return true;
            this.antennaInvCount = antennaInvCount;
            Logger.trace(String.format("!!! Skip setAntennaInvCount[0x%X]", antennaInvCount));
            return true;
        }

        //Tag select block parameters
        final int INVSELECT_INVALID = -1; final int INVSELECT_MIN = 0; final int INVSELECT_MAX = 7;
        public int invSelectIndex = INVSELECT_INVALID;
        public int getInvSelectIndex() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) {
                invSelectIndex = 0;
                Logger.trace("!!! Skip getInvSelectIndex with assumed value = 0");
            }
            return invSelectIndex;
        }
        public boolean setInvSelectIndex(int invSelect) {
            if (invSelect < INVSELECT_MIN || invSelect > INVSELECT_MAX) invSelect = mDefault.invSelectIndex;
            if (this.invSelectIndex == invSelect && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with invSelect = {}", invSelect);
                return true;
            }
            this.invSelectIndex = invSelect;
            Logger.trace("!!! Skip setInvSelectIndex[{}]", invSelect);
            return true;
        }

        InvSelectData[] invSelectData;
        public int getSelectEnable() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT getSelectEnable as selectConfiguration[{}] is null", invSelectIndex);
            else iValue = (byte)(selectConfiguration[invSelectIndex][0] & 0xFF);
            return iValue;
        }
        public boolean setSelectEnable(int enable, int selectTarget, int selectAction, int selectDelay) {
            boolean bValue = false;
            Logger.debug("Start with enable = {}, selectTarget = {}, selectAction = {}, selectDelay = {}", enable, selectTarget, selectAction, selectDelay);
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT setSelectEnable as selectConfiguration[{}] is null", invSelectIndex);
            else {
                Logger.debug("Old selectConfiguration {} = {}", invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[42];
                if (selectConfiguration[invSelectIndex] != null) System.arraycopy(selectConfiguration[invSelectIndex], 0, bytes, 0, bytes.length);
                bytes[0] = (byte)(enable & 0xFF);
                bytes[39] = (byte)(selectTarget & 0xFF);
                bytes[40] = (byte)(selectAction & 0xFF);
                bytes[41] = (byte)(selectDelay & 0xFF);;
                if (compareArray(selectConfiguration[invSelectIndex], bytes, bytes.length) && sameCheck) {
                    Logger.trace("!!! Skip sending repeated data {} to address 0x3140", byteArrayToString(bytes));
                    return true;
                }
                bValue = writeMAC(0x3140 + invSelectIndex * 42, bytes, true);
                Logger.debug("after writeMAC 0x3140, bValue = {}", bValue);
                if (bValue) selectConfiguration[invSelectIndex] = bytes;
                else Logger.trace("!!! Failed to send data {} to address 0x3140", byteArrayToString(bytes));
                Logger.debug("bytes = {}, new selectConfiguration {} = {}", byteArrayToString(bytes), invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            Logger.debug("End with bValue = {}", bValue);
            return bValue;
        }

        public int getSelectTarget() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT getSelectTarget as selectConfiguration[{}] is null", invSelectIndex);
            else iValue = (selectConfiguration[invSelectIndex][39] & 0xFF);
            return iValue;
        }

        public int getSelectAction() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT getSelectTarget as selectConfiguration[{}] is null", invSelectIndex);
            else iValue = (selectConfiguration[invSelectIndex][40] & 0xFF);
            return iValue;
        }

        public int getSelectMaskBank() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT getSelectTarget as selectConfiguration[{}] is null", invSelectIndex);
            else iValue = (byte)(selectConfiguration[invSelectIndex][1] & 0xFF);
            return iValue;
        }
        public boolean setSelectMaskBank(int selectMaskBank) {
            boolean bValue = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT setSelectMaskBank[{}] as selectConfiguration[{}] is null", selectMaskBank, invSelectIndex);
            else {
                Logger.debug("Old selectConfiguration {} = {}", invSelectIndex,byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(selectMaskBank & 0xFF);
                if (false) {
                    Logger.trace("!!!! Skip 1A writeMAC 0x3141");
                    bValue = true;
                }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 1, bytes, true);
                if (bValue) selectConfiguration[invSelectIndex][1] = bytes[0];
                Logger.debug("bytes = {}, new selectConfiguration {} = {}" + byteArrayToString(bytes), invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            Logger.debug("1 setSelectMaskBank with selectMaskBank = {} and bValue = {}", selectMaskBank, bValue);
            return bValue;
        }

        public int getSelectMaskOffset() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT getSelectMaskBank as selectConfiguration[{}] is null", invSelectIndex);
            else {
                iValue = ((selectConfiguration[invSelectIndex][2] & 0xFF) << 24);
                iValue += ((selectConfiguration[invSelectIndex][3] & 0xFF) << 16);
                iValue += ((selectConfiguration[invSelectIndex][4] & 0xFF) << 8);
                iValue += (selectConfiguration[invSelectIndex][5] & 0xFF);
            }
            return iValue;
        }
        public boolean setSelectMaskOffset(int selectMaskOffset) {
            boolean bValue = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT setSelectMaskBank[{}] as selectConfiguration[{}] is null", selectMaskOffset, invSelectIndex);
            else {
                Logger.debug("Old selectConfiguration {} = {}", invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[4];
                bytes[0] = (byte)((selectMaskOffset >> 24) & 0xFF);
                bytes[1] = (byte)((selectMaskOffset >> 16) & 0xFF);
                bytes[2] = (byte)((selectMaskOffset >> 8) & 0xFF);
                bytes[3] = (byte)(selectMaskOffset & 0xFF);
                if (false) {
                    Logger.trace("!!!! Skip 1A writeMAC 0x3142");
                    bValue = true;
                }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 2, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, selectConfiguration[invSelectIndex], 2, bytes.length);
                Logger.debug("bytes = {}, new selectConfiguration {} = {}", byteArrayToString(bytes), invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            Logger.debug("1 setSelectMaskOffset with selectMaskOffset = {} and bValue = {}", selectMaskOffset, bValue);
            return bValue;
        }

        public int getSelectMaskLength() {
            int dataIndex = invSelectIndex, iValue = INVSELECT_INVALID;
            if (dataIndex >= INVSELECT_MIN && dataIndex <= INVSELECT_MAX) {
                Logger.debug("selectConfiguration {} = {}", dataIndex, byteArrayToString(selectConfiguration[dataIndex]));
                if (selectConfiguration[dataIndex] != null) {
                    iValue = selectConfiguration[dataIndex][6];
                }
            }
            Logger.debug("iValue = {}", iValue);
            return iValue;
        }
        public boolean setSelectMaskLength(int selectMaskLength) {
            boolean bValue = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT setSelectMaskLength[{}] as selectConfiguration[{}] is null", selectMaskLength, invSelectIndex);
            else {
                Logger.debug("Old selectConfiguration {} = {}", invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(selectMaskLength & 0xFF);
                if (false) {
                    Logger.trace("!!!! Skip 1A writeMAC 0x3146");
                    bValue = true;
                }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 6, bytes, true);
                if (bValue) selectConfiguration[invSelectIndex][6] = bytes[0];
                Logger.debug("bytes = {}, new selectConfiguration {} = {}", byteArrayToString(bytes), invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            Logger.debug("1 setSelectMaskOffset with setSelectMaskLength = {} and bValue = {}", selectMaskLength, bValue);
            return bValue;
        }

        public String getSelectMaskData() {
            String strValue = null;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.debug("!!! CANNOT getSelectMaskBank as selectConfiguration[{}] is null", invSelectIndex);
            else {
                if (selectConfiguration[invSelectIndex] != null && selectConfiguration[invSelectIndex].length > 39) {
                    Logger.debug("selectConfiguration {} = {}", invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
                    byte[] bytes = new byte[32];
                    System.arraycopy(selectConfiguration[invSelectIndex], 7, bytes, 0, bytes.length);
                    Logger.debug("bytes = {}", byteArrayToString(bytes));
                    for (int i = 0; i < bytes.length; i++) {
                        String string = String.format("%02X", bytes[i]);
                        if (strValue == null) strValue = string;
                        else strValue += string;
                        Logger.debug("i = {}, strValue = {}", i, strValue);
                    }
                    if (strValue == null) strValue = "";
                }
            }
            return strValue;
        }
        public boolean setSelectMaskData(String maskData) {
            boolean bValue = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) Logger.trace("!!! CANNOT setSelectMaskData[{}] as selectConfiguration[{}] is null", maskData, invSelectIndex);
            else {
                Logger.debug("Old selectConfiguration {} = {}", invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[maskData.length() / 2 + maskData.length() % 2];
                for (int i = 0; i < bytes.length; i++) {
                    boolean bSingle = false;
                    if (i * 2 + 2 > maskData.length()) bSingle = true;
                    Logger.debug("substring i = {} {}", i, maskData.substring(i * 2, (bSingle ? i * 2 +1 : i * 2 + 2)));
                    try {
                        String stringSub = null;
                        if (bSingle) {
                            stringSub = maskData.substring(i * 2, i * 2 + 1) + "0";
                        } else stringSub = maskData.substring(i * 2, i * 2 + 2);
                        int iValue = Integer.parseInt(stringSub, 16);
                        bytes[i] = (byte) (iValue & 0xFF);
                    } catch (Exception ex) {
                        Logger.trace("!!! Error in parsing maskdata {} when i = {}", maskData, i);
                    }
                }
                if (false) {
                    Logger.trace("!!!! Skip 1A writeMAC 0x3147");
                    bValue = true;
                }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 7, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, selectConfiguration[invSelectIndex], 7, bytes.length);
                Logger.trace("bytes = {}, new selectConfiguration {} = {}", byteArrayToString(bytes), invSelectIndex, byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            Logger.debug("1 setSelectMaskData with maskData = {} and bValue = {}", maskData, bValue);
            return bValue;

        }

        public int getQueryTarget() {
            int iValue = -1;
            Logger.debug("2 getQueryTarget: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null)
                Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!",antennaSelect);
            else {
                Logger.debug("2A getQueryTarget: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                if (antennaPortConfig[antennaSelect][13] != 0) iValue = 2;
                else if ((antennaPortConfig[antennaSelect][6] & 0x80) != 0) iValue = 1;
                else iValue = 0;
                Logger.debug(String.format("2b getQueryTarget: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        public boolean setQueryTarget(int queryTarget, int querySession, int querySelect) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else { // queryTarget = 0;
                Logger.debug("2 setQueryConfig: queryTarget = {}, querySession = {}, querySelect = {}", queryTarget, querySession, querySelect);
                Logger.debug("2A setQueryConfig: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[9];
                System.arraycopy(antennaPortConfig[antennaSelect], 5, bytes, 0, bytes.length);

                int iValue;
                if (querySession >= 0) {
                    iValue = querySession & 0x03;
                    bytes[1] &= ~0x18; bytes[1] |= (iValue << 3);
                }
                if (querySelect >= 0) {
                    iValue = querySelect & 0x03;
                    bytes[1] &= ~0x60; bytes[1] |= (iValue << 5);
                }
                Logger.debug("2b setQueryConfig: queryTarget = {}", queryTarget);
                if (queryTarget >= 0) {
                    iValue = queryTarget & 0x01;
                    bytes[1] &= ~0x80; bytes[1] |= (iValue << 7);
                    if (queryTarget >= 2) bytes[8] = 1;
                    else bytes[8] = 0;
                }
                Logger.debug("2C setQueryConfig: bytes = {}", byteArrayToString(bytes));
                boolean bSame = false;
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[9];
                    System.arraycopy(antennaPortConfig[antennaSelect], 5, bytesOld, 0, bytes.length);
                    Logger.debug("2d setQueryConfig: bytesOld = {}", byteArrayToString(bytesOld));
                    bSame = compareArray(bytes, bytesOld, bytes.length);
                    Logger.debug("2E setQueryConfig: the array is the same = {}", bSame);
                }
                //bSame = false; Logger.trace("!!! assme bSame is false before 1b writeMAC 0x3035");
                if (bSame) {
                    Logger.pkData(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 5));
                    bValue = true;
                } else {
                    Logger.trace("test 2");
                    bValue = rx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 5, bytes, true);
                    if (bValue)
                        System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 5, bytes.length);
                }
                Logger.debug("2F setQueryConfig: with updated {}", byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        public int getQuerySession() {
            int iValue = -1;
            Logger.debug("2 getQuerySession: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2A getQuerySession: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][6] & 0x18) >> 3;
                Logger.debug(String.format("2b getQuerySession: iValue = 0x%X", iValue));
            }
            return iValue;
        }

        public int getQuerySelect() {
            int iValue = -1;
            Logger.debug("2 getQuerySession: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2A getQuerySession: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][6] & 0x60) >> 5;
                Logger.debug(String.format("2b getQuerySession: iValue = 0x%X", iValue));
            }
            return iValue;

        }
        public boolean setQuerySelect(int querySelect) {
            boolean bValue = false;
            for (int antennaSelect = 0; antennaSelect < 16; antennaSelect++) {
                if (antennaPortConfig[antennaSelect] == null)
                    Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
                else { // queryTarget = 0;
                    Logger.debug("2 setQuerySelect: querySelect = {}", querySelect);
                    Logger.debug("2A setQuerySelect: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                    byte[] bytes = new byte[1];
                    bytes[0] = antennaPortConfig[antennaSelect][6];

                    int iValue;
                    if (querySelect >= 0) {
                        iValue = querySelect & 0x03;
                        bytes[0] &= ~0x60;
                        bytes[0] |= (iValue << 5);
                    }
                    Logger.debug("2C setQueryConfig: bytes = {}", byteArrayToString(bytes));
                    boolean bSame = false;
                    if (sameCheck) {
                        if (bytes[0] == antennaPortConfig[antennaSelect][6]) bSame = true;
                        Logger.debug("2E setQueryConfig: the array is the same = {}", bSame);
                    }
                    if (bSame) {
                        Logger.pkData(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes), 0x3030 + antennaSelect * 16 + 5));
                        bValue = true;
                    } else {
                        bValue = rx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 6, bytes, true);
                        if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
                    }
                    Logger.debug("2F setQueryConfig: with updated {}", byteArrayToString(antennaPortConfig[antennaSelect]));
                    if (bValue == false) break;
                }
            }
            return bValue;
        }

        final int INVALGO_INVALID = -1; final int INVALGO_MIN = 0; final int INVALGO_MAX = 3;
        int invAlgo = INVALGO_INVALID;
        public int getInvAlgo() {
            int iValue = -1;
            Logger.debug("3 getInvAlgo: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("3A getInvAlgo: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][6];
                Logger.debug(String.format("3b getInvAlgo: iValue = 0x%X", iValue));
                iValue &= 0x01; //iValue |= 0x01;
                Logger.debug(String.format("3c getInvAlgo: changed iValue = 0x%X", iValue));
                if (iValue == 0) iValue = 3;
                else iValue = 0;
                Logger.debug(String.format("3d getInvAlgo: after adjustement, iValue = 0x%X", iValue));
            }
            return iValue;
        }
        public boolean setInvAlgo(int invAlgo) {
            boolean bValue = false;
            Logger.debug("3 setInvAlgo[{}]", invAlgo);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(antennaPortConfig[antennaSelect][6] & ~0x01);
                if (invAlgo == 0) bytes[0] |= 0x01;
                Logger.debug("3 setInvAlgo: bytes = {}", byteArrayToString(bytes));
                bValue = writeMAC(0x3036 + this.antennaSelect * 16, bytes, true);
                if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
            }
            Logger.debug("3A setInvAlgo with bValue = {}", bValue);
            return bValue;
        }

        final int MATCHREP_INVALID = -1; final int MATCHREP_MIN = 0; final int MATCHREP_MAX = 255;
        int matchRep = MATCHREP_INVALID;
        public boolean setMatchRep(int matchRep) {
            if (matchRep == this.matchRep) return true;
            if (this.matchRep == matchRep && sameCheck) {
                Logger.trace("Skip sending repeated data with matchRep = {}", matchRep);
                return true;
            }
            this.matchRep = matchRep;
            Logger.trace(String.format("!!! Skip setMatchRep[0x%X]", matchRep));
            return true;
        }

        final int TAGSELECT_INVALID = -1; final int TAGSELECT_MIN = 0; final int TAGSELECT_MAX = 1;
        int tagSelect = TAGSELECT_INVALID;
        public boolean setTagSelect(int tagSelect) {
            if (tagSelect == this.tagSelect) return true;
            this.tagSelect = tagSelect;
            Logger.trace(String.format("!!! Skip setTagSelect[%d]", tagSelect));
            return true;
        }

        final int NOINVENTORY_INVALID = -1; final int NOINVENTORY_MIN = 0; final int NOINVENTORY_MAX = 1;
        int noInventory = NOINVENTORY_INVALID;

        final int TAGREAD_INVALID = -1; final int TAGREAD_MIN = 0; final int TAGREAD_MAX = 2;
        int tagRead = TAGREAD_INVALID;
        public int getTagRead() {
            int iValue = 0;
            if (rx000Setting.multibankReadConfig[0] == null)
                Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (rx000Setting.multibankReadConfig[1] == null)
                Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else if (rx000Setting.multibankReadConfig[0][0] != 0) {
                iValue++;
                if (rx000Setting.multibankReadConfig[1][0] != 0)
                    iValue++;
                Logger.trace("getTagRead = {}", iValue);
            } else Logger.trace("getTagRead = 0 as multibankReadConfig[0] = {}, [1] = {}", byteArrayToString(multibankReadConfig[0]), byteArrayToString(multibankReadConfig[1]));
            return iValue;
        }
        boolean isMultibankReplyNeed(int index) {
            boolean bValue = false;
            if (resReadNoReply) bValue = true;
            Logger.trace("multibankReadConfig[{}] = {}, bValue = {}", index,  byteArrayToString(rx000Setting.multibankReadConfig[index]), bValue);
            return bValue;
        }
        boolean resReadNoReply = false;
        public boolean setResReadNoReply(boolean resReadNoReply) {
            Logger.trace("setResReadNoReply[{}]", resReadNoReply);
            this.resReadNoReply = resReadNoReply;
            return true;
        }
        public boolean setTagRead(int tagRead) {
            boolean bValue = false;
            Logger.debug("0 setTagRead with tagRead = {}", tagRead);
            if (rx000Setting.multibankReadConfig[0] == null)
                Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (rx000Setting.multibankReadConfig[1] == null)
                Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                Logger.debug("0 multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if ((tagRead == 0 && rx000Setting.multibankReadConfig[0][0] != 0)
                        || (tagRead != 0 && rx000Setting.multibankReadConfig[0][0] == 0)) {
                    byte[] bytes = new byte[1];
                    if (tagRead == 2 && isMultibankReplyNeed(0)) bytes[0] = 2;
                    else if (tagRead != 0) bytes[0] = 1;
                    else bytes[0] = 0;
                    bValue = writeMAC(0x3270 + 7 * 0, bytes, true);
                    if (bValue)
                        rx000Setting.multibankReadConfig[0][0] = bytes[0];
                    Logger.debug("0A multibankReadConfig[0] = {}, with bValue = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]), bValue);
                } else bValue = true;
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if (bValue && ((tagRead < 2 && rx000Setting.multibankReadConfig[1][0] != 0)
                        || (tagRead >= 2 && rx000Setting.multibankReadConfig[1][0] == 0))) {
                    byte[] bytes = new byte[1];
                    if (tagRead >= 2) bytes[0] = 1;
                    else bytes[0] = 0;
                    bValue = writeMAC(0x3270 + 7 * 1, bytes, true);
                    if (bValue)
                        rx000Setting.multibankReadConfig[1][0] = bytes[0];
                    Logger.debug("0A multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int TAGDELAY_INVALID = -1; final int TAGDELAY_MIN = 0; final int TAGDELAY_MAX = 63;
        int tagDelay = TAGDELAY_INVALID;
        public boolean setTagDelay(int tagDelay) {
            if (tagDelay == this.tagDelay) return true;
            if (this.tagDelay == tagDelay && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with tagDelay = {}", tagDelay);
                return true;
            }
            this.tagDelay = tagDelay;
            Logger.trace("!!! Skip setTagDelay[{}]", tagDelay);
            return true;
        }

        byte[] dupElimRollWindow = null;
        public byte getDupElimRollWindow() {
            if (dupElimRollWindow != null && dupElimRollWindow.length == 1) return dupElimRollWindow[0];
            readMAC(0x3900, 1);
            return ((byte)-1);
        }
        public boolean setDupElimRollWindow(byte dupElimDelay) {
            if (dupElimRollWindow != null && dupElimRollWindow.length == 1 && dupElimRollWindow[0] == dupElimDelay && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with dupElimDelay = {}", dupElimDelay);
                return true;
            }
            byte[] bytes = new byte[1];
            bytes[0] = dupElimDelay;
            boolean bValue;
            bValue = rx000Setting.writeMAC(0x3900, bytes, true);
            if (bValue) dupElimRollWindow = bytes;
            return true;
        }
        Date keepAliveTime;
        Date inventoryRoundEndTime;
        int crcErrorRate;
        int tagRate = -1;
        public int getTagRate() {
            int iValue = tagRate;
            tagRate = -1;
            return iValue;
        }
        byte[] eventPacketUplnkEnable = null;
        public int getEventPacketUplinkEnable() {
            if (eventPacketUplnkEnable != null && eventPacketUplnkEnable.length == 2) {
                int iValue = ((eventPacketUplnkEnable[0] & 0xFF) << 8) + (eventPacketUplnkEnable[1] & 0xFF);
                Logger.trace("eventPacketUplnkEnable iValue = {}", iValue);
                return iValue;
            }
            readMAC(0x3906, 2);
            return -1;
        }

        public boolean setEventPacketUplinkEnable(byte byteEventPacketUplinkEnable) {
            if (eventPacketUplnkEnable != null && eventPacketUplnkEnable.length == 2 && eventPacketUplnkEnable[1] == byteEventPacketUplinkEnable && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with byteEventPacketUplinkEnable = {}", byteEventPacketUplinkEnable);
                return true;
            }
            byte[] bytes = new byte[2];
            bytes[1] = byteEventPacketUplinkEnable;
            boolean bValue;
            bValue = rx000Setting.writeMAC(0x3906, bytes, true);
            if (bValue) eventPacketUplnkEnable = bytes;
            return true;
        }

        byte[] intraPacketDelay = null;
        public byte getIntraPacketDelay() {
            if (intraPacketDelay != null && intraPacketDelay.length == 1) return intraPacketDelay[0];
            readMAC(0x3908, 1);
            return ((byte)-1);
        }
        public boolean setIntraPacketDelay(byte intraPkDelay) {
            if (intraPkDelay >= 0 && intraPacketDelay != null && sameCheck) {
                if (intraPacketDelay.length == 1 && intraPacketDelay[0] == intraPkDelay) {
                    Logger.trace("!!! Skip sending repeated data with intraPkDelay = {}", intraPkDelay);
                    return true;
                }
            }
            byte[] bytes = new byte[1];
            bytes[0] = intraPkDelay;
            boolean bValue;
            bValue = rx000Setting.writeMAC(0x3908, bytes, true);
            if (bValue) intraPacketDelay = bytes;
            return true;
        }
        long cycleDelay = -1;
        public long getCycleDelay() {
            return cycleDelay;
        }
        public boolean setCycleDelay(long cycleDelay) {
            if (cycleDelay == this.cycleDelay) return true;
            if (this.cycleDelay == cycleDelay && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with cycleDelay = {}", cycleDelay);
                return true;
            }
            this.cycleDelay = cycleDelay;
            Logger.trace("!!! Skip setCycleDelay[{}]", cycleDelay);
            return true;
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
        boolean setHST_AUTHENTICATE_CFG(boolean sendReply, boolean incReplyLenth, int csi, int length) {
            Logger.trace("sendReply = {}, incReplyLenth = {}, length = {}", sendReply, incReplyLenth, length);
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
                    Logger.trace("i = {}, authMatchDataReady = {}", i, authMatchDataReady);
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
            Logger.trace("range = {}, user = {}, tid = {}, epc = {}, epcLength = {}, xcpc = {}", range, user, tid, epc, epcLength, uxpc);
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
            Logger.trace("going to do sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG,");
            return sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG, true, msgBuffer);
        }

        final int TAGJOIN_INVALID = -1; final int TAGJOIN_MIN = 0; final int TAGJOIN_MAX = 1;
        int invModeCompact = TAGJOIN_INVALID;
        public boolean setInvModeCompact(boolean bInvModeCompact) {
            int invModeCompact = (bInvModeCompact ? 1 : 0);
            if (invModeCompact == this.invModeCompact && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with bInvModeCompact = {}", bInvModeCompact);
                return true;
            }
            this.invModeCompact = invModeCompact;
            Logger.trace("!!! Skip setInvModeCompact[{}]", bInvModeCompact ? "true" : "false");
            return true;
        }

        final int BRAND_INVALID = -1; final int BRANDID_MIN = 0; final int BRANDID_MAX = 1;
        int invBrandId = BRAND_INVALID;
        boolean getInvBrandId() {
            if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) { getHST_INV_CFG(); return false; }
            return (invModeCompact == 1 ? true : false);
        }
        public boolean setInvBrandId(boolean invBrandId) {
            if (invBrandId == getInvBrandId()) return true;
            this.invBrandId = (invBrandId ? 1 : 0); Logger.trace("!!! Skip setInvBrandId[{}]", invBrandId);
            return true;
        }

        private boolean getHST_INV_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_CFG, false, msgBuffer);
        }
        final int ALGOSELECT_INVALID = -1; final int ALGOSELECT_MIN = 0; final int ALGOSELECT_MAX = 3;   //DataSheet says Max=1
        int algoSelect = ALGOSELECT_INVALID;
        public boolean setAlgoSelect(int algoSelect) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else { //algoSelect = 0;
                Logger.debug("2 setAlgoSelect: algoSelect = {}", algoSelect);
                Logger.debug("2A setAlgoSelect: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][6];
                if (algoSelect >= 3) bytes[0] &= ~0x01;
                else bytes[0] |= 0x01;
                Logger.debug(String.format("2A1 setAlgoSelect: bytes = 0x%X with sameCheck = ", bytes[0]) + sameCheck);
                boolean bSame = false;
                if (sameCheck) {
                    Logger.debug(String.format("2A2 setAlgoSelect: bytesOld = 0x%X", antennaPortConfig[antennaSelect][6]));
                    if (antennaPortConfig[antennaSelect][6] == bytes[0]) bSame = true;
                    Logger.debug("2ab setAlgoSelect: the array is the same = {}", bSame);
                }
                if (bSame) {
                    Logger.pkData(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 6));
                    bValue = true;
                } else {
                    bValue = rx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 6, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
                }
                Logger.debug("2b setAlgoSelect: with updated array {}", byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
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
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else { //algoStartQ = 6;
                Logger.debug("2 setAlgoStartQ: algoStartQ = {}", algoStartQ);
                Logger.debug("2A setAlgoStartQ: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][8];

                bytes[0] &= ~0x0F; bytes[0] |= (algoStartQ & 0x0F);
                Logger.debug(String.format("2A1 setAlgoStartQ: bytes = 0x%X with sameCheck = ", bytes[0]) + sameCheck);
                boolean bSame = false;
                if (sameCheck) {
                    if (antennaPortConfig[antennaSelect][8] == bytes[0]) bSame = true;
                    Logger.debug("2ab setAlgoStartQ: the array is the same = {}", bSame);
                }
                if (bSame) {
                    Logger.pkData(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes), 0x3030 + antennaSelect * 16 + 8));
                    bValue = true;
                } else {
                    bValue = writeMAC(0x3030 + antennaSelect * 16 + 8, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][8] = bytes[0];
                }
                Logger.debug("2b setAlgoStartQ: with updated {}", byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
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

        public int getAlgoMinQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMinQ();
            }
        }

        final int ALGORETRY_INVALID = -1, ALGORETRY_MIN = 0, ALGORETRY_MAX = 255, ALGORETRY_DEFAULT = 1;
        int algoRetry = ALGORETRY_INVALID;
        public int getAlgoMinQCycles() {
            int iValue = -1;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("3A getAlgoMinQCycles: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = ((antennaPortConfig[antennaSelect][7] >> 4) & 0x0F);
                Logger.debug(String.format("3b getAlgoMinQCycles: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        public boolean setAlgoMinQCycles(int minQCycles) {
            boolean bValue = false;
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect );
            else if (getAlgoMinQCycles() == minQCycles && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with algoRetry = {}", algoRetry);
                return true;
            } else {
                Logger.debug("3A setAlgoMinQCycles: minQCycles = {}, getAntennaPortConfig[{}] = {}", minQCycles, antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] data = new byte[1];
                data[0] = (byte) (antennaPortConfig[antennaSelect][7] & 0x0F);
                data[0] |= (byte)(minQCycles << 4);
                bValue = writeMAC(0x3037 + this.antennaSelect * 16, data, true);
                if (bValue) antennaPortConfig[antennaSelect][7] = data[0];
                Logger.debug("3C setAlgoMinQCycles: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }

        int getAlgoAbFlip(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoAbFlip();
            }
        }
        public int getAlgoAbFlip() {
            int iValue = -1;
            Logger.debug("3 getAlgoAbFlip: iAntennaPort = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("3A getAlgoAbFlip: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][13];
                Logger.debug(String.format("3b getAlgoAbFlip: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        public boolean setAlgoAbFlip(int algoAbFlip) {
            boolean bValue = false;
            Logger.debug("3 setAlgoAbFlip: iAntennaPort = {}" + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("3A setAlgoAbFlip: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] data = new byte[1];
                data[0] = (byte)(algoAbFlip & 0xFF);
                bValue = writeMAC(0x303d + this.antennaSelect * 16, data, true);
                if (bValue) antennaPortConfig[antennaSelect][13] = data[0];
                Logger.debug("3b setAlgoAbFlip: bValue = {}", bValue);
                Logger.debug("3C setAlgoAbFlip: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        boolean setAlgoAbFlip(int algoAbFlip, int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            Logger.trace("algoSelect = {}, algoAbFlip = {}, algoRunTilZero = {}", algoSelect, algoAbFlip, algoRunTilZero);
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
        int algoRunTilZero = -1, ALGORUNTILZERO_MIN = 0, ALGORUNTILZERO_MAX = 1, ALGORUNTILZERO_DEFAULT = 0;
        public boolean setAlgoRunTilZero(int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            else if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX) algoRunTilZero = ALGORUNTILZERO_DEFAULT;
            if (this.algoRunTilZero == algoRunTilZero && sameCheck) {
                Logger.trace("!!! Skip sending repeated data with algoRunTilZero = {}", algoRunTilZero);
                return true;
            }
            this.algoRunTilZero = algoRunTilZero;
            Logger.trace("!!! Skip setAlgoRunTilZero[{}]", algoRunTilZero);
            return true;
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
            Logger.trace("rssiFilterType = {}, rssiFilterOption = {}", rssiFilterType, rssiFilterOption);
            byte[] bytes = new byte[] { 0 };
            if (rssiFilterType > 0) {
                if (rssiFilterOption > 0) bytes[0] = 2;
                else bytes[0] = 1;
            } else bytes[0] = 0;
            boolean bValue = writeMAC(0x390A, bytes, true);
            if (bValue) {
                this.rssiFilterType = rssiFilterType;
                this.rssiFilterOption = rssiFilterOption;
            }
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
            byte[] bytes = new byte[2];
            bytes[0] = (byte)(((short)rssiFilterThreshold1 >> 8) & 0xFF);
            bytes[1] = (byte)((short)rssiFilterThreshold1 & 0xFF);
            boolean bValue = writeMAC(0x390C, bytes, true);
            if (bValue) {
                this.rssiFilterThreshold1 = rssiFilterThreshold1;
                this.rssiFilterThreshold2 = rssiFilterThreshold2;
            }
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
            Logger.trace("entry: rssiFilterCount = {}, this.rssiFilterCount = {}", rssiFilterCount, this.rssiFilterCount);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 9, 9, 0, 0, 0, 0};
            if (rssiFilterCount < RSSIFILTERCOUNT_MIN || rssiFilterCount > RSSIFILTERCOUNT_MAX)
                rssiFilterCount = mDefault.rssiFilterCount;
            Logger.trace("rssiFilterCount 1 = {}, this.rssiFilterCount = {}", rssiFilterCount, this.rssiFilterCount);
            if (this.rssiFilterCount == rssiFilterCount && sameCheck) return true;
            Logger.trace("rssiFilterCount 2 = {}, this.rssiFilterCount = {}", rssiFilterCount, this.rssiFilterCount);
            msgBuffer[4] |= (byte) (rssiFilterCount & 0xFF);
            msgBuffer[5] |= (byte) ((rssiFilterCount >> 8) & 0xFF);
            msgBuffer[6] |= (byte) ((rssiFilterCount >> 16) & 0xFF);
            msgBuffer[7] |= (byte) ((rssiFilterCount >> 24) & 0xFF);
            this.rssiFilterCount = rssiFilterCount;
            Logger.trace("entering to sendHostRegRequest: rssiFilterCount = {}", rssiFilterCount);
            boolean bValue = sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_COUNT, true, msgBuffer);
            Logger.trace("after sendHostRegRequest: rssiFilterCount = {}", rssiFilterCount);
            return bValue;
        }

        final int MATCHENABLE_INVALID = -1; final int MATCHENABLE_MIN = 0; final int MATCHENABLE_MAX = 1;
        int matchEnable = MATCHENABLE_INVALID;
        public int getInvMatchEnable() {
            getHST_INV_EPC_MATCH_CFG();
            return matchEnable;
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

        private boolean getHST_INV_EPC_MATCH_CFG() { return true; }
        private boolean setHST_INV_EPC_MATCH_CFG(int matchEnable, int matchType, int matchLength, int matchOffset) {
            this.matchEnable = matchEnable;
            this.matchType = matchType;
            this.matchLength = matchLength;
            this.matchOffset = matchOffset;
            return true;
        }

        byte[] invMatchData0_63; int invMatchDataReady = 0;
        public String getInvMatchData() {
            String strValue = "";
            if (invMatchData0_63 != null) strValue = byteArrayToString(invMatchData0_63);
            return strValue;
        }
        public boolean setInvMatchData(String matchData) {
            invMatchData0_63 = utility.stringToByteArray(matchData);
            return true;
        }

        //Tag access block parameters
        boolean accessVerfiy;
        final int ACCRETRY_INVALID = -1; final int ACCRETRY_MIN = 0; final int ACCRETRY_MAX = 7;
        int accessRetry = ACCRETRY_INVALID;
        public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
            if (accessVerfiy == this.accessVerfiy && accessRetry == this.accessRetry) return true;
            this.accessVerfiy = accessVerfiy;
            this.accessRetry = accessRetry;
            Logger.trace("!!! Skip setAccessRetry[{}, {}]", accessVerfiy, accessRetry);
            return true;
        }

        boolean setAccessEnable(int accessEnable, int accessEnable2) {
            boolean bValue = false;
            Logger.debug("0 setAccessEnable with accessEnable = {}, accessEnable2 = {}", accessEnable, accessEnable2);
            if (rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (rx000Setting.multibankReadConfig[1] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                Logger.debug("0 multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                if (accessEnable == rx000Setting.multibankReadConfig[0][0] && sameCheck) bValue = true;
                else {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte) (accessEnable & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[0][0] = bytes[0];
                    Logger.debug("0A multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                }
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if (accessEnable2 == rx000Setting.multibankReadConfig[1][0] && sameCheck) { }
                else if (bValue) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte) (accessEnable2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[1][0] = bytes[0];
                    Logger.debug("0A multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }
        final int ACCBANK_INVALID = -1; final int ACCBANK_MIN = 0; final int ACCBANK_MAX = 3;
        int accessBank = ACCBANK_INVALID; int accessBank2 = ACCBANK_INVALID;
        int getAccessBank() {
            int iValue = -1;
            if (accessBank >= 0 && accessBank <= 3 && rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else iValue = rx000Setting.multibankReadConfig[0][1];
            return iValue;
        }
        public boolean setAccessBank(int accessBank) { return setAccessBank(accessBank, 0); }
        public boolean setAccessBank(int accessBank, int accessBank2) {
            boolean bValue = false;
            Logger.debug("0 setAccessBank with accessBank = {}, accessBank2 = {}", accessBank, accessBank2);
            if (accessBank >= 0 && accessBank <= 3 && rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (accessBank2 >= 0 && accessBank2 <= 3 && rx000Setting.multibankReadConfig[1] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                Logger.debug("0 multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                if (accessBank == rx000Setting.multibankReadConfig[0][1] && sameCheck) bValue = true;
                else if (accessBank >= 0 && accessBank <= 3) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessBank & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0 + 1, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[0][1] = bytes[0];
                    Logger.debug("0A multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                }
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if (accessBank2 == rx000Setting.multibankReadConfig[1][1] && sameCheck) { }
                else if (bValue && accessBank2 >= 0 && accessBank2 <= 3) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessBank2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1 + 1, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[1][1] = bytes[0];
                    Logger.debug("0A multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int ACCOFFSET_INVALID = -1;
        int getAccessOffset() {
            int iValue = -1;
            if (accessBank >= 0 && accessBank <= 3 && rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else {
                iValue = (rx000Setting.multibankReadConfig[0][2] & 0xFF) << 24;
                iValue |= (rx000Setting.multibankReadConfig[0][3] & 0xFF) << 16;
                iValue |= (rx000Setting.multibankReadConfig[0][4] & 0xFF) << 8;
                iValue |= (rx000Setting.multibankReadConfig[0][5] & 0xFF);
            }
            return iValue;
        }
        public boolean setAccessOffset(int accessOffset) {
            //Logger.trace("10 setAccessOffset with accessOffset = " + accessOffset);
            return setAccessOffset(accessOffset, 0); }
        public boolean setAccessOffset(int accessOffset, int accessOffset2) {
            boolean bValue = false;
            Logger.debug("0 setAccessOffset with accessOffset = {}, accessOffset2 = {}", accessOffset, accessOffset2);
            if (accessOffset >= 0 && rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (accessOffset2 >= 0 && rx000Setting.multibankReadConfig[1] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                Logger.debug("0 multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                if (accessOffset >= 0) {
                    byte[] bytes = new byte[4];
                    bytes[0] = (byte)((accessOffset >> 24) & 0xFF);
                    bytes[1] = (byte)((accessOffset >> 16) & 0xFF);
                    bytes[2] = (byte)((accessOffset >> 8) & 0xFF);
                    bytes[3] = (byte)(accessOffset & 0xFF);
                    byte[] bytesOld = new byte[4]; System.arraycopy(rx000Setting.multibankReadConfig[0], 2, bytesOld, 0, bytesOld.length);
                    if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) bValue = true;
                    else {
                        bValue = writeMAC(0x3270 + 7 * 0 + 2, bytes, true);
                        if (bValue) System.arraycopy(bytes, 0, rx000Setting.multibankReadConfig[0], 2, bytes.length);
                        Logger.debug("0A multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                    }
                }
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if (bValue && accessOffset2 >= 0) {
                    byte[] bytes = new byte[4];
                    bytes[0] = (byte)((accessOffset2 >> 24) & 0xFF);
                    bytes[1] = (byte)((accessOffset2 >> 16) & 0xFF);
                    bytes[2] = (byte)((accessOffset2 >> 8) & 0xFF);
                    bytes[3] = (byte)(accessOffset2 & 0xFF);
                    byte[] bytesOld = new byte[4]; System.arraycopy(rx000Setting.multibankReadConfig[1], 2, bytesOld, 0, bytesOld.length);
                    if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) { }
                    else {
                        bValue = writeMAC(0x3270 + 7 * 1 + 2, bytes, true);
                        if (bValue) System.arraycopy(bytes, 0, rx000Setting.multibankReadConfig[1], 2, bytes.length);
                        Logger.debug("0A multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                    }
                }
            }
            return bValue;
        }

        final int ACCCOUNT_INVALID = -1; final int ACCCOUNT_MIN = 0; final int ACCCOUNT_MAX = 255;
        int accessCount = ACCCOUNT_INVALID; int accessCount2 = ACCCOUNT_INVALID;
        public boolean setAccessCount(int accessCount) {
            setAccessEnable(((accessCount != 0) ? 1 : 0), 0);
            return setAccessCount(accessCount, 0); }
        public boolean setAccessCount(int accessCount, int accessCount2) {
            boolean bValue = false;
            Logger.debug("0 setAccessCount with accessCount = {}, accessCount2 = {}", accessCount, accessCount2);
            if (rx000Setting.multibankReadConfig[0] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (rx000Setting.multibankReadConfig[1] == null) Logger.trace("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                Logger.debug("0 multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                if (accessCount == rx000Setting.multibankReadConfig[0][6] && sameCheck) bValue = true;
                else {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessCount & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0 + 6, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[0][6] = bytes[0];
                    Logger.debug("0A multibankReadConfig[0] = {}", byteArrayToString(rx000Setting.multibankReadConfig[0]));
                }
                Logger.debug("0 multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                if (accessCount2 == rx000Setting.multibankReadConfig[1][6] && sameCheck) { }
                else if (bValue) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessCount2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1 + 6, bytes, true);
                    if (bValue) rx000Setting.multibankReadConfig[1][6] = bytes[0];
                    Logger.debug("0A multibankReadConfig[1] = {}", byteArrayToString(rx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int ACCLOCKACTION_INVALID = -1; final int ACCLOCKACTION_MIN = 0; final int ACCLOCKACTION_MAX = 0x3FF;
        int accessLockAction = ACCLOCKACTION_INVALID;
        final int ACCLOCKMASK_INVALID = -1; final int ACCLOCKMASK_MIN = 0; final int ACCLOCKMASK_MAX = 0x3FF;
        int accessLockMask = ACCLOCKMASK_INVALID;

        boolean getHST_TAGACC_LOCKCFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, (byte) 0x0A, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_TAGACC_LOCKCFG, false, msgBuffer);
        }

        byte[] lockMask, lockAction;
        public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
            Logger.trace("accessLockAction = {}, accessLockMask = {}", accessLockAction, accessLockMask);
            boolean bValue = false;
            byte[] bytes = new byte[2];
            bytes[0] = (byte) (accessLockMask / 256);
            bytes[1] = (byte) (accessLockMask % 256);
            bValue = writeMAC(0x38AE, bytes, true);
            if (bValue) {
                lockMask = bytes;
                byte[] bytes1 = new byte[2];
                bytes1[0] = (byte) (accessLockAction / 256);
                bytes1[1] = (byte) (accessLockAction % 256);
                bValue = writeMAC(0x38B0, bytes1, true);
                if (bValue) lockAction = bytes;
            }
            return bValue;
        }

        byte[] accessPassword = null;
        public boolean getRx000AccessPassword() {
            return readMAC(0x38A6, 4);
        }
        public boolean setRx000AccessPassword(String password) {
            boolean bValue = false;
            Logger.debug("0 setRx000AccessPassword with password = {}", password);
            if (accessPassword == null) Logger.trace("!!! CANNOT continue as accessPassword is null !!!");
            else {
                Logger.debug("0 accessPassword = {}", byteArrayToString(accessPassword));
                byte[] bytes = new byte[4];
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
                            bytes[j / 2] |= (byte) (k << 4);
                        } else {
                            bytes[j / 2] |= (byte) (k);
                        }
                    }
                }
                byte[] bytesOld = new byte[4];
                System.arraycopy(accessPassword, 0, bytesOld, 0, bytesOld.length);
                Logger.debug("0 bytes = {}", byteArrayToString(bytes));
                if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) bValue = true;
                else {
                    bValue = writeMAC(0x38A6, bytes, true);
                    if (bValue) accessPassword = bytes;
                    Logger.debug("0A accessPassword = {}", byteArrayToString(accessPassword));
                }
            }
            return bValue;
        }

        final int KILLPWD_INVALID = 0; final long KILLPWD_MIN = 0; final long KILLPWD_MAX = 0x0FFFFFFFF;
        byte[] killPassword = null;
        public boolean getRx000KillPassword() {
            return readMAC(0x38AA, 4);
        }
        public boolean setRx000KillPassword(String password) {
            boolean bValue = false;
            Logger.debug("0 setRx000KillPassword with password = {}", password);
            if (killPassword == null) Logger.trace("!!! CANNOT continue as killPassword is null !!!");
            else {
                Logger.debug("0 killPassword = {}", byteArrayToString(killPassword));
                byte[] bytes = utility.stringToByteArray(password);
                byte[] bytesOld = new byte[4];
                System.arraycopy(accessPassword, 0, bytesOld, 0, bytesOld.length);
                Logger.debug("0 bytes = {}", byteArrayToString(bytes));
                if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) bValue = true;
                else {
                    bValue = writeMAC(0x38AA, bytes, true);
                    if (bValue) killPassword = bytes;
                    Logger.debug("0A killPassword = {}", byteArrayToString(killPassword));
                }
            }
            return bValue;
        }

        final int ACCWRITEDATSEL_INVALID = -1; final int ACCWRITEDATSEL_MIN = 0; final int ACCWRITEDATSEL_MAX = 7;
        int accessWriteDataSelect = ACCWRITEDATSEL_INVALID;

        byte[] accWriteData0_63; int accWriteDataReady = 0;
        public boolean setAccessWriteData(String dataInput) {
            boolean bVAlue = false;
            Logger.debug("Start with dataInput = {}", dataInput);
            dataInput = dataInput.trim();
            int writeBufLength = 16 * 2; //16
            int wrieByteSize = 4;   //8
            int length = dataInput.length();
            Logger.debug("Check dataInput length = {} with maximum length = {}", length, wrieByteSize * writeBufLength);
            if (length > wrieByteSize * writeBufLength) return false;
            byte[] msgBuffer = new byte[length/2 + (length%2 != 0 ? 1 : 0)];
            for (int i = 0; i < writeBufLength; i++) {
                Logger.debug("Before processing 4 nibbles, check length = {}", length);
                if (length > 0) {
                    length -= wrieByteSize;
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < wrieByteSize; j++) {
                        Logger.debug("Check dataInput = {}, i = {}, wrieByteSize = {}, j = {}", dataInput, i, wrieByteSize, j);
                        if (i * wrieByteSize + j >= dataInput.length()) break;
                        String subString = dataInput.substring(i * wrieByteSize + j, i * wrieByteSize + j + 1).toUpperCase();
                        Logger.debug("subString = {}", subString);
                        int k = 0;
                        for (k = 0; k < 16; k++) {
                            Logger.debug("k = {}, with hexString = {}", k , hexString);
                            if (subString.matches(hexString.substring(k, k + 1))) {
                                break;
                            }
                        }
                        if (k == 16) {
                            Logger.trace("!!! Cannot decode the data, with with i = {}, j = {}, subString = {}", i, j, subString);
                            return false;
                        }
                        if ((j / 2) * 2 == j) {
                            msgBuffer[i * 2 + j / 2] |= (byte) (k << 4);
                        } else {
                            msgBuffer[i * 2 + j / 2] |= (byte) (k);
                        }
                        Logger.debug("j = {} with updated data : {}", j, byteArrayToString(msgBuffer));
                    }
                    Logger.debug("complete 4 bytes: {}", byteArrayToString(msgBuffer));
                } else break;
            }

            bVAlue = setMultibankWriteConfig(0,true, getAccessBank(), getAccessOffset(), (msgBuffer.length/2 + (msgBuffer.length%2 != 0 ? 1 : 0)), msgBuffer);
            Logger.debug("after setMultibankWriteConfig, bvalue = {}", bVAlue);
            if (bVAlue) {
                //mRfidReaderChip.mRx000Setting.accWriteDataReady |= (0x01 << i);
                Logger.debug("accWriteReady={}", accWriteDataReady);
                for (int k = 0; k < 4; k++) {
                    //accWriteData0_63[i * 4 + k] = msgBuffer[7 - k];
                }
                Logger.debug("Data={}", byteArrayToString(accWriteData0_63));
            }
            return bVAlue;
        }

        //RFTC block paramters
        final int PROFILE_INVALID = -1; final int PROFILE_MIN = 0; final int PROFILE_MAX = 5;   //profile 4 and 5 are custom profiles.
        int currentProfile = PROFILE_INVALID;
        int iRfidModeSingleByte = -1;
        public int getCurrentProfile() {
            int iValue = -1;
            Logger.debug("2 getCurrentProfile: antennaSelect = {}", antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else {
                Logger.debug("2A getCurrentProfile: getAntennaPortConfig[{}] = {}", antennaSelect, byteArrayToString(antennaPortConfig[antennaSelect]));
                if (antennaPortConfig[antennaSelect][14] != 0 && antennaPortConfig[antennaSelect][15] == 0) {
                    iRfidModeSingleByte = 1;
                    iValue = antennaPortConfig[antennaSelect][14];
                } else {
                    iRfidModeSingleByte = 0;
                    iValue = (antennaPortConfig[antennaSelect][14] & 0xFF) << 8;
                    iValue += (antennaPortConfig[antennaSelect][15] & 0xFF);
                }
                Logger.debug(String.format("2b getCurrentProfile: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        public boolean setCurrentProfile(int currentProfile) {
            byte[] data; boolean bValue = false;
            Logger.debug("2 setCurrentProfile: currentProfile = {}, iRfidModeSingleByte = {}", currentProfile, iRfidModeSingleByte);
            if (antennaPortConfig[antennaSelect] == null) Logger.trace("CANNOT continue as antennaPortConfig[{}] is null !!!", antennaSelect);
            else if (getCurrentProfile() == currentProfile && sameCheck) bValue = true;
            else {
                if (iRfidModeSingleByte < 0) getCurrentProfile();
                else {
                    if (iRfidModeSingleByte != 0) {
                        data = new byte[1];
                        data[0] = (byte) currentProfile;
                    } else {
                        data = new byte[2];
                        data[0] = (byte) (currentProfile / 256);
                        data[1] = (byte) (currentProfile & 0xFF);
                    }
                    Logger.debug("2A setCurrentProfile: data = {}", byteArrayToString(data));
                    bValue = writeMAC(0x3030 + this.antennaSelect * 16 + 14, data, true);
                    Logger.debug("2b setCurrentProfile: after writeMAC, bValue = {}", bValue);
                    if (bValue && antennaPortConfig[antennaSelect] != null)
                        System.arraycopy(data, 0, antennaPortConfig[antennaSelect], 14, data.length);
                }
            }
            return bValue;
        }

        final int COUNTRYCODE_INVALID = -1;
        final int FREQCHANSEL_INVALID = -1;

        final int FREQCHANCONFIG_INVALID = -1; final int FREQCHANCONFIG_MIN = 0; final int FREQCHANCONFIG_MAX = 1;
        int freqChannelConfig = FREQCHANCONFIG_INVALID;

        final int FREQPLLMULTIPLIER_INVALID = -1;

        final int FREQPLLDAC_INVALID = -1;
    }
    class AntennaSelectedData {
        AntennaSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0)    default_setting_type = 0;
            if (default_setting_type > 5)    default_setting_type = 5;
            mDefault = new AntennaSelectedData.AntennaSelectedData_default(default_setting_type);
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
                antennaPower = mDefault.antennaPower; Logger.trace("antennaPower is set to default {}", antennaPower);
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
        AntennaSelectedData.AntennaSelectedData_default mDefault;

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
        AntennaSelectedData.AntennaSelectedData_defaultArray mDefaultArray = new AntennaSelectedData.AntennaSelectedData_defaultArray();

        final int ANTENABLE_INVALID = -1; final int ANTENABLE_MIN = 0; final int ANTENABLE_MAX = 1;
        int antennaEnable = ANTENABLE_INVALID;

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

        final int ANTDEFINE_INVALID = -1; final int ANTDEFINE_MIN = 0; final int ANTDEFINE_MAX = 3;
        int antennaDefine = ANTDEFINE_INVALID;

        final long ANTDWELL_INVALID = -1;
        long antennaDwell = ANTDWELL_INVALID;

        final long ANTPOWER_INVALID = -1; final long ANTPOWER_MIN = 0; final long ANTPOWER_MAX = 330; //Maximum 330\
        long antennaPower = ANTPOWER_INVALID;   //default value = 300

        final long ANTINVCOUNT_INVALID = -1; final long ANTINVCOUNT_MIN = 0; final long ANTINVCOUNT_MAX = 0xFFFFFFFFL;
        long antennaInvCount = ANTINVCOUNT_INVALID;
        long getAntennaInvCount() {
            if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 7, 7, 0, 0, 0, 0};
                sendHostRegRequest(HostRegRequests.HST_ANT_DESC_INV_CNT, false, msgBuffer);
            }
            return antennaInvCount;
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
        }
        InvSelectData.InvSelectData_default mDefault = new InvSelectData.InvSelectData_default();

        final int INVSELENABLE_INVALID = 0; final int INVSELENABLE_MIN = 0; final int INVSELENABLE_MAX = 1;
        int selectEnable = INVSELENABLE_INVALID;

        final int INVSELTARGET_INVALID = -1; final int INVSELTARGET_MIN = 0; final int INVSELTARGET_MAX = 7;
        int selectTarget = INVSELTARGET_INVALID;

        final int INVSELACTION_INVALID = -1; final int INVSELACTION_MIN = 0; final int INVSELACTION_MAX = 7;
        int selectAction = INVSELACTION_INVALID;

        final int INVSELDELAY_INVALID = -1; final int INVSELDELAY_MIN = 0; final int INVSELDELAY_MAX = 255;
        int selectDelay = INVSELDELAY_INVALID;

        final int INVSELMBANK_INVALID = -1; final int INVSELMBANK_MIN = 0; final int INVSELMBANK_MAX = 3;
        int selectMaskBank = INVSELMBANK_INVALID;

        final int INVSELMOFFSET_INVALID = -1; final int INVSELMOFFSET_MIN = 0; final int INVSELMOFFSET_MAX = 0xFFFF;
        int selectMaskOffset = INVSELMOFFSET_INVALID;

        final int INVSELMLENGTH_INVALID = -1; final int INVSELMLENGTH_MIN = 0; final int INVSELMLENGTH_MAX = 255;
        int selectMaskLength = INVSELMLENGTH_INVALID;
    }
    class AlgoSelectedData {
        AlgoSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0) default_setting_type = 0;
            if (default_setting_type > 4) default_setting_type = 4;
            mDefault = new AlgoSelectedData.AlgoSelectedData_default(default_setting_type);
            if (set_default_setting) {
                algoStartQ = mDefault.algoStartQ;
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
        AlgoSelectedData.AlgoSelectedData_default mDefault;

        class AlgoSelectedData_defaultArray { //0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
            int[] algoStartQ =     { -1, 0, 0, 0, 4 };
            int[] algoMaxQ =      { -1, 0, 0, 0, 15 };
            int[] algoMinQ =      { -1, 0, 0, 0, 0 };
            int[] algoMaxRep =    { -1, 0, 0, 0, 4 };
            int[] algoHighThres =  { -1, 0, 5, 5, 5 };
            int[] algoLowThres =  { -1, 0, 3, 3, 3 };
            int[] algoRetry =      { -1, 0, 0, 0, 0 };
            int[] algoAbFlip =     { -1, 0, 1, 1, 1 };
            int[] algoRunTilZero = { -1, 0, 0, 0, 0 };
        }
        AlgoSelectedData.AlgoSelectedData_defaultArray mDefaultArray = new AlgoSelectedData.AlgoSelectedData_defaultArray();

        final int ALGOSTARTQ_INVALID = -1; final int ALGOSTARTQ_MIN = 0; final int ALGOSTARTQ_MAX = 15;
        int algoStartQ = ALGOSTARTQ_INVALID;
        int getAlgoStartQ(boolean getInvalid) {
            if (getInvalid && (algoStartQ < ALGOSTARTQ_MIN || algoStartQ > ALGOSTARTQ_MAX)) getHST_INV_ALG_PARM_0();
            return algoStartQ;
        }
        boolean setAlgoStartQ(int algoStartQ) {
            Logger.trace("1A setAlgoStartQ with algoStartQ = {}", algoStartQ);
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMAXQ_INVALID = -1; final int ALGOMAXQ_MIN = 0; final int ALGOMAXQ_MAX = 15;
        int algoMaxQ = ALGOMAXQ_INVALID;
        int getAlgoMaxQ() {
            if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMaxQ;
        }
        boolean setAlgoMaxQ(int algoMaxQ) {
            Logger.trace("1b setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMINQ_INVALID = -1; final int ALGOMINQ_MIN = 0; final int ALGOMINQ_MAX = 15;
        int algoMinQ = ALGOMINQ_INVALID;
        int getAlgoMinQ() {
            if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMinQ;
        }

        final int ALGOMAXREP_INVALID = -1; final int ALGOMAXREP_MIN = 0; final int ALGOMAXREP_MAX = 255;
        int algoMaxRep = ALGOMAXREP_INVALID;

        final int ALGOHIGHTHRES_INVALID = -1; final int ALGOHIGHTHRES_MIN = 0; final int ALGOHIGHTHRES_MAX = 15;
        int algoHighThres = ALGOHIGHTHRES_INVALID;

        final int ALGOLOWTHRES_INVALID = -1; final int ALGOLOWTHRES_MIN = 0; final int ALGOLOWTHRES_MAX = 15;
        int algoLowThres = ALGOLOWTHRES_INVALID;

        private boolean getHST_INV_ALG_PARM_0() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 9, 0, 0, 0, 0};
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, false, msgBuffer);
        }
        boolean setAlgoStartQ(int startQ, int algoMaxQ, int algoMinQ, int algoMaxRep, int algoHighThres, int algoLowThres) {
            Logger.trace("0 setAlgoStartQ with startQ = {}", startQ);
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
            if (false && this.algoStartQ == startQ && this.algoMaxQ == algoMaxQ && this.algoMinQ == algoMinQ
                    && this.algoMaxRep == algoMaxRep && this.algoHighThres == algoHighThres && this.algoLowThres == algoLowThres
                    && sameCheck)
                return true;
            msgBuffer[4] |= (byte) (startQ & 0x0F);
            msgBuffer[4] |= (byte) ((algoMaxQ & 0x0F) << 4);
            msgBuffer[5] |= (byte) (algoMinQ & 0x0F);
            msgBuffer[5] |= (byte) ((algoMaxRep & 0xF) << 4);
            msgBuffer[6] |= (byte) ((algoMaxRep & 0xF0) >> 4);
            msgBuffer[6] |= (byte) ((algoHighThres & 0x0F) << 4);
            msgBuffer[7] |= (byte) (algoLowThres & 0x0F);
            this.algoStartQ = startQ;
            this.algoMaxQ = algoMaxQ;
            this.algoMinQ = algoMinQ;
            this.algoMaxRep = algoMaxRep;
            this.algoHighThres = algoHighThres;
            this.algoLowThres = algoLowThres;
            return sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, true, msgBuffer);
        }

        final int ALGORETRY_INVALID = -1; final int ALGORETRY_MIN = 0; final int ALGORETRY_MAX = 255;
        int algoRetry = ALGORETRY_INVALID;

        final int ALGOABFLIP_INVALID = -1; final int ALGOABFLIP_MIN = 0; final int ALGOABFLIP_MAX = 1;
        int algoAbFlip = ALGOABFLIP_INVALID;
        int getAlgoAbFlip() {
            if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX) getHST_INV_ALG_PARM_2();
            return algoAbFlip;
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
            Logger.trace("this.algoAbFlip  = {}, algoAbFlip = {}, this.algoRunTilZero = {}, algoRunTilZero = {}", this.algoAbFlip, algoAbFlip, this.algoRunTilZero, algoRunTilZero);
            if (false && this.algoAbFlip == algoAbFlip && this.algoRunTilZero == algoRunTilZero && sameCheck) return true;
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
        public void resetRSSI() {
            narrowRSSI = -1; wideRSSI = -1;
        }
    }

    //public boolean bFirmware_reset_before = false;
    final int RFID_READING_BUFFERSIZE = 600; //1024;

    byte[] mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
    int mRfidToReadingOffset = 0;
    ArrayList<RfidConnector.CsReaderRfidData> mRx000ToWrite = new ArrayList<>();

    public Rx000Setting rx000Setting = new Rx000Setting(true);
    public Rx000EngSetting rx000EngSetting = new Rx000EngSetting();

    public ArrayList<RfidReaderChipData.Rx000pkgData> mRx000ToRead = new ArrayList<>();
    boolean commandOperating;

    public double decodeNarrowBandRSSI(byte byteRSSI) {
        byte mantissa = byteRSSI;
        mantissa &= 0x07;
        byte exponent = byteRSSI;
        exponent >>= 3;
        double dValue = 20 * log10(pow(2, exponent) * (1 + (mantissa / pow(2, 3))));
        Logger.trace("byteRSSI = {}, mantissa = {}, exponent = {}, dValue = {}", String.format("%X", byteRSSI), mantissa, exponent, dValue);
        return dValue;
    }

    public boolean bRx000ToReading = false;
    int getBytes2EpcLength(byte[] bytes) {
        int iValue = ((bytes[0] & 0xFF) >> 3) * 2;
        Logger.trace("bytes = {}, iValue = {}", byteArrayToString(bytes), iValue);
        return iValue;
    }
    void uplinkHandler() {
        if (bRx000ToReading) return;
        bRx000ToReading = true;
        int startIndex = 0, startIndexOld = 0, startIndexNew = 0;
        boolean packageFound = false;
        int packageType = 0;
        long lTime = System.currentTimeMillis();
        if (csReaderConnector.rfidConnector.mRfidToRead.size() != 0) {
            Logger.debug("mRx000UplinkHandler(): START with mRfidToRead size = {}, mRx000ToRead size = {}", csReaderConnector.rfidConnector.mRfidToRead.size(), mRx000ToRead.size());
        } else Logger.debug("START AAA with mRx000ToRead size = {}", mRx000ToRead.size());
        if (false && mRx000ToRead.size() != 0) Logger.trace("START AAA with mRx000ToRead size = {}", mRx000ToRead.size());
        boolean bFirst = true;
        while (csReaderConnector.rfidConnector.mRfidToRead.size() != 0) {
            Logger.debug("Looping with mRfidToRead.size = {} with bleConnected = {}", csReaderConnector.rfidConnector.mRfidToRead.size(), csReaderConnector.isBleConnected());
            if (csReaderConnector.isBleConnected() == false) {
                csReaderConnector.rfidConnector.mRfidToRead.clear();
                Logger.trace("BLE DISCONNECTED !!! mRfidToRead.size() = {}", csReaderConnector.rfidConnector.mRfidToRead.size());
            } else if (System.currentTimeMillis() - lTime > (intervalRx000UplinkHandler/2)) {
                writeDebug2File("D" + String.valueOf(intervalRx000UplinkHandler) + ", " + System.currentTimeMillis() + ", Timeout");
                Logger.toLogView("TIMEOUT !!! mRfidToRead.size() = {}", csReaderConnector.rfidConnector.mRfidToRead.size()).trace();
                break;
            } else {
                Logger.debug("Check bFirst = {}", bFirst);
                if (bFirst) { bFirst = false; writeDebug2File("D" + String.valueOf(intervalRx000UplinkHandler) + ", " + System.currentTimeMillis()); }
                byte[] dataIn = csReaderConnector.rfidConnector.mRfidToRead.get(0).dataValues;
                long tagMilliSeconds = csReaderConnector.rfidConnector.mRfidToRead.get(0).milliseconds;
                boolean invalidSequence = csReaderConnector.rfidConnector.mRfidToRead.get(0).invalidSequence;
                Logger.apData("ApData: found mRfidToRead data with invalidSequence= {}, bytes= {}", invalidSequence, byteArrayToString(dataIn));
                csReaderConnector.rfidConnector.mRfidToRead.remove(0);

                Logger.debug("Check buffer size: data.length = {}, mRfidToReading.length = {}, mRfidToReadingOffset = {}", dataIn.length, mRfidToReading.length, mRfidToReadingOffset);
                if (dataIn.length >= mRfidToReading.length - mRfidToReadingOffset) {
                    if (mRfidToReadingOffset != 0) {
                        byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                        System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                        Logger.toLogView("!!! ERROR insufficient buffer, mRfidToReadingOffset={}, dataIn.length={}, clear mRfidToReading: {}", mRfidToReadingOffset, dataIn.length, byteArrayToString(unhandledBytes)).trace();
                        byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                        mRfidToReading = mRfidToReadingNew;
                        mRfidToReadingOffset = 0;
                        csReaderConnector.invalidUpdata++;
                    }
                    if (dataIn.length >= mRfidToReading.length - mRfidToReadingOffset) {
                        Logger.toLogView("!!! ERROR insufficient buffer, mRfidToReading.length={}, dataIn.length={}, clear mRfidToReading: {}", mRfidToReading.length, dataIn.length, byteArrayToString(dataIn)).trace();
                        csReaderConnector.invalidata++;
                        break;
                    }
                }

                Logger.debug("Check invalidSequence = {} with mRfidToReadingOffset = {}", invalidSequence, mRfidToReadingOffset);
                if (mRfidToReadingOffset != 0 && invalidSequence) {
                    byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                    System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                    Logger.trace("!!! ERROR invalidSequence with nonzero mRfidToReadingOffset={}, throw invalid unused data={}, {}", mRfidToReadingOffset, unhandledBytes.length, byteArrayToString(unhandledBytes));
                    mRfidToReadingOffset = 0;
                    startIndex = 0;
                    startIndexNew = 0;
                }

                System.arraycopy(dataIn, 0, mRfidToReading, mRfidToReadingOffset, dataIn.length);
                mRfidToReadingOffset += dataIn.length;

                int iPayloadSizeMin = 7; //boolean bprinted = false;
                while (mRfidToReadingOffset - startIndex >= iPayloadSizeMin) {
                    //if (bprinted == false) { bprinted = true; Logger.trace(byteArrayToString(mRfidToReading)); }
                    int packageLengthRead = (mRfidToReading[startIndex + 5] & 0xFF) * 256 + (mRfidToReading[startIndex + 6] & 0xFF);
                    int expectedLength = 7 + (mRfidToReading[startIndex + 5] & 0xFF) * 256 + (mRfidToReading[startIndex + 6] & 0xFF);
                    Logger.debug("Looping with startIndex = {}, mRfidToReadingOffset = {}, iPayloadSizeMin = {}, expectedLength = {}", startIndex, mRfidToReadingOffset, iPayloadSizeMin, expectedLength);
                    if (true) {
                        if (mRfidToReading[startIndex + 0] == 0x49
                                && mRfidToReading[startIndex + 1] == (byte) 0xdc
                                && (mRfidToReadingOffset - startIndex >= expectedLength) && (expectedLength > 7)
                        ) {
                            byte[] header = new byte[7], payload = new byte[expectedLength - 7];
                            System.arraycopy(mRfidToReading, startIndex, header, 0, header.length);
                            System.arraycopy(mRfidToReading, startIndex + 7, payload, 0, payload.length);
                            int iUplinkPackageType = (mRfidToReading[startIndex + 2] & 0xFF) * 256 + (mRfidToReading[startIndex + 3] & 0xFF);
                            Logger.apData(String.format("ApData: found Rfid.Uplink.DataRead.UplinkPackage_%04X with payload = {}", iUplinkPackageType), byteArrayToString(payload));
                            RfidReaderChipData.Rx000pkgData dataA = new RfidReaderChipData.Rx000pkgData();
                            dataA.dataValues = new byte[expectedLength - 7];
                            System.arraycopy(mRfidToReading, startIndex + 7, dataA.dataValues, 0, dataA.dataValues.length);
                            if (iUplinkPackageType == 0x3001 || iUplinkPackageType == 0x3003) {
                                dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                                //mRfidDevice.setInventoring(true);
                                Logger.debug("Check UplinkPackage_Event_csl_tag_read_epc_only_new data length = {}", dataA.dataValues.length);
                                if ((iUplinkPackageType == 0x3001 && dataA.dataValues.length < 17)
                                        || (iUplinkPackageType == 0x3003 && dataA.dataValues.length < 18)) {
                                    Logger.trace("!!! UplinkPackage_Event_csl_tag_read_epc_only_new data length has length equal or less than 15");
                                    dataA.decodedError = "Received UplinkPackage_Event_csl_tag_read_epc_only_new with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                } else {
                                    dataA.decodedTime = System.currentTimeMillis();
                                    dataA.decodedRssi = get2BytesOfRssi(dataA.dataValues, 4);
                                    Logger.debug("decoded decodedRssi = {}", dataA.decodedRssi);
                                    dataA.decodedPhase = (dataA.dataValues[6] & 0xFF) * 256 + (dataA.dataValues[7] & 0xFF);
                                    Logger.debug("decoded decodedPhase = {}", dataA.decodedPhase);
                                    dataA.decodedPort = (dataA.dataValues[10] & 0xFF);
                                    Logger.debug("decoded decodedPort = {}", dataA.decodedPort);
                                    dataA.decodedChidx = 1; //(dataA.dataValues[13] & 0xFF) * 256 + (dataA.dataValues[14] & 0xFF);
                                    Logger.debug("decoded decodedChidx = {}", dataA.decodedChidx);
                                    dataA.decodedPc = new byte[2]; System.arraycopy(dataA.dataValues, 15, dataA.decodedPc, 0, dataA.decodedPc.length);
                                    Logger.debug("decoded decodedPc = {}", byteArrayToString(dataA.decodedPc));
                                    if (iUplinkPackageType == 0x3001) {
                                        dataA.decodedEpc = new byte[dataA.dataValues.length - 17];
                                        System.arraycopy(dataA.dataValues, 17, dataA.decodedEpc, 0, dataA.decodedEpc.length);
                                    } else {
                                        int iEpcLength = getBytes2EpcLength(dataA.decodedPc);
                                        Logger.debug("dataA.dataValues.length = {}, iEpcLength = {} for data {}", dataA.dataValues.length, iEpcLength, byteArrayToString(dataA.dataValues));
                                        if (dataA.dataValues.length - 18 >= iEpcLength) {
                                            dataA.decodedEpc = new byte[dataA.dataValues.length - 18];
                                            System.arraycopy(dataA.dataValues, 17, dataA.decodedEpc, 0, iEpcLength);
                                            System.arraycopy(dataA.dataValues, iEpcLength + 18, dataA.decodedEpc, iEpcLength, dataA.dataValues.length - iEpcLength - 18);
                                            Logger.debug("decodedEpc = {}", byteArrayToString(dataA.decodedEpc));

                                            int iMbDataLength = dataA.dataValues.length - 18 - iEpcLength;
                                            int iDataIndex = 0, iDataOffset = 0;
                                            for (int i = 0; i < 3; i++) {
                                                int iValue = rx000Setting.getMultibankReadLength(i);
                                                Logger.debug("i = {} getMultibankReadLength = {}", i, iValue);
                                                if (iValue != 0) {
                                                    int iBankLength = iValue * 2;
                                                    Logger.debug("Check iDataIndex = {}, iDataOffset = {}, iBankLength = {}, iMbDataLength = {}", iDataIndex, iDataOffset, iBankLength, iMbDataLength);
                                                    if (iDataOffset + iBankLength > iMbDataLength) Logger.trace("!!! iBankLength {} is too long for iDataOffset {}, iMbDataLength = {}", iBankLength, iDataOffset, iMbDataLength);
                                                    else {
                                                        if (rx000Setting.getMultibankEnable(i) == 2) iDataIndex++;
                                                        else if (iDataIndex == 0) {
                                                            dataA.decodedData1 = new byte[iBankLength];
                                                            System.arraycopy(dataA.dataValues, iEpcLength + 18 + iDataOffset, dataA.decodedData1, 0, dataA.decodedData1.length);
                                                            Logger.debug("decodedData1 = {}", byteArrayToString(dataA.decodedData1));
                                                            iDataIndex++; iDataOffset += iBankLength;
                                                        } else if (iDataIndex == 1) {
                                                            dataA.decodedData2 = new byte[iBankLength];
                                                            System.arraycopy(dataA.dataValues, iEpcLength + 18 + iDataOffset, dataA.decodedData2, 0, dataA.decodedData2.length);
                                                            Logger.debug("decodedData2 = {}", byteArrayToString(dataA.decodedData2));
                                                            iDataIndex++; iDataOffset += iBankLength;
                                                        } else Logger.trace("!!! CANNOT handle the third multibank data");
                                                    }
                                                }
                                            }
                                            int extraLength = 0;
                                            if (dataA.decodedData1 != null) extraLength += dataA.decodedData1.length;
                                            if (dataA.decodedData2 != null) extraLength += dataA.decodedData2.length;
                                            if (extraLength != 0) {
                                                byte[] decodedEpcNew = new byte[dataA.decodedEpc.length - extraLength];
                                                System.arraycopy(dataA.decodedEpc, 0, decodedEpcNew, 0, decodedEpcNew.length);
                                                dataA.decodedEpc = decodedEpcNew;
                                            }
                                            Logger.trace("dataA.decodedPc,Epc = {}, {}, decodedData1,2 = {}, {}", byteArrayToString(dataA.decodedPc), byteArrayToString(dataA.decodedEpc),
                                                    dataA.decodedData1 == null ? "null" : byteArrayToString(dataA.decodedData1), dataA.decodedData2 == null ? "null" : byteArrayToString(dataA.decodedData2));
                                            if (iDataOffset != iMbDataLength) Logger.trace("!!! Some unhandled data as iDataOffset = {} for iMbDataLength = {}", iDataOffset, iMbDataLength);
                                            else Logger.debug("iDataOffset = iMbDataLength = {}", iMbDataLength);
                                        } else Logger.trace("!!! iEpcLength {} is too long for the data {}", iEpcLength, byteArrayToString(dataA.dataValues));
                                    }
                                    mRx000ToRead.add(dataA);
                                    Logger.debug("3001/3003 dataA.responseType = {}", dataA.responseType.toString());
                                    Logger.debug("decoded decodedEpc = {} with mRx000ToRead.size = {}", byteArrayToString(dataA.decodedEpc), mRx000ToRead.size());
                                    Logger.apData("ApData: uplink data UplinkPackage_Event_csl_tag_read_epc_only_new tag with Epc = {} is uploaded to mRx000ToRead with mRx000ToRead.size = {}", byteArrayToString(dataA.decodedEpc), mRx000ToRead.size());
                                    Logger.apData("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_epc_only_new has been processed");
                                }
                            } else if (iUplinkPackageType == 0x3006) {
                                dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                //mRfidDevice.setInventoring(true);
                                Logger.debug("Check UplinkPackage_Event_csl_tag_read_compact data length = {}", dataA.dataValues.length);
                                if (dataA.dataValues.length < 10) {
                                    Logger.trace("!!! UplinkPackage_Event_csl_tag_read_compact data length has length equal or less than 6");
                                    dataA.decodedError = "Received Event_csl_tag_read_compact with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                } else {
                                    int index = 0;
                                    byte[] dataHeader = new byte[6]; System.arraycopy(dataA.dataValues, 0, dataHeader, 0, dataHeader.length);
                                    byte[] dataValuesFull = new byte[dataA.dataValues.length - 6]; System.arraycopy(dataA.dataValues, 6, dataValuesFull, 0, dataValuesFull.length);
                                    Logger.apData("ApData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_compact with payload header = {}, dataValuesFull = {}", byteArrayToString(dataHeader), byteArrayToString(dataValuesFull));
                                    while (index < dataValuesFull.length) { //change from while
                                        Logger.debug("Looping with index = {}, dataValuesFull.length = {}", index, dataValuesFull.length);
                                        dataA.decodedTime = System.currentTimeMillis();
                                        if (dataValuesFull.length >= index + 2) {
                                            dataA.decodedPc = new byte[2];
                                            System.arraycopy(dataValuesFull, index, dataA.decodedPc, 0, dataA.decodedPc.length);
                                            index += 2;
                                        } else break;

                                        int epcLength = getBytes2EpcLength(dataA.decodedPc); //((dataA.decodedPc[0] & 0xFF) >> 3) * 2;
                                        Logger.debug("decoded decodedPc = {} with epclength = {}", byteArrayToString(dataA.decodedPc), epcLength);
                                        if (dataValuesFull.length >= index + epcLength) {
                                            dataA.decodedEpc = new byte[epcLength];
                                            System.arraycopy(dataValuesFull, index, dataA.decodedEpc, 0, epcLength);
                                            index += epcLength;
                                        } else break;

                                        Logger.debug("decoded decodedEpc = {}", byteArrayToString(dataA.decodedEpc));
                                        if (dataValuesFull.length >= index + 2) {
                                            dataA.decodedRssi = get2BytesOfRssi(dataValuesFull, index);
                                            Logger.debug("decoded decodedRssi = {}", dataA.decodedRssi);
                                            index += 2;
                                        } else break;

                                        mRx000ToRead.add(dataA);
                                        Logger.debug("3006 dataA.responseType = {}", dataA.responseType.toString());
                                        Logger.apData("ApData: uplink data UplinkPackage_Event_csl_tag_read_compact tag with Epc = {} is uploaded to mRx000ToRead with mRx000ToRead.size = {}", byteArrayToString(dataA.decodedEpc), mRx000ToRead.size());

                                        dataA = new RfidReaderChipData.Rx000pkgData();
                                        dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                    }
                                    Logger.debug("Exit while loop with index = {}, dataValuesFull.length = {}", index, dataValuesFull.length);
                                    if (index != dataValuesFull.length) {
                                        byte[] bytesUnhandled = new byte[dataValuesFull.length - index];
                                        System.arraycopy(dataValuesFull, index, bytesUnhandled, 0, bytesUnhandled.length);
                                        Logger.trace("!!! unhandled data: {}", byteArrayToString(bytesUnhandled));
                                    }
                                    Logger.apData("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_compact has been processed");
                                }
                            } else if (iUplinkPackageType == 0x3007) {
                                Logger.debug("Check UplinkPackage_Event_csl_miscellaneous_event data length = {}", dataA.dataValues.length);
                                int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                if (dataA.dataValues.length < 6 || (iCommand >= 3 && dataA.dataValues.length < 8)) {
                                    Logger.trace("!!! UplinkPackage_Event_csl_miscellaneous_event data length has length equal or less than 8");
                                } else {
                                    switch (iCommand) {
                                        case 1:
                                            rx000Setting.keepAliveTime = new Date();
                                            break;
                                        case 2:
                                            rx000Setting.inventoryRoundEndTime = new Date();
                                            break;
                                        case 3:
                                            rx000Setting.crcErrorRate = ((dataA.dataValues[6] & 0xFF) << 8) + (dataA.dataValues[7] & 0xFF);
                                            break;
                                        case 4:
                                            rx000Setting.tagRate = ((dataA.dataValues[6] & 0xFF) << 8) + (dataA.dataValues[7] & 0xFF);
                                            break;
                                        default:
                                            Logger.trace("!!! iCommand cannot be recognised for the uplink data {}", byteArrayToString(dataA.dataValues));
                                            break;
                                    }
                                    Logger.pkData("PkData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_miscellaneous_event has been processed");
                                }
                            } else if (iUplinkPackageType == 0x3008) {
                                dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END;
                                setInventoring(false);
                                Logger.toLogView("mRx000UplinkHandler_3008: {}", byteArrayToString(dataA.dataValues)).trace();
                                Logger.debug("Check UplinkPackage_Event_csl_operation_complete data length = {}", dataA.dataValues.length);
                                if (dataA.dataValues.length < 8) {
                                    Logger.trace("!!! UplinkPackage_Event_csl_operation_complete data length has length equal or less than 8");
                                    dataA.decodedError = "Received Event_csl_operation_complete with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                } else {
                                    int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                    int iStatus = (dataA.dataValues[6] & 0xFF) * 256 + (dataA.dataValues[7] & 0xFF);
                                    Logger.apData("ApData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete");
                                    Logger.debug("Check iStatus = {}", iStatus);
                                    switch (iStatus) {
                                        case 0:
                                            dataA.decodedError = null;
                                            break;
                                        case 1:
                                            dataA.decodedError = "0x0001 Tag cache table buffer is overflowed";
                                            break;
                                        case 2:
                                            dataA.decodedError = "0x0002 Wrong register address";
                                            break;
                                        case 3:
                                            dataA.decodedError = "0x0003 Register length too large";
                                            break;
                                        case 4:
                                            dataA.decodedError = "0x0004 E710 not powered up";
                                            break;
                                        case 5:
                                            dataA.decodedError = "0x0005 Invalid parameter";
                                            break;
                                        case 6:
                                            dataA.decodedError = "0x0006 Event fifo full";
                                            break;
                                        case 7:
                                            dataA.decodedError = "0x0007 TX not ramped up";
                                            break;
                                        case 8:
                                            dataA.decodedError = "0x0008 Register read only";
                                            break;
                                        case 9:
                                            dataA.decodedError = "0x0009 Failed to halt";
                                            break;
                                        case 10:
                                            dataA.decodedError = "0x000A PLL not locked";
                                            break;
                                        case 11:
                                            dataA.decodedError = "0x000B Power control target failed";
                                            break;
                                        case 12:
                                            dataA.decodedError = "0x000C Radio power not enabled";
                                            break;
                                        case 13:
                                            dataA.decodedError = "0x000D E710 command error (e.g. battery low)";
                                            break;
                                        case 14:
                                            dataA.decodedError = "0x000E E710 Op timeout";
                                            break;
                                        case 15:
                                            dataA.decodedError = "0x000F E710 Aggregate error (e.g. battery low, metal reflection)";
                                            break;
                                        case 0x10:
                                            dataA.decodedError = "0x0010 E710 hardware link error";
                                            break;
                                        case 0x11:
                                            dataA.decodedError = "0x0011 E710 event fail to send error";
                                            break;
                                        case 0x12:
                                            dataA.decodedError = "0x0012 E710 antenna error (e.g. metal reflection)";
                                            break;
                                        case 0x00FF:
                                            dataA.decodedError = "0x00FF Other error (e.g. battery low)";
                                            break;
                                        default:
                                            dataA.decodedError = "Unknown error";
                                            Logger.trace("!!! CANNOT handle status type with {}.{}", byteArrayToString(header), byteArrayToString(payload));
                                            break;
                                    }
                                    mRx000ToRead.add(dataA);
                                    Logger.debug("3008 dataA.responseType = {}", dataA.responseType.toString());
                                    Logger.apData("ApData: uplink data UplinkPackage_Event_csl_operation_complete with decodedError = {} is uploaded to mRx000ToRead with mRx000ToRead.size = {}", dataA.decodedError, mRx000ToRead.size());
                                    Logger.apData("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete has been processed");
                                }
                            } else if (iUplinkPackageType == 0x3009) {
                                dataA.responseType = RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                                Logger.debug("Check UplinkPackage_Event_csl_access_complete data length = {}", dataA.dataValues.length);
                                if (dataA.dataValues.length < 12) {
                                    Logger.trace("!!! UplinkPackage_Event_csl_access_complete data length has length equal or less than 12");
                                    dataA.decodedError = "Received Event_csl_access_complete with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                } else {
                                    int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                    int iTagError = dataA.dataValues[6];
                                    int iMacError = dataA.dataValues[7];
                                    int iWriteCount = dataA.dataValues[8] * 256 + dataA.dataValues[9];
                                    byte[] bytesResponse = null, bytesHeader = new byte[12];
                                    System.arraycopy(dataA.dataValues, 0, bytesHeader, 0, bytesHeader.length);
                                    if (dataA.dataValues.length > 12) {
                                        bytesResponse = new byte[dataA.dataValues.length - 12];
                                        System.arraycopy(dataA.dataValues, 12, bytesResponse, 0, bytesResponse.length);
                                        Logger.debug("bytesResponse = {}", byteArrayToString(bytesResponse));
                                    }
                                    String string = null;
                                    switch (iTagError) {
                                        case 0x00:
                                            string = "Other error";
                                            break;
                                        case 0x01:
                                            string = "Not supported";
                                            break;
                                        case 0x02:
                                            string = "Insufficient privileges";
                                            break;
                                        case 0x03:
                                            string = "Memory overrun";
                                            break;
                                        case 0x04:
                                            string = "Memory locked";
                                            break;
                                        case 0x05:
                                            string = "Crypto suite error";
                                            break;
                                        case 0x06:
                                            string = "Command not encapsulated";
                                            break;
                                        case 0x07:
                                            string = "ResponseBuffer overflow";
                                            break;
                                        case 0x08:
                                            string = "Security timeout";
                                            break;
                                        case 0x0B:
                                            string = "Insufficient power";
                                            break;
                                        case 0x0F:
                                            string = "Non-specific error";
                                            break;
                                        case 0x10:
                                            //string = "No error";
                                            break;
                                        default:
                                            string = "OTHER errors";
                                            break;
                                    }
                                    if (string != null)
                                        dataA.decodedError = "Tag Error: " + string;
                                    string = null;
                                    switch (iMacError) {
                                        case 0x00:
                                            //string = "No error";
                                            break;
                                        case 0x01:
                                            string = "No tag reply";
                                            break;
                                        case 0x02:
                                            string = "Invalid password";
                                            break;
                                        case 0x03:
                                            string = "Failed to send command";
                                            break;
                                        case 0x04:
                                            string = "No access reply";
                                            break;
                                        default:
                                            string = "OTHER errors";
                                            break;
                                    }
                                    if (string != null) {
                                        if (dataA.decodedError == null)
                                            dataA.decodedError = "Mac Error: " + string;
                                        else dataA.decodedError += (", Mac Error: " + string);
                                    }
                                    if (iCommand == 0xC3 && iWriteCount == 0) {
                                        string = "Write Error: nothing is written";
                                        if (dataA.decodedError == null)
                                            dataA.decodedError = string;
                                        else dataA.decodedError += (", " + string);
                                        Logger.trace(String.format("rx000pkgData: Command 0x%X with mRfidToWrite.size = %s", iCommand, csReaderConnector.rfidConnector.mRfidToWrite.size()));
                                    }
                                    Logger.debug("decodedError2 = {}", dataA.decodedError);
                                    Logger.trace("bytesResponse is {}", bytesResponse == null ? "null" : byteArrayToString(bytesResponse));
                                    if (bytesResponse != null && bytesResponse.length != 0) {
                                        for (int i = 0; i < bytesResponse.length; i++) {
                                            string = String.format("%02X", (byte) ((bytesResponse[i] & 0xFF)));
                                            if (dataA.decodedResult == null)
                                                dataA.decodedResult = string;
                                            else dataA.decodedResult += string;
                                        }
                                    } else dataA.decodedResult = "";
                                    Logger.debug("decodedResult = {}", dataA.decodedResult);
                                }
                                mRx000ToRead.add(dataA);
                                Logger.debug("3009 dataA.responseType = {}", dataA.responseType);
                                Logger.apData("ApData: uplink data UplinkPackage_Event_csl_access_complete tag with data = {} is uploaded to mRx000ToRead with mRx000ToRead.size = {}", byteArrayToString(dataA.dataValues), mRx000ToRead.size());
                                Logger.apData("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_access_complete has been processed");
                            }
                            else Logger.trace("{} with uplink data {}.{}", String.format("!!! CANNOT handle UplinkPackageType 0x%X", iUplinkPackageType), byteArrayToString(header), byteArrayToString(payload));
                            packageFound = true;
                            packageType = 4;
                            startIndexNew = startIndex + expectedLength;
                        }
                    }

                    if (packageFound) {
                        packageFound = false;
                        Logger.debug("Found package with packageType = {}, Check startIndex = {} with startIndexNew = {}, mRfidToReadingOffset = {}", packageType, startIndex, startIndexNew, mRfidToReadingOffset);
                        if (startIndex != 0) {
                            byte[] unhandledBytes = new byte[startIndex];
                            System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                            Logger.debug("!!! packageFound with invalid unused data: {}, {}", unhandledBytes.length, byteArrayToString(unhandledBytes));
                            csReaderConnector.invalidUpdata++;
                        }
                        byte[] usedBytes = new byte[startIndexNew - startIndex];
                        System.arraycopy(mRfidToReading, startIndex, usedBytes, 0, usedBytes.length);
                        Logger.debug("used data = {}, {}", usedBytes.length, byteArrayToString(usedBytes));
                        byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                        System.arraycopy(mRfidToReading, startIndexNew, mRfidToReadingNew, 0, mRfidToReadingOffset - startIndexNew);
                        mRfidToReading = mRfidToReadingNew;
                        mRfidToReadingOffset -= startIndexNew;
                        startIndex = 0;
                        startIndexNew = 0;
                        startIndexOld = 0;
                        Logger.debug("Check new mRfidToReadingOffset = {} with startIndex and startIndexNew = 0", mRfidToReadingOffset);
                        if (mRfidToReadingOffset != 0) {
                            byte[] remainedBytes = new byte[mRfidToReadingOffset];
                            System.arraycopy(mRfidToReading, 0, remainedBytes, 0, remainedBytes.length);
                            Logger.debug("!!! moved with remained bytes={}", byteArrayToString(remainedBytes));
                        }
                    } else {
                        startIndex++;
                    }
                }
                Logger.debug("Exit while loop with startIndex = {}, mRfidToReadingOffset = {}, iPayloadSizeMin = {}", startIndex, mRfidToReadingOffset, iPayloadSizeMin);
                if (startIndex != 0 && mRfidToReadingOffset != 0) {
                    //Logger.trace("exit while(-8) loop with startIndex = " + startIndex + ( startIndex == 0 ? "" : "(NON-ZERO)" ) + ", mRfidToReadingOffset=" + mRfidToReadingOffset);
                    if (startIndex > mRfidToReadingOffset) Logger.trace("!!! ERROR. startIndex = {} is greater than mRfidToReadingOffset = {}", startIndex, mRfidToReadingOffset);
                    else {
                        byte[] unhandled = new byte[startIndex];
                        System.arraycopy(mRfidToReading, 0, unhandled, 0, unhandled.length);
                        Logger.trace("!!! Unhandled data: {}", byteArrayToString(unhandled));
                        byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                        System.arraycopy(mRfidToReading, startIndex, mRfidToReadingNew, 0, mRfidToReadingOffset - startIndex);
                        mRfidToReading = mRfidToReadingNew;
                        mRfidToReadingOffset = mRfidToReadingOffset - startIndex;
                        startIndex = 0;
                        startIndexNew = 0;
                        csReaderConnector.invalidUpdata++;
                    }
                }
            }
        }
        if (bFirst == false) Logger.trace("Exit while loop with mRfidToRead.size = {}", csReaderConnector.rfidConnector.mRfidToRead.size());
        //Logger.debug("mRfidToReadingOffset = {}, startIndexNew = {}", mRfidToReadingOffset, startIndexNew);
        //if (mRfidToReadingOffset == startIndexNew && mRfidToReadingOffset != 0) {
        //    byte[] unusedData = new byte[mRfidToReadingOffset];
        //    System.arraycopy(mRfidToReading, 0, unusedData, 0, unusedData.length);
        //    Logger.trace("Ending with invaid unused data: " + mRfidToReadingOffset + ", " + byteArrayToString(unusedData));
        //    mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
        //    mRfidToReadingOffset = 0;
        //}
        bRx000ToReading = false;
        if (mRx000ToRead.size() != 0) Logger.debug("mRx000UplinkHandler(): END with mRx000ToRead size = {}", mRx000ToRead.size());
    }
    public boolean turnOn(boolean onStatus) {
        RfidConnector.CsReaderRfidData csReaderRfidData = new RfidConnector.CsReaderRfidData();
        if (onStatus) {
            csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_POWER_ON;
            csReaderRfidData.waitUplinkResponse = false;
            addRfidToWrite(csReaderRfidData);
            return true;
        } else if (onStatus == false) {
            csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_POWER_OFF;
            csReaderRfidData.waitUplinkResponse = false;
            addRfidToWrite(csReaderRfidData);
            return true;
        } else {
            return false;
        }
    }

    boolean bLowPowerStandby = false;
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        if (csReaderConnector.isBleConnected() == false) return false;
        if (this.bLowPowerStandby == bLowPowerStandby) return true;
        this.bLowPowerStandby = bLowPowerStandby;
        Logger.trace("!!! Skip setPwrManagementMode[{}] with this.blowPowerStandby = {}", bLowPowerStandby, this.bLowPowerStandby);
        return true;
    }

    int wideRSSI = -1;
    public int getwideRSSI() {
        if (wideRSSI < 0) {
            setPwrManagementMode(false);
            wideRSSI = 0;
            Logger.trace("!!! Skip getwideRSSI with assumed value = 0");
        }
        return wideRSSI;
    }

    final int RXGAIN_INVALID = -1, RXGAIN_MIN = 0, RXGAIN_MAX = 0x1FF, RXGAIN_DEFAULT = 0x104;
    int rxGain = RXGAIN_INVALID;
    public int getHighCompression() {
        int iRetValue = -1;
        if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
            setPwrManagementMode(false);
            rxGain = RXGAIN_DEFAULT;
            Logger.trace(String.format("!!! Skip getHighCompression with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
        } else iRetValue = (rxGain >> 8);
        return iRetValue;
    }
    public int getRflnaGain() {
        int iRetValue = -1;
        if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
            setPwrManagementMode(false);
            rxGain = RXGAIN_DEFAULT;
            Logger.trace(String.format("!!! Skip getRflnaGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
        } else iRetValue = ((rxGain & 0xC0) >> 6);
        return iRetValue;
    }
    public int getIflnaGain() {
        int iRetValue = -1;
        if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
            setPwrManagementMode(false);
            rxGain = RXGAIN_DEFAULT;
            Logger.trace(String.format("!!! Skip getIflnaGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
        } else iRetValue = ((rxGain & 0x38) >> 3);
        return iRetValue;
    }
    public int getAgcGain() {
        int iRetValue = -1;
        if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
            setPwrManagementMode(false);
            rxGain = RXGAIN_DEFAULT;
            Logger.trace(String.format("!!! Skip getAgcGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
        } else iRetValue = (rxGain & 0x07);
        return iRetValue;
    }
    public int getRxGain() {
        int iRetValue = -1;
        if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
            setPwrManagementMode(false);
            rxGain = RXGAIN_DEFAULT;
            Logger.trace(String.format("!!! Skip getRxGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
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
            rxGain = rxGain_new;
            Logger.trace(String.format("!!! Skip setRxGain[0x%X]", rxGain_new));
        }
        return bRetValue;
    }

    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        Logger.trace("!!! hostCommand = {}", hostCommand.toString());
        long hostCommandData = -1;
        switch (hostCommand) {
            case CMD_18K6CINV:
                hostCommandData = 0xA1;
                if (rx000Setting.getQuerySelect() > 1 /*&& mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA3;
                break;
            case CMD_18K6CINV_COMPACT:
                hostCommandData = 0xA2;
                if (rx000Setting.getQuerySelect() > 1 /*&& mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA6;
                break;
            case CMD_18K6CINV_MB:
                hostCommandData = 0xA4;
                Logger.trace("getQuerySelect = {}, getImpinjExtension = {}", rx000Setting.getQuerySelect(), rx000Setting.getImpinjExtension());
                if (rx000Setting.getQuerySelect() > 1 /*&& mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA5;
                break;
            case NULL:
                hostCommandData = 0xAE;
                break;
            case CMD_18K6CREAD:
                hostCommandData = 0xB1;
                break;
            case CMD_18K6CWRITE:
                hostCommandData = 0xB2;
                break;
            case CMD_18K6CLOCK:
                hostCommandData = 0xB7;
                break;
            case CMD_18K6CKILL:
                hostCommandData = 0xB8;
                break;
            case CMD_18K6CAUTHENTICATE:
                hostCommandData = 0xB9;
                break;

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
            case CMD_READBUFFER:
                hostCommandData = 0x51;
                break;
            case CMD_UNTRACEABLE:
                hostCommandData = 0x52;
                break;
            case CMD_FDM_RDMEM:
                hostCommandData = 0x53; break;
            case CMD_FDM_WRMEM:
                hostCommandData = 0x54; break;
            case CMD_FDM_AUTH:
                hostCommandData = 0x55; break;
            case CMD_FDM_GET_TEMPERATURE:
                hostCommandData = 0x56; break;
            case CMD_FDM_START_LOGGING:
                hostCommandData = 0x57; break;
            case CMD_FDM_STOP_LOGGING:
                hostCommandData = 0x58; break;
            case CMD_FDM_WRREG:
                hostCommandData = 0x59; break;
            case CMD_FDM_RDREG:
                hostCommandData = 0x5A; break;
            case CMD_FDM_DEEP_SLEEP:
                hostCommandData = 0x5B; break;
            case CMD_FDM_OPMODE_CHECK:
                hostCommandData = 0x5C; break;
            case CMD_FDM_INIT_REGFILE:
                hostCommandData = 0x5d; break;
            case CMD_FDM_LED_CTRL:
                hostCommandData = 0x5e; break;
            default:
                Logger.trace("!!! CANNOT handle with hostCommand = {}", hostCommand);
        }
        if (hostCommandData == -1) {
            return false;
        } else {
            commandOperating = true;
            byte[] msgBuffer = new byte[]{(byte)0x80, (byte)0xb3, (byte)0x10, (byte)0xA1, 0, 0, 0};
            msgBuffer[3] = (byte) (hostCommandData % 256);
            Logger.trace("3030 data = {}", byteArrayToString(rx000Setting.antennaPortConfig[0]));
            return sendHostRegRequest(HostRegRequests.HST_CMD, true, msgBuffer);
        }
    }

    ArrayList<byte[]> macAccessHistory = new ArrayList<>();
    void addMacAccessHistory(byte[] msgBuffer) {
        byte[] msgBuffer4 = Arrays.copyOf(msgBuffer, 4);
        for (int i = 0; i < macAccessHistory.size(); i++) {
            byte[] macAccessHistory4 = Arrays.copyOf(macAccessHistory.get(i), 4);
            if (Arrays.equals(msgBuffer4, macAccessHistory4)) {
                Logger.trace("macAccessHistory: deleted old record={}", byteArrayToString(macAccessHistory4));
                macAccessHistory.remove(i);
                break;
            }
        }
        Logger.trace("macAccessHistory: added msgbuffer={}", byteArrayToString(msgBuffer));
        macAccessHistory.add(msgBuffer);
    }

    byte downlinkSequenceNumber = 0;
    boolean sendHostRegRequest(HostRegRequests hostRegRequests, boolean writeOperation, byte[] msgBuffer) {
        boolean needResponse = false;
        boolean validRequest = false;

        if (hostRegRequests == HostRegRequests.HST_ANT_DESC_DWELL) Logger.trace("setAntennaDwell 4");
        boolean bSkip = false;
        if ( (hostRegRequests != HostRegRequests.HST_CMD && hostRegRequests != HostRegRequests.MAC_OPERATION)
                || (hostRegRequests == HostRegRequests.HST_CMD
                && msgBuffer[3] != (byte)0xA1
                && msgBuffer[3] != (byte)0xA2
                && msgBuffer[3] != (byte)0xA3
                && msgBuffer[3] != (byte)0xA4
                && msgBuffer[3] != (byte)0xA5
                && msgBuffer[3] != (byte)0xA6
                && msgBuffer[3] != (byte)0xAE
                && msgBuffer[3] != (byte)0xB1
                && msgBuffer[3] != (byte)0xB2
                && msgBuffer[3] != (byte)0xB7
                && msgBuffer[3] != (byte)0xB8
                && msgBuffer[3] != (byte)0xB9
        ) || (hostRegRequests == HostRegRequests.MAC_OPERATION && writeOperation && msgBuffer[0] != (byte)0x80)
        ) bSkip = true;
        if (bSkip) {
            Logger.trace("!!! Skip sendingRegRequest with {}, writeOperation = {}.{}", hostRegRequests, writeOperation, byteArrayToString(msgBuffer));
            return true;
        }
        if (csReaderConnector.isBleConnected() == false) {
            Logger.trace("!!! Skip sending as bleConnected is false");
            return false;
        }
        if (false) addMacAccessHistory(msgBuffer);
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

        if (msgBuffer == null || validRequest == false) {
            Logger.trace("invalid request for msgbuffer = {}, validRequest = {}", msgBuffer == null ? "NULL" : "Valid", validRequest);
            return false;
        } else {
            RfidConnector.CsReaderRfidData csReaderRfidData = new RfidConnector.CsReaderRfidData();
            csReaderRfidData.rfidPayloadEvent = RfidConnector.RfidPayloadEvents.RFID_COMMAND;
            csReaderRfidData.dataValues = msgBuffer;
            csReaderRfidData.waitUplinkResponse = true; //(needResponse || writeOperation == false);
            if (msgBuffer[0] == (byte)0x80
                    && msgBuffer[1] == (byte)0xB3
            ) {
                csReaderRfidData.dataValues[4] = downlinkSequenceNumber++;
                if (msgBuffer[2] == 0x10
                        && msgBuffer[3] != (byte)0xA1
                        && msgBuffer[3] != (byte)0xA2
                        && msgBuffer[3] != (byte)0xA3
                        && msgBuffer[3] != (byte)0xA4
                        && msgBuffer[3] != (byte)0xA5
                        && msgBuffer[3] != (byte)0xA6
                        && msgBuffer[3] != (byte)0xB1
                        && msgBuffer[3] != (byte)0xB2
                        && msgBuffer[3] != (byte)0xB7
                        && msgBuffer[3] != (byte)0xB8
                        && msgBuffer[3] != (byte)0xB9
                ) csReaderRfidData.waitUplink1Response = true;
            }
            addRfidToWrite(csReaderRfidData);
            return true;
        }
    }

    void addRfidToWrite(RfidConnector.CsReaderRfidData csReaderRfidData) {
        boolean repeatRequest = false;
        if (false && csReaderRfidData.rfidPayloadEvent == RfidConnector.RfidPayloadEvents.RFID_COMMAND) {
            Logger.trace("!!! Skip {}.{}", csReaderRfidData.rfidPayloadEvent, byteArrayToString(csReaderRfidData.dataValues));
            return;
        }
        if (csReaderConnector.rfidConnector.mRfidToWrite.size() != 0 && sameCheck) {
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
            csReaderConnector.rfidConnector.mRfidToWrite.add(csReaderRfidData);
            Logger.pkData("PkData: add {}{}{}{}{} to mRfidToWrite with length = {}", csReaderRfidData.rfidPayloadEvent, csReaderRfidData.dataValues != null ? "." : "", byteArrayToString(csReaderRfidData.dataValues),
                    csReaderRfidData.waitUplinkResponse ? " waitUplinkResponse" : "", csReaderRfidData.waitUplink1Response ? " waitUplink1Response" : "",
                    csReaderConnector.rfidConnector.mRfidToWrite.size());
        } else Logger.pkData("!!! Skip repeated sending {}{}{}", csReaderRfidData.rfidPayloadEvent, csReaderRfidData.dataValues != null ? "." : "", byteArrayToString(csReaderRfidData.dataValues));
    }

    boolean inventoring = false;
    public boolean isInventoring() { return  inventoring; }
    void setInventoring(boolean enable) { inventoring = enable; utility.debugFileEnable(false); Logger.trace("setInventoring E710 is set as {}", inventoring);}
    boolean decode710Data(byte[] dataValues){
        Logger.debug("mRfidToWrite.size = {}", csReaderConnector.rfidConnector.mRfidToWrite.size());
        if (csReaderConnector.rfidConnector.mRfidToWrite.size() > 0) {
            RfidConnector.CsReaderRfidData csReaderRfidData = csReaderConnector.rfidConnector.mRfidToWrite.get(0);
            Logger.debug("downlinkResponsed = {}, uplinkResponsed = {}", csReaderRfidData.downlinkResponded, csReaderRfidData.uplinkResponded);
            if (csReaderRfidData.downlinkResponded || csReaderRfidData.uplinkResponded) {
                boolean matched = false, updatedUplinkResponse = false;
                Logger.debug("mRfidToWrite.dataValue = {}, dataValues = {}", byteArrayToString(csReaderRfidData.dataValues), byteArrayToString(dataValues));
                if (csReaderRfidData.dataValues[0] == (byte)0x80
                        && csReaderRfidData.dataValues[1] == (byte)0xB3
                        && dataValues[0] == 0x51
                        && dataValues[1] == (byte)0xE2
                        && csReaderRfidData.dataValues[2] == dataValues[2]
                        && csReaderRfidData.dataValues[3] == dataValues[3]
                        && csReaderRfidData.dataValues[4] == dataValues[4]
                ) {
                    boolean valid = false;
                    byte[] commandValue = null;
                    int iCommandCode = (dataValues[2] & 0xFF) * 256 + (dataValues[3] & 0xFF);
                    int iLength = dataValues[5] * 256 + dataValues[6];
                    if (iLength != 0) {
                        commandValue = new byte[iLength];
                        System.arraycopy(dataValues, 7, commandValue, 0, commandValue.length);
                    }
                    Logger.pkData("PkData: found Rfid.Uplink.DataRead.CommandResponse{}", iLength != 0 ? " with payload = " + byteArrayToString(commandValue) : "");
                    Logger.debug("found iCommandCode = {}, iLength = {}, commandValue = {}", String.format("%4X", iCommandCode), iLength, byteArrayToString(commandValue));
                    if ((iCommandCode == 0x10A1 && iLength == 0)
                            || (iCommandCode == 0x10A2 && iLength == 0)
                            || (iCommandCode == 0x10A3 && iLength == 0)
                            || (iCommandCode == 0x10A4 && iLength == 0)
                            || (iCommandCode == 0x10A5 && iLength == 0)
                            || (iCommandCode == 0x10A6 && iLength == 0)
                            || (iCommandCode == 0x10AE && iLength == 0)
                            || (iCommandCode == 0x10B1 && iLength == 0)
                            || (iCommandCode == 0x10B2 && iLength == 0)
                            || (iCommandCode == 0x10B7 && iLength == 0)
                            || (iCommandCode == 0x10B8 && iLength == 0)
                            || (iCommandCode == 0x10B9 && iLength == 0)
                            || (iCommandCode == 0x1471 && csReaderRfidData.dataValues.length == 11)
                            || (iCommandCode == 0x9A06 && commandValue.length == 1)
                    ) valid = true;
                    String strCommandResponseType = null;
                    if (iCommandCode == 0x10A1) strCommandResponseType = "RfidStartSimpleInventory";
                    else if (iCommandCode == 0x10A2) strCommandResponseType = "RfidStartCompactInventory";
                    else if (iCommandCode == 0x10A3) strCommandResponseType = "RfidStartSelectInventory";
                    else if (iCommandCode == 0x10A4) strCommandResponseType = "RfidStartMBInventory";
                    else if (iCommandCode == 0x10A5) strCommandResponseType = "RfidStartSelectMBInventory";
                    else if (iCommandCode == 0x10A6) strCommandResponseType = "RfidStartSelectCompactInventory";
                    else if (iCommandCode == 0x10AE) strCommandResponseType = "RfidStopOperation";
                    else if (iCommandCode == 0x10B1) strCommandResponseType = "RfidReadMB";
                    else if (iCommandCode == 0x10B2) strCommandResponseType = "RfidWriteMB";
                    else if (iCommandCode == 0x10B7) strCommandResponseType = "RfidLock";
                    else if (iCommandCode == 0x10B8) strCommandResponseType = "RfidKill";
                    else if (iCommandCode == 0x10B9) strCommandResponseType = "RfidAuthenticate";
                    if (valid) {
                        if (csReaderRfidData.waitUplink1Response) {
                            csReaderConnector.rfidConnector.found = true;
                            csReaderRfidData.uplinkResponded = true; updatedUplinkResponse = true;
                            csReaderConnector.rfidConnector.mRfidToWrite.set(0, csReaderRfidData);
                            Logger.pkData("PkData: Rfid.Uplink.DataRead.CommandResponse_{} is processed to set mRfidToWrite.uplinkResponded and wait uplink data 1", strCommandResponseType);
                        } else {
                            matched = true;
                            Logger.debug(String.format("000 iCommandCode = 0x%X with writeData.dataValues = %s", iCommandCode, byteArrayToString(csReaderRfidData.dataValues)));
                            if (iCommandCode == 0x10A1) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartSimpleInventory"); }
                            else if (iCommandCode == 0x10A2) { setInventoring(true); Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartCompactInventory"); }
                            else if (iCommandCode == 0x10A3) { setInventoring(true); Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartSelectInventory"); }
                            else if (iCommandCode == 0x10A4) { setInventoring(true); Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartMBInventory"); }
                            else if (iCommandCode == 0x10A5) { setInventoring(true); Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartSelectMBInventory"); }
                            else if (iCommandCode == 0x10A6) { setInventoring(true); Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStartSelectCompactInventory"); }
                            else if (iCommandCode == 0x10AE) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidStopOperation"); }
                            else if (iCommandCode == 0x10B1) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidReadMB"); }
                            else if (iCommandCode == 0x10B2) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidWriteMB"); }
                            else if (iCommandCode == 0x10B7) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidLock"); }
                            else if (iCommandCode == 0x10B8) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidKill"); }
                            else if (iCommandCode == 0x10B9) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RfidAuthenticate"); }
                            //else if ((iCommandCode & 0x7F00) == 0x1000) { Logger.pkData("PkData: uplink data is processed as CommandResponse.RFID??? !!!"); }
                            else if (iCommandCode == 0x1471 || iCommandCode == 0x9A06) {
                                int iRegAddr = (csReaderRfidData.dataValues[8] & 0xFF) * 256 + (csReaderRfidData.dataValues[9] & 0xFF);
                                boolean bprocessed = false;
                                Logger.debug(String.format("1 iCommandCode = 0x%X with iRegAddr = 0x%X", iCommandCode, iRegAddr));
                                if (iCommandCode == 0x9A06) {
                                    Logger.debug(String.format("2 CommandCode = 0x%X is processed here", iCommandCode));
                                    bprocessed = true;
                                } else if (iRegAddr == 8) {
                                    Logger.debug("2 iCommandCode");
                                    try {
                                        rx000Setting.macVer = new String(commandValue, StandardCharsets.UTF_8).trim();
                                        bprocessed = true;
                                        Logger.trace("macVer = {}", rx000Setting.macVer);
                                    } catch (Exception e) {
                                        //throw new RuntimeException(e);
                                    }
                                } else if (iRegAddr == 0x28 && commandValue.length >= 3) {
                                    int iValue = 0;
                                    for (int i = 0, increment = 1; i < commandValue.length; i++, increment *= 10) {
                                        iValue = commandValue[commandValue.length - 1 - i] * increment;
                                    }
                                    rx000Setting.macVerBuild = iValue;
                                    bprocessed = true;
                                    Logger.trace("macVerBuild = {}", rx000Setting.macVerBuild);
                                } else if (iRegAddr == 0x3014) {
                                    rx000Setting.countryEnum = commandValue;
                                    bprocessed = true;
                                    Logger.trace("countryEnum = {}", byteArrayToString(rx000Setting.countryEnum));
                                } else if (iRegAddr == 0x3018) {
                                    rx000Setting.frequencyChannelIndex = commandValue;
                                    bprocessed = true;
                                    Logger.trace("frequencyChannelIndex = {}", byteArrayToString(rx000Setting.frequencyChannelIndex));
                                } else if (iRegAddr >= 0x3030 && iRegAddr < 0x3030 + 16 * 16) {
                                    int iPort = 0, iOffset = 0, iWidth = 0;
                                    for (iPort = 0; iPort < 16; iPort++) {
                                        Logger.debug("antennaPortConfig: iPort = {}{}", iPort, String.format(", iRegAddr = 0x%04X", iRegAddr));
                                        if (iRegAddr < 0x3030 + (iPort + 1) * 16) break;
                                    }
                                    iOffset = iRegAddr - 0x3030 - iPort * 16;
                                    Logger.debug("antennaPortConfig: iOffset = {}", iOffset);
                                    iWidth = commandValue.length;
                                    Logger.debug("antennaPortConfig: iWidth = {}", iWidth);
                                    if (iOffset == 0 && iWidth == 16) {
                                        rx000Setting.antennaPortConfig[iPort] = commandValue;
                                        bprocessed = true;
                                        Logger.trace("antennaPortConfig[{}] = {}", iPort, byteArrayToString(rx000Setting.antennaPortConfig[iPort]));
                                    } else Logger.trace("!!! CANNOT handle with iPort = {}, iOffset = {}, iWidth = {}", iPort, iOffset, iWidth);
                                } else if (iRegAddr >= 0x3140 && iRegAddr < 0x3140 + 42 * 7) {
                                    int index = 0, iOffset = 0, iWidth = 0;
                                    for (index = 0; index < 7; index++) {
                                        Logger.debug("selectConfiguration: index = {}{}", index, String.format(", iRegAddr = 0x%04X", iRegAddr));
                                        if (iRegAddr < 0x3140 + (index + 1) * 42) break;
                                    }
                                    iOffset = iRegAddr - 0x3140 - index * 42;
                                    Logger.debug("selectConfiguration: iOffset = {}", iOffset);
                                    iWidth = commandValue.length;
                                    Logger.debug("selectConfiguration: iWidth = {}", iWidth);
                                    if (iOffset == 0 && iWidth == 42) {
                                        rx000Setting.selectConfiguration[index] = commandValue;
                                        bprocessed = true;
                                        Logger.trace("selectConfiguration[{}] = {}", index, byteArrayToString(rx000Setting.selectConfiguration[index]));
                                    } else Logger.trace("!!! CANNOT handle with index = {}, iOffset = {}, iWidth = {}", index, iOffset, iWidth);
                                } else if (iRegAddr >= 0x3270 && iRegAddr < 0x3270 + 7 * 3) {
                                    int index = 0, iOffset = 0, iWidth = 0, iPortStartAddr = 0x3270, iPortSize = 7;
                                    for (index = 0; index < 3; index++) {
                                        Logger.debug("multibankReadConfig: index = {}{}", index, String.format(", iRegAddr = 0x%04X", iRegAddr));
                                        if (iRegAddr < iPortStartAddr + (index + 1) * iPortSize) break;
                                    }
                                    iOffset = iRegAddr - iPortStartAddr - index * iPortSize;
                                    Logger.debug("multibankReadConfig: iOffset = {}", iOffset);
                                    iWidth = commandValue.length;
                                    Logger.debug("multibankReadConfig: iWidth = {}", iWidth);
                                    if (iOffset == 0 && iWidth == iPortSize) {
                                        rx000Setting.multibankReadConfig[index] = commandValue;
                                        bprocessed = true;
                                        Logger.trace("multibankReadConfig[{}] = {}", index, byteArrayToString(rx000Setting.multibankReadConfig[index]));
                                    } else Logger.trace("!!! CANNOT handle with index = {}, iOffset = {}, iWidth = {}", index, iOffset, iWidth);
                                } else if (iRegAddr == 0x38A6) {
                                    rx000Setting.accessPassword = commandValue;
                                    bprocessed = true;
                                    Logger.trace("accessPassword = {}", byteArrayToString(rx000Setting.accessPassword));
                                } else if (iRegAddr == 0x38AA) {
                                    rx000Setting.killPassword = commandValue;
                                    bprocessed = true;
                                    Logger.trace("killPassword = {}", byteArrayToString(rx000Setting.killPassword));
                                } else if (iRegAddr == 0x3900) {
                                    rx000Setting.dupElimRollWindow = commandValue;
                                    bprocessed = true;
                                    Logger.trace("dupElimDelay = {}", byteArrayToString(rx000Setting.dupElimRollWindow));
                                } else if (iRegAddr == 0x3906) {
                                    rx000Setting.eventPacketUplnkEnable = commandValue;
                                    bprocessed = true;
                                    Logger.trace("eventPacketUplnkEnable = {}", byteArrayToString(rx000Setting.eventPacketUplnkEnable));
                                } else if (iRegAddr == 0x3908) {
                                    rx000Setting.intraPacketDelay = commandValue;
                                    bprocessed = true;
                                    Logger.trace("intraPacketDelay = {}", byteArrayToString(rx000Setting.intraPacketDelay));
                                } else if (iRegAddr == 0x3948) {
                                    rx000Setting.currentPort = commandValue;
                                    bprocessed = true;
                                    Logger.trace("currentPort = {}", byteArrayToString(rx000Setting.currentPort));
                                } else if (iRegAddr == 0x5000) {
                                    rx000Setting.modelCode = commandValue;
                                    bprocessed = true;
                                    Logger.trace("modelCode = {}", byteArrayToString(rx000Setting.modelCode));
                                } else if (iRegAddr == 0x5020) {
                                    rx000Setting.productSerialNumber = commandValue;
                                    bprocessed = true;
                                    Logger.trace("productSerialNumber = {}", byteArrayToString(rx000Setting.productSerialNumber));
                                } else if (iRegAddr == 0x5040) {
                                    rx000Setting.countryEnumOem = commandValue;
                                    bprocessed = true;
                                    Logger.trace("countryEnumOem = {}", byteArrayToString(rx000Setting.countryEnumOem));
                                } else if (iRegAddr == 0xEF98) {
                                    rx000Setting.countryCodeOem = commandValue;
                                    bprocessed = true;
                                    Logger.trace("countryCodeOem = {}", byteArrayToString(rx000Setting.countryCodeOem));
                                } else if (iRegAddr == 0xEF9C) {
                                    rx000Setting.boardSerialNumber = commandValue;
                                    bprocessed = true;
                                    Logger.trace("boardSerialNumber = {}", byteArrayToString(rx000Setting.boardSerialNumber));
                                } else if (iRegAddr == 0xEFAC) {
                                    rx000Setting.specialcountryCodeOem = commandValue;
                                    bprocessed = true;
                                    Logger.trace("specialcountryCodeOem = {}", byteArrayToString(rx000Setting.specialcountryCodeOem));
                                } else if (iRegAddr == 0xEFB0) {
                                    rx000Setting.freqModifyCode = commandValue;
                                    bprocessed = true;
                                    Logger.trace("freqModifyCode = {}", byteArrayToString(rx000Setting.freqModifyCode));
                                }
                                if (bprocessed) {
                                    if (iCommandCode == 0x9A06)
                                        Logger.pkData("PkData: Rfid.Uplink.DataRead.CommandResponse_WriteRegister with result {} is processed for register address = {}", byteArrayToString(commandValue), String.format("0x%X", iRegAddr));
                                    else if (iCommandCode == 0x1471)
                                        Logger.pkData("PkData: Rfid.Uplink.DataRead.CommandResponse_ReadRegister with result {} is processed for register address = {}", byteArrayToString(commandValue), String.format("0x%X", iRegAddr));
                                    else {
                                        Logger.pkData("PkData: Rfid.Uplink.DataRead.CommandResponse_xxx with result {} is processed", byteArrayToString(commandValue));
                                    }
                                } else Logger.trace("!!! Rfid.Uplink.DataRead.CommandResponse_ReadRegister CANNOT be processed for register address = {}", String.format("0x%X", iRegAddr));
                            } else Logger.trace(String.format("!!! Rfid.Uplink.DataRead.CommandResponse_%X CANNOT be processed", iCommandCode));
                        }
                    } else Logger.trace("!!! Rfid.Uplink.DataRead.CommandResponse CANNOT be processed");
                } else if (dataValues[0] == 0x49
                        && dataValues[1] == (byte)0xDC
                        && dataValues[4] == 0
                ) {
                    Logger.debug("Ready 0");
                    int iLength = dataValues[5] * 256 + dataValues[6];
                    Logger.debug("Ready 1 with length = {}", iLength);
                    if (dataValues.length == 7 + iLength) {
                        byte[] dataValues1 = new byte[iLength];
                        System.arraycopy(dataValues, 7, dataValues1, 0, dataValues1.length);
                        int iCommand = (dataValues[2] & 0xFF) * 256 + (dataValues[3] & 0xFF);
                        if (iCommand == 0x3008) {
                            setInventoring(false);
                            Logger.toLogView("isRfidToRead_3008: {}", byteArrayToString(dataValues)).debug();
                            Logger.debug("Ready 2");
                            Logger.pkData("PkData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete with payload = {}", byteArrayToString(dataValues1));
                            if (csReaderRfidData.dataValues[2] == dataValues1[4]
                                    && csReaderRfidData.dataValues[3] == dataValues1[5]) {
                                Logger.debug("Ready 3");

                                int iStatus = dataValues1[6] * 256 + dataValues1[7];
                                if (csReaderRfidData.uplinkResponded) {
                                    matched = true;
                                    Logger.pkData("PkData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete is processed with status = {}", String.format("%04X", iStatus));
                                }
                            } else Logger.trace("!!! mismatched command code");
                        }
                    }
                }
                                /*
                                int count = 0;
                                if (barcodeToWrite.get(0).dataValues[0] == 0x1b) {
                                    commandType = BarcodeCommendTypes.COMMAND_COMMON;
                                    count = 1;
                                    Logger.trace("uplink data is processed with count = " + count + " for barcodeToWrite data = " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                } else if (barcodeToWrite.get(0).dataValues[0] == 0x7E) {
                                    matched = true;
                                    commandType = BarcodeCommendTypes.COMMAND_QUERY;
                                    int index = 0;
                                    while (dataValues.length - index >= 5 + 1) {
                                        if (dataValues[index+0] == 2 && dataValues[index+1] == 0 && dataValues[index+4] == 0x34) {
                                            int length = dataValues[index+2] * 256 + dataValues[index+3];
                                            if (dataValues.length - index >= length + 4 + 1) {
                                                matched = true;
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
                                                    }
                                                    if (true) Logger.trace("uplink data is processed as Barcode Prefix = " + byteArrayToString(bytesBarcodePrefix) + ", Suffix = " + byteArrayToString(bytesBarcodeSuffix));
                                                } else if (barcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
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
                                                    if (true) Logger.trace("uplink data " + byteArrayToString(byteVersion) + " is processsed as version = " + versionNumber);
                                                } else if (barcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                                                    if (dataValues[index+5] == barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeToWrite.get(0).dataValues[7]) {
                                                        matched = true; //for ESN, S/N or Date
                                                        byte[] byteSN = new byte[length - 3];
                                                        System.arraycopy(dataValues, index + 7, byteSN, 0, byteSN.length);
                                                        String serialNumber;
                                                        try {
                                                            serialNumber = new String(byteSN, "UTF-8");
                                                            int snLength = Integer.parseInt(serialNumber.substring(0, 2));
                                                            if (snLength + 2 == serialNumber.length()) {
                                                                serialNumber = serialNumber.substring(2);
                                                            } else serialNumber = null;
                                                        } catch (Exception e) {
                                                            serialNumber = null;
                                                        }
                                                        Logger.trace("uplink data is processed as Barcode serial number [" + serialNumber + "] for index = " + index);
                                                        if (dataValues[index+6] == (byte)0x32) strESN = serialNumber;
                                                        else if (dataValues[index+6] == (byte)0x33) strSerialNumber = serialNumber;
                                                        else if (dataValues[index+6] == (byte)0x34) strDate = serialNumber;
                                                    }
                                                } else if (barcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                                                    if (dataValues[index+5] == barcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == barcodeToWrite.get(0).dataValues[7]) {
                                                        matched = true;
                                                        if (barcodeToWrite.get(0).dataValues[6] == 0x30 && barcodeToWrite.get(0).dataValues[7] == 0x30  && barcodeToWrite.get(0).dataValues[8] == 0x30) {
                                                            bBarcodeTriggerMode = dataValues[7];
                                                            if (dataValues[index + 7] == 0x30) {
                                                                Logger.trace("uplink data is processed as Barcode Reading mode TRIGGER");
                                                            } else
                                                                Logger.trace("uplink data is processed as Barcode Reading mode " + String.valueOf(dataValues[7]));
                                                        } else Logger.trace("uplink data is processed as incorrect response !!!");
                                                    } else if (true) {
                                                        matched = true;
                                                        Logger.trace("uplink data is processed as incorrect response !!!");
                                                    }
                                                }
                                                index += (length + 5);
                                            } else break;
                                        } else index++;
                                    }
                                    if (matched) { Logger.debug("Matched Query response"); }
                                    else Logger.trace("uplink data is processed as Mis-matched Query response");
                                } else {
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
                                }
                                if (count != 0) {
                                    if (false) Logger.trace("dataValues.length = " + dataValues.length + ", okCount = " + iOkCount + ", count = " + count + " for barcodeToWrite data = " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                    matched = false; boolean foundOk = false;
                                    for (int k = 0; k < dataValues.length; k++) {
                                        boolean match06 = false;
                                        if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++iOkCount == count) matched = true; }
                                        if (match06 == false) break;
                                        foundOk = true; found = true;
                                    }
                                    if (false) Logger.trace("00 matcched = " + matched);
                                    if (matched) Logger.trace("uplink data is processed with matched = " + matched + ", OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                    else if (foundOk) Logger.trace("uplink data is processed with matched = " + matched + ", but OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(barcodeToWrite.get(0).dataValues));
                                    else {
                                        mBarcodeDevice.mBarcodeToRead.add(cs108BarcodeData);
                                        Logger.trace("uplink data Barcode.DataRead." + byteArrayToString(cs108BarcodeData.dataValues) + " is added to mBarcodeToRead");
                                    }
                                }*/
                if (matched) {
                    csReaderConnector.rfidConnector.found = true;
                    csReaderConnector.rfidConnector.mRfidToWrite.remove(0); csReaderConnector.rfidConnector.sendRfidToWriteSent = 0; csReaderConnector.rfidConnector.mRfidToWriteRemoved = true;
                    Logger.pkData("PkData: new mRfidToWrite size = " + csReaderConnector.rfidConnector.mRfidToWrite.size());
                }
                if (matched || updatedUplinkResponse) return true;
            }
        }
        return false;
    }
}
