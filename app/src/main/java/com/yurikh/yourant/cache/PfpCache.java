package com.yurikh.yourant.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public class PfpCache extends Cache<String, Bitmap> {
    public static final String BASE_URL = "https://avatars.devrant.com/";
    public PfpCache() {
        super(Duration.ofMinutes(10));
    }

    @Override
    protected Bitmap load(String url) throws IOException{
        return loadInternal(BASE_URL + url);
    }

    private Bitmap loadInternal(String url) throws IOException {
        try (InputStream stream = new URL(url).openStream()) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap == null)
                throw new IOException("Failed to load image from url: " + url);
            return bitmap;
        }
    }
}
