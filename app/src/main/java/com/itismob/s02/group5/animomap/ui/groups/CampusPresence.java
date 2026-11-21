package com.itismob.s02.group5.animomap.ui.groups;

import android.content.Context;
import android.text.format.DateUtils;

import com.itismob.s02.group5.animomap.R;

/**
 * What's shared with a members group: the campus building they are in and when it was last updated.
 * It never holds an exact room, floor, route, or location history.
 */
public class CampusPresence {

    private final String buildingName;
    private final long updatedAtMillis;

    public CampusPresence(String buildingName, long updatedAtMillis) {
        this.buildingName = buildingName;
        this.updatedAtMillis = updatedAtMillis;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    /*
     * How long ago it was updated. E.g: "just now", "5 minutes ago", "2 hours ago".
     */
    public String getUpdatedAgo(Context context) {
        long now = System.currentTimeMillis();
        if (now - updatedAtMillis < DateUtils.MINUTE_IN_MILLIS) {
            return context.getString(R.string.groups_updated_just_now);
        }
        return DateUtils.getRelativeTimeSpanString(
                updatedAtMillis, now, DateUtils.MINUTE_IN_MILLIS).toString();
    }
}
