package com.yurikh.yourant.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile {
    public final long id;
    public final String username;
    public final long score;
    public final String about;
    public final String location;
    public final long created_time;
    public final String skills;
    public final String github;
    public final String website;
    public final boolean dpp;

    public final long rants_count;
    public final long upvote_count;
    public final long comments_count;
    public final long favorites_count;
    public final long collabs_count;

    public final Rant[] rants;
    public final Rant[] favorites;
    public final Object[] collabs; // TODO: Collabs
    public final Comment[] upvotes;
    public final Comment[] comments;


    /** User avatar's background color in hex. */
    public final String avatar_background;
    /** User avatar's filename. */
    public final String avatar_image;

    public Profile(long id, String username, long score, String about, String location,
    long created_time, String skills, String github, String website, boolean dpp, long rants_count,
    long upvote_count, long comments_count, long favorites_count, long collabs_count, Rant[] rants,
    Rant[] favorites, Object[] collabs, Comment[] upvotes, Comment[] comments,
    String avatar_background, String avatar_image) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.about = about;
        this.location = location;
        this.created_time = created_time;
        this.skills = skills;
        this.github = github;
        this.website = website;
        this.dpp = dpp;
        this.rants_count = rants_count;
        this.upvote_count = upvote_count;
        this.comments_count = comments_count;
        this.favorites_count = favorites_count;
        this.collabs_count = collabs_count;
        this.rants = rants;
        this.favorites = favorites;
        this.collabs = collabs;
        this.upvotes = upvotes;
        this.comments = comments;
        this.avatar_background = avatar_background;
        this.avatar_image = avatar_image;
    }

//    public static Profile fromJSON(JSONObject json) {
//
//    }

    public static Profile fromJSONFlat(JSONObject json, long userid) throws JSONException {
        final JSONObject profileJson = json.getJSONObject("profile");
        final JSONObject avatarJson = profileJson.getJSONObject("avatar");
        final JSONObject contentJson = profileJson.getJSONObject("content");
        final JSONObject countsJson = contentJson.getJSONObject("counts");

        final String username = profileJson.getString("username");
        final long score = profileJson.getLong("score");
        final String about = profileJson.getString("about");
        final String location = profileJson.getString("location");
        final long created_time = profileJson.getLong("created_time");
        final String skills = profileJson.getString("skills");
        final String github = profileJson.getString("github");
        final String website = profileJson.getString("website");
        final int dpp = profileJson.optInt("dpp", 0);

        final long rants_count = countsJson.getLong("rants");
        final long upvote_count = countsJson.getLong("upvoted");
        final long comments_count = countsJson.getLong("comments");
        final long favorites_count = countsJson.getLong("favorites");
        final long collabs_count = countsJson.getLong("collabs");

        final Rant[] rants = {};
        final Rant[] favorites = {};
        final Object[] collabs = {};
        final Comment[] upvotes = {};
        final Comment[] comments = {};

        /** User avatar's background color in hex. */
        final String avatar_background = avatarJson.getString("b");
        /** User avatar's filename. */
        final String avatar_image = avatarJson.optString("i");

        return new Profile(
                userid, username, score, about, location, created_time, skills, github, website,
                (dpp == 1), rants_count, upvote_count, comments_count, favorites_count,
                collabs_count, rants, favorites, collabs, upvotes, comments, avatar_background,
                avatar_image
        );
    }
}
