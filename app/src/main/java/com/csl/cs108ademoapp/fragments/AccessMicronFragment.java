package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON_OPUS;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_MAGNUS_S1;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_MAGNUS_S2;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_MAGNUS_S3;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON_XERXES;

import androidx.annotation.NonNull;

import android.graphics.Color;
import android.os.Bundle;
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

import com.csl.cslibrary4a.AccessTaskCustom;
import com.csl.cslibrary4a.CustomAsyncTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.CustomTabLayout;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;
import com.csl.cslibrary4a.RfidReaderChipData;

public class AccessMicronFragment extends CommonFragment {
    final boolean DEBUG = true;
    boolean bXerxesEnable = false;
    View viewFragment;
    EditText editTextaccessRWSelectHoldTime, editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewSelectHoldTimeLabel, textViewConfigOk, textViewCalibrationOk, textViewAnalogPort2CodeOk, textViewAnalogPort1CodeOk, textViewSensorCodeOk,
            textViewRssiCodeOk, textViewTemperatureCodeOk;
    CheckBox checkBoxConfig, checkBoxCalibration, checkBoxAnalogPort1Code, checkBoxAnalogPort2Code, checkBoxSensorCode,
            checkBoxRssiCode, checkBoxTemperatureCode;
    Spinner spinnerTagType, spinnerSensorUnit, spinnerTemperatureUnit;
    boolean btagTypeSelected = false;

    TextView textViewModelCode, textViewCalibrationVersion, textViewAnalogPort1Code, textViewAnalogPort2Code, textViewSensorCode,
            textViewRssiCode, textViewTemperatureCode;
	private Button buttonRead;

    enum ReadWriteTypes {
        NULL, MODELCODE, CALIBRATION, SENSORCODE, RSSICODE, RSSISELECT, TEMPERATURECODE
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;

    private AccessTaskCustom accessTask;
    private int modelCode = 0, selectHold = 15;
    private int calCode1, calTemp1, calCode2, calTemp2, calVer = -1;
    private boolean changedSelectIndex = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewFragment = inflater.inflate(R.layout.fragment_access_micron, container, false);
        return viewFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextRWTagID = (EditText) view.findViewById(R.id.accessMNTagID);
        editTextAccessRWAccPassword = (EditText) view.findViewById(R.id.accessMNAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText(MainActivity.config.configPassword);

        if (MainActivity.config != null) { if (MainActivity.config.config0 != null) selectHold = Integer.parseInt(MainActivity.config.config0); }
        EditText editText = (EditText) view.findViewById(R.id.accessMNRssiUpperLimit);
        editText.setText(MainActivity.config.configRssiUpperLimit);
        editText = (EditText) view.findViewById(R.id.accessMNRssiLowerLimit);
        editText.setText(MainActivity.config.configRssiLowerLimit);
        editText = (EditText) view.findViewById(R.id.accessMNHumidityThreshold);
        editText.setText(MainActivity.config.configHumidityThreshold);
        TableRow tableRow = (TableRow) view.findViewById(R.id.accessMNHumidityThresholdRow);
        MainActivity.csLibrary4A.appendToLog("AccessMicronFragment.onViewCreated: DebugABC, MainActivity.tagType = " + MainActivity.tagType.toString());
        if (MainActivity.tagType == TAG_AXZON) tableRow.setVisibility(View.GONE);

        textViewConfigOk = (TextView) view.findViewById(R.id.accessMNModelCodeOK);
        textViewCalibrationOk = (TextView) view.findViewById(R.id.accessMNCalibrationOK);
        textViewAnalogPort1CodeOk = (TextView) view.findViewById(R.id.accessMNAnalogPort1CodeOK);
        textViewAnalogPort2CodeOk = (TextView) view.findViewById(R.id.accessMNAnalogPort2CodeOK);
        textViewSensorCodeOk = (TextView) view.findViewById(R.id.accessMNSensorCodeOK);
        textViewRssiCodeOk = (TextView) view.findViewById(R.id.accessMNRssiCodeOK);
        textViewTemperatureCodeOk = (TextView) view.findViewById(R.id.accessMNTemperatureCodeOK);

        checkBoxConfig = (CheckBox) view.findViewById(R.id.accessMNModelCodeTitle);
        checkBoxCalibration = (CheckBox) view.findViewById(R.id.accessMNCalibrationTitle);
        checkBoxAnalogPort1Code = (CheckBox) view.findViewById(R.id.accessMNAnalogPort1CodeTitle); checkBoxAnalogPort1Code.setEnabled(false);
        checkBoxAnalogPort2Code = (CheckBox) view.findViewById(R.id.accessMNAnalogPort2CodeTitle); checkBoxAnalogPort2Code.setEnabled(false);
        checkBoxSensorCode = (CheckBox) view.findViewById(R.id.accessMNSensorCodeTitle);
        checkBoxRssiCode = (CheckBox) view.findViewById(R.id.accessMNRssiCodeTitle);
        checkBoxTemperatureCode = (CheckBox) view.findViewById(R.id.accessMNTemperatureCodeTitle);

        textViewModelCode = (TextView) view.findViewById(R.id.accessMNModelCode);

        textViewAnalogPort1Code = (TextView) view.findViewById(R.id.accessMNAnalogPort1Code);
        textViewAnalogPort2Code = (TextView) view.findViewById(R.id.accessMNAnalogPort2Code);
        textViewSensorCode = (TextView) view.findViewById(R.id.accessMNSensorCode);
        textViewRssiCode = (TextView) view.findViewById(R.id.accessMNRssiCode);
        textViewCalibrationVersion = (TextView) view.findViewById(R.id.accessMNCalibrationVersion);
        textViewTemperatureCode = (TextView) view.findViewById(R.id.accessMNTemperatureCode);

        ArrayAdapter<CharSequence> arrayAdapterTagType;
        if (bXerxesEnable) arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.xerxesTag_options, R.layout.custom_spinner_layout);
        else arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.rfMicronTag_options, R.layout.custom_spinner_layout);
        arrayAdapterTagType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagType = (Spinner) view.findViewById(R.id.accessMNTagType);
        spinnerTagType.setAdapter(arrayAdapterTagType);
        spinnerTagType.setEnabled(true );
        if (true) {
            if (MainActivity.tagType == TAG_AXZON) spinnerTagType.setSelection(0);
            else if (MainActivity.tagType == TAG_MAGNUS_S2) spinnerTagType.setSelection(1);
            else if (MainActivity.tagType == TAG_MAGNUS_S3) spinnerTagType.setSelection(2);
            else if (MainActivity.tagType == TAG_AXZON_XERXES) spinnerTagType.setSelection(3);
            else if (MainActivity.tagType == TAG_AXZON_OPUS) spinnerTagType.setSelection(4);
        }
        spinnerTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.csLibrary4A.appendToLog("AccessMicroFragment.onItemSelected: 0 MainActivity.tagType = " + MainActivity.tagType.toString());
                LinearLayout layoutMagnus = (LinearLayout) viewFragment.findViewById(R.id.accessMNMagnusLayout);
                LinearLayout layoutTemperature = (LinearLayout) viewFragment.findViewById(R.id.accessMNTemperatureLayout);
                LinearLayout layoutBackport = (LinearLayout) viewFragment.findViewById(R.id.accessMNBackportLayout) ;
                CustomTabLayout tabLayout = (CustomTabLayout) getActivity().findViewById(R.id.OperationsTabLayout2);
                MainActivity.csLibrary4A.appendToLog("AccessMicronFragment.onViewCreated.onItemSelected: tabLayout is " + (tabLayout == null ? "null" : "valid"));

                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                        MainActivity.tagType = TAG_AXZON; MainActivity.mDid = ""; //""E2824";
                        break;
                    case 1:
                        MainActivity.tagType = TAG_MAGNUS_S2; MainActivity.mDid = ""; //""E282402";
                        break;
                    case 2:
                        MainActivity.tagType = TAG_MAGNUS_S3; MainActivity.mDid = ""; //""E282403";
                        break;
                    case 3:
                        //tabLayout.getTabAt(2).setCustomView(View.VISIBLE);
                        MainActivity.tagType = TAG_AXZON_XERXES; MainActivity.mDid = ""; //""E282405";
                        break;
                    case 4:
                        MainActivity.tagType = TAG_AXZON_OPUS; MainActivity.mDid = ""; //""E2C24500";
                        break;
                }
                MainActivity.csLibrary4A.appendToLog("AccessMicroFragment.onItemSelected: MainActivity.tagType = " + MainActivity.tagType.toString());

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
                        tabLayout.getTabAt(2).view.setVisibility(View.GONE);
                        tabLayout.getTabAt(3).view.setVisibility(View.GONE);
                        //textViewSelectHoldTimeLabel.setVisibility(View.GONE);
                        //editTextaccessRWSelectHoldTime.setVisibility(View.GONE);
                        break;
                    case 3:
                    case 4:
                        tabLayout.getTabAt(2).view.setVisibility(View.VISIBLE);
                        if (spinnerTagType.getSelectedItemPosition() == 3 && (MainActivity.csLibrary4A.get98XX() == 0 || MainActivity.csLibrary4A.getMacVer().indexOf("1.2") == 0)) {
                            tabLayout.getTabAt(3).view.setVisibility(View.VISIBLE);
                        } else tabLayout.getTabAt(3).view.setVisibility(View.GONE);
                        //textViewSelectHoldTimeLabel.setVisibility(View.VISIBLE);
                        //editTextaccessRWSelectHoldTime.setVisibility(View.VISIBLE);
                        break;
                }
                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 1:
                    case 2:
                    case 3:
                        layoutMagnus.setVisibility(View.VISIBLE);
                        break;
                    case 0:
                    case 4:
                        layoutMagnus.setVisibility(View.GONE);
                        break;
                }
                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                    case 1:
                    case 4:
                        layoutTemperature.setVisibility(View.GONE);
                        break;
                    case 2:
                    case 3:
                        layoutTemperature.setVisibility(View.VISIBLE);
                        break;
                }

                TableRow rowSelectHoldTime = (TableRow) viewFragment.findViewById(R.id.accessMNSelectHoldTimeRow);
                switch (spinnerTagType.getSelectedItemPosition()) {
                    case 0:
                    case 1:
                    case 2:
                    case 4:
                        rowSelectHoldTime.setVisibility(View.GONE);
                        layoutBackport.setVisibility(View.GONE);
                        break;
                    case 3:
                        rowSelectHoldTime.setVisibility(View.GONE);
                        layoutBackport.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> arrayAdapterSensorUnit = ArrayAdapter.createFromResource(getActivity(), R.array.sensor_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterSensorUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensorUnit = (Spinner) view.findViewById(R.id.accessMNSensorUnit);
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
        if (MainActivity.tagType == TAG_AXZON) spinnerSensorUnit.setEnabled(false);

        ArrayAdapter<CharSequence> arrayAdapterTemperatureUnit = ArrayAdapter.createFromResource(getActivity(), R.array.temperature_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterTemperatureUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemperatureUnit = (Spinner) view.findViewById(R.id.accessMNTemperatureUnit);
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

        textViewSelectHoldTimeLabel = (TextView) view.findViewById(R.id.accessMNSelectHoldTimeLabel);
        editTextaccessRWSelectHoldTime = (EditText) view.findViewById(R.id.accessMNSelectHoldTime);
        editTextaccessRWSelectHoldTime.setText(String.valueOf(selectHold));

        editTextaccessRWAntennaPower = (EditText) view.findViewById(R.id.accessMNAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(MainActivity.config.configPower));

        buttonRead = (Button) view.findViewById(R.id.accessMNReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
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
        setUserVisibleHint2(true);
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setSameCheck(true);
        setUserVisibleHint2(false);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("AccessMicronFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser + ", MainActivity.tagSelected = " + (MainActivity.tagSelected == null ? "null" : "valid"));
        //if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) == false) { MainActivity.csLibrary4A.appendToLog("AccessMicronFragment.setUserVisibleHint: return"); return; }
        if (isVisibleToUser) { //getUserVisibleHint()) {
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

            EditText editText = (EditText) viewFragment.findViewById(R.id.accessMNRssiUpperLimit);
            MainActivity.config.configRssiUpperLimit = editText.getText().toString();
            editText = (EditText) viewFragment.findViewById(R.id.accessMNRssiLowerLimit);
            MainActivity.config.configRssiLowerLimit = editText.getText().toString();
            editText = (EditText) viewFragment.findViewById(R.id.accessMNHumidityThreshold);
            MainActivity.config.configHumidityThreshold = editText.getText().toString();
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
        MainActivity.csLibrary4A.appendToLog("AccessMicroFragment.setupTagID: tagSelected = " + (tagSelected == null ? "null" : "valid"));
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

                } else if (tagSelected.getTagTypeExpected() == null) {
                } else if (tagSelected.getTagTypeExpected() == TAG_MAGNUS_S1) {
                    textViewModelCode.setText("01"); modelCode = 1;
                } else if (tagSelected.getTagTypeExpected() == TAG_MAGNUS_S2) {
                    textViewModelCode.setText("02"); modelCode = 2;
                } else if (tagSelected.getTagTypeExpected() == TAG_MAGNUS_S3) {
                    textViewModelCode.setText("03"); modelCode = 3;
                } else if (tagSelected.getTagTypeExpected() == TAG_AXZON_XERXES) {
                    textViewModelCode.setText("05"); modelCode = 5;
                } else if (tagSelected.getTagTypeExpected() == TAG_AXZON_OPUS) {
                    textViewModelCode.setText("50"); modelCode = 50;
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
            } else if (accessTask.getStatus() != CustomAsyncTask.Status.FINISHED) {
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
                    accessTask = MainActivity.csLibrary4A.getAccessTaskCustom(buttonRead, null, invalid, true,
                            selectMask, selectBank, selectOffset,
                            editTextAccessRWAccPassword.getText().toString(),
                            Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                            (operationRead ? RfidReaderChipData.HostCommands.CMD_18K6CREAD: RfidReaderChipData.HostCommands.CMD_18K6CWRITE),
                            0, 0, true, bSkipClearPrefilter,
                            null, null, null, null, null,
                            MainActivity.sharedObjects.playerN, MainActivity.sharedObjects.playerO);
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
        MainActivity.csLibrary4A.appendToLog("AccesssMicronFragment.setModelCode: DebugABC, strTid = " + strTid + ", MainActivity.tagType = " + MainActivity.tagType.toString() + ", tagSelected = " + MainActivity.tagSelected.getMdid());
        if (strTid == null) return false;
        if (strTid.length() <= 7) return false;
        RfidReader.TagType tagType = MainActivity.csLibrary4A.getagType(strTid);
        if (tagType == TAG_MAGNUS_S1) {
            modelCode = 1; return true;
        } else if (tagType == TAG_MAGNUS_S2) {
            modelCode = 2; return true;
        } else if (tagType == TAG_MAGNUS_S3) {
            modelCode = 3; return true;
        } else if (tagType == TAG_AXZON_XERXES) {
            modelCode = 5; return true;
        } else if (tagType == TAG_AXZON_OPUS) {
            modelCode = 50; return true;
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
        EditText editText = (EditText) viewFragment.findViewById(R.id.accessMNRssiLowerLimit);
        int iTempLower = Integer.parseInt(editText.getText().toString());
        editText = (EditText) viewFragment.findViewById(R.id.accessMNRssiUpperLimit);
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
        if (spinnerSensorUnit.getSelectedItemPosition() > 0) {
            float fValue = (float) Integer.parseInt(strData);
            if (spinnerSensorUnit.getSelectedItemPosition() == 2) {
                EditText editText = (EditText) viewFragment.findViewById(R.id.accessMNHumidityThreshold);
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
        else if (accessTask.getStatus() != CustomAsyncTask.Status.FINISHED) return false;
        else {
            if (changedSelectIndex) {
                changedSelectIndex = false; MainActivity.selectFor = 0;
                MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
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
                    else Toast.makeText(MainActivity.context, "This is not Micron 0X tag !!!", Toast.LENGTH_SHORT).show();
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

    boolean bSkipClearPrefilter = false;
    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accSize = 0, accOffset = 0;
        String writeData = "";

        bSkipClearPrefilter = false;
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
            accOffset = 12; if (modelCode == 1 || modelCode == 2) accOffset = 11;
            accSize = 1; readWriteTypes = ReadWriteTypes.SENSORCODE; checkProcessing = 3;
            textViewSensorCodeOk.setText(""); textViewSensorCode.setText("");
        } else if (checkBoxRssiCode.isChecked() == true && modelCode != -1 && checkProcessing < 4 && operationRead) {
            int offset = 0xA0;
            if (modelCode == 3) offset = 0xD0;
            else if (modelCode == 5) offset = 0x3D0;
            bSkipClearPrefilter = true;
            MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
            MainActivity.csLibrary4A.setSelectCriteria(-1, true, 4, 5, selectHold,3, offset, "1F");
            changedSelectIndex = true;
            accBank = 0; accOffset = 13; if (modelCode == 1) { accBank = 3; accOffset = 9; }
            accSize = 1; readWriteTypes = ReadWriteTypes.RSSICODE; checkProcessing = 4;
            textViewRssiCodeOk.setText(""); textViewRssiCode.setText("");
        } else if (checkBoxTemperatureCode.isChecked() == true && (modelCode == 3 || modelCode == 5) && checkProcessing < 5 && operationRead) {
            bSkipClearPrefilter = true;
            MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
            if (modelCode == 3) MainActivity.csLibrary4A.setSelectCriteria(-1, true, 4, 2, 0,3, 0xE0, "");
            else MainActivity.csLibrary4A.setSelectCriteria(-1, true, 4, 5, selectHold,3, 0x3B0, "00");
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
