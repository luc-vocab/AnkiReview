package com.luc.ankireview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
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

        m_cardTemplateMap.put(key1, cardTemplate);
        m_cardTemplateMap.put(key2, cardTemplate);

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

    public void renderCard(Card card, FlashcardLayout layout) {

        // look for the card template
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardTemplateMap.get(templateKey);

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

    public void renderCardOld(Card card, FlashcardLayout layout, ReviewActivity reviewActivity) {
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
            answerBuilder.append(" ");
            answerBuilder.append(chinese);
            answerBuilder.setSpan(new ForegroundColorSpan(cantoneseColor),romanization.length() + 1, romanization.length() + chinese.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
            String definition = card.getFieldValue("Definition");


            questionBuilder.append(character);
            questionBuilder.setSpan(new ForegroundColorSpan(questionColor),0, character.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            questionBuilder.setSpan(new RelativeSizeSpan(3.0f),0, character.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            answerBuilder.append(pinyin);
            answerBuilder.setSpan(new ForegroundColorSpan(romanizationColor), 0, pinyin.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            answerBuilder.append(" ");
            answerBuilder.append(cantonese);
            answerBuilder.setSpan(new ForegroundColorSpan(cantoneseColor), pinyin.length() + 1, 1 + pinyin.length() + cantonese.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            answerBuilder.append("\n");
            Log.v(TAG, "definition: " + definition);
            CharSequence convertedDefinition = removeTrailingLineReturns(Html.fromHtml(definition, Html.FROM_HTML_MODE_LEGACY));
            answerBuilder.append(convertedDefinition);

            answerBuilder.setSpan(new RelativeSizeSpan(0.5f),
                                  2 + pinyin.length() + cantonese.length(),
                                  2 + pinyin.length() + cantonese.length() + convertedDefinition.length(),
                                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            answerBuilder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    2 + pinyin.length() + cantonese.length(),
                    2 + pinyin.length() + cantonese.length() + convertedDefinition.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            answerBuilder.setSpan(new LeadingMarginSpan.Standard(120),
                    2 + pinyin.length() + cantonese.length(),
                    2 + pinyin.length() + cantonese.length() + convertedDefinition.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        Typeface typeface = ResourcesCompat.getFont(layout.getContext(), R.font.fira_sans_condensed);

        questionText.setTypeface(typeface);
        answerText.setTypeface(typeface);

        questionText.setText(questionBuilder, TextView.BufferType.SPANNABLE);
        answerText.setText(answerBuilder, TextView.BufferType.SPANNABLE);


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
