package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.murrayc.bigoquiz.shared.Question;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionUserAnswerAddedEvent extends GwtEvent<QuestionUserAnswerAddedEvent.EventHandler> {
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

    public interface EventHandler extends com.google.gwt.event.shared.EventHandler {
        void onQuestionUserAnswerAdded(QuestionUserAnswerAddedEvent event);
    }

    public static final Type<EventHandler> TYPE = new Type<>();

    public static void fire(@NotNull final HasHandlers source, final Question question, boolean answerIsCorrect) {
        if (TYPE != null) {
            source.fireEvent(new QuestionUserAnswerAddedEvent(question, answerIsCorrect));
        }
    }

    /*
    public static Type<EventHandler> getType() {
        return TYPE;
    }
    */

    @NotNull
    @Override
    public Type<EventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(@NotNull final EventHandler handler) {
        handler.onQuestionUserAnswerAdded(this);
    }
}
