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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.csl.cslibrary4a.CustomPopupWindow;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTaskCustom;

public class SettingOperateFragment extends CommonFragment {
    final String strOVERRIDE = "Override"; final String strRESET = "Reset";
    private CheckBox checkBoxPortEnable, checkBoxPowerBoost, checkBoxTagFocus, checkBoxFastId, checkBoxHighCompression;
    private Spinner spinnerRegulatoryRegion, spinnerFrequencyOrder, spinnerChannel, spinnerQueryTarget, spinnerQuerySession, spinnerInvAlgo, spinnerProfile, spinnerRflnaGain, spinnerIflnaGain, spinnerAgcGain;
    private EditText editTextPopulation, editTextStartQValue, editTextOperatePower, editTextPortDwell, editTextTagDelay, editTextIntraPkDelay, editTextDupDelay, editTextRetry;
    private TextView textViewPortChannel;
    private Button buttonPortSelect, buttonOverride, button, button1;
    private TextView textViewEnvironmentalRSSI;

    boolean sameCheck = true;
    Handler mHandler = new Handler();
    class SettingBeforeTagFocus {
        int querySession;
        int queryTarget;
        String dwell;
        String tagDelay;//, intraPkDelay, dupDelay;
        void store() {
            querySession = spinnerQuerySession.getSelectedItemPosition(); spinnerQuerySession.setSelection(1); spinnerQuerySession.setEnabled(false);
            queryTarget = spinnerQueryTarget.getSelectedItemPosition(); spinnerQueryTarget.setSelection(0); spinnerQueryTarget.setEnabled(false);
            dwell = editTextPortDwell.getText().toString(); editTextPortDwell.setText("2000"); editTextPortDwell.setEnabled(false);
            tagDelay = editTextTagDelay.getText().toString(); editTextTagDelay.setText("0"); editTextTagDelay.setEnabled(false);
            //intraPkDelay = editTextIntraPkDelay.getText().toString(); editTextIntraPkDelay.setText("0"); editTextIntraPkDelay.setEnabled(false);
            //dupDelay = editTextDupDelay.getText().toString(); editTextDupDelay.setText("0"); editTextDupDelay.setEnabled(false);
        }
        void restore() {
            spinnerQuerySession.setSelection(querySession); spinnerQuerySession.setEnabled(true);
            spinnerQueryTarget.setSelection(queryTarget); spinnerQueryTarget.setEnabled(true);
            editTextPortDwell.setText(dwell); editTextPortDwell.setEnabled(true);
            editTextTagDelay.setText(tagDelay); editTextTagDelay.setEnabled(true);
            //editTextIntraPkDelay.setText(intraPkDelay); editTextIntraPkDelay.setEnabled(true);
            //editTextDupDelay.setText(dupDelay); editTextDupDelay.setEnabled(true);
        }
    };
    SettingBeforeTagFocus settingBeforeTagFocus = new SettingBeforeTagFocus();

    boolean overriding = false;
    int countrySelect = -1;
    int channelOrder = -1; int channelSelect = -1;
    int channel = -1; final int channelMin = 1; int channelMax = 1; int iPortNumber = 1;
    boolean portEnable = false, powerBoost;
    long powerLevel = -1; final long powerLevelMin = 0; final long powerLevelMax = 300;
    long dwellTime = -1; final long dwellTimeMin = 0; final long dwellTimeMax = 10000;
    byte byteTagDelay = -1; byte byteTagDelayMin = 0; byte byteTagDelayMax = 63;
    byte byteIntraPkDelay = -1; byte byteIntraPkDelayMin = 0; byte byteIntraPkDelayMax = 63;
    byte byteDupDelay = -1; byte byteDupDelayMin = 0; byte byteDupDelayMax = 63;
    int iPopulation = -1; int iPopulationMin = 1; int iPopulationMax = 9999;
    byte byteFixedQValue = -1; byte byteFixedQValueMin = 0; byte byteFixedQValueMax = 15;
    int queryTarget;
    int querySession = -1;
    int tagFocus = -1, fastId = -1;
    int invAlgoDynamic = -1;
    int retry = -1;
    int profile = -1;
    int highCompression = -1, rflnagain = -1, iflnagain = -1, agcgain = -1;

    private SettingTaskCustom settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_settings_operate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.csLibrary4A.get98XX() == 2) {
            TableRow tableRow = (TableRow) view.findViewById(R.id.settingOperateCompactDelayRow);
            tableRow.setVisibility(View.GONE);
            LinearLayout linearLayout0 = (LinearLayout) view.findViewById(R.id.settingOperateOtherConfigurationLayout0);
            linearLayout0.setVisibility(View.GONE);
            LinearLayout linearLayout1 = (LinearLayout) view.findViewById(R.id.settingOperateOtherConfigurationLayout1);
            linearLayout1.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.settingOperateTestConfigurationLayout);
            linearLayout.setVisibility(View.GONE);
        } else {
            TableRow tableRow = (TableRow) view.findViewById(R.id.settingOperateDupDelayRow);
            tableRow.setVisibility(View.GONE);
            TableRow tableRow1 = (TableRow) view.findViewById(R.id.settingOperateIntraPkDelayRow);
            tableRow1.setVisibility(View.GONE);
            LinearLayout linearLayout1 = (LinearLayout) view.findViewById(R.id.settingOperateOtherConfigurationLayout1);
            linearLayout1.setVisibility(View.GONE);
            LinearLayout linearLayout2 = (LinearLayout) view.findViewById(R.id.settingOperateOtherConfigurationLayout2);
            linearLayout2.setVisibility(View.GONE);
        }

        spinnerRegulatoryRegion = (Spinner) view.findViewById(R.id.settingOperateRegulatoryRegion);
        spinnerFrequencyOrder = (Spinner) view.findViewById(R.id.settingOperateFrequencyOrder); spinnerFrequencyOrder.setEnabled(false);
        spinnerChannel = (Spinner) view.findViewById(R.id.settingOperateChannel);
        {
            TableRow tableRow = (TableRow) view.findViewById(R.id.settingOperateChannelRow);
            if (MainActivity.csLibrary4A.getChannelHoppingStatus()) tableRow.setVisibility(View.GONE);
        }

        iPortNumber = MainActivity.csLibrary4A.getPortNumber();
        if (iPortNumber == 1 && false) {
            TableRow tableRow = (TableRow) view.findViewById(R.id.settingOperatePortChannelRow);
            tableRow.setVisibility(View.GONE);
            tableRow = (TableRow) view.findViewById(R.id.settingOperatePortEnableRow);
            tableRow.setVisibility(View.GONE);
            tableRow = (TableRow) view.findViewById(R.id.settingOperatePortDwellRow);
            tableRow.setVisibility(View.GONE);
        } else {
            int iTemp = iPortNumber;
            if (true || iTemp > 1) channelMax = iTemp;
            else channelMax = 16;
            TextView textViewPortChannelLabel = (TextView) view.findViewById(R.id.settingOperatePortChannelLabel);
            if (iPortNumber != 1) textViewPortChannelLabel.setText("Ant port #");
            else textViewPortChannelLabel.setText("Antenna port");
            if (channelMax != 1) {
                String stringPortChannelLabel = textViewPortChannelLabel.getText().toString();
                stringPortChannelLabel += "(" + String.valueOf(channelMin) + "-" + String.valueOf(channelMax) + ")";
                textViewPortChannelLabel.setText(stringPortChannelLabel);
            }
            textViewPortChannel = (TextView) view.findViewById(R.id.settingOperatePortChannel); setTextViewChannel(1);
            buttonPortSelect = (Button) view.findViewById(R.id.settingOperatePortChannelSelect);
            if (iPortNumber == 1) buttonPortSelect.setVisibility(View.GONE);
            if (false && MainActivity.csLibrary4A.get98XX() == 2) buttonPortSelect.setEnabled(false);
            buttonPortSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int channel = getTextViewChannel();
                    if (channel < 1) channel = channelMin;
                    if (++channel > channelMax) channel = channelMin;
                    setTextViewChannel(channel);
                    editTextOperatePower.setText("");
                    editTextPortDwell.setText("");
                    mHandler.post(updateRunnable);
                }
            });

            checkBoxPortEnable = (CheckBox) view.findViewById(R.id.settingOperatePortEnable);
            if (iPortNumber == 1) checkBoxPortEnable.setEnabled(false);

            TextView textViewPortDwellLabel = (TextView) view.findViewById(R.id.settingOperatePortDwellLabel);
            String stringPortDwellLabel = textViewPortDwellLabel.getText().toString();
            stringPortDwellLabel += "(" + String.valueOf(dwellTimeMin) + "-" + String.valueOf(dwellTimeMax) + ")";
            textViewPortDwellLabel.setText(stringPortDwellLabel);
            editTextPortDwell = (EditText) view.findViewById(R.id.settingOperatePortDwell);
        }

        TextView textViewAdminTagDelayLabel = (TextView) view.findViewById(R.id.settingAdminTagDelayLabel);
        String stringAdminTagDelayLabel = textViewAdminTagDelayLabel.getText().toString();
        stringAdminTagDelayLabel += "(" + String.valueOf(byteTagDelayMin) + "-" + String.valueOf(byteTagDelayMax) + "ms)";
        textViewAdminTagDelayLabel.setText(stringAdminTagDelayLabel);
        editTextTagDelay = (EditText) view.findViewById(R.id.settingOperateTagDelay);
        editTextIntraPkDelay = (EditText) view.findViewById(R.id.settingOperateIntraPkDelay);
        editTextDupDelay = (EditText) view.findViewById(R.id.settingOperateDupDelay);

        TextView textViewOperatePowerLabel = (TextView) view.findViewById(R.id.settingOperatePowerLabel);
        String stringOperationPowerLabel = textViewOperatePowerLabel.getText().toString();
        stringOperationPowerLabel += "(" + String.valueOf(powerLevelMin) + "-" + String.valueOf(powerLevelMax) + ")";
        textViewOperatePowerLabel.setText(stringOperationPowerLabel);
        editTextOperatePower = (EditText) view.findViewById(R.id.settingOperatePower);

        checkBoxPowerBoost = (CheckBox) view.findViewById(R.id.settingOperatePowerBoost);
        if (MainActivity.csLibrary4A.get98XX() == 0) {
            checkBoxPowerBoost.setVisibility(View.GONE);
        }

        TextView textViewOperatePopulationLabel = (TextView) view.findViewById(R.id.settingOperatePopulationLabel);
        String stringOperationPopulationLabel = textViewOperatePopulationLabel.getText().toString();
        stringOperationPopulationLabel += "(" + String.valueOf(iPopulationMin) + "-" + String.valueOf(iPopulationMax) + ")";
        textViewOperatePopulationLabel.setText(stringOperationPopulationLabel);
        editTextPopulation = (EditText) view.findViewById(R.id.settingOperatePopulation);
        editTextStartQValue = (EditText) view.findViewById(R.id.settingOperateQValue);

        spinnerQueryTarget = (Spinner) view.findViewById(R.id.settingOperateTarget);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.query_target_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQueryTarget.setAdapter(targetAdapter);

        spinnerQuerySession = (Spinner) view.findViewById(R.id.settingOperateSession);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.query_session_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuerySession.setAdapter(targetAdapter);

        checkBoxTagFocus = (CheckBox) view.findViewById(R.id.settingOperateTagFocus);
        String string = checkBoxTagFocus.getText().toString();
        if (MainActivity.csLibrary4A.get98XX() ==  2) checkBoxTagFocus.setText(string.substring(0, string.length()-1) + ". When enabled, tag select is disabled.)");
        checkBoxTagFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) settingBeforeTagFocus.store();
                else settingBeforeTagFocus.restore();
            }
        });

        checkBoxFastId = (CheckBox) view.findViewById(R.id.settingOperateFastId);

        spinnerInvAlgo = (Spinner) view.findViewById(R.id.settingOperateAlgorithmToUse);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventory_algorithm_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInvAlgo.setAdapter(targetAdapter);

        TextView textViewRetry = (TextView) view.findViewById(R.id.settingOperateRetryLabel);
        if (MainActivity.csLibrary4A.get98XX() == 2) textViewRetry.setText("Minimum Q cycles");
        editTextRetry = (EditText) view.findViewById(R.id.settingOperateRetry);

        spinnerProfile = (Spinner) view.findViewById(R.id.settingOperateProfile);
        if (true) {
            ArrayAdapter<String> targetAdapter1 = new ArrayAdapter<String>(getContext(),  android.R.layout.simple_spinner_dropdown_item, MainActivity.csLibrary4A.getProfileList());
            targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProfile.setAdapter(targetAdapter1);
        }

        MainActivity.csLibrary4A.resetEnvironmentalRSSI();
        textViewEnvironmentalRSSI = (TextView) view.findViewById(R.id.settingOperateEnvironmentalRSSI);
        checkBoxHighCompression = (CheckBox) view.findViewById(R.id.settingOperateHighCompression);

        spinnerRflnaGain = (Spinner) view.findViewById(R.id.settingOperateRflnaGain);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.rflnagain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRflnaGain.setAdapter(targetAdapter);

        spinnerIflnaGain = (Spinner) view.findViewById(R.id.settingOperateIflnaGain);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.iflnagain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIflnaGain.setAdapter(targetAdapter);

        spinnerAgcGain = (Spinner) view.findViewById(R.id.settingOperateAgcGAin);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.agcgain_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgcGain.setAdapter(targetAdapter);

        buttonOverride = (Button) view.findViewById(R.id.settingOperateOverrideButton);
        buttonOverride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strText = buttonOverride.getText().toString();
                int iPopulation2 = -1;
                try {
                    iPopulation2 = Integer.parseInt(editTextPopulation.getText().toString());
                } catch (Exception ex) { }
                if (iPopulation2 <= 0) { }
                else if (strText.contains(strOVERRIDE)) {
                    editTextPopulation.setEnabled(false);
                    editTextStartQValue.setEnabled(true);
                    buttonOverride.setText(strRESET); overriding = true;

                    editTextStartQValue.setText(String.valueOf(MainActivity.csLibrary4A.getPopulation2Q(iPopulation2)));
                } else {
                    editTextPopulation.setEnabled(true);
                    editTextStartQValue.setEnabled(false);;
                    buttonOverride.setText(strOVERRIDE); overriding = false;

                    updatePopulation4Q();
                }
            }
        });

        button = (Button) view.findViewById(R.id.settingSaveButtonOperate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sameCheck = true; settingUpdate1();
            }
        });

        button1 = (Button) view.findViewById(R.id.settingSaveButtonOperate1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sameCheck = false; settingUpdate1();
            }
        });

        mHandler.post(updateRunnable);
    }
    void updatePopulation4Q() {
        int iPopulation2 = -1;
        try {
            iPopulation2 = Integer.parseInt(editTextPopulation.getText().toString());
        } catch (Exception ex) { }
        if (iPopulation2 <= 0) { }
        else {
            int iQValue4Population = (int) MainActivity.csLibrary4A.getPopulation2Q(iPopulation);
            int iQValueNew = -1;
            try {
                iQValueNew = Integer.valueOf(editTextStartQValue.getText().toString());
            } catch (Exception ex) {
            }
            MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.updatePopulation4Q: iPopulation = " + iPopulation + ", iQValue4Population = " + iQValue4Population + ", iQValueNew = " + iQValueNew);
            if (iQValueNew < 0 || iQValueNew > 15)
                editTextStartQValue.setText(String.valueOf(iQValue4Population));
            else if (iQValueNew != iQValue4Population) {
                iPopulation2 = MainActivity.csLibrary4A.getQ2Population(iQValueNew);
                editTextPopulation.setText(String.valueOf(iPopulation2));
                iPopulation = iPopulation2;
            }
            MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.updatePopulation4Q: editTextPopulation.getText = " + editTextPopulation.getText().toString());
        }
    }
    void settingUpdate1() {
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        } else if (MainActivity.csLibrary4A.isRfidFailure()) {
            Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
            return;
        } else if (updateRunning) {
            Toast.makeText(MainActivity.context, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
            return;
        } else {
            MainActivity.csLibrary4A.setSameCheck(sameCheck);
            try {
                countrySelect = spinnerRegulatoryRegion.getSelectedItemPosition();
                channelOrder = spinnerFrequencyOrder.getSelectedItemPosition();
                channelSelect = spinnerChannel.getSelectedItemPosition();
                if (textViewPortChannel != null) channel = getTextViewChannel();
                if (checkBoxPortEnable != null) portEnable = checkBoxPortEnable.isChecked();
                powerLevel = Long.parseLong(editTextOperatePower.getText().toString());
                powerBoost = checkBoxPowerBoost.isChecked();
                if (editTextPortDwell != null)
                    dwellTime = Long.parseLong(editTextPortDwell.getText().toString());
                if (editTextTagDelay != null)
                    byteTagDelay = Byte.parseByte(editTextTagDelay.getText().toString());
                if (editTextIntraPkDelay != null)
                    byteIntraPkDelay = Byte.parseByte(editTextIntraPkDelay.getText().toString());
                if (editTextDupDelay != null)
                    byteDupDelay = Byte.parseByte(editTextDupDelay.getText().toString());
                iPopulation = Integer.parseInt(editTextPopulation.getText().toString());
                MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.settingUpdate1: iPopulation = " + iPopulation);
                byteFixedQValue = Byte.parseByte(editTextStartQValue.getText().toString());
                queryTarget = spinnerQueryTarget.getSelectedItemPosition();
                querySession = spinnerQuerySession.getSelectedItemPosition();
                tagFocus = (checkBoxTagFocus.isChecked() ? 1 : 0);
                fastId = (checkBoxFastId.isChecked() ? 1 : 0);
                invAlgoDynamic = (spinnerInvAlgo.getSelectedItemPosition() == 0 ? 3 : 0);
                retry = Integer.parseInt(editTextRetry.getText().toString());
                profile = spinnerProfile.getSelectedItemPosition();
                highCompression = (checkBoxHighCompression.isChecked() ? 1 : 0);
                rflnagain = spinnerRflnaGain.getSelectedItemPosition();
                iflnagain = spinnerIflnaGain.getSelectedItemPosition();
                agcgain = spinnerAgcGain.getSelectedItemPosition();
                settingUpdate();
            } catch (Exception ex) {
                Toast.makeText(MainActivity.context, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
            }
        }
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
        if (settingTask != null) settingTask.cancel(true);
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    boolean userVisibleHint = true;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) { //getUserVisibleHint()) {
            if (userVisibleHint == false) {
                userVisibleHint = true;
                mHandler.post(updateRunnable);
            }
        } else {
            userVisibleHint = false;
        }
    }

    public SettingOperateFragment() {
        super("SettingOperateFragment");
    }

    int getTextViewChannel() {
        String string = textViewPortChannel.getText().toString();
        String[] strings = string.split(" ");
        return Integer.parseInt(strings[0]);
    }
    void setTextViewChannel(int channel) {
        textViewPortChannel.setText("");
        if (MainActivity.csLibrary4A.setAntennaSelect(channel-1)) {
            String string = String.valueOf(channel);
            if (MainActivity.csLibrary4A.getcsModel() == 203) {
                String string1 = "";
                if (channel == 1) string1 = " [external]";
                else if (channel == 2) string1 = " [internal]";
                string = string + string1;
            }
            textViewPortChannel.setText(string);
        }
    }
    boolean updateRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            updateRunning = true;
            if (MainActivity.csLibrary4A.rfidToWriteSize() != 0)   {
                updating = true; MainActivity.csLibrary4A.appendToLog("updating 1");
            }
            else {
                iPopulation = MainActivity.csLibrary4A.getPopulation();
                if (iPopulation < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 2");
                } else {
                    editTextPopulation.setText(String.valueOf(iPopulation));
                    //byteFixedQValue = MainActivity.csLibrary4A.getQValue();
                    //editTextStartQValue.setText(String.valueOf(byteFixedQValue));
                    editTextStartQValue.setText(String.valueOf(MainActivity.csLibrary4A.getPopulation2Q(iPopulation)));
                }
                if (updating == false && textViewPortChannel != null) {
                    int iValue1 = MainActivity.csLibrary4A.getAntennaSelect();
                    if (iValue1 < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 4");
                    } else {
                        setTextViewChannel(iValue1+1);
                    }
                }
                if (checkBoxPortEnable != null) checkBoxPortEnable.setChecked(MainActivity.csLibrary4A.getAntennaEnable() > 0);
                if (checkBoxPowerBoost != null) checkBoxPowerBoost.setChecked(MainActivity.csLibrary4A.getPowerBoost() > 0);
                if (updating == false) {
                    lValue = MainActivity.csLibrary4A.getPwrlevel();
                    if (lValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 5");
                    } else {
                        editTextOperatePower.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false && editTextPortDwell != null) {
                    lValue = MainActivity.csLibrary4A.getAntennaDwell();
                    if (lValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 6");
                    } else {
                        editTextPortDwell.setText(String.valueOf(lValue));
                    }
                }
                if (editTextTagDelay != null)   editTextTagDelay.setText(String.valueOf(MainActivity.csLibrary4A.getTagDelay()));
                if (editTextIntraPkDelay != null)   editTextIntraPkDelay.setText(String.valueOf(MainActivity.csLibrary4A.getIntraPkDelay()));
                if (editTextDupDelay != null)   editTextDupDelay.setText(String.valueOf(MainActivity.csLibrary4A.getDupDelay()));
                if (updating == false) {
                    spinnerQueryTarget.setSelection(MainActivity.csLibrary4A.getQueryTarget());
                }
                if (updating == false) {
                    iValue = MainActivity.csLibrary4A.getQuerySession();
                    if (iValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 7");
                    } else {
                        spinnerQuerySession.setSelection(iValue);
                    }
                }
                if (updating == false) {
                    iValue = MainActivity.csLibrary4A.getTagFocus();
                    if (iValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 8");
                    }
                    else {
                        checkBoxTagFocus.setChecked(iValue > 0 ? true : false);
                        if (checkBoxTagFocus.isChecked()) settingBeforeTagFocus.store();
                    }
                }
                if (updating == false) {
                    iValue = MainActivity.csLibrary4A.getFastId();
                    if (iValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 8");
                    }
                    else checkBoxFastId.setChecked(iValue > 0 ? true : false);
                }
                if (updating == false) {
                    spinnerInvAlgo.setSelection(MainActivity.csLibrary4A.getInvAlgo() == 3 ? 0 : 1);
                }
                if (updating == false) {
                    int iRetry = MainActivity.csLibrary4A.getRetryCount();
                    if (iRetry < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 9");
                    }
                    else editTextRetry.setText(String.valueOf(iRetry));
                }
                if (updating == false) {
                    String[] strCountryList = MainActivity.csLibrary4A.getCountryList();
                    MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.updateRunnable.run: strCountryList is " + (strCountryList == null ? "null" : "valid"));
                    for (int i = 0; i < strCountryList.length; i++) MainActivity.csLibrary4A.appendToLog("updating: String " + i + " = " + strCountryList[i]);
                    String[] strChannelFrequencyList = MainActivity.csLibrary4A.getChannelFrequencyList();
                    //for (int i = 0; i < strChannelFrequencyList.length; i++) MainActivity.csLibrary4A.appendToLog("updating: String " + i + " = " + strChannelFrequencyList[i]);
                    if (strCountryList == null) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("updating 10");
                    } else {
                        ArrayAdapter targetAdapter1 = new ArrayAdapter(getActivity(), R.layout.custom_spinner_layout, strCountryList);
                        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRegulatoryRegion.setAdapter(targetAdapter1);
                        int countryNumber = MainActivity.csLibrary4A.getCountryNumberInList();
                        MainActivity.csLibrary4A.appendToLog("updating countryNumber = " + countryNumber);
                        if (countryNumber < 0 || countryNumber > strCountryList.length) spinnerRegulatoryRegion.setSelection(0);
                        else spinnerRegulatoryRegion.setSelection(countryNumber);
                        if (strCountryList.length == 1) spinnerRegulatoryRegion.setEnabled(false);
                        else spinnerRegulatoryRegion.setEnabled(true);

                        ArrayAdapter<CharSequence> targetAdapter;
                        //if (MainActivity.csLibrary4A.getChannelHoppingDefault())
                            targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequencyOrder_options, R.layout.custom_spinner_layout);
                        //else targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequencyAgile_options, R.layout.custom_spinner_layout);
                        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerFrequencyOrder.setAdapter(targetAdapter);
                        spinnerFrequencyOrder.setSelection(MainActivity.csLibrary4A.getChannelHoppingStatus() ? 0 : 1);
                        if (MainActivity.csLibrary4A.getChannelHoppingStatus()) spinnerChannel.setEnabled(false);
                        else spinnerChannel.setEnabled(true);

                        ArrayAdapter targetAdapter2 = new ArrayAdapter(getActivity(), R.layout.custom_spinner_layout, strChannelFrequencyList);
                        targetAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerChannel.setAdapter(targetAdapter2);
                        int channel = MainActivity.csLibrary4A.getChannel();
                        MainActivity.csLibrary4A.appendToLog("channel = " + channel);
                        if (channel < 0 || channel > strChannelFrequencyList.length) spinnerChannel.setSelection(0);
                        else spinnerChannel.setSelection(channel);
                    }
                }
            }
            if (updating == false) {
                iValue = MainActivity.csLibrary4A.getCurrentProfile();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 11");
                } else {
                    spinnerProfile.setSelection(iValue);
                }
            }
            if (updating == false) {
                String strRssi = MainActivity.csLibrary4A.getEnvironmentalRSSI();
                if (strRssi == null) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 12");
                }
                else textViewEnvironmentalRSSI.setText(strRssi);
            }
            if (updating == false) {
                iValue = MainActivity.csLibrary4A.getHighCompression();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 13");
                } else checkBoxHighCompression.setChecked(iValue == 0 ? false : true);
            }
            if (updating == false) {
                iValue = MainActivity.csLibrary4A.getRflnaGain();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 14");
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
                iValue = MainActivity.csLibrary4A.getIflnaGain();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 15");
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
                iValue = MainActivity.csLibrary4A.getAgcGain();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("updating 16");
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

        if (invalidRequest == false && (MainActivity.csLibrary4A.getCountryNumberInList() != countrySelect || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 1");
            if (MainActivity.csLibrary4A.setCountryInList(countrySelect) == false)    invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getChannelHoppingStatus() != (channelOrder == 0 ? true : false) || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 2");
            if (MainActivity.csLibrary4A.setChannelHoppingStatus(channelOrder == 0 ? true : false) == false)    invalidRequest = true;
            else if (channelOrder > 0) spinnerChannel.setEnabled(true);
            else spinnerChannel.setEnabled(false);
            spinnerChannel.setSelection(MainActivity.csLibrary4A.getChannel()); MainActivity.csLibrary4A.appendToLog("1 channel = ");
        }
        MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.settingUpdate: invalidRequest = " + invalidRequest + ", getChannel = " + MainActivity.csLibrary4A.getChannel() + ", channelSelect = " + channelSelect + ", sameCheck = " + sameCheck);
        if (invalidRequest == false && (MainActivity.csLibrary4A.getChannel() != channelSelect || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.settingUpdate: point 3, channelSelect = " + channelSelect + ", FreqChnCnt = " + MainActivity.csLibrary4A.FreqChnCnt());
            if (channelSelect < MainActivity.csLibrary4A.FreqChnCnt()) {
                if (MainActivity.csLibrary4A.setChannel(channelSelect) == false)    invalidRequest = true;
            } else {
                invalidRequest = true;
            }
        }
        if (false && invalidRequest == false && (MainActivity.csLibrary4A.getAntennaSelect() + 1 != channel  || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 4");
            if (channel < channelMin || channel > channelMax) invalidRequest = true;
            else if (MainActivity.csLibrary4A.setAntennaSelect(channel - 1) == false) invalidRequest = true;
            else changedChannel = true;
        }
        if (invalidRequest == false && ((MainActivity.csLibrary4A.getAntennaEnable() > 0) != portEnable || sameCheck == false || changedChannel)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 5");
            if (MainActivity.csLibrary4A.setAntennaEnable(portEnable) == false) {
                MainActivity.csLibrary4A.appendToLog("point 5A");
                invalidRequest = true;
            }
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getPwrlevel() != powerLevel || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 6");
            if (powerLevel < powerLevelMin) invalidRequest = true;
            else if (powerLevel > MainActivity.csLibrary4A.getPowerLevelMax()) {
                CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.context);
                customPopupWindow.popupStart("Power can only be set to 320 or below", false);
                invalidRequest = true;
            }
            else if (MainActivity.csLibrary4A.setPowerLevel(powerLevel) == false) invalidRequest = true;
        }
        if (invalidRequest == false && ((MainActivity.csLibrary4A.getPowerBoost() > 0) != powerBoost || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 6A");
            if (MainActivity.csLibrary4A.setPowerBoost(powerBoost) == false) invalidRequest = true;
        }
        if ((invalidRequest == false && (MainActivity.csLibrary4A.getAntennaDwell() != dwellTime || sameCheck == false || changedChannel))) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 7");
            if (dwellTime < dwellTimeMin || dwellTime > dwellTimeMax) invalidRequest = true;
            else if (MainActivity.csLibrary4A.setAntennaDwell(dwellTime) == false) invalidRequest = true;
        }
        if ((invalidRequest == false && editTextTagDelay != null)) {
            if (MainActivity.csLibrary4A.getTagDelay() != byteTagDelay || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 8");
                if (byteTagDelay < byteTagDelayMin || byteTagDelay > byteTagDelayMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setTagDelay(byteTagDelay) == false)
                    invalidRequest = true;
            }
        }
        if ((invalidRequest == false && editTextIntraPkDelay != null)) {
            if (MainActivity.csLibrary4A.getIntraPkDelay() != byteIntraPkDelay || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 9A");
                if (byteDupDelay < byteIntraPkDelayMin || byteIntraPkDelay > byteIntraPkDelayMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setIntraPkDelay(byteIntraPkDelay) == false)
                    invalidRequest = true;
            }
        }
        if ((invalidRequest == false && editTextDupDelay != null)) {
            if (MainActivity.csLibrary4A.getDupDelay() != byteDupDelay || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 9");
                if (byteDupDelay < byteDupDelayMin || byteDupDelay > byteDupDelayMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setDupDelay(byteDupDelay) == false)
                    invalidRequest = true;
            }
        }

        MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.settingUpdate: overriding = " + overriding + ", invalidRequest = " + invalidRequest + ", getPopulation = " + MainActivity.csLibrary4A.getPopulation()
                + ", iPopulation = " + iPopulation + ", getQValue = " + MainActivity.csLibrary4A.getQValue() + ", byteFixedQValue = " + byteFixedQValue + ", sameCheck = " + sameCheck);
        if (overriding) updatePopulation4Q();
        MainActivity.csLibrary4A.appendToLog("SettingOperateFragment.settingUpdate: 2 overriding = " + overriding + ", invalidRequest = " + invalidRequest + ", getPopulation = " + MainActivity.csLibrary4A.getPopulation()
                + ", iPopulation = " + iPopulation + ", getQValue = " + MainActivity.csLibrary4A.getQValue() + ", byteFixedQValue = " + byteFixedQValue + ", sameCheck = " + sameCheck);
        if (invalidRequest == false && (MainActivity.csLibrary4A.getPopulation() != iPopulation || MainActivity.csLibrary4A.getQValue() != byteFixedQValue  || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 10");
            if (iPopulation < iPopulationMin || iPopulation > iPopulationMax) invalidRequest = true;
            else if (MainActivity.csLibrary4A.setPopulation(iPopulation) == false) {
                invalidRequest = true;
            } else {
                editTextStartQValue.setText(String.valueOf(MainActivity.csLibrary4A.getPopulation2Q(iPopulation)));
            }
        }
        if ((MainActivity.csLibrary4A.getQueryTarget() != queryTarget
                || MainActivity.csLibrary4A.getQuerySession() != querySession || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 11");
            if (MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), querySession, queryTarget) == false)
                invalidRequest = true;
        }
        if (MainActivity.csLibrary4A.getTagFocus() != tagFocus || sameCheck == false) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 12");
            if (MainActivity.csLibrary4A.setTagFocus(tagFocus > 0 ? true : false) == false)
                invalidRequest = true;
        }
        if (MainActivity.csLibrary4A.getFastId() != fastId || sameCheck == false) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 12");
            if (MainActivity.csLibrary4A.setFastId(fastId > 0 ? true : false) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.getInvAlgo() != invAlgoDynamic || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 13");
                if (MainActivity.csLibrary4A.setInvAlgo(invAlgoDynamic == 3) == false)
                    invalidRequest = true;
                spinnerQueryTarget.setSelection(MainActivity.csLibrary4A.getQueryTarget());
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.getRetryCount() != retry || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 14");
                if (MainActivity.csLibrary4A.setRetryCount(retry) == false)
                    invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.getCurrentProfile() != profile || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 15 with profile = " + profile);
                if (MainActivity.csLibrary4A.setCurrentLinkProfile(profile) == false)
                    invalidRequest = true;
            }
        }
        MainActivity.csLibrary4A.appendToLog("point end 0 with invalidRequest = " + invalidRequest);
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
            if ((MainActivity.csLibrary4A.getHighCompression() != highCompression)
                    || (MainActivity.csLibrary4A.getRflnaGain() != rflnagain)
                    || (MainActivity.csLibrary4A.getIflnaGain() != iflnagain)
                    || (MainActivity.csLibrary4A.getAgcGain() != agcgain)
                    || sameCheck == false) {
                sameSetting = false;
                if (MainActivity.csLibrary4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain) == false) {
                    MainActivity.csLibrary4A.appendToLog("point end 1");
                    invalidRequest = true;
                }
            }
        }
        MainActivity.csLibrary4A.appendToLog("point end 2 with invalidRequest = " + invalidRequest);
        settingTask = new SettingTaskCustom((sameCheck ? button: button1), sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.csLibrary4A.saveSetting2File();
        mHandler.post(updateRunnable);
    }
}
