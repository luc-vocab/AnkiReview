package com.luc.ankireview.style;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.luc.ankireview.R;

public class FieldViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextView;
    public FieldViewHolder(View v) {
        super(v);
        mTextView = v.findViewById(R.id.field_name);
    }
}
