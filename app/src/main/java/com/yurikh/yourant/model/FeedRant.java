package com.yurikh.yourant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/** Representation of a single rant as it appears in the main feed. */
public class FeedRant {
    public final long id;
    /** The actual content of the rant. */
    public final String text;
    /** The amount of upvotes(++) of the rant. */
    public final int score;
    /** A timestamp(unix epoch) of when the rant was created. */
    public final long created_time;
    public final AttachedImage attached_image;
    public final String[] tags;
    /** The vote(++/--/No) of the logged in user on the rant. */
    public final VoteState vote_state;
    public final boolean edited;
    /** If Collab, which type. */
    public final CollabType c_type;
    /** Collab type but as string. */
    public final String c_type_long;
    /** The user that posted the rant. */
    public final User user;

    public FeedRant(long id, String text, int score, long created_time,
    AttachedImage attached_image, String[] tags, VoteState vote_state,
    boolean edited, CollabType c_type, String c_type_long, User user) {
        this.id = id;
        this.text = text;
        this.score = score;
        this.created_time = created_time;
        this.attached_image = attached_image;
        this.tags = Arrays.copyOf(tags, tags.length);
        this.vote_state = vote_state;
        this.edited = edited;
        this.c_type = c_type;
        this.c_type_long = c_type_long;
        this.user = user;
    }

    public static FeedRant fromJSON(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        String text = json.getString("text");
        int score = json.getInt("score");
        long created_time = json.getLong("created_time");
        AttachedImage attached_image = AttachedImage.fromJSON(json.optJSONObject("attached_image"));
        JSONArray jsonTags = json.getJSONArray("tags");
            String[] tags = new String[jsonTags.length()];
            for (int i = 0; i < jsonTags.length(); i++)
                tags[i] = jsonTags.getString(i);
        VoteState vote_state = VoteState.fromValue(json.getInt("vote_state"));
        boolean edited = json.getBoolean("edited");
        int rt = json.getInt("rt"); // We don't know what this is. Ignore.
        int rc = json.getInt("rc"); // We don't know what this is. Ignore.
        CollabType c_type = null;
        if (json.has("c_type"))
            c_type = CollabType.fromValue(json.getInt("c_type"));
        String c_type_long = json.optString("c_type_long", "");
        User user = User.fromJSON(json);

        return new FeedRant(
            id, text, score, created_time, attached_image, tags, vote_state, edited,
            c_type, c_type_long, user
        );
    }

}
