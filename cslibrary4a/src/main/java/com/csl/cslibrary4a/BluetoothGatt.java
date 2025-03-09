package com.csl.cslibrary4a;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.Context.LOCATION_SERVICE;

public class BluetoothGatt extends BluetoothGattCallback {
    private Handler mHandler = new Handler();

    private ReaderDevice mBluetoothDevice;
    public ReaderDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;
    public android.bluetooth.BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner bluetoothLeScanner;

    public int bluetoothConnectionState;
    public boolean isBleConnected() { return bluetoothConnectionState == BluetoothProfile.STATE_CONNECTED && mReaderStreamOutCharacteristic != null; }

    private boolean mScanning = false;
    public boolean isBleScanning() { return mScanning; }

    int serviceUUID2p1 = 0;
    public void setServiceUUIDType(int serviceUUID2p1) { this.serviceUUID2p1 = serviceUUID2p1; }

    private final UUID UUID_READER_STREAM_OUT_CHARACTERISTIC = UUID.fromString("00009900-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_READER_STREAM_IN_CHARACTERISTIC = UUID.fromString("00009901-0000-1000-8000-00805f9b34fb");

    private int mRssi;
    public int getRssi() { return mRssi; }

    private boolean characteristicListRead = false;

    public boolean isCharacteristicListRead() {
        return characteristicListRead;
    }

    public BluetoothGattCharacteristic mReaderStreamOutCharacteristic;
    private BluetoothGattCharacteristic mReaderStreamInCharacteristic;
    private long mStreamWriteCount, mStreamWriteCountOld;
    private boolean _readCharacteristic_in_progress;
    private boolean _writeCharacteristic_in_progress;
    private ArrayList<BluetoothGattCharacteristic> mBluetoothGattCharacteristicToRead = new ArrayList<>();

    private final int STREAM_IN_BUFFER_MAX = 0x100000; //0xC00;  //0x800;  //0x400;
    private byte[] streamInBuffer = new byte[STREAM_IN_BUFFER_MAX];
    private int streamInBufferHead, streamInBufferTail;
    private int streamInBufferSize = 0;

    public int getStreamInBufferSize() {
        return streamInBufferSize;
    }

    private long streamInOverflowTime = 0;

    public long getStreamInOverflowTime() {
        return streamInOverflowTime;
    }

    private int streamInBytesMissing = 0;

    public int getStreamInBytesMissing() {
        int missingByte = streamInBytesMissing;
        streamInBytesMissing = 0;
        return missingByte;
    }

    private int streamInTotalCounter = 0;

    public int getStreamInTotalCounter() {
        return streamInTotalCounter;
    }

    private int streamInAddCounter = 0;

    public int getStreamInAddCounter() {
        return streamInAddCounter;
    }

    private long streamInAddTime = 0;

    public long getStreamInAddTime() {
        return streamInAddTime;
    }

    private boolean connectionHSpeed = true;

    public boolean getConnectionHSpeedA() {
        return connectionHSpeed;
    }

    public boolean setConnectionHSpeedA(boolean connectionHSpeed) {
        this.connectionHSpeed = connectionHSpeed;
        return true;
    }

    @Override
    public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
        boolean DEBUG = false;
        super.onConnectionStateChange(gatt, status, newState);
        Logger.connect("newState = {}", newState);
        if (gatt != bluetoothGatt) {
            Logger.debug("abcc mismatched mBluetoothGatt = {}, status = {}", gatt != bluetoothGatt, status);
        } else {
            bluetoothConnectionState = newState;
            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    Logger.connect("state=Disconnected with status = {}", status);
                    if (disconnectRunning == false) {
                        Logger.debug("disconnect b");
                        disconnect();
                    }
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    Logger.connect("state=Connected with status = {}", status);
                    if (disconnectRunning) {
                        Logger.debug("abcc disconnectRunning !!!");
                        break;
                    }
                    mStreamWriteCount = mStreamWriteCountOld = 0;
                    _readCharacteristic_in_progress = _writeCharacteristic_in_progress = false;
                    if (bDiscoverStarted) {
                        Logger.debug("abc discovery has been started before");
                        break;
                    }
                    Logger.connect("Start discoverServices");
                    if (discoverServices()) {
                        bDiscoverStarted = true;
                        Logger.connect("state=Connected. discoverServices starts with status = {}", status);
                    } else {
                        Logger.debug("state=Connected. discoverServices FAIL");
                    }
                    utility.setReferenceTimeMs();
                    mHandler.removeCallbacks(mReadRssiRunnable);
                    mHandler.post(mReadRssiRunnable);
                    break;
                default:
                    Logger.debug("state={}",newState);
                    break;
            }
        }
    }

    public boolean bDiscoverStarted = false;

    @Override
    public void onServicesDiscovered(android.bluetooth.BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (gatt != bluetoothGatt) {
            Logger.debug("INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.debug("status={}. restart discoverServices", status);
            discoverServices();
        } else {
            UUID UUID_READER_SERVICE = UUID.fromString("0000" + strReaderServiceUUID + "-0000-1000-8000-00805f9b34fb");
            mReaderStreamOutCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_OUT_CHARACTERISTIC);
            mReaderStreamInCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_IN_CHARACTERISTIC);
            Logger.btop("mReaderStreamOutCharacteristic flag = {}", mReaderStreamOutCharacteristic.getProperties());
            Logger.btop("mReaderStreamInCharacteristic flag = {}", mReaderStreamInCharacteristic.getProperties());
            if (mReaderStreamInCharacteristic == null || mReaderStreamOutCharacteristic == null) {
                Logger.debug("restart discoverServices");
                discoverServices();
                return;
            }

            if (checkSelfPermissionBLUETOOTH() == false) return;
            if (!bluetoothGatt.setCharacteristicNotification(mReaderStreamInCharacteristic, true)) {
                Logger.debug("setCharacteristicNotification() FAIL");
            } else {
                int mtu_requested = 255;
                boolean bValue = gatt.requestMtu(mtu_requested);
                Logger.btop("requestMtu[{}] with result={}",mtu_requested, bValue);

                Logger.btop("characteristicListRead = {}", characteristicListRead);
                if (characteristicListRead == false) {
                    Logger.debug("with services");
                    mBluetoothGattCharacteristicToRead.clear();
                    List<BluetoothGattService> ss = bluetoothGatt.getServices();
                    for (BluetoothGattService service : ss) {
                        String uuid = service.getUuid().toString().substring(4, 8); //substring(0, 8)
                        List<BluetoothGattCharacteristic> cc = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : cc) {
                            String characteristicUuid = characteristic.getUuid().toString().substring(4, 8);    //substring(0, 8)
                            int properties = characteristic.getProperties();
                            boolean do_something = false;
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                Logger.debug("service={}, characteristic={}, property=read", uuid, characteristicUuid);
                                mBluetoothGattCharacteristicToRead.add(characteristic);
                                do_something = true;
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                Logger.debug("service={} characteristic={}, property=write", uuid, characteristicUuid);
                                do_something = true;
                            }
                            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                Logger.debug("service={}, characteristic={}, property=notify", uuid, characteristicUuid);
                                do_something = true;
                            }
                            if (!do_something) {
                                Logger.debug("service={}, characteristic={}, property={}", uuid, characteristicUuid, String.format("%X ", properties));
                            }
                        }
                    }
                    if (true) mBluetoothGattCharacteristicToRead.clear();
                    mHandler.removeCallbacks(mReadCharacteristicRunnable);
                    Logger.debug("starts in onServicesDiscovered");
                    mHandler.postDelayed(mReadCharacteristicRunnable, 500);
                }
            }
        }
    }

    @Override
    public void onReadRemoteRssi(android.bluetooth.BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (gatt != bluetoothGatt) {
            Logger.runDebug("onReadRemoteRssi: INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.runDebug("onReadRemoteRssi: NOT GATT_SUCCESS");
        } else {
            Logger.runDebug("onReadRemoteRssi: rssi={}", rssi);
            mRssi = rssi;
        }
    }

    private final Runnable mReadRssiRunnable = new Runnable() {
        @Override
        public void run() {
            if (checkSelfPermissionBLUETOOTH() == false) return;
            if (bluetoothGatt == null) {
                Logger.debug("mReadRssiRunnable: readRemoteRssi with null mBluetoothGatt");
                return;
            } else if (bluetoothGatt.readRemoteRssi()) {
                Logger.btop("mReadRssiRunnable: readRemoteRssi starts");
            } else {
                Logger.debug("mReadRssiRunnable: readRemoteRssi FAIL");
            }
        }
    };

    @Override
    public void onDescriptorWrite(android.bluetooth.BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (gatt != bluetoothGatt) {
            Logger.debug("INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.debug("status={}", status);
        } else {
            Logger.debug("descriptor={}", descriptor.getUuid().toString().substring(4, 8));
        }
    }

    private boolean writeDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
        descriptor.setValue(value);
        if (checkSelfPermissionBLUETOOTH() == false) return false;
        if (!bluetoothGatt.writeDescriptor(descriptor))
            return false;
        return true;
    }

    @Override
    public void onDescriptorRead(android.bluetooth.BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        if (gatt != bluetoothGatt) {
            Logger.runDebug("onDescriptorRead(): INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.runDebug("onDescriptorRead(): status={}", status);
        } else {
            Logger.runDebug("onDescriptorRead(): descriptor={}", descriptor.getUuid().toString().substring(4, 8));
        }
    }

    @Override
    public void onCharacteristicRead(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (gatt != bluetoothGatt) {
            Logger.debug("INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.debug("status={}", status);
        } else {
            _readCharacteristic_in_progress = false;

            final String serviceUuidd = characteristic.getService().getUuid().toString().substring(4, 8);
            final String characteristicUuid = characteristic.getUuid().toString().substring(4, 8);
            final byte[] v = characteristic.getValue();
            final long t = utility.getReferencedCurrentTimeMs();
            mHandler.removeCallbacks(mReadCharacteristicRunnable);
            StringBuilder stringBuilder = new StringBuilder();
            if (v != null && v.length > 0) {
                stringBuilder.ensureCapacity(v.length * 3);
                for (byte b : v)
                    stringBuilder.append(String.format("%02X ", b));
            }
            Logger.debug("{}, {} = {} = {}", serviceUuidd, characteristicUuid, stringBuilder, new String(v));
            Logger.debug("starts in onCharacteristicRead");
            mReadCharacteristicRunnable.run();
        }
    }

    private final Runnable mReadCharacteristicRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGattCharacteristicToRead.size() == 0) {
                Logger.debug("mReadCharacteristicRunnable(): read finish");
                characteristicListRead = true;
            } else if (isBleBusy()) {
                Logger.debug("mReadCharacteristicRunnable(): PortBusy");
                mHandler.postDelayed(mReadCharacteristicRunnable, 100);
            } else if (readCharacteristic(mBluetoothGattCharacteristicToRead.get(0)) == false) {
                Logger.debug("mReadCharacteristicRunnable(): Read FAIL");
                mHandler.postDelayed(mReadCharacteristicRunnable, 100);
            } else {
                mBluetoothGattCharacteristicToRead.remove(0);
                Logger.debug("mReadCharacteristicRunnable(): starts in mReadCharacteristicRunnable");
                mHandler.postDelayed(mReadCharacteristicRunnable, 10000);
            }
        }
    };

    private boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (checkSelfPermissionBLUETOOTH() == false) return false;
        if (bluetoothGatt.readCharacteristic(characteristic)) {
            _readCharacteristic_in_progress = true;
            return true;
        }
        return false;
    }

    @Override
    public void onCharacteristicWrite(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (gatt != bluetoothGatt) {
            Logger.debug("INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            onCharacteristicWriteFailue++;
            Logger.debug("status={}", status);
        } else {
            onCharacteristicWriteFailue = 0;
            Logger.debug("characteristic={}, sent {} bytes", characteristic.getUuid().toString().substring(4, 8), mStreamWriteCount - mStreamWriteCountOld);
            _writeCharacteristic_in_progress = false;
        }
    }

    private int writeBleFailure = 0;
    private int onCharacteristicWriteFailue = 0;
    public boolean writeBleStreamOut(byte[] value) {
        if (bluetoothGatt == null) {
            Logger.debug("ERROR with NULL mBluetoothGatt");
        } else if (mReaderStreamOutCharacteristic == null) {
            Logger.debug("ERROR with NULL mReaderStreamOutCharacteristic");
        } else if (isBleBusy() || characteristicListRead == false) {
            Logger.info("isBleBusy() = {}, characteristicListRead = ", isBleBusy(), characteristicListRead);
        } else {
            mReaderStreamOutCharacteristic.setValue(value);
            if (checkSelfPermissionBLUETOOTH() == false) return false;
            boolean bValue = bluetoothGatt.writeCharacteristic(mReaderStreamOutCharacteristic);
            if (bValue == false) writeBleFailure++;
            else {
                writeBleFailure = 0;
                Logger.btdData("BtDataOut: {}", byteArrayToString(value));
                writeDebug2File("Down " + byteArrayToString(value));
                _writeCharacteristic_in_progress = true;
                mStreamWriteCountOld = mStreamWriteCount;
                mStreamWriteCount += value.length;
                return true;
            }
            if (false && (writeBleFailure != 0 || onCharacteristicWriteFailue != 0)) {
                Logger.trace("failure in writeCharacteristic({}), writeBleFailure = {}, onCharacteristicWriteFailue = {}", byteArrayToString(value), writeBleFailure, onCharacteristicWriteFailue);
                if (writeBleFailure > 5 || onCharacteristicWriteFailue > 5) {
                    Logger.trace("writeBleFailure is too much. start disconnect !!!");
                    Logger.trace("disconnect C");
                    disconnect(); //mReaderStreamOutCharacteristic = null;
                }
            }
        }
        return false;
    }

    private boolean streamInRequest = false;
    @Override
    public void onCharacteristicChanged(android.bluetooth.BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (gatt != bluetoothGatt) {
            utility.writeDebug2File("Up1  Error, mismatched gatt");
            Logger.runDebug("onCharacteristicChanged(): INVALID mBluetoothGatt, with address = {}, values ={}", gatt.getDevice().getAddress(), byteArrayToString(characteristic.getValue()));
        } else if (!characteristic.equals(mReaderStreamInCharacteristic)) {
            utility.writeDebug2File("Up1  Error, mismatched characteristic");
            Logger.runDebug("onCharacteristicChanged(): characteristic is not ReaderSteamIn");
        } else if (bluetoothConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
            utility.writeDebug2File("Up1  Error, disconnected bluetoothConnectionState");
            streamInBufferHead = 0;
            streamInBufferTail = 0;
            streamInBufferSize = 0;
        } else {
            byte[] v = characteristic.getValue();
            if (false) Logger.runDebug("onCharacteristicChanged(): VALID mBluetoothGatt, values ={}", byteArrayToString(v));
            synchronized (arrayListStreamIn) {
                if (v.length != 0) {
                    streamInTotalCounter++;
                }
                if (streamInBufferReseting) {
                    Logger.runDebug("onCharacteristicChanged(): RESET.");
                    streamInBufferReseting = false;
                    streamInBufferSize = 0;
                    streamInBytesMissing = 0;
                }
                if (streamInBufferSize + v.length > streamInBuffer.length) {
                    utility.writeDebug2File("Up1  Error, insufficient buffer. missed " + byteArrayToString(v));
                    Logger.info(".Hello: missing data  = {}", byteArrayToString(v));
                    if (streamInBytesMissing == 0) {
                        streamInOverflowTime = utility.getReferencedCurrentTimeMs();
                    }
                    streamInBytesMissing += v.length;
                } else {
                    if (true) utility.writeDebug2File("Up1  " + byteArrayToString(v));
                    Logger.btdData("BtDataIn= {}", byteArrayToString(v));
                    if (isStreamInBufferRing) {
                        streamInBufferPush(v, 0, v.length);
                    } else {
                        System.arraycopy(v, 0, streamInBuffer, streamInBufferSize, v.length);
                    }
                    streamInBufferSize += v.length;
                    streamInAddCounter++;
                    streamInAddTime = utility.getReferencedCurrentTimeMs();
                    if (streamInRequest == false) {
                        streamInRequest = true;
                        mHandler.removeCallbacks(runnableProcessBleStreamInData); mHandler.post(runnableProcessBleStreamInData);
                    }
                }
            }
        }
    }

    private boolean streamInBufferReseting = false;
    void setStreamInBufferReseting() { streamInBufferReseting = true; }

    void processBleStreamInData() {
        if (bluetoothGattConnectorCallback != null) bluetoothGattConnectorCallback.callbackMethod();
    }

    public interface BluetoothGattConnectorCallback {
        void callbackMethod();
    }
    public BluetoothGattConnectorCallback bluetoothGattConnectorCallback = null;

    private int intervalProcessBleStreamInData = 100; //50;
    public int getIntervalProcessBleStreamInData() { return intervalProcessBleStreamInData; }
    public final Runnable runnableProcessBleStreamInData = new Runnable() {
        @Override
        public void run() {
            streamInRequest = false;
            processBleStreamInData();
            mHandler.postDelayed(runnableProcessBleStreamInData, intervalProcessBleStreamInData);
        }
    };

    @Override
    public void onMtuChanged(android.bluetooth.BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        Logger.info("onMtuChanged starts");
        if (gatt != bluetoothGatt) {
            Logger.runDebug("onMtuChanged: INVALID mBluetoothGatt");
        } else if (status != android.bluetooth.BluetoothGatt.GATT_SUCCESS) {
            Logger.runDebug("onMtuChanged: status={}", status);
        } else {
            Logger.runDebug("onMtuChanged: mtu={}", mtu);
        }
    }

    @Override
    public void onReliableWriteCompleted(android.bluetooth.BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (gatt != bluetoothGatt) {
            Logger.runDebug("INVALID mBluetoothGatt");
        } else {
            Logger.runDebug("onReliableWriteCompleted(): status={}", status);
            //mBluetoothGatt.abortReliableWrite();
        }
    }

    private Context mContext; Utility utility; String strReaderServiceUUID; //private Activity activity;
    public BluetoothGatt(Context context, Utility utility, String strReaderServiceUUID) {
        mContext = context; //activity = (Activity) mContext;
        this.strReaderServiceUUID = strReaderServiceUUID;
        this.utility = utility;

//        BluetoothConfigManager mConfigManager;
//        mConfigManager = BluetoothConfigManager.getInstance();

        PackageManager mPackageManager = mContext.getPackageManager();
        if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                boolean isBle5 = bluetoothAdapter.isLeCodedPhySupported();
                boolean isAdvertising5 = bluetoothAdapter.isLeExtendedAdvertisingSupported();
                Logger.debug("isBle5 = {}, isAdvertising5 = {}", isBle5, isAdvertising5);
            }
        } else {
            bluetoothAdapter = null;
            Logger.debug("NO BLUETOOTH_LE");
        }

        if (Logger.LEVEL.ordinal() <= Logger.LogLevel.DEBUG.ordinal()) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) Logger.debug("permitted ACCESS_FINE_LOCATION");
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) Logger.debug("permitted ACCESS_COARSE_LOCATION");

            List<String> stringProviderList = locationManager.getAllProviders();
            for (String stringProvider : stringProviderList)
                Logger.debug("Provider = {}", stringProvider);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                Logger.debug("ProviderEnabled GPS_PROVIDER");
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                Logger.debug("ProviderEnabled NETWORK_PROVIDER");
            if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
                Logger.debug("ProviderEnabled PASSIVE_PROVIDER");
        }
    }

    private PopupWindow popupWindow;
    private boolean /*bleEnableRequestShown0 = false, */bleEnableRequestShown = false;
    private boolean isLocationAccepted = false;
    boolean bAlerting = false; //CustomAlertDialog appdialog;
    public boolean scanLeDevice(boolean enable, BluetoothAdapter.LeScanCallback mLeScanCallback, ScanCallback mScanCallBack) {
        Logger.debug("StreamOut: enable = {}", enable);
        boolean result = false;
        boolean locationReady = true;
        if (enable && isBleConnected()) return true;
        if (enable == false && isBleScanning() == false) return true;

        if (enable) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false)
                isLocationAccepted = false;
        }
        Logger.scan("isLocationAccepted = {}, bAlerting = {}, bleEnableRequestShown = {}",isLocationAccepted, bAlerting,  bleEnableRequestShown);
        /*if (false && isLocationAccepted == false) {
            if (bAlerting == false && bleEnableRequestShown0 == false) {
                bAlerting = true;
                popupAlert();
            }
            return false;
        }*/
/*
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (DEBUG) appendToLog("Checking permission and grant !!!");
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (DEBUG_SCAN) appendToLog("locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            if (DEBUG_SCAN) appendToLog("locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) = " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
            if (DEBUG_SCAN) appendToLog("ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) = " + ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION));
            if (DEBUG_SCAN) appendToLog("ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)  = " + ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION));
            if (false && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                boolean isShowing = false;
                if (popupWindow != null) isShowing = popupWindow.isShowing();
                if (isShowing == false) {
                    LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = layoutInflater.inflate(R.layout.popup, null);
                    popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                    TextView textViewDismiss = (TextView) popupView.findViewById(R.id.dismissMessage);
                    textViewDismiss.setText("Android OS 6.0+ requires to enable location service to find the nearby BLE devices");
                    Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                    if (DEBUG) appendToLog("Setting grant");
                    btnDismiss.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            if (DEBUG) appendToLog("Set GRANT");
                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mContext.startActivity(intent1);
                        }
                    });
                }
                return false;
            } else if (
                    (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false
                            && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false)
                            || (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                if (true) {
                    if (bAlerting || bleEnableRequestShown0) return false;
                    bAlerting = true;
                    popupAlert();
                    return false;
                }
            }
        }
*/
        if (isBLUETOOTH_CONNECTinvalid()) return false;

        if (locationReady == false) {
            Logger.debug("AccessCoarseLocatin is NOT granted");
        } else if (bluetoothAdapter == null) {
            Logger.debug("scanLeDevice({}) with NULL mBluetoothAdapter");
/*        } else if (!bluetoothAdapter.isEnabled()) {
            if (DEBUG) appendToLog("StreamOut: bleEnableRequestShown = " + bleEnableRequestShown);
            if (bleEnableRequestShown == false) {
                if (true) appendToLog("scanLeDevice(" + enable + ") with DISABLED mBluetoothAdapter");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
                if (DEBUG) appendToLog("StreamOut: bleEnableRequestShown is set");
                bleEnableRequestShown = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
            }
*/
        } else {
            bleEnableRequestShown = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (bluetoothLeScanner == null) {
                    Logger.debug("scanLeDevice({}) with NULL BluetoothLeScanner", enable);
                    return false;
                }
            }
            if (!enable) {
                Logger.info("abcc scanLeDevice({}) with mScanCallBack is {}", enable, mScanCallBack != null ? "VALID" : "INVALID");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mScanCallBack != null) bluetoothLeScanner.stopScan(mScanCallBack);
                } else {
                    if (mLeScanCallback != null) bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
                mScanning = false; result = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Logger.info("scanLeDevice({}): START with mleScanner. ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) = {}", enable, ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN));
                    if (isBLUETOOTH_CONNECTinvalid()) return false;
                    else bluetoothLeScanner.startScan(mScanCallBack);
                } else {
                    Logger.info("scanLeDevice({}): START with mBluetoothAdapter", enable);
                    bluetoothAdapter.startLeScan(mLeScanCallback);
                }
                mScanning = true; result = true;
            }
        }
        return result;
    }

    private final Runnable mRquestAllowRunnable = new Runnable() {
        @Override
        public void run() {
            //bleEnableRequestShown0 = false;
            bleEnableRequestShown = false;
        }
    };
/*
    void popupAlert() {
        appdialog = new CustomAlertDialog();
        appdialog.Confirm(activity, "Use your location",
                "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
                "No thanks", "Turn on",
                new Runnable() {
                    @Override
                    public void run() {
                        isLocationAccepted = true;
                        appendToLog("StreamOut: This from FALSE proc");
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            appendToLog("requestPermissions ACCESS_FINE_LOCATION 123");
                            requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                            if (false) Toast.makeText(mContext, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                        }
                        {
                            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                                appendToLog("StreamOut: start activity ACTION_LOCATION_SOURCE_SETTINGS");
                                Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mContext.startActivity(intent1);
                            }
                        }
                        bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                        bAlerting = false;
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        appendToLog("StreamOut: This from FALSE proc");
                        bAlerting = false;
                        bleEnableRequestShown0 = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
                    }
                });
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            appendToLog("action = " + action);
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // CONNECT
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Discover new device
            }
        }
    };
*/
    public boolean connectBle(ReaderDevice readerDevice) {
        Logger.debug("abcc: start connecting {}", readerDevice.getName());
        if (readerDevice == null) {
            Logger.debug("with NULL readerDevice");
        } else {
            String address = readerDevice.getAddress();
            if (bluetoothAdapter == null) {
                Logger.debug("connectBle[{}] with NULL mBluetoothAdapter", address);
            } else if (!bluetoothAdapter.isEnabled()) {
                Logger.debug("connectBle[{}] with DISABLED mBluetoothAdapter", address);
            } else {
                utility.debugFileSetup(); utility.debugFileEnable(true);
                utility.setReferenceTimeMs();
                Logger.connect("connectBle[{}]: connectGatt starts", address);
                bluetoothConnectionState = -1;
                if (checkSelfPermissionBLUETOOTH() == false) return false;
                bluetoothGatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(mContext, false, this);
                if (bluetoothGatt != null) mBluetoothGattActive = true;
                if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (true) {
                        bluetoothGatt.requestConnectionPriority(android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        Logger.debug("Stream Set to HIGH");
                    }
                    else {
                        bluetoothGatt.requestConnectionPriority(android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
                        Logger.debug("Stream Set to BALANCED");
                    }
                }
                mBluetoothDevice = readerDevice;
                characteristicListRead = true; //skip in case there is problem in completing reading characteristic features, causing endless reading 0706 and 0C02
                Logger.info("post runnableProcessBleStreamInData after connectBle");
                mHandler.removeCallbacks(runnableProcessBleStreamInData); mHandler.post(runnableProcessBleStreamInData);
                return true;
            }
        }
        return false;
    }

    public void disconnect() {
        Logger.info("abcc: start disconnect ");
        if (bluetoothGatt == null) {
            Logger.debug("NULL mBluetoothGatt");
        } else {
            utility.debugFileClose();
            mReaderStreamOutCharacteristic = null;
            mHandler.removeCallbacks(mDisconnectRunnable);
            mHandler.post(mDisconnectRunnable); disconnectRunning = true;
            Logger.debug("abcc done and start mDisconnectRunnable");
            Logger.info("post runnableProcessBleStreamInData after disconnect");
            mHandler.removeCallbacks(runnableProcessBleStreamInData);
        }
    }
    boolean mBluetoothGattActive = false;
    boolean forcedDisconnect1() {
        mHandler.removeCallbacks(mReadRssiRunnable);
        mHandler.removeCallbacks(mReadCharacteristicRunnable);
        if (bluetoothGatt != null) {
            if (mBluetoothGattActive) {
                Logger.info("abcc mDisconnectRunnable(): close mBluetoothGatt");
                if (checkSelfPermissionBLUETOOTH() == false) return false;
                bluetoothGatt.close();
                mBluetoothGattActive = false;
            } else {
                Logger.info("abcc mDisconnectRunnable(): Null mBluetoothGatt");
                bluetoothGatt = null;
                return true;
            }
        }
        return false;
        //mBluetoothConnectionState = -1;
    }

    private boolean disconnectRunning = false;
    public BluetoothDevice bluetoothDeviceConnectOld;
    private final Runnable mDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            boolean done = false;
            int bGattConnection = -1;
            if (checkSelfPermissionBLUETOOTH() == false) return;
            if (bluetoothDeviceConnectOld != null) bGattConnection = bluetoothManager.getConnectionState(bluetoothDeviceConnectOld, BluetoothProfile.GATT);
            Logger.debug("abcc DisconnectRunnable(): disconnect with mBluetoothConnectionState = {}, getConnection = {}", bluetoothConnectionState, bGattConnection);
            if (bluetoothConnectionState < 0) {
                Logger.info("abcc DisconnectRunnable(): start mBluetoothGatt.disconnect");
                bluetoothGatt.disconnect();
                bluetoothConnectionState = BluetoothProfile.STATE_DISCONNECTED;
            } else if (bluetoothConnectionState != BluetoothProfile.STATE_DISCONNECTED) {
                Logger.info("abcc 2 DisconnectRunnable(): start mBluetoothGatt.disconnect");
                if (checkSelfPermissionBLUETOOTH()) {
                    bluetoothGatt.disconnect(); //forcedDisconnect(true);
                    bluetoothConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                }
            } else if (forcedDisconnect1()) {
                Logger.debug("abcc mDisconnectRunnable(): END");
                disconnectRunning = false;
                if (false) bluetoothAdapter.disable();
                done = true;
            }
            if (done == false) mHandler.postDelayed(mDisconnectRunnable, 100);
        }
    };

    boolean isBleBusy() {
        return bluetoothConnectionState != BluetoothProfile.STATE_CONNECTED || _readCharacteristic_in_progress /*|| _writeCharacteristic_in_progress*/;
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID service, UUID characteristic) {
        BluetoothGattService s = bluetoothGatt.getService(service);
        if (s == null)
            return null;
        BluetoothGattCharacteristic c = s.getCharacteristic(characteristic);
        return c;
    }

    private long streamInDataMilliSecond;
    public long getStreamInDataMilliSecond() { return streamInDataMilliSecond; }
    public int readBleSteamIn(byte[] buffer, int byteOffset, int byteCount) {
        synchronized (arrayListStreamIn) {
            if (0 == streamInBufferSize) return 0;
            if (isArrayListStreamInBuffering) {
                int byteGot = 0;
                int length1 = arrayListStreamIn.get(0).data.length;
                if (arrayListStreamIn.size() != 0 && buffer.length - byteOffset > length1) {
                    System.arraycopy(arrayListStreamIn.get(0).data, 0, buffer, byteOffset, length1);
                    streamInDataMilliSecond = arrayListStreamIn.get(0).milliseconds;
                    arrayListStreamIn.remove(0);
                    byteOffset += length1;
                    byteGot += length1;
                }
                byteCount = byteGot;
            } else {
            if (byteCount > streamInBufferSize)
                byteCount = streamInBufferSize;
            if (byteOffset + byteCount > buffer.length) {
                byteCount = buffer.length - byteOffset;
            }
            if (byteCount <= 0) return 0;
            if (isStreamInBufferRing) {
                streamInBufferPull(buffer, byteOffset, byteCount);
            } else {
                System.arraycopy(streamInBuffer, 0, buffer, byteOffset, byteCount);
                System.arraycopy(streamInBuffer, byteCount, streamInBuffer, 0, streamInBufferSize - byteCount);
            }
            }
            streamInBufferSize -= byteCount;
            return byteCount;
        }
    }

    private int totalTemp, totalReceived;
    private long firstTime, totalTime; public long getStreamInRate() {
        if (totalTime == 0 || totalReceived == 0) return 0;
        return totalReceived * 1000 / totalTime;
    }

    private class StreamInData {
        byte[] data;
        long milliseconds;
    }
    private ArrayList<StreamInData> arrayListStreamIn = new ArrayList<StreamInData>(); private boolean isArrayListStreamInBuffering = true;
    private boolean isStreamInBufferRing = true;
    private void streamInBufferPush(byte[] inData, int inDataOffset, int length) {
        int length1 = streamInBuffer.length - streamInBufferTail;
        int totalCopy = 0;
        if (isArrayListStreamInBuffering) {
            StreamInData streamInData = new StreamInData();
            streamInData.data = inData;
            streamInData.milliseconds = System.currentTimeMillis();
            arrayListStreamIn.add(streamInData);
            totalCopy = length;
        } else {
        if (length > length1) {
            totalCopy = length1;
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length1);
            length -= length1;
            inDataOffset += length1;
            streamInBufferTail = 0;
        }
        if (length != 0) {
            totalCopy += length;
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length);
            streamInBufferTail += length;
        }
        }
        if (totalCopy != 0) {
            totalTemp += totalCopy;
            long timeDifference = System.currentTimeMillis() - firstTime;
            if (totalTemp > 17 && timeDifference > 1000) {
                totalReceived = totalTemp;
                totalTime = timeDifference;
                firstTime = System.currentTimeMillis();
                totalTemp = 0;
            }
        }
    }
    private void streamInBufferPull(byte[] buffer, int byteOffset, int length) {
        synchronized (arrayListStreamIn) {
        int length1 = streamInBuffer.length - streamInBufferHead;
        if (length > length1) {
            System.arraycopy(streamInBuffer, streamInBufferHead, buffer, byteOffset, length1);
            length -= length1;
            byteOffset += length1;
            streamInBufferHead = 0;
        }
        if (length != 0) {
            System.arraycopy(streamInBuffer, streamInBufferHead, buffer, byteOffset, length);
            streamInBufferHead += length;
        }}
    }

    public boolean isBLUETOOTH_CONNECTinvalid() {
        boolean bValue = false;
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        )) {
            requestPermissions(activity, new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 123);
            if (false) Toast.makeText(mContext, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
            bValue = true;
        }
*/
        return bValue;
    }

    String byteArray2DisplayString(byte[] byteData) { return utility.byteArray2DisplayString(byteData); }
    String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    int byteArrayToInt(byte[] bytes) { return utility.byteArrayToInt(bytes); }
    void writeDebug2File(String stringDebug) { utility.writeDebug2File(stringDebug); }
    boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    void debugFileEnable(boolean enable) { utility.debugFileEnable(enable); }
    String getlast3digitVersion(String str) { return utility.getlast3digitVersion(str); }
    public boolean isVersionGreaterEqual(String version, int majorVersion, int minorVersion, int buildVersion) { return utility.isVersionGreaterEqual(version, majorVersion, minorVersion, buildVersion); }
    double get2BytesOfRssi(byte[] bytes, int index) { return utility.get2BytesOfRssi(bytes, index); }

    int getConnectionState(BluetoothDevice bluetoothDevice) {
        if (checkSelfPermissionBLUETOOTH() == false) return -1;

        return bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
    }

    boolean discoverServices() {
        if (checkSelfPermissionBLUETOOTH() == false) return false;
        return bluetoothGatt.discoverServices();
    }
    boolean checkSelfPermissionBLUETOOTH() {
        boolean bValue = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) bValue = true;
        } else if (ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) bValue = true;
        if (false) Log.i("Hello3", "checkSelfPermissionBLUETOOTH bValue = " + bValue);
        return bValue;
    }

    public static class CsScanData {
        public BluetoothDevice device; String name, address;
        public int rssi;
        public byte[] scanRecord;
        public ArrayList<byte[]> decoded_scanRecord;
        public int serviceUUID2p2;

        public CsScanData(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
            decoded_scanRecord = new ArrayList<byte[]>();
        }
        CsScanData(String name, String address, int rssi, byte[] scanRecord) {
            this.device = device; this.name = name; this.address = address;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
        public BluetoothDevice getDevice() { return device; }
        public String getName() {
            return name;
        }
        public String getAddress() {
            return address;
        }
        public byte[] getScanRecord() { return scanRecord; }
    }
}
