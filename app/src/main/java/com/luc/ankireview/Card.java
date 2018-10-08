package com.luc.ankireview;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    public static Pattern s_soundPattern = Pattern.compile("\\[sound\\:([^\\[\\]]*)\\]");
    private static final String TAG = "Card";


    public Card(long noteId, int cardOrd, long modelId, String cardTemplateName, HashMap<String,String> fieldMap, int buttonCount, Vector<String> nextReviewTimes) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_modelId = modelId;
        m_cardTemplateName = cardTemplateName;
        m_fieldMap = fieldMap;
        m_buttonCount = buttonCount;

        m_nextReviewTimes = nextReviewTimes;

    }

    public String extractSoundFile( String content ) {
        String result = "";

        Matcher matcher = s_soundPattern.matcher(content);
        // While there is matches of the pattern for sound markers
        while (matcher.find()) {
            String sound = matcher.group(1);
            Log.v(TAG, "sound: " + sound);
            result = sound;
        }

        return result;
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

    public int getButtonCount()
    {
        return m_buttonCount;
    }

    public long getNoteId() {
        return m_noteId;
    }

    public long getModelId() { return m_modelId; }

    public int getCardOrd() {
        return m_cardOrd;
    }

    public String getCardTemplateName() { return m_cardTemplateName; }

    public String getFieldValue(String fieldName) { return m_fieldMap.get(fieldName); }

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

    public Vector<AnkiUtils.AnswerChoice> getAnswerChoices(Resources resources) {
        Vector<AnkiUtils.AnswerChoice> choices = new Vector<AnkiUtils.AnswerChoice>();

        // build possible choices based on number of buttons
        switch (m_buttonCount) {
            case 2:
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_1,
                                                       resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")",
                                                        R.drawable.close,
                                                        R.color.answer_wrong));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_2,
                                                      resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(1) + ")",
                                                       R.drawable.check,
                                                       R.color.answer_good));
                break;
            case 3:
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_1,
                                                      resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")",
                                                       R.drawable.close,
                                                       R.color.answer_wrong));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_2,
                                                     resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(1) + ")",
                                                       R.drawable.check,
                                                       R.color.answer_good));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_3,
                                                       resources.getString(R.string.ease_button_easy) + " (" + m_nextReviewTimes.get(2) + ")",
                                                        R.drawable.check,
                                                        R.color.answer_easy));
                break;
            default:
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_1,
                                                       resources.getString(R.string.ease_button_again) + " (" + m_nextReviewTimes.get(0) + ")",
                                                        R.drawable.close,
                                                        R.color.answer_wrong));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_2,
                                                       resources.getString(R.string.ease_button_hard) + " (" + m_nextReviewTimes.get(1) + ")",
                                                        R.drawable.check,
                                                        R.color.answer_hard));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_3,
                                                       resources.getString(R.string.ease_button_good) + " (" + m_nextReviewTimes.get(2) + ")",
                                                        R.drawable.check,
                                                        R.color.answer_good));
                choices.add(new AnkiUtils.AnswerChoice(R.id.reviewer_action_ease_4,
                                                        resources.getString(R.string.ease_button_easy) + " (" + m_nextReviewTimes.get(3) + ")",
                                                         R.drawable.check,
                                                         R.color.answer_easy));
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
    private long m_modelId;
    private String m_cardTemplateName;

    HashMap<String,String> m_fieldMap;

    private String m_answerSound;
    private int m_buttonCount;
    private Vector<String> m_nextReviewTimes;
}
