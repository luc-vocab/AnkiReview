package com.luc.ankireview.backgrounds;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class BackgroundManager {
    private static final String TAG = "BackgroundManager";

    public BackgroundManager() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        m_firestoreDb = FirebaseFirestore.getInstance();
        m_firestoreDb.setFirestoreSettings(settings);


        m_fillImageQueue = new ArrayList<ImageView>();
        m_backgroundUrlList = new Vector<String>();

        m_firestoreDb.collection("backgrounds").document("9JMXEtYV1J9UYCKPvxWv").collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                m_backgroundUrlList.add(document.getString("image"));
                            }
                            Collections.shuffle(m_backgroundUrlList);
                            // process backlog of "fillImageView"
                            for( ImageView imageView : m_fillImageQueue) {
                                fillImageViewComplete(imageView);
                            }
                            m_fillImageQueue.clear();
                            m_backgroundListReady = true;
                            Log.v(TAG, "retrieved " + m_backgroundUrlList.size() + " backgrounds");

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }


    private String getImage() {

        // get current URL
        m_currentBackgroundIndex++;
        if(m_currentBackgroundIndex > m_backgroundUrlList.size() - 1) {
            m_currentBackgroundIndex = 0;
        }
        String imgUrl = m_backgroundUrlList.get(m_currentBackgroundIndex);

        return imgUrl;
    }

    private void fillImageViewComplete(final ImageView imageView) {
        String imagePublicId = getImage();
        Url baseUrl = MediaManager.get().url().secure(true).transformation(new Transformation().quality("auto").fetchFormat("webp")).publicId(imagePublicId);

        MediaManager.get().responsiveUrl(true, true, "imagga_scale", null)
                .stepSize(100)
                .minDimension(100)
                .maxDimension(2500)
                .generate(baseUrl, imageView, new ResponsiveUrl.Callback() {
                    @Override
                    public void onUrlReady(Url url) {
                        String finalUrl = url.generate();
                        Log.v(TAG, "final URL: " + finalUrl);
                        Picasso.get().load(finalUrl ).into(imageView);
                    }
                });
    }

    public void fillImageView(final ImageView imageView)
    {
        if( m_backgroundListReady ) {
            // we have the backgrounds, go ahead
            fillImageViewComplete(imageView);
        } else {
            // need to queue up this action
            m_fillImageQueue.add(imageView);
        }

    }


    private boolean m_backgroundListReady = false;
    private Vector<String> m_backgroundUrlList;
    private int m_currentBackgroundIndex = 0;
    private FirebaseFirestore m_firestoreDb;
    private List<ImageView> m_fillImageQueue;

}
