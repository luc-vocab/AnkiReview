package com.luc.ankireview;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.HashMap;
import java.util.Map;

public class AnkiReviewApp extends Application {
    private static final String TAG = "AnkiReviewApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // initialization logic placed here
        Log.v(TAG, "onCreate");

        // cloudinary initialization
        // -------------------------

        Map config = new HashMap();
        config.put("cloud_name", "photozzap");
        config.put("secure", true);
        MediaManager.init(this, config);


        // fresco initialization
        // ---------------------

        // enable logging on fresco
        /*
        Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                // other setters
                .setRequestListeners(requestListeners)
                .build();
        Fresco.initialize(this, config);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
        */

        Fresco.initialize(this);


    }
}
