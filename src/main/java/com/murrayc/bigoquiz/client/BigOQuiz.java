package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BigOQuiz implements EntryPoint {
  private LoginInfo loginInfo = null;
  private VerticalPanel loginPanel = new VerticalPanel();
  private Label loginLabel = new Label(
          "Please sign in to your Google Account to access the StockWatcher application.");
  private Anchor signInLink = new Anchor("Sign In");
  final Label nameLabel = new Label();
    final Label scoreLabel = new Label();


    /**
   * The message displayed to the user when the server cannot be reached or
   * returns an error.
   */
  private static final String SERVER_ERROR = "An error occurred while "
      + "attempting to contact the server. Please check your network "
      + "connection and try again.";

  /**
   * Create a remote service proxy to talk to the server-side Greeting service.
   */
  private final QuizServiceAsync quizService = GWT.create(QuizService.class);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // Check login status using login service.
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
      public void onFailure(final Throwable error) {
      }

      public void onSuccess(final LoginInfo result) {
        loginInfo = result;
        if(loginInfo.isLoggedIn()) {
          loadMainUI();
        } else {
          loadLogin();
        }
      }
    });

    loadMainUI();

      getAndShowScore();
  }

    private void getAndShowScore() {
        QuizServiceAsync quizService = GWT.create(QuizService.class);
        quizService.getUserProfile(new AsyncCallback<UserProfile>() {
          public void onFailure(final Throwable error) {
              nameLabel.setText("Error: Can't get username");
              scoreLabel.setText("0");
          }

          public void onSuccess(final UserProfile result) {
            nameLabel.setText(result.getName());

              //TODO: internationalization:
              scoreLabel.setText(String.valueOf(result.getCountCorrectAnswers()));
          }
        });
    }

    private void loadLogin() {
    // Assemble login panel.
    signInLink.setHref(loginInfo.getLoginUrl());
    loginPanel.add(loginLabel);
    loginPanel.add(signInLink);
    RootPanel.get("login").add(loginPanel);
  }

  private void loadMainUI() {
    final Button nextButton = new Button("Next Question");
    nameLabel.setText("<user>");
    final Label errorLabel = new Label();

    // We can add style names to widgets
    nextButton.addStyleName("nextButton");

    // Add the nameLabel and nextButton to the RootPanel
    // Use RootPanel.get() to get the entire body element
    RootPanel.get("nameFieldContainer").add(nameLabel);
      RootPanel.get("scoreFieldContainer").add(scoreLabel);
    RootPanel.get("sendButtonContainer").add(nextButton);
    RootPanel.get("errorLabelContainer").add(errorLabel);

    // Create the popup dialog box
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Remote Procedure Call");
    dialogBox.setAnimationEnabled(true);
    final Button closeButton = new Button("Close");
    // We can set the id of a widget by accessing its Element
    closeButton.getElement().setId("closeButton");
    final Label textToServerLabel = new Label();
    final HTML serverResponseLabel = new HTML();
    VerticalPanel dialogVPanel = new VerticalPanel();
    dialogVPanel.addStyleName("dialogVPanel");
    dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
    dialogVPanel.add(textToServerLabel);
    dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
    dialogVPanel.add(serverResponseLabel);
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
    dialogVPanel.add(closeButton);
    dialogBox.setWidget(dialogVPanel);

    // Add a handler to close the DialogBox
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        nextButton.setEnabled(true);
        nextButton.setFocus(true);
      }
    });

    // Create a handler for the nextButton and nameLabel
    class MyHandler implements ClickHandler {
      /**
       * Fired when the user clicks on the nextButton.
       */
      public void onClick(ClickEvent event) {
        increaseScore();
      }

      /**
       * Send the name from the nameLabel to the server and wait for a response.
       */
      private void increaseScore() {
          quizService.increaseScore(new AsyncCallback<Void>() {
              public void onFailure(final Throwable caught) {
                // Show the RPC error message to the user
                dialogBox.setText("Remote Procedure Call - Failure");
                serverResponseLabel.addStyleName("serverResponseLabelError");
                serverResponseLabel.setHTML(SERVER_ERROR);
                dialogBox.center();
                closeButton.setFocus(true);
              }
    
              public void onSuccess(final Void result) {
                  getAndShowScore();
              }
        });
      }
    }

    // Add a handler for the button:
    MyHandler handler = new MyHandler();
    nextButton.addClickHandler(handler);
  }
}
