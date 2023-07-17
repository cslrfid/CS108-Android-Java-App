package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SettingTask;

public class SettingFilterPreFragment extends CommonFragment {
    private EditText editTextFilterPreSelectIndex;
    private CheckBox checkBoxEnable;
    private Spinner targetSpinner;
    private Spinner actionSpinner;
    private Spinner memoryBankSpinner;
    private Spinner spinnerMaskDataType;
    private EditText preFilterOffset;
    private EditText filterPreMaskDataHex, filterPreMaskDataBit;
    private Button button;

    final boolean sameCheck = false;
    Handler mHandler = new Handler();

    int invSelectIndex = -1;
    boolean invSelectEnable;
    int invSelectTarget = -1;
    int invSelectAction = -1;
    int invSelectMaskBank = -1;
    int invSelectMaskOffset = -1;
    String invSelectMaskData = null;

    private SettingTask settingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_filterpre_content, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextFilterPreSelectIndex = (EditText) getActivity().findViewById(R.id.filterPreSelectxIndex);
        checkBoxEnable = (CheckBox) getActivity().findViewById(R.id.filterPreCheck);

        targetSpinner = (Spinner) getActivity().findViewById(R.id.preFilterTarget);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filterPre_target_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetSpinner.setAdapter(targetAdapter);
        if (false) targetSpinner.setEnabled(false);

        actionSpinner = (Spinner) getActivity().findViewById(R.id.preFilterAction);
        ArrayAdapter<CharSequence> actionAdapter;
        if (true) { //MainActivity.csLibrary4A.getQuerySelect() >= 2) {
            actionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filterPre_SLaction_options, R.layout.custom_spinner_layout);
        } else {
            actionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filterPre_SessionAction_options, R.layout.custom_spinner_layout);
        }
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(actionAdapter);
        if (false) actionSpinner.setEnabled(false);

        memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.preFilterMemoryBank);
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memoryBankSpinner.setAdapter(memoryBankAdapter);
        memoryBankSpinner.setEnabled(true);

        spinnerMaskDataType = (Spinner) getActivity().findViewById(R.id.filterPreMaskDataType);
        ArrayAdapter<CharSequence> maskDataTypeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dataType_options, R.layout.custom_spinner_layout);
        maskDataTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaskDataType.setAdapter(maskDataTypeAdapter);
        spinnerMaskDataType.setEnabled(true);
        spinnerMaskDataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerMaskDataType.getSelectedItemPosition() == 0) {
                    filterPreMaskDataHex.setVisibility(View.VISIBLE);
                    filterPreMaskDataBit.setVisibility(View.GONE);
                } else {
                    filterPreMaskDataHex.setVisibility(View.GONE);
                    filterPreMaskDataBit.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        preFilterOffset = (EditText) getActivity().findViewById(R.id.filterPreOffset);
        filterPreMaskDataHex = (EditText) getActivity().findViewById(R.id.filterPreMaskDataHex);
        filterPreMaskDataBit = (EditText) getActivity().findViewById(R.id.filterPreMaskDataBit);

        button = (Button) getActivity().findViewById(R.id.filterPreSaveButton);
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
                    try {
                        invSelectIndex = Integer.parseInt(editTextFilterPreSelectIndex.getText().toString());
                        invSelectEnable = checkBoxEnable.isChecked();
                        invSelectTarget = targetSpinner.getSelectedItemPosition();
                        invSelectAction = actionSpinner.getSelectedItemPosition();
                        invSelectMaskBank = memoryBankSpinner.getSelectedItemPosition() + 1;
                        invSelectMaskOffset = Integer.parseInt(preFilterOffset.getText().toString()); if (invSelectMaskBank == 1) invSelectMaskOffset += 32;

                        settingUpdate();
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_invalid_range, Toast.LENGTH_SHORT).show();
                    }
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

    public SettingFilterPreFragment() {
        super("SettingFilterPreFragment");
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            int iValue;
            long lValue;
            boolean updating = false;

            if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0)   updating = true;
            else {
                if (updating == false && editTextFilterPreSelectIndex.getText().length() == 0) {
                    lValue = MainActivity.csLibrary4A.getInvSelectIndex();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getInvSelectIndex = " + lValue);
                    if (lValue < 0) {
                        updating = true;
                    } else {
                        editTextFilterPreSelectIndex.setText(String.valueOf(lValue));
                    }
                }
                if (updating == false) {
                    boolean bValue = MainActivity.csLibrary4A.getSelectEnable();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectEnable = " + bValue);
                    checkBoxEnable.setChecked(bValue);
                }
                if (updating == false) {
                    int iValue1 = MainActivity.csLibrary4A.getSelectTarget();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectTarget = " + iValue1);
                    if (iValue1 < 0) {
                        updating = true;
                    } else {
                        if (iValue1 > 4) iValue1 = 4;
                        iValue1 = 4;    //fixed as SL
                        targetSpinner.setSelection(iValue1);
                    }
                }
                if (updating == false) {
                    int iValue1 = MainActivity.csLibrary4A.getSelectAction();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectAction = " + iValue1);
                    if (iValue1 < 0) {
                        updating = true;
                    } else {
                        actionSpinner.setSelection(iValue1);
                    }
                }
                if (updating == false) {
                    int iValue1 = MainActivity.csLibrary4A.getSelectMaskBank();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectMaskBank = " + iValue1);
                    if (iValue1 < 0) {
                        updating = true;
                    } else {
                        if (iValue1 < 1) iValue1 = 1;
                        iValue1--;
                        memoryBankSpinner.setSelection(iValue1);
                    }
                }
                if (updating == false) {
                    int iValue1 = MainActivity.csLibrary4A.getSelectMaskOffset();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectMaskOffset = " + iValue1);
                    if (iValue1 < 0) {
                        updating = true;
                    } else {
                        if (iValue1 < 32) iValue1 = 32;
                        iValue1 -= 32;
                        preFilterOffset.setText(String.valueOf(iValue1));
                    }
                }
                if (updating == false) {
                    String strValue = MainActivity.csLibrary4A.getSelectMaskData();
                    MainActivity.csLibrary4A.appendToLog("updateRunnable getSelectMaskData = " + strValue);
                    if (strValue == null) {
                        updating = true;
                    } else {
                        spinnerMaskDataType.setSelection(0);
                        filterPreMaskDataHex.setText(strValue);
                    }
                }
            }
            if (updating) {
                mHandler.postDelayed(updateRunnable, 1000);
            }
        }
    };

    void settingUpdate() {
        boolean sameSetting = true;
        boolean invalidRequest = false;

        if (MainActivity.csLibrary4A.getInvSelectIndex() != invSelectIndex) {
            sameSetting = false;
            if (MainActivity.csLibrary4A.setInvSelectIndex(invSelectIndex) == false)
                invalidRequest = true;
            else {
                mHandler.removeCallbacks(updateRunnable);
                mHandler.post(updateRunnable);
            }
        }
        if (sameSetting == true && invalidRequest == false) {
            if (true)
            {
                boolean dataMatched = false;
                boolean maskbit = (spinnerMaskDataType.getSelectedItemPosition() == 0 ? false : true);
                if (maskbit) invSelectMaskData = filterPreMaskDataBit.getText().toString();
                else invSelectMaskData = filterPreMaskDataHex.getText().toString();
                String strValue = MainActivity.csLibrary4A.getSelectMaskData();
                if (invSelectMaskData.length() != strValue.length()) { }
                else if (invSelectMaskData.length() == 0 && strValue.length() == 0) { dataMatched = true; }
                else dataMatched = invSelectMaskData.matches(strValue);
                if (MainActivity.csLibrary4A.getSelectEnable() !=  invSelectEnable
                        || MainActivity.csLibrary4A.getSelectTarget() != invSelectTarget
                        || MainActivity.csLibrary4A.getSelectAction() != invSelectAction
                        || MainActivity.csLibrary4A.getSelectMaskBank() != invSelectMaskBank
                        || MainActivity.csLibrary4A.getSelectMaskOffset() != invSelectMaskOffset
                        || dataMatched == false  || sameCheck == false) {
                    sameSetting = false;
                    if (MainActivity.csLibrary4A.setSelectCriteria(0, invSelectEnable, invSelectTarget, invSelectAction, invSelectMaskBank, invSelectMaskOffset, invSelectMaskData, maskbit) == false)
                        invalidRequest = true;
                }
            }
        }

        settingTask = new SettingTask(button, sameSetting, invalidRequest);
        settingTask.execute();
        MainActivity.csLibrary4A.saveSetting2File();
    }
}

