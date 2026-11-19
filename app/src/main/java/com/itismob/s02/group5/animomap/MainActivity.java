package com.itismob.s02.group5.animomap;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itismob.s02.group5.animomap.ui.community.CommunityFragment;
import com.itismob.s02.group5.animomap.ui.groups.GroupsFragment;
import com.itismob.s02.group5.animomap.ui.map.MapFragment;
import com.itismob.s02.group5.animomap.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_MAP = "TAG_MAP";
    private static final String TAG_COMMUNITY = "TAG_COMMUNITY";
    private static final String TAG_GROUPS = "TAG_GROUPS";
    private static final String TAG_PROFILE = "TAG_PROFILE";

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        FragmentManager fragmentManager = getSupportFragmentManager();
        int selectedItem = R.id.navigation_map;
        // The FragmentManager restores the tab instances and their hidden flags.
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            int itemId = bottomNav.getMenu().getItem(i).getItemId();
            Fragment fragment = fragmentManager.findFragmentByTag(getTabTag(itemId));
            if (fragment != null && !fragment.isHidden()) {
                activeFragment = fragment;
                selectedItem = itemId;
                break;
            }
        }
        if (activeFragment == null) {
            switchFragment(TAG_MAP);
        }
        bottomNav.setSelectedItemId(selectedItem);

        bottomNav.setOnItemSelectedListener(item -> {
            String tag = getTabTag(item.getItemId());
            if (tag == null || fragmentManager.isStateSaved()) return false;
            switchFragment(tag);
            return true;
        });
    }

    private String getTabTag(int itemId) {
        if (itemId == R.id.navigation_map) return TAG_MAP;
        if (itemId == R.id.navigation_community) return TAG_COMMUNITY;
        if (itemId == R.id.navigation_groups) return TAG_GROUPS;
        if (itemId == R.id.navigation_profile) return TAG_PROFILE;
        return null;
    }

    private void switchFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment target = fragmentManager.findFragmentByTag(tag);
        if (target != null && target == activeFragment) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction().setReorderingAllowed(true);
        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }
        if (target == null) {
            switch (tag) {
                case TAG_COMMUNITY:
                    target = new CommunityFragment();
                    break;
                case TAG_GROUPS:
                    target = new GroupsFragment();
                    break;
                case TAG_PROFILE:
                    target = new ProfileFragment();
                    break;
                default:
                    target = new MapFragment();
                    break;
            }
            transaction.add(R.id.fragment_container, target, tag);
        } else {
            transaction.show(target);
        }
        // Finish the add before another selection looks up the same tag.
        transaction.commitNow();
        activeFragment = target;
    }
}
