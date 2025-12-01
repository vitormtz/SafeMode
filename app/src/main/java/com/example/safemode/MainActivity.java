package com.example.safemode;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private Switch switchSafeMode;
    private Switch switchLocationControl;
    private Switch switchLockScreen;
    private Button btnSelectApps;
    private Button btnSetLocation;
    private Button btnSettings;
    private Button btnSavePin;
    private Button btnSaveSecondaryPin;
    private Button btnSelectHiddenApps;
    private android.widget.EditText etPin;
    private android.widget.EditText etSecondaryPin;
    private AppPreferences preferences;
    private PinManager pinManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        preferences = new AppPreferences(this);
        boolean initialSafeModeState = preferences.isSafeModeEnabled();

        setupSystemBars();
        setContentView(R.layout.activity_main);

        switchSafeMode = findViewById(R.id.switch_safe_mode);

        if (switchSafeMode != null) {
            boolean switchStateFromXML = switchSafeMode.isChecked();

            if (switchStateFromXML && !initialSafeModeState) {
                switchSafeMode.setChecked(false);
            }
        }

        initializeViews();
        setupClickListeners();
    }

    private void setupSystemBars() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
            }
        } catch (Exception e) {
        }
    }

    private void initializeViews() {

        try {
            switchSafeMode = findViewById(R.id.switch_safe_mode);
            switchLocationControl = findViewById(R.id.switch_location_control);
            switchLockScreen = findViewById(R.id.switch_lock_screen);
            btnSelectApps = findViewById(R.id.btn_select_apps);
            btnSetLocation = findViewById(R.id.btn_set_location);
            btnSettings = findViewById(R.id.btn_settings);
            btnSavePin = findViewById(R.id.btn_save_pin);
            btnSaveSecondaryPin = findViewById(R.id.btn_save_secondary_pin);
            btnSelectHiddenApps = findViewById(R.id.btn_select_hidden_apps);
            etPin = findViewById(R.id.et_pin);
            etSecondaryPin = findViewById(R.id.et_secondary_pin);

            preferences = new AppPreferences(this);
            pinManager = new PinManager(this);

        } catch (Exception e) {
        }
    }

    private void setupClickListeners() {

        try {
            if (switchSafeMode != null) {
                switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked) {
                        checkPermissionsAndEnableSafeMode();
                    } else {
                        disableSafeMode();
                    }
                });
            }

            if (switchLocationControl != null) {
                switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!hasLocationPermission()) {
                            showLocationPermissionDialog();
                            switchLocationControl.setChecked(false);
                            return;
                        }
                    }
                    preferences.setLocationEnabled(isChecked);
                });
            }

            if (btnSelectApps != null) {
                btnSelectApps.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AppSelectionActivity.class);
                    startActivity(intent);
                });
            }

            if (btnSetLocation != null) {
                btnSetLocation.setOnClickListener(v -> {
                    Intent intent = new Intent(this, LocationSetupActivity.class);
                    startActivity(intent);
                });
            }

            if (btnSettings != null) {
                btnSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                });
            }

            if (switchLockScreen != null) {
                switchLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        enableLockScreen();
                    } else {
                        disableLockScreen();
                    }
                });
            }

            if (btnSavePin != null) {
                btnSavePin.setOnClickListener(v -> {
                    savePinConfiguration();
                });
            }

            if (btnSaveSecondaryPin != null) {
                btnSaveSecondaryPin.setOnClickListener(v -> {
                    saveSecondaryPinConfiguration();
                });
            }

            if (btnSelectHiddenApps != null) {
                btnSelectHiddenApps.setOnClickListener(v -> {
                    openHiddenAppsSelection();
                });
            }

        } catch (Exception e) {
        }
    }

    private void checkPermissionsAndEnableSafeMode() {

        try {
            boolean hasLocation = hasLocationPermission();
            boolean hasOverlay = hasOverlayPermission();
            boolean hasUsageStats = hasUsageStatsPermission();
            boolean hasAccessibility = hasAccessibilityPermission();

            if (hasLocation && hasOverlay && hasUsageStats && hasAccessibility) {
                enableSafeMode();
                return;
            }

            showPermissionsDialog(hasLocation, hasOverlay, hasUsageStats, hasAccessibility);

        } catch (Exception e) {

            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    private void showPermissionsDialog(boolean hasLocation, boolean hasOverlay, boolean hasUsageStats, boolean hasAccessibility) {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_permissions, null);

            androidx.cardview.widget.CardView locationCard = dialogView.findViewById(R.id.locationPermissionCard);
            androidx.cardview.widget.CardView overlayCard = dialogView.findViewById(R.id.overlayPermissionCard);
            androidx.cardview.widget.CardView usageStatsCard = dialogView.findViewById(R.id.usageStatsPermissionCard);
            androidx.cardview.widget.CardView accessibilityCard = dialogView.findViewById(R.id.accessibilityPermissionCard);

            locationCard.setVisibility(!hasLocation ? View.VISIBLE : View.GONE);
            overlayCard.setVisibility(!hasOverlay ? View.VISIBLE : View.GONE);
            usageStatsCard.setVisibility(!hasUsageStats ? View.VISIBLE : View.GONE);
            accessibilityCard.setVisibility(!hasAccessibility ? View.VISIBLE : View.GONE);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            btnGoToSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                if (switchSafeMode != null) {
                    switchSafeMode.setChecked(false);
                }
                dialog.dismiss();
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();

        } catch (Exception e) {
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    private void showLocationPermissionDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_permission, null);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            btnGoToSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();

        } catch (Exception e) {
        }
    }

    private boolean hasLocationPermission() {
        return androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return android.provider.Settings.canDrawOverlays(this);
        }
        return true;
    }

    private boolean hasAccessibilityPermission() {
        return AccessibilityUtils.isAccessibilityServiceEnabled(this, SafeModeAccessibilityService.class);
    }

    private boolean hasUsageStatsPermission() {
        return UsageStatsUtils.hasUsageStatsPermission(this);
    }

    private boolean isDefaultLauncher() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);

            android.content.pm.ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                String currentLauncher = resolveInfo.activityInfo.packageName;
                return currentLauncher.equals(getPackageName());
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void enableSafeMode() {
        try {
            preferences.setSafeModeEnabled(true);

            Intent serviceIntent = new Intent(this, SafeModeService.class);
            startService(serviceIntent);

        } catch (Exception e) {

            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    private void disableSafeMode() {
        try {
            preferences.setSafeModeEnabled(false);

            Intent serviceIntent = new Intent(this, SafeModeService.class);
            stopService(serviceIntent);

        } catch (Exception e) {
        }
    }

    private void enableLockScreen() {
        try {
            if (!isDefaultLauncher()) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_launcher_required, null);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                btnGoToSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> {
                    if (switchLockScreen != null) {
                        switchLockScreen.setChecked(false);
                    }
                    dialog.dismiss();
                });

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                dialog.show();

                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }

                return;
            }

            if (!pinManager.hasPin()) {
                Toast.makeText(this, "Configure um PIN primeiro!", Toast.LENGTH_SHORT).show();
                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }
                return;
            }

            if (!pinManager.hasSecondaryPin()) {
                Toast.makeText(this, "Configure o PIN secundário primeiro!", Toast.LENGTH_SHORT).show();
                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }
                return;
            }

            preferences.setLockScreenEnabled(true);

            Intent serviceIntent = new Intent(this, LockScreenService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Toast.makeText(this, "Bloqueio de tela ativado!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao ativar bloqueio", Toast.LENGTH_SHORT).show();
            if (switchLockScreen != null) {
                switchLockScreen.setChecked(false);
            }
        }
    }

    private void disableLockScreen() {
        try {
            preferences.setLockScreenEnabled(false);

            Intent serviceIntent = new Intent(this, LockScreenService.class);
            stopService(serviceIntent);

            Toast.makeText(this, "Bloqueio de tela desativado!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao desativar bloqueio", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePinConfiguration() {
        try {
            String pin = etPin.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN deve ter 4 dígitos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinManager.hasSecondaryPin()) {
                if (pinManager.verifySecondaryPin(pin)) {
                    Toast.makeText(this, "PIN principal não pode ser igual ao PIN secundário!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (pinManager.setPin(pin)) {
                Toast.makeText(this, "PIN configurado com sucesso!", Toast.LENGTH_SHORT).show();
                etPin.setText("");
            } else {
                Toast.makeText(this, "Erro ao salvar PIN", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao configurar PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSecondaryPinConfiguration() {
        try {
            String pin = etSecondaryPin.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN secundário deve ter 4 dígitos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pinManager.hasPin()) {
                Toast.makeText(this, "Configure o PIN principal primeiro!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinManager.verifyPin(pin)) {
                Toast.makeText(this, "PIN secundário deve ser diferente do principal!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinManager.setSecondaryPin(pin)) {
                Toast.makeText(this, "PIN secundário configurado com sucesso!", Toast.LENGTH_SHORT).show();
                etSecondaryPin.setText("");
            } else {
                Toast.makeText(this, "Erro ao salvar PIN secundário", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao configurar PIN secundário", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHiddenAppsSelection() {
        try {
            if (!pinManager.hasSecondaryPin()) {
                Toast.makeText(this, "Configure o PIN secundário primeiro!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, HiddenAppsSelectionActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir seleção de apps", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (isFirstRun()) {
                preferences.setSafeModeEnabled(false);
                preferences.setLocationEnabled(false);
                preferences.setBlockedApps(new HashSet<>());
                preferences.setHiddenApps(new HashSet<>());
            }

            boolean safeModeEnabled = preferences.isSafeModeEnabled();
            boolean locationEnabled = preferences.isLocationEnabled();

            if (switchSafeMode != null) {
                boolean currentSafeModeState = switchSafeMode.isChecked();

                if (currentSafeModeState != safeModeEnabled) {

                    switchSafeMode.setOnCheckedChangeListener(null);

                    switchSafeMode.setChecked(safeModeEnabled);

                    switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked) {
                            checkPermissionsAndEnableSafeMode();
                        } else {
                            disableSafeMode();
                        }
                    });
                } else {

                    switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked) {
                            checkPermissionsAndEnableSafeMode();
                        } else {
                            disableSafeMode();
                        }
                    });
                }
            }

            if (switchLocationControl != null) {
                boolean currentLocationState = switchLocationControl.isChecked();

                if (currentLocationState != locationEnabled) {

                    switchLocationControl.setOnCheckedChangeListener(null);

                    switchLocationControl.setChecked(locationEnabled);

                    switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            if (!hasLocationPermission()) {
                                showLocationPermissionDialog();
                                switchLocationControl.setChecked(false);
                                return;
                            }
                        }
                        preferences.setLocationEnabled(isChecked);
                    });

                } else {
                    switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            if (!hasLocationPermission()) {
                                showLocationPermissionDialog();
                                switchLocationControl.setChecked(false);
                                return;
                            }
                        }
                        preferences.setLocationEnabled(isChecked);
                    });
                }
            }

            boolean lockScreenEnabled = preferences.isLockScreenEnabled();
            if (switchLockScreen != null) {
                boolean currentLockScreenState = switchLockScreen.isChecked();

                if (currentLockScreenState != lockScreenEnabled) {
                    switchLockScreen.setOnCheckedChangeListener(null);
                    switchLockScreen.setChecked(lockScreenEnabled);

                    switchLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            enableLockScreen();
                        } else {
                            disableLockScreen();
                        }
                    });
                } else {
                    switchLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            enableLockScreen();
                        } else {
                            disableLockScreen();
                        }
                    });
                }
            }

        } catch (Exception e) {

            try {
                if (switchSafeMode != null) {
                    switchSafeMode.setOnCheckedChangeListener(null);
                    switchSafeMode.setChecked(false);
                }

                if (switchLocationControl != null) {
                    switchLocationControl.setOnCheckedChangeListener(null);
                    switchLocationControl.setChecked(false);
                }

                preferences.setSafeModeEnabled(false);
                preferences.setLocationEnabled(false);

            } catch (Exception secondaryError) {
            }
        }
    }

    private boolean isFirstRun() {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);

            long installTime = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
            long lastKnownInstallTime = prefs.getLong("last_install_time", 0);

            boolean isFirst = (lastKnownInstallTime != installTime);

            if (isFirst) {
                prefs.edit()
                    .putBoolean("is_first_run", false)
                    .putLong("last_install_time", installTime)
                    .apply();
            }

            return isFirst;

        } catch (Exception e) {
            return false;
        }
    }
}