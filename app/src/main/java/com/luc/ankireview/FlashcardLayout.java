package com.luc.ankireview;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FlashcardLayout extends CoordinatorLayout {
    private static final String TAG = "FlashcardLayout";

    public FlashcardLayout(Context context) {
        super(context);
        init(context);
    }

    public FlashcardLayout(Context context, Card card ) {
        super(context);
        init(context);
        setCard(card);
    }

    public FlashcardLayout (Context context,
                       AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlashcardLayout(Context context,
                    AttributeSet attrs,
                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // need to return true to indicate interest in the event, otherwise the
            // remaining events won't be sent to us

            Log.v(TAG, "FlingGestureDetector.onDown");

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            startSpringAnimations();
            return true; // consume single tap event
        }

    }

    private View.OnTouchListener m_gestureListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // run through gesture detector
            if (m_detector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
    };

    private void init(Context context) {
        inflate(context, R.layout.flashcard, this);

        FrameLayout touchLayer = findViewById(R.id.flashcard_touch_layer);

        // set touch listener
        m_detector = new GestureDetectorCompat(context, new FlashcardLayout.FlingGestureDetector());
        touchLayer.setOnTouchListener(m_gestureListener);


        m_questionCard = findViewById(R.id.question_card);
        m_answerCard = findViewById(R.id.answer_card);

    }

    public void setCard(Card card) {

        TextView questionText = this.findViewById(R.id.question_text);
        TextView answerText = this.findViewById(R.id.answer_text);

        questionText.setText(card.getQuestionSimple());
        answerText.setText(card.getAnswerSimple());

    }

    public void setSpringAnimation(int questionTargetY, int answerTargetY) {

        m_questionSpringAnimation = new SpringAnimation(m_questionCard, DynamicAnimation.TRANSLATION_Y, questionTargetY);
        m_answerSpringAnimation = new SpringAnimation(m_answerCard, DynamicAnimation.TRANSLATION_Y, answerTargetY);

    }

    public void startSpringAnimations() {
        m_questionSpringAnimation.start();
        m_answerSpringAnimation.start();
    }

    // question and answer cards
    QuestionCard m_questionCard;
    AnswerCard m_answerCard;

    SpringAnimation m_questionSpringAnimation;
    SpringAnimation m_answerSpringAnimation;

    // gesture detection
    private GestureDetectorCompat m_detector;

}
