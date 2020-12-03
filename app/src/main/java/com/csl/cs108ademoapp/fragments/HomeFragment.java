package com.csl.cs108ademoapp.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.*;

public class HomeFragment extends CommonFragment {
    final boolean DEBUG = false;

    @Override
    public void onAttach(Context context) {
        Log.i("Hello", "HomeFragment.onAttach");
        super.onAttach(context);
    }
    @Override
    public void onStart() {
        Log.i("Hello", "HomeFragment.onStart");
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.home_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(android.R.drawable.ic_menu_save);
            actionBar.setTitle(R.string.title_activity_home);
        }
        mHandler.post(runnableConfiguring);
    }

    @Override
    public void onStop() {
        mHandler.removeCallbacks(runnableConfiguring);
        super.onStop();
    }

    public HomeFragment() {
        super("HomeFragment");
    }

    CustomProgressDialog progressDialog;
    void stopProgressDialog() {
        if (progressDialog != null) { if (progressDialog.isShowing()) progressDialog.dismiss(); }
    }
    Runnable runnableConfiguring = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AAA: runnableConfiguring(): mrfidToWriteSize = " + MainActivity.mCs108Library4a.mrfidToWriteSize());
            boolean progressShown = false;
            if (progressDialog != null) if (progressDialog.isShowing()) progressShown = true;
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                if (progressShown) {
                    stopProgressDialog();
                    String stringPopup = "Connection failed, please rescan.";
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context) getActivity());
                    customPopupWindow.popupStart(stringPopup, false);
                }
            } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0 && MainActivity.mCs108Library4a.isRfidFailure() == false) {
                mHandler.postDelayed(runnableConfiguring, 250);
                if (progressShown == false) {
                    progressDialog = new CustomProgressDialog(getActivity(), "Initializing reader. Please wait.");
                    progressDialog.show();
                }
            } else {
                stopProgressDialog();
                if (MainActivity.sharedObjects.versioinWarningShown == false) {
                    String macVersion = MainActivity.mCs108Library4a.getMacVer();
                    String hostVersion = MainActivity.mCs108Library4a.hostProcessorICGetFirmwareVersion();
                    String bluetoothVersion = MainActivity.mCs108Library4a.getBluetoothICFirmwareVersion();
                    String strVersionRFID = "2.6.20"; String[] strRFIDVersions = strVersionRFID.split("\\.");
                    String strVersionBT = "1.0.14"; String[] strBTVersions = strVersionBT.split("\\.");
                    String strVersionHost = "1.0.9"; String[] strHostVersions = strVersionHost.split("\\.");
                    String stringPopup = "";
                    int icsModel = MainActivity.mCs108Library4a.getcsModel();
                    if (MainActivity.mCs108Library4a.isRfidFailure() == false && MainActivity.mCs108Library4a.checkHostProcessorVersion(macVersion, Integer.parseInt(strRFIDVersions[0].trim()), Integer.parseInt(strRFIDVersions[1].trim()), Integer.parseInt(strRFIDVersions[2].trim())) == false)
                        stringPopup += "\nRFID processor firmware: V" + strVersionRFID;
                    if (icsModel == 108) if (MainActivity.mCs108Library4a.checkHostProcessorVersion(hostVersion,  Integer.parseInt(strHostVersions[0].trim()), Integer.parseInt(strHostVersions[1].trim()), Integer.parseInt(strHostVersions[2].trim())) == false)
                        stringPopup += "\nSiliconLab firmware: V" + strVersionHost;
                    if (icsModel == 108) if (MainActivity.mCs108Library4a.checkHostProcessorVersion(bluetoothVersion, Integer.parseInt(strBTVersions[0].trim()), Integer.parseInt(strBTVersions[1].trim()), Integer.parseInt(strBTVersions[2].trim())) == false)
                        stringPopup += "\nBluetooth firmware: V" + strVersionBT;
                    if (stringPopup.length() != 0) {
                        stringPopup = "Firmware too old\nPlease upgrade frimware to at least:" + stringPopup;
                        CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context)getActivity());
                        customPopupWindow.popupStart(stringPopup, false);
                    }
                    MainActivity.sharedObjects.versioinWarningShown = true;
                }
            }
            MainActivity.mCs108Library4a.setPwrManagementMode(true);
        }
    };
}
