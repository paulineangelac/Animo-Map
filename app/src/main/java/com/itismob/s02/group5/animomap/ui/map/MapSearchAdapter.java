package com.itismob.s02.group5.animomap.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

/** Displays recent places or matching places in the map search panel. */
public class MapSearchAdapter extends RecyclerView.Adapter<MapSearchAdapter.PlaceViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(MapPlace place);
    }

    private final List<MapPlace> places = new ArrayList<>();
    private final OnPlaceClickListener listener;
    private boolean showingRecentPlaces;

    public MapSearchAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void setPlaces(List<MapPlace> newPlaces, boolean showingRecentPlaces) {
        places.clear();
        places.addAll(newPlaces);
        this.showingRecentPlaces = showingRecentPlaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_search_result, parent, false);
        return new PlaceViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        MapPlace place = places.get(position);
        holder.name.setText(place.getName());
        holder.subtitle.setText(place.getLocationLine());
        holder.icon.setImageResource(showingRecentPlaces
                ? R.drawable.ic_map_history : R.drawable.ic_map_search);
        holder.icon.setContentDescription(holder.itemView.getContext().getString(
                showingRecentPlaces ? R.string.map_recent_place_description
                        : R.string.map_search_icon_description));
        holder.itemView.setOnClickListener(view -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;
        final TextView subtitle;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_search_result_icon);
            name = itemView.findViewById(R.id.tv_search_result_name);
            subtitle = itemView.findViewById(R.id.tv_search_result_subtitle);
        }
    }
}
