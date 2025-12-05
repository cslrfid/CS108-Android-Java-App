package com.csl.cslibrary4a;

import android.content.Context;
import android.util.Log;
import android.widget.Button;

public class TagAxzonOpus {
    public static SelectData selectData;
    TagBanks tagBanks;
    String TAG = "Hello";

    public final float fNO_SUCH_SETTING = 65522;
    public final int iNO_SUCH_SETTING = 65522;
    public TagAxzonOpus(Context context, CsLibrary4A csLibrary4A, CustomMediaPlayer playerN, CustomMediaPlayer playerO, Button buttonRead, Button buttonWrite) {
        tagBanks = new TagBanks(context, csLibrary4A, playerN, playerO, buttonRead, buttonWrite);
    }
    public CustomAsyncTask.Status getReadWriteStatus() {
        return tagBanks.getReadWriteStatus();
    }
    int batteryLevel = iNO_SUCH_SETTING;
    public int getBatteryLevel() {
        appendToLog("TagAxzonOpus.getBatteryLevel 1");
        if (true || batteryLevel == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getBatteryLevel 2");
            int iOffset = 3;
            if (tagBanks.stringsUser == null || tagBanks.stringsUser.length <= iOffset || tagBanks.stringsUser[iOffset] == null) {
                tagBanks.setBankDataStart(selectData, 3, 3, 1, null);
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getBatteryLevel 3 with string as " + tagBanks.stringsUser[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsUser[iOffset], 16);
            appendToLog("TagAxzonOpus.getBatteryLevel 3 with iValue = " + iValue);
            iValue >>= 4;
            batteryLevel = iValue;
            appendToLog("TagAxzonOpus.getBatteryLevel 3 with batteryLevel as " + batteryLevel);

            tagBanks.stringsUser[iOffset] = null;
        }
        return batteryLevel;
    }
    public enum LoggerStateTypes {
        SLEEP, STANDBY, STATE2, READY,
        STATE4, BAP, LOGGING, FINISHED
    }
    LoggerStateTypes loggerStateType = null;
    public LoggerStateTypes getLoggerStateType() {
        appendToLog("TagAxzonOpus.getLoggerStateType 1");
        if (false && loggerStateType == null) {
            appendToLog("TagAxzonOpus.getLoggerStateType 2");
            int iOffset = 5;
            if (tagBanks.stringsUser == null || tagBanks.stringsUser[iOffset] == null) {
                tagBanks.setBankDataStart(selectData, 3, iOffset, 1, null);
                return null;
            }
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with stringsUser[" + iOffset + "] = " + tagBanks.stringsUser[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsUser[iOffset], 16);
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with iValue = " + iValue);
            iValue &= 0x7;
            loggerStateType = LoggerStateTypes.values()[iValue];
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with loggerStateType = " + loggerStateType.toString());

            tagBanks.stringsUser[iOffset] = null;
        }
        if (true || loggerStateType == null) {
            appendToLog("TagAxzonOpus.getLoggerStateType 2");
            int iOffset = 1;
            if (tagBanks.stringsEpc == null || tagBanks.stringsEpc.length <= iOffset || tagBanks.stringsEpc[iOffset] == null) {
                tagBanks.setBankDataStart(selectData, 1, iOffset, 1,  null);
                return null;
            }
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with stringsEpc[1] = " + tagBanks.stringsEpc[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsEpc[iOffset], 16);
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with iValue = " + iValue);
            iValue = iValue >> 5;
            iValue &= 0x7;
            loggerStateType = LoggerStateTypes.values()[iValue];
            appendToLog("TagAxzonOpus.getLoggerStateType 3 with loggerStateType = " + loggerStateType.toString());

            tagBanks.stringsEpc[1] = null;
        }
        return loggerStateType;
    }
    int clock = iNO_SUCH_SETTING;
    public int getClock() {
        appendToLog("TagAxzonOpus.getClock 1");
        if (true || clock == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getClock 2");
            int iOffset = 6;
            if (tagBanks.stringsUser == null || tagBanks.stringsUser.length <= (iOffset + 1) || tagBanks.stringsUser[iOffset] == null || tagBanks.stringsUser[iOffset+1] == null ) {
                tagBanks.setBankDataStart(selectData, 3, iOffset, 2, null);
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getClock 3 with string as " + tagBanks.stringsUser[iOffset]);
            int iValueL = Integer.valueOf(tagBanks.stringsUser[iOffset], 16);
            appendToLog("TagAxzonOpus.getClock 3 with iValueL as " + iValueL);
            appendToLog("TagAxzonOpus.getClock 3 with string as " + tagBanks.stringsUser[iOffset + 1]);
            int iValueH = Integer.valueOf(tagBanks.stringsUser[iOffset + 1], 16);
            appendToLog("TagAxzonOpus.getClock 3 with iValueH as " + iValueH);
            clock = ((iValueH & 0xFF) << 16) + (iValueL & 0xFFFF);
            appendToLog("TagAxzonOpus.getClock 3 with iValueL as " + iValueL);

            tagBanks.stringsUser[iOffset] = null; tagBanks.stringsUser[iOffset + 1] = null;
        }
        return clock;
    }
    int nextLogAddress = iNO_SUCH_SETTING;
    public int getNextLogAddress() {
        appendToLog("TagAxzonOpus.getNextLogAddress 1");
        if (true || nextLogAddress == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getNextLogAddress 2");
            int iOffset = 0x1B;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData1AReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getNextLogAddress 3 with string as " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getNextLogAddress 3 with iValue as " + iValue);
            nextLogAddress = iValue & 0x0FFF;
            appendToLog("TagAxzonOpus.getNextLogAddress 3 with nextLogAddress as " + iValue);

            tagBanks.stringsTid[iOffset] = null;
        }
        return nextLogAddress;
    }
    public void setNextLogAddress(int iValue) {
        appendToLog("TagAxzonOpus.setNextLogAddress with input = " + iValue);
        int iOffset = 0x1B;
        String string = String.format("%04X", iValue & 0xFFF);
        appendToLog("TagAxzonOpus.setNextLogAddress with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    public static class TidAlarmTypes {
        public int lowTemperatureAlarm = 0;
        public int highTemperatureAlarm = 0;
        public int tamperAlarm = 0;
        public int batteryAlarm = 0;
        public boolean isAlarm() {
            boolean bValue = false;
            if (lowTemperatureAlarm > 0) bValue = true;
            else if (highTemperatureAlarm > 0) bValue = true;
            else if (tamperAlarm > 0) bValue = true;
            else if (batteryAlarm > 0) bValue = true;
            return bValue;
        }
    }
    TidAlarmTypes tidAlarmType = null;
    public TidAlarmTypes getTidAlarmType() {
        appendToLog("TagAxzonOpus.getTidAlarmType 1");
        if (true || tidAlarmType == null) {
            appendToLog("TagAxzonOpus.getTidAlarmType 2");
            int iOffset = 0x10;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return null;
            }
            appendToLog("TagAxzonOpus.getTidAlarmType 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getTidAlarmType 3 with iValue = " + iValue);
            iValue &= 0xF;
            tidAlarmType = new TidAlarmTypes();
            tidAlarmType.lowTemperatureAlarm = iValue & 1;
            tidAlarmType.highTemperatureAlarm = iValue & 2;
            tidAlarmType.tamperAlarm = iValue & 4;
            tidAlarmType.batteryAlarm = iValue & 8;
            appendToLog("TagAxzonOpus.getTidAlarmType 3 with lowTemperatureAlarm = " + tidAlarmType.lowTemperatureAlarm + ", highTemperatureAlarm = " + tidAlarmType.highTemperatureAlarm
                + ", tamperAlarm = " + tidAlarmType.tamperAlarm + ", batteryAlarm = " + tidAlarmType.batteryAlarm);

            getAlarmLowerDelay(); //update other bits before clear
            getAlarmUpperDelay();
            getDelayedLoggingStart();

            appendToLog("TagAxzonOpus.getTidAlarmType 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            tagBanks.stringsTid[iOffset] = null;
        }
        return tidAlarmType;
    }
    public void setTidAlarmType(TidAlarmTypes tidAlarmType) {
        appendToLog("TagAxzonOpus.setTidAlarmType with lowTemperatureAlarm = " + tidAlarmType.lowTemperatureAlarm  + ", highTemperatureAlarm = " + tidAlarmType.highTemperatureAlarm + ", tamperAlarm = " + tidAlarmType.tamperAlarm + ", batteryAlarm = " + tidAlarmType.batteryAlarm);

        int iOffset = 0x10;
        int iValue = 0, iValue1;
        iValue1 = getDelayedLoggingStart(); iValue1 &= 0x7; iValue1 <<= 10; iValue |= iValue1; appendToLog("TagAxzonOpus.setTidAlarmType 1 with string = " + String.format("%04X", iValue & 0x1FFF));
        iValue1 = getAlarmUpperDelay() - 1; iValue1 &= 0x7; iValue1 <<= 7; iValue |= iValue1; appendToLog("TagAxzonOpus.setTidAlarmType 2A with string = " + String.format("%04X", iValue & 0x1FFF));
        iValue1 = getAlarmLowerDelay() - 1; iValue1 &= 0x7; iValue1 <<= 4; iValue |= iValue1; appendToLog("TagAxzonOpus.setTidAlarmType 3 with string = " + String.format("%04X", iValue & 0x1FFF));
        if (tidAlarmType.batteryAlarm > 0) iValue |= 8;
        if (tidAlarmType.tamperAlarm > 0) iValue |= 4;
        if (tidAlarmType.highTemperatureAlarm > 0) iValue |= 2;
        if (tidAlarmType.lowTemperatureAlarm > 0) iValue |= 1;
        String string = String.format("%04X", iValue & 0x1FFF);
        appendToLog("TagAxzonOpus.setTidAlarmType with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    int fingerArmedClock = iNO_SUCH_SETTING;
    public int getFingerArmedClock() {
        appendToLog("TagAxzonOpus.getFingerArmedClock 1");
        if (true || fingerArmedClock == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getFingerArmedClock 2");
            int iOffset = 0x0d;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= (iOffset + 1) || tagBanks.stringsTid[iOffset] == null || tagBanks.stringsTid[iOffset+1] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getFingerArmedClock 3 with string as " + tagBanks.stringsTid[iOffset] + ", " + tagBanks.stringsTid[iOffset+1]);
            int iValueM = Integer.valueOf(tagBanks.stringsTid[iOffset], 16); iValueM &= 0xFF;
            int iValueL = Integer.valueOf(tagBanks.stringsTid[iOffset+1], 16);
            appendToLog("TagAxzonOpus.getFingerArmedClock 3 with iValue as " + iValueM + ", " + iValueL);
            fingerArmedClock = ((iValueM & 0xFF) << 16) + (iValueL & 0xFFFF);

            appendToLog("TagAxzonOpus.getFingerArmedClock 3 with iValue as " + fingerArmedClock);
            tagBanks.stringsTid[iOffset] = null; tagBanks.stringsTid[iOffset + 1] = null;
        }
        return fingerArmedClock;
    }
    public void setFingerArmedClock(int iValue) {
        appendToLog("TagAxzonOpus.setFingerArmedClock with input = " + iValue);
        int iOffset = 0x0d;
        String string = String.format("%08X", iValue & 0xFFFFFF);
        appendToLog("TagAxzonOpus.setFingerArmedClock with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 2, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    int firstTamperAlarmAddress = iNO_SUCH_SETTING;
    public int getFirstTamperAlarmAddress() {
        appendToLog("TagAxzonOpus.getFirstTamperAlarmAddress 1");
        if (true || firstTamperAlarmAddress == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getFirstTamperAlarmAddress 2 with " + (tagBanks.stringsTid == null ? "stringsTid = null" : ", stringsTid.length = " + tagBanks.stringsTid.length + ", stringsTid[0x0c] = " + (tagBanks.stringsTid[12] == null ? "null" : "valid")));
            int iOffset = 0x0C;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getFirstTamperAlarmAddress 3 with string as " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getFirstTamperAlarmAddress 3 with iValue as " + iValue);
            firstTamperAlarmAddress = iValue & 0x0FFF;
            appendToLog("TagAxzonOpus.getFirstTamperAlarmAddress 3 with iValue as " + firstTamperAlarmAddress);

            tagBanks.stringsTid[iOffset] = null;
        }
        return firstTamperAlarmAddress;
    }
    public void setFirstTamperAlarmAddress(int iValue) {
        appendToLog("TagAxzonOpus.setFirstTamperAlarmAddress with input = " + iValue);
        int iOffset = 0x0C;
        String string = String.format("%04X", iValue & 0x0FFF);
        appendToLog("TagAxzonOpus.setFirstTamperAlarmAddress with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    int firstTemperatureAlarmAddress = iNO_SUCH_SETTING;
    public int getFirstTemperatureAlarmAddress() {
        appendToLog("TagAxzonOpus.getFirstTemperatureAlarmAddress 1");
        if (true || firstTemperatureAlarmAddress == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getFirstTemperatureAlarmAddress 2");
            int iOffset = 0x0b;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getFirstTemperatureAlarmAddress 3 with string as " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getFirstTemperatureAlarmAddress 3 with iValue as " + iValue);
            firstTemperatureAlarmAddress = iValue & 0x0FFF;
            appendToLog("TagAxzonOpus.getFirstTemperatureAlarmAddress 3 with iValue as " + firstTemperatureAlarmAddress);

            tagBanks.stringsTid[iOffset] = null;
        }
        return firstTemperatureAlarmAddress;
    }
    public void setFirstTemperatureAlarmAddress(int iValue) {
        appendToLog("TagAxzonOpus.setFirstTemperatureAlarmAddress with input = " + iValue);
        int iOffset = 0x0b;
        String string = String.format("%04X", iValue & 0x0FFF);
        appendToLog("TagAxzonOpus.setFirstTemperatureAlarmAddress with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    int alarmLowerDelayed = iNO_SUCH_SETTING; int iTidA_backup = 0;
    public int getAlarmLowerDelayed() {
        appendToLog("TagAxzonOpus.getAlarmLowerDelayed 1");
        if (true || alarmLowerDelayed == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmLowerDelayed 2");
            int iOffset = 0x0A;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null ) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getAlarmLowerDelayed 3 with string as " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getAlarmLowerDelayed 3 with iValue as " + iValue);

            iValue = iValue >> 12;
            alarmLowerDelayed = (iValue & 0xF);
            appendToLog("TagAxzonOpus.getAlarmLowerDelayed 3 with alarmLowerDelayed as " + alarmLowerDelayed);

            getAlarmLowerLimitx16(); //update other bits before clear

            iTidA_backup = iValue;
            tagBanks.stringsTid[iOffset] = null;
        }
        appendToLog("TagAxzonOpus.getAlarmLowerDelayed 4: setTidAlarmType, alarmLowerDelayed = " + alarmLowerDelayed);
        return alarmLowerDelayed;
    }
    public void setAlarmLowerDelayed(int iValue) {
        appendToLog("TagAxzonOpus.setAlarmLowerDelayed with input = " + iValue);
        int iOffset = 0x0A;
        int iValue1 = getAlarmLowerLimitx16() + ((iValue & 0x0F) << 12);
        String string = String.format("%04X", iValue1 & 0xFFFF);
        appendToLog("TagAxzonOpus.setAlarmLowerDelayed with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    int alarmUpperDelayed = iNO_SUCH_SETTING; int iTid9_backup = 0;
    public int getAlarmUpperDelayed() {
        appendToLog("TagAxzonOpus.getAlarmUpperDelayed 1");
        if (true || alarmUpperDelayed == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmUpperDelayed 2");
            int iOffset = 9;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null ) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }
            appendToLog("TagAxzonOpus.getAlarmUpperDelayed 3 with string as " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getAlarmUpperDelayed 3 with iValue as " + iValue);

            int iValue1 = iValue >> 12;
            alarmUpperDelayed = (iValue1 & 0xF);
            appendToLog("TagAxzonOpus.getAlarmLowerDelayed 3 with alarmUpperDelayed as " + alarmUpperDelayed);

            getAlarmUpperLimitx16(); //update other bits before clear

            iTid9_backup = iValue;
            tagBanks.stringsTid[iOffset] = null;
        }
        appendToLog("TagAxzonOpus.getAlarmUpperDelayed 4: setTidAlarmType, alarmUpperDelayed = " + alarmUpperDelayed);
        return alarmUpperDelayed;
    }
    public void setAlarmUpperDelayed(int iValue) {
        appendToLog("TagAxzonOpus.setAlarmUpperDelayed with input = " + iValue);
        int iOffset = 0x09;
        int iValue1 = getAlarmUpperLimitx16() + ((iValue & 0x0F) << 12);
        String string = String.format("%04X", iValue1 & 0xFFFF);
        appendToLog("TagAxzonOpus.setAlarmUpperDelayed with string = " + string);
        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
    }
    public enum DisableEnableTypes {
        DISABLE, ENABLE
    }
    DisableEnableTypes batteryLowAlarmType = null; int iXPC_backup = iNO_SUCH_SETTING;
    public DisableEnableTypes getInitialBatteryLowAlarmType() {
        appendToLog("TagAxzonOpus.getInitialBatteryLowAlarmType 1");
        if (true || batteryLowAlarmType == null) {
            appendToLog("TagAxzonOpus.getInitialBatteryLowAlarmType 2");
            int iOffset = 0x21;
            if (tagBanks.stringsEpc == null || tagBanks.stringsEpc.length <= iOffset || tagBanks.stringsEpc[iOffset] == null) {
                tagBanks.setBankDataStart(selectData, 1, iOffset, 1, null);
                return null;
            }
            appendToLog("TagAxzonOpus.getInitialBatteryLowAlarmType 3 with string as " + tagBanks.stringsEpc[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsEpc[iOffset], 16);
            appendToLog("TagAxzonOpus.getInitialBatteryLowAlarmType 3 with iValue = " + iValue);

            int iValue1 = iValue & 0x800; // 0x800 for bit 11, 0x10 for bit 4
            if (iValue1 == 0) batteryLowAlarmType = DisableEnableTypes.DISABLE;
            else batteryLowAlarmType = DisableEnableTypes.ENABLE;
            appendToLog("TagAxzonOpus.getInitialBatteryLowAlarmType 3 with batteryLowAlarmType = " + batteryLowAlarmType.toString());

            iXPC_backup = iValue;
            tagBanks.stringsEpc[iOffset] = null;
        }
        return batteryLowAlarmType;
    }
    public void setInitialBatteryLowAlarmType(boolean enable) {
        appendToLog("TagAxzonOpus.setInitialBatteryLowAlarmType with input = " + enable);
        int iOffset = 0x21;
        int iValue1 = (enable ? (iXPC_backup | 0x800) : (iXPC_backup & ~0x800));
        String string = String.format("%04X", iValue1 & 0xFFFF);
        appendToLog("TagAxzonOpus.setInitialBatteryLowAlarmType with string = " + string);
        tagBanks.setBankDataStart(selectData, 1, iOffset, 1, string);
        tagBanks.stringsEpc[iOffset] = string;
    }
    public static class EpcAlarmTypes {
        public int batteryInstalled = 0;
        public int batteryConnected = 0;
        public int temperatureAlarm = 0;
        public int batteryAlarm = 0;
        public int tamperAlarm = 0;
        public boolean isAlarm() {
            boolean bValue = false;
            if (temperatureAlarm > 0) bValue = true;
            else if (batteryAlarm > 0) bValue = true;
            else if (tamperAlarm > 0) bValue = true;
            return bValue;
        }
    }
    EpcAlarmTypes epcAlarmType = null; int oPC_backup = iNO_SUCH_SETTING;
    public EpcAlarmTypes getEpcAlarmType() {
        appendToLog("TagAxzonOpus.getEpcAlarmType 1");
        if (true || epcAlarmType == null) {
            appendToLog("TagAxzonOpus.getEpcAlarmType 2");
            int iOffset = 1;
            if (tagBanks.stringsEpc == null || tagBanks.stringsEpc.length <= iOffset || tagBanks.stringsEpc[iOffset] == null) {
                tagBanks.setBankDataStart(selectData, 1, iOffset, 1, null);
                return null;
            }
            appendToLog("TagAxzonOpus.getEpcAlarmType 3 with stringsEpc[" + iOffset + "] = " + tagBanks.stringsEpc[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsEpc[iOffset], 16);
            appendToLog("TagAxzonOpus.getEpcAlarmType 3 with iValue = " + iValue);

            epcAlarmType = new EpcAlarmTypes();
            epcAlarmType.tamperAlarm = iValue & 1;
            epcAlarmType.batteryAlarm = iValue & 2;
            epcAlarmType.temperatureAlarm = iValue & 4;
            epcAlarmType.batteryConnected = iValue & 8;
            epcAlarmType.batteryInstalled = iValue & 16;
            appendToLog("TagAxzonOpus.getEpcAlarmType 3 with temperatureAlarm = " + epcAlarmType.temperatureAlarm + ", batteryAlarm = " + epcAlarmType.batteryAlarm + ", tamperAlarm = " + epcAlarmType.tamperAlarm);

            oPC_backup = iValue;

            appendToLog("TagAxzonOpus.getTidAlarmType 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsEpc[iOffset]);
            tagBanks.stringsEpc[iOffset] = null;
        }
        return epcAlarmType;
    }
    public void setEpcAlarmType(EpcAlarmTypes epcAlarmType) {
        appendToLog("TagAxzonOpus.setEpcAlarmType with input temperatureAlarm = " + epcAlarmType.temperatureAlarm + ", batteryAlarm = " + epcAlarmType.batteryAlarm + ", tamperAlarm = " + epcAlarmType.tamperAlarm);
        int iOffset = 1;
        int iValue = oPC_backup & ~0x7;
        if (epcAlarmType.tamperAlarm > 0) iValue |= 1;
        if (epcAlarmType.batteryAlarm > 0) iValue |= 2;
        if (epcAlarmType.temperatureAlarm > 0) iValue |= 4;
        String string = String.format("%04X", iValue);
        appendToLog("TagAxzonOpus.setEpcAlarmType with string = " + string);
        tagBanks.setBankDataStart(selectData,1, iOffset, 1, string);
        tagBanks.stringsEpc[iOffset] = string;
    }
    public enum LoggingIntervalTypes {
        INTERVAL_1SEC, INTERVAL_5SEC, INTERVAL_10SEC, INTERVAL_15SEC, INTERVAL_20SEC, INTERVAL_25SEC, INTERVAL_30SEC,
        INTERVAL_1MIN, INTERVAL_2MIN, INTERVAL_3MIN, INTERVAL_4MIN, INTERVAL_5MIN, INTERVAL_6MIN, INTERVAL_7MIN,
        INTERVAL_10MIN, INTERVAL_15MIN, INTERVAL_20MIN, INTERVAL_25MIN, INTERVAL_30MIN, INTERVAL_35MIN, INTERVAL_40MIN,
        INTERVAL_1HOUR, INTERVAL_2HOUR, INTERVAL_3HOUR, INTERVAL_4HOUR, INTERVAL_5HOUR, INTERVAL_6HOUR, INTERVAL_7HOUR, INTERVAL_8HOUR
    }
    LoggingIntervalTypes loggingIntervalType = null;
    public LoggingIntervalTypes getLoggingIntervalType() {
        appendToLog("TagAxzonOpus.getLoggingIntervalType 1");
        if (loggingIntervalType == null) {
            appendToLog("TagAxzonOpus.getLoggingIntervalType 2");
            int iOffset = 8, iOffset1 = 0x0F;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset1 || tagBanks.stringsTid[iOffset] == null || tagBanks.stringsTid[iOffset1] == null) {
                setTidBankData8ReadStart();
                return null;
            }

            for (int i = iOffset; i < iOffset1 + 1; i++) {
                appendToLog("TagAxzonOpus.getLoggingIntervalType 3 with stringsTid[" + i + "] = " + tagBanks.stringsTid[i]);
            }
            int iMsb = Integer.parseInt(tagBanks.stringsTid[iOffset], 16) & 0x100;
            int iLsb = (Integer.parseInt(tagBanks.stringsTid[iOffset1], 16) >> 3) & 0x0F;
            appendToLog("TagAxzonOpus.getLoggingIntervalType 3 with iMsb = " + iMsb + ", iLsb = " + iLsb);
            loggingIntervalType = null;
            if (iMsb != 0) {
                if (iLsb >= 0 && iLsb <= 10) {
                    loggingIntervalType = LoggingIntervalTypes.values()[iLsb];
                } else if (iLsb >= 0x0B && iLsb <= 0x0C) {
                    loggingIntervalType = LoggingIntervalTypes.values()[12 + iLsb - 0x0B];
                }
            } else {
                if (iLsb == 0) loggingIntervalType = LoggingIntervalTypes.INTERVAL_5MIN; //Okay
                else if (iLsb >= 1) {
                    loggingIntervalType = LoggingIntervalTypes.values()[14 + iLsb - 1];
                }
            }
        }
        return loggingIntervalType;
    }
    public void setLoggingIntervalType(LoggingIntervalTypes loggingIntervalType) {
        appendToLog("TagAxzonOpus.setLoggingIntervalType with input = " + loggingIntervalType.toString() + ", stringsTid.length = " + tagBanks.stringsTid.length);
        int iOffset = 8, iOffset1 = 0x0F;
        String[] strings = new String[iOffset1-iOffset+1];
        System.arraycopy(tagBanks.stringsTid, iOffset, strings, 0, strings.length);

        boolean bMsbExpected = (loggingIntervalType.ordinal() <= LoggingIntervalTypes.INTERVAL_7MIN.ordinal());
        int iValueExpected = -1;
        if (bMsbExpected) {
            if (loggingIntervalType == LoggingIntervalTypes.INTERVAL_5MIN) {
                bMsbExpected = false;
                iValueExpected = 0;
            } else if (loggingIntervalType.ordinal() > LoggingIntervalTypes.INTERVAL_5MIN.ordinal() )
                iValueExpected = loggingIntervalType.ordinal() - 1;
            else iValueExpected = loggingIntervalType.ordinal();
        } else iValueExpected = loggingIntervalType.ordinal() - LoggingIntervalTypes.INTERVAL_7MIN.ordinal();
        appendToLog("TagAxzonOpus.setLoggingIntervalType with bMsbExpected = " + bMsbExpected + ", iValueExpected = " + iValueExpected);

        int iValueH = Integer.parseInt(strings[8-iOffset], 16);
        if (bMsbExpected) iValueH |= 0x100;
        else iValueH &= ~0x100;
        strings[8-iOffset] = String.format("%04X", iValueH);
        int iValueL = Integer.parseInt(strings[0x0F-iOffset], 16);
        iValueL &= ~0x78;
        iValueL |= (iValueExpected << 3);
        strings[0x0F-iOffset] = String.format("%04X", iValueL);

        StringBuilder stringData = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            stringData.append(strings[i]);
            appendToLog("TagAxzonOpus.setLoggingIntervalType with strings[" + i + "] = " + strings[i] + ", stringData = " + stringData.toString());
        }
        tagBanks.setBankDataStart(selectData, 2, 8 , strings.length, stringData.toString());
        this.loggingIntervalType =  null;
    }
    public static class FingerSpotStartupEnables {
        public int samplingRegimeEnable = 0;
        public int fingerSpotStartEnable = 0;
        public int tamperDetectEnable = 0;
        public int tamperDisconnectPolarity = 0;
        public boolean isEnable() {
            boolean bValue = false;
            if (samplingRegimeEnable > 0) bValue = true;
            else if (fingerSpotStartEnable > 0) bValue = true;
            else if (tamperDetectEnable > 0) bValue = true;
            return bValue;
        }
    }
    FingerSpotStartupEnables fingerSpotStartupEnables = null;
    public FingerSpotStartupEnables getFingerSpotStartupEnables() {
        appendToLog("TagAxzonOpus.getFingerSpotStartupEnables 1");
        if (fingerSpotStartupEnables == null) {
            appendToLog("TagAxzonOpus.getFingerSpotStartupEnables 2");
            int iOffset = 8;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return null;
            }

            appendToLog("TagAxzonOpus.getFingerSpotStartupEnables 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.parseInt(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getFingerSpotStartupEnables 3 with iValue = " + iValue);

            fingerSpotStartupEnables = new FingerSpotStartupEnables();
            if ((iValue & 0x08) != 0) fingerSpotStartupEnables.fingerSpotStartEnable = 1;
            else fingerSpotStartupEnables.fingerSpotStartEnable = 0;
            if ((iValue & 0x400) != 0) fingerSpotStartupEnables.tamperDetectEnable = 1;
            else fingerSpotStartupEnables.tamperDetectEnable = 0;
            if ((iValue & 0x800) != 0) fingerSpotStartupEnables.tamperDisconnectPolarity = 1;
            else fingerSpotStartupEnables.tamperDisconnectPolarity = 0;
            appendToLog("TagAxzonOpus.getFingerSpotStartupEnables 3 with fingerSpotStartEnable = " + fingerSpotStartupEnables.fingerSpotStartEnable +
                    ", tamperDetectEnable = " + fingerSpotStartupEnables.tamperDetectEnable +
                    ", tamperDisconnectPolarity = " + fingerSpotStartupEnables.tamperDisconnectPolarity);
        }
        return fingerSpotStartupEnables;
    }
    public void setFingerSpotStartupEnables(FingerSpotStartupEnables fingerSpotStartupEnables) {
        appendToLog("TagAxzonOpus.setFingerSpotStartupEnables with input fingerSpotStartEnable = " + fingerSpotStartupEnables.fingerSpotStartEnable +
                ", tamperDetectEnable = " + fingerSpotStartupEnables.tamperDetectEnable +
                ", tamperDisconnectPolarity = " + fingerSpotStartupEnables.tamperDisconnectPolarity);

        int iOffset = 8;
        appendToLog("TagAxzonOpus.setFingerSpotStartupEnables 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
        int iValue = Integer.parseInt(tagBanks.stringsTid[iOffset], 16);
        iValue &= ~0xC08;
        if (fingerSpotStartupEnables.fingerSpotStartEnable > 0) iValue |= 0x08;
        if (fingerSpotStartupEnables.tamperDetectEnable > 0) iValue |= 0x400;
        if (fingerSpotStartupEnables.tamperDisconnectPolarity > 0) iValue |= 0x800;
        String string = String.format("%04X", iValue);
        appendToLog("TagAxzonOpus.setFingerSpotStartupEnables with string = " + string);

        tagBanks.setBankDataStart(selectData,2, iOffset, 1, string);
        tagBanks.stringsTid[iOffset] = string;
        this.fingerSpotStartupEnables = fingerSpotStartupEnables;
    }
    int alarmUpperLimitx16 = iNO_SUCH_SETTING;
    public int getAlarmUpperLimitx16() {
        appendToLog("TagAxzonOpus.getAlarmUpperLimitx16 1");
        if (alarmUpperLimitx16 == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmUpperLimitx16 2");
            int iOffset = 9;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }

            appendToLog("TagAxzonOpus.getAlarmUpperLimitx16 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getAlarmUpperLimitx16 3 with iValue as " + iValue);

            alarmUpperLimitx16 = iValue & 0xFFF;
            appendToLog("TagAxzonOpus.getAlarmUpperLimitx16 3 with alarmUpperLimitx16 as " + alarmUpperLimitx16);
        }
        return alarmUpperLimitx16;
    }
    public float getAlarmUpperLimit() {
        appendToLog("TagAxzonOpus.getAlarmUpperLimit 1");
        int iValue = getAlarmUpperLimitx16();
        appendToLog("TagAxzonOpus.getAlarmUpperLimit 2 with alarmUpperLimitx16 as " + iValue);
        if (iValue == iNO_SUCH_SETTING) return fNO_SUCH_SETTING;

        //iValue = 0xF08;
        iValue &= 0xFFF;
        if ((iValue & 0x800) != 0) {
            iValue |= 0xFFFFF000;
        }
        appendToLog("TagAxzonOpus.getAlarmUpperLimit 3 with alarmUpperLimitx16 as " + iValue);
        float fValue = iValue;
        fValue /= 16;
        appendToLog("TagAxzonOpus.getAlarmUpperLimit 3 with fValue as " + fValue);
        return fValue;
    }
    public void setAlarmUpperLimit(float fValue) {
        int iOffset = 9;
        String string = tagBanks.stringsTid[iOffset];
        appendToLog("TagAxzonOpus.setAlarmUpperLimit with input fValue = " + fValue + ", original string = " + string + ", iTid9_Backup = " + String.format("%04X", iTid9_backup));

        //fValue = (float)-15.5;
        fValue *= 16;
        int iValue = (int)fValue;
        appendToLog("TagAxzonOpus.setAlarmUpperLimit with iValue 1 = " + iValue);
        iValue &= 0xFFF;
        appendToLog("TagAxzonOpus.setAlarmUpperLimit with iValue 3 = " + iValue);

        int iValueN = (string == null ? iTid9_backup : Integer.parseInt(string, 16));
        iValueN &= ~0xFFF;
        iValueN |= iValue;
        string = String.format("%04X", iValueN);
        appendToLog("TagAxzonOpus.setAlarmUpperLimit with string = " + string);

        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        alarmUpperLimitx16 = iValueN;
    }
    int alarmLowerLimitx16 = iNO_SUCH_SETTING;
    public int getAlarmLowerLimitx16() {
        appendToLog("TagAxzonOpus.getAlarmLowerLimitx16 1");
        if (alarmLowerLimitx16 == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmLowerLimitx16 2");
            int iOffset = 10;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            }

            appendToLog("TagAxzonOpus.getAlarmLowerLimitx16 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getAlarmLowerLimitx16 3 with iValue as " + iValue);

            alarmLowerLimitx16 = iValue & 0xFFF;
            appendToLog("TagAxzonOpus.getAlarmLowerLimitx16 3 with alarmLowerLimitx16 as " + iValue);
        }
        return alarmLowerLimitx16;
    }
    public float getAlarmLowerLimit() {
        appendToLog("TagAxzonOpus.getAlarmLowerLimit 1");
        int iValue = getAlarmLowerLimitx16();
        appendToLog("TagAxzonOpus.getAlarmLowerLimit 2 with alarmLowerLimitx16 as " + iValue);
        if (iValue == iNO_SUCH_SETTING) return fNO_SUCH_SETTING;

        //iValue = 0xF08;
        iValue &= 0xFFF;
        if ((iValue & 0x800) != 0) {
            iValue |= 0xFFFFF000;
        }
        appendToLog("TagAxzonOpus.getAlarmLowerLimit 3 with alarmUpperLimitx16 as " + iValue);
        float fValue = iValue;
        fValue /= 16;
        appendToLog("TagAxzonOpus.getAlarmLowerLimit 3 with fValue as " + fValue);
        return fValue;
    }
    public void setAlarmLowerLimit(float fValue) {
        int iOffset = 10;
        String string = tagBanks.stringsTid[iOffset];
        appendToLog("TagAxzonOpus.setAlarmLowerLimit with input fValue = " + fValue + ", original string = " + string + ", iTidA_Backup = " + String.format("%04X", iTidA_backup));

        //fValue = (float)-15.5;
        fValue *= 16;
        int iValue = (int)fValue;
        appendToLog("TagAxzonOpus.setAlarmLowerLimit with iValue 1 = " + iValue);
        iValue &= 0xFFF;
        appendToLog("TagAxzonOpus.setAlarmLowerLimit with iValue 3 = " + iValue);

        int iValueN = (string == null ? iTidA_backup : Integer.parseInt(string, 16));
        iValueN &= ~0xFFF;
        iValueN |= iValue;
        string = String.format("%04X", iValueN);
        appendToLog("TagAxzonOpus.setAlarmLowerLimit with string = " + string);

        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        alarmLowerLimitx16 = iValueN;
    }
    int samplingRegimePeriod = iNO_SUCH_SETTING;
    public int getSamplingRegimePeriod() {
        appendToLog("TagAxzonOpus.getSamplingRegimePeriod 1");
        if (samplingRegimePeriod == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getSamplingRegimePeriod 2");
            int iOffset = 0xF;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length < (iOffset + 1) || tagBanks.stringsTid[iOffset] == null ) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getSamplingRegimePeriod 3 with string as " + tagBanks.stringsTid[iOffset]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getSamplingRegimePeriod 3 with iValue as " + iValue);

                samplingRegimePeriod = iValue;
                appendToLog("TagAxzonOpus.getSamplingRegimePeriod 3 with samplingRegimePeriod as " + samplingRegimePeriod);
            }
        }
        appendToLog("TagAxzonOpus.getSamplingRegimePeriod 4: setTidAlarmType, samplingRegimePeriod = " + samplingRegimePeriod);
        return samplingRegimePeriod;
    }
    public void setSamplingRegimePeriod(int iValue) {
        appendToLog("TagAxzonOpus.setSamplingRegimePeriod with input iValue = " + iValue);

        String string = String.format("%04X", iValue);
        appendToLog("TagAxzonOpus.setSamplingRegimePeriod with string = " + string);

        int iOffset = 0x0F;
        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        samplingRegimePeriod = iValue;
    }
    int alarmUpperDelay = iNO_SUCH_SETTING;
    public int getAlarmUpperDelay() {
        appendToLog("TagAxzonOpus.getAlarmUpperDelay 1");
        if (alarmUpperDelay == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmUpperDelay 2 with "
                    + (tagBanks.stringsTid == null ? "tagBanks.stringsTid = null" :
                    ("tagBanks.stringsTid.length = " + tagBanks.stringsTid.length + ", tagBanks.stringsTid[0x10] = " + (tagBanks.stringsTid[0x10] == null ? "null" : "valid"))));
            int iOffset = 0x10;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length < (iOffset + 1) || tagBanks.stringsTid[iOffset] == null ) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getAlarmUpperDelay 3 with string as " + tagBanks.stringsTid[iOffset]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getAlarmUpperDelay 3 with iValue as " + iValue);

                iValue = iValue >> 7;
                alarmUpperDelay = (iValue & 0x7) + 1;
                appendToLog("TagAxzonOpus.getAlarmLowerDelay 3 with alarmUpperDelay as " + alarmUpperDelay);
            }
        }
        appendToLog("TagAxzonOpus.getAlarmUpperDelay 4: setTidAlarmType, alarmUpperDelay = " + alarmUpperDelay);
        return alarmUpperDelay;
    }
    public void setAlarmUpperDelay(int iValue) {
        appendToLog("TagAxzonOpus.setAlarmUpperDelay with input iValue = " + iValue);

        iValue--;
        if (iValue < 0) iValue = 0;
        else if (iValue > 7) iValue = 7;

        int iOffset = 0x10;
        int iValue1 = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
        appendToLog("TagAxzonOpus.setAlarmUpperDelay 1 with iValue1 as " + String.format("%04X", iValue1));
        iValue1 &= ~0x380;
        iValue1 |= (iValue << 7);
        appendToLog("TagAxzonOpus.setAlarmUpperDelay 2 with iValue1 as " + String.format("%04X", iValue1));

        String string = String.format("%04X", iValue1);
        appendToLog("TagAxzonOpus.setAlarmUpperDelay with string = " + string);

        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        alarmUpperDelay = iValue + 1;
    }
    int alarmLowerDelay = iNO_SUCH_SETTING;
    public int getAlarmLowerDelay() {
        appendToLog("TagAxzonOpus.getAlarmLowerDelay 1");
        if (alarmLowerDelay == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getAlarmLowerDelay 2");
            int iOffset = 0x10;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length < (iOffset + 1) || tagBanks.stringsTid[iOffset] == null ) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getAlarmLowerDelay 3 with string as " + tagBanks.stringsTid[iOffset]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getAlarmLowerDelay 3 with iValue as " + iValue);

                iValue = iValue >> 4;
                alarmLowerDelay = (iValue & 0x7) + 1;
                appendToLog("TagAxzonOpus.getAlarmLowerDelay 3 with alarmLowerDelay as " + alarmLowerDelay);
            }
        }
        appendToLog("TagAxzonOpus.getAlarmLowerDelay 4: setTidAlarmType, alarmLowerDelay = " + alarmLowerDelay);
        return alarmLowerDelay;
    }
    public void setAlarmLowerDelay(int iValue) {
        appendToLog("TagAxzonOpus.setAlarmLowerDelay with input iValue = " + iValue);

        iValue--;
        if (iValue < 0) iValue = 0;
        else if (iValue > 7) iValue = 7;

        int iOffset = 0x10;
        int iValue1 = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
        appendToLog("TagAxzonOpus.setAlarmLowerDelay 1 with iValue1 as " + String.format("%04X", iValue1));
        iValue1 &= ~0x70;
        iValue1 |= (iValue << 4);
        appendToLog("TagAxzonOpus.setAlarmLowerDelay 2 with iValue1 as " + String.format("%04X", iValue1));

        String string = String.format("%04X", iValue1);
        appendToLog("TagAxzonOpus.setAlarmLowerDelay with string = " + string);

        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        alarmLowerDelay = iValue + 1;
    }
    int delayedLoggingStart = iNO_SUCH_SETTING;
    public int getDelayedLoggingStart() {
        appendToLog("TagAxzonOpus.getDelayedLoggingStart 1");
        if (delayedLoggingStart == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getDelayedLoggingStart 2");
            int iOffset = 0x10;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length < (iOffset + 1) || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getDelayedLoggingStart 3 with string as " + tagBanks.stringsTid[iOffset]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getDelayedLoggingStart 3 with iValue as " + iValue);

                iValue = iValue >> 10;
                delayedLoggingStart = (iValue & 0x7);
                appendToLog("TagAxzonOpus.getDelayedLoggingStart 3 with iValue as " + iValue);
            }
        }
        appendToLog("TagAxzonOpus.getDelayedLoggingStart 4: setTidAlarmType, delayLoggingStart = " + delayedLoggingStart);
        return delayedLoggingStart;
    }
    public void setDelayedLoggingStart(int iValue) {
        appendToLog("TagAxzonOpus.setDelayedLoggingStart with input iValue = " + iValue);

        if (iValue < 0) iValue = 0;
        else if (iValue > 7) iValue = 7;

        int iOffset = 0x10;
        int iValue1 = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
        appendToLog("TagAxzonOpus.setDelayedLoggingStart 1 with iValue1 as " + String.format("%04X", iValue1));
        iValue1 &= ~0x1C00;
        iValue1 |= (iValue << 10);
        appendToLog("TagAxzonOpus.setDelayedLoggingStart 2 with iValue1 as " + String.format("%04X", iValue1));

        String string = String.format("%04X", iValue1);
        appendToLog("TagAxzonOpus.setDelayedLoggingStart with string = " + string);

        tagBanks.setBankDataStart(selectData, 2, iOffset , 1, string);
        delayedLoggingStart = iValue;
    }
    int loggerArmedSecond = iNO_SUCH_SETTING;
    public int getLoggerArmedSecond() {
        appendToLog("TagAxzonOpus.getLoggerArmedSecond 1");
        if (loggerArmedSecond == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getLoggerArmedSecond 2");
            int iOffset = 0x11;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= (iOffset + 2) || tagBanks.stringsTid[iOffset] == null || tagBanks.stringsTid[iOffset+1] == null) {
                setTidBankData8ReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getLoggerArmedSecond 3 with string as " + tagBanks.stringsTid[iOffset] + ", " + tagBanks.stringsTid[iOffset+1]);
                int iValueM = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                int iValueL = Integer.valueOf(tagBanks.stringsTid[iOffset+1], 16);
                appendToLog("TagAxzonOpus.getLoggerArmedSecond 3 with iValue as " + iValueM + ", " + iValueL);
                loggerArmedSecond = ((iValueM & 0xFF) << 16) + (iValueL & 0xFFFF);
                appendToLog("TagAxzonOpus.getLoggerArmedSecond 3 with iValue as " + loggerArmedSecond);
                tagBanks.stringsTid[iOffset] = null;
            }
        }
        return loggerArmedSecond;
    }


    int minBattery4Arming = iNO_SUCH_SETTING;
    public int getMinBattery4Arming() {
        appendToLog("TagAxzonOpus.getMinBattery4Arming 1");
        if (minBattery4Arming == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getMinBattery4Arming 2");
            int iOffset = 0x1d;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData1AReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getMinBattery4Arming 3 with string as " + tagBanks.stringsTid[0x10]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getMinBattery4Arming 3 with iValue as " + iValue);
                minBattery4Arming = iValue;
                appendToLog("TagAxzonOpus.getMinBattery4Arming 3 with iValue as " + iValue);
            }
        }
        return minBattery4Arming;
    }
    int minBattery4Logging = iNO_SUCH_SETTING;
    public int getMinBattery4Logging() {
        appendToLog("TagAxzonOpus.getMinBattery4Logging 1");
        if (minBattery4Logging == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getMinBattery4Logging 2");
            int iOffset = 0x1c;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData1AReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getMinBattery4Logging 3 with string as " + tagBanks.stringsTid[0x10]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getMinBattery4Logging 3 with iValue as " + iValue);
                minBattery4Logging = iValue;
                appendToLog("TagAxzonOpus.getMinBattery4Logging 3 with iValue as " + iValue);
            }
        }
        return minBattery4Logging;
    }
    public enum SampleNumberToLogTypes {
        SIZE_512, SIZE_1024, SIZE_1536, SIZE_2048, SIZE_2560, SIZE_3072, SIZE_3584, SIZE_4096
    }
    SampleNumberToLogTypes sampleNumberToLogType = null;
    public SampleNumberToLogTypes getSampleNumberToLog() {
        appendToLog("TagAxzonOpus.getSampleNumberToLogType 1");
        if (sampleNumberToLogType == null) {
            appendToLog("TagAxzonOpus.getSampleNumberToLogType 2");
            int iOffset = 0x1A;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData8ReadStart();
                return null;
            }
            appendToLog("TagAxzonOpus.getSampleNumberToLogType 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            sampleNumberToLogType = null;
            int iValue = Integer.parseInt(tagBanks.stringsTid[iOffset], 16) & 0x100;
            //sampleNumberToLogType = SampleNumberToLogTypes.values()[iValue];
        }
        return sampleNumberToLogType;
    }
    DisableEnableTypes fingerSpotLedType = null;
    public DisableEnableTypes getFingerSpotLedType() {
        appendToLog("TagAxzonOpus.getFingerSpotLedType 1");
        if (fingerSpotLedType == null) {
            appendToLog("TagAxzonOpus.getFingerSpotLedType 2");
            int iOffset = 0x1E;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData1AReadStart();
                return null;
            }
            appendToLog("TagAxzonOpus.getFingerSpotLedType 3 with stringsTid[" + iOffset + "] = " + tagBanks.stringsTid[iOffset]);
            fingerSpotLedType = null;
            int iValue = Integer.parseInt(tagBanks.stringsTid[iOffset], 16);
            appendToLog("TagAxzonOpus.getFingerSpotLedType 3 with iValue = " + iValue);
            iValue &= 0x08;
            if (iValue == 0) fingerSpotLedType = DisableEnableTypes.DISABLE;
            else fingerSpotLedType = DisableEnableTypes.ENABLE;
            appendToLog("TagAxzonOpus.getFingerSpotLedType 3 with fingerSpotLedType = " + fingerSpotLedType.toString());
        }
        return fingerSpotLedType;
    }
    int bapDuration = iNO_SUCH_SETTING;
    public int getBAPduration() {
        appendToLog("TagAxzonOpus.getBAPduration 1");
        if (bapDuration == iNO_SUCH_SETTING) {
            appendToLog("TagAxzonOpus.getBAPduration 2");
            int iOffset = 0x1F;
            if (tagBanks.stringsTid == null || tagBanks.stringsTid.length <= iOffset || tagBanks.stringsTid[iOffset] == null) {
                setTidBankData1AReadStart();
                return iNO_SUCH_SETTING;
            } else {
                appendToLog("TagAxzonOpus.getBAPduration 3 with string as " + tagBanks.stringsTid[iOffset]);
                int iValue = Integer.valueOf(tagBanks.stringsTid[iOffset], 16);
                appendToLog("TagAxzonOpus.getBAPduration 3 with iValue as " + iValue);
                iValue &= 0x1F;
                bapDuration = iValue;
                appendToLog("TagAxzonOpus.getBAPduration 3 with iValue as " + iValue);
            }
        }
        return bapDuration;
    }
    void setTidBankData8ReadStart() {
        tagBanks.setBankDataStart(selectData, 2, 8, 9+2, null);
    }
    void setTidBankData1AReadStart() {
        tagBanks.setBankDataStart(selectData, 2, 0x1A, 6, null);
    }
    void appendToLog(String string) {
        Log.i(TAG, string);
    }
}
