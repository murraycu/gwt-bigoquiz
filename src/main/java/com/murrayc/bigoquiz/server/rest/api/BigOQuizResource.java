package com.murrayc.bigoquiz.server.rest.api;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.server.QuizLoader;
import com.murrayc.bigoquiz.server.QuizzesMap;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("quiz")
public class BigOQuizResource {
    private static final String LOADED_QUIZZES = "loaded-quizzes";

    private QuizzesMap quizzes = null;

    public BigOQuizResource() {
    }

    @GET
    @Produces("application/json")
    public Collection<Quiz> get() {
        getOrLoadQuizzes();

        if (quizzes == null) {
            return null;
        }

        return quizzes.map.values();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Quiz getById(@PathParam("id") String id) {
        getOrLoadQuizzes();

        if (quizzes == null) {
            return null;
        }

        return quizzes.map.get(id);
    }


    private void getQuizzesMap() {
        /*
        final ServletConfig config = this.getServletConfig();
        if (config == null) {
            throw new RuntimeException("getServletConfig() returned null.");
        }

        final ServletContext context = config.getServletContext();
        if (context == null) {
            throw new RuntimeException("getServletContext() returned null.");
        }

        //Use the existing shared quizzes if any:
        final Object object = context.getAttribute(LOADED_QUIZZES);
        if ((object != null) && !(object instanceof QuizzesMap)) {
            throw new RuntimeException("The loaded-quizzes attribute is not of the expected type.");
        }

        quizzes = (QuizzesMap) object;
        */
        if (quizzes == null) {
            quizzes = new QuizzesMap();
            //context.setAttribute(LOADED_QUIZZES, quizzes);
        }
    }

    private void getOrLoadQuizzes() {
        // Load all quizzes.
        getQuizzesMap();

        quizzes.loadQuizzes();
    }

}
