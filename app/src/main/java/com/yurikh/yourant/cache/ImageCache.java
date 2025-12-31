package com.yurikh.yourant.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.function.Consumer;

public class ImageCache {
    private final HashMap<String, CachedImage> cache;

    public ImageCache() {
        cache = new HashMap<>();
    }

    public Bitmap getBitmap(String url) {
        boolean existsAlready = false;
        CachedImage image = new CachedImage();
        synchronized (this) {
            CachedImage cached = cache.get(url);
            if (cached == null) {
                cache.put(url, image);
            } else {
                existsAlready = true;
                image = cached;
            }
        }

        if (existsAlready) {
            try {
                return image.await().getImage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        try (InputStream stream = new URL(url).openStream()) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap == null)
                throw new IOException();
            image.finishLoading(bitmap);
            return bitmap;
        } catch (IOException e) {
            synchronized (this) {
                cache.remove(url);
            }
            Log.e("ImageCache", "Failed to load image from " + url, e);
            return null;
        } catch (IllegalStateException e) {
            Log.w("ImageCache", "Failed to load image from ", e);
            return null;
        }
    }

    public void getBitmapAsync(String url, Consumer<Bitmap> consumer) {
        Bitmap cached = getBitmapIfCached(url);
        if (cached != null) {
            consumer.accept(cached);
            return;
        }

        new Thread(() -> consumer.accept(getBitmap(url))).start();
    }

    public synchronized Bitmap getBitmapIfCached(String url) {
        CachedImage image = cache.get(url);
        if (image == null || image.isLoading())
            return null;
        return image.getImage();
    }

    public synchronized boolean isCached(String url) {
        // Check if exists OR if null(still loading)
        return cache.get(url) != null;
    }

    private static class CachedImage {
        private Bitmap image;
        private boolean loading;

        public CachedImage() {
            loading = true;
            image = null;
        }

        synchronized void finishLoading(Bitmap bitmap) throws IllegalStateException {
            if (!loading) {
                throw new IllegalStateException("Image already loaded.");
            }

            this.image = bitmap;
            this.loading = false;
            this.notifyAll();
        }

        public synchronized boolean isLoading() {
            return loading;
        }

        public synchronized Bitmap getImage() {
            return image;
        }

        public synchronized CachedImage await() throws InterruptedException {
            while (isLoading()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
            return this;
        }
    }
}
