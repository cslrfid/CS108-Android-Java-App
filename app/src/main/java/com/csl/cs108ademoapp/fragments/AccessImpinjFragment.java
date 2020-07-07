package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AccessImpinjFragment extends CommonFragment {
    CheckBox checkBoxTagFocus, checkBoxFastId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_impinj, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkBoxTagFocus = (CheckBox) getActivity().findViewById(R.id.accessIMPINJTagFocus);
        checkBoxFastId = (CheckBox) getActivity().findViewById(R.id.accessIMPINJFastId);
        MainActivity.mCs108Library4a.appendToLog("CheckBoxTagFocus is set");

        MainActivity.mCs108Library4a.setSameCheck(false);
        data2Restore.iQuerySession = MainActivity.mCs108Library4a.getQuerySession();
        data2Restore.iQueryTarget = MainActivity.mCs108Library4a.getQueryTarget();
        data2Restore.tagDelay = MainActivity.mCs108Library4a.getTagDelay();
        data2Restore.dwellTime = MainActivity.mCs108Library4a.getAntennaDwell();
        data2Restore.tagFocus = MainActivity.mCs108Library4a.getTagFocus();
    }

    class Data2Restore {
        int iQuerySession;
        int iQueryTarget;
        byte tagDelay;
        long dwellTime;
        int tagFocus;
    }
    Data2Restore data2Restore = new Data2Restore();

    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setSameCheck(true);
        MainActivity.mCs108Library4a.setTagGroup(MainActivity.mCs108Library4a.getQuerySelect(), data2Restore.iQuerySession, data2Restore.iQueryTarget);
        MainActivity.mCs108Library4a.setTagDelay((byte)data2Restore.tagDelay);
        MainActivity.mCs108Library4a.setAntennaDwell(data2Restore.dwellTime);
        MainActivity.mCs108Library4a.setTagFocus(data2Restore.tagFocus > 0 ? true : false);
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.mCs108Library4a.appendToLog("AccessImpinjFragment is now VISIBLE");
        } else {
            int iValue = 0;
            if (checkBoxTagFocus != null && checkBoxFastId != null) {
                if (checkBoxTagFocus.isChecked()) {
                    MainActivity.mCs108Library4a.setTagGroup(MainActivity.mCs108Library4a.getQuerySelect(), 1, 0);
                    MainActivity.mCs108Library4a.setTagDelay((byte)0);
                    MainActivity.mCs108Library4a.setAntennaDwell(2000);

                    iValue |= 0x10;
                } else MainActivity.mCs108Library4a.setTagGroup(MainActivity.mCs108Library4a.getQuerySelect(), 0, 2);
                if (checkBoxFastId.isChecked()) iValue |= 0x20;
                MainActivity.mCs108Library4a.appendToLog("HelloK: iValue = " + String.format("%20X", iValue));
                MainActivity.mDid = "E28011" + String.format("%02X", iValue);
                MainActivity.mCs108Library4a.macWrite(0x203, iValue);
            }
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("AccessImpinjFragment is now INVISIBLE" + (checkBoxFastId != null ? (" with Value = " + iValue) : ""));
        }
    }

    public AccessImpinjFragment() {
        super("AccessImpinjFragment");
    }
}