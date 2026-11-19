package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

import java.util.List;

/**
 * Shows the student's place reviews on Profile.
 */
public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.ReviewViewHolder> {

    private final List<MyReview> reviews;

    public MyReviewAdapter(List<MyReview> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        MyReview review = reviews.get(position);
        Context context = holder.itemView.getContext();

        holder.tvReviewPlace.setText(review.getPlaceName());
        holder.tvReviewMeta.setText(context.getString(R.string.my_item_meta,
                review.getStars(), review.getPlaceType()));
        holder.tvReviewComment.setText(review.getComment());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView tvReviewPlace, tvReviewMeta, tvReviewComment;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewPlace = itemView.findViewById(R.id.tvReviewPlace);
            tvReviewMeta = itemView.findViewById(R.id.tvReviewMeta);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
        }
    }
}