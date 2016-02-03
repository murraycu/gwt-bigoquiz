package com.murrayc.bigoquiz.client;

//This is not generated with the 18nCreator (for instance, via the maven-gwt-plugin's gwt:i18n goal
//using <i18nMessagesBundles> declarations.

import com.google.gwt.i18n.client.LocalizableResource;

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
    @DefaultMessage("{1,number} correct / {0,number} answers")
    @AlternateMessage({"one", "{1,number} correct / {0,number} answer"})
    @Key("scoreMessage")
    String scoreMessage(@PluralCount int arg0, int arg1);

    @DefaultMessage("{0,number} questions")
    @AlternateMessage({"one", "1 question"})
    @Key("questionsCount")
    String questionsCount(@PluralCount int questionsCount);

    @DefaultMessage("{0,number} correct once")
    @Key("correctOnce")
    String correctOnce(int arg0);

    @DefaultMessage("{0,number} answered once")
    @Key("answeredOnce")
    String answeredOnce(int arg0);

    @DefaultMessage("Big-O Algorithms Quiz : Question: {0}")
    @Key("windowTitle")
    String windowTitle(String text);

    @DefaultMessage("and {0,number} more")
    @AlternateMessage({"one", "and {0,number} more"})
    @Key("moreProblemQuestions")
    String moreProblemQuestions(@PluralCount int arg0);
}
