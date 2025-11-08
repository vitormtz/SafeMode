package com.example.safemode;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
            boolean hasAccessibility = hasAccessibilityPermission();

            // Se todas as permissões estão OK, ativar normalmente
            if (hasLocation && hasOverlay && hasAccessibility) {
                enableSafeMode();
                return;
            }

            // Se faltam permissões, mostrar aviso e redirecionar
            showPermissionsDialog(hasLocation, hasOverlay, hasAccessibility);

        } catch (Exception e) {

            // Reverter o switch
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
        }
    }

    /**
     * ✅ NOVO: Mostra diálogo explicando quais permissões faltam
     */
    /**
     * ✅ NOVO: Mostra diálogo explicando quais permissões faltam - COM COR PERSONALIZADA
     */
    private void showPermissionsDialog(boolean hasLocation, boolean hasOverlay, boolean hasAccessibility) {
        try {
            // Criar lista das permissões que faltam
            StringBuilder missingPermissions = new StringBuilder();

            if (!hasLocation) {
                missingPermissions.append("• Acesso à localização\n");
            }
            if (!hasOverlay) {
                missingPermissions.append("• Exibir sobre outros apps\n" +
                        "• Estatísticas de uso\n");
            }
            if (!hasAccessibility) {
                missingPermissions.append("• Serviço de acessibilidade\n");
            }

            String message = "Para que o Safe Mode funcione corretamente, você precisa conceder as seguintes permissões:\n\n" +
                    missingPermissions.toString() +
                    "\nDeseja ir para as configurações agora?";

            // ✅ CRIAR O DIÁLOGO
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Permissões Necessárias")
                    .setMessage(message)
                    .setPositiveButton("Ir para Configurações", (dialogInterface, which) -> {
                        // Ir para a tela de configurações onde pode configurar tudo
                        Intent intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                        // Reverter o switch se cancelar
                        if (switchSafeMode != null) {
                            switchSafeMode.setChecked(false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            // ✅ PERSONALIZAR COR DE FUNDO DIRETO NO CÓDIGO
            dialog.show();
             GradientDrawable gradient = new GradientDrawable();
             gradient.setColors(new int[]{
                getResources().getColor(R.color.primary_dark_blue),
                 getResources().getColor(R.color.blue_medium)
            });
            gradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            gradient.setCornerRadius(20f);
            dialog.getWindow().setBackgroundDrawable(gradient);

            // ✅ PERSONALIZAR COR DO TEXTO DOS BOTÕES (opcional)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.white));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.white));

        } catch (Exception e) {

            // Reverter o switch
            if (switchSafeMode != null) {
                switchSafeMode.setChecked(false);
            }
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
            // Verificar se tem PIN configurado
            if (!pinManager.hasPin()) {
                Toast.makeText(this, "Configure um PIN primeiro!", Toast.LENGTH_SHORT).show();
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

            if (pin.equals(etPin.getText().toString())) {
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

                        preferences.setLocationEnabled(isChecked);
                    });

                } else {

                    // Garantir que o listener está correto
                    switchLocationControl.setOnCheckedChangeListener((buttonView, isChecked) -> {

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
     * 🆕 NOVO MÉTODO: Verifica se é a primeira execução do app
     * É como perguntar: "é a primeira vez que este app está rodando?"
     */
    private boolean isFirstRun() {
        try {
            // Usar SharedPreferences para verificar se já rodou antes
            android.content.SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);
            boolean isFirst = prefs.getBoolean("is_first_run", true);


            if (isFirst) {
                // Marcar que já não é mais a primeira vez
                prefs.edit().putBoolean("is_first_run", false).apply();
            }

            return isFirst;

        } catch (Exception e) {
            // Em caso de erro, assumir que NÃO é primeira execução (mais seguro)
            return false;
        }
    }
}