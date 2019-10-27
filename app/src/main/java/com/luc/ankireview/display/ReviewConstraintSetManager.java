package com.luc.ankireview.display;

import android.content.res.Resources;
import android.util.TypedValue;

import com.luc.ankireview.R;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class ReviewConstraintSetManager {
    public static float WIDTH = 0.9f;

    public static void applyConstraintSetConstants(MotionLayout reviewMotionLayout) {
        Resources res = reviewMotionLayout.getResources();

        ConstraintSet questionShownConstraintSet = reviewMotionLayout.getConstraintSet(R.id.question_shown);
        // questionShownConstraintSet.setMargin(R.id.question_card, ConstraintSet.TOP, dpToPx(40, res));

        setWidth(reviewMotionLayout.getConstraintSet(R.id.question_shown));
        setWidth(reviewMotionLayout.getConstraintSet(R.id.answer_shown));
        setWidth(reviewMotionLayout.getConstraintSet(R.id.answer_bad));
        setWidth(reviewMotionLayout.getConstraintSet(R.id.answer_good));
        setWidth(reviewMotionLayout.getConstraintSet(R.id.answer_good_offscreen));
        setWidth(reviewMotionLayout.getConstraintSet(R.id.answer_bad_offscreen));
    }

    public static void setWidth(ConstraintSet constraintSet) {
        constraintSet.constrainPercentWidth(R.id.question_card, WIDTH);
        constraintSet.constrainPercentWidth(R.id.answer_card, WIDTH);
        constraintSet.constrainPercentWidth(R.id.next_question_card, WIDTH);
    }

    public static int dpToPx(int dpDimension, Resources resources) {
        int dimension_px = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dpDimension, resources.getDisplayMetrics());
        return dimension_px;
    }

}
