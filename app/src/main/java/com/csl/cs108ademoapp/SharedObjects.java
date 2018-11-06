package com.csl.cs108ademoapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.csl.cs108library4a.ReaderDevice;

import java.io.IOException;
import java.util.ArrayList;

public class SharedObjects {
    public static class TagsIndex implements Comparable<TagsIndex> {
        private String address;
        private int position;

        @Keep
        public TagsIndex(String address, int position) {
            this.address = address;
            this.position = position;
        }

        @Keep public String getAddress() {
            return address;
        }
        @Keep public int getPosition() {
            return position;
        }

        @Override
        public int compareTo(@NonNull TagsIndex tagsIndex) {
            return address.compareTo(tagsIndex.address);
        }
    }

    Context context;

    public ArrayList<ReaderDevice> readersList = new ArrayList<ReaderDevice>();
    public String connectedBleAddressOld = "";
    public boolean versioinWarningShown = false;
    public int batteryWarningShown = 0;

    public boolean runningInventoryRfidTask = false;
    public ArrayList<ReaderDevice> tagsList = new ArrayList<>();
    public ArrayList<TagsIndex> tagsIndexList = new ArrayList<>();
    public ArrayList<String> serviceArrayList = new ArrayList<String>();

    public boolean runningInventoryBarcodeTask = false;
    public ArrayList<ReaderDevice> barsList = new ArrayList<>();

    public CustomMediaPlayer playerO, playerN, playerL;

    public SharedObjects(Context context) {
        this.context = context;
        playerO = new CustomMediaPlayer(context, "beeplow.mp3");
        playerN = new CustomMediaPlayer(context, "beephigh.mp3");
        playerL = new CustomMediaPlayer(context, "beep3s1khz.wav");
    }
}
