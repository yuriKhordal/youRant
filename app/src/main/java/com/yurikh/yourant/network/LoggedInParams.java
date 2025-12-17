package com.yurikh.yourant.network;

import okhttp3.FormBody;

public class LoggedInParams {
    public static final LoggedInParams ANONYMOUS = new LoggedInParams();

    public final int app = 3;
    public final long token_id;
    public final String token_key;
    public final long user_id;
    public boolean logged_in;

    private LoggedInParams() {
        token_id = 0;
        token_key = "";
        user_id = 0;
        logged_in = false;
    }

    public LoggedInParams(long tokenId, String tokenKey, long userId) {
        token_id = tokenId;
        token_key = tokenKey;
        user_id = userId;
        logged_in = true;
    }

    public String toUrlParamString() {
        if (logged_in)
            return "app=" + app +
                "&token_id=" + token_id +
                "&token_key=" + token_key +
                "&user_id=" + user_id;
        else return "app=" + app;
    }

    public FormBody.Builder toFormUrlEncoded() {
        if (!logged_in) {
            return new FormBody.Builder().add("app", app + "");
        }

        return new FormBody.Builder()
            .add("app", app + "")
            .add("token_id", token_id + "")
            .add("token_key", token_key)
            .add("user_id", user_id + "");
    }
}
