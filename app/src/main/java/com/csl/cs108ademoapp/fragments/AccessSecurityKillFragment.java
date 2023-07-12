package com.csl.cs108ademoapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessSecurityKillFragment extends CommonFragment {
    private EditText editTextTagID, editTextPassword, editTextAntennaPower;
    private Button button;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_kill, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextTagID = (EditText) getActivity().findViewById(R.id.accessKillTagID);
        editTextPassword = (EditText) getActivity().findViewById(R.id.accessKillPasswordValue);
        editTextPassword.addTextChangedListener(new GenericTextWatcher(editTextPassword, 8));
        editTextPassword.setText("00000000");

        editTextAntennaPower = (EditText) getActivity().findViewById(R.id.accessKillAntennaPower);
        editTextAntennaPower.setText(String.valueOf(300));

        button = (Button) getActivity().findViewById(R.id.accessKillButton);
        button.setOnClickListener(new View.OnClickListener() {
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
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessSecurityKillFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            setNotificationListener();
        }
    }

    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        if (accessTask != null) accessTask.cancel(true);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
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
        MainActivity.csLibrary4A.setNotificationListener(new Cs108Library4A.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.csLibrary4A.appendToLog("TRIGGER key is pressed.");
                if (MainActivity.csLibrary4A.getTriggerButtonStatus()) startAccessTask();
            }
        });
    }

    void startAccessTask() {
        if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
        boolean invalidRequest = false;
        String strTagID = editTextTagID.getText().toString();
        String strPassword = editTextPassword.getText().toString();
        int powerLevel = Integer.valueOf(editTextAntennaPower.getText().toString());
        accessTask = new AccessTask(button, null, invalidRequest, strTagID, 1, 32, strPassword, powerLevel, Cs108Library4A.HostCommands.CMD_18K6CKILL, 0, 0, true, null, null, null, null, null);
        accessTask.execute();
    }
}
