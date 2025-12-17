package com.yurikh.yourant.model;

import androidx.annotation.NonNull;

public enum RantCategory {
    Rant(1, "Rant"),
    Collab(2, "Collab"),
    JokeMeme (3, "Joke/Meme"),
    Question (4, "Question"),
    devRant (5, "devRant"),
    Random (6, "Random");

    public final int value;
    public final String name;

    private RantCategory(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static RantCategory fromValue(int value) throws IllegalArgumentException {
        if (value == Rant.value) return Rant;
        if (value == Collab.value) return Collab;
        if (value == JokeMeme.value) return JokeMeme;
        if (value == Question.value) return Question;
        if (value == devRant.value) return devRant;
        if (value == Random.value) return Random;

        throw new IllegalArgumentException("No RantCategory with the value " + value);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
