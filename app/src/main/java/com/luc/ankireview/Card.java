package com.luc.ankireview;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    public static Pattern s_soundPattern = Pattern.compile("\\[sound\\:([^\\[\\]]*)\\]");
    private static final String TAG = "Card";


    public Card(long noteId, int cardOrd, String question, String answer, int buttonCount, Vector<String> nextReviewTimes) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_question = question;
        m_buttonCount = buttonCount;

        // does the answer content have a sound ?
        m_answer = filterSound(answer);

        m_nextReviewTimes = nextReviewTimes;
    }

    private String filterSound( String content ) {
        Matcher matcher = s_soundPattern.matcher(content);
        // While there is matches of the pattern for sound markers
        while (matcher.find()) {
            String sound = matcher.group(1);
            Log.v(TAG, "sound: " + sound);
            m_answerSound = sound;
        }

        content = matcher.replaceAll("");

        return content;
    }

    public String getQuestion() {
        return m_question;
    }

    public String getAnswer() {
        return m_answer;
    }

    public String getAnswerAudio()
    {
        return m_answerSound;
    }

    public int getButtonCount()
    {
        return m_buttonCount;
    }

    public long getNoteId() {
        return m_noteId;
    }

    public int getCardOrd() {
        return m_cardOrd;
    }


    public AnkiUtils.Ease getEaseBad() {
        return AnkiUtils.Ease.EASE_1;
    }

    public AnkiUtils.Ease getEaseGood() {
        switch (m_buttonCount) {
            case 2:
                return AnkiUtils.Ease.EASE_2;
            case 3:
                return AnkiUtils.Ease.EASE_2;
            case 4:
                return AnkiUtils.Ease.EASE_3;
            default:
                return AnkiUtils.Ease.EASE_1;
        }
    }

    public Vector<String> getEaseStrings(Resources resources) {
        Vector<String> choices = new Vector<String>();
        // build possible choices based on number of buttons
        switch (m_buttonCount) {
            case 2:
                choices.add(resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")");
                choices.add(resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(1) + ")");
                break;
            case 3:
                choices.add(resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")");
                choices.add(resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(1) + ")");
                choices.add(resources.getString(R.string.ease_button_easy) + " (" + m_nextReviewTimes.get(2) + ")");
                break;
            default:
                choices.add(resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")");
                choices.add(resources.getString(R.string.ease_button_hard) + " (" + m_nextReviewTimes.get(1) + ")");
                choices.add(resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(2) + ")");
                choices.add(resources.getString(R.string.ease_button_easy) + " (" + m_nextReviewTimes.get(3) + ")");
                break;
        }

        return choices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return m_noteId == card.m_noteId &&
                m_cardOrd == card.m_cardOrd;
    }

    @Override
    public int hashCode() {

        return Objects.hash(m_noteId, m_cardOrd);
    }

    private long m_noteId;
    private int m_cardOrd;
    private String m_question;
    private String m_answer;
    private String m_answerSound;
    private int m_buttonCount;
    private Vector<String> m_nextReviewTimes;
}
