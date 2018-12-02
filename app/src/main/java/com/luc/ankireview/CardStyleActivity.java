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
import android.support.v7.widget.Toolbar;
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

import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "CardStyleActivity";

    private class SideFieldDragListener implements View.OnDragListener {
        private static final String TAG = "SideFieldDragListener";

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            Log.v(TAG, "onDrag");
            if( dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED ) {
                // drag started
                // accept the drag and drop
                return true;
            }
            if( dragEvent.getAction() == DragEvent.ACTION_DRAG_ENTERED ) {
                Log.v(TAG, "drag entered");
            }
            if( dragEvent.getAction() == DragEvent.ACTION_DRAG_EXITED ) {
                Log.v(TAG,"drag exited");
            }

            return false;
        }
    }

    private static class FieldShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public FieldShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth();

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight();

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);

        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
            getView().draw(canvas);
        }
    }


    private class FieldListAdapter extends BaseAdapter {
        public FieldListAdapter(Context context, Vector<String> cardFields) {
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
            // display field name inside TextView
            final String fieldName = (String) this.getItem(i);

            if( view == null )
            {
                view = new TextView(m_context);
                view.setTag(fieldName);
                TextView textView = (TextView) view;
                ViewGroup.LayoutParams params = viewGroup.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(params);
                textView.setText(fieldName);

                // drag and drop listener
                //view.setOnClickListener(vew View.OnClickListener);

                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            // Create a new ClipData.
                            // This is done in two steps to provide clarity. The convenience method
                            // ClipData.newPlainText() can create a plain text ClipData in one step.

                            // Create a new ClipData.Item from the ImageView object's tag
                            ClipData.Item item = new ClipData.Item(fieldName);

                            // Create a new ClipData using the tag as a label, the plain text MIME type, and
                            // the already-created item. This will create a new ClipDescription object within the
                            // ClipData, and set its MIME type entry to "text/plain"
                            ClipData dragData = new ClipData(
                                    fieldName,
                                    new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                                    item);

                            // Instantiates the drag shadow builder.
                            View.DragShadowBuilder shadow = new FieldShadowBuilder(v);

                            // Starts the drag
                            v.startDragAndDrop(dragData, shadow, null, 0);
                            return true;
                        }
                        return false;
                    }
                });

            }

            TextView textView = (TextView) view;
            textView.setText(fieldName);

            return view;
        }

        private Context m_context;
        private Vector<String> m_cardFields;
    }

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
        m_card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);

        // retrieve the card template
        m_cardTemplateKey = new CardTemplateKey(m_card.getModelId(), m_card.getCardOrd());
        m_cardTemplate = m_cardStyle.getCardTemplate(m_cardTemplateKey);

        Log.v(TAG, "num question card fields: " + m_cardTemplate.getQuestionCardFields().size());

        // setup the Question Fields ListView
        m_questionFieldsListView = findViewById(R.id.cardstyle_editor_question_fields);
        m_questionFieldsAdapter = new CardFieldAdapter(this, m_cardTemplate.getQuestionCardFields());
        m_questionFieldsListView.setAdapter(m_questionFieldsAdapter);
        m_questionFieldsListView.setOnItemClickListener(this);

        // add drag listener
        m_questionFieldsListView.setOnDragListener(new SideFieldDragListener());

        // setup the AnswerFields ListView
        m_answerFieldsListView = findViewById(R.id.cardstyle_editor_answer_fields);
        m_answerFieldsAdapter = new CardFieldAdapter(this, m_cardTemplate.getAnswerCardFields());
        m_answerFieldsListView.setAdapter(m_answerFieldsAdapter);
        m_answerFieldsListView.setOnItemClickListener(this);

        // setup the full Field list ListView
        m_fullFieldListView = findViewById(R.id.cardstyle_editor_all_fields);
        Vector<String> fullFieldList = new Vector<String>();
        for(String field: m_card.getFieldMap().keySet())
        {
            fullFieldList.add(field);
        }
        m_fieldListAdapter = new FieldListAdapter(this, fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if( adapterView == m_questionFieldsListView) {
            Log.d(TAG, "click question field: " + i);
            CardField cardField = m_cardTemplate.getQuestionCardFields().get(i);
            displayFieldEditor(cardField);

        } else if( adapterView == m_answerFieldsListView) {
            Log.d(TAG, "click answer field: " + i);
            CardField cardField = m_cardTemplate.getAnswerCardFields().get(i);
            displayFieldEditor(cardField);
        }

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


    private ListView m_questionFieldsListView;
    private ListView m_answerFieldsListView;
    private ListView m_fullFieldListView;
    private CardFieldAdapter m_questionFieldsAdapter;
    private CardFieldAdapter m_answerFieldsAdapter;
    private FieldListAdapter m_fieldListAdapter;


    CardTemplateKey m_cardTemplateKey;
    CardTemplate m_cardTemplate;
    Card m_card;

}
