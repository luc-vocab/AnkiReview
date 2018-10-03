package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class QuestionCardBehavior extends CoordinatorLayout.Behavior<QuestionCard>  {
    private static final String TAG = "QuestionCardBehavior";

    public QuestionCardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "constructor");

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, QuestionCard child, View dependency) {
        Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.inner_scrollview) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  QuestionCard child,
                                  int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);

        if( ! m_initialLayoutDone) {
            // center the question vertically

            int totalWindowHeight = parent.getHeight();
            int questionHeight = child.getHeight();

            int yPosition = totalWindowHeight/2 - questionHeight/2;

            Log.v(TAG,"onLayoutChild, setting Y position to " + yPosition + " totalHeight: " + totalWindowHeight);
            child.setY(yPosition);

            // place the answer just below the screen threshold
            FrameLayout topSpacer = (FrameLayout) parent.findViewById(R.id.answer_top_spacer);

            //LinearLayout.LayoutParams
            LinearLayout.LayoutParams topSpacerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalWindowHeight);
            //topSpacer.setMinimumHeight(totalWindowHeight);
            topSpacer.setLayoutParams(topSpacerLayoutParams);

            // calculate how much the answer can scroll up
            AnswerCard answer = parent.findViewById(R.id.answer_card);
            int answerHeight = answer.getHeight();

            // get margins
            ViewGroup.MarginLayoutParams questionMarginParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            ViewGroup.MarginLayoutParams answerMarginParams = (ViewGroup.MarginLayoutParams) answer.getLayoutParams();

            int combinedHeight = questionHeight + questionMarginParams.bottomMargin + answerMarginParams.topMargin + answerHeight;
            int bottomSpacerHeight = totalWindowHeight / 2 - combinedHeight / 2;


            FrameLayout bottomSpacer = (FrameLayout) parent.findViewById(R.id.answer_bottom_spacer);
            LinearLayout.LayoutParams bottomSpacerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomSpacerHeight);
            bottomSpacer.setLayoutParams(bottomSpacerLayoutParams);


            // subscribe to scrollview events
            NestedScrollView nestedScrollView = parent.findViewById(R.id.inner_scrollview);
            //nestedScrollView.setOnScrollChangeListener(this);

            m_initialLayoutDone = true;

            return true;
        }

        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, QuestionCard child, View dependency) {
        Log.v(TAG, "dependency.getY(): " + dependency.getY());
        return false;
    }


    @Override
    public boolean onStartNestedScroll (CoordinatorLayout coordinatorLayout,
                                        QuestionCard child,
                                        View directTargetChild,
                                        View target,
                                        int axes,
                                        int type) {

        // Log.v(TAG, "onStartNestedScroll ");

        // start listening to NestedScrollEvents

        return true;

    }

    @Override
    public boolean onNestedPreFling (CoordinatorLayout coordinatorLayout,
                                     QuestionCard child,
                              View target,
                              float velocityX,
                              float velocityY) {
        // Log.v(TAG, "onNestedPreFling velocityY: " + velocityY);

        if( ! m_acceptTouchEvents ) {
            // consume the fling
            return true;
        }

        return false;
    }


    @Override
    public void onNestedPreScroll (CoordinatorLayout coordinatorLayout,
                                   QuestionCard child,
                                   View target,
                                   int dx,
                                   int dy,
                                   int[] consumed,
                                   int type) {
        // Log.v(TAG, "onNestedPreScroll ");

        if( ! m_acceptTouchEvents ) {
            // consume the scroll
            consumed[1] = dy;
        }
    }

    @Override
    public void onNestedScroll (CoordinatorLayout coordinatorLayout,
                                QuestionCard child,
                                 View target,
                                 int dxConsumed,
                                 int dyConsumed,
                                 int dxUnconsumed,
                                 int dyUnconsumed,
                                 int type) {
        // Log.v(TAG, "onNestedScroll");

        // see if question and answer overlap

        int questionLocation[] = new int[2];
        int answerLocation[] = new int[2];
        child.getLocationOnScreen(questionLocation);
        View answer = target.findViewById(R.id.answer_card);
        answer.getLocationOnScreen(answerLocation);

        // get margins
        ViewGroup.MarginLayoutParams questionMarginParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        ViewGroup.MarginLayoutParams answerMarginParams = (ViewGroup.MarginLayoutParams) answer.getLayoutParams();

        int questionBottom = questionLocation[1] + child.getHeight() + questionMarginParams.bottomMargin;
        int answerTop = answerLocation[1] - answerMarginParams.topMargin;

        int diff = answerTop - questionBottom;

        if( diff < 0) {
            // adjust question Y by diff
            float originalY = child.getY();
            float newY = originalY + diff;
            child.setY(newY);

        }

    }

    public void stopScroll(Context context) {
        m_acceptTouchEvents = false;

        ReviewActivity reviewActivity = (ReviewActivity) context;
        reviewActivity.showAnswer();
    }

    /*
    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // Log.v(TAG, "onScrollChange scrollY: " + scrollY + " targetScrollY: " + m_targetScrollY);
        int targetScrollY = v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight();
        if( scrollY == targetScrollY ) {
            stopScroll(v.getContext());
        }
    }
    */


    private boolean m_initialLayoutDone = false;
    private boolean m_acceptTouchEvents = true;


}
