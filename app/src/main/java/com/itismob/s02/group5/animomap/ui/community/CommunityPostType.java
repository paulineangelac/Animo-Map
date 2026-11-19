package com.itismob.s02.group5.animomap.ui.community;

import com.itismob.s02.group5.animomap.R;

enum CommunityPostType {
    POI_RECOMMENDATION(R.string.community_type_poi),
    CAMPUS_UPDATES(R.string.community_type_updates),
    CAMPUS_TIPS(R.string.community_type_tips),
    GENERAL_DISCUSSION(R.string.community_type_discussion);

    final int labelRes;

    CommunityPostType(int labelRes) {
        this.labelRes = labelRes;
    }
}
