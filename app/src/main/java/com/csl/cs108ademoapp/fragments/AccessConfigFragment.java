package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AccessConfigFragment extends CommonFragment {
    boolean bXerxesEnable = false;
    EditText editTextPassWord, editTextPower, editText0, editText1, editText2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_config, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle("Configuration");
        }

        editTextPassWord = (EditText) getActivity().findViewById(R.id.accessConfigPasswordValue);
        editTextPassWord.setText("00000000");
        editTextPower = (EditText) getActivity().findViewById(R.id.accessConfigAntennaPower);
        editTextPower.setText("300");

        TextView textView0 = (TextView) getActivity().findViewById(R.id.accessConfigData0Label);
        textView0.setText("select hold(ms)");
        editText0 = (EditText) getActivity().findViewById(R.id.accessConfigData0);

        if (MainActivity.mDid == null) editText0.setText("0");
        else if (MainActivity.mDid.indexOf("E282405") == 0) editText0.setText("9");
        else if (MainActivity.mDid.indexOf("E282403") == 0) editText0.setText("3");
        else editText0.setText("0");

        TextView textView1 = (TextView) getActivity().findViewById(R.id.accessConfigData1Label);
        textView1.setText("Upper Limit of On Chip RSSI");
        editText1 = (EditText) getActivity().findViewById(R.id.accessConfigData1);
        editText1.setText("21");

        TextView textView2 = (TextView) getActivity().findViewById(R.id.accessConfigData2Label);
        textView2.setText("Lower Limit On Chip RSSI");
        editText2 = (EditText) getActivity().findViewById(R.id.accessConfigData2);
        editText2.setText("13");

        Button button = (Button) getActivity().findViewById(R.id.accessConfigOKButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.config.configPassword = editTextPassWord.getText().toString();
                MainActivity.config.configPower = editTextPower.getText().toString();
                MainActivity.config.config0 = editText0.getText().toString();
                MainActivity.config.config1 = editText1.getText().toString();
                MainActivity.config.config2 = editText2.getText().toString();

                if (MainActivity.mDid != null) if (MainActivity.mDid.indexOf("E282405") == 0) bXerxesEnable = true;

                Fragment fragment;
                if (bXerxesEnable) fragment = new AxzonFragment();
                else fragment = new MicronFragment();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    public static AccessConfigFragment newInstance(boolean bXerxesEnable) {
        AccessConfigFragment myFragment = new AccessConfigFragment();
        myFragment.bXerxesEnable = bXerxesEnable;
        return myFragment;
    }
    public AccessConfigFragment() {
        super("AccessConfigFragment");
    }
}
