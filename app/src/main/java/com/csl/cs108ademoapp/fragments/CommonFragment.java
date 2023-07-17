package com.csl.cs108ademoapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.CustomAlertDialog;

public abstract class CommonFragment extends Fragment {
    final boolean DEBUG = false; final String TAG = "Hello";
    private String fragmentName;
    MenuItem menuTriggerItem, menuBatteryVoltageItem;
    Handler mHandler = new Handler();
    boolean fragmentActive = false;

    @Override
    public void onAttach(Context context) {
        if (DEBUG) {
        if (fragmentName == null) Log.i(TAG, "CommonFragment.onAttach: NULL fragmentName");
        else Log.i(TAG, "CommonFragment.onAttach: fragmentName = " + fragmentName);
        if (MainActivity.csLibrary4A == null) Log.i(TAG, "CommonFragment.onAttach: NULL MainActivity.mCs108Library4a");
        if (fragmentName == null) MainActivity.csLibrary4A.appendToLog("NULL fragmentName");
        MainActivity.csLibrary4A.appendToLog(fragmentName);
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onCreate(savedInstanceState);
    }

    boolean menuFragment = false;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, boolean menuFragment) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        this.menuFragment = menuFragment;

        bleConnected = false; if (MainActivity.csLibrary4A.isBleConnected()) bleConnected = true;
        rfidFailure = false; if (MainActivity.csLibrary4A.isRfidFailure()) rfidFailure = true;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        boolean bHomeAsUpEnabled = true;
        if (fragmentName.matches("HomeFragment")) bHomeAsUpEnabled = false;
        if (fragmentName.matches("HomeWFragment")) bHomeAsUpEnabled = false;
        actionBar.setDisplayHomeAsUpEnabled(bHomeAsUpEnabled);
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("CommonFragment: onCreateView with fragmentName = " + fragmentName + " , onOptionsItemSelected = " + menuFragment + ", DisplayHomeAsUpEnabled = " + bHomeAsUpEnabled);

        if (menuFragment)   setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    int triggerCount_old;
    private final Runnable updateTriggerRunnable = new Runnable() {
        @Override
        public void run() {
            short reportCount = 5;
            if (MainActivity.csLibrary4A.isBleConnected()) reportCount = MainActivity.csLibrary4A.getTriggerReportingCount();
            mHandler.postDelayed(updateTriggerRunnable, reportCount * 1100);
            if (menuTriggerItem == null) return;
            if (MainActivity.csLibrary4A.isBleConnected() == false) { menuTriggerItem.setTitle("");  return; }
            int triggerCount = MainActivity.csLibrary4A.getTriggerCount();
            if (triggerCount != triggerCount_old) {
                triggerCount_old = triggerCount;
                if (MainActivity.csLibrary4A.getTriggerButtonStatus()) menuTriggerItem.setTitle("Ton");
                else menuTriggerItem.setTitle("Toff");
            } else menuTriggerItem.setTitle("");
        }
    };

    boolean bleDisConnecting = false;
    boolean bleConnected = false, rfidFailure = false;
    int batteryCount_old; boolean batteryUpdate = false; CustomPopupWindow batteryWarningPopupWindow; String strBatteryLow;
    private final Runnable updateBatteryRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(updateBatteryRunnable, 5000);  //normal battery level updates every 4 seconds

            if (menuBatteryVoltageItem == null) return;
            if (MainActivity.csLibrary4A.isBleConnected() == false) {
                if (bleDisConnecting) bleConnected = false; bleDisConnecting = true;
                if (bleConnected) {
                    bleConnected = false; if (DEBUG) MainActivity.csLibrary4A.appendToLog("bleConnected is FALSE in " + fragmentName);
                    if (false) Toast.makeText(MainActivity.mContext, "Bluetooth is disconnected", Toast.LENGTH_SHORT).show();
                    else {
                        CustomAlertDialog appdialog = new CustomAlertDialog();
                        appdialog.Confirm((Activity) MainActivity.mContext, "Bluetooth is DISCONNECTED",
                                "Do you want to reconnect the Bluetooth ?",
                                "No thanks", "Reconnect",
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("Confirm is pressed");
                                        MainActivity.csLibrary4A.connect(null);
                                    }
                                },
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("Cancel is pressed.");
                                    }
                                });
                    }
                } else if (DEBUG) MainActivity.csLibrary4A.appendToLog("bleConnected is Kept as FALSE in " + fragmentName);
                MainActivity.sharedObjects.batteryWarningShown = 0; menuBatteryVoltageItem.setTitle("");  return;
            } else {
                bleConnected = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("bleConnected is TRUE in " + fragmentName);
            }

            if (MainActivity.csLibrary4A.isRfidFailure()) {
                if (rfidFailure == false) {
                    rfidFailure = true;
                    CustomAlertDialog appdialog = new CustomAlertDialog();
                    appdialog.Confirm((Activity) MainActivity.mContext, "Rfid Transmission failure",
                            "Do you want to disconnect the Bluetooth ?",
                            "No thanks", "Disconnect",
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("Confirm is pressed");
                                    MainActivity.csLibrary4A.forceBTdisconnect();
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("Cancel is pressed.");
                                }
                            });
                }
            } else rfidFailure = false;

            int batteryCount = MainActivity.csLibrary4A.getBatteryCount();
            String strText = MainActivity.csLibrary4A.getBatteryDisplay(false);
            if (batteryCount_old != batteryCount) strBatteryLow = MainActivity.csLibrary4A.isBatteryLow();

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
                MainActivity.csLibrary4A.batteryLevelRequest();
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
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        if (menuFragment) {
            batteryCount_old = -1;
            mHandler.post(updateTriggerRunnable);
            mHandler.post(updateBatteryRunnable);
        }
        super.onResume();
        fragmentActive = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName + " with fragmentActive = " + fragmentActive);
        if (fragmentActive == false) return;
        if (fragmentName.matches("ConnectionFragment")) {
            inflater.inflate(R.menu.menu_connection, menu);
            if (MainActivity.csLibrary4A.isBleScanning()) {
                menu.findItem(R.id.action_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                menu.findItem(R.id.action_refresh).setActionView(null);
            }
        } else {
            inflater.inflate(R.menu.menu_home, menu);
            menuBatteryVoltageItem = menu.findItem(R.id.home_voltage);;
            menuTriggerItem = menu.findItem(R.id.home_trigger);
            menu.removeItem(R.id.home_menu);
            if (fragmentName.matches("InventoryFragment")
                    || fragmentName.contains("InventoryRfidiMultiFragment")
                    || fragmentName.contains("InventoryRfidSimpleFragment")

                    || fragmentName.contains("ImpinjFragment")
                    || fragmentName.contains("Ucode8Fragment")
                    || fragmentName.contains("UcodeFragment")
                    || fragmentName.contains("ColdChainFragment")
                    || fragmentName.contains("AuraSenseFragment")
                    || fragmentName.contains("AxzonFragment")
                    || fragmentName.contains("MicronFragment")
                    || fragmentName.contains("FdmicroFragment")
                    || fragmentName.contains("LedTagFragment")

                    || fragmentName.contains("InventoryRfidSimpleFragment")
                    ) {
                menu.findItem(R.id.menuAction_clear).setTitle("Clear");
                menu.findItem(R.id.menuAction_save).setTitle("Save");
                menu.findItem(R.id.menuAction_share).setTitle("Share");
                menu.findItem(R.id.menuAction_share).setIcon(android.R.drawable.ic_menu_share);
                if (fragmentName.contains("InventoryBarcodeFragment")) {
                    menu.removeItem(R.id.menuAction_sort);
                    menu.removeItem(R.id.menuAction_sortRssi);
                } else {
                    menu.findItem(R.id.menuAction_sort).setTitle("Sort by EPC");
                    menu.findItem(R.id.menuAction_sortRssi).setTitle("Sort by Rssi");
                }
            } else {
                menu.removeItem(R.id.menuAction_clear);
                menu.removeItem(R.id.menuAction_save);
                menu.removeItem(R.id.menuAction_share);
                menu.removeItem(R.id.menuAction_sort);
                menu.removeItem(R.id.menuAction_sortRssi);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("CommonFragment: onOptionsItemSelected");
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName + " with fragmentActive = " + fragmentActive);
        if (fragmentActive == false) return false;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("CommonFragment: onOptionsItemSelected: getActivity().onBackPressed");
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        fragmentActive = false;
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        mHandler.removeCallbacks(updateTriggerRunnable);
        mHandler.removeCallbacks(updateBatteryRunnable);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(fragmentName);
        super.onDetach();
    }

    CommonFragment(String text) {
        fragmentName = text;
        if (DEBUG) MainActivity.csLibrary4A.appendToLog(",,," + fragmentName);
    }
}
