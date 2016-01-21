package com.murrayc.bigoquiz.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.murrayc.bigoquiz.client.ClientFactory;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.place.QuestionPlace;
import com.murrayc.bigoquiz.client.ui.QuestionView;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;

/**
 * Created by murrayc on 1/19/16.s
 */
public class QuestionActivity extends AbstractActivity implements QuestionView.Presenter {
    private final ClientFactory clientFactory;
    private final Place place;
    private String questionId;
    private QuestionView questionView;

    public QuestionActivity(final QuestionPlace place, final ClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        questionView = clientFactory.getQuestionView();
        questionView.setPresenter(this);
        panel.setWidget(questionView.asWidget());

        final AsyncCallback<QuestionAndAnswer> callback = new AsyncCallback<QuestionAndAnswer>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: OnlineGlomService.getDetailsLayoutAndData(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final QuestionAndAnswer result) {

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

        final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: submitAnswer(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final Boolean result) {
                questionView.setSubmissionResult(result);
            }

        };

        QuizServiceAsync.Util.getInstance().submitAnswer(questionId, answer, callback);
    }
}
