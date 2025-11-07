package com.example.safemode;

import android.graphics.drawable.Drawable;

public class LauncherAppInfo {
    public String packageName;
    public String appName;
    public String activityName;
    public Drawable icon;

    public LauncherAppInfo() {
    }

    public LauncherAppInfo(String packageName, String appName, String activityName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.activityName = activityName;
        this.icon = icon;
    }
}
