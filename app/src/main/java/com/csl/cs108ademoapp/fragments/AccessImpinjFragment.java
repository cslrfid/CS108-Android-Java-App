package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_IMPINJ;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.AccessTask1;
import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;
import com.google.android.material.tabs.TabLayout;

public class AccessImpinjFragment extends CommonFragment {
    CheckBox checkBoxTagFocus, checkBoxFastId, checkBoxProtectSelect, checkBoxAutoTuneDisable, checkBoxProtect, checkBoxShortRange, checkBoxMemorySelect, checkBoxUnkillable;
    Spinner spinnerTagSelect;

    TextView textViewUserValue;
    Button buttonReadUserBank, buttonWriteUserBank;
    TextView textViewRunTime;
    enum impinjTag {
        m775, m780, m830, m770, m730, monza_R6A, monza_R6P, monza_x8k, others
    }

    SelectTag selectTag;
    TextView textViewAuthenticatedResult, textViewAutotuneValue, textViewProtectValue, textViewProtectNormalValue, textViewEpc128Value, textViewConfiguration;
    Button button, buttonAutoTuneValueRead, buttonProtectValueRead, buttonProtectResumeRead, buttonEpc128ValueRead, buttonRead, buttonWrite;
    boolean operationRead = false;
    AccessTask accessTask;
    AccessTask1 accessTask1;
    int iRunType = -1; String stringNewAutoTuneConfig = null;
    int unprotecting = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_impinj, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxTagFocus = (CheckBox) getActivity().findViewById(R.id.accessImpinjTagFocus);
        if (MainActivity.csLibrary4A.get98XX() == 2) checkBoxTagFocus.setText(checkBoxTagFocus.getText().toString() + " (When enabled, tag select is disabled.)");
        MainActivity.csLibrary4A.appendToLog("CheckBoxTagFocus is set");
        checkBoxFastId = (CheckBox) getActivity().findViewById(R.id.accessImpinjFastId);

        checkBoxAutoTuneDisable = (CheckBox) getActivity().findViewById(R.id.accessImpinjAutoTune);
        checkBoxProtect = (CheckBox) getActivity().findViewById(R.id.accessImpinjProtect);
        checkBoxShortRange = (CheckBox) getActivity().findViewById(R.id.accessImpinjShortRange);
        checkBoxMemorySelect = (CheckBox) getActivity().findViewById(R.id.accessImpinjMemorySelect);
        checkBoxUnkillable = (CheckBox) getActivity().findViewById(R.id.accessImpinjUnkillable);

        spinnerTagSelect = (Spinner) getActivity().findViewById(R.id.accessImpinjTagSelect);
        ArrayAdapter<CharSequence> targetAdapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.impinj_options, R.layout.custom_spinner_layout);
        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagSelect.setAdapter(targetAdapter1); spinnerTagSelect.setSelection(0);
        spinnerTagSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);
                TabLayout.TabView tabView = tabLayout.getTabAt(2).view;
                //LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.accessImpinjAuthenticateLayout);
                if (position == impinjTag.m775.ordinal()) {
                    //layout.setVisibility(View.VISIBLE);
                    textViewAuthenticatedResult.setText("");
                    tabView.setVisibility(View.VISIBLE);
                } else {
                    //layout.setVisibility(View.GONE);
                    tabView.setVisibility(View.GONE);
                }

                LinearLayout layoutA = (LinearLayout) getActivity().findViewById(R.id.accessImpinjProtectLayout);
                LinearLayout layoutA1 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjProtectLayout1);
                if (position == impinjTag.m775.ordinal() ||
                        position == impinjTag.m780.ordinal() ||
                        position == impinjTag.m830.ordinal() ||
                        position == impinjTag.m770.ordinal() ||
                        position == impinjTag.m730.ordinal()) {
                    layoutA.setVisibility(View.VISIBLE);
                    layoutA1.setVisibility(View.VISIBLE);
                    textViewProtectValue.setText("");
                } else {
                    layoutA.setVisibility(View.GONE);
                    layoutA1.setVisibility(View.GONE);
                }

                LinearLayout layout0 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjMemorySelectLayout);
                if (position == impinjTag.m830.ordinal() || position == impinjTag.monza_R6P.ordinal()) {
                    layout0.setVisibility(View.VISIBLE);
                    textViewEpc128Value.setText("");
                } else layout0.setVisibility(View.GONE);

                LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjSelectLayout);
                if (position == impinjTag.others.ordinal()) layout1.setVisibility(View.GONE);
                else layout1.setVisibility(View.VISIBLE);

                LinearLayout layout2 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjAutotuneLayout);
                LinearLayout layout3 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjConfigLayout);
                if (position == impinjTag.monza_x8k.ordinal() || position == impinjTag.others.ordinal()) {
                    layout2.setVisibility(View.GONE);
                    layout3.setVisibility(View.GONE);
                } else {
                    layout2.setVisibility(View.VISIBLE);
                    layout3.setVisibility(View.VISIBLE);

                    textViewAutotuneValue.setText("");

                    if (position == impinjTag.monza_R6A.ordinal() || position == impinjTag.monza_R6P.ordinal() || position == impinjTag.others.ordinal()) checkBoxProtect.setVisibility(View.GONE);
                    else checkBoxProtect.setVisibility(View.VISIBLE);
                    if (position == impinjTag.m830.ordinal() || position == impinjTag.monza_R6P.ordinal()) checkBoxMemorySelect.setVisibility(View.VISIBLE);
                    else checkBoxMemorySelect.setVisibility(View.GONE);
                    if (position == impinjTag.m775.ordinal() || position == impinjTag.m780.ordinal() || position == impinjTag.m770.ordinal()) checkBoxUnkillable.setVisibility(View.VISIBLE);
                    else checkBoxUnkillable.setVisibility(View.GONE);

                    checkBoxAutoTuneDisable.setChecked(false); checkBoxAutoTuneDisable.setEnabled(false);
                    checkBoxProtect.setChecked(false); checkBoxProtect.setEnabled(false);
                    checkBoxShortRange.setChecked(false); checkBoxShortRange.setEnabled(false);
                    checkBoxMemorySelect.setChecked(false); checkBoxMemorySelect.setEnabled(false);
                    checkBoxUnkillable.setChecked(false); checkBoxUnkillable.setEnabled(false);
                    textViewConfiguration.setText("");
                }

                LinearLayout layout4 = (LinearLayout) getActivity().findViewById(R.id.accessImpinjReadUserLayout);
                if (position == impinjTag.monza_x8k.ordinal()) layout4.setVisibility(View.VISIBLE);
                else layout4.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        MainActivity.csLibrary4A.setSameCheck(false);
        data2Restore.iQuerySession = MainActivity.csLibrary4A.getQuerySession();
        data2Restore.iQueryTarget = MainActivity.csLibrary4A.getQueryTarget();
        data2Restore.tagDelay = MainActivity.csLibrary4A.getTagDelay();
        data2Restore.dwellTime = MainActivity.csLibrary4A.getAntennaDwell();
        data2Restore.tagFocus = MainActivity.csLibrary4A.getTagFocus();

        selectTag = new SelectTag((Activity)getActivity(), 0);

        if (true) {
            textViewAuthenticatedResult = (TextView) getActivity().findViewById(R.id.accessImpinjAuthenticatedResult);
            button = (Button) getActivity().findViewById(R.id.accessImpinjAuthenticateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    textViewAuthenticatedResult.setText("");
                    boolean invalidRequest = MainActivity.csLibrary4A.setAuthenticateConfiguration();
                    accessTask = new AccessTask(button, null, false,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE,
                            -1, -1, false, false,
                            null, null, null, null, null);
                    accessTask.execute();

                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 1; mHandler.post(updateRunnable);
                }
            });
        }

        if (true) {
            textViewAutotuneValue = (TextView) getActivity().findViewById(R.id.accessImpinjAutoTuneValue);
            buttonAutoTuneValueRead = (Button) getActivity().findViewById(R.id.accessImpinjAutotuneValueButton);
            buttonAutoTuneValueRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    textViewAutotuneValue.setText("");
                    boolean invalidRequest = false;
                    int iAccOffset = 0x1c; int itagSelect = spinnerTagSelect.getSelectedItemPosition();
                    if (itagSelect == impinjTag.m775.ordinal()) iAccOffset = 0x1c;
                    else if (itagSelect == impinjTag.m780.ordinal()) iAccOffset = 0x34;
                    else if (itagSelect == impinjTag.m830.ordinal()) iAccOffset = 0x14;
                    else if (itagSelect == impinjTag.m770.ordinal()) iAccOffset = 0x1c;
                    else if (itagSelect == impinjTag.m730.ordinal()) iAccOffset = 0x14;
                    else if (itagSelect == impinjTag.monza_R6A.ordinal()) iAccOffset = 0x14;
                    else if (itagSelect == impinjTag.monza_R6P.ordinal()) iAccOffset = 0x14;
                    MainActivity.csLibrary4A.appendToLog(String.format("AutoTune offset is 0x%X", iAccOffset));
                    if (set_before_access(0, itagSelect, 1) == false) invalidRequest = true;
                    accessTask = new AccessTask(buttonAutoTuneValueRead, null, invalidRequest,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CREAD,
                            -1, -1, false, checkProtectedBoxBeforeAccess(),
                            null, null, null, null, null);
                    accessTask.execute();
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 2; mHandler.post(updateRunnable);
                }
            });

            textViewProtectValue = (TextView) getActivity().findViewById(R.id.accessImpinjProtectValue);
            checkBoxProtectSelect = (CheckBox) getActivity().findViewById(R.id.accessImpinjProtectSelect);
            textViewProtectNormalValue = (TextView) getActivity().findViewById(R.id.accessImpinjProtectNormalValue);
            buttonProtectValueRead = (Button) getActivity().findViewById(R.id.accessImpinjProtectValueButton);
            buttonProtectValueRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    textViewProtectValue.setText("");
                    boolean invalidRequest = false;
                    if (set_before_access(1, 2, 6) == false) invalidRequest = true;
                    accessTask = new AccessTask(buttonProtectValueRead, null, invalidRequest,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CREAD,
                            -1, -1, false, checkProtectedBoxBeforeAccess(),
                            null, null, null, null, null);
                    MainActivity.csLibrary4A.appendToLog("setSelectCriteria: before execute");
                    accessTask.execute();
                    MainActivity.csLibrary4A.appendToLog("setSelectCriteria: after execute");
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 3; mHandler.post(updateRunnable);
                }
            });

            buttonProtectResumeRead = (Button) getActivity().findViewById(R.id.accessImpinjProtectResumeButton);
            buttonProtectResumeRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isZeroPassword()) return;
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) {
                        if (accessTask != null) accessTask.taskCancelReason = AccessTask.TaskCancelRReason.DESTORY;
                    }

                    if (unprotecting > 0) stopProtectResuming();
                    else {
                        unprotecting = 1;
                        if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal()) selectTag.editTextTagID.setText("E2C011A2");
                        else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal()) selectTag.editTextTagID.setText("E28011C");
                        else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal()) selectTag.editTextTagID.setText("E28011A0");
                        else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal()) selectTag.editTextTagID.setText("E28011B0");
                        else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m730.ordinal()) selectTag.editTextTagID.setText("E280119");
                        selectTag.spinnerSelectBank.setSelection(1);
                        checkBoxProtectSelect.setChecked(true);
                        buttonProtectResumeRead.setText("Stop resuming to normal");

                        startConfigRead();
                    }
                }
            });

            textViewEpc128Value = (TextView) getActivity().findViewById(R.id.accessImpinjEpc128Value);
            buttonEpc128ValueRead = (Button) getActivity().findViewById(R.id.accessImpinjEpc128ValueButton);
            buttonEpc128ValueRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    textViewEpc128Value.setText("");
                    boolean invalidRequest = false;
                    if (set_before_access(1, 2, 8) == false) invalidRequest = true;
                    accessTask = new AccessTask(buttonEpc128ValueRead, null, invalidRequest,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CREAD,
                            -1, -1, false, checkProtectedBoxBeforeAccess(),
                            null, null, null, null, null);
                    accessTask.execute();
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 4; mHandler.post(updateRunnable);
                }
            });

            textViewRunTime = (TextView) getActivity().findViewById(R.id.accessImpinjRunTime);
            buttonReadUserBank = (Button) getActivity().findViewById(R.id.accessRWReadButton);
            buttonReadUserBank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.csLibrary4A.isBleConnected() == false) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                        Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    operationRead = true; startAccessUserTask();
                }
            });

            buttonWriteUserBank = (Button) getActivity().findViewById(R.id.accessRWWriteButton);
            buttonWriteUserBank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.csLibrary4A.isBleConnected() == false) {
                        Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                        Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    operationRead = false; startAccessUserTask();
                }
            });

            textViewConfiguration = (TextView) getActivity().findViewById(R.id.accessImpinjConfiguration);
            buttonRead = (Button) getActivity().findViewById(R.id.accessImpinjReadButton);
            buttonRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    startConfigRead();
                }
            });

            buttonWrite = (Button) getActivity().findViewById(R.id.accessImpinjWriteButton);
            buttonWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isZeroPassword()) return;
                    if (isRfidConnectionValid() == false) return;
                    if (isRunningAccessTask()) return;

                    if (textViewConfiguration.getText().toString().length() < 4) {
                        Toast.makeText(MainActivity.mContext, "Please read configuration first", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startConfigWrite();
                }
            });
        }
    }
    boolean isZeroPassword() {
        boolean bValue = false;
        int iValue = 0;
        try {
            iValue = Integer.parseInt(selectTag.editTextAccessPassword.getText().toString(), 16);
        } catch (Exception ex) {
            iValue = -1;
        }
        MainActivity.csLibrary4A.appendToLog("Password = " + iValue);
        if (iValue == 0) {
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
            customPopupWindow.popupStart("Before operation, please enter non-zero access password !!!", false);
            bValue = true;
        }
        return bValue;
    }
    void stopProtectResuming() {
        unprotecting = 0;
        checkBoxProtectSelect.setChecked(false);
        buttonProtectResumeRead.setText("Resume invisible tag to normal");
    }

    boolean updating = false; long msStartTime; int bankProcessing = 0; int restartAccessBank = -1;
    void startAccessUserTask() {
        msStartTime = SystemClock.elapsedRealtime();
        textViewRunTime.setText("");

        int iSelectBank = selectTag.spinnerSelectBank.getSelectedItemPosition() + 1;
        int iSelectOffset = 32;
        if (iSelectBank != 1) iSelectOffset = 0;

        EditText editTextBlockCount = (EditText) getActivity().findViewById(R.id.accessImpinjBlockCount);
        EditText editTextUserOffset = (EditText) getActivity().findViewById(R.id.accessImpinjUserOffset);
        EditText editTextUserLength = (EditText) getActivity().findViewById(R.id.accessImpinjUserLength);
        int accBlockCount = 32, accOffset = 1, accSize = 1;
        try {
            accBlockCount = Integer.parseInt(editTextBlockCount.getText().toString());
        } catch (Exception ex) { }
        try {
            accOffset = Integer.valueOf(editTextUserOffset.getText().toString(), 10);
        } catch (Exception ex) { }
        try {
            accSize = Integer.valueOf(editTextUserLength.getText().toString(), 10);
        } catch (Exception ex) { }

        textViewUserValue = (TextView) getActivity().findViewById(R.id.accessImpinjUserValue);
        if (operationRead) textViewUserValue.setText("");
        boolean invalidRequest = false;

        MainActivity.csLibrary4A.appendToLog("Start accessTask1 with accBlockCount + " + accBlockCount + "F" + editTextBlockCount.getText().toString() + ", accOffset = " + accOffset + "F" + editTextUserOffset.getText().toString()  + ", accSize = " + accSize + "F" + editTextUserLength.getText().toString());
        accessTask1 = new AccessTask1(
                (operationRead ? buttonReadUserBank : buttonWriteUserBank), invalidRequest,
                3, accOffset, accSize, accBlockCount, null,
                selectTag.editTextTagID.getText().toString(), iSelectBank, iSelectOffset,
                selectTag.editTextAccessPassword.getText().toString(),
                Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()),
                (operationRead ? RfidReaderChipData.HostCommands.CMD_18K6CREAD: RfidReaderChipData.HostCommands.CMD_18K6CWRITE), updateRunnable);
        accessTask1.execute();
        iRunType = 7;
    }
    void startConfigRead() {
        textViewConfiguration.setText("");
        boolean invalidRequest = false;
        if (set_before_access(0, 4, 1) == false) invalidRequest = true;
        int iSelectBank = selectTag.spinnerSelectBank.getSelectedItemPosition() + 1;
        int iSelectOffset = 32;
        if (iSelectBank != 1) iSelectOffset = 0;
        accessTask = new AccessTask(buttonRead, null, invalidRequest,
                selectTag.editTextTagID.getText().toString(), iSelectBank, iSelectOffset,
                selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CREAD,
                -1, -1, false, checkProtectedBoxBeforeAccess(),
                null, null, null, null, null);
        accessTask.execute();
        mHandler.removeCallbacks(updateRunnable);
        iRunType = 5; mHandler.post(updateRunnable);
    }
    void startConfigWrite() {
        boolean invalidRequest = false;
        if (set_before_access(0, 4, 1) == false) invalidRequest = true;
        int iSelectBank = selectTag.spinnerSelectBank.getSelectedItemPosition() + 1;
        int iSelectOffset = 32;
        if (iSelectBank != 1) iSelectOffset = 0;
        if (invalidRequest == false) {
            String string = textViewConfiguration.getText().toString();
            int iValue = Integer.valueOf(string, 16);
            MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%02X", iValue));

            if (checkBoxAutoTuneDisable.isChecked()) iValue |= 0x01;
            else iValue &= ~0x01;

            if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m730.ordinal()) {
                if (checkBoxProtect.isChecked()) iValue |= 0x02;
                else iValue &= ~0x02;
            }

            int iValueModified = 0x02;
            if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal()) iValueModified = 0x04;
            else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal()) iValueModified = 0x04;
            else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal()) iValueModified = 0x04;
            else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal()) iValueModified = 0x10;
            else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m730.ordinal()) iValueModified = 0x10;
            if (checkBoxShortRange.isChecked()) iValue |= iValueModified;
            else iValue &= ~iValueModified;

            iValueModified = 0;
            if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal()) iValueModified = 0x08;
            else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.monza_R6P.ordinal()) iValueModified = 0x04;
            if (iValueModified != 0) {
                if (checkBoxMemorySelect.isChecked()) iValue |= iValueModified;
                else iValue &= ~iValueModified;
            }

            if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal() ||
                    spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal()) {
                if (checkBoxUnkillable.isChecked()) iValue |= 0x08;
                else iValue &= ~0x08;
            }

            MainActivity.csLibrary4A.appendToLog(String.format("revised iValue = 0x%02X", iValue));
            string = String.format("%04X", iValue);
            MainActivity.csLibrary4A.appendToLog("string = " + string);
            MainActivity.csLibrary4A.appendToLog("new AutotuneConfiguration = " + string);
            stringNewAutoTuneConfig = string;
            if (MainActivity.csLibrary4A.setAccessWriteData(string) == false) {
                invalidRequest = true;
            }
        }

        accessTask = new AccessTask(buttonWrite, null, invalidRequest,
                selectTag.editTextTagID.getText().toString(), 1, 32,
                selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), RfidReaderChipData.HostCommands.CMD_18K6CWRITE,
                -1, -1, false, checkProtectedBoxBeforeAccess(),
                null, null, null, null, null);
        accessTask.execute();
        mHandler.removeCallbacks(updateRunnable);
        iRunType = 6; mHandler.post(updateRunnable);
    }
    boolean checkProtectedBoxBeforeAccess() {
        if (checkBoxProtectSelect.isChecked()) {
            MainActivity.csLibrary4A.appendToLog("Going to setSelectCriteria disable");
            MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
            MainActivity.csLibrary4A.appendToLog("Going to setSelectCriteria");
            MainActivity.csLibrary4A.setSelectCriteria(-1, true, 4, 0, 3, 0, selectTag.editTextAccessPassword.getText().toString(), false);
        }
        return checkBoxProtectSelect.isChecked();
    }
    boolean isRfidConnectionValid() {
        boolean bValue = false;
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
        } else if (MainActivity.csLibrary4A.isRfidFailure()) {
            Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
        } else bValue = true;
        return bValue;
    }
    boolean set_before_access(int accBank, int accOffset, int accSize) {
        boolean invalidRequest = false;
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                MainActivity.csLibrary4A.appendToLog("HelloK: accBank, invalidRequest=" + invalidRequest);
                invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                MainActivity.csLibrary4A.appendToLog("HelloK: accOffset, invalidRequest=" + invalidRequest);
                invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest = true;
            }
        }
        if (invalidRequest) return false;
        return true;
    }

    boolean isRunningAccessTask() {
        boolean retValue = false;
        if (accessTask != null) {
            if (accessTask.getStatus() != AsyncTask.Status.FINISHED) retValue = true;
        }
        return retValue;
    }
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (accessTask == null && accessTask1 == null) {
                MainActivity.csLibrary4A.appendToLog("updateRunnable(): null AccessTask");
            } else if (accessTask != null && accessTask.getStatus() == AsyncTask.Status.FINISHED) {
                MainActivity.csLibrary4A.appendToLog("accessResult = " + accessTask.accessResult + " with iRunType = " + iRunType);
                if (accessTask.accessResult == null) {
                     MainActivity.csLibrary4A.appendToLog("updateRunnable(): accessTask is finished without result but with error = " + accessTask.resultError);
                     if (unprotecting > 0) stopProtectResuming();
                } else if (iRunType == 1) textViewAuthenticatedResult.setText(accessTask.accessResult);
                else if (iRunType == 2) textViewAutotuneValue.setText(accessTask.accessResult);
                else if (iRunType == 3) textViewProtectValue.setText(accessTask.accessResult);
                else if (iRunType == 4) textViewEpc128Value.setText(accessTask.accessResult);
                else if (iRunType == 5) {
                    textViewConfiguration.setText(accessTask.accessResult);
                    int iValue = Integer.valueOf(accessTask.accessResult.substring(accessTask.accessResult.length()-2, accessTask.accessResult.length()), 16);
                    MainActivity.csLibrary4A.appendToLog("updateRunnable(): " + String.format("accessResult = %s, iValue = 0x%02X", accessTask.accessResult, iValue));

                    if ((iValue & 0x01) != 0) checkBoxAutoTuneDisable.setChecked(true); else checkBoxAutoTuneDisable.setChecked(false);
                    checkBoxAutoTuneDisable.setEnabled(true);

                    if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m730.ordinal()) {
                        if ((iValue & 0x02) != 0) checkBoxProtect.setChecked(true); else checkBoxProtect.setChecked(false);
                        checkBoxProtect.setEnabled(true);
                    }

                    int iBitCompared = 0x02;
                    if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal()) iBitCompared = 0x04;
                    else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal()) iBitCompared = 0x04;
                    else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal()) iBitCompared = 0x04;
                    else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal()) iBitCompared = 0x10;
                    else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m730.ordinal()) iBitCompared = 0x10;
                    if ((iValue & iBitCompared) != 0) checkBoxShortRange.setChecked(true); else checkBoxShortRange.setChecked(false);
                    checkBoxShortRange.setEnabled(true);

                    iBitCompared = 0;
                    if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m830.ordinal()) iBitCompared = 0x08;
                    else if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.monza_R6P.ordinal()) iBitCompared = 0x04;
                    if (iBitCompared != 0) {
                        if ((iValue & iBitCompared) != 0) checkBoxMemorySelect.setChecked(true); else checkBoxMemorySelect.setChecked(false);
                        checkBoxMemorySelect.setEnabled(true);
                    }

                     if (spinnerTagSelect.getSelectedItemPosition() == impinjTag.m775.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m780.ordinal() ||
                            spinnerTagSelect.getSelectedItemPosition() == impinjTag.m770.ordinal()) {
                        if ((iValue & 0x08) != 0) checkBoxUnkillable.setChecked(true);
                        else checkBoxUnkillable.setChecked(false);
                        checkBoxUnkillable.setEnabled(true);
                     }

                     if (unprotecting > 0) {
                         textViewProtectValue.setText(accessTask.accessTagEpc);
                         textViewProtectNormalValue.setText("" + unprotecting);
                         unprotecting++;
                         if (checkBoxProtect.isChecked()) {
                             selectTag.editTextTagID.setText(accessTask.accessTagEpc);
                             selectTag.spinnerSelectBank.setSelection(0);
                             checkBoxProtect.setChecked(false);
                             MainActivity.csLibrary4A.appendToLog("updateRunnable(): startConfigWrite");
                             startConfigWrite();
                         } else {
                             MainActivity.csLibrary4A.appendToLog("updateRunnable(): startConfigRead");
                             startConfigRead();
                         }
                     }
                }
                else if (iRunType == 6) {
                    MainActivity.csLibrary4A.appendToLog("updateRunnable(): accessResult = " + accessTask.accessResult + ", accessError = " + accessTask.resultError);
                    if (accessTask.resultError.trim().length() != 0) Toast.makeText(MainActivity.mContext, accessTask.resultError, Toast.LENGTH_SHORT).show();
                    else if (accessTask.accessResult.length() == 0) textViewConfiguration.setText(stringNewAutoTuneConfig);

                    if (unprotecting > 0) stopProtectResuming();
                }
                else if (iRunType == 7) textViewUserValue.setText(accessTask.accessResult);
                else MainActivity.csLibrary4A.appendToLog("updateRunnable(): No procedure for iRunType == " + iRunType);
            } else if (accessTask1 != null && accessTask1.isResultReady()) {
                long duration = SystemClock.elapsedRealtime() - msStartTime;
                textViewRunTime.setText(String.format("Run time: %.2f sec", ((float) duration / 1000))); MainActivity.csLibrary4A.appendToLog("StreamOut: End of running time");
                MainActivity.csLibrary4A.appendToLog("access1Result = " + accessTask1.getResult() + " with iRunType = " + iRunType);
                if (iRunType == 7) textViewUserValue.setText(accessTask1.getResult());
                else MainActivity.csLibrary4A.appendToLog("updateRunnable(): No procedure for iRunType == " + iRunType);
            }else {
//                MainActivity.csLibrary4A.appendToLog("updateRunnable(): rerun after 100ms with accessTask.getStatus() = " + accessTask.getStatus().toString());
                mHandler.postDelayed(updateRunnable, 100);
            }
        }
    };
    class Data2Restore {
        int iQuerySession;
        int iQueryTarget;
        byte tagDelay;
        long dwellTime;
        int tagFocus;
    }
    Data2Restore data2Restore = new Data2Restore();

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.abortOperation();
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), data2Restore.iQuerySession, data2Restore.iQueryTarget);
        MainActivity.csLibrary4A.setTagDelay((byte)data2Restore.tagDelay);
        MainActivity.csLibrary4A.setAntennaDwell(data2Restore.dwellTime);
        MainActivity.csLibrary4A.setTagFocus(data2Restore.tagFocus > 0 ? true : false);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessImpinjFragment is now VISIBLE");
           setupTagID();
        } else {
            int iValue = 0;
            if (checkBoxTagFocus != null && checkBoxFastId != null) {
                if (checkBoxTagFocus.isChecked()) {
                    MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), 1, 0);
                    MainActivity.csLibrary4A.setTagDelay((byte)0);
                    MainActivity.csLibrary4A.setAntennaDwell(2000);
                    iValue |= 0x10;
                } else MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), 0, 2);
                if (checkBoxFastId.isChecked()) iValue |= 0x20;
                if (spinnerTagSelect.getSelectedItemPosition() != impinjTag.others.ordinal()) iValue |= (spinnerTagSelect.getSelectedItemPosition() + 1);
                MainActivity.tagType = TAG_IMPINJ; /* need more tagType */ MainActivity.mDid = "E28011" + String.format("%02X", iValue);
                MainActivity.csLibrary4A.appendToLog(String.format("HelloK: iValue = 0x%02X, mDid = %s", iValue, MainActivity.mDid));
                MainActivity.csLibrary4A.setImpinJExtension(checkBoxTagFocus.isChecked(), checkBoxFastId.isChecked());
            }
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessImpinjFragment is now INVISIBLE" + (checkBoxFastId != null ? (" with Value = " + iValue) : ""));
        }
    }

    public AccessImpinjFragment(boolean b775) {
        super("AccessImpinjFragment");
    }

    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                bSelected = true;
                if (selectTag != null) {
                    selectTag.editTextTagID.setText(tagSelected.getAddress());
                    selectTag.spinnerSelectBank.setSelection(0);
                }

                String stringDetail = tagSelected.getDetails();
                int indexUser = stringDetail.indexOf("USER=");
                if (indexUser != -1) {
                    String stringUser = stringDetail.substring(indexUser + 5);
                    MainActivity.csLibrary4A.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
//                    CheckBox checkBoxBAP = (CheckBox) getActivity().findViewById(R.id.coldChainEnableBAP);
//                    checkBoxBAP.setChecked(bEnableBAPMode);
                }
            }
        }
    }
}