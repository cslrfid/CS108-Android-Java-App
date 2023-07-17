package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class TestFragment extends CommonFragment {
    private Button buttonRead;
    private Button buttonWrite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        androidx.appcompat.app.ActionBar actionBar;
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_access);
        actionBar.setTitle("Test");

        buttonRead = (Button) getActivity().findViewById(R.id.accessRWReadButton);
        buttonWrite = (Button) getActivity().findViewById(R.id.accessRWWriteButton);
    }

    @Override
    public void onDestroy() {
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    public TestFragment() {
        super("TestFragment");
    }
}
