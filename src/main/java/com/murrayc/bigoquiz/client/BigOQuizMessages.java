package com.murrayc.bigoquiz.client;

//This is not generated with the 18nCreator (for instance, via the maven-gwt-plugin's gwt:i18n goal
//using <i18nMessagesBundles> declarations.

import com.google.gwt.i18n.client.LocalizableResource;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to represent the messages contained in resource bundle:
 * /home/murrayc/checkout/github/gwt-bigoquiz/src/main/resources/com/murrayc/bigoquiz/client/ui/BigOQuizMessages.properties'.
 */
@LocalizableResource.DefaultLocale("en")
public interface BigOQuizMessages extends com.google.gwt.i18n.client.Messages {

    /**
     * Translated "Answered: {0,number}: , Correct: {1,number}".
     *
     * @return translated "Answered: {0,number}: , Correct: {1,number}"
     */
    @NotNull
    @DefaultMessage("{1,number} correct / {0,number} answers")
    @AlternateMessage({"one", "{1,number} correct / {0,number} answer"})
    @Key("scoreMessage")
    String scoreMessage(@PluralCount int arg0, int arg1);

    @NotNull
    @DefaultMessage("{0,number} questions")
    @AlternateMessage({"one", "1 question"})
    @Key("questionsCount")
    String questionsCount(@PluralCount int questionsCount);

    @NotNull
    @DefaultMessage("{0,number} correct once")
    @Key("correctOnce")
    String correctOnce(int arg0);

    @NotNull
    @DefaultMessage("{0,number} answered once")
    @Key("answeredOnce")
    String answeredOnce(int arg0);

    @NotNull
    @DefaultMessage("Big-O Quiz: {0}")
    @Key("windowTitleQuiz")
    String windowTitleQuiz(String text);

    @NotNull
    @DefaultMessage("Big-O Quiz: {0}: Question: {1}")
    @Key("windowTitleQuestion")
    String windowTitleQuestion(String text0, String text1);

    @NotNull
    @DefaultMessage("Quiz : {0}")
    @Key("quizTitle")
    String quizTitle(String text);

    @NotNull
    @DefaultMessage("and {0,number} more")
    @AlternateMessage({"one", "and {0,number} more"})
    @Key("moreProblemQuestions")
    String moreProblemQuestions(@PluralCount int arg0);

    @NotNull
    @DefaultMessage("Please <a href=\"{0}\">sign in</a> to track your progress and identify problem questions.")
    @Key("pleaseSignIn")
    String pleaseSignIn(final String text);

    @NotNull
    @DefaultMessage("Username: {0}")
    @Key("username")
    String username(final String text);
}
