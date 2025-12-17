package com.yurikh.yourant.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthToken {
    public final long id;
    public final String key;
    public final long expire_time;
    public final long user_id;

    public AuthToken(long id, String key, long expire_time, long user_id) {
        this.id = id;
        this.key = key;
        this.expire_time = expire_time;
        this.user_id = user_id;
    }


    public static AuthToken fromJSON(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        String key = json.getString("key");
        long expire_time = json.getLong("expire_time");
        long user_id = json.getLong("user_id");

        return new AuthToken(id, key, expire_time, user_id);
    }
}
