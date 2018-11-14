package com.luc.ankireview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.luc.ankireview.style.CardStyle;

public class CardStyleFlashcardLayout extends FrameLayout {
    private static final String TAG = "CardStyleFlashcardLayout";

    public CardStyleFlashcardLayout(Context context) {
        super(context);
        init(context);
    }

    public CardStyleFlashcardLayout(Context context, Card card ) {
        super(context);
        init(context);
        setCard(card);
    }

    public CardStyleFlashcardLayout(Context context,
                                   AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardStyleFlashcardLayout(Context context,
                                   AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.flashcard, this);

        m_cardStyleActivity = (CardStyleActivity) context;

    }

    public void setCard(Card card) {
        CardStyle cardStyle = m_cardStyleActivity.getCardStyle();
        cardStyle.renderCard(card, this);
    }


    private CardStyleActivity m_cardStyleActivity;

}
