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
    String strExtra1Filter, strExtra1Decode;

    final boolean invalidDisplay = false;
    private int total, allTotal;
    private int yield, yield4RateCount, yieldRate;
    double rssi = 0; int phase, chidx, data1_count, data2_count, data1_offset, data2_offset;
    long timeMillis, startTimeMillis, runTimeMillis;
    long firstTime;
    long lastTime;
    boolean continousRequest = false;
    boolean finishingRequest = false; boolean debugEndRequest = false;
    int batteryCountInventory_old;

    boolean requestSound = false; boolean requestNewSound = false; boolean requestNewVibrate = false; long timeMillisNewVibrate;
    long timeMillisSound = 0;
    String strEpcOld = "";
    private ArrayList<byte[]> epcDataArray = new ArrayList<>();
    private ArrayList<Cs108Connector.Rx000pkgData> rx000pkgDataArrary = new ArrayList<Cs108Connector.Rx000pkgData>();
    private String endingMessaage;

    SaveList2ExternalTask saveExternalTask;
    boolean serverConnectValid = false;
    Handler handler = new Handler(); boolean bValidVibrateNewAll = false; boolean bUseVibrateMode0 = false;

    @Override
    protected void onPreExecute() {
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
        MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.invalidUpdata = 0;
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
        MainActivity.mCs108Library4a.appendToLog("serverConnectValid = " + serverConnectValid + ", strExtra1Filter = " + strExtra1Filter);


        if (MainActivity.mCs108Library4a.getInventoryVibrate() && bUseVibrateMode0 == false && MainActivity.mCs108Library4a.getVibrateModeSetting() == 1 && MainActivity.mCs108Library4a.getAntennaDwell() == 0) bValidVibrateNewAll = true;
        if (bValidVibrateNewAll) MainActivity.mCs108Library4a.setVibrateOn(2);
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
            if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   timeMillis = System.currentTimeMillis();
/*                if (System.currentTimeMillis() - timeMillis > 10000) {
                    if (debugEndRequest)    MainActivity.mCs108Library4a.getMacLastCommandDuration(true);
                    taskCancelReason = TaskCancelRReason.TIMEOUT; taskCancelling = true;
                }*/
/*                if (System.currentTimeMillis() - timeMillisSound > 1000) {
                    timeMillisSound = System.currentTimeMillis();
                    requestSound = true;
                }*/
            if (taskCancelReason != TaskCancelRReason.NULL) {
                MainActivity.mCs108Library4a.abortOperation();
                publishProgress("XX");
                if(popRequest)  { popStatus = true; publishProgress("P"); }
                timeMillis = 0; boolean endStatus = true; if (finishingRequest) publishProgress("EEE");
                while (MainActivity.mCs108Library4a.isBleConnected() && ((popRequest && popStatus) || endStatus) && finishingRequest) {
                    if (System.currentTimeMillis() - timeMillis > 2000) {
                        timeMillis = System.currentTimeMillis();
                        //publishProgress("EEE");
                    }
                    rx000pkgData = MainActivity.mCs108Library4a.onRFIDEvent();
                    if (rx000pkgData != null) {
                        if (MainActivity.mCs108Library4a.mrfidToWriteSize() == 0) {
                            MainActivity.mCs108Library4a.abortOperation();
                        }
                    } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() == 0) {
                        endStatus = false;
                    }
                }
                if (taskCancelReason == TaskCancelRReason.TIMEOUT && debugEndRequest) {
                    if (MainActivity.mCs108Library4a.getMacLastCommandDuration(false) == 0)
                        taskCancelReason = TaskCancelRReason.RFID_RESET;
                }
                cancel(true);
            }
        }
        return "End of Asynctask()";
    }

    boolean debugged = false;
    long firstTimeOld = 0; int totalResetCount = 0; int totalOld = 0;
    @Override
    protected void onProgressUpdate(String... output) {
        if (output[0] != null) {
            if (output[0].length() == 1) {
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
            } else if (output[0].length() == 2) {
                if (output[0].contains("XX")) MainActivity.mCs108Library4a.appendToLogView("CANCELLING. sent abortOperation");
                else if (output[0].contains("WW")) {
                    long timePeriod = (System.currentTimeMillis() - startTimeMillis) / 1000;
                    if (timePeriod > 0) {
                        if (rfidRunTime != null) rfidRunTime.setText(String.format("Run time: %d sec", timePeriod));
                        yieldRate = yield4RateCount; yield4RateCount = 0;
                    }
                } else if (taskCancelReason == TaskCancelRReason.NULL) {
                    if (rfidVoltageLevel != null) rfidVoltageLevel.setText(MainActivity.mCs108Library4a.getBatteryDisplay(true));
                }
            } else if (output[0].length() == 3) {
                mytoast = Toast.makeText(MainActivity.mContext, R.string.toast_finishing_tag_data_upload, Toast.LENGTH_LONG);
                mytoast.show();
            } else
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidTask.InventoryRfidTask.onProgressUpdate(): " + output[0]);
        } else {
            long currentTime = 0;
            {
                while (rx000pkgDataArrary.size() != 0) {
                    Cs108Connector.Rx000pkgData rx000pkgData = rx000pkgDataArrary.get(0);
                    rx000pkgDataArrary.remove(0);
                    if (rx000pkgData == null) continue;;

                    boolean match = false;
                    boolean updated = false;
                    currentTime = rx000pkgData.decodedTime;
                    String strPc = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedPc);
                    int extraLength = 0;
                    if (extra1Bank != -1 && rx000pkgData.decodedData1 != null) extraLength += rx000pkgData.decodedData1.length;
                    if (extra2Bank != -1 && rx000pkgData.decodedData2 != null) extraLength += rx000pkgData.decodedData2.length;
                    if (extraLength != 0) {
                        byte[] decodedEpcNew = new byte[rx000pkgData.decodedEpc.length - extraLength];
                        System.arraycopy(rx000pkgData.decodedEpc, 0, decodedEpcNew, 0, decodedEpcNew.length);
                        rx000pkgData.decodedEpc = decodedEpcNew;
                    }
                    String strEpc = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedEpc);
                    String strExtra2 = null; if (rx000pkgData.decodedData2 != null) strExtra2 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedData2);
                    String strExtra1 = null; if (rx000pkgData.decodedData1 != null) {
                        strExtra1 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedData1);
                        if (strExtra1Decode != null && strExtra1 != null && strExtra2 != null) {
                            if (strExtra1Decode.contains("E282403"))
                                strExtra1 += decodeMicronData(strExtra2, strExtra1);
                        }
                    }
                    String strAddresss = strEpc; // strEpc, strEpc + strExtra1 + strExtra2
                    String strCrc16 = null; if (rx000pkgData.decodedCrc != null) strCrc16 = MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedCrc);
                    if (strExtra1 != null && strExtra1Filter != null) {
                        MainActivity.mCs108Library4a.appendToLog("strEpc = " + strEpc + ", strExtra1 = " + strExtra1 + ", strExtra1Filter = " + strExtra1Filter + ", strExtra2 = " + strExtra2 );
                        String stringExtra1Compare = "00" + strExtra1.substring(2, 6);
                        if (false) {
                            int index = strEpc.indexOf(stringExtra1Compare);
                            if (index == -1) {
                                MainActivity.mCs108Library4a.appendToLog("Continue 1");
                                continue;
                            } else strEpc = strEpc.substring(index);
                            MainActivity.mCs108Library4a.appendToLog("Continue 2: index = " + index + ", Extra1compare = " + stringExtra1Compare);
                        } else {
                            if (strExtra1.contains(strExtra1Filter) == false) {
                                MainActivity.mCs108Library4a.appendToLog("Continue 3");
                                continue;
                            }
                            MainActivity.mCs108Library4a.appendToLog("Continue 4");
                        }
                    }
                    rssi = rx000pkgData.decodedRssi;
                    phase = rx000pkgData.decodedPhase;
                    chidx = rx000pkgData.decodedChidx;

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
                    } else {
                        ReaderDevice readerDevice = null;
                        int iMatchItem = -1;
//                        MainActivity.mCs108Library4a.appendToLog("Matching Epc = " + strEpc);
                        iMatchItem = -1;
                        if (true) {
                            int index = Collections.binarySearch(MainActivity.sharedObjects.tagsIndexList, new SharedObjects.TagsIndex(strAddresss, 0));
                            if (index >= 0) {
                                iMatchItem = MainActivity.sharedObjects.tagsIndexList.size() - 1 - MainActivity.sharedObjects.tagsIndexList.get(index).getPosition();
//                                MainActivity.mCs108Library4a.appendToLog("Binary matched index = " + index + ", iMatchItem = " + iMatchItem + ", Epc = " + tagsList.get(iMatchItem).getAddress());
                            }
                        } else {
                            for (int i = 0; i < tagsList.size(); i++) {
                                if (strEpc.matches(tagsList.get(i).getAddress())) {
                                    iMatchItem = i;
//                                    MainActivity.mCs108Library4a.appendToLog("Normal matched position = " + iMatchItem + ", Epc = " + tagsList.get(i).getAddress());
                                    break;
                                }
                            }
                        }
                        if (iMatchItem >= 0) {
                            readerDevice = tagsList.get(iMatchItem);
                            int count = readerDevice.getCount();
                            count++;
                            readerDevice.setCount(count);
                            readerDevice.setRssi(rssi);
                            readerDevice.setPhase(phase);
                            readerDevice.setChannel(chidx);
                            readerDevice.setExtra1(strExtra1, extra1Bank, data1_offset);
                            MainActivity.mCs108Library4a.appendToLog("setExtra1 with strExtra1 = " + strExtra1);
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
                            ReaderDevice readerDevice = new ReaderDevice("", strEpc, false, null,
                                    strPc, strCrc16,
                                    strExtra1, extra1Bank, data1_offset,
                                    strExtra2, extra2Bank, data2_offset,
                                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()), new SimpleDateFormat("z").format(new Date()).replaceAll("GMT", ""),
                                    MainActivity.mSensorConnector.mLocationDevice.getLocation(), MainActivity.mSensorConnector.mSensorDevice.getEcompass(),
                                    1, rssi, phase, chidx);
                            if (bAdd2End) tagsList.add(readerDevice);
                            else tagsList.add(0, readerDevice);
                            SharedObjects.TagsIndex tagsIndex = new SharedObjects.TagsIndex(strAddresss, tagsList.size()-1); MainActivity.sharedObjects.tagsIndexList.add(tagsIndex); Collections.sort(MainActivity.sharedObjects.tagsIndexList);
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
                if (rfidRateView != null) rfidRateView.setText(String.valueOf(MainActivity.mCs108Library4a.invalidata) + "," + String.valueOf(MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.invalidUpdata));
            } else {
                String stringTemp = "Unique:" + String.valueOf(yield);
                if (true) {
                    float fErrorRate = (float) MainActivity.mCs108Library4a.invalidata / ( (float) MainActivity.mCs108Library4a.validata + (float) MainActivity.mCs108Library4a.invalidata ) * 100;
                    stringTemp += "\nE" + String.valueOf(MainActivity.mCs108Library4a.invalidata) + "/" + String.valueOf(MainActivity.mCs108Library4a.validata) + "/" + String.valueOf((int)fErrorRate);
                } else if (true) {
                    stringTemp += "\nE" + String.valueOf(MainActivity.mCs108Library4a.invalidata) + "," + String.valueOf(MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.invalidUpdata) + "/" + String.valueOf(MainActivity.mCs108Library4a.validata);
                }
                if (rfidYieldView != null) rfidYieldView.setText(stringTemp);
                if (total != 0) {
                    if (firstTimeOld == 0) firstTimeOld = firstTime;
                    if (totalOld == 0) totalOld = total;
                    String strRate = "Total:" + String.valueOf(allTotal) + "\n";
                    if (lastTime == 0) {
                        strRate += "Rate:" + String.valueOf(yieldRate) + "/" + String.valueOf(MainActivity.mCs108Library4a.getStreamInRate() / 17);
                    } else if (currentTime > firstTimeOld) strRate += "Rate:" + String.valueOf(yieldRate) + "/" + String.valueOf(totalOld * 1000 / (currentTime - firstTimeOld));
                    if (rfidRateView != null) rfidRateView.setText(strRate);
                    //if (lastTime - firstTime > 1000) {
                    firstTimeOld = currentTime;
                    totalOld = total;
                    total = 0;
                    //}
                }
            }
            if (playerN != null && playerO != null) {
                if (requestSound && playerO.isPlaying() == false && playerN.isPlaying() == false) {
                    if (true) {
                        if (bStartBeepWaiting == false) {
                            bStartBeepWaiting = true;
                            handler.postDelayed(runnableStartBeep, 250);
                        }
                        if (MainActivity.mCs108Library4a.getInventoryVibrate()) {
                            boolean validVibrate0 = false, validVibrate = false;
                            MainActivity.mCs108Library4a.appendToLog("Hello1: BBB");
                            if (MainActivity.mCs108Library4a.getVibrateModeSetting() == 0) {
                                MainActivity.mCs108Library4a.appendToLog("Hello1: CCC");
                                if (requestNewVibrate) {
                                    MainActivity.mCs108Library4a.appendToLog("Hello1: DDD");
                                    validVibrate0 = true;
                                }
                            } else if (bValidVibrateNewAll == false) {
                                MainActivity.mCs108Library4a.appendToLog("Hello1: EEE");
                                validVibrate0 = true;
                            }
                            requestNewVibrate = false;

                            if (validVibrate0) {
                                MainActivity.mCs108Library4a.appendToLog("Hello1: FFF");
                                if (bStartVibrateWaiting == false) {
                                    MainActivity.mCs108Library4a.appendToLog("Hello1: GGG");
                                    validVibrate = true;
                                } else if (bUseVibrateMode0 == false) {
                                    handler.removeCallbacks(runnableStartVibrate); int timeout = MainActivity.mCs108Library4a.getVibrateWindow() * 1000;
                                    handler.postDelayed(runnableStartVibrate, MainActivity.mCs108Library4a.getVibrateWindow() * 1000);
                                    MainActivity.mCs108Library4a.appendToLog("Hello1: HHH with timeout = " + timeout);    ///
                                }
                            }

                            if (validVibrate) {
                                if (bUseVibrateMode0) {
                                    MainActivity.mCs108Library4a.appendToLog("Hello1: JJJ");
                                    MainActivity.mCs108Library4a.setVibrateOn(1);
                                } else {
                                    MainActivity.mCs108Library4a.appendToLog("Hello1: KKK");    //
                                    MainActivity.mCs108Library4a.setVibrateOn(2);
                                }
                                bStartVibrateWaiting = true; int timeout = MainActivity.mCs108Library4a.getVibrateWindow() * 1000;
                                handler.postDelayed(runnableStartVibrate, MainActivity.mCs108Library4a.getVibrateWindow() * 1000);
                                MainActivity.mCs108Library4a.appendToLog("Hello1: III with timeout = " + timeout);
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
            bStartBeepWaiting = false;
            requestSound = false;
            if (requestNewSound) {
                requestNewSound = false;
                playerN.start(); playerN.setVolume(300, 300);
            } else {
                playerO.start(); playerN.setVolume(30, 30);
            }
        }
    };

    boolean bStartVibrateWaiting = false;
    Runnable runnableStartVibrate = new Runnable() {
        @Override
        public void run() {
            bStartVibrateWaiting = false;
            MainActivity.mCs108Library4a.appendToLog("Hello1: LLL");
            if (bUseVibrateMode0 == false) {
                MainActivity.mCs108Library4a.appendToLog("Hello1: MMM");    //
                MainActivity.mCs108Library4a.setVibrateOn(0);
            }
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
                             String strExtra1Filter, String strExtra1Decode,
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
        this.strExtra1Filter = strExtra1Filter;
        this.strExtra1Decode = strExtra1Decode;

        this.rfidRunTime = rfidRunTime;
        this.geigerTagGotView = geigerTagGotView;
        this.rfidVoltageLevel = rfidVoltageLevel;
        this.rfidYieldView = rfidYieldView;
        this.button = button;
        this.rfidRateView = rfidRateView;
        this.beepEnable = beepEnable;

        if (tagsList != null && readerListAdapter != null && beepEnable) {
            playerO = MainActivity.sharedObjects.playerO;
            playerN = MainActivity.sharedObjects.playerN;
        }
    }

    boolean popStatus = false; boolean popRequest = false; Toast mytoast;
    void DeviceConnectTask4InventoryEnding(TaskCancelRReason taskCancelRReason) {
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

    String decodeMicronData(String strCalData, String strActData) {
        MainActivity.mCs108Library4a.appendToLog("strCalData = " + strCalData + ", strActData = " + strActData);
        int calCode1, calTemp1, calCode2, calTemp2, calVer = -1;
        if (strCalData == null) return null;
        if (strCalData.length() < 16) return null;
        int crc = Integer.parseInt(strCalData.substring(0, 4), 16);
        calCode1 = Integer.parseInt(strCalData.substring(4, 7), 16);
        calTemp1 = Integer.parseInt(strCalData.substring(7, 10), 16); calTemp1 >>= 1;
        calCode2 = Integer.parseInt(strCalData.substring(9, 13), 16); calCode2 >>= 1; calCode2 &= 0xFFF;
        calTemp2 = Integer.parseInt(strCalData.substring(12, 16), 16); calTemp2 >>= 2; calTemp2 &= 0x7FF;
        calVer = Integer.parseInt(strCalData.substring(15, 16),16); calVer &= 0x3;
        MainActivity.mCs108Library4a.appendToLog("bExtraFilter: crc = " + crc + ", code1 = " + calCode1 + ", temp1 = " + calTemp1 + ", code2 = " + calCode2 + ", temp2 = " + calTemp2 + ", ver = " + calVer);

        if (strActData == null) return null;
        if (strActData.length() < 8) return null;

        int iRssi = Integer.parseInt(strActData.substring(0,4), 16);
        float fTemperature = Integer.parseInt(strActData.substring(4,8), 16);
        fTemperature = ((float)calTemp2 - (float)calTemp1) * (fTemperature - (float) calCode1);
        fTemperature /= ((float) (calCode2) - (float)calCode1);
        fTemperature += (float) calTemp1;
        fTemperature -= 800;
        fTemperature /= 10;
        String strRetValue = "(T=" + String.format("%.1f", fTemperature) + (char) 0x00B0 + "C" + "; OCRSSI=" + String.format("%d", iRssi) + ")";
        MainActivity.mCs108Library4a.appendToLog("bExtraFilter: strRetValue = " + strRetValue);
        return strRetValue;
    }
}