package com.luc.ankireview;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;

public class FlashCardViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "FlashCardViewPagerAdapter";

    public FlashCardViewPagerAdapter(Context context, String baseUrl, ReviewActivity reviewActivity) {
        m_baseUrl = baseUrl;
        m_reviewActivity = reviewActivity;

        //m_baseUrl = "file:///sdcard/AnkiDroid/collection.media/";
        Log.v(TAG, "base url: " + m_baseUrl);

        try {
            m_cardTemplate = Utils.convertStreamToString(context.getAssets().open("card_template.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create all 3 webviews
        m_currentView = createWebView(context);
        m_prevView = createWebView(context);
        m_nextView = createWebView(context);

    }

    private WebView createWebView(Context context) {
        WebView webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);

        webView.setWebViewClient(new WebViewClient() {
                                    @Override
                                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
                                    {
                                        Log.e(TAG,"WebView error " + error.getDescription() + " request: " + request.getUrl());
                                    }

                                    @Override
                                    public void onLoadResource(WebView view, String url)
                                    {
                                        Log.v(TAG,"WebView loadresource: " +  url);
                                    }

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        Log.v(TAG, "WebView pagefinished " + url);
                                        m_reviewActivity.pageLoaded();
                                    }


        });

        return webView;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // add the correct webview to the viewgroup, based on position

        WebView card = null;

        switch( position)
        {
            case 0:
                card = m_prevView;
                break;
            case 1:
                card = m_currentView;
                break;
            case 2:
                card = m_nextView;
                break;
            default:
                break;
        }

        container.addView(card);

        return card;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        // we only ever have 3 pages
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    public void setCardContent(String cardContent, boolean isCenter) {

        // don't process an empty card (at the end we'll have empty cards)
        if( cardContent.length() > 0 )
            cardContent = processCardContent(cardContent);

        // the content in the center is the "main" item, the content on the sides is always the same, because we want to show
        // the same card, regardless of whether the user swipes left or right

        if (isCenter) {
            //Log.v(TAG, "baseUrl: " + m_baseUrl + " content: " + cardContent);
            m_currentView.loadDataWithBaseURL(m_baseUrl, cardContent, "text/html", "utf-8", null);
        }

        if (! isCenter) {
            m_prevView.loadDataWithBaseURL(m_baseUrl, cardContent, "text/html", "utf-8", null);
            m_nextView .loadDataWithBaseURL(m_baseUrl, cardContent, "text/html", "utf-8", null);
        }
    }

    private String processCardContent(String cardContent)
    {
        return m_cardTemplate.replace("::content::", cardContent).
                              replace("::style::", "").
                              replace("::class::", "card");
    }

    private String m_baseUrl;
    private String m_cardTemplate;

    private WebView m_prevView = null;
    private WebView m_currentView = null;
    private WebView m_nextView = null;

    private ReviewActivity m_reviewActivity;

}
