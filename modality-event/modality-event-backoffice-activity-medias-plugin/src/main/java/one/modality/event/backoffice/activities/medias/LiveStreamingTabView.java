package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.SubmitChangesResult;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.result.EntityChanges;
import dev.webfx.stack.orm.entity.result.EntityChangesBuilder;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import one.modality.base.client.i18n.LabelTextField;
import one.modality.base.shared.entity.message.sender.ModalityEntityMessageSender;
import one.modality.base.shared.entities.Event;
import one.modality.event.client.event.fx.FXEvent;

/**
 * @author David Hello
 */
final class LiveStreamingTabView {

    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final EntityStore entityStore = EntityStore.create();
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private LabelTextField liveMessageTextField;
    private VBox mainVBox; // Reference to the main container for adding notifications
    private Label successNotificationLabel; // Success notification label

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.setPadding(new Insets(0, 0, 30, 0));
        Label title = I18nControls.newLabel(MediasI18nKeys.LiveStreamingTitle);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H2);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        int maxWith = 1000;
        mainVBox = new VBox(); // Store reference
        mainVBox.setSpacing(20);
        mainVBox.setAlignment(Pos.CENTER);
        mainVBox.setMaxWidth(maxWith);

        // Creating the success notification (initially hidden)
        createSuccessNotification();

        Label liveMessageLabel = I18nControls.newLabel(MediasI18nKeys.LiveInfoMessage);
        liveMessageLabel.setTextFill(Color.WHITE);
        liveMessageLabel.getStyleClass().add(Bootstrap.STRONG);

        liveMessageTextField = new LabelTextField(FXEvent.getEvent(), null, Event.livestreamMessageLabel, updateStore);
        liveMessageTextField.setMaxWidth(Integer.MAX_VALUE);
        FXProperties.runNowAndOnPropertyChange(() ->
                liveMessageTextField.reloadOnNewEntity(FXEvent.getEvent())
            , FXEvent.eventProperty());

        // Make the text field grow to fill available space
        HBox.setHgrow(liveMessageTextField.getView(), Priority.ALWAYS);

        Button publishMessageButton = Bootstrap.successButton(new Button(I18n.getI18nText(MediasI18nKeys.PublishMessage)));
        publishMessageButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());

        publishMessageButton.setOnAction(event ->
            updateStore.submitChanges()
                .onFailure(Console::log)
                .inUiThread()
                .onSuccess(result -> {
                    notifyFrontOfficeClientsAboutLivestreamMessageChanges(result);
                    // Show success notification
                    showSuccessNotification();
                }));

        Button removeMessageButton = Bootstrap.dangerButton(new Button(I18n.getI18nText(MediasI18nKeys.RemoveMessage)));
        removeMessageButton.setOnAction(event -> {
            Event currentEvent = updateStore.updateEntity(FXEvent.getEvent());
            one.modality.base.shared.entities.Label labelToDelete = currentEvent.getLivestreamMessageLabel();
            if (labelToDelete != null) {
                currentEvent.setLivestreamMessageLabel(null);
                updateStore.deleteEntity(labelToDelete);
                updateStore.submitChanges().onFailure(Console::log)
                    .inUiThread()
                    .onSuccess(result -> {
                        notifyFrontOfficeClientsAboutLivestreamMessageChanges(result);
                        liveMessageTextField.reloadOnNewEntity(currentEvent);
                        // Show success notification for removal
                        showSuccessNotification("Message successfully removed!");
                    });
            }
        });

        // Create a VBox for buttons to stack them vertically
        VBox buttonVBox = new VBox(10);
        buttonVBox.setAlignment(Pos.CENTER_RIGHT);
        buttonVBox.getChildren().addAll(publishMessageButton, removeMessageButton);

        HBox liveMessageHBox = new HBox(20, liveMessageTextField.getView(), buttonVBox);
        liveMessageHBox.setAlignment(Pos.CENTER_LEFT);
        VBox liveMessageVBox = new VBox(20, liveMessageLabel, liveMessageHBox);
        MonoPane liveMessageContainer = new MonoPane(liveMessageVBox);
        liveMessageContainer.setPadding(new Insets(15));
        liveMessageContainer.setBackground(new Background(new BackgroundFill(
            Color.web("0096D6"), new CornerRadii(10), Insets.EMPTY // Match border CornerRadii
        )));

        mainVBox.getChildren().add(liveMessageContainer);

        mainFrame.setCenter(mainVBox);
        BorderPane.setAlignment(mainVBox, Pos.CENTER);

        return Controls.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private void notifyFrontOfficeClientsAboutLivestreamMessageChanges(SubmitChangesResult result) {
        // Notifying the front-office clients of the possible changes made
        EntityChanges committedChanges = result.getCommittedChanges();
        ModalityEntityMessageSender.getFrontOfficeEntityMessageSender().publishEntityChanges(
            EntityChangesBuilder.create()
                .addFilteredEntityChanges(committedChanges, Event.class, Event.livestreamMessageLabel)
                .addFilteredEntityChanges(committedChanges, one.modality.base.shared.entities.Label.class, "en", "de", "fr", "es", "pt") // I18n.getSupportedLanguages().toArray() returns only "en", "fr" for some reasons TODO: fix this
                .build()
        );
    }

    /**
     * Creates the success notification component
     */
    private void createSuccessNotification() {
        successNotificationLabel = Bootstrap.strong(new Label("Message successfully published!"));
        successNotificationLabel.setTextFill(Color.WHITE);
        successNotificationLabel.setPadding(new Insets(15, 20, 15, 20));

        // Creating the container with a green background and rounded corners
        MonoPane notificationContainer = new MonoPane(successNotificationLabel);
        notificationContainer.setBackground(new Background(new BackgroundFill(
            Color.web("#28a745"), // Bootstrap success green
            new CornerRadii(8),
            Insets.EMPTY
        )));
        notificationContainer.setBorder(new Border(new BorderStroke(
            Color.web("#1e7e34"), // Darker green border
            BorderStrokeStyle.SOLID,
            new CornerRadii(8),
            new BorderWidths(1)
        )));

        // Initially hide the notification
        notificationContainer.setVisible(false);
        notificationContainer.setManaged(false);

        // Store reference for later use
        successNotificationLabel.setUserData(notificationContainer);
    }

    /**
     * Shows success notification with a default message
     */
    private void showSuccessNotification() {
        showSuccessNotification("Message successfully published!");
    }

    /**
     * Shows success notification with a custom message
     */
    private void showSuccessNotification(String message) {
        successNotificationLabel.setText(message);
        MonoPane container = (MonoPane) successNotificationLabel.getUserData();

        // Adding the container to `mainVBox` if not already added
        if (!mainVBox.getChildren().contains(container)) {
            mainVBox.getChildren().add(0, container); // Add at the top
        }

        // Show with fade-in animation
        container.setVisible(true);
        container.setManaged(true);
        container.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Auto-hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> hideSuccessNotification());
        pause.play();
    }

    /**
     * Hides the success notification with fade-out animation
     */
    private void hideSuccessNotification() {
        MonoPane container = (MonoPane) successNotificationLabel.getUserData();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            container.setVisible(false);
            container.setManaged(false);
        });
        fadeOut.play();
    }

    public void setActive(boolean b) {
        activeProperty.set(b);
    }
}