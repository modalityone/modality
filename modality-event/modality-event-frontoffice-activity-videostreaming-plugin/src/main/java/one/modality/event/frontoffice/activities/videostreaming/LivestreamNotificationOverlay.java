package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.player.Player;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.messaging.ModalityMessaging;
import one.modality.base.shared.entities.Event;

import java.util.function.Consumer;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class LivestreamNotificationOverlay {

    private final Label livestreamMessageLabel = new Label();
    private final HBox notificationContainer = new HBox(12);
    private Unregisterable unregisterable;

    public static void addNotificationOverlayToLivestreamPlayer(Player livestreamPlayer, Event event) {
        new LivestreamNotificationOverlay(livestreamPlayer, event);
    }

    private LivestreamNotificationOverlay(Player livestreamPlayer, Event event) {
        notificationContainer.setMaxHeight(60);
        notificationContainer.setPrefHeight(60);
        notificationContainer.setPadding(new Insets(0, 15, 0, 15));
        StackPane.setAlignment(notificationContainer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(notificationContainer, new Insets(0, 20, 60, 20));
        notificationContainer.setAlignment(Pos.CENTER);
        notificationContainer.setVisible(false);

        StackPane.setAlignment(notificationContainer, Pos.BOTTOM_CENTER);
        livestreamPlayer.getOverlayChildren().add(notificationContainer);

        // Push-notification management: we turn the published field into a property
        ModalityMessaging.getFrontOfficeEntityMessaging().listenEntityChanges(event.getStore());
        ObjectProperty<one.modality.base.shared.entities.Label> liveMessageLabelProperty = EntityBindings.getForeignEntityProperty(event, Event.livestreamMessageLabel);

        // Consumer that gets called when address fields change
        Consumer<one.modality.base.shared.entities.Label> onLabelChange = (labelEntity) -> Platform.runLater(() -> {
            I18nEntities.bindExpressionProperties(livestreamMessageLabel, labelEntity, "i18n(this)");
            showNotification(livestreamMessageLabel);
        });
        if (unregisterable != null) {
            unregisterable.unregister();
        }
        unregisterable = EntityBindings.onForeignFieldsChanged(onLabelChange, event, Event.livestreamMessageLabel, "en", "de", "fr", "es", "pt");

        // Listen for livestream message changes and show notifications
        FXProperties.runOnPropertyChange(() -> {
            if (liveMessageLabelProperty.get() == null) {
                hideNotification();
            } else {
                showNotification(livestreamMessageLabel);
            }
        }, liveMessageLabelProperty);

        I18nEntities.bindExpressionProperties(livestreamMessageLabel, liveMessageLabelProperty, "i18n(this)");
        livestreamMessageLabel.setTextAlignment(TextAlignment.CENTER);
        if (liveMessageLabelProperty.get() != null) {
            showNotification(livestreamMessageLabel);
        }

    }

    // Method to show notification
    private void showNotification(Label messageLabel) {

        switch ("critical".toLowerCase()) {
            case "error":
            case "critical":
                notificationContainer.getStyleClass().setAll("notification-bar", "notification-error");
                break;
            case "warning":
                notificationContainer.getStyleClass().setAll("notification-bar", "notification-warning");
                break;
            case "info":
            default:
                notificationContainer.getStyleClass().setAll("notification-bar", "notification-info");
                break;
        }

        // Icon (you can replace with actual icons from your icon library)
        Label iconLabel = new Label("⚠"); // Use the appropriate icon based on type
        iconLabel.getStyleClass().add("notification-icon");

        // Message text
        messageLabel.getStyleClass().add("notification-text");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        // Close button
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("notification-close");
        closeButton.setOnAction(e -> hideNotification());

        notificationContainer.getChildren().setAll(iconLabel, messageLabel, closeButton);

        // CSS-like slide-in animation using Timeline for smooth transition
        Timeline slideIn = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(notificationContainer.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(notificationContainer.opacityProperty(), 1, Interpolator.EASE_OUT)
            )
        );
        slideIn.play();
        notificationContainer.setVisible(true);
    }

    // Method to hide notification with CSS-like animation
    private void hideNotification() {
        Timeline slideOut = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(notificationContainer.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(250),
                new KeyValue(notificationContainer.opacityProperty(), 0, Interpolator.EASE_IN)
            )
        );
        slideOut.setOnFinished(e -> notificationContainer.setVisible(false));
        slideOut.play();
    }


}
