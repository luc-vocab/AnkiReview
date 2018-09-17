package com.luc.ankireview;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ichi2.anki.FlashCardsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class AnkiUtils {

    private static final String TAG = "AnkiUtils";

    public static int getDeckDueCount(ContentResolver contentResolver, long deckId) {
        Uri deckUri = Uri.withAppendedPath(FlashCardsContract.Deck.CONTENT_ALL_URI, Long.toString(deckId));
        Cursor decksCursor = contentResolver.query(deckUri, null, null, null, null);

        int dueCount = 0;

        if (decksCursor == null || !decksCursor.moveToFirst()) {
            Log.e(TAG, "query for deck returned no result");
            if (decksCursor != null) {
                decksCursor.close();
            }
        } else {
            JSONObject decks = new JSONObject();
            long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
            String deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));

            try {
                JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                Log.d(TAG, "deckCounts " + deckCounts);
                dueCount = deckCounts.getInt(0) + deckCounts.getInt(1) + deckCounts.getInt(2);
                decks.put(deckName, deckID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            decksCursor.close();
        }

        return dueCount;
    }

    public static Vector<Card> getDueCards(ContentResolver contentResolver, long deckId, int numCards)
    {
        Vector<Card> cardList = new Vector<Card>();

        Uri scheduled_cards_uri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        String deckArguments[] = new String[]{Integer.toString(numCards), Long.toString(deckId)};
        String deckSelector = "limit=?, deckID=?";
        final Cursor cur = contentResolver.query(scheduled_cards_uri,
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
                final Cursor cardCursor = contentResolver.query(specificCardUri,
                        null,  // projection
                        null,  // selection is ignored for this URI
                        null,  // selectionArgs is ignored for this URI
                        null   // sortOrder is ignored for this URI
                );
                if(cardCursor.moveToFirst()) {
                    String question = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.QUESTION));
                    String answer = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.ANSWER));

                    // Log.v(TAG, "question: " + question);

                    Card card = new Card(noteId, cardOrd, question, answer, buttonCount);
                    cardList.add(card);
                }

            } while (cur.moveToNext());
        }

        return cardList;
    }

}
