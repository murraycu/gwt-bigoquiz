package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/24/16.
 */
public class QuizSections implements IsSerializable {
    //TODO: Can this be non-public while still being serializable by GWT?

    public static class HasIdAndTitle implements IsSerializable {
        public HasIdAndTitle() {
        }

        public HasIdAndTitle(final String id, final String title) {
            this.id = id;
            this.title = title;
        }

        public String id;
        public String title;
    }

    /**
     * Created by murrayc on 1/30/16.
     */
    public static class SubSection extends HasIdAndTitle {
        public String link;

        public SubSection() {
        }

        public SubSection(final String id, final String title, final String link) {
            super(id, title);

            this.title = title;
            this.link = link;
        }
    }

    //TODO: Can this be non-public while still being serializable by GWT?
    static public class Section extends HasIdAndTitle {
        @NotNull
        public Map<String, SubSection> subSections = new HashMap<>();
        public List<String> defaultChoices;
        public int questionsCount;
    }

    //Map of section ID to sections.
    @NotNull
    private Map<String, Section> sections = new HashMap<>();

    public void addSection(final String sectionId, final String sectionTitle, final List<String> defaultChoices) {
        @NotNull final Section section = new Section();
        section.id = sectionId;
        section.title = sectionTitle;
        section.defaultChoices = defaultChoices;
        this.sections.put(sectionId, section);
    }

    public void addSubSection(final String sectionId, final String subSectionId, final String subSectionTitle, final String subSectionLink) {
        final Section section = getSection(sectionId);
        if (section == null) {
            Log.error("addSubSection(): section does not exist: " + sectionId);
            return;
        }

        section.subSections.put(subSectionId,
                new SubSection(subSectionId, subSectionTitle, subSectionLink));
    }

    @NotNull
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

        return section.title;
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

        return section.subSections.get(subSectionId);
    }

    //TODO: Internationalization.
    @Nullable
    public String getSubSectionTitle(final String sectionId, final String subSectionId) {
        @Nullable final SubSection subSection = getSubSection(sectionId, subSectionId);
        if (subSection == null) {
            return null;
        }

        return subSection.title;
    }

    @NotNull
    public Collection<String> getTitles() {
        @NotNull final Collection<String> result = new ArrayList<>();
        for (@NotNull final Section section : sections.values()) {
            result.add(section.title);
        }

        return result;
    }

    @Nullable
    public String getIdForTitle(final String title) {
        //TODO: Use reverse hashmap?
        for (final String sectionId : sections.keySet()) {
            final Section section = sections.get(sectionId);
            if ((section != null) &&
                    StringUtils.equals(section.title, title)) {
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
     * Generate a sorted List of the sections.
     * @return
     */
    @NotNull
    public List<Section> getSectionsSorted() {
        final List<Section> result = new ArrayList<>(sections.values());

        //Do the sorting here on the client-side,
        //because the titles could (one day) be localized,
        //and the sorting would need to depend on the user's locale too.
        Collections.sort(result,
                generateTitleSortComparator());

        return result;
    }

    @NotNull
    private Comparator<HasIdAndTitle> generateTitleSortComparator() {
        return new Comparator<HasIdAndTitle>() {
            @Override
            public int compare(@Nullable final HasIdAndTitle o1, @Nullable final HasIdAndTitle o2) {
                if ((o1 == null) && (o2 == null)) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                }

                if ((o1.title == null) && (o2.title == null)) {
                    return 0;
                } else if (o1.title == null) {
                    return -1;
                }

                return o1.title.compareTo(o2.title);
            }
        };
    }

    //TODO: This is only used on the client side:
    /**
     * Generate a sorted List of the sections.
     * @return
     */
    @Nullable
    public List<SubSection> getSubSectionsSorted(final String sectionId) {
        final Section section = getSection(sectionId);
        if (section == null) {
            return null;
        }

        if (section.subSections == null) {
            return null;
        }

        final List<SubSection> result = new ArrayList<>(section.subSections.values());

        //Do the sorting here on the client-side,
        //because the titles could (one day) be localized,
        //and the sorting would need to depend on the user's locale too.
        Collections.sort(result,
                generateTitleSortComparator());

        return result;
    }
}
