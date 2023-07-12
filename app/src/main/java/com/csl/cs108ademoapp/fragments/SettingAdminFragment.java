package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;
import com.csl.cs108library4a.Cs108Library4A;

public class SettingAdminFragment extends CommonFragment {
    private CheckBox checkBoxTriggerReporting, checkBoxInventoryBeep, checkBoxInventoryVibrate, checkBoxSaveFileEnable, checkBoxSaveCloudEnable, checkBoxSaveNewCloudEnable, checkBoxSaveAllCloudEnable, checkBoxDebugEnable;
    private CheckBox checkBoxCsvColumnResBank, checkBoxCsvColumnEpcBank, checkBoxCsvColumnTidBank, checkBoxCsvColumnUserBank, checkBoxCsvColumnPhase, checkBoxCsvColumnChannel, checkBoxCsvColumnTime, checkBoxCsvColumnTimeZone, checkBoxCsvColumnLocation, checkBoxCsvColumnDirection, checkBoxCsvColumnOthers;
    private EditText editTextDeviceName, editTextCycleDelay, editTextTriggerReportingCount, editTextBeepCount, editTextVibrateTime, editTextVibrateWindow, editTextServer, editTextServerTimeout;
    private TextView textViewReaderModel;
    private Spinner spinnerQueryBattery, spinnerQueryRssi, spinnerQueryVibrateMode, spinnerSavingFormat;
    private Button buttonCSLServer, button;

    final boolean sameCheck = true;
    Handler mHandler = new Handler();

    int batteryDisplaySelect = -1;
    int rssiDisplaySelect = -1;
    int vibrateModeSelect = -1;
    int savingFormatSelect = -1;
    int csvColumnSelect = -1;
    String deviceName = "";
    long cycleDelay = -1; long cycleDelayMin = 0; long cycleDelayMax = 2000;
    int iBeepCount = -1; int iBeepCountMin = 1; int iBeepCountMax = 100;
    short sTriggerCount = -1, sTriggerCountMin = 1, sTriggerCountMax = 100;
    int iVibrateTime = -1; int iVibrateTimeMin = 1; int iVibrateTimeMax = 999;
    int iVibrateWindow = -1; int iVibrateWindowMin = 1; int iVibrateWindowMax = 4;
    boolean triggerReporting, inventoryBeep, inventoryVibrate, saveFileEnable, saveCloudEnable, saveNewCloudEnable, saveAllCloudEnable, debugEnable;
    String serverName;
    int iServerTimeout = -1; int iServerTimeoutMin = 3; int iServerTimeoutMax = 9;

    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_settings_admin, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewReaderModel = (TextView) getActivity().findViewById(R.id.settingAdminReaderModel);
        editTextDeviceName = (EditText) getActivity().findViewById(R.id.settingAdminDeviceName);
        editTextDeviceName.setBackgroundResource(R.drawable.my_edittext_background);
        editTextDeviceName.setHint("Name Pattern");
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(20);
        editTextDeviceName.setFilters(FilterArray);

        spinnerQueryBattery = (Spinner) getActivity().findViewById(R.id.settingAdminBattery);
        {
            ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.admin_battery_options, R.layout.custom_spinner_layout);
            targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerQueryBattery.setAdapter(targetAdapter);
        }

        spinnerQueryRssi = (Spinner) getActivity().findViewById(R.id.settingAdminRssi);
        {
            ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.admin_rssi_options, R.layout.custom_spinner_layout);
            targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerQueryRssi.setAdapter(targetAdapter);
        }

        spinnerQueryVibrateMode = (Spinner) getActivity().findViewById(R.id.settingAdminVibrateMode);
        {
            ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.admin_vibratemode_options, R.layout.custom_spinner_layout);
            targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerQueryVibrateMode.setAdapter(targetAdapter);
        }

        if (MainActivity.csLibrary4A.get98XX() == 2) {
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.settingAdminCycleDelayRow);
            linearLayout.setVisibility(View.GONE);
            spinnerQueryVibrateMode.setEnabled(false);
        }

        spinnerSavingFormat = (Spinner) getActivity().findViewById(R.id.settingAdminSavingFormat);
        {
            ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.admin_savingformat_options, R.layout.custom_spinner_layout);
            targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSavingFormat.setAdapter(targetAdapter);
            spinnerSavingFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.settingAdminCSVColumnSelectLayout);
                    switch (i) {
                        case 1:
                            linearLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            linearLayout.setVisibility(View.GONE);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        checkBoxCsvColumnResBank = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnResBank);
        checkBoxCsvColumnEpcBank = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnEpcBank);
        checkBoxCsvColumnTidBank = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnTidBank);
        checkBoxCsvColumnUserBank = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnUserBank);
        checkBoxCsvColumnPhase = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnPhase);
        checkBoxCsvColumnChannel = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnChannel);
        checkBoxCsvColumnTime = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnTime);
        checkBoxCsvColumnTimeZone = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnTimeZone);
        checkBoxCsvColumnLocation = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnLocation);
        checkBoxCsvColumnDirection = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnDirection);
        checkBoxCsvColumnOthers = (CheckBox) getActivity().findViewById(R.id.settingAdminCSVColumnOthers);

        TextView textViewAdminCycleDelayLabel = (TextView) getActivity().findViewById(R.id.settingAdminCycleDelayLabel);
        String stringAdminCycleDelayLabel = textViewAdminCycleDelayLabel.getText().toString();
        stringAdminCycleDelayLabel += "(" + String.valueOf(cycleDelayMin) + "-" + String.valueOf(cycleDelayMax) + "ms)";
        textViewAdminCycleDelayLabel.setText(stringAdminCycleDelayLabel);
        editTextCycleDelay = (EditText) getActivity().findViewById(R.id.settingAdminCycleDelay);

        TextView textViewAdminTriggerReportingCountLabel = (TextView) getActivity().findViewById(R.id.settingAdminTriggerReportingCountLabel);
        String stringAdminTriggeringReportingCountLabel = textViewAdminTriggerReportingCountLabel.getText().toString();
        stringAdminTriggeringReportingCountLabel += "(" + String.valueOf(sTriggerCountMin) + "-" + String.valueOf(sTriggerCountMax) + ") sec";
        textViewAdminTriggerReportingCountLabel.setText(stringAdminTriggeringReportingCountLabel);
        editTextTriggerReportingCount = (EditText) getActivity().findViewById(R.id.settingAdminTriggerReportingCount);

        TextView textViewAdminBeepCountLabel = (TextView) getActivity().findViewById(R.id.settingAdminBeepCountLabel);
        String stringAdminBeepCountLabel = textViewAdminBeepCountLabel.getText().toString();
        stringAdminBeepCountLabel += "(" + String.valueOf(iBeepCountMin) + "-" + String.valueOf(iBeepCountMax) + ")";
        textViewAdminBeepCountLabel.setText(stringAdminBeepCountLabel);
        editTextBeepCount = (EditText) getActivity().findViewById(R.id.settingAdminBeepCount);

        TextView textViewAdminVibrateTimeLabel = (TextView) getActivity().findViewById(R.id.settingAdminVibrateTimeLabel);
        String stringAdminVibrateTimeLabel = textViewAdminVibrateTimeLabel.getText().toString();
        stringAdminVibrateTimeLabel += "(" + String.valueOf(iVibrateTimeMin) + "-" + String.valueOf(iVibrateTimeMax) + "ms)";
        textViewAdminVibrateTimeLabel.setText(stringAdminVibrateTimeLabel);
        editTextVibrateTime = (EditText) getActivity().findViewById(R.id.settingAdminVibrateTime);

        TextView textViewAdminVibrateWindowLabel = (TextView) getActivity().findViewById(R.id.settingAdminVibrateWindowLabel);
        String stringAdminVibrateWindowLabel = textViewAdminVibrateWindowLabel.getText().toString();
        stringAdminVibrateWindowLabel += "(" + String.valueOf(iVibrateWindowMin) + "-" + String.valueOf(iVibrateWindowMax) + "sec)";
        textViewAdminVibrateWindowLabel.setText(stringAdminVibrateWindowLabel);
        editTextVibrateWindow = (EditText) getActivity().findViewById(R.id.settingAdminVibrateWindow);

        checkBoxTriggerReporting = (CheckBox) getActivity().findViewById(R.id.settingAdminTriggerReporting);
        checkBoxInventoryBeep = (CheckBox) getActivity().findViewById(R.id.settingAdminInventoryBeep);
        checkBoxInventoryVibrate = (CheckBox) getActivity().findViewById(R.id.settingAdminInventoryVibrate);
        checkBoxSaveFileEnable = (CheckBox) getActivity().findViewById(R.id.settingAdminToFileEnable);
        checkBoxSaveCloudEnable = (CheckBox) getActivity().findViewById(R.id.settingAdminToCloudEnable);
        checkBoxSaveNewCloudEnable = (CheckBox) getActivity().findViewById(R.id.settingAdminNewToCloudEnable);
        checkBoxSaveAllCloudEnable = (CheckBox) getActivity().findViewById(R.id.settingAdminAllToCloudEnable);
        editTextServer = (EditText) getActivity().findViewById(R.id.settingAdminServer);
        editTextServer.setHint("Cloud Address Pattern");

        TextView textViewAdminServerConnectTimeoutLabel = (TextView) getActivity().findViewById(R.id.settingAdminServerConnectTimeoutLabel);
        String stringAdminServerConnectTimeoutLabel  = textViewAdminServerConnectTimeoutLabel.getText().toString();
        stringAdminServerConnectTimeoutLabel += "(" + String.valueOf(iServerTimeoutMin) + "-" + String.valueOf(iServerTimeoutMax) + "sec)";
        textViewAdminServerConnectTimeoutLabel.setText(stringAdminServerConnectTimeoutLabel);
        editTextServerTimeout = (EditText) getActivity().findViewById(R.id.settingAdminServerConnectTimeout);

        buttonCSLServer = (Button) getActivity().findViewById(R.id.settingAdminCSLServer);
        buttonCSLServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverLocation = "https://" + "democloud.convergence.com.hk:" + "29090/WebServiceRESTs/1.0/req/" + "create-update-delete/update-entity/" + "tagdata";
                //String serverLocation = "https://" + "www.convergence.com.hk:" + "29090/WebServiceRESTs/1.0/req/" + "create-update-delete/update-entity/" + "tagdata";
                //String serverLocation = "http://ptsv2.com/t/10i1t-1519143332/post";
                editTextServer.setText(serverLocation);
            }
        });

        Button buttonReset = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButton);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.csLibrary4A.setBarcodeOn(true);
                    MainActivity.csLibrary4A.barcodeSendCommandConinuous();
                    MainActivity.csLibrary4A.setBarcodeOn(false);
                    MainActivity.csLibrary4A.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset2 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonT);
        buttonReset2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.csLibrary4A.setBarcodeOn(true);
                    MainActivity.csLibrary4A.barcodeSendCommandTrigger();
                    MainActivity.csLibrary4A.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset3 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonF);
        buttonReset3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.csLibrary4A.setBarcodeOn(true);
                    MainActivity.csLibrary4A.barcodeSendCommandSetPreSuffix();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset4 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonR);
        buttonReset4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.csLibrary4A.setBarcodeOn(true);
                    MainActivity.csLibrary4A.barcodeSendCommandResetPreSuffix();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset1 = (Button) getActivity().findViewById(R.id.settingAdminReaderResetButton);
        buttonReset1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (false && MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.csLibrary4A.setReaderDefault();
                    MainActivity.csLibrary4A.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();

                    mHandler.post(updateRunnable);
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
                    String stringInfo = "Please power cycle reader and also this application";
                    customPopupWindow.popupStart(stringInfo, false);
                }
            }
        });

        button = (Button) getActivity().findViewById(R.id.settingSaveButtonAdmin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validValue = false;
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (false && MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else if (updateRunning) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        deviceName = editTextDeviceName.getText().toString();
                        batteryDisplaySelect = spinnerQueryBattery.getSelectedItemPosition();
                        rssiDisplaySelect = spinnerQueryRssi.getSelectedItemPosition();
                        vibrateModeSelect = spinnerQueryVibrateMode.getSelectedItemPosition();
                        savingFormatSelect = spinnerSavingFormat.getSelectedItemPosition();
                        {
                            csvColumnSelect = 0;
                            if (checkBoxCsvColumnResBank.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.RESERVE_BANK.ordinal());
                            if (checkBoxCsvColumnEpcBank.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.EPC_BANK.ordinal());
                            if (checkBoxCsvColumnTidBank.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.TID_BANK.ordinal());
                            if (checkBoxCsvColumnUserBank.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.USER_BANK.ordinal());
                            if (checkBoxCsvColumnPhase.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.PHASE.ordinal());
                            if (checkBoxCsvColumnChannel.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.CHANNEL.ordinal());
                            if (checkBoxCsvColumnTime.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.TIME.ordinal());
                            if (checkBoxCsvColumnTimeZone.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.TIMEZONE.ordinal());
                            if (checkBoxCsvColumnLocation.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.LOCATION.ordinal());
                            if (checkBoxCsvColumnDirection.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.DIRECTION.ordinal());
                            if (checkBoxCsvColumnOthers.isChecked()) csvColumnSelect |= (0x01 << Cs108Library4A.CsvColumn.OTHERS.ordinal());
                        }
                        if (editTextCycleDelay != null)   cycleDelay = Long.parseLong(editTextCycleDelay.getText().toString());
                        if (editTextTriggerReportingCount != null)   sTriggerCount = Short.parseShort(editTextTriggerReportingCount.getText().toString());
                        if (editTextBeepCount != null)   iBeepCount = Integer.parseInt(editTextBeepCount.getText().toString());
                        if (editTextVibrateTime != null)    iVibrateTime = Integer.parseInt(editTextVibrateTime.getText().toString());
                        if (editTextVibrateWindow != null)  iVibrateWindow = Integer.parseInt(editTextVibrateWindow.getText().toString());
                        triggerReporting = checkBoxTriggerReporting.isChecked();
                        inventoryBeep = checkBoxInventoryBeep.isChecked();
                        inventoryVibrate = checkBoxInventoryVibrate.isChecked();
                        saveFileEnable = checkBoxSaveFileEnable.isChecked();
                        saveCloudEnable = checkBoxSaveCloudEnable.isChecked();
                        saveNewCloudEnable = checkBoxSaveNewCloudEnable.isChecked();
                        saveAllCloudEnable = checkBoxSaveAllCloudEnable.isChecked();
                        serverName = editTextServer.getText().toString();
                        iServerTimeout = Integer.parseInt(editTextServerTimeout.getText().toString());
                        debugEnable = checkBoxDebugEnable.isChecked();
                        settingUpdate();
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        checkBoxDebugEnable = (CheckBox) getActivity().findViewById(R.id.settingAdminDebugEnable);

        if (sameCheck == false) MainActivity.csLibrary4A.setSameCheck(false);
        mHandler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        if (settingTask != null) settingTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            checkBoxTriggerReporting.setChecked(MainActivity.csLibrary4A.getTriggerReporting());
            checkBoxInventoryBeep.setChecked(MainActivity.csLibrary4A.getInventoryBeep());
            checkBoxInventoryVibrate.setChecked(MainActivity.csLibrary4A.getInventoryVibrate());
            checkBoxSaveFileEnable.setChecked(MainActivity.csLibrary4A.getSaveFileEnable());
            checkBoxSaveCloudEnable.setChecked(MainActivity.csLibrary4A.getSaveCloudEnable());
            checkBoxSaveNewCloudEnable.setChecked(MainActivity.csLibrary4A.getSaveNewCloudEnable());
            checkBoxSaveAllCloudEnable.setChecked(MainActivity.csLibrary4A.getSaveAllCloudEnable());
            checkBoxDebugEnable.setChecked(MainActivity.csLibrary4A.getUserDebugEnable());
        }
    }

    public SettingAdminFragment() {
        super("SettingAdminFragment");
    }

    boolean updateRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            updateRunning = true;
            spinnerQueryBattery.setSelection(MainActivity.csLibrary4A.getBatteryDisplaySetting());
            spinnerQueryRssi.setSelection(MainActivity.csLibrary4A.getRssiDisplaySetting());
            spinnerQueryVibrateMode.setSelection(MainActivity.csLibrary4A.getVibrateModeSetting());
            spinnerSavingFormat.setSelection(MainActivity.csLibrary4A.getSavingFormatSetting());
            {
                int csvColumnSelect = MainActivity.csLibrary4A.getCsvColumnSelectSetting();
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.RESERVE_BANK.ordinal())) != 0) checkBoxCsvColumnResBank.setChecked(true); else checkBoxCsvColumnResBank.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.EPC_BANK.ordinal())) != 0) checkBoxCsvColumnEpcBank.setChecked(true); else checkBoxCsvColumnEpcBank.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.TID_BANK.ordinal())) != 0) checkBoxCsvColumnTidBank.setChecked(true); else checkBoxCsvColumnTidBank.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.USER_BANK.ordinal())) != 0) checkBoxCsvColumnUserBank.setChecked(true); else checkBoxCsvColumnUserBank.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.PHASE.ordinal())) != 0) checkBoxCsvColumnPhase.setChecked(true); else checkBoxCsvColumnPhase.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.CHANNEL.ordinal())) != 0) checkBoxCsvColumnChannel.setChecked(true); else checkBoxCsvColumnChannel.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.TIME.ordinal())) != 0) checkBoxCsvColumnTime.setChecked(true); else checkBoxCsvColumnTime.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.TIMEZONE.ordinal())) != 0) checkBoxCsvColumnTimeZone.setChecked(true); else checkBoxCsvColumnTimeZone.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.LOCATION.ordinal())) != 0) checkBoxCsvColumnLocation.setChecked(true); else checkBoxCsvColumnLocation.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.DIRECTION.ordinal())) != 0) checkBoxCsvColumnDirection.setChecked(true); else checkBoxCsvColumnDirection.setChecked(false);
                if ((csvColumnSelect & (0x01 << Cs108Library4A.CsvColumn.OTHERS.ordinal())) != 0) checkBoxCsvColumnOthers.setChecked(true); else checkBoxCsvColumnOthers.setChecked(false);
            }
            if (editTextCycleDelay != null)   editTextCycleDelay.setText(String.valueOf(MainActivity.csLibrary4A.getCycleDelay()));
            if (editTextTriggerReportingCount != null)   {
                int triggerReportingCount = MainActivity.csLibrary4A.getTriggerReportingCount();
                if (triggerReportingCount != MainActivity.csLibrary4A.iNO_SUCH_SETTING) {
                    TableRow tableRow = (TableRow) getActivity().findViewById(R.id.settingAdminTriggerReportingRow);
                    tableRow.setVisibility(View.VISIBLE);
                }
                editTextTriggerReportingCount.setText(String.valueOf(triggerReportingCount));
            }
            if (editTextBeepCount != null)   editTextBeepCount.setText(String.valueOf(MainActivity.csLibrary4A.getBeepCount()));
            if (editTextVibrateTime != null)   editTextVibrateTime.setText(String.valueOf(MainActivity.csLibrary4A.getVibrateTime()));
            if (editTextVibrateWindow != null)   editTextVibrateWindow.setText(String.valueOf(MainActivity.csLibrary4A.getVibrateWindow()));
            editTextServer.setText(MainActivity.csLibrary4A.getServerLocation());
            editTextServerTimeout.setText(String.valueOf(MainActivity.csLibrary4A.getServerTimeout()));
            if (updating == false) {
                String name = MainActivity.csLibrary4A.getBluetoothICFirmwareName();
                if (name == null)   {
                    MainActivity.csLibrary4A.appendToLog("updating 1");
                    updating = true;
                }
                else if (name.length() == 0) {
                    MainActivity.csLibrary4A.appendToLog("updating 2");
                    updating = true;
                }
                else editTextDeviceName.setText(name);
            }
            if (updating == false) {
                String name = MainActivity.csLibrary4A.getModelNumber();
                if (name == null)   {
                    MainActivity.csLibrary4A.appendToLog("updating 3");
                    updating = true;
                }
                else if (name.length() == 0)    {
                    MainActivity.csLibrary4A.appendToLog("updating 4");
                    updating = true;
                }
                else textViewReaderModel.setText(name);
            }
            if (updating) {
                mHandler.postDelayed(updateRunnable, 1000);
            } else updateRunning = false;
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        boolean invalidRequest = false;

        if (invalidRequest == false && (MainActivity.csLibrary4A.getBluetoothICFirmwareName().matches(deviceName) == false || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setBluetoothICFirmwareName(deviceName) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getBatteryDisplaySetting() != batteryDisplaySelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setBatteryDisplaySetting(batteryDisplaySelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getRssiDisplaySetting() != rssiDisplaySelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setRssiDisplaySetting(rssiDisplaySelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getVibrateModeSetting() != vibrateModeSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setVibrateModeSetting(vibrateModeSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getSavingFormatSetting() != savingFormatSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setSavingFormatSetting(savingFormatSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getCsvColumnSelectSetting() != csvColumnSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setCsvColumnSelectSetting(csvColumnSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && editTextCycleDelay != null) {
            if (MainActivity.csLibrary4A.getCycleDelay() != cycleDelay || sameCheck == false) {
                sameSetting = false;
                if (cycleDelay < cycleDelayMin || cycleDelay > cycleDelayMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setCycleDelay(cycleDelay) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextTriggerReportingCount != null) {
            if (MainActivity.csLibrary4A.getTriggerReportingCount() != sTriggerCount || sameCheck == false) {
                sameSetting = false;
                if (sTriggerCount < sTriggerCountMin || sTriggerCount > sTriggerCountMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setTriggerReportingCount(sTriggerCount) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextBeepCount != null) {
            if (MainActivity.csLibrary4A.getBeepCount() != iBeepCount || sameCheck == false) {
                sameSetting = false;
                if (iBeepCount < iBeepCountMin || iBeepCount > iBeepCountMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setBeepCount(iBeepCount) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextVibrateTime != null) {
            if (MainActivity.csLibrary4A.getVibrateTime() != iVibrateTime || sameCheck == false) {
                sameSetting = false;
                if (iVibrateTime < iVibrateTimeMin || iVibrateTime > iVibrateTimeMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setVibrateTime(iVibrateTime) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextVibrateWindow != null) {
            if (MainActivity.csLibrary4A.getVibrateWindow() != iVibrateWindow || sameCheck == false) {
                sameSetting = false;
                if (iVibrateWindow < iVibrateWindowMin || iVibrateWindow > iVibrateWindowMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setVibrateWindow(iVibrateWindow) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxInventoryVibrate != null) {
            MainActivity.csLibrary4A.appendToLog("getInventoryVibrate = " + MainActivity.csLibrary4A.getInventoryVibrate() + ", inventoryVibrate = " + inventoryVibrate);
            if (MainActivity.csLibrary4A.getInventoryVibrate() != inventoryVibrate || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setInventoryVibrate(inventoryVibrate) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxTriggerReporting != null) {
            if (MainActivity.csLibrary4A.getTriggerReporting() != triggerReporting || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setTriggerReporting(triggerReporting) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxInventoryBeep != null) {
            MainActivity.csLibrary4A.appendToLog("getInventoryBeep = " + MainActivity.csLibrary4A.getInventoryBeep() + ", inventoryBeep = " + inventoryBeep);
            if (MainActivity.csLibrary4A.getInventoryBeep() != inventoryBeep || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setInventoryBeep(inventoryBeep) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveFileEnable != null) {
            MainActivity.csLibrary4A.appendToLog("getSaveFileEnable = " + MainActivity.csLibrary4A.getSaveFileEnable() + ", saveFileEnable = " + saveFileEnable);
            if (MainActivity.csLibrary4A.getSaveFileEnable() != saveFileEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setSaveFileEnable(saveFileEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveCloudEnable != null) {
            MainActivity.csLibrary4A.appendToLog("getSaveCloudEnable = " + MainActivity.csLibrary4A.getSaveCloudEnable() + ", saveCloudEnable = " + saveCloudEnable);
            if (MainActivity.csLibrary4A.getSaveCloudEnable() != saveCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setSaveCloudEnable(saveCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveNewCloudEnable != null) {
            MainActivity.csLibrary4A.appendToLog("getSaveNewCloudEnable = " + MainActivity.csLibrary4A.getSaveNewCloudEnable() + ", saveNewCloudEnable = " + saveNewCloudEnable);
            if (MainActivity.csLibrary4A.getSaveNewCloudEnable() != saveNewCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setSaveNewCloudEnable(saveNewCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveAllCloudEnable != null) {
            MainActivity.csLibrary4A.appendToLog("getSaveAllCloudEnable = " + MainActivity.csLibrary4A.getSaveAllCloudEnable() + ", saveAllCloudEnable = " + saveAllCloudEnable);
            if (MainActivity.csLibrary4A.getSaveAllCloudEnable() != saveAllCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setSaveAllCloudEnable(saveAllCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextServer != null) {
            String serverLocation = MainActivity.csLibrary4A.getServerLocation(); if (serverLocation == null) serverLocation = "";
            if (serverLocation.matches(serverName) == false || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setServerLocation(serverName) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextServerTimeout != null) {
            if (MainActivity.csLibrary4A.getServerTimeout() != iServerTimeout || sameCheck == false) {
                sameSetting = false;
                if (iServerTimeout < iServerTimeoutMin || iServerTimeout > iServerTimeoutMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setServerTimeout(iServerTimeout) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxDebugEnable != null) {
            MainActivity.csLibrary4A.appendToLog("getDebugEnable = " + MainActivity.csLibrary4A.getUserDebugEnable() + ", debugEnable = " + debugEnable);
            if (MainActivity.csLibrary4A.getUserDebugEnable() != debugEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setUserDebugEnable(debugEnable) == false)
                    invalidRequest = true;
            }
        }
        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.csLibrary4A.saveSetting2File();
    }
}
