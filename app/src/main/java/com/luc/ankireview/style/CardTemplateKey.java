package com.luc.ankireview.style;


import java.io.Serializable;
import java.util.Objects;

public class CardTemplateKey implements Serializable {
    public CardTemplateKey(long modelId, int cardOrd) {
        m_modelId = modelId;
        m_cardOrd = cardOrd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardTemplateKey that = (CardTemplateKey) o;
        return m_modelId == that.m_modelId &&
                m_cardOrd == that.m_cardOrd;
    }

    @Override
    public int hashCode() {

        return Objects.hash(m_modelId, m_cardOrd);
    }

    public String toString() {
        return " modelId: " + Long.toString(m_modelId);
    }

    private long m_modelId;
    private int m_cardOrd;
}
