package com.itismob.s02.group5.animomap.ui.community;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.itismob.s02.group5.animomap.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Loads only a display-sized image and ignores results for recycled or removed views. */
final class PostPhotoLoader {
    private static final ExecutorService DECODER = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final int MAX_IMPORT_SIDE = 1440;

    private PostPhotoLoader() { }

    static void load(ImageView view, byte[] photo, int maxDp, int position, int count) {
        clear(view);
        view.setContentDescription(view.getContext().getString(
                R.string.community_photo_accessibility, position, count));
        int pixels = Math.round(maxDp * view.getResources().getDisplayMetrics().density);
        Object request = new Object();
        view.setTag(request);
        Future<?> job = DECODER.submit(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
            configureSampleSize(options, pixels);
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
            MAIN.post(() -> {
                if (view.getTag() != request) return;
                view.setTag(null);
                view.setTag(R.id.community_photo_load_job, null);
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                } else {
                    view.setImageResource(R.drawable.ic_community_photo);
                    view.setContentDescription(view.getContext().getString(
                            R.string.community_photo_unavailable, position, count));
                }
            });
        });
        view.setTag(R.id.community_photo_load_job, job);
    }

    @SuppressWarnings("deprecation") // The older WebP constant is required before API 30.
    static Future<?> importPhoto(ContentResolver resolver, Uri uri, Consumer<byte[]> onImported) {
        return DECODER.submit(() -> {
            Bitmap bitmap = decode(resolver, uri);
            byte[] encoded = null;
            if (bitmap != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                // Keep a display-sized copy in RAM before the picker's temporary access ends.
                Bitmap.CompressFormat format = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                        ? Bitmap.CompressFormat.WEBP_LOSSY : Bitmap.CompressFormat.WEBP;
                try {
                    if (bitmap.compress(format, 82, output)) encoded = output.toByteArray();
                } catch (RuntimeException ignored) {
                    // Report the attachment as unavailable if the device cannot encode it.
                } finally {
                    bitmap.recycle();
                }
            }
            byte[] result = encoded;
            if (!Thread.currentThread().isInterrupted()) MAIN.post(() -> onImported.accept(result));
        });
    }

    static void clear(ImageView view) {
        Future<?> pending = (Future<?>) view.getTag(R.id.community_photo_load_job);
        if (pending != null) pending.cancel(true);
        view.setTag(null);
        view.setTag(R.id.community_photo_load_job, null);
        view.setImageDrawable(null);
    }

    private static Bitmap decode(ContentResolver resolver, Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return decodeModern(resolver, uri);
            }
            return decodeLegacy(resolver, uri);
        } catch (IOException | RuntimeException ignored) {
            // The media might have been deleted or its temporary grant might have expired.
            return null;
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private static Bitmap decodeModern(ContentResolver resolver, Uri uri)
            throws IOException {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri),
                (decoder, info, source) -> {
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    int width = info.getSize().getWidth();
                    int height = info.getSize().getHeight();
                    float scale = Math.min(1f, (float) MAX_IMPORT_SIDE / Math.max(width, height));
                    decoder.setTargetSize(Math.max(1, Math.round(width * scale)),
                            Math.max(1, Math.round(height * scale)));
                });
    }

    private static Bitmap decodeLegacy(ContentResolver resolver, Uri uri)
            throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) return null;
        configureSampleSize(options, MAX_IMPORT_SIDE);
        Bitmap bitmap;
        try (InputStream stream = resolver.openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(stream, null, options);
        }
        if (bitmap == null) return null;

        Matrix matrix = new Matrix();
        try (InputStream stream = resolver.openInputStream(uri)) {
            if (stream == null) return bitmap;
            int orientation = new ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90); matrix.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(270); matrix.postScale(-1, 1); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(270); break;
                default: return bitmap;
            }
        } catch (IOException ignored) {
            return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static void configureSampleSize(BitmapFactory.Options options, int maxPixels) {
        int largestSide = Math.max(options.outWidth, options.outHeight);
        options.inSampleSize = 1;
        while (largestSide / options.inSampleSize > maxPixels) options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
    }
}
