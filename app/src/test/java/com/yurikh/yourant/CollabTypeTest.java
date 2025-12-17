package com.yurikh.yourant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.yurikh.yourant.model.CollabType;

import org.junit.Test;

public class CollabTypeTest {

    @Test
    public void valuesAreValue() {
        assertEquals(CollabType.OpenSourceIdea, CollabType.fromValue(1));
        assertEquals(CollabType.ExistingOpenSourceProject, CollabType.fromValue(2));
        assertEquals(CollabType.ProjectIdea, CollabType.fromValue(3));
        assertEquals(CollabType.ExistingProject, CollabType.fromValue(4));

        assertThrows(IllegalArgumentException.class, () -> CollabType.fromValue(0));
        assertThrows(IllegalArgumentException.class, () -> CollabType.fromValue(5));
        assertThrows(IllegalArgumentException.class, () -> CollabType.fromValue(8721369));
        assertThrows(IllegalArgumentException.class, () -> CollabType.fromValue(-124));
    }
}
