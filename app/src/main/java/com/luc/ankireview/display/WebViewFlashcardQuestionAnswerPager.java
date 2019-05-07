package com.luc.ankireview.display;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.luc.ankireview.Card;

public class WebViewFlashcardQuestionAnswerPager extends ViewPager {

    public WebViewFlashcardQuestionAnswerPager(@NonNull Context context, Card card) {
        super(context);

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
                        cardLayout = new WebViewFlashcardLayout(container.getContext(), m_card, false);
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

        this.setAdapter(m_pagerAdapter);
        this.setCurrentItem(1);

    }


    private Card m_card;
    private PagerAdapter m_pagerAdapter;

    private WebViewFlashcardLayout m_left = null;
    private WebViewFlashcardLayout m_center = null;
    private WebViewFlashcardLayout m_right = null;
}
