package com.csl.cs108ademoapp.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.InventoryBarcodeTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Connector;

public class AccessRegisterFragment extends CommonFragment {
    Spinner spinnerSelectBank;
    EditText editTextSelectMask, editTextBarValue;
    Button buttonReadBar, buttonWrite;
    EditText editTextPassword, editTextWriteLength, editTextAntennaPower;
    CheckBox checkBoxAutoRun, checkBoxNewValue, checkBoxNewBarcode;
    TextView textViewWriteCount, textViewRunTime, textViewTagGot, textViewVoltageLevel;
    TextView textViewYield, textViewTotal;
    InventoryBarcodeTask inventoryBarcodeTask;
    AccessTask accessTask;
    boolean newBarcode;
    String barValueOld = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_access_register, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_access);
        actionBar.setTitle(R.string.title_activity_registertag);

        spinnerSelectBank = (Spinner) getActivity().findViewById(R.id.registerSelectBank);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectBank.setAdapter(targetAdapter);

        Button buttonConfirm = (Button) getActivity().findViewById(R.id.regtagConfirm2Button);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.mContext, "Step 2 data is confirmed.", Toast.LENGTH_SHORT).show();
            }
        });

        editTextSelectMask = (EditText) getActivity().findViewById(R.id.registerSelectMask);
//        editTextSelectMask.setText("19dec16");

        checkBoxNewValue = (CheckBox) getActivity().findViewById(R.id.registerNewValue);
        checkBoxNewValue.setVisibility(View.GONE);
        checkBoxNewValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                resetOldValue();
            }
        });

        editTextBarValue = (EditText) getActivity().findViewById(R.id.registerBarValue);
        editTextBarValue.setEnabled(true);
//        editTextBarValue.setText("19dec163");
        newBarcode = false; editTextBarValue.setTextColor(Color.RED);
        editTextBarValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String barValue = editTextBarValue.getText().toString().trim();
                if (true) newBarcode = true;
                if (barValue.matches(barValueOld) == false) {
                    barValueOld = barValue;
                    editTextBarValue.setTextColor(Color.BLACK);
                    checkBoxNewValue.setEnabled(true);
                    checkBoxNewValue.setText("Reset to old value");
                    newBarcode = true;
                }

                if (inventoryBarcodeTask != null) {
                    if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) //if (runningAuto123 && newBarcode)
                    startStopBarcodeHandler(false);
                }
                if (true) {
                    int length1 = barValue.length() * 4;
                    int length = length1 / 16;
                    if (length * 16 != length1) length++;
                    editTextWriteLength.setText(String.valueOf(length));
                }
            }
        });
        checkBoxNewBarcode = (CheckBox) getActivity().findViewById(R.id.registerNewBarcode);

        buttonReadBar = (Button) getActivity().findViewById(R.id.registerReadBarButton);
        buttonReadBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopBarcodeHandler(false);
            }
        });

        editTextPassword = (EditText) getActivity().findViewById(R.id.registerPassword);
        editTextPassword.setText("00000000");

        editTextWriteLength = (EditText) getActivity().findViewById(R.id.registerWriteLength);
        editTextWriteLength.setText("2");

        editTextAntennaPower = (EditText) getActivity().findViewById(R.id.registerAntennaPower);
        editTextAntennaPower.setText(String.valueOf(300));

        checkBoxAutoRun = (CheckBox) getActivity().findViewById(R.id.registerAutoRun);

        //textViewWriteCount = (TextView) getActivity().findViewById(R.id.registerWrittenCount);
        textViewRunTime = (TextView) getActivity().findViewById(R.id.registerRunTime);
        textViewTagGot = (TextView) getActivity().findViewById(R.id.registetTagGotView);
        textViewVoltageLevel = (TextView) getActivity().findViewById(R.id.registerVoltageLevel);

        Button buttonWrite3 = (Button) getActivity().findViewById(R.id.regtagWrite3Button);
        buttonWrite3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                checkBoxAutoRun.setChecked(false); checkBoxNewBarcode.setChecked(false);
                runningAuto123 = false;
                startStopAccessHandler(false);
            }
        });

        Button buttonAuto123 = (Button) getActivity().findViewById(R.id.regtagAuto123Button);
        buttonAuto123.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                checkBoxAutoRun.setChecked(true); checkBoxNewBarcode.setChecked(true);
                runningAuto123 = true; mHandler.post(runnableAuto123);
            }
        });

        Button buttonAuto23 = (Button) getActivity().findViewById(R.id.regtagAuto23Button);
        buttonAuto23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                checkBoxAutoRun.setChecked(true); checkBoxNewBarcode.setChecked(false);
                startStopAccessHandler(false);
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.regtagWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonWrite.getText().toString().trim().length() == 0) return;
                checkBoxAutoRun.setChecked(false); checkBoxNewBarcode.setChecked(false);

                runningAuto123 = false;
                startStopAccessHandler(false);
            }
        });
        textViewYield = (TextView) getActivity().findViewById(R.id.registerYieldView);
        textViewTotal = (TextView) getActivity().findViewById(R.id.registerTotalView);

        MainActivity.mCs108Library4a.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNotificationListener();
    }

    @Override
    public void onPause() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        if (accessTask != null) {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AcccessRegisterFragment().onDestory(): VALID inventoryRfidTask");
            accessTask.taskCancelReason = AccessTask.TaskCancelRReason.DESTORY;
        }
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AcccessRegisterFragment().onDestory(): onDestory()");
        MainActivity.mCs108Library4a.setSameCheck(true);
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    public AccessRegisterFragment() {
        super("AccessRegisterFragment");
    }

    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.mCs108Library4a.appendToLog("TRIGGER key is pressed.");
                startStopAccessHandler(true);
            }
        });
    }

    boolean runningAuto123 = false;
    Runnable runnableAuto123 = new Runnable() {
        @Override
        public void run() {
            boolean running = false;
            if (inventoryBarcodeTask != null) { if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) running = true; }
            if (accessTask != null) { if (accessTask.getStatus() == AsyncTask.Status.RUNNING) running = true; }
            if (running == false && runningAuto123) { startStopAccessHandler(false); running = true; }
            if (running) mHandler.postDelayed(runnableAuto123, 250);
        }
    };

    void startStopBarcodeHandler(boolean buttonTrigger) {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID access", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) return;
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean noToast = true; //runningAuto123;
            inventoryBarcodeTask = new InventoryBarcodeTask(null, null, editTextBarValue, null, null, null, buttonReadBar, buttonWrite, null, noToast);
            inventoryBarcodeTask.execute();
        } else inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
    }

    void startStopAccessHandler(boolean buttonTrigger) {
        if (inventoryBarcodeTask != null) { if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) { startStopBarcodeHandler(buttonTrigger); return; } }

        boolean started = false;
        if (accessTask != null) { if (accessTask.getStatus() == AsyncTask.Status.RUNNING) started = true; }
        if (buttonTrigger && ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) return;
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                return;
            } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean startBarcode = false;
            if (newBarcode == false && checkBoxNewBarcode.isChecked()) startBarcode = true;
            if (startBarcode) startStopBarcodeHandler(buttonTrigger);
            else {
                startAccessTask();
                resetOldValue();
            }
        } else {
            if (buttonTrigger) accessTask.taskCancelReason = AccessTask.TaskCancelRReason.BUTTON_RELEASE;
            else accessTask.taskCancelReason = AccessTask.TaskCancelRReason.STOP;
        }
    }

    void resetOldValue() {
        editTextBarValue.setTextColor(Color.RED);
        checkBoxNewValue.setEnabled(false);
        checkBoxNewValue.setText("Old value");
        checkBoxNewValue.setChecked(false);
        newBarcode = false;
    }

    boolean resetCount = true;
    void startAccessTask() {
        boolean invalidRequest1 = false;

        int selectBank = spinnerSelectBank.getSelectedItemPosition(); MainActivity.mCs108Library4a.appendToLog("selectBank = " + selectBank);

        EditText editTextSelectOffset = (EditText) getActivity().findViewById(R.id.registerSelectOffset);
        int selectOffset = 0;
        try {
            selectOffset = Integer.parseInt(editTextSelectOffset.getText().toString());
        } catch (Exception ex) { }
        MainActivity.mCs108Library4a.appendToLog("selectOffset = " + selectOffset);

        EditText editTextSelectPopulation = (EditText) getActivity().findViewById(R.id.registerSelectPopulation);
        int selectQValue = 0;
        try {
            selectQValue = Integer.parseInt(editTextSelectPopulation.getText().toString());
        } catch (Exception ex) { }
        selectQValue = MainActivity.mCs108Library4a.getPopulation2Q(selectQValue);
        MainActivity.mCs108Library4a.appendToLog("selectQValue = " + selectQValue);

        String selectMask = editTextSelectMask.getText().toString(); MainActivity.mCs108Library4a.appendToLog("selectMask = " + selectMask);

//        editTextBarValue.setText("19dec163");
        String barValue = editTextBarValue.getText().toString(); MainActivity.mCs108Library4a.appendToLog("barValue = " + barValue);

        String password = editTextPassword.getText().toString(); MainActivity.mCs108Library4a.appendToLog("password = " + password);

        EditText editTextWriteOffset = (EditText) getActivity().findViewById(R.id.registerWriteOffset);
        int writeOffset = 0;
        try {
            writeOffset = Integer.parseInt(editTextWriteOffset.getText().toString());
        } catch (Exception ex) { }
        MainActivity.mCs108Library4a.appendToLog("writeOffset = " + writeOffset);

        int writeLength = 0;
        try {
            writeLength = Integer.parseInt(editTextWriteLength.getText().toString());
        } catch (Exception ex) { }
        MainActivity.mCs108Library4a.appendToLog("writeLength = " + writeLength);

        int antennaPower = 0;
        try {
            antennaPower = Integer.parseInt(editTextAntennaPower.getText().toString());
        } catch (Exception ex) { }
        MainActivity.mCs108Library4a.appendToLog("antennaPower = " + antennaPower);

        boolean resetCount = true;
        if (this.resetCount == false) {
            if (checkBoxAutoRun.isChecked() && checkBoxNewBarcode.isChecked()) resetCount = false;
            else resetCount = newBarcode;
        }
        this.resetCount = false;
        boolean isNewBarcode = checkBoxNewBarcode.isChecked(); MainActivity.mCs108Library4a.appendToLog("isNewBarcode = " + isNewBarcode);

        if (invalidRequest1 == false) {
            if (MainActivity.mCs108Library4a.setAccessBank(1) == false) {
                invalidRequest1 = true; MainActivity.mCs108Library4a.appendToLog("setAccessBank");
            }
        }
        if (invalidRequest1 == false) {
            if (MainActivity.mCs108Library4a.setAccessOffset(writeOffset + 2) == false) {
                invalidRequest1 = true; MainActivity.mCs108Library4a.appendToLog("setAccessOffset");
            }
        }
        if (invalidRequest1 == false) {
            if (writeLength == 0) {
                invalidRequest1 = true; MainActivity.mCs108Library4a.appendToLog("writeLength");
            } else if (MainActivity.mCs108Library4a.setAccessCount(writeLength) == false) {
                invalidRequest1 = true; MainActivity.mCs108Library4a.appendToLog("setAccessCount");
            }
        }
        if (invalidRequest1 == false) {
            if (MainActivity.mCs108Library4a.setAccessWriteData(barValue) == false) {
                invalidRequest1 = true; MainActivity.mCs108Library4a.appendToLog("setAccessWriteData(" + barValue + ")");
            }
        }
        int selectOffset1 = selectBank == 0 ? selectOffset + 32 : selectOffset;
        MainActivity.mCs108Library4a.appendToLog("selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", selectOffset1= " + selectOffset1 + ", invalidRequest1 = " + invalidRequest1);

        accessTask = new AccessTask(buttonWrite, textViewWriteCount, invalidRequest1,
                "", selectMask, selectBank + 1, (selectBank == 0 ? selectOffset + 32 : selectOffset),
                password, antennaPower, Cs108Connector.HostCommands.CMD_18K6CWRITE, selectQValue, checkBoxAutoRun.isChecked(), checkBoxNewBarcode.isChecked(), resetCount,
                textViewRunTime, textViewTagGot, textViewVoltageLevel,
                textViewYield, textViewTotal);
        accessTask.execute();
    }
}
