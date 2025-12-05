package com.csl.cslibrary4a;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class CustomMediaPlayer {
    final boolean DEBUG = false;
    Context context;
    CsLibrary4A csLibrary4A;
    MediaPlayer player; boolean starting = false;

    public CustomMediaPlayer(Context context, CsLibrary4A csLibrary4A, String file) {
        this.context = context;
        this.csLibrary4A = csLibrary4A;
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
                    if (DEBUG) csLibrary4A.appendToLog("MediaPlayer is completed.");
                }
            });
        } catch (IOException e) {
            csLibrary4A.appendToLog("mp3 setup FAIL");
        }
    }

    public void start() {
        player.start();
        if (false) starting = true;
    }
    public boolean isPlaying() {
        return (player.isPlaying()); // | starting) ;
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
            csLibrary4A.appendToLog("Hello8: currentVolume = " + currentVolume);
            if (currentVolume > 0) {
                int volume12 = volume1 + volume2;
                volume12 = ( volume12 * iVolumeMax ) / 600;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, iVolumeMax, 0);
            }
        }
    }
}
