package com.luc.ankireview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.luc.ankireview.backgrounds.BackgroundManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class BackgroundViewPagerAdapter extends PagerAdapter {

    public BackgroundViewPagerAdapter(Context context, long deckId) {

        m_context = context;

        m_backgroundManager = new BackgroundManager(deckId);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ImageView imageView = new ImageView(m_context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String imgUrl = m_backgroundManager.getBackgroundUrl();

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


    private ImageView m_left;
    private ImageView m_center;
    private ImageView m_right;

    private Context m_context;

    private BackgroundManager m_backgroundManager;
}
