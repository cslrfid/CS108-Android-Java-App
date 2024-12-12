package com.csl.cs108ademoapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.SharedObjects;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InventoryRfidSimpleFragment extends CommonFragment {
    final private boolean bAdd2End = false;
    int vibrateTimeBackup = 0;

    private ListView rfidListView;
    private TextView rfidEmptyView;
    private TextView rfidRunTime;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button, buttonShow;

    private ReaderListAdapter readerListAdapter;

    void clearTagsList() {
        if (bRunningInventory) return;
        rfidYieldView.setText("");
        rfidRateView.setText("");
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_inventory_rfid_simple, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity.selectFor = -1;
        if (true) {
            androidx.appcompat.app.ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_inv);
            actionBar.setTitle(R.string.title_activity_simpleInventory);
        }

        rfidListView = (ListView) getActivity().findViewById(R.id.simpleInventoryinventoryRfidList1);
        rfidEmptyView = (TextView) getActivity().findViewById(R.id.simpleInventoryRfidEmpty1);
        rfidListView.setEmptyView(rfidEmptyView);
        boolean bSelect4detail = true;

        boolean needDupElim = true;
        boolean need4Extra1 = MainActivity.csLibrary4A.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = false;
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);

        rfidListView.setAdapter(readerListAdapter);
        rfidListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        rfidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        rfidRunTime = (TextView) getActivity().findViewById(R.id.simpleInventoryRfidRunTime1);
        TextView rfidFilterOn = (TextView) getActivity().findViewById(R.id.SimpleInventoryRfidFilterOn1);
        if (MainActivity.csLibrary4A.getSelectEnable() == false && MainActivity.csLibrary4A.getInvMatchEnable() == false && MainActivity.csLibrary4A.getRssiFilterEnable() == false )
            rfidFilterOn.setVisibility(View.INVISIBLE);

        rfidYieldView = (TextView) getActivity().findViewById(R.id.simpleInventoryRfidYield1);
        rfidRateView = (TextView) getActivity().findViewById(R.id.simpleInventoryRfidRate1);

        button = (Button) getActivity().findViewById(R.id.simpleInventoryRfidButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        buttonShow = (Button) getActivity().findViewById(R.id.simpleInventoryRfidButtonShow);
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonShow.setVisibility(View.GONE);
                inventoryHandler_tag();
            }
        });

        vibrateTimeBackup = MainActivity.csLibrary4A.getVibrateTime();
    }

    @Override
    public void onResume() {
        super.onResume();
        setNotificationListener();
    }

    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(runnableCheckReady);
        MainActivity.csLibrary4A.setNotificationListener(null);
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.setInvBrandId(false);
        MainActivity.csLibrary4A.setVibrateTime(vibrateTimeBackup);
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidSimpleFragment().onDestory(): onDestory()");
        super.onDestroy();
    }

    public static InventoryRfidSimpleFragment newInstance(boolean bMultiBank, String mDid) {
        InventoryRfidSimpleFragment myFragment = new InventoryRfidSimpleFragment();

        Bundle args = new Bundle();
        args.putBoolean("bMultiBank", bMultiBank);
        args.putString("mDid", mDid);
        myFragment.setArguments(args);

        return myFragment;
    }
    public InventoryRfidSimpleFragment() {
        super("InventoryRfidSimpleFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.csLibrary4A.appendToLog("TRIGGER key is pressed.");
                startStopHandler(true);
            }
        });
    }
    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        else MainActivity.csLibrary4A.appendToLog("TriggerButton is pressed");

        boolean started = false;
        if (bRunningInventory) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        MainActivity.csLibrary4A.appendToLog("started = " + started);
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                mHandler.post(runnableCheckReady);
                return;
            }

            if (true) {
                EditText editText = (EditText) getActivity().findViewById(R.id.simpleInventoryTagPopulation);
                int iValue = Integer.valueOf(editText.getText().toString(), 16);
                MainActivity.csLibrary4A.setPopulation(iValue);

                editText = (EditText) getActivity().findViewById(R.id.simpleInventoryTagTargetNumber);
                iTagTarget = Integer.valueOf(editText.getText().toString());
                iTagGot = 0;

                uplinkPacketList.clear();

                timeMillis = System.currentTimeMillis(); startTimeMillis = timeMillis; runTimeMillis = timeMillis;
                total = 0;
            }

            MainActivity.sharedObjects.tagsList.clear(); MainActivity.sharedObjects.tagsIndexList.clear();
            rfidYieldView.setText("");
            rfidRateView.setText("");
            button.setText("Stop");
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            else rfidListView.setSelection(0);

            MainActivity.csLibrary4A.appendToLog("setVibrateOn i 2 with getInventoryVibrate as " + MainActivity.csLibrary4A.getInventoryVibrate() + ", getVibrateModeSetting as " + MainActivity.csLibrary4A.getVibrateModeSetting());
            if (MainActivity.csLibrary4A.getInventoryVibrate() && MainActivity.csLibrary4A.getVibrateModeSetting() == 1) {
                MainActivity.csLibrary4A.setVibrateOn(2);
            }
            //else MainActivity.csLibrary4A.setVibrateOn(0);

            startInventoryTask();
            bRunningInventory = true;
            myHandler.post(runnableSimpleInentory);
        } else {
            MainActivity.csLibrary4A.abortOperation();
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            if (true) { bRunningInventory = false; }
        }
    }

    boolean bRunningInventory = false;
    int total;
    long timeMillis, startTimeMillis, runTimeMillis, rateTimeMillis;
    int iTagTarget, iTagGot;
    Handler myHandler = new Handler(Looper.getMainLooper());
    ArrayList<RfidReaderChipData.Rx000pkgData> uplinkPacketList = new ArrayList<>();
    Runnable runnableSimpleInentory = new Runnable() {
        @Override
        public void run() {
            RfidReaderChipData.Rx000pkgData uplinkPacket;
            if (MainActivity.csLibrary4A.isBleConnected() && bRunningInventory) {
                while (MainActivity.csLibrary4A.mrfidToWriteSize() == 0 && (iTagTarget == 0 || iTagGot < iTagTarget)) {
                    if (System.currentTimeMillis() > runTimeMillis + 1000) {
                        runTimeMillis = System.currentTimeMillis();
                        long timePeriod = (System.currentTimeMillis() - startTimeMillis) / 1000;
                        if (timePeriod > 0) {
                            if (rfidRunTime != null) rfidRunTime.setText(String.format("Run time: %d sec", timePeriod));
                        }
                    }
                    uplinkPacket = MainActivity.csLibrary4A.onRFIDEvent();
                    if (uplinkPacket == null) break;
                    else {
                        uplinkPacketList.add(uplinkPacket);
                        iTagGot++;
                        mHandler.postDelayed(runnableStartBeep, 250);
                    }
                }
                if (iTagTarget != 0 && iTagGot >= iTagTarget) {
                    MainActivity.csLibrary4A.abortOperation();
                    bRunningInventory = false;
                }

                if (iTagGot != 0) {
                    rfidYieldView.setText("Total:" + iTagGot);

                    long tagRate = MainActivity.csLibrary4A.getTagRate();
                    String strRate = ""; boolean bUpdateRate = false;
                    if (tagRate >= 0) {
                        strRate = String.valueOf(tagRate);
                        bUpdateRate = true;
                    } else {
                        if (System.currentTimeMillis() - rateTimeMillis > 1500) {
                            strRate = "___";
                            bUpdateRate = true;
                        }
                    }
                    if (bUpdateRate) {
                        rfidRateView.setText("Rate:" + strRate);
                        rateTimeMillis = System.currentTimeMillis();
                    }
                }
                myHandler.postDelayed(runnableSimpleInentory, 200);
            } else {
                bRunningInventory = false;
                button.setText("Start"); buttonShow.setVisibility(View.VISIBLE);
            }
        }
    };

    void inventoryHandler_tag() {
        double rssi = 0;
        int phase = -1, chidx = -1, port = -1;
        int total = 0;

        while (uplinkPacketList.size() != 0) {
            RfidReaderChipData.Rx000pkgData uplinkPacket = uplinkPacketList.get(0);
            uplinkPacketList.remove(0);
            RfidReaderChipData.Rx000pkgData tagData = uplinkPacket;

            boolean match = false;
            total++;
            rssi = uplinkPacket.decodedRssi;
            phase = uplinkPacket.decodedPhase;
            chidx = uplinkPacket.decodedChidx;
            port = uplinkPacket.decodedPort;

            ReaderDevice deviceTag = null;
            int iMatchItem = -1;
            if (true) {
                int index = Collections.binarySearch(MainActivity.sharedObjects.tagsIndexList, new SharedObjects.TagsIndex(MainActivity.csLibrary4A.byteArrayToString(tagData.decodedEpc), 0));
                if (index >= 0) {
                    iMatchItem = MainActivity.sharedObjects.tagsIndexList.size() - 1 - MainActivity.sharedObjects.tagsIndexList.get(index).getPosition();
                }
            }
            if (iMatchItem >= 0) {
                deviceTag = MainActivity.sharedObjects.tagsList.get(iMatchItem);
                int count = deviceTag.getCount();
                count++;
                deviceTag.setCount(count);
                deviceTag.setRssi(rssi);
                deviceTag.setPhase(phase);
                deviceTag.setChannel(chidx);
                deviceTag.setPort(port);
                MainActivity.sharedObjects.tagsList.set(iMatchItem, deviceTag);
                match = true;
            }
            if (match == false) {
                deviceTag = new ReaderDevice("", MainActivity.csLibrary4A.byteArrayToString(uplinkPacket.decodedEpc), false, null,
                        MainActivity.csLibrary4A.byteArrayToString(uplinkPacket.decodedPc),
                        null,
                        (uplinkPacket.decodedCrc != null ? MainActivity.csLibrary4A.byteArrayToString(uplinkPacket.decodedCrc) : null),
                        null, null, 0, 0, null, 0, 0,null, null, null, null, 1,
                        rssi, phase, chidx, port,
                        0, 0, 0, 0, 0, 0, null, 0);
                if (bAdd2End) MainActivity.sharedObjects.tagsList.add(deviceTag);
                else MainActivity.sharedObjects.tagsList.add(0, deviceTag);
                SharedObjects.TagsIndex tagsIndex = new SharedObjects.TagsIndex(MainActivity.csLibrary4A.byteArrayToString(uplinkPacket.decodedEpc), MainActivity.sharedObjects.tagsList.size() - 1);
                MainActivity.sharedObjects.tagsIndexList.add(tagsIndex);
                Collections.sort(MainActivity.sharedObjects.tagsIndexList);
            }
        }
        MainActivity.csLibrary4A.appendToLog("readerListAdapter is " + (readerListAdapter != null ? "valid" : "null"));
        if (readerListAdapter != null) readerListAdapter.notifyDataSetChanged();
        rfidYieldView.setText("Unique:" + MainActivity.sharedObjects.tagsList.size() + "\nTotal:" + total);
    }

    Runnable runnableStartBeep = new Runnable() {
            @Override
            public void run() {
                //if (MainActivity.isInventoryRfidRequestNewSound()) MainActivity.sharedObjects.playerN.start(); //playerN.setVolume(300, 300);
                //else
                MainActivity.sharedObjects.playerO.start();
            }
        };

    void startInventoryTask() {
        MainActivity.csLibrary4A.appendToLog("startInventoryTask");
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
    }

    private final Runnable runnableCheckReady = new Runnable() {
        @Override
        public void run() {
            if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                button.setEnabled(false);
                button.setText("Please wait");
                MainActivity.csLibrary4A.setNotificationListener(null);
                mHandler.postDelayed(runnableCheckReady, 500);
            } else {
                button.setText("Start");
                button.setEnabled(true);
                setNotificationListener();
            }
        }
    };
}
