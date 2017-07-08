package com.murrayc.bigoquiz.server.rest.api;

import com.murrayc.bigoquiz.server.QuizzesMap;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("quiz")
public class QuizResource extends ResourceWithQuizzes {

    public QuizResource() {
    }

    @GET
    @Produces("application/json")
    public Collection<Quiz> get(@QueryParam("list-only") boolean listOnly) {
        getOrLoadQuizzes();

        if (quizzes == null) {
            return null;
        }

        if (!listOnly) {
            return quizzes.map.values();
        } else {
            // Create a list of quizzes in which each quiz has only the ID and title.
            // TODO: Cache this.
            final List<Quiz> result = new ArrayList<>();
            for (final Quiz quiz : quizzes.map.values()) {
                final Quiz brief = new Quiz();
                brief.setId(quiz.getId());
                brief.setTitle(quiz.getTitle());
                result.add(brief);
            }

            return result;
        }
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
}
