package com.example.safemode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

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

    public void setMapContainer(View mapContainer) {
        this.mapContainer = mapContainer;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            if (mapContainer == null) {
                return super.onInterceptTouchEvent(event);
            }

            if (isTouchInsideMapArea(event)) {
                return false;
            }

            return super.onInterceptTouchEvent(event);

        } catch (Exception e) {
            return super.onInterceptTouchEvent(event);
        }
    }

    private boolean isTouchInsideMapArea(MotionEvent event) {
        try {
            if (mapContainer == null) {
                return false;
            }

            float touchX = event.getRawX();
            float touchY = event.getRawY();

            int[] mapLocation = new int[2];
            mapContainer.getLocationOnScreen(mapLocation);

            int mapLeft = mapLocation[0];
            int mapTop = mapLocation[1];
            int mapRight = mapLeft + mapContainer.getWidth();
            int mapBottom = mapTop + mapContainer.getHeight();

            boolean isInsideMap = touchX >= mapLeft && touchX <= mapRight &&
                    touchY >= mapTop && touchY <= mapBottom;

            return isInsideMap;

        } catch (Exception e) {
            return false;
        }
    }
}