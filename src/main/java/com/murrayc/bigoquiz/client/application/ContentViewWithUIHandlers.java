package com.murrayc.bigoquiz.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 2/17/16.
 */
public class ContentViewWithUIHandlers<C extends UiHandlers> extends ViewWithUiHandlers<C>
        implements ContentView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    protected final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    @NotNull
    protected final Panel parentPanel = new FlowPanel();
    @NotNull
    protected final Panel mainPanel = new FlowPanel();
    @NotNull
    private final HeadingElement titleHeading;
    @NotNull
    private final Label titleLabel = new InlineLabel();
    @NotNull
    private final HeadingElement secondaryTitleHeading;
    @NotNull
    private final Label secondaryTitleLabel = new InlineLabel();
    private final Label labelLoading = Utils.createServerLoadingLabel(constants);
    private final Label labelError = Utils.createServerErrorLabel(constants);

    private boolean hasMathMLSupport = false;

    protected ContentViewWithUIHandlers() {
        parentPanel.addStyleName("parent-content-panel");
        mainPanel.addStyleName("content-panel");
        parentPanel.add(mainPanel);

        titleLabel.addStyleName("page-title-label");
        titleHeading = Utils.addHeaderToPanel(2, mainPanel, titleLabel);
        setHeadingVisible(false);

        secondaryTitleLabel.addStyleName("page-secondary-title-label");
        secondaryTitleHeading = Utils.addHeaderToPanel(2, mainPanel, secondaryTitleLabel);
        setSecondaryHeadingVisible(false);

        mainPanel.add(labelLoading);
        setLoadingLabelVisible(false);
        mainPanel.add(labelError);
        setErrorLabelVisible(false);

        initWidget(parentPanel);
    }

    protected void setTitle(final String title) {
        titleLabel.setText(title);
        setHeadingVisible(!StringUtils.isEmpty(title));

        //A good default:
        Window.setTitle(title);
    }

    protected void setSecondaryTitle(final String title) {
        secondaryTitleLabel.setText(title);
        setSecondaryHeadingVisible(!StringUtils.isEmpty(title));
    }

    @Override
    public void setLoadingLabelVisible(boolean visible) {
        labelLoading.setVisible(visible);
    }

    private void setErrorLabel(final String message) {
        labelError.setText(message);
    }

    protected void setErrorLabelVisible(boolean visible) {
        labelError.setVisible(visible);
    }

    private void setHeadingVisible(boolean visible) {
        titleHeading.getStyle().setProperty("display", visible ? "block" : "none");
    }

    private void setSecondaryHeadingVisible(boolean visible) {
        secondaryTitleHeading.getStyle().setProperty("display", visible ? "block" : "none");
    }

    @Override
    public void setServerFailed() {
        setErrorLabel(constants.errorNoServer());
        setErrorLabelVisible(true);
    }

    @Override
    public void setServerFailedUnknownQuiz() {
        setErrorLabel(constants.errorUnknownQuiz());
        setErrorLabelVisible(true);
    }

    protected void useAndReloadMathJax() {
        reloadMathJax();

        /* TODO: Make this work, instead of loading MathJax JS in every page, even when we don't use it.
         *  This error currently appears in the browser console:
         * SEVERE: (ReferenceError) : MathJax is not definedcom.google.gwt.core.client.JavaScriptException: (ReferenceError) : MathJax is not defined
         */
        /*
        if (!hasMathMLSupport) {
            ScriptInjector.fromUrl("https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-MML-AM_CHTML").
                    setWindow(ScriptInjector.TOP_WINDOW).
                    setCallback(
                        new Callback<Void, Exception>() {
                            @Override
                            public void onFailure(final Exception reason) {
                                Log.fatal("Script load failed.");
                            }

                            @Override
                            public void onSuccess(final Void result) {
                                final String scriptBody =
                                        "function reloadMathJax() {\n" +
                                                "    MathJax.Hub.Queue([\"Typeset\", MathJax.Hub]);\n" +
                                                " }";
                                ScriptInjector.fromString(scriptBody).inject();

                                reloadMathJax();

                                hasMathMLSupport = true;
                            }
                        }).inject();
        } else {
            reloadMathJax();
        }
        */
    }

    public static final native void reloadMathJax()/*-{
        $wnd.reloadMathJax();
        //$wnd.MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }-*/;
}
