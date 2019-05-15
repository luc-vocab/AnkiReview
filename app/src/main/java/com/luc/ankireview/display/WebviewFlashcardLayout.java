package com.luc.ankireview.display;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.view.View;

import com.luc.ankireview.Card;
import com.luc.ankireview.R;
import com.luc.ankireview.ReviewActivity;

/**
 * Encapsulate the question and answer WebViews (equivalent to ReviewerFlashcardLayout)
 */
public class WebviewFlashcardLayout extends FrameLayout implements View.OnTouchListener {
    public WebviewFlashcardLayout(Context context) {
        super(context);
        init(context);
    }

    public WebviewFlashcardLayout(Context context, ReviewActivity reviewActivity, Card card) {
        super(context);
        m_card = card;
        init(context);
    }

    public WebviewFlashcardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WebviewFlashcardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public WebviewFlashcardLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    private void init(Context context) {
        inflate(context, R.layout.flashcard_webview, this);

        FrameLayout touchLayer = findViewById(R.id.flashcard_touch_layer);

        // set touch listener
        // m_detector = new GestureDetectorCompat(context, new FlashcardLayout.FlingGestureDetector());
        // touchLayer.setOnTouchListener(this);
        touchLayer.setOnTouchListener(this);

        FrameLayout questionFrame = findViewById(R.id.question_frame);
        WebViewLayout cardLayout = new WebViewLayout(context, m_card, false);
        questionFrame.addView(cardLayout);



        /*
        QuestionCardBehavior behavior = new QuestionCardBehavior();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) m_questionCard.getLayoutParams();
        layoutParams.setBehavior(behavior);

        m_reviewActivity = (ReviewActivity) context;
        */

    }


    private Card m_card;
}
