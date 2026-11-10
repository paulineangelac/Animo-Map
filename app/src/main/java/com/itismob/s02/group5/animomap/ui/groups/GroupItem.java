package com.itismob.s02.group5.animomap.ui.groups;

import java.util.List;

/**
 * Data model representing a group and its campus presence summary.
 */
public class GroupItem {

    private final String id;
    private final String name;
    private final boolean isPrivate;
    private final int memberCount;
    private final String updateStatus;
    private final List<String> memberInitials;
    private final String overflowInitialsCount;
    private final String presenceSummary;

    public GroupItem(String id, String name, boolean isPrivate, int memberCount,
                     String updateStatus, List<String> memberInitials,
                     String overflowInitialsCount, String presenceSummary) {
        this.id = id;
        this.name = name;
        this.isPrivate = isPrivate;
        this.memberCount = memberCount;
        this.updateStatus = updateStatus;
        this.memberInitials = memberInitials;
        this.overflowInitialsCount = overflowInitialsCount;
        this.presenceSummary = presenceSummary;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public List<String> getMemberInitials() {
        return memberInitials;
    }

    public String getOverflowInitialsCount() {
        return overflowInitialsCount;
    }

    public String getPresenceSummary() {
        return presenceSummary;
    }
}
