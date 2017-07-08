package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question implements IsSerializable {
    private /* final */ String id;
    private /* final */ String sectionId;
    private /* final */ String subSectionId;
    private /* final */ Question.Text text; //The actual question text.
    private /* final */ String link; //An informative URL.
    private /* final */ List<Text> choices; //Possible answers, only one of which is correct.
    private /* final */ String note;
    private /* final */ String videoUrl;
    private /* final */ String codeUrl;

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

    public Question(final String id, final String sectionId, final String subSectionId, final Text text,
                    final String link, final List<Text> choices, final String note, final String videoUrl, String codeUrl) {
        this.id = id;
        this.sectionId = sectionId;
        this.subSectionId = subSectionId;
        this.text = text;
        this.link = link;
        this.choices = choices;
        this.note = note;
        this.videoUrl = videoUrl;
        this.codeUrl = codeUrl;
    }

    public String getId() {
        return id;
    }

    /** Without this, the client code will not set this from the JSON.
     */
    public void setId(final String id) {
        this.id = id;
    }

    public String getSectionId() {
        return sectionId;
    }

    /** This is only used for the JSON input.
     */
    public void setSectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSubSectionId() {
        return subSectionId;
    }

    /** Without this, the client code will not set this from the JSON.
     */
    public void setSubSectionId(final String subSectionId) {
        this.subSectionId = subSectionId;
    }

    public Question.Text getText() {
        return text;
    }

    /** Without this, the client code will not set this from the JSON.
     */
    public void setText(final Question.Text text) {
        this.text = text;
    }

    @Nullable
    public String getLink() {
        return link;
    }

    /** Without this, the client code will not set this from the JSON.
     */
    public void setLink(final String link) {
        this.link = link;
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

    @Nullable
    public String getNote() {
        return note;
    }

    /** This is only used for the JSON input.
     */
    public void setNote(final String note) {
        this.note = note;
    }

    public boolean hasNote() {
        return !StringUtils.isEmpty(note);
    }

    @Nullable
    public String getVideoUrl() {
        return videoUrl;
    }

    /** This is only used for the JSON input.
     */
    public void setVideoUrl(final String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Nullable
    public String getCodeUrl() {
        return codeUrl;
    }

    /** This is only used for the JSON input.
     */
    public void setCodeUrl(final String codeUrl) {
        this.codeUrl = codeUrl;
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

    /** This is only used for the JSON input.
     */
    public void setQuizTitle(final String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    /** This is only used for the JSON input.
     */
    public void setSubSectionTitle(final String subSectionTitle) {
        this.subSectionTitle = subSectionTitle;
    }

    public boolean getQuizUsesMathML() {
        return quizUsesMathML;
    }
}