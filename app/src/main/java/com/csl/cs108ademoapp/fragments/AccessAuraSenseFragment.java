package com.csl.cs108ademoapp.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cs108library4a.Cs108Library4A;

public class AccessAuraSenseFragment extends CommonFragment {
    final boolean DEBUG = true;
    RadioButton radioButtonAuraSensAtBoot, radioButtonAuraSensAtSelect;
    Button buttonRead, buttonWrite;

    SelectTag selectTag;

    TextView textViewUserCode1OK, textViewUserCode2OK, textViewUserCode3OK, textViewUserCode4OK, textViewUserCode5OK;
    CheckBox checkBoxUserCode1, checkBoxUserCode2, checkBoxUserCode3, checkBoxUserCode4, checkBoxUserCode5;
    EditText editTextUserCode1, editTextUserCode2, editTextUserCode3, editTextUserCode5;
    CheckBox checkBoxUserCode4a, checkBoxUserCode4b, checkBoxUserCode4c;

    enum ReadWriteTypes {
        NULL, USERCODE1, USERCODE2, USERCODE3, USERCODE4, USERCODE5
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_aurasense, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        radioButtonAuraSensAtBoot = (RadioButton) getActivity().findViewById(R.id.accessAuraSensAtBoot);
        radioButtonAuraSensAtSelect = (RadioButton) getActivity().findViewById(R.id.accessAuraSensAtSelect);

        selectTag = new SelectTag((Activity)getActivity ());
        selectTag.tableRowSelectMemoryBank.setVisibility(View.GONE);

        textViewUserCode2OK = (TextView) getActivity().findViewById(R.id.accessAuraSystemConfigurationOK);
        textViewUserCode3OK = (TextView) getActivity().findViewById(R.id.accessAuraSensorCalibrationOK);
        textViewUserCode4OK = (TextView) getActivity().findViewById(R.id.accessAuraSensorControlOK);
        textViewUserCode5OK = (TextView) getActivity().findViewById(R.id.accessAuraSensorDataStoredOK);

        checkBoxUserCode2 = (CheckBox) getActivity().findViewById(R.id.accessAuraSystemConfigurationCheck);
        checkBoxUserCode3 = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorCalibrationCheck);
        checkBoxUserCode4 = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorControlCheck);
        checkBoxUserCode5 = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorDataStoredCheck);

        editTextUserCode1 = (EditText) getActivity().findViewById(R.id.accessAuraSensorData); editTextUserCode1.setEnabled(false);
        editTextUserCode2 = (EditText) getActivity().findViewById(R.id.accessAuraSystemConfiguration); editTextUserCode2.setEnabled(false);
        editTextUserCode3 = (EditText) getActivity().findViewById(R.id.accessAuraSensorCalibration); editTextUserCode3.setInputType(InputType.TYPE_CLASS_NUMBER);
        checkBoxUserCode4a = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtBootCheck);
        checkBoxUserCode4b = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtSelectCheck);
        checkBoxUserCode4c = (CheckBox) getActivity().findViewById(R.id.accessAuraSensAtWriteCheck);
        editTextUserCode5 = (EditText) getActivity().findViewById(R.id.accessAuraSensorDataStored); editTextUserCode5.setEnabled(false);
        buttonRead = (Button) getActivity().findViewById(R.id.accessRWReadButton);
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
                textViewUserCode1OK = (TextView) getActivity().findViewById(R.id.accessAuraSensorDataOK);
                checkBoxUserCode1 = (CheckBox) getActivity().findViewById(R.id.accessAuraSensorDataCheck);
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessRWWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                textViewUserCode1OK = (TextView) getActivity().findViewById(R.id.accessAuraWriteSensorDataOK);
                checkBoxUserCode1 = (CheckBox) getActivity().findViewById(R.id.accessAuraWriteSensorDataCheck);
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
        }
        else {
            if (radioButtonAuraSensAtBoot != null && radioButtonAuraSensAtSelect != null) {
                if (radioButtonAuraSensAtBoot.isChecked()) MainActivity.mDid = "E280B12A";
                if (radioButtonAuraSensAtSelect.isChecked()) MainActivity.mDid = "E280B12B";
            }
            userVisibleHint = false;
        }
    }

    public AccessAuraSenseFragment() {
        super("AccessAuraSenseFragment");
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
                if (bankProcessing++ != 0 && invalid == true)   rerunRequest = false;
                else {
                    String selectMask = selectTag.editTextTagID.getText().toString();
                    int selectBank = selectTag.spinnerSelectBank.getSelectedItemPosition()+1;
                    int selectOffset = Integer.valueOf(selectTag.editTextSelectOffset.getText().toString());
                    accessTask = new AccessTask(
                            (operationRead ? buttonRead : buttonWrite), null,
                            invalid,
                            selectMask, selectBank, selectOffset,
                            selectTag.editTextAccessPassword.getText().toString(),
                            Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()),
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
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.USERCODE1) {
                    textViewUserCode1OK.setText("E");
                    //checkBoxUserCode1.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE2) {
                    textViewUserCode2OK.setText("E");
                    //checkBoxUserCode2.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE3) {
                    textViewUserCode3OK.setText("E");
                    //checkBoxUserCode3.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE4) {
                    textViewUserCode4OK.setText("E");
                    //checkBoxUserCode4.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE5) {
                    textViewUserCode5OK.setText("E");
                    //checkBoxUserCode5.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.USERCODE1) {
                    textViewUserCode1OK.setText("O");
                    //checkBoxUserCode1.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextUserCode1.setText(showSensorData(accessResult));
                } else if (readWriteTypes == ReadWriteTypes.USERCODE2) {
                    textViewUserCode2OK.setText("O");
                    //checkBoxUserCode2.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  editTextUserCode2.setText(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE3) {
                    textViewUserCode3OK.setText("O");
                    //checkBoxUserCode3.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        editTextUserCode3.setText(String.valueOf(iValue & 0xFF));
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE4) {
                    textViewUserCode4OK.setText("O");
                    //checkBoxUserCode4.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        if ((iValue & 0x2000) != 0) checkBoxUserCode4a.setChecked(true);
                        if ((iValue & 0x4000) != 0) checkBoxUserCode4b.setChecked(true);
                        if ((iValue & 0x8000) != 0) checkBoxUserCode4c.setChecked(true);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE5) {
                    textViewUserCode5OK.setText("O");
                    //checkBoxUserCode5.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextUserCode5.setText(showSensorData(accessResult));
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
        else if (checkBoxUserCode1.isChecked() == true && checkProcessing < 1) {
            accBank = 1; accSize = 1; accOffset = 0x22; readWriteTypes = ReadWriteTypes.USERCODE1; checkProcessing = 1;
            if (operationRead) {
                textViewUserCode1OK.setText("");
                editTextUserCode1.setText("");
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
        } else if (checkBoxUserCode2.isChecked() == true && checkProcessing < 2 && operationRead) {
            accBank = 3; accSize = 1; accOffset = 0x120; readWriteTypes = ReadWriteTypes.USERCODE2; checkProcessing = 2;
            if (operationRead) {
                textViewUserCode2OK.setText("");
                editTextUserCode2.setText("");
            }
        } else if (checkBoxUserCode3.isChecked() == true && checkProcessing < 3) {
            accBank = 3; accSize = 1; accOffset = 0x122; readWriteTypes = ReadWriteTypes.USERCODE3; checkProcessing = 3;
            if (operationRead) {
                textViewUserCode3OK.setText("");
                editTextUserCode3.setText("");
            } else {
                String strValue = editTextUserCode3.getText().toString();
                Integer iValue = Integer.valueOf(strValue);
                iValue &= 0xFF;
                writeData = String.format("%04X", iValue);
            }
        } else if (checkBoxUserCode4.isChecked() == true && checkProcessing < 4) {
            accBank = 3; accSize = 1; accOffset = 0x123; readWriteTypes = ReadWriteTypes.USERCODE4; checkProcessing = 4;
            if (operationRead) {
                textViewUserCode4OK.setText("");
                checkBoxUserCode4a.setChecked(false); checkBoxUserCode4b.setChecked(false); checkBoxUserCode4c.setChecked(false);
            } else {
                int iValue = (checkBoxUserCode4a.isChecked() ? 0x2000 : 0) | (checkBoxUserCode4b.isChecked() ? 0x4000 : 0) | (checkBoxUserCode4c.isChecked() ? 0x8000 : 0);
                writeData = String.format("%04X", iValue);
            }
        } else if (checkBoxUserCode5.isChecked() == true && checkProcessing < 5 && operationRead) {
            accBank = 3; accSize = 1; accOffset = 0x124; readWriteTypes = ReadWriteTypes.USERCODE5; checkProcessing = 5;
            if (operationRead) {
                textViewUserCode5OK.setText("");
                editTextUserCode5.setText("");
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
        return invalidRequest1;
    }
}
