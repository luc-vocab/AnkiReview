package com.luc.ankireview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Random;

public class BackgroundViewPagerAdapter extends PagerAdapter {

    public BackgroundViewPagerAdapter(Context context) {
        m_context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // add the correct webview to the viewgroup, based on position

        ImageView imageView = new ImageView(m_context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


        String[] backgroundImageUrls = {
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301931/ankireview_backgrounds/chinese_women/dreamstimemaximum_52491159.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301930/ankireview_backgrounds/chinese_women/dreamstimemaximum_51242767.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301928/ankireview_backgrounds/chinese_women/dreamstimemaximum_46084453.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301927/ankireview_backgrounds/chinese_women/dreamstimemaximum_45547181.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301925/ankireview_backgrounds/chinese_women/dreamstimemaximum_45193806.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301923/ankireview_backgrounds/chinese_women/dreamstimemaximum_41211514.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301922/ankireview_backgrounds/chinese_women/dreamstimemaximum_41171330.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301921/ankireview_backgrounds/chinese_women/dreamstimemaximum_40065466.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301920/ankireview_backgrounds/chinese_women/dreamstimemaximum_33367818.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301917/ankireview_backgrounds/chinese_women/dreamstimemaximum_33112734.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301912/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54834109.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686849.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301911/ankireview_backgrounds/chinese_women/dreamstimeextralarge_54833749.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301909/ankireview_backgrounds/chinese_women/dreamstimeextralarge_53686790.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_48563750.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136353.jpg",
                "https://res.cloudinary.com/photozzap/image/upload/c_scale,h_2000/v1540301900/ankireview_backgrounds/chinese_women/dreamstimeextralarge_51136341.jpg"
        };

        int rnd = new Random().nextInt(backgroundImageUrls.length);
        String imgUrl = backgroundImageUrls[rnd];
        Picasso.get().setLoggingEnabled(true);
        Picasso.get().load(imgUrl).into(imageView);

        switch( position)
        {
            case 0:
                m_left = imageView;
                break;
            case 1:
                m_center = imageView;
                break;
            case 2:
                m_right = imageView;
                break;
            default:
                break;
        }

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        // we only ever have 3 pages
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public int getItemPosition (Object object) {
        if( object == m_center ) {
            return 1;
        } else if( object == m_left ) {
            return 0;
        } else if( object == m_right ) {
            return 2;
        }

        // view doesn't exist anymore
        return POSITION_NONE;
    }

    public void moveToNextBackground(int currentPage) {

        if( currentPage == 0) {
            // answered bad on the question before
            m_center = m_left;

            // other views should be regenerated
            m_left = null;
            m_right = null;


        } else if (currentPage == 2 ) {
            // answered good on the previous question
            m_center = m_right;
            // other views should be regenerated
            m_left = null;
            m_right = null;

        } else if (currentPage == 1 ) {
            //regenerate everything
            m_center = null;
            m_left = null;
            m_right = null;

        } else {
            throw new IllegalArgumentException("currentPage " + currentPage + " is impossible");
        }

        notifyDataSetChanged();
    }


    ImageView m_left;
    ImageView m_center;
    ImageView m_right;

    Context m_context;
}
