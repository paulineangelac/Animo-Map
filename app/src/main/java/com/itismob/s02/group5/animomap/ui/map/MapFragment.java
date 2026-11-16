package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final float LASALLE_HALL_X = 0.21f;
    private static final float LASALLE_HALL_Y = 0.71f;
    private static final int MAX_RECENT_PLACES = 6;

    private MaterialCardView searchCard;
    private TextView searchPrompt;
    private EditText searchField;
    private ImageButton clearSearchButton;
    private ImageButton closeSearchButton;
    private LinearLayout searchPanel;
    private TextView searchResultsHeading;
    private TextView searchEmptyText;
    private MapSearchAdapter searchAdapter;
    private OnBackPressedCallback searchBackCallback;
    private MaterialCardView routePreviewCard;
    private TextView routeSummaryText;
    private ZoomableImageView mapView;
    private MapOverlayView mapOverlay;

    private final List<MapPlace> places = new ArrayList<>();
    private final List<MapPlace> recentPlaces = new ArrayList<>();
    private MapPlace selectedPlace;
    private MapPlace.Category activeCategory = MapPlace.Category.BUILDING;
    private boolean routeMode;
    private boolean suppressSearchChange;

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.iv_campus_map);
        mapOverlay = view.findViewById(R.id.map_overlay);
        searchCard = view.findViewById(R.id.map_search_card);
        searchPrompt = view.findViewById(R.id.tv_map_search_prompt);
        searchField = view.findViewById(R.id.et_map_search);
        clearSearchButton = view.findViewById(R.id.btn_clear_map_search);
        closeSearchButton = view.findViewById(R.id.btn_close_map_search);
        searchPanel = view.findViewById(R.id.map_search_panel);
        searchResultsHeading = view.findViewById(R.id.tv_map_search_results_heading);
        searchEmptyText = view.findViewById(R.id.tv_map_search_empty);
        routePreviewCard = view.findViewById(R.id.card_route_preview);
        routeSummaryText = view.findViewById(R.id.tv_route_summary);

        createSamplePlaces();
        seedRecentPlaces();
        mapOverlay.setMapView(mapView);
        mapOverlay.setPlaces(places);
        mapOverlay.setActiveCategory(activeCategory);
        mapView.setOnImageMatrixChangedListener(mapOverlay::invalidate);

        setupSearch(view);
        setupFilters(view);
        updateRoutePreview();
    }

    private void createSamplePlaces() {
        places.clear();
        places.add(new MapPlace(getString(R.string.place_st_lasalle_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                LASALLE_HALL_X, LASALLE_HALL_Y));
        places.add(new MapPlace(getString(R.string.place_henry_sy_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                0.45f, 0.56f));
        places.add(new MapPlace(getString(R.string.place_velasco_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                0.56f, 0.62f));
        places.add(new MapPlace(getString(R.string.place_yuchengco_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                0.27f, 0.44f));
        places.add(new MapPlace(getString(R.string.place_st_miguel_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                0.66f, 0.44f));
        places.add(new MapPlace(getString(R.string.place_gokongwei_hall),
                getString(R.string.place_type_building), MapPlace.Category.BUILDING,
                0.70f, 0.71f));
        places.add(new MapPlace(getString(R.string.place_g303),
                getString(R.string.place_g303_subtitle), MapPlace.Category.BUILDING,
                0.70f, 0.73f));
        places.add(new MapPlace(getString(R.string.place_study_demo),
                getString(R.string.place_study_demo_subtitle), MapPlace.Category.STUDY,
                0.45f, 0.58f));
        places.add(new MapPlace(getString(R.string.place_food_demo),
                getString(R.string.place_food_demo_subtitle), MapPlace.Category.FOOD,
                0.60f, 0.55f));
    }

    private void seedRecentPlaces() {
        recentPlaces.clear();
        if (places.size() >= 6) {
            recentPlaces.add(places.get(0));
            recentPlaces.add(places.get(1));
            recentPlaces.add(places.get(5));
        }
    }

    private void setupSearch(View root) {
        RecyclerView resultsList = root.findViewById(R.id.rv_map_search_results);
        searchAdapter = new MapSearchAdapter(this::selectPlace);
        resultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        resultsList.setAdapter(searchAdapter);
        updateSearchResults("");

        searchCard.setOnClickListener(button -> openSearch());
        searchPrompt.setOnClickListener(button -> openSearch());
        closeSearchButton.setOnClickListener(button -> closeSearch());

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // No action needed before the query changes.
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (suppressSearchChange) {
                    return;
                }
                String query = text.toString().trim();
                if (selectedPlace != null && !selectedPlace.getName().equalsIgnoreCase(query)) {
                    selectedPlace = null;
                    updateRoutePreview();
                }
                updateSearchResults(query);
            }

            @Override
            public void afterTextChanged(Editable text) {
                // Results are refreshed in onTextChanged.
            }
        });

        clearSearchButton.setOnClickListener(button -> clearSelectedPlace());

        searchBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeSearch();
            }
        };
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), searchBackCallback);
    }

    private void openSearch() {
        searchPanel.setVisibility(View.VISIBLE);
        searchBackCallback.setEnabled(true);
        suppressSearchChange = true;
        searchField.setText("");
        suppressSearchChange = false;
        updateSearchResults("");
        searchField.requestFocus();
        searchField.postDelayed(() -> {
            if (!isAdded() || searchPanel.getVisibility() != View.VISIBLE) {
                return;
            }
            InputMethodManager keyboard = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) {
                keyboard.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 160);
    }

    private void closeSearch() {
        hideKeyboard();
        searchPanel.setVisibility(View.GONE);
        searchBackCallback.setEnabled(false);
        suppressSearchChange = true;
        searchField.setText("");
        suppressSearchChange = false;
        searchPrompt.setText(selectedPlace == null
                ? getString(R.string.map_search_landing_label) : selectedPlace.getName());
        clearSearchButton.setVisibility(selectedPlace == null ? View.GONE : View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
        }
        searchField.clearFocus();
    }

    private void updateSearchResults(String query) {
        boolean showingRecents = query.isEmpty();
        List<MapPlace> matches = new ArrayList<>();
        if (showingRecents) {
            matches.addAll(recentPlaces);
            searchResultsHeading.setText(R.string.map_search_recent_heading);
        } else {
            for (MapPlace place : places) {
                if (place.matches(query)) {
                    matches.add(place);
                }
            }
            searchResultsHeading.setText(R.string.map_search_results_heading);
        }
        searchAdapter.setPlaces(matches, showingRecents);
        searchEmptyText.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void selectPlace(MapPlace place) {
        selectedPlace = place;
        addRecentPlace(place);
        searchPrompt.setText(place.getName());
        clearSearchButton.setVisibility(View.VISIBLE);
        mapOverlay.setSelectedPlace(place);
        mapView.centerOnMapPoint(place.getMapX(), place.getMapY());
        updateRoutePreview();
        closeSearch();
    }

    private void addRecentPlace(MapPlace place) {
        for (int i = recentPlaces.size() - 1; i >= 0; i--) {
            if (recentPlaces.get(i).getName().equalsIgnoreCase(place.getName())) {
                recentPlaces.remove(i);
            }
        }
        recentPlaces.add(0, place);
        while (recentPlaces.size() > MAX_RECENT_PLACES) {
            recentPlaces.remove(recentPlaces.size() - 1);
        }
    }

    private void clearSelectedPlace() {
        selectedPlace = null;
        searchPrompt.setText(R.string.map_search_landing_label);
        clearSearchButton.setVisibility(View.GONE);
        mapOverlay.setSelectedPlace(null);
        updateRoutePreview();
    }

    private void setupFilters(View view) {
        Chip routeChip = view.findViewById(R.id.chip_route);
        Chip buildingsChip = view.findViewById(R.id.chip_buildings);
        Chip studyChip = view.findViewById(R.id.chip_study);
        Chip foodChip = view.findViewById(R.id.chip_food);

        routeChip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                routeMode = true;
                updateMapMode();
            }
        });
        buildingsChip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                activeCategory = MapPlace.Category.BUILDING;
                routeMode = false;
                updateMapMode();
            }
        });
        studyChip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                activeCategory = MapPlace.Category.STUDY;
                routeMode = false;
                updateMapMode();
            }
        });
        foodChip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                activeCategory = MapPlace.Category.FOOD;
                routeMode = false;
                updateMapMode();
            }
        });
    }

    private void updateMapMode() {
        mapOverlay.setActiveCategory(activeCategory);
        mapOverlay.setRouteVisible(routeMode && selectedPlace != null);
        updateRoutePreview();
    }

    private void updateRoutePreview() {
        boolean showRoute = routeMode && selectedPlace != null;
        routePreviewCard.setVisibility(showRoute ? View.VISIBLE : View.GONE);
        mapOverlay.setRouteVisible(showRoute);
        mapOverlay.setSelectedPlace(selectedPlace);
        if (showRoute) {
            routeSummaryText.setText(getString(R.string.map_route_summary,
                    getString(R.string.map_route_origin), selectedPlace.getName()));
        }
    }
}
