package com.csl.cslibrary4a;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

public class TagBanks {
    Context context;
    CsLibrary4A csLibrary4A;
    CustomMediaPlayer playerN, playerO;
    Button buttonRead, buttonWrite;

    class AccessData {
        int accBank, accOffset, accSize;
        String writeData;
    }

    AccessTaskCustom accessTask;
    Handler handler = new Handler();
    AccessData accessData;
    public SelectData selectData;
    boolean updating = false;
    String[] stringsReserved = new String[2];
    String[] stringsEpc;
    String[] stringsTid;
    String[] stringsUser;

    public TagBanks(Context context, CsLibrary4A csLibrary4A, CustomMediaPlayer playerN, CustomMediaPlayer playerO, Button buttonRead, Button buttonWrite) {
        this.context = context;
        this.csLibrary4A = csLibrary4A;
        this.playerN = playerN;
        this.playerO = playerO;
        this.buttonRead = buttonRead;
        this.buttonWrite = buttonWrite;
    }
    void setBankDataStart(SelectData selectData, int accBank, int accOffset, int accSize, String writeData) {
        this.selectData = selectData;
        accessData = new AccessData(); accessData.accBank = accBank; accessData.accOffset = accOffset; accessData.accSize = accSize; accessData.writeData = writeData;
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable); updateRunning = true;
    }
    CustomAsyncTask.Status getReadWriteStatus() {
        if (updateRunning) return CustomAsyncTask.Status.RUNNING;
        else if (accessTask == null) return null;
        else {
            CustomAsyncTask.Status status = accessTask.getStatus();
            if (status == CustomAsyncTask.Status.FINISHED) accessTask = null;
            return status;
        }
    }
    boolean updateRunning = false;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean rerunRequest = false; boolean taskRequest = false;
            if (accessTask == null) {
                appendToLog("TagBanks.updateRunnable: NULL accessReadWriteTask");
                taskRequest = true;
            } else if (accessTask.getStatus() != CustomAsyncTask.Status.FINISHED) {
                appendToLog("TagBanks.updateRunnable: accessReadWriteTask.getStatus() = " + accessTask.getStatus().toString());
                rerunRequest = true;
            } else {
                appendToLog("TagBanks.updateRunnable: FINISHED accessReadWriteTask");
                //taskRequest = true;
            }

            if (taskRequest) {
                boolean invalid = processTickItems();
                appendToLog("TagBanks.updateRunnable: processTickItems is invalid = " + invalid);

                int selectBank = 1;
                int selectOffset = 32;
                accessTask = csLibrary4A.getAccessTaskCustom((accessData.writeData == null ? buttonRead : buttonWrite), null, invalid, true,
                        selectData.selectMaskEpc, selectBank, selectOffset,
                        selectData.selectPassword, selectData.selectPower, (accessData.writeData == null ? RfidReaderChipData.HostCommands.CMD_18K6CREAD: RfidReaderChipData.HostCommands.CMD_18K6CWRITE),
                        0, 0, true, false,
                        null, null, null, null, null,
                        playerN, playerO);
                accessTask.execute();
                rerunRequest = true;
                appendToLog("TagBanks.updateRunnable: accessTask is created with accessBank = " + accessData.accBank + ", accessOffset = " + accessData.accOffset + ", accessSize = " + accessData.accSize + ", accessData = " + (accessData.writeData == null ? "null" : accessData.writeData));
            } else if (!rerunRequest) {
                processResult();
                //rerunRequest = true;
                appendToLog("TagBanks.updateRunnable: processResult is TRUE");
            }
            if (rerunRequest) {
                handler.postDelayed(updateRunnable, 500); updateRunning = true;
                appendToLog("TagBanks.updateRunnable: Restart");
            } else updateRunning = false;
            appendToLog("TagBanks.updateRunnable: Ending with updateRunning = " + updateRunning);
        }
    };
    void processResult() {
        String accessResult = null;
        /*if (accessTask == null) {
            appendToLog("TagBanks.processResult: accesssTask is NULL");
            return false;
        } else if (accessTask.getStatus() != CustomAsyncTask.Status.FINISHED) {
            appendToLog("TagBanks.processResult: accesssTask is working with status as " + accessTask.getStatus().toString());
            return false;
        } else*/ {
            accessResult = accessTask.accessResult;
            if (accessResult == null) {
                appendToLog("TagBanks.processResult: accessTask is finished with null accessResult with resultError = " + accessTask.resultError);
                if (true) {
                    //textViewLoggingInterval.setText("E");
                    //textViewLoggingInterval.setChecked(false);
                }
            } else {
                appendToLog("TagBanks.processResult: accessTask is finished with accessResult = " + accessResult + ", resultError = " + accessTask.resultError);
                if (true) {
                    //textViewLoggingInterval.setText("O");
                    //textViewLoggingInterval.setChecked(false);
                    //readWriteTypes = ReadWriteTypes.NULL;
                    int iOffset = accessData.accOffset;
                    if (accessData.writeData == null) {
                        switch (accessData.accBank) {
                            case 0:
                                break;
                            case 1:
                                appendToLog("TagBanks.processResult: Old stringsEpc.length = " + (stringsEpc == null ? "null" : stringsEpc.length));
                                if (stringsEpc == null || stringsEpc.length < iOffset) {
                                    String[] stringsNew = new String[iOffset + accessData.accSize];
                                    if (stringsEpc != null) {
                                        for (int i = 0; i < stringsEpc.length; i++) stringsNew[i] = stringsEpc[i];
                                    }
                                    stringsEpc = stringsNew;
                                }
                                appendToLog("TagBanks.processResult: New stringsEpc.length = " + (stringsEpc == null ? "null" : stringsEpc.length));
                                break;
                            case 2:
                                appendToLog("TagBanks.processResult: Old stringsTid.length = " + (stringsTid == null ? "null" : stringsTid.length));
                                if (stringsTid == null || stringsTid.length < iOffset) {
                                    String[] stringsNew = new String[iOffset + accessData.accSize];
                                    if (stringsTid != null) {
                                        for (int i = 0; i < stringsTid.length; i++) stringsNew[i] = stringsTid[i];
                                    }
                                    stringsTid = stringsNew;
                                }
                                appendToLog("TagBanks.processResult: New stringsTid.length = " + (stringsTid == null ? "null" : stringsTid.length));
                                break;
                            case 3:
                                appendToLog("TagBanks.processResult: Old stringsUser.length = " + (stringsUser == null ? "null" : stringsUser.length));
                                if (stringsUser == null || stringsUser.length < iOffset) {
                                    String[] stringsNew = new String[iOffset + accessData.accSize];
                                    if (stringsUser != null) {
                                        for (int i = 0; i < stringsUser.length; i++) stringsNew[i] = stringsUser[i];
                                    }
                                    stringsUser = stringsNew;
                                }
                                appendToLog("TagBanks.processResult: New stringsUser.length = " + (stringsUser == null ? "null" : stringsUser.length));
                                break;
                        }
                        for (int i = 0; i < accessData.accSize; i++) {
                            String string = accessResult.substring(i * 4, i * 4 + 4);
                            appendToLog("TagBanks.processResult: bank = " + accessData.accBank + ", offset = " + accessData.accOffset + ", i = " + i + ", string = " + string);
                            switch (accessData.accBank) {
                                case 1:
                                    stringsEpc[accessData.accOffset + i] = string;
                                    break;
                                case 2:
                                    stringsTid[accessData.accOffset + i] = string;
                                    break;
                                case 3:
                                    stringsUser[accessData.accOffset + i] = string;
                                    break;
                                default:
                                    break;
                            }

                        }
                    } else {
                        switch (accessData.accBank) {
                            case 2:
                                appendToLog("TagBanks.processResult: writeData = " + accessData.writeData);
                                for (int i = 0; i < accessData.accSize; i++) {
                                    appendToLog("TagBanks.processResult: i = " + i + ", data = " + accessData.writeData.substring(i * 4, i * 4 + 4));
                                    stringsTid[accessData.accOffset +  i] = accessData.writeData.substring(i*4, i*4+4);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            //accessTask = null;
            //return true;
        }
    }
    boolean processTickItems() {
        boolean invalidRequest1 = false;
        int accBank = 0, accOffset = 0, accSize = 0;
        String writeData = null;

        if (selectData.selectMaskEpc == null || selectData.selectMaskEpc.isEmpty()) invalidRequest1 = true;
        else  if (accessData != null) {
            accBank = accessData.accBank; accOffset = accessData.accOffset; accSize = accessData.accSize; writeData = accessData.writeData;
        } else {
            invalidRequest1 = true;
        }

        if (invalidRequest1 == false) {
            if (csLibrary4A.setAccessBank(accBank) == false) {
                invalidRequest1 = true;
            }
            csLibrary4A.appendToLog("TagBanks.processTickItems: bank = " + accBank + ", invalidRequest1 is " + invalidRequest1);
        }
        if (invalidRequest1 == false) {
            if (csLibrary4A.setAccessOffset(accOffset) == false) {
                invalidRequest1 = true;
            }
            csLibrary4A.appendToLog("TagBanks.processTickItems: offset = " + accOffset + ", invalidRequest1 is " + invalidRequest1);
        }
        if (invalidRequest1 == false) {
            if (accSize == 0) {
                invalidRequest1 = true;
            } else if (csLibrary4A.setAccessCount(accSize) == false) {
                invalidRequest1 = true;
            }
            csLibrary4A.appendToLog("TagBanks.processTickItems: size = " + accOffset + ", invalidRequest1 is " + invalidRequest1);
        }
        if (invalidRequest1 == false && writeData != null) {
            if (invalidRequest1 == false) {
                if (csLibrary4A.setAccessWriteData(writeData) == false) {
                    invalidRequest1 = true;
                }
            }
            csLibrary4A.appendToLog("TagBanks.processTickItems: data + " + accessData + ", invalidRequest1 is " + invalidRequest1);
        }
        return invalidRequest1;
    }
    void appendToLog(String string) {
        Log.i("Hello", string);
    }
}
