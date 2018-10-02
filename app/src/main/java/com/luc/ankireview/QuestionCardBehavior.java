package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class QuestionCardBehavior extends CoordinatorLayout.Behavior<QuestionCard> {
    private static final String TAG = "QuestionCardBehavior";

    public QuestionCardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "constructor");

    }

    /*
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.inner_scrollview) {
            return true;
        }
        return false;
    }
    */

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  QuestionCard child,
                                  int layoutDirection) {
        Log.v(TAG, "onLayoutChild");

        int totalHeight = parent.getHeight();
        int questionHeight = child.getHeight();

        int yPosition = totalHeight/2 - questionHeight/2;

        Log.v(TAG,"onLayoutChild, setting Y position to " + yPosition + " totalHeight: " + totalHeight);
        child.setY(yPosition);

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

        Log.v(TAG, "onStartNestedScroll ");
        return true;

    }

    @Override
    public boolean onNestedPreFling (CoordinatorLayout coordinatorLayout,
                                     QuestionCard child,
                              View target,
                              float velocityX,
                              float velocityY) {
        Log.v(TAG, "onNestedPreFling velocityY: " + velocityY);

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
        Log.v(TAG, "onNestedPreScroll ");
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
        Log.v(TAG, "onNestedScroll");

        // see if question and answer overlap

        int questionLocation[] = new int[2];
        int answerLocation[] = new int[2];
        child.getLocationOnScreen(questionLocation);
        target.findViewById(R.id.answer_card).getLocationOnScreen(answerLocation);

        int questionBottom = questionLocation[1] + child.getHeight();
        int answerTop = answerLocation[1];

        int diff = answerTop - questionBottom;

        if( diff < 0) {
            // adjust question Y by diff
            float originalY = child.getY();
            float newY = originalY + diff;
            child.setY(newY);
        }

    }


}
