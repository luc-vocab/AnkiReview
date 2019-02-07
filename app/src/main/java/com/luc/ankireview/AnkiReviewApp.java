package com.luc.ankireview;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

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

    }

}
