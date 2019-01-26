package com.luc.ankireview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.database.Cursor;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ichi2.anki.FlashCardsContract;
import com.luc.ankireview.backgrounds.BackgroundManager;


public class DeckPickerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "DeckPickerActivity";
    private static final String ANKI_PERMISSIONS = "com.ichi2.anki.permission.READ_WRITE_DATABASE";
    private static final String READ_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String INTERNET_PERMISSION = Manifest.permission.INTERNET;


    private class AnkiDeck {
        public long deckId;
        public String deckName;
        public AnkiUtils.DeckDueCounts deckDueCounts;
        public String toString() {
            return deckName;
        }
    }

    private class DeckAdapter extends BaseAdapter {
        public DeckAdapter(Context context, Vector<AnkiDeck> ankiDeckList) {
            this.m_context = context;
            this.m_ankiDecks = ankiDeckList;
        }

        @Override
        public int getCount() {
            return m_ankiDecks.size();
        }

        @Override
        public Object getItem(int i) {
            return m_ankiDecks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null)
            {
                if(Settings.ENABLE_BACKGROUNDS) {
                    view = LayoutInflater.from(m_context).inflate(R.layout.deck_card_item_with_background,viewGroup,false);
                } else {
                    view = LayoutInflater.from(m_context).inflate(R.layout.deck_card_item,viewGroup,false);
                }

            }

            AnkiDeck deck = (AnkiDeck) this.getItem(i);
            TextView deckNameView = (TextView) view.findViewById(R.id.deck_name);
            deckNameView.setText(deck.deckName);

            if( deck.deckDueCounts.getTotal() > 0) {

                TextView deckNewCount = (TextView) view.findViewById(R.id.deck_new_count);
                deckNewCount.setText(Integer.toString(deck.deckDueCounts.newCount));

                TextView deckReviewCount = (TextView) view.findViewById(R.id.deck_review_count);
                deckReviewCount.setText(Integer.toString(deck.deckDueCounts.reviewCount));

                TextView deckLearnCount = (TextView) view.findViewById(R.id.deck_learn_count);
                deckLearnCount.setText(Integer.toString(deck.deckDueCounts.learnCount));

                view.findViewById(R.id.deck_due_counts).setVisibility(View.VISIBLE);

            } else {
                view.findViewById(R.id.deck_due_counts).setVisibility(View.GONE);
            }

            if( Settings.ENABLE_BACKGROUNDS) {
                ImageView backgroundImageView = view.findViewById(R.id.deck_backgroundimage);
                m_backgroundManager.fillImageView(backgroundImageView);
            }

            return view;
        }

        private Context m_context;
        private Vector<AnkiDeck> m_ankiDecks;
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
                showToast("Missing permissions, will not be able to list AnkiDroid decks");
            }
        }

        if(allPermissionsGranted) {
            listDecks();
        }

        return;

    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    private void listDecksIfPermissionsGranted() {
        if( permissionsGranted() ) {
            listDecks();
        }
    }

    private boolean permissionsGranted() {
        if ( ContextCompat.checkSelfPermission(this, ANKI_PERMISSIONS ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, INTERNET_PERMISSION ) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void checkAllPermissions() {

        try {

            // Here, thisActivity is the current activity
            if (!permissionsGranted()) {

                Log.d(TAG, "permissions not granted yet");

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, ANKI_PERMISSIONS) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, READ_STORAGE_PERMISSION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, INTERNET_PERMISSION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    Log.d(TAG, "show explanation ?");

                    ActivityCompat.requestPermissions(this,
                            new String[]{ANKI_PERMISSIONS, READ_STORAGE_PERMISSION, INTERNET_PERMISSION},
                            0);

                } else {
                    Log.d(TAG, "requesting permissions");

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{ANKI_PERMISSIONS, READ_STORAGE_PERMISSION, INTERNET_PERMISSION},
                            0);

                    // we're going to get a callback later
                }
            } else {
                // Permission has already been granted
                Log.d(TAG, "permissions already granted");

                listDecks();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Could not request permissions " + e.getMessage());
            Toast toast = Toast.makeText(this, "Could not list request permissions, do you have AnkiDroid installed? " + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_picker);

        m_backgroundManager = new BackgroundManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.deckpicker_toolbar);
        setSupportActionBar(toolbar);

        m_deckList = findViewById(R.id.deck_list);
        m_deckList.setOnItemClickListener(this);

        m_ankiDeckList = new Vector<AnkiDeck>();

        m_adapter = new DeckAdapter(this, m_ankiDeckList);
        m_deckList.setAdapter(m_adapter);

        checkAllPermissions();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // refresh deck list
        listDecksIfPermissionsGranted();
    }

    private void listDecks() {

        try {

            m_ankiDeckList.clear();

            Cursor decksCursor = getContentResolver().query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null);
            if (decksCursor.moveToFirst()) {
                do {
                    long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
                    String deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));

                    AnkiDeck deck = new AnkiDeck();
                    deck.deckId = deckID;
                    deck.deckName = deckName;

                    try {
                        JSONObject deckOptions = new JSONObject(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.OPTIONS)));
                        JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                        deck.deckDueCounts = AnkiUtils.parseDeckDueCount(deckCounts);

                        // Log.d(TAG, deckOptions.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "deck name: " + deckName);
                    if (!deckName.equals("Default"))
                        m_ankiDeckList.add(deck);

                } while (decksCursor.moveToNext());
            }

            m_adapter.notifyDataSetChanged();

            if( m_ankiDeckList.size() == 0 ) {
                showToast("No decks found, please add some flashcard decks in AnkiDroid");
            }

        } catch (Exception e) {
            Log.e(TAG, "Could not list AnkiDroid decks: " + e);
            showToast("Could not list AnkiDroid decks " + e.getMessage());
        }

    }

    Vector<AnkiDeck> m_ankiDeckList;
    private ListView m_deckList;
    private DeckAdapter m_adapter;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "position: " + i);

        AnkiDeck deck = (AnkiDeck) m_adapter.getItem(i);
        Log.d(TAG, "selected deck: " + deck.deckName + " deckId: " + deck.deckId);

        // launch review activity
        Intent intent = new Intent(DeckPickerActivity.this, ReviewActivity.class);
        intent.putExtra("deckId", deck.deckId);
        this.startActivity(intent);
    }
    //private List<String> m_decks = new LinkedList<String>();

    private BackgroundManager m_backgroundManager;

}
