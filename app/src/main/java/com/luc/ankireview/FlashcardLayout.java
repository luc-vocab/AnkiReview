package com.luc.ankireview;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import static java.lang.Math.abs;

public class FlashcardLayout extends FrameLayout  implements View.OnTouchListener{
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


    private void init(Context context) {
        inflate(context, R.layout.flashcard, this);

        FrameLayout touchLayer = findViewById(R.id.flashcard_touch_layer);

        // set touch listener
        // m_detector = new GestureDetectorCompat(context, new FlashcardLayout.FlingGestureDetector());
        // touchLayer.setOnTouchListener(this);
        touchLayer.setOnTouchListener(this);


        m_questionCard = findViewById(R.id.question_card);
        m_answerCard = findViewById(R.id.answer_card);

        m_reviewActivity = (ReviewActivity) context;

    }

    public void setCard(Card card) {

        TextView questionText = this.findViewById(R.id.question_text);
        TextView answerText = this.findViewById(R.id.answer_text);

        questionText.setText(card.getQuestionSimple());
        answerText.setText(card.getAnswerSimple());

    }

    public void setSpringAnimation(int originalQuestionY, int questionTargetY, int answerTargetY) {

        m_questionOriginalY = originalQuestionY;
        m_questionRestingY = questionTargetY;

        m_questionSpringAnimation = new SpringAnimation(m_questionCard, DynamicAnimation.TRANSLATION_Y, questionTargetY);
        m_answerSpringAnimation = new SpringAnimation(m_answerCard, DynamicAnimation.TRANSLATION_Y, answerTargetY);

        m_questionSpringAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        m_answerSpringAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);

        // m_questionSpringAnimation.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);
        // m_answerSpringAnimation.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);


        m_questionSpringAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                questionAnimationDone();
            }
        });
        m_answerSpringAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                answerAnimationDone();
            }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Log.v(TAG, "onTouch");

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

                Log.v(TAG, "starting spring animations with velocity " + velocity);
                startSpringAnimations(velocity);

                break;
        }



        return true;
    }

    public void dragAnswerCard(float dy) {
        // move answer card by this much
        float originalAnswerY = m_answerCard.getY();
        m_answerCard.setY(originalAnswerY + dy);

        // do we need to move question card ?
        ViewGroup.MarginLayoutParams questionMarginParams = (ViewGroup.MarginLayoutParams) m_questionCard.getLayoutParams();
        ViewGroup.MarginLayoutParams answerMarginParams = (ViewGroup.MarginLayoutParams) m_answerCard.getLayoutParams();

        float questionToAnswerDistance = (m_answerCard.getY() - answerMarginParams.topMargin) - (m_questionCard.getY() + m_questionCard.getHeight() + questionMarginParams.bottomMargin);
        float questionToRestingDistance = m_questionOriginalY - m_questionCard.getY();

        if (questionToAnswerDistance < 0.0) {
            // answer card is overlapping with question card, need to drag it up
            float originalQuestionY = m_questionCard.getY();
            m_questionCard.setY(originalQuestionY + questionToAnswerDistance);
        } else {
            float originalQuestionY = m_questionCard.getY();

            if( m_animationsTriggeredOnce) {
                if (questionToRestingDistance > 1.0) { // only trigger when questionToAnswerDistance is above a certain threshold
                    // answer card is far away from question card, drag question card down
                    m_questionCard.setY(originalQuestionY + questionToAnswerDistance);
                }
            } else {
                if( questionToRestingDistance < 0.1) {
                    // allow dragging of the question card down, before animations are triggered
                    m_questionCard.setY(originalQuestionY + dy);
                }
            }
        }

    }

    public void startSpringAnimations(float velocity) {
        m_questionSpringAnimation.setStartVelocity(velocity);
        m_answerSpringAnimation.setStartVelocity(velocity);
        m_questionSpringAnimation.start();
        m_answerSpringAnimation.start();
    }

    protected void questionAnimationDone() {
        m_questionAnimationDone = true;
        checkAnimationsDone();
    }

    protected void answerAnimationDone() {
        m_answerAnimationDone = true;
        checkAnimationsDone();
    }

    private void checkAnimationsDone() {
        if( m_questionAnimationDone && m_answerAnimationDone ) {
            Log.v(TAG, "animations done");
            if( ! m_animationsTriggeredOnce) {
                m_reviewActivity.showAnswer();
            }
            m_animationsTriggeredOnce = true;
        }
    }

    // question and answer cards
    QuestionCard m_questionCard;
    AnswerCard m_answerCard;

    SpringAnimation m_questionSpringAnimation;
    SpringAnimation m_answerSpringAnimation;

    boolean m_animationsTriggeredOnce = false;

    boolean m_questionAnimationDone = false;
    boolean m_answerAnimationDone = false;

    // animation data
    int m_questionOriginalY; // Y position of question before answer is shown( centered)
    int m_questionRestingY; // resting Y position of question after animation (centered when taking into account combined height of question+answer)

    // track velocity of pointer
    private VelocityTracker m_velocityTracker;
    private float m_lastPointerY;



    // link back
    ReviewActivity m_reviewActivity;

}
