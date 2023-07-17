package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AccessUcode8Fragment extends CommonFragment {
    final boolean DEBUG = true;
    RadioButton radioButtonSelectEpc, radioButtonSelectEpcTid, radioButtonSelectEpcBrand, radioButtonSelectEpcBrandTidCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_ucode8, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        radioButtonSelectEpc = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpc);
        radioButtonSelectEpcTid = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpcTid);
        radioButtonSelectEpcBrand = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpcBrand); radioButtonSelectEpcBrand.setChecked(true);
        radioButtonSelectEpcBrandTidCheck = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpcBrandTidCheck);

        MainActivity.csLibrary4A.setSameCheck(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment is now VISIBLE");
            //            setNotificationListener();
        } else {
            if (radioButtonSelectEpc != null && radioButtonSelectEpcTid != null && radioButtonSelectEpcBrand != null && radioButtonSelectEpcBrandTidCheck != null) {
                if (radioButtonSelectEpc.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC");
                    MainActivity.mDid = "E2806894A";
                }
                if (radioButtonSelectEpcTid.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+TID");
                    MainActivity.mDid = "E2806894B";
                }
                if (radioButtonSelectEpcBrand.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+BRAND");
                    MainActivity.mDid = "E2806894C";
                }
                if (radioButtonSelectEpcBrandTidCheck.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+BRAND");
                    MainActivity.mDid = "E2806894d";
                }
            }
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment is now INVISIBLE");
        }
    }

    public AccessUcode8Fragment() {
        super("AccessUcode8Fragment");
    }
}
