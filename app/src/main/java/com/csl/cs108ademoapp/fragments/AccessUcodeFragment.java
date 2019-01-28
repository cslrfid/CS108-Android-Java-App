package com.csl.cs108ademoapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Connector;
import com.csl.cs108library4a.ReaderDevice;

public class AccessUcodeFragment extends CommonFragment {
    final boolean DEBUG = true;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewAesKey0ActivateOk, textViewAesKey1ActivateOk, textViewAesKey0Ok, textViewAesKey1Ok;
    Spinner spinnerHideTid;
    CheckBox checkBoxHideEpc, checkBoxHideTid, checkBoxHideUser, checkBoxHideRange;
    CheckBox checkBoxAesKey0Activate, checkBoxAesKey1Activate, checkBoxAesKey0, checkBoxAesKey1;

    EditText editTextAuthMsg, editTextAuthResponse, editTextEpcSize, editTextAesKey0, editTextAesKey1;
	private Button buttonRead, buttonWrite;
    private Button buttonReadBuffer, buttonTam1, buttonTam2, buttonUntrace, buttonShowEpc; String strShowEpcButtonBackup;

    enum ReadWriteTypes {
        NULL, TEMPERATURE, AESKEY0, AESKEY1, AESKEY0ACTIVATE, AESKEY1ACTIVATE, ENABLE
    }
    boolean operationRead = false;
    boolean readBufferChecked = false;
    boolean authenChecked = false; boolean authenTam1;
    boolean untraceChecked = false;
    boolean showEpcChecked = false;
    ReadWriteTypes readWriteTypes;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_ucode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessUCTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.accessUCAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

        spinnerHideTid = (Spinner) getActivity().findViewById(R.id.accessUCHideTid);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.hideTid_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHideTid.setAdapter(targetAdapter);

        checkBoxHideEpc = (CheckBox) getActivity().findViewById(R.id.accessUCHideEpc);
        checkBoxHideTid = (CheckBox) getActivity().findViewById(R.id.accessUCHideTid1);
        checkBoxHideUser = (CheckBox) getActivity().findViewById(R.id.accessUCHideUser);
        checkBoxHideRange = (CheckBox) getActivity().findViewById(R.id.accessUCHideRange);
        textViewAesKey0ActivateOk = (TextView) getActivity().findViewById(R.id.accessUCAesKey0ActivateOK);
        textViewAesKey1ActivateOk = (TextView) getActivity().findViewById(R.id.accessUCAesKey1ActivateOK);
        checkBoxAesKey0Activate = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey0Activate);
        checkBoxAesKey1Activate = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey1Activate);

        textViewAesKey0Ok = (TextView) getActivity().findViewById(R.id.accessUCAesKey0OK);
        textViewAesKey1Ok = (TextView) getActivity().findViewById(R.id.accessUCAesKey1OK);
        checkBoxAesKey0 = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey0Title);
        checkBoxAesKey1 = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey1Title);

        editTextAuthMsg = (EditText) getActivity().findViewById(R.id.accessUCAuthMsg);
        editTextAuthMsg.addTextChangedListener(new GenericTextWatcher(editTextAuthMsg, 20));
        editTextAuthResponse = (EditText) getActivity().findViewById(R.id.accessUCAuthResponse);
        editTextEpcSize = (EditText) getActivity().findViewById(R.id.accessUCEpcSize);
        editTextAesKey0 = (EditText) getActivity().findViewById(R.id.accessUCAesKey0);
        editTextAesKey0.addTextChangedListener(new GenericTextWatcher(editTextAesKey0, 32));
        editTextAesKey1 = (EditText) getActivity().findViewById(R.id.accessUCAesKey1);
        editTextAesKey1.addTextChangedListener(new GenericTextWatcher(editTextAesKey1, 32));

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessUCAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        buttonRead = (Button) getActivity().findViewById(R.id.accessUCReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessUCWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = false; startAccessTask();
            }
        });

        buttonReadBuffer = (Button) getActivity().findViewById(R.id.accessUCReadBufferButton);
        buttonReadBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                readBufferChecked = true; startAccessTask();
            }
        });

        buttonTam1 = (Button) getActivity().findViewById(R.id.accessUCTam1AuthButton);
        buttonTam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                authenTam1 = true; authenChecked = true; startAccessTask();
            }
        });

        buttonTam2 = (Button) getActivity().findViewById(R.id.accessUCTam2AuthButton);
        buttonTam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                authenTam1 = false; authenChecked = true; startAccessTask();
            }
        });

        buttonUntrace = (Button) getActivity().findViewById(R.id.accessUCUntraceButton);
        buttonUntrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                untraceChecked = true; startAccessTask();
            }
        });

        buttonShowEpc = (Button) getActivity().findViewById(R.id.accessUCShowEpcButton);
        buttonShowEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                showEpcChecked = true; startAccessTask();
            }
        });

        MainActivity.mCs108Library4a.getAuthenticateReplyLength();
        MainActivity.mCs108Library4a.getUntraceableEpcLength();
        setupTagID();
        MainActivity.mCs108Library4a.setSameCheck(false);
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        MainActivity.mCs108Library4a.setSameCheck(true);
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.mCs108Library4a.appendToLog("AccessUcodeFragment is now VISIBLE");
            setupTagID();
            //            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("AccessUcodeFragment is now INVISIBLE");
//            MainActivity.mCs108Library4a.setNotificationListener(null);
        }
    }

    public AccessUcodeFragment() {
        super("AccessUcodeFragment");
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
                    MainActivity.mCs108Library4a.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
//                    CheckBox checkBoxBAP = (CheckBox) getActivity().findViewById(R.id.coldChainEnableBAP);
//                    checkBoxBAP.setChecked(bEnableBAPMode);
                }
            }
        }
    }

    void startAccessTask() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("startAccessTask()");
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
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.mCs108Library4a.appendToLog("processResult is TRUE"); }
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.mCs108Library4a.appendToLog("processTickItems, invalid = " + invalid);
                if (bankProcessing++ != 0 && invalid == true)   rerunRequest = false;
                else {
                    Button button;
                    if (readBufferChecked) button = buttonReadBuffer;
                    else if (authenChecked && authenTam1) button = buttonTam1;
                    else if (authenChecked) button = buttonTam2;
                    else if (untraceChecked) button = buttonUntrace;
                    else if (showEpcChecked) { if (strShowEpcButtonBackup == null) strShowEpcButtonBackup = buttonShowEpc.getText().toString(); buttonShowEpc.setText("Show"); button = buttonShowEpc; }
                    else if (operationRead) button = buttonRead;
                    else button = buttonWrite;

                    Cs108Connector.HostCommands hostCommand;
                    if (readBufferChecked) hostCommand = Cs108Connector.HostCommands.CMD_READBUFFER;
                    else if (authenChecked) hostCommand = Cs108Connector.HostCommands.CMD_AUTHENTICATE;
                    else if (untraceChecked || showEpcChecked) hostCommand = Cs108Connector.HostCommands.CMD_UNTRACEABLE;
                    else if (operationRead) hostCommand = Cs108Connector.HostCommands.CMD_18K6CREAD;
                    else hostCommand = Cs108Connector.HostCommands.CMD_18K6CWRITE;

                    accessTask = new AccessTask(
                            button, null,
                            invalid,
                            editTextRWTagID.getText().toString(), 1, 32,
                            editTextAccessRWAccPassword.getText().toString(),
                            Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                            hostCommand,
                            0, false, false,true,
                            null, null, null, null, null);
                    accessTask.execute();
                    rerunRequest = true;
                    MainActivity.mCs108Library4a.appendToLog("accessTask is created");
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.mCs108Library4a.appendToLog("updateRunnable(): Ending with updating = " + updating);
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

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            if (readBufferChecked) readBufferChecked = false;
            else if (authenChecked) { authenChecked = false; if (accessResult != null) editTextAuthResponse.setText(accessResult); }
            else if (untraceChecked) untraceChecked = false;
            else if (showEpcChecked) { showEpcChecked = false; if (strShowEpcButtonBackup != null) buttonShowEpc.setText(strShowEpcButtonBackup); strShowEpcButtonBackup = null; }
            else if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.AESKEY0ACTIVATE) {
                    textViewAesKey0ActivateOk.setText("E"); checkBoxAesKey0Activate.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1ACTIVATE) {
                    textViewAesKey1ActivateOk.setText("E"); checkBoxAesKey1Activate.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY0) {
                    textViewAesKey0Ok.setText("E"); checkBoxAesKey0.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1) {
                    textViewAesKey1Ok.setText("E"); checkBoxAesKey1.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.AESKEY0ACTIVATE) {
                    textViewAesKey0ActivateOk.setText("O"); checkBoxAesKey0Activate.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1ACTIVATE) {
                    textViewAesKey1ActivateOk.setText("O"); checkBoxAesKey1Activate.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                } else if (readWriteTypes == ReadWriteTypes.AESKEY0) {
                    textViewAesKey0Ok.setText("O"); checkBoxAesKey0.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAesKey0.setText(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1) {
                    textViewAesKey1Ok.setText("O"); checkBoxAesKey1.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAesKey1.setText(accessResult);
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

        if (readBufferChecked) {
            accOffset = 0; accSize = 1;
        } else if (authenChecked) {
            if (authenTam1) {
                if (MainActivity.mCs108Library4a.setTam1Configuration(editTextAuthMsg.getText().toString()) == false)
                    invalidRequest1 = true;
            } else if (MainActivity.mCs108Library4a.setTam2Configuration(editTextAuthMsg.getText().toString()) == false)
                invalidRequest1 = true;
            if (invalidRequest1 == false) editTextAuthResponse.setText("");
            return invalidRequest1;
        } else if (untraceChecked) {
            if (MainActivity.mCs108Library4a.setUntraceableEpc(checkBoxHideEpc.isChecked(), checkBoxHideEpc.isChecked() ? 2 : 6, checkBoxHideTid.isChecked() ? 1: 0, checkBoxHideUser.isChecked(), checkBoxHideRange.isChecked()) == false) invalidRequest1 = true;
            return invalidRequest1;
        } else if (showEpcChecked) {
            try {
                if (MainActivity.mCs108Library4a.setUntraceableEpc(false, Integer.parseInt(editTextEpcSize.getText().toString()), 0, false, false) == false) invalidRequest1 = true;
            } catch (Exception ex) {
                invalidRequest1 = true;
            }
            return invalidRequest1;
        } else if (checkBoxAesKey0Activate.isChecked() == true) {
            accOffset = 0xC8; accSize = 1; readWriteTypes = ReadWriteTypes.AESKEY0ACTIVATE; textViewAesKey0ActivateOk.setText("");
            if (operationRead == false) writeData = "E200";
        } else if (checkBoxAesKey1Activate.isChecked() == true) {
            accOffset = 0xD8; accSize = 1; readWriteTypes = ReadWriteTypes.AESKEY1ACTIVATE; textViewAesKey0ActivateOk.setText("");
            if (operationRead == false) writeData = "E200";
        } else if (checkBoxAesKey0.isChecked() == true) {
            accOffset = 0xC0; accSize = 8; readWriteTypes = ReadWriteTypes.AESKEY0; textViewAesKey0Ok.setText("");
            if (operationRead) editTextAesKey0.setText("");
            else writeData = editTextAesKey0.getText().toString();
        } else if (checkBoxAesKey1.isChecked() == true) {
            accOffset = 0xD0; accSize = 8; readWriteTypes = ReadWriteTypes.AESKEY1; textViewAesKey1Ok.setText("");
            if (operationRead) editTextAesKey1.setText("");
            else writeData = editTextAesKey1.getText().toString();
        } else {
            invalidRequest1 = true;
        }

        if (invalidRequest1 == false) {
            if (MainActivity.mCs108Library4a.setAccessBank(3) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (MainActivity.mCs108Library4a.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (MainActivity.mCs108Library4a.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false && operationRead == false) {
            if (invalidRequest1 == false) {
                if (MainActivity.mCs108Library4a.setAccessWriteData(writeData) == false) {
                    invalidRequest1 = true;
                }
            }
        }
        return invalidRequest1;
    }
}
