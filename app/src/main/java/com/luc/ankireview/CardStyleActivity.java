package com.luc.ankireview;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.databinding.DataBindingUtil;

import com.luc.ankireview.databinding.CardFieldEditorBinding;
import com.luc.ankireview.style.CardField;
import com.luc.ankireview.style.CardStyle;
import com.luc.ankireview.style.CardTemplate;
import com.luc.ankireview.style.CardTemplateKey;
import com.luc.ankireview.databinding.CardFieldItemBinding;
import com.luc.ankireview.style.FieldListAdapter;
import com.luc.ankireview.style.ItemTouchCallback;
import com.warkiz.widget.Indicator;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnSeekChangeListener {
    private static final String TAG = "CardStyleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardstyle);


        m_cardStyle = new CardStyle(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.review_toolbar);
        toolbar.setTitle(R.string.card_style);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        long noteId = intent.getLongExtra("noteId", 0l);
        int cardOrd = intent.getIntExtra("cardOrd", 0);

        Log.v(TAG, "starting CardStyleActivity with noteId: " + noteId + " cardOrd: " + cardOrd);

        // retrieve the appropriate card
        m_card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);

        // retrieve the card template
        m_cardTemplateKey = new CardTemplateKey(m_card.getModelId(), m_card.getCardOrd());
        m_cardTemplate = m_cardStyle.getCardTemplate(m_cardTemplateKey);

        Log.v(TAG, "num question card fields: " + m_cardTemplate.getQuestionCardFields().size());

        m_cardstyleTabs = findViewById(R.id.cardstyle_tabs);
        m_cardstyleTabs.addOnTabSelectedListener(this);



        m_cardstyleEditorCards = findViewById(R.id.cardstyle_editor_cards);
        m_cardStyle.renderCard(m_card, m_cardstyleEditorCards);

        // get font view and margins view
        m_fontView = findViewById(R.id.cardstyle_editor_font);
        m_marginsView = findViewById(R.id.cardstyle_editor_margins);

        // setup the full Field list ListView
        m_fullFieldListView = findViewById(R.id.cardstyle_editor_all_fields);
        m_fullFieldListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        m_fullFieldListView.setLayoutManager(linearLayoutManager);

        // get field list
        Vector<String> fullFieldList = new Vector<String>();
        for(String field: m_card.getFieldMap().keySet())
        {
            fullFieldList.add(field);
        }
        m_fieldListAdapter = new FieldListAdapter(this, m_cardTemplate, fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);

        ItemTouchCallback itemTouchCallback = new ItemTouchCallback(m_fieldListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchCallback);
        m_fieldListAdapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(m_fullFieldListView);

        // set visibility of tabs
        m_fullFieldListView.setVisibility(View.VISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);

        // text controls
        // -------------
        m_text_basesize_isb = findViewById(R.id.cardstyle_text_basesize_isb);
        m_text_basesize_isb.setOnSeekChangeListener(this);
        m_text_basesize_isb.setProgress(m_cardTemplate.getBaseTextSize());

        // margin controls
        // ---------------

        m_margin_leftright_isb = findViewById(R.id.cardstyle_margin_leftright_isb);
        m_margin_leftright_isb.setProgress(m_cardTemplate.getLeftRightMargin());

        m_margin_center_isb = findViewById(R.id.cardstyle_margin_center_isb);
        m_margin_center_isb.setProgress(m_cardTemplate.getCenterMargin());

        m_padding_top_isb = findViewById(R.id.cardstyle_padding_top_isb);
        m_padding_top_isb.setProgress(m_cardTemplate.getPaddingTop());
        m_padding_bottom_isb = findViewById(R.id.cardstyle_padding_bottom_isb);
        m_padding_bottom_isb.setProgress(m_cardTemplate.getPaddingBottom());
        m_padding_leftright_isb = findViewById(R.id.cardstyle_padding_leftright_isb);
        m_padding_leftright_isb.setProgress(m_cardTemplate.getPaddingLeftRight());

        m_margin_leftright_isb.setOnSeekChangeListener(this);
        m_margin_center_isb.setOnSeekChangeListener(this);
        m_padding_top_isb.setOnSeekChangeListener(this);
        m_padding_bottom_isb.setOnSeekChangeListener(this);
        m_padding_leftright_isb.setOnSeekChangeListener(this);

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        Log.v(TAG, "onTabSelected " + tab.getPosition());

        if( tab.getPosition() == 0 ) {
            m_fullFieldListView.setVisibility(View.VISIBLE);
            m_fontView.setVisibility(View.INVISIBLE);
            m_marginsView.setVisibility(View.INVISIBLE);
        } else if( tab.getPosition() == 1 ) {
            m_fullFieldListView.setVisibility(View.INVISIBLE);
            m_fontView.setVisibility(View.VISIBLE);
            m_marginsView.setVisibility(View.INVISIBLE);
        } else if( tab.getPosition() == 2 ) {
            m_fullFieldListView.setVisibility(View.INVISIBLE);
            m_fontView.setVisibility(View.INVISIBLE);
            m_marginsView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onSeeking(SeekParams seekParams) {
        if( seekParams.seekBar == m_margin_leftright_isb) {
            Log.v(TAG, "left/right margin: " + seekParams.progress);
            m_cardTemplate.setLeftRightMargin(seekParams.progress);
        } else if( seekParams.seekBar == m_margin_center_isb ) {
            Log.v(TAG, "left/right margin: " + seekParams.progress);
            m_cardTemplate.setCenterMargin(seekParams.progress);
        } else if ( seekParams.seekBar == m_text_basesize_isb) {
            m_cardTemplate.setBaseTextSize(seekParams.progress);
        } else if ( seekParams.seekBar == m_padding_bottom_isb) {
            m_cardTemplate.setPaddingBottom(seekParams.progress);
        } else if ( seekParams.seekBar == m_padding_top_isb) {
            m_cardTemplate.setPaddingTop(seekParams.progress);
        } else if ( seekParams.seekBar == m_padding_leftright_isb) {
            m_cardTemplate.setPaddingLeftRight(seekParams.progress);
        }
        updateCardPreview();
    }

    @Override
    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
    }

    public void updateCardPreview() {
        m_cardStyle.renderCard(m_card, m_cardstyleEditorCards);
    }

    public CardStyle getCardStyle() {
        return m_cardStyle;
    }

    private CardStyle m_cardStyle;

    private FrameLayout m_fontView;
    private FrameLayout m_marginsView;

    // views
    private RecyclerView m_fullFieldListView;
    private FieldListAdapter m_fieldListAdapter;

    private TabLayout m_cardstyleTabs;

    private LinearLayout m_cardstyleEditorCards;


    // text controls
    private IndicatorSeekBar m_text_basesize_isb;

    // margin controls
    private IndicatorSeekBar m_margin_leftright_isb;
    private IndicatorSeekBar m_margin_center_isb;

    private IndicatorSeekBar m_padding_top_isb;
    private IndicatorSeekBar m_padding_bottom_isb;
    private IndicatorSeekBar m_padding_leftright_isb;


    private CardTemplateKey m_cardTemplateKey;
    private CardTemplate m_cardTemplate;
    private Card m_card;

}
