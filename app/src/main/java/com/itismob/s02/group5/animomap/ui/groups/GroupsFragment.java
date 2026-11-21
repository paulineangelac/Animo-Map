package com.itismob.s02.group5.animomap.ui.groups;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * My Groups: the student's joined groups, location sharing, and the Create / Join buttons.
 * Tapping a group opens GroupDetailsActivity.
 */
public class GroupsFragment extends Fragment {

    // How many member initials fit on a group card before showing "+N"
    private static final int MAX_AVATARS = 3;

    // Views
    private LinearLayout groupsListContainer;
    private View emptyState;
    private View featuredCard;
    private TextView featuredGroupAvatar;
    private TextView featuredGroupName;
    private TextView featuredGroupDetail;
    private TextView sharingStatusTitle;
    private TextView sharingStatusSubtitle;
    private TextView sharingStatusBadge;

    public GroupsFragment() {
        // empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groupsListContainer = view.findViewById(R.id.groups_list_container);
        emptyState = view.findViewById(R.id.groups_empty_state);
        featuredCard = view.findViewById(R.id.groups_featured_card);
        featuredGroupAvatar = view.findViewById(R.id.groups_featured_avatar);
        featuredGroupName = view.findViewById(R.id.groups_featured_name);
        featuredGroupDetail = view.findViewById(R.id.groups_featured_detail);
        sharingStatusTitle = view.findViewById(R.id.sharing_status_title);
        sharingStatusSubtitle = view.findViewById(R.id.sharing_status_subtitle);
        sharingStatusBadge = view.findViewById(R.id.sharing_status_badge);

        view.findViewById(R.id.btn_choose_location).setOnClickListener(v -> showAudienceSelectionDialog());
        view.findViewById(R.id.btn_create_group).setOnClickListener(v -> showCreateGroupDialog());
        view.findViewById(R.id.btn_join_code).setOnClickListener(v -> showJoinWithCodeDialog());
        view.findViewById(R.id.action_manage_groups).setOnClickListener(v ->
                showMessage(getString(R.string.groups_manage_unavailable)));
        // da list is filled in onResume, which always runs after this
    }

    /*
     * Refresh when returning from Group Details or Edit Profile,
     * so names, photos, and "updated ... ago" times are current.
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshScreen();
    }

    // MainActivity hides/shows tabs instead of recreating them, so refresh when shown again
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshScreen();
        }
    }

    private void refreshScreen() {
        if (getView() == null) {
            return;
        }
        List<GroupItem> myGroups = GroupsDataSource.getMyGroups();
        updateLocationSharingUI(myGroups);
        updateFeaturedGroup(myGroups);
        renderGroupsList(myGroups);
    }

    /*
     * Featured Card: 1st group in your list. "Continue with your group" shows the most recent group.
     * Hidden when the student has no groups.
     */
    private void updateFeaturedGroup(List<GroupItem> myGroups) {
        if (myGroups.isEmpty()) {
            featuredCard.setVisibility(View.GONE);
            return;
        }

        GroupItem group = myGroups.get(0);
        featuredCard.setVisibility(View.VISIBLE);
        featuredGroupAvatar.setText(getFirstLetter(group.getName()));
        featuredGroupName.setText(group.getName());
        featuredGroupDetail.setText(getMemberCountText(group.getMemberCount()) + " · " + getPresenceSummary(group));
        featuredCard.setContentDescription(getString(R.string.groups_open_group_description, group.getName()));
        featuredCard.setOnClickListener(v -> openGroup(group));
    }

    /*
     * Group List: one card per joined group with its name, member count, member initials,
     * and how many members are sharing their building.
     */
    private void renderGroupsList(List<GroupItem> myGroups) {
        groupsListContainer.removeAllViews();
        emptyState.setVisibility(myGroups.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        String myInitials = UserProfile.load(requireContext()).getInitials();

        for (GroupItem group : myGroups) {
            View cardView = inflater.inflate(R.layout.item_group, groupsListContainer, false);

            TextView nameView = cardView.findViewById(R.id.group_name);
            TextView statusLineView = cardView.findViewById(R.id.group_status_line);
            TextView presenceSummaryView = cardView.findViewById(R.id.group_presence_summary);
            TextView[] avatarViews = {
                    cardView.findViewById(R.id.avatar_1),
                    cardView.findViewById(R.id.avatar_2),
                    cardView.findViewById(R.id.avatar_3)
            };
            TextView avatarOverflowView = cardView.findViewById(R.id.avatar_overflow);

            // Every group in Animo Map is invite-only, sooo the PRIVATE badge stays visible
            nameView.setText(group.getName());
            statusLineView.setText(getMemberCountText(group.getMemberCount()));
            presenceSummaryView.setText(getPresenceSummary(group));

            // Initials of the first few members, then "+N" for the rest
            List<GroupMember> members = group.getMembers();
            for (int i = 0; i < avatarViews.length; i++) {
                if (i < members.size()) {
                    GroupMember member = members.get(i);
                    avatarViews[i].setText(member.isMe() ? myInitials : getInitials(member.getName()));
                    avatarViews[i].setVisibility(View.VISIBLE);
                } else {
                    avatarViews[i].setVisibility(View.GONE);
                }
            }
            int extraMembers = members.size() - MAX_AVATARS;
            if (extraMembers > 0) {
                avatarOverflowView.setText("+" + extraMembers);
                avatarOverflowView.setVisibility(View.VISIBLE);
            } else {
                avatarOverflowView.setVisibility(View.GONE);
            }

            cardView.setContentDescription(getString(R.string.groups_open_group_description, group.getName()));
            cardView.setOnClickListener(v -> openGroup(group));
            groupsListContainer.addView(cardView);
        }
    }

    private void openGroup(GroupItem group) {
        GroupDetailsActivity.open(requireContext(), group.getId());
    }

    // create group

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_group, null);

        EditText nameInput = dialogView.findViewById(R.id.dialog_create_input);
        EditText descriptionInput = dialogView.findViewById(R.id.dialog_create_description);
        TextView errorView = dialogView.findViewById(R.id.dialog_create_error);
        View cancelBtn = dialogView.findViewById(R.id.dialog_create_cancel);
        View submitBtn = dialogView.findViewById(R.id.dialog_create_submit);

        AlertDialog dialog = createDialog(dialogView);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        submitBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            // Check the name first, then the description, and show the first problem found
            List<GroupItem> myGroups = GroupsDataSource.getMyGroups();
            int nameError = GroupsDataSource.validateGroupName(name, myGroups);
            if (nameError != 0) {
                showError(errorView, getString(nameError));
                nameInput.requestFocus();
                return;
            }
            int descriptionError = GroupsDataSource.validateDescription(description);
            if (descriptionError != 0) {
                showError(errorView, getString(descriptionError));
                descriptionInput.requestFocus();
                return;
            }

            GroupItem newGroup = GroupsDataSource.createGroup(name, description);
            dialog.dismiss();
            refreshScreen();
            showMessage(getString(R.string.groups_created_success, newGroup.getName()));
            // Open the new group so the student sees its invite code right away
            openGroup(newGroup);
        });

        dialog.show();
    }

    // join group with a code

    private void showJoinWithCodeDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_join_group, null);

        EditText input = dialogView.findViewById(R.id.dialog_join_input);
        TextView errorView = dialogView.findViewById(R.id.dialog_join_error);
        View cancelBtn = dialogView.findViewById(R.id.dialog_join_cancel);
        View submitBtn = dialogView.findViewById(R.id.dialog_join_submit);

        // Typed letters become capitals, and at most 6 characters fit
        input.setFilters(new InputFilter[]{
                new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(GroupsDataSource.CODE_LENGTH)
        });

        AlertDialog dialog = createDialog(dialogView);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        submitBtn.setOnClickListener(v -> {
            String code = GroupsDataSource.normalizeCode(input.getText().toString());

            // checks if it looks like a code
            int formatError = GroupsDataSource.validateInviteCode(code);
            if (formatError != 0) {
                showError(errorView, getString(formatError));
                return;
            }

            // checks if it is the group's specific code
            GroupItem group = GroupsDataSource.findByCode(code);
            if (group == null) {
                showError(errorView, getString(R.string.groups_error_code_not_found));
                return;
            }

            // checks if student's already in (I'm in, 1.3s)
            if (group.hasMe()) {
                showError(errorView, getString(R.string.groups_error_already_member, group.getName()));
                return;
            }

            // Joining never turns on location sharing by itself
            GroupsDataSource.joinGroup(group);
            dialog.dismiss();
            refreshScreen();
            showMessage(getString(R.string.groups_joined_success, group.getName()));
            openGroup(group);
        });

        dialog.show();
    }

    // Location Sharing

    /*
     * choose who can see my location: Off, all groups, or one group, plus the building to share.
     * The group options are made from the student's current groups.
     */
    private void showAudienceSelectionDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_audience_selection, null);

        RadioGroup radioGroup = dialogView.findViewById(R.id.dialog_audience_radio_group);
        View buildingSection = dialogView.findViewById(R.id.dialog_audience_building_section);
        Spinner buildingSpinner = dialogView.findViewById(R.id.dialog_audience_building);
        TextView errorView = dialogView.findViewById(R.id.dialog_audience_error);
        View cancelBtn = dialogView.findViewById(R.id.dialog_audience_cancel);
        View confirmBtn = dialogView.findViewById(R.id.dialog_audience_confirm);

        // One radio button per group. The group's id is saved as the button's tag.
        String currentAudience = GroupsDataSource.getMyAudience();
        int checkedId = GroupsDataSource.AUDIENCE_ALL.equals(currentAudience) ? R.id.radio_all : R.id.radio_off;
        RadioButton allButton = dialogView.findViewById(R.id.radio_all);
        for (GroupItem group : GroupsDataSource.getMyGroups()) {
            RadioButton button = new RadioButton(requireContext());
            button.setId(View.generateViewId());
            button.setTag(group.getId());
            button.setText(getString(R.string.groups_audience_one, group.getName()));
            button.setTextColor(requireContext().getColor(R.color.text_primary));
            button.setTextSize(14);
            button.setButtonTintList(allButton.getButtonTintList());
            button.setPadding(allButton.getPaddingLeft(), allButton.getPaddingTop(),
                    allButton.getPaddingRight(), allButton.getPaddingBottom());
            radioGroup.addView(button, new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (group.getId().equals(currentAudience)) {
                checkedId = button.getId();
            }
        }
        radioGroup.check(checkedId);

        // Building list: "Choose a building" first, then every campus building
        List<String> buildingOptions = new ArrayList<>();
        buildingOptions.add(getString(R.string.groups_building_prompt));
        buildingOptions.addAll(GroupsDataSource.getCampusBuildingNames());
        ArrayAdapter<String> buildingAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, buildingOptions);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildingSpinner.setAdapter(buildingAdapter);
        int savedBuilding = buildingOptions.indexOf(GroupsDataSource.getMyBuildingName());
        buildingSpinner.setSelection(Math.max(savedBuilding, 0));

        // Only ask for a building when sharing is on
        buildingSection.setVisibility(checkedId == R.id.radio_off ? View.GONE : View.VISIBLE);
        radioGroup.setOnCheckedChangeListener((group, id) -> {
            buildingSection.setVisibility(id == R.id.radio_off ? View.GONE : View.VISIBLE);
            errorView.setVisibility(View.GONE);
        });

        AlertDialog dialog = createDialog(dialogView);
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_off || selectedId == View.NO_ID) {
                GroupsDataSource.setMySharing(GroupsDataSource.AUDIENCE_OFF, "");
                showMessage(getString(R.string.groups_sharing_turned_off));
            } else {
                // Sharing needs a building (position 0 is the "Choose a building" prompt)
                if (buildingSpinner.getSelectedItemPosition() <= 0) {
                    errorView.setVisibility(View.VISIBLE);
                    return;
                }
                String building = (String) buildingSpinner.getSelectedItem();
                String audience = selectedId == R.id.radio_all
                        ? GroupsDataSource.AUDIENCE_ALL
                        : (String) dialogView.findViewById(selectedId).getTag();
                GroupsDataSource.setMySharing(audience, building);
                showMessage(getString(R.string.groups_sharing_turned_on, building));
            }
            refreshScreen();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateLocationSharingUI(List<GroupItem> myGroups) {
        if (!GroupsDataSource.isSharingOn()) {
            sharingStatusTitle.setText(R.string.groups_title_location_sharing_off);
            sharingStatusSubtitle.setText(R.string.groups_subtitle_location_sharing_off);
            sharingStatusBadge.setText(R.string.groups_badge_off);
            sharingStatusBadge.setTextColor(requireContext().getColor(R.color.text_secondary));
            return;
        }

        String building = GroupsDataSource.getMyBuildingName();
        String audience = GroupsDataSource.getMyAudience();
        String subtitle = getString(R.string.groups_subtitle_sharing_all, building);
        for (GroupItem group : myGroups) {
            if (group.getId().equals(audience)) {
                subtitle = getString(R.string.groups_subtitle_sharing_one, building, group.getName());
            }
        }

        sharingStatusTitle.setText(R.string.groups_title_location_sharing_on);
        sharingStatusSubtitle.setText(subtitle);
        sharingStatusBadge.setText(R.string.groups_badge_on);
        sharingStatusBadge.setTextColor(requireContext().getColor(R.color.accent_teal));
    }

    // helpers

    // Example: "4 members"
    private String getMemberCountText(int count) {
        return getResources().getQuantityString(R.plurals.groups_member_count, count, count);
    }

    // Example: "2 on campus · updated 5 minutes ago", or "No one is sharing right now"
    private String getPresenceSummary(GroupItem group) {
        int sharing = GroupsDataSource.countSharing(group);
        CampusPresence latest = GroupsDataSource.getLatestPresence(group);
        if (sharing == 0 || latest == null) {
            return getString(R.string.groups_presence_none);
        }
        return getString(R.string.groups_presence_summary, sharing, latest.getUpdatedAgo(requireContext()));
    }

    // Example: "Mika S." becomes "MS"
    private static String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (!word.isEmpty() && initials.length() < 2) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return initials.toString();
    }

    private static String getFirstLetter(String name) {
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "" : trimmed.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    // Dialog with the rounded card background from the layout
    private AlertDialog createDialog(View dialogView) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    // Shows an error inside a dialog, where a Snackbar behind the dialog would be hard to see
    private static void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
