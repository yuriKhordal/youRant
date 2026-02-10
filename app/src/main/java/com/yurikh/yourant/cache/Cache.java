package com.yurikh.yourant.cache;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.yurikh.yourant.UiBackgroundTask;

import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Cache<TKey, TResource> {
    protected final HashMap<TKey, Cached<TResource>> cache;
    protected final TemporalAmount expirationTime;

    public Cache(TemporalAmount expirationTime) {
        this.cache = new HashMap<>();
        this.expirationTime = expirationTime;
    }

    protected abstract TResource load(TKey key) throws Exception;

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized void cleanOld() {
        cache.entrySet().removeIf(
                entry -> entry.getValue().isExpired()
        );
    }

    public TResource get(TKey key) throws InterruptedException, Exception {
        boolean alreadyCached = false;
        Cached<TResource> resource = new Cached<>();
        synchronized (this) {
            Cached<TResource> cached = cache.get(key);
            if (cached == null) {
                cache.put(key, resource);
            } else if (cached.isExpired()) {
                cache.remove(key);
                cache.put(key, resource);
            } else {
                alreadyCached = true;
                resource = cached;
            }
        }

        if (alreadyCached) {
            return resource.await().getResource();
        }

        try {
            TResource res = load(key);
            resource.finishLoading(res, expirationTime);
            return res;
        } catch (Exception e) {
            synchronized (this) {
                cache.remove(key);
            }
            Log.w("Cache", "Failed to load resource with key " + key, e);
            throw e;
        }
    }

    public TResource refresh(TKey key) throws InterruptedException, Exception {
        synchronized (this) {
            cache.remove(key);
        }
        return get(key);
    }

    public synchronized TResource getIfCached(TKey key) {
        Cached<TResource> resource = cache.get(key);
        if (resource == null || resource.isLoading())
            return null;
        return resource.getResource();
    }

    public void getAsync(TKey key, LifecycleOwner lifecycleOwner, Consumer<TResource> onLoad) {
        synchronized (this) {
            var cached = cache.get(key);
            if (cached != null && !cached.isExpired()) {
                onLoad.accept(cached.getResource());
                return;
            }
        }

        new UiBackgroundTask<TResource>(lifecycleOwner)
                .task(() -> get(key))
                .then(onLoad)
                .start();
    }

    public void getAsync(TKey key, LifecycleOwner lifecycleOwner, Consumer<TResource> onLoad, Consumer<Exception> onFail) {
        synchronized (this) {
            var cached = cache.get(key);
            if (cached != null && !cached.isExpired()) {
                onLoad.accept(cached.getResource());
                return;
            }
        }

        new UiBackgroundTask<TResource>(lifecycleOwner)
                .task(() -> get(key))
                .then(onLoad)
                .ifError(InterruptedException.class, e -> {
                    Thread.currentThread().interrupt();
                }).ifError(Exception.class, onFail)
                .start();
    }

    public void refreshAsync(TKey key, LifecycleOwner lifecycleOwner, Consumer<TResource> onLoad) {
        new UiBackgroundTask<TResource>(lifecycleOwner)
                .task(() -> refresh(key))
                .then(onLoad)
                .start();
    }

    public void refreshAsync(TKey key, LifecycleOwner lifecycleOwner, Consumer<TResource> onLoad, Consumer<Exception> onFail) {
        new UiBackgroundTask<TResource>(lifecycleOwner)
                .task(() -> refresh(key))
                .then(onLoad)
                .ifError(InterruptedException.class, e -> {
                    Thread.currentThread().interrupt();
                }).ifError(Exception.class, onFail)
                .start();
    }

    public synchronized boolean isCached(TKey key) {
        var res = cache.get(key);
        if (res == null)
            return false;
        return !res.isLoading();
    }
}
