package com.csl.cs108ademoapp.fragments;

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

import androidx.annotation.NonNull;

import com.csl.cslibrary4a.AccessTaskCustom;
import com.csl.cslibrary4a.CustomAsyncTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

public class AccessSecurityLockFragment extends CommonFragment {
    private EditText editTextTagID, editTextPassword, editTextAntennaPower;
    private CheckBox checkBox;
    private Spinner spinner4KillPwd, spinner4AccessPwd, spinner4EpcMemory, spinner4TidMemory, spinner4UserMemory;
    private Button button;

    private AccessTaskCustom accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_access_lock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTagID = (EditText) view.findViewById(R.id.accessLockTagID);
        editTextPassword = (EditText) view.findViewById(R.id.accessLockPasswordValue);
        editTextPassword.addTextChangedListener(new GenericTextWatcher(editTextPassword, 8));
        editTextPassword.setText("00000000");

        checkBox = (CheckBox) view.findViewById(R.id.accessLockAllPermLock);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.access_lock_privilege_array, R.layout.custom_spinner_layout);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner4KillPwd = (Spinner) view.findViewById(R.id.accessLockPrivilege4KillPwd);
        spinner4KillPwd.setAdapter(arrayAdapter);

        spinner4AccessPwd = (Spinner) view.findViewById(R.id.accessLockPrivilege4AccessPwd);
        spinner4AccessPwd.setAdapter(arrayAdapter);

        spinner4EpcMemory = (Spinner) view.findViewById(R.id.accessLockPrivilege4EpcMemory);
        spinner4EpcMemory.setAdapter(arrayAdapter);

        spinner4TidMemory = (Spinner) view.findViewById(R.id.accessLockPrivilege4TidMemory);
        spinner4TidMemory.setAdapter(arrayAdapter);

        spinner4UserMemory = (Spinner) view.findViewById(R.id.accessLockPrivilege4UserMemory);
        spinner4UserMemory.setAdapter(arrayAdapter);

        editTextAntennaPower = (EditText) view.findViewById(R.id.accessLockAntennaPower);
        editTextAntennaPower.setText(String.valueOf(300));

        button = (Button) view.findViewById(R.id.accessLockButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
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
        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint2(true);
    }

    @Override
    public void onPause() {
        setUserVisibleHint2(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setNotificationListener(null);
        if (accessTask != null) accessTask.cancel(true);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("AccessSecurityLockFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) { //getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessSecurityLockFragment is now VISIBLE");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessSecurityLockFragment is now INVISIBLE");
            MainActivity.csLibrary4A.setNotificationListener(null);
        }
    }

    public AccessSecurityLockFragment() {
        super("AccessSecurityLockFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new NotificationConnector.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.csLibrary4A.appendToLog("TRIGGER key is pressed.");
                if (MainActivity.csLibrary4A.getTriggerButtonStatus()) startAccessTask();
            }
        });
    }

    void startAccessTask() {
        int accessLockAction = 0;
        int accessLockMask = 0;
        int lockAction;

        if (accessTask != null) if (accessTask.getStatus() == CustomAsyncTask.Status.RUNNING) return;
        if (checkBox.isChecked()) {
            accessLockAction = 0x3FF;
            accessLockMask = 0x3FF;
        } else {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));
            lockAction = spinner4KillPwd.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 8);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 8);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("Kill: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4AccessPwd.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 6);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 6);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("Access: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4EpcMemory.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 4);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 4);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("Epc: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4TidMemory.getSelectedItemPosition();
            accessLockAction |= ((lockAction == 0 ? 0 : lockAction - 1) << 2);
            accessLockMask |= ((lockAction == 0 ? 0 : 3) << 2);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("Tid: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));

            lockAction = spinner4UserMemory.getSelectedItemPosition();
            accessLockAction |= (lockAction == 0 ? 0 : lockAction - 1);
            accessLockMask |= (lockAction == 0 ? 0 : 3);
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("Uesr: accessLockAction = " + String.format("%x", accessLockAction) + ", accessLockMask = " + String.format("%x", accessLockMask));
        }

        boolean invalidRequest = false;
        String strTagID = editTextTagID.getText().toString();
        String strPassword = editTextPassword.getText().toString();
        int powerLevel = Integer.valueOf(editTextAntennaPower.getText().toString());
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessLockAction(accessLockAction, accessLockMask) == false) {
                invalidRequest = true;
            }
        }
        accessTask = MainActivity.csLibrary4A.getAccessTaskCustom(button, null, invalidRequest, true,
                strTagID, 1, 32,
                strPassword, powerLevel, RfidReaderChipData.HostCommands.CMD_18K6CLOCK,
                0, 0, true, false,
                null, null, null, null, null,
                MainActivity.sharedObjects.playerN, MainActivity.sharedObjects.playerO);
        accessTask.execute();
    }
}
