package com.csl.cs108ademoapp.fragments;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cslibrary4a.CustomAsyncTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;
import com.csl.cslibrary4a.SelectData;
import com.csl.cslibrary4a.TagAxzonOpus;

public class AccessOpusLoggerFragment extends CommonFragment {
    final boolean DEBUG = true;
	EditText editTextRWTagID, editTextAccessRWAccPassword, editTextaccessRWAntennaPower;
    TextView textViewBatteryLevel, textViewLoggerState, textViewClock, textViewNextLogAddress, textViewTidAlarm; //relocate
    TextView textViewFingerArmedClock, textViewFirstTamperAlarmAddress, textViewFirstTemperatureAlarmAddress, textViewAlarmLowerDelayed, textViewAlarmUpperDelayed;
    TextView textViewInitialBatteryLowAlarm, textViewEpcAlarm;
    TextView textViewLoggingInterval, textViewFingerSpotStartup, textViewAlarmUpperLimit, textViewAlarmLowerLimit, textViewSamplingRegimePeriod; //relocate config
    TextView textViewAlarmUpperDelay, textViewAlarmLowerDelay, textViewLoggingDelayedStart, textViewLoggerArmedSecond;


    TextView textViewMinBattery4Arming, textViewMinBattery4Logging, textViewLoggingSampleSize;
    TextView textViewFingerSpotLed;
    TextView textViewWritePermalock, textViewBAPduration;
    TextView textViewSamplesPerMeasure, textViewConfigAddress, textViewSsdAddress, textViewRtcAddress;
    CheckBox checkBoxBatteryLevel, checkBoxLoggerState, checkBoxClock, checkBoxNextLogAddress, checkBoxTidAlarm; //relocate
    CheckBox checkBoxFingerArmedClock, checkBoxFirstTamperAlarmAddress, checkBoxFirstTemperatureAlarmAddress, checkBoxAlarmLowerDelayed, checkBoxAlarmUpperDelayed;
    CheckBox checkBoxInitialBatteryLowAlarm, checkBoxEpcAlarm;
    CheckBox checkBoxTidAlarmBattery, checkBoxTidAlarmTamper, checkBoxTidAlarmHighTemperature, checkBoxTidAlarmLowTemperature;
    CheckBox checkBoxEpcAlarmBatteryInstalled, checkBoxEpcAlarmBatteryConnected, checkBoxEpcAlarmTemperature, checkBoxEpcAlarmBattery, checkBoxEpcAlarmTamper;
    CheckBox checkBoxLoggingInterval, checkBoxFingerSpotStartup, checkBoxAlarmUpperLimit, checkBoxAlarmLowerLimit, checkBoxSamplingRegimePeriod; //relocate config
    CheckBox checkBoxAlarmUpperDelay, checkBoxAlarmLowerDelay, checkBoxLoggingDelayedStart, checkBoxLoggerArmedSecond;


    CheckBox checkBoxMinBattery4Arming, checkBoxMinBattery4Logging, checkBoxLoggingSampleSize;
    CheckBox checkBoxFingerSpotLed;
    CheckBox checkBoxWritePermalock, checkBoxBAPduration;
    CheckBox checkBoxSamplesPerMeasure, checkBoxConfigAddress, checkBoxSsdAddress, checkBoxRtcAddress;
    TextView textViewLedMode, textViewLedOn, textViewLedOff;
    Spinner spinnerLoggerState, spinnerTidAlarm, spinnerInitialBatteryLowAlarm, spinnerEpcAlarm; //relocate


    EditText editTextBatteryLevel, editTextClock, editTextNextLogAddress, editTextFingerArmedClock, editTextFirstTamperAlarmAddress; //relocate
    EditText editTextFirstTemperatureAlarmAddress, editTextAlarmLowerDelayed, editTextAlarmUpperDelayed;
    Spinner spinnerLoggingInterval, spinnerFingerSpotStartup; //relocate config
    EditText editTextAlarmUpperLimit, editTextAlarmLowerLimit, editTextSamplingRegimePeriod, editTextAlarmUpperDelay, editTextAlarmLowerDelay;
    EditText editTextLoggingDelayedStart, editTextLoggerArmedSecond;
    CheckBox checkBoxFingerSpotStartEnable, checkBoxTamperDetectEnable, checkBoxTamperDisconnectPolarity, checkBoxSampingRegimeEnable;
    TextView textViewAlarmUpperLimitUnit, textViewAlarmLowerLimitUnit;


    Spinner spinnerLoggingSampleSize, spinnerFingerSpotLed, spinnerLedMode;
    Spinner spinnerWritePermalock;
    EditText editTextMinBattery4Arming, editTextMinBattery4Logging, editTextLedOn, editTextLedOff;
    EditText editTextBAPduration;
    EditText editTextSamplesPerMeasure, editTextConfigAddress, editTextSsdAddress, editTextRtcAddress;

    int iUserCode2UnitPosition, iUserCode3UnitPosition;
	private Button buttonRead, buttonWrite;
    enum ReadWriteTypes {
        NULL,
        USERCODE_BATTERY_LEVEL, USERCODE_LOGGER_STATE, USERCODE_CLOCK, USERCODE_NEXT_LOG_ADDRESS, USERCODE_TID_ALARM,
        USERCODE_FINGER_ARMED_CLOCK, USERCODE_FIRST_TAMPER_ALARM_ADDRESS, USERCODE_FIRST_TEMPERATURE_ALARM_ADDRESS, USERCODE_ALARM_LOWER_DELAYED, USERCODE_ALARM_UPPER_DELAYED,
        USERCODE_INITIAL_BATTERY_LOW_ALARM, USERCODE_EPC_ALARM,
        USERCODE_LOGGING_INTERVAL, USERCODE_FINGERSPOT_STARTUP, USERCODE_ALRAM_UPPER_LIMIT, USERCODE_ALRAM_LOWER_LIMIT, USERCODE_SAMPLING_REGIME_PERIOD,
        USERCODE_ALRAM_UPPER_DELAY, USERCODE_ALRAM_LOWER_DELAY, USERCODE_DELAYED_LOGGING_START, USERCODE_LOGGER_ARMED_SECOND,


        USERCODE_SAMPLENUMBER_TOLOG,
        USERCODDE_CONFIG_ADDRESSS, USERCODE_MINBATTERY_4LOGGING, USERCODE_MINBATTERY_4ARMING, USERCODE_FINGERSPOT_LED, USERCODE_WRITE_PERMALOCK,
        USERCODE_BAP_DURATION, USERCODE_SAMPLES_PER_MEASURE, USERCODE_SSD_ADDRESS, USERCODE_RTC_ADDRESS
    }
    ReadWriteTypes readWriteTypes;
    boolean operationRead = false;
    private int modelCode = 0;
    TagAxzonOpus tagAxzonOpus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_access_opus_logger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextRWTagID = (EditText) view.findViewById(R.id.accessXXTagID);
        editTextAccessRWAccPassword = (EditText) view.findViewById(R.id.accessXXAccPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");

        textViewBatteryLevel = (TextView) view.findViewById(R.id.accessAxzonTextView999);
        checkBoxBatteryLevel = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle999); checkBoxBatteryLevel.setText("? Read Battery Level (U3)");
        editTextBatteryLevel = (EditText) view.findViewById(R.id.accessAxzonEditView999); editTextBatteryLevel.setInputType(InputType.TYPE_CLASS_NUMBER); editTextBatteryLevel.setEnabled(false);

        textViewLoggerState = (TextView) view.findViewById(R.id.accessAxzonTextView10);
        checkBoxLoggerState = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle10); checkBoxLoggerState.setText("Read State (E1,U5)");
        spinnerLoggerState = (Spinner) view.findViewById(R.id.accessAxzonSpinner10); setupSpinner(spinnerLoggerState, R.array.tagAxzon_Opus_logger_state_options); spinnerLoggerState.setEnabled(false);

        textViewClock = (TextView) view.findViewById(R.id.accessAxzonTextView12);
        checkBoxClock = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle12); checkBoxClock.setText("Read 30-second Clock Count (U6,7)");
        editTextClock = (EditText) view.findViewById(R.id.accessAxzonEditView12); editTextClock.setInputType(InputType.TYPE_CLASS_NUMBER); editTextClock.setEnabled(false);

        textViewNextLogAddress = (TextView) view.findViewById(R.id.accessAxzonTextView13);
        checkBoxNextLogAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle13); checkBoxNextLogAddress.setText("Next Log Address (1b)");
        editTextNextLogAddress = (EditText) view.findViewById(R.id.accessAxzonEditView13); editTextNextLogAddress.setInputType(InputType.TYPE_CLASS_NUMBER); editTextNextLogAddress.setEnabled(false);

        textViewTidAlarm = (TextView) view.findViewById(R.id.accessAxzonTextView16);
        checkBoxTidAlarm = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle16); checkBoxTidAlarm.setText("TID Alarm(10)");
        spinnerTidAlarm = (Spinner) view.findViewById(R.id.accessAxzonSpinner16); setupSpinner(spinnerTidAlarm, R.array.tagAxzon_Opus_inactive_active_options); spinnerTidAlarm.setEnabled(false);
        checkBoxTidAlarmBattery = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle16a); checkBoxTidAlarmBattery.setText("battery"); checkBoxTidAlarmBattery.setEnabled(false);
        checkBoxTidAlarmTamper = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle16b); checkBoxTidAlarmTamper.setText("tamper"); checkBoxTidAlarmTamper.setEnabled(false);
        checkBoxTidAlarmHighTemperature = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle16c); checkBoxTidAlarmHighTemperature.setText("high temp"); checkBoxTidAlarmHighTemperature.setEnabled(false);
        checkBoxTidAlarmLowTemperature = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle16d); checkBoxTidAlarmLowTemperature.setText("low temp"); checkBoxTidAlarmLowTemperature.setEnabled(false);

        textViewFingerArmedClock = (TextView) view.findViewById(R.id.accessAxzonTextView13b);
        checkBoxFingerArmedClock = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle13b); checkBoxFingerArmedClock.setText("FingerSpot Armed Clock Count (d,E)");
        editTextFingerArmedClock = (EditText) view.findViewById(R.id.accessAxzonEditView13b); editTextFingerArmedClock.setInputType(InputType.TYPE_CLASS_NUMBER); editTextFingerArmedClock.setEnabled(false);

        textViewFirstTamperAlarmAddress = (TextView) view.findViewById(R.id.accessAxzonTextView13d);
        checkBoxFirstTamperAlarmAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle13d); checkBoxFirstTamperAlarmAddress.setText("First Tamper Violation Address (c)");
        editTextFirstTamperAlarmAddress = (EditText) view.findViewById(R.id.accessAxzonEditView13d); editTextFirstTamperAlarmAddress.setInputType(InputType.TYPE_CLASS_NUMBER); editTextFirstTamperAlarmAddress.setEnabled(false);

        textViewFirstTemperatureAlarmAddress = (TextView) view.findViewById(R.id.accessAxzonTextView13c);
        checkBoxFirstTemperatureAlarmAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle13c); checkBoxFirstTemperatureAlarmAddress.setText("First Temperature Violation Address (b)");
        editTextFirstTemperatureAlarmAddress = (EditText) view.findViewById(R.id.accessAxzonEditView13c); editTextFirstTemperatureAlarmAddress.setInputType(InputType.TYPE_CLASS_NUMBER); editTextFirstTemperatureAlarmAddress.setEnabled(false);

        textViewAlarmLowerDelayed = (TextView) view.findViewById(R.id.accessAxzonTextView20);
        checkBoxAlarmLowerDelayed = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle20); checkBoxAlarmLowerDelayed.setText("? Alarm lower delayed (A)");
        editTextAlarmLowerDelayed = (EditText) view.findViewById(R.id.accessAxzonEditView20); editTextAlarmLowerDelayed.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewAlarmUpperDelayed = (TextView) view.findViewById(R.id.accessAxzonTextView21);
        checkBoxAlarmUpperDelayed = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle21); checkBoxAlarmUpperDelayed.setText("? Alarm upper delayed (9)");
        editTextAlarmUpperDelayed = (EditText) view.findViewById(R.id.accessAxzonEditView21); editTextAlarmUpperDelayed.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewInitialBatteryLowAlarm = (TextView) view.findViewById(R.id.accessAxzonTextView14);
        checkBoxInitialBatteryLowAlarm = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle14); checkBoxInitialBatteryLowAlarm.setText("Initial Battery Low Alarm (E21)");
        spinnerInitialBatteryLowAlarm = (Spinner) view.findViewById(R.id.accessAxzonSpinner14); setupSpinner(spinnerInitialBatteryLowAlarm, R.array.tagAxzon_Opus_inactive_active_options); spinnerInitialBatteryLowAlarm.setEnabled(false);

        textViewEpcAlarm = (TextView) view.findViewById(R.id.accessAxzonTextView15);
        checkBoxEpcAlarm = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15); checkBoxEpcAlarm.setText("EPC Alarm (E1)");
        spinnerEpcAlarm = (Spinner) view.findViewById(R.id.accessAxzonSpinner15); setupSpinner(spinnerEpcAlarm, R.array.tagAxzon_Opus_inactive_active_options); spinnerEpcAlarm.setEnabled(false);
        checkBoxEpcAlarmBatteryInstalled = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15aa); checkBoxEpcAlarmBatteryInstalled.setText("Battery Installed"); checkBoxEpcAlarmBatteryInstalled.setEnabled(false);
        checkBoxEpcAlarmBatteryConnected = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15ab); checkBoxEpcAlarmBatteryConnected.setText("Battery Connected"); checkBoxEpcAlarmBatteryConnected.setEnabled(false);
        checkBoxEpcAlarmTemperature = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15a); checkBoxEpcAlarmTemperature.setText("temperature"); checkBoxEpcAlarmTemperature.setEnabled(false);
        checkBoxEpcAlarmBattery = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15b); checkBoxEpcAlarmBattery.setText("battery"); checkBoxEpcAlarmBattery.setEnabled(false);
        checkBoxEpcAlarmTamper = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle15c); checkBoxEpcAlarmTamper.setText("tamper"); checkBoxEpcAlarmTamper.setEnabled(false);

        textViewLoggingInterval = (TextView) view.findViewById(R.id.accessAxzonTextView1);
        checkBoxLoggingInterval = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle1); checkBoxLoggingInterval.setText("Logging interval (8,F)");
        spinnerLoggingInterval = (Spinner) view.findViewById(R.id.accessAxzonSpinner1); setupSpinner(spinnerLoggingInterval, R.array.tagAxzon_Opus_logging_interval_options);

        textViewFingerSpotStartup = (TextView) view.findViewById(R.id.accessAxzonTextView11);
        checkBoxFingerSpotStartup = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11); checkBoxFingerSpotStartup.setText("FingerSpot startup (8)");
        spinnerFingerSpotStartup = (Spinner) view.findViewById(R.id.accessAxzonSpinner11); setupSpinner(spinnerFingerSpotStartup, R.array.tagAxzon_Opus_disable_enable_options); spinnerFingerSpotStartup.setEnabled(false);
        checkBoxFingerSpotStartEnable = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11m); checkBoxFingerSpotStartEnable.setText("fingerSpot startup enable");
        checkBoxTamperDetectEnable = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11n); checkBoxTamperDetectEnable.setText("tamper detect enable");
        checkBoxTamperDisconnectPolarity = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11o); checkBoxTamperDisconnectPolarity.setText("tamper disconnect polarity");
        checkBoxSampingRegimeEnable = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11p); checkBoxSampingRegimeEnable.setText("sampling regime enable");

        textViewAlarmUpperLimit = (TextView) view.findViewById(R.id.accessAxzonTextView2);
        checkBoxAlarmUpperLimit = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle2); checkBoxAlarmUpperLimit.setText("Alarm upper limit (9)");
        editTextAlarmUpperLimit = (EditText) view.findViewById(R.id.accessAxzonEditView2); editTextAlarmUpperLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        textViewAlarmUpperLimitUnit = (TextView) view.findViewById(R.id.accessAxzonTextView222); textViewAlarmUpperLimitUnit.setText("\u00B0C");

        textViewAlarmLowerLimit = (TextView) view.findViewById(R.id.accessAxzonTextView3);
        checkBoxAlarmLowerLimit = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle3); checkBoxAlarmLowerLimit.setText("Alarm lower limit (A)");
        editTextAlarmLowerLimit = (EditText) view.findViewById(R.id.accessAxzonEditView3); editTextAlarmLowerLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        textViewAlarmLowerLimitUnit = (TextView) view.findViewById(R.id.accessAxzonTextView333); textViewAlarmLowerLimitUnit.setText("\u00B0C");

        textViewSamplingRegimePeriod = (TextView) view.findViewById(R.id.accessAxzonTextView4x);
        checkBoxSamplingRegimePeriod = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle4x); checkBoxSamplingRegimePeriod.setText("? Sampling Regime Period (F)");
        editTextSamplingRegimePeriod = (EditText) view.findViewById(R.id.accessAxzonEditView4x); editTextSamplingRegimePeriod.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewAlarmUpperDelay = (TextView) view.findViewById(R.id.accessAxzonTextView4);
        checkBoxAlarmUpperDelay = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle4); checkBoxAlarmUpperDelay.setText("Alarm upper delay (10)");
        editTextAlarmUpperDelay = (EditText) view.findViewById(R.id.accessAxzonEditView4); editTextAlarmUpperDelay.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewAlarmLowerDelay = (TextView) view.findViewById(R.id.accessAxzonTextView5);
        checkBoxAlarmLowerDelay = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle5); checkBoxAlarmLowerDelay.setText("Alarm lower delay (10)");
        editTextAlarmLowerDelay = (EditText) view.findViewById(R.id.accessAxzonEditView5); editTextAlarmLowerDelay.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewLoggingDelayedStart = (TextView) view.findViewById(R.id.accessAxzonTextView8);
        checkBoxLoggingDelayedStart = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle8); checkBoxLoggingDelayedStart.setText("Logging Delayed cycles to start (10)");
        editTextLoggingDelayedStart = (EditText) view.findViewById(R.id.accessAxzonEditView8); editTextLoggingDelayedStart.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewLoggerArmedSecond = (TextView) view.findViewById(R.id.accessAxzonTextView13a);
        checkBoxLoggerArmedSecond = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle13a); checkBoxLoggerArmedSecond.setText("Logger Armed Time Stamp RTC (11,12)");
        editTextLoggerArmedSecond = (EditText) view.findViewById(R.id.accessAxzonEditView13a); editTextLoggerArmedSecond.setInputType(InputType.TYPE_CLASS_NUMBER); editTextLoggerArmedSecond.setEnabled(false);


        textViewLoggingSampleSize = (TextView) view.findViewById(R.id.accessAxzonTextView80);
        checkBoxLoggingSampleSize = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle80); checkBoxLoggingSampleSize.setText("? Logging Sample size (1A)");
        spinnerLoggingSampleSize = (Spinner) view.findViewById(R.id.accessAxzonSpinner80); setupSpinner(spinnerLoggingSampleSize, R.array.tagAxzon_Opus_SampleNumber_ToLog_options);

        textViewConfigAddress = (TextView) view.findViewById(R.id.accessAxzonTextView9c);
        checkBoxConfigAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9c); checkBoxConfigAddress.setText("? Config Address (1A)");
        editTextConfigAddress = (EditText) view.findViewById(R.id.accessAxzonEditView9c); editTextConfigAddress.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewMinBattery4Logging = (TextView) view.findViewById(R.id.accessAxzonTextView1b);
        checkBoxMinBattery4Logging = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle1b); checkBoxMinBattery4Logging.setText("? Minimum battery for logging (1c)");
        editTextMinBattery4Logging = (EditText) view.findViewById(R.id.accessAxzonEditView1b); editTextMinBattery4Logging.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewMinBattery4Arming = (TextView) view.findViewById(R.id.accessAxzonTextView1a);
        checkBoxMinBattery4Arming = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle1a); checkBoxMinBattery4Arming.setText("? Minimum battery for arming (1d)");
        editTextMinBattery4Arming = (EditText) view.findViewById(R.id.accessAxzonEditView1a); editTextMinBattery4Arming.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewFingerSpotLed = (TextView) view.findViewById(R.id.accessAxzonTextView11a);
        checkBoxFingerSpotLed = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle11a); checkBoxFingerSpotLed.setText("? FingerSpot LED (1E)");
        spinnerFingerSpotLed = (Spinner) view.findViewById(R.id.accessAxzonSpinner11a); setupSpinner(spinnerFingerSpotLed, R.array.tagAxzon_Opus_disable_enable_options);
        textViewLedMode = (TextView) view.findViewById(R.id.accessAxzonTextView11b); textViewLedMode.setText("Mode:");
        spinnerLedMode = (Spinner) view.findViewById(R.id.accessAxzonSpinner11b); setupSpinner(spinnerLedMode, R.array.tagAxzon_Opus_disable_enable_options);
        textViewLedOn = (TextView) view.findViewById(R.id.accessAxzonTextView11c); textViewLedOn.setText("On time:");
        editTextLedOn = (EditText) view.findViewById(R.id.accessAxzonEditView11c); editTextLedOn.setInputType(InputType.TYPE_CLASS_NUMBER);
        textViewLedOff = (TextView) view.findViewById(R.id.accessAxzonTextView11d); textViewLedOff.setText("Off time:");
        editTextLedOff = (EditText) view.findViewById(R.id.accessAxzonEditView11d); editTextLedOff.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewWritePermalock = (TextView) view.findViewById(R.id.accessAxzonTextView9);
        checkBoxWritePermalock = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9); checkBoxWritePermalock.setText("? Write PermaLock (1F)");
        spinnerWritePermalock = (Spinner) view.findViewById(R.id.accessAxzonEditView9); setupSpinner(spinnerWritePermalock, R.array.tagAxzon_Opus_disable_enable_options);

        textViewBAPduration = (TextView) view.findViewById(R.id.accessAxzonTextView9a);
        checkBoxBAPduration = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9a); checkBoxBAPduration.setText("BAP duration (1F)");
        editTextBAPduration = (EditText) view.findViewById(R.id.accessAxzonEditView9a); editTextBAPduration.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewSamplesPerMeasure = (TextView) view.findViewById(R.id.accessAxzonTextView9b);
        checkBoxSamplesPerMeasure = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9b); checkBoxSamplesPerMeasure.setText("? Samples per measurement (1F)");
        editTextSamplesPerMeasure = (EditText) view.findViewById(R.id.accessAxzonEditView9b); editTextSamplesPerMeasure.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewSsdAddress= (TextView) view.findViewById(R.id.accessAxzonTextView9d);
        checkBoxSsdAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9d); checkBoxSsdAddress.setText("? SSD Addresss (26,27)");
        editTextSsdAddress = (EditText) view.findViewById(R.id.accessAxzonEditView9d); editTextSsdAddress.setInputType(InputType.TYPE_CLASS_NUMBER);

        textViewRtcAddress= (TextView) view.findViewById(R.id.accessAxzonTextView9e);
        checkBoxRtcAddress = (CheckBox) view.findViewById(R.id.accessAxzonCheckBoxTitle9e); checkBoxRtcAddress.setText("? RTC Addresss (28,27)");
        editTextRtcAddress = (EditText) view.findViewById(R.id.accessAxzonEditView9e); editTextRtcAddress.setInputType(InputType.TYPE_CLASS_NUMBER);

        editTextaccessRWAntennaPower = (EditText) view.findViewById(R.id.accessXXAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        buttonRead = (Button) view.findViewById(R.id.accessRWReadButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = true; startReadWrite();
            }
        });

        buttonWrite = (Button) view.findViewById(R.id.accessRWWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.context, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.context, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = false; startReadWrite();
            }
        });

        MainActivity.csLibrary4A.setSameCheck(false);

        tagAxzonOpus = new TagAxzonOpus(MainActivity.context, MainActivity.csLibrary4A, MainActivity.sharedObjects.playerN, MainActivity.sharedObjects.playerO, buttonRead, buttonWrite);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint2(true);
    }

    @Override
    public void onDestroy() {
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setSameCheck(true);
        setUserVisibleHint2(false);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        //if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) == false) return;
        MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.setUserVisibleHint: 1 isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) { //getUserVisibleHint()) {
            userVisibleHint = true;
            setupTagID();
        } else {
            userVisibleHint = false;
        }
    }
    public AccessOpusLoggerFragment() {
        super("AccessXerxesLoggerFragment");
    }
    void setupTagID() {
        ReaderDevice tagSelected = MainActivity.tagSelected;
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                String stringDetail = tagSelected.getDetails();
                int indexUser = stringDetail.indexOf("TID=");
                if (indexUser != -1) {
                    //
                }
                bSelected = true;
                if (editTextRWTagID != null) editTextRWTagID.setText(tagSelected.getAddress());

                if (tagSelected.getMdid() == null) {
                } else if (tagSelected.getTagTypeExpected() == RfidReader.TagType.TAG_MAGNUS_S2) {
                    modelCode = 2;
                } else if (tagSelected.getTagTypeExpected() == RfidReader.TagType.TAG_MAGNUS_S3) {
                    modelCode = 3;
                } else if (tagSelected.getTagTypeExpected() == RfidReader.TagType.TAG_AXZON_XERXES) {
                    modelCode = 5;
                } else if (tagSelected.getTagTypeExpected() == RfidReader.TagType.TAG_AXZON_OPUS) {
                    modelCode = 50;
                }

                String strRes = tagSelected.getRes();
                if (strRes != null) {
                    int ibracket = strRes.indexOf("(");
                    if (ibracket > 0) strRes = strRes.substring(0, ibracket);
                }

                stringDetail = tagSelected.getDetails();
                indexUser = stringDetail.indexOf("USER=");
                if (indexUser != -1) {
                    String stringUser = stringDetail.substring(indexUser + 5);
                    MainActivity.csLibrary4A.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
                }
            }
        }
    }
    void startReadWrite() {
        MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.startReadWrite: updating = " + updating);
        if (updating == false) {
            TagAxzonOpus.selectData = new SelectData();
            tagAxzonOpus.selectData.selectMaskEpc = editTextRWTagID.getText().toString();
            tagAxzonOpus.selectData.selectPassword = editTextAccessRWAccPassword.getText().toString();
            tagAxzonOpus.selectData.selectPower = Integer.valueOf(editTextaccessRWAntennaPower.getText().toString());

            updating = true; bankProcessing = 0;
            checkProcessing = 0;

            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0;
    int checkProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = true;
            CustomAsyncTask.Status status = tagAxzonOpus.getReadWriteStatus();
            if (status == null) {
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): NULL stringReadWriteStatus");

                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.updateRunnable: processTickItems is invalid = " + invalid);
                if (invalid == true)   {
                    rerunRequest = false;
                } else {
                    //rerunRequest = true;
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): " + (operationRead ? "reading" : "writing") + " is started");
                }
            } else if (status != CustomAsyncTask.Status.FINISHED) {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): stringReadWriteStatus =  " + status.toString());
                    //rerunRequest = true;
            } else {
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): FINISHED accessReadWriteTask");
                if (processResult()) {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): processResult is TRUE");
                } else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): processResult is FALSE");
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): Restart");
            }
            else updating = false;
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().updateRunnable(): Ending with updating = " + updating);
        }
    };
    void setupSpinner(Spinner spinner, @ArrayRes int textArrayResId) {
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), textArrayResId, R.layout.custom_spinner_layout);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }
    void updateBatteryLevel(int iValue) {
        textViewBatteryLevel.setText("O");
        checkBoxBatteryLevel.setChecked(false);
        editTextBatteryLevel.setText(String.valueOf(iValue));
    }
    void updateLoggerState(TagAxzonOpus.LoggerStateTypes loggingStateType) {
        textViewLoggerState.setText("O");
        checkBoxLoggerState.setChecked(false);
        spinnerLoggerState.setSelection(loggingStateType.ordinal() + 1);
    }
    void updateClock(int iValue) {
        textViewClock.setText("O");
        checkBoxClock.setChecked(false);
        editTextClock.setText(String.valueOf(iValue));
    }
    void updateNextLogAddress(int iValue) {
        textViewNextLogAddress.setText("O");
        checkBoxNextLogAddress.setChecked(false);
        editTextNextLogAddress.setText(String.valueOf(iValue)); editTextNextLogAddress.setEnabled(true);
    }
    void updateTidAlarm(TagAxzonOpus.TidAlarmTypes tidAlarmType) {
        textViewTidAlarm.setText("O");
        checkBoxTidAlarm.setChecked(false);

        spinnerTidAlarm.setSelection(tidAlarmType.isAlarm() ? 2 : 1);
        checkBoxTidAlarmLowTemperature.setEnabled(true);
        if (tidAlarmType.lowTemperatureAlarm > 0) checkBoxTidAlarmLowTemperature.setChecked(true);
        else checkBoxTidAlarmLowTemperature.setChecked(false);
        checkBoxTidAlarmHighTemperature.setEnabled(true);
        if (tidAlarmType.highTemperatureAlarm > 0) checkBoxTidAlarmHighTemperature.setChecked(true);
        else checkBoxTidAlarmHighTemperature.setChecked(false);
        checkBoxTidAlarmTamper.setEnabled(true);
        if (tidAlarmType.tamperAlarm > 0) checkBoxTidAlarmTamper.setChecked(true);
        else checkBoxTidAlarmTamper.setChecked(false);
        checkBoxTidAlarmBattery.setEnabled(true);
        if (tidAlarmType.batteryAlarm > 0) checkBoxTidAlarmBattery.setChecked(true);
        else checkBoxTidAlarmBattery.setChecked(false);
    }
    void updateFingerArmedClock(int iValue) {
        textViewFingerArmedClock.setText("O");
        checkBoxFingerArmedClock.setChecked(false);
        editTextFingerArmedClock.setText(String.valueOf(iValue)); editTextFingerArmedClock.setEnabled(true);
    }
    void updateFirstTamperAlarmAddress(int iValue) {
        textViewFirstTamperAlarmAddress.setText("O");
        checkBoxFirstTamperAlarmAddress.setChecked(false);
        editTextFirstTamperAlarmAddress.setText(String.valueOf(iValue)); editTextFirstTamperAlarmAddress.setEnabled(true);
    }
    void updateFirstTemperatureAlarmAddress(int iValue) {
        textViewFirstTemperatureAlarmAddress.setText("O");
        checkBoxFirstTemperatureAlarmAddress.setChecked(false);
        editTextFirstTemperatureAlarmAddress.setText(String.valueOf(iValue)); editTextFirstTemperatureAlarmAddress.setEnabled(true);
    }
    void updateAlarmLowerDelayed(int iValue) {
        textViewAlarmLowerDelayed.setText("O");
        checkBoxAlarmLowerDelayed.setChecked(false);
        editTextAlarmLowerDelayed.setText(String.valueOf(iValue)); editTextAlarmLowerDelayed.setEnabled(true);
    }
    void updateAlarmUpperDelayed(int iValue) {
        textViewAlarmUpperDelayed.setText("O");
        checkBoxAlarmUpperDelayed.setChecked(false);
        editTextAlarmUpperDelayed.setText(String.valueOf(iValue)); editTextAlarmUpperDelayed.setEnabled(true);
    }
    void updateInitialBatteryLowAlarm(@NonNull TagAxzonOpus.DisableEnableTypes batteryLowAlarmType) {
        textViewInitialBatteryLowAlarm.setText("O");
        checkBoxInitialBatteryLowAlarm.setChecked(false);
        spinnerInitialBatteryLowAlarm.setSelection(batteryLowAlarmType.ordinal() + 1); spinnerInitialBatteryLowAlarm.setEnabled(true);
    }
    void updateEpcAlarm(@NonNull TagAxzonOpus.EpcAlarmTypes epcAlarmType) {
        textViewEpcAlarm.setText("O");
        checkBoxEpcAlarm.setChecked(false);

        spinnerEpcAlarm.setSelection(epcAlarmType.isAlarm() ? 2 : 1);
        if (epcAlarmType.batteryInstalled > 0) checkBoxEpcAlarmBatteryInstalled.setChecked(true);
        else checkBoxEpcAlarmBatteryInstalled.setChecked(false);
        if (epcAlarmType.batteryConnected > 0) checkBoxEpcAlarmBatteryConnected.setChecked(true);
        else checkBoxEpcAlarmBatteryConnected.setChecked(false);
        checkBoxEpcAlarmTemperature.setEnabled(true);
        if (epcAlarmType.temperatureAlarm > 0) checkBoxEpcAlarmTemperature.setChecked(true);
        else checkBoxEpcAlarmTemperature.setChecked(false);
        checkBoxEpcAlarmBattery.setEnabled(true);
        if (epcAlarmType.batteryAlarm > 0) checkBoxEpcAlarmBattery.setChecked(true);
        else checkBoxEpcAlarmBattery.setChecked(false);
        checkBoxEpcAlarmTamper.setEnabled(true);
        if (epcAlarmType.tamperAlarm > 0) checkBoxEpcAlarmTamper.setChecked(true);
        else checkBoxEpcAlarmTamper.setChecked(false);
    }
    void updateLoggingInterval(TagAxzonOpus.LoggingIntervalTypes loggingIntervalType) {
        textViewLoggingInterval.setText("O");
        checkBoxLoggingInterval.setChecked(false);
        spinnerLoggingInterval.setSelection(loggingIntervalType.ordinal() + 1);
    }
    void updateFingerSpotStartup(TagAxzonOpus.FingerSpotStartupEnables fingerSpotStartupType) {
        MainActivity.csLibrary4A.appendToLog("AccessOpusLoggerFragment.updateFingerSpotStartup with tamperPolarityConnected = " + fingerSpotStartupType.tamperDisconnectPolarity);
        textViewFingerSpotStartup.setText("O");
        checkBoxFingerSpotStartup.setChecked(false);

        spinnerFingerSpotStartup.setSelection(fingerSpotStartupType.isEnable() ? 2 : 1); spinnerFingerSpotStartup.setEnabled(true);
        if (fingerSpotStartupType.fingerSpotStartEnable > 0) checkBoxFingerSpotStartEnable.setChecked(true);
        else checkBoxFingerSpotStartEnable.setChecked(false);
        if (fingerSpotStartupType.tamperDetectEnable > 0) checkBoxTamperDetectEnable.setChecked(true);
        else checkBoxTamperDetectEnable.setChecked(false);
        if (fingerSpotStartupType.tamperDisconnectPolarity > 0) checkBoxTamperDisconnectPolarity.setChecked(true);
        else checkBoxTamperDisconnectPolarity.setChecked(false);
        if (fingerSpotStartupType.samplingRegimeEnable > 0) checkBoxSampingRegimeEnable.setChecked(true);
        else checkBoxSampingRegimeEnable.setChecked(false);
    }
    void updateAlarmUpperLimit(float fValue) {
        textViewAlarmUpperLimit.setText("O");
        checkBoxAlarmUpperLimit.setChecked(false);
        editTextAlarmUpperLimit.setText(String.valueOf(fValue));
    }
    void updateAlarmLowerLimit(float fValue) {
        textViewAlarmLowerLimit.setText("O");
        checkBoxAlarmLowerLimit.setChecked(false);
        editTextAlarmLowerLimit.setText(String.valueOf(fValue));
    }
    void updateSamplingRegimePeriod(int iValue) {
        textViewSamplingRegimePeriod.setText("O");
        checkBoxSamplingRegimePeriod.setChecked(false);
        editTextSamplingRegimePeriod.setText(String.valueOf(iValue));
    }
    void updateAlarmUpperDelay(int iValue) {
        textViewAlarmUpperDelay.setText("O");
        checkBoxAlarmUpperDelay.setChecked(false);
        editTextAlarmUpperDelay.setText(String.valueOf(iValue));
    }
    void updateAlarmLowerDelay(int iValue) {
        textViewAlarmLowerDelay.setText("O");
        checkBoxAlarmLowerDelay.setChecked(false);
        editTextAlarmLowerDelay.setText(String.valueOf(iValue));
    }
    void updateLoggingDelayedStart(int iValue) {
        textViewLoggingDelayedStart.setText("O");
        checkBoxLoggingDelayedStart.setChecked(false);
        editTextLoggingDelayedStart.setText(String.valueOf(iValue));
    }
    void updateLoggerArmedSecond(int iValue) {
        textViewLoggerArmedSecond.setText("O");
        checkBoxLoggerArmedSecond.setChecked(false);
        editTextLoggerArmedSecond.setText(String.valueOf(iValue));
    }


    void updateMinBattery4Arming(int iValue) {
        textViewMinBattery4Arming.setText("O");
        checkBoxMinBattery4Arming.setChecked(false);
        editTextMinBattery4Arming.setText(String.valueOf(iValue));
    }
    void updateMinBattery4Logging(int iValue) {
        textViewMinBattery4Logging.setText("O");
        checkBoxMinBattery4Logging.setChecked(false);
        editTextMinBattery4Logging.setText(String.valueOf(iValue));
    }
    void updateLoggingSampleSize(TagAxzonOpus.SampleNumberToLogTypes sampleNumberToLogType) {
        textViewLoggingSampleSize.setText("O");
        checkBoxLoggingSampleSize.setChecked(false);
        spinnerLoggingSampleSize.setSelection(sampleNumberToLogType.ordinal() + 1);
    }
    void updateFingerSpotLed(TagAxzonOpus.DisableEnableTypes fingerSpotLedType) {
        textViewFingerSpotLed.setText("O");
        checkBoxFingerSpotLed.setChecked(false);
        spinnerFingerSpotLed.setSelection(fingerSpotLedType.ordinal() + 1);
    }
    void updateBAPduration(int iValue) {
        textViewBAPduration.setText("O");
        checkBoxBAPduration.setChecked(false);
        editTextBAPduration.setText(String.valueOf(iValue));
    }
    boolean processResult() {
        if (readWriteTypes == ReadWriteTypes.USERCODE_BATTERY_LEVEL) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getBatteryLevel();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): batteryLevel = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewBatteryLevel.setText("E");
                return false;
            } else {
                updateBatteryLevel(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_LOGGER_STATE) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.LoggerStateTypes loggerStateType = tagAxzonOpus.getLoggerStateType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): loggerStateType = " + loggerStateType.toString());
            if (loggerStateType == null) {
                textViewLoggerState.setText("E");
                return false;
            } else {
                updateLoggerState(loggerStateType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_CLOCK) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getClock();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): RTC = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewClock.setText("E");
                return false;
            } else {
                updateClock(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_NEXT_LOG_ADDRESS) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getNextLogAddress();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): nextLogAddress = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewNextLogAddress.setText("E");
                return false;
            } else {
                 updateNextLogAddress(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_TID_ALARM) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.TidAlarmTypes tidAlarmType = tagAxzonOpus.getTidAlarmType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): epcAlarmType = " + (tidAlarmType == null ? "null" : tidAlarmType.toString()));
            if (tidAlarmType == null) {
                textViewTidAlarm.setText("E");
                return false;
            } else {
                updateTidAlarm(tidAlarmType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_FINGER_ARMED_CLOCK) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getFingerArmedClock();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): fingerArmedAddress = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewFingerArmedClock.setText("E");
                return false;
            } else {
                updateFingerArmedClock(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_FIRST_TAMPER_ALARM_ADDRESS) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getFirstTamperAlarmAddress();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): firstTamperAlarmAddress = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewFirstTamperAlarmAddress.setText("E");
                return false;
            } else {
                updateFirstTamperAlarmAddress(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_FIRST_TEMPERATURE_ALARM_ADDRESS) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getFirstTemperatureAlarmAddress();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): firstTemperatureAlarmddress = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewFirstTemperatureAlarmAddress.setText("E");
                return false;
            } else {
                updateFirstTemperatureAlarmAddress(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALARM_LOWER_DELAYED) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getAlarmLowerDelayed();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): alarmLowerDelayed = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewAlarmLowerDelayed.setText("E");
                return false;
            } else {
                updateAlarmLowerDelayed(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALARM_UPPER_DELAYED) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getAlarmUpperDelayed();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): alarmUpperDelayed = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewAlarmUpperDelayed.setText("E");
                return false;
            } else {
                updateAlarmUpperDelayed(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_INITIAL_BATTERY_LOW_ALARM) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.DisableEnableTypes batteryLowAlarmType = tagAxzonOpus.getInitialBatteryLowAlarmType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): batteryLowAlarmType = " + (batteryLowAlarmType == null ? "null" : batteryLowAlarmType.toString()));
            if (batteryLowAlarmType == null) {
                textViewInitialBatteryLowAlarm.setText("E");
                return false;
            } else {
                updateInitialBatteryLowAlarm(batteryLowAlarmType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_EPC_ALARM) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.EpcAlarmTypes epcAlarmType = tagAxzonOpus.getEpcAlarmType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): epcAlarmType = " + (epcAlarmType == null ? "null" : "valid"));
            if (epcAlarmType == null) {
                textViewEpcAlarm.setText("E");
                return false;
            } else {
                updateEpcAlarm(epcAlarmType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_LOGGING_INTERVAL) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.LoggingIntervalTypes loggingIntervalType = tagAxzonOpus.getLoggingIntervalType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): LoggingIntervalType = " + (loggingIntervalType == null ? "null" : loggingIntervalType.toString()));
            if (loggingIntervalType == null) {
                textViewLoggingInterval.setText("E");
                return false;
            } else {
                updateLoggingInterval(loggingIntervalType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_FINGERSPOT_STARTUP) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.FingerSpotStartupEnables fingerSpotStartupType = tagAxzonOpus.getFingerSpotStartupEnables();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): fingerSpotType = " + (fingerSpotStartupType == null ? "null" : "valid"));
            if (fingerSpotStartupType == null) {
                textViewFingerSpotStartup.setText("E");
                return false;
            } else {
                MainActivity.csLibrary4A.appendToLog("AccessOpusLoggerFragment.processResult: going to updateFingerSpotStartup");
                updateFingerSpotStartup(fingerSpotStartupType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALRAM_UPPER_LIMIT) {
            readWriteTypes = ReadWriteTypes.NULL;
            float fValue = tagAxzonOpus.getAlarmUpperLimit();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): AlarmUpperLimit = " + fValue);
            if (fValue == tagAxzonOpus.fNO_SUCH_SETTING) {
                textViewAlarmUpperLimit.setText("E");
                return false;
            } else {
                updateAlarmUpperLimit(fValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALRAM_LOWER_LIMIT) {
            readWriteTypes = ReadWriteTypes.NULL;
            float fValue = tagAxzonOpus.getAlarmLowerLimit();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): AlarmLowerLimit = " + fValue);
            if (fValue == tagAxzonOpus.fNO_SUCH_SETTING) {
                textViewAlarmLowerLimit.setText("E");
                return false;
            } else {
                updateAlarmLowerLimit(fValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_SAMPLING_REGIME_PERIOD) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getSamplingRegimePeriod();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): samplingRegimePeriod = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewSamplingRegimePeriod.setText("E");
                return false;
            } else {
                updateSamplingRegimePeriod(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALRAM_UPPER_DELAY) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getAlarmUpperDelay();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): AlarmUpperDelay = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewAlarmUpperDelay.setText("E");
                return false;
            } else {
                updateAlarmUpperDelay(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_ALRAM_LOWER_DELAY) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getAlarmLowerDelay();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): AlarmLowerDelay = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewAlarmLowerDelay.setText("E");
                return false;
            } else {
                updateAlarmLowerDelay(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_DELAYED_LOGGING_START) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getDelayedLoggingStart();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): delayedLoggingStart = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewLoggingDelayedStart.setText("E");
                return false;
            } else {
                updateLoggingDelayedStart(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_LOGGER_ARMED_SECOND) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getLoggerArmedSecond();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): loggerArmedClock = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewLoggerArmedSecond.setText("E");
                return false;
            } else {
                updateLoggerArmedSecond(iValue);
                return true;
            }


        } else if (readWriteTypes == ReadWriteTypes.USERCODE_MINBATTERY_4ARMING) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getMinBattery4Arming();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): minBattery4Arming = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewMinBattery4Arming.setText("E");
                return false;
            } else {
                updateMinBattery4Arming(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_MINBATTERY_4LOGGING) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getMinBattery4Logging();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): minBattery4Logging = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewMinBattery4Logging.setText("E");
                return false;
            } else {
                updateMinBattery4Logging(iValue);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_SAMPLENUMBER_TOLOG) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.SampleNumberToLogTypes sampleNumberToLogType = tagAxzonOpus.getSampleNumberToLog();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): sampleNumberToLogType = " + (sampleNumberToLogType == null ? "null" : sampleNumberToLogType.toString()));
            if (sampleNumberToLogType == null) {
                textViewLoggingSampleSize.setText("E");
                return false;
            } else {
                updateLoggingSampleSize(sampleNumberToLogType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_FINGERSPOT_LED) {
            readWriteTypes = ReadWriteTypes.NULL;
            TagAxzonOpus.DisableEnableTypes fingerSpotLedType = tagAxzonOpus.getFingerSpotLedType();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): fingerSpotLedType = " + fingerSpotLedType.toString());
            if (fingerSpotLedType == null) {
                textViewFingerSpotLed.setText("E");
                return false;
            } else {
                updateFingerSpotLed(fingerSpotLedType);
                return true;
            }
        } else if (readWriteTypes == ReadWriteTypes.USERCODE_BAP_DURATION) {
            readWriteTypes = ReadWriteTypes.NULL;
            int iValue = tagAxzonOpus.getBAPduration();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment().processResult(): BAPduration = " + iValue);
            if (iValue == tagAxzonOpus.iNO_SUCH_SETTING) {
                textViewBAPduration.setText("E");
                return false;
            } else {
                updateBAPduration(iValue);
                return true;
            }
        }
        return false;
        /*String accessResult = null;
        if (false && accessTask == null) return false;
        else if (false && accessTask.getStatus() != CustomAsyncTask.Status.FINISHED) return false;
        else {
            if (changedSelectIndex) {
                changedSelectIndex = false; MainActivity.selectFor = 0;
                MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
            }
            accessResult = accessTask.accessResult;
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.USERCODE1) {
                    textViewUserCode1OK.setText("E");
                    //checkBoxUserCode1.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE2) {
                    textViewUserCode2OK.setText("E");
                    //checkBoxUserCode2.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE3) {
                    textViewUserCode3OK.setText("E");
                    //checkBoxUserCode3.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE4) {
                    textViewUserCode4OK.setText("E");
                    //checkBoxUserCode4.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE5) {
                    textViewUserCode5OK.setText("E");
                    //checkBoxUserCode5.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE_LOGGING_INTERVAL) {
                    textViewLoggingInterval.setText("E");
                    //textViewLoggingInterval.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.USERCODE1) {
                    textViewUserCode1OK.setText("O");
                    //checkBoxUserCode1.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode1 = String.valueOf(iValue);
                        editTextUserCode1.setText(strReadUserCode1);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE2) {
                    textViewUserCode2OK.setText("O");
                    //checkBoxUserCode2.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  setTemperatureCode(accessResult, 0);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE3) {
                    textViewUserCode3OK.setText("O");
                    //checkBoxUserCode3.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead)  setTemperatureCode(accessResult, 1);
                } else if (readWriteTypes == ReadWriteTypes.USERCODE4) {
                    textViewUserCode4OK.setText("O");
                    //checkBoxUserCode4.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode4 = String.valueOf(iValue);
                        editTextUserCode4.setText(strReadUserCode4);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE5) {
                    textViewUserCode5OK.setText("O");
                    //checkBoxUserCode5.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        int iValue = Integer.parseInt(accessResult, 16);
                        strReadUserCode5 = String.valueOf(iValue);
                        editTextUserCode5.setText(strReadUserCode5);
                    }
                } else if (readWriteTypes == ReadWriteTypes.USERCODE_LOGGING_INTERVAL) {
                    textViewLoggingInterval.setText("O");
                    //textViewLoggingInterval.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        //int iValue = Integer.parseInt(accessResult, 16);
                        strReadLoggingInterval = accessResult; //String.valueOf(iValue);
                        //editTextLoggingInterval.setText(strReadLoggingInterval);
                    }
                }
            }
            accessTask = null;
            return true;
        }
         */
    }
    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accSize = 0, accOffset = 0;
        String writeData = "";

        MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: Start, editTextRWTagID = " + editTextRWTagID.getText().toString()
                + ", checkProcessing = " + checkProcessing
                + ", modelCode == " + modelCode);

        if (editTextRWTagID.getText().toString().length() == 0) invalidRequest1 = true;
        else if (checkBoxBatteryLevel.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_BATTERY_LEVEL.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_BATTERY_LEVEL;
            checkProcessing = ReadWriteTypes.USERCODE_BATTERY_LEVEL.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getBatteryLevel();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: batteryLevel is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateBatteryLevel(iValue);
                } else {
                    textViewBatteryLevel.setText("");
                    editTextBatteryLevel.setText("");
                }
            } else {
            }
        } else if (checkBoxLoggerState.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_LOGGER_STATE.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_LOGGER_STATE; checkProcessing = ReadWriteTypes.USERCODE_LOGGER_STATE.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.LoggerStateTypes loggerStateType = tagAxzonOpus.getLoggerStateType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: loggerStateType is " + (loggerStateType == null ? "null" : loggerStateType.toString()));
                if (loggerStateType != null) {
                    updateLoggerState(loggerStateType);
                } else {
                    textViewLoggerState.setText("");
                    spinnerLoggerState.setSelection(0);
                }
            } else {
            }
        } else if (checkBoxClock.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_CLOCK.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_CLOCK; checkProcessing = ReadWriteTypes.USERCODE_CLOCK.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getClock();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: RTC is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateClock(iValue);
                } else {
                    textViewClock.setText("");
                    editTextClock.setText("");
                }
            } else {
            }
        } else if (checkBoxNextLogAddress.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_NEXT_LOG_ADDRESS.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_NEXT_LOG_ADDRESS; checkProcessing = ReadWriteTypes.USERCODE_NEXT_LOG_ADDRESS.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getNextLogAddress();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: nextLogAddress is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateNextLogAddress(iValue);
                } else {
                    textViewNextLogAddress.setText("");
                    editTextNextLogAddress.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextNextLogAddress.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                //else if (tagAxzonOpus.getNextLogAddress() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with nextLogAddress = " + iValue);
                    tagAxzonOpus.setNextLogAddress(iValue);
                    textViewNextLogAddress.setText("");
                    editTextNextLogAddress.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewNextLogAddress.setText("E");
                    checkBoxNextLogAddress.setChecked(false);
                }
            }
        } else if (checkBoxTidAlarm.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_TID_ALARM.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_TID_ALARM; checkProcessing = ReadWriteTypes.USERCODE_TID_ALARM.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.TidAlarmTypes tidAlarmType = tagAxzonOpus.getTidAlarmType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: epcAlarmType is " + (tidAlarmType == null ? "null" : tidAlarmType.toString()));
                if (tidAlarmType != null) {
                    updateTidAlarm(tidAlarmType);
                } else {
                    textViewTidAlarm.setText("");
                    spinnerTidAlarm.setSelection(0);
                }
            } else {
                TagAxzonOpus.TidAlarmTypes tidAlarmType = new TagAxzonOpus.TidAlarmTypes();
                if (checkBoxTidAlarmLowTemperature.isChecked()) tidAlarmType.lowTemperatureAlarm = 1;
                else tidAlarmType.lowTemperatureAlarm = 0;
                if (checkBoxTidAlarmHighTemperature.isChecked()) tidAlarmType.highTemperatureAlarm = 1;
                else tidAlarmType.highTemperatureAlarm = 0;
                if (checkBoxTidAlarmTamper.isChecked()) tidAlarmType.tamperAlarm = 1;
                else tidAlarmType.tamperAlarm = 0;
                if (checkBoxTidAlarmBattery.isChecked()) tidAlarmType.batteryAlarm = 1;
                else tidAlarmType.batteryAlarm = 0;

                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with tidAlarmType");
                tagAxzonOpus.setTidAlarmType(tidAlarmType);
                textViewTidAlarm.setText("");
                spinnerTidAlarm.setSelection(0);
                checkBoxTidAlarmBattery.setChecked(false);
                checkBoxTidAlarmTamper.setChecked(false);
                checkBoxTidAlarmLowTemperature.setChecked(false);
                checkBoxTidAlarmHighTemperature.setChecked(false);
            }
        } else if (checkBoxFingerArmedClock.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_FINGER_ARMED_CLOCK.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_FINGER_ARMED_CLOCK; checkProcessing = ReadWriteTypes.USERCODE_FINGER_ARMED_CLOCK.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getFingerArmedClock();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: fingerArmedAddress is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateFingerArmedClock(iValue);
                } else {
                    textViewFingerArmedClock.setText("");
                    editTextFingerArmedClock.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextFingerArmedClock.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                //else if (tagAxzonOpus.getFingerArmedClock() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with fingerArmedClock = " + iValue);
                    tagAxzonOpus.setFingerArmedClock(iValue);
                    textViewFingerArmedClock.setText("");
                    editTextFingerArmedClock.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewFingerArmedClock.setText("E");
                    checkBoxFingerArmedClock.setChecked(false);
                }
            }
        } else if (checkBoxFirstTamperAlarmAddress.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_FIRST_TAMPER_ALARM_ADDRESS.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_FIRST_TAMPER_ALARM_ADDRESS; checkProcessing = ReadWriteTypes.USERCODE_FIRST_TAMPER_ALARM_ADDRESS.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getFirstTamperAlarmAddress();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: firstTamperAlarmAddress is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateFirstTamperAlarmAddress(iValue);
                } else {
                    textViewFirstTamperAlarmAddress.setText("");
                    editTextFirstTamperAlarmAddress.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextFirstTamperAlarmAddress.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                    //else if (tagAxzonOpus.getFirstTamperAlarmAddress() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with firstTamperAlarmAddress = " + iValue);
                    tagAxzonOpus.setFirstTamperAlarmAddress(iValue);
                    textViewFirstTamperAlarmAddress.setText("");
                    editTextFirstTamperAlarmAddress.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewFirstTamperAlarmAddress.setText("E");
                    checkBoxFirstTamperAlarmAddress.setChecked(false);
                }
            }
        } else if (checkBoxFirstTemperatureAlarmAddress.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_FIRST_TEMPERATURE_ALARM_ADDRESS.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_FIRST_TEMPERATURE_ALARM_ADDRESS; checkProcessing = ReadWriteTypes.USERCODE_FIRST_TEMPERATURE_ALARM_ADDRESS.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getFirstTemperatureAlarmAddress();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: firstTemperatureAlarmAddress is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateFirstTemperatureAlarmAddress(iValue);
                } else {
                    textViewFirstTemperatureAlarmAddress.setText("");
                    editTextFirstTemperatureAlarmAddress.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextFirstTemperatureAlarmAddress.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                    //else if (tagAxzonOpus.getFirstTemperatureAlarmAddress() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with firstTemperatureAlarmAddress = " + iValue);
                    tagAxzonOpus.setFirstTemperatureAlarmAddress(iValue);
                    textViewFirstTemperatureAlarmAddress.setText("");
                    editTextFirstTemperatureAlarmAddress.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewFirstTemperatureAlarmAddress.setText("E");
                    checkBoxFirstTemperatureAlarmAddress.setChecked(false);
                }
            }
        } else if (checkBoxAlarmLowerDelayed.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALARM_LOWER_DELAYED.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALARM_LOWER_DELAYED; checkProcessing = ReadWriteTypes.USERCODE_ALARM_LOWER_DELAYED.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getAlarmLowerDelayed();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: alarmLowerDelayed is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateAlarmLowerDelayed(iValue);
                } else {
                    textViewAlarmLowerDelayed.setText("");
                    editTextAlarmLowerDelayed.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextAlarmLowerDelayed.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                    //else if (tagAxzonOpus.getAlarmLowerDelayed() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmLowerDelayed = " + iValue);
                    tagAxzonOpus.setAlarmLowerDelayed(iValue);
                    textViewAlarmLowerDelayed.setText("");
                    editTextAlarmLowerDelayed.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmLowerDelayed.setText("E");
                    checkBoxAlarmLowerDelayed.setChecked(false);
                }
            }
        } else if (checkBoxAlarmUpperDelayed.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALARM_UPPER_DELAYED.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALARM_UPPER_DELAYED; checkProcessing = ReadWriteTypes.USERCODE_ALARM_UPPER_DELAYED.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getAlarmUpperDelayed();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: alarmUpperDelayed is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateAlarmUpperDelayed(iValue);
                } else {
                    textViewAlarmUpperDelayed.setText("");
                    editTextAlarmUpperDelayed.setText("");
                }
            } else {
                int iValue = -1;
                try {
                    iValue = Integer.parseInt(editTextAlarmUpperDelayed.getText().toString());
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue < 0) invalidRequest1 = true;
                //else if (tagAxzonOpus.getAlarmUpperDelayed() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmUpperDelayed = " + iValue);
                    tagAxzonOpus.setAlarmUpperDelayed(iValue);
                    textViewAlarmUpperDelayed.setText("");
                    editTextAlarmUpperDelayed.setText("");
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmUpperDelayed.setText("E");
                    checkBoxAlarmUpperDelayed.setChecked(false);
                }
            }
        } else if (checkBoxInitialBatteryLowAlarm.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_INITIAL_BATTERY_LOW_ALARM.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_INITIAL_BATTERY_LOW_ALARM; checkProcessing = ReadWriteTypes.USERCODE_INITIAL_BATTERY_LOW_ALARM.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.DisableEnableTypes batteryLowAlarmType = tagAxzonOpus.getInitialBatteryLowAlarmType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: batteryLowAlarmType is " + (batteryLowAlarmType == null ? "null" : batteryLowAlarmType.toString()));
                if (batteryLowAlarmType != null) {
                    updateInitialBatteryLowAlarm(batteryLowAlarmType);
                } else {
                    textViewInitialBatteryLowAlarm.setText("");
                    spinnerInitialBatteryLowAlarm.setSelection(0);
                }
            } else {
                int iValue = spinnerInitialBatteryLowAlarm.getSelectedItemPosition();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue <= 0) invalidRequest1 = true;
                    //else if (tagAxzonOpus.getInitialBatteryLowAlarmType() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with initialBatteryLowAlarmType = " + iValue);
                    tagAxzonOpus.setInitialBatteryLowAlarmType(iValue > 1);
                    textViewInitialBatteryLowAlarm.setText("");
                    spinnerInitialBatteryLowAlarm.setSelection(0);
                }
                if (invalidRequest1) {
                    if (iValue < 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewInitialBatteryLowAlarm.setText("E");
                    checkBoxInitialBatteryLowAlarm.setChecked(false);
                }
            }
        } else if (checkBoxEpcAlarm.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_EPC_ALARM.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_EPC_ALARM; checkProcessing = ReadWriteTypes.USERCODE_EPC_ALARM.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.EpcAlarmTypes epcAlarmType = tagAxzonOpus.getEpcAlarmType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: epcAlarmType is " + (epcAlarmType == null ? "null" : epcAlarmType.toString()));
                if (epcAlarmType != null) {
                    updateEpcAlarm(epcAlarmType);
                } else {
                    textViewEpcAlarm.setText("");
                    spinnerEpcAlarm.setSelection(0);
                }
            } else {
                TagAxzonOpus.EpcAlarmTypes epcAlarmType = new TagAxzonOpus.EpcAlarmTypes();
                if (checkBoxEpcAlarmTamper.isChecked()) epcAlarmType.tamperAlarm = 1;
                else epcAlarmType.tamperAlarm = 0;
                if (checkBoxEpcAlarmTemperature.isChecked()) epcAlarmType.temperatureAlarm = 1;
                else epcAlarmType.temperatureAlarm = 0;
                if (checkBoxEpcAlarmBattery.isChecked()) epcAlarmType.batteryAlarm = 1;
                else epcAlarmType.batteryAlarm = 0;

                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with epcAlarmType");
                tagAxzonOpus.setEpcAlarmType(epcAlarmType);
                textViewEpcAlarm.setText("");
                spinnerEpcAlarm.setSelection(0);
                checkBoxEpcAlarmTamper.setChecked(false);
                checkBoxEpcAlarmBattery.setChecked(false);
                checkBoxEpcAlarmTemperature.setChecked(false);
                checkBoxEpcAlarmBatteryConnected.setChecked(false);
                checkBoxEpcAlarmBatteryInstalled.setChecked(false);
            }
        } else if (checkBoxLoggingInterval.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_LOGGING_INTERVAL.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_LOGGING_INTERVAL; checkProcessing = ReadWriteTypes.USERCODE_LOGGING_INTERVAL.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.LoggingIntervalTypes loggingIntervalType = tagAxzonOpus.getLoggingIntervalType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: loggingIntervalType is " + (loggingIntervalType == null ? "null" : loggingIntervalType.toString()));
                if (loggingIntervalType != null) {
                    updateLoggingInterval(loggingIntervalType);
                } else {
                    textViewLoggingInterval.setText("");
                    spinnerLoggingInterval.setSelection(0);
                }
            } else {
                int iValue = spinnerLoggingInterval.getSelectedItemPosition();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with iValue = " + iValue);
                if (iValue == 0) invalidRequest1 = true;
                else if (tagAxzonOpus.getLoggingIntervalType() == null) invalidRequest1 = true;
                else {
                    TagAxzonOpus.LoggingIntervalTypes loggingIntervalType = TagAxzonOpus.LoggingIntervalTypes.values()[iValue-1];
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with loggingIntervalType = " + loggingIntervalType.toString());
                    tagAxzonOpus.setLoggingIntervalType(loggingIntervalType);
                    textViewLoggingInterval.setText("");
                    spinnerLoggingInterval.setSelection(0);
                }
                if (invalidRequest1) {
                    if (iValue == 0) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewLoggingInterval.setText("E");
                    checkBoxLoggingInterval.setChecked(false);
                }
            }
        } else if (checkBoxFingerSpotStartup.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_FINGERSPOT_STARTUP.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_FINGERSPOT_STARTUP; checkProcessing = ReadWriteTypes.USERCODE_FINGERSPOT_STARTUP.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.FingerSpotStartupEnables fingerSpotStartupType = tagAxzonOpus.getFingerSpotStartupEnables();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: fingerSpotStartupType is " + (fingerSpotStartupType == null ? "null" : "valid"));
                if (fingerSpotStartupType != null) {
                    MainActivity.csLibrary4A.appendToLog("AccessOpusLoggerFragment.processTickItems: going to updateFingerSpotStartup");
                    updateFingerSpotStartup(fingerSpotStartupType);
                } else {
                    textViewFingerSpotStartup.setText("");
                    spinnerFingerSpotStartup.setSelection(0);
                }
            } else {
                if (tagAxzonOpus.getFingerSpotStartupEnables() == null) invalidRequest1 = true;
                else {
                    TagAxzonOpus.FingerSpotStartupEnables fingerSpotStartupEnables = new TagAxzonOpus.FingerSpotStartupEnables();
                    if (checkBoxFingerSpotStartEnable.isChecked())
                        fingerSpotStartupEnables.fingerSpotStartEnable = 1;
                    if (checkBoxTamperDetectEnable.isChecked())
                        fingerSpotStartupEnables.tamperDetectEnable = 1;
                    if (checkBoxTamperDisconnectPolarity.isChecked())
                        fingerSpotStartupEnables.tamperDisconnectPolarity = 1;
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with fingerSpotStartupEnables");
                    tagAxzonOpus.setFingerSpotStartupEnables(fingerSpotStartupEnables);
                    textViewFingerSpotStartup.setText("");
                    spinnerFingerSpotStartup.setSelection(0);
                }
                if (invalidRequest1) {
                    Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewFingerSpotStartup.setText("E");
                    checkBoxFingerSpotStartup.setChecked(false);
                }
            }
        } else if (checkBoxAlarmUpperLimit.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALRAM_UPPER_LIMIT.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALRAM_UPPER_LIMIT; checkProcessing = ReadWriteTypes.USERCODE_ALRAM_UPPER_LIMIT.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                float fValue = tagAxzonOpus.getAlarmUpperLimit();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: fValue is " + fValue);
                if (fValue != tagAxzonOpus.fNO_SUCH_SETTING) {
                    updateAlarmUpperLimit(fValue);
                } else {
                    textViewAlarmUpperLimit.setText("");
                    editTextAlarmUpperLimit.setText("");
                }
            } else {
                String string = editTextAlarmUpperLimit.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                float fValue = tagAxzonOpus.fNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    fValue = Float.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", fValue = " + fValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getAlarmUpperLimit() == tagAxzonOpus.fNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmUpperLimit = " + fValue);
                    tagAxzonOpus.setAlarmUpperLimit(fValue);
                    textViewAlarmUpperLimit.setText("");
                    editTextAlarmUpperLimit.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmUpperLimit.setText("E");
                    checkBoxAlarmUpperLimit.setChecked(false);
                }
            }
        } else if (checkBoxAlarmLowerLimit.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALRAM_LOWER_LIMIT.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALRAM_LOWER_LIMIT; checkProcessing = ReadWriteTypes.USERCODE_ALRAM_LOWER_LIMIT.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                float fValue = tagAxzonOpus.getAlarmLowerLimit();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: fValue is " + fValue);
                if (fValue != tagAxzonOpus.fNO_SUCH_SETTING) {
                    updateAlarmLowerLimit(fValue);
                } else {
                    textViewAlarmLowerLimit.setText("");
                    editTextAlarmLowerLimit.setText("");
                }
            } else {
                String string = editTextAlarmLowerLimit.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                float fValue = tagAxzonOpus.fNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    fValue = Float.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", fValue = " + fValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getAlarmLowerLimit() == tagAxzonOpus.fNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmUpperLimit = " + fValue);
                    tagAxzonOpus.setAlarmLowerLimit(fValue);
                    textViewAlarmLowerLimit.setText("");
                    editTextAlarmLowerLimit.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmLowerLimit.setText("E");
                    checkBoxAlarmLowerLimit.setChecked(false);
                }
            }
        } else if (checkBoxSamplingRegimePeriod.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_SAMPLING_REGIME_PERIOD.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_SAMPLING_REGIME_PERIOD; checkProcessing = ReadWriteTypes.USERCODE_SAMPLING_REGIME_PERIOD.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getSamplingRegimePeriod();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: iValue is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateSamplingRegimePeriod(iValue);
                } else {
                    textViewSamplingRegimePeriod.setText("");
                    editTextSamplingRegimePeriod.setText("");
                }
            } else {
                String string = editTextSamplingRegimePeriod.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                int iValue = tagAxzonOpus.iNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    iValue = Integer.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", iValue = " + iValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getSamplingRegimePeriod() == tagAxzonOpus.fNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with samplingRegimePeriod = " + iValue);
                    tagAxzonOpus.setSamplingRegimePeriod(iValue);
                    textViewSamplingRegimePeriod.setText("");
                    editTextSamplingRegimePeriod.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewSamplingRegimePeriod.setText("E");
                    checkBoxSamplingRegimePeriod.setChecked(false);
                }
            }
        } else if (checkBoxAlarmUpperDelay.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALRAM_UPPER_DELAY.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALRAM_UPPER_DELAY; checkProcessing = ReadWriteTypes.USERCODE_ALRAM_UPPER_DELAY.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getAlarmUpperDelay();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: iValue is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateAlarmUpperDelay(iValue);
                } else {
                    textViewAlarmUpperDelay.setText("");
                    editTextAlarmUpperDelay.setText("");
                }
            } else {
                String string = editTextAlarmUpperDelay.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                int iValue = tagAxzonOpus.iNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    iValue = Integer.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", iValue = " + iValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getAlarmUpperDelay() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmUpperDelay = " + iValue);
                    tagAxzonOpus.setAlarmUpperDelay(iValue);
                    textViewAlarmUpperDelay.setText("");
                    editTextAlarmUpperDelay.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmUpperDelay.setText("E");
                    checkBoxAlarmUpperDelay.setChecked(false);
                }
            }
        } else if (checkBoxAlarmLowerDelay.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_ALRAM_LOWER_DELAY.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_ALRAM_LOWER_DELAY; checkProcessing = ReadWriteTypes.USERCODE_ALRAM_LOWER_DELAY.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getAlarmLowerDelay();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: iValue is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateAlarmLowerDelay(iValue);
                } else {
                    textViewAlarmLowerDelay.setText("");
                    editTextAlarmLowerDelay.setText("");
                }
            } else {
                String string = editTextAlarmLowerDelay.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                int iValue = tagAxzonOpus.iNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    iValue = Integer.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", iValue = " + iValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getAlarmUpperDelay() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with alarmUpperDelay = " + iValue);
                    tagAxzonOpus.setAlarmLowerDelay(iValue);
                    textViewAlarmLowerDelay.setText("");
                    editTextAlarmLowerDelay.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewAlarmLowerDelay.setText("E");
                    checkBoxAlarmLowerDelay.setChecked(false);
                }
            }
        } else if (checkBoxLoggingDelayedStart.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_DELAYED_LOGGING_START.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_DELAYED_LOGGING_START; checkProcessing = ReadWriteTypes.USERCODE_DELAYED_LOGGING_START.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getDelayedLoggingStart();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: delayedLoggingStart is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateLoggingDelayedStart(iValue);
                } else {
                    textViewLoggingDelayedStart.setText("");
                    editTextLoggingDelayedStart.setText("");
                }
            } else {
                String string = editTextLoggingDelayedStart.getText().toString();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with string = " + string);
                int iValue = tagAxzonOpus.iNO_SUCH_SETTING;
                boolean bInvalid = true;
                try {
                    iValue = Integer.valueOf(string);
                    bInvalid = false;
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with bInvalid = " + bInvalid + ", iValue = " + iValue);
                if (bInvalid) invalidRequest1 = true;
                else if (tagAxzonOpus.getDelayedLoggingStart() == tagAxzonOpus.iNO_SUCH_SETTING) invalidRequest1 = true;
                else {
                    MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: write with delayedLoggingStart = " + iValue);
                    tagAxzonOpus.setDelayedLoggingStart(iValue);
                    textViewLoggingDelayedStart.setText("");
                    editTextLoggingDelayedStart.setText("");
                }
                if (invalidRequest1) {
                    if (bInvalid) Toast.makeText(MainActivity.context, "Invalid data", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(MainActivity.context, "Reading before writing", Toast.LENGTH_SHORT).show();
                    textViewLoggingDelayedStart.setText("E");
                    checkBoxLoggingDelayedStart.setChecked(false);
                }
            }
        } else if (checkBoxLoggerArmedSecond.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_LOGGER_ARMED_SECOND.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_LOGGER_ARMED_SECOND; checkProcessing = ReadWriteTypes.USERCODE_LOGGER_ARMED_SECOND.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getLoggerArmedSecond();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: loggerArmedSecond is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateLoggerArmedSecond(iValue);
                } else {
                    textViewLoggerArmedSecond.setText("");
                    editTextLoggerArmedSecond.setText("");
                }
            } else {
            }


        } else if (checkBoxMinBattery4Arming.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_MINBATTERY_4ARMING.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_MINBATTERY_4ARMING; checkProcessing = ReadWriteTypes.USERCODE_MINBATTERY_4ARMING.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getMinBattery4Arming();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: minBattery4Arming is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateMinBattery4Arming(iValue);
                } else {
                    textViewMinBattery4Arming.setText("");
                    editTextMinBattery4Arming.setText("");
                }
            } else {
            }
        } else if (checkBoxMinBattery4Logging.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_MINBATTERY_4LOGGING.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_MINBATTERY_4LOGGING; checkProcessing = ReadWriteTypes.USERCODE_MINBATTERY_4LOGGING.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getMinBattery4Logging();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: minBattery4Logging is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateMinBattery4Logging(iValue);
                } else {
                    textViewMinBattery4Logging.setText("");
                    editTextMinBattery4Logging.setText("");
                }
            } else {
            }
        } else if (checkBoxLoggingSampleSize.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_SAMPLENUMBER_TOLOG.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_SAMPLENUMBER_TOLOG; checkProcessing = ReadWriteTypes.USERCODE_SAMPLENUMBER_TOLOG.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.SampleNumberToLogTypes sampleNumberToLogType = tagAxzonOpus.getSampleNumberToLog();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: sampleNumberToLog is " + (sampleNumberToLogType == null ? "null" : sampleNumberToLogType.toString()));
                if (sampleNumberToLogType != null) {
                    updateLoggingSampleSize(sampleNumberToLogType);
                } else {
                    textViewLoggingSampleSize.setText("");
                    spinnerLoggingSampleSize.setSelection(0);
                }
            } else {
            }
        } else if (checkBoxFingerSpotLed.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_FINGERSPOT_LED.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_FINGERSPOT_LED; checkProcessing = ReadWriteTypes.USERCODE_FINGERSPOT_LED.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                TagAxzonOpus.DisableEnableTypes fingerSpotLedType = tagAxzonOpus.getFingerSpotLedType();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: fingerSpotLedType is " + (fingerSpotLedType == null ? "null" : fingerSpotLedType.toString()));
                if (fingerSpotLedType != null) {
                    updateFingerSpotLed(fingerSpotLedType);
                } else {
                    textViewFingerSpotLed.setText("");
                    spinnerFingerSpotLed.setSelection(0);
                }
            } else {
            }
        } else if (checkBoxBAPduration.isChecked() && checkProcessing < ReadWriteTypes.USERCODE_BAP_DURATION.ordinal() && modelCode == 50) {
            readWriteTypes = ReadWriteTypes.USERCODE_BAP_DURATION; checkProcessing = ReadWriteTypes.USERCODE_BAP_DURATION.ordinal();
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: " + readWriteTypes.toString() + "-" + readWriteTypes.ordinal() + ", operationRead is " + operationRead);
            if (operationRead) {
                int iValue = tagAxzonOpus.getBAPduration();
                MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: BAPduration is " + iValue);
                if (iValue != tagAxzonOpus.iNO_SUCH_SETTING) {
                    updateBAPduration(iValue);
                } else {
                    textViewBAPduration.setText("");
                    editTextBAPduration.setText("");
                }
            } else {
            }
        } else {
            invalidRequest1 = true;
        }
/*
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                invalidRequest1 = true;
            }
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: 2, invalidRequest1 is " + invalidRequest1);
        }
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (MainActivity.csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: 3, invalidRequest1 is " + invalidRequest1);
        }
        if (invalidRequest1 == false && operationRead == false) {
            if (invalidRequest1 == false) {
                if (MainActivity.csLibrary4A.setAccessWriteData(writeData) == false) {
                    invalidRequest1 = true;
                }
            }
            MainActivity.csLibrary4A.appendToLog("AccessXerxesLoggerFragment.processTickItems: 4, invalidRequest1 is " + invalidRequest1);
        }
*/
        return invalidRequest1;
    }
}
