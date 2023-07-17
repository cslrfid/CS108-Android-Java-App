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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingFilterRssiFragment extends CommonFragment {
    private CheckBox checkBoxEnable;
    private Spinner spinnerFilterType, spinnerFilterOption;
    private EditText editTextFilterThreshold1, editTextFilterThreshold2, editTextFilterCount;
    private Button button;

    final boolean sameCheck = false;
    Handler mHandler = new Handler();

    boolean invSelectEnable;
    int invSelectFilterType = -1, invSelectFilterOption = -1;
    double invSelectFilterThreshold1 = -1, invSelectFilterThreshold2 = -1;
    long invSelectFilterCount = -1;

    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_filterrssi_content, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxEnable = (CheckBox) getActivity().findViewById(R.id.filterRssiCheck);

        spinnerFilterType = (Spinner) getActivity().findViewById(R.id.filterRssiFilterType);
        ArrayAdapter<CharSequence> filterTypeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filterType_options, R.layout.custom_spinner_layout);
        filterTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterType.setAdapter(filterTypeAdapter);
        spinnerFilterType.setEnabled(false);

        spinnerFilterOption = (Spinner) getActivity().findViewById(R.id.filterRssiFilterOption);
        ArrayAdapter<CharSequence> filterOptionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filter_options1, R.layout.custom_spinner_layout);
        filterOptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterOption.setAdapter(filterOptionAdapter);

        String strUnit = MainActivity.csLibrary4A.getRssiDisplaySetting() > 0 ? "(dBm)" : "(dBuV)";
        TextView textViewThreshold1 = (TextView) getActivity().findViewById(R.id.filterRssiThreshold1Label);
        textViewThreshold1.setText(textViewThreshold1.getText().toString() + strUnit);
        TextView textViewThreshold2 = (TextView) getActivity().findViewById(R.id.filterRssiThreshold2Label);
        textViewThreshold2.setText(textViewThreshold2.getText().toString() + strUnit);

        editTextFilterThreshold1 = (EditText) getActivity().findViewById(R.id.filterRssiThreshold1);
        editTextFilterThreshold2 = (EditText) getActivity().findViewById(R.id.filterRssiThreshold2);
        editTextFilterCount = (EditText) getActivity().findViewById(R.id.filterRssiCount);

        button = (Button) getActivity().findViewById(R.id.filterRssiSaveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validValue = false;
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    boolean bValid = false;
                    try {
                        invSelectEnable = checkBoxEnable.isChecked();
                        invSelectFilterType = spinnerFilterType.getSelectedItemPosition();
                        invSelectFilterOption = spinnerFilterOption.getSelectedItemPosition();
                        invSelectFilterThreshold1 = Float.parseFloat(editTextFilterThreshold1.getText().toString());
                        if (MainActivity.csLibrary4A.getRssiDisplaySetting() > 0) invSelectFilterThreshold1 += MainActivity.csLibrary4A.dBuV_dBm_constant;
                        invSelectFilterThreshold2 = Float.parseFloat(editTextFilterThreshold2.getText().toString());
                        if (MainActivity.csLibrary4A.getRssiDisplaySetting() > 0) invSelectFilterThreshold2 += MainActivity.csLibrary4A.dBuV_dBm_constant;
                        invSelectFilterCount = Integer.parseInt(editTextFilterCount.getText().toString());
                        bValid = true;
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
                    if (bValid) settingUpdate();
                }
            }
        });

        if (sameCheck == false) MainActivity.csLibrary4A.setSameCheck(false);
        mHandler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        if (settingTask != null) settingTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    public SettingFilterRssiFragment() {
        super("SettingFilterRssiFragment");
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            String updating = null;

            if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0)   updating = "waiting empty buffer";
            else {
                if (updating == null) {
                    boolean bValue = MainActivity.csLibrary4A.getRssiFilterEnable();
                    MainActivity.csLibrary4A.appendToLog("0 updateRunnable getSelectEnable = " + bValue);
                    checkBoxEnable.setChecked(bValue);
                }
                if (updating == null) {
                    int iValue1 = MainActivity.csLibrary4A.getRssiFilterType();
                    MainActivity.csLibrary4A.appendToLog("1 updateRunnable getSelectAction = " + iValue1);
                    if (iValue1 < 0) updating = "getting filter type";
                    else spinnerFilterType.setSelection(iValue1);
                }
                if (updating == null) {
                    int iValue1 = MainActivity.csLibrary4A.getRssiFilterOption();
                    MainActivity.csLibrary4A.appendToLog("2 updateRunnable getSelectAction = " + iValue1);
                    if (iValue1 < 0) updating = "getting filter option";
                    else spinnerFilterOption.setSelection(iValue1);
                }
                if (updating == null) {
                    double dValue = MainActivity.csLibrary4A.getRssiFilterThreshold1();
                    MainActivity.csLibrary4A.appendToLog(String.format("3 updateRunnable getRssiFilterThreshold1 = %f", dValue));
                    if (dValue < 0 && false) updating = "updating threshold 1";
                    else editTextFilterThreshold1.setText(String.format("%.1f", (MainActivity.csLibrary4A.getRssiDisplaySetting() > 0 ? dValue -= MainActivity.csLibrary4A.dBuV_dBm_constant : dValue)));
                }
                if (updating == null) {
                    double dValue = MainActivity.csLibrary4A.getRssiFilterThreshold2();
                    MainActivity.csLibrary4A.appendToLog(String.format("4 updateRunnable getRssiFilterThreshold2 = %f", dValue));
                    if (dValue < 0 && false) updating = "updating threshold 2";
                    else editTextFilterThreshold2.setText(String.format("%.1f", (MainActivity.csLibrary4A.getRssiDisplaySetting() > 0 ? dValue -= MainActivity.csLibrary4A.dBuV_dBm_constant : dValue)));
                }
                if (updating == null) {
                    long lValue1 = MainActivity.csLibrary4A.getRssiFilterCount();
                    MainActivity.csLibrary4A.appendToLog(String.format("5 updateRunnable getRssiFilterCount = %d", lValue1));
                    if (lValue1 < 0) updating = "updating count";
                    else editTextFilterCount.setText(String.valueOf(lValue1));
                }
            }
            if (updating != null) {
                mHandler.postDelayed(updateRunnable, 1000);
                MainActivity.csLibrary4A.appendToLogView("Updating in " + updating);
            }
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        String invalidRequest = null;

        if (sameSetting == true && invalidRequest == null) {
            if (MainActivity.csLibrary4A.getRssiFilterEnable() != invSelectEnable
                    || MainActivity.csLibrary4A.getRssiFilterType() != invSelectFilterType
                    || MainActivity.csLibrary4A.getRssiFilterOption() != invSelectFilterOption) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setRssiFilterConfig(invSelectEnable, invSelectFilterType, invSelectFilterOption) == false) invalidRequest = "setting filter type";
            }
            if (MainActivity.csLibrary4A.getRssiFilterThreshold1() != invSelectFilterThreshold1 || MainActivity.csLibrary4A.getRssiFilterThreshold2() != invSelectFilterThreshold2) {
                sameSetting = false;
                MainActivity.csLibrary4A.appendToLog(String.format("updateRunnable: getRssiFilterThreshold2 = %f, invSelectFilterThreshold2 = %f", MainActivity.csLibrary4A.getRssiFilterThreshold2(), invSelectFilterThreshold2));
                invSelectFilterThreshold2 = 0;
                if (MainActivity.csLibrary4A.setRssiFilterThreshold(invSelectFilterThreshold1, invSelectFilterThreshold2) == false) invalidRequest = "setting filter threshold";
            }
            if (MainActivity.csLibrary4A.getRssiFilterCount() != invSelectFilterCount) {
                sameSetting = false;
                MainActivity.csLibrary4A.appendToLog("rssiFilterCount = " + invSelectFilterCount);
                if (MainActivity.csLibrary4A.setRssiFilterCount(invSelectFilterCount) == false) invalidRequest = "setting filter count";
            }
        }

        if (invalidRequest != null) {
            String strValue = "Invalid " + invalidRequest + ". Operation is cancelled.";
            Toast.makeText(MainActivity.mContext, strValue, Toast.LENGTH_SHORT).show();

        } else {
            settingTask = new SettingTask(button, sameSetting, false);
            settingTask.execute();
            MainActivity.csLibrary4A.saveSetting2File();
        }
    }
}