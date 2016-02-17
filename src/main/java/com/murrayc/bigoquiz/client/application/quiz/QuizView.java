package com.murrayc.bigoquiz.client.application.quiz;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewImpl;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.Quiz;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizView extends ViewImpl
        implements QuizPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Label labelError = Utils.createServerErrorLabel(constants);
    private final Label labelTitle = new InlineLabel();


    QuizView() {
        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        mainPanel.add(labelError);

        mainPanel.add(labelTitle);

        initWidget(mainPanel);
    }




    @Override
    public void setQuiz(@NotNull final Quiz quiz) {
        labelTitle.setText(quiz.getTitle());
    }

    @Override
    public void setServerFailed() {
        labelError.setVisible(true);
    }
}
