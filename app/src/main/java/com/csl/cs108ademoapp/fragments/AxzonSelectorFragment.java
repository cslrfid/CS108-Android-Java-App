package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON_OPUS;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_MAGNUS_S2;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_MAGNUS_S3;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON_XERXES;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.RfidReader;

public class AxzonSelectorFragment extends CommonFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_select_axzon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        menuFragment = true;
        super.onViewCreated(view, savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(R.string.title_activity_axzonSelector);
        }

        Button button_s2 = (Button) getActivity().findViewById(R.id.select_axzon_s2);
        button_s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(TAG_MAGNUS_S2);
            }
        });
        Button button_s3 = (Button) getActivity().findViewById(R.id.select_axzon_s3);
        button_s3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(TAG_MAGNUS_S3);
            }
        });
        Button button_xx = (Button) getActivity().findViewById(R.id.select_axzon_xerxes);
        button_xx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(TAG_AXZON_XERXES);
            }
        });
        Button button_opus = (Button) getActivity().findViewById(R.id.select_axzon_opus);
        //if (MainActivity.csLibrary4A.get98XX() != 0) button_opus.setVisibility(View.GONE);
        button_opus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(TAG_AXZON_OPUS);
            }
        });
        Button button_all = (Button) getActivity().findViewById(R.id.select_axzon_all);
        button_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(TAG_AXZON);
            }
        });
    }

    public static AxzonSelectorFragment newInstance() {
        AxzonSelectorFragment myFragment = new AxzonSelectorFragment();
        return myFragment;
    }
    public AxzonSelectorFragment() {
        super("AxzonSelectorFragment");
    }

    void gotoAxzonFragment(RfidReader.TagType tagType) {
        MainActivity.tagType = tagType; MainActivity.mDid = "";
        MainActivity.csLibrary4A.appendToLog("HelloABC: gotoAxzonFragment with tagType = " + tagType.toString());

        MainActivity.csLibrary4A.appendToLog("HelloABC: config is " + (MainActivity.config == null ? "null" : "Valid"));
        MainActivity.config.configPassword = "00000000";
        MainActivity.config.configPower = Integer.toString(300);
        MainActivity.config.config0 = Integer.toString(9);
        MainActivity.config.configRssiUpperLimit = Integer.toString(21);
        MainActivity.config.configRssiLowerLimit = Integer.toString(13);
        if (tagType == TAG_MAGNUS_S2) MainActivity.config.configHumidityThreshold = Integer.toString(13);
        else MainActivity.config.configHumidityThreshold = Integer.toString(160);

            Fragment fragment = new AxzonFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
    }
}
