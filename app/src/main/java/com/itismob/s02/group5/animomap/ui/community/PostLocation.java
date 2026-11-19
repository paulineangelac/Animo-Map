package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.os.Bundle;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.map.MapPlace;

/** An attachment to a post. A free pin has coordinates and no campus place ID. */
final class PostLocation {
    final String placeId;
    final String name;
    final String detail;
    final double latitude;
    final double longitude;

    private PostLocation(String placeId, String name, String detail,
                         double latitude, double longitude) {
        this.placeId = placeId;
        this.name = name;
        this.detail = detail;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    static PostLocation fromPlace(MapPlace place) {
        return new PostLocation(place.getId(), place.getName(), place.getLocationLine(),
                place.getLatitude(), place.getLongitude());
    }

    static PostLocation pin(Context context, double latitude, double longitude) {
        return new PostLocation(null, context.getString(R.string.community_pinned_location),
                context.getString(R.string.community_coordinates, latitude, longitude),
                latitude, longitude);
    }

    String label(Context context) {
        return detail == null || detail.isEmpty() || detail.equals(name) ? name
                : context.getString(R.string.community_meta_pair, name, detail);
    }

    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("place_id", placeId);
        bundle.putString("name", name);
        bundle.putString("detail", detail);
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        return bundle;
    }

    static PostLocation fromBundle(Bundle bundle) {
        if (bundle == null) return null;
        return new PostLocation(bundle.getString("place_id"), bundle.getString("name"),
                bundle.getString("detail"), bundle.getDouble("latitude"),
                bundle.getDouble("longitude"));
    }
}
