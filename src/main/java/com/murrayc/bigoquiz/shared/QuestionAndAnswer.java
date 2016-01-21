package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuestionAndAnswer implements IsSerializable {
    //Don't make these final, because gwt serialization doesn't support that.
    private /* final */ String id;
    private /* final */ String question;
    private /* final */ String answer;
    private /* final */ List<String> choices;

    public QuestionAndAnswer(final String id, final String question, final String answer, final List<String> choices) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.choices = choices;
    }

    public QuestionAndAnswer() {
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getChoices() {
        return choices;
    }



}
