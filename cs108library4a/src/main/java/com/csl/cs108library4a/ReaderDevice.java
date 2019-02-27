package com.csl.cs108library4a;

import android.bluetooth.BluetoothDevice;

public class ReaderDevice implements Comparable<ReaderDevice>  {
    boolean isUsbDevice;
    private BluetoothDevice bluetoothDevice;
    private String name;
    private String address;
    boolean selected;
    private String details;
    int extra1Bank, extra2Bank, extra1Offset, extra2Offset;
    String strPc, strCrc16, strExtra1, strExtra2;
    private int count;
    private double rssi;
    private int phase, channel;
    private boolean isConnected;
    private String timeOfRead, timeZone;
    private String location;
    private String compass;

    public ReaderDevice(String name, String address, boolean selected, String details,
                              String strPc, String strCrc16,
                              String strExtra1, int extra1Bank, int extra1Offset,
                              String strExtra2, int extra2Bank, int extra2Offset,
                              String strTimeOfRead, String strTimeZone, String strLocation, String strCompass,
                              int count, double rssi, int phase, int channel) {
        if (address.contains(":")) {
            isUsbDevice = false;
        } else {
            isUsbDevice = true;
        }
        bluetoothDevice = null;
        this.name = name;
        this.address = address;
        this.selected = selected;
        this.details = details;
        this.strPc = strPc;
        this.strCrc16 = strCrc16;
        this.strExtra1 = strExtra1;
        this.extra1Bank = extra1Bank;
        this.extra1Offset = extra1Offset;
        this.strExtra2 = strExtra2;
        this.extra2Bank = extra2Bank;
        this.extra2Offset = extra2Offset;

        timeOfRead = strTimeOfRead;
        timeZone = strTimeZone;
        location = strLocation;
        compass = strCompass;

        this.count = count;
        this.rssi = rssi;

        this.phase = phase;
        this.channel = channel;
    }

    public ReaderDevice(BluetoothDevice bluetoothDevice, String name, String address, boolean selected, String details, int count, double rssi) {
        if (address == null) isUsbDevice = false;
        else if (address.contains(":")) {
            isUsbDevice = false;
        } else {
            isUsbDevice = true;
        }
        this.bluetoothDevice = bluetoothDevice;
        this.name = name;
        this.address = address;
        this.selected = selected;
        this.details = details;
        this.count = count;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDetails() {
        if (details == null) {
            String strDetail = "PC=" + strPc + ", CRC16=" + strCrc16;
            if (strExtra1 != null) {
                String strHeader = null;
                switch (extra1Bank) {
                    case 0:
                        strHeader = "RES";
                        break;
                    case 1:
                        strHeader = "EPC";
                        break;
                    case 2:
                        strHeader = "TID";
                        break;
                    case 3:
                        strHeader = "USER";
                        break;
                }
                if (strHeader != null)
                    strDetail += "\n" + strHeader + "=" + strExtra1;
            }
            if (strExtra2 != null) {
                String strHeader = null;
                switch (extra2Bank) {
                    case 0:
                        strHeader = "RES";
                        break;
                    case 1:
                        strHeader = "EPC";
                        break;
                    case 2:
                        strHeader = "TID";
                        break;
                    case 3:
                        strHeader = "USER";
                        break;
                }
                if (strHeader != null)
                    strDetail += "\n" + strHeader + "=" + strExtra2;
            }
            details = strDetail;
        }
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPc() { return strPc; }
    public String getRes() {
        if (extra1Bank == 0) return strExtra1;
        else if (extra2Bank == 0) return strExtra2;
        else return null;
    }
    public String getEpc() {
        if (extra1Bank == 1) return strExtra1;
        else if (extra2Bank == 1) return strExtra2;
        else return null;
    }
    public String getTid() {
        if (extra1Bank == 2) return strExtra1;
        else if (extra2Bank == 2) return strExtra2;
        else return null;
    }
    public String getUser() {
        if (extra1Bank == 3) return strExtra1;
        else if (extra2Bank == 3) return strExtra2;
        else return null;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setExtra1(String strExtra1, int extra1Bank, int extra1Offset) {
        this.strExtra1 = strExtra1;
        this.extra1Bank = extra1Bank;
        this.extra1Offset = extra1Offset;
        this.details = null; getDetails();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getTimeOfRead() { return timeOfRead; }
    public String getTimeZone() { return timeZone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCompass() { return compass; }
    public void setCcompass(String compass) { this.compass = compass; }

    BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ReaderDevice)) {
            return false;
        }
        final ReaderDevice readerDevice = (ReaderDevice) o;
        return ((this.getAddress() != null && readerDevice.getAddress() != null) && (readerDevice.getAddress().equalsIgnoreCase(this.getAddress())));
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 53 * hash + (this.getAddress() != null ? this.getAddress().hashCode() : 0);
        return hash;
    }

    public int compareTo(ReaderDevice other) {
        return address.compareTo(other.address);
    }
}
