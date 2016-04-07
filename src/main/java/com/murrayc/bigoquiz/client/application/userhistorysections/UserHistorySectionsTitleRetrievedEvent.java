package com.murrayc.bigoquiz.client.application.userhistorysections;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import org.jetbrains.annotations.NotNull;

/**
 * This lets the UserHistorySectionsPresenter tell other parts of the API
 * the human-readable title that it has discovered for a quiz ID,
 * so we don't need to retrieve it from the server twice.
 *
 * Created by murrayc on 1/25/16.
 */
public class UserHistorySectionsTitleRetrievedEvent extends GwtEvent<UserHistorySectionsTitleRetrievedEvent.EventHandler> {
    private final String quizId;
    private final String quizTitle;

    public UserHistorySectionsTitleRetrievedEvent(final String quizId, final String quizTitle) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public interface EventHandler extends com.google.gwt.event.shared.EventHandler {
        void onQuizTitleRetrieved(UserHistorySectionsTitleRetrievedEvent event);
    }

    public static final Type<EventHandler> TYPE = new Type<>();

    public static void fire(@NotNull final HasHandlers source, final String quizId, final String quizTitle) {
        if (TYPE != null) {
            source.fireEvent(new UserHistorySectionsTitleRetrievedEvent(quizId, quizTitle));
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
        handler.onQuizTitleRetrieved(this);
    }
}
