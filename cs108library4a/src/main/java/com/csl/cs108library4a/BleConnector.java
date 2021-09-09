package com.csl.cs108library4a;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.PHY_LE_2M;
import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.app.ActivityCompat.requestPermissions;

class BleConnector extends BluetoothGattCallback {
    final boolean DEBUG = true; boolean DEBUG_BTDATA = true; boolean DEBUG_BTDATA0 = true;

    private Handler mHandler = new Handler();

    private ReaderDevice mBluetoothDevice;
    ReaderDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScanner mleScanner;

    private int mBluetoothProfile;
    boolean isBleConnected() { return mBluetoothProfile == BluetoothProfile.STATE_CONNECTED && mReaderStreamOutCharacteristic != null; }

    private boolean mScanning = false;
    boolean isBleScanning() { return mScanning; }

    int serviceUUID2p1 = 0;
    void setServiceUUIDType(int serviceUUID2p1) { this.serviceUUID2p1 = serviceUUID2p1; }
    int getServiceUUIDType() { return serviceUUID2p1; }
    boolean isCs710ServiceUUID() { return serviceUUID2p1 == 3; }
    private final UUID UUID_READER_STREAM_OUT_CHARACTERISTIC = UUID.fromString("00009900-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_READER_STREAM_IN_CHARACTERISTIC = UUID.fromString("00009901-0000-1000-8000-00805f9b34fb");

    private int mRssi; int getRssi() { return mRssi; }

    private boolean characteristicListRead = false;
    boolean isCharacteristicListRead() { return characteristicListRead; }

    private BluetoothGattCharacteristic mReaderStreamOutCharacteristic;
    private BluetoothGattCharacteristic mReaderStreamInCharacteristic;
    private long mStreamWriteCount, mStreamWriteCountOld;
    private boolean _readCharacteristic_in_progress;
    private boolean _writeCharacteristic_in_progress;
    private ArrayList<BluetoothGattCharacteristic> mBluetoothGattCharacteristicToRead = new ArrayList<>();

    private final int STREAM_IN_BUFFER_MAX = 0x100000; //0xC00;  //0x800;  //0x400;
    private byte[] streamInBuffer = new byte[STREAM_IN_BUFFER_MAX];
    private int streamInBufferHead, streamInBufferTail;
    private int streamInBufferSize = 0;
    int getStreamInBufferSize() { return streamInBufferSize; }

    private long streamInOverflowTime = 0;
    long getStreamInOverflowTime() {
        return streamInOverflowTime;
    }

    private int streamInBytesMissing = 0;
    int getStreamInBytesMissing() {
        int missingByte = streamInBytesMissing;
        streamInBytesMissing = 0;
        return missingByte;
    }

    private int streamInTotalCounter = 0;
    int getStreamInTotalCounter() {
        return streamInTotalCounter;
    }

    private int streamInAddCounter = 0;
    int getStreamInAddCounter() {
        return streamInAddCounter;
    }

    private long streamInAddTime = 0;
    long getStreamInAddTime() {
        return streamInAddTime;
    }

    private boolean connectionHSpeed = true;
    boolean getConnectionHSpeedA() { return connectionHSpeed; }
    boolean setConnectionHSpeedA(boolean connectionHSpeed) { this.connectionHSpeed = connectionHSpeed; return true; }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLog("INVALID mBluetoothGatt");
//        } else if (status != BluetoothGatt.GATT_SUCCESS) {
//            if (DEBUG) appendToLog("NOT GATT_SUCCESS, newState=" + newState);
        } else {
            mBluetoothProfile = newState;
            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    forcedDisconnect(false);
                    if (DEBUG) appendToLog("state=Disconnected");
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    if (disconnectRunning) break;
                    mStreamWriteCount = mStreamWriteCountOld = 0;
                    _readCharacteristic_in_progress = _writeCharacteristic_in_progress = false;
                    appendToLog("Start discoverServices");
                    if (mBluetoothGatt.discoverServices()) {
                        if (DEBUG) appendToLog("state=Connected. discoverServices starts");
                    } else {
                        if (DEBUG) appendToLog("state=Connected. discoverServices FAIL");
                    }
                    utility.setReferenceTimeMs();
                    mHandler.removeCallbacks(mReadRssiRunnable);
                    mHandler.post(mReadRssiRunnable);
                    break;
                default:
                    if (DEBUG) appendToLog("state=" + newState);
                    break;
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLog("INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (true) appendToLog("status=" + status + ". restart discoverServices"); mBluetoothGatt.discoverServices();
        } else {
            UUID UUID_READER_SERVICE = UUID.fromString("00009800-0000-1000-8000-00805f9b34fb");
            if (serviceUUID2p1 != 1) UUID_READER_SERVICE = UUID.fromString("00009802-0000-1000-8000-00805f9b34fb");
            appendToLog("serviceUD1D2p1 = " + serviceUUID2p1);
            mReaderStreamOutCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_OUT_CHARACTERISTIC);
            mReaderStreamInCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_IN_CHARACTERISTIC);
            appendToLog("mReaderStreamOutCharacteristic flag = " + mReaderStreamOutCharacteristic.getProperties());
            appendToLog("mReaderStreamInCharacteristic flag = " + mReaderStreamInCharacteristic.getProperties());
            if (mReaderStreamInCharacteristic == null || mReaderStreamOutCharacteristic == null) {
                if (true) appendToLog("restart discoverServices");mBluetoothGatt.discoverServices(); return;
            }

            if (!mBluetoothGatt.setCharacteristicNotification(mReaderStreamInCharacteristic, true)) {
                if (DEBUG) appendToLog("setCharacteristicNotification() FAIL");
            } else {
                if (DEBUG) appendToLog("writeDescriptor() starts with characteristicListRead = " + characteristicListRead);
                if (characteristicListRead == false) {
                    if (DEBUG) appendToLog("with services");
                            mBluetoothGattCharacteristicToRead.clear();
                            List<BluetoothGattService> ss = mBluetoothGatt.getServices();
                            for (BluetoothGattService service : ss) {
                                String uuid = service.getUuid().toString().substring(4, 8); //substring(0, 8)
                                List<BluetoothGattCharacteristic> cc = service.getCharacteristics();
                                for (BluetoothGattCharacteristic characteristic : cc) {
                                    String characteristicUuid = characteristic.getUuid().toString().substring(4, 8);    //substring(0, 8)
                                    int properties = characteristic.getProperties();
                                    boolean do_something = false;
                                    if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                        if (DEBUG) appendToLog("service=" + uuid + ", characteristic=" + characteristicUuid + ", property=read");
                                        mBluetoothGattCharacteristicToRead.add(characteristic);
                                        do_something = true;
                                    }
                                    if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                        if (DEBUG) appendToLog("service=" + uuid + ", characteristic=" + characteristicUuid + ", property=write");
                                        do_something = true;
                                    }
                                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                        if (DEBUG) appendToLog("service=" + uuid + ", characteristic=" + characteristicUuid + ", property=notify");
                                        do_something = true;
                                    }
                                    if (!do_something) {
                                        if (DEBUG) appendToLog("service=" + uuid + ", characteristic=" + characteristicUuid + ", property=" + String.format("%X ", properties));
                                    }
                                }
                            }
                            if (true) mBluetoothGattCharacteristicToRead.clear();
                            mHandler.removeCallbacks(mReadCharacteristicRunnable);
                            if (DEBUG) appendToLog("starts in onServicesDiscovered");
                            mHandler.postDelayed(mReadCharacteristicRunnable, 500);
                }
            }
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) utility.appendToLogRunnable("onReadRemoteRssi(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) utility.appendToLogRunnable("onReadRemoteRssi(): NOT GATT_SUCCESS");
        } else {
            if (DEBUG) utility.appendToLogRunnable("onReadRemoteRssi(): rssi=" + rssi);
            mRssi = rssi;
        }
    }

    private final Runnable mReadRssiRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGatt == null) return;
            if (mBluetoothGatt.readRemoteRssi()) {
                if (DEBUG) appendToLog("mReadRssiRunnable(): readRemoteRssi starts");
            } else {
                if (DEBUG) appendToLog("mReadRssiRunnable(): readRemoteRssi FAIL");
            }
        }
    };

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLog("INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) appendToLog("status=" + status);
        } else {
            if (DEBUG) appendToLog("descriptor=" + descriptor.getUuid().toString().substring(4, 8));
        }
    }

    private boolean writeDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
        descriptor.setValue(value);
        if (!mBluetoothGatt.writeDescriptor(descriptor))
            return false;
        return true;
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) utility.appendToLogRunnable("onDescriptorRead(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) utility.appendToLogRunnable("onDescriptorRead(): status=" + status);
        } else {
            if (DEBUG) utility.appendToLogRunnable("onDescriptorRead(): descriptor=" + descriptor.getUuid().toString().substring(4, 8));
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLog("INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) appendToLog("status=" + status);
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
            if (DEBUG) appendToLog(serviceUuidd + ", " + characteristicUuid + " = " + stringBuilder.toString() + " = " + new String(v));
            if (DEBUG) appendToLog("starts in onCharacteristicRead");
            mReadCharacteristicRunnable.run();
        }
    }

    private final Runnable mReadCharacteristicRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothGattCharacteristicToRead.size() == 0) {
                if (DEBUG) appendToLog("mReadCharacteristicRunnable(): read finish");
                characteristicListRead = true;
            } else if (isBleBusy()) {
                if (DEBUG) appendToLog("mReadCharacteristicRunnable(): PortBusy");
                mHandler.postDelayed(mReadCharacteristicRunnable, 100);
            } else if (readCharacteristic(mBluetoothGattCharacteristicToRead.get(0)) == false) {
                if (DEBUG) appendToLog("mReadCharacteristicRunnable(): Read FAIL");
                mHandler.postDelayed(mReadCharacteristicRunnable, 100);
            } else {
                mBluetoothGattCharacteristicToRead.remove(0);
                if (DEBUG) appendToLog("mReadCharacteristicRunnable(): starts in mReadCharacteristicRunnable");
                mHandler.postDelayed(mReadCharacteristicRunnable, 10000);
            }
        }
    };

    private boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt.readCharacteristic(characteristic)) {
            _readCharacteristic_in_progress = true;
            return true;
        }
        return false;
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLog("INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            onCharacteristicWriteFailue++;
            if (DEBUG) appendToLog("status=" + status);
        } else {
            if (DEBUG_BTDATA) appendToLog("characteristic=" + characteristic.getUuid().toString().substring(4, 8) + ", sent " + (mStreamWriteCount - mStreamWriteCountOld) + " bytes");
            _writeCharacteristic_in_progress = false;
        }
    }

    private int writeBleCounter = 0;
    private int writeBleFailure = 0;
    private int onCharacteristicWriteFailue = 0;
    boolean writeBleStreamOut(byte[] value) {
        //appendToLog("Start");
        if (mBluetoothGatt == null) {
            if (DEBUG) appendToLog("ERROR with NULL mBluetoothGatt");
        } else if (mReaderStreamOutCharacteristic == null) {
            if (DEBUG) appendToLog("ERROR with NULL mReaderStreamOutCharacteristic");
        } else if (isBleBusy() || characteristicListRead == false) {
            if (true) appendToLog("isBleBusy()  = " + isBleBusy() + ", characteristicListRead = " + characteristicListRead);
        } else {
            mReaderStreamOutCharacteristic.setValue(value);
            if (((writeBleCounter / 100) * 100) == writeBleCounter) {
                if (true) appendToLog("writeBleCounter = " + writeBleCounter + ", writeBleFailure = " + writeBleFailure + ", onCharacteristicWriteFailue = " + onCharacteristicWriteFailue);
                if (writeBleCounter == 1000) {
                    writeBleCounter = 0;
                    writeBleFailure = 0;
                    onCharacteristicWriteFailue = 0;
                }
            }
            writeBleCounter++;
            boolean bValue = mBluetoothGatt.writeCharacteristic(mReaderStreamOutCharacteristic);
            if (bValue == false) {
                writeBleFailure++;
                if (true) appendToLog("writeCharacteristic(): ERROR for " + byteArrayToString(value));
            } else {
                if (DEBUG_BTDATA0) appendToLog(byteArrayToString(value));
                _writeCharacteristic_in_progress = true;
                mStreamWriteCountOld = mStreamWriteCount;
                mStreamWriteCount += value.length;
                return true;
            }
        }
        return false;
    }

    private boolean streamInRequest = false;
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) {
                byte[] v = characteristic.getValue();
                utility.appendToLogRunnable("onCharacteristicChanged(): INVALID mBluetoothGatt, with address = " + gatt.getDevice().getAddress() + ", values =" + byteArrayToString(v));
            }
        } else if (!characteristic.equals(mReaderStreamInCharacteristic)) {
            if (DEBUG) utility.appendToLogRunnable("onCharacteristicChanged(): characteristic is not ReaderSteamIn");
        } else if (mBluetoothProfile == BluetoothProfile.STATE_DISCONNECTED) {
                streamInBufferHead = 0;
                streamInBufferTail = 0;
                streamInBufferSize = 0;
        } else {
            byte[] v = characteristic.getValue();
            if (false) utility.appendToLogRunnable("onCharacteristicChanged(): VALID mBluetoothGatt, values =" + byteArrayToString(v));
            synchronized (arrayListStreamIn) {
                if (v.length != 0) {
                    streamInTotalCounter++;
                }
                if (streamInBufferReseting) {
                    if (DEBUG) utility.appendToLogRunnable("onCharacteristicChanged(): RESET.");
                    streamInBufferReseting = false;
                    streamInBufferSize = 0;
                    streamInBytesMissing = 0;
                }
                if (streamInBufferSize + v.length > streamInBuffer.length) {
                    utility.writeDebug2File("A, " + System.currentTimeMillis() + ", Overflow");
                    Log.i(TAG, ".Hello: missing data  = " + byteArrayToString(v));
                    if (streamInBytesMissing == 0) {
                        streamInOverflowTime = utility.getReferencedCurrentTimeMs();
                    }
                    streamInBytesMissing += v.length;
                } else {
                    utility.writeDebug2File("A, " + System.currentTimeMillis());
                    if (DEBUG_BTDATA0) Log.i(TAG, ".Hello: StreamIn = " + byteArrayToString(v));
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
    }

    private int intervalProcessBleStreamInData = 50;
    int getIntervalProcessBleStreamInData() { return intervalProcessBleStreamInData; }
    final Runnable runnableProcessBleStreamInData = new Runnable() {
        @Override
        public void run() {
            streamInRequest = false;
            processBleStreamInData();
            mHandler.postDelayed(runnableProcessBleStreamInData, intervalProcessBleStreamInData);
        }
    };

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        Log.i(TAG, "onMtuChanged starts");
        if (gatt != mBluetoothGatt) {
            if (DEBUG) utility.appendToLogRunnable("onMtuChanged(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) utility.appendToLogRunnable("onMtuChanged(): status=" + status);
        } else {
            if (DEBUG) utility.appendToLogRunnable("onMtuChanged(): mtu=" + mtu);
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (gatt != mBluetoothGatt) {
            if (true) utility.appendToLogRunnable("INVALID mBluetoothGatt");
        } else {
            if (true) utility.appendToLogRunnable("onReliableWriteCompleted(): status=" + status);
            //mBluetoothGatt.abortReliableWrite();
        }
    }

    private Context mContext; private Activity activity;
    BleConnector(Context context, TextView mLogView) {
        mContext = context; activity = (Activity) mContext;
        utility = new Utility(context, mLogView);

//        BluetoothConfigManager mConfigManager;
//        mConfigManager = BluetoothConfigManager.getInstance();
//        appendToLog("BluetoothConfigManager.getIoCapability = " + mConfigManager.getIoCapability());

        PackageManager mPackageManager = mContext.getPackageManager();
        if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                boolean isBle5 = mBluetoothAdapter.isLeCodedPhySupported();
                boolean isAdvertising5 = mBluetoothAdapter.isLeExtendedAdvertisingSupported();
                appendToLog("isBle5 = " + isBle5 + ", isAdvertising5 = " + isAdvertising5);
            }
        } else {
            mBluetoothAdapter = null;
            if (DEBUG) appendToLog("NO BLUETOOTH_LE");
        }

        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            appendToLog("permitted ACCESS_FINE_LOCATION");
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            appendToLog("permitted ACCESS_COARSE_LOCATION");
        }
        List<String> stringProviderList = locationManager.getAllProviders();
        for (String stringProvider: stringProviderList) appendToLog("Provider = " + stringProvider);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) appendToLog("ProviderEnabled GPS_PROVIDER");
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) appendToLog("ProviderEnabled NETWORK_PROVIDER");
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) appendToLog("ProviderEnabled PASSIVE_PROVIDER");
    }

    private PopupWindow popupWindow;
    private boolean bleEnableRequestShown0 = false, bleEnableRequestShown = false;
    private boolean isLocationAccepted = false;
    CustomAlertDialog appdialog; boolean bAlerting = false;
    boolean scanLeDevice(boolean enable, BluetoothAdapter.LeScanCallback mLeScanCallback, ScanCallback mScanCallBack) {
        appendToLog("StreamOut: enable = " + enable);
        boolean result = false;
        boolean locationReady = true;
        if (enable && isBleConnected()) return true;
        if (enable == false && isBleScanning() == false) return true;

        if (enable) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false)
                isLocationAccepted = false;
        }
        appendToLog("StreamOut: Start " + enable + ", with isLocationAccepted = " + isLocationAccepted + ", bAlerting = " + bAlerting);
        if (false && isLocationAccepted == false) {
            if (bAlerting == false && bleEnableRequestShown0 == false) {
                bAlerting = true;
                appendToLog("StreamOut: new AlertDialog");
                popupAlert();
            }
            return false;
        }

        appendToLog("StreamOut: Passed AlertDialog");
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appendToLog("Checking permission and grant !!!");
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
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
                    appendToLog("Setting grant");
                    btnDismiss.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            appendToLog("Set GRANT");
                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mContext.startActivity(intent1);
                        }
                    });
                }
                return false;
            } else if (
                    (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) ||
                            (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                if (true) {
                    if (bAlerting || bleEnableRequestShown0) return false;
                    bAlerting = true;
                    popupAlert();
                    return false;
                } else
                requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        }

        if (locationReady == false) {
            if (DEBUG) appendToLog("AccessCoarseLocatin is NOT granted");
        } else if (mBluetoothAdapter == null) {
            if (DEBUG) appendToLog("scanLeDevice(" + enable + ") with NULL mBluetoothAdapter");
        } else if (!mBluetoothAdapter.isEnabled()) {
            appendToLog("StreamOut: bleEnableRequestShown = " + bleEnableRequestShown);
            if (bleEnableRequestShown == false) {
                if (DEBUG) appendToLog("scanLeDevice(" + enable + ") with DISABLED mBluetoothAdapter");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
                appendToLog("StreamOut: bleEnableRequestShown is set");
                bleEnableRequestShown = true; mHandler.postDelayed(mRquestAllowRunnable, 60000);
            }
        } else {
            bleEnableRequestShown = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mleScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (mleScanner == null) {
                    if (DEBUG) appendToLog("scanLeDevice(" + enable + ") with NULL BluetoothLeScanner");
                    return false;
                }
            }
            if (enable == false) {
                if (DEBUG) appendToLog("scanLeDevice(" + enable + ") with mScanCallBack is " + (mScanCallBack != null ? "VALID" : "INVALID"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mScanCallBack != null) mleScanner.stopScan(mScanCallBack);
                } else {
                    if (mLeScanCallback != null) mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
                mScanning = false; result = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (DEBUG) appendToLog("scanLeDevice(" + enable + "): START with mleScanner");
                    mleScanner.startScan(mScanCallBack);
                } else {
                    if (DEBUG) appendToLog("scanLeDevice(" + enable + "): START with mBluetoothAdapter");
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
                mScanning = true; result = true;
            }
        }
        return result;
    }

    private final Runnable mRquestAllowRunnable = new Runnable() {
        @Override
        public void run() {
            bleEnableRequestShown0 = false;
            bleEnableRequestShown = false;
        }
    };
    void popupAlert() {
        appdialog = new CustomAlertDialog();
        appdialog.Confirm((Activity) mContext, "Use your location",
                "This app collects location data in the background.  In terms of the features using this location data in the background, this App collects location data when it is reading RFID tag in all inventory pages.  The purpose of this is to correlate the RFID tag with the actual GNSS(GPS) location of the tag.  In other words, this is to track the physical location of the logistics item tagged with the RFID tag.",
                "No thanks", "Turn on",
                new Runnable() {
                    @Override
                    public void run() {
                        isLocationAccepted = true;
                        appendToLog("StreamOut: This from FALSE proc");
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            appendToLog("StreamOut: request ACCESS_FINE_LOCATION");
                            requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
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

    /*
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
    boolean connectBle(ReaderDevice readerDevice) {
        if (readerDevice == null) {
            if (DEBUG) appendToLog("with NULL readerDevice");
        } else {
            String address = readerDevice.getAddress();
            if (mBluetoothAdapter == null) {
                if (DEBUG) appendToLog("connectBle(" + address + ") with NULL mBluetoothAdapter");
            } else if (!mBluetoothAdapter.isEnabled()) {
                if (DEBUG) appendToLog("connectBle(" + address + ") with DISABLED mBluetoothAdapter");
            } else {
                utility.debugFileSetup();
                mHandler.removeCallbacks(mDisconnectRunnable);
                utility.setReferenceTimeMs();
                if (DEBUG) appendToLog("connectBle(" + address + "): connectGatt starts");
/*
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                mContext.registerReceiver(myReceiver, intentFilter);
*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //android 8 = api level 26
                    BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
                    appendToLog("mBluetoothDevice.getBondState() = " + mBluetoothDevice.getBondState());
                    boolean bOkBond = true;
                    if (false && mBluetoothDevice.getBondState() == BOND_NONE) {
                        bOkBond = mBluetoothDevice.createBond();
                        if (bOkBond) appendToLog("sucess to creatBond");
                        else appendToLog("failed to createBond");
                    }
                    appendToLog("writeBleStreamOut: android 8 or above sets PHY_LE_2M");
                    if (bOkBond) mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, this, BluetoothDevice.TRANSPORT_LE, PHY_LE_2M);
                    else return  false;
                } else {
                    mBluetoothGatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(mContext, false, this);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (true) {
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        appendToLog("Stream Set to HIGH");
                    }
                    else {
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
                        appendToLog("Stream Set to BALANCED");
                    }
                }
                mBluetoothDevice = readerDevice;
                characteristicListRead = true; //skip in case there is problem in completing reading characteristic features, causing endless reading 0706 and 0C02
                return true;
            }
        }
        return false;
    }

    void disconnect() {
        if (mBluetoothAdapter == null) {
            if (DEBUG) appendToLog("with NULL mBluetoothAdapter");
        } else if (!mBluetoothAdapter.isEnabled()) {
            if (DEBUG) appendToLog("DISABLED mBluetoothAdapter");
        } else if (mBluetoothGatt == null) {
            if (DEBUG) appendToLog("NULL mBluetoothGatt");
        } else {
            utility.debugFileClose();
            mReaderStreamOutCharacteristic = null;
            mBluetoothGatt.disconnect();
            mHandler.removeCallbacks(mDisconnectRunnable);
            mHandler.postDelayed(mDisconnectRunnable, 200); disconnectRunning = true;
            if (DEBUG) appendToLog("done");
        }
    }
    void forcedDisconnect(boolean bForce) {
        if (bForce) forcedDisconnect1();
        mHandler.removeCallbacks(mReadRssiRunnable);
        mHandler.removeCallbacks(mReadCharacteristicRunnable);
    }
    void forcedDisconnect1() {
        if (mBluetoothGatt != null) { appendToLog("mDisconnectRunnable(): Close the GATT"); mBluetoothGatt.close(); }
        mBluetoothProfile = BluetoothProfile.STATE_DISCONNECTED;
    }

    private boolean disconnectRunning = false;
    private final Runnable mDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothProfile != BluetoothProfile.STATE_DISCONNECTED) {
                forcedDisconnect(true);
                if (DEBUG) appendToLog("mDisconnectRunnable(): disconnect");
                mHandler.postDelayed(mDisconnectRunnable, 500);
            } else {
                forcedDisconnect1();
                if (DEBUG) appendToLog("mDisconnectRunnable(): END");
                disconnectRunning = false;
            }
        }
    };

    boolean isBleBusy() {
        return mBluetoothProfile != BluetoothProfile.STATE_CONNECTED || _readCharacteristic_in_progress /*|| _writeCharacteristic_in_progress*/;
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID service, UUID characteristic) {
        BluetoothGattService s = mBluetoothGatt.getService(service);
        if (s == null)
            return null;
        BluetoothGattCharacteristic c = s.getCharacteristic(characteristic);
        return c;
    }

    private long streamInDataMilliSecond;
    long getStreamInDataMilliSecond() { return streamInDataMilliSecond; }
    int readBleSteamIn(byte[] buffer, int byteOffset, int byteCount) {
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
    private long firstTime, totalTime; long getStreamInRate() {
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

    private Utility utility;
    String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    void appendToLog(String s) { utility.appendToLog(s); }
    void appendToLogView(String s) { utility.appendToLogView(s); }
    void writeDebug2File(String stringDebug) { utility.writeDebug2File(stringDebug); }
    boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }
    void debugFileEnable(boolean enable) { utility.debugFileEnable(enable); }
}
