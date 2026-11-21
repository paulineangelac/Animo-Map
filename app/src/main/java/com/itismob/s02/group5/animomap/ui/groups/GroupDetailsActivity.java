package com.itismob.s02.group5.animomap.ui.groups;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.UserProfile;

import java.util.Locale;

/**
 * Group Details: the group's name, description, invite code, and members.
 * Members shows their username, profile picture, and the campus building they share
 * (with when it was last updated) if they turned sharing on for this group.
 */
public class GroupDetailsActivity extends AppCompatActivity {

    // Key for the id of the group to show, sent by the screen that opens this one
    public static final String EXTRA_GROUP_ID = "extra_group_id";

    // Views
    private TextView tvGroupAvatar, tvGroupName, tvGroupMeta, tvGroupDescription;
    private TextView tvGroupInviteCode, tvGroupSharingStatus, tvGroupMembersHeader;
    private RecyclerView rvGroupMembers;

    private String groupId;
    private GroupItem group;

    public static void open(Context context, String groupId) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_group_details);

        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        final int padLeft = root.getPaddingLeft();
        final int padTop = root.getPaddingTop();
        final int padRight = root.getPaddingRight();
        final int padBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padLeft + bars.left, padTop + bars.top, padRight + bars.right, padBottom + bars.bottom);
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.btnGroupBack);
        tvGroupAvatar = findViewById(R.id.tvGroupAvatar);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupMeta = findViewById(R.id.tvGroupMeta);
        tvGroupDescription = findViewById(R.id.tvGroupDescription);
        tvGroupInviteCode = findViewById(R.id.tvGroupInviteCode);
        tvGroupSharingStatus = findViewById(R.id.tvGroupSharingStatus);
        tvGroupMembersHeader = findViewById(R.id.tvGroupMembersHeader);
        rvGroupMembers = findViewById(R.id.rvGroupMembers);
        rvGroupMembers.setLayoutManager(new LinearLayoutManager(this));

        // Buttons
        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnCopyInviteCode).setOnClickListener(v -> copyInviteCode());
        findViewById(R.id.btnShareInviteCode).setOnClickListener(v -> shareInviteCode());

        // Which group to show. Only members can open it.
        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        group = groupId == null ? null : GroupsDataSource.findById(groupId);
        if (group == null || !group.hasMe()) {
            Toast.makeText(this, R.string.group_details_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*
     * Fill the screen every time it comes back into view (for example, after viewing a member's
     * profile), so "Updated ... ago" times stay current.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (group != null) {
            showGroup();
        }
    }

    private void showGroup() {
        String name = group.getName();
        tvGroupAvatar.setText(name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ROOT));
        tvGroupName.setText(name);

        // Example: "4 members · Created by Mika S."
        String memberCount = getResources().getQuantityString(
                R.plurals.groups_member_count, group.getMemberCount(), group.getMemberCount());
        if (group.getCreatedBy().isEmpty()) {
            tvGroupMeta.setText(getString(R.string.group_details_meta_by_you, memberCount));
        } else {
            tvGroupMeta.setText(getString(R.string.group_details_meta_by_other, memberCount, group.getCreatedBy()));
        }

        // Description is optional
        tvGroupDescription.setText(group.getDescription());
        tvGroupDescription.setVisibility(group.getDescription().isEmpty() ? View.GONE : View.VISIBLE);

        tvGroupInviteCode.setText(group.getInviteCode());

        // Is the student's own building visible to this group?
        if (GroupsDataSource.isSharingWith(group.getId())) {
            tvGroupSharingStatus.setText(getString(R.string.group_details_sharing_on,
                    GroupsDataSource.getMyBuildingName()));
            tvGroupSharingStatus.setTextColor(getColor(R.color.accent_teal));
        } else {
            tvGroupSharingStatus.setText(R.string.group_details_sharing_off);
            tvGroupSharingStatus.setTextColor(getColor(R.color.text_secondary));
        }

        tvGroupMembersHeader.setText(getString(R.string.group_details_members_header, group.getMemberCount()));
        rvGroupMembers.setAdapter(new GroupMemberAdapter(group, UserProfile.load(this)));
    }

    // Puts the invite code on the clipboard so it can be pasted into a chat
    private void copyInviteCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(
                    getString(R.string.group_details_invite_label), group.getInviteCode()));
            Toast.makeText(this, R.string.group_details_copied, Toast.LENGTH_SHORT).show();
        }
    }

    // Opens the phone's share sheet (Messenger, SMS, email, ...) with an invite message
    private void shareInviteCode() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.group_details_share_message, group.getName(), group.getInviteCode()));
        startActivity(Intent.createChooser(sendIntent, getString(R.string.group_details_share_chooser)));
    }
}
