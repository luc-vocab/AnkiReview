package com.luc.ankireview;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.Vector;

public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";

    public static class AnswerDialogFragment extends DialogFragment {

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof ReviewActivity){
                m_reviewActivity=(ReviewActivity) context;
            }
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Vector<String> choices = m_reviewActivity.getCurrentCard().getEaseStrings(getResources());
            builder.setTitle(R.string.pick_ease)
                    .setItems(choices.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "picked choice: " + which);

                            AnkiUtils.Ease ease = AnkiUtils.Ease.fromInt(which + 1);
                            m_reviewActivity.answerCustom(ease);
                        }
                    });
            return builder.create();
        }

        private ReviewActivity m_reviewActivity;
    }

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

        setContentView(R.layout.activity_review);

        m_firstTimeInitDone = false;

        String mediaDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AnkiDroid/collection.media/";
        Uri mediaDirUri = Uri.fromFile(new File(mediaDir));
        m_baseUrl = mediaDirUri.toString() +"/";

        m_frame = findViewById(R.id.review_frame);
        m_flashcardFrame = findViewById(R.id.flashcard_frame);
        m_touchLayer = findViewById(R.id.touch_layer);
        m_questionPager = findViewById(R.id.flashcard_question_pager);
        m_answerPager = findViewById(R.id.flashcard_answer_pager);

        m_progressBar = findViewById(R.id.review_progressbar);

        // set touch listener
        m_detector = new GestureDetectorCompat(this, new ReviewerGestureDetector());
        m_touchLayer.setOnTouchListener(m_gestureListener);


        m_questionAdapter = new FlashCardViewPagerAdapter(this, m_baseUrl,this );
        m_answerAdapter = new FlashCardViewPagerAdapter(this, m_baseUrl,this );

        m_questionPager.setAdapter(m_questionAdapter);
        m_answerPager.setAdapter(m_answerAdapter);

        m_questionPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                    if(mCurrentPosition != 1)
                    {
                        // user scrolled to one of the sides
                        showAnswer();
                    }
                }
            }
            private int mCurrentPosition = 1;
        });


        m_answerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        // setup audio
        // -----------

        m_mediaPlayer = new MediaPlayer();
        m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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


        Intent intent = getIntent();
        m_deckId = intent.getLongExtra("deckId", 0);
        Log.d(TAG, "ReviewActivity.onCreate, deckId: "  + m_deckId);

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
                    switch (speedDialActionItem.getId()) {
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
        ActionBar ab = getSupportActionBar();
        ab.setTitle(deckName);

        getSupportActionBar().setElevation(0);
        loadCards();
    }

    private void singleTapHandler() {
        Log.v(TAG, "singleTapHandler");
        playAnswerAudio();
    }

    private void doubleTapHandler() {
        Log.v(TAG, "doubleTapHandler");
        showAnswerMenu();
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



    }

    private void showAnswerMenu() {
        if( ! m_showingQuestion ) {
            AnswerDialogFragment dialog = new AnswerDialogFragment();
            dialog.show(getSupportFragmentManager(), "AnswerDialog");
        }
    }

    public void pageLoaded() {
        if( ! m_firstTimeInitDone) {
            // WebView seems to have a hardtime loading assets from the collection.media directory on first run
            // we reload the first question card once to get around this issue.
            Log.v(TAG, "reloading question for first time init");
            loadFirstQuestion();
            showQuestion();
            m_firstTimeInitDone = true;
        }
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

        Vector<Card> initialCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 2);
        if( initialCards.size() == 0 ) {
            // nothing to review
            reviewsDone();
        } else {
            m_currentCard = initialCards.get(0);
            // default to current card
            m_nextCard = m_currentCard;
            if( initialCards.size() == 2)
                m_nextCard = initialCards.get(1);

            // done loading cards, show first question
            showQuestion();
        }



    }

    private void loadFirstQuestion()
    {
        // if we've never shown a question before, do some first time setup

        // show question in the middle
        m_questionAdapter.setCardContent(m_currentCard.getQuestion(), true);
        m_questionPager.setCurrentItem(1);

        m_questionPager.bringToFront();

    }

    private void showQuestion() {
        if(m_isFirstCard)
        {
            loadFirstQuestion();
            m_isFirstCard = false;
        }

        // load current answer onto the sides (should not create visual disruption)
        m_questionAdapter.setCardContent(m_currentCard.getAnswer(), false);

        // the question pager data is already loaded. we only need to bring it to the front
        m_questionPager.bringToFront();

        // the question pager is now on top, we can make visual changes to the answer pager
        m_answerAdapter.setCardContent(m_currentCard.getAnswer(), true);
        m_answerPager.setCurrentItem(1);

        prepareAnswerAudio();

        m_cardReviewStartTime = System.currentTimeMillis();
        m_showingQuestion = true;

        setupSpeedDial();

    }

    private void showAnswer()
    {
        // load next question onto the sides (should not create visual disruption)
        String nextCardQuestion = "";
        if( m_nextCard != null)
            nextCardQuestion = m_nextCard.getQuestion();

        m_answerAdapter.setCardContent(nextCardQuestion, false);

        // the answer pager data is already loaded. we only need to bring it to the front
        m_answerPager.bringToFront();

        // load next question onto the middle page of the question pager
        m_questionAdapter.setCardContent(nextCardQuestion, true);

        // center the question adapter ( not visible currently)
        m_questionPager.setCurrentItem(1);


        m_showingQuestion = false;
        
        playAnswerAudio();

        setupSpeedDial();

    }

    private void prepareAnswerAudio() {
        if( m_currentCard.getAnswerAudio() != null)
        {
            Uri uri = Uri.parse(m_baseUrl + m_currentCard.getAnswerAudio());
            try {
                m_mediaPlayer.reset();
                m_mediaPlayer.setDataSource(getApplicationContext(), uri);
                m_mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void answerCard(AnkiUtils.Ease ease) {

        long timeTaken = Math.min(System.currentTimeMillis() - m_cardReviewStartTime, 60000);
        AnkiUtils.answerCard(getContentResolver(), m_currentCard, ease, timeTaken);

        //String msg = String.format("ease: %d time: %.1fs", ease.getValue(), timeTaken / 1000.0);
        //showToast(msg);
    }

    private void playAnswerAudio()
    {
        if( m_currentCard.getAnswerAudio() != null && ! m_showingQuestion) {
            m_mediaPlayer.start();
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

    public void markCard() {
        AnkiUtils.markCard(getContentResolver(), m_currentCard);
        showToast("Marked Card");
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
        ActionBar ab = getSupportActionBar();
        ab.setSubtitle("Cards due: learn: " + deckDueCounts.learnCount + " review: " + deckDueCounts.reviewCount + " new: " + deckDueCounts.newCount);
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
        Vector<Card> nextCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 5);

        if( nextCards.size() == 0) {
            // zero cards due. we've finished our reviews
            reviewsDone();
        } else {
            // move "next card" up to current card
            m_currentCard = m_nextCard;

            // now loop over the nextCards array. if we find a card which is different from
            // the current card, assign it to m_nextCard (to avoid reviewing the same card twice in a row).
            // if this is impossible ,then m_nextCard stays the same as m_currentCard.
            for(Card card : nextCards) {
                if( ! card.equals(m_currentCard)) {
                    m_nextCard = card;
                    break;
                }
            }

            showQuestion();
        }

    }

    private void showToast(String text)
    {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }


    private void reviewsDone() {
        showToast("End of cards reached");
        finish();
    }

    private long m_deckId;

    private boolean m_firstTimeInitDone;


    public Card getCurrentCard() { return m_currentCard; }
    private Card m_currentCard;
    private Card m_nextCard;

    // layout elements
    private FrameLayout m_frame;
    private FrameLayout m_flashcardFrame;
    private ViewPager m_questionPager;
    private ViewPager m_answerPager;
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

    // adapters
    private FlashCardViewPagerAdapter m_questionAdapter;
    private FlashCardViewPagerAdapter m_answerAdapter;
    // where to load file assets
    private String m_baseUrl;
    // for playing audio
    private MediaPlayer m_mediaPlayer;

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


}
