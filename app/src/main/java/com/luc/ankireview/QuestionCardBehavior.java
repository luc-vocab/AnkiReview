package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class QuestionCardBehavior extends CoordinatorLayout.Behavior<CardView> {
    private static final String TAG = "QuestionCardBehavior";

    public QuestionCardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "constructor");
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CardView child, View dependency) {
        Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency instanceof NestedScrollView) {
            return true;
        }

        /*
        if( dependency.getId() == R.id.answer_card ) {
            Log.v(TAG, "found AnswerCard");
            return true;
        }
        */
        /*
        if( dependency instanceof  AnswerCard) {
            Log.v(TAG, "found AnswerCard");
            return true;
        }*/
        return false;
    }

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  CardView child,
                                  int layoutDirection) {
        Log.v(TAG, "onLayoutChild");
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CardView child, View dependency) {
        Log.v(TAG, "dependency.getY(): " + dependency.getY());
        return false;
    }


    @Override
    public boolean onStartNestedScroll (CoordinatorLayout coordinatorLayout,
                                 CardView child,
                                 View directTargetChild,
                                 View target,
                                 int axes,
                                 int type) {

        Log.v(TAG, "onStartNestedScroll");
        return true;

    }

    @Override
    public boolean onNestedPreFling (CoordinatorLayout coordinatorLayout,
                              CardView child,
                              View target,
                              float velocityX,
                              float velocityY) {
        Log.v(TAG, "onNestedPreFling");
        return false;
    }

}
