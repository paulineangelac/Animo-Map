package com.itismob.s02.group5.animomap.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
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

    // The list is short and replaced as a whole on every keystroke, so a full refresh is fine here.
    @SuppressLint("NotifyDataSetChanged")
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
        Context context = holder.itemView.getContext();
        String categoryName = context.getString(MapPlace.categoryLabelRes(place.getCategory()));
        String location = place.getLocationLine();
        // "Restroom · Henry Sy Sr. Hall · Floor 3", or just "Building" when there is nothing more to say.
        holder.subtitle.setText(location.equals(categoryName) ? categoryName
                : categoryName + " · " + location);
        if (showingRecentPlaces) {
            holder.icon.setImageResource(R.drawable.ic_map_history);
            ImageViewCompat.setImageTintList(holder.icon, null);
            holder.icon.setContentDescription(context.getString(R.string.map_recent_place_description));
        } else {
            // Search results show the category symbol so places can be told apart at a glance.
            holder.icon.setImageResource(MapPlace.glyphRes(place.getCategory()));
            ImageViewCompat.setImageTintList(holder.icon, ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.accent_teal)));
            holder.icon.setContentDescription(categoryName);
        }
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
