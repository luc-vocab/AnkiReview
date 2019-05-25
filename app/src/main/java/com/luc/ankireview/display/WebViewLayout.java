package com.luc.ankireview.display;

import android.content.Context;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.luc.ankireview.Card;
import com.luc.ankireview.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * show either the question or the answre
 */
public class WebViewLayout extends WebView {
    private static final String TAG = "WebViewLayout";

    public WebViewLayout(Context context, Card card, boolean showAnswer, final WebviewFlashcardLayout callOnRenderFinish) {
        super(context);

        // webview settings
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowFileAccessFromFileURLs(true);

        setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
                Log.e(TAG,"WebView error " + error.toString() + " request: " + request.getUrl());
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
                Log.v(TAG,"WebView loadresource: " +  url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.v(TAG, "WebView pagefinished " + url);
                if( !m_firstTimeInitDone) {
                    // reload
                    renderCard(m_showAnswer);
                    m_firstTimeInitDone = true;
                } else {
                    if(callOnRenderFinish != null) {
                        callOnRenderFinish.answerRenderingFinished();
                    }
                }
            }


        });

        m_card = card;
        m_showAnswer = showAnswer;
        try {
            m_cardTemplate = Utils.convertStreamToString(context.getAssets().open("card_template.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderCard(showAnswer);
    }

    private void renderCard(boolean showAnswer)
    {
        Spanned content;
        if(showAnswer) {
            content = commonContentProcessing(m_card.getAnswerContent(), true, 100, 100, false);
        } else {
            content = commonContentProcessing(m_card.getQuestionContent(), true, 100, 100, false);
        }
        //Log.v(TAG, "renderCard: content: " + content.toString());
        loadDataWithBaseURL(Utils.getBaseUrl(), content.toString(),"text/html", "utf-8", null);
    }

    private Spanned commonContentProcessing(String content, boolean prefCenterVertically, int cardZoom, int imageZoom, boolean nightMode)
    {

        // todo: add sound
        content = m_card.filterSound(content);

        // In order to display the bold style correctly, we have to change
        // font-weight to 700
        content = content.replace("font-weight:600;", "font-weight:700;");

        // CSS class for card-specific styling
        String cardClass = "card card" + (m_card.getCardOrd() + 1);

        if (prefCenterVertically) {
            cardClass += " vertically_centered";
        }

        Log.d("content card = \n %s", content);
        StringBuilder style = new StringBuilder();

        // Zoom cards
        if (cardZoom != 100) {
            style.append(String.format("body { zoom: %s }\n", cardZoom / 100.0));
        }

        // Zoom images
        if (imageZoom != 100) {
            style.append(String.format("img { zoom: %s }\n", imageZoom / 100.0));
        }

        Log.d("::style::", style.toString());

        // todo: enable night mode
        /*
        if (nightMode) {
            // Enable the night-mode class
            cardClass += " night_mode";
            // If card styling doesn't contain any mention of the night_mode class then do color inversion as fallback
            // TODO: find more robust solution that won't match unrelated classes like "night_mode_old"
            if (!getCard().css().contains(".night_mode")) {
                content = HtmlColors.invertColors(content);
            }
        }
        */

        content = smpToHtmlEntity(content);
        return(new SpannedString(m_cardTemplate.replace("::content::", content)
                .replace("::style::", style.toString()).replace("::class::", cardClass)));
    }

    private String smpToHtmlEntity(String text) {
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("([^\u0000-\uFFFF])").matcher(text);
        while (m.find()) {
            String a = "&#x" + Integer.toHexString(m.group(1).codePointAt(0)) + ";";
            m.appendReplacement(sb, Matcher.quoteReplacement(a));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private Card m_card;
    private boolean m_showAnswer;
    private String m_cardTemplate;
    private boolean m_firstTimeInitDone = false;
}
