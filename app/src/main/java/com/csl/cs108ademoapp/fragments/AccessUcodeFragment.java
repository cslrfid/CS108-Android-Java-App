package com.csl.cs108ademoapp.fragments;

import static com.csl.cs108ademoapp.MainActivity.csLibrary4A;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.SaveList2ExternalTask;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cslibrary4a.AesCmac;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AccessUcodeFragment extends CommonFragment {
    final boolean DEBUG = true; int iTagType = -1;
    SelectTag selectTag;
    TextView textViewAesKey0ActivateOk, textViewAesKey1ActivateOk, textViewAesKey0Ok, textViewAesKey1Ok;
    Spinner spinnerHideTid;
    CheckBox checkBoxAuthEncryptMode, checkBoxAuthValidMode;
    CheckBox checkBoxHideEpc, checkBoxHideTid, checkBoxHideUser, checkBoxHideRange;
    CheckBox checkBoxAesKey0Activate, checkBoxAesKey1Activate, checkBoxAesKey0, checkBoxAesKey1;

    EditText editTextAuthKeyId, editTextAuthMsg, editTextAuthProfile, editTextAuthOffset, editTextAuthBlockId, editTextAuthProtMode, editTextEpcSize, editTextAesKey0, editTextAesKey1, editTextAuthResponse;
    TextView textViewAuthResponseDecoded, textViewAuthResponseDecodedCustom, editTextAuthResponseEncodedMac, textViewImpinjResponse;
    EditText editTextAccessUCTid, editTextAccessUCserverImpinj, editTextAccessUCemail, editTextAccessUCpassword;
    private Button buttonRead, buttonWrite;
    private Button buttonReadBuffer, buttonTam1, buttonTam2, buttonUntrace, buttonShowEpc; String strShowEpcButtonBackup;

    enum ReadWriteTypes {
        NULL, TEMPERATURE, AESKEY0, AESKEY1, AESKEY0ACTIVATE, AESKEY1ACTIVATE, ENABLE
    }
    boolean operationRead = false;
    boolean readBufferChecked = false;
    boolean authenChecked = false; boolean authenTam1; int keyId, profile, offset, blockId, protMode; String strChallenge;
    boolean untraceChecked = false;
    boolean showEpcChecked = false;
    ReadWriteTypes readWriteTypes;
    boolean bImpinJTag = false;

    private AccessTask accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_ucode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (MainActivity.mDid != null) if (MainActivity.mDid.contains("E28240")) iTagType = 5;

        selectTag = new SelectTag((Activity)getActivity(), 1);
        if (MainActivity.mDid != null && MainActivity.mDid.indexOf("E2801") == 0) bImpinJTag = true;

        spinnerHideTid = (Spinner) getActivity().findViewById(R.id.accessUCHideTid);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.hideTid_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHideTid.setAdapter(targetAdapter);

        checkBoxHideEpc = (CheckBox) getActivity().findViewById(R.id.accessUCHideEpc);
        checkBoxHideTid = (CheckBox) getActivity().findViewById(R.id.accessUCHideTid1);
        checkBoxHideUser = (CheckBox) getActivity().findViewById(R.id.accessUCHideUser);
        checkBoxHideRange = (CheckBox) getActivity().findViewById(R.id.accessUCHideRange);
        textViewAesKey0ActivateOk = (TextView) getActivity().findViewById(R.id.accessUCAesKey0ActivateOK);
        textViewAesKey1ActivateOk = (TextView) getActivity().findViewById(R.id.accessUCAesKey1ActivateOK);
        checkBoxAesKey0Activate = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey0Activate);
        checkBoxAesKey1Activate = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey1Activate);

        TableRow tableRow = (TableRow) getActivity().findViewById(R.id.accessUCAesKeysRow);
        if (iTagType == 5) tableRow.setVisibility(View.GONE);
        textViewAesKey0Ok = (TextView) getActivity().findViewById(R.id.accessUCAesKey0OK);
        textViewAesKey1Ok = (TextView) getActivity().findViewById(R.id.accessUCAesKey1OK);
        checkBoxAesKey0 = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey0Title);
        checkBoxAesKey1 = (CheckBox) getActivity().findViewById(R.id.accessUCAesKey1Title);

        editTextAuthKeyId = (EditText) getActivity().findViewById(R.id.accessUCAuthKeyId);
        editTextAuthKeyId.setText(String.valueOf(0));
        editTextAuthMsg = (EditText) getActivity().findViewById(R.id.accessUCAuthMsg);
        editTextAuthMsg.addTextChangedListener(new GenericTextWatcher(editTextAuthMsg, bImpinJTag ? 12 : 20));
        editTextAuthProfile = (EditText) getActivity().findViewById(R.id.accessUCAuthProfile);
        editTextAuthProfile.setText(String.valueOf(0));
        editTextAuthOffset = (EditText) getActivity().findViewById(R.id.accessUCAuthOffset);
        editTextAuthOffset.setText(String.valueOf(0));
        editTextAuthBlockId = (EditText) getActivity().findViewById(R.id.accessUCAuthBlockId);
        editTextAuthBlockId.setText(String.valueOf(1));

        TextView textViewAuthProtModeLabel = (TextView) getActivity().findViewById(R.id.accessUCAuthProtModeLabel);
        editTextAuthProtMode = (EditText) getActivity().findViewById(R.id.accessUCAuthProtMode);
        TableRow tableRowAuthProtMode = (TableRow) getActivity().findViewById(R.id.accessUCAuthtModeRow);
        checkBoxAuthEncryptMode = (CheckBox) getActivity().findViewById(R.id.accessUCAuthEncryptMode);
        checkBoxAuthValidMode = (CheckBox) getActivity().findViewById(R.id.accessUCAuthValidMode);
        if (iTagType == 5) {
            textViewAuthProtModeLabel.setVisibility(View.GONE);
            editTextAuthProtMode.setVisibility(View.GONE);
        } else {
            tableRowAuthProtMode.setVisibility(View.GONE);
            editTextAuthProtMode.setText(String.valueOf(1));
        }

        editTextAuthResponse = (EditText) getActivity().findViewById(R.id.accessUCAuthResponse);
        textViewAuthResponseDecoded = (TextView) getActivity().findViewById(R.id.accessUCAuthResponseDecoded);
        textViewAuthResponseDecodedCustom = (TextView) getActivity().findViewById(R.id.accessUCAuthResponseDecodedCustom);
        editTextAuthResponseEncodedMac = (TextView) getActivity().findViewById(R.id.accessUCAuthResponseEecodedMac);
        textViewImpinjResponse = (TextView) getActivity().findViewById(R.id.accessUCImpinjResponse);
        editTextEpcSize = (EditText) getActivity().findViewById(R.id.accessUCEpcSize);
        editTextAesKey0 = (EditText) getActivity().findViewById(R.id.accessUCAesKey0);
        editTextAesKey0.addTextChangedListener(new GenericTextWatcher(editTextAesKey0, 32));
        editTextAesKey1 = (EditText) getActivity().findViewById(R.id.accessUCAesKey1);
        editTextAesKey1.addTextChangedListener(new GenericTextWatcher(editTextAesKey1, 32));

        buttonRead = (Button) getActivity().findViewById(R.id.accessUCReadButton);
        if (iTagType == 5) buttonRead.setVisibility(View.GONE);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = true; startAccessTask();
            }
        });

        buttonWrite = (Button) getActivity().findViewById(R.id.accessUCWriteButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                operationRead = false; startAccessTask();
            }
        });

        buttonReadBuffer = (Button) getActivity().findViewById(R.id.accessUCReadBufferButton);
        buttonReadBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                readBufferChecked = true; startAccessTask();
            }
        });

        buttonTam1 = (Button) getActivity().findViewById(R.id.accessUCTam1AuthButton);
        buttonTam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.csLibrary4A.appendToLog("buttonTam1 is pressed");
                authenTam1 = true; authenChecked = true; keyId = Integer.parseInt(editTextAuthKeyId.getText().toString()); strChallenge = editTextAuthMsg.getText().toString();
                startAccessTask();
            }
        });

        buttonTam2 = (Button) getActivity().findViewById(R.id.accessUCTam2AuthButton);
        buttonTam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.csLibrary4A.appendToLog("buttonTam2 is pressed");
                authenTam1 = false; authenChecked = true; keyId = Integer.parseInt(editTextAuthKeyId.getText().toString()); strChallenge = editTextAuthMsg.getText().toString();
                profile = Integer.parseInt(editTextAuthProfile.getText().toString());
                offset = Integer.parseInt(editTextAuthOffset.getText().toString());
                blockId = Integer.parseInt(editTextAuthBlockId.getText().toString());
                if (iTagType != 5)  protMode = Integer.parseInt(editTextAuthProtMode.getText().toString());
                else {
                    protMode = 0;
                    if (checkBoxAuthEncryptMode.isChecked()) protMode += 1;
                    if (checkBoxAuthValidMode.isChecked()) protMode += 2;
                }
                startAccessTask();
            }
        });

        buttonUntrace = (Button) getActivity().findViewById(R.id.accessUCUntraceButton);
        buttonUntrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                untraceChecked = true; startAccessTask();
            }
        });

        tableRow = (TableRow) getActivity().findViewById(R.id.accessUCShowEpcRow);
        if (iTagType == 5) tableRow.setVisibility(View.GONE);
        buttonShowEpc = (Button) getActivity().findViewById(R.id.accessUCShowEpcButton);
        buttonShowEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.csLibrary4A.isBleConnected() == false) {
                    Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (MainActivity.csLibrary4A.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                showEpcChecked = true; startAccessTask();
            }
        });

        TextView textViewWarning = (TextView) getActivity().findViewById(R.id.accessUCWarning);
        if (iTagType == 5) textViewWarning.setText("Notice: Xerxes assumes Key0 for Tam1 and Tam 2");
        else textViewWarning.setText("Notice: Ucode assumes Key1 for Tam2");

        MainActivity.csLibrary4A.getAuthenticateReplyLength();
        MainActivity.csLibrary4A.getUntraceableEpcLength();
        MainActivity.csLibrary4A.setSameCheck(false);

        MainActivity.csLibrary4A.appendToLog("mDid in AccessUcodeFragment = " + MainActivity.mDid);
        TextView textView = (TextView) getActivity().findViewById(R.id.accessUCAuthKeyIdLabel);
        EditText editText = (EditText) getActivity().findViewById(R.id.accessUCAuthKeyId);
        TableRow tableRow1 = (TableRow) getActivity().findViewById(R.id.accessUCAuthProfileRow);
        LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.accessUCKeyLayout);
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.accessUCButtons); layout.setVisibility(View.GONE);
        if (bImpinJTag) {
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            editTextAuthMsg.setText("049CA53E55EA");
            tableRow1.setVisibility(View.GONE);
            layout1.setVisibility(View.GONE);
        }

        Button buttonImpinjCheck = (Button) getActivity().findViewById(R.id.accessUCImpinjCheck);
        buttonImpinjCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bRunning) {
                    textViewImpinjResponse.setText("");
                    mHandler.post(runnable);
                }
            }
        });

        editTextAccessUCserverImpinj = (EditText) getActivity().findViewById(R.id.accessUCserverImpinj);
        editTextAccessUCemail = (EditText) getActivity().findViewById(R.id.accessUCemail);
        editTextAccessUCpassword = (EditText) getActivity().findViewById(R.id.accessUCpassword);
        if (false) {
            editTextAccessUCserverImpinj.setText(csLibrary4A.getServerImpinjLocation());
            editTextAccessUCemail.setText(csLibrary4A.getServerImpinjName());
            editTextAccessUCpassword.setText(csLibrary4A.getServerImpinjPassword());
        } else if (false) {
            editTextAccessUCserverImpinj.setText("https://h9tqczg9-7275.asse.devtunnels.ms");
            editTextAccessUCemail.setText("wallace.sit@cne.com.hk");
            editTextAccessUCpassword.setText("Cne12345678?");
        } else {
            editTextAccessUCserverImpinj.setText("https://democloud.convergence.com.hk/ias");
            editTextAccessUCemail.setText("wallace.sit@cne.com.hk");
            editTextAccessUCpassword.setText("Cne12345678?");
        }
    }

    SaveList2ExternalTask saveExternalTask; boolean bRunning = false; int bStep = 0; String strBearer = null;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            csLibrary4A.appendToLog("0 bRunning = " + bRunning + ", bStep = " + bStep);
            if (saveExternalTask != null && saveExternalTask.getStatus() == AsyncTask.Status.FINISHED) {
                bStep++;
                if (saveExternalTask.responseCode != 200) bStep++;
                else {
                    if (bStep == 1) strBearer = saveExternalTask.response;
                    else {
                        String strTid = null, strResult = null;
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(saveExternalTask.response);
                            JSONArray jsonArray = obj.getJSONArray("tagValidity");
                            MainActivity.csLibrary4A.appendToLog("Json tagValidity is " + (jsonArray == null ? "null" : "valid"));
                            strTid = jsonArray.getJSONObject(0).getString("tid");
                            strResult = jsonArray.getJSONObject(0).getString("tagValid");
                        } catch (JSONException e) {
                            csLibrary4A.appendToLog("Json exception = " + e.toString());
                        }
                        if (strTid != null && strResult != null) {
                            //textViewImpinjResponse.setText(strResult + " " + strTid);
                            textViewImpinjResponse.setText((strResult.matches("true") ? "Valid" : "Invalid"));
                        }
                    }
                }
                MainActivity.csLibrary4A.appendToLog("responseCode = " + saveExternalTask.responseCode + ", response = " + saveExternalTask.response);
                saveExternalTask = null;
            }
            csLibrary4A.appendToLog("bRunning = " + bRunning + ", bStep = " + bStep);
            if (bStep < 2) {
                if (saveExternalTask == null || saveExternalTask.getStatus() != AsyncTask.Status.RUNNING) {
                    csLibrary4A.appendToLog("1 bRunning = " + bRunning + ", bStep = " + bStep);
                    bRunning = true;
                    if (bStep == 0) {
                        bStep = 0;
                        saveExternalTask = new SaveList2ExternalTask(true);

                        JSONObject object = new JSONObject();
                        try {
                            //object.put("Username", "CslAuth2047#$");
                            //object.put("Password", "Sheungwan123#$");
                            object.put("email", editTextAccessUCemail.getText().toString()); //"wallace.sit@cne.com.hk");
                            object.put("password", editTextAccessUCpassword.getText().toString()); //"Cne12345678?");
                        } catch (Exception ex) {
                            MainActivity.csLibrary4A.appendToLog("Error in creating Json");
                        }
                        saveExternalTask.messageStr = object.toString();
                        //saveExternalTask.url = "https://ias.authenticate.impinj.io/external/jwt/authenticate";
                        saveExternalTask.url = editTextAccessUCserverImpinj.getText().toString() + "/api/Auth/login"; // "https://h9tqczg9-7275.asse.devtunnels.ms/api/Auth/login";
                        MainActivity.csLibrary4A.appendToLog("Adddress: " + saveExternalTask.url + ", json message:" + saveExternalTask.messageStr);
                        saveExternalTask.execute();
                        MainActivity.csLibrary4A.appendToLog("responseCode = " + saveExternalTask.responseCode + ", response = " + saveExternalTask.response);
                    } else {
                        saveExternalTask = new SaveList2ExternalTask(true);

                        JSONObject object = new JSONObject();
                        try {
                            JSONArray jsonArray = new JSONArray();
                            JSONObject object1 = new JSONObject();
                            object1.put("tid", editTextAccessUCTid.getText().toString()); //tagSelected.getTid()); //"E2C011A21234123412341234"); //
                            object1.put("challenge", editTextAuthMsg.getText().toString());
                            object1.put("tagResponse", editTextAuthResponse.getText().toString());
                            jsonArray.put(object1);
                            object.put("tagVerify", jsonArray);
                            object.put("sendSignature", true);
                            object.put("sendSalt", true);
                            object.put("sendTime", true);
                        } catch (Exception ex) {
                            MainActivity.csLibrary4A.appendToLog("Error in creating Json");
                        }
                        saveExternalTask.messageStr = object.toString();
                        saveExternalTask.url = editTextAccessUCserverImpinj.getText().toString() + "/api/ImpinjAuthentication/authenticate"; //"https://h9tqczg9-7275.asse.devtunnels.ms/api/ImpinjAuthentication/authenticate";
                        saveExternalTask.strBearer = "Bearer " + strBearer;
                        MainActivity.csLibrary4A.appendToLog("Adddress: " + saveExternalTask.url + ", json message:" + saveExternalTask.messageStr);
                        saveExternalTask.execute();
                        MainActivity.csLibrary4A.appendToLog("responseCode = " + saveExternalTask.responseCode + ", response = " + saveExternalTask.response);
                    }
                }
                mHandler.postDelayed(runnable, 100);
            } else {
                bRunning = false; bStep = 0;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setupTagID();
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        //MainActivity.mCs108Library4a.appendToLog("onDestroy");
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.csLibrary4A.appendToLog("AccessUcodeFragment is now VISIBLE");
            setupTagID();
            //            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.csLibrary4A.appendToLog("AccessUcodeFragment is now INVISIBLE");
//            MainActivity.mCs108Library4a.setNotificationListener(null);
        }
    }

    public AccessUcodeFragment() {
        super("AccessUcodeFragment");
    }

    void setupTagID() {
        if (selectTag == null) return;
        ReaderDevice tagSelected = MainActivity.tagSelected;
        MainActivity.csLibrary4A.appendToLog("Start with tagSelected = " + (tagSelected == null ? "NULL" : (tagSelected.getSelected() + ", " + tagSelected.getAddress())));
        boolean bSelected = false;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                bSelected = true;
                MainActivity.csLibrary4A.appendToLog("selectTag is " + (selectTag == null ? "NULL" : "valid"));
                if (selectTag != null) MainActivity.csLibrary4A.appendToLog("selectTag.editTextTag is " + (selectTag.editTextTagID == null ? "NULL" : "valid"));
                if (selectTag.editTextTagID != null) {
                    MainActivity.csLibrary4A.appendToLog("editTextRWTagID.setTTEXT " + tagSelected.getAddress());
                    selectTag.editTextTagID.setText(tagSelected.getAddress());
                }

                String stringDetail = tagSelected.getDetails();
                int indexUser = stringDetail.indexOf("USER=");
                if (indexUser != -1) {
                    String stringUser = stringDetail.substring(indexUser + 5);
                    MainActivity.csLibrary4A.appendToLog("stringUser = " + stringUser);

                    boolean bEnableBAPMode = false;
                    int number = Integer.valueOf(stringUser.substring(3, 4), 16);
                    if ((number % 2) == 1) bEnableBAPMode = true;
//                    CheckBox checkBoxBAP = (CheckBox) getActivity().findViewById(R.id.coldChainEnableBAP);
//                    checkBoxBAP.setChecked(bEnableBAPMode);
                }

                editTextAccessUCTid = (EditText) getActivity().findViewById(R.id.accessUCTid);
                editTextAccessUCTid.setText(tagSelected.getTid());
            }
        }
    }

    private byte[] doubleSubKey(byte[] k) {
        byte[] ret = new byte[k.length];

        boolean firstBitSet = ((k[0]&0x80) != 0);
        for (int i=0; i<k.length; i++) {
            ret[i] = (byte) (k[i] << 1);
            if (i+1 < k.length && ((k[i+1]&0x80) != 0)) {
                ret[i] |= 0x01;
            }
        }
        if (firstBitSet) {
            ret[ret.length-1] ^= (byte) 0x87;
        }
        return ret;
    }
    boolean processAESdata(String strData) {
        boolean retValue = false;
        String strKey, strKey0, strKey1;
        strKey0 = editTextAesKey0.getText().toString();
        strKey1 = editTextAesKey1.getText().toString();
        if (keyId == 0) strKey = strKey0;
        else if (keyId == 1) strKey = strKey1;
        else return retValue;

        if (strKey.length() != 32) return retValue;
        byte[] key = new byte[strKey.length() / 2];
        for (int i = 0; i < strKey.length(); i++) {
            int iTemp = Integer.parseInt(strKey.substring(i, i+1), 16);
            if (i % 2 == 0) key[i/2] = (byte)(iTemp << 4);
            else key[i/2] |= iTemp;
        }
        byte[] key1 = new byte[strKey1.length() / 2];
        for (int i = 0; i < strKey1.length(); i++) {
            int iTemp = Integer.parseInt(strKey1.substring(i, i+1), 16);
            if (i % 2 == 0) key1[i/2] = (byte)(iTemp << 4);
            else key1[i/2] |= iTemp;
        }

        int iAesCbcLength = (strData.length() / 32 ) * 32;
        if (iAesCbcLength == 0) return retValue;
        String strData1 = strData.substring(0, iAesCbcLength);

        byte[] dataIn = new byte[strData1.length() / 2];
        for (int i = 0; i < strData1.length(); i++) {
            int iTemp = Integer.parseInt(strData1.substring(i, i+1), 16);
            if (i % 2 == 0) dataIn[i/2] = (byte)(iTemp << 4);
            else dataIn[i/2] |= iTemp;
        }

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        try {
            String strAlgo = "AES/CBC/NoPadding";    //PKCS5Padding, NoPadding
            Cipher cipher = Cipher.getInstance(strAlgo);
            if (false) {
                byte[] data2enc = {(byte) 0x96, (byte) 0xC5, 0x4F, (byte) 0xA8, 0x1D, 0x3C,
                        (byte) 0xFD, 0x5D, (byte) 0x80, (byte) 0x48, (byte) 0xF4, (byte) 0x8D, (byte) 0xD0, (byte) 0x9A, (byte) 0xAD, 0x22};
                byte[] encValue = cipher.doFinal(data2enc);
                textViewAuthResponseDecoded.setText(MainActivity.csLibrary4A.byteArrayToString(encValue));
            } else {
                byte[] data2dec = dataIn;
                byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                byte[] decValue = cipher.doFinal(data2dec);
                byte[] decValue16 = new byte[16]; System.arraycopy(decValue, 0, decValue16, 0, 16);
                textViewAuthResponseDecoded.setText(MainActivity.csLibrary4A.byteArrayToString(decValue16));

                String strMatchResult = "Challenge";
                String strMatching = textViewAuthResponseDecoded.getText().toString();
                boolean bMatch = false;
                if (strMatching == null) { }
                else if (strMatching.length() != 32) { }
                else if (strMatching.substring(0, 4).matches("96C5") == false) { }
                else if (strMatching.substring(12).matches(strChallenge)) bMatch = true;
                if (bMatch) strMatchResult += " Matched"; else strMatchResult += " Not Matched";

                if (authenTam1 == false) {
                    if (protMode == 0 || protMode == 2) {
                        decValue16 = new byte[16];
                        System.arraycopy(data2dec, 16, decValue16, 0, 16);
                    } else {
                        decValue16 = new byte[16];
                        System.arraycopy(decValue, 16, decValue16, 0, 16);
                    }
                    textViewAuthResponseDecodedCustom.setText(MainActivity.csLibrary4A.byteArrayToString(decValue16));

                    if (protMode >= 2) {
                        if (true) {
                            AesCmac mac = null;
                            mac = new AesCmac();
                            secretKey = new SecretKeySpec(key1, "AES");
                            mac.init(secretKey);  //set master key
                            mac.updateBlock(dataIn); //given input
                            decValue = mac.doFinal();
                        } else if (true) {
                            cipher = Cipher.getInstance(strAlgo);
                            secretKey = new SecretKeySpec(key1, "AES");
                            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

                            // First calculate k0 from zero bytes
                            byte[] k0 = new byte[16];
                            cipher.update(k0, 0, k0.length, k0, 0);

                            // Calculate values for k1 and k2
                            byte[] k1 = doubleSubKey(k0);
                            byte[] k2 = doubleSubKey(k1);
                            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
                            int bufferCount;
                        } else if (false) {
                            secretKey = new SecretKeySpec(key1, "AES");
                            Mac hmac = Mac.getInstance("HmacSHA256"); //HmacMD5, HmacSHA1, HmacSHA256
                            hmac.init(secretKey);
                            hmac.update(iv);
                            decValue = hmac.doFinal(dataIn);
                            MainActivity.csLibrary4A.appendToLog("decValue1.length = " + decValue.length);
                        } else {
                            secretKey = new SecretKeySpec(key1, "AES");
                            cipher = Cipher.getInstance(strAlgo);
                            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
                            decValue = cipher.doFinal(dataIn);
                        }
                        String strMac = MainActivity.csLibrary4A.byteArrayToString(decValue).substring(0, 24);
                        editTextAuthResponseEncodedMac.setText(strMac);

                        strMatchResult += ", MAC";
                        strMatching = editTextAuthResponse.getText().toString();
                        while (strMatching.length() > 32)
                            strMatching = strMatching.substring(32).trim();
                        bMatch = false;
                        if (strMatching.matches(strMac)) bMatch = true;
                        if (bMatch) strMatchResult += " Matched";
                        else strMatchResult += " Not Matched";
                    }
                }
                Toast.makeText(MainActivity.mContext, strMatchResult, Toast.LENGTH_SHORT).show();
                retValue = true;
            }

            if (false) {
                secretKey = new SecretKeySpec(key, "HmacSHA256");
                try {
                    Mac hmac = Mac.getInstance("HmacSHA256");

                    byte[] data2dec = dataIn;
                    byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                    hmac.init(secretKey);
                    hmac.update(iv);
                    byte[] decValue = hmac.doFinal(data2dec);
                    MainActivity.csLibrary4A.appendToLog("decValue.length = " + decValue.length);
                    editTextAuthResponseEncodedMac.setText(MainActivity.csLibrary4A.byteArrayToString(decValue));
                    if (false) {
                        byte[] decValue16 = new byte[16];
                        System.arraycopy(decValue, 0, decValue16, 0, 16);
                        textViewAuthResponseDecoded.setText(MainActivity.csLibrary4A.byteArrayToString(decValue16));
                        if (protMode == 0 || protMode == 2) {
                            decValue16 = new byte[16];
                            System.arraycopy(data2dec, 16, decValue16, 0, 16);
                        } else {
                            decValue16 = new byte[16];
                            System.arraycopy(decValue, 16, decValue16, 0, 16);
                        }
                        textViewAuthResponseDecodedCustom.setText(MainActivity.csLibrary4A.byteArrayToString(decValue16));
                        retValue = true;
                    }
                } catch (Exception ex) {
                    MainActivity.csLibrary4A.appendToLog("Error while encrypting: " + ex.toString());
                }
            }
        } catch (Exception ex) {
            MainActivity.csLibrary4A.appendToLog("Error while encrypting: " + ex.toString());
        }
        return retValue;
    }
    void startAccessTask() {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("startAccessTask()");
        MainActivity.csLibrary4A.setInvAlgo(false);
        if (MainActivity.csLibrary4A.getRetryCount() < 2) MainActivity.csLibrary4A.setRetryCount(2);
        if (updating == false) {
            updating = true; bankProcessing = 0;
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) {
                rerunRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): accessReadWriteTask.getStatus() =  " + accessTask.getStatus().toString());
            } else {
                taskRequest = true;
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): FINISHED accessReadWriteTask");
            }
            if (processResult()) { rerunRequest = true; MainActivity.csLibrary4A.appendToLog("processResult is TRUE"); }
            else if (taskRequest) {
                boolean invalid = processTickItems();
                MainActivity.csLibrary4A.appendToLog("processTickItems, invalid = " + invalid);
                if (bankProcessing++ != 0 && invalid == true)   rerunRequest = false;
                else {
                    Button button;
                    if (readBufferChecked) button = buttonReadBuffer;
                    else if (authenChecked && authenTam1) button = buttonTam1;
                    else if (authenChecked) button = buttonTam2;
                    else if (untraceChecked) button = buttonUntrace;
                    else if (showEpcChecked) { if (strShowEpcButtonBackup == null) strShowEpcButtonBackup = buttonShowEpc.getText().toString(); buttonShowEpc.setText("Show"); button = buttonShowEpc; }
                    else if (operationRead) button = buttonRead;
                    else button = buttonWrite;

                    RfidReaderChipData.HostCommands hostCommand;
                    if (readBufferChecked) hostCommand = RfidReaderChipData.HostCommands.CMD_READBUFFER;
                    else if (authenChecked) hostCommand = RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE;
                    else if (untraceChecked || showEpcChecked) hostCommand = RfidReaderChipData.HostCommands.CMD_UNTRACEABLE;
                    else if (operationRead) hostCommand = RfidReaderChipData.HostCommands.CMD_18K6CREAD;
                    else hostCommand = RfidReaderChipData.HostCommands.CMD_18K6CWRITE;

                    accessTask = new AccessTask(button, null, invalid, true,
                            selectTag.editTextTagID.getText().toString(), 1, 32,
                            selectTag.editTextAccessPassword.getText().toString(), Integer.valueOf(selectTag.editTextAccessAntennaPower.getText().toString()), hostCommand,
                            0, 0, true, false,
                            null, null, null, null, null);
                    accessTask.execute();
                    rerunRequest = true;
                    MainActivity.csLibrary4A.appendToLog("accessTask is created");
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 500);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("updateRunnable(): Restart");
            }
            else    updating = false;
            MainActivity.csLibrary4A.appendToLog("updateRunnable(): Ending with updating = " + updating);
        }
    };

    short setTemperature(float fTemperature) {
        if (fTemperature > 63.75) fTemperature = (float) 63.75;
        else if (fTemperature < -64) fTemperature = -64;
        boolean bNegative = false;
        if (fTemperature < 0) { bNegative = true; fTemperature = 0 - fTemperature; }
        fTemperature += 0.125; fTemperature /= 0.25;
        short retValue = (short)fTemperature;
        if (bNegative) { retValue--; retValue &= 0xFF; retValue ^= 0xFF; retValue |= 0x100; }
        return  retValue;
    }
    String getTemperatue(String stringInput) {
        byte bValue = Byte.parseByte(stringInput.substring(0,1), 16);
        byte bValue2 = Byte.parseByte(stringInput.substring(1, 2), 16); bValue2 <<= 4;
        byte bValue3 = Byte.parseByte(stringInput.substring(2, 3), 16); bValue2 |= bValue3;
        String stringValue = ""; short sValue = (short)(bValue2 & 0xFF);
        if ((bValue & 0x01) != 0) { stringValue = "-"; bValue2 ^= 0xFF; sValue = (short)(bValue2 & 0xFF); sValue++; }
        stringValue += String.valueOf((sValue & 0x1FF) >> 2);
        switch (sValue & 0x03) {
            case 1:
                stringValue += ".25";
                break;
            case 2:
                stringValue += ".50";
                break;
            case 3:
                stringValue += ".75";
                break;
        }
        return  stringValue;
    }

    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return false;
        else {
            accessResult = accessTask.accessResult;
            if (readBufferChecked) readBufferChecked = false;
            else if (authenChecked) {
                authenChecked = false; if (accessResult != null) {
                    String strValue = "";
                    for (int i = 0; i < accessResult.length(); i += 32) {
                        int i_end = i + 32; if (i_end >= accessResult.length()) i_end = accessResult.length();
                        if (i != 0) strValue += "\n";
                        strValue += accessResult.substring(i, i_end);
                    }
                    editTextAuthResponse.setText(strValue);
                    if (bImpinJTag) {
                        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.accessUCImpinjLayout);
                        if (authenTam1) layout.setVisibility(View.VISIBLE);
                        else layout.setVisibility(View.GONE);
                    } else processAESdata(accessResult);
                }
            }
            else if (untraceChecked) untraceChecked = false;
            else if (showEpcChecked) { showEpcChecked = false; if (strShowEpcButtonBackup != null) buttonShowEpc.setText(strShowEpcButtonBackup); strShowEpcButtonBackup = null; }
            else if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.AESKEY0ACTIVATE) {
                    textViewAesKey0ActivateOk.setText("E"); checkBoxAesKey0Activate.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1ACTIVATE) {
                    textViewAesKey1ActivateOk.setText("E"); checkBoxAesKey1Activate.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY0) {
                    textViewAesKey0Ok.setText("E"); checkBoxAesKey0.setChecked(false);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1) {
                    textViewAesKey1Ok.setText("E"); checkBoxAesKey1.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.AESKEY0ACTIVATE) {
                    textViewAesKey0ActivateOk.setText("O"); checkBoxAesKey0Activate.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1ACTIVATE) {
                    textViewAesKey1ActivateOk.setText("O"); checkBoxAesKey1Activate.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                } else if (readWriteTypes == ReadWriteTypes.AESKEY0) {
                    textViewAesKey0Ok.setText("O"); checkBoxAesKey0.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAesKey0.setText(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.AESKEY1) {
                    textViewAesKey1Ok.setText("O"); checkBoxAesKey1.setChecked(false); readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) editTextAesKey1.setText(accessResult);
                }
            }
            accessTask = null;
            return true;
        }
    }
    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accSize = 0, accOffset = 0, accBank = 3;
        String writeData = "";

        if (readBufferChecked) {
            accOffset = 0; accSize = 1;
        } else if (authenChecked) {
            if (authenTam1) {
                if (bImpinJTag) {
                    if (MainActivity.csLibrary4A.setTamConfiguration(false, strChallenge) == false)
                        invalidRequest1 = true;
                } else if (MainActivity.csLibrary4A.setTam1Configuration(keyId, strChallenge) == false)
                    invalidRequest1 = true;
            } else if (bImpinJTag) {
                if (MainActivity.csLibrary4A.setTamConfiguration(true, strChallenge) == false)
                    invalidRequest1 = true;
            } else if (MainActivity.csLibrary4A.setTam2Configuration(keyId, strChallenge, profile, offset, blockId, protMode) == false)
                invalidRequest1 = true;
            if (invalidRequest1 == false) { editTextAuthResponse.setText(""); textViewAuthResponseDecoded.setText(""); textViewAuthResponseDecodedCustom.setText(""); editTextAuthResponseEncodedMac.setText(""); }
            return invalidRequest1;
        } else if (untraceChecked) {
            if (MainActivity.csLibrary4A.setUntraceable(checkBoxHideEpc.isChecked(), checkBoxHideEpc.isChecked() ? 2 : 6, checkBoxHideTid.isChecked() ? 1: 0, checkBoxHideUser.isChecked(), checkBoxHideRange.isChecked()) == false) invalidRequest1 = true;
            return invalidRequest1;
        } else if (showEpcChecked) {
            try {
                if (MainActivity.csLibrary4A.setUntraceable(false, Integer.parseInt(editTextEpcSize.getText().toString()), 0, false, false) == false) invalidRequest1 = true;
            } catch (Exception ex) {
                invalidRequest1 = true;
            }
            return invalidRequest1;
        } else if (checkBoxAesKey0Activate.isChecked() == true) {
            accOffset = 0xC8; accSize = 1; readWriteTypes = ReadWriteTypes.AESKEY0ACTIVATE; textViewAesKey0ActivateOk.setText("");
            if (operationRead == false) writeData = "E200";
        } else if (checkBoxAesKey1Activate.isChecked() == true) {
            accOffset = 0xD8; accSize = 1; readWriteTypes = ReadWriteTypes.AESKEY1ACTIVATE; textViewAesKey0ActivateOk.setText("");
            if (operationRead == false) writeData = "E200";
        } else if (checkBoxAesKey0.isChecked() == true) {
            accOffset = 0xC0; if (iTagType == 5) { accOffset = 0x10; accBank = 0; }
            accSize = 8; readWriteTypes = ReadWriteTypes.AESKEY0; textViewAesKey0Ok.setText("");
            if (operationRead) editTextAesKey0.setText("");
            else writeData = editTextAesKey0.getText().toString();
        } else if (checkBoxAesKey1.isChecked() == true) {
            accOffset = 0xD0; if (iTagType == 5) { accOffset = 0x18; accBank = 0; }
            accSize = 8; readWriteTypes = ReadWriteTypes.AESKEY1; textViewAesKey1Ok.setText("");
            if (operationRead) editTextAesKey1.setText("");
            else writeData = editTextAesKey1.getText().toString();
        } else {
            invalidRequest1 = true;
        }

        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (MainActivity.csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
        }
        if (invalidRequest1 == false && operationRead == false) {
            if (invalidRequest1 == false) {
                if (MainActivity.csLibrary4A.setAccessWriteData(writeData) == false) {
                    invalidRequest1 = true;
                }
            }
        }
        return invalidRequest1;
    }
}
