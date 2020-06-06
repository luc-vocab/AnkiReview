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
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.review_bottomsheet, container, false);

        ViewGroup quickTagContainer = v.findViewById(R.id.review_bottomsheet_quicktag_container);
        // create new button
        Button addQuicktagButton = new Button(getContext());
        addQuicktagButton.setText("Add Quicktag");
        addQuicktagButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.answer_tag_suspend)));
        addQuicktagButton.setTextColor(getResources().getColor(R.color.button_text_color));
        // set layout params
        GridLayout.LayoutParams param= new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f),
                GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f));
        param.height = 0;
        param.width  = 0;
        addQuicktagButton.setLayoutParams(param);
        quickTagContainer.addView(addQuicktagButton);

        /*
        View addQuicktag = v.findViewById(R.id.clickhandler_add_quicktag);
        addQuicktag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.showAddQuicktag();
                dismiss();

            }
        });
        */

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
        //setupQuicktags(v);

        return v;
    }

    /*
    private void setupQuicktags(View v) {
        View clickHandler1 = v.findViewById(R.id.clickhandler_tag1);
        View clickHandler2 = v.findViewById(R.id.clickhandler_tag2);
        View clickHandler3 = v.findViewById(R.id.clickhandler_tag3);
        View clickHandler4 = v.findViewById(R.id.clickhandler_tag4);
        View clickHandler5 = v.findViewById(R.id.clickhandler_tag5);

        ImageView icon1 = v.findViewById(R.id.icon_quicktag_1);
        ImageView icon2 = v.findViewById(R.id.icon_quicktag_2);
        ImageView icon3 = v.findViewById(R.id.icon_quicktag_3);
        ImageView icon4 = v.findViewById(R.id.icon_quicktag_4);
        ImageView icon5 = v.findViewById(R.id.icon_quicktag_5);

        TextView tagName1 = v.findViewById(R.id.tagname_1);
        TextView tagName2 = v.findViewById(R.id.tagname_2);
        TextView tagName3 = v.findViewById(R.id.tagname_3);
        TextView tagName4 = v.findViewById(R.id.tagname_4);
        TextView tagName5 = v.findViewById(R.id.tagname_5);

        ArrayList<String> quicktagList = m_listener.getQuicktagList();
        HashSet<String> tagMap = m_listener.getCardTagMap();

        Vector<QuickTagData> quickTagDataVector = new Vector<QuickTagData>();
        quickTagDataVector.add(new QuickTagData(clickHandler1, m_quicktag1ViewIds, tagName1, icon1));
        quickTagDataVector.add(new QuickTagData(clickHandler2, m_quicktag2ViewIds, tagName2, icon2));
        quickTagDataVector.add(new QuickTagData(clickHandler3, m_quicktag3ViewIds, tagName3, icon3));
        quickTagDataVector.add(new QuickTagData(clickHandler4, m_quicktag4ViewIds, tagName4, icon4));
        quickTagDataVector.add(new QuickTagData(clickHandler5, m_quicktag5ViewIds, tagName5, icon5));

        for(int i = 0; i < Settings.MAX_QUICKTAGS; i++) {
            QuickTagData quickTagData = quickTagDataVector.get(i);
            // do we have a quicktag at that position ?
            if (i < quicktagList.size()) {
                String tagName = quicktagList.get(i);
                quickTagData.tagNameTextView.setText(tagName);
                setupTagHandler(quickTagData.clickHandler, tagName);
                // is the card already tagged ? if so, change tint of icon
                if(tagMap.contains(tagName)) {
                    // quickTagData.icon.setTint
                    quickTagData.icon.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.answer_tag_disabled), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
            } else {
                // no quicktag, hide the views
                hideViewIds(v, quickTagData.viewIds);
            }
        }

    }
    */

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
        HashSet<String> getCardTagMap();
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
