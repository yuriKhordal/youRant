package com.yurikh.yourant.network;

import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.AuthToken;
import com.yurikh.yourant.model.Profile;
import com.yurikh.yourant.model.Rant;
import com.yurikh.yourant.model.RantCategory;
import com.yurikh.yourant.model.RantFeed;
import com.yurikh.yourant.model.VoteState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API {
//    public static final String API_BASE = "https://devrant.com";
    public static final String API_BASE = "https://dr.molodetz.nl/api";

    public static RantFeed getRantsFeed(RantSort sort, long page) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE +
                "/devrant/rants?" + loggedInParams.toUrlParamString() +
                "&sort=" + sort.toString() +
                "&limit=" + 50 +
                "&skip=" + (page * 50);

        JSONObject json = request(RequestMethod.GET, PATH);
        return RantFeed.fromJSON(json);
    }

    public static Rant getRant(long id) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants/" + id + "?" + loggedInParams.toUrlParamString();
        JSONObject json = request(RequestMethod.GET, PATH);
        return Rant.fromJSON(json);
    }

    public static AuthToken postAuth(String username, String password) throws JSONException, IOException {
        final String PATH = API_BASE + "/users/auth-token";
        FormBody body = new FormBody.Builder()
                .add("app", "3")
                .addEncoded("username", username)
                .addEncoded("password", password)
                .build();

        JSONObject json = request(RequestMethod.POST, PATH, body);
        return AuthToken.fromJSON(json.getJSONObject("auth_token"));
    }

    public static long postRant(RantCategory category, String content, String[] tags) throws JSONException, IOException {
        String _tags = String.join(", ", tags);
        return postRant(category, content, _tags);
    }

    public static long postRant(RantCategory category, String content, String tags) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to post rants!");
        }
        if (category == RantCategory.Collab) {
            throw new IllegalArgumentException("This is the wrong method for posting collabs.");
        }

        FormBody body = loggedInParams.toFormUrlEncoded()
                .add("rant", content)
                .add("tags", tags)
                .add("type", category.value + "")
                .build();

        JSONObject json = request(RequestMethod.POST, PATH, body);
        return json.getLong("rant_id");
    }

    public static void postComment(long rantId, String comment) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants/" + rantId + "/comments";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to post rants!");
        }

        FormBody body = loggedInParams.toFormUrlEncoded()
                .add("comment", comment)
                .build();

        request(RequestMethod.POST, PATH, body);
    }

    public static void postRantVote(long rantId, VoteState state) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants/" + rantId + "/vote";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to upvote/downvote!");
        }

        FormBody body = loggedInParams.toFormUrlEncoded()
                .add("vote", state.value + "")
                .build();

        request(RequestMethod.POST, PATH, body);
    }

    public static void postCommentVote(long commentId, VoteState state) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/comments/" + commentId + "/vote";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to upvote/downvote!");
        }

        FormBody body = loggedInParams.toFormUrlEncoded()
                .add("vote", state.value + "")
                .build();

        request(RequestMethod.POST, PATH, body);
    }

//    public static Profile getEmptyProfile(long userid) throws JSONException, IOException {
//        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
//        final String PATH = API_BASE + "/users/" + userid +
//                "?" + loggedInParams.toUrlParamString() +
//                "&limit=0";
//        JSONObject json = request(RequestMethod.GET, PATH);
//        return Profile.fromJSON(json);
//    }

    private enum RequestMethod { GET, POST, DELETE }

    private static JSONObject request(RequestMethod method, String url) throws IOException, JSONException {
        return request(method, url, null);
    }

    private static JSONObject request(RequestMethod method, String url, RequestBody body) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url);
        switch (method) {
            case GET: builder.get(); break;
            case POST: builder.post(body); break;
            case DELETE: builder.delete(body); break;
            default: throw new IllegalArgumentException("Error: Method '" + method + "' unknown or unsupported.");
        }

        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            boolean success = json.getBoolean("success");
            if (json.has("confirmed") && !json.getBoolean("confirmed"))
                throw new IOException("Error: Email unverified!");
            String error = json.optString("error", "");
            if (!response.isSuccessful())
                throw new IOException("Error " + response.code() + ": " + error);
            if (!success)
                throw new IOException("Error: " + error);
            return json;
        }
    }
}