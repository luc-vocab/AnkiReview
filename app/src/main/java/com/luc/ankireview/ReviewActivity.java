package com.luc.ankireview;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ichi2.anki.FlashCardsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        m_questionPager = findViewById(R.id.flashcard_question_pager);
        m_answerPager = findViewById(R.id.flashcard_answer_pager);

        m_questionAdapter = new FlashCardViewPagerAdapter(this);
        m_answerAdapter = new FlashCardViewPagerAdapter(this);

        m_questionPager.setAdapter(m_questionAdapter);
        m_questionPager.setCurrentItem(1);


        Intent intent = getIntent();
        m_deckId = intent.getLongExtra("deckId", 0);

        Log.d(TAG, "ReviewActivity.onCreate, deckId: "  + m_deckId);

        loadCards();
    }

    private void loadCards() {
        Uri scheduled_cards_uri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        String deckArguments[] = new String[]{"2", Long.toString(m_deckId)};
        String deckSelector = "limit=?, deckID=?";
        final Cursor cur = getContentResolver().query(scheduled_cards_uri,
                null,  // projection
                deckSelector,  // if null, default values will be used
                deckArguments,  // if null, the deckSelector must not contain any placeholders ("?")
                null   // sortOrder is ignored for this URI
        );

        if (cur.moveToFirst()) {
            do {

                long noteId = cur.getLong(cur.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID));
                int cardOrd = cur.getInt(cur.getColumnIndex(FlashCardsContract.ReviewInfo.CARD_ORD));
                int buttonCount = cur.getInt(cur.getColumnIndex(FlashCardsContract.ReviewInfo.BUTTON_COUNT));
                try {
                    JSONArray media = new JSONArray(cur.getString(cur.getColumnIndex(FlashCardsContract.ReviewInfo.MEDIA_FILES)));
                    Log.v(TAG, media.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "noteId: " + noteId + " cardOrd: " + cardOrd);

                // retrieve card information

                Uri noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(noteId));
                Uri cardsUri = Uri.withAppendedPath(noteUri, "cards");
                Uri specificCardUri = Uri.withAppendedPath(cardsUri, Integer.toString(cardOrd));
                final Cursor cardCursor = getContentResolver().query(specificCardUri,
                        null,  // projection
                        null,  // selection is ignored for this URI
                        null,  // selectionArgs is ignored for this URI
                        null   // sortOrder is ignored for this URI
                );
                if(cardCursor.moveToFirst()) {
                    String question = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.QUESTION));
                    String answer = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.ANSWER));

                    // Log.v(TAG, "question: " + question);

                    Card card = new Card(noteId, cardOrd, question, answer);
                    m_initialCardSet.add(card);
                    m_cardList.add(card);
                }

            } while (cur.moveToNext());
        }

        // done loading cards, populate
        Card firstCard = m_cardList.get(0);
        m_questionAdapter.setCardContent(firstCard.getQuestion(), true);
        m_questionAdapter.setCardContent(firstCard.getAnswer(), false);


    }

    private long m_deckId;
    private Set<Card> m_initialCardSet = new HashSet<Card>();
    private Vector<Card> m_cardList = new Vector<Card>();

    // layout elements
    ViewPager m_questionPager;
    ViewPager m_answerPager;
    // adapters
    FlashCardViewPagerAdapter m_questionAdapter;
    FlashCardViewPagerAdapter m_answerAdapter;

}
