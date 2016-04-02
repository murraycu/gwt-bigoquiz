package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class Quiz implements IsSerializable {
    /** These are the simple details of the quiz,
     * without the full details of all the questions and answers,
     * so we can describe the quiz without providing the whole thing.
     */
    public static class QuizDetails implements IsSerializable {
        private String id;
        private String title;

        public QuizDetails() {
        }

        public QuizDetails(final String id, final String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    private /* final */ QuizDetails details = new QuizDetails();

    //Map of section ID to (map of question IDs to question):
    @NotNull
    private /* final */ Map<String, Map<String, QuestionAndAnswer>> questions = new HashMap<>();

    private /* final */ QuizSections quizSections = new QuizSections();

    //TODO: This is useless on the client:
    //An extra list, used only for getting a random question,
    //regardless of what section it is in.
    private transient final List<QuestionAndAnswer> listQuestions = new ArrayList<>();

    //TODO: This is useless on the client:
    //This is only for getting a random question from a particular section:
    private transient final Map<String, List<QuestionAndAnswer>> listSectionQuestions = new HashMap<>();

    public Quiz() {
    }

    public void setId(final String id) {
        this.details.id = id;
    }

    public String getId() {
        return details.id;
    }

    public void setTitle(final String title) {
        this.details.title = title;
    }

    public String getTitle() {
        return details.title;
    }

    public QuizDetails getDetails() {
        return details;
    }

    public void addQuestion(final String sectionId, @NotNull final QuestionAndAnswer questionAndAnswer) {
        Map<String, QuestionAndAnswer> map = questions.get(sectionId);
        if (map == null) {
            map = new HashMap<>();
            questions.put(sectionId, map);
        }

        map.put(questionAndAnswer.getId(), questionAndAnswer);

        //Store it here too:
        listQuestions.add(questionAndAnswer);

        //And here too:
        List<QuestionAndAnswer> sectionList = listSectionQuestions.get(sectionId);
        if (sectionList == null) {
            sectionList = new ArrayList<>();
            listSectionQuestions.put(sectionId, sectionList);
        }
        sectionList.add(questionAndAnswer);
    }

    @Nullable
    public Question getRandomQuestion(final String sectionId) {
        if (!StringUtils.isEmpty(sectionId)) {
            final List<QuestionAndAnswer> sectionQuestions = listSectionQuestions.get(sectionId);
            if ((sectionQuestions != null) && !sectionQuestions.isEmpty()) {
                return getRandomQuestionFromList(sectionQuestions);
            }
        }

        return getRandomQuestionFromList(listQuestions);
    }

    @Nullable
    private static Question getRandomQuestionFromList(@Nullable final List<QuestionAndAnswer> listQuestions) {
        if (listQuestions == null) {
            return null;
        }

        final int index = new Random().nextInt(listQuestions.size());
        final QuestionAndAnswer questionAndAnswer = listQuestions.get(index);
        if (questionAndAnswer == null) {
            Log.error("getRandomQuestionFromList(): QuestionAndAnswer was null.");
            return null;
        }

        return questionAndAnswer.getQuestion();
    }

    public boolean contains(final String questionId) {
        return questions.containsKey(questionId);
    }

    @Nullable
    public Question getQuestion(final String questionId) {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getQuestion();
        }

        return null;
    }

    @Nullable
    public String getAnswer(final String questionId) {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getAnswer();
        }

        return null;
    }

    public void addSection(final String sectionId, final String sectionTitle, final List<String> defaultChoices) {
        if (quizSections.containsSection(sectionId)) {
            Log.error("Quiz.addSection(): section already exists. Replacing. sectionId: " + sectionId);
        }

        quizSections.addSection(sectionId, sectionTitle, defaultChoices);
    }

    public void setSectionQuestionsCount(final String sectionId, int questionsCount) {
        final QuizSections.Section section = quizSections.getSection(sectionId);
        if (section == null) {
            Log.error("Quiz.setSectionQuestionsCount(): section does not already exist. Failing.");
            return;
        }

        section.questionsCount = questionsCount;
    }

    public void setSectionDefaultChoicesFromAnswers(final String sectionId) {
        final QuizSections.Section section = quizSections.getSection(sectionId);
        if (section == null) {
            Log.error("setSubSectionDefaultChoicesFromAnswers(): section not found.");
            return;
        }

        //Use a set to avoid duplicates:
        final Set<String> choicesSet = new HashSet<>();
        final List<QuestionAndAnswer> sectionList = listSectionQuestions.get(sectionId);
        for (final QuestionAndAnswer question : sectionList) {
            choicesSet.add(question.getAnswer());
        }

        //TODO: Actually use this instead of setting the choice directly in each question?
        final List<String> choices = new ArrayList<>(choicesSet);
        section.defaultChoices = choices;

        for (final QuestionAndAnswer questionAndAnswer : sectionList) {
            final Question question = questionAndAnswer.getQuestion();
            question.setChoices(choices);
        }
    }


    public void addSubSection(final String sectionId, final String subSectionId, final String subSectionTitle, final String subSectionLink) {
        if (!quizSections.containsSection(sectionId)) {
            Log.error("Quiz.addSection(): section does not already exist. Failing.");
            return;
        }

        quizSections.addSubSection(sectionId, subSectionId, subSectionTitle, subSectionLink);
    }

    @Nullable
    public QuestionAndAnswer getQuestionAndAnswer(final String questionId) {
        //Look in every section:
        for (@NotNull Map<String, QuestionAndAnswer> section : questions.values()) {
            final QuestionAndAnswer questionAndAnswer = section.get(questionId);
            if (questionAndAnswer != null) {
                return questionAndAnswer;
            }
        }

        return null;
    }

    /*
    public String getSectionTitle(final String sectionId) {
        return quizSections.getSectionTitle(sectionId);
    }
    */

    @NotNull
    public QuizSections getSections() {
        return quizSections;
    }

    public List<QuestionAndAnswer> getQuestionsForSection(@NotNull final String sectionId) {
        if (listSectionQuestionsIsEmpty()) {
            fillListSectionQuestions();
            if (listSectionQuestionsIsEmpty()) {
                return null;
            }
        }

        return listSectionQuestions.get(sectionId);
    }

    private boolean listSectionQuestionsIsEmpty() {
        return listSectionQuestions == null ||
                listSectionQuestions.isEmpty();
    }

    /**
     * Fill the whole listSectionQuestions cache.
     */
    private void fillListSectionQuestions() {
        for (final String sectionId : questions.keySet()) {
            final Map<String, QuestionAndAnswer> mapQuestions = questions.get(sectionId);
            if (mapQuestions == null) {
                continue;
            }

            listSectionQuestions.put(sectionId, new ArrayList<>(mapQuestions.values()));
        }
    }
}
