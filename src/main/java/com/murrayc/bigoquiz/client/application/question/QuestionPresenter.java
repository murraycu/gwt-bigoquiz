package com.murrayc.bigoquiz.client.application.question;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.StringUtils;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;

import com.google.inject.Inject;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionPresenter extends Presenter<QuestionPresenter.MyView, QuestionPresenter.MyProxy>
        implements QuestionUserEditUiHandlers {
    interface MyView extends View, HasUiHandlers<QuestionUserEditUiHandlers> {
        void setSections(final QuizSections sections);

        void setQuestion(final Question question);

        void setNextQuestionSection(final String sectionId);

        String getChoiceSelected();

        void setSubmissionResult(final QuizService.SubmissionResult submissionResult);

        void showAnswer(final String correctAnswer);
    }

    private String nextQuestionSectionId;
    private String questionId;
    private String correctAnswer;
    private Question nextQuestion;

    @ProxyStandard
    @NameToken(NameTokens.QUESTION)
    interface MyProxy extends ProxyPlace<QuestionPresenter> {
    }

    @Inject
    QuestionPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN);

        getView().setUiHandlers(this);

        getAndUseSections();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        //Question ID:
        final String questionId = request.getParameter(NameTokens.QUESTION_PARAM_QUESTION_ID, null);
        if (!StringUtils.isEmpty(questionId)) {
            getAndUseQuestion(questionId);
        }

        //Next question section ID,
        nextQuestionSectionId = request.getParameter(NameTokens.QUESTION_PARAM_SECTION_ID, null);
        if (StringUtils.isEmpty(nextQuestionSectionId)) {
            getView().setNextQuestionSection(nextQuestionSectionId);

            getAndUseNextQuestion(nextQuestionSectionId);
        }
    }


    @Override
    public void onSubmitAnswer() {
        final String answer = getView().getChoiceSelected();

        final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: onSubmitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizService.SubmissionResult result) {
                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();
                nextQuestion = result.getNextQuestion();

                //Show the user:
                getView().setSubmissionResult(result);
            }

        };

        QuizServiceAsync.Util.getInstance().submitAnswer(questionId, answer, nextQuestionSectionId, callback);
    }

    @Override
    public void onShowAnswer() {
        //If we previously submitted an incorrect answer,
        //we would have received the correct answer along with the result:
        if (correctAnswer != null) {
            getView().showAnswer(correctAnswer);
            return;
        }

        //The user is giving up on the question,
        //which we treat much the same way as submitting an incorrect answer:
        final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: onSubmitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizService.SubmissionResult result) {
                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();
                nextQuestion = result.getNextQuestion();

                //Show the user the correct answer:
                getView().showAnswer(correctAnswer);
            }

        };

        QuizServiceAsync.Util.getInstance().submitDontKnowAnswer(questionId, nextQuestionSectionId, callback);
    }

    @Override
    public void onGoToNextQuestion() {
        //This was for the previously-answered question:
        correctAnswer = null;

        //If we previously submitted an answer,
        //we would have received the next question along with the result:
        if (nextQuestion != null) {
            final Question question = nextQuestion;
            questionId = nextQuestion.getId();
            nextQuestion = null;

            getView().setQuestion(question);
            return;
        }

        //Otherwise, get it from the server:
        getAndUseNextQuestion(nextQuestionSectionId);
    }

    private void getAndUseSections() {
        final AsyncCallback<QuizSections> callback = new AsyncCallback<QuizSections>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: getSections(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizSections result) {
                getView().setSections(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getSections(callback);
    }

    /** Get and show a question from the specified section.
     * or from any section if @a sectionId is null.
     *
     * @param sectionId
     */
    private void getAndUseNextQuestion(final String sectionId) {
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

                getView().setQuestion(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getNextQuestion(sectionId, callback);
    }

    private void getAndUseQuestion(final String questionId) {
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

                QuestionPresenter.this.questionId = result.getId();
                getView().setQuestion(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getQuestion(questionId, callback);
    }
}