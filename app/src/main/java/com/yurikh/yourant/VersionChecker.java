package com.yurikh.yourant;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VersionChecker {
    public static final String GITHUB_TAG = "v0.1.2-alpha";
    public static final String GITHUB_API_URL = "https://api.github.com/repos/yuriKhordal/youRant/releases/latest";
    public static final String GITHUB_BROWSER_URL = "https://github.com/yuriKhordal/youRant/releases/latest";

    public static boolean checkForUpdates(Context context) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GITHUB_API_URL)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            String tagName = json.optString("tag_name");
            if (!GITHUB_TAG.equals(tagName)) {
                onUpdateAvailable(context, json);
                return true;
            }
        } catch (IOException e) {
            Helper.displayError(context, "Update Checker Error", e);
        } catch (JSONException e) {
            Helper.displayError(context, "Update Checker Error", e);
        }
        return false;
    }

    private static void onUpdateAvailable(Context context, JSONObject response) throws JSONException {
        String tagName = response.optString("tag_name");
        String versionName = response.optString("name");
        String apkUrl = findApkUrl(response.optJSONArray("assets"), tagName);

        var builder = new AlertDialog.Builder(context)
                .setTitle("A new version is available!")
                .setMessage(versionName)
                .setNeutralButton("Read Changes", (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_BROWSER_URL));
                    context.startActivity(intent);
                })
                .setNegativeButton("Whatever", null);

        if (apkUrl != null) {
            builder.setPositiveButton("Download Update", (dialogInterface, i) -> {
                String filename = "youRant-" + tagName + ".apk";
                var downReq = new DownloadManager.Request(Uri.parse(apkUrl))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    downReq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                DownloadManager downMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                downMgr.enqueue(downReq);
            });
        } else {
            builder.setPositiveButton("Download Update", (dialogInterface, i) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_BROWSER_URL));
                context.startActivity(intent);
            });
        }

        Handler uiThread = new Handler(Looper.getMainLooper());
        uiThread.post(builder::show);
    }

    private static String findApkUrl(JSONArray assets, String tag) throws JSONException {
        final String EXPECTED_NAME = "youRant-" + tag + ".apk";
        if (assets == null)
            return null;

        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.optString("name");
            String downloadUrl = asset.optString("browser_download_url");

            if (EXPECTED_NAME.equals(name))
                return downloadUrl;
        }
        return  null;
    }
}
