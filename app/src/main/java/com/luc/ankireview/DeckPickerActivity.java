package com.luc.ankireview;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ichi2.anki.FlashCardsContract;
import com.kobakei.ratethisapp.RateThisApp;
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
                showDeckPickerMessage(R.string.deckpicker_missingpermissions_title, R.string.deckpicker_missingpermissions_description, null);

                m_firebaseAnalytics.logEvent(Analytics.MISSING_PERMISSION, null);
            }
        }

        if(allPermissionsGranted) {
            listDecks();
        }

        return;

    }


    private void showDeckPickerMessage(int titleResource, int descriptionResource, String extra) {

        m_deckPickerMessageTitle.setText(titleResource);
        m_deckPickerMessageDescription.setText(descriptionResource);
        if( extra != null) {
            m_deckPickerMessageExtra.setVisibility(View.VISIBLE);
            m_deckPickerMessageExtra.setText(extra);
        } else {
            m_deckPickerMessageExtra.setVisibility(View.GONE);
            m_deckPickerMessageExtra.setText(null);
        }

        m_deckPickerMessage.setVisibility(View.VISIBLE);
        m_deckList.setVisibility(View.INVISIBLE);
    }

    private void showDecks() {
        m_deckPickerMessage.setVisibility(View.INVISIBLE);
        m_deckList.setVisibility(View.VISIBLE);
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

        // check whether ankidroid is installed
        if ( ! Utils.isAppInstalled(this, "com.ichi2.anki")) {
            showDeckPickerMessage(R.string.deckpicker_ankidroid_not_found_title, R.string.deckpicker_ankidroid_not_found_description, null);
            m_firebaseAnalytics.logEvent(Analytics.MISSING_ANKIDROID, null);
            return;
        }


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
        m_firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // rating request library
        RateThisApp.onCreate(this);
        RateThisApp.Config config = new RateThisApp.Config(0, 0);
        config.setMessage(R.string.review_request_message);
        RateThisApp.init(config);

        Toolbar toolbar = (Toolbar) findViewById(R.id.deckpicker_toolbar);
        setSupportActionBar(toolbar);

        m_deckList = findViewById(R.id.deck_list);
        m_deckList.setOnItemClickListener(this);

        m_ankiDeckList = new Vector<AnkiDeck>();

        m_adapter = new DeckAdapter(this, m_ankiDeckList);
        m_deckList.setAdapter(m_adapter);

        m_deckPickerMessage = findViewById(R.id.deckpicker_message);
        m_deckPickerMessage.setVisibility(View.INVISIBLE);

        m_deckPickerMessageTitle = findViewById(R.id.deckpicker_message_title);
        m_deckPickerMessageDescription = findViewById(R.id.deckpicker_message_description);
        m_deckPickerMessageExtra = findViewById(R.id.deckpicker_message_extra);

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
            Vector<AnkiDeck> noDueDeckList = new Vector<AnkiDeck>();

            Cursor decksCursor = getContentResolver().query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null);
            if (decksCursor.moveToFirst()) {
                do {
                    long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
                    String deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));

                    AnkiDeck deck = new AnkiDeck();
                    deck.deckId = deckID;
                    deck.deckName = deckName.replaceAll("::", " > ");

                    try {
                        JSONObject deckOptions = new JSONObject(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.OPTIONS)));
                        JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                        deck.deckDueCounts = AnkiUtils.parseDeckDueCount(deckCounts);

                        // Log.d(TAG, deckOptions.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "deck name: " + deckName);
                    if (!deckName.equals("Default")) {

                        if( deck.deckDueCounts.getTotal() > 0) {
                            m_ankiDeckList.add(deck);
                        } else {
                            noDueDeckList.add(deck);
                        }
                    }


                } while (decksCursor.moveToNext());
            }

            m_adapter.notifyDataSetChanged();

            if( m_ankiDeckList.size() == 0 ) {
                if ( noDueDeckList.size() > 0) {
                    showDeckPickerMessage(R.string.deckpicker_nocardsdue_title, R.string.deckpicker_nocardsdue_description, null);
                    m_firebaseAnalytics.logEvent(Analytics.NO_DUEDECKS, null);
                } else {
                    showDeckPickerMessage(R.string.deckpicker_nodecksfound_title, R.string.deckpicker_nodecksfound_description, null);
                    m_firebaseAnalytics.logEvent(Analytics.NO_DECKS, null);
                }
            } else {
                Bundle bundle = new Bundle();
                bundle.putInt(Analytics.DECK_COUNT, m_ankiDeckList.size());
                m_firebaseAnalytics.logEvent(Analytics.LIST_DECKS, bundle);
                showDecks();
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.e(TAG, "Could not list AnkiDroid decks: " + e);
            showDeckPickerMessage(R.string.deckpicker_cannotlistdecks_title, R.string.deckpicker_cannotlistdecks_description, e.getMessage());
        }

    }

    Vector<AnkiDeck> m_ankiDeckList;
    private ListView m_deckList;
    private DeckAdapter m_adapter;

    private FrameLayout m_deckPickerMessage;
    private TextView m_deckPickerMessageTitle;
    private TextView m_deckPickerMessageDescription;
    private TextView m_deckPickerMessageExtra;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "position: " + i);

        AnkiDeck deck = (AnkiDeck) m_adapter.getItem(i);
        Log.d(TAG, "selected deck: " + deck.deckName + " deckId: " + deck.deckId);

        m_firebaseAnalytics.logEvent(Analytics.REVIEW_START, null);

        // launch review activity
        Intent intent = new Intent(DeckPickerActivity.this, ReviewActivity.class);
        intent.putExtra("deckId", deck.deckId);
        this.startActivityForResult(intent, 0);
    }
    //private List<String> m_decks = new LinkedList<String>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "review session has been completed");

                // show review request
                // If the condition is satisfied, "Rate this app" dialog will be shown
                RateThisApp.showRateDialogIfNeeded(this);

                // RatingRequest.with(this).message(getString(R.string.review_request_message)).register();
            } else if (resultCode == RESULT_CANCELED) {
                Log.v(TAG, "review session has been interrupted");
            }
        }
    }

    private BackgroundManager m_backgroundManager;
    private FirebaseAnalytics m_firebaseAnalytics;

}
