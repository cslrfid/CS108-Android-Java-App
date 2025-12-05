package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class AccessSecurityKillFragment extends CommonFragment {
    private EditText editTextTagID, editTextPassword, editTextAntennaPower;
    private Button button;

    private AccessTaskCustom accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_access_kill, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTagID = (EditText) view.findViewById(R.id.accessKillTagID);
        editTextPassword = (EditText) view.findViewById(R.id.accessKillPasswordValue);
        editTextPassword.addTextChangedListener(new GenericTextWatcher(editTextPassword, 8));
        editTextPassword.setText("00000000");

        editTextAntennaPower = (EditText) view.findViewById(R.id.accessKillAntennaPower);
        editTextAntennaPower.setText(String.valueOf(300));

        button = (Button) view.findViewById(R.id.accessKillButton);
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
        MainActivity.csLibrary4A.appendToLog("AccessSecurityKillFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if(isVisibleToUser) { //getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessSecurityKillFragment is now VISIBLE");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessSecurityKillFragment is now INVISIBLE");
            MainActivity.csLibrary4A.setNotificationListener(null);
        }
    }

    public AccessSecurityKillFragment() {
        super("AccessSecurityKillFragment");
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
        if (accessTask != null) if (accessTask.getStatus() == CustomAsyncTask.Status.RUNNING) return;
        boolean invalidRequest = false;
        String strTagID = editTextTagID.getText().toString();
        String strPassword = editTextPassword.getText().toString();
        int powerLevel = Integer.valueOf(editTextAntennaPower.getText().toString());
        accessTask = MainActivity.csLibrary4A.getAccessTaskCustom(button, null, invalidRequest, true,
                strTagID, 1, 32,
                strPassword, powerLevel, RfidReaderChipData.HostCommands.CMD_18K6CKILL,
                0, 0, true, false,
                null, null, null, null, null,
                MainActivity.sharedObjects.playerN, MainActivity.sharedObjects.playerO);
        accessTask.execute();
    }
}
