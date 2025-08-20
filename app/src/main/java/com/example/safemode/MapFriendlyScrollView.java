package com.example.safemode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * ScrollView customizado que não interfere com o Google Maps
 * É como ter um ScrollView "inteligente" que sabe quando deixar o mapa em paz
 */
public class MapFriendlyScrollView extends ScrollView {

    private View mapContainer;

    public MapFriendlyScrollView(Context context) {
        super(context);
    }

    public MapFriendlyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapFriendlyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Define qual é o container do mapa
     * É como "avisar" para o ScrollView onde está o mapa
     */
    public void setMapContainer(View mapContainer) {
        this.mapContainer = mapContainer;
    }

    /**
     * Intercepta os toques antes que cheguem aos filhos
     * É como um "filtro" que decide se o toque é para o mapa ou para o scroll
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            // Se não temos referência do mapa, comportamento normal
            if (mapContainer == null) {
                return super.onInterceptTouchEvent(event);
            }

            // Verificar se o toque está dentro da área do mapa
            if (isTouchInsideMapArea(event)) {
                // Toque está no mapa - não interceptar, deixar o mapa lidar com o toque
                return false;
            }

            // Toque está fora do mapa - pode interceptar normalmente para scroll
            return super.onInterceptTouchEvent(event);

        } catch (Exception e) {
            // Em caso de erro, comportamento padrão
            return super.onInterceptTouchEvent(event);
        }
    }

    /**
     * Verifica se o toque está dentro da área do mapa
     * É como perguntar: "esse dedo está tocando no mapa?"
     */
    private boolean isTouchInsideMapArea(MotionEvent event) {
        try {
            if (mapContainer == null) {
                return false;
            }

            // Pegar as coordenadas do toque em relação à tela
            float touchX = event.getRawX();
            float touchY = event.getRawY();

            // Pegar a posição do container do mapa na tela
            int[] mapLocation = new int[2];
            mapContainer.getLocationOnScreen(mapLocation);

            int mapLeft = mapLocation[0];
            int mapTop = mapLocation[1];
            int mapRight = mapLeft + mapContainer.getWidth();
            int mapBottom = mapTop + mapContainer.getHeight();

            // Verificar se o toque está dentro dos limites do mapa
            boolean isInsideMap = touchX >= mapLeft && touchX <= mapRight &&
                    touchY >= mapTop && touchY <= mapBottom;

            return isInsideMap;

        } catch (Exception e) {
            // Em caso de erro, assumir que não está no mapa
            return false;
        }
    }

    /**
     * Método adicional para debug - mostra onde está o toque
     */
    private void debugTouchPosition(MotionEvent event) {
        try {
            float touchX = event.getRawX();
            float touchY = event.getRawY();

            if (mapContainer != null) {
                int[] mapLocation = new int[2];
                mapContainer.getLocationOnScreen(mapLocation);

                android.util.Log.d("MapFriendlyScrollView",
                        String.format("Touch: (%.0f, %.0f) | Map: (%d, %d) to (%d, %d)",
                                touchX, touchY,
                                mapLocation[0], mapLocation[1],
                                mapLocation[0] + mapContainer.getWidth(),
                                mapLocation[1] + mapContainer.getHeight()));
            }

        } catch (Exception e) {
            android.util.Log.e("MapFriendlyScrollView", "Erro no debug: " + e.getMessage());
        }
    }
}