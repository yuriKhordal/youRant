package com.yurikh.yourant.model;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/** An image attached to a rant or a comment. */
public class AttachedImage {
    public final String url;
    public final int width;
    public final int height;

    public AttachedImage(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Nullable
    public static AttachedImage fromJSON(@Nullable JSONObject json) throws JSONException {
        if (json == null) return null;

        String url = json.getString("url");
        int width = json.getInt("width");
        int height = json.getInt("height");

        return new AttachedImage(url, width, height);
    }
}
