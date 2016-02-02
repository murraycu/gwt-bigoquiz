package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionNextQuestionSetionIdEvent extends GwtEvent<QuestionNextQuestionSetionIdEvent.QuestionUserAnswerAddedEventHandler> {
    private final String nextQuestionSectionId;

    public QuestionNextQuestionSetionIdEvent(final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
    }

    public String getNextQuestionSectionId() {
        return nextQuestionSectionId;
    }

    public interface QuestionUserAnswerAddedEventHandler extends EventHandler {
        void onQuestionNextSectionId(QuestionNextQuestionSetionIdEvent event);
    }

    public static final Type<QuestionUserAnswerAddedEventHandler> TYPE = new Type<>();

    public static void fire(final HasHandlers source, final String nextQuestionSectionId) {
        if (TYPE != null) {
            source.fireEvent(new QuestionNextQuestionSetionIdEvent(nextQuestionSectionId));
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
        handler.onQuestionNextSectionId(this);
    }
}
