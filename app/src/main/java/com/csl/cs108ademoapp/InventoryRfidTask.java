package com.csl.cs108ademoapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108library4a.Cs108Connector;
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
    final boolean endingRequest = false;

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
    private ArrayList<Cs108Connector.Rx000pkgData> rx000pkgDataArrary = new ArrayList<Cs108Connector.Rx000pkgData>();
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
            MainActivity.mCs108Library4a.appendToLog("yield = " + yield + ", allTotal = " + allTotal);
        }
        MainActivity.mCs108Library4a.invalidata = 0;
        MainActivity.mCs108Library4a.invalidUpdata = 0;
        MainActivity.mCs108Library4a.validata = 0;

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
                MainActivity.mCs108Library4a.appendToLog("openServer is done");
            } catch (Exception ex) {
                MainActivity.mCs108Library4a.appendToLog("openServer has Exception");
            }
        }
        MainActivity.mCs108Library4a.appendToLog("serverConnectValid = " + serverConnectValid);


        if (MainActivity.mCs108Library4a.getInventoryVibrate() && bUseVibrateMode0 == false && MainActivity.mCs108Library4a.getVibrateModeSetting() == 1 && MainActivity.mCs108Library4a.getAntennaDwell() == 0) bValidVibrateNewAll = true;
        if (bValidVibrateNewAll) MainActivity.mCs108Library4a.setVibrateOn(2);
    }

    @Override
    protected void onPreExecute() {
        inventoryHandler_setup();
    }

    @Override
    protected String doInBackground(Void... a) {
        boolean ending = false;
        Cs108Connector.Rx000pkgData rx000pkgData;
        while (MainActivity.mCs108Library4a.isBleConnected() && isCancelled() == false && ending == false) {
            int batteryCount = MainActivity.mCs108Library4a.getBatteryCount();
            if (batteryCountInventory_old != batteryCount) {
                batteryCountInventory_old = batteryCount;
                publishProgress("VV");
            }
            if (System.currentTimeMillis() > runTimeMillis + 1000) {
                runTimeMillis = System.currentTimeMillis();
                publishProgress("WW");
            }
            rx000pkgData = MainActivity.mCs108Library4a.onRFIDEvent();
            if (rx000pkgData != null && MainActivity.mCs108Library4a.mrfidToWriteSize() == 0) {
                if (rx000pkgData.responseType == null) {
                    publishProgress("null response");
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_INVENTORY) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            else lastTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) {
                    {
                        if (rx000pkgData.decodedError != null)  publishProgress(rx000pkgData.decodedError);
                        else {
                            if (firstTime == 0) firstTime = rx000pkgData.decodedTime;
                            rx000pkgDataArrary.add(rx000pkgData); publishProgress(null, "", "");
                        }
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_ANTENNA_CYCLE_END) {
                    timeMillis = System.currentTimeMillis();
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_COMMAND_END) {
                    if (rx000pkgData.decodedError != null) endingMessaage = rx000pkgData.decodedError;
                    if (continousRequest) {
                        MainActivity.mCs108Library4a.batteryLevelRequest();
                        MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
                    } else  ending = true;
                }
            }
            if (false) {
                if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   timeMillis = System.currentTimeMillis();
            } else {
                //suspend the current thread up to 5 seconds until all the commands on the output buffer got sent out
                long toCnt = System.currentTimeMillis();
                if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    while (System.currentTimeMillis() - toCnt < 50000 && MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask: send commands elapsed time: " + String.format("%d", System.currentTimeMillis() - toCnt));
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
            if (taskCancelReason != TaskCancelRReason.NULL) {
                MainActivity.mCs108Library4a.abortOperation();
                publishProgress("XX");
                if(popRequest) publishProgress("P");
                timeMillis = 0; boolean endStatus = true;
                cancel(true);
            }
        }
        return "End of Asynctask()";
    }

    long firstTimeOld = 0, timeMillisSound = 0; int totalOld = 0;
    @Override
    protected void onProgressUpdate(String... output) {
        if (output[0] != null) {
            if (output[0].length() == 1) inventoryHandler_endReason();
            else if (output[0].length() == 2) {
                if (output[0].contains("XX")) MainActivity.mCs108Library4a.appendToLogView("CANCELLING. sent abortOperation");
                else if (output[0].contains("WW")) inventoryHandler_runtime();
                else if (output[0].contains("VV")) inventoryHandler_voltage();
            } else
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask.InventoryRfidTask.onProgressUpdate(): " + output[0]);
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
        if (rfidVoltageLevel != null) rfidVoltageLevel.setText(MainActivity.mCs108Library4a.getBatteryDisplay(true));
    }
    void inventoryHandler_tag() {
        {
            long currentTime = 0;
            {
                while (rx000pkgDataArrary.size() != 0) {
                    Cs108Connector.Rx000pkgData rx000pkgData = rx000pkgDataArrary.get(0);
                    rx000pkgDataArrary.remove(0);
                    if (rx000pkgData == null) {
                        MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask: null rx000pkgData !!!");
                        continue;
                    };

                    boolean match = false;
                    boolean updated = false;
                    currentTime = rx000pkgData.decodedTime;
                    int iFlag = rx000pkgData.flags;
                    String strPc = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedPc);
                    if (strPc.length() != 4) {
                        MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask: !!! rx000pkgData.Pc length = " + strPc.length());
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
                    String strEpc = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedEpc);
                    if (false) MainActivity.mCs108Library4a.appendToLog("HelloC: decodePc = " + strPc + ", decodedEpc = " + strEpc + ", iFlags = " + String.format("%2X", iFlag));
                    portstatus = INVALID_CODEVALUE; backport1 = INVALID_CODEVALUE; backport2 = INVALID_CODEVALUE; codeSensor = INVALID_CODEVALUE; codeRssi = INVALID_CODEVALUE; codeTempC = INVALID_CODEVALUE; brand = null;
                    String strExtra2 = null; if (rx000pkgData.decodedData2 != null) strExtra2 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedData2);
                    if (strExtra2 != null && strMdid != null) {
                        if (false) MainActivity.mCs108Library4a.appendToLog("HelloK: strExtra2 = " + strExtra2 + ", strMdid = " + strMdid);
                        if (strMdid.contains("E200B0")) portstatus = Integer.parseInt(strExtra2.substring(3, 4), 16);
                    }
                    String strExtra1 = null; if (rx000pkgData.decodedData1 != null) {
                        strExtra1 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedData1);
                        if (strMdid != null && strExtra1 != null && strExtra2 != null) {
                            decodeMicronData(strExtra1, strExtra2);
                        }
                    }
                    String strAddresss = strEpc;
                    String strCrc16 = null; if (rx000pkgData.decodedCrc != null) strCrc16 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedCrc);

                    int extra1Bank = this.extra1Bank;
                    int data1_offset = this.data1_offset;

                    if (strMdid != null) {
                        if (strMdid.indexOf("E203510") == 0) {
                            if (strEpc.length() == 24 && strExtra2 != null) {
                                codeTempC = MainActivity.mCs108Library4a.decodeCtesiusTemperature(strEpc.substring(16, 24), strExtra2);
                                strEpc = strEpc.substring(0, 16); strAddresss = strEpc;
                            }
                        }
                    }

                    boolean bFastId = false; boolean bTempId = false;
                    if (MainActivity.mDid != null) {
                        if (MainActivity.mDid.indexOf("E28011") == 0) {
                            int iValue = Integer.valueOf(MainActivity.mDid.substring("E28011".length()), 16);
                            if ((iValue & 0x20) != 0) bFastId = true;
                            MainActivity.mCs108Library4a.appendToLog("HelloK: iValue = " + String.format("%02X", iValue));
                        }
                    }
                    MainActivity.mCs108Library4a.appendToLog("HelloK: strMdid = " + strMdid + ", MainMdid = " + MainActivity.mDid + ", bFastId = " + bFastId);

                    int iPc = Integer.parseInt(strPc, 16);
                    String strXpc = null; int iSensorData = ReaderDevice.INVALID_SENSORDATA; if ((iPc & 0x0200) != 0) {
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
                                                //MainActivity.mCs108Library4a.appendToLog(String.format("Hello123: iXpcw2 = %04X, iSensorData = %d", iXpcw2, iSensorData ));
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
                            if (strTid.indexOf("E28011") == 0) {
                                strEpc = strEpc1; strAddresss = strEpc;
                                strExtra2 = strTid;
                                extra2Bank = 2;
                                data2_offset = 0;
                                bValidFastId = true;
                            }
                        }
                        if (bValidFastId == false) return;
                        MainActivity.mCs108Library4a.appendToLog("HelloK: Doing IMPINJ Inventory  with strMdid = " + strMdid + ", strEpc1 = " + strEpc1 + ":, strTid = " + strTid);
                    } else if (strMdid != null && MainActivity.mDid != null) {
                        if (MainActivity.mDid.matches("E2806894B")) {
                            if (strEpc.length() >= 24) {
                                String strEpc1 = strEpc.substring(0, strEpc.length() - 24);
                                String strTid = strEpc.substring(strEpc.length() - 24, strEpc.length());
                                if (strExtra1 != null) {
                                    if (strExtra1.length() == 8 && strTid.contains(strExtra1)) {
                                        strEpc = strEpc1; strAddresss = strEpc;
                                        strExtra2 = strTid;
                                        extra2Bank = 2;
                                        data2_offset = 0;
                                    }
                                }
                            }
                        } else if (MainActivity.mDid.matches("E2806894C")) {
                            if (strExtra1 != null && strEpc.length() >= 4) {
                                if (strExtra1.contains("E2806894")) {
                                    String strEpc1 = strEpc.substring(0, strEpc.length() - 4);
                                    String strTid = strEpc.substring(strEpc.length() - 4, strEpc.length());
                                    strEpc = strEpc1; strAddresss = strEpc;
                                    brand = strTid;
                                    MainActivity.mCs108Library4a.appendToLog("HelloK: brand 1 = " + brand + ", strEpc = " + strEpc);
                                }
                            }
                        }
                    }
                    rssi = rx000pkgData.decodedRssi;
                    phase = rx000pkgData.decodedPhase;
                    chidx = rx000pkgData.decodedChidx;
                    port = rx000pkgData.decodedPort;

                    timeMillis = System.currentTimeMillis();

                    double rssiGeiger = rssi;
                    if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0)
                        rssiGeiger -= 106.98;
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
                            MainActivity.mCs108Library4a.appendToLog("HelloK: updated Epc = " + readerDevice.getAddress() + ", brand = " + brand);
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
                            readerDevice.setExtra(strExtra1, extra1Bank, data1_offset, strExtra2, extra2Bank, data2_offset);
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
                            MainActivity.mCs108Library4a.appendToLog("HelloK: New Epc = " + strEpc + ", brand = " + brand);
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
                                    String msgOutput = saveExternalTask.createJSON(null, readerDevice).toString(); MainActivity.mCs108Library4a.appendToLog("Json = " + msgOutput);
                                    saveExternalTask.write2Server(msgOutput);

                                    //                                   saveExternalTask.closeServer();
                                    MainActivity.mCs108Library4a.appendToLog("write2Server is done");
                                } catch (Exception ex) {
                                    MainActivity.mCs108Library4a.appendToLog("write2Server has Exception");
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
            if (++requestSoundCount >= MainActivity.mCs108Library4a.getBeepCount()) {
                requestSoundCount = 0;
                requestSound = true;
            }
            if (requestSound && requestNewSound) requestSoundCount = 0;
            if (readerListAdapter != null) readerListAdapter.notifyDataSetChanged();
            if (invalidDisplay) {
                if (rfidYieldView != null) rfidYieldView.setText(String.valueOf(total) + "," + String.valueOf(MainActivity.mCs108Library4a.validata));
                if (rfidRateView != null) rfidRateView.setText(String.valueOf(MainActivity.mCs108Library4a.invalidata) + "," + String.valueOf(MainActivity.mCs108Library4a.invalidUpdata));
            } else {
                String stringTemp = "Unique:" + String.valueOf(yield);
                if (true) {
                    float fErrorRate = (float) MainActivity.mCs108Library4a.invalidata / ((float) MainActivity.mCs108Library4a.validata + (float) MainActivity.mCs108Library4a.invalidata) * 100;
                    stringTemp += "\nE" + String.valueOf(MainActivity.mCs108Library4a.invalidata) + "/" + String.valueOf(MainActivity.mCs108Library4a.validata) + "/" + String.valueOf((int) fErrorRate);
                } else if (true) {
                    stringTemp += "\nE" + String.valueOf(MainActivity.mCs108Library4a.invalidata) + "," + String.valueOf(MainActivity.mCs108Library4a.invalidUpdata) + "/" + String.valueOf(MainActivity.mCs108Library4a.validata);
                }
                if (rfidYieldView != null) rfidYieldView.setText(stringTemp);
                if (total != 0 && currentTime - firstTimeOld > 500) {
                    if (firstTimeOld == 0) firstTimeOld = firstTime;
                    if (totalOld == 0) totalOld = total;
                    String strRate = "Total:" + String.valueOf(allTotal) + "\n";
                    long tagRate = MainActivity.mCs108Library4a.getTagRate();
                    if (tagRate >= 0) {
                        strRate += "raTe";
                    } else if (lastTime == 0) {
                        tagRate = MainActivity.mCs108Library4a.getStreamInRate() / 17;
                        strRate += "rAte";
                    } else if (currentTime > firstTimeOld) {
                        tagRate = totalOld * 1000 / (currentTime - firstTimeOld);
                        strRate += "Rate";
                    }
                    strRate += ":" + String.valueOf(yieldRate) + "/" + String.valueOf(tagRate);
                    if (rfidRateView != null) rfidRateView.setText(strRate);
                    //if (lastTime - firstTime > 1000) {
                    firstTimeOld = currentTime;
                    totalOld = total;
                    total = 0;
                    //}
                }
            }
            if (false) MainActivity.mCs108Library4a.appendToLogView("playerN = " + (playerN == null ? "Null" : "Valid") + ", playerO = " + (playerO == null ? "Null" : "Valid"));
            if (playerN != null && playerO != null) {
                if (false) MainActivity.mCs108Library4a.appendToLogView("requestSound = " + requestSound + ", bStartBeepWaiting = " + bStartBeepWaiting + ", Op=" + playerO.isPlaying() + ", Np=" + playerN.isPlaying());
                if (requestSound && playerO.isPlaying() == false && playerN.isPlaying() == false) {
                    if (true) {
                        if (bStartBeepWaiting == false) {
                            bStartBeepWaiting = true;
                            if (false) MainActivity.mCs108Library4a.appendToLogView("Going to play old song");
                            handler.postDelayed(runnableStartBeep, 250);
                        }
                        if (MainActivity.mCs108Library4a.getInventoryVibrate()) {
                            boolean validVibrate0 = false, validVibrate = false;
                            if (MainActivity.mCs108Library4a.getVibrateModeSetting() == 0) {
                                if (requestNewVibrate) validVibrate0 = true;
                            } else if (bValidVibrateNewAll == false) validVibrate0 = true;
                            requestNewVibrate = false;

                            if (validVibrate0) {
                                if (bStartVibrateWaiting == false) {
                                    validVibrate = true;
                                } else if (bUseVibrateMode0 == false) {
                                    handler.removeCallbacks(runnableStartVibrate); int timeout = MainActivity.mCs108Library4a.getVibrateWindow() * 1000;
                                    handler.postDelayed(runnableStartVibrate, MainActivity.mCs108Library4a.getVibrateWindow() * 1000);
                                }
                            }

                            if (validVibrate) {
                                if (bUseVibrateMode0) MainActivity.mCs108Library4a.setVibrateOn(1);
                                else MainActivity.mCs108Library4a.setVibrateOn(2);
                                bStartVibrateWaiting = true; int timeout = MainActivity.mCs108Library4a.getVibrateWindow() * 1000;
                                handler.postDelayed(runnableStartVibrate, MainActivity.mCs108Library4a.getVibrateWindow() * 1000);
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
            if (false) MainActivity.mCs108Library4a.appendToLogView("Playing old song");
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
            if (bUseVibrateMode0 == false) MainActivity.mCs108Library4a.setVibrateOn(0);
        }
    };

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask.InventoryRfidTask.onCancelled()");

        DeviceConnectTask4InventoryEnding(taskCancelReason);
    }

    @Override
    protected void onPostExecute(String result) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask.InventoryRfidTask.onPostExecute(): " + result);

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
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("data1_count = " + data1_count + ", data2_count = " + data2_count + ", extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank);

        this.invalidRequest = invalidRequest;

        this.geigerTagRssiView = geigerTagRssiView;
        this.tagsList = tagsList;
        this.readerListAdapter = readerListAdapter;
        this.strMdid = strMdid; MainActivity.mCs108Library4a.appendToLog("HelloK: strMdid = " + strMdid);

        this.rfidRunTime = rfidRunTime;
        this.geigerTagGotView = geigerTagGotView;
        this.rfidVoltageLevel = rfidVoltageLevel;
        this.rfidYieldView = rfidYieldView;
        this.button = button;
        this.rfidRateView = rfidRateView;
        this.beepEnable = beepEnable;

        MainActivity.mCs108Library4a.appendToLogView("going to create playerO and playerN with beepEnable = " + beepEnable);
        if (tagsList != null && readerListAdapter != null && beepEnable) {
            playerO = MainActivity.sharedObjects.playerO;
            playerN = MainActivity.sharedObjects.playerN;
            MainActivity.mCs108Library4a.appendToLogView("playerO and playerN is created");
        }
    }

    boolean popRequest = false; Toast mytoast;
    void DeviceConnectTask4InventoryEnding(TaskCancelRReason taskCancelRReason) {
        MainActivity.mCs108Library4a.abortOperation();  //added in case previous command end is received with inventory stopped
        MainActivity.mCs108Library4a.appendToLog("serverConnectValid = " + serverConnectValid);
        if (serverConnectValid && ALLOW_RTSAVE) {
            try {
                saveExternalTask.closeServer();
                MainActivity.mCs108Library4a.appendToLog("closeServer is done");
            } catch (Exception ex) {
                MainActivity.mCs108Library4a.appendToLog("closeServer has Exception");
            }
        }
        MainActivity.mCs108Library4a.appendToLog("INVENDING: Ending with endingRequest = " + endingRequest);
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
            }
            MainActivity.mCs108Library4a.appendToLog("INVENDING: Toasting");
            if (mytoast != null)    mytoast.show();
        }
        if (button != null) button.setText("Start"); MainActivity.sharedObjects.runningInventoryRfidTask = false;
        if (endingMessaage != null) {
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
            customPopupWindow.popupStart(endingMessaage, false);
        }
        MainActivity.mSensorConnector.mLocationDevice.turnOn(false);
        MainActivity.mSensorConnector.mSensorDevice.turnOn(false);
        MainActivity.mCs108Library4a.setVibrateOn(0);
    }

    String decodeMicronData(String strActData, String strCalData) {
        int iTag35 = -1;
        if (strMdid.contains("E282402")) iTag35 = 2;
        else if (strMdid.contains("E282403")) iTag35 = 3;
        else if (strMdid.contains("E282405")) iTag35 = 5;
        if (iTag35 < 2) return "";

        if (iTag35 == 5) {
            backport1 = Integer.parseInt(strActData.substring(0, 4), 16); backport2 = Integer.parseInt(strActData.substring(4, 8), 16);
            MainActivity.mCs108Library4a.appendToLog("backport1 = " + backport1 + ", backport2 = " + backport2);
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
                codeTempC = MainActivity.mCs108Library4a.decodeMicronTemperature(iTag35, strActData.substring(8, 12), strCalData);
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
                codeTempC = MainActivity.mCs108Library4a.decodeMicronTemperature(iTag35, strActData.substring(8, 12), strCalData);
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