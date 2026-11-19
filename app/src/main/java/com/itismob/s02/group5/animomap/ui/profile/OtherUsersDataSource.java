package com.itismob.s02.group5.animomap.ui.profile;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.community.CommunityFeedDataSource;
import com.itismob.s02.group5.animomap.ui.community.CommunityPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sample profiles for the students who posted in the Community feed.
 * Prototype: Firebase will load real profiles and posts in MCO3.
 */
public final class OtherUsersDataSource {

    private OtherUsersDataSource() {
    }

    /*
     * The sample students. Names match the post authors in CommunityFeedDataSource.
     */
    private static List<OtherUser> getUsers() {
        List<OtherUser> users = new ArrayList<>();
        users.add(new OtherUser("Mika S.", "mika_s", "Always hunting for quiet study spots.",
                "College of Computer Studies (CCS)", "BS Computer Science",
                R.drawable.community_avatar_placeholder));
        users.add(new OtherUser("Bea R.", "bea_r", "I know where every charging outlet on campus is.",
                "College of Science (COS)", "BS Biology", 0));
        users.add(new OtherUser("Luis D.", "luis_d", "Usually in Goks between classes.",
                "Ramon V. del Rosario College of Business (RVR-COB)", "BS Accountancy", 0));
        users.add(new OtherUser("Ana M.", "ana_m", "Coffee, books, and quiet corners.",
                "College of Liberal Arts (CLA)", "BS Psychology", 0));
        // Carlo hasn't added a bio or school details, so those stay hidden on his profile
        users.add(new OtherUser("Carlo P.", "carlo_p", "", "", "", 0));
        users.add(new OtherUser("Nina T.", "nina_t", "Sharing small campus finds.",
                "College of Computer Studies (CCS)", "BS Information Systems", 0));
        return users;
    }

    /*
     * FIND: the student with this name. If they aren't in the sample list
     * (for example, a comment author added later), a basic profile is made from the name.
     */
    public static OtherUser findByName(String name) {
        for (OtherUser user : getUsers()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return new OtherUser(name, makeUsername(name), "", "", "", 0);
    }

    /*
     * POSTS: this student's posts from the Community feed, newest first.
     * They have no status badge, because only the poster can see their review status.
     */
    public static List<MyPost> getPostsBy(String name) {
        List<MyPost> posts = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (CommunityPost post : CommunityFeedDataSource.getSamplePosts()) {
            if (post.getAuthorName().equals(name)) {
                String place = TextUtils.isEmpty(post.getLocationName())
                        ? "Community post" : post.getLocationName();
                // Example: "12 minutes ago"
                String postedAgo = DateUtils.getRelativeTimeSpanString(
                        post.getCreatedAtMillis(), now, DateUtils.MINUTE_IN_MILLIS).toString();
                posts.add(new MyPost(post.getTitle(), place, postedAgo, MyPost.STATUS_NONE));
            }
        }
        return posts;
    }

    // ---------- Helpers ----------

    // Makes a username from a name. Example: "Juan D." becomes "juan_d"
    private static String makeUsername(String name) {
        String username = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        return username.replaceAll("^_+|_+$", "");   // remove "_" at the start or end
    }
}