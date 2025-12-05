package com.csl.cs108ademoapp.fragments;

import android.content.Intent;
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

import androidx.annotation.NonNull;

import com.csl.cslibrary4a.CustomAsyncTask;
import com.csl.cs108ademoapp.InventoryBarcodeTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;

import java.util.Collections;

public class InventoryBarcodeFragment extends CommonFragment {
    private ListView barcodeListView;
    private TextView barcodeEmptyView;
    private TextView barcodeRunTime, barcodeVoltageLevel;
    private TextView barcodeYieldView, barcodeTotal;
    private Button button;

    private ReaderListAdapter readerListAdapter;

    InventoryBarcodeTask inventoryBarcodeTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_inventory_barcode, container, false);
    }

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
    public boolean onMenuItemSelectedA(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment.onMenuItemSelectedA");
        if (item.getItemId() == R.id.menuAction_clear) {
            clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            //sortTagsListByRssi();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sort) {
            sortTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_save) {
            saveTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_share) {
            shareTagsList();
            return true;
        } else return super.onMenuItemSelectedA(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeListView = (ListView) view.findViewById(R.id.inventoryBarcodeList);
        barcodeEmptyView = (TextView) view.findViewById(R.id.inventoryBarcodeEmpty);
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
                //if (readerDevice.getSelected()) MainActivity.tagSelected = readerDevice;
                //else MainActivity.tagSelected = null;
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

        barcodeRunTime = (TextView) view.findViewById(R.id.inventoryBarcodeRunTime);
        barcodeVoltageLevel = (TextView) view.findViewById(R.id.inventoryBarcodeVoltageLevel);

        barcodeYieldView = (TextView) view.findViewById(R.id.inventoryBarcodeYield);
        button = (Button) view.findViewById(R.id.inventoryBarcodeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        barcodeTotal = (TextView) view.findViewById(R.id.inventoryBarcodeTotal);

        MainActivity.csLibrary4A.getBarcodePreSuffix();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint2(true);
    }

    @Override
    public void onPause() {
        if (inventoryBarcodeTask != null) {
            if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment.onPause: taskCancelReason as DESTORY");
            inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.DESTORY;
        }
        setUserVisibleHint2(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.csLibrary4A != null) {
            MainActivity.csLibrary4A.setAutoBarStartSTop(false);
            MainActivity.csLibrary4A.setNotificationListener(null);
        }
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if(getUserVisibleHint()) {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now VISIBLE");
            userVisibleHint = true;
            MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
        } else {
            MainActivity.csLibrary4A.appendToLog("InventoryBarcodeFragment is now INVISIBLE");
            userVisibleHint = false;
            if (inventoryBarcodeTask != null) {
                inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.DESTORY;
            }
            MainActivity.csLibrary4A.setAutoBarStartSTop(false); MainActivity.csLibrary4A.setNotificationListener(null);
        }
    }

    public InventoryBarcodeFragment() {
        super("InventoryBarcodeFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.context, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == CustomAsyncTask.Status.RUNNING) started = true;
        /*
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        */
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                Toast.makeText(MainActivity.context, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Start Barcode inventory");
            inventoryBarcodeTask = new InventoryBarcodeTask(MainActivity.sharedObjects.barsList, readerListAdapter, null, barcodeRunTime, barcodeVoltageLevel, barcodeYieldView, button, null, barcodeTotal, false);
            inventoryBarcodeTask.execute();
        } else {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Stop Barcode inventory");
            if (button.getText().toString().toUpperCase().matches("START")) {
                if (!buttonTrigger) MainActivity.csLibrary4A.barcodeInventory(true);
                inventoryBarcodeTask.pseudoStop = false;
                button.setText("Stop");
            } else if (!MainActivity.csLibrary4A.getTriggerButtonStatus()) {
                if (!buttonTrigger) MainActivity.csLibrary4A.barcodeInventory(false);
                inventoryBarcodeTask.pseudoStop = true;
                button.setText("Start");
            }
            /*
            if (buttonTrigger) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
            */
        }
    }
}
