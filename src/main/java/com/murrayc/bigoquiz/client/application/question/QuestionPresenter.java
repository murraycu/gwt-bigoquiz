package com.murrayc.bigoquiz.client.application.question;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.application.quiz.BigOQuizPresenter;
import com.murrayc.bigoquiz.client.application.quiz.QuizClient;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;

import com.google.inject.Inject;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionPresenter extends BigOQuizPresenter<QuestionPresenter.MyView, QuestionPresenter.MyProxy>
        implements QuestionUserEditUiHandlers {
    //Put this in a shared PresenterWithUserHistoryRecent class, also used by QuizPresenter?
    private final UserHistorySectionsPresenter userHistorySectionsPresenter;
    public static final SingleSlot<UserHistorySectionsPresenter> SLOT_USER_HISTORY_RECENT = new SingleSlot<>();

    private final PlaceManager placeManager;
    private String quizId = null;
    private QuizSections sections = null;
    private boolean waitingForSections = false;
    private boolean showQuestionPending = false;

    interface MyView extends ContentView, HasUiHandlers<QuestionUserEditUiHandlers> {
        void setSections(final QuizSections sections);

        void setQuestion(String quizId, final Question question);

        void setNextQuestionSectionId(final String sectionId);

        Question.Text getChoiceSelected();

        void setSubmissionResult(final QuizService.SubmissionResult submissionResult);

        void showAnswer(final Question.Text correctAnswer);

        //TODO: The presenter should know if it is waiting,
        //because it tells the view what to do.
        boolean isWaiting();

        void setServerFailed();
    }

    private String nextQuestionSectionId = null;
    private Question question = null;
    @Nullable
    private Question.Text correctAnswer = null;
    @Nullable
    private Question nextQuestion = null;

    final Timer autoNextTimer = new Timer() {
        @Override
        public void run() {
            onGoToNextQuestion();
        }
    };

    @ProxyStandard
    @NameToken(NameTokens.QUESTION)
    interface MyProxy extends ProxyPlace<QuestionPresenter> {
    }

    @Inject
    QuestionPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager,
            UserHistorySectionsPresenter userHistorySectionsPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.placeManager = placeManager;
        this.userHistorySectionsPresenter = userHistorySectionsPresenter;

        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        super.onBind();

        setInSlot(SLOT_USER_HISTORY_RECENT, userHistorySectionsPresenter);
    }

    private
    String getQuestionId() {
        if (question == null) {
            return null;
        }

        return question.getId();
    }



    @Override
    public void prepareFromRequest(@NotNull final PlaceRequest request) {
        super.prepareFromRequest(request);

        //Quiz ID:
        this.quizId = request.getParameter(NameTokens.PARAM_QUIZ_ID, null);
        if (StringUtils.isEmpty(quizId)) {
            //Default to bigoquiz.
            this.quizId = "bigoquiz";
            //TODO: Take the user to a list of quizzes.
        }

        getAndUseSections();

        //Next question section ID,
        nextQuestionSectionId = request.getParameter(NameTokens.QUESTION_PARAM_NEXT_QUESTION_SECTION_ID, null);

        getView().setNextQuestionSectionId(nextQuestionSectionId);

        //Question ID:
        final String questionId = request.getParameter(NameTokens.QUESTION_PARAM_QUESTION_ID, null);
        //Log.error("prepareFromRequest(): questionId=" + questionId);
        if (!StringUtils.isEmpty(questionId)) {

            /* Don't check for this,
             * because this prepareFromRequest() is presumably an actual new request/refresh.
            if (StringUtils.equals(questionId, getQuestionId())) {
                //We are already showing the correct question.
                //Log.error("prepareFromRequest(): already showing.");
                return;
            }
            */

            //If we have already cached this one, just show it:
            if (nextQuestion != null && StringUtils.equals(nextQuestion.getId(), questionId)) {
                //Log.error("prepareFromRequest(): using nextQuestion.");
                question = nextQuestion;
                nextQuestion = null;
                showQuestionInView();
                return;
            }

            //Log.error("prepareFromRequest(): getting from server.");

            //Otherwise, get it from the server and show it:
            getAndUseQuestion(questionId);
        } else {
            getAndUseNextQuestion(nextQuestionSectionId);
        }

    }

    @Override
    public void onReset() {
        //Make sure that the sidebar's links are updated too:
        tellUserHistoryPresenterAboutQuestionContext();
    }

    @Override
    public void onSubmitAnswer() {
        //Submit the answer to the server:
        final Question.Text answer = getView().getChoiceSelected();
        if (answer == null) {
            Log.error("onSubmitAnswer(): answer was null.");
            return;
        }

        @NotNull final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);

                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: onSubmitAnswer()", ex);
                    getView().setServerFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: onSubmitAnswer()", ex);
                    getView().setServerFailed();
                }
            }

            @Override
            public void onSuccess(@Nullable final QuizService.SubmissionResult result) {
                getView().setLoadingLabelVisible(false);

                if (result == null) {
                    Log.error("AsyncCallback: onSubmitAnswer: onSuccess: result was null.");
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
                    //Log.error("Storing nextQuestion for later: " + nextQuestion.getId());
                }

                tellUserHistoryPresenterAboutNewUserAnswer(result.getResult());

                //Show the user:
                getView().setSubmissionResult(result);

                // Show the next question automatically,
                // unless there is a note that the user should see.
                if (result.getResult() && !question.hasNote()) {
                    //Automatically show the next question after a delay.
                    autoNextTimer.schedule(5000);
                }
            }

        };

        getView().setLoadingLabelVisible(true);
        QuizServiceAsync.Util.getInstance().submitAnswer(getQuizId(), getQuestionId(), answer.text,
                nextQuestionSectionId, callback);
    }

    private void tellUserHistoryPresenterAboutNewUserAnswer(boolean answerIsCorrect) {
        if (sections == null) {
            //We need this to continue, to get the titles.
            Log.error("tellUserHistoryPresenterAboutNewUserAnswer(): userhistorysections is null.");
            return;
        }

        //TODO: Just use the question as it is?
        //Tell the UserHistoryRecent presenter/view that there is a new history item.
        //Otherwise it will only update when the whole page refreshes.
        @Nullable final String subSectionTitle =
                sections.getSubSectionTitle(question.getSectionId(), question.getSubSectionId());
        question.setTitles(question.getQuizTitle(), subSectionTitle, question);

        QuestionUserAnswerAddedEvent.fire(this, question, answerIsCorrect);
    }

    private void tellUserHistoryPresenterAboutQuestionContext() {
        QuestionContextEvent.fire(this, getQuizId(), nextQuestionSectionId);
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
        @NotNull final AsyncCallback<QuizService.SubmissionResult> callback = new AsyncCallback<QuizService.SubmissionResult>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: submitDontKnowAnswer()", ex);
                } catch (final Throwable ex) {
                    // TODO: create a way to notify users of asynchronous callback failures
                    Log.error("AsyncCallback Failed: submitDontKnowAnswer()", ex);
                }
            }

            @Override
            public void onSuccess(@NotNull final QuizService.SubmissionResult result) {
                tellUserHistoryPresenterAboutNewUserAnswer(false);

                //Store these in case they are needed soon:
                correctAnswer = result.getCorrectAnswer();
                nextQuestion = result.getNextQuestion();

                //Show the user the correct answer:
                getView().showAnswer(correctAnswer);
            }

        };

        QuizServiceAsync.Util.getInstance().submitDontKnowAnswer(getQuizId(), getQuestionId(), nextQuestionSectionId, callback);
    }

    @Override
    public void onGoToNextQuestion() {
        //Stop this from happening automatically,
        //because then the question would change yet again:
        autoNextTimer.cancel();

        //Log.error("onGoToNextQuestion: current=" + questionId);
        //This was for the previously-answered question:
        correctAnswer = null;

        //If we previously submitted an answer,
        //we would have received the next question along with the result,
        //and it might still be what we want:
        if (nextQuestion != null) {
            //Log.error("onGoToNextQuestion: nextQuestion != null");

            if ((nextQuestionSectionId == null) ||
                StringUtils.equals(nextQuestion.getSectionId(), nextQuestionSectionId)) {
                //Log.error("onGoToNextQuestion: revealQuestion(): id=" + nextQuestion.getId());
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
    private void revealQuestion(@NotNull final Question question) {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(quizId, question.getId(),
                nextQuestionSectionId);
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

        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(getQuizId(), nextQuestionId, nextQuestionSectionId);
        placeManager.revealPlace(placeRequest);
    }

    private String getQuizId() {
        return quizId;
    }

    private void showQuestionInView() {
        //We cannot show the question without the userhistorysections.
        if (waitingForSections) {
            showQuestionPending = true;
            return;
        }

        getView().setQuestion(getQuizId(), question);
    }

    @Override
    public void onNextQuestionSectionSelected(@Nullable final String nextQuestionSectionId) {
        //Stop the next question from being shown automatically
        //because then the question would change yet again:
        autoNextTimer.cancel();

        // Don't get a new question if we are already waiting for an answer
        // and the current question is already from a correct section.
        @Nullable String nextQuestionId = null;
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
        waitingForSections = true;

        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        QuizClient client = GWT.create(QuizClient.class);

        @NotNull final MethodCallback<QuizSections> callback = new MethodCallback<QuizSections>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                Utils.tellUserHistoryPresenterAboutNoQuestionContext(QuestionPresenter.this); //clear the sections sidebar.
                getView().setQuestion(null, null);

                try {
                    throw caught;
                } catch (final UnknownQuizException ex) {
                    Log.error("AsyncCallback Failed with UnknownQuizException: getSections()", ex);
                    getView().setServerFailedUnknownQuiz();
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getSections()", ex);
                    getView().setServerFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getSections()", ex);
                    getView().setServerFailed();
                }

                waitingForSections = false;
            }

            @Override
            public void onSuccess(final Method method, final QuizSections result) {
                QuestionPresenter.this.sections = result;
                getView().setSections(result);

                waitingForSections = false;
                if(showQuestionPending) {
                    showQuestionInView();
                }
            }
        };


        client.getQuizSectionsById(getQuizId(), true /* list-only */, callback);
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

        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        QuestionClient client = GWT.create(QuestionClient.class);

        @NotNull final MethodCallback<Question> callback = new MethodCallback<Question>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getNextQuestion()", ex);
                    getView().setServerFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getNextQuestion()", ex);
                    getView().setServerFailed();
                }
            }

            @Override
            public void onSuccess(final Method method, @NotNull final Question result) {
                getView().setLoadingLabelVisible(false);
                revealQuestion(result);
            }

        };

        getView().setLoadingLabelVisible(true);
        client.getNextQuestion(getQuizId(), sectionId, callback);
    }

    private void getAndUseQuestion(final String questionId) {
        //Log.error("getAndUseQuestion");
        correctAnswer = null;
        nextQuestion = null;

        @NotNull final AsyncCallback<Question> callback = new AsyncCallback<Question>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getQuestion()", ex);
                    //Maybe the user tried to view a non-existent question:
                    abandonQuestionAndShowAnother();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getQuestion()", ex);
                    getView().setServerFailed();
                }
            }

            @Override
            public void onSuccess(final Question result) {
                getView().setLoadingLabelVisible(false);
                if (result != null) {
                    question = result;
                    showQuestionInView();
                } else {
                    Log.error("AsyncCallback Failed: getQuestion(): result was null.");

                    abandonQuestionAndShowAnother();
                }
            }

        };

        getView().setLoadingLabelVisible(true);
        QuizServiceAsync.Util.getInstance().getQuestion(getQuizId(), questionId, callback);
    }

    private void abandonQuestionAndShowAnother() {
        question = null;
        getAndUseNextQuestion(nextQuestionSectionId);
    }
}