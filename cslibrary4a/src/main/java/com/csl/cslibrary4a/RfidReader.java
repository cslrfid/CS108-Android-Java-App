package com.csl.cslibrary4a;

import android.content.Context;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.Collections;

public class RfidReader {
    final boolean DEBUG = false;
    RfidConnector rfidConnector; public RfidReaderChipR2000 rfidReaderChipR2000; public RfidReaderChipE710 rfidReaderChipE710;
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

    public boolean getInvAlgo1() {
        int iValue;
        if (bis108) iValue = rfidReaderChipR2000.rx000Setting.getInvAlgo();
        else iValue = rfidReaderChipE710.rx000Setting.getInvAlgo();
        if (iValue < 0) {
            return true;
        } else {
            return (iValue != 0 ? true : false);
        }
    }

    public final int FCC_CHN_CNT = 50;
    public final double[] FCCTableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, 905.25, 905.75, 906.25, 906.75, 907.25,//10
            907.75, 908.25, 908.75, 909.25, 909.75, 910.25, 910.75, 911.25, 911.75, 912.25,//20
            912.75, 913.25, 913.75, 914.25, 914.75, 915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    public final double[] FCCTableOfFreq0 = new double[] {
            903.75, 912.25, 907.75, 910.25, 922.75,     923.25, 923.75, 915.25, 909.25, 912.75,
            910.75, 913.75, 909.75, 905.25, 911.75,     902.75, 914.25, 918.25, 926.25, 925.75,
            920.75, 920.25, 907.25, 914.75, 919.75,     922.25, 903.25, 906.25, 905.75, 926.75,
            924.25, 904.75, 925.25, 924.75, 919.25,     916.75, 911.25, 921.25, 908.25, 908.75,
            913.25, 916.25, 904.25, 906.75, 917.75,     921.75, 917.25, 927.25, 918.75, 915.75 };
    public int[] fccFreqSortedIdx0;
    public final double[] FCCTableOfFreq1 = new double[] {
            915.25, 920.75, 909.25, 912.25, 918.25,     920.25, 909.75, 910.25, 919.75, 922.75,
            908.75, 913.75, 903.75, 919.25, 922.25,     907.75, 911.75, 923.75, 916.75, 926.25,
            908.25, 912.75, 924.25, 916.25, 927.25,     907.25, 910.75, 903.25, 917.75, 926.75,
            905.25, 911.25, 924.75, 917.25, 925.75,     906.75, 914.25, 904.75, 918.75, 923.25,
            902.75, 914.75, 905.75, 915.75, 925.25,     906.25, 921.25, 913.25, 921.75, 904.25 };
    public int[] fccFreqSortedIdx1;
    public int[] fccFreqTable = new int[] {
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
    public int[] fccFreqTableIdx;
    public final int[] fccFreqSortedIdx = new int[] {
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
    public final int AUS_CHN_CNT = 10;
    public final double[] AUSTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25, 924.75, 925.25 };
    public final int[] AusFreqTable = new int[] {
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
    public final int[] ausFreqSortedIdx = new int[] {
            0, 3, 6, 8, 1,
            4, 7, 9, 2, 5 };

    public final double[] PRTableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25,
            922.75, 923.25, 923.75, 924.25, 924.75, 925.25, 925.75, 926.25, 926.75, 927.25 };
    private int[] freqTable = null;
    public int[] freqSortedIdx = null;

    public final int VZ_CHN_CNT = 10;
    public final double[] VZTableOfFreq = new double[] {
            922.75, 923.25, 923.75, 924.25, 924.75,
            925.25, 925.75, 926.25, 926.75, 927.25 };
    public final int[] vzFreqTable = new int[] {
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
    public final int[] vzFreqSortedIdx = new int[] {
            6, 0, 9, 5, 1,
            8, 4, 2, 7, 3 };

    public final int BR1_CHN_CNT = 24;
    public final double[] BR1TableOfFreq = new double[] {
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
    public final int[] br1FreqTable = new int[] {
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
    public final int[] br1FreqSortedIdx = new int[] {
            0, 22, 21, 23, 9,
            7, 6, 4, 19, 12,
            13, 3, 5, 1, 18,
            8, 2, 16, 20, 17,
            11, 10, 14, 15 };

    public final int BR2_CHN_CNT = 33;
    public double[] BR2TableOfFreq = new double[] {
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
    public final int[] br2FreqTable = new int[] {
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
    public final int[] br2FreqSortedIdx = new int[] {
            9, 1, 31, 30, 3,
            32, 18, 16, 15, 13,
            5, 4, 28, 21, 8,
            22, 2, 6, 7, 12,
            14, 10, 27, 17, 11,
            25, 29, 26, 20, 19,
            23, 0, 24,
    };

    public final int BR3_CHN_CNT = 9;
    public final double[] BR3TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75 };
    public final int[] br3FreqTable = new int[] {
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
    public final int[] br3FreqSortedIdx = new int[] {
            1, 3, 5, 4, 8,
            2, 6, 7, 0 };

    public final int BR4_CHN_CNT = 4;
    public final double[] BR4TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25 };
    public final int[] br4FreqTable = new int[] {
            0x00180E1D, //903.25 MHz
            0x00180E21, //904.25 MHz
            0x00180E1F, //903.75 MHz
            0x00180E1B, //902.75 MHz
    };
    public final int[] br4FreqSortedIdx = new int[] {
            1, 3, 2, 0 };

    public final int BR5_CHN_CNT = 14;
    public final double[] BR5TableOfFreq = new double[] {
            917.75, 918.25, 918.75, 919.25, 919.75, // 4
            920.25, 920.75, 921.25, 921.75, 922.25, // 9
            922.75, 923.25, 923.75, 924.25 };
    public final int[] br5FreqTable = new int[] {
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
    public final int[] br5FreqSortedIdx = new int[] {
            5, 3, 2, 0, 8,
            9, 1, 4, 12, 13,
            7, 6, 10, 11 };

    public final int HK_CHN_CNT = 8;
    public final double[] HKTableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25, 922.75,
            923.25, 923.75, 924.25 };
    public final int[] hkFreqTable = new int[] {
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
            0x00180E71, //924.25MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E6D, //923.25MHz
            0x00180E6F, //923.75MHz
            0x00180E67, //921.75MHz
    };
    public final int[] hkFreqSortedIdx = new int[] {
            0, 3, 7, 1, 4,
            5, 6, 2 };

    public final int BD_CHN_CNT = 4;
    public final double[] BDTableOfFreq = new double[] {
            925.25, 925.75, 926.25, 926.75 };
    public final int[] bdFreqTable = new int[] {
            0x00180E75, //925.25MHz
            0x00180E77, //925.75MHz
            0x00180E79, //926.25MHz
            0x00180E7B, //926.75MHz
    };
    public final int[] bdFreqSortedIdx = new int[] {
            0, 3, 1, 2  };

    public final int TW_CHN_CNT = 12;
    public final double[] TWTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,
            924.75, 925.25, 925.75, 926.25, 926.75,
            927.25, 927.75 };
    public int[] twFreqTable = new int[] {
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
    public final int[] twFreqSortedIdx = new int[] {
            10, 5, 1, 6, 11,
            4, 8, 2, 9, 0,
            7, 3 };

    public final int MYS_CHN_CNT = 8;
    public final double[] MYSTableOfFreq = new double[] {
            919.75, 920.25, 920.75, 921.25, 921.75,
            922.25, 922.75, 923.25 };
    public final int[] mysFreqTable = new int[] {
            0x00180E5F, //919.75MHz
            0x00180E65, //921.25MHz
            0x00180E6B, //922.75MHz
            0x00180E61, //920.25MHz
            0x00180E67, //921.75MHz
            0x00180E6D, //923.25MHz
            0x00180E63, //920.75MHz
            0x00180E69, //922.25MHz
    };
    public final int[] mysFreqSortedIdx = new int[] {
            0, 3, 6, 1, 4,
            7, 2, 5 };

    public final int ZA_CHN_CNT = 16;
    public final double[] ZATableOfFreq = new double[] {
            915.7, 915.9, 916.1, 916.3, 916.5,
            916.7, 916.9, 917.1, 917.3, 917.5,
            917.7, 917.9, 918.1, 918.3, 918.5,
            918.7 };
    public final int[] zaFreqTable = new int[] {
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
    public final int[] zaFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10, 11, 12, 13, 14,
            15 };

    public final int ID_CHN_CNT = 4;
    public final double[] IDTableOfFreq = new double[] {
            923.25, 923.75, 924.25, 924.75 };
    public final int[] indonesiaFreqTable = new int[] {
            0x00180E6D, //923.25 MHz
            0x00180E6F,//923.75 MHz
            0x00180E71,//924.25 MHz
            0x00180E73,//924.75 MHz
    };
    public final int[] indonesiaFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    public final int IL_CHN_CNT = 7;
    public final double[] ILTableOfFreq = new double[] {
            915.25, 915.5, 915.75, 916.0, 916.25, // 4
            916.5, 916.75 };
    public final int[] ilFreqTable = new int[] {
            0x00180E4D, //915.25 MHz
            0x00180E51, //916.25 MHz
            0x00180E4E, //915.5 MHz
            0x00180E52, //916.5 MHz
            0x00180E4F, //915.75 MHz
            0x00180E53, //916.75 MHz
            0x00180E50, //916.0 MHz
    };
    public final int[] ilFreqSortedIdx = new int[] {
            0, 4, 1, 5, 2,  6, 3 };

    public final int IL2019RW_CHN_CNT = 5;
    public final double[] IL2019RWTableOfFreq = new double[] {
            915.9, 916.025, 916.15, 916.275, 916.4 };
    public final int[] il2019RwFreqTable = new int[] {
            0x003C23C7, //915.9 MHz
            0x003C23C8, //916.025 MHz
            0x003C23C9, //916.15 MHz
            0x003C23CA, //916.275 MHz
            0x003C23CB, //916.4 MHz
    };
    public final int[] il2019RwFreqSortedIdx = new int[] {
            0, 4, 1, 2, 3 };

    public final int PH_CHN_CNT = 8;
    public final double[] PHTableOfFreq = new double[] {
            918.125, 918.375, 918.625, 918.875, 919.125, // 5
            919.375, 919.625, 919.875 };
    public final int[] phFreqTable = new int[] {
            0x00301CB1, //918.125MHz   Channel 0
            0x00301CBB, //919.375MHz   Channel 5
            0x00301CB7, //918.875MHz   Channel 3
            0x00301CBF, //919.875MHz   Channel 7
            0x00301CB3, //918.375MHz   Channel 1
            0x00301CBD, //919.625MHz   Channel 6
            0x00301CB5, //918.625MHz   Channel 2
            0x00301CB9, //919.125MHz   Channel 4
    };
    public final int[] phFreqSortedIdx = new int[] {
            0, 5, 3, 7, 1,  6, 2, 4 };

    public final int NZ_CHN_CNT = 11;
    public final double[] NZTableOfFreq = new double[] {
            922.25, 922.75, 923.25, 923.75, 924.25,// 4
            924.75, 925.25, 925.75, 926.25, 926.75,// 9
            927.25 };
    public final int[] nzFreqTable = new int[] {
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
    public final int[] nzFreqSortedIdx = new int[] {
            4, 7, 0, 9, 2,  10, 6, 1, 8, 3,     5 };

    public final int CN_CHN_CNT = 16;
    public final double[] CHNTableOfFreq = new double[] {
            920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375, 922.625, 922.875,
            923.125, 923.375, 923.625, 923.875, 924.125, 924.375 };
    public final int[] cnFreqTable = new int[] {
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
    public final int[] cnFreqSortedIdx = new int[] {
            7, 6, 4, 0, 10,
            14, 3, 1, 9, 8,
            2, 13, 12, 11, 5,
            15 };

    public final int UH1_CHN_CNT = 10;
    public final double[] UH1TableOfFreq = new double[] {
            915.25, 915.75, 916.25, 916.75, 917.25,
            917.75, 918.25, 918.75, 919.25, 919.75 };
    public final int[] uh1FreqTable = new int[] {
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
    public final int[] uh1FreqSortedIdx = new int[] {
            1, 0, 8, 7, 5,
            4, 6, 2, 9, 3 };

    public final int UH2_CHN_CNT = 15;
    public final double[] UH2TableOfFreq = new double[] {
            920.25, 920.75, 921.25, 921.75, 922.25,   // 4
            922.75, 923.25, 923.75, 924.25, 924.75,   // 9
            925.25, 925.75, 926.25, 926.75, 927.25 };
    public final int[] uh2FreqTable = new int[] {
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
    public final int[] uh2FreqSortedIdx = new int[]{
            13, 12, 14, 0, 10,
            3, 4, 9, 7, 11,
            8, 2, 1, 5, 6, };

    public final int LH_CHN_CNT = 26;
    public double[] LHTableOfFreq = new double[] {
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
    public final int[] lhFreqTable = new int[] {
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
    public final int[] lhFreqSortedIdx = new int[] {
            0, 13, 1, 14, 2,
            15, 3, 16, 4, 17,
            5, 18, 6, 19, 7,
            20, 8, 21, 9, 22,
            10, 23, 11, 24, 12,
            25 };

    public final int LH1_CHN_CNT = 14;
    public double[] LH1TableOfFreq = new double[] {
            902.75, 903.25, 903.75, 904.25, 904.75, // 4
            905.25, 905.75, 906.25, 906.75, 907.25, // 9
            907.75, 908.25, 908.75, 909.25, // 13
    };
    public final int[] lh1FreqTable = new int[] {
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
    public final int[] lh1FreqSortedIdx = new int[] {
            0, 13, 1, 2, 3,
            4, 5, 6, 7, 8,
            9, 10, 11, 12 };

    public final int LH2_CHN_CNT = 11;
    public double[] LH2TableOfFreq = new double[] {
            909.75, 910.25, 910.75, 911.25, 911.75, // 4
            912.25, 912.75, 913.25, 913.75, 914.25, // 9
            914.75 };
    public final int[] lh2FreqTable = new int[] {
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
    public final int[] lh2FreqSortedIdx = new int[] {
            0, 1, 2, 3, 4,
            5, 6, 7, 8, 9,
            10 };

    public final int ETSI_CHN_CNT = 4;
    public final double[] ETSITableOfFreq = new double[] {
            865.70, 866.30, 866.90, 867.50 };
    public final int[] etsiFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    public final int[] etsiFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    public final int IDA_CHN_CNT = 3;
    public final double[] IDATableOfFreq = new double[] {
            865.70, 866.30, 866.90 };
    public final int[] indiaFreqTable = new int[] {
            0x003C21D1, //865.700MHz
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
    };
    public final int[] indiaFreqSortedIdx = new int[] {
            0, 1, 2 };

    public final int KR_CHN_CNT = 19;
    public final double[] KRTableOfFreq = new double[] {
            910.20, 910.40, 910.60, 910.80, 911.00, 911.20, 911.40, 911.60, 911.80, 912.00,
            912.20, 912.40, 912.60, 912.80, 913.00, 913.20, 913.40, 913.60, 913.80 };
    public int[] krFreqTable = new int[] {
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
    public final int[] krFreqSortedIdx = new int[] {
            13, 9, 15, 8, 11,
            18, 2, 17, 1, 7,
            4, 10, 0, 12, 5,
            3, 16, 6, 14 };

    public final int KR2017RW_CHN_CNT = 6;
    public final double[] KR2017RwTableOfFreq = new double[] {
            917.30, 917.90, 918.50, 919.10, 919.70, 920.30 };
    public int[] kr2017RwFreqTable = new int[] {
            0x003C23D5, // 917.3 -> 917.25  MHz Channel 1
            0x003C23DB, //917.9 -> 918 MHz Channel 2
            0x003C23E1, //918.5 MHz Channel 3
            0x003C23E7, //919.1 -> 919  MHz Channel 4
            0x003C23ED, //919.7 -> 919.75 MHz Channel 5
            0x003C23F3 // 920.3 -> 920.25 MHz Channel 6
        };
    public final int[] kr2017RwFreqSortedIdx = new int[] {
            3, 0, 5, 1, 4, 2 };

    public final int JPN2012_CHN_CNT = 4;
    public final double[] JPN2012TableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40 };
    public final int[] jpn2012FreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            //0x003C23F6, //920.600MHz   Channel 5
            //0x003C23F8, //920.800MHz   Channel 6
    };
    public final int[] jpn2012FreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    public final int JPN2012A_CHN_CNT = 6;
    public final double[] JPN2012ATableOfFreq = new double[] {
            916.80, 918.00, 919.20, 920.40, 920.60, 920.80 };
    public final int[] jpn2012AFreqTable = new int[] {
            0x003C23D0, //916.800MHz   Channel 1
            0x003C23DC, //918.000MHz   Channel 2
            0x003C23E8, //919.200MHz   Channel 3
            0x003C23F4, //920.400MHz   Channel 4
            0x003C23F6, //920.600MHz   Channel 5
            0x003C23F8, //920.800MHz   Channel 6
    };
    public final int[] jpn2012AFreqSortedIdx = new int[] {
            0, 1, 2, 3, 4, 5 };

    public final int ETSIUPPERBAND_CHN_CNT = 4;
    public final double[] ETSIUPPERBANDTableOfFreq = new double[] {
            916.3, 917.5, 918.7, 919.9 };
    public final int[] etsiupperbandFreqTable = new int[] {
            0x003C23CB, //916.3 MHz
            0x003C23D7, //917.5 MHz
            0x003C23E3, //918.7 MHz
            0x003C23EF, //919.9 MHz
        };
    public final int[] etsiupperbandFreqSortedIdx = new int[] {
            0, 1, 2, 3 };

    public final int VN1_CHN_CNT = 3;
    public final double[] VN1TableOfFreq = new double[] {
            866.30, 866.90, 867.50 };
    public final int[] vietnam1FreqTable = new int[] {
            0x003C21D7, //866.300MHz
            0x003C21DD, //866.900MHz
            0x003C21E3, //867.500MHz
        };
    public final int[] vietnam1FreqSortedIdx = new int[] {
            0, 1, 2 };

    public final int VN2_CHN_CNT = 8;
    public final double[] VN2TableOfFreq = new double[] {
            918.75, 919.25, 919.75, 920.25, 920.75, 921.25, 921.75, 922.25 };
    public final int[] vietnam2FreqTable = new int[] {
            0x00180E61, //920.25 MHz
            0x00180E5D, //919.25 MHz
            0x00180E5B, //918.75 MHz
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E5F, //919.75 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    public final int[] vietnam2FreqSortedIdx = new int[] {
            3, 1, 0, 6, 7, 2, 5, 4 };

    public final int VN3_CHN_CNT = 4;
    public final double[] VN3TableOfFreq = new double[] {
            920.75, 921.25, 921.75, 922.25 };
    public final int[] vietnam3FreqTable = new int[] {
            0x00180E67, //921.75 MHz
            0x00180E69, //922.25 MHz
            0x00180E65, //921.25 MHz
            0x00180E63, //920.75 MHz
        };
    public final int[] vietnam3FreqSortedIdx = new int[] {
            2, 3, 1, 0 };
    boolean setChannelData(RegionCodes regionCode) {
        return true;
    }
/*
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
    public boolean starAuthOperation() {
        if (bis108) {
            rfidReaderChipR2000.setPwrManagementMode(false);
            return rfidReaderChipR2000.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE);
        }
        rfidReaderChipE710.setPwrManagementMode(false);
        return rfidReaderChipE710.sendHostRegRequestHST_CMD(RfidReaderChipData.HostCommands.CMD_18K6CAUTHENTICATE);
    }
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
    int getFreqModifyCode() {
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
            String strFreqChnCnt = csReaderConnector.rfidReader.strCountryEnumInfo[(iValue - 1) * csReaderConnector.rfidReader.iCountryEnumInfoColumn + 3];
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
