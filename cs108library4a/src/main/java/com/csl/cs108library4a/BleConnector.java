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
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.support.v4.app.ActivityCompat.requestPermissions;

class BleConnector extends BluetoothGattCallback {
    final boolean appendToLogDisable = false;
    final boolean DEBUG = true;
    final boolean DEBUGlowLevel = true;

    private Context mContext;
    private Activity activity;
    private TextView mLogView;

    private Handler mHandler = new Handler();
    private ReaderDevice mBluetoothDevice;

    ReaderDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    private BluetoothAdapter mBluetoothAdapter;

    BluetoothGatt mBluetoothGatt;
    private BluetoothLeScanner mleScanner;

    private int mBluetoothProfile; boolean isBleConnected() { return mBluetoothProfile == BluetoothProfile.STATE_CONNECTED && mReaderStreamOutCharacteristic != null; }

    private long mConnectedTimeMillis;

    private boolean mScanning = false; boolean isBleScanning() { return mScanning; }

    private final UUID UUID_GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_READER_SERVICE = UUID.fromString("00009800-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_READER_STREAM_OUT_CHARACTERISTIC = UUID.fromString("00009900-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_READER_STREAM_IN_CHARACTERISTIC = UUID.fromString("00009901-0000-1000-8000-00805f9b34fb");

    private int mRssi; int getRssi() { return mRssi; }

    boolean characteristicListRead = false;
    private BluetoothGattCharacteristic mReaderStreamOutCharacteristic;
    private BluetoothGattCharacteristic mReaderStreamInCharacteristic;
    private long mStreamWriteCount, mStreamWriteCountOld;
    private boolean _readCharacteristic_in_progress;
    private boolean _writeCharacteristic_in_progress;
    private ArrayList<BluetoothGattCharacteristic> mBluetoothGattCharacteristicToRead = new ArrayList<>();

    private final int STREAM_IN_BUFFER_MAX = 0x4000; //0xC00;  //0x800;  //0x400;
    private final int STREAM_IN_BUFFER_LIMIT = 0x3F80;   //0xB80;    //0x780;    //0x380;
    private byte[] streamInBuffer = new byte[STREAM_IN_BUFFER_MAX];
    int streamInBufferHead, streamInBufferTail, streamInBufferSize = 0;

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
                    if (mBluetoothGatt.discoverServices()) {
                        if (DEBUG) appendToLog("state=Connected. discoverServices starts");
                    } else {
                        if (DEBUG) appendToLog("state=Connected. discoverServices FAIL");
                    }
                    mConnectedTimeMillis = System.currentTimeMillis();
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
            if (DEBUG) appendToLog("status=" + status);
        } else {
            mReaderStreamOutCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_OUT_CHARACTERISTIC);
            mReaderStreamInCharacteristic = getCharacteristic(UUID_READER_SERVICE, UUID_READER_STREAM_IN_CHARACTERISTIC);
            BluetoothGattDescriptor mReaderStreamInDescriptor = mReaderStreamInCharacteristic.getDescriptor(UUID_GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIG); //MUST BE AFTER getCharacteristic()

            if (!mBluetoothGatt.setCharacteristicNotification(mReaderStreamInCharacteristic, true)) {
                if (DEBUG) appendToLog("setCharacteristicNotification() FAIL");
//            } else if (!writeDescriptor(mReaderStreamInDescriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
//                if (DEBUG) appendToLog("writeDescriptor() FAIL");
            } else {
                if (DEBUG) appendToLog("writeDescriptor() starts with characteristicListRead = " + characteristicListRead);
                if (characteristicListRead == false) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
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
//                        }
//                    });
                }
            }
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLogRunnable("onReadRemoteRssi(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) appendToLogRunnable("onReadRemoteRssi(): NOT GATT_SUCCESS");
        } else {
            if (DEBUG) appendToLogRunnable("onReadRemoteRssi(): rssi=" + rssi);
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
//            if (isBleConnected())
//                mHandler.postDelayed(mReadRssiRunnable, 5000);
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
            if (DEBUG) appendToLogRunnable("onDescriptorRead(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) appendToLogRunnable("onDescriptorRead(): status=" + status);
        } else {
            if (DEBUG) appendToLogRunnable("onDescriptorRead(): descriptor=" + descriptor.getUuid().toString().substring(4, 8));
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
            final long t = currentBleConnectTimeMillis();
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
            if (DEBUG) appendToLog("characteristic=" + characteristic.getUuid().toString().substring(4, 8) + ", sent " + (mStreamWriteCount - mStreamWriteCountOld) + " bytes");
            _writeCharacteristic_in_progress = false;
        }
    }

    int writeBleCounter = 0;
    int writeBleFailure = 0;
    int onCharacteristicWriteFailue = 0;
    boolean writeBleStreamOut(byte[] value) {
        if (mBluetoothGatt == null) {
            if (DEBUG) appendToLog("ERROR with NULL mBluetoothGatt");
        } else if (mReaderStreamOutCharacteristic == null) {
            if (DEBUG) appendToLog("ERROR with NULL mReaderStreamOutCharacteristic");
        } else if (isBleBusy() || characteristicListRead == false) {
            appendToLog("isBleBusy()  = " + isBleBusy() + ", characteristicListRead = " + characteristicListRead);
        } else {
            mReaderStreamOutCharacteristic.setValue(value);
            if (((writeBleCounter / 100) * 100) == writeBleCounter) {
                appendToLog("writeBleCounter = " + writeBleCounter + ", writeBleFailure = " + writeBleFailure + ", onCharacteristicWriteFailue = " + onCharacteristicWriteFailue);
                if (writeBleCounter == 1000) {
                    writeBleCounter = 0;
                    writeBleFailure = 0;
                    onCharacteristicWriteFailue = 0;
                }
            }
            writeBleCounter++;
            if (mBluetoothGatt.writeCharacteristic(mReaderStreamOutCharacteristic) == false) {
                writeBleFailure++;
                if (DEBUG) appendToLog("writeCharacteristic(): ERROR for " + byteArrayToString(value));
            } else {
                appendToLog(byteArrayToString(value));
                _writeCharacteristic_in_progress = true;
                mStreamWriteCountOld = mStreamWriteCount;
                mStreamWriteCount += value.length;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) {
                byte[] v = characteristic.getValue();
                appendToLogRunnable("onCharacteristicChanged(): INVALID mBluetoothGatt, with address = " + gatt.getDevice().getAddress() + ", values =" + byteArrayToString(v));
            }
        } else if (!characteristic.equals(mReaderStreamInCharacteristic)) {
            if (DEBUG) appendToLogRunnable("onCharacteristicChanged(): characteristic is not ReaderSteamIn");
        } else if (mBluetoothProfile == BluetoothProfile.STATE_DISCONNECTED) {
                streamInBufferHead = 0;
                streamInBufferTail = 0;
                streamInBufferSize = 0;
        } else {
            byte[] v = characteristic.getValue();
            if (false) appendToLogRunnable("onCharacteristicChanged(): VALID mBluetoothGatt, values =" + byteArrayToString(v));
            synchronized (streamInBuffer) {
                if (v.length != 0) {
                    streamInTotalCounter++;
                }
                if (streamInBufferReseting) {
                    if (DEBUG) appendToLogRunnable("onCharacteristicChanged(): RESET.");
                    streamInBufferReseting = false;
                    streamInBufferSize = 0;
                    streamInBytesMissing = 0;
                }
                if (streamInBufferSize + v.length > streamInBuffer.length) {
                    writeDebug2File("A, " + System.currentTimeMillis() + ", Overflow");
                    if (streamInBytesMissing == 0) {
                        streamInOverflowTime = currentBleConnectTimeMillis();
                    }
                    streamInBytesMissing += v.length;
                } else {
                    writeDebug2File("A, " + System.currentTimeMillis());
                    if (isStreamInBufferRing) {
                        streamInBufferPush(v, 0, v.length);
                    } else {
                        System.arraycopy(v, 0, streamInBuffer, streamInBufferSize, v.length);
                    }
                    streamInBufferSize += v.length;
                    streamInAddCounter++;
                    streamInAddTime = currentBleConnectTimeMillis();
                }
            }
        }
    }

    boolean streamInBufferReseting = false;

    void processCs108DataIn() {
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLogRunnable("onMtuChanged(): INVALID mBluetoothGatt");
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            if (DEBUG) appendToLogRunnable("onMtuChanged(): status=" + status);
        } else {
            if (DEBUG) appendToLogRunnable("onMtuChanged(): mtu=" + mtu);
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (gatt != mBluetoothGatt) {
            if (DEBUG) appendToLogRunnable("onMtuChanged(): INVALID mBluetoothGatt");
        } else {
            if (DEBUG) appendToLogRunnable("onReliableWriteCompleted(): status=" + status);
        }
    }

    BleConnector(Context context, TextView mLogView) {
        mContext = context; activity = (Activity) mContext;
//        this.mLogView = mLogView;

//        final ScrollView mScrollView = (ScrollView) activity.findViewById(R.id.log_scroll);
//        mLogView = (TextView) activity.findViewById(R.id.log_view);

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
            if (mLogView != null)   mLogView.append("permitted ACCESS_FINE_LOCATION\n");
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            appendToLog("permitted ACCESS_COARSE_LOCATION");
            if (mLogView != null)   mLogView.append("permitted ACCESS_COARSE_LOCATION\n");
        }
        List<String> stringProviderList = locationManager.getAllProviders();
        for (String stringProvider: stringProviderList) appendToLog("Provider = " + stringProvider);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) appendToLog("ProviderEnabled GPS_PROVIDER");
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) appendToLog("ProviderEnabled NETWORK_PROVIDER");
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) appendToLog("ProviderEnabled PASSIVE_PROVIDER");
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //
    }

    PopupWindow popupWindow; boolean popupWindowShown = false;
    boolean bleEnableRequestShown = false;
    boolean scanLeDevice(final boolean enable, BluetoothAdapter.LeScanCallback mLeScanCallback, ScanCallback mScanCallBack) {
        boolean result = false;
        boolean locationReady = true;
        if (enable && isBleConnected()) return true;
        if (enable == false && isBleScanning() == false) return true;

        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appendToLog("Checking permission and grant !!!");
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false) {
                boolean isShowing = false;
                if (popupWindow != null) isShowing = popupWindow.isShowing();
                if (isShowing == false) {
                    LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = layoutInflater.inflate(R.layout.popup, null);
                    popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                    TextView textViewDismiss = (TextView)popupView.findViewById(R.id.dismissMessage);
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
            } else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            }
        }

        if (locationReady == false) {
            if (DEBUG) appendToLog("AccessCoarseLocatin is NOT granted");
        } else if (mBluetoothAdapter == null) {
            if (DEBUG) appendToLog("scanLeDevice(" + enable + ") with NULL mBluetoothAdapter");
        } else if (!mBluetoothAdapter.isEnabled()) {
            if (bleEnableRequestShown == false) {
                if (DEBUG)
                    appendToLog("scanLeDevice(" + enable + ") with DISABLED mBluetoothAdapter");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
                bleEnableRequestShown = true;
            }
        } else {
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
                debugFileSetup();
                mHandler.removeCallbacks(mDisconnectRunnable);
                mConnectedTimeMillis = System.currentTimeMillis();
                if (DEBUG) appendToLog("connectBle(" + address + "): connectGatt starts");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(mContext, false, this, BluetoothDevice.TRANSPORT_LE);
                } else {
                    mBluetoothGatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(mContext, false, this);
                }
                mBluetoothDevice = readerDevice;
                characteristicListRead = false;
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
            debugFileClose();
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

    boolean disconnectRunning = false;
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

    private long currentBleConnectTimeMillis() {
        return System.currentTimeMillis() - mConnectedTimeMillis;
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID service, UUID characteristic) {
        BluetoothGattService s = mBluetoothGatt.getService(service);
        if (s == null)
            return null;
        BluetoothGattCharacteristic c = s.getCharacteristic(characteristic);
        return c;
    }

    String byteArrayToString(byte[] packet) {
        if (packet == null) return "";
        StringBuilder sb = new StringBuilder(packet.length * 2);
        for (byte b : packet) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    void appendToLog(String s) {
        if (appendToLogDisable) return;
        String TAG = "";
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        boolean logged = false;
        boolean foundMe = false;
        for(int i=0; i<stacktrace.length; i++) {
            StackTraceElement e = stacktrace[i];
            String methodName = e.getMethodName();
            if (methodName.equals("appendToLog")) {
                foundMe = true;
            } else if (foundMe) {
                if (!methodName.startsWith("access$")) {
                    TAG = String.format(Locale.US, "%s.%s", e.getClassName(), methodName);
                    logged = true;
                    break;
                }
            }
        }
        if (mLogView != null)   mLogView.append("\n" + currentBleConnectTimeMillis() + "." + TAG + "." + s);
        Log.i(TAG + ".Hello", s);
    }
    void appendToLogRunnable(final String s) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                appendToLog(s);
            }
        });
    }

    private byte[] peekSteamInBuffer(int size) {
        synchronized (streamInBuffer) {
            if (streamInBufferSize == 0)
                return null;
            if (size > streamInBufferSize)
                size = streamInBufferSize;
            byte[] data = new byte[size];
            System.arraycopy(streamInBuffer, 0, data, 0, size);
            return data;
        }
    }

    int readBleSteamIn(byte[] buffer, int byteOffset, int byteCount) {
        synchronized (streamInBuffer) {
            if (0 == streamInBufferSize) return 0;
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
            streamInBufferSize -= byteCount;
            return byteCount;
        }
    }

    private int totalTemp, totalReceived;
    private long firstTime, totalTime; long getStreamInRate() {
        if (totalTime == 0 || totalReceived == 0) return 0;
        return totalReceived * 1000 / totalTime;
    }

    private boolean isStreamInBufferRing = true;
    private void streamInBufferPush(byte[] inData, int inDataOffset, int length) {
        int length1 = streamInBuffer.length - streamInBufferTail;
        int totalCopy = 0;
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
        }
    }

    File fileDebug; boolean inventoring = false;
    void debugFileSetup() {
        boolean writeExtPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                writeExtPermission = false;
                requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
            }
        }
        String errorDisplay = null;
        if (writeExtPermission == false) {
            errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!";
        } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) errorDisplay = "Error in mouting external storage !!!";
        else {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/cs108Java");
            if (path.exists() == false) path.mkdirs();
            if (path.exists() == false) errorDisplay = "Error in making directory !!!";
            else {
                String dateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                String fileName = "cs108JavaDebug_" + dateTime + ".txt";
                fileDebug = new File(path, fileName);
                if (fileDebug == null) errorDisplay = "Error in making directory !!!";
            }
        }
        if (errorDisplay != null) appendToLog("Error in saving file with " + errorDisplay);
    }
    void debugFileClose() {
        if (fileDebug != null) {
            try {
                MediaScannerConnection.scanFile(mContext, new String[]{fileDebug.getAbsolutePath()}, null, null);
            } catch (Exception ex) {
            }
        }
    }
    void writeDebug2File(String stringDebug) {
        if (fileDebug != null && inventoring) {
            try {
                FileOutputStream outputStreamDebug = new FileOutputStream(fileDebug, true);
                PrintWriter printWriterDebug = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(outputStreamDebug), "UTF-8"));
                printWriterDebug.println(stringDebug);
                printWriterDebug.flush(); printWriterDebug.close();
                outputStreamDebug.close();
            } catch (Exception ex) {
            }
        }
    }
}
