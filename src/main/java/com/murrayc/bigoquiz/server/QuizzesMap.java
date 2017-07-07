package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.server.QuizLoader;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** A cache of loaded quizzes.
 *
 * Created by murrayc on 7/7/17.
 */
public class QuizzesMap {
    @Nullable
    public final Map<String, Quiz> map = new HashMap<>();
    public boolean allTitlesLoaded = false;


    public void loadQuizzes() {
        if (allTitlesLoaded) {
            return;
        }

        final String[] names = {
                QuizConstants.DEFAULT_QUIZ_ID,
                "algorithms_analysis",
                "designpatterns",
                "graphs",
                "cpp_std_algorithms",
                "notation",
                "numbers",
                "algorithms",
                "string_algorithms",
                "combinatorics",
                "math",
                "datastructures",
                "bitwise",
                "concurrency",
                "distributed_systems",
                "book_stepanov_fmtgp",
                "networking",
                "compilers"};

        for (final String name : names) {
            loadQuizIntoQuizzes(name);
        }

        allTitlesLoaded = true;
    }

    /**
     * Returns false if the load failed.
     *
     * @param quizId
     * @return
     */
    public boolean loadQuizIntoQuizzes(final String quizId) {
        if (map.containsKey(quizId)) {
            return true;
        }

        final Quiz quiz;
        try {
            quiz = loadQuiz(quizId);
            if (quiz != null) {
                map.put(quizId, quiz);
            }
        } catch (@NotNull final Exception e) {
            Log.error("Could not load quiz: " + quizId, e);
            return false;
        }

        return true;
    }

    private static Quiz loadQuiz(@NotNull final String quizId) {
        final String filename = "quizzes" + File.separator + quizId + ".xml";
        try (final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filename)) {
            if (is == null) {
                Log.fatal("quiz XML file not found: " + filename);
                return null;
            }

            try {
                return QuizLoader.loadQuiz(is);
            } catch (final QuizLoader.QuizLoaderException e) {
                Log.fatal("loadQuiz() failed", e);
            }
        } catch (final IOException e) {
            Log.error("loadQuiz(): Could not get file as stream from resouce", e);
        }

        return null;
    }
}
