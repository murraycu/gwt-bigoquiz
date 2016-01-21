package com.murrayc.bigoquiz.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.murrayc.bigoquiz.client.ClientFactory;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.place.QuestionPlace;
import com.murrayc.bigoquiz.client.ui.QuestionView;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/19/16.s
 */
public class QuestionActivity extends AbstractActivity implements QuestionView.Presenter {
    private final ClientFactory clientFactory;
    private final Place place;
    private String questionId;
    private QuestionView questionView;
    private String correctAnswer;
    private Question nextQuestion;

    public QuestionActivity(final QuestionPlace place, final ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        questionView = clientFactory.getQuestionView();
        questionView.setPresenter(this);
        panel.setWidget(questionView.asWidget());

        getAndUseNextQuestion();
    }

    private void getAndUseNextQuestion() {
        correctAnswer = null;
        nextQuestion = null;

        final AsyncCallback<Question> callback = new AsyncCallback<Question>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: getNextQuestion(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final Question result) {

                questionId = result.getId();
                questionView.setQuestion(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getNextQuestion(callback);
    }

    @Override
    public void goTo(final Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public void submitAnswer() {
        final String answer = questionView.getChoiceSelected();

        final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: submitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizService.SubmissionResult result) {
                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();
                nextQuestion = result.getNextQuestion();

                //Show the user:
                questionView.setSubmissionResult(result);
            }

        };

        QuizServiceAsync.Util.getInstance().submitAnswer(questionId, answer, callback);
    }

    @Override
    public void showAnswer() {
        //If we previously submitted an incorrect answer,
        //we would have received the correct answer along with the result:
        if (correctAnswer != null) {
            questionView.showAnswer(correctAnswer);
            return;
        }

        //The user is giving up on the question,
        //which we treat much the same way as submitting an incorrect answer:
        final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: submitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizService.SubmissionResult result) {
                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();
                nextQuestion = result.getNextQuestion();

                //Show the user the correct answer:
                questionView.showAnswer(correctAnswer);
            }

        };

        QuizServiceAsync.Util.getInstance().submitDontKnowAnswer(questionId, callback);
    }

    @Override
    public void goToNextQuestion() {
        //This was for the previously-answered question:
        correctAnswer = null;

        //If we previously submitted an answer,
        //we would have received the next question along with the result:
        if (nextQuestion != null) {
            final Question question = nextQuestion;
            nextQuestion = null;
            questionView.setQuestion(question);
            return;
        }

        //Otherwise, get it from the server:
        getAndUseNextQuestion();
    }
}
