package com.luc.ankireview.style;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luc.ankireview.AnswerCard;
import com.luc.ankireview.Card;
import com.luc.ankireview.QuestionCard;
import com.luc.ankireview.R;

import java.util.HashMap;
import java.util.Vector;

public class CardStyle {
    private static final String TAG = "CardStyle";

    public CardStyle(Context context) {

        m_cardTemplateMap = new HashMap<>();

        Typeface typeface = ResourcesCompat.getFont(context, R.font.fira_sans_condensed);

        // build CardTemplate for Chinese-Words
        // ====================================

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
        // set sound field
        cardTemplate.setSoundField("Sound");

        // text
        // ----
        cardTemplate.setBaseTextSize(40);

        // margins
        // -------
        cardTemplate.setCenterMargin(20);
        cardTemplate.setLeftRightMargin(40);

        cardTemplate.setPaddingTop(20);
        cardTemplate.setPaddingBottom(30);
        cardTemplate.setPaddingLeftRight(15);

        CardTemplateKey key1 = new CardTemplateKey(1354424015761l, 0);
        CardTemplateKey key2 = new CardTemplateKey(1354424015760l, 0);
        CardTemplateKey key3 = new CardTemplateKey(1400993365602l, 0);

        m_cardTemplateMap.put(key1, cardTemplate);
        m_cardTemplateMap.put(key2, cardTemplate);
        m_cardTemplateMap.put(key3, cardTemplate);

        // build CardTemplate for Hanzi
        // ============================

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

        // set sound field
        cardTemplate.setSoundField("Sound");

        // text
        // ----
        cardTemplate.setBaseTextSize(40);

        // margins
        // -------
        cardTemplate.setCenterMargin(20);
        cardTemplate.setLeftRightMargin(40);

        cardTemplate.setPaddingTop(20);
        cardTemplate.setPaddingBottom(30);
        cardTemplate.setPaddingLeftRight(15);

        key1 = new CardTemplateKey(1423381647288l, 0);
        key2 = new CardTemplateKey(1423381647288l, 0);
        m_cardTemplateMap.put(key1, cardTemplate);
        m_cardTemplateMap.put(key2, cardTemplate);

    }

    public void renderCard(Card card, ViewGroup layout) {

        // look for the card template
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardTemplateMap.get(templateKey);
        if(cardTemplate == null) {
            Log.e(TAG, "could not find cardtemplate for " + templateKey);
        }

        SpannableStringBuilder questionBuilder = buildString(cardTemplate.getQuestionCardFields(), card);
        SpannableStringBuilder answerBuilder = buildString(cardTemplate.getAnswerCardFields(), card);

        QuestionCard questionCard = layout.findViewById(R.id.question_card);
        AnswerCard answerCard = layout.findViewById(R.id.answer_card);



        int leftRightMargin_dp = cardTemplate.getLeftRightMargin();
        int leftRightMargin_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, leftRightMargin_dp, layout.getResources()
                        .getDisplayMetrics());

        int bottomMargin_dp = cardTemplate.getCenterMargin();
        int bottomMargin_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, bottomMargin_dp, layout.getResources()
                        .getDisplayMetrics());

        if( questionCard.getLayoutParams() instanceof  CoordinatorLayout.LayoutParams ) {
            CoordinatorLayout.LayoutParams questionLayoutParams = (CoordinatorLayout.LayoutParams) questionCard.getLayoutParams();
            questionLayoutParams.setMargins(leftRightMargin_px, 0,  leftRightMargin_px, bottomMargin_px);
            questionCard.setLayoutParams(questionLayoutParams);

            CoordinatorLayout.LayoutParams answerLayoutParams = (CoordinatorLayout.LayoutParams) answerCard.getLayoutParams();
            answerLayoutParams.setMargins(leftRightMargin_px, 0,  leftRightMargin_px, bottomMargin_px);
            answerCard.setLayoutParams(answerLayoutParams);
        } else if ( questionCard.getLayoutParams() instanceof LinearLayout.LayoutParams )
        {
            LinearLayout.LayoutParams questionLayoutParams = (LinearLayout.LayoutParams) questionCard.getLayoutParams();
            questionLayoutParams.setMargins(leftRightMargin_px, 20,  leftRightMargin_px, bottomMargin_px);
            questionCard.setLayoutParams(questionLayoutParams);

            LinearLayout.LayoutParams answerLayoutParams = (LinearLayout.LayoutParams) answerCard.getLayoutParams();
            answerLayoutParams.setMargins(leftRightMargin_px, 0,  leftRightMargin_px, bottomMargin_px);
            answerCard.setLayoutParams(answerLayoutParams);
        }



        TextView questionText = layout.findViewById(R.id.question_text);
        TextView answerText = layout.findViewById(R.id.answer_text);

        questionText.setTypeface(cardTemplate.getTypeface());
        answerText.setTypeface(cardTemplate.getTypeface());

        questionText.setText(questionBuilder, TextView.BufferType.SPANNABLE);
        answerText.setText(answerBuilder, TextView.BufferType.SPANNABLE);

        // compute text size
        int baseTextSize_dp = cardTemplate.getBaseTextSize();
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, baseTextSize_dp);
        answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, baseTextSize_dp);

        // compute margins
        int paddingTop_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingTop(), layout.getResources().getDisplayMetrics());
        int paddingBottom_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingBottom(), layout.getResources().getDisplayMetrics());
        int paddingLeftRight_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingLeftRight(), layout.getResources().getDisplayMetrics());

        questionText.setPadding(paddingLeftRight_px, paddingTop_px, paddingLeftRight_px, paddingBottom_px);
        answerText.setPadding(paddingLeftRight_px, paddingTop_px, paddingLeftRight_px, paddingBottom_px);


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
        // get card template
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardTemplateMap.get(cardTemplateKey);

        String soundFile = card.extractSoundFile(card.getFieldValue(cardTemplate.getSoundField()));
        return soundFile;
    }


    public CardTemplate getCardTemplate(CardTemplateKey cardTemplateKey)
    {
        return m_cardTemplateMap.get(cardTemplateKey);
    }


    private HashMap<CardTemplateKey, CardTemplate> m_cardTemplateMap;

}
