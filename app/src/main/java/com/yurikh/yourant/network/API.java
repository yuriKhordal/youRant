package com.yurikh.yourant.network;

import android.util.Log;

import com.yurikh.yourant.Helper;
import com.yurikh.yourant.YouRantApp;
import com.yurikh.yourant.model.AuthToken;
import com.yurikh.yourant.model.Comment;
import com.yurikh.yourant.model.FeedRant;
import com.yurikh.yourant.model.Profile;
import com.yurikh.yourant.model.Rant;
import com.yurikh.yourant.model.RantCategory;
import com.yurikh.yourant.model.RantFeed;
import com.yurikh.yourant.model.DownvoteReason;
import com.yurikh.yourant.model.VoteState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class API {
    public static final String API_BASE = "https://devrant.com/api";
//    public static final String API_BASE = "https://dr.molodetz.nl/api";

    // ==================== Rants ====================

    public static RantFeed getRantsFeed(RantSort sort, long page) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE +
                "/devrant/rants?" + loggedInParams.toUrlParamString() +
                "&sort=" + sort.toString() +
                "&limit=" + 50 +
                "&skip=" + (page * 50);

        JSONObject json = requestRetries(RequestMethod.GET, PATH, null, 5);
        return RantFeed.fromJSON(json);
    }

    public static Rant getRant(long id) throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants/" + id + "?" + loggedInParams.toUrlParamString();
        JSONObject json = requestRetries(RequestMethod.GET, PATH, null, 5);
        return Rant.fromJSON(json);
    }

    public static long postRant(RantCategory category, String content, String[] tags)
    throws JSONException, IOException {
        String _tags = String.join(", ", tags);
        return postRant(category, content, _tags);
    }

    public static long postRant(RantCategory category, String content, String tags)
    throws JSONException, IOException {
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

    public static FeedRant postRantVote(long rantId, VoteState state, DownvoteReason reason)
    throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/devrant/rants/" + rantId + "/vote";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to upvote/downvote!");
        }

        var builder = loggedInParams.toFormUrlEncoded()
                .add("vote", state.value + "");
        if (reason != DownvoteReason.NoReasonUpvote)
            builder.add("reason", reason.value + "");
        FormBody body = builder.build();

        JSONObject json = request(RequestMethod.POST, PATH, body);
        return FeedRant.fromJSON(json.getJSONObject("rant"));
    }

    // ==================== Comments ====================

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

    public static Comment postCommentVote(long commentId, VoteState state, DownvoteReason reason)
            throws JSONException, IOException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/comments/" + commentId + "/vote";
        if (!loggedInParams.logged_in) {
            throw new RuntimeException("You must log in to upvote/downvote!");
        }

        var builder = loggedInParams.toFormUrlEncoded()
                .add("vote", state.value + "");
        if (reason != DownvoteReason.NoReasonUpvote)
            builder.add("reason", reason.value + "");
        FormBody body = builder.build();

        JSONObject json = request(RequestMethod.POST, PATH, body);
        return Comment.fromJSON(json.getJSONObject("comment"));
    }

    // ==================== Users ====================

    public static Profile getFlatProfile(long userid) throws IOException, JSONException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/users/" + userid + "?" + loggedInParams.toUrlParamString();
        JSONObject json = requestRetries(RequestMethod.GET, PATH, null, 5);
        return Profile.fromJSONFlat(json, userid);
    }

    public static Profile getFlatProfile(String username) throws IOException, JSONException {
        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
        final String PATH = API_BASE + "/get-user-id?" + loggedInParams.toUrlParamString() +
                "&username=" + username;
        JSONObject json = requestRetries(RequestMethod.GET, PATH, null, 5);
        long userid = json.getLong("user_id");
        return getFlatProfile(userid);
    }

    // ==================== Authentication ====================

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

//    public static Profile getEmptyProfile(long userid) throws JSONException, IOException {
//        final LoggedInParams loggedInParams = YouRantApp.getInstance().getLoggedInParams();
//        final String PATH = API_BASE + "/users/" + userid +
//                "?" + loggedInParams.toUrlParamString() +
//                "&limit=0";
//        JSONObject json = request(RequestMethod.GET, PATH);
//        return Profile.fromJSON(json);
//    }

    // ==================== Request Helpers ====================

    private enum RequestMethod { GET, POST, DELETE }

    private static JSONObject request(RequestMethod method, String url, RequestBody body)
    throws IOException, JSONException {
        TrustManager[] trustManagers = {devrantTrustManager};
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        CertificatePinner certPinner = new CertificatePinner.Builder()
                .add("devrant.com", "sha256/kyLqB2UeGxOdM0UDAa74JGJr1WZhKgREsZCxb2hTTXk=")
                .add("devrant.com", "sha256/AlSQhgtJirc8ahLyekmtX+Iw+v46yPYRLJt9Cq1GlB0=")
                .build();

//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
//                .sslSocketFactory(sslContext.getSocketFactory(), devrantTrustManager)
//                .certificatePinner(certPinner)
                .build();

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
            // For newly registered users whoose email is not verified yet.
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

    private static JSONObject requestRetries(RequestMethod method, String url, RequestBody body, int maxRetries)
    throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url);
        switch (method) {
            case GET: builder.get(); break;
            case POST: builder.post(body); break;
            case DELETE: builder.delete(body); break;
            default: throw new IllegalArgumentException("Error: Method '" + method + "' unknown or unsupported.");
        }

        Request request = builder.build();
        int retry = 1;

        while (true) {
            try (Response response = client.newCall(request).execute()) {
                ResponseBody resBody = response.body();
                if (isSlimAppError(response.code(), resBody)) {
                    if (retry < maxRetries) {
                        retry++;
                        continue;
                    }
                    throw new IOException(
                        "Error 500 Internal Server Error: Slim Application error. Failed after " +
                        maxRetries + " retries."
                    );
                }
                JSONObject json = new JSONObject(resBody.string());
                boolean success = json.getBoolean("success");
                // For newly registered users whoose email is not verified yet.
                if (json.has("confirmed") && !json.getBoolean("confirmed"))
                    throw new IOException("Error: Email unverified!");
                String error = json.optString("error", "");
                if (!response.isSuccessful())
                    throw new IOException("Error " + response.code() + ": " + error);
                if (!success)
                    throw new IOException("Error: " + error);
                Log.d("SlimAppErr", "Success after " + retry + " tries.");
                return json;
            }
        }
    }

    private static boolean isSlimAppError(int code, ResponseBody body) throws IOException {
        if (code != 500) return false;
        MediaType media = body.contentType();
        if (media != null && (!media.type().equals("text") || !media.subtype().equals("html")))
            return false;
        return body.string().contains("Slim Application Error");
    }

    private static final X509TrustManager devrantTrustManager = new X509TrustManager() {
        final String devRantHash = "1ae12e02c6656ee36994f1b76398ff6ff7df11194ad044188b07eb111e2690d2";
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String s)
        throws CertificateException {
            certs[0].checkValidity();
        }
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String s)
        throws CertificateException {
//            for (var cert : certs) {
//                String hash = CertificatePinner.pin(cert);
//                Log.i("CERT", hash);
//            }
            if (!isDevRantCert(certs[0])) {
                certs[0].checkValidity();
            }
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public boolean isDevRantCert(X509Certificate cert) throws CertificateEncodingException {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] hashBinary = digest.digest(cert.getEncoded());
            String hash = Helper.byteArrayToHex(hashBinary);
            return devRantHash.equals(hash);
        }
    };
}