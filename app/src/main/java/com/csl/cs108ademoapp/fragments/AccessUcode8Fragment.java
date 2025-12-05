package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPC;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCBRAND;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCBRANDTID;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODE8_EPCTID;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_NXP_UCODEDNA;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.CustomTabLayout;

public class AccessUcode8Fragment extends CommonFragment {
    final boolean DEBUG = true;
    View viewFragment;
    Spinner spinnerTagSelect;
    RadioButton radioButtonSelectEpc, radioButtonSelectEpcTid, radioButtonSelectEpcBrand, radioButtonSelectEpcBrandTidCheck;
    enum nxpTag {
        ucode8, ucodeDNA, others
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewFragment = inflater.inflate(R.layout.fragment_access_ucode8, container, false);
        return viewFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerTagSelect = (Spinner) view.findViewById(R.id.accessNxpTagSelect);
        ArrayAdapter<CharSequence> targetAdapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.nxp_options, R.layout.custom_spinner_layout);
        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagSelect.setAdapter(targetAdapter1); spinnerTagSelect.setSelection(0);
        spinnerTagSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                CustomTabLayout tabLayout = (CustomTabLayout) getActivity().findViewById(R.id.OperationsTabLayout2);
                MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.onViewCreated: tabLayout is " + (tabLayout == null ? "null" : "valid"));
                CustomTabLayout.TabView tabView = tabLayout.getTabAt(2).view; tabView.setVisibility(View.GONE);
                CustomTabLayout.TabView tabViewUntrace = tabLayout.getTabAt(3).view; tabViewUntrace.setVisibility(View.GONE);
                LinearLayout layout = (LinearLayout) viewFragment.findViewById(R.id.accessNxpUcode8Select); layout.setVisibility(View.GONE);
                if (position == nxpTag.ucode8.ordinal()) {
                    updateUcode8Type();
                    if (MainActivity.csLibrary4A.get98XX() == 0) tabViewUntrace.setVisibility(View.VISIBLE);
                    layout.setVisibility(View.VISIBLE);
                } else if (position == nxpTag.ucodeDNA.ordinal()) {
                    MainActivity.tagType = TAG_NXP_UCODEDNA; MainActivity.mDid = "" /*"E2C06"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.onItemSelected set MainActivity.mDid as E2C06");
                    tabView.setVisibility(View.VISIBLE);
                    if (MainActivity.csLibrary4A.get98XX() == 0) tabViewUntrace.setVisibility(View.VISIBLE);
                } else {
                    MainActivity.tagType = TAG_NXP; MainActivity.mDid = "" /*"E2806"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.onItemSelected set MainActivity.mDid as E2806");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        radioButtonSelectEpc = (RadioButton) view.findViewById(R.id.accessUC8SelectEpc);
        radioButtonSelectEpcTid = (RadioButton) view.findViewById(R.id.accessUC8SelectEpcTid);
        radioButtonSelectEpcBrand = (RadioButton) view.findViewById(R.id.accessUC8SelectEpcBrand);
        radioButtonSelectEpcBrandTidCheck = (RadioButton) view.findViewById(R.id.accessUC8SelectEpcBrandTidCheck);
        if (MainActivity.csLibrary4A.get98XX() == 2) {
            radioButtonSelectEpc.setChecked(true);
            radioButtonSelectEpcBrand.setVisibility(View.GONE);
            radioButtonSelectEpcBrandTidCheck.setVisibility(View.GONE);
        } else radioButtonSelectEpcBrand.setChecked(true);

        MainActivity.csLibrary4A.setSameCheck(false);
    }
    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint2(true);
    }

    @Override
    public void onPause() {
        setUserVisibleHint2(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setSameCheck(true);
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    //@Override
    public void setUserVisibleHint2(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.setUserVisibleHint: isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) { //getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment is now VISIBLE");
            //            setNotificationListener();
        } else {
            updateUcode8Type();
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment is now INVISIBLE");
        }
    }

    void updateUcode8Type() {
        if (spinnerTagSelect != null && spinnerTagSelect.getSelectedItemPosition() == nxpTag.ucode8.ordinal()) {
            if (radioButtonSelectEpc != null && radioButtonSelectEpcTid != null && radioButtonSelectEpcBrand != null && radioButtonSelectEpcBrandTidCheck != null) {
                if (radioButtonSelectEpc.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC");
                    MainActivity.tagType = TAG_NXP_UCODE8_EPC; MainActivity.mDid = "" /*"E2806894A"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.setUserVisibleHint set MainActivity.mDid as E2806894A");
                }
                if (radioButtonSelectEpcTid.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+TID");
                    MainActivity.tagType = TAG_NXP_UCODE8_EPCTID; MainActivity.mDid = "" /*"E2806894B"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.setUserVisibleHint set MainActivity.mDid as E2806894b");
                }
                if (radioButtonSelectEpcBrand.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+BRAND");
                    MainActivity.tagType = TAG_NXP_UCODE8_EPCBRAND; MainActivity.mDid = "" /*"E2806894C"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.setUserVisibleHint set MainActivity.mDid as E2806894C");
                }
                if (radioButtonSelectEpcBrandTidCheck.isChecked()) {
                    MainActivity.csLibrary4A.appendToLog("Selected EPC+BRAND");
                    MainActivity.tagType = TAG_NXP_UCODE8_EPCBRANDTID; MainActivity.mDid = "" /*"E2806894d"*/;
                    MainActivity.csLibrary4A.appendToLog("AccessUcode8Fragment.setUserVisibleHint set MainActivity.mDid as E2806894d");
                }
            }
        }
    }

    public AccessUcode8Fragment() {
        super("AccessUcode8Fragment");
    }
}
