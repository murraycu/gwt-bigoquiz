package com.murrayc.bigoquiz.client.application.question;

import com.murrayc.bigoquiz.shared.Question;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by murrayc on 7/8/17.
 */
@Path("/api/question")
public interface QuestionClient extends RestService {
    @GET
    @Path("/next")
    public void getNextQuestion(@QueryParam("quiz-id") String quizId, @QueryParam("section-id") String sectionId, MethodCallback<Question> callback);
}
