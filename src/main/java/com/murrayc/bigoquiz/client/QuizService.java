package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface QuizService extends RemoteService {
    Question getQuestion(final String questionId) throws IllegalArgumentException;
    Question getNextQuestion() throws IllegalArgumentException;
    boolean submitAnswer(final String questionId, final String answer) throws IllegalArgumentException;

    UserProfile getUserProfile() throws IllegalArgumentException;

    void increaseScore()  throws IllegalArgumentException;
}
