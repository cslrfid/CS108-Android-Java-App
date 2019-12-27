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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingOperateFragment extends CommonFragment {
    final String strOVERRIDE = "Override"; final String strRESET = "Reset";
    private CheckBox checkBoxPortEnable;
    private Spinner spinnerRegulatoryRegion, spinnerFrequencyOrder, spinnerQueryTarget, spinnerQuerySession, spinnerInvAlgo, spinnerProfile;
    private EditText editTextChannel, editTextPopulation, editTextStartQValue, editTextOperatePower, editTextPortDwell;
    private TextView textViewPortChannel;
    private Button buttonPortSelect, buttonOverride, button;

    final boolean sameCheck = true;
    Handler mHandler = new Handler();

    boolean overriding = false;
    int countrySelect = -1;
    int channelOrder = -1; int channelSelect = -1;
    int channel = MainActivity.mCs108Library4a.getAntennaSelect() + 1; final int channelMin = 1; int channelMax = 1; int iPortNumber = 1;
    boolean portEnable = MainActivity.mCs108Library4a.getAntennaEnable();
    long powerLevel = -1; final long powerLevelMin = 0; final long powerLevelMax = 300;
    long dwellTime = MainActivity.mCs108Library4a.getAntennaDwell(); final long dwellTimeMin = 0; final long dwellTimeMax = 10000;
    int iPopulation = -1; int iPopulationMin = 1; int iPopulationMax = 9999;
    byte byteFixedQValue = -1; byte byteFixedQValueMin = 0; byte byteFixedQValueMax = 15;
    int queryTarget;
    int querySession = -1;
    boolean invAlgoDynamic = false;
    int profile = -1;

    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_settings_operate, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        spinnerRegulatoryRegion = (Spinner) getActivity().findViewById(R.id.settingOperateRegulatoryRegion);
        spinnerFrequencyOrder = (Spinner) getActivity().findViewById(R.id.settingOperateFrequencyOrder); spinnerFrequencyOrder.setEnabled(false);
        editTextChannel = (EditText) getActivity().findViewById(R.id.settingOperateChannel);

        iPortNumber = MainActivity.mCs108Library4a.getPortNumber();
        if (iPortNumber == 1 && false) {
            TableRow tableRow = (TableRow) getActivity().findViewById(R.id.settingOperatePortChannelRow);
            tableRow.setVisibility(View.GONE);
            tableRow = (TableRow) getActivity().findViewById(R.id.settingOperatePortEnableRow);
            tableRow.setVisibility(View.GONE);
            tableRow = (TableRow) getActivity().findViewById(R.id.settingOperatePortDwellRow);
            tableRow.setVisibility(View.GONE);
        } else {
            int iTemp = iPortNumber;
            if (iTemp > 1) channelMax = iTemp;
            else channelMax = 16;
            if (channelMax != 1) {
                TextView textViewPortChannelLabel = (TextView) getActivity().findViewById(R.id.settingOperatePortChannelLabel);
                if (iPortNumber != 1) textViewPortChannelLabel.setText("Ant port #");
                else textViewPortChannelLabel.setText("Power level");
                String stringPortChannelLabel = textViewPortChannelLabel.getText().toString();
                stringPortChannelLabel += "(" + String.valueOf(channelMin) + "-" + String.valueOf(channelMax) + ")";
                textViewPortChannelLabel.setText(stringPortChannelLabel);
            }
            textViewPortChannel = (TextView) getActivity().findViewById(R.id.settingOperatePortChannel); textViewPortChannel.setText("1");
            buttonPortSelect = (Button) getActivity().findViewById(R.id.settingOperatePortChannelSelect);
            buttonPortSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int channel = Integer.parseInt(textViewPortChannel.getText().toString());
                    if (channel < 1) channel = channelMin;
                    if (++channel > channelMax) channel = channelMin;
                    textViewPortChannel.setText(""); if (MainActivity.mCs108Library4a.setAntennaSelect(channel-1))    textViewPortChannel.setText(String.valueOf(channel));
                    editTextOperatePower.setText("");
                    editTextPortDwell.setText("");
                    mHandler.post(updateRunnable);
                }
            });

            checkBoxPortEnable = (CheckBox) getActivity().findViewById(R.id.settingOperatePortEnable);

            TextView textViewPortDwellLabel = (TextView) getActivity().findViewById(R.id.settingOperatePortDwellLabel);
            String stringPortDwellLabel = textViewPortDwellLabel.getText().toString();
            stringPortDwellLabel += "(" + String.valueOf(dwellTimeMin) + "-" + String.valueOf(dwellTimeMax) + ")";
            textViewPortDwellLabel.setText(stringPortDwellLabel);
            editTextPortDwell = (EditText) getActivity().findViewById(R.id.settingOperatePortDwell);
        }
        TextView textViewOperatePowerLabel = (TextView) getActivity().findViewById(R.id.settingOperatePowerLabel);
        String stringOperationPowerLabel = textViewOperatePowerLabel.getText().toString();
        stringOperationPowerLabel += "(" + String.valueOf(powerLevelMin) + "-" + String.valueOf(powerLevelMax) + ")";
        textViewOperatePowerLabel.setText(stringOperationPowerLabel);
        editTextOperatePower = (EditText) getActivity().findViewById(R.id.settingOperatePower);

        TextView textViewOperatePopulationLabel = (TextView) getActivity().findViewById(R.id.settingOperatePopulationLabel);
        String stringOperationPopulationLabel = textViewOperatePopulationLabel.getText().toString();
        stringOperationPopulationLabel += "(" + String.valueOf(iPopulationMin) + "-" + String.valueOf(iPopulationMax) + ")";
        textViewOperatePopulationLabel.setText(stringOperationPopulationLabel);
        editTextPopulation = (EditText) getActivity().findViewById(R.id.settingOperatePopulation);

        editTextStartQValue = (EditText) getActivity().findViewById(R.id.settingOperateQValue);

        spinnerQueryTarget = (Spinner) getActivity().findViewById(R.id.settingOperateTarget);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.query_target_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQueryTarget.setAdapter(targetAdapter);

        spinnerQuerySession = (Spinner) getActivity().findViewById(R.id.settingOperateSession);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.query_session_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuerySession.setAdapter(targetAdapter);

        spinnerInvAlgo = (Spinner) getActivity().findViewById(R.id.settingOperateAlgorithmToUse);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventory_algorithm_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInvAlgo.setAdapter(targetAdapter);

        spinnerProfile = (Spinner) getActivity().findViewById(R.id.settingOperateProfile);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.profile1_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProfile.setAdapter(targetAdapter);

        buttonOverride = (Button) getActivity().findViewById(R.id.settingOperateOverrideButton);
        String strText = buttonOverride.getText().toString();
        if (strText.contains(strOVERRIDE)) {
            editTextPopulation.setEnabled(true);
            editTextStartQValue.setEnabled(false);
        } else {
            editTextPopulation.setEnabled(false);
            editTextStartQValue.setEnabled(true);
        }
        buttonOverride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strText = buttonOverride.getText().toString();
                if (strText.contains(strOVERRIDE)) {
                    editTextPopulation.setEnabled(false);
                    editTextStartQValue.setEnabled(true);
                    buttonOverride.setText(strRESET); overriding = true;
                } else {
                    editTextPopulation.setEnabled(true);
                    editTextStartQValue.setEnabled(false);;
                    buttonOverride.setText(strOVERRIDE); overriding = false;

                    iPopulation = Integer.parseInt(editTextPopulation.getText().toString());
                    editTextStartQValue.setText(String.valueOf(MainActivity.mCs108Library4a.getPopulation2Q(iPopulation)));
                }
            }
        });

        button = (Button) getActivity().findViewById(R.id.settingSaveButtonOperate);
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
                        countrySelect = spinnerRegulatoryRegion.getSelectedItemPosition();
                        channelOrder = spinnerFrequencyOrder.getSelectedItemPosition();
                        channelSelect = Integer.parseInt(editTextChannel.getText().toString());
                        if (textViewPortChannel != null) channel = Integer.parseInt(textViewPortChannel.getText().toString());
                        if (checkBoxPortEnable != null) portEnable = checkBoxPortEnable.isChecked();
                        powerLevel = Long.parseLong(editTextOperatePower.getText().toString());
                        if (editTextPortDwell != null) dwellTime = Long.parseLong(editTextPortDwell.getText().toString());
                        iPopulation = Integer.parseInt(editTextPopulation.getText().toString());
                        byteFixedQValue = Byte.parseByte(editTextStartQValue.getText().toString());
                        queryTarget = spinnerQueryTarget.getSelectedItemPosition();
                        querySession = spinnerQuerySession.getSelectedItemPosition();
                        invAlgoDynamic = (spinnerInvAlgo.getSelectedItemPosition() == 0 ? true : false) ;
                        profile = spinnerProfile.getSelectedItemPosition();

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

    public SettingOperateFragment() {
        super("SettingOperateFragment");
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
                iPopulation = MainActivity.mCs108Library4a.getPopulation();
                if (iPopulation < 0)    updating = true;
                else {
                    editTextPopulation.setText(String.valueOf(iPopulation));

                    byteFixedQValue = MainActivity.mCs108Library4a.getQValue();
                    editTextStartQValue.setText(String.valueOf(byteFixedQValue));
                    if (MainActivity.mCs108Library4a.getPopulation2Q(iPopulation) != byteFixedQValue) {
                        buttonOverride.setText(strRESET); overriding = true;
                    } else {
                        buttonOverride.setText(strOVERRIDE); overriding = false;
                    }
                }
                if (updating == false) {
                    int channel = MainActivity.mCs108Library4a.getChannel();
                    if (channel < 0) updating = true;
                    else { editTextChannel.setText(String.valueOf(channel)); }
                }
                if (updating == false && textViewPortChannel != null) {
                    lValue = MainActivity.mCs108Library4a.getAntennaSelect();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        textViewPortChannel.setText(String.valueOf(lValue+1));
                    }
                }
                if (checkBoxPortEnable != null) checkBoxPortEnable.setChecked(MainActivity.mCs108Library4a.getAntennaEnable());
                if (updating == false) {
                    lValue = MainActivity.mCs108Library4a.getPwrlevel();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextOperatePower.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false && editTextPortDwell != null) {
                    lValue = MainActivity.mCs108Library4a.getAntennaDwell();
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextPortDwell.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false) {
                    MainActivity.mCs108Library4a.appendToLog("Hello3: checkpoint 1");
                    spinnerQueryTarget.setSelection(MainActivity.mCs108Library4a.getQueryTarget());
                }
                if (updating == false) {
                    iValue = MainActivity.mCs108Library4a.getQuerySession();
                    if (iValue < 0) {
                        updating = true;
                    } else {
                        spinnerQuerySession.setSelection(iValue);
                    }
                }
                if (updating == false) {
                    spinnerInvAlgo.setSelection(MainActivity.mCs108Library4a.getInvAlgo() ? 0 : 1);
                }
                if (updating == false) {
                    String[] strCountryList = MainActivity.mCs108Library4a.getCountryList();
                    if (strCountryList == null) updating = true;
                    else {
                        ArrayAdapter targetAdapter1 = new ArrayAdapter(getActivity(), R.layout.custom_spinner_layout, strCountryList);
                        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRegulatoryRegion.setAdapter(targetAdapter1);
                        int countryNumber = MainActivity.mCs108Library4a.getCountryNumberInList();
                        if (countryNumber < 0 || countryNumber > strCountryList.length) spinnerRegulatoryRegion.setSelection(0);
                        else spinnerRegulatoryRegion.setSelection(countryNumber);
                        if (strCountryList.length == 1) spinnerRegulatoryRegion.setEnabled(false);
                        else spinnerRegulatoryRegion.setEnabled(true);

                        ArrayAdapter<CharSequence> targetAdapter;
                        if (MainActivity.mCs108Library4a.getChannelHoppingDefault())
                            targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequencyOrder_options, R.layout.custom_spinner_layout);
                        else targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequencyAgile_options, R.layout.custom_spinner_layout);
                        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerFrequencyOrder.setAdapter(targetAdapter);
                        spinnerFrequencyOrder.setSelection(MainActivity.mCs108Library4a.getChannelHoppingStatus() ? 0 : 1);
                        if (MainActivity.mCs108Library4a.getChannelHoppingStatus()) editTextChannel.setEnabled(false);
                        else editTextChannel.setEnabled(true);
                    }
                }
            }
            if (updating == false) {
                iValue = MainActivity.mCs108Library4a.getCurrentProfile();
                if (iValue < 0) {
                    updating = true;
                } else {
                    spinnerProfile.setSelection(iValue);
                }
            }
            if (updating) {
                mHandler.postDelayed(updateRunnable, 500);
            } else updateRunning = false;
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        boolean invalidRequest = false;
        boolean changedChannel = false;

        if (invalidRequest == false && (MainActivity.mCs108Library4a.getCountryNumberInList() != countrySelect || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setCountryInList(countrySelect) == false)    invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getChannelHoppingStatus() != (channelOrder == 0 ? true : false) || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setChannelHoppingStatus(channelOrder == 0 ? true : false) == false)    invalidRequest = true;
            else if (channelOrder > 0) editTextChannel.setEnabled(true);
            else editTextChannel.setEnabled(false);
            editTextChannel.setText(String.valueOf(MainActivity.mCs108Library4a.getChannel())); MainActivity.mCs108Library4a.appendToLog("1 channel = ");
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getChannel() != channelSelect || sameCheck == false)) {
            sameSetting = false;
            if (channelSelect < MainActivity.mCs108Library4a.FreqChnCnt()) {
                if (MainActivity.mCs108Library4a.setChannel(channelSelect) == false)    invalidRequest = true;
            } else {
                invalidRequest = true;
            }
        }
        if (false && invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaSelect() + 1 != channel  || sameCheck == false)) {
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
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getPwrlevel() != powerLevel || sameCheck == false)) {
            sameSetting = false;
            if (powerLevel < powerLevelMin || powerLevel > 330) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setPowerLevel(powerLevel) == false) invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaDwell() != dwellTime || sameCheck == false || changedChannel)) {
            sameSetting = false;
            if (dwellTime < dwellTimeMin || dwellTime > dwellTimeMax) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setAntennaDwell(dwellTime) == false) invalidRequest = true;
        }

        if (overriding) {
            if (MainActivity.mCs108Library4a.getQValue() != byteFixedQValue || sameCheck == false) {
                sameSetting = false;
                if (byteFixedQValue < byteFixedQValueMin || byteFixedQValue > byteFixedQValueMax) invalidRequest = true;
                if (MainActivity.mCs108Library4a.setQValue(byteFixedQValue) == false)
                    invalidRequest = true;
            }
        } else {
            if (invalidRequest == false && (MainActivity.mCs108Library4a.getPopulation() != iPopulation || MainActivity.mCs108Library4a.getQValue() != byteFixedQValue || sameCheck == false)) {
                sameSetting = false;
                if (iPopulation < iPopulationMin || iPopulation > iPopulationMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setPopulation(iPopulation) == false) {
                    invalidRequest = true;
                } else {
                    editTextStartQValue.setText(String.valueOf(MainActivity.mCs108Library4a.getPopulation2Q(iPopulation)));
                }
            }
        }
        if (MainActivity.mCs108Library4a.getQueryTarget() != queryTarget
                || MainActivity.mCs108Library4a.getQuerySession() != querySession || sameCheck == false) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setTagGroup(MainActivity.mCs108Library4a.getQuerySelect(), querySession, queryTarget) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.getInvAlgo() != invAlgoDynamic || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setInvAlgo(invAlgoDynamic) == false)
                    invalidRequest = true;
                MainActivity.mCs108Library4a.appendToLog("Hello3: checkpoint 2");
                spinnerQueryTarget.setSelection(MainActivity.mCs108Library4a.getQueryTarget());
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.getCurrentProfile() != profile || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setCurrentLinkProfile(profile) == false)
                    invalidRequest = true;
            }
        }
        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.mCs108Library4a.saveSetting2File();
    }
}
