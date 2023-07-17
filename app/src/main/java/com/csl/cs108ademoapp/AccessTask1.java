package com.csl.cs108ademoapp;

import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import com.csl.cs108library4a.Cs108Library4A;

public class AccessTask1 {
    Button button;
    boolean invalidRequest;
    int accBank, accOffset, accSize, accSizeNow, accBlockCount = 90; String accWriteData, accWriteDataNow;
    String selectMask;
    int selectBank, selectOffset;
    String strPassword;
    int powerLevel;
    Cs108Library4A.HostCommands hostCommand;
    Runnable updateRunnable = null;

    AccessTask accessTask;
    public AccessTask1(Button button, boolean invalidRequest,
                       int accBank, int accOffset, int accSize, int accBlockCount, String accWriteData,
                       String selectMask, int selectBank, int selectOffset,
                       String strPassword, int powerLevel, Cs108Library4A.HostCommands hostCommand, Runnable updateRunnable) {
        this.button = button;
        this.invalidRequest = invalidRequest;
        MainActivity.csLibrary4A.appendToLog("HelloK: invalidRequest=" + invalidRequest);
        this.accBank = accBank;
        this.accOffset = accOffset;
        if (hostCommand == Cs108Library4A.HostCommands.CMD_18K6CWRITE) { if (accBlockCount > 16) accBlockCount = 16; }
        else if (accBlockCount > 255) accBlockCount = 255;
        this.accBlockCount = accBlockCount;
        if (accWriteData == null) accWriteData = "";
        if (hostCommand == Cs108Library4A.HostCommands.CMD_18K6CWRITE) {
            MainActivity.csLibrary4A.appendToLog("strOut: accWriteData=" + accWriteData);
            accWriteData = deformatWriteAccessData(accWriteData);
            if (accWriteData.length() < accSize * 4) {
                accSize = accWriteData.length()/4;
                if (accSize*4 != accWriteData.length()) accSize++;
            }
        }
        this.accSize = accSize;
        if (accSize == 0) {
            isResultReady = true;
            accessResult = "";
        }
        this.accWriteData = accWriteData;
        this.selectMask = selectMask;
        this.selectBank = selectBank;
        this.selectOffset = selectOffset;
        this.strPassword = strPassword;
        this.powerLevel = powerLevel;
        this.hostCommand = hostCommand;
        this.updateRunnable = updateRunnable;
        MainActivity.csLibrary4A.appendToLog("HelloA, AccessTask1");
        CustomMediaPlayer playerN = MainActivity.sharedObjects.playerN;
        playerN.start();
        setup();
    }

    public void execute() {
        if (accessTask != null) accessTask.execute();
    }

    public boolean cancel(boolean bCancel) {
        if (accessTask == null) return true;
        return accessTask.cancel(bCancel);
    }

    public AsyncTask.Status getStatus() {
        if (accessTask == null) return AsyncTask.Status.FINISHED;
        return accessTask.getStatus();
    }

    public String deformatWriteAccessData(String strIn) {
        MainActivity.csLibrary4A.appendToLog("strOut: strIn=" + strIn);
        String strOut = strIn.replaceAll("\\P{Print}", "");
        MainActivity.csLibrary4A.appendToLog("strOut=" + strOut);
        while (strOut.indexOf(":") > 0) {
            int index = strOut.indexOf(":");
            String writeDataTemp = "";
            if (index > 4) writeDataTemp = strOut.substring(0, index - 3);
            writeDataTemp += strOut.substring(index + 1);
            strOut = writeDataTemp;
            MainActivity.csLibrary4A.appendToLog("strOut=" + strOut);
        }
        MainActivity.csLibrary4A.appendToLog("strOut=" + strOut);
        return strOut;
    }

    boolean isResultReady = false; int tryCount = 0, tryCountMax = 3;
    public boolean isResultReady() {
        boolean bValue = false;
        if (accessTask == null) { }
        else if (accessTask.getStatus() != AsyncTask.Status.FINISHED) { }
        else if (button.getText().toString().indexOf("ING") > 0) { }
        else if (isResultReady == false) {
            String strAccessResult = "";
            if (hostCommand != Cs108Library4A.HostCommands.CMD_18K6CREAD || accBank != 3) strAccessResult = accessTask.accessResult;
            else {
                int word4line = 7;
                for (int i = 0; i < accSizeNow; i=i+word4line) {
                    if (tryCount < tryCountMax && accessTask.accessResult == null) break;
                    strAccessResult += String.format("%03d:", accOffset + i);
                    if (accessTask.accessResult != null) {
                        if ((i + word4line) * 4 >= accessTask.accessResult.length()) {
                            int iLastWordIndex = accessTask.accessResult.substring(i * 4).length() / 4;
                            if (iLastWordIndex * 4 != accessTask.accessResult.substring(i * 4).length())
                                iLastWordIndex++;
                            strAccessResult += String.format("%03d:", accOffset + i + iLastWordIndex - 1) + accessTask.accessResult.substring(i * 4);
                        } else
                            strAccessResult += accessTask.accessResult.substring(i * 4, (i + word4line) * 4);
                    }
                    strAccessResult += "\n";
                    MainActivity.csLibrary4A.appendToLog("i=" + i + ", formatted accessTask.accessResult=" + strAccessResult);
                    }
               }
            if (accessResult == null) accessResult = strAccessResult;
            else accessResult += strAccessResult;

            MainActivity.csLibrary4A.appendToLog("HelloA: accessResult=" + accessTask.accessResult);
            if (accessTask.accessResult != null && accSizeNow >= accSize) {
                bValue = true;
                Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_SUCCESS, Toast.LENGTH_SHORT).show();
            }
            else {
                if (accessTask.accessResult != null) {
                    accOffset += accSizeNow;
                    accSize -= accSizeNow;
                    if (accWriteData != null) { if (accWriteData.length() >= accSizeNow*4) accWriteData = accWriteData.substring(accSizeNow*4); }
                    tryCount = 0;
                }
                MainActivity.csLibrary4A.appendToLog("HelloA: Going to retry with TryCount=" + tryCount + ", resultError = " + accessTask.resultError);
                if (tryCount < tryCountMax) {
                    MainActivity.csLibrary4A.appendToLog("HelloA: re-setup");
                    setup();
                    execute();
                } else bValue = true;
            }
        } else bValue = true;
        //MainActivity.csLibrary4A.appendToLog("HelloA: bValue=" + bValue);
        isResultReady = bValue;
        return bValue;
    }
    public String accessResult;
    public String getResult() {
        MainActivity.csLibrary4A.appendToLog("HelloA: accessResult=" + accessResult);
        if (accessTask == null) return null;
        if (accessTask.getStatus() != AsyncTask.Status.FINISHED) return null;
        if (button.getText().toString().indexOf("ING") > 0) return null;
        return accessResult;
    }

    void setup() {
        tryCount++;
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessBank(accBank) == false) {
                MainActivity.csLibrary4A.appendToLog("HelloK: accBank, invalidRequest=" + invalidRequest);
                invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.csLibrary4A.setAccessOffset(accOffset) == false) {
                MainActivity.csLibrary4A.appendToLog("HelloK: accOffset, invalidRequest=" + invalidRequest);
                invalidRequest = true;
            }
        }
        if (invalidRequest == false) {
            if (accSize == 0) {
                MainActivity.csLibrary4A.appendToLog("HelloK: accSize0, invalidRequest=" + invalidRequest);
                invalidRequest = true;
            } else {
                if (accSize > accBlockCount) accSizeNow = accBlockCount;
                else accSizeNow = accSize;
                MainActivity.csLibrary4A.appendToLog("HelloA: accSize=" + accSize + ", accSizeNow=" + accSizeNow);
                if (MainActivity.csLibrary4A.setAccessCount(accSizeNow) == false) {
                    invalidRequest = true;
                }
            }
        }
        if (invalidRequest == false && hostCommand == Cs108Library4A.HostCommands.CMD_18K6CWRITE) {
            if (accWriteData.length() > accSizeNow * 4) accWriteDataNow = accWriteData.substring(0, accSizeNow*4);
            else accWriteDataNow = accWriteData;
            if (MainActivity.csLibrary4A.setAccessWriteData(accWriteDataNow) == false) {
                invalidRequest = true;
            }
        }
        MainActivity.csLibrary4A.appendToLog("HelloA: accOffset=" + accOffset + ", accSizeNow=" + accSizeNow + ", accSize=" + accSize);
        MainActivity.csLibrary4A.appendToLog("HelloK: invalidRequest=" + invalidRequest);
        accessTask = new AccessTask(button, invalidRequest,
                selectMask, selectBank, selectOffset,
                strPassword, powerLevel, hostCommand, tryCount==tryCountMax, updateRunnable);
    }
}
