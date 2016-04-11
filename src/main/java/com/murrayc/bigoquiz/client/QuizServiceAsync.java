package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import java.util.List;

/**
 * The async counterpart of <code>QuizService</code>.
 */
public interface QuizServiceAsync {
    void getQuizList(AsyncCallback<List<Quiz.QuizDetails>> async);

    void getQuiz(final String quizId, AsyncCallback<Quiz> async) throws UnknownQuizException, IllegalArgumentException;

    void getQuestion(final String quizId, String questionId, AsyncCallback<Question> async)
            throws IllegalArgumentException;

    void getNextQuestion(final String quizId, final String sectionId, AsyncCallback<Question> async)
            throws IllegalArgumentException;

    void submitAnswer(final String quizId, final String questionId, final String answer, final boolean exact, final String nextQuestionSectionId, AsyncCallback<QuizService.SubmissionResult> async)
            throws IllegalArgumentException;

    void submitDontKnowAnswer(final String quizId, final String questionId, String nextQuestionSectionId, AsyncCallback<QuizService.SubmissionResult> async)
            throws IllegalArgumentException;

    void getUserHistorySections(final String quizId, final String requestUri, AsyncCallback<UserHistorySections> async)
            throws UnknownQuizException, IllegalArgumentException;

    void getSections(final String quizId, AsyncCallback<QuizSections> async);

    /**
     * Clear all question answer history, progress, scores, etc.
     */
    void resetSections(final String quizId, AsyncCallback<Void> async);

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
