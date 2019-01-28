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
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Connector;
import com.csl.cs108library4a.ReaderDevice;

public class AccessSecurityLockFragment extends CommonFragment {
    private EditText editTextTagID, editTextPassword, editTextAntennaPower;
    private CheckBox checkBox;
    private Spinner spinner4KillPwd, spinner4AccessPwd, spinner4EpcMemory, spinner4TidMemory, spinner4UserMemory;
    private Button button;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_lock, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextTagID = (EditText) getActivity().findViewById(R.id.accessLockTagID);
        editTextPassword = (EditText) getActivity().findViewById(R.id.accessLockPasswordValue);
        editTextPassword.addTextChangedListener(new GenericTextWatcher(editTextPassword, 8));
        editTextPassword.setText("00000000");

        checkBox = (CheckBox) getActivity().findViewById(R.id.accessLockAllPermLock);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.access_lock_privilege_array, R.layout.custom_spinner_layout);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner4KillPwd = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege4KillPwd);
        spinner4KillPwd.setAdapter(arrayAdapter);

        spinner4AccessPwd = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege4AccessPwd);
        spinner4AccessPwd.setAdapter(arrayAdapter);

        spinner4EpcMemory = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege4EpcMemory);
        spinner4EpcMemory.setAdapter(arrayAdapter);

        spinner4TidMemory = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege4TidMemory);
        spinner4TidMemory.setAdapter(arrayAdapter);

        spinner4UserMemory = (Spinner) getActivity().findViewById(R.id.accessLockPrivilege4UserMemory);
        spinner4UserMemory.setAdapter(arrayAdapter);

        editTextAntennaPower = (EditText) getActivity().findViewById(R.id.accessLockAntennaPower);
        editTextAntennaPower.setText(String.valueOf(300));

        button = (Button) getActivity().findViewById(R.id.accessLockButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                startAccessTask();
            }
        });

        ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                editTextTagID.setText(tagSelected.getAddress());
            }
        }
        MainActivity.mCs108Library4a.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AccessSecurityLockFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            setNotificationListener();
        }
    }

    @Override
    public void onPause() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
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
            MainActivity.mCs108Library4a.appendToLog("AccessSecurityLockFragment is now VISIBLE");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("AccessSecurityLockFragment is now INVISIBLE");
            MainActivity.mCs108Library4a.setNotificationListener(null);
        }
    }

    public AccessSecurityLockFragment() {
        super("AccessSecurityLockFragment");
    }

    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.mCs108Library4a.appendToLog("TRIGGER key is pressed.");
                if (MainActivity.mCs108Library4a.getTriggerButtonStatus()) startAccessTask();
            }
        });
    }

    void startAccessTask() {
        int accessLockAction = 0;
        int accessLockMask = 0;
        int lockAction;

        if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
        if (checkBox.isChecked()) {
            accessLockAction = 0x3FF;
            accessLockMask = 0x3FF;
        } else {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));
            lockAction = spinner4KillPwd.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 8);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 8);
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Kill: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4AccessPwd.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 6);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 6);
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Access: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4EpcMemory.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 4);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 4);
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Epc: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4TidMemory.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 2);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 2);
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Tid: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4UserMemory.getSelectedItemPosition();
            accessLockAction |= (lockAction == 0 ? 0 : lockAction - 1);
            accessLockMask |= (lockAction == 0 ? 0 : 3);
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Uesr: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));
        }

        boolean invalidRequest = false;
        String strTagID = editTextTagID.getText().toString();
        String strPassword = editTextPassword.getText().toString();
        int powerLevel = Integer.valueOf(editTextAntennaPower.getText().toString());
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.setAccessLockAction(accessLockAction, accessLockMask) == false) {
                invalidRequest = true;
            }
        }
        accessTask = new AccessTask(button, null, invalidRequest, strTagID, 1, 32, strPassword, powerLevel, Cs108Connector.HostCommands.CMD_18K6CLOCK, 0, false, false,true, null, null, null, null, null);
        accessTask.execute();
    }
}
