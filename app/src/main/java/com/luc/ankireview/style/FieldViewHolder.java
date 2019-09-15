package com.luc.ankireview.style;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.luc.ankireview.R;

public class FieldViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextView;
    public ImageView mDragHandle;
    public FieldViewHolder(View v) {
        super(v);
        mTextView = v.findViewById(R.id.field_name);
        mDragHandle = v.findViewById(R.id.drag_handle);
    }
}
