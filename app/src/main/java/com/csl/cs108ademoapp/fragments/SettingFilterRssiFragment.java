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

        String strUnit = MainActivity.mCs108Library4a.getRssiDisplaySetting() > 0 ? "(dBm)" : "(dBuV)";
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
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    boolean bValid = false;
                    try {
                        invSelectEnable = checkBoxEnable.isChecked();
                        invSelectFilterType = spinnerFilterType.getSelectedItemPosition();
                        invSelectFilterOption = spinnerFilterOption.getSelectedItemPosition();
                        invSelectFilterThreshold1 = Float.parseFloat(editTextFilterThreshold1.getText().toString());
                        if (MainActivity.mCs108Library4a.getRssiDisplaySetting() > 0) invSelectFilterThreshold1 += MainActivity.mCs108Library4a.dBuV_dBm_constant;
                        invSelectFilterThreshold2 = Float.parseFloat(editTextFilterThreshold2.getText().toString());
                        if (MainActivity.mCs108Library4a.getRssiDisplaySetting() > 0) invSelectFilterThreshold2 += MainActivity.mCs108Library4a.dBuV_dBm_constant;
                        invSelectFilterCount = Integer.parseInt(editTextFilterCount.getText().toString());
                        bValid = true;
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
                    if (bValid) settingUpdate();
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

    public SettingFilterRssiFragment() {
        super("SettingFilterRssiFragment");
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            String updating = null;

            if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   updating = "waiting empty buffer";
            else {
                if (updating == null) {
                    boolean bValue = MainActivity.mCs108Library4a.getRssiFilterEnable();
                    MainActivity.mCs108Library4a.appendToLog("updateRunnable getSelectEnable = " + bValue);
                    checkBoxEnable.setChecked(bValue);
                }
                if (updating == null) {
                    int iValue1 = MainActivity.mCs108Library4a.getRssiFilterType();
                    MainActivity.mCs108Library4a.appendToLog("updateRunnable getSelectAction = " + iValue1);
                    if (iValue1 < 0) updating = "getting filter type";
                    else spinnerFilterType.setSelection(iValue1);
                }
                if (updating == null) {
                    int iValue1 = MainActivity.mCs108Library4a.getRssiFilterOption();
                    MainActivity.mCs108Library4a.appendToLog("updateRunnable getSelectAction = " + iValue1);
                    if (iValue1 < 0) updating = "getting filter option";
                    else spinnerFilterOption.setSelection(iValue1);
                }
                if (updating == null) {
                    double dValue = MainActivity.mCs108Library4a.getRssiFilterThreshold1();
                    if (dValue < 0) updating = "updating threshold 1";
                    else editTextFilterThreshold1.setText(String.format("%.1f", (MainActivity.mCs108Library4a.getRssiDisplaySetting() > 0 ? dValue -= MainActivity.mCs108Library4a.dBuV_dBm_constant : dValue)));
                }
                if (updating == null) {
                    double dValue = MainActivity.mCs108Library4a.getRssiFilterThreshold2();
                    if (dValue < 0) updating = "updating threshold 2";
                    else editTextFilterThreshold2.setText(String.format("%.1f", (MainActivity.mCs108Library4a.getRssiDisplaySetting() > 0 ? dValue -= MainActivity.mCs108Library4a.dBuV_dBm_constant : dValue)));
                }
                if (updating == null) {
                    long lValue1 = MainActivity.mCs108Library4a.getRssiFilterCount();
                    if (lValue1 < 0) updating = "updating count";
                    else editTextFilterCount.setText(String.valueOf(lValue1));
                }
            }
            if (updating != null) {
                mHandler.postDelayed(updateRunnable, 1000);
                MainActivity.mCs108Library4a.appendToLog("Updating in " + updating);
            }
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        String invalidRequest = null;

        if (sameSetting == true && invalidRequest == null) {
            if (MainActivity.mCs108Library4a.getRssiFilterEnable() != invSelectEnable
                    || MainActivity.mCs108Library4a.getRssiFilterType() != invSelectFilterType
                    || MainActivity.mCs108Library4a.getRssiFilterOption() != invSelectFilterOption) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setRssiFilterConfig(invSelectEnable, invSelectFilterType, invSelectFilterOption) == false) invalidRequest = "setting filter type";
            }
            if (MainActivity.mCs108Library4a.getRssiFilterThreshold1() != invSelectFilterThreshold1 || MainActivity.mCs108Library4a.getRssiFilterThreshold2() != invSelectFilterThreshold2) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setRssiFilterThreshold(invSelectFilterThreshold1, invSelectFilterThreshold2) == false) invalidRequest = "setting filter threshold";
            }
            if (MainActivity.mCs108Library4a.getRssiFilterCount() != invSelectFilterCount) {
                sameSetting = false;
                MainActivity.mCs108Library4a.appendToLog("rssiFilterCount = " + invSelectFilterCount);
                if (MainActivity.mCs108Library4a.setRssiFilterCount(invSelectFilterCount) == false) invalidRequest = "setting filter count";
            }
        }

        if (invalidRequest != null) {
            String strValue = "Invalid " + invalidRequest + ". Operation is cancelled.";
            Toast.makeText(MainActivity.mContext, strValue, Toast.LENGTH_SHORT).show();

        } else {
            settingTask = new SettingTask(button, sameSetting, false);
            settingTask.execute();
            MainActivity.mCs108Library4a.saveSetting2File();
        }
    }
}