package com.luc.ankireview.backgrounds;

import android.util.Log;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import static android.support.constraint.Constraints.TAG;

public class BackgroundManager {
    private static final String TAG = "BackgroundManager";

    public BackgroundManager(long deckId) {
        m_deckId = deckId;

        String[] backgroundImageUrls = {
                "v1540301931/ankireview_backgrounds/chinese_women/dreamstimemaximum_52491159.jpg",
                "v1540301930/ankireview_backgrounds/chinese_women/dreamstimemaximum_51242767.jpg",
                "v1540301928/ankireview_backgrounds/chinese_women/dreamstimemaximum_46084453.jpg",
                "v1540301927/ankireview_backgrounds/chinese_women/dreamstimemaximum_45547181.jpg",
                "v1540301925/ankireview_backgrounds/chinese_women/dreamstimemaximum_45193806.jpg",
                "v1540301923/ankireview_backgrounds/chinese_women/dreamstimemaximum_41211514.jpg",
                "v1540301922/ankireview_backgrounds/chinese_women/dreamstimemaximum_41171330.jpg",
                "v1540301921/ankireview_backgrounds/chinese_women/dreamstimemaximum_40065466.jpg",
                "v1540301920/ankireview_backgrounds/chinese_women/dreamstimemaximum_33367818.jpg",
                "v1540301917/ankireview_backgrounds/chinese_women/dreamstimemaximum_33112734.jpg",
                "v1540301912/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54834109.jpg",
                "v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686849.jpg",
                "v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54833749.jpg",
                "v1540301909/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686790.jpg",
                "v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_48563750.jpg",
                "v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136353.jpg",
                "v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136341.jpg"
        };

        m_backgroundUrlList = new Vector<String>(Arrays.asList(backgroundImageUrls));
        Collections.shuffle(m_backgroundUrlList);
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

    public void fillImageView(final SimpleDraweeView imageView)
    {
        String imagePublicId = getImage();
        Url baseUrl = MediaManager.get().url().transformation(new Transformation().quality("auto").fetchFormat("webp")).publicId(imagePublicId);

                //.generate(imageUrl);
        // baseUrl.transformation(new Transformation().quality("auto").fetchFormat("webp")).generate(imageUrl);

        MediaManager.get().responsiveUrl(true, true, "imagga_scale", null)
                .stepSize(1)
                .minDimension(500)
                .maxDimension(2000)
                .generate(baseUrl, imageView, new ResponsiveUrl.Callback() {
                    @Override
                    public void onUrlReady(Url url) {
                        String finalUrl = url.generate();
                        Log.v(TAG, "final URL: " + finalUrl);
                        imageView.setImageURI(finalUrl);
                    }
                });
    }


    private long m_deckId;

    private Vector<String> m_backgroundUrlList;
    private int m_currentBackgroundIndex = 0;

}
