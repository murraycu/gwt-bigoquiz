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


    public QuestionAndAnswer(final String questionId, final String sectionId, final String subSectionId, final String questionText, final String answerText, final List<String> choices) {
        this.question = new Question(questionId, sectionId, subSectionId, questionText, choices);
        this.answer = answerText;
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
}
