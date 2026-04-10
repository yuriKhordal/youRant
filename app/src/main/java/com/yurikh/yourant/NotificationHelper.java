package com.yurikh.yourant;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.yurikh.yourant.ui.RantActivity;

import java.util.Date;

public class NotificationHelper {
    public static void init() {
        YouRantApp app = YouRantApp.getInstance();

        // Create Channel for devRant Mention notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = app.getString(R.string.notif_channel_mentions_id);
            String name = app.getString(R.string.notif_channel_mentions_name);
            String description = app.getString(R.string.notif_channel_mentions_description);
            NotificationChannel channel = new NotificationChannel(
                    id, name, NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(description);
            NotificationManager notifMgr = app.getSystemService(NotificationManager.class);
            notifMgr.createNotificationChannel(channel);
        }
    }

    /**
     * Create and show a notification about the user being mentioned in a comment.
     * The id of the notification is the ID of the comment where the user is mentioned, and the tag
     * of the notification is the mentioned user's username (in case multiple users are mentioned
     * in the same comment).
     * @param commentId The ID of the comment where the user was mentioned. Used as the notification ID.
     * @param mentionedUsername The username of the user who was mentioned. Used as the notification tag.
     * @param when The time the user was mentioned at.
     * @param author The username of the user that wrote the mention. The notification displays this name in the title.
     * @param content The text of the comment the user was mentioned in.
     * @param profile The author's profile picture.
     */
    public static void notifyCommentMention(int commentId, String mentionedUsername, long rant_id, Date when, String author, String content, Bitmap profile) {
        if (!hasPermission()) return;

        YouRantApp app = YouRantApp.getInstance();

        // TODO: Check this, GPT wrote this.
        NotificationManager notificationManager = (NotificationManager)
                app.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = app.getString(R.string.notif_channel_mentions_id);
            builder = new NotificationCompat.Builder(app, channelId);
        } else {
            builder = new NotificationCompat.Builder(app);
        }

        // TODO: Add a makeIntent that scrolls to the comment.
        Intent intent = RantActivity.makeIntent(app, rant_id);
        PendingIntent pintent = PendingIntent.getActivity(app, commentId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        String title = author + " mentioned you in a comment";
        Notification notification = builder
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.yourant_logo2)
                .setWhen(when.getTime())
//                .setLargeIcon(profile)
                .setContentIntent(pintent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(mentionedUsername, commentId, notification);
    }

    public static boolean hasPermission() {
        YouRantApp app = YouRantApp.getInstance();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return true;

        var perm = app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return;
        if (hasPermission())
            return;

        final String[] permission = {Manifest.permission.POST_NOTIFICATIONS};
        ActivityCompat.requestPermissions(ctx, permission, 0);
    }

    public static void notifySample() {
        if (!hasPermission()) return;

        final int SAMPLE_ID = 12;
        final String SAMPLE_TAG = "sample";
        YouRantApp app = YouRantApp.getInstance();

        // TODO: Check this, GPT wrote this.
        NotificationManager notificationManager = (NotificationManager)
                app.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = app.getString(R.string.notif_channel_mentions_id);
            builder = new NotificationCompat.Builder(app, channelId);
        } else {
            builder = new NotificationCompat.Builder(app);
        }

        Notification notification = builder
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification")
                .setSmallIcon(R.drawable.yourant_logo2)
                .setWhen(new Date().getTime())
//                .setLargeIcon(profile)
                .build();

        notificationManager.notify(SAMPLE_TAG, SAMPLE_ID, notification);
    }

    public static void cancelNotification(int id, String tag) {
        YouRantApp app = YouRantApp.getInstance();

        NotificationManager notifMgr = app.getSystemService(NotificationManager.class);
        notifMgr.cancel(tag, id);
    }

}
