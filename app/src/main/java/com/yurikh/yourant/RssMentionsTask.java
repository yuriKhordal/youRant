package com.yurikh.yourant;

import android.util.Log;

import com.rometools.rome.io.FeedException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RssMentionsTask {
    private ScheduledExecutorService scheduler;

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            Log.w("RssMentions", "RSS mentions task is already running!");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::doTask, 5, 60, TimeUnit.SECONDS);
    }

    public void doTask() {
        Log.d("RssMentions", "Scheduler: Checking for mentions.");
        try {
            RssMentions.checkForMentions();
        } catch (IOException | FeedException e) {
            Log.e("RssMentions", "Failed to check for new mentions", e);
        }
    }
}
