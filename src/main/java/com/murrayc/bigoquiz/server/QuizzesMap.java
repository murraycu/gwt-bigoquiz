package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizConstants;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/** A cache of loaded quizzes.
 *
 * Created by murrayc on 7/7/17.
 */
public class QuizzesMap {
    @Nullable
    public final Map<String, Quiz> map = new HashMap<>();
    public final List<Quiz> listIdsAndTitles = new ArrayList(); // sorted
    public final Map<String, QuizSections> mapQuizSectionsIdAndTitle = new HashMap<>(); // in sequence

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

        listIdsAndTitles.sort(generateQuizTitleSortComparator());

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
            if (quiz == null) {
                return false;
            }
        } catch (@NotNull final Exception e) {
            Log.error("Could not load quiz: " + quizId, e);
            return false;
        }

        // Avoid a race when checking if it's already there and adding it,
        // and make sure that all the maps (including the ID+title caches)
        // are consistent.
        synchronized(this) {
            if (map.containsKey(quizId)) {
                // It's already been added.
                return true;
            }

            map.put(quizId, quiz);

            // Keep the ID and title separately, for the list-only server query.
            final Quiz brief = new Quiz();
            brief.setId(quizId);
            brief.setTitle(quiz.getTitle());
            listIdsAndTitles.add(brief);

            // Generate the simple list of sections, in advance:
            final QuizSections briefSections = mapQuizSectionsIdAndTitle.computeIfAbsent(quizId, k -> new QuizSections());
            for (final QuizSections.Section section : quiz.getSections().getSectionsInSequence()) {
                briefSections.addSection(section.getId(), section.getTitle(), section.getLink(), null);
            }
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


    @NotNull
    private static Comparator<Quiz> generateQuizTitleSortComparator() {
        return (o1, o2) -> {
            if ((o1 == null) && (o2 == null)) {
                return 0;
            } else if (o1 == null) {
                return -1;
            }

            final String title1 = o1.getTitle();
            final String title2 = o2.getTitle();
            if ((title1 == null) && (title2 == null)) {
                return 0;
            } else if (title1 == null) {
                return -1;
            }

            return title1.compareTo(title2);
        };
    }
}
