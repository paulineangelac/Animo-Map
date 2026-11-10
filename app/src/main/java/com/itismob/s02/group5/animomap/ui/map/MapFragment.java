package com.itismob.s02.group5.animomap.ui.map;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final float LASALLE_HALL_X = 0.21f;
    private static final float LASALLE_HALL_Y = 0.71f;

    private AutoCompleteTextView searchField;
    private ImageButton clearSearchButton;
    private MaterialCardView routePreviewCard;
    private TextView routeSummaryText;
    private ZoomableImageView mapView;
    private MapOverlayView mapOverlay;

    private final List<MapPlace> places = new ArrayList<>();
    private MapPlace selectedPlace;
    private MapPlace.Category activeCategory = MapPlace.Category.BUILDING;
    private boolean routeMode;

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
        searchField = view.findViewById(R.id.et_map_search);
        clearSearchButton = view.findViewById(R.id.btn_clear_map_search);
        routePreviewCard = view.findViewById(R.id.card_route_preview);
        routeSummaryText = view.findViewById(R.id.tv_route_summary);

        createSamplePlaces();
        mapOverlay.setMapView(mapView);
        mapOverlay.setPlaces(places);
        mapOverlay.setActiveCategory(activeCategory);
        mapView.setOnImageMatrixChangedListener(mapOverlay::invalidate);

        setupSearch();
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

    private void setupSearch() {
        PlaceSearchAdapter searchAdapter = new PlaceSearchAdapter(places);
        searchField.setAdapter(searchAdapter);
        searchField.setOnItemClickListener((parent, row, position, id) -> {
            MapPlace place = searchAdapter.getItem(position);
            if (place == null) {
                return;
            }

            selectedPlace = place;
            searchField.setText(place.getName(), false);
            searchField.dismissDropDown();
            clearSearchButton.setVisibility(View.VISIBLE);
            mapOverlay.setSelectedPlace(selectedPlace);
            mapView.centerOnMapPoint(place.getMapX(), place.getMapY());
            updateRoutePreview();
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // No action needed before the query changes.
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                clearSearchButton.setVisibility(text.length() == 0 ? View.INVISIBLE : View.VISIBLE);
                if (selectedPlace != null
                        && !selectedPlace.getName().equalsIgnoreCase(text.toString().trim())) {
                    selectedPlace = null;
                    mapOverlay.setSelectedPlace(null);
                    updateRoutePreview();
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                // AutoCompleteTextView runs the adapter filter as text changes.
            }
        });

        clearSearchButton.setOnClickListener(button -> {
            searchField.setText("");
            searchField.dismissDropDown();
            searchField.clearFocus();
            selectedPlace = null;
            mapOverlay.setSelectedPlace(null);
            updateRoutePreview();
        });
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

    private final class PlaceSearchAdapter extends ArrayAdapter<MapPlace> {
        private final List<MapPlace> allPlaces;
        private final LayoutInflater inflater;
        private final Filter placeFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MapPlace> matches = new ArrayList<>();
                if (constraint != null && !constraint.toString().trim().isEmpty()) {
                    for (MapPlace place : allPlaces) {
                        if (place.matches(constraint.toString())) {
                            matches.add(place);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = matches;
                results.count = matches.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results.values != null) {
                    addAll((List<MapPlace>) results.values);
                }
                notifyDataSetChanged();
            }
        };

        PlaceSearchAdapter(List<MapPlace> places) {
            super(requireContext(), 0, new ArrayList<>());
            this.allPlaces = new ArrayList<>(places);
            this.inflater = LayoutInflater.from(requireContext());
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return placeFilter;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = inflater.inflate(R.layout.item_map_search_result, parent, false);
            }

            MapPlace place = getItem(position);
            if (place != null) {
                TextView name = row.findViewById(R.id.tv_search_result_name);
                TextView subtitle = row.findViewById(R.id.tv_search_result_subtitle);
                name.setText(place.getName());
                subtitle.setText(place.getSubtitle());
            }
            return row;
        }

        @NonNull
        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }
}
