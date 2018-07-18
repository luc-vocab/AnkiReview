package com.luc.ankireview;

import android.content.Context;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class FlashCardViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "FlashCardViewPagerAdapter";

    public FlashCardViewPagerAdapter(Context context) {
        m_baseUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AnkiDroid/collection.media/";

        // create all 3 webviews
        m_currentView = createWebView(context);
        m_prevView = createWebView(context);
        m_nextView = createWebView(context);

    }

    private WebView createWebView(Context context) {
        WebView webView = new WebView(context);
        webView.loadDataWithBaseURL(m_baseUrl + "__viewer__.html", "", "text/html", "utf-8", null);
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

        // the content in the center is the "main" item, the content on the sides is always the same, because we want to show
        // the same card, regardless of whether the user swipes left or right

        if (isCenter) {
            m_currentView.loadDataWithBaseURL(m_baseUrl + "__viewer__.html", cardContent, "text/html", "utf-8", null);
        }

        if (! isCenter) {
            m_prevView.loadDataWithBaseURL(m_baseUrl + "__viewer__.html", cardContent, "text/html", "utf-8", null);
            m_nextView .loadDataWithBaseURL(m_baseUrl + "__viewer__.html", cardContent, "text/html", "utf-8", null);
        }
    }

    private String m_baseUrl;


    private WebView m_prevView = null;
    private WebView m_currentView = null;
    private WebView m_nextView = null;
}
