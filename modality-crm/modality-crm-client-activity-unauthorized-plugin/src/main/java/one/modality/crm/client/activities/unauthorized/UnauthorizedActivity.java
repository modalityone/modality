package one.modality.crm.client.activities.unauthorized;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.action.OperationActionRegistry;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsWaiting;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * @author Bruno Salmon
 */
final class UnauthorizedActivity extends ViewDomainActivityBase {

    private static final String SECURITY_PATH =
        // circle
        "M0,64a64,64 0 1,0 128,0a64,64 0 1,0 -128,0 " +
        // padlock bottom
        "M 49.7231 56.7138 C 48.2625 56.7138 47.0645 57.9117 47.0645 59.3722 L 47.0645 80.6564 C 47.0645 82.1168 48.2625 83.3148 49.7231 83.3148 L 78.0964 83.3148 C 79.5568 83.3148 80.7548 82.1168 80.7548 80.6564 L 80.7548 59.3722 C 80.7548 57.9117 79.5568 56.7138 78.0964 56.7138 Z M 49.7231 58.4861 L 78.0964 58.4861 C 78.5886 58.4861 78.9825 58.88 78.9825 59.3722 L 78.9825 80.6564 C 78.9825 81.1486 78.5886 81.5425 78.0964 81.5425 L 49.7231 81.5425 C 49.2307 81.5425 48.8369 81.1486 48.8369 80.6564 L 48.8369 59.3722 C 48.8369 58.88 49.2307 58.4861 49.7231 58.4861 Z M 63.9178 65.5753 C 62.4082 65.5753 61.2594 66.724 61.2594 68.2338 C 61.2594 69.0379 61.6205 69.7271 62.1456 70.1702 L 62.1456 72.6646 C 62.1456 73.6492 62.9497 74.4369 63.9178 74.4369 C 64.8861 74.4369 65.6902 73.6327 65.6902 72.6646 L 65.6902 70.1702 C 66.2318 69.7271 66.5764 69.0215 66.5764 68.2338 C 66.5764 66.724 65.4276 65.5753 63.9178 65.5753 Z " +
        // padlock top
        "M 63.9999 44.8656 C 68.0369 44.8656 71.3189 48.1477 71.3189 52.1846 L 71.3189 56.1886 L 56.681 56.1886 L 56.681 52.1846 C 56.681 48.1477 59.963 44.8656 63.9999 44.8656 M 63.9999 42.2399 L 63.9999 42.2399 C 58.5025 42.2399 54.0553 46.6871 54.0553 52.1846 L 54.0553 58.8143 L 73.9445 58.8143 L 73.9445 52.1846 C 73.9445 46.6871 69.4973 42.2399 63.9999 42.2399 Z " +
        // shield
        "M 63.2451 107.553 C63.2451,107.553 98.3811,90.5552 98.3811,40.4459L98.3811,39.1251 C 86.2194 37.5138 79.1466 33.8051 74.0102 31.0974 C 70.006 28.9969 67.1179 27.4707 63.9178 27.4707 C 60.7179 27.4707 57.8296 28.9969 53.8256 31.0974 C 48.6727 33.8051 41.6164 37.5138 29.6697 40.1395 L 28.4061 40.4184 L 28.4061 41.7148 C 28.4061 90.8963 62.9004 107.389 63.2451 107.553";

    @Override
    public Node buildUi() {
        // Security icon with better styling
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(SECURITY_PATH);

        ScalePane iconPane = new ScalePane(svgPath);
        iconPane.setFixedSize(180, 180);

        // Add subtle pulse animation to icon
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), iconPane);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);

        // Main title
        Label titleLabel = I18nControls.newLabel("CheckingAuthorization");
        titleLabel.getStyleClass().add("unauthorized-title");
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Message label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("unauthorized-message");
        messageLabel.setAlignment(Pos.CENTER); // Centers single-line text
        messageLabel.setTextAlignment(TextAlignment.CENTER); // Centers multi-line text
        Controls.setupTextWrapping(messageLabel, true, false);

        // Content container
        VBox contentBox = new VBox(30, iconPane, titleLabel, messageLabel);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(40));
        contentBox.setMaxWidth(500);

        // Center everything
        BorderPane container = new BorderPane(contentBox);
        container.getStyleClass().add("unauthorized-container");

        // Handle authorization state changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (FXAuthorizationsWaiting.isAuthorizationsWaiting() || !OperationActionRegistry.getInstance().isLoaded()) {
                // Always showing a waiting mode for at least 1 s (to prevent showing transitory access denied)
                Collections.addIfNotContainsOrRemove(svgPath.getStyleClass(), true, "waiting");
                Collections.addIfNotContainsOrRemove(svgPath.getStyleClass(), false, "denied");
                I18nControls.bindI18nProperties(titleLabel, "CheckingAuthorization");
                I18nControls.bindI18nProperties(messageLabel, "AuthorizationCheck");
                messageLabel.setGraphic(Controls.createSpinner(16));
                pulse.play();
            } else {
                // Waiting for at least 1 s to prevent showing transitory access denied
                UiScheduler.scheduleDelay(1000, () -> {
                    Collections.addIfNotContainsOrRemove(svgPath.getStyleClass(), false, "waiting");
                    Collections.addIfNotContainsOrRemove(svgPath.getStyleClass(), true, "denied");
                    I18nControls.bindI18nProperties(titleLabel, "Unauthorized");
                    I18nControls.bindI18nProperties(messageLabel, "UnauthorizedMessage");
                    messageLabel.setGraphic(null);
                    pulse.stop();
                    iconPane.setScaleX(1.0);
                    iconPane.setScaleY(1.0);
                });
            }
            // Fade in the message
            messageLabel.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(400), messageLabel);
            fade.setToValue(1.0);
            fade.play();
        }, FXAuthorizationsWaiting.authorizationsWaitingProperty(), OperationActionRegistry.getInstance().loadedProperty());

        return container;
    }
}
