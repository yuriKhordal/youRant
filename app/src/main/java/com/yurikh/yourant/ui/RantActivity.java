package com.yurikh.yourant.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yurikh.yourant.Helper;
import com.yurikh.yourant.R;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.Comment;
import com.yurikh.yourant.model.Rant;
import com.yurikh.yourant.model.RantCategory;
import com.yurikh.yourant.model.VoteState;
import com.yurikh.yourant.network.API;

import org.json.JSONException;

import java.io.IOException;

public class RantActivity extends YouRantActivity {
    private static final String INTENT_RANT_ID = "rant_id";

    ProgressBar throbber;
    TextView lbl_username, lbl_content, lbl_time, lbl_tags, lbl_score;
    ImageView img_avatar, img_attach;
    Button btn_upvote, btn_downvote, btn_comment;
    LinearLayout lyt_comments;

    long rant_id;
    Rant rant;

    public static Intent makeIntent(Context ctx, long rant_id) {
        return new Intent(ctx, RantActivity.class)
                .putExtra(INTENT_RANT_ID, rant_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        throbber = findViewById(R.id.throbber);
        lbl_username = findViewById(R.id.lbl_username);
        lbl_content = findViewById(R.id.lbl_content);
        lbl_time = findViewById(R.id.lbl_time);
        lbl_tags = findViewById(R.id.lbl_tags);
        lbl_score = findViewById(R.id.lbl_score);
        img_avatar = findViewById(R.id.img_avatar);
        img_attach = findViewById(R.id.img_attach);
        btn_upvote = findViewById(R.id.btn_upvote);
        btn_downvote = findViewById(R.id.btn_downvote);
        btn_comment = findViewById(R.id.btn_comment);

        lyt_comments = findViewById(R.id.lyt_comments);
    }

    @Override
    protected void onResume() {
        super.onResume();

        rant_id = getIntent().getLongExtra(INTENT_RANT_ID, -1);
        if (rant_id == -1) {
            Toast.makeText(this, "Error: Rant page opened incorrectly, go back and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        lbl_content.setMovementMethod(LinkMovementMethod.getInstance());
        lbl_content.setLinksClickable(true);

        // Technically this could be TOCTTU
        if (rant == null) {
            loadRant();
        } else {
            displayRant();
        }
    }

    @Override
    protected void onBtnToolbarRefreshClick(View view) {
        super.onBtnToolbarRefreshClick(view);

        loadRant();
    }

    public void upvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        // TODO: Upvote
    }

    public void downvoteRant(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        // TODO: Upvote
    }


    public void upvoteComment(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        // TODO: Upvote
    }


    public void downvoteComment(View view) {
        if (!YouRantApp.getInstance().isLoggedIn()) {
            Toast.makeText(this, "Log in to upvote rants!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Not Implemented yet!", Toast.LENGTH_SHORT).show();
        // TODO: Upvote
    }

    public void onBtnCommentClick(View view) {
        final EditText txt_comment = new EditText(this);
        txt_comment.setHint("Comment");
        txt_comment.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        txt_comment.setMinLines(5);

        new AlertDialog.Builder(this)
                .setTitle("New Comment")
                .setView(txt_comment)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Post", (dialogInterface, i) -> {
                    postComment(txt_comment.getText().toString());
                }).show();
    }

    private void postComment(String comment) {
        ProgressBar throbber = findViewById(R.id.comment_throbber);
        throbber.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                API.postComment(rant.id, comment);
                runOnUiThread(() -> {
                    throbber.setVisibility(View.GONE);
                    loadRant();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Helper.displayError(this, e));
            } finally {
                runOnUiThread(() -> throbber.setVisibility(View.GONE));
            }
        }).start();
    }

    private void loadRant() {
        throbber.setVisibility(TextView.VISIBLE);

        new Thread(() -> {
            try {
                rant = API.getRant(rant_id);
                runOnUiThread(this::displayRant);
            } catch (JSONException | IOException e) {
                runOnUiThread(() -> Helper.displayError(this, e));
            } finally {
                runOnUiThread(() -> throbber.setVisibility(View.GONE));
            }
        }).start();
    }

    private void displayRant() {
        lyt_comments.removeAllViews();

        String title = RantCategory.fromValue(rant.rc).name;
        String username = "@" + rant.user.username;
        String text = rant.text;
        for (int i = rant.links.length-1; i >= 0 ; i--)
            text = rant.links[i].embedIntoString(text);
        text = text.replaceAll("\n", "<br>");

        setTitle(title);
        lbl_username.setText(username);
        lbl_username.setTextColor(Color.parseColor("#" + rant.user.avatar_background));
        lbl_content.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        lbl_time.setText(Helper.prettifyTime(rant.created_time));
        lbl_tags.setText(String.join(", ", rant.tags));
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

        for (Comment comment : rant.comments)
            addComment(comment);
    }

    private void addComment(Comment comment) {
        View vComment = LayoutInflater.from(lyt_comments.getContext())
                .inflate(R.layout.view_comment, lyt_comments, false);

        TextView lbl_username = vComment.findViewById(R.id.lbl_username);
        TextView lbl_content =  vComment.findViewById(R.id.lbl_content);
        TextView lbl_time =     vComment.findViewById(R.id.lbl_time);
        TextView lbl_score =    vComment.findViewById(R.id.lbl_score);
        ImageView img_attach =  vComment.findViewById(R.id.img_attach);
        ImageView img_avatar =  vComment.findViewById(R.id.img_avatar);
        Button btn_upvote =     vComment.findViewById(R.id.btn_upvote);
        Button btn_downvote =   vComment.findViewById(R.id.btn_downvote);

        String body = comment.body;
        for (int i = comment.links.length-1; i >= 0 ; i--)
            body = comment.links[i].embedIntoString(body);
        body = body.replaceAll("\n", "<br>");

        lbl_username.setText("@" + comment.user.username);
        lbl_username.setTextColor(Color.parseColor("#" + comment.user.avatar_background));
        lbl_content.setMovementMethod(LinkMovementMethod.getInstance());
        lbl_content.setLinksClickable(true);
        lbl_content.setText(Html.fromHtml(body, Html.FROM_HTML_MODE_COMPACT));
        lbl_time.setText(Helper.prettifyTime(comment.created_time));
        lbl_score.setText(comment.score + "");
        img_avatar.setBackgroundColor(Color.parseColor("#" + comment.user.avatar_background));
        if (comment.attached_image != null) {
            // Load attached image
        }

        if (comment.vote_state == VoteState.Upvoted) {
            btn_upvote.setTextColor(Color.WHITE);
        } else if (comment.vote_state == VoteState.Downvoted) {
            btn_downvote.setTextColor(Color.WHITE);
        } else if (comment.vote_state == VoteState.NoVoteAllowed) {
            btn_upvote.setEnabled(false);
            btn_downvote.setEnabled(false);
        }

        btn_upvote.setOnClickListener(this::upvoteComment);
        btn_downvote.setOnClickListener(this::downvoteComment);
        vComment.setTag(comment);

        lyt_comments.addView(vComment);
    }
}