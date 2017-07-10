package com.murrayc.bigoquiz.client.application.quiz;

import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * Created by murrayc on 7/8/17.
 */
@Path("/api/quiz")
public interface QuizClient extends RestService {
    @GET
    public void getQuiz(@QueryParam("list-only") boolean listOnly, MethodCallback<List<Quiz>> callback);

    @GET
    @Path("/{id}")
    public void getQuiz(@PathParam("id") String id, @QueryParam("list-only") boolean listOnly, MethodCallback<List<Quiz>> callback);

    @GET
    @Path("/{quiz-id}/section")
    public void getQuizSectionsById(@PathParam("quiz-id") String id, @QueryParam("list-only") boolean listOnly, MethodCallback<QuizSections> callback);

    // TODO: Add /{quiz-id}/question/, returning all quiz questions, just for completeness.

    @GET
    @Path("/{quiz-id}/question/{question-id}")
    public void getQuizQuestion(@PathParam("quiz-id") String quizId, @PathParam("question-id") String questionId, MethodCallback<Question> callback);
}