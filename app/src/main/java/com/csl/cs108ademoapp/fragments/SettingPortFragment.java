package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingPortFragment extends CommonFragment {
    private CheckBox checkBoxPortEnable;
    private EditText editTextPortChannel, editTextPortPower, editTextPortDwell;
    private Button button;

    final boolean sameCheck = true;
    Handler mHandler = new Handler();

    boolean portEnable;
    int channel = -1; final int channelMin = 1; int channelMax = 1;
    long powerLevel = -1; final long powerLevelMin = 0; final long powerLevelMax = 300;
    long dwellTime = -1; final long dwellTimeMin = 0; final long dwellTimeMax = 10000;
    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_settings_port, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxPortEnable = (CheckBox) getActivity().findViewById(R.id.settingPortEnable);

        int iTemp = MainActivity.mCs108Library4a.getPortNumber();
        if (iTemp > 1) channelMax = iTemp;
        if (channelMax != 1) {
            TextView textViewPortChannelLabel = (TextView) getActivity().findViewById(R.id.settingPortChannelLabel);
            String stringPortChannelLabel = textViewPortChannelLabel.getText().toString();
            stringPortChannelLabel += "(" + String.valueOf(channelMin) + "-" + String.valueOf(channelMax) + ")";
            textViewPortChannelLabel.setText(stringPortChannelLabel);
        }
        editTextPortChannel = (EditText) getActivity().findViewById(R.id.settingPortChannel);

        TextView textViewPortPowerLabel = (TextView) getActivity().findViewById(R.id.settingPortPowerLabel);
        String stringPortPowerLabel = textViewPortPowerLabel.getText().toString();
        stringPortPowerLabel += "(" + String.valueOf(powerLevelMin) + "-" + String.valueOf(powerLevelMax) + ")";
        textViewPortPowerLabel.setText(stringPortPowerLabel);
        editTextPortPower = (EditText) getActivity().findViewById(R.id.settingPortPower);

        TextView textViewPortDwellLabel = (TextView) getActivity().findViewById(R.id.settingPortDwellLabel);
        String stringPortDwellLabel = textViewPortDwellLabel.getText().toString();
        stringPortDwellLabel += "(" + String.valueOf(dwellTimeMin) + "-" + String.valueOf(dwellTimeMax) + ")";
        textViewPortDwellLabel.setText(stringPortDwellLabel);
        editTextPortDwell = (EditText) getActivity().findViewById(R.id.settingPortDwell);

        button = (Button) getActivity().findViewById(R.id.settingSaveButtonPort);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validValue = false;
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else if (updateRunning) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        channel = Integer.parseInt(editTextPortChannel.getText().toString());
                        portEnable = checkBoxPortEnable.isChecked();
                        powerLevel = Long.parseLong(editTextPortPower.getText().toString());
                        dwellTime = Long.parseLong(editTextPortDwell.getText().toString());
                        settingUpdate();
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (sameCheck == false) MainActivity.mCs108Library4a.setSameCheck(false);
        mHandler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        if (settingTask != null) settingTask.cancel(true);
        MainActivity.mCs108Library4a.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            checkBoxPortEnable.setChecked(MainActivity.mCs108Library4a.getAntennaEnable());
        }
    }

    public SettingPortFragment() {
        super("SettingPortFragment");
    }

    boolean updateRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            updateRunning = true;
            if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   updating = true;
            else {
                if (updating == false) {
                    lValue = MainActivity.mCs108Library4a.getAntennaSelect();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextPortChannel.setText(String.valueOf(lValue+1));
                    }
                }
                if (updating == false) {
                    lValue = MainActivity.mCs108Library4a.getPwrlevel();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextPortPower.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false) {
                    lValue = MainActivity.mCs108Library4a.getAntennaDwell();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextPortDwell.setText(String.valueOf(lValue));
                    }
                }
            }
            if (updating) {
                mHandler.postDelayed(updateRunnable, 1000);
            } else updateRunning = false;
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        boolean invalidRequest = false;
        boolean changedChannel = false;

        if (invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaSelect() + 1 != channel  || sameCheck == false)) {
            sameSetting = false;
            if (channel < channelMin || channel > channelMax) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setAntennaSelect(channel - 1) == false) invalidRequest = true;
            else changedChannel = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaEnable() != portEnable || sameCheck == false || changedChannel)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setAntennaEnable(portEnable) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getPwrlevel() != powerLevel || sameCheck == false || changedChannel)) {
            sameSetting = false;
            if (powerLevel < powerLevelMin || powerLevel > 330) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setPowerLevel(powerLevel) == false) invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getPwrlevel() != powerLevel || sameCheck == false || changedChannel)) {
           sameSetting = false;
            if (powerLevel < powerLevelMin || powerLevel > 330) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setPowerLevel(powerLevel) == false) invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaDwell() != dwellTime || sameCheck == false || changedChannel)) {
            sameSetting = false;
            if (dwellTime < dwellTimeMin || dwellTime > dwellTimeMax) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setAntennaDwell(dwellTime) == false) invalidRequest = true;
        }
        MainActivity.mCs108Library4a.appendToLog("sameSetting = " + sameSetting);
        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.mCs108Library4a.saveSetting2File();
    }
}
