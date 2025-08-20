package com.example.safemode;

import android.graphics.drawable.Drawable;

public class AppInfo {

    public String packageName;    // Nome técnico do app (ex: com.whatsapp)
    public String appName;        // Nome amigável (ex: WhatsApp)
    public Drawable icon;         // Ícone do app
    public boolean isBlocked;     // Se está marcado para ser bloqueado
    public long lastUsedTime;     // Última vez que foi usado
    public boolean isSystemApp;   // Se é um app do sistema

    public AppInfo() {
        // Construtor vazio
    }

    public AppInfo(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isBlocked = false;
        this.isSystemApp = false;
    }

    /**
     * Compara dois apps pelo nome (para ordenação)
     */
    public int compareTo(AppInfo other) {
        return this.appName.compareToIgnoreCase(other.appName);
    }

    /**
     * Verifica se é o mesmo app
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AppInfo appInfo = (AppInfo) obj;
        return packageName != null ? packageName.equals(appInfo.packageName) : appInfo.packageName == null;
    }

    /**
     * Código hash baseado no nome do pacote
     */
    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }

    /**
     * Representação em texto (para debug)
     */
    @Override
    public String toString() {
        return "AppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", isBlocked=" + isBlocked +
                '}';
    }
}