package com.csl.cs108ademoapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessEm4325PassiveFragment extends CommonFragment {
    final boolean DEBUG = true;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;

    TextView textViewTemperature;
	Button buttonRead;

    enum ReadWriteTypes {
        NULL, TEMPERATURE
    }
    boolean operationRead = false;
    ReadWriteTypes readWriteTypes;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_em4325passive, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessCCPTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.accessCCPAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

        textViewTemperature = (TextView) getActivity().findViewById(R.id.accessCCPTemperature);

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessCCPAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        buttonRead = (Button) getActivity().findViewById(R.id.accessCCPReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperationRunning()) return;
                readWriteTypes = ReadWriteTypes.NULL;
                bRequestCheck = true;
                operationRead = true; startAccessTask();
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
            MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment is now VISIBLE");
            setupTagID();
            //            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment is now INVISIBLE");
//            MainActivity.mCs108Library4a.setNotificationListener(null);
        }
    }

    public AccessEm4325PassiveFragment() {
        super("AccessEm4325PassiveFragment");
    }

    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                if (editTextRWTagID != null) editTextRWTagID.setText(tagSelected.getAddress());
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
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment().updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment().updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment().updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.csLibrary4A.appendToLog("updateRunnable: processResult is TRUE with bankprocessing = " + bankProcessing);}
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("updateRunnable: processTickItems Result = " + invalid + ", bankprocessing = " + bankProcessing);
                if (bankProcessing++ != 0 && invalid) rerunRequest = false;
                else  {
                    Cs108Library4A.HostCommands hostCommand;
                    if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead) hostCommand = Cs108Library4A.HostCommands.CMD_GETSENSORDATA;
                    else if (operationRead) hostCommand = Cs108Library4A.HostCommands.CMD_18K6CREAD;
                    else hostCommand = Cs108Library4A.HostCommands.CMD_18K6CWRITE;
                    accessTask = new AccessTask(
                            buttonRead, null,
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
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment().updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessEm4325PassiveFragment().updateRunnable(): Ending with updating = " + updating);
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


    boolean bRequestCheck;
    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            MainActivity.csLibrary4A.appendToLog("accessResult 2 bankProcessing = " + bankProcessing + ", accessResult = " + accessTask.accessResult );
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead) {
                    bRequestCheck = false;
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead == false) {
                } else if (readWriteTypes == ReadWriteTypes.TEMPERATURE && operationRead) {
                    bRequestCheck = false; readWriteTypes = ReadWriteTypes.NULL;
                    MainActivity.csLibrary4A.appendToLog("accessResult of Temperature = " + accessResult);

                    if (accessResult.length() >= 16) {
                        int indexBegin = accessResult.length() - 16;
                        String stringValue = accessResult.substring(indexBegin, indexBegin + 4);
                        MainActivity.csLibrary4A.appendToLog("temperature part of Temperature accessResult = " + stringValue);
                        accessResult = stringValue; //"00B5"; //stringValue;
                    }
                    if (accessResult.length() == 4) {
                        byte bValue = Byte.parseByte(accessResult.substring(1, 2), 16);
                        Integer iValue2 = Integer.parseInt(accessResult.substring(2, 4), 16);
                        iValue2 &= 0x1FF;
                        if ((bValue & 0x01) != 0 && iValue2 == 0) textViewTemperature.setText("Invalid");
                        else {
                            String stringValue = getTemperatue(accessResult.substring(1, 4));
                            stringValue += (char) 0x00B0 + "C";
                            textViewTemperature.setText(stringValue);
                        }
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

        if (bRequestCheck) {
            readWriteTypes = ReadWriteTypes.TEMPERATURE;
            if (bankProcessing == 0) {
                if (operationRead) {
                    textViewTemperature = (TextView) getActivity().findViewById(R.id.accessCCPTemperature);
                    textViewTemperature.setText("");
                    accOffset = 0x10D; accSize = 1; operationRead = false; writeData = "0000";
                } else invalidRequest1 = true;
            } else {
                operationRead = true;
                MainActivity.csLibrary4A.macWrite(0x11F, 3);
                return false;
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
