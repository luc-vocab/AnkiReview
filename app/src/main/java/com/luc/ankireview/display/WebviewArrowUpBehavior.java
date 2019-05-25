package com.luc.ankireview.display;

import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luc.ankireview.R;

public class WebviewArrowUpBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private static final String TAG = "WebviewArrowUpBehavior";

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        // Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.answer_frame) {
            Log.v(TAG, "dependency on answer_frame");
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged (CoordinatorLayout parent,
                                    ImageView child,
                                    View dependency)
    {

        child.setY(dependency.getY() - child.getHeight());
        return true;

    }




}
