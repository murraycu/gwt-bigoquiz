package com.murrayc.bigoquiz.client;

import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
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
		final AsyncCallback<QuestionAndAnswer> callback = new AsyncCallback<QuestionAndAnswer>() {
			@Override
			public void onFailure(final Throwable caught) {
				fail(caught.toString());
			}

			@Override
			public void onSuccess(final QuestionAndAnswer question) {
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
		return "org.glom.web.OnlineGlom";
	}

}
