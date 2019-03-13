package com.luc.ankireview;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.luc.ankireview.animation.ReviewPageTransformer;
import com.luc.ankireview.display.BackgroundViewPagerAdapter;
import com.luc.ankireview.display.FlashCardViewPagerAdapter;
import com.luc.ankireview.display.FlashcardViewPager;
import com.luc.ankireview.style.CardStyle;
import com.luc.ankireview.style.CardTemplateKey;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class ReviewActivity extends AppCompatActivity {
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
            m_flashcardFrame.dispatchTouchEvent(event);
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

        String mediaDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AnkiDroid/collection.media/";
        Uri mediaDirUri = Uri.fromFile(new File(mediaDir));
        m_baseUrl = mediaDirUri.toString() +"/";

        Intent intent = getIntent();
        m_deckId = intent.getLongExtra("deckId", 0);
        Log.d(TAG, "ReviewActivity.onCreate, deckId: "  + m_deckId);

        m_firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        m_flashcardFrame = findViewById(R.id.flashcard_frame);
        m_styleNotFound = findViewById(R.id.cardstyle_not_defined);
        m_styleNotFound.setVisibility(View.INVISIBLE);
        m_touchLayer = findViewById(R.id.touch_layer);
        m_flashcardPager = findViewById(R.id.flashcard_pager);


        m_progressBar = findViewById(R.id.review_progressbar);

        // set touch listener
        m_detector = new GestureDetectorCompat(this, new ReviewerGestureDetector());
        m_touchLayer.setOnTouchListener(m_gestureListener);

        Button cardStyleButton = findViewById(R.id.define_cardstyle_button);
        cardStyleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCardStyle();
            }
        });


        // setupFlashcardPager();

        // setup ViewPager for backgrounds
        // -------------------------------
        if(Settings.ENABLE_BACKGROUNDS) {
            m_backgroundPager = findViewById(R.id.background_pager);
            m_backgroundAdapter = new BackgroundViewPagerAdapter(this, m_deckId);
            m_backgroundPager.setAdapter(m_backgroundAdapter);
            m_backgroundPager.setCurrentItem(1); // center

            // the flashcardpager will forward touch events to the backgroundpager
            m_flashcardPager.setBackgroundPager(m_backgroundPager);

            // when we move to one of the side pages, reload
            m_backgroundPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                @Override
                public void onPageSelected(int position) {
                    mCurrentPosition = position;
                }
                @Override
                public void onPageScrollStateChanged(int state) {
                    if(ViewPager.SCROLL_STATE_IDLE == state){
                        //Scrolling finished. Do something.
                        if(mCurrentPosition == 0)
                        {
                            if(Settings.ENABLE_BACKGROUNDS){
                                m_backgroundAdapter.moveToNextBackground(mCurrentPosition);
                            }
                            m_flashcardPager.disableBackgroundSwiping();
                        } else if(mCurrentPosition == 2)
                        {
                            if(Settings.ENABLE_BACKGROUNDS) {
                                m_backgroundAdapter.moveToNextBackground(mCurrentPosition);
                            }
                            m_flashcardPager.disableBackgroundSwiping();
                        }
                    }
                }
                private int mCurrentPosition = 1;
            });
            // m_backgroundPager.setPageTransformer(true, new AlphaPageTransformer());
        }


        // setup audio
        // -----------

        m_questionSoundMediaPlayer = new MediaPlayer();
        m_questionSoundMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        m_answerSoundMediaPlayer = new MediaPlayer();
        m_answerSoundMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        m_answerBadAudio = MediaPlayer.create(this, R.raw.cancel_41);
        m_answerGoodAudio = MediaPlayer.create(this, R.raw.select_13);

        // setup animation
        // ---------------

        m_correct = (ImageView) findViewById(R.id.correct_svg);
        m_incorrect = (ImageView) findViewById(R.id.incorrect_svg);

        m_correct.setVisibility(View.INVISIBLE);
        m_incorrect.setVisibility(View.INVISIBLE);

        int animationSpeed = 450;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(animationSpeed);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(animationSpeed);
        fadeOut.setDuration(animationSpeed);

        m_animationSet = new AnimationSet(false);
        m_animationSet.addAnimation(fadeIn);
        m_animationSet.addAnimation(fadeOut);

        m_animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                m_correct.setVisibility(View.INVISIBLE);
                m_incorrect.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
            @Override
            public void onAnimationStart(Animation animation) {

            }
        });




        // setup speed dial
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
        String deckName = AnkiUtils.getDeckName(getContentResolver(), m_deckId);

        // action bar
        m_toolbar = (Toolbar) findViewById(R.id.review_toolbar);
        m_toolbar.setTitle(deckName);
        setSupportActionBar(m_toolbar);

        // final step
        reloadCardStyleAndCards();
    }

    private void reloadCardStyleAndCards() {
        Log.v(TAG, "reloadCardStyleAndCards");

        setupFlashcardPager();

        m_cardStyle = new CardStyle(this);
        loadCards();
    }

    private void setupFlashcardPager() {
        // setup ViewPager for flashcards
        // ------------------------------

        m_flashcardAdapter = new FlashCardViewPagerAdapter(this, this );
        m_flashcardPager.setAdapter(m_flashcardAdapter);

        m_flashcardPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if(ViewPager.SCROLL_STATE_IDLE == state){
                    //Scrolling finished. Do something.
                    if(mCurrentPosition == 0)
                    {
                        Log.v(TAG, "Answer Pager: user scrolled left, card not memorized");
                        answerBad();

                    } else if(mCurrentPosition == 2)
                    {
                        Log.v(TAG, "Answer Pager: user scrolled right, card memorized");
                        answerGood();
                    }
                }
            }
            private int mCurrentPosition = 1;
        });

        // don't use any page transformers for now
        // m_flashcardPager.setPageTransformer(true, new ReviewPageTransformer());
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
            Intent intent = new Intent(ReviewActivity.this, CardStyleActivity.class);
            intent.putExtra("noteId", m_cardForCardStyleEdit.getNoteId());
            intent.putExtra("cardOrd", m_cardForCardStyleEdit.getCardOrd());
            this.startActivityForResult(intent,0);
        }
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


        // disable these until Anki 2.9
        /*
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

        */


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
            m_speedDialView.addActionItem(
                    new SpeedDialActionItem.Builder(resourceArray[i], R.drawable.tag)
                            .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.answer_tag_suspend, getTheme()))
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

        m_progressBar.setMax(m_initialDueCount * 100);
        m_progressBar.setProgress(0);

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
            Crashlytics.logException(e);
            Utils.reportAnkiAPIException(this, e);
        }





    }

    private void loadFirstQuestion()
    {
        // if we've never shown a question before, do some first time setup

        // show question in the middle
        m_flashcardAdapter.setCurrentCard(m_currentCard);
        m_flashcardAdapter.setNextCard(m_nextCard);
        m_flashcardPager.setCurrentItem(1);

    }

    private void showReviewControls() {
        m_flashcardFrame.setVisibility(View.VISIBLE);
        m_speedDialView.setVisibility(View.VISIBLE);
        m_styleNotFound.setVisibility(View.INVISIBLE);
    }

    private void showCardStyleNotDefinedControls() {
        m_flashcardFrame.setVisibility(View.INVISIBLE);
        m_speedDialView.setVisibility(View.INVISIBLE);
        m_styleNotFound.setVisibility(View.VISIBLE);
    }

    private void setupCardStyleHandler(Card card) {
        // store card information to handle clicks to card style
        m_cardForCardStyleEdit = card;
    }

    private boolean checkStyleExists(Card card) {
        if( ! m_cardStyle.styleExistsForCard(card)) {
            // redirect used to style activity
            // launchCardStyleForCard(card);

            showCardStyleNotDefinedControls();
            setupCardStyleHandler(card);

            return false;
        }
        return true;
    }

    private void showQuestion() {
        m_answerAudio = false;

        // play question audio if found
        playQuestionAudio(m_cardStyle.getQuestionAudio(m_currentCard));
        // prepare answer audio if found
        prepareAnswerAudio(m_cardStyle.getAnswerAudio(m_currentCard));

        if(m_isFirstCard)
        {
            loadFirstQuestion();
            m_isFirstCard = false;
        }

        m_cardReviewStartTime = System.currentTimeMillis();
        m_showingQuestion = true;

        m_flashcardPager.disableSwipe();

        setupSpeedDial();
    }

    public void showAnswer()
    {
        Log.v(TAG,"showAnswer");

        m_showingQuestion = false;
        playAnswerAudio();
        setupSpeedDial();
        m_flashcardPager.enableSwipe();
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
        showIncorrectAnimation();
        answerCard(m_currentCard.getEaseBad());
        moveToNextQuestion();
    }

    private void answerGood()
    {
        m_answerGoodAudio.start();
        showCorrectAnimation();
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


                setupSpeedDial();
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
        showToast("Tagged card " + tag);
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
        m_toolbar.setSubtitle("Cards due: learn: " + deckDueCounts.learnCount + " review: " + deckDueCounts.reviewCount + " new: " + deckDueCounts.newCount);
    }


    // animate progress bar
    private void setProgressAnimate(int progressTo)
    {
        ObjectAnimator animation = ObjectAnimator.ofFloat(m_progressBar, "progress", m_progressBar.getProgress(), progressTo * 100);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void showCorrectAnimation() {
        m_correct.startAnimation(m_animationSet);
        m_correct.setVisibility(View.VISIBLE);
    }

    private void showIncorrectAnimation() {
        m_incorrect.startAnimation(m_animationSet);
        m_incorrect.setVisibility(View.VISIBLE);
    }

    private void moveToNextQuestion()
    {
        AnkiUtils.DeckDueCounts deckDueCounts = AnkiUtils.getDeckDueCount(getContentResolver(), m_deckId);
        updateDueCountSubtitle(deckDueCounts);

        m_reviewCount++;
        if(m_reviewCount % 3 == 0) {
            // enable background swiping
            m_flashcardPager.enableBackgroundSwiping();
        }

        int currentDueCount = deckDueCounts.getTotalWithWeights();
        int numCardsDone = m_initialDueCount - currentDueCount;
        Log.v(TAG,"current due count: " + currentDueCount);

        if( numCardsDone >= m_cardsDone) {
            // we don't want the progress bar to move backwards (which can happen in some cases,
            // a single bad review can result in two due cards created on the queue
            m_cardsDone = numCardsDone;
            // m_progressBar.setProgress(m_cardsDone * 100);
            setProgressAnimate(m_cardsDone);
        }


        // retrieve next 5 cards due
        try {
            Vector<Card> nextCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 5);

            if (nextCards.size() == 0) {
                // zero cards due. we've finished our reviews
                reviewsDone();
            } else {
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


                int currentPage = m_flashcardPager.getCurrentItem();
                m_flashcardAdapter.moveToNextQuestion(currentPage, m_currentCard, m_nextCard);

                showQuestion();
            }
        } catch ( Exception e ) {
            Crashlytics.logException(e);
            Utils.reportAnkiAPIException(this, e);
        }

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
        finish();
    }

    private long m_deckId;

    private Card m_currentCard;
    private Card m_nextCard;

    // when the user clicks Card Style, they'll go edit this card template
    private Card m_cardForCardStyleEdit;

    private boolean m_answerAudio = false;

    private CardStyle m_cardStyle;
    public CardStyle getCardStyle() { return m_cardStyle; }

    // layout elements
    private Toolbar m_toolbar;
    private FrameLayout m_flashcardFrame;
    private FrameLayout m_styleNotFound;
    private FlashcardViewPager m_flashcardPager;
    private ViewPager m_backgroundPager;
    private FrameLayout m_touchLayer;

    private RoundCornerProgressBar m_progressBar;

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

    // adapters
    private FlashCardViewPagerAdapter m_flashcardAdapter;
    private BackgroundViewPagerAdapter m_backgroundAdapter;
    // where to load file assets
    private String m_baseUrl;
    // for playing audio
    private MediaPlayer m_questionSoundMediaPlayer;
    private MediaPlayer m_answerSoundMediaPlayer;

    // answer audio
    MediaPlayer m_answerBadAudio;
    MediaPlayer m_answerGoodAudio;

    // gesture detection
    private GestureDetectorCompat m_detector;

    // animations
    private ImageView m_correct;
    private ImageView m_incorrect;
    private AnimationSet m_animationSet;

    // speed dial button
    SpeedDialView m_speedDialView;

    private FirebaseAnalytics m_firebaseAnalytics;

}
