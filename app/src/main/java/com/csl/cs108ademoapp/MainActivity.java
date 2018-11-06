package com.csl.cs108ademoapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.DrawerListContent.DrawerPositions;
import com.csl.cs108ademoapp.adapters.DrawerListAdapter;
import com.csl.cs108ademoapp.fragments.*;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    final boolean DEBUG = false; final String TAG = "Hello";
    public static boolean activityActive = false;

    //Tag to identify the currently displayed fragment
    Fragment fragment = null;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";

    public static TextView mLogView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;

    public static Context mContext;
    public static Cs108Library4A mCs108Library4a;
    public static SharedObjects sharedObjects;
    public static SensorConnector mSensorConnector;
    public static ReaderDevice tagSelected;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) Log.i(TAG, "MainActivity.onCreate: NULL savedInstanceState");
        else Log.i(TAG, "MainActivity.onCreate: VALID savedInstanceState");

        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mLogView = (TextView) findViewById(R.id.log_view);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, DrawerListContent.ITEMS));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mContext = this;
        sharedObjects = new SharedObjects(mContext);
        mCs108Library4a = new Cs108Library4A(mContext, mLogView);
        mSensorConnector = new SensorConnector(mContext);

        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> lst = imeManager.getInputMethodList();
        for (InputMethodInfo info : lst) {
//            MainActivity.mCs108Library4a.appendToLog(info.getId() + " " + info.loadLabel(getPackageManager()).toString());
        }
//        Intent intent = new Intent(MainActivity.this, CustomIME.class);
 //       startService(intent);
//        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) selectItem(DrawerPositions.MAIN);
        Log.i(TAG, "MainActivity.onCreate.onCreate: END");
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "MainActivity.onRestart.onRestart");
        super.onRestart();
        MainActivity.mCs108Library4a.connect(null);
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true; wedged = false;
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onResume()");
    }

    @Override
    protected void onPause() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onPause()");
        activityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onStop()");
        //serviceArrayList.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onDestroy()");
        if (true) { mCs108Library4a.disconnect(true); if (DEBUG) mCs108Library4a.appendToLog("done"); }
        super.onDestroy();
//        finishAffinity();
    }

    boolean configureDisplaying = false;
    Toast configureToast;
    private final Runnable configureRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity.mCs108Library4a.appendToLog("AAA: mrfidToWriteSize = " + mCs108Library4a.mrfidToWriteSize());
            if (mCs108Library4a.mrfidToWriteSize() != 0) {
                MainActivity.mCs108Library4a.mrfidToWritePrint();
                configureDisplaying = true;
                mHandler.postDelayed(configureRunnable, 500);
            } else {
                configureDisplaying = false;
                progressDialog.dismiss();
            }
        }
    };

    CustomProgressDialog progressDialog;
    private void selectItem(DrawerPositions position) {
        Log.i(TAG, "MainActivity.selectItem: position = " + position);
        if (false && position != DrawerPositions.MAIN && position != DrawerPositions.ABOUT &&  position != DrawerPositions.CONNECT && mCs108Library4a != null) {
            if (MainActivity.mCs108Library4a.isRfidFailure() == false && mCs108Library4a.mrfidToWriteSize() != 0) {
                if (configureDisplaying == false) {
                    progressDialog = new CustomProgressDialog(this, "Initializing reader. Please wait.");
                    progressDialog.show();
                    mHandler.post(configureRunnable);
                }
                return;
            }
        }
        if (true && position != DrawerPositions.MAIN && position != DrawerPositions.ABOUT && position != DrawerPositions.CONNECT && mCs108Library4a.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, "Bluetooth Disconnected.  Please Connect.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (position) {
            case MAIN:
                fragment = new HomeFragment();
                break;
            case SPECIAL:
                fragment = new HomeSpecialFragment();
                break;
            case ABOUT:
                fragment = new AboutFragment();
                break;
            case CONNECT:
                fragment = new ConnectionFragment();
                break;
            case INVENTORY:
                fragment = new InventoryFragment();
                break;
            case SEARCH:
                fragment = new InventoryRfidSearchFragment();
                break;
            case MULTIBANK:
                fragment = InventoryRfidiMultiFragment.newInstance(true, null, false);
                break;
            case SETTING:
                fragment = new SettingFragment();
                break;
            case FILTER:
                fragment = new SettingFilterFragment();
                break;
            case READWRITE:
                fragment = new AccessReadWriteFragment();
                break;
            case SECURITY:
                fragment = new AccessSecurityFragment();
                break;
            case REGISTER:
                fragment = new AccessRegisterFragment();
                break;
            case COLDCHAIN:
                fragment = new ColdChainFragment();
                break;
            case MICROTEMPERATURE:
                fragment = new MicronFragment();
                break;
            case UCODE:
                fragment = new UcodeFragment();
                break;
            case WEDGE:
                fragment = new HomeSpecialFragment();
                break;
            case BLANK:
//                fragment = new BlankFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (position == DrawerPositions.MAIN) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }

        // update selected item and title, then close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mOptionTitles[position]);  // redundent instructions
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        mDrawerList.setItemChecked(0, true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        super.onBackPressed();
    }

    public static boolean permissionRequesting;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MainActivity.mCs108Library4a.appendToLog("onRequestPermissionsResult ====");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequesting = false;
    }


    public void sfnClicked(View view) {
        selectItem(DrawerPositions.SPECIAL);
    }

    public void connectClicked(View view) {
        selectItem(DrawerPositions.CONNECT);
    }

    public void invClicked(View view) { selectItem(DrawerPositions.INVENTORY); }

    public void locateClicked(View view) {
        selectItem(DrawerPositions.SEARCH);
    }

    public void multiBankClicked(View view) {
        selectItem(DrawerPositions.MULTIBANK);
    }

    public void settClicked(View view) {
        selectItem(DrawerPositions.SETTING);
    }

    public void filterClicked(View view) {
        selectItem(DrawerPositions.FILTER);
    }

    public void rrClicked(View view) {
        selectItem(DrawerPositions.READWRITE);
    }

    public void accessClicked(View view) {
        selectItem(DrawerPositions.SECURITY);
    }

    public void regClicked(View view) {
        selectItem(DrawerPositions.REGISTER);
    }

    public void coldChainClicked(View view) {
        selectItem(DrawerPositions.COLDCHAIN);
    }

    public void microTemperatureClicked(View view) {
        selectItem(DrawerPositions.MICROTEMPERATURE);
    }

    public void uCodeClicked(View view) {
        selectItem(DrawerPositions.UCODE);
    }

    static boolean wedged = false;
    public void wedgeClicked(View view) {
        if (true) {
            wedged = true;
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
    }

    public void blankClicked(View view) {
        selectItem(DrawerPositions.BLANK);
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "MainActivity.onItemClick: position = " + position + ", id = " + id);
            selectItem(DrawerListContent.DrawerPositions.toDrawerPosition(position));
        }
    }
}
