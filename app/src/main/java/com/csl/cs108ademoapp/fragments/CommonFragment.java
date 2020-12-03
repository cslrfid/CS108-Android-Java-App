package com.csl.cs108ademoapp.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public abstract class CommonFragment extends Fragment {
    final boolean DEBUG = false; final String TAG = "Hello";
    private String fragmentName;
    MenuItem menuBatteryVoltageItem;
    Handler mHandler = new Handler();
    boolean fragmentActive = false;

    @Override
    public void onAttach(Context context) {
        if (fragmentName == null) Log.i(TAG, "CommonFragment.onAttach: NULL fragmentName");
        else Log.i(TAG, "CommonFragment.onAttach: fragmentName = " + fragmentName);
        if (MainActivity.mCs108Library4a == null) Log.i(TAG, "CommonFragment.onAttach: NULL MainActivity.mCs108Library4a");
        if (fragmentName == null) MainActivity.mCs108Library4a.appendToLog("NULL fragmentName");
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onCreate(savedInstanceState);
    }

    boolean menuFragment = false;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, boolean menuFragment) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        this.menuFragment = menuFragment;
        if (menuFragment)   setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    int batteryCount_old; boolean batteryUpdate = false; CustomPopupWindow batteryWarningPopupWindow; String strBatteryLow;
    private final Runnable updateBatteryRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(updateBatteryRunnable, 5000);  //normal battery level updates every 4 seconds

            if (menuBatteryVoltageItem == null) return;
            if (MainActivity.mCs108Library4a.isBleConnected() == false) { MainActivity.sharedObjects.batteryWarningShown = 0; menuBatteryVoltageItem.setTitle("");  return; }

            int batteryCount = MainActivity.mCs108Library4a.getBatteryCount();
            String strText = MainActivity.mCs108Library4a.getBatteryDisplay(false);
            if (batteryCount_old != batteryCount) strBatteryLow = MainActivity.mCs108Library4a.isBatteryLow();

            if (strBatteryLow == null) MainActivity.sharedObjects.batteryWarningShown = 0;
            else if (++MainActivity.sharedObjects.batteryWarningShown == 1) {
                if (batteryWarningPopupWindow != null)
                    batteryWarningPopupWindow.popupWindow.dismiss();
                batteryWarningPopupWindow = new CustomPopupWindow(MainActivity.mContext);
                batteryWarningPopupWindow.popupStart(strBatteryLow + "% Battery Life Left, Please Recharge CS108 or Replace with Freshly Charged CS108B", false);
            } else if (false && MainActivity.sharedObjects.batteryWarningShown > 10) MainActivity.sharedObjects.batteryWarningShown = 0;

            if (batteryCount_old == batteryCount && strText.length() != 0) {
                if (batteryUpdate) strText = "B" + strText;
                else strText = "A" + strText;
                batteryUpdate = !batteryUpdate;
                menuBatteryVoltageItem.setTitle(strText);
            } else {
                batteryCount_old = batteryCount;
                SpannableString spanString = new SpannableString(strText);
                spanString.setSpan(new ForegroundColorSpan(Color.RED), 0, spanString.length(), 0); //fix the color to white
                if (false || strBatteryLow != null) menuBatteryVoltageItem.setTitle(spanString);
                else menuBatteryVoltageItem.setTitle(strText);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        if (menuFragment) {
            batteryCount_old = -1;
            mHandler.post(updateBatteryRunnable);
        }
        super.onResume();
        fragmentActive = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName + " with fragmentActive = " + fragmentActive);
        if (fragmentActive == false) return;
        if (fragmentName.matches("ConnectionFragment")) {
            inflater.inflate(R.menu.menu_connection, menu);
            if (MainActivity.mCs108Library4a.isBleScanning()) {
                menu.findItem(R.id.action_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
        } else {
            inflater.inflate(R.menu.menu_home, menu);
            menuBatteryVoltageItem = menu.findItem(R.id.home_voltage);
            if (fragmentName.matches("InventoryFragment")
                    || fragmentName.contains("InventoryRfidiMultiFragment")
                    || fragmentName.contains("ColdChainFragment")
                    || fragmentName.contains("AxzonFragment")
                    || fragmentName.contains("MicronFragment")
                    || fragmentName.contains("FdmicroFragment")
                    || fragmentName.contains("UcodeFragment")
                    || fragmentName.contains("Ucode8Fragment")
                    || fragmentName.contains("ImpinjFragment")
                    || fragmentName.contains("AuraSenseFragment")
                    ) {
                menu.findItem(R.id.menuAction_1).setTitle("Clear");
                menu.findItem(R.id.menuAction_2).setTitle("Sort");
                menu.findItem(R.id.menuAction_3).setTitle("Save");
                menu.findItem(R.id.menuAction_4).setIcon(android.R.drawable.ic_menu_share);
            } else {
                menu.removeItem(R.id.menuAction_1);
                menu.removeItem(R.id.menuAction_2);
                menu.removeItem(R.id.menuAction_3);
                menu.removeItem(R.id.menuAction_4);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName + " with fragmentActive = " + fragmentActive);
        if (fragmentActive == false) return false;
        switch (item.getItemId()) {
            case R.id.home_menu:
                DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        fragmentActive = false;
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        mHandler.removeCallbacks(updateBatteryRunnable);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(fragmentName);
        super.onDetach();
    }

    CommonFragment(String text) {
        fragmentName = text;
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog(",,," + fragmentName);
    }
}
