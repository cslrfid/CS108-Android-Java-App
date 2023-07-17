package com.csl.cs108ademoapp.fragments;

import androidx.lifecycle.Lifecycle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessXerxesLoggerFragment extends CommonFragment {
    final boolean DEBUG = true;
    boolean bXerxesEnable = false;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewUserCode1OK, textViewUserCode2OK, textViewUserCode3OK, textViewUserCode4OK, textViewUserCode5OK;
    CheckBox checkBoxUserCode1, checkBoxUserCode2, checkBoxUserCode3, checkBoxUserCode4, checkBoxUserCode5;
    EditText editTextUserCode1, editTextUserCode2, editTextUserCode3, editTextUserCode4, editTextUserCode5;
    Spinner spinnerUserCode2Unit, spinnerUserCode3Unit; int iUserCode2UnitPosition, iUserCode3UnitPosition;
    String strReadUserCode1, strReadUserCode2, strReadUserCode3, strReadUserCode4, strReadUserCode5;
	private Button buttonRead, buttonWrite;

    enum ReadWriteTypes {
        NULL, USERCODE1, USERCODE2, USERCODE3, USERCODE4, USERCODE5
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;

    private AccessTask accessTask;
    private int modelCode = 0;
    private int calCode1, calTemp1, calCode2, calTemp2, calVer = -1;
    private boolean changedSelectIndex = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_xerxes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessXXTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.accessXXAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

        textViewUserCode1OK = (TextView) getActivity().findViewById(R.id.accessXXXerxes1OK);
        textViewUserCode2OK = (TextView) getActivity().findViewById(R.id.accessXXXerxes2OK);
        textViewUserCode3OK = (TextView) getActivity().findViewById(R.id.accessXXXerxes3OK);
        textViewUserCode4OK = (TextView) getActivity().findViewById(R.id.accessXXXerxes4OK);
        textViewUserCode5OK = (TextView) getActivity().findViewById(R.id.accessXXXerxes5OK);

        checkBoxUserCode1 = (CheckBox) getActivity().findViewById(R.id.accessXXXerxees1Title); checkBoxUserCode1.setText("Log number:");
        checkBoxUserCode2 = (CheckBox) getActivity().findViewById(R.id.accessXXXerxees2Title); checkBoxUserCode2.setText("Temperature:");
        checkBoxUserCode3 = (CheckBox) getActivity().findViewById(R.id.accessXXXerxees3Title); checkBoxUserCode3.setText("Maximum temperature:");
        checkBoxUserCode4 = (CheckBox) getActivity().findViewById(R.id.accessXXXerxees4Title); checkBoxUserCode4.setText("Alarm high trigger count:");
        checkBoxUserCode5 = (CheckBox) getActivity().findViewById(R.id.accessXXXerxees5Title); checkBoxUserCode5.setText("Alarm low trigger count:");

        editTextUserCode1 = (EditText) getActivity().findViewById(R.id.accessXXXerxes1Code); editTextUserCode1.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextUserCode2 = (EditText) getActivity().findViewById(R.id.accessXXXerxes2Code); editTextUserCode2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editTextUserCode3 = (EditText) getActivity().findViewById(R.id.accessXXXerxes3Code); editTextUserCode3.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editTextUserCode4 = (EditText) getActivity().findViewById(R.id.accessXXXerxes4Code); editTextUserCode4.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextUserCode5 = (EditText) getActivity().findViewById(R.id.accessXXXerxes5Code); editTextUserCode5.setInputType(InputType.TYPE_CLASS_NUMBER);

        ArrayAdapter<CharSequence> arrayAdapterUserCode2Unit = ArrayAdapter.createFromResource(getActivity(), R.array.temperature_unit_options, R.layout.custom_spinner_layout);
        arrayAdapterUserCode2Unit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserCode2Unit = (Spinner) getActivity().findViewById(R.id.accessXXXerxes2Unit);
        spinnerUserCode2Unit.setAdapter(arrayAdapterUserCode2Unit);
        spinnerUserCode2Unit.setSelection(0); iUserCode2UnitPosition = 0;
        spinnerUserCode2Unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != iUserCode2UnitPosition) {
                    String strValue = editTextUserCode2.getText().toString();
                    if (iUserCode2UnitPosition == 0 && i == 1) strValue = MainActivity.csLibrary4A.temperatureC2F(strValue);
                    else if (iUserCode2UnitPosition == 1 && i == 0) strValue = MainActivity.csLibrary4A.temperatureF2C(strValue);
                    editTextUserCode2.setText(strValue);
                }
                iUserCode2UnitPosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerUserCode3Unit = (Spinner) getActivity().findViewById(R.id.accessXXXerxes3Unit);
        spinnerUserCode3Unit.setAdapter(arrayAdapterUserCode2Unit);
        spinnerUserCode3Unit.setSelection(0); iUserCode3UnitPosition = 0;
        spinnerUserCode3Unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != iUserCode3UnitPosition) {
                    String strValue = editTextUserCode3.getText().toString();
                    if (iUserCode3UnitPosition == 0 && i == 1) strValue = MainActivity.csLibrary4A.temperatureC2F(strValue);
                    else if (iUserCode3UnitPosition == 1 && i == 0) strValue = MainActivity.csLibrary4A.temperatureF2C(strValue);
                    editTextUserCode3.setText(strValue);
                }
                iUserCode3UnitPosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessXXAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        buttonRead = (Button) getActivity().findViewById(R.id.accessXXReadButton);
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
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessXXWriteButton);
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
                operationRead = false; startAccessTask();
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
        }
    }

    public AccessXerxesLoggerFragment() {
        super("AccessXerxesLoggerFragment");
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

                if (tagSelected.getMdid() == null) {
                } else if (tagSelected.getMdid().contains("E282402")) {
                    modelCode = 2;
                } else if (tagSelected.getMdid().contains("E282403")) {
                    modelCode = 3;
                } else if (tagSelected.getMdid().contains("E282405")) {
                    modelCode = 5;
                }

                String strRes = tagSelected.getRes();
                if (strRes != null) {
                    int ibracket = strRes.indexOf("(");
                    if (ibracket > 0) strRes = strRes.substring(0, ibracket);
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
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): FINISHED accessReadWriteTask");
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
                            (operationRead ? buttonRead : buttonWrite), null,
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
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): Ending with updating = " + updating);
        }
    };

    boolean setTemperatureCode(String strData, int iTempIndex) {
        if (strData == null)    return false;
        if (strData.length() < 4) return false;

        String strValue = MainActivity.csLibrary4A.strFloat16toFloat32(strData);
        if (strValue == null) return false;
        else {
            if ( (iTempIndex == 0 && spinnerUserCode2Unit.getSelectedItemPosition() == 1)
            || (iTempIndex == 1 && spinnerUserCode3Unit.getSelectedItemPosition() == 1) )
                strValue = MainActivity.csLibrary4A.temperatureC2F(strValue);
            if (iTempIndex == 0)    editTextUserCode2.setText(strValue);
            else    editTextUserCode3.setText(strValue);
            }
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
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode1 = String.valueOf(iValue);
                        editTextUserCode1.setText(strReadUserCode1);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE2) {
                    textViewUserCode2OK.setText("O");
                    //checkBoxUserCode2.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  setTemperatureCode(accessResult, 0);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE3) {
                    textViewUserCode3OK.setText("O");
                    //checkBoxUserCode3.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  setTemperatureCode(accessResult, 1);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE4) {
                    textViewUserCode4OK.setText("O");
                    //checkBoxUserCode4.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode4 = String.valueOf(iValue);
                        editTextUserCode4.setText(strReadUserCode4);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE5) {
                    textViewUserCode5OK.setText("O");
                    //checkBoxUserCode5.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode5 = String.valueOf(iValue);
                        editTextUserCode5.setText(strReadUserCode5);
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
        else if (checkBoxUserCode1.isChecked() == true && checkProcessing < 1 && modelCode == 5) {
            accBank = 3; accSize = 1; accOffset = 2; readWriteTypes = ReadWriteTypes.USERCODE1; checkProcessing = 1;
            if (operationRead) {
                textViewUserCode1OK.setText("");
                editTextUserCode1.setText("");
            } else {
                String strValue = editTextUserCode1.getText().toString();
                if (strValue.length() == 0) invalidRequest1 = true;
                else {
                    int iValue = Integer.parseInt(strValue);
                    if (iValue > 0xFFFF) invalidRequest1 = true;
                    else writeData = String.format("%04X", iValue);
                }
            }
        } else if (checkBoxUserCode2.isChecked() == true && checkProcessing < 2 && modelCode == 5) {
            accBank = 3; accSize = 1; accOffset = 3; readWriteTypes = ReadWriteTypes.USERCODE2; checkProcessing = 2;
            if (operationRead) {
                textViewUserCode2OK.setText("");
                editTextUserCode2.setText("");
            } else {
                String strValue = editTextUserCode2.getText().toString();
                if (strValue.length() == 0) invalidRequest1 = true;
                else {
                    if (spinnerUserCode2Unit.getSelectedItemPosition() == 1)    strValue =  MainActivity.csLibrary4A.temperatureF2C(strValue);
                    strValue = MainActivity.csLibrary4A.str2float16(strValue);
                    if (strValue.length() != 4) invalidRequest1 = true;
                    else writeData = strValue;
                }
            }
        } else if (checkBoxUserCode3.isChecked() == true && checkProcessing < 3 && modelCode == 5) {
            accBank = 3; accSize = 1; accOffset = 4; readWriteTypes = ReadWriteTypes.USERCODE3; checkProcessing = 3;
            if (operationRead) {
                textViewUserCode3OK.setText("");
                editTextUserCode3.setText("");
            } else {
                String strValue = editTextUserCode3.getText().toString();
                if (strValue.length() == 0) invalidRequest1 = true;
                else {
                    if (spinnerUserCode3Unit.getSelectedItemPosition() == 1)    strValue =  MainActivity.csLibrary4A.temperatureF2C(strValue);
                    strValue = MainActivity.csLibrary4A.str2float16(strValue);
                    if (strValue.length() != 4) invalidRequest1 = true;
                    else writeData = strValue;
                }
            }
        } else if (checkBoxUserCode4.isChecked() == true && checkProcessing < 4 && modelCode == 5) {
            accBank = 3; accSize = 1; accOffset = 5; readWriteTypes = ReadWriteTypes.USERCODE4; checkProcessing = 4;
            if (operationRead) {
                textViewUserCode4OK.setText("");
                editTextUserCode4.setText("");
            } else {
                String strValue = editTextUserCode4.getText().toString();
                if (strValue.length() == 0) invalidRequest1 = true;
                else {
                    int iValue = Integer.parseInt(strValue);
                    if (iValue > 0xFFFF) invalidRequest1 = true;
                    else writeData = String.format("%04X", iValue);
                }
            }
        } else if (checkBoxUserCode5.isChecked() == true && checkProcessing < 5 && modelCode == 5) {
            accBank = 3; accSize = 1; accOffset = 6; readWriteTypes = ReadWriteTypes.USERCODE5; checkProcessing = 5;
            if (operationRead) {
                textViewUserCode5OK.setText("");
                editTextUserCode5.setText("");
            } else {
                String strValue = editTextUserCode5.getText().toString();
                if (strValue.length() == 0) invalidRequest1 = true;
                else {
                    int iValue = Integer.parseInt(strValue);
                    if (iValue > 0xFFFF) invalidRequest1 = true;
                    else writeData = String.format("%04X", iValue);
                }
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
