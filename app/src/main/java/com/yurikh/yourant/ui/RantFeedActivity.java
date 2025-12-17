package com.yurikh.yourant.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.yourant.Helper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.FeedRant;
import com.yurikh.yourant.model.RantFeed;
import com.yurikh.yourant.model.VoteState;
import com.yurikh.yourant.network.API;
import com.yurikh.yourant.network.RantSort;

import org.json.JSONException;

import java.io.IOException;

public class RantFeedActivity extends YouRantActivity {
    LinearLayout lyt_rants;
    ProgressBar throbber;
    Button btn_post;
    FeedRant[] rants = null;
    long lastLoadTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rant_feed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setTitle(R.string.toolbar_title_rants_feed);
        lyt_rants = findViewById(R.id.lyt_rants);
        throbber = findViewById(R.id.throbber);
        btn_post = findViewById(R.id.btn_post);

        loadRants();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (YouRantApp.getInstance().isLoggedIn()) {
            btn_post.setVisibility(View.VISIBLE);
        } else {
            btn_post.setVisibility(View.GONE);
        }

        final long timeSinceLoad = System.currentTimeMillis() - lastLoadTime;
        final long MINUTE = 60 * 1000L;
        if (rants == null || timeSinceLoad > 5 * MINUTE ) {
            loadRants();
        } else {
            for (final FeedRant rant : rants) {
                addRant(rant);
            }
        }
    }

    @Override
    protected void onBtnToolbarRefreshClick(View view) {
        loadRants();
    }

    protected void loadRants() {
        lastLoadTime = System.currentTimeMillis();
        lyt_rants.removeAllViews();
        lyt_rants.addView(throbber);
        throbber.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                RantFeed feed = API.getRantsFeed(RantSort.recent, 0);
                rants = feed.rants;
                for (FeedRant rant : feed.rants) {
                    runOnUiThread(() -> addRant(rant));
                }
            } catch (JSONException | IOException e) {
                runOnUiThread(() -> Helper.displayError(this, e));
            } finally {
                runOnUiThread(() -> throbber.setVisibility(View.GONE));
            }
        }).start();
    }

    private void addRant(FeedRant rant) {
        View viewRant = LayoutInflater.from(lyt_rants.getContext())
                .inflate(R.layout.view_feed_rant, lyt_rants, false);

        TextView lbl_username = viewRant.findViewById(R.id.lbl_username);
        TextView lbl_content =  viewRant.findViewById(R.id.lbl_content);
        TextView lbl_time =     viewRant.findViewById(R.id.lbl_time);
        TextView lbl_tags =     viewRant.findViewById(R.id.lbl_tags);
        TextView lbl_score =    viewRant.findViewById(R.id.lbl_score);
        ImageView img_attach =  viewRant.findViewById(R.id.img_attach);
        ImageView img_avatar =  viewRant.findViewById(R.id.img_avatar);
        Button btn_upvote =     viewRant.findViewById(R.id.btn_upvote);
        Button btn_downvote =   viewRant.findViewById(R.id.btn_downvote);


        lbl_username.setText("@" + rant.user.username);
        lbl_username.setTextColor(Color.parseColor("#" + rant.user.avatar_background));
        lbl_content.setText(rant.text);
        lbl_time.setText(Helper.prettifyTime(rant.created_time));
        lbl_tags.setText("tags: " + String.join(", ", rant.tags));
        lbl_score.setText(rant.score + "");

        if (rant.vote_state == VoteState.Upvoted) {
            btn_upvote.setTextColor(Color.WHITE);
        } else if (rant.vote_state == VoteState.Downvoted) {
            btn_downvote.setTextColor(Color.WHITE);
        } else if (rant.vote_state == VoteState.NoVoteAllowed) {
            btn_upvote.setEnabled(false);
            btn_downvote.setEnabled(false);
        }

        img_avatar.setBackgroundColor(Color.parseColor("#" + rant.user.avatar_background));
        if (rant.attached_image != null) {
            // Load attached image
        }


        viewRant.setTag(rant);
        viewRant.setOnClickListener(this::onRantClick);
        btn_upvote.setTag(rant.id);
        btn_upvote.setOnClickListener(this::upvoteRant);
        btn_downvote.setTag(rant.id);
        btn_downvote.setOnClickListener(this::downvoteRant);


        lyt_rants.addView(viewRant);
    }

    private void upvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        long rantId = (long)view.getTag();

//        view.setEnabled(false);
//        new Thread(() -> {
//            try {
//                API.postRantVote(rantId, state);
//                runOnUiThread(() -> {
//                    btn.setEnabled(true);
//                    btn.setTag(1, !upvoted);
//                    if (!upvoted) {
//                        btn.setTextColor(Color.WHITE);
//                    }
//                    else btn.setTextColor(Color.BLACK);
//                });
//
//            } catch (Exception e) {
//                runOnUiThread(() -> Helper.displayError(this, e));
//            }
//        }).start();
    }

    private void downvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to downvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        long rantId = (long)view.getTag();
        // TODO: Upvote
    }

    private void onRantClick(View view) {
        FeedRant rant = (FeedRant) view.getTag();
        startActivity(RantActivity.makeIntent(this, rant.id));
    }

    public void onBtnPostClick(View view) {
        startActivity(NewRantActivity.makeIntent(this));
    }
}