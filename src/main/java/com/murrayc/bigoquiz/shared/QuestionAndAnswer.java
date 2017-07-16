package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuestionAndAnswer {
    private Question question;
    private Question.Text answer;

    public QuestionAndAnswer(final String questionId, final String sectionId, final String subSectionId,
                             final Question.Text questionText, final String questionLink, final Question.Text answerText,
                             final List<Question.Text> choices, final String note, final String videoUrl, final String codeUrl) {
        this.question = new Question(questionId, sectionId, subSectionId, questionText, questionLink, choices, note, videoUrl, codeUrl);
        this.answer = answerText;
    }

    public QuestionAndAnswer() {
    }

    @JsonIgnore
    public String getId() {
        return question.getId();
    }

    public Question getQuestion() {
        return question;
    }

    /** Without this, the client code will not fill the QuestionAndAnswer with the question from the JSON.
     */
    public void setQuestion(final Question question) {
        this.question = question;
    }

    public Question.Text getAnswer() {
        return answer;
    }

    /** Without this, the client code will not fill the QuestionAndAnswer with the answer from the JSON.
     */
    public void setAnswer(final Question.Text answer) {
        this.answer = answer;
    }
}
