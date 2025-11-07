package com.example.safemode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public class AppPreferences {

    private static final String TAG = "AppPreferences";
    private static final String PREF_NAME = "SafeModePrefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";
    private static final String KEY_SAFE_MODE_ENABLED = "safe_mode_enabled";
    private static final String KEY_ALLOWED_LATITUDE = "allowed_latitude";
    private static final String KEY_ALLOWED_LONGITUDE = "allowed_longitude";
    private static final String KEY_ALLOWED_RADIUS = "allowed_radius";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_LOCK_SCREEN_ENABLED = "lock_screen_enabled";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";
    private static final String KEY_HIDE_MODE_ACTIVE = "hide_mode_active";

    private SharedPreferences preferences;

    public AppPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

     public void setBlockedApps(Set<String> blockedApps) {
        Log.d(TAG, "💾 ===== SALVANDO APPS BLOQUEADOS =====");
        Log.d(TAG, "💾 Quantidade: " + blockedApps.size());

        // Log detalhado de cada app
        int i = 1;
        for (String app : blockedApps) {
            Log.d(TAG, "💾 App " + i + ": [" + app + "] (length: " + app.length() + ")");
            i++;
        }

        SharedPreferences.Editor editor = preferences.edit();

        // Remover a chave primeiro, depois salvar
        editor.remove(KEY_BLOCKED_APPS);
        editor.apply();

        // Criar uma nova cópia do Set com strings "limpas"
        Set<String> cleanSet = new HashSet<>();
        for (String app : blockedApps) {
            String cleanApp = app.trim(); // Remove espaços
            cleanSet.add(cleanApp);
            Log.d(TAG, "💾 App limpo: [" + cleanApp + "]");
        }

        editor.putStringSet(KEY_BLOCKED_APPS, cleanSet);
        editor.apply();

        Log.d(TAG, "✅ Apps salvos com sucesso: " + cleanSet.size() + " apps");
        Log.d(TAG, "💾 ===== FIM SALVAMENTO =====");
    }

    /**
     * Pega a lista de apps bloqueados - VERSÃO COM DEBUG
     */
    public Set<String> getBlockedApps() {
        Set<String> originalSet = preferences.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
        Set<String> copySet = new HashSet<>(originalSet);

        Log.d(TAG, "📋 ===== CARREGANDO APPS BLOQUEADOS =====");
        Log.d(TAG, "📋 Quantidade carregada: " + copySet.size());

        int i = 1;
        for (String app : copySet) {
            Log.d(TAG, "📋 App " + i + ": [" + app + "] (length: " + app.length() + ")");
            i++;
        }

        Log.d(TAG, "📋 ===== FIM CARREGAMENTO =====");
        return copySet;
    }

    /**
     * Verifica se um app específico está bloqueado - VERSÃO COM DEBUG SUPER DETALHADO
     */
    public boolean isAppBlocked(String packageName) {
        Log.d(TAG, "🔍 ===== VERIFICANDO BLOQUEIO =====");
        Log.d(TAG, "🔍 Package para verificar: [" + packageName + "] (length: " + packageName.length() + ")");

        // Limpar o package name de entrada
        String cleanPackageName = packageName.trim();
        Log.d(TAG, "🔍 Package limpo: [" + cleanPackageName + "] (length: " + cleanPackageName.length() + ")");

        Set<String> blockedApps = getBlockedApps();

        // Verificação SUPER DETALHADA
        Log.d(TAG, "🔍 ----- COMPARANDO COM CADA APP -----");

        boolean found = false;
        int i = 1;
        for (String blockedApp : blockedApps) {
            Log.d(TAG, "🔍 Comparação " + i + ":");
            Log.d(TAG, "🔍   Procurando: [" + cleanPackageName + "]");
            Log.d(TAG, "🔍   Na lista  : [" + blockedApp + "]");
            Log.d(TAG, "🔍   Iguais equals()? " + cleanPackageName.equals(blockedApp));
            Log.d(TAG, "🔍   Iguais contains()? " + blockedApp.contains(cleanPackageName));
            Log.d(TAG, "🔍   Iguais equalsIgnoreCase()? " + cleanPackageName.equalsIgnoreCase(blockedApp));

            // 🔧 TESTE: Comparação byte a byte
            if (cleanPackageName.length() == blockedApp.length()) {
                Log.d(TAG, "🔍   Mesmo tamanho - comparando byte a byte...");
                boolean bytesIguais = true;
                for (int j = 0; j < cleanPackageName.length(); j++) {
                    char c1 = cleanPackageName.charAt(j);
                    char c2 = blockedApp.charAt(j);
                    if (c1 != c2) {
                        Log.d(TAG, "🔍   Diferença na posição " + j + ": '" + c1 + "' != '" + c2 + "'");
                        bytesIguais = false;
                        break;
                    }
                }
                Log.d(TAG, "🔍   Bytes iguais? " + bytesIguais);
            }

            if (cleanPackageName.equals(blockedApp)) {
                found = true;
                Log.d(TAG, "🔍 ✅ ENCONTRADO! App está na lista de bloqueio");
                break;
            }

            i++;
        }

        if (!found) {
            Log.d(TAG, "🔍 ❌ NÃO ENCONTRADO! App NÃO está na lista de bloqueio");

            // 🔧 TESTE ADICIONAL: Verificar se contém parcialmente
            Log.d(TAG, "🔍 ----- TESTE ADICIONAL: BUSCA PARCIAL -----");
            for (String blockedApp : blockedApps) {
                if (blockedApp.contains(cleanPackageName) || cleanPackageName.contains(blockedApp)) {
                    Log.d(TAG, "🔍 ⚠️ MATCH PARCIAL encontrado: [" + blockedApp + "] contém [" + cleanPackageName + "]");
                }
            }
        }

        Log.d(TAG, "🔍 RESULTADO FINAL: " + found);
        Log.d(TAG, "🔍 ===== FIM VERIFICAÇÃO =====");

        return found;
    }

    /**
     * Adiciona um app à lista de bloqueados - VERSÃO CORRIGIDA
     */
    public void addBlockedApp(String packageName) {
        Log.d(TAG, "➕ Adicionando app à lista: [" + packageName + "]");

        // Limpar o package name
        String cleanPackageName = packageName.trim();

        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(cleanPackageName);
        setBlockedApps(blockedApps);

        Log.d(TAG, "✅ App adicionado: [" + cleanPackageName + "]");
    }

    /**
     * Remove um app da lista de bloqueados - VERSÃO CORRIGIDA
     */
    public void removeBlockedApp(String packageName) {
        Log.d(TAG, "➖ Removendo app da lista: [" + packageName + "]");

        String cleanPackageName = packageName.trim();
        Set<String> blockedApps = getBlockedApps();
        blockedApps.remove(cleanPackageName);
        setBlockedApps(blockedApps);

        Log.d(TAG, "✅ App removido: [" + cleanPackageName + "]");
    }

    // ===== RESTO DOS MÉTODOS IGUAIS =====

    public void setSafeModeEnabled(boolean enabled) {
        Log.d(TAG, "⚙️ Definindo Safe Mode ativo: " + enabled);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SAFE_MODE_ENABLED, enabled);
        editor.apply();
    }

    public boolean isSafeModeEnabled() {
        boolean enabled = preferences.getBoolean(KEY_SAFE_MODE_ENABLED, false);
        Log.d(TAG, "❓ Safe Mode está ativo? " + enabled);
        return enabled;
    }

    public void setAllowedLatitude(double latitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ALLOWED_LATITUDE, String.valueOf(latitude)); // ← STRING
        editor.apply();
    }

    public double getAllowedLatitude() {
        try {
            String latString = preferences.getString(KEY_ALLOWED_LATITUDE, "0.0");
            double value = Double.parseDouble(latString);
            Log.d(TAG, "📖 getAllowedLatitude (STRING): " + value);
            return value;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao ler latitude: " + e.getMessage());
            return 0.0;
        }
    }

    public void setAllowedLongitude(double longitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ALLOWED_LONGITUDE, String.valueOf(longitude)); // ← STRING
        editor.apply();
    }

    public double getAllowedLongitude() {
        try {
            String lngString = preferences.getString(KEY_ALLOWED_LONGITUDE, "0.0");
            double value = Double.parseDouble(lngString);
            Log.d(TAG, "📖 getAllowedLongitude (STRING): " + value);
            return value;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao ler longitude: " + e.getMessage());
            return 0.0;
        }
    }


    public void setAllowedRadius(int radiusInMeters) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_ALLOWED_RADIUS, radiusInMeters);
        editor.apply();
    }

    public int getAllowedRadius() {
        int value = preferences.getInt(KEY_ALLOWED_RADIUS, 100);
        Log.d(TAG, "📖 getAllowedRadius: " + value);
        return value;
    }

    public void setLocationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOCATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isLocationEnabled() {
        boolean value = preferences.getBoolean(KEY_LOCATION_ENABLED, false);
        Log.d(TAG, "📖 isLocationEnabled: " + value);
        return value;
    }

    public void setAllowedLocation(double latitude, double longitude, int radiusInMeters) {
        Log.d(TAG, "💾 setAllowedLocation chamado (versão DOUBLE):");
        Log.d(TAG, "   Recebido Lat: " + latitude);
        Log.d(TAG, "   Recebido Lng: " + longitude);
        Log.d(TAG, "   Recebido Raio: " + radiusInMeters);

        try {
            SharedPreferences.Editor editor = preferences.edit();

            // 🔧 CORREÇÃO: Salvar como STRING para manter precisão total
            editor.putString(KEY_ALLOWED_LATITUDE, String.valueOf(latitude));
            editor.putString(KEY_ALLOWED_LONGITUDE, String.valueOf(longitude));
            editor.putInt(KEY_ALLOWED_RADIUS, radiusInMeters);
            editor.putBoolean(KEY_LOCATION_ENABLED, true);

            boolean success = editor.commit();

            Log.d(TAG, "💾 Commit result: " + success);

            if (success) {
                Log.d(TAG, "✅ Salvamento bem-sucedido (como STRING)");
            } else {
                Log.e(TAG, "❌ Falha no commit!");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Exceção ao salvar: " + e.getMessage(), e);
        }
    }


    public void setLockScreenEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOCK_SCREEN_ENABLED, enabled);
        editor.apply();
    }

    public boolean isLockScreenEnabled() {
        return preferences.getBoolean(KEY_LOCK_SCREEN_ENABLED, false);
    }

    public void clearAllPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // ===== MÉTODOS PARA APLICATIVOS OCULTOS =====

    public void setHiddenApps(Set<String> hiddenApps) {
        Log.d(TAG, "💾 ===== SALVANDO APPS OCULTOS =====");
        Log.d(TAG, "💾 Quantidade: " + hiddenApps.size());

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_HIDDEN_APPS);
        editor.apply();

        Set<String> cleanSet = new HashSet<>();
        for (String app : hiddenApps) {
            String cleanApp = app.trim();
            cleanSet.add(cleanApp);
            Log.d(TAG, "💾 App oculto: [" + cleanApp + "]");
        }

        editor.putStringSet(KEY_HIDDEN_APPS, cleanSet);
        editor.apply();

        Log.d(TAG, "✅ Apps ocultos salvos: " + cleanSet.size() + " apps");
        Log.d(TAG, "💾 ===== FIM SALVAMENTO APPS OCULTOS =====");
    }

    public Set<String> getHiddenApps() {
        Set<String> originalSet = preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>());
        Set<String> copySet = new HashSet<>(originalSet);

        Log.d(TAG, "📋 ===== CARREGANDO APPS OCULTOS =====");
        Log.d(TAG, "📋 Quantidade carregada: " + copySet.size());

        int i = 1;
        for (String app : copySet) {
            Log.d(TAG, "📋 App oculto " + i + ": [" + app + "]");
            i++;
        }

        Log.d(TAG, "📋 ===== FIM CARREGAMENTO APPS OCULTOS =====");
        return copySet;
    }

    public boolean isAppHidden(String packageName) {
        String cleanPackageName = packageName.trim();
        Set<String> hiddenApps = getHiddenApps();
        return hiddenApps.contains(cleanPackageName);
    }

    public void addHiddenApp(String packageName) {
        Log.d(TAG, "➕ Adicionando app à lista de ocultos: [" + packageName + "]");
        String cleanPackageName = packageName.trim();
        Set<String> hiddenApps = getHiddenApps();
        hiddenApps.add(cleanPackageName);
        setHiddenApps(hiddenApps);
        Log.d(TAG, "✅ App adicionado aos ocultos: [" + cleanPackageName + "]");
    }

    public void removeHiddenApp(String packageName) {
        Log.d(TAG, "➖ Removendo app da lista de ocultos: [" + packageName + "]");
        String cleanPackageName = packageName.trim();
        Set<String> hiddenApps = getHiddenApps();
        hiddenApps.remove(cleanPackageName);
        setHiddenApps(hiddenApps);
        Log.d(TAG, "✅ App removido dos ocultos: [" + cleanPackageName + "]");
    }

    public void setHideModeActive(boolean active) {
        Log.d(TAG, "⚙️ Definindo modo oculto ativo: " + active);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_HIDE_MODE_ACTIVE, active);
        editor.apply();
    }

    public boolean isHideModeActive() {
        boolean active = preferences.getBoolean(KEY_HIDE_MODE_ACTIVE, false);
        Log.d(TAG, "❓ Modo oculto está ativo? " + active);
        return active;
    }
}