package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.bootstrap.ModalityStyle;

public final class ContactUsDialog extends BaseDialog {

    private TextField subjectTextField;
    private Button cancelButton;
    private Button sendButton;
    private TextArea messageArea;

    @Override
    public void buildUI() {
        // Header
        VBox header = createHeader(OrdersI18nKeys.ContactUs, OrdersI18nKeys.ContactUsPrompt);
        dialogPane.setTop(header);

        // Form
        VBox form = createForm();

        // Subject
        Label subjectLabel = Bootstrap.strong(I18nControls.newLabel(OrdersI18nKeys.Subject));
        subjectLabel.getStyleClass().add("form-label");

        subjectTextField = new TextField();
        I18nControls.bindI18nProperties(subjectTextField, OrdersI18nKeys.SubjectPlaceholder);
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
        messageArea.setPrefRowCount(7);
        Label messageCount = new Label("0/1000");
        messageCount.getStyleClass().add("char-count");
        VBox messageGroup = new VBox(8, messageLabel, messageArea, messageCount);

        form.getChildren().addAll(subjectGroup, messageGroup);
        dialogPane.setCenter(form);

        // Character counters logic
        setupCharacterCounters(subjectTextField, subjectCount, 100);
        setupCharacterCounters(messageArea, messageCount, 1000);

        // Buttons
        cancelButton = ModalityStyle.whiteButton(I18nControls.newButton(OrdersI18nKeys.Cancel));
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);
        sendButton = Bootstrap.primaryButton(I18nControls.newButton(OrdersI18nKeys.SendMessage));
        sendButton.setMinWidth(Region.USE_PREF_SIZE);

        HBox buttonGroup = createButtonGroup(cancelButton, sendButton);
        dialogPane.setBottom(buttonGroup);

        // Validation
        sendButton.disableProperty().bind(
            Bindings.createBooleanBinding(() ->
                    subjectTextField.getText().trim().isEmpty() || messageArea.getText().trim().isEmpty(),
                subjectTextField.textProperty(),
                messageArea.textProperty()
            )
        );
    }

    private void setupCharacterCounters(TextInputControl textControl, Label countLabel, int maxLength) {
        textControl.textProperty().addListener((obs, old, text) -> {
            if (text.length() > maxLength) {
                text = text.substring(0, maxLength);
                textControl.setText(text);
            }
            countLabel.setText(text.length() + "/" + maxLength);
        });
    }

    public void displaySuccessMessage(int duration, Runnable onFinished) {
        displaySuccessMessage(
            OrdersI18nKeys.MessageSentSuccessfully,
            OrdersI18nKeys.MessageSentSuccessfullyDetails,
            OrdersI18nKeys.ThisWindowWillCloseAutomatically,
            duration,
            onFinished
        );
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public String getSubject() {
        return subjectTextField.getText();
    }

    public String getMessage() {
        return messageArea.getText();
    }
}