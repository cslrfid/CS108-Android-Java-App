package com.csl.cs108ademoapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class InventoryRfidTask extends AsyncTask<Void, String, String> {
    final boolean DEBUG = false; final boolean ALLOW_WEDGE = true; boolean ALLOW_RTSAVE = false;
    public enum TaskCancelRReason {
        NULL, INVALD_REQUEST, DESTORY, STOP, BUTTON_RELEASE, TIMEOUT, RFID_RESET
    }
    final private boolean bAdd2End = false;
    final boolean endingRequest = true;

    Context context;
    public TaskCancelRReason taskCancelReason;
    private boolean invalidRequest;
    boolean beepEnable;

    private ArrayList<ReaderDevice> tagsList;
    private ReaderListAdapter readerListAdapter;
    private TextView geigerTagRssiView;
    private TextView rfidRunTime, geigerTagGotView, rfidVoltageLevel;
    private TextView rfidYieldView, rfidRateView;
    private Button button;

    CustomMediaPlayer playerO, playerN; int requestSoundCount;

    int extra1Bank = -1, extra2Bank = -1;
    String strMdid;

    final boolean invalidDisplay = false;
    private int total, allTotal;
    private int yield, yield4RateCount, yieldRate;
    double rssi = 0; int phase, chidx, data1_count, data2_count, data1_offset, data2_offset;
    int port = -1; int portstatus; int backport1, backport2, codeSensor, codeRssi; float codeTempC; final int INVALID_CODEVALUE = -500; String brand;
    long timeMillis, startTimeMillis, runTimeMillis;
    long firstTime;
    long lastTime;
    boolean continousRequest = false;
    int batteryCountInventory_old;

    boolean requestSound = false; boolean requestNewSound = false; boolean requestNewVibrate = false; long timeMillisNewVibrate;
    String strEpcOld = "";
    private ArrayList<Cs108Library4A.Rx000pkgData> rx000pkgDataArrary = new ArrayList<Cs108Library4A.Rx000pkgData>();
    private String endingMessaage;

    SaveList2ExternalTask saveExternalTask;
    boolean serverConnectValid = false;
    Handler handler = new Handler(); boolean bValidVibrateNewAll = false; boolean bUseVibrateMode0 = false;

    void inventoryHandler_setup() {
        MainActivity.sharedObjects.runningInventoryRfidTask = true;
        total = 0; allTotal = 0; yield = 0;
        if (tagsList != null) {
            yield = tagsList.size();
            for (int i = 0; i < yield; i++) {
                allTotal += tagsList.get(i).getCount();
            }
            MainActivity.csLibrary4A.appendToLog("yield = " + yield + ", allTotal = " + allTotal);
        }
        MainActivity.csLibrary4A.invalidata = 0;
        MainActivity.csLibrary4A.invalidUpdata = 0;
        MainActivity.csLibrary4A.validata = 0;

        timeMillis = System.currentTimeMillis(); startTimeMillis = System.currentTimeMillis(); runTimeMillis = startTimeMillis;
        firstTime = 0;
        lastTime = 0;

        if (rfidVoltageLevel != null) rfidVoltageLevel.setText("");
        if (rfidYieldView != null) rfidYieldView.setText("");
        if (rfidRateView != null) rfidRateView.setText("");

        taskCancelReason = TaskCancelRReason.NULL;
        if (invalidRequest) {
            cancel(true);
            taskCancelReason = TaskCancelRReason.INVALD_REQUEST;
            Toast.makeText(MainActivity.mContext, "Invalid Request.", Toast.LENGTH_SHORT).show();
        }
        if (button != null) button.setText("Stop");
        MainActivity.mSensorConnector.mLocationDevice.turnOn(true);
        MainActivity.mSensorConnector.mSensorDevice.turnOn(true);
        if (ALLOW_RTSAVE) {
            saveExternalTask = new SaveList2ExternalTask();
            try {
                saveExternalTask.openServer();
                serverConnectValid = true;
                MainActivity.csLibrary4A.appendToLog("openServer is done");
            } catch (Exception ex) {
                MainActivity.csLibrary4A.appendToLog("openServer has Exception");
            }
        }
        MainActivity.csLibrary4A.appendToLog("serverConnectValid = " + serverConnectValid);


        MainActivity.csLibrary4A.appendToLog("getInventoryVibrate = " + MainActivity.csLibrary4A.getInventoryVibrate()
                + ", bUseVibrate0 = " + bUseVibrateMode0
                + ", getVibrateModeSetting = " + MainActivity.csLibrary4A.getVibrateModeSetting()
        );
        if (MainActivity.csLibrary4A.getInventoryVibrate() && bUseVibrateMode0 == false && MainActivity.csLibrary4A.getVibrateModeSetting() == 1) bValidVibrateNewAll = true;
        MainActivity.csLibrary4A.appendToLog("bValidVibrateNewAll = " + bValidVibrateNewAll + ". If true, setVibrate_2");
        if (bValidVibrateNewAll) MainActivity.csLibrary4A.setVibrateOn(2);
    }

    @Override
    protected void onPreExecute() {
        inventoryHandler_setup();
    }

    @Override
    protected String doInBackground(Void... a) {
        boolean ending = false, triggerReleased = false; long triggerReleaseTime = 0;
        Cs108Library4A.Rx000pkgData rx000pkgData = null;
        while (MainActivity.csLibrary4A.onRFIDEvent() != null) { } //clear up possible message before operation
        while (MainActivity.csLibrary4A.isBleConnected() && isCancelled() == false && ending == false && MainActivity.csLibrary4A.isRfidFailure() == false) {
            int batteryCount = MainActivity.csLibrary4A.getBatteryCount();
            if (batteryCountInventory_old != batteryCount) {
                batteryCountInventory_old = batteryCount;
                publishProgress("VV");
            }
            if (System.currentTimeMillis() > runTimeMillis + 1000) {
                runTimeMillis = System.currentTimeMillis();
                publishProgress("WW");
            }
            rx000pkgData = MainActivity.csLibrary4A.onRFIDEvent();
            if (rx000pkgData != null && MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                if (rx000pkgData.responseType == null) {
                    publishProgress("null response");
                } else if (rx000pkgData.responseType == Cs108Library4A.HostCmdResponseTypes.TYPE_18K6C_INVENTORY) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            else lastTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Library4A.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Library4A.HostCmdResponseTypes.TYPE_ANTENNA_CYCLE_END) {
                    timeMillis = System.currentTimeMillis();
                } else if (rx000pkgData.responseType == Cs108Library4A.HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN) {
                    MainActivity.csLibrary4A.appendToLog("AAA: Abort return is received !!!");
                    ending = true;
                } else if (rx000pkgData.responseType == Cs108Library4A.HostCmdResponseTypes.TYPE_COMMAND_END) {
                    if (rx000pkgData.decodedError != null) endingMessaage = rx000pkgData.decodedError;
                    if (continousRequest) {
                        MainActivity.csLibrary4A.batteryLevelRequest();
                        MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
                    } else  ending = true;
                }
            }
            if (false) {
                if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0)   timeMillis = System.currentTimeMillis();
            } else {
                //suspend the current thread up to 5 seconds until all the commands on the output buffer got sent out
                long toCnt = System.currentTimeMillis();
                if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                    while (System.currentTimeMillis() - toCnt < 50000 && MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    MainActivity.csLibrary4A.appendToLog("InventoryRfidTask: send commands elapsed time: " + String.format("%d", System.currentTimeMillis() - toCnt));
                    timeMillis = System.currentTimeMillis();
                }
            }
            if (System.currentTimeMillis() - timeMillis > 10000 && false) { //no tag timeout handling during inventory
                if (true) taskCancelReason = TaskCancelRReason.TIMEOUT;
                else {
                    timeMillisSound = System.currentTimeMillis();
                    requestSound = true;
                }
            }
            if (triggerReleased == false && taskCancelReason == TaskCancelRReason.BUTTON_RELEASE) {
                triggerReleased = true; triggerReleaseTime = System.currentTimeMillis();
                //taskCancelReason = TaskCancelRReason.NULL;
                MainActivity.csLibrary4A.appendToLog("AAA: release is triggered !!!");
            }
            if (taskCancelReason != TaskCancelRReason.NULL) {
                MainActivity.csLibrary4A.abortOperation();
                publishProgress("XX");
                if (popRequest) publishProgress("P");
                timeMillis = 0;
                boolean endStatus = true;
                cancel(true);
            } else if (triggerReleased && (System.currentTimeMillis() > (triggerReleaseTime + 2000))) {
                MainActivity.csLibrary4A.appendToLog("AAA: triggerRelease Timeout !!!");
                taskCancelReason = TaskCancelRReason.BUTTON_RELEASE;
            }
        }
        String stringReturn = "End of Asynctask()";
        if (MainActivity.csLibrary4A.isBleConnected() == false) stringReturn = "isBleConnected is false";
        else if (isCancelled()) stringReturn = "isCancelled is true";
        else if (MainActivity.csLibrary4A.isRfidFailure()) stringReturn = "isRfidFailure is true";
        else if (ending) stringReturn = (rx000pkgData == null ? "null ending" : (rx000pkgData.responseType.toString() + " ending"));
        return stringReturn;
    }

    long firstTimeOld = 0, timeMillisSound = 0; int totalOld = 0;
    @Override
    protected void onProgressUpdate(String... output) {
        if (false) MainActivity.csLibrary4A.appendToLog("InventoryRfidTask: output[0] = " + output[0]);
        if (output[0] != null) {
            if (output[0].length() == 1) inventoryHandler_endReason();
            else if (output[0].length() == 2) {
                if (output[0].contains("XX")) MainActivity.csLibrary4A.appendToLogView("CANCELLING: PostProgressUpdate sent abortOperation");
                else if (output[0].contains("WW")) inventoryHandler_runtime();
                else if (output[0].contains("VV")) inventoryHandler_voltage();
            } else
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidTask.InventoryRfidTask.onProgressUpdate(): " + output[0]);
        } else inventoryHandler_tag();
    }

    void inventoryHandler_endReason() {
        String message;
        switch (taskCancelReason) {
            case STOP:
                message = "Stop button pressed";
                break;
            case BUTTON_RELEASE:
                message = "Trigger Released";
                break;
            case TIMEOUT:
                message = "Time Out";
                break;
            default:
                message = taskCancelReason.name();
                break;
        }
        CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
        customPopupWindow.popupStart(message, false);
    }
    void inventoryHandler_runtime() {
        long timePeriod = (System.currentTimeMillis() - startTimeMillis) / 1000;
        if (timePeriod > 0) {
            if (rfidRunTime != null) rfidRunTime.setText(String.format("Run time: %d sec", timePeriod));
            yieldRate = yield4RateCount; yield4RateCount = 0;
        }
    }
    void inventoryHandler_voltage() {
        if (rfidVoltageLevel != null) rfidVoltageLevel.setText(MainActivity.csLibrary4A.getBatteryDisplay(true));
    }

    boolean bGotTagRate = false;
    void inventoryHandler_tag() {
        boolean DEBUG = false;
        {
            long currentTime = 0;
            {
                while (rx000pkgDataArrary.size() != 0) {
                    Cs108Library4A.Rx000pkgData rx000pkgData = rx000pkgDataArrary.get(0);
                    rx000pkgDataArrary.remove(0);
                    if (rx000pkgData == null) {
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidTask: null rx000pkgData !!!");
                        continue;
                    };

                    boolean match = false;
                    boolean updated = false;
                    currentTime = rx000pkgData.decodedTime;
                    int iFlag = rx000pkgData.flags;
                    String strPc = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData.decodedPc);
                    if (strPc.length() != 4) {
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidTask: !!! rx000pkgData.Pc length = " + strPc.length());
                        continue;
                    }
                    int extraLength = 0;
                    if (extra1Bank != -1 && rx000pkgData.decodedData1 != null) extraLength += rx000pkgData.decodedData1.length;
                    if (extra2Bank != -1 && rx000pkgData.decodedData2 != null) extraLength += rx000pkgData.decodedData2.length;
                    if (extraLength != 0) {
                        byte[] decodedEpcNew = new byte[rx000pkgData.decodedEpc.length - extraLength];
                        System.arraycopy(rx000pkgData.decodedEpc, 0, decodedEpcNew, 0, decodedEpcNew.length);
                        rx000pkgData.decodedEpc = decodedEpcNew;
                    }
                    String strEpc = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData.decodedEpc);
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloC: decodePc = " + strPc + ", decodedEpc = " + strEpc + ", iFlags = " + String.format("%2X", iFlag));
                    portstatus = INVALID_CODEVALUE; backport1 = INVALID_CODEVALUE; backport2 = INVALID_CODEVALUE; codeSensor = INVALID_CODEVALUE; codeRssi = INVALID_CODEVALUE; codeTempC = INVALID_CODEVALUE; brand = null;
                    String strExtra2 = null; if (rx000pkgData.decodedData2 != null) strExtra2 = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData.decodedData2);
                    if (strExtra2 != null && strMdid != null) {
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: strExtra2 = " + strExtra2 + ", strMdid = " + strMdid);
                        if (strMdid.contains("E200B0")) portstatus = Integer.parseInt(strExtra2.substring(3, 4), 16);
                    }
                    String strExtra1 = null; if (rx000pkgData.decodedData1 != null) {
                        strExtra1 = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData.decodedData1);
                        if (strMdid != null && strExtra1 != null && strExtra2 != null) {
                            decodeMicronData(strExtra1, strExtra2);
                        }
                    }
                    String strAddresss = strEpc;
                    String strCrc16 = null; if (rx000pkgData.decodedCrc != null) strCrc16 = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData.decodedCrc);

                    int extra1Bank = this.extra1Bank;
                    int data1_offset = this.data1_offset;

                    if (strMdid != null) {
                        if (strMdid.indexOf("E203510") == 0) {
                            if (strEpc.length() == 24 && strExtra2 != null) {
                                codeTempC = MainActivity.csLibrary4A.decodeCtesiusTemperature(strEpc.substring(16, 24), strExtra2);
                                strEpc = strEpc.substring(0, 16); strAddresss = strEpc;
                            }
                        } else if (strMdid.indexOf("E283A") == 0) {
                            MainActivity.csLibrary4A.appendToLog("E283A is found with extra1Bank = " + extra1Bank + ", strExtra1 = " + strExtra1 + ", extra2Bank = " + extra2Bank + ", strExtra2 = " + strExtra2);
                            if (strExtra2 != null && strExtra2.length() >= 28) codeTempC = MainActivity.csLibrary4A.decodeAsygnTemperature(strExtra2);
                        }
                    }

                    boolean bFastId = false; boolean bTempId = false;
                    if (MainActivity.mDid != null) {
                        MainActivity.csLibrary4A.appendToLog("mDid = " + MainActivity.mDid);
                        if (MainActivity.mDid.indexOf("E28011") == 0) {
                            int iValue = Integer.valueOf(MainActivity.mDid.substring("E28011".length()), 16);
                            MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%02X", iValue));
                            if ((iValue & 0x20) != 0) bFastId = true;
                            if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: iValue = " + String.format("%02X", iValue));
                        }
                    } else if (MainActivity.csLibrary4A.getFastId() > 0) bFastId = true;
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: strMdid = " + strMdid + ", MainMdid = " + MainActivity.mDid + ", bFastId = " + bFastId);

                    int iPc = Integer.parseInt(strPc, 16);
                    String strXpc = null; int iSensorData = ReaderDevice.INVALID_SENSORDATA; if ((iPc & 0x0200) != 0 && strEpc != null && strEpc.length() >= 8) {
                        int iXpcw1 = Integer.parseInt(strEpc.substring(0, 4), 16);
                        if ((iXpcw1 & 0x8000) != 0) {
                            strXpc = strEpc.substring(0, 8);
                            strEpc = strEpc.substring(8); strAddresss = strEpc;
                            if (strMdid != null) {
                                if (strMdid.indexOf("E280B12") == 0) {
                                    int iXpcw2 = Integer.parseInt(strXpc.substring(4, 8), 16);
                                    if ((iXpcw1 & 0x8100) != 0 && (iXpcw2 & 0xF000) == 0) {
                                        if ((iXpcw2 & 0x0C00) == 0x0C00) {
                                            //iXpcw2 |= 0x200;
                                            iSensorData = iXpcw2 & 0x1FF;
                                            if ((iXpcw2 & 0x200) != 0) {
                                                iSensorData ^= 0x1FF; iSensorData++; iSensorData = -iSensorData;
                                                //MainActivity.csLibrary4A.appendToLog(String.format("Hello123: iXpcw2 = %04X, iSensorData = %d", iXpcw2, iSensorData ));
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            strXpc = strEpc.substring(0, 4);
                            strEpc = strEpc.substring(4); strAddresss = strEpc;
                        }
                    }

                    if (bFastId) {
                        String strEpc1 = null, strTid = null;
                        boolean bValidFastId = false;
                        if (strEpc.length() > 24) {
                            strEpc1 = strEpc.substring(0, strEpc.length() - 24);
                            strTid = strEpc.substring(strEpc.length() - 24, strEpc.length());
                            if (strTid.indexOf("E28011") == 0 || strTid.indexOf("E2C011") == 0 ) {
                                strEpc = strEpc1; strAddresss = strEpc;
                                strExtra2 = strTid;
                                extra2Bank = 2;
                                data2_offset = 0;
                                bValidFastId = true;
                            }
                        }
                        if (bValidFastId == false) return;
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: Doing IMPINJ Inventory  with strMdid = " + strMdid + ", strEpc1 = " + strEpc1 + ":, strTid = " + strTid);
                    } else if (MainActivity.mDid != null) {
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: MainActivity.mDid = " + MainActivity.mDid);
                        if (MainActivity.mDid.matches("E2806894B")) {
                            if (strEpc.length() >= 24) {
                                String strEpc1 = strEpc.substring(0, strEpc.length() - 24);
                                String strTid = strEpc.substring(strEpc.length() - 24, strEpc.length());
                                if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: matched E2806894B with strEpc = " + strEpc + ", strEpc1 = " + strEpc1 + ", strTid = " + strTid + ", strExtra1 = " + strExtra1);
                                boolean matched = true;
                                if (strExtra1 != null) {
                                    if (!(strExtra1.length() == 8 && strTid.contains(strExtra1))) matched = false;
                                }
                                if (matched) {
                                    strEpc = strEpc1;
                                    strAddresss = strEpc;
                                    strExtra2 = strTid;
                                    extra2Bank = 2;
                                    data2_offset = 0;
                                }
                                /*if (strTid.contains("E2806894") == false) {
                                    MainActivity.csLibrary4A.appendToLog("HelloK: Skip the record without strExtra1 E2806894: " + strEpc);
                                    return;
                                }*/
                            }
                        } else if (MainActivity.mDid.matches("E2806894C") || MainActivity.mDid.matches("E2806894d")) {
                            if (strEpc.length() >= 4) {
                                String strEpc1 = strEpc.substring(0, strEpc.length() - 4);
                                String strBrand = strEpc.substring(strEpc.length() - 4, strEpc.length());
                                if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: matched E2806894B with strEpc = " + strEpc + ", strEpc1 = " + strEpc1 + ", strBrand = " + strBrand + ", strExtra1 = " + strExtra1);
                                boolean matched = true;
                                if (strExtra1 != null || MainActivity.mDid.matches("E2806894d")) {
                                    if (!(strExtra1 != null && strExtra1.length() == 8 && strExtra1.contains("E2806894"))) {
                                        matched = false;
                                        /*if (MainActivity.mDid.matches("E2806894d")) {
                                            MainActivity.csLibrary4A.appendToLog("HelloK: Skip the record without strExtra1 E2806894: " + strEpc);
                                            return;
                                        }*/
                                    }
                                }
                                if (matched) {
                                    strEpc = strEpc1; strAddresss = strEpc;
                                    brand = strBrand;
                                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: brand 1 = " + brand + ", strEpc = " + strEpc);
                                }
                            }
                        }
                    }

                    if (DEBUG || true) MainActivity.csLibrary4A.appendToLog("strTidCompared = " + strMdid + ", MainActivity.mDid = " + MainActivity.mDid + ", strExtra1 = " + strExtra1 + ", strExtra2 = " + strExtra2);
                    if (strMdid != null) {
                        String strTidCompared = strMdid;
                        if (strTidCompared.indexOf("E28011") == 0) {
                            int iValue = Integer.valueOf(MainActivity.mDid.substring("E28011".length()), 16);
                            MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%02X", iValue));
                            if ((iValue & 0x40) != 0) strTidCompared = "E2C011";
                            else if ((iValue & 0x80) != 0) strTidCompared = "E280117";
                            else strTidCompared = "E28011";
                        }
                        if (strTidCompared.matches("E282402")) { }
                        else if (strTidCompared.matches("E282403")) { }
                        else if (strTidCompared.matches("E282405")) { }
                        else if (strTidCompared.matches("E2806894") && MainActivity.mDid.matches("E2806894C")) { }
                        else { //if (strMdid.matches("E280B0"))
                            boolean bMatched = false;
                            if (strExtra1 != null && strExtra1.indexOf(strTidCompared) == 0) {
                                bMatched = true; if (DEBUG) MainActivity.csLibrary4A.appendToLog("strExtra1 contains strTidCompared");
                            } else if (strExtra2 != null && strExtra2.indexOf(strTidCompared) == 0) {
                                bMatched = true; if (DEBUG) MainActivity.csLibrary4A.appendToLog("strEXTRA2 contains strTidCompared");
                            }
                            if (bMatched == false) return;
                        }
                    }

                    rssi = rx000pkgData.decodedRssi;
                    phase = rx000pkgData.decodedPhase;
                    chidx = rx000pkgData.decodedChidx;
                    port = rx000pkgData.decodedPort;

                    timeMillis = System.currentTimeMillis();

                    double rssiGeiger = rssi;
                    if (MainActivity.csLibrary4A.getRssiDisplaySetting() != 0)
                        rssiGeiger -= MainActivity.csLibrary4A.dBuV_dBm_constant;
                    if (geigerTagRssiView != null)
                        geigerTagRssiView.setText(String.format("%.1f", rssiGeiger));
                    if (geigerTagGotView != null) geigerTagGotView.setText(strEpc);

                    if (tagsList == null) {
                        if (strEpc.matches(strEpcOld)) {
                            match = true;
                            updated = true;
                        }
                    } else if (readerListAdapter.getSelectDupElim()) {
                        ReaderDevice readerDevice = null;
                        int iMatchItem = -1;
                        if (true) {
                            int index = Collections.binarySearch(MainActivity.sharedObjects.tagsIndexList, new SharedObjects.TagsIndex(strAddresss, 0));
                            if (index >= 0) {
                                iMatchItem = MainActivity.sharedObjects.tagsIndexList.size() - 1 - MainActivity.sharedObjects.tagsIndexList.get(index).getPosition();
                            }
                        } else {
                            for (int i = 0; i < tagsList.size(); i++) {
                                if (strEpc.matches(tagsList.get(i).getAddress())) {
                                    iMatchItem = i;
                                    break;
                                }
                            }
                        }
                        if (iMatchItem >= 0) {
                            readerDevice = tagsList.get(iMatchItem);
                            int count = readerDevice.getCount();
                            count++;
                            if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: updated Epc = " + readerDevice.getAddress() + ", brand = " + brand);
                            readerDevice.setCount(count);
                            readerDevice.setXpc(strXpc);
                            readerDevice.setRssi(rssi);
                            readerDevice.setPhase(phase);
                            readerDevice.setChannel(chidx);
                            readerDevice.setPort(port);
                            readerDevice.setStatus(portstatus);
                            readerDevice.setBackport1((backport1));
                            readerDevice.setBackport2(backport2);
                            readerDevice.setCodeSensor(codeSensor);
                            readerDevice.setCodeRssi(codeRssi);
                            readerDevice.setBrand(brand);
                            readerDevice.setCodeTempC(codeTempC);
                            readerDevice.setSensorData(iSensorData);
                            if (strExtra1 != null) readerDevice.setExtra1(strExtra1, extra1Bank, data1_offset);
                            else if (readerDevice.getstrExtra1() != null) {
                                if (DEBUG) MainActivity.csLibrary4A.appendToLog("HelloK: no null replacement of StrExtra1");
                            }
                            if (strExtra2 != null) readerDevice.setExtra2(strExtra2, extra2Bank, data2_offset);
                            else if (readerDevice.getstrExtra2() != null) {
                                MainActivity.csLibrary4A.appendToLog("HelloK: no null replacement of StrExtra2");
                            }
                            tagsList.set(iMatchItem, readerDevice);
                            match = true;
                            updated = true;
                        }
                    }
                    if (ALLOW_WEDGE) MainActivity.sharedObjects.serviceArrayList.add(strEpc);
                    if (match == false) {
                        if (tagsList == null) {
                            strEpcOld = strEpc;
                            updated = true;
                        } else {
                            MainActivity.csLibrary4A.appendToLog("HelloK: New Epc = " + strEpc + ", brand = " + brand);
                            ReaderDevice readerDevice = new ReaderDevice("", strEpc, false, null,
                                    strPc, strXpc, strCrc16, strMdid,
                                    strExtra1, extra1Bank, data1_offset,
                                    strExtra2, extra2Bank, data2_offset,
                                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()), new SimpleDateFormat("z").format(new Date()).replaceAll("GMT", ""),
                                    MainActivity.mSensorConnector.mLocationDevice.getLocation(), MainActivity.mSensorConnector.mSensorDevice.getEcompass(),
                                    1, rssi, phase, chidx, port, portstatus, backport1, backport2, codeSensor, codeRssi, codeTempC, brand, iSensorData);
                            if (strMdid != null) {
                                if (strMdid.indexOf("E282402") == 0) readerDevice.setCodeSensorMax(0x1F);
                                else readerDevice.setCodeSensorMax(0x1FF);
                            }
                            if (bAdd2End) tagsList.add(readerDevice);
                            else tagsList.add(0, readerDevice);
                            SharedObjects.TagsIndex tagsIndex = new SharedObjects.TagsIndex(strAddresss, tagsList.size() - 1); MainActivity.sharedObjects.tagsIndexList.add(tagsIndex); Collections.sort(MainActivity.sharedObjects.tagsIndexList);
                            if (serverConnectValid && ALLOW_RTSAVE && true) {
                                try {
//                                    saveExternalTask = new SaveList2ExternalTask();
//                                    saveExternalTask.openServer();
                                    String msgOutput = saveExternalTask.createJSON(null, readerDevice).toString(); MainActivity.csLibrary4A.appendToLog("Json = " + msgOutput);
                                    saveExternalTask.write2Server(msgOutput);

                                    //                                   saveExternalTask.closeServer();
                                    MainActivity.csLibrary4A.appendToLog("write2Server is done");
                                } catch (Exception ex) {
                                    MainActivity.csLibrary4A.appendToLog("write2Server has Exception");
                                }
                            }
                        }
                        yield++; yield4RateCount++;
                        updated = true;
                        requestNewSound = true; requestNewVibrate = true;
                        requestSound = true;
                    }
                    if (updated) {
                        total++;
                        allTotal++;
                    }
                }
            }
            if (++requestSoundCount >= MainActivity.csLibrary4A.getBeepCount()) {
                requestSoundCount = 0;
                requestSound = true;
            }
            if (requestSound && requestNewSound) requestSoundCount = 0;
            if (readerListAdapter != null) readerListAdapter.notifyDataSetChanged();
            if (invalidDisplay) {
                if (rfidYieldView != null) rfidYieldView.setText(String.valueOf(total) + "," + String.valueOf(MainActivity.csLibrary4A.validata));
                if (rfidRateView != null) rfidRateView.setText(String.valueOf(MainActivity.csLibrary4A.invalidata) + "," + String.valueOf(MainActivity.csLibrary4A.invalidUpdata));
            } else {
                String stringTemp = "Unique:" + String.valueOf(yield);
                if (true) {
                    float fErrorRate = (float) MainActivity.csLibrary4A.invalidata / ((float) MainActivity.csLibrary4A.validata + (float) MainActivity.csLibrary4A.invalidata) * 100;
                    stringTemp += "\nE" + String.valueOf(MainActivity.csLibrary4A.invalidata) + "/" + String.valueOf(MainActivity.csLibrary4A.validata) + "/" + String.valueOf((int) fErrorRate);
                } else if (true) {
                    stringTemp += "\nE" + String.valueOf(MainActivity.csLibrary4A.invalidata) + "," + String.valueOf(MainActivity.csLibrary4A.invalidUpdata) + "/" + String.valueOf(MainActivity.csLibrary4A.validata);
                }
                if (rfidYieldView != null) rfidYieldView.setText(stringTemp);
                if (total != 0 && currentTime - firstTimeOld > 500) {
                    if (firstTimeOld == 0) firstTimeOld = firstTime;
                    if (totalOld == 0) totalOld = total;
                    String strRate = "Total:" + String.valueOf(allTotal) + "\n";

                    if (firstTimeOld != 0) {
                        long tagRate = MainActivity.csLibrary4A.getTagRate();
                        long tagRate2 = -1;
                        if (currentTime > firstTimeOld) tagRate2 = totalOld * 1000 / (currentTime - firstTimeOld);
                        if (tagRate >= 0 || bGotTagRate) {
                            bGotTagRate = true;
                            strRate += String.valueOf(yieldRate) + "/";
                            strRate += (tagRate != -1 ? String.valueOf(tagRate) : "___") + "/" + String.valueOf(tagRate2);
                        } else {
                            if (lastTime == 0) {
                                tagRate = MainActivity.csLibrary4A.getStreamInRate() / 17;
                                strRate += "rAte";
                            } else if (currentTime > firstTimeOld) {
                                tagRate = totalOld * 1000 / (currentTime - firstTimeOld);
                                strRate += "Rate";
                            }
                            strRate += ":" + String.valueOf(yieldRate) + "/" + String.valueOf(tagRate);
                        }
                    }

                    if (rfidRateView != null) rfidRateView.setText(strRate);
                    //if (lastTime - firstTime > 1000) {
                    firstTimeOld = currentTime;
                    totalOld = total;
                    total = 0;
                    //}
                }
            }
            if (false) MainActivity.csLibrary4A.appendToLogView("playerN = " + (playerN == null ? "Null" : "Valid") + ", playerO = " + (playerO == null ? "Null" : "Valid"));
            if (playerN != null && playerO != null) {
                if (false) MainActivity.csLibrary4A.appendToLogView("requestSound = " + requestSound + ", bStartBeepWaiting = " + bStartBeepWaiting + ", Op=" + playerO.isPlaying() + ", Np=" + playerN.isPlaying());
                if (requestSound && playerO.isPlaying() == false && playerN.isPlaying() == false) {
                    if (true) {
                        if (bStartBeepWaiting == false) {
                            bStartBeepWaiting = true;
                            if (false) MainActivity.csLibrary4A.appendToLogView("Going to play old song");
                            handler.postDelayed(runnableStartBeep, 250);
                        }
                        if (MainActivity.csLibrary4A.getInventoryVibrate()) {
                            boolean validVibrate0 = false, validVibrate = false;
                            if (MainActivity.csLibrary4A.getVibrateModeSetting() == 0) {
                                if (requestNewVibrate) validVibrate0 = true;
                            } else if (bValidVibrateNewAll == false) validVibrate0 = true;
                            requestNewVibrate = false;

                            if (validVibrate0) {
                                if (bStartVibrateWaiting == false) {
                                    validVibrate = true;
                                } else if (bUseVibrateMode0 == false) {
                                    handler.removeCallbacks(runnableStartVibrate); int timeout = MainActivity.csLibrary4A.getVibrateWindow() * 1000;
                                    handler.postDelayed(runnableStartVibrate, MainActivity.csLibrary4A.getVibrateWindow() * 1000);
                                }
                            }

                            if (validVibrate) {
                                if (bUseVibrateMode0) MainActivity.csLibrary4A.setVibrateOn(1);
                                else MainActivity.csLibrary4A.setVibrateOn(2);
                                bStartVibrateWaiting = true; int timeout = MainActivity.csLibrary4A.getVibrateWindow() * 1000;
                                handler.postDelayed(runnableStartVibrate, MainActivity.csLibrary4A.getVibrateWindow() * 1000);
                            }
                        }
                    } else {
                        requestSound = false;
                        if (requestNewSound) {
                            requestNewSound = false;
                            playerN.start();
                        } else {
                            playerO.start();
                        }
                    }
                }
            }
        }
    }

    boolean bStartBeepWaiting = false;
    Runnable runnableStartBeep = new Runnable() {
        @Override
        public void run() {
            if (false) MainActivity.csLibrary4A.appendToLogView("Playing old song");
            bStartBeepWaiting = false;
            requestSound = false;
            if (requestNewSound) {
                requestNewSound = false;
                playerN.start(); //playerN.setVolume(300, 300);
            } else {
                playerO.start(); //playerO.setVolume(30, 30);
            }
        }
    };

    boolean bStartVibrateWaiting = false;
    Runnable runnableStartVibrate = new Runnable() {
        @Override
        public void run() {
            bStartVibrateWaiting = false;
            if (bUseVibrateMode0 == false) MainActivity.csLibrary4A.setVibrateOn(0);
        }
    };

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (DEBUG || true) MainActivity.csLibrary4A.appendToLogView("InventoryRfidTask.InventoryRfidTask.onCancelled()");

        DeviceConnectTask4InventoryEnding(taskCancelReason);
    }

    @Override
    protected void onPostExecute(String result) {
        if (DEBUG || true) MainActivity.csLibrary4A.appendToLogView("InventoryRfidTask.InventoryRfidTask.onPostExecute(): " + result);

        DeviceConnectTask4InventoryEnding(taskCancelReason);
    }

    public InventoryRfidTask() { }
    public InventoryRfidTask(Context context, int extra1Bank, int extra2Bank, int data1_count, int data2_count, int data1_offset, int data2_offset,
                             boolean invalidRequest, boolean beepEnable,
                             ArrayList<ReaderDevice> tagsList, ReaderListAdapter readerListAdapter, TextView geigerTagRssiView,
                             String strMdid,
                             TextView rfidRunTime, TextView geigerTagGotView, TextView rfidVoltageLevel,
                             TextView rfidYieldView, Button button, TextView rfidRateView) {
        this.context = context;
        this.extra1Bank = extra1Bank;
        this.extra2Bank = extra2Bank;
        this.data1_count = data1_count;
        this.data2_count = data2_count;
        this.data1_offset = data1_offset;
        this.data2_offset = data2_offset;
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("data1_count = " + data1_count + ", data2_count = " + data2_count + ", extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank);

        this.invalidRequest = invalidRequest;

        this.geigerTagRssiView = geigerTagRssiView;
        this.tagsList = tagsList;
        this.readerListAdapter = readerListAdapter;
        this.strMdid = strMdid; MainActivity.csLibrary4A.appendToLog("HelloK: strMdid = " + strMdid);

        this.rfidRunTime = rfidRunTime;
        this.geigerTagGotView = geigerTagGotView;
        this.rfidVoltageLevel = rfidVoltageLevel;
        this.rfidYieldView = rfidYieldView;
        this.button = button;
        this.rfidRateView = rfidRateView;
        this.beepEnable = beepEnable;

        MainActivity.csLibrary4A.appendToLogView("going to create playerO and playerN with beepEnable = " + beepEnable);
        if (tagsList != null && readerListAdapter != null && beepEnable) {
            playerO = MainActivity.sharedObjects.playerO;
            playerN = MainActivity.sharedObjects.playerN;
            MainActivity.csLibrary4A.appendToLogView("playerO and playerN is created");
        }
    }

    boolean popRequest = false; Toast mytoast;
    void DeviceConnectTask4InventoryEnding(TaskCancelRReason taskCancelReason) {
        MainActivity.csLibrary4A.appendToLogView("CANCELLING: TaskEnding sent abortOperation again with taskCancelReason = " + taskCancelReason.toString());
        MainActivity.csLibrary4A.abortOperation();  //added in case previous command end is received with inventory stopped
        MainActivity.csLibrary4A.appendToLog("serverConnectValid = " + serverConnectValid);
        if (serverConnectValid && ALLOW_RTSAVE) {
            try {
                saveExternalTask.closeServer();
                MainActivity.csLibrary4A.appendToLog("closeServer is done");
            } catch (Exception ex) {
                MainActivity.csLibrary4A.appendToLog("closeServer has Exception");
            }
        }
        MainActivity.csLibrary4A.appendToLog("INVENDING: Ending with endingRequest = " + endingRequest);
        if (MainActivity.mContext == null) return;
        if (readerListAdapter != null) readerListAdapter.notifyDataSetChanged();
        if (mytoast != null)    mytoast.cancel();
        if (endingRequest) {
            switch (taskCancelReason) {
                case NULL:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_END, Toast.LENGTH_SHORT);
                    break;
                case STOP:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_STOP, Toast.LENGTH_SHORT);
                    break;
                case BUTTON_RELEASE:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_BUTTON, Toast.LENGTH_SHORT);
                    break;
                case TIMEOUT:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_TIMEOUT, Toast.LENGTH_SHORT);
                    break;
                case RFID_RESET:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_RFIDRESET, Toast.LENGTH_SHORT);
                    break;
                case INVALD_REQUEST:
                    mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_invalid_sendHostRequest, Toast.LENGTH_SHORT);
                    break;
                default:
                    mytoast = Toast.makeText(MainActivity.mContext, ("Finish reason as " + taskCancelReason.toString()), Toast.LENGTH_SHORT);
                    break;
            }
            MainActivity.csLibrary4A.appendToLog("INVENDING: Toasting");
            if (mytoast != null)    mytoast.show();
        }
        if (button != null) button.setText("Start"); MainActivity.sharedObjects.runningInventoryRfidTask = false;
        if (endingMessaage != null) {
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
            customPopupWindow.popupStart(endingMessaage, false);
        }
        MainActivity.mSensorConnector.mLocationDevice.turnOn(false);
        MainActivity.mSensorConnector.mSensorDevice.turnOn(false);
        MainActivity.csLibrary4A.setVibrateOn(0);
    }

    String decodeMicronData(String strActData, String strCalData) {
        int iTag35 = -1;
        if (strMdid.contains("E282402")) iTag35 = 2;
        else if (strMdid.contains("E282403")) iTag35 = 3;
        else if (strMdid.contains("E282405")) iTag35 = 5;
        if (iTag35 < 2) return "";

        if (iTag35 == 5) {
            backport1 = Integer.parseInt(strActData.substring(0, 4), 16); backport2 = Integer.parseInt(strActData.substring(4, 8), 16);
            MainActivity.csLibrary4A.appendToLog("backport1 = " + backport1 + ", backport2 = " + backport2);
            strActData = strActData.substring(8);
        }
        int iSensorCode = Integer.parseInt(strActData.substring(0,4), 16); iSensorCode &= 0x1FF; if (iTag35 == 2) iSensorCode &= 0x1F; codeSensor = iSensorCode;
        int iRssi;
        String strRetValue = "";
        if (iTag35 == 2) {
            iRssi = Integer.parseInt(strCalData.substring(0,4), 16); iRssi &= 0x1F; codeRssi = iRssi;
        } else if (iTag35 == 3) {
            iRssi = Integer.parseInt(strActData.substring(4,8), 16); iRssi &= 0x1F; codeRssi = iRssi;

            if (true) {
                if (strActData.length() < 8) return null;
                codeTempC = MainActivity.csLibrary4A.decodeMicronTemperature(iTag35, strActData.substring(8, 12), strCalData);
            } else {
                int calCode1, calTemp1, calCode2, calTemp2, calVer = -1;
                if (strCalData == null) return null;
                if (strCalData.length() < 16) return null;
                int crc = Integer.parseInt(strCalData.substring(0, 4), 16);
                calCode1 = Integer.parseInt(strCalData.substring(4, 7), 16);
                calTemp1 = Integer.parseInt(strCalData.substring(7, 10), 16);
                calTemp1 >>= 1;
                calCode2 = Integer.parseInt(strCalData.substring(9, 13), 16);
                calCode2 >>= 1;
                calCode2 &= 0xFFF;
                calTemp2 = Integer.parseInt(strCalData.substring(12, 16), 16);
                calTemp2 >>= 2;
                calTemp2 &= 0x7FF;
                calVer = Integer.parseInt(strCalData.substring(15, 16), 16);
                calVer &= 0x3;

                if (strActData == null) return null;
                if (strActData.length() < 8) return null;

                float fTemperature = Integer.parseInt(strActData.substring(8, 12), 16);
                fTemperature = ((float) calTemp2 - (float) calTemp1) * (fTemperature - (float) calCode1);
                fTemperature /= ((float) (calCode2) - (float) calCode1);
                fTemperature += (float) calTemp1;
                fTemperature -= 800;
                fTemperature /= 10;
                codeTempC = fTemperature;
            }
        } else if (iTag35 == 5) {
            iRssi = Integer.parseInt(strActData.substring(4,8), 16); iRssi &= 0x1F; codeRssi = iRssi;

            if (true) {
                codeTempC = MainActivity.csLibrary4A.decodeMicronTemperature(iTag35, strActData.substring(8, 12), strCalData);
            } else {
                int iTemp;
                float calCode2 = Integer.parseInt(strCalData.substring(0, 4), 16);
                calCode2 /= 16;
                iTemp = Integer.parseInt(strCalData.substring(4, 8), 16);
                iTemp &= 0x7FF;
                float calTemp2 = iTemp;
                calTemp2 -= 600;
                calTemp2 /= 10;
                float calCode1 = Integer.parseInt(strCalData.substring(8, 12), 16);
                calCode1 /= 16;
                iTemp = Integer.parseInt(strCalData.substring(12, 16), 16);
                iTemp &= 0x7FF;
                float calTemp1 = iTemp;
                calTemp1 -= 600;
                calTemp1 /= 10;

                float fTemperature = Integer.parseInt(strActData.substring(8, 12), 16);
                fTemperature -= calCode1;
                fTemperature *= (calTemp2 - calTemp1);
                fTemperature /= (calCode2 - calCode1);
                fTemperature += calTemp1;
                codeTempC = fTemperature;
            }
        }
        return "";
    }
}