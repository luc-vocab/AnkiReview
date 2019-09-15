package com.luc.ankireview.display;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.luc.ankireview.Card;
import com.luc.ankireview.ReviewActivity;

public class FlashCardViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "FlashCardViewPagerAdapter";

    public FlashCardViewPagerAdapter(Context context, ReviewActivity reviewActivity) {
        m_reviewActivity = reviewActivity;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // add the correct webview to the viewgroup, based on position

        View cardLayout = null;

        switch( position)
        {
            case 0:
                cardLayout = createCardView(container, m_nextCard, false);
                m_left = (FlashCardLayoutInterface) cardLayout;
                break;
            case 1:
                cardLayout = createCardView(container, m_currentCard, true);
                m_center = (FlashCardLayoutInterface) cardLayout;
                if(m_centerCardDisplayedQueued) {
                    m_centerCardDisplayedQueued = false;
                    m_center.isDisplayed();
                }
                break;
            case 2:
                cardLayout = createCardView(container, m_nextCard, false);
                m_right = (FlashCardLayoutInterface) cardLayout;
                break;
            default:
                break;
        }

        container.addView(cardLayout);

        return cardLayout;
    }

    private View createCardView(ViewGroup container, Card card, boolean isCenter) {
       View cardView;
        if(m_reviewActivity.useAnkiReviewStyle()) {
            cardView = new ReviewerFlashcardLayout(container.getContext(), card);
        } else {
            cardView = new WebviewFlashcardLayout(container.getContext(), m_reviewActivity, card, isCenter);
        }
        return cardView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        // we only ever have 3 pages

        int count = 0;
        if(m_currentCard != null) {
            count += 1;
        }
        if(m_nextCard != null) {
            count += 2;
        }

        return count;
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
        notifyDataSetChanged();
    }

    public void setNextCard(Card card) {

        m_nextCard = card;
        notifyDataSetChanged();
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

    public void centerCardDisplayed() {
        if(m_center == null) {
            m_centerCardDisplayedQueued = true;
        } else {
            m_center.isDisplayed();
        }
    }


    ReviewActivity m_reviewActivity;

    FlashCardLayoutInterface m_left;
    FlashCardLayoutInterface m_right;
    FlashCardLayoutInterface m_center;

    Card m_currentCard;
    Card m_nextCard;

    boolean m_centerCardDisplayedQueued = false;


}
