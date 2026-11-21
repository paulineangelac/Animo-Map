package com.itismob.s02.group5.animomap.ui.community;

final class CommunityComment {
    final String authorName;
    final String username;
    final String initials;
    final int avatarResourceId;
    final String avatarUri;
    final String text;
    final long createdAtMillis;

    CommunityComment(String authorName, String username, String initials, int avatarResourceId,
                     String avatarUri, String text, long createdAtMillis) {
        this.authorName = authorName;
        this.username = username;
        this.initials = initials;
        this.avatarResourceId = avatarResourceId;
        this.avatarUri = avatarUri;
        this.text = text;
        this.createdAtMillis = createdAtMillis;
    }
}
