package com.luc.ankireview.style;

import android.text.Layout;

public class CardField {
    public static final int DEFAULT_COLOR = -1;
    public static final float RELATIVE_SIZE_DEFAULT = 1.0f;
    public static final Layout.Alignment DEFAULT_ALIGNMENT = Layout.Alignment.ALIGN_CENTER;

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

    public void setLineReturn(boolean lineReturn) {
        m_lineReturn = lineReturn;
    }

    public boolean getLineReturn() {
        return m_lineReturn;
    }

    public void setRelativeSize(float relativeSize) {
        m_relativeSize = relativeSize;
    }

    public float getRelativeSize() {
        return m_relativeSize;
    }

    public void setAlignment(Layout.Alignment alignment) {
        m_alignment = alignment;
    }

    public Layout.Alignment getAlignment() {
        return m_alignment;
    }

    public void setLeftMargin(int margin) {
        m_leftMargin = margin;
    }

    public int getLeftMargin() {
        return m_leftMargin;
    }

    public void setIsHtml(boolean isHtml) {
        m_isHtml = isHtml;
    }

    public boolean getIsHtml() {
        return m_isHtml;
    }

    private String m_fieldName;
    private int m_textColor = DEFAULT_COLOR;
    private boolean m_lineReturn = false; // whether there is a line return before this field
    private float m_relativeSize = RELATIVE_SIZE_DEFAULT;
    private Layout.Alignment m_alignment = DEFAULT_ALIGNMENT;
    private int m_leftMargin = 0;
    private boolean m_isHtml = false;

}
