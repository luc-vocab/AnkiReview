package com.luc.ankireview.display;

import android.content.res.Resources;
import android.util.TypedValue;

import com.luc.ankireview.R;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class ReviewConstraintSetManager {

    public static void applyConstraintSetConstants(MotionLayout reviewMotionLayout) {
        ConstraintSet questionShownConstraintSet = reviewMotionLayout.getConstraintSet(R.id.question_shown);
        questionShownConstraintSet.setMargin(R.id.question_card, ConstraintSet.TOP, dpToPx(40, reviewMotionLayout.getResources()));
    }

    public static int dpToPx(int dpDimension, Resources resources) {
        int dimension_px = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dpDimension, resources.getDisplayMetrics());
        return dimension_px;
    }

}
