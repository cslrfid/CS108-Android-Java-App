package com.csl.cs108ademoapp.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.InventoryRfidTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.adapters.ReaderListAdapter;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import java.util.Collections;
import java.util.Comparator;

public class InventoryRfidiMultiFragment extends CommonFragment {
    final private boolean bAdd2End = false;
    private boolean bMultiBank = false, bMultiBankInventory = false, bBapInventory = false, bctesiusInventory = false;
    private String mDid = null;
    int vibrateTimeBackup = 0;

    private CheckBox checkBoxDupElim, checkBoxFilterByTid;
    private Spinner spinnerBank1, spinnerBank2;
    private ListView rfidListView;
    private TextView rfidEmptyView;
    private TextView rfidRunTime, rfidVoltageLevel;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;

    private ReaderListAdapter readerListAdapter;
    private InventoryRfidTask inventoryRfidTask;

    void clearTagsList() {
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment: NOT onOptionsItemSelected: clearTagsList");
        MainActivity.csLibrary4A.appendToLog("runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask + ", readerListAdapter" + (readerListAdapter != null ? " tagCount = " + String.valueOf(readerListAdapter.getCount()) : " = NULL"));
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        rfidYieldView.setText("");
        rfidRateView.setText("");
        MainActivity.tagSelected = null;
        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        readerListAdapter.notifyDataSetChanged();
        MainActivity.mLogView.setText("");
    }
    void sortTagsList() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        Collections.sort(MainActivity.sharedObjects.tagsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsListByRssi() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
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
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, bMultiBankInventory | bBapInventory | bctesiusInventory);
        return inflater.inflate(R.layout.fragment_inventory_rfid_multi, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment: onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menuAction_clear:
                clearTagsList();
                return true;
            case R.id.menuAction_sortRssi:
                sortTagsListByRssi();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bMultiBank = getArguments().getBoolean("bMultiBank");
            mDid = getArguments().getString("mDid");
            if (bMultiBank && mDid == null) {
                bMultiBankInventory = true;
            } else if (bMultiBank && mDid != null) {
                if (mDid.matches("E200B0")) {
                    bBapInventory = true;
                } else if (mDid.matches("E203510") || mDid.matches("E283A")) {
                    bctesiusInventory = true;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity.selectFor = -1;
        if (bMultiBankInventory | bBapInventory | bctesiusInventory) {
            androidx.appcompat.app.ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_inv);
            if (bMultiBankInventory) actionBar.setTitle(R.string.title_activity_inventoryRfidMulti);
            else if (bBapInventory) actionBar.setTitle(R.string.title_activity_bap);
            else if (bctesiusInventory) {
                if (mDid.matches("E283A")) actionBar.setTitle("AS321x");
                else actionBar.setTitle(R.string.title_activity_ctesisu);
            }
        }
        if (bMultiBankInventory) {
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.inventoryMultibankSetting);
            linearLayout.setVisibility(View.VISIBLE);
            checkBoxDupElim = (CheckBox) getActivity().findViewById(R.id.accessInventoryDupElim);
            checkBoxDupElim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBoxDupElim.isChecked()) readerListAdapter.setSelectDupElim(true);
                    else readerListAdapter.setSelectDupElim(false);
                }
            });
        }

        checkBoxFilterByTid = (CheckBox) getActivity().findViewById(R.id.accessInventoryFilterByTid);
        if (mDid != null) {
            if (mDid.indexOf("E2827001") == 0) {
                checkBoxFilterByTid.setVisibility(View.VISIBLE);
                checkBoxFilterByTid.setText("filter FM13DT160 only");
            }
        }

        ArrayAdapter<CharSequence> lockAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.memoryBank_options, R.layout.custom_spinner_layout);
        lockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBank1 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank1);
        spinnerBank1.setAdapter(lockAdapter); spinnerBank1.setSelection(2);
        spinnerBank2 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank2);
        spinnerBank2.setAdapter(lockAdapter); spinnerBank2.setSelection(3);

        rfidListView = (ListView) getActivity().findViewById(R.id.inventoryRfidList1);
        rfidEmptyView = (TextView) getActivity().findViewById(R.id.inventoryRfidEmpty1);
        rfidListView.setEmptyView(rfidEmptyView);
        boolean bSelect4detail = true;
        if (bMultiBankInventory) bSelect4detail = false;
        boolean needDupElim = true;

        boolean need4Extra1 = MainActivity.csLibrary4A.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = (mDid != null ? true : false);

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

        rfidRunTime = (TextView) getActivity().findViewById(R.id.inventoryRfidRunTime1);
        rfidVoltageLevel = (TextView) getActivity().findViewById(R.id.inventoryRfidVoltageLevel1);
        TextView rfidFilterOn = (TextView) getActivity().findViewById(R.id.inventoryRfidFilterOn1);
        if (mDid != null || (MainActivity.csLibrary4A.getSelectEnable() == false && MainActivity.csLibrary4A.getInvMatchEnable() == false && MainActivity.csLibrary4A.getRssiFilterEnable() == false))
            rfidFilterOn.setVisibility(View.INVISIBLE);

        rfidYieldView = (TextView) getActivity().findViewById(R.id.inventoryRfidYield1);
        rfidRateView = (TextView) getActivity().findViewById(R.id.inventoryRfidRate1);
        button = (Button) getActivity().findViewById(R.id.inventoryRfidButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        vibrateTimeBackup = MainActivity.csLibrary4A.getVibrateTime();
        final Button buttonT1 = (Button) getActivity().findViewById(R.id.inventoryRfidButtonT1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = buttonT1.getText().toString().trim();
                if (buttonText.toUpperCase().matches("BUZ")) {
                    MainActivity.csLibrary4A.setVibrateTime(0); MainActivity.csLibrary4A.setVibrateOn(1);
                    buttonT1.setText("STOP");
                }
                else {
                    MainActivity.csLibrary4A.setVibrateOn(0);
                    buttonT1.setText("BUZ");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            setNotificationListener();
        }
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
        if (inventoryRfidTask != null) {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().onDestory(): VALID inventoryRfidTask");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
            MainActivity.csLibrary4A.abortOperation(); //added in case inventoryRiidTask is removed
        }
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.setInvBrandId(false);
        resetSelectData();
        MainActivity.csLibrary4A.setVibrateTime(vibrateTimeBackup);
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().onDestory(): onDestory()");
        super.onDestroy();
    }

    boolean userVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment is now VISIBLE");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment is now INVISIBLE");
            MainActivity.csLibrary4A.setNotificationListener(null);
            if (inventoryRfidTask != null) {
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            }
        }
    }

    public static InventoryRfidiMultiFragment newInstance(boolean bMultiBank, String mDid) {
        InventoryRfidiMultiFragment myFragment = new InventoryRfidiMultiFragment();

        Bundle args = new Bundle();
        args.putBoolean("bMultiBank", bMultiBank);
        args.putString("mDid", mDid);
        myFragment.setArguments(args);

        return myFragment;
    }
    public InventoryRfidiMultiFragment() {
        super("InventoryRfidiMultiFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new Cs108Library4A.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.csLibrary4A.appendToLog("TRIGGER key is pressed.");
                startStopHandler(true);
            }
        });
    }

    boolean needResetData = false;
    void resetSelectData() {
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        if (needResetData) {
            MainActivity.csLibrary4A.setTagRead(0);
            MainActivity.csLibrary4A.setAccessBank(1);
            MainActivity.csLibrary4A.setAccessOffset(0);
            MainActivity.csLibrary4A.setAccessCount(0);
            needResetData = false;
        }
        if (mDid != null && mDid.matches("E203510")) MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
    }
    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.csLibrary4A.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.csLibrary4A.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryBarcodeTask) {
            Toast.makeText(MainActivity.mContext, "Running barcode inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            MainActivity.csLibrary4A.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
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
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            else rfidListView.setSelection(0);
            startInventoryTask();
        } else if (MainActivity.csLibrary4A.mrfidToWriteSize() == 0) {
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            if (buttonTrigger) inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            MainActivity.csLibrary4A.appendToLogView("CANCELLING: StartStopHandler generates taskCancelReason = " + inventoryRfidTask.taskCancelReason.toString());
        } else MainActivity.csLibrary4A.appendToLog("BtData. Stop when still writing !!!");
    }

    void startInventoryTask() {
        MainActivity.csLibrary4A.appendToLog("startInventoryTask");
        int extra1Bank = -1, extra2Bank = -1;
        int extra1Count = 0, extra2Count = 0;
        int extra1Offset = 0, extra2Offset = 0;
        String mDid = this.mDid;

        MainActivity.csLibrary4A.appendToLog("Rin: mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid);
        if (mDid != null) {
            if (MainActivity.mDid != null && mDid.length() == 0) mDid = MainActivity.mDid;
            extra2Bank = 2;
            extra2Offset = 0;
            extra2Count = 2;
            if (mDid.matches("E200B0")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 0x2d;
                extra2Count = 1;
            } else if (mDid.matches("E203510")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 8;
                extra2Count = 2;
            } else if (mDid.matches("E283A")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 0;
                extra2Count = 8;
            } else if (mDid.indexOf("E280B12") == 0) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 0x120;
                extra2Count = 1;
            } else if (mDid.indexOf("E280B0") == 0) {
                extra1Bank = 3;
                extra1Offset = 188;
                extra1Count = 2;
                //extra2Bank = 3;
                //extra2Offset = 0x10d;
                //extra2Count = 1;
            } else if (mDid.matches("E282402")) {
                extra1Bank = 0;
                extra1Offset = 11;
                extra1Count = 1;
                extra2Bank = 0;
                extra2Offset = 13;
                extra2Count = 1;
            } else if (mDid.matches("E282403")) {
                extra1Bank = 0;
                extra1Offset = 12;
                extra1Count = 3;
                extra2Bank = 3;
                extra2Offset = 8;
                extra2Count = 4;
            } else if (mDid.matches("E282405")) {
                extra1Bank = 0;
                extra1Offset = 10;
                extra1Count = 5;
                extra2Bank = 3;
                extra2Offset = 0x12;
                extra2Count = 4;
            }
            if (mDid.indexOf("E280B12") == 0) {
                if (MainActivity.mDid.matches("E280B12B")) {
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 0, 5, 1, 0x220, "8321");
                    MainActivity.csLibrary4A.appendToLog("Hello123: Set Sense at Select !!!");
                } else { //if (MainActivity.mDid.matches("E280B12A")) {
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
                    MainActivity.csLibrary4A.appendToLog("Hello123: Set Sense at BOOT !!!");
                }
            } else if (mDid.matches("E203510")) {
                MainActivity.csLibrary4A.setSelectCriteria(1, true, 7, 4, 0, 2, 0, mDid);
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(1);
            } else if (mDid.matches("E283A")) {
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(9);
            } else if (mDid.matches("E28240")) {
                if (MainActivity.selectFor != 0) {
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = 0;
                }
            } else if (mDid.matches("E282402")) {
                if (MainActivity.selectFor != 2) {
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 2, 0, 3, 0xA0, "20");
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = 2;
                }
            } else if (mDid.matches("E282403")) {
                if (MainActivity.selectFor != 3) {
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 2, 0, 3, 0xD0, "1F");
                    MainActivity.csLibrary4A.setSelectCriteria(2, true, 4, 2, 5, 3, 0xE0, "");
                    MainActivity.selectFor = 3;
                }
            } else if (mDid.matches("E282405")) {
                if (MainActivity.selectFor != 5) {
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 5, MainActivity.selectHold, 3, 0x3B0, "00");
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(2);
                    if (MainActivity.csLibrary4A.getRetryCount() < 2) MainActivity.csLibrary4A.setRetryCount(2);
                    MainActivity.selectFor = 5;
                }
            } else {
                if (MainActivity.selectFor != -1) {
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = -1;
                }
            }
            boolean bNeedSelectedTagByTID = true;
            if (mDid.matches("E2806894")) {
                Log.i(TAG, "HelloK: Find E2806894 with MainActivity.mDid = " + MainActivity.mDid);
                if (MainActivity.mDid.matches("E2806894A")) {
                    Log.i(TAG, "HelloK: Find E2806894A");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
                } else if (MainActivity.mDid.matches("E2806894B")) {
                    Log.i(TAG, "HelloK: Find E2806894B");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                    MainActivity.csLibrary4A.setSelectCriteria(0, true, 4, 0, 1, 0x203, "1", true);
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 2, 2, 0, "E2806894", false);
                    if (true) bNeedSelectedTagByTID = false;
                } else if (MainActivity.mDid.matches("E2806894C") || MainActivity.mDid.matches("E2806894d")) {
                    Log.i(TAG, "HelloK: Find " + MainActivity.mDid);
                    MainActivity.csLibrary4A.setInvBrandId(true);
                    MainActivity.csLibrary4A.setSelectCriteria(0, true, 4, 0, 1, 0x204, "1", true);
                    MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 2, 2, 0, "E2806894", false);
                    if (true) bNeedSelectedTagByTID = false;
                }
            } //else if (mDid.indexOf("E28011") == 0) bNeedSelectedTagByTID = false;
            Log.i(TAG, "BleStreamOut: going to setSelectedTagByTID with mDid = " + mDid + " with extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank + ", bNeedSelectedTagByTID = " + bNeedSelectedTagByTID + ", checkBoxFilterFdTag = " + checkBoxFilterByTid.isChecked() + ", bMultiBank = " + bMultiBank);
            if (bNeedSelectedTagByTID) {
                String strMdid = mDid;
                if (strMdid.indexOf("E28011") == 0) {
                    int iValue = Integer.valueOf(strMdid.substring(6, 8), 16);
                    MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%X", iValue));
                    if ((iValue & 0x40) != 0) strMdid = "E2C011";
                    else if ((iValue & 0x80) != 0) strMdid = "E280117";
                    else strMdid = strMdid.substring(0, 6);
                }
                MainActivity.csLibrary4A.appendToLog("revised mDid = " + strMdid);
                if (checkBoxFilterByTid.isChecked()) MainActivity.csLibrary4A.setSelectedTagByTID(strMdid, -1);
                else {
                    MainActivity.csLibrary4A.setSelectCriteriaDisable(0);
                    MainActivity.csLibrary4A.setInvAlgo(MainActivity.csLibrary4A.getInvAlgo());
                }
            }
        } else if (bMultiBankInventory) {
            CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle1);
            if (checkBox.isChecked()) {
                extra1Bank = spinnerBank1.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset1);
                extra1Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength1);
                extra1Count = Integer.valueOf(editText.getText().toString());
            }
            checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle2);
            if (checkBox.isChecked()) {
                extra2Bank = spinnerBank2.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset2);
                extra2Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength2);
                extra2Count = Integer.valueOf(editText.getText().toString());
            }
        }

        MainActivity.csLibrary4A.appendToLog("bMultiBank = " + bMultiBank);
        if (bMultiBank == false) {
            MainActivity.csLibrary4A.restoreAfterTagSelect();
            MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
            inventoryRfidTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0,
                    false, MainActivity.csLibrary4A.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, null,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
        } else {
            boolean inventoryUcode8_bc = mDid != null && mDid.matches("E2806894") && MainActivity.mDid != null && (MainActivity.mDid.matches("E2806894B") || MainActivity.mDid.matches("E2806894C"));
            if ((extra1Bank != -1 && extra1Count != 0) || (extra2Bank != -1 && extra2Count != 0)) {
                if (extra1Bank == -1 || extra1Count == 0) {
                    extra1Bank = extra2Bank;
                    extra2Bank = 0;
                    extra1Count = extra2Count;
                    extra2Count = 0;
                    extra1Offset = extra2Offset;
                    extra2Offset = 0;
                }
                if (extra1Bank == 1) extra1Offset += 2;
                if (extra2Bank == 1) extra2Offset += 2;
                MainActivity.csLibrary4A.appendToLog("HelloK: mDid = " + mDid + ", MainActivity.mDid = " + MainActivity.mDid);
                if (inventoryUcode8_bc == false) {
                    MainActivity.csLibrary4A.appendToLog("BleStreamOut: Set Multibank");
                    MainActivity.csLibrary4A.setTagRead(extra2Count != 0 && extra2Count != 0 ? 2 : 1);
                    MainActivity.csLibrary4A.setAccessBank(extra1Bank, extra2Bank);
                    MainActivity.csLibrary4A.setAccessOffset(extra1Offset, extra2Offset);
                    MainActivity.csLibrary4A.setAccessCount(extra1Count, extra2Count);
                    needResetData = true;
                } else if (needResetData) {
                    MainActivity.csLibrary4A.setTagRead(0);
                    MainActivity.csLibrary4A.setAccessBank(1);
                    MainActivity.csLibrary4A.setAccessOffset(0);
                    MainActivity.csLibrary4A.setAccessCount(0);
                    needResetData = false;
                }
            } else resetSelectData();
            MainActivity.csLibrary4A.appendToLog("startInventoryTask: going to startOperation");
            if (inventoryUcode8_bc)
                MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
            else
                MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY);
            inventoryRfidTask = new InventoryRfidTask(getContext(), extra1Bank, extra2Bank, extra1Count, extra2Count, extra1Offset, extra2Offset,
                    false, MainActivity.csLibrary4A.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, mDid,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
        }
        inventoryRfidTask.execute();
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
