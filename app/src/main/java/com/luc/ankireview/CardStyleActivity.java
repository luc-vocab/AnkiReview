package com.luc.ankireview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.databinding.DataBindingUtil;

import com.luc.ankireview.style.CardField;
import com.luc.ankireview.style.CardStyle;
import com.luc.ankireview.style.CardTemplate;
import com.luc.ankireview.style.CardTemplateKey;
import com.luc.ankireview.databinding.CardFieldItemBinding;

import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "CardStyleActivity";


    private class CardFieldAdapter extends BaseAdapter {
        public CardFieldAdapter(Context context, Vector<CardField> cardFields) {
            this.m_context = context;
            this.m_cardFields = cardFields;
        }

        @Override
        public int getCount() {
            return m_cardFields.size();
        }

        @Override
        public Object getItem(int i) {
            return m_cardFields.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CardFieldItemBinding binding;

            if(view==null)
            {
                view = LayoutInflater.from(m_context).inflate(R.layout.card_field_item,viewGroup,false);
                binding = DataBindingUtil.bind(view);
                view.setTag(binding);
            } else {
                binding = (CardFieldItemBinding) view.getTag();
            }

            CardField cardField = (CardField) this.getItem(i);
            binding.setField(cardField);

            return binding.getRoot();
        }

        private Context m_context;
        private Vector<CardField> m_cardFields;
    }

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
        Card card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);

        // retrieve the card template
        CardTemplateKey cardTemplateKey = new CardTemplateKey(card.getModelId(), card.getCardOrd());
        CardTemplate cardTemplate = m_cardStyle.getCardTemplate(cardTemplateKey);

        Log.v(TAG, "num question card fields: " + cardTemplate.getQuestionCardFields().size());

        m_questionFieldsListView = findViewById(R.id.cardstyle_editor_question_fields);
        m_questionFieldsAdapter = new CardFieldAdapter(this, cardTemplate.getQuestionCardFields());
        m_questionFieldsListView.setAdapter(m_questionFieldsAdapter);
        m_questionFieldsListView.setOnItemClickListener(this);

        m_answerFieldsListView = findViewById(R.id.cardstyle_editor_answer_fields);
        m_answerFieldsAdapter = new CardFieldAdapter(this, cardTemplate.getAnswerCardFields());
        m_answerFieldsListView.setAdapter(m_answerFieldsAdapter);
        m_answerFieldsListView.setOnItemClickListener(this);


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if( adapterView == m_questionFieldsListView) {
            Log.d(TAG, "click question field: " + i);
        } else if( adapterView == m_answerFieldsListView) {
            Log.d(TAG, "click answer field: " + i);
        }

    }

    public CardStyle getCardStyle() {
        return m_cardStyle;
    }

    private CardStyle m_cardStyle;


    private ListView m_questionFieldsListView;
    private ListView m_answerFieldsListView;
    private CardFieldAdapter m_questionFieldsAdapter;
    private CardFieldAdapter m_answerFieldsAdapter;

}
