package com.csl.cslibrary4a;

import static java.lang.Math.log10;

import android.content.Context;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RfidReader {
    final boolean DEBUG = false;
    RfidConnector rfidConnector; public RfidReaderChipR2000 rfidReaderChipR2000; public RfidReaderChipE710 rfidReaderChipE710;
    ArrayList<RfidConnector.CsReaderRfidData> mRx000ToWrite;
    ArrayList<RfidReaderChipData.Rx000pkgData> mRx000ToRead;
    public ArrayList<RfidConnector.CsReaderRfidData> mRfidToWrite;
    Context context; Utility utility; boolean bis108; BluetoothGatt bluetoothGatt; SettingData settingData; NotificationConnector notificationConnector;
    public RfidReader(Context context, Utility utility, CsReaderConnector csReaderConnector, boolean bis108, BluetoothGatt bluetoothGatt, SettingData settingData, NotificationConnector notificationConnector) {
        this.context = context;
        this.utility = utility;
        this.bis108 = bis108;
        this.bluetoothGatt = bluetoothGatt;
        this.settingData = settingData;
        this.notificationConnector = notificationConnector;

        rfidConnector = new RfidConnector(context, utility); mRfidToWrite = rfidConnector.mRfidToWrite;
        if (bis108) {
            appendToLog("bis108: new RfidReaderChipR2000 is created");
            rfidReaderChipR2000 = new RfidReaderChipR2000(context, utility, csReaderConnector);
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
        fccFreqTableIdx = new int[50];
        int[] freqSortedINx = fccFreqSortedIdx;
        for (int i = 0; i < 50; i++) {
            fccFreqTableIdx[fccFreqSortedIdx[i]] = i;
        }
        for (int i = 0; i < 50; i++) {
            if (false) appendToLog("fccFreqTableIdx[" + i + "] = " + fccFreqTableIdx[i]);
        }
    }

    //============ utility ============
    private void appendToLog(String s) { utility.appendToLog(s); }

    //============ Rfid ============
    //============ Rfid ============
    //============ Rfid ============
    //============ Rfid ============
    //============ Rfid ============

    public boolean setInvAlgoNoSave(boolean dynamicAlgo) {
        appendToLog("writeBleStreamOut: going to setInvAlgo with dynamicAlgo = " + dynamicAlgo);
        return setInvAlgo1(dynamicAlgo);
    }
    public boolean setInvAlgo1(boolean dynamicAlgo) {
        boolean bValue = true, DEBUG = false;
        if (DEBUG) appendToLog("2 setInvAlgo1");
        int iAlgo = (bis108 ? rfidReaderChipR2000.rx000Setting.getInvAlgo() : rfidReaderChipE710.rx000Setting.getInvAlgo());
        int iRetry = getRetryCount();
        int iAbFlip = (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoAbFlip() : rfidReaderChipE710.rx000Setting.getAlgoAbFlip());
        if (DEBUG) appendToLog("2 setInvAlgo1: going to setInvAlgo with dynamicAlgo = " + dynamicAlgo + ", iAlgo = " + iAlgo + ", iRetry = " + iRetry + ", iabFlip = " + iAbFlip);
        if ( (dynamicAlgo && iAlgo == 0) || (dynamicAlgo == false && iAlgo == 3)) {
            bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setInvAlgo(dynamicAlgo ? 3 : 0) : rfidReaderChipE710.rx000Setting.setInvAlgo(dynamicAlgo ? 3 : 0));
            if (DEBUG) appendToLog("After setInvAlgo, bValue = " + bValue);
            if (DEBUG) appendToLog("Before setPopulation, population = " + getPopulation());
            if (bValue) bValue = setPopulation(getPopulation());
            if (DEBUG) appendToLog("After setPopulation, bValue = " + bValue);
            if (bValue) bValue = setRetryCount(iRetry);
            if (DEBUG) appendToLog("After setRetryCount, bValue = " + bValue);
            if (bValue) bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoAbFlip(iAbFlip) : rfidReaderChipE710.rx000Setting.setAlgoAbFlip(iAbFlip));
            if (DEBUG) appendToLog("After setAlgoAbFlip, bValue = " + bValue);
        }
        return bValue;
    }

    private final int FCC_CHN_CNT = 50;
    private final double[] FCCTableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, 905.25, 905.75, 906.25, 906.75, 907.25,//10
            907.75, 908.25, 908.75, 909.25, 909.75, 910.25, 910.75, 911.25, 911.75, 912.25,//20
            912.75, 913.25, 913.75, 914.25, 914.75, 915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    private final double[] FCCTableOfFreq0 = new double[] {
            903.75, 912.25, 907.75, 910.25, 922.75,     923.25, 923.75, 915.25, 909.25, 912.75,
            910.75, 913.75, 909.75, 905.25, 911.75,     902.75, 914.25, 918.25, 926.25, 925.75,
            920.75, 920.25, 907.25, 914.75, 919.75,     922.25, 903.25, 906.25, 905.75, 926.75,
            924.25, 904.75, 925.25, 924.75, 919.25,     916.75, 911.25, 921.25, 908.25, 908.75,
            913.25, 916.25, 904.25, 906.75, 917.75,     921.75, 917.25, 927.25, 918.75, 915.75 };
    private int[] fccFreqSortedIdx0;
    private final double[] FCCTableOfFreq1 = new double[] {
            915.25, 920.75, 909.25, 912.25, 918.25,     920.25, 909.75, 910.25, 919.75, 922.75,
            908.75, 913.75, 903.75, 919.25, 922.25,     907.75, 911.75, 923.75, 916.75, 926.25,
            908.25, 912.75, 924.25, 916.25, 927.25,     907.25, 910.75, 903.25, 917.75, 926.75,
            905.25, 911.25, 924.75, 917.25, 925.75,     906.75, 914.25, 904.75, 918.75, 923.25,
            902.75, 914.75, 905.75, 915.75, 925.25,     906.25, 921.25, 913.25, 921.75, 904.25 };
    private int[] fccFreqSortedIdx1;
    private int[] fccFreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            0x00180E4D, //915.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            0x00180E4B, //914.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E3D, //911.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E33, //908.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            0x00180E49, //914.25 MHz
            0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E39, //910.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private int[] fccFreqTableIdx;
    private final int[] fccFreqSortedIdx = new int[] {
            26, 25, 1, 48, 47,
            3, 49, 35, 33, 13,
            32, 30, 5, 4, 45,
            38, 24, 8, 22, 39,
            17, 18, 2, 12, 6,
            19, 7, 29, 23, 9,
            31, 27, 15, 16, 10,
            44, 14, 34, 28, 21,
            42, 11, 46, 20, 43,
            37, 36, 40, 0, 41 };
    private final int AUS_CHN_CNT = 10;
    private final double[] AUSTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25 };
    private final int[] AusFreqTable = new int[] {
            0x00180E63, // 920.75MHz
            0x00180E69, // 922.25MHz
            0x00180E6F, // 923.75MHz
            0x00180E73, // 924.75MHz
            0x00180E65, // 921.25MHz
            0x00180E6B, // 922.75MHz
            0x00180E71, // 924.25MHz
            0x00180E75, // 925.25MHz
            0x00180E67, // 921.75MHz
            0x00180E6D, // 923.25MHz
    };
    private final int[] ausFreqSortedIdx = new int[] {
            0, 3, 6, 8, 1,
            4, 7, 9, 2, 5 };

    private double[] PRTableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    private int[] freqTable = null;
    private int[] freqSortedIdx = null;

    private final int VZ_CHN_CNT = 10;
    private final double[] VZTableOfFreq = new double[] {
            922.75, 923.25, 923.75, 924.25, 924.75,
            925.25, 925.75, 926.25, 926.75, 927.25 };
    private final int[] vzFreqTable = new int[] {
            0x00180E77, // 925.75 MHz
            0x00180E6B, // 922.75MHz
            0x00180E7D, // 927.25 MHz
            0x00180E75, // 925.25MHz
            0x00180E6D, // 923.25MHz
            0x00180E7B, // 926.75 MHz
            0x00180E73, // 924.75MHz
            0x00180E6F, // 923.75MHz
            0x00180E79, // 926.25 MHz
            0x00180E71, // 924.25MHz
    };
    private final int[] vzFreqSortedIdx = new int[] {
            6, 0, 9, 5, 1,
            8, 4, 2, 7, 3 };

    private final int BR1_CHN_CNT = 24;
    private final double[] BR1TableOfFreq = new double[] {
            //902.75, 903.25, 903.75, 904.25, 904.75,
            //905.25, 905.75, 906.25, 906.75, 907.25,
            //907.75, 908.25, 908.75, 909.25, 909.75,
            //910.25, 910.75, 911.25, 911.75, 912.25,
            //912.75, 913.25, 913.75, 914.25, 914.75,
            //915.25,
            915.75, 916.25, 916.75, 917.25, 917.75,
            918.25, 918.75, 919.25, 919.75, 920.25,
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25,
            925.75, 926.25, 926.75, 927.25 };
    private final int[] br1FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            //0x00180E4D, //915.25 MHz
            //0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            //0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            //0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            //0x00180E25, //905.25 MHz
            //0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            //0x00180E4B, //914.75 MHz
            //0x00180E2B, //906.75 MHz
            //0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            //0x00180E3D, //911.25 MHz
            //0x00180E3F, //911.75 MHz
            //0x00180E1F, //903.75 MHz
            //0x00180E33, //908.75 MHz
            //0x00180E27, //905.75 MHz
            //0x00180E41, //912.25 MHz
            //0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            //0x00180E49, //914.25 MHz
            //0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            //0x00180E39, //910.25 MHz
            //0x00180E3B, //910.75 MHz
            //0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            //0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            //0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            //0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            //0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            //0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br1FreqSortedIdx = new int[] {
            0, 22, 21, 23, 9,
            7, 6, 4, 19, 12,
            13, 3, 5, 1, 18,
            8, 2, 16, 20, 17,
            11, 10, 14, 15 };

    private final int BR2_CHN_CNT = 33;
    private double[] BR2TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75,
            905.25, 905.75, 906.25, 906.75,
            //907.25, 907.75, 908.25, 908.75, 909.25,
            //909.75, 910.25, 910.75, 911.25, 911.75,
            //912.25, 912.75, 913.25, 913.75, 914.25,
            //914.75, 915.25,
            915.75, 916.25, 916.75, 917.25, 917.75,
            918.25, 918.75, 919.25, 919.75, 920.25,
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25,
            925.75, 926.25, 926.75, 927.25 };
    private final int[] br2FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            //0x00180E4D, //915.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            //0x00180E35, //909.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            //0x00180E4B, //914.75 MHz
            0x00180E2B, //906.75 MHz
            //0x00180E47, //913.75 MHz
            0x00180E69, //922.25 MHz
            //0x00180E3D, //911.25 MHz
            //0x00180E3F, //911.75 MHz
            0x00180E1F, //903.75 MHz
            //0x00180E33, //908.75 MHz
            0x00180E27, //905.75 MHz
            //0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E55, //917.25 MHz
            //0x00180E49, //914.25 MHz
            //0x00180E2D, //907.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            //0x00180E39, //910.25 MHz
            //0x00180E3B, //910.75 MHz
            //0x00180E2F, //907.75 MHz
            0x00180E73, //924.75 MHz
            //0x00180E37, //909.75 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
            //0x00180E45, //913.25 MHz
            0x00180E6F, //923.75 MHz
            //0x00180E31, //908.25 MHz
            0x00180E77, //925.75 MHz
            //0x00180E43, //912.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E1B, //902.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br2FreqSortedIdx = new int[] {
            9, 1, 31, 30, 3,
            32, 18, 16, 15, 13,
            5, 4, 28, 21, 8,
            22, 2, 6, 7, 12,
            14, 10, 27, 17, 11,
            25, 29, 26, 20, 19,
            23, 0, 24,
    };

    private final int BR3_CHN_CNT = 9;
    private final double[] BR3TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75 };
    private final int[] br3FreqTable = new int[] {
            0x00180E1D, //903.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E25, //905.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E29, //906.25 MHz
            0x00180E1B, //902.75 MHz
    };
    private final int[] br3FreqSortedIdx = new int[] {
            1, 3, 5, 4, 8,
            2, 6, 7, 0 };

    private final int BR4_CHN_CNT = 4;
    private final double[] BR4TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25 };
    private final int[] br4FreqTable = new int[] {
            0x00180E1D, //903.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E1F, //903.75 MHz
            0x00180E1B, //902.75 MHz
    };
    private final int[] br4FreqSortedIdx = new int[] {
            1, 3, 2, 0 };

    private final int BR5_CHN_CNT = 14;
    private final double[] BR5TableOfFreq = new double[] {
            917.75, 918.25, 918.75, 919.25, 919.75, // 4
            920.25, 920.75, 921.25, 921.75, 922.25, // 9
            922.75, 923.25, 923.75, 924.25 };
    private final int[] br5FreqTable = new int[] {
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E6F, //923.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] br5FreqSortedIdx = new int[] {
            5, 3, 2, 0, 8,
            9, 1, 4, 12, 13,
            7, 6, 10, 11 };

    private final int HK_CHN_CNT = 8;
    private final double[] HKTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25 };
    private final int[] hkFreqTable = new int[] {
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
            0x00180E71, //924.25MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E6D, //923.25MHz
            0x00180E6F, //923.75MHz
            0x00180E67, //921.75MHz
    };
    private final int[] hkFreqSortedIdx = new int[] {
            0, 3, 7, 1, 4,
            5, 6, 2 };

    private final int BD_CHN_CNT = 4;
    private final double[] BDTableOfFreq = new double[] {
            925.25, 925.75, 926.25, 926.75 };
    private final int[] bdFreqTable = new int[] {
            0x00180E75, //925.25MHz
            0x00180E77, //925.75MHz
            0x00180E79, //926.25MHz
            0x00180E7B, //926.75MHz
    };
    private final int[] bdFreqSortedIdx = new int[] {
            0, 3, 1, 2  };

    private final int TW_CHN_CNT = 12;
    private final double[] TWTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,
            924.75, 925.25, 925.75, 926.25, 926.75,
            927.25, 927.75 };
    private int[] twFreqTable = new int[] {
            0x00180E7D, //927.25MHz   10
            0x00180E73, //924.75MHz   5
            0x00180E6B, //922.75MHz   1
            0x00180E75, //925.25MHz   6
            0x00180E7F, //927.75MHz   11
            0x00180E71, //924.25MHz   4
            0x00180E79, //926.25MHz   8
            0x00180E6D, //923.25MHz   2
            0x00180E7B, //926.75MHz   9
            0x00180E69, //922.25MHz   0
            0x00180E77, //925.75MHz   7
            0x00180E6F, //923.75MHz   3
    };
    private final int[] twFreqSortedIdx = new int[] {
            10, 5, 1, 6, 11,
            4, 8, 2, 9, 0,
            7, 3 };

    private final int MYS_CHN_CNT = 8;
    private final double[] MYSTableOfFreq = new double[] {
            919.75, 920.25, 920.75, 921.25, 921.75,
            922.25, 922.75, 923.25 };
    private final int[] mysFreqTable = new int[] {
            0x00180E5F, //919.75MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E61, //920.25MHz
            0x00180E67, //921.75MHz
            0x00180E6D, //923.25MHz
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
    };
    private final int[] mysFreqSortedIdx = new int[] {
            0, 3, 6, 1, 4,
            7, 2, 5 };

    private final int ZA_CHN_CNT = 16;
    private final double[] ZATableOfFreq = new double[] {
            915.7, 915.9, 916.1, 916.3, 916.5,
            916.7, 916.9, 917.1, 917.3, 917.5,
            917.7, 917.9, 918.1, 918.3, 918.5,
            918.7 };
    private final int[] zaFreqTable = new int[] {
            0x003C23C5, //915.7 MHz
            0x003C23C7, //915.9 MHz
            0x003C23C9, //916.1 MHz
            0x003C23CB, //916.3 MHz
            0x003C23CD, //916.5 MHz
            0x003C23CF, //916.7 MHz
            0x003C23D1, //916.9 MHz
            0x003C23D3, //917.1 MHz
            0x003C23D5, //917.3 MHz
            0x003C23D7, //917.5 MHz
            0x003C23D9, //917.7 MHz
            0x003C23DB, //917.9 MHz
            0x003C23DD, //918.1 MHz
            0x003C23DF, //918.3 MHz
            0x003C23E1, //918.5 MHz
            0x003C23E3, //918.7 MHz
    };
    private final int[] zaFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10, 11, 12, 13, 14,
            15 };

    final int ID_CHN_CNT = 4;
    private final double[] IDTableOfFreq = new double[] {
            923.25, 923.75, 924.25, 924.75 };
    private final int[] indonesiaFreqTable = new int[] {
            0x00180E6D, //923.25 MHz
            0x00180E6F,//923.75 MHz
            0x00180E71,//924.25 MHz
            0x00180E73,//924.75 MHz
    };
    private final int[] indonesiaFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int IL_CHN_CNT = 7;
    private final double[] ILTableOfFreq = new double[] {
            915.25, 915.5, 915.75, 916.0, 916.25, // 4
            916.5, 916.75 };
    private final int[] ilFreqTable = new int[] {
            0x00180E4D, //915.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E4E, //915.5 MHz
            0x00180E52, //916.5 MHz
            0x00180E4F, //915.75 MHz
            0x00180E53, //916.75 MHz
            0x00180E50, //916.0 MHz
    };
    private final int[] ilFreqSortedIdx = new int[] {
            0, 4, 1, 5, 2,  6, 3 };

    private final int IL2019RW_CHN_CNT = 5;
    private final double[] IL2019RWTableOfFreq = new double[] {
            915.9, 916.025, 916.15, 916.275, 916.4 };
    private final int[] il2019RwFreqTable = new int[] {
            0x003C23C7, //915.9 MHz
            0x003C23C8, //916.025 MHz
            0x003C23C9, //916.15 MHz
            0x003C23CA, //916.275 MHz
            0x003C23CB, //916.4 MHz
    };
    private final int[] il2019RwFreqSortedIdx = new int[] {
            0, 4, 1, 2, 3 };

    private final int PH_CHN_CNT = 8;
    private final double[] PHTableOfFreq = new double[] {
            918.125, 918.375, 918.625, 918.875, 919.125, // 5
            919.375, 919.625, 919.875 };
    private final int[] phFreqTable = new int[] {
            0x00301CB1, //918.125MHz   Channel 0
            0x00301CBB, //919.375MHz   Channel 5
            0x00301CB7, //918.875MHz   Channel 3
            0x00301CBF, //919.875MHz   Channel 7
            0x00301CB3, //918.375MHz   Channel 1
            0x00301CBD, //919.625MHz   Channel 6
            0x00301CB5, //918.625MHz   Channel 2
            0x00301CB9, //919.125MHz   Channel 4
    };
    private final int[] phFreqSortedIdx = new int[] {
            0, 5, 3, 7, 1,  6, 2, 4 };

    private int NZ_CHN_CNT = 11;
    private final double[] NZTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,// 4
            924.75, 925.25, 925.75, 926.25, 926.75,// 9
            927.25 };
    private final int[] nzFreqTable = new int[] {
            0x00180E71, //924.25 MHz
            0x00180E77, //925.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E7B, //926.75 MHz
            0x00180E6D, //923.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E75, //925.25 MHz
            0x00180E6B, //922.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E6F, //923.75 MHz
            0x00180E73, //924.75 MHz
    };
    private final int[] nzFreqSortedIdx = new int[] {
            4, 7, 0, 9, 2,  10, 6, 1, 8, 3,     5 };

    private final int CN_CHN_CNT = 16;
    private final double[] CHNTableOfFreq = new double[] {
            920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375, 922.625, 922.875,
            923.125, 923.375, 923.625, 923.875, 924.125, 924.375 };
    private final int[] cnFreqTable = new int[] {
            0x00301CD3, //922.375MHz
            0x00301CD1, //922.125MHz
            0x00301CCD, //921.625MHz
            0x00301CC5, //920.625MHz
            0x00301CD9, //923.125MHz
            0x00301CE1, //924.125MHz
            0x00301CCB, //921.375MHz
            0x00301CC7, //920.875MHz
            0x00301CD7, //922.875MHz
            0x00301CD5, //922.625MHz
            0x00301CC9, //921.125MHz
            0x00301CDF, //923.875MHz
            0x00301CDD, //923.625MHz
            0x00301CDB, //923.375MHz
            0x00301CCF, //921.875MHz
            0x00301CE3, //924.375MHz
    };
    private final int[] cnFreqSortedIdx = new int[] {
            7, 6, 4, 0, 10,
            14, 3, 1, 9, 8,
            2, 13, 12, 11, 5,
            15 };

    private final int UH1_CHN_CNT = 10;
    private final double[] UH1TableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75 };
    private final int[] uh1FreqTable = new int[] {
            0x00180E4F, //915.75 MHz
            0x00180E4D, //915.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E57, //917.75 MHz
            0x00180E55, //917.25 MHz
            0x00180E59, //918.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E53, //916.75 MHz
    };
    private final int[] uh1FreqSortedIdx = new int[] {
            1, 0, 8, 7, 5,
            4, 6, 2, 9, 3 };

    private final int UH2_CHN_CNT = 15;
    private final double[] UH2TableOfFreq = new double[] {
            920.25, 920.75, 921.25, 921.75, 922.25,   // 4
            922.75, 923.25, 923.75, 924.25, 924.75,   // 9
            925.25, 925.75, 926.25, 926.75, 927.25 };
    private final int[] uh2FreqTable = new int[] {
            0x00180E7B, //926.75 MHz
            0x00180E79, //926.25 MHz
            0x00180E7D, //927.25 MHz
            0x00180E61, //920.25 MHz
            0x00180E75, //925.25 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E73, //924.75 MHz
            0x00180E6F, //923.75 MHz
            0x00180E77, //925.75 MHz
            0x00180E71, //924.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
            0x00180E6B, //922.75 MHz
            0x00180E6D, //923.25 MHz
    };
    private final int[] uh2FreqSortedIdx = new int[]{
            13, 12, 14, 0, 10,
            3, 4, 9, 7, 11,
            8, 2, 1, 5, 6, };

    private final int LH_CHN_CNT = 26;
    private double[] LHTableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75, 907.25, // 9
            907.75, 908.25, 908.75, 909.25, 909.75, // 14
            910.25, 910.75, 911.25, 911.75, 912.25, // 19
            912.75, 913.25, 913.75, 914.25, 914.75, // 24
            915.25, // 25
            //915.75, 916.25, 916.75, 917.25, 917.75,
            //918.25, 918.75, 919.25, 919.75, 920.25,
            //920.75, 921.25, 921.75, 922.25, 922.75,
            //923.25, 923.75, 924.25, 924.75, 925.25,
            //925.75, 926.25, 926.75, 927.25,
    };
    private final int[] lhFreqTable = new int[] {
            0x00180E1B, //902.75 MHz
            0x00180E35, //909.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E37, //909.75 MHz
            0x00180E1F, //903.75 MHz
            0x00180E39, //910.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E23, //904.75 MHz
            0x00180E3D, //911.25 MHz
            0x00180E25, //905.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E27, //905.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E29, //906.25 MHz
            0x00180E43, //912.75 MHz
            0x00180E2B, //906.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E2D, //907.25 MHz
            0x00180E47, //913.75 MHz
            0x00180E2F, //907.75 MHz
            0x00180E49, //914.25 MHz
            0x00180E31, //908.25 MHz
            0x00180E4B, //914.75 MHz
            0x00180E33, //908.75 MHz
            0x00180E4D, //915.25 MHz


            //0x00180E4F, //915.75 MHz
            //0x00180E7B, //926.75 MHz
            //0x00180E79, //926.25 MHz
            //0x00180E7D, //927.25 MHz
            //0x00180E61, //920.25 MHz
            //0x00180E5D, //919.25 MHz
            //0x00180E5B, //918.75 MHz
            //0x00180E57, //917.75 MHz
            //0x00180E75, //925.25 MHz
            //0x00180E67, //921.75 MHz
            //0x00180E69, //922.25 MHz
            //0x00180E55, //917.25 MHz
            //0x00180E59, //918.25 MHz
            //0x00180E51, //916.25 MHz
            //0x00180E73, //924.75 MHz
            //0x00180E5F, //919.75 MHz
            //0x00180E53, //916.75 MHz
            //0x00180E6F, //923.75 MHz
            //0x00180E77, //925.75 MHz
            //0x00180E71, //924.25 MHz
            //0x00180E65, //921.25 MHz
            //0x00180E63, //920.75 MHz
            //0x00180E6B, //922.75 MHz
            //0x00180E6D, //923.25 MHz
    };
    private final int[] lhFreqSortedIdx = new int[] {
            0, 13, 1, 14, 2,
            15, 3, 16, 4, 17,
            5, 18, 6, 19, 7,
            20, 8, 21, 9, 22,
            10, 23, 11, 24, 12,
            25 };

    private final int LH1_CHN_CNT = 14;
    private double[] LH1TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75, 907.25, // 9
            907.75, 908.25, 908.75, 909.25, // 13
    };
    private final int[] lh1FreqTable = new int[] {
            0x00180E1B, //902.75 MHz
            0x00180E35, //909.25 MHz
            0x00180E1D, //903.25 MHz
            0x00180E1F, //903.75 MHz
            0x00180E21, //904.25 MHz
            0x00180E23, //904.75 MHz
            0x00180E25, //905.25 MHz
            0x00180E27, //905.75 MHz
            0x00180E29, //906.25 MHz
            0x00180E2B, //906.75 MHz
            0x00180E2D, //907.25 MHz
            0x00180E2F, //907.75 MHz
            0x00180E31, //908.25 MHz
            0x00180E33, //908.75 MHz
    };
    private final int[] lh1FreqSortedIdx = new int[] {
            0, 13, 1, 2, 3,
            4, 5, 6, 7, 8,
            9, 10, 11, 12 };

    private final int LH2_CHN_CNT = 11;
    private double[] LH2TableOfFreq = new double[] {
            909.75, 910.25, 910.75, 911.25, 911.75, // 4
            912.25, 912.75, 913.25, 913.75, 914.25, // 9
            914.75 };
    private final int[] lh2FreqTable = new int[] {
            0x00180E37, //909.75 MHz
            0x00180E39, //910.25 MHz
            0x00180E3B, //910.75 MHz
            0x00180E3D, //911.25 MHz
            0x00180E3F, //911.75 MHz
            0x00180E41, //912.25 MHz
            0x00180E43, //912.75 MHz
            0x00180E45, //913.25 MHz
            0x00180E47, //913.75 MHz
            0x00180E49, //914.25 MHz
            0x00180E4B, //914.75 MHz
    };
    private final int[] lh2FreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10 };

    private final int ETSI_CHN_CNT = 4;
    private final double[] ETSITableOfFreq = new double[] {
            865.70, 866.30, 866.90, 867.50 };
    private final int[] etsiFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    private final int[] etsiFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int IDA_CHN_CNT = 3;
    private final double[] IDATableOfFreq = new double[] {
            865.70, 866.30, 866.90 };
    private final int[] indiaFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
    };
    private final int[] indiaFreqSortedIdx = new int[] {
            0, 1, 2 };

    private final int KR_CHN_CNT = 19;
    private final double[] KRTableOfFreq = new double[] {
            910.20, 910.40, 910.60, 910.80, 911.00, 911.20, 911.40, 911.60, 911.80, 912.00,
            912.20, 912.40, 912.60, 912.80, 913.00, 913.20, 913.40, 913.60, 913.80 };
    private int[] krFreqTable = new int[] {
            0x003C23A8, //912.8MHz   13
            0x003C23A0, //912.0MHz   9
            0x003C23AC, //913.2MHz   15
            0x003C239E, //911.8MHz   8
            0x003C23A4, //912.4MHz   11
            0x003C23B2, //913.8MHz   18
            0x003C2392, //910.6MHz   2
            0x003C23B0, //913.6MHz   17
            0x003C2390, //910.4MHz   1
            0x003C239C, //911.6MHz   7
            0x003C2396, //911.0MHz   4
            0x003C23A2, //912.2MHz   10
            0x003C238E, //910.2MHz   0
            0x003C23A6, //912.6MHz   12
            0x003C2398, //911.2MHz   5
            0x003C2394, //910.8MHz   3
            0x003C23AE, //913.4MHz   16
            0x003C239A, //911.4MHz   6
            0x003C23AA, //913.0MHz   14
        };
    private final int[] krFreqSortedIdx = new int[] {
            13, 9, 15, 8, 11,
            18, 2, 17, 1, 7,
            4, 10, 0, 12, 5,
            3, 16, 6, 14 };

    private final int KR2017RW_CHN_CNT = 6;
    private final double[] KR2017RwTableOfFreq = new double[] {
            917.30, 917.90, 918.50, 919.10, 919.70, 920.30 };
    private int[] kr2017RwFreqTable = new int[] {
            0x003C23D5, // 917.3 -> 917.25  MHz Channel 1
            0x003C23DB, //917.9 -> 918 MHz Channel 2
            0x003C23E1, //918.5 MHz Channel 3
            0x003C23E7, //919.1 -> 919  MHz Channel 4
            0x003C23ED, //919.7 -> 919.75 MHz Channel 5
            0x003C23F3 // 920.3 -> 920.25 MHz Channel 6
        };
    private final int[] kr2017RwFreqSortedIdx = new int[] {
            3, 0, 5, 1, 4, 2 };

    private final int JPN2012_CHN_CNT = 4;
    private final double[] JPN2012TableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40 };
    private final int[] jpn2012FreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            //0x003C23F6, //920.600MHz   Channel 5
            //0x003C23F8, //920.800MHz   Channel 6
    };
    private final int[] jpn2012FreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int JPN2012A_CHN_CNT = 6;
    private final double[] JPN2012ATableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40, 920.60, 920.80 };
    private final int[] jpn2012AFreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            0x003C23F6, //920.600MHz   Channel 5
            0x003C23F8, //920.800MHz   Channel 6
    };
    private final int[] jpn2012AFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4, 5 };

    private final int ETSIUPPERBAND_CHN_CNT = 4;
    private final double[] ETSIUPPERBANDTableOfFreq = new double[] {
            916.3, 917.5, 918.7, 919.9 };
    private final int[] etsiupperbandFreqTable = new int[] {
            0x003C23CB, //916.3 MHz
            0x003C23D7, //917.5 MHz
            0x003C23E3, //918.7 MHz
            0x003C23EF, //919.9 MHz
        };
    private final int[] etsiupperbandFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    private final int VN1_CHN_CNT = 3;
    private final double[] VN1TableOfFreq = new double[] {
            866.30, 866.90, 867.50 };
    private final int[] vietnam1FreqTable = new int[] {
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    private final int[] vietnam1FreqSortedIdx = new int[] {
            0, 1, 2 };

    private final int VN2_CHN_CNT = 8;
    private final double[] VN2TableOfFreq = new double[] {
            918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25 };
    private final int[] vietnam2FreqTable = new int[] {
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    private final int[] vietnam2FreqSortedIdx = new int[] {
            3, 1, 0, 6, 7, 2, 5, 4 };

    private final int VN3_CHN_CNT = 4;
    private final double[] VN3TableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25 };
    private final int[] vietnam3FreqTable = new int[] {
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    private final int[] vietnam3FreqSortedIdx = new int[] {
            2, 3, 1, 0 };
/*
    boolean setChannelData(RegionCodes regionCode) {
        return true;
    }
    private void SetFrequencyBand (UInt32 frequencySelector, BandState config, UInt32 multdiv, UInt32 pllcc)
    {
        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_SEL, frequencySelector);

        MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_CFG, (uint)config);

        if (config == BandState.ENABLE)
        {
            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDIVMULT, multdiv);

            MacWriteRegister(MACREGISTER.HST_RFTC_FRQCH_DESC_PLLDACCTL, pllcc);
        }
    }
*/
    public double[] GetAvailableFrequencyTable(RegionCodes regionCode) {
        double[] freqText = null;
    if (bis108) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        return FCCTableOfFreq0;
//                    case 1:
//                        return FCCTableOfFreq1;
//                    default:
//                        return FCCTableOfFreq;
//                }
                return FCCTableOfFreq;
            case PR:
                return PRTableOfFreq;
            case VZ:
                return VZTableOfFreq;
            case AU:
                return AUSTableOfFreq;
            case BR1:
                return BR1TableOfFreq;
            case BR2:
                return BR2TableOfFreq;
            case BR3:
                return BR3TableOfFreq;
            case BR4:
                return BR4TableOfFreq;
            case BR5:
                return BR5TableOfFreq;
            case HK:
            case SG:
            case TH:
            case VN:
                return HKTableOfFreq;
            case VN1:
                return VN1TableOfFreq;
            case VN2:
                return VN2TableOfFreq;
            case VN3:
                return VN3TableOfFreq;
            case BD:
                return BDTableOfFreq;
            case TW:
                return TWTableOfFreq;
            case MY:
                return MYSTableOfFreq;
            case ZA:
                return ZATableOfFreq;
            case ID:
                return IDTableOfFreq;
            case IL:
                return ILTableOfFreq;
            case IL2019RW:
                return IL2019RWTableOfFreq;
            case PH:
                return PHTableOfFreq;
            case NZ:
                return NZTableOfFreq;
            case CN:
                return CHNTableOfFreq;

            case UH1:
                return UH1TableOfFreq;
            case UH2:
                return UH2TableOfFreq;
            case LH:
                return LHTableOfFreq;
            case LH1:
                return LH1TableOfFreq;
            case LH2:
                return LH2TableOfFreq;

            case ETSI:
                appendToLog("Got ETSI Table of Frequencies");
                return ETSITableOfFreq;
            case IN:
                return IDATableOfFreq;
            case KR:
                return KRTableOfFreq;
            case KR2017RW:
                return KR2017RwTableOfFreq;
            case JP:
                return JPN2012TableOfFreq;
            case JP6:
                return JPN2012ATableOfFreq;
            case ETSIUPPERBAND:
                return ETSIUPPERBANDTableOfFreq;

            default:
                return new double[0];
        }
    } else {
        int iRegionEnum = regionCode.ordinal() - RegionCodes.Albania1.ordinal() + 1;
        if (DEBUG) appendToLog("regionCode = " + regionCode.toString() + ", iRegionEnum = " + iRegionEnum);

        String strChannelCount = strCountryEnumInfo[(iRegionEnum - 1) * iCountryEnumInfoColumn + 3];
        int iChannelCount = -1;
        try {
            iChannelCount = Integer.parseInt(strChannelCount);
        } catch (Exception ex) { }
        if (DEBUG) appendToLog("strChannelCount = " + strChannelCount + ", iChannelCount = " + iChannelCount);

        String strChannelSeparation = strCountryEnumInfo[(iRegionEnum - 1) * iCountryEnumInfoColumn + 5];
        int iChannelSeparation = -1;
        try {
            iChannelSeparation = Integer.parseInt(strChannelSeparation);
        } catch (Exception ex) { }
        if (DEBUG) appendToLog("strChannelSeparation = " + strChannelSeparation + ",iChannelSeparation = " + iChannelSeparation);

        String strChannelFirst = strCountryEnumInfo[(iRegionEnum - 1) * iCountryEnumInfoColumn + 6];
        double dChannelFirst = -1;
        try {
            dChannelFirst = Double.parseDouble(strChannelFirst);
        } catch (Exception ex) { }
        if (DEBUG) appendToLog("strChannelFirst = " + strChannelFirst + ", dChannelFirst = " + dChannelFirst);

        if (iChannelCount > 0) {
            freqText = new double[iChannelCount];
            for (int i = 0; i < iChannelCount; i++) {
                freqText[i] = dChannelFirst + ((double) iChannelSeparation) / 1000 * i;
                if (DEBUG) appendToLog("Frequency freqTable[" + i + "] = " + freqText[i]);
            }
        }
        return freqText;
    }
    }
    public int[] FreqIndex(RegionCodes regionCode) {
    if (bis108) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        return fccFreqSortedIdx0;
//                    case 1:
//                        return fccFreqSortedIdx1;
//                    default:
//                        return fccFreqSortedIdx;
//                }
                return fccFreqSortedIdx;
            case PR:
                if (freqSortedIdx == null) {
                    freqSortedIdx = new int[PRTableOfFreq.length];
                    if (DEBUG) appendToLog("PR: freqSortedIdx size = " + freqSortedIdx.length);
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    for (int i = 0; i < freqSortedIdx.length; i++) list.add(new Integer(i));
                    Collections.shuffle(list);
                    for (int i = 0; i < freqSortedIdx.length; i++) {
                        freqSortedIdx[i] = list.get(i);
                        if (DEBUG) appendToLog("PR: Random Value = " + freqSortedIdx[i]);
                    }
                }
                return freqSortedIdx;
            case VZ:
                return vzFreqSortedIdx;
            case AU:
                return ausFreqSortedIdx;
            case BR1:
                return br1FreqSortedIdx;
            case BR2:
                return br2FreqSortedIdx;
            case BR3:
                return br3FreqSortedIdx;
            case BR4:
                return br4FreqSortedIdx;
            case BR5:
                return br5FreqSortedIdx;
            case HK:
            case SG:
            case TH:
            case VN:
                return hkFreqSortedIdx;
            case VN1:
                return vietnam1FreqSortedIdx;
            case VN2:
                return vietnam2FreqSortedIdx;
            case VN3:
                return vietnam3FreqSortedIdx;
            case BD:
                return bdFreqSortedIdx;
            case TW:
                return twFreqSortedIdx;
            case MY:
                return mysFreqSortedIdx;
            case ZA:
                return zaFreqSortedIdx;
            case ID:
                return indonesiaFreqSortedIdx;
            case IL:
                return ilFreqSortedIdx;
            case IL2019RW:
                return il2019RwFreqSortedIdx;
            case PH:
                return phFreqSortedIdx;
            case NZ:
                return nzFreqSortedIdx;
            case CN:
                return cnFreqSortedIdx;

            case UH1:
                return uh1FreqSortedIdx;
            case UH2:
                return uh2FreqSortedIdx;
            case LH:
                return lhFreqSortedIdx;
            case LH1:
                return lh1FreqSortedIdx;
            case LH2:
                return lh2FreqSortedIdx;

            case ETSI:
                return etsiFreqSortedIdx;
            case IN:
                return indiaFreqSortedIdx;
            case KR:
                return krFreqSortedIdx;
            case KR2017RW:
                return kr2017RwFreqSortedIdx;
            case JP:
                return jpn2012FreqSortedIdx;
            case JP6:
                return jpn2012AFreqSortedIdx;
            case ETSIUPPERBAND:
                return etsiupperbandFreqSortedIdx;

            default:
                return null;
        }
    } else {
        int[] iFreqSortedIdx = null;
        int iFreqChnCnt = FreqChnCnt(regionCode);
        if (iFreqChnCnt > 0) {
            iFreqSortedIdx = new int[iFreqChnCnt];
            for (int i = 0; i < iFreqChnCnt; i++) {
                iFreqSortedIdx[i] = (byte)i;
                if (false) appendToLog("Frequency index[" + i + "] = " + iFreqSortedIdx[i]);
            }
        }
        return iFreqSortedIdx;
    }
    }
    public int[] FreqTable(RegionCodes regionCode) {
        switch (regionCode) {
            case FCC:
            case AG:
            case CL:
            case CO:
            case CR:
            case DR:
            case MX:
            case PM:
            case UG:
//                int[] freqTableIdx = fccFreqTableIdx;
//                int[] freqSortedIdx;
//                int[] freqTable = new int[50];
//                if (DEBUG) appendToLog("gerVersionCode = " + mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode());
//                switch (mRfidDevice.mRx000Device.mRx000OemSetting.getVersionCode()) {
//                    case 0:
//                        freqSortedIdx = fccFreqSortedIdx0;
//                        break;
//                    case 1:
//                        freqSortedIdx = fccFreqSortedIdx1;
//                        break;
//                    default:
//                        freqSortedIdx = fccFreqSortedIdx;
//                        break;
//                }
//                for (int i = 0; i < 50; i++) {
//                    freqTable[i] = fccFreqTable[fccFreqTableIdx[freqSortedIdx[i]]];
//                    if (DEBUG) appendToLog("i = " + i + ", freqSortedIdx = " + freqSortedIdx[i] + ", fccFreqTableIdx = " + fccFreqTableIdx[freqSortedIdx[i]] + ", freqTable[" + i + "] = " + freqTable[i]);
//                }
//                return freqTable;
                return fccFreqTable;
            case PR:
                int[] freqSortedIndex = FreqIndex(regionCode);
                int[] freqTable = null;
                if (freqSortedIndex != null) {
                    freqTable = new int[freqSortedIndex.length];
                    for (int i = 0; i < freqSortedIndex.length; i++) {
                        int j = 0;
                        for (; j < FCCTableOfFreq.length; j++) {
                            if (FCCTableOfFreq[j] == PRTableOfFreq[freqSortedIndex[i]]) break;
                        }
                        freqTable[i] = fccFreqTable[fccFreqTableIdx[j]];
                    }
                } else
                if (DEBUG) appendToLog("NULL freqSortedIndex");
                return freqTable;   // return prFreqTable;
            case VZ:
                return vzFreqTable;
            case AU:
                return AusFreqTable;

            case BR1:
                return br1FreqTable;
            case BR2:
                return br2FreqTable;
            case BR3:
                return br3FreqTable;
            case BR4:
                return br4FreqTable;
            case BR5:
                return br5FreqTable;

            case HK:
            case SG:
            case TH:
            case VN:
                return hkFreqTable;
            case VN1:
                return vietnam1FreqTable;
            case VN2:
                return vietnam2FreqTable;
            case VN3:
                return vietnam3FreqTable;
            case BD:
                return bdFreqTable;
            case TW:
                return twFreqTable;
            case MY:
                return mysFreqTable;
            case ZA:
                return zaFreqTable;

            case ID:
                return indonesiaFreqTable;
            case IL:
                return ilFreqTable;
            case IL2019RW:
                return il2019RwFreqTable;
            case PH:
                return phFreqTable;
            case NZ:
                return nzFreqTable;
            case CN:
                return cnFreqTable;

            case UH1:
                return uh1FreqTable;
            case UH2:
                return uh2FreqTable;
            case LH:
                return lhFreqTable;
            case LH1:
                return lh1FreqTable;
            case LH2:
                return lh2FreqTable;

            case ETSI:
                return etsiFreqTable;
            case IN:
                return indiaFreqTable;
            case KR:
                return krFreqTable;
            case KR2017RW:
                return kr2017RwFreqTable;
            case JP:
                return jpn2012FreqTable;
            case JP6:
                return jpn2012AFreqTable;
            case ETSIUPPERBAND:
                return etsiupperbandFreqTable;

            default:
                return null;
        }
    }
    private long GetPllcc(RegionCodes regionCode) {
        switch (regionCode) {
            case ETSI:
            case IN:
                return 0x14070400;  //Notice: the read value is 0x14040400
        }
        return 0x14070200;  //Notice: the read value is 0x14020200
    }
    private boolean FreqChnWithinRange(int Channel, RegionCodes regionCode) {
        int TotalCnt = FreqChnCnt(regionCode);
        if (TotalCnt <= 0)   return false;
        if (Channel >= 0 && Channel < TotalCnt) return true;
        return false;
    }
    private int FreqSortedIdxTbls(RegionCodes regionCode, int Channel) {
        int TotalCnt = FreqChnCnt(regionCode);
        int[] freqIndex = FreqIndex(regionCode);
        if (!FreqChnWithinRange(Channel, regionCode) || freqIndex == null)
            return -1;
        for (int i = 0; i < TotalCnt; i++) {
            if (freqIndex[i] == Channel)    return i;
        }
        return -1;
    }
    byte tagDelayDefaultCompactSetting = 0;
    public byte tagDelayDefaultNormalSetting = 30;
    public byte tagDelaySettingDefault = tagDelayDefaultCompactSetting, tagDelaySetting = tagDelaySettingDefault;
    public long cycleDelaySettingDefault = 0, cycleDelaySetting = cycleDelaySettingDefault;
    /*
    byte[] string2ByteArray(String string) {
        byte[] bytes = null;
        if (string == null) return null;
        if ((string.length()/2)*2 != string.length()) string += "0";
        for (int i = 0; i < string.length(); i+=2) {
            try {
                Short sValue = Short.parseShort(string.substring(i, i + 2), 16);
                byte[] bytesNew = new byte[1];
                if (bytes != null) {
                    bytesNew = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, bytesNew, 0, bytes.length);
                }
                bytesNew[bytesNew.length - 1] = (byte) (sValue & 0xFF);
                bytes = bytesNew;
            } catch (Exception ex) {
                appendToLog("Exception in i = " + i + ", substring = " + string.substring(i, i+2));
                break;
            }
        }
        return bytes;
    }
*/
    public boolean starAuthOperation() {
        if (bis108) {
            rfidReaderChipR2000.setPwrManagementMode(false);
            return rfidReaderChipR2000.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE);
        }
        rfidReaderChipE710.setPwrManagementMode(false);
        return rfidReaderChipE710.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE);
    }

    //============ Rfid ============
    //============ Rfid ============

    public String getAuthMatchData() {
        int iValue1 = 96;
        String strValue;
        if (bis108) strValue = rfidReaderChipR2000.rx000Setting.getAuthMatchData();
        else strValue = rfidReaderChipE710.rx000Setting.getAuthMatchData();
        if (strValue == null) return null;
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1)  strLength++;
        return strValue.substring(0, strLength);
    }
    public boolean setAuthMatchData(String mask) {
        boolean result = false;
        if (mask != null) {
            if (bis108) result = rfidReaderChipR2000.rx000Setting.setAuthMatchData(mask);
            else result = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(mask.getBytes());
        }
        return result;
    }
    public int getStartQValue() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoStartQ(3) : rfidReaderChipE710.rx000Setting.getAlgoStartQ(3));
    }
    public int getMaxQValue() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoMaxQ(3) : rfidReaderChipE710.rx000Setting.getAlgoMaxQ(3));
    }
    public int getMinQValue() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoMinQ(3) : rfidReaderChipE710.rx000Setting.getAlgoMinQ(3));
    }
    public boolean setDynamicQParms(int startQValue, int minQValue, int maxQValue, int retryCount) {
        appendToLog("setTagGroup: going to setAlgoSelect with input as 3");
        boolean result;
        result = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoSelect(3) : rfidReaderChipE710.rx000Setting.setAlgoSelect(3));
        if (result) {
            result = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoStartQ(startQValue, maxQValue, minQValue, -1, -1, -1) : rfidReaderChipE710.rx000Setting.setAlgoStartQ(startQValue, maxQValue, minQValue, -1, -1, -1));
        }
        if (result) result = setRetryCount(retryCount);
        return result;
    }
    public int getFixedQValue() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoStartQ(0) : rfidReaderChipE710.rx000Setting.getAlgoStartQ(0));
    }
    public int getFixedRetryCount() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoRetry(0) : rfidReaderChipE710.rx000Setting.getAlgoMinQCycles());
    }
    public boolean getRepeatUnitNoTags() {
        if (bis108) return rfidReaderChipR2000.rx000Setting.getAlgoRunTilZero(0) == 1 ? true : false;
        return rfidReaderChipE710.rx000Setting.getAlgoRunTilZero(0) == 1 ? true : false;
    }
    public boolean setFixedQParms(int qValue, int retryCount, boolean repeatUnitNoTags) {
        boolean result, DEBUG = false;
        if (DEBUG) appendToLog("qValue=" + qValue + ", retryCount = " + retryCount + ", repeatUntilNoTags = " + repeatUnitNoTags);
        result = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoSelect(0) : rfidReaderChipE710.rx000Setting.setAlgoSelect(0));
        if (DEBUG) appendToLog("after setAlgoSelect, result = " + result);
        if (qValue == getFixedQValue() && retryCount == getFixedRetryCount() && repeatUnitNoTags == getRepeatUnitNoTags()) {
            appendToLog("!!! Skip repeated repeated data with qValue=" + qValue + ", retryCount = " + retryCount + ", repeatUntilNoTags = " + repeatUnitNoTags);
            return true;
        }

        if (result) {
            result = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoStartQ(qValue, -1, -1, -1, -1, -1) : rfidReaderChipE710.rx000Setting.setAlgoStartQ(qValue));
        }
        if (result) result = setRetryCount(retryCount);
        if (result) {
            result = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoRunTilZero(repeatUnitNoTags ? 1 : 0) : rfidReaderChipE710.rx000Setting.setAlgoRunTilZero(repeatUnitNoTags ? 1 : 0));
        }
        return result;
    }

    public static class PreMatchData {
        public boolean enable; public int target;
        public int action; public int bank;
        public int offset; public String mask; public int maskblen; public int querySelect; public long pwrlevel; public boolean invAlgo; public int qValue;
        public PreMatchData(boolean enable, int target, int action, int bank, int offset, String mask, int maskblen, int querySelect, long pwrlevel, boolean invAlgo, int qValue) {
            this.enable = enable;
            this.target = target;
            this.action = action;
            this.bank = bank;
            this.offset = offset;
            this.mask = mask;
            this.maskblen = maskblen;
            this.querySelect = querySelect;
            this.pwrlevel = pwrlevel;
            this.invAlgo = invAlgo;
            this.qValue = qValue;
        }
    }
    public PreMatchData preMatchData;
    public boolean setOnlyPowerLevel(long pwrlevel) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaPower(pwrlevel) : rfidReaderChipE710.rx000Setting.setAntennaPower(pwrlevel));
    }

    boolean[] bSelectEnabled = { false, false, false };
    boolean setSelectCriteria3(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask, int maskblen) {
        boolean DEBUG = false;
        if (!enable) {
            if (bSelectEnabled[index] = false) return true;
        }
        if (DEBUG || true) appendToLog("setSelectCriteria 3 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask + ", maskbitlen = " + maskblen);
        int maskbytelen = maskblen / 4; if ((maskblen % 4) != 0) maskbytelen++; if (maskbytelen > 64) maskbytelen = 64;
        if (mask.length() > maskbytelen ) mask = mask.substring(0, maskbytelen);
        if (index == 0) preMatchData = new RfidReader.PreMatchData(enable, target, action, bank, offset, mask, maskblen,
                (bis108 ? rfidReaderChipR2000.rx000Setting.getQuerySelect() : rfidReaderChipE710.rx000Setting.getQuerySelect()), getPwrlevel(), getInvAlgo(), getQValue());
        boolean result = true;
        if (index != (bis108 ? rfidReaderChipR2000.rx000Setting.getInvSelectIndex() : rfidReaderChipE710.rx000Setting.getInvSelectIndex())) {
            result = (bis108 ? rfidReaderChipR2000.rx000Setting.setInvSelectIndex(index) : rfidReaderChipE710.rx000Setting.setInvSelectIndex(index));
            if (DEBUG) appendToLog("After setInvSelectIndex, result = " + result);
        }
        if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setSelectEnable(enable ? 1 : 0, target, action, delay) : rfidReaderChipE710.rx000Setting.setSelectEnable(enable ? 1 : 0, target, action, delay));
        if (DEBUG) appendToLog("After setSelectEnable, result = " + result);
        if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setSelectMaskBank(bank) : rfidReaderChipE710.rx000Setting.setSelectMaskBank(bank));
        if (DEBUG) appendToLog("After setSelectMaskBank, result = " + result);
        if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setSelectMaskOffset(offset) : rfidReaderChipE710.rx000Setting.setSelectMaskOffset(offset));
        if (DEBUG) appendToLog("After setSelectMaskOffset, result = " + result + " and mask = " + mask);
        if (mask == null)   return false;
        if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setSelectMaskLength(maskblen) : rfidReaderChipE710.rx000Setting.setSelectMaskLength(maskblen));
        if (DEBUG) appendToLog("After setSelectMaskLength, result = " + result);
        if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setSelectMaskData(mask) : rfidReaderChipE710.rx000Setting.setSelectMaskData(mask));
        if (DEBUG) appendToLog("After setSelectMaskData, result = " + result);
        if (result) {
            if (enable) {
                result = (bis108 ? rfidReaderChipR2000.rx000Setting.setTagSelect(1) : rfidReaderChipE710.rx000Setting.setTagSelect(1));
                if (DEBUG) appendToLog("After setTagSelect[1], result = " + result);
                if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setQuerySelect(3) : rfidReaderChipE710.rx000Setting.setQuerySelect(3));
                if (DEBUG) appendToLog("After setQuerySelect[3], result = " + result);
            } else {
                result = (bis108 ? rfidReaderChipR2000.rx000Setting.setTagSelect(0) : rfidReaderChipE710.rx000Setting.setTagSelect(0));
                if (DEBUG) appendToLog("After setTagSelect[0], result = " + result);
                if (result) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setQuerySelect(0) : rfidReaderChipE710.rx000Setting.setQuerySelect(0));
                if (DEBUG) appendToLog("After setQuerySelect[0], result = " + result);
            }
        }
        if (result) {
            bSelectEnabled[index] = enable;
        }
        return result;
    }
    public PostMatchData postMatchDataOld; public boolean postMatchDataChanged = false;
    public RfidReader.PreMatchData preMatchDataOld; public boolean preMatchDataChanged = false;
    public boolean setSelectedTag1(String selectMask, int selectBank, int selectOffset, int delay, long pwrlevel, int qValue, int matchRep) {
        appendToLog("setSelectCriteria selectMask = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", delay = " + delay + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        boolean setSuccess = true, DEBUG = true;
        if (selectMask == null)   selectMask = "";

        if (preMatchDataChanged == false) {
            preMatchDataChanged = true; if (DEBUG) appendToLog("setSelectCriteria preMatchDataChanged is SET with preMatchData = " + (preMatchData != null ? "valid" : "null"));
            if (preMatchData == null) {
                preMatchData = new RfidReader.PreMatchData(false,
                        (bis108 ? rfidReaderChipR2000.rx000Setting.getQueryTarget() : rfidReaderChipE710.rx000Setting.getQueryTarget()), 0, 0, 0, "", 0,
                        (bis108 ? rfidReaderChipR2000.rx000Setting.getQuerySelect() : rfidReaderChipE710.rx000Setting.getQuerySelect()), getPwrlevel(), getInvAlgo(), getQValue());
            }
            preMatchDataOld = preMatchData;
        }
        int index = 0;
        int indexCurrent = (bis108 ? rfidReaderChipR2000.rx000Setting.invSelectIndex : rfidReaderChipE710.rx000Setting.invSelectIndex);
        for (int i = 0; i < 7; i++) {
            if (bis108) rfidReaderChipR2000.rx000Setting.setInvSelectIndex(i); else rfidReaderChipE710.rx000Setting.setInvSelectIndex(i);
            if ((bis108 ? rfidReaderChipR2000.rx000Setting.getSelectEnable() : rfidReaderChipE710.rx000Setting.getSelectEnable()) == 0) {
                appendToLog("free select when i = " + i + ". Going to setSelectCriteria");
                setSuccess = setSelectCriteria3(i, true, 4, 0, delay, selectBank, selectOffset, selectMask, selectMask.length() * 4);
                if (DEBUG) appendToLog("setSelectCriteria after setSelectCriteria, setSuccess = " + setSuccess);
                break;
            }
        }
        if (bis108) rfidReaderChipR2000.rx000Setting.setInvSelectIndex(indexCurrent); else rfidReaderChipE710.rx000Setting.setInvSelectIndex(indexCurrent);

        if (setSuccess) setSuccess = setOnlyPowerLevel(pwrlevel);
        if (DEBUG) appendToLog("setSelectCriteria after setOnlyPowerLevel, setSuccess = " + setSuccess);
        if (false) {
            if (setSuccess) setSuccess = setFixedQParms(qValue, 5, false);
            if (DEBUG) appendToLog("setSelectCriteria after setFixedQParms, setSuccess = " + setSuccess);
            if (setSuccess) setSuccess = (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoAbFlip(1) : rfidReaderChipE710.rx000Setting.setAlgoAbFlip(1));
            if (DEBUG) appendToLog("setSelectCriteria after setAlgoAbFlip, setSuccess = " + setSuccess);
            if (setSuccess) setSuccess = setInvAlgo1(false);
            if (DEBUG) appendToLog("setSelectCriteria after setInvAlgo1, setSuccess = " + setSuccess);
        }

        if (setSuccess) setSuccess = (bis108 ? rfidReaderChipR2000.rx000Setting.setMatchRep(matchRep) : rfidReaderChipE710.rx000Setting.setMatchRep(matchRep));
        if (DEBUG) appendToLog("setSelectCriteria after setMatchRep, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = (bis108 ? rfidReaderChipR2000.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting) : rfidReaderChipE710.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting));
        if (DEBUG) appendToLog("setSelectCriteria after setTagDelay, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = (bis108 ? rfidReaderChipR2000.rx000Setting.setCycleDelay(cycleDelaySetting) : rfidReaderChipE710.rx000Setting.setCycleDelay(cycleDelaySetting));
        if (DEBUG) appendToLog("setSelectCriteria after setCycleDelay, setSuccess = " + setSuccess);
        if (setSuccess) setSuccess = (bis108 ? rfidReaderChipR2000.rx000Setting.setInvModeCompact(false) : rfidReaderChipE710.rx000Setting.setInvModeCompact(false));
        if (DEBUG) appendToLog("setSelectCriteria after setInvModeCompact, setSuccess = " + setSuccess);
        return setSuccess;
    }

    public final int modifyCodeAA = 0xAA;
    public enum RegionCodes {
        NULL,
        AG, BD, CL, CO, CR, DR, MX, PM, UG,
        BR1, BR2, BR3, BR4, BR5,
        IL, IL2019RW, PR, PH, SG, ZA, VZ,
        AU, NZ, HK, MY, VN, VN1, VN2, VN3,
        CN, TW, KR, KR2017RW, JP, JP6, TH, IN, FCC,
        UH1, UH2, LH, LH1, LH2,
        ETSI, ID, ETSIUPPERBAND,

        Albania1, Albania2, Algeria1, Algeria2, Algeria3,     Algeria4, Argentina, Armenia, Australia1, Australia2,
        Austria1, Austria2, Azerbaijan, Bahrain, Bangladesh,  Belarus, Belgium1, Belgium2, Bolivia, Bosnia,
        Botswana, Brazil1, Brazil2, Brunei1, Brunei2,         Bulgaria1, Bulgaria2, Cambodia, Cameroon, Canada,
        Chile1, Chile2, Chile3, China, Colombia,              Congo, CostaRica, Cotedlvoire, Croatia, Cuba,
        Cyprus1, Cyprus2, Czech1, Czech2, Denmark1,           Denmark2, Dominican, Ecuador, Egypt, ElSalvador,
        Estonia, Finland1, Finland2, France, Georgia,         Germany, Ghana, Greece, Guatemala, HongKong1,
        HongKong2, Hungary1, Hungary2, Iceland, India,        Indonesia, Iran, Ireland1, Ireland2, Israel,
        Italy, Jamaica, Japan4, Japan6, Jordan,               Kazakhstan, Kenya, Korea, KoreaDPR, Kuwait,
        Kyrgyz, Latvia, Lebanon, Libya, Liechtenstein1,       Liechtenstein2, Lithuania1, Lithuania2, Luxembourg1, Luxembourg2,
        Macao, Macedonia, Malaysia, Malta1, Malta2,           Mauritius, Mexico, Moldova1, Moldova2, Mongolia,
        Montenegro, Morocco, Netherlands, NewZealand1, NewZealand2,   Nicaragua, Nigeria, Norway1, Norway2, Oman,
        Pakistan, Panama, Paraguay, Peru, Philippines,        Poland, Portugal, Romania, Russia1, Russia3,
        Senegal, Serbia, Singapore1, Singapore2, Slovak1,     Slovak2, Slovenia1, Solvenia2, SAfrica1, SAfrica2,
        Spain, SriLanka, Sudan, Sweden1, Sweden2,             Switzerland1, Switzerland2, Syria, Taiwan1, Taiwan2,
        Tajikistan, Tanzania, Thailand, Trinidad, Tunisia,    Turkey, Turkmenistan, Uganda, Ukraine, UAE,
        UK1, UK2, USA, Uruguay, Venezuela,                    Vietnam1, Vietnam2, Yemen, Zimbabwe, Vietnam3
    }
    public String regionCode2StringArray(RegionCodes region) {
        switch (region) {
            case AG:
                return "Argentina";
            case CL:
                return "Chile";
            case CO:
                return "Columbia";
            case CR:
                return "Costa Rica";
            case DR:
                return "Dominican Republic";
            case MX:
                return "Mexico";
            case PM:
                return "Panama";
            case UG:
                return "Uruguay";
            case BR1:
                return "Brazil 915-927";
            case BR2:
                return "Brazil 902-906, 915-927";
            case BR3:
                return "Brazil 902-906";
            case BR4:
                return "Brazil 902-904";
            case BR5:
                return "Brazil 917-924";
            case IL:
            case IL2019RW:
                return "Israel";
            case PR:
                return "Peru";
            case PH:
                return "Philippines";
            case SG:
                return "Singapore";
            case ZA:
                return "South Africa";
            case VZ:
                return "Venezuela";
            case AU:
                return "Australia";
            case NZ:
                return "New Zealand";
            case HK:
                return "Hong Kong";
            case MY:
                return "Malaysia";
            case VN:
                return "Vietnam";
            case VN1:
                return "Vietnam1";
            case VN2:
                return "Vietnam2";
            case VN3:
                return "Vietnam3";
            case BD:
                return "Bangladesh";
            case CN:
                return "China";
            case TW:
                return "Taiwan";
            case KR:
            case KR2017RW:
                return "Korea";
            case JP:
                return "Japan";
            case JP6:
                return "Japan";
            case TH:
                return "Thailand";
            case ID:
                return "Indonesia";
            case FCC:
                if (getFreqModifyCode() == modifyCodeAA) return "FCC";
                return "USA/Canada";
            case UH1:
                return "UH1";
            case UH2:
                return "UH2";
            case LH:
                return "LH";
            case LH1:
                return "LH1";
            case LH2:
                return "LH2";
            case ETSI:
                return "Europe";
            case IN:
                return "India";
            case ETSIUPPERBAND:
                return "ETSI Upper Band";
            default:
                return region.toString();
        }
    }
    public RegionCodes regionCode = null;
    RegionCodes[] getRegionList1() {
        boolean DEBUG = false;
        RegionCodes[] regionList = null;
        regionCode = null;
        if (DEBUG) appendToLog("3 getCountryList: getCountryCode is " + getCountryCode());
        switch (getCountryCode()) {
            case 1:
                RegionCodes RegionCodes;
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Albania1, RfidReader.RegionCodes.Algeria1, RfidReader.RegionCodes.Algeria2, RfidReader.RegionCodes.Armenia, RfidReader.RegionCodes.Austria1,
                        RfidReader.RegionCodes.Azerbaijan, RfidReader.RegionCodes.Bahrain, RfidReader.RegionCodes.Bangladesh, RfidReader.RegionCodes.Belarus, RfidReader.RegionCodes.Belgium1,
                        RfidReader.RegionCodes.Bosnia, RfidReader.RegionCodes.Botswana, RfidReader.RegionCodes.Brunei1, RfidReader.RegionCodes.Bulgaria1, RfidReader.RegionCodes.Cameroon,
                        RfidReader.RegionCodes.Congo, RfidReader.RegionCodes.Cotedlvoire, RfidReader.RegionCodes.Croatia, RfidReader.RegionCodes.Cyprus1, RfidReader.RegionCodes.Czech1,
                        RfidReader.RegionCodes.Denmark1, RfidReader.RegionCodes.Egypt, RfidReader.RegionCodes.Estonia, RfidReader.RegionCodes.Finland1, RfidReader.RegionCodes.France,
                        RfidReader.RegionCodes.Georgia, RfidReader.RegionCodes.Germany, RfidReader.RegionCodes.Ghana, RfidReader.RegionCodes.Greece, RfidReader.RegionCodes.HongKong1,
                        RfidReader.RegionCodes.Hungary1, RfidReader.RegionCodes.Iceland, RfidReader.RegionCodes.India, RfidReader.RegionCodes.Iran, RfidReader.RegionCodes.Ireland1,
                        RfidReader.RegionCodes.Italy, RfidReader.RegionCodes.Jordan, RfidReader.RegionCodes.Kazakhstan, RfidReader.RegionCodes.Kenya, RfidReader.RegionCodes.Kuwait,
                        RfidReader.RegionCodes.Kyrgyz, RfidReader.RegionCodes.Latvia, RfidReader.RegionCodes.Lebanon, RfidReader.RegionCodes.Libya, RfidReader.RegionCodes.Liechtenstein1,
                        RfidReader.RegionCodes.Lithuania1, RfidReader.RegionCodes.Luxembourg1, RfidReader.RegionCodes.Macedonia, RfidReader.RegionCodes.Malta1, RfidReader.RegionCodes.Mauritius,
                        RfidReader.RegionCodes.Moldova1, RfidReader.RegionCodes.Montenegro, RfidReader.RegionCodes.Morocco, RfidReader.RegionCodes.Netherlands, RfidReader.RegionCodes.NewZealand1,
                        RfidReader.RegionCodes.Nigeria, RfidReader.RegionCodes.Norway1, RfidReader.RegionCodes.Oman, RfidReader.RegionCodes.Pakistan, RfidReader.RegionCodes.Poland,
                        RfidReader.RegionCodes.Portugal, RfidReader.RegionCodes.Romania, RfidReader.RegionCodes.Russia1, RfidReader.RegionCodes.SAfrica1, RfidReader.RegionCodes.Senegal,
                        RfidReader.RegionCodes.Serbia, RfidReader.RegionCodes.Singapore1, RfidReader.RegionCodes.Slovak1, RfidReader.RegionCodes.Slovenia1, RfidReader.RegionCodes.Spain,
                        RfidReader.RegionCodes.SriLanka, RfidReader.RegionCodes.Sudan, RfidReader.RegionCodes.Sweden1, RfidReader.RegionCodes.Switzerland1, RfidReader.RegionCodes.Syria,
                        RfidReader.RegionCodes.Tajikistan, RfidReader.RegionCodes.Tanzania, RfidReader.RegionCodes.Tunisia, RfidReader.RegionCodes.Turkey, RfidReader.RegionCodes.Turkmenistan,
                        RfidReader.RegionCodes.UAE, RfidReader.RegionCodes.Uganda, RfidReader.RegionCodes.UK1, RfidReader.RegionCodes.Ukraine, RfidReader.RegionCodes.Vietnam1,
                        RfidReader.RegionCodes.Yemen, RfidReader.RegionCodes.Zimbabwe, RfidReader.RegionCodes.Vietnam3
                };
                break;
            case 2:
                String strSpecialCountryVersion = getSpecialCountryVersion();
                if (DEBUG) appendToLog("3A getCountryList: getSpecialCountryVersion is [" + strSpecialCountryVersion + "]");
                if (strSpecialCountryVersion == null || (strSpecialCountryVersion != null && strSpecialCountryVersion.length() == 0)) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.Bolivia, RfidReader.RegionCodes.Canada, RfidReader.RegionCodes.Mexico, RfidReader.RegionCodes.USA
                    };
                } else if (strSpecialCountryVersion.contains("AS")) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.Australia1, RfidReader.RegionCodes.Australia2
                    };
                } else if (strSpecialCountryVersion.contains("NZ")) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.NewZealand2
                    };
                } else if (strSpecialCountryVersion.contains("OFCA")) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.HongKong2
                    };
                } else if (strSpecialCountryVersion.contains("SG")) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.Singapore2
                    };
                } else if (strSpecialCountryVersion.contains("RW")) {
                    regionList = new RegionCodes[]{
                            RfidReader.RegionCodes.Albania2, RfidReader.RegionCodes.Argentina, RfidReader.RegionCodes.Brazil1, RfidReader.RegionCodes.Brazil2, RfidReader.RegionCodes.Chile1,
                            RfidReader.RegionCodes.Chile2, RfidReader.RegionCodes.Chile3, RfidReader.RegionCodes.Colombia, RfidReader.RegionCodes.CostaRica, RfidReader.RegionCodes.Cuba,
                            RfidReader.RegionCodes.Dominican, RfidReader.RegionCodes.Ecuador, RfidReader.RegionCodes.ElSalvador, RfidReader.RegionCodes.Guatemala, RfidReader.RegionCodes.Jamaica,
                            RfidReader.RegionCodes.Nicaragua, RfidReader.RegionCodes.Panama, RfidReader.RegionCodes.Paraguay, RfidReader.RegionCodes.Peru, RfidReader.RegionCodes.Philippines,
                            RfidReader.RegionCodes.Singapore2, RfidReader.RegionCodes.Thailand, RfidReader.RegionCodes.Trinidad, RfidReader.RegionCodes.Uruguay, RfidReader.RegionCodes.Venezuela
                    };
                }
                break;
            case 4:
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Taiwan1, RfidReader.RegionCodes.Taiwan2
                };
                break;
            case 6:
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Korea
                };
                break;
            case 7:
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Algeria4, RfidReader.RegionCodes.Brunei2, RfidReader.RegionCodes.Cambodia, RfidReader.RegionCodes.China, RfidReader.RegionCodes.Indonesia,
                        RfidReader.RegionCodes.KoreaDPR, RfidReader.RegionCodes.Macao, RfidReader.RegionCodes.Malaysia, RfidReader.RegionCodes.Mongolia, RfidReader.RegionCodes.Vietnam2
                };
                break;
            case 8:
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Japan4, RfidReader.RegionCodes.Japan6
                };
                break;
            case 9:
                regionList = new RfidReader.RegionCodes[]{
                        RfidReader.RegionCodes.Algeria3, RfidReader.RegionCodes.Austria2, RfidReader.RegionCodes.Belgium2, RfidReader.RegionCodes.Bulgaria2, RfidReader.RegionCodes.Cyprus2,
                        RfidReader.RegionCodes.Czech2, RfidReader.RegionCodes.Denmark2, RfidReader.RegionCodes.Finland2, RfidReader.RegionCodes.Hungary2, RfidReader.RegionCodes.Ireland2,
                        RfidReader.RegionCodes.Israel, RfidReader.RegionCodes.Liechtenstein2, RfidReader.RegionCodes.Lithuania2, RfidReader.RegionCodes.Luxembourg2, RfidReader.RegionCodes.Malta2,
                        RfidReader.RegionCodes.Moldova2, RfidReader.RegionCodes.Norway2, RfidReader.RegionCodes.Russia3, RfidReader.RegionCodes.SAfrica2, RfidReader.RegionCodes.Slovak2,
                        RfidReader.RegionCodes.Solvenia2, RfidReader.RegionCodes.Sweden2, RfidReader.RegionCodes.Switzerland2, RfidReader.RegionCodes.UK2
                };
                break;
            default:
                int indexBegin = RfidReader.RegionCodes.Albania1.ordinal();
                int indexEnd = RfidReader.RegionCodes.Vietnam3.ordinal();
                regionList = new RegionCodes[indexEnd - indexBegin + 1];
                for (int i = 0; i < regionList.length; i++)
                    regionList[i] = RfidReader.RegionCodes.values()[indexBegin + i];
                break;
        }
        int iValue = rfidReaderChipE710.rx000Setting.getCountryEnum();
        if (DEBUG) appendToLog("3b getCountryList: getCountryEnum is " + iValue);
        if (iValue < 0) return null;

        iValue += RfidReader.RegionCodes.Albania1.ordinal() - 1;
        regionCode = RfidReader.RegionCodes.values()[iValue];
        if (DEBUG) appendToLog("3C getCountryList: regionCode is " + regionCode.toString());
        return regionList;
    }
    public final RegionCodes regionCodeDefault4Country2 = RegionCodes.FCC;
    public RegionCodes[] getRegionList() {
        if (bis108) {
        boolean DEBUG = false;
        RegionCodes[] regionList = null;
        {
            switch (getCountryCode()) {
                case 1:
                    if (regionCode == null) regionCode = RegionCodes.ETSI;
                    regionList = new RegionCodes[]{RegionCodes.ETSI, RegionCodes.IN, RegionCodes.VN1};
                    break;
                default:
                case 2:
                    int modifyCode = getFreqModifyCode();
                    if (modifyCode != modifyCodeAA) {
                        if (regionCode == null) regionCode = regionCodeDefault4Country2;
                        regionList = new RegionCodes[]{
                                RegionCodes.AG,
                                RegionCodes.AU,
                                RegionCodes.BD,
                                RegionCodes.BR1, RegionCodes.BR2, RegionCodes.BR3, RegionCodes.BR4, RegionCodes.BR5,
                                RegionCodes.CL, RegionCodes.CO, RegionCodes.CR, RegionCodes.DR,
                                RegionCodes.HK,
                                RegionCodes.ID,
                                RegionCodes.IL2019RW,
                                RegionCodes.KR2017RW,
                                RegionCodes.LH1, RegionCodes.LH2,
                                RegionCodes.MY,
                                RegionCodes.MX, RegionCodes.PM,
                                RegionCodes.PR,
                                RegionCodes.PH, RegionCodes.SG,
                                RegionCodes.ZA,
                                RegionCodes.TH,
                                RegionCodes.UH1, RegionCodes.UH2,
                                RegionCodes.UG,
                                RegionCodes.FCC,
                                RegionCodes.VZ,
                                RegionCodes.VN};
                    } else {
                        String strSpecialCountryVersion = rfidReaderChipR2000.rx000OemSetting.getSpecialCountryVersion();
                        if (strSpecialCountryVersion.contains("OFCA")) {
                            regionCode = RegionCodes.HK;
                            regionList = new RegionCodes[]{RegionCodes.HK};
                        } else if (strSpecialCountryVersion.contains("SG")) {
                            regionCode = RegionCodes.SG;
                            regionList = new RegionCodes[]{RegionCodes.SG};
                        } else if (strSpecialCountryVersion.contains("AS")) {
                            regionCode = RegionCodes.AU;
                            regionList = new RegionCodes[]{RegionCodes.AU};
                        } else if (strSpecialCountryVersion.contains("NZ")) {
                            regionCode = RegionCodes.NZ;
                            regionList = new RegionCodes[]{RegionCodes.NZ};
                        } else if (strSpecialCountryVersion.contains("ZA")) {
                            regionCode = RegionCodes.ZA;
                            regionList = new RegionCodes[]{RegionCodes.ZA};
                        } else if (strSpecialCountryVersion.contains("TH")) {
                            regionCode = RegionCodes.TH;
                            regionList = new RegionCodes[]{RegionCodes.TH};
                        } else {    //if (strSpecialCountryVersion.contains("*USA")) {
                            regionCode = regionCodeDefault4Country2;
                            regionList = new RegionCodes[]{RegionCodes.FCC};
                        }
                    }
                    break;
                case 3:
//                break;
                case 4:
                    if (regionCode == null) regionCode = RegionCodes.TW;
                    regionList = new RegionCodes[]{RegionCodes.TW, RegionCodes.AU, RegionCodes.MY,
                            RegionCodes.HK, RegionCodes.SG, RegionCodes.ID, RegionCodes.CN};
                    break;
                case 5:
                    regionCode = RegionCodes.KR;
                    regionList = new RegionCodes[]{RegionCodes.KR};
                    break;
                case 6:
                    regionCode = RegionCodes.KR2017RW;
                    regionList = new RegionCodes[]{RegionCodes.KR2017RW};
                    break;
                case 7:
                    if (regionCode == null) regionCode = RegionCodes.CN;
                    regionList = new RegionCodes[]{RegionCodes.CN, RegionCodes.AU, RegionCodes.HK, RegionCodes.TH,
                            RegionCodes.SG, RegionCodes.MY, RegionCodes.ID, RegionCodes.VN2, RegionCodes.VN3};
                    break;
                case 8:
                    String strSpecialCountryVersion = rfidReaderChipR2000.rx000OemSetting.getSpecialCountryVersion();
                    if (strSpecialCountryVersion.contains("6")) {
                        regionCode = RegionCodes.JP6;
                        regionList = new RegionCodes[]{RegionCodes.JP6};
                    } else {
                        regionCode = RegionCodes.JP;
                        regionList = new RegionCodes[]{RegionCodes.JP};
                    }
                    break;
                case 9:
                    regionCode = RegionCodes.ETSIUPPERBAND;
                    regionList = new RegionCodes[]{RegionCodes.ETSIUPPERBAND};
                    break;
            }
        }
        countryInList = 0; if (DEBUG) appendToLog("saveSetting2File testpoint 1");
        for (int i = 0; i < regionList.length; i++) {
            if (regionCode == regionList[i]) {
                countryInList = i; if (DEBUG) appendToLog("saveSetting2File testpoint 2"); break;
            }
        }
        if (countryInListDefault < 0) countryInListDefault = countryInList;
        appendToLog("countryInListDefault = " + countryInListDefault);
        return regionList;
        } else {
        boolean DEBUG = false;
        RegionCodes[] regionList;
        regionCode = null;
        if (DEBUG) appendToLog("2 getCountryList");
        regionList = getRegionList1();
        if (DEBUG) appendToLog("2A getCountryList: regionList is " + (regionList != null ? "Valid" : "null"));
        if (regionList != null) {
            if (DEBUG) appendToLog(String.format("2b getCountryList: countryInList = %d, regionCode = %s", countryInList, (regionCode != null ? regionCode.toString() : "")));
            if (countryInList < 0) {
                if (regionCode == null) regionCode = regionList[0];
                countryInList = 0;
                for (int i = 0; i < regionList.length; i++) {
                    if (regionCode == regionList[i]) {
                        countryInList = i;
                        break;
                    }
                }
                if (countryInListDefault < 0) countryInListDefault = countryInList;
                regionCode = regionList[countryInList];
                if (DEBUG) appendToLog(String.format("2C getCountryList: countryInList = %d, regionCode = %s", countryInList, regionCode.toString()));
            }
        } else regionCode = null;
        return regionList;
        }
    }
    public boolean getChannelHoppingDefault() {
        int countryCode = getCountryCode();
        appendToLog("getChannelHoppingDefault: countryCode (for channelOrderType) = " + countryCode);
        {
            if (countryCode == 1 || countryCode == 8 || countryCode == 9) return false;
            return true;
        }
    }
    public final int iCountryEnumInfoColumn = 7;
    public String[] strCountryEnumInfo = {
            "1", "Albania1", "-1", "4", "Fixed", "600", "865.7",
            "2", "Albania2", "-2 RW", "23", "Hop", "250", "915.25",
            "3", "Algeria1", "-1", "4", "Fixed", "600", "871.6",
            "4", "Algeria2", "-1", "4", "Fixed", "600", "881.6",
            "5", "Algeria3", "-9", "3", "Fixed", "1200", "916.3",
            "6", "Algeria4", "-7", "2", "Fixed", "500", "925.25",
            "7", "Argentina", "-2 RW", "50", "Hop", "500", "902.75",
            "8", "Armenia", "-1", "4", "Fixed", "600", "865.7",
            "9", "Australia1", "-2 AS", "10", "Hop", "500", "920.75",
            "10", "Australia2", "-2 AS", "14", "Hop", "500", "918.75",
            "11", "Austria1", "-1", "4", "Fixed", "600", "865.7",
            "12", "Austria2", "-9", "3", "Fixed", "1200", "916.3",
            "13", "Azerbaijan", "-1", "4", "Fixed", "600", "865.7",
            "14", "Bahrain", "-1", "4", "Fixed", "600", "865.7",
            "15", "Bangladesh", "-1", "4", "Fixed", "600", "865.7",
            "16", "Belarus", "-1", "4", "Fixed", "600", "865.7",
            "17", "Belgium1", "-1", "4", "Fixed", "600", "865.7",
            "18", "Belgium2", "-9", "3", "Fixed", "1200", "916.3",
            "19", "Bolivia", "-2", "50", "Hop", "500", "902.75",
            "20", "Bosnia", "-1", "4", "Fixed", "600", "865.7",
            "21", "Botswana", "-1", "4", "Fixed", "600", "865.7",
            "22", "Brazil1", "-2 RW", "9", "Fixed", "500", "902.75",
            "23", "Brazil2", "-2 RW", "24", "Fixed", "500", "915.75",
            "24", "Brunei1", "-1", "4", "Fixed", "600", "865.7",
            "25", "Brunei2", "-7", "7", "Fixed", "250", "923.25",
            "26", "Blgaria1", "-1", "4", "Fixed", "600", "865.7",
            "27", "Bulgaria2", "-9", "3", "Fixed", "1200", "916.3",
            "28", "Cambodia", "-7", "16", "Hop", "250", "920.625",
            "29", "Cameroon", "-1", "4", "Fixed", "600", "865.7",
            "30", "Canada", "-2", "50", "Hop", "500", "902.75",
            "31", "Chile1", "-2 RW", "3", "Fixed", "1200", "916.3",
            "32", "Chile2", "-2 RW", "24", "Hop", "500", "915.75",
            "33", "Chile3", "-2 RW", "4", "Hop", "500", "925.75",
            "34", "China", "-7", "16", "Hop", "250", "920.625",
            "35", "Colombia", "-2 RW", "50", "Hop", "500", "902.75",
            "36", "Congo", "-1", "4", "Fixed", "600", "865.7",
            "37", "CostaRica", "-2 RW", "50", "Hop", "500", "902.75",
            "38", "Cotedlvoire", "-1", "4", "Fixed", "600", "865.7",
            "39", "Croatia", "-1", "4", "Fixed", "600", "865.7",
            "40", "Cuba", "-2 RW", "50", "Hop", "500", "902.75",
            "41", "Cyprus1", "-1", "4", "Fixed", "600", "865.7",
            "42", "Cyprus2", "-9", "3", "Fixed", "1200", "916.3",
            "43", "Czech1", "-1", "4", "Fixed", "600", "865.7",
            "44", "Czech2", "-9", "3", "Fixed", "1200", "916.3",
            "45", "Denmark1", "-1", "4", "Fixed", "600", "865.7",
            "46", "Denmark2", "-9", "3", "Fixed",  "1200", "916.3",
            "47", "Dominican", "-2 RW", "50", "Hop", "500", "902.75",
            "48", "Ecuador", "-2 RW", "50", "Hop", "500", "902.75",
            "49", "Egypt", "-1", "4", "Fixed",  "600", "865.7",
            "50", "ElSalvador", "-2 RW", "50", "Hop", "500", "902.75",
            "51", "Estonia", "-1", "4", "Fixed", "600", "865.7",
            "52", "Finland1", "-1", "4", "Fixed",  "600", "865.7",
            "53", "Finland2", "-9", "3", "Fixed", "1200", "916.3",
            "54", "France", "-1", "4", "Fixed", "600", "865.7",
            "55", "Georgia", "-1", "4", "Fixed",  "600", "865.7",
            "56", "Germany", "-1", "4", "Fixed", "600", "865.7",
            "57", "Ghana", "-1", "4", "Fixed", "600", "865.7",
            "58", "Greece", "-1", "4", "Fixed",  "600", "865.7",
            "59", " Guatemala", "-2 RW", "50", "Hop", "500", "902.75",
            "60", "HongKong1", "-1", "4", "Fixed", "600", "865.7",
            "61", "HongKong2", "-2 OFCA", "50", "Hop", "50", "921.25",
            "62", "Hungary1", "-1", "4", "Fixed", "600", "865.7",
            "63", "Hungary2", "-9", "3", "Fixed", "1200", "916.3",
            "64", "Iceland", "-1", "4", "Fixed", "600", "865.7",
            "65", "India", "-1", "3", "Fixed", "600", "865.7",
            "66", "Indonesia", "-7", "4", "Hop", "500", "923.75",
            "67", "Iran", "-1", "4", "Fixed", "600", "865.7",
            "68", "Ireland1", "-1", "4", "Fixed", "600", "865.7",
            "69", "Ireland2", "-9", "3", "Fixed", "1200", "916.3",
            "70", "Israel", "-9", "3", "Fixed", "500", "915.5",
            "71", "Italy", "-1", "4", "Fixed", "600", "865.7",
            "72", "Jamaica", "-2 RW", "50", "Hop", "500", "902.75",
            "73", "Japan4", "-8", "4", "Fixed", "1200", "916.8",
            "74", "Japan6", "-8", "6", "Fixed", "1200", "916.8",
            "75", "Jordan", "-1", "4", "Fixed", "600", "865.7",
            "76", "Kazakhstan", "-1", "4", "Fixed", "600", "865.7",
            "77", "Kenya", "-1", "4", "Fixed", "600", "865.7",
            "78", "Korea", "-6", "6", "Hop", "600", "917.3",
            "79", "KoreaDPR", "-7", "16", "Hop", "250", "920.625",
            "80", "Kuwait", "-1", "4", "Fixed", "600", "865.7",
            "81", "Kyrgyz", "-1", "4", "Fixed", "600", "865.7",
            "82", "Latvia", "-1", "4", "Fixed", "600", "865.7",
            "83", "Lebanon", "-1", "4", "Fixed", "600", "865.7",
            "84", "Libya", "-1", "4", "Fixed", "600", "865.7",
            "85", "Liechtenstein1", "-1", "4", "Fixed", "600", "865.7",
            "86", "Liechtenstein2", "-9", "3", "Fixed", "1200", "916.3",
            "87", "Lithuania1", "-1", "4", "Fixed", "600", "865.7",
            "88", "Lithuania2", "-9", "3", "Fixed", "1200", "916.3",
            "89", "Luxembourg1", "-1", "4", "Fixed", "600", "865.7",
            "90", "Luxembourg2", "-9", "3", "Fixed", "1200", "916.3",
            "91", "Macao", "-7", "16", "Hop", "250", "920.625",
            "92", "Macedonia", "-1", "4", "Fixed", "600", "865.7",
            "93", "Malaysia", "-7", "6", "Hop", "500", "919.75",
            "94", "Malta1", "-1", "4", "Fixed", "600", "865.7",
            "95", "Malta2", "-9", "3", "Fixed", "1200", "916.3",
            "96", "Mauritius", "-1", "4", "Fixed", "600", "865.7",
            "97", "Mexico", "-2", "50", "Hop", "500", "902.75",
            "98", "Moldova1", "-1", "4", "Fixed", "600", "865.7",
            "99", "Moldova2", "-9", "3", "Fixed", "1200", "916.3",
            "100", "Mongolia", "-7", "16", "Hop", "250", "920.625",
            "101", "Montenegro", "-1", "4", "Fixed", "600", "865.7",
            "102", "Morocco", "-1", "4", "Fixed", "600", "865.7",
            "103", "Netherlands", "-1", "4", "Fixed", "600", "865.7",
            "104", "NewZealand1", "-1", "4", "Hop", "500", "864.75",
            "105", "NewZealand2", "-2 NZ", "14", "Hop", "500", "920.75",
            "106", "Nicaragua", "-2 RW", "50", "Hop", "500", "902.75",
            "107", "Nigeria", "-1", "4", "Fixed", "600", "865.7",
            "108", "Norway1", "-1", "4", "Fixed", "600", "865.7",
            "109", "Norway2", "-9", "3", "Fixed", "1200", "916.3",
            "110", "Oman", "-1", "4", "Fixed", "600", "865.7",
            "111", "Pakistan", "-1", "4", "Fixed", "600", "865.7",
            "112", "Panama", "-2 RW", "50", "Hop", "500", "902.75",
            "113", "Paraguay", "-2 RW", "50", "Hop", "500", "902.75",
            "114", "Peru", "-2 RW", "24", "Hop", "500", "915.75",
            "115", "Philippines", "-2 RW", "50", "Hop", "250", "918.125",
            "116", "Poland", "-1", "4", "Fixed", "600", "865.7",
            "117", "Portugal", "-1", "4", "Fixed", "600", "865.7",
            "118", "Romania", "-1", "4", "Fixed", "600", "865.7",
            "119", "Russia1", "-1", "4", "Fixed", "600", "866.3",
            "120", "Russia3", "-9", "4", "Fixed", "1200", "915.6",
            "121", "Senegal", "-1", "4", "Fixed", "600", "865.7",
            "122", "Serbia", "-1", "4", "Fixed", "600", "865.7",
            "123", "Singapore1", "-1", "4", "Fixed", "600", "865.7",
            "124", "Singapore2", "-2 RW", "8", "Hop", "500", "920.75",
            "125", "Slovak1", "-1", "4", "Fixed", "600", "865.7",
            "126", "Slovak2", "-9", "3", "Fixed", "1200", "916.3",
            "127", "Slovenia1", "-1", "4", "Fixed", "600", "865.7",
            "128", "Solvenia2", "-9", "3", "Fixed", "1200", "916.3",
            "129", "SAfrica1", "-1", "4", "Fixed", "600", "865.7",
            "130", "SAfrica2", "-9", "7", "Fixed", "500", "915.7",
            "131", "Spain", "-1", "4", "Fixed", "600", "865.7",
            "132", "SriLanka", "-1", "4", "Fixed", "600", "865.7",
            "133", "Sudan", "-1", "4", "Fixed", "600", "865.7",
            "134", "Sweden1", "-1", "4", "Fixed", "600", "865.7",
            "135", "Sweden2", "-9", "3", "Fixed", "1200", "916.3",
            "136", "Switzerland1", "-1", "4", "Fixed", "600", "865.7",
            "137", "Switzerland2", "-9", "3", "Fixed", "1200", "916.3",
            "138", "Syria", "-1", "4", "Fixed", "600", "865.7",
            "139", "Taiwan1", "-4", "12", "Hop", "375", "922.875",
            "140", "Taiwan2", "-4", "12", "Hop", "375", "922.875",
            "141", "Tajikistan", "-1", "4", "Fixed", "600", "865.7",
            "142", "Tanzania", "-1", "4", "Fixed", "600", "865.7",
            "143", "Thailand", "-2 RW", "8", "Hop", "500", "920.75",
            "144", "Trinidad", "-2 RW", "50", "Hop", "500", "902.75",
            "145", "Tunisia", "-1", "4", "Fixed", "600", "865.7",
            "146", "Turkey", "-1", "4", "Fixed", "600", "865.7",
            "147", "Turkmenistan", "-1", "4", "Fixed", "600", "865.7",
            "148", "Uganda", "-1", "4", "Fixed", "600", "865.7",
            "149", "Ukraine", "-1", "4", "Fixed", "600", "865.7",
            "150", "UAE", "-1", "4", "Fixed", "600", "865.7",
            "151", "UK1", "-1", "4", "Fixed", "600", "865.7",
            "152", "UK2", "-9", "3", "Fixed", "1200", "916.3",
            "153", "USA", "-2", "50", "Hop", "500", "902.75",
            "154", "Uruguay", "-2 RW", "50", "Hop", "500", "902.75",
            "155", "Venezuela", "-2 RW", "50", "Hop", "500", "902.75",
            "156", "Vietnam1", "-1", "4", "Fixed", "600", "866.3",
            "157", "Vietnam2", "-7", "16", "Hop", "500", "918.75",
            "158", "Yemen", "-1", "4", "Fixed", "600", "865.7",
            "159", "Zimbabwe", "-1", "4", "Fixed", "600", "865.7",
            "160", "Vietnam3", "-7", "4", "Hop", "500", "920.75"
    };
    public int getCountryCode() {
        if (bis108) return rfidReaderChipR2000.rx000OemSetting.getCountryCode();
        final boolean DEBUG = false;
        int iCountrycode = -1;
        int iValue = rfidReaderChipE710.rx000Setting.getCountryEnum();
        if (DEBUG) appendToLog("getCountryEnum 0x3014 = " + iValue);
        if (iValue > 0 && iValue < strCountryEnumInfo.length/iCountryEnumInfoColumn) {
            if (DEBUG) {
                for (int i = 1; i <= 160; i++) {
                    appendToLog("i = " + i + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 0]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 1]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 2]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 3]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 4]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 5]
                            + ", " + strCountryEnumInfo[(i - 1) * iCountryEnumInfoColumn + 6]
                    );
                }
            }
            String strCountryCode = strCountryEnumInfo[(iValue - 1) * iCountryEnumInfoColumn + 2];
            if (DEBUG) appendToLog("strCountryCode 0 = " + strCountryCode);
            String[] countryCodePart = strCountryCode.split(" ");
            strCountryCode = countryCodePart[0].substring(1);
            if (DEBUG) appendToLog("strCountryCode 1 = " + strCountryCode);
            try {
                iCountrycode = Integer.decode(strCountryCode);
                if (DEBUG) appendToLog("iCountrycode = " + iCountrycode);
            } catch (Exception ex) {
            }
        }
        if (true) {
            int iCountrycode1 = rfidReaderChipE710.rx000Setting.getCountryEnumOem();
            if (DEBUG) appendToLog("getCountryEnumOem 0x5040 = " + iCountrycode1);
            int iCountrycode2 = rfidReaderChipE710.rx000Setting.getCountryCodeOem();
            if (DEBUG) appendToLog("getCountryCodeOem 0xef98 = " + iCountrycode2);
            if (iCountrycode < 0 && iCountrycode1 > 0 && iCountrycode1 < 10) iCountrycode = iCountrycode1;
            if (iCountrycode < 0 && iCountrycode2 > 0 && iCountrycode2 < 10) iCountrycode = iCountrycode2;
        }
        return iCountrycode;
    }
    public int getFreqModifyCode() {
        if (bis108) return rfidReaderChipR2000.rx000OemSetting.getFreqModifyCode();
        boolean DEBUG = false;
        int iFreqModifyCode = rfidReaderChipE710.rx000Setting.getFreqModifyCode();
        if (DEBUG) appendToLog("getFreqModifyCode 0xefb0 = " + iFreqModifyCode);
        return iFreqModifyCode;
    }
    public boolean getRfidOnStatus() {
        return rfidConnector.getOnStatus();
    }
    public boolean isRfidFailure() {
        return rfidConnector.rfidFailure;
    }
    public void setReaderDefault() {
        setPowerLevel(300);
        setTagGroup(0, 0, 2);
        setPopulation(60);
        setInvAlgoNoSave(true);
        setBasicCurrentLinkProfile();

        setCountryInList(countryInListDefault);
        setChannel(0);

        //getAntennaPower(0)
        //getPopulation()
        //getQuerySession()
        //getQueryTarget()
        setTagFocus(false);
        setFastId(false);
        //getInvAlgo()
        //\\getRetryCount()
        //getCurrentProfile() + "\n"));
        //\\getRxGain() + "\n"));
    }
    public String getMacVer() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getMacVer() : rfidReaderChipE710.rx000Setting.getMacVer());
    }
    public String getRadioSerial() {
        boolean DEBUG = true;
        String strValue, strValue1;
        strValue = getSerialNumber();
        if (bis108 == false) {
            if (DEBUG) appendToLog("getSerialNumber 0xEF9C = " + strValue);
            strValue1 = rfidReaderChipE710.rx000Setting.getProductSerialNumber();
            if (DEBUG) appendToLog("getProductSerialNumber 0x5020 = " + strValue1);
            if (strValue1 != null && false) strValue = strValue1;
        }
        if (strValue != null) {
            appendToLog("strValue length = " + strValue.length());
            if (strValue.length() >= 13) strValue = strValue.substring(0, 13);
        } else appendToLog("BBB");
        if (DEBUG) appendToLog("strValue = " + strValue);
        return strValue;
    }
    public String getRadioBoardVersion() {
        String str = getSerialNumber();
        if (str == null) return null;
        if (str.length() != 16) return null;
        if (bis108) {
            String strOut;
            if (str.substring(13, 14).matches("0")) strOut = str.substring(14, 15);
            else strOut = str.substring(13, 15);
            strOut += "." + str.substring(15);
            str = strOut;
        } else {
            str = str.substring(13);
            String string = "";
            if (str.length() >= 2) string = str.substring(0, 2);
            if (str.length() >= 3) string += ("." + str.substring(2, 3));
            str = string;
        }
        return str;
    }
    public int getAntennaSelect() {
        int iValue = 0;
        iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getAntennaSelect() : rfidReaderChipE710.rx000Setting.getAntennaPort());
        appendToLog("AntennaSelect = " + iValue);
        return iValue;
    }
    public boolean setAntennaSelect(int number) {
        boolean bValue = false;
        bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaSelect(number) : rfidReaderChipE710.rx000Setting.setAntennaSelect(number));
        appendToLog("AntennaSelect = " + number + " returning " + bValue);
        return bValue;
    }
    public boolean getAntennaEnable() {
        int iValue; boolean DEBUG = false;
        if (DEBUG) appendToLog("1 getAntennaEnable");
        iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getAntennaEnable() : rfidReaderChipE710.rx000Setting.getAntennaEnable());
        if (DEBUG) appendToLog("1A getAntennaEnable: AntennaEnable = " + iValue);
        if (iValue > 0) return true;
        else return false;
    }
    public boolean setAntennaEnable(boolean enable) {
        int iEnable = 0;
        if (enable) iEnable = 1;
        boolean bValue = false;
        appendToLog("1A setAntennaEnable: iEnable = " + iEnable);
        bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaEnable(iEnable) : rfidReaderChipE710.rx000Setting.setAntennaEnable(iEnable));
        appendToLog("AntennaEnable = " + iEnable + " returning " + bValue);
        if (bValue && bis108 == false) bValue = rfidReaderChipE710.rx000Setting.updateCurrentPort();
        return bValue;
    }
    public long getAntennaDwell() {
        long lValue = 0; boolean DEBUG = false;
        if (DEBUG) appendToLog("1 getAntennaDwell");
        lValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getAntennaDwell() : rfidReaderChipE710.rx000Setting.getAntennaDwell());
        if (DEBUG) appendToLog("1A getAntennaDwell: lValue = " + lValue);
        return lValue;
    }
    public boolean setAntennaDwell(long antennaDwell) {
        boolean bValue = false, DEBUG = false;
        if (DEBUG) appendToLog("1 AntennaDwell = " + antennaDwell + " returning " + bValue);
        bValue =  (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaDwell(antennaDwell) : rfidReaderChipE710.rx000Setting.setAntennaDwell(antennaDwell));
        if (DEBUG) appendToLog("1A AntennaDwell = " + antennaDwell + " returning " + bValue);
        return bValue;
    }
    public long getPwrlevel() {
        long lValue = 0;
        lValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getAntennaPower(-1) : rfidReaderChipE710.rx000Setting.getAntennaPower(-1));
        return lValue;
    }
    public long pwrlevelSetting;
    public boolean setPowerLevel(long pwrlevel) {
        pwrlevelSetting = pwrlevel;
        boolean bValue = false;
        bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaPower(pwrlevel) : rfidReaderChipE710.rx000Setting.setAntennaPower(pwrlevel));
        if (false) appendToLog("PowerLevel = " + pwrlevel + " returning " + bValue);
        return bValue;
    }
    public int getQueryTarget() {
        int iValue; boolean DEBUG = false;
        if (DEBUG) appendToLog("1 getQueryTarget");
        iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoAbFlip() : rfidReaderChipE710.rx000Setting.getQueryTarget());
        if (bis108) {
            if (iValue > 0) return 2;
            else {
                iValue = rfidReaderChipR2000.rx000Setting.getQueryTarget();
                if (iValue > 0) return 1;
                return 0;
            }
        } else {
            if (DEBUG) appendToLog("1A getQueryTarget: iValue = " + iValue);
            if (iValue < 0) iValue = 0;
            return iValue;
        }
    }
    public int getQuerySession() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getQuerySession() : rfidReaderChipE710.rx000Setting.getQuerySession());
    }
    public int getQuerySelect() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getQuerySelect() : rfidReaderChipE710.rx000Setting.getQuerySelect());
    }
    public boolean setTagGroup(int sL, int session, int target1) {
        if (bis108) {
            if (false) appendToLog("Hello6: invAlgo = " + rfidReaderChipR2000.rx000Setting.getInvAlgo());
            if (false) appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = " + rfidReaderChipR2000.rx000Setting.getInvAlgo());
            rfidReaderChipR2000.rx000Setting.setAlgoSelect(rfidReaderChipR2000.rx000Setting.getInvAlgo()); //Must not delete this line
            return rfidReaderChipR2000.rx000Setting.setQueryTarget(target1, session, sL);
        } else {
            //appendToLog("1d");
            int iAlgoAbFlip = rfidReaderChipE710.rx000Setting.getAlgoAbFlip();
            appendToLog("sL = " + sL + ", session = " + session + ", target = " + target1 + ", getAlgoAbFlip = " + iAlgoAbFlip);
            boolean bValue = false;
            bValue = rfidReaderChipE710.rx000Setting.setQueryTarget(target1, session, sL);
            if (bValue) {
                if (iAlgoAbFlip != 0 && target1 < 2)
                    bValue = rfidReaderChipE710.rx000Setting.setAlgoAbFlip(0);
                else if (iAlgoAbFlip == 0 && target1 >= 2)
                    bValue = rfidReaderChipE710.rx000Setting.setAlgoAbFlip(1);
            }
            return bValue;
        }
    }
    public int tagFocus = -1;
    public int getTagFocus() {
        if (bis108) {
            tagFocus = rfidReaderChipR2000.rx000Setting.getImpinjExtension();
            if (tagFocus > 0) tagFocus = ((tagFocus & 0x10) >> 4);
        } else {
            tagFocus = rfidReaderChipE710.rx000Setting.getImpinjExtension() & 0x04;
        }
        return tagFocus;
    }
    public boolean setTagFocus(boolean tagFocusNew) {
        boolean bRetValue;
        if (bis108) {
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
        if (bis108) {
            bRetValue = rfidReaderChipR2000.rx000Setting.setImpinjExtension((tagFocus > 0 ? true : false), fastIdNew);
        } else {
            bRetValue = rfidReaderChipE710.rx000Setting.setImpinjExtension((tagFocus > 0 ? true : false), fastIdNew);
        }
        if (bRetValue) fastId = (fastIdNew ? 1 : 0);
        return bRetValue;
    }
    public boolean invAlgoSetting = true;
    public boolean getInvAlgo() {
        return invAlgoSetting;
    }
    public boolean setInvAlgo(boolean dynamicAlgo) {
        boolean DEBUG = false;
        if (DEBUG) appendToLog("1 setInvAlgo with dynamicAlgo = " + dynamicAlgo);
        boolean bValue = setInvAlgo1(dynamicAlgo);
        if (bValue) invAlgoSetting = dynamicAlgo;
        if (DEBUG) appendToLog("1A setInvAlgo with bValue = " + bValue);
        return bValue;
    }
    public List<String> getProfileList() {
        if (bis108) return Arrays.asList(context.getResources().getStringArray(R.array.profile1_options));
        if (bluetoothGatt.isVersionGreaterEqual(rfidReaderChipE710.rx000Setting.getMacVer(), 1, 0, 250))
            return Arrays.asList(context.getResources().getStringArray(R.array.profile3A_options));
        else return Arrays.asList(context.getResources().getStringArray(R.array.profile2_options)); //for 1.0.12
    }
    public int getCurrentProfile() {
        if (bis108) return rfidReaderChipR2000.rx000Setting.getCurrentProfile();
        boolean DEBUG = false;
        if (DEBUG) appendToLog("1 getCurrentProfile");
        int iValue;
        if (true) {
            iValue = rfidReaderChipE710.rx000Setting.getCurrentProfile();
            if (DEBUG) appendToLog("1A getCurrentProfile: getCurrentProfile = " + iValue);
            if (iValue > 0) {
                List<String> profileList = getProfileList();
                if (DEBUG) appendToLog("1b getCurrentProfile: getProfileList = " + (profileList != null ? "valid" : ""));
                int index = 0;
                for (; index < profileList.size(); index++) {
                    if (Integer.valueOf(profileList.get(index).substring(0, profileList.get(index).indexOf(":"))) == iValue)
                        break;
                }
                if (index >= profileList.size()) {
                    index = profileList.size()-1;
                    setCurrentLinkProfile(index);
                }
                if (DEBUG) appendToLog("1C getCurrentProfile: index in the profileList = " + index);
                iValue = index;
            }
        }
        return iValue;
    }
    public boolean setBasicCurrentLinkProfile() {
        if (bis108) return setCurrentLinkProfile(1);
        int profile = 244;
        if (getCountryCode() == 1) profile = 241;
        appendToLog("profile is " + profile);
        return rfidReaderChipE710.rx000Setting.setCurrentProfile(profile);
    }
    public boolean setCurrentLinkProfile(int profile) {
        if (bis108) {
            if (profile == getCurrentProfile()) return true;
            boolean result;
            result = rfidReaderChipR2000.rx000Setting.setCurrentProfile(profile);
            if (result) {
                rfidReaderChipR2000.setPwrManagementMode(false);
                result = rfidReaderChipR2000.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_UPDATELINKPROFILE);
            }
            if (result && profile == 3) {
                appendToLog("It is profile3");
                if (getTagDelay() < 2) result = setTagDelay((byte) 2);
            }
            return result;
        }
        boolean DEBUG = true;
        if (DEBUG) appendToLog("1 setCurrentLinkProfile: input profile = " + profile);
        if (true && profile < 50) {
            List<String> profileList = getProfileList();
            if (profile < 0 || profile >= profileList.size()) return false;
            int profile1 = Integer.valueOf(profileList.get(profile).substring(0, profileList.get(profile).indexOf(":")));
            profile = profile1;
        }
        if (DEBUG) appendToLog("1A setCurrentLinkProfile: adjusted profile = " + profile);
        boolean result = rfidReaderChipE710.rx000Setting.setCurrentProfile(profile);
        if (DEBUG) appendToLog("1b setCurrentLinkProfile: after setCurrentProfile, result = " + result);
        if (result) {
            setPwrManagementMode(false);
        }
        if (DEBUG) appendToLog("1C setCurrentLinkProfile: after setPwrManagementMode, result = " + result + ", profile = " + profile);
        if (result && profile == 3) {
            if (getTagDelay() < 2) result = setTagDelay((byte)2);
        }
        if (DEBUG) appendToLog("1d setCurrentLinkProfile: after setTagDelay, result = " + result);
        getCurrentProfile();
        return result;
    }
    public void resetEnvironmentalRSSI() {
        if (bis108) rfidReaderChipR2000.rx000EngSetting.resetRSSI();
        else rfidReaderChipE710.rx000EngSetting.resetRSSI();
    }
    public String getEnvironmentalRSSI() {
        int iValue;
        if (bis108) {
            rfidReaderChipR2000.setPwrManagementMode(false);
            iValue =  rfidReaderChipR2000.rx000EngSetting.getwideRSSI();
        }
        else {
            rfidReaderChipE710.setPwrManagementMode(false);
            iValue =  rfidReaderChipE710.getwideRSSI();
        }

        if (iValue < 0) return null;
        if (iValue > 255) return "Invalid data";
        double dValue = (bis108 ? rfidReaderChipR2000.decodeNarrowBandRSSI((byte)iValue) : rfidReaderChipE710.decodeNarrowBandRSSI((byte)iValue));
        return String.format("%.2f dB", dValue);
    }
    public int getHighCompression() {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.getHighCompression() : rfidReaderChipE710.getHighCompression());
    }
    public int getRflnaGain() {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.getRflnaGain() : rfidReaderChipE710.getRflnaGain());
    }
    public int getIflnaGain() {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.getIflnaGain() : rfidReaderChipE710.getIflnaGain());
    }
    public int getAgcGain() {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.getAgcGain() : rfidReaderChipE710.getAgcGain());
    }
    public int getRxGain() {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.getRxGain() : rfidReaderChipE710.getRxGain());
    }
    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.setRxGain(highCompression, rflnagain, iflnagain, agcgain) : rfidReaderChipE710.setRxGain(highCompression, rflnagain, iflnagain, agcgain));
    }
    public boolean setRxGain(int rxGain) {
        return (bis108 ? rfidReaderChipR2000.rx000MbpSetting.setRxGain(rxGain) : rfidReaderChipE710.setRxGain(rxGain));
    }
    public int FreqChnCnt() {
        return FreqChnCnt(regionCode);
    }
    public int FreqChnCnt(RegionCodes regionCode) {
        if (bis108) {
            switch (regionCode) {
                case FCC:
                case AG:
                case CL:
                case CO:
                case CR:
                case DR:
                case MX:
                case PM:
                case UG:
                    return FCC_CHN_CNT;
                case PR:
                    return PRTableOfFreq.length;
                case VZ:
                    return VZ_CHN_CNT;
                case AU:
                    return AUS_CHN_CNT;
                case BR1:
                    return BR1_CHN_CNT;
                case BR2:
                    return BR2_CHN_CNT;
                case BR3:
                    return BR3_CHN_CNT;
                case BR4:
                    return BR4_CHN_CNT;
                case BR5:
                    return BR5_CHN_CNT;
                case HK:
                case SG:
                case TH:
                case VN:
                    return HK_CHN_CNT;
                case VN1:
                    return VN1_CHN_CNT;
                case VN2:
                    return VN2_CHN_CNT;
                case VN3:
                    return VN3_CHN_CNT;
                case BD:
                    return BD_CHN_CNT;
                case TW:
                    return TW_CHN_CNT;
                case MY:
                    return MYS_CHN_CNT;
                case ZA:
                    return ZA_CHN_CNT;
                case ID:
                    return ID_CHN_CNT;
                case IL:
                    return IL_CHN_CNT;
                case IL2019RW:
                    return IL2019RW_CHN_CNT;
                case PH:
                    return PH_CHN_CNT;
                case NZ:
                    return NZ_CHN_CNT;
                case CN:
                    return CN_CHN_CNT;

                case UH1:
                    return UH1_CHN_CNT;
                case UH2:
                    return UH2_CHN_CNT;
                case LH:
                    return LH_CHN_CNT;
                case LH1:
                    return LH1_CHN_CNT;
                case LH2:
                    return LH2_CHN_CNT;

                case ETSI:
                    return ETSI_CHN_CNT;
                case IN:
                    return IDA_CHN_CNT;
                case KR:
                    return KR_CHN_CNT;
                case KR2017RW:
                    return KR2017RW_CHN_CNT;
                case JP:
                    return JPN2012_CHN_CNT;
                case JP6:
                    return JPN2012A_CHN_CNT;
                case ETSIUPPERBAND:
                    return ETSIUPPERBAND_CHN_CNT;

                default:
                    return 0;
            }
        } else {
            boolean DEBUG = true;
            int iFreqChnCnt = -1, iValue = -1; //mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000Setting.getCountryEnum(); //iValue--;
            iValue = regionCode.ordinal() - RegionCodes.Albania1.ordinal() + 1;
            if (DEBUG) appendToLog("regionCode = " + regionCode.toString() + ", regionCodeEnum = " + iValue);
            if (iValue > 0) {
                String strFreqChnCnt = strCountryEnumInfo[(iValue - 1) * iCountryEnumInfoColumn + 3];
                if (DEBUG) appendToLog("strFreqChnCnt = " + strFreqChnCnt);
                try {
                    iFreqChnCnt = Integer.parseInt(strFreqChnCnt);
                } catch (Exception ex) {
                    appendToLog("!!! CANNOT parse strFreqChnCnt = " + strFreqChnCnt);
                }
            }
            if (DEBUG) appendToLog("iFreqChnCnt = " + iFreqChnCnt);
            return iFreqChnCnt; //1 for hopping, 0 for fixed
        }
    }
    public double getLogicalChannel2PhysicalFreq(int channel) {
        if (bis108) {
            getCountryList();             //  used to set up possibly regionCode
            int TotalCnt = FreqChnCnt(regionCode);
            int[] freqIndex = FreqIndex(regionCode);
            double[] freqTable = GetAvailableFrequencyTable(regionCode);
            if (freqIndex.length != TotalCnt || freqTable.length != TotalCnt || channel >= TotalCnt)
                return -1;
            return freqTable[freqIndex[channel]];
        }
        boolean DEBUG = false;
        if (DEBUG) appendToLog("regionCode = " + regionCode.toString());
        int TotalCnt = FreqChnCnt(regionCode);
        if (DEBUG) appendToLog("TotalCnt = " + TotalCnt);
        int[] freqIndex = FreqIndex(regionCode);
        if (DEBUG) appendToLog("Frequency index " + (freqIndex != null ? ("length = " + freqIndex.length) : "null"));
        double[] freqTable = GetAvailableFrequencyTable(regionCode);
        if (DEBUG) appendToLog("Frequency freqTable " + (freqTable != null ? ("length = " + freqTable.length) : "null"));
        if (DEBUG) appendToLog("Check TotalCnt = " + TotalCnt + ", freqIndex.length = " + freqIndex.length + ", freqTable.length = " + freqTable.length + ", channel = " + channel);
        if (freqIndex.length != TotalCnt || freqTable.length != TotalCnt || channel >= TotalCnt)   return -1;
        double dRetvalue = freqTable[freqIndex[channel]];
        if (DEBUG) appendToLog("channel = " + channel + ", dRetvalue = " + dRetvalue);
        return dRetvalue;
    }
    public byte getTagDelay() {
        return tagDelaySetting;
    }
    public boolean setTagDelay(byte tagDelay) {
        tagDelaySetting = tagDelay;
        return true;
    }
    public byte getIntraPkDelay() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getIntraPacketDelay() : rfidReaderChipE710.rx000Setting.getIntraPacketDelay());
    }
    public boolean setIntraPkDelay(byte intraPkDelay) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setIntraPacketDelay(intraPkDelay) : rfidReaderChipE710.rx000Setting.setIntraPacketDelay(intraPkDelay));
    }
    public byte getDupDelay() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getDupElimRollWindow() : rfidReaderChipE710.rx000Setting.getDupElimRollWindow());
    }
    public boolean setDupDelay(byte dupElim) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setDupElimRollWindow(dupElim) : rfidReaderChipE710.rx000Setting.setDupElimRollWindow(dupElim));
    }
    public long getCycleDelay() {
        cycleDelaySetting = (bis108 ? rfidReaderChipR2000.rx000Setting.getCycleDelay() : rfidReaderChipE710.rx000Setting.getCycleDelay());
        return cycleDelaySetting;
    }
    public boolean setCycleDelay(long cycleDelay) {
        cycleDelaySetting = cycleDelay;
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setCycleDelay(cycleDelay) : rfidReaderChipE710.rx000Setting.setCycleDelay(cycleDelay));
    }
    public void getAuthenticateReplyLength() {
        if (bis108) rfidReaderChipR2000.rx000Setting.getAuthenticateReplyLength();
        else rfidReaderChipE710.rx000Setting.getAuthenticateReplyLength();
    }
    public boolean setTamConfiguration(boolean header, String matchData) {
        appendToLog("header = " + header + ", matchData.length = " + matchData.length() + ", matchData = " + matchData);
        if (matchData.length() != 12) return false;
        boolean retValue = false; String preChallenge = matchData.substring(0, 2);
        int iValue = Integer.parseInt(preChallenge, 16);
        iValue &= 0x07;
        if (header) iValue |= 0x04;
        else iValue &= ~0x04;
        preChallenge = String.format("%02X", iValue);
        matchData = preChallenge + matchData.substring(2);
        appendToLog("new matchData = " + matchData);
        if (bis108) {
            retValue = setAuthMatchData(matchData);
            appendToLog("setAuthMatchData returns " + retValue);
            if (retValue) {
                retValue = rfidReaderChipR2000.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 1, matchData.length() * 4);
                appendToLog("setHST_AUTHENTICATE_CFG returns " + retValue);
            }
            return retValue;
        } else {
            boolean bValue = rfidReaderChipE710.rx000Setting.setAuthenticateConfig(((matchData.length() * 4) << 10) | (1 << 2) | 0x03);
            appendToLog("setAuthenticateConfiguration 1 revised matchData = " + matchData + " with bValue = " + (bValue ? "true" : "false"));
            appendToLog("revised bytes = " + utility.byteArrayToString(utility.string2ByteArray(matchData)));
            if (bValue) {
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(utility.string2ByteArray(matchData));
                appendToLog("setAuthenticateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
            }
            if (bValue) {
                int iLength = 8 * 8;
                if (header) iLength = 16 * 8;
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateResponseLen(iLength);
                appendToLog("setAuthenticateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
            }
            return bValue;
        }
    }
    public boolean setTam1Configuration(int keyId, String matchData) {
        appendToLog("keyId = " + keyId + ", matchData = " + matchData);
        if (keyId > 255) return false;
        if (matchData.length() != 20) return false;

        boolean retValue = false; String preChallenge = "00";
        preChallenge += String.format("%02X", keyId);
        matchData = preChallenge + matchData;
        if (bis108) {
            retValue = setAuthMatchData(matchData);
            appendToLog("setAuthMatchData returns " + retValue);
            if (retValue) {
                retValue = rfidReaderChipR2000.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 0, matchData.length() * 4);
                appendToLog("setHST_AUTHENTICATE_CFG returns " + retValue);
            }
            return retValue;
        } else {
            boolean bValue = rfidReaderChipE710.rx000Setting.setAuthenticateConfig(((matchData.length() * 4) << 10) | (0 << 2) | 0x03);
            appendToLog("setAuthenticateConfiguration 1 revised matchData = " + matchData + " with bValue = " + (bValue ? "true" : "false"));
            appendToLog("revised bytes = " + utility.byteArrayToString(utility.string2ByteArray(matchData)));
            if (bValue) {
                if (true)
                    bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(utility.string2ByteArray(matchData));
                else
                    bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(new byte[]{
                            0, 0, (byte) 0xFD, (byte) 0x5D,
                            (byte) 0x80, 0x48, (byte) 0xF4, (byte) 0x8D,
                            (byte) 0xD0, (byte) 0x9A, (byte) 0xAD, 0x22});
                appendToLog("setAuthenticateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
            }
            if (bValue) {
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateResponseLen(16 * 8);
                appendToLog("setAuthenticateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
            }
            return bValue;
        }
    }
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        if (keyId > 255) return false;
        if (matchData.length() != 20) return false;
        if (profile > 15) return false;
        if (offset > 0xFFF) return false;
        if (blockId > 15) return false;
        if (protMode > 15) return false;

        boolean retValue = false;
        String preChallenge = "20";
        String postChallenge;
        preChallenge += String.format("%02X", keyId);
        postChallenge = String.valueOf(profile);
        postChallenge += String.format("%03X", offset);
        postChallenge += String.valueOf(blockId);
        postChallenge += String.valueOf(protMode);
        matchData = preChallenge + matchData + postChallenge;
        if (bis108) {
            retValue = setAuthMatchData(matchData);
            if (retValue) {
                retValue = rfidReaderChipR2000.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 0, matchData.length() * 4);
            }
            return retValue;
        } else {
            boolean bValue = rfidReaderChipE710.rx000Setting.setAuthenticateConfig(((matchData.length() * 4) << 10) | (0 << 2) | 0x03);
            appendToLog("setAuthenticateConfiguration 1 revised matchData = " + matchData + " with bValue = " + (bValue ? "true" : "false"));
            appendToLog("revised bytes = " + utility.byteArrayToString(utility.string2ByteArray(matchData)));
            if (bValue) {
                if (true)
                    bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(utility.string2ByteArray(matchData));
                else
                    bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(new byte[]{
                            0, 0, (byte) 0xFD, (byte) 0x5D,
                            (byte) 0x80, 0x48, (byte) 0xF4, (byte) 0x8D,
                            (byte) 0xD0, (byte) 0x9A, (byte) 0xAD, 0x22});
                appendToLog("setAuthenticateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
            }
            if (bValue) {
                int iSize = 32;
                if (protMode > 2) iSize = 44;
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateResponseLen(iSize * 8);
                appendToLog("setAuthenticateConfiguration 3: protMode = " + protMode + ", bValue = " + (bValue ? "true" : "false"));
            }
            return bValue;
        }
    }
    public int getUntraceableEpcLength() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getUntraceableEpcLength() : rfidReaderChipE710.rx000Setting.getUntraceableEpcLength());
    }
    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setHST_UNTRACEABLE_CFG(bHideRange ? 2 : 0, bHideUser, iHideTid, ishowEpcSize, bHideEpc, false) : rfidReaderChipE710.rx000Setting.setHST_UNTRACEABLE_CFG(bHideRange ? 2 : 0, bHideUser, iHideTid, ishowEpcSize, bHideEpc, false));
    }
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setHST_UNTRACEABLE_CFG(range, user, tid, epcLength, epc, uxpc) : rfidReaderChipE710.rx000Setting.setHST_UNTRACEABLE_CFG(range, user, tid, epcLength, epc, uxpc));
    }
    public boolean setAuthenticateConfiguration() {
        boolean bValue = (bis108 ? rfidReaderChipR2000.rx000Setting.setHST_AUTHENTICATE_CFG(true, true, 1, 48)
                : rfidReaderChipE710.rx000Setting.setAuthenticateConfig((48 << 10) | (1 << 2) | 0x03));
        if (bis108) {
            if (bValue) {
                bValue = rfidReaderChipR2000.rx000Setting.setAuthMatchData("049CA53E55EA"); //setAuthenticateMessage(new byte[] { 0x04, (byte)0x9C, (byte)0xA5, 0x3E, 0x55, (byte)0xEA } );
                appendToLog("setAuthenuateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
            }
        /*if (bValue) {
            bValue = mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000Setting.setAuthenticateResponseLen(16 * 8);
            appendToLog("setAuthenuateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
        }*/
            return false; //bValue;
        } else {
            if (bValue) {
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateMessage(new byte[]{0x04, (byte) 0x9C, (byte) 0xA5, 0x3E, 0x55, (byte) 0xEA});
                appendToLog("setAuthenticateConfiguration 2: bValue = " + (bValue ? "true" : "false"));
            }
            if (bValue) {
                bValue = rfidReaderChipE710.rx000Setting.setAuthenticateResponseLen(16 * 8);
                appendToLog("setAuthenticateConfiguration 3: bValue = " + (bValue ? "true" : "false"));
            }
            return bValue;
        }
    }
    public int getRetryCount() {
        if (bis108) {
            int algoSelect;
            algoSelect = rfidReaderChipR2000.rx000Setting.getAlgoSelect();
            if (algoSelect == 0 || algoSelect == 3) {
                return rfidReaderChipR2000.rx000Setting.getAlgoRetry(algoSelect);
            } else return -1;
        } else {
            return rfidReaderChipE710.rx000Setting.getAlgoMinQCycles();
        }
    }
    public boolean setRetryCount(int retryCount) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAlgoRetry(retryCount) : rfidReaderChipE710.rx000Setting.setAlgoMinQCycles(retryCount));
    }
    public int getInvSelectIndex() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getInvSelectIndex(): rfidReaderChipE710.rx000Setting.getInvSelectIndex());
    }
    public boolean getSelectEnable() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectEnable() : rfidReaderChipE710.rx000Setting.getSelectEnable());
        return iValue > 0 ? true : false;
    }
    public int getSelectTarget() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectTarget() : rfidReaderChipE710.rx000Setting.getSelectTarget());
    }
    public int getSelectAction() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectAction() : rfidReaderChipE710.rx000Setting.getSelectAction());
    }
    public int getSelectMaskBank() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectMaskBank() : rfidReaderChipE710.rx000Setting.getSelectMaskBank());
    }
    public int getSelectMaskOffset() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectMaskOffset() : rfidReaderChipE710.rx000Setting.getSelectMaskOffset());
    }
    public String getSelectMaskData() {
        int iValue1;
        iValue1 = (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectMaskLength() : rfidReaderChipE710.rx000Setting.getSelectMaskLength());
        if (iValue1 < 0) return null;
        String strValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectMaskData() : rfidReaderChipE710.rx000Setting.getSelectMaskData());
        if (strValue == null) return null;
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1) strLength++;
        if (false) appendToLog("Mask data = iValue1 = " + iValue1 + ", strValue = " + strValue + ", strLength = " + strLength);
        if (strValue.length() < strLength) strLength = strValue.length();
        return strValue.substring(0, strLength);
    }
    public boolean setInvSelectIndex(int invSelect) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setInvSelectIndex(invSelect) : rfidReaderChipE710.rx000Setting.setInvSelectIndex(invSelect));
    }
    public boolean setSelectCriteriaDisable(int index) {
        if (bis108) rfidReaderChipR2000.rx000Setting.setQuerySelect(0);
        else rfidReaderChipE710.rx000Setting.setQuerySelect(0);
        boolean bValue = false;
        if (index < 0) {
            for (int i = 0; i < 3; i++) {
                bValue = setSelectCriteria(i, false, 0, 0, 0, 0, 0, "");
                if (bValue == false) {
                    break;
                }
            }
        } else {
            appendToLog("setSelectCriteria loop ends");
            bValue = setSelectCriteria(index, false, 0, 0, 0, 0, 0, "");
        }
        return bValue;
    }
    public int findFirstEmptySelect() {
        int iValue = -1, iSelectEnable;
        for (int i = 0; i < 3; i++) {
            if (bis108) rfidReaderChipR2000.rx000Setting.setInvSelectIndex(i);
            iSelectEnable = (bis108 ? rfidReaderChipR2000.rx000Setting.getSelectEnable() : rfidReaderChipE710.rx000Setting.selectConfiguration[i][0]);
            if (iSelectEnable == 0) {
                iValue = i;
                appendToLog("cs710Library4A: setSelectCriteria 1 with New index = " + iValue);
                break;
            }
        }
        return iValue;
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        if (index == 0) settingData.preFilterData = new SettingData.PreFilterData(enable, target, action, bank, offset, mask, maskbit);
        if (index < 0) index = findFirstEmptySelect();
        if (index < 0) {
            appendToLog("cs710Library4A: no index is available !!!"); return false;
        }

        int maskblen = mask.length() * 4;
        String maskHex = ""; int iHex = 0;
        if (maskbit) {
            for (int i = 0; i < mask.length(); i++) {
                iHex <<= 1;
                if (mask.substring(i, i+1).matches("0")) iHex &= 0xFE;
                else if (mask.substring(i, i+1).matches("1"))  iHex |= 0x01;
                else return false;
                if ((i+1) % 4 == 0) maskHex += String.format("%1X", iHex & 0x0F);
            }
            int iBitRemain = mask.length() % 4;
            if (iBitRemain != 0) {
                iHex <<= (4 - iBitRemain);
                maskHex += String.format("%1X", iHex & 0x0F);
            }
            maskblen = mask.length();
            mask = maskHex;
        }
        return setSelectCriteria3(index, enable, target, action, 0, bank, offset, mask, maskblen);
    }
    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        if (bis108) {
            if (!enable) {
                if (bSelectEnabled[index] == enable) return true;
            }
            appendToLog("cs108Library4A: setSelectCriteria 2 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask);
            if (index < 0) index = findFirstEmptySelect();
            if (index < 0) {
                appendToLog("cs710Library4A: no index is available !!!");
                return false;
            }

            if (index == 0)
                settingData.preFilterData = new SettingData.PreFilterData(enable, target, action, bank, offset, mask, false);
            if (mask.length() > 64) mask = mask.substring(0, 64);
            if (index == 0)
                preMatchData = new RfidReader.PreMatchData(enable, target, action, bank, offset, mask, mask.length() * 4, rfidReaderChipR2000.rx000Setting.getQuerySelect(), getPwrlevel(), getInvAlgo(), getQValue());
            boolean result = true;
            if (index != rfidReaderChipR2000.rx000Setting.getInvSelectIndex())
                result = rfidReaderChipR2000.rx000Setting.setInvSelectIndex(index);
            if (rfidReaderChipR2000.rx000Setting.getSelectEnable() == 0 && enable == false) {
                appendToLog("cs108Library4A: setSelectCriteria 2: no need to set as when index = " + index + ", getSelectEnable() = " + rfidReaderChipR2000.rx000Setting.getSelectEnable() + ", new enable = " + enable);
                result = true;
            } else {
                if (result)
                    result = rfidReaderChipR2000.rx000Setting.setSelectEnable(enable ? 1 : 0, target, action, delay);
                if (result) result = rfidReaderChipR2000.rx000Setting.setSelectMaskBank(bank);
                if (result) result = rfidReaderChipR2000.rx000Setting.setSelectMaskOffset(offset);
                if (mask == null) return false;
                if (result)
                    result = rfidReaderChipR2000.rx000Setting.setSelectMaskLength(mask.length() * 4);
                if (result) result = rfidReaderChipR2000.rx000Setting.setSelectMaskData(mask);
                if (result) {
                    if (enable) {
                        rfidReaderChipR2000.rx000Setting.setTagSelect(1);
                        rfidReaderChipR2000.rx000Setting.setQuerySelect(3);
                    } else {
                        rfidReaderChipR2000.rx000Setting.setTagSelect(0);
                        rfidReaderChipR2000.rx000Setting.setQuerySelect(0);
                    }
                }
            }
            if (result) {
                bSelectEnabled[index] = enable;
            }
            return result;
        } else {
            boolean bValue = false, DEBUG = false;
            appendToLog("cs710Library4A: setSelectCriteria 2 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask);
            if (index < 0) index = findFirstEmptySelect();
            if (index < 0) {
                appendToLog("cs710Library4A: no index is available !!!"); return false;
            }

            if (rfidReaderChipE710.rx000Setting.selectConfiguration[index] == null) appendToLog("CANNOT continue as selectConfiguration[" + index + "] is null !!!");
            else if (rfidReaderChipE710.rx000Setting.selectConfiguration[index][0] != 0 || enable != false) {
                if (DEBUG) appendToLog("0 selectConfiguration[" + index + "] = " + utility.byteArrayToString(rfidReaderChipE710.rx000Setting.selectConfiguration[index]));
                byte[] byteArrayMask = null;
                if (mask != null) byteArrayMask = utility.string2ByteArray(mask);
                bValue = rfidReaderChipE710.rx000Setting.setSelectConfiguration(index, enable, bank, offset, byteArrayMask, target, action, delay);
                if (DEBUG) appendToLog("0 selectConfiguration[" + index + "] = " + utility.byteArrayToString(rfidReaderChipE710.rx000Setting.selectConfiguration[index]));
            } else {
                appendToLog("cs710Library4A: setSelectCriteria 2: no need to set as old selectConfiguration[" + index + "][0] = " + rfidReaderChipE710.rx000Setting.selectConfiguration[index][0] + ", new enable = " + enable);
                bValue = true;
            }
            return bValue;
        }
    }
    public boolean getRssiFilterEnable() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getRssiFilterType() : rfidReaderChipE710.rx000Setting.getRssiFilterType());
        if (iValue < 0) return false;
        iValue &= 0xF;
        return (iValue > 0 ? true : false);
    }
    public int getRssiFilterType() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getRssiFilterType() : rfidReaderChipE710.rx000Setting.getRssiFilterType());
        if (iValue < 0) return 0;
        iValue &= 0xF;
        if (iValue < 2) return 0;
        return iValue - 1;
    }
    public int getRssiFilterOption() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getRssiFilterOption() : rfidReaderChipE710.rx000Setting.getRssiFilterOption());
        if (iValue < 0) return 0;
        iValue &= 0xF;
        return iValue;
    }
    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        int iValue = 0;
        if (enable == false) iValue = 0;
        else iValue = rssiFilterType + 1;
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setHST_INV_RSSI_FILTERING_CONFIG(iValue, rssiFilterOption) : rfidReaderChipE710.rx000Setting.setHST_INV_RSSI_FILTERING_CONFIG(iValue, rssiFilterOption));
    }
    public double getRssiFilterThreshold1() {
        double dValue;
        if (bis108) {
            int iValue = rfidReaderChipR2000.rx000Setting.getRssiFilterThreshold1();
            byte byteValue = (byte) (iValue & 0xFF);
            dValue = rfidReaderChipR2000.decodeNarrowBandRSSI(byteValue);
        } else {
            int iValue = rfidReaderChipE710.rx000Setting.getRssiFilterThreshold1();
            dValue = (double) iValue;
            dValue /= 100;
            dValue += dBuV_dBm_constant;
        }
        return dValue;
    }
    public double getRssiFilterThreshold2() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getRssiFilterThreshold2() : rfidReaderChipE710.rx000Setting.getRssiFilterThreshold2());
        appendToLog("iValue = " + iValue);
        byte byteValue = (byte)(iValue & 0xFF);
        double dValue = (bis108 ? rfidReaderChipR2000.decodeNarrowBandRSSI(byteValue) : rfidReaderChipE710.decodeNarrowBandRSSI(byteValue));
        return dValue;
    }
    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        if (bis108) {
            appendToLog("rssiFilterThreshold = " + rssiFilterThreshold1 + ", " + rssiFilterThreshold2);
            return rfidReaderChipR2000.rx000Setting.setHST_INV_RSSI_FILTERING_THRESHOLD(rfidReaderChipR2000.encodeNarrowBandRSSI(rssiFilterThreshold1), rfidReaderChipR2000.encodeNarrowBandRSSI(rssiFilterThreshold2));
        } else {
            appendToLog("rssiFilterThreshold = " + rssiFilterThreshold1 + ", rssiFilterThreshold2 = " + rssiFilterThreshold2);
            rssiFilterThreshold1 -= dBuV_dBm_constant;
            rssiFilterThreshold2 -= dBuV_dBm_constant;
            appendToLog("After adjustment, rssiFilterThreshold = " + rssiFilterThreshold1 + ", rssiFilterThreshold2 = " + rssiFilterThreshold2);
            rssiFilterThreshold1 *= 100;
            rssiFilterThreshold2 *= 100;
            appendToLog("After multiplication, rssiFilterThreshold = " + rssiFilterThreshold1 + ", rssiFilterThreshold2 = " + rssiFilterThreshold2);
            return rfidReaderChipE710.rx000Setting.setHST_INV_RSSI_FILTERING_THRESHOLD((int) rssiFilterThreshold1, (int) rssiFilterThreshold2);
        }
    }
    public long getRssiFilterCount() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getRssiFilterCount() : rfidReaderChipE710.rx000Setting.getRssiFilterCount());
    }
    public boolean setRssiFilterCount(long rssiFilterCount) {
        appendToLog("rssiFilterCount = " + rssiFilterCount);
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setHST_INV_RSSI_FILTERING_COUNT(rssiFilterCount) : rfidReaderChipE710.rx000Setting.setHST_INV_RSSI_FILTERING_COUNT(rssiFilterCount));
    }
    public boolean getInvMatchEnable() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getInvMatchEnable() : rfidReaderChipE710.rx000Setting.getInvMatchEnable());
        return iValue > 0 ? true : false;
    }
    public boolean getInvMatchType() {
        int iValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getInvMatchType() : rfidReaderChipE710.rx000Setting.getInvMatchType());
        return iValue > 0 ? true : false;
    }
    public int getInvMatchOffset() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getInvMatchOffset() : rfidReaderChipE710.rx000Setting.getInvMatchOffset());
    }
    public String getInvMatchData() {
        int iValue1 = (bis108 ? rfidReaderChipR2000.rx000Setting.getInvMatchLength() : rfidReaderChipE710.rx000Setting.getInvMatchLength());
        if (iValue1 < 0)    return null;
        String strValue = (bis108 ? rfidReaderChipR2000.rx000Setting.getInvMatchData() : rfidReaderChipE710.rx000Setting.getInvMatchData());
        int strLength = iValue1 / 4;
        if (strLength * 4 != iValue1)  strLength++;
        return strValue.substring(0, strLength);
    }
    String getSpecialCountryVersion() {
        boolean DEBUG = false;
        String strSpecialCountryCode = null;
        int iValue = rfidReaderChipE710.rx000Setting.getCountryEnum();
        if (DEBUG) appendToLog("getCountryEnum 0x3014 = " + iValue);
        if (iValue > 0 && iValue < strCountryEnumInfo.length/iCountryEnumInfoColumn) {
            String strCountryCode = strCountryEnumInfo[(iValue - 1) * iCountryEnumInfoColumn + 2];
            if (DEBUG) appendToLog("strCountryCode 0 = " + strCountryCode);
            String[] countryCodePart = strCountryCode.split(" ");
            if (DEBUG) appendToLog("countryCodePart.length = " + countryCodePart.length);
            if (countryCodePart.length >= 2) strSpecialCountryCode = countryCodePart[1];
            else strSpecialCountryCode = "";
            if (DEBUG) appendToLog("strSpecialCountryCode = " + strSpecialCountryCode);
        }
        if (true) {
            String strValue = rfidReaderChipE710.rx000Setting.getSpecialCountryCodeOem();
            if (DEBUG) appendToLog("getCountryCodeOem 0xefac = " + strValue);
            if (strSpecialCountryCode == null && strValue != null) {
                if (DEBUG) appendToLog("strSpecialCountryCode is replaced with countryCodeOem");
                strSpecialCountryCode = strValue;
            }
        }
        return strSpecialCountryCode;
    }
    public static class PostMatchData {
        public boolean enable; public boolean target; public int offset; public String mask; public long pwrlevel; public boolean invAlgo; public int qValue;
        public PostMatchData(boolean enable, boolean target, int offset, String mask, int antennaCycle, long pwrlevel, boolean invAlgo, int qValue) {
            this.enable = enable;
            this.target = target;
            this.offset = offset;
            this.mask = mask;
            this.pwrlevel = pwrlevel;
            this.invAlgo = invAlgo;
            this.qValue = qValue;
        }
    }
    public PostMatchData postMatchData;
    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        postMatchData = new RfidReader.PostMatchData(enable, target, offset, mask, getAntennaCycle(), getPwrlevel(), getInvAlgo(), getQValue());
        boolean result = (bis108 ? rfidReaderChipR2000.rx000Setting.setInvMatchEnable(enable ? 1 : 0, target ? 1 : 0, mask == null ? -1 : mask.length() * 4, offset)
                : rfidReaderChipE710.rx000Setting.setInvMatchEnable(enable ? 1 : 0, target ? 1 : 0, mask == null ? -1 : mask.length() * 4, offset));
        if (result && mask != null) result = (bis108 ? rfidReaderChipR2000.rx000Setting.setInvMatchData(mask) : rfidReaderChipE710.rx000Setting.setInvMatchData(mask));
        return result;
    }
    public int mrfidToWriteSize() {
        return mRfidToWrite.size();
    }
    public void mrfidToWritePrint() {
        for (int i = 0; i < mRfidToWrite.size(); i++) {
            appendToLog(utility.byteArrayToString(mRfidToWrite.get(i).dataValues));
        }
    }
    public long getTagRate() {
        return (bis108 ? -1 : rfidReaderChipE710.rx000Setting.getTagRate());
    }
    public boolean startOperation(RfidReaderChipData.OperationTypes operationTypes) {
        boolean retValue = false;
        switch (operationTypes) {
            case TAG_INVENTORY_COMPACT:
            case TAG_INVENTORY:
            case TAG_SEARCHING:
                //setInventoring(true);
                if (operationTypes == RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT) {
                    if (false && tagFocus >= 1) {
                        setTagGroup(-1, 1, 0);  //Set Session S1, Target A
                        if (bis108) {
                            rfidReaderChipR2000.rx000Setting.setTagDelay(0);
                            rfidReaderChipR2000.rx000Setting.setAntennaDwell(2000);
                        } else {
                            rfidReaderChipE710.rx000Setting.setTagDelay(0);
                            rfidReaderChipE710.rx000Setting.setAntennaDwell(2000);
                        }
                    }
                    if (bis108) { rfidReaderChipR2000.rx000Setting.setInvModeCompact(true); } else { rfidReaderChipE710.rx000Setting.setInvModeCompact(true); }
                } else {
                    if (bis108) {
                        rfidReaderChipR2000.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting);
                        rfidReaderChipR2000.rx000Setting.setCycleDelay(cycleDelaySetting);
                        rfidReaderChipR2000.rx000Setting.setInvModeCompact(false);
                    } else {
                        rfidReaderChipE710.rx000Setting.setTagDelay(tagDelayDefaultNormalSetting);
                        rfidReaderChipE710.rx000Setting.setCycleDelay(cycleDelaySetting);
                        rfidReaderChipE710.rx000Setting.setInvModeCompact(false);
                    }
                    if (operationTypes == RfidReaderChipData.OperationTypes.TAG_SEARCHING && bis108 == false) rfidReaderChipE710.rx000Setting.setDupElimRollWindow((byte)0);
                }
                if (bis108) {
                    notificationConnector.getAutoRFIDAbort();
                    notificationConnector.setAutoRFIDAbort(true);
                    notificationConnector.getAutoRFIDAbort();
                    rfidReaderChipR2000.setPwrManagementMode(false);
                    appendToLog("going to sendHostRegRequestHST_CMD(Cs108Library4A.HostCommands.CMD_18K6CINV)");

                    retValue = true;
                    RfidReaderChipData.HostCommands hostCommand = RfidReaderChipData.HostCommands.CMD_18K6CINV;
                    retValue = rfidReaderChipR2000.sendHostRegRequestHST_CMD(hostCommand);
                    break;
                } else {
                    rfidReaderChipE710.rx000Setting.setEventPacketUplinkEnable((byte)0x09);
                    rfidReaderChipE710.setPwrManagementMode(false);
                    RfidReaderChipData.HostCommands hostCommands = RfidReaderChipData.HostCommands.CMD_18K6CINV;
                    appendToLog("BtData: tagFocus = " + rfidReaderChipE710.rx000Setting.getImpinjExtension());
                    boolean bTagFocus = ((rfidReaderChipE710.rx000Setting.getImpinjExtension() & 0x04) != 0);
                    appendToLog("0 OperationTypes = " + operationTypes.toString() + ", hostCommands = " + hostCommands.toString() + ", bTagFocus = " + bTagFocus);
                    if (operationTypes == RfidReaderChipData.OperationTypes.TAG_INVENTORY_COMPACT) hostCommands = RfidReaderChipData.HostCommands.CMD_18K6CINV_COMPACT;
                    else if (rfidReaderChipE710.rx000Setting.getTagRead() != 0 && bTagFocus == false) hostCommands = RfidReaderChipData.HostCommands.CMD_18K6CINV_MB;
                    appendToLog("1 OperationTypes = " + operationTypes.toString() + ", hostCommands = " + hostCommands.toString());
                    retValue = rfidReaderChipE710.sendHostRegRequestHST_CMD(hostCommands);
                    break;
                }
        }
        return retValue;
    }
    public boolean abortOperation() {
        boolean bRetValue = (bis108 ? rfidReaderChipR2000.sendControlCommand(RfidReaderChipR2000.ControlCommands.ABORT) : rfidReaderChipE710.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.NULL));
        setInventoring(false);
        return bRetValue;
    }
    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        if (pwrlevel < 0) pwrlevel = pwrlevelSetting;
        return setSelectedTag1(strTagId, 2, 0, 0, pwrlevel, 0, 0);
    }
    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        boolean isValid = false;
        if (selectBank < 0 || selectBank > 3) return false;
        int selectOffset = (selectBank == 1 ? 32 : 0);
        isValid = setSelectCriteriaDisable(-1);
        if (isValid) isValid = setSelectedTag1(strTagId, selectBank, selectOffset, 0, pwrlevel, 0, 0);
        return isValid;
    }
    public boolean setSelectedTag(String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        appendToLog("cs108LibraryA: setSelectCriteria strTagId = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        return setSelectedTag1(selectMask, selectBank, selectOffset, 0, pwrlevel, qValue, matchRep);
    }
    public boolean setMatchRep(int matchRep) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setMatchRep(matchRep) : rfidReaderChipE710.rx000Setting.setMatchRep(matchRep));
    }
    public String[] getCountryList() {
        String[] strCountryList = null;
        RegionCodes[] regionList = getRegionList();
        if (regionList != null) {
            strCountryList = new String[regionList.length];
            for (int i = 0; i < regionList.length; i++) {
                strCountryList[i] = regionCode2StringArray(regionList[i]);
            }
        }
        return strCountryList;
    }
    public int countryInList = -1, countryInListDefault = -1;
    public int getCountryNumberInList() {
        return countryInList;
    }
    public boolean setCountryInList(int countryInList) {
        if (bis108) {
            boolean DEBUG = true;
            if (DEBUG)
                appendToLog("this.countryInList =" + this.countryInList + ", countryInList = " + countryInList);
            if (this.countryInList == countryInList) return true;

            RegionCodes[] regionList = getRegionList();
            if (DEBUG)
                appendToLog("regionList length =" + (regionList == null ? "NULL" : regionList.length));
            if (regionList == null) return false;
            if (countryInList < 0 || countryInList >= regionList.length) return false;

            int[] freqDataTableOld = FreqTable(regionCode);
            if (DEBUG)
                appendToLog("regionCode =" + regionCode + ", freqDataTableOld length = " + (freqDataTableOld == null ? "NULL" : freqDataTableOld.length));
            if (freqDataTableOld == null) return false;

            RegionCodes regionCodeNew = regionList[countryInList];
            final int[] freqDataTable = FreqTable(regionCodeNew);
            if (DEBUG)
                appendToLog("regionCodeNew =" + regionCodeNew + ", freqDataTable length = " + (freqDataTable == null ? "NULL" : freqDataTable.length));
            if (freqDataTable == null) return false;

            this.countryInList = countryInList;
            appendToLog("saveSetting2File testpoint 4");
            regionCode = regionCodeNew;
            if (DEBUG)
                appendToLog("getChannel =" + getChannel() + ", FreqChnCnt = " + FreqChnCnt());
            appendToLog("X channel = ");
            if (getChannel() >= FreqChnCnt()) setChannel(0);
            switch (getCountryCode()) {
                case 1:
                case 5:
                case 8:
                case 9:
                    break;
                case 2:
                    if (false && regionCode == regionCodeDefault4Country2) {
                        if (DEBUG) appendToLog("FCC Region is set");
//                        toggledConnection = false;
//                        mHandler.removeCallbacks(runnableToggleConnection);
//                        mHandler.postDelayed(runnableToggleConnection, 500);
                        return true;
                    }
                default:    //  2, 4, 7
                    if (freqDataTable.length == freqDataTableOld.length) {
                        int i = 0;
                        for (; i < freqDataTable.length; i++) {
                            if (freqDataTable[i] != freqDataTableOld[i]) break;
                        }
                        if (i == freqDataTable.length) {
                            if (DEBUG) appendToLog("Break as same freqDataTable");
                            break;
                        }
                    }
                    if (DEBUG) appendToLog("Finish as different freqDataTable");
                    int k = 0;
                    for (; k < freqDataTable.length; k++) {
                        if (DEBUG) appendToLog("Setting channel = " + k);
                        rfidReaderChipR2000.rx000Setting.setFreqChannelSelect(k);
                        rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(true);
                        rfidReaderChipR2000.rx000Setting.setFreqPllMultiplier(freqDataTable[k]);
                    }
                    for (; k < 50; k++) {
                        if (DEBUG) appendToLog("Resetting channel = " + k);
                        rfidReaderChipR2000.rx000Setting.setFreqChannelSelect(k);
                        rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(false);
                    }
                    break;
            }
            if (DEBUG)
                appendToLog("New regionCode = " + regionCode.toString() + ", channel = " + getChannel() + ", FreqChnCnt = " + FreqChnCnt());
            return true;
        } else {
            boolean DEBUG = true;
            if (this.countryInList == countryInList) return true;

            if (DEBUG) appendToLog("1 setCountryInList with countryInList = " + countryInList);
            RegionCodes[] regionList = getRegionList();
            if (regionList == null) return false;

            RegionCodes regionCodeNew = regionList[countryInList];
            regionCode = regionCodeNew;

            int indexBegin = RegionCodes.Albania1.ordinal();
            int indexEnd = RegionCodes.Vietnam3.ordinal();
            int i = indexBegin;
            for (; i < indexEnd + 1; i++) {
                if (regionCode == RegionCodes.values()[i]) {
                    break;
                }
            }

            boolean bValue = false;
            if (i < indexEnd + 1) {
                appendToLog("countryEnum: i = " + i + ", indexEnd = " + indexEnd);
                bValue = rfidReaderChipE710.rx000Setting.setCountryEnum((short)(i - indexBegin + 1));
                if (bValue) {
                    this.countryInList = countryInList;
                    channelOrderType = -1;
                }
            }
            if (DEBUG) appendToLog("1A setCountryInList with bValue = " + bValue);
            return bValue;
        }
    }
    public int channelOrderType; // 0 for frequency hopping / agile, 1 for fixed frequencey
    public boolean getChannelHoppingStatus() {
        if (bis108) {
            appendToLog("countryCode with channelOrderType = " + channelOrderType);
            if (channelOrderType < 0) {
                if (getChannelHoppingDefault()) channelOrderType = 0;
                else channelOrderType = 1;
            }
            return (channelOrderType == 0 ? true : false);
        } else {
            boolean bValue = false, DEBUG = false;
            int iValue = rfidReaderChipE710.rx000Setting.getCountryEnum(); //iValue--;
            if (DEBUG) appendToLog("getChannelHoppingStatus: countryEnum = " + iValue);
            if (iValue > 0) {
                String strFixedHop = strCountryEnumInfo[(iValue - 1) * iCountryEnumInfoColumn + 4];
                if (DEBUG) appendToLog("getChannelHoppingStatus: FixedHop = " + strFixedHop);
                if (strFixedHop.matches("Hop")) {
                    if (DEBUG) appendToLog("getChannelHoppingStatus: matched");
                    bValue = true;
                }
            }
            if (DEBUG) appendToLog("getChannelHoppingStatus: bValue = " + bValue);
            return bValue; //1 for hopping, 0 for fixed
        }
    }
    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        if (this.channelOrderType != (channelOrderHopping ? 0 : 1)) {
            if (bis108) {
                boolean result = true;
                if (getChannelHoppingDefault() == false) {
                    result = rfidReaderChipR2000.rx000Setting.setAntennaFreqAgile(channelOrderHopping ? 1 : 0);
                }
                int freqcnt = FreqChnCnt();
                appendToLog("FrequencyA Count = " + freqcnt);
                int channel = getChannel();
                appendToLog(" FrequencyA Channel = " + channel);
                for (int i = 0; i < freqcnt; i++) {
                    if (result == true) rfidReaderChipR2000.rx000Setting.setFreqChannelSelect(i);
                    if (result == true) rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(channelOrderHopping);
                }
                if (result == true) rfidReaderChipR2000.rx000Setting.setFreqChannelSelect(channel);
                if (result == true) rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(true);
            } else {
                boolean result = true;
                if (getChannelHoppingDefault() == false) {
                    result = rfidReaderChipE710.rx000Setting.setAntennaFreqAgile(channelOrderHopping ? 1 : 0);
                }
                int freqcnt = FreqChnCnt(); appendToLog("FrequencyA Count = " + freqcnt);
                int channel = getChannel(); appendToLog(" FrequencyA Channel = " + channel);
            }
            appendToLog(" FrequencyA: end of setting");

            this.channelOrderType = (channelOrderHopping ? 0 : 1);
            appendToLog("setChannelHoppingStatus: channelOrderType = " + channelOrderType);
        }
        return true;
    }
    public String[] getChannelFrequencyList() {
        if (bis108) {
            boolean DEBUG = true;
            appendToLog("regionCode is " + regionCode.toString());
            double[] table = GetAvailableFrequencyTable(regionCode);
            appendToLog("table length = " + table.length);
            for (int i = 0; i < table.length; i++) appendToLog("table[" + i + "] = " + table[i]);
            String[] strChannnelFrequencyList = new String[table.length];
            for (int i = 0; i < table.length; i++) {
                strChannnelFrequencyList[i] = String.format("%.2f MHz", table[i]);
                appendToLog("strChannnelFrequencyList[" + i + "] = " + strChannnelFrequencyList[i]);
            }
            return strChannnelFrequencyList;
        } else {
            boolean DEBUG = true;
            int iCountryEnum = rfidReaderChipE710.rx000Setting.getCountryEnum();
            appendToLog("countryEnum = " + iCountryEnum);
            appendToLog("i = " + iCountryEnum + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 0]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 1]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 2]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 3]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 4]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 5]
                    + ", " + strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 6]
            );
            int iFrequencyCount = Integer.valueOf(strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 3]);
            int iFrequencyInterval = Integer.valueOf(strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 5]);
            float iFrequencyStart = Float.valueOf(strCountryEnumInfo[(iCountryEnum - 1) * iCountryEnumInfoColumn + 6]);
            appendToLog("iFrequencyCount = " + iFrequencyCount + ", interval = " + iFrequencyInterval + ", start = " + iFrequencyStart);

            String[] strChannnelFrequencyList = new String[iFrequencyCount];
            for (int i = 0; i < iFrequencyCount ; i++) {
                strChannnelFrequencyList[i] = String.format("%.2f MHz", (iFrequencyStart * 1000 + iFrequencyInterval * i) / 1000);
                appendToLog("strChannnelFrequencyList[" + i + "] = " + strChannnelFrequencyList[i]);
            }
            return strChannnelFrequencyList;
        }
    }
    public int getChannel() {
        if (true) return settingData.channel;
        if (bis108) {
            int channel = -1;
            if (rfidReaderChipR2000.rx000Setting.getFreqChannelConfig() != 0) {
                channel = rfidReaderChipR2000.rx000Setting.getFreqChannelSelect();
                appendToLog("loadSetting1File: getting channel = " + channel);
            }
            if (getChannelHoppingStatus()) {
                channel = 0;
            }
            return channel;
        } else {
            int channel = -1;
            channel = rfidReaderChipE710.rx000Setting.getFrequencyChannelIndex();
            if (getChannelHoppingStatus()) channel = 0;
            else if (channel > 0) channel--;
            return channel;
        }
    }
    public boolean setChannel(int channelSelect) {
        if (bis108) {
            boolean result = true;
            if (result == true) result = rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(false);
            if (result == true)
                result = rfidReaderChipR2000.rx000Setting.setFreqChannelSelect(channelSelect);
            if (result == true) result = rfidReaderChipR2000.rx000Setting.setFreqChannelConfig(true);
            if (result) settingData.channel = channelSelect;
            return result;
        } else {
            boolean result = true; int channel = channelSelect;
            if (getChannelHoppingStatus()) channelSelect = 0;
            else channelSelect++;
            if (result == true)    result = rfidReaderChipE710.rx000Setting.setFrequencyChannelIndex((byte)(channelSelect & 0xFF));
            if (result) settingData.channel = channel;
            return result;
        }
    }
    public byte getPopulation2Q(int population) {
        double dValue = 1 + log10(population * 2) / log10(2);
        if (dValue < 0) dValue = 0;
        if(dValue > 15) dValue = 15;
        byte iValue = (byte) dValue;
        if (DEBUG || true) appendToLog("getPopulation2Q(" + population + "): log dValue = " + dValue + ", iValue = " + iValue);
        return iValue;
    }
    public int population = 30;
    public int getPopulation() {
        return population;
    }
    public boolean setPopulation(int population) {
        if (true) appendToLog("setPopulation " + population);
        byte iValue = getPopulation2Q(population);
        if (true) appendToLog("getPopulation2Q = " + iValue);
        this.population = population;
        return setQValue(iValue);
    }
    public byte qValueSetting = -1;
    public byte getQValue() {
        return qValueSetting;
    }
    public boolean setQValue(byte byteValue) {
        qValueSetting = byteValue;
        if (false) appendToLog("Stream population qValue = " + qValueSetting);
        return setQValue1(byteValue);
    }
    int getQValue1() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAlgoStartQ() : rfidReaderChipE710.rx000Setting.getAlgoStartQ());
    }
    public boolean setQValue1(int iValue) {
        if (bis108) {
            boolean result = true;
            {
                int invAlgo = rfidReaderChipR2000.rx000Setting.getInvAlgo();
                if (iValue != rfidReaderChipR2000.rx000Setting.getAlgoStartQ(invAlgo)) {
                    if (false)
                        appendToLog("setTagGroup: going to setAlgoSelect with invAlgo = " + invAlgo);
                    result = rfidReaderChipR2000.rx000Setting.setAlgoSelect(invAlgo);
                }
            }
            if (result) {
                result = rfidReaderChipR2000.rx000Setting.setAlgoStartQ(iValue);
            }
            return result;
        } else {
            boolean result = true;
            if (false) appendToLog("3 setAlgoStartQ with iValue = " + iValue);
            result = rfidReaderChipE710.rx000Setting.setAlgoStartQ(iValue);
            return result;
        }
    }
    public static final double dBuV_dBm_constant = 106.98;
    public RfidReaderChipData.Rx000pkgData onRFIDEvent() {
        if (bis108) {
            RfidReaderChipData.Rx000pkgData rx000pkgData = null;
            //if (mrfidToWriteSize() != 0) mRfidReaderChip.mRx000ToRead.clear();
            if (rfidReaderChipR2000.bRx000ToReading == false && rfidReaderChipR2000.mRx000ToRead.size() != 0) {
                rfidReaderChipR2000.bRx000ToReading = true;
                int index = 0;
                try {
                    rx000pkgData = rfidReaderChipR2000.mRx000ToRead.get(index);
                    if (false)
                        appendToLog("rx000pkgData.type = " + rx000pkgData.responseType.toString());
                    rfidReaderChipR2000.mRx000ToRead.remove(index); //appendToLog("mRx000ToRead.remove");
                } catch (Exception ex) {
                    rx000pkgData = null;
                }
                rfidReaderChipR2000.bRx000ToReading = false;
            }
            return rx000pkgData;
        } else {
            boolean DEBUG = false;
            RfidReaderChipData.Rx000pkgData rx000pkgData = null;
            //if (mrfidToWriteSize() != 0) mRfidDevice.mRfidReaderChip.mRfidReaderChip.mRx000ToRead.clear();
            if (rfidReaderChipE710.bRx000ToReading == false && rfidReaderChipE710.mRx000ToRead.size() != 0) {
                rfidReaderChipE710.bRx000ToReading = true;
                int index = 0;
                try {
                    rx000pkgData = rfidReaderChipE710.mRx000ToRead.get(index);
                    if (false && rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_COMMAND_END)
                        if (DEBUG) appendToLog("get mRx000ToRead with COMMAND_END");
                    rfidReaderChipE710.mRx000ToRead.remove(index);
                    if (DEBUG) appendToLog("got one mRx000ToRead with responseType = " + rx000pkgData.responseType.toString() + ", and remained size = " + rfidReaderChipE710.mRx000ToRead.size());
                } catch (Exception ex) {
                    rx000pkgData = null;
                }
                rfidReaderChipE710.bRx000ToReading = false;
            }
            if (rx000pkgData != null && rx000pkgData.responseType != null) {
                if (rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY || rx000pkgData.responseType == RfidReaderChipData.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT) {
                    if (DEBUG) appendToLog("Before adjustment, decodedRssi = " + rx000pkgData.decodedRssi);
                    rx000pkgData.decodedRssi += dBuV_dBm_constant;
                    if (DEBUG) appendToLog("After adjustment, decodedRssi = " + rx000pkgData.decodedRssi);
                    if (rfidReaderChipE710.rx000Setting.getInvMatchEnable() > 0) {
                        byte[] bytesCompared = new byte[rx000pkgData.decodedEpc.length];
                        System.arraycopy(rx000pkgData.decodedEpc, 0, bytesCompared, 0, rx000pkgData.decodedEpc.length);
                        //bytesCompared = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x2F };
                        //bytesCompared = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0x2F };
                        appendToLog("decodedEpc = " + utility.byteArrayToString(rx000pkgData.decodedEpc));
                        if (rfidReaderChipE710.rx000Setting.getInvMatchOffset() > 0) {
                            appendToLog("getInvMatchOffset = " + rfidReaderChipE710.rx000Setting.getInvMatchOffset());
                            BigInteger bigInt = new BigInteger(bytesCompared);
                            BigInteger shiftInt = bigInt.shiftLeft(rfidReaderChipE710.rx000Setting.getInvMatchOffset());
                            byte [] shifted = shiftInt.toByteArray();
                            appendToLog("shifted = " + utility.byteArrayToString(shifted));
                            if (shifted.length > bytesCompared.length) System.arraycopy(shifted, shifted.length - bytesCompared.length, bytesCompared, 0, bytesCompared.length);
                            else if (shifted.length < bytesCompared.length) {
                                System.arraycopy(shifted, 0, bytesCompared, bytesCompared.length - shifted.length, shifted.length);
                                for (int i = 0; i < bytesCompared.length - shifted.length; i++) {
                                    if ((shifted[0] & 0x80) == 0) bytesCompared[i] = 0;
                                    else bytesCompared[i] = (byte)0xFF;
                                }
                            }
                            appendToLog("new bytesCompared 1 = " + utility.byteArrayToString(bytesCompared));
                        }

                        if (rfidReaderChipE710.rx000Setting.getInvMatchType() > 0) {
                            appendToLog("getInvMatchType = " + rfidReaderChipE710.rx000Setting.getInvMatchType());
                            for (int i = 0; i < bytesCompared.length; i++) {
                                bytesCompared[i] ^= (byte) 0xFF;
                            }
                        }
                        appendToLog("new bytesCompared 2 = " + utility.byteArrayToString(bytesCompared));
                        appendToLog("getInvMatchData = " + rfidReaderChipE710.rx000Setting.getInvMatchData());
                        if (utility.byteArrayToString(bytesCompared).indexOf(rfidReaderChipE710.rx000Setting.getInvMatchData()) != 0) {
                            appendToLog("Post Mis-Matched !!!");
                            rx000pkgData = null;
                        }
                    }
                }
            }
            if (rx000pkgData != null && DEBUG) appendToLog("response = " + rx000pkgData.responseType.toString() + ", " + utility.byteArrayToString(rx000pkgData.dataValues));
            return rx000pkgData;
        }
    }
    public String getModelNumber(String strModelName) {
        if (bis108) {
            int iCountryCode = getCountryCode();
            String strCountryCode = "";
            appendToLog("iCountryCode = " + iCountryCode + ", strModelNumber = " + strModelName);
            if (strModelName != null && strModelName.length() != 0) {
                if (iCountryCode > 0)
                    strCountryCode = strModelName + "-" + String.valueOf(iCountryCode) + " " + rfidReaderChipR2000.rx000OemSetting.getSpecialCountryVersion();
                else strCountryCode = strModelName;
            }
            return strCountryCode;
        } else {
            boolean DEBUG = false;
            if (DEBUG) appendToLog("getModelName = " + strModelName);
            int iCountryCode = getCountryCode();
            if (DEBUG) appendToLog("getCountryCode = " + iCountryCode);
            String strSpecialCountryVersion = getSpecialCountryVersion();
            if (DEBUG) appendToLog("getSpecialCountryVersion = " + strSpecialCountryVersion);
            int iFreqModifyCode = getFreqModifyCode();
            if (DEBUG) appendToLog("getFreqModifyCode = " + iFreqModifyCode);

            String strModelNumber = (strModelName == null ? "" : strModelName);
            if (iCountryCode > 0) strModelNumber += ("-" + String.valueOf(iCountryCode) + (strSpecialCountryVersion == null ? "" : (" " + strSpecialCountryVersion)));
            if (DEBUG) appendToLog("strModelNumber = " + strModelNumber);
            return strModelNumber;
        }
    }
    public boolean setRx000KillPassword(String password) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setRx000KillPassword(password) : rfidReaderChipE710.rx000Setting.setRx000KillPassword(password));
    }
    public boolean setRx000AccessPassword(String password) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setRx000AccessPassword(password) : rfidReaderChipE710.rx000Setting.setRx000AccessPassword(password));
    }
    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessRetry(accessVerfiy, accessRetry) : rfidReaderChipE710.rx000Setting.setAccessRetry(accessVerfiy, accessRetry));
    }
    public boolean setInvModeCompact(boolean invModeCompact) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setInvModeCompact(invModeCompact) : rfidReaderChipE710.rx000Setting.setInvModeCompact(invModeCompact));
    }
    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessLockAction(accessLockAction, accessLockMask)
                : rfidReaderChipE710.rx000Setting.setAccessLockAction(accessLockAction, accessLockMask));
    }
    public boolean setAccessBank(int accessBank) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessBank(accessBank) : rfidReaderChipE710.rx000Setting.setAccessBank(accessBank));
    }
    public boolean setAccessBank(int accessBank, int accessBank2) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessBank(accessBank, accessBank2) : rfidReaderChipE710.rx000Setting.setAccessBank(accessBank, accessBank2));
    }
    public boolean setAccessOffset(int accessOffset) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessOffset(accessOffset) : rfidReaderChipE710.rx000Setting.setAccessOffset(accessOffset));
    }
    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessOffset(accessOffset, accessOffset2) : rfidReaderChipE710.rx000Setting.setAccessOffset(accessOffset, accessOffset2));
    }
    public boolean setAccessCount(int accessCount) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessCount(accessCount) : rfidReaderChipE710.rx000Setting.setAccessCount(accessCount));
    }
    public boolean setAccessCount(int accessCount, int accessCount2) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessCount(accessCount, accessCount2) : rfidReaderChipE710.rx000Setting.setAccessCount(accessCount, accessCount2));
    }
    public boolean setAccessWriteData(String dataInput) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAccessWriteData(dataInput) : rfidReaderChipE710.rx000Setting.setAccessWriteData(dataInput));
    }
    public boolean setResReadNoReply(boolean resReadNoReply) {
        return (bis108 ? false : rfidReaderChipE710.rx000Setting.setResReadNoReply(resReadNoReply));
    }
    public boolean setTagRead(int tagRead) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setTagRead(tagRead) : rfidReaderChipE710.rx000Setting.setTagRead(tagRead));
    }
    public boolean setInvBrandId(boolean invBrandId) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setInvBrandId(invBrandId) : rfidReaderChipE710.rx000Setting.setInvBrandId(invBrandId));
    }
    public boolean sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands hostCommand) {
        if (bis108) rfidReaderChipR2000.setPwrManagementMode(false);
        else rfidReaderChipE710.setPwrManagementMode(false);
        return (bis108 ? rfidReaderChipR2000.sendHostRegRequestHST_CMD(hostCommand) : rfidReaderChipE710.sendHostRegRequestHST_CMD(hostCommand));
    }
    public boolean setPwrManagementMode(boolean bLowPowerStandby) {
        return (bis108 ? rfidReaderChipR2000.setPwrManagementMode(bLowPowerStandby) : rfidReaderChipE710.setPwrManagementMode(bLowPowerStandby));
    }
    void macRead(int address) {
        if (bis108) rfidReaderChipR2000.rx000Setting.readMAC(address);
        else rfidReaderChipE710.rx000Setting.readMAC(address);
    }
    public boolean macWrite(int address, long value) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.writeMAC(address, value) : rfidReaderChipE710.rx000Setting.writeMAC(address, value));
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
    public String getSerialNumber() {
        return (bis108 ? rfidReaderChipR2000.rx000OemSetting.getSerialNumber() : rfidReaderChipE710.rx000Setting.getBoardSerialNumber());
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
    public int getAntennaCycle() {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.getAntennaCycle() : rfidReaderChipE710.rx000Setting.getAntennaCycle());
    }
    public boolean setAntennaCycle(int antennaCycle) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaCycle(antennaCycle) : rfidReaderChipE710.rx000Setting.setAntennaCycle(antennaCycle));
    }
    public boolean setAntennaInvCount(long antennaInvCount) {
        return (bis108 ? rfidReaderChipR2000.rx000Setting.setAntennaInvCount(antennaInvCount) : rfidReaderChipE710.rx000Setting.setAntennaInvCount(antennaInvCount));
    }
}
