package com.rnd.utilities;

import androidx.multidex.MultiDexApplication;

public class MainApplication extends MultiDexApplication {



    @Override
    public void onCreate() {
        super.onCreate();
        com.rnd.utilities.FontsOverride.setDefaultFont(this, "SERIF", "erasdemi.TTF");

    }



}
