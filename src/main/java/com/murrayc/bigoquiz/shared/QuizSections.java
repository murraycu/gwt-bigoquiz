package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizSections implements IsSerializable {
    //TODO: Can this be non-public while still being serializable by GWT?

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

        public void setSubSections(@NotNull Map<String, SubSection> subSections) {
            this.subSections = subSections;
        }

        public List<Question.Text> getDefaultChoices() {
            return defaultChoices;
        }

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
    public List<Section> getSectionsInSequence() {
        final List<Section> result = new ArrayList<>();

        for (final String sectionId : sectionsSequence) {
            result.add(getSection(sectionId));
        }

        return result;
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
