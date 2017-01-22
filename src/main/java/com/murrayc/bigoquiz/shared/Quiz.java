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
    public static class QuizDetails extends HasIdAndTitle
            implements IsSerializable {
        public QuizDetails() {
        }

        public QuizDetails(final String id, final String title) {
            super(id, title);
        }
    }

    private /* final */ QuizDetails details = new QuizDetails();

    //Map of section ID to (map of question IDs to question):
    @NotNull
    private /* final */ Map<String, Map<String, QuestionAndAnswer>> questions = new HashMap<>();

    private /* final */ QuizSections quizSections = new QuizSections();

    // Whether we need extra code to support MathML, such as MathJax.
    private /* final */ boolean usesMathML;

    //TODO: This is useless on the client:
    //An extra list, used only for getting a random question,
    //regardless of what section it is in.
    private transient final List<QuestionAndAnswer> listQuestions = new ArrayList<>();

    //TODO: This is useless on the client:
    //This is only for getting a random question from a particular section:
    private transient final Map<String, List<QuestionAndAnswer>> listSectionQuestions = new HashMap<>();

    public Quiz() {
    }


    public String getId() {
        return details.getId();
    }

    public void setId(final String id) {
        details.setId(id);
    }

    public void setTitle(final String title) {
        this.details.setTitle(title);
    }

    public String getTitle() {
        return details.getTitle();
    }

    public void setUsesMathML(boolean usesMathML) {
        this.usesMathML = usesMathML;
    }

    public boolean getUsesMathML() {
        return usesMathML;
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

    /**
     * Get the question by ID.
     *
     * @param questionId
     * @return
     */
    @Nullable
    public Question getQuestion(final String questionId) {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getQuestion();
        }

        return null;
    }

    @Nullable
    public Question.Text getAnswer(final String questionId) {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getAnswer();
        }

        return null;
    }

    public void addSection(final String sectionId, final String sectionTitle, final String sectionLink, final List<Question.Text> defaultChoices) {
        if (quizSections.containsSection(sectionId)) {
            Log.error("Quiz.addSection(): section already exists. Replacing. sectionId: " + sectionId);
        }

        quizSections.addSection(sectionId, sectionTitle, sectionLink, defaultChoices);
    }

    public void setSectionQuestionsCount(final String sectionId, int questionsCount) {
        final QuizSections.Section section = quizSections.getSection(sectionId);
        if (section == null) {
            Log.error("Quiz.setSectionQuestionsCount(): section does not already exist. Failing.");
            return;
        }

        section.questionsCount = questionsCount;
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

    /** Get the count of all questions in all sections.
     *
     * @return
     */
    public int getQuestionsCount() {
        return listQuestions.size();
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
