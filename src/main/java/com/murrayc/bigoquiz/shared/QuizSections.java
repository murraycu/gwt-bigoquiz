package com.murrayc.bigoquiz.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by murrayc on 1/24/16.
 */
class QuizSections {
    //Map of section ID to section title.
    private Map<String, String> sectionTitles = new HashMap<>();

    //Map of section ID to default choices:
    private Map<String, List<String>> defaultChoices = new HashMap<>();

    void addSection(final String sectionId, final String sectionTitle, final List<String> defaultChoices) {
        this.sectionTitles.put(sectionId, sectionTitle);
        this.defaultChoices.put(sectionId, defaultChoices);
    }

    public Set<String> getSectionIds() {
        return sectionTitles.keySet();
    }

    //TODO: Internationalization.
    public String getSectionTitle(final String sectionId) {
        return sectionTitles.get(sectionId);
    }

    //TODO: Internationalization.
    public void setSectionTitle(final String sectionId, final String sectionTitle) {
        sectionTitles.put(sectionId, sectionTitle);
    }
}
