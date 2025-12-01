package com.example.safemode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;

/**
 * SettingsActivity - VERSÃO EXPANDIDA COM PERMISSÕES
 * Agora inclui toda a funcionalidade de verificação e configuração de permissões
 */
public class SettingsActivity extends AppCompatActivity {

    // Códigos de request para permissões
    private static final int PERMISSION_REQUEST_LOCATION = 1001;
    private static final int PERMISSION_REQUEST_OVERLAY = 1002;
    private static final int PERMISSION_REQUEST_ACCESSIBILITY = 1003;
    private static final int PERMISSION_REQUEST_USAGE_STATS = 1004;

    // Elementos da interface
    private TextView textBlockedAppsCount;
    private TextView textTodayStats;
    private Button btnViewLogs;
    private Button btnPermissions;
    private Button btnSetLauncher;
    private android.widget.LinearLayout layoutLauncherOption;

    // Elementos de status das permissões
    private ImageView iconLocationPermission;
    private ImageView iconOverlayPermission;
    private ImageView iconAccessibilityPermission;
    private ImageView iconUsagePermission;
    private ImageView iconLauncherStatus;
    private TextView textLocationStatus;
    private TextView textOverlayStatus;
    private TextView textAccessibilityStatus;
    private TextView textUsageStatus;
    private TextView textLauncherStatus;

    // Classes auxiliares
    private AppPreferences preferences;
    private BlockLogger blockLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_settings);

        // Inicializar componentes
        setupSystemBars();
        initializeViews();
        loadCurrentSettings();
        setupListeners();

        // Verificar permissões logo de cara
        checkAllPermissions();
    }

    /**
     * Configura as barras do sistema
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
            // Elementos básicos de configurações
            textBlockedAppsCount = findViewById(R.id.text_blocked_apps_count);
            textTodayStats = findViewById(R.id.text_today_stats);
            btnViewLogs = findViewById(R.id.btn_view_logs);
            btnPermissions = findViewById(R.id.btn_permissions);
            btnSetLauncher = findViewById(R.id.btn_set_launcher);
            layoutLauncherOption = findViewById(R.id.layout_launcher_option);

            // Elementos de status das permissões
            iconLocationPermission = findViewById(R.id.icon_location_permission);
            iconOverlayPermission = findViewById(R.id.icon_overlay_permission);
            iconAccessibilityPermission = findViewById(R.id.icon_accessibility_permission);
            iconUsagePermission = findViewById(R.id.icon_usage_permission);
            iconLauncherStatus = findViewById(R.id.icon_launcher_status);

            textLocationStatus = findViewById(R.id.text_location_status);
            textOverlayStatus = findViewById(R.id.text_overlay_status);
            textAccessibilityStatus = findViewById(R.id.text_accessibility_status);
            textUsageStatus = findViewById(R.id.text_usage_status);
            textLauncherStatus = findViewById(R.id.text_launcher_status);

            // Inicializar classes auxiliares
            preferences = new AppPreferences(this);
            blockLogger = new BlockLogger(this);

        } catch (Exception e) {
        }
    }

    /**
     * Carrega as configurações atuais
     */
    private void loadCurrentSettings() {

        try {
            // Switch de controle por localização foi movido para MainActivity
            // Não há mais switch aqui

            // Contar apps bloqueados
            int blockedAppsCount = preferences.getBlockedApps().size();
            if (textBlockedAppsCount != null) {
                textBlockedAppsCount.setText(blockedAppsCount + " apps selecionados para bloqueio");
            }

            // Estatísticas de hoje
            loadTodayStats();

        } catch (Exception e) {
        }
    }

    /**
     * Carrega estatísticas do dia atual
     */
    private void loadTodayStats() {
        try {
            JSONObject stats = blockLogger.getTodayStats();

            int totalBlocks = stats.optInt("total_blocks_today", 0);
            int appsBlocked = stats.optInt("apps_blocked_today", 0);
            String mostBlocked = stats.optString("most_blocked_app", "Nenhum");

            String statsText = String.format(
                    "Hoje: %d bloqueios em %d apps\nMais bloqueado: %s",
                    totalBlocks, appsBlocked, mostBlocked
            );

            if (textTodayStats != null) {
                textTodayStats.setText(statsText);
            }

        } catch (Exception e) {
            if (textTodayStats != null) {
                textTodayStats.setText("Erro ao carregar estatísticas");
            }
        }
    }

    /**
     * Configura os listeners dos controles
     */
    private void setupListeners() {

        try {
            // Switch de controle por localização foi movido para MainActivity
            // Aqui ficam apenas os listeners dos botões

            // Botão para ver histórico de bloqueios
            if (btnViewLogs != null) {
                btnViewLogs.setOnClickListener(v -> showBlockLogs());
            }

            // Botão para configurar permissões
            if (btnPermissions != null) {
                btnPermissions.setOnClickListener(v -> {
                    requestAllPermissions();
                });
            }

            // Botão para definir launcher padrão
            if (btnSetLauncher != null) {
                btnSetLauncher.setOnClickListener(v -> {
                    openLauncherSettings();
                });
            }

        } catch (Exception e) {
        }
    }

    // ===== FUNCIONALIDADES DE PERMISSÕES =====

    /**
     * Verifica todas as permissões e atualiza a interface
     */
    private void checkAllPermissions() {

        try {
            boolean hasLocation = hasLocationPermission();
            boolean hasOverlay = hasOverlayPermission();
            boolean hasAccessibility = hasAccessibilityPermission();
            boolean hasUsageStats = hasUsageStatsPermission();
            boolean isLauncher = isDefaultLauncher();

            updatePermissionStatus(hasLocation, hasOverlay, hasAccessibility, hasUsageStats, isLauncher);

        } catch (Exception e) {
        }
    }

    /**
     * Verifica se tem permissão de localização
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se tem permissão de overlay
     */
    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
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

    /**
     * Verifica se tem todas as permissões
     */
    private boolean hasAllPermissions() {
        return hasLocationPermission() && hasOverlayPermission() &&
                hasAccessibilityPermission() && hasUsageStatsPermission();
    }

    /**
     * Solicita todas as permissões necessárias, uma por vez
     */
    private void requestAllPermissions() {

        try {
            if (!hasLocationPermission()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
                return;
            }

            if (!hasOverlayPermission()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_OVERLAY);
                return;
            }

            if (!hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, PERMISSION_REQUEST_USAGE_STATS);
                return;
            }

            if (!hasAccessibilityPermission()) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, PERMISSION_REQUEST_ACCESSIBILITY);
                return;
            }

            // Se chegou aqui, todas as permissões já foram concedidas
            showMessage("Todas as permissões já foram concedidas!");

        } catch (Exception e) {
        }
    }

    /**
     * Atualiza o status visual das permissões na interface
     */
    private void updatePermissionStatus(boolean location, boolean overlay,
                                        boolean accessibility, boolean usageStats, boolean isLauncher) {

        try {
            // Permissão de Localização
            updatePermissionIcon(iconLocationPermission, location);
            updatePermissionText(textLocationStatus, location ? "Concedida" : "Negada", location);

            // Permissão de Overlay
            updatePermissionIcon(iconOverlayPermission, overlay);
            updatePermissionText(textOverlayStatus, overlay ? "Concedida" : "Negada", overlay);

            // Permissão de Acessibilidade
            updatePermissionIcon(iconAccessibilityPermission, accessibility);
            updatePermissionText(textAccessibilityStatus, accessibility ? "Ativo" : "Inativo", accessibility);

            // Permissão de Estatísticas
            updatePermissionIcon(iconUsagePermission, usageStats);
            updatePermissionText(textUsageStatus, usageStats ? "Concedida" : "Negada", usageStats);

            // Status do Launcher
            updatePermissionIcon(iconLauncherStatus, isLauncher);
            updatePermissionText(textLauncherStatus, isLauncher ? "Definido" : "Não Definido", isLauncher);


        } catch (Exception e) {
        }
    }

    /**
     * Atualiza o ícone de uma permissão (verde = OK, vermelho = negada)
     */
    private void updatePermissionIcon(ImageView icon, boolean hasPermission) {
        if (icon != null) {
            try {
                int color = hasPermission ?
                        getResources().getColor(android.R.color.holo_green_dark) :
                        getResources().getColor(android.R.color.holo_red_dark);
                icon.setColorFilter(color);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Atualiza o texto de uma permissão
     */
    private void updatePermissionText(TextView textView, String text, boolean hasPermission) {
        if (textView != null) {
            try {
                textView.setText(text);
                int color = hasPermission ?
                        getResources().getColor(android.R.color.holo_green_dark) :
                        getResources().getColor(android.R.color.holo_red_dark);
                textView.setTextColor(color);
            } catch (Exception e) {
            }
        }
    }

    // ===== RESULTADO DAS PERMISSÕES =====

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == PERMISSION_REQUEST_LOCATION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }

            // Verificar novamente todas as permissões
            checkAllPermissions();

        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // Pequeno delay para garantir que as configurações foram aplicadas
            new android.os.Handler().postDelayed(() -> {
                checkAllPermissions();
            }, 1000);

        } catch (Exception e) {
        }
    }

    // ===== OUTRAS FUNCIONALIDADES =====

    /**
     * Mostra o histórico de bloqueios
     */
    private void showBlockLogs() {

        try {
            StringBuilder logsText = new StringBuilder();

            // Pegar logs dos últimos 7 dias
            long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            var recentLogs = blockLogger.getLogEntriesInPeriod(sevenDaysAgo, System.currentTimeMillis());

            if (recentLogs.isEmpty()) {
                logsText.append("Nenhum bloqueio registrado nos últimos 7 dias.");
            } else {
                logsText.append("Bloqueios dos últimos 7 dias:\n\n");

                int maxEntries = Math.min(recentLogs.size(), 20); // Mostrar no máximo 20
                for (int i = 0; i < maxEntries; i++) {
                    JSONObject log = recentLogs.get(i);

                    String appName = log.optString("app_name", "App desconhecido");
                    String time = log.optString("readable_time", "Horário desconhecido");

                    logsText.append("• ").append(appName).append("\n")
                            .append("  ").append(time).append("\n\n");
                }

                if (recentLogs.size() > 20) {
                    logsText.append("... e mais ").append(recentLogs.size() - 20).append(" registros");
                }
            }

            // ✅ CRIAR O DIÁLOGO COM COR PERSONALIZADA
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Histórico de Bloqueios")
                    .setMessage(logsText.toString())
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Limpar Histórico", (dialogInterface, which) -> confirmClearLogs())
                    .setIcon(android.R.drawable.ic_menu_recent_history)
                    .create();

            dialog.show();

            // OPÇÃO 4: Gradiente elegante para logs
             GradientDrawable gradient = new GradientDrawable();
             gradient.setColors(new int[]{
                 getResources().getColor(R.color.primary_dark_blue),
                 getResources().getColor(R.color.blue_medium)
             });
             gradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
             gradient.setCornerRadius(15f);
             dialog.getWindow().setBackgroundDrawable(gradient);

            // ✅ PERSONALIZAR COR DOS BOTÕES
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.white));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(android.R.color.white));

        } catch (Exception e) {
            // ✅ MENSAGEM DE ERRO TAMBÉM COM COR PERSONALIZADA
            AlertDialog errorDialog = new AlertDialog.Builder(this)
                    .setTitle("❌ Erro")
                    .setMessage("Erro ao carregar histórico: " + e.getMessage())
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            errorDialog.show();

            // Cor de fundo para erro (vermelho)
            errorDialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_red_dark);
            errorDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    /**
     * Confirma limpeza apenas dos logs
     */
    private void confirmClearLogs() {

        try {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Limpar Histórico")
                    .setMessage("Apagar todo o histórico de bloqueios?\n\nEsta ação não pode ser desfeita.")
                    .setPositiveButton("Limpar", (dialogInterface, which) -> {
                        try {
                            blockLogger.clearAllLogs();
                            showMessage("Histórico limpo com sucesso");
                            loadTodayStats(); // Atualizar estatísticas
                        } catch (Exception e) {
                            showMessage("Erro ao limpar histórico");
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .create();

            // ✅ MOSTRAR O DIÁLOGO
            dialog.show();

            // ✅ OPÇÃO 4: GRADIENTE PERSONALIZADO PARA CONFIRMAÇÃO DE EXCLUSÃO
            GradientDrawable gradient = new GradientDrawable();
            gradient.setColors(new int[]{
                    // Do vermelho escuro para vermelho mais claro (efeito de aviso elegante)
                    getResources().getColor(R.color.primary_dark_blue),
                    getResources().getColor(R.color.blue_medium)
            });

            // Direção do gradiente (de cima para baixo)
            gradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);

            // Bordas arredondadas para ficar mais moderno
            gradient.setCornerRadius(20f);

            // Aplicar o gradiente como fundo
            dialog.getWindow().setBackgroundDrawable(gradient);

            // ✅ PERSONALIZAR COR DOS BOTÕES (branco para contrastar com o gradiente)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.white));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.white));

        } catch (Exception e) {
            showMessage("Erro ao mostrar confirmação");
        }
    }

    /**
     * Mostra mensagem na tela
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Abre as configurações do sistema para definir o launcher padrão
     */
    private void openLauncherSettings() {
        try {
            // Abrir as configurações de launcher
            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);

        } catch (Exception e) {
            // Se não conseguir abrir as configurações de HOME, tentar abrir as configurações gerais de apps padrão
            try {
                Intent fallbackIntent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                startActivity(fallbackIntent);

            } catch (Exception e2) {
                showMessage("Erro ao abrir configurações de launcher");
            }
        }
    }

    /**
     * Atualizar dados quando voltar para a tela
     */
    @Override
    protected void onResume() {
        super.onResume();

        try {
            loadCurrentSettings();
            checkAllPermissions();
        } catch (Exception e) {
        }
    }
}