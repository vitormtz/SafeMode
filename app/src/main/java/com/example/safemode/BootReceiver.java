package com.example.safemode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receptor que inicia o Safe Mode automaticamente quando o celular é ligado
 * É como ter um "despertador" que liga o sistema de segurança
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // Verificar se é um dos eventos que nos interessam
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            // Verificar se o Safe Mode estava ativo antes do reinício
            AppPreferences preferences = new AppPreferences(context);

            if (preferences.isSafeModeEnabled()) {

                // Iniciar o serviço automaticamente
                Intent serviceIntent = new Intent(context, SafeModeService.class);

                try {
                    // No Android 8+ precisa usar startForegroundService
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }

                } catch (Exception e) {
                }
            }
        }
    }
}