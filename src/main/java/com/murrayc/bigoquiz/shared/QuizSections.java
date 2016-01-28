package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

/**
 * Created by murrayc on 1/24/16.
 */
public class QuizSections implements IsSerializable {
    //Map of section ID to section title.
    private Map<String, String> sectionTitles = new HashMap<>();

    //Map of section ID to default choices:
    private Map<String, List<String>> defaultChoices = new HashMap<>();

    public void addSection(final String sectionId, final String sectionTitle, final List<String> defaultChoices) {
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
    /*
    public void setSectionTitle(final String sectionId, final String sectionTitle) {
        sectionTitles.put(sectionId, sectionTitle);
    }
    */

    public Collection<String> getTitles() {
        return sectionTitles.values();
    }

    public String getIdForTitle(final String title) {
        //TODO: Use reverse hashmap?
        for (final String id : sectionTitles.keySet()) {
            if (StringUtils.equals(
                    getSectionTitle(id), title)) {
                return id;
            }
        }

        return null;
    }
}
