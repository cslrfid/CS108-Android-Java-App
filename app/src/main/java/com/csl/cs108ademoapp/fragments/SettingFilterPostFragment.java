package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingFilterPostFragment extends CommonFragment {
    private CheckBox checkBoxEnable;
    private CheckBox checkBoxType;
    private EditText postFilterOffset;
    private EditText filterPostMaskData;
    private Button button;

    final boolean sameCheck = false;
    Handler mHandler = new Handler();

    boolean invMatchEnable;
    boolean invMatchType;
    int invMatchOffset = -1;
    String invMatchData = null;

    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_filterpost_content, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxEnable = (CheckBox) getActivity().findViewById(R.id.filterPostCheckEnable);
        checkBoxType = (CheckBox) getActivity().findViewById(R.id.filterPostCheckType);
        postFilterOffset = (EditText) getActivity().findViewById(R.id.filterPostOffset);
        filterPostMaskData = (EditText) getActivity().findViewById(R.id.filterPostMaskData);

        button = (Button) getActivity().findViewById(R.id.filterPostSaveButton);
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
                } else {
                    try {
                        invMatchEnable = checkBoxEnable.isChecked();
                        invMatchType = checkBoxType.isChecked();
                        invMatchOffset = Integer.parseInt(postFilterOffset.getText().toString());

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

    public SettingFilterPostFragment() {
        super("SettingFilterPostFragment");
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   updating = true;
            else {
                if (updating == false) {
                    checkBoxEnable.setChecked(MainActivity.mCs108Library4a.getInvMatchEnable());
                    checkBoxType.setChecked(MainActivity.mCs108Library4a.getInvMatchType());
                }
                if (updating == false) {
                    int iValue1 = MainActivity.mCs108Library4a.getInvMatchOffset();
                    if (iValue1 < 0) {
                        updating = true;
                    } else {
                        postFilterOffset.setText(String.valueOf(iValue1));
                    }
                }
                if (updating == false && filterPostMaskData.getText().length() == 0) {
                    String strValue = MainActivity.mCs108Library4a.getInvMatchData();
                    if (strValue == null) {
                        updating = true;
                    } else {
                        filterPostMaskData.setText(strValue);
                    }
                }
            }
            if (updating) {
                mHandler.postDelayed(updateRunnable, 1000);
            }
        }
    };

    void settingUpdate() {
        boolean dataMatched = false;
        invMatchData = filterPostMaskData.getText().toString();
        String strValue = MainActivity.mCs108Library4a.getInvMatchData();
        boolean sameSetting = true;
        boolean invalidRequest = false;

        if (invMatchData.length() != strValue.length()) {
            dataMatched = false;
        } else if (invMatchData.length() == 0 && strValue.length() == 0) {
            dataMatched = true;
        } else dataMatched = invMatchData.matches(strValue);
        if (MainActivity.mCs108Library4a.getInvMatchEnable() != invMatchEnable
                || MainActivity.mCs108Library4a.getInvMatchType() != invMatchType
                || MainActivity.mCs108Library4a.getInvMatchOffset() != invMatchOffset
                || dataMatched == false || sameCheck == false) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setPostMatchCriteria(invMatchEnable, invMatchType, invMatchOffset, invMatchData) == false)
                invalidRequest = true;
        }
        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
    }
}
