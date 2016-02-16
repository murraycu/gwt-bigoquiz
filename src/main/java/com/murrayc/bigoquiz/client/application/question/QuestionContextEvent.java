package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/25/16.
 */
public class QuestionContextEvent extends GwtEvent<QuestionContextEvent.QuestionContextEventHandler> {
    private final String quizId;
    private final String nextQuestionSectionId;

    public QuestionContextEvent(@NotNull final String quizId, final String nextQuestionSectionId) {
        this.quizId = quizId;
        this.nextQuestionSectionId = nextQuestionSectionId;;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getNextQuestionSectionId() {
        return nextQuestionSectionId;
    }

    public interface QuestionContextEventHandler extends EventHandler {
        void onQuestionContextChanged(QuestionContextEvent event);
    }

    public static final Type<QuestionContextEventHandler> TYPE = new Type<>();

    public static void fire(@NotNull final HasHandlers source, @NotNull final String quizId, final String nextQuestionSectionId) {
        if (TYPE != null) {
            source.fireEvent(new QuestionContextEvent(quizId, nextQuestionSectionId));
        }
    }

    /*
    public static Type<UserProfileResetSectionsEventHandler> getType() {
        return TYPE;
    }
    */

    @NotNull
    @Override
    public Type<QuestionContextEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(@NotNull final QuestionContextEventHandler handler) {
        handler.onQuestionContextChanged(this);
    }
}
