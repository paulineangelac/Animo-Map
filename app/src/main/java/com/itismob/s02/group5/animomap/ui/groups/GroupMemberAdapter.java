package com.itismob.s02.group5.animomap.ui.groups;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.OtherProfileActivity;
import com.itismob.s02.group5.animomap.ui.profile.OtherUser;
import com.itismob.s02.group5.animomap.ui.profile.OtherUsersDataSource;
import com.itismob.s02.group5.animomap.ui.profile.UserProfile;

import java.util.List;

/**
 * Shows a group's members in Group Details: profile picture, name, username, and the campus
 * building they share with this group (with when it was last updated).
 * Tapping another member opens their public profile.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {

    private final GroupItem group;
    private final List<GroupMember> members;
    private final UserProfile myProfile;

    public GroupMemberAdapter(GroupItem group, UserProfile myProfile) {
        this.group = group;
        this.members = group.getMembers();
        this.myProfile = myProfile;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        GroupMember member = members.get(position);
        Context context = holder.itemView.getContext();

        if (member.isMe()) {
            bindMe(holder, context);
        } else {
            bindOtherMember(holder, context, member);
        }
        bindPresence(holder, context, GroupsDataSource.getPresence(group, member));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    /*
     * your row: uses your current profile, so edits on the Profile tab show up here.
     * It isn't clickable, since your own profile is on the Profile tab.
     */
    private void bindMe(MemberViewHolder holder, Context context) {
        holder.tvName.setText(context.getString(R.string.group_member_you, myProfile.getFullName()));
        holder.tvUsername.setText(context.getString(R.string.profile_username_format, myProfile.getUsername()));

        if (TextUtils.isEmpty(myProfile.getPhotoUri())) {
            showInitials(holder, myProfile.getInitials());
        } else {
            holder.ivPhoto.setImageURI(null);   // forces a refresh if the photo changed
            holder.ivPhoto.setImageURI(Uri.parse(myProfile.getPhotoUri()));
            showPhoto(holder, context, myProfile.getFullName());
        }

        holder.ivChevron.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);
    }

    /*
     * other members: their public profile picture and username. Tapping opens their profile.
     */
    private void bindOtherMember(MemberViewHolder holder, Context context, GroupMember member) {
        OtherUser user = OtherUsersDataSource.findByName(member.getName());

        holder.tvName.setText(member.getName());
        holder.tvUsername.setText(context.getString(R.string.profile_username_format, member.getUsername()));

        if (user.getAvatarResId() != 0) {
            holder.ivPhoto.setImageResource(user.getAvatarResId());
            showPhoto(holder, context, member.getName());
        } else {
            showInitials(holder, user.getInitials());
        }

        holder.ivChevron.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(v -> OtherProfileActivity.open(context, member.getName()));
    }

    /*
     * PRESENCE: "Gokongwei Hall · Updated 2 minutes ago", or "Not sharing their location".
     */
    private void bindPresence(MemberViewHolder holder, Context context, CampusPresence presence) {
        if (presence == null) {
            holder.tvPresence.setText(R.string.group_member_not_sharing);
            holder.tvPresence.setTextColor(context.getColor(R.color.text_secondary));
            holder.ivPresenceIcon.setVisibility(View.GONE);
        } else {
            holder.tvPresence.setText(context.getString(R.string.group_member_presence,
                    presence.getBuildingName(), presence.getUpdatedAgo(context)));
            holder.tvPresence.setTextColor(context.getColor(R.color.text_primary));
            holder.ivPresenceIcon.setVisibility(View.VISIBLE);
        }
    }

    private void showInitials(MemberViewHolder holder, String initials) {
        holder.tvInitials.setText(initials);
        holder.tvInitials.setVisibility(View.VISIBLE);
        holder.ivPhoto.setVisibility(View.GONE);
    }

    private void showPhoto(MemberViewHolder holder, Context context, String name) {
        holder.ivPhoto.setContentDescription(context.getString(R.string.group_member_avatar_description, name));
        holder.ivPhoto.setVisibility(View.VISIBLE);
        holder.tvInitials.setVisibility(View.GONE);
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitials, tvName, tvUsername, tvPresence;
        final ShapeableImageView ivPhoto;
        final ImageView ivPresenceIcon, ivChevron;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvMemberInitials);
            ivPhoto = itemView.findViewById(R.id.ivMemberPhoto);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvUsername = itemView.findViewById(R.id.tvMemberUsername);
            tvPresence = itemView.findViewById(R.id.tvMemberPresence);
            ivPresenceIcon = itemView.findViewById(R.id.ivMemberPresenceIcon);
            ivChevron = itemView.findViewById(R.id.ivMemberChevron);
        }
    }
}
