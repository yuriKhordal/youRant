package com.yurikh.yourant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Rant {
    public final long id;
    public final String text;
    public final int score;
    public final long created_time;
    public final AttachedImage attached_image;
    public final int num_comments;
    public final String[] tags;
    public final VoteState vote_state;
    public final boolean edited;
    public final String link;
    public final int rt;
    public final int rc;
    public final ContentLink[] links;
    public final CollabInfo collab;
    public final User user;
    public final Comment[] comments;

    public Rant(long id, String text, int score, long created_time, AttachedImage attached_image,
    int num_comments, String[] tags, VoteState vote_state, boolean edited, String link,
    int rt, int rc, ContentLink[] links, CollabInfo collab, User user, Comment[] comments) {
        this.id = id;
        this.text = text;
        this.score = score;
        this.created_time = created_time;
        this.attached_image = attached_image;
        this.num_comments = num_comments;
        this.tags = tags;
        this.vote_state = vote_state;
        this.edited = edited;
        this.link = link;
        this.rt = rt;
        this.rc = rc;
        this.links = links;
        this.collab = collab;
        this.user = user;
        this.comments = comments;
    }

    public static Rant fromJSON(JSONObject json) throws JSONException {
        final JSONObject EMPTY = new JSONObject();

        JSONObject rantJson = json.getJSONObject("rant");
        long id = rantJson.getLong("id");
        String text = rantJson.getString("text");
        int score = rantJson.getInt("score");
        long created_time = rantJson.getLong("created_time");
        AttachedImage attached_image = AttachedImage.fromJSON(rantJson.optJSONObject("attached_image"));
        int num_comments = rantJson.getInt("num_comments");
        JSONArray jsonTags = rantJson.getJSONArray("tags");
        String[] tags = new String[jsonTags.length()];
        for (int i = 0; i < jsonTags.length(); i++)
            tags[i] = jsonTags.getString(i);
        VoteState vote_state = VoteState.fromValue(rantJson.getInt("vote_state"));
        boolean edited = rantJson.getBoolean("edited");
        String link = rantJson.getString("link");
        int rt = rantJson.getInt("rt"); // We don't know what this is. Ignore.
        int rc = rantJson.getInt("rc"); // We don't know what this is. Ignore.
        ContentLink[] links = ContentLink.fromJSON(rantJson.optJSONArray("links"));
        CollabInfo collab = CollabInfo.fromJSON(rantJson);
        User user = User.fromJSON(rantJson);

        Comment[] comments = Comment.fromJSON(json.getJSONArray("comments"));

        return new Rant(
                id, text, score, created_time, attached_image, num_comments, tags,
                vote_state, edited, link, rt, rc, links, collab, user, comments
        );
    }

    public static class CollabInfo {
        public final CollabType c_type;
        public final String c_type_long;
        public final String c_description;
        public final String c_tech_stack;
        public final String c_team_size;
        public final String c_url;

        public CollabInfo(CollabType c_type, String c_type_long, String c_description,
        String c_tech_stack, String c_team_size, String c_url) {
            this.c_type = c_type;
            this.c_type_long = c_type_long;
            this.c_description = c_description;
            this.c_tech_stack = c_tech_stack;
            this.c_team_size = c_team_size;
            this.c_url = c_url;
        }

        public static CollabInfo fromJSON(JSONObject json) throws JSONException {
            if (!json.has("c_type"))
                return null;

            CollabType c_type = CollabType.fromValue(json.getInt("c_type"));
            String c_type_long = json.getString("c_type_long");
            String c_description = json.getString("c_description");
            String c_tech_stack = json.getString("c_tech_stack");
            String c_team_size = json.getString("c_team_size");
            String c_url = json.getString("c_url");

            return new CollabInfo(c_type, c_type_long, c_description, c_tech_stack, c_team_size, c_url);
        }
    }
}
