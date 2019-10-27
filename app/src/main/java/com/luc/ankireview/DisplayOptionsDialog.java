package com.luc.ankireview;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DisplayOptionsDialog extends DialogFragment {
    private static final String TAG = "DisplayOptionsDialog";

    public interface DisplayOptionsDialogListener {
        void onSelectAnkiHTMLMode();
        void onSelectAnkireviewMode();
        void onSelectTeacherMode();
    }


    public DisplayOptionsDialog(Context activity) {
        m_activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setTitle("Deck Display Options");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        View view = inflater.inflate(R.layout.deck_display_options, null);
        view.findViewById(R.id.deck_display_mode_choose_ankihtml).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_listener.onSelectAnkiHTMLMode();
                dismiss();
            }
        });
        view.findViewById(R.id.deck_display_mode_choose_ankireview).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                m_listener.onSelectAnkireviewMode();
                dismiss();
            }
        });
        view.findViewById(R.id.deck_display_mode_choose_teachermode).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                m_listener.onSelectTeacherMode();
                dismiss();
            }
        });

        builder.setView(view);


        builder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // LoginDialogFragment.this.getDialog().cancel();
                        Log.v(TAG, "cancel");
                    }
                });


        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        m_listener = (DisplayOptionsDialogListener) context;
    }


    private Context m_activity;
    private DisplayOptionsDialogListener m_listener;
}



