package com.csl.cs108ademoapp.fragments;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.LOCATION_SERVICE;

import static com.csl.cs108ademoapp.MainActivity.mContext;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.DrawerListContent;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.CustomAlertDialog;
import com.csl.cslibrary4a.BluetoothGatt;
import com.csl.cslibrary4a.ReaderDevice;

import java.util.ArrayList;
import java.util.List;

public class DirectWedgeFragment extends CommonFragment {
    Button buttonConnect;
    private ArrayList<ReaderDevice> readersList = MainActivity.sharedObjects.readersList;
    Handler handler = new Handler();
    private ReaderListAdapter readerListAdapter;
    boolean bWedgeConnecting = false, bWedgeConnected = false;
    boolean bCurrentIMEmatched = false;
    String stringImeExpected, stringLabelExpected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_directwedge, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TableRow tableRow1 = getActivity().findViewById(R.id.directWedgeRow1);
        TableRow tableRow2 = getActivity().findViewById(R.id.directWedgeRow2);
        TableRow tableRow3 = getActivity().findViewById(R.id.directWedgeRow3);
        TableRow tableRow4 = getActivity().findViewById(R.id.directWedgeRow4);
        TableRow tableRow5 = getActivity().findViewById(R.id.directWedgeRow5);
        TableRow tableRowStart = getActivity().findViewById(R.id.directWedgeRowStart);
        MainActivity.csLibrary4A.appendToLog("getPackageName = " + getActivity().getPackageName());
        if (getActivity().getPackageName().contains("cs710awedgeapp")) {
            androidx.appcompat.app.ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_access);
            ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle(R.string.app_ime_simplewedge1);
            actionBar.setDisplayHomeAsUpEnabled(false);
            //String strTitle = actionBar.getTitle().toString() + " v" + BuildConfig.VERSION_NAME;
            //actionBar.setTitle(strTitle);
        } else { //if (getActivity().getPackageName().contains("cs710ademoapp")) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_inv);
            actionBar.setTitle("Wedge");

            tableRow1.setVisibility(View.GONE);
            tableRow2.setVisibility(View.GONE);
            tableRow3.setVisibility(View.GONE);
            tableRow4.setVisibility(View.GONE);
            tableRow5.setVisibility(View.GONE);
            tableRowStart.setVisibility(View.VISIBLE);
        }

        if (false) {
            ReaderDevice readerDevice1 = new ReaderDevice(null, "123456", false, null, 0, 0);
            ReaderDevice readerDevice2 = new ReaderDevice(null, "1234567890", false, null, 0, 0);
            readersList.add(readerDevice1);
            readersList.add(readerDevice2);
        }

        ListView readerListView = (ListView) getActivity().findViewById(R.id.directWedgeListView);
        TextView readerEmptyView = (TextView) getActivity().findViewById(R.id.directWedgeTextViewEmpty);
        readerListView.setEmptyView(readerEmptyView);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, readersList, true,  true);
        readerListView.setAdapter(readerListAdapter);
        readerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        readerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean DEBUG = true;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("OnItemClickListener: bConnecting = " + bWedgeConnecting + ", position = " + position);
                if (bWedgeConnecting) return;

                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (readersList.size() > position) {
                    if (readerDevice.getSelected()) readerDevice.setSelected(false);
                    else readerDevice.setSelected(true);
                    readersList.set(position, readerDevice);
                    for (int i = 0; i < readersList.size(); i++) {
                        if (i != position) {
                            ReaderDevice readerDevice1 = readersList.get(i);
                            if (readerDevice1.getSelected()) {
                                readerDevice1.setSelected(false);
                                readersList.set(i, readerDevice1);
                            }
                        }
                    }
                }
                readerListAdapter.notifyDataSetChanged();

                if (DEBUG) MainActivity.csLibrary4A.appendToLog("OnItemClickListener: readerDevice.getSelected = " + readerDevice.getSelected());
                if (MainActivity.csLibrary4A.isBleConnected() && readerDevice.getSelected() == false && readerDevice.isConnected()) {
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("OnItemClickListener: going to disconnect");
                    disconnectWedge();
                } else if (MainActivity.csLibrary4A.isBleConnected() == false && readerDevice.getSelected()) {
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("OnItemClickListener: going to CONNECT");
                    if (true) connectWedge(readerDevice);
                }
            }
        });

        Button buttonSetup = (Button) getActivity().findViewById(R.id.directWedgeButtonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() || true) new SettingWedgeFragment().show(getChildFragmentManager(), "TAG");
                else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_not_ready), Toast.LENGTH_SHORT).show();
                else Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_not_connected), Toast.LENGTH_SHORT).show();
            }
        });

        buttonConnect = (Button) getActivity().findViewById(R.id.directWedgeButtonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bWedgeConnecting) return;
                if (MainActivity.csLibrary4A.isBleConnected()) {
                    disconnectWedge();
                    readerListAdapter.notifyDataSetChanged();
                } else {
                    boolean bFound = false;
                    for (int i = 0; i < readersList.size(); i++) {
                        ReaderDevice readerDevice = readersList.get(i);
                        if (readerDevice.getSelected() && readerDevice.isConnected() == false) {
                            connectWedge(readerDevice);
                            bFound = true;
                            break;
                        }
                    }
                    if (bFound == false) Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_select_reader_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonStart = (Button) getActivity().findViewById(R.id.directWedgeButtonStart);
        if (!getActivity().getPackageName().matches("com.csl.cs710awedgeapp")) buttonStart.setVisibility(View.VISIBLE);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected()) {
                    if (MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                        MainActivity.wedged = true;
                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_HOME);
                        startActivity(i);
                    }
                } else Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_not_connected), Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonInfo = (Button) getActivity().findViewById(R.id.directWedgeButtonInfo);
        buttonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomPopupWindow customPopupWindow = new CustomPopupWindow(mContext);
                String stringInfo =
                "1.	For the first time launching the application, please grant device permission for location, connectivity and enabling <CSL Data Wedge>. \n\n" +
                "2.	Readers will be discovered by the app and please check the box on the right hand side to select the reader to be connected. \n\n" +
                "3.	Once connected, put the application in background.\n\n" +
                "4.	Open the target application where you would like data to be printed.\n\n" +
                "5.	On the target applicatoin , put the cursor to the text field where data will be printed.  Switch your keyboard to <CSL Data Wedge>.\n\n" +
                "6.	Press and hold the trigger key of the reader to start reading tags. EPC values will be printed to the cursor on your target application.\n\n" +
                "7.	Switch back to the CSL Data Wedge application.  Now you can press the \"Disconnect\" button to disconnect from the reader.\n\n" +
                "8.	Configuration button: Press the button to modify parameters such as power, prefix, suffix and delimiter.\n\n";
                MainActivity.csLibrary4A.appendToLog(stringInfo);
                customPopupWindow.popupStart(stringInfo, false);
            }
        });

        handler.post(runnableStartService);
        handler.postDelayed(runnableStart, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        readerListAdapter.notifyDataSetChanged();
        updateCurrentIMEMatched();
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    public DirectWedgeFragment() {
        super("DirectWedgeFragment");
    }

    public static boolean bUserRequestedDisconnect = false;
    void disconnectWedge() {
        MainActivity.csLibrary4A.appendToLog("foregroundReader108: updated 2A");
        MainActivity.csLibrary4A.setForegroundServiceEnable(false);
        MainActivity.csLibrary4A.appendToLog("getForegroundReader = " + MainActivity.csLibrary4A.getForegroundReader());

        MainActivity.csLibrary4A.disconnect(false); bleDisConnecting = true; bWedgeConnecting = false; bUserRequestedDisconnect = true;
        readersList.clear();
        buttonConnect.setText("Connect");
        handler.removeCallbacks(runnableStart); handler.postDelayed(runnableStart, 2000);

        MainActivity.csLibrary4A.setWedgeDeviceName(null); MainActivity.csLibrary4A.setWedgeDeviceAddress(null);
    }
    void connectWedge(ReaderDevice readerDevice) {
        MainActivity.csLibrary4A.scanLeDevice(false);
        MainActivity.csLibrary4A.connect(readerDevice); bWedgeConnecting = true; bWedgeConnected = false; bUserRequestedDisconnect = false;
        buttonConnect.setText("Connecting");
        MainActivity.csLibrary4A.setWedgeDeviceName(readerDevice.getName()); MainActivity.csLibrary4A.setWedgeDeviceAddress(readerDevice.getAddress());
        MainActivity.csLibrary4A.setWedgeDeviceUUID2p1(readerDevice.getServiceUUID2p1());
    }

    Runnable runnableStartService = new Runnable() {
        boolean DEBUG = false;

        @Override
        public void run() {
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) = " + ActivityCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)  = " + ActivityCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions WRITE_EXTERNAL_STORAGE"); //
                    requestPermissions(new String[] { WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE }, 1);
                    //Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: WRITE_EXTERNAL_STORAGE is permitted"); ///
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle WRITE_EXTERNAL_STORAGE");

            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)  = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomAlertDialog appdialog = new CustomAlertDialog();
                    appdialog.Confirm(getActivity(), "Use your location",
                            "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
                            "No thanks", "Turn on",
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: allow permission in ACCESS_FINE_LOCATION handler");
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions ACCESS_FINE_LOCATION");
                                    requestPermissions(new String[] { ACCESS_FINE_LOCATION }, 123); //ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
                                    //if (false) Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                                    /*{
                                        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                                            MainActivity.csLibrary4A.appendToLog("popupAlert: StreamOut: start activity ACTION_LOCATION_SOURCE_SETTINGS");
                                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            mContext.startActivity(intent1);
                                        }
                                    }*/
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                    //bAlerting = false;
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: reject permission in ACCESS_FINE_LOCATION handler");
                                    //bAlerting = false;
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                }
                            });
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: started ACCESS_FINE_LOCATION handler");
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled ACCESS_FINE_LOCATION");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle ACCESS_FINE_LOCATION");

            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_SCAN)  = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
                    requestPermissions(new String[] { BLUETOOTH_SCAN, BLUETOOTH_CONNECT }, 123);
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled BLUETOOTH_CONNECT and BLUETOOTH_SCAN");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
        }
    };

    int runningMode = -1, connectWait = 0, scanWait = 0;
    Runnable runnableStart = new Runnable() {
        @Override
        public void run() {
            boolean bValue = true;
            if (MainActivity.csLibrary4A.isBleConnected()) {
                if (MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                    bWedgeConnecting = false;
                    if (bWedgeConnected == false) {
                        bWedgeConnected = true;
                        MainActivity.csLibrary4A.setPowerLevel(MainActivity.csLibrary4A.getWedgePower());
                        MainActivity.csLibrary4A.appendToLog("runnableStart: isBleConnected is true with mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
                        for (int i = 0; i < readersList.size(); i++) {
                            ReaderDevice readerDevice = readersList.get(i);
                            if (readerDevice.getSelected() && readerDevice.isConnected() == false) {
                                readerDevice.setConnected(true);
                                readersList.set(i, readerDevice);
                                readerListAdapter.notifyDataSetChanged();
                            }
                        }
                        if (getActivity().getPackageName().contains("cs710awedgeapp")) {
                            MainActivity.csLibrary4A.appendToLog("foregroundReader108: updated 2b");
                            MainActivity.csLibrary4A.setForegroundServiceEnable(true);
                            MainActivity.csLibrary4A.saveSetting2File();
                        }
                    }
                    buttonConnect.setText("Disconnect");
                } //else Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_not_ready), Toast.LENGTH_SHORT).show();
            } else if (bWedgeConnecting) {
                MainActivity.csLibrary4A.appendToLog("runnableStart: bConnecting is true");
            } else if (MainActivity.csLibrary4A.isBleScanning()) {
                MainActivity.csLibrary4A.appendToLog("runnableStart: isBleScanning is true");
                boolean listUpdated = false;
                if (++scanWait > 10) {
                    boolean bValue1 = MainActivity.csLibrary4A.scanLeDevice(false);
                    MainActivity.csLibrary4A.appendToLog("runnableStart: STOP scanning with result = " + bValue1);
                    scanWait = 0;
                    readersList.clear();
                    listUpdated = true;
                } else {
                    while (true) {
                        BluetoothGatt.CsScanData csScanData = MainActivity.csLibrary4A.getNewDeviceScanned();
                        if (csScanData != null) {
                            BluetoothGatt.CsScanData scanResultA = csScanData;
                            if (getActivity() == null) continue;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    continue;
                                }
                            } else if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                                continue;
                            }
                            if (DEBUG)
                                MainActivity.csLibrary4A.appendToLog("runnableStart: scanResultA.device.getType() = " + scanResultA.device.getType() + ". scanResultA.rssi = " + scanResultA.rssi);
                            if (/*scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE &&*/ (true || scanResultA.rssi < 0)) {
                                boolean match = false;
                                for (int i = 0; i < readersList.size(); i++) {
                                    if (readersList.get(i).getAddress().matches(scanResultA.device.getAddress())) {
                                        ReaderDevice readerDevice1 = readersList.get(i);
                                        int count = readerDevice1.getCount();
                                        count++;
                                        readerDevice1.setCount(count);
                                        readerDevice1.setRssi(scanResultA.rssi);
                                        readersList.set(i, readerDevice1);
                                        listUpdated = true;
                                        match = true;
                                        break;
                                    }
                                }
                                if (match == false) {
                                    ReaderDevice readerDevice = new ReaderDevice(scanResultA.device.getName(), scanResultA.device.getAddress(), false, "", 1, scanResultA.rssi, scanResultA.serviceUUID2p2);
                                    String strInfo = "";
                                    if (scanResultA.device.getBondState() == 12) {
                                        strInfo += "BOND_BONDED\n";
                                    }
                                    //readerDevice.setDetails(strInfo + "scanRecord=" + mCsLibrary4A.byteArrayToString(scanResultA.scanRecord));
                                    readersList.add(readerDevice);
                                    listUpdated = true;
                                }
                            } else {
                                if (DEBUG)
                                    MainActivity.csLibrary4A.appendToLog("runnableStart: deviceScanTask: rssi=" + scanResultA.rssi + ", error type=" + scanResultA.device.getType());
                            }
                            //scanWait = 0;
                        } else {
                            MainActivity.csLibrary4A.appendToLog("runnableStart: NO reader is found with scanWait = " + scanWait);
                            break;
                        }
                    }
                    if (listUpdated) readerListAdapter.notifyDataSetChanged();
                }
            } else {
                MainActivity.csLibrary4A.appendToLog("bbb 4");
                if (bWedgeConnected) {
                    bWedgeConnected = false;
                    MainActivity.csLibrary4A.appendToLog("bbb 4A");
                    readersList.clear(); readerListAdapter.notifyDataSetChanged();
                    buttonConnect.setText("Connect");
                }
                MainActivity.csLibrary4A.appendToLog("runnableStart: isBleScanning is FALSE");
                boolean bScanPermitted = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //that is android 12 or above
                    if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        bScanPermitted = false;
                        //MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
                        //requestPermissions(new String[] { BLUETOOTH_SCAN, BLUETOOTH_CONNECT }, 123);
                        MainActivity.csLibrary4A.appendToLog("runnableStart: CANNOT start scanLeDevice as BLUETOOTH_CONNECT && BLUETOOTH_SCAN is NOT yet permitted");
                    } else {
                        MainActivity.csLibrary4A.appendToLog("runnableStartService: BLUETOOTH_CONNECT and BLUETOOTH_SCAN and (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION) is permitted");
                    }
                } else {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
                    if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        bScanPermitted = false;
                        MainActivity.csLibrary4A.appendToLog("runnableStart: CANNOT start scanLeDevice as BLUETOOTH is NOT yet permitted");
                    } else {
                        MainActivity.csLibrary4A.appendToLog("runnableStartService: BLUETOOTH_CONNECT and BLUETOOTH_SCAN and (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION) is permitted");
                    }
                }
                if ((ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    bScanPermitted = false;
                    MainActivity.csLibrary4A.appendToLog("runnableStart: CANNOT start scanLeDevice as ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is NOT yet permitted");
                }
                bWedgeConnecting = false; connectWait = 0; scanWait = 0;
                boolean bValue1 = false;
                if (bScanPermitted) bValue1 = MainActivity.csLibrary4A.scanLeDevice(true);
                MainActivity.csLibrary4A.appendToLog("runnableStart: starting scanLeDevice is " + bValue1 + " with isScanning = " + MainActivity.csLibrary4A.isBleScanning());
            }
            handler.postDelayed(runnableStart, 1000);
        }
    };
    CustomAlertDialog appdialog;
    void popupAlert() {
        MainActivity.csLibrary4A.appendToLog("DirectWedgeFragment: entering popupAlert");
        if (appdialog != null && appdialog.isShowing()) {
            MainActivity.csLibrary4A.appendToLog("DirectWedgeFragment: skip popupAlert");
            return;
        }
        appdialog = new CustomAlertDialog();
        appdialog.Confirm(getActivity(), "Enable <" + stringLabelExpected + ">",
                "<" + stringLabelExpected + "> is not enabled. Click OK to open Languages & Input Settings. You will need to select <" + stringLabelExpected + "> in your current keyboard to use it",
                "No thanks", "OK",
                new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.csLibrary4A.appendToLog("Ok Procedure");
                        Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(enableIntent);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.csLibrary4A.appendToLog("Cancel procedure");
                        getActivity().finish();
                        //System.exit(0);
                    }
                });
        MainActivity.csLibrary4A.appendToLog("DirectWedgeFragment: EXIT popupAlert");
    }

    void updateCurrentIMEMatched() {
        if (true) {
            boolean bFound = false;
            List<InputMethodInfo> list = null;
            stringImeExpected = getActivity().getPackageName() + ".CustomIME";
            if (MainActivity.drawerPositionsDefault != DrawerListContent.DrawerPositions.MAIN) stringLabelExpected = getResources().getString(R.string.app_ime_simplewedge1);
            else if (getActivity().getPackageName().contains("cs710ademoapp")) stringLabelExpected = getResources().getString(R.string.app_ime_cs710);
            else stringLabelExpected = getResources().getString(R.string.app_ime_cs108);
            InputMethodManager inputMethodManager = inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);

            MainActivity.csLibrary4A.appendToLog("FEATURE_INPUT_METHODS is " + getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_INPUT_METHODS));
            list = inputMethodManager.getEnabledInputMethodList(); bFound = false;
            MainActivity.csLibrary4A.appendToLog("getCurrentInputMethodSubtype = " + inputMethodManager.getCurrentInputMethodSubtype().getLanguageTag());
            for (int i = 0; i < list.size(); i++) {
                MainActivity.csLibrary4A.appendToLog("enabled " + i + ": " + list.get(i).getServiceName()
                        + ", " + list.get(i).getPackageName()
                        + ", " + list.get(i).loadLabel(getActivity().getPackageManager()).toString()
                        + ", " + list.get(i).getIsDefaultResourceId());
                if (list.get(i).getServiceName().contains(stringImeExpected) && list.get(i).loadLabel(getActivity().getPackageManager()).toString().contains(stringLabelExpected)) {
                    bFound = true;
                    MainActivity.csLibrary4A.appendToLog("Found expected IME");
                }
            }

            if (bFound == false) {
                MainActivity.csLibrary4A.appendToLog("DirectWedgeFragment: No " + stringImeExpected + " is found");
                if (false) inputMethodManager.showInputMethodPicker();
                else popupAlert();
            } else if (true) {
                list = inputMethodManager.getInputMethodList(); bFound = false;
                for (int i = 0; i < list.size(); i++) {
                    MainActivity.csLibrary4A.appendToLog(i + ": " + list.get(i).getServiceName()
                            + ", " + list.get(i).getPackageName()
                            + ", " + list.get(i).loadLabel(getActivity().getPackageManager()).toString()
                            + ", " + list.get(i).getIsDefaultResourceId());

                    List<InputMethodSubtype> submethods = inputMethodManager.getEnabledInputMethodSubtypeList(list.get(i), true);
                    for (InputMethodSubtype submethod : submethods) {
                        if (submethod.getMode().equals("keyboard")) {
                            String currentLocale = submethod.getLocale();
                            MainActivity.csLibrary4A.appendToLog("Available input method locale: " + currentLocale);
                        }
                    }

                    if (list.get(i).getServiceName().contains(stringImeExpected) && list.get(i).loadLabel(getActivity().getPackageManager()).toString().contains(stringLabelExpected)) {
                        bFound = true;
                        MainActivity.csLibrary4A.appendToLog("Found expected IME with id = " + list.get(i).getId());
                        String idCurrent = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                        MainActivity.csLibrary4A.appendToLog("Found current IME with id = " + idCurrent);
                        if (list.get(i).getId().matches(idCurrent)) {
                            MainActivity.csLibrary4A.appendToLog("expected IME matches current IME");
                            bCurrentIMEmatched = true;
                        } else {
                            MainActivity.csLibrary4A.appendToLog("expected IME does NOT match current IME");
                            bCurrentIMEmatched = false;
                        }
/*
                        CustomIME customIME = new CustomIME();
                        InputMethodService inputMethodService = getActivity().get
                        customIME.switchInputMethod(list.get(i).getId());
*/
                        //String oldDefaultKeyboard = Settings.Secure.getString(resolver, Setting.Secure.DEFAULT_INPUT_METHOD);
                        //MainActivity.csLibrary4A.appendToLog("oldDefaultKeyboard = " + oldDefaultKeyboard);
            /*inputMethodManager.toggleSoftInputFromWindow(
                    linearLayout.getApplicationWindowToken(),
                    InputMethodManager.SHOW_FORCED, 0);*/

                        IBinder iBinder = getView().getWindowToken();
                        MainActivity.csLibrary4A.appendToLog("iBinder is " + (iBinder == null ? "null" : "valid"));
                        if (iBinder != null) inputMethodManager.setInputMethod(iBinder, list.get(i).getId());
                    }
                }
            }
        }
    }
}
