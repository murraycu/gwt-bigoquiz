package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("quiz-service")
public interface QuizService extends RemoteService {

    @Nullable
    List<Quiz.QuizDetails> getQuizList() throws IllegalArgumentException;

    @Nullable
    Quiz getQuiz(final String quizId) throws UnknownQuizException, IllegalArgumentException;

    @Nullable Question getQuestion(final String quizId, final String questionId) throws IllegalArgumentException;

    @Nullable Question getNextQuestion(final String quizId, final String sectionId) throws IllegalArgumentException;

    @NotNull QuizSections getSections(final String quizId) throws IllegalArgumentException;

    /**
     * submitAnswer() returns the correct correctAnswer (if the supplied correctAnswer was wrong) and the next question.
     * This avoids the client needing to make multiple calls.
     *
     * @param questionId
     * @param answer
     * @param nextQuestionSectionId
     * @return
     * @throws IllegalArgumentException
     */
    @NotNull SubmissionResult submitAnswer(final String quizId, final String questionId, final String answer, final boolean exact, String nextQuestionSectionId) throws IllegalArgumentException;

    @NotNull SubmissionResult submitDontKnowAnswer(final String quizId, final String questionId, String nextQuestionSectionId) throws IllegalArgumentException;

    /**
     * Gets the currently logged-in user's statistics,
     * or an empty set of statistics for not-logged in users.
     *
     * @return
     * @throws IllegalArgumentException
     */
    @Nullable
    UserHistory getUserRecentHistory(final String quizId, final String requestUri) throws UnknownQuizException, IllegalArgumentException;

    /**
     * Clear all question answer history, progress, scores, etc.
     */
    void resetSections(final String quizId);

    class SubmissionResult implements IsSerializable {
        private boolean result = false;
        private String correctAnswer = null; //If result is false.
        private Question nextQuestion = null;

        public SubmissionResult() {
        }

        public SubmissionResult(final boolean result, final String correctAnswer, final Question nextQuestion) {
            this.result = result;
            this.correctAnswer = correctAnswer;
            this.nextQuestion = nextQuestion;
        }

        public boolean getResult() {
            return result;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public Question getNextQuestion() {
            return nextQuestion;
        }
    }
}
