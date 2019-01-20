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

import java.util.HashMap;
import java.util.Vector;

public class AnkiUtils {

    private static final String TAG = "AnkiUtils";
    public static final String MARK_TAG = "marked";

    public enum Ease {
        EASE_1(1, R.id.reviewer_action_ease_1),
        EASE_2(2, R.id.reviewer_action_ease_2),
        EASE_3(3, R.id.reviewer_action_ease_3),
        EASE_4(4, R.id.reviewer_action_ease_4);

        private int m_value;
        private int m_actionId;

        Ease(int value, int actionId) {
            m_value = value;
            m_actionId = actionId;
        }

        public int getValue() {
            return m_value;
        }

        public int getActionId() { return m_actionId; }

        public static Ease fromInt(int value) {
            for (Ease e : Ease.values()) {
                if (e.getValue() == value) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Not a valid ease value: " + value);
        }

        public static Ease fromActionId(int actionId) {
            for (Ease e : Ease.values()) {
                if (e.getActionId() == actionId) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Not a valid actionId: " + actionId);
        }
    }

    public static class AnswerChoice {
        public AnswerChoice(int actionId, String text, int drawableId, int colorId) {
            m_actionId = actionId;
            m_text = text;
            m_drawableId = drawableId;
            m_colorId = colorId;
        }
        public int getActionId() { return m_actionId; }
        public String getText() { return m_text; }
        public int getDrawableId() { return m_drawableId; }
        public int getColorId() { return m_colorId; }

        private final int m_actionId;
        private final String m_text;
        private final int m_drawableId;
        private final int m_colorId;
    }

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

    public static DeckDueCounts parseDeckDueCount(JSONArray deckCounts) {
        DeckDueCounts deckDueCounts = new DeckDueCounts();
        try {
            deckDueCounts.learnCount = deckCounts.getInt(0);
            deckDueCounts.reviewCount = deckCounts.getInt(1);
            deckDueCounts.newCount = deckCounts.getInt(2);
        } catch( JSONException e ) {
            e.printStackTrace();
        }
        return deckDueCounts;
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
                deckDueCounts = parseDeckDueCount(deckCounts);
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
                Vector<String> nextReviewTimes = new Vector<String>();
                try {
                    JSONArray reviewTimes = new JSONArray(cur.getString(cur.getColumnIndex(FlashCardsContract.ReviewInfo.NEXT_REVIEW_TIMES)));
                    for (int i = 0; i < reviewTimes.length(); i++)
                    {
                        String nextReviewTime = reviewTimes.getString(i);
                        nextReviewTimes.add(nextReviewTime);
                    }
                    JSONArray media = new JSONArray(cur.getString(cur.getColumnIndex(FlashCardsContract.ReviewInfo.MEDIA_FILES)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "noteId: " + noteId + " cardOrd: " + cardOrd);

                Card card = retrieveCard(contentResolver, noteId, cardOrd);
                card.setReviewData(buttonCount, nextReviewTimes);
                cardList.add(card);

            } while (cur.moveToNext());
        }

        return cardList;
    }

    public static Card retrieveCard(ContentResolver contentResolver, long noteId, int cardOrd) {
        // retrieve card information
        // -------------------------

        String question = null;
        String answer = null;

        Uri noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(noteId));
        Uri cardsUri = Uri.withAppendedPath(noteUri, "cards");
        Uri specificCardUri = Uri.withAppendedPath(cardsUri, Integer.toString(cardOrd));
        final Cursor cardCursor = contentResolver.query(specificCardUri,
                new String[]{FlashCardsContract.Card.QUESTION, FlashCardsContract.Card.ANSWER},  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null   // sortOrder is ignored for this URI
        );
        if(cardCursor.moveToFirst()) {
            question = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.QUESTION));
            answer = cardCursor.getString(cardCursor.getColumnIndex(FlashCardsContract.Card.ANSWER));

        }

        // retrieve note information
        // -------------------------

        String[] fieldValues = {};
        long modelId = 0;

        Uri noteInfoUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(noteId));
        final Cursor noteCursor = contentResolver.query(noteInfoUri,
                null,  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null   // sortOrder is ignored for this URI
        );

        if(noteCursor.moveToFirst()) {
            modelId = noteCursor.getLong(noteCursor.getColumnIndex(FlashCardsContract.Note.MID));

            String fields = noteCursor.getString(noteCursor.getColumnIndex(FlashCardsContract.Note.FLDS));
            Log.v(TAG, "fields: " + fields);

            fieldValues = fields.split("\\x1f");

        }

        // retrieve model information
        // --------------------------

        String[] fieldNames = {};

        Uri modelUri = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, Long.toString(modelId));
        final Cursor modelCursor = contentResolver.query(modelUri,
                null,  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null   // sortOrder is ignored for this URI
        );

        if ( modelCursor.moveToFirst() ) {
            String fieldNamesStr = modelCursor.getString(modelCursor.getColumnIndex(FlashCardsContract.Model.FIELD_NAMES));
            fieldNames = fieldNamesStr.split("\\x1f");
        }

        // create field name/value map
        HashMap<String,String> fieldMap = new HashMap<String,String>();
        for(int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldName = fieldNames[i];
            String fieldValue = "";
            if (i < fieldValues.length ) {
                fieldValue = fieldValues[i];
            }

            fieldMap.put(fieldName, fieldValue);
        }

        // retrieve card template information
        // ----------------------------------
        String cardTemplateName = "";

        Uri uri1 = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, Long.toString(modelId));
        Uri uri2 = Uri.withAppendedPath(uri1 , "templates");
        Uri cardTemplateUri = Uri.withAppendedPath(uri2 , Integer.toString(cardOrd));
        final Cursor cardTemplateCursor = contentResolver.query(cardTemplateUri,
                null,  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null   // sortOrder is ignored for this URI
        );

        if ( cardTemplateCursor.moveToFirst() ) {
            cardTemplateName = cardTemplateCursor.getString(cardTemplateCursor.getColumnIndex(FlashCardsContract.CardTemplate.NAME));
            // Log.v(TAG, "card template name: " + cardTemplateName);
        }

        Card card = new Card(noteId, cardOrd, modelId, cardTemplateName, fieldMap);

        return card;
    }

    public static void answerCard(ContentResolver contentResolver, Card card, AnkiUtils.Ease ease, long timeTaken) {
        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();

        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, card.getNoteId());
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, card.getCardOrd());
        values.put(FlashCardsContract.ReviewInfo.EASE, ease.getValue());
        values.put(FlashCardsContract.ReviewInfo.TIME_TAKEN, timeTaken);
        contentResolver.update(reviewInfoUri, values, null, null);
    }

    public static void suspendCard(ContentResolver contentResolver, Card card) {
        // will be supported only in Anki 2.9
        /*
        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();

        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, card.getNoteId());
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, card.getCardOrd());
        values.put(FlashCardsContract.ReviewInfo.SUSPEND, 1);
        contentResolver.update(reviewInfoUri, values, null, null);
        */
    }

    public static void buryCard(ContentResolver contentResolver, Card card) {
        // will be supported only in Anki 2.9
        /*
        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();

        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, card.getNoteId());
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, card.getCardOrd());
        values.put(FlashCardsContract.ReviewInfo.BURY, 1);
        contentResolver.update(reviewInfoUri, values, null, null);
        */
    }

    public static void markCard(ContentResolver contentResolver, Card card) {
        addCardTag(contentResolver, card, MARK_TAG);
    }

    public static void addCardTag(ContentResolver contentResolver, Card card, String newTag) {
        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();

        // query existing tags
        Uri noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(card.getNoteId()));
        final Cursor cursor = contentResolver.query(noteUri,
                null,  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null   // sortOrder is ignored for this URI
        );

        if (cursor.moveToFirst()) {
            String tags = cursor.getString(cursor.getColumnIndex(FlashCardsContract.Note.TAGS));

            // add the new tag at the end
            String newTags = tags + " " + newTag;

            Uri updateNoteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(card.getNoteId()));
            values = new ContentValues();
            values.put(FlashCardsContract.Note.TAGS, newTags);
            int updateCount = contentResolver.update(updateNoteUri, values, null, null);

        }
    }

}
