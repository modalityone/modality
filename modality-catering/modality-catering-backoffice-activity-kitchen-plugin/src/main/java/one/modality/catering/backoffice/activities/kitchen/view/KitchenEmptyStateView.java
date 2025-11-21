package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;
import one.modality.catering.backoffice.activities.kitchen.KitchenI18nKeys;

/**
 * Empty state view displayed when no meal data is configured in the KBS3 system.
 * Follows the design from the HTML mockup with settings/gear icon and alert badge.
 *
 * @author Claude Code
 */
public final class KitchenEmptyStateView {

    /**
     * Creates the empty state view with configuration guidance.
     *
     * @return VBox containing the complete empty state UI
     */
    public static VBox createEmptyStateView() {
        // Main container
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50, 40, 50, 40));
        container.setMaxWidth(600);
        container.getStyleClass().add("kitchen-empty-state-container");

        // Icon section with settings gear and alert badge
        VBox iconContainer = createIconSection();

        // Title
        Label titleLabel = I18nControls.newLabel(KitchenI18nKeys.EmptyStateTitle);
        titleLabel.getStyleClass().add("kitchen-empty-state-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Subtitle
        Label subtitleLabel = I18nControls.newLabel(KitchenI18nKeys.EmptyStateSubtitle);
        subtitleLabel.getStyleClass().add("kitchen-empty-state-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);

        // Message
        Label messageLabel = I18nControls.newLabel(KitchenI18nKeys.EmptyStateMessage);
        messageLabel.getStyleClass().add("kitchen-empty-state-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        // Info box - What needs to be done?
        VBox infoBox = createInfoBox();

        // Action box - Contact support
        VBox actionBox = createActionBox();

        // Assemble container
        container.getChildren().addAll(
            iconContainer,
            titleLabel,
            subtitleLabel,
            messageLabel,
            infoBox,
            actionBox
        );

        return container;
    }

    /**
     * Creates the icon section with configuration/setup gear icon.
     */
    private static VBox createIconSection() {
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setPrefSize(120, 120);
        iconContainer.getStyleClass().add("kitchen-empty-state-icon-container");

        // Create SVG configuration icon using SvgIcons
        SVGPath configIcon = SvgIcons.createConfigSetupIcon();
        configIcon.setStroke(Color.GRAY);
        configIcon.setFill(Color.WHITE);

        configIcon.setScaleX(3.5);  // Scale up the 24x24 icon to about 90x90
        configIcon.setScaleY(3.5);

        javafx.scene.layout.StackPane iconStack = new javafx.scene.layout.StackPane(configIcon);
        iconStack.setMaxSize(120, 120);

        iconContainer.getChildren().add(iconStack);
        return iconContainer;
    }

    /**
     * Creates the info box explaining what needs to be done.
     */
    private static VBox createInfoBox() {
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.getStyleClass().add("kitchen-empty-state-info-box");
        infoBox.setMaxWidth(550);

        Label infoTitle = I18nControls.newLabel(KitchenI18nKeys.EmptyStateInfoTitle);
        infoTitle.getStyleClass().add("kitchen-empty-state-info-title");

        Label infoText = I18nControls.newLabel(KitchenI18nKeys.EmptyStateInfoText);
        infoText.getStyleClass().add("kitchen-empty-state-info-text");
        infoText.setWrapText(true);

        infoBox.getChildren().addAll(infoTitle, infoText);
        return infoBox;
    }

    /**
     * Creates the action box with contact information.
     */
    private static VBox createActionBox() {
        VBox actionBox = new VBox(15);
        actionBox.setPadding(new Insets(25));
        actionBox.getStyleClass().add("kitchen-empty-state-action-box");
        actionBox.setMaxWidth(550);
        actionBox.setAlignment(Pos.CENTER);

        Label actionTitle = I18nControls.newLabel(KitchenI18nKeys.EmptyStateActionTitle);
        actionTitle.getStyleClass().add("kitchen-empty-state-action-title");

        // Email contact
        HBox emailContact = createContactRow(
        );

        actionBox.getChildren().addAll(actionTitle, emailContact);
        return actionBox;
    }

    /**
     * Creates a contact info row with icon and text.
     */
    private static HBox createContactRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);

        Label textLabel = I18nControls.newLabel(KitchenI18nKeys.EmptyStateContactEmail);
        textLabel.getStyleClass().add("kitchen-empty-state-contact-text");

        row.getChildren().addAll(textLabel);
        return row;
    }

}
