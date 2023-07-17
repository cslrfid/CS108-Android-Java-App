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

        if (true && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(android.R.drawable.ic_menu_save);
            actionBar.setTitle(R.string.title_activity_home);
        }
        MainActivity.mDid = null;
        if (true || MainActivity.sharedObjects.versioinWarningShown == false)
            mHandler.post(runnableConfiguring);
    }

    @Override
    public void onStop() {
        stopProgressDialog();
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
        boolean DEBUG = false;
        @Override
        public void run() {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("runnableConfiguring(): mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
            boolean progressShown = false;
            if (progressDialog != null) { if (progressDialog.isShowing()) progressShown = true; }
            if (MainActivity.csLibrary4A.isBleConnected() == false || MainActivity.csLibrary4A.isRfidFailure()) {
                if (progressShown) {
                    stopProgressDialog();
                    /*String stringPopup = "Connection failed, please rescan.";
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context) getActivity());
                    customPopupWindow.popupStart(stringPopup, false); */
                }
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
                mHandler.postDelayed(runnableConfiguring, 250);
                if (progressShown == false) {
                    progressDialog = new CustomProgressDialog(getActivity(), "Initializing reader. Please wait.");
                    progressDialog.show();
                }
            } else {
                stopProgressDialog();
                if (MainActivity.sharedObjects.versioinWarningShown == false) {
                    String stringPopup = MainActivity.csLibrary4A.checkVersion();
                    if (false && stringPopup != null && stringPopup.length() != 0) {
                        stringPopup = "Firmware too old\nPlease upgrade firmware to at least:" + stringPopup;
                        CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context)getActivity());
                        customPopupWindow.popupStart(stringPopup, false);
                    }
                    MainActivity.sharedObjects.versioinWarningShown = true;
                }
            }
            MainActivity.csLibrary4A.setPwrManagementMode(true);
        }
    };
}
