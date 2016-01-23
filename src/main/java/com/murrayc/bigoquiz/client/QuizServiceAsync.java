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

    void submitAnswer(final String questionId, final String answer, AsyncCallback<QuizService.SubmissionResult> async)
            throws IllegalArgumentException;

    void submitDontKnowAnswer(final String questionId, AsyncCallback<QuizService.SubmissionResult> async)
            throws IllegalArgumentException;

    void getUserProfile(AsyncCallback<UserProfile> async)
            throws IllegalArgumentException;

    void getUserRecentHistory(AsyncCallback<UserRecentHistory> async)
            throws IllegalArgumentException;



    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    final class Util {
        private static QuizServiceAsync instance;

        private Util() {
            // Utility class should not be instantiated
        }

        public static QuizServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(QuizService.class);
            }
            return instance;
        }
    }

}
