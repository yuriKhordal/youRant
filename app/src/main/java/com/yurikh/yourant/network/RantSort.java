package com.yurikh.yourant.network;

public enum RantSort {
    top("top"),algo("top"), recent("recent");

    public final String sort;

    private RantSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return sort;
    }
}
