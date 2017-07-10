package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.client.application.quiz.QuizClient;
import com.murrayc.bigoquiz.shared.Question;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@murrayc.com.com>
 */
public class GwtTestQuizService extends GWTTestCase {

    @Test
    public void test() {
        // Setup an asynchronous event handler.
        @NotNull final MethodCallback<Question> callback = new MethodCallback<Question>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                fail(caught.toString());
            }

            @Override
            public void onSuccess(final Method method, final Question question) {
                finishTest();
            }
        };

        delayTestFinish(500);

        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        QuizClient client = GWT.create(QuizClient.class);
        assertNotNull(client);
        client.getQuizQuestion("quiz1", "question1", callback);
    }

    @NotNull
    @Override
    public String getModuleName() {
        return "com.murrayc.BigOQuiz";
    }

}
