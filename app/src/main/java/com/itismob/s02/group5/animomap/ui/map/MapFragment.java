package com.itismob.s02.group5.animomap.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Toast;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itismob.s02.group5.animomap.R;
import com.mapbox.bindgen.DataRef;
import com.mapbox.bindgen.Value;
import com.mapbox.common.MapboxOptions;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraBoundsOptions;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.Image;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxDelicateApi;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.StyleObjectInfo;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.RasterLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.extension.style.sources.generated.ImageSource;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationsUtils;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Campus map screen: a Mapbox map with place markers, search, filters, and a demo route. */
public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    private static final double FOCUS_ZOOM = 17.5;
    private static final long CAMERA_MS = 600L;
    private static final double MASK_OPACITY = 0.8;
    private static final double MASK_FADE_METERS = 30.0;
    private static final String FADE_SOURCE = "campus-fade-source";
    private static final String PREFS = "animomap_map";
    private static final String PREF_RECENT = "recent_places";
    // Buildings are numerous, so their badges are smaller than the other categories'.

    private View root, header, controls, card;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CampusMask.Request fadeRequest;
    private PointAnnotationManager markers;
    private PolylineAnnotationManager routeLine;
    private final Map<String, MapPlace> markerPlaces = new HashMap<>();
    private final Map<String, Bitmap> markerIcons = new HashMap<>();
    private EdgeInsets padding = new EdgeInsets(0, 0, 0, 0);
    private EditText search;
    private ImageButton clear, closeSearchButton;
    private LinearLayout searchPanel;
    private TextView searchPrompt, searchHeading, searchEmpty;
    private MapSearchAdapter searchAdapter;
    private RecentPlaces recentPlaces = new RecentPlaces();
    private boolean suppressSearchChange;
    private OnBackPressedCallback searchBackCallback;
    private TextView title, subtitle, categoryText, hotspotNote;
    private MaterialButton directions, getDirections;
    private PlaceDetailsController details;
    private ChipGroup filters, searchFilters;
    private MapPlace selected;
    private MapPlace.Category category;
    private boolean demo, updatingUi, focusRequested;
    private int lastMaxCardHeight;
    private boolean gpsRequested, gpsUpdatesRunning, centerOnNextFix;
    private Bitmap userPinIcon;
    private final OnIndicatorPositionChangedListener positionListener = this::onGpsPosition;
    private final ActivityResultLauncher<String[]> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (!isAdded() || getView() == null) { return; }
                if (hasLocationPermission()) {
                    startGps();
                } else if (!isHidden()) {
                    Toast.makeText(requireContext(), R.string.map_location_denied, Toast.LENGTH_LONG).show();
                    showLocationPicker();
                }
            });

    private double[] cameraToRestore;
    private OnBackPressedCallback backCallback;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private ViewTreeObserver observedTree;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        String token = getString(R.string.mapbox_access_token).trim();
        if (token.isEmpty()) {
            MapboxOptions.setAccessToken("pk.placeholder");
        }
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        root = view;
        header = view.findViewById(R.id.map_header);
        controls = view.findViewById(R.id.map_controls);
        card = view.findViewById(R.id.card_map_place);
        mapView = view.findViewById(R.id.mapbox_map);
        search = view.findViewById(R.id.et_map_search);
        searchPrompt = view.findViewById(R.id.tv_map_search_prompt);
        clear = view.findViewById(R.id.btn_clear_map_search);
        closeSearchButton = view.findViewById(R.id.btn_close_map_search);
        searchPanel = view.findViewById(R.id.map_search_panel);
        searchHeading = view.findViewById(R.id.tv_map_search_results_heading);
        searchEmpty = view.findViewById(R.id.tv_map_search_empty);
        title = view.findViewById(R.id.tv_map_place_name);
        ViewCompat.setAccessibilityHeading(title, true);
        subtitle = view.findViewById(R.id.tv_map_place_subtitle);
        categoryText = view.findViewById(R.id.tv_map_place_category);
        hotspotNote = view.findViewById(R.id.tv_map_place_hotspot_note);
        directions = view.findViewById(R.id.btn_map_directions);
        getDirections = view.findViewById(R.id.btn_map_get_directions);
        details = new PlaceDetailsController(view);
        details.setOnContentChangedListener(this::resetCardHeight);
        filters = view.findViewById(R.id.map_filter_group);
        if (state != null) {
            String savedCategory = state.getString("map.category");
            category = savedCategory == null ? null : MapPlace.Category.valueOf(savedCategory);
            selected = CampusMapData.findPlace(state.getString("map.selected"));
            demo = state.getBoolean("map.demo") && selected != null
                    && CampusMapData.hasDemoRoute(selected.getId());
            cameraToRestore = state.getDoubleArray("map.camera");
            gpsRequested = state.getBoolean("map.gps");
            centerOnNextFix = state.getBoolean("map.center_on_next_fix");
        }
        setupMap();
        setupSearch(view);
        if (state != null && state.getBoolean("map.search_open")) {
            searchPanel.setVisibility(View.VISIBLE);
        }
        updatingUi = true;
        filters.check(filterId());
        updatingUi = false;
        filters.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingUi || ids.isEmpty()) { return; }
            int id = ids.get(0);
            category = id == R.id.chip_buildings ? MapPlace.Category.BUILDING
                    : id == R.id.chip_food ? MapPlace.Category.FOOD
                    : id == R.id.chip_restrooms ? MapPlace.Category.RESTROOM
                    : id == R.id.chip_study ? MapPlace.Category.STUDY
                    : id == R.id.chip_hangout ? MapPlace.Category.HANGOUT : null;
            syncSearchChips();
            clearSelection();
            showOverview();
        });
        view.findViewById(R.id.btn_map_zoom_in).setOnClickListener(button -> zoomBy(1));
        view.findViewById(R.id.btn_map_zoom_out).setOnClickListener(button -> zoomBy(-1));
        view.findViewById(R.id.btn_map_overview).setOnClickListener(button -> showOverview());
        View myLocation = view.findViewById(R.id.btn_map_my_location);
        myLocation.setOnClickListener(button -> onMyLocationClicked());
        myLocation.setOnLongClickListener(button -> { showLocationPicker(); return true; });
        view.findViewById(R.id.btn_close_map_place).setOnClickListener(button -> clearSelection());
        directions.setOnClickListener(button -> {
            demo = !demo;
            render();
            focusRequested = true;
        });
        backCallback = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() {
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
                if (insets != null && insets.isVisible(WindowInsetsCompat.Type.ime())) {
                    hideKeyboard();
                } else if (demo) {
                    demo = false;
                    render();
                    focusRequested = true;
                } else { clearSelection(); }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backCallback);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), searchBackCallback);
        layoutListener = () -> {
            if (!isAdded() || getView() != root) { return; }
            observedTree = root.getViewTreeObserver();
            if (!updateViewport()) { return; }
            if (focusRequested && selected != null) {
                focusRequested = false;
                if (demo) { focusOnRoute(); } else { focusOnPlace(selected); }
            }
        };
        root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        render();
    }

    @Override public void onResume() {
        super.onResume();
        syncGpsUpdates();
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (getView() == null) { return; }
        updateBackCallbacks();
        syncGpsUpdates();
        if (hidden && search.hasFocus()) { hideKeyboard(); }
    }

    // ---- Mapbox ----

    private void setupMap() {
        String token = getString(R.string.mapbox_access_token).trim();
        if (token.isEmpty() || token.equals("pk.placeholder")) {
            root.findViewById(R.id.tv_map_token_missing).setVisibility(View.VISIBLE);
        }
        mapboxMap = mapView.getMapboxMap();
        // Keep the camera on campus.
        mapboxMap.setBounds(new CameraBoundsOptions.Builder()
                .bounds(new CoordinateBounds(
                        Point.fromLngLat(CampusMapData.WEST, CampusMapData.SOUTH),
                        Point.fromLngLat(CampusMapData.EAST, CampusMapData.NORTH)))
                .minZoom(CampusMapData.MIN_ZOOM)
                .maxZoom(20.0)
                .build());
        if (cameraToRestore != null && cameraToRestore.length == 5) {
            mapboxMap.setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(cameraToRestore[1], cameraToRestore[0]))
                    .zoom(cameraToRestore[2]).bearing(cameraToRestore[3]).pitch(cameraToRestore[4])
                    .build());
            cameraToRestore = null;
        } else {
            mapboxMap.setCamera(overviewCamera());
        }
        // The dark style matches the app's dark design system.
        View requestedView = root;
        MapboxMap requestedMap = mapboxMap;
        requestedMap.loadStyle(Style.DARK, style -> {
            if (getView() != requestedView || mapboxMap != requestedMap || !style.isValid()) return;
            hideBasemapPlaceNames(style);
            addOutsideCampusMask(style, requestedView, requestedMap);
            AnnotationConfig noConfig = null;
            markers = PointAnnotationManagerKt.createPointAnnotationManager(
                    AnnotationsUtils.getAnnotations(mapView), noConfig);
            routeLine = PolylineAnnotationManagerKt.createPolylineAnnotationManager(
                    AnnotationsUtils.getAnnotations(mapView), noConfig);
            markers.setIconAllowOverlap(true);
            markers.setTextOptional(true);
            markers.addClickListener(annotation -> {
                MapPlace place = markerPlaces.get(annotation.getId());
                if (place != null) { select(place, false); }
                return true;
            });
            renderMap();
        });
    }
    /**
     * The app draws its own markers, so hide the basemap's building, shop and landmark names
     * and keep only street names. Layers are matched by id: every "...label" layer is hidden
     * except the "road" ones.
     */
    private void hideBasemapPlaceNames(Style style) {
        for (StyleObjectInfo layer : style.getStyleLayers()) {
            String id = layer.getId();
            if (id.contains("label") && !id.startsWith("road")) {
                style.setStyleLayerProperty(id, "visibility", Value.valueOf("none"));
            }
        }
    }
    private void addOutsideCampusMask(Style style, View requestedView, MapboxMap requestedMap) {
        int color = ContextCompat.getColor(requireContext(), R.color.bg_primary);
        double w = CampusMapData.WEST, e = CampusMapData.EAST;
        double s = CampusMapData.SOUTH, n = CampusMapData.NORTH;

        List<Point> world = Arrays.asList(
                Point.fromLngLat(-180.0, -85.0), Point.fromLngLat(180.0, -85.0),
                Point.fromLngLat(180.0, 85.0), Point.fromLngLat(-180.0, 85.0),
                Point.fromLngLat(-180.0, -85.0));
        List<Point> bounds = Arrays.asList(
                Point.fromLngLat(w, s), Point.fromLngLat(w, n), Point.fromLngLat(e, n),
                Point.fromLngLat(e, s), Point.fromLngLat(w, s));
        new GeoJsonSource.Builder("campus-mask-source")
                .geometry(Polygon.fromLngLats(Arrays.asList(world, bounds))).build().bindTo(style);
        FillLayer outside = new FillLayer("campus-mask-layer", "campus-mask-source");
        outside.fillColor(color);
        outside.fillOpacity(MASK_OPACITY);
        outside.bindTo(style);

        new ImageSource.Builder(FADE_SOURCE)
                .coordinates(Arrays.asList(
                        Arrays.asList(w, n), Arrays.asList(e, n),
                        Arrays.asList(e, s), Arrays.asList(w, s)))
                .build().bindTo(style);
        // Reserve the same layer position below the markers while the image is prepared.
        RasterLayer layer = new RasterLayer("campus-fade-layer", FADE_SOURCE);
        layer.rasterFadeDuration(0.0);
        layer.bindTo(style);
        cancelFadeRequest();
        fadeRequest = CampusMask.request(color, (int) Math.round(MASK_OPACITY * 255),
                MASK_FADE_METERS, 2.0, fade -> {
                    if (getView() != requestedView || mapboxMap != requestedMap) return;
                    try {
                        if (!style.isValid() || requestedMap.getStyle() != style
                                || !style.styleSourceExists(FADE_SOURCE)) return;
                        String error = style.updateStyleImageSourceImage(FADE_SOURCE,
                                new Image(fade.width, fade.height,
                                        new DataRef(fade.pixelsForUpload()))).getError();
                        if (error == null) {
                            Log.d(TAG, "Campus fade uploaded");
                        } else {
                            Log.w(TAG, "Could not upload the campus fade: " + error);
                        }
                    } catch (RuntimeException | OutOfMemoryError error) {
                        Log.w(TAG, "Could not upload the campus fade", error);
                    }
                });
    }

    private void cancelFadeRequest() {
        if (fadeRequest != null) {
            fadeRequest.cancel();
            fadeRequest = null;
        }
    }
    private CameraOptions overviewCamera() {
        return new CameraOptions.Builder()
                .center(Point.fromLngLat(CampusMapData.CENTER_LNG, CampusMapData.CENTER_LAT))
                .zoom(CampusMapData.OVERVIEW_ZOOM).bearing(0.0).pitch(0.0)
                .build();
    }

    /** Round badge with a symbol per category, so places can be told apart without relying on colour. */
    private Bitmap markerIcon(MapPlace.Category value, boolean isSelected, boolean isHotspot) {
        String key = value.name() + isSelected + isHotspot;
        return markerIcons.computeIfAbsent(key,
                unused -> CampusMarkerIcons.create(requireContext(), value, isSelected, isHotspot));
    }
    private int markerSizeDp(MapPlace.Category value, boolean isSelected) {
        return CampusMarkerIcons.sizeDp(value, isSelected);
    }

    private void renderMap() {
        if (markers == null || routeLine == null) { return; }
        markers.deleteAll();
        routeLine.deleteAll();
        markerPlaces.clear();
        if (demo) {
            List<Point> points = new ArrayList<>();
            for (double[] p : CampusMapData.getDemoRoutePoints()) { points.add(Point.fromLngLat(p[1], p[0])); }
            routeLine.create(new PolylineAnnotationOptions().withPoints(points)
                    .withLineColor(ContextCompat.getColor(requireContext(), R.color.accent_teal))
                    .withLineWidth(6.0));
        }
        List<MapPlace> shown = new ArrayList<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            if (CampusMapData.isShownOnMap(place, selected, category, demo)) { shown.add(place); }
        }
        int labelColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int haloColor = ContextCompat.getColor(requireContext(), R.color.bg_primary);
        List<PointAnnotationOptions> options = new ArrayList<>();
        for (MapPlace place : shown) {
            boolean isSelected = place == selected;
            options.add(new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(place.getLongitude(), place.getLatitude()))
                    .withIconImage(markerIcon(place.getCategory(), isSelected, place.isHotspot()))
                    // Hotspots carry a "Hotspot" label so they stand out from ordinary markers.
                    .withTextField(place.isHotspot()
                            ? getString(R.string.place_hotspot_marker_label, place.getShortLabel())
                            : place.getShortLabel())
                    .withTextSize(12.0)
                    .withTextColor(labelColor)
                    .withTextHaloColor(haloColor)
                    .withTextHaloWidth(1.5)
                    .withTextAnchor(TextAnchor.TOP)
                    .withTextOffset(Arrays.asList(0.0,
                            markerSizeDp(place.getCategory(), isSelected) / 2.0 / 12.0 + 0.15))
                    // Lower keys are placed first, so the selected label always wins collisions.
                    .withSymbolSortKey(isSelected ? 0.0 : 1.0));
        }
        List<PointAnnotation> created = markers.create(options);
        for (int i = 0; i < created.size(); i++) {
            markerPlaces.put(created.get(i).getId(), shown.get(i));
        }
        // Manually chosen "I am here" pin, drawn just above the building's marker.
        MapPlace manual = UserLocation.getManualPlace();
        if (manual != null) {
            markers.create(new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(manual.getLongitude(), manual.getLatitude()))
                    .withIconImage(userPin())
                    .withIconAnchor(IconAnchor.BOTTOM)
                    .withIconOffset(Arrays.asList(0.0, -markerSizeDp(manual.getCategory(), manual == selected) / 2.0))
                    .withSymbolSortKey(-1.0));
        }
    }
    private void moveCamera(CameraOptions camera) {
        MapAnimationOptions options = new MapAnimationOptions.Builder().duration(CAMERA_MS).build();
        CameraAnimationsUtils.getCamera(mapView).easeTo(camera, options, null);
    }
    private void showOverview() {
        moveCamera(new CameraOptions.Builder().center(
                Point.fromLngLat(CampusMapData.CENTER_LNG, CampusMapData.CENTER_LAT))
                .zoom(CampusMapData.OVERVIEW_ZOOM).bearing(0.0).pitch(0.0).build());
    }
    private void zoomBy(double levels) {
        moveCamera(new CameraOptions.Builder()
                .zoom(mapboxMap.getCameraState().getZoom() + levels).build());
    }
    private void focusOnPlace(MapPlace place) {
        moveCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(place.getLongitude(), place.getLatitude()))
                .zoom(Math.max(mapboxMap.getCameraState().getZoom(), FOCUS_ZOOM))
                .build());
    }

    @OptIn(markerClass = MapboxDelicateApi.class)
    private void focusOnRoute() {
        List<Point> points = new ArrayList<>();
        for (double[] p : CampusMapData.getDemoRoutePoints()) { points.add(Point.fromLngLat(p[1], p[0])); }
        MapPlace origin = CampusMapData.findPlace(CampusMapData.LASALLE);
        if (origin != null) { points.add(Point.fromLngLat(origin.getLongitude(), origin.getLatitude())); }
        points.add(Point.fromLngLat(selected.getLongitude(), selected.getLatitude()));
        EdgeInsets fit = new EdgeInsets(padding.getTop() + dp(24), padding.getLeft() + dp(24),
                padding.getBottom() + dp(24), padding.getRight() + dp(24));
        CameraOptions camera = mapboxMap.cameraForCoordinates(points, new CameraOptions.Builder().build(),
                fit, 19.0, null);
        moveCamera(camera);
    }

    // ---- User location: GPS when permitted, otherwise a manually chosen building ----

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void onMyLocationClicked() {
        if (hasLocationPermission()) {
            startGps();
        } else {
            locationPermission.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION });
        }
    }
    private void startGps() {
        UserLocation.clear();
        renderMap();
        gpsRequested = true;
        centerOnNextFix = true;
        syncGpsUpdates();
    }
    private void stopGps() {
        gpsRequested = false;
        centerOnNextFix = false;
        setGpsUpdatesRunning(false);
    }
    private void syncGpsUpdates() {
        setGpsUpdatesRunning(gpsRequested && !isHidden() && hasLocationPermission());
    }
    private void setGpsUpdatesRunning(boolean running) {
        if (mapView == null || gpsUpdatesRunning == running) { return; }
        // Hiding the Map pauses updates without discarding the user's location preference.
        gpsUpdatesRunning = running;
        LocationComponentPlugin location = LocationComponentUtils.getLocationComponent(mapView);
        if (running) {
            location.setLocationPuck(LocationComponentUtils.createDefault2DPuck(true));
            // The "you are here" pulse is teal, not the default blue.
            location.setPulsingColor(ContextCompat.getColor(requireContext(), R.color.accent_teal));
            location.setEnabled(true);
            location.addOnIndicatorPositionChangedListener(positionListener);
        } else {
            location.removeOnIndicatorPositionChangedListener(positionListener);
            location.setEnabled(false);
        }
    }
    private void onGpsPosition(Point point) {
        if (!gpsUpdatesRunning || !centerOnNextFix || isHidden() || getView() == null) { return; }
        centerOnNextFix = false;
        if (!CampusMapData.isOnCampus(point.latitude(), point.longitude())) {
            // The map is limited to campus, so an off-campus fix cannot be shown.
            stopGps();
            Toast.makeText(requireContext(), R.string.map_location_off_campus, Toast.LENGTH_LONG).show();
            showLocationPicker();
            return;
        }
        moveCamera(new CameraOptions.Builder().center(point)
                .zoom(Math.max(mapboxMap.getCameraState().getZoom(), FOCUS_ZOOM)).build());
    }
    private void showLocationPicker() {
        List<MapPlace> buildings = new ArrayList<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            if (place.getCategory() == MapPlace.Category.BUILDING) { buildings.add(place); }
        }
        String[] names = new String[buildings.size()];
        for (int i = 0; i < names.length; i++) { names[i] = buildings.get(i).getName(); }
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.map_location_picker_title)
                .setItems(names, (d, which) -> setManualLocation(buildings.get(which)))
                .setNegativeButton(android.R.string.cancel, null);
        if (UserLocation.getManualPlaceId() != null) {
            dialog.setNeutralButton(R.string.map_location_clear, (d, which) -> {
                UserLocation.clear();
                renderMap();
            });
        }
        dialog.show();
    }
    private void setManualLocation(MapPlace building) {
        stopGps();
        UserLocation.setManualPlaceId(building.getId());
        renderMap();
        focusOnPlace(building);
        Toast.makeText(requireContext(),
                getString(R.string.map_location_set, building.getName()), Toast.LENGTH_SHORT).show();
    }
    private Bitmap userPin() {
        if (userPinIcon != null) { return userPinIcon; }
        int w = dp(26), h = dp(34);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float r = w / 2f;
        // White pin with a teal dot: clearly "you", and different from the selected-place ring.
        paint.setColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        Path drop = new Path();
        drop.addCircle(r, r, r, Path.Direction.CW);
        drop.moveTo(r * 0.25f, r * 1.6f);
        drop.lineTo(r, h);
        drop.lineTo(r * 1.75f, r * 1.6f);
        drop.close();
        canvas.drawPath(drop, paint);
        paint.setColor(ContextCompat.getColor(requireContext(), R.color.primary_teal));
        canvas.drawCircle(r, r, r * 0.45f, paint);
        userPinIcon = bitmap;
        return bitmap;
    }

    // ---- Search, selection, and the place card ----

    private void setupSearch(View view) {
        setupSearchFilters(view);
        RecyclerView results = view.findViewById(R.id.rv_map_search_results);
        searchAdapter = new MapSearchAdapter(place -> select(place, true));
        results.setLayoutManager(new LinearLayoutManager(requireContext()));
        results.setAdapter(searchAdapter);
        loadRecentPlaces();
        updateSearchResults("");
        view.findViewById(R.id.map_search_card).setOnClickListener(v -> openSearch());
        searchPrompt.setOnClickListener(v -> openSearch());
        view.findViewById(R.id.btn_map_search_set_location).setOnClickListener(v -> {
            closeSearch();
            showLocationPicker();
        });
        closeSearchButton.setOnClickListener(v -> closeSearch());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!suppressSearchChange) { updateSearchResults(s.toString().trim()); }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        search.setOnEditorActionListener((textView, action, event) -> {
            hideKeyboard();
            return true;
        });
        clear.setOnClickListener(button -> clearSelection());
        searchBackCallback = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { closeSearch(); }
        };
    }
    /** Category chips inside the search panel. They share the map's filter, so both stay in sync. */
    private void setupSearchFilters(View view) {
        searchFilters = view.findViewById(R.id.map_search_filter_group);
        addSearchChip(R.string.map_filter_all, null);
        addSearchChip(R.string.map_filter_buildings, MapPlace.Category.BUILDING);
        addSearchChip(R.string.map_filter_food, MapPlace.Category.FOOD);
        addSearchChip(R.string.map_filter_restrooms, MapPlace.Category.RESTROOM);
        addSearchChip(R.string.map_filter_study, MapPlace.Category.STUDY);
        addSearchChip(R.string.map_filter_hangout, MapPlace.Category.HANGOUT);
        syncSearchChips();
        searchFilters.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingUi || ids.isEmpty()) { return; }
            Chip chip = group.findViewById(ids.get(0));
            category = (MapPlace.Category) chip.getTag();
            updatingUi = true;
            filters.check(filterId());
            updatingUi = false;
            updateSearchResults(search.getText().toString().trim());
            renderMap();
        });
    }
    private void addSearchChip(int labelRes, MapPlace.Category chipCategory) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_map_filter, searchFilters, false);
        chip.setId(View.generateViewId());
        chip.setText(labelRes);
        chip.setTag(chipCategory);
        searchFilters.addView(chip);
    }
    /** Checks the search chip that matches the current map filter. */
    private void syncSearchChips() {
        boolean wasUpdating = updatingUi;
        updatingUi = true;
        for (int i = 0; i < searchFilters.getChildCount(); i++) {
            Chip chip = (Chip) searchFilters.getChildAt(i);
            if (chip.getTag() == category) { searchFilters.check(chip.getId()); break; }
        }
        updatingUi = wasUpdating;
    }
    /** Loads the saved recent places. Starts empty on a fresh install. */
    private void loadRecentPlaces() {
        recentPlaces = RecentPlaces.decode(requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_RECENT, ""));
    }
    private void openSearch() {
        syncSearchChips();
        searchPanel.setVisibility(View.VISIBLE);
        updateBackCallbacks();
        suppressSearchChange = true;
        search.setText("");
        suppressSearchChange = false;
        updateSearchResults("");
        search.requestFocus();
        search.postDelayed(() -> {
            if (!isAdded() || isHidden() || getView() != root
                    || searchPanel.getVisibility() != View.VISIBLE) { return; }
            WindowCompat.getInsetsController(requireActivity().getWindow(), search)
                    .show(WindowInsetsCompat.Type.ime());
        }, 160);
    }
    private void closeSearch() {
        hideKeyboard();
        searchPanel.setVisibility(View.GONE);
        updateBackCallbacks();
        suppressSearchChange = true;
        search.setText("");
        suppressSearchChange = false;
    }
    private void updateBackCallbacks() {
        boolean mapVisible = !isHidden();
        backCallback.setEnabled(mapVisible && selected != null);
        searchBackCallback.setEnabled(mapVisible && searchPanel.getVisibility() == View.VISIBLE);
    }
    private void updateSearchResults(String query) {
        boolean recents = query.isEmpty();
        List<MapPlace> matches = new ArrayList<>();
        if (recents) {
            matches.addAll(recentPlaces.getPlaces());
        } else {
            for (MapPlace place : CampusMapData.getPlaces()) {
                if (place.matches(query)) { matches.add(place); }
            }
        }
        if (category != null) {
            for (int i = matches.size() - 1; i >= 0; i--) {
                if (matches.get(i).getCategory() != category) { matches.remove(i); }
            }
        }
        if (!recents) {
            // Best matches first; places that rank the same keep their list order.
            matches.sort(Comparator.comparingInt(place -> place.searchRank(query)));
        }
        searchHeading.setText(recents ? R.string.map_search_recent_heading
                : R.string.map_search_results_heading);
        searchAdapter.setPlaces(matches, recents);
        searchEmpty.setText(recents ? R.string.map_search_no_recent : R.string.map_search_empty);
        searchEmpty.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }
    private void addRecentPlace(MapPlace place) {
        recentPlaces.add(place);
        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(PREF_RECENT, recentPlaces.encode()).apply();
    }
    private void select(MapPlace place, boolean fromSearch) {
        selected = place;
        demo = false;
        updatingUi = true;
        if (fromSearch) {
            // Rooms and indoor places are shown alone, so no category chip applies to them.
            category = place.hasMapMarker() ? place.getCategory() : null;
            filters.check(filterId());
        }
        updatingUi = false;
        addRecentPlace(place);
        closeSearch();
        render();
        focusRequested = true;
        root.requestLayout();
        ViewCompat.setAccessibilityPaneTitle(card, title.getText());
    }
    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) { keyboard.hideSoftInputFromWindow(search.getWindowToken(), 0); }
        search.clearFocus();
        root.requestFocus();
    }
    private void clearSelection() {
        selected = null;
        demo = false;
        focusRequested = false;
        hideKeyboard();
        render();
    }
    private int filterId() {
        return category == MapPlace.Category.BUILDING ? R.id.chip_buildings
                : category == MapPlace.Category.FOOD ? R.id.chip_food
                : category == MapPlace.Category.RESTROOM ? R.id.chip_restrooms
                : category == MapPlace.Category.STUDY ? R.id.chip_study
                : category == MapPlace.Category.HANGOUT ? R.id.chip_hangout : R.id.chip_all;
    }
    private void render() {
        renderMap();
        clear.setVisibility(selected == null ? View.GONE : View.VISIBLE);
        searchPrompt.setText(selected == null ? getString(R.string.map_search_landing_label)
                : selected.getName());
        card.setVisibility(selected == null ? View.GONE : View.VISIBLE);
        // Filters only matter while browsing; hiding them gives a selected place more map.
        root.findViewById(R.id.map_filter_scroll).setVisibility(selected == null ? View.VISIBLE : View.GONE);
        updateBackCallbacks();
        if (selected != null) {
            if (demo) {
                categoryText.setText(R.string.map_demo_notice);
            } else if (selected.isHotspot()) {
                categoryText.setText(getString(R.string.place_hotspot_category,
                        getString(MapPlace.categoryLabelRes(selected.getCategory()))));
            } else {
                categoryText.setText(MapPlace.categoryLabelRes(selected.getCategory()));
            }
            title.setText(demo ? getString(R.string.map_demo_title) : selected.getName());
            String locationLine = selected.getLocationLine();
            subtitle.setText(demo ? getString(R.string.map_demo_subtitle) : locationLine);
            subtitle.setVisibility(!demo && "Building".equals(locationLine) ? View.GONE : View.VISIBLE);
            boolean hasRoute = CampusMapData.hasDemoRoute(selected.getId());
            hotspotNote.setVisibility(selected.isHotspot() && !demo ? View.VISIBLE : View.GONE);
            directions.setVisibility(hasRoute ? View.VISIBLE : View.GONE);
            directions.setText(demo ? R.string.map_back_to_place : R.string.map_preview_demo);
            // Directions is a disabled placeholder for now: it has no action yet.
            getDirections.setVisibility(demo ? View.GONE : View.VISIBLE);
            details.bind(selected, !demo);
            resetCardHeight();
            ScrollView body = card.findViewById(R.id.map_place_scroll);
            body.scrollTo(0, 0);
        }
        root.requestLayout();
    }

    /** Lets the place card measure itself again, so it grows or shrinks with its content. */
    private void resetCardHeight() {
        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        card.setLayoutParams(params);
        root.requestLayout();
    }

    private boolean updateViewport() {
        if (root.getWidth() == 0 || root.getHeight() == 0 || header.getHeight() == 0) { return false; }
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean sideCard = selected != null && landscape;
        LinearLayout headerLayout = (LinearLayout) header;
        int orientation = landscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
        if (headerLayout.getOrientation() != orientation) {
            headerLayout.setOrientation(orientation);
            headerLayout.setPadding(dp(16), dp(landscape ? 4 : 8),
                    dp(16), dp(landscape ? 4 : 8));
            LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                    landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT, dp(52), landscape ? .55f : 0);
            LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(
                    landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, landscape ? .45f : 0);
            filterParams.topMargin = landscape ? 0 : dp(4);
            filterParams.setMarginStart(landscape ? dp(8) : 0);
            root.findViewById(R.id.map_search_card).setLayoutParams(searchParams);
            root.findViewById(R.id.map_filter_scroll).setLayoutParams(filterParams);
            return false;
        }
        LinearLayout controlLayout = (LinearLayout) controls;
        boolean compact = root.getHeight() < dp(200);
        boolean spacesChanged = false;
        for (int i = 0; i < controlLayout.getChildCount(); i++) {
            View child = controlLayout.getChildAt(i);
            if (child instanceof Space) {
                int visibility = compact ? View.GONE : View.VISIBLE;
                if (child.getVisibility() != visibility) {
                    child.setVisibility(visibility);
                    spacesChanged = true;
                }
            }
        }
        if (spacesChanged) { return false; }
        int cardWidth = sideCard ? Math.min(dp(280), (int) (root.getWidth() * .42f))
                : root.getWidth() - dp(32);
        FrameLayout.LayoutParams cardParams = (FrameLayout.LayoutParams) card.getLayoutParams();
        FrameLayout.LayoutParams headerParams = (FrameLayout.LayoutParams) header.getLayoutParams();
        int headerWidth = root.getWidth() - (sideCard ? cardWidth + dp(32) : 0)
                - (landscape ? dp(80) : 0);
        if (cardParams.width != cardWidth || headerParams.width != headerWidth) {
            cardParams.width = cardWidth;
            cardParams.topMargin = dp(sideCard ? 8 : 16);
            cardParams.bottomMargin = dp(sideCard ? 8 : 16);
            cardParams.gravity = Gravity.BOTTOM | (sideCard ? Gravity.END : Gravity.CENTER_HORIZONTAL);
            card.setLayoutParams(cardParams);
            headerParams.width = headerWidth;
            header.setLayoutParams(headerParams);
            return false;
        }
        if (selected != null && card.getHeight() == 0) { return false; }
        int maxCardHeight = sideCard ? root.getHeight() - dp(16)
                : Math.max(dp(64), Math.min((int) (root.getHeight() * .42f),
                root.getHeight() - header.getBottom() - dp(100)));
        // The available height changed (for example the keyboard closed): measure the card freshly.
        if (maxCardHeight != lastMaxCardHeight) {
            lastMaxCardHeight = maxCardHeight;
            if (cardParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                cardParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                card.setLayoutParams(cardParams);
                return false;
            }
        }
        if (selected != null && card.getHeight() > maxCardHeight) {
            cardParams.height = maxCardHeight;
            card.setLayoutParams(cardParams);
            return false;
        }
        int right = sideCard ? cardWidth + dp(16) : 0;
        int bottom = selected != null && !sideCard ? card.getHeight() + dp(32) : 0;
        // Zoom buttons sit at the end edge, beside a landscape card or above a portrait card.
        FrameLayout.LayoutParams controlParams = (FrameLayout.LayoutParams) controls.getLayoutParams();
        int controlsEnd = dp(12) + right;
        if (controlParams.getMarginEnd() != controlsEnd || controlParams.bottomMargin != bottom) {
            controlParams.setMarginEnd(controlsEnd);
            controlParams.bottomMargin = bottom;
            controls.setLayoutParams(controlParams);
        }
        EdgeInsets insets = new EdgeInsets(header.getBottom() + dp(4), dp(16), bottom, right + dp(64));
        if (!insets.equals(padding)) {
            padding = insets;
            mapboxMap.setCamera(new CameraOptions.Builder().padding(padding).build());
        }
        return true;
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("map.category", category == null ? null : category.name());
        state.putString("map.selected", selected == null ? null : selected.getId());
        state.putBoolean("map.demo", demo);
        state.putBoolean("map.gps", gpsRequested);
        state.putBoolean("map.center_on_next_fix", centerOnNextFix);
        state.putBoolean("map.search_open", searchPanel != null
                && searchPanel.getVisibility() == View.VISIBLE);
        if (mapboxMap != null) {
            CameraState camera = mapboxMap.getCameraState();
            state.putDoubleArray("map.camera", new double[] {camera.getCenter().latitude(),
                    camera.getCenter().longitude(), camera.getZoom(), camera.getBearing(),
                    camera.getPitch()});
        }
    }
    @Override public void onDestroyView() {
        cancelFadeRequest();
        if (observedTree != null && observedTree.isAlive()) {
            observedTree.removeOnGlobalLayoutListener(layoutListener);
        }
        if (root.getViewTreeObserver().isAlive()) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
        setGpsUpdatesRunning(false);
        markers = null;
        routeLine = null;
        markerIcons.clear();
        userPinIcon = null;
        markerPlaces.clear();
        super.onDestroyView();
    }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
