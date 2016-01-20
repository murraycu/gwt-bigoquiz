package com.murrayc.bigoquiz.client;

/**
 * Created by murrayc on 1/19/16.
 */
public class StringUtils {

    public static boolean isEmpty(final String str) {
        return (str == null) || (str.isEmpty());
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(final String a, final String b) {
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
    public static String defaultString(final String text) {
        if (text == null) {
            return "";
        } else {
            return text;
        }
    }

}

