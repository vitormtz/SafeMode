package com.example.safemode;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class AppPreferences {

    private static final String PREF_NAME = "SafeModePrefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";
    private static final String KEY_SAFE_MODE_ENABLED = "safe_mode_enabled";
    private static final String KEY_ALLOWED_LATITUDE = "allowed_latitude";
    private static final String KEY_ALLOWED_LONGITUDE = "allowed_longitude";
    private static final String KEY_ALLOWED_RADIUS = "allowed_radius";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_LOCK_SCREEN_ENABLED = "lock_screen_enabled";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";
    private static final String KEY_HIDE_MODE_ACTIVE = "hide_mode_active";

    private SharedPreferences preferences;

    public AppPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

     public void setBlockedApps(Set<String> blockedApps) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(KEY_BLOCKED_APPS);
        editor.apply();

        Set<String> cleanSet = new HashSet<>();
        for (String app : blockedApps) {
            String cleanApp = app.trim();
            cleanSet.add(cleanApp);
        }

        editor.putStringSet(KEY_BLOCKED_APPS, cleanSet);
        editor.apply();
    }

    public Set<String> getBlockedApps() {
        Set<String> originalSet = preferences.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        Set<String> copySet = new HashSet<>(originalSet);
        return copySet;
    }

    public boolean isAppBlocked(String packageName) {

        String cleanPackageName = packageName.trim();
        Set<String> blockedApps = getBlockedApps();

        boolean found = false;
        for (String blockedApp : blockedApps) {
            if (cleanPackageName.equals(blockedApp)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public void addBlockedApp(String packageName) {

        String cleanPackageName = packageName.trim();
        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(cleanPackageName);
        setBlockedApps(blockedApps);
    }

    public void removeBlockedApp(String packageName) {
        String cleanPackageName = packageName.trim();
        Set<String> blockedApps = getBlockedApps();
        blockedApps.remove(cleanPackageName);
        setBlockedApps(blockedApps);
    }

    public void setSafeModeEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SAFE_MODE_ENABLED, enabled);
        editor.apply();
    }

    public boolean isSafeModeEnabled() {
        boolean enabled = preferences.getBoolean(KEY_SAFE_MODE_ENABLED, false);
        return enabled;
    }

    public double getAllowedLatitude() {
        try {
            String latString = preferences.getString(KEY_ALLOWED_LATITUDE, "0.0");
            double value = Double.parseDouble(latString);
            return value;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getAllowedLongitude() {
        try {
            String lngString = preferences.getString(KEY_ALLOWED_LONGITUDE, "0.0");
            double value = Double.parseDouble(lngString);
            return value;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public int getAllowedRadius() {
        int value = preferences.getInt(KEY_ALLOWED_RADIUS, 100);
        return value;
    }

    public void setLocationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOCATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isLocationEnabled() {
        boolean value = preferences.getBoolean(KEY_LOCATION_ENABLED, false);
        return value;
    }

    public void setAllowedLocation(double latitude, double longitude, int radiusInMeters) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_ALLOWED_LATITUDE, String.valueOf(latitude));
            editor.putString(KEY_ALLOWED_LONGITUDE, String.valueOf(longitude));
            editor.putInt(KEY_ALLOWED_RADIUS, radiusInMeters);
            editor.putBoolean(KEY_LOCATION_ENABLED, true);

            editor.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLockScreenEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOCK_SCREEN_ENABLED, enabled);
        editor.apply();
    }

    public boolean isLockScreenEnabled() {
        return preferences.getBoolean(KEY_LOCK_SCREEN_ENABLED, false);
    }

    public void setHiddenApps(Set<String> hiddenApps) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_HIDDEN_APPS);
        editor.apply();

        Set<String> cleanSet = new HashSet<>();
        for (String app : hiddenApps) {
            String cleanApp = app.trim();
            cleanSet.add(cleanApp);
        }

        editor.putStringSet(KEY_HIDDEN_APPS, cleanSet);
        editor.apply();
    }

    public Set<String> getHiddenApps() {
        Set<String> originalSet = preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>());
        Set<String> copySet = new HashSet<>(originalSet);
        return copySet;
    }

    public boolean isAppHidden(String packageName) {
        String cleanPackageName = packageName.trim();
        Set<String> hiddenApps = getHiddenApps();
        return hiddenApps.contains(cleanPackageName);
    }

    public void setHideModeActive(boolean active) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_HIDE_MODE_ACTIVE, active);
        editor.apply();
    }

    public boolean isHideModeActive() {
        boolean active = preferences.getBoolean(KEY_HIDE_MODE_ACTIVE, false);
        return active;
    }
}