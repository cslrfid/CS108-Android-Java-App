package com.csl.cs108ademoapp.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

public class UtraceFragment extends CommonFragment {
    final boolean DEBUG = true;
    SelectTag selectTag;
    Spinner memoryBankSpinner;
	EditText editTextRWTagID, editTextAccessRWAccPassword;
    CheckBox checkBoxHideXpc, checkBoxHideEpc, checkBoxHideTid, checkBoxHideUser, checkBoxHideRange;
    RadioButton radioButtonRangeToggle, radioButtonRangeReduced, radioButtonHideSomeTid, radioButtonHideAllTid;

    EditText editTextEpcSize;
    private Button buttonUntrace; String strUntraceButtonBackup;
    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_utrace, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        selectTag = new SelectTag((Activity)getActivity(), 2);

        checkBoxHideXpc = (CheckBox) getActivity().findViewById(R.id.utraceAssertUXPC);
        checkBoxHideEpc = (CheckBox) getActivity().findViewById(R.id.utraceHideEpc);
        checkBoxHideEpc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView textView = (TextView) getActivity().findViewById(R.id.utraceEpcLengthTitle);
                if (isChecked) {
                    textView.setVisibility(View.VISIBLE);
                    editTextEpcSize.setVisibility(View.VISIBLE);
                } else {
                    textView.setVisibility(View.INVISIBLE);
                    editTextEpcSize.setVisibility(View.INVISIBLE);
                }
            }
        });
        checkBoxHideTid = (CheckBox) getActivity().findViewById(R.id.utraceHideTid);
        radioButtonHideSomeTid = (RadioButton) getActivity().findViewById(R.id.utraceHideSomeTid);
        radioButtonHideAllTid = (RadioButton) getActivity().findViewById(R.id.utraceHideAllTid);
        checkBoxHideTid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RadioButton radioButton1 = (RadioButton) getActivity().findViewById(R.id.utraceHideSomeTid);
                RadioButton radioButton2 = (RadioButton) getActivity().findViewById(R.id.utraceHideAllTid);
                if (isChecked) {
                    radioButton1.setVisibility(View.VISIBLE);
                    radioButton2.setVisibility(View.VISIBLE);
                } else {
                    radioButton1.setVisibility(View.INVISIBLE);
                    radioButton2.setVisibility(View.INVISIBLE);
                }
            }
        });
        checkBoxHideUser = (CheckBox) getActivity().findViewById(R.id.utraceHideUser);

        checkBoxHideRange = (CheckBox) getActivity().findViewById(R.id.utraceHideRange);
        radioButtonRangeToggle = (RadioButton) getActivity().findViewById(R.id.utraceRangeToggle);
        radioButtonRangeReduced = (RadioButton) getActivity().findViewById(R.id.utraceRangeReduced);
        checkBoxHideRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RadioButton radioButton1 = (RadioButton) getActivity().findViewById(R.id.utraceRangeToggle);
                RadioButton radioButton2 = (RadioButton) getActivity().findViewById(R.id.utraceRangeReduced);
                if (isChecked) {
                    radioButton1.setVisibility(View.VISIBLE);
                    radioButton2.setVisibility(View.VISIBLE);
                } else {
                    radioButton1.setVisibility(View.INVISIBLE);
                    radioButton2.setVisibility(View.INVISIBLE);
                }
            }
        });

        editTextEpcSize = (EditText) getActivity().findViewById(R.id.utraceEpcLength);

        selectTag.editTextAccessAntennaPower.setText(String.valueOf(300));

        buttonUntrace = (Button) getActivity().findViewById(R.id.utraceUntraceButton);
        buttonUntrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                startAccessTask();
            }
        });

        MainActivity.csLibrary4A.appendToLog("going to setupTagID"); setupTagID();
        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.csLibrary4A.appendToLog("going to setupTagID"); setupTagID();
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
            MainActivity.csLibrary4A.appendToLog("going to setupTagID"); setupTagID();
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("UtraceFragment is now VISIBLE");
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("UtraceFragment is now INVISIBLE");
        }
    }

    public UtraceFragment() {
        super("UtraceFragment");
    }

    void setupTagID() {
        MainActivity.csLibrary4A.appendToLog("selectTag 1 = " + (selectTag != null ? "Valid" : "Null"));
        if (selectTag == null) return;
        ReaderDevice tagSelected = MainActivity.tagSelected;
        MainActivity.csLibrary4A.appendToLog("Start with tagSelected = " + (tagSelected == null ? "NULL" : (tagSelected.getSelected() + ", " + tagSelected.getAddress())));
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                bSelected = true;
                MainActivity.csLibrary4A.appendToLog("selectTag is " + (selectTag == null ? "NULL" : "valid"));
                if (selectTag != null) MainActivity.csLibrary4A.appendToLog("selectTag.editTextTag is " + (selectTag.editTextTagID == null ? "NULL" : "valid"));
                if (selectTag.editTextTagID != null) {
                    MainActivity.csLibrary4A.appendToLog("editTextRWTagID.setText " + tagSelected.getAddress());
                    selectTag.editTextTagID.setText(tagSelected.getAddress());
                }

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

    void startAccessTask() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("startAccessTask()");
        if (updating == false) {
            updating = true;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { MainActivity.csLibrary4A.appendToLog("processResult is TRUE"); }
            else if (taskRequest) {
                int rangeValue = 0;
                if (checkBoxHideRange.isChecked()) {
                    if (radioButtonRangeToggle.isChecked()) rangeValue = 1;
                    else if (radioButtonRangeReduced.isChecked()) rangeValue = 2;
                }
                int tidValue = 0;
                if (checkBoxHideTid.isChecked()) {
                    if (radioButtonHideSomeTid.isChecked()) tidValue = 1;
                    else if (radioButtonHideAllTid.isChecked()) tidValue = 2;
                }
                int epcValue = 0;
                epcValue = Integer.parseInt(editTextEpcSize.getText().toString());
                if (epcValue < 0 || epcValue > 31) {
                    epcValue = 6; editTextEpcSize.setText("6");
                }
                boolean invalid = (MainActivity.csLibrary4A.setUntraceable(rangeValue, checkBoxHideUser.isChecked(), tidValue, epcValue, checkBoxHideEpc.isChecked(), checkBoxHideXpc.isChecked()) == false);
                MainActivity.csLibrary4A.appendToLog("processTickItems, invalid = " + invalid);

                Button button = buttonUntrace; int selectBank = selectTag.spinnerSelectBank.getSelectedItemPosition() + 1; MainActivity.csLibrary4A.appendToLog("selectBank = " + selectBank);
                //if (strUntraceButtonBackup == null) strUntraceButtonBackup = buttonUntrace.getText().toString(); buttonUntrace.setText("Show"); button = buttonUntrace;
                accessTask = new AccessTask(
                        button, null, invalid,
                        selectTag.editTextTagID.getText().toString(), selectBank, (selectBank == 1 ? 32 : 0),
                        selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_UNTRACEABLE,
                        0, 0, true, false,
                        null, null, null, null, null);
                MainActivity.csLibrary4A.appendToLog("setSelectCriteria: going to execute accessTask");
                accessTask.execute();
                rerunRequest = true;
                MainActivity.csLibrary4A.appendToLog("setSelectCriteria: accessTask is executed");
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("updateRunnable(): Ending with updating = " + updating);
        }
    };

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            //if (strUntraceButtonBackup != null) buttonUntrace.setText(strUntraceButtonBackup); strUntraceButtonBackup = null;
            accessTask = null;
            return true;
        }
    }
}
