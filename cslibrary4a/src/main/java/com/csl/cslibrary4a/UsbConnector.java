package com.csl.cslibrary4a;

import static android.content.Intent.parseUri;
import static android.hardware.usb.UsbConstants.USB_DIR_IN;
import static android.hardware.usb.UsbConstants.USB_DIR_OUT;
import static android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK;
import static android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_INT;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class UsbConnector {
    final boolean DEBUG = false;

    CsReaderConnector csReaderConnector;
    private Context context;
    private TextView textView;
    private int productId;
    private int vendorId;
    Utility utility;

    final String ACTION_USB_PERMISSION = "com.csl.cs710ademoapp.USB_PERMISSION";
    PendingIntent mPermissionIntent;

    private UsbManager usbManager;
    private UsbInterface usbInterface;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbEndpoint usbEndpointOut, usbEndpointIn;
    //private UsbEndpoint usbEndpoint4Write, usbEndpoint4Read;
    private int packetSize;

    private Handler mHandler = new Handler();
    PendingIntent permissionIntent;

    UsbConnector(CsReaderConnector csReaderConnector, Context context, TextView textView, int productId, int vendorId, Utility utility) {
        this.csReaderConnector = csReaderConnector;
        this.context = context;
        this.textView = textView;
        this.productId = productId;
        this.vendorId = vendorId;
        this.utility = utility;
        appendToLog("UsbConnector.UsbConnector");

        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        appendToLog("UsbConnector.UsbConnector: done getSystemService with usbManager is " + (usbManager == null ? "null" : "valid"));

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        ContextCompat.registerReceiver(context, broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
        appendToLog("UsbConnector.UsbConnector: done registerReceiver");
    }

    HashMap<String, UsbDevice> usbDeviceHashMap;
    private boolean scanning = false; boolean isBleScanning() { return scanning; }
    void scanDevice(boolean enable) {
        if (enable == false && isBleScanning() == false) return;

        if (enable) {
            if (true) {
                Intent intent = new Intent();
                UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                appendToLog("UsbConnector.scanDevice: usbDevice is " + (usbDevice == null ? "null" : "valid"));
            } //not working with the intent-filter, get only null intent
            if (false) {
                Intent intent = null;
                try {
                    //intent = getIntent(UsbManager.EXTRA_DEVICE);
                    intent = parseUri(UsbManager.EXTRA_DEVICE, 0);
                } catch (Exception ex) {
                    appendToLog("UsbConnector.scanDevice: getIntent exception " + ex.toString());
                }
                appendToLog("UsbConnector.scanDevice: intent is " + (intent == null ? "null" : "valid"));
                if (intent != null) {
                    appendToLog("UsbConnector.scanDevice: intent.getAction is " + intent.getAction());
                    if (intent.getAction().equals(ACTION_USB_DEVICE_ATTACHED)) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (usbDevice != null) {
                            Log.d("onResume", "USB device attached: name: " + usbDevice.getDeviceName());
                        }
                    }
                }
            } //not working with the intent-filter, get only intent with action android.intent.action.VIEW

            scanning = true;
            Thread thread = new Thread(runnable);
            thread.start();
        } else scanning = false;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            appendToLog("UsbConnector.runnable");

            if (usbManager != null) {
                usbDeviceHashMap = usbManager.getDeviceList();
                appendToLog("UsbConnector.runnable: deviceList is " + (usbDeviceHashMap == null ? "null" : ("valid with size = " + usbDeviceHashMap.size())));
                if (false) {
                    UsbDevice usbDevice = usbDeviceHashMap.get("/dev/bus/usb/001/004"); ///dev/bus/usb/001/002, 003
                    appendToLog("UsbConnector.runnable: usbDeviceHashMap.get[abc] is " + (usbDevice == null ? "null" : "valid"));
                } //alternative method to get usbDevice, but the device name is changing when plugging
                if (false) {
                    Iterator<UsbDevice> deviceIterator = usbDeviceHashMap.values().iterator();
                    appendToLog("UsbConnector.getUsbDeviceList: deviceIterator is " + (deviceIterator == null ? "null" : "valid"));
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                    }
                } //alternative method to get usbDevice
                for (UsbDevice device : usbDeviceHashMap.values()) {
                    if (!usbManager.hasPermission(device)) {
                        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                        usbManager.requestPermission(device, permissionIntent);
                        break;
                    }
                    appendToLog("UsbConnector.runnable: device.getDeviceName = " + device.getDeviceName());
                    appendToLog("UsbConnector.runnable: device.getVendorId = " + device.getVendorId());
                    appendToLog("UsbConnector.runnable: device.getManufacturerName = " + device.getManufacturerName()); //null
                    appendToLog("UsbConnector.runnable: device.getProductName = " + device.getProductName()); //null
                    appendToLog("UsbConnector.runnable: device.getProductId = " + device.getProductId());
                    appendToLog("UsbConnector.runnable: device.getDeviceId = " + device.getDeviceId());
                    appendToLog("UsbConnector.runnable: device.getVersion = " + device.getVersion());
                    //appendToLog("UsbConnector.runnable: device.getSerialNumber = " + device.getSerialNumber()); //exceptioin
                    BluetoothGatt.CsScanData scanResultA = new BluetoothGatt.CsScanData(null, 0, null);
                    scanResultA.address = String.valueOf(device.getVendorId())
                            + " " + String.valueOf(device.getProductId())
                            + " " + String.valueOf(device.getDeviceId());
                    switch (device.getVendorId()) {
                        case 1003: //Atmel
                            scanResultA.name = "CS701 USB HID";
                            scanResultA.serviceUUID = 3;
                            break;
                        case 4292: //Silicon Laboratory
                            scanResultA.name = "CS108 USB HID";
                            scanResultA.serviceUUID = 1;
                            break;
                    }
                    appendToLog("UsbConnector.runnable: scanResultA.name is " + scanResultA.name);

                    if (csReaderConnector.mScanResultList != null) {
                        csReaderConnector.mScanResultList.add(scanResultA);
                        appendToLog("UsbConnector.runnable: scanResultA.getAddress is " + csReaderConnector.mScanResultList.get(0).address);
                    }
                }
            }
            scanning = false;
        }
    };

    boolean usbConnectionState = false;
    boolean connect(ReaderDevice readerDevice) {
        boolean DEBUG = true;
        if (DEBUG) appendToLog("UsbConnector.connect: " + (readerDevice == null ? "null" : "valid") + " readerDevice");
        if (readerDevice == null) {
            if (DEBUG) appendToLog("UsbConnector.connect: NULL readerDevice");
        } else {
            String address = readerDevice.getAddress();
            if (DEBUG) appendToLog("UsbConnector.connect: readerDevice.address is " + address);
            String[] stringsAddress = address.split(" ");
            if (stringsAddress.length != 3) {
                if (DEBUG) appendToLog("UsbConnector.connect: stringsAddress does not have 3 parts");
            } else {
                int iDeviceIdExpected = Integer.parseInt(stringsAddress[stringsAddress.length - 1]);
                if (DEBUG) appendToLog("UsbConnector.connect: stringDeviceIdExpected = " + iDeviceIdExpected);

                if (DEBUG) appendToLog("UsbConnector.connect: " + (usbManager == null ? "null": "valid") + " usbManager, "
                        + (usbDeviceHashMap == null ? "null" : "valid") + " usbDeviceHashMap");
                if (usbManager == null) {
                    if (DEBUG) appendToLog("UsbConnector.connect: NULL mUsbManager");
                } else if (usbDeviceHashMap == null) {
                    if (DEBUG) appendToLog("UsbConnector.connect: NULL usbDeviceHashMap");
                } else {
                    boolean bValid = false;
                    if (DEBUG) appendToLog("UsbConnector.connect: usbDeviceHashMap.size = " + usbDeviceHashMap.size());
                    for (UsbDevice device : usbDeviceHashMap.values()) {
                        if (device == null) {
                            if (DEBUG) appendToLog("UsbConnector.connect: null usbDevice");
                        } else if (device.getDeviceId() == iDeviceIdExpected) {
                            if (DEBUG) appendToLog("UsbConnector.connect: found the corresponding usbDevice");

                            usbInterface = null;
                            int usbInterfaceCount = device.getInterfaceCount();
                            if (DEBUG) appendToLog("UsbConnector.connect: usbInterfaceCount = " + usbInterfaceCount);
                            for (int i = 0; i < usbInterfaceCount; i++) {
                                UsbInterface usbInterface1 = device.getInterface(i);
                                if (DEBUG) appendToLog("UsbConnector.connect: usbInterface[" + i + "] is "+ (usbInterface1 == null ? "null" : "valid"));
                                usbInterface = usbInterface1;
                            }

                            usbEndpointOut = null;
                            if (usbInterface == null) {
                                if (DEBUG) appendToLog("UsbConnector.connect: null usbInterface");
                            } else {
                                int usbEndpointCount = usbInterface.getEndpointCount();
                                if (DEBUG) appendToLog("UsbConnector.connect: usbEndpointCount = " + usbEndpointCount);
                                for (int i = 0; i < usbEndpointCount; i++) {
                                    UsbEndpoint usbEndpoint1 = usbInterface.getEndpoint(i);
                                    appendToLog("UsbConnector.connect: " + (usbEndpoint1 == null ? "null" : "valid") + " usbInterface[" + i
                                            + "], type = " + usbEndpoint1.getType()
                                            + ", direction = " + usbEndpoint1.getDirection());
                                    if (usbEndpoint1.getDirection() == USB_DIR_OUT) {
                                        appendToLog("UsbConnector.connect: found output endpoint with MaxPacketSize = " + usbEndpoint1.getMaxPacketSize());
                                        usbEndpointOut = usbEndpoint1;
                                    } else if (usbEndpoint1.getDirection() == USB_DIR_IN) {
                                        appendToLog("UsbConnector.connect: found input endpoint with MaxPacketSize = " + usbEndpoint1.getMaxPacketSize());
                                        usbEndpointIn = usbEndpoint1;
                                    }
                                }
                            }

                            usbDeviceConnection = null;
                            if (usbInterface == null || usbEndpointOut == null) {
                                if (DEBUG) appendToLog("UsbConnector.connect: null usbInterface or usbEndpointOut");
                            } else {
                                usbDeviceConnection = usbManager.openDevice(device);
                                if (DEBUG) appendToLog("UsbConnector.connect: usbDeviceConnection is " + (usbDeviceConnection == null ? "null" : "valid"));
                                usbDeviceConnection.claimInterface(usbInterface, true);
                                if (DEBUG) appendToLog("UsbConnector.connect: done claimInterface");
                            }
                            bValid = true;
                        }
                    }

                    if (bValid) {
                        usbConnectionState = true;
                        mConnectedTimeMillis = System.currentTimeMillis();
                        boolean validPreviousConnect = false;
                        boolean needNewConnect = true;
                        if (isConnected()) validPreviousConnect = true;
    /*
            if (validPreviousConnect) {
    //                if (mBluetoothDevice.getAddress().matches(name)) {
                    needNewConnect = false;
    //                } else {
    //                    disconnect(true);
    //                }
            }
            if (needNewConnect) {
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if (device.getProductId() == productId && device.getVendorId() == vendorId && device.getDeviceName().contains(readerDevice.getName())) {
                        _usbDevice = device;
                        if (DEBUG) appendToLog("UsbConnector: connectBle(" + name + "): connectBle starts");

                        usbManager.requestPermission(_usbDevice, mPermissionIntent);
                        if (DEBUG) appendToLog("UsbConnector: UsbConnector().connectBle(): AAA");

                        connectRequested = true;
                        return true;
                    }
                }
            }
            if (validPreviousConnect == false) {
                if (DEBUG) appendToLog("UsbConnector: connectBle(" + name + "): connectBle FAIL");
    //            } else if (mBluetoothGatt.connectBle()) {
    //                appendToLog("UsbConnector: connectBle(" + name + "): re-connectBle starts");
    //                return true;
    //            } else {
    //                appendToLog("UsbConnector: connectBle(" + name + "): re-connectBle FAIL");
            }
    */
                    }
                }
            }
        }
        return false;
    }

    boolean connectRequested = false;
    boolean isConnected() {
        return usbConnectionState;
/*
        boolean DEBUG = true;
        if (DEBUG) appendToLog("UsbConnector.isConnected: usbManger is " + (usbManager == null ? "null" : "valid")
                + "usbDevice is " + (usbDevice == null ? "null" : "valid")
                + "connectRequested is " + connectRequested);
        if (usbManager == null || usbDevice == null || connectRequested == false)  return false;
        if (DEBUG) appendToLog("UsbConnector.isConnected: usbManager.hasPermission is " + usbManager.hasPermission(usbDevice));
        if (!usbManager.hasPermission(usbDevice)) return false;
        if (DEBUG) appendToLog("UsbConnector.isConnected: usbDeviceConnection is " + (usbDeviceConnection == null ? "null" : "valid"));
        if (usbDeviceConnection == null)    return false;
        if (DEBUG) appendToLog("UsbConnector.isConnected: connected");
//        appendToLog("UsbConnector: g ");
        return true;
*/
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            appendToLog("UsbConnector.broadcastReceiver.onReceive: intent.getAction = " + action);
            //appendToLog("UsbConnector.broadcastReceiver.onReceive: intent.getBooleanExtra = " + intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)); //getBooleanExtra is always default value
            UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            appendToLog("UsbConnector.broadcastReceiver.onReceive: got usbDevice is " + (usbDevice == null ? "null" : ("with deviceId as " + usbDevice.getDeviceId())));
            if (action.equals(ACTION_USB_PERMISSION)) { //got usbDevice is null
                appendToLog("UsbConnector.broadcastReceiver.onReceive: ACTION_USB_PERMISSION");
                //setDevice(context, intent);
            } else if (action.equals(ACTION_USB_DEVICE_ATTACHED)) {
                appendToLog("UsbConnector.broadcastReceiver.onReceive: ACTION_USB_DEVICE_ATTACHED");
            } else if (action.equals(ACTION_USB_DEVICE_DETACHED)) {
                appendToLog("UsbConnector.broadcastReceiver.onReceive: ACTION_USB_DEVICE_DETACHED");
                if (usbDevice != null) {
                    if (usbDeviceConnection != null) {
                        appendToLog("UsbConnector.broadcastReceiver.onReceive: going to releaseInterface and close usbDeviceConnection");
                        usbDeviceConnection.releaseInterface(usbInterface);
                        usbDeviceConnection.close();
                    }
                    // call your method that cleans up and closes communication with the device
                }
            }
        }

        private void setDevice(Context context, Intent intent) {
            appendToLog("UsbConnector.usbReceiver.setDevice");
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

    boolean usbDataSending = false;
    byte[] dataOutPending = null;
    boolean writeStreamOut(byte[] dataOutRaw) {
        boolean DEBUG = true;

        appendToLog("UsbConnector.writeStreamOut, UsbConnector.waitStreamInOut.Runnable: raw dataOut " + byteArrayToString(dataOutRaw) + ", dataOutPending as "  + (dataOutPending == null ? "null" : "valid"));
        dataOutRaw[1] = (byte)0xE6;

        dataOutPending = new byte[dataOutRaw.length + 2];
        dataOutPending[0] = 2;
        dataOutPending[1] = (byte) dataOutRaw.length;
        System.arraycopy(dataOutRaw, 0, dataOutPending, 2, dataOutRaw.length);
        appendToLog("UsbConnector.writeStreamOut, UsbData: processed value: " + byteArrayToString(dataOutPending));

        if (true) waitStreamInOut();
        else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG)
                        appendToLog("UsbConnector.writeStreamOut.Runnable: " + (usbDeviceConnection == null ? "null" : "valid") + " usbDeviceConnection, "
                                + (usbEndpointOut == null ? "null" : "valid") + " usbEndpointOut, "
                                + (usbEndpointIn == null ? "null" : "valid") + " usbEndpointIn");
                    if (usbEndpointOut.getType() == USB_ENDPOINT_XFER_BULK) { //for sending, okay to use following even if endpoint type is INT
                        if (DEBUG)
                            appendToLog("UsbConnector.writeStreamOut.Runnable: going to do bulkTransfer with dataOut as " + byteArrayToString(dataOutPending));
                        int iLength = usbDeviceConnection.bulkTransfer(usbEndpointOut, dataOutPending, dataOutPending.length, 0);
                        if (DEBUG)
                            appendToLog("UsbConnector.writeStreamOut.Runnable: done bulkTransfer with length = " + iLength);
                    } else if (usbEndpointOut.getType() == USB_ENDPOINT_XFER_INT) {
                        if (DEBUG)
                            appendToLog("UsbConnector.writeStreamOut.Runnable: going to do interrrupt transfer with dataOut as " + byteArrayToString(dataOutPending));
                        if (dataOutPending.length > usbEndpointOut.getMaxPacketSize())
                            appendToLog("UsbConnector.writeStreamOut.Runnable: cannot transfer as dataOut length is greater than MaxPacketSize " + usbEndpointOut.getMaxPacketSize());
                        else {
                            int bufferDataLength = dataOutPending.length;
                            ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
                            UsbRequest usbRequest = new UsbRequest();
                            buffer.put(dataOutPending);
                            if (DEBUG)
                                appendToLog("UsbConnector.writeStreamOut.Runnable: going to usbRequest.queue with position = " + buffer.position());

                            usbDataSending = true;
                            usbRequest.initialize(usbDeviceConnection, usbEndpointOut);
                            usbRequest.queue(buffer, bufferDataLength);
                            try {
                                boolean bEqual = usbRequest.equals(usbDeviceConnection.requestWait());
                                if (DEBUG)
                                    appendToLog("UsbConnector.writeStreamOut.Runnable: done transfer with usbRequest.equals[usbDeviceConnection.requestWait] is " + bEqual + ", position = " + buffer.position());
                            } catch (Exception ex) {
                                appendToLog("UsbConnector.writeStreamOut.Runnable: requestWait exception as " + ex.toString());
                            }
                            usbDataSending = false;
                        }
                    } else {
                        if (DEBUG)
                            appendToLog("UsbConnector.writeStreamOut.Runnable: cannot handle as usbEndpointOut type is " + usbEndpointOut.getType());
                    }

                    if (DEBUG)
                        appendToLog("UsbConnector.writeStreamOut.Runnable: going to do threadStreamIn.start");
                    waitStreamInOut();
                }
            });
            thread.start();
        }

        return true;
    }

    private boolean disableUSBThread = false;
    public void waitStreamInOut() {
        if (!disableUSBThread) {
            disableUSBThread = true;
            appendToLog("UsbConnector.waitStreamInOut, UsbData: starts");
            Thread thread = new Thread(new Runnable() {
                boolean DEBUG = true;
                byte[] dataIn = new byte[16];

                @Override
                public void run() {
                    appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: starts");
                    while (true) {
                        appendToLog("UsbConnector.waitStreamInOut.Runnable: dataOutPending is " + (dataOutPending == null ? "null" : "valid"));
                        if (dataOutPending != null) {
                            if (DEBUG)
                                appendToLog("UsbConnector.waitStreamInOut.Runnable: " + (usbDeviceConnection == null ? "null" : "valid") + " usbDeviceConnection, "
                                        + (usbEndpointOut == null ? "null" : "valid") + " usbEndpointOut, "
                                        + (usbEndpointIn == null ? "null" : "valid") + " usbEndpointIn");
                            if (usbEndpointOut.getType() == USB_ENDPOINT_XFER_BULK) { //for sending, okay to use following even if endpoint type is INT
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: going to do bulkTransfer with dataOut as " + byteArrayToString(dataOutPending));
                                int iLength = usbDeviceConnection.bulkTransfer(usbEndpointOut, dataOutPending, dataOutPending.length, 0);
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: done bulkTransfer with length = " + iLength);
                            } else if (usbEndpointOut.getType() == USB_ENDPOINT_XFER_INT) {
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: going to do interrrupt transfer with dataOut as " + byteArrayToString(dataOutPending));
                                if (dataOutPending.length > usbEndpointOut.getMaxPacketSize())
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: cannot transfer as dataOut length is greater than MaxPacketSize " + usbEndpointOut.getMaxPacketSize());
                                else {
                                    int bufferDataLength = dataOutPending.length;
                                    ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
                                    UsbRequest usbRequest = new UsbRequest();
                                    buffer.put(dataOutPending);
                                    if (DEBUG)
                                        appendToLog("UsbConnector.waitStreamInOut.Runnable: going to usbRequest.queue with position = " + buffer.position());

                                    usbDataSending = true;
                                    usbRequest.initialize(usbDeviceConnection, usbEndpointOut);
                                    usbRequest.queue(buffer, bufferDataLength);
                                    try {
                                        boolean bEqual = usbRequest.equals(usbDeviceConnection.requestWait());
                                        if (DEBUG)
                                            appendToLog("UsbConnector.waitStreamInOut.Runnable: done transfer with usbRequest.equals[usbDeviceConnection.requestWait] is " + bEqual + ", position = " + buffer.position());
                                        Thread.sleep(100);
                                    } catch (Exception ex) {
                                        appendToLog("UsbConnector.waitStreamInOut.Runnable: requestWait exception as " + ex.toString());
                                    }
                                    usbDataSending = false;
                                }
                            } else if (DEBUG) appendToLog("UsbConnector.waitStreamInOut.Runnable: cannot handle as usbEndpointOut type is " + usbEndpointOut.getType());
                            dataOutPending = null;
                        } else {
                            if (usbEndpointIn.getType() == USB_ENDPOINT_XFER_BULK) {
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: going to do bulkTransfer to dataIn");
                                int iLength = usbDeviceConnection.bulkTransfer(usbEndpointIn, dataIn, dataIn.length, 0);
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: done bulkTransfer to usbEndpointIn with length = " + iLength);
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: buffer received is " + byteArrayToString(dataIn));

                            } else if (usbEndpointIn.getType() == USB_ENDPOINT_XFER_INT) { //for receiving, must use following if endpoint type is INT
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: going to do interrupt transfer to dataIn");
                                int bufferDataLength = usbEndpointIn.getMaxPacketSize();
                                ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
                                UsbRequest usbRequest = new UsbRequest();
                                if (DEBUG)
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: going to usbRequest.queue with position = " + buffer.position());
                                usbRequest.initialize(usbDeviceConnection, usbEndpointIn);
                                usbRequest.queue(buffer, bufferDataLength);
                                try {
                                    boolean bEqual = usbRequest.equals(usbDeviceConnection.requestWait(50));
                                    if (DEBUG)
                                        appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: done transfer with usbRequest.equals[usbDeviceConnection.requestWait] is " + bEqual + ", position = " + buffer.position());
                                    byte[] dataInRaw = buffer.array();
                                    if (DEBUG && dataInRaw[0] != 0)
                                        appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: buffer received is " + byteArrayToString(dataInRaw));

                                    if (bEqual && (buffer.position() > 0) && (dataInRaw[0] == 1) && (dataInRaw.length > (dataInRaw[1] + 2))) {
                                        dataIn = new byte[dataInRaw[1]];
                                        System.arraycopy(dataInRaw, 2, dataIn, 0, dataIn.length);
                                        if (DEBUG || true)
                                            appendToLog("UsbConnector.waitStreamInOut.Runnable, UsbData: dataIn " + dataIn.length + " is " + byteArrayToString(dataIn));
                                        synchronized (arrayListStreamIn) {
                                            //arrayListStreamIn.add(dataIn);
                                            streamInBufferPush(dataIn, 0, dataIn.length);
                                            streamInBufferSize += dataIn.length;
                                            if (DEBUG)
                                                appendToLog("UsbConnector.waitStreamInOut.Runnable: done streamInBufferPush with streamInRequest as " + streamInRequest);
                                            if (streamInRequest == false) {
                                                streamInRequest = true;
                                                if (DEBUG)
                                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: going to do runnableProcessStreamInData");
                                                mHandler.removeCallbacks(runnableProcessStreamInData);
                                                mHandler.post(runnableProcessStreamInData);
                                                if (DEBUG)
                                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: done post[runnableProcessStreamInData]");
                                            }
                                        }
                                    }
                                    //Thread.sleep(50);
                                } catch (Exception ex) {
                                    appendToLog("UsbConnector.waitStreamInOut.Runnable: Exception as " + ex.toString());
                                }
                            } else if (DEBUG) appendToLog("UsbConnector.waitStreamInOut.Runnable: cannot handle as usbEndpointIn type is " + usbEndpointIn.getType());
                        }
                    }
                }
            });
            thread.start();
        }
    }

    void processStreamInData() {
        appendToLog("UsbConnector.processStreamInData with connectorCallback " + (connectorCallback == null ? "null" : "valid"));
        if (connectorCallback != null) connectorCallback.callbackMethod();
    }

    public interface ConnectorCallback {
        void callbackMethod();
    }
    public ConnectorCallback connectorCallback = null;

    private boolean streamInRequest = false;
    private int intervalProcessBleStreamInData = 100; //50;
    public int getIntervalProcessBleStreamInData() { return intervalProcessBleStreamInData; }
    public final Runnable runnableProcessStreamInData = new Runnable() {
        @Override
        public void run() {
            streamInRequest = false;
            processStreamInData();
            //appendToLog("UsbConnector: post runnableProcessStreamInData within runnableProcessStreamInData");
            mHandler.postDelayed(runnableProcessStreamInData, intervalProcessBleStreamInData);
        }
    };

    private int streamInBufferSize = 0;
    public int getStreamInBufferSize() {
        appendToLog("UsbConnector.getStreamInBufferSize: streamInBufferSize = " + streamInBufferSize);
        return streamInBufferSize;
    }

/*
    private class UsbAsyncTask extends AsyncTask<Void, String, String> {
        protected MainActivity ma;

        public UsbAsyncTask() {
            super();
        }

        protected String doInBackground(String... strings) {
            boolean hasData = true;
            String allData = "";
            byte[] byteA = new byte[usbEndpointIn.getMaxPacketSize()];
            try {
                UsbRequest usbRequest = new UsbRequest();
                if (!usbRequest.initialize(usbDeviceConnection, usbEndpointIn)) {
                    return "";
                }
                while (hasData) {
                    allData = "";
                    hasData = usbDeviceConnection.bulkTransfer(usbEndpointIn, byteA, usbEndpointIn.getMaxPacketSize(), 1000) >= 0;
                    String result = Arrays.toString(byteA);
                    if (!hasData || result.isEmpty()) {
                        hasData = false;
                        if (startedScan) {
                            finishedScan = true;
                        }
                        return "";
                    } else {
                        startedScan = true;
                        byte[] bA = byteA;
                        for (int i = 0; i < bA.length; i++) {
                            allData += (char)bA[i];
                        }
                        lastDataReceived += allData;
                    }
                }
                usbRequest.close();

            } catch (Exception e) {
                finishedScan = false;
                startedScan = false;
                lastDataReceived = "";
                return " Error while reading ";
            }
            return allData;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            if (startedScan && finishedScan) {
                mLog.setText(lastDataReceived);
                lastDataReceived = "";
                finishedScan = false;
                startedScan = false;
            }
            parseUsb();
        }
    }
*/
    public void appendToLog(String s) {
        utility.appendToLog(s);
    }

    //////////////////////////////////////////////////////////////////////////////////


    void makeConnection() {
        /*
        usbDeviceConnection = usbManager.openDevice(usbDevice);   //Release later: mConnection.close();
        if (usbDeviceConnection == null) {
            if (DEBUG) appendToLog("UsbConnector().setDevice(): connection is NULL");
        } else {
            usbInterface = usbDevice.getInterface(0);
            usbDeviceConnection.claimInterface(usbInterface, true);    // Release later: mConnection.releaseInterface(mInterface);
            try {
                if (UsbConstants.USB_DIR_OUT == usbInterface.getEndpoint(1).getDirection()) {
                    usbEndpoint4Write = usbInterface.getEndpoint(1);
                }
                if (UsbConstants.USB_DIR_IN == usbInterface.getEndpoint(0).getDirection()) {
                    usbEndpoint4Read = usbInterface.getEndpoint(0);
                    packetSize = usbEndpoint4Read.getMaxPacketSize() * 50;
                }
                mHandler.removeCallbacks(mUsbConnectRunnable);
                mHandler.post(mUsbConnectRunnable);
                if (DEBUG) appendToLog("UsbConnector().setDevice(): connection is VALID");
            } catch (Exception ex) {
                usbInterface = null; usbEndpoint4Write = null; usbEndpoint4Read = null;
                if (DEBUG) appendToLog("UsbConnector().setDevice(): Exception = " + ex.toString());
            }
        }
         */
    }

    private final int STREAM_IN_BUFFER_MAX = 0x4000; //0xC00;  //0x800;  //0x400;
    private final int STREAM_IN_BUFFER_LIMIT = 0x3F80;   //0xB80;    //0x780;    //0x380;
    private byte[] streamInBuffer = new byte[STREAM_IN_BUFFER_MAX];
    int streamInBufferHead, streamInBufferTail;

    private long streamInDataMilliSecond;
    public long getStreamInDataMilliSecond() { return streamInDataMilliSecond; }
    int readSteamIn(byte[] buffer, int byteOffset, int byteCount) {
        appendToLog("UsbConnector.readSteamIn: starts with byteOffset = " + byteOffset + ", byteCount = " + byteCount + ", streamInBufferSize = " + streamInBufferSize);

        synchronized (streamInBuffer) {
            if (0 == streamInBufferSize) return 0;

            if (isArrayListStreamInBuffering) {
                int byteGot = 0;
                int length1 = arrayListStreamIn.get(0).data.length;
                appendToLog("UsbConnector.readSteamIn: arrayListStreamIn.size = " + arrayListStreamIn.size() + ", buffer.length = " + buffer.length
                        + ", byteOffset = " + byteOffset + ", length1 = " + length1);
                if (arrayListStreamIn.size() != 0 && buffer.length - byteOffset > length1) {
                    appendToLog("UsbConnector.readSteamIn: going to arraycopy");
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
            appendToLog("UsbConnector.readSteamIn: byteCount returned = " + byteCount);
            return byteCount;
        }
    }

    int debugDisplayCounter = 0; int debugDisplayCounter1 = 0;
    boolean inventoryRunning = false; int inventoryInCounter = 0;
    int connectCounter = 0; int recCounter = 0;
    int connectRunInterval = 10; //5; //10;
    private final Runnable mUsbConnectRunnable = new Runnable() {
        @Override
        public void run() {
            /*
            boolean readConnectStatus = false;
//            appendToLogS("o ");
            mHandler.postDelayed(mUsbConnectRunnable, connectRunInterval);
            if (++connectCounter > 10 && inventoryRunning) { // && inventoryInCounter > 3) {
                connectCounter = 0;
                //mTextView.append("a "); //String.format("%d ", r));
//                appendToLogS(String.format("%d + %d", recCounter, streamInBufferSize)); recCounter = 0;
            }
            if (usbDeviceConnection == null) {
                publishProgress("UsbConnector().mUsbConnectRunnable():  mConnection is NULL");
            } else {
                final byte[] bytes = new byte[packetSize];
                int r = 0;
//                    do {
                long timeStart = currentBleConnectTimeMillis();
                        r = usbDeviceConnection.bulkTransfer(usbEndpoint4Read, bytes, packetSize, 1);//5); //10);  //150
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
            */
        }
    };



    void disconnect() {
        mHandler.removeCallbacks(mUsbConnectRunnable);
        connectRequested = false;
/*        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        appendToLog("UsbConnector: disconnect(): connection_state=" + mBluetoothAdapter.getState() + ", state2=" + mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size() + ", state3=" + mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER).size());
        if (isBleConnected()) {
            if (mBluetoothAdapter == null) {
                appendToLog("UsbConnector: disconnect() with NULL mBluetoothAdapter");
            } else if (!mBluetoothAdapter.isEnabled()) {
                appendToLog("UsbConnector: disconnect(): DISABLED mBluetoothAdapter");
            } else if (mBluetoothGatt == null) {
                appendToLog("UsbConnector: disconnect(): NULL mBluetoothGatt");
            } else {
                mBluetoothGatt.disconnect();
                if (tempDisconnect == false) mBluetoothDevice = null;
                appendToLog("UsbConnector: disconnect(): done");
            }
        }*/
    }

    int writeCounter = 0;
    boolean WriteData(byte[] bytes) {
/*        if (writeCounter > 2) {
            appendToLog("UsbConnector().WriteData()");
            return true;
        }
        if (usbDevice == null || usbDeviceConnection == null || usbInterface == null || usbEndpoint4Write == null || usbManager.hasPermission(usbDevice) == false) {
            if (DEBUG) appendToLog("UsbConnector().WriteData(): NULL somethings. Cannot writeData");
        } else {
//            mConnection.claimInterface(mInterface, true);    // Lock the usb interface.
            int r = usbDeviceConnection.bulkTransfer(usbEndpoint4Write, bytes, bytes.length, 250);
            if (r <= 0) {
                if (DEBUG) appendToLog("UsbConnector().WriteData(): mConnection.bulkTransfer is ZERO");
            } else {
                if (DEBUG) appendToLog("UsbConnector().WriteData(" + writeCounter++ + "): " + String.format("Written %s bytes to the dongle. Data written: %s", r, byteArrayToString(bytes)));
            }
//            mConnection.releaseInterface(mInterface);
            if (r > 0) return true;
        }
 */
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
        if (textView != null) {
            if (logString.length() > 0) textView.append(logString); logString = ""; oldPublish = "";
            String ss = currentBleConnectTimeMillis() + ">" + s;
            ssString += (", " + ss);
//            if (++ssCount >= ssInterval) {
                textView.append(ssString); ssCount = 0; ssString = "";
 //           }
            appended = true;
        }
    }

    /*void appendToLog(String s) {
        if (textView != null) {
            if (logString.length() > 0) textView.append(logString); logString = ""; oldPublish = "";
            textView.append("\n" + currentBleConnectTimeMillis() + ">" + s);
            appended = true;
        }
//        Log.v("Hello", "\n" + currentBleConnectTimeMillis() + ">" + s);
    }*/
    private long mConnectedTimeMillis;
    private long currentBleConnectTimeMillis() {
        return System.currentTimeMillis() - mConnectedTimeMillis;
    }

    private int totalTemp, totalReceived;
    private long firstTime, totalTime;

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
                //appendToLog("UsbConnector: BtDataIn: totalReceived = " + totalReceived + ", totalTime = " + totalTime);
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


    long ltimeLogView;
    void appendToLogView(String s) {
        if (ltimeLogView == 0) ltimeLogView = System.currentTimeMillis();
        if (s.trim().length() == 0) textView.setText("");
        else textView.append(System.currentTimeMillis() - ltimeLogView + ": " + s + "\n" );
    }
}
