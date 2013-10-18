package com.imbryk.demo;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

public class App extends Application {

	private static RequestQueue sRequestQueue;
	private static ImageLoader sImageLoader;

	@Override
	public void onCreate() {
		super.onCreate();

		sRequestQueue = Volley.newRequestQueue(
				getApplicationContext(),
				new HttpClientStack(AndroidHttpClient
						.newInstance("com.imbryk.demo/0")));
		sImageLoader = new ImageLoader(sRequestQueue, new BitmapLruCache());
	}


	public static RequestQueue getRequestQueue() {
		return sRequestQueue;
	}

	public static ImageLoader getImageLoader() {
		return sImageLoader;
	}

	public static class BitmapLruCache extends LruCache<String, Bitmap>
			implements ImageCache {
		public static int getDefaultLruCacheSize() {
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			final int cacheSize = maxMemory / 8;

			return cacheSize;
		}

		public BitmapLruCache() {
			this(getDefaultLruCacheSize());
		}

		public BitmapLruCache(int size) {
			super(size);
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight() / 1024;
		}

		@Override
		public Bitmap getBitmap(String url) {
			return get(url);
		}

		@Override
		public void putBitmap(String url, Bitmap bitmap) {
			put(url, bitmap);
		}
	}
}