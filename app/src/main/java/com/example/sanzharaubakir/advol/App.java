package com.example.sanzharaubakir.advol;

import android.app.Application;
import android.content.Intent;

/**
 * Created by sanzharaubakir on 16.06.17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, AdVolService.class));
    }
}
