package com.luc.ankireview;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Vector;

public class CardStyle {
    private static final String TAG = "CardStyle";

    public CardStyle(Context context) {

        m_cardTemplateMap = new HashMap<>();

        Typeface typeface = ResourcesCompat.getFont(context, R.font.fira_sans_condensed);

        // build CardTemplate for Chinese-Words
        // ------------------------------------

        CardTemplate cardTemplate = new CardTemplate();
        cardTemplate.setTypeface(typeface);
        // question
        CardField englishField = new CardField("English");
        englishField.setColor(ContextCompat.getColor(context, R.color.text_question));
        cardTemplate.addQuestionCardField(englishField);
        // answer
        CardField romanizationField = new CardField("Romanization");
        romanizationField.setColor(ContextCompat.getColor(context, R.color.text_romanization));
        cardTemplate.addAnswerCardField(romanizationField);
        CardField chineseField = new CardField("Chinese");
        chineseField.setColor(ContextCompat.getColor(context, R.color.text_cantonese));
        cardTemplate.addAnswerCardField(chineseField);

        CardTemplateKey key1 = new CardTemplateKey(1354424015761l, 0);
        CardTemplateKey key2 = new CardTemplateKey(1354424015760l, 0);
        CardTemplateKey key3 = new CardTemplateKey(1400993365602l, 0);

        m_cardTemplateMap.put(key1, cardTemplate);
        m_cardTemplateMap.put(key2, cardTemplate);
        m_cardTemplateMap.put(key3, cardTemplate);

        // build CardTemplate for Hanzi
        // ----------------------------

        cardTemplate = new CardTemplate();
        cardTemplate.setTypeface(typeface);

        // question
        CardField characterField = new CardField("Character");
        characterField.setColor(ContextCompat.getColor(context, R.color.text_question));
        characterField.setRelativeSize(3.0f);
        cardTemplate.addQuestionCardField(characterField);

        // answer
        CardField pinyinField = new CardField("Pinyin");
        pinyinField.setColor(ContextCompat.getColor(context, R.color.text_romanization));
        cardTemplate.addAnswerCardField(pinyinField);

        CardField cantoneseField = new CardField("Cantonese");
        cantoneseField.setColor(ContextCompat.getColor(context, R.color.text_cantonese));
        cardTemplate.addAnswerCardField(cantoneseField);

        CardField definitionField = new CardField("Definition");
        definitionField.setIsHtml(true);
        definitionField.setAlignment(Layout.Alignment.ALIGN_NORMAL);
        definitionField.setLeftMargin(120);
        definitionField.setRelativeSize(0.5f);
        definitionField.setLineReturn(true);
        cardTemplate.addAnswerCardField(definitionField);

        key1 = new CardTemplateKey(1423381647288l, 0);
        key2 = new CardTemplateKey(1423381647288l, 0);
        m_cardTemplateMap.put(key1, cardTemplate);
        m_cardTemplateMap.put(key2, cardTemplate);

    }

    public void renderCard(Card card, FrameLayout layout) {

        // look for the card template
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardTemplateMap.get(templateKey);
        if(cardTemplate == null) {
            Log.e(TAG, "could not find cardtemplate for " + templateKey);
        }

        SpannableStringBuilder questionBuilder = buildString(cardTemplate.getQuestionCardFields(), card);
        SpannableStringBuilder answerBuilder = buildString(cardTemplate.getAnswerCardFields(), card);

        TextView questionText = layout.findViewById(R.id.question_text);
        TextView answerText = layout.findViewById(R.id.answer_text);

        questionText.setTypeface(cardTemplate.getTypeface());
        answerText.setTypeface(cardTemplate.getTypeface());

        questionText.setText(questionBuilder, TextView.BufferType.SPANNABLE);
        answerText.setText(answerBuilder, TextView.BufferType.SPANNABLE);


    }

    private SpannableStringBuilder buildString(Vector<CardField> fields, Card card) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int currentIndex = 0;
        for( CardField cardField : fields) {
            CharSequence textValue = card.getFieldValue(cardField.getFieldName());

            if(cardField.getIsHtml()) {
                // convert from HTML
                textValue = removeTrailingLineReturns(Html.fromHtml(textValue.toString(), Html.FROM_HTML_MODE_LEGACY));
            }

            int currentFieldLength = textValue.length();
            if( currentFieldLength > 0) {
                if( currentIndex > 0) {
                    // not the first field, append a space or line return first
                    if (cardField.getLineReturn()) {
                        builder.append("\n");
                    } else {
                        builder.append(" ");
                    }
                    currentIndex += 1;
                }

                builder.append(textValue);
                if( cardField.getColor() != cardField.DEFAULT_COLOR ) {
                    builder.setSpan(new ForegroundColorSpan(cardField.getColor()), currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if(cardField.getRelativeSize() != cardField.RELATIVE_SIZE_DEFAULT) {
                    // add relative size span
                    builder.setSpan(new RelativeSizeSpan(cardField.getRelativeSize()),currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if(cardField.getAlignment() != cardField.DEFAULT_ALIGNMENT) {
                    // add alignment span
                    builder.setSpan(new AlignmentSpan.Standard(cardField.getAlignment()),currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (cardField.getLeftMargin() > 0)  {
                    // add left margin span
                    // new LeadingMarginSpan.Standard(120),
                    builder.setSpan(new LeadingMarginSpan.Standard(cardField.getLeftMargin()),currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                currentIndex += currentFieldLength;
            }
        }

        return builder;
    }


    private CharSequence removeTrailingLineReturns(CharSequence text) {

        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }
        return text;
    }

    public String getAnswerAudio(Card card) {
        String soundFile = card.extractSoundFile(card.getFieldValue("Sound"));
        return soundFile;
    }



    private HashMap<CardTemplateKey, CardTemplate> m_cardTemplateMap;

}
