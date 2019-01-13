package com.luc.ankireview.style;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.luc.ankireview.R;

public class ValueSlider extends LinearLayout {
    private static final String TAG = "ValueSlider";

    public ValueSlider(Context context) {
        super(context);
        initView();
    }

    public ValueSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.value_slider, this);

        LinearLayout header = findViewById(R.id.value_slider_header);
        header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "header clicked");
            }
        });
    }
}
