package com.csl.cs108ademoapp.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.InventoryBarcodeTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import java.util.Collections;

public class InventoryBarcodeFragment extends CommonFragment {
    private ListView barcodeListView;
    private TextView barcodeEmptyView;
    private TextView barcodeRunTime, barcodeVoltageLevel;
    private TextView barcodeYieldView, barcodeTotal;
    private Button button;

    MediaPlayer playerO, playerN;

    private ReaderListAdapter readerListAdapter;

    InventoryBarcodeTask inventoryBarcodeTask;

    void clearTagsList() {
        barcodeYieldView.setText(""); barcodeTotal.setText("");
        MainActivity.sharedObjects.barsList.clear();
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsList() {
        Collections.sort(MainActivity.sharedObjects.barsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void saveTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.barsList);
        saveExternalTask.execute();
    }
    void shareTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.barsList);
        String stringOutput = saveExternalTask.createStrEpcList();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sharing to"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_inventory_barcode, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAction_clear:
                clearTagsList();
                return true;
            case R.id.menuAction_sortRssi:
                //sortTagsListByRssi();
                return true;
            case R.id.menuAction_sort:
                sortTagsList();
                return true;
            case R.id.menuAction_save:
                saveTagsList();
                return true;
            case R.id.menuAction_share:
                shareTagsList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        barcodeListView = (ListView) getActivity().findViewById(R.id.inventoryBarcodeList);
        barcodeEmptyView = (TextView) getActivity().findViewById(R.id.inventoryBarcodeEmpty);
        barcodeListView.setEmptyView(barcodeEmptyView);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.barsList, true, false);
        barcodeListView.setAdapter(readerListAdapter);
        barcodeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        barcodeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReaderDevice readerDevice = readerListAdapter.getItem(position);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("Position  = " + position);
                if (readerDevice.getSelected()) {
                    readerDevice.setSelected(false);
                } else {
                    readerDevice.setSelected(true);
                }
                MainActivity.sharedObjects.barsList.set(position, readerDevice);
                if (readerDevice.getSelected()) MainActivity.tagSelected = readerDevice;
                else MainActivity.tagSelected = null;
                for (int i = 0; i < MainActivity.sharedObjects.barsList.size(); i++) {
                    if (i != position) {
                        ReaderDevice readerDevice1 = MainActivity.sharedObjects.barsList.get(i);
                        if (readerDevice1.getSelected()) {
                            readerDevice1.setSelected(false);
                            MainActivity.sharedObjects.barsList.set(i, readerDevice1);
                        }
                    }
                }
                readerListAdapter.notifyDataSetChanged();
            }
        });

        barcodeRunTime = (TextView) getActivity().findViewById(R.id.inventoryBarcodeRunTime);
        barcodeVoltageLevel = (TextView) getActivity().findViewById(R.id.inventoryBarcodeVoltageLevel);

        barcodeYieldView = (TextView) getActivity().findViewById(R.id.inventoryBarcodeYield);
        button = (Button) getActivity().findViewById(R.id.inventoryBarcodeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        barcodeTotal = (TextView) getActivity().findViewById(R.id.inventoryBarcodeTotal);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (true) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
        }
    }

    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        if (inventoryBarcodeTask != null) {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onDestory(): VALID inventoryBarcodeTask");
            inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.DESTORY;
        }
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment().onDestory(): onDestory()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setAutoBarStartSTop(false); MainActivity.csLibrary4A.setNotificationListener(null);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now VISIBLE");
            userVisibleHint = true;
            MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
        } else {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now INVISIBLE");
            userVisibleHint = false;
            MainActivity.csLibrary4A.setAutoBarStartSTop(false); MainActivity.csLibrary4A.setNotificationListener(null);
        }
    }

    public InventoryBarcodeFragment() {
        super("InventoryBarcodeFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new Cs108Library4A.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Start Barcode inventory");
            started = true;
            inventoryBarcodeTask = new InventoryBarcodeTask(MainActivity.sharedObjects.barsList, readerListAdapter, null, barcodeRunTime, barcodeVoltageLevel, barcodeYieldView, button, null, barcodeTotal, false);
            inventoryBarcodeTask.execute();
        } else {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Stop Barcode inventory");
            if (buttonTrigger) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
        }
    }
}
