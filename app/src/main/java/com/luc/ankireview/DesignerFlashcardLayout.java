package com.luc.ankireview;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luc.ankireview.style.CardStyle;

public class DesignerFlashcardLayout extends FrameLayout {

    private static final String TAG = "DesignerFlashcardLayout";

    public DesignerFlashcardLayout(Context context) {
        super(context);
        init(context);
    }

    public DesignerFlashcardLayout(Context context, Card card ) {
        super(context);
        init(context);
        setCard(card);
    }

    public DesignerFlashcardLayout(Context context,
                                   AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DesignerFlashcardLayout(Context context,
                                   AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        inflate(context, R.layout.flashcard, this);

        m_questionCard = findViewById(R.id.question_card);
        m_answerCard = findViewById(R.id.answer_card);

        m_activity = (CardStyleActivity) context;
    }

    public void setCard(Card card) {
        CardStyle cardStyle = m_activity.getCardStyle();
        cardStyle.renderCard(card, this);
    }


    // question and answer cards
    private QuestionCard m_questionCard;
    private AnswerCard m_answerCard;

    private CardStyleActivity m_activity;


}
