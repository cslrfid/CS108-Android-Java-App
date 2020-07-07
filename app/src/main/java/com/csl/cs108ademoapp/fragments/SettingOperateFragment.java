package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private CheckBox checkBoxPortEnable, checkBoxTagFocus, checkBoxHighCompression;
    private Spinner spinnerRegulatoryRegion, spinnerFrequencyOrder, spinnerQueryTarget, spinnerQuerySession, spinnerInvAlgo, spinnerProfile, spinnerRflnaGain, spinnerIflnaGain, spinnerAgcGain;
    private EditText editTextChannel, editTextPopulation, editTextStartQValue, editTextOperatePower, editTextPortDwell, editTextTagDelay, editTextRetry;
    private TextView textViewPortChannel;
    private Button buttonPortSelect, buttonOverride, button, button1;
    private TextView textViewEnvironmentalRSSI;

    boolean sameCheck = true;
    Handler mHandler = new Handler();
    class SettingBeforeTagFocus {
        int querySession;
        int queryTarget;
        String dwell;
        String tagDelay;
        void store() {
            querySession = spinnerQuerySession.getSelectedItemPosition(); spinnerQuerySession.setSelection(1); spinnerQuerySession.setEnabled(false);
            queryTarget = spinnerQueryTarget.getSelectedItemPosition(); spinnerQueryTarget.setSelection(0); spinnerQueryTarget.setEnabled(false);
            dwell = editTextPortDwell.getText().toString(); editTextPortDwell.setText("2000"); editTextPortDwell.setEnabled(false);
            tagDelay = editTextTagDelay.getText().toString(); editTextTagDelay.setText("0"); editTextTagDelay.setEnabled(false);
        }
        void restore() {
            spinnerQuerySession.setSelection(querySession); spinnerQuerySession.setEnabled(true);
            spinnerQueryTarget.setSelection(queryTarget); spinnerQueryTarget.setEnabled(true);
            editTextPortDwell.setText(dwell); editTextPortDwell.setEnabled(true);
            editTextTagDelay.setText(tagDelay); editTextTagDelay.setEnabled(true);
        }
    };
    SettingBeforeTagFocus settingBeforeTagFocus = new SettingBeforeTagFocus();

    boolean overriding = false;
    int countrySelect = -1;
    int channelOrder = -1; int channelSelect = -1;
    int channel = MainActivity.mCs108Library4a.getAntennaSelect() + 1; final int channelMin = 1; int channelMax = 1; int iPortNumber = 1;
    boolean portEnable = MainActivity.mCs108Library4a.getAntennaEnable();
    long powerLevel = -1; final long powerLevelMin = 0; final long powerLevelMax = 300;
    long dwellTime = MainActivity.mCs108Library4a.getAntennaDwell(); final long dwellTimeMin = 0; final long dwellTimeMax = 10000;
    byte byteTagDelay = -1; byte byteTagDelayMin = 0; byte byteTagDelayMax = 63;
    int iPopulation = -1; int iPopulationMin = 1; int iPopulationMax = 9999;
    byte byteFixedQValue = -1; byte byteFixedQValueMin = 0; byte byteFixedQValueMax = 15;
    int queryTarget;
    int querySession = -1;
    int tagFocus = -1;
    boolean invAlgoDynamic = false;
    int retry = -1;
    int profile = -1;
    int highCompression = -1, rflnagain = -1, iflnagain = -1, agcgain = -1;

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

        TextView textViewAdminTagDelayLabel = (TextView) getActivity().findViewById(R.id.settingAdminTagDelayLabel);
        String stringAdminTagDelayLabel = textViewAdminTagDelayLabel.getText().toString();
        stringAdminTagDelayLabel += "(" + String.valueOf(byteTagDelayMin) + "-" + String.valueOf(byteTagDelayMax) + "ms)";
        textViewAdminTagDelayLabel.setText(stringAdminTagDelayLabel);
        editTextTagDelay = (EditText) getActivity().findViewById(R.id.settingOperateTagDelay);

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

        checkBoxTagFocus = (CheckBox) getActivity().findViewById(R.id.settingOperateTagFocus);
        checkBoxTagFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) settingBeforeTagFocus.store();
                else settingBeforeTagFocus.restore();
            }
        });

        spinnerInvAlgo = (Spinner) getActivity().findViewById(R.id.settingOperateAlgorithmToUse);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventory_algorithm_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInvAlgo.setAdapter(targetAdapter);

        editTextRetry = (EditText) getActivity().findViewById(R.id.settingOperateRetry);

        spinnerProfile = (Spinner) getActivity().findViewById(R.id.settingOperateProfile);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.profile1_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProfile.setAdapter(targetAdapter);

        MainActivity.mCs108Library4a.resetEnvironmentalRSSI();
        textViewEnvironmentalRSSI = (TextView) getActivity().findViewById(R.id.settingOperateEnvironmentalRSSI);
        checkBoxHighCompression = (CheckBox) getActivity().findViewById(R.id.settingOperateHighCompression);

        spinnerRflnaGain = (Spinner) getActivity().findViewById(R.id.settingOperateRflnaGain);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.rflnagain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRflnaGain.setAdapter(targetAdapter);

        spinnerIflnaGain = (Spinner) getActivity().findViewById(R.id.settingOperateIflnaGain);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.iflnagain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIflnaGain.setAdapter(targetAdapter);

        spinnerAgcGain = (Spinner) getActivity().findViewById(R.id.settingOperateAgcGAin);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.agcgain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgcGain.setAdapter(targetAdapter);

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
                sameCheck = true; settingUpdate1();
            }
        });

        button1 = (Button) getActivity().findViewById(R.id.settingSaveButtonOperate1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sameCheck = false; settingUpdate1();
            }
        });

        mHandler.post(updateRunnable);
    }

    void settingUpdate1() {
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
            MainActivity.mCs108Library4a.setSameCheck(sameCheck);
            try {
                countrySelect = spinnerRegulatoryRegion.getSelectedItemPosition();
                channelOrder = spinnerFrequencyOrder.getSelectedItemPosition();
                channelSelect = Integer.parseInt(editTextChannel.getText().toString());
                if (textViewPortChannel != null)
                    channel = Integer.parseInt(textViewPortChannel.getText().toString());
                if (checkBoxPortEnable != null) portEnable = checkBoxPortEnable.isChecked();
                powerLevel = Long.parseLong(editTextOperatePower.getText().toString());
                if (editTextPortDwell != null)
                    dwellTime = Long.parseLong(editTextPortDwell.getText().toString());
                if (editTextTagDelay != null)
                    byteTagDelay = Byte.parseByte(editTextTagDelay.getText().toString());
                iPopulation = Integer.parseInt(editTextPopulation.getText().toString());
                byteFixedQValue = Byte.parseByte(editTextStartQValue.getText().toString());
                queryTarget = spinnerQueryTarget.getSelectedItemPosition();
                querySession = spinnerQuerySession.getSelectedItemPosition();
                tagFocus = (checkBoxTagFocus.isChecked() ? 1 : 0);
                invAlgoDynamic = (spinnerInvAlgo.getSelectedItemPosition() == 0 ? true : false);
                retry = Integer.parseInt(editTextRetry.getText().toString());
                profile = spinnerProfile.getSelectedItemPosition();
                highCompression = (checkBoxHighCompression.isChecked() ? 1 : 0);
                rflnagain = spinnerRflnaGain.getSelectedItemPosition();
                iflnagain = spinnerIflnaGain.getSelectedItemPosition();
                agcgain = spinnerAgcGain.getSelectedItemPosition();
                settingUpdate();
            } catch (Exception ex) {
                Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
            }
        }
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
                if (editTextTagDelay != null)   editTextTagDelay.setText(String.valueOf(MainActivity.mCs108Library4a.getTagDelay()));
                if (updating == false) {
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
                    iValue = MainActivity.mCs108Library4a.getTagFocus();
                    if (iValue < 0) updating = true;
                    else {
                        checkBoxTagFocus.setChecked(iValue > 0 ? true : false);
                        if (checkBoxTagFocus.isChecked()) settingBeforeTagFocus.store();
                    }
                }
                if (updating == false) {
                    spinnerInvAlgo.setSelection(MainActivity.mCs108Library4a.getInvAlgo() ? 0 : 1);
                }
                if (updating == false) {
                    int iRetry = MainActivity.mCs108Library4a.getRetryCount();
                    if (iRetry < 0) updating = true;
                    else editTextRetry.setText(String.valueOf(iRetry));
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
            if (updating == false) {
                String strRssi = MainActivity.mCs108Library4a.getEnvironmentalRSSI();
                if (strRssi == null) updating = true;
                else textViewEnvironmentalRSSI.setText(strRssi);
            }
            if (updating == false) {
                iValue = MainActivity.mCs108Library4a.getHighCompression();
                if (iValue < 0) {
                    updating = true;
                } else checkBoxHighCompression.setChecked(iValue == 0 ? false : true);
            }
            if (updating == false) {
                iValue = MainActivity.mCs108Library4a.getRflnaGain();
                if (iValue < 0) {
                    updating = true;
                } else {
                    switch (iValue) {
                        case 2:
                            iValue = 1;
                            break;
                        case 3:
                            iValue = 2;
                            break;
                        case 0:
                        default:
                            iValue = 0;
                            break;
                    }
                    spinnerRflnaGain.setSelection(iValue);
                }
            }
            if (updating == false) {
                iValue = MainActivity.mCs108Library4a.getIflnaGain();
                if (iValue < 0) {
                    updating = true;
                } else {
                    switch (iValue) {
                        case 1:
                            iValue = 1;
                            break;
                        case 3:
                            iValue = 2;
                            break;
                        case 7:
                            iValue = 3;
                            break;
                        case 0:
                        default:
                            iValue = 0;
                            break;
                    }
                    spinnerIflnaGain.setSelection(iValue);
                }
            }
            if (updating == false) {
                iValue = MainActivity.mCs108Library4a.getAgcGain();
                if (iValue < 0) {
                    updating = true;
                } else {
                    switch (iValue) {
                        case 4:
                            iValue = 1;
                            break;
                        case 6:
                            iValue = 2;
                            break;
                        case 7:
                            iValue = 3;
                            break;
                        case 0:
                        default:
                            iValue = 0;
                            break;

                    }
                    spinnerAgcGain.setSelection(iValue);
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
        if ((invalidRequest == false && (MainActivity.mCs108Library4a.getAntennaDwell() != dwellTime || sameCheck == false || changedChannel))) {
            sameSetting = false;
            if (dwellTime < dwellTimeMin || dwellTime > dwellTimeMax) invalidRequest = true;
            else if (MainActivity.mCs108Library4a.setAntennaDwell(dwellTime) == false) invalidRequest = true;
        }
        if ((invalidRequest == false && editTextTagDelay != null)) {
            if (MainActivity.mCs108Library4a.getTagDelay() != byteTagDelay || sameCheck == false) {
                sameSetting = false;
                if (byteTagDelay < byteTagDelayMin || byteTagDelay > byteTagDelayMax) invalidRequest = true;
                else if (MainActivity.mCs108Library4a.setTagDelay(byteTagDelay) == false)
                    invalidRequest = true;
            }
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
        if ((MainActivity.mCs108Library4a.getQueryTarget() != queryTarget
                || MainActivity.mCs108Library4a.getQuerySession() != querySession || sameCheck == false)) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setTagGroup(MainActivity.mCs108Library4a.getQuerySelect(), querySession, queryTarget) == false)
                invalidRequest = true;
        }
        if (MainActivity.mCs108Library4a.getTagFocus() != tagFocus || sameCheck == false) {
            sameSetting = false;
            if (MainActivity.mCs108Library4a.setTagFocus(tagFocus > 0 ? true : false) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.getInvAlgo() != invAlgoDynamic || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setInvAlgo(invAlgoDynamic) == false)
                    invalidRequest = true;
                spinnerQueryTarget.setSelection(MainActivity.mCs108Library4a.getQueryTarget());
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.getRetryCount() != retry || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setRetryCount(retry) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.getCurrentProfile() != profile || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setCurrentLinkProfile(profile) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            switch(rflnagain) {
                case 1:
                    rflnagain = 2;
                    break;
                case 2:
                    rflnagain = 3;
                    break;
                case 0:
                default:
                    rflnagain = 0;
                    break;
            }
            switch(iflnagain) {
                case 1:
                    iflnagain = 1;
                    break;
                case 2:
                    iflnagain = 3;
                    break;
                case 3:
                    iflnagain = 7;
                    break;
                case 0:
                default:
                    iflnagain = 0;
                    break;
            }
            switch(agcgain) {
                case 1:
                    agcgain = 4;
                    break;
                case 2:
                    agcgain = 6;
                    break;
                case 3:
                    agcgain = 7;
                    break;
                case 0:
                default:
                    agcgain = 0;
                    break;
            }
            if ((MainActivity.mCs108Library4a.getHighCompression() != highCompression)
                    || (MainActivity.mCs108Library4a.getRflnaGain() != rflnagain)
                    || (MainActivity.mCs108Library4a.getIflnaGain() != iflnagain)
                    || (MainActivity.mCs108Library4a.getAgcGain() != agcgain)
                    || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.mCs108Library4a.setRxGain(highCompression, rflnagain, iflnagain, agcgain) == false)
                    invalidRequest = true;
            }
        }
        settingTask = new SettingTask((sameCheck ? button: button1), sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.mCs108Library4a.saveSetting2File();
    }
}
