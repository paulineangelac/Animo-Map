package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.itismob.s02.group5.animomap.R;
import java.util.ArrayList;
import java.util.List;

/** Screen-sized labels and touch targets, using the same camera as the vector drawable. */
public class MapOverlayView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint route = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<MapPlace> visiblePlaces = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private ZoomableImageView map;
    private MapPlace.Category category;
    private MapPlace selected;
    private boolean demo;
    private final int surface, accent, highlight, primary, secondary;

    private static final class Label {
        final MapPlace place;
        final RectF bounds;
        Label(MapPlace place, RectF bounds) { this.place = place; this.bounds = bounds; }
    }
    public MapOverlayView(@NonNull Context c) { this(c, null); }
    public MapOverlayView(@NonNull Context c, @Nullable AttributeSet attrs) { this(c, attrs, 0); }
    public MapOverlayView(@NonNull Context c, @Nullable AttributeSet attrs, int style) {
        super(c, attrs, style);
        surface = ContextCompat.getColor(c, R.color.surface_card);
        accent = ContextCompat.getColor(c, R.color.accent_teal);
        highlight = ContextCompat.getColor(c, R.color.highlight_selected);
        primary = ContextCompat.getColor(c, R.color.text_primary);
        secondary = ContextCompat.getColor(c, R.color.text_secondary);
        text.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                getResources().getDisplayMetrics()));
        text.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        route.setStyle(Paint.Style.STROKE);
        route.setStrokeJoin(Paint.Join.ROUND);
        route.setStrokeCap(Paint.Cap.ROUND);
        setWillNotDraw(false);
    }
    public void setMapView(ZoomableImageView map) { this.map = map; invalidate(); }
    public void setActiveCategory(@Nullable MapPlace.Category category) { this.category = category; invalidate(); }
    public void setSelectedPlace(@Nullable MapPlace place) { selected = place; invalidate(); }
    public void setRouteVisible(boolean visible) { demo = visible; invalidate(); }

    private boolean sameAnchor(MapPlace a, MapPlace b) {
        return Math.abs(a.getMapX() - b.getMapX()) < .0001f
                && Math.abs(a.getMapY() - b.getMapY()) < .0001f;
    }
    private float[] point(MapPlace place) {
        if (demo && (CampusMapData.LASALLE.equals(place.getId())
                || CampusMapData.HENRY_SY.equals(place.getId()))) {
            float[][] points = CampusMapData.getDemoRoutePoints();
            float[] endpoint = points[CampusMapData.LASALLE.equals(place.getId()) ? 0 : points.length - 1];
            return map.mapPoint(endpoint[0], endpoint[1]);
        }
        return map.mapPoint(place.getMapX(), place.getMapY());
    }
    @Override protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        labels.clear();
        visiblePlaces.clear();
        if (map == null) { return; }
        RectF viewport = map.getMapViewport();
        canvas.save();
        canvas.clipRect(viewport);
        if (demo) { drawRoute(canvas); }
        if (selected != null) { visiblePlaces.add(selected); }
        for (MapPlace place : CampusMapData.getPlaces()) {
            if (place == selected || !place.hasMapMarker()
                    || (selected != null && sameAnchor(place, selected))) { continue; }
            if (category == null || category == place.getCategory()) { visiblePlaces.add(place); }
        }
        // Draw every marker first, so later markers cannot paint over earlier labels.
        for (MapPlace place : visiblePlaces) {
            float[] p = point(place);
            if (!viewport.contains(p[0], p[1])) { continue; }
            boolean isSelected = place == selected;
            fill.setColor(surface);
            canvas.drawCircle(p[0], p[1], dp(isSelected ? 10 : 6), fill);
            fill.setColor(isSelected ? highlight : accent);
            canvas.drawCircle(p[0], p[1], dp(isSelected ? 6 : 3), fill);
        }
        // Reserve label space for selected and overview buildings before other names.
        for (int priority = 0; priority < 3; priority++) {
            for (MapPlace place : visiblePlaces) {
                int level = place == selected ? 0 : place.hasOverviewLabel() ? 1 : 2;
                if (level != priority) { continue; }
                float[] p = point(place);
                if (!viewport.contains(p[0], p[1])) { continue; }
                if (place == selected || place.hasOverviewLabel() || map.getRelativeZoom() >= 1.99f
                        || place.getCategory() != MapPlace.Category.BUILDING) {
                    drawLabel(canvas, place, p[0], p[1], viewport);
                }
            }
        }
        drawMapCaption(canvas, "Taft Avenue", .067f, .55f, viewport);
        if (map.getRelativeZoom() >= 1.99f) {
            drawMapCaption(canvas, "Fidel Reyes St.", .41f, .83f, viewport);
            drawMapCaption(canvas, "SJ Walkway", .665f, .47f, viewport);
            drawMapCaption(canvas, "Castro St.", .26f, .775f, viewport);
            drawMapCaption(canvas, "Noli St.", .75f, .989f, viewport);
        }
        drawMapCaption(canvas, "N", .855f, .085f, viewport);
        canvas.restore();
    }
    private void drawRoute(Canvas canvas) {
        Path path = new Path();
        float[][] points = CampusMapData.getDemoRoutePoints();
        for (int i = 0; i < points.length; i++) {
            float[] p = map.mapPoint(points[i][0], points[i][1]);
            if (i == 0) { path.moveTo(p[0], p[1]); } else { path.lineTo(p[0], p[1]); }
        }
        route.setColor(surface);
        route.setStrokeWidth(dp(8));
        route.setPathEffect(null);
        canvas.drawPath(path, route);
        route.setColor(highlight);
        route.setStrokeWidth(dp(4));
        route.setPathEffect(new DashPathEffect(new float[] {dp(8), dp(5)}, 0));
        canvas.drawPath(path, route);
    }
    private void drawLabel(Canvas canvas, MapPlace place, float x, float y, RectF viewport) {
        String title = place.getShortLabel();
        float width = text.measureText(title) + dp(16);
        float height = text.getFontSpacing() + dp(10);
        if (width > viewport.width() || height > viewport.height()) { return; }
        float left = Math.max(viewport.left, Math.min(x - width / 2, viewport.right - width));
        float above = y - height - dp(10);
        if (place == selected) {
            above = Math.max(viewport.top, Math.min(above, viewport.bottom - height));
        }
        float[] tops = { above, y + dp(10) };
        for (float top : tops) {
            RectF rect = new RectF(left, top, left + width, top + height);
            if (!viewport.contains(rect) || overlaps(rect)) { continue; }
            labels.add(new Label(place, rect));
            fill.setColor(surface);
            canvas.drawRoundRect(rect, dp(6), dp(6), fill);
            text.setColor(place == selected ? highlight : primary);
            canvas.drawText(title, rect.left + dp(8), rect.top + dp(5) - text.ascent(), text);
            return;
        }
    }
    private boolean overlaps(RectF rect) {
        RectF padded = new RectF(rect);
        padded.inset(-dp(3), -dp(3));
        for (Label label : labels) {
            if (RectF.intersects(padded, label.bounds)) { return true; }
        }
        return false;
    }
    private void drawMapCaption(Canvas canvas, String caption, float x, float y, RectF viewport) {
        float[] p = map.mapPoint(x, y);
        float width = text.measureText(caption);
        if (!viewport.contains(p[0], p[1]) || width > viewport.width()) { return; }
        p[0] = Math.max(viewport.left + width / 2,
                Math.min(p[0], viewport.right - width / 2));
        RectF rect = new RectF(p[0] - width / 2, p[1] + text.ascent(),
                p[0] + width / 2, p[1] - text.ascent());
        if (!viewport.contains(rect) || overlaps(rect)) { return; }
        text.setColor(secondary);
        canvas.drawText(caption, rect.left, p[1], text);
        // Street names also reserve space, although they are not selectable places.
        labels.add(new Label(null, rect));
    }
    /** Nearest visible marker within a 48dp target; a visible label is also a target. */
    @Nullable public MapPlace findPlaceAt(float x, float y) {
        if (map == null || !map.getMapViewport().contains(x, y)) { return null; }
        // A name drawn over a marker should receive the tap on its visible text.
        for (Label label : labels) {
            if (label.place != null && label.bounds.contains(x, y)) { return label.place; }
        }
        MapPlace nearest = null;
        double distance = dp(24);
        for (MapPlace place : visiblePlaces) {
            float[] p = point(place);
            if (!map.getMapViewport().contains(p[0], p[1])) { continue; }
            double d = Math.hypot(x - p[0], y - p[1]);
            if (d < distance) { distance = d; nearest = place; }
        }
        if (nearest != null) { return nearest; }
        double labelDistance = Double.MAX_VALUE;
        for (Label label : labels) {
            if (label.place == null) { continue; }
            RectF target = new RectF(label.bounds);
            target.inset(-Math.max(0, (dp(48) - target.width()) / 2),
                    -Math.max(0, (dp(48) - target.height()) / 2));
            double d = Math.hypot(x - target.centerX(), y - target.centerY());
            if (target.contains(x, y) && d < labelDistance) {
                nearest = label.place;
                labelDistance = d;
            }
        }
        return nearest;
    }
    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
}
