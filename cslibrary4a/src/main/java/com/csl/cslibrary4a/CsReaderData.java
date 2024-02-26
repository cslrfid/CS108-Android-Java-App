package com.csl.cslibrary4a;

public class CsReaderData {
    public enum ConnectorTypes {
        RFID, BARCODE, NOTIFICATION, SILICONLAB, BLUETOOTH, OTHER
    }
    public ConnectorTypes connectorTypes;
    public byte[] dataValues;
    public boolean invalidSequence;
    public long milliseconds;
}
