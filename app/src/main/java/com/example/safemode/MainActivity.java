package com.example.safemode;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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

        // ANTES de fazer qualquer coisa, verificar o estado
        preferences = new AppPreferences(this);
        boolean initialSafeModeState = preferences.isSafeModeEnabled();

        setupSystemBars();
        setContentView(R.layout.activity_main);

        // DEPOIS de carregar o layout, verificar o switch
        switchSafeMode = findViewById(R.id.switch_safe_mode);

        if (switchSafeMode != null) {
            boolean switchStateFromXML = switchSafeMode.isChecked();

            // Se o switch está ligado mas as preferências dizem que não deveria estar...
            if (switchStateFromXML && !initialSafeModeState) {
                switchSafeMode.setChecked(false);
            }
        }

        initializeViews();
        setupClickListeners();
    }

    /**
     * Configura as barras do sistema para funcionamento correto
     */
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

    /**
     * Inicializa todos os elementos da tela
     */
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

    /**
     * Configura o que cada botão faz quando clicado
     */
    private void setupClickListeners() {

        try {
            // 🔧 CORREÇÃO PRINCIPAL: Switch do Safe Mode COM verificação de permissões
            if (switchSafeMode != null) {
                switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked) {
                        // ✅ NOVO: Verificar permissões ANTES de ativar
                        checkPermissionsAndEnableSafeMode();
                    } else {
                        // Desativar pode ser feito normalmente
                        disableSafeMode();
                    }
                });
            }

            // Switch de controle por localização
            if (switchLocationControl != null) {
                switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // Verificar se tem permissão de localização
                        if (!hasLocationPermission()) {
                            // Mostrar diálogo pedindo permissão
                            showLocationPermissionDialog();
                            // Reverter o switch
                            switchLocationControl.setChecked(false);
                            return;
                        }
                    }
                    preferences.setLocationEnabled(isChecked);
                });
            }

            // Botão Selecionar Apps
            if (btnSelectApps != null) {
                btnSelectApps.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AppSelectionActivity.class);
                    startActivity(intent);
                });
            }

            // Botão Configurar Localização
            if (btnSetLocation != null) {
                btnSetLocation.setOnClickListener(v -> {
                    Intent intent = new Intent(this, LocationSetupActivity.class);
                    startActivity(intent);
                });
            }

            // Botão Configurações Gerais
            if (btnSettings != null) {
                btnSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                });
            }

            // Switch de bloqueio de tela
            if (switchLockScreen != null) {
                switchLockScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        enableLockScreen();
                    } else {
                        disableLockScreen();
                    }
                });
            }

            // Botão Salvar PIN
            if (btnSavePin != null) {
                btnSavePin.setOnClickListener(v -> {
                    savePinConfiguration();
                });
            }

            // Botão Salvar PIN Secundário
            if (btnSaveSecondaryPin != null) {
                btnSaveSecondaryPin.setOnClickListener(v -> {
                    saveSecondaryPinConfiguration();
                });
            }

            // Botão Selecionar Apps para Ocultar
            if (btnSelectHiddenApps != null) {
                btnSelectHiddenApps.setOnClickListener(v -> {
                    openHiddenAppsSelection();
                });
            }

        } catch (Exception e) {
        }
    }

    // ===== 🔧 NOVA FUNCIONALIDADE: VERIFICAÇÃO DE PERMISSÕES =====

    /**
     * ✅ NOVO MÉTODO: Verifica permissões antes de ativar Safe Mode
     * É como verificar se você tem carteira de motorista antes de dirigir
     */
    private void checkPermissionsAndEnableSafeMode() {

        try {
            // Verificar todas as permissões necessárias
            boolean hasLocation = hasLocationPermission();
            boolean hasOverlay = hasOverlayPermission();
            boolean hasUsageStats = hasUsageStatsPermission();
            boolean hasAccessibility = hasAccessibilityPermission();

            // Se todas as permissões estão OK, ativar normalmente
            if (hasLocation && hasOverlay && hasUsageStats && hasAccessibility) {
                enableSafeMode();
                return;
            }

            // Se faltam permissões, mostrar aviso e redirecionar
            showPermissionsDialog(hasLocation, hasOverlay, hasUsageStats, hasAccessibility);

        } catch (Exception e) {

            // Reverter o switch
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    /**
     * Mostra diálogo customizado explicando quais permissões faltam
     */
    private void showPermissionsDialog(boolean hasLocation, boolean hasOverlay, boolean hasUsageStats, boolean hasAccessibility) {
        try {
            // Inflar o layout customizado
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_permissions, null);

            // Referenciar os cards de permissões
            androidx.cardview.widget.CardView locationCard = dialogView.findViewById(R.id.locationPermissionCard);
            androidx.cardview.widget.CardView overlayCard = dialogView.findViewById(R.id.overlayPermissionCard);
            androidx.cardview.widget.CardView usageStatsCard = dialogView.findViewById(R.id.usageStatsPermissionCard);
            androidx.cardview.widget.CardView accessibilityCard = dialogView.findViewById(R.id.accessibilityPermissionCard);

            // Mostrar apenas os cards das permissões que faltam
            locationCard.setVisibility(!hasLocation ? View.VISIBLE : View.GONE);
            overlayCard.setVisibility(!hasOverlay ? View.VISIBLE : View.GONE);
            usageStatsCard.setVisibility(!hasUsageStats ? View.VISIBLE : View.GONE);
            accessibilityCard.setVisibility(!hasAccessibility ? View.VISIBLE : View.GONE);

            // Criar o diálogo customizado
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            // Configurar os botões
            Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            btnGoToSettings.setOnClickListener(v -> {
                // Ir para a tela de configurações
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                // Reverter o switch se cancelar
                if (switchSafeMode != null) {
                    switchSafeMode.setChecked(false);
                }
                dialog.dismiss();
            });

            // Configurar o fundo transparente para o diálogo
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();

        } catch (Exception e) {
            // Reverter o switch
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    /**
     * Mostra diálogo pedindo permissão de localização
     */
    private void showLocationPermissionDialog() {
        try {
            // Inflar o layout customizado
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_permission, null);

            // Criar o diálogo customizado
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            // Configurar os botões
            Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            btnGoToSettings.setOnClickListener(v -> {
                // Ir para a tela de configurações
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });

            // Configurar o fundo transparente para o diálogo
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();

        } catch (Exception e) {
            // Silenciar erro
        }
    }

    // ===== MÉTODOS DE VERIFICAÇÃO DE PERMISSÕES =====

    /**
     * Verifica se tem permissão de localização
     */
    private boolean hasLocationPermission() {
        return androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se tem permissão de overlay
     */
    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return android.provider.Settings.canDrawOverlays(this);
        }
        return true;
    }

    /**
     * Verifica se o serviço de acessibilidade está ativo
     */
    private boolean hasAccessibilityPermission() {
        return AccessibilityUtils.isAccessibilityServiceEnabled(this, SafeModeAccessibilityService.class);
    }

    /**
     * Verifica se tem permissão de estatísticas de uso
     */
    private boolean hasUsageStatsPermission() {
        return UsageStatsUtils.hasUsageStatsPermission(this);
    }

    /**
     * Verifica se o SafeMode está definido como launcher padrão
     */
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

    // ===== MÉTODOS ORIGINAIS (SEM ALTERAÇÃO) =====

    /**
     * Ativa o Safe Mode (só chamado se as permissões estiverem OK)
     */
    private void enableSafeMode() {
        try {
            preferences.setSafeModeEnabled(true);

            Intent serviceIntent = new Intent(this, SafeModeService.class);
            startService(serviceIntent);

        } catch (Exception e) {

            // Reverter o switch em caso de erro
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    /**
     * Desativa o Safe Mode
     */
    private void disableSafeMode() {
        try {
            preferences.setSafeModeEnabled(false);

            Intent serviceIntent = new Intent(this, SafeModeService.class);
            stopService(serviceIntent);

        } catch (Exception e) {
        }
    }

    // ===== MÉTODOS DE BLOQUEIO DE TELA =====

    /**
     * Ativa o bloqueio de tela
     */
    private void enableLockScreen() {
        try {
            // Verificar se o app está definido como launcher padrão
            if (!isDefaultLauncher()) {
                // Inflar o layout customizado
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_launcher_required, null);

                // Criar o diálogo customizado
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                // Configurar os botões
                Button btnGoToSettings = dialogView.findViewById(R.id.btnGoToSettings);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                btnGoToSettings.setOnClickListener(v -> {
                    // Ir para a tela de configurações
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                });

                btnCancel.setOnClickListener(v -> {
                    // Reverter o switch se cancelar
                    if (switchLockScreen != null) {
                        switchLockScreen.setChecked(false);
                    }
                    dialog.dismiss();
                });

                // Configurar o fundo transparente para o diálogo
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                dialog.show();

                // Reverter o switch
                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }

                return;
            }

            // Verificar se tem PIN configurado
            if (!pinManager.hasPin()) {
                Toast.makeText(this, "Configure um PIN primeiro!", Toast.LENGTH_SHORT).show();
                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }
                return;
            }

            // Verificar se tem PIN secundário configurado
            if (!pinManager.hasSecondaryPin()) {
                Toast.makeText(this, "Configure o PIN secundário primeiro!", Toast.LENGTH_SHORT).show();
                if (switchLockScreen != null) {
                    switchLockScreen.setChecked(false);
                }
                return;
            }

            preferences.setLockScreenEnabled(true);

            // Iniciar serviço de bloqueio
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

    /**
     * Desativa o bloqueio de tela
     */
    private void disableLockScreen() {
        try {
            preferences.setLockScreenEnabled(false);

            // Parar serviço de bloqueio
            Intent serviceIntent = new Intent(this, LockScreenService.class);
            stopService(serviceIntent);

            Toast.makeText(this, "Bloqueio de tela desativado!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao desativar bloqueio", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Salva a configuração do PIN
     */
    private void savePinConfiguration() {
        try {
            String pin = etPin.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN deve ter 4 dígitos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar se já existe um PIN secundário configurado e se é igual ao novo PIN principal
            if (pinManager.hasSecondaryPin()) {
                // Comparar o novo PIN principal com o PIN secundário salvo
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

    /**
     * Salva a configuração do PIN secundário
     */
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

            // Verificar se o PIN secundário é igual ao PIN principal salvo
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

    /**
     * Abre a tela de seleção de apps para ocultar
     */
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
            // 🔧 CORREÇÃO PRINCIPAL: Garantir que SafeMode comece sempre como false na primeira execução
            if (isFirstRun()) {
                preferences.setSafeModeEnabled(false);
                preferences.setLocationEnabled(false);
                preferences.setBlockedApps(new HashSet<>()); // Limpar lista de apps bloqueados
                preferences.setHiddenApps(new HashSet<>()); // Limpar lista de apps ocultos
            }

            // Ler os valores das preferências (agora garantidamente corretos)
            boolean safeModeEnabled = preferences.isSafeModeEnabled();
            boolean locationEnabled = preferences.isLocationEnabled();

            // ===== CONFIGURAR SWITCH DO SAFE MODE =====
            if (switchSafeMode != null) {
                boolean currentSafeModeState = switchSafeMode.isChecked();

                // Só atualizar se for diferente (evita loops desnecessários)
                if (currentSafeModeState != safeModeEnabled) {

                    // Remover listener temporariamente para evitar disparo acidental
                    switchSafeMode.setOnCheckedChangeListener(null);

                    // Definir o estado correto
                    switchSafeMode.setChecked(safeModeEnabled);

                    // Restaurar listener com a lógica correta
                    switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked) {
                            // Verificar permissões antes de ativar
                            checkPermissionsAndEnableSafeMode();
                        } else {
                            // Desativar normalmente
                            disableSafeMode();
                        }
                    });
                } else {

                    // Mesmo se não mudou o estado, garantir que o listener está correto
                    switchSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked) {
                            checkPermissionsAndEnableSafeMode();
                        } else {
                            disableSafeMode();
                        }
                    });
                }
            }

            // ===== CONFIGURAR SWITCH DE LOCALIZAÇÃO =====
            if (switchLocationControl != null) {
                boolean currentLocationState = switchLocationControl.isChecked();

                // Só atualizar se for diferente
                if (currentLocationState != locationEnabled) {

                    // Remover listener temporariamente
                    switchLocationControl.setOnCheckedChangeListener(null);

                    // Definir estado correto
                    switchLocationControl.setChecked(locationEnabled);

                    // Restaurar listener
                    switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            // Verificar se tem permissão de localização
                            if (!hasLocationPermission()) {
                                // Mostrar diálogo pedindo permissão
                                showLocationPermissionDialog();
                                // Reverter o switch
                                switchLocationControl.setChecked(false);
                                return;
                            }
                        }
                        preferences.setLocationEnabled(isChecked);
                    });

                } else {

                    // Garantir que o listener está correto
                    switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            // Verificar se tem permissão de localização
                            if (!hasLocationPermission()) {
                                // Mostrar diálogo pedindo permissão
                                showLocationPermissionDialog();
                                // Reverter o switch
                                switchLocationControl.setChecked(false);
                                return;
                            }
                        }
                        preferences.setLocationEnabled(isChecked);
                    });
                }
            }

            // ===== CONFIGURAR SWITCH DE BLOQUEIO DE TELA =====
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

            // Em caso de erro, garantir que os switches fiquem em estado seguro
            try {
                if (switchSafeMode != null) {
                    switchSafeMode.setOnCheckedChangeListener(null);
                    switchSafeMode.setChecked(false);
                }

                if (switchLocationControl != null) {
                    switchLocationControl.setOnCheckedChangeListener(null);
                    switchLocationControl.setChecked(false);
                }

                // Forçar preferências para estado seguro
                preferences.setSafeModeEnabled(false);
                preferences.setLocationEnabled(false);

            } catch (Exception secondaryError) {
            }
        }
    }

    /**
     * 🆕 NOVO MÉTODO: Verifica se é a primeira execução do app ou se foi reinstalado
     * É como perguntar: "é a primeira vez que este app está rodando?"
     */
    private boolean isFirstRun() {
        try {
            // Usar SharedPreferences para verificar se já rodou antes
            android.content.SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);

            // Verificar se o app foi instalado/reinstalado comparando com timestamp de instalação
            long installTime = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
            long lastKnownInstallTime = prefs.getLong("last_install_time", 0);

            boolean isFirst = (lastKnownInstallTime != installTime);

            if (isFirst) {
                // Marcar que já não é mais a primeira vez e salvar timestamp de instalação
                prefs.edit()
                    .putBoolean("is_first_run", false)
                    .putLong("last_install_time", installTime)
                    .apply();
            }

            return isFirst;

        } catch (Exception e) {
            // Em caso de erro, assumir que NÃO é primeira execução (mais seguro)
            return false;
        }
    }
}