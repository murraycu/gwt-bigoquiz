package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionNextQuestionSectionIdEvent extends GwtEvent<QuestionNextQuestionSectionIdEvent.QuestionUserAnswerAddedEventHandler> {
    private final String nextQuestionSectionId;

    public QuestionNextQuestionSectionIdEvent(final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
    }

    public String getNextQuestionSectionId() {
        return nextQuestionSectionId;
    }

    public interface QuestionUserAnswerAddedEventHandler extends EventHandler {
        void onQuestionNextSectionId(QuestionNextQuestionSectionIdEvent event);
    }

    public static final Type<QuestionUserAnswerAddedEventHandler> TYPE = new Type<>();

    public static void fire(final HasHandlers source, final String nextQuestionSectionId) {
        if (TYPE != null) {
            source.fireEvent(new QuestionNextQuestionSectionIdEvent(nextQuestionSectionId));
        }
    }

    /*
    public static Type<UserProfileResetSectionsEventHandler> getType() {
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
