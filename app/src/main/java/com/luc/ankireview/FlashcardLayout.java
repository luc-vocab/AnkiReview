package com.luc.ankireview;

import android.content.Context;
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

    }

    public void setCard(Card card) {

        TextView questionText = this.findViewById(R.id.question_text);
        TextView answerText = this.findViewById(R.id.answer_text);

        questionText.setText(card.getQuestionSimple());
        answerText.setText(card.getAnswerSimple());

    }

    // gesture detection
    private GestureDetectorCompat m_detector;

}
