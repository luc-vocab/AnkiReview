package com.luc.ankireview.display;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.View;

import com.luc.ankireview.Card;
import com.luc.ankireview.R;
import com.luc.ankireview.ReviewActivity;

/**
 * Encapsulate the question and answer WebViews (equivalent to ReviewerFlashcardLayout)
 */
public class WebviewFlashcardLayout extends FrameLayout implements View.OnTouchListener, FlashCardLayoutInterface {
    private static final String TAG = "WebviewFlashcardLayout";

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


    private void init(Context context) {
        inflate(context, R.layout.flashcard_webview, this);

        // render question

        FrameLayout questionFrame = findViewById(R.id.question_frame);
        WebViewLayout questionCardLayout = new WebViewLayout(context, m_card, false);
        questionFrame.addView(questionCardLayout);


        // answer will be rendered only when this card is displayed.
        m_answerFrame= findViewById(R.id.answer_frame);

        WebviewCardBehavior behavior = new WebviewCardBehavior();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) questionFrame.getLayoutParams();
        layoutParams.setBehavior(behavior);

        m_reviewActivity = (ReviewActivity) context;

    }

    private void renderAnswer() {
        // setup touch listener
        FrameLayout touchLayer = findViewById(R.id.flashcard_touch_layer);
        touchLayer.setOnTouchListener(this);

        Log.v(TAG, "renderAnswer");
        WebViewLayout answerCardLayout = new WebViewLayout(m_reviewActivity, m_card, true);
        m_answerFrame.addView(answerCardLayout);
    }


    public void setSpringAnimation(int answerTargetY) {

        m_answerSpringAnimation = new SpringAnimation(m_answerFrame, DynamicAnimation.TRANSLATION_Y, answerTargetY);
        m_answerSpringAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);

        m_answerSpringAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                answerAnimationDone();
            }
        });

    }

    protected void answerAnimationDone() {
        if( ! m_animationsTriggeredOnce) {
            Log.v(TAG, "answerAnimationDone, showAnswer()");
            m_reviewActivity.showAnswer();
        }
        m_animationsTriggeredOnce = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        if(m_animationsTriggeredOnce) {
            return false;
        }

        Log.v(TAG, "onTouch");

        int index = event.getActionIndex();
        int action = event.getActionMasked();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(m_velocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the
                    // velocity of a motion.
                    m_velocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    m_velocityTracker.clear();
                }
                // Add  movement to the tracker.
                m_velocityTracker.addMovement(event);

                m_lastPointerY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                m_velocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                m_velocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                // Log.d(&#34;&#34;, &#34;X velocity: &#34; + mVelocityTracker.getXVelocity(pointerId));
                // Log.d(&#34;&#34;, &#34;Y velocity: &#34; + mVelocityTracker.getYVelocity(pointerId));

                float dy = event.getY() - m_lastPointerY;
                dragAnswerCard(dy);
                m_lastPointerY = event.getY();


                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.

                m_velocityTracker.computeCurrentVelocity(1000);
                float velocity = m_velocityTracker.getYVelocity();


                m_velocityTracker.recycle();
                m_velocityTracker = null;

                //Log.v(TAG, "starting spring animations with velocity " + velocity);
                startSpringAnimations(velocity);

                break;
        }



        return true;
    }

    public void dragAnswerCard(float dy) {
        // move answer card by this much
        float originalAnswerY = m_answerFrame.getY();
        m_answerFrame.setY(originalAnswerY + dy);

    }

    public void startSpringAnimations(float velocity) {
        m_answerSpringAnimation.setStartVelocity(velocity);
        m_answerSpringAnimation.start();
    }

    @Override
    public void isDisplayed() {
        Log.v(TAG, "isDisplayed");
        if( ! m_answerAdded ) {
            renderAnswer();
            m_answerAdded = true;
        }
    }

    private Card m_card;
    private ReviewActivity m_reviewActivity;

    FrameLayout m_answerFrame;

    SpringAnimation m_answerSpringAnimation;
    boolean m_animationsTriggeredOnce = false;

    // track velocity of pointer
    private VelocityTracker m_velocityTracker;
    private float m_lastPointerY;

    private boolean m_answerAdded = false;




}
