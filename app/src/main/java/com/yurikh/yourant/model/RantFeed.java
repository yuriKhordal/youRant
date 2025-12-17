package com.yurikh.yourant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/** Represents a feed of rants. */
public class RantFeed {
    public final FeedRant[] rants;
    public final String notif_state;
    public final String notif_token;
    public final String session_hash;
    public final long weekly_rant_week;
    public final boolean dpp;
    public final int num_notifs;

    public RantFeed(FeedRant[] rants, String notif_state, String notif_token,
    String session_hash, long weekly_rant_week, boolean dpp, int num_notifs) {
        this.rants = Arrays.copyOf(rants, rants.length);
        this.notif_state = notif_state;
        this.notif_token = notif_token;
        this.session_hash = session_hash;
        this.weekly_rant_week = weekly_rant_week;
        this.dpp = dpp;
        this.num_notifs = num_notifs;
    }

    public static RantFeed fromJSON(JSONObject json) throws JSONException {
        final JSONObject EMPTY = new JSONObject();

        boolean success = json.getBoolean("success");
        JSONArray rantsJson = json.getJSONArray("rants");
            FeedRant[] rants = new FeedRant[rantsJson.length()];
            for (int i = 0; i < rantsJson.length(); i++)
                rants[i] = FeedRant.fromJSON(rantsJson.getJSONObject(i));
        JSONObject settingsJson = json.optJSONObject("settings");
        if (settingsJson == null) { settingsJson = EMPTY; }
        String notif_state = settingsJson.optString("notif_state", "");
        String notif_token = settingsJson.optString("notif_token", "");
        String session_hash = json.getString("set");
        long weekly_rant_week = json.getLong("wrw");
        boolean dpp = 0 == json.optInt("dpp", 0);
        int num_notifs = json.optInt("num_notifs", 0);
        JSONObject newsJson = json.getJSONObject("news");

        return new RantFeed(rants, notif_state, notif_token, session_hash, weekly_rant_week, dpp, num_notifs);
    }
}
