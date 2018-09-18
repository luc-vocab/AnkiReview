package com.luc.ankireview;

import android.content.ContentResolver;
import android.content.ContentValues;
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

    public static class DeckDueCounts {
        public int learnCount;
        public int reviewCount;
        public int newCount;

        public int getTotal() {
            return learnCount + reviewCount + newCount;
        }

        public int getTotalWithWeights() {
            return learnCount + reviewCount + 2 * newCount;
        }
    }

    public static String getDeckName(ContentResolver contentResolver, long deckId) {
        Uri deckUri = Uri.withAppendedPath(FlashCardsContract.Deck.CONTENT_ALL_URI, Long.toString(deckId));
        Cursor decksCursor = contentResolver.query(deckUri, null, null, null, null);

        String deckName = "unknown";

        if (decksCursor == null || !decksCursor.moveToFirst()) {
            Log.e(TAG, "query for deck returned no result");
            if (decksCursor != null) {
                decksCursor.close();
            }
        } else {
            JSONObject decks = new JSONObject();
            long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
            deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));
        }

        return deckName;
    }

    public static DeckDueCounts getDeckDueCount(ContentResolver contentResolver, long deckId) {
        Uri deckUri = Uri.withAppendedPath(FlashCardsContract.Deck.CONTENT_ALL_URI, Long.toString(deckId));
        Cursor decksCursor = contentResolver.query(deckUri, null, null, null, null);

        DeckDueCounts deckDueCounts = new DeckDueCounts();

        if (decksCursor == null || !decksCursor.moveToFirst()) {
            Log.e(TAG, "query for deck returned no result");
            if (decksCursor != null) {
                decksCursor.close();
            }
        } else {
            try {
                JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                Log.d(TAG, "deckCounts " + deckCounts);

                deckDueCounts.learnCount = deckCounts.getInt(0);
                deckDueCounts.reviewCount = deckCounts.getInt(1);
                deckDueCounts.newCount = deckCounts.getInt(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            decksCursor.close();
        }

        return deckDueCounts;
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

    public static void answerCard(ContentResolver contentResolver, Card card, int ease, long timeTaken) {
        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();

        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, card.getNoteId());
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, card.getCardOrd());
        values.put(FlashCardsContract.ReviewInfo.EASE, ease);
        values.put(FlashCardsContract.ReviewInfo.TIME_TAKEN, timeTaken);
        contentResolver.update(reviewInfoUri, values, null, null);
    }

}
