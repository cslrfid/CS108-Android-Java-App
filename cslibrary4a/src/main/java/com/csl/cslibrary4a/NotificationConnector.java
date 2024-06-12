package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class NotificationConnector {
    boolean DEBUG_PKDATA;
    boolean userDebugEnableDefault = false, userDebugEnable = userDebugEnableDefault;
    public int mVoltageValue;
    public int mVoltageCount;
    public boolean triggerButtonStatus; public int iTriggerCount;
    public enum NotificationPayloadEvents {
        NOTIFICATION_GET_BATTERY_VOLTAGE, NOTIFICATION_GET_TRIGGER_STATUS,
        NOTIFICATION_AUTO_BATTERY_VOLTAGE, NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE,
        NOTIFICATION_AUTO_RFIDINV_ABORT, NOTIFICATION_GET_AUTO_RFIDINV_ABORT,
        NOTIFICATION_AUTO_BARINV_STARTSTOP, NOTIFICATION_GET_AUTO_BARINV_STARTSTOP,
        NOTIFICATION_AUTO_TRIGGER_REPORT, NOTIFICATION_STOP_TRIGGER_REPORT,

        NOTIFICATION_BATTERY_FAILED, NOTIFICATION_BATTERY_ERROR,
        NOTIFICATION_TRIGGER_PUSHED, NOTIFICATION_TRIGGER_RELEASED
    }
    public class CsReaderNotificationData {
        public NotificationPayloadEvents notificationPayloadEvent;
        public byte[] dataValues;
    }

    public interface NotificationListener { void onChange(); }

        NotificationListener listener;
        public void setNotificationListener0(NotificationListener listener) { this.listener = listener; }

        //NotificationListener getListener() { return listener; }
        boolean mTriggerStatus;
        public boolean getTriggerStatus() { return mTriggerStatus; }
        void setTriggerStatus(boolean mTriggerStatus) {
        if (this.mTriggerStatus != mTriggerStatus) {
            this.mTriggerStatus = mTriggerStatus;
            if (listener != null) listener.onChange();
        }
    }

        boolean mAutoRfidAbortStatus = true, mAutoRfidAbortStatusUpdate = false;
        boolean getAutoRfidAbortStatus() {
            if (true || mAutoRfidAbortStatusUpdate == false) {
                CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
                csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT;
                notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
                if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_AUTO_RFIDINV_ABORT to mNotificationToWrite with length = " + notificationToWrite.size());
            }
            return mAutoRfidAbortStatus;
        }
        void setAutoRfidAbortStatus(boolean mAutoRfidAbortStatus) { this.mAutoRfidAbortStatus = mAutoRfidAbortStatus; mAutoRfidAbortStatusUpdate = true; }

        boolean mAutoBarStartStopStatus = false, mAutoBarStartStopStatusUpdated = false;
        public boolean getAutoBarStartStopStatus() {
            if (mAutoBarStartStopStatusUpdated == false) {
                CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
                csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP;
                notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
                if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_AUTO_BARINV_STARTSTOP to mNotificationToWrite with length = " + notificationToWrite.size());
            }
            return mAutoBarStartStopStatus;
        }
        void setAutoBarStartStopStatus(boolean mAutoBarStartStopStatus) { this.mAutoBarStartStopStatus = mAutoBarStartStopStatus; mAutoBarStartStopStatusUpdated = true; }

        public ArrayList<CsReaderNotificationData> notificationToWrite = new ArrayList<>();
        public ArrayList<CsReaderNotificationData> notificationToRead = new ArrayList<>();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, NotificationPayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case NOTIFICATION_GET_BATTERY_VOLTAGE:
                    validEvent = true;
                    break;
                case NOTIFICATION_GET_TRIGGER_STATUS:
                    dataBuf[pos] = 1;
                    validEvent = true;
                    break;
                case NOTIFICATION_AUTO_BATTERY_VOLTAGE:
                    //if (false && checkHostProcessorVersion(controllerConnector.getVersion(), 0, 0, 2) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 2;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE:
                    //if (false && checkHostProcessorVersion(controllerConnector.getVersion(), 0, 0, 1) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 3;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_AUTO_RFIDINV_ABORT:
                    //if (false && checkHostProcessorVersion(bluetoothConnector.getBluetoothIcVersion(), 0, 0, 13) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 4;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_GET_AUTO_RFIDINV_ABORT:
                    //if (false && checkHostProcessorVersion(bluetoothConnector.getBluetoothIcVersion(), 1, 0, 13) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 5;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_AUTO_BARINV_STARTSTOP:
                    //if (false && checkHostProcessorVersion(bluetoothConnector.getBluetoothIcVersion(), 1, 0, 14) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 6;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_GET_AUTO_BARINV_STARTSTOP:
                    //if (false && checkHostProcessorVersion(bluetoothConnector.getBluetoothIcVersion(), 1, 0, 14) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 7;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_AUTO_TRIGGER_REPORT:
                    //if (false && checkHostProcessorVersion(controllerConnector.getVersion(), 1, 0, 16) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 8;
                        validEvent = true;
                    //}
                    break;
                case NOTIFICATION_STOP_TRIGGER_REPORT:
                    //if (false && checkHostProcessorVersion(controllerConnector.getVersion(), 1, 0, 16) == false) {
                    //    validEvent = false;
                    //} else {
                        dataBuf[pos] = 9;
                        validEvent = true;
                    //}
                    break;
            }
            return validEvent;
        }

        private byte[] writeNotification(CsReaderNotificationData data) {
            int datalength = 0; boolean DEBUG = false;
            if (data.dataValues != null)    datalength = data.dataValues.length;
            byte[] dataOutRef = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xD9, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xA0, 0};
            byte[] dataOut = new byte[10 + datalength];
            if (DEBUG) appendToLog("event = " + data.notificationPayloadEvent.toString() + ", with datalength = " + datalength);
            if (datalength != 0) {
                System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
                dataOutRef[2] += datalength;
            }
            System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);
            if (arrayTypeSet(dataOut, 9, data.notificationPayloadEvent)) {
                if (DEBUG_PKDATA) appendToLog(String.format("PkData: write mNotificationDevice.%s.%s with  mNotificationDevice.sendDataToWriteSent = %d", data.notificationPayloadEvent.toString(), byteArrayToString(data.dataValues), sendDataToWriteSent));
                if (DEBUG) appendToLogView("NOut: " + byteArrayToString(dataOut));
                return dataOut;
            }
            return null;
        }
        public boolean isMatchNotificationToWrite(ConnectorData connectorData) {
            boolean match = false;
            if (notificationToWrite.size() != 0 && connectorData.dataValues[0] == (byte)0xA0) {
                byte[] dataInCompare = new byte[]{(byte) 0xA0, 0};
                if (arrayTypeSet(dataInCompare, 1, notificationToWrite.get(0).notificationPayloadEvent) && (connectorData.dataValues.length >= dataInCompare.length + 1)) {
                    if (match = compareArray(connectorData.dataValues, dataInCompare, dataInCompare.length)) {
                        boolean bprocessed = false;
                        byte[] data1 = new byte[connectorData.dataValues.length - 2]; System.arraycopy(connectorData.dataValues, 2, data1, 0, data1.length);
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply with payload = " + byteArrayToString(connectorData.dataValues) + " for writeData Notification." + notificationToWrite.get(0).notificationPayloadEvent.toString());
                        if (notificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE) {
                            if (connectorData.dataValues.length >= dataInCompare.length + 2) {
                                mVoltageValue = (connectorData.dataValues[2] & 0xFF) * 256 + (connectorData.dataValues[3] & 0xFF);
                                mVoltageCount++;
                                bprocessed = true;
                            }
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetBatteryVoltage with result = " + String.format("%X", mVoltageValue));
                        } else if (notificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS) {
                            if (connectorData.dataValues[2] != 0) {
                                setTriggerStatus(true); //mTriggerStatus = true;
                                triggerButtonStatus = true;
                            } else {
                                setTriggerStatus(false); //mTriggerStatus = false;
                                triggerButtonStatus = false;
                            }
                            iTriggerCount++;
                            if (DEBUG_PKDATA) appendToLog("PkData: BARTRIGGER: isMatchNotificationToWrite finds trigger = " + getTriggerStatus());
                            bprocessed = true;
                        } else if (notificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT) {
                            if (connectorData.dataValues[2] != 0) setAutoRfidAbortStatus(true);
                            else setAutoRfidAbortStatus(false);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetAutoRfidinvAbort with result = " + connectorData.dataValues[2] + " and autoRfidAbortStatus = " + getAutoRfidAbortStatus());
                            bprocessed = true;
                        } else if (notificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP) {
                            if (connectorData.dataValues[2] != 0) setAutoBarStartStopStatus(true);
                            else setAutoBarStartStopStatus(false);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetAutoBarinvStartstop with result = " + connectorData.dataValues[2] + " and " + getAutoBarStartStopStatus());
                            bprocessed = true;
                        } else {
                            bprocessed = true;
                        	if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply." + notificationToWrite.get(0).notificationPayloadEvent.toString() + " with result = " + connectorData.dataValues[2]);
                        }
                        String string = "Up31 " + (bprocessed ? "" : "Unprocessed, ") + notificationToWrite.get(0).notificationPayloadEvent.toString() + ", " + byteArrayToString(data1);
                        utility.writeDebug2File(string);
                        notificationToWrite.remove(0); sendDataToWriteSent = 0; appendToLog("notificationToWrite remove0 with length = " + notificationToWrite.size());
                        if (DEBUG_PKDATA) appendToLog("PkData: new mNotificationToWrite size = " + notificationToWrite.size());
                    }
                }
            }
            return match;
        }

        public int sendDataToWriteSent = 0;
        boolean notificationFailure = false;
        public byte[] sendNotificationToWrite() {
            boolean DEBUG = false;
            if (notificationFailure) {
                notificationToWrite.remove(0); sendDataToWriteSent = 0; appendToLog("notificationToWrite remove0 with length = " + notificationToWrite.size());
            } else if (sendDataToWriteSent >= 5) {
                int oldSize = notificationToWrite.size();
                CsReaderNotificationData csReaderNotificationData = notificationToWrite.get(0);
                notificationToWrite.remove(0); sendDataToWriteSent = 0; appendToLog("notificationToWrite remove0 with length = " + notificationToWrite.size());
                if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mNotificationToWrite.size() = " + notificationToWrite.size());
                if (DEBUG) appendToLog("Removed after sending count-out.");
                String string = "Problem in sending data to Notification Module. Removed data sending after count-out";
                if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                else appendToLogView(string);
                if (true) Toast.makeText(context, csReaderNotificationData.notificationPayloadEvent.toString(), Toast.LENGTH_LONG).show();
                notificationFailure = true; // disconnect(false);
            } else {
                sendDataToWriteSent++;
                return writeNotification(notificationToWrite.get(0));
            }
            return null;
        }

        long timeTriggerRelease;
        public boolean isNotificationToRead(ConnectorData connectorData) {
            boolean found = false, DEBUG = false;
            CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
            byte[] data1 = new byte[connectorData.dataValues.length - 2]; System.arraycopy(connectorData.dataValues, 2, data1, 0, data1.length);
            if (connectorData.dataValues[0] == (byte) 0xA0 && connectorData.dataValues[1] == (byte) 0x00 && connectorData.dataValues.length >= 4) {
                mVoltageValue = (connectorData.dataValues[2] & 0xFF) * 256 + (connectorData.dataValues[3] & 0xFF);
                mVoltageCount++;
                csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE;
                csReaderNotificationData.dataValues = data1;
                if (false) notificationToRead.add(csReaderNotificationData);
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(connectorData.dataValues));
                if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.GetCurrentBattteryVoltage is processed as mVoltageValue = " + mVoltageValue + " and mVoltageCount = " + mVoltageCount);
                found = true;
            } else if (connectorData.dataValues[0] == (byte) 0xA0 && connectorData.dataValues[1] == (byte) 0x01 && connectorData.dataValues.length >= 3) {
                if (connectorData.dataValues[2] == 0) triggerButtonStatus = false;
                else triggerButtonStatus = true;
                iTriggerCount++;
                csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS;
                csReaderNotificationData.dataValues = data1;
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(connectorData.dataValues));
                if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.GetCurrentTriggerState is processed as triggerButtonStatus = " + triggerButtonStatus + " and iTriggerCount = " + iTriggerCount);
                found = true;
            } else if (connectorData.dataValues[0] == (byte) 0xA1) {
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(connectorData.dataValues));
                //Cs108NotificatiionData cs108NotificatiionData = new Cs108NotificatiionData();
                switch (connectorData.dataValues[1]) {
                    case 0:
                        if (DEBUG) appendToLog("matched batteryFailed data is found.");
                        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_BATTERY_FAILED;
                        csReaderNotificationData.dataValues = null;
                        if (true) notificationToRead.add(csReaderNotificationData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.Reserve is processed");
                        found = true;
                        break;
                    case 1:
                        if (DEBUG) appendToLog("matched Error data is found, " + byteArrayToString(connectorData.dataValues));
                        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_BATTERY_ERROR;
                        csReaderNotificationData.dataValues = data1;
                        if (true) notificationToRead.add(csReaderNotificationData);
                        //btSendTime = System.currentTimeMillis() - btSendTimeOut + 50;
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.ErrorCode with value = " + byteArrayToString(data1) + " is addded to mNotificationToRead");
                        found = true;
                        break;
                    case 2:
                        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_TRIGGER_PUSHED;
                        csReaderNotificationData.dataValues = data1;
                        setTriggerStatus(true);
                        if (DEBUG) appendToLog("BARTRIGGER: isNotificationToRead finds trigger = " + getTriggerStatus());
                        if (false) notificationToRead.add(csReaderNotificationData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.TriggerPushed is processed as trigger = " + getTriggerStatus());
                        found = true;
                        break;
                    case 3:
                        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_TRIGGER_RELEASED;
                        csReaderNotificationData.dataValues = data1;
                        setTriggerStatus(false);
                        if (DEBUG) appendToLog("BARTRIGGER: isNotificationToRead finds trigger = " + getTriggerStatus());
                        if (false) notificationToRead.add(csReaderNotificationData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.TriggerReleased is processed as trigger = " + getTriggerStatus());
                        found = true;
                        break;
                    default:
                        appendToLog("Notification.Uplink with mis-matched result");
                        break;
                }
            }
            if (DEBUG_PKDATA && found) appendToLog("found Notification.read data = " + byteArrayToString(connectorData.dataValues));
            if (found) utility.writeDebug2File("Up32 " + csReaderNotificationData.notificationPayloadEvent.toString() + ", " + byteArrayToString(csReaderNotificationData.dataValues));
            return found;
        }
    Context context; Utility utility; boolean triggerReporting; short triggerReportingCountSetting;
    public NotificationConnector(Context context, Utility utility, boolean triggerReporting, short triggerReportingCountSetting) {
        this.context = context;
        this.utility = utility; DEBUG_PKDATA = utility.DEBUG_PKDATA;
        this.triggerReporting = triggerReporting;
        this.triggerReportingCountSetting = triggerReportingCountSetting;
    }
    private void appendToLog(String s) { utility.appendToLog(s); }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    private void appendToLogView(String s) { utility.appendToLogView(s); }
    public boolean batteryLevelRequest() {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE;
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_BATTERY_VOLTAGE to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean triggerButtoneStatusRequest() {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS;
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_TRIGGER_STATUS to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean setBatteryAutoReport(boolean on) {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = (on ? NotificationPayloadEvents.NOTIFICATION_AUTO_BATTERY_VOLTAGE: NotificationPayloadEvents.NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE);
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add " + csReaderNotificationData.notificationPayloadEvent.toString() + " to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean setAutoRFIDAbort(boolean enable) {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_AUTO_RFIDINV_ABORT;
        csReaderNotificationData.dataValues = new byte[1];
        setAutoRfidAbortStatus(enable);
        csReaderNotificationData.dataValues[0] = (enable ? (byte)1 : 0);
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_AUTO_RFIDINV_ABORT." + byteArrayToString(csReaderNotificationData.dataValues) + " to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean getAutoRFIDAbort() {
        return getAutoRfidAbortStatus(); }

    public boolean setAutoBarStartSTop(boolean enable) {
        boolean autoBarStartStopStatus = getAutoBarStartSTop();
        if (enable & autoBarStartStopStatus) return true;
        else if (enable == false && autoBarStartStopStatus == false) return true;

        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_AUTO_BARINV_STARTSTOP;
        csReaderNotificationData.dataValues = new byte[1];
        setAutoBarStartStopStatus(enable);
        csReaderNotificationData.dataValues[0] = (enable ? (byte)1 :  0);
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_AUTO_BARINV_STARTSTOP." + byteArrayToString(csReaderNotificationData.dataValues) + " to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean getAutoBarStartSTop() { return getAutoBarStartStopStatus(); }

    public boolean getTriggerReporting() { return triggerReporting; }
    public boolean setTriggerReporting(boolean triggerReporting) {
        boolean bValue = false;
        //if (this.triggerReporting == triggerReporting) return true;
        if (triggerReporting) {
            appendToLog("setAutoTriggerReporting 3");
            bValue = setAutoTriggerReporting((byte) triggerReportingCountSetting);
        } else bValue = stopAutoTriggerReporting();
        if (bValue) this.triggerReporting = triggerReporting;
        return bValue;
    }

    public final int iNO_SUCH_SETTING = 10000;
    //public short triggerReportingCountSettingDefault = 1;
    //public short triggerReportingCountSetting = triggerReportingCountSettingDefault;
    public short getTriggerReportingCount() {
        //if (getcsModel() == 108) bValue = checkHostProcessorVersion(hostProcessorICGetFirmwareVersion(),  1, 0, 16);
        //if (bValue == false) return iNO_SUCH_SETTING; else
            return triggerReportingCountSetting;
    }
    public boolean setTriggerReportingCount(short triggerReportingCount) {
        boolean bValue = false;
        if (triggerReportingCount < 0 || triggerReportingCount > 255) return false;
        if (getTriggerReporting()) {
            if (triggerReportingCountSetting == triggerReportingCount) return true;
            bValue = setAutoTriggerReporting((byte)(triggerReportingCount & 0xFF));
        } else bValue = true;
        if (bValue) triggerReportingCountSetting = triggerReportingCount;
        return true;
    }
    public boolean setAutoTriggerReporting(byte timeSecond) {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_AUTO_TRIGGER_REPORT;
        csReaderNotificationData.dataValues = new byte[1];
        csReaderNotificationData.dataValues[0] = timeSecond;
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_AUTO_TRIGGER_REPORT." + byteArrayToString(csReaderNotificationData.dataValues) + " to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }
    public boolean stopAutoTriggerReporting() {
        CsReaderNotificationData csReaderNotificationData = new CsReaderNotificationData();
        csReaderNotificationData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_STOP_TRIGGER_REPORT;
        boolean bValue = notificationToWrite.add(csReaderNotificationData); appendToLog("notificationToWrite is added with length = " + notificationToWrite.size());
        if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_STOP_TRIGGER_REPORT to mNotificationToWrite with length = " + notificationToWrite.size());
        return bValue;
    }

}
