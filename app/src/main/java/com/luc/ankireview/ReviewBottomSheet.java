package com.luc.ankireview;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Vector;

import androidx.annotation.Nullable;

public class ReviewBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "ReviewBottomSheet";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.review_bottomsheet, container, false);

        // TODO: setup button wiring here

        View addQuicktag = v.findViewById(R.id.clickhandler_add_quicktag);
        addQuicktag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.showAddQuicktag();
                dismiss();

            }
        });

        View mark = v.findViewById(R.id.clickhandler_mark);
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.markCard();
                dismiss();

            }
        });

        View markSuspend = v.findViewById(R.id.clickhandler_mark_suspend);
        markSuspend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.markSuspendCard();
                dismiss();
            }
        });

        View markBury = v.findViewById(R.id.clickhandler_mark_bury);
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
        View clickHandler1 = v.findViewById(R.id.clickhandler_answer_ease1);
        View clickHandler2 = v.findViewById(R.id.clickhandler_answer_ease2);
        View clickHandler3 = v.findViewById(R.id.clickhandler_answer_ease3);
        View clickHandler4 = v.findViewById(R.id.clickhandler_answer_ease4);

        int buttonCount = m_listener.getAnswerButtonCount();

        switch(buttonCount) {
            case 2:
                setupAnswerHandler(clickHandler1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(clickHandler3, AnkiUtils.Ease.EASE_2);
                hideAnswerIds(v, m_answer2ViewIds);
                hideAnswerIds(v, m_answer4ViewIds);
                break;
            case 3:
                setupAnswerHandler(clickHandler1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(clickHandler3, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(clickHandler4, AnkiUtils.Ease.EASE_3);
                hideAnswerIds(v, m_answer2ViewIds);
                break;
            default:
                setupAnswerHandler(clickHandler1, AnkiUtils.Ease.EASE_1);
                setupAnswerHandler(clickHandler2, AnkiUtils.Ease.EASE_2);
                setupAnswerHandler(clickHandler3, AnkiUtils.Ease.EASE_3);
                setupAnswerHandler(clickHandler4, AnkiUtils.Ease.EASE_4);
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

    private void hideAnswerIds(View v, int[] answerIds) {
        for(int id : answerIds) {
            View view = v.findViewById(id);
            view.setVisibility(View.GONE);
        }
    }

    public interface ReviewBottomSheetListener {
        int getAnswerButtonCount();
        void showAddQuicktag();
        void markCard();
        void markSuspendCard();
        void markBuryCard();
        void answerCustom(AnkiUtils.Ease ease);
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

    private static final int[] m_answer1ViewIds = {R.id.icon_answer_ease1, R.id.text_answer_ease1, R.id.interval_answer_ease1};
    private static final int[] m_answer2ViewIds = {R.id.icon_answer_ease2, R.id.text_answer_ease2, R.id.interval_answer_ease2};
    private static final int[] m_answer3ViewIds = {R.id.icon_answer_ease3, R.id.text_answer_ease3, R.id.interval_answer_ease3};
    private static final int[] m_answer4ViewIds = {R.id.icon_answer_ease4, R.id.text_answer_ease4, R.id.interval_answer_ease4};

}
