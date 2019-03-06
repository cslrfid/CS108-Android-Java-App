package com.csl.cs108ademoapp.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.CustomProgressDialog;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Connector;
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
    private Cs108Library4A mCs108Library4a = MainActivity.mCs108Library4a;

    private ArrayList<Cs108Connector.Cs108ScanData> mScanResultList = new ArrayList<>();
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

        if (mCs108Library4a.isBleConnected() == false) readersList.clear();
        final ListView readerListView = (ListView) getActivity().findViewById(R.id.readersList);
        TextView readerEmptyView = (TextView) getActivity().findViewById(R.id.empty);
        readerListView.setEmptyView(readerEmptyView);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, readersList, true);
        readerListView.setAdapter(readerListAdapter);
        readerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        readerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCs108Library4a.appendToLog("bConnecting = " + bConnecting);
                if (bConnecting) return;

                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (readerDevice.getSelected()) {
                    if (mCs108Library4a.isBleConnected() && readerDevice.isConnected()) { mCs108Library4a.disconnect(false); mCs108Library4a.appendToLog("done"); }
                    readerDevice.setConnected(false);
                    readerDevice.setSelected(false);
                    readersList.set(position, readerDevice);
                } else {
                    if (mCs108Library4a.isBleConnected() == false) {
                        boolean validStart = false;
                        if (deviceConnectTask == null) {
                            validStart = true;
                        } else if (deviceConnectTask.getStatus() == AsyncTask.Status.FINISHED) {
                            validStart = true;
                        }
                        if (validStart) {
                            bConnecting = true;
                            readerDevice.setSelected(true);
                            if (deviceScanTask != null)  deviceScanTask.cancel(true);
                            MainActivity.mCs108Library4a.appendToLog("Connecting");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                deviceConnectTask = new DeviceConnectTask(position, readerDevice, "Connecting with " + readerDevice.getName());
                                deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                deviceConnectTask = new DeviceConnectTask(position, readerDevice, "Connecting with " + readerDevice.getName());
                                deviceConnectTask.execute();
                            }
                        }
                    }
                }
//                readersList.set(position, readerDevice);
                for (int i = 0; i < readersList.size(); i++) {
                    if (i != position) {
                        ReaderDevice readerDevice1 = readersList.get(i);
                        if (readerDevice1.getSelected()) {
                            readerDevice1.setSelected(false);
                            readersList.set(i, readerDevice1);
                        }
                    }
                }
                readerListAdapter.notifyDataSetChanged();
            }
        });
        if (mCs108Library4a.isBleConnected() == false) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_0:
//                MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.mRx000Setting.setOEMAddress(2);
//                MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.sendHostRegRequestHST_CMD(Cs108Connector.HostCommands.CMD_RDOEM);
                if (MainActivity.mCs108Library4a.isBleConnected()) MainActivity.mCs108Library4a.forceBTdisconnect();
                return true;

            case R.id.action_1:
//                MainActivity.mCs108Library4a.barcodeSendCommandSerialNumber();
//                MainActivity.mCs108Library4a.setBarcodeOn(false);
//                MainActivity.mCs108Library4a.hostProcessorICGetFirmwareVersion();
//                if (MainActivity.mCs108Library4a.isBleConnected()) MainActivity.mCs108Library4a.setVibrateOn(true);
                return true;

            case R.id.action_2:
                for (int i = 0; i < 300; i++) MainActivity.mCs108Library4a.setPowerLevel(i);
//                MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.mRx000Setting.setFreqChannelConfig(false);
//                MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.mRx000Setting.setFreqChannelSelect(3);
//                MainActivity.mCs108Library4a.mRfidDevice.mRx000Device.mRx000Setting.setFreqChannelConfig(true);
//                if (MainActivity.mCs108Library4a.isBleConnected()) MainActivity.mCs108Library4a.setVibrateOn(false);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ConnectionFragment() {
        super("ConnectionFragment");
    }

    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            boolean operating = false;
            if (mCs108Library4a.isBleConnected())   operating = true;
            if (operating == false && deviceScanTask != null) {
                if (deviceScanTask.isCancelled() == false)   operating = true;
            }
            if (operating == false && deviceConnectTask != null) {
                if (deviceConnectTask.isCancelled() == false)   operating = true;
            }
            if (operating == false) {
                deviceScanTask = new DeviceScanTask();
                deviceScanTask.execute(); mCs108Library4a.appendToLog("Started DeviceScanTask");
            }
            mHandler.postDelayed(checkRunnable, 5000);
        }
    };

    private class DeviceScanTask extends AsyncTask<Void, String, String> {
        private long timeMillisUpdate = System.currentTimeMillis();
        boolean usbDeviceFound = false;
        ArrayList<ReaderDevice> readersListOld = new ArrayList<ReaderDevice>();
        boolean wait4process = false; boolean scanning = false;

        @Override
        protected String doInBackground(Void... a) {
            while (isCancelled() == false) {
                if (wait4process == false) {
                    Cs108Connector.Cs108ScanData cs108ScanData = mCs108Library4a.getNewDeviceScanned();
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
            if (false) mCs108Library4a.appendToLog("Entry with mScanResultList.size() = " + mScanResultList.size());
            if (scanning == false) {
                scanning = true;
                if (mCs108Library4a.scanLeDevice(true) == false) cancel(true);
                else getActivity().invalidateOptionsMenu();
            }
            boolean listUpdated = false;
            while (mScanResultList.size() != 0) {
                Cs108Connector.Cs108ScanData scanResultA = mScanResultList.get(0);
                mScanResultList.remove(0);
                if (false) mCs108Library4a.appendToLog("scanResultA.device.getType() = " + scanResultA.device.getType() + ". scanResultA.rssi = " + scanResultA.rssi);
                if (scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE && scanResultA.rssi < 0) {
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
                        ReaderDevice readerDevice = new ReaderDevice(scanResultA.device, scanResultA.device.getName(), scanResultA.device.getAddress(), false, "", 1, scanResultA.rssi);
                        String strInfo = "";
                        if (scanResultA.device.getBondState() == 12) {
                            strInfo += "BOND_BONDED\n";
                        }
                        readerDevice.setDetails(strInfo + "scanRecord=" + mCs108Library4a.byteArrayToString(scanResultA.scanRecord));
                        readersList.add(readerDevice); listUpdated = true;
                    }
                } else {
                    if (DEBUG) mCs108Library4a.appendToLog("deviceScanTask: rssi=" + scanResultA.rssi + ", error type=" + scanResultA.device.getType());
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
                        ReaderDevice readerDevice1 = new ReaderDevice(null, null, readerDeviceNew.getAddress(), false, null, readerDeviceNew.getCount(), 0);
                        readersListOld.add(readerDevice1);
                    }
                }
                if (DEBUG) mCs108Library4a.appendToLog("Matched. Updated readerListOld with size = " + readersListOld.size());
                mCs108Library4a.scanLeDevice(false);
                getActivity().invalidateOptionsMenu();
                scanning = false;
            }
            if (listUpdated) readerListAdapter.notifyDataSetChanged();
            wait4process = false;
            if (false) mCs108Library4a.appendToLog("Exit....");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mCs108Library4a.appendToLog("Stop Scanning 1A");
            deviceScanEnding();
        }

        @Override
        protected void onPostExecute(String result) {
            mCs108Library4a.appendToLog("Stop Scanning 1B");
            deviceScanEnding();
        }

        void deviceScanEnding() {
            mCs108Library4a.scanLeDevice(false);
        }
    }

    long connectTimeMillis; boolean bConnecting = false;
    private class DeviceConnectTask extends AsyncTask<Void, String, Integer> {
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

            mCs108Library4a.connect(connectingDevice);
            waitTime = 20;
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
                if (mCs108Library4a.isBleConnected()) {
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
            if (true) mCs108Library4a.appendToLog("onCancelled(): setting = " + setting + ", waitTime = " + waitTime);
            if (setting >= 0) {
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_ble_setup_problem), Toast.LENGTH_SHORT).show();
            } else {
                mCs108Library4a.isBleConnected();
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.error_bluetooth_connection_failed), Toast.LENGTH_SHORT).show();
            }
            super.onCancelled();
            mCs108Library4a.disconnect(false); mCs108Library4a.appendToLog("done");

            bConnecting = false;
        }

        protected void onPostExecute(Integer result) {
            if (DEBUG) mCs108Library4a.appendToLog("onPostExecute(): setting = " + setting + ", waitTime = " + waitTime);
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
            MainActivity.mCs108Library4a.appendToLog("doing onBackPressed with mrfidToWriteSize = " + mCs108Library4a.mrfidToWriteSize());
            getActivity().onBackPressed();

            bConnecting = false;
        }
    }
}
