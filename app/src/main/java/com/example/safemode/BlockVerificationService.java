package com.example.safemode;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * Serviço de Verificação Automática do Bloqueio
 * Monitora se o bloqueio está funcionando corretamente e força correção se necessário
 */
public class BlockVerificationService extends Service {

    private static final String TAG = "BlockVerificationService";
    private static final int MAX_VERIFICATION_ATTEMPTS = 5; // Máximo 5 tentativas
    private static final long VERIFICATION_INTERVAL = 1000; // Verificar a cada 1 segundo
    private static final long TOTAL_VERIFICATION_TIME = 10000; // Verificar por 10 segundos total

    private Handler verificationHandler;
    private String targetPackageName;
    private int verificationAttempts = 0;
    private long verificationStartTime;
    private boolean blockingSuccessful = false;

    @Override
    public void onCreate() {
        super.onCreate();
        verificationHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            // Pegar informações do app a ser verificado
            if (intent != null) {
                targetPackageName = intent.getStringExtra("target_package");
            }

            if (targetPackageName == null || targetPackageName.isEmpty()) {
                stopSelf();
                return START_NOT_STICKY;
            }

            // Inicializar verificação
            verificationStartTime = System.currentTimeMillis();
            verificationAttempts = 0;
            blockingSuccessful = false;

            // Aguardar 3 segundos antes de começar a verificar (tempo para o bloqueio normal funcionar)
            verificationHandler.postDelayed(() -> {
                startVerificationCycle();
            }, 3000);

        } catch (Exception e) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    /**
     * Inicia o ciclo de verificação contínua
     */
    private void startVerificationCycle() {
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - verificationStartTime;

            // Verificar se já passou do tempo limite
            if (elapsedTime > TOTAL_VERIFICATION_TIME) {
                finishVerification(false);
                return;
            }

            // Verificar se já tentou muitas vezes
            if (verificationAttempts >= MAX_VERIFICATION_ATTEMPTS) {
                finishVerification(false);
                return;
            }

            verificationAttempts++;

            // Verificar se o bloqueio está funcionando
            boolean isBlocked = checkIfAppIsBlocked();
            boolean messageShowing = checkIfMessageIsShowing();

            if (isBlocked && messageShowing) {
                // Tudo funcionando corretamente!
                finishVerification(true);
                return;
            }

            if (!isBlocked || !messageShowing) {
                // Algo não está funcionando - forçar correção
                forceCorrectBlocking();
            }

            // Programar próxima verificação
            verificationHandler.postDelayed(() -> startVerificationCycle(), VERIFICATION_INTERVAL);

        } catch (Exception e) {
            finishVerification(false);
        }
    }

    /**
     * Verifica se o app está realmente bloqueado/travado
     */
    private boolean checkIfAppIsBlocked() {
        try {

            // Verificar se há uma Activity de bloqueio ativa
            boolean hasBlockActivity = SimpleBlockActivity.isCurrentlyActive();

            // Verificar se o app está em primeiro plano (não deveria estar responsivo)
            String currentApp = getCurrentForegroundApp();
            boolean targetInForeground = targetPackageName.equals(currentApp);

            // App está bloqueado se:
            // 1. Há uma Activity de bloqueio ativa OU
            // 2. O app alvo não está responsivo em primeiro plano
            return hasBlockActivity || !targetInForeground;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se a mensagem de erro está sendo exibida
     */
    private boolean checkIfMessageIsShowing() {
        try {

            // Verificar se SimpleBlockActivity está rodando
            boolean activityRunning = SimpleBlockActivity.isCurrentlyActive();

            return activityRunning;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Força o bloqueio correto quando algo não está funcionando
     */
    private void forceCorrectBlocking() {
        try {

            // Criar Intent para forçar o bloqueio
            Intent forceBlockIntent = new Intent(this, SimpleBlockActivity.class);
            forceBlockIntent.putExtra("blocked_package", targetPackageName);
            forceBlockIntent.putExtra("force_correction", true); // Flag especial
            forceBlockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(forceBlockIntent);
        } catch (Exception e) {
        }
    }

    /**
     * Finaliza o processo de verificação
     */
    private void finishVerification(boolean successful) {
        try {
            blockingSuccessful = successful;

        } catch (Exception e) {
        } finally {
            stopSelf();
        }
    }

    /**
     * Pega o app que está em primeiro plano atualmente
     */
    private String getCurrentForegroundApp() {
        try {
            return UsageStatsUtils.getCurrentForegroundApp(this);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        try {

            // Cancelar todas as verificações pendentes
            if (verificationHandler != null) {
                verificationHandler.removeCallbacksAndMessages(null);
            }

        } catch (Exception e) {
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}