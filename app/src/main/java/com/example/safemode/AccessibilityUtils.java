package com.example.safemode;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class AccessibilityUtils {

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {

        String expectedServiceName = context.getPackageName() + "/" + serviceClass.getName();

        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (TextUtils.isEmpty(enabledServices)) {
            return false;
        }

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

}