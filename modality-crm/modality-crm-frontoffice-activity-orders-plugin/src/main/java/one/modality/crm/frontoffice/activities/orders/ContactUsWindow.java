package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.bootstrap.ModalityStyle;



public final class ContactUsWindow  {

    private final BorderPane dialogPane = new BorderPane();
    private TextField subjectTextField;
    private Button cancelButton;
    private Button sendButton;
    private TextArea messageArea;

    public void buildUI() {
        dialogPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        dialogPane.setMaxWidth(500);

        // Header
        VBox header = new VBox(5);
        header.setPadding(new Insets(24));
        header.setBackground(new Background(new BackgroundFill(Color.web("#2196F3"), new CornerRadii(12, 12, 0, 0, false), Insets.EMPTY)));
        Label titleLabel = Bootstrap.strong(Bootstrap.h3(I18nControls.newLabel("ContactUs")));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        Label subtitleLabel = Bootstrap.strong(I18nControls.newLabel("ContactUsPrompt"));
        subtitleLabel.setTextFill(Color.WHITE);
        subtitleLabel.setWrapText(true);
        header.getChildren().addAll(titleLabel, subtitleLabel);
        dialogPane.setTop(header);

        // Form
        VBox form = new VBox(24);
        form.setPadding(new Insets(32, 24, 24, 24));

        // Subject
        Label subjectLabel = Bootstrap.strong(I18nControls.newLabel(OrdersI18nKeys.Subject));
        subjectLabel.getStyleClass().add("form-label");

        subjectTextField = new TextField();
        I18nControls.bindI18nProperties(subjectTextField,OrdersI18nKeys.SubjectPlaceholder);
        subjectTextField.setMaxWidth(Double.MAX_VALUE);
        Label subjectCount = new Label("0/100");
        subjectCount.getStyleClass().add("char-count");
        VBox subjectGroup = new VBox(8, subjectLabel, subjectTextField, subjectCount);

        // Message
        Label messageLabel = Bootstrap.strong(I18nControls.newLabel(OrdersI18nKeys.Message));
        messageLabel.getStyleClass().add("form-label");
        messageArea = new TextArea();
        I18nControls.bindI18nProperties(messageArea, "MessagePlaceholder");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(5);
        Label messageCount = new Label("0/1000");
        messageCount.getStyleClass().add("char-count");
        VBox messageGroup = new VBox(8, messageLabel, messageArea, messageCount);

        form.getChildren().addAll(subjectGroup, messageGroup);
        dialogPane.setCenter(form);

        // Character counters logic
        final int subjectMaxLength = 100;
        subjectTextField.textProperty().addListener((obs, old, text) -> {
            if (text.length() > subjectMaxLength) {
                text = text.substring(0, subjectMaxLength);
                subjectTextField.setText(text);
            }
            subjectCount.setText(text.length() + "/" + subjectMaxLength);
        });

        final int messageMaxLength = 1000;
        messageArea.textProperty().addListener((obs, old, text) -> {
            if (text.length() > messageMaxLength) {
                text = text.substring(0, messageMaxLength);
                messageArea.setText(text);
            }
            messageCount.setText(text.length() + "/" + messageMaxLength);
        });

        // Buttons
        cancelButton = ModalityStyle.whiteButton(I18nControls.newButton(OrdersI18nKeys.Cancel));
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);
        sendButton = Bootstrap.primaryButton(I18nControls.newButton(OrdersI18nKeys.SendMessage));
        sendButton.setMinWidth(Region.USE_PREF_SIZE);

        HBox buttonGroup = new HBox(12, cancelButton, sendButton);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);
        buttonGroup.setPadding(new Insets(0, 24, 24, 24));
        dialogPane.setBottom(buttonGroup);
        // Dialog display

        // Validation
        sendButton.disableProperty().bind(
            Bindings.createBooleanBinding(() ->
                    subjectTextField.getText().trim().isEmpty() || messageArea.getText().trim().isEmpty(),
                subjectTextField.textProperty(),
                messageArea.textProperty()
            )
        );
    }

    public void displaySuccessMessage(int duration, Runnable onFinished) {
        // 1. Create the success layout
        Platform.runLater(()-> {
            VBox successPane = new VBox(20);
            successPane.setAlignment(Pos.CENTER);
            successPane.setPadding(new Insets(40, 24, 40, 24)); // Consistent padding

            // Icon (using a simple label with style)
            Label iconLabel = new Label("✓");
            iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

            // Message
            Label messageLabel = I18nControls.newLabel("MessageSentSuccessfully");
            messageLabel.setWrapText(true);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setStyle("-fx-font-size: 16px;");


            // Progress Bar
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(Double.MAX_VALUE); // Take full width of VBox

            successPane.getChildren().addAll(iconLabel, messageLabel, progressBar);

            // 2. Replace dialog content
            dialogPane.setCenter(successPane);
            dialogPane.setBottom(null); // Remove cancel/send buttons

            // 3. Animate progress bar and close dialog on finish
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(new Duration(duration), new KeyValue(progressBar.progressProperty(), 1))
            );
            timeline.setOnFinished(event -> onFinished.run());
            timeline.play();
        });
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public BorderPane getContainer() {
        return dialogPane;
    }

    public String getSubject() {
        return subjectTextField.getText();
    }

    public String getMessage() {
        return messageArea.getText();
    }
}
