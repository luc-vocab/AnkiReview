package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class AnswerCardBehavior extends CoordinatorLayout.Behavior<NestedScrollView> {

    private static final String TAG = "AnswerCardBehavior";

    public AnswerCardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "constructor");
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency instanceof QuestionCard) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  NestedScrollView child,
                                  int layoutDirection) {
        Log.v(TAG, "onLayoutChild");
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        Log.v(TAG, "dependency.getY(): " + dependency.getY());
        return false;
    }

}
