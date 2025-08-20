package com.example.safemode;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import java.util.List;

/**
 * Utilitários para trabalhar com estatísticas de uso de apps
 * É como ter um contador que vê quais apps são mais usados
 */
public class UsageStatsUtils {

    /**
     * Verifica se temos permissão para ver estatísticas de uso
     * É como perguntar: "posso ver o relatório de uso dos apps?"
     */
    public static boolean hasUsageStatsPermission(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true; // Versões antigas não precisam dessa permissão
        }

        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName()
            );

            return mode == AppOpsManager.MODE_ALLOWED;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Pega o app que está em primeiro plano agora
     * É como perguntar: "qual app está sendo usado agora?"
     */
    public static String getCurrentForegroundApp(Context context) {

        if (!hasUsageStatsPermission(context)) {
            return null;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return getForegroundAppLegacy(context);
        }

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager)
                    context.getSystemService(Context.USAGE_STATS_SERVICE);

            long currentTime = System.currentTimeMillis();

            // Pegar estatísticas dos últimos 10 segundos
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    currentTime - 10000, // 10 segundos atrás
                    currentTime
            );

            if (usageStatsList == null || usageStatsList.isEmpty()) {
                return null;
            }

            // Encontrar o app usado mais recentemente
            UsageStats mostRecentApp = null;
            for (UsageStats usageStats : usageStatsList) {
                if (mostRecentApp == null ||
                        usageStats.getLastTimeUsed() > mostRecentApp.getLastTimeUsed()) {
                    mostRecentApp = usageStats;
                }
            }

            return mostRecentApp != null ? mostRecentApp.getPackageName() : null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Método para versões antigas do Android (antes do Lollipop)
     */
    private static String getForegroundAppLegacy(Context context) {

        try {
            android.app.ActivityManager activityManager = (android.app.ActivityManager)
                    context.getSystemService(Context.ACTIVITY_SERVICE);

            List<android.app.ActivityManager.RunningTaskInfo> tasks =
                    activityManager.getRunningTasks(1);

            if (tasks != null && !tasks.isEmpty()) {
                return tasks.get(0).topActivity.getPackageName();
            }

        } catch (Exception e) {
            // Ignorar erros em versões antigas
        }

        return null;
    }

    /**
     * Verifica se um app específico está em primeiro plano
     */
    public static boolean isAppInForeground(Context context, String packageName) {

        String currentApp = getCurrentForegroundApp(context);
        return packageName != null && packageName.equals(currentApp);
    }

    /**
     * Pega uma lista dos apps mais usados
     */
    public static List<UsageStats> getMostUsedApps(Context context, long startTime, long endTime) {

        if (!hasUsageStatsPermission(context) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager)
                    context.getSystemService(Context.USAGE_STATS_SERVICE);

            return usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
            );

        } catch (Exception e) {
            return null;
        }
    }
}