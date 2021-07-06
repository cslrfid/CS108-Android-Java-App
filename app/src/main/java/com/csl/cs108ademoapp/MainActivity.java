package com.csl.cs108ademoapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.UnsupportedEncodingException;
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

    public static NfcAdapter nfcAdapter = null;
    PendingIntent mPendingIntent;
    IntentFilter writeTagFilters[];
    String[][] techList;

    public static String mDid; public static int selectHold; public static int selectFor;
    public static class Config {
        public String configPassword, configPower, config0, config1, config2, config3;
    };
    public static Config config  = new Config();

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

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) MainActivity.mCs108Library4a.appendToLog("onNewIntent !!! This device doesn't support NFC");
        else if (nfcAdapter.isEnabled() == false) MainActivity.mCs108Library4a.appendToLog("onNewIntent !!! This device doesn't enable NFC");
        else {
            readFromIntent(getIntent());

            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[] { tagDetected };
            techList = new String[][]{{android.nfc.tech.Ndef.class.getName()}, {android.nfc.tech.NdefFormatable.class.getName()}};
        }

        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) selectItem(DrawerPositions.MAIN);
        Log.i(TAG, "MainActivity.onCreate.onCreate: END");
    }

    @Override
    protected void onRestart() {
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
        if (nfcAdapter != null && nfcAdapter.isEnabled()) nfcAdapter.enableForegroundDispatch(this, mPendingIntent, writeTagFilters, techList);
        activityActive = true; wedged = false;
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onResume()");
    }

    @Override
    protected void onPause() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onPause()");
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
        activityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onDestroy()");
        if (true) { mCs108Library4a.disconnect(true); }
        super.onDestroy();
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
                mDid = null;
                fragment = InventoryRfidiMultiFragment.newInstance(true, null);
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
            case COLDCHAIN:
                fragment = new ColdChainFragment();
                break;
            case AXZON:
                fragment = AxzonSelectorFragment.newInstance(true);
                break;
            case RFMICRON:
                fragment = AxzonSelectorFragment.newInstance(false);
                break;
            case CTESIUS:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "E203510");
                break;
            case FDMICRO:
                fragment = new FdmicroFragment();
                break;
            case UCODE:
                fragment = new UcodeFragment();
                break;
            case UCODE8:
                fragment = new Ucode8Fragment();
                break;
            case BAPCARD:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "E200B0");
                break;
            case IMPINVENTORY:
                fragment = new ImpinjFragment();
                break;
            case AURASENSE:
                fragment = new AuraSenseFragment();
                break;
            case REGISTER:
                fragment = new AccessRegisterFragment();
                break;
            case READWRITEUSER:
                fragment = new AccessReadWriteUserFragment();
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
        mDrawerList.setItemChecked(0, true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        MainActivity.mCs108Library4a.appendToLog("MainActivity super.onBackPressed");
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

    public void privacyClicked(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://www.convergence.com.hk/apps-privacy-policy"));
        startActivity(intent);
    }
    public void aboutClicked(View view) {
//        MainActivity.mCs108Library4a.macRead(8);

//        MainActivity.mCs108Library4a.setRfidOn(false);

//        MainActivity.mCs108Library4a.barcodeSendCommandTrigger();
//        MainActivity.mCs108Library4a.setVibrateOn(1);
//        MainActivity.mCs108Library4a.barcodeReadTriggerStart();
//        MainActivity.mCs108Library4a.setBarcodeOn(false);

//        MainActivity.mCs108Library4a.getAutoBarStartSTop();
//        MainActivity.mCs108Library4a.setAutoBarStartSTop(false);
//        MainActivity.mCs108Library4a.getAutoRFIDAbort();
//        MainActivity.mCs108Library4a.setAutoRFIDAbort(false);
//        MainActivity.mCs108Library4a.setBatteryAutoReport(true);
//        MainActivity.mCs108Library4a.triggerButtoneStatusRequest();
//        MainActivity.mCs108Library4a.batteryLevelRequest();

//        MainActivity.mCs108Library4a.resetSiliconLab();
//        MainActivity.mCs108Library4a.getModelName();
//        MainActivity.mCs108Library4a.getHostProcessorICSerialNumber();
//        MainActivity.mCs108Library4a.hostProcessorICGetFirmwareVersion();

//        MainActivity.mCs108Library4a.forceBTdisconnect();
//        MainActivity.mCs108Library4a.getBluetoothICFirmwareName();
//        MainActivity.mCs108Library4a.setBluetoothICFirmwareName("CS109 Reader TT");
//        MainActivity.mCs108Library4a.getBluetoothICFirmwareVersion();

        selectItem(DrawerPositions.ABOUT);
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
    public void rrUserClicked(View view) { selectItem(DrawerPositions.READWRITEUSER); }

    public void accessClicked(View view) {
        selectItem(DrawerPositions.SECURITY);
    }

    public void regClicked(View view) {
        selectItem(DrawerPositions.REGISTER);
    }

    public void coldChainClicked(View view) { selectItem(DrawerPositions.COLDCHAIN); }
    public void bapCardClicked(View view) { selectItem(DrawerPositions.BAPCARD); }
    public void ctesiusClicked(View view) { selectItem(DrawerPositions.CTESIUS); }
    public void fdmicroClicked(View view) { selectItem(DrawerPositions.FDMICRO); }

    public void axzonClicked(View view) { selectItem(DrawerPositions.AXZON); }
    public void rfMicronClicked(View view) { selectItem(DrawerPositions.RFMICRON); }

    public void uCodeClicked(View view) { selectItem(DrawerPositions.UCODE); }
    public void uCode8Clicked(View view) { selectItem(DrawerPositions.UCODE8); }

    public void impInventoryClicked(View view) { selectItem(DrawerPositions.IMPINVENTORY); }
    public void aurasenseClicked(View view) { selectItem(DrawerPositions.AURASENSE); }

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
//        selectItem(DrawerPositions.BLANK);
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "MainActivity.onItemClick: position = " + position + ", id = " + id);
            selectItem(DrawerListContent.DrawerPositions.toDrawerPosition(position));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCs108Library4a.appendToLog("onNewIntent !!! intent.getAction = " + intent.getAction());
        readFromIntent(intent);
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//        }
    }
    private void readFromIntent(Intent intent) {
        mCs108Library4a.appendToLog("onNewIntent !!! readFromIntent entry");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            mCs108Library4a.appendToLog("onNewIntent !!! readFromIntent getAction = " + action);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            mCs108Library4a.appendToLog("onNewIntent !!! readFromIntent rawMsgs.length = " + rawMsgs.length);
            mCs108Library4a.appendToLog("onNewIntent !!! readFromIntent rawMsgs[0].toString = " + rawMsgs[0].toString());
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;
        mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs.length = " + msgs.length + ", msgs[0].getRecords().size = " + msgs[0].getRecords().length);

        String text = "";
        for (int x = 0; x < msgs.length; x++) {
        for (int y = 0; y < msgs[0].getRecords().length; y++) {
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs[" + x + "][" + y + "].Inf = " + msgs[x].getRecords()[y].getTnf());
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs[" + x + "][" + y + "].Type = " + mCs108Library4a.byteArrayToString(msgs[x].getRecords()[y].getType()));
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs[" + x + "][" + y + "].Id = " + mCs108Library4a.byteArrayToString(msgs[x].getRecords()[y].getId()));
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs[" + x + "][" + y + "].Payload = " + mCs108Library4a.byteArrayToString(msgs[x].getRecords()[y].getPayload()));
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews msgs[" + x + "][" + y + "].Class = " + msgs[x].getRecords()[y].getClass().toString());
        }}

        byte[] payload = msgs[0].getRecords()[0].getPayload();
        mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews payload.length = " + payload.length + ", with payload[0] = " + payload[0]);
        mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews payload = " + mCs108Library4a.byteArrayToString(payload));
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews text = " + text);
        } catch (UnsupportedEncodingException e) {
            mCs108Library4a.appendToLog("onNewIntent !!! buildTagViews UnsupportedEncoding" + e.toString());
            Log.e("UnsupportedEncoding", e.toString());
        }
        //tvNFCContent.setText("NFC Content: " + text);
    }
}
