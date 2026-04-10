package com.yurikh.yourant;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rometools.rome.io.FeedException;

import java.io.IOException;

public class RssMentionsWorker extends Worker {

    public RssMentionsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override @NonNull
    public Result doWork() {
        Log.d("RssMentions", "WorkManager: Checking for mentions.");
        try {
            RssMentions.checkForMentions();
        } catch (IOException | FeedException e) {
            return Result.retry();
        }

        return Result.success();
    }
}
