package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/** A small matrix-based camera shared by the vector map and its overlay. */
public class ZoomableImageView extends AppCompatImageView {
    public interface OnMapTapListener { void onTap(float screenX, float screenY); }
    private final Matrix matrix = new Matrix();
    private final RectF viewport = new RectF();
    private final RectF insets = new RectF();
    private final GestureDetector gestures;
    private final ScaleGestureDetector pinch;
    private final int touchSlop;
    private float minScale = 1f, scale = 1f;
    private float lastX, lastY, downX, downY;
    private int pointer = MotionEvent.INVALID_POINTER_ID;
    private boolean initialized, overview = true, dragged;
    private float[] pendingState;
    private Runnable matrixListener;
    private OnMapTapListener tapListener;

    public ZoomableImageView(@NonNull Context context) { this(context, null); }
    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int style) {
        super(context, attrs, style);
        setScaleType(ScaleType.MATRIX);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(@NonNull MotionEvent e) { return true; }
            @Override public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (!dragged && tapListener != null && viewport.contains(e.getX(), e.getY())) {
                    tapListener.onTap(e.getX(), e.getY());
                    performClick();
                }
                return true;
            }
            @Override public boolean onDoubleTap(@NonNull MotionEvent e) {
                zoomAt(1.5f, e.getX(), e.getY());
                return true;
            }
        });
        pinch = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        dragged = true;
                        zoomAt(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                        return true;
                    }
                });
    }
    public void setOnImageMatrixChangedListener(@Nullable Runnable listener) {
        matrixListener = listener;
        changed();
    }
    public void setOnMapTapListener(@Nullable OnMapTapListener listener) { tapListener = listener; }
    public RectF getMapViewport() { return new RectF(viewport); }
    public float getRelativeZoom() { return initialized ? scale / minScale : 1f; }
    public boolean canZoomIn() { return initialized && getRelativeZoom() < 3.999f; }
    public boolean canZoomOut() { return initialized && getRelativeZoom() > 1.001f; }

    public void setViewportInsets(float left, float top, float right, float bottom) {
        if (insets.left == left && insets.top == top && insets.right == right
                && insets.bottom == bottom && initialized) { return; }
        float[] state = initialized ? getCameraState() : pendingState;
        insets.set(left, top, right, bottom);
        rebuildViewport(state);
    }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float[] state = initialized ? getCameraState() : pendingState;
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildViewport(state);
    }
    private void rebuildViewport(float[] state) {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() <= 0 || getHeight() <= 0) { return; }
        float right = Math.max(insets.left + 1, getWidth() - insets.right);
        float bottom = Math.max(insets.top + 1, getHeight() - insets.bottom);
        viewport.set(insets.left, insets.top, right, bottom);
        minScale = Math.min(viewport.width() / drawable.getIntrinsicWidth(),
                viewport.height() / drawable.getIntrinsicHeight());
        initialized = true;
        pendingState = null;
        if (state == null || state[3] == 1f) { showOverview(); }
        else { overview = false; position(state[0], state[1], state[2]); }
    }
    public void showOverview() {
        if (!initialized) { return; }
        overview = true;
        position(.5f, .5f, 1f);
    }
    private void position(float x, float y, float relativeZoom) {
        Drawable drawable = getDrawable();
        if (drawable == null) { return; }
        scale = minScale * Math.max(1f, Math.min(4f, relativeZoom));
        matrix.reset();
        matrix.postScale(scale, scale);
        matrix.postTranslate(viewport.centerX() - x * drawable.getIntrinsicWidth() * scale,
                viewport.centerY() - y * drawable.getIntrinsicHeight() * scale);
        clamp();
        changed();
    }
    public void focusOnPoint(float x, float y) {
        overview = false;
        position(x, y, Math.max(2f, getRelativeZoom()));
    }
    public void focusOnBounds(RectF normalizedBounds) {
        Drawable drawable = getDrawable();
        if (!initialized || drawable == null) { return; }
        float pad = 32f * getResources().getDisplayMetrics().density;
        // A fixed margin would consume most of a short landscape viewport.
        float padX = Math.min(pad, viewport.width() * .1f);
        float padY = Math.min(pad, viewport.height() * .1f);
        float fitX = Math.max(1, viewport.width() - padX * 2)
                / Math.max(1, normalizedBounds.width() * drawable.getIntrinsicWidth());
        float fitY = Math.max(1, viewport.height() - padY * 2)
                / Math.max(1, normalizedBounds.height() * drawable.getIntrinsicHeight());
        overview = false;
        position(normalizedBounds.centerX(), normalizedBounds.centerY(),
                Math.min(fitX, fitY) / minScale);
    }
    public void zoomBy(float factor) { zoomAt(factor, viewport.centerX(), viewport.centerY()); }
    private void zoomAt(float factor, float x, float y) {
        if (!initialized) { return; }
        float next = Math.max(minScale, Math.min(minScale * 4f, scale * factor));
        matrix.postScale(next / scale, next / scale, x, y);
        scale = next;
        overview = Math.abs(scale - minScale) < .0001f;
        clamp();
        changed();
    }
    public float[] mapPoint(float x, float y) {
        Drawable drawable = getDrawable();
        if (drawable == null) { return new float[] {0, 0}; }
        float[] point = {x * drawable.getIntrinsicWidth(), y * drawable.getIntrinsicHeight()};
        matrix.mapPoints(point);
        return point;
    }
    public float[] screenToMap(float x, float y) {
        Drawable drawable = getDrawable();
        Matrix inverse = new Matrix();
        if (drawable == null || !matrix.invert(inverse)) { return new float[] {.5f, .5f}; }
        float[] point = {x, y};
        inverse.mapPoints(point);
        point[0] /= drawable.getIntrinsicWidth();
        point[1] /= drawable.getIntrinsicHeight();
        return point;
    }
    /** Center in map coordinates, relative zoom, and whether this is the overview. */
    public float[] getCameraState() {
        if (!initialized) { return pendingState == null ? new float[] {.5f, .5f, 1, 1} : pendingState.clone(); }
        float[] center = screenToMap(viewport.centerX(), viewport.centerY());
        return new float[] {center[0], center[1], getRelativeZoom(), overview ? 1f : 0f};
    }
    public void restoreCameraState(float[] state) {
        if (state == null || state.length != 4) { return; }
        pendingState = state.clone();
        if (initialized) {
            if (state[3] == 1f) { showOverview(); }
            else { overview = false; position(state[0], state[1], state[2]); }
        }
    }
    private void clamp() {
        Drawable drawable = getDrawable();
        if (drawable == null) { return; }
        RectF bounds = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(bounds);
        float dx = bounds.width() <= viewport.width() ? viewport.centerX() - bounds.centerX()
                : bounds.left > viewport.left ? viewport.left - bounds.left
                : bounds.right < viewport.right ? viewport.right - bounds.right : 0;
        float dy = bounds.height() <= viewport.height() ? viewport.centerY() - bounds.centerY()
                : bounds.top > viewport.top ? viewport.top - bounds.top
                : bounds.bottom < viewport.bottom ? viewport.bottom - bounds.bottom : 0;
        matrix.postTranslate(dx, dy);
    }
    private void changed() {
        setImageMatrix(matrix);
        if (matrixListener != null) { matrixListener.run(); }
    }
    @Override public boolean performClick() { return super.performClick(); }
    // performClick runs in onSingleTapConfirmed, after rejecting drags and pinches.
    @android.annotation.SuppressLint("ClickableViewAccessibility")
    @Override public boolean onTouchEvent(MotionEvent e) {
        pinch.onTouchEvent(e);
        gestures.onTouchEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = e.getX();
                downY = lastY = e.getY();
                dragged = false;
                pointer = e.getPointerId(0);
                if (getParent() != null) { getParent().requestDisallowInterceptTouchEvent(true); }
                break;
            case MotionEvent.ACTION_MOVE:
                int index = e.findPointerIndex(pointer);
                if (index >= 0) {
                    float x = e.getX(index), y = e.getY(index);
                    if (Math.hypot(x - downX, y - downY) > touchSlop) { dragged = true; }
                    if (!pinch.isInProgress() && dragged) {
                        matrix.postTranslate(x - lastX, y - lastY);
                        clamp();
                        changed();
                    }
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (e.getPointerId(e.getActionIndex()) == pointer) {
                    int next = e.getActionIndex() == 0 ? 1 : 0;
                    pointer = e.getPointerId(next);
                    lastX = e.getX(next);
                    lastY = e.getY(next);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pointer = MotionEvent.INVALID_POINTER_ID;
                if (getParent() != null) { getParent().requestDisallowInterceptTouchEvent(false); }
                break;
            default: break;
        }
        return true;
    }
}
