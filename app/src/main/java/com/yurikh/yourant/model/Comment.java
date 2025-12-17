package com.yurikh.yourant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
    public final long id;
    public final long rand_id;
    public final String body;
    public final int score;
    public final long created_time;
    public final VoteState vote_state;
    public final ContentLink[] links;
    public final boolean edited;
    public final User user;
    public final AttachedImage attached_image;

    public Comment(long id, long rand_id, String body, int score, long created_time,
    VoteState vote_state, ContentLink[] links, boolean edited, User user,
    AttachedImage attached_image) {
        this.id = id;
        this.rand_id = rand_id;
        this.body = body;
        this.score = score;
        this.created_time = created_time;
        this.vote_state = vote_state;
        this.links = links;
        this.edited = edited;
        this.user = user;
        this.attached_image = attached_image;
    }

    public static Comment fromJSON(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        long rand_id = json.getLong("rant_id");
        String body = json.getString("body");
        int score = json.getInt("score");
        long created_time = json.getLong("created_time");
        VoteState vote_state = VoteState.fromValue(json.getInt("vote_state"));
        ContentLink[] links = ContentLink.fromJSON(json.optJSONArray("links"));
        boolean edited = json.optBoolean("edited", false);
        User user = User.fromJSON(json);
        AttachedImage attached_image = AttachedImage.fromJSON(json.optJSONObject("attached_image"));

        return new Comment(
                id, rand_id, body, score, created_time, vote_state,
                links, edited, user, attached_image
        );
    }

    public static Comment[] fromJSON(JSONArray json) throws JSONException {
        if (json == null) return new Comment[0];

        Comment[] comments = new Comment[json.length()];
        for (int i = 0; i < json.length(); i++)
            comments[i] = Comment.fromJSON(json.getJSONObject(i));
        return comments;
    }
}
