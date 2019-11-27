package com.luc.ankireview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.luc.ankireview.backgrounds.BackgroundManager;
import com.luc.ankireview.style.CardStyle;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.json.JSONArray;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class ReviewActivity extends AppCompatActivity implements DisplayOptionsDialog.DisplayOptionsDialogListener {
    private static final String TAG = "ReviewActivity";

    class ReviewerGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // need to return true to indicate interest in the event, otherwise the
            // remaining events won't be sent to us
            return true;
        }

        @Override
        public boolean onDoubleTap (MotionEvent e) {
            doubleTapHandler();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            singleTapHandler();
            return true; // consume single tap event
        }
    }

    private View.OnTouchListener m_gestureListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // let the viewpagers / webviews consume the event
            m_activeMotionLayout.dispatchTouchEvent(event);
            // run through gesture detector
            if (m_detector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");

        setContentView(R.layout.activity_review);

        m_baseUrl = Utils.getBaseUrl();

        Intent intent = getIntent();
        m_deckId = intent.getLongExtra("deckId", 0);
        Log.d(TAG, "ReviewActivity.onCreate, deckId: "  + m_deckId);

        m_firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // flashcard elements
        // ------------------

        m_flashcardFrameAnkiReview = findViewById(R.id.review_cards_ankireview);
        m_flashcardFrameTeacherMode = findViewById(R.id.review_cards_teachermode);

        m_styleNotFound = findViewById(R.id.cardstyle_not_defined);
        m_styleNotFound.setVisibility(View.INVISIBLE);

        m_cardTemplateName = findViewById(R.id.cardstyle_cardtemplate_name);

        // set touch listener
        m_detector = new GestureDetectorCompat(this, new ReviewerGestureDetector());
        m_touchLayer = findViewById(R.id.touch_layer);
        m_touchLayer.setOnTouchListener(m_gestureListener);

        // buttons for card style

        Button cardStyleButton = findViewById(R.id.define_cardstyle_button);
        cardStyleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCardStyle();
            }
        });



        // setup audio
        // -----------

        m_questionSoundMediaPlayer = new MediaPlayer();
        m_questionSoundMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        m_answerSoundMediaPlayer = new MediaPlayer();
        m_answerSoundMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        m_answerBadAudio = MediaPlayer.create(this, R.raw.cancel_41);
        m_answerGoodAudio = MediaPlayer.create(this, R.raw.select_13);


        // setup speed dial
        // ----------------
        m_speedDialView = findViewById(R.id.speedDial);

        m_speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {

                int actionId = speedDialActionItem.getId();

                if( actionId == R.id.reviewer_action_ease_1 ||
                    actionId == R.id.reviewer_action_ease_2 ||
                    actionId == R.id.reviewer_action_ease_3 ||
                    actionId == R.id.reviewer_action_ease_4) {

                    AnkiUtils.Ease ease = AnkiUtils.Ease.fromActionId(speedDialActionItem.getId());
                    answerCustom(ease);
                    return false;

                } else {
                    ArrayList<String> quicktagList = getQuicktagList();
                    switch (speedDialActionItem.getId()) {
                        case R.id.reviewer_action_quicktag_1:
                            tagCard(quicktagList.get(0));
                            return false;
                        case R.id.reviewer_action_quicktag_2:
                            tagCard(quicktagList.get(1));
                            return false;
                        case R.id.reviewer_action_quicktag_3:
                            tagCard(quicktagList.get(2));
                            return false;
                        case R.id.reviewer_action_quicktag_4:
                            tagCard(quicktagList.get(3));
                            return false;
                        case R.id.reviewer_action_quicktag_5:
                            tagCard(quicktagList.get(4));
                            return false;

                        case R.id.reviewer_action_add_quicktag:
                            showAddQuicktag();
                            return false;
                        case R.id.reviewer_action_mark:
                            markCard();
                            return false;
                        case R.id.reviewer_action_mark_bury:
                            markBuryCard();
                            return false;
                        case R.id.reviewer_action_mark_suspend:
                            markSuspendCard();
                            return false;
                        default:
                            return false;
                    }
                }
            }
        });


        // load deck name
        m_deckName = AnkiUtils.getDeckName(getContentResolver(), m_deckId);

        m_deckNameText = findViewById(R.id.deckName);
        m_learnCountText = findViewById(R.id.learnCount);
        m_reviewCountText = findViewById(R.id.reviewCount);
        m_newCountText = findViewById(R.id.newCount);

        m_learnCountText.setCharacterLists(TickerUtils.provideNumberList());
        m_reviewCountText.setCharacterLists(TickerUtils.provideNumberList());
        m_newCountText.setCharacterLists(TickerUtils.provideNumberList());

        // set deck name
        m_deckNameText.setText(m_deckName);

        // action bar
        m_toolbar = (Toolbar) findViewById(R.id.review_toolbar);
        setSupportActionBar(m_toolbar);


        setupCardMotionLayoutTransitions(m_flashcardFrameAnkiReview);
        setupCardMotionLayoutTransitions(m_flashcardFrameTeacherMode);

        m_backgroundPhoto = findViewById(R.id.background_photo);

        m_backgroundManager = new BackgroundManager();
        m_backgroundManager.fillImageView(m_backgroundPhoto);

        // final step
        reloadCardStyleAndCards();
    }

    private void reloadCardStyleAndCards() {
        Log.v(TAG, "reloadCardStyleAndCards");

        m_cardStyle = new CardStyle(this);
        if( ! m_cardStyle.deckDisplayModeConfigured(m_deckId)) {
            // show deck style prompt
            showDeckDisplayOptions();
        } else {
            setupFlashcardFrame();
            loadCards();
        }
    }


    private void setupCardMotionLayoutTransitions(MotionLayout motionLayout) {
        motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if( i == R.id.answer_shown){
                    showAnswer();
                }
                else if( i == R.id.answer_good_offscreen) {
                    answerGood();
                }
                else if( i == R.id.answer_bad_offscreen) {
                    answerBad();
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
    }

    private void setupFlashcardFrame() {
        CardStyle.DeckDisplayMode deckDisplayMode = m_cardStyle.getdeckDisplayMode(m_deckId);


        if( deckDisplayMode == CardStyle.DeckDisplayMode.ANKIREVIEW) {
            m_flashcardFrameTeacherMode .setVisibility(View.GONE);
            m_flashcardFrameAnkiReview.setVisibility(View.VISIBLE);
            m_activeMotionLayout = m_flashcardFrameAnkiReview;
            setupFlashcardFrameFromMotionLayout(m_activeMotionLayout);
        } else if (deckDisplayMode == CardStyle.DeckDisplayMode.TEACHER) {
            m_flashcardFrameTeacherMode .setVisibility(View.VISIBLE);
            m_flashcardFrameAnkiReview.setVisibility(View.GONE);
            m_activeMotionLayout = m_flashcardFrameTeacherMode;
            setupFlashcardFrameFromMotionLayout(m_activeMotionLayout );
        } else {
            Log.e(TAG, "not supported yet");
        }

    }

    private void setupFlashcardFrameFromMotionLayout(MotionLayout motionLayout) {

        // card views
        m_questionCardView = motionLayout.findViewById(R.id.question_card);
        m_answerCardView = motionLayout.findViewById(R.id.answer_card);
        m_nextQuestionCardView = motionLayout.findViewById(R.id.next_question_card);

        // card textviews
        m_questionTextView = m_questionCardView.findViewById(R.id.side_text);
        m_answerTextView = m_answerCardView.findViewById(R.id.side_text);
        m_nextQuestionTextView = m_nextQuestionCardView.findViewById(R.id.side_text);

        // intervals
        m_badAnswerInterval = motionLayout.findViewById(R.id.bad_answer_interval);
        m_goodAnswerInterval = motionLayout.findViewById(R.id.good_answer_interval);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deck_display_mode:
                showDeckDisplayOptions();
                return true;
            case R.id.cardstyle:
                Log.v(TAG, "card style selected");
                launchCardStyle();
                return true;

            case R.id.reset_quicktags:
                resetQuicktags();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void launchCardStyle() {
        if (m_cardForCardStyleEdit != null) {

            // is this deck enabled for AnkiReview style ?
            if ( ! m_cardStyle.usingAnkiReviewMode(m_deckId)) {

                new AlertDialog.Builder(this)
                        .setTitle("Please enable AnkiReview style")
                        .setMessage("Editing the Card Style only works if you've chosen AnkiReview style in Deck Display Mode.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }

                        })
                        .show();

            } else {

                Intent intent = new Intent(ReviewActivity.this, CardStyleActivity.class);
                intent.putExtra("noteId", m_cardForCardStyleEdit.getNoteId());
                intent.putExtra("cardOrd", m_cardForCardStyleEdit.getCardOrd());
                intent.putExtra("cardTemplateName", m_cardForCardStyleEdit.getCardTemplateName());
                intent.putExtra("deckName", m_deckName);
                this.startActivityForResult(intent, 0);
            }
        }
    }

    @Override
    public void onSelectAnkiHTMLMode() {
        m_cardStyle.chooseDeckDisplayMode(m_deckId, CardStyle.DeckDisplayMode.ANKIHTML);
        showReviewControls();
        m_firebaseAnalytics.logEvent(Analytics.DISPLAYOPTIONS_HTML, null);
        reloadCardStyleAndCards();
    }

    @Override
    public void onSelectAnkireviewMode() {
        m_cardStyle.chooseDeckDisplayMode(m_deckId, CardStyle.DeckDisplayMode.ANKIREVIEW);
        m_firebaseAnalytics.logEvent(Analytics.DISPLAYOPTIONS_ANKIREVIEW, null);
        reloadCardStyleAndCards();
        showReviewControls();
    }

    @Override
    public void onSelectTeacherMode() {
        m_cardStyle.chooseDeckDisplayMode(m_deckId, CardStyle.DeckDisplayMode.TEACHER);
        m_firebaseAnalytics.logEvent(Analytics.DISPLAYOPTIONS_TEACHER, null);
        reloadCardStyleAndCards();
        showReviewControls();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // card style has been saved
                Log.v(TAG, "card style has been updated");
                reloadCardStyleAndCards();
            } else if (resultCode == RESULT_CANCELED) {
                Log.v(TAG, "user canceled");
            }
        }
    }

    private void singleTapHandler() {
        Log.v(TAG, "singleTapHandler");
        playAnswerAudio();
    }

    private void doubleTapHandler() {
        Log.v(TAG, "doubleTapHandler");
    }

    private void setupSpeedDial() {
        // populate with possible choices

        if( m_currentCard == null) {
            // don't do anything
            return;
        }

        m_speedDialView.clearActionItems();

        if( ! m_showingQuestion) {
            // card answers choices. only display on answer

            Vector<AnkiUtils.AnswerChoice> answerChoices = m_currentCard.getAnswerChoices(getResources());
            Collections.reverse(answerChoices);
            for (AnkiUtils.AnswerChoice answerChoice : answerChoices) {
                m_speedDialView.addActionItem(
                        new SpeedDialActionItem.Builder(answerChoice.getActionId(), answerChoice.getDrawableId())
                                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), answerChoice.getColorId(), getTheme()))
                                .setLabel(answerChoice.getText())
                                .create());
            }
        }

        // other actions

        m_speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.reviewer_action_mark, R.drawable.tag)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme()))
                        .setLabel(R.string.reviewer_action_mark)
                        .create());


        m_speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.reviewer_action_mark_suspend, R.drawable.pause)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme()))
                        .setLabel(R.string.reviewer_action_mark_suspend)
                        .create());

        m_speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.reviewer_action_mark_bury, R.drawable.pause)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme()))
                        .setLabel(R.string.reviewer_action_mark_bury)
                        .create());


        // quicktags

        ArrayList<String> quicktagList = getQuicktagList();
        int resourceArray[] = {
                R.id.reviewer_action_quicktag_1,
                R.id.reviewer_action_quicktag_2,
                R.id.reviewer_action_quicktag_3,
                R.id.reviewer_action_quicktag_4,
                R.id.reviewer_action_quicktag_5
        };
        int i = 0;
        for(String quicktag : quicktagList) {

            int color = ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme());
            if (m_currentCard.getTagMap().contains(quicktag)) {
                color = ResourcesCompat.getColor(getResources(), R.color.answer_tag_disabled, getTheme());
            }

            m_speedDialView.addActionItem(
                    new SpeedDialActionItem.Builder(resourceArray[i], R.drawable.tag)
                            .setFabBackgroundColor(color)
                            .setLabel(quicktag)
                            .create());
            i++;
        }

        m_speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.reviewer_action_add_quicktag, R.drawable.tag)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme()))
                        .setLabel(R.string.reviewer_action_add_quicktag)
                        .create());


    }


    private void loadCards() {

        AnkiUtils.DeckDueCounts deckDueCounts = AnkiUtils.getDeckDueCount(getContentResolver(), m_deckId);
        updateDueCountSubtitle(deckDueCounts);
        m_initialDueCount = deckDueCounts.getTotalWithWeights();
        m_cardsDone = 0;
        m_isFirstCard = true;
        Log.v(TAG, "initial due count: " + m_initialDueCount);


        try {
            Vector<Card> initialCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 2);

            // ensure we have a style defined for all of these cards, otherwise don't continue
            for(Card card : initialCards) {
                if (! checkStyleExists(card)) {
                    // don't continue
                    return;
                }
            }


            if( initialCards.size() == 0 ) {
                // nothing to review
                reviewsDone();
            } else {
                m_currentCard = initialCards.get(0);
                setupCardStyleHandler(m_currentCard);

                // default to current card
                m_nextCard = m_currentCard;
                if( initialCards.size() == 2)
                    m_nextCard = initialCards.get(1);

                // done loading cards, show first question
                showQuestion();

                showReviewControls();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Utils.reportAnkiAPIException(this, e);
        }





    }

    private void loadFirstQuestion()
    {
        // if we've never shown a question before, do some first time setup

        // m_currentCard
        CardStyle cardStyle = getCardStyle();

        // render the current card
        cardStyle.renderBothCards(m_currentCard, m_questionCardView, m_answerCardView, m_questionTextView, m_answerTextView);

        // set the intervals on the good/bad notifications
        m_goodAnswerInterval.setText(m_currentCard.getGoodAnswerInterval());
        m_badAnswerInterval.setText(m_currentCard.getBadAnswerInterval());

        // render the next card
        cardStyle.renderQuestion(m_nextCard, m_nextQuestionCardView, m_nextQuestionTextView);

        cardStyle.applyMotionLayoutStyle(m_currentCard, m_activeMotionLayout);
    }

    private void loadNextQuestion() {
        CardStyle cardStyle = getCardStyle();

        // render the current card (already rolled forward)
        cardStyle.renderBothCards(m_currentCard, m_questionCardView, m_answerCardView, m_questionTextView, m_answerTextView);

        // transition back to "question shown" state, immediately
        m_activeMotionLayout.setProgress(0.0f);
        m_activeMotionLayout.setTransition(R.id.question_shown, R.id.answer_shown);

        // once we are in "question shown", render the next card (it is currently invisible)
        m_cardStyle.renderQuestion(m_nextCard, m_nextQuestionCardView, m_nextQuestionTextView);

        // set the intervals on the good/bad notifications
        m_goodAnswerInterval.setText(m_currentCard.getGoodAnswerInterval());
        m_badAnswerInterval.setText(m_currentCard.getBadAnswerInterval());

    }

    private void showReviewControls() {
        m_activeMotionLayout.setVisibility(View.VISIBLE);
        m_speedDialView.setVisibility(View.VISIBLE);
        m_styleNotFound.setVisibility(View.INVISIBLE);
    }

    private void showCardStyleNotDefinedControls(String cardTemplateName) {
        m_activeMotionLayout.setVisibility(View.INVISIBLE);
        m_speedDialView.setVisibility(View.INVISIBLE);
        m_styleNotFound.setVisibility(View.VISIBLE);
        m_cardTemplateName.setText(cardTemplateName);
    }

    private void showDeckDisplayOptions() {
        // start up dialog

        DisplayOptionsDialog dialog = new DisplayOptionsDialog(this);
        dialog.show(getSupportFragmentManager(), "DisplayOptionsDialog");
    }

    private void setupCardStyleHandler(Card card) {
        // store card information to handle clicks to card style
        m_cardForCardStyleEdit = card;
    }

    private boolean checkStyleExists(Card card) {
        boolean useAnkiReviewDeckDisplayMode = m_cardStyle.usingAnkiReviewMode(m_deckId);
        Log.v(TAG, "useAnkiReviewDeckDisplayMode: " + useAnkiReviewDeckDisplayMode);

        if( useAnkiReviewDeckDisplayMode )
        {
            if( ! m_cardStyle.styleExistsForCard(card)) {
                // redirect used to style activity
                // launchCardStyleForCard(card);

                showCardStyleNotDefinedControls(card.getCardTemplateName());
                setupCardStyleHandler(card);

                return false;
            }
        }

        return true;
    }

    private void showQuestion() {
        m_answerAudio = false;

        // play question audio if found
        playQuestionAudio(m_cardStyle.getQuestionAudio(m_deckId, m_currentCard));
        // prepare answer audio if found
        prepareAnswerAudio(m_cardStyle.getAnswerAudio(m_deckId, m_currentCard));

        if(m_isFirstCard)
        {
            loadFirstQuestion();
            m_isFirstCard = false;
        }

        m_cardReviewStartTime = System.currentTimeMillis();
        m_showingQuestion = true;

        setupSpeedDial();
    }

    public void showAnswer()
    {
        Log.v(TAG,"showAnswer start");

        m_showingQuestion = false;
        playAnswerAudio();
        setupSpeedDial();

        Log.v(TAG, "showAnswer end");
    }

    private void playQuestionAudio(String audioFile) {
        if( audioFile != null) {
            Uri uri = Uri.parse(m_baseUrl + audioFile);
            try {
                m_questionSoundMediaPlayer.reset();
                m_questionSoundMediaPlayer.setDataSource(getApplicationContext(), uri);
                m_questionSoundMediaPlayer.prepare();
                m_questionSoundMediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareAnswerAudio(String audioFile) {

        if( audioFile != null ) {
            Uri uri = Uri.parse(m_baseUrl + audioFile);
            try {
                m_answerSoundMediaPlayer.reset();
                m_answerSoundMediaPlayer.setDataSource(getApplicationContext(), uri);
                m_answerSoundMediaPlayer.prepare();

                m_answerAudio = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void answerCard(AnkiUtils.Ease ease) {

        long timeTaken = Math.min(System.currentTimeMillis() - m_cardReviewStartTime, 60000);
        AnkiUtils.answerCard(getContentResolver(), m_currentCard, ease, timeTaken);

        // String msg = String.format("ease: %d time: %.1fs", ease.getValue(), timeTaken / 1000.0);
        // showToast(msg);
    }

    private void playAnswerAudio()
    {
        if( m_answerAudio && ! m_showingQuestion) {
            m_answerSoundMediaPlayer.start();
        }
    }

    private void answerBad()
    {
        m_answerBadAudio.start();
        answerCard(m_currentCard.getEaseBad());
        moveToNextQuestion();
    }

    private void answerGood()
    {
        m_answerGoodAudio.start();
        answerCard(m_currentCard.getEaseGood());
        moveToNextQuestion();
    }

    public void showAddQuicktag() {


        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add Quicktag");


        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                value = value.replaceAll("\\s+", "");
                Log.v(TAG, "new tag: " + value);
                addQuicktag(value);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }


    private ArrayList<String> getQuicktagList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayList<String> quickTagList = new ArrayList<String>();

        try{

            JSONArray quickTagArray = new JSONArray(prefs.getString(Settings.PREFERENCES_KEY_QUICKTAGS, "[]"));
            for (int i = 0; i < quickTagArray.length(); i++) {
                String currentTag = quickTagArray.getString(i);
                quickTagList.add(currentTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quickTagList;
    }

    private void resetQuicktags() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Settings.PREFERENCES_KEY_QUICKTAGS, null);
        editor.commit();
        showToast("Quicktags reset");
        setupSpeedDial();
    }

    public void addQuicktag(String newTag) {

        ArrayList<String> quickTagList = getQuicktagList();
        if( ! quickTagList.contains(newTag)){

            if (quickTagList.size() >= Settings.MAX_QUICKTAGS) {
                showToast("You can set a maximum of " + Settings.MAX_QUICKTAGS + " quicktags");
                return;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                JSONArray quickTagArray = new JSONArray(prefs.getString(Settings.PREFERENCES_KEY_QUICKTAGS, "[]"));
                quickTagArray.put(newTag);

                // write back to preferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Settings.PREFERENCES_KEY_QUICKTAGS, quickTagArray.toString());
                System.out.println(quickTagArray.toString());
                editor.commit();


            } catch(Exception e) {
                e.printStackTrace();

            }
        }
        // tag card regardless of whether the quicktag existed or not
        tagCard(newTag);

    }

    public void markCard() {
        AnkiUtils.markCard(getContentResolver(), m_currentCard);
        showToast("Marked Card");
    }

    public void tagCard(String tag) {
        AnkiUtils.tagCard(getContentResolver(), m_currentCard, tag);
        // add the tag locally so that the speedial can reflect this new tag
        m_currentCard.getTagMap().add(tag);
        showToast("Tagged card " + tag);
        setupSpeedDial();
    }

    public void markSuspendCard() {
        AnkiUtils.markCard(getContentResolver(), m_currentCard);
        AnkiUtils.suspendCard(getContentResolver(), m_currentCard);
        showAnswer();
        moveToNextQuestion();
        showToast("Marked and Suspended Card");
    }

    public void markBuryCard() {
        AnkiUtils.markCard(getContentResolver(), m_currentCard);
        AnkiUtils.buryCard(getContentResolver(), m_currentCard);
        showAnswer();
        moveToNextQuestion();
        showToast("Marked and Buried Card");
    }

    public void answerCustom(AnkiUtils.Ease ease) {
        answerCard(ease);
        moveToNextQuestion();
    }

    private void updateDueCountSubtitle(AnkiUtils.DeckDueCounts deckDueCounts) {
        m_learnCountText.setText(String.valueOf(deckDueCounts.learnCount));
        m_reviewCountText.setText(String.valueOf(deckDueCounts.reviewCount));
        m_newCountText.setText(String.valueOf(deckDueCounts.newCount));
        //showToast("learn: " + deckDueCounts.learnCount + " review: " + deckDueCounts.reviewCount + " new: " + deckDueCounts.newCount);
    }


    private void moveToNextQuestion()
    {

        AnkiUtils.DeckDueCounts deckDueCounts = AnkiUtils.getDeckDueCount(getContentResolver(), m_deckId);
        updateDueCountSubtitle(deckDueCounts);

        m_reviewCount++;

        int currentDueCount = deckDueCounts.getTotalWithWeights();
        int numCardsDone = m_initialDueCount - currentDueCount;
        Log.v(TAG,"current due count: " + currentDueCount);

        if( numCardsDone >= m_cardsDone) {
            // we don't want the progress bar to move backwards (which can happen in some cases,
            // a single bad review can result in two due cards created on the queue
            m_cardsDone = numCardsDone;

            retrieveFollowingCards();


        } else {
            retrieveFollowingCards();
        }

        showQuestion();


    }

    private void retrieveFollowingCards() {
        // retrieve next 5 cards due
        try {
            Vector<Card> nextCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 5);

            if (nextCards.size() == 0) {
                // zero cards due. we've finished our reviews
                reviewsDone();
            } else {

                // check whether all cards loaded have a style associated with them
                for(Card card : nextCards) {
                    if (! checkStyleExists(card)) {
                        // don't continue
                        return;
                    }
                }

                // move "next card" up to current card
                m_currentCard = m_nextCard;
                setupCardStyleHandler(m_currentCard);

                // now loop over the nextCards array. if we find a card which is different from
                // the current card, assign it to m_nextCard (to avoid reviewing the same card twice in a row).
                // if this is impossible ,then m_nextCard stays the same as m_currentCard.
                for (Card card : nextCards) {
                    if (!card.equals(m_currentCard)) {
                        m_nextCard = card;
                        break;
                    }
                }

                loadNextQuestion();
            }
        } catch ( Exception e ) {
            Crashlytics.logException(e);
            Utils.reportAnkiAPIException(this, e);
        }

        if ((m_reviewCount % 3) == 0) {{
            // cycle background
            m_backgroundManager.fillImageView(m_backgroundPhoto);
        }}

        Bundle bundle = new Bundle();
        bundle.putInt(Analytics.REVIEW_COUNT, m_reviewCount);
        m_firebaseAnalytics.logEvent(Analytics.REVIEW_PROGRESS, bundle);
    }

    private void showToast(String text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }


    private void reviewsDone() {
        Bundle bundle = new Bundle();
        bundle.putInt(Analytics.REVIEW_COUNT, m_reviewCount);
        m_firebaseAnalytics.logEvent(Analytics.REVIEW_END, bundle);


        showToast("End of cards reached");

        setResult(Activity.RESULT_OK, null);
        finish();
    }

    private long m_deckId;
    private String m_deckName;

    private Card m_currentCard;
    private Card m_nextCard;

    // when the user clicks Card Style, they'll go edit this card template
    private Card m_cardForCardStyleEdit;

    private boolean m_answerAudio = false;

    private CardStyle m_cardStyle;
    public CardStyle getCardStyle() { return m_cardStyle; }


    // layout elements
    private Toolbar m_toolbar;
    private MotionLayout m_activeMotionLayout;
    private MotionLayout m_flashcardFrameAnkiReview;
    private MotionLayout m_flashcardFrameTeacherMode;

    // card views
    private CardView m_questionCardView;
    private CardView m_answerCardView;
    private CardView m_nextQuestionCardView;

    // card textviews
    private TextView m_questionTextView;
    private TextView m_answerTextView;
    private TextView m_nextQuestionTextView;

    // answer intervals
    private TextView m_badAnswerInterval;
    private TextView m_goodAnswerInterval;



    private FrameLayout m_styleNotFound;

    private TextView m_cardTemplateName;


    // keep track of review time
    private long m_cardReviewStartTime;

    // keep track of init
    boolean m_isFirstCard;

    // keep track of whether we are displaying question
    boolean m_showingQuestion;

    // keep track of due counts
    int m_initialDueCount;
    int m_cardsDone; // not due anymore
    int m_reviewCount = 0;

    private TextView m_deckNameText;
    private TickerView m_learnCountText;
    private TickerView m_reviewCountText;
    private TickerView m_newCountText;

    // where to load file assets
    private String m_baseUrl;
    // for playing audio
    private MediaPlayer m_questionSoundMediaPlayer;
    private MediaPlayer m_answerSoundMediaPlayer;

    // answer audio
    MediaPlayer m_answerBadAudio;
    MediaPlayer m_answerGoodAudio;

    // gesture detection
    private FrameLayout m_touchLayer;
    private GestureDetectorCompat m_detector;


    // speed dial button
    SpeedDialView m_speedDialView;

    BackgroundManager m_backgroundManager;
    ImageView m_backgroundPhoto;

    private FirebaseAnalytics m_firebaseAnalytics;

}
