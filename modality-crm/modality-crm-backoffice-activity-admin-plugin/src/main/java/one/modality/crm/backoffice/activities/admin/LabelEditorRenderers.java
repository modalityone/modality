package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.animation.PauseTransition;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Label;

import java.util.Map;

/**
 * Custom renderers for LabelEditorView grid columns.
 *
 * @author Claude Code
 */
public class LabelEditorRenderers {

    private static LabelEditorView labelEditorView;

    public static void setLabelEditorView(LabelEditorView view) {
        labelEditorView = view;
    }

    static {
        registerRenderers();
    }

    public static void registerRenderers() {

        // Renderer for the label ID column - shows just the numeric ID
        ValueRendererRegistry.registerValueRenderer("labelId", (value, context) -> {
            if (!(value instanceof Label label)) {
                return null;
            }
            Object pk = label.getPrimaryKey();
            String idText = pk != null ? pk.toString() : "";
            javafx.scene.control.Label idLabel = new javafx.scene.control.Label(idText);
            idLabel.getStyleClass().add("labeleditor-id-cell");
            return idLabel;
        });

        // Renderer for the reference language column
        ValueRendererRegistry.registerValueRenderer("refLanguage", (value, context) -> {
            if (value == null) {
                return null;
            }
            String refLang = value.toString().toUpperCase().trim();
            if (refLang.isEmpty()) {
                refLang = "EN"; // Default to English if not set
            }
            javafx.scene.control.Label badge = ModalityStyle.badgeLightPurple(new javafx.scene.control.Label(refLang));
            badge.setPadding(new Insets(4, 8, 4, 8));
            return badge;
        });

        // Renderer for the reference text column - shows text in the label's reference language
        ValueRendererRegistry.registerValueRenderer("referenceText", (value, context) -> {
            if (!(value instanceof Label label)) {
                return null;
            }
            String ref = label.getRef();
            String text = getReferenceText(label, ref);
            if (text == null || text.isEmpty()) {
                text = label.getEn(); // Fallback to English
            }
            Text textNode = new Text(text != null ? truncateText(text, 80) : "");
            if (text != null && text.length() > 80) {
                Tooltip.install(textNode, new Tooltip(text));
            }
            return textNode;
        });

        // Renderer for translation flags - shows which languages are filled
        ValueRendererRegistry.registerValueRenderer("translationFlags", (value, context) -> {
            if (!(value instanceof Label label)) {
                return null;
            }
            HBox flagsBox = new HBox(3);
            flagsBox.setAlignment(Pos.CENTER_LEFT);

            String ref = label.getRef();
            if (ref == null || ref.isEmpty()) {
                ref = Label.en;
            }

            // Add flags for each language (9 languages total - no Italian in database)
            addLanguageFlag(flagsBox, "EN", label.getEn(), ref.equals(Label.en));
            addLanguageFlag(flagsBox, "FR", label.getFr(), ref.equals(Label.fr));
            addLanguageFlag(flagsBox, "ES", label.getEs(), ref.equals(Label.es));
            addLanguageFlag(flagsBox, "DE", label.getDe(), ref.equals(Label.de));
            addLanguageFlag(flagsBox, "PT", label.getPt(), ref.equals(Label.pt));
            addLanguageFlag(flagsBox, "ZHS", label.getZhs(), ref.equals(Label.zhs));
            addLanguageFlag(flagsBox, "ZHT", label.getYue(), ref.equals(Label.zht));
            addLanguageFlag(flagsBox, "EL", label.getEl(), ref.equals(Label.el));
            addLanguageFlag(flagsBox, "VI", label.getVi(), ref.equals(Label.vi));

            return flagsBox;
        });

        // Renderer for usage count with hover popup showing details
        ValueRendererRegistry.registerValueRenderer("usageCount", (value, context) -> {
            if (!(value instanceof Label label)) {
                return null;
            }
            int count = labelEditorView != null ? labelEditorView.getUsageCount(label) : 0;
            javafx.scene.control.Label badge;
            if (count > 0) {
                badge = ModalityStyle.badgeLightInfo(new javafx.scene.control.Label(String.valueOf(count)));
            } else {
                badge = ModalityStyle.badgeGray(new javafx.scene.control.Label("-"));
            }
            badge.setPadding(new Insets(4, 8, 4, 8));

            // Add hover popup if there are usages
            if (count > 0 && labelEditorView != null) {
                LabelUsageDetails usageDetails = labelEditorView.getUsageDetails(label);
                if (usageDetails != null && usageDetails.hasUsages()) {
                    setupUsageHoverPopup(badge, usageDetails);
                }
            }

            return badge;
        });

        // Renderer for action buttons (Edit, Delete)
        ValueRendererRegistry.registerValueRenderer("labelActions", (value, context) -> {
            if (!(value instanceof Label label)) {
                return null;
            }

            HBox actionsBox = new HBox(8);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#6c757d"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> {
                if (labelEditorView != null) {
                    labelEditorView.showEditLabelDialog(label);
                }
            });
            editButton.getStyleClass().add("admin-action-button");

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (labelEditorView != null) {
                    int usageCount = labelEditorView.getUsageCount(label);
                    labelEditorView.showDeleteConfirmation(label, usageCount);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().addAll(editButton, deleteButton);
            return actionsBox;
        });
    }

    /**
     * Gets the text in the reference language for a label.
     */
    private static String getReferenceText(Label label, String ref) {
        if (ref == null || ref.isEmpty()) {
            return label.getEn();
        }
        return switch (ref.toLowerCase().trim()) {
            case "en" -> label.getEn();
            case "fr" -> label.getFr();
            case "es" -> label.getEs();
            case "de" -> label.getDe();
            case "pt" -> label.getPt();
            case "zhs", "zh" -> label.getZhs();
            case "zht" -> label.getYue();
            case "el" -> label.getEl();
            case "vi" -> label.getVi();
            default -> label.getEn();
        };
    }

    /**
     * Adds a language flag badge to the container.
     */
    private static void addLanguageFlag(HBox container, String code, String text, boolean isReference) {
        boolean isFilled = text != null && !text.trim().isEmpty();

        javafx.scene.control.Label flag = new javafx.scene.control.Label(code);
        flag.setPadding(new Insets(2, 4, 2, 4));
        flag.getStyleClass().add("labeleditor-lang-flag");

        if (isReference) {
            // Reference language - show with special styling
            flag = ModalityStyle.badgeLightWarning(flag);
            flag.setText(code + "*");
        } else if (isFilled) {
            // Filled translation
            flag = Bootstrap.successBadge(flag);
        } else {
            // Empty translation
            flag = ModalityStyle.badgeGray(flag);
            flag.setOpacity(0.5);
        }

        String tooltip = getLanguageName(code) + (isReference ? " (Reference)" : (isFilled ? " - Filled" : " - Empty"));
        Tooltip.install(flag, new Tooltip(tooltip));

        container.getChildren().add(flag);
    }

    /**
     * Gets the full language name from the code.
     */
    private static String getLanguageName(String code) {
        return switch (code.toUpperCase()) {
            case "EN" -> "English";
            case "FR" -> "French";
            case "ES" -> "Spanish";
            case "DE" -> "German";
            case "PT" -> "Portuguese";
            case "ZH", "ZHS" -> "Chinese (Simplified)";
            case "ZHT" -> "Chinese (Traditional)";
            case "EL" -> "Greek";
            case "VI" -> "Vietnamese";
            default -> code;
        };
    }

    /**
     * Truncates text to a maximum length with ellipsis.
     */
    private static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Creates usage breakdown content for use in dialogs (not as a popup).
     * Shows detailed entity references with names, IDs, and dates when available.
     */
    public static VBox createUsageBreakdownPanel(LabelUsageDetails usageDetails) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(12, 16, 12, 16));
        panel.getStyleClass().add("labeleditor-usage-panel");

        // Header with icon-like indicator
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Label warningIcon = new javafx.scene.control.Label("\u26A0"); // Warning symbol
        warningIcon.getStyleClass().add("labeleditor-usage-warning-icon");

        javafx.scene.control.Label titleLabel = I18nControls.newLabel(LabelUsageLocation);
        titleLabel.getStyleClass().add("labeleditor-usage-title");
        Bootstrap.strong(titleLabel);

        header.getChildren().addAll(warningIcon, titleLabel);
        panel.getChildren().add(header);

        // Usage breakdown with detailed entity references
        Map<String, java.util.List<LabelUsageDetails.EntityReference>> detailedUsage = usageDetails.getDetailedUsage();

        for (Map.Entry<String, java.util.List<LabelUsageDetails.EntityReference>> entry : detailedUsage.entrySet()) {
            String entityType = entry.getKey();
            java.util.List<LabelUsageDetails.EntityReference> refs = entry.getValue();

            // Entity type header
            HBox typeHeader = new HBox(8);
            typeHeader.setAlignment(Pos.CENTER_LEFT);
            typeHeader.setPadding(new Insets(4, 0, 0, 10));

            javafx.scene.control.Label typeLabel = new javafx.scene.control.Label(entityType + " (" + refs.size() + "):");
            typeLabel.getStyleClass().add("labeleditor-usage-type-label");
            Bootstrap.strong(typeLabel);

            typeHeader.getChildren().add(typeLabel);
            panel.getChildren().add(typeHeader);

            // Show individual entity references (limit to first 10 to avoid too long list)
            int displayCount = Math.min(refs.size(), 10);
            for (int i = 0; i < displayCount; i++) {
                LabelUsageDetails.EntityReference ref = refs.get(i);
                VBox refBox = createEntityReferenceRow(ref, entityType);
                panel.getChildren().add(refBox);
            }

            // Show "and X more..." if there are more items
            if (refs.size() > 10) {
                javafx.scene.control.Label moreLabel = new javafx.scene.control.Label();
                moreLabel.textProperty().bind(I18n.i18nTextProperty(AndMoreItems, refs.size() - 10));
                moreLabel.getStyleClass().add("labeleditor-usage-more-label");
                moreLabel.setPadding(new Insets(0, 0, 0, 22));
                panel.getChildren().add(moreLabel);
            }
        }

        return panel;
    }

    /**
     * Creates a row showing an entity reference with ID, name, and dates.
     */
    private static VBox createEntityReferenceRow(LabelUsageDetails.EntityReference ref, String entityType) {
        VBox container = new VBox(2);
        container.setPadding(new Insets(2, 0, 2, 22));

        HBox mainRow = new HBox(8);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Bullet point
        javafx.scene.control.Label bullet = new javafx.scene.control.Label("\u2022");
        bullet.getStyleClass().add("labeleditor-ref-bullet");

        // Check if we have detailed info or just a placeholder
        if (ref.getId() != null || ref.getName() != null) {
            // ID badge
            if (ref.getId() != null) {
                javafx.scene.control.Label idBadge = new javafx.scene.control.Label("#" + ref.getId());
                idBadge.setPadding(new Insets(1, 4, 1, 4));
                idBadge.getStyleClass().add("labeleditor-ref-id-badge");
                mainRow.getChildren().addAll(bullet, idBadge);
            } else {
                mainRow.getChildren().add(bullet);
            }

            // Name
            if (ref.getName() != null && !ref.getName().isEmpty()) {
                javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(truncateText(ref.getName(), 50));
                nameLabel.getStyleClass().add("labeleditor-ref-name");
                mainRow.getChildren().add(nameLabel);
            }

            // Field used (e.g., "Name", "Short Description")
            if (ref.getFieldUsed() != null && !ref.getFieldUsed().isEmpty()) {
                javafx.scene.control.Label fieldBadge = new javafx.scene.control.Label("[" + ref.getFieldUsed() + "]");
                fieldBadge.getStyleClass().add("labeleditor-ref-field");
                mainRow.getChildren().add(fieldBadge);
            }

            container.getChildren().add(mainRow);

            // Date range on second line if available
            if (ref.getDateRange() != null && !ref.getDateRange().isEmpty()) {
                HBox dateRow = new HBox();
                dateRow.setPadding(new Insets(0, 0, 0, 18)); // Indent to align with content

                javafx.scene.control.Label dateLabel = new javafx.scene.control.Label("\uD83D\uDCC5 " + ref.getDateRange());
                dateLabel.getStyleClass().add("labeleditor-ref-date");

                dateRow.getChildren().add(dateLabel);
                container.getChildren().add(dateRow);
            }
        } else {
            // Simple placeholder (no detailed info)
            mainRow.getChildren().add(bullet);
            javafx.scene.control.Label placeholderLabel = I18nControls.newLabel(DetailsNotAvailable);
            placeholderLabel.getStyleClass().add("labeleditor-ref-placeholder");
            mainRow.getChildren().add(placeholderLabel);
            container.getChildren().add(mainRow);
        }

        return container;
    }

    // Currently active popup reference (to ensure only one popup is shown at a time)
    private static StackPane currentPopup;
    private static PauseTransition hideDelay;
    private static boolean isMouseOverPopup = false;
    private static boolean isMouseOverBadge = false;

    // Store mouse position when entering the badge (for popup positioning)
    private static double lastMouseSceneX;
    private static double lastMouseSceneY;

    /**
     * Sets up hover popup functionality for a usage badge.
     * Shows detailed usage information when the user hovers over the badge.
     */
    private static void setupUsageHoverPopup(javafx.scene.control.Label badge, LabelUsageDetails usageDetails) {
        // Create delay timer for showing popup
        PauseTransition showDelay = new PauseTransition(Duration.millis(500));

        badge.setOnMouseEntered(e -> {
            isMouseOverBadge = true;
            // Store mouse position for later use
            lastMouseSceneX = e.getSceneX();
            lastMouseSceneY = e.getSceneY();

            // Cancel any pending hide
            cancelHideDelay();

            // If popup already exists, just keep it visible
            if (currentPopup != null) {
                return;
            }

            showDelay.setOnFinished(event -> {
                // Double-check we're still over the badge
                if (!isMouseOverBadge) {
                    return;
                }

                // Use the dialog area (overlay layer) for the popup
                Pane dialogArea = FXMainFrameDialogArea.getDialogArea();
                if (dialogArea == null) {
                    return;
                }

                // Create the popup content
                StackPane popup = createUsagePopup(usageDetails);
                currentPopup = popup;

                // Add to dialog area
                dialogArea.getChildren().add(popup);

                // Bring popup to front
                popup.toFront();

                // Position the popup near the mouse (which is over the badge)
                // Use sceneToLocal to properly convert coordinates
                javafx.geometry.Point2D localPos = dialogArea.sceneToLocal(lastMouseSceneX, lastMouseSceneY);

                // Calculate position - popup to the left of mouse, overlapping
                double popupWidth = 500;
                double popupX = localPos.getX() - popupWidth + 40; // 40px overlap (popup extends right to mouse)
                double popupY = localPos.getY() - 10; // Slightly above mouse for overlap

                // Adjust if popup would go off-screen
                if (popupX < 20) {
                    popupX = 20;
                }
                double areaWidth = dialogArea.getWidth();
                if (areaWidth > 0 && popupX + popupWidth > areaWidth - 20) {
                    popupX = areaWidth - popupWidth - 20;
                }
                if (popupY < 20) {
                    popupY = 20;
                }

                popup.setLayoutX(popupX);
                popup.setLayoutY(popupY);

                // Setup mouse events on the popup itself to keep it visible when hovering over it
                popup.setOnMouseEntered(pe -> {
                    isMouseOverPopup = true;
                    cancelHideDelay();
                });

                popup.setOnMouseExited(pe -> {
                    isMouseOverPopup = false;
                    scheduleHidePopup();
                });
            });
            showDelay.playFromStart();
        });

        // Track mouse movement over the badge to update position
        badge.setOnMouseMoved(e -> {
            lastMouseSceneX = e.getSceneX();
            lastMouseSceneY = e.getSceneY();
        });

        badge.setOnMouseExited(e -> {
            isMouseOverBadge = false;
            showDelay.stop();
            scheduleHidePopup();
        });
    }

    /**
     * Cancels any pending hide delay.
     */
    private static void cancelHideDelay() {
        if (hideDelay != null) {
            hideDelay.stop();
            hideDelay = null;
        }
    }

    /**
     * Schedules hiding the current popup after a delay.
     * Will not hide if mouse is still over badge or popup.
     */
    private static void scheduleHidePopup() {
        // Don't schedule hide if mouse is over badge or popup
        if (isMouseOverBadge || isMouseOverPopup) {
            return;
        }

        cancelHideDelay();
        hideDelay = new PauseTransition(Duration.millis(300));
        hideDelay.setOnFinished(e -> {
            // Final check before hiding
            if (!isMouseOverBadge && !isMouseOverPopup) {
                hideCurrentPopup();
            }
        });
        hideDelay.playFromStart();
    }

    /**
     * Hides and removes the current popup.
     */
    private static void hideCurrentPopup() {
        if (currentPopup != null) {
            Parent parent = currentPopup.getParent();
            if (parent instanceof Pane parentPane) {
                parentPane.getChildren().remove(currentPopup);
            }
            currentPopup = null;
        }
        isMouseOverPopup = false;
    }

    /**
     * Creates the popup StackPane with usage details.
     * Uses a Rectangle background for better GWT rendering compatibility.
     */
    private static StackPane createUsagePopup(LabelUsageDetails usageDetails) {
        // Content VBox - sized to fit content only
        VBox content = new VBox(8);
        content.setPadding(new Insets(14, 18, 14, 18));
        content.setMinWidth(450);
        content.setPrefWidth(500);
        content.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        content.getStyleClass().add("labeleditor-popup-content");

        // Header
        javafx.scene.control.Label header = I18nControls.newLabel(LabelUsageTitle);
        header.getStyleClass().add("labeleditor-popup-header");
        Bootstrap.strong(header);
        content.getChildren().add(header);

        // Usage breakdown
        Map<String, java.util.List<LabelUsageDetails.EntityReference>> detailedUsage = usageDetails.getDetailedUsage();

        for (Map.Entry<String, java.util.List<LabelUsageDetails.EntityReference>> entry : detailedUsage.entrySet()) {
            String entityType = entry.getKey();
            java.util.List<LabelUsageDetails.EntityReference> refs = entry.getValue();

            // Entity type label
            javafx.scene.control.Label typeLabel = new javafx.scene.control.Label(entityType + " (" + refs.size() + "):");
            typeLabel.getStyleClass().add("labeleditor-popup-type-label");
            typeLabel.setPadding(new Insets(6, 0, 4, 0));
            typeLabel.setWrapText(true);
            Bootstrap.strong(typeLabel);
            content.getChildren().add(typeLabel);

            // Show first 5 references in popup (keep it compact)
            int displayCount = Math.min(refs.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                LabelUsageDetails.EntityReference ref = refs.get(i);
                VBox refRow = createCompactEntityRow(ref);
                content.getChildren().add(refRow);
            }

            if (refs.size() > 5) {
                javafx.scene.control.Label moreLabel = new javafx.scene.control.Label();
                moreLabel.textProperty().bind(I18n.i18nTextProperty(PlusMoreItems, refs.size() - 5));
                moreLabel.getStyleClass().add("labeleditor-popup-more-label");
                moreLabel.setPadding(new Insets(2, 0, 0, 16));
                content.getChildren().add(moreLabel);
            }
        }

        // Create background rectangle - not managed so it doesn't affect layout
        Rectangle background = new Rectangle();
        background.setFill(Color.web("#212529"));  // Dark background
        background.setStroke(Color.web("#495057"));  // Border color
        background.setStrokeWidth(2);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setManaged(false); // Don't participate in layout calculations

        // Create the StackPane container
        StackPane popup = new StackPane(background, content);

        // Bind background size to content size (not popup, to avoid feedback loop)
        background.widthProperty().bind(content.widthProperty());
        background.heightProperty().bind(content.heightProperty());

        // Ensure popup is visible and on top
        popup.setMouseTransparent(false);
        popup.setPickOnBounds(true);

        return popup;
    }

    /**
     * Creates a compact row for displaying an entity reference in the hover popup.
     * Uses VBox layout to allow text wrapping.
     */
    private static VBox createCompactEntityRow(LabelUsageDetails.EntityReference ref) {
        VBox container = new VBox(2);
        container.setPadding(new Insets(3, 0, 3, 16));

        // Main row with bullet and ID
        HBox mainRow = new HBox(8);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Bullet
        javafx.scene.control.Label bullet = new javafx.scene.control.Label("\u2022");
        bullet.getStyleClass().add("labeleditor-popup-bullet");
        mainRow.getChildren().add(bullet);

        if (ref.getId() != null || ref.getName() != null) {
            // ID badge
            if (ref.getId() != null) {
                javafx.scene.control.Label idBadge = new javafx.scene.control.Label("#" + ref.getId());
                idBadge.setPadding(new Insets(2, 6, 2, 6));
                idBadge.getStyleClass().add("labeleditor-popup-id-badge");
                mainRow.getChildren().add(idBadge);
            }

            // Date range if available (on same line as ID)
            if (ref.getDateRange() != null && !ref.getDateRange().isEmpty()) {
                javafx.scene.control.Label dateLabel = new javafx.scene.control.Label(ref.getDateRange());
                dateLabel.getStyleClass().add("labeleditor-popup-date");
                mainRow.getChildren().add(dateLabel);
            }

            container.getChildren().add(mainRow);

            // Name on second line with wrapping
            if (ref.getName() != null && !ref.getName().isEmpty()) {
                javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(ref.getName());
                nameLabel.getStyleClass().add("labeleditor-popup-name");
                nameLabel.setWrapText(true);
                nameLabel.setPadding(new Insets(0, 0, 0, 20)); // Indent to align with content after bullet
                nameLabel.setMaxWidth(440); // Allow wrapping within popup width
                container.getChildren().add(nameLabel);
            }
        } else {
            javafx.scene.control.Label placeholderLabel = I18nControls.newLabel(NoDetails);
            placeholderLabel.getStyleClass().add("labeleditor-popup-placeholder");
            mainRow.getChildren().add(placeholderLabel);
            container.getChildren().add(mainRow);
        }

        return container;
    }
}
