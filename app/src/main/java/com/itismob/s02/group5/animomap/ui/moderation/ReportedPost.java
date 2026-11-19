package com.itismob.s02.group5.animomap.ui.moderation;

/**
 * A community post that students reported, waiting for a moderator to act on it.
 */
public class ReportedPost {

    public static final String STATUS_OPEN = "OPEN";          // waiting for a moderator
    public static final String STATUS_KEPT = "KEPT";          // report dismissed, the post stays up
    public static final String STATUS_REMOVED = "REMOVED";    // the post was taken down

    private final String postTitle;
    private final String authorName;
    private final String reason;
    private final int reportCount;
    private String status;

    public ReportedPost(String postTitle, String authorName, String reason, int reportCount) {
        this.postTitle = postTitle;
        this.authorName = authorName;
        this.reason = reason;
        this.reportCount = reportCount;
        this.status = STATUS_OPEN;   // every report starts open
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getReason() {
        return reason;
    }

    public int getReportCount() {
        return reportCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOpen() {
        return STATUS_OPEN.equals(status);
    }
}