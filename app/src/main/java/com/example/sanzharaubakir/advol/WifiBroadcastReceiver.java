package com.example.sanzharaubakir.advol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by sanzharaubakir on 05.06.17.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private static AudioManager audioManager;
    private MediaRecorder mRecorder;
    private static final int second = 1000;
    private Context context;
    private static final int lowNoiseLevel = 1500;
    private static final int highNoiseLevel = 4000;
    public WifiBroadcastReceiver ()
    {

    }
    public WifiBroadcastReceiver (Context context)
    {
        this.context = context;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION .equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (SupplicantState.isValidState(state)
                    && state == SupplicantState.COMPLETED) {

                boolean connected = checkConnectedToDesiredWifi();
                int dbLevel = 2000;
                if (connected)
                    dbLevel = recordAudio();
                if (dbLevel > highNoiseLevel)
                {
                    for (int i = 0; i < 10; i++)
                        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                    Toast.makeText(context, "Raising the volume", Toast.LENGTH_LONG).show();
                }
                else if (dbLevel < lowNoiseLevel)
                {
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                    Toast.makeText(context, "Muting the volume", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(context, "Normal noise level, leaving the volume at same level", Toast.LENGTH_LONG).show();
                }


            }
        }
    }

     public boolean checkConnectedToDesiredWifi() {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            DBConnection dbConnection = new DBConnection(context);
            String myNetwork = dbConnection.getMyNetowrk();
            String ssid = wifi.getSSID();
            if (!myNetwork.equals(ssid))
            {
                dbConnection.clearNetworkTable();
                dbConnection.setMyCurrentNetwork(ssid);
                //if (ssid.equals(R.string.home_network))
                    //return false;
                return true;
            }

        }
        return false;
    }
    private int recordAudio() {
        int sum = 0;
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
            int count = 10;
            while (count != 0)
            {
                sum += mRecorder.getMaxAmplitude();
                Thread.sleep(second);
                count--;
            }
            sum /= 10;
            //Toast.makeText(context, "sum - " + sum, Toast.LENGTH_LONG).show();

            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sum;
    }
}
