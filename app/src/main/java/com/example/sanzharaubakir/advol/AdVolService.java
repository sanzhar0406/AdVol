package com.example.sanzharaubakir.advol;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by sanzharaubakir on 16.06.17.
 */

public class AdVolService extends Service {
    private static final String TAG = "AdVolService";
    private static AudioManager audioManager;
    private MediaRecorder mRecorder;
    private static final int second = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        WifiBroadcastReceiver broadcastReceiver = new WifiBroadcastReceiver(getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);

        return super.onStartCommand(intent, flags, startId);
    }

}
