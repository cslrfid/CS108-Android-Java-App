package com.csl.cs108ademoapp.fragments;

import androidx.lifecycle.Lifecycle;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessMicronFragment extends CommonFragment {
    final boolean DEBUG = true;
    boolean bXerxesEnable = false;
    EditText editTextaccessRWSelectHoldTime, editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewSelectHoldTimeLabel, textViewConfigOk, textViewCalibrationOk, textViewAnalogPort2CodeOk, textViewAnalogPort1CodeOk, textViewSensorCodeOk, textViewRssiCodeOk, textViewTemperatureCodeOk;
    CheckBox checkBoxConfig, checkBoxCalibration, checkBoxAnalogPort1Code, checkBoxAnalogPort2Code, checkBoxSensorCode, checkBoxRssiCode, checkBoxTemperatureCode;
    Spinner spinnerTagType, spinnerSensorUnit, spinnerTemperatureUnit;
    boolean btagTypeSelected = false;

    TextView textViewModelCode, textViewCalibrationVersion, textViewAnalogPort1Code, textViewAnalogPort2Code, textViewSensorCode, textViewRssiCode, textViewTemperatureCode;
	private Button buttonRead;

    enum ReadWriteTypes {
        NULL, MODELCODE, CALIBRATION, SENSORCODE, RSSICODE, TEMPERATURECODE
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;

    private AccessTask accessTask;
    private int modelCode = 0, selectHold = 15;
    private int calCode1, calTemp1, calCode2, calTemp2, calVer = -1;
    private boolean changedSelectIndex = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_micron, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessMNTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.accessMNAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText(MainActivity.config.configPassword);

        if (MainActivity.config != null) { if (MainActivity.config.config0 != null) selectHold = Integer.parseInt(MainActivity.config.config0); }
        EditText editText = (EditText) getActivity().findViewById(R.id.accessMNRssiUpperLimit);
        editText.setText(MainActivity.config.config1);
        editText = (EditText) getActivity().findViewById(R.id.accessMNRssiLowerLimit);
        editText.setText(MainActivity.config.config2);
        editText = (EditText) getActivity().findViewById(R.id.accessMNHumidityThreshold);
        editText.setText(MainActivity.config.config3);
        TableRow tableRow = (TableRow) getActivity().findViewById(R.id.accessMNHumidityThresholdRow);
        if (MainActivity.mDid.matches("E28240")) tableRow.setVisibility(View.GONE);

        textViewConfigOk = (TextView) getActivity().findViewById(R.id.accessMNModelCodeOK);
        textViewCalibrationOk = (TextView) getActivity().findViewById(R.id.accessMNCalibrationOK);
        textViewAnalogPort1CodeOk = (TextView) getActivity().findViewById(R.id.accessMNAnalogPort1CodeOK);
        textViewAnalogPort2CodeOk = (TextView) getActivity().findViewById(R.id.accessMNAnalogPort2CodeOK);
        textViewSensorCodeOk = (TextView) getActivity().findViewById(R.id.accessMNSensorCodeOK);
        textViewRssiCodeOk = (TextView) getActivity().findViewById(R.id.accessMNRssiCodeOK);
        textViewTemperatureCodeOk = (TextView) getActivity().findViewById(R.id.accessMNTemperatureCodeOK);

        checkBoxConfig = (CheckBox) getActivity().findViewById(R.id.accessMNModelCodeTitle);
        checkBoxCalibration = (CheckBox) getActivity().findViewById(R.id.accessMNCalibrationTitle);
        checkBoxAnalogPort1Code = (CheckBox) getActivity().findViewById(R.id.accessMNAnalogPort1CodeTitle); checkBoxAnalogPort1Code.setEnabled(false);
        checkBoxAnalogPort2Code = (CheckBox) getActivity().findViewById(R.id.accessMNAnalogPort2CodeTitle); checkBoxAnalogPort2Code.setEnabled(false);
        checkBoxSensorCode = (CheckBox) getActivity().findViewById(R.id.accessMNSensorCodeTitle);
        checkBoxRssiCode = (CheckBox) getActivity().findViewById(R.id.accessMNRssiCodeTitle);
        checkBoxTemperatureCode = (CheckBox) getActivity().findViewById(R.id.accessMNTemperatureCodeTitle);

        textViewModelCode = (TextView) getActivity().findViewById(R.id.accessMNModelCode);

        textViewAnalogPort1Code = (TextView) getActivity().findViewById(R.id.accessMNAnalogPort1Code);
        textViewAnalogPort2Code = (TextView) getActivity().findViewById(R.id.accessMNAnalogPort2Code);
        textViewSensorCode = (TextView) getActivity().findViewById(R.id.accessMNSensorCode);
        textViewRssiCode = (TextView) getActivity().findViewById(R.id.accessMNRssiCode);
        textViewCalibrationVersion = (TextView) getActivity().findViewById(R.id.accessMNCalibrationVersion);
        textViewTemperatureCode = (TextView) getActivity().findViewById(R.id.accessMNTemperatureCode);

        ArrayAdapter<CharSequence> arrayAdapterTagType;
        if (bXerxesEnable) arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.xerxesTag_options, R.layout.custom_spinner_layout);
        else arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.rfMicronTag_options, R.layout.custom_spinner_layout);
        arrayAdapterTagType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagType = (Spinner) getActivity().findViewById(R.id.accessMNTagType);
        spinnerTagType.setAdapter(arrayAdapterTagType);
        spinnerTagType.setEnabled(false);
        if (MainActivity.mDid != null) {
            if (MainActivity.mDid.matches("E28240")) spinnerTagType.setSelection(0);
            else if (MainActivity.mDid.matches("E282402")) spinnerTagType.setSelection(1);
            else if (MainActivity.mDid.matches("E282403")) spinnerTagType.setSelection(2);
            else if (MainActivity.mDid.matches("E282405")) spinnerTagType.setSelection(3);
        }
        spinnerTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TableRow tableRowCalibration = (TableRow) getActivity().findViewById(R.id.accessMNCalibrationRow);
                TableRow tableRowTemperatureCode = (TableRow) getActivity().findViewById(R.id.accessMNTemperatureCodeRow);
                TableRow tableRowAnalogPort1 = (TableRow) getActivity().findViewById(R.id.accessMNAnalogPort1CodeRow);
                TableRow tableRowAnalogPort2 = (TableRow) getActivity().findViewById(R.id.accessMNAnalogPort2CodeRow);

                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                        MainActivity.mDid = "E28240";
                        break;
                    case 1:
                        MainActivity.mDid = "E282402";
                        break;
                    case 2:
                        MainActivity.mDid = "E282403";
                        break;
                    case 3:
                        MainActivity.mDid = "E282405";
                        break;
                }
                if (btagTypeSelected) {
                    switch (spinnerTagType.getSelectedItemPosition()) {
                        case 0:
                        case 1:
                            editTextaccessRWSelectHoldTime.setText("0");
                            break;
                        case 2:
                            editTextaccessRWSelectHoldTime.setText("3");
                            break;
                        case 3:
                            editTextaccessRWSelectHoldTime.setText("9");
                            break;
                    }
                } else btagTypeSelected = true;
                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                    case 1:
                    case 2:
                        textViewSelectHoldTimeLabel.setVisibility(View.GONE);
                        editTextaccessRWSelectHoldTime.setVisibility(View.GONE);
                        tableRowAnalogPort1.setVisibility(View.GONE);
                        tableRowAnalogPort2.setVisibility(View.GONE);
                        break;
                    case 3:
                        textViewSelectHoldTimeLabel.setVisibility(View.VISIBLE);
                        editTextaccessRWSelectHoldTime.setVisibility(View.VISIBLE);
                        tableRowAnalogPort1.setVisibility(View.VISIBLE);
                        tableRowAnalogPort2.setVisibility(View.VISIBLE);
                        break;
                }
                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                    case 1:
                        tableRowCalibration.setVisibility(View.GONE);
                        tableRowTemperatureCode.setVisibility(View.GONE);
                        break;
                    case 2:
                    case 3:
                        tableRowCalibration.setVisibility(View.VISIBLE);
                        tableRowTemperatureCode.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> arrayAdapterSensorUnit = ArrayAdapter.createFromResource(getActivity(), R.array.sensor_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterSensorUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensorUnit = (Spinner) getActivity().findViewById(R.id.accessMNSensorUnit);
        spinnerSensorUnit.setAdapter(arrayAdapterSensorUnit);
        spinnerSensorUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSensorCode(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (MainActivity.mDid.matches("E28240")) spinnerSensorUnit.setEnabled(false);

        ArrayAdapter<CharSequence> arrayAdapterTemperatureUnit = ArrayAdapter.createFromResource(getActivity(), R.array.temperature_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterTemperatureUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemperatureUnit = (Spinner) getActivity().findViewById(R.id.accessMNTemperatureUnit);
        spinnerTemperatureUnit.setAdapter(arrayAdapterTemperatureUnit);
        spinnerTemperatureUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setTemperatureCode(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        textViewSelectHoldTimeLabel = (TextView) getActivity().findViewById(R.id.accessMNSelectHoldTimeLabel);
        editTextaccessRWSelectHoldTime = (EditText) getActivity().findViewById(R.id.accessMNSelectHoldTime);
        editTextaccessRWSelectHoldTime.setText(String.valueOf(selectHold));

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessMNAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(MainActivity.config.configPower));

        buttonRead = (Button) getActivity().findViewById(R.id.accessMNReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectHold = Integer.parseInt(editTextaccessRWSelectHoldTime.getText().toString());
                operationRead = true; startAccessTask();
            }
        });

        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupTagID();
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) == false) return;
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            setupTagID();
        } else {
            userVisibleHint = false;

            if (editTextaccessRWSelectHoldTime == null) return;
            int iValue = Integer.parseInt(editTextaccessRWSelectHoldTime.getText().toString());
            if (iValue > 255) editTextaccessRWSelectHoldTime.setText("255");
            else if (iValue < 0) editTextaccessRWSelectHoldTime.setText("0");
            iValue = Integer.parseInt(editTextaccessRWSelectHoldTime.getText().toString());
            MainActivity.selectHold = iValue;

            EditText editText = (EditText) getActivity().findViewById(R.id.accessMNRssiUpperLimit);
            MainActivity.config.config1 = editText.getText().toString();
            editText = (EditText) getActivity().findViewById(R.id.accessMNRssiLowerLimit);
            MainActivity.config.config2 = editText.getText().toString();
            editText = (EditText) getActivity().findViewById(R.id.accessMNHumidityThreshold);
            MainActivity.config.config3 = editText.getText().toString();
        }
    }

    public static AccessMicronFragment newInstance(boolean bXerxesEnable) {
        AccessMicronFragment myFragment = new AccessMicronFragment();
        myFragment.bXerxesEnable = bXerxesEnable;
        return myFragment;
    }
    public AccessMicronFragment() {
        super("AccessMicronFragment");
    }

    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                String stringDetail = tagSelected.getDetails();
                int indexUser = stringDetail.indexOf("TID=");
                if (indexUser != -1) {
                    //
                }
                bSelected = true;
                if (editTextRWTagID != null) editTextRWTagID.setText(tagSelected.getAddress());

                if (setModelCode(tagSelected.getTid())) {
                    textViewModelCode.setText(tagSelected.getTid().substring(5));
                } else if (tagSelected.getMdid() == null) {
                } else if (tagSelected.getMdid().contains("E282402")) {
                    textViewModelCode.setText("02"); modelCode = 2;
                } else if (tagSelected.getMdid().contains("E282403")) {
                    textViewModelCode.setText("03"); modelCode = 3;
                } else if (tagSelected.getMdid().contains("E282405")) {
                    textViewModelCode.setText("05"); modelCode = 5;
                }

                String strRes = tagSelected.getRes();
                if (strRes != null) {
                    int ibracket = strRes.indexOf("(");
                    if (ibracket > 0) strRes = strRes.substring(0, ibracket);

                    if (modelCode == 5) {
                        if (strRes.length() < 4) textViewAnalogPort1Code.setText("");
                        else textViewAnalogPort1Code.setText(str2Decimal(strRes.substring(0,4)));
                        if (strRes.length() < 8) textViewAnalogPort2Code.setText("");
                        else textViewAnalogPort2Code.setText(str2Decimal(strRes.substring(4, 8)));
                        if (strRes.length() < 8) strRes = "";
                        else strRes = strRes.substring(8);
                    }

                    if (strRes.length() < 4) textViewSensorCode.setText("");
                    else setSensorCode(strRes.substring(0, 4));
                    if (strRes.length() < 4) strRes = "";
                    else strRes = strRes.substring(4);

                    if (modelCode == 2) strRes = tagSelected.getRes2();
                    if (strRes.length() < 4) textViewRssiCode.setText("");
                    else textViewRssiCode.setText(str2Decimal(strRes.substring(0, 4)));
                    if (strRes.length() < 4) strRes = "";
                    else strRes = strRes.substring(4);

                    if (modelCode == 3 || modelCode == 5) {
                        if (setCalibrationVersion(tagSelected.getUser()))
                            setTemperatureCode(strRes);
                    }
                }

                stringDetail = tagSelected.getDetails();
                indexUser = stringDetail.indexOf("USER=");
                if (indexUser != -1) {
                    String stringUser = stringDetail.substring(indexUser + 5);
                    MainActivity.csLibrary4A.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
                }
            }
        }
    }

    void startAccessTask() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("startAccessTask()");
        if (updating == false) {
            updating = true; bankProcessing = 0;
            checkProcessing = 0;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0;
    int checkProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessMicronFragment().updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessMicronFragment().updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessMicronFragment().updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.csLibrary4A.appendToLog("processResult is TRUE");}
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("processTickItems, invalid = " + invalid);
                if (bankProcessing++ != 0 && invalid == true)   rerunRequest = false;
                else {
                    int selectBank = 1;
                    int selectOffset = 32;
                    String selectMask = editTextRWTagID.getText().toString();
                    accessTask = new AccessTask(
                            buttonRead, null,
                            invalid,
                            selectMask, selectBank, selectOffset,
                            editTextAccessRWAccPassword.getText().toString(),
                            Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                            (operationRead ? Cs108Library4A.HostCommands.CMD_18K6CREAD: Cs108Library4A.HostCommands.CMD_18K6CWRITE),
                            0, 0, true,
                            null, null, null, null, null);
                    accessTask.execute();
                    rerunRequest = true;
                    MainActivity.csLibrary4A.appendToLog("accessTask is created with selectBank = " + selectBank);
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessMicronFragment().updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessMicronFragment().updateRunnable(): Ending with updating = " + updating);
        }
    };

    boolean setModelCode(String strTid) {
        if (strTid == null) return false;
        if (strTid.length() <= 7) return false;
        if (strTid.substring(0, 7).matches("E282401")) {
            modelCode = 1; return true;
        } else if (strTid.substring(0, 7).matches("E282402")) {
            modelCode = 2; return true;
        } else if (strTid.substring(0, 7).matches("E282403")) {
            modelCode = 3; return true;
        } else if (strTid.substring(0, 7).matches("E282405")) {
            modelCode = 5; return true;
        }
        return false;
    }
    boolean setCalibrationVersion(String strUser) {
        MainActivity.csLibrary4A.appendToLog("strUser = " + strUser);
        textViewCalibrationVersion.setText("");
        if (strUser == null) return false;
        if (strUser.length() < 16) return false;
        if (modelCode == 3) {
            int crc = Integer.parseInt(strUser.substring(0, 4), 16);
            calCode1 = Integer.parseInt(strUser.substring(4, 7), 16);
            calTemp1 = Integer.parseInt(strUser.substring(7, 10), 16);
            calTemp1 >>= 1;
            calCode2 = Integer.parseInt(strUser.substring(9, 13), 16);
            calCode2 >>= 1;
            calCode2 &= 0xFFF;
            calTemp2 = Integer.parseInt(strUser.substring(12, 16), 16);
            calTemp2 >>= 2;
            calTemp2 &= 0x7FF;
            calVer = Integer.parseInt(strUser.substring(15, 16), 16);
            calVer &= 0x3;
            if (DEBUG)
                MainActivity.csLibrary4A.appendToLog("crc = " + crc + ", code1 = " + calCode1 + ", temp1 = " + calTemp1 + ", code2 = " + calCode2 + ", temp2 = " + calTemp2 + ", ver = " + calVer);
            strUser += String.format(", v%d", calVer);
        }
        textViewCalibrationVersion.setText(strUser);
        return true;
    }
    String str2Decimal(String strData) {
        int iTemp = -1;
        if (strData != null)    if (strData.length() >= 4)  iTemp = Integer.parseInt(strData.substring(0, 4), 16);
        if (iTemp == -1) return "";
        return String.format("%d", iTemp);
    }
    boolean setRssiCode(String strData) {
        if (strData == null) return false;
        if (strData.length() < 4) return false;

        int iTemp = Integer.parseInt(strData.substring(0,4), 16); iTemp &= 0x1F;
        textViewRssiCode.setText(String.format("%d", iTemp));   //"%02X"
        EditText editText = (EditText) getActivity().findViewById(R.id.accessMNRssiLowerLimit);
        int iTempLower = Integer.parseInt(editText.getText().toString());
        editText = (EditText) getActivity().findViewById(R.id.accessMNRssiUpperLimit);
        int iTempUpper = Integer.parseInt(editText.getText().toString());
        if (iTemp >= iTempLower && iTemp <= iTempUpper) textViewRssiCode.setTextColor(Color.BLACK);
        else textViewRssiCode.setTextColor(Color.RED);
        return true;
    }

    String strSensorCode0;
    boolean setSensorCode(String strData) {
        if (strData == null) {
            if (strSensorCode0 != null && textViewSensorCode.getText().toString().length() != 0) strData = strSensorCode0;
            else return false;
        }
        if (strData.length() < 4) return false;
        strSensorCode0 = strData;
        strData = str2Decimal(strData);
        if (spinnerSensorUnit.getSelectedItemPosition() == 1) {
            float fValue = (float) Integer.parseInt(strData);
            if (true) {
                EditText editText = (EditText) getActivity().findViewById(R.id.accessMNHumidityThreshold);
                int iValue = Integer.parseInt(editText.getText().toString());
                MainActivity.csLibrary4A.appendToLog("iValue for Dry/Wet comparision = " + iValue);
                if (fValue >=  iValue) strData = "dry";
                else strData = "wet";
            } else {
                if (modelCode == 2) fValue /= (float) 0x1F;
                else fValue /= (float) 0x1FF;
                fValue *= 100;
                strData = String.format("%.1f", fValue);
            }
        }
        textViewSensorCode.setText(strData);
        return true;
    }

    String strTemperatureCode0;
    boolean setTemperatureCode(String strData) {
        if (strData == null) {
            if (strTemperatureCode0 != null && textViewTemperatureCode.getText().toString().length() != 0) strData = strTemperatureCode0;
            else return false;
        }
        if (strData.length() < 4) return false;
        float fTemperature = -500;
        if (modelCode == 3 && calVer != -1) {
            fTemperature = Integer.parseInt(strData.substring(0, 4), 16);
            fTemperature = ((float) calTemp2 - (float) calTemp1) * (fTemperature - (float) calCode1);
            fTemperature /= ((float) (calCode2) - (float) calCode1);
            fTemperature += (float) calTemp1;
            fTemperature -= 800;
            fTemperature /= 10;
        } else if (modelCode == 5) {
            String strCalData = textViewCalibrationVersion.getText().toString();
            if (strCalData != null) fTemperature = MainActivity.csLibrary4A.decodeMicronTemperature(5, strData, strCalData);
        }
        if (fTemperature != -500) {
            if (spinnerTemperatureUnit.getSelectedItemPosition() == 1) {
                fTemperature *= 1.8;
                fTemperature += 32;
            }
            strTemperatureCode0 = strData;
            strData = String.format("%.1f", fTemperature);
        } else {
            strTemperatureCode0 = null;
            strData = "";
        }
        textViewTemperatureCode.setText(strData);
        return true;
    }

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            if (changedSelectIndex) {
                changedSelectIndex = false; MainActivity.selectFor = 0;
                MainActivity.csLibrary4A.setSelectCriteriaDisable(2);
                MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
            }
            accessResult = accessTask.accessResult;
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.MODELCODE) {
                    textViewConfigOk.setText("E");
                    //checkBoxConfig.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.CALIBRATION) {
                    textViewCalibrationOk.setText("E");
                    //checkBoxCalibration.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.SENSORCODE) {
                    textViewSensorCodeOk.setText("E");
                    //checkBoxSensorCode.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.RSSICODE) {
                    textViewRssiCodeOk.setText("E");
                    //checkBoxRssiCode.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.TEMPERATURECODE) {
                    textViewTemperatureCodeOk.setText("E");
                    //checkBoxTemperatureCode.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.MODELCODE) {
                    textViewConfigOk.setText("O");
                    //checkBoxConfig.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    boolean valid = setModelCode(accessResult);
                    if (valid) textViewModelCode.setText(accessResult.substring(5));
                    else Toast.makeText(MainActivity.mContext, "This is not Micron 0X tag !!!", Toast.LENGTH_SHORT).show();
                } else if (readWriteTypes == ReadWriteTypes.CALIBRATION) {
                    textViewCalibrationOk.setText("O");
                    //checkBoxCalibration.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    setCalibrationVersion(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.SENSORCODE) {
                    textViewSensorCodeOk.setText("O");
                    //checkBoxSensorCode.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    setSensorCode(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.RSSICODE) {
                    textViewRssiCodeOk.setText("O");
                    //checkBoxRssiCode.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    setRssiCode(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.TEMPERATURECODE) {
                    textViewTemperatureCodeOk.setText("O");
                    //checkBoxTemperatureCode.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() >= 4) {
                        setTemperatureCode(accessResult.substring(0,4));
                    }
                }
            }
            accessTask = null;
            return true;
        }
    }

    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accSize = 0, accOffset = 0;
        String writeData = "";

        if (editTextRWTagID.getText().toString().length() == 0) invalidRequest1 = true;
        else if (checkBoxConfig.isChecked() == true && checkProcessing < 1 && operationRead) {
            accBank = 2; accOffset = 0; accSize = 2; readWriteTypes = ReadWriteTypes.MODELCODE; checkProcessing = 1;
            textViewConfigOk.setText(""); textViewModelCode.setText(""); modelCode = -1;
        } else if (checkBoxCalibration.isChecked() == true && checkProcessing < 2 && operationRead && (modelCode == 3 || modelCode == 5)) {
            accBank = 3; accSize = 4; readWriteTypes = ReadWriteTypes.CALIBRATION; checkProcessing = 2;
            accOffset = 8; if (modelCode == 5) accOffset = 18;
            textViewCalibrationOk.setText(""); textViewCalibrationVersion.setText(""); calVer = -1;
        } else if (checkBoxSensorCode.isChecked() == true && modelCode != -1 && checkProcessing < 3 && operationRead) {
            accBank = 0; if (modelCode == 1) accBank = 3;
            accOffset = 11; if (modelCode == 3 || modelCode == 5) accOffset = 12;
            accSize = 1; readWriteTypes = ReadWriteTypes.SENSORCODE; checkProcessing = 3;
            textViewSensorCodeOk.setText(""); textViewSensorCode.setText("");
        } else if (checkBoxRssiCode.isChecked() == true && modelCode != -1 && checkProcessing < 4 && operationRead) {
            int offset = 0xA0;
            if (modelCode == 3) offset = 0xD0;
            else if (modelCode == 5) offset = 0x3D0;
            MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 5, selectHold,3, offset, "3F");
            changedSelectIndex = true;
            accBank = 0; accOffset = 13; if (modelCode == 1) { accBank = 3; accOffset = 9; }
            accSize = 1; readWriteTypes = ReadWriteTypes.RSSICODE; checkProcessing = 4;
            textViewRssiCodeOk.setText(""); textViewRssiCode.setText("");
        } else if (checkBoxTemperatureCode.isChecked() == true && (modelCode == 3 || modelCode == 5) && checkProcessing < 5 && operationRead) {
            if (modelCode == 3) MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 2, 0,3, 0xE0, "");
            else MainActivity.csLibrary4A.setSelectCriteria(1, true, 4, 5, selectHold,3, 0x3B0, "00");
            changedSelectIndex = true;
            accBank = 0; accOffset = 14; accSize = 1; readWriteTypes = ReadWriteTypes.TEMPERATURECODE; checkProcessing = 5;
            textViewTemperatureCodeOk.setText(""); textViewTemperatureCode.setText("");
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
        return invalidRequest1;
    }
}
