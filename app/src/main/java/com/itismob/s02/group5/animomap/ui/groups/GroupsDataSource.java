package com.itismob.s02.group5.animomap.ui.groups;


import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.map.CampusMapData;
import com.itismob.s02.group5.animomap.ui.map.MapPlace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Sample groups and campus presence for the prototype.
 * Everything lives in memory only (reset when the app closes). Firebase will store real groups
 * and presence across devices in MCO3, so the screens only talk to the methods of this class.
 *
 * Sample invite codes for groups the student hasn't joined yet (for testing "Join with code"):
 *   THS2BD  -> Thesis Buddies
 *   GREEN5  -> Archers Watch Party
 */
public final class GroupsDataSource {

    // Rules for names and invite codes

    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 40;
    public static final int DESCRIPTION_MAX_LENGTH = 120;
    public static final int CODE_LENGTH = 6;
    // Letters and numbers that are easy to read aloud. 0/O and 1/I are left out so they aren't mixed up.
    static final String CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // Who the student shares their building with

    public static final String AUDIENCE_OFF = "off";
    public static final String AUDIENCE_ALL = "all";
    // Any other audience value is the id of one group.

    // Every sample group, including ones the student can join with a code -w-
    private static final List<GroupItem> GROUPS = createSampleGroups();

    // The student's own sharing. Memory only, so it is reset when the app closes~
    private static String myAudience = AUDIENCE_OFF;
    private static String myBuildingName = "";
    private static long myUpdatedAtMillis = 0;

    // Sample presence for other students, as if they shared their building a few minutes ago -w-
    private static final Map<String, CampusPresence> SAMPLE_PRESENCE = createSamplePresence();

    private static final Random RANDOM = new Random();

    private GroupsDataSource() {
    }

    // reading groups

    /** The groups the logged-in student is in, most recently created or joined first. */
    public static List<GroupItem> getMyGroups() {
        List<GroupItem> mine = new ArrayList<>();
        for (GroupItem group : GROUPS) {
            if (group.hasMe()) {
                mine.add(group);
            }
        }
        return mine;
    }

    /** The group with this id, or null if it doesn't exist. */
    public static GroupItem findById(String groupId) {
        for (GroupItem group : GROUPS) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    /** The group with this invite code, or null if no group uses it. */
    public static GroupItem findByCode(String code) {
        String cleanCode = normalizeCode(code);
        for (GroupItem group : GROUPS) {
            if (group.getInviteCode().equals(cleanCode)) {
                return group;
            }
        }
        return null;
    }

    // creating and joining

    /*
     * create: makes a new invite-only group with the logged-in student as its only member.
     * Check the name with validateGroupName(...) and the description with
     * validateDescription(...) first.
     */
    public static GroupItem createGroup(String name, String description) {
                List<GroupMember> members = new ArrayList<>();
        members.add(GroupMember.createMe());

        GroupItem group = new GroupItem(
                "group_" + System.currentTimeMillis(),
                name.trim(),
                description == null ? "" : description.trim(),
                generateInviteCode(RANDOM, GROUPS),
                "",   // "" means the logged-in student created it
                System.currentTimeMillis(),
                members);

        GROUPS.add(0, group);   // newest first
        return group;
    }

    /*
     * join: adds the logged-in student to the group and moves it to the top of their list.
     * Find the group with findByCode(...) first.
     */
    public static void joinGroup(GroupItem group) {
                group.addMe();
        GROUPS.remove(group);
        GROUPS.add(0, group);
    }

    // validation

    /*
     * Checks a new group's name. Returns 0 when it's fine, otherwise the string resource of the error.
     * A student can't have two groups with the same name, so they can tell them apart.
     */
    public static int validateGroupName(String name, List<GroupItem> myGroups) {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty()) {
            return R.string.groups_error_empty_group_name;
        }
        if (cleanName.length() < NAME_MIN_LENGTH) {
            return R.string.groups_error_name_too_short;
        }
        if (cleanName.length() > NAME_MAX_LENGTH) {
            return R.string.groups_error_name_too_long;
        }
        for (GroupItem group : myGroups) {
            if (group.getName().equalsIgnoreCase(cleanName)) {
                return R.string.groups_error_name_taken;
            }
        }
        return 0;
    }

    /** Checks the optional description. Returns 0 when it's fine, otherwise the error's string resource. */
    public static int validateDescription(String description) {
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanDescription.length() > DESCRIPTION_MAX_LENGTH) {
            return R.string.groups_error_description_too_long;
        }
        return 0;
    }

    /*
     * Checks the shape of an invite code (not whether a group uses it).
     * Returns 0 when it's fine, otherwise the error's string resource.
     */
    public static int validateInviteCode(String code) {
        String cleanCode = normalizeCode(code);
        if (cleanCode.isEmpty()) {
            return R.string.groups_error_empty_code;
        }
        if (cleanCode.length() != CODE_LENGTH) {
            return R.string.groups_error_invalid_code;
        }
        for (int i = 0; i < cleanCode.length(); i++) {
            if (CODE_CHARACTERS.indexOf(cleanCode.charAt(i)) < 0) {
                return R.string.groups_error_invalid_code;
            }
        }
        return 0;
    }

    /** Removes spaces and dashes and uses capital letters. Example: " k7q-2mx " becomes "K7Q2MX". */
    public static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        return code.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** A random 6-character code that no other group is using. */
    static String generateInviteCode(Random random, List<GroupItem> existingGroups) {
        while (true) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
            }
            boolean used = false;
            for (GroupItem group : existingGroups) {
                if (group.getInviteCode().contentEquals(code)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                return code.toString();
            }
        }
    }

    // campus presence

    /** "off", "all", or the id of the one group the student shares with. */
    public static String getMyAudience() {
        return myAudience;
    }

    public static String getMyBuildingName() {
        return myBuildingName;
    }

    public static boolean isSharingOn() {
        return !AUDIENCE_OFF.equals(myAudience);
    }

    /** True when the student's building is visible to this group. */
    public static boolean isSharingWith(String groupId) {
        return AUDIENCE_ALL.equals(myAudience) || myAudience.equals(groupId);
    }

    /*
     * Turns the student's sharing on or off.
     * audience: AUDIENCE_OFF, AUDIENCE_ALL, or a group id. buildingName is ignored when turning it off.
     * Joining a group never calls this, so joining never turns sharing on by itself.
     */
    public static void setMySharing(String audience, String buildingName) {
        if (audience == null || AUDIENCE_OFF.equals(audience)) {
            myAudience = AUDIENCE_OFF;
            myBuildingName = "";
            myUpdatedAtMillis = 0;
        } else {
            myAudience = audience;
            myBuildingName = buildingName;
            myUpdatedAtMillis = System.currentTimeMillis();
        }
    }

    /*
     * What this member shares with this group, or null if they aren't sharing with it.
     * Prototype: the sample students share with every group they're in.
     */
    public static CampusPresence getPresence(GroupItem group, GroupMember member) {
        if (member.isMe()) {
            if (isSharingWith(group.getId())) {
                return new CampusPresence(myBuildingName, myUpdatedAtMillis);
            }
            return null;
        }
        return SAMPLE_PRESENCE.get(member.getUsername());
    }

    /** How many members of the group are sharing their building right now. */
    public static int countSharing(GroupItem group) {
        int count = 0;
        for (GroupMember member : group.getMembers()) {
            if (getPresence(group, member) != null) {
                count++;
            }
        }
        return count;
    }

    /** The newest presence update in the group, or null if no one is sharing. */
    public static CampusPresence getLatestPresence(GroupItem group) {
        CampusPresence latest = null;
        for (GroupMember member : group.getMembers()) {
            CampusPresence presence = getPresence(group, member);
            if (presence != null
                    && (latest == null || presence.getUpdatedAtMillis() > latest.getUpdatedAtMillis())) {
                latest = presence;
            }
        }
        return latest;
    }

    /** Names of the campus buildings a student can choose to share. */
    public static List<String> getCampusBuildingNames() {
        List<String> names = new ArrayList<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            // Buildings have their own marker. Classrooms are also BUILDING places but have no marker.
            if (place.getCategory() == MapPlace.Category.BUILDING && place.hasMapMarker()) {
                names.add(place.getName());
            }
        }
        return names;
    }

    // campus data

    /*
     * Sample members use the students in OtherUsersDataSource, so tapping them opens a full profile.
     */
    private static List<GroupItem> createSampleGroups() {
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;
        List<GroupItem> samples = new ArrayList<>();

        // Groups the student is already in
        samples.add(new GroupItem("group_ccs_friends", "CCS Friends",
                "Lunch plans and study breaks for CCS blockmates.", "CCS4FR", "Mika S.", now - 30 * day,
                Arrays.asList(sample("Mika S.", "mika_s"), sample("Nina T.", "nina_t"),
                        sample("Carlo P.", "carlo_p"), GroupMember.createMe())));

        samples.add(new GroupItem("group_between_classes", "Between Classes",
                "Find each other during breaks.", "BTW8CL", "Luis D.", now - 14 * day,
                Arrays.asList(sample("Luis D.", "luis_d"), sample("Ana M.", "ana_m"),
                        sample("Bea R.", "bea_r"), sample("Mika S.", "mika_s"), GroupMember.createMe())));

        samples.add(new GroupItem("group_study_squad", "Study Squad",
                "", "STD3SQ", "", now - 7 * day,
                Arrays.asList(GroupMember.createMe(), sample("Ana M.", "ana_m"), sample("Luis D.", "luis_d"))));

        // Groups the student can join with a code (see the codes at the top of this class)
        samples.add(new GroupItem("group_thesis_buddies", "Thesis Buddies",
                "Accountability group for thesis writing.", "THS2BD", "Nina T.", now - 3 * day,
                Arrays.asList(sample("Nina T.", "nina_t"), sample("Bea R.", "bea_r"))));

        samples.add(new GroupItem("group_archers_watch", "Archers Watch Party",
                "Meet up for UAAP games.", "GREEN5", "Luis D.", now - 2 * day,
                Arrays.asList(sample("Luis D.", "luis_d"), sample("Carlo P.", "carlo_p"),
                        sample("Ana M.", "ana_m"), sample("Mika S.", "mika_s"))));

        return new ArrayList<>(samples);
    }

    private static GroupMember sample(String name, String username) {
        return new GroupMember(name, username, false);
    }

    /*
     * Some sample students are sharing their building, as of a few minutes before the app opened.
     * Luis and Carlo aren't sharing, so their rows show "Not sharing their location".
     */
    private static Map<String, CampusPresence> createSamplePresence() {
        long now = System.currentTimeMillis();
        long minute = 60L * 1000;
        Map<String, CampusPresence> presence = new HashMap<>();
        presence.put("mika_s", new CampusPresence(buildingName("gokongwei"), now - 2 * minute));
        presence.put("nina_t", new CampusPresence(buildingName("andrew"), now - 5 * minute));
        presence.put("bea_r", new CampusPresence(buildingName(CampusMapData.HENRY_SY), now - 15 * minute));
        presence.put("ana_m", new CampusPresence(buildingName("velasco"), now - 38 * minute));
        return presence;
    }

    private static String buildingName(String placeId) {
        MapPlace place = CampusMapData.findPlace(placeId);
        return place == null ? placeId : place.getName();
    }
}
