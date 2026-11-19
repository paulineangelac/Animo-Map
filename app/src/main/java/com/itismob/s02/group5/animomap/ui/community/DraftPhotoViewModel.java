package com.itismob.s02.group5.animomap.ui.community;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/** Keeps draft images and in-progress imports across rotation, in memory only. */
public final class DraftPhotoViewModel extends ViewModel {
    static final int MAX_PHOTOS = 6;

    private final List<Photo> photos = new ArrayList<>();
    private final MutableLiveData<List<Photo>> previews =
            new MutableLiveData<>(Collections.emptyList());

    LiveData<List<Photo>> previews() { return previews; }

    List<Photo> photos() { return photos; }

    boolean add(ContentResolver resolver, List<Uri> picked) {
        boolean overflow = false;
        for (Uri uri : picked) {
            if (contains(uri)) continue;
            if (photos.size() == MAX_PHOTOS) {
                overflow = true;
                break;
            }
            Photo photo = new Photo(uri);
            photos.add(photo);
            photo.job = PostPhotoLoader.importPhoto(resolver, uri, image -> {
                if (!photos.contains(photo)) return;
                photo.job = null;
                photo.image = image;
                showChanges();
            });
        }
        showChanges();
        return overflow;
    }

    void remove(Photo photo) {
        if (photo.job != null) photo.job.cancel(true);
        photos.remove(photo);
        showChanges();
    }

    @Override
    protected void onCleared() {
        for (Photo photo : photos) {
            if (photo.job != null) photo.job.cancel(true);
        }
        photos.clear();
    }

    private boolean contains(Uri uri) {
        for (Photo photo : photos) {
            if (photo.source.equals(uri)) return true;
        }
        return false;
    }

    private void showChanges() {
        previews.setValue(new ArrayList<>(photos));
    }

    static final class Photo {
        final Uri source;
        byte[] image;
        Future<?> job;

        Photo(Uri source) { this.source = source; }
    }
}
