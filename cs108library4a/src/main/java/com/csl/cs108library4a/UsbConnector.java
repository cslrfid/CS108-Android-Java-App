package com.csl.cs108library4a;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class UsbConnector {
    final boolean DEBUG = false;

    private Context mContext;
    private TextView mTextView;
    private int _productId;
    private int _vendorId;

    PendingIntent mPermissionIntent;

    private UsbManager mUsbManager;
    private UsbDevice _usbDevice;
    private UsbInterface mInterface;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint writeEp, readEp;
    private int packetSize;

    private Handler mHandler;

    UsbConnector(Context context, TextView textView, int productId, int vendorId) {
        mContext = context;
        mTextView = textView;
        mTextView = textView;
        _productId = productId;
        _vendorId = vendorId;

        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);    //onDestroy(): unregisterReceiver(mUsbReceiver);

        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mHandler = new Handler();

        appendToLogView("AAA !!!");
    }

    private boolean mScanning = false; boolean isBleScanning() { return mScanning; }
    void scanUsbDevice(boolean enable) {
        if (enable == false && isBleScanning() == false) return;
        if (mUsbManager == null) {
            if (DEBUG) appendToLog("scanUsbDevice(" + enable + ") with NULL mUsbManager");
        } else if (enable == false) {
            if (DEBUG) appendToLog("scanUsbDevice(" + enable + ")");
            mScanning = false;
        } else {
            if (DEBUG) appendToLog("scanUsbDevice(" + enable + ")");
            mScanning = true;
        }
    }

    ArrayList<UsbDevice> getUsbDeviceList() {
        ArrayList<UsbDevice> usbDevicesList = new ArrayList<UsbDevice>();
        if (mUsbManager != null) {
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                if (device.getProductId() == _productId && device.getVendorId() == _vendorId) {
                    usbDevicesList.add(device);
                }
            }
        }
        return usbDevicesList;
    }

    boolean connect(ReaderDevice readerDevice) {
        String name = null;
        if (mUsbManager == null) {
            if (DEBUG) appendToLog("UsbConnector.connectBle() with NULL mUsbManager");
        } else if (readerDevice == null) {
            if (DEBUG) appendToLog("UsbConnector.connectBle() with NULL readerDevice");
        } else {
            name = readerDevice.getName();
            mConnectedTimeMillis = System.currentTimeMillis();
            boolean validPreviousConnect = false;
            boolean needNewConnect = true;
            if (DEBUG) appendToLog("connecting(" + name + ")");
            if (isBleConnected())   validPreviousConnect = true;
            if (validPreviousConnect) {
//                if (mBluetoothDevice.getAddress().matches(name)) {
                    needNewConnect = false;
//                } else {
//                    disconnect(true);
//                }
            }
            if (needNewConnect) {
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if (device.getProductId() == _productId && device.getVendorId() == _vendorId && device.getDeviceName().contains(readerDevice.getName())) {
                        _usbDevice = device;
                        if (DEBUG) appendToLog("connectBle(" + name + "): connectBle starts");

                        mUsbManager.requestPermission(_usbDevice, mPermissionIntent);
                        if (DEBUG) appendToLog("UsbConnector().connectBle(): AAA");

                        connectRequested = true;
                        return true;
                    }
                }
            }
            if (validPreviousConnect == false) {
                if (DEBUG) appendToLog("connectBle(" + name + "): connectBle FAIL");
//            } else if (mBluetoothGatt.connectBle()) {
//                appendToLog("connectBle(" + name + "): re-connectBle starts");
//                return true;
//            } else {
//                appendToLog("connectBle(" + name + "): re-connectBle FAIL");
            }
        }
        return false;
    }

    private final String ACTION_USB_PERMISSION = "com.example.company.app.testhid.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
//            if (DEBUG) appendToLog("UsbConnector().setDevice(): BBB");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                setDevice(context, intent);
            }
        }

        private void setDevice(Context context, Intent intent) {
//            appendToLog("UsbConnector().setDevice(): CCC");
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null) {
                Toast.makeText(context, "Permission request with NULL DEVICE.", Toast.LENGTH_SHORT).show();
            } else if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) == false) {
                //Log.d("TAG", "permission denied for the device " + device);
                Toast.makeText(context, "Permission is REJECTED.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Permission is Granted.", Toast.LENGTH_SHORT).show();
                makeConnection();
            }
        }
    };
    void makeConnection() {
        mConnection = mUsbManager.openDevice(_usbDevice);   //Release later: mConnection.close();
        if (mConnection == null) {
            if (DEBUG) appendToLog("UsbConnector().setDevice(): connection is NULL");
        } else {
            mInterface = _usbDevice.getInterface(0);
            mConnection.claimInterface(mInterface, true);    // Release later: mConnection.releaseInterface(mInterface);
            try {
                if (UsbConstants.USB_DIR_OUT == mInterface.getEndpoint(1).getDirection()) {
                    writeEp = mInterface.getEndpoint(1);
                }
                if (UsbConstants.USB_DIR_IN == mInterface.getEndpoint(0).getDirection()) {
                    readEp = mInterface.getEndpoint(0);
                    packetSize = readEp.getMaxPacketSize() * 50;
                }
                mHandler.removeCallbacks(mUsbConnectRunnable);
                mHandler.post(mUsbConnectRunnable);
                if (DEBUG) appendToLog("UsbConnector().setDevice(): connection is VALID");
            } catch (Exception ex) {
                mInterface = null; writeEp = null; readEp = null;
                if (DEBUG) appendToLog("UsbConnector().setDevice(): Exception = " + ex.toString());
            }
        }
    }

    private final int STREAM_IN_BUFFER_MAX = 0x4000; //0xC00;  //0x800;  //0x400;
    private final int STREAM_IN_BUFFER_LIMIT = 0x3F80;   //0xB80;    //0x780;    //0x380;
    private byte[] streamInBuffer = new byte[STREAM_IN_BUFFER_MAX];
    int streamInBufferHead, streamInBufferTail, streamInBufferSize = 0;
    int readBleSteamIn(byte[] buffer, int byteOffset, int byteCount) {
//        synchronized (streamInBuffer) {
        if (inventoryRunning) {
            if (DEBUG) appendToLog(String.format("readSteamIn(): Tail = %d, Head=%d, Size=%d", streamInBufferTail, streamInBufferHead, streamInBufferSize));
        }
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
//        }
    }

    int debugDisplayCounter = 0; int debugDisplayCounter1 = 0;
    boolean inventoryRunning = false; int inventoryInCounter = 0;
    int connectCounter = 0; int recCounter = 0;
    int connectRunInterval = 10; //5; //10;
    private final Runnable mUsbConnectRunnable = new Runnable() {
        @Override
        public void run() {
            boolean readConnectStatus = false;
//            appendToLogS("o ");
            mHandler.postDelayed(mUsbConnectRunnable, connectRunInterval);
            if (++connectCounter > 10 && inventoryRunning) { // && inventoryInCounter > 3) {
                connectCounter = 0;
                //mTextView.append("a "); //String.format("%d ", r));
//                appendToLogS(String.format("%d + %d", recCounter, streamInBufferSize)); recCounter = 0;
            }
            if (mConnection == null) {
                publishProgress("UsbConnector().mUsbConnectRunnable():  mConnection is NULL");
            } else {
                final byte[] bytes = new byte[packetSize];
                int r = 0;
//                    do {
                long timeStart = currentBleConnectTimeMillis();
                        r = mConnection.bulkTransfer(readEp, bytes, packetSize, 1);//5); //10);  //150
                        if (r < 0) {
                            if (inventoryRunning) {
                                if (debugDisplayCounter1++ < 5) {
                                    publishProgress(String.format("%d>bulktransfer1 duration = %d", currentBleConnectTimeMillis(), currentBleConnectTimeMillis()-timeStart) );
                                }
                            }
                            publishProgress("0"); //UsbConnector().mUsbConnectRunnable():  mConnection.bulkTransfer is ZERO");
                        } else {
                            if (inventoryRunning) {
                                inventoryInCounter++;
                                if (debugDisplayCounter++ < 5) {
                                    publishProgress(String.format("%d>bulktransfer duration = %d, length = %d, data(1) = %d", currentBleConnectTimeMillis(), currentBleConnectTimeMillis()-timeStart, r, bytes[1]) );
                                }
                            }
//                            publishProgress(String.format("UsbConnector().mUsbConnectRunnable(): mConnection.bulkTransfer lengths %s, content: %s", r, byteArrayToString(bytes)));
                            if (inventoryRunning) { // && inventoryInCounter > 3) {
                                recCounter += r;
    //                            mTextView.append(String.format("%d ", r));
                            }
                            if (false) { } else
                            if (streamInBufferSize + r >= streamInBuffer.length) {
                                publishProgress("111");
//                            } else if (inventoryRunning) {
//                                publishProgress("222");
                            } else if (bytes[0] == 1 && bytes[1] > 0) {
                                {
                                    if (isStreamInBufferRing) {
                                        streamInBufferPush(bytes, 2, bytes[1]);
                                    } else {
                                        System.arraycopy(bytes, 2, streamInBuffer, streamInBufferSize, bytes[1]);
                                    }
                                    streamInBufferSize += bytes[1];
//                                byte[] byteDisplay = new byte[streamInBufferSize];
//                                System.arraycopy(streamInBuffer, 0, byteDisplay, 0, byteDisplay.length);
//                                        publishProgress(String.format("\tMessage received of lengths %s, content: %s, streamInBuffer of lengths %s, content: %s", r, byteArrayToString(bytes), byteDisplay.length, byteArrayToString(byteDisplay)));
//                                        publishProgress(String.format("UsbConnector().mUsbConnectRunnable(): streamInBuffer of lengths %s, content: %s", byteDisplay.length, byteArrayToString(byteDisplay)));
                                }
                            }
                        }
//                    } while (r > 0);
                    readConnectStatus = true;
            }
            if (readConnectStatus) {
//                mHandler.postDelayed(mUsbConnectRunnable, 5);
//                } else {
//                    connectRequested = false;
            }
//            appendToLogS("p ");
        }
    };

    boolean connectRequested = false;
    boolean isBleConnected() {
        if (mUsbManager == null || _usbDevice == null || connectRequested == false)  return false;
        if (mUsbManager.hasPermission(_usbDevice) == false) return false;
        if (mConnection == null)    return false;
//        appendToLog("g ");
        return true;
    }

    void disconnect() {
        mHandler.removeCallbacks(mUsbConnectRunnable);
        connectRequested = false;
/*        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        appendToLog("disconnect(): connection_state=" + mBluetoothAdapter.getState() + ", state2=" + mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size() + ", state3=" + mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER).size());
        if (isBleConnected()) {
            if (mBluetoothAdapter == null) {
                appendToLog("disconnect() with NULL mBluetoothAdapter");
            } else if (!mBluetoothAdapter.isEnabled()) {
                appendToLog("disconnect(): DISABLED mBluetoothAdapter");
            } else if (mBluetoothGatt == null) {
                appendToLog("disconnect(): NULL mBluetoothGatt");
            } else {
                mBluetoothGatt.disconnect();
                if (tempDisconnect == false) mBluetoothDevice = null;
                appendToLog("disconnect(): done");
            }
        }*/
    }

    boolean writeStreamOut(byte[] value) {
/*        if (!isBleBusy() && characteristicListRead) {
            mReaderStreamOutCharacteristic.setValue(value);
            if (mBluetoothGatt.writeCharacteristic(mReaderStreamOutCharacteristic)) {
                _writeCharacteristic_in_progress = true;
                mStreamWriteCountOld = mStreamWriteCount;
                mStreamWriteCount += value.length;
                return true;
            }
        }*/
        if (DEBUG) appendToLog("writeStreamOut(): " + byteArrayToString(value));
        byte[] dataOut1 = value; //new byte[] { (byte)0xA7, (byte)0xE6, 2, (byte)0x6A, (byte)0x82, (byte)0x37, 0, 0, (byte)0x90, 0 };  //max len = 59
        int srcDataLength = dataOut1.length;
        byte[] dataOut = new byte[dataOut1.length + 2];
        dataOut1[1] = (byte)0xE6;
        dataOut[0] = 2;
        if (srcDataLength < 35) {
            dataOut[1] = (byte) srcDataLength;
        } else {
            dataOut[1] = 35;
        }
        System.arraycopy(dataOut1, 0, dataOut, 2, srcDataLength);
        return WriteData(dataOut);
    }

    int writeCounter = 0;
    boolean WriteData(byte[] bytes) {
/*        if (writeCounter > 2) {
            appendToLog("UsbConnector().WriteData()");
            return true;
        }*/
        if (_usbDevice == null || mConnection == null || mInterface == null || writeEp == null || mUsbManager.hasPermission(_usbDevice) == false) {
            if (DEBUG) appendToLog("UsbConnector().WriteData(): NULL somethings. Cannot writeData");
        } else {
//            mConnection.claimInterface(mInterface, true);    // Lock the usb interface.
            int r = mConnection.bulkTransfer(writeEp, bytes, bytes.length, 250);
            if (r <= 0) {
                if (DEBUG) appendToLog("UsbConnector().WriteData(): mConnection.bulkTransfer is ZERO");
            } else {
                if (DEBUG) appendToLog("UsbConnector().WriteData(" + writeCounter++ + "): " + String.format("Written %s bytes to the dongle. Data written: %s", r, byteArrayToString(bytes)));
            }
//            mConnection.releaseInterface(mInterface);
            if (r > 0) return true;
        }
        return false;
    }

    String byteArrayToString(byte[] packet) {
        StringBuilder sb = new StringBuilder(packet.length * 2);
        for (byte b : packet) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    String logString = "";
    String oldPublish = "";
    void publishProgress(String s) {
        if (oldPublish.contains(s)) return; oldPublish = s;
//        if (appended == false) return;
        logString += (", " + s);
    }
    boolean appended = false;
    String ssString = ""; int ssCount = 0; int ssInterval = 10;
    void appendToLogS(String s) {
        if (mTextView != null) {
            if (logString.length() > 0) mTextView.append(logString); logString = ""; oldPublish = "";
            String ss = currentBleConnectTimeMillis() + ">" + s;
            ssString += (", " + ss);
//            if (++ssCount >= ssInterval) {
                mTextView.append(ssString); ssCount = 0; ssString = "";
 //           }
            appended = true;
        }
    }
    void appendToLog(String s) {
        if (mTextView != null) {
            if (logString.length() > 0) mTextView.append(logString); logString = ""; oldPublish = "";
            mTextView.append("\n" + currentBleConnectTimeMillis() + ">" + s);
            appended = true;
        }
//        Log.v("Hello", "\n" + currentBleConnectTimeMillis() + ">" + s);
    }
    private long mConnectedTimeMillis;
    private long currentBleConnectTimeMillis() {
        return System.currentTimeMillis() - mConnectedTimeMillis;
    }

    private boolean isStreamInBufferRing = true;
    private void streamInBufferPush(byte[] inData, int inDataOffset, int length) {
        int length1 = streamInBuffer.length - streamInBufferTail;
        if (length > length1) {
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length1);
            length -= length1;
            inDataOffset += length1;
            streamInBufferTail = 0;
        }
        if (length != 0) {
            System.arraycopy(inData, inDataOffset, streamInBuffer, streamInBufferTail, length);
            streamInBufferTail += length;
        }
        if (DEBUG) appendToLogS(String.format("Push %d_%d", length, streamInBufferTail));
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
        if (DEBUG) appendToLogS(String.format("Pull %d_%d", length, streamInBufferHead));
    }

    long ltimeLogView;
    void appendToLogView(String s) {
        if (ltimeLogView == 0) ltimeLogView = System.currentTimeMillis();
        if (s.trim().length() == 0) mTextView.setText("");
        else mTextView.append(System.currentTimeMillis() - ltimeLogView + ": " + s + "\n" );
    }
}
