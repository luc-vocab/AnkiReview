package com.luc.ankireview.display;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.luc.ankireview.Card;
import com.luc.ankireview.ReviewActivity;
import com.luc.ankireview.display.ReviewerFlashcardLayout;

public class FlashCardViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "FlashCardViewPagerAdapter";

    public FlashCardViewPagerAdapter(Context context, ReviewActivity reviewActivity) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // add the correct webview to the viewgroup, based on position

        ReviewerFlashcardLayout cardLayout = null;

        switch( position)
        {
            case 0:
                cardLayout = new ReviewerFlashcardLayout(container.getContext(), m_nextCard);
                m_left = cardLayout;
                break;
            case 1:
                cardLayout = new ReviewerFlashcardLayout(container.getContext(), m_currentCard);
                m_center = cardLayout;
                break;
            case 2:
                cardLayout = new ReviewerFlashcardLayout(container.getContext(), m_nextCard);
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

    public void setCurrentCard(Card card) {
        m_currentCard = card;
    }

    public void setNextCard(Card card) {
        m_nextCard = card;
    }


    public void moveToNextQuestion(int currentPage, Card currentCard, Card nextCard) {
        m_currentCard = currentCard;
        m_nextCard = nextCard;

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


    ReviewerFlashcardLayout m_left;
    ReviewerFlashcardLayout m_right;
    ReviewerFlashcardLayout m_center;

    Card m_currentCard;
    Card m_nextCard;


}