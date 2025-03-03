package com.csl.cslibrary4a;

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

import androidx.core.app.ActivityCompat;

import com.csl.cslibrary4a.RfidReader.RegionCodes;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Cs710Library4A {
    final boolean DEBUG = false;
    final boolean DEBUG_FILE = false;
    private Handler mHandler = new Handler();
    BluetoothAdapter.LeScanCallback mLeScanCallback = null;
    ScanCallback mScanCallback = null;
    //File file;

    Context context;
    CsReaderConnector csReaderConnector; Utility utility;
    boolean DEBUG_CONNECT, DEBUG_SCAN;
    BluetoothGatt bluetoothGatt;
    RfidReaderChipE710 rfidReaderChip; //RfidConnector rfidConnector;
    BarcodeNewland barcodeNewland; BarcodeConnector barcodeConnector;
    NotificationConnector notificationConnector;
    ControllerConnector controllerConnector;
    BluetoothConnector bluetoothConnector;
    public Cs710Library4A(Context context, TextView mLogView) {
        this.context = context;
        utility = new Utility(context, mLogView);
        csReaderConnector = new CsReaderConnector(context, mLogView, utility, false);
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
                        BluetoothGatt.CsScanData scanResultA = new BluetoothGatt.CsScanData(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        boolean found98 = true;
                        if (true) found98 = check9800(scanResultA);
                        if (DEBUG) appendToLog("found98 = " + found98 + ", mScanResultList 0 = " + (mScanResultList != null ? "VALID" : "NULL"));
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
                    BluetoothGatt.CsScanData scanResultA = new BluetoothGatt.CsScanData(device, rssi, scanRecord);
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
        if (true || DEBUG)
            appendToLog("Number of file in data storage sub-directory = " + fileArray.length);
        boolean bProfileInstalledFound = false;
        for (int i = 0; i < fileArray.length; i++) {
            String fileName = fileArray[i].toString();
            if (true) appendToLog("Stored file (" + i + ") = " + fileName);
            if (fileName.contains("profileInstalled") || fileName.contains("profileinstaller")) {
                bProfileInstalledFound = true;
                appendToLog("Found profileInstalled or profileinstaller file");
            }
            File file = new File(fileName);
            if (deleteFiles) file.delete();
        }
        if (!bProfileInstalledFound) {
            for (int i = 0; i < fileArray.length; i++) {
                String fileName = fileArray[i].toString();
                File file = new File(fileName);
                file.delete();
                appendToLog("Deleted " + fileName);
            }
        }
    }
    public String getlibraryVersion() {
        String version = BuildConfig.VERSION_NAME;
        //int iVersion = Integer.parseInt(version) + 10;
        version = "14.17"; //+ String.valueOf(iVersion);
        appendToLog("version = " + version);
        return utility.getCombinedVersion(version);
    }
    public String checkVersion() {
        String macVersion = getMacVer();
        String hostVersion = hostProcessorICGetFirmwareVersion();
        String bluetoothVersion = getBluetoothICFirmwareVersion();
        String strVersionRFID = "2.6.44"; String[] strRFIDVersions = strVersionRFID.split("\\.");
        String strVersionBT = "1.0.17"; String[] strBTVersions = strVersionBT.split("\\.");
        String strVersionHost = "1.0.16"; String[] strHostVersions = strVersionHost.split("\\.");
        String stringPopup = "";
        int icsModel = getcsModel();

        if (isRfidFailure() == false && checkHostProcessorVersion(macVersion, Integer.parseInt(strRFIDVersions[0].trim()), Integer.parseInt(strRFIDVersions[1].trim()), Integer.parseInt(strRFIDVersions[2].trim())) == false)
            stringPopup += "\nRFID processor firmware: V" + strVersionRFID;
        if (icsModel != 463) {
            if (checkHostProcessorVersion(hostVersion, Integer.parseInt(strHostVersions[0].trim()), Integer.parseInt(strHostVersions[1].trim()), Integer.parseInt(strHostVersions[2].trim())) == false)
                stringPopup += "\nAtmel firmware: V" + strVersionHost;
            if (checkHostProcessorVersion(bluetoothVersion, Integer.parseInt(strBTVersions[0].trim()), Integer.parseInt(strBTVersions[1].trim()), Integer.parseInt(strBTVersions[2].trim())) == false)
                stringPopup += "\nBluetooth firmware: V" + strVersionBT;
        }
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
    ArrayList<BluetoothGatt.CsScanData> mScanResultList = new ArrayList<>();
    int check9800_serviceUUID2p1 = 0;
    boolean bleConnection = false;
    ReaderDevice readerDeviceConnect;
    boolean bNeedReconnect = false;
    int iConnectStateTimer = 0;

    boolean check9800(BluetoothGatt.CsScanData scanResultA) {
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
        for (int i = 0; /*scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE &&*/ i < scanResultA.decoded_scanRecord.size(); i++) {
            byte[] currentAD = scanResultA.decoded_scanRecord.get(i);
            if (DEBUG) appendToLog("Processing decoded data = " + byteArrayToString(currentAD));
            if (currentAD[0] == 2) {
                if (DEBUG) appendToLog("Processing UUIDs 02");
                if ((currentAD[1] == 2) && currentAD[2] == (byte) 0x98) {
                    if (DEBUG) appendToLog("Found 9802");
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
            if (barcodeConnector == null) return;
            appendToLog("abcc disconnectRunnable with barcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size());
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
    public BluetoothGatt.CsScanData getNewDeviceScanned() {
        if (mScanResultList.size() != 0) {
            if (DEBUG_SCAN) appendToLog("mScanResultList.size() = " + mScanResultList.size());
            BluetoothGatt.CsScanData csScanData = mScanResultList.get(0); mScanResultList.remove(0);
            return csScanData;
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
        boolean DEBUG = true;
        boolean bleConnectionNew = csReaderConnector.isBleConnected();
        if (bleConnectionNew) {
            if (bleConnection == false) {
                bleConnection = bleConnectionNew;
                if (DEBUG_CONNECT || DEBUG) appendToLog("Newly connected");

                csReaderConnector.csConnectorDataInit();
                rfidReaderChip = csReaderConnector.rfidReader.rfidReaderChipE710;
                barcodeNewland = csReaderConnector.barcodeNewland;
                barcodeConnector = csReaderConnector.barcodeConnector;
                notificationConnector = csReaderConnector.notificationConnector;
                controllerConnector = csReaderConnector.controllerConnector;
                bluetoothConnector = csReaderConnector.bluetoothConnector;

                setRfidOn(true);
                setBarcodeOn(true);
                hostProcessorICGetFirmwareVersion();
                getBluetoothICFirmwareVersion();
                csReaderConnector.rfidReader.channelOrderType = -1;
                {
                //    getBarcodePreSuffix();
                //    getBarcodeReadingMode();
                //    getBarcodeSerial();
                    //getBarcodeNoDuplicateReading();
                    //getBarcodeDelayTimeOfEachReading();
                    //getBarcodeEnable2dBarCodes();
                    //getBarcodePrefixOrder();
                    //getBarcodeVersion();
                    //barcodeSendCommandLoadUserDefault();
                    //barcodeSendQuerySystem();
                //    barcodeNewland.barcodeSendCommandItf14Cksum();

                    notificationConnector.setBatteryAutoReport(true); //0xA003
                }
                abortOperation();
                //getHostProcessorICSerialNumber(); //0xb004 (but access Oem as bluetooth version is not got)
                //getMacVer();
                if (true) {
                    rfidReaderChip.rx000Setting.getAntennaPortConfig(0);
                    rfidReaderChip.rx000Setting.getAntennaPortConfig(1);
                    rfidReaderChip.rx000Setting.getSelectConfiguration(0);
                    rfidReaderChip.rx000Setting.getSelectConfiguration(1);
                    rfidReaderChip.rx000Setting.getSelectConfiguration(2);
                    rfidReaderChip.rx000Setting.getMultibankReadConfig(0);
                    rfidReaderChip.rx000Setting.getMultibankReadConfig(1);
                    rfidReaderChip.rx000Setting.getRx000AccessPassword();
                    rfidReaderChip.rx000Setting.getRx000KillPassword();
                    rfidReaderChip.rx000Setting.getDupElimRollWindow();
                    //rfidReaderChip.rx000Setting.getEventPacketUplinkEnable();
                    rfidReaderChip.rx000Setting.setEventPacketUplinkEnable((byte) 0x09);
                    rfidReaderChip.rx000Setting.getIntraPacketDelay();
                    rfidReaderChip.rx000Setting.getFrequencyChannelIndex();
                    rfidReaderChip.rx000Setting.getCurrentPort();
                }
                csReaderConnector.rfidReader.regionCode = null;
                getModelNumber();
                getCountryCode();
                //getSerialNumber();
                getQueryTarget();
                csReaderConnector.rfidReader.getImpinjExtension();
                csReaderConnector.rfidReader.getInvAlgoInChip();
                if (DEBUG_CONNECT || DEBUG) appendToLog("Start checkVersionRunnable");
                mHandler.postDelayed(checkVersionRunnable, 500);

                if (csReaderConnector.settingData.strForegroundReader.trim().length() != 0) {
                    csReaderConnector.settingData.strForegroundReader = bluetoothGatt.getmBluetoothDevice().getAddress();
                }
                csReaderConnector.settingData.saveForegroundSetting2File();
            } else if (rfidReaderChip == null) {
                bleConnection = false;
                appendToLog("rfidReaderChip is NULL");
            } else if (csReaderConnector.rfidReader.bFirmware_reset_before) {
                csReaderConnector.rfidReader.bFirmware_reset_before = false;
                mHandler.postDelayed(reinitaliseDataRunnable, 500);
            }
        } else if (bleConnection) {
            rfidReaderChip = null;
            barcodeNewland = null; barcodeConnector = null;
            notificationConnector = null;
            controllerConnector = null;
            bluetoothConnector = null;
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
                appendToLog("going to disconnectRunnable with remaining barcodeToWrite.size = " + barcodeConnector.barcodeToWrite.size() + ", data = " + byteArrayToString(barcodeConnector.barcodeToWrite.get(0).dataValues));
            }
            barcodeConnector.barcodeToWrite.clear(); appendToLog("barcodeToWrite is clear");
            setBarcodeOn(false);
        } else appendToLog("tempDisconnect: getBarcodeOnStatus is false");
        mHandler.postDelayed(disconnectRunnable, 100);
        appendToLog("done with tempDisconnect = " + tempDisconnect);
        if (tempDisconnect == false)    {
            mHandler.removeCallbacks(connectRunnable);
            bluetoothGatt.bluetoothDeviceConnectOld = null;
            if (readerDeviceConnect != null) bluetoothGatt.bluetoothDeviceConnectOld = bluetoothGatt.bluetoothAdapter.getRemoteDevice(readerDeviceConnect.getAddress());
            readerDeviceConnect = null;
        }
    }
    public boolean forceBTdisconnect() {
        return bluetoothConnector.forceBTdisconnect();
    }
    public int getRssi() {
        return bluetoothGatt.getRssi();
    }
    boolean getConnectionHSpeed() {
        return bluetoothGatt.getConnectionHSpeedA();
    }
    boolean setConnectionHSpeed(boolean on) {
        return bluetoothGatt.setConnectionHSpeedA(on);
    }
    public long getStreamInRate() {
        return csReaderConnector.getStreamInRate();
    }
    public int get98XX() {
        return 2;
    }

    //============ Rfid ============
    //============ Rfid ============
    //============ Rfid ============

    boolean setInvAlgoNoSave(boolean dynamicAlgo) {
        return csReaderConnector.rfidReader.setInvAlgoNoSave(dynamicAlgo);
    }
    boolean setInvAlgo1(boolean dynamicAlgo) {
        return csReaderConnector.rfidReader.setInvAlgo1(dynamicAlgo);
    }
    public String getAuthMatchData() {
        return csReaderConnector.rfidReader.getAuthMatchData();
    }
    public boolean setAuthMatchData(String mask) {
        return csReaderConnector.rfidReader.setAuthMatchData(mask);
    }
    public int getStartQValue() {
        return csReaderConnector.rfidReader.getStartQValue();
    }
    public int getMaxQValue() {
        return csReaderConnector.rfidReader.getMaxQValue();
    }
    public int getMinQValue() {
        return csReaderConnector.rfidReader.getMinQValue();
    }
    public boolean setDynamicQParms(int startQValue, int minQValue, int maxQValue, int retryCount) {
        return csReaderConnector.rfidReader.setDynamicQParms(startQValue, minQValue, maxQValue, retryCount);
    }
    public int getFixedQValue() {
        return csReaderConnector.rfidReader.getFixedQValue();
    }
    public int getFixedRetryCount() {
        return csReaderConnector.rfidReader.getFixedRetryCount();
    }
    public boolean getRepeatUnitNoTags() {
        return csReaderConnector.rfidReader.getRepeatUnitNoTags();
    }
    public boolean setFixedQParms(int qValue, int retryCount, boolean repeatUnitNoTags) {
        return csReaderConnector.rfidReader.setFixedQParms(qValue, retryCount, repeatUnitNoTags);
    }
    RegionCodes[] getRegionList() {
        return csReaderConnector.rfidReader.getRegionList();
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
            } else { appendToLog("disconnect H"); csReaderConnector.disconnect(); appendToLog("done"); }
            mHandler.postDelayed(runnableToggleConnection, 500);
        }
    };
    public boolean getChannelHoppingDefault() {
        return csReaderConnector.rfidReader.getChannelHoppingDefault();
    }
    int getCountryCode() {
        return csReaderConnector.rfidReader.getCountryCode();
    }
    int getFreqModifyCode() {
        return csReaderConnector.rfidReader.getFreqModifyCode();
    }
    public boolean getRfidOnStatus() {
        return csReaderConnector.rfidReader.getRfidOnStatus();
    }
    public boolean isRfidFailure() {
        //appendToLog("BtDataOut: isRfidFailure rfidReader is " + (csReaderConnector.rfidReader == null ? "null" : csReaderConnector.rfidReader.isRfidFailure()));
        if (csReaderConnector.rfidReader == null) return false;
        return csReaderConnector.rfidReader.isRfidFailure();
    }
    public void setReaderDefault() {
        if (true) csReaderConnector.rfidReader.setReaderDefault();
        else {
        setPowerLevel(300);
        setTagGroup(0, 0, 2);
        setPopulation(60);
        setInvAlgoNoSave(true);
        setBasicCurrentLinkProfile();
        }
        String string = bluetoothGatt.getmBluetoothDevice().getAddress();
        string = string.replaceAll("[^a-zA-Z0-9]","");
        string = string.substring(string.length()-6, string.length());
        setBluetoothICFirmwareName("CS710Sreader" + string);
        //getlibraryVersion()
        if (false) {
        setCountryInList(csReaderConnector.rfidReader.countryInListDefault);
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
        }
        //getBluetoothICFirmwareName() + "\n");
        setTagDelay(csReaderConnector.rfidReader.tagDelaySettingDefault);
        setCycleDelay((long)0);
        setIntraPkDelay((byte)4);
        setDupDelay((byte)0);

        setBatteryDisplaySetting(csReaderConnector.settingData.batteryDisplaySelectDefault);
        setRssiDisplaySetting(csReaderConnector.settingData.rssiDisplaySelectDefault);
        notificationConnector.setTriggerReporting(csReaderConnector.settingData.triggerReportingDefault);
        notificationConnector.setTriggerReportingCount(csReaderConnector.settingData.triggerReportingCountSettingDefault);
        setInventoryBeep(csReaderConnector.settingData.inventoryBeepDefault);
        setBeepCount(csReaderConnector.settingData.beepCountSettingDefault);
        setInventoryVibrate(csReaderConnector.settingData.inventoryVibrateDefault);
        setVibrateTime(csReaderConnector.settingData.vibrateTimeSettingDefault);
        setVibrateModeSetting(csReaderConnector.settingData.vibrateModeSelectDefault);
        setVibrateWindow(csReaderConnector.settingData.vibrateWindowSettingDefault);

        setSavingFormatSetting(csReaderConnector.settingData.savingFormatSelectDefault);
        setCsvColumnSelectSetting(csReaderConnector.settingData.csvColumnSelectDefault);
        setSaveFileEnable(csReaderConnector.settingData.saveFileEnableDefault);
        setSaveCloudEnable(csReaderConnector.settingData.saveCloudEnableDefault);
        setSaveNewCloudEnable(csReaderConnector.settingData.saveNewCloudEnableDefault);
        setSaveAllCloudEnable(csReaderConnector.settingData.saveAllCloudEnableDefault);
        setServerLocation(csReaderConnector.settingData.serverLocationDefault);
        setServerTimeout(csReaderConnector.settingData.serverTimeoutDefault);
        csReaderConnector.settingData.barcode2TriggerMode = csReaderConnector.settingData.barcode2TriggerModeDefault;

        setUserDebugEnable(csReaderConnector.settingData.userDebugEnableDefault);
        csReaderConnector.settingData.preFilterData = null;
    }
    public String getMacVer() {
        return csReaderConnector.rfidReader.getMacVer();
    }
    public String getRadioSerial() {
        return csReaderConnector.rfidReader.getRadioSerial();
    }
    public String getRadioBoardVersion() {
        return csReaderConnector.rfidReader.getRadioBoardVersion();
    }
    public int getPortNumber() {
        if (bluetoothConnector.getCsModel() == 463) return 4;
        else return 1;
    }
    public int getAntennaSelect() {
        return csReaderConnector.rfidReader.getAntennaSelect();
    }
    public boolean setAntennaSelect(int number) {
        return csReaderConnector.rfidReader.setAntennaSelect(number);
    }
    public boolean getAntennaEnable() {
        return csReaderConnector.rfidReader.getAntennaEnable();
    }
    public boolean setAntennaEnable(boolean enable) {
        return csReaderConnector.rfidReader.setAntennaEnable(enable);
    }
    public long getAntennaDwell() {
        long lValue = 0; boolean DEBUG = false;
        if (DEBUG) appendToLog("1 getAntennaDwell");
        lValue = rfidReaderChip.rx000Setting.getAntennaDwell();
        if (DEBUG) appendToLog("1A getAntennaDwell: lValue = " + lValue);
        return lValue;
    }
    public boolean setAntennaDwell(long antennaDwell) {
        boolean bValue = false, DEBUG = false;
        if (DEBUG) appendToLog("1 AntennaDwell = " + antennaDwell + " returning " + bValue);
        bValue =  rfidReaderChip.rx000Setting.setAntennaDwell(antennaDwell);
        if (DEBUG) appendToLog("1A AntennaDwell = " + antennaDwell + " returning " + bValue);
        return bValue;
    }
    public long getPwrlevel() {
        if (true) return csReaderConnector.rfidReader.getPwrlevel();
        long lValue = 0;
        lValue = rfidReaderChip.rx000Setting.getAntennaPower(-1);
        return lValue;
    }
    //long pwrlevelSetting;
    public boolean setPowerLevel(long pwrlevel) {
        return csReaderConnector.rfidReader.setPowerLevel(pwrlevel);
    }
    public int getQueryTarget() {
        return csReaderConnector.rfidReader.getQueryTarget();
    }
    public int getQuerySession() {
        if (true) return csReaderConnector.rfidReader.getQuerySession();
        return rfidReaderChip.rx000Setting.getQuerySession();
    }
    public int getQuerySelect() {
        if (true) return csReaderConnector.rfidReader.getQuerySelect();
        return rfidReaderChip.rx000Setting.getQuerySelect();
    }
    public boolean setTagGroup(int sL, int session, int target1) {
        //appendToLog("1d");
        int iAlgoAbFlip = rfidReaderChip.rx000Setting.getAlgoAbFlip();
        appendToLog("sL = " + sL + ", session = " + session + ", target = " + target1 + ", getAlgoAbFlip = " + iAlgoAbFlip);
        boolean bValue = false;
        bValue = rfidReaderChip.rx000Setting.setQueryTarget(target1, session, sL);
        if (bValue) {
            if (iAlgoAbFlip != 0 && target1 < 2) bValue = rfidReaderChip.rx000Setting.setAlgoAbFlip(0);
            else if (iAlgoAbFlip == 0 && target1 >= 2) bValue = rfidReaderChip.rx000Setting.setAlgoAbFlip(1);
        }
        return bValue;
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
        appendToLog("bFastId: setFastId[" + fastIdNew);
        return csReaderConnector.rfidReader.setFastId(fastIdNew);
    }
    public boolean getInvAlgo() {
        return csReaderConnector.rfidReader.getInvAlgo();
    }
    public boolean setInvAlgo(boolean dynamicAlgo) {
        return csReaderConnector.rfidReader.setInvAlgo(dynamicAlgo);
    }
    public List<String> getProfileList() {
        return csReaderConnector.rfidReader.getProfileList();
    }
    public int getCurrentProfile() {
        return csReaderConnector.rfidReader.getCurrentProfile();
    }
    public boolean setBasicCurrentLinkProfile() {
        return csReaderConnector.rfidReader.setBasicCurrentLinkProfile();
    }
    public boolean setCurrentLinkProfile(int profile) {
        return csReaderConnector.rfidReader.setCurrentLinkProfile(profile);
    }
    public void resetEnvironmentalRSSI() {
        csReaderConnector.rfidReader.resetEnvironmentalRSSI();
    }
    public String getEnvironmentalRSSI() {
        return csReaderConnector.rfidReader.getEnvironmentalRSSI();
    }
    public int getHighCompression() {
        return csReaderConnector.rfidReader.getHighCompression();
    }
    public int getRflnaGain() {
        return csReaderConnector.rfidReader.getRflnaGain();
    }
    public int getIflnaGain() {
        return csReaderConnector.rfidReader.getIflnaGain();
    }
    public int getAgcGain() {
        return csReaderConnector.rfidReader.getAgcGain();
    }
    public int getRxGain() {
        return csReaderConnector.rfidReader.getRxGain();
    }
    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        return csReaderConnector.rfidReader.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
    }
    public boolean setRxGain(int rxGain) {
        return csReaderConnector.rfidReader.setRxGain(rxGain);
    }
    public int FreqChnCnt() {
        return csReaderConnector.rfidReader.FreqChnCnt(csReaderConnector.rfidReader.regionCode);
    }
    public double getLogicalChannel2PhysicalFreq(int channel) {
        return csReaderConnector.rfidReader.getLogicalChannel2PhysicalFreq(channel);
    }
    public byte getTagDelay() {
        return csReaderConnector.rfidReader.getTagDelay();
    }
    public boolean setTagDelay(byte tagDelay) {
        return csReaderConnector.rfidReader.setTagDelay(tagDelay);
    }
    public byte getIntraPkDelay() {
        return csReaderConnector.rfidReader.getIntraPkDelay();
    }
    public boolean setIntraPkDelay(byte intraPkDelay) {
        return csReaderConnector.rfidReader.setIntraPkDelay(intraPkDelay);
    }
    public byte getDupDelay() {
        return csReaderConnector.rfidReader.getDupDelay();
    }
    public boolean setDupDelay(byte dupElim) {
        return csReaderConnector.rfidReader.setDupDelay(dupElim);
    }
    public long getCycleDelay() {
        return csReaderConnector.rfidReader.getCycleDelay();
    }
    public boolean setCycleDelay(long cycleDelay) {
        return csReaderConnector.rfidReader.setCycleDelay(cycleDelay);
    }
    public void getAuthenticateReplyLength() {
        csReaderConnector.rfidReader.getAuthenticateReplyLength();
    }
    public boolean setTamConfiguration(boolean header, String matchData) {
        return csReaderConnector.rfidReader.setTamConfiguration(header, matchData);
    }
    public boolean setTam1Configuration(int keyId, String matchData) {
        return csReaderConnector.rfidReader.setTam1Configuration(keyId, matchData);
    }
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        return csReaderConnector.rfidReader.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
    }
    public int getUntraceableEpcLength() {
        return csReaderConnector.rfidReader.getUntraceableEpcLength();
    }
    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        return csReaderConnector.rfidReader.setUntraceable(bHideEpc, ishowEpcSize, iHideTid, bHideUser, bHideRange);
    }
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        return csReaderConnector.rfidReader.setUntraceable(range, user, tid, epcLength, epc, uxpc);
    }
    public boolean setAuthenticateConfiguration() {
        if (true) return csReaderConnector.rfidReader.setAuthenticateConfiguration();
        appendToLog("setAuthenticateConfiguration Started");
        boolean bValue = rfidReaderChip.rx000Setting.setAuthenticateConfig((48 << 10) | (1 << 2) | 0x03);
        appendToLog("setAuthenticateConfiguration 1: bValue = " + (bValue ? "true" : "false"));
        if (bValue) {
            bValue = rfidReaderChip.rx000Setting.setAuthenticateMessage(new byte[] { 0x04, (byte)0x9C, (byte)0xA5, 0x3E, 0x55, (byte)0xEA } );
            appendToLog("setAuthenticateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
        }
        if (bValue) {
            bValue = rfidReaderChip.rx000Setting.setAuthenticateResponseLen(16 * 8);
            appendToLog("setAuthenticateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
        }
        return bValue;
    }
    public int getRetryCount() {
        return csReaderConnector.rfidReader.getRetryCount();
    }
    public boolean setRetryCount(int retryCount) {
        return csReaderConnector.rfidReader.setRetryCount(retryCount);
    }
    public int getInvSelectIndex() {
        return csReaderConnector.rfidReader.getInvSelectIndex();
    }
    public boolean getSelectEnable() {
        if (true) return csReaderConnector.rfidReader.getSelectEnable();
        int iValue;
        iValue = rfidReaderChip.rx000Setting.getSelectEnable();
        if (iValue < 0) return false;
        return iValue != 0 ? true : false;
    }
    public int getSelectTarget() {
        return csReaderConnector.rfidReader.getSelectTarget();
    }
    public int getSelectAction() {
        return csReaderConnector.rfidReader.getSelectAction();
    }
    public int getSelectMaskBank() {
        return csReaderConnector.rfidReader.getSelectMaskBank();
    }
    public int getSelectMaskOffset() {
        return csReaderConnector.rfidReader.getSelectMaskOffset();
    }
    public String getSelectMaskData() {
        return csReaderConnector.rfidReader.getSelectMaskData();
    }
    public boolean setInvSelectIndex(int invSelect) {
        return csReaderConnector.rfidReader.setInvSelectIndex(invSelect);
    }
    public boolean setSelectCriteriaDisable(int index) {
        if (utility.DEBUG_SELECT) appendToLog("Debug_Select: Cs710Library4A.setSelectCriteriaDisable[" + index + "] goes to setSelectCriteriaDisable");
        return csReaderConnector.rfidReader.setSelectCriteriaDisable(index);
    }
    int findFirstEmptySelect() {
        return csReaderConnector.rfidReader.findFirstEmptySelect();
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        return csReaderConnector.rfidReader.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        return csReaderConnector.rfidReader.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
    }
    public boolean getRssiFilterEnable() {
        return csReaderConnector.rfidReader.getRssiFilterEnable();
    }
    public int getRssiFilterType() {
        return csReaderConnector.rfidReader.getRssiFilterType();
    }
    public int getRssiFilterOption() {
        return csReaderConnector.rfidReader.getRssiFilterOption();
    }
    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        return csReaderConnector.rfidReader.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
    }
    public double getRssiFilterThreshold1() {
        return csReaderConnector.rfidReader.getRssiFilterThreshold1();
    }
    public double getRssiFilterThreshold2() {
        return csReaderConnector.rfidReader.getRssiFilterThreshold2();
    }
    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        return csReaderConnector.rfidReader.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
    }
    public long getRssiFilterCount() {
        return csReaderConnector.rfidReader.getRssiFilterCount();
    }
    public boolean setRssiFilterCount(long rssiFilterCount) {
        return csReaderConnector.rfidReader.setRssiFilterCount(rssiFilterCount);
    }
    public boolean getInvMatchEnable() {
        return csReaderConnector.rfidReader.getInvMatchEnable();
    }
    public boolean getInvMatchType() {
        return csReaderConnector.rfidReader.getInvMatchType();
    }
    public int getInvMatchOffset() {
        return csReaderConnector.rfidReader.getInvMatchOffset();
    }
    public String getInvMatchData() {
        return csReaderConnector.rfidReader.getInvMatchData();
    }
    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        return csReaderConnector.rfidReader.setPostMatchCriteria(enable, target, offset, mask);
    }
    public int mrfidToWriteSize() {
        if (isBleConnected() == false) return -1;
        if (csReaderConnector.rfidReader == null) return -1;
        return csReaderConnector.rfidReader.mrfidToWriteSize();
    }
    public void mrfidToWritePrint() {
        if (true) { csReaderConnector.rfidReader.mrfidToWriteSize(); return; }
        for (int i = 0; i < csReaderConnector.rfidReader.mRfidToWrite.size(); i++) {
            appendToLog(byteArrayToString(csReaderConnector.rfidReader.mRfidToWrite.get(i).dataValues));
        }
    }
    public long getTagRate() {
        return csReaderConnector.rfidReader.getTagRate();
    }
    public boolean startOperation(RfidReaderChipData.OperationTypes operationTypes) {
        return csReaderConnector.rfidReader.startOperation(operationTypes);
    }
    public boolean abortOperation() {
        return csReaderConnector.rfidReader.abortOperation();
    }
    public void restoreAfterTagSelect() {
        if (!isBleConnected()) return;
        if (utility.DEBUG_SELECT) appendToLog("Debug_Select: Cs710Library4A.restoreAfterTagSelect goes to setSelectCriteriaDisable");
        setSelectCriteriaDisable(0); setSelectCriteriaDisable(1); setSelectCriteriaDisable(2);
        loadSetting1File();
        setAccessCount(0);
        setRx000AccessPassword("00000000");
        if (checkHostProcessorVersion(getMacVer(), 2, 6, 8)) {
            rfidReaderChip.rx000Setting.setMatchRep(0);
            rfidReaderChip.rx000Setting.setTagDelay(csReaderConnector.rfidReader.tagDelaySetting);
            rfidReaderChip.rx000Setting.setCycleDelay(csReaderConnector.rfidReader.cycleDelaySetting);
            rfidReaderChip.rx000Setting.setInvModeCompact(true);
        }
        if (csReaderConnector.rfidReader.postMatchDataChanged) {
            csReaderConnector.rfidReader.postMatchDataChanged = false;
            setPostMatchCriteria(csReaderConnector.rfidReader.postMatchDataOld.enable, csReaderConnector.rfidReader.postMatchDataOld.target, csReaderConnector.rfidReader.postMatchDataOld.offset, csReaderConnector.rfidReader.postMatchDataOld.mask);
            appendToLog("PowerLevel");
            setPowerLevel(csReaderConnector.rfidReader.postMatchDataOld.pwrlevel);
            appendToLog("writeBleStreamOut: invAlgo = " + csReaderConnector.rfidReader.postMatchDataOld.invAlgo); setInvAlgo1(csReaderConnector.rfidReader.postMatchDataOld.invAlgo);
            setQValue1(csReaderConnector.rfidReader.postMatchDataOld.qValue);
        }
    }
    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        return csReaderConnector.rfidReader.setSelectedTagByTID(strTagId, pwrlevel);
    }
    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        return csReaderConnector.rfidReader.setSelectedTag(strTagId, selectBank, pwrlevel);
    }
    public boolean setSelectedTag(String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        return csReaderConnector.rfidReader.setSelectedTag4Access(false, selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
    }
    public boolean setMatchRep(int matchRep) {
        if (utility.DEBUG_INVCFG) appendToLog("Debug_InvCfg: Cs710Library4A.setMatchRep goes to setMatchRep with matchRep = " + matchRep);
        return csReaderConnector.rfidReader.setMatchRep(matchRep);
    }
    public String[] getCountryList() {
        return csReaderConnector.rfidReader.getCountryList();
    }
    public int getCountryNumberInList() {
        return csReaderConnector.rfidReader.countryInList;
    }
    public boolean setCountryInList(int countryInList) {
        return csReaderConnector.rfidReader.setCountryInList(countryInList);
    }
    public boolean getChannelHoppingStatus() {
        if (true) csReaderConnector.rfidReader.getChannelHoppingStatus();
        boolean bValue = false, DEBUG = false;
        int iValue = rfidReaderChip.rx000Setting.getCountryEnum(); //iValue--;
        if (DEBUG) appendToLog("getChannelHoppingStatus: countryEnum = " + iValue);
        if (iValue > 0) {
            String strFixedHop = csReaderConnector.rfidReader.countryChannelData.strCountryEnumInfo[(iValue - 1) * csReaderConnector.rfidReader.countryChannelData.iCountryEnumInfoColumn + 4];
            if (DEBUG) appendToLog("getChannelHoppingStatus: FixedHop = " + strFixedHop);
            if (strFixedHop.matches("Hop")) {
                if (DEBUG) appendToLog("getChannelHoppingStatus: matched");
                bValue = true;
            }
        }
        if (DEBUG) appendToLog("getChannelHoppingStatus: bValue = " + bValue);
        return bValue; //1 for hopping, 0 for fixed
    }
    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        if (true) return csReaderConnector.rfidReader.setChannelHoppingStatus(channelOrderHopping);
        if (csReaderConnector.rfidReader.channelOrderType != (channelOrderHopping ? 0 : 1)) {
            boolean result = true;
            if (getChannelHoppingDefault() == false) {
                result = rfidReaderChip.rx000Setting.setAntennaFreqAgile(channelOrderHopping ? 1 : 0);
            }
            int freqcnt = FreqChnCnt(); appendToLog("FrequencyA Count = " + freqcnt);
            int channel = getChannel(); appendToLog(" FrequencyA Channel = " + channel);
            appendToLog(" FrequencyA: end of setting");

            csReaderConnector.rfidReader.channelOrderType = (channelOrderHopping ? 0 : 1);
            appendToLog("setChannelHoppingStatus: channelOrderType = " + csReaderConnector.rfidReader.channelOrderType);
        }
        return true;
    }
    public String[] getChannelFrequencyList() {
        return csReaderConnector.rfidReader.getChannelFrequencyList();
    }
    public int getChannel() {
        return csReaderConnector.settingData.channel;
    }
    public boolean setChannel(int channelSelect) {
        return csReaderConnector.rfidReader.setChannel(channelSelect);
    }
    public byte getPopulation2Q(int population) {
        return csReaderConnector.rfidReader.getPopulation2Q(population);
    }
    public int getPopulation() {
        return csReaderConnector.rfidReader.getPopulation();
    }
    public boolean setPopulation(int population) {
        return csReaderConnector.rfidReader.setPopulation(population);
    }
    public byte getQValue() {
        return csReaderConnector.rfidReader.qValueSetting;
    }
    public boolean setQValue(byte byteValue) {
        return csReaderConnector.rfidReader.setQValue(byteValue);
    }
    int getQValue1() {
        return csReaderConnector.rfidReader.getQValue();
    }
    boolean setQValue1(int iValue) {
        return csReaderConnector.rfidReader.setQValue1(iValue);
    }
    public RfidReaderChipData.Rx000pkgData onRFIDEvent() {
        boolean DEBUG = false;
        RfidReaderChipData.Rx000pkgData rx000pkgData = null;
        //if (mrfidToWriteSize() != 0) mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000ToRead.clear();
        if (rfidReaderChip.bRx000ToReading == false && rfidReaderChip.mRx000ToRead.size() != 0) {
            rfidReaderChip.bRx000ToReading = true;
            int index = 0;
            try {
                rx000pkgData = rfidReaderChip.mRx000ToRead.get(index);
                if (false && rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END)
                    if (DEBUG) appendToLog("get mRx000ToRead with COMMAND_END");
                rfidReaderChip.mRx000ToRead.remove(index);
                if (DEBUG) appendToLog("got one mRx000ToRead with responseType = " + rx000pkgData.responseType.toString() + ", and remained size = " + rfidReaderChip.mRx000ToRead.size());
            } catch (Exception ex) {
                rx000pkgData = null;
            }
            rfidReaderChip.bRx000ToReading = false;
        }
        if (rx000pkgData != null && rx000pkgData.responseType != null) {
            if (rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY || rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) {
                if (DEBUG) appendToLog("Before adjustment, decodedRssi = " + rx000pkgData.decodedRssi);
                rx000pkgData.decodedRssi += dBuV_dBm_constant;
                if (DEBUG) appendToLog("After adjustment, decodedRssi = " + rx000pkgData.decodedRssi);
                if (rfidReaderChip.rx000Setting.getInvMatchEnable() > 0) {
                    byte[] bytesCompared = new byte[rx000pkgData.decodedEpc.length];
                    System.arraycopy(rx000pkgData.decodedEpc, 0, bytesCompared, 0, rx000pkgData.decodedEpc.length);
                    //bytesCompared = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x2F };
                    //bytesCompared = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0x2F };
                    appendToLog("decodedEpc = " + byteArrayToString(rx000pkgData.decodedEpc));
                    if (rfidReaderChip.rx000Setting.getInvMatchOffset() > 0) {
                        appendToLog("getInvMatchOffset = " + rfidReaderChip.rx000Setting.getInvMatchOffset());
                        BigInteger bigInt = new BigInteger(bytesCompared);
                        BigInteger shiftInt = bigInt.shiftLeft(rfidReaderChip.rx000Setting.getInvMatchOffset());
                        byte [] shifted = shiftInt.toByteArray();
                        appendToLog("shifted = " + byteArrayToString(shifted));
                        if (shifted.length > bytesCompared.length) System.arraycopy(shifted, shifted.length - bytesCompared.length, bytesCompared, 0, bytesCompared.length);
                        else if (shifted.length < bytesCompared.length) {
                            System.arraycopy(shifted, 0, bytesCompared, bytesCompared.length - shifted.length, shifted.length);
                            for (int i = 0; i < bytesCompared.length - shifted.length; i++) {
                                if ((shifted[0] & 0x80) == 0) bytesCompared[i] = 0;
                                else bytesCompared[i] = (byte)0xFF;
                            }
                        }
                        appendToLog("new bytesCompared 1 = " + byteArrayToString(bytesCompared));
                    }

                    if (rfidReaderChip.rx000Setting.getInvMatchType() > 0) {
                        appendToLog("getInvMatchType = " + rfidReaderChip.rx000Setting.getInvMatchType());
                        for (int i = 0; i < bytesCompared.length; i++) {
                            bytesCompared[i] ^= (byte) 0xFF;
                        }
                    }
                    appendToLog("new bytesCompared 2 = " + byteArrayToString(bytesCompared));
                    appendToLog("getInvMatchData = " + rfidReaderChip.rx000Setting.getInvMatchData());
                    if (byteArrayToString(bytesCompared).indexOf(rfidReaderChip.rx000Setting.getInvMatchData()) != 0) {
                        appendToLog("Post Mis-Matched !!!");
                        rx000pkgData = null;
                    }
                }
            }
        }
        if (rx000pkgData != null && DEBUG) appendToLog("response = " + rx000pkgData.responseType.toString() + ", " + byteArrayToString(rx000pkgData.dataValues));
        return rx000pkgData;
    }
    public String getModelNumber() {
        return csReaderConnector.rfidReader.getModelNumber(getModelName());
    }
    public boolean setRx000KillPassword(String password) {
        return csReaderConnector.rfidReader.setRx000KillPassword(password);
    }
    public boolean setRx000AccessPassword(String password) {
        return csReaderConnector.rfidReader.setRx000AccessPassword(password);
    }
    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        return csReaderConnector.rfidReader.setAccessRetry(accessVerfiy, accessRetry);
    }
    public boolean setInvModeCompact(boolean invModeCompact) {
        if (utility.DEBUG_COMPACT) appendToLog("Debug_Compact 2: Cs710Library4A.setInvModeCompact goes to setInvModeCompact");
        return csReaderConnector.rfidReader.setInvModeCompact(invModeCompact);
    }
    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        return csReaderConnector.rfidReader.setAccessLockAction(accessLockAction, accessLockMask);
    }
    public boolean setAccessBank(int accessBank) {
        return csReaderConnector.rfidReader.setAccessBank(accessBank);
    }
    public boolean setAccessBank(int accessBank, int accessBank2) {
        return csReaderConnector.rfidReader.setAccessBank(accessBank, accessBank2);
    }
    public boolean setAccessOffset(int accessOffset) {
        return csReaderConnector.rfidReader.setAccessOffset(accessOffset);
    }
    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        return csReaderConnector.rfidReader.setAccessOffset(accessOffset, accessOffset2);
    }
    public boolean setAccessCount(int accessCount) {
        return csReaderConnector.rfidReader.setAccessCount(accessCount);
    }
    public boolean setAccessCount(int accessCount, int accessCount2) {
        return csReaderConnector.rfidReader.setAccessCount(accessCount, accessCount2);
    }
    public boolean setAccessWriteData(String dataInput) {
        return csReaderConnector.rfidReader.setAccessWriteData(dataInput);
    }
    public boolean setResReadNoReply(boolean resReadNoReply) {
        return csReaderConnector.rfidReader.setResReadNoReply(resReadNoReply);
    }
    public boolean setTagRead(int tagRead) {
        return csReaderConnector.rfidReader.setTagRead(tagRead);
    }
    public boolean setInvBrandId(boolean invBrandId) {
        return csReaderConnector.rfidReader.setInvBrandId(invBrandId);
    }
    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        return csReaderConnector.rfidReader.sendHostRegRequestHST_CMD(hostCommand);
    }
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        if (isBleConnected() == false) return false;
        return csReaderConnector.rfidReader.setPwrManagementMode(bLowPowerStandby);
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
        if (barcodeConnector == null) return false;
    	return barcodeConnector.barcodeFailure;
    }
    public String getBarcodeDate() {
        return barcodeNewland.getBarcodeDate();
    }
    public boolean getBarcodeOnStatus() {
    	if (barcodeConnector == null) return false;
        return barcodeConnector.getOnStatus();
    }
    public boolean setBarcodeOn(boolean on) {
        boolean retValue;
        BarcodeConnector.CsReaderBarcodeData csReaderBarcodeData = new BarcodeConnector.CsReaderBarcodeData();
        if (on) csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_POWER_ON;
        else    csReaderBarcodeData.barcodePayloadEvent = BarcodeConnector.BarcodePayloadEvents.BARCODE_POWER_OFF;
        csReaderBarcodeData.waitUplinkResponse = false;
        retValue = barcodeConnector.barcodeToWrite.add(csReaderBarcodeData); appendToLog("barcodeToWrite added with size = " + barcodeConnector.barcodeToWrite.size());
        if (DEBUG_FILE) appendToLog("add " + csReaderBarcodeData.barcodePayloadEvent.toString() + " to barcodeToWrite with length = " + barcodeConnector.barcodeToWrite.size());
        boolean continuousAfterOn = false;
        if (retValue && on && continuousAfterOn) {
            if (checkHostProcessorVersion(getBluetoothICFirmwareVersion(), 1, 0, 2)) {
                if (DEBUG) appendToLog("to barcodeSendCommandConinuous()");
                retValue = barcodeNewland.barcodeSendCommandConinuous();
            } else retValue = false;
        }
        if (DEBUG) appendToLog("barcodeToWrite size = " + barcodeConnector.barcodeToWrite.size());
        return retValue;
    }
    int iModeSet = -1, iVibratieTimeSet = -1;
    public boolean setVibrateOn(int mode) {
        boolean retValue;
        if (true) appendToLog("setVibrateOn with mode = " + mode + ", and isInventoring = " + csReaderConnector.rfidReader.isInventoring());
        if (csReaderConnector.rfidReader.isInventoring()) return false;
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
        if (DEBUG) appendToLog("barcodeToWrite size = " + barcodeConnector.barcodeToWrite.size());
        if (retValue) {
            iModeSet = mode; iVibratieTimeSet = getVibrateTime();
        }
        return retValue;
    }
    public boolean getInventoryVibrate() {
        return csReaderConnector.settingData.inventoryVibrate;
    }
    public boolean setInventoryVibrate(boolean inventoryVibrate) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("this.inventoryVibrate = " + csReaderConnector.settingData.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate);
        csReaderConnector.settingData.inventoryVibrate = inventoryVibrate;
        if (DEBUG) appendToLog("this.inventoryVibrate = " + csReaderConnector.settingData.inventoryVibrate + ", inventoryVibrate = " + inventoryVibrate);
        return true;
    }
    public int getVibrateTime() {
        return csReaderConnector.settingData.vibrateTimeSetting;
    }
    public boolean setVibrateTime(int vibrateTime) {
        csReaderConnector.settingData.vibrateTimeSetting = vibrateTime;
        return true;
    }
    public int getVibrateWindow() {
        return csReaderConnector.settingData.vibrateWindowSetting;
    }
    public boolean setVibrateWindow(int vibrateWindow) {
        csReaderConnector.settingData.vibrateWindowSetting = vibrateWindow;
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
            if (csReaderConnector.settingData.barcode2TriggerMode && result) {
                if (getTriggerButtonStatus() && notificationConnector.getAutoBarStartSTop()) {  appendToLog("TTestPoint 2"); barcodeAutoStarted = true; result = true; }
                else {  appendToLog("TTestPoint 3"); result = barcodeNewland.barcodeSendCommand(new byte[]{0x1b, 0x33}); }
            } else  appendToLog("TTestPoint 4");
            appendToLog("TTestPoint 5");
        } else {
            appendToLog("getBarcodeOnStatus = " + getBarcodeOnStatus() + ", result = " + result);
            if (csReaderConnector.settingData.barcode2TriggerMode == false) {  appendToLog("TTestPoint 6"); result = setBarcodeOn(false); }
            else if (getBarcodeOnStatus() == false && result) {  appendToLog("TTestPoint 7"); result = setBarcodeOn(true); }
            appendToLog("barcode2TriggerMode = " + csReaderConnector.settingData.barcode2TriggerMode + ", result = " + result + ", barcodeAutoStarted = " + barcodeAutoStarted);
            if (csReaderConnector.settingData.barcode2TriggerMode && result) {
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
        if (false) appendToLog("new sameCheck = " + sameCheck1 + ", with old sameCheck = " + csReaderConnector.sameCheck);
        csReaderConnector.sameCheck = sameCheck1; //sameCheck = false;
    }

    public int getBeepCount() {
        return csReaderConnector.settingData.beepCountSetting;
    }
    public boolean setBeepCount(int beepCount) {
        csReaderConnector.settingData.beepCountSetting = beepCount;
        return true;
    }

    public boolean getInventoryBeep() {
        return csReaderConnector.settingData.inventoryBeep;
    }
    public boolean setInventoryBeep(boolean inventoryBeep) {
        csReaderConnector.settingData.inventoryBeep = inventoryBeep;
        return true;
    }

    public boolean getSaveFileEnable() {
        return csReaderConnector.settingData.saveFileEnable;
    }
    public boolean setSaveFileEnable(boolean saveFileEnable) {
        appendToLog("this.saveFileEnable = " + csReaderConnector.settingData.saveFileEnable + ", saveFileEnable = " + saveFileEnable);
        csReaderConnector.settingData.saveFileEnable = saveFileEnable;
        appendToLog("this.saveFileEnable = " + csReaderConnector.settingData.saveFileEnable + ", saveFileEnable = " + saveFileEnable);
        return true;
    }
    public boolean getSaveCloudEnable() {
        return csReaderConnector.settingData.saveCloudEnable;
    }
    public boolean setSaveCloudEnable(boolean saveCloudEnable) {
        csReaderConnector.settingData.saveCloudEnable = saveCloudEnable;
        return true;
    }
    public boolean getSaveNewCloudEnable() {
        return csReaderConnector.settingData.saveNewCloudEnable;
    }
    public boolean setSaveNewCloudEnable(boolean saveNewCloudEnable) {
        csReaderConnector.settingData.saveNewCloudEnable = saveNewCloudEnable;
        return true;
    }
    public boolean getSaveAllCloudEnable() {
        return csReaderConnector.settingData.saveAllCloudEnable;
    }
    public boolean setSaveAllCloudEnable(boolean saveAllCloudEnable) {
        csReaderConnector.settingData.saveAllCloudEnable = saveAllCloudEnable;
        return true;
    }
    public boolean getUserDebugEnable() {
        boolean bValue = csReaderConnector.settingData.userDebugEnable; appendToLog("bValue = " + bValue); return bValue;
    }
    public boolean setUserDebugEnable(boolean userDebugEnable) {
        appendToLog("new userDebug = " + userDebugEnable);
        csReaderConnector.settingData.userDebugEnable = userDebugEnable;
        return true;
    }
    public String getForegroundReader() {
        return csReaderConnector.settingData.strForegroundReader;
    }
    public boolean getForegroundServiceEnable() {
        String string = csReaderConnector.settingData.strForegroundReader;
        return (string.trim().length() == 0 ? false : true);
    }
    public boolean setForegroundServiceEnable(boolean bForegroundService) {
        if (bForegroundService) csReaderConnector.settingData.strForegroundReader = csReaderConnector.bluetoothGatt.getmBluetoothDevice().getAddress();
        else csReaderConnector.settingData.strForegroundReader = "";
        return true;
    }
    public String getServerLocation() {
        return csReaderConnector.settingData.serverLocation;
    }
    public boolean setServerLocation(String serverLocation) {
        csReaderConnector.settingData.serverLocation = serverLocation;
        return true;
    }
    public int getServerTimeout() {
        return csReaderConnector.settingData.serverTimeout;
    }
    public boolean setServerTimeout(int serverTimeout) {
        csReaderConnector.settingData.serverTimeout = serverTimeout;
        return true;
    }
    public String getServerMqttLocation() {
        return csReaderConnector.settingData.serverMqttLocation;
    }
    public boolean setServerMqttLocation(String serverLocation) {
        csReaderConnector.settingData.serverMqttLocation = serverLocation;
        return true;
    }
    public String getTopicMqtt() {
        return csReaderConnector.settingData.topicMqtt;
    }
    public boolean setTopicMqtt(String topicMqtt) {
        csReaderConnector.settingData.topicMqtt = topicMqtt;
        return true;
    }
    public int getForegroundDupElim() {
        return csReaderConnector.settingData.iForegroundDupElim;
    }
    public boolean setForegroundDupElim(int iForegroundDupElim) {
        csReaderConnector.settingData.iForegroundDupElim = iForegroundDupElim;
        return true;
    }
    public int getInventoryCloudSave() {
        int i = csReaderConnector.settingData.inventoryCloudSave;
        if (false) appendToLog("getInventoryCloudSave710 = " + i);
        return i;
    }
    public boolean setInventoryCloudSave(int inventoryCloudSave) {
        appendToLog("setInventoryCloudSave710 = " + inventoryCloudSave);
        csReaderConnector.settingData.inventoryCloudSave = inventoryCloudSave;
        return true;
    }
    public String getServerImpinjLocation() {
        return csReaderConnector.settingData.serverImpinjLocation;
    }
    public boolean setServerImpinjLocation(String serverImpinjLocation) {
        csReaderConnector.settingData.serverImpinjLocation = serverImpinjLocation;
        return true;
    }
    public String getServerImpinjName() {
        appendToLog("serverImpinjName = " + csReaderConnector.settingData.serverImpinjName);
        return csReaderConnector.settingData.serverImpinjName;
    }
    public boolean setServerImpinjName(String serverImpinjName) {
        csReaderConnector.settingData.serverImpinjName = serverImpinjName;
        appendToLog("serverImpinjName = " + serverImpinjName);
        return true;
    }
    public String getServerImpinjPassword() {
        return csReaderConnector.settingData.serverImpinjPassword;
    }
    public boolean setServerImpinjPassword(String serverImpinjPassword) {
        csReaderConnector.settingData.serverImpinjPassword = serverImpinjPassword;
        return true;
    }
    public int getBatteryDisplaySetting() {
        return csReaderConnector.settingData.batteryDisplaySelect;
    }
    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        if (true) return csReaderConnector.settingData.setBatteryDisplaySetting(batteryDisplaySelect);
        if (batteryDisplaySelect < 0 || batteryDisplaySelect > 1)   return false;
        csReaderConnector.settingData.batteryDisplaySelect = batteryDisplaySelect;
        return true;
    }
    public double dBuV_dBm_constant = RfidReader.dBuV_dBm_constant; //106.98;
    public int getRssiDisplaySetting() {
        return csReaderConnector.settingData.rssiDisplaySelect;
    }
    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        if (true) return csReaderConnector.settingData.setRssiDisplaySetting(rssiDisplaySelect);
        if (rssiDisplaySelect < 0 || rssiDisplaySelect > 1)   return false;
        csReaderConnector.settingData.rssiDisplaySelect = rssiDisplaySelect;
        return true;
    }
    public int getVibrateModeSetting() {
        return csReaderConnector.settingData.vibrateModeSelect;
    }
    public boolean setVibrateModeSetting(int vibrateModeSelect) {
        if (vibrateModeSelect < 0 || vibrateModeSelect > 1)   return false;
        csReaderConnector.settingData.vibrateModeSelect = vibrateModeSelect;
        return true;
    }
    public int getSavingFormatSetting() {
        return csReaderConnector.settingData.savingFormatSelect;
    }
    public boolean setSavingFormatSetting(int savingFormatSelect) {
        if (false) appendToLog("savingFormatSelect = " + savingFormatSelect);
        if (savingFormatSelect < 0 || savingFormatSelect > 1)   return false;
        csReaderConnector.settingData.savingFormatSelect = savingFormatSelect;
        return true;
    }
    public int getCsvColumnSelectSetting() {
        return csReaderConnector.settingData.csvColumnSelect;
    }
    public boolean setCsvColumnSelectSetting(int csvColumnSelect) {
        csReaderConnector.settingData.csvColumnSelect = csvColumnSelect;
        return true;
    }
    public String getWedgeDeviceName() {
        return csReaderConnector.settingData.wedgeDeviceName;
    }
    public String getWedgeDeviceAddress() {
        return csReaderConnector.settingData.wedgeDeviceAddress;
    }
    public int getWedgeDeviceUUID2p1() {
        return csReaderConnector.settingData.wedgeDeviceUUID2p1;
    }
    public int getWedgePower() {
        return csReaderConnector.settingData.wedgePower;
    }
    public String getWedgePrefix() {
        return csReaderConnector.settingData.wedgePrefix;
    }
    public String getWedgeSuffix() {
        return csReaderConnector.settingData.wedgeSuffix;
    }
    public int getWedgeDelimiter() {
        return csReaderConnector.settingData.wedgeDelimiter;
    }
    public int getWedgeOutput() {
        return csReaderConnector.settingData.wedgeOutput;
    }
    public void setWedgeDeviceName(String wedgeDeviceName) {
        csReaderConnector.settingData.wedgeDeviceName = wedgeDeviceName;
    }
    public void setWedgeDeviceAddress(String wedgeDeviceAddress) {
        csReaderConnector.settingData.wedgeDeviceAddress = wedgeDeviceAddress;
    }
    public void setWedgeDeviceUUID2p1(int wedgeDeviceUUID2p1) {
        csReaderConnector.settingData.wedgeDeviceUUID2p1 = wedgeDeviceUUID2p1;;
    }
    public void setWedgePower(int iPower) {
        csReaderConnector.settingData.wedgePower = iPower;
    }
    public void setWedgePrefix(String string) {
        csReaderConnector.settingData.wedgePrefix = string;
    }
    public void setWedgeSuffix(String string) {
        csReaderConnector.settingData.wedgeSuffix = string;
    }
    public void setWedgeDelimiter(int iValue) {
        csReaderConnector.settingData.wedgeDelimiter = iValue;
    }
    public void setWedgeOutput(int iOutput) {
        csReaderConnector.settingData.wedgeOutput = iOutput;
    }
    public void saveWedgeSetting2File() {
        csReaderConnector.settingData.saveWedgeSetting2File();
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
        String str = controllerConnector.getSerialNumber();
        appendToLog("str = " + str);
        if (str != null) {
            if (str.length() >= 16) return str.substring(0, 16);
        }
        return null;
    }
    public String getHostProcessorICBoardVersion() {
        String str = controllerConnector.getSerialNumber();
        if (false) appendToLog("str = " + str);
        if (str == null) return null;
        if (str.length() < 16+4) return null;
        str = str.substring(16);
        if (true) {
            String string = "";
            if (str.length() >= 1) string = str.substring(0,1);
            if (str.length() >= 3) string += ("." + str.substring(1, 3));
            if (str.length() >= 4) string += ("." + str.substring(3, 4));

            if (false) {
                if (str.length() >= 5) string += (", " + str.substring(4, 5));
                if (str.length() >= 7) string += ("." + str.substring(5, 7));
                if (str.length() >= 8) string += ("." + str.substring(7, 8));
            }
            str = string;
        }
        return str;
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
        int iValue = csReaderConnector.csConnectorData.getVoltageMv();
        String hostVersion = hostProcessorICGetFirmwareVersion(); //false if lower, true if equal or higher
        String strVersionHost = "2.1.5";
        String[] strHostVersions = strVersionHost.split("\\.");
        boolean bResult = checkHostProcessorVersion(hostVersion, Integer.parseInt(strHostVersions[0].trim()), Integer.parseInt(strHostVersions[1].trim()), Integer.parseInt(strHostVersions[2].trim()));
        appendToLog("getBatteryLevel: hostVersion = " + hostVersion + ", bResult = " + bResult + ", level = " + iValue);
        if (!bResult) {
            if (iValue >= 4450) iValue -= 430;
            else if (iValue > 350) iValue -= 350;
        }
        return iValue;
    }
    public boolean setAutoTriggerReporting(byte timeSecond) {
        return notificationConnector.setAutoTriggerReporting(timeSecond);
    }
    public boolean getAutoBarStartSTop() {
        return notificationConnector.getAutoBarStartStopStatus();
    }

    public boolean batteryLevelRequest() {
        if (csReaderConnector.rfidReader == null) return false;
        if (notificationConnector == null) return false;
        if (csReaderConnector.rfidReader.isInventoring()) {
            appendToLog("Skip batteryLevelREquest as inventoring !!!");
            return true;
        }
        if (mrfidToWriteSize() != 0) return false;
        return notificationConnector.batteryLevelRequest();
    }
    public boolean setAutoBarStartSTop(boolean enable) {
        return notificationConnector.setAutoBarStartSTop(enable);
    }
    public boolean getTriggerReporting() {
        return csReaderConnector.settingData.triggerReporting;
    }
    public boolean setTriggerReporting(boolean triggerReporting) {
        return notificationConnector.setTriggerReporting(triggerReporting);
    }
    public final int iNO_SUCH_SETTING = 10000;
    public short getTriggerReportingCount() {
        boolean bValue = false;
        if (getcsModel() != 463) bValue = checkHostProcessorVersion(hostProcessorICGetFirmwareVersion(),  1, 0, 16);
        if (bValue == false) return iNO_SUCH_SETTING; else
            return csReaderConnector.settingData.triggerReportingCountSetting;
    }
    public boolean setTriggerReportingCount(short triggerReportingCount) {
        boolean bValue = false;
        if (triggerReportingCount < 0 || triggerReportingCount > 255) return false;
        if (getTriggerReporting()) {
            if (csReaderConnector.settingData.triggerReportingCountSetting == triggerReportingCount) return true;
            bValue = setAutoTriggerReporting((byte)(triggerReportingCount & 0xFF));
        } else bValue = true;
        if (bValue) csReaderConnector.settingData.triggerReportingCountSetting = triggerReportingCount;
        return true;
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
        return csReaderConnector.csConnectorData.getVoltageCnt();
    }
    public boolean getTriggerButtonStatus() {
        return notificationConnector.getTriggerStatus();
    }
    public int getTriggerCount() {
        return csReaderConnector.csConnectorData.getTriggerCount();
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
        boolean DEBUG = false;
        String strModelName = controllerConnector.getModelName();
        if (DEBUG) appendToLog("getModelName 0xb006 = " + strModelName);
        if (true) {
            String strModelName1 = rfidReaderChip.rx000Setting.getModelCode();
            if (DEBUG) appendToLog("getModelCode 0x5000 = " + strModelName1);
            if (strModelName == null || strModelName.length() == 0) {
                if (DEBUG) appendToLog("strModeName is updated as modeCode");
                strModelName = strModelName1;
            }
        }
        return strModelName;
    }
    public String getSerialNumber() {
        if (true) return csReaderConnector.rfidReader.getSerialNumber();
        return rfidReaderChip.rx000Setting.getBoardSerialNumber();
    }
    public boolean setRfidOn(boolean onStatus) {
        return csReaderConnector.rfidReader.turnOn(onStatus);
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
            if (csReaderConnector.rfidReader == null || barcodeNewland == null || csReaderConnector.rfidReader.mRfidToWrite.size() != 0 || (isBarcodeFailure() == false && barcodeNewland.bBarcodeTriggerMode == (byte)0xFF)) {
                mHandler.removeCallbacks(checkVersionRunnable);
                mHandler.postDelayed(checkVersionRunnable, 500);
            } else {
                setSameCheck(false);
                if (DEBUG_CONNECT) appendToLog("2 checkVersionRunnable: BarcodeFailure = " + isBarcodeFailure()); ///
                if (isBarcodeFailure() == false) {
                    if (DEBUG_CONNECT) appendToLog("3 checkVersionRunnable"); ///5
                    if (barcodeNewland.checkPreSuffix(barcodeNewland.prefixRef, barcodeNewland.suffixRef) == false) barcodeNewland.barcodeSendCommandSetPreSuffix();
                    if (barcodeNewland.bBarcodeTriggerMode != 0x30) barcodeNewland.barcodeSendCommandTrigger();
                    notificationConnector.getAutoRFIDAbort(); notificationConnector.getAutoBarStartSTop(); //setAutoRFIDAbort(false); setAutoBarStartSTop(true);
                }
                setAntennaCycle(0xffff);
                if (false) {
                    if (bluetoothConnector.getCsModel() == 463) {
                        appendToLog("4 checkVersionRunnable");
                        setAntennaDwell(2000);
                        setAntennaInvCount(0);
                    } else {
                        if (DEBUG_CONNECT) appendToLog("5 checkVersionRunnable"); ///8
                        setAntennaDwell(0);
                        setAntennaInvCount(0xfffffffeL);
                    }
                }
                csReaderConnector.settingData.loadWedgeSettingFile();
                if (loadSetting1File()) loadSetting1File();
                if (checkHostProcessorVersion(getMacVer(), 2, 6, 8)) {
                    if (DEBUG_CONNECT) appendToLog("7 checkVersionRunnable: macVersion [" + getMacVer() + "] >= 2.6.8");
                    appendToLog("0a setTagDelay[" + csReaderConnector.rfidReader.tagDelaySetting + "]");
                    rfidReaderChip.rx000Setting.setTagDelay(csReaderConnector.rfidReader.tagDelaySetting);
                    rfidReaderChip.rx000Setting.setCycleDelay(csReaderConnector.rfidReader.cycleDelaySetting);
                    appendToLog("2EF setInvAlgo");
                    rfidReaderChip.rx000Setting.setInvModeCompact(true);
                } else {
                    if (DEBUG_CONNECT) appendToLog("8 checkVersionRunnable: macVersion [" + getMacVer() + "] < 2.6.8");
                    rfidReaderChip.rx000Setting.setTagDelay(csReaderConnector.rfidReader.tagDelayDefaultNormalSetting);
                    rfidReaderChip.rx000Setting.setCycleDelay(csReaderConnector.rfidReader.cycleDelaySetting);
                }
                if (DEBUG_CONNECT) appendToLog("9 checkVersionRunnable: end of CheckVersionRunnable with mRfidToWrite.size = " + csReaderConnector.rfidReader.mRfidToWrite.size());
                setSameCheck(true);
            }
        }
    };

    boolean loadSetting1File() {
        return csReaderConnector.settingData.loadSettingFile(getlibraryVersion(), getChannelHoppingStatus(), getCurrentProfile());
    }
    public void saveSetting2File() {
        csReaderConnector.settingData.saveSetting2File(getlibraryVersion(), getChannelHoppingStatus(), getCurrentProfile());
    }

    public int getcsModel() {
        return bluetoothConnector.getCsModel();
    }
    public int getAntennaCycle() {
        return csReaderConnector.rfidReader.getAntennaCycle();
    }
    public boolean setAntennaCycle(int antennaCycle) {
        return csReaderConnector.rfidReader.setAntennaCycle(antennaCycle);
    }
    public boolean setAntennaInvCount(long antennaInvCount) {
        return csReaderConnector.rfidReader.setAntennaInvCount(antennaInvCount);
    }

    public void clearInvalidata() {
        csReaderConnector.clearInvalidata();
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

    public int setSelectData(RfidReader.TagType tagType, String mDid, boolean bNeedSelectedTagByTID, String stringProtectPassword, int selectFor, int selectHold) {
        return csReaderConnector.rfidReader.setSelectData4Inventory(tagType, mDid, bNeedSelectedTagByTID, stringProtectPassword, selectFor, selectHold);
    }
}
