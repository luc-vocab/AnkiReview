package com.luc.ankireview.style;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.luc.ankireview.Utils;
import com.luc.ankireview.Card;
import com.luc.ankireview.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

public class CardStyle implements Serializable {
    private static final String TAG = "CardStyle";
    public static final String CARDSTYLE_DATA_FILENAME = "cardstyle";

    public CardStyle(Context context) {

        m_context = context;
        m_fontCache = new HashMap<>();
        loadCardStyleData();

    }

    private Handler getHandlerThreadHandler() {
        if (m_handler == null) {
            HandlerThread handlerThread = new HandlerThread("fonts");
            handlerThread.start();
            m_handler = new Handler(handlerThread.getLooper());
        }
        return m_handler;
    }

    private void renderOneCard(Card card, Vector<CardField> cardFields, View cardView, TextView cardText, String placeholderContent) {
        // look for the card template (it should definitely exist at this point)
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(templateKey);

        SpannableStringBuilder stringBuilder = buildString(cardFields, card, cardView.getContext(), placeholderContent);

        int bottomMargin_dp = cardTemplate.getCenterMargin();
        int bottomMargin_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, bottomMargin_dp, cardView.getResources()
                        .getDisplayMetrics());

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cardView.getLayoutParams();
        layoutParams.setMargins(0, 0,  0, bottomMargin_px);
        cardView.setLayoutParams(layoutParams);


        String font = cardTemplate.getFont();
        if( font != null && font.length() > 0) {
            if (m_fontCache.containsKey(font)) {
                // we have it in the cache, apply directly
                cardText.setTypeface(m_fontCache.get(font));
            } else {
                // need to request asynchronously
                requestTypeface(font, cardText);
            }
        }

        cardText.setText(stringBuilder, TextView.BufferType.SPANNABLE);

        // compute text size
        int baseTextSize_dp = cardTemplate.getBaseTextSize();
        cardText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, baseTextSize_dp);

        // compute margins
        int paddingTop_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingTop(), cardView.getResources().getDisplayMetrics());
        int paddingBottom_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingBottom(), cardView.getResources().getDisplayMetrics());
        int paddingLeftRight_px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cardTemplate.getPaddingLeftRight(), cardView.getResources().getDisplayMetrics());

        cardText.setPadding(paddingLeftRight_px, paddingTop_px, paddingLeftRight_px, paddingBottom_px);
    }

    public void renderQuestion(Card card, View questionCard, TextView questionText) {
        // look for the card template (it should definitely exist at this point)
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(templateKey);

        renderOneCard(card, cardTemplate.getQuestionCardFields(), questionCard, questionText, questionCard.getContext().getString(R.string.card_style_question_empty));
    }

    public void renderBothCards(Card card, View questionCard, View answerCard, TextView questionText, TextView answerText) {

        // look for the card template (it should definitely exist at this point)
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(templateKey);

        renderOneCard(card, cardTemplate.getQuestionCardFields(), questionCard, questionText, questionCard.getContext().getString(R.string.card_style_question_empty));
        renderOneCard(card, cardTemplate.getAnswerCardFields(), answerCard, answerText, answerCard.getContext().getString(R.string.card_style_answer_empty));

    }

    private void requestTypeface(final String fontRequested, final TextView cardText) {
        // retrieve fonts

        Log.v(TAG, "requesting typeface " + fontRequested);

        FontRequest request = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                fontRequested,
                R.array.com_google_android_gms_fonts_certs);

        FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                Log.v(TAG, "retrieved typeface ");
                m_fontCache.put(fontRequested, typeface);

                cardText.setTypeface(typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                Log.e(TAG, "Typeface request for " + fontRequested + " failed: " +  reason);
                Toast toast = Toast.makeText(m_context, "Could not find font family " + fontRequested, Toast.LENGTH_LONG);
                toast.show();
            }
        };
        FontsContractCompat.requestFont(m_context, request, callback, getHandlerThreadHandler());
    }

    private SpannableStringBuilder buildString(Vector<CardField> fields, Card card, Context context, String placeholderContent) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (fields.size() == 0)
        {

            builder.append(placeholderContent);
            int currentFieldLength = placeholderContent.length();
            int color = ContextCompat.getColor(context, R.color.cardstyle_empty_color);
            builder.setSpan(new ForegroundColorSpan(color), 0, currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.5f), 0, currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {


            int currentIndex = 0;
            for (CardField cardField : fields) {
                CharSequence textValue = card.getFieldValue(cardField.getFieldName());

                // filter out sound
                textValue = Card.filterSound((String) textValue);

                if (cardField.getIsHtml()) {
                    // convert from HTML
                    textValue = removeTrailingLineReturns(Utils.fromHtml(textValue.toString()));
                }

                int currentFieldLength = textValue.length();
                if (currentFieldLength > 0) {
                    if (currentIndex > 0) {
                        // not the first field, append a space or line return first
                        if (cardField.getLineReturn()) {
                            builder.append("\n");
                        } else {
                            builder.append(" ");
                        }
                        currentIndex += 1;
                    }

                    builder.append(textValue);
                    if (cardField.getColor() != cardField.DEFAULT_COLOR) {
                        builder.setSpan(new ForegroundColorSpan(cardField.getColor()), currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (cardField.getRelativeSize() != cardField.RELATIVE_SIZE_DEFAULT) {
                        // add relative size span
                        builder.setSpan(new RelativeSizeSpan(cardField.getRelativeSize()), currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (cardField.getAlignment() != cardField.DEFAULT_ALIGNMENT) {
                        // add alignment span
                        builder.setSpan(new AlignmentSpan.Standard(cardField.getAlignment()), currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (cardField.getLeftMargin() > 0) {
                        // add left margin span
                        // new LeadingMarginSpan.Standard(120),
                        int leftMargin_px = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, cardField.getLeftMargin(), context.getResources().getDisplayMetrics());
                        builder.setSpan(new LeadingMarginSpan.Standard(leftMargin_px), currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    currentIndex += currentFieldLength;
                }
            }
        }

        return builder;
    }


    private CharSequence removeTrailingLineReturns(CharSequence text) {
        if( text.length() > 0 ) {
            while (text.charAt(text.length() - 1) == '\n') {
                text = text.subSequence(0, text.length() - 1);
            }
        }
        return text;
    }

    public boolean styleExistsForCard(Card card) {
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);
        if (cardTemplate != null) {
            return true;
        }
        return false;
    }

    public String getQuestionAudio(long deckId, Card card) {
        if( ! useAnkiReviewDeckDisplayMode(deckId) ) {
            String soundFile = card.extractSoundFile(card.getQuestionContent());
            return soundFile;
        }

        // get card template
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);

        String soundField = cardTemplate.getQuestionSoundField();
        if(soundField != null) {
            String soundFile = card.extractSoundFile(card.getFieldValue(soundField));
            return soundFile;
        }
        return null;
    }

    public String getAnswerAudio(long deckId, Card card) {
        if( ! useAnkiReviewDeckDisplayMode(deckId) ) {
            String soundFile = card.extractSoundFile(card.getAnswerContent());
            return soundFile;
        }

        // get card template
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);

        String soundField = cardTemplate.getAnswerSoundField();
        if(soundField != null) {
            String soundFile = card.extractSoundFile(card.getFieldValue(soundField));
            return soundFile;
        }
        return null;
    }


    public CardTemplate createCardTemplate(CardTemplateKey cardTemplateKey) {
        CardTemplate cardTemplate = new CardTemplate();
        m_cardStyleStorage.cardTemplateMap.put(cardTemplateKey, cardTemplate);
        return cardTemplate;
    }

    public CardTemplate getCardTemplate(CardTemplateKey cardTemplateKey)
    {
        return m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);
    }

    public void loadCardStyleData() {
        Log.v(TAG, "loading card data from " + CARDSTYLE_DATA_FILENAME);
        try {
            FileInputStream fis = m_context.openFileInput(CARDSTYLE_DATA_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            m_cardStyleStorage = (CardStyleStorage) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "could not open cardstylestorage, creating new");
            m_cardStyleStorage = new CardStyleStorage();
            m_cardStyleStorage.cardTemplateMap = new HashMap<CardTemplateKey, CardTemplate>();
            m_cardStyleStorage.deckDisplayMode = new HashMap<Long,Boolean>();
        }
    }

    public void saveCardStyleData()
    {

        try {
            FileOutputStream fos = m_context.openFileOutput(CARDSTYLE_DATA_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(m_cardStyleStorage);
            os.close();
            fos.close();
            Log.v(TAG, "saved card style to file " + CARDSTYLE_DATA_FILENAME);
            Toast toast = Toast.makeText(m_context, "Saved Card Style", Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();

            Toast toast = Toast.makeText(m_context, "Could not save Card Style", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    public void chooseDeckDisplayMode(long deckId, boolean useAnkiReviewStyle) {
        Log.v(TAG, "chooseDeckDisplayMode, useAnkiReviewStyle: " + useAnkiReviewStyle);
        m_cardStyleStorage.deckDisplayMode.put(deckId, useAnkiReviewStyle);
        saveCardStyleData();
    }

    public boolean deckDisplayModeConfigured(long deckId) {
        if( m_cardStyleStorage.deckDisplayMode.containsKey(deckId) ) {
            return true;
        }
        return false;
    }

    public boolean useAnkiReviewDeckDisplayMode(long deckId) {
        boolean result = false;
        if (m_cardStyleStorage.deckDisplayMode.containsKey(deckId)) {
            result = m_cardStyleStorage.deckDisplayMode.get(deckId).booleanValue();
        }
        return result;
    }

    private HashMap<String,Typeface> m_fontCache;

    private Context m_context;
    private CardStyleStorage m_cardStyleStorage;

    // thread for downloading fonts
    private Handler m_handler = null;
}
