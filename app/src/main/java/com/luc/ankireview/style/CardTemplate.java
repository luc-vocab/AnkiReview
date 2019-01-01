package com.luc.ankireview.style;

import android.graphics.Typeface;

import com.luc.ankireview.style.CardField;

import java.util.Vector;

public class CardTemplate {

    public CardTemplate() {
        m_questionCardFields = new Vector<CardField>();
        m_answerCardFields = new Vector<CardField>();
        m_soundField = null;
        m_leftRightMargin_px = 0;
    }


    public void setTypeface(Typeface typeface) {
        m_typeface = typeface;
    }

    public Typeface getTypeface() {
        return m_typeface;
    }

    public void addQuestionCardField(CardField cardField) {
        m_questionCardFields.add(cardField);
    }

    public void addAnswerCardField(CardField cardField) {
        m_answerCardFields.add(cardField);
    }


    public Vector<CardField> getQuestionCardFields() {
        return m_questionCardFields;
    }

    public Vector<CardField> getAnswerCardFields() {
        return m_answerCardFields;
    }


    public String getSoundField() { return m_soundField; }

    public void setSoundField(String  soundField) { m_soundField = soundField; }

    public int getLeftRightMargin() { return m_leftRightMargin_px; }
    public void setLeftRightMargin( int margin ) { m_leftRightMargin_px = margin; }

    private Typeface m_typeface;

    private Vector<CardField> m_questionCardFields;
    private Vector<CardField> m_answerCardFields;
    private String m_soundField;
    private int m_leftRightMargin_px;

}
