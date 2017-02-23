package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Question;
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
import java.util.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoader {
    private static final String NODE_ROOT = "quiz";
    private static final String NODE_SECTION = "section";
    private static final String ATTR_ANSWERS_AS_CHOICES = "answers_as_choices";
    private static final String ATTR_AND_REVERSE = "and_reverse";
    private static final String NODE_SUB_SECTION = "subsection";

    private static final String NODE_QUESTION = "question";
    private static final String ATTR_ID = "id";
    private static final String ATTR_PRIVATE = "private";
    private static final String ATTR_USES_MATHML = "uses_mathml";
    private static final String ATTR_IS_HTML = "is_html";
    private static final String NODE_TITLE = "title";
    private static final String NODE_LINK = "link";
    private static final String NODE_TEXT = "text";
    private static final String NODE_ANSWER = "answer";
    private static final String NODE_CHOICES = "choices";
    private static final String NODE_CHOICE = "choice";
    private static final String NODE_DEFAULT_CHOICES = "default_choices";
    private static final String NODE_NOTE = "note";
    private static final String NODE_VIDEO_URL = "video_url";
    private static final String NODE_CODE_URL = "code_url"; // For source code examples.
    private static final int MAX_CHOICES_FROM_ANSWERS = 6;

    public static void setSectionDefaultChoicesFromAnswers(final Quiz quiz, final String sectionId) {
        final List<QuestionAndAnswer> sectionList = quiz.getQuestionsForSection(sectionId);

        setQuestionsChoicesFromAnswers(sectionList);
    }

    private static void setQuestionsChoicesFromAnswers(final List<QuestionAndAnswer> questions) {
        //Use a set to avoid duplicates:
        final Set<Question.Text> choicesSet = new HashSet<>();
        for (final QuestionAndAnswer question : questions) {
            choicesSet.add(question.getAnswer());
        }

        final List<Question.Text> choices = new ArrayList<>(choicesSet);
        final boolean too_many_choices = choices.size() > MAX_CHOICES_FROM_ANSWERS;

        for (final QuestionAndAnswer questionAndAnswer : questions) {
            final Question question = questionAndAnswer.getQuestion();

            if (!too_many_choices) {
                question.setChoices(choices);
            } else {
                //Reduce the list:
                final List<Question.Text> less_choices = reduce_choices(choices, questionAndAnswer.getAnswer());
                question.setChoices(less_choices);
            }
        }
    }

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

        final boolean isPrivate = getAttributeAsBoolean(rootNode, ATTR_PRIVATE);
        result.setIsPrivate(isPrivate);

        final boolean usesMathML = getAttributeAsBoolean(rootNode, ATTR_USES_MATHML);
        result.setUsesMathML(usesMathML);

        //Sections:
        @NotNull final List<Node> listSectionNodes = getChildrenByTagName(rootNode, NODE_SECTION);
        if (listSectionNodes.isEmpty()) {
            // Add a virtual section, by using the root node,
            // so we have somewhere to put the questions.
            // This lets a quiz have just questions with no sections.
            // The generated section will have the same id and title as the quiz itself.
            listSectionNodes.add(rootNode);
        }

        for (final Node sectionNode : listSectionNodes) {
            @NotNull final Element sectionElement = (Element) sectionNode;

            loadSectionNode(result, sectionElement);
        }

        return result;
    }

    private static void loadSectionNode(final Quiz result, final Element sectionElement) throws QuizLoaderException {
        loadSectionNode(result, sectionElement, false);

        final boolean andReverse = getAttributeAsBoolean(sectionElement, ATTR_AND_REVERSE);
        if (andReverse) {
            loadSectionNode(result, sectionElement, true);
        }
    }

    private static void loadSectionNode(final Quiz result, final Element sectionElement, boolean reverse) throws QuizLoaderException {
        String sectionId = sectionElement.getAttribute(ATTR_ID);
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("loadSectionNode: sectionId is null.");
            return;
        }

        @Nullable String sectionTitle = getTitleNodeText(sectionElement);
        if (reverse) {
            sectionId = "reverse-" + sectionId;
            sectionTitle = "Reverse: " + sectionTitle;
        }

        @Nullable String sectionLink = getLinkNodeText(sectionElement);

        //Default choices:
        @Nullable List<Question.Text> defaultChoices = null;
        @Nullable final Element elementChoices = getElementByName(sectionElement, NODE_DEFAULT_CHOICES);
        if (elementChoices != null) {
            defaultChoices = loadChoices(elementChoices);
        }

        final boolean useAnswersAsChoices = getAttributeAsBoolean(sectionElement, ATTR_ANSWERS_AS_CHOICES);

        result.addSection(sectionId, sectionTitle, sectionLink, defaultChoices);

        int questionsCount = 0;
        @NotNull final List<Node> listSubSectionNodes = getChildrenByTagName(sectionElement, NODE_SUB_SECTION);
        for (final Node subSectionNode : listSubSectionNodes) {
            @NotNull final Element subSectionElement = (Element) subSectionNode;
            final String subSectionId = subSectionElement.getAttribute(ATTR_ID);
            @Nullable final String subSectionTitle = getTitleNodeText(subSectionElement);
            @Nullable final String subSectionLink= getLinkNodeText(subSectionElement);

            //Don't use subsection answers as choices if the parent section wants answers-as-choices.
            //In that case, all questions will instead share answers from all sub-sections.
            final boolean subSectionUseAnswersAsChoices = getAttributeAsBoolean(subSectionElement, ATTR_ANSWERS_AS_CHOICES) &&
                    !useAnswersAsChoices;

            result.addSubSection(sectionId, subSectionId, subSectionTitle, subSectionLink);

            //Questions:
            questionsCount += addChildQuestions(result, sectionId, subSectionId, defaultChoices, subSectionElement, reverse, subSectionUseAnswersAsChoices);
        }

        //Add any Questions that are not in a subsection:
        questionsCount += addChildQuestions(result, sectionId, null, defaultChoices, sectionElement, reverse, false);

        result.setSectionQuestionsCount(sectionId, questionsCount);

        //Make sure that we set sub-section choices from the answers from all questions in the whole section:
        if (useAnswersAsChoices) {
            setSectionDefaultChoicesFromAnswers(result, sectionId);
        }

    }

    private static boolean getAttributeAsBoolean(final Element element, final String attribute) {
        final String str = element.getAttribute(attribute);
        return StringUtils.equals(str, "true");
    }

    @Nullable
    private static String getTitleNodeText(@NotNull final Element sectionElement) {
        return getNodeTextContent(sectionElement, NODE_TITLE);
    }

    @Nullable
    private static String getLinkNodeText(@NotNull final Element parentElement) {
        return getNodeTextContent(parentElement, NODE_LINK);

    }

    private static int addChildQuestions(@NotNull final Quiz quiz, final String sectionId, final String subSectionId,
                                         final List<Question.Text> defaultChoices, @NotNull final Element parentElement, boolean reverse, boolean useAnswersAsChoices) throws QuizLoaderException {
        int result = 0;

        // We only use this if using answers as choices.
        final List<QuestionAndAnswer> questions = new ArrayList<>();

        @NotNull final List<Node> listQuestionNodes = getChildrenByTagName(parentElement, NODE_QUESTION);
        for (final Node questionNode : listQuestionNodes) {
            if (!(questionNode instanceof Element)) {
                continue;
            }

            @NotNull final Element element = (Element) questionNode;
            @Nullable final QuestionAndAnswer questionAndAnswer = loadQuestionNode(element, sectionId, subSectionId, defaultChoices, reverse);
            if (questionAndAnswer != null) {
                //warn about duplicates:
                if (quiz.contains(questionAndAnswer.getId())) {
                    Log.error("QuizLoader: Duplicate question ID: " + questionAndAnswer.getId());
                } else {
                    quiz.addQuestion(sectionId, questionAndAnswer);
                    result += 1;

                    if (useAnswersAsChoices) {
                        questions.add(questionAndAnswer);
                    }
                }
            }
        }

        if (useAnswersAsChoices) {
            setQuestionsChoicesFromAnswers(questions);
        }

        return result;
    }

    private static QuestionAndAnswer loadQuestionNode(@NotNull final Element element, final String sectionID,
                                                      final String subSectionId, final List<Question.Text> defaultChoices,
                                                      boolean reverse) throws QuizLoaderException {
        String id = element.getAttribute(ATTR_ID);
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

        boolean questionTextIsHtml = getAttributeAsBoolean(textElement, ATTR_IS_HTML);
        String questionText = textElement.getTextContent();
        if (questionText == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing text content.");
        }

        //This is optional:
        final String questionLink = getLinkNodeText(element);

        boolean answerTextIsHtml = getAttributeAsBoolean(answerElement, ATTR_IS_HTML);
        String answerText = answerElement.getTextContent();
        if (answerText == null) {
            throw new QuizLoaderException("loadQuestionNode(): Missing answer content.");
        }

        @Nullable List<Question.Text> choices = null;
        @Nullable final Element elementChoices = getElementByName(element, NODE_CHOICES);
        if (elementChoices != null) {
            choices = loadChoices(elementChoices);
        }

        if (choices == null || choices.isEmpty()) {
            choices = defaultChoices;
        }

        if (choices != null && !choices.contains(new Question.Text(answerText, false))) {
            throw new QuizLoaderException("QuizLoader.loadQuestionNode(): answer is not in the choices: questionId: " + id);
        }

        //These are optional:
        final String noteText = getNodeTextContent(element, NODE_NOTE);
        final String videoUrl = getNodeTextContent(element, NODE_VIDEO_URL);
        final String codeUrl = getNodeTextContent(element, NODE_CODE_URL);

        if (reverse) {
            //Swap the question and answer text:
            final String temp = questionText;
            questionText = answerText;
            answerText = temp;

            final boolean tempIsHtml = questionTextIsHtml;
            questionTextIsHtml = answerTextIsHtml;
            answerTextIsHtml = questionTextIsHtml;

            id = "reverse-" + questionText; //Otherwise the id in the URL will show the answer.
        }

        return new QuestionAndAnswer(id, sectionID, subSectionId, new Question.Text(questionText, questionTextIsHtml),
                questionLink, new Question.Text(answerText, answerTextIsHtml), choices, noteText, videoUrl, codeUrl);
    }

    @Nullable
    private static String getNodeTextContent(final @NotNull Element parentElement, final String tagName) {
        String result = null;
        @Nullable final Element element = getElementByName(parentElement, tagName);
        if (element != null) {
            result = element.getTextContent();
        }
        return result;
    }

    private static <T> void swap(T a, T b) {

    }

    @NotNull
    private static List<Question.Text> loadChoices(@NotNull final Element elementChoices) {
        @NotNull List<Question.Text> choices = new ArrayList<>();

        @NotNull final List<Node> listChoices = getChildrenByTagName(elementChoices, NODE_CHOICE);
        for (final Node choiceNode : listChoices) {
            if (!(choiceNode instanceof Element)) {
                continue;
            }

            @NotNull final Element elementChoice = (Element) choiceNode;
            boolean choiceIsHtml = getAttributeAsBoolean(elementChoice, ATTR_IS_HTML);
            final String choice = elementChoice.getTextContent();
            if (!StringUtils.isEmpty(choice)) {
                choices.add(new Question.Text(choice, choiceIsHtml));
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

    private static List<Question.Text> reduce_choices(final List<Question.Text> choices, final Question.Text answer) {
        //TODO: The shuffling is inefficient.
        List<Question.Text> result = new ArrayList<>(choices);
        Collections.shuffle(result);
        final int answerIndex = result.indexOf(answer);
        if (answerIndex == -1) {
            Log.error("reduce_choices(): choices did not contain the answer.");
            return null;
        }

        if (answerIndex >= MAX_CHOICES_FROM_ANSWERS) {
            result = result.subList(0, MAX_CHOICES_FROM_ANSWERS - 1);
            result.add(answer);
            Collections.shuffle(result);
        } else {
            result = result.subList(0, MAX_CHOICES_FROM_ANSWERS);
        }

        //We put this in a new ArrayList because subList() returns an ArrayList.SubList, which GWT cannot serialize.
        return new ArrayList<>(result);
    }
}
