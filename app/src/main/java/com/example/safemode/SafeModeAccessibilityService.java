package com.example.safemode;

import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Serviço de acessibilidade - VERSÃO CORRIGIDA
 * Resolve problema de localização após app ficar fechado
 */
public class SafeModeAccessibilityService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "SafeModeAccessibility";
    private AppPreferences preferences;
    private LocationManager locationManager;
    private long lastLocationUpdate = 0;
    private static final long LOCATION_TIMEOUT = 10000;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        try {
            // Inicializar componentes
            preferences = new AppPreferences(this);
            locationManager = new LocationManager(this);

        } catch (Exception e) {
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            // Verificar se é o tipo de evento que queremos
            if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                return;
            }

            // Pegar o nome do pacote
            String packageName = null;
            if (event.getPackageName() != null) {
                packageName = event.getPackageName().toString();
            }

            if (packageName == null || packageName.isEmpty()) {
                return;
            }

            // Verificar se Safe Mode está ativo
            if (!preferences.isSafeModeEnabled()) {
                return;
            }

            // Verificar se é nosso próprio app
            if (packageName.equals(getPackageName())) {
                return;
            }

            // Verificar se é app do sistema crítico
            if (isSystemApp(packageName)) {
                return;
            }

            // ✅ CORREÇÃO: Verificar modo oculto primeiro
            boolean isHideModeActive = preferences.isHideModeActive();

            if (isHideModeActive) {
                // Se modo oculto ativo, bloquear apps da lista de ocultos
                if (preferences.isAppHidden(packageName)) {
                    blockAppWithActivity(packageName);
                    return; // App oculto foi bloqueado, não precisa continuar
                }
                // ✅ BUG FIX: Não retornar aqui! Continuar para verificar apps bloqueados normais
            }

            // Verificar se está na lista de bloqueados (lógica normal)
            if (!preferences.isAppBlocked(packageName)) {
                return;
            }

            // ✅ CORREÇÃO PRINCIPAL: Nova lógica de verificação de localização
            if (shouldBlockBasedOnLocation()) {
                blockAppWithActivity(packageName);
            }

        } catch (Exception e) {
        }
    }

    /**
     * ✅ NOVO MÉTODO: Lógica melhorada para decidir se deve bloquear
     */
    private boolean shouldBlockBasedOnLocation() {

        // Se controle por localização está desligado, sempre bloquear
        if (!preferences.isLocationEnabled()) {
            return true;
        }

        // Verificar se há área configurada
        double allowedLat = preferences.getAllowedLatitude();
        double allowedLng = preferences.getAllowedLongitude();
        int allowedRadius = preferences.getAllowedRadius();

        if (allowedLat == 0.0 && allowedLng == 0.0) {
            return true;
        }

        // ✅ CORREÇÃO: Tentar obter localização com timeout
        android.location.Location currentLoc = getCurrentLocationWithTimeout();

        if (currentLoc == null) {
            return true; // ← CORRIGIDO: Bloquear quando não consegue localização
        }

        // Calcular distância
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                currentLoc.getLatitude(),
                currentLoc.getLongitude(),
                allowedLat,
                allowedLng,
                results
        );

        float distance = results[0];
        boolean isOutside = distance > allowedRadius;

        lastLocationUpdate = System.currentTimeMillis();

        return isOutside;
    }

    /**
     * ✅ NOVO MÉTODO: Obtém localização com timeout e tentativas múltiplas
     */
    private android.location.Location getCurrentLocationWithTimeout() {

        try {
            // Primeiro, tentar localização atual do LocationManager
            android.location.Location currentLoc = locationManager.getCurrentLocation();

            if (currentLoc != null) {
                long locationAge = System.currentTimeMillis() - currentLoc.getTime();

                // Se a localização é muito antiga (mais de 5 minutos), tentar atualizar
                if (locationAge > 300000) { // 5 minutos
                    locationManager.getLocationOnce();

                    // Aguardar um pouco para nova localização
                    try {
                        Thread.sleep(3000);
                        android.location.Location newLoc = locationManager.getCurrentLocation();
                        if (newLoc != null && newLoc.getTime() > currentLoc.getTime()) {
                            return newLoc;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                return currentLoc;
            }

            // Se não tem localização, tentar obter uma nova
            locationManager.getLocationOnce();

            // Aguardar até 8 segundos por uma nova localização
            for (int i = 0; i < 8; i++) {
                try {
                    Thread.sleep(1000);
                    currentLoc = locationManager.getCurrentLocation();
                    if (currentLoc != null) {
                        return currentLoc;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Bloqueia app - VERSÃO INALTERADA
     */
    private void blockAppWithActivity(String packageName) {

        try {
            Intent blockIntent = new Intent(this, SimpleBlockActivity.class);
            blockIntent.putExtra("blocked_package", packageName);
            blockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            startActivity(blockIntent);

            // Iniciar verificação automática
            startBlockVerification(packageName);

            // Registrar bloqueio
            logBlockedApp(packageName);
        } catch (Exception e) {
        }
    }

    /**
     * Verifica se é app do sistema CRÍTICO - VERSÃO INALTERADA
     */
    private boolean isSystemApp(String packageName) {
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
                "com.android.incallui",
                "com.example.safemode"
        };

        for (String criticalApp : criticalSystemApps) {
            if (packageName.equals(criticalApp) || packageName.startsWith(criticalApp + ".")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Inicia serviço de verificação automática do bloqueio
     */
    private void startBlockVerification(String packageName) {
        try {
            Intent verificationIntent = new Intent(this, BlockVerificationService.class);
            verificationIntent.putExtra("target_package", packageName);
            startService(verificationIntent);
        } catch (Exception e) {
        }
    }

    /**
     * Registra bloqueio no log
     */
    private void logBlockedApp(String packageName) {
        try {
            BlockLogger logger = new BlockLogger(this);
            logger.logBlock(packageName, System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}