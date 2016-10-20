package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class Question implements IsSerializable {
    private /* final */ String id;
    private /* final */ String sectionId;
    private /* final */ String subSectionId;
    private /* final */ Question.Text text; //The actual question text.
    private /* final */ String link; //An informative URL.
    private /* final */ List<Text> choices; //Possible answers, only one of which is correct.
    private /* final */ String note;

    private /* final */ String quizTitle;
    private /* final */ String subSectionTitle;

    private /* final */ boolean quizUsesMathML;

    public static class Text implements IsSerializable {
        Text() {
            this.text = null;
            this.isHtml = false;
        }

        public Text(final String text, boolean isHtml) {
            this.text = text;
            this.isHtml = isHtml;
        }

        public /* final */ String text;
        public /* final */ boolean isHtml;

        // We override equals() and hashode() so we can use this class in a Set.
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Text text1 = (Text) o;

            if (isHtml != text1.isHtml) return false;
            return text != null ? text.equals(text1.text) : text1.text == null;

        }

        @Override
        public int hashCode() {
            int result = text != null ? text.hashCode() : 0;
            result = 31 * result + (isHtml ? 1 : 0);
            return result;
        }
    }

    public Question() {
    }

    public Question(final String id, final String sectionId, final String subSectionId, final Question.Text text,
                    final String link, final List<Question.Text> choices, final String note) {
        this.id = id;
        this.sectionId = sectionId;
        this.subSectionId = subSectionId;
        this.text = text;
        this.link = link;
        this.choices = choices;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getSubSectionId() {
        return subSectionId;
    }

    public Question.Text getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public List<Text> getChoices() {
        return choices;
    }

    public boolean hasChoices() {
        return (choices != null) && !choices.isEmpty();
    }

    public void setChoices(final List<Text> choices) {
        this.choices = choices;
    }

    public String getNote() {
        return note;
    }


    /** Set titles, just to save users of Question the bother of having to get them from the Quiz class.
     *
     * @param quizTitle The title of the quiz that this Question is from.
     * @param subSectionTitle The title of the quiz's sub-section that this Question is from.
     * @param question
     */
    public void setTitles(final String quizTitle, final String subSectionTitle, @NotNull final Question question) {
        this.quizTitle = quizTitle;
        this.subSectionTitle = subSectionTitle;
        this.text = question.getText(); //TODO: This is not useful.
    }

    /** This is just to save users of Question the bother of having to get it from the Quiz class.
     */
    public void setQuizUsesMathML(boolean quizUsesMathML) {
        this.quizUsesMathML = quizUsesMathML;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    public boolean getQuizUsesMathML() {
        return quizUsesMathML;
    }
}