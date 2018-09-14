package com.luc.ankireview;

import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    public static Pattern s_soundPattern = Pattern.compile("\\[sound\\:([^\\[\\]]*)\\]");
    private static final String TAG = "Card";


    public Card(long noteId, int cardOrd, String question, String answer) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_question = question;

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
}
