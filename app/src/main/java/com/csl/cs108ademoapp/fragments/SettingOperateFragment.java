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

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingOperateFragment extends CommonFragment {
    final String strOVERRIDE = "Override"; final String strRESET = "Reset";
    private CheckBox checkBoxPortEnable, checkBoxTagFocus, checkBoxFastId, checkBoxHighCompression;
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
    boolean portEnable = false;
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

        if (MainActivity.csLibrary4A.get98XX() == 2) {
            TableRow tableRow = (TableRow) getActivity().findViewById(R.id.settingOperateCompactDelayRow);
            tableRow.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.settingOperateGainControl);
            linearLayout.setVisibility(View.GONE);
        } else {
            TableRow tableRow = (TableRow) getActivity().findViewById(R.id.settingOperateDupDelayRow);
            tableRow.setVisibility(View.GONE);
            TableRow tableRow1 = (TableRow) getActivity().findViewById(R.id.settingOperateIntraPkDelayRow);
            tableRow1.setVisibility(View.GONE);
            //TableRow tableRow2 = (TableRow) getActivity().findViewById(R.id.settingOperatePortWarningRow);
            //tableRow2.setVisibility(View.GONE);
        }

        spinnerRegulatoryRegion = (Spinner) getActivity().findViewById(R.id.settingOperateRegulatoryRegion);
        spinnerFrequencyOrder = (Spinner) getActivity().findViewById(R.id.settingOperateFrequencyOrder); spinnerFrequencyOrder.setEnabled(false);
        spinnerChannel = (Spinner) getActivity().findViewById(R.id.settingOperateChannel);

        iPortNumber = MainActivity.csLibrary4A.getPortNumber();
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
            if (false && MainActivity.csLibrary4A.get98XX() == 2) buttonPortSelect.setEnabled(false);
            buttonPortSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int channel = Integer.parseInt(textViewPortChannel.getText().toString());
                    if (channel < 1) channel = channelMin;
                    if (++channel > channelMax) channel = channelMin;
                    textViewPortChannel.setText(""); if (MainActivity.csLibrary4A.setAntennaSelect(channel-1))    textViewPortChannel.setText(String.valueOf(channel));
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
        editTextIntraPkDelay = (EditText) getActivity().findViewById(R.id.settingOperateIntraPkDelay);
        editTextDupDelay = (EditText) getActivity().findViewById(R.id.settingOperateDupDelay);

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
        String string = checkBoxTagFocus.getText().toString();
        if (MainActivity.csLibrary4A.get98XX() !=  0) checkBoxTagFocus.setText(string.substring(0, string.length()-1) + ". When enabled, tag select is disabled.)");
        checkBoxTagFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) settingBeforeTagFocus.store();
                else settingBeforeTagFocus.restore();
            }
        });

        checkBoxFastId = (CheckBox) getActivity().findViewById(R.id.settingOperateFastId);

        spinnerInvAlgo = (Spinner) getActivity().findViewById(R.id.settingOperateAlgorithmToUse);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.inventory_algorithm_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInvAlgo.setAdapter(targetAdapter);

        editTextRetry = (EditText) getActivity().findViewById(R.id.settingOperateRetry);

        spinnerProfile = (Spinner) getActivity().findViewById(R.id.settingOperateProfile);
        if (true) {
            ArrayAdapter<String> targetAdapter1 = new ArrayAdapter<String>(getContext(),  android.R.layout.simple_spinner_dropdown_item, MainActivity.csLibrary4A.getProfileList());
            targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProfile.setAdapter(targetAdapter1);
        }

        MainActivity.csLibrary4A.resetEnvironmentalRSSI();
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
                    editTextStartQValue.setText(String.valueOf(MainActivity.csLibrary4A.getPopulation2Q(iPopulation)));
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
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
            return;
        } else if (MainActivity.csLibrary4A.isRfidFailure()) {
            Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
            return;
        } else if (updateRunning) {
            Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
            return;
        } else {
            MainActivity.csLibrary4A.setSameCheck(sameCheck);
            try {
                countrySelect = spinnerRegulatoryRegion.getSelectedItemPosition();
                channelOrder = spinnerFrequencyOrder.getSelectedItemPosition();
                channelSelect = spinnerChannel.getSelectedItemPosition();
                if (textViewPortChannel != null)
                    channel = Integer.parseInt(textViewPortChannel.getText().toString());
                if (checkBoxPortEnable != null) portEnable = checkBoxPortEnable.isChecked();
                powerLevel = Long.parseLong(editTextOperatePower.getText().toString());
                if (editTextPortDwell != null)
                    dwellTime = Long.parseLong(editTextPortDwell.getText().toString());
                if (editTextTagDelay != null)
                    byteTagDelay = Byte.parseByte(editTextTagDelay.getText().toString());
                if (editTextIntraPkDelay != null)
                    byteIntraPkDelay = Byte.parseByte(editTextIntraPkDelay.getText().toString());
                if (editTextDupDelay != null)
                    byteDupDelay = Byte.parseByte(editTextDupDelay.getText().toString());
                iPopulation = Integer.parseInt(editTextPopulation.getText().toString());
                byteFixedQValue = Byte.parseByte(editTextStartQValue.getText().toString());
                queryTarget = spinnerQueryTarget.getSelectedItemPosition();
                querySession = spinnerQuerySession.getSelectedItemPosition();
                tagFocus = (checkBoxTagFocus.isChecked() ? 1 : 0);
                fastId = (checkBoxFastId.isChecked() ? 1 : 0);
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
        MainActivity.csLibrary4A.setSameCheck(true);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    boolean userVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        MainActivity.csLibrary4A.appendToLog("isVisibleToUser = " + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
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

    boolean updateRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            updateRunning = true;
            if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0)   {
                updating = true; MainActivity.csLibrary4A.appendToLog("upating 1");
            }
            else {
                iPopulation = MainActivity.csLibrary4A.getPopulation();
                if (iPopulation < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 2");
                }
                else {
                    editTextPopulation.setText(String.valueOf(iPopulation));

                    byteFixedQValue = MainActivity.csLibrary4A.getQValue();
                    editTextStartQValue.setText(String.valueOf(byteFixedQValue));
                    if (MainActivity.csLibrary4A.getPopulation2Q(iPopulation) != byteFixedQValue) {
                        buttonOverride.setText(strRESET); overriding = true;
                    } else {
                        buttonOverride.setText(strOVERRIDE); overriding = false;
                    }
                }
                if (updating == false && textViewPortChannel != null) {
                    lValue = MainActivity.csLibrary4A.getAntennaSelect();
                    if (lValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 4");
                    } else {
                        textViewPortChannel.setText(String.valueOf(lValue+1));
                    }
                }
                if (checkBoxPortEnable != null) checkBoxPortEnable.setChecked(MainActivity.csLibrary4A.getAntennaEnable());
                if (updating == false) {
                    lValue = MainActivity.csLibrary4A.getPwrlevel();
                    if (lValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 5");
                    } else {
                        editTextOperatePower.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false && editTextPortDwell != null) {
                    lValue = MainActivity.csLibrary4A.getAntennaDwell();
                    if (lValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 6");
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
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 7");
                    } else {
                        spinnerQuerySession.setSelection(iValue);
                    }
                }
                if (updating == false) {
                    iValue = MainActivity.csLibrary4A.getTagFocus();
                    if (iValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 8");
                    }
                    else {
                        checkBoxTagFocus.setChecked(iValue > 0 ? true : false);
                        if (checkBoxTagFocus.isChecked()) settingBeforeTagFocus.store();
                    }
                }
                if (updating == false) {
                    iValue = MainActivity.csLibrary4A.getFastId();
                    if (iValue < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 8");
                    }
                    else checkBoxFastId.setChecked(iValue > 0 ? true : false);
                }
                if (updating == false) {
                    spinnerInvAlgo.setSelection(MainActivity.csLibrary4A.getInvAlgo() ? 0 : 1);
                }
                if (updating == false) {
                    int iRetry = MainActivity.csLibrary4A.getRetryCount();
                    if (iRetry < 0) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 9");
                    }
                    else editTextRetry.setText(String.valueOf(iRetry));
                }
                if (updating == false) {
                    String[] strCountryList = MainActivity.csLibrary4A.getCountryList();
                    for (int i = 0; i < strCountryList.length; i++) MainActivity.csLibrary4A.appendToLog("upating: String " + i + " = " + strCountryList[i]);
                    String[] strChannelFrequencyList = MainActivity.csLibrary4A.getChannelFrequencyList();
                    //for (int i = 0; i < strChannelFrequencyList.length; i++) MainActivity.csLibrary4A.appendToLog("upating: String " + i + " = " + strChannelFrequencyList[i]);
                    if (strCountryList == null) {
                        updating = true; MainActivity.csLibrary4A.appendToLog("upating 10");
                    } else {
                        ArrayAdapter targetAdapter1 = new ArrayAdapter(getActivity(), R.layout.custom_spinner_layout, strCountryList);
                        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRegulatoryRegion.setAdapter(targetAdapter1);
                        int countryNumber = MainActivity.csLibrary4A.getCountryNumberInList();
                        MainActivity.csLibrary4A.appendToLog("upating countryNumber = " + countryNumber);
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
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 11");
                } else {
                    spinnerProfile.setSelection(iValue);
                }
            }
            if (updating == false) {
                String strRssi = MainActivity.csLibrary4A.getEnvironmentalRSSI();
                if (strRssi == null) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 12");
                }
                else textViewEnvironmentalRSSI.setText(strRssi);
            }
            if (updating == false) {
                iValue = MainActivity.csLibrary4A.getHighCompression();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 13");
                } else checkBoxHighCompression.setChecked(iValue == 0 ? false : true);
            }
            if (updating == false) {
                iValue = MainActivity.csLibrary4A.getRflnaGain();
                if (iValue < 0) {
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 14");
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
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 15");
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
                    updating = true; MainActivity.csLibrary4A.appendToLog("upating 16");
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
        if (invalidRequest == false && (MainActivity.csLibrary4A.getChannel() != channelSelect || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 3");
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
        if (invalidRequest == false && (MainActivity.csLibrary4A.getAntennaEnable() != portEnable || sameCheck == false || changedChannel)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 5");
            if (MainActivity.csLibrary4A.setAntennaEnable(portEnable) == false)
                invalidRequest = true;
        }
        if (invalidRequest == false && (MainActivity.csLibrary4A.getPwrlevel() != powerLevel || sameCheck == false)) {
            sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 6");
            if (powerLevel < powerLevelMin || powerLevel > 330) invalidRequest = true;
            else if (MainActivity.csLibrary4A.setPowerLevel(powerLevel) == false) invalidRequest = true;
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

        if (overriding) {
            if (MainActivity.csLibrary4A.getQValue() != byteFixedQValue || sameCheck == false) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 9");
                if (byteFixedQValue < byteFixedQValueMin || byteFixedQValue > byteFixedQValueMax) invalidRequest = true;
                if (MainActivity.csLibrary4A.setQValue(byteFixedQValue) == false)
                    invalidRequest = true;
            }
        } else {
            if (invalidRequest == false && (MainActivity.csLibrary4A.getPopulation() != iPopulation || MainActivity.csLibrary4A.getQValue() != byteFixedQValue || sameCheck == false)) {
                sameSetting = false; MainActivity.csLibrary4A.appendToLog("point 10");
                if (iPopulation < iPopulationMin || iPopulation > iPopulationMax) invalidRequest = true;
                else if (MainActivity.csLibrary4A.setPopulation(iPopulation) == false) {
                    invalidRequest = true;
                } else {
                    editTextStartQValue.setText(String.valueOf(MainActivity.csLibrary4A.getPopulation2Q(iPopulation)));
                }
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
                if (MainActivity.csLibrary4A.setInvAlgo(invAlgoDynamic) == false)
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
                if (MainActivity.csLibrary4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain) == false)
                    invalidRequest = true;
            }
        }
        settingTask = new SettingTask((sameCheck ? button: button1), sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.csLibrary4A.saveSetting2File();
        mHandler.post(updateRunnable);
    }
}
