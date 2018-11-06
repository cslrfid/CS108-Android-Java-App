package com.csl.cs108ademoapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.csl.cs108library4a.ReaderDevice;

import java.io.IOException;
import java.util.ArrayList;

public class CustomMediaPlayer {
    Context context;
    MediaPlayer player; boolean starting = false;

    public CustomMediaPlayer(Context context, String file) {
        this.context = context;
        player = null;
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(file);
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    starting = false;
                    MainActivity.mCs108Library4a.appendToLog("MediaPlayer is completed.");
                }
            });
        } catch (IOException e) {
            MainActivity.mCs108Library4a.appendToLog("mp3 setup FAIL");
        }
    }

    public void start() {
        player.start();
        if (false) starting = true;
    }
    public boolean isPlaying() {
        return (player.isPlaying() | starting) ;
    }
    public void pause() {
        player.pause();
    }
    void setVolume(int volume1, int volume2) {
        player.setVolume(volume1, volume2);
    }
}
