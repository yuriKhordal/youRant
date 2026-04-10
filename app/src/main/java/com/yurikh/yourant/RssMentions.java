package com.yurikh.yourant;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

public class RssMentions {
    private static final String RSS_URL = "https://static.molodetz.nl/dr.mentions.xml";
    private static final String PREFS_NAME = "rss_prefs";
    private static final String PREF_LAST_ENTRY = "last_entry";
    public static SyndFeed fetchFeed() throws FeedException, IOException {
        try {
            URL feedUrl = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            return input.build(new XmlReader(feedUrl));
        } catch (FeedException | IOException e) {
            Log.e("RssMentions", "Failed to fetch RSS feed", e);
            throw e;
        }
    }

    public static void checkForMentions() throws FeedException, IOException {
        // Don't check for mentions if guest.
        if (!YouRantApp.getInstance().isLoggedIn())
            return;

        Date lastEntry = getLastEntry();
        SyndFeed feed = fetchFeed();

        Date nextLastEntry = new Date(0);
        int i = 0;
        for(var entry : feed.getEntries()) {
            Date date = entry.getPublishedDate();
            if (i++ < 0) // DEBUG: notify first three notifications
                handleEntry(entry);
            else if (date.after(lastEntry))
                handleEntry(entry);
            if (date.after(nextLastEntry))
                nextLastEntry = date;
        }

        setLastEntry(nextLastEntry);
//        NotificationHelper.notifySample(); // DEBUG: Test notification.
    }

    private static void handleEntry(SyndEntry entry) {
        // TODO: Add validation for getTitle, getDescription (nulls), and title.indexOf (-1) and log a warning.
        String title = entry.getTitle();
        String content = entry.getDescription().getValue();
        String author = title.substring(0, title.indexOf(' ')); // The user who mentioned, author of the comment.
        String username = title.substring(title.indexOf('@')+1); // The user who was mentioned.
        String link = entry.getLink();
        long rant_id = Long.parseLong(link.substring(link.lastIndexOf('/')+1)); // Is this a hack?
        Date publishDate = entry.getPublishedDate();
        // TODO: Later use comment ID and username to identify notification to know to cancel it later.
        int id = (int) System.currentTimeMillis(); // This should be comment ID.
        Bitmap profile = null;

        if (isCurrentUser(username))
            NotificationHelper.notifyCommentMention(id, username, rant_id, publishDate, author, content, profile);
    }

    private static boolean isCurrentUser(String username) {
        YouRantApp app = YouRantApp.getInstance();
        var loggedInUser = app.getLoggedInUser();
        if (!loggedInUser.isInitialized() || loggedInUser.getValue() == null)
            return false;
        return loggedInUser.getValue().username.equals(username);
    }

    private static Date getLastEntry() {
        YouRantApp app = YouRantApp.getInstance();

        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long timestamp = prefs.getLong(PREF_LAST_ENTRY, -1);
        if (timestamp == -1)
            return new Date();
        return new Date(timestamp);
    }

    private static void setLastEntry(Date date) {
        YouRantApp app = YouRantApp.getInstance();
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(PREF_LAST_ENTRY, date.getTime()).apply();
    }

    public static void debugSetLastEntry(Date date) {
        YouRantApp app = YouRantApp.getInstance();
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(PREF_LAST_ENTRY, date.getTime()).apply();
    }
}
