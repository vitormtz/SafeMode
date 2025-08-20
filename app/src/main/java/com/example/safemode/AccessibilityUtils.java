package com.example.safemode;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Utilitários para verificar se o serviço de acessibilidade está funcionando
 * É como ter um teste para ver se o "espião" está trabalhando
 */
public class AccessibilityUtils {

    /**
     * Verifica se o serviço de acessibilidade está ativo
     * É como perguntar: "o espião está de plantão?"
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {

        String expectedServiceName = context.getPackageName() + "/" + serviceClass.getName();

        // Pegar a lista de serviços de acessibilidade ativos
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (TextUtils.isEmpty(enabledServices)) {
            return false;
        }

        // Verificar se nosso serviço está na lista
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);

        while (splitter.hasNext()) {
            String serviceName = splitter.next();
            if (expectedServiceName.equals(serviceName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica se os serviços de acessibilidade estão ligados no sistema
     */
    public static boolean isAccessibilityEnabled(Context context) {

        int accessibilityEnabled = 0;

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        return accessibilityEnabled == 1;
    }

    /**
     * Pega uma descrição amigável do status do serviço
     */
    public static String getAccessibilityStatusDescription(Context context, Class<?> serviceClass) {

        if (!isAccessibilityEnabled(context)) {
            return "Serviços de acessibilidade estão desabilitados no sistema";
        }

        if (!isAccessibilityServiceEnabled(context, serviceClass)) {
            return "Safe Mode não está ativo nos serviços de acessibilidade";
        }

        return "Serviço de acessibilidade está funcionando";
    }
}