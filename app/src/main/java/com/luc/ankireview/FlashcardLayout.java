package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

public class FlashcardLayout extends CoordinatorLayout {

    public FlashcardLayout(Context context) {
        super(context);
        init(context);
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
    }
}
