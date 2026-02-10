package com.yurikh.yourant.cache;


import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

public class Cached<T> {
    private T resource;
    private boolean loading;
    private LocalDateTime expiration;

    public Cached() {
        resource = null;
        loading = true;
        expiration = null;
    }

    synchronized void finishLoading(T resource, TemporalAmount expireAfter)
    throws IllegalStateException {
        if (!loading) {
            throw new IllegalStateException("Resource already loaded.");
        }

        this.resource = resource;
        this.loading = false;
        this.expiration = LocalDateTime.now().plus(expireAfter);
        this.notifyAll();
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    public synchronized boolean isExpired() {
        return !loading && LocalDateTime.now().isAfter(expiration);
    }

    public synchronized T getResource() {
        return resource;
    }

    public synchronized Cached<T> await() throws InterruptedException {
        while (isLoading()) {
            try { // TODO: What if notifyAll happens here? TOCTTOU & deadlock?
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        return this;
    }
}
