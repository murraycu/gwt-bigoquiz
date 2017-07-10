package com.murrayc.bigoquiz.client.application.userhistorysections;

import com.murrayc.bigoquiz.client.UserHistoryOverall;
import com.murrayc.bigoquiz.client.UserHistorySections;
import com.murrayc.bigoquiz.shared.SubmissionResult;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Created by murrayc on 7/8/17.
 * @see UserHistoryResource.
 */
@Path("/api/user-history")
public interface UserHistoryClient extends RestService {
    /**
     * requestUrl is the URL that the user should be returned to after logging in or out.
     * It corresponds to the destinationURL parameter to UserService.createLogoutURL():
     * https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/users/UserService.html#createLogoutURL-java.lang.String-java.lang.String-
     *
     * @return
     */
    @GET
    public void get(@QueryParam("requestUrl") String requestUrl, MethodCallback<UserHistoryOverall> callback);

    /**
     * requestUrl is the URL that the user should be returned to after logging in or out.
     * It corresponds to the destinationURL parameter to UserService.createLogoutURL():
     * https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/users/UserService.html#createLogoutURL-java.lang.String-java.lang.String-
     *
     * @return
     */
    @GET
    @Path("/{quizId}")
    public void getByQuizId(@PathParam("quizId") String quizId, @QueryParam("requestUrl") String requestUrl, MethodCallback<UserHistorySections> callback);

    @POST
    @Path("/reset-sections")
    public void resetSections(@QueryParam("quizId") String quizId, MethodCallback<Void> callback);

    @POST
    @Path("/submit-answer")
    public void submitAnswer(@QueryParam("quizId") String quizId, @QueryParam("questionId") String questionId, @QueryParam("answer") String answer, @QueryParam("nextQuestionSectionId") String nextQuestionSectionId, MethodCallback<SubmissionResult> callback);

    @POST
    @Path("/submit-dont-know-answer")
    public void submitDontKnowAnswer(@QueryParam("quizId") String quizId, @QueryParam("questionId") String questionId, @QueryParam("nextQuestionSectionId") String nextQuestionSectionId,  MethodCallback<SubmissionResult> callback);
}
