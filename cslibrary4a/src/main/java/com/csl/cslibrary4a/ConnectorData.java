package com.csl.cslibrary4a;

public class ConnectorData {
    public enum ConnectorTypes {
        RFID, BARCODE, NOTIFICATION, SILICONLAB, BLUETOOTH, OTHER
    }
    public ConnectorTypes connectorTypes;
    public byte[] dataValues;
    public boolean invalidSequence;
    public long milliseconds;
}
