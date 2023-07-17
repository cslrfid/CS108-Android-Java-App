package com.csl.cs108ademoapp.fragments;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Test1Fragment extends CommonFragment {
    Button startbtn, stopbtn, playbtn, stopplay;
    MediaRecorder mRecorder;
    MediaPlayer mediaPlayer;
    static final String LOG_TAG = "AudioRecording";
    static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    Handler handler = new Handler();

    TextView seekBarHint;
    SeekBar seekBar;
    boolean wasPlaying = false;

    File file;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.fragment_test1, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        androidx.appcompat.app.ActionBar actionBar;
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_access);
        actionBar.setTitle("Test");

        startbtn = (Button) getActivity().findViewById(R.id.btnRecord);
        stopbtn = (Button) getActivity().findViewById(R.id.btnStop);
        playbtn = (Button) getActivity().findViewById(R.id.btnPlay);
        stopplay = (Button) getActivity().findViewById(R.id.btnStopPlay);
        stopbtn.setEnabled(false);
        playbtn.setEnabled(false);
        stopplay.setEnabled(false);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/Download/cs710Java/";

        seekBarHint = (TextView) getActivity().findViewById(R.id.textView);
        seekBar = (SeekBar) getActivity().findViewById(R.id.seekbar);

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermissions() == false) RequestPermissions();
                else {
                    startbtn.setEnabled(false);
                    stopbtn.setEnabled(true);
                    playbtn.setEnabled(false);
                    stopplay.setEnabled(false);

                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mFileName += "audio";
                    mFileName += new SimpleDateFormat("_yyMMdd_HHmmss").format(new java.util.Date());
                    Log.i("Hello2", "mFileName is " + mFileName );
                    mRecorder.setOutputFile(mFileName + ".3gp");
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    mRecorder.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();

                    file = new File(getActivity().getFilesDir(), mFileName + ".txt");
                    try {
                        FileOutputStream stream = new FileOutputStream(file);
                        stream.write("Start of data\n".getBytes());
                        String outData = "preFilterData.maskbit";
                        stream.write(outData.getBytes());
                        stream.write("End of data\n".getBytes());
                        stream.close();
                    } catch (Exception ex) {
                        Log.i("Hello2", "Exception is " + ex.toString());
                    }

                    MainActivity.mSensorConnector.mLocationDevice.turnOn(true);
                    MainActivity.mSensorConnector.mSensorDevice.turnOn(true);
                    handler.post(runnableRecord);
                }
            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startbtn.setEnabled(true);
                stopbtn.setEnabled(false);
                playbtn.setEnabled(true);
                stopplay.setEnabled(true);
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                Toast.makeText(getActivity().getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();

                MainActivity.mSensorConnector.mLocationDevice.turnOn(false);
                MainActivity.mSensorConnector.mSensorDevice.turnOn(false);
                handler.removeCallbacks(runnableRecord);
            }
        });

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startbtn.setEnabled(true);
                stopbtn.setEnabled(false);
                playbtn.setEnabled(false);
                stopplay.setEnabled(true);

                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            clearMediaPlayer();
                            wasPlaying = true;
                        }
                    } catch (Exception ex) {
                        Log.i("Hello2", "Exception is " + ex.toString());
                    }
                }
                if (!wasPlaying) {
                    if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(mFileName);
                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);
                        seekBar.setMax(mediaPlayer.getDuration());

                        mediaPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Recording Started Playing", Toast.LENGTH_LONG).show();
                        handler.removeCallbacks(runnablePlay);
                        handler.post(runnablePlay);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                }
            }
        });

        stopplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMediaPlayer();
                Toast.makeText(getActivity().getApplicationContext(),"Playing Audio Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress / 1000f);
                if (x != 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    void clearMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        seekBar.setProgress(0);
        startbtn.setEnabled(true);
        stopbtn.setEnabled(false);
        playbtn.setEnabled(true);
        stopplay.setEnabled(false);
        mediaPlayer = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), RECORD_AUDIO);
        Log.i("Hello2", "result = " + result + ", result1 = " + result1);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    void RequestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    final Runnable runnableRecord = new Runnable() {
        boolean DEBUG = false;
        @Override
        public void run() {
            String stringLocation = MainActivity.mSensorConnector.mLocationDevice.getLocation();
            String stringCompass = MainActivity.mSensorConnector.mSensorDevice.getEcompass();
            Log.i("Hello2", "stringLocation = " + stringLocation + ", stringCompass = " + stringCompass);
            handler.postDelayed(runnableRecord, 1000);
        }
    };

    final Runnable runnablePlay = new Runnable() {
        boolean DEBUG = false;
        @Override
        public void run() {
            boolean bPlaying = false;
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int total = mediaPlayer.getDuration();
                    if (currentPosition < total) {
                        seekBar.setProgress(currentPosition);
                        bPlaying = true;
                    }
                } else clearMediaPlayer();
            } catch (Exception ex) {
                Log.i("Hello2", "Runnable Exception: " + ex.toString());
            }
            if (bPlaying) handler.postDelayed(runnablePlay, 1000);
            else clearMediaPlayer();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Test1Fragment() {
        super("Test1Fragment");
    }

}
