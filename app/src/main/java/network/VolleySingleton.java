package network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import api.ShouldProvideVolleyInstance;

public class VolleySingleton implements ShouldProvideVolleyInstance {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private VolleySingleton(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), new OkHttp3Stack());

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>(100);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static void init(Context context) {
        mInstance = new VolleySingleton(context);
    }

    public static VolleySingleton getInstance() {
        if (mInstance == null) {
            throwException();
        }

        return mInstance;
    }

    @Override
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            throwException();
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            throwException();
        }

        return mImageLoader;
    }

    private static void throwException() {
        throw new RuntimeException("Volley should be initialized");
    }
}