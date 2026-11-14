package com.itismob.s02.group5.animomap.ui.map;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.graphics.RectF;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.itismob.s02.group5.animomap.R;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
    private View root, header, controls, card;
    private ZoomableImageView mapView;
    private MapOverlayView overlay;
    private AutoCompleteTextView search;
    private ImageButton clear, zoomIn, zoomOut;
    private TextView title, subtitle, categoryText, notice;
    private MaterialButton directions;
    private ChipGroup filters;
    private MapPlace selected;
    private MapPlace.Category category;
    private boolean demo, updatingUi, focusRequested;
    private float[] cameraToRestore;
    private OnBackPressedCallback backCallback;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private ViewTreeObserver observedTree;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        root = view;
        header = view.findViewById(R.id.map_header);
        controls = view.findViewById(R.id.map_controls);
        card = view.findViewById(R.id.card_map_place);
        mapView = view.findViewById(R.id.iv_campus_map);
        overlay = view.findViewById(R.id.map_overlay);
        search = view.findViewById(R.id.et_map_search);
        clear = view.findViewById(R.id.btn_clear_map_search);
        zoomIn = view.findViewById(R.id.btn_map_zoom_in);
        zoomOut = view.findViewById(R.id.btn_map_zoom_out);
        title = view.findViewById(R.id.tv_map_place_name);
        ViewCompat.setAccessibilityHeading(title, true);
        subtitle = view.findViewById(R.id.tv_map_place_subtitle);
        categoryText = view.findViewById(R.id.tv_map_place_category);
        notice = view.findViewById(R.id.tv_map_directions_notice);
        directions = view.findViewById(R.id.btn_map_directions);
        filters = view.findViewById(R.id.map_filter_group);
        if (state != null) {
            String savedCategory = state.getString("map.category");
            category = savedCategory == null ? null : MapPlace.Category.valueOf(savedCategory);
            selected = CampusMapData.findPlace(state.getString("map.selected"));
            demo = state.getBoolean("map.demo") && selected != null
                    && CampusMapData.hasDemoRoute(selected.getId());
            cameraToRestore = state.getFloatArray("map.camera");
        }
        overlay.setMapView(mapView);
        mapView.setOnImageMatrixChangedListener(() -> {
            overlay.invalidate();
            zoomIn.setEnabled(mapView.canZoomIn());
            zoomOut.setEnabled(mapView.canZoomOut());
            zoomIn.setAlpha(mapView.canZoomIn() ? 1f : .4f);
            zoomOut.setAlpha(mapView.canZoomOut() ? 1f : .4f);
        });
        mapView.setOnMapTapListener((x, y) -> {
            MapPlace place = overlay.findPlaceAt(x, y);
            if (place != null) { select(place, false); }
        });
        setupSearch();
        updatingUi = true;
        search.setText(state == null ? "" : state.getString("map.query", ""), false);
        filters.check(filterId());
        updatingUi = false;
        filters.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingUi || ids.isEmpty()) { return; }
            int id = ids.get(0);
            category = id == R.id.chip_buildings ? MapPlace.Category.BUILDING
                    : id == R.id.chip_study ? MapPlace.Category.STUDY
                    : id == R.id.chip_food ? MapPlace.Category.FOOD : null;
            clearSelection();
            mapView.showOverview();
        });
        zoomIn.setOnClickListener(button -> mapView.zoomBy(1.5f));
        zoomOut.setOnClickListener(button -> mapView.zoomBy(1f / 1.5f));
        view.findViewById(R.id.btn_map_overview).setOnClickListener(button -> mapView.showOverview());
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
        layoutListener = () -> {
            if (!isAdded() || getView() != root) { return; }
            observedTree = root.getViewTreeObserver();
            if (!updateViewport()) { return; }
            if (cameraToRestore != null) {
                mapView.restoreCameraState(cameraToRestore);
                cameraToRestore = null;
            } else if (focusRequested && selected != null) {
                focusRequested = false;
                if (demo) {
                    RectF bounds = new RectF(selected.getMapX(), selected.getMapY(),
                            selected.getMapX(), selected.getMapY());
                    MapPlace origin = CampusMapData.findPlace(CampusMapData.LASALLE);
                    bounds.union(origin.getMapX(), origin.getMapY());
                    for (float[] p : CampusMapData.getDemoRoutePoints()) { bounds.union(p[0], p[1]); }
                    mapView.focusOnBounds(bounds);
                } else { mapView.focusOnPoint(selected.getMapX(), selected.getMapY()); }
            }
        };
        root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        render();
    }
    private void setupSearch() {
        PlaceSearchAdapter adapter = new PlaceSearchAdapter();
        search.setAdapter(adapter);
        search.setOnItemClickListener((parent, row, position, id) -> {
            MapPlace place = adapter.getItem(position);
            if (place != null) { select(place, true); }
        });
        search.setOnFocusChangeListener((v, focused) -> {
            if (focused) { adapter.getFilter().filter(search.getText(), count -> {
                if (search.hasFocus()) { search.showDropDown(); }
            }); }
        });
        search.setOnClickListener(v -> {
            if (search.getText().length() == 0) {
                adapter.getFilter().filter("", count -> search.showDropDown());
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clear.setVisibility(s.length() == 0 ? View.INVISIBLE : View.VISIBLE);
                if (!updatingUi && selected != null && !selected.getName().equals(s.toString())) {
                    selected = null;
                    demo = false;
                    focusRequested = false;
                    render();
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        clear.setOnClickListener(button -> clearSelection());
        search.setOnEditorActionListener((textView, action, event) -> {
            adapter.getFilter().filter(search.getText(), count -> search.showDropDown());
            return true;
        });
    }
    private void select(MapPlace place, boolean fromSearch) {
        selected = place;
        demo = false;
        updatingUi = true;
        if (fromSearch) { category = place.getCategory(); filters.check(filterId()); }
        search.setText(place.getName(), false);
        updatingUi = false;
        hideKeyboard();
        render();
        focusRequested = true;
        root.requestLayout();
        root.announceForAccessibility(place.getName());
    }
    private void hideKeyboard() {
        search.dismissDropDown();
        InputMethodManager keyboard = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.clearFocus();
        root.requestFocus();
    }
    private void clearSelection() {
        selected = null;
        demo = false;
        focusRequested = false;
        updatingUi = true;
        search.setText("", false);
        updatingUi = false;
        hideKeyboard();
        render();
    }
    private int filterId() {
        return category == MapPlace.Category.BUILDING ? R.id.chip_buildings
                : category == MapPlace.Category.STUDY ? R.id.chip_study
                : category == MapPlace.Category.FOOD ? R.id.chip_food : R.id.chip_all;
    }
    private void render() {
        overlay.setActiveCategory(category);
        overlay.setSelectedPlace(selected);
        overlay.setRouteVisible(demo);
        clear.setVisibility(search.length() == 0 ? View.INVISIBLE : View.VISIBLE);
        card.setVisibility(selected == null ? View.GONE : View.VISIBLE);
        backCallback.setEnabled(selected != null);
        if (selected != null) {
            categoryText.setText(demo ? R.string.map_demo_notice
                    : selected.getCategory() == MapPlace.Category.BUILDING ? R.string.place_type_building
                    : selected.getCategory() == MapPlace.Category.STUDY ? R.string.map_category_study
                    : R.string.map_category_food);
            title.setText(demo ? getString(R.string.map_demo_title) : selected.getName());
            subtitle.setText(demo ? getString(R.string.map_demo_subtitle) : selected.getSubtitle());
            subtitle.setVisibility(!demo && "Building".equals(selected.getSubtitle()) ? View.GONE : View.VISIBLE);
            boolean hasRoute = CampusMapData.hasDemoRoute(selected.getId());
            notice.setText(hasRoute ? R.string.map_demo_notice : R.string.map_unmapped);
            notice.setVisibility(demo ? View.GONE : View.VISIBLE);
            directions.setVisibility(hasRoute ? View.VISIBLE : View.GONE);
            directions.setText(demo ? R.string.map_back_to_place : R.string.map_preview_demo);
            ViewGroup.LayoutParams params = card.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            card.setLayoutParams(params);
            android.widget.ScrollView body = (android.widget.ScrollView) card.findViewById(R.id.map_place_scroll);
            body.scrollTo(0, 0);
        }
        root.requestLayout();
    }
    /** Reserve actual measured UI space, including a side card on landscape screens. */
    private boolean updateViewport() {
        if (root.getWidth() == 0 || root.getHeight() == 0 || header.getHeight() == 0) { return false; }
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean sideCard = selected != null && landscape;
        LinearLayout headerLayout = (LinearLayout) header;
        int orientation = landscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
        if (headerLayout.getOrientation() != orientation) {
            headerLayout.setOrientation(orientation);
            headerLayout.setPadding(dp(16), dp(landscape ? 4 : 16),
                    dp(16), dp(landscape ? 4 : 16));
            LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                    landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT, dp(56), landscape ? .55f : 0);
            LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(
                    landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, landscape ? .45f : 0);
            filterParams.topMargin = landscape ? 0 : dp(8);
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
            if (child instanceof android.widget.Space) {
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
        if (selected != null && card.getHeight() > maxCardHeight) {
            cardParams.height = maxCardHeight;
            card.setLayoutParams(cardParams);
            return false;
        }
        int right = dp(80) + (sideCard ? cardWidth + dp(16) : 0);
        int bottom = selected != null && !sideCard ? card.getHeight() + dp(32) : dp(landscape ? 8 : 16);
        mapView.setViewportInsets(dp(16), header.getBottom() + dp(4), right, bottom);
        RectF viewport = mapView.getMapViewport();
        FrameLayout.LayoutParams controlParams = (FrameLayout.LayoutParams) controls.getLayoutParams();
        int left = (int) viewport.right + dp(12);
        int top = Math.max(0, Math.min(root.getHeight() - controls.getHeight() - dp(8),
                (int) (viewport.centerY() - controls.getHeight() / 2f)));
        int start = ViewCompat.getLayoutDirection(root) == ViewCompat.LAYOUT_DIRECTION_RTL
                ? root.getWidth() - left - controls.getWidth() : left;
        if (controlParams.getMarginStart() != start || controlParams.topMargin != top
                || controlParams.gravity != (Gravity.TOP | Gravity.START)) {
            controlParams.gravity = Gravity.TOP | Gravity.START;
            controlParams.setMarginStart(start);
            controlParams.topMargin = top;
            controls.setLayoutParams(controlParams);
        }
        return true;
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("map.category", category == null ? null : category.name());
        state.putString("map.selected", selected == null ? null : selected.getId());
        state.putBoolean("map.demo", demo);
        if (search != null) { state.putString("map.query", search.getText().toString()); }
        if (mapView != null) { state.putFloatArray("map.camera", mapView.getCameraState()); }
    }
    @Override public void onDestroyView() {
        if (observedTree != null && observedTree.isAlive()) {
            observedTree.removeOnGlobalLayoutListener(layoutListener);
        }
        if (root.getViewTreeObserver().isAlive()) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
        mapView.setOnImageMatrixChangedListener(null);
        mapView.setOnMapTapListener(null);
        super.onDestroyView();
    }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private final class PlaceSearchAdapter extends ArrayAdapter<MapPlace> {
        private final List<MapPlace> results = new ArrayList<>(CampusMapData.getPlaces());
        PlaceSearchAdapter() { super(requireContext(), 0); }
        @Override public int getCount() { return Math.max(1, results.size()); }
        @Nullable @Override public MapPlace getItem(int position) {
            return position >= 0 && position < results.size() ? results.get(position) : null;
        }
        @Override public boolean isEnabled(int position) { return getItem(position) != null; }
        @Override public boolean areAllItemsEnabled() { return false; }
        private final Filter placeFilter = new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                List<MapPlace> matches = new ArrayList<>();
                for (MapPlace place : CampusMapData.getPlaces()) {
                    if (place.matches(constraint == null ? "" : constraint.toString())) { matches.add(place); }
                }
                FilterResults filtered = new FilterResults();
                filtered.values = matches;
                // Keep a disabled empty-result row visible in AutoCompleteTextView.
                filtered.count = Math.max(1, matches.size());
                return filtered;
            }
            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults filtered) {
                results.clear();
                results.addAll((List<MapPlace>) filtered.values);
                notifyDataSetChanged();
            }
            @Override public CharSequence convertResultToString(Object result) {
                return result instanceof MapPlace ? ((MapPlace) result).getName() : "";
            }
        };
        @NonNull @Override public Filter getFilter() { return placeFilter; }
        @NonNull @Override public View getView(int position, @Nullable View recycled,
                @NonNull ViewGroup parent) {
            View row = recycled == null ? LayoutInflater.from(requireContext()).inflate(
                    R.layout.item_map_search_result, parent, false) : recycled;
            TextView name = row.findViewById(R.id.tv_search_result_name);
            TextView sub = row.findViewById(R.id.tv_search_result_subtitle);
            MapPlace place = getItem(position);
            name.setText(place == null ? getString(R.string.map_no_results) : place.getName());
            sub.setText(place == null ? getString(R.string.map_try_search) : place.getSubtitle());
            return row;
        }
        @Override public View getDropDownView(int position, @Nullable View recycled,
                @NonNull ViewGroup parent) { return getView(position, recycled, parent); }
    }
}
