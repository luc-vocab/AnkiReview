package com.luc.ankireview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashSet;
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
                ease1.setText(nextReviewTimes.get(0));
                ease3.setText(nextReviewTimes.get(1));
                break;
            case 3:
                setupAnswerHandler(ease1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(ease3, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(ease4, AnkiUtils.Ease.EASE_3);
                ease1.setText(nextReviewTimes.get(0));
                ease3.setText(nextReviewTimes.get(1));
                ease4.setText(nextReviewTimes.get(2));
                ease2.setVisibility(View.GONE);
                break;
            default:
                setupAnswerHandler(ease1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(ease2, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(ease3, AnkiUtils.Ease.EASE_3);
                setupAnswerHandler(ease4, AnkiUtils.Ease.EASE_4);
                ease1.setText(nextReviewTimes.get(0));
                ease2.setText(nextReviewTimes.get(1));
                ease3.setText(nextReviewTimes.get(2));
                ease4.setText(nextReviewTimes.get(3));
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
        ArrayList<String> getQuicktagList();
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

    /*
    private static final int[] m_answer1ViewIds = {R.id.icon_answer_ease1, R.id.text_answer_ease1, R.id.interval_answer_ease1};
    private static final int[] m_answer2ViewIds = {R.id.icon_answer_ease2, R.id.text_answer_ease2, R.id.interval_answer_ease2};
    private static final int[] m_answer3ViewIds = {R.id.icon_answer_ease3, R.id.text_answer_ease3, R.id.interval_answer_ease3};
    private static final int[] m_answer4ViewIds = {R.id.icon_answer_ease4, R.id.text_answer_ease4, R.id.interval_answer_ease4};

    private static final int[] m_quicktag1ViewIds = {R.id.icon_quicktag_1, R.id.text_quicktag_1, R.id.tagname_1};
    private static final int[] m_quicktag2ViewIds = {R.id.icon_quicktag_2, R.id.text_quicktag_2, R.id.tagname_2};
    private static final int[] m_quicktag3ViewIds = {R.id.icon_quicktag_3, R.id.text_quicktag_3, R.id.tagname_3};
    private static final int[] m_quicktag4ViewIds = {R.id.icon_quicktag_4, R.id.text_quicktag_4, R.id.tagname_4};
    private static final int[] m_quicktag5ViewIds = {R.id.icon_quicktag_5, R.id.text_quicktag_5, R.id.tagname_5};
    */

}
