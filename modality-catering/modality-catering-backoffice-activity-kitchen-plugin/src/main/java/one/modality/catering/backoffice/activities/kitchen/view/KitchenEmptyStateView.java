package one.modality.catering.backoffice.activities.kitchen.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;

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
        Label titleLabel = new Label("Configuration Setup Required");
        titleLabel.getStyleClass().add("kitchen-empty-state-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Subtitle
        Label subtitleLabel = new Label("KBS3 Integration Not Yet Configured");
        subtitleLabel.getStyleClass().add("kitchen-empty-state-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);

        // Message
        Label messageLabel = new Label(
            "Your organization's setup is not yet configured to work with the KBS3 meal booking system. " +
            "To enable this functionality, please contact our technical support team."
        );
        messageLabel.getStyleClass().add("kitchen-empty-state-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        // Info box - What needs to be done?
        VBox infoBox = createInfoBox();

        // Action box - Contact support
        VBox actionBox = createActionBox();

        // Footer
        Label footerLabel = new Label("This setup typically takes 1-2 business days to complete.");
        footerLabel.getStyleClass().add("kitchen-empty-state-footer");
        footerLabel.setTextAlignment(TextAlignment.CENTER);

        // Assemble container
        container.getChildren().addAll(
            iconContainer,
            titleLabel,
            subtitleLabel,
            messageLabel,
            infoBox,
            actionBox,
            footerLabel
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

        Label infoTitle = new Label("⚙️ What needs to be done?");
        infoTitle.getStyleClass().add("kitchen-empty-state-info-title");

        Label infoText = new Label(
            "Our technical team will configure the necessary integration settings for your organization " +
            "to access the KBS3 meal booking calendar and ordering system."
        );
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

        Label actionTitle = new Label("📧 Contact Technical Support");
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

        Label textLabel = new Label("kbs@kadampa.net");
        textLabel.getStyleClass().add("kitchen-empty-state-contact-text");

        row.getChildren().addAll(textLabel);
        return row;
    }

}
