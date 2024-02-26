package com.csl.cslibrary4a;

public class ReaderDevice implements Comparable<ReaderDevice>  {
    private String name;
    private String address; private String upcSerial;
    boolean selected;
    private String details;
    int extra1Bank, extra2Bank, extra1Offset, extra2Offset;
    String strPc, strXpc, strCrc16, strMdid, strExtra1, strExtra2;
    private int count;
    private double rssi;
    private int serviceUUID2p1;
    private int phase, channel, port;
    public final int INVALID_STATUS = -1;
    private int status = INVALID_STATUS;
    public static final int INVALID_BACKPORT = -1, INVALID_CODESENSOR = -1, INVALID_CODERSSI = -1, INVALID_BRAND = -1, INVALID_SENSORDATA = 0x1000; public final float INVALID_CODETEMPC = -300;
    private int backport1 = INVALID_BACKPORT, backport2 = INVALID_BACKPORT;
    private int codeSensor = INVALID_CODESENSOR, codeSensorMax = INVALID_CODESENSOR, codeRssi = INVALID_CODERSSI, sensorData = INVALID_SENSORDATA; private float codeTempC = INVALID_CODETEMPC; private String brand;
    private boolean isConnected;
    private String timeOfRead, timeZone;
    private String location;
    private String compass;

    public ReaderDevice(String name, String address, boolean selected, String details,
                        String strPc, String strXpc, String strCrc16, String strMdid,
                        String strExtra1, int extra1Bank, int extra1Offset,
                        String strExtra2, int extra2Bank, int extra2Offset,
                        String strTimeOfRead, String strTimeZone, String strLocation, String strCompass,
                        int count, double rssi, int phase, int channel, int port, int status, int backPort1, int backPort2, int codeSensor, int codeRssi, float codeTempC, String brand, int sensorData) {
        this.name = name;
        this.address = address;
        this.selected = selected;
        this.details = details;
        this.strPc = strPc;
        this.strXpc = strXpc;
        this.strCrc16 = strCrc16;
        this.strMdid = strMdid;
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
        this.port = port;
        this.status = status;
        this.backport1 = backPort1;
        this.backport2 = backPort2;
        this.codeSensor = codeSensor;
        this.codeRssi = codeRssi;
        this.codeTempC = codeTempC;
        this.brand = brand;
        this.sensorData = sensorData;
    }

    public ReaderDevice(String name, String address, boolean selected, String details, int count, double rssi, int serviceUUID2p1) {
        this.name = name;
        this.address = address;
        this.selected = selected;
        this.details = details;
        this.count = count;
        this.rssi = rssi;
        this.serviceUUID2p1 = serviceUUID2p1;
    }

    public ReaderDevice(String name, String address, boolean selected, String details, int count, double rssi) {
        this.name = name;
        this.address = address;
        this.selected = selected;
        this.details = details;
        this.count = count;
        this.rssi = rssi;
    }

    public String getName() {
        String strName = name;
        if (strName != null) {
            strName = strName.replaceAll("\\r|\\n", "");
            strName = strName.trim();
        }
        return strName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() { return address; }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpcSerial() { return upcSerial; }

    public void setUpcSerial(String upcSerial) {
        this.upcSerial = upcSerial;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDetails() {
        if (details == null) {
            String strDetail = "PC=" + strPc + ", CRC16=" + strCrc16; // + ", Port=" + String.valueOf(port+1);
            if (strXpc != null) strDetail += "\nXPC=" + strXpc;
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

    public String getXpc() {
        return strXpc;
    }
    public void setXpc(String strXpc) {
        this.strXpc = strXpc;
    }

    public String getRes() {
        if (extra1Bank == 0) return strExtra1;
        else if (extra2Bank == 0) return strExtra2;
        else return null;
    }
    public String getRes2() {
        if (extra2Bank == 0) return strExtra2;
        else if (extra1Bank == 0) return strExtra1;
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
    public String getMdid() {
        return strMdid;
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

    public int getServiceUUID2p1() { return serviceUUID2p1; }

    public int getPhase() { return phase; }
    public void setPhase(int phase) { this.phase = phase; }

    public int getChannel() {
        return channel;
    }
    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) {
        this.status = status;
    }

    public int getBackport1() {
        return backport1;
    }
    public void setBackport1(int backport1) {
        this.backport1 = backport1;
    }
    public int getBackport2() {
        return backport2;
    }
    public void setBackport2(int backport2) {
        this.backport2 = backport2;
    }

    public int getCodeSensor() { return codeSensor; }
    public void setCodeSensor(int codeSensor) {
        this.codeSensor = codeSensor;
    }

    public int getCodeSensorMax() { return codeSensorMax; }
    public void setCodeSensorMax(int codeSensor) {
        this.codeSensorMax = codeSensor;
    }

    public int getCodeRssi() {
        return codeRssi;
    }
    public void setCodeRssi(int codeRssi) {
        this.codeRssi = codeRssi;
    }

    public float getCodeTempC() { return codeTempC; }
    public void setCodeTempC(float codeTempC) {
        this.codeTempC = codeTempC;
    }

    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) { this.brand = brand; }

    public int getSensorData() {
        return sensorData;
    }
    public void setSensorData(int sensorData) { this.sensorData = sensorData; }

    public String getstrExtra1() { return strExtra1; }
    public void setExtra1(String strExtra1, int extra1Bank, int extra1Offset) {
        this.strExtra1 = strExtra1;
        this.extra1Bank = extra1Bank;
        this.extra1Offset = extra1Offset;
    }
    public String getstrExtra2() { return strExtra2; }
    public void setExtra2(String strExtra2, int extra2Bank, int extra2Offset) {
        this.strExtra2 = strExtra2;
        this.extra2Bank = extra2Bank;
        this.extra2Offset = extra2Offset;
    }
    void setExtra(String strExtra1, int extra1Bank, int extra1Offset, String strExtra2, int extra2Bank, int extra2Offset) {
        this.strExtra1 = strExtra1;
        this.extra1Bank = extra1Bank;
        this.extra1Offset = extra1Offset;
        this.strExtra2 = strExtra2;
        this.extra2Bank = extra2Bank;
        this.extra2Offset = extra2Offset;
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
