package com.luc.ankireview;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.widget.TextView;

public class CardStyle {
    private static final String TAG = "CardStyle";

    public void renderCard(Card card, FlashcardLayout layout, ReviewActivity reviewActivity) {
        TextView questionText = layout.findViewById(R.id.question_text);
        TextView answerText = layout.findViewById(R.id.answer_text);

        Log.v(TAG, "modelId: " + card.getModelId());
        SpannableStringBuilder questionBuilder = new SpannableStringBuilder();
        SpannableStringBuilder answerBuilder = new SpannableStringBuilder();

        if( (card.getModelId() == 1354424015761l || card.getModelId() == 1354424015760l) && card.getCardOrd() == 0) {
            // Chinese Words

            int questionColor = ContextCompat.getColor(layout.getContext(), R.color.text_question);
            int romanizationColor = ContextCompat.getColor(layout.getContext(), R.color.text_romanization);
            int chineseColor = ContextCompat.getColor(layout.getContext(), R.color.text_chinese);
            int cantoneseColor = ContextCompat.getColor(layout.getContext(), R.color.text_cantonese);

            // question fields
            String englishText = card.getFieldValue("English");
            questionBuilder.append(englishText);
            questionBuilder.setSpan(new ForegroundColorSpan(questionColor),0, englishText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // answer fields
            String romanization = card.getFieldValue("Romanization");
            String chinese = card.getFieldValue("Chinese");

            answerBuilder.append(romanization);
            answerBuilder.setSpan(new ForegroundColorSpan(romanizationColor),0, romanization.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            answerBuilder.append(chinese);
            answerBuilder.setSpan(new ForegroundColorSpan(cantoneseColor),romanization.length(), romanization.length() + chinese.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        if( (card.getModelId() == 1423381647288l || card.getModelId() == 1423381647288l) && card.getCardOrd() == 0) {
            // Hanzi

            int questionColor = ContextCompat.getColor(layout.getContext(), R.color.text_question);
            int romanizationColor = ContextCompat.getColor(layout.getContext(), R.color.text_romanization);
            int chineseColor = ContextCompat.getColor(layout.getContext(), R.color.text_chinese);
            int cantoneseColor = ContextCompat.getColor(layout.getContext(), R.color.text_cantonese);

            String character = card.getFieldValue("Character");
            String pinyin = card.getFieldValue("Pinyin");
            String cantonese = card.getFieldValue("Cantonese");

            questionBuilder.append(character);
            questionBuilder.setSpan(new ForegroundColorSpan(questionColor),0, character.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            questionBuilder.setSpan(new RelativeSizeSpan(3.0f),0, character.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            answerBuilder.append(pinyin);
            answerBuilder.setSpan(new ForegroundColorSpan(romanizationColor),0, pinyin.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            answerBuilder.append(" ");
            answerBuilder.append(cantonese);
            answerBuilder.setSpan(new ForegroundColorSpan(cantoneseColor), pinyin.length() + 1, 1 + pinyin.length() + cantonese.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        questionText.setText(questionBuilder, TextView.BufferType.SPANNABLE);
        answerText.setText(answerBuilder, TextView.BufferType.SPANNABLE);


    }

    public String getAnswerAudio(Card card) {
        String soundFile = card.extractSoundFile(card.getFieldValue("Sound"));
        return soundFile;
    }

}
