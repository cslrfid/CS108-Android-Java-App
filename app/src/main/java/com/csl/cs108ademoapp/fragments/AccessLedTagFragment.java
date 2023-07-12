package com.csl.cs108ademoapp.fragments;

import androidx.lifecycle.Lifecycle;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessLedTagFragment extends CommonFragment {
    final boolean DEBUG = true;
    EditText editTextRWTagID, editTextaccessRWAntennaPower;

    TextView textViewOk;
    CheckBox checkBox;
    TextView textView;

    private Button buttonRead;

    enum ReadWriteTypes {
        NULL, READVALUE
    }
    ReadWriteTypes readWriteTypes;

    private AccessTask accessTask;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_ledtag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextRWTagID = (EditText) getActivity().findViewById(R.id.accessLEDTagID);
        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.accessLEDAntennaPower);

        textViewOk = (TextView) getActivity().findViewById(R.id.accessLedTagResultOK);
        checkBox = (CheckBox) getActivity().findViewById(R.id.accessLedTagResultTitle);
        textView = (TextView) getActivity().findViewById(R.id.accessLedTagResult);

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
                startAccessTask();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) == false) return;
        if(getUserVisibleHint()) {
            setupTagID();
        }
    }

    public AccessLedTagFragment() {
        super("AccessLedTagFragment");
    }

    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                if (editTextRWTagID != null) editTextRWTagID.setText(tagSelected.getAddress());
            }
        }
    }

    void startAccessTask() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("startAccessTask()");
        if (updating == false) {
            updating = true; bankProcessing = 0; checkProcessing = 0;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0; int checkProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): Beginning"); ///
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): NULL accessReadWriteTask"); ///
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): FINISHED accessReadWriteTask"); ///
            }
            if (processResult()) {
                rerunRequest = true; MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): processResult is TRUE");
            } else {
                MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment: processResult is false with taskRequest = " + taskRequest); ///
                if (taskRequest) {
                    boolean invalid = processTickItems();
                    MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): processTickItems with invalid = " + invalid + ", bankProcessing = " + bankProcessing + ", checkProcessing = " + checkProcessing); ///
                    if (bankProcessing++ != 0 && invalid == true)   {
                        CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.accessLedTagRepeat);
                        rerunRequest = true;
                        if (checkBox.isChecked()) { bankProcessing = 0; checkProcessing = 0; }
                        else rerunRequest = false;
                    } else {
                        accessTask = new AccessTask(
                                buttonRead, invalid,
                                editTextRWTagID.getText().toString(), 1, 32,
                                "00000000", Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()), Cs108Library4A.HostCommands.CMD_18K6CREAD,
                                true, null);
                        accessTask.execute();
                        rerunRequest = true;
                        MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): accessTask is created"); ///
                    }
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): Restart"); ///
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): Ending with updating = " + updating); ///
        }
    };

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessLedTagFragment(): accessResult = " + accessResult);
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.READVALUE) {
                    textViewOk.setText("E");
                }
            } else {
                if (readWriteTypes == ReadWriteTypes.READVALUE) {
                    textViewOk.setText("O");
                    textView.setText(accessResult);
                    readWriteTypes = ReadWriteTypes.NULL;
                }
            }
            accessTask = null;
            return true;
        }
    }

    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accSize = 0, accOffset = 0;

        if (editTextRWTagID.getText().toString().length() == 0) invalidRequest1 = true;
        MainActivity.csLibrary4A.appendToLog("1: invalidRequest1 = " + invalidRequest1);

        if (checkBox.isChecked() == true && checkProcessing < 1) {
            accBank = 2; accSize = 2; accOffset = 0;
            readWriteTypes = ReadWriteTypes.READVALUE; checkProcessing = 1;
            textViewOk.setText(""); textView.setText("");
        } else {
            invalidRequest1 = true;
        }
        MainActivity.csLibrary4A.appendToLog("2: invalidRequest1 = " + invalidRequest1);

        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                invalidRequest1 = true;
            }
        }
        MainActivity.csLibrary4A.appendToLog("3: invalidRequest1 = " + invalidRequest1);

        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
        }
        MainActivity.csLibrary4A.appendToLog("4: invalidRequest1 = " + invalidRequest1);

        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (MainActivity.csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
        }
        MainActivity.csLibrary4A.appendToLog("5: invalidRequest1 = " + invalidRequest1);

        return invalidRequest1;
    }
}
