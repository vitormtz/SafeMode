package com.example.safemode;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AppSelectionActivity - VERSÃO COM CONTAINER LinearLayout
 * Agora os apps aparecem dentro de uma caixinha personalizada!
 */
public class AppSelectionActivity extends AppCompatActivity {

    private static final String TAG = "AppSelectionActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private LinearLayout loadingLayout;
    private LinearLayout containerApps; // ✨ NOVA: Caixinha para os apps
    private TextView containerTitle;    // ✨ NOVA: Título da caixinha
    private AppListAdapter adapter;
    private AppPreferences preferences;
    private List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_app_selection);

        Log.d(TAG, "🚀 AppSelectionActivity iniciada - VERSÃO COM CONTAINER");

        setupSystemBars();
        initializeViews();
        setupAppsContainer(); // ✨ NOVA: Configurar a caixinha
        loadInstalledApps();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_apps);
        progressBar = findViewById(R.id.progress_loading);
        layoutEmpty = findViewById(R.id.layout_empty);
        loadingLayout = findViewById(R.id.loading_layout);

        // ✨ NOVA: Pegar referências da caixinha
        containerApps = findViewById(R.id.container_apps);
        containerTitle = findViewById(R.id.container_title);

        preferences = new AppPreferences(this);
        appList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(appList, this::onAppToggled);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "✅ Views inicializadas com container");
    }

    /**
     * ✨ NOVA FUNÇÃO: Configura a caixinha dos apps
     */
    private void setupAppsContainer() {
        Log.d(TAG, "🎨 Configurando container dos apps...");

        try {
            if (containerApps != null) {
                // ✨ Personalizar a caixinha
                containerApps.setBackgroundColor(Color.parseColor("#F8FAFC"));
                containerApps.setPadding(24, 20, 24, 20);

                // ✨ Adicionar uma bordinha sutil
                containerApps.setBackground(getResources().getDrawable(R.drawable.card_background));

                // ✨ Adicionar elevação (sombra)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    containerApps.setElevation(4f);
                }

                Log.d(TAG, "✅ Container configurado com estilo");
            }

            if (containerTitle != null) {
                // ✨ Personalizar o título movido para o container
                containerTitle.setTextColor(Color.parseColor("#212121"));
                containerTitle.setTextSize(20f);

                // ✨ Manter título fixo
                updateContainerTitle(0);

                Log.d(TAG, "✅ Título do container configurado (agora dentro do container)");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao configurar container: " + e.getMessage());
        }
    }

    /**
     * ✨ AJUSTADA: Mantém o título fixo
     */
    private void updateContainerTitle(int appCount) {
        if (containerTitle != null) {
            // ✨ TÍTULO FIXO: Não mostra mais contadores
            containerTitle.setText("Selecionar Apps para Bloquear");
        }
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
            Log.e(TAG, "❌ Erro ao configurar barras: " + e.getMessage(), e);
        }
    }

    /**
     * ✨ ATUALIZADA: Mostra estado de carregamento
     */
    private void showLoadingState() {
        Log.d(TAG, "📱 Mostrando estado de carregamento");

        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
        }

        if (containerApps != null) {
            containerApps.setVisibility(View.GONE); // ✨ Esconder caixinha durante carregamento
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * ✨ ATUALIZADA: Mostra lista de apps na caixinha
     */
    private void showAppsList() {
        Log.d(TAG, "📱 Mostrando lista de apps na caixinha");

        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }

        if (containerApps != null) {
            containerApps.setVisibility(View.VISIBLE); // ✨ Mostrar caixinha com apps
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * ✨ ATUALIZADA: Mostra estado vazio
     */
    private void showEmptyState() {
        Log.d(TAG, "📱 Mostrando estado vazio");

        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }

        if (containerApps != null) {
            containerApps.setVisibility(View.GONE); // ✨ Esconder caixinha se não há apps
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void loadInstalledApps() {
        Log.d(TAG, "📲 Iniciando carregamento com queryIntentActivities...");
        showLoadingState();
        new LoadAppsTask().execute();
    }

    private void onAppToggled(AppInfo appInfo, boolean isBlocked) {
        Log.d(TAG, "🔄 App " + appInfo.appName + " " + (isBlocked ? "ADICIONADO" : "REMOVIDO"));

        if (isBlocked) {
            preferences.addBlockedApp(appInfo.packageName);
        } else {
            preferences.removeBlockedApp(appInfo.packageName);
        }

        // ✨ REMOVIDO: Não atualiza mais contador no título
        // updateAppsCounter(); // Função mantida para futuro uso se necessário
    }

    /**
     * ✨ FUNÇÃO MANTIDA: Para futuro uso se necessário
     */
    private void updateAppsCounter() {
        try {
            Set<String> blockedApps = preferences.getBlockedApps();
            int selectedCount = 0;

            // Contar quantos da lista atual estão selecionados
            for (AppInfo app : appList) {
                if (app.isBlocked) {
                    selectedCount++;
                }
            }

            // ✨ REMOVIDO: Não atualiza mais o título
            // Função mantida apenas para debug se necessário

            Log.d(TAG, "📊 Contador: " + selectedCount + " de " + appList.size() + " selecionados");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "💬 Mensagem: " + message);
    }

    /**
     * LoadAppsTask - VERSÃO ATUALIZADA para trabalhar com container
     */
    private class LoadAppsTask extends AsyncTask<Void, Integer, List<AppInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "🔄 LoadAppsTask iniciada");
            runOnUiThread(() -> showLoadingState());
        }

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            Log.d(TAG, "🔄 ===== CARREGANDO APPS PARA CONTAINER =====");

            List<AppInfo> apps = new ArrayList<>();
            Set<String> packageNameSet = new HashSet<>();

            try {
                PackageManager packageManager = getPackageManager();
                Set<String> blockedApps = preferences.getBlockedApps();

                Log.d(TAG, "🔄 Criando Intent para launcher apps...");

                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                Log.d(TAG, "🔄 Executando queryIntentActivities...");
                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);

                Log.d(TAG, "✅ queryIntentActivities retornou: " + resolveInfos.size() + " apps");

                int totalApps = resolveInfos.size();
                int processedApps = 0;
                int addedApps = 0;

                for (ResolveInfo info : resolveInfos) {
                    processedApps++;

                    try {
                        ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
                        String packageName = applicationInfo.packageName;

                        // Publicar progresso a cada 20 apps
                        if (processedApps % 20 == 0) {
                            publishProgress(processedApps, totalApps);
                        }

                        // Verificar duplicatas
                        if (packageNameSet.contains(packageName)) {
                            continue;
                        }
                        packageNameSet.add(packageName);

                        // Verificar se é crítico
                        if (isCriticalSystemApp(packageName)) {
                            continue;
                        }

                        // Verificar se é nosso app
                        if (packageName.equals(getPackageName())) {
                            continue;
                        }

                        // Verificar se tem launcher intent
                        if (!hasLauncherIntent(packageName)) {
                            continue;
                        }

                        // Verificar se está habilitado
                        if (!applicationInfo.enabled) {
                            continue;
                        }

                        // ✨ Criar AppInfo com verificação de bloqueio
                        AppInfo app = new AppInfo();
                        app.packageName = packageName;
                        app.appName = packageManager.getApplicationLabel(applicationInfo).toString();
                        app.icon = packageManager.getApplicationIcon(applicationInfo);
                        app.isBlocked = blockedApps.contains(packageName);
                        app.isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

                        apps.add(app);
                        addedApps++;

                    } catch (Exception e) {
                        Log.w(TAG, "⚠️ Erro ao processar app: " + e.getMessage());
                    }
                }

                // Ordenar por nome
                Collections.sort(apps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));

                Log.d(TAG, "📊 Apps processados: " + processedApps + "/" + totalApps);
                Log.d(TAG, "📊 Apps adicionados ao container: " + addedApps);

            } catch (Exception e) {
                Log.e(TAG, "❌ ERRO no doInBackground: " + e.getMessage(), e);
            }

            return apps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values.length >= 2) {
                Log.d(TAG, "📈 Progresso: " + values[0] + "/" + values[1]);

                // ✨ REMOVIDO: Não atualiza mais título durante carregamento
                // Título permanece fixo agora
            }
        }

        @Override
        protected void onPostExecute(List<AppInfo> apps) {
            Log.d(TAG, "🏁 ===== ONPOSTEXECUTE PARA CONTAINER =====");
            Log.d(TAG, "🏁 Apps recebidos: " + (apps != null ? apps.size() : "NULL"));

            try {
                if (apps == null) {
                    Log.e(TAG, "❌ ERRO: Lista de apps é NULL!");
                    runOnUiThread(() -> showEmptyState());
                    return;
                }

                if (apps.isEmpty()) {
                    Log.w(TAG, "⚠️ Nenhum app encontrado");
                    runOnUiThread(() -> showEmptyState());
                    return;
                }

                // ✨ ATUALIZADA: Atualizar UI com container
                runOnUiThread(() -> {
                    try {
                        // Atualizar lista
                        appList.clear();
                        appList.addAll(apps);

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "✅ Adapter notificado");
                        }

                        // ✨ Mostrar caixinha com apps
                        showAppsList();

                        Log.d(TAG, "✅ UI atualizada com container - " + apps.size() + " apps");

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erro ao atualizar UI: " + e.getMessage(), e);
                        showEmptyState();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ ERRO no onPostExecute: " + e.getMessage(), e);
                runOnUiThread(() -> showEmptyState());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.w(TAG, "⚠️ LoadAppsTask cancelada");
            runOnUiThread(() -> showEmptyState());
        }

        private boolean isCriticalSystemApp(String packageName) {
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
                    "com.android.incallui"
            };

            for (String criticalApp : criticalSystemApps) {
                if (packageName.equals(criticalApp) || packageName.startsWith(criticalApp + ".")) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasLauncherIntent(String packageName) {
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                return launchIntent != null;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * ✨ NOVA FUNÇÃO: Debug do container
     */
    private void debugContainer() {
        Log.d(TAG, "🔍 ===== DEBUG CONTAINER =====");

        if (containerApps != null) {
            Log.d(TAG, "📦 Container visível: " + (containerApps.getVisibility() == View.VISIBLE));
            Log.d(TAG, "📦 Container filhos: " + containerApps.getChildCount());
        }

        if (recyclerView != null) {
            Log.d(TAG, "📋 RecyclerView itens: " + (adapter != null ? adapter.getItemCount() : "adapter null"));
        }

        Log.d(TAG, "📊 Lista apps: " + appList.size());
        Log.d(TAG, "🔍 ===== FIM DEBUG =====");
    }
}