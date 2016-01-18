package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.shared.Question;

/**
 * The async counterpart of <code>QuizService</code>.
 */
public interface QuizServiceAsync {
    void getQuestion(AsyncCallback<Question> async)
        throws IllegalArgumentException;
}
