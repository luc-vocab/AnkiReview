package com.luc.ankireview.display;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.luc.ankireview.Card;
import com.luc.ankireview.ReviewActivity;
import com.luc.ankireview.Settings;

public class WebViewFlashcardQuestionAnswerPager extends ViewPager {
    private static final String TAG = "WebViewFlashcardQuestionAnswerPager";

    public WebViewFlashcardQuestionAnswerPager(@NonNull Context context, ReviewActivity reviewActivity, Card card) {
        super(context);

        m_reviewActivity = reviewActivity;
        m_card = card;

        m_pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // add the correct webview to the viewgroup, based on position

                WebViewFlashcardLayout cardLayout = null;

                switch( position)
                {
                    case 0:
                        cardLayout = new WebViewFlashcardLayout(container.getContext(), m_card, true);
                        m_left = cardLayout;
                        break;
                    case 1:
                        cardLayout = new WebViewFlashcardLayout(container.getContext(), m_card, false);
                        m_center = cardLayout;
                        break;
                    case 2:
                        cardLayout = new WebViewFlashcardLayout(container.getContext(), m_card, true);
                        m_right = cardLayout;
                        break;
                    default:
                        break;
                }

                container.addView(cardLayout);

                return cardLayout;
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

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View)object);
            }

        };

        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if(ViewPager.SCROLL_STATE_IDLE == state){
                    //Scrolling finished. Do something.
                    if(mCurrentPosition == 0)
                    {
                        answerRevealed();
                    } else if(mCurrentPosition == 2)
                    {
                        answerRevealed();
                    }
                }
            }
            private int mCurrentPosition = 1;
        });

        this.setAdapter(m_pagerAdapter);
        this.setCurrentItem(1);

    }

    public void answerRevealed() {
        disableSwipe();
        m_reviewActivity.showAnswer();
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

    public void enableSwipe() {
        m_swipeEnabled = true;
    }

    public void disableSwipe() {
        m_swipeEnabled = false;
    }

    private ReviewActivity m_reviewActivity;
    private Card m_card;
    private PagerAdapter m_pagerAdapter;

    private WebViewFlashcardLayout m_left = null;
    private WebViewFlashcardLayout m_center = null;
    private WebViewFlashcardLayout m_right = null;

    private boolean m_swipeEnabled = true;
}
