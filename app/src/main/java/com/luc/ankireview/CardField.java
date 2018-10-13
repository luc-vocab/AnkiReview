package com.luc.ankireview;

public class CardField {

    public CardField(String fieldName) {
        m_fieldName = fieldName;
    }

    public String getFieldName() {
        return m_fieldName;
    }

    public void setColor(int color) {
        m_textColor = color;
    }

    public int getColor() {
        return m_textColor;
    }


    private String m_fieldName;
    private int m_textColor;

}
