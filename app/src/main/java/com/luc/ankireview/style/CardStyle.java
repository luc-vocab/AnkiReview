package com.luc.ankireview.style;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luc.ankireview.display.AnswerCard;
import com.luc.ankireview.Card;
import com.luc.ankireview.display.QuestionCard;
import com.luc.ankireview.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public void renderCard(Card card, ViewGroup layout) {

        // look for the card template
        CardTemplateKey templateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(templateKey);
        if(cardTemplate == null) {
            Log.e(TAG, "could not find cardtemplate for " + templateKey);
        }

        SpannableStringBuilder questionBuilder = buildString(cardTemplate.getQuestionCardFields(), card, layout);
        SpannableStringBuilder answerBuilder = buildString(cardTemplate.getAnswerCardFields(), card, layout);

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

        String font = cardTemplate.getFont();
        if( font != null && font.length() > 0) {
            if (m_fontCache.containsKey(font)) {
                 // we have it in the cache, apply directly
                questionText.setTypeface(m_fontCache.get(font));
                answerText.setTypeface(m_fontCache.get(font));
            } else {
                // need to request asynchronously
                requestTypeface(font, questionText, answerText);
            }
        }

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

    private void requestTypeface(final String fontRequested, final TextView questionText, final TextView answerText ) {
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

                questionText.setTypeface(typeface);
                answerText.setTypeface(typeface);
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

    private SpannableStringBuilder buildString(Vector<CardField> fields, Card card, ViewGroup layout) {
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
                    int leftMargin_px = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, cardField.getLeftMargin(), layout.getResources().getDisplayMetrics());
                    builder.setSpan(new LeadingMarginSpan.Standard(leftMargin_px),currentIndex, currentIndex + currentFieldLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public boolean styleExistsForCard(Card card) {
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);
        if (cardTemplate != null) {
            return true;
        }
        return false;
    }

    public String getAnswerAudio(Card card) {
        // get card template
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyleStorage.cardTemplateMap.get(cardTemplateKey);

        String soundField = cardTemplate.getSoundField();
        if(soundField != null) {
            String soundFile = card.extractSoundFile(card.getFieldValue(cardTemplate.getSoundField()));
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


    private HashMap<String,Typeface> m_fontCache;

    private Context m_context;
    private CardStyleStorage m_cardStyleStorage;

    // thread for downloading fonts
    private Handler m_handler = null;
}
