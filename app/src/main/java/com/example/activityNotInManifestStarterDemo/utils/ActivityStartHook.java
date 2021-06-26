package com.example.activityNotInManifestStarterDemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

public class ActivityStartHook {

    private static Class<? extends Activity> stubActivity = com.example.activityNotInManifestStarterDemo.StubActivity.class;
    private static final String hideKey = "realStartActivity";

    public static void init(Class<? extends Activity> stubActivity) {
        ActivityStartHook.stubActivity = stubActivity;
        hookActivityThread();
    }

    public static Intent buildWrapIntent(Context context, Intent intent) {
        Intent resIntent = new Intent(context, stubActivity);
        resIntent.putExtra(hideKey, intent);
        return resIntent;
    }

    private static void hookActivityThread() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Field threadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            threadField.setAccessible(true);
            Object sCurrentActivityThread = threadField.get(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            final Object mH = mHField.get(sCurrentActivityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, (Handler.Callback)(Message msg) -> {
                if (msg.what == 100) {
                    try {
                        Field intentField = msg.obj.getClass().getDeclaredField("intent");
                        intentField.setAccessible(true);
                        Intent intent = (Intent) intentField.get(msg.obj);
                        if (intent != null) {
                            Intent raw = intent.getParcelableExtra(hideKey);
                            if (raw != null) {
                                intentField.set(msg.obj, raw);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Activity Hook", "recover intent fail", e);
                    }
                } else if (msg.what == 159) { // Android Pie
                    try {
                        Field activityCallbacksFile = msg.obj.getClass().getDeclaredField("mActivityCallbacks");
                        activityCallbacksFile.setAccessible(true);
                        Object callbacks = activityCallbacksFile.get(msg.obj);
                        if (callbacks != null) {
                            for (Object launchActivityItem: (List<Object>) callbacks) {
                                Field intentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                intentField.setAccessible(true);
                                Intent intent = (Intent) intentField.get(launchActivityItem);
                                if (intent != null) {
                                    Intent raw = intent.getParcelableExtra(hideKey);
                                    if (raw != null) {
                                        intentField.set(launchActivityItem, raw);
                                    }
                                }
                            }
                        }

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Log.e("Activity Hook", "recover intent fail", e);
                    }
                }
                return false;
            });

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.e("Activity Hook", "add hook fail", e);
        }
    }
}
