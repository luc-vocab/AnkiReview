package com.luc.ankireview;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class AnkiReviewGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setLogLevel(Log.DEBUG);
        // memory cache size
        long memoryCacheSizeBytes = 1024l * 1024l * 500l; // 500mb
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        // disk cache size
        long diskCacheSizeBytes = 1024l * 1024l * 3000l; // 3 GB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
}
