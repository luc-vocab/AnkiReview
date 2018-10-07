package com.luc.ankireview;

import android.widget.TextView;

public class CardStyle {

    public void renderCard(Card card, FlashcardLayout layout, ReviewActivity reviewActivity) {
        TextView questionText = layout.findViewById(R.id.question_text);
        TextView answerText = layout.findViewById(R.id.answer_text);


        questionText.setText(card.getFieldValue("English"));
        answerText.setText(card.getFieldValue("Romanization") + " " + card.getFieldValue("Chinese"));

    }

    public String getAnswerAudio(Card card) {
        String soundFile = card.extractSoundFile(card.getFieldValue("Sound"));
        return soundFile;
    }

}
