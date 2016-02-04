package com.murrayc.bigoquiz.client;

import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by murrayc on 1/27/16.
 */
public class UserRecentHistoryTest {

    public static final String SECTION_1 = "section1";
    public static final String SUBSECTION_1_1 = "subsection1.1";

    @Test
    public void testAddUserAnswerAtStart() throws Exception {
        //TODO: Break this up into smaller tests,
        //when the UserRecentHistory API has settled down.
        @NotNull UserRecentHistory history = createUserRecentHistory();

        @Nullable Question question = createQuestion("question4", SECTION_1, SUBSECTION_1_1);
        history.addUserAnswerAtStart(question, false);

        final UserStats stats = history.getStats(SECTION_1);
        assertNotNull(stats);

        @NotNull Collection<UserQuestionHistory> problems = stats.getQuestionHistories();
        assertNotNull(problems);
        assertEquals(1, problems.size());

        history.addUserAnswerAtStart(question, false);
        problems = stats.getQuestionHistories();
        assertNotNull(problems);
        assertEquals(1, problems.size());

        question = createQuestion("question5", SECTION_1, SUBSECTION_1_1);
        history.addUserAnswerAtStart(question, false);

        problems = stats.getQuestionHistories();
        assertNotNull(problems);
        assertEquals(2, problems.size());

        //assertEquals("question5", problems.get(0).getQuestionId());
    }

    @NotNull
    private UserRecentHistory createUserRecentHistory() {
        @NotNull final QuizSections sections = new QuizSections();
        sections.addSection(SECTION_1, "section 1", null);
        sections.addSection("section2", "section 2", null);

        @NotNull final UserStats stats = new UserStats();

        @NotNull UserRecentHistory history = new UserRecentHistory("userid 1", sections);
        history.setSectionStats("section1", stats);

        return history;
    }

    @Nullable
    private Question createQuestion(final String questionId, final String sectionId, final String subSectionId) {
        return new Question(questionId, sectionId, subSectionId, "question text", null);
    }
}