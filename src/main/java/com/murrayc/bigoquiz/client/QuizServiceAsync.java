package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * The async counterpart of <code>QuizService</code>.
 */
public interface QuizServiceAsync {
    void getQuestion(String questionId, AsyncCallback<Question> async)
            throws IllegalArgumentException;

    void getNextQuestion(AsyncCallback<Question> async)
            throws IllegalArgumentException;

    void submitAnswer(final String questionId, final String answer, AsyncCallback<Boolean> async)
            throws IllegalArgumentException;

    void getUserProfile(AsyncCallback<UserProfile> async)
            throws IllegalArgumentException;

    void increaseScore(AsyncCallback<Void> async);

    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    final class Util {
        private static QuizServiceAsync instance;

        public static QuizServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(QuizService.class);
            }
            return instance;
        }

        private Util() {
            // Utility class should not be instantiated
        }
    }

}
