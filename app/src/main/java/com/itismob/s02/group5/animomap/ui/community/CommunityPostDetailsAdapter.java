package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.OtherProfileActivity;
import com.itismob.s02.group5.animomap.ui.profile.OtherUsersDataSource;
import com.itismob.s02.group5.animomap.ui.profile.UserProfile;

import java.util.Collections;
import java.util.List;

final class CommunityPostDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int COMMENTS_POSITION = 1;
    private static final int POST_HEADER = 0;
    private static final int COMMENTS_HEADER = 1;
    private static final int COMMENT = 2;
    private static final int HEADER_COUNT = 2;

    private final CommunityPost post;
    private final UserProfile profile;
    private final View.OnClickListener onSort;
    private final View.OnClickListener onReport;
    private List<CommunityComment> comments = Collections.emptyList();
    private boolean newestFirst = true;

    CommunityPostDetailsAdapter(CommunityPost post, UserProfile profile,
                                View.OnClickListener onSort, View.OnClickListener onReport) {
        this.post = post;
        this.profile = profile;
        this.onSort = onSort;
        this.onReport = onReport;
    }

    void showComments(List<CommunityComment> comments, boolean newestFirst) {
        this.comments = comments;
        this.newestFirst = newestFirst;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return HEADER_COUNT + comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return POST_HEADER;
        if (position == COMMENTS_POSITION) return COMMENTS_HEADER;
        return COMMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case POST_HEADER:
                return new PostHeaderHolder(inflater.inflate(
                        R.layout.item_community_post_details, parent, false));
            case COMMENTS_HEADER:
                return new CommentsHeaderHolder(inflater.inflate(
                        R.layout.item_community_comments_header, parent, false));
            default:
                return new CommentHolder(inflater.inflate(
                        R.layout.item_community_comment, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case POST_HEADER:
                bindPost((PostHeaderHolder) holder);
                break;
            case COMMENTS_HEADER:
                bindCommentsHeader((CommentsHeaderHolder) holder);
                break;
            default:
                bindComment((CommentHolder) holder, comments.get(position - HEADER_COUNT));
                break;
        }
    }

    private void bindPost(PostHeaderHolder holder) {
        Context context = holder.itemView.getContext();
        String author = post.getAuthorName();
        String username = author.startsWith("@") ? author : formatUsername(context,
                OtherUsersDataSource.findByName(author).getUsername());
        boolean isOwnPost = username.equals(formatUsername(context, profile.getUsername()));
        CommunityPostViews.bindAvatar(holder.initials, holder.avatar, post.getAuthorInitials(),
                post.getAvatarResourceId(), isOwnPost ? profile.getPhotoUri() : null);
        bindAuthor(holder.name, holder.avatarFrame, author, username, isOwnPost);
        String date = DateUtils.formatDateTime(context, post.getCreatedAtMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME);
        String affiliation = post.getStudentAffiliation();
        holder.date.setText(TextUtils.isEmpty(affiliation) ? date
                : context.getString(R.string.community_meta_pair, date, affiliation));
        CommunityPostType type = post.getPostType();
        holder.category.setText((type == null ? CommunityPostType.CAMPUS_TIPS : type).labelRes);
        CommunityPostViews.bindMapReviewLabel(holder.mapReview, post);
        CommunityPostViews.bindTitle(holder.title, post);
        holder.description.setText(post.getDescription());
        CommunityPostViews.bindLocation(holder.location, post);
        holder.photos.bind(post, index -> CommunityPhotoViewerActivity.open(context, post, index));
        int voteCount = CommunityFeedDataSource.getDisplayedVoteCount(post);
        holder.votes.setText(context.getResources().getQuantityString(
                R.plurals.community_upvote_total, voteCount, voteCount));
        int voteColor = CommunityFeedDataSource.isUpvoted(post)
                ? R.color.accent_teal : R.color.text_secondary;
        TextViewCompat.setCompoundDrawableTintList(holder.votes,
                ColorStateList.valueOf(context.getColor(voteColor)));
        int commentCount = post.getCommentCount();
        holder.comments.setText(context.getResources().getQuantityString(
                R.plurals.community_comments_count, commentCount, commentCount));
        holder.more.setOnClickListener(onReport);
    }

    private void bindCommentsHeader(CommentsHeaderHolder holder) {
        Context context = holder.itemView.getContext();
        holder.heading.setText(context.getString(R.string.community_comments_heading,
                post.getCommentCount()));
        int sortLabel = newestFirst ? R.string.community_filter_recent
                : R.string.community_comments_oldest;
        holder.sort.setText(sortLabel);
        holder.sort.setContentDescription(context.getString(
                R.string.community_comments_sort_description, context.getString(sortLabel)));
        holder.sort.setOnClickListener(onSort);
        holder.empty.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bindComment(CommentHolder holder, CommunityComment comment) {
        Context context = holder.itemView.getContext();
        CommunityPostViews.bindAvatar(holder.initials, holder.photo, comment.initials,
                comment.avatarResourceId, comment.avatarUri);
        String username = formatUsername(context, comment.username);
        boolean isOwnComment = username.equals(formatUsername(context, profile.getUsername()));
        bindAuthor(holder.name, holder.avatar, comment.authorName, username, isOwnComment);
        holder.body.setText(comment.text);
        long now = System.currentTimeMillis();
        holder.time.setText(now - comment.createdAtMillis < DateUtils.MINUTE_IN_MILLIS
                ? context.getString(R.string.community_time_now)
                : DateUtils.getRelativeTimeSpanString(comment.createdAtMillis, now,
                        DateUtils.MINUTE_IN_MILLIS));
    }

    private static String formatUsername(Context context, String username) {
        return username.startsWith("@") ? username
                : context.getString(R.string.profile_username_format, username);
    }

    private static void bindAuthor(TextView name, View avatar, String author, String username,
                                   boolean isCurrentUser) {
        Context context = name.getContext();
        name.setText(author.equals(username) ? author
                : context.getString(R.string.community_author_with_username, author, username));
        View.OnClickListener openProfile = isCurrentUser ? null
                : view -> OtherProfileActivity.open(context, author);
        name.setOnClickListener(openProfile);
        name.setClickable(!isCurrentUser);
        avatar.setOnClickListener(openProfile);
        avatar.setClickable(!isCurrentUser);
        avatar.setContentDescription(isCurrentUser ? null
                : context.getString(R.string.community_open_author_profile, author));
    }

    private static final class PostHeaderHolder extends RecyclerView.ViewHolder {
        final View avatarFrame;
        final ImageView avatar;
        final TextView initials;
        final TextView name;
        final TextView date;
        final TextView category;
        final View mapReview;
        final TextView title;
        final TextView description;
        final TextView location;
        final TextView votes;
        final TextView comments;
        final View more;
        final PostPhotoCarousel photos;

        PostHeaderHolder(View view) {
            super(view);
            avatarFrame = view.findViewById(R.id.details_author_avatar);
            avatar = view.findViewById(R.id.details_author_photo);
            initials = view.findViewById(R.id.details_author_initials);
            name = view.findViewById(R.id.details_author_name);
            date = view.findViewById(R.id.details_post_date);
            category = view.findViewById(R.id.details_post_category);
            mapReview = view.findViewById(R.id.details_candidate_label);
            title = view.findViewById(R.id.details_post_title);
            description = view.findViewById(R.id.details_post_description);
            location = view.findViewById(R.id.details_location_label);
            votes = view.findViewById(R.id.details_vote_count);
            comments = view.findViewById(R.id.details_comment_count);
            more = view.findViewById(R.id.details_post_more);
            photos = new PostPhotoCarousel(view);
        }
    }

    private static final class CommentsHeaderHolder extends RecyclerView.ViewHolder {
        final TextView heading;
        final TextView sort;
        final View empty;

        CommentsHeaderHolder(View view) {
            super(view);
            heading = view.findViewById(R.id.details_comments_heading);
            ViewCompat.setAccessibilityHeading(heading, true);
            sort = view.findViewById(R.id.details_comments_sort);
            empty = view.findViewById(R.id.details_comments_empty);
        }
    }

    private static final class CommentHolder extends RecyclerView.ViewHolder {
        final View avatar;
        final TextView initials;
        final ImageView photo;
        final TextView name;
        final TextView time;
        final TextView body;

        CommentHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.comment_author_avatar);
            initials = view.findViewById(R.id.comment_author_initials);
            photo = view.findViewById(R.id.comment_author_photo);
            name = view.findViewById(R.id.comment_author_name);
            time = view.findViewById(R.id.comment_time);
            body = view.findViewById(R.id.comment_text);
        }
    }
}
