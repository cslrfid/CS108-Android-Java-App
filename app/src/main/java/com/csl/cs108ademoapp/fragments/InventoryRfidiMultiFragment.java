package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_ALIEN;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_ASYGN;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON_XERXES;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_CTESIUS;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_EM_BAP;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_FDMICRO;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ_M730;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ_M770;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ_M775;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ_M780;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ_M830;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_KILOWAY;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NULL;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPC;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCBRAND;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCBRANDTID;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCTID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;

import com.csl.cslibrary4a.CustomAsyncTask;
import com.csl.cslibrary4a.CustomPopupWindow;
import com.csl.cs108ademoapp.GenericTextWatcher;
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

public class InventoryRfidiMultiFragment extends CommonFragment {
    final private boolean bAdd2End = false;
    private boolean bMultiBank = false, bMultiBankInventory = false, bctesiusInventory = false;
    private RfidReader.TagType tagType;
    private String mDid = null;
    int vibrateTimeBackup = 0;

    private View viewFragment;
    private CheckBox checkBoxDupElim, checkBoxFilterByTid, checkBoxFilterByEpc, checkBoxFilterByProtect;
    private EditText editTextProtectPassword;
    private Spinner spinnerBank1, spinnerBank2;
    private ListView rfidListView;
    private TextView rfidRunTime, rfidVoltageLevel;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;

    private ReaderListAdapter readerListAdapter;
    private InventoryRfidTask inventoryRfidTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewFragment = inflater.inflate(R.layout.fragment_inventory_rfid_multi, container, false);
        return viewFragment;
    }

    void clearTagsList() {
        MainActivity.csLibrary4A.appendToLog("runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask + ", readerListAdapter" + (readerListAdapter != null ? " tagCount = " + String.valueOf(readerListAdapter.getCount()) : " = NULL"));
        if (MainActivity.csLibrary4A.isInventoring()) return; //if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        rfidYieldView.setText("");
        rfidRateView.setText("");
        MainActivity.tagSelected = null;
        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        readerListAdapter.notifyDataSetChanged();
        MainActivity.mLogView.setText("");
    }
    void sortTagsList() {
        if (MainActivity.csLibrary4A.isInventoring()) return; //if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        Collections.sort(MainActivity.sharedObjects.tagsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsListByRssi() {
        if (MainActivity.csLibrary4A.isInventoring()) return; //if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
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
        if (MainActivity.csLibrary4A.isInventoring()) return; //if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
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
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuItem as " + item.getItemId());
        if (item.getItemId() == R.id.menuAction_clear) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuAction_clear");
            clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuAction_sortRssi");
            sortTagsListByRssi();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sort) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuAction_sort");
            sortTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_save) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuAction_save");
            saveTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_share) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onMenuItemSelectedA: with menuAction_share");
            shareTagsList();
            return true;
        } else return super.onMenuItemSelectedA(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bMultiBank = getArguments().getBoolean("bMultiBank");
            mDid = getArguments().getString("mDid");
            try {
                int iValue = getArguments().getInt("enumTagType");
                MainActivity.csLibrary4A.appendToLog("onCreate: enumTagType iValue = " + iValue);
                tagType = RfidReader.TagType.values()[iValue];
                MainActivity.csLibrary4A.appendToLog("onCreate: enumTagType = " + tagType.toString());
            } catch (Exception e) { }
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onCreate: bMultiBank = " + bMultiBank + ", mDid = " + mDid + ", tagType = " + tagType.toString());
            if (bMultiBank && tagType == TAG_NULL /*mDid == null*/) {
                bMultiBankInventory = true;
                MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onCreate: set bMultiBankInventory");
            } else if (bMultiBank && ((tagType == TAG_ALIEN /*mDid.matches("E2003"*/)
                        || (tagType == TAG_EM_BAP /*mDid.matches("E200B0"*/)
                        || (tagType == TAG_CTESIUS /*mDid.matches("E203510"*/)
                        || (tagType == TAG_ASYGN /*mDid.matches("E283A")*/))) {
                bctesiusInventory = true;
                MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onCreate: set bctesiusInventory");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (bMultiBankInventory || bctesiusInventory) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onViewCreated: going to addMenuProvider");
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
        }
        super.onViewCreated(view, savedInstanceState);
        MainActivity.selectFor = -1;
        if (bMultiBankInventory | bctesiusInventory) {
            ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_inv);
            if (bMultiBankInventory) actionBar.setTitle(R.string.title_activity_inventoryRfidMulti);
            else if (bctesiusInventory) {
                if (tagType == TAG_ALIEN /*mDid.matches("E2003")*/) actionBar.setTitle(R.string.title_activity_alien);
                else if (tagType == TAG_EM_BAP /*mDid.matches("E200B0")*/) actionBar.setTitle(R.string.title_activity_bap);
                else if (tagType == TAG_CTESIUS /*mDid.matches("E203510")*/) actionBar.setTitle(R.string.title_activity_ctesisu);
                else if (tagType == TAG_ASYGN /*mDid.matches("E283A")*/) actionBar.setTitle("AS321x");
            }
        }
        if (bMultiBankInventory) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.inventoryMultibankSetting);
            linearLayout.setVisibility(View.VISIBLE);
            checkBoxDupElim = (CheckBox) view.findViewById(R.id.accessInventoryDupElim);
            checkBoxDupElim.setVisibility(View.VISIBLE);
            checkBoxDupElim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onViewCreate: checking checkBoxDupElim as " + (checkBoxDupElim == null ? "null" : "valid"));
                    if (checkBoxDupElim.isChecked()) readerListAdapter.setSelectDupElim(true);
                    else readerListAdapter.setSelectDupElim(false);
                }
            });
        }

        checkBoxFilterByTid = (CheckBox) view.findViewById(R.id.accessInventoryFilterByTid);
        checkBoxFilterByEpc = (CheckBox) view.findViewById(R.id.accessInventoryFilterByEpc);
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onViewCreated: mDid = " + mDid + ", tagType = " + tagType.toString() + ", bMultiBankInventory = " + bMultiBankInventory);
        if (tagType != null && tagType != TAG_NULL) {
            if (tagType == TAG_FDMICRO) { //mDid.indexOf("E2827001") == 0) {
                checkBoxFilterByTid.setVisibility(View.VISIBLE);
                //checkBoxFilterByTid.setText("filter FM13DT160 only");
            }
        } else if (!bMultiBankInventory) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.onViewCreated: checkBoxFilterByEpc is " + (checkBoxFilterByEpc == null ? "null" : "valid"));
            checkBoxFilterByEpc.setVisibility(View.VISIBLE);
        }

        checkBoxFilterByProtect = (CheckBox) view.findViewById(R.id.accessInventoryFilterByProtect);
        editTextProtectPassword = (EditText) view.findViewById(R.id.accessInventoryProtectPassword);
        editTextProtectPassword.addTextChangedListener(new GenericTextWatcher(editTextProtectPassword, 8));
        editTextProtectPassword.setText("00000000");

        ArrayAdapter<CharSequence> lockAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.memoryBank_options, R.layout.custom_spinner_layout);
        lockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBank1 = (Spinner) view.findViewById(R.id.accessInventoryBank1);
        spinnerBank1.setAdapter(lockAdapter); spinnerBank1.setSelection(2);
        spinnerBank2 = (Spinner) view.findViewById(R.id.accessInventoryBank2);
        spinnerBank2.setAdapter(lockAdapter); spinnerBank2.setSelection(3);

        rfidListView = (ListView) view.findViewById(R.id.inventoryRfidList1);
        TextView rfidEmptyView = (TextView) view.findViewById(R.id.inventoryRfidEmpty1);
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
                    String strPopup = readerDevice.getUpcSerial();
                    MainActivity.csLibrary4A.appendToLog("strPopup = " + (strPopup == null ? "null" : strPopup));
                    if (strPopup != null && strPopup.trim().length() != 0) {
                        strPopup = MainActivity.csLibrary4A.getUpcSerialDetail(strPopup);
                        CustomPopupWindow epcSerialPopupWindow = new CustomPopupWindow(MainActivity.context);
                        epcSerialPopupWindow.popupStart(strPopup, false);
                    }
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

        rfidRunTime = (TextView) view.findViewById(R.id.inventoryRfidRunTime1);
        rfidVoltageLevel = (TextView) view.findViewById(R.id.inventoryRfidVoltageLevel1);
        TextView rfidFilterOn = (TextView) view.findViewById(R.id.inventoryRfidFilterOn1);
        if (mDid != null || (MainActivity.csLibrary4A.getSelectEnable() == false && MainActivity.csLibrary4A.getInvMatchEnable() == false && MainActivity.csLibrary4A.getRssiFilterEnable() == false))
            rfidFilterOn.setVisibility(View.INVISIBLE);

        rfidYieldView = (TextView) view.findViewById(R.id.inventoryRfidYield1);
        rfidRateView = (TextView) view.findViewById(R.id.inventoryRfidRate1);
        button = (Button) view.findViewById(R.id.inventoryRfidButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        vibrateTimeBackup = MainActivity.csLibrary4A.getVibrateTime();
        final Button buttonT1 = (Button) view.findViewById(R.id.inventoryRfidButtonT1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = buttonT1.getText().toString().trim();
                if (buttonText.toUpperCase().matches("BUZ")) {
                    MainActivity.csLibrary4A.appendToLog("setVibrateOn G 1");
                    MainActivity.csLibrary4A.setVibrateTime(0); MainActivity.csLibrary4A.setVibrateOn(1);
                    buttonT1.setText("STOP");
                }
                else {
                    MainActivity.csLibrary4A.appendToLog("setVibrateOn H 0");
                    MainActivity.csLibrary4A.setVibrateOn(0);
                    buttonT1.setText("BUZ");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint2(true);
    }

    @Override
    public void onPause() {
        setUserVisibleHint2(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(runnableStartStopHandler);
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setNotificationListener(null);
        if (inventoryRfidTask != null) {
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
            /*if (MainActivity.csLibrary4A != null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().onDestory(): VALID inventoryRfidTask");
                MainActivity.csLibrary4A.abortOperation(); //added in case inventoryRiidTask is removed
            }*/
        }
        if (MainActivity.csLibrary4A != null) {
            MainActivity.csLibrary4A.setSameCheck(true);
            MainActivity.csLibrary4A.setInvBrandId(false);
            resetSelectData();
            MainActivity.csLibrary4A.setVibrateTime(vibrateTimeBackup);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().onDestory(): onDestory()");
        }
        super.onDestroy();
    }

    boolean userVisibleHint = true;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if (checkBoxFilterByProtect == null) return;
        if(isVisibleToUser) { //getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().setUserVisibleHint(): userVisibleHint is set as true");
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment is now VISIBLE with tagType = " + (tagType == null ? "null" : tagType.toString()) + ", MainActivity.tagType = " + (MainActivity.tagType == null ? "null" : MainActivity.tagType.toString()));
                 if (MainActivity.tagType == TAG_IMPINJ_M775 || MainActivity.tagType == TAG_IMPINJ_M780 || MainActivity.tagType == TAG_IMPINJ_M830 || MainActivity.tagType == TAG_IMPINJ_M770 || MainActivity.tagType == TAG_IMPINJ_M730 ) {
                    checkBoxFilterByProtect.setVisibility(View.VISIBLE);
                    editTextProtectPassword.setVisibility(View.VISIBLE);
                } else {
                    checkBoxFilterByProtect.setVisibility(View.GONE); checkBoxFilterByProtect.setChecked(false);
                    editTextProtectPassword.setVisibility(View.GONE);
                }
            MainActivity.csLibrary4A.appendToLog("setNotificationListener in multibank inventory");
            setNotificationListener();
            if (true) { //false && !bMultiBank) {
                inventoryRfidTask = new InventoryRfidTask(getContext(),
                        false, MainActivity.csLibrary4A.getInventoryBeep(), false,
                        MainActivity.sharedObjects.tagsList, readerListAdapter,
                        null, null,
                        rfidRunTime, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
                inventoryRfidTask.bSgtinOnly = checkBoxFilterByEpc.isChecked();
                inventoryRfidTask.execute();
            }
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment().setUserVisibleHint(): userVisibleHint is set as false");
            MainActivity.csLibrary4A.appendToLog("InventoryRfidiMultiFragment is now INVISIBLE");
            MainActivity.csLibrary4A.appendToLog("setNotificationListener null in multibank inventory");
            MainActivity.csLibrary4A.setNotificationListener(null);
            if (inventoryRfidTask != null) {
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
            }
        }
    }

    public static InventoryRfidiMultiFragment newInstance(boolean bMultiBank, RfidReader.TagType tagType, String mDid) {
        InventoryRfidiMultiFragment myFragment = new InventoryRfidiMultiFragment();

        Bundle args = new Bundle();
        MainActivity.csLibrary4A.appendToLog("newInstance: enumTagType = " + (tagType == null ? "null" : tagType.toString()));
        if (tagType != null) args.putInt("enumTagType", tagType.ordinal());
        args.putBoolean("bMultiBank", bMultiBank);
        args.putString("mDid", mDid);
        myFragment.setArguments(args);

        return myFragment;
    }
    public InventoryRfidiMultiFragment() {
        super("InventoryRfidiMultiFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.appendToLog("setNotificationListener A in multibank inventory");
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    boolean needResetData = false;
    void resetSelectData() {
        if (MainActivity.tagType == TAG_AXZON_XERXES) { }
        else MainActivity.csLibrary4A.restoreAfterTagSelect();
        if (needResetData) {
            MainActivity.csLibrary4A.setTagRead(0);
            MainActivity.csLibrary4A.setAccessBank(1);
            MainActivity.csLibrary4A.setAccessOffset(0);
            MainActivity.csLibrary4A.setAccessCount(0);
            needResetData = false;
        }
        if (mDid != null && tagType == TAG_CTESIUS /*mDid.matches("E203510")*/) MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
    }
    boolean bStartStopping = false, bButtonTriggering = false, bStartStopHandling = false;
    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) bButtonTriggering = true;
        else bStartStopping = true;
        if (!bStartStopHandling) {
            bStartStopHandling = true;
            mHandler.post(runnableStartStopHandler);
        }
    }
    Runnable runnableStartStopHandler = new Runnable() {
        @Override
        public void run() {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.runnableStartStopHandler.run: BtDataOut, isInventoring = " + MainActivity.csLibrary4A.isInventoring() + ", bStartStopping = " + bStartStopping + ", bButtonTriggering=" + bButtonTriggering + ", triggerButtonStatus=" + MainActivity.csLibrary4A.getTriggerButtonStatus());
            if (MainActivity.csLibrary4A.isInventoring()) {
                if (bStartStopping || (bButtonTriggering && !MainActivity.csLibrary4A.getTriggerButtonStatus())) {
                    if (bAdd2End)
                        rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                    if (bButtonTriggering)
                        inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
                    else
                        inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
                    MainActivity.csLibrary4A.appendToLogView("InventoryRfidMultiFragment.runnableStartStopHandler.run: BtDataOut, generated taskCancelReason = " + inventoryRfidTask.taskCancelReason.toString());
                } else {
                    MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.runnableStartStopHandler.run: BtDataOut, Triggger is pressed when inventoring. please wait");
                    mHandler.postDelayed(runnableStartStopHandler, 100);
                    return;
                }
            } else {
                if (!MainActivity.csLibrary4A.isBleConnected()) {
                    Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                } else if (MainActivity.csLibrary4A.rfidToWriteSize() != 0) {
                    MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.runnableStartStopHandler.run: BtDataOut, sending data. please wait");
                    mHandler.postDelayed(runnableStartStopHandler, 100);
                    return;
                } else if (bStartStopping || (bButtonTriggering && MainActivity.csLibrary4A.getTriggerButtonStatus())) {
                    if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    else rfidListView.setSelection(0);
                    startInventoryTask();
                    MainActivity.csLibrary4A.appendToLogView("InventoryRfidMultiFragment.runnableStartStopHandler.run: done startInventoryTask");
                }
            }
            bStartStopping = false;
            bButtonTriggering = false;
            bStartStopHandling = false;
            MainActivity.csLibrary4A.appendToLogView("InventoryRfidMultiFragment.runnableStartStopHandler.run: ending");
        }
    };
    void startInventoryTask() {
        //RfidReader.ExtraBankData extraBankData = new RfidReader.ExtraBankData();
        String mDid = this.mDid;

        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: tagType = " + (tagType == null ? "null" : tagType.toString()) + ", MainActivity.tagType = " + (MainActivity.tagType == null ? "null" : MainActivity.tagType.toString()));
        RfidReader.TagType tagType1 = this.tagType;
        if (MainActivity.tagType != null) tagType1 = MainActivity.tagType;

        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask 0 with tagType1 = " + tagType1.toString() + ", mDid = " + mDid);
        MainActivity.csLibrary4A.appendToLog("Rin: tagType1 = " + (tagType1 == null ? "null" : tagType1.toString()) + ", MainActivity.tagType = " + (MainActivity.tagType == null ? "null" : MainActivity.tagType.toString()) + ", bMultiBankInventory = " + bMultiBankInventory);

        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: mDid = " + (mDid == null ? "null" : mDid)
                + ", tagType = " + tagType1.toString() + ", MainActivity.tagType = " + MainActivity.tagType
                + ", bMultiBankInventory = " + bMultiBankInventory);
        /*if (mDid != null && MainActivity.mDid != null) {
            if (MainActivity.mDid.indexOf("E280B12") != 0) mDid = MainActivity.mDid;
        }*/

        RfidReader.ExtraBankData extraBankData = new RfidReader.ExtraBankData();
        if (mDid != null) extraBankData.setExtraBankData(tagType1, mDid);
        else if (bMultiBank) {
            CheckBox checkBox = (CheckBox) viewFragment.findViewById(R.id.accessInventoryBankTitle1);
            int extra1Bank = 0, extra2Bank = 0;
            int extra1Count, extra2Count;
            int extra1Offset = 0, extra2Offset = 0;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: checking accessInventoryBankTitle1 as " + (checkBox == null ? "null" : "valid"));
            if (checkBox.isChecked()) {
                extra1Bank = spinnerBank1.getSelectedItemPosition();
                EditText editText = (EditText) viewFragment.findViewById(R.id.accessInventoryOffset1);
                extra1Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) viewFragment.findViewById(R.id.accessInventoryLength1);
                extra1Count = Integer.valueOf(editText.getText().toString());
            } else extra1Count = 0;
            checkBox = (CheckBox) viewFragment.findViewById(R.id.accessInventoryBankTitle2);
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: checking accessInventoryBankTitle2 as " + (checkBox == null ? "null" : "valid"));
            if (checkBox.isChecked()) {
                extra2Bank = spinnerBank2.getSelectedItemPosition();
                EditText editText = (EditText) viewFragment.findViewById(R.id.accessInventoryOffset2);
                extra2Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) viewFragment.findViewById(R.id.accessInventoryLength2);
                extra2Count = Integer.valueOf(editText.getText().toString());
            } else extra2Count = 0;
            extraBankData.setExtraBankData(extra1Bank, extra1Count, extra1Offset, extra2Bank, extra2Count, extra2Offset);
        }

        if (mDid != null) {
            //MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: mDid is valid as " + (mDid == null ? "null" : mDid) + ", MainActivity.mDid = " + (MainActivity.mDid == null ? "null" : MainActivity.mDid));
            //if (MainActivity.mDid != null && mDid.length() == 0) mDid = MainActivity.mDid;
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: new mDid is " + mDid);

            MainActivity.csLibrary4A.appendToLog("mDid = " + mDid);
            if (tagType1 == TAG_CTESIUS /*mDid.matches("E203510")*/) {
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(1); //set profile 302
            } else if (tagType1 == TAG_ASYGN /*mDid.matches("E283A")*/) {
                if (MainActivity.csLibrary4A.get98XX() == 2) MainActivity.csLibrary4A.setCurrentLinkProfile(9); //set profile 244
            } else if (tagType1 == TAG_AXZON_XERXES /*mDid.matches("E282405")*/) {
                if (MainActivity.csLibrary4A.getRetryCount() < 2) MainActivity.csLibrary4A.setRetryCount(2);
            }
            boolean bNeedSelectedTagByTID = true;
            if (true /*mDid.indexOf("E2806894") == 0*/) {
                Log.i(TAG, "HelloK: Find E2806894 with mDid = " + mDid);
                if (tagType1 == TAG_NXP_UCODE8_EPC /*mDid.matches("E2806894A")*/) {
                    Log.i(TAG, "HelloK: Find E2806894A");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                } else if (tagType1 == TAG_NXP_UCODE8_EPCTID /*mDid.matches("E2806894B")*/) {
                    Log.i(TAG, "HelloK: Find E2806894B");
                    MainActivity.csLibrary4A.setInvBrandId(false);
                    if (true) bNeedSelectedTagByTID = false;
                } else if (tagType1 == TAG_NXP_UCODE8_EPCBRAND /*mDid.matches("E2806894C")*/ || tagType1 == TAG_NXP_UCODE8_EPCBRANDTID /*mDid.matches("E2806894d")*/) {
                    MainActivity.csLibrary4A.setInvBrandId(true);
                    if (true) bNeedSelectedTagByTID = false;
                }
            }
            MainActivity.csLibrary4A.setOtherInventoryData(tagType1, mDid);
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask with tagType = " + tagType.toString() + ", mDid = " + mDid);
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: checking checkBoxFilterByTid as " + (checkBoxFilterByTid == null ? "null" : "valid"));
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: checking checkBoxFilterByProtect as " + (checkBoxFilterByProtect == null ? "null" : "valid"));
            int iValue123 = MainActivity.csLibrary4A.setSelectData(tagType1, mDid,
                    bNeedSelectedTagByTID && checkBoxFilterByTid.isChecked(),
                    (checkBoxFilterByProtect.isChecked() ? editTextProtectPassword.getText().toString(): null),
                    MainActivity.selectFor, MainActivity.selectHold);
            /*if (mDid.indexOf("E2806894") == 0) {
                mDid = "E2806894";
            }*/
            if (iValue123 > 0) MainActivity.selectFor = iValue123;
        }

        MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: checking checkBoxFilterByEpc as " + (checkBoxFilterByEpc == null ? "null" : "valid"));
        MainActivity.csLibrary4A.appendToLog("bSgtinOnly: bMultiBank is " + bMultiBank + ", checkBoxFilterByEpc is " + (checkBoxFilterByEpc == null ? "null" : checkBoxFilterByEpc.isChecked()));
        if (!bMultiBank) {
            if (checkBoxFilterByEpc.isChecked()) {
                MainActivity.csLibrary4A.appendToLog("bSgtinOnly: clearTagList");
                clearTagsList();
            }
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultFragment.startInventoryTask: BtDataOut, going to restoreAfterTagSelect");
            MainActivity.csLibrary4A.restoreAfterTagSelect();
            if (false) {
            MainActivity.csLibrary4A.appendToLog("InventoryRfidMultiFragment.startInventoryTask: " + (extraBankData == null ? "extraBankBank = null" : ("extra1Bank = " + extraBankData.extra1Bank + ", extra2Bank = " + extraBankData.extra2Bank)) + ", tagType1 = " + (tagType1 == null ? "null" : tagType1.toString()));
            inventoryRfidTask = new InventoryRfidTask(getContext(),
                    false, MainActivity.csLibrary4A.getInventoryBeep(), false,
                    MainActivity.sharedObjects.tagsList, readerListAdapter,
                    null, null,
                    rfidRunTime, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
            inventoryRfidTask.bSgtinOnly = checkBoxFilterByEpc.isChecked();
            inventoryRfidTask.execute();
            } else {
                inventoryRfidTask.extraBankData = new RfidReader.ExtraBankData();
                MainActivity.csLibrary4A.appendToLog("inventoryRfidMultiFragment.startInventoryTask: 0 created extraBankData");
                inventoryRfidTask.tagType = null;
            }
            MainActivity.csLibrary4A.appendToLog("Debug_Compact 1: InventoryRfidMultiFragment.startInventoryTask");
            MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
        } else {
            Log.i("Hello", "InventoryRfidMultiFragment.startInventoryTask: DebugABC, extra1Bank = " + extraBankData.extra1Bank + ", extra2Bank = " + extraBankData.extra2Bank);
            boolean inventoryUcode8_bc = /*mDid != null && mDid.matches("E2806894") && MainActivity.mDid != null
                    && */(MainActivity.tagType == TAG_NXP_UCODE8_EPCTID || MainActivity.tagType == TAG_NXP_UCODE8_EPCBRAND);
            if ((extraBankData.extra1Bank != -1 && extraBankData.extra1Count != 0) || (extraBankData.extra2Bank != -1 && extraBankData.extra2Count != 0)) {
                extraBankData.adjustExtraBank1();
                MainActivity.csLibrary4A.appendToLog("HelloK: mDid = " + mDid + " with extra1Bank = " + extraBankData.extra1Bank + "," + extraBankData.extra1Offset + "," + extraBankData.extra1Count + ", extra2Bank = " + extraBankData.extra2Bank + "," + extraBankData.extra2Offset + "," + extraBankData.extra2Count);
                if (tagType1 /*mDid*/!= null) MainActivity.csLibrary4A.setResReadNoReply(tagType1 == TAG_KILOWAY /*mDid.matches("E281D")*/);
                if (inventoryUcode8_bc == false) {
                    MainActivity.csLibrary4A.appendToLog("BleStreamOut: Set Multibank");
                    MainActivity.csLibrary4A.setTagRead(extraBankData.extra2Count != 0 && extraBankData.extra2Count != 0 ? 2 : 1);
                    MainActivity.csLibrary4A.setAccessBank(extraBankData.extra1Bank, extraBankData.extra2Bank);
                    MainActivity.csLibrary4A.setAccessOffset(extraBankData.extra1Offset, extraBankData.extra2Offset);
                    MainActivity.csLibrary4A.setAccessCount(extraBankData.extra1Count, extraBankData.extra2Count);
                    needResetData = true;
                } else if (needResetData) {
                    MainActivity.csLibrary4A.setTagRead(0);
                    MainActivity.csLibrary4A.setAccessBank(1);
                    MainActivity.csLibrary4A.setAccessOffset(0);
                    MainActivity.csLibrary4A.setAccessCount(0);
                    needResetData = false;
                }
            } else resetSelectData();
            MainActivity.csLibrary4A.appendToLog("startInventoryTask: going to startOperation with extra1Bank = " + extraBankData.extra1Bank + "," + extraBankData.extra1Offset + "," + extraBankData.extra1Count + ", extra2Bank = " + extraBankData.extra2Bank + "," + extraBankData.extra2Offset + "," + extraBankData.extra2Count);
            inventoryRfidTask.extraBankData = extraBankData;
            MainActivity.csLibrary4A.appendToLog("inventoryRfidMultiFragment.startInventoryTask: 1 created extraBankData");
            inventoryRfidTask.tagType = tagType1;
            if (inventoryUcode8_bc) {
                MainActivity.csLibrary4A.appendToLog("Debug_Compact 2: InventoryRfidMultiFragment.startInventoryTask");
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT);
            } else {
                MainActivity.csLibrary4A.appendToLog("Debug_Compact 3: InventoryRfidMultiFragment.startInventoryTask");
                MainActivity.csLibrary4A.startOperation(RfidReaderChipData.OperationTypes.TAG_INVENTORY);
            }
        }
    }
}
