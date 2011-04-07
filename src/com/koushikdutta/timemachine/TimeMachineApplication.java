package com.koushikdutta.timemachine;

import android.app.Application;

public class TimeMachineApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        AssetExtractor.unzipAssets(this);
    }
}
