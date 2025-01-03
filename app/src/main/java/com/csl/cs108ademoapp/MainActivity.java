package com.csl.cs108ademoapp;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_ALIEN;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_ASYGNTAG;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_CTESIUS;
import static com.csl.cslibrary4a.RfidReader.TagType.TAG_EM_BAP;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.csl.cs108ademoapp.DrawerListContent.DrawerPositions;
import com.csl.cs108ademoapp.adapters.DrawerListAdapter;
import com.csl.cs108ademoapp.fragments.AboutFragment;
import com.csl.cs108ademoapp.fragments.AccessReadWriteFragment;
import com.csl.cs108ademoapp.fragments.AccessReadWriteUserFragment;
import com.csl.cs108ademoapp.fragments.AccessRegisterFragment;
import com.csl.cs108ademoapp.fragments.AccessSecurityFragment;
import com.csl.cs108ademoapp.fragments.AuraSenseFragment;
import com.csl.cs108ademoapp.fragments.AxzonSelectorFragment;
import com.csl.cs108ademoapp.fragments.ColdChainFragment;
import com.csl.cs108ademoapp.fragments.ConnectionFragment;
import com.csl.cs108ademoapp.fragments.DirectWedgeFragment;
import com.csl.cs108ademoapp.fragments.FdmicroFragment;
import com.csl.cs108ademoapp.fragments.HomeFragment;
import com.csl.cs108ademoapp.fragments.HomeSpecialFragment;
import com.csl.cs108ademoapp.fragments.ImpinjFragment;
import com.csl.cs108ademoapp.fragments.ImpinjM775Fragment;
import com.csl.cs108ademoapp.fragments.InventoryFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidSearchFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidSimpleFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;
import com.csl.cs108ademoapp.fragments.KilowayFragment;
import com.csl.cs108ademoapp.fragments.LongjingFragment;
import com.csl.cs108ademoapp.fragments.SettingFilterFragment;
import com.csl.cs108ademoapp.fragments.SettingFragment;
import com.csl.cs108ademoapp.fragments.TestFragment;
import com.csl.cs108ademoapp.fragments.Ucode8Fragment;
import com.csl.cs108ademoapp.fragments.UcodeFragment;
import com.csl.cslibrary4a.Cs108Library4A;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;

public class MainActivity extends AppCompatActivity {
    final boolean DEBUG = false; final String TAG = "Hello";
    public static boolean activityActive = false;
    public static DrawerPositions drawerPositionsDefault = DrawerPositions.MAIN;

    //Tag to identify the currently displayed fragment
    Fragment fragment = null;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    public static boolean isHomeFragment = false;

    public static TextView mLogView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;

    public static Context mContext;
    public static Cs108Library4A csLibrary4A;
    public static SharedObjects sharedObjects;
    public static SensorConnector mSensorConnector;
    public static ReaderDevice tagSelected;

    Handler mHandler = new Handler();

    public static String mDid; public static int selectHold; public static int selectFor;
    public static RfidReader.TagType tagType;
    public static class Config {
        public String configPassword, configPower, config0, config1, config2, config3;
    };
    public static Config config  = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            if (savedInstanceState == null) Log.i(TAG, "MainActivity.onCreate: NULL savedInstanceState");
            else Log.i(TAG, "MainActivity.onCreate: VALID savedInstanceState");
        }
        Log.i("Hello", "PackageName is " + getPackageName());
        if (getPackageName().matches("com.csl.updwedgeservice")) setContentView(R.layout.activity_main1);
        else setContentView(R.layout.activity_main);

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mLogView = (TextView) findViewById(R.id.log_view);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, DrawerListContent.ITEMS));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mContext = this;
        sharedObjects = new SharedObjects(mContext);
        csLibrary4A = new Cs108Library4A(mContext, mLogView);
        mSensorConnector = new SensorConnector(mContext);

        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) selectItem(drawerPositionsDefault);
        if (true) Log.i(TAG, "MainActivity.onCreate.onCreate: END");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MainActivity.csLibrary4A.connect(null);
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true; wedged = false;
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onResume()");
    }

    @Override
    protected void onPause() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onPause()");
        activityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onDestroy()");
        if (true) { csLibrary4A.disconnect(true); }
        csLibrary4A = null;
        super.onDestroy();
    }

    boolean configureDisplaying = false;
    Toast configureToast;
    private final Runnable configureRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("AAA: mrfidToWriteSize = " + csLibrary4A.mrfidToWriteSize());
            if (csLibrary4A.mrfidToWriteSize() != 0) {
                MainActivity.csLibrary4A.mrfidToWritePrint();
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
        if (DEBUG) Log.i(TAG, "MainActivity.selectItem: position = " + position);
        if (position != DrawerPositions.MAIN
                && position != DrawerPositions.SPECIAL
                && position != DrawerPositions.ABOUT
                && position != DrawerPositions.CONNECT
                && position != DrawerPositions.DIRECTWEDGE && csLibrary4A.isBleConnected() == false) {
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
                //fragment = new Test1Fragment();
                fragment = new AboutFragment();
                break;
            case CONNECT:
                fragment = new ConnectionFragment();
                break;
            case INVENTORY:
                fragment = new InventoryFragment();
                break;
            case SEARCH:
                fragment = new InventoryRfidSearchFragment(false);
                break;
            case MULTIBANK:
                fragment = InventoryRfidiMultiFragment.newInstance(true, null, null);
                break;
            case SIMINVENTORY:
                fragment = InventoryRfidSimpleFragment.newInstance(false, null);
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

            case IMPINVENTORY:
                fragment = new ImpinjFragment();
                break;
            case IMP775:
                fragment = new ImpinjM775Fragment();
                break;
            case ALIEN:
                fragment = InventoryRfidiMultiFragment.newInstance(true, TAG_ALIEN, "E2003");
                break;
            case UCODE8:
                fragment = new Ucode8Fragment();
                break;
            case UCODEDNA:
                fragment = new UcodeFragment();
                break;
            case BAPCARD:
                fragment = InventoryRfidiMultiFragment.newInstance(true, TAG_EM_BAP, "E200B0");
                break;
            case COLDCHAIN:
                fragment = new ColdChainFragment();
                break;
            case AURASENSE:
                fragment = new AuraSenseFragment();
                break;
            case KILOWAY:
                fragment = new KilowayFragment();
                break;
            case LONGJING:
                fragment = new LongjingFragment();
                break;
            case AXZON:
                fragment = AxzonSelectorFragment.newInstance(true);
                break;
            case RFMICRON:
                fragment = AxzonSelectorFragment.newInstance(false);
                break;
            case FDMICRO:
                fragment = new FdmicroFragment();
                break;
            case CTESIUS:
                fragment = InventoryRfidiMultiFragment.newInstance(true, TAG_CTESIUS, "E203510");
                break;
            case ASYGNTAG:
                fragment = InventoryRfidiMultiFragment.newInstance(true, TAG_ASYGNTAG, "E283A");
                break;

            case REGISTER:
                fragment = new AccessRegisterFragment();
                break;
            case READWRITEUSER:
                fragment = new AccessReadWriteUserFragment();
                break;
            case WEDGE:
            case DIRECTWEDGE:
                fragment = new DirectWedgeFragment();
                break;
            case BLANK:
                fragment = new TestFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (position == drawerPositionsDefault) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            //fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        if (drawerPositionsDefault != DrawerPositions.MAIN) return;
        mDrawerList.setItemChecked(0, true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("MainActivity super.onBackPressed");
        super.onBackPressed();
    }

    public static boolean permissionRequesting;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("permissionRequesting: requestCode = " + requestCode + ", permissions is " + (permissions == null ? "null" : "valid") + ", grantResults is " + (grantResults == null ? "null" : "valid") );
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("permissionRequesting: permissions[" + permissions.length + "] = " + (permissions != null && permissions.length > 0 ? permissions[0] : ""));
        if (grantResults != null && grantResults.length != 0) {
            boolean bNegative = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] < 0) bNegative = true;
                if (DEBUG) csLibrary4A.appendToLog("permissionRequesting: grantResults[" + i + "] = " + grantResults[i] );
            }
            if (bNegative) {
                Toast toast = Toast.makeText(this, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT);
                if (false) toast.setGravity(Gravity.TOP | Gravity.RIGHT, 100, 200);
                toast.show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequesting = false;
    }

    public void sfnClicked(View view) {
        selectItem(DrawerPositions.SPECIAL);
    }

    public void privacyClicked(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://www.convergence.com.hk/apps-privacy-policy"));
        startActivity(intent);
    }

    public void aboutClicked(View view) { selectItem(DrawerPositions.ABOUT); }
    public void connectClicked(View view) { selectItem(DrawerPositions.CONNECT); }
    public void invClicked(View view) { selectItem(DrawerPositions.INVENTORY); }
    public void locateClicked(View view) {
        selectItem(DrawerPositions.SEARCH);
    }
    public void multiBankClicked(View view) { selectItem(DrawerPositions.MULTIBANK); }
    public void settClicked(View view) {
        selectItem(DrawerPositions.SETTING);
    }
    public void filterClicked(View view) {
        selectItem(DrawerPositions.FILTER);
    }
    public void rrClicked(View view) {
        selectItem(DrawerPositions.READWRITE);
    }
    public void rrUserClicked(View view) { selectItem(DrawerPositions.READWRITEUSER); }

    public void accessClicked(View view) {
        selectItem(DrawerPositions.SECURITY);
    }

    public void impInventoryClicked(View view) { selectItem(DrawerPositions.IMPINVENTORY); }
    public void m775Clicked(View view) { selectItem(DrawerPositions.IMP775); }
    public void alienClicked(View view) { selectItem(DrawerPositions.ALIEN); }
    public void uCode8Clicked(View view) { selectItem(DrawerPositions.UCODE8); }
    public void uCodeClicked(View view) { selectItem(DrawerPositions.UCODEDNA); }
    public void bapCardClicked(View view) { selectItem(DrawerPositions.BAPCARD); }
    public void coldChainClicked(View view) { selectItem(DrawerPositions.COLDCHAIN); }
    public void aurasenseClicked(View view) { selectItem(DrawerPositions.AURASENSE); }
    public void kilowayClicked(View view) { selectItem(DrawerPositions.KILOWAY); }
    public void longjingClicked(View view) { selectItem(DrawerPositions.LONGJING); }
    public void axzonClicked(View view) { selectItem(DrawerPositions.AXZON); }
    public void rfMicronClicked(View view) { selectItem(DrawerPositions.RFMICRON); }
    public void fdmicroClicked(View view) { selectItem(DrawerPositions.FDMICRO); }
    public void ctesiusClicked(View view) { selectItem(DrawerPositions.CTESIUS); }
    public void asygnClicked(View view) { selectItem(DrawerPositions.ASYGNTAG); }

    public void regClicked(View view) { selectItem(DrawerPositions.REGISTER); }
    public static boolean wedged = false;
    public void wedgeClicked(View view) {
        if (false) {
            wedged = true;
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        } else selectItem(DrawerPositions.WEDGE);
    }
    public void directWedgeClicked(View view) {
        selectItem(DrawerPositions.DIRECTWEDGE);
    }

    public void simpleInventoryClicked(View view) { selectItem(DrawerPositions.SIMINVENTORY); }

    public void blankClicked(View view) { if (false) selectItem(DrawerPositions.BLANK); }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (DEBUG) Log.i(TAG, "MainActivity.onItemClick: position = " + position + ", id = " + id);
            selectItem(DrawerPositions.toDrawerPosition(position));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) csLibrary4A.appendToLog("onNewIntent !!! intent.getAction = " + intent.getAction());
        readFromIntent(intent);
    }
    private void readFromIntent(Intent intent) {
        if (DEBUG) csLibrary4A.appendToLog("onNewIntent !!! readFromIntent entry");
        String action = intent.getAction();
    }
}
