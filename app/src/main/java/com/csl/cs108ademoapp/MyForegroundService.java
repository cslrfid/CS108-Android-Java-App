package com.csl.cs108ademoapp;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static com.csl.cs108ademoapp.MainActivity.mContext;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.CLOUDCONNECT;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.CONNECT;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.CONNECTED;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.INVENTORY;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.NULL;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.SCAN;
import static com.csl.cs108ademoapp.MyForegroundService.ForegroundServiceState.WAIT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.csl.cs108ademoapp.fragments.AboutFragment;
import com.csl.cslibrary4a.BluetoothGatt;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyForegroundService extends Service {
    boolean DEBUG = false;
    String TAG = "Hello";
    Handler mHandler = new Handler();
    public static final String CHANNEL_ID = "ForegroundServiceChannelA";
    public static final int SERVICE_ID = 1;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationCompatBuilder;
    ReaderDevice readerDevice;
    SaveList2ExternalTask saveExternalTask; boolean isHttpServerOpened;
    MyMqttClient myMqttClient;
    ArrayList<String> epcArrayList = new ArrayList<String>();
    ArrayList<ReaderDevice> readerDeviceArrayList = new ArrayList<>();
    long startTimeMillis, inventoryStartTimeMillis = 0;
    int iConnectingCount = 0;

    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        MyForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return MyForegroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler.post(serviceRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra("inputExtra");
        if (MainActivity.csLibrary4A != null) Log.i(TAG, "MyForegroundService onStartCommand: csLibrary4A is created");
        else Log.i(TAG, "MyForegroundService onStartCommand: null csLibrary4A");
        Log.i(TAG, "MyForegroundService: onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "MyForegroundService: createNotificationChannel");
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager = (NotificationManager) getSystemService(NotificationManager.class); //NotificationManager.class); //NOTIFICATION_SERVICE
            List<NotificationChannel> list = notificationManager.getNotificationChannels(); Log.i(TAG, "MyForegroundService onStartComand: getNotificationChannels.size = " + list.size());
            notificationManager.createNotificationChannel(serviceChannel);
            list = notificationManager.getNotificationChannels(); Log.i(TAG, "MyForegroundService onStartCommand: after createNotificationChannel, getNotificationChannels.size = " + list.size());
        }

        Intent notificationIntent = new Intent(this, AboutFragment.class);
        //notificationIntent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationCompatBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CsReader Foreground Service")
                .setSmallIcon(R.drawable.csl_logo);
                //.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setContentIntent(pendingIntent)
                //.setAutoCancel(true)
                //.build();
        Notification notification = updateNotification("MyForegroundService starts");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(SERVICE_ID, notification);
        }

        thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            int iRandom = getRandomNumber();
                            String strMessage = iRandom + ", ";
                            if (MainActivity.csLibrary4A == null) {
                                strMessage += "Cannot connect. Please restart App";
                                foregroundServiceState = NULL;
                            } else
                        {
                            ForegroundServiceState foregroundServiceStateOld = foregroundServiceState;
                            if (foregroundServiceState == NULL) {
                                strMessage += "ServiceState = " + foregroundServiceState.toString();
                                foregroundServiceState = WAIT;
                            } else if (foregroundServiceState == WAIT) {
                                strMessage += "Wait to enable Foreground Service";
                                if (isForegroundEnable()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //that is android 12 or above
                                        if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                                                || ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                            Log.i(TAG, "runnableStart: CANNOT start scanLeDevice as BLUETOOTH_CONNECT && BLUETOOTH_SCAN is NOT yet permitted");
                                        } else if (MainActivity.csLibrary4A.isBleConnected()) {
                                            foregroundServiceState = CONNECTED;
                                            Log.i(TAG, "going to CONNECTED");
                                        } else if (MainActivity.activityActive == false) {
                                            Log.i(TAG, "runnableStartService: BLUETOOTH_CONNECT and BLUETOOTH_SCAN and (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION) is permitted");
                                        } else {
                                            Log.i(TAG, "Start ScanLeDevice");
                                            MainActivity.csLibrary4A.scanLeDevice(true);
                                            foregroundServiceState = SCAN;
                                        }
                                    }
                                }
                            } else if (foregroundServiceState == SCAN) {
                                strMessage += "Scanning reader";
                                if (MainActivity.csLibrary4A.isBleConnected()) {
                                    foregroundServiceState = CONNECTED;
                                } else if (isForegroundEnable()) {
                                    BluetoothGatt.CsScanData csScanData = null;
                                    while (true) {
                                        csScanData = MainActivity.csLibrary4A.getNewDeviceScanned();
                                        Log.i(TAG, "cs108ScanData is " + (csScanData == null ? "null" : "valid") + ", foregroundReader = " + MainActivity.csLibrary4A.getForegroundReader());
                                        if (csScanData == null) break;
                                        strMessage += ("\n" + csScanData.device.getAddress());
                                        if (csScanData.device.getAddress().matches(MainActivity.csLibrary4A.getForegroundReader())) { //"84:C6:92:9D:DD:52")) {
                                            readerDevice = new ReaderDevice(csScanData.device.getName(), csScanData.device.getAddress(), false, "", 1, csScanData.rssi, csScanData.serviceUUID2p2);
                                            String strInfo = "";
                                            if (csScanData.device.getBondState() == 12) {
                                                strInfo += "BOND_BONDED\n";
                                            }
                                            readerDevice.setDetails(strInfo + "scanRecord=" + MainActivity.csLibrary4A.byteArrayToString(csScanData.scanRecord));

                                            MainActivity.csLibrary4A.scanLeDevice(false);
                                            MainActivity.csLibrary4A.connect(readerDevice);
                                            foregroundServiceState = CONNECT;
                                            iConnectingCount = 0;
                                            break;
                                        }
                                    }
                                    if (foregroundServiceState != CONNECT && csScanData != null)
                                        strMessage += ("\n" + csScanData.device.getAddress());
                                } else {
                                    Log.i(TAG, "Stop ScanLeDevice");
                                    MainActivity.csLibrary4A.scanLeDevice(false);
                                    foregroundServiceState = NULL;
                                }
                            } else if (foregroundServiceState == CONNECT) {
                                strMessage += "Connecting Reader";
                                if (MainActivity.csLibrary4A.isBleConnected()) {
                                    readerDevice.setConnected(true);
                                    readerDevice.setSelected(true);
                                    MainActivity.sharedObjects.readersList.add(readerDevice);
                                    foregroundServiceState = CONNECTED;
                                } else if (++iConnectingCount > 10) {
                                    MainActivity.csLibrary4A.disconnect(false);
                                    foregroundServiceState = NULL;
                                }
                            } else if (foregroundServiceState == CLOUDCONNECT) {
                                strMessage += "Connecting MQTT Server";
                                if (!MainActivity.csLibrary4A.isBleConnected())
                                    foregroundServiceState = CONNECTED;
                                else if (!isForegroundEnable())
                                    foregroundServiceState = CONNECTED;
                                if (false && MainActivity.csLibrary4A.getInventoryCloudSave() == 1) {
                                    saveExternalTask = new SaveList2ExternalTask(false);
                                    Log.i(TAG, "Server: new saveExternalTask");
                                    isHttpServerOpened = saveExternalTask.openServer(false);
                                    Log.i(TAG, "Server: openServer is " + isHttpServerOpened);
                                    foregroundServiceState = CONNECTED;
                                } else if (MainActivity.csLibrary4A.getInventoryCloudSave() == 2) {
                                    if (myMqttClient != null && myMqttClient.isMqttServerConnected) {
                                        Log.i(TAG, "MyForegroundService: Connecting MQTT 1");
                                        foregroundServiceState = CONNECTED;
                                    } else {
                                        Log.i(TAG, "MyForegroundService: Connecting MQTT 2");
                                        myMqttClient = new MyMqttClient(getApplicationContext());
                                        myMqttClient.connect(null);
                                    }
                                } else foregroundServiceState = NULL;
                            } else if (foregroundServiceState == CONNECTED) {
                                strMessage += "Connected";
                                if (MainActivity.csLibrary4A.isBleConnected()) {
                                    if (isForegroundEnable()) {
                                        boolean bStartInventory = false;
                                        if (MainActivity.csLibrary4A.getInventoryCloudSave() == 1) {
                                            if (false && !isHttpServerOpened) {
                                                Log.i(TAG, "Server: going to CloudConnect");
                                                foregroundServiceState = CLOUDCONNECT;
                                            } else if (MainActivity.csLibrary4A.getTriggerButtonStatus()) {
                                                strMessage += ("\n" + "H pressed trigger");
                                                bStartInventory = true;
                                            } else strMessage += ("\n" + "H released trigger");
                                        } else if (MainActivity.csLibrary4A.getInventoryCloudSave() == 2
                                                && MainActivity.csLibrary4A.getServerMqttLocation().length() > 0 && MainActivity.csLibrary4A.getTopicMqtt().length() > 0) {
                                            if (myMqttClient == null || !myMqttClient.isMqttServerConnected)
                                                foregroundServiceState = CLOUDCONNECT;
                                            else if (MainActivity.csLibrary4A.getTriggerButtonStatus()) {
                                                strMessage += (" with trigger pressed");
                                                bStartInventory = true;
                                            } else strMessage += (" with trigger released");
                                        }
                                        if (bStartInventory) {
                                            Log.i(TAG, "Debug_Compact: MyForegroundService.onStartCommand");
                                            MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
                                            Log.i(TAG, "Server:sss startOperation");
                                            inventoryStartTimeMillis = System.currentTimeMillis();
                                            foregroundServiceState = INVENTORY;
                                        }
                                    }
                                    if (false) Log.i(TAG, "isForegroundEnable = " + isForegroundEnable()
                                            + ", getInventoryCloudSave = " + MainActivity.csLibrary4A.getInventoryCloudSave()
                                            + ", getServerMqttLocation = " + MainActivity.csLibrary4A.getServerMqttLocation()
                                            + ", getTopicMqtt = " + MainActivity.csLibrary4A.getTopicMqtt());
                                } else {
                                    Log.i(TAG, "Disconnecting");
                                    MainActivity.csLibrary4A.disconnect(false);
                                    if (isHttpServerOpened) {
                                        if (saveExternalTask.closeServer())
                                            Log.i(TAG, "Server: closeServer success");
                                        else Log.i(TAG, "Server: closeServer failure");
                                        isHttpServerOpened = false;
                                    }
                                    if (myMqttClient != null) myMqttClient.disconnect(null);
                                    foregroundServiceState = NULL;
                                }
                            } else if (foregroundServiceState == INVENTORY) {
                                strMessage += "Doing inventory ";
                                Log.i(TAG, "inventory: isBleConnected = " + MainActivity.csLibrary4A.isBleConnected());
                                Log.i(TAG, "inventory: myMqttClient = " + (myMqttClient == null ? "null" : myMqttClient.isMqttServerConnected));
                                if (!MainActivity.csLibrary4A.isBleConnected())
                                    foregroundServiceState = CONNECTED;
//                                else if (csLibrary4A.getInventoryCloudSave() == 2 && (myMqttClient == null || !myMqttClient.isMqttServerConnected)) foregroundServiceState = CONNECTED;
                                else {
                                    long timePeriod = System.currentTimeMillis() - startTimeMillis;
                                    if (timePeriod > MainActivity.csLibrary4A.getForegroundDupElim() * 1000L) {
                                        startTimeMillis = System.currentTimeMillis();
                                        Log.i(TAG, "Foreground removes strEpcList of size " + epcArrayList.size());
                                        epcArrayList.clear();
                                    }
                                    timePeriod = System.currentTimeMillis() - inventoryStartTimeMillis;
                                    if (MainActivity.csLibrary4A.getInventoryCloudSave() == 2)
                                        timePeriod = 0;
                                    if (isForegroundEnable() && MainActivity.csLibrary4A.getTriggerButtonStatus() && timePeriod < 2000L) {
                                        RfidReaderChipData.Rx000pkgData rx000pkgData = null, rx000pkgData1 = null;
                                        while (MainActivity.csLibrary4A.getTriggerButtonStatus()) {
                                            rx000pkgData = MainActivity.csLibrary4A.onRFIDEvent();
                                            Log.i(TAG, "rx000pkgData is " + (rx000pkgData == null ? "null" : "valid") +
                                                    ", rx000pkgData1 is " + (rx000pkgData1 == null ? "null" : "valid"));
                                            if (rx000pkgData == null) {
                                                if (rx000pkgData1 == null)
                                                    strMessage += ("\n NO Tag is found");
                                                break;
                                            } else {
                                                rx000pkgData1 = rx000pkgData;
                                                String strEpc = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc);
                                                if (strEpc == null || strEpc.length() <= 0)
                                                    rx000pkgData1 = null;
                                                else {
                                                    boolean match = false;
                                                    for (int i = 0; i < epcArrayList.size(); i++) {
                                                        if (strEpc.matches(epcArrayList.get(i))) {
                                                            match = true;
                                                            Log.i(TAG, "Foreground matches tag " + strEpc);
                                                            break;
                                                        }
                                                    }
                                                    if (!match) epcArrayList.add(strEpc);
                                                    else rx000pkgData1 = null;
                                                }
                                                if (rx000pkgData1 != null) {
                                                    Log.i(TAG, "Server: getInventoryCloudSave = " + MainActivity.csLibrary4A.getInventoryCloudSave());
                                                    if (MainActivity.csLibrary4A.getInventoryCloudSave() == 1) {
                                                        ReaderDevice readerDevice1 = new ReaderDevice("", MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc), false, null,
                                                                MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedPc), null, MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedCrc), null,
                                                                null, -1, -1,
                                                                null, -1, -1,
                                                                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()), new SimpleDateFormat("z").format(new Date()).replaceAll("GMT", ""),
                                                                MainActivity.mSensorConnector.mLocationDevice.getLocation(), MainActivity.mSensorConnector.mSensorDevice.getEcompass(),
                                                                1, rx000pkgData1.decodedRssi, rx000pkgData1.decodedPhase, rx000pkgData1.decodedChidx, rx000pkgData1.decodedPort, -1, -1, -1, -1, -1, -1, null, -1);

                                                        readerDeviceArrayList.add(readerDevice1);
                                                    } else if (MainActivity.csLibrary4A.getInventoryCloudSave() == 2) {
                                                        myMqttClient.publish(MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc));
                                                    }
                                                }
                                            }
                                        }
                                        if (rx000pkgData1 != null)
                                            strMessage += ("\n " + MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc));
                                    } else { //isForegroundEnable() && csLibrary4A.getTriggerButtonStatus() && timePeriod
                                        Log.i(TAG, "Server:sss abortOperation with isForegroundEnable = " + isForegroundEnable()
                                                + ", getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus() + ", timePeriod = " + timePeriod + ", ");
                                        MainActivity.csLibrary4A.abortOperation();
                                        while (true) {
                                            RfidReaderChipData.Rx000pkgData rx000pkgData = MainActivity.csLibrary4A.onRFIDEvent();
                                            if (rx000pkgData == null) break;
                                        }
                                        if (readerDeviceArrayList.size() != 0) {
                                            saveExternalTask = new SaveList2ExternalTask(false);
                                            Log.i(TAG, "Server: new saveExternalTask");
                                            isHttpServerOpened = saveExternalTask.openServer(false);
                                            Log.i(TAG, "Server: openServer is " + isHttpServerOpened);
                                            String messageStr = saveExternalTask.createJSON(readerDeviceArrayList, null).toString();
                                            Log.i(TAG, "Server: Json = " + messageStr);
                                            saveExternalTask.write2Server(messageStr);
                                            Log.i(TAG, "Server: write2Server success");
                                            if (saveExternalTask.closeServer())
                                                Log.i(TAG, "Server: closeServer success");
                                            else Log.i(TAG, "Server: closeServer failure");
                                        }
                                        foregroundServiceState = CONNECTED;
                                    }
                                }
                            }
                            if (foregroundServiceState != foregroundServiceStateOld) {
                                strMessage += (", New ServiceState = " + foregroundServiceState.toString());
                            }
                            MainActivity.csLibrary4A.batteryLevelRequest(); //dummy reader access
                        }

                            updateNotification(strMessage);
                            try {
                                int iTime = 2000;
                                if (foregroundServiceState == INVENTORY) iTime = 100;
                                Thread.sleep(iTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        thread.start();

        // Builds the notification and issues it.
        //do heavy work on a background thread
        //stopSelf();
        return super.onStartCommand(intent, flags, startId); //return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "MyForegroundService: onDestroy");
        mHandler.removeCallbacks(serviceRunnable);
        if (MainActivity.csLibrary4A != null) {
            MainActivity.csLibrary4A.disconnect(false);
            Log.i(TAG, "MyForegroundService: onDestroy 0");
        }
        thread.stop();
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "MyForegroundService: onBind");
        return binder; // null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "MyForegroundService: onTaskRemoved");
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        //do something you want
        //stop service
        thread.stop();
        this.stopSelf();
    }

    Thread thread = null;
    Notification updateNotification(String string) {
        if (DEBUG) Log.i(TAG, "MyForegroundService.updateNotification: " + string);
        notificationCompatBuilder.setContentText(string);
        Notification notification = notificationCompatBuilder.build();
        notificationManager.notify(1, notification);
        return notification;
    }

    int batteryCount_old; String strBatteryLow_old;
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) Log.i(TAG, "MyForegroundService.serviceRunnable starts");
            mHandler.postDelayed(serviceRunnable, 2000);
            if (!MainActivity.activityActive && MainActivity.csLibrary4A != null && MainActivity.csLibrary4A.isBleConnected()) {
                int batteryCount = MainActivity.csLibrary4A.getBatteryCount();
                String strBatteryLow = MainActivity.csLibrary4A.isBatteryLow();
                if (DEBUG) Log.i(TAG, "MyForegroundService.serviceRunnable: batteryCount = " + batteryCount + ", batteryCount_old = " + batteryCount_old);
                if (batteryCount_old != batteryCount) {
                    batteryCount_old = batteryCount;
                    if (strBatteryLow == null || strBatteryLow_old == null)
                        strBatteryLow_old = strBatteryLow;
                    else if (!strBatteryLow.matches(strBatteryLow_old))
                        strBatteryLow_old = strBatteryLow;
                }
                if (strBatteryLow != null) {
                    if (false) Log.i(TAG, "CustomIME Debug 112");
                    Toast.makeText(mContext,
                            "Battery Low: " + strBatteryLow + "% Battery Life Left",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    final Random mGenerator = new Random();
    int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    boolean isForegroundEnable() {
        if (MainActivity.csLibrary4A == null) return false;
        if (DEBUG) Log.i(TAG, "MyForegroundService.isForegroundEnable: isHomeFragment = " + MainActivity.isHomeFragment + ", getForegroundReader = " + MainActivity.csLibrary4A.getForegroundReader());
        return (MainActivity.isHomeFragment && MainActivity.csLibrary4A.getForegroundReader().length() != 0);
    }

    enum ForegroundServiceState {
        NULL, WAIT, SCAN, CONNECT, CLOUDCONNECT, CONNECTED, INVENTORY, DISCONNECT
    }
    ForegroundServiceState foregroundServiceState = NULL;
}