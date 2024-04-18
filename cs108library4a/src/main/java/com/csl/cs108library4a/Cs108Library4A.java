package com.csl.cs108library4a;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;

import com.csl.cslibrary4a.BarcodeConnector;
import com.csl.cslibrary4a.BarcodeNewland;
import com.csl.cslibrary4a.BluetoothConnector;
import com.csl.cslibrary4a.BluetoothGatt;
import com.csl.cslibrary4a.ControllerConnector;
import com.csl.cslibrary4a.CsReaderConnector108;
import com.csl.cslibrary4a.NotificationConnector;
import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReader;
import com.csl.cslibrary4a.RfidReader.RegionCodes;
import com.csl.cslibrary4a.RfidReaderChipData;
import com.csl.cslibrary4a.RfidReaderChipR2000;
import com.csl.cslibrary4a.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.log10;

public class Cs108Library4A {
    final boolean DEBUG = false;
    final boolean DEBUG_FILE = false;
    private Handler mHandler = new Handler();
    BluetoothAdapter.LeScanCallback mLeScanCallback = null;
    ScanCallback mScanCallback = null;
    File file;

    Context context;
    CsReaderConnector108 csReaderConnector; Utility utility;
    boolean DEBUG_CONNECT, DEBUG_SCAN;
    BluetoothGatt bluetoothGatt;
    RfidReaderChipR2000 rfidReaderChip; //RfidConnector rfidConnector;
    BarcodeNewland barcodeNewland; BarcodeConnector barcodeConnector;
    NotificationConnector notificationConnector;
    ControllerConnector controllerConnector;
    BluetoothConnector bluetoothConnector;
    public Cs108Library4A(Context context, TextView mLogView) {
        this.context = context;
        utility = new Utility(context, mLogView);
        csReaderConnector = new CsReaderConnector108(context, mLogView, utility, true);
        bluetoothGatt = csReaderConnector.bluetoothGatt; DEBUG_CONNECT = bluetoothGatt.DEBUG_CONNECT; DEBUG_SCAN = bluetoothGatt.DEBUG_SCAN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    if (DEBUG) appendToLog("onBatchScanResults()");
                }

                @Override
                public void onScanFailed(int errorCode) {
                    if (DEBUG) appendToLog("onScanFailed()");
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    boolean DEBUG = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        BluetoothGatt.Cs108ScanData scanResultA = new BluetoothGatt.Cs108ScanData(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        boolean found98 = true;
                        if (true) found98 = check9800(scanResultA);
                        if (DEBUG)
                            appendToLog("found98 = " + found98 + ", mScanResultList 0 = " + (mScanResultList != null ? "VALID" : "NULL"));
                        if (mScanResultList != null && found98) {
                            scanResultA.serviceUUID2p2 = check9800_serviceUUID2p1;
                            mScanResultList.add(scanResultA);
                            if (DEBUG) appendToLog("mScanResultList 0 = " + mScanResultList.size());
                        }
                    }
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    if (true) appendToLog("onLeScan()");
                    BluetoothGatt.Cs108ScanData scanResultA = new BluetoothGatt.Cs108ScanData(device, rssi, scanRecord);
                    boolean found98 = true;
                    if (true) found98 = check9800(scanResultA);
                    appendToLog("found98 = " + found98 + ", mScanResultList 1 = " + (mScanResultList != null ? "VALID" : "NULL"));
                    if (mScanResultList != null && found98) {
                        scanResultA.serviceUUID2p2 = check9800_serviceUUID2p1;
                        mScanResultList.add(scanResultA);
                        appendToLog("mScanResultList 1 = " + mScanResultList.size());
                    }
                }
            };
        }

        File path = context.getFilesDir();
        File[] fileArray = path.listFiles();
        boolean deleteFiles = false;
        if (DEBUG)
            appendToLog("Number of file in data storage sub-directory = " + fileArray.length);
        for (int i = 0; i < fileArray.length; i++) {
            String fileName = fileArray[i].toString();
            if (DEBUG) appendToLog("Stored file (" + i + ") = " + fileName);
            File file = new File(fileName);
            if (deleteFiles) file.delete();
        }
        if (deleteFiles)
            if (DEBUG) appendToLog("Stored file size after DELETE = " + path.listFiles().length);
        if (false) {
            double[] tableFreq = csReaderConnector.rfidReader.FCCTableOfFreq;
            double[] tableFreq0 = csReaderConnector.rfidReader.FCCTableOfFreq0;
            csReaderConnector.rfidReader.fccFreqSortedIdx0 = new int[50];
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    if (csReaderConnector.rfidReader.FCCTableOfFreq0[i] == csReaderConnector.rfidReader.FCCTableOfFreq[j]) {
                        csReaderConnector.rfidReader.fccFreqSortedIdx0[i] = j;
                        if (DEBUG) appendToLog("fccFreqSortedIdx0[" + i + "] = " + j);
                        break;
                    }
                }
            }
            double[] tableFreq1 = csReaderConnector.rfidReader.FCCTableOfFreq1;
            csReaderConnector.rfidReader.fccFreqSortedIdx1 = new int[50];
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    if (csReaderConnector.rfidReader.FCCTableOfFreq1[i] == csReaderConnector.rfidReader.FCCTableOfFreq[j]) {
                        csReaderConnector.rfidReader.fccFreqSortedIdx1[i] = j;
                        if (DEBUG) appendToLog("fccFreqSortedIdx1[" + i + "] = " + j);
                        break;
                    }
                }
            }
        }
        /*csReaderConnector.rfidReader.fccFreqTableIdx = new int[50];
        int[] freqSortedINx = csReaderConnector.rfidReader.fccFreqSortedIdx;
        for (int i = 0; i < 50; i++) {
            csReaderConnector.rfidReader.fccFreqTableIdx[csReaderConnector.rfidReader.fccFreqSortedIdx[i]] = i;
        }
        for (int i = 0; i < 50; i++) {
            if (DEBUG) appendToLog("fccFreqTableIdx[" + i + "] = " + csReaderConnector.rfidReader.fccFreqTableIdx[i]);
        }*/

        if (false) {    //for testing
            float fValue;
            fValue = (float) (3.124 - (3.124 - 2.517) / 2);
            appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue));
            fValue = (float) (3.394 - (3.394 - 3.124) / 2);
            appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue));
            fValue = (float) (3.504 - (3.504 - 3.394) / 2);
            appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue));
            fValue = (float) (3.552 - (3.552 - 3.504) / 2);
            appendToLog("fValue = " + fValue + ", percent = " + getBatteryValue2Percent(fValue));
        }
    }
    public String getlibraryVersion() {
        String version = BuildConfig.VERSION_NAME;
        appendToLog("version = " + version);
        return utility.getCombinedVersion(version);
    }
    public String checkVersion() {
        String macVersion = getMacVer();
        String hostVersion = hostProcessorICGetFirmwareVersion();
        String bluetoothVersion = getBluetoothICFirmwareVersion();
        String strVersionRFID = "2.6.44";
        String[] strRFIDVersions = strVersionRFID.split("\\.");
        String strVersionBT = "1.0.17";
        String[] strBTVersions = strVersionBT.split("\\.");
        String strVersionHost = "1.0.16";
        String[] strHostVersions = strVersionHost.split("\\.");
        String stringPopup = "";
        int icsModel = getcsModel();

        if (isRfidFailure() == false && checkHostProcessorVersion(macVersion, Integer.parseInt(strRFIDVersions[0].trim()), Integer.parseInt(strRFIDVersions[1].trim()), Integer.parseInt(strRFIDVersions[2].trim())) == false)
            stringPopup += "\nRFID processor firmware: V" + strVersionRFID;
        if (icsModel == 108)
            if (checkHostProcessorVersion(hostVersion, Integer.parseInt(strHostVersions[0].trim()), Integer.parseInt(strHostVersions[1].trim()), Integer.parseInt(strHostVersions[2].trim())) == false)
                stringPopup += "\nSiliconLab firmware: V" + strVersionHost;
        if (icsModel == 108)
            if (checkHostProcessorVersion(bluetoothVersion, Integer.parseInt(strBTVersions[0].trim()), Integer.parseInt(strBTVersions[1].trim()), Integer.parseInt(strBTVersions[2].trim())) == false)
                stringPopup += "\nBluetooth firmware: V" + strVersionBT;
        return stringPopup;
    }

    //============ utility ============
    public String byteArrayToString(byte[] packet) {
        return utility.byteArrayToString(packet);
    }
    public void appendToLog(String s) {
        utility.appendToLog(s);
    }
    public void appendToLogView(String s) {
        utility.appendToLogView(s);
    }
    public String strFloat16toFloat32(String strData) {
        return utility.strFloat16toFloat32(strData);
    }
    public String str2float16(String strData) {
        return utility.str2float16(strData);
    }
    public float decodeCtesiusTemperature(String strActData, String strCalData) {
        return utility.decodeCtesiusTemperature(strActData, strCalData);
    }
    public float decodeMicronTemperature(int iTag35, String strActData, String strCalData) {
        return utility.decodeMicronTemperature(iTag35, strActData, strCalData);
    }
    public float decodeAsygnTemperature(String string) {
        return utility.decodeAsygnTemperature(string);
    }
    public String temperatureC2F(String strValue) {
        return utility.temperatureC2F(strValue);
    }
    public String temperatureF2C(String strValue) {
        return utility.temperatureF2C(strValue);
    }
    public String getUpcSerial(String strEpc) {
        return utility.getUpcSerial(strEpc);
    }
    public String getUpcSerialDetail(String strUpcSerial) {
        return utility.getUpcSerialDetail(strUpcSerial);
    }
    public String getEpc4upcSerial(Utility.EpcClass epcClass, String filter, String companyPrefix, String itemReference, String serialNumber) {
        return utility.getEpc4upcSerial(epcClass, filter, companyPrefix, itemReference, serialNumber);
    }
    public boolean checkHostProcessorVersion(String version, int majorVersion, int minorVersion, int buildVersion) {
        return utility.checkHostProcessorVersion(version, majorVersion, minorVersion, buildVersion);
    }

    //============ android bluetooth ============
    ArrayList<BluetoothGatt.Cs108ScanData> mScanResultList = new ArrayList<>();
    int check9800_serviceUUID2p1 = 0;
    boolean bleConnection = false;
    ReaderDevice readerDeviceConnect;
    boolean bNeedReconnect = false;
    int iConnectStateTimer = 0;

    boolean check9800(BluetoothGatt.Cs108ScanData scanResultA) {
        boolean found98 = false, DEBUG = false;
        if (DEBUG) appendToLog("decoded data size = " + scanResultA.decoded_scanRecord.size());
        int iNewADLength = 0;
        byte[] newAD = new byte[0];
        int iNewADIndex = 0;
        check9800_serviceUUID2p1 = -1;
        if (bluetoothGatt.isBLUETOOTH_CONNECTinvalid()) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            return true;
        String strTemp = scanResultA.getDevice().getName();
        if (strTemp != null && DEBUG)
            appendToLog("Found name = " + strTemp + ", length = " + String.valueOf(strTemp.length()));
        for (byte bdata : scanResultA.getScanRecord()) {
            if (iNewADIndex >= iNewADLength && iNewADLength != 0) {
                scanResultA.decoded_scanRecord.add(newAD);
                iNewADIndex = 0;
                iNewADLength = 0;
                if (DEBUG)
                    appendToLog("Size = " + scanResultA.decoded_scanRecord.size() + ", " + byteArrayToString(newAD));
            }
            if (iNewADLength == 0) {
                iNewADLength = bdata;
                newAD = new byte[iNewADLength];
                iNewADIndex = 0;
            } else newAD[iNewADIndex++] = bdata;
        }
        if (DEBUG) appendToLog("decoded data size = " + scanResultA.decoded_scanRecord.size());
        for (int i = 0; scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE && i < scanResultA.decoded_scanRecord.size(); i++) {
            byte[] currentAD = scanResultA.decoded_scanRecord.get(i);
            if (DEBUG) appendToLog("Processing decoded data = " + byteArrayToString(currentAD));
            if (currentAD[0] == 2) {
                if (DEBUG) appendToLog("Processing UUIDs");
                if ((currentAD[1] == 0) && currentAD[2] == (byte) 0x98) {
                    if (DEBUG) appendToLog("Found 9800");
                    found98 = true;
                    check9800_serviceUUID2p1 = currentAD[1];
                    if (DEBUG) appendToLog("serviceUD1D2p1 = " + check9800_serviceUUID2p1);
                    break;
                }
            }
        }
        if (found98 == false && DEBUG)
            appendToLog("No 9800: with scanData = " + byteArrayToString(scanResultA.getScanRecord()));
        else if (DEBUG_SCAN)
            appendToLog("Found 9800: with scanData = " + byteArrayToString(scanResultA.getScanRecord()));
        return found98;
    }
    boolean connect1(ReaderDevice readerDevice) {
        boolean DEBUG = false;
        if (DEBUG_CONNECT)
            appendToLog("Connect with NULLreaderDevice = " + (readerDevice == null) + ", NULLreaderDeviceConnect = " + (readerDeviceConnect == null));
        if (readerDevice == null && readerDeviceConnect != null) readerDevice = readerDeviceConnect;
        boolean result = false;
        if (readerDevice != null) {
            bNeedReconnect = false;
            iConnectStateTimer = 0;
            bluetoothGatt.bDiscoverStarted = false;
            bluetoothGatt.setServiceUUIDType(readerDevice.getServiceUUID2p1());
            result = csReaderConnector.connectBle(readerDevice);
        }
        if (DEBUG_CONNECT) appendToLog("Result = " + result);
        return result;
    }
    final Runnable connectRunnable = new Runnable() {
        boolean DEBUG = false;

        @Override
        public void run() {
            if (DEBUG_CONNECT)
                appendToLog("0 connectRunnable: mBluetoothConnectionState = " + bluetoothGatt.bluetoothConnectionState + ", bNeedReconnect = " + bNeedReconnect);
            if (isBleScanning()) {
                if (DEBUG) appendToLog("connectRunnable: still scanning. Stop scanning first");
                scanLeDevice(false);
            } else if (bNeedReconnect) {
                if (bluetoothGatt.bluetoothGatt != null) {
                    if (DEBUG)
                        appendToLog("connectRunnable: mBluetoothGatt is null before connect. disconnect first");
                    csReaderConnector.disconnect();
                } else if (readerDeviceConnect == null) {
                    if (DEBUG) appendToLog("connectRunnable: exit with null readerDeviceConnect");
                    return;
                } else if (bluetoothGatt.bluetoothGatt == null) {
                    if (DEBUG_CONNECT) appendToLog("4 connectRunnable: connect1 starts");
                    connect1(null);
                    bNeedReconnect = false;
                }
            } else if (bluetoothGatt.bluetoothConnectionState == BluetoothProfile.STATE_DISCONNECTED) { //mReaderStreamOutCharacteristic valid around 1500ms
                iConnectStateTimer = 0;
                if (DEBUG)
                    appendToLog("connectRunnable: disconnect as disconnected connectionState is received");
                bNeedReconnect = true;
                if (bluetoothGatt.bluetoothGatt != null) {
                    if (DEBUG) appendToLog("disconnect F");
                    csReaderConnector.disconnect();
                }
            } else if (bluetoothGatt.mReaderStreamOutCharacteristic == null) {
                if (DEBUG_CONNECT)
                    appendToLog("6 connectRunnable: wait as not yet discovery, with iConnectStateTimer = " + iConnectStateTimer);
                if (++iConnectStateTimer > 10) {
                }
            } else {
                if (DEBUG_CONNECT) appendToLog("7 connectRunnable: end of ConnectRunnable");
                return;
            }
            mHandler.postDelayed(connectRunnable, 500);
        }
    };
    final Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            appendToLog("abcc disconnectRunnable with mBarcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size());
            if (barcodeConnector.barcodeToWrite.size() != 0)
                mHandler.postDelayed(disconnectRunnable, 100);
            else {
                appendToLog("disconnect G");
                csReaderConnector.disconnect();
            }
        }
    };
    public boolean isBleScanning() {
        return bluetoothGatt.isBleScanning();
    }
    public boolean scanLeDevice(final boolean enable) {
        boolean DEBUG = false;
        if (enable) mHandler.removeCallbacks(connectRunnable);

        if (DEBUG_SCAN) appendToLog("scanLeDevice[" + enable + "]");
        if (bluetoothGatt.bluetoothDeviceConnectOld != null) {
            if (DEBUG) appendToLog("bluetoothDeviceConnectOld connection state = " + bluetoothGatt.bluetoothManager.getConnectionState(bluetoothGatt.bluetoothDeviceConnectOld, BluetoothProfile.GATT));
        }
        boolean bValue = bluetoothGatt.scanLeDevice(enable, this.mLeScanCallback, this.mScanCallback);
        if (DEBUG_SCAN) appendToLog("isScanning = " + isBleScanning());
        return bValue;
    }
    public BluetoothGatt.Cs108ScanData getNewDeviceScanned() {
        if (mScanResultList.size() != 0) {
            if (false) appendToLog("mScanResultList.size() = " + mScanResultList.size());
            BluetoothGatt.Cs108ScanData cs108ScanData = mScanResultList.get(0); mScanResultList.remove(0);
            return cs108ScanData;
        } else return null;
    }
    public String getBluetoothDeviceAddress() {
        if (bluetoothGatt.getmBluetoothDevice() == null) return null;
        return bluetoothGatt.getmBluetoothDevice().getAddress();
    }
    public String getBluetoothDeviceName() {
        if (bluetoothGatt.getmBluetoothDevice() == null) return null;
        return bluetoothGatt.getmBluetoothDevice().getName();
    }
    public boolean isBleConnected() {
        boolean DEBUG = false;
        boolean bleConnectionNew = csReaderConnector.isBleConnected();
        if (bleConnectionNew) {
            if (bleConnection == false) {
                bleConnection = bleConnectionNew;
                if (DEBUG_CONNECT) appendToLog("Newly connected");

                csReaderConnector.cs108ConnectorDataInit();
                rfidReaderChip = csReaderConnector.rfidReader.rfidReaderChipR2000; //rfidConnector = csReaderConnector108.rfidConnector;
                barcodeNewland = csReaderConnector.barcodeNewland; barcodeConnector = csReaderConnector.barcodeConnector;
                notificationConnector = csReaderConnector.notificationConnector;
                controllerConnector = csReaderConnector.controllerConnector;
                bluetoothConnector = csReaderConnector.bluetoothConnector;

                setRfidOn(true);
                setBarcodeOn(true);
                hostProcessorICGetFirmwareVersion();
                getBluetoothICFirmwareVersion();
                channelOrderType = -1;
                {
                    getBarcodePreSuffix();
                    getBarcodeReadingMode();
                    getBarcodeSerial();
                    //getBarcodeNoDuplicateReading();
                    //getBarcodeDelayTimeOfEachReading();
                    //getBarcodeEnable2dBarCodes();
                    //getBarcodePrefixOrder();
                    //getBarcodeVersion();
                    //barcodeSendCommandLoadUserDefault();
                    //barcodeSendQuerySystem();
                    barcodeNewland.barcodeSendCommandItf14Cksum();

                    notificationConnector.setBatteryAutoReport(true); //0xA003
                }
                abortOperation();
                getHostProcessorICSerialNumber(); //0xb004 (but access Oem as bluetooth version is not got)
                getMacVer();
                { //following two instructions seems not used
                    int iValue = rfidReaderChip.rx000Setting.getDiagnosticConfiguration();
                    if (DEBUG) appendToLog("diagnostic data = " + iValue);
                    macWrite(0xC08, 0x100);
                    rfidReaderChip.rx000OemSetting.getVersionCode();
                }
                regionCode = null;
                getCountryCode();
                {
                    rfidReaderChip.rx000OemSetting.getFreqModifyCode();
                    rfidReaderChip.rx000OemSetting.getSpecialCountryVersion();
                }
                getSerialNumber();
                if (DEBUG_CONNECT) appendToLog("Start checkVersionRunnable");
                mHandler.postDelayed(checkVersionRunnable, 500);
            } else if (rfidReaderChip.bFirmware_reset_before) {
                rfidReaderChip.bFirmware_reset_before = false;
                mHandler.postDelayed(reinitaliseDataRunnable, 500);
            }
        } else if (bleConnection) {
            bleConnection = bleConnectionNew;
            if (DEBUG) appendToLog("Newly disconnected");
        }
        return(bleConnection);
    }
    public void connect(ReaderDevice readerDevice) {
        if (isBleConnected()) return;
        if (bluetoothGatt.bluetoothGatt != null) csReaderConnector.disconnect();
        if (readerDevice != null) readerDeviceConnect = readerDevice;
        mHandler.removeCallbacks(connectRunnable);
        bNeedReconnect = true; mHandler.post(connectRunnable);
        if (DEBUG_CONNECT) appendToLog("Start ConnectRunnable");
    }
    public void disconnect(boolean tempDisconnect) {
        appendToLog("abcc tempDisconnect: getBarcodeOnStatus = " + (getBarcodeOnStatus() ? "on" : "off"));
        if (DEBUG) appendToLog("tempDisconnect = " + tempDisconnect);
        mHandler.removeCallbacks(checkVersionRunnable);
        mHandler.removeCallbacks(runnableToggleConnection);
        if (getBarcodeOnStatus()) {
            appendToLog("tempDisconnect: setBarcodeOn(false)");
            if (barcodeConnector.barcodeToWrite.size() != 0) {
                appendToLog("going to disconnectRunnable with remaining mBarcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size() + ", data = " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
            }
            barcodeConnector.barcodeToWrite.clear();
            setBarcodeOn(false);
        } else appendToLog("tempDisconnect: getBarcodeOnStatus is false");
        mHandler.postDelayed(disconnectRunnable, 100);
        appendToLog("done with tempDisconnect = " + tempDisconnect);
        if (tempDisconnect == false)    {
            mHandler.removeCallbacks(connectRunnable);
            bluetoothGatt.bluetoothDeviceConnectOld = bluetoothGatt.bluetoothAdapter.getRemoteDevice(readerDeviceConnect.getAddress());
            readerDeviceConnect = null;
        }
    }
    public boolean forceBTdisconnect() {
        return bluetoothConnector.forceBTdisconnect();
    }
    public int getRssi() {
        return bluetoothGatt.getRssi();
    }
    public long getStreamInRate() {
        return csReaderConnector.getStreamInRate();
    }
    public int get98XX() {
        return 0;
    }

    //============ Rfid ============
    boolean setInvAlgoNoSave(boolean dynamicAlgo) {
        appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = " + dynamicAlgo);
        return setInvAlgo1(dynamicAlgo);
    }
    boolean getInvAlgo1() {
        if (true) return csReaderConnector.rfidReader.getInvAlgo1();
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getInvAlgo();
        if (iValue < 0) {
            return true;
        } else {
            return (iValue != 0 ? true : false);
        }
    }
    boolean setInvAlgo1(boolean dynamicAlgo) {
        boolean bValue = true, DEBUG = false;
        if (DEBUG) appendToLog("2 setInvAlgo1");
        int iAlgo = rfidReaderChip.rx000Setting.getInvAlgo();
        int iRetry = getRetryCount();
        int iAbFlip = rfidReaderChip.rx000Setting.getAlgoAbFlip();
        if (DEBUG) appendToLog("2 setInvAlgo1: going to setInvAlgo with dynamicAlgo = " + dynamicAlgo + ", iAlgo = " + iAlgo + ", iRetry = " + iRetry + ", iabFlip = " + iAbFlip);
        if ( (dynamicAlgo && iAlgo == 0) || (dynamicAlgo == false && iAlgo == 3)) {
            bValue = rfidReaderChip.rx000Setting.setInvAlgo(dynamicAlgo ? 3 : 0);
            if (DEBUG) appendToLog("After setInvAlgo, bValue = " + bValue);
            if (DEBUG) appendToLog("Before setPopulation, population = " + getPopulation());
            if (bValue) bValue = setPopulation(getPopulation());
            if (DEBUG) appendToLog("After setPopulation, bValue = " + bValue);
            if (bValue) bValue = setRetryCount(iRetry);
            if (DEBUG) appendToLog("After setRetryCount, bValue = " + bValue);
            if (bValue) bValue = rfidReaderChip.rx000Setting.setAlgoAbFlip(iAbFlip);
            if (DEBUG) appendToLog("After setAlgoAbFlip, bValue = " + bValue);
        }
        return bValue;
    }
/*
    private final int FCC_CHN_CNT = 50;
    private final double[] FCCTableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, 905.25, 905.75, 906.25, 906.75, 907.25,//10
            907.75, 908.25, 908.75, 909.25, 909.75, 910.25, 910.75, 911.25, 911.75, 912.25,//20
            912.75, 913.25, 913.75, 914.25, 914.75, 915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    private final double[] FCCTableOfFreq0 = new double[] {
            903.75, 912.25, 907.75, 910.25, 922.75,     923.25, 923.75, 915.25, 909.25, 912.75,
            910.75, 913.75, 909.75, 905.25, 911.75,     902.75, 914.25, 918.25, 926.25, 925.75,
            920.75, 920.25, 907.25, 914.75, 919.75,     922.25, 903.25, 906.25, 905.75, 926.75,
            924.25, 904.75, 925.25, 924.75, 919.25,     916.75, 911.25, 921.25, 908.25, 908.75,
            913.25, 916.25, 904.25, 906.75, 917.75,     921.75, 917.25, 927.25, 918.75, 915.75 };
    private int[] fccFreqSortedIdx0;
    private final double[] FCCTableOfFreq1 = new double[] {
            915.25, 920.75, 909.25, 912.25, 918.25,     920.25, 909.75, 910.25, 919.75, 922.75,
            908.75, 913.75, 903.75, 919.25, 922.25,     907.75, 911.75, 923.75, 916.75, 926.25,
            908.25, 912.75, 924.25, 916.25, 927.25,     907.25, 910.75, 903.25, 917.75, 926.75,
            905.25, 911.25, 924.75, 917.25, 925.75,     906.75, 914.25, 904.75, 918.75, 923.25,
            902.75, 914.75, 905.75, 915.75, 925.25,     906.25, 921.25, 913.25, 921.75, 904.25 };
    private int[] fccFreqSortedIdx1;
    private int[] fccFreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            0x00180E4D, //915.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            0x00180E4B, //914.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E3D, //911.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E33, //908.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            0x00180E49, //914.25 MHz
            0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E39, //910.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
            };
    private int[] fccFreqTableIdx;
    private final int[] fccFreqSortedIdx = new int[] {
            26, 25, 1, 48, 47,
            3, 49, 35, 33, 13,
            32, 30, 5, 4, 45,
            38, 24, 8, 22, 39,
            17, 18, 2, 12, 6,
            19, 7, 29, 23, 9,
            31, 27, 15, 16, 10,
            44, 14, 34, 28, 21,
            42, 11, 46, 20, 43,
            37, 36, 40, 0, 41 };
    private final int AUS_CHN_CNT = 10;
    private final double[] AUSTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25 };
    private final int[] AusFreqTable = new int[] {
            0x00180E63, // 920.75MHz
            0x00180E69, // 922.25MHz
            0x00180E6F, // 923.75MHz
            0x00180E73, // 924.75MHz
            0x00180E65, // 921.25MHz
            0x00180E6B, // 922.75MHz
            0x00180E71, // 924.25MHz
            0x00180E75, // 925.25MHz
            0x00180E67, // 921.75MHz
            0x00180E6D, // 923.25MHz
    };
    private final int[] ausFreqSortedIdx = new int[] {
            0, 3, 6, 8, 1,
            4, 7, 9, 2, 5 };

    private final double[] PRTableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    private int[] freqTable = null;
    private int[] freqSortedIdx = null;

    private final int VZ_CHN_CNT = 10;
    private final double[] VZTableOfFreq = new double[] {
            922.75, 923.25, 923.75, 924.25, 924.75,
            925.25, 925.75, 926.25, 926.75, 927.25 };
    private final int[] vzFreqTable = new int[] {
            0x00180E77, // 925.75 MHz
            0x00180E6B, // 922.75MHz
            0x00180E7D, // 927.25 MHz
            0x00180E75, // 925.25MHz
            0x00180E6D, // 923.25MHz
            0x00180E7B, // 926.75 MHz
            0x00180E73, // 924.75MHz
            0x00180E6F, // 923.75MHz
            0x00180E79, // 926.25 MHz
            0x00180E71, // 924.25MHz
    };
    private final int[] vzFreqSortedIdx = new int[] {
            6, 0, 9, 5, 1,
            8, 4, 2, 7, 3 };

    private final int BR1_CHN_CNT = 24;
    private final double[] BR1TableOfFreq = new double[] {
            //902.75, 903.25, 903.75, 904.25, 904.75,
            //905.25, 905.75, 906.25, 906.75, 907.25,
            //907.75, 908.25, 908.75, 909.25, 909.75,
            //910.25, 910.75, 911.25, 911.75, 912.25,
            //912.75, 913.25, 913.75, 914.25, 914.75,
            //915.25,
            915.75, 916.25, 916.75, 917.25, 917.75,
            918.25, 918.75, 919.25, 919.75, 920.25,
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25,
            925.75, 926.25, 926.75, 927.25 };
    private final int[] br1FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            //0x00180E4D, //915.25 MHz
            //0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            //0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            //0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            //0x00180E25, //905.25 MHz
            //0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            //0x00180E4B, //914.75 MHz
            //0x00180E2B, //906.75 MHz
            //0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            //0x00180E3D, //911.25 MHz
            //0x00180E3F, //911.75 MHz
            //0x00180E1F, //903.75 MHz
            //0x00180E33, //908.75 MHz
            //0x00180E27, //905.75 MHz
            //0x00180E41, //912.25 MHz
            //0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            //0x00180E49, //914.25 MHz
            //0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            //0x00180E39, //910.25 MHz
            //0x00180E3B, //910.75 MHz
            //0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            //0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            //0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            //0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            //0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            //0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br1FreqSortedIdx = new int[] {
            0, 22, 21, 23, 9,
            7, 6, 4, 19, 12,
            13, 3, 5, 1, 18,
            8, 2, 16, 20, 17,
            11, 10, 14, 15 };

    private final int BR2_CHN_CNT = 33;
    private double[] BR2TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75,
            905.25, 905.75, 906.25, 906.75,
            //907.25, 907.75, 908.25, 908.75, 909.25,
            //909.75, 910.25, 910.75, 911.25, 911.75,
            //912.25, 912.75, 913.25, 913.75, 914.25,
            //914.75, 915.25,
            915.75, 916.25, 916.75, 917.25, 917.75,
            918.25, 918.75, 919.25, 919.75, 920.25,
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25,
            925.75, 926.25, 926.75, 927.25 };
    private final int[] br2FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            //0x00180E4D, //915.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            //0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            //0x00180E4B, //914.75 MHz
            0x00180E2B, //906.75 MHz
            //0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            //0x00180E3D, //911.25 MHz
            //0x00180E3F, //911.75 MHz
            0x00180E1F, //903.75 MHz
            //0x00180E33, //908.75 MHz
            0x00180E27, //905.75 MHz
            //0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            //0x00180E49, //914.25 MHz
            //0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            //0x00180E39, //910.25 MHz
            //0x00180E3B, //910.75 MHz
            //0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            //0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            //0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            //0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            //0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br2FreqSortedIdx = new int[] {
            9, 1, 31, 30, 3,
            32, 18, 16, 15, 13,
            5, 4, 28, 21, 8,
            22, 2, 6, 7, 12,
            14, 10, 27, 17, 11,
            25, 29, 26, 20, 19,
            23, 0, 24,
    };

    private final int BR3_CHN_CNT = 9;
    private final double[] BR3TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75 };
    private final int[] br3FreqTable = new int[] {
            0x00180E1D, //903.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E29, //906.25 MHz
            0x00180E1B, //902.75 MHz
    };
    private final int[] br3FreqSortedIdx = new int[] {
            1, 3, 5, 4, 8,
            2, 6, 7, 0 };

    private final int BR4_CHN_CNT = 4;
    private final double[] BR4TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25 };
    private final int[] br4FreqTable = new int[] {
            0x00180E1D, //903.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E1F, //903.75 MHz
            0x00180E1B, //902.75 MHz
    };
    private final int[] br4FreqSortedIdx = new int[] {
            1, 3, 2, 0 };

    private final int BR5_CHN_CNT = 14;
    private final double[] BR5TableOfFreq = new double[] {
            917.75, 918.25, 918.75, 919.25, 919.75, // 4
            920.25, 920.75, 921.25, 921.75, 922.25, // 9
            922.75, 923.25, 923.75, 924.25 };
    private final int[] br5FreqTable = new int[] {
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E6F, //923.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br5FreqSortedIdx = new int[] {
            5, 3, 2, 0, 8,
            9, 1, 4, 12, 13,
            7, 6, 10, 11 };

    private final int HK_CHN_CNT = 8;
    private final double[] HKTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25 };
    private final int[] hkFreqTable = new int[] {
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
            0x00180E71, //924.25MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E6D, //923.25MHz
            0x00180E6F, //923.75MHz
            0x00180E67, //921.75MHz
    };
    private final int[] hkFreqSortedIdx = new int[] {
            0, 3, 7, 1, 4,
            5, 6, 2 };

    private final int BD_CHN_CNT = 4;
    private final double[] BDTableOfFreq = new double[] {
            925.25, 925.75, 926.25, 926.75 };
    private final int[] bdFreqTable = new int[] {
            0x00180E75, //925.25MHz
            0x00180E77, //925.75MHz
            0x00180E79, //926.25MHz
            0x00180E7B, //926.75MHz
    };
    private final int[] bdFreqSortedIdx = new int[] {
            0, 3, 1, 2  };

    private final int TW_CHN_CNT = 12;
    private final double[] TWTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,
            924.75, 925.25, 925.75, 926.25, 926.75,
            927.25, 927.75 };
    private int[] twFreqTable = new int[] {
            0x00180E7D, //927.25MHz   10
            0x00180E73, //924.75MHz   5
            0x00180E6B, //922.75MHz   1
            0x00180E75, //925.25MHz   6
            0x00180E7F, //927.75MHz   11
            0x00180E71, //924.25MHz   4
            0x00180E79, //926.25MHz   8
            0x00180E6D, //923.25MHz   2
            0x00180E7B, //926.75MHz   9
            0x00180E69, //922.25MHz   0
            0x00180E77, //925.75MHz   7
            0x00180E6F, //923.75MHz   3
    };
    private final int[] twFreqSortedIdx = new int[] {
            10, 5, 1, 6, 11,
            4, 8, 2, 9, 0,
            7, 3 };

    private final int MYS_CHN_CNT = 8;
    private final double[] MYSTableOfFreq = new double[] {
            919.75, 920.25, 920.75, 921.25, 921.75,
            922.25, 922.75, 923.25 };
    private final int[] mysFreqTable = new int[] {
            0x00180E5F, //919.75MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E61, //920.25MHz
            0x00180E67, //921.75MHz
            0x00180E6D, //923.25MHz
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
    };
    private final int[] mysFreqSortedIdx = new int[] {
            0, 3, 6, 1, 4,
            7, 2, 5 };

    private final int ZA_CHN_CNT = 16;
    private final double[] ZATableOfFreq = new double[] {
            915.7, 915.9, 916.1, 916.3, 916.5,
            916.7, 916.9, 917.1, 917.3, 917.5,
            917.7, 917.9, 918.1, 918.3, 918.5,
            918.7 };
    private final int[] zaFreqTable = new int[] {
            0x003C23C5, //915.7 MHz
            0x003C23C7, //915.9 MHz
            0x003C23C9, //916.1 MHz
            0x003C23CB, //916.3 MHz
            0x003C23CD, //916.5 MHz
            0x003C23CF, //916.7 MHz
            0x003C23D1, //916.9 MHz
            0x003C23D3, //917.1 MHz
            0x003C23D5, //917.3 MHz
            0x003C23D7, //917.5 MHz
            0x003C23D9, //917.7 MHz
            0x003C23DB, //917.9 MHz
            0x003C23DD, //918.1 MHz
            0x003C23DF, //918.3 MHz
            0x003C23E1, //918.5 MHz
            0x003C23E3, //918.7 MHz
    };
    private final int[] zaFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10, 11, 12, 13, 14,
            15 };

    private final int ID_CHN_CNT = 4;
    private final double[] IDTableOfFreq = new double[] {
            923.25, 923.75, 924.25, 924.75 };
    private final int[] indonesiaFreqTable = new int[] {
            0x00180E6D, //923.25 MHz
            0x00180E6F,//923.75 MHz
            0x00180E71,//924.25 MHz
            0x00180E73,//924.75 MHz
    };
    private final int[] indonesiaFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int IL_CHN_CNT = 7;
    private final double[] ILTableOfFreq = new double[] {
            915.25, 915.5, 915.75, 916.0, 916.25, // 4
            916.5, 916.75 };
    private final int[] ilFreqTable = new int[] {
            0x00180E4D, //915.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E4E, //915.5 MHz
            0x00180E52, //916.5 MHz
            0x00180E4F, //915.75 MHz
            0x00180E53, //916.75 MHz
            0x00180E50, //916.0 MHz
    };
    private final int[] ilFreqSortedIdx = new int[] {
            0, 4, 1, 5, 2,  6, 3 };

    private final int IL2019RW_CHN_CNT = 5;
    private final double[] IL2019RWTableOfFreq = new double[] {
            915.9, 916.025, 916.15, 916.275, 916.4 };
    private final int[] il2019RwFreqTable = new int[] {
            0x003C23C7, //915.9 MHz
            0x003C23C8, //916.025 MHz
            0x003C23C9, //916.15 MHz
            0x003C23CA, //916.275 MHz
            0x003C23CB, //916.4 MHz
    };
    private final int[] il2019RwFreqSortedIdx = new int[] {
            0, 4, 1, 2, 3 };

    private final int PH_CHN_CNT = 8;
    private final double[] PHTableOfFreq = new double[] {
            918.125, 918.375, 918.625, 918.875, 919.125, // 5
            919.375, 919.625, 919.875 };
    private final int[] phFreqTable = new int[] {
            0x00301CB1, //918.125MHz   Channel 0
            0x00301CBB, //919.375MHz   Channel 5
            0x00301CB7, //918.875MHz   Channel 3
            0x00301CBF, //919.875MHz   Channel 7
            0x00301CB3, //918.375MHz   Channel 1
            0x00301CBD, //919.625MHz   Channel 6
            0x00301CB5, //918.625MHz   Channel 2
            0x00301CB9, //919.125MHz   Channel 4
    };
    private final int[] phFreqSortedIdx = new int[] {
            0, 5, 3, 7, 1,  6, 2, 4 };

    private final int NZ_CHN_CNT = 11;
    private final double[] NZTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,// 4
            924.75, 925.25, 925.75, 926.25, 926.75,// 9
            927.25 };
    private final int[] nzFreqTable = new int[] {
            0x00180E71, //924.25 MHz
            0x00180E77, //925.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E6D, //923.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E75, //925.25 MHz
            0x00180E6B, //922.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E6F, //923.75 MHz
            0x00180E73, //924.75 MHz
    };
    private final int[] nzFreqSortedIdx = new int[] {
            4, 7, 0, 9, 2,  10, 6, 1, 8, 3,     5 };

    private final int CN_CHN_CNT = 16;
    private final double[] CHNTableOfFreq = new double[] {
            920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375, 922.625, 922.875,
            923.125, 923.375, 923.625, 923.875, 924.125, 924.375 };
    private final int[] cnFreqTable = new int[] {
            0x00301CD3, //922.375MHz
            0x00301CD1, //922.125MHz
            0x00301CCD, //921.625MHz
            0x00301CC5, //920.625MHz
            0x00301CD9, //923.125MHz
            0x00301CE1, //924.125MHz
            0x00301CCB, //921.375MHz
            0x00301CC7, //920.875MHz
            0x00301CD7, //922.875MHz
            0x00301CD5, //922.625MHz
            0x00301CC9, //921.125MHz
            0x00301CDF, //923.875MHz
            0x00301CDD, //923.625MHz
            0x00301CDB, //923.375MHz
            0x00301CCF, //921.875MHz
            0x00301CE3, //924.375MHz
    };
    private final int[] cnFreqSortedIdx = new int[] {
            7, 6, 4, 0, 10,
            14, 3, 1, 9, 8,
            2, 13, 12, 11, 5,
            15 };

    private final int UH1_CHN_CNT = 10;
    private final double[] UH1TableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75 };
    private final int[] uh1FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            0x00180E4D, //915.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E55, //917.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
    };
    private final int[] uh1FreqSortedIdx = new int[] {
            1, 0, 8, 7, 5,
            4, 6, 2, 9, 3 };

    private final int UH2_CHN_CNT = 15;
    private final double[] UH2TableOfFreq = new double[] {
            920.25, 920.75, 921.25, 921.75, 922.25,   // 4
            922.75, 923.25, 923.75, 924.25, 924.75,   // 9
            925.25, 925.75, 926.25, 926.75, 927.25 };
    private final int[] uh2FreqTable = new int[] {
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E73, //924.75 MHz
            0x00180E6F, //923.75 MHz
            0x00180E77, //925.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] uh2FreqSortedIdx = new int[]{
            13, 12, 14, 0, 10,
            3, 4, 9, 7, 11,
            8, 2, 1, 5, 6, };

    private final int LH_CHN_CNT = 26;
    private double[] LHTableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75, 907.25, // 9
            907.75, 908.25, 908.75, 909.25, 909.75, // 14
            910.25, 910.75, 911.25, 911.75, 912.25, // 19
            912.75, 913.25, 913.75, 914.25, 914.75, // 24
            915.25, // 25
            //915.75, 916.25, 916.75, 917.25, 917.75,
            //918.25, 918.75, 919.25, 919.75, 920.25,
            //920.75, 921.25, 921.75, 922.25, 922.75,
            //923.25, 923.75, 924.25, 924.75, 925.25,
            //925.75, 926.25, 926.75, 927.25,
    };
    private final int[] lhFreqTable = new int[] {
            0x00180E1B, //902.75 MHz
            0x00180E35, //909.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E37, //909.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E39, //910.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E23, //904.75 MHz
            0x00180E3D, //911.25 MHz
            0x00180E25, //905.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E43, //912.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E2D, //907.25 MHz
            0x00180E47, //913.75 MHz
            0x00180E2F, //907.75 MHz
            0x00180E49, //914.25 MHz
            0x00180E31, //908.25 MHz
            0x00180E4B, //914.75 MHz
            0x00180E33, //908.75 MHz
            0x00180E4D, //915.25 MHz


            //0x00180E4F, //915.75 MHz
            //0x00180E7B, //926.75 MHz
            //0x00180E79, //926.25 MHz
            //0x00180E7D, //927.25 MHz
            //0x00180E61, //920.25 MHz
            //0x00180E5D, //919.25 MHz
            //0x00180E5B, //918.75 MHz
            //0x00180E57, //917.75 MHz
            //0x00180E75, //925.25 MHz
            //0x00180E67, //921.75 MHz
            //0x00180E69, //922.25 MHz
            //0x00180E55, //917.25 MHz
            //0x00180E59, //918.25 MHz
            //0x00180E51, //916.25 MHz
            //0x00180E73, //924.75 MHz
            //0x00180E5F, //919.75 MHz
            //0x00180E53, //916.75 MHz
            //0x00180E6F, //923.75 MHz
            //0x00180E77, //925.75 MHz
            //0x00180E71, //924.25 MHz
            //0x00180E65, //921.25 MHz
            //0x00180E63, //920.75 MHz
            //0x00180E6B, //922.75 MHz
            //0x00180E6D, //923.25 MHz
    };
    private final int[] lhFreqSortedIdx = new int[] {
            0, 13, 1, 14, 2,
            15, 3, 16, 4, 17,
            5, 18, 6, 19, 7,
            20, 8, 21, 9, 22,
            10, 23, 11, 24, 12,
            25 };

    private final int LH1_CHN_CNT = 14;
    private double[] LH1TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75, 907.25, // 9
            907.75, 908.25, 908.75, 909.25, // 13
    };
    private final int[] lh1FreqTable = new int[] {
            0x00180E1B, //902.75 MHz
            0x00180E35, //909.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E1F, //903.75 MHz
            0x00180E21, //904.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E27, //905.75 MHz
            0x00180E29, //906.25 MHz
            0x00180E2B, //906.75 MHz
            0x00180E2D, //907.25 MHz
            0x00180E2F, //907.75 MHz
            0x00180E31, //908.25 MHz
            0x00180E33, //908.75 MHz
    };
    private final int[] lh1FreqSortedIdx = new int[] {
            0, 13, 1, 2, 3,
            4, 5, 6, 7, 8,
            9, 10, 11, 12 };

    private final int LH2_CHN_CNT = 11;
    private double[] LH2TableOfFreq = new double[] {
            909.75, 910.25, 910.75, 911.25, 911.75, // 4
            912.25, 912.75, 913.25, 913.75, 914.25, // 9
            914.75 };
    private final int[] lh2FreqTable = new int[] {
            0x00180E37, //909.75 MHz
            0x00180E39, //910.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E3D, //911.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E43, //912.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E47, //913.75 MHz
            0x00180E49, //914.25 MHz
            0x00180E4B, //914.75 MHz
    };
    private final int[] lh2FreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10 };

    private final int ETSI_CHN_CNT = 4;
    private final double[] ETSITableOfFreq = new double[] {
            865.70, 866.30, 866.90, 867.50 };
    private final int[] etsiFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    private final int[] etsiFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int IDA_CHN_CNT = 3;
    private final double[] IDATableOfFreq = new double[] {
            865.70, 866.30, 866.90 };
    private final int[] indiaFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
    };
    private final int[] indiaFreqSortedIdx = new int[] {
            0, 1, 2 };

    private final int KR_CHN_CNT = 19;
    private final double[] KRTableOfFreq = new double[] {
            910.20, 910.40, 910.60, 910.80, 911.00, 911.20, 911.40, 911.60, 911.80, 912.00,
            912.20, 912.40, 912.60, 912.80, 913.00, 913.20, 913.40, 913.60, 913.80 };
    private int[] krFreqTable = new int[] {
            0x003C23A8, //912.8MHz   13
            0x003C23A0, //912.0MHz   9
            0x003C23AC, //913.2MHz   15
            0x003C239E, //911.8MHz   8
            0x003C23A4, //912.4MHz   11
            0x003C23B2, //913.8MHz   18
            0x003C2392, //910.6MHz   2
            0x003C23B0, //913.6MHz   17
            0x003C2390, //910.4MHz   1
            0x003C239C, //911.6MHz   7
            0x003C2396, //911.0MHz   4
            0x003C23A2, //912.2MHz   10
            0x003C238E, //910.2MHz   0
            0x003C23A6, //912.6MHz   12
            0x003C2398, //911.2MHz   5
            0x003C2394, //910.8MHz   3
            0x003C23AE, //913.4MHz   16
            0x003C239A, //911.4MHz   6
            0x003C23AA, //913.0MHz   14
        };
    private final int[] krFreqSortedIdx = new int[] {
            13, 9, 15, 8, 11,
            18, 2, 17, 1, 7,
            4, 10, 0, 12, 5,
            3, 16, 6, 14 };

    private final int KR2017RW_CHN_CNT = 6;
    private final double[] KR2017RwTableOfFreq = new double[] {
            917.30, 917.90, 918.50, 919.10, 919.70, 920.30 };
    private int[] kr2017RwFreqTable = new int[] {
            0x003C23D5, // 917.3 -> 917.25  MHz Channel 1
            0x003C23DB, //917.9 -> 918 MHz Channel 2
            0x003C23E1, //918.5 MHz Channel 3
            0x003C23E7, //919.1 -> 919  MHz Channel 4
            0x003C23ED, //919.7 -> 919.75 MHz Channel 5
            0x003C23F3 // 920.3 -> 920.25 MHz Channel 6
        };
    private final int[] kr2017RwFreqSortedIdx = new int[] {
            3, 0, 5, 1, 4, 2 };

    private final int JPN2012_CHN_CNT = 4;
    private final double[] JPN2012TableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40 };
    private final int[] jpn2012FreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            //0x003C23F6, //920.600MHz   Channel 5
            //0x003C23F8, //920.800MHz   Channel 6
    };
    private final int[] jpn2012FreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int JPN2012A_CHN_CNT = 6;
    private final double[] JPN2012ATableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40, 920.60, 920.80 };
    private final int[] jpn2012AFreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            0x003C23F6, //920.600MHz   Channel 5
            0x003C23F8, //920.800MHz   Channel 6
    };
    private final int[] jpn2012AFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4, 5 };

    private final int ETSIUPPERBAND_CHN_CNT = 4;
    private final double[] ETSIUPPERBANDTableOfFreq = new double[] {
            916.3, 917.5, 918.7, 919.9 };
    private final int[] etsiupperbandFreqTable = new int[] {
            0x003C23CB, //916.3 MHz
            0x003C23D7, //917.5 MHz
            0x003C23E3, //918.7 MHz
            0x003C23EF, //919.9 MHz
        };
    private final int[] etsiupperbandFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int VN1_CHN_CNT = 3;
    private final double[] VN1TableOfFreq = new double[] {
            866.30, 866.90, 867.50 };
    private final int[] vietnam1FreqTable = new int[] {
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    private final int[] vietnam1FreqSortedIdx = new int[] {
            0, 1, 2 };

    private final int VN2_CHN_CNT = 8;
    private final double[] VN2TableOfFreq = new double[] {
            918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25 };
    private final int[] vietnam2FreqTable = new int[] {
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    private final int[] vietnam2FreqSortedIdx = new int[] {
            3, 1, 0, 6, 7, 2, 5, 4 };

    private final int VN3_CHN_CNT = 4;
    private final double[] VN3TableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25 };
    private final int[] vietnam3FreqTable = new int[] {
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    private final int[] vietnam3FreqSortedIdx = new int[] {
            2, 3, 1, 0 };
    boolean setChannelData(RegionCodes regionCode) {
        return true;
    }
    private void SetFrequencyBand (UInt32 frequencySelector, BandState config, UInt32 multdiv, UInt32 pllcc)
    {
        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_SEL, frequencySelector);

        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_CFG, (uint)config);

        if (config == BandState.ENABLE)
        {
            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDIVMULT, multdiv);

            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDACCTL, pllcc);
        }
    }
    double[] GetAvailableFrequencyTable(RegionCodes regionCode) {
        double[] freqText;
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        return FCCTableOfFreq0;
//                    case 1:
//                        return FCCTableOfFreq1;
//                    default:
//                        return FCCTableOfFreq;
//                }
                return FCCTableOfFreq;
            case PR:
                return PRTableOfFreq;
            case VZ:
                return VZTableOfFreq;
            case AU:
                return AUSTableOfFreq;
            case BR1:
                return BR1TableOfFreq;
            case BR2:
                return BR2TableOfFreq;
            case BR3:
                return BR3TableOfFreq;
            case BR4:
                return BR4TableOfFreq;
            case BR5:
                return BR5TableOfFreq;
            case HK:
            case SG:
            case TH:
            case VN:
                return HKTableOfFreq;
            case VN1:
                return VN1TableOfFreq;
            case VN2:
                return VN2TableOfFreq;
            case VN3:
                return VN3TableOfFreq;
            case BD:
                return BDTableOfFreq;
            case TW:
                return TWTableOfFreq;
            case MY:
                return MYSTableOfFreq;
            case ZA:
                return ZATableOfFreq;
            case ID:
                return IDTableOfFreq;
            case IL:
                return ILTableOfFreq;
            case IL2019RW:
                return IL2019RWTableOfFreq;
            case PH:
                return PHTableOfFreq;
            case NZ:
                return NZTableOfFreq;
            case CN:
                return CHNTableOfFreq;

            case UH1:
                return UH1TableOfFreq;
            case UH2:
                return UH2TableOfFreq;
            case LH:
                return LHTableOfFreq;
            case LH1:
                return LH1TableOfFreq;
            case LH2:
                return LH2TableOfFreq;

            case ETSI:
                appendToLog("Got ETSI Table of Frequencies");
                return ETSITableOfFreq;
            case IN:
                return IDATableOfFreq;
            case KR:
                return KRTableOfFreq;
            case KR2017RW:
                return KR2017RwTableOfFreq;
            case JP:
                return JPN2012TableOfFreq;
            case JP6:
                return JPN2012ATableOfFreq;
            case ETSIUPPERBAND:
                return ETSIUPPERBANDTableOfFreq;

            default:
                return new double[0];
        }
    }
    private int[] FreqIndex(RegionCodes regionCode) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        return fccFreqSortedIdx0;
//                    case 1:
//                        return fccFreqSortedIdx1;
//                    default:
//                        return fccFreqSortedIdx;
//                }
                return fccFreqSortedIdx;
            case PR:
                if (freqSortedIdx == null) {
                    freqSortedIdx = new int[PRTableOfFreq.length];
                    if (DEBUG) appendToLog("PR: freqSortedIdx size = " + freqSortedIdx.length);
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    for (int i=0; i<freqSortedIdx.length; i++) list.add(new Integer(i));
                    Collections.shuffle(list);
                    for (int i=0; i<freqSortedIdx.length; i++) {
                        freqSortedIdx[i] = list.get(i);
                        if (DEBUG) appendToLog("PR: Random Value = " + freqSortedIdx[i]);
                    }
                }
                return freqSortedIdx;
            case VZ:
                return vzFreqSortedIdx;
            case AU:
                return ausFreqSortedIdx;
            case BR1:
                return br1FreqSortedIdx;
            case BR2:
                return br2FreqSortedIdx;
            case BR3:
                return br3FreqSortedIdx;
            case BR4:
                return br4FreqSortedIdx;
            case BR5:
                return br5FreqSortedIdx;
            case HK:
            case SG:
            case TH:
            case VN:
                return hkFreqSortedIdx;
            case VN1:
                return vietnam1FreqSortedIdx;
            case VN2:
                return vietnam2FreqSortedIdx;
            case VN3:
                return vietnam3FreqSortedIdx;
            case BD:
                return bdFreqSortedIdx;
            case TW:
                return twFreqSortedIdx;
            case MY:
                return mysFreqSortedIdx;
            case ZA:
                return zaFreqSortedIdx;
            case ID:
                return indonesiaFreqSortedIdx;
            case IL:
                return ilFreqSortedIdx;
            case IL2019RW:
                return il2019RwFreqSortedIdx;
            case PH:
                return phFreqSortedIdx;
            case NZ:
                return nzFreqSortedIdx;
            case CN:
                return cnFreqSortedIdx;

            case UH1:
                return uh1FreqSortedIdx;
            case UH2:
                return uh2FreqSortedIdx;
            case LH:
                return lhFreqSortedIdx;
            case LH1:
                return lh1FreqSortedIdx;
            case LH2:
                return lh2FreqSortedIdx;

            case ETSI:
                return etsiFreqSortedIdx;
            case IN:
                return indiaFreqSortedIdx;
            case KR:
                return krFreqSortedIdx;
            case KR2017RW:
                return kr2017RwFreqSortedIdx;
            case JP:
                return jpn2012FreqSortedIdx;
            case JP6:
                return jpn2012AFreqSortedIdx;
            case ETSIUPPERBAND:
                return etsiupperbandFreqSortedIdx;

            default:
                return null;
        }
    }
    int[] FreqTable(RegionCodes regionCode) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                int[] freqTableIdx = fccFreqTableIdx;
//                int[] freqSortedIdx;
//                int[] freqTable = new int[50];
//                if (DEBUG) appendToLog("gerVersionCode = " + mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode());
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        freqSortedIdx = fccFreqSortedIdx0;
//                        break;
//                    case 1:
//                        freqSortedIdx = fccFreqSortedIdx1;
//                        break;
//                    default:
//                        freqSortedIdx = fccFreqSortedIdx;
//                        break;
//                }
//                for (int i = 0; i < 50; i++) {
//                    freqTable[i] = fccFreqTable[fccFreqTableIdx[freqSortedIdx[i]]];
//                    if (DEBUG) appendToLog("i = " + i + ", freqSortedIdx = " + freqSortedIdx[i] + ", fccFreqTableIdx = " + fccFreqTableIdx[freqSortedIdx[i]] + ", freqTable[" + i + "] = " + freqTable[i]);
//                }
//                return freqTable;
                return fccFreqTable;
            case PR:
                int[] freqSortedIndex = FreqIndex(regionCode);
                int[] freqTable = null;
                if (freqSortedIndex != null) {
                    freqTable = new int[freqSortedIndex.length];
                    for (int i = 0; i < freqSortedIndex.length; i++) {
                        int j = 0;
                        for (; j < FCCTableOfFreq.length; j++) {
                            if (FCCTableOfFreq[j] == PRTableOfFreq[freqSortedIndex[i]]) break;
                        }
                        freqTable[i] = fccFreqTable[fccFreqTableIdx[j]];
                    }
                } else
                if (DEBUG) appendToLog("NULL freqSortedIndex");
                return freqTable;   // return prFreqTable;
            case VZ:
                return vzFreqTable;
            case AU:
                return AusFreqTable;

            case BR1:
                return br1FreqTable;
            case BR2:
                return br2FreqTable;
            case BR3:
                return br3FreqTable;
            case BR4:
                return br4FreqTable;
            case BR5:
                return br5FreqTable;

            case HK:
            case SG:
            case TH:
            case VN:
                return hkFreqTable;
            case VN1:
                return vietnam1FreqTable;
            case VN2:
                return vietnam2FreqTable;
            case VN3:
                return vietnam3FreqTable;
            case BD:
                return bdFreqTable;
            case TW:
                return twFreqTable;
            case MY:
                return mysFreqTable;
            case ZA:
                return zaFreqTable;

            case ID:
                return indonesiaFreqTable;
            case IL:
                return ilFreqTable;
            case IL2019RW:
                return il2019RwFreqTable;
            case PH:
                return phFreqTable;
            case NZ:
                return nzFreqTable;
            case CN:
                return cnFreqTable;

            case UH1:
                return uh1FreqTable;
            case UH2:
                return uh2FreqTable;
            case LH:
                return lhFreqTable;
            case LH1:
                return lh1FreqTable;
            case LH2:
                return lh2FreqTable;

            case ETSI:
                return etsiFreqTable;
            case IN:
                return indiaFreqTable;
            case KR:
                return krFreqTable;
            case KR2017RW:
                return kr2017RwFreqTable;
            case JP:
                return jpn2012FreqTable;
            case JP6:
                return jpn2012AFreqTable;
            case ETSIUPPERBAND:
                return etsiupperbandFreqTable;

            default:
                return null;
        }
    }
    private long GetPllcc(RegionCodes regionCode) {
        switch (regionCode) {
            case ETSI:
            case IN:
                return 0x14070400;  //Notice: the read value is 0x14040400
        }
        return 0x14070200;  //Notice: the read value is 0x14020200
    }
    private boolean FreqChnWithinRange(int Channel, RegionCodes regionCode) {
        int TotalCnt = FreqChnCnt(regionCode);
        if (TotalCnt <= 0)   return false;
        if (Channel >= 0 && Channel < TotalCnt) return true;
        return false;
    }
    private int FreqSortedIdxTbls(RegionCodes regionCode, int Channel) {
        int TotalCnt = FreqChnCnt(regionCode);
        int[] freqIndex = FreqIndex(regionCode);
        if (!FreqChnWithinRange(Channel, regionCode) || freqIndex == null)
            return -1;
        for (int i = 0; i < TotalCnt; i++) {
            if (freqIndex[i] == Channel)    return i;
        }
        return -1;
    }
*/
    boolean getConnectionHSpeed() {
        return bluetoothGatt.getConnectionHSpeedA();
    }
    boolean setConnectionHSpeed(boolean on) {
        return bluetoothGatt.setConnectionHSpeedA(on);
    }
    byte tagDelayDefaultCompactSetting = 0;
    byte tagDelayDefaultNormalSetting = 30;
    byte tagDelaySettingDefault = tagDelayDefaultCompactSetting, tagDelaySetting = tagDelaySettingDefault;
    long cycleDelaySetting;
/*
    byte[] string2ByteArray(String string) {
        byte[] bytes = null;
        if (string == null) return null;
        if ((string.length()/2)*2 != string.length()) string += "0";
        for (int i = 0; i < string.length(); i+=2) {
            try {
                Short sValue = Short.parseShort(string.substring(i, i + 2), 16);
                byte[] bytesNew = new byte[1];
                if (bytes != null) {
                    bytesNew = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, bytesNew, 0, bytes.length);
                }
                bytesNew[bytesNew.length - 1] = (byte) (sValue & 0xFF);
                bytes = bytesNew;
            } catch (Exception ex) {
                appendToLog("Exception in i = " + i + ", substring = " + string.substring(i, i+2));
                break;
            }
        }
        return bytes;
    }
*/
    boolean starAuthOperation() {
        if (true) return csReaderConnector.rfidReader.starAuthOperation();
        rfidReaderChip.setPwrManagementMode(false);
        return rfidReaderChip.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE);
    }
    public String getAuthMatchData() {
        if (true) return csReaderConnector.rfidReader.getAuthMatchData();
        int iValue1 = 96;
        String strValue;
        strValue = rfidReaderChip.rx000Setting.getAuthMatchData();
        if (strValue == null) return null;
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1)  strLength++;
        return strValue.substring(0, strLength);
    }
    public boolean setAuthMatchData(String mask) {
        if (true) return csReaderConnector.rfidReader.setAuthMatchData(mask);
        boolean result = false;
        if (mask != null) {
            result = rfidReaderChip.rx000Setting.setAuthMatchData(mask);
        }
        return result;
    }
    int beepCountSettingDefault = 8, beepCountSetting = beepCountSettingDefault;
    //    String serverLocation = "https://" + "www.convergence.com.hk:" + "29090/WebServiceRESTs/1.0/req/" + "create-update-delete/update-entity/" + "tagdata";
//    String serverLocation = "http://ptsv2.com/t/10i1t-1519143332/post";
    //    aetOperationMode(continuous, antennaSequenceMode, result)
    public int getStartQValue() {
        return rfidReaderChip.rx000Setting.getAlgoStartQ(3);
    }
    public int getMaxQValue() {
        return rfidReaderChip.rx000Setting.getAlgoMaxQ(3);
    }
    public int getMinQValue() {
        return rfidReaderChip.rx000Setting.getAlgoMinQ(3);
    }
    public boolean setDynamicQParms(int startQValue, int minQValue, int maxQValue, int retryCount) {
        appendToLog("setTagGroup: going to setAlgoSelect with input as 3");
        boolean result;
        result = rfidReaderChip.rx000Setting.setAlgoSelect(3);
        if (result) {
            result = rfidReaderChip.rx000Setting.setAlgoStartQ(startQValue, maxQValue, minQValue, -1, -1, -1);
        }
        if (result) result = setRetryCount(retryCount);
        return result;
    }
    public int getFixedQValue() {
        return rfidReaderChip.rx000Setting.getAlgoStartQ(0);
    }
    public int getFixedRetryCount() {
        return rfidReaderChip.rx000Setting.getAlgoRetry(0);
    }
    public boolean getRepeatUnitNoTags() {
        return rfidReaderChip.rx000Setting.getAlgoRunTilZero(0) == 1 ? true : false;
    }
    public boolean setFixedQParms(int qValue, int retryCount, boolean repeatUnitNoTags) {
        if (DEBUG) appendToLog("qValue=" + qValue + ", retryCount = " + retryCount + ", repeatUntilNoTags = " + repeatUnitNoTags);
        boolean result;
        appendToLog("setTagGroup: going to setAlgoSelect with input as 0");
        result = rfidReaderChip.rx000Setting.setAlgoSelect(0);
        appendToLog("Hello6: invAlgo = 0 ");
        if (qValue == getFixedQValue() && retryCount == getFixedRetryCount() && repeatUnitNoTags == getRepeatUnitNoTags())  return true;
        appendToLog("Hello6: new invAlgo parameters are set");
        if (result) {
            result = rfidReaderChip.rx000Setting.setAlgoStartQ(qValue, -1, -1, -1, -1, -1);
        }
        if (result) result = setRetryCount(retryCount);
        if (result) {
            result = rfidReaderChip.rx000Setting.setAlgoRunTilZero(repeatUnitNoTags ? 1 : 0);
        }
        return result;
    }
    class PreFilterData {
        boolean enable; int target, action, bank, offset; String mask; boolean maskbit;
        PreFilterData() { }
        PreFilterData(boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
            this.enable = enable;
            this.target = target;
            this.action = action;
            this.bank = bank;
            this.offset = offset;
            this.mask = mask;
            this.maskbit = maskbit;
        }
    }
    PreFilterData preFilterData;
    class PreMatchData {
        boolean enable; int target, action; int bank, offset; String mask; int maskblen; int querySelect; long pwrlevel; boolean invAlgo; int qValue;
        PreMatchData(boolean enable, int target, int action, int bank, int offset, String mask, int maskblen, int querySelect, long pwrlevel, boolean invAlgo, int qValue) {
            this.enable = enable;
            this.target = target;
            this.action = action;
            this.bank = bank;
            this.offset = offset;
            this.mask = mask;
            this.maskblen = maskblen;
            this.querySelect = querySelect;
            this.pwrlevel = pwrlevel;
            this.invAlgo = invAlgo;
            this.qValue = qValue;
        }
    }
    PreMatchData preMatchData;
    boolean setOnlyPowerLevel(long pwrlevel) {
        appendToLog("start");
        return rfidReaderChip.rx000Setting.setAntennaPower(pwrlevel);
    }
    boolean setSelectCriteria3(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask, int maskblen) {
        boolean DEBUG = false;
        if (DEBUG || true) appendToLog("setSelectCriteria 3 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask + ", maskbitlen = " + maskblen);
        int maskbytelen = maskblen / 4; if ((maskblen % 4) != 0) maskbytelen++; if (maskbytelen > 64) maskbytelen = 64;
        if (mask.length() > maskbytelen ) mask = mask.substring(0, maskbytelen);
        if (index == 0) preMatchData = new PreMatchData(enable, target, action, bank, offset, mask, maskblen, rfidReaderChip.rx000Setting.getQuerySelect(), getPwrlevel(), getInvAlgo(), getQValue());
        boolean result = true;
        if (index != rfidReaderChip.rx000Setting.getInvSelectIndex()) {
        	result = rfidReaderChip.rx000Setting.setInvSelectIndex(index);
            if (DEBUG) appendToLog("After setInvSelectIndex, result = " + result);
        }
        if (result) result = rfidReaderChip.rx000Setting.setSelectEnable(enable ? 1 : 0, target, action, delay);
        if (DEBUG) appendToLog("After setSelectEnable, result = " + result);
        if (result) result = rfidReaderChip.rx000Setting.setSelectMaskBank(bank);
        if (DEBUG) appendToLog("After setSelectMaskBank, result = " + result);
        if (result) result = rfidReaderChip.rx000Setting.setSelectMaskOffset(offset);
        if (DEBUG) appendToLog("After setSelectMaskOffset, result = " + result + " and mask = " + mask);
        if (mask == null)   return false;
        if (result) result = rfidReaderChip.rx000Setting.setSelectMaskLength(maskblen);
        if (DEBUG) appendToLog("After setSelectMaskLength, result = " + result);
        if (result) result = rfidReaderChip.rx000Setting.setSelectMaskData(mask);
        if (DEBUG) appendToLog("After setSelectMaskData, result = " + result);
        if (result) {
            if (enable) {
                result = rfidReaderChip.rx000Setting.setTagSelect(1);
                if (DEBUG) appendToLog("After setTagSelect[1], result = " + result);
                result = rfidReaderChip.rx000Setting.setQuerySelect(3);
                if (DEBUG) appendToLog("After setQuerySelect[3], result = " + result);
            } else {
                result = rfidReaderChip.rx000Setting.setTagSelect(0);
                if (DEBUG) appendToLog("After setTagSelect[0], result = " + result);
                result = rfidReaderChip.rx000Setting.setQuerySelect(0);
                if (DEBUG) appendToLog("After setQuerySelect[0], result = " + result);
            }
        }
        return result;
    }
    PostMatchData postMatchDataOld; boolean postMatchDataChanged = false;
    PreMatchData preMatchDataOld; boolean preMatchDataChanged = false;

    @Keep boolean setSelectedTag1(String selectMask, int selectBank, int selectOffset, int delay, long pwrlevel, int qValue, int matchRep) {
        appendToLog("setSelectCriteria selectMask = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", delay = " + delay + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        boolean setSuccess = true, DEBUG = true;
        if (selectMask == null)   selectMask = "";

        if (preMatchDataChanged == false) {
            preMatchDataChanged = true; if (DEBUG) appendToLog("setSelectCriteria preMatchDataChanged is SET with preMatchData = " + (preMatchData != null ? "valid" : "null"));
            if (preMatchData == null) {
                preMatchData = new PreMatchData(false, rfidReaderChip.rx000Setting.getQueryTarget(), 0, 0, 0, "", 0,
                        rfidReaderChip.rx000Setting.getQuerySelect(), getPwrlevel(), getInvAlgo(), getQValue());
            }
            preMatchDataOld = preMatchData;
        }
        int index = 0;
        int indexCurrent = rfidReaderChip.rx000Setting.invSelectIndex;
        for (int i = 0; i < 7; i++) {
            rfidReaderChip.rx000Setting.setInvSelectIndex(i);
            if (rfidReaderChip.rx000Setting.getSelectEnable() == 0) {
                appendToLog("free select when i = " + i + ". Going to setSelectCriteria");
                setSuccess = setSelectCriteria3(i, true, 4, 0, delay, selectBank, selectOffset, selectMask, selectMask.length() * 4);
                if (DEBUG) appendToLog("setSelectCriteria after setSelectCriteria, setSuccess = " + setSuccess);
                break;
            }
        }
        rfidReaderChip.rx000Setting.setInvSelectIndex(indexCurrent);

        if (setSuccess) setSuccess = setOnlyPowerLevel(pwrlevel);
        if (DEBUG) appendToLog("setSelectCriteria after setOnlyPowerLevel, setSuccess = " + setSuccess);
        if (false) {
            if (setSuccess) setSuccess = setFixedQParms(qValue, 5, false);
            if (DEBUG) appendToLog("setSelectCriteria after setFixedQParms, setSuccess = " + setSuccess);
            if (setSuccess) setSuccess = rfidReaderChip.rx000Setting.setAlgoAbFlip(1);
            if (DEBUG) appendToLog("setSelectCriteria after setAlgoAbFlip, setSuccess = " + setSuccess);
            if (setSuccess) setSuccess = setInvAlgo1(false);
            if (DEBUG) appendToLog("setSelectCriteria after setInvAlgo1, setSuccess = " + setSuccess);
        }

        if (setSuccess) setSuccess = rfidReaderChip.rx000Setting.setMatchRep(matchRep);
        if (DEBUG) appendToLog("setSelectCriteria after setMatchRep, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = rfidReaderChip.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting);
        if (DEBUG) appendToLog("setSelectCriteria after setTagDelay, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = rfidReaderChip.rx000Setting.setCycleDelay(cycleDelaySetting);
        if (DEBUG) appendToLog("setSelectCriteria after setCycleDelay, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = rfidReaderChip.rx000Setting.setInvModeCompact(false);
        if (DEBUG) appendToLog("setSelectCriteria after setInvModeCompact, setSuccess = " + setSuccess);
        return setSuccess;
    }
/*
    final private int modifyCodeAA = 0xAA;
    enum RegionCodes {
        NULL,
        AG, BD, CL, CO, CR, DR, MX, PM, UG,
        BR1, BR2, BR3, BR4, BR5,
        IL, IL2019RW, PR, PH, SG, ZA, VZ,
        AU, NZ, HK, MY, VN, VN1, VN2, VN3,
        CN, TW, KR, KR2017RW, JP, JP6, TH, IN, FCC,
        UH1, UH2, LH, LH1, LH2,
        ETSI, ID, ETSIUPPERBAND,

        Albania1, Albania2, Algeria1, Algeria2, Algeria3,     Algeria4, Argentina, Armenia, Australia1, Australia2,
        Austria1, Austria2, Azerbaijan, Bahrain, Bangladesh,  Belarus, Belgium1, Belgium2, Bolivia, Bosnia,
        Botswana, Brazil1, Brazil2, Brunei1, Brunei2,         Bulgaria1, Bulgaria2, Cambodia, Cameroon, Canada,
        Chile1, Chile2, Chile3, China, Colombia,              Congo, CostaRica, Cotedlvoire, Croatia, Cuba,
        Cyprus1, Cyprus2, Czech1, Czech2, Denmark1,           Denmark2, Dominican, Ecuador, Egypt, ElSalvador,
        Estonia, Finland1, Finland2, France, Georgia,         Germany, Ghana, Greece, Guatemala, HongKong1,
        HongKong2, Hungary1, Hungary2, Iceland, India,        Indonesia, Iran, Ireland1, Ireland2, Israel,
        Italy, Jamaica, Japan4, Japan6, Jordan,               Kazakhstan, Kenya, Korea, KoreaDPR, Kuwait,
        Kyrgyz, Latvia, Lebanon, Libya, Liechtenstein1,       Liechtenstein2, Lithuania1, Lithuania2, Luxembourg1, Luxembourg2,
        Macao, Macedonia, Malaysia, Malta1, Malta2,           Mauritius, Mexico, Moldova1, Moldova2, Mongolia,
        Montenegro, Morocco, Netherlands, NewZealand1, NewZealand2,   Nicaragua, Nigeria, Norway1, Norway2, Oman,
        Pakistan, Panama, Paraguay, Peru, Philippines,        Poland, Portugal, Romania, Russia1, Russia3,
        Senegal, Serbia, Singapore1, Singapore2, Slovak1,     Slovak2, Slovenia1, Solvenia2, SAfrica1, SAfrica2,
        Spain, SriLanka, Sudan, Sweden1, Sweden2,             Switzerland1, Switzerland2, Syria, Taiwan1, Taiwan2,
        Tajikistan, Tanzania, Thailand, Trinidad, Tunisia,    Turkey, Turkmenistan, Uganda, Ukraine, UAE,
        UK1, UK2, USA, Uruguay, Venezuela,                    Vietnam1, Vietnam2, Yemen, Zimbabwe, Vietnam3
    }
    String regionCode2StringArray(RegionCodes region) {
        switch (region) {
            case AG:
                return "Argentina";
            case CL:
                return "Chile";
            case CO:
                return "Columbia";
            case CR:
                return "Costa Rica";
            case DR:
                return "Dominican Republic";
            case MX:
                return "Mexico";
            case PM:
                return "Panama";
            case UG:
                return "Uruguay";
            case BR1:
                return "Brazil 915-927";
            case BR2:
                return "Brazil 902-906, 915-927";
            case BR3:
                return "Brazil 902-906";
            case BR4:
                return "Brazil 902-904";
            case BR5:
                return "Brazil 917-924";
            case IL:
            case IL2019RW:
                return "Israel";
            case PR:
                return "Peru";
            case PH:
                return "Philippines";
            case SG:
                return "Singapore";
            case ZA:
                return "South Africa";
            case VZ:
                return "Venezuela";
            case AU:
                return "Australia";
            case NZ:
                return "New Zealand";
            case HK:
                return "Hong Kong";
            case MY:
                return "Malaysia";
            case VN:
                return "Vietnam";
            case VN1:
                return "Vietnam1";
            case VN2:
                return "Vietnam2";
            case VN3:
                return "Vietnam3";
            case BD:
                return "Bangladesh";
            case CN:
                return "China";
            case TW:
                return "Taiwan";
            case KR:
            case KR2017RW:
                return "Korea";
            case JP:
                return "Japan";
            case JP6:
                return "Japan";
            case TH:
                return "Thailand";
            case ID:
                return "Indonesia";
            case FCC:
                if (getFreqModifyCode() == modifyCodeAA) return "FCC";
                return "USA/Canada";
            case UH1:
                return "UH1";
            case UH2:
                return "UH2";
            case LH:
                return "LH";
            case LH1:
                return "LH1";
            case LH2:
                return "LH2";
            case ETSI:
                return "Europe";
            case IN:
                return "India";
            case ETSIUPPERBAND:
                return "ETSI Upper Band";
            default:
                return region.toString();
        }
    }
*/
    RegionCodes regionCode = RegionCodes.JP6;
    final RegionCodes regionCodeDefault4Country2 = RegionCodes.FCC;
    RegionCodes[] getRegionList() {
        boolean DEBUG = false;
        RegionCodes[] regionList = null;
        {
            switch (getCountryCode()) {
                case 1:
                    if (regionCode == null) regionCode = RegionCodes.ETSI;
                    regionList = new RegionCodes[]{RegionCodes.ETSI, RegionCodes.IN, RegionCodes.VN1};
                    break;
                default:
                case 2:
                    int modifyCode = getFreqModifyCode();
                    if (modifyCode != csReaderConnector.rfidReader.modifyCodeAA) {
                        if (regionCode == null) regionCode = regionCodeDefault4Country2;
                        regionList = new RegionCodes[]{
                                RegionCodes.AG,
                                RegionCodes.AU,
                                RegionCodes.BD,
                                RegionCodes.BR1, RegionCodes.BR2, RegionCodes.BR3, RegionCodes.BR4, RegionCodes.BR5,
                                RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR,
                                RegionCodes.HK,
                                RegionCodes.ID,
                                RegionCodes.IL2019RW,
                                RegionCodes.KR2017RW,
                                RegionCodes.LH1, RegionCodes.LH2,
                                RegionCodes.MY,
                                RegionCodes.MX, RegionCodes.PM,
                                RegionCodes.PR,
                                RegionCodes.PH, RegionCodes.SG,
                                RegionCodes.ZA,
                                RegionCodes.TH,
                                RegionCodes.UH1, RegionCodes.UH2,
                                RegionCodes.UG,
                                RegionCodes.FCC,
                                RegionCodes.VZ,
                                RegionCodes.VN};
                    } else {
                        String strSpecialCountryVersion = rfidReaderChip.rx000OemSetting.getSpecialCountryVersion();
                        if (strSpecialCountryVersion.contains("OFCA")) {
                            regionCode = RegionCodes.HK;
                            regionList = new RegionCodes[]{RegionCodes.HK};
                        } else if (strSpecialCountryVersion.contains("SG")) {
                            regionCode = RegionCodes.SG;
                            regionList = new RegionCodes[]{RegionCodes.SG};
                        } else if (strSpecialCountryVersion.contains("AS")) {
                            regionCode = RegionCodes.AU;
                            regionList = new RegionCodes[]{RegionCodes.AU};
                        } else if (strSpecialCountryVersion.contains("NZ")) {
                            regionCode = RegionCodes.NZ;
                            regionList = new RegionCodes[]{RegionCodes.NZ};
                        } else if (strSpecialCountryVersion.contains("ZA")) {
                            regionCode = RegionCodes.ZA;
                            regionList = new RegionCodes[]{RegionCodes.ZA};
                        } else if (strSpecialCountryVersion.contains("TH")) {
                            regionCode = RegionCodes.TH;
                            regionList = new RegionCodes[]{RegionCodes.TH};
                        } else {    //if (strSpecialCountryVersion.contains("*USA")) {
                            regionCode = regionCodeDefault4Country2;
                            regionList = new RegionCodes[]{RegionCodes.FCC};
                        }
                    }
                    break;
                case 3:
//                break;
                case 4:
                    if (regionCode == null) regionCode = RegionCodes.TW;
                    regionList = new RegionCodes[]{RegionCodes.TW, RegionCodes.AU, RegionCodes.MY,
                            RegionCodes.HK, RegionCodes.SG, RegionCodes.ID, RegionCodes.CN};
                    break;
                case 5:
                    regionCode = RegionCodes.KR;
                    regionList = new RegionCodes[]{RegionCodes.KR};
                    break;
                case 6:
                    regionCode = RegionCodes.KR2017RW;
                    regionList = new RegionCodes[]{RegionCodes.KR2017RW};
                    break;
                case 7:
                    if (regionCode == null) regionCode = RegionCodes.CN;
                    regionList = new RegionCodes[]{RegionCodes.CN, RegionCodes.AU, RegionCodes.HK, RegionCodes.TH,
                            RegionCodes.SG, RegionCodes.MY, RegionCodes.ID, RegionCodes.VN2, RegionCodes.VN3};
                    break;
                case 8:
                    String strSpecialCountryVersion = rfidReaderChip.rx000OemSetting.getSpecialCountryVersion();
                    if (strSpecialCountryVersion.contains("6")) {
                        regionCode = RegionCodes.JP6;
                        regionList = new RegionCodes[]{RegionCodes.JP6};
                    } else {
                        regionCode = RegionCodes.JP;
                        regionList = new RegionCodes[]{RegionCodes.JP};
                    }
                    break;
                case 9:
                    regionCode = RegionCodes.ETSIUPPERBAND;
                    regionList = new RegionCodes[]{RegionCodes.ETSIUPPERBAND};
                    break;
            }
        }
        countryInList = 0; if (DEBUG) appendToLog("saveSetting2File testpoint 1");
        for (int i = 0; i < regionList.length; i++) {
            if (regionCode == regionList[i]) {
                countryInList = i; if (DEBUG) appendToLog("saveSetting2File testpoint 2"); break;
            }
        }
        if (countryInListDefault < 0) countryInListDefault = countryInList;
        appendToLog("countryInListDefault = " + countryInListDefault);
        return regionList;
    }
    boolean toggledConnection = false;
    Runnable runnableToggleConnection = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) appendToLog("runnableToggleConnection(): toggledConnection = " + toggledConnection + ", isBleConnected() = " + isBleConnected());
            if (isBleConnected() == false)  toggledConnection = true;
            if (toggledConnection) {
                if (isBleConnected() == false) {
                    if (connect1(null) == false) return;
                } else return;
            } else { appendToLog("disconnect H"); csReaderConnector.disconnect(); }
            mHandler.postDelayed(runnableToggleConnection, 500);
        }
    };
    public boolean getChannelHoppingDefault() {
        int countryCode = getCountryCode();
        appendToLog("getChannelHoppingDefault: countryCode (for channelOrderType) = " + countryCode);
        {
            if (countryCode == 1 || countryCode == 8 || countryCode == 9) return false;
            return true;
        }
    }
/*
    final int iCountryEnumInfoColumn = 7;
    String[] strCountryEnumInfo = {
            "1", "Albania1", "-1", "4", "Fixed", "600", "865.7",
            "2", "Albania2", "-2 RW", "23", "Hop", "250", "915.25",
            "3", "Algeria1", "-1", "4", "Fixed", "600", "871.6",
            "4", "Algeria2", "-1", "4", "Fixed", "600", "881.6",
            "5", "Algeria3", "-9", "3", "Fixed", "1200", "916.3",
            "6", "Algeria4", "-7", "2", "Fixed", "500", "925.25",
            "7", "Argentina", "-2 RW", "50", "Hop", "500", "902.75",
            "8", "Armenia", "-1", "4", "Fixed", "600", "865.7",
            "9", "Australia1", "-2 AS", "10", "Hop", "500", "920.75",
            "10", "Australia2", "-2 AS", "14", "Hop", "500", "918.75",
            "11", "Austria1", "-1", "4", "Fixed", "600", "865.7",
            "12", "Austria2", "-9", "3", "Fixed", "1200", "916.3",
            "13", "Azerbaijan", "-1", "4", "Fixed", "600", "865.7",
            "14", "Bahrain", "-1", "4", "Fixed", "600", "865.7",
            "15", "Bangladesh", "-1", "4", "Fixed", "600", "865.7",
            "16", "Belarus", "-1", "4", "Fixed", "600", "865.7",
            "17", "Belgium1", "-1", "4", "Fixed", "600", "865.7",
            "18", "Belgium2", "-9", "3", "Fixed", "1200", "916.3",
            "19", "Bolivia", "-2", "50", "Hop", "500", "902.75",
            "20", "Bosnia", "-1", "4", "Fixed", "600", "865.7",
            "21", "Botswana", "-1", "4", "Fixed", "600", "865.7",
            "22", "Brazil1", "-2 RW", "9", "Fixed", "500", "902.75",
            "23", "Brazil2", "-2 RW", "24", "Fixed", "500", "915.75",
            "24", "Brunei1", "-1", "4", "Fixed", "600", "865.7",
            "25", "Brunei2", "-7", "7", "Fixed", "250", "923.25",
            "26", "Blgaria1", "-1", "4", "Fixed", "600", "865.7",
            "27", "Bulgaria2", "-9", "3", "Fixed", "1200", "916.3",
            "28", "Cambodia", "-7", "16", "Hop", "250", "920.625",
            "29", "Cameroon", "-1", "4", "Fixed", "600", "865.7",
            "30", "Canada", "-2", "50", "Hop", "500", "902.75",
            "31", "Chile1", "-2 RW", "3", "Fixed", "1200", "916.3",
            "32", "Chile2", "-2 RW", "24", "Hop", "500", "915.75",
            "33", "Chile3", "-2 RW", "4", "Hop", "500", "925.75",
            "34", "China", "-7", "16", "Hop", "250", "920.625",
            "35", "Colombia", "-2 RW", "50", "Hop", "500", "902.75",
            "36", "Congo", "-1", "4", "Fixed", "600", "865.7",
            "37", "CostaRica", "-2 RW", "50", "Hop", "500", "902.75",
            "38", "Cotedlvoire", "-1", "4", "Fixed", "600", "865.7",
            "39", "Croatia", "-1", "4", "Fixed", "600", "865.7",
            "40", "Cuba", "-2 RW", "50", "Hop", "500", "902.75",
            "41", "Cyprus1", "-1", "4", "Fixed", "600", "865.7",
            "42", "Cyprus2", "-9", "3", "Fixed", "1200", "916.3",
            "43", "Czech1", "-1", "4", "Fixed", "600", "865.7",
            "44", "Czech2", "-9", "3", "Fixed", "1200", "916.3",
            "45", "Denmark1", "-1", "4", "Fixed", "600", "865.7",
            "46", "Denmark2", "-9", "3", "Fixed",  "1200", "916.3",
            "47", "Dominican", "-2 RW", "50", "Hop", "500", "902.75",
            "48", "Ecuador", "-2 RW", "50", "Hop", "500", "902.75",
            "49", "Egypt", "-1", "4", "Fixed",  "600", "865.7",
            "50", "ElSalvador", "-2 RW", "50", "Hop", "500", "902.75",
            "51", "Estonia", "-1", "4", "Fixed", "600", "865.7",
            "52", "Finland1", "-1", "4", "Fixed",  "600", "865.7",
            "53", "Finland2", "-9", "3", "Fixed", "1200", "916.3",
            "54", "France", "-1", "4", "Fixed", "600", "865.7",
            "55", "Georgia", "-1", "4", "Fixed",  "600", "865.7",
            "56", "Germany", "-1", "4", "Fixed", "600", "865.7",
            "57", "Ghana", "-1", "4", "Fixed", "600", "865.7",
            "58", "Greece", "-1", "4", "Fixed",  "600", "865.7",
            "59", " Guatemala", "-2 RW", "50", "Hop", "500", "902.75",
            "60", "HongKong1", "-1", "4", "Fixed", "600", "865.7",
            "61", "HongKong2", "-2 OFCA", "50", "Hop", "50", "921.25",
            "62", "Hungary1", "-1", "4", "Fixed", "600", "865.7",
            "63", "Hungary2", "-9", "3", "Fixed", "1200", "916.3",
            "64", "Iceland", "-1", "4", "Fixed", "600", "865.7",
            "65", "India", "-1", "3", "Fixed", "600", "865.7",
            "66", "Indonesia", "-7", "4", "Hop", "500", "923.75",
            "67", "Iran", "-1", "4", "Fixed", "600", "865.7",
            "68", "Ireland1", "-1", "4", "Fixed", "600", "865.7",
            "69", "Ireland2", "-9", "3", "Fixed", "1200", "916.3",
            "70", "Israel", "-9", "3", "Fixed", "500", "915.5",
            "71", "Italy", "-1", "4", "Fixed", "600", "865.7",
            "72", "Jamaica", "-2 RW", "50", "Hop", "500", "902.75",
            "73", "Japan4", "-8", "4", "Fixed", "1200", "916.8",
            "74", "Japan6", "-8", "6", "Fixed", "1200", "916.8",
            "75", "Jordan", "-1", "4", "Fixed", "600", "865.7",
            "76", "Kazakhstan", "-1", "4", "Fixed", "600", "865.7",
            "77", "Kenya", "-1", "4", "Fixed", "600", "865.7",
            "78", "Korea", "-6", "6", "Hop", "600", "917.3",
            "79", "KoreaDPR", "-7", "16", "Hop", "250", "920.625",
            "80", "Kuwait", "-1", "4", "Fixed", "600", "865.7",
            "81", "Kyrgyz", "-1", "4", "Fixed", "600", "865.7",
            "82", "Latvia", "-1", "4", "Fixed", "600", "865.7",
            "83", "Lebanon", "-1", "4", "Fixed", "600", "865.7",
            "84", "Libya", "-1", "4", "Fixed", "600", "865.7",
            "85", "Liechtenstein1", "-1", "4", "Fixed", "600", "865.7",
            "86", "Liechtenstein2", "-9", "3", "Fixed", "1200", "916.3",
            "87", "Lithuania1", "-1", "4", "Fixed", "600", "865.7",
            "88", "Lithuania2", "-9", "3", "Fixed", "1200", "916.3",
            "89", "Luxembourg1", "-1", "4", "Fixed", "600", "865.7",
            "90", "Luxembourg2", "-9", "3", "Fixed", "1200", "916.3",
            "91", "Macao", "-7", "16", "Hop", "250", "920.625",
            "92", "Macedonia", "-1", "4", "Fixed", "600", "865.7",
            "93", "Malaysia", "-7", "6", "Hop", "500", "919.75",
            "94", "Malta1", "-1", "4", "Fixed", "600", "865.7",
            "95", "Malta2", "-9", "3", "Fixed", "1200", "916.3",
            "96", "Mauritius", "-1", "4", "Fixed", "600", "865.7",
            "97", "Mexico", "-2", "50", "Hop", "500", "902.75",
            "98", "Moldova1", "-1", "4", "Fixed", "600", "865.7",
            "99", "Moldova2", "-9", "3", "Fixed", "1200", "916.3",
            "100", "Mongolia", "-7", "16", "Hop", "250", "920.625",
            "101", "Montenegro", "-1", "4", "Fixed", "600", "865.7",
            "102", "Morocco", "-1", "4", "Fixed", "600", "865.7",
            "103", "Netherlands", "-1", "4", "Fixed", "600", "865.7",
            "104", "NewZealand1", "-1", "4", "Hop", "500", "864.75",
            "105", "NewZealand2", "-2 NZ", "14", "Hop", "500", "920.75",
            "106", "Nicaragua", "-2 RW", "50", "Hop", "500", "902.75",
            "107", "Nigeria", "-1", "4", "Fixed", "600", "865.7",
            "108", "Norway1", "-1", "4", "Fixed", "600", "865.7",
            "109", "Norway2", "-9", "3", "Fixed", "1200", "916.3",
            "110", "Oman", "-1", "4", "Fixed", "600", "865.7",
            "111", "Pakistan", "-1", "4", "Fixed", "600", "865.7",
            "112", "Panama", "-2 RW", "50", "Hop", "500", "902.75",
            "113", "Paraguay", "-2 RW", "50", "Hop", "500", "902.75",
            "114", "Peru", "-2 RW", "24", "Hop", "500", "915.75",
            "115", "Philippines", "-2 RW", "50", "Hop", "250", "918.125",
            "116", "Poland", "-1", "4", "Fixed", "600", "865.7",
            "117", "Portugal", "-1", "4", "Fixed", "600", "865.7",
            "118", "Romania", "-1", "4", "Fixed", "600", "865.7",
            "119", "Russia1", "-1", "4", "Fixed", "600", "866.3",
            "120", "Russia3", "-9", "4", "Fixed", "1200", "915.6",
            "121", "Senegal", "-1", "4", "Fixed", "600", "865.7",
            "122", "Serbia", "-1", "4", "Fixed", "600", "865.7",
            "123", "Singapore1", "-1", "4", "Fixed", "600", "865.7",
            "124", "Singapore2", "-2 RW", "8", "Hop", "500", "920.75",
            "125", "Slovak1", "-1", "4", "Fixed", "600", "865.7",
            "126", "Slovak2", "-9", "3", "Fixed", "1200", "916.3",
            "127", "Slovenia1", "-1", "4", "Fixed", "600", "865.7",
            "128", "Solvenia2", "-9", "3", "Fixed", "1200", "916.3",
            "129", "SAfrica1", "-1", "4", "Fixed", "600", "865.7",
            "130", "SAfrica2", "-9", "7", "Fixed", "500", "915.7",
            "131", "Spain", "-1", "4", "Fixed", "600", "865.7",
            "132", "SriLanka", "-1", "4", "Fixed", "600", "865.7",
            "133", "Sudan", "-1", "4", "Fixed", "600", "865.7",
            "134", "Sweden1", "-1", "4", "Fixed", "600", "865.7",
            "135", "Sweden2", "-9", "3", "Fixed", "1200", "916.3",
            "136", "Switzerland1", "-1", "4", "Fixed", "600", "865.7",
            "137", "Switzerland2", "-9", "3", "Fixed", "1200", "916.3",
            "138", "Syria", "-1", "4", "Fixed", "600", "865.7",
            "139", "Taiwan1", "-4", "12", "Hop", "375", "922.875",
            "140", "Taiwan2", "-4", "12", "Hop", "375", "922.875",
            "141", "Tajikistan", "-1", "4", "Fixed", "600", "865.7",
            "142", "Tanzania", "-1", "4", "Fixed", "600", "865.7",
            "143", "Thailand", "-2 RW", "8", "Hop", "500", "920.75",
            "144", "Trinidad", "-2 RW", "50", "Hop", "500", "902.75",
            "145", "Tunisia", "-1", "4", "Fixed", "600", "865.7",
            "146", "Turkey", "-1", "4", "Fixed", "600", "865.7",
            "147", "Turkmenistan", "-1", "4", "Fixed", "600", "865.7",
            "148", "Uganda", "-1", "4", "Fixed", "600", "865.7",
            "149", "Ukraine", "-1", "4", "Fixed", "600", "865.7",
            "150", "UAE", "-1", "4", "Fixed", "600", "865.7",
            "151", "UK1", "-1", "4", "Fixed", "600", "865.7",
            "152", "UK2", "-9", "3", "Fixed", "1200", "916.3",
            "153", "USA", "-2", "50", "Hop", "500", "902.75",
            "154", "Uruguay", "-2 RW", "50", "Hop", "500", "902.75",
            "155", "Venezuela", "-2 RW", "50", "Hop", "500", "902.75",
            "156", "Vietnam1", "-1", "4", "Fixed", "600", "866.3",
            "157", "Vietnam2", "-7", "16", "Hop", "500", "918.75",
            "158", "Yemen", "-1", "4", "Fixed", "600", "865.7",
            "159", "Zimbabwe", "-1", "4", "Fixed", "600", "865.7",
            "160", "Vietnam3", "-7", "4", "Hop", "500", "920.75"
    };
*/
    int getCountryCode() {
        return rfidReaderChip.rx000OemSetting.getCountryCode();
    }
    int getFreqModifyCode() {
        return rfidReaderChip.rx000OemSetting.getFreqModifyCode();
    }
    public boolean getRfidOnStatus() {
        return csReaderConnector.rfidReader.getRfidOnStatus();
    }
    public boolean isRfidFailure() {
        if (csReaderConnector.rfidReader == null) return false;
        return csReaderConnector.rfidReader.isRfidFailure();
    }
    public void setReaderDefault() {
        setPowerLevel(300);
        setTagGroup(0, 0, 2);
        setPopulation(60);
        setInvAlgoNoSave(true);
        setBasicCurrentLinkProfile();
        String string = bluetoothGatt.getmBluetoothDevice().getAddress();
        string = string.replaceAll("[^a-zA-Z0-9]","");
        string = string.substring(string.length()-6, string.length());
        setBluetoothICFirmwareName("CS108Reader" + string);

        //getlibraryVersion()
        setCountryInList(countryInListDefault);
        setChannel(0);

        //getAntennaPower(0)
        //getPopulation()
        //getQuerySession()
        //getQueryTarget()
        setTagFocus(false);
        setFastId(false);
        //getInvAlgo()
        //\\getRetryCount()
        //getCurrentProfile() + "\n"));
        //\\getRxGain() + "\n"));

        //getBluetoothICFirmwareName() + "\n");
        setBatteryDisplaySetting(batteryDisplaySelectDefault);
        setRssiDisplaySetting(rssiDisplaySelectDefault);
        setTagDelay(tagDelaySettingDefault);
        setCycleDelay((long)0);
        setIntraPkDelay((byte)4);
        setDupDelay((byte)0);

        notificationConnector.setTriggerReporting(notificationConnector.triggerReportingDefault);
        notificationConnector.setTriggerReportingCount(notificationConnector.triggerReportingCountSettingDefault);
        setInventoryBeep(inventoryBeepDefault);
        setBeepCount(beepCountSettingDefault);
        setInventoryVibrate(inventoryVibrateDefault);
        setVibrateTime(vibrateTimeSettingDefault);
        setVibrateModeSetting(vibrateModeSelectDefault);
        setVibrateWindow(vibrateWindowSettingDefault);

        setSavingFormatSetting(savingFormatSelectDefault);
        setCsvColumnSelectSetting(csvColumnSelectDefault);
        setSaveFileEnable(saveFileEnableDefault);
        setSaveCloudEnable(saveCloudEnableDefault);
        setSaveNewCloudEnable(saveNewCloudEnableDefault);
        setSaveAllCloudEnable(saveAllCloudEnableDefault);
        setServerLocation(serverLocationDefault);
        setServerTimeout(serverTimeoutDefault);
        barcodeNewland.barcode2TriggerMode = barcodeNewland.barcode2TriggerModeDefault;

        setUserDebugEnable(bluetoothConnector.userDebugEnableDefault);
        preFilterData = null;
    }
    public String getMacVer() {
        return rfidReaderChip.rx000Setting.getMacVer();
    }
    public String getRadioSerial() {
        String strValue;
        strValue = getSerialNumber();
        if (strValue != null) {
            appendToLog("strValue length = " + strValue.length());
            if (strValue.length() > 13) strValue = strValue.substring(0, 13);
        } else appendToLog("BBB");
        return strValue;
    }
    public String getRadioBoardVersion() {
        String str = rfidReaderChip.rx000OemSetting.getSerialNumber();
        if (str != null) {
            if (str.length() == 16) {
                String strOut;
                if (str.substring(13, 14).matches("0")) strOut = str.substring(14, 15);
                else strOut = str.substring(13, 15);
                strOut += "." + str.substring(15);
                return strOut;
            }
        }
        return str;
    }
    public int getPortNumber() {
        if (bluetoothConnector.getCsModel() == 463) return 4;
        else return 1;
    }
    public int getAntennaSelect() {
        int iValue = 0;
        iValue = rfidReaderChip.rx000Setting.getAntennaSelect();
        appendToLog("AntennaSelect = " + iValue);
        return iValue;
    }
    public boolean setAntennaSelect(int number) {
        boolean bValue = false;
        bValue = rfidReaderChip.rx000Setting.setAntennaSelect(number);
        appendToLog("AntennaSelect = " + number + " returning " + bValue);
        return bValue;
    }
    public boolean getAntennaEnable() {
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getAntennaEnable();
        appendToLog("AntennaEnable = " + iValue);
        if (iValue > 0) return true;
        else return false;
    }
    public boolean setAntennaEnable(boolean enable) {
        int iEnable = 0;
        if (enable) iEnable = 1;
        boolean bValue = false;
        bValue = rfidReaderChip.rx000Setting.setAntennaEnable(iEnable);
        appendToLog("AntennaEnable = " + iEnable + " returning " + bValue);
        return bValue;
    }
    public long getAntennaDwell() {
        long lValue = 0;
        lValue = rfidReaderChip.rx000Setting.getAntennaDwell();
        appendToLog("AntennaDwell = " + lValue);
        return lValue;
    }
    public boolean setAntennaDwell(long antennaDwell) {
        boolean bValue = false;
        bValue =  rfidReaderChip.rx000Setting.setAntennaDwell(antennaDwell);
        if (false) appendToLog("AntennaDwell = " + antennaDwell + " returning " + bValue);
        return bValue;
    }
    public long getPwrlevel() {
        long lValue = 0;
        lValue = rfidReaderChip.rx000Setting.getAntennaPower(-1);
        return lValue;
    }
    long pwrlevelSetting;
    public boolean setPowerLevel(long pwrlevel) {
        pwrlevelSetting = pwrlevel;
        boolean bValue = false;
        bValue = rfidReaderChip.rx000Setting.setAntennaPower(pwrlevel);
        if (false) appendToLog("PowerLevel = " + pwrlevel + " returning " + bValue);
        return bValue;
    }
    public int getQueryTarget() {
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getAlgoAbFlip();
        if (iValue > 0) return 2;
        else {
            iValue = rfidReaderChip.rx000Setting.getQueryTarget();
            if (iValue > 0) return 1;
            return 0;
        }
    }
    public int getQuerySession() {
        return rfidReaderChip.rx000Setting.getQuerySession();
    }
    public int getQuerySelect() {
        return rfidReaderChip.rx000Setting.getQuerySelect();
    }
    public boolean setTagGroup(int sL, int session, int target1) {
        if (false) appendToLog("Hello6: invAlgo = " + rfidReaderChip.rx000Setting.getInvAlgo());
        if (false) appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = " + rfidReaderChip.rx000Setting.getInvAlgo());
        rfidReaderChip.rx000Setting.setAlgoSelect(rfidReaderChip.rx000Setting.getInvAlgo()); //Must not delete this line
        return rfidReaderChip.rx000Setting.setQueryTarget(target1, session, sL);
    }
    public int getTagFocus() {
        return csReaderConnector.rfidReader.getTagFocus();
    }
    public boolean setTagFocus(boolean tagFocusNew) {
        return csReaderConnector.rfidReader.setTagFocus(tagFocusNew);
    }
    public int getFastId() {
        return csReaderConnector.rfidReader.getFastId();
    }
    public boolean setFastId(boolean fastIdNew) {
        return csReaderConnector.rfidReader.setFastId(fastIdNew);
    }
    boolean invAlgoSetting = true;
    public boolean getInvAlgo() {
        if (false) appendToLog("invAlgoSetting = " + invAlgoSetting);
        return invAlgoSetting;
    }
    public boolean setInvAlgo(boolean dynamicAlgo) {
        invAlgoSetting = dynamicAlgo;
        if (false) appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = " + dynamicAlgo);
        return setInvAlgo1(dynamicAlgo);
    }
    public List<String> getProfileList() {
        return Arrays.asList(context.getResources().getStringArray(R.array.profile1_options));
    }
    public int getCurrentProfile() {
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getCurrentProfile();
        return iValue;
    }
    public boolean setBasicCurrentLinkProfile() {
        return setCurrentLinkProfile(1);
    }
    public boolean setCurrentLinkProfile(int profile) {
        if (profile == getCurrentProfile()) return true;
        boolean result;
        result = rfidReaderChip.rx000Setting.setCurrentProfile(profile);
        if (result) {
            rfidReaderChip.setPwrManagementMode(false);
            result = rfidReaderChip.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_UPDATELINKPROFILE);
        }
        if (result && profile == 3) {
            appendToLog("It is profile3");
            if (getTagDelay() < 2) result = setTagDelay((byte)2);
        }
        return result;
    }
    public void resetEnvironmentalRSSI() {
        rfidReaderChip.rx000EngSetting.resetRSSI();
    }
    public String getEnvironmentalRSSI() {
        appendToLog("Hello123: getEnvironmentalRSSI");
        rfidReaderChip.setPwrManagementMode(false);
        int iValue =  rfidReaderChip.rx000EngSetting.getwideRSSI();
        if (iValue < 0) return null;
        if (iValue > 255) return "Invalid data";
        double dValue = rfidReaderChip.decodeNarrowBandRSSI((byte)iValue);
        return String.format("%.2f dB", dValue);
    }
    public int getHighCompression() {
        return rfidReaderChip.rx000MbpSetting.getHighCompression();
    }
    public int getRflnaGain() {
        return rfidReaderChip.rx000MbpSetting.getRflnaGain();
    }
    public int getIflnaGain() {
        return rfidReaderChip.rx000MbpSetting.getIflnaGain();
    }
    public int getAgcGain() {
        return rfidReaderChip.rx000MbpSetting.getAgcGain();
    }
    public int getRxGain() {
        return rfidReaderChip.rx000MbpSetting.getRxGain();
    }
    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        return rfidReaderChip.rx000MbpSetting.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
    }
    public boolean setRxGain(int rxGain) {
        return rfidReaderChip.rx000MbpSetting.setRxGain(rxGain);
    }
    public int FreqChnCnt() {
        return csReaderConnector.rfidReader.FreqChnCnt(regionCode);
    }
/*
    int FreqChnCnt(RegionCodes regionCode) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
                return FCC_CHN_CNT;
            case PR:
                return PRTableOfFreq.length;
            case VZ:
                return VZ_CHN_CNT;
            case AU:
                return AUS_CHN_CNT;
            case BR1:
                return BR1_CHN_CNT;
            case BR2:
                return BR2_CHN_CNT;
            case BR3:
                return BR3_CHN_CNT;
            case BR4:
                return BR4_CHN_CNT;
            case BR5:
                return BR5_CHN_CNT;
            case HK:
            case SG:
            case TH:
            case VN:
                return HK_CHN_CNT;
            case VN1:
                return VN1_CHN_CNT;
            case VN2:
                return VN2_CHN_CNT;
            case VN3:
                return VN3_CHN_CNT;
            case BD:
                return BD_CHN_CNT;
            case TW:
                return TW_CHN_CNT;
            case MY:
                return MYS_CHN_CNT;
            case ZA:
                return ZA_CHN_CNT;
            case ID:
                return ID_CHN_CNT;
            case IL:
                return IL_CHN_CNT;
            case IL2019RW:
                return IL2019RW_CHN_CNT;
            case PH:
                return PH_CHN_CNT;
            case NZ:
                return NZ_CHN_CNT;
            case CN:
                return CN_CHN_CNT;

            case UH1:
                return UH1_CHN_CNT;
            case UH2:
                return UH2_CHN_CNT;
            case LH:
                return LH_CHN_CNT;
            case LH1:
                return LH1_CHN_CNT;
            case LH2:
                return LH2_CHN_CNT;

            case ETSI:
                return ETSI_CHN_CNT;
            case IN:
                return IDA_CHN_CNT;
            case KR:
                return KR_CHN_CNT;
            case KR2017RW:
                return KR2017RW_CHN_CNT;
            case JP:
                return JPN2012_CHN_CNT;
            case JP6:
                return JPN2012A_CHN_CNT;
            case ETSIUPPERBAND:
                return ETSIUPPERBAND_CHN_CNT;

            default:
                return 0;
        }
    }
*/
    public double getLogicalChannel2PhysicalFreq(int channel) {
        getCountryList();             //  used to set up possibly regionCode
        int TotalCnt = csReaderConnector.rfidReader.FreqChnCnt(regionCode);
        int[] freqIndex = csReaderConnector.rfidReader.FreqIndex(regionCode);
        double[] freqTable = csReaderConnector.rfidReader.GetAvailableFrequencyTable(regionCode);
        if (freqIndex.length != TotalCnt || freqTable.length != TotalCnt || channel >= TotalCnt)   return -1;
        return freqTable[freqIndex[channel]];
    }
    public byte getTagDelay() {
        return tagDelaySetting;
    }
    public boolean setTagDelay(byte tagDelay) {
        tagDelaySetting = tagDelay;
        return true;
    }
    public byte getIntraPkDelay() {
        return rfidReaderChip.rx000Setting.getIntraPacketDelay();
    }
    public boolean setIntraPkDelay(byte intraPkDelay) {
        return rfidReaderChip.rx000Setting.setIntraPacketDelay(intraPkDelay);
    }
    public byte getDupDelay() {
        return rfidReaderChip.rx000Setting.getDupElimRollWindow();
    }
    public boolean setDupDelay(byte dupElim) {
        return rfidReaderChip.rx000Setting.setDupElimRollWindow(dupElim);
    }
    public long getCycleDelay() {
        cycleDelaySetting = rfidReaderChip.rx000Setting.getCycleDelay();
        return cycleDelaySetting;
    }
    public boolean setCycleDelay(long cycleDelay) {
        cycleDelaySetting = cycleDelay;
        return rfidReaderChip.rx000Setting.setCycleDelay(cycleDelay);
    }
    public void getAuthenticateReplyLength() {
        rfidReaderChip.rx000Setting.getAuthenticateReplyLength();
    }
    public boolean setTamConfiguration(boolean header, String matchData) {
        appendToLog("header = " + header + ", matchData.length = " + matchData.length() + ", matchData = " + matchData);
        if (matchData.length() != 12) return false;

        boolean retValue = false; String preChallenge = matchData.substring(0, 2);
        int iValue = Integer.parseInt(preChallenge, 16);
        iValue &= 0x07;
        if (header) iValue |= 0x04;
        else iValue &= ~0x04;
        preChallenge = String.format("%02X", iValue);
        matchData = preChallenge + matchData.substring(2);
        appendToLog("new matchData = " + matchData);

        retValue = setAuthMatchData(matchData);
        appendToLog("setAuthMatchData returns " + retValue);
        if (retValue) {
            retValue = rfidReaderChip.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 1, matchData.length() * 4);
            appendToLog("setHST_AUTHENTICATE_CFG returns " + retValue);
        }
        return retValue;
    }
    public boolean setTam1Configuration(int keyId, String matchData) {
        appendToLog("keyId = " + keyId + ", matchData.length = " + matchData.length());
        if (keyId > 255) return false;
        if (matchData.length() != 20) return false;

        boolean retValue = false; String preChallenge = "00";
        preChallenge += String.format("%02X", keyId);
        matchData = preChallenge + matchData;
        retValue = setAuthMatchData(matchData);
        appendToLog("setAuthMatchData returns " + retValue);
        if (retValue) {
            retValue = rfidReaderChip.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 0, matchData.length() * 4);
            appendToLog("setHST_AUTHENTICATE_CFG returns " + retValue);
        }
        return retValue;
    }
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        if (keyId > 255) return false;
        if (matchData.length() != 20) return false;
        if (profile >15) return false;
        if (offset > 0xFFF) return false;
        if (blockId > 15) return false;
        if (protMode > 15) return false;

        boolean retValue = false; String preChallenge = "20"; String postChallenge;
        preChallenge += String.format("%02X", keyId);
        postChallenge = String.valueOf(profile);
        postChallenge += String.format("%03X", offset);
        postChallenge += String.valueOf(blockId);
        postChallenge += String.valueOf(protMode);
        matchData = preChallenge + matchData + postChallenge;
        retValue = setAuthMatchData(matchData);
        if (retValue) {
            retValue = rfidReaderChip.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 0, matchData.length() * 4);
        }
        return retValue;
    }
    public int getUntraceableEpcLength() {
        return rfidReaderChip.rx000Setting.getUntraceableEpcLength();
    }
    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        return rfidReaderChip.rx000Setting.setHST_UNTRACEABLE_CFG(bHideRange ? 2 : 0, bHideUser, iHideTid, ishowEpcSize, bHideEpc, false);
    }
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        return rfidReaderChip.rx000Setting.setHST_UNTRACEABLE_CFG(range, user, tid, epcLength, epc, uxpc);
    }
    public boolean setAuthenticateConfiguration() {
        appendToLog("setAuthenuateConfiguration0 Started");
        boolean bValue = rfidReaderChip.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 1, 48); //setAuthenticateConfig((48 << 10) | (1 << 2) | 0x03);
        appendToLog("setAuthenuateConfiguration 1: bValue = " + (bValue ? "true" : "false"));
        if (bValue) {
            bValue = rfidReaderChip.rx000Setting.setAuthMatchData("049CA53E55EA"); //setAuthenticateMessage(new byte[] { 0x04, (byte)0x9C, (byte)0xA5, 0x3E, 0x55, (byte)0xEA } );
            appendToLog("setAuthenuateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
        }
        /*if (bValue) {
            bValue = mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000Setting.setAuthenticateResponseLen(16 * 8);
            appendToLog("setAuthenuateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
        }*/
        return false; //bValue;
    }
    public int getRetryCount() {
        int algoSelect;
        algoSelect = rfidReaderChip.rx000Setting.getAlgoSelect();
        if (algoSelect == 0 || algoSelect == 3) {
            return rfidReaderChip.rx000Setting.getAlgoRetry(algoSelect);
        }
        else return -1;
    }
    public boolean setRetryCount(int retryCount) {
        return rfidReaderChip.rx000Setting.setAlgoRetry(retryCount);
    }
    public int getInvSelectIndex() {
        return rfidReaderChip.rx000Setting.getInvSelectIndex();
    }
    public boolean getSelectEnable() {
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getSelectEnable();
        if (iValue < 0) return false;
        return iValue != 0 ? true : false;
    }
    public int getSelectTarget() {
        return rfidReaderChip.rx000Setting.getSelectTarget();
    }
    public int getSelectAction() {
        return rfidReaderChip.rx000Setting.getSelectAction();
    }
    public int getSelectMaskBank() {
        return rfidReaderChip.rx000Setting.getSelectMaskBank();
    }
    public int getSelectMaskOffset() {
        return rfidReaderChip.rx000Setting.getSelectMaskOffset();
    }
    public String getSelectMaskData() {
        int iValue1;
        iValue1 = rfidReaderChip.rx000Setting.getSelectMaskLength();
        if (iValue1 < 0)    return null;
        String strValue;
        strValue = rfidReaderChip.rx000Setting.getSelectMaskData();
        if (strValue == null) return null;
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1)  strLength++;
        if (false) appendToLog("Mask data = iValue1 = " + iValue1 + ", strValue = " + strValue + ", strLength = " + strLength);
        if (strValue.length() < strLength) strLength = strValue.length();
        return strValue.substring(0, strLength);
    }
    public boolean setInvSelectIndex(int invSelect) {
        return rfidReaderChip.rx000Setting.setInvSelectIndex(invSelect);
    }
    public boolean setSelectCriteriaDisable(int index) {
        rfidReaderChip.rx000Setting.setQuerySelect(0);
        appendToLog("cs108Library4A: setSelectCriteria Disable with index = " + index);
        boolean bValue = false;
        if (index < 0) {
            for (int i = 0; i < 3; i++) {
                bValue = setSelectCriteria(i, false, 0, 0, 0, 0, 0, "");
                if (bValue == false) {
                    appendToLog("cs108Library4A: setSelectCriteria disable break as false");
                    break;
                }
            }
        } else {
            appendToLog("setSelectCriteria loop ends");
            bValue = setSelectCriteria(index, false, 0, 0, 0, 0, 0, "");
        }
        return bValue;
    }
    int findFirstEmptySelect() {
        int iValue = -1;
        for (int i = 0; i < 3; i++) {
            rfidReaderChip.rx000Setting.setInvSelectIndex(i);
            if (rfidReaderChip.rx000Setting.getSelectEnable() == 0) {
                iValue = i;
                appendToLog("cs710Library4A: setSelectCriteria 1 with New index = " + iValue);
                break;
            }
        }
        return iValue;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        appendToLog("cs108Library4A: setSelectCriteria 1 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask + ", maskbit = " + maskbit);
        if (index == 0) preFilterData = new PreFilterData(enable, target, action, bank, offset, mask, maskbit);
        if (index < 0) index = findFirstEmptySelect();
        if (index < 0) {
            appendToLog("cs710Library4A: no index is available !!!"); return false;
        }

        int maskblen = mask.length() * 4;
        String maskHex = ""; int iHex = 0;
        if (maskbit) {
            for (int i = 0; i < mask.length(); i++) {
                iHex <<= 1;
                if (mask.substring(i, i+1).matches("0")) iHex &= 0xFE;
                else if (mask.substring(i, i+1).matches("1"))  iHex |= 0x01;
                else return false;
                if ((i+1) % 4 == 0) maskHex += String.format("%1X", iHex & 0x0F);
            }
            int iBitRemain = mask.length() % 4;
            if (iBitRemain != 0) {
                iHex <<= (4 - iBitRemain);
                maskHex += String.format("%1X", iHex & 0x0F);
            }
            maskblen = mask.length();
            mask = maskHex;
        }
        return setSelectCriteria3(index, enable, target, action, 0, bank, offset, mask, maskblen);
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        appendToLog("cs108Library4A: setSelectCriteria 2 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask);
        if (index < 0) index = findFirstEmptySelect();
        if (index < 0) {
            appendToLog("cs710Library4A: no index is available !!!"); return false;
        }

        if (index == 0) preFilterData = new PreFilterData(enable, target, action, bank, offset, mask, false);
        if (mask.length() > 64) mask = mask.substring(0, 64);
        if (index == 0) preMatchData = new PreMatchData(enable, target, action, bank, offset, mask, mask.length() * 4, rfidReaderChip.rx000Setting.getQuerySelect(), getPwrlevel(), getInvAlgo(), getQValue());
        boolean result = true;
        if (index != rfidReaderChip.rx000Setting.getInvSelectIndex()) result = rfidReaderChip.rx000Setting.setInvSelectIndex(index);
        if (rfidReaderChip.rx000Setting.getSelectEnable() == 0 && enable == false) {
            appendToLog("cs108Library4A: setSelectCriteria 2: no need to set as when index = " + index + ", getSelectEnable() = " + rfidReaderChip.rx000Setting.getSelectEnable() + ", new enable = " + enable);
            result = true;
        } else {
            if (result) result = rfidReaderChip.rx000Setting.setSelectEnable(enable ? 1 : 0, target, action, delay);
            if (result) result = rfidReaderChip.rx000Setting.setSelectMaskBank(bank);
            if (result) result = rfidReaderChip.rx000Setting.setSelectMaskOffset(offset);
            if (mask == null)   return false; if (result) result = rfidReaderChip.rx000Setting.setSelectMaskLength(mask.length() * 4);
            if (result) result = rfidReaderChip.rx000Setting.setSelectMaskData(mask);
            if (result) {
                if (enable) {
                    rfidReaderChip.rx000Setting.setTagSelect(1);
                    rfidReaderChip.rx000Setting.setQuerySelect(3);
                } else {
                    rfidReaderChip.rx000Setting.setTagSelect(0);
                    rfidReaderChip.rx000Setting.setQuerySelect(0);
                }
            }
        }
        return result;
    }
    public boolean getRssiFilterEnable() {
        int iValue = rfidReaderChip.rx000Setting.getRssiFilterType();
        if (iValue < 0) return false;
        iValue &= 0xF;
        return (iValue > 0 ? true : false);
    }
    public int getRssiFilterType() {
        int iValue = rfidReaderChip.rx000Setting.getRssiFilterType();
        if (iValue < 0) return 0;
        iValue &= 0xF;
        if (iValue < 2) return 0;
        return iValue - 1;
    }
    public int getRssiFilterOption() {
        int iValue = rfidReaderChip.rx000Setting.getRssiFilterOption();
        if (iValue < 0) return 0;
        iValue &= 0xF;
        return iValue;
    }
    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        int iValue = 0;
        if (enable == false) iValue = 0;
        else iValue = rssiFilterType + 1;
        return rfidReaderChip.rx000Setting.setHST_INV_RSSI_FILTERING_CONFIG(iValue, rssiFilterOption);
    }
    public double getRssiFilterThreshold1() {
        int iValue = rfidReaderChip.rx000Setting.getRssiFilterThreshold1();
        appendToLog("iValue = " + iValue);
        byte byteValue = (byte)(iValue & 0xFF);
        appendToLog("byteValue = " + byteValue);
        double dValue = rfidReaderChip.decodeNarrowBandRSSI(byteValue);
        appendToLog("dValue = " + dValue);
        return dValue;
    }
    public double getRssiFilterThreshold2() {
        int iValue = rfidReaderChip.rx000Setting.getRssiFilterThreshold2();
        appendToLog("iValue = " + iValue);
        byte byteValue = (byte)(iValue & 0xFF);
        double dValue = rfidReaderChip.decodeNarrowBandRSSI(byteValue);
        return dValue;
    }
    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        appendToLog("rssiFilterThreshold = " + rssiFilterThreshold1 + ", " + rssiFilterThreshold2);
        return rfidReaderChip.rx000Setting.setHST_INV_RSSI_FILTERING_THRESHOLD(rfidReaderChip.encodeNarrowBandRSSI(rssiFilterThreshold1), rfidReaderChip.encodeNarrowBandRSSI(rssiFilterThreshold2));
    }
    public long getRssiFilterCount() {
        return rfidReaderChip.rx000Setting.getRssiFilterCount();
    }
    public boolean setRssiFilterCount(long rssiFilterCount) {
        appendToLog("rssiFilterCount = " + rssiFilterCount);
        return rfidReaderChip.rx000Setting.setHST_INV_RSSI_FILTERING_COUNT(rssiFilterCount);
    }
    public boolean getInvMatchEnable() {
        return rfidReaderChip.rx000Setting.getInvMatchEnable() > 0 ? true : false;
    }
    public boolean getInvMatchType() {
        return rfidReaderChip.rx000Setting.getInvMatchType() > 0 ? true : false;
    }
    public int getInvMatchOffset() {
        return rfidReaderChip.rx000Setting.getInvMatchOffset();
    }
    public String getInvMatchData() {
        int iValue1 = rfidReaderChip.rx000Setting.getInvMatchLength();
        if (iValue1 < 0)    return null;
        String strValue = rfidReaderChip.rx000Setting.getInvMatchData();
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1)  strLength++;
        return strValue.substring(0, strLength);
    }
    class PostMatchData {
        boolean enable; boolean target; int offset; String mask; long pwrlevel; boolean invAlgo; int qValue;
        PostMatchData(boolean enable, boolean target, int offset, String mask, int antennaCycle, long pwrlevel, boolean invAlgo, int qValue) {
            this.enable = enable;
            this.target = target;
            this.offset = offset;
            this.mask = mask;
            this.pwrlevel = pwrlevel;
            this.invAlgo = invAlgo;
            this.qValue = qValue;
        }
    }
    PostMatchData postMatchData;
    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        postMatchData = new PostMatchData(enable, target, offset, mask, getAntennaCycle(), getPwrlevel(), getInvAlgo(), getQValue());
        boolean result = rfidReaderChip.rx000Setting.setInvMatchEnable(enable ? 1 : 0, target ? 1 : 0, mask == null ? -1 : mask.length() * 4, offset);
        if (result && mask != null) result = rfidReaderChip.rx000Setting.setInvMatchData(mask);
        return result;
    }
    public int mrfidToWriteSize() {
        return csReaderConnector.rfidReader.mRfidToWrite.size();
    }
    public void mrfidToWritePrint() {
        for (int i = 0; i < csReaderConnector.rfidReader.mRfidToWrite.size(); i++) {
            appendToLog(byteArrayToString(csReaderConnector.rfidReader.mRfidToWrite.get(i).dataValues));
        }
    }
    public long getTagRate() {
        return -1;
    }
    public boolean startOperation(RfidReaderChipData.OperationTypes operationTypes) {
        boolean retValue = false;
        switch (operationTypes) {
            case TAG_INVENTORY_COMPACT:
            case TAG_INVENTORY:
            case TAG_SEARCHING:
                if (operationTypes == RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT) {
                    if (false && csReaderConnector.rfidReader.tagFocus >= 1) {
                        setTagGroup(-1, 1, 0);  //Set Session S1, Target A
                        rfidReaderChip.rx000Setting.setTagDelay(0);
                        rfidReaderChip.rx000Setting.setAntennaDwell(2000);
                    }
                    rfidReaderChip.rx000Setting.setInvModeCompact(true);
                }
                else {
                    rfidReaderChip.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting);
                    rfidReaderChip.rx000Setting.setCycleDelay(cycleDelaySetting);
                    rfidReaderChip.rx000Setting.setInvModeCompact(false);
                }
                notificationConnector.getAutoRFIDAbort(); notificationConnector.setAutoRFIDAbort(true); notificationConnector.getAutoRFIDAbort();
                rfidReaderChip.setPwrManagementMode(false);
                appendToLog("going to sendHostRegRequestHST_CMD(Cs108Library4A.HostCommands.CMD_18K6CINV)");

                retValue = true;
                RfidReaderChipData.HostCommands hostCommand = RfidReaderChipData.HostCommands.CMD_18K6CINV;
                retValue = rfidReaderChip.sendHostRegRequestHST_CMD(hostCommand);
                break;
        }
        return retValue;
    }
    public boolean abortOperation() {
        boolean bRetValue = false;
        if (rfidReaderChip != null) {
            bRetValue = rfidReaderChip.sendControlCommand(RfidReaderChipR2000.ControlCommands.ABORT);
        }
        csReaderConnector.rfidReader.setInventoring(false);
        return bRetValue;
    }
    public void restoreAfterTagSelect() {
        appendToLog("Start");
        setSelectCriteriaDisable(0); setSelectCriteriaDisable(1); setSelectCriteriaDisable(2);
        if (true) loadSetting1File();
        else if (DEBUG) appendToLog("postMatchDataChanged = " + postMatchDataChanged + ",  preMatchDataChanged = " + preMatchDataChanged + ", macVersion = " + getMacVer());
        setRx000AccessPassword("00000000");
        if (checkHostProcessorVersion(getMacVer(), 2, 6, 8)) {
            rfidReaderChip.rx000Setting.setMatchRep(0);
            rfidReaderChip.rx000Setting.setTagDelay(tagDelaySetting);
            rfidReaderChip.rx000Setting.setCycleDelay(cycleDelaySetting);
            rfidReaderChip.rx000Setting.setInvModeCompact(true);
        }
        if (postMatchDataChanged) {
            postMatchDataChanged = false;
            setPostMatchCriteria(postMatchDataOld.enable, postMatchDataOld.target, postMatchDataOld.offset, postMatchDataOld.mask);
            appendToLog("PowerLevel");
            setPowerLevel(postMatchDataOld.pwrlevel);
            appendToLog("writeBleStreamOut: invAlgo = " + postMatchDataOld.invAlgo); setInvAlgo1(postMatchDataOld.invAlgo);
            setQValue1(postMatchDataOld.qValue);
        }
        if (false && preMatchDataChanged) {
            preMatchDataChanged = false; appendToLog("preMatchDataChanged is reset");
            rfidReaderChip.rx000Setting.setQuerySelect(preMatchDataOld.querySelect);
            appendToLog("PowerLevel");
            setPowerLevel(preMatchDataOld.pwrlevel);
            appendToLog("writeBleStreamOut: invAlgo = " + preMatchDataOld.invAlgo); setInvAlgo1(preMatchDataOld.invAlgo);
            setQValue1(preMatchDataOld.qValue);
            setSelectCriteria3(0, preMatchDataOld.enable, preMatchDataOld.target, preMatchDataOld.action, 0, preMatchDataOld.bank, preMatchDataOld.offset, preMatchDataOld.mask, preMatchDataOld.maskblen);
        }
    }
    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        if (pwrlevel < 0) pwrlevel = pwrlevelSetting;
        appendToLog("cs108Library4A: setSelectCriteria setSelectedByTID strTagId = " + strTagId + ", pwrlevel = " + pwrlevel);
        return setSelectedTag1(strTagId, 2, 0, 0, pwrlevel, 0, 0);
    }
    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        boolean isValid = false;
        appendToLog("cs108Library4A: setSelectCriteria strTagId = " + strTagId + ", selectBank = " + selectBank);
        if (selectBank < 0 || selectBank > 3) return false;
        int selectOffset = (selectBank == 1 ? 32 : 0);
        isValid = setSelectCriteriaDisable(-1);
        if (isValid) isValid = setSelectedTag1(strTagId, selectBank, selectOffset, 0, pwrlevel, 0, 0);
        return isValid;
    }
    public boolean setSelectedTag(String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        appendToLog("cs108LibraryA: setSelectCriteria strTagId = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        return setSelectedTag1(selectMask, selectBank, selectOffset, 0, pwrlevel, qValue, matchRep);
    }
    public boolean setMatchRep(int matchRep) {
        return rfidReaderChip.rx000Setting.setMatchRep(matchRep);
    }
    public String[] getCountryList() {
        String[] strCountryList = null;
        RegionCodes[] regionList = getRegionList();
        if (regionList != null) {
            strCountryList = new String[regionList.length];
            for (int i = 0; i < regionList.length; i++) {
                strCountryList[i] = csReaderConnector.rfidReader.regionCode2StringArray(regionList[i]);
            }
        }
        return strCountryList;
    }
    int countryInList = -1, countryInListDefault = -1;
    public int getCountryNumberInList() {
        return countryInList;
    }
    public boolean setCountryInList(int countryInList) {
        boolean DEBUG = true;
        if (DEBUG) appendToLog("this.countryInList =" + this.countryInList + ", countryInList = " + countryInList);
        if (this.countryInList == countryInList)    return true;

        RegionCodes[] regionList = getRegionList();
        if (DEBUG) appendToLog("regionList length =" + (regionList == null ? "NULL" : regionList.length));
        if (regionList == null)     return false;
        if (countryInList < 0 || countryInList >= regionList.length)    return false;

        int[] freqDataTableOld = csReaderConnector.rfidReader.FreqTable(regionCode);
        if (DEBUG) appendToLog("regionCode =" + regionCode + ", freqDataTableOld length = " + (freqDataTableOld == null ? "NULL" : freqDataTableOld.length));
        if (freqDataTableOld == null) return false;

        RegionCodes regionCodeNew = regionList[countryInList];
        final int[] freqDataTable = csReaderConnector.rfidReader.FreqTable(regionCodeNew);
        if (DEBUG) appendToLog("regionCodeNew =" + regionCodeNew + ", freqDataTable length = " + (freqDataTable == null ? "NULL" : freqDataTable.length));
        if (freqDataTable == null) return false;

        this.countryInList = countryInList; appendToLog("saveSetting2File testpoint 4");
        regionCode = regionCodeNew;
        if (DEBUG) appendToLog("getChannel =" + getChannel() + ", FreqChnCnt = " + FreqChnCnt());
        appendToLog("X channel = ");
        if (getChannel() >= FreqChnCnt())   setChannel(0);
        switch (getCountryCode()) {
            case 1:
            case 5:
            case 8:
            case 9:
                break;
            case 2:
                if (false && regionCode == regionCodeDefault4Country2) {
                    if (DEBUG) appendToLog("FCC Region is set");
                    toggledConnection = false;
                    mHandler.removeCallbacks(runnableToggleConnection);
                    mHandler.postDelayed(runnableToggleConnection, 500);
                    return true;
                }
            default:    //  2, 4, 7
                if (freqDataTable.length == freqDataTableOld.length) {
                    int i = 0;
                    for (; i < freqDataTable.length; i++) {
                        if (freqDataTable[i] != freqDataTableOld[i])    break;
                    }
                    if (i == freqDataTable.length)  {
                        if (DEBUG) appendToLog("Break as same freqDataTable");
                        break;
                    }
                }
                if (DEBUG) appendToLog("Finish as different freqDataTable");
                int k = 0;
                for (; k < freqDataTable.length; k++) {
                    if (DEBUG) appendToLog("Setting channel = " + k);
                    rfidReaderChip.rx000Setting.setFreqChannelSelect(k);
                    rfidReaderChip.rx000Setting.setFreqChannelConfig(true);
                    rfidReaderChip.rx000Setting.setFreqPllMultiplier(freqDataTable[k]);
                }
                for (; k < 50; k++) {
                    if (DEBUG) appendToLog("Resetting channel = " + k);
                    rfidReaderChip.rx000Setting.setFreqChannelSelect(k);
                    rfidReaderChip.rx000Setting.setFreqChannelConfig(false);
                }
                break;
        }
        if (DEBUG) appendToLog("New regionCode = " + regionCode.toString() + ", channel = " + getChannel() + ", FreqChnCnt = " + FreqChnCnt());
        return true;
    }
    int channelOrderType; // 0 for frequency hopping / agile, 1 for fixed frequencey
    public boolean getChannelHoppingStatus() {
        appendToLog("countryCode with channelOrderType = " + channelOrderType);
        if (channelOrderType < 0) {
            if (getChannelHoppingDefault()) channelOrderType = 0;
            else channelOrderType = 1;
        }
        return (channelOrderType == 0 ? true : false);
    }
    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        if (this.channelOrderType != (channelOrderHopping ? 0 : 1)) {
            boolean result = true;
            if (getChannelHoppingDefault() == false) {
                result = rfidReaderChip.rx000Setting.setAntennaFreqAgile(channelOrderHopping ? 1 : 0);
            }
            int freqcnt = FreqChnCnt(); appendToLog("FrequencyA Count = " + freqcnt);
            int channel = getChannel(); appendToLog(" FrequencyA Channel = " + channel);
            for (int i = 0; i < freqcnt; i++) {
                if (result == true) rfidReaderChip.rx000Setting.setFreqChannelSelect(i);
                if (result == true) rfidReaderChip.rx000Setting.setFreqChannelConfig(channelOrderHopping);
            }
            if (result == true) rfidReaderChip.rx000Setting.setFreqChannelSelect(channel);
            if (result == true) rfidReaderChip.rx000Setting.setFreqChannelConfig(true);
            appendToLog(" FrequencyA: end of setting");

            this.channelOrderType = (channelOrderHopping ? 0 : 1);
            appendToLog("setChannelHoppingStatus: channelOrderType = " + channelOrderType);
        }
        return true;
    }
    public String[] getChannelFrequencyList() {
        boolean DEBUG = true;
        appendToLog("regionCode is " + regionCode.toString());
        double[] table = csReaderConnector.rfidReader.GetAvailableFrequencyTable(regionCode);
        appendToLog("table length = " + table.length);
        for (int i = 0; i < table.length; i++) appendToLog("table[" + i + "] = " + table[i]);
        String[] strChannnelFrequencyList = new String[table.length];
        for (int i = 0; i < table.length ; i++) {
            strChannnelFrequencyList[i] = String.format("%.2f MHz", table[i]);
            appendToLog("strChannnelFrequencyList[" + i + "] = " + strChannnelFrequencyList[i]);
        }
        return strChannnelFrequencyList;
    }
    public int getChannel() {
        int channel = -1;
        appendToLog("loadSetting1File: getChannel");
        if (rfidReaderChip.rx000Setting.getFreqChannelConfig() != 0) {
            channel = rfidReaderChip.rx000Setting.getFreqChannelSelect();
            appendToLog("loadSetting1File: getting channel = " + channel);
        }
        if (getChannelHoppingStatus()) {
            appendToLog("loadSetting1File: got hoppingStatus: channel = " + channel);
            channel = 0;
        }
        appendToLog("loadSetting1File: channel = " + channel);
        return channel;
    }
    public boolean setChannel(int channelSelect) {
        boolean result = true;
        appendToLog("loadSetting1File: channelSelect = " + channelSelect);
        if (result == true)    result = rfidReaderChip.rx000Setting.setFreqChannelConfig(false);
        if (result == true)    result = rfidReaderChip.rx000Setting.setFreqChannelSelect(channelSelect);
        if (result == true)    result = rfidReaderChip.rx000Setting.setFreqChannelConfig(true);
        return result;
    }
    public byte getPopulation2Q(int population) {
        double dValue = 1 + log10(population * 2) / log10(2);
        if (dValue < 0) dValue = 0;
        if(dValue > 15) dValue = 15;
        byte iValue = (byte) dValue;
        if (false) appendToLog("getPopulation2Q(" + population + "): log dValue = " + dValue + ", iValue = " + iValue);
        return iValue;
    }
    int population = 30;
    public int getPopulation() {
        return population;
    }
    public boolean setPopulation(int population) {
        if (false) appendToLog("Stream population = " + population);
        byte iValue = getPopulation2Q(population);
        this.population = population;
        return setQValue(iValue);
    }
    byte qValueSetting = -1;
    public byte getQValue() {
        return qValueSetting;
    }
    public boolean setQValue(byte byteValue) {
        qValueSetting = byteValue;
        if (false) appendToLog("Stream population qValue = " + qValueSetting);
        return setQValue1(byteValue);
    }
    int getQValue1() {
        return rfidReaderChip.rx000Setting.getAlgoStartQ();
    }
    boolean setQValue1(int iValue) {
        boolean result = true;
        {
            int invAlgo = rfidReaderChip.rx000Setting.getInvAlgo();
            if (iValue != rfidReaderChip.rx000Setting.getAlgoStartQ(invAlgo)) {
                if (false) appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = " + invAlgo);
                result = rfidReaderChip.rx000Setting.setAlgoSelect(invAlgo);
            }
        }
        if (result) {
            result = rfidReaderChip.rx000Setting.setAlgoStartQ(iValue);
        }
        return result;
    }
    public RfidReaderChipData.Rx000pkgData onRFIDEvent() {
        RfidReaderChipData.Rx000pkgData rx000pkgData = null;
        //if (mrfidToWriteSize() != 0) mRfidReaderChip.mRx000ToRead.clear();
        if (rfidReaderChip.bRx000ToReading == false && rfidReaderChip.mRx000ToRead.size() != 0) {
            rfidReaderChip.bRx000ToReading = true;
            int index = 0;
            try {
                rx000pkgData = rfidReaderChip.mRx000ToRead.get(index);
                if (false) appendToLog("rx000pkgData.type = " + rx000pkgData.responseType.toString());
                rfidReaderChip.mRx000ToRead.remove(index); //appendToLog("mRx000ToRead.remove");
            } catch (Exception ex) {
                rx000pkgData = null;
            }
            rfidReaderChip.bRx000ToReading = false;
        }
        return rx000pkgData;
    }
    public String getModelNumber() {
        int iCountryCode = getCountryCode();
        String strCountryCode = "";
        String strModelName = getModelName(); //"CS108";
        appendToLog("iCountryCode = " + iCountryCode + ", strModelNumber = " + strModelName);
        if (strModelName != null && strModelName.length() != 0) {
            if (iCountryCode > 0) strCountryCode = strModelName + "-" + String.valueOf(iCountryCode) + " " + rfidReaderChip.rx000OemSetting.getSpecialCountryVersion();
            else strCountryCode = strModelName;
        }
        return strCountryCode;
    }
    public boolean setRx000KillPassword(String password) {
        return rfidReaderChip.rx000Setting.setRx000KillPassword(password);
    }
    public boolean setRx000AccessPassword(String password) {
        return rfidReaderChip.rx000Setting.setRx000AccessPassword(password);
    }
    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        return rfidReaderChip.rx000Setting.setAccessRetry(accessVerfiy, accessRetry);
    }
    public boolean setInvModeCompact(boolean invModeCompact) {
        return rfidReaderChip.rx000Setting.setInvModeCompact(invModeCompact);
    }
    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        return rfidReaderChip.rx000Setting.setAccessLockAction(accessLockAction, accessLockMask);
    }
    public boolean setAccessBank(int accessBank) {
        return rfidReaderChip.rx000Setting.setAccessBank(accessBank);
    }
    public boolean setAccessBank(int accessBank, int accessBank2) {
        return rfidReaderChip.rx000Setting.setAccessBank(accessBank, accessBank2);
    }
    public boolean setAccessOffset(int accessOffset) {
        return rfidReaderChip.rx000Setting.setAccessOffset(accessOffset);
    }
    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        return rfidReaderChip.rx000Setting.setAccessOffset(accessOffset, accessOffset2);
    }
    public boolean setAccessCount(int accessCount) {
        return rfidReaderChip.rx000Setting.setAccessCount(accessCount);
    }
    public boolean setAccessCount(int accessCount, int accessCount2) {
        return rfidReaderChip.rx000Setting.setAccessCount(accessCount, accessCount2);
    }
    public boolean setAccessWriteData(String dataInput) {
        return rfidReaderChip.rx000Setting.setAccessWriteData(dataInput);
    }
    public boolean setTagRead(int tagRead) {
        appendToLog("012 tagRead is " + tagRead);
        return rfidReaderChip.rx000Setting.setTagRead(tagRead);
    }
    public boolean setInvBrandId(boolean invBrandId) {
        return rfidReaderChip.rx000Setting.setInvBrandId(invBrandId);
    }
    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        rfidReaderChip.setPwrManagementMode(false);
        return rfidReaderChip.sendHostRegRequestHST_CMD(hostCommand);
    }
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        if (rfidReaderChip == null) {
            appendToLog("rfidReaderChip is null");
            return false;
        }
        return rfidReaderChip.setPwrManagementMode(bLowPowerStandby);
    }
    public void macWrite(int address, long value) {
        csReaderConnector.rfidReader.macWrite(address, value);
    }
    public void set_fdCmdCfg(int value) {
        csReaderConnector.rfidReader.set_fdRegAddr(value);
    }
    public void set_fdRegAddr(int addr) {
        csReaderConnector.rfidReader.set_fdRegAddr(addr);
    }
    public void set_fdWrite(int addr, long value) {
        csReaderConnector.rfidReader.set_fdWrite(addr, value);
    }
    public void set_fdPwd(int value) {
        csReaderConnector.rfidReader.set_fdPwd(value);
    }
    public void set_fdBlockAddr4GetTemperature(int addr) {
        csReaderConnector.rfidReader.set_fdBlockAddr4GetTemperature(addr);
    }
    public void set_fdReadMem(int addr, long len) {
        csReaderConnector.rfidReader.set_fdReadMem(addr, len);
    }
    public void set_fdWriteMem(int addr, int len, long value) {
        csReaderConnector.rfidReader.set_fdWriteMem(addr, len, value);
    }
    public void setImpinJExtension(boolean tagFocus, boolean fastId) {
        csReaderConnector.rfidReader.setImpinJExtension(tagFocus, fastId);
    }

    //============ Barcode ============
    public void getBarcodePreSuffix() {
        barcodeNewland.getBarcodePreSuffix();
    }
    public void getBarcodeReadingMode() {
        barcodeNewland.barcodeSendQueryReadingMode();
    }
    void getBarcodeEnable2dBarCodes() {
        barcodeNewland.barcodeSendQueryEnable2dBarCodes();
    }
    void getBarcodePrefixOrder() {
        barcodeNewland.barcodeSendQueryPrefixOrder();
    }
    void getBarcodeDelayTimeOfEachReading() {
        barcodeNewland.barcodeSendQueryDelayTimeOfEachReading();
    }
    void getBarcodeNoDuplicateReading() {
        barcodeNewland.barcodeSendQueryNoDuplicateReading();
    }

    public boolean isBarcodeFailure() {
        return barcodeConnector.barcodeFailure;
    }
    public String getBarcodeDate() {
        return barcodeNewland.getBarcodeDate();
    }
    public boolean getBarcodeOnStatus() {
        return barcodeConnector.getOnStatus();
    }
    public boolean setBarcodeOn(boolean on) {
        boolean retValue;
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        if (on) csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_POWER_ON;
        else    csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_POWER_OFF;
        csReaderBarcodeData.waitUplinkResponse = false;
        retValue = barcodeConnector.barcodeToWrite.add(csReaderBarcodeData); appendToLog("barcodeToWrite added with size = " + barcodeConnector.barcodeToWrite.size());
        boolean continuousAfterOn = false;
        if (retValue && on && continuousAfterOn) {
            if (checkHostProcessorVersion(getBluetoothICFirmwareVersion(), 1, 0, 2)) {
                if (DEBUG) appendToLog("to barcodeSendCommandConinuous()");
                retValue = barcodeNewland.barcodeSendCommandConinuous();
            } else retValue = false;
        }
        if (DEBUG) appendToLog("mBarcodeToWrite size = " + barcodeConnector.barcodeToWrite.size());
        return retValue;
    }
    int iModeSet = -1, iVibratieTimeSet = -1;
    public boolean setVibrateOn(int mode) {
        boolean retValue;
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        if (mode > 0) csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_VIBRATE_ON;
        else    csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_VIBRATE_OFF;
        csReaderBarcodeData.waitUplinkResponse = false;
        if (iModeSet == mode && iVibratieTimeSet == getVibrateTime()) {
            appendToLog("writeBleStreamOut: A7B3: Skip saving vibration data");
            return true;
        }
        if (mode > 0) {
            byte[] barcodeCommandData = new byte[3];
            barcodeCommandData[0] = (byte) (mode - 1);
            barcodeCommandData[1] = (byte) (getVibrateTime() / 256);
            barcodeCommandData[2] = (byte) (getVibrateTime() % 256);
            csReaderBarcodeData.dataValues = barcodeCommandData;
        }
        retValue = barcodeConnector.barcodeToWrite.add(csReaderBarcodeData); appendToLog("barcodeToWrite added with size = " + barcodeConnector.barcodeToWrite.size());
        if (DEBUG) appendToLog("mBarcodeToWrite size = " + barcodeConnector.barcodeToWrite.size());
        if (retValue) {
            iModeSet = mode; iVibratieTimeSet = getVibrateTime();
        }
        return retValue;
    }
    boolean inventoryVibrateDefault = false, inventoryVibrate = inventoryVibrateDefault;
    public boolean getInventoryVibrate() {
        return inventoryVibrate;
    }
    public boolean setInventoryVibrate(boolean inventoryVibrate) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("this.inventoryVibrate = " + this.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate);
        this.inventoryVibrate = inventoryVibrate;
        if (DEBUG) appendToLog("this.inventoryVibrate = " + this.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate);
        return true;
    }
    int vibrateTimeSettingDefault = 300, vibrateTimeSetting = vibrateTimeSettingDefault;
    public int getVibrateTime() {
        return vibrateTimeSetting;
    }
    public boolean setVibrateTime(int vibrateTime) {
        vibrateTimeSetting = vibrateTime;
        return true;
    }
    int vibrateWindowSettingDefault = 2, vibrateWindowSetting = vibrateWindowSettingDefault;
    public int getVibrateWindow() {
        return vibrateWindowSetting;
    }
    public boolean setVibrateWindow(int vibrateWindow) {
        vibrateWindowSetting = vibrateWindow;
        return true;
    }
    public boolean barcodeSendCommandTrigger() {
        return barcodeNewland.barcodeSendCommandTrigger();
    }
    public boolean barcodeSendCommandSetPreSuffix() {
        return barcodeNewland.barcodeSendCommandSetPreSuffix();
    }
    public boolean barcodeSendCommandResetPreSuffix() {
        return barcodeNewland.barcodeSendCommandResetPreSuffix();
    }
    public boolean barcodeSendCommandConinuous() {
        return barcodeNewland.barcodeSendCommandConinuous();
    }
    public String getBarcodeVersion() {
        return barcodeNewland.getBarcodeVersion();
    }
    public String getBarcodeSerial() {
        return barcodeNewland.getBarcodeSerial();
    }
    boolean barcodeAutoStarted = false;
    public boolean barcodeInventory(boolean start) {
        boolean result = true;
        appendToLog("TTestPoint 0: " + start);
        if (start) {
            barcodeConnector.mBarcodeToRead.clear(); barcodeDataStore = null;
            if (getBarcodeOnStatus() == false) { result = setBarcodeOn(true); appendToLog("TTestPoint 1"); }
            if (barcodeNewland.barcode2TriggerMode && result) {
                if (getTriggerButtonStatus() && notificationConnector.getAutoBarStartSTop()) {  appendToLog("TTestPoint 2"); barcodeAutoStarted = true; result = true; }
                else {  appendToLog("TTestPoint 3"); result = barcodeNewland.barcodeSendCommand(new byte[]{0x1b, 0x33}); }
            } else  appendToLog("TTestPoint 4");
            appendToLog("TTestPoint 5");
        } else {
            appendToLog("getBarcodeOnStatus = " + getBarcodeOnStatus() + ", result = " + result);
            if (barcodeNewland.barcode2TriggerMode == false) {  appendToLog("TTestPoint 6"); result = setBarcodeOn(false); }
            else if (getBarcodeOnStatus() == false && result) {  appendToLog("TTestPoint 7"); result = setBarcodeOn(true); }
            appendToLog("barcode2TriggerMode = " + barcodeNewland.barcode2TriggerMode + ", result = " + result + ", barcodeAutoStarted = " + barcodeAutoStarted);
            if (barcodeNewland.barcode2TriggerMode && result) {
                if (barcodeAutoStarted && result) {  appendToLog("TTestPoint 8"); barcodeAutoStarted = false; result = true; }
                else {  appendToLog("TTestPoint 9"); result = barcodeNewland.barcodeSendCommand(new byte[] { 0x1b, 0x30 }); }
            } else  appendToLog("TTestPoint 10");
        }
        return result;
    }
    byte[] barcodeDataStore = null; long timeBarcodeData;
    public byte[] onBarcodeEvent() {
        byte[] barcodeData = null;
        if (barcodeConnector.mBarcodeToRead.size() != 0) {
            BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = barcodeConnector.mBarcodeToRead.get(0);
            barcodeConnector.mBarcodeToRead.remove(0);
            if (csReaderBarcodeData != null) {
                if (csReaderBarcodeData.barcodePayloadEvent == BarcodeConnector.BarcodePayloadEvents.BARCODE_GOOD_READ) {
                    if (false) barcodeData = "<GR>".getBytes();
                } else if (csReaderBarcodeData.barcodePayloadEvent == BarcodeConnector.BarcodePayloadEvents.BARCODE_DATA_READ) {
                    barcodeData = csReaderBarcodeData.dataValues;
                }
            }
        }

        byte[] barcodeCombined = null;
        if (false) barcodeCombined = barcodeData;
        else if (barcodeData != null) {
            appendToLog("BarStream: barcodeData = " + byteArrayToString(barcodeData) + ", barcodeDataStore = " + byteArrayToString(barcodeDataStore));
            int barcodeDataStoreIndex = 0;
            int length = barcodeData.length;
            if (barcodeDataStore != null) {
                barcodeDataStoreIndex = barcodeDataStore.length;
                length += barcodeDataStoreIndex;
            }
            barcodeCombined = new byte[length];
            if (barcodeDataStore != null)
                System.arraycopy(barcodeDataStore, 0, barcodeCombined, 0, barcodeDataStore.length);
            System.arraycopy(barcodeData, 0, barcodeCombined, barcodeDataStoreIndex, barcodeData.length);
            barcodeDataStore = barcodeCombined;
            timeBarcodeData = System.currentTimeMillis();
            barcodeCombined = new byte[0];
        }
        if (barcodeDataStore != null) {
            barcodeCombined = new byte[barcodeDataStore.length];
            System.arraycopy(barcodeDataStore, 0, barcodeCombined, 0, barcodeCombined.length);

            if (System.currentTimeMillis() - timeBarcodeData < 300) barcodeCombined = null;
            else barcodeDataStore = null;
        }
        if (barcodeCombined != null && barcodeNewland.getPrefix() != null && barcodeNewland.getSuffix() != null) {
            if (barcodeCombined.length == 0) barcodeCombined = null;
            else {
                byte[] prefixExpected = barcodeNewland.getPrefix(); boolean prefixFound = false;
                byte[] suffixExpected = barcodeNewland.getSuffix(); boolean suffixFound = false;
                int codeTypeLength = 4;
                appendToLog("BarStream: barcodeCombined = " + byteArrayToString(barcodeCombined) + ", Expected Prefix = " + byteArrayToString(prefixExpected)  + ", Expected Suffix = " + byteArrayToString(suffixExpected));
                if (barcodeCombined.length > prefixExpected.length + suffixExpected.length + codeTypeLength) {
                    int i = 0;
                    for (; i <= barcodeCombined.length - prefixExpected.length - suffixExpected.length; i++) {
                        int j = 0;
                        for (; j < prefixExpected.length; j++) {
                            if (barcodeCombined[i+j] != prefixExpected[j]) break;
                        }
                        if (j == prefixExpected.length) { prefixFound = true; break; }
                    }
                    int k = i + prefixExpected.length;
                    for (; k <= barcodeCombined.length - suffixExpected.length; k++) {
                        int j = 0;
                        for (; j < suffixExpected.length; j++) {
                            if (barcodeCombined[k+j] != suffixExpected[j]) break;
                        }
                        if (j == suffixExpected.length) { suffixFound = true; break; }
                    }
                    appendToLog("BarStream: iPrefix = " + i + ", iSuffix = " + k + ", with prefixFound = " + prefixFound + ", suffixFound = " + suffixFound);
                    if (prefixFound && suffixFound) {
                        byte[] barcodeCombinedNew = new byte[k - i - prefixExpected.length - codeTypeLength];
                        System.arraycopy(barcodeCombined, i + prefixExpected.length + codeTypeLength, barcodeCombinedNew, 0, barcodeCombinedNew.length);
                        barcodeCombined = barcodeCombinedNew;
                        appendToLog("BarStream: barcodeCombinedNew = " + byteArrayToString(barcodeCombinedNew));

                        if (true) {
                            byte[] prefixExpected1 = {0x5B, 0x29, 0x3E, 0x1E};
                            prefixFound = false;
                            byte[] suffixExpected1 = {0x1E, 0x04};
                            suffixFound = false;
                            appendToLog("BarStream: barcodeCombined = " + byteArrayToString(barcodeCombined) + ", Expected Prefix = " + byteArrayToString(prefixExpected1) + ", Expected Suffix = " + byteArrayToString(suffixExpected1));
                            if (barcodeCombined.length > prefixExpected1.length + suffixExpected1.length) {
                                i = 0;
                                for (; i <= barcodeCombined.length - prefixExpected1.length - suffixExpected1.length; i++) {
                                    int j = 0;
                                    for (; j < prefixExpected1.length; j++) {
                                        if (barcodeCombined[i + j] != prefixExpected1[j]) break;
                                    }
                                    if (j == prefixExpected1.length) {
                                        prefixFound = true;
                                        break;
                                    }
                                }
                                k = i + prefixExpected1.length;
                                for (; k <= barcodeCombined.length - suffixExpected1.length; k++) {
                                    int j = 0;
                                    for (; j < suffixExpected1.length; j++) {
                                        if (barcodeCombined[k + j] != suffixExpected1[j]) break;
                                    }
                                    if (j == suffixExpected1.length) {
                                        suffixFound = true;
                                        break;
                                    }
                                }
                                appendToLog("BarStream: iPrefix = " + i + ", iSuffix = " + k + ", with prefixFound = " + prefixFound + ", suffixFound = " + suffixFound);
                                if (prefixFound && suffixFound) {
                                    barcodeCombinedNew = new byte[k - i - prefixExpected1.length];
                                    System.arraycopy(barcodeCombined, i + prefixExpected1.length, barcodeCombinedNew, 0, barcodeCombinedNew.length);
                                    barcodeCombined = barcodeCombinedNew;
                                    appendToLog("BarStream: barcodeCombinedNew = " + byteArrayToString(barcodeCombinedNew));
                                }
                            }
                        }
                    }
                } else barcodeCombined = null;
            }
        }
        return barcodeCombined;
    }

    //============ Android General ============
    public void setSameCheck(boolean sameCheck1) {
        if (csReaderConnector.sameCheck == sameCheck1) return;
        appendToLog("!!! new sameCheck = " + sameCheck1 + ", with old sameCheck = " + csReaderConnector.sameCheck);
        csReaderConnector.sameCheck = sameCheck1; //sameCheck = false;
    }
    public void saveSetting2File() {
        appendToLog("Start");
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            stream.write("Start of data\n".getBytes());

            String outData = "appVersion," + getlibraryVersion() +"\n"; stream.write(outData.getBytes());  appendToLog("outData = " + outData);
            outData = "countryInList," + String.valueOf(getCountryNumberInList() +"\n"); stream.write(outData.getBytes());  appendToLog("outData = " + outData);
            if (getChannelHoppingStatus() == false)
                outData = "channel," + String.valueOf(getChannel() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);

            outData = "antennaPower," + String.valueOf(rfidReaderChip.rx000Setting.getAntennaPower(0) +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "population," + String.valueOf(getPopulation() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "querySession," + String.valueOf(getQuerySession() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "queryTarget," + String.valueOf(getQueryTarget() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "tagFocus," + String.valueOf(getTagFocus() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "fastId," + String.valueOf(getFastId() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "invAlgo," + String.valueOf(getInvAlgo() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "retry," + String.valueOf(getRetryCount() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "currentProfile," + String.valueOf(getCurrentProfile() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "rxGain," + String.valueOf(getRxGain() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);

            outData = "deviceName," + getBluetoothICFirmwareName() +"\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "batteryDisplay," + String.valueOf(getBatteryDisplaySetting() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "rssiDisplay," + String.valueOf(getRssiDisplaySetting() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "tagDelay," + String.valueOf(getTagDelay() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "cycleDelay," + String.valueOf(getCycleDelay() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "intraPkDelay," + String.valueOf(getIntraPkDelay() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "dupDelay," + String.valueOf(getDupDelay() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);

            outData = "triggerReporting," + String.valueOf(notificationConnector.getTriggerReporting() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "triggerReportingCount," + String.valueOf(notificationConnector.getTriggerReportingCount() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryBeep," + String.valueOf(getInventoryBeep() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryBeepCount," + String.valueOf(getBeepCount() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryVibrate," + String.valueOf(getInventoryVibrate() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryVibrateTime," + String.valueOf(getVibrateTime() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryVibrateMode," + String.valueOf(getVibrateModeSetting() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "inventoryVibrateWindow," + String.valueOf(getVibrateWindow() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);

            outData = "savingFormat," + String.valueOf(getSavingFormatSetting() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "csvColumnSelect," + String.valueOf(getCsvColumnSelectSetting() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "saveFileEnable," + String.valueOf(getSaveFileEnable() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "saveCloudEnable," + String.valueOf(getSaveCloudEnable() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "saveNewCloudEnable," + String.valueOf(getSaveNewCloudEnable() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "saveAllCloudEnable," + String.valueOf(getSaveAllCloudEnable() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "serverLocation," + getServerLocation() + "\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "serverTimeout," + String.valueOf(getServerTimeout() +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            write2FileStream(stream, "serverImpinjLocation," + getServerImpinjLocation() + "\n");
            write2FileStream(stream, "serverImpinjName," + getServerImpinjName() + "\n");
            write2FileStream(stream, "serverImpinjPassword," + getServerImpinjPassword() + "\n");
            outData = "barcode2TriggerMode," + String.valueOf(barcodeNewland.barcode2TriggerMode +"\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
/*
            outData = "wedgePrefix," + getWedgePrefix() + "\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "wedgeSuffix," + getWedgeSuffix() + "\n"; stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            outData = "wedgeDelimiter," + String.valueOf(getWedgeDelimiter() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
*/
            outData = "userDebugEnable," + String.valueOf(getUserDebugEnable() + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            if (preFilterData != null) {
                outData = "preFilterData.enable," + String.valueOf(preFilterData.enable + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.target," + String.valueOf(preFilterData.target + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.action," + String.valueOf(preFilterData.action + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.bank," + String.valueOf(preFilterData.bank + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.offset," + String.valueOf(preFilterData.offset + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.mask," + String.valueOf(preFilterData.mask + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
                outData = "preFilterData.maskbit," + String.valueOf(preFilterData.maskbit + "\n"); stream.write(outData.getBytes()); appendToLog("outData = " + outData);
            }

            stream.write("End of data\n".getBytes()); appendToLog("outData = " + outData);
            stream.close();
        } catch (Exception ex){
            //
        }
    }
    public int getBeepCount() {
        return beepCountSetting;
    }
    public boolean setBeepCount(int beepCount) {
        beepCountSetting = beepCount;
        return true;
    }
    boolean inventoryBeepDefault = true, inventoryBeep = inventoryBeepDefault;
    public boolean getInventoryBeep() {
        return inventoryBeep;
    }
    public boolean setInventoryBeep(boolean inventoryBeep) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("this.inventoryBeep = " + this.inventoryBeep + ", inventoryBeep = " + inventoryBeep);
        this.inventoryBeep = inventoryBeep;
        if (DEBUG) appendToLog("this.inventoryBeep = " + this.inventoryBeep + ", inventoryBeep = " + inventoryBeep);
        return true;
    }
    boolean saveFileEnableDefault = true, saveFileEnable = saveFileEnableDefault;
    public boolean getSaveFileEnable() {
        return saveFileEnable;
    }
    public boolean setSaveFileEnable(boolean saveFileEnable) {
        appendToLog("this.saveFileEnable = " + this.saveFileEnable + ", saveFileEnable = " + saveFileEnable);
        this.saveFileEnable = saveFileEnable;
        appendToLog("this.saveFileEnable = " + this.saveFileEnable + ", saveFileEnable = " + saveFileEnable);
        return true;
    }
    boolean saveCloudEnableDefault = false, saveCloudEnable = saveCloudEnableDefault;
    public boolean getSaveCloudEnable() {
        return saveCloudEnable;
    }
    public boolean setSaveCloudEnable(boolean saveCloudEnable) {
        this.saveCloudEnable = saveCloudEnable;
        return true;
    }
    boolean saveNewCloudEnableDefault = false, saveNewCloudEnable = saveNewCloudEnableDefault;
    public boolean getSaveNewCloudEnable() {
        return saveNewCloudEnable;
    }
    public boolean setSaveNewCloudEnable(boolean saveNewCloudEnable) {
        this.saveNewCloudEnable = saveNewCloudEnable;
        return true;
    }
    boolean saveAllCloudEnableDefault = false, saveAllCloudEnable = saveAllCloudEnableDefault;
    public boolean getSaveAllCloudEnable() {
        return saveAllCloudEnable;
    }
    public boolean setSaveAllCloudEnable(boolean saveAllCloudEnable) {
        this.saveAllCloudEnable = saveAllCloudEnable;
        return true;
    }
    public boolean getUserDebugEnable() {
        boolean bValue = bluetoothConnector.userDebugEnable; appendToLog("bValue = " + bValue); return bValue;
    }
    public boolean setUserDebugEnable(boolean userDebugEnable) {
        appendToLog("new userDebug = " + userDebugEnable);
        bluetoothConnector.userDebugEnable = userDebugEnable;
        return true;
    }
    String serverLocationDefault = "", serverLocation = serverLocationDefault;
    public String getServerLocation() {
        return serverLocation;
    }
    public boolean setServerLocation(String serverLocation) {
        this.serverLocation = serverLocation;
        return true;
    }
    int serverTimeoutDefault = 6, serverTimeout = serverTimeoutDefault;
    public int getServerTimeout() {
        return serverTimeout;
    }
    public boolean setServerTimeout(int serverTimeout) {
        this.serverTimeout = serverTimeout;
        return true;
    }
    //String serverImpinjLocationDefault = "https://h9tqczg9-7275.asse.devtunnels.ms", serverImpinjLocation = serverImpinjLocationDefault;
    String serverImpinjLocationDefault = "https://democloud.convergence.com.hk/ias", serverImpinjLocation = serverImpinjLocationDefault;
    public String getServerImpinjLocation() {
        return serverImpinjLocation;
    }
    public boolean setServerImpinjLocation(String serverImpinjLocation) {
        this.serverImpinjLocation = serverImpinjLocation;
        return true;
    }
    //String serverImpinjNameDefault = "wallace.sit@cne.com.hk", serverImpinjName = serverImpinjNameDefault;
    String serverImpinjNameDefault = "", serverImpinjName = serverImpinjNameDefault;
    public String getServerImpinjName() {
        appendToLog("serverImpinjName = " + serverImpinjName);
        return serverImpinjName;
    }
    public boolean setServerImpinjName(String serverImpinjName) {
        this.serverImpinjName = serverImpinjName;
        appendToLog("serverImpinjName = " + serverImpinjName);
        return true;
    }
    //String serverImpinjPasswordDefault = "Cne12345678?", serverImpinjPassword = serverImpinjPasswordDefault;
    String serverImpinjPasswordDefault = "", serverImpinjPassword = serverImpinjPasswordDefault;
    public String getServerImpinjPassword() {
        return serverImpinjPassword;
    }
    public boolean setServerImpinjPassword(String serverImpinjPassword) {
        this.serverImpinjPassword = serverImpinjPassword;
        return true;
    }
    int batteryDisplaySelectDefault = 1, batteryDisplaySelect = batteryDisplaySelectDefault;
    public int getBatteryDisplaySetting() {
        return batteryDisplaySelect;
    }
    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        if (batteryDisplaySelect < 0 || batteryDisplaySelect > 1)   return false;
        this.batteryDisplaySelect = batteryDisplaySelect;
        return true;
    }
    public final double dBuV_dBm_constant = 106.98;
    int rssiDisplaySelectDefault = 1, rssiDisplaySelect = rssiDisplaySelectDefault;
    public int getRssiDisplaySetting() {
        return rssiDisplaySelect;
    }
    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        if (rssiDisplaySelect < 0 || rssiDisplaySelect > 1)   return false;
        this.rssiDisplaySelect = rssiDisplaySelect;
        return true;
    }
    int vibrateModeSelectDefault = 0, vibrateModeSelect = vibrateModeSelectDefault;
    public int getVibrateModeSetting() {
        return vibrateModeSelect;
    }
    public boolean setVibrateModeSetting(int vibrateModeSelect) {
        if (vibrateModeSelect < 0 || vibrateModeSelect > 1)   return false;
        this.vibrateModeSelect = vibrateModeSelect;
        return true;
    }
    int savingFormatSelectDefault = 0, savingFormatSelect = savingFormatSelectDefault;
    public int getSavingFormatSetting() {
        return savingFormatSelect;
    }
    public boolean setSavingFormatSetting(int savingFormatSelect) {
        if (false) appendToLog("savingFormatSelect = " + savingFormatSelect);
        if (savingFormatSelect < 0 || savingFormatSelect > 1)   return false;
        this.savingFormatSelect = savingFormatSelect;
        return true;
    }
    int csvColumnSelectDefault = 0, csvColumnSelect = csvColumnSelectDefault;
    public int getCsvColumnSelectSetting() {
        return csvColumnSelect;
    }
    public boolean setCsvColumnSelectSetting(int csvColumnSelect) {
        if (false) appendToLog("csvColumnSelect = " + csvColumnSelect);
        this.csvColumnSelect = csvColumnSelect;
        return true;
    }
    private String wedgePrefix = null, wedgeSuffix = null;
    private int wedgeDelimiter = 0x0a;
    public String getWedgePrefix() {
        return wedgePrefix;
    }
    public String getWedgeSuffix() {
        return wedgeSuffix;
    }
    public int getWedgeDelimiter() {
        return wedgeDelimiter;
    }
    public void setWedgePrefix(String string) {
        wedgePrefix = string;
    }
    public void setWedgeSuffix(String string) {
        wedgeSuffix = string;
    }
    public void setWedgeDelimiter(int iValue) {
        wedgeDelimiter = iValue;
    }

    //============ Bluetooth ============
    public String getBluetoothICFirmwareVersion() {
        return bluetoothConnector.getBluetoothIcVersion();
    }
    public String getBluetoothICFirmwareName() {
        return bluetoothConnector.getBluetoothIcName();
    }
    public boolean setBluetoothICFirmwareName(String name) {
        return bluetoothConnector.setBluetoothIcName(name);
    }

    //============ Controller ============
    public String hostProcessorICGetFirmwareVersion() {
        return controllerConnector.getVersion();
    }
    public String getHostProcessorICSerialNumber() {
        String str;
        if (bluetoothConnector.getCsModel() == 108) str = controllerConnector.getSerialNumber();
        else str = rfidReaderChip.rx000OemSetting.getProductSerialNumber();
        if (str != null) {
            if (str.length() > 13) return str.substring(0, 13);
        }
        return null;
    }
    public String getHostProcessorICBoardVersion() {
        String str;
        if (bluetoothConnector.getCsModel() == 108) str = controllerConnector.getSerialNumber();
        else str = rfidReaderChip.rx000OemSetting.getProductSerialNumber();
        if (false) appendToLog("getBoardVersion = " + str);
        if (str != null) {
            if (str.length() == 16) {
                String strOut = "";
                if (str.substring(13, 14).matches("0") == false) strOut = str.substring(13, 14);
                strOut += (strOut.length() != 0 ? "." : "") + str.substring(14, 15);
                if (str.substring(15, 16).matches("0") == false || strOut.length() < 3) strOut += (strOut.length() < 3 ? "." : "") + str.substring(15, 16);
                if (false) appendToLog("getBoardVersion 1 = " + str);
                return strOut;
            }
        }
        return null;
    }

    //============ Controller notification ============
    int iBatteryCount;
    int getBatteryValue2Percent(float floatValue) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("getHostProcessorICBoardVersion = " + getHostProcessorICBoardVersion() + ", strVersionMBoard = " + strVersionMBoard);
        if (false || checkHostProcessorVersion(getHostProcessorICBoardVersion(), Integer.parseInt(strMBoardVersions[0].trim()), Integer.parseInt(strMBoardVersions[1].trim()), 0)) {
            final float[] fValueStbyRef = {
                    (float) 4.212, (float) 4.175, (float) 4.154, (float) 4.133, (float) 4.112,
                    (float) 4.085, (float) 4.069, (float) 4.054, (float) 4.032, (float) 4.011,
                    (float) 3.990, (float) 3.969, (float) 3.953, (float) 3.937, (float) 3.922,
                    (float) 3.901, (float) 3.885, (float) 3.869, (float) 3.853, (float) 3.837,
                    (float) 3.821, (float) 3.806, (float) 3.790, (float) 3.774, (float) 3.769,
                    (float) 3.763, (float) 3.758, (float) 3.753, (float) 3.747, (float) 3.742,
                    (float) 3.732, (float) 3.721, (float) 3.705, (float) 3.684, (float) 3.668,
                    (float) 3.652, (float) 3.642, (float) 3.626, (float) 3.615, (float) 3.605,
                    (float) 3.594, (float) 3.584, (float) 3.568, (float) 3.557, (float) 3.542,
                    (float) 3.531, (float) 3.510, (float) 3.494, (float) 3.473, (float) 3.457,
                    (float) 3.436, (float) 3.410, (float) 3.362, (float) 3.235, (float) 2.987,
                    (float) 2.982
            };
            final float[] fPercentStbyRef = {
                    (float) 100, (float) 98, (float) 96, (float) 95, (float) 93,
                    (float)  91, (float) 89, (float) 87, (float) 85, (float) 84,
                    (float)  82, (float) 80, (float) 78, (float) 76, (float) 75,
                    (float)  73, (float) 71, (float) 69, (float) 67, (float) 65,
                    (float)  64, (float) 62, (float) 60, (float) 58, (float) 56,
                    (float)  55, (float) 53, (float) 51, (float) 49, (float) 47,
                    (float)  45, (float) 44, (float) 42, (float) 40, (float) 38,
                    (float)  36, (float) 35, (float) 33, (float) 31, (float) 29,
                    (float)  27, (float) 25, (float) 24, (float) 22, (float) 20,
                    (float)  18, (float) 16, (float) 15, (float) 13, (float) 11,
                    (float)   9, (float)  7, (float)  5, (float)  4, (float)  2,
                    (float)   0
            };
            final float[] fValueRunRef = {
                    (float) 4.106, (float) 4.017, (float) 3.98 , (float) 3.937, (float) 3.895,
                    (float) 3.853, (float) 3.816, (float) 3.779, (float) 3.742, (float) 3.711,
                    (float) 3.679, (float) 3.658, (float) 3.637, (float) 3.626, (float) 3.61 ,
                    (float) 3.584, (float) 3.547, (float) 3.515, (float) 3.484, (float) 3.457,
                    (float) 3.431, (float) 3.399, (float) 3.362, (float) 3.32 , (float) 3.251,
                    (float) 3.135
            };
            final float[] fPercentRunRef = {
                    (float) 100, (float) 96, (float) 92, (float) 88, (float) 84,
                    (float) 80,  (float) 76, (float) 72, (float) 67, (float) 63,
                    (float) 59,  (float) 55, (float) 51, (float) 47, (float) 43,
                    (float) 39,  (float) 35, (float) 31, (float) 27, (float) 23,
                    (float) 19,  (float) 15, (float) 11,  (float) 7, (float)  2,
                    (float) 0
            };
            float[] fValueRef = fValueStbyRef;
            float[] fPercentRef = fPercentStbyRef;

            if (true && iBatteryCount != getBatteryCount()) {
                iBatteryCount = getBatteryCount();
                iBatteryNewCurveDelay++;
            }
            if (csReaderConnector.rfidReader.mRfidToWrite.size() != 0) iBatteryNewCurveDelay = 0;
            else if (csReaderConnector.rfidReader.isInventoring()) {
                if (bUsingInventoryBatteryCurve == false) { if (iBatteryNewCurveDelay > 1) { iBatteryNewCurveDelay = 0; bUsingInventoryBatteryCurve = true; } }
                else iBatteryNewCurveDelay = 0;
            } else if (bUsingInventoryBatteryCurve) { if (iBatteryNewCurveDelay > 2) { iBatteryNewCurveDelay = 0; bUsingInventoryBatteryCurve = false; } }
            else iBatteryNewCurveDelay = 0;

            if (bUsingInventoryBatteryCurve) {
                fValueRef = fValueRunRef;
                fPercentRef = fPercentRunRef;
            }
            if (DEBUG) appendToLog("NEW Percentage cureve is USED with bUsingInventoryBatteryCurve = " + bUsingInventoryBatteryCurve + ", iBatteryNewCurveDelay = " + iBatteryNewCurveDelay);

            int index = 0;
            while (index < fValueRef.length) {
                if (floatValue > fValueRef[index]) break;
                index++;
            }
            if (DEBUG) appendToLog("Index = " + index);
            if (index == 0) return 100;
            if (index == fValueRef.length) return 0;
            float value = ((fValueRef[index - 1] - floatValue) / (fValueRef[index - 1] - fValueRef[index]));
            if (true) {
                value *= (fPercentRef[index -1] - fPercentRef[index]);
                value = fPercentRef[index - 1] - value;
            } else {
                value += (float) (index - 1);
                value /= (float) (fValueRef.length - 1);
                value *= 100;
                value = 100 - value;
            }
            value += 0.5;
            int iValue = (int) (value);
            if (iBatteryNewCurveDelay != 0) iValue = iBatteryPercentOld;
            else if (bUsingInventoryBatteryCurve && floatValue <= fBatteryValueOld && iValue >= iBatteryPercentOld) iValue = iBatteryPercentOld;
            fBatteryValueOld = floatValue; iBatteryPercentOld = iValue;
            return iValue;
        } else {
            if (DEBUG) appendToLog("OLD Percentage cureve is USED");
            if (floatValue >= 4) return 100;
            else if (floatValue < 3.4) return 0;
            else {
                float result = (float) 166.67 * floatValue - (float) 566.67;
                return (int) result;
            }
        }
    }
    public int getBatteryLevel() {
        return csReaderConnector.mCs108ConnectorData.getVoltageMv();
    }
    public boolean setAutoTriggerReporting(byte timeSecond) {
        return notificationConnector.setAutoTriggerReporting(timeSecond);
    }
    public boolean getAutoBarStartSTop() {
        return notificationConnector.getAutoBarStartStopStatus();
    }

    public boolean batteryLevelRequest() {
        if (csReaderConnector.rfidReader.isInventoring()) {
            appendToLog("Skip batteryLevelREquest as inventoring !!!");
            return true;
        }
        return notificationConnector.batteryLevelRequest();
    }
    public boolean setAutoBarStartSTop(boolean enable) {
        return notificationConnector.setAutoBarStartSTop(enable);
    }
    public boolean getTriggerReporting() {
        return notificationConnector.triggerReporting;
    }
    public boolean setTriggerReporting(boolean triggerReporting) {
        return notificationConnector.setTriggerReporting(triggerReporting);
    }
    public final int iNO_SUCH_SETTING = 10000;
    public short getTriggerReportingCount() {
        boolean bValue = false;
        if (getcsModel() == 108) bValue = checkHostProcessorVersion(hostProcessorICGetFirmwareVersion(),  1, 0, 16);
        if (bValue == false) return iNO_SUCH_SETTING; else
            return notificationConnector.triggerReportingCountSetting;
    }
    public boolean setTriggerReportingCount(short triggerReportingCount) {
        return notificationConnector.setTriggerReportingCount(triggerReportingCount);
    }
    public String getBatteryDisplay(boolean voltageDisplay) {
        float floatValue = (float) getBatteryLevel() / 1000;
        if (floatValue == 0)    return " ";
        String retString = null;
        if (voltageDisplay || (getBatteryDisplaySetting() == 0)) retString = String.format("%.3f V", floatValue);
        else retString = (String.format("%d", getBatteryValue2Percent(floatValue)) + "%");
        if (voltageDisplay == false) retString +=  String.format("\r\n P=%d", getPwrlevel());
        return retString;
    }
    String strVersionMBoard = "1.8"; String[] strMBoardVersions = strVersionMBoard.split("\\.");
    int iBatteryNewCurveDelay; boolean bUsingInventoryBatteryCurve = false; float fBatteryValueOld; int iBatteryPercentOld;
    public String isBatteryLow() {
        boolean batterylow = false;
        int iValue = getBatteryLevel();
        if (iValue == 0) return null;
        float fValue = (float) iValue / 1000;
        int iPercent = getBatteryValue2Percent(fValue);
        if (checkHostProcessorVersion(getHostProcessorICBoardVersion(), Integer.parseInt(strMBoardVersions[0].trim()), Integer.parseInt(strMBoardVersions[1].trim()), 0)) {
            if (true) {
                if (csReaderConnector.rfidReader.isInventoring()) {
                    if (fValue < 3.520) batterylow = true;
                } else if (bUsingInventoryBatteryCurve == false) {
                    if (fValue < 3.626) batterylow = true;
                }
            } else if (iPercent <= 20) batterylow = true;
        } else if (true) {
            if (csReaderConnector.rfidReader.isInventoring()) {
                if (fValue < 3.45) batterylow = true;
            } else if (bUsingInventoryBatteryCurve == false) {
                if (fValue < 3.6) batterylow = true;
            }
        } else if (iPercent <= 8) batterylow = true;
        if (batterylow) return String.valueOf(iPercent);
        return null;
    }
    public int getBatteryCount() {
        return csReaderConnector.mCs108ConnectorData.getVoltageCnt();
    }
    public boolean getTriggerButtonStatus() {
        return notificationConnector.getTriggerStatus();
    }
    public int getTriggerCount() {
        return csReaderConnector.mCs108ConnectorData.getTriggerCount();
    }
    //public interface NotificationListener { void onChange(); }
    public void setNotificationListener(NotificationConnector.NotificationListener listener) {
        notificationConnector.setNotificationListener0(listener);
    }
    public byte[] onNotificationEvent() {
        byte[] notificationData = null;
        if (notificationConnector == null) {
            appendToLog("notificationConnector is null");
            return null;
        }
        if (notificationConnector.notificationToRead.size() != 0) {
            NotificationConnector.CsReaderNotificationData csReaderNotificationData = notificationConnector.notificationToRead.get(0);
            notificationConnector.notificationToRead.remove(0);
            if (csReaderNotificationData != null) notificationData = csReaderNotificationData.dataValues;
        }
        return notificationData;
    }

    //============ to be modified ============
    String getModelName() {
        return controllerConnector.getModelName();
    }
    public String getSerialNumber() {
        return rfidReaderChip.rx000OemSetting.getSerialNumber();
    }
    public boolean setRfidOn(boolean onStatus) {
        return rfidReaderChip.turnOn(onStatus);
    }
    private final Runnable reinitaliseDataRunnable = new Runnable() {
        @Override
        public void run() {
            appendToLog("reset before: reinitaliseDataRunnable starts with inventoring=" + csReaderConnector.rfidReader.isInventoring() + ", mrfidToWriteSize=" + mrfidToWriteSize());
            if (csReaderConnector.rfidReader.isInventoring() || mrfidToWriteSize() != 0) {
                mHandler.removeCallbacks(reinitaliseDataRunnable);
                mHandler.postDelayed(reinitaliseDataRunnable, 500);
            } else {
                if (DEBUG_CONNECT) appendToLog("reinitaliseDataRunnable: Start checkVersionRunnable");
                mHandler.postDelayed(checkVersionRunnable, 500);
            }
        }
    };
    private final Runnable checkVersionRunnable = new Runnable() {
        boolean DEBUG = false;
        @Override
        public void run() {
            if (DEBUG_CONNECT) appendToLog("0 checkVersionRunnable with getFreqChannelConfig = " + rfidReaderChip.rx000Setting.getFreqChannelConfig() + ", isBarcodeFailure = " + isBarcodeFailure() + ", bBarcodeTriggerMode = " + barcodeNewland.bBarcodeTriggerMode);
            //if (false && (mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000Setting.getFreqChannelConfig() < 0 || (isBarcodeFailure() == false && mBarcodeDevice.bBarcodeTriggerMode == (byte)0xFF))) {
            if (rfidReaderChip.rx000Setting.getFreqChannelConfig() < 0 || (isBarcodeFailure() == false && barcodeNewland.bBarcodeTriggerMode == (byte)0xFF)) {
                if (DEBUG) appendToLog("checkVersionRunnable: RESTART with FreqChannelConfig = " + rfidReaderChip.rx000Setting.getFreqChannelConfig() + ", bBarcodeTriggerMode = " + barcodeNewland.bBarcodeTriggerMode);
                mHandler.removeCallbacks(checkVersionRunnable);
                mHandler.postDelayed(checkVersionRunnable, 500);
            } else {
                setSameCheck(false);
                if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 1 with BarcodeFailure = " + isBarcodeFailure());
                if (isBarcodeFailure() == false) {
                    if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 2");
                    if (barcodeNewland.checkPreSuffix(barcodeNewland.prefixRef, barcodeNewland.suffixRef) == false) barcodeNewland.barcodeSendCommandSetPreSuffix();
                    if (barcodeNewland.bBarcodeTriggerMode != 0x30) barcodeNewland.barcodeSendCommandTrigger();
                    notificationConnector.getAutoRFIDAbort(); notificationConnector.getAutoBarStartSTop(); //setAutoRFIDAbort(false); setAutoBarStartSTop(true);
                }
                if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 3");
                setAntennaCycle(0xffff);
                if (bluetoothConnector.getCsModel() == 463) {
                    if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 4");
                    setAntennaDwell(2000);
                    setAntennaInvCount(0);
                } else if (bluetoothConnector.getCsModel() == 108) {
                    if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 5");
                    setAntennaDwell(0);
                    setAntennaInvCount(0xfffffffeL);
                }
                if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 6");
                //mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000Setting.setDiagnosticConfiguration(false);
                if (loadSetting1File()) loadSetting1File();
                if (DEBUG) appendToLog("checkVersionRunnable: macVersion  = " + getMacVer());
                if (checkHostProcessorVersion(getMacVer(), 2, 6, 8)) {
                    if (DEBUG) appendToLog("checkVersionRunnable: macVersion >= 2.6.8");
                    rfidReaderChip.rx000Setting.setTagDelay(tagDelaySetting);
                    rfidReaderChip.rx000Setting.setCycleDelay(cycleDelaySetting);
                    rfidReaderChip.rx000Setting.setInvModeCompact(true);
                } else {
                    if (DEBUG) appendToLog("checkVersionRunnable: macVersion < 2.6.8");
                    rfidReaderChip.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting);
                    rfidReaderChip.rx000Setting.setCycleDelay(cycleDelaySetting);
                }
                rfidReaderChip.rx000Setting.setDiagnosticConfiguration(true);
                if (DEBUG) appendToLog("checkVersionRunnable: Checkpoint 10");
                setSameCheck(true);
            }
        }
    };
    boolean loadSetting1File() {
        File path = context.getFilesDir();
        String fileName = bluetoothGatt.getmBluetoothDevice().getAddress();

        fileName = "cs108A_" + fileName.replaceAll(":", "");
        file = new File(path, fileName);
        boolean bNeedDefault = true, DEBUG = false;
        if (DEBUG_FILE) utility.appendToLogView("FileName = " + fileName + ".exits = " + file.exists() + ", with beepEnable = " + getInventoryBeep());
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(instream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    int queryTarget = -1; int querySession = -1; int querySelect = -1;
                    int startQValue = -1; int maxQValue = -1; int minQValue = -1; int retryCount = -1;
                    int fixedQValue = -1; int fixedRetryCount = -1;
                    int population = -1;
                    boolean invAlgo = true; int retry = -1;
                    preFilterData = new PreFilterData();
                    while ((line = bufferedReader.readLine()) != null) {
                        if (DEBUG_FILE || true) appendToLog("Data read = " + line);
                        String[] dataArray = line.split(",");
                        if (dataArray.length == 2) {
                            if (dataArray[0].matches("appVersion")) {
                                if (dataArray[1].matches(getlibraryVersion())) bNeedDefault = false;
                            } else if (bNeedDefault == true) {
                            } else if (dataArray[0].matches("countryInList")) {
                                getRegionList();
                                int countryInListNew = Integer.valueOf(dataArray[1]);
                                if (countryInList != countryInListNew && countryInListNew >= 0) setCountryInList(countryInListNew);
                                channelOrderType = -1;
                            } else if (dataArray[0].matches("channel")) {
                                int channelNew = Integer.valueOf(dataArray[1]);
                                if (getChannelHoppingStatus() == false && channelNew >= 0) setChannel(channelNew);
                            } else if (dataArray[0].matches("antennaPower")) {
                                long lValue = Long.valueOf(dataArray[1]);
                                if (lValue >= 0) setPowerLevel(lValue);
                            } else if (dataArray[0].matches("population")) {
                                population = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("querySession")) {
                                int iValue = Integer.valueOf(dataArray[1]);
                                if (iValue >= 0) querySession = iValue;
                            } else if (dataArray[0].matches("queryTarget")) {
                                queryTarget = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("tagFocus")) {
                                int iValue = Integer.valueOf(dataArray[1]);
                                if (iValue >= 0) csReaderConnector.rfidReader.tagFocus = iValue;
                            } else if (dataArray[0].matches("fastId")) {
                                int iValue = Integer.valueOf(dataArray[1]);
                                if (iValue >= 0) csReaderConnector.rfidReader.fastId = iValue;
                            } else if (dataArray[0].matches("invAlgo")) {
                                invAlgo = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches("retry")) {
                                retry = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("currentProfile")) {
                                int iValue = Integer.valueOf(dataArray[1]);
                                if (iValue >= 0) setCurrentLinkProfile(iValue);
                            } else if (dataArray[0].matches("rxGain")) {
                                setRxGain(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("deviceName")) {
                                bluetoothConnector.deviceName = dataArray[1].getBytes();
                            } else if (dataArray[0].matches("batteryDisplay")) {
                                setBatteryDisplaySetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("rssiDisplay")) {
                                setRssiDisplaySetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("tagDelay")) {
                                setTagDelay(Byte.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("cycleDelay")) {
                                setCycleDelay(Long.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("intraPkDelay")) {
                                setIntraPkDelay(Byte.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches("dupDelay")) {
                                setDupDelay(Byte.valueOf(dataArray[1]));

                            } else if (dataArray[0].matches(("triggerReporting"))) {
                                notificationConnector.setTriggerReporting(dataArray[1].matches("true") ? true : false);
                            } else if (dataArray[0].matches(("triggerReportingCount"))) {
                                notificationConnector.setTriggerReportingCount(Short.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("inventoryBeep"))) {
                                setInventoryBeep(dataArray[1].matches("true") ? true : false);
                            } else if (dataArray[0].matches(("inventoryBeepCount"))) {
                                setBeepCount(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("inventoryVibrate"))) {
                                setInventoryVibrate(dataArray[1].matches("true") ? true : false);
                            } else if (dataArray[0].matches(("inventoryVibrateTime"))) {
                                setVibrateTime(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("inventoryVibrateMode"))) {
                                setVibrateModeSetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("savingFormat"))) {
                                setSavingFormatSetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("csvColumnSelect"))) {
                                setCsvColumnSelectSetting(Integer.valueOf(dataArray[1]));
                            } else if (dataArray[0].matches(("inventoryVibrateWindow"))) {
                                setVibrateWindow(Integer.valueOf(dataArray[1]));

                            } else if (dataArray[0].matches(("saveFileEnable"))) {
                                saveFileEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveCloudEnable"))) {
                                saveCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveNewCloudEnable"))) {
                                saveNewCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("saveAllCloudEnable"))) {
                                saveAllCloudEnable = dataArray[1].matches("true") ? true : false;
                            } else if (dataArray[0].matches(("serverLocation"))) {
                                serverLocation = dataArray[1];
                            } else if (dataArray[0].matches("serverTimeout")) {
                                serverTimeout = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches(("serverImpinjLocation"))) {
                                serverImpinjLocation = dataArray[1];
                            } else if (dataArray[0].matches(("serverImpinjName"))) {
                                serverImpinjName = dataArray[1];
                            } else if (dataArray[0].matches(("serverImpinjPassword"))) {
                                serverImpinjPassword = dataArray[1];

                            } else if (dataArray[0].matches("barcode2TriggerMode")) {
                                if (dataArray[1].matches("true")) barcodeNewland.barcode2TriggerMode = true;
                                else barcodeNewland.barcode2TriggerMode = false;
/*
                            } else if (dataArray[0].matches("wedgePrefix")) {
                                setWedgePrefix(dataArray[1]);
                            } else if (dataArray[0].matches("wedgeSuffix")) {
                                setWedgeSuffix(dataArray[1]);
                            } else if (dataArray[0].matches("wedgeDelimiter")) {
                                setWedgeDelimiter(Integer.valueOf(dataArray[1]));
*/
                            } else if (dataArray[0].matches("preFilterData.enable")) {
                                if (dataArray[1].matches("true")) preFilterData.enable = true;
                                else preFilterData.enable  = false;
                            } else if (dataArray[0].matches("preFilterData.target")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.target = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.action")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.action = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.bank")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.bank = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.offset")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.offset = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("preFilterData.mask")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                preFilterData.mask = dataArray[1];
                            } else if (dataArray[0].matches("preFilterData.maskbit")) {
                                if (preFilterData == null) preFilterData = new PreFilterData();
                                if (dataArray[1].matches("true")) preFilterData.maskbit = true;
                                else preFilterData.maskbit = false;
                            } else if (dataArray[0].matches(("userDebugEnable"))) {
                                bluetoothConnector.userDebugEnable = dataArray[1].matches("true") ? true : false;
                            }
                        }
                    }
                    setInvAlgo(invAlgo); setPopulation(population); setRetryCount(retry); setTagGroup(querySelect, querySession, queryTarget); setTagFocus(csReaderConnector.rfidReader.tagFocus > 0 ? true : false);
                    if (preFilterData != null && preFilterData.enable) {
                        appendToLog("preFilterData is valid. Going to setSelectCriteria");
                        setSelectCriteria(0, preFilterData.enable, preFilterData.target, preFilterData.action, preFilterData.bank, preFilterData.offset, preFilterData.mask, preFilterData.maskbit);
                    } else {
                        appendToLog("preFilterData is null or disabled. Going to setSelectCriteriaDisable");
                        setSelectCriteriaDisable(0);
                    }
                }
                instream.close();
                if (DEBUG_FILE) appendToLog("Data is read from FILE.");
            } catch (Exception ex) {
                //
            }
        }
        if (bNeedDefault) {
            appendToLog("saveSetting2File default !!!");
            setReaderDefault();
            saveSetting2File();
        }
        return bNeedDefault;
    }
    void write2FileStream(FileOutputStream stream, String string) {
        boolean DEBUG = false;
        try {
            stream.write(string.getBytes()); if (DEBUG) appendToLog("outData = " + string);
        } catch (Exception ex) { }
    }
    public int getcsModel() {
        return bluetoothConnector.getCsModel();
    }
    public int getAntennaCycle() {
        return rfidReaderChip.rx000Setting.getAntennaCycle();
    }
    public boolean setAntennaCycle(int antennaCycle) {
        return rfidReaderChip.rx000Setting.setAntennaCycle(antennaCycle);
    }
    public boolean setAntennaInvCount(long antennaInvCount) {
        return rfidReaderChip.rx000Setting.setAntennaInvCount(antennaInvCount);
    }

    public void clearInvalidata() {
        csReaderConnector.invalidata = 0;
        csReaderConnector.invalidUpdata = 0;
        csReaderConnector.validata = 0;
    }
    public int getInvalidata() {
        return csReaderConnector.invalidata;
    }
    public int getInvalidUpdata() {
        return csReaderConnector.invalidUpdata;
    }
    public int getValidata() {
        return csReaderConnector.validata;
    }
}
