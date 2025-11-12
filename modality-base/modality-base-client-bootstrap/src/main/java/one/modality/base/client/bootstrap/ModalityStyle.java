package one.modality.base.client.bootstrap;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import one.modality.base.client.icons.SvgIcons;

/**
 * @author David Hello
 */
public interface ModalityStyle {

    String BTN_BLACK = "btn-black";
    String BTN_WHITE = "btn-white";
    String TEXT_COMMENT = "comment";

    // Outline button styles
    String OUTLINE = "outline";
    String OUTLINE_PRIMARY = "outline-primary";
    String OUTLINE_SECONDARY = "outline-secondary";
    String OUTLINE_SUCCESS = "outline-success";
    String OUTLINE_DANGER = "outline-danger";
    String OUTLINE_WARNING = "outline-warning";
    String OUTLINE_INFO = "outline-info";

    // Color-based badge styles (moved from Bootstrap for custom Modality styling)
    String BADGE_LIGHT_INFO = "badge-light-info";
    String BADGE_LIGHT_SUCCESS = "badge-light-success";
    String BADGE_LIGHT_WARNING = "badge-light-warning";
    String BADGE_LIGHT_DANGER = "badge-light-danger";
    String BADGE_GRAY = "badge-gray";
    String BADGE_LIGHT_GRAY = "badge-light-gray";
    String BADGE_PURPLE = "badge-purple";
    String BADGE_LIGHT_PURPLE = "badge-light-purple";
    String BADGE_LIGHT_PINK = "badge-light-pink";

    // Form section card styles
    String FORM_SECTION_CARD = "form-section-card";
    String FORM_SECTION_CARD_TITLE = "form-section-card-title";
    String FORM_GRID = "form-grid";

    static <N extends Node> N blackButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_BLACK);
    }
    static <N extends Node> N whiteButton(N button) {
        return Bootstrap.style(Bootstrap.button(button), BTN_WHITE);
    }

    // Outline button methods
    static <N extends Node> N outlineButton(N button, String... styles) {
        return Bootstrap.button(button, Bootstrap.combineStyles(OUTLINE, styles));
    }

    static <N extends Node> N outlinePrimaryButton(N button) {
        return Bootstrap.button(button, OUTLINE_PRIMARY);
    }

    static <N extends Node> N outlineSecondaryButton(N button) {
        return Bootstrap.button(button, OUTLINE_SECONDARY);
    }

    static <N extends Node> N outlineSuccessButton(N button) {
        return Bootstrap.button(button, OUTLINE_SUCCESS);
    }

    static <N extends Node> N outlineDangerButton(N button) {
        return Bootstrap.button(button, OUTLINE_DANGER);
    }

    static <N extends Node> N outlineWarningButton(N button) {
        return Bootstrap.button(button, OUTLINE_WARNING);
    }

    static <N extends Node> N outlineInfoButton(N button) {
        return Bootstrap.button(button, OUTLINE_INFO);
    }

    // Color-based badges (moved from Bootstrap for custom Modality styling)
    static <N extends Node> N badgeLightInfo(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_INFO);
    }

    static <N extends Node> N badgeLightSuccess(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_SUCCESS);
    }

    static <N extends Node> N badgeLightWarning(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_WARNING);
    }

    static <N extends Node> N badgeLightDanger(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_DANGER);
    }

    static <N extends Node> N badgeGray(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_GRAY);
    }

    static <N extends Node> N badgeLightGray(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_GRAY);
    }

    static <N extends Node> N badgePurple(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_PURPLE);
    }

    static <N extends Node> N badgeLightPurple(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_PURPLE);
    }

    static <N extends Node> N badgeLightPink(N badge) {
        return Bootstrap.badgePadding(badge, BADGE_LIGHT_PINK);
    }

    // Icon button helper methods

    /**
     * Sets an SVG icon as the graphic of a Labeled (Button, Label, etc.)
     * The icon is automatically scaled to fit button size and styled with CSS class
     * Handles bound graphic properties by unbinding them first if necessary
     */
    static <N extends Labeled> N setIcon(N labeled, SVGPath icon) {
        // Target size of 16px for button icons
        double targetSize = 16.0;

        // Calculate appropriate scale based on icon bounds
        double boundsWidth = icon.getBoundsInLocal().getWidth();
        double boundsHeight = icon.getBoundsInLocal().getHeight();
        double maxDimension = Math.max(boundsWidth, boundsHeight);
        double scale = maxDimension > 0 ? targetSize / maxDimension : 1.0;

        icon.setScaleX(scale);
        icon.setScaleY(scale);

        // Add CSS class for styling (color inheritance)
        icon.getStyleClass().add("button-icon");

        // Wrap the icon in a fixed-size container so the button sizes correctly
        // This prevents the button from using the icon's original (unscaled) bounds
        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(targetSize, targetSize);
        iconContainer.setPrefSize(targetSize, targetSize);
        iconContainer.setMaxSize(targetSize, targetSize);
        iconContainer.setAlignment(Pos.CENTER);

        // Check if graphic property is bound (e.g., from I18nControls)
        // If bound, unbind it first to avoid "A bound value cannot be set" error
        if (labeled.graphicProperty().isBound()) {
            labeled.graphicProperty().unbind();
        }

        labeled.setGraphic(iconContainer);
        return labeled;
    }

    /**
     * Applies primary button style with an SVG icon to an existing button
     */
    static Button primaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.primaryButton(button);
    }

    /**
     * Applies secondary button style with an SVG icon to an existing button
     */
    static Button secondaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.secondaryButton(button);
    }

    /**
     * Applies success button style with an SVG icon to an existing button
     */
    static Button successButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.successButton(button);
    }

    /**
     * Applies danger button style with an SVG icon to an existing button
     */
    static Button dangerButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.dangerButton(button);
    }

    // Outline button variants with icons

    /**
     * Applies outline primary button style with an SVG icon to an existing button
     */
    static Button outlinePrimaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlinePrimaryButton(button);
    }

    /**
     * Applies outline secondary button style with an SVG icon to an existing button
     */
    static Button outlineSecondaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlineSecondaryButton(button);
    }

    /**
     * Applies outline success button style with an SVG icon to an existing button
     */
    static Button outlineSuccessButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlineSuccessButton(button);
    }

    /**
     * Applies outline danger button style with an SVG icon to an existing button
     */
    static Button outlineDangerButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlineDangerButton(button);
    }

    /**
     * Applies outline warning button style with an SVG icon to an existing button
     */
    static Button outlineWarningButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlineWarningButton(button);
    }

    /**
     * Applies outline info button style with an SVG icon to an existing button
     */
    static Button outlineInfoButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return outlineInfoButton(button);
    }

    // Large button variants with icons

    /**
     * Applies large primary button style with an SVG icon to an existing button
     * Defaults to normal height (largeHeight = false) for consistency with typical usage
     */
    static Button largePrimaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largePrimaryButton(button, false);
    }

    /**
     * Applies large primary button style with an SVG icon to an existing button
     */
    static Button largePrimaryButtonWithIcon(Button button, SVGPath icon, boolean largeHeight) {
        setIcon(button, icon);
        return Bootstrap.largePrimaryButton(button, largeHeight);
    }

    /**
     * Applies large secondary button style with an SVG icon to an existing button
     * Defaults to normal height (largeHeight = false) for consistency with typical usage
     */
    static Button largeSecondaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeSecondaryButton(button, false);
    }

    /**
     * Applies large secondary button style with an SVG icon to an existing button
     */
    static Button largeSecondaryButtonWithIcon(Button button, SVGPath icon, boolean largeHeight) {
        setIcon(button, icon);
        return Bootstrap.largeSecondaryButton(button, largeHeight);
    }

    /**
     * Applies large success button style with an SVG icon to an existing button
     * Defaults to normal height (largeHeight = false) for consistency with typical usage
     */
    static Button largeSuccessButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeSuccessButton(button, false);
    }

    /**
     * Applies large success button style with an SVG icon to an existing button
     */
    static Button largeSuccessButtonWithIcon(Button button, SVGPath icon, boolean largeHeight) {
        setIcon(button, icon);
        return Bootstrap.largeSuccessButton(button, largeHeight);
    }

    /**
     * Applies large danger button style with an SVG icon to an existing button
     */
    static Button largeDangerButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeDangerButton(button);
    }

    // Specific icon button examples

    /**
     * Applies primary button style with a pen icon to an existing button
     */
    static Button primaryEditButton(Button button) {
        return primaryButtonWithIcon(button, SvgIcons.createPenPath());
    }

    /**
     * Applies primary button style with custom SVG icon to an existing button
     */
    static Button primaryEditButton(Button button, SVGPath icon) {
        return primaryButtonWithIcon(button, icon);
    }

    /**
     * Applies danger button style with a trash icon to an existing button
     */
    static Button dangerDeleteButton(Button button) {
        return dangerButtonWithIcon(button, SvgIcons.createTrashSVGPath());
    }

    /**
     * Applies success button style with a save icon to an existing button
     */
    static Button successSaveButton(Button button) {
        return successButtonWithIcon(button, SvgIcons.createSavePath());
    }

    /**
     * Applies primary button style with a plus icon to an existing button
     */
    static Button primaryAddButton(Button button) {
        return primaryButtonWithIcon(button, SvgIcons.createPlusIconPath());
    }

    /**
     * Applies secondary button style with a times icon to an existing button
     */
    static Button secondaryCancelButton(Button button) {
        return secondaryButtonWithIcon(button, SvgIcons.createTimesPath());
    }

    // Outline specific icon button examples

    /**
     * Applies outline primary button style with a pen icon to an existing button
     */
    static Button outlinePrimaryEditButton(Button button) {
        return outlinePrimaryButtonWithIcon(button, SvgIcons.createPenPath());
    }

    /**
     * Applies outline primary button style with custom SVG icon to an existing button
     */
    static Button outlinePrimaryEditButton(Button button, SVGPath icon) {
        return outlinePrimaryButtonWithIcon(button, icon);
    }

    /**
     * Applies outline danger button style with a trash icon to an existing button
     */
    static Button outlineDangerDeleteButton(Button button) {
        return outlineDangerButtonWithIcon(button, SvgIcons.createTrashSVGPath());
    }

    /**
     * Applies outline success button style with a save icon to an existing button
     */
    static Button outlineSuccessSaveButton(Button button) {
        return outlineSuccessButtonWithIcon(button, SvgIcons.createSavePath());
    }

    /**
     * Applies outline primary button style with a plus icon to an existing button
     */
    static Button outlinePrimaryAddButton(Button button) {
        return outlinePrimaryButtonWithIcon(button, SvgIcons.createPlusIconPath());
    }

    /**
     * Applies outline secondary button style with a times icon to an existing button
     */
    static Button outlineSecondaryCancelButton(Button button) {
        return outlineSecondaryButtonWithIcon(button, SvgIcons.createTimesPath());
    }

    // Large specific icon button examples

    /**
     * Applies large primary button style with a pen icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largePrimaryEditButton(Button button) {
        return largePrimaryButtonWithIcon(button, SvgIcons.createPenPath(), false);
    }

    /**
     * Applies large primary button style with a pen icon to an existing button
     */
    static Button largePrimaryEditButton(Button button, boolean largeHeight) {
        return largePrimaryButtonWithIcon(button, SvgIcons.createPenPath(), largeHeight);
    }

    /**
     * Applies large primary button style with custom SVG icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largePrimaryEditButton(Button button, SVGPath icon) {
        return largePrimaryButtonWithIcon(button, icon, false);
    }

    /**
     * Applies large primary button style with custom SVG icon to an existing button
     */
    static Button largePrimaryEditButton(Button button, SVGPath icon, boolean largeHeight) {
        return largePrimaryButtonWithIcon(button, icon, largeHeight);
    }

    /**
     * Applies large danger button style with a trash icon to an existing button
     * Note: Danger buttons always use large height (Bootstrap limitation)
     */
    static Button largeDangerDeleteButton(Button button) {
        return largeDangerButtonWithIcon(button, SvgIcons.createTrashSVGPath());
    }

    /**
     * Applies large success button style with a save icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largeSuccessSaveButton(Button button) {
        return largeSuccessButtonWithIcon(button, SvgIcons.createSavePath(), false);
    }

    /**
     * Applies large success button style with a save icon to an existing button
     */
    static Button largeSuccessSaveButton(Button button, boolean largeHeight) {
        return largeSuccessButtonWithIcon(button, SvgIcons.createSavePath(), largeHeight);
    }

    /**
     * Applies large primary button style with a plus icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largePrimaryAddButton(Button button) {
        return largePrimaryButtonWithIcon(button, SvgIcons.createPlusIconPath(), false);
    }

    /**
     * Applies large primary button style with a plus icon to an existing button
     */
    static Button largePrimaryAddButton(Button button, boolean largeHeight) {
        return largePrimaryButtonWithIcon(button, SvgIcons.createPlusIconPath(), largeHeight);
    }

    /**
     * Applies large secondary button style with a times icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largeSecondaryCancelButton(Button button) {
        return largeSecondaryButtonWithIcon(button, SvgIcons.createTimesPath(), false);
    }

    /**
     * Applies large secondary button style with a times icon to an existing button
     */
    static Button largeSecondaryCancelButton(Button button, boolean largeHeight) {
        return largeSecondaryButtonWithIcon(button, SvgIcons.createTimesPath(), largeHeight);
    }

    // Large outline button variants with icons

    /**
     * Applies large outline primary button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlinePrimaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_PRIMARY);
    }

    /**
     * Applies large outline secondary button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlineSecondaryButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_SECONDARY);
    }

    /**
     * Applies large outline success button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlineSuccessButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_SUCCESS);
    }

    /**
     * Applies large outline danger button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlineDangerButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_DANGER);
    }

    /**
     * Applies large outline warning button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlineWarningButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_WARNING);
    }

    /**
     * Applies large outline info button style with an SVG icon to an existing button
     * Uses largeResize for consistent large button sizing
     */
    static Button largeOutlineInfoButtonWithIcon(Button button, SVGPath icon) {
        setIcon(button, icon);
        return Bootstrap.largeButton(button, false, OUTLINE_INFO);
    }

    // Large outline specific icon button examples

    /**
     * Applies large outline primary button style with a pen icon to an existing button
     * Defaults to normal height for consistency
     */
    static Button largeOutlinePrimaryEditButton(Button button) {
        return largeOutlinePrimaryButtonWithIcon(button, SvgIcons.createPenPath());
    }

    /**
     * Applies large outline primary button style with custom SVG icon to an existing button
     */
    static Button largeOutlinePrimaryEditButton(Button button, SVGPath icon) {
        return largeOutlinePrimaryButtonWithIcon(button, icon);
    }

    /**
     * Applies large outline danger button style with a trash icon to an existing button
     */
    static Button largeOutlineDangerDeleteButton(Button button) {
        return largeOutlineDangerButtonWithIcon(button, SvgIcons.createTrashSVGPath());
    }

    /**
     * Applies large outline success button style with a save icon to an existing button
     */
    static Button largeOutlineSuccessSaveButton(Button button) {
        return largeOutlineSuccessButtonWithIcon(button, SvgIcons.createSavePath());
    }

    /**
     * Applies large outline primary button style with a plus icon to an existing button
     */
    static Button largeOutlinePrimaryAddButton(Button button) {
        return largeOutlinePrimaryButtonWithIcon(button, SvgIcons.createPlusIconPath());
    }

    /**
     * Applies large outline secondary button style with a times icon to an existing button
     */
    static Button largeOutlineSecondaryCancelButton(Button button) {
        return largeOutlineSecondaryButtonWithIcon(button, SvgIcons.createTimesPath());
    }

    // Form field helpers

    /**
     * Creates a form field container with a label and text field
     * Returns a VBox containing the label, field, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param textField The text field control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @param enableCopy Whether to add copy-to-clipboard button for non-editable fields
     * @return VBox container with label, field, and optional help text
     */
    static VBox createFormTextField(Label label, TextField textField, String placeholder, String helpText, boolean enableCopy) {
        VBox container = new VBox(8);
        container.getStyleClass().add("form-group");

        label.getStyleClass().add("form-label");
        textField.getStyleClass().add("form-field");

        // Bind readonly style class to editable property - add when not editable, remove when editable
        textField.editableProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // Field became non-editable, add readonly style
                if (!textField.getStyleClass().contains("readonly-field")) {
                    textField.getStyleClass().add("readonly-field");
                }
            } else {
                // Field became editable, remove readonly style
                textField.getStyleClass().remove("readonly-field");
            }
        });
        // Set initial state
        if (!textField.isEditable()) {
            textField.getStyleClass().add("readonly-field");
        }

        if (placeholder != null && !placeholder.isEmpty()) {
            textField.setPromptText(placeholder);
        }

        // Add copy button if enabled and field is not editable
        Node fieldNode = textField;
        if (enableCopy && !textField.isEditable()) {
            fieldNode = addCopyToClipboard(textField);
        }

        container.getChildren().addAll(label, fieldNode);

        if (helpText != null && !helpText.isEmpty()) {
            Label helpLabel = new Label(helpText);
            helpLabel.getStyleClass().add("form-help-text");
            helpLabel.setWrapText(true);
            container.getChildren().add(helpLabel);
        }

        return container;
    }

    /**
     * Creates a form field container with a label and text field
     * Returns a VBox containing the label, field, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param textField The text field control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @return VBox container with label, field, and optional help text
     */
    static VBox createFormTextField(Label label, TextField textField, String placeholder, String helpText) {
        return createFormTextField(label, textField, placeholder, helpText, false);
    }

    /**
     * Creates a form field container with a label and text field (no placeholder/help text)
     * Returns a VBox containing the label and field with consistent spacing
     *
     * @param label The label for the field
     * @param textField The text field control
     * @param enableCopy Whether to add copy-to-clipboard button for non-editable fields
     * @return VBox container with label and field
     */
    static VBox createFormTextField(Label label, TextField textField, boolean enableCopy) {
        return createFormTextField(label, textField, null, null, enableCopy);
    }

    /**
     * Creates a form field container with a label and text field (no placeholder/help text)
     * Returns a VBox containing the label and field with consistent spacing
     *
     * @param label The label for the field
     * @param textField The text field control
     * @return VBox container with label and field
     */
    static VBox createFormTextField(Label label, TextField textField) {
        return createFormTextField(label, textField, null, null, false);
    }

    /**
     * Creates a form field container with a label and entity selector
     * Returns a VBox containing the label, entity selector, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param entitySelector The entity button selector control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @return VBox container with label, entity selector, and optional help text
     */
    static <E extends Entity> VBox createFormEntitySelector(Label label, EntityButtonSelector<E> entitySelector, String placeholder, String helpText) {
        VBox container = new VBox(8);
        container.getStyleClass().add("form-group");

        label.getStyleClass().add("form-label");
        Button selectorButton = entitySelector.getButton();
        if (selectorButton != null) {
            selectorButton.getStyleClass().add("form-field");
        }

        if (placeholder != null && !placeholder.isEmpty()) {
            // EntityButtonSelector doesn't have setPromptText, but we can set it on the button if needed
            // For now, we'll skip this as EntityButtonSelector handles its own display
        }

        container.getChildren().addAll(label, entitySelector.getButton());

        if (helpText != null && !helpText.isEmpty()) {
            Label helpLabel = new Label(helpText);
            helpLabel.getStyleClass().add("form-help-text");
            helpLabel.setWrapText(true);
            container.getChildren().add(helpLabel);
        }

        return container;
    }

    /**
     * Creates a form field container with a label and entity selector (no placeholder/help text)
     * Returns a VBox containing the label and entity selector with consistent spacing
     *
     * @param label The label for the field
     * @param entitySelector The entity button selector control
     * @return VBox container with label and entity selector
     */
    static <E extends Entity> VBox createFormEntitySelector(Label label, EntityButtonSelector<E> entitySelector) {
        return createFormEntitySelector(label, entitySelector, null, null);
    }

    /**
     * Creates a form field container with a label and date picker
     * Returns a VBox containing the label, date picker, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param datePicker The date picker control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @return VBox container with label, date picker, and optional help text
     */
    static VBox createFormDatePicker(Label label, DatePicker datePicker, String placeholder, String helpText) {
        VBox container = new VBox(8);
        container.getStyleClass().add("form-group");

        label.getStyleClass().add("form-label");
        datePicker.getStyleClass().add("form-field");

        if (placeholder != null && !placeholder.isEmpty()) {
            datePicker.setPromptText(placeholder);
        }

        container.getChildren().addAll(label, datePicker);

        if (helpText != null && !helpText.isEmpty()) {
            Label helpLabel = new Label(helpText);
            helpLabel.getStyleClass().add("form-help-text");
            helpLabel.setWrapText(true);
            container.getChildren().add(helpLabel);
        }

        return container;
    }

    /**
     * Creates a form field container with a label and date picker (no placeholder/help text)
     * Returns a VBox containing the label and date picker with consistent spacing
     *
     * @param label The label for the field
     * @param datePicker The date picker control
     * @return VBox container with label and date picker
     */
    static VBox createFormDatePicker(Label label, DatePicker datePicker) {
        return createFormDatePicker(label, datePicker, null, null);
    }

    /**
     * Creates a form field container with a label and password field
     * Returns a VBox containing the label, password field, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param passwordField The password field control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @return VBox container with label, password field, and optional help text
     */
    static VBox createFormPasswordField(Label label, PasswordField passwordField, String placeholder, String helpText) {
        VBox container = new VBox(8);
        container.getStyleClass().add("form-group");

        label.getStyleClass().add("form-label");
        passwordField.getStyleClass().add("form-field");

        if (placeholder != null && !placeholder.isEmpty()) {
            passwordField.setPromptText(placeholder);
        }

        container.getChildren().addAll(label, passwordField);

        if (helpText != null && !helpText.isEmpty()) {
            Label helpLabel = new Label(helpText);
            helpLabel.getStyleClass().add("form-help-text");
            helpLabel.setWrapText(true);
            container.getChildren().add(helpLabel);
        }

        return container;
    }

    /**
     * Creates a form field container with a label and password field (no placeholder/help text)
     * Returns a VBox containing the label and password field with consistent spacing
     *
     * @param label The label for the field
     * @param passwordField The password field control
     * @return VBox container with label and password field
     */
    static VBox createFormPasswordField(Label label, PasswordField passwordField) {
        return createFormPasswordField(label, passwordField, null, null);
    }

    /**
     * Creates a form field container with a label and text area
     * Returns a VBox containing the label, text area, and optional help text with consistent spacing
     *
     * @param label The label for the field
     * @param textArea The text area control
     * @param placeholder Optional placeholder text (can be null)
     * @param helpText Optional help text shown below the field (can be null)
     * @return VBox container with label, text area, and optional help text
     */
    static VBox createFormTextArea(Label label, TextArea textArea, String placeholder, String helpText) {
        VBox container = new VBox(8);
        container.getStyleClass().add("form-group");

        label.getStyleClass().add("form-label");
        textArea.getStyleClass().add("form-field");

        if (placeholder != null && !placeholder.isEmpty()) {
            textArea.setPromptText(placeholder);
        }

        container.getChildren().addAll(label, textArea);

        if (helpText != null && !helpText.isEmpty()) {
            Label helpLabel = new Label(helpText);
            helpLabel.getStyleClass().add("form-help-text");
            helpLabel.setWrapText(true);
            container.getChildren().add(helpLabel);
        }

        return container;
    }

    /**
     * Creates a form field container with a label and text area (no placeholder/help text)
     * Returns a VBox containing the label and text area with consistent spacing
     *
     * @param label The label for the field
     * @param textArea The text area control
     * @return VBox container with label and text area
     */
    static VBox createFormTextArea(Label label, TextArea textArea) {
        return createFormTextArea(label, textArea, null, null);
    }

    /**
     * Applies form checkbox styling to a checkbox
     * Checkboxes typically don't need a separate label container since they have built-in text
     *
     * @param checkBox The checkbox control
     * @return The styled checkbox
     */
    static CheckBox createFormCheckBox(CheckBox checkBox) {
        checkBox.getStyleClass().add("form-checkbox");
        return checkBox;
    }

    /**
     * Adds a copy-to-clipboard button to a non-editable text field
     * The copy icon appears on the right side of the text field
     * When clicked, it copies the field content to the clipboard and shows a temporary confirmation message
     *
     * @param textField The text field to enhance with copy functionality (must be non-editable)
     * @return StackPane containing the text field with copy button overlay, or the original text field if editable
     */
    static StackPane addCopyToClipboard(TextField textField) {
        // Create the copy button with icon
        Button copyButton = new Button();
        SVGPath copyIcon = SvgIcons.createCopyPath();
        setIcon(copyButton, copyIcon);
        copyButton.getStyleClass().addAll("btn", "btn-link", "copy-button");
        copyButton.setMinWidth(32);
        copyButton.setPrefWidth(32);
        copyButton.setMaxWidth(32);
        copyButton.setMinHeight(32);
        copyButton.setPrefHeight(32);
        copyButton.setMaxHeight(32);
        copyButton.setPadding(new Insets(4));

        // Bind button visibility and managed to inverse of textField editable property
        // Button should only be visible when field is not editable
        copyButton.visibleProperty().bind(textField.editableProperty().not());
        copyButton.managedProperty().bind(textField.editableProperty().not());

        // Create notification label (initially hidden)
        Label notificationLabel = I18nControls.newLabel("Copied");
        notificationLabel.getStyleClass().add("copy-notification");
        notificationLabel.setPadding(new Insets(4, 8, 4, 8));
        notificationLabel.setOpacity(0);
        notificationLabel.setVisible(false);
        notificationLabel.setMouseTransparent(true);

        // Setup copy action
        copyButton.setOnAction(e -> {
            String textToCopy = textField.getText();
            if (textToCopy != null && !textToCopy.isEmpty()) {
                // Copy to clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(textToCopy);
                clipboard.setContent(content);

                // Show notification with fade in/out animation
                notificationLabel.setVisible(true);
                notificationLabel.setOpacity(1);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1500), notificationLabel);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setDelay(Duration.millis(800)); // Show for 800ms before fading
                fadeOut.setOnFinished(event -> notificationLabel.setVisible(false));
                fadeOut.play();
            }
        });
        
        // Set cursor to hand for better UX
        copyButton.setCursor(Cursor.HAND);

        // Create container with button positioned on the right
        HBox buttonContainer = new HBox(copyButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPickOnBounds(false); // Only button area is clickable
        buttonContainer.setPadding(new Insets(0, 4, 0, 0));

        // Create notification container positioned at the right (above the button)
        HBox notificationContainer = new HBox(notificationLabel);
        notificationContainer.setAlignment(Pos.CENTER_RIGHT);
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setPadding(new Insets(0, 40, 0, 0)); // Offset to the left of the button

        // Stack everything together
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(textField, buttonContainer, notificationContainer);
        StackPane.setAlignment(buttonContainer, Pos.CENTER_RIGHT);
        StackPane.setAlignment(notificationContainer, Pos.CENTER_RIGHT);

        // Ensure copy button and containers remain enabled even when TextField is disabled
        // This is important for read-only fields that users still need to copy from
        textField.disableProperty().addListener((obs, oldVal, newVal) -> {
            copyButton.setDisable(false);
            buttonContainer.setDisable(false);
            notificationContainer.setDisable(false);
        });
        // Set initial state
        copyButton.setDisable(false);
        buttonContainer.setDisable(false);
        notificationContainer.setDisable(false);

        return stackPane;
    }

    // Tab styling helpers

    /**
     * Applies modern tab styling to a TabPane
     * Creates a clean, modern look with bottom border on selected tabs
     * Used in backoffice admin panels for consistent UX
     *
     * @param tabPane The TabPane to style
     * @return The styled TabPane
     */
    static TabPane modernTabPane(TabPane tabPane) {
        tabPane.getStyleClass().add("modality-modern-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    /**
     * Creates a wrapper with padding for tab content
     * Provides consistent spacing within tab content areas
     *
     * @param content The content node to wrap
     * @return StackPane containing the content with padding
     */
    static StackPane wrapTabContent(Node content) {
        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(24));
        return wrapper;
    }

}
