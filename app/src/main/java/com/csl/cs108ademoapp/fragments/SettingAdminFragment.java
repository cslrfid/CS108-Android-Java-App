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

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;
import com.csl.cs108library4a.Cs108Library4A;

public class SettingAdminFragment extends CommonFragment {
    private CheckBox checkBoxTriggerReporting, checkBoxInventoryBeep, checkBoxInventoryVibrate, checkBoxSaveFileEnable, checkBoxSaveCloudEnable, checkBoxSaveNewCloudEnable, checkBoxSaveAllCloudEnable;
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
    boolean triggerReporting, inventoryBeep, inventoryVibrate, saveFileEnable, saveCloudEnable, saveNewCloudEnable, saveAllCloudEnable;
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
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.mCs108Library4a.setBarcodeOn(true);
                    MainActivity.mCs108Library4a.barcodeSendCommandConinuous();
                    MainActivity.mCs108Library4a.setBarcodeOn(false);
                    MainActivity.mCs108Library4a.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset2 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonT);
        buttonReset2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.mCs108Library4a.setBarcodeOn(true);
                    MainActivity.mCs108Library4a.barcodeSendCommandTrigger();
                    MainActivity.mCs108Library4a.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset3 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonF);
        buttonReset3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.mCs108Library4a.setBarcodeOn(true);
                    MainActivity.mCs108Library4a.barcodeSendCommandSetPreSuffix();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset4 = (Button) getActivity().findViewById(R.id.settingAdminBarcodeResetButtonR);
        buttonReset4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                    Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.mCs108Library4a.setBarcodeOn(true);
                    MainActivity.mCs108Library4a.barcodeSendCommandResetPreSuffix();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonReset1 = (Button) getActivity().findViewById(R.id.settingAdminReaderResetButton);
        buttonReset1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (false && MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    MainActivity.mCs108Library4a.setReaderDefault();
                    MainActivity.mCs108Library4a.saveSetting2File();
                    Toast.makeText(MainActivity.mContext, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });

        button = (Button) getActivity().findViewById(R.id.settingSaveButtonAdmin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validValue = false;
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (false && MainActivity.mCs108Library4a.isRfidFailure()) {
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
                        settingUpdate();
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (sameCheck == false) MainActivity.mCs108Library4a.setSameCheck(false);
        mHandler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        if (settingTask != null) settingTask.cancel(true);
        MainActivity.mCs108Library4a.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            checkBoxTriggerReporting.setChecked(MainActivity.mCs108Library4a.getTriggerReporting());
            checkBoxInventoryBeep.setChecked(MainActivity.mCs108Library4a.getInventoryBeep());
            checkBoxInventoryVibrate.setChecked(MainActivity.mCs108Library4a.getInventoryVibrate());
            checkBoxSaveFileEnable.setChecked(MainActivity.mCs108Library4a.getSaveFileEnable());
            checkBoxSaveCloudEnable.setChecked(MainActivity.mCs108Library4a.getSaveCloudEnable());
            checkBoxSaveNewCloudEnable.setChecked(MainActivity.mCs108Library4a.getSaveNewCloudEnable());
            checkBoxSaveAllCloudEnable.setChecked(MainActivity.mCs108Library4a.getSaveAllCloudEnable());
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
            spinnerQueryBattery.setSelection(MainActivity.mCs108Library4a.getBatteryDisplaySetting());
            spinnerQueryRssi.setSelection(MainActivity.mCs108Library4a.getRssiDisplaySetting());
            spinnerQueryVibrateMode.setSelection(MainActivity.mCs108Library4a.getVibrateModeSetting());
            spinnerSavingFormat.setSelection(MainActivity.mCs108Library4a.getSavingFormatSetting());
            {
                int csvColumnSelect = MainActivity.mCs108Library4a.getCsvColumnSelectSetting();
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
            if (editTextCycleDelay != null)   editTextCycleDelay.setText(String.valueOf(MainActivity.mCs108Library4a.getCycleDelay()));
            if (editTextTriggerReportingCount != null)   {
                int triggerReportingCount = MainActivity.mCs108Library4a.getTriggerReportingCount();
                if (triggerReportingCount != MainActivity.mCs108Library4a.iNO_SUCH_SETTING) {
                    TableRow tableRow = (TableRow) getActivity().findViewById(R.id.settingAdminTriggerReportingRow);
                    tableRow.setVisibility(View.VISIBLE);
                }
                editTextTriggerReportingCount.setText(String.valueOf(triggerReportingCount));
            }
            if (editTextBeepCount != null)   editTextBeepCount.setText(String.valueOf(MainActivity.mCs108Library4a.getBeepCount()));
            if (editTextVibrateTime != null)   editTextVibrateTime.setText(String.valueOf(MainActivity.mCs108Library4a.getVibrateTime()));
            if (editTextVibrateWindow != null)   editTextVibrateWindow.setText(String.valueOf(MainActivity.mCs108Library4a.getVibrateWindow()));
            editTextServer.setText(MainActivity.mCs108Library4a.getServerLocation());
            editTextServerTimeout.setText(String.valueOf(MainActivity.mCs108Library4a.getServerTimeout()));
            if (updating == false) {
                String name = MainActivity.mCs108Library4a.getBluetoothICFirmwareName();
                if (name == null)   updating = true;
                else if (name.length() == 0) updating = true;
                else editTextDeviceName.setText(name);
            }
            if (updating == false) {
                String name = MainActivity.mCs108Library4a.getModelNumber();
                if (name == null)   updating = true;
                else if (name.length() == 0)    updating = true;
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

        if (invalidRequest == false && (MainActivity.mCs108Library4a.getBluetoothICFirmwareName().matches(deviceName) == false || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setBluetoothICFirmwareName(deviceName) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getBatteryDisplaySetting() != batteryDisplaySelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setBatteryDisplaySetting(batteryDisplaySelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getRssiDisplaySetting() != rssiDisplaySelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setRssiDisplaySetting(rssiDisplaySelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getVibrateModeSetting() != vibrateModeSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setVibrateModeSetting(vibrateModeSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getSavingFormatSetting() != savingFormatSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setSavingFormatSetting(savingFormatSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getCsvColumnSelectSetting() != csvColumnSelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setCsvColumnSelectSetting(csvColumnSelect) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && editTextCycleDelay != null) {
            if (MainActivity.mCs108Library4a.getCycleDelay() != cycleDelay || sameCheck == false) {
                sameSetting = false;
                if (cycleDelay < cycleDelayMin || cycleDelay > cycleDelayMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setCycleDelay(cycleDelay) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextTriggerReportingCount != null) {
            if (MainActivity.mCs108Library4a.getTriggerReportingCount() != sTriggerCount || sameCheck == false) {
                sameSetting = false;
                if (sTriggerCount < sTriggerCountMin || sTriggerCount > sTriggerCountMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setTriggerReportingCount(sTriggerCount) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextBeepCount != null) {
            if (MainActivity.mCs108Library4a.getBeepCount() != iBeepCount || sameCheck == false) {
                sameSetting = false;
                if (iBeepCount < iBeepCountMin || iBeepCount > iBeepCountMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setBeepCount(iBeepCount) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextVibrateTime != null) {
            if (MainActivity.mCs108Library4a.getVibrateTime() != iVibrateTime || sameCheck == false) {
                sameSetting = false;
                if (iVibrateTime < iVibrateTimeMin || iVibrateTime > iVibrateTimeMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setVibrateTime(iVibrateTime) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextVibrateWindow != null) {
            if (MainActivity.mCs108Library4a.getVibrateWindow() != iVibrateWindow || sameCheck == false) {
                sameSetting = false;
                if (iVibrateWindow < iVibrateWindowMin || iVibrateWindow > iVibrateWindowMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setVibrateWindow(iVibrateWindow) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxInventoryVibrate != null) {
            MainActivity.mCs108Library4a.appendToLog("getInventoryVibrate = " + MainActivity.mCs108Library4a.getInventoryVibrate() + ", inventoryVibrate = " + inventoryVibrate);
            if (MainActivity.mCs108Library4a.getInventoryVibrate() != inventoryVibrate || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setInventoryVibrate(inventoryVibrate) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxTriggerReporting != null) {
            if (MainActivity.mCs108Library4a.getTriggerReporting() != triggerReporting || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setTriggerReporting(triggerReporting) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxInventoryBeep != null) {
            MainActivity.mCs108Library4a.appendToLog("getInventoryBeep = " + MainActivity.mCs108Library4a.getInventoryBeep() + ", inventoryBeep = " + inventoryBeep);
            if (MainActivity.mCs108Library4a.getInventoryBeep() != inventoryBeep || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setInventoryBeep(inventoryBeep) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveFileEnable != null) {
            MainActivity.mCs108Library4a.appendToLog("getSaveFileEnable = " + MainActivity.mCs108Library4a.getSaveFileEnable() + ", saveFileEnable = " + saveFileEnable);
            if (MainActivity.mCs108Library4a.getSaveFileEnable() != saveFileEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setSaveFileEnable(saveFileEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveCloudEnable != null) {
            MainActivity.mCs108Library4a.appendToLog("getSaveCloudEnable = " + MainActivity.mCs108Library4a.getSaveCloudEnable() + ", saveCloudEnable = " + saveCloudEnable);
            if (MainActivity.mCs108Library4a.getSaveCloudEnable() != saveCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setSaveCloudEnable(saveCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveNewCloudEnable != null) {
            MainActivity.mCs108Library4a.appendToLog("getSaveNewCloudEnable = " + MainActivity.mCs108Library4a.getSaveNewCloudEnable() + ", saveNewCloudEnable = " + saveNewCloudEnable);
            if (MainActivity.mCs108Library4a.getSaveNewCloudEnable() != saveNewCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setSaveNewCloudEnable(saveNewCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && checkBoxSaveAllCloudEnable != null) {
            MainActivity.mCs108Library4a.appendToLog("getSaveAllCloudEnable = " + MainActivity.mCs108Library4a.getSaveAllCloudEnable() + ", saveAllCloudEnable = " + saveAllCloudEnable);
            if (MainActivity.mCs108Library4a.getSaveAllCloudEnable() != saveAllCloudEnable || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setSaveAllCloudEnable(saveAllCloudEnable) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextServer != null) {
            String serverLocation = MainActivity.mCs108Library4a.getServerLocation(); if (serverLocation == null) serverLocation = "";
            if (serverLocation.matches(serverName) == false || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setServerLocation(serverName) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false && editTextServerTimeout != null) {
            if (MainActivity.mCs108Library4a.getServerTimeout() != iServerTimeout || sameCheck == false) {
                sameSetting = false;
                if (iServerTimeout < iServerTimeoutMin || iServerTimeout > iServerTimeoutMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setServerTimeout(iServerTimeout) == false)
                    invalidRequest = true;
            }
        }
        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.mCs108Library4a.saveSetting2File();
    }
}
