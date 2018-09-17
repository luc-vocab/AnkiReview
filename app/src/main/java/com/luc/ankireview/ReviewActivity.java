package com.luc.ankireview;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ichi2.anki.FlashCardsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

        m_mediaPlayer = new MediaPlayer();
        m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        Intent intent = getIntent();
        m_deckId = intent.getLongExtra("deckId", 0);



        Log.d(TAG, "ReviewActivity.onCreate, deckId: "  + m_deckId);

        loadCards();
    }

    private void singleTapHandler() {
        Log.v(TAG, "singleTapHandler");
        playAnswerAudio();
    }

    private void doubleTapHandler() {
        Log.v(TAG, "doubleTapHandler");
    }

    public void pageLoaded() {
        if( ! m_firstTimeInitDone) {
            // WebView seems to have a hardtime loading assets from the collection.media directory on first run
            // we reload the first question card once to get around this issue.
            Log.v(TAG, "reloading question for first time init");
            showQuestion();
            m_firstTimeInitDone = true;
        }
    }

    private void loadCards() {

        m_initialDueCount = AnkiUtils.getDeckDueCount(getContentResolver(), m_deckId);
        m_currentDueCount = m_initialDueCount;
        Log.v(TAG, "initial due count: " + m_initialDueCount);

        m_progressBar.setMax(m_initialDueCount);
        m_progressBar.setProgress(0);

        Vector<Card> initialCards = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 2);
        if( initialCards.size() == 0 ) {
            // nothing to review
            reviewsDone();
        } else {
            m_currentCard = initialCards.get(0);
            if( initialCards.size() == 2)
                m_nextCard = initialCards.get(1);
            else
                m_nextCard = null;

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
        if(m_initialDueCount == m_currentDueCount)
        {
            loadFirstQuestion();
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

        playAnswerAudio();

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

    private void answerCard(int ease) {

        Uri reviewInfoUri = FlashCardsContract.ReviewInfo.CONTENT_URI;
        ContentValues values = new ContentValues();
        long timeTaken = System.currentTimeMillis() - m_cardReviewStartTime; //<- insert real time taken here

        values.put(FlashCardsContract.ReviewInfo.NOTE_ID, m_currentCard.getNoteId());
        values.put(FlashCardsContract.ReviewInfo.CARD_ORD, m_currentCard.getCardOrd());
        values.put(FlashCardsContract.ReviewInfo.EASE, ease);
        values.put(FlashCardsContract.ReviewInfo.TIME_TAKEN, timeTaken);
        getContentResolver().update(reviewInfoUri, values, null, null);

        String msg = String.format("ease: %d time: %.1fs", ease, timeTaken / 1000.0);
        showToast(msg);
    }

    private void playAnswerAudio()
    {
        if( m_currentCard.getAnswerAudio() != null) {
            m_mediaPlayer.start();
        }
    }

    private void answerBad()
    {
        answerCard(m_currentCard.getEaseBad());
        moveToNextQuestion();
    }

    private void answerGood()
    {
        answerCard(m_currentCard.getEaseGood());
        moveToNextQuestion();
    }


    private void moveToNextQuestion()
    {
        m_currentDueCount = AnkiUtils.getDeckDueCount(getContentResolver(), m_deckId);
        Log.v(TAG,"current due count: " + m_currentDueCount);

        m_progressBar.setProgress(m_initialDueCount - m_currentDueCount);

        Vector<Card> nextTwo = AnkiUtils.getDueCards(getContentResolver(), m_deckId, 2);

        if( nextTwo.size() == 0) {
            // zero cards due. we've finished our reviews
            reviewsDone();
        } else if ( nextTwo.size() == 2 ) {
            // we got two cards back.
            // current card is now the previous "next card"
            m_currentCard = m_nextCard;
            // next card is the second card retrieved
            m_nextCard = nextTwo.get(1);
            showQuestion();
        } else {
            // only one card retrieved
            // still move the previous "next card" to the current
            m_currentCard = m_nextCard;
            m_nextCard = nextTwo.get(0);
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


    private Card m_currentCard;
    private Card m_nextCard;

    // layout elements
    private FrameLayout m_frame;
    private FrameLayout m_flashcardFrame;
    private ViewPager m_questionPager;
    private ViewPager m_answerPager;
    private FrameLayout m_touchLayer;

    private ProgressBar m_progressBar;

    // keep track of review time
    private long m_cardReviewStartTime;

    // keep track of due counts
    int m_initialDueCount;
    int m_currentDueCount;

    // adapters
    private FlashCardViewPagerAdapter m_questionAdapter;
    private FlashCardViewPagerAdapter m_answerAdapter;
    // where to load file assets
    private String m_baseUrl;
    // for playing audio
    private MediaPlayer m_mediaPlayer;

    // gesture detection
    private GestureDetectorCompat m_detector;


}
