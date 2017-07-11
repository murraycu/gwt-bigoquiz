package com.murrayc.bigoquiz.server.api;

import com.murrayc.bigoquiz.client.UnknownQuizException;
import com.murrayc.bigoquiz.server.QuizzesMap;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Copyright (c) 2016 Murray Cumming
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * Created by murrayc on 7/8/17.
 */
public class ResourceWithQuizzes {
    private static final String LOADED_QUIZZES = "loaded-quizzes";
    protected QuizzesMap quizzes = null;

    @Context
    ServletContext context;


    @NotNull
    protected Quiz getQuiz(final String quizId) throws UnknownQuizException, IllegalArgumentException {
        if (!loadQuizIntoQuizzes(quizId)) {
            throw new UnknownQuizException();
        }

        if (quizzes == null) {
            throw new UnknownQuizException();
        }

        final Quiz result = quizzes.map.get(quizId);
        if (result == null) {
            throw new UnknownQuizException();
        }

        return result;
    }

    private boolean loadQuizIntoQuizzes(final String quizId) {
        getQuizzesMap();

        return quizzes.loadQuizIntoQuizzes(quizId);
    }

    private void getQuizzesMap() {
        if (context == null) {
            throw new RuntimeException("ServletContext is null.");
        }

        //Use the existing shared quizzes if any:
        final Object object = context.getAttribute(LOADED_QUIZZES);
        if ((object != null) && !(object instanceof QuizzesMap)) {
            throw new RuntimeException("The loaded-quizzes attribute is not of the expected type.");
        }

        quizzes = (QuizzesMap) object;
        if (quizzes == null) {
            quizzes = new QuizzesMap();
            context.setAttribute(LOADED_QUIZZES, quizzes);
        }
    }

    protected void getOrLoadQuizzes() {
        // Load all quizzes.
        getQuizzesMap();

        quizzes.loadQuizzes();
    }

    protected void setQuestionExtras(final Question question, @NotNull Quiz quiz) {
        QuizSections.SubSection subSection = null;
        @NotNull final QuizSections sections = quiz.getSections();
        if (sections != null) {
            subSection = sections.getSubSection(question.getSectionId(),
                    question.getSubSectionId());
        }
        question.setTitles(quiz.getTitle(), subSection, question);

        question.setQuizUsesMathML(quiz.getUsesMathML());
    }
}
