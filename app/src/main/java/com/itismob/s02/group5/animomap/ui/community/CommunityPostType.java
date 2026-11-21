package com.itismob.s02.group5.animomap.ui.community;

import com.itismob.s02.group5.animomap.R;

enum CommunityPostType {
    POI_RECOMMENDATION(R.string.community_type_poi, R.id.create_post_type_poi),
    CAMPUS_UPDATES(R.string.community_type_updates, R.id.create_post_type_updates),
    CAMPUS_TIPS(R.string.community_type_tips, R.id.create_post_type_tips),
    GENERAL_DISCUSSION(R.string.community_type_discussion, R.id.create_post_type_discussion);

    final int labelRes;
    final int chipId;

    CommunityPostType(int labelRes, int chipId) {
        this.labelRes = labelRes;
        this.chipId = chipId;
    }

    static CommunityPostType fromChipId(int chipId) {
        for (CommunityPostType type : values()) {
            if (type.chipId == chipId) return type;
        }
        return null;
    }
}
