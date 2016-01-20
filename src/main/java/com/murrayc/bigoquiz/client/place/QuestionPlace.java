package com.murrayc.bigoquiz.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.murrayc.bigoquiz.client.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionPlace extends Place {
    private final String questionId;

    public QuestionPlace() {
        this.questionId = null;
    }

    public QuestionPlace(final String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    @Prefix("question")
    public static class Tokenizer implements PlaceTokenizer<QuestionPlace> {
        final String questionIdKey = "questionId";
        private final String equals = "=";
        private final String separator = "&";

        @Override
        public QuestionPlace getPlace(final String token) {
            return new QuestionPlace(token);
        }

        @Override
        public String getToken(final QuestionPlace place) {
            // create the URL string:
            final HashMap<String, String> params = new HashMap<>();
            params.put(questionIdKey, place.getQuestionId());
            return buildParamsToken(params);
        }

        /**
         * Build a history token based on a HashMap of parameter names and values. This can later be parsed by
         * getTokenParams().
         *
         * @param params
         *            A HashMap of names and values.
         * @return A history string for use by getToken() implementation.
         */
        String buildParamsToken(final HashMap<String, String> params) {
            String token = "";
            for (final Map.Entry<String, String> entry : params.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                    continue;
                }

                if (!StringUtils.isEmpty(token)) {
                    token += separator;
                }

                token += key + equals + value;
            }

            return token;
        }
    }
}
