package com.csl.cs108ademoapp.fragments;

import static com.csl.cs108ademoapp.MainActivity.mContext;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.InventoryBarcodeTask;
import com.csl.cs108ademoapp.InventoryRfidTask;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import java.util.ArrayList;

public class AccessRegisterFragment extends CommonFragment {
    CustomPopupWindow customPopupWindow;

    TableRow tableRowSelectMask, tableRowSelectBank;
    Spinner spinnerSelectBank, spinnerAccessBank, spinnerWriteDataType;
    EditText editTextSelectMask, editTextSelectPopulation, editTextPassword, editTextAntennaPower, editTextWriteData, editTextWriteLength;
    CheckBox checkBoxWriteLengthEnable;
    TextView textViewSelectedTags, textViewWriteCount, textViewRunTime, textViewTagGot, textViewVoltageLevel;
    TextView textViewYield, textViewTotal;
    Button buttonSelect, buttonClearSelect, buttonReadBar, buttonWrite;

    InventoryRfidTask inventoryRfidTask;
    InventoryBarcodeTask inventoryBarcodeTask;
    AccessTask accessTask;

    ReaderDevice tagSelected = MainActivity.tagSelected;
    boolean newWriteData;
    String writeValueOld = "";
    int iAutoRun = 0;

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

        customPopupWindow = new CustomPopupWindow(mContext);

        tableRowSelectMask = (TableRow) getActivity().findViewById(R.id.registerSelectMaskRow);
        tableRowSelectBank = (TableRow) getActivity().findViewById(R.id.registerSelectBankRow);

        spinnerSelectBank = (Spinner) getActivity().findViewById(R.id.registerSelectBank);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectBank.setAdapter(targetAdapter);
        spinnerSelectBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //if EPC
                        if (tagSelected != null) editTextSelectMask.setText(tagSelected.getAddress());
                        break;
                    case 1:
                        if (tagSelected != null) { if (tagSelected.getTid() != null) editTextSelectMask.setText(tagSelected.getTid()); }
                        break;
                    case 2:
                        if (tagSelected != null) { if (tagSelected.getUser() != null) editTextSelectMask.setText(tagSelected.getUser()); }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAccessBank = (Spinner) getActivity().findViewById(R.id.registerAccessBank);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.write_memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessBank.setAdapter(targetAdapter);

        spinnerWriteDataType = (Spinner) getActivity().findViewById(R.id.registerWriteDataType);
        targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.write_data_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWriteDataType.setAdapter(targetAdapter);

        Button buttonConfirm = (Button) getActivity().findViewById(R.id.registerConfirm2Button);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.mContext, "Step 2 data is confirmed.", Toast.LENGTH_SHORT).show();
            }
        });

        editTextSelectMask = (EditText) getActivity().findViewById(R.id.registerSelectMask);
        editTextSelectPopulation = (EditText) getActivity().findViewById(R.id.registerSelectPopulation);
        editTextPassword = (EditText) getActivity().findViewById(R.id.registerPassword);
        editTextPassword.setText("00000000");
        editTextAntennaPower = (EditText) getActivity().findViewById(R.id.registerAntennaPower);
        editTextWriteData = (EditText) getActivity().findViewById(R.id.registerWriteData);
        editTextWriteData.setEnabled(true);
//        editTextBarValue.setText("19dec163");
        newWriteData = false; editTextWriteData.setTextColor(Color.RED);
        editTextWriteData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String writeValue = editTextWriteData.getText().toString().trim();
                if (true) newWriteData = true;
                if (writeValue.matches(writeValueOld) == false) {
                    writeValueOld = writeValue;
                    editTextWriteData.setTextColor(Color.BLACK);
                    //checkBoxNewValue.setEnabled(true);
                    //checkBoxNewValue.setText("Reset to old value");
                    newWriteData = true;
                }

                if (inventoryBarcodeTask != null) {
                    if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) {
                        barcodeReadDone = true; MainActivity.csLibrary4A.appendToLog("barcodeReadDone = true in textChanged");
                        MainActivity.csLibrary4A.appendToLog("going to startStopBarcodeHandler 1"); startStopBarcodeHandler(false);
                    }
                }
                if (checkBoxWriteLengthEnable.isChecked() == false) {
                    int length1 = writeValue.length() * 4;
                    int length = length1 / 16;
                    if (length * 16 != length1) length++;
                    editTextWriteLength.setText(String.valueOf(length));
                }
            }
        });
        editTextWriteLength = (EditText) getActivity().findViewById(R.id.registerWriteLength);

        checkBoxWriteLengthEnable = (CheckBox) getActivity().findViewById(R.id.registerWriteLengthEnable);
        checkBoxWriteLengthEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) editTextWriteLength.setEnabled(true);
                else editTextWriteLength.setEnabled(false);
            }
        });

        buttonClearSelect = (Button) getActivity().findViewById(R.id.registerClearSelectButton);
        buttonClearSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagSelected = null;
                editTextSelectMask.setText("");
            }
        });

        buttonSelect = (Button) getActivity().findViewById(R.id.registerSelectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonSelect.getText().toString().contains("Clear")) {
                    buttonClearSelect.setVisibility(View.VISIBLE);
                    tableRowSelectMask.setVisibility(View.VISIBLE);
                    tableRowSelectBank.setVisibility(View.VISIBLE);
                    textViewSelectedTags.setText("");
                    buttonSelect.setText("Read");
                } else if (buttonSelect.getText().toString().contains("Stop")) {
                    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
                    mHandler.removeCallbacks(runnableSelect);
                    textViewSelectedTags.setText("");
                    for (int i = 0; i < epcArrayList.size(); i++) {
                        MainActivity.csLibrary4A.appendToLog("epcArrayList.get[" + i + "] = " + epcArrayList.get(i));
                        textViewSelectedTags.append(epcArrayList.get(i) + "\n");
                    }
                    if (textViewSelectedTags.getText().toString().trim().length() == 0)  {
                        buttonSelect.setText("Read");
                    } else {
                        buttonClearSelect.setVisibility(View.GONE);
                        tableRowSelectMask.setVisibility(View.GONE);
                        tableRowSelectBank.setVisibility(View.GONE);
                        buttonSelect.setText("Clear");
                    }
                }
                else {
                    textViewSelectedTags.setText("");
                    String strTagId = editTextSelectMask.getText().toString();
                    int selectBank = spinnerSelectBank.getSelectedItemPosition() + 1;
                    long pwrlevel = Integer.parseInt(editTextAntennaPower.getText().toString());
                    MainActivity.csLibrary4A.setTagRead(0);
                    MainActivity.csLibrary4A.setSelectedTag(strTagId, selectBank, pwrlevel);
                    MainActivity.csLibrary4A.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY);
                    inventoryRfidTask = new InventoryRfidTask();
                    inventoryRfidTask.execute();
                    MainActivity.sharedObjects.serviceArrayList.clear(); epcArrayList.clear();
                    mHandler.post(runnableSelect); buttonSelect.setText("Stop");
                }
            }
        });

        buttonReadBar = (Button) getActivity().findViewById(R.id.registerReadBarButton);
        buttonReadBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.csLibrary4A.appendToLog("going to startStopBarcodeHandler 2"); startStopBarcodeHandler(false);
            }
        });

        textViewSelectedTags = (TextView) getActivity().findViewById(R.id.registerSelectedTags);
        //textViewWriteCount = (TextView) getActivity().findViewById(R.id.registerWrittenCount);
        textViewRunTime = (TextView) getActivity().findViewById(R.id.registerRunTime);
        textViewTagGot = (TextView) getActivity().findViewById(R.id.registetTagGotView);
        textViewVoltageLevel = (TextView) getActivity().findViewById(R.id.registerVoltageLevel);

        buttonWrite = (Button) getActivity().findViewById(R.id.registerWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonWrite.getText().toString().trim().length() == 0) return;
                runningAuto123 = 0; startStopAccessHandler(false);
            }
        });

        Button buttonWrite3 = (Button) getActivity().findViewById(R.id.registerWrite3Button);
        buttonWrite3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                runningAuto123 = 0; startStopAccessHandler(false);
            }
        });

        Button buttonAuto = (Button) getActivity().findViewById(R.id.registerAutoButton);
        buttonAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                runningAuto123 = 1; startStopAccessHandler(false);
            }
        });

        Button buttonAuto123 = (Button) getActivity().findViewById(R.id.registerAutoButtonWBarcodeRead);
        buttonAuto123.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessTask != null) if (accessTask.getStatus() == AsyncTask.Status.RUNNING) return;
                runningAuto123 = 2; mHandler.post(runnableAuto123);
            }
        });

        textViewYield = (TextView) getActivity().findViewById(R.id.registerYieldView);
        textViewTotal = (TextView) getActivity().findViewById(R.id.registerTotalView);

        Button buttonResetCount = (Button) getActivity().findViewById(R.id.registerResetCountButton);
        buttonResetCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCount = true;
                textViewRunTime.setText(""); textViewTagGot.setText(""); textViewVoltageLevel.setText("");
                textViewYield.setText(""); textViewTotal.setText("");
            }
        });

        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNotificationListener();
    }

    @Override
    public void onPause() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setNotificationListener(null);
        mHandler.removeCallbacks(runnableSelect);
        mHandler.removeCallbacks(runnableAuto123);
        if (inventoryBarcodeTask != null) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.DESTORY;
        if (accessTask != null) accessTask.taskCancelReason = AccessTask.TaskCancelRReason.DESTORY;
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("AcccessRegisterFragment().onDestory(): onDestory()");
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    public AccessRegisterFragment() {
        super("AccessRegisterFragment");
    }

    void setNotificationListener() {
        MainActivity.csLibrary4A.setNotificationListener(new Cs108Library4A.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.csLibrary4A.appendToLog("TRIGGER key is pressed.");
                if (barcodeReadRequesting) {
                    if (customPopupWindow.popupWindow.isShowing()) {
                        customPopupWindow.popupWindow.dismiss();
                        barcodeReadRequesting = false; MainActivity.csLibrary4A.appendToLog("barcodeReadRequesting = false");
                        ready2nextRun = true; MainActivity.csLibrary4A.appendToLog("ready2nextRun 1 = true after popup");
                    }
                }
            }
        });
    }

    ArrayList<String> epcArrayList = new ArrayList<String>();
    Runnable runnableSelect = new Runnable() {
        @Override
        public void run() {
            while (MainActivity.sharedObjects.serviceArrayList.size() != 0) {
                String strEpc = MainActivity.sharedObjects.serviceArrayList.get(0); MainActivity.sharedObjects.serviceArrayList.remove(0);
                MainActivity.csLibrary4A.appendToLog("epcArrayList.add[" + epcArrayList.size() + "] = " + strEpc);
                boolean matched = false;
                for (int i = 0; i < epcArrayList.size(); i++) {
                    if (epcArrayList.get(i).matches(strEpc)) {
                        matched = true;
                        break;
                    }
                }
                if (matched == false && strEpc != null) {
                    epcArrayList.add(strEpc);
                }
            }
            textViewSelectedTags.setText("unique tag number = " + epcArrayList.size());
            mHandler.postDelayed(runnableSelect, 1000);
        }
    };

    boolean ready2nextRun = true, barcodeReadRequesting = false, barcodeReadDone = false;
    boolean runningAccessTask = false; int runningAuto123 = 0, totalTag = 0;
    Runnable runnableAuto123 = new Runnable() {
        @Override
        public void run() {
            boolean running = false;
            MainActivity.csLibrary4A.appendToLog("found barcodeReadRequesting as " + barcodeReadRequesting );
            if (barcodeReadRequesting) {
                if (customPopupWindow.popupWindow.isShowing()) running = true;
                else {
                    barcodeReadRequesting = false; MainActivity.csLibrary4A.appendToLog("barcodeReadRequesting = false");
                    ready2nextRun = true; MainActivity.csLibrary4A.appendToLog("ready2nextRun 1 = true after popup");
                }
            }
            MainActivity.csLibrary4A.appendToLog("runnableAuto123: runningAuto123 = " + runningAuto123 + ", inventoryBarcodeTask = " + (inventoryBarcodeTask != null ? "valid" : "null"));
            if (runningAuto123 == 2 && inventoryBarcodeTask != null) { if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) running = true; }
            MainActivity.csLibrary4A.appendToLog("runnableAuto123: accessTask = " + (accessTask != null ? "valid" : "null"));
            if (accessTask != null) { if (accessTask.getStatus() == AsyncTask.Status.RUNNING) running = true; }
            MainActivity.csLibrary4A.appendToLog("runnableAuto123: running = " + running);
            if (running == false) {
                int totalTagNew = getTotalTag();
                if (runningAccessTask) {
                    if (totalTagNew > totalTag) {
                        if (spinnerWriteDataType.getSelectedItemPosition() == 2) editTextWriteData.setText(incrementString(editTextWriteData.getText().toString()));
                    } else runningAuto123 = 0;
                }
                MainActivity.csLibrary4A.appendToLog("runnableAuto123: totalTagNew = " + totalTagNew + ", totalTag = " + totalTag + ", runningAuto123 = " + runningAuto123 + ", runningAccessTask = " + runningAccessTask);
                runningAccessTask = false;

                boolean bcontinue = true;
                if (spinnerWriteDataType.getSelectedItemPosition() == 1 && ready2nextRun == false) {
                    if (buttonSelect.getText().toString().contains("Clear")) {
                        if (textViewSelectedTags.getText().toString().trim().length() == 0) bcontinue = false;
                    }
                    if (bcontinue) {
                        customPopupWindow.popupStart("Next barcode.", false);
                        barcodeReadRequesting = true; MainActivity.csLibrary4A.appendToLog("barcodeReadRequesting = true");
                        barcodeReadDone = false; MainActivity.csLibrary4A.appendToLog("barcodeReadDone = false as popup");
                        bcontinue = false;
                        running = true;
                    }
                }
                if (bcontinue) {
                    if (ready2nextRun == false) {
                        if (buttonSelect.getText().toString().contains("Clear")) {
                            if (textViewSelectedTags.getText().toString().trim().length() != 0) {
                                ready2nextRun = true; MainActivity.csLibrary4A.appendToLog("ready2nextRun = true as valid selected tag in textview");
                            }
                        } else {
                            ready2nextRun = true; MainActivity.csLibrary4A.appendToLog("ready2nextRun = true as not clear");
                        }
                    }
                    if (ready2nextRun) {
                        if (startStopAccessHandler(false)) running = true;
                    }
                }
            }
            if (running && runningAuto123 == 2) mHandler.postDelayed(runnableAuto123, 250);
            else {
                ready2nextRun = true; MainActivity.csLibrary4A.appendToLog("ready2nextRun = true at the runnable end");
                barcodeReadRequesting = false; barcodeReadDone = false; MainActivity.csLibrary4A.appendToLog("barcodeReadDone = false at the runnable end");
            }
        }
    };

    String incrementString(String string1) {
        for (int i = 0; i < string1.length(); i++) {
            String string2 = string1.substring(string1.length() - 1 - i, string1.length() - i);
            Integer iValue = Integer.valueOf(string2, 16);
            if (++iValue >= 16) iValue = 0;
            String stringA = string1.substring(0, string1.length() - 1 - i);
            String stringB = String.format("%X", iValue);
            String stringC = null;
            if (i > 0) stringC = string1.substring(string1.length() - i, string1.length());
            MainActivity.csLibrary4A.appendToLog("stringABC = " + stringA + "," + stringB + "," + stringC);
            String stringABC = (stringA != null ? stringA : "") + stringB + (stringC != null ? stringC : "");
            string1 = stringABC;
            if (iValue != 0) break;
        }
        return string1;
    }

    int getTotalTag() {
        int iValue = 0;
        String stringTotal = textViewTotal.getText().toString();
        String stringTotalTag = stringTotal.replaceAll("[^0-9.]", "");
        try {
            iValue = Integer.parseInt(stringTotalTag);
        } catch (Exception ex) {
        }
        MainActivity.csLibrary4A.appendToLog("totalTag = " + iValue);
        return iValue;
    }

    void startStopBarcodeHandler(boolean buttonTrigger) {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID access", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (started == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) return;
        if (started == false) {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.csLibrary4A.isBarcodeFailure()) {
                MainActivity.csLibrary4A.appendToLog("Toasted 'Barcode is disable'");
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean noToast = true; //runningAuto123;
            inventoryBarcodeTask = new InventoryBarcodeTask(null, null, editTextWriteData, null, null, null, buttonReadBar, buttonWrite, null, noToast);
            inventoryBarcodeTask.execute();
        } else inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
    }

    boolean startStopAccessHandler(boolean buttonTrigger) {
        boolean runningBarcode = false;
        if (inventoryBarcodeTask != null) {
            if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) {
                MainActivity.csLibrary4A.appendToLog("going to startStopBarcodeHandler 1"); startStopBarcodeHandler(buttonTrigger);
                runningBarcode = true;
            }
        }

        boolean runningAccessTask = false;
        if (accessTask != null) { if (accessTask.getStatus() == AsyncTask.Status.RUNNING) runningAccessTask = true; }
        if (buttonTrigger && ((runningAccessTask && MainActivity.csLibrary4A.getTriggerButtonStatus()) || (runningAccessTask == false && MainActivity.csLibrary4A.getTriggerButtonStatus() == false))) {
            return true;
        }

        boolean validResult = true;
        if (runningBarcode) { }
        else if (runningAccessTask) {
            if (buttonTrigger) accessTask.taskCancelReason = AccessTask.TaskCancelRReason.BUTTON_RELEASE;
            else accessTask.taskCancelReason = AccessTask.TaskCancelRReason.STOP;
        } else {
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                validResult = false;
            } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                validResult = false;
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                validResult = false;
            }

            if (validResult) {
                if (barcodeReadDone == false && spinnerWriteDataType.getSelectedItemPosition() == 1) {
                    MainActivity.csLibrary4A.appendToLog("going to startStopBarcodeHandler 2");
                    startStopBarcodeHandler(buttonTrigger);
                } else {
                    totalTag = getTotalTag();
                    if (startAccessTask()) validResult = false;
                    else if (runningAuto123 == 2) this.runningAccessTask = true;
                    MainActivity.csLibrary4A.appendToLog("runningAccessTask = " + this.runningAccessTask);
                    resetOldValue();
                    ready2nextRun = false;
                    MainActivity.csLibrary4A.appendToLog("ready2nextRun = false after startStopAccessHandler");
                }
            }
        }
        return validResult;
    }

    void resetOldValue() {
        editTextWriteData.setTextColor(Color.RED);
        /*checkBoxNewValue.setEnabled(false);
        checkBoxNewValue.setText("Old value");
        checkBoxNewValue.setChecked(false);*/
        newWriteData = false;
    }

    boolean resetCount = true;
    boolean startAccessTask() {
        boolean invalidRequest1 = false;

        int selectQValue = -1, selectPopulation = -1;
        try {
            selectPopulation = Integer.parseInt(editTextSelectPopulation.getText().toString());
        } catch (Exception ex) { }
        if (selectPopulation < 0) invalidRequest1 = true;
        else if (selectPopulation <= getTotalTag()) invalidRequest1 = true;
        else {
            selectQValue = MainActivity.csLibrary4A.getPopulation2Q(selectPopulation);
            if (selectQValue < 0) invalidRequest1 = true;
        }
        MainActivity.csLibrary4A.appendToLog("selectQValue = " + selectQValue + ", selectPopulation = " + selectPopulation);

        String selectMask = "";
        int selectBank1 = -1;
        int selectOffset1 = -1;
        if (buttonSelect.getText().toString().contains("Clear")) {
            String[] stringSplited = textViewSelectedTags.getText().toString().split("\n", 2);
            if (stringSplited != null && stringSplited.length > 0 && stringSplited[0] != null && stringSplited[0].trim().length() != 0) {
                selectMask = stringSplited[0].trim();
                selectBank1 = 1;
                selectOffset1 = 32;
                if (stringSplited.length > 1 && stringSplited[1] != null && stringSplited[1].trim().length() != 0) textViewSelectedTags.setText(stringSplited[1].trim());
                else textViewSelectedTags.setText("");
            }
        } else {
            selectMask = editTextSelectMask.getText().toString().trim();
            int selectBankPosition = spinnerSelectBank.getSelectedItemPosition();
            selectBank1 = selectBankPosition + 1;
            int selectOffset = 0;
            try {
                EditText editTextSelectOffset = (EditText) getActivity().findViewById(R.id.registerSelectOffset);
                selectOffset = Integer.parseInt(editTextSelectOffset.getText().toString());
            } catch (Exception ex) { }
            selectOffset1 = selectBankPosition == 0 ? selectOffset + 32 : selectOffset;
        }
        if (selectMask.trim().length() == 0 || selectBank1 < 0 || selectOffset1 < 0) {
            invalidRequest1 = true;
        }

        String password = editTextPassword.getText().toString();
        if (password.length() != 8) invalidRequest1 = true;

        int antennaPower = -1;
        try {
            editTextAntennaPower = (EditText) getActivity().findViewById(R.id.registerAntennaPower);
            antennaPower = Integer.parseInt(editTextAntennaPower.getText().toString());
        } catch (Exception ex) { }
        if (antennaPower < 0) invalidRequest1 = true;

        int accessBank = spinnerAccessBank.getSelectedItemPosition() == 0 ? 1 : 3;
        MainActivity.csLibrary4A.appendToLog("accessBank = " + accessBank);
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accessBank) == false) invalidRequest1 = true;
        }

        int writeOffset = -1;
        try {
            EditText editTextWriteOffset = (EditText) getActivity().findViewById(R.id.registerWriteOffset);
            writeOffset = Integer.parseInt(editTextWriteOffset.getText().toString());
            if (spinnerAccessBank.getSelectedItemPosition() == 0) writeOffset += 2;
        } catch (Exception ex) { }
        if (writeOffset < 0) invalidRequest1 = true;
        MainActivity.csLibrary4A.appendToLog("writeOffset = " + writeOffset);
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(writeOffset) == false) invalidRequest1 = true;
        }

        int writeLength = -1;
        try {
            writeLength = Integer.parseInt(editTextWriteLength.getText().toString());
        } catch (Exception ex) { }
        if (writeLength < 0) invalidRequest1 = true;
        MainActivity.csLibrary4A.appendToLog("writeLength = " + writeLength);
        if (invalidRequest1 == false) {
            if (writeLength == 0) invalidRequest1 = true;
            else if (MainActivity.csLibrary4A.setAccessCount(writeLength) == false) {
                invalidRequest1 = true;
            }
        }

        String writeData = editTextWriteData.getText().toString().trim();
        if (writeData.length() == 0) invalidRequest1 = true;
        MainActivity.csLibrary4A.appendToLog("writeData = " + writeData);
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessWriteData(writeData) == false) {
                invalidRequest1 = true;
            }
        }

        int repeatCount = 0;
        if (runningAuto123 == 1) repeatCount = selectPopulation;

        MainActivity.csLibrary4A.appendToLog("invalidRequest1 = " + invalidRequest1
                + ", selectMask = " + selectMask + ", selectBank1 = " + selectBank1 + ", selectOffset1 = " + selectOffset1
                + ", password = " + password + ", power = " + antennaPower + ", repeatCount = " + repeatCount + ", resetCount = " + resetCount);
        accessTask = new AccessTask(buttonWrite, textViewWriteCount, invalidRequest1,
                selectMask, selectBank1, selectOffset1,
                password, antennaPower, Cs108Library4A.HostCommands.CMD_18K6CWRITE, selectQValue, repeatCount, resetCount,
                textViewRunTime, textViewTagGot, textViewVoltageLevel,
                textViewYield, textViewTotal);
        accessTask.execute();
        resetCount = false;
        return invalidRequest1;
    }
}
