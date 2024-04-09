package com.csl.cslibrary4a;

import android.content.Context;

import java.util.ArrayList;

public class RfidReader {
    RfidConnector rfidConnector; public RfidReaderChipR2000 rfidReaderChipR2000; RfidReaderChipE710 rfidReaderChipE710;
    ArrayList<RfidConnector.CsReaderRfidData> mRx000ToWrite;
    ArrayList<RfidReaderChipData.Rx000pkgData> mRx000ToRead;
    public ArrayList<RfidConnector.CsReaderRfidData> mRfidToWrite;
    Context context; Utility utility; CsReaderConnector108 csReaderConnector108; CsReaderConnector csReaderConnector; boolean bis108;
    public RfidReader(Context context, Utility utility, CsReaderConnector108 csReaderConnector108, CsReaderConnector csReaderConnector, boolean bis108) {
        this.context = context;
        this.utility = utility;
        this.csReaderConnector108 = csReaderConnector108;
        this.csReaderConnector = csReaderConnector;
        this.bis108 = bis108;

        rfidConnector = new RfidConnector(context, utility); mRfidToWrite = rfidConnector.mRfidToWrite;
        if (bis108) { //csReaderConnector108 != null) {
            appendToLog("bis108: new RfidReaderChipR2000 is created");
            rfidReaderChipR2000 = new RfidReaderChipR2000(context, utility, csReaderConnector108);
            mRx000ToWrite = rfidReaderChipR2000.mRx000ToWrite;
            mRx000ToRead = rfidReaderChipR2000.mRx000ToRead;
        } else {
            appendToLog("bis108: new RfidReaderChipE710 is created");
            rfidReaderChipE710 = new RfidReaderChipE710(context, utility, csReaderConnector);
            mRx000ToWrite = rfidReaderChipE710.mRx000ToWrite;
            mRx000ToRead = rfidReaderChipE710.mRx000ToRead;
            rfidConnector.rfidConnectorCallback = new RfidConnector.RfidConnectorCallback(){
                @Override
                public boolean callbackMethod(byte[] dataValues) {
                    return rfidReaderChipE710.decode710Data(dataValues);
                }
            };
        }
    }
    private void appendToLog(String s) { utility.appendToLog(s); }

    public boolean getRfidOnStatus() {
        return rfidConnector.getOnStatus();
    }
    public boolean isRfidFailure() {
        return rfidConnector.rfidFailure;
    }
    public int tagFocus = -1;
    public int getTagFocus() {
        if (bis108) { //csReaderConnector108 != null) {
            tagFocus = rfidReaderChipR2000.rx000Setting.getImpinjExtension();
            if (tagFocus > 0) tagFocus = ((tagFocus & 0x10) >> 4);
        } else {
            tagFocus = rfidReaderChipE710.rx000Setting.getImpinjExtension() & 0x04;
        }
        return tagFocus;
    }
    public boolean setTagFocus(boolean tagFocusNew) {
        boolean bRetValue;
        if (bis108) { //csReaderConnector108 != null) {
            bRetValue = rfidReaderChipR2000.rx000Setting.setImpinjExtension(tagFocusNew, (fastId > 0 ? true : false));
        } else {
            bRetValue = rfidReaderChipE710.rx000Setting.setImpinjExtension(tagFocusNew, (fastId > 0 ? true : false));
        }
        if (bRetValue) tagFocus = (tagFocusNew ? 1 : 0);
        return bRetValue;
    }
    public int fastId = -1;
    public int getFastId() {
        if (rfidReaderChipR2000 != null) {
            fastId = rfidReaderChipR2000.rx000Setting.getImpinjExtension();
            if (fastId > 0) fastId = ((fastId & 0x20) >> 5);
        } else {
            fastId = rfidReaderChipE710.rx000Setting.getImpinjExtension() & 0x02;
        }
        return fastId;
    }
    public boolean setFastId(boolean fastIdNew) {
        boolean bRetValue;
        if (bis108) { //csReaderConnector108 != null) {
            bRetValue = rfidReaderChipR2000.rx000Setting.setImpinjExtension((csReaderConnector108.rfidReader.tagFocus > 0 ? true : false), fastIdNew);
        } else {
            bRetValue = rfidReaderChipE710.rx000Setting.setImpinjExtension((csReaderConnector.rfidReader.tagFocus > 0 ? true : false), fastIdNew);
        }
        if (bRetValue) fastId = (fastIdNew ? 1 : 0);
        return bRetValue;
    }
    void macRead(int address) {
        if (rfidReaderChipR2000 != null) rfidReaderChipR2000.rx000Setting.readMAC(address);
        else rfidReaderChipE710.rx000Setting.readMAC(address);
    }
    public boolean macWrite(int address, long value) {
        if (rfidReaderChipR2000 != null) return rfidReaderChipR2000.rx000Setting.writeMAC(address, value);
        else return rfidReaderChipE710.rx000Setting.writeMAC(address, value);
    }
    public void set_fdCmdCfg(int value) {
        macWrite(0x117, value);
    }
    public void set_fdRegAddr(int addr) {
        macWrite(0x118, addr);
    }
    public void set_fdWrite(int addr, long value) {
        macWrite(0x118, addr);
        macWrite(0x119, value);
    }
    public void set_fdPwd(int value) {
        macWrite(0x11A, value);
    }
    public void set_fdBlockAddr4GetTemperature(int addr) {
        macWrite(0x11b, addr);
    }
    public void set_fdReadMem(int addr, long len) {
        macWrite(0x11c, addr);
        macWrite(0x11d, len);
    }
    public void set_fdWriteMem(int addr, int len, long value) {
        set_fdReadMem(addr, len);
        macWrite(0x11e, value);
    }
    public void setImpinJExtension(boolean tagFocus, boolean fastId) {
        if (rfidReaderChipR2000 != null) {
            int iValue = 0;
            if (tagFocus) iValue |= 0x10;
            if (fastId) iValue |= 0x20;
            boolean bRetValue;
            bRetValue = macWrite(0x203, iValue);
        } else {
            boolean bRetValue;
            bRetValue = rfidReaderChipE710.rx000Setting.setImpinjExtension(tagFocus, fastId);
            if (bRetValue) this.tagFocus = (tagFocus ? 1 : 0);
        }
    }

    public boolean isInventoring() {
        boolean bValue;
        if (bis108) bValue = rfidReaderChipR2000.isInventoring();
        else bValue = rfidReaderChipE710.isInventoring();
        //appendToLog("isInventoring " + bValue + " with bis108 " + bis108);
        return bValue;
    }
    public void setInventoring(boolean enable) {
        appendToLog("setInventoring " + enable + " with bis108 " + bis108);
        if (bis108) rfidReaderChipR2000.setInventoring(enable);
        else rfidReaderChipE710.setInventoring(enable);
    }

    void addRfidToWrite(RfidConnector.CsReaderRfidData csReaderRfidData) {
        if (bis108) rfidReaderChipR2000.addRfidToWrite(csReaderRfidData);
        else rfidReaderChipE710.addRfidToWrite(csReaderRfidData);
    }
    void mRx000UplinkHandler() {
        if (bis108) rfidReaderChipR2000.mRx000UplinkHandler();
        else rfidReaderChipE710.mRx000UplinkHandler();
    }
}
