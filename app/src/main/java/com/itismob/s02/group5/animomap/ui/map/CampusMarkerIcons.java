package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
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
        // Hotspots are the only warm marker; everything else stays teal.
        int accentRes = hotspot ? R.color.hotspot_amber
                : (selected ? R.color.highlight_selected : R.color.accent_teal);
        int accent = ContextCompat.getColor(context, accentRes);
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

    public static int sizeDp(MapPlace.Category category, boolean selected) {
        if (category == MapPlace.Category.BUILDING) return selected ? 40 : 28;
        return selected ? 42 : 32;
    }

}
