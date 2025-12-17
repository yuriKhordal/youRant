package com.yurikh.yourant.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**Represents a link inside a rant or a comment.*/
public class ContentLink {
    public final String type;
    public final String url;
    public final String short_url;
    public final String title;
    public final int start;
    public final int end;

    public ContentLink(String type, String url, String short_url, String title, int start, int end) {
        this.type = type;
        this.url = url;
        this.short_url = short_url;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public String toXmlLink() {
//        return "<string><a href=\"" + url + "\">" + short_url + "</a></string>";
        return "<a href=\"" + url + "\">" + short_url + "</a>";
    }

    public String embedIntoString(String textWithLink) {
        if (!textWithLink.substring(start, end).equals(short_url)) {
            throw new IllegalArgumentException("Link '" + short_url + "' was not found in text:\n" + textWithLink);
        }

        return textWithLink.substring(0, start) + toXmlLink() + textWithLink.substring(end);
    }

    public static ContentLink fromJSON(JSONObject json) throws JSONException {
        String type = json.getString("type");
        String url = json.getString("url");
        String short_url = json.getString("short_url");
        String title = json.getString("title");
        int start = json.getInt("start");
        int end = json.getInt("end");

        return new ContentLink(type, url, short_url, title, start, end);
    }

    public static ContentLink[] fromJSON(JSONArray json) throws JSONException {
        if (json == null) return new ContentLink[0];

        ContentLink[] links = new ContentLink[json.length()];
        for (int i = 0; i < json.length(); i++)
            links[i] = fromJSON(json.getJSONObject(i));
        return links;
    }
}
