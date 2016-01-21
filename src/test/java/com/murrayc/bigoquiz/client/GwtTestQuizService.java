package com.murrayc.bigoquiz.client;

import com.murrayc.bigoquiz.shared.Question;
import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Murray Cumming <murrayc@murrayc.com.com>
 * 
 */
public class GwtTestQuizService extends GWTTestCase {

	@Test
	public void test() {
		// Setup an asynchronous event handler.
		final AsyncCallback<Question> callback = new AsyncCallback<Question>() {
			@Override
			public void onFailure(final Throwable caught) {
				fail(caught.toString());
			}

			@Override
			public void onSuccess(final Question question) {
				finishTest();
			}
		};

		delayTestFinish(500);

		final QuizServiceAsync service = QuizServiceAsync.Util.getInstance();
		assertNotNull(service);
		service.getQuestion("1", callback);
	}

	@Override
	public String getModuleName() {
		return "com.murrayc.BigOQuiz";
	}

}
