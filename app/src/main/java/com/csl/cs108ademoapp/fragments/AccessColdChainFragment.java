package com.csl.cs108ademoapp.fragments;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessColdChainFragment extends CommonFragment {
    final boolean DEBUG = true;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewConfigOk, textViewTemperatureOk, textViewEnableOk;
    CheckBox checkBoxConfig, checkBoxTemperature, checkBoxEnable;

    EditText editTextTempThresUnder, editTextTempThresOver, editTextTempCountUnder, editTextTempCountOver, editTextMonitorDelay, editTextSamplingInterval;
    TextView textViewTemperature, textViewUnderAlarm, textViewOverAlarm, textViewBatteryAlarm;
    Spinner spinnerDelayUnit, spinnerIntervalUnit, spinnerEnable, spinnerTagType;
	Button buttonRead, buttonWrite, buttonStartLogging, buttonStopLogging, buttonCheckAlarm, buttonGetLogging;
	TextView textViewStartLoggingStatus, textViewStopLoggingStatus, textViewCheckAlaramStatus, textViewGetLoggingStatus;

    enum ReadWriteTypes {
        NULL, TEMPERATURE, CONFIGURATION, ENABLE,
        STARTLOGGING, STOPLOGGING, CHECKLOGGING, GETLOGGING
    }
    boolean operationRead = false;
    ReadWriteTypes readWriteTypes;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_coldchain, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessCCTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.accessCCAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

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

        TextView textViewDegreeC = (TextView) getActivity().findViewById(R.id.accessCCDegreeC);
        textViewDegreeC.setText(textViewDegreeC.getText().toString() + (char) 0x00B0 + "C)");

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

        ArrayAdapter<CharSequence> arrayAdapterTagType = ArrayAdapter.createFromResource(getActivity(), R.array.coldChain_tagtype_options, R.layout.custom_spinner_layout);
        arrayAdapterTagType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagType = (Spinner) getActivity().findViewById(R.id.selectCCTagType);
        spinnerTagType.setAdapter(arrayAdapterTagType);
        spinnerTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessCCAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        buttonStartLogging = (Button) getActivity().findViewById(R.id.accessCCStartLogging);
        buttonStartLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.STARTLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonStopLogging = (Button) getActivity().findViewById(R.id.accessCCStopLogging);
        buttonStopLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.STOPLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonCheckAlarm = (Button) getActivity().findViewById(R.id.accessCCcheckAlarm);
        buttonCheckAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.CHECKLOGGING;
                operationRead = true; startAccessTask();
            }
        });
        buttonGetLogging = (Button) getActivity().findViewById(R.id.accessCCGetLogging);
        buttonGetLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.GETLOGGING;
                operationRead = true; startAccessTask();
            }
        });

        textViewStartLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCCStartLoggingStatus);
        textViewStopLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCCStopLoggingStatus);
        textViewCheckAlaramStatus = (TextView) getActivity().findViewById(R.id.accessCCcheckAlarmStatus);
        textViewGetLoggingStatus = (TextView) getActivity().findViewById(R.id.accessCCGetLoggingStatus);

        buttonRead = (Button) getActivity().findViewById(R.id.accessCCReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.NULL;
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessCCWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.NULL;
                operationRead = false; startAccessTask();
            }
        });

        setupTagID();
        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment is now VISIBLE");
            setupTagID();
            //            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment is now INVISIBLE");
//            MainActivity.mCs108Library4a.setNotificationListener(null);
        }
    }

    public AccessColdChainFragment() {
        super("AccessColdChainFragment");
    }

    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                bSelected = true;
                if (editTextRWTagID != null) editTextRWTagID.setText(tagSelected.getAddress());

                String stringDetail = tagSelected.getDetails();
                int indexUser = stringDetail.indexOf("USER=");
                if (indexUser != -1) {
                    String stringUser = stringDetail.substring(indexUser + 5);
                    MainActivity.csLibrary4A.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
//                    CheckBox checkBoxBAP = (CheckBox) getActivity().findViewById(R.id.coldChainEnableBAP);
//                    checkBoxBAP.setChecked(bEnableBAPMode);
                }
            }
        }
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
            updating = true; bankProcessing = 0;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment().updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment().updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment().updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.csLibrary4A.appendToLog("processResult is TRUE");}
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("processTickItems, invalid = " + invalid);
                if (bankProcessing++ != 0 && invalid) rerunRequest = false;
                else  {
                    Cs108Library4A.HostCommands hostCommand;
                    if (readWriteTypes == ReadWriteTypes.TEMPERATURE) hostCommand = Cs108Library4A.HostCommands.CMD_GETSENSORDATA;
                    else if (operationRead) hostCommand = Cs108Library4A.HostCommands.CMD_18K6CREAD;
                    else hostCommand = Cs108Library4A.HostCommands.CMD_18K6CWRITE;
                    accessTask = new AccessTask(
                            (operationRead ? buttonRead : buttonWrite), null,
                            invalid,
                            editTextRWTagID.getText().toString(), 1, 32,
                            editTextAccessRWAccPassword.getText().toString(),
                            Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                            hostCommand,
                            0, 0, true,
                            null, null, null, null, null);
                    accessTask.execute();
                    rerunRequest = true;
                    MainActivity.csLibrary4A.appendToLog("accessTask is created");
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment().updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessColdChainFragment().updateRunnable(): Ending with updating = " + updating);
        }
    };

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

    //    int accessBank, accSize, accOffset;
//    int restartCounter = 0; int restartAccessBank = -1;
    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            MainActivity.csLibrary4A.appendToLog("accessResult 2 bankProcewssing = " + bankProcessing + ", accessResult = " + accessTask.accessResult );
            if (readWriteTypes == ReadWriteTypes.STARTLOGGING) textViewStartLoggingStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.STOPLOGGING) textViewStopLoggingStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.CHECKLOGGING) textViewCheckAlaramStatus.setText(accessResult);
            else if (readWriteTypes == ReadWriteTypes.GETLOGGING) textViewGetLoggingStatus.setText(accessResult);
            else if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.CONFIGURATION) {
                    textViewConfigOk.setText("E"); checkBoxConfig.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead) {
                    textViewTemperatureOk.setText("E"); checkBoxTemperature.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.ENABLE) {
                    textViewEnableOk.setText("E"); checkBoxEnable.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.CONFIGURATION) {
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
                } else if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead) {
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
                } else if (readWriteTypes == ReadWriteTypes.ENABLE) {
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
    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accSize = 0, accOffset = 0;
        String writeData = "";

        if (readWriteTypes == ReadWriteTypes.STARTLOGGING || readWriteTypes == ReadWriteTypes.STOPLOGGING || readWriteTypes == ReadWriteTypes.CHECKLOGGING || readWriteTypes == ReadWriteTypes.GETLOGGING) {
            MainActivity.csLibrary4A.appendToLog("accessResult 1 bankProcewssing = " + bankProcessing );
            accOffset = 0xF0; accSize = 1; operationRead = true;
            if (readWriteTypes == ReadWriteTypes.STARTLOGGING) {
                switch(bankProcessing) {
                    case 0:
                        textViewStartLoggingStatus.setText("");
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
                        textViewStartLoggingStatus.setText("");
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
                        textViewStartLoggingStatus.setText("");
                        break;
                    case 1:
                        accOffset = 0x108; accSize = 1; writeData = "";
                        operationRead = true;
                        break;
                    case 2:
                        int iValue = 0;
                        try {
                            iValue = Integer.parseInt(textViewCheckAlaramStatus.getText().toString(), 16);
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
                        textViewGetLoggingStatus.setText("");
                        break;
                    default:
                        invalidRequest1 = true; readWriteTypes = ReadWriteTypes.NULL;
                        break;
                }
            }
        } else if (checkBoxConfig.isChecked() == true) {
            accOffset = 0xEC; accSize = 3; readWriteTypes = ReadWriteTypes.CONFIGURATION; textViewConfigOk.setText("");
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
            readWriteTypes = ReadWriteTypes.TEMPERATURE;
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
                    accOffset = 0x100; accSize = 1; operationRead = false;
                } else invalidRequest1 = true;
            } else {
                accOffset = 0x100; accSize = 1; operationRead = true;
            }
        } else if (checkBoxEnable.isChecked() == true) {
            accOffset = 0x10D; accSize = 1; readWriteTypes = ReadWriteTypes.ENABLE; textViewEnableOk.setText("");
            if (operationRead) spinnerEnable.setSelection(0);
            else {
                int iSelect = spinnerEnable.getSelectedItemPosition();
                if (iSelect == 0) invalidRequest1 = true;
                else if (iSelect == 1) writeData = "0000";
                else writeData = "0001";
                    String  stringValue = "0000";
            }
        } else {
            invalidRequest1 = true;
        }

        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(3) == false) {
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
