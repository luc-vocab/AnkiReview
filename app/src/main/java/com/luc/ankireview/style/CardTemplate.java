package com.luc.ankireview.style;

import android.graphics.Typeface;

import com.luc.ankireview.style.CardField;

import java.util.Vector;

public class CardTemplate {

    public CardTemplate() {
        m_questionCardFields = new Vector<CardField>();
        m_answerCardFields = new Vector<CardField>();
        m_soundField = null;

        m_baseTextSize = 40;

        m_leftRightMargin_px = 0;
        m_centerMargin_px = 0;
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

    public int getBaseTextSize() { return m_baseTextSize; }
    public void setBaseTextSize(int margin) { m_baseTextSize = margin; }

    public int getLeftRightMargin() { return m_leftRightMargin_px; }
    public void setLeftRightMargin( int margin ) { m_leftRightMargin_px = margin; }

    public int getCenterMargin() { return m_centerMargin_px; }
    public void setCenterMargin( int margin ) { m_centerMargin_px = margin; }

    public int getPaddingTop() { return m_paddingTop; }
    public void setPaddingTop( int padding) { m_paddingTop = padding; }

    public int getPaddingBottom() { return m_paddingBottom; }
    public void setPaddingBottom( int padding )  { m_paddingBottom = padding; }

    public int getPaddingLeftRight() { return m_paddingLeftRight; }
    public void setPaddingLeftRight( int padding ) { m_paddingLeftRight = padding; }

    private Typeface m_typeface;

    private Vector<CardField> m_questionCardFields;
    private Vector<CardField> m_answerCardFields;
    private String m_soundField;

    // text
    private int m_baseTextSize;

    // margins
    private int m_leftRightMargin_px;
    private int m_centerMargin_px;

    private int m_paddingTop;
    private int m_paddingBottom;
    private int m_paddingLeftRight;


}
