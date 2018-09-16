package com.luc.ankireview;

import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    public static final int EASE_1 = 1;
    public static final int EASE_2 = 2;
    public static final int EASE_3 = 3;
    public static final int EASE_4 = 4;

    public static Pattern s_soundPattern = Pattern.compile("\\[sound\\:([^\\[\\]]*)\\]");
    private static final String TAG = "Card";


    public Card(long noteId, int cardOrd, String question, String answer, int buttonCount) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_question = question;
        m_buttonCount = buttonCount;

        // does the answer content have a sound ?
        m_answer = filterSound(answer);
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


    public int getEaseBad() {
        return EASE_1;
    }

    public int getEaseGood() {
        switch (m_buttonCount) {
            case 2:
                return EASE_2;
            case 3:
                return EASE_2;
            case 4:
                return EASE_3;
            default:
                return 0;
        }
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
}
