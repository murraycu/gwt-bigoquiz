package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.murrayc.bigoquiz.shared.Question;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface QuizService extends RemoteService {
    Question getQuestion() throws IllegalArgumentException;
}
