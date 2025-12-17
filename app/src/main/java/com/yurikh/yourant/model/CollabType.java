package com.yurikh.yourant.model;

public enum CollabType {
    OpenSourceIdea(1),
    ExistingOpenSourceProject(2),
    ProjectIdea(3),
    ExistingProject(4);

    public final int value;
    private CollabType(int value) {
        this.value = value;
    }

    public static CollabType fromValue(int value) throws IllegalArgumentException {
        if (value == OpenSourceIdea.value) return OpenSourceIdea;
        if (value == ExistingOpenSourceProject.value) return ExistingOpenSourceProject;
        if (value == ProjectIdea.value) return ProjectIdea;
        if (value == ExistingProject.value) return ExistingProject;

        throw new IllegalArgumentException("No CollabType with the value " + value);
    }
}
