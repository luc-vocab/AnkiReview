package com.luc.ankireview.display;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.luc.ankireview.Settings;

public class FlashcardViewPager extends ViewPager {
    private static final String TAG = "FlashcardViewPager";

    public FlashcardViewPager(Context context) {
        super(context);
    }

    public FlashcardViewPager (Context context,
               AttributeSet attrs) {
        super(context, attrs);
    }

    public void enableSwipe() {
        m_swipeEnabled = true;
    }

    public void disableSwipe() {
        m_swipeEnabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(Settings.ENABLE_BACKGROUNDS && m_backgroundSwipingEnabled) {
            m_backgroundPager.onTouchEvent(event);
        }
        if( m_swipeEnabled)
            return super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(Settings.ENABLE_BACKGROUNDS && m_backgroundSwipingEnabled) {
            m_backgroundPager.onInterceptTouchEvent(event);
        }
        if( m_swipeEnabled )
            return super.onInterceptTouchEvent(event);
        return false;
    }

    public void setBackgroundPager(ViewPager backgroundPager) {
        m_backgroundPager = backgroundPager;
    }

    public void enableBackgroundSwiping() {
        m_backgroundSwipingEnabled = true;
    }

    public void disableBackgroundSwiping() {
        m_backgroundSwipingEnabled = false;
    }

    private boolean m_swipeEnabled = false;
    private boolean m_backgroundSwipingEnabled = false;
    private ViewPager m_backgroundPager;
}
