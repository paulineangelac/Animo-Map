package com.itismob.s02.group5.animomap.ui.groups;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupsFragment extends Fragment {

    private final List<GroupItem> groupList = new ArrayList<>(GroupsDataSource.getSampleGroups());

    private LinearLayout groupsListContainer;
    private TextView sharingStatusTitle;
    private TextView sharingStatusSubtitle;
    private TextView sharingStatusBadge;

    private boolean isLocationSharingOn = false;
    private String selectedAudience = "Off (Private)";

    public GroupsFragment() {
        // Required empty public constructor
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
        sharingStatusTitle = view.findViewById(R.id.sharing_status_title);
        sharingStatusSubtitle = view.findViewById(R.id.sharing_status_subtitle);
        sharingStatusBadge = view.findViewById(R.id.sharing_status_badge);

        View btnChooseLocation = view.findViewById(R.id.btn_choose_location);
        View btnCreateGroup = view.findViewById(R.id.btn_create_group);
        View btnJoinCode = view.findViewById(R.id.btn_join_code);
        View actionManageGroups = view.findViewById(R.id.action_manage_groups);

        if (btnChooseLocation != null) {
            btnChooseLocation.setOnClickListener(v -> showAudienceSelectionDialog());
        }

        if (btnCreateGroup != null) {
            btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        }

        if (btnJoinCode != null) {
            btnJoinCode.setOnClickListener(v -> showJoinWithCodeDialog());
        }

        if (actionManageGroups != null) {
            actionManageGroups.setOnClickListener(v ->
                    showMessage(getString(R.string.groups_manage_unavailable)));
        }

        updateLocationSharingUI();
        renderGroupsList();
    }

    private void showAudienceSelectionDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_audience_selection, null);

        RadioGroup radioGroup = dialogView.findViewById(R.id.dialog_audience_radio_group);
        RadioButton radioOff = dialogView.findViewById(R.id.radio_off);
        RadioButton radioAll = dialogView.findViewById(R.id.radio_all);
        RadioButton radioCcs = dialogView.findViewById(R.id.radio_ccs);
        RadioButton radioBetween = dialogView.findViewById(R.id.radio_between);
        RadioButton radioStudy = dialogView.findViewById(R.id.radio_study);
        View cancelBtn = dialogView.findViewById(R.id.dialog_audience_cancel);
        View confirmBtn = dialogView.findViewById(R.id.dialog_audience_confirm);

        if ("All My Groups".equals(selectedAudience)) {
            radioAll.setChecked(true);
        } else if ("CCS Friends only".equals(selectedAudience)) {
            radioCcs.setChecked(true);
        } else if ("Between Classes only".equals(selectedAudience)) {
            radioBetween.setChecked(true);
        } else if ("Study Squad only".equals(selectedAudience)) {
            radioStudy.setChecked(true);
        } else {
            radioOff.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        confirmBtn.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_all) {
                selectedAudience = "All My Groups";
            } else if (selectedId == R.id.radio_ccs) {
                selectedAudience = "CCS Friends only";
            } else if (selectedId == R.id.radio_between) {
                selectedAudience = "Between Classes only";
            } else if (selectedId == R.id.radio_study) {
                selectedAudience = "Study Squad only";
            } else {
                selectedAudience = "Off (Private)";
            }

            isLocationSharingOn = !"Off (Private)".equals(selectedAudience);
            updateLocationSharingUI();
            showMessage(getString(R.string.groups_sharing_updated, selectedAudience));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_group, null);

        EditText input = dialogView.findViewById(R.id.dialog_create_input);
        View cancelBtn = dialogView.findViewById(R.id.dialog_create_cancel);
        View submitBtn = dialogView.findViewById(R.id.dialog_create_submit);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        submitBtn.setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                showMessage(getString(R.string.groups_error_empty_group_name));
                return;
            }

            GroupItem newGroup = new GroupItem(
                    "group_" + System.currentTimeMillis(),
                    name,
                    true,
                    1,
                    "1 member · Updated just now",
                    Collections.singletonList("YOU"),
                    "",
                    "1 member on campus"
            );

            groupList.add(newGroup);
            renderGroupsList();
            showMessage(getString(R.string.groups_created_success, name));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showJoinWithCodeDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_join_group, null);

        EditText input = dialogView.findViewById(R.id.dialog_join_input);
        View cancelBtn = dialogView.findViewById(R.id.dialog_join_cancel);
        View submitBtn = dialogView.findViewById(R.id.dialog_join_submit);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        submitBtn.setOnClickListener(v -> {
            String code = input.getText().toString().trim();
            if (code.length() != 6) {
                showMessage(getString(R.string.groups_error_invalid_code));
                return;
            }

            showMessage(getString(R.string.groups_join_submitted, code));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateLocationSharingUI() {
        if (sharingStatusTitle == null || sharingStatusSubtitle == null || sharingStatusBadge == null) {
            return;
        }

        if (isLocationSharingOn) {
            sharingStatusTitle.setText(R.string.groups_title_location_sharing_on);
            sharingStatusSubtitle.setText(getString(R.string.groups_subtitle_location_sharing_on, selectedAudience));
            sharingStatusBadge.setText(R.string.groups_badge_on);
            sharingStatusBadge.setTextColor(requireContext().getColor(R.color.accent_teal));
        } else {
            sharingStatusTitle.setText(R.string.groups_title_location_sharing_off);
            sharingStatusSubtitle.setText(R.string.groups_subtitle_location_sharing_off);
            sharingStatusBadge.setText(R.string.groups_badge_off);
            sharingStatusBadge.setTextColor(requireContext().getColor(R.color.text_secondary));
        }
    }

    private void renderGroupsList() {
        if (groupsListContainer == null) {
            return;
        }

        groupsListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (GroupItem group : groupList) {
            View cardView = inflater.inflate(R.layout.item_group, groupsListContainer, false);

            TextView nameView = cardView.findViewById(R.id.group_name);
            TextView privacyBadgeView = cardView.findViewById(R.id.group_privacy_badge);
            TextView statusLineView = cardView.findViewById(R.id.group_status_line);
            TextView avatar1View = cardView.findViewById(R.id.avatar_1);
            TextView avatar2View = cardView.findViewById(R.id.avatar_2);
            TextView avatar3View = cardView.findViewById(R.id.avatar_3);
            TextView avatarOverflowView = cardView.findViewById(R.id.avatar_overflow);
            TextView presenceSummaryView = cardView.findViewById(R.id.group_presence_summary);

            nameView.setText(group.getName());
            privacyBadgeView.setVisibility(group.isPrivate() ? View.VISIBLE : View.GONE);
            statusLineView.setText(group.getUpdateStatus());
            presenceSummaryView.setText(group.getPresenceSummary());

            List<String> initials = group.getMemberInitials();
            if (initials != null && initials.size() >= 3) {
                avatar1View.setText(initials.get(0));
                avatar2View.setText(initials.get(1));
                avatar3View.setText(initials.get(2));
                avatar1View.setVisibility(View.VISIBLE);
                avatar2View.setVisibility(View.VISIBLE);
                avatar3View.setVisibility(View.VISIBLE);
            } else if (initials != null) {
                avatar1View.setText(!initials.isEmpty() ? initials.get(0) : "");
                avatar2View.setText(initials.size() > 1 ? initials.get(1) : "");
                avatar1View.setVisibility(!initials.isEmpty() ? View.VISIBLE : View.GONE);
                avatar2View.setVisibility(initials.size() > 1 ? View.VISIBLE : View.GONE);
                avatar3View.setVisibility(View.GONE);
            }

            if (group.getOverflowInitialsCount() != null && !group.getOverflowInitialsCount().isEmpty()) {
                avatarOverflowView.setText(group.getOverflowInitialsCount());
                avatarOverflowView.setVisibility(View.VISIBLE);
            } else {
                avatarOverflowView.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v ->
                    showMessage(getString(R.string.groups_selected_feedback, group.getName())));

            groupsListContainer.addView(cardView);
        }
    }

    private void showMessage(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
