package com.luc.ankireview;

import java.util.Objects;

public class Card {
    public Card(long noteId, int cardOrd, String question, String answer) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_question = question;
        m_answer = answer;
    }

    private long m_noteId;
    private int m_cardOrd;
    private String m_question;
    private String m_answer;

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
}
