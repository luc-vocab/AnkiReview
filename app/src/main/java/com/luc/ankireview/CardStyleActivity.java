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

    public interface ActionCompletionContract {
        void onViewMoved(int oldPosition, int newPosition);
        void onViewSwiped(int position);
    }

    public class ItemTouchCallback extends ItemTouchHelper.Callback {

        private ActionCompletionContract m_contract;

        public ItemTouchCallback(ActionCompletionContract contract) {
            this.m_contract = contract;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            /*
            if (viewHolder instanceof SectionHeaderViewHolder) {
                return 0;
            }
            */
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            m_contract.onViewMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }


    }

    public class FieldListAdapter extends RecyclerView.Adapter<FieldListAdapter.FieldViewHolder> implements ActionCompletionContract {
        private Vector<String> m_fieldList;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class FieldViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public FieldViewHolder(View v) {
                super(v);

                mTextView = v.findViewById(R.id.field_name);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public FieldListAdapter(Vector<String> fieldList) {
            m_fieldList = fieldList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public FieldListAdapter.FieldViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

            View containingView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_field_item, parent, false);

            FieldViewHolder vh = new FieldViewHolder(containingView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final FieldViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(m_fieldList.get(position));

            ((FieldViewHolder) holder).mTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        m_touchHelper.startDrag(holder);
                    }
                    return false;
                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return m_fieldList.size();
        }

        public void setTouchHelper(ItemTouchHelper touchHelper) {

            this.m_touchHelper = touchHelper;
        }

        @Override
        public void onViewMoved(int oldPosition, int newPosition) {
            String targetField = m_fieldList.get(oldPosition);
            String field = new String(targetField);
            m_fieldList.remove(oldPosition);
            m_fieldList.add(newPosition, field);
            notifyItemMoved(oldPosition, newPosition);
        }

        @Override
        public void onViewSwiped(int position) {
            /*
            usersList.remove(position);
            notifyItemRemoved(position);
            */
        }

        private ItemTouchHelper m_touchHelper;
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

        public Vector<CardField> getCardFields() {
            return m_cardFields;
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
        //m_questionFieldsListView.setOnDragListener(new SideFieldDragListener(m_questionFieldsAdapter));

        // setup the AnswerFields ListView
        m_answerFieldsListView = findViewById(R.id.cardstyle_editor_answer_fields);
        m_answerFieldsAdapter = new CardFieldAdapter(this, m_cardTemplate.getAnswerCardFields());
        m_answerFieldsListView.setAdapter(m_answerFieldsAdapter);
        m_answerFieldsListView.setOnItemClickListener(this);
        // add drag listener
        //m_answerFieldsListView.setOnDragListener(new SideFieldDragListener(m_answerFieldsAdapter));

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
        m_fieldListAdapter = new FieldListAdapter(fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);

        ItemTouchCallback itemTouchCallback = new ItemTouchCallback(m_fieldListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchCallback);
        m_fieldListAdapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(m_fullFieldListView);


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

    private CardFieldAdapter m_questionFieldsAdapter;
    private CardFieldAdapter m_answerFieldsAdapter;

    private RecyclerView m_fullFieldListView;
    private FieldListAdapter m_fieldListAdapter;




    CardTemplateKey m_cardTemplateKey;
    CardTemplate m_cardTemplate;
    Card m_card;

}
