package com.csl.cs108ademoapp.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.CustomProgressDialog;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;

import java.util.ArrayList;

public class ConnectionFragment extends CommonFragment {
    private DeviceScanTask deviceScanTask;
    private ReaderListAdapter readerListAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;
    private ArrayList<ReaderDevice> readersList = MainActivity.sharedObjects.readersList;
    private Cs108Library4A mCsLibrary4A = MainActivity.csLibrary4A;

    private ArrayList<Cs108Library4A.Cs108ScanData> mScanResultList = new ArrayList<>();
    private Handler mHandler = new Handler();
    private DeviceConnectTask deviceConnectTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_rdl);
        actionBar.setTitle(R.string.title_activity_connection);

        TextView textview = (TextView) getActivity().findViewById(R.id.connection_warning);
        MainActivity.csLibrary4A.appendToLog("getActivity().getPackageName() = " + getActivity().getPackageName());
        if (getActivity().getPackageName().contains("com.csl.cs710ademoapp")) textview.setVisibility(View.VISIBLE);

        if (mCsLibrary4A.isBleConnected() == false) readersList.clear();
        final ListView readerListView = (ListView) getActivity().findViewById(R.id.readersList);
        TextView readerEmptyView = (TextView) getActivity().findViewById(R.id.empty);
        readerListView.setEmptyView(readerEmptyView);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, readersList, true,  true);
        readerListView.setAdapter(readerListAdapter);
        readerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        readerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean DEBUG = false;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bConnecting) return;

                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (DEBUG) mCsLibrary4A.appendToLog("ConnectionFragment.OnItemClickListener: bConnecting = " + bConnecting + ", postion = " + position);
                boolean bSelectOld = readerDevice.getSelected();

                if (mCsLibrary4A.isBleConnected() && readerDevice.isConnected() && (readerDevice.getSelected() || false)) {
                    mCsLibrary4A.disconnect(false); bleDisConnecting = true;
                    readersList.clear();
                } else if (mCsLibrary4A.isBleConnected() == false && readerDevice.getSelected() == false) {
                    boolean validStart = false;
                    if (deviceConnectTask == null) {
                        validStart = true;
                    } else if (deviceConnectTask.getStatus() == AsyncTask.Status.FINISHED) {
                        validStart = true;
                    }
                    if (validStart) {
                        bConnecting = true;
                        if (deviceScanTask != null) deviceScanTask.cancel(true);
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("ConnectionFragment.OnItemClickListener: Connecting");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            deviceConnectTask = new DeviceConnectTask(position, readerDevice, "Connecting with " + readerDevice.getName());
                            deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            deviceConnectTask = new DeviceConnectTask(position, readerDevice, "Connecting with " + readerDevice.getName());
                            deviceConnectTask.execute();
                        }
                    }
                }

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
            }
        });
        if (mCsLibrary4A.isBleConnected() == false) {
            for (int i = 0; i < readersList.size(); i++) {
                ReaderDevice readerDevice1 = readersList.get(i);
                if (readerDevice1.isConnected()) {
                    readerDevice1.setConnected(false);
                    readersList.set(i, readerDevice1);
                }
            }
        }
        readerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        mHandler.post(checkRunnable);
        super.onResume();
    }

    @Override
    public void onStop() {
        mHandler.removeCallbacks(checkRunnable);
        if (deviceScanTask != null) {
            deviceScanTask.cancel(true);
        }
        if (deviceConnectTask != null) {
            deviceConnectTask.cancel(true);
        }
        super.onStop();
    }

    public ConnectionFragment() {
        super("ConnectionFragment");
    }

    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            boolean operating = false;
            if (mCsLibrary4A.isBleConnected())   operating = true;
            if (operating == false && deviceScanTask != null) {
                if (deviceScanTask.isCancelled() == false)   operating = true;
            }
            if (operating == false && deviceConnectTask != null) {
                if (deviceConnectTask.isCancelled() == false)   operating = true;
            }
            if (operating == false) {
                deviceScanTask = new DeviceScanTask();
                deviceScanTask.execute();
            }
            mHandler.postDelayed(checkRunnable, 5000);
        }
    };

    private class DeviceScanTask extends AsyncTask<Void, String, String> {
        private long timeMillisUpdate = System.currentTimeMillis();
        ArrayList<ReaderDevice> readersListOld = new ArrayList<ReaderDevice>();
        boolean wait4process = false; boolean scanning = false, DEBUG = false;

        @Override
        protected String doInBackground(Void... a) {
            while (isCancelled() == false) {
                if (wait4process == false) {
                    Cs108Library4A.Cs108ScanData cs108ScanData = mCsLibrary4A.getNewDeviceScanned();
                    if (cs108ScanData != null) mScanResultList.add(cs108ScanData);
                    if (scanning == false || mScanResultList.size() != 0 || System.currentTimeMillis() - timeMillisUpdate > 10000) {
                        wait4process = true; publishProgress("");
                    }
                }
            }
            return "End of Asynctask()";
        }

        @Override
        protected void onProgressUpdate(String... output) {
            if (scanning == false) {
                scanning = true;
                if (mCsLibrary4A.scanLeDevice(true) == false) cancel(true);
                else getActivity().invalidateOptionsMenu();
            }
            boolean listUpdated = false;
            while (mScanResultList.size() != 0) {
                Cs108Library4A.Cs108ScanData scanResultA = mScanResultList.get(0);
                mScanResultList.remove(0);
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
            }
            if (System.currentTimeMillis() - timeMillisUpdate > 10000) {
                timeMillisUpdate = System.currentTimeMillis();
                for (int i = 0; i < readersList.size(); i++) {
                    ReaderDevice readerDeviceNew = readersList.get(i);
                    boolean matched = false;
                    for (int k = 0; k < readersListOld.size(); k++) {
                        ReaderDevice readerDeviceOld = readersListOld.get(k);
                        if (readerDeviceOld.getAddress().matches(readerDeviceNew.getAddress())) {
                            matched = true;
                            if (readerDeviceOld.getCount() >= readerDeviceNew.getCount()) {
                                readersList.remove(i); listUpdated = true;
                                readersListOld.remove(k);
                            } else readerDeviceOld.setCount(readerDeviceNew.getCount());
                            break;
                        }
                    }
                    if (matched == false) {
                        ReaderDevice readerDevice1 = new ReaderDevice(null, readerDeviceNew.getAddress(), false, null, readerDeviceNew.getCount(), 0);
                        readersListOld.add(readerDevice1);
                    }
                }
                if (DEBUG) mCsLibrary4A.appendToLog("Matched. Updated readerListOld with size = " + readersListOld.size());
                mCsLibrary4A.scanLeDevice(false);
                getActivity().invalidateOptionsMenu();
                scanning = false;
            }
            if (listUpdated) readerListAdapter.notifyDataSetChanged();
            wait4process = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (DEBUG) mCsLibrary4A.appendToLog("Stop Scanning 1A");
            deviceScanEnding();
        }

        @Override
        protected void onPostExecute(String result) {
            if (DEBUG) mCsLibrary4A.appendToLog("Stop Scanning 1B");
            deviceScanEnding();
        }

        void deviceScanEnding() {
            mCsLibrary4A.scanLeDevice(false);
        }
    }

    long connectTimeMillis; boolean bConnecting = false;
    private class DeviceConnectTask extends AsyncTask<Void, String, Integer> {
        boolean DEBUG = false;
        private int position;
        private final ReaderDevice connectingDevice;
        private String prgressMsg;
        int waitTime;
        private CustomProgressDialog progressDialog;
        private int setting;

        DeviceConnectTask(int position, ReaderDevice connectingDevice, String prgressMsg) {
            this.position = position;
            this.connectingDevice = connectingDevice;
            this.prgressMsg = prgressMsg;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (DEBUG) MainActivity.csLibrary4A.appendToLog("start of Connection with mrfidToWriteSize = " + mCsLibrary4A.mrfidToWriteSize());
            mCsLibrary4A.connect(connectingDevice);
            waitTime = 30;
            setting = -1;
            progressDialog = new CustomProgressDialog(getActivity(), prgressMsg);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... a) {
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress("kkk ");
                if (mCsLibrary4A.isBleConnected()) {
                    setting = 0; break;
                }
            } while (--waitTime > 0);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            if (setting != 0 || waitTime <= 0) {
                cancel(true);
            }
            publishProgress("mmm ");
            return waitTime;
        }

        @Override
        protected void onProgressUpdate(String... output) {
        }

        @Override
        protected void onCancelled(Integer result) {
            if (DEBUG) mCsLibrary4A.appendToLog("onCancelled(): setting = " + setting + ", waitTime = " + waitTime);
            if (setting >= 0) {
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_setup_problem), Toast.LENGTH_SHORT).show();
            } else {
                mCsLibrary4A.isBleConnected();
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.error_bluetooth_connection_failed), Toast.LENGTH_SHORT).show();
            }
            super.onCancelled();
            mCsLibrary4A.disconnect(false); bleDisConnecting = true;

            bConnecting = false;
        }

        protected void onPostExecute(Integer result) {
            if (DEBUG) mCsLibrary4A.appendToLog("onPostExecute(): setting = " + setting + ", waitTime = " + waitTime);
            ReaderDevice readerDevice = readersList.get(position);
            readerDevice.setConnected(true);
            readersList.set(position, readerDevice);
            readerListAdapter.notifyDataSetChanged();

            String connectedBleAddress = connectingDevice.getAddress();
            if (connectedBleAddress.matches(MainActivity.sharedObjects.connectedBleAddressOld) == false)   MainActivity.sharedObjects.versioinWarningShown = false;
            MainActivity.sharedObjects.connectedBleAddressOld = connectedBleAddress;
            MainActivity.sharedObjects.barsList.clear();
            MainActivity.sharedObjects.tagsList.clear();
            MainActivity.sharedObjects.tagsIndexList.clear();

            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_connected), Toast.LENGTH_SHORT).show();

            connectTimeMillis = System.currentTimeMillis();
            super.onPostExecute(result);

            if (DEBUG) MainActivity.csLibrary4A.appendToLog("ConnectionFragment: onPostExecute: getActivity().onBackPressed");
            getActivity().onBackPressed();
            bConnecting = false;
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("end of Connection with mrfidToWriteSize = " + mCsLibrary4A.mrfidToWriteSize());
        }
    }
}
