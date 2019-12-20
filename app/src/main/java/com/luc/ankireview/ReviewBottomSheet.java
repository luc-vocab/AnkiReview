package com.luc.ankireview;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

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

            /*
            Button button1 = v.findViewById(R.id.button1);
            Button button2 = v.findViewById(R.id.button2);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onButtonClicked("Button 1 clicked");
                    dismiss();
                }
            });
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onButtonClicked("Button 2 clicked");
                    dismiss();
                }
            });
            */

        return v;
    }

    public interface ReviewBottomSheetListener {
        void showAddQuicktag();
        void markCard();
        void markSuspendCard();
        void markBuryCard();
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
