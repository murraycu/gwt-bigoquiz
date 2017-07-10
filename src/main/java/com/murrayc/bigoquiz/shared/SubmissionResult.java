package com.murrayc.bigoquiz.shared;

/**
 * Created by murrayc on 7/10/17.
 */
public class SubmissionResult {
    private boolean result = false;
    private Question.Text correctAnswer = null; //If result is false.
    private Question nextQuestion = null;

    public SubmissionResult() {
    }

    public SubmissionResult(final boolean result, final Question.Text correctAnswer, final Question nextQuestion) {
        this.result = result;
        this.correctAnswer = correctAnswer;
        this.nextQuestion = nextQuestion;
    }

    public boolean getResult() {
        return result;
    }

    /** This is just for the JSON input.
     */
    void setResult(boolean result) {
        this.result = result;
    }

    public Question.Text getCorrectAnswer() {
        return correctAnswer;
    }

    /** This is just for the JSON input.
     */
    void setCorrectAnswer(final Question.Text correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Question getNextQuestion() {
        return nextQuestion;
    }

    /** This is just for the JSON input.
     */
    void setNextQuestion(final Question nextQuestion) {
        this.nextQuestion = nextQuestion;
    }
}
