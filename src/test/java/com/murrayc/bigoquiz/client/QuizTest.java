package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by murrayc on 1/27/16.
 */
public class QuizTest {

    public static final String QUIZ_ID = "somequiz";
    public static final String QUIZ_TITLE = "Some Quiz";
    public static final String SECTION_1 = "section1";
    public static final String SECTION_TITLE_1 = "section title 1";
    public static final String SECTION_2 = "section2";
    public static final String SECTION_TITLE_2 = "section title 2";

    public static final String SUBSECTION_1_1 = "subsection1.1";
    public static final String QUESTION_1 = "question1";
    public static final String QUESTION_2 = "question2";
    public static final String QUESTION_3 = "question3";
    public static final String QUESTION_ANSWER_1 = "answer1";
    public static final String QUESTION_ANSWER_2 = "answer2";
    public static final String QUESTION_ANSWER_3 = "answer3";


    // TODO: This seems to all run in Java. We need to somehow serialize the
    // JSON from Java and deserialize it in the compiled (from Java) Javascript.
    @Test
    public void QuizJsonTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Extra checks:
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
        //objectMapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);

        // Get the JSON for an object:
        Quiz objToWrite = createQuiz();
        final String json = objectMapper.writeValueAsString(objToWrite);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        Quiz obj = objectMapper.readValue(json, Quiz.class);
        assertNotNull(obj);

        final QuizSections sections = obj.getSections();
        final Map<String, QuizSections.Section> listSections = sections.getSections();
        assertNotNull(listSections);
        assertFalse(listSections.isEmpty());
        assertEquals(2, listSections.size());

        final List<QuizSections.Section> listSectionsInSeq = sections.getSectionsInSequence();
        assertNotNull(listSectionsInSeq);
        assertFalse(listSectionsInSeq.isEmpty());
        assertEquals(2, listSectionsInSeq.size());
        assertEquals(listSectionsInSeq.get(0).getId(), SECTION_1);
        assertEquals(listSectionsInSeq.get(0).getTitle(), SECTION_TITLE_1);
        assertEquals(listSectionsInSeq.get(1).getId(), SECTION_2);
        assertEquals(listSectionsInSeq.get(1).getTitle(), SECTION_TITLE_2);

        final QuizSections.Section section = listSectionsInSeq.get(0);
        assertNotNull(section);
        assertEquals(SECTION_1, section.getId());
    }

    @NotNull
    private Quiz createQuiz() {
        @NotNull final Quiz quiz = new Quiz();
        quiz.setId(QUIZ_ID);
        quiz.setTitle(QUIZ_TITLE);

        @NotNull final QuizSections sections = new QuizSections();
        @NotNull final QuizSections.Section section1 =
                sections.addSection(SECTION_1, SECTION_TITLE_1, null,null);
        @NotNull final QuizSections.Section section2 =
                sections.addSection(SECTION_2, SECTION_TITLE_2, null, null);
        quiz.setSections(sections);

        section1.addQuestion(createQuestionAndAnswer(QUESTION_1, SECTION_1, null, QUESTION_ANSWER_1));
        section1.addQuestion(createQuestionAndAnswer(QUESTION_2, SECTION_1, null, QUESTION_ANSWER_2));

        return quiz;
    }

    @Nullable
    private Question createQuestion(final String questionId, final String sectionId, final String subSectionId) {
        return new Question(questionId, sectionId, subSectionId,
                new Question.Text("question text", false), "someurl", null, null, null, null);
    }

    @Nullable
    private QuestionAndAnswer createQuestionAndAnswer(final String questionId, final String sectionId, final String subSectionId, final String answer) {
        final QuestionAndAnswer result = new QuestionAndAnswer();
        result.setQuestion(createQuestion(questionId, sectionId, subSectionId));

        final Question.Text answerText = new Question.Text(answer, false);
        result.setAnswer(answerText);
        return result;
    }
}