package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

import com.itismob.s02.group5.animomap.R;

/** The same campus category badges are used on the public map and in the location picker. */
public final class CampusMarkerIcons {
    private CampusMarkerIcons() { }

    public static Bitmap create(Context context, MapPlace.Category category, boolean selected) {
        return create(context, category, selected, false);
    }

    /** A hotspot uses the flame symbol instead of its category symbol. */
    public static Bitmap create(Context context, MapPlace.Category category, boolean selected,
                                boolean hotspot) {
        float density = context.getResources().getDisplayMetrics().density;
        int size = Math.round(sizeDp(category, selected) * density);
        boolean building = category == MapPlace.Category.BUILDING;
        float stroke = Math.round((selected ? 2.5f : (building ? 1.5f : 2f)) * density);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float center = size / 2f;
        paint.setColor(ContextCompat.getColor(context, R.color.surface_card));
        canvas.drawCircle(center, center, center, paint);
        paint.setStyle(Paint.Style.STROKE);
        // Each category has its own colour; hotspots are the only amber marker.
        int base = ContextCompat.getColor(context, hotspot ? R.color.hotspot_amber : colorRes(category));
        // A selected marker is a lighter tint of its own colour, and larger.
        int accent = selected ? ColorUtils.blendARGB(base, Color.WHITE, 0.4f) : base;
        paint.setColor(accent);
        paint.setStrokeWidth(stroke);
        canvas.drawCircle(center, center, center - stroke / 2f, paint);
        Drawable glyph = ContextCompat.getDrawable(context,
                hotspot ? R.drawable.ic_marker_hotspot : MapPlace.glyphRes(category));
        if (glyph != null) {
            glyph = DrawableCompat.wrap(glyph.mutate());
            DrawableCompat.setTint(glyph, accent);
            int inset = Math.round(size * 0.26f);
            glyph.setBounds(inset, inset, size - inset, size - inset);
            glyph.draw(canvas);
        }
        return bitmap;
    }

    /** The colour that identifies a category on the map. */
    private static int colorRes(MapPlace.Category category) {
        switch (category) {
            case FOOD: return R.color.marker_food;
            case RESTROOM: return R.color.marker_restroom;
            case STUDY: return R.color.marker_study;
            case HANGOUT: return R.color.marker_hangout;
            default: return R.color.accent_teal;
        }
    }

    public static int sizeDp(MapPlace.Category category, boolean selected) {
        if (category == MapPlace.Category.BUILDING) return selected ? 40 : 28;
        return selected ? 42 : 32;
    }

}
