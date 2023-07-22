package com.luc.ankireview;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cloudinary.android.MediaManager;
// import com.crashlytics.android.Crashlytics;
// import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
// import io.fabric.sdk.android.Fabric;

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

    public static void handleForceDarkSetting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean forceDarkMode = prefs.getBoolean("force_dark_mode", false);
        if (forceDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

}
