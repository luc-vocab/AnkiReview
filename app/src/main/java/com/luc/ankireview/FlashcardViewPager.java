package com.luc.ankireview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

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
        if( m_swipeEnabled)
            return super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if( m_swipeEnabled )
            return super.onInterceptTouchEvent(event);
        return false;
    }

    private boolean m_swipeEnabled = false;
}