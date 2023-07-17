package com.csl.cs108ademoapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class CustomMediaPlayer {
    final boolean DEBUG = false;
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
                    if (DEBUG) MainActivity.csLibrary4A.appendToLog("MediaPlayer is completed.");
                }
            });
        } catch (IOException e) {
            MainActivity.csLibrary4A.appendToLog("mp3 setup FAIL");
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
        if (false) player.setVolume(volume1, volume2);
        else {
            AudioManager audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
            int iVolumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            MainActivity.csLibrary4A.appendToLog("Hello8: currentVolume = " + currentVolume);
            if (currentVolume > 0) {
                int volume12 = volume1 + volume2;
                volume12 = ( volume12 * iVolumeMax ) / 600;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, iVolumeMax, 0);
            }
        }
    }
}
