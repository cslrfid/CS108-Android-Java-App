package com.csl.cs108ademoapp.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.AccessTask1;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

public class AccessImpinjFragment extends CommonFragment {
    CheckBox checkBoxTagFocus, checkBoxFastId, checkBoxTagSecurity, checkBoxTagAutotune, checkBoxShortRange, checkBoxAutoTuneDisable;
    SelectTag selectTag;
    TextView textViewAuthenticatedResult, textViewAutotuneValue, textViewAutotuneConfiguration;
    Button button, buttonValueRead, buttonRead, buttonWrite;
    AccessTask accessTask;
    AccessTask1 accessTask1;
    int iRunType = -1; String stringNewAutoTuneConfig = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_impinj, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxTagFocus = (CheckBox) getActivity().findViewById(R.id.accessImpinjTagFocus);
        if (MainActivity.csLibrary4A.get98XX() != 0) checkBoxTagFocus.setText(checkBoxTagFocus.getText().toString() + " (When enabled, tag select is disabled.)");
        checkBoxFastId = (CheckBox) getActivity().findViewById(R.id.accessImpinjFastId);
        checkBoxTagSecurity = (CheckBox) getActivity().findViewById(R.id.accessImpinjTagSecurity);
        checkBoxTagAutotune = (CheckBox) getActivity().findViewById(R.id.accessImpinjTagAutotune);
        checkBoxShortRange = (CheckBox) getActivity().findViewById(R.id.accessImpinjAutoTuneShortRange);
        checkBoxAutoTuneDisable = (CheckBox) getActivity().findViewById(R.id.accessImpinjAutoTuneDisable);
        MainActivity.csLibrary4A.appendToLog("CheckBoxTagFocus is set");

        MainActivity.csLibrary4A.setSameCheck(false);
        data2Restore.iQuerySession = MainActivity.csLibrary4A.getQuerySession();
        data2Restore.iQueryTarget = MainActivity.csLibrary4A.getQueryTarget();
        data2Restore.tagDelay = MainActivity.csLibrary4A.getTagDelay();
        data2Restore.dwellTime = MainActivity.csLibrary4A.getAntennaDwell();
        data2Restore.tagFocus = MainActivity.csLibrary4A.getTagFocus();

        selectTag = new SelectTag((Activity)getActivity ());

        if (true) {
            textViewAuthenticatedResult = (TextView) getActivity().findViewById(R.id.accessImpinjAuthenticatedResult);
            button = (Button) getActivity().findViewById(R.id.accessImpinjAuthenticateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    textViewAuthenticatedResult.setText("");

                    boolean invalidRequest = MainActivity.csLibrary4A.setAuthenticateConfiguration();
                    accessTask = new AccessTask(button, null, false,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), Cs108Library4A.HostCommands.CMD_18K6CAUTHENTICATE,
                            -1, -1, false,
                            null, null, null, null, null);
                    accessTask.execute();

                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 1; mHandler.post(updateRunnable);
                }
            });
        }

        if (true) {
            textViewAutotuneValue = (TextView) getActivity().findViewById(R.id.accessImpinjAutoTuneValue);
            buttonValueRead = (Button) getActivity().findViewById(R.id.accessImpinjAutotuneValueButton);
            buttonValueRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    textViewAutotuneValue.setText("");

                    boolean invalidRequest = false;
                    if (set_before_access(0, (checkBoxTagSecurity.isChecked() ? 0x1c : 0x14), 1) == false) invalidRequest = true;
                    accessTask = new AccessTask(buttonValueRead, null, invalidRequest,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), Cs108Library4A.HostCommands.CMD_18K6CREAD,
                            -1, -1, false,
                            null, null, null, null, null);
                    accessTask.execute();
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 2; mHandler.post(updateRunnable);
                }
            });

            textViewAutotuneConfiguration = (TextView) getActivity().findViewById(R.id.accessImpinjAutoTuneConfiguration);
            buttonRead = (Button) getActivity().findViewById(R.id.accessImpinjReadButton);
            buttonRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    textViewAutotuneConfiguration.setText("");

                    boolean invalidRequest = false;
                    if (set_before_access(0, 4, 1) == false) invalidRequest = true;
                    accessTask = new AccessTask(buttonRead, null, invalidRequest,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), Cs108Library4A.HostCommands.CMD_18K6CREAD,
                            -1, -1, false,
                            null, null, null, null, null);
                    accessTask.execute();
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 3; mHandler.post(updateRunnable);
                }
            });
            buttonWrite = (Button) getActivity().findViewById(R.id.accessImpinjWriteButton);
            buttonWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRfidConnectionValid() == false) return;
                    else if (textViewAutotuneConfiguration.getText().toString().length() < 4) {
                        Toast.makeText(MainActivity.mContext, "Please read configuration first", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean invalidRequest = false;
                    if (set_before_access(0, 4, 1) == false) invalidRequest = true;
                    if (invalidRequest == false) {
                        String string = textViewAutotuneConfiguration.getText().toString();
                        int iValue = Integer.valueOf(string, 16);
                        MainActivity.csLibrary4A.appendToLog(String.format("iValue = 0x%02X", iValue));
                        int iValueModified = 0x02; if (checkBoxTagSecurity.isChecked()) iValueModified = 0x04;
                        if (checkBoxShortRange.isChecked()) iValue |= iValueModified;
                        else iValue &= ~iValueModified;
                        if (checkBoxAutoTuneDisable.isChecked()) iValue |= 0x01;
                        else iValue &= ~0x01;
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
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), Cs108Library4A.HostCommands.CMD_18K6CWRITE,
                            -1, -1, false,
                            null, null, null, null, null);
                    accessTask.execute();
                    mHandler.removeCallbacks(updateRunnable);
                    iRunType = 4; mHandler.post(updateRunnable);
                }
            });
        }
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

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (accessTask == null && accessTask1 == null) {
                MainActivity.csLibrary4A.appendToLog("updateRunnable(): null AccessTask");
            } else if (accessTask.getStatus() == AsyncTask.Status.FINISHED && accessTask != null && accessTask.accessResult != null) {
                if (iRunType == 1) textViewAuthenticatedResult.setText(accessTask.accessResult);
                else if (iRunType == 2) textViewAutotuneValue.setText(accessTask.accessResult);
                else if (iRunType == 3) {
                    textViewAutotuneConfiguration.setText(accessTask.accessResult);
                    textViewAutotuneValue = (TextView) getActivity().findViewById(R.id.accessImpinjAutoTuneValue);
                    int iValue = Integer.valueOf(accessTask.accessResult.substring(accessTask.accessResult.length()-1, accessTask.accessResult.length()), 16);
                    MainActivity.csLibrary4A.appendToLog(String.format("accessResult = %s, iValue = 0x%02X", accessTask.accessResult, iValue));
                    int iBitCompared = 0x02;
                    if (checkBoxTagSecurity.isChecked()) iBitCompared = 0x04;
                    if ((iValue & iBitCompared) != 0) checkBoxShortRange.setChecked(true); else checkBoxShortRange.setChecked(false);
                    checkBoxShortRange.setEnabled(true);
                    if ((iValue & 0x01) != 0) checkBoxAutoTuneDisable.setChecked(true); else checkBoxAutoTuneDisable.setChecked(false);
                    checkBoxAutoTuneDisable.setEnabled(true);
                }
                else if (iRunType == 4) {
                    MainActivity.csLibrary4A.appendToLog("accessResult = " + accessTask.accessResult + ", accessError = " + accessTask.resultError);
                    if (accessTask.resultError.trim().length() != 0) Toast.makeText(MainActivity.mContext, accessTask.resultError, Toast.LENGTH_SHORT).show();
                    else if (accessTask.accessResult.length() == 0) textViewAutotuneConfiguration.setText(stringNewAutoTuneConfig);
                }
            } else {
                MainActivity.csLibrary4A.appendToLog("updateRunnable(): rerun after 100ms");
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
                if (checkBoxTagSecurity.isChecked()) iValue |= 0x40;
                MainActivity.csLibrary4A.appendToLog("autoTune is " + (checkBoxTagAutotune.isChecked() ? "checked" : "open"));
                if (checkBoxTagAutotune.isChecked()) iValue |= 0x80;
                MainActivity.csLibrary4A.appendToLog(String.format("HelloK: iValue = 0x%02X", iValue));
                MainActivity.mDid = "E28011" + String.format("%02X", iValue);
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
                if (selectTag != null) selectTag.editTextTagID.setText(tagSelected.getAddress());

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