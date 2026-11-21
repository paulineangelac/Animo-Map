package com.itismob.s02.group5.animomap.ui.groups;

import java.util.ArrayList;
import java.util.List;

/**
 * A private (invite-only) group, also called a circle.
 * Students can only join it with its invite code.
 */
public class GroupItem {

    private final String id;
    private final String name;
    private final String description;   // "" when the creator left it empty
    private final String inviteCode;    // 6 letters or numbers, e.g. "K7Q2MX"
    private final String createdBy;     // display name of the creator, or "" if it was the logged-in student
    private final long createdAtMillis;
    private final List<GroupMember> members;

    public GroupItem(String id, String name, String description, String inviteCode,
                     String createdBy, long createdAtMillis, List<GroupMember> members) {
        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
        this.inviteCode = inviteCode;
        this.createdBy = createdBy == null ? "" : createdBy;
        this.createdAtMillis = createdAtMillis;
        this.members = new ArrayList<>(members);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    /** A copy of the member list, so screens can't change the group by accident. */
    public List<GroupMember> getMembers() {
        return new ArrayList<>(members);
    }

    public int getMemberCount() {
        return members.size();
    }

    /** True when the logged-in student is one of the members. */
    public boolean hasMe() {
        for (GroupMember member : members) {
            if (member.isMe()) {
                return true;
            }
        }
        return false;
    }

    /** Adds the logged-in student. Only GroupsDataSource calls this, when joining with a code. */
    void addMe() {
        if (!hasMe()) {
            members.add(GroupMember.createMe());
        }
    }
}
