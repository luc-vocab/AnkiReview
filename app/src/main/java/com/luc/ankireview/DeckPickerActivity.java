package com.luc.ankireview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.database.Cursor;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ichi2.anki.FlashCardsContract;



public class DeckPickerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "DeckPickerActivity";
    private static final String ANKI_PERMISSIONS = "com.ichi2.anki.permission.READ_WRITE_DATABASE";
    private static final String READ_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE;


    private class AnkiDeck {
        public long deckId;
        public String deckName;
        public String toString() {
            return deckName;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        boolean allPermissionsGranted = true;

        for (int i=0; i<permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];

            if( grantResult != PackageManager.PERMISSION_GRANTED )
            {
                Log.e(TAG, "permission not granted: " + permission);
                allPermissionsGranted = false;
            }
        }

        if(allPermissionsGranted) {
            listDecks();
        }

        return;

    }


    private void checkAllPermissions() {
        // Here, thisActivity is the current activity
        if ( ContextCompat.checkSelfPermission(this, ANKI_PERMISSIONS ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION ) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "permissions not granted yet");

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ANKI_PERMISSIONS ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, READ_STORAGE_PERMISSION )   ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Log.d(TAG, "show explanation ?");

                ActivityCompat.requestPermissions(this,
                        new String[]{ANKI_PERMISSIONS, READ_STORAGE_PERMISSION },
                        0);

            } else {
                Log.d(TAG, "requesting permissions");

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{ANKI_PERMISSIONS, READ_STORAGE_PERMISSION },
                        0);

                // we're going to get a callback later
            }
        } else {
            // Permission has already been granted
            Log.d(TAG, "permissions already granted");

            listDecks();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_picker);
        m_deckList = findViewById(R.id.deck_list);

        m_adapter = new ArrayAdapter<AnkiDeck>(this, R.layout.deck_item);

        m_deckList.setAdapter(m_adapter);
        m_deckList.setOnItemClickListener(this);

        checkAllPermissions();

    }

    private void listDecks() {

        Cursor decksCursor = getContentResolver().query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null);
        if (decksCursor.moveToFirst()) {
            HashMap decks = new HashMap();
            do {
                long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
                String deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));

                AnkiDeck deck = new AnkiDeck();
                deck.deckId = deckID;
                deck.deckName = deckName;

                Log.d(TAG, "deck name: " + deckName);
                m_adapter.add(deck);

                try {
                    JSONObject deckOptions = new JSONObject(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.OPTIONS)));
                    JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                    Log.d(TAG, deckOptions.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                decks.put(deckID, deckName);
            } while (decksCursor.moveToNext());
        }
    }

    private ListView m_deckList;
    private ArrayAdapter<AnkiDeck> m_adapter;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "position: " + i);

        AnkiDeck deck = m_adapter.getItem(i);

        Log.d(TAG, "selected deck: " + deck.deckName + " deckId: " + deck.deckId);

        // launch review activity
        Intent intent = new Intent(DeckPickerActivity.this, ReviewActivity.class);
        intent.putExtra("deckId", deck.deckId);
        this.startActivity(intent);
    }
    //private List<String> m_decks = new LinkedList<String>();

}
