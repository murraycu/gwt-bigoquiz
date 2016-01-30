package com.murrayc.bigoquiz.client.application.question;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;

import com.google.inject.Inject;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionPresenter extends Presenter<QuestionPresenter.MyView, QuestionPresenter.MyProxy>
        implements QuestionUserEditUiHandlers {
    private final PlaceManager placeManager;
    private QuizSections sections;

    interface MyView extends View, HasUiHandlers<QuestionUserEditUiHandlers> {
        void setSections(final QuizSections sections);

        void setQuestion(final Question question);

        void setNextQuestionSectionId(final String sectionId);

        String getChoiceSelected();

        void setSubmissionResult(final QuizService.SubmissionResult submissionResult);

        void showAnswer(final String correctAnswer);

        //TODO: The presenter should know if it is waiting,
        //because it tells the view what to do.
        boolean isWaiting();
    }

    private String nextQuestionSectionId;
    private Question question;
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
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.placeManager = placeManager;

        getView().setUiHandlers(this);

        getAndUseSections();
    }

    private
    String getQuestionId() {
        if (question == null) {
            return null;
        }

        return question.getId();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        //Next question section ID,
        nextQuestionSectionId = request.getParameter(NameTokens.QUESTION_PARAM_NEXT_QUESTION_SECTION_ID, null);
        getView().setNextQuestionSectionId(nextQuestionSectionId);

        //Question ID:
        final String questionId = request.getParameter(NameTokens.QUESTION_PARAM_QUESTION_ID, null);
        //GWT.log("prepareFromRequest(): questionId=" + questionId);
        if (!StringUtils.isEmpty(questionId)) {
            if (StringUtils.equals(questionId, getQuestionId())) {
                //We are already showing the correct question.
                //GWT.log("prepareFromRequest(): already showing.");
                return;
            }

            //If we have already cached this one, just show it:
            if (nextQuestion != null && StringUtils.equals(nextQuestion.getId(), questionId)) {
                //GWT.log("prepareFromRequest(): using nextQuestion.");
                final Question question = nextQuestion;
                nextQuestion = null;
                showQuestionInView(question);
                return;
            }

            //GWT.log("prepareFromRequest(): getting from server.");

            //Otherwise, get it from the server and show it:
            getAndUseQuestion(questionId);
        } else {
            getAndUseNextQuestion(nextQuestionSectionId);
        }

    }

    @Override
    public void onSubmitAnswer() {
        //Submit the answer to the server:
        final String answer = getView().getChoiceSelected();
        if (answer == null) {
            GWT.log("onSubmitAnswer(): answer was null.");
            return;
        }

        final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: onSubmitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuizService.SubmissionResult result) {
                if (result == null) {
                    GWT.log("AsyncCallback: onSubmitAnswer: onSuccess: result was null.");
                    return;
                }

                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();

                //Store the possible next question, to avoid having to ask as a separate async request,
                //but ignore duplicates:
                nextQuestion = null;
                final Question possibleNextQuestion = result.getNextQuestion();
                if ((possibleNextQuestion != null) &&
                        (!StringUtils.equals(possibleNextQuestion.getId(), getQuestionId()))) {
                    nextQuestion = possibleNextQuestion;
                    //GWT.log("Storing nextQuestion for later: " + nextQuestion.getId());
                }

                tellUserHistoryPresenterAboutNewUserAnswer(result.getResult());

                //Show the user:
                getView().setSubmissionResult(result);
            }

        };

        QuizServiceAsync.Util.getInstance().submitAnswer(getQuestionId(), answer, nextQuestionSectionId, callback);
    }

    private void tellUserHistoryPresenterAboutNewUserAnswer(boolean answerIsCorrect) {
        if (sections == null) {
            //We need this to continue, to get the titles.
            GWT.log("tellUserHistoryPresenterAboutNewUserAnswer(): sections is null.");
            return;
        }

        //Tell the UserHistoryRecent presenter/view that there is a new history item.
        //Otherwise it will only update when the whole page refreshes.
        final UserAnswer userAnswer = new UserAnswer(null, question, answerIsCorrect, null);
        final String subSectionTitle =
                sections.getSubSectionTitle(question.getSectionId(), question.getSubSectionId());
        userAnswer.setTitles(subSectionTitle, question);

        QuestionUserAnswerAddedEvent.fire(this, userAnswer);
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

        QuizServiceAsync.Util.getInstance().submitDontKnowAnswer(getQuestionId(), nextQuestionSectionId, callback);
    }

    @Override
    public void onGoToNextQuestion() {
        //GWT.log("onGoToNextQuestion: current=" + questionId);
        //This was for the previously-answered question:
        correctAnswer = null;

        //If we previously submitted an answer,
        //we would have received the next question along with the result,
        //and it might still be what we want:
        if (nextQuestion != null) {
            //GWT.log("onGoToNextQuestion: nextQuestion != null");

            if ((nextQuestionSectionId == null) ||
                StringUtils.equals(nextQuestion.getSectionId(), nextQuestionSectionId)) {
                //GWT.log("onGoToNextQuestion: revealQuestion(): id=" + nextQuestion.getId());
                revealQuestion(nextQuestion);
                return;
            }
        }

        //Otherwise, get it from the server:
        getAndUseNextQuestion(nextQuestionSectionId);
    }

    /**
     * This will cause prepareFromRequest() be called,
     * where we can update the view with the specified questionId,
     * storing a history token for the "place" along the way,
     * so the browser's back button can take us to the previous question.
     *
     * @param question
     */
    private void revealQuestion(final Question question) {
        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(question.getId(), nextQuestionSectionId);
        placeManager.revealPlace(placeRequest);
    }

    /**
     * This will cause prepareFromRequest() be called,
     * where we can update the view with a question from the specified section,
     * or the specified question from that section, getting subsequent questions from the same section.
     * storing a history token for the "place" along the way,
     * so the browser's back button can take us to the previous question.
     *
     * @param nextQuestionSectionId
     * @param nextQuestionId May be null.
     */
    private void revealSection(final String nextQuestionSectionId, final String nextQuestionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;

        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(nextQuestionId, nextQuestionSectionId);
        placeManager.revealPlace(placeRequest);
    }

    private void showQuestionInView(final Question question) {
        this.question = question;
        getView().setQuestion(question);
    }

    @Override
    public void onNextQuestionSectionSelected(final String nextQuestionSectionId) {

        // Don't get a new question if we are already waiting for an answer
        // and the current question is already from a correct section.
        String nextQuestionId = null;
        if (question != null &&
                getView().isWaiting()) {
            //null means "any section"
            if(nextQuestionSectionId == null ||
                    StringUtils.equals(nextQuestionSectionId, question.getSectionId())) {
                nextQuestionId = getQuestionId();
            }
        }

        revealSection(nextQuestionSectionId, nextQuestionId);
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
                QuestionPresenter.this.sections = result;
                getView().setSections(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getSections(callback);
    }

    /** Get and show a question from the specified section.
     * or from any section if @a sectionId is null.
     * This ignores any cached nextQuestion,
     * so don't call this method if you want to possibly use
     * the cached nextQuestion
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
                revealQuestion(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getNextQuestion(sectionId, callback);
    }

    private void getAndUseQuestion(final String questionId) {
        //GWT.log("getAndUseQuestion");
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
                showQuestionInView(result);
            }

        };

        QuizServiceAsync.Util.getInstance().getQuestion(questionId, callback);
    }
}