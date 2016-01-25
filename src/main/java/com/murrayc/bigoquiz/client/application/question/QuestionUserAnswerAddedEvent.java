package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionUserAnswerAddedEvent extends GwtEvent<QuestionUserAnswerAddedEvent.QuestionUserAnswerAddedEventHandler> {
    private final UserAnswer userAnswer;

    public QuestionUserAnswerAddedEvent(final UserAnswer userAnswer) {
        this.userAnswer = userAnswer;
    }

    public interface QuestionUserAnswerAddedEventHandler extends EventHandler {
        void onQuestionUserAnswerAdded(QuestionUserAnswerAddedEvent event);
    }

    public static Type<QuestionUserAnswerAddedEventHandler> TYPE = new Type<>();

    public static void fire(final HasHandlers source, final UserAnswer userAnswer) {
        if (TYPE != null) {
            source.fireEvent(new QuestionUserAnswerAddedEvent(userAnswer));
        }
    }

    public static Type<QuestionUserAnswerAddedEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<QuestionUserAnswerAddedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final QuestionUserAnswerAddedEventHandler handler) {
        handler.onQuestionUserAnswerAdded(this);
    }
}
