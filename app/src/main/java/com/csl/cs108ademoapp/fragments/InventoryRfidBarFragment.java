package com.csl.cs108ademoapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;

import com.csl.cs108ademoapp.AsyncTaskA;
import com.csl.cs108ademoapp.InventoryBarcodeTask;
import com.csl.cs108ademoapp.InventoryRfidTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;
import com.csl.cslibrary4a.RfidReaderChipData;

import java.util.Collections;
import java.util.Comparator;

public class InventoryRfidBarFragment extends CommonFragment {
    private ListView barcodeListView;
    private TextView barcodeEmptyView;
    private TextView barcodeRunTime, barcodeVoltageLevel;
    private TextView barcodeYieldView, barcodeTotal;
    private Button button;

    private ReaderListAdapter readerListAdapter;

    InventoryBarcodeTask inventoryBarcodeTask;
    InventoryRfidTask inventoryRfidTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_inventory_barcode, container, false);
    }

    void clearTagsList() {
        if (bRunningInventory) return;
        barcodeYieldView.setText(""); barcodeTotal.setText("");
        MainActivity.tagSelected = null;
        MainActivity.sharedObjects.tagsList.clear(); MainActivity.sharedObjects.tagsIndexList.clear();
        readerListAdapter.notifyDataSetChanged();
        MainActivity.mLogView.setText("");
    }
    void sortTagsList() {
        if (bRunningInventory) return;
        Collections.sort(MainActivity.sharedObjects.tagsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsListByRssi() {
        if (bRunningInventory) return;
        Collections.sort(MainActivity.sharedObjects.tagsList, new Comparator<ReaderDevice>() {
            @Override
            public int compare(ReaderDevice deviceTag, ReaderDevice t1) {
                if (deviceTag.getRssi() == t1.getRssi()) return 0;
                else if (deviceTag.getRssi() < t1.getRssi()) return 1;
                else return -1;
            }
        });
        readerListAdapter.notifyDataSetChanged();
    }
    void saveTagsList() {
        if (bRunningInventory) return;
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        saveExternalTask.execute();
    }
    void shareTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        String stringOutput = saveExternalTask.createStrEpcList();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sharing to"));
    }

    @Override
    public boolean onMenuItemSelectedA(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryRifdBarFragment.onMenuItemSelectedA");
        if (item.getItemId() == R.id.menuAction_clear) {
            clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            sortTagsListByRssi();
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
        MainActivity.csLibrary4A.appendToLog("InventoryRfidBarFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                onCreateMenuA(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        androidx.appcompat.app.ActionBar actionBar;
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_tagBarInventory);

        barcodeListView = (ListView) getActivity().findViewById(R.id.inventoryBarcodeList);
        barcodeEmptyView = (TextView) getActivity().findViewById(R.id.inventoryBarcodeEmpty);
        barcodeListView.setEmptyView(barcodeEmptyView);

        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.tagsList, false, true, false, false, false);

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
                MainActivity.sharedObjects.tagsList.set(position, readerDevice);
                if (readerDevice.getSelected()) MainActivity.tagSelected = readerDevice;
                else MainActivity.tagSelected = null;
                for (int i = 0; i < MainActivity.sharedObjects.tagsList.size(); i++) {
                    if (i != position) {
                        ReaderDevice readerDevice1 = MainActivity.sharedObjects.tagsList.get(i);
                        if (readerDevice1.getSelected()) {
                            readerDevice1.setSelected(false);
                            MainActivity.sharedObjects.tagsList.set(i, readerDevice1);
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

        MainActivity.csLibrary4A.getBarcodePreSuffix();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.csLibrary4A.setAutoBarStartSTop(true); setNotificationListener();
    }

    @Override
    public void onPause() {
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.csLibrary4A != null) {
            MainActivity.csLibrary4A.setAutoBarStartSTop(false);
            MainActivity.csLibrary4A.setNotificationListener(null);
        }
        super.onDestroy();
    }

    public InventoryRfidBarFragment() {
        super("InventoryRfidBarFragment");
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
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTaskA.Status.RUNNING) started = true;
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
            inventoryRfidTask = null;
            inventoryBarcodeTask = new InventoryBarcodeTask(null, readerListAdapter, null, barcodeRunTime, barcodeVoltageLevel, barcodeYieldView, button, null, barcodeTotal, false);
            inventoryBarcodeTask.execute();
            mHandler.post(runnable);
        } else if (inventoryBarcodeTask.getStatus() == AsyncTaskA.Status.RUNNING) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: Stop Barcode inventory");
            if (buttonTrigger) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (inventoryBarcodeTask.getStatus() == AsyncTaskA.Status.RUNNING) {
                MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryBarcodeTask running");
                mHandler.postDelayed(runnable, 100);
            } else if (inventoryRfidTask == null) {
                if (inventoryBarcodeTask.tagResult != null) {
                    MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryBarcodeTask finished with result");
                    //MainActivity.csLibrary4A.setPowerLevel(150);
                    MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY);
                    inventoryRfidTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0,
                            false, MainActivity.csLibrary4A.getInventoryBeep(), true,
                            null, null, RfidReader.TagType.TAG_NULL, null,
                            null, null,
                            null, barcodeVoltageLevel, null, button, null);
                    inventoryRfidTask.execute();
                    mHandler.postDelayed(runnable, 100);
                } else MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryBarcodeTask finished without result");
            } else if (inventoryRfidTask.getStatus() == AsyncTaskA.Status.RUNNING) {
                MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryRfidTask running");
                mHandler.postDelayed(runnable, 100);
            } else if (inventoryRfidTask.rx000pkgDataResult != null) {
                MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryRfidTask finished with result, EPC = " + MainActivity.csLibrary4A.byteArrayToString(inventoryRfidTask.rx000pkgDataResult.decodedEpc));
                ReaderDevice readerDevice = new ReaderDevice("", null, false, "", 1, 0);
                readerDevice.setName(inventoryBarcodeTask.tagResult);
                readerDevice.setAddress(MainActivity.csLibrary4A.byteArrayToString(inventoryRfidTask.rx000pkgDataResult.decodedEpc));
                readerDevice.setRssi(inventoryRfidTask.rx000pkgDataResult.decodedRssi);
                MainActivity.sharedObjects.tagsList.add(0, readerDevice);
                readerListAdapter.notifyDataSetChanged();
            } else MainActivity.csLibrary4A.appendToLog("InventoryRfidBarInventory.runnable: with inventoryRfidTask finished without result");
        }
    };

    boolean bRunningInventory = false;
}
