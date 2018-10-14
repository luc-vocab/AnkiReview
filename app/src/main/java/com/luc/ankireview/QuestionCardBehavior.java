package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class QuestionCardBehavior extends CoordinatorLayout.Behavior<QuestionCard>  {
    private static final String TAG = "QuestionCardBehavior";

    public QuestionCardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "constructor");

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, QuestionCard child, View dependency) {
        // Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.answer_card) {
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

            // position the answer card outside of the screen

            AnswerCard answerCard = parent.findViewById(R.id.answer_card);
            answerCard.setY(totalWindowHeight);

            // calculate ending position after spring animation

            ViewGroup.MarginLayoutParams questionMarginParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            ViewGroup.MarginLayoutParams answerMarginParams = (ViewGroup.MarginLayoutParams) answerCard.getLayoutParams();

            int combinedHeight = (int)( child.getHeight() + questionMarginParams.bottomMargin + answerMarginParams.topMargin + answerCard.getHeight());
            int questionTargetY = totalWindowHeight/2 - combinedHeight/2;
            int answerTargetY = questionTargetY + child.getHeight() + questionMarginParams.bottomMargin + answerMarginParams.topMargin;

            if ( parent.getParent() instanceof  ReviewerFlashcardLayout) {
                ReviewerFlashcardLayout reviewerFlashcardLayout = (ReviewerFlashcardLayout) parent.getParent();
                reviewerFlashcardLayout.setSpringAnimation(yPosition, questionTargetY, answerTargetY);
            }

            m_initialLayoutDone = true;

            return true;
        }

        return false;
    }


    private boolean m_initialLayoutDone = false;


}
