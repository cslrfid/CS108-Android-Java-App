package com.csl.cs108ademoapp;

import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs108library4a.Cs108Connector;

import java.util.ArrayList;

public class AccessTask extends AsyncTask<Void, String, String> {
    final boolean DEBUG = true;
    final boolean skipSelect = false;
    public enum TaskCancelRReason {
        NULL, INVALD_REQUEST, DESTORY, STOP, BUTTON_RELEASE, TIMEOUT
    }
    public TaskCancelRReason taskCancelReason;
    public String accessResult;

    Button button; String buttonText;
    TextView registerRunTime, registerTagGot, registerVoltageLevel;
    TextView registerYield, registerTotal;
    boolean invalidRequest;
    String selectMask; int selectBank, selectOffset;
    String strPassword; int powerLevel;
    Cs108Connector.HostCommands hostCommand;

    CustomMediaPlayer playerO, playerN;

    long timeMillis, startTimeMillis, runTimeMillis;
    int accessError, backscatterError;
    boolean timeoutError, crcError;
    String resultError = "";
    boolean success;
    boolean done = false;
    boolean ending = false;
    private String endingMessaage;

    int qValue=0;
    boolean repeat=false, nextNew=false;
    boolean bEnableErrorPopWindow=true;

    boolean gotInventory;
    int batteryCountInventory_old;

    public AccessTask(Button button, boolean invalidRequest,
                      String selectMask, int selectBank, int selectOffset,
                      String strPassword, int powerLevel, Cs108Connector.HostCommands hostCommand, boolean bEnableErrorPopWindow) {
        this.button = button;
        this.registerTagGot = registerTagGot;
        this.registerVoltageLevel = registerVoltageLevel;

        this.invalidRequest = invalidRequest; MainActivity.mCs108Library4a.appendToLog("invalidRequest = " + invalidRequest);
        this.selectMask = selectMask;
        this.selectBank = selectBank;
        this.selectOffset = selectOffset;
        this.strPassword = strPassword;
        this.powerLevel = powerLevel;
        this.hostCommand = hostCommand;
        this.bEnableErrorPopWindow = bEnableErrorPopWindow;
        if (true) {
            total = 0;
            tagList.clear();
        }
        preExecute();
    }
    public AccessTask(Button button, TextView textViewWriteCount, boolean invalidRequest,
                      String selectMask, int selectBank, int selectOffset,
                      String strPassword, int powerLevel, Cs108Connector.HostCommands hostCommand,
                      int qValue, boolean repeat, boolean nextNew, boolean resetCount,
                      TextView registerRunTime, TextView registerTagGot, TextView registerVoltageLevel,
                      TextView registerYieldView, TextView registerTotalView) {
        this.button = button;
        this.registerTotal = textViewWriteCount;
        this.registerRunTime = registerRunTime;
        this.registerTagGot = registerTagGot;
        this.registerVoltageLevel = registerVoltageLevel;
        this.registerYield = registerYieldView;
        this.registerTotal = registerTotalView;

        this.invalidRequest = invalidRequest; MainActivity.mCs108Library4a.appendToLog("invalidRequest = " + invalidRequest);
        this.selectMask = selectMask;
        this.selectBank = selectBank;
        this.selectOffset = selectOffset;
        this.strPassword = strPassword;
        this.powerLevel = powerLevel;
        this.hostCommand = hostCommand;
        this.qValue = qValue;
        this.repeat = repeat;
        this.nextNew = nextNew;
        if (resetCount) {
            total = 0;
            tagList.clear();
        }
        preExecute();
    }

    void preExecute() {
        accessResult = null;
        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;
        //playerN.start();

        buttonText = button.getText().toString().trim();
        String buttonText1 = null; String strLastChar = buttonText.substring(buttonText.length()-1);
        if (strLastChar.toUpperCase().matches("E")) {
            buttonText1 = buttonText.substring(0, buttonText.length()-1);
            if (strLastChar.matches("E")) buttonText1 += "ING";
            else buttonText1 += "ing";
        }
        if (repeat || buttonText.length() == 0) button.setText("Stop");
        else if (buttonText1 != null) button.setText(buttonText1);
        else if (Character.isUpperCase(strLastChar.charAt(0))) button.setText(buttonText + "ING");
        else button.setText(buttonText + "ing");
        if (registerYield != null && tagList.size()==0) registerYield.setText("");
        if (registerTotal != null && total == 0) registerTotal.setText("");

        timeMillis = System.currentTimeMillis(); startTimeMillis = timeMillis; runTimeMillis = startTimeMillis;
        accessError = 0; backscatterError = 0; timeoutError = false; crcError = false;
        success = false;

        if (invalidRequest == false) {
            if (strPassword.length() != 8) { invalidRequest = true; MainActivity.mCs108Library4a.appendToLog("strPassword.length = " + strPassword.length() + " (not 8)."); }
            else if (hostCommand == Cs108Connector.HostCommands.CMD_18K6CKILL) {
                if (MainActivity.mCs108Library4a.setRx000KillPassword(strPassword) == false) {
                    invalidRequest = true; MainActivity.mCs108Library4a.appendToLog("setRx000KillPassword is failed");
                }
            } else if (MainActivity.mCs108Library4a.setRx000AccessPassword(strPassword) == false) {
                invalidRequest = true;
                MainActivity.mCs108Library4a.appendToLog("setRx000AccessPassword is failed");
            }
        }
        if (invalidRequest == false) {
            if (MainActivity.mCs108Library4a.setAccessRetry(true, 7) == false) {
                invalidRequest = true; MainActivity.mCs108Library4a.appendToLog("setAccessRetry is failed");
            }
        }
        if (invalidRequest == false) {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AccessTask(): powerLevel = " + powerLevel);
            int matchRep = 0;
            if (repeat == false || nextNew) matchRep = 1;
            if (powerLevel < 0 || powerLevel > 330) invalidRequest = true;
            else if (skipSelect) { }
            else if (MainActivity.mCs108Library4a.setSelectedTag(selectMask, selectBank, selectOffset, powerLevel, qValue, matchRep) == false) {
                invalidRequest = true; MainActivity.mCs108Library4a.appendToLog("setSelectedTag is failed with selectMask = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", powerLevel = " + powerLevel);
            }
        }
        gotInventory = false;
        taskCancelReason = TaskCancelRReason.NULL;
        if (invalidRequest) {
            cancel(true);
            taskCancelReason = TaskCancelRReason.INVALD_REQUEST;
            MainActivity.mCs108Library4a.appendToLog("invalidRequest A= " + invalidRequest);
        } else {
            if (MainActivity.mCs108Library4a.checkHostProcessorVersion(MainActivity.mCs108Library4a.getMacVer(), 2, 6, 8)) {
                MainActivity.mCs108Library4a.setInvModeCompact(false);
            }
            MainActivity.mCs108Library4a.sendHostRegRequestHST_CMD(hostCommand);
        }
    }

    @Override
    protected String doInBackground(Void... a) {
        boolean ending = false;

        while (MainActivity.mCs108Library4a.isBleConnected() && isCancelled() == false && ending == false) {
            int batteryCount = MainActivity.mCs108Library4a.getBatteryCount();
            if (batteryCountInventory_old != batteryCount) {
                batteryCountInventory_old = batteryCount;
                publishProgress("VV");
            }
            if (System.currentTimeMillis() > runTimeMillis + 1000) {
                runTimeMillis = System.currentTimeMillis();
                publishProgress("WW");
            }
            Cs108Connector.Rx000pkgData rx000pkgData = MainActivity.mCs108Library4a.onRFIDEvent();
            if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0)   timeMillis = System.currentTimeMillis();
            else if (rx000pkgData != null) {
                if (rx000pkgData.responseType == null) {
                    publishProgress("null response");
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS) {
                    if (true) {
                        if (rx000pkgData.decodedError == null) {
                            if (done == false) { accessResult = rx000pkgData.decodedResult; MainActivity.mCs108Library4a.appendToLog("HelloA, accResult=" + accessResult);}
                            done = true;
                            publishProgress(null, rx000pkgData.decodedResult);
                        }
                        else publishProgress(rx000pkgData.decodedError);
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_COMMAND_END) {
                    if (rx000pkgData.decodedError != null) { endingMessaage = rx000pkgData.decodedError; ending = true; }
                    else if (repeat && (nextNew == false || resultError.length() != 0)) {
                        resultError = "";
                        MainActivity.mCs108Library4a.sendHostRegRequestHST_CMD(hostCommand);
                    } else {
                        endingMessaage = "";
                        ending = true;
                    }
                } else if (rx000pkgData.responseType == Cs108Connector.HostCmdResponseTypes.TYPE_18K6C_INVENTORY) {
                    publishProgress("TT", MainActivity.mCs108Library4a.byteArrayToString(rx000pkgData.decodedEpc));
                } else {
                    publishProgress("Unhandled Response: " + rx000pkgData.responseType.toString());
                }
                timeMillis = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - timeMillis > 5000) {
                cancel(true);
                taskCancelReason = TaskCancelRReason.TIMEOUT;
            }
            if (taskCancelReason != TaskCancelRReason.NULL) cancel(true);
        }
        return "End of Asynctask():" + ending;
    }

    static int total = 0;
    static ArrayList<String> tagList = new ArrayList<String>();
    String tagInventoried = null;
    @Override
    protected void onProgressUpdate(String... output) {
        if (true) progressUpdate(output);
        else if (output[0] != null) {
            if (output[0].length() == 2) {
                if (output[0].contains("TT")) {
                    gotInventory = true;
                    boolean matched = false;
                    for (int i = 0; i < tagList.size(); i++) {
                        if (output[1].matches(tagList.get(i))) {
                            matched = true;
                            break;
                        }
                    }
                    if (registerTagGot != null) registerTagGot.setText(output[1]);
                    if (matched == false) tagInventoried = output[1];
                } else if (output[0].contains("WW")) {
                    long timePeriod = (System.currentTimeMillis() - startTimeMillis) / 1000;
                    if (timePeriod > 0) {
                        if (registerRunTime != null) registerRunTime.setText(String.format("Run time: %d sec", timePeriod));
                    }
                } else if (taskCancelReason == TaskCancelRReason.NULL) {
                    if (registerVoltageLevel != null) registerVoltageLevel.setText(MainActivity.mCs108Library4a.getBatteryDisplay(true));
                }
            } else {
                resultError += output[0];
                if (true)
                    MainActivity.mCs108Library4a.appendToLog("output[0]: " + output[0] + ", resultError = " + resultError);
            }
        } else {
            if (registerYield != null) {
                if (tagInventoried != null) {
                    tagList.add(tagInventoried);
                    tagInventoried = null;
                }
                registerYield.setText("Unique:" + Integer.toString(tagList.size()));
            }
            if (registerTotal != null) registerTotal.setText("Total:" + Integer.toString(++total));
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AccessSecurityLockFragment.InventoryRfidTask.onCancelled()");

        MainActivity.mCs108Library4a.abortOperation();
        if (taskCancelReason == TaskCancelRReason.NULL)  taskCancelReason = TaskCancelRReason.DESTORY;
        DeviceConnectTask4RegisterEnding();
    }

    @Override
    protected void onPostExecute(String result) {
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("AccessSecurityLockFragment.InventoryRfidTask.onPostExecute(): " + result);

        DeviceConnectTask4RegisterEnding();
    }

    protected void progressUpdate(String... output) {
        if (output[0] != null) {
            if (output[0].length() == 2) {
                if (output[0].contains("TT")) {
                    gotInventory = true;
                    boolean matched = false;
                    for (int i = 0; i < tagList.size(); i++) {
                        if (output[1].matches(tagList.get(i))) {
                            matched = true;
                            break;
                        }
                    }
                    if (registerTagGot != null) registerTagGot.setText(output[1]);
                    if (matched == false) tagInventoried = output[1];
                } else if (output[0].contains("WW")) {
                    long timePeriod = (System.currentTimeMillis() - startTimeMillis) / 1000;
                    if (timePeriod > 0) {
                        if (registerRunTime != null) registerRunTime.setText(String.format("Run time: %d sec", timePeriod));
                    }
                } else if (taskCancelReason == TaskCancelRReason.NULL) {
                    if (registerVoltageLevel != null) registerVoltageLevel.setText(MainActivity.mCs108Library4a.getBatteryDisplay(true));
                }
            } else {
                resultError += output[0];
                if (true)
                    MainActivity.mCs108Library4a.appendToLog("output[0]: " + output[0] + ", resultError = " + resultError);
            }
        } else {
            if (registerYield != null) {
                if (tagInventoried != null) {
                    tagList.add(tagInventoried);
                    tagInventoried = null;
                }
                registerYield.setText("Unique:" + Integer.toString(tagList.size()));
            }
            if (registerTotal != null) registerTotal.setText("Total:" + Integer.toString(++total));
        }
    }

    void DeviceConnectTask4RegisterEnding() {
        String strErrorMessage = "";
        if (false) {
            boolean success = false;
            MainActivity.mCs108Library4a.appendToLog("repeat = " + repeat + ", taskCancelReason = " + taskCancelReason.toString()
                    + ", backscatterError = " + backscatterError + ", accessError =" + accessError + ", accessResult = " + accessResult + ", resultError = " + resultError);
            if ((repeat == false && taskCancelReason != TaskCancelRReason.NULL) || backscatterError != 0 || accessError != 0 || accessResult == null || resultError.length() != 0) {
                MainActivity.mCs108Library4a.appendToLog("FAILURE"); Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_FAILURE, Toast.LENGTH_SHORT).show();
                playerO.start();
            } else {
                MainActivity.mCs108Library4a.appendToLog("SUCCESS"); Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_SUCCESS, Toast.LENGTH_SHORT).show();
                playerN.start();
            }
        } else {
            strErrorMessage = "";
            switch (taskCancelReason) {
                case NULL:
                    if (accessResult == null || (resultError != null && resultError.length() != 0) || (endingMessaage != null && endingMessaage.length() != 0)) strErrorMessage += ("Finish as COMMAND END is received " + (gotInventory ? "WITH" : "WITHOUT") + " tag response");
                    //else Toast.makeText(MainActivity.mContext, R.string.toast_abort_by_SUCCESS, Toast.LENGTH_SHORT).show();
                    break;
                case STOP:
                    strErrorMessage += "Finish as STOP is pressed. ";
                    break;
                case BUTTON_RELEASE:
                    strErrorMessage += "Finish as BUTTON is released. ";
                    break;
                case TIMEOUT:
                    strErrorMessage += "TIMEOUT without COMMAND_END. ";
                    break;
                case INVALD_REQUEST:
                    strErrorMessage += "Invalid request. Operation is cancelled. ";
                    break;
            }
            MainActivity.mCs108Library4a.appendToLog("taskCancelReason = " + taskCancelReason.toString() + ", accessResult = " + (accessResult == null ? "NULL": accessResult) + ", endingMessaage = " + (endingMessaage == null ? "NULL" : endingMessaage) + ", resultError = " + (resultError == null ? "NULL" : resultError));
            if (resultError.length() != 0) strErrorMessage += resultError;
            if (strErrorMessage.length() != 0) strErrorMessage += ". ";
        }
        if (endingMessaage != null) if (endingMessaage.length() != 0) strErrorMessage += "Received CommandEND Error = " + endingMessaage;
        if (strErrorMessage.length() != 0) endingMessaage = strErrorMessage;
        button.setText(buttonText);
        if (endingMessaage != null) {
            if (endingMessaage.length() != 0 || true) {
                MainActivity.mCs108Library4a.appendToLog("endingMessage=" + endingMessaage);
                if (bEnableErrorPopWindow) {
                    CustomPopupWindow customPopupWindow = new CustomPopupWindow(MainActivity.mContext);
                    customPopupWindow.popupStart(endingMessaage, false);
                }
            }
        }
    }
}
