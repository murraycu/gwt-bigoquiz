package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.Quiz;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoader {
    private static final String NODE_ROOT = "quiz";
    private static final String NODE_SECTION = "section";
    private static final String ATTR_ANSWERS_AS_CHOICES = "answers_as_choices";
    private static final String NODE_SUB_SECTION = "subsection";

    private static final String NODE_QUESTION = "question";
    private static final String ATTR_ID = "id";
    private static final String NODE_TITLE = "title";
    private static final String NODE_LINK = "link";
    private static final String NODE_TEXT = "text";
    private static final String NODE_ANSWER = "answer";
    private static final String NODE_CHOICES = "choices";
    private static final String NODE_CHOICE = "choice";
    private static final String NODE_DEFAULT_CHOICES = "default_choices";

    public static class QuizLoaderException extends Exception {
        public QuizLoaderException(final String message) {
            super(message);
        }

        public QuizLoaderException(final String message, final Exception cause) {
            super(message, cause);
        }
    }

    public static Quiz loadQuiz(final InputStream is) throws QuizLoaderException {
        @NotNull final Quiz result = new Quiz();

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (@NotNull final ParserConfigurationException e) {
            throw new QuizLoaderException("XML parsing failed while creating DocumentBuilder.", e);
        }

        org.w3c.dom.Document xmlDocument;
        try {
            xmlDocument = documentBuilder.parse(is);
        } catch (@NotNull final SAXException e) {
            throw new QuizLoaderException("XML parsing failed.", e);
        } catch (@NotNull final IOException e) {
            throw new QuizLoaderException("XML parsing failed with IOException.", e);
        }

        final Element rootNode = xmlDocument.getDocumentElement();
        if (!StringUtils.equals(rootNode.getNodeName(), NODE_ROOT)) {
            throw new QuizLoaderException("Unexpected XML root node name found: " + rootNode.getNodeName());
        }

        @Nullable final String quizId = rootNode.getAttribute(ATTR_ID);
        if (StringUtils.isEmpty(quizId)) {
            throw new QuizLoaderException("No quiz ID found.");
        }
        result.setId(quizId);

        @Nullable final String quizTitle = getTitleNodeText(rootNode);
        if (StringUtils.isEmpty(quizId)) {
            throw new QuizLoaderException("No quiz title found.");
        }
        result.setTitle(quizTitle);

        //Sections:
        @NotNull final List<Node> listSectionNodes = getChildrenByTagName(rootNode, NODE_SECTION);
        for (final Node sectionNode : listSectionNodes) {
            @NotNull final Element sectionElement = (Element) sectionNode;

            final String sectionId = sectionElement.getAttribute(ATTR_ID);
            @Nullable final String sectionTitle = getTitleNodeText(sectionElement);

            //Default choices:
            @Nullable List<String> defaultChoices = null;
            @Nullable final Element elementChoices = getElementByName(sectionElement, NODE_DEFAULT_CHOICES);
            if (elementChoices != null) {
                defaultChoices = loadChoices(elementChoices);
            }

            final boolean useAnswersAsChoices = getAttributeAsBoolean(sectionElement, ATTR_ANSWERS_AS_CHOICES);

            result.addSection(sectionId, sectionTitle, defaultChoices);

            int questionsCount = 0;
            @NotNull final List<Node> listSubSectionNodes = getChildrenByTagName(sectionElement, NODE_SUB_SECTION);
            for (final Node subSectionNode : listSubSectionNodes) {
                @NotNull final Element subSectionElement = (Element) subSectionNode;
                final String subSectionId = subSectionElement.getAttribute(ATTR_ID);
                @Nullable final String subSectionTitle = getTitleNodeText(subSectionElement);
                @Nullable final String subSectionLink= getLinkNodeText(subSectionElement);

                result.addSubSection(sectionId, subSectionId, subSectionTitle, subSectionLink);

                //Questions:
                questionsCount += addChildQuestions(result, sectionId, subSectionId, defaultChoices, subSectionElement);
            }

            //Add any Questions that are not in a subsection:
            questionsCount += addChildQuestions(result, sectionId, null, defaultChoices, sectionElement);

            result.setSectionQuestionsCount(sectionId, questionsCount);

            if (useAnswersAsChoices) {
                result.setSectionDefaultChoicesFromAnswers(sectionId);
            }
        }

        return result;
    }

    private static boolean getAttributeAsBoolean(final Element element, final String attribute) {
        final String str = element.getAttribute(attribute);
        return StringUtils.equals(str, "true");
    }

    @Nullable
    private static String getTitleNodeText(@NotNull final Element sectionElement) {
        @Nullable String sectionTitle = null;
        @Nullable final Element sectionTitleElement = getElementByName(sectionElement, NODE_TITLE);
        if (sectionTitleElement != null) {
            sectionTitle = sectionTitleElement.getTextContent();
        }
        return sectionTitle;
    }

    @Nullable
    private static String getLinkNodeText(@NotNull final Element sectionElement) {
        @Nullable String sectionTitle = null;
        @Nullable final Element sectionTitleElement = getElementByName(sectionElement, NODE_LINK);
        if (sectionTitleElement != null) {
            sectionTitle = sectionTitleElement.getTextContent();
        }
        return sectionTitle;
    }

    private static int addChildQuestions(@NotNull final Quiz quiz, final String sectionId, final String subSectionId, final List<String> defaultChoices, @NotNull final Element parentElement) throws QuizLoaderException {
        int result = 0;

        @NotNull final List<Node> listQuestionNodes = getChildrenByTagName(parentElement, NODE_QUESTION);
        for (final Node questionNode : listQuestionNodes) {
            if (!(questionNode instanceof Element)) {
                continue;
            }

            @NotNull final Element element = (Element) questionNode;
            @Nullable final QuestionAndAnswer questionAndAnswer = loadQuestionNode(element, sectionId, subSectionId, defaultChoices);
            if (questionAndAnswer != null) {
                //warn about duplicates:
                if (quiz.contains(questionAndAnswer.getId())) {
                    Log.error("QuizLoader: Duplicate question ID: " + questionAndAnswer.getId());
                } else {
                    quiz.addQuestion(sectionId, questionAndAnswer);
                    result += 1;
                }
            }
        }

        return result;
    }

    private static QuestionAndAnswer loadQuestionNode(@NotNull final Element element, final String sectionID, final String subSectionId, final List<String> defaultChoices) throws QuizLoaderException {
        final String id = element.getAttribute(ATTR_ID);
        if (StringUtils.isEmpty(id)) {
            throw new QuizLoaderException("loadQuestionNode(): Missing ID.");
        }

        @Nullable final Element textElement = getElementByName(element, NODE_TEXT);
        if (textElement == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing text.");
        }

        @Nullable final Element answerElement = getElementByName(element, NODE_ANSWER);
        if (answerElement == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing answer.");
        }

        final String questionText = textElement.getTextContent();
        if (questionText == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing text content.");
        }

        final String answerText = answerElement.getTextContent();
        if (answerText == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing answer content.");
        }

        @Nullable List<String> choices = null;
        @Nullable final Element elementChoices = getElementByName(element, NODE_CHOICES);
        if (elementChoices != null) {
            choices = loadChoices(elementChoices);
        }

        if (choices == null || choices.isEmpty()) {
            choices = defaultChoices;
        }

        if (choices != null && !choices.contains(answerText)) {
            throw new QuizLoaderException("QuizLoader.loadQuestionNode(): answer is not in the choices: questionId: " + id);
        }

        return new QuestionAndAnswer(id, sectionID, subSectionId, questionText, answerText, choices);
    }

    @NotNull
    private static List<String> loadChoices(@NotNull final Element elementChoices) {
        @NotNull List<String> choices = new ArrayList<>();

        @NotNull final List<Node> listChoices = getChildrenByTagName(elementChoices, NODE_CHOICE);
        for (final Node choiceNode : listChoices) {
            if (!(choiceNode instanceof Element)) {
                continue;
            }

            @NotNull final Element elementChoice = (Element) choiceNode;
            final String choice = elementChoice.getTextContent();
            if (!StringUtils.isEmpty(choice)) {
                choices.add(choice);
            }
        }

        return choices;
    }

    private static Element getElementByName(@NotNull final Element parentElement, final String tagName) {
        @NotNull final List<Node> listNodes = getChildrenByTagName(parentElement, tagName);
        if (listNodes == null) {
            return null;
        }

        if (listNodes.size() == 0) {
            return null;
        }

        return (Element) listNodes.get(0);
    }

    /**
     * getElementsByTagName() is recursive, but we do not want that.
     *
     * @param
     * @return
     */
    @NotNull
    private static List<Node> getChildrenByTagName(@NotNull final Element parentNode, final String tagName) {
        @NotNull final List<Node> result = new ArrayList<>();

        final NodeList list = parentNode.getElementsByTagName(tagName);
        final int num = list.getLength();
        for (int i = 0; i < num; i++) {
            final Node node = list.item(i);
            if (node == null) {
                continue;
            }

            final Node itemParentNode = node.getParentNode();
            if (itemParentNode.equals(parentNode)) {
                result.add(node);
            }
        }

        return result;
    }


}
