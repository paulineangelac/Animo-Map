package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

/** Draws sample place markers and an illustrative route in the campus map's image coordinates. */
public class MapOverlayView extends View {

    private static final float ORIGIN_X = 0.21f;
    private static final float ORIGIN_Y = 0.71f;

    private final Paint markerFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelSubtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint routeOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix imageMatrix = new Matrix();
    private final Path routePath = new Path();
    private final RectF labelRect = new RectF();
    private final float[] pointBuffer = new float[2];

    private int surfaceColor;
    private int accentColor;
    private int selectedColor;
    private int primaryTextColor;
    private int secondaryTextColor;

    private ZoomableImageView mapView;
    private List<MapPlace> places = new ArrayList<>();
    private MapPlace.Category activeCategory = MapPlace.Category.BUILDING;
    private MapPlace selectedPlace;
    private boolean routeVisible;

    public MapOverlayView(@NonNull Context context) {
        super(context);
        init();
    }

    public MapOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapOverlayView(@NonNull Context context, @Nullable AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        markerFillPaint.setStyle(Paint.Style.FILL);
        markerOutlinePaint.setStyle(Paint.Style.FILL);
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        labelOutlinePaint.setStyle(Paint.Style.STROKE);
        labelOutlinePaint.setStrokeWidth(dp(1));
        labelTitlePaint.setTextSize(sp(13));
        labelTitlePaint.setTypeface(android.graphics.Typeface.create(
                "sans-serif-medium", android.graphics.Typeface.NORMAL));
        labelSubtitlePaint.setTextSize(sp(11));
        routeOutlinePaint.setStyle(Paint.Style.STROKE);
        routeOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        routeOutlinePaint.setStrokeJoin(Paint.Join.ROUND);
        routeOutlinePaint.setStrokeWidth(dp(8));
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setStrokeCap(Paint.Cap.ROUND);
        routePaint.setStrokeJoin(Paint.Join.ROUND);
        routePaint.setStrokeWidth(dp(4));
        routePaint.setPathEffect(new DashPathEffect(new float[]{dp(11), dp(7)}, 0));
        surfaceColor = ContextCompat.getColor(getContext(), R.color.surface_card);
        accentColor = ContextCompat.getColor(getContext(), R.color.accent_teal);
        selectedColor = ContextCompat.getColor(getContext(), R.color.highlight_selected);
        primaryTextColor = ContextCompat.getColor(getContext(), R.color.text_primary);
        secondaryTextColor = ContextCompat.getColor(getContext(), R.color.text_secondary);
        setWillNotDraw(false);
    }

    public void setMapView(@Nullable ZoomableImageView mapView) {
        this.mapView = mapView;
        invalidate();
    }

    public void setPlaces(@NonNull List<MapPlace> places) {
        this.places = new ArrayList<>(places);
        invalidate();
    }

    public void setActiveCategory(@NonNull MapPlace.Category activeCategory) {
        this.activeCategory = activeCategory;
        invalidate();
    }

    public void setSelectedPlace(@Nullable MapPlace selectedPlace) {
        this.selectedPlace = selectedPlace;
        invalidate();
    }

    public void setRouteVisible(boolean routeVisible) {
        this.routeVisible = routeVisible;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mapView == null) {
            return;
        }

        Drawable mapDrawable = mapView.getDrawable();
        if (mapDrawable == null || mapDrawable.getIntrinsicWidth() <= 0
                || mapDrawable.getIntrinsicHeight() <= 0) {
            return;
        }

        float imageWidth = mapDrawable.getIntrinsicWidth();
        float imageHeight = mapDrawable.getIntrinsicHeight();
        imageMatrix.set(mapView.getImageMatrix());

        if (routeVisible && selectedPlace != null) {
            drawIllustrativeRoute(canvas, imageMatrix, imageWidth, imageHeight);
        }

        for (MapPlace place : places) {
            if (place.getCategory() == activeCategory) {
                mapPointToScreen(place.getMapX(), place.getMapY(), imageWidth, imageHeight,
                        imageMatrix, pointBuffer);
                float pointX = pointBuffer[0];
                float pointY = pointBuffer[1];
                if (isOnScreen(pointX, pointY)) {
                    boolean selected = place == selectedPlace;
                    drawMarker(canvas, pointX, pointY, selected);
                    if (selected || activeCategory != MapPlace.Category.BUILDING) {
                        drawLabel(canvas, pointX, pointY, place.getName(),
                                place.getSubtitle(), true);
                    }
                }
            }
        }

        if (selectedPlace != null && selectedPlace.getCategory() != activeCategory) {
            mapPointToScreen(selectedPlace.getMapX(), selectedPlace.getMapY(), imageWidth,
                    imageHeight, imageMatrix, pointBuffer);
            float selectedX = pointBuffer[0];
            float selectedY = pointBuffer[1];
            if (isOnScreen(selectedX, selectedY)) {
                drawMarker(canvas, selectedX, selectedY, true);
                drawLabel(canvas, selectedX, selectedY, selectedPlace.getName(),
                        selectedPlace.getSubtitle(), true);
            }
        }

        if (routeVisible && selectedPlace != null) {
            drawOriginMarker(canvas, imageWidth, imageHeight, imageMatrix);
            mapPointToScreen(selectedPlace.getMapX(), selectedPlace.getMapY(), imageWidth,
                    imageHeight, imageMatrix, pointBuffer);
            float destinationX = pointBuffer[0];
            float destinationY = pointBuffer[1];
            if (isOnScreen(destinationX, destinationY)) {
                drawMarker(canvas, destinationX, destinationY, true);
                drawLabel(canvas, destinationX, destinationY, selectedPlace.getName(),
                        selectedPlace.getSubtitle(), true);
            }
        }
    }

    private void drawIllustrativeRoute(Canvas canvas, Matrix imageMatrix, float imageWidth,
                                       float imageHeight) {
        routePath.reset();
        routePath.moveTo(ORIGIN_X * imageWidth, ORIGIN_Y * imageHeight);
        routePath.lineTo(0.27f * imageWidth, 0.71f * imageHeight);
        routePath.lineTo(0.27f * imageWidth, 0.64f * imageHeight);
        routePath.lineTo(0.38f * imageWidth, 0.64f * imageHeight);
        routePath.lineTo(0.38f * imageWidth, 0.55f * imageHeight);
        routePath.lineTo(0.56f * imageWidth, 0.55f * imageHeight);
        routePath.lineTo(0.56f * imageWidth, selectedPlace.getMapY() * imageHeight);
        routePath.lineTo(selectedPlace.getMapX() * imageWidth,
                selectedPlace.getMapY() * imageHeight);

        routePath.transform(imageMatrix);
        routeOutlinePaint.setColor(surfaceColor);
        routePaint.setColor(accentColor);
        canvas.drawPath(routePath, routeOutlinePaint);
        canvas.drawPath(routePath, routePaint);
    }

    private void drawOriginMarker(Canvas canvas, float imageWidth, float imageHeight,
                                  Matrix imageMatrix) {
        mapPointToScreen(ORIGIN_X, ORIGIN_Y, imageWidth, imageHeight, imageMatrix,
                pointBuffer);
        float originX = pointBuffer[0];
        float originY = pointBuffer[1];
        if (!isOnScreen(originX, originY)) {
            return;
        }
        drawMarker(canvas, originX, originY, true);
        drawLabel(canvas, originX, originY,
                getContext().getString(R.string.map_route_origin),
                getContext().getString(R.string.map_route_demo_start), false);
    }

    private void drawMarker(Canvas canvas, float x, float y, boolean selected) {
        float outerRadius = selected ? dp(12) : dp(9);
        float innerRadius = selected ? dp(7) : dp(5);
        markerOutlinePaint.setColor(surfaceColor);
        markerFillPaint.setColor(selected ? selectedColor : accentColor);
        canvas.drawCircle(x, y, outerRadius, markerOutlinePaint);
        canvas.drawCircle(x, y, innerRadius, markerFillPaint);
    }

    private void drawLabel(Canvas canvas, float x, float y, String title, String subtitle,
                           boolean placeAbove) {
        labelBackgroundPaint.setColor(surfaceColor);
        labelOutlinePaint.setColor(accentColor);
        labelTitlePaint.setColor(primaryTextColor);
        labelSubtitlePaint.setColor(secondaryTextColor);

        float horizontalPadding = dp(11);
        float titleWidth = labelTitlePaint.measureText(title);
        float subtitleWidth = labelSubtitlePaint.measureText(subtitle);
        float labelWidth = Math.max(titleWidth, subtitleWidth) + horizontalPadding * 2;
        float labelHeight = dp(39);
        float left = x + dp(13);
        if (left + labelWidth > getWidth() - dp(8)) {
            left = x - labelWidth - dp(13);
        }
        left = Math.max(dp(8), Math.min(left, getWidth() - labelWidth - dp(8)));

        float top = placeAbove ? y - labelHeight - dp(12) : y + dp(13);
        if (top < dp(8)) {
            top = y + dp(13);
        }
        if (top + labelHeight > getHeight() - dp(8)) {
            top = Math.max(dp(8), y - labelHeight - dp(12));
        }

        labelRect.set(left, top, left + labelWidth, top + labelHeight);
        canvas.drawRoundRect(labelRect, dp(12), dp(12), labelBackgroundPaint);
        canvas.drawRoundRect(labelRect, dp(12), dp(12), labelOutlinePaint);
        canvas.drawText(title, left + horizontalPadding, top + dp(16), labelTitlePaint);
        canvas.drawText(subtitle, left + horizontalPadding, top + dp(31), labelSubtitlePaint);
    }

    private void mapPointToScreen(float normalizedX, float normalizedY, float imageWidth,
                                  float imageHeight, Matrix imageMatrix, float[] outputPoint) {
        outputPoint[0] = normalizedX * imageWidth;
        outputPoint[1] = normalizedY * imageHeight;
        imageMatrix.mapPoints(outputPoint);
    }

    private boolean isOnScreen(float x, float y) {
        return x >= -dp(24) && x <= getWidth() + dp(24)
                && y >= -dp(24) && y <= getHeight() + dp(24);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                getResources().getDisplayMetrics());
    }
}
