package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class HomeSpecialFragment extends CommonFragment {
    final boolean DEBUG = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.home_special_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(R.string.title_activity_special);
        }

        Button buttonFdmicro = (Button) getActivity().findViewById(R.id.SpecialButtonFdmicro);
        Button buttonLanda = (Button) getActivity().findViewById(R.id.SpecialButtonLanda);
        if (MainActivity.csLibrary4A.get98XX() == 2) {
            buttonFdmicro.setVisibility(View.GONE); buttonLanda.setVisibility(View.GONE);
        }
        MainActivity.mDid = null;
        MainActivity.csLibrary4A.restoreAfterTagSelect();
    }

    public HomeSpecialFragment() {
        super("HomeSpecialFragment");
    }
}
