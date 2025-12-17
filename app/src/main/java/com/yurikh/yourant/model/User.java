package com.yurikh.yourant.model;

import org.json.JSONException;
import org.json.JSONObject;

/** Specifically in Rants or Comments, NOT PROFILE!! */
public class User {
    public final long id;
    public final String username;
    /** The score of the user in upvotes(++). */
    public final long score;
    /** User avatar's background color in hex. */
    public final String avatar_background;
    /** User avatar's filename. */
    public final String avatar_image;
    /** Whether the user is a devRant++ subscriber. (doesn't even work anymore IIRC) */
    public final boolean dpp;

    public User(long id, String username, long score, String avatar_background, String avatar_image, boolean dpp) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.avatar_background = avatar_background;
        this.avatar_image = avatar_image;
        this.dpp = dpp;
    }

    public static User fromJSON(JSONObject json) throws JSONException {
        long id = json.getLong("user_id");
        String username = json.getString("user_username");
        long score = json.getLong("user_score");
        JSONObject avatar = json.getJSONObject("user_avatar");
            String avatar_background = avatar.getString("b");
            String avatar_image = avatar.optString("i", null);
        int dpp = json.optInt("user_dpp", 0);

        return new User(id, username, score, avatar_background, avatar_image, dpp == 1);
    }
}
