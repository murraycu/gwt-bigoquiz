package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/18/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Quiz implements IsSerializable {
    /** These are the simple details of the quiz,
     * without the full details of all the questions and answers,
     * so we can describe the quiz without providing the whole thing.
     */
    public static class QuizDetails extends HasIdAndTitle
            implements IsSerializable {

        private /* final */ boolean isPrivate = false;

        public QuizDetails() {
        }

        public QuizDetails(final String id, final String title) {
            super(id, title, null);
        }

        public boolean getIsPrivate() {
            return isPrivate;
        }

        private void setIsPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
        }
    }

    private /* final */ QuizDetails details = new QuizDetails();

    //Map of section ID to (map of question IDs to question):
    @NotNull
    private transient final Map<String, Map<String, QuestionAndAnswer>> questions = new HashMap<>();

    private /* final */ QuizSections quizSections = new QuizSections();

    // Whether we need extra code to support MathML, such as MathJax.
    private /* final */ boolean usesMathML;

    //TODO: This is useless on the client:
    //An extra list, used only for getting a random question,
    //regardless of what section it is in.
    private transient final List<QuestionAndAnswer> listQuestions = new ArrayList<>();

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

    public void setIsPrivate(boolean isPrivate) {
        details.setIsPrivate(isPrivate);
    }

    public boolean getIsPrivate() {
        return details.isPrivate;
    }

    @JsonIgnore
    public QuizDetails getDetails() {
        return details;
    }

    public void addQuestion(final String sectionId, @NotNull final QuestionAndAnswer questionAndAnswer) {
        Map<String, QuestionAndAnswer> map = questions.computeIfAbsent(sectionId, k -> new HashMap<>());
        map.put(questionAndAnswer.getId(), questionAndAnswer);

        //Store it here too:
        listQuestions.add(questionAndAnswer);

        //And here too:
        QuizSections.Section section = quizSections.getSection(sectionId);
        if (section == null) {
            Log.error("Quiz.addQuestion(): section not present: " + sectionId);
            return;
        }

        section.addQuestion(questionAndAnswer);
    }

    @Nullable
    public Question getRandomQuestion(final String sectionId) {
        if (!StringUtils.isEmpty(sectionId)) {
            final QuizSections.Section section = quizSections.getSection(sectionId);
            if (section != null) {
                final List<QuestionAndAnswer> sectionQuestions = section.getQuestions();
                if ((sectionQuestions != null) && !sectionQuestions.isEmpty()) {
                    return getRandomQuestionFromList(sectionQuestions);
                }
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
    @JsonIgnore
    public QuizSections getSections() {
        return quizSections;
    }

    @NotNull
    @JsonProperty("sections")
    public List<QuizSections.Section> getSectionsList() {
        return quizSections.getSections();
    }

    public List<QuestionAndAnswer> getQuestionsForSection(@NotNull final String sectionId) {
        QuizSections.Section section = quizSections.getSection(sectionId);
        if (section == null) {
            Log.error("Quiz.addQuestion(): section not present: " + sectionId);
            return null;
        }

        return section.getQuestions();
    }

    /** Get the count of all questions in all sections.
     *
     * @return
     */
    public int getQuestionsCount() {
        return listQuestions.size();
    }
}
