package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import org.apache.commons.lang3.StringUtils;
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
    private static final String NODE_QUESTION = "question";
    private static final String ATTR_ID = "id";
    private static final String ATTR_TITLE = "title";
    private static final String NODE_TEXT = "text";
    private static final String NODE_ANSWER = "answer";
    private static final String NODE_CHOICES = "choices";
    private static final String NODE_CHOICE = "choice";
    private static final String NODE_DEFAULT_CHOICES = "default_choices";


    public static Quiz loadQuiz(final InputStream is) {
        final Quiz result = new Quiz();

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        org.w3c.dom.Document xmlDocument;
        try {
            xmlDocument = documentBuilder.parse(is);
        } catch (final SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        final Element rootNode = xmlDocument.getDocumentElement();
        if (!StringUtils.equals(rootNode.getNodeName(), NODE_ROOT)) {
            Log.error("Unexpected XML root node name found: " + rootNode.getNodeName());
            return null;
        }

        //Sections:
        final List<Node> listSectionNodes = getChildrenByTagName(rootNode, NODE_SECTION);
        for (final Node sectionNode : listSectionNodes) {
            final Element sectionElement = (Element) sectionNode;

            final String sectionId = sectionElement.getAttribute(ATTR_ID);
            final String sectionTitle = sectionElement.getAttribute(ATTR_TITLE);

            //Default choices:
            List<String> defaultChoices = null;
            final Element elementChoices = getElementByName(sectionElement, NODE_DEFAULT_CHOICES);
            if (elementChoices != null) {
                defaultChoices = loadChoices(elementChoices);
            }

            result.addSection(sectionId, sectionTitle, defaultChoices);

            //Questions:
            final List<Node> listQuestionNodes = getChildrenByTagName(sectionElement, NODE_QUESTION);
            for (final Node questionNode : listQuestionNodes) {
                if (!(questionNode instanceof Element)) {
                    continue;
                }

                final Element element = (Element) questionNode;
                final QuestionAndAnswer questionAndAnswer = loadQuestionNode(element, sectionId, defaultChoices);
                if (questionAndAnswer != null) {
                    //warn about duplicates:
                    if (result.contains(questionAndAnswer.getId())) {
                        Log.error("QuizLoader: Duplicate question ID: " + questionAndAnswer.getId());
                    } else {
                        result.addQuestion(sectionId, questionAndAnswer);
                    }
                }
            }
        }

        return result;
    }

    private static QuestionAndAnswer loadQuestionNode(final Element element, final String sectionID, final List<String> defaultChoices) {
        final String id = element.getAttribute(ATTR_ID);
        if (StringUtils.isEmpty(id)) {
            return null;
        }

        final Element textElement = getElementByName(element, NODE_TEXT);
        if (textElement == null) {
            return null;
        }

        final Element answerElement = getElementByName(element, NODE_ANSWER);
        if (answerElement == null) {
            return null;
        }

        final String questionText = textElement.getTextContent();
        if (questionText == null) {
            return null;
        }

        final String answerText = answerElement.getTextContent();
        if (answerText == null) {
            return null;
        }

        List<String> choices = null;
        final Element elementChoices = getElementByName(element, NODE_CHOICES);
        if (elementChoices != null) {
            choices = loadChoices(elementChoices);
        }

        if (choices == null || choices.isEmpty()) {
            choices = defaultChoices;
        }

        return new QuestionAndAnswer(id, sectionID, questionText, answerText, choices);
    }

    private static List<String> loadChoices(final Element elementChoices) {
        List<String> choices = new ArrayList<>();

        final List<Node> listChoices = getChildrenByTagName(elementChoices, NODE_CHOICE);
        for (final Node choiceNode : listChoices) {
            if (!(choiceNode instanceof Element)) {
                continue;
            }

            final Element elementChoice = (Element) choiceNode;
            final String choice = elementChoice.getTextContent();
            if (!StringUtils.isEmpty(choice)) {
                choices.add(choice);
            }
        }

        return choices;
    }

    private static Element getElementByName(final Element parentElement, final String tagName) {
        final List<Node> listNodes = getChildrenByTagName(parentElement, tagName);
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
    private static List<Node> getChildrenByTagName(final Element parentNode, final String tagName) {
        final List<Node> result = new ArrayList<>();

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
