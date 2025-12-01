package com.example.safemode;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

public class LocationManager implements LocationListener {
    private Context context;
    private android.location.LocationManager systemLocationManager;
    private AppPreferences preferences;
    private Location currentLocation;
    private LocationUpdateListener listener;
    private long lastLocationTime = 0;

    // ✅ NOVO: Configurações melhoradas
    private static final long LOCATION_MAX_AGE = 300000; // 5 minutos
    private static final long UPDATE_INTERVAL = 15000; // 15 segundos
    private static final float MIN_DISTANCE = 5; // 5 metros

    public interface LocationUpdateListener {
        void onLocationChanged(boolean isInsideAllowedArea);
        void onLocationError(String error);
    }

    public LocationManager(Context context) {
        this.context = context;
        this.systemLocationManager = (android.location.LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        this.preferences = new AppPreferences(context);
    }

    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * ✅ MÉTODO MELHORADO: Obtém localização uma vez com múltiplas tentativas
     */
    public void getLocationOnce() {

        try {
            if (!hasLocationPermission()) {
                if (listener != null) {
                    listener.onLocationError("Permissão de localização negada");
                }
                return;
            }

            if (!isLocationEnabled()) {
                if (listener != null) {
                    listener.onLocationError("GPS está desligado");
                }
                return;
            }

            // ✅ NOVA ESTRATÉGIA: Tentar múltiplas fontes simultaneamente
            requestLocationFromAllProviders();

        } catch (SecurityException e) {
            if (listener != null) {
                listener.onLocationError("Erro de permissão de localização");
            }
        }
    }

    /**
     * ✅ NOVO MÉTODO: Solicita localização de todos os provedores disponíveis
     */
    private void requestLocationFromAllProviders() {

        try {
            // Primeiro, tentar última localização conhecida mais recente
            Location bestLastKnown = getBestLastKnownLocation();
            if (bestLastKnown != null) {
                long age = System.currentTimeMillis() - bestLastKnown.getTime();
                if (age < LOCATION_MAX_AGE) {
                    onLocationChanged(bestLastKnown);
                    return;
                }
            }

            // Se não tem localização recente, solicitar nova de múltiplos provedores
            boolean gpsRequested = false;
            boolean networkRequested = false;

            // ✅ Tentar GPS se disponível
            if (systemLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                try {
                    systemLocationManager.requestSingleUpdate(
                            android.location.LocationManager.GPS_PROVIDER,
                            new SingleUpdateLocationListener("GPS"),
                            null
                    );
                    gpsRequested = true;
                } catch (Exception e) {
                }
            }

            // ✅ Tentar Network se disponível
            if (systemLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                try {
                    systemLocationManager.requestSingleUpdate(
                            android.location.LocationManager.NETWORK_PROVIDER,
                            new SingleUpdateLocationListener("Network"),
                            null
                    );
                    networkRequested = true;
                } catch (Exception e) {
                }
            }

            // ✅ Tentar Passive como fallback
            try {
                systemLocationManager.requestSingleUpdate(
                        android.location.LocationManager.PASSIVE_PROVIDER,
                        new SingleUpdateLocationListener("Passive"),
                        null
                );
            } catch (Exception e) {
            }

            if (!gpsRequested && !networkRequested) {
                if (listener != null) {
                    listener.onLocationError("Nenhum provedor de localização disponível");
                }
            }

        } catch (SecurityException e) {
            if (listener != null) {
                listener.onLocationError("Erro de permissão");
            }
        }
    }

    /**
     * ✅ NOVA CLASSE: Listener para uma única atualização
     */
    private class SingleUpdateLocationListener implements android.location.LocationListener {
        private String providerName;

        public SingleUpdateLocationListener(String providerName) {
            this.providerName = providerName;
        }

        @Override
        public void onLocationChanged(Location location) {

            // Usar a localização se for melhor que a atual
            if (isBetterLocation(location, currentLocation)) {
                LocationManager.this.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 2 * 60 * 1000;
        boolean isSignificantlyOlder = timeDelta < -2 * 60 * 1000;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = location.getProvider() != null &&
                location.getProvider().equals(currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (!isSignificantlyLessAccurate && !isFromSameProvider) {
            return true;
        }

        return false;
    }

    /**
     * ✅ MÉTODO MELHORADO: Inicia monitoramento contínuo
     */
    public void startLocationUpdates() {

        try {
            if (!hasLocationPermission()) {
                if (listener != null) {
                    listener.onLocationError("Permissão de localização não concedida");
                }
                return;
            }

            if (!isLocationEnabled()) {
                if (listener != null) {
                    listener.onLocationError("GPS está desligado");
                }
                return;
            }

            // Primeiro obter localização atual
            Location bestLocation = getBestLastKnownLocation();
            if (bestLocation != null) {
                onLocationChanged(bestLocation);
            }

            // ✅ Iniciar monitoramento melhorado de GPS
            if (systemLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                systemLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL,
                        MIN_DISTANCE,
                        this
                );
            }

            // ✅ Iniciar monitoramento melhorado de Network
            if (systemLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                systemLocationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL * 2, // Network menos frequente
                        MIN_DISTANCE * 2,
                        this
                );
            }
        } catch (SecurityException e) {
            if (listener != null) {
                listener.onLocationError("Erro de permissão: " + e.getMessage());
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onLocationError("Erro ao iniciar GPS: " + e.getMessage());
            }
        }
    }

    /**
     * Para de monitorar a localização
     */
    public void stopLocationUpdates() {
        try {
            systemLocationManager.removeUpdates(this);
        } catch (Exception e) {
        }
    }

    public boolean isOutsideAllowedArea() {

        // Se o controle por localização está desligado, sempre permitir
        if (!preferences.isLocationEnabled()) {
            return false;
        }

        // ✅ CORREÇÃO PRINCIPAL: Se não temos localização atual, ASSUMIR QUE ESTÁ FORA
        if (currentLocation == null) {
            // Tentar obter localização rapidamente
            getLocationOnce();
            return true; // ← CORRIGIDO: Bloquear quando não tem localização
        }

        // Verificar se a localização é muito antiga
        long locationAge = System.currentTimeMillis() - currentLocation.getTime();
        if (locationAge > LOCATION_MAX_AGE) {
            getLocationOnce(); // Tentar obter nova
            return true; // ← CORRIGIDO: Bloquear quando localização é muito antiga
        }

        // Calcular a distância até o centro da área permitida
        double allowedLat = preferences.getAllowedLatitude();
        double allowedLng = preferences.getAllowedLongitude();
        int allowedRadius = preferences.getAllowedRadius();

        // Se não há área definida, sempre permitir
        if (allowedLat == 0.0 && allowedLng == 0.0) {
            return false;
        }

        // Calcular distância em metros
        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                allowedLat,
                allowedLng,
                results
        );

        float distanceInMeters = results[0];
        boolean isOutside = distanceInMeters > allowedRadius;

        return isOutside;
    }

    /**
     * Pega a localização atual (se disponível)
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Chamado quando a localização muda
     */
    @Override
    public void onLocationChanged(Location location) {

        // ✅ Só atualizar se a nova localização for melhor
        if (isBetterLocation(location, currentLocation)) {
            currentLocation = location;
            lastLocationTime = System.currentTimeMillis();

            // Verificar se estamos dentro ou fora da área
            boolean isOutside = isOutsideAllowedArea();

            // Avisar quem está interessado
            if (listener != null) {
                listener.onLocationChanged(!isOutside);
            }
        }
    }

    /**
     * Chamado quando um provedor de localização é ligado
     */
    @Override
    public void onProviderEnabled(String provider) {
        requestSingleLocationUpdate();
    }

    /**
     * Chamado quando um provedor de localização é desligado
     */
    @Override
    public void onProviderDisabled(String provider) {
        if (listener != null) {
            listener.onLocationError("Provedor " + provider + " foi desligado");
        }
    }

    /**
     * Pede uma única atualização de localização (mais rápido)
     */
    public void requestSingleLocationUpdate() {
        getLocationOnce();
    }

    /**
     * Verifica se temos permissão de localização
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se o GPS está ligado
     */
    private boolean isLocationEnabled() {
        return systemLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                systemLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    /**
     * ✅ MÉTODO MELHORADO: Pega a melhor última localização conhecida
     */
    private Location getBestLastKnownLocation() {
        try {
            Location lastGPS = null;
            Location lastNetwork = null;
            Location lastPassive = null;

            // Tentar GPS
            try {
                lastGPS = systemLocationManager.getLastKnownLocation(
                        android.location.LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
            }

            // Tentar Network
            try {
                lastNetwork = systemLocationManager.getLastKnownLocation(
                        android.location.LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException e) {
            }

            // Tentar Passive
            try {
                lastPassive = systemLocationManager.getLastKnownLocation(
                        android.location.LocationManager.PASSIVE_PROVIDER);
            } catch (SecurityException e) {
            }

            // Encontrar a melhor localização
            Location best = null;

            if (lastGPS != null) {
                best = lastGPS;
            }

            if (lastNetwork != null && isBetterLocation(lastNetwork, best)) {
                best = lastNetwork;
            }

            if (lastPassive != null && isBetterLocation(lastPassive, best)) {
                best = lastPassive;
            }

            return best;

        } catch (SecurityException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}