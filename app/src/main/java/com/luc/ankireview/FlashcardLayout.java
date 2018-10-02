package com.luc.ankireview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.TextView;

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

    public void setCard(Card card) {

        TextView questionText = this.findViewById(R.id.question_text);
        TextView answerText = this.findViewById(R.id.answer_text);

        questionText.setText(card.getQuestionSimple());
        answerText.setText(card.getAnswerSimple());

    }

}
