package com.murrayc.bigoquiz.client;

//This is not generated with the 18nCreator (for instance, via the maven-gwt-plugin's gwt:i18n goal
//using <i18nMessagesBundles> declarations.

/**
 * Interface to represent the messages contained in resource bundle:
 * /home/murrayc/checkout/github/gwt-bigoquiz/src/main/resources/com/murrayc/bigoquiz/client/ui/BigOQuizMessages.properties'.
 */
public interface BigOQuizMessages extends com.google.gwt.i18n.client.Messages {

    /**
     * Translated "Answered: {0,number}: , Correct: {1,number}".
     *
     * @return translated "Answered: {0,number}: , Correct: {1,number}"
     */
    @DefaultMessage("{1,number} correct / {0,number} answered")
    @Key("scoreMessage")
    String scoreMessage(int arg0, int arg1);

    @DefaultMessage("Big-O Algorithms Quiz : Question: {0}")
    @Key("windowTitle")
    String windowTitle(String text);
}
