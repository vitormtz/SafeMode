package com.example.safemode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

/**
 * Serviço principal do Safe Mode
 * É como o "cérebro" que coordena tudo - localização, monitoramento, etc.
 */
public class SafeModeService extends Service implements LocationManager.LocationUpdateListener {

    private static final String CHANNEL_ID = "SafeModeService";
    private static final int NOTIFICATION_ID = 1001;

    private LocationManager locationManager;
    private AppPreferences preferences;
    private boolean isLocationMonitoringActive = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar componentes
        preferences = new AppPreferences(this);
        locationManager = new LocationManager(this);
        locationManager.setLocationUpdateListener(this);

        // Criar canal de notificação (para Android 8+)
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Verificar se o Safe Mode está ativo
        if (!preferences.isSafeModeEnabled()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Iniciar como serviço em primeiro plano (para não ser morto pelo sistema)
        startForeground(NOTIFICATION_ID, createNotification());

        // Iniciar monitoramento de localização se habilitado
        if (preferences.isLocationEnabled()) {
            startLocationMonitoring();
        }

        return START_STICKY; // Reiniciar se for morto pelo sistema
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Parar monitoramento
        stopLocationMonitoring();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Não precisamos de binding
    }

    /**
     * Inicia o monitoramento de localização
     * É como ligar o "radar" que vigia onde você está
     */
    private void startLocationMonitoring() {

        if (!isLocationMonitoringActive) {
            locationManager.startLocationUpdates();
            isLocationMonitoringActive = true;

            android.util.Log.d("SafeModeService", "Monitoramento de localização iniciado");
        }
    }

    /**
     * Para o monitoramento de localização
     */
    private void stopLocationMonitoring() {

        if (isLocationMonitoringActive) {
            locationManager.stopLocationUpdates();
            isLocationMonitoringActive = false;
        }
    }

    /**
     * Implementação do LocationUpdateListener
     * Chamado quando a localização muda
     */
    @Override
    public void onLocationChanged(boolean isInsideAllowedArea) {

        // Atualizar notificação com status atual
        updateNotification(isInsideAllowedArea);
    }

    @Override
    public void onLocationError(String error) {
        android.util.Log.e("SafeModeService", "Erro de localização: " + error);
    }

    /**
     * Cria o canal de notificação (necessário para Android 8+)
     */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Safe Mode",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("Monitoramento do Safe Mode");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Cria a notificação do serviço
     */
    private Notification createNotification() {
        return createNotificationWithStatus(true); // Padrão: dentro da área
    }

    /**
     * Atualiza a notificação com o status atual
     */
    private void updateNotification(boolean isInsideAllowedArea) {

        Notification notification = createNotificationWithStatus(isInsideAllowedArea);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Cria notificação com status específico
     */
    private Notification createNotificationWithStatus(boolean isInsideAllowedArea) {

        // Intent para abrir o app quando clicar na notificação
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Definir título e texto baseado no status
        String title = getString(R.string.notification_title);
        String text;
        int iconColor;

        if (preferences.isLocationEnabled()) {
            if (isInsideAllowedArea) {
                text = "Área permitida - Apps liberados";
                iconColor = 0xFF4CAF50; // Verde
            } else {
                text = "Fora da área - Apps bloqueados";
                iconColor = 0xFFFF9800; // Laranja
            }
        } else {
            text = getString(R.string.notification_description);
            iconColor = 0xFF2196F3; // Azul
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_safe_mode)
                .setColor(iconColor)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Não pode ser removida pelo usuário
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    /**
     * Método público para verificar se o serviço está rodando
     */
    public static boolean isRunning(android.content.Context context) {

        android.app.ActivityManager manager = (android.app.ActivityManager)
                context.getSystemService(android.content.Context.ACTIVITY_SERVICE);

        for (android.app.ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {

            if (SafeModeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}