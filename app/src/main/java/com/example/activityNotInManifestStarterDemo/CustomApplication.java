package com.example.activityNotInManifestStarterDemo;

import android.app.Application;

import com.example.activityNotInManifestStarterDemo.utils.ActivityStartHook;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActivityStartHook.init(StubActivity.class);
    }
}
