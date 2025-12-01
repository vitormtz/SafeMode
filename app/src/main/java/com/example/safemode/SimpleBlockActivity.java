package com.example.safemode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SimpleBlockActivity extends Activity {

    private Handler autoCloseHandler;
    private static boolean isCurrentlyShowing = false;
    private static long lastCloseTime = 0;
    private static SimpleBlockActivity currentInstance = null;

    public static boolean isCurrentlyActive() {
        return isCurrentlyShowing && currentInstance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long currentTime = System.currentTimeMillis();
        long timeSinceLastClose = currentTime - lastCloseTime;

        if (isCurrentlyShowing && timeSinceLastClose > 1000) {
            isCurrentlyShowing = false;
        }

        if (isCurrentlyShowing) {
            finish();
            return;
        }

        isCurrentlyShowing = true;
        currentInstance = this;

        try {
            setupInvisibleWindow();

            createInvisibleOverlay();

            scheduleErrorMessage();

        } catch (Exception e) {
            finish();
        }
    }

    private void setupInvisibleWindow() {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            Window window = getWindow();

            window.setFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            );

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(android.R.color.black));
                window.setNavigationBarColor(getResources().getColor(android.R.color.black));
            }

        } catch (Exception e) {
        }
    }

    private void createInvisibleOverlay() {
        try {
            android.widget.FrameLayout invisibleLayout = new android.widget.FrameLayout(this);
            invisibleLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            invisibleLayout.setClickable(true);
            invisibleLayout.setFocusable(true);

            android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
            invisibleLayout.setLayoutParams(params);

            setContentView(invisibleLayout);

        } catch (Exception e) {
        }
    }

    private void scheduleErrorMessage() {
        try {
            autoCloseHandler = new Handler();

            autoCloseHandler.postDelayed(() -> {
                showErrorMessage();
            }, 2000);

            autoCloseHandler.postDelayed(() -> {
                isCurrentlyShowing = false;
                lastCloseTime = System.currentTimeMillis();
            }, 30000);
        } catch (Exception e) {
        }
    }

    private void showErrorMessage() {
        try {
            setContentView(R.layout.overlay_block);

            setupUI();

        } catch (Exception e) {
        }
    }

    private void setupUI() {
        try {
            String blockedPackage = getIntent().getStringExtra("blocked_package");
            String appName = getAppName(blockedPackage);

            TextView textoErro = findViewById(R.id.textoErro);
            if (textoErro != null) {
                String message = "O app " + appName + " apresenta falhas contínuas";
                textoErro.setText(message);
            }

            Button btnInfoApp = findViewById(R.id.btnInfoApp);
            if (btnInfoApp != null) {
                btnInfoApp.setOnClickListener(v -> {
                    openAppInfo(blockedPackage);
                });
            }

            Button btnFecharApp = findViewById(R.id.btnFecharApp);
            if (btnFecharApp != null) {
                btnFecharApp.setOnClickListener(v -> {
                    goHome();
                });
            }

        } catch (Exception e) {
        }
    }

    private void cancelAutoClose() {
        if (autoCloseHandler != null) {
            autoCloseHandler.removeCallbacksAndMessages(null);
            autoCloseHandler = null;
        }
    }

    private String getAppName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "este aplicativo";
        }

        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String realAppName = pm.getApplicationLabel(appInfo).toString();

            return realAppName;

        } catch (Exception e) {
            return "este aplicativo";
        }
    }

    private void openAppInfo(String packageName) {
        try {

            if (packageName == null || packageName.isEmpty()) {
                goHome();
                return;
            }

            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

            new Handler().postDelayed(() -> {
                forceClose();
            }, 500);

        } catch (Exception e) {
            try {
                Intent fallbackIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                fallbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallbackIntent);

                new Handler().postDelayed(() -> forceClose(), 500);

            } catch (Exception e2) {
                goHome();
            }
        }
    }

    private void forceClose() {
        try {
            cancelAutoClose();

            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            currentInstance = null;

            super.finish();

        } catch (Exception e) {
            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            System.exit(0);
        }
    }

    private void goHome() {
        try {

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(homeIntent);

            new Handler().postDelayed(() -> forceClose(), 200);

        } catch (Exception e) {
            forceClose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean isFinishing() {
        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            cancelAutoClose();

            lastCloseTime = System.currentTimeMillis();
            isCurrentlyShowing = false;
            currentInstance = null;

        } catch (Exception e) {
        }
        super.onDestroy();
    }
}