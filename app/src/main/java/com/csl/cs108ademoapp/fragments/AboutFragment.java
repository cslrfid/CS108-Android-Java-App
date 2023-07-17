package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csl.cs108ademoapp.BuildConfig;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AboutFragment extends CommonFragment {
    Handler mHandler = new Handler();
    long timeMillis = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_about);
        actionBar.setTitle(R.string.title_activity_about);

        TextView appVersionView = (TextView) getActivity().findViewById(R.id.appVersion);
        appVersionView.setText(BuildConfig.VERSION_NAME);
        TextView libVersionView = (TextView) getActivity().findViewById(R.id.libVersion);
        libVersionView.setText(MainActivity.csLibrary4A.getlibraryVersion());

        MainActivity.mSensorConnector.mLocationDevice.turnOn(true);
        MainActivity.mSensorConnector.mSensorDevice.turnOn(true);
        mHandler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        MainActivity.mSensorConnector.mLocationDevice.turnOn(false);
        MainActivity.mSensorConnector.mSensorDevice.turnOn(false);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    public AboutFragment() {
        super("AboutFragment");
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            TextView timeStampView = (TextView) getActivity().findViewById(R.id.timeStamp);
            TextView locationView = (TextView) getActivity().findViewById(R.id.GeoLocation);
            TextView eCompassView = (TextView) getActivity().findViewById(R.id.eCompass);

            TextView radioVersion = (TextView) getActivity().findViewById(R.id.radioVersion);
            TextView modelVersion = (TextView) getActivity().findViewById(R.id.modelVersion);
            TextView moduleRfidOnStatus = (TextView) getActivity().findViewById(R.id.moduleRfid_onStatus);
            TextView moduleBarOnStatus = (TextView) getActivity().findViewById(R.id.moduleBar_onStatus);
            TextView triggerOnStatus = (TextView) getActivity().findViewById(R.id.trigger_onStatus);
            TextView moduleBattery = (TextView) getActivity().findViewById(R.id.module_battery);
            TextView moduleVersion = (TextView) getActivity().findViewById(R.id.module_versoin);
            TextView radioSerial = (TextView) getActivity().findViewById(R.id.radio_serialnumber);
            TextView radioBoardVersion = (TextView) getActivity().findViewById(R.id.radio_boardVersion);
            TextView productSerial = (TextView) getActivity().findViewById(R.id.module_productserialnumber);
            TextView boardVersion = (TextView) getActivity().findViewById(R.id.module_boardversion);
            TextView barcodeSerial = (TextView) getActivity().findViewById(R.id.moduleBar_serialNumber);
            TextView barcodeDate = (TextView) getActivity().findViewById(R.id.moduleBar_date);
            TextView barcodeVersion = (TextView) getActivity().findViewById(R.id.moduleBar_version);

            TextView bluetoothVersion = (TextView) getActivity().findViewById(R.id.bluetooth_version);
            TextView bluetoothAddress = (TextView) getActivity().findViewById(R.id.bluetooth_address);
            TextView bluetoothRssi = (TextView) getActivity().findViewById(R.id.bluetooth_rssi);

            timeStampView.setText(MainActivity.mSensorConnector.getTimeStamp());
            locationView.setText(MainActivity.mSensorConnector.mLocationDevice.getLocation());
            eCompassView.setText(MainActivity.mSensorConnector.mSensorDevice.getEcompass());
            if (MainActivity.csLibrary4A.isBleConnected()) {
                if (System.currentTimeMillis() - timeMillis > 5000) {
                    timeMillis = System.currentTimeMillis();
                    radioVersion.setText(MainActivity.csLibrary4A.getMacVer());
                    modelVersion.setText(MainActivity.csLibrary4A.getModelNumber());
                    moduleVersion.setText(MainActivity.csLibrary4A.hostProcessorICGetFirmwareVersion());
                    bluetoothVersion.setText(MainActivity.csLibrary4A.getBluetoothICFirmwareVersion());
                    MainActivity.csLibrary4A.batteryLevelRequest();
                }
                moduleRfidOnStatus.setText(MainActivity.csLibrary4A.getRfidOnStatus() ? "on" : "off");
                triggerOnStatus.setText(MainActivity.csLibrary4A.getTriggerButtonStatus() ? "pressed" : "released");
                moduleBarOnStatus.setText(MainActivity.csLibrary4A.getBarcodeOnStatus() ? "on" : "off");
                moduleBattery.setText(MainActivity.csLibrary4A.getBatteryDisplay(true));
                if (MainActivity.csLibrary4A.isRfidFailure()) radioSerial.setText("Not available");
                else radioSerial.setText(MainActivity.csLibrary4A.getRadioSerial());
                radioBoardVersion.setText(MainActivity.csLibrary4A.getRadioBoardVersion());
                productSerial.setText(MainActivity.csLibrary4A.getHostProcessorICSerialNumber());
                boardVersion.setText(MainActivity.csLibrary4A.getHostProcessorICBoardVersion());
                if (MainActivity.csLibrary4A.isBarcodeFailure()) barcodeSerial.setText("Not available");
                else {
                    barcodeSerial.setText(MainActivity.csLibrary4A.getBarcodeSerial());
                    barcodeDate.setText(MainActivity.csLibrary4A.getBarcodeDate());
                    barcodeVersion.setText(MainActivity.csLibrary4A.getBarcodeVersion());
                }
                bluetoothAddress.setText(MainActivity.csLibrary4A.getBluetoothDeviceAddress());
                bluetoothRssi.setText(String.valueOf(MainActivity.csLibrary4A.getRssi()));
            } else {
                radioVersion.setText("");
                moduleRfidOnStatus.setText("");
                moduleBarOnStatus.setText("");
                triggerOnStatus.setText("");
                moduleBattery.setText("");
                moduleVersion.setText("");
                bluetoothVersion.setText("");
                radioSerial.setText("");
                radioBoardVersion.setText("");
                productSerial.setText("");
                boardVersion.setText("");
                barcodeSerial.setText("");
                bluetoothAddress.setText("");
                bluetoothRssi.setText("");
            }
            mHandler.postDelayed(updateRunnable, 1000);
        }
    };
}
