package com.luc.ankireview;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Vector;

public class CardPrototypeActivity extends AppCompatActivity {
    private static final String TAG = "CardPrototypeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_card_prototype6);

        Card card = new Card(0, 0, "", "", "closed (a business)", "bù yíng yè / 不营业",0, new Vector<String>());

        FlashcardLayout flashcardLayout = findViewById(R.id.flashcard_layout);
        flashcardLayout.setCard(card);

    }


}
