package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by murrayc on 7/20/17.
 */
public class UserQuestionHistoryTest {
    @Test
    public void jsonTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Extra checks:
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
        //objectMapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);

        UserQuestionHistory objToWrite = new UserQuestionHistory();

        final String QUESTION_ID = "foobar";
        objToWrite.setQuestionId(QUESTION_ID);

        final String QUESTION_TITLE = "Foo Bar";
        objToWrite.setQuestionTitle(new Question.Text(QUESTION_TITLE, false));

        final String SUBSECTION_TITLE = "Sub Section Foo Bar";
        objToWrite.setSubSectionTitle(SUBSECTION_TITLE);

        final int COUNT_ANSWERED_WRONG = 123;
        objToWrite.setCountAnsweredWrong(COUNT_ANSWERED_WRONG);

        final boolean ANSWERED_CORRECT_ONCE = true;
        objToWrite.setAnsweredCorrectlyOnce(ANSWERED_CORRECT_ONCE);


        // Create JSON from the object:
        final String json = objectMapper.writeValueAsString(objToWrite);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Create an object from the JSON:
        UserQuestionHistory obj = objectMapper.readValue(json, UserQuestionHistory.class);
        assertNotNull(obj);

        assertEquals(QUESTION_ID, obj.getQuestionId());
        assertEquals(QUESTION_TITLE, obj.getQuestionTitle().text);
        assertEquals(COUNT_ANSWERED_WRONG, obj.getCountAnsweredWrong());
        assertEquals(ANSWERED_CORRECT_ONCE, obj.getAnsweredCorrectlyOnce());
        assertEquals(SUBSECTION_TITLE, obj.getSubSectionTitle());
    }
}
