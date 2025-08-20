package com.example.safemode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// ✅ IMPORTS DO GOOGLE MAPS
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * LocationSetupActivity - VERSÃO COMPLETA COM GOOGLE MAPS
 * Agora o usuário pode ver um mapa de verdade e selecionar a área visualmente
 */
public class LocationSetupActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationSetupMaps";
    private static final int PERMISSION_REQUEST_LOCATION = 1001;
    private MapFriendlyScrollView scrollView;
    private LinearLayout mapLoadingLayout;
    private TextView textSelectedLocation;
    private TextView textRadiusValue;
    private SeekBar seekBarRadius;
    private Button btnMyLocation;
    private Button btnSaveLocation;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SupportMapFragment mapFragment;
    private Marker selectedLocationMarker;
    private Circle safeAreaCircle;
    private LatLng selectedLatLng;
    private int currentRadius = 100;

    // ✅ CLASSES AUXILIARES
    private AppPreferences preferences;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_location_setup);

        // Inicializar componentes
        setupSystemBars();
        initializeViews();
        initializeGoogleMaps();
        loadSavedSettings();
        setupListeners();
    }

    /**
     * ✅ NOVO: Configura o ScrollView para não interferir com o mapa
     */
    private void setupMapFriendlyScrollView() {
        try {

            if (scrollView != null && mapFragment != null) {
                // Definir o container do mapa para o ScrollView customizado
                View mapContainer = mapFragment.getView();

                if (mapContainer != null) {
                    scrollView.setMapContainer(mapContainer);
                } else {

                    // Tentar novamente após um delay
                    new android.os.Handler().postDelayed(() -> {
                        View delayedMapContainer = mapFragment.getView();
                        if (delayedMapContainer != null) {
                            scrollView.setMapContainer(delayedMapContainer);
                        }
                    }, 1000);
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * Configura as barras do sistema
     */
    private void setupSystemBars() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
            }
        } catch (Exception e) {
        }
    }

    /**
     * Inicializa os elementos da interface
     */
    private void initializeViews() {

        try {
            // Elementos da interface
            scrollView = findViewById(R.id.scroll_view);
            mapLoadingLayout = findViewById(R.id.map_loading);
            textSelectedLocation = findViewById(R.id.text_selected_location);
            textRadiusValue = findViewById(R.id.text_radius_value);
            seekBarRadius = findViewById(R.id.seekbar_radius);
            btnMyLocation = findViewById(R.id.btn_my_location);
            btnSaveLocation = findViewById(R.id.btn_save_location);

            // Classes auxiliares
            preferences = new AppPreferences(this);
            locationManager = new LocationManager(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Configurar SeekBar
            seekBarRadius.setMax(2000); // Máximo 2km
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                seekBarRadius.setMin(50); // Mínimo 50m
            }
            seekBarRadius.setProgress(currentRadius);
            updateRadiusText(currentRadius);

            // Configurar o ScrollView customizado para não interferir com o mapa
            setupMapFriendlyScrollView();


        } catch (Exception e) {
        }
    }

    /**
     * ✅ NOVO: Inicializa o Google Maps
     */
    private void initializeGoogleMaps() {

        try {
            // Pegar o fragmento do mapa
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_fragment);

            if (mapFragment != null) {
                // Solicitar que o mapa seja carregado
                mapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
        }
    }

    /**
     * ✅ CALLBACK QUANDO O MAPA ESTÁ PRONTO
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {

        try {
            googleMap = map;

            // Configurar o mapa
            setupMapSettings();

            // Esconder indicador de carregamento
            if (mapLoadingLayout != null) {
                mapLoadingLayout.setVisibility(View.GONE);
            }

            // Configurar clique no mapa
            googleMap.setOnMapClickListener(this::onMapClick);

            // ✅ NOVO: Reconfigurar ScrollView agora que o mapa está pronto
            setupMapFriendlyScrollView();

            // Tentar ir para a localização atual
            goToCurrentLocation();

            // Carregar área salva (se existir)
            loadSavedArea();


        } catch (Exception e) {
        }
    }

    /**
     * ✅ NOVO: Configura as opções do mapa
     */
    private void setupMapSettings() {
        try {
            // Tipo de mapa (normal, satélite, híbrido, terreno)
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Controles da UI
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false); // Usaremos nosso próprio botão

            // ✅ NOVO: Desabilitar InfoWindows (janelas de informação dos marcadores)
            googleMap.setOnMarkerClickListener(marker -> {
                return true;
            });

            // Habilitar localização se tiver permissão
            if (hasLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
            }

        } catch (SecurityException e) {
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao configurar mapa: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Chamado quando usuário clica no mapa
     */
    private void onMapClick(LatLng latLng) {
        Log.d(TAG, "📍 Usuário clicou no mapa: " + latLng.latitude + ", " + latLng.longitude);

        try {
            // Salvar a localização selecionada
            selectedLatLng = latLng;

            // Atualizar marcador
            updateLocationMarker(latLng);

            // Atualizar círculo da área
            updateSafeAreaCircle();

            // Atualizar textos da interface
            updateLocationInfo();

            // Habilitar botões
            enableActionButtons(true);

            Log.d(TAG, "✅ Localização selecionada e interface atualizada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao processar clique no mapa: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Atualiza ou cria o marcador da localização selecionada
     */
    private void updateLocationMarker(LatLng latLng) {
        try {
            // Remover marcador anterior se existir
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
            }

            // ✅ CORREÇÃO: Criar marcador SEM título e snippet para evitar InfoWindow
            selectedLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng));
            // Removido: .title("Área Segura")
            // Removido: .snippet("Centro da área onde o Safe Mode ficará desativado")

            Log.d(TAG, "📌 Marcador atualizado na posição: " + latLng);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar marcador: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Atualiza ou cria o círculo da área segura
     */
    private void updateSafeAreaCircle() {
        try {
            if (selectedLatLng == null) {
                return;
            }

            // Remover círculo anterior se existir
            if (safeAreaCircle != null) {
                safeAreaCircle.remove();
            }

            // Criar novo círculo
            CircleOptions circleOptions = new CircleOptions()
                    .center(selectedLatLng)
                    .radius(currentRadius) // raio em metros
                    .strokeColor(Color.BLUE)
                    .strokeWidth(3)
                    .fillColor(Color.argb(50, 0, 100, 255)); // Azul semi-transparente

            safeAreaCircle = googleMap.addCircle(circleOptions);

            Log.d(TAG, "🔵 Círculo da área atualizado - raio: " + currentRadius + "m");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar círculo: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Atualiza as informações da localização na interface
     */
    private void updateLocationInfo() {
        try {
            if (selectedLatLng != null) {
                String locationText = String.format(
                        "Localização selecionada\nLat: %.6f\nLng: %.6f\nRaio: %d metros",
                        selectedLatLng.latitude,
                        selectedLatLng.longitude,
                        currentRadius
                );

                textSelectedLocation.setText(locationText);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar informações: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Habilita ou desabilita os botões de ação
     */
    private void enableActionButtons(boolean enabled) {
        try {
            btnSaveLocation.setEnabled(enabled);

            float alpha = enabled ? 1.0f : 0.5f;
            btnSaveLocation.setAlpha(alpha);

            if (enabled) {
                btnSaveLocation.setText("Salvar localização");
            } else {
                btnSaveLocation.setText("Selecione uma localização primeiro");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar botões: " + e.getMessage());
        }
    }

    /**
     * Carrega configurações salvas anteriormente
     */
    private void loadSavedSettings() {
        try {
            // Carregar raio salvo
            int savedRadius = preferences.getAllowedRadius();
            if (savedRadius > 0) {
                currentRadius = savedRadius;
                seekBarRadius.setProgress(currentRadius);
                updateRadiusText(currentRadius);
            }

            Log.d(TAG, "📚 Configurações carregadas - raio: " + currentRadius);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar configurações: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Carrega área segura salva no mapa
     */
    private void loadSavedArea() {
        try {
            double savedLat = preferences.getAllowedLatitude();
            double savedLng = preferences.getAllowedLongitude();

            if (savedLat != 0.0 && savedLng != 0.0) {
                Log.d(TAG, "📍 Carregando área salva: " + savedLat + ", " + savedLng);

                LatLng savedLocation = new LatLng(savedLat, savedLng);
                selectedLatLng = savedLocation;

                // Atualizar mapa
                updateLocationMarker(savedLocation);
                updateSafeAreaCircle();
                updateLocationInfo();
                enableActionButtons(true);

                // Mover câmera para a localização salva
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(savedLocation, 15));
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar área salva: " + e.getMessage());
        }
    }

    /**
     * Configura os listeners dos controles
     */
    private void setupListeners() {
        try {
            // ✅ BOTÃO: Ir para minha localização
            btnMyLocation.setOnClickListener(v -> goToCurrentLocation());

            // ✅ BOTÃO: Salvar localização
            btnSaveLocation.setOnClickListener(v -> saveLocationSettings());

            // ✅ SEEKBAR: Controle do raio
            seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Garantir valor mínimo
                        if (progress < 50) progress = 50;

                        currentRadius = progress;
                        updateRadiusText(progress);

                        // Atualizar círculo no mapa se tiver localização selecionada
                        if (selectedLatLng != null) {
                            updateSafeAreaCircle();
                            updateLocationInfo();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            Log.d(TAG, "✅ Listeners configurados");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao configurar listeners: " + e.getMessage());
        }
    }

    /**
     * ✅ NOVO: Vai para a localização atual do usuário
     */
    private void goToCurrentLocation() {
        Log.d(TAG, "🎯 Obtendo localização atual...");

        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            btnMyLocation.setEnabled(false);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            Log.d(TAG, "📍 Localização atual obtida: " + currentLatLng);

                            // Mover câmera para localização atual
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                        } else {
                            Log.w(TAG, "⚠️ Localização atual é null");
                        }

                        // Restaurar botão
                        btnMyLocation.setEnabled(true);
                        btnMyLocation.setText("Minha localização");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Erro ao obter localização: " + e.getMessage());

                        // Restaurar botão
                        btnMyLocation.setEnabled(true);
                        btnMyLocation.setText("Minha localização");
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Erro de permissão: " + e.getMessage());
            requestLocationPermission();

            // Restaurar botão
            btnMyLocation.setEnabled(true);
            btnMyLocation.setText("Minha localização");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro geral: " + e.getMessage());

            // Restaurar botão
            btnMyLocation.setEnabled(true);
            btnMyLocation.setText("Minha localização");
        }
    }

    /**
     * ✅ NOVO: Limpa a seleção atual
     */
    private void clearSelection() {
        Log.d(TAG, "🗑️ Limpando seleção...");

        try {
            // Limpar variáveis
            selectedLatLng = null;

            // Remover marcador e círculo do mapa
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
                selectedLocationMarker = null;
            }

            if (safeAreaCircle != null) {
                safeAreaCircle.remove();
                safeAreaCircle = null;
            }

            // Atualizar interface
            textSelectedLocation.setText("Toque no mapa para selecionar uma localização");

            // Desabilitar botões
            enableActionButtons(false);

            showMessage("Seleção limpa");
            Log.d(TAG, "✅ Seleção limpa com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao limpar seleção: " + e.getMessage());
        }
    }

    /**
     * Salva as configurações da área segura
     */
    private void saveLocationSettings() {
        Log.d(TAG, "💾 Salvando configurações da área segura...");

        try {
            if (selectedLatLng == null) {
                showMessage("Selecione uma localização no mapa primeiro");
                return;
            }

            // Salvar usando o método do AppPreferences
            preferences.setAllowedLocation(
                    selectedLatLng.latitude,
                    selectedLatLng.longitude,
                    currentRadius
            );

            // Verificar se salvou corretamente
            double savedLat = preferences.getAllowedLatitude();
            double savedLng = preferences.getAllowedLongitude();
            int savedRadius = preferences.getAllowedRadius();

            boolean saveSuccess = Math.abs(savedLat - selectedLatLng.latitude) < 0.000001 &&
                    Math.abs(savedLng - selectedLatLng.longitude) < 0.000001 &&
                    savedRadius == currentRadius;

            if (saveSuccess) {
                showMessage("Localização salva com sucesso!");
                Log.d(TAG, "✅ Configurações salvas com sucesso");
            } else {
                showMessage("Erro ao salvar localização");
                Log.e(TAG, "❌ Falha na verificação do salvamento");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao salvar: " + e.getMessage());
            showMessage("Erro ao salvar: " + e.getMessage());
        }
    }

    /**
     * Atualiza o texto do raio
     */
    private void updateRadiusText(int radiusInMeters) {
        try {
            String radiusText;
            if (radiusInMeters >= 1000) {
                radiusText = String.format("%.1f km", radiusInMeters / 1000.0);
            } else {
                radiusText = radiusInMeters + " metros";
            }

            textRadiusValue.setText(radiusText);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar texto do raio: " + e.getMessage());
        }
    }

    /**
     * Verifica se tem permissão de localização
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita permissão de localização
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_LOCATION);
    }

    /**
     * Mostra mensagem na tela
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(TAG, "💬 Mensagem: " + message);
    }

    /**
     * Resultado das permissões
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMessage("Permissão de localização concedida!");
                Log.d(TAG, "✅ Permissão concedida");

                // Tentar configurar localização no mapa novamente
                try {
                    if (googleMap != null) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "⚠️ Ainda sem permissão após concessão");
                }
            } else {
                showMessage("Permissão de localização negada!");
                Log.w(TAG, "❌ Permissão negada");
            }
        }
    }

    /**
     * Limpeza quando a Activity é destruída
     */
    @Override
    protected void onDestroy() {
        try {
            Log.d(TAG, "🔚 Destruindo LocationSetupActivity...");

            // Limpar referências
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
                selectedLocationMarker = null;
            }
            if (safeAreaCircle != null) {
                safeAreaCircle.remove();
                safeAreaCircle = null;
            }

            googleMap = null;
            selectedLatLng = null;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no onDestroy: " + e.getMessage());
        }

        super.onDestroy();
        Log.d(TAG, "✅ LocationSetupActivity destruída");
    }

    /**
     * ✅ MÉTODO ADICIONAL: Chamado quando Activity volta do background
     */
    @Override
    protected void onResume() {
        super.onResume();

        try {
            Log.d(TAG, "🔄 Activity resumida");

            // Verificar se o mapa ainda está funcionando
            if (googleMap != null && selectedLatLng != null) {
                // Recriar marcador e círculo se necessário
                if (selectedLocationMarker == null) {
                    updateLocationMarker(selectedLatLng);
                }
                if (safeAreaCircle == null) {
                    updateSafeAreaCircle();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no onResume: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Para quando Activity vai para background
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ Activity pausada");
    }

    /**
     * ✅ MÉTODO ADICIONAL: Para debug - força atualização da interface
     */
    private void forceUIUpdate() {
        try {
            Log.d(TAG, "🔄 Forçando atualização da UI...");

            if (selectedLatLng != null) {
                updateLocationInfo();
                enableActionButtons(true);

                // Recriar elementos visuais no mapa se necessário
                if (googleMap != null) {
                    if (selectedLocationMarker == null) {
                        updateLocationMarker(selectedLatLng);
                    }
                    if (safeAreaCircle == null) {
                        updateSafeAreaCircle();
                    }
                }
            } else {
                enableActionButtons(false);
                textSelectedLocation.setText("Toque no mapa para selecionar uma localização");
            }

            // Atualizar texto do raio
            updateRadiusText(currentRadius);

            Log.d(TAG, "✅ UI atualizada com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar UI: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Verifica se as configurações estão válidas
     */
    private boolean isConfigurationValid() {
        try {
            boolean hasLocation = selectedLatLng != null;
            boolean hasValidRadius = currentRadius >= 50 && currentRadius <= 2000;

            Log.d(TAG, "🔍 Verificando configuração - Localização: " + hasLocation + ", Raio: " + hasValidRadius);

            return hasLocation && hasValidRadius;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar configuração: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Centraliza o mapa na área selecionada
     */
    private void centerMapOnSelectedArea() {
        try {
            if (googleMap != null && selectedLatLng != null) {
                // Calcular zoom apropriado baseado no raio
                float zoom = calculateZoomLevel(currentRadius);

                Log.d(TAG, "🎯 Centralizando mapa - Zoom: " + zoom);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, zoom));
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao centralizar mapa: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Calcula o nível de zoom apropriado baseado no raio
     */
    private float calculateZoomLevel(int radiusInMeters) {
        try {
            // Fórmula aproximada para calcular zoom baseado no raio
            // Quanto maior o raio, menor o zoom (para mostrar mais área)

            if (radiusInMeters <= 100) {
                return 17.0f; // Zoom bem próximo
            } else if (radiusInMeters <= 300) {
                return 16.0f;
            } else if (radiusInMeters <= 500) {
                return 15.0f;
            } else if (radiusInMeters <= 1000) {
                return 14.0f;
            } else if (radiusInMeters <= 2000) {
                return 13.0f;
            } else {
                return 12.0f; // Zoom mais distante
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao calcular zoom: " + e.getMessage());
            return 15.0f; // Zoom padrão em caso de erro
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Mostra informações detalhadas sobre a configuração atual
     */
    private void showConfigurationSummary() {
        try {
            if (!isConfigurationValid()) {
                showMessage("Configuração incompleta");
                return;
            }

            String summary = String.format(
                    "📍 RESUMO DA CONFIGURAÇÃO\n\n" +
                            "Centro da área segura:\n" +
                            "Latitude: %.6f\n" +
                            "Longitude: %.6f\n\n" +
                            "Raio da área: %s\n\n" +
                            "Área total: %.2f km²",
                    selectedLatLng.latitude,
                    selectedLatLng.longitude,
                    currentRadius >= 1000 ?
                            String.format("%.1f km", currentRadius / 1000.0) :
                            currentRadius + " metros",
                    Math.PI * Math.pow(currentRadius / 1000.0, 2)
            );

            // Criar um AlertDialog para mostrar o resumo
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Configuração da Área Segura")
                    .setMessage(summary)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Centralizar no Mapa", (dialog, which) -> centerMapOnSelectedArea())
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao mostrar resumo: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Tratamento de erro quando o Google Maps falha
     */
    private void handleMapError(String errorMessage) {
        try {
            Log.e(TAG, "🗺️ Erro no Google Maps: " + errorMessage);

            // Mostrar layout de erro ao invés do mapa
            if (mapLoadingLayout != null) {
                mapLoadingLayout.setVisibility(View.VISIBLE);

                // Encontrar os elementos dentro do layout de carregamento
                View progressBar = mapLoadingLayout.findViewById(R.id.progress_loading);
                TextView loadingText = mapLoadingLayout.findViewById(R.id.text_loading);

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (loadingText != null) {
                    loadingText.setText("❌ Erro ao carregar o mapa\n\n" + errorMessage);
                    loadingText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            // Desabilitar funcionalidades que dependem do mapa
            if (btnMyLocation != null) {
                btnMyLocation.setEnabled(false);
                btnMyLocation.setText("❌ Mapa indisponível");
            }

            showMessage("Erro no mapa: " + errorMessage);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao tratar erro do mapa: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Validação final antes de salvar
     */
    private boolean validateBeforeSave() {
        try {
            // Verificar se tem localização selecionada
            if (selectedLatLng == null) {
                showMessage("❌ Selecione uma localização no mapa primeiro");
                return false;
            }

            // Verificar se as coordenadas são válidas
            if (selectedLatLng.latitude < -90 || selectedLatLng.latitude > 90) {
                showMessage("❌ Latitude inválida");
                return false;
            }

            if (selectedLatLng.longitude < -180 || selectedLatLng.longitude > 180) {
                showMessage("❌ Longitude inválida");
                return false;
            }

            // Verificar se o raio é válido
            if (currentRadius < 50 || currentRadius > 2000) {
                showMessage("❌ Raio deve estar entre 50m e 2km");
                return false;
            }

            Log.d(TAG, "✅ Validação passou - pronto para salvar");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro na validação: " + e.getMessage());
            showMessage("Erro na validação: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Salvar com validação e confirmação
     */
    private void saveWithConfirmation() {
        try {
            if (!validateBeforeSave()) {
                return;
            }

            // Mostrar diálogo de confirmação com resumo
            String confirmationMessage = String.format(
                    "Deseja salvar esta configuração?\n\n" +
                            "📍 Centro: %.6f, %.6f\n" +
                            "🎯 Raio: %s\n\n" +
                            "Quando você estiver dentro desta área, o Safe Mode ficará desativado.",
                    selectedLatLng.latitude,
                    selectedLatLng.longitude,
                    currentRadius >= 1000 ?
                            String.format("%.1f km", currentRadius / 1000.0) :
                            currentRadius + " metros"
            );

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar Configuração")
                    .setMessage(confirmationMessage)
                    .setPositiveButton("Salvar", (dialog, which) -> saveLocationSettings())
                    .setNegativeButton("Cancelar", null)
                    .setNeutralButton("Ver Resumo", (dialog, which) -> showConfigurationSummary())
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao mostrar confirmação: " + e.getMessage());
            // Se der erro no diálogo, salvar diretamente
            saveLocationSettings();
        }
    }
}