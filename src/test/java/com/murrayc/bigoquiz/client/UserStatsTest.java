package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by murrayc on 7/20/17.
 */
public class UserStatsTest {
    @Test
    public void jsonTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Extra checks:
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
        //objectMapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);

        UserStats objToWrite = new UserStats();

        // Marked with @JsonIgnore:
        // final String USER_ID = "useridfoo";
        // objToWrite.setUserId(USER_ID);

        final String QUIZ_ID = "quizfoobar";
        objToWrite.setQuizId(QUIZ_ID);

        final String SECTION_ID = "sectionidfoo";
        objToWrite.setSectionId(SECTION_ID);

        final int ANSWERED = 123;
        objToWrite.setAnswered(ANSWERED);

        final int ANSWERED_ONCE = 234;
        objToWrite.setAnsweredOnce(ANSWERED_ONCE);

        final int CORRECT = 345;
        objToWrite.setCorrect(CORRECT);

        final int CORRECT_ONCE = 345;
        objToWrite.setCorrectOnce(CORRECT_ONCE);


        final int PROBLEM_QUESTIONS_HISTORIES_COUNT = 3;
        objToWrite.setProblemQuestionHistoriesCount(PROBLEM_QUESTIONS_HISTORIES_COUNT);

        final List<UserQuestionHistory> TOP_PROBLEM_QUESTION_HISTORIES = new ArrayList<>();
        TOP_PROBLEM_QUESTION_HISTORIES.add(new UserQuestionHistory());
        TOP_PROBLEM_QUESTION_HISTORIES.add(new UserQuestionHistory());
        objToWrite.setTopProblemQuestionHistories(TOP_PROBLEM_QUESTION_HISTORIES);

        // Create JSON from the object:
        final String json = objectMapper.writeValueAsString(objToWrite);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Create an object from the JSON:
        UserStats obj = objectMapper.readValue(json, UserStats.class);
        assertNotNull(obj);

        // Marked with @JsonIgnore: assertEquals(USER_ID, obj.getUserId());
        assertEquals(QUIZ_ID, obj.getQuizId());
        assertEquals(SECTION_ID, obj.getSectionId());
        assertEquals(ANSWERED, obj.getAnswered());
        assertEquals(ANSWERED_ONCE, obj.getAnsweredOnce());
        assertEquals(CORRECT, obj.getCorrect());
        assertEquals(CORRECT_ONCE, obj.getCorrectOnce());
        assertEquals(PROBLEM_QUESTIONS_HISTORIES_COUNT, obj.getProblemQuestionHistoriesCount());

        final List<UserQuestionHistory> objTopProblems = obj.getTopProblemQuestionHistories();
        assertNotNull(objTopProblems);
        assertEquals(TOP_PROBLEM_QUESTION_HISTORIES.size(), objTopProblems.size());
    }
}
