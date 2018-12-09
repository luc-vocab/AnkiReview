package com.luc.ankireview.style;

import android.preference.PreferenceActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luc.ankireview.R;

import java.util.Vector;


public class FieldListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ActionCompletionContract {
    private static final String TAG = "FieldListAdapter";


    // Provide a suitable constructor (depends on the kind of dataset)
    public FieldListAdapter(CardTemplate cardTemplate, Vector<String> fullFieldList) {
        m_cardTemplate = cardTemplate;
        m_fullFieldList = fullFieldList;
        buildFieldList();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View containingView;
        switch(viewType)
        {
            case FieldListItem.VIEWTYPE_FIELD:
                containingView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_field_item, parent, false);
                return new FieldViewHolder(containingView);
            case FieldListItem.VIEWTYPE_HEADER:
                containingView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.header_field_item, parent, false);
                return new HeaderViewHolder(containingView);
        }
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        FieldListItem item = m_fieldList.get(position);

        if(viewType == FieldListItem.VIEWTYPE_FIELD) {
            FieldViewHolder fieldViewHolder = (FieldViewHolder) holder;
            fieldViewHolder.mTextView.setText(item.getCardField().getFieldName());
            ((FieldViewHolder) holder).mTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        m_touchHelper.startDrag(holder);
                    }
                    return false;
                }
            });
        } else {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.mTextView.setText(item.getHeader());
        }

    }


    private void buildFieldList() {
        m_fieldList = new Vector<FieldListItem>();

        // add the "all fields" header
        m_fieldList.add(new FieldListItem(null, FieldListItem.VIEWTYPE_HEADER, "All Fields"));

        // add the unassigned fields
        for( String field : m_fullFieldList) {
            boolean isUnassigned = true;
            for( CardField cardField : m_cardTemplate.getQuestionCardFields() ) {
                if( cardField.getFieldName().equals(field)) {
                    isUnassigned = false;
                }
            }
            for( CardField cardField : m_cardTemplate.getAnswerCardFields() ) {
                if( cardField.getFieldName().equals(field)) {
                    isUnassigned = false;
                }
            }

            if(isUnassigned) {
                CardField cardField = new CardField(field);
                m_fieldList.add(new FieldListItem(cardField, FieldListItem.VIEWTYPE_FIELD, null));
            }
        }

        // add the "question" items
        m_fieldList.add(new FieldListItem(null, FieldListItem.VIEWTYPE_HEADER, "Question Fields"));
        for( CardField cardField : m_cardTemplate.getQuestionCardFields() ) {
            m_fieldList.add(new FieldListItem(cardField, FieldListItem.VIEWTYPE_FIELD, null));
        }

        // add the "answer" items
        m_fieldList.add(new FieldListItem(null, FieldListItem.VIEWTYPE_HEADER, "Answer Fields"));
        for( CardField cardField : m_cardTemplate.getAnswerCardFields() ) {
            m_fieldList.add(new FieldListItem(cardField, FieldListItem.VIEWTYPE_FIELD, null));
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_fieldList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return m_fieldList.get(position).getViewType();
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.m_touchHelper = touchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {

        FieldListItem fieldListItem = m_fieldList.get(oldPosition);
        m_fieldList.remove(oldPosition);
        m_fieldList.add(newPosition, fieldListItem);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        // swiping is not allowed
    }

    private Vector<String> m_fullFieldList;
    private CardTemplate m_cardTemplate;
    private ItemTouchHelper m_touchHelper;

    // cached data
    Vector<FieldListItem> m_fieldList;
}
