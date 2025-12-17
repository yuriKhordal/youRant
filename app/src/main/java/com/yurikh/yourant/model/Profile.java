package com.yurikh.yourant.model;

import org.json.JSONObject;

public class Profile {
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
    public final Object[] Collabs; // TODO: Collabs
    public final Comment[] upvotes;
    public final Comment[] comments;


    /** User avatar's background color in hex. */
    public final String avatar_background;
    /** User avatar's filename. */
    public final String avatar_image;

    public Profile(String username, long score, String about, String location, long created_time,
    String skills, String github, String website, boolean dpp,
    long rants_count, long upvote_count, long comments_count, long favorites_count, long collabs_count,
    Rant[] rants, Rant[] favorites, Object[] collabs, Comment[] upvotes, Comment[] comments,
    String avatar_background, String avatar_image) {
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
        Collabs = collabs;
        this.upvotes = upvotes;
        this.comments = comments;
        this.avatar_background = avatar_background;
        this.avatar_image = avatar_image;
    }

//    public static Profile fromJSON(JSONObject json) {
//
//    }
}
