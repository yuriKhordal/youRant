package com.yurikh.yourant.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.yurikh.yourant.Helper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.Profile;
import com.yurikh.yourant.network.API;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executors;

public class ProfileActivity extends YouRantActivity {
    public static final String INTENT_USER_ID = "user_id";

    long id;
    final MutableLiveData<Profile> user = new MutableLiveData<>();

    ProgressBar throbber;
    ImageView img_avatar;
    TextView lbl_about, lbl_create_time, lbl_location, lbl_skills, lbl_github, lbl_website;
    TabLayout lyt_tabs;
    TabLayout.Tab tab_rants, tab_comments, tab_upvoted, tab_favorites;
    ViewPager2 viewPager;


    public static Intent makeIntent(Context ctx, long user_id) {
        return new Intent(ctx, ProfileActivity.class)
                .putExtra(INTENT_USER_ID, user_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        throbber = findViewById(R.id.throbber);
        img_avatar = findViewById(R.id.img_avatar);
        lbl_about = findViewById(R.id.lbl_about);
        lbl_create_time = findViewById(R.id.lbl_create_time);
        lbl_location = findViewById(R.id.lbl_location);
        lbl_skills = findViewById(R.id.lbl_skills);
        lbl_github = findViewById(R.id.lbl_github);
        lbl_website = findViewById(R.id.lbl_website);
        lyt_tabs = findViewById(R.id.lyt_tabs);
        tab_rants = lyt_tabs.getTabAt(0);
        tab_comments = lyt_tabs.getTabAt(1);
        tab_upvoted = lyt_tabs.getTabAt(2);
        tab_favorites = lyt_tabs.getTabAt(3);
        viewPager = findViewById(R.id.viewPager);

        setupTabs();

        id = getIntent().getLongExtra(INTENT_USER_ID, -1);
        if (id == -1) {
            // TODO: show error
        }

        user.observe(this, this::displayUser);
        var executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                var profile = API.getFlatProfile(id);
                user.postValue(profile);
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> Helper.displayErrorAndFinish(this, "Error: Failed to get user", e));
            }
        });
        executor.shutdown();
    }

    private void setupTabs() {
//        viewPager.setAdapter(new ProfileTabsAdapter(this));
//        new TabLayoutMediator(lyt_tabs, viewPager, (tab, position) -> {
//            var text = lyt_tabs.getTabAt(position).getText();
//            tab.setText((text == null) ? "WTF" : text);
//        }).attach();
    }

    private void displayUser(Profile profile) {
        YouRantApp app = YouRantApp.getInstance();

        throbber.setVisibility(TextView.GONE);
        if (profile.score > 0)
            setTitle(profile.username + " (+" + profile.score + ")");
        else
            setTitle(profile.username + " (" + profile.score + ")");

        String about = profile.about;
        String create_time = getString(R.string.profile_created_time) + " " + Helper.prettifyTime(profile.created_time);
        String location = getString(R.string.profile_location) + " " + profile.location;
        String skills = getString(R.string.profile_skills) + " " + profile.skills;
        String github = getString(R.string.profile_github) + " " + profile.github;
        String website = getString(R.string.profile_website) + " " + profile.website;

        lbl_about.setText(about);
        lbl_create_time.setText(create_time);
        lbl_location.setText(location);
        lbl_skills.setText(skills);
        lbl_github.setText(github);
        lbl_website.setText(website);

        img_avatar.setBackgroundColor(Color.parseColor("#" + profile.avatar_background));
        if (!profile.avatar_image.isBlank()) {
            app.avatars().getAsync(profile.avatar_image, this, bitmap -> {
                img_avatar.setImageBitmap(bitmap);
                img_avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            });
        }
    }

    private static class ProfileTabsAdapter extends FragmentStateAdapter {
        public ProfileTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new Fragment();
                case 1: return new Fragment();
                case 2: return new Fragment();
                case 3: return new Fragment();
                default: throw new RuntimeException("Impossible");
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}