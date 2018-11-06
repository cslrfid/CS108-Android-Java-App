package com.csl.cs108ademoapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.CustomMediaPlayer;
import com.csl.cs108ademoapp.InventoryRfidTask;
import com.csl.cs108library4a.Cs108Connector;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class InventoryRfidSearchFragment extends CommonFragment {
    final double dBuV_dBm_constant = 106.98;
    final int labelMin = -90;
    final int labelMax = -10;

    private ProgressBar geigerProgress;
    private EditText editTextGeigerTagID;
    private CheckBox checkBoxGeigerTone;
    private SeekBar seekGeiger;
    private EditText editTextGeigerAntennaPower;
    private TextView geigerThresholdView;
    private TextView geigerTagRssiView;
    private TextView geigerTagGotView;
    private TextView geigerRunTime, geigerVoltageLevelView;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;

    private boolean started = false;
    int thresholdValue = 0;

    private InventoryRfidTask geigerSearchTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_geiger_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        android.support.v7.app.ActionBar actionBar;
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_loc);
        actionBar.setTitle(R.string.title_activity_geiger);

        TableRow tableRowProgressLabel;
        TextView textViewProgressLabelMin = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMin);
        TextView textViewProgressLabelMid = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMid);
        TextView textViewProgressLabelMax = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMax);
        textViewProgressLabelMin.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMin : labelMin + dBuV_dBm_constant));
        textViewProgressLabelMid.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMin + (labelMax - labelMin) / 2 : labelMin + (labelMax - labelMin) / 2 + dBuV_dBm_constant));
        textViewProgressLabelMax.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMax : labelMax + dBuV_dBm_constant));

        geigerProgress = (ProgressBar) getActivity().findViewById(R.id.geigerProgress);
        editTextGeigerTagID = (EditText) getActivity().findViewById(R.id.geigerTagID);
        checkBoxGeigerTone = (CheckBox) getActivity().findViewById(R.id.geigerToneCheck);

        ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                editTextGeigerTagID.setText(tagSelected.getAddress());
            }
        }

        seekGeiger = (SeekBar) getActivity().findViewById(R.id.geigerSeek);
        seekGeiger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == seekGeiger && fromUser == true) {
                    thresholdValue = progress;
                    geigerThresholdView.setText(String.format("%.2f", MainActivity.mCs108Library4a.getRssiDisplaySetting() == 0 ? thresholdValue : thresholdValue - dBuV_dBm_constant));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        editTextGeigerAntennaPower = (EditText) getActivity().findViewById(R.id.geigerAntennaPower);
        editTextGeigerAntennaPower.setText(String.valueOf(300));

        geigerThresholdView = (TextView) getActivity().findViewById(R.id.geigerThreshold);
        geigerTagRssiView = (TextView) getActivity().findViewById(R.id.geigerTagRssi);
        geigerTagRssiView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (alertRssiUpdateTime < 0) return;
                double rssi = Double.parseDouble(geigerTagRssiView.getText().toString());
                if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0) rssi += dBuV_dBm_constant;

                double progressPos = geigerProgress.getMax() * ( rssi - labelMin - dBuV_dBm_constant) / (labelMax - labelMin);
                if (progressPos < 0) progressPos = 0;
                if (progressPos > geigerProgress.getMax()) progressPos = geigerProgress.getMax();
                geigerProgress.setProgress((int) (progressPos));

                alertRssiUpdateTime = System.currentTimeMillis(); alertRssi = rssi;
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("afterTextChanged(): alerting = " + alerting + ", alertRssi = " + alertRssi);
                if (rssi > thresholdValue && checkBoxGeigerTone.isChecked()) {
                    if (alerting == false)  {
                        alerting = true;
                        mHandler.removeCallbacks(mAlertRunnable);
                        mHandler.post(mAlertRunnable);
                        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("afterTextChanged(): mAlertRunnable starts");
                    }
                }
            }
        });
        geigerRunTime = (TextView) getActivity().findViewById(R.id.geigerRunTime);
        geigerTagGotView = (TextView) getActivity().findViewById(R.id.geigerTagGot);
        geigerVoltageLevelView = (TextView) getActivity().findViewById(R.id.geigerVoltageLevel);
        rfidYieldView = (TextView) getActivity().findViewById(R.id.geigerYield);
        rfidRateView = (TextView) getActivity().findViewById(R.id.geigerRate);
        button = (Button) getActivity().findViewById(R.id.geigerStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        MainActivity.mCs108Library4a.setSelectedTag(editTextGeigerTagID.getText().toString(), Integer.valueOf(editTextGeigerAntennaPower.getText().toString()));
        playerN = MainActivity.sharedObjects.playerL;
    }

    @Override
    public void onResume() {
        super.onResume();
        setNotificationListener();
    }

    @Override
    public void onPause() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        if (geigerSearchTask != null) {
            geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    public InventoryRfidSearchFragment() {
        super("InventoryRfidSearchFragment");
    }

    double alertRssi; boolean alerting = false; long alertRssiUpdateTime;
    CustomMediaPlayer playerN;
    private final Runnable mAlertRunnable = new Runnable() {
        @Override
        public void run() {
            boolean alerting1 = true;
            final int toneLength = 50;

            mHandler.removeCallbacks(mAlertRunnable);

            if (alertRssi < 20 || alertRssi < thresholdValue || checkBoxGeigerTone.isChecked() == false || alertRssiUpdateTime < 0 || System.currentTimeMillis() - alertRssiUpdateTime > 200) alerting1 = false;
            if (alerting1 == false) {
                playerN.pause(); alerting = false;
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): ENDS with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
            } else if (playerN.isPlaying() == false) {
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): TONE starts");
                mHandler.postDelayed(mAlertRunnable, toneLength);
                playerN.start();
            } else {
                int tonePause = 0;
                if (alertRssi >= 60) tonePause = toneLength;
                else if (alertRssi >= 50) tonePause = 250 - toneLength;
                else if (alertRssi >= 40) tonePause = 500 - toneLength;
                else if (alertRssi >= 30) tonePause = 1000 - toneLength;
                else if (alertRssi >= 20) tonePause = 2000 - toneLength;
                if (tonePause > 0) mHandler.postDelayed(mAlertRunnable, tonePause);
                if (tonePause <= 0 || alertRssi < 60) { playerN.pause(); if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Pause"); }
                if (DEBUG) MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): START with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
                alerting = tonePause > 0 ? true : false;
            }
        }
    };

    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    void startStopHandler(boolean buttonTrigger) {
        boolean started = false;
        if (geigerSearchTask != null) {
            if (geigerSearchTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        }
        if (buttonTrigger == true &&
                ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus())
                        || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false)))   return;
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            startInventoryTask();
            alertRssiUpdateTime = 0;
        } else {
            if (buttonTrigger) geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            alertRssiUpdateTime = -1;
        }
    }

    void startInventoryTask() {
        started = true; boolean invalidRequest = false;
        int powerLevel = Integer.valueOf(editTextGeigerAntennaPower.getText().toString());
        if (powerLevel < 0 || powerLevel > 330) invalidRequest = true;
        else if (MainActivity.mCs108Library4a.setSelectedTag(editTextGeigerTagID.getText().toString(), powerLevel) == false) {
            invalidRequest = true;
        } else {
            MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_SEARCHING);
        }
        geigerSearchTask = new InventoryRfidTask(getContext(), -1,-1, 0, 0, 0, 0, invalidRequest, true,
                null, null, geigerTagRssiView, null,
                geigerRunTime, geigerTagGotView, geigerVoltageLevelView, null, button, rfidRateView);
        geigerSearchTask.execute();
    }
}
