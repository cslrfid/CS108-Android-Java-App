package com.csl.cs108ademoapp.fragments;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.app.ServiceCompat.STOP_FOREGROUND_REMOVE;
import static androidx.core.app.ServiceCompat.stopForeground;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.csl.cs108ademoapp.MainActivity.isHomeFragment;
import static com.csl.cs108ademoapp.MainActivity.mContext;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.csl.cs108ademoapp.CustomAlertDialog;
import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.CustomProgressDialog;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.MyForegroundService;
import com.csl.cs108ademoapp.R;

public class HomeFragment extends CommonFragment {
    final boolean DEBUG = false;

    @Override
    public void onAttach(Context context) {
        Log.i("Hello", "HomeFragment.onAttach");
        super.onAttach(context);
    }
    @Override
    public void onStart() {
        Log.i("Hello", "HomeFragment.onStart");
        isHomeFragment = true;
        MainActivity.csLibrary4A.appendToLog("isHomeFragment1 = " + isHomeFragment);
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        if (getActivity().getPackageName().contains("com.csl.cs710ademoapp")) return inflater.inflate(R.layout.home_layout710, container, false);
        else return inflater.inflate(R.layout.home_layout108, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (true && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(android.R.drawable.ic_menu_save);
            if (getActivity().getPackageName().contains("cs710ademoapp")) actionBar.setTitle(R.string.title_activity_home_cs710);
            else actionBar.setTitle(R.string.title_activity_home_cs108);
        }
        if (true) {
            String strForegroundReader = MainActivity.csLibrary4A.getForegroundReader();
            MainActivity.csLibrary4A.appendToLog("strForegroundReader = " + strForegroundReader + ", getForegroundServiceEnable = " + MainActivity.csLibrary4A.getForegroundServiceEnable());
            if (!getActivity().getPackageName().contains("com.csl.cs710awedgeapp") && strForegroundReader != null && strForegroundReader.length() != 0) {
                MainActivity.csLibrary4A.appendToLog("strForegroundReader = " + strForegroundReader + ", getForegroundServiceEnable = " + MainActivity.csLibrary4A.getForegroundServiceEnable());
                LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5;
                linearLayout1 = (LinearLayout) getActivity().findViewById(R.id.mainRow1);
                linearLayout2 = (LinearLayout) getActivity().findViewById(R.id.mainRow2);
                linearLayout3 = (LinearLayout) getActivity().findViewById(R.id.mainRow3);
                linearLayout4 = (LinearLayout) getActivity().findViewById(R.id.mainRow4);
                linearLayout5 = (LinearLayout) getActivity().findViewById(R.id.mainRow5);
                if (getActivity().getPackageName().contains("com.csl.cs710ademoapp")) {
                    FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.mainButton1);
                    frameLayout.setVisibility(View.INVISIBLE);
                } else {
                    Button button = (Button) getActivity().findViewById(R.id.mainButton1);
                    button.setVisibility(View.INVISIBLE);
                }
                linearLayout1.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.GONE);
                linearLayout3.setVisibility(View.VISIBLE);
                linearLayout4.setVisibility(View.INVISIBLE);
                //linearLayout5.setVisibility(View.INVISIBLE);

            }
        }
        MainActivity.tagType = null; MainActivity.mDid = null;
        if (true || MainActivity.sharedObjects.versionWarningShown == false)
            mHandler.post(runnableConfiguring);
        mHandler.postDelayed(runnableStartService, 1000);
    }

    @Override
    public void onStop() {
        stopProgressDialog();
        mHandler.removeCallbacks(runnableConfiguring);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        isHomeFragment = false;
        //MainActivity.csLibrary4A.appendToLog("isHomeFragment1 = " + isHomeFragment);
        super.onDestroyView();
    }

    public HomeFragment() {
        super("HomeFragment");
    }

    CustomProgressDialog progressDialog;
    void stopProgressDialog() {
        if (progressDialog != null) { if (progressDialog.isShowing()) progressDialog.dismiss(); }
    }
    Runnable runnableConfiguring = new Runnable() {
        boolean DEBUG = false;
        @Override
        public void run() {
            if (true) MainActivity.csLibrary4A.appendToLog("runnableConfiguring(): mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
            boolean progressShown = false;
            if (progressDialog != null) { if (progressDialog.isShowing()) progressShown = true; }
            if (MainActivity.csLibrary4A.isBleConnected() == false || MainActivity.csLibrary4A.isRfidFailure()) {
                if (progressShown) {
                    stopProgressDialog();
                    /*String stringPopup = "Connection failed, please rescan.";
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context) getActivity());
                    customPopupWindow.popupStart(stringPopup, false); */
                }
            } else if (MainActivity.csLibrary4A.mrfidToWriteSize() != 0) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("mrfidToWriteSize = " + MainActivity.csLibrary4A.mrfidToWriteSize());
                mHandler.postDelayed(runnableConfiguring, 250);
                if (progressShown == false) {
                    progressDialog = new CustomProgressDialog(getActivity(), "Initializing reader. Please wait.");
                    progressDialog.show();
                }
            } else {
                stopProgressDialog();
                if (MainActivity.sharedObjects.versionWarningShown == false) {
                    String stringPopup = MainActivity.csLibrary4A.checkVersion();
                    if (false && stringPopup != null && stringPopup.length() != 0) {
                        stringPopup = "Firmware too old\nPlease upgrade firmware to at least:" + stringPopup;
                        CustomPopupWindow customPopupWindow = new CustomPopupWindow((Context)getActivity());
                        customPopupWindow.popupStart(stringPopup, false);
                    }
                    MainActivity.sharedObjects.versionWarningShown = true;
                }
            }
            MainActivity.csLibrary4A.setPwrManagementMode(true);
        }
    };

    Runnable runnableStartService = new Runnable() {
        boolean DEBUG = false;

        @Override
        public void run() {
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) = " + ActivityCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)  = " + ActivityCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions WRITE_EXTERNAL_STORAGE"); //
                    requestPermissions(new String[] { WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE }, 1);
                    //Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: WRITE_EXTERNAL_STORAGE is permitted"); ///
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle WRITE_EXTERNAL_STORAGE");

            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION)  = " + ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomAlertDialog appdialog = new CustomAlertDialog();
                    appdialog.Confirm(getActivity(), "Use your location",
                            "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
                            "No thanks", "Turn on",
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: allow permission in ACCESS_FINE_LOCATION handler");
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions ACCESS_FINE_LOCATION");
                                    requestPermissions(new String[] { ACCESS_FINE_LOCATION }, 123); //ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
                                    //if (false) Toast.makeText(mContext, com.csl.cslibrary4a.R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                                    /*{
                                        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                                            MainActivity.csLibrary4A.appendToLog("popupAlert: StreamOut: start activity ACTION_LOCATION_SOURCE_SETTINGS");
                                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            mContext.startActivity(intent1);
                                        }
                                    }*/
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                    //bAlerting = false;
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.csLibrary4A.appendToLog("runnableStartService: reject permission in ACCESS_FINE_LOCATION handler");
                                    //bAlerting = false;
                                    //bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                                }
                            });
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: started ACCESS_FINE_LOCATION handler");
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled ACCESS_FINE_LOCATION");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle ACCESS_FINE_LOCATION");

            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT));
            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, BLUETOOTH_SCAN)  = " + ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions BLUETOOTH_SCAN and BLUETOOTH_CONNECT");
                    requestPermissions(new String[] { BLUETOOTH_SCAN, BLUETOOTH_CONNECT }, 123);
                } else MainActivity.csLibrary4A.appendToLog("runnableStartService: handled BLUETOOTH_CONNECT and BLUETOOTH_SCAN");
            } else MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle BLUETOOTH_SCAN and BLUETOOTH_CONNECT");

            MainActivity.csLibrary4A.appendToLog("runnableStartService: ActivityCompat.checkSelfPermission(activity, POST_NOTIFICATIONS) = " + ActivityCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS));
            if (NotificationManagerCompat.from(getActivity()).areNotificationsEnabled()) MainActivity.csLibrary4A.appendToLog("Notification is enabled");
            else MainActivity.csLibrary4A.appendToLog("Notification is disabled");
            if (!MainActivity.foregroundServiceEnable) { }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: requestPermissions POST_NOTIFICATIONS");
                    requestPermissions(new String[] { POST_NOTIFICATIONS }, 10); //POST_NOTIFICATIONS, FOREGROUND_SERVICE_LOCATION
                } else {
                    MainActivity.csLibrary4A.appendToLog("runnableStartService: handled POST_NOTIFICATIONS");
                    startService();
                }
            } else {
                MainActivity.csLibrary4A.appendToLog("runnableStartService: no need to handle POST_NOTIFICATIONS");
                startService();
            }
        }
    };
    Intent serviceIntent;
    Service serviceStarted;
    public void startService() {
        ActivityManager activityManager = (ActivityManager) getSystemService(getContext(), ActivityManager.class);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            MainActivity.csLibrary4A.appendToLog("MyForegroundService.class.getName = " + MyForegroundService.class.getName() + ", service.service.getClassName = " + service.service.getClassName());
            if (MyForegroundService.class.getName().equals(service.service.getClassName())) {
                return;
            }
        }
        serviceIntent = new Intent(getActivity(), MyForegroundService.class);
        Log.i("Hello", "getActivity is " + (getActivity() == null ? "NULL" : "Valid"));
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        Log.i("Hello", "getContext is " + (getContext() == null ? "NULL" : "Valid"));
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }
    public void stopService1() {
        //Intent serviceIntent = new Intent(getActivity(), CustomForegroundService.class);
        stopForeground(serviceStarted, STOP_FOREGROUND_REMOVE);
    }

    void popupAlert() {

    }
}
