package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.shared.Question;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
    void getQuestion(AsyncCallback<Question> async)
        throws IllegalArgumentException;
}
