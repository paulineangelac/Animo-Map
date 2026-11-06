package com.itismob.s02.group5.animomap.ui.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local demonstration data for Groups.
 */
public final class GroupsDataSource {

    private GroupsDataSource() {
    }

    public static List<GroupItem> getSampleGroups() {
        List<GroupItem> groups = new ArrayList<>();

        groups.add(new GroupItem(
                "group_ccs_friends",
                "CCS Friends",
                true,
                5,
                "5 members · Updated just now",
                Arrays.asList("MS", "JP", "AK"),
                "+2",
                "3 members on campus"
        ));

        groups.add(new GroupItem(
                "group_between_classes",
                "Between Classes",
                true,
                8,
                "8 members · Sharing ends at 6:00 PM",
                Arrays.asList("RN", "EL", "CP"),
                "+5",
                "4 members on campus"
        ));

        groups.add(new GroupItem(
                "group_study_squad",
                "Study Squad",
                true,
                4,
                "4 members · No one sharing right now",
                Arrays.asList("AV", "TN", "LD"),
                "+1",
                "Last active 21 min ago"
        ));

        return groups;
    }
}
