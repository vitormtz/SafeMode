package com.example.safemode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class LockScreenService extends Service {

    private static final String CHANNEL_ID = "LockScreenChannel";
    private static final int NOTIFICATION_ID = 2;

    private ScreenReceiver screenReceiver;
    private AppPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new AppPreferences(this);

        screenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(screenReceiver, filter);

        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        createNotificationChannel();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Safe Mode - Bloqueio de Tela")
                .setContentText("Proteção ativa")
                .setSmallIcon(R.drawable.ic_safe_mode)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bloqueio de Tela",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Serviço de bloqueio de tela do Safe Mode");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }

            if (!preferences.isLockScreenEnabled()) {
                return;
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                showLockScreen();
            }
        }

        private void showLockScreen() {
            Intent lockIntent = new Intent(LockScreenService.this, LockScreenActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                              Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                              Intent.FLAG_ACTIVITY_NO_ANIMATION |
                              Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(lockIntent);
        }
    }
}
