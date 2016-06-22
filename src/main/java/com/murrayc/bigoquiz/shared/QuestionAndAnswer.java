package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuestionAndAnswer implements IsSerializable {
    //Don't make these final, because gwt serialization doesn't support that.

    private /* final */ Question question;
    private /* final */ String answer;
    private /* final */ boolean answerIsHtml;

    public QuestionAndAnswer(final String questionId, final String sectionId, final String subSectionId,
                             final String questionText, boolean questionTextIsHtml, final String questionLink, final String answerText,
                             boolean answerTextIsHtml, final List<String> choices) {
        this.question = new Question(questionId, sectionId, subSectionId, questionText, questionTextIsHtml, questionLink, choices);
        this.answer = answerText;
        this.answerIsHtml = answerTextIsHtml;
    }

    public QuestionAndAnswer() {
    }

    public String getId() {
        return question.getId();
    }

    public Question getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean getAnswerIsHtml() {
        return answerIsHtml;
    }
}
