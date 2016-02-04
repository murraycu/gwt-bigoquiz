package com.murrayc.bigoquiz.shared;

import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/19/16.
 */
public class StringUtils {

    public static boolean isEmpty(@Nullable final String str) {
        return (str == null) || (str.isEmpty());
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(@Nullable final String a, @Nullable final String b) {
        if (a == null) {
            return b == null;
        }

        if (b == null) {
            return false; // a was already checked for null.
        }

        return a.equals(b);
    }

    /**
     * @param text
     * @return
     */
    @Nullable
    public static String defaultString(@Nullable final String text) {
        if (text == null) {
            return "";
        } else {
            return text;
        }
    }

}

