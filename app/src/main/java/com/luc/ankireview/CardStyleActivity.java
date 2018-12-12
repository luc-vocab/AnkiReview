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
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity {
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

        m_designerFlashcardLayout = findViewById(R.id.cardstyle_editor_card_preview);
        m_designerFlashcardLayout.setCard(m_card);

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
        m_fieldListAdapter = new FieldListAdapter(m_cardTemplate, fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);

        ItemTouchCallback itemTouchCallback = new ItemTouchCallback(m_fieldListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchCallback);
        m_fieldListAdapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(m_fullFieldListView);


    }

    private void displayFieldEditor(CardField cardField) {
        LinearLayout fieldEditor = findViewById(R.id.cardstyle_editor);
        CardFieldEditorBinding binding = DataBindingUtil.bind(fieldEditor);
        binding.setEditorField(cardField);
    }

    public CardStyle getCardStyle() {
        return m_cardStyle;
    }

    private CardStyle m_cardStyle;

    // views
    private RecyclerView m_fullFieldListView;
    private FieldListAdapter m_fieldListAdapter;
    private DesignerFlashcardLayout m_designerFlashcardLayout;



    private CardTemplateKey m_cardTemplateKey;
    private CardTemplate m_cardTemplate;
    private Card m_card;

}
