package com.example.safemode;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private ProgressBar progressBar;
    private TextView tvTitle;
    private Button btnSave;
    private Button btnBack;
    private AppPreferences preferences;
    private List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_apps_selection);

        preferences = new AppPreferences(this);
        appList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadInstalledApps();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_apps);
        progressBar = findViewById(R.id.progress_bar);
        tvTitle = findViewById(R.id.tv_title);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);

        btnSave.setOnClickListener(v -> saveHiddenApps());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(appList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        new Thread(() -> {
            List<AppInfo> apps = getInstalledApps();

            runOnUiThread(() -> {
                appList.clear();
                appList.addAll(apps);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
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

    private void saveHiddenApps() {
        try {
            Set<String> hiddenAppsSet = new java.util.HashSet<>();

            for (AppInfo app : appList) {
                if (app.isBlocked) {
                    hiddenAppsSet.add(app.packageName);
                }
            }

            preferences.setHiddenApps(hiddenAppsSet);

            Toast.makeText(this, "Apps para ocultar configurados: " + hiddenAppsSet.size(), Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar apps", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAppToggled(AppInfo appInfo, boolean isBlocked) {
        // Listener para quando um app é marcado/desmarcado
        // Não precisa fazer nada aqui, pois o estado já é atualizado no adapter
    }
}
