package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizSections {
    /**
     * Created by murrayc on 1/30/16.
     */
    public static class SubSection extends HasIdAndTitle {

        public SubSection() {
        }

        public SubSection(final String id, final String title, final String link) {
            super(id, title, link);

        }
    }

    //TODO: Can this be non-public while still being serializable by GWT?
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static public class Section extends HasIdAndTitle {
        @NotNull
        private Map<String, SubSection> subSections = new HashMap<>();
        private List<String> subSectionsSequence = new ArrayList<>(); // IDs.
        private List<Question.Text> defaultChoices;
        private List<QuestionAndAnswer> questions;

        private int questionsCount;

        Section() {
        }

        Section(final String id, final String title, final String link) {
            super(id, title, link);
        }

        public List<QuestionAndAnswer> getQuestions() {
            return questions;
        }

        /** Without this, the client code will not fill the section with questions from the JSON.
         *
         * @param questions
         */
        public void setQuestions(final List<QuestionAndAnswer> questions) {
            this.questions = questions;
        }

        public void addQuestion(final QuestionAndAnswer questionAndAnswer) {
            if (questions == null) {
                questions = new ArrayList<>();
            }

            questions.add(questionAndAnswer);
        }

        @NotNull
        public Map<String, SubSection> getSubSections() {
            return subSections;
        }

        /** Without this, the client code will not fill the section with questions from the JSON.
         */
        public void setSubSections(@NotNull Map<String, SubSection> subSections) {
            this.subSections = subSections;
        }

        public List<Question.Text> getDefaultChoices() {
            return defaultChoices;
        }

        /** Without this, the client code will not fill the section with questions from the JSON.
         */
        public void setDefaultChoices(List<Question.Text> defaultChoices) {
            this.defaultChoices = defaultChoices;
        }

        public List<String> getSubSectionsSequence() {
            return subSectionsSequence;
        }

        public void setSubSectionsSequence(List<String> subSectionsSequence) {
            this.subSectionsSequence = subSectionsSequence;
        }

        public int getQuestionsCount() {
            return questionsCount;
        }

        public void setQuestionsCount(int questionsCount) {
            this.questionsCount = questionsCount;
        }
    }

    //Map of section ID to userhistorysections.
    @NotNull
    private Map<String, Section> sections = new HashMap<>();
    private List<String> sectionsSequence = new ArrayList<>();

    public void addSection(final String sectionId, final String sectionTitle, final String sectionLink, final List<Question.Text> defaultChoices) {
        @NotNull final Section section = new Section(sectionId, sectionTitle, sectionLink);
        section.setDefaultChoices(defaultChoices);
        addSection(section);
    }

    private void addSection(final Section section) {
        final String sectionId = section.getId();
        this.sections.put(sectionId, section);

        this.sectionsSequence.add(sectionId);
    }

    public void addSubSection(final String sectionId, final String subSectionId, final String subSectionTitle, final String subSectionLink) {
        final Section section = getSection(sectionId);
        if (section == null) {
            Log.error("addSubSection(): section does not exist: " + sectionId);
            return;
        }

        section.getSubSections().put(subSectionId,
                new SubSection(subSectionId, subSectionTitle, subSectionLink));
        section.getSubSectionsSequence().add(subSectionId);
    }

    @NotNull
    @JsonIgnore
    public Set<String> getSectionIds() {
        return sections.keySet();
    }

    //TODO: Internationalization.
    @Nullable
    public String getSectionTitle(final String sectionId) {
        final Section section = getSection(sectionId);
        if (section == null) {
            return null;
        }

        return section.getTitle();
    }

    //TODO: Internationalization.
    /*
    public void setSectionTitle(final String sectionId, final String sectionTitle) {
        sectionTitles.put(sectionId, sectionTitle);
    }
    */

    //TODO: Internationalization.
    @Nullable
    public SubSection getSubSection(final String sectionId, final String subSectionId) {
        final Section section = getSection(sectionId);
        if (section == null) {
            return null;
        }

        return section.getSubSections().get(subSectionId);
    }

    //TODO: Internationalization.
    @Nullable
    public String getSubSectionTitle(final String sectionId, final String subSectionId) {
        @Nullable final SubSection subSection = getSubSection(sectionId, subSectionId);
        if (subSection == null) {
            return null;
        }

        return subSection.getTitle();
    }

    /** Get a (sorted) list of section titles.
     *
     * @return
     */
    @NotNull
    @JsonIgnore
    public Collection<String> getTitles() {
        @NotNull final ArrayList<String> result = new ArrayList<>();
        for (@NotNull final Section section : sections.values()) {
            result.add(section.getTitle());
        }

        //Sort this instead of the order being arbitrary:
        Collections.sort(result);

        return result;
    }

    @Nullable
    public String getIdForTitle(final String title) {
        //TODO: Use reverse hashmap?
        for (final String sectionId : sections.keySet()) {
            final Section section = sections.get(sectionId);
            if ((section != null) &&
                    StringUtils.equals(section.getTitle(), title)) {
                return sectionId;
            }
        }

        return null;
    }

    public boolean containsSection(final String sectionId) {
        return sections.containsKey(sectionId);
    }

    public Section getSection(final String sectionId) {
        return sections.get(sectionId);
    }

    //TODO: This is only used on the client side:
    /**
     * Generate a sorted List of the Sections, sorted by original sequence in the XML file, not alphabetically.
     * @return
     */
    @NotNull
    @JsonIgnore
    public List<Section> getSectionsInSequence() {
        final List<Section> result = new ArrayList<>();

        for (final String sectionId : sectionsSequence) {
            result.add(getSection(sectionId));
        }

        return result;
    }

    /** This is only for the JSON output.
     *
     * @return
     */
    public Map<String, Section> getSections() {
        return this.sections;
    }

    /** This is only for the JSON input.
     */
    public void setSections(final Map<String, Section> sections) {
        this.sections = sections;
    }

    /** This is only for the JSON output.
     *
     * @return
     */
    public List<String> getSectionsSequence() {
        return this.sectionsSequence;
    }

    /** This is only for the JSON input.
     */
    public void setSectionsSequence(final List<String> sectionsSequence) {
        this.sectionsSequence = sectionsSequence;
    }

    //TODO: This is only used on the client side:
    /**
     * Generate a sorted List of the sub-sections, sorted by original sequence in the XML file, not alphabetically.
     * @return
     */
    @Nullable
    public List<SubSection> getSubSectionsSorted(final String sectionId) {
        final Section section = getSection(sectionId);
        if (section == null) {
            return null;
        }

        if (section.getSubSections() == null) {
            return null;
        }

        final List<SubSection> result = new ArrayList<>();

        for (final String subSectionId : section.getSubSectionsSequence()) {
            result.add(section.getSubSections().get(subSectionId));
        }

        return result;
    }
}
