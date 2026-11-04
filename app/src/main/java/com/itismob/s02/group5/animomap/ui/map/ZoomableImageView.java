package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Custom ImageView that provides initial height-based scaling,
 * horizontal panning, and pinch-to-zoom for campus maps.
 */
public class ZoomableImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private float minScale = 1.0f;
    private float maxScale = 3.0f;
    private float currentScale = 1.0f;

    private boolean isInitialized = false;

    private float lastTouchX;
    private float lastTouchY;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;

    public ZoomableImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if ((changed || !isInitialized) && getWidth() > 0 && getHeight() > 0) {
            initMapScaleAndPosition();
        }
    }

    /**
     * Scales the map image so its height fills the available view height
     * and centers it horizontally as its initial position.
     */
    private void initMapScaleAndPosition() {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }

        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        int imgWidth = drawable.getIntrinsicWidth();
        int imgHeight = drawable.getIntrinsicHeight();

        // Scale image height to fill the available view height
        minScale = (float) viewHeight / (float) imgHeight;
        maxScale = minScale * 3.0f;
        currentScale = minScale;

        // Reset matrix and apply initial height scale
        matrix.reset();
        matrix.postScale(minScale, minScale);

        // Center the map horizontally
        float scaledWidth = imgWidth * minScale;
        float initialTranslationX = (viewWidth - scaledWidth) / 2.0f;
        float initialTranslationY = 0f;

        matrix.postTranslate(initialTranslationX, initialTranslationY);

        setImageMatrix(matrix);
        isInitialized = true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Delegate gestures to detectors
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                activePointerId = event.getPointerId(0);
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex != -1 && !scaleGestureDetector.isInProgress()) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);

                    float dx = x - lastTouchX;
                    float rawDy = y - lastTouchY;

                    // Lock vertical scrolling if map height matches view height
                    float dy = (Math.abs(currentScale - minScale) < 0.001f) ? 0f : rawDy;

                    matrix.postTranslate(dx, dy);
                    clampMatrixBounds();
                    setImageMatrix(matrix);

                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int actionIndex = event.getActionIndex();
                int pointerId = event.getPointerId(actionIndex);
                if (pointerId == activePointerId) {
                    int newPointerIndex = actionIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_UP:
                performClick();
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_CANCEL:
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float rawScaleFactor = detector.getScaleFactor();
            float newScale = currentScale * rawScaleFactor;
            float scaleFactor;

            // Clamp new scale between minScale and maxScale
            if (newScale < minScale) {
                scaleFactor = minScale / currentScale;
                currentScale = minScale;
            } else if (newScale > maxScale) {
                scaleFactor = maxScale / currentScale;
                currentScale = maxScale;
            } else {
                scaleFactor = rawScaleFactor;
                currentScale = newScale;
            }

            // Scale around gesture focus point
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            clampMatrixBounds();
            setImageMatrix(matrix);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            // Reset to default scale on double tap if zoomed in, or zoom in 2x if at default scale
            if (currentScale > minScale + 0.01f) {
                initMapScaleAndPosition();
            } else {
                float targetScale = minScale * 2.0f;
                float scaleFactor = targetScale / currentScale;
                currentScale = targetScale;
                matrix.postScale(scaleFactor, scaleFactor, e.getX(), e.getY());
                clampMatrixBounds();
                setImageMatrix(matrix);
            }
            return true;
        }
    }

    /**
     * Prevents the image from leaving empty background gaps outside the view bounds.
     */
    private void clampMatrixBounds() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        RectF rect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);

        float viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        float deltaX;
        float deltaY;

        // Horizontal boundaries
        if (rect.width() >= viewWidth) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            } else {
                deltaX = 0;
            }
        } else {
            deltaX = (viewWidth / 2f) - (rect.left + rect.width() / 2f);
        }

        // Vertical boundaries
        if (rect.height() >= viewHeight) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = viewHeight - rect.bottom;
            } else {
                deltaY = 0;
            }
        } else {
            deltaY = (viewHeight / 2f) - (rect.top + rect.height() / 2f);
        }

        matrix.postTranslate(deltaX, deltaY);
    }
}
