package com.example.safemode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            AppPreferences preferences = new AppPreferences(context);

            if (preferences.isSafeModeEnabled()) {

                Intent serviceIntent = new Intent(context, SafeModeService.class);

                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }

                } catch (Exception e) {
                }
            }

            if (preferences.isLockScreenEnabled()) {
                Intent lockServiceIntent = new Intent(context, LockScreenService.class);

                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(lockServiceIntent);
                    } else {
                        context.startService(lockServiceIntent);
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}
