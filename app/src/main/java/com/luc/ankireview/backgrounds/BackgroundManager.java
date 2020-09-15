package com.luc.ankireview.backgrounds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.luc.ankireview.DynamicHeightImageView;

import java.util.Collections;
import java.util.Vector;


public class BackgroundManager {
    private static final String TAG = "BackgroundManager";

    public static final String TEACHERS = "teachers";
    public static final String BACKGROUNDS = "backgrounds";

    public static final String CROPMODE_IMAGGA_SCALE = "imagga_scale";
    public static final String CROPMODE_LIMIT = "limit";

    public static final String GRAVITY_NORTH = "north";


    private class BitmapImageViewTargetDynamic extends BitmapImageViewTarget {

        public BitmapImageViewTargetDynamic(ImageView view) {
            super(view);
        }

        @Override
        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            Log.v(TAG, "BitmapImageViewTargetDynamic.onResourceReady");
            processLoadedBitmap(bitmap);
        }

    }

    public enum BackgroundType {
        Teachers(TEACHERS, CROPMODE_LIMIT, GRAVITY_NORTH, false, true),
        Backgrounds(BACKGROUNDS, CROPMODE_IMAGGA_SCALE, null, true, false),
        BackgroundsFull(BACKGROUNDS, CROPMODE_IMAGGA_SCALE, null, false, false);

        BackgroundType(String setType, String cropMode, String gravity, boolean applyBlur, boolean resizeImageView) {
            m_setType = setType;
            m_cropMode = cropMode;
            m_gravity = gravity;
            m_applyBlur = applyBlur;
            m_resizeImageView = resizeImageView;
        }

        public String getSetType() {
            return m_setType;
        }

        public String getCropMode() {
            return m_cropMode;
        }

        public String getGravity() { return m_gravity; }

        public boolean getApplyBlur() {
            return m_applyBlur;
        }

        public boolean getResizeImageView() { return m_resizeImageView; }

        private final String m_setType;
        private final String m_cropMode;
        private final String m_gravity;
        private final boolean m_applyBlur;
        private final boolean m_resizeImageView;
    }


    public BackgroundManager(
            DynamicHeightImageView imageView,
            BackgroundType backgroundType, String setName, int changeImageEveryNumTicks, MotionLayout container, View teacherSpacerTop) {

        m_imageView = imageView;
        m_target = new BitmapImageViewTargetDynamic(m_imageView);
//        m_imageView.setHeightRatio(1.0);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        m_firestoreDb = FirebaseFirestore.getInstance();
        m_firestoreDb.setFirestoreSettings(settings);

        m_backgroundType = backgroundType;

        m_changeImageNumTicks = changeImageEveryNumTicks;

        m_imageUrlList = new Vector<String>();

        m_container = container;
        m_teacherSpacerTop = teacherSpacerTop;

        // Log.v(TAG, "retrieving ")
        CollectionReference path = m_firestoreDb.collection(m_backgroundType.getSetType()).document(setName).collection("images");
        Log.v(TAG, "retrieving from " + path.getPath());
        path
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                m_imageUrlList.add(document.getString("public_id"));
                            }
                            Log.v(TAG, "retrieved " + m_imageUrlList.size() + " backgrounds");
                            Collections.shuffle(m_imageUrlList);

                            // process backlog of "fillImageView"
                            fillImageViewComplete();
                            m_backgroundListReady = true;


                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void processLoadedBitmap(Bitmap bitmap) {
        if( m_backgroundType.getResizeImageView()) {
            int availableWidth = m_container.getWidth();
            int availableHeight = m_container.getHeight();

            // set height of spacer
            int spacerHeight = availableHeight - bitmap.getHeight();
            if( spacerHeight < 0) {
                // don't go below zero
                spacerHeight = 0;
            }

            Log.v(TAG, "processLoadedBitmap: "
                    + " bitmap.getWidth(): " + bitmap.getWidth()
                    + " bitmap.getHeight(): " + bitmap.getHeight()
                    + " availableHeight: " + availableHeight
                    + " spacerHeight: " + spacerHeight);

            ViewGroup.LayoutParams layoutParams = m_teacherSpacerTop.getLayoutParams();
            layoutParams.height = spacerHeight;
            m_teacherSpacerTop.setMinimumHeight(spacerHeight);
            m_teacherSpacerTop.requestLayout();

            Log.v(TAG, "setting teacherSpacerTop height to " + spacerHeight);

            float middle = (float) (availableWidth / 2.0);
            m_imageView.setPivotX(middle);

            // m_teacherSpacerTop.setLayoutParams(new ConstraintLayout.LayoutParams(availableWidth, spacerHeight));
        }

        // Bitmap is loaded, use image here

        float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();

        Log.v(TAG, "onBitmapLoaded, ratio: " + ratio);

        // Set the ratio for the image
        m_imageView.setHeightRatio(ratio);
        // Load the image into the view
        m_imageView.setImageBitmap(bitmap);
    }

    private String getImage() {

        // get current URL
        m_currentBackgroundIndex++;
        if(m_currentBackgroundIndex > m_imageUrlList.size() - 1) {
            m_currentBackgroundIndex = 0;
        }
        String imgUrl = m_imageUrlList.get(m_currentBackgroundIndex);

        return imgUrl;
    }

    private void fillImageViewComplete() {
        String imagePublicId = getImage();

        int width = m_container.getWidth();
        int height = m_container.getHeight();

        Url baseUrl = MediaManager.get().url().secure(true).transformation(new Transformation()
                .quality("auto")
                .fetchFormat("webp")
                .width(width)
                .height(height)
                .crop(m_backgroundType.getCropMode())
                .gravity(m_backgroundType.getGravity())
        ).publicId(imagePublicId);

        if(m_backgroundType.getApplyBlur()) {
            baseUrl = MediaManager.get().url().secure(true).
                    transformation(
                            new Transformation()
                                    .quality("auto")
                                    .fetchFormat("webp")
                                    .width(width)
                                    .height(height)
                                    .crop(m_backgroundType.getCropMode())
                                    .gravity(m_backgroundType.getGravity())
                                    .effect("blur:200")
                    )
                    .publicId(imagePublicId);
        }

        String finalUrl = baseUrl.generate();
        Log.v(TAG, "Final URL: " + finalUrl);

        Glide
            .with(m_container)
            .asBitmap()
            .load(finalUrl)
            .timeout(60000)
            .fitCenter() // seems to work better with the placeholder logic
            .placeholder(m_imageView.getDrawable())
            .into(m_target);

    }

    public void fillImageView()
    {
        Log.v(TAG, "fillImageView");

        if( m_backgroundListReady ) {
            // we have the backgrounds, go ahead
            fillImageViewComplete();
        }
        // queue up action

    }

    public void tick() {
        m_ticks += 1;
        if( m_ticks % m_changeImageNumTicks == 0) {
            fillImageView();
        }
    }


    private BackgroundType m_backgroundType;
    private boolean m_backgroundListReady = false;
    private Vector<String> m_imageUrlList;
    private int m_currentBackgroundIndex = 0;
    private FirebaseFirestore m_firestoreDb;
    private BitmapImageViewTargetDynamic m_target;
    private DynamicHeightImageView m_imageView;
    private int m_changeImageNumTicks = 3;
    private int m_ticks = 0;
    private MotionLayout m_container;
    private View m_teacherSpacerTop;

}
