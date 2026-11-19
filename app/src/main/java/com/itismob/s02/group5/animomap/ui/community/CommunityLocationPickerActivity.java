package com.itismob.s02.group5.animomap.ui.community;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.map.CampusMapData;
import com.itismob.s02.group5.animomap.ui.map.CampusMarkerIcons;
import com.itismob.s02.group5.animomap.ui.map.MapPlace;
import com.itismob.s02.group5.animomap.ui.map.MapSearchAdapter;
import com.mapbox.common.MapboxOptions;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraBoundsOptions;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.MapLoadingErrorType;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.plugin.annotation.AnnotationsUtils;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityLocationPickerActivity extends AppCompatActivity {
    static final String EXTRA_LOCATION = "community.location";
    private static final String PIN_MODE = "pin_mode";
    private static final String MAP_CAMERA = "camera";
    private static final String LOCAL_MAP_STYLE = "asset://community_picker_map.json";

    private View root;
    private EditText query;
    private TextView selection;
    private View confirm;
    private MapSearchAdapter resultsAdapter;
    private boolean pinMode;
    private PostLocation location;
    private double[] restoredCamera;
    private MapView mapView;
    private boolean usingLocalStyle;
    private PointAnnotationManager selectionMarkers;
    private Bitmap pinIcon;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        CommunityWindow.enableEdgeToEdge(this);
        setContentView(R.layout.activity_community_location_picker);
        root = findViewById(R.id.community_picker_root);
        CommunityWindow.applyInsets(root, true);
        query = findViewById(R.id.community_picker_query);
        selection = findViewById(R.id.community_picker_selection);
        confirm = findViewById(R.id.community_picker_confirm);

        location = PostLocation.fromBundle(state == null ? getIntent().getBundleExtra(EXTRA_LOCATION)
                : state.getBundle(EXTRA_LOCATION));
        if (state != null) {
            pinMode = state.getBoolean(PIN_MODE);
            restoredCamera = state.getDoubleArray(MAP_CAMERA);
        }

        RecyclerView results = findViewById(R.id.community_picker_results);
        results.setLayoutManager(new LinearLayoutManager(this));
        resultsAdapter = new MapSearchAdapter(place -> {
            choose(PostLocation.fromPlace(place));
            hideKeyboard();
        });
        results.setAdapter(resultsAdapter);
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                showSearchResults(text.toString().trim());
            }
            @Override public void afterTextChanged(Editable text) { }
        });

        findViewById(R.id.community_picker_back).setOnClickListener(view -> finish());
        findViewById(R.id.community_picker_search_mode).setOnClickListener(view -> showMode(false));
        findViewById(R.id.community_picker_pin_mode).setOnClickListener(view -> showMode(true));
        confirm.setOnClickListener(view -> {
            if (location == null) return;
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_LOCATION, location.toBundle()));
            finish();
        });
        showSearchResults("");
        showSelection();
        showMode(pinMode);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(PIN_MODE, pinMode);
        if (location != null) state.putBundle(EXTRA_LOCATION, location.toBundle());
        if (mapView != null) {
            CameraState camera = mapView.getMapboxMap().getCameraState();
            state.putDoubleArray(MAP_CAMERA, new double[] {camera.getCenter().longitude(),
                    camera.getCenter().latitude(), camera.getZoom(), camera.getBearing(),
                    camera.getPitch()});
        }
    }

    private void showSearchResults(String text) {
        List<MapPlace> matches = new ArrayList<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            if (place.matches(text)) matches.add(place);
        }
        if (!text.isEmpty()) matches.sort(Comparator.comparingInt(place -> place.searchRank(text)));
        resultsAdapter.setPlaces(matches, false);
        findViewById(R.id.community_picker_empty)
                .setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showMode(boolean showMap) {
        pinMode = showMap;
        findViewById(R.id.community_picker_search_panel).setVisibility(showMap ? View.GONE : View.VISIBLE);
        findViewById(R.id.community_picker_pin_panel).setVisibility(showMap ? View.VISIBLE : View.GONE);
        styleMode(findViewById(R.id.community_picker_search_mode), !showMap);
        styleMode(findViewById(R.id.community_picker_pin_mode), showMap);
        if (showMap) {
            hideKeyboard();
            if (mapView == null) setupMap();
        }
    }

    private void styleMode(TextView button, boolean selected) {
        button.setSelected(selected);
        button.setTextColor(getColor(selected ? R.color.highlight_selected : R.color.text_secondary));
        button.setBackgroundResource(R.drawable.bg_community_category);
    }

    private void hideKeyboard() {
        WindowCompat.getInsetsController(getWindow(), query).hide(WindowInsetsCompat.Type.ime());
        query.clearFocus();
        root.requestFocus();
    }

    private void setupMap() {
        String token = getString(R.string.mapbox_access_token).trim();
        usingLocalStyle = token.isEmpty() || token.equals("pk.placeholder");
        if (usingLocalStyle) {
            MapboxOptions.setAccessToken("pk.placeholder");
        }
        ViewStub stub = findViewById(R.id.community_picker_map_stub);
        mapView = (MapView) stub.inflate();
        MapboxMap map = mapView.getMapboxMap();
        map.setBounds(new CameraBoundsOptions.Builder()
                .bounds(new CoordinateBounds(
                        Point.fromLngLat(CampusMapData.WEST, CampusMapData.SOUTH),
                        Point.fromLngLat(CampusMapData.EAST, CampusMapData.NORTH)))
                .minZoom(CampusMapData.MIN_ZOOM)
                .maxZoom(20.0)
                .build());
        CameraOptions.Builder camera = new CameraOptions.Builder();
        if (restoredCamera != null) {
            camera.center(Point.fromLngLat(restoredCamera[0], restoredCamera[1]))
                    .zoom(restoredCamera[2]).bearing(restoredCamera[3]).pitch(restoredCamera[4]);
            restoredCamera = null;
        } else if (location != null) {
            camera.center(Point.fromLngLat(location.longitude, location.latitude)).zoom(17.5);
        } else {
            camera.center(Point.fromLngLat(CampusMapData.CENTER_LNG, CampusMapData.CENTER_LAT))
                    .zoom(CampusMapData.OVERVIEW_ZOOM);
        }
        map.setCamera(camera.build());
        map.subscribeMapLoadingError(error -> {
            // A missing tile or label should not replace an otherwise usable basemap.
            if (error.getType() != MapLoadingErrorType.STYLE || isFinishing() || isDestroyed()) return;
            if (!usingLocalStyle) {
                usingLocalStyle = true;
                map.loadStyle(LOCAL_MAP_STYLE, this::showCampus);
            } else if (pinMode) {
                Snackbar.make(root, R.string.community_location_map_unavailable,
                        Snackbar.LENGTH_LONG).show();
            }
        });
        if (usingLocalStyle) {
            map.getStyle(this::showCampus);
        } else {
            // Keep the bundled style as the initial view and load the main Map's basemap over it.
            map.loadStyle(Style.DARK, this::showCampus);
        }
    }

    private void showCampus(Style style) {
        if (isFinishing() || isDestroyed()) return;
        List<Point> outline = new ArrayList<>();
        for (double[] coordinates : CampusMapData.getCampusOutline()) {
            outline.add(Point.fromLngLat(coordinates[1], coordinates[0]));
        }
        outline.add(outline.get(0));
        String outlineSource = "picker-campus-outline";
        new GeoJsonSource.Builder(outlineSource)
                .geometry(Polygon.fromLngLats(Collections.singletonList(outline))).build().bindTo(style);
        FillLayer campus = new FillLayer("picker-campus-fill", outlineSource);
        campus.fillColor(getColor(R.color.surface_elevated));
        campus.fillOpacity(usingLocalStyle ? 1.0 : 0.08);
        campus.bindTo(style);
        LineLayer border = new LineLayer("picker-campus-border", outlineSource);
        border.lineColor(getColor(R.color.accent_teal));
        border.lineWidth(1.5);
        border.bindTo(style);

        PointAnnotationManager places = PointAnnotationManagerKt.createPointAnnotationManager(
                AnnotationsUtils.getAnnotations(mapView), null);
        places.setIconAllowOverlap(true);
        places.setTextOptional(true);
        Map<MapPlace.Category, Bitmap> icons = new EnumMap<>(MapPlace.Category.class);
        Map<String, MapPlace> placeByMarker = new HashMap<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            if (!place.hasMapMarker()) continue;
            Bitmap icon = icons.computeIfAbsent(place.getCategory(),
                    category -> CampusMarkerIcons.create(this, category, false));
            PointAnnotationOptions options = new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(place.getLongitude(), place.getLatitude()))
                    .withIconImage(icon);
            if (!usingLocalStyle) {
                options.withTextField(place.getShortLabel())
                        .withTextSize(12.0)
                        .withTextColor(getColor(R.color.text_primary))
                        .withTextHaloColor(getColor(R.color.bg_primary))
                        .withTextHaloWidth(1.5)
                        .withTextAnchor(TextAnchor.TOP)
                        .withTextOffset(Arrays.asList(0.0,
                                CampusMarkerIcons.sizeDp(place.getCategory(), false) / 24.0 + 0.15));
            }
            PointAnnotation marker = places.create(options);
            placeByMarker.put(marker.getId(), place);
        }
        places.addClickListener(marker -> {
            MapPlace place = placeByMarker.get(marker.getId());
            if (place == null) return false;
            choose(PostLocation.fromPlace(place));
            return true;
        });
        selectionMarkers = PointAnnotationManagerKt.createPointAnnotationManager(
                AnnotationsUtils.getAnnotations(mapView), null);
        selectionMarkers.setIconAllowOverlap(true);
        selectionMarkers.addClickListener(marker -> true);
        GesturesUtils.getGestures(mapView).addOnMapClickListener(point -> {
            if (!CampusMapData.isOnCampus(point.latitude(), point.longitude())) {
                Snackbar.make(root, R.string.community_location_outside, Snackbar.LENGTH_SHORT).show();
            } else {
                choose(PostLocation.pin(this, point.latitude(), point.longitude()));
            }
            return true;
        });
        drawPin();
    }

    private void choose(PostLocation chosen) {
        location = chosen;
        showSelection();
        drawPin();
    }

    private void showSelection() {
        selection.setVisibility(location == null ? View.GONE : View.VISIBLE);
        selection.setText(location == null ? null : location.label(this));
        confirm.setEnabled(location != null);
        confirm.setAlpha(location == null ? 0.5f : 1f);
    }

    private void drawPin() {
        if (selectionMarkers == null || location == null) return;
        if (pinIcon == null) {
            Drawable pin = AppCompatResources.getDrawable(this, R.drawable.ic_community_location);
            if (pin == null) return;
            int size = Math.round(36 * getResources().getDisplayMetrics().density);
            pinIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            pin.setTint(getColor(R.color.highlight_selected));
            pin.setBounds(0, 0, size, size);
            pin.draw(new Canvas(pinIcon));
        }
        selectionMarkers.deleteAll();
        selectionMarkers.create(new PointAnnotationOptions()
                .withPoint(Point.fromLngLat(location.longitude, location.latitude))
                .withIconAnchor(IconAnchor.BOTTOM)
                .withIconImage(pinIcon));
    }
}
