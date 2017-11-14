package com.example.sanzharaubakir.advol;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static Button plus, minus, checkNoiseLevel, checkService, launchService;
    private static AudioManager audioManager;
    private MediaRecorder mRecorder;
    private static final int second = 1000;
    private Boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);


        // Request Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_WIFI_STATE},
                    1);
        }
        else
            permissionGranted = true;

        // Adjust volume rate
        minus = (Button) findViewById(R.id.minus);
        plus = (Button) findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                //audioManager.adjustStreamVolume(AudioManager.FLAG_PLAY_SOUND, audioManager.getStreamMaxVolume(AudioManager.FLAG_PLAY_SOUND), 0);
                for (int i = 0; i < 10; i++)
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                //audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        });


        //Check environment noise level
        checkNoiseLevel = (Button) findViewById(R.id.checkVolume);
        checkNoiseLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "permissionGranted - " + permissionGranted, Toast.LENGTH_SHORT).show();
                if (permissionGranted)
                    recordAudio();
            }
        });

        //Check my network
        WifiBroadcastReceiver broadcastReceiver = new WifiBroadcastReceiver(getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);

        // check for Wi-Fi state changes if wi-fi is changed then check the environment noise
        if (broadcastReceiver.checkConnectedToDesiredWifi())
        {
            recordAudio();
            // if environment is noisy increase volume
            // else decrease volume
        }
        checkService = (Button) findViewById(R.id.checkService);
        checkService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "" + isMyServiceRunning(AdVolService.class), Toast.LENGTH_LONG).show();
            }
        });
        launchService = (Button) findViewById(R.id.launchService);
        launchService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMyServiceRunning(AdVolService.class))
                    stopService(new Intent(getApplicationContext(), AdVolService.class));
                else
                    startService(new Intent(getApplicationContext(), AdVolService.class));
            }
        });

        TextView tv = (TextView) findViewById(R.id.network);
        DBConnection dbConnection = new DBConnection(getApplicationContext());
        String myNetwork = dbConnection.getMyNetowrk();
        tv.setTextColor(Color.RED);
        tv.setText(myNetwork);
        //Toast.makeText(getApplicationContext(), "my network - " + broadcastReceiver.checkConnectedToDesiredWifi(), Toast.LENGTH_LONG).show();

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void recordAudio() {
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
            int sum = 0, count = 10;
            while (count != 0)
            {
                sum += mRecorder.getMaxAmplitude();
                Thread.sleep(second);
                count--;
            }
            sum /= 10;
            Toast.makeText(getApplicationContext(), "sum - " + sum, Toast.LENGTH_LONG).show();

            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_WIFI_STATE},
                        1);
            }
            //Log.d(TAG, "permission denied");
            // permission denied
        }

    }
}
