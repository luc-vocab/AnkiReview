package com.luc.ankireview.style;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.luc.ankireview.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextView;
    public HeaderViewHolder(View v) {
        super(v);
        mTextView = v.findViewById(R.id.header_name);
    }
}
