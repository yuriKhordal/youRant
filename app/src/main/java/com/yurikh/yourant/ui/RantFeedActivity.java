package com.yurikh.yourant.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.yurikh.yourant.NotificationHelper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.cache.ImageCache;
import com.yurikh.yourant.model.FeedRant;
import com.yurikh.yourant.model.RantFeed;
import com.yurikh.yourant.model.DownvoteReason;
import com.yurikh.yourant.model.VoteState;
import com.yurikh.yourant.network.API;
import com.yurikh.yourant.network.RantSort;
import com.yurikh.yourant.viewmodel.RantVM;

import org.json.JSONException;

import java.io.IOException;

public class RantFeedActivity extends YouRantActivity {
    RantVM vm;
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
        vm = new RantVM(this);
        lyt_rants = findViewById(R.id.lyt_rants);
        throbber = findViewById(R.id.throbber);
        btn_post = findViewById(R.id.btn_post);

        NotificationHelper.requestPermission(this);
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
            lyt_rants.removeAllViews();
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
                runOnUiThread(() -> {
                    for (final FeedRant rant : feed.rants) {
                        addRant(rant);
                    }
                });
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
        ProgressBar trb_attach =  viewRant.findViewById(R.id.trb_attach);
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
            btn_upvote.setTextColor(Color.rgb(192, 0, 0));
            btn_upvote.setTypeface(btn_upvote.getTypeface(), Typeface.BOLD);
        } else if (rant.vote_state == VoteState.Downvoted) {
            btn_downvote.setTextColor(Color.rgb(192, 0, 0));
            btn_downvote.setTypeface(btn_downvote.getTypeface(), Typeface.BOLD);
        } else if (rant.vote_state == VoteState.NoVoteAllowed) {
            btn_upvote.setEnabled(false);
            btn_downvote.setEnabled(false);
        }

        img_avatar.setBackgroundColor(Color.parseColor("#" + rant.user.avatar_background));
        if (!rant.user.avatar_image.isEmpty())
            YouRantApp.getInstance().avatars().getAsync(rant.user.avatar_image, this, bitmap -> {
                img_avatar.setImageTintList(null);
                img_avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                img_avatar.setImageBitmap(bitmap);
            });
        if (rant.attached_image != null) {
            trb_attach.setVisibility(View.VISIBLE);
            ImageCache cache = YouRantApp.getInstance().images();
            cache.getAsync(rant.attached_image.url, this, bitmap -> {
                img_attach.setImageBitmap(bitmap);
                trb_attach.setVisibility(View.GONE);
            });
        }

        viewRant.setTag(rant);
        viewRant.setOnClickListener(this::onRantClick);
        btn_upvote.setOnClickListener(this::upvoteRant);
        btn_upvote.setTag(rant);
        btn_downvote.setOnClickListener(this::downvoteRant);
        btn_downvote.setTag(rant);


        lyt_rants.addView(viewRant);
    }

    private void upvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedRant rant = (FeedRant)view.getTag();
        view.setEnabled(false);
        if (rant.vote_state == VoteState.Upvoted) {
            voteRant(rant, VoteState.Unvoted, DownvoteReason.NoReasonUpvote);
        } else {
            voteRant(rant, VoteState.Upvoted, DownvoteReason.NoReasonUpvote);
        }
    }

    private void downvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to downvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedRant rant = (FeedRant)view.getTag();
        view.setEnabled(false);
        if (rant.vote_state == VoteState.Downvoted) {
            voteRant(rant, VoteState.Unvoted, DownvoteReason.NoReasonUpvote);
        } else {
            vm.downvoteAlert(
                    reason -> voteRant(rant, VoteState.Downvoted, reason),
                    () -> view.setEnabled(true)
            ).show();
        }
    }

    private void voteRant(FeedRant rant, VoteState state, DownvoteReason reason){
        int index = Helper.indexOf(rants, rant);

        new Thread(() -> {
            try {
                FeedRant voted = API.postRantVote(rant.id, state, reason);
                rants[index] = voted;
                runOnUiThread(() -> {
                    lyt_rants.removeAllViews();
                    for (final var r : rants)
                        addRant(r);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Helper.displayError(this, e));
            }
        }).start();
    }

    private void onRantClick(View view) {
        FeedRant rant = (FeedRant) view.getTag();
        startActivity(RantActivity.makeIntent(this, rant.id));
    }

    public void onBtnPostClick(View view) {
        startActivity(NewRantActivity.makeIntent(this));
    }
}