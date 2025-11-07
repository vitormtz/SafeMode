package com.example.safemode;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class LockScreenActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable checkForegroundTask;
    private EditText pin1, pin2, pin3, pin4;
    private TextView tvError;
    private PinManager pinManager;
    private boolean isScreenOn = true;
    private BroadcastReceiver screenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setupLockScreenFlags();
        setContentView(R.layout.activity_lock_screen);
        pinManager = new PinManager(this);
        initializeViews();
        setupPinFields();
        handler = new Handler();
        setupForegroundMonitoring();
        setupScreenReceiver();
    }

    private void setupLockScreenFlags() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    keyguardManager.requestDismissKeyguard(this, null);
                }
            }

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            boolean isScreenOn = powerManager != null && powerManager.isInteractive();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true);
                if (!isScreenOn) {
                    setTurnScreenOn(true);
                }
                getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
            } else {
                int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

                if (!isScreenOn) {
                    flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
                }

                getWindow().addFlags(flags);
            }

            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            );

            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            hideNavigationBar();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void initializeViews() {
        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        tvError = findViewById(R.id.tv_error);
    }

    private void setupPinFields() {
        setupPinField(pin1, null, pin2);
        setupPinField(pin2, pin1, pin3);
        setupPinField(pin3, pin2, pin4);
        setupPinField(pin4, pin3, null);
        pin1.requestFocus();
    }

    private void setupPinField(final EditText current, final EditText previous, final EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    if (next != null) {
                        next.requestFocus();
                    } else {
                        verifyPin();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.requestFocus();
                    previous.setText("");
                }
            }
            return false;
        });
    }

    private void verifyPin() {
        String enteredPin = pin1.getText().toString() +
                           pin2.getText().toString() +
                           pin3.getText().toString() +
                           pin4.getText().toString();

        if (enteredPin.length() == 4) {
            int pinType = pinManager.verifyPinType(enteredPin);

            if (pinType == 1) {
                // PIN principal - desativa modo oculto
                unlockScreen(false);
            } else if (pinType == 2) {
                // PIN secundário - ativa modo oculto
                unlockScreen(true);
            } else {
                showError();
            }
        }
    }

    private void unlockScreen(boolean activateHideMode) {
        AppPreferences prefs = new AppPreferences(this);
        prefs.setHideModeActive(activateHideMode);

        if (activateHideMode) {
            Toast.makeText(this, "Desbloqueado - Modo privado ativado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Desbloqueado!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void showError() {
        tvError.setVisibility(View.VISIBLE);
        tvError.setText("PIN incorreto!");
        pin1.postDelayed(() -> {
            clearPinFields();
            tvError.setVisibility(View.GONE);
        }, 1000);
    }

    private void clearPinFields() {
        pin1.setText("");
        pin2.setText("");
        pin3.setText("");
        pin4.setText("");
        pin1.requestFocus();
    }

    private void setupScreenReceiver() {
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    isScreenOn = false;
                } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    isScreenOn = true;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, filter);
    }

    private void setupForegroundMonitoring() {
        checkForegroundTask = new Runnable() {
            @Override
            public void run() {
                AppPreferences prefs = new AppPreferences(LockScreenActivity.this);
                if (prefs.isLockScreenEnabled() && isScreenOn && !isThisAppInForeground()) {
                    bringToFront();
                }
                handler.postDelayed(this, 500);
            }
        };
    }

    private boolean isThisAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) return false;

            final String packageName = getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void bringToFront() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null && !powerManager.isInteractive()) {
            return;
        }

        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                       Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                       Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
        AppPreferences prefs = new AppPreferences(this);
        if (prefs.isLockScreenEnabled() && checkForegroundTask != null) {
            handler.post(checkForegroundTask);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkForegroundTask != null) {
            handler.removeCallbacks(checkForegroundTask);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppPreferences prefs = new AppPreferences(this);
        if (prefs.isLockScreenEnabled() && !isFinishing() && isScreenOn) {
            bringToFront();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && checkForegroundTask != null) {
            handler.removeCallbacks(checkForegroundTask);
        }
        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        AppPreferences prefs = new AppPreferences(this);
        if (prefs.isLockScreenEnabled() && !isFinishing() && isScreenOn) {
            bringToFront();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0);
        hideNavigationBar();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        // Bloqueia o botão voltar
    }
}
