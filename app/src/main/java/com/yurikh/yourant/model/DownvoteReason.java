package com.yurikh.yourant.model;

import androidx.annotation.NonNull;

public enum DownvoteReason {
    NoReasonUpvote(-1, "No reason (upvote or unvote)"),
    NotForMe(0, "Not for me"),
    Repost(1, "Repost"),
    OffensiveSpam(2, "Offensive/Spam");

    public final int value;
    public final String text;
    private DownvoteReason(int value, String sValue) {
        this.value = value;
        this.text = sValue;
    }

    public static DownvoteReason fromValue(int value) throws IllegalArgumentException {
        if (value == NoReasonUpvote.value) return NoReasonUpvote;
        if (value == NotForMe.value) return NotForMe;
        if (value == Repost.value) return Repost;
        if (value == OffensiveSpam.value) return OffensiveSpam;

        throw new IllegalArgumentException("No VoteReason with the value " + value);
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
