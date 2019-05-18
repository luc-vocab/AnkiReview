package com.luc.ankireview.display;

import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.luc.ankireview.R;

public class WebviewCardBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    private static final String TAG = "WebviewCardBehavior";

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        // Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.answer_frame) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  FrameLayout child,
                                  int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);


        if( ! m_initialLayoutDone) {

            // center the question vertically
            if ( parent.getParent() instanceof WebviewFlashcardLayout) {

                int totalWindowHeight = parent.getHeight();

                // position the answer card outside of the screen

                FrameLayout answerFrame = parent.findViewById(R.id.answer_frame);
                answerFrame.setY(totalWindowHeight);

                /*
                ReviewerFlashcardLayout reviewerFlashcardLayout = (ReviewerFlashcardLayout) parent.getParent();
                reviewerFlashcardLayout.setSpringAnimation(yPosition, questionTargetY, answerTargetY);
                */
            }

            m_initialLayoutDone = true;

            return true;
        }


        return false;
    }


    private boolean m_initialLayoutDone = false;

}
