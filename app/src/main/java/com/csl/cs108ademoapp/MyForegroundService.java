package com.csl.cs108ademoapp;

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
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.csl.cs108ademoapp.fragments.AboutFragment;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cslibrary4a.BluetoothGatt;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannelA";
    public static final int SERVICE_ID = 1;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationCompatBuilder;
    Cs108Library4A csLibrary4A;
    ReaderDevice readerDevice;
    MyMqttClient myMqttClient;
    ArrayList<String> epcArrayList = new ArrayList<String>();
    long startTimeMillis = 0;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra("inputExtra");
        csLibrary4A = MainActivity.csLibrary4A; //new CsLibrary4A(this, null); //MainActivity.csLibrary4A
        if (csLibrary4A != null) Log.i("Hello", "MyForegroundService: onCreate");
        else Log.i("Hello", "MyForegroundService: onCreate with null csLibrary4A");
        Log.i("Hello", "MyForegroundService: onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("Hello", "MyForegroundService: createNotificationChannel");
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager = (NotificationManager) getSystemService(NotificationManager.class); //NotificationManager.class); //NOTIFICATION_SERVICE
            List<NotificationChannel> list = notificationManager.getNotificationChannels(); Log.i("Hello", "MyForegroundService: getNotificationChannels.size = " + list.size());
            notificationManager.createNotificationChannel(serviceChannel);
            list = notificationManager.getNotificationChannels(); Log.i("Hello", "MyForegroundService: after createNotificationChannel, getNotificationChannels.size = " + list.size());
        }

        Intent notificationIntent = new Intent(this, AboutFragment.class); // null); //, MainActivity.class);
        //notificationIntent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationCompatBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CsReader Foreground Service")
                //.setContentText("Hello World !!!")
                .setSmallIcon(R.drawable.csl_logo);
                //.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setContentIntent(pendingIntent)
                //.setAutoCancel(true)
                //.build();
        Notification notification = updateNotification("Hello World !!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(SERVICE_ID, notification);
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            int iRandom = getRandomNumber();
                            String strMessage = iRandom + ", foregroundServiceState = " + foregroundServiceState.toString();
                            if (foregroundServiceState == NULL) {
                                foregroundServiceState = WAIT;
                            } else if (foregroundServiceState == WAIT && MainActivity.csLibrary4A != null) {
                                MainActivity.csLibrary4A.appendToLog("ForegroundReader is ");
                                MainActivity.csLibrary4A.appendToLog("ForegroundReader is " + MainActivity.csLibrary4A.getForegroundReader());
                                if (isForegroundEnable()) {
                                    if (MainActivity.csLibrary4A.isBleConnected()) foregroundServiceState = CONNECTED;
                                    else {
                                        csLibrary4A.appendToLog("Start ScanLeDevice");
                                        csLibrary4A.scanLeDevice(true);
                                        foregroundServiceState = SCAN;
                                    }
                                }
                            } else if (foregroundServiceState == SCAN) {
                                if (MainActivity.csLibrary4A.isBleConnected()) foregroundServiceState = CONNECTED;
                                else if (isForegroundEnable()) {
                                    BluetoothGatt.Cs108ScanData cs108ScanData = null;
                                    while (true) {
                                        cs108ScanData = csLibrary4A.getNewDeviceScanned();
                                        csLibrary4A.appendToLog("cs108ScanData is " + (cs108ScanData == null ? "null" : "valid"));
                                        if (cs108ScanData == null) break;
                                        strMessage += ("\n" + cs108ScanData.device.getAddress());
                                        if (cs108ScanData.device.getAddress().matches(MainActivity.csLibrary4A.getForegroundReader())) { //"84:C6:92:9D:DD:52")) {
                                            readerDevice = new ReaderDevice(cs108ScanData.device.getName(), cs108ScanData.device.getAddress(), false, "", 1, cs108ScanData.rssi, cs108ScanData.serviceUUID2p2);
                                            String strInfo = "";
                                            if (cs108ScanData.device.getBondState() == 12) {
                                                strInfo += "BOND_BONDED\n";
                                            }
                                            readerDevice.setDetails(strInfo + "scanRecord=" + MainActivity.csLibrary4A.byteArrayToString(cs108ScanData.scanRecord));

                                            csLibrary4A.scanLeDevice(false);
                                            csLibrary4A.connect(readerDevice);
                                            foregroundServiceState = CONNECT;
                                            break;
                                        }
                                    }
                                    if (foregroundServiceState != CONNECT && cs108ScanData != null)
                                        strMessage += ("\n" + cs108ScanData.device.getAddress());
                                } else {
                                    csLibrary4A.appendToLog("Stop ScanLeDevice");
                                    csLibrary4A.scanLeDevice(false);
                                    foregroundServiceState = NULL;
                                }
                            } else if (foregroundServiceState == CONNECT) {
                                if (csLibrary4A.isBleConnected()) {
                                    readerDevice.setConnected(true);
                                    readerDevice.setSelected(true);
                                    MainActivity.sharedObjects.readersList.add(readerDevice);
                                    foregroundServiceState = CONNECTED;
                                }
                            } else if (foregroundServiceState == CLOUDCONNECT) {
                                if (myMqttClient != null && myMqttClient.isMqttServerConnected) foregroundServiceState = CONNECTED;
                                else {
                                    myMqttClient = new MyMqttClient(getApplicationContext());
                                    myMqttClient.connect(null);
                                }
                            } else if (foregroundServiceState == CONNECTED) {
                                if (csLibrary4A.isBleConnected()) {
                                    if (isForegroundEnable() && csLibrary4A.getInventoryCloudSave() == 2
                                            && csLibrary4A.getServerMqttLocation().length() > 0 && csLibrary4A.getTopicMqtt().length() > 0) {
                                        if (myMqttClient == null || !myMqttClient.isMqttServerConnected) foregroundServiceState = CLOUDCONNECT;
                                        else if (csLibrary4A.getTriggerButtonStatus())  {
                                            //write2Server(messageStr); csLibrary4A.appendToLog("bImpinjServer: doInBackground after write2Server");
                                            //closeServer(); csLibrary4A.appendToLog("bImpinjServer: doInBackground after closeServer");
                                            strMessage += ("\n" + "pressed trigger");
                                            csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
                                            foregroundServiceState = INVENTORY;
                                        } else strMessage += ("\n" + "released trigger");
                                    }
                                    MainActivity.csLibrary4A.appendToLog("isForegroundEnable = " + isForegroundEnable() + ", getInventoryCloudSave = " + csLibrary4A.getInventoryCloudSave() + ", getServerMqttLocation = " + csLibrary4A.getServerMqttLocation() + ", getTopicMqtt = " + csLibrary4A.getTopicMqtt());
                                } else {
                                    csLibrary4A.appendToLog("disconnect");
                                    csLibrary4A.disconnect(false);
                                    myMqttClient.disconnect(null);
                                    foregroundServiceState = NULL;
                                }
                            } else if (foregroundServiceState == INVENTORY) {
                                if (csLibrary4A.isBleConnected() && myMqttClient.isMqttServerConnected) {
                                    long timePeriod = System.currentTimeMillis() - startTimeMillis;
                                    if (timePeriod > MainActivity.csLibrary4A.getForegroundDupElim() * 1000L) {
                                        startTimeMillis = System.currentTimeMillis();
                                        MainActivity.csLibrary4A.appendToLog("Foreground removes strEpcList of size " + epcArrayList.size());
                                        epcArrayList.clear();
                                    }
                                    if (isForegroundEnable()) {
                                        if (csLibrary4A.getTriggerButtonStatus()) {
                                            //strMessage += ("\n" + "pressed trigger");
                                            RfidReaderChipData.Rx000pkgData rx000pkgData = null, rx000pkgData1 = null;
                                            while (true) {
                                                rx000pkgData = csLibrary4A.onRFIDEvent();
                                                MainActivity.csLibrary4A.appendToLog("rx000pkgData is " + (rx000pkgData == null ? "null" : "valid") +
                                                        ", rx000pkgData1 is " + (rx000pkgData1 == null ? "null" : "valid"));
                                                if (rx000pkgData == null) {
                                                    if (rx000pkgData1 == null) strMessage += ("\n NO Tag is found");
                                                    break;
                                                } else {
                                                    rx000pkgData1 = rx000pkgData;
                                                    String strEpc = MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc);
                                                    if (strEpc == null || strEpc.length() <= 0) rx000pkgData1 = null;
                                                    else {
                                                        boolean match = false;
                                                        for (int i = 0; i < epcArrayList.size(); i++) {
                                                           if (strEpc.matches(epcArrayList.get(i))) {
                                                               match = true;
                                                               MainActivity.csLibrary4A.appendToLog("Foreground matches tag " + strEpc);
                                                               break;
                                                           }
                                                        }
                                                        if (!match) epcArrayList.add(strEpc);
                                                        else rx000pkgData1 = null;
                                                    }
                                                    if (rx000pkgData1 != null) {
                                                        if (MainActivity.csLibrary4A.getInventoryCloudSave() == 1) {
                                                            SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(false);
                                                            try {
                                                                saveExternalTask.openServer(false);
                                                                csLibrary4A.appendToLog("Server: openServer success");
                                                                ReaderDevice readerDevice1 = new ReaderDevice("", csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc), false, null,
                                                                        csLibrary4A.byteArrayToString(rx000pkgData1.decodedPc), null, csLibrary4A.byteArrayToString(rx000pkgData1.decodedCrc), null,
                                                                        null, -1, -1,
                                                                        null, -1, -1,
                                                                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()), new SimpleDateFormat("z").format(new Date()).replaceAll("GMT", ""),
                                                                        MainActivity.mSensorConnector.mLocationDevice.getLocation(), MainActivity.mSensorConnector.mSensorDevice.getEcompass(),
                                                                        1, rx000pkgData1.decodedRssi, rx000pkgData1.decodedPhase, rx000pkgData1.decodedChidx, rx000pkgData1.decodedPort, -1, -1, -1, -1, -1, -1, null, -1);
                                                                String messageStr = saveExternalTask.createJSON(null, readerDevice1).toString();
                                                                MainActivity.csLibrary4A.appendToLog("Json = " + messageStr);
                                                                saveExternalTask.write2Server(messageStr);
                                                                csLibrary4A.appendToLog("Server: write2Server success");
                                                                saveExternalTask.closeServer();
                                                                csLibrary4A.appendToLog("Server: closeServer success");
                                                            } catch (Exception e) {
                                                                csLibrary4A.appendToLog("Server: write2Server failure");
                                                                //throw new RuntimeException(e);
                                                            }
                                                        } else if (MainActivity.csLibrary4A.getInventoryCloudSave() == 2) {
                                                            myMqttClient.publish(MainActivity.csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc));
                                                        }
                                                    }
                                                }
                                            }
                                            if (rx000pkgData1 != null) strMessage += ("\n " + csLibrary4A.byteArrayToString(rx000pkgData1.decodedEpc));
                                        } else {
                                            while (true) {
                                                RfidReaderChipData.Rx000pkgData rx000pkgData = csLibrary4A.onRFIDEvent();
                                                if (rx000pkgData == null) break;
                                            }
                                            strMessage += ("\n" + "released trigger");
                                            csLibrary4A.abortOperation();
                                            foregroundServiceState = CONNECTED;
                                        }
                                    } else {
                                        csLibrary4A.abortOperation();
                                        foregroundServiceState = CONNECTED;
                                    }
                                } else foregroundServiceState = CONNECTED;
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
        ).start();

        // Builds the notification and issues it.
        //do heavy work on a background thread
        //stopSelf();
        return super.onStartCommand(intent, flags, startId); //return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        csLibrary4A.disconnect(false);
        csLibrary4A.appendToLog("MyForegroundService: onDestroy 0");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        csLibrary4A.appendToLog("MyForegroundService: onBind");
        return binder; // null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        csLibrary4A.appendToLog("MyForegroundService: onTaskRemoved");
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        //do something you want
        //stop service
        this.stopSelf();
    }

    Notification updateNotification(String string) {
        Log.i("Hello","MyForegroundService updateNotification: " + string);
        notificationCompatBuilder.setContentText(string);
        Notification notification = notificationCompatBuilder.build();
        notificationManager.notify(1, notification);
        return notification;
    }

    final Random mGenerator = new Random();
    int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    boolean isForegroundEnable() {
        MainActivity.csLibrary4A.appendToLog("isHomeFragment = " + MainActivity.isHomeFragment + ", getForegroundReader = " + MainActivity.csLibrary4A.getForegroundReader());
        return (MainActivity.isHomeFragment && MainActivity.csLibrary4A.getForegroundReader().length() != 0);
    }

    enum ForegroundServiceState {
        NULL, WAIT, SCAN, CONNECT, CLOUDCONNECT, CONNECTED, INVENTORY, DISCONNECT
    }
    ForegroundServiceState foregroundServiceState = NULL;
}