package com.murrayc.bigoquiz.client.application.quiz;

import com.murrayc.bigoquiz.shared.Quiz;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

/**
 * Created by murrayc on 7/8/17.
 */
@Path("/api/quiz")
public interface QuizClient extends RestService {
    @GET
    public void getQuiz(MethodCallback<List<Quiz>> callback);

    @GET
    @Path("/{id}")
    public void getQuiz(@PathParam("id") String id, MethodCallback<List<Quiz>> callback);

}