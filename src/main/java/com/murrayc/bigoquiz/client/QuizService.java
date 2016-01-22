package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface QuizService extends RemoteService {
    Question getQuestion(final String questionId) throws IllegalArgumentException;

    Question getNextQuestion() throws IllegalArgumentException;

    /**
     * submitAnswer() returns the correct correctAnswer (if the supplied correctAnswer was wrong) and the next question.
     * This avoids the client needing to make multiple calls.
     *
     * @param questionId
     * @param answer
     * @return
     * @throws IllegalArgumentException
     */
    SubmissionResult submitAnswer(final String questionId, final String answer) throws IllegalArgumentException;

    SubmissionResult submitDontKnowAnswer(final String questionId) throws IllegalArgumentException;

    UserProfile getUserProfile() throws IllegalArgumentException;

    public static class SubmissionResult implements IsSerializable {
        private boolean result;
        private String correctAnswer; //If result is false.
        private Question nextQuestion;

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
