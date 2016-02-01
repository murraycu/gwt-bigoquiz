package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionUserAnswerAddedEvent extends GwtEvent<QuestionUserAnswerAddedEvent.QuestionUserAnswerAddedEventHandler> {
    private final Question question;
    private final boolean answerIsCorrect;

    public QuestionUserAnswerAddedEvent(final Question question, boolean answerIsCorrect) {
        this.question = question;
        this.answerIsCorrect = answerIsCorrect;
    }

    public Question getQuestion() {
        return question;
    }

    public boolean getAnswerIsCorrect() {
        return answerIsCorrect;
    }

    public interface QuestionUserAnswerAddedEventHandler extends EventHandler {
        void onQuestionUserAnswerAdded(QuestionUserAnswerAddedEvent event);
    }

    public static final Type<QuestionUserAnswerAddedEventHandler> TYPE = new Type<>();

    public static void fire(final HasHandlers source, final Question question, boolean answerIsCorrect) {
        if (TYPE != null) {
            source.fireEvent(new QuestionUserAnswerAddedEvent(question, answerIsCorrect));
        }
    }

    /*
    public static Type<QuestionUserAnswerAddedEventHandler> getType() {
        return TYPE;
    }
    */

    @Override
    public Type<QuestionUserAnswerAddedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final QuestionUserAnswerAddedEventHandler handler) {
        handler.onQuestionUserAnswerAdded(this);
    }
}
