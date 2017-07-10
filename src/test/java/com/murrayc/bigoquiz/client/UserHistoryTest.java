package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by murrayc on 1/27/16.
 */
public class UserHistoryTest {

    public static final String QUIZ_ID = "somequiz";
    public static final String SECTION_1 = "section1";
    public static final String SUBSECTION_1_1 = "subsection1.1";
    public static final String USER_ID_1 = "userid 1";

    @Test
    public void testAddUserAnswerAtStart() throws Exception {
        //TODO: Break this up into smaller tests,
        //when the UserHistorySections API has settled down.
        @NotNull UserHistorySections history = createUserRecentHistory();

        @Nullable Question question = createQuestion("question4", SECTION_1, SUBSECTION_1_1);
        history.addUserAnswerAtStart(QUIZ_ID, question, false);

        final UserStats stats = history.getStats(SECTION_1);
        assertNotNull(stats);

        @NotNull Collection<UserQuestionHistory> problems = stats.getTopProblemQuestionHistories();
        assertNotNull(problems);
        assertEquals(1, problems.size());

        history.addUserAnswerAtStart(QUIZ_ID, question, false);
        problems = stats.getTopProblemQuestionHistories();
        assertNotNull(problems);
        assertEquals(1, problems.size());

        question = createQuestion("question5", SECTION_1, SUBSECTION_1_1);
        history.addUserAnswerAtStart(QUIZ_ID, question, false);

        problems = stats.getTopProblemQuestionHistories();
        assertNotNull(problems);
        assertEquals(2, problems.size());

        //assertEquals("question5", problems.get(0).getQuestionId());
    }

    @Test
    public void UserHistorySectionsJsonTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Get the JSON for an object:
        UserHistorySections objToWrite = createUserRecentHistory();
        final String json = objectMapper.writeValueAsString(objToWrite);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        UserHistorySections obj = objectMapper.readValue(json, UserHistorySections.class);
        assertNotNull(obj);

        final LoginInfo loginInfo = obj.getLoginInfo();
        assertNotNull(loginInfo);
        assertEquals(USER_ID_1, loginInfo.getUserId());

        final QuizSections sections = obj.getSections();
        assertNotNull(sections);

        final List<QuizSections.Section> listSections = sections.getSectionsInSequence();
        assertNotNull(listSections);
        assertFalse(listSections.isEmpty());

        final QuizSections.Section section = listSections.get(0);
        assertNotNull(section);
        assertEquals(SECTION_1, section.getId());
    }

    @NotNull
    private UserHistorySections createUserRecentHistory() {
        @NotNull final QuizSections sections = new QuizSections();
        sections.addSection(SECTION_1, "section 1", null,null);
        sections.addSection("section2", "section 2", null, null);

        @NotNull final UserStats stats = new UserStats();

        @NotNull LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUserId(USER_ID_1);
        @NotNull UserHistorySections history = new UserHistorySections(loginInfo, sections, "some title");
        history.setSectionStats("section1", stats);

        return history;
    }

    @Nullable
    private Question createQuestion(final String questionId, final String sectionId, final String subSectionId) {
        return new Question(questionId, sectionId, subSectionId,
                new Question.Text("question text", false), "someurl", null, null, null, null);
    }
}