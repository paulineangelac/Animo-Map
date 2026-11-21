package com.itismob.s02.group5.animomap.ui.groups;

/**
 * One member of a group.
 * Only the name and username are saved here. Profile pictures and other public details are
 * looked up when the member list is shown, so they always match the student's current profile.
 */
public class GroupMember {

    private final String name;       // display name, e.g. "Mika S."
    private final String username;   // saved without the "@"
    private final boolean me;        // true for the logged-in student

    public GroupMember(String name, String username, boolean me) {
        this.name = name;
        this.username = username;
        this.me = me;
    }

    /*
     * The logged-in student. Their name and username come from UserProfile when shown,
     * so they stay correct after the student edits their profile.
     */
    public static GroupMember createMe() {
        return new GroupMember("", "", true);
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public boolean isMe() {
        return me;
    }
}
