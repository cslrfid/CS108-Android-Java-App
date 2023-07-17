package com.csl.cs108ademoapp.fragments;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.DrawerListContent;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108ademoapp.BuildConfig;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.CustomAlertDialog;
import com.csl.cs108library4a.ReaderDevice;

import java.util.ArrayList;
import java.util.List;

public class DirectWedgeFragment extends CommonFragment {
    Button buttonConnect;
    private ArrayList<ReaderDevice> readersList = MainActivity.sharedObjects.readersList;
    Handler handler = new Handler();
    private ReaderListAdapter readerListAdapter;
    private Cs108Library4A mCsLibrary4A = MainActivity.csLibrary4A;
    boolean bConnecting = false;
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

        if (true) {
            androidx.appcompat.app.ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_access);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF0000"));
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setTitle("CSL Java Simple Wedge v" + BuildConfig.VERSION_NAME);
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
                if (DEBUG) mCsLibrary4A.appendToLog("OnItemClickListener: bConnecting = " + bConnecting + ", position = " + position);
                if (bConnecting) return;

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

                if (DEBUG) mCsLibrary4A.appendToLog("OnItemClickListener: readerDevice.getSelected = " + readerDevice.getSelected());
                if (mCsLibrary4A.isBleConnected() && readerDevice.getSelected() == false && readerDevice.isConnected()) {
                    if (DEBUG) mCsLibrary4A.appendToLog("OnItemClickListener: going to disconnect");
                    disconnectWedge();
                } else if (mCsLibrary4A.isBleConnected() == false && readerDevice.getSelected()) {
                    if (DEBUG) mCsLibrary4A.appendToLog("OnItemClickListener: going to CONNECT");
                    if (true) connectWedge(readerDevice);
                }
            }
        });

        Button buttonSetup = (Button) getActivity().findViewById(R.id.directWedgeButtonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCsLibrary4A.isBleConnected() || true) new SettingWedgeFragment().show(getChildFragmentManager(), "TAG");
                else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_not_ready), Toast.LENGTH_SHORT).show();
                else Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_not_connected), Toast.LENGTH_SHORT).show();
            }
        });

        buttonConnect = (Button) getActivity().findViewById(R.id.directWedgeButtonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bConnecting) return;
                if (mCsLibrary4A.isBleConnected()) {
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
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCsLibrary4A.isBleConnected()) {
                    if (mCsLibrary4A.mrfidToWriteSize() == 0) {
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
                CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
                String stringInfo = "Hello. Here is the instruction....";
                customPopupWindow.popupStart(stringInfo, false);
            }
        });

        handler.post(runnableStart);
    }

    @Override
    public void onResume() {
        super.onResume();

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

    void disconnectWedge() {
        mCsLibrary4A.disconnect(false); bleDisConnecting = true; bConnecting = false;
        readersList.clear();
        buttonConnect.setText("Connect");
        handler.removeCallbacks(runnableStart); handler.postDelayed(runnableStart, 2000);
    }
    void connectWedge(ReaderDevice readerDevice) {
        MainActivity.csLibrary4A.scanLeDevice(false);
        mCsLibrary4A.connect(readerDevice); bConnecting = true;
        buttonConnect.setText("Connecting");
    }

    int runningMode = -1, connectWait = 0, scanWait = 0;
    Runnable runnableStart = new Runnable() {
        @Override
        public void run() {
            boolean bValue = true;
            if (MainActivity.csLibrary4A.isBleConnected()) {
                if (MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
                    MainActivity.csLibrary4A.setPowerLevel(MainActivity.wedgePower);
                    MainActivity.csLibrary4A.appendToLog("isBleConnected is true with mrfidToWriteSize = " + mCsLibrary4A.mrfidToWriteSize());
                    bConnecting = false;
                    for (int i = 0; i < readersList.size(); i++) {
                        ReaderDevice readerDevice = readersList.get(i);
                        if (readerDevice.getSelected() && readerDevice.isConnected() == false) {
                            readerDevice.setConnected(true);
                            readersList.set(i, readerDevice);
                            readerListAdapter.notifyDataSetChanged();
                        }
                    }
                    buttonConnect.setText("Disconnect");
                    return;
                } //else Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_not_ready), Toast.LENGTH_SHORT).show();
            } else if (bConnecting) {
                MainActivity.csLibrary4A.appendToLog("bConnecting is true");
            } else if (MainActivity.csLibrary4A.isBleScanning()) {
                MainActivity.csLibrary4A.appendToLog("isBleScanning is true");
                boolean listUpdated = false;
                if (false && ++scanWait > 10) {
                    scanWait = 0;
                    readersList.clear(); listUpdated = true;
                }
                while (true) {
                    Cs108Library4A.Cs108ScanData cs108ScanData = MainActivity.csLibrary4A.getNewDeviceScanned();
                    if (cs108ScanData != null) {
                        Cs108Library4A.Cs108ScanData scanResultA = cs108ScanData;
                        if (getActivity() == null) continue;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) continue;
                        } else if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) continue;
                        if (DEBUG) mCsLibrary4A.appendToLog("scanResultA.device.getType() = " + scanResultA.device.getType() + ". scanResultA.rssi = " + scanResultA.rssi);
                        if (scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE && (true || scanResultA.rssi < 0)) {
                            boolean match = false;
                            for (int i = 0; i < readersList.size(); i++) {
                                if (readersList.get(i).getAddress().matches(scanResultA.device.getAddress())) {
                                    ReaderDevice readerDevice1 = readersList.get(i);
                                    int count = readerDevice1.getCount();
                                    count++;
                                    readerDevice1.setCount(count);
                                    readerDevice1.setRssi(scanResultA.rssi);
                                    readersList.set(i, readerDevice1); listUpdated = true;
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
                                readerDevice.setDetails(strInfo + "scanRecord=" + mCsLibrary4A.byteArrayToString(scanResultA.scanRecord));
                                readersList.add(readerDevice); listUpdated = true;
                            }
                        } else {
                            if (DEBUG) mCsLibrary4A.appendToLog("deviceScanTask: rssi=" + scanResultA.rssi + ", error type=" + scanResultA.device.getType());
                        }
                    } else break;
                }
                if (listUpdated) readerListAdapter.notifyDataSetChanged();
            } else {
                MainActivity.csLibrary4A.appendToLog("isBleScanning is FALSE");
                bConnecting = false; connectWait = 0; scanWait = 0;
                boolean bValue1 = MainActivity.csLibrary4A.scanLeDevice(true);
                MainActivity.csLibrary4A.appendToLog("runnableStart: starting scanLeDevice is " + bValue1 + " with isScanning = " + MainActivity.csLibrary4A.isBleScanning());
            }
            handler.postDelayed(runnableStart, 1000);
        }
    };
    void popupAlert() {
        CustomAlertDialog appdialog = new CustomAlertDialog();
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
    }

    void updateCurrentIMEMatched() {
        if (true) {
            boolean bFound = false;
            List<InputMethodInfo> list = null;
            stringImeExpected = "com.csl.cs108ademoapp.CustomIME";
            if (MainActivity.drawerPositionsDefault != DrawerListContent.DrawerPositions.MAIN) stringLabelExpected = getResources().getString(R.string.app_ime1);
            else stringLabelExpected = getResources().getString(R.string.app_ime);
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
                MainActivity.csLibrary4A.appendToLog("No " + stringImeExpected + " is found");
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
                        String idCurrent = Settings.Secure.getString(MainActivity.mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
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
