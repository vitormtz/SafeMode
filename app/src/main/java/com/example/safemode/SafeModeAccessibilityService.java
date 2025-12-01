package com.example.safemode;

import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class SafeModeAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private AppPreferences preferences;
    private LocationManager locationManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        try {
            preferences = new AppPreferences(this);
            locationManager = new LocationManager(this);

        } catch (Exception e) {
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                return;
            }

            String packageName = null;
            if (event.getPackageName() != null) {
                packageName = event.getPackageName().toString();
            }

            if (packageName == null || packageName.isEmpty()) {
                return;
            }

            if (!preferences.isSafeModeEnabled()) {
                return;
            }

            if (packageName.equals(getPackageName())) {
                return;
            }

            if (isSystemApp(packageName)) {
                return;
            }

            boolean isHideModeActive = preferences.isHideModeActive();

            if (isHideModeActive) {
                if (preferences.isAppHidden(packageName)) {
                    blockAppWithActivity(packageName);
                    return;
                }
            }

            if (!preferences.isAppBlocked(packageName)) {
                return;
            }

            if (shouldBlockBasedOnLocation()) {
                blockAppWithActivity(packageName);
            }

        } catch (Exception e) {
        }
    }

    private boolean shouldBlockBasedOnLocation() {

        if (!preferences.isLocationEnabled()) {
            return true;
        }

        double allowedLat = preferences.getAllowedLatitude();
        double allowedLng = preferences.getAllowedLongitude();
        int allowedRadius = preferences.getAllowedRadius();

        if (allowedLat == 0.0 && allowedLng == 0.0) {
            return true;
        }

        android.location.Location currentLoc = getCurrentLocationWithTimeout();

        if (currentLoc == null) {
            return true;
        }

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                currentLoc.getLatitude(),
                currentLoc.getLongitude(),
                allowedLat,
                allowedLng,
                results
        );

        float distance = results[0];
        boolean isOutside = distance > allowedRadius;

        return isOutside;
    }

    private android.location.Location getCurrentLocationWithTimeout() {

        try {
            android.location.Location currentLoc = locationManager.getCurrentLocation();

            if (currentLoc != null) {
                long locationAge = System.currentTimeMillis() - currentLoc.getTime();

                if (locationAge > 300000) {
                    locationManager.getLocationOnce();

                    try {
                        Thread.sleep(3000);
                        android.location.Location newLoc = locationManager.getCurrentLocation();
                        if (newLoc != null && newLoc.getTime() > currentLoc.getTime()) {
                            return newLoc;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                return currentLoc;
            }

            locationManager.getLocationOnce();

            for (int i = 0; i < 8; i++) {
                try {
                    Thread.sleep(1000);
                    currentLoc = locationManager.getCurrentLocation();
                    if (currentLoc != null) {
                        return currentLoc;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private void blockAppWithActivity(String packageName) {

        try {
            Intent blockIntent = new Intent(this, SimpleBlockActivity.class);
            blockIntent.putExtra("blocked_package", packageName);
            blockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            startActivity(blockIntent);

            startBlockVerification(packageName);

            logBlockedApp(packageName);
        } catch (Exception e) {
        }
    }

    private boolean isSystemApp(String packageName) {
        String[] criticalSystemApps = {
                "com.android.systemui",
                "android",
                "com.android.phone",
                "com.android.settings",
                "com.android.launcher",
                "com.android.dialer",
                "com.google.android.gms",
                "com.android.packageinstaller",
                "com.android.launcher3",
                "com.sec.android.app.launcher",
                "com.android.emergency",
                "com.android.incallui",
                "com.example.safemode"
        };

        for (String criticalApp : criticalSystemApps) {
            if (packageName.equals(criticalApp) || packageName.startsWith(criticalApp + ".")) {
                return true;
            }
        }

        return false;
    }

    private void startBlockVerification(String packageName) {
        try {
            Intent verificationIntent = new Intent(this, BlockVerificationService.class);
            verificationIntent.putExtra("target_package", packageName);
            startService(verificationIntent);
        } catch (Exception e) {
        }
    }

    private void logBlockedApp(String packageName) {
        try {
            BlockLogger logger = new BlockLogger(this);
            logger.logBlock(packageName, System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}