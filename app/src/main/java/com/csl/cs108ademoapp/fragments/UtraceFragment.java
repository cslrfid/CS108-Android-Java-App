package com.csl.cs108ademoapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class UtraceFragment extends CommonFragment {
    final boolean DEBUG = true;
    Spinner memoryBankSpinner;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
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

        memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.utraceBank);
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memoryBankSpinner.setAdapter(memoryBankAdapter);
        memoryBankSpinner.setEnabled(true);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.utraceTagID);
        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.utracePasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

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

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.utraceAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

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

        setupTagID();
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
            setupTagID();
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

                Button button = null; int selectBank = memoryBankSpinner.getSelectedItemPosition() + 1; MainActivity.csLibrary4A.appendToLog("selectBank = " + selectBank);
                if (strUntraceButtonBackup == null) strUntraceButtonBackup = buttonUntrace.getText().toString(); buttonUntrace.setText("Show"); button = buttonUntrace;
                accessTask = new AccessTask(
                        button, null,
                        invalid,
                        editTextRWTagID.getText().toString(), selectBank, (selectBank == 1 ? 32 : 0),
                        editTextAccessRWAccPassword.getText().toString(),
                        Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                        Cs108Library4A.HostCommands.CMD_UNTRACEABLE,
                        0, 0, true,
                        null, null, null, null, null);
                accessTask.execute();
                rerunRequest = true;
                MainActivity.csLibrary4A.appendToLog("accessTask is created");
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
            if (strUntraceButtonBackup != null) buttonUntrace.setText(strUntraceButtonBackup); strUntraceButtonBackup = null;
            accessTask = null;
            return true;
        }
    }
}
