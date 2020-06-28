package com.luc.ankireview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import androidx.annotation.Nullable;

public class ReviewBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "ReviewBottomSheet";

    class QuickTagData {
        public QuickTagData(View clickHandler, int[] viewIds, TextView tagNameTextView, ImageView icon) {
            this.clickHandler = clickHandler;
            this.viewIds = viewIds;
            this.tagNameTextView = tagNameTextView;
            this.icon = icon;
        }

        public final View clickHandler;
        public final int[] viewIds;
        public final TextView tagNameTextView;
        public final ImageView icon;
    }

    private Button createQuicktagButton(String buttonText, Boolean disabled) {
        Button addQuicktagButton = new Button(getContext());
        addQuicktagButton.setText(buttonText);
        if( disabled) {
            addQuicktagButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.answer_tag_disabled)));
        } else {
            addQuicktagButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.answer_tag_suspend)));
        }
        addQuicktagButton.setTextColor(getResources().getColor(R.color.button_text_color));
        // set layout params
        GridLayout.LayoutParams param= new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f),
                GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f));
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width  = 0;
        addQuicktagButton.setLayoutParams(param);
        return addQuicktagButton;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.review_bottomsheet, container, false);

        ViewGroup quickTagContainer = v.findViewById(R.id.review_bottomsheet_quicktag_container);

        // add all quicktags
        Map<String,Boolean> quicktagList = m_listener.getQuicktagList();
        ArrayList<String> tagArrayList = new ArrayList<String>(quicktagList.keySet());
        Collections.sort(tagArrayList, String.CASE_INSENSITIVE_ORDER);
        for( final String tag : tagArrayList ) {
            Boolean disabled = quicktagList.get(tag);

            Button quicktagButton = createQuicktagButton(tag, disabled);
            quicktagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_listener.tagCard(tag);
                    dismiss();
                }
            });
            quickTagContainer.addView(quicktagButton);
        }

        // create new quicktag button
        Button addQuicktagButton = createQuicktagButton("Add quicktag", false);
        addQuicktagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.showAddQuicktag();
                dismiss();
            }
        });
        quickTagContainer.addView(addQuicktagButton);


        View mark = v.findViewById(R.id.button_mark);
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.markCard();
                dismiss();

            }
        });

        View markSuspend = v.findViewById(R.id.button_mark_suspend);
        markSuspend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.markSuspendCard();
                dismiss();
            }
        });

        View markBury = v.findViewById(R.id.button_mark_bury);
        markBury.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.markBuryCard();
                dismiss();
            }
        });


        setupAnswerChoices(v);

        return v;
    }

    private void setupAnswerChoices(View v) {
        Button ease1 = v.findViewById(R.id.button_answer_ease1);
        Button ease2 = v.findViewById(R.id.button_answer_ease2);
        Button ease3 = v.findViewById(R.id.button_answer_ease3);
        Button ease4 = v.findViewById(R.id.button_answer_ease4);

        Vector<String> nextReviewTimes = m_listener.getNextReviewtimes();

        int buttonCount = m_listener.getAnswerButtonCount();

        switch(buttonCount) {
            case 2:
                setupAnswerHandler(ease1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(ease3, AnkiUtils.Ease.EASE_2);
                ease2.setVisibility(View.GONE);
                ease4.setVisibility(View.GONE);
                ease1.setText(nextReviewTimes.get(0) + '\n' + v.getContext().getString(R.string.ease_button_again));
                ease3.setText(nextReviewTimes.get(1) + '\n' + v.getContext().getString(R.string.ease_button_good));
                break;
            case 3:
                setupAnswerHandler(ease1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(ease3, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(ease4, AnkiUtils.Ease.EASE_3);
                ease1.setText(nextReviewTimes.get(0) + '\n' + v.getContext().getString(R.string.ease_button_again));
                ease3.setText(nextReviewTimes.get(1) + '\n' + v.getContext().getString(R.string.ease_button_good));
                ease4.setText(nextReviewTimes.get(2) + '\n' + v.getContext().getString(R.string.ease_button_easy));
                ease2.setVisibility(View.GONE);
                break;
            default:
                setupAnswerHandler(ease1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(ease2, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(ease3, AnkiUtils.Ease.EASE_3);
                setupAnswerHandler(ease4, AnkiUtils.Ease.EASE_4);
                ease1.setText(nextReviewTimes.get(0) + '\n' + v.getContext().getString(R.string.ease_button_again));
                ease2.setText(nextReviewTimes.get(1) + '\n' + v.getContext().getString(R.string.ease_button_hard));
                ease3.setText(nextReviewTimes.get(2) + '\n' + v.getContext().getString(R.string.ease_button_good));
                ease4.setText(nextReviewTimes.get(3) + '\n' + v.getContext().getString(R.string.ease_button_easy));
                break;
        }


    }

    private void setupAnswerHandler(View clickHandler, final AnkiUtils.Ease ease) {
        clickHandler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.answerCustom(ease);
                dismiss();
            }
        });
    }

    private void setupTagHandler(View clickHandler, final String tagName) {
        clickHandler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.tagCard(tagName);
                dismiss();
            }
        });
    }

    private void hideViewIds(View v, int[] answerIds) {
        for(int id : answerIds) {
            View view = v.findViewById(id);
            view.setVisibility(View.GONE);
        }
    }

    public interface ReviewBottomSheetListener {
        int getAnswerButtonCount();
        Vector<String> getNextReviewtimes();
        void showAddQuicktag();
        Map<String,Boolean> getQuicktagList(); // true if set for current card
        void markCard();
        void markSuspendCard();
        void markBuryCard();
        void answerCustom(AnkiUtils.Ease ease);
        void tagCard(String tag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            m_listener = (ReviewBottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    private ReviewBottomSheetListener m_listener;

}
