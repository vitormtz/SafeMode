package com.example.safemode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SafeModeLauncherActivity extends AppCompatActivity {

    private RecyclerView recyclerViewApps;
    private LauncherAppAdapter adapter;
    private EditText searchBar;
    private TextView tvTime;
    private TextView tvDate;
    private AppPreferences preferences;
    private List<LauncherAppInfo> allApps;
    private BroadcastReceiver packageChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        preferences = new AppPreferences(this);
        allApps = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupSearchBar();
        setupPackageChangeReceiver();
        loadApps();
        updateDateTime();
    }

    private void initializeViews() {
        recyclerViewApps = findViewById(R.id.recycler_apps);
        searchBar = findViewById(R.id.search_bar);
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerViewApps.setLayoutManager(layoutManager);

        adapter = new LauncherAppAdapter(this, new ArrayList<>(), app -> launchApp(app));
        recyclerViewApps.setAdapter(adapter);
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupPackageChangeReceiver() {
        packageChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadApps();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(packageChangeReceiver, filter);
    }

    private void loadApps() {
        new Thread(() -> {
            List<LauncherAppInfo> apps = getInstalledApps();

            runOnUiThread(() -> {
                allApps.clear();
                allApps.addAll(apps);
                filterApps(searchBar.getText().toString());
            });
        }).start();
    }

    private List<LauncherAppInfo> getInstalledApps() {
        List<LauncherAppInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);

        boolean isHideModeActive = preferences.isHideModeActive();
        Set<String> hiddenApps = preferences.getHiddenApps();

        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;

            // Se modo oculto ativo, ocultar o próprio SafeMode
            if (isHideModeActive && packageName.equals(getPackageName())) {
                continue;
            }

            // Se modo oculto ativo, filtrar apps da lista de ocultos
            if (isHideModeActive && hiddenApps.contains(packageName)) {
                continue;
            }

            String appName = resolveInfo.loadLabel(pm).toString();
            Drawable icon = resolveInfo.loadIcon(pm);

            LauncherAppInfo appInfo = new LauncherAppInfo();
            appInfo.packageName = packageName;
            appInfo.appName = appName;
            appInfo.icon = icon;
            appInfo.activityName = resolveInfo.activityInfo.name;

            apps.add(appInfo);
        }

        Collections.sort(apps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));

        return apps;
    }

    private void filterApps(String query) {
        List<LauncherAppInfo> filteredApps = new ArrayList<>();

        if (query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String lowerQuery = query.toLowerCase();
            for (LauncherAppInfo app : allApps) {
                if (app.appName.toLowerCase().contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
        }

        adapter.updateApps(filteredApps);
    }

    private void launchApp(LauncherAppInfo app) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(app.packageName);
            if (intent != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSafeModeSettings() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());

        tvTime.setText(timeFormat.format(new Date()));
        tvDate.setText(dateFormat.format(new Date()));

        // Atualizar a cada minuto
        tvTime.postDelayed(this::updateDateTime, 60000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
        updateDateTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (packageChangeReceiver != null) {
            try {
                unregisterReceiver(packageChangeReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // No launcher, botão voltar não faz nada (comportamento padrão de launcher)
    }
}
