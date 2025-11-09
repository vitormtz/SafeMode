package com.example.safemode;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HiddenAppsSelectionActivity extends AppCompatActivity implements AppListAdapter.OnAppToggleListener {

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private LinearLayout containerApps;
    private LinearLayout loadingLayout;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView containerTitle;
    private Button btnRetry;
    private AppPreferences preferences;
    private List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupSystemBars();
        setContentView(R.layout.activity_hidden_apps_selection);

        preferences = new AppPreferences(this);
        appList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadInstalledApps();
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
        recyclerView = findViewById(R.id.recycler_view_apps);
        containerApps = findViewById(R.id.container_apps);
        loadingLayout = findViewById(R.id.loading_layout);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        containerTitle = findViewById(R.id.container_title);
        btnRetry = findViewById(R.id.btn_retry);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> loadInstalledApps());
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(appList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadInstalledApps() {
        // Mostrar loading e esconder container
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
        }
        if (containerApps != null) {
            containerApps.setVisibility(View.GONE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        new Thread(() -> {
            List<AppInfo> apps = getInstalledApps();

            runOnUiThread(() -> {
                appList.clear();
                appList.addAll(apps);
                adapter.notifyDataSetChanged();

                // Esconder loading
                if (loadingLayout != null) {
                    loadingLayout.setVisibility(View.GONE);
                }

                // Mostrar container com apps ou mensagem de vazio
                if (apps.isEmpty()) {
                    if (layoutEmpty != null) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (containerApps != null) {
                        containerApps.setVisibility(View.VISIBLE);
                    }
                }
            });
        }).start();
    }

    private List<AppInfo> getInstalledApps() {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        Set<String> hiddenApps = preferences.getHiddenApps();

        for (ApplicationInfo appInfo : installedApps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String packageName = appInfo.packageName;

                // Não mostrar o próprio SafeMode na lista de apps para ocultar
                if (packageName.equals(getPackageName())) {
                    continue;
                }

                String appName = appInfo.loadLabel(pm).toString();
                Drawable icon = appInfo.loadIcon(pm);

                boolean isHidden = hiddenApps.contains(packageName);

                AppInfo info = new AppInfo(packageName, appName, icon);
                info.isBlocked = isHidden;
                apps.add(info);
            }
        }

        Collections.sort(apps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));

        return apps;
    }

    @Override
    public void onAppToggled(AppInfo appInfo, boolean isBlocked) {
        // Listener para quando um app é marcado/desmarcado
        // Não precisa fazer nada aqui, pois o estado já é atualizado no adapter
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Salvar automaticamente quando o usuário sair da tela
        saveHiddenApps();
    }

    private void saveHiddenApps() {
        try {
            Set<String> hiddenAppsSet = new java.util.HashSet<>();

            for (AppInfo app : appList) {
                if (app.isBlocked) {
                    hiddenAppsSet.add(app.packageName);
                }
            }

            preferences.setHiddenApps(hiddenAppsSet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
