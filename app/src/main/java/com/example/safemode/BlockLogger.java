package com.example.safemode;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BlockLogger {

    private static final String PREF_NAME = "BlockLog";
    private static final String KEY_LOG_ENTRIES = "log_entries";
    private static final int MAX_LOG_ENTRIES = 1000; // Máximo de registros

    private Context context;
    private SharedPreferences preferences;

    public BlockLogger(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Registra quando um app foi bloqueado
     * É como escrever no diário: "Bloqueei o WhatsApp às 14:30"
     */
    public void logBlock(String packageName, long timestamp) {
        try {
            // Criar um novo registro
            JSONObject logEntry = new JSONObject();
            logEntry.put("package_name", packageName);
            logEntry.put("timestamp", timestamp);
            logEntry.put("app_name", getAppName(packageName));
            logEntry.put("readable_time", formatTimestamp(timestamp));

            // Pegar os registros existentes
            List<JSONObject> existingLogs = getLogEntries();

            // Adicionar o novo registro no início da lista
            existingLogs.add(0, logEntry);

            // Limitar o número de registros (para não ocupar muito espaço)
            if (existingLogs.size() > MAX_LOG_ENTRIES) {
                existingLogs = existingLogs.subList(0, MAX_LOG_ENTRIES);
            }

            // Salvar de volta
            saveLogEntries(existingLogs);

        } catch (JSONException e) {
        }
    }

    /**
     * Pega todos os registros de bloqueio
     */
    public List<JSONObject> getLogEntries() {
        List<JSONObject> entries = new ArrayList<>();

        try {
            String jsonString = preferences.getString(KEY_LOG_ENTRIES, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                entries.add(jsonArray.getJSONObject(i));
            }

        } catch (JSONException e) {
        }

        return entries;
    }

    /**
     * Pega registros de um período específico
     */
    public List<JSONObject> getLogEntriesInPeriod(long startTime, long endTime) {
        List<JSONObject> allEntries = getLogEntries();
        List<JSONObject> filteredEntries = new ArrayList<>();

        try {
            for (JSONObject entry : allEntries) {
                long timestamp = entry.getLong("timestamp");
                if (timestamp >= startTime && timestamp <= endTime) {
                    filteredEntries.add(entry);
                }
            }
        } catch (JSONException e) {
        }

        return filteredEntries;
    }

    /**
     * Conta quantas vezes um app foi bloqueado
     */
    public int getBlockCountForApp(String packageName) {
        List<JSONObject> entries = getLogEntries();
        int count = 0;

        try {
            for (JSONObject entry : entries) {
                if (packageName.equals(entry.getString("package_name"))) {
                    count++;
                }
            }
        } catch (JSONException e) {
        }

        return count;
    }

    /**
     * Pega estatísticas dos bloqueios de hoje
     */
    public JSONObject getTodayStats() {
        JSONObject stats = new JSONObject();

        try {
            // Definir o início e fim do dia de hoje
            long startOfDay = getStartOfDay(System.currentTimeMillis());
            long endOfDay = startOfDay + (24 * 60 * 60 * 1000); // 24 horas

            List<JSONObject> todayEntries = getLogEntriesInPeriod(startOfDay, endOfDay);

            stats.put("total_blocks_today", todayEntries.size());
            stats.put("apps_blocked_today", getUniqueAppsCount(todayEntries));
            stats.put("most_blocked_app", getMostBlockedApp(todayEntries));

        } catch (JSONException e) {
        }

        return stats;
    }

    /**
     * Limpa todos os registros
     */
    public void clearAllLogs() {
        preferences.edit().remove(KEY_LOG_ENTRIES).apply();
    }

    /**
     * Limpa registros antigos (mais de 30 dias)
     */
    public void cleanOldLogs() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        List<JSONObject> entries = getLogEntries();
        List<JSONObject> recentEntries = new ArrayList<>();

        try {
            for (JSONObject entry : entries) {
                long timestamp = entry.getLong("timestamp");
                if (timestamp > thirtyDaysAgo) {
                    recentEntries.add(entry);
                }
            }

            saveLogEntries(recentEntries);

        } catch (JSONException e) {
        }
    }

    /**
     * Salva a lista de registros
     */
    private void saveLogEntries(List<JSONObject> entries) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (JSONObject entry : entries) {
                jsonArray.put(entry);
            }

            preferences.edit()
                    .putString(KEY_LOG_ENTRIES, jsonArray.toString())
                    .apply();

        } catch (Exception e) {
        }
    }

    /**
     * Pega o nome amigável do app
     */
    private String getAppName(String packageName) {
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();
            android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();

        } catch (Exception e) {
            return packageName; // Se não conseguir, usar o nome do pacote
        }
    }

    /**
     * Formata timestamp para texto legível
     */
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    /**
     * Pega o início do dia (00:00:00)
     */
    private long getStartOfDay(long timestamp) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Conta quantos apps únicos foram bloqueados
     */
    private int getUniqueAppsCount(List<JSONObject> entries) {
        java.util.Set<String> uniqueApps = new java.util.HashSet<>();

        try {
            for (JSONObject entry : entries) {
                uniqueApps.add(entry.getString("package_name"));
            }
        } catch (JSONException e) {
        }

        return uniqueApps.size();
    }

    /**
     * Encontra o app mais bloqueado em uma lista de registros
     */
    private String getMostBlockedApp(List<JSONObject> entries) {
        java.util.Map<String, Integer> appCounts = new java.util.HashMap<>();

        try {
            // Contar bloqueios por app
            for (JSONObject entry : entries) {
                String packageName = entry.getString("package_name");
                appCounts.put(packageName, appCounts.getOrDefault(packageName, 0) + 1);
            }

            // Encontrar o app com mais bloqueios
            String mostBlockedApp = null;
            int maxCount = 0;

            for (java.util.Map.Entry<String, Integer> entry : appCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostBlockedApp = entry.getKey();
                }
            }

            return mostBlockedApp != null ? getAppName(mostBlockedApp) : "Nenhum";

        } catch (JSONException e) {
            return "Erro";
        }
    }
}