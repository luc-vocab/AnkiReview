package com.luc.ankireview.style;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luc.ankireview.R;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.IndicatorStayLayout;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class ValueSlider extends LinearLayout implements  OnSeekChangeListener {
    private static final String TAG = "ValueSlider";

    public ValueSlider(Context context) {
        super(context);
        initView();
    }

    public ValueSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ValueSlider, 0, 0);
        m_titleText = a.getString(R.styleable.ValueSlider_titleText);
        m_minValue = a.getInt(R.styleable.ValueSlider_minValue, 0);
        m_maxValue = a.getInt(R.styleable.ValueSlider_maxValue, 0);

        initView();
    }


    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.value_slider, this);

        LinearLayout header = findViewById(R.id.value_slider_header);
        header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "header clicked");

                if(m_sliderVisible == false ) {
                    m_slider.setVisibility(View.VISIBLE);
                    m_sliderVisible = true;
                } else {
                    m_slider.setVisibility(View.GONE);
                    m_sliderVisible = false;
                }
            }
        });

        m_slider = findViewById(R.id.value_slider_slider);
        m_slider.setVisibility(View.GONE);

        TextView title = findViewById(R.id.value_slider_title);
        title.setText(m_titleText);

        m_valueTextView = findViewById(R.id.value_slider_value);

        m_indicatorSeekBar = findViewById(R.id.built_in_isb);
        m_indicatorSeekBar.setMin(m_minValue);
        m_indicatorSeekBar.setMax(m_maxValue);

        m_indicatorSeekBar.setOnSeekChangeListener(this);
    }

    public void setListener(ValueSliderUpdate listener) {
        m_listener = listener;
    }

    public void setCurrentValue(int currentValue) {
        m_indicatorSeekBar.setProgress(currentValue);
        m_valueTextView.setText(Integer.toString(currentValue));
    }

    @Override
    public void onSeeking(SeekParams seekParams) {
        int currentValue = seekParams.progress;
        m_valueTextView.setText(Integer.toString(currentValue));

        if(m_listener != null ) {
            m_listener.valueUpdate(currentValue);
        }
    }

    @Override
    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
    }

    private boolean m_sliderVisible = false;
    private IndicatorStayLayout m_slider;
    private IndicatorSeekBar m_indicatorSeekBar;
    private TextView m_valueTextView;

    private String m_titleText;
    private int m_minValue;
    private int m_maxValue;

    private ValueSliderUpdate m_listener;

}
