package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.panes.MonoClipPane;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.message.receiver.ModalityEntityMessageReceiver;
import one.modality.base.shared.entities.Event;

import java.util.function.Consumer;

/**
 * A class that can display a notification message pushed by the server on the livestream video overlay. The message is
 * displayed as a text that scrolls in a loop from right to left in a notification bar. The user can close the
 * notification bar using a close button before the team clears the message.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class LivestreamNotificationOverlay {

    private final HBox notificationContainer = new HBox(12);
    private final Text notificationText = new Text();

    public static void addNotificationOverlayToLivestreamPlayer(Player livestreamPlayer, Event event) {
        new LivestreamNotificationOverlay(livestreamPlayer, event);
    }

    private LivestreamNotificationOverlay(Player livestreamPlayer, Event event) {
        // Note that we use web CSS for the scroll text animation in the browser. This web animation completely resets
        // any translateX/Y possibly set by WebFX, which actually includes layoutX/Y. So it's important to set any
        // padding or margin not on the text itself but on the container.
        notificationContainer.setMaxHeight(Region.USE_PREF_SIZE);
        notificationContainer.setPadding(new Insets(8, 15, 8, 15));
        StackPane.setAlignment(notificationContainer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(notificationContainer, new Insets(0, 20, 60, 20));
        notificationContainer.setVisible(false);

        StackPane.setAlignment(notificationContainer, Pos.BOTTOM_CENTER);
        livestreamPlayer.getOverlayChildren().add(notificationContainer);

        // Push-notification management: we turn the published field into a property
        ModalityEntityMessageReceiver.getFrontOfficeEntityMessageReceiver().listenEntityChanges(event.getStore());
        ObjectProperty<one.modality.base.shared.entities.Label> liveMessageLabelProperty = EntityBindings.getForeignEntityProperty(event, Event.livestreamMessageLabel);

        // Consumer that gets called when address fields change
        Consumer<one.modality.base.shared.entities.Label> onLabelChange = labelEntity -> UiScheduler.runInUiThread(() ->
            showNotification(I18nEntities.bindTranslatedEntityToProperties(notificationText, labelEntity))
        );
        EntityBindings.onForeignFieldsChanged(onLabelChange, event, Event.livestreamMessageLabel, "en", "de", "fr", "es", "pt");

        I18nEntities.bindTranslatedEntityToProperties(notificationText, liveMessageLabelProperty);
        notificationText.setTextAlignment(TextAlignment.CENTER);

        // Listen for livestream message changes and show notifications
        FXProperties.runNowAndOnPropertyChange(liveMessageLabel -> {
            if (liveMessageLabel == null) {
                hideNotification();
            } else {
                showNotification(notificationText);
            }
        }, liveMessageLabelProperty);

        FXProperties.runOnPropertiesChange(() ->
            // The reason for deferring is to ensure that notificationText.prefWidth(-1) will return the correct value
            // (otherwise WebFX may not have mapped yet the text to the DOM)
            UiScheduler.scheduleDeferred(() -> {
                // Computing the "--scroll-text-duration" CSS property so that the text scroll speed is always the same
                // whatever the container width and text length.
                double containerWidth = notificationContainer.getWidth();
                double messageWidth = notificationText.prefWidth(-1); // total width of the text on a single line
                double totalScrollDistance = containerWidth + messageWidth;
                double totalScrollDuration = totalScrollDistance / 160; // in seconds - 160 is an empiric value
                if (UserAgent.isBrowser()) // We use CSS animation in browsers (smoother)
                    notificationText.setStyle("--scroll-text-duration:" + totalScrollDuration + "s");
                else // otherwise JavaFX programmatic animation
                    startJavaFxTextScrollAnimation(containerWidth, totalScrollDistance, totalScrollDuration);
            }), notificationContainer.widthProperty(), notificationText.textProperty());
    }

    private Timeline javaFxTextScrollTimeline;

    private void startJavaFxTextScrollAnimation(double containerWidth, double totalScrollDistance, double totalScrollDuration) {
        if (javaFxTextScrollTimeline != null)
            javaFxTextScrollTimeline.stop();
        notificationText.setTranslateX(containerWidth);
        javaFxTextScrollTimeline = Animations.animateProperty(notificationText.translateXProperty(), containerWidth - totalScrollDistance, Duration.seconds(totalScrollDuration), Interpolator.LINEAR);
        javaFxTextScrollTimeline.setOnFinished(e -> startJavaFxTextScrollAnimation(containerWidth, totalScrollDistance, totalScrollDuration));
    }

    // Method to show the notification message
    private void showNotification(Node notificationText) {
        notificationContainer.getStyleClass().setAll("notification-bar");

        // Icon
        Text iconText = new Text("⚠️");
        iconText.getStyleClass().add("notification-icon");

        // Text box with the message inside, but clipped if it's too long
        MonoClipPane notificationTextBox = new MonoClipPane(notificationText); // We clip the text (works for both web and JavaFX)
        notificationTextBox.setAlignment(Pos.TOP_LEFT); // The web CSS animation will reset any layoutX/Y anyway
        notificationTextBox.getStyleClass().add("notification-text-box");
        // Important to tell that it can be smaller than the text (for long texts)
        notificationTextBox.setMinWidth(0);
        // Important to tell that it should always fit the whole width of the notification bar (for short texts)
        notificationTextBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(notificationTextBox, Priority.ALWAYS);

        // Close button
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("notification-close");
        closeButton.setOnAction(e -> hideNotification());

        notificationContainer.getChildren().setAll(iconText, notificationTextBox, closeButton);

        Animations.fadeIn(notificationContainer, true);
    }

    // Method to hide notification with CSS-like animation
    private void hideNotification() {
        Animations.fadeOut(notificationContainer, true);
    }


}
