package com.csl.cs108ademoapp.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108ademoapp.AccessTask1;
import com.csl.cs108ademoapp.CustomPopupWindow;
import com.csl.cs108ademoapp.GenericTextWatcher;
import com.csl.cs108ademoapp.SelectTag;
import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.Cs108Library4A;
import com.csl.cs108library4a.ReaderDevice;

import static com.csl.cs108ademoapp.MainActivity.mContext;
import static com.csl.cs108ademoapp.MainActivity.tagSelected;

public class AccessReadWriteFragment extends CommonFragment {
    SelectTag selectTag;
    Spinner spinnerSelectBank, spinnerRWSelectEpc1;
    EditText editTextRWSelectOffset, editTextAccessRWAccPassword, editTextAccessRWKillPwd, editTextAccessRWAccPwd, editTextAccPc, editTextAccessRWEpc, editTExtAccessRWXpc;
    EditText editTextTidValue, editTextUserValue, editTextEpcValue, editTextaccessRWAntennaPower;
    TextView textViewEpcLength, textViewRunTime;
    private Button buttonRead;
    private Button buttonWrite;
    Handler mHandler = new Handler();
    String strPCValueRef = "";

    String accEpcValue = ""; String accXpcValue = ""; String accTidValue = ""; String accUserValue = "";
    enum ReadWriteTypes {
        NULL, RESERVE, PC, EPC, XPC, TID, USER, EPC1
    }
    boolean operationRead = false;
    ReadWriteTypes readWriteTypes;

    private AccessTask1 accessTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_access_readwrite, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        androidx.appcompat.app.ActionBar actionBar;
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_access);
        actionBar.setTitle(R.string.title_activity_readwrite);

        selectTag = new SelectTag((Activity)getActivity ());

        spinnerSelectBank = (Spinner) getActivity().findViewById(R.id.selectMemoryBank);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectBank.setAdapter(targetAdapter);
        spinnerSelectBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //if EPC
                        if (tagSelected != null) selectTag.editTextTagID.setText(tagSelected.getAddress());
                        editTextRWSelectOffset.setText("32");
                        break;
                    case 1:
                        if (tagSelected != null) { if (tagSelected.getTid() != null) selectTag.editTextTagID.setText(tagSelected.getTid()); }
                        editTextRWSelectOffset.setText("0");
                        break;
                    case 2:
                        if (tagSelected != null) { if (tagSelected.getUser() != null) selectTag.editTextTagID.setText(tagSelected.getUser()); }
                        editTextRWSelectOffset.setText("0");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editTextRWSelectOffset = (EditText) getActivity().findViewById(R.id.selectMemoryOffset);
        spinnerRWSelectEpc1 = (Spinner) getActivity().findViewById(R.id.accessRWEpc1Title1);
        ArrayAdapter<CharSequence> targetAdapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRWSelectEpc1.setAdapter(targetAdapter1); spinnerRWSelectEpc1.setSelection(1);

        editTextAccessRWAccPassword = (EditText) getActivity().findViewById(R.id.selectPasswordValue);
        editTextAccessRWAccPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPassword, 8));
        editTextAccessRWAccPassword.setText("00000000");
        editTextAccessRWKillPwd = (EditText) getActivity().findViewById(R.id.accessRWKillPwdValue);
        editTextAccessRWKillPwd.addTextChangedListener(new GenericTextWatcher(editTextAccessRWKillPwd, 8));
        editTextAccessRWAccPwd = (EditText) getActivity().findViewById(R.id.accessRWAccPwdValue);
        editTextAccessRWAccPwd.addTextChangedListener(new GenericTextWatcher(editTextAccessRWAccPwd, 8));
        editTextAccPc = (EditText) getActivity().findViewById(R.id.accessRWAccPcValue);
        editTextAccPc.setHint("PC value");
        editTextAccPc.addTextChangedListener(new GenericTextWatcher(editTextAccPc, 4));
        editTextAccessRWEpc = (EditText) getActivity().findViewById(R.id.accessRWAccEpcValue);
        editTExtAccessRWXpc = (EditText) getActivity().findViewById(R.id.accessRWAccXpcValue);
        editTextTidValue = (EditText) getActivity().findViewById(R.id.accessRWTidValue);
        editTextTidValue.setHint("Data Pattern");
        editTextUserValue = (EditText) getActivity().findViewById(R.id.accessRWUserValue); editTextUserValue.setTypeface(Typeface.MONOSPACE);
        editTextUserValue.setHint("Data Pattern");
        editTextEpcValue = (EditText) getActivity().findViewById(R.id.accessRWEpcValue);
        editTextEpcValue.setHint("Data Pattern");

        editTextaccessRWAntennaPower = (EditText) getActivity().findViewById(R.id.selectAntennaPower);
        editTextaccessRWAntennaPower.setText(String.valueOf(300));

        textViewEpcLength = (TextView) getActivity().findViewById(R.id.accessRWAccEpcLength);
        textViewRunTime = (TextView) getActivity().findViewById(R.id.accessRWRunTime);

        buttonRead = (Button) getActivity().findViewById(R.id.accessRWReadButton);
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

        buttonWrite = (Button) getActivity().findViewById(R.id.accessRWWriteButton);
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

        ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                String strEpcValue = tagSelected.getAddress();
                String detail = tagSelected.getDetails();
                String header = "PC=";
                if (detail != null && detail.indexOf(header) >= 0) {
                    int index = detail.indexOf(header) + header.length();
                    strPCValueRef = detail.substring(index, index + 4);
                    updatePCEpc(strPCValueRef, strEpcValue);
                }
            }
        }
        MainActivity.csLibrary4A.setSameCheck(true);
    }

    @Override
    public void onDestroy() {
        if (accessTask != null) accessTask.cancel(true);
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    public AccessReadWriteFragment() {
        super("AccessReadWriteFragment");
    }

    int getPC2EpcWordCount(String detail) {
        String detail2 = detail.substring(0, 1);
        int number2 = Integer.valueOf(detail2, 16) * 2;
        String detail3 = detail.substring(1, 2);
        int number3 = Integer.valueOf(detail3, 16);
        if ((number3 / 8) != 0) number2 += 1;
        return number2;
    }
    void updatePCEpc(String strPCValue, String strEpcValue) {
        boolean needPopup = false;
        if (strPCValue == null) strPCValue = "";
        if (strPCValue.length() != 0) editTextAccPc.setText(strPCValue);
        else strPCValue = strPCValueRef;
        if (strPCValueRef != null && strPCValue != null) {
            if (strPCValue.matches(strPCValueRef) == false && strPCValue.length() == 4) {
                needPopup = true;
                strPCValueRef = strPCValue;
            }
        }

        int iWordCount = getPC2EpcWordCount(strPCValue);
        textViewEpcLength.setText("EPC has " + (iWordCount * 16) + " bits");
        if (strEpcValue != null) {
            tagSelected.setAddress(strEpcValue); if (spinnerSelectBank.getSelectedItemPosition() == 0) selectTag.editTextTagID.setText(strEpcValue);
            editTextAccessRWEpc.setText(strEpcValue);
        } else {
            if (iWordCount * 4 < selectTag.editTextTagID.getText().toString().length()) {
                // needPopup = true;
                String strTemp = selectTag.editTextTagID.getText().toString().substring(0, iWordCount * 4);
                tagSelected.setAddress(strEpcValue); if (spinnerSelectBank.getSelectedItemPosition() == 0) selectTag.editTextTagID.setText(strTemp);
            }
            if (iWordCount * 4 < editTextAccessRWEpc.getText().toString().length()) {
                // needPopup = true;
                String strTemp = editTextAccessRWEpc.getText().toString().substring(0, iWordCount * 4);
                editTextAccessRWEpc.setText(strTemp);
            }
            if (editTextAccessRWEpc.getText().toString().length() != 0) {
                String strTemp = editTextAccessRWEpc.getText().toString();
                if (selectTag.editTextTagID.getText().toString().matches(strTemp) == false) {
                    // needPopup = true;
                    tagSelected.setAddress(strEpcValue); if (spinnerSelectBank.getSelectedItemPosition() == 0) selectTag.editTextTagID.setText(strTemp);
                }
            }
        }
        editTextAccessRWEpc.addTextChangedListener(new GenericTextWatcher(editTextAccessRWEpc, iWordCount * 4));
        String strTemp = editTextAccessRWEpc.getText().toString();
        editTextAccessRWEpc.setText(strTemp);

        if (needPopup) {
            CustomPopupWindow customPopupWindow = new CustomPopupWindow(mContext);
            customPopupWindow.popupStart("Changing EPC Length will automatically modify to " + (iWordCount * 16) + " bits.", false);
        }
    }

    long msStartTime;
    void startAccessTask() {
        if (selectTag.editTextTagID.getText().toString().length() == 0) {
            Toast.makeText(MainActivity.mContext, "Please select tag first !!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (updating == false) {
            msStartTime = SystemClock.elapsedRealtime();
            textViewRunTime.setText("");
            updating = true; bankProcessing = 0; restartAccessBank = -1;
//            MainActivity.mCs108Library4a.
            mHandler.removeCallbacks(updateRunnable);
            mHandler.post(updateRunnable);
        }
    }
    boolean updating = false; int bankProcessing = 0;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = true; boolean taskRequest = false;
            if (processResult()) { rerunRequest = true; }
            else if (accessTask == null || accessTask.isResultReady()) {
                bcheckBoxAll = false;
                boolean invalid = processTickItems();
                if (bankProcessing == 0 && bcheckBoxAll) rerunRequest = false;
                else if (bankProcessing++ != 0 && invalid == true)   rerunRequest = false;
                else {
                    if (restartAccessBank != accessBank) {
                        restartAccessBank = accessBank;
                        restartCounter = 3;
                    }
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessReadWriteFragment().InventoryRfidTask(): tagID=" + selectTag.editTextTagID.getText() + ", operationrRead=" + operationRead + ", accessBank=" + accessBank + ", accOffset=" + accOffset + ", accSize=" + accSize);
                    int selectOffset = 0;
                    selectOffset = Integer.parseInt(editTextRWSelectOffset.getText().toString());
                    EditText editTextBlockCount = (EditText) getActivity().findViewById(R.id.accessRWBlockCount);
                    Integer accBlockCount = 32;
                    try {
                        accBlockCount = Integer.parseInt(editTextBlockCount.getText().toString());
                    } catch (Exception ex) { }
                    MainActivity.csLibrary4A.appendToLog("strOut: accWriteData=" + accWriteData);
                    accessTask = new AccessTask1(
                            (operationRead ? buttonRead : buttonWrite), invalid,
                            accessBank, accOffset, accSize, accBlockCount, accWriteData,
                            selectTag.editTextTagID.getText().toString(), spinnerSelectBank.getSelectedItemPosition() + 1, selectOffset,
                            editTextAccessRWAccPassword.getText().toString(),
                            Integer.valueOf(editTextaccessRWAntennaPower.getText().toString()),
                            (operationRead ? Cs108Library4A.HostCommands.CMD_18K6CREAD: Cs108Library4A.HostCommands.CMD_18K6CWRITE), updateRunnable);
                    accessTask.execute();
                    rerunRequest = true;
                }
            }
            if (rerunRequest) {
                mHandler.postDelayed(updateRunnable, 100);
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessReadWriteFragment().updateRunnable(): Restart");
            }
            else {
                if (bankProcessing == 0 && bcheckBoxAll) {
                    Toast.makeText(MainActivity.mContext, "no choice selected yet", Toast.LENGTH_SHORT).show();
                }
                updating = false;
            }
        }
    };

    TextView textViewReserveOk, textViewPcOk, textViewEpcOk, textViewTidOk, textViewUserOk, textViewEpc1Ok;
    CheckBox checkBoxReserve, checkBoxPc, checkBoxEpc, checkBoxTid, checkBoxUser, checkBoxEpc1;
    int accessBank, accSize, accOffset; String accWriteData;
    int restartCounter = 0; int restartAccessBank = -1;
    boolean processResult() {
        String accessResult = null;
        if (accessTask == null) return false;
        if (accessTask.isResultReady() == false) return false;
        else {
            long duration = SystemClock.elapsedRealtime() - msStartTime;
            textViewRunTime.setText(String.format("Run time: %.2f sec", ((float) duration / 1000)));
            accessResult = accessTask.getResult();
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("processResult(): accessResult = " + accessResult);
            if (accessResult == null) {
                if (readWriteTypes == ReadWriteTypes.RESERVE) {
                    textViewReserveOk.setText("E"); checkBoxReserve.setChecked(false);
                }
                if (readWriteTypes == ReadWriteTypes.PC) {
                    textViewPcOk.setText("E"); checkBoxPc.setChecked(false);
                }
                if (readWriteTypes == ReadWriteTypes.EPC) {
                    textViewEpcOk.setText("E"); checkBoxEpc.setChecked(false);
                }
                if (readWriteTypes == ReadWriteTypes.TID) {
                    textViewTidOk.setText("E"); checkBoxTid.setChecked(false);
                }
                if (readWriteTypes == ReadWriteTypes.USER) {
                    textViewUserOk.setText("E"); checkBoxUser.setChecked(false);
                }
                if (readWriteTypes == ReadWriteTypes.EPC1) {
                    textViewEpc1Ok.setText("E"); checkBoxEpc1.setChecked(false);
                }
            } else {
                if (DEBUG) MainActivity.csLibrary4A.appendToLog("accessResult = " + accessResult);
                if (readWriteTypes == ReadWriteTypes.RESERVE) {
                    textViewReserveOk.setText("O"); checkBoxReserve.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() == 0 || operationRead == false) {
                    } else if (accessResult.length() < 8) {
                        editTextAccessRWKillPwd.setText(accessResult);
                    } else {
                        editTextAccessRWKillPwd.setText(accessResult.substring(0, 8));
                    }
                    if (accessResult.length() <= 8) {
                        editTextAccessRWAccPwd.setText("");
                    } else if (accessResult.length() < 16) {
                        editTextAccessRWAccPwd.setText(accessResult.subSequence(8, accessResult.length()));
                    } else {
                        editTextAccessRWAccPwd.setText(accessResult.subSequence(8, 16));
                    }
                } else if (readWriteTypes == ReadWriteTypes.PC) {
                    textViewPcOk.setText("O"); checkBoxPc.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        String newValue = "";
                        if (accessResult.length() <= 4) {
                            newValue = accessResult.subSequence(0, accessResult.length()).toString();
                        } else {
                            newValue = accessResult.subSequence(0, 4).toString();
                        }
                        editTextAccPc.setText(newValue);
                    }
                    updatePCEpc(editTextAccPc.getText().toString(), null);
                } else if (readWriteTypes == ReadWriteTypes.EPC) {
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessReadWrite(). EPC DATA with accessBank = " + accessBank + ", with accessResult.length = " + accessResult.length());
                    textViewEpcOk.setText("O"); checkBoxEpc.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        String newValue = "";
                        if (accessResult.length() <= 4) {
                            newValue = accessResult.subSequence(0, accessResult.length()).toString();
                        } else {
                            newValue = accessResult.subSequence(0, 4).toString();
                        }
                        editTextAccPc.setText(newValue);
                    }
                    updatePCEpc(editTextAccPc.getText().toString(), null);

                    if (operationRead) {
                        String newValue = "";
                        if (accessResult.length() > 4) {
                            newValue = accessResult.subSequence(4, accessResult.length()).toString();
                        }
                        editTextAccessRWEpc.setText(newValue);
                    }
                } else if (readWriteTypes == ReadWriteTypes.XPC) {
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessReadWrite(). XPC DATA with accessBank = " + accessBank + ", with accessResult.length = " + accessResult.length() + ", with accessResult=" + accessResult);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        String newValue = accessResult.toString();
                        editTExtAccessRWXpc.setText(newValue);
                        accXpcValue = newValue;
                    } else {
                        accXpcValue = editTExtAccessRWXpc.getText().toString();
                    }
                } else if (readWriteTypes == ReadWriteTypes.TID) {
                    textViewTidOk.setText("O"); checkBoxTid.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() == 0 || operationRead == false) {
                    } else editTextTidValue.setText(accessResult);
                } else if (readWriteTypes == ReadWriteTypes.USER) {
                    textViewUserOk.setText("O"); checkBoxUser.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (operationRead) {
                        if (DEBUG) MainActivity.csLibrary4A.appendToLog("AccessReadWrite(). DATA with accessBank = " + accessBank);
                        if (accessTask != null) {
                            int iLength = accessTask.deformatWriteAccessData(accessResult).length();
                            MainActivity.csLibrary4A.appendToLog("endingMessage length=" + iLength);
                            if (iLength < accSize*4) textViewUserOk.setText("H");
                        }
                        editTextUserValue.setText(accessResult);
                        accUserValue = accessResult;
                    } else {
                        accUserValue = editTextUserValue.getText().toString();
                    }
                } else if (readWriteTypes == ReadWriteTypes.EPC1) {
                    textViewEpc1Ok.setText("O"); checkBoxEpc1.setChecked(false);
                    readWriteTypes = ReadWriteTypes.NULL;
                    if (accessResult.length() == 0 || operationRead == false) {
                    } else {
                        editTextEpcValue.setText(accessResult);
                        accEpcValue = accessResult;
                    }
                    if (operationRead == false) accEpcValue = editTextEpcValue.getText().toString();
                }
                accessResult = null;
            }
            accessTask = null;
            return true;
        }
    }
    boolean bcheckBoxAll = false;
    boolean processTickItems() {
        String writeData = "";
        boolean invalidRequest1 = false;

        textViewReserveOk = (TextView) getActivity().findViewById(R.id.accessRWReserveOK);
        textViewPcOk = (TextView) getActivity().findViewById(R.id.accessRWPcOK);
        textViewEpcOk = (TextView) getActivity().findViewById(R.id.accessRWEpcOK);
        textViewTidOk = (TextView) getActivity().findViewById(R.id.accessRWTidOK);
        textViewUserOk = (TextView) getActivity().findViewById(R.id.accessRWUserOK);
        textViewEpc1Ok = (TextView) getActivity().findViewById(R.id.accessRWEpc1OK);

        checkBoxReserve = (CheckBox) getActivity().findViewById(R.id.accessRWReserveTitle);
        checkBoxPc = (CheckBox) getActivity().findViewById(R.id.accessRWPcTitle);
        checkBoxEpc = (CheckBox) getActivity().findViewById(R.id.accessRWEpcTitle);
        checkBoxTid = (CheckBox) getActivity().findViewById(R.id.accessRWTidTitle);
        checkBoxUser = (CheckBox) getActivity().findViewById(R.id.accessRWUserTitle);
        checkBoxEpc1 = (CheckBox) getActivity().findViewById(R.id.accessRWEpc1);

        if (checkBoxReserve.isChecked() == true) {
            textViewReserveOk.setText("");
            accessBank = 0; accOffset = 0; accSize = 4; readWriteTypes = ReadWriteTypes.RESERVE;
            if (operationRead) {
                editTextAccessRWKillPwd.setText("");
                editTextAccessRWAccPwd.setText("");
            } else {
                String strValue = editTextAccessRWKillPwd.getText().toString();
                String strValue1 = editTextAccessRWAccPwd.getText().toString();
                if (strValue.length() != 8 || strValue1.length() != 8) {
                    invalidRequest1 = true;
                } else {
                    writeData = strValue + strValue1;
                }
            }
        } else if (checkBoxPc.isChecked() == true || ((checkBoxEpc.isChecked() == true) && (strPCValueRef.length() != 4) )) {
            textViewPcOk.setText("");
            accessBank = 1; accOffset = 1; accSize = 1; readWriteTypes = ReadWriteTypes.PC;
            if (operationRead) {
                editTextAccPc.setText("");
            } else {
                String strValue = editTextAccPc.getText().toString();
                if (strValue.length() != 4) invalidRequest1 = true;
                else writeData = strValue;
            }
        } else if (checkBoxEpc.isChecked() == true) {
            textViewEpcOk.setText("");
            accessBank = 1; accOffset = 1; accSize = 0; readWriteTypes = ReadWriteTypes.EPC;
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("processTickItems(): start EPC operation");
            if (operationRead) {
                if (strPCValueRef.length() != 4) accSize = 1;
                else {
                    accSize = getPC2EpcWordCount(strPCValueRef) + 1;
                    editTextAccessRWEpc.setText("");
                }
            } else {
                String strValue = editTextAccPc.getText().toString();
                String strValue1 = editTextAccessRWEpc.getText().toString();
                if (strValue1.length() == 0) {
                    if (strValue.length() != 4) invalidRequest1 = true;
                    else {
                        accSize = 1;
                        writeData = strValue;
                    }
                } else {
                    accSize += strValue1.length() / 4;
                    if (strValue1.length() % 4 != 0) accSize++;
                    if (strValue.length() == 4) {
                        int iPCWordCount = getPC2EpcWordCount(strValue);
                        if (iPCWordCount < accSize) accSize = iPCWordCount;
                        accSize++;
                        writeData = strValue + strValue1;
                    } else {
                        accOffset = 2;
                        writeData = strValue1;
                    }
                }
            }
        } else if (checkBoxTid.isChecked() == true) {
            textViewTidOk.setText("");
            accessBank = 2; accOffset = 0; accSize = 0; readWriteTypes = ReadWriteTypes.TID;
            EditText editTextTidValue = (EditText) getActivity().findViewById(R.id.accessRWTidValue);
            if (operationRead) {
                int iValue = 0;
                try {
                    EditText editTextTidOffset = (EditText) getActivity().findViewById(R.id.accessRWTidOffset);
                    iValue = Integer.parseInt(editTextTidOffset.getText().toString());
                } catch (Exception ex) {
                }
                accOffset = iValue;
                iValue = 0;
                try {
                    EditText editTextTidLength = (EditText) getActivity().findViewById(R.id.accessRWTidLength);
                    iValue = Integer.parseInt(editTextTidLength.getText().toString());
                } catch (Exception ex) {
                }
                accSize = iValue;
                editTextTidValue.setText("");
            } else {
                invalidRequest1 = true;
                editTextTidValue.setText("");

            }
        } else if (checkBoxUser.isChecked() == true) {
            textViewUserOk.setText("");
            accessBank = 3; accOffset = 0; accSize = 0; readWriteTypes = ReadWriteTypes.USER;
            if (DEBUG) MainActivity.csLibrary4A.appendToLog("processTickItems(): start USER operation");
            int iValue = 0;
            try {
                EditText editTextTidOffset = (EditText) getActivity().findViewById(R.id.accessRWUserOffset);
                iValue = Integer.parseInt(editTextTidOffset.getText().toString());
            } catch (Exception ex) {
            }
            accOffset = iValue;
            iValue = 0;
            try {
                EditText editTextUserLength = (EditText) getActivity().findViewById(R.id.accessRWUserLength);
                iValue = Integer.parseInt(editTextUserLength.getText().toString());
            } catch (Exception ex) {
            }
            accSize = iValue;
            if (operationRead) {
                editTextUserValue.setText("");
            } else {
                String strValue = editTextUserValue.getText().toString();
                MainActivity.csLibrary4A.appendToLog("strOut: strValue=" + strValue + ", strValue.length=" + strValue.length() + ", accUserValue=" + accUserValue + ", accUserValue.lengt=" + accUserValue.length());
                if (strValue.length() >= 4 && accUserValue.matches(strValue) == false) {
                    writeData = strValue;
                }
            }
        } else if (checkBoxEpc1.isChecked() == true) {
            textViewEpc1Ok.setText("");
            accessBank = spinnerRWSelectEpc1.getSelectedItemPosition(); accOffset = 0; accSize = 0; readWriteTypes = ReadWriteTypes.EPC1;
            int iValue = 0;
            try {
                EditText editTextEpcOffset = (EditText) getActivity().findViewById(R.id.accessRWEpcOffset);
                iValue = Integer.parseInt(editTextEpcOffset.getText().toString());
            } catch (Exception ex) {
            }
            accOffset = iValue;
            iValue = 0;
            try {
                EditText editTextEpcLength = (EditText) getActivity().findViewById(R.id.accessRWEpcLength);
                iValue = Integer.parseInt(editTextEpcLength.getText().toString());
            } catch (Exception ex) {
            }
            accSize = iValue;
            if (operationRead) {
                editTextEpcValue.setText("");
            } else {
                String strValue = editTextEpcValue.getText().toString();
                if (strValue.length() >= 4 && strValue.matches(accEpcValue) == false) {
                    accSize = strValue.length() / 4;
                    if (strValue.length() %4 != 0)  accSize++;
                    writeData = strValue;
                }
            }
        } else {
            invalidRequest1 = true;
            bcheckBoxAll = true;
        }

        if (restartAccessBank == accessBank) {
            if (restartCounter == 0) invalidRequest1 = true;
            else restartCounter--;
        }
        accWriteData = writeData;
        return invalidRequest1;
    }
}
