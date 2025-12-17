package com.yurikh.yourant.model;

public enum VoteState {
    Upvoted(1),
    Downvoted(-1),
    Unvoted(0),
    NoVoteAllowed(-2);

    public final int value;
    private VoteState(int value) {
        this.value = value;
    }

    public static VoteState fromValue(int value) throws IllegalArgumentException {
        if (value == Upvoted.value) return Upvoted;
        if (value == Downvoted.value) return Downvoted;
        if (value == Unvoted.value) return Unvoted;
        if (value == NoVoteAllowed.value) return NoVoteAllowed;

        throw new IllegalArgumentException("No VoteState with the value " + value);
    }

}
