package com.yurikh.yourant;

import org.junit.Test;
import static org.junit.Assert.*;

import com.yurikh.yourant.model.VoteState;

public class VoteStateTest {

    @Test
    public void valuesAreValue() {
        assertEquals(VoteState.Upvoted, VoteState.fromValue(1));
        assertEquals(VoteState.Downvoted, VoteState.fromValue(-1));
        assertEquals(VoteState.Unvoted, VoteState.fromValue(0));
        assertEquals(VoteState.NoVoteAllowed, VoteState.fromValue(-2));

        assertThrows(IllegalArgumentException.class, () -> VoteState.fromValue(-3));
        assertThrows(IllegalArgumentException.class, () -> VoteState.fromValue(2));
        assertThrows(IllegalArgumentException.class, () -> VoteState.fromValue(8721369));
    }
}
