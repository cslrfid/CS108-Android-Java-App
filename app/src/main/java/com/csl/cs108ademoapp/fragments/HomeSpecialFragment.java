package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class HomeSpecialFragment extends CommonFragment {
    final boolean DEBUG = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.home_special_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        menuFragment = true;
        super.onViewCreated(view, savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(R.string.title_activity_special);
        }

        Button buttonAuraSense = (Button) getActivity().findViewById(R.id.SpecialButtonAurasense);
        Button buttonFdmicro = (Button) getActivity().findViewById(R.id.SpecialButtonFdmicro);
        Button buttonLanda = (Button) getActivity().findViewById(R.id.SpecialButtonLanda);
        MainActivity.csLibrary4A.appendToLog("HomeSpecialFragment.onViewCreated: get98XX() = " + MainActivity.csLibrary4A.get98XX());
        if (MainActivity.csLibrary4A.get98XX() == 2) {
            buttonAuraSense.setVisibility(View.GONE);
            buttonFdmicro.setVisibility(View.GONE); buttonLanda.setVisibility(View.GONE);
        }
        MainActivity.tagType = null; MainActivity.mDid = null;
        if (MainActivity.csLibrary4A.isBleConnected()) MainActivity.csLibrary4A.restoreAfterTagSelect();
    }

    public HomeSpecialFragment() {
        super("HomeSpecialFragment");
    }
}
