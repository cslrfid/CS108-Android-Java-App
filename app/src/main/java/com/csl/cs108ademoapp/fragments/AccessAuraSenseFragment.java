package com.csl.cs108ademoapp.fragments;

import static com.csl.cs108ademoapp.MainActivity.tagSelected;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cs108library4a.Cs108Library4A;

public class AccessAuraSenseFragment extends CommonFragment {
    final boolean DEBUG = true;
    Spinner spinnerTagSelect;
    SelectTag selectTag;
    RadioButton radioButtonAuraSensAtBoot, radioButtonAuraSensAtSelect;
    TextView textViewAuraSensorDataOK, textViewAuraSystemConfigurationOK, textViewAuraSensorCalibrationOK, textViewAuraSensorControlOK, textViewAuraSensorDataStoredOK;
    CheckBox checkBoxAuraSensorDataRCommandW, checkBoxAuraSystemConfiguration, checkBoxAuraCalibration, checkBoxAuraControl, checkBoxAuraDataStored;
    EditText editTextAuraSensorData, editTextAuraSystemConfiguration, editTextAuraSensorCalibration, editTextAuraSensorDataStored;
    CheckBox checkBoxAuraSensAtBootCheck, checkBoxAuraSensAtSelectCheck, checkBoxAuraSensWriteCheck;
    TextView textViewConfigOk, textViewTemperatureOk, textViewEnableOk;
    CheckBox checkBoxConfig, checkBoxTemperature, checkBoxEnable;
    TextView textViewTemperature, textViewUnderAlarm, textViewOverAlarm, textViewBatteryAlarm;
    EditText editTextTempThresUnder, editTextTempThresOver, editTextTempCountUnder, editTextTempCountOver, editTextMonitorDelay, editTextSamplingInterval;
    Spinner spinnerDelayUnit, spinnerIntervalUnit, spinnerEnable;
    Spinner spinnerCustomTagType;
    TextView textViewCustomTagFound;
    Button buttonCs8304StartLogging, buttonCs8304StopLogging, buttonCs8304CheckAlarm, buttonCs8304GetLogging;
    TextView textViewCs8304StartLoggingStatus, textViewCs8304StopLoggingStatus, textViewCs8304CheckAlaramStatus, textViewCs8304GetLoggingStatus;

    Button buttonRead, buttonWrite;

    enum ReadWriteTypes {
        NULL, AURA_SENSORDATARCOMMANDW, AURA_SYSTEMCONFIGURATION, AURA_CALIBRATION, AURA_CONTROL, AURA_DATASTORED,
        COLDCHAIN_CONFIGURATION, COLDCHAIN_TEMPERATURE, COLDCHAIN_ENABLE,
        STARTLOGGING, STOPLOGGING, CHECKLOGGING, GETLOGGING
    }

    enum eMicroTag {
        emAuraSense, emColdChain, emBap, others
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;
    boolean bRequestCheck;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_aurasense, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        spinnerTagSelect = (Spinner) getActivity().findViewById(R.id.accessEmicroTagSelect);
        ArrayAdapter<CharSequence> targetAdapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.emicro_options, R.layout.custom_spinner_layout);
        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagSelect.setAdapter(targetAdapter1); spinnerTagSelect.setSelection(0);
        spinnerTagSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LinearLayout layout0 = (LinearLayout) getActivity().findViewById(R.id.accessEmicroSelectLayout);
                LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.accessEmMicroAuroSenseLayout);
                LinearLayout layout2 = (LinearLayout) getActivity().findViewById(R.id.accessEmMicroColdChainLayout);
                TableRow tableRow = (TableRow) getActivity().findViewById(R.id.accessEmicroCCTemperature);
                if (MainActivity.csLibrary4A.get98XX() == 2) tableRow.setVisibility(View.GONE);
                LinearLayout layout4 = (LinearLayout) getActivity().findViewById(R.id.accessCustomReadWrite);
                if (position == eMicroTag.emAuraSense.ordinal()) {
                    MainActivity.mDid = "E280B12";
                    layout0.setVisibility(View.VISIBLE);
                    layout1.setVisibility(View.VISIBLE);
                    layout2.setVisibility(View.GONE);
                    layout4.setVisibility(View.VISIBLE);
                } else if (position == eMicroTag.emColdChain.ordinal()) {
                    MainActivity.mDid = "E280B0";
                    layout0.setVisibility(View.VISIBLE);
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                    layout4.setVisibility(View.VISIBLE);
                } else if (position == eMicroTag.emBap.ordinal()) {
                    MainActivity.mDid = "E200B0";
                    layout0.setVisibility(View.GONE);
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.GONE);
                    layout4.setVisibility(View.GONE);
                } else {
                    MainActivity.mDid = "E280B";
                    layout0.setVisibility(View.GONE);
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.GONE);
                    layout4.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        selectTag = new SelectTag((Activity)getActivity(), 0);
        selectTag.tableRowSelectMemoryBank.setVisibility(View.GONE);

        radioButtonAuraSensAtBoot = (RadioButton) getActivity().findViewById(R.id.accessAuraSensAtBoot);
        radioButtonAuraSensAtSelect = (RadioButton) getActivity().findViewById(R.id.accessAuraSensAtSelect);

        textViewAuraSystemConfigurationOK = (TextView) getActivity().findViewById(R.id.accessAuraSystemConfigurationOK);
        textViewAuraSensorCalibrationOK = (TextView) getActivity().findViewById(R.id.accessAuraSensorCalibrationOK);
        textViewAuraSensorControlOK = (TextView) getActivity().findViewById(R.id.accessAuraSensorControlOK);
        textViewAuraSensorDataStoredOK = (TextView) getActivity().findViewById(R.id.accessAuraSensorDataStoredOK);

        checkBoxAuraSystemConfiguration = (CheckBox) getActivity().findViewById(R.id.accessAuraSystemConfigurationCheck);
        checkBoxAuraCalibration = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorCalibrationCheck);
        checkBoxAuraControl = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorControlCheck);
        checkBoxAuraDataStored = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorDataStoredCheck);

        editTextAuraSensorData = (EditText) getActivity().findViewById(R.id.accessAuraSensorData); editTextAuraSensorData.setEnabled(false);
        editTextAuraSystemConfiguration = (EditText) getActivity().findViewById(R.id.accessAuraSystemConfiguration); editTextAuraSystemConfiguration.setEnabled(false);
        editTextAuraSensorCalibration = (EditText) getActivity().findViewById(R.id.accessAuraSensorCalibration); editTextAuraSensorCalibration.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextAuraSensorDataStored = (EditText) getActivity().findViewById(R.id.accessAuraSensorDataStored); editTextAuraSensorDataStored.setEnabled(false);
        checkBoxAuraSensAtBootCheck = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtBootCheck);
        checkBoxAuraSensAtSelectCheck = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtSelectCheck);
        checkBoxAuraSensWriteCheck = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtWriteCheck);

        textViewConfigOk = (TextView) getActivity().findViewById(R.id.accessCCConfigOK);
        textViewTemperatureOk = (TextView) getActivity().findViewById(R.id.accessCCTemperatureOK);
        textViewEnableOk = (TextView) getActivity().findViewById(R.id.accessCCEnableOK);

        checkBoxConfig = (CheckBox) getActivity().findViewById(R.id.accessCCConfigTitle);
        checkBoxTemperature = (CheckBox) getActivity().findViewById(R.id.accessCCTemperatureTitle);
        checkBoxEnable = (CheckBox) getActivity().findViewById(R.id.accessCCEnableTitle);

        textViewTemperature = (TextView) getActivity().findViewById(R.id.accessCCTemperature);
        textViewUnderAlarm = (TextView) getActivity().findViewById(R.id.accessCCUnderTempAlarm);
        textViewOverAlarm = (TextView) getActivity().findViewById(R.id.accessCCOverTempAlarm);
        textViewBatteryAlarm = (TextView) getActivity().findViewById(R.id.accessCCBatteryAlarm);

        editTextTempThresUnder = (EditText) getActivity().findViewById(R.id.accessCCTempThresUnder);
        editTextTempThresOver = (EditText) getActivity().findViewById(R.id.accessCCTempThresOver);
        editTextTempCountUnder = (EditText) getActivity().findViewById(R.id.accessCCTempCountUnder);
        editTextTempCountOver = (EditText) getActivity().findViewById(R.id.accessCCTempCountOver);
        editTextMonitorDelay = (EditText) getActivity().findViewById(R.id.accessCCMonitorDelay);
        editTextSamplingInterval = (EditText) getActivity().findViewById(R.id.accessCCSamplingInverval);

        ArrayAdapter<CharSequence> arrayAdapterUnit = ArrayAdapter.createFromResource(getActivity(), R.array.coldChain_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDelayUnit = (Spinner) getActivity().findViewById(R.id.accessCCMonitorUnit);
        spinnerDelayUnit.setAdapter(arrayAdapterUnit);

        ArrayAdapter<CharSequence> arrayAdapterUnit1 = ArrayAdapter.createFromResource(getActivity(), R.array.coldChain_IntervalUnit_options, R.layout.custom_spinner_layout);
        arrayAdapterUnit1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntervalUnit = (Spinner) getActivity().findViewById(R.id.accessCCSamplingIntervalUnit);
        spinnerIntervalUnit.setAdapter(arrayAdapterUnit1);

        ArrayAdapter<CharSequence> arrayAdapterEnable = ArrayAdapter.createFromResource(getActivity(), R.array.coldChain_enable_options, R.layout.custom_spinner_layout);
        arrayAdapterEnable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEnable = (Spinner) getActivity().findViewById(R.id.accessCCEnable);
        spinnerEnable.setAdapter(arrayAdapterEnable);

        textViewCustomTagFound = (TextView) getActivity().findViewById(R.id.accessCustomTagFound);
/*
        ArrayAdapter<CharSequence> arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.coldChain_tagtype_options, R.layout.custom_spinner_layout);
        arrayAdapterTagType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomTagType = (Spinner) getActivity().findViewById(R.id.selectCustomTagType);
        spinnerCustomTagType.setAdapter(arrayAdapterTagType);
        spinnerCustomTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch(position) {
                    case 0:
                        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.accessCC8304Layout);
                        linearLayout.setVisibility(View.VISIBLE);
                        linearLayout = (LinearLayout) getActivity().findViewById(R.id.accessCCmaxduraLayout);
                        linearLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        linearLayout = (LinearLayout) getActivity().findViewById(R.id.accessCC8304Layout);
                        linearLayout.setVisibility(View.GONE);
                        linearLayout = (LinearLayout) getActivity().findViewById(R.id.accessCCmaxduraLayout);
                        linearLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
*/
        buttonCs8304StartLogging = (Button) getActivity().findViewById(R.id.accessCs8304StartLogging);
        buttonCs8304StartLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.STARTLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonCs8304StopLogging = (Button) getActivity().findViewById(R.id.accessCs8304StopLogging);
        buttonCs8304StopLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.STOPLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonCs8304CheckAlarm = (Button) getActivity().findViewById(R.id.accessCs8304CheckAlarm);
        buttonCs8304CheckAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.CHECKLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonCs8304GetLogging = (Button) getActivity().findViewById(R.id.accessCs8304GetLogging);
        buttonCs8304GetLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.GETLOGGING;
                operationRead = true; startAccessTask();
            }
        });

        textViewCs8304StartLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCs8304StartLoggingStatus);
        textViewCs8304StopLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCs8304StopLoggingStatus);
        textViewCs8304CheckAlaramStatus = (TextView) getActivity().findViewById(R.id.accessCs8304CheckAlarmStatus);
        textViewCs8304GetLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCs8304GetLoggingStatus);

        buttonRead = (Button) getActivity().findViewById(R.id.accessRWReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                textViewAuraSensorDataOK = (TextView) getActivity().findViewById(R.id.accessAuraSensorDataOK);
                checkBoxAuraSensorDataRCommandW = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorDataCheck);
                readWriteTypes = ReadWriteTypes.NULL;
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessRWWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                textViewAuraSensorDataOK = (TextView) getActivity().findViewById(R.id.accessAuraWriteSensorDataOK);
                checkBoxAuraSensorDataRCommandW = (CheckBox) getActivity().findViewById(R.id.accessAuraWriteSensorDataCheck);
                readWriteTypes = ReadWriteTypes.NULL;
                operationRead = false; startAccessTask();
            }
        });

        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            if (selectTag != null) selectTag.updateBankSelected();
            userVisibleHint = true;
            if (textViewCustomTagFound != null) {
                if (tagSelected != null) {
                    MainActivity.csLibrary4A.appendToLog("tagSelected = " + tagSelected.getUser());
                    if (tagSelected.getUser() != null && tagSelected.getUser().indexOf("830") == 0) {
                        textViewCustomTagFound.setText("CS" + tagSelected.getUser().substring(0, 4) + "-" + tagSelected.getUser().substring(4, 5));
                    } else textViewCustomTagFound.setText("");
                }
                LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.accessEmicroCS8304Layout);
                if (textViewCustomTagFound.getText().toString().contains("8304")) {
                    layout.setVisibility(View.VISIBLE);
                } else layout.setVisibility(View.GONE);
            }
        }
        else {
            if (spinnerTagSelect != null && spinnerTagSelect.getSelectedItemPosition() == eMicroTag.emAuraSense.ordinal()) {
                if (radioButtonAuraSensAtBoot != null && radioButtonAuraSensAtSelect != null) {
                    if (radioButtonAuraSensAtBoot.isChecked()) MainActivity.mDid = "E280B12A";
                    if (radioButtonAuraSensAtSelect.isChecked()) MainActivity.mDid = "E280B12B";
                }
            }
            userVisibleHint = false;
        }
    }

    public AccessAuraSenseFragment() {
        super("AccessAuraSenseFragment");
    }

    boolean isOperationRunning() {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return true;
        } else if (MainActivity.csLibrary4A.isRfidFailure()) {
            Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
            return true;
        } else if (accessTask != null) {
            if (accessTask.getStatus() == AsyncTask.Status.RUNNING) {
                Toast.makeText(MainActivity.mContext, "Running acccess task. Please wait", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
    void startAccessTask() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("startAccessTask()");
        if (updating == false) {
            updating = true;
            bankProcessing = 0; checkProcessing = 0;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false;
    int bankProcessing = 0, checkProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessAuraSenseFragment().updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessAuraSenseFragment().updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessAuraSenseFragment().updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.csLibrary4A.appendToLog("processResult is TRUE");}
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("processTickItems, invalid = " + invalid);
                if (bankProcessing++ != 0 && invalid)   rerunRequest = false;
                else {
                    String selectMask = selectTag.editTextTagID.getText().toString();
                    int selectBank = selectTag.spinnerSelectBank.getSelectedItemPosition()+1;
                    int selectOffset = Integer.valueOf(selectTag.editTextSelectOffset.getText().toString());
                    Cs108Library4A.HostCommands hostCommand;
                    Button buttonAccess;
                    if (readWriteTypes == ReadWriteTypes.COLDCHAIN_TEMPERATURE && operationRead) {
                        hostCommand = Cs108Library4A.HostCommands.CMD_GETSENSORDATA;
                        buttonAccess = buttonRead;
                    } else if (operationRead) {
                        hostCommand = Cs108Library4A.HostCommands.CMD_18K6CREAD;
                        buttonAccess = buttonRead;
                    } else {
                        hostCommand = Cs108Library4A.HostCommands.CMD_18K6CWRITE;
                        buttonAccess = buttonWrite;
                    }
                    MainActivity.csLibrary4A.appendToLog("hostCommand 1 = " + hostCommand.toString());
                    accessTask = new AccessTask(
                            buttonAccess, null, invalid,
                            selectMask, selectBank, selectOffset,
                            selectTag.editTextAccessPassword.getText().toString(),
                            Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()),
                            hostCommand,
                            0, 0, true, false,
                            null, null, null, null, null);
                    accessTask.execute();
                    rerunRequest = true;
                    MainActivity.csLibrary4A.appendToLog("accessTask is created with selectBank = " + selectBank);
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessAuraSenseFragment().updateRunnable(): Restart");
            } else updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessAuraSenseFragment().updateRunnable(): Ending with updating = " + updating);
        }
    };

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            if (DEBUG || true) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
            if (readWriteTypes == ReadWriteTypes.STARTLOGGING) textViewCs8304StartLoggingStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.STOPLOGGING) textViewCs8304StopLoggingStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.CHECKLOGGING) textViewCs8304CheckAlaramStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.GETLOGGING) textViewCs8304GetLoggingStatus.setText(accessResult);
            else if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.AURA_SENSORDATARCOMMANDW) {
                    textViewAuraSensorDataOK.setText("E");
                    //checkBoxUserCode1.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AURA_SYSTEMCONFIGURATION) {
                    textViewAuraSystemConfigurationOK.setText("E");
                    //checkBoxUserCode2.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AURA_CALIBRATION) {
                    textViewAuraSensorCalibrationOK.setText("E");
                    //checkBoxUserCode3.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AURA_CONTROL) {
                    textViewAuraSensorControlOK.setText("E");
                    //checkBoxUserCode4.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AURA_DATASTORED) {
                    textViewAuraSensorDataStoredOK.setText("E");
                    //checkBoxUserCode5.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_CONFIGURATION) {
                    textViewConfigOk.setText("E"); checkBoxConfig.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_TEMPERATURE && operationRead) {
                    textViewTemperatureOk.setText("E"); checkBoxTemperature.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_ENABLE) {
                    textViewEnableOk.setText("E");
                    checkBoxEnable.setChecked(false);
                }
            } else {
                if (readWriteTypes == ReadWriteTypes.AURA_SENSORDATARCOMMANDW) {
                    textViewAuraSensorDataOK.setText("O");
                    //checkBoxUserCode1.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAuraSensorData.setText(showSensorData(accessResult));
                } else if (readWriteTypes == ReadWriteTypes.AURA_SYSTEMCONFIGURATION) {
                    textViewAuraSystemConfigurationOK.setText("O");
                    //checkBoxUserCode2.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  editTextAuraSystemConfiguration.setText(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.AURA_CALIBRATION) {
                    textViewAuraSensorCalibrationOK.setText("O");
                    //checkBoxUserCode3.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        editTextAuraSensorCalibration.setText(String.valueOf(iValue & 0xFF));
                    }
                } else if (readWriteTypes == ReadWriteTypes.AURA_CONTROL) {
                    textViewAuraSensorControlOK.setText("O");
                    //checkBoxUserCode4.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        if ((iValue & 0x2000) != 0) checkBoxAuraSensAtBootCheck.setChecked(true);
                        if ((iValue & 0x4000) != 0) checkBoxAuraSensAtSelectCheck.setChecked(true);
                        if ((iValue & 0x8000) != 0) checkBoxAuraSensWriteCheck.setChecked(true);
                    }
                } else if (readWriteTypes == ReadWriteTypes.AURA_DATASTORED) {
                    textViewAuraSensorDataStoredOK.setText("O");
                    //checkBoxUserCode5.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAuraSensorDataStored.setText(showSensorData(accessResult));
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_TEMPERATURE && operationRead == false) {
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_CONFIGURATION) {
                    textViewConfigOk.setText("O"); checkBoxConfig.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() == 12 && operationRead) { // 43 60 42 FC 04 06
                        byte bValue = Byte.parseByte(accessResult.substring(0, 1), 16);
                        if ((bValue & 8) == 0) {
                            bValue = Byte.parseByte(accessResult.substring(0, 2), 16);
                            bValue &= 0x3F;
                            editTextTempCountUnder.setText(String.valueOf(bValue >> 1));
                            editTextTempThresUnder.setText(getTemperatue(accessResult.substring(1, 4)));

                            bValue = Byte.parseByte(accessResult.substring(4, 6), 16);
                            bValue &= 0x3F;
                            editTextTempCountOver.setText(String.valueOf(bValue >> 1));
                            editTextTempThresOver.setText(getTemperatue(accessResult.substring(5, 8)));

                            byte bValue2 = Byte.parseByte(accessResult.substring(8, 10), 16);
                            spinnerDelayUnit.setSelection(((bValue2 & 0xFF) >> 6) + 1);
                            editTextMonitorDelay.setText(String.valueOf(bValue2 & 0x3F));

                            bValue2 = Byte.parseByte(accessResult.substring(10, 12), 16);
                            spinnerIntervalUnit.setSelection(((bValue2 & 0xFF) >> 6) + 1);
                            editTextSamplingInterval.setText(String.valueOf(bValue2 & 0x3F));
                        }
                    }
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_TEMPERATURE && operationRead) {
                    textViewTemperatureOk.setText("O"); checkBoxTemperature.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    MainActivity.csLibrary4A.appendToLog("accessResult of Temperature = " + accessResult);

                    if (accessResult.length() >= 16) {
                        int indexBegin = accessResult.length() - 16;
                        String stringValue = accessResult.substring(indexBegin, indexBegin + 4);
                        MainActivity.csLibrary4A.appendToLog("temperature part of Temperature accessResult = " + stringValue);
                        accessResult = stringValue;
                    }
                    if (accessResult.length() == 4) {
                        byte bValue = Byte.parseByte(accessResult.substring(0, 1), 16);
                        if ((bValue & 0x8) != 0) textViewBatteryAlarm.setVisibility(View.VISIBLE);
                        else if ((bValue & 0x2) != 0) textViewOverAlarm.setVisibility(View.VISIBLE);
                        else if ((bValue & 01) != 0) textViewUnderAlarm.setVisibility(View.VISIBLE);

                        bValue = Byte.parseByte(accessResult.substring(1, 2), 16);
                        Integer iValue2 = Integer.parseInt(accessResult.substring(2, 4), 16);
                        iValue2 &= 0x1FF;
                        if ((bValue & 0x01) != 0 && iValue2 == 0) textViewTemperature.setText("Invalid");
                        else {
                            String stringValue = getTemperatue(accessResult.substring(1, 4));
                            stringValue += (char) 0x00B0 + "C";
                            textViewTemperature.setText(stringValue);
                        }
                    }
                } else if (readWriteTypes == ReadWriteTypes.COLDCHAIN_ENABLE) {
                    textViewEnableOk.setText("O"); checkBoxEnable.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() == 4 && operationRead) {
                        byte bValue = Byte.parseByte(accessResult.substring(3, 4), 16);
                        if ((bValue & 0x01) != 0) spinnerEnable.setSelection(2);
                        else spinnerEnable.setSelection(1);
                    }
                }
            }
            accessTask = null;
            return true;
        }
    }
    String showSensorData(String accessResult) {
        String strValue = "";
        int iValue = Integer.parseInt(accessResult,16);
        if ((iValue & 0xFC00) == 0x0C00) {
            iValue &= 0x3FF;
            if ((iValue & 0x200) == 0) strValue = String.valueOf(iValue);
            else {
                iValue &= 0x1FF;
                iValue ^= 0x1FF; iValue++; iValue = -iValue;
                strValue = String.valueOf(iValue);
            }
        }
        return strValue;
    }

    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accSize = 0, accOffset = 0;
        String writeData = "";

        if (selectTag.editTextTagID.getText().toString().length() == 0) invalidRequest1 = true;
        else if (spinnerTagSelect.getSelectedItemPosition() == eMicroTag.emAuraSense.ordinal()) {
            if (checkBoxAuraSensorDataRCommandW != null && checkBoxAuraSensorDataRCommandW.isChecked() == true && checkProcessing < 1) {
                accBank = 1; accSize = 1; accOffset = 0x22; readWriteTypes = ReadWriteTypes.AURA_SENSORDATARCOMMANDW; checkProcessing = 1;
                if (operationRead) {
                    textViewAuraSensorDataOK.setText("");
                    editTextAuraSensorData.setText("");
                } else {
                    int iValue = 0;
                    RadioButton radioButtonAuraSens2Null = (RadioButton) getActivity().findViewById(R.id.accessAuraSens2Null);
                    RadioButton radioButtonAuraSens2Store = (RadioButton) getActivity().findViewById(R.id.accessAuraSens2Store);
                    RadioButton radioButtonAuraSens2Calibration = (RadioButton) getActivity().findViewById(R.id.accessAuraSens2Calibration);

                    if (radioButtonAuraSens2Null.isChecked()) iValue = 0x8001;
                    else if (radioButtonAuraSens2Store.isChecked()) iValue = 0x1000;
                    else if (radioButtonAuraSens2Calibration.isChecked()) iValue = 0x2000;
                    writeData = String.format("%04X", iValue);
                    MainActivity.csLibrary4A.appendToLog("WriteData = " + writeData);
                }
            } else if (checkBoxAuraSystemConfiguration != null && checkBoxAuraSystemConfiguration.isChecked() == true && checkProcessing < 2 && operationRead) {
                accBank = 3; accSize = 1; accOffset = 0x120; readWriteTypes = ReadWriteTypes.AURA_SYSTEMCONFIGURATION; checkProcessing = 2;
                if (operationRead) {
                    textViewAuraSystemConfigurationOK.setText("");
                    editTextAuraSystemConfiguration.setText("");
                }
            } else if (checkBoxAuraCalibration != null && checkBoxAuraCalibration.isChecked() == true && checkProcessing < 3) {
                accBank = 3; accSize = 1; accOffset = 0x122; readWriteTypes = ReadWriteTypes.AURA_CALIBRATION; checkProcessing = 3;
                if (operationRead) {
                    textViewAuraSensorCalibrationOK.setText("");
                    editTextAuraSensorCalibration.setText("");
                } else {
                    String strValue = editTextAuraSensorCalibration.getText().toString();
                    Integer iValue = Integer.valueOf(strValue);
                    iValue &= 0xFF;
                    writeData = String.format("%04X", iValue);
                }
            } else if (checkBoxAuraControl != null && checkBoxAuraControl.isChecked() == true && checkProcessing < 4) {
                accBank = 3; accSize = 1; accOffset = 0x123; readWriteTypes = ReadWriteTypes.AURA_CONTROL; checkProcessing = 4;
                if (operationRead) {
                    textViewAuraSensorControlOK.setText("");
                    checkBoxAuraSensAtBootCheck.setChecked(false); checkBoxAuraSensAtSelectCheck.setChecked(false); checkBoxAuraSensWriteCheck.setChecked(false);
                } else {
                    int iValue = (checkBoxAuraSensAtBootCheck.isChecked() ? 0x2000 : 0) | (checkBoxAuraSensAtSelectCheck.isChecked() ? 0x4000 : 0) | (checkBoxAuraSensWriteCheck.isChecked() ? 0x8000 : 0);
                    writeData = String.format("%04X", iValue);
                }
            } else if (checkBoxAuraDataStored != null && checkBoxAuraDataStored.isChecked() == true && checkProcessing < 5 && operationRead) {
                accBank = 3;
                accSize = 1;
                accOffset = 0x124;
                readWriteTypes = ReadWriteTypes.AURA_DATASTORED;
                checkProcessing = 5;
                if (operationRead) {
                    textViewAuraSensorDataStoredOK.setText("");
                    editTextAuraSensorDataStored.setText("");
                }
            } else {
                invalidRequest1 = true;
            }
        } else if (spinnerTagSelect.getSelectedItemPosition() == eMicroTag.emColdChain.ordinal()) {
            accBank = 3;
            if (bRequestCheck) {
                readWriteTypes = ReadWriteTypes.COLDCHAIN_TEMPERATURE;
                if (bankProcessing == 0) {
                    if (operationRead) {
                        textViewTemperature.setText("");
                        accOffset = 0x10D;
                        accSize = 1;
                        writeData = "0000";
                    } else invalidRequest1 = true;
                } else {
                    operationRead = true;
                    MainActivity.csLibrary4A.macWrite(0x11F, 3);
                    return false;
                }
            } else if (checkBoxConfig.isChecked() == true) {
                accOffset = 0xEC; accSize = 3; readWriteTypes = ReadWriteTypes.COLDCHAIN_CONFIGURATION; textViewConfigOk.setText("");
                if (operationRead) {
                    editTextTempThresUnder.setText("");
                    editTextTempThresOver.setText("");
                    editTextTempCountUnder.setText("");
                    editTextTempCountOver.setText("");
                    editTextMonitorDelay.setText("");
                    editTextSamplingInterval.setText("");
                    spinnerDelayUnit.setSelection(0);
                    spinnerIntervalUnit.setSelection(0);
                } else {
                    try {
                        int underTempCount, underTempThreshold, overTempCount, overTempThreshold;
                        byte tempBytes[] = new byte[6];

                        tempBytes[0] = 0x40;
                        tempBytes[0] |= (Byte.parseByte(editTextTempCountUnder.getText().toString()) << 1);
                        float fValue = Float.parseFloat(editTextTempThresUnder.getText().toString());
                        short sValue = setTemperature(fValue);
                        if ((sValue & (short)0x100) != 0) tempBytes[0] |= 1;
                        tempBytes[1] = (byte)sValue;

                        tempBytes[2] = 0;
                        tempBytes[2] |= (Byte.parseByte(editTextTempCountOver.getText().toString()) << 1);
                        fValue = Float.parseFloat(editTextTempThresOver.getText().toString());
                        sValue = setTemperature(fValue);
                        if ((sValue & (short)0x100) != 0) tempBytes[2] |= 1;
                        tempBytes[3] = (byte)sValue;

                        int iTemp = spinnerDelayUnit.getSelectedItemPosition();
                        if (iTemp < 1) iTemp = 1;
                        else if (iTemp > 4) iTemp = 4;
                        iTemp--;
                        tempBytes[4] = (byte)iTemp; tempBytes[4] <<= 6;
                        byte bValue = Byte.parseByte(editTextMonitorDelay.getText().toString());
                        tempBytes[4] |= (bValue & 0x3F);

                        iTemp = spinnerIntervalUnit.getSelectedItemPosition();
                        if (iTemp < 1) iTemp = 1;
                        else if (iTemp > 4) iTemp = 4;
                        iTemp--;
                        tempBytes[5] = (byte)iTemp; tempBytes[5] <<= 6;
                        bValue = Byte.parseByte(editTextSamplingInterval.getText().toString());
                        tempBytes[5] |= (bValue & 0x3F);

                        writeData = MainActivity.csLibrary4A.byteArrayToString(tempBytes);
                        MainActivity.csLibrary4A.appendToLog("editTextTempCountUnder = " + MainActivity.csLibrary4A.byteArrayToString(tempBytes));
                    } catch (Exception ex) {
                        MainActivity.csLibrary4A.appendToLog("Invalid String.parse !!!");
                        invalidRequest1 = true;
                    }
                }
            } else if (checkBoxTemperature.isChecked() == true) {
                readWriteTypes = ReadWriteTypes.COLDCHAIN_TEMPERATURE;
                if (bankProcessing == 0) {
                    if (operationRead) {
                        textViewTemperature = (TextView) getActivity().findViewById(R.id.accessCCTemperature);
                        textViewTemperature.setText("");
                        textViewUnderAlarm = (TextView) getActivity().findViewById(R.id.accessCCUnderTempAlarm);
                        textViewUnderAlarm.setVisibility(View.INVISIBLE);
                        textViewOverAlarm = (TextView) getActivity().findViewById(R.id.accessCCOverTempAlarm);
                        textViewOverAlarm.setVisibility(View.INVISIBLE);
                        textViewBatteryAlarm = (TextView) getActivity().findViewById(R.id.accessCCBatteryAlarm);
                        textViewBatteryAlarm.setVisibility(View.INVISIBLE);
                        textViewTemperatureOk.setText("");
                        if (true) {
                            MainActivity.csLibrary4A.macWrite(0x11F, 3);
                            return false;
                        }
                        accOffset = 0x100;
                        accSize = 1;
                        operationRead = false;
                    } else invalidRequest1 = true;
                } else {
                    accOffset = 0x100;
                    accSize = 1;
                    operationRead = true;
                }
            } else if (checkBoxEnable.isChecked() == true) {
                accOffset = 0x10D;
                accSize = 1;
                readWriteTypes = ReadWriteTypes.COLDCHAIN_ENABLE;
                textViewEnableOk.setText("");
                if (operationRead) spinnerEnable.setSelection(0);
                else {
                    int iSelect = spinnerEnable.getSelectedItemPosition();
                    if (iSelect == 0) invalidRequest1 = true;
                    else if (iSelect == 1) writeData = "0000";
                    else writeData = "0001";
                    String stringValue = "0000";
                }
            } else if (readWriteTypes == ReadWriteTypes.STARTLOGGING || readWriteTypes == ReadWriteTypes.STOPLOGGING || readWriteTypes == ReadWriteTypes.CHECKLOGGING || readWriteTypes == ReadWriteTypes.GETLOGGING) {
                MainActivity.csLibrary4A.appendToLog("accessResult 1 bankProcewssing = " + bankProcessing );
                accOffset = 0xF0; accSize = 1; operationRead = true;
                if (readWriteTypes == ReadWriteTypes.STARTLOGGING) {
                    switch(bankProcessing) {
                        case 0:
                            textViewCs8304StartLoggingStatus.setText("");
                            break;
                        case 1:
                            long seconds = System.currentTimeMillis() / (long)1000;
                            int interval = 10;
                            float temperatureOffset = 10; //range 20 to 0 represents -20 to 0 degreeC
                            accOffset = 0; accSize = 4; writeData = "";

                            writeData += String.format("%08X", seconds);
                            writeData += String.format("%04X", interval);
                            float fTemp = temperatureOffset / (float) 0.25;
                            short sTemp = (short) fTemp;
                            writeData += String.format("%04X", sTemp);
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: UTC seconds = " + seconds + ", writedata = " + writeData);
                            break;
                        case 2:
                            float overTemperature = 20;
                            float underTemperature = -10;
                            accOffset = 0x106; accSize = 3; writeData = "";

                            fTemp = overTemperature / (float) 0.25;
                            sTemp = (short) fTemp;
                            writeData += String.format("%04X", sTemp);
                            fTemp = underTemperature / (float) 0.25;
                            sTemp = (short) fTemp;
                            writeData += String.format("%04X", sTemp);
                            writeData += "0000";    //clear Alarm status
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: temperature alarm: writeData = " + writeData);
                            break;
                        case 3:
                            accOffset = 0x104; accSize = 1; writeData = "0001";
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: status: writeData = " + writeData);
                            break;
                        case 4:
                            accOffset = 0xF0; accSize = 1; writeData = "A000";
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: control: writeData = " + writeData);
                            break;
                        default:
                            invalidRequest1 = true; readWriteTypes = ReadWriteTypes.NULL;
                            break;
                    }
                } else if (readWriteTypes == ReadWriteTypes.STOPLOGGING) {
                    switch(bankProcessing) {
                        case 0:
                            textViewCs8304StartLoggingStatus.setText("");
                            break;
                        case 1:
                            accOffset = 0x104; accSize = 1; writeData = "0002";
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: status: writeData = " + writeData);
                            break;
                        case 2:
                        case 3:
                            accOffset = 0xF0; accSize = 1; writeData = "A600";
                            operationRead = false;
                            MainActivity.csLibrary4A.appendToLog("accessResult: control: writeData = " + writeData);
                            break;
                        default:
                            invalidRequest1 = true; readWriteTypes = ReadWriteTypes.NULL;
                            break;
                    }
                } else if (readWriteTypes == ReadWriteTypes.CHECKLOGGING) {
                    switch(bankProcessing) {
                        case 0:
                            textViewCs8304StartLoggingStatus.setText("");
                            break;
                        case 1:
                            accOffset = 0x108; accSize = 1; writeData = "";
                            operationRead = true;
                            break;
                        case 2:
                            int iValue = 0;
                            try {
                                iValue = Integer.parseInt(textViewCs8304CheckAlaramStatus.getText().toString(), 16);
                            } catch (Exception ex) { }
                            if ((iValue & 2) != 0) {
                                accOffset = 0x108; accSize = 1; writeData = "";
                                iValue &= 0x3; iValue |= 1;
                                writeData = String.format("%04X", iValue);
                                operationRead = false;
                                MainActivity.csLibrary4A.appendToLog("accessResult: writeData = " + writeData);
                                break;
                            }
                        default:
                            invalidRequest1 = true; readWriteTypes = ReadWriteTypes.NULL;
                            break;
                    }
                } else if (readWriteTypes == ReadWriteTypes.GETLOGGING) {
                    switch(bankProcessing) {
                        case 0:
                            textViewCs8304GetLoggingStatus.setText("");
                            break;
                        default:
                            invalidRequest1 = true; readWriteTypes = ReadWriteTypes.NULL;
                            break;
                    }
                }
            } else {
                invalidRequest1 = true;
            }
        } else {
            invalidRequest1 = true;
        }

        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (MainActivity.csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false && operationRead == false) {
            if (invalidRequest1 == false) {
                if (MainActivity.csLibrary4A.setAccessWriteData(writeData) == false) {
                    invalidRequest1 = true;
                }
            }
        }
        MainActivity.csLibrary4A.appendToLog("found invalidRequest1 = " + invalidRequest1);
        return invalidRequest1;
    }

    short setTemperature(float fTemperature) {
        if (fTemperature > 63.75) fTemperature = (float) 63.75;
        else if (fTemperature < -64) fTemperature = -64;
        boolean bNegative = false;
        if (fTemperature < 0) { bNegative = true; fTemperature = 0 - fTemperature; }
        fTemperature += 0.125; fTemperature /= 0.25;
        short retValue = (short)fTemperature;
        if (bNegative) { retValue--; retValue &= 0xFF; retValue ^= 0xFF; retValue |= 0x100; }
        return  retValue;
    }
    String getTemperatue(String stringInput) {
        byte bValue = Byte.parseByte(stringInput.substring(0,1), 16);
        byte bValue2 = Byte.parseByte(stringInput.substring(1, 2), 16); bValue2 <<= 4;
        byte bValue3 = Byte.parseByte(stringInput.substring(2, 3), 16); bValue2 |= bValue3;
        String stringValue = ""; short sValue = (short)(bValue2 & 0xFF);
        if ((bValue & 0x01) != 0) { stringValue = "-"; bValue2 ^= 0xFF; sValue = (short)(bValue2 & 0xFF); sValue++; }
        stringValue += String.valueOf((sValue & 0x1FF) >> 2);
        switch (sValue & 0x03) {
            case 1:
                stringValue += ".25";
                break;
            case 2:
                stringValue += ".50";
                break;
            case 3:
                stringValue += ".75";
                break;
        }
        return  stringValue;
    }
}
