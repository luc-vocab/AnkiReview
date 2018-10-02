package com.luc.ankireview;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;

public class FlashCardViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "FlashCardViewPagerAdapter";

    public FlashCardViewPagerAdapter(Context context, ReviewActivity reviewActivity) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // add the correct webview to the viewgroup, based on position

        FlashcardLayout cardLayout = null;

        switch( position)
        {
            case 0:
                cardLayout = new FlashcardLayout(container.getContext(), m_nextCard);
                m_left = cardLayout;
                break;
            case 1:
                cardLayout = new FlashcardLayout(container.getContext(), m_currentCard);
                m_center = cardLayout;
                break;
            case 2:
                cardLayout = new FlashcardLayout(container.getContext(), m_nextCard);
                m_right = cardLayout;
                break;
            default:
                break;
        }

        container.addView(cardLayout);

        return cardLayout;
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


    public void setCurrentCard(Card card) {
        m_currentCard = card;
    }

    public void setNextCard(Card card) {
        m_nextCard = card;
    }


    FlashcardLayout m_left;
    FlashcardLayout m_right;
    FlashcardLayout m_center;

    Card m_currentCard;
    Card m_nextCard;

    private ReviewActivity m_reviewActivity;

}
