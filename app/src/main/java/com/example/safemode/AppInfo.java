package com.example.safemode;

import android.graphics.drawable.Drawable;

public class AppInfo {

    public String packageName;
    public String appName;
    public Drawable icon;
    public boolean isBlocked;
    public boolean isSystemApp;

    public AppInfo() {
    }

    public AppInfo(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isBlocked = false;
        this.isSystemApp = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AppInfo appInfo = (AppInfo) obj;
        return packageName != null ? packageName.equals(appInfo.packageName) : appInfo.packageName == null;
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", isBlocked=" + isBlocked +
                '}';
    }
}