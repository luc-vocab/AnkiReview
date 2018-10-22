package com.luc.ankireview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class CardStyleActivity extends AppCompatActivity {
    private static final String TAG = "CardStyleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardstyle);

        m_cardStyle = new CardStyle(this);
        m_cardStyleLayout = findViewById(R.id.flashcard_style);

        Toolbar toolbar = (Toolbar) findViewById(R.id.review_toolbar);
        toolbar.setTitle(R.string.card_style);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        long noteId = intent.getLongExtra("noteId", 0l);
        int cardOrd = intent.getIntExtra("cardOrd", 0);

        Log.v(TAG, "starting CardStyleActivity with noteId: " + noteId + " cardOrd: " + cardOrd);

        // retrieve the appropriate card
        Card card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);
        m_cardStyleLayout.setCard(card);
    }

    public CardStyle getCardStyle() {
        return m_cardStyle;
    }

    private CardStyle m_cardStyle;
    private CardStyleFlashcardLayout m_cardStyleLayout;

}
