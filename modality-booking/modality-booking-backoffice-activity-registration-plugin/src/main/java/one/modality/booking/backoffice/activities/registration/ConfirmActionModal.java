package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Confirmation modal for line actions: Cancel, Delete, Restore, and Sold Out Force Booking.
 * <p>
 * Features:
 * - Action-specific header color and icon
 * - Information box explaining the action
 * - Optional comment/reason field
 * - Go Back / Confirm buttons
 * <p>
 * Based on RegistrationDashboardFull.jsx confirmation dialogs (lines 5830-6105, 7090-7245).
 *
 * @author Claude Code
 */
public class ConfirmActionModal {

    /**
     * Action types for the modal.
     */
    public enum ActionType {
        CANCEL,      // Cancel line (keeps minimum deposit)
        DELETE,      // Delete/remove line (no charges)
        RESTORE,     // Restore cancelled/deleted line
        SOLD_OUT     // Force booking of sold-out accommodation
    }

    private final ActionType actionType;
    private final DocumentLine line;
    private final Consumer<String> onConfirm; // Consumer receives the comment
    private final Runnable onCancel;

    private DialogCallback dialogCallback;
    private TextArea commentArea;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    public ConfirmActionModal(ActionType actionType, DocumentLine line, Consumer<String> onConfirm, Runnable onCancel) {
        this.actionType = actionType;
        this.line = line;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    /**
     * Shows the confirmation modal.
     */
    public void show() {
        VBox dialogPane = new VBox();
        dialogPane.setBackground(createBackground(BG, 12));
        dialogPane.setPrefWidth(actionType == ActionType.SOLD_OUT ? 440 : 400);
        dialogPane.setMaxWidth(450);

        // Header
        dialogPane.getChildren().add(createHeader());

        // Content
        dialogPane.getChildren().add(createContent());

        // Footer
        dialogPane.getChildren().add(createFooter());

        // Show dialog
        dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());
    }

    /**
     * Creates the header with action-specific color and icon.
     */
    private Node createHeader() {
        // Determine colors based on action type
        Color headerBg, headerBorder, iconBg, titleColor, subtitleColor;
        String iconText, titleText, subtitleText;

        switch (actionType) {
            case CANCEL:
                headerBg = WARM_ORANGE_LIGHT;
                headerBorder = deriveColor(WARM_ORANGE, 0.3);
                iconBg = WARM_ORANGE;
                titleColor = WARM_TEXT;
                subtitleColor = WARM_ORANGE;
                iconText = "🚫";
                titleText = "Cancel Option";
                subtitleText = getItemName();
                break;

            case DELETE:
                headerBg = DANGER_LIGHT;
                headerBorder = DANGER_BORDER;
                iconBg = DANGER;
                titleColor = Color.web("#991b1b");
                subtitleColor = DANGER;
                iconText = "🗑️";
                titleText = "Remove Option";
                subtitleText = getItemName();
                break;

            case RESTORE:
                headerBg = Color.web("#d1fae5");
                headerBorder = Color.web("#86efac");
                iconBg = SUCCESS;
                titleColor = Color.web("#166534");
                subtitleColor = SUCCESS;
                iconText = "↩️";
                titleText = "Restore Option";
                subtitleText = getItemName();
                break;

            case SOLD_OUT:
            default:
                headerBg = Color.web("#fef2f2");
                headerBorder = Color.web("#fecaca");
                iconBg = DANGER;
                titleColor = Color.web("#991b1b");
                subtitleColor = DANGER;
                iconText = "⚠️";
                titleText = "Accommodation Sold Out";
                subtitleText = "";
                break;
        }

        HBox header = new HBox(12);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(createBackground(headerBg, 12, 12, 0, 0));
        header.setBorder(new Border(new BorderStroke(
            headerBorder,
            BorderStrokeStyle.SOLID,
            new CornerRadii(12, 12, 0, 0, false),
            new BorderWidths(0, 0, 1, 0) // bottom border only
        )));

        // Icon circle
        StackPane iconCircle;
        if (actionType == ActionType.SOLD_OUT) {
            // Round icon for sold out
            iconCircle = new StackPane();
            Circle circle = new Circle(20, iconBg);
            Label iconLabel = new Label(iconText);
            iconLabel.setFont(Font.font(16));
            iconCircle.getChildren().addAll(circle, iconLabel);
        } else {
            // Rounded square for other actions
            iconCircle = new StackPane();
            iconCircle.setMinSize(40, 40);
            iconCircle.setMaxSize(40, 40);
            iconCircle.setBackground(createBackground(iconBg, 10));
            Label iconLabel = new Label(iconText);
            iconLabel.setFont(Font.font(20));
            iconCircle.getChildren().add(iconLabel);
        }

        // Title and subtitle
        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        titleLabel.setTextFill(titleColor);

        if (!subtitleText.isEmpty()) {
            Label subtitleLabel = new Label(subtitleText);
            subtitleLabel.setFont(Font.font(12));
            subtitleLabel.setTextFill(subtitleColor);
            titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        } else {
            titleBox.getChildren().add(titleLabel);
        }

        header.getChildren().addAll(iconCircle, titleBox);
        return header;
    }

    /**
     * Creates the content area with info boxes and comment field.
     */
    private Node createContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        switch (actionType) {
            case CANCEL:
                content.getChildren().add(createCancelInfoBox());
                break;
            case DELETE:
                content.getChildren().add(createDeleteInfoBox());
                break;
            case RESTORE:
                content.getChildren().add(createRestoreInfoBox());
                break;
            case SOLD_OUT:
                content.getChildren().add(createSoldOutInfoBox());
                break;
        }

        // Comment field (not for sold out)
        if (actionType != ActionType.SOLD_OUT) {
            content.getChildren().add(createCommentField());
        }

        return content;
    }

    /**
     * Creates the cancel info box.
     */
    private Node createCancelInfoBox() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 14, 12, 14));
        box.setBackground(createBackground(Color.web("#fffbeb"), 8));
        box.setBorder(createBorder(Color.web("#fcd34d"), 8));

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label icon = new Label("⚠️");
        icon.setFont(Font.font(18));

        VBox textBox = new VBox(4);
        Label title = new Label("Minimum deposit applies");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        title.setTextFill(Color.web("#92400e"));

        Integer minDeposit = line.getPriceMinDeposit();
        Integer priceNet = line.getPriceNet();
        int deposit = minDeposit != null ? minDeposit : 0;
        int price = priceNet != null ? priceNet : 0;

        Label desc;
        if (deposit > 0) {
            desc = new Label("£" + deposit + " will be kept as minimum deposit\n" +
                "Original: £" + price);
            desc.setStyle("-fx-strikethrough: false;");
        } else {
            desc = new Label("No deposit required – full refund");
        }
        desc.setFont(Font.font(12));
        desc.setTextFill(Color.web("#a16207"));
        desc.setWrapText(true);

        textBox.getChildren().addAll(title, desc);
        row.getChildren().addAll(icon, textBox);
        box.getChildren().add(row);
        return box;
    }

    /**
     * Creates the delete info box.
     */
    private Node createDeleteInfoBox() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 14, 12, 14));
        box.setBackground(createBackground(Color.web("#f3f4f6"), 8));
        box.setBorder(createBorder(Color.web("#d1d5db"), 8));

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label icon = new Label("ℹ");
        icon.setFont(Font.font(18));
        icon.setTextFill(Color.web("#6b7280"));

        VBox textBox = new VBox(4);
        Label title = new Label("Remove completely");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        title.setTextFill(Color.web("#374151"));

        Label desc = new Label("This option will be hidden from view. No charges apply.\n" +
            "You can restore it later if needed.");
        desc.setFont(Font.font(12));
        desc.setTextFill(Color.web("#6b7280"));
        desc.setWrapText(true);

        textBox.getChildren().addAll(title, desc);
        row.getChildren().addAll(icon, textBox);
        box.getChildren().add(row);
        return box;
    }

    /**
     * Creates the restore info box.
     */
    private Node createRestoreInfoBox() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 14, 12, 14));
        box.setBackground(createBackground(Color.web("#ecfdf5"), 8));
        box.setBorder(createBorder(Color.web("#86efac"), 8));

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label icon = new Label("✓");
        icon.setFont(Font.font(18));
        icon.setTextFill(SUCCESS);

        VBox textBox = new VBox(4);
        Label title = new Label("Restore this option");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        title.setTextFill(Color.web("#166534"));

        Integer priceNet = line.getPriceNet();
        int price = priceNet != null ? priceNet : 0;

        boolean isCancelled = Boolean.TRUE.equals(line.isCancelled());
        String descText;
        if (isCancelled) {
            descText = "The full price of £" + price + " will be charged again.";
        } else {
            descText = "This option will be visible again and charged at £" + price + ".";
        }

        Label desc = new Label(descText);
        desc.setFont(Font.font(12));
        desc.setTextFill(Color.web("#15803d"));
        desc.setWrapText(true);

        textBox.getChildren().addAll(title, desc);
        row.getChildren().addAll(icon, textBox);
        box.getChildren().add(row);
        return box;
    }

    /**
     * Creates the sold out info box.
     */
    private Node createSoldOutInfoBox() {
        VBox container = new VBox(16);

        // Explanation text
        Label explanationLabel = new Label(
            "The following accommodation is currently sold out for the selected dates:");
        explanationLabel.setFont(Font.font(14));
        explanationLabel.setTextFill(Color.web("#374151"));
        explanationLabel.setWrapText(true);

        // Item details box
        VBox itemBox = new VBox(4);
        itemBox.setPadding(new Insets(12, 16, 12, 16));
        itemBox.setBackground(createBackground(Color.web("#fef2f2"), 8));
        itemBox.setBorder(createBorder(Color.web("#fecaca"), 8));

        HBox itemRow = new HBox(10);
        itemRow.setAlignment(Pos.CENTER_LEFT);

        Label bedIcon = new Label("🛏️");
        bedIcon.setFont(Font.font(16));

        VBox itemInfo = new VBox(2);
        Label itemName = new Label(getItemName());
        itemName.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        itemName.setTextFill(Color.web("#991b1b"));

        // Date range
        LocalDate startDate = line.getStartDate();
        LocalDate endDate = line.getEndDate();
        String dateText = "";
        if (startDate != null && endDate != null) {
            dateText = startDate.format(DATE_FORMAT) + " – " + endDate.format(DATE_FORMAT);
        }
        Label dateLabel = new Label(dateText);
        dateLabel.setFont(Font.font(12));
        dateLabel.setTextFill(Color.web("#b91c1c"));

        itemInfo.getChildren().addAll(itemName, dateLabel);
        itemRow.getChildren().addAll(bedIcon, itemInfo);
        itemBox.getChildren().add(itemRow);

        // Question text
        Label questionLabel = new Label(
            "Do you want to force this booking anyway? This will overbook the accommodation.");
        questionLabel.setFont(Font.font(14));
        questionLabel.setTextFill(Color.web("#374151"));
        questionLabel.setWrapText(true);

        container.getChildren().addAll(explanationLabel, itemBox, questionLabel);
        return container;
    }

    /**
     * Creates the comment/reason field.
     */
    private Node createCommentField() {
        VBox box = new VBox(6);

        String labelText = actionType == ActionType.RESTORE ? "Comment (optional)" : "Reason (optional)";
        Label label = new Label(labelText.toUpperCase());
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        label.setTextFill(Color.web("#6b7280"));

        String placeholder;
        switch (actionType) {
            case CANCEL:
                placeholder = "e.g., Guest requested to skip meals...";
                break;
            case DELETE:
                placeholder = "e.g., Added by mistake...";
                break;
            case RESTORE:
            default:
                placeholder = "e.g., Guest changed their mind...";
                break;
        }

        commentArea = new TextArea();
        commentArea.setPromptText(placeholder);
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.setFont(Font.font(13));
        commentArea.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #d1d5db; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );

        box.getChildren().addAll(label, commentArea);
        return box;
    }

    /**
     * Creates the footer with action buttons.
     */
    private Node createFooter() {
        // Determine button color based on action
        Color confirmBtnColor;
        String confirmBtnIcon, confirmBtnText;

        switch (actionType) {
            case CANCEL:
                confirmBtnColor = WARM_ORANGE;
                confirmBtnIcon = "🚫";
                confirmBtnText = "Cancel Option";
                break;
            case DELETE:
                confirmBtnColor = DANGER;
                confirmBtnIcon = "🗑️";
                confirmBtnText = "Remove Option";
                break;
            case RESTORE:
                confirmBtnColor = SUCCESS;
                confirmBtnIcon = "↩️";
                confirmBtnText = "Restore Option";
                break;
            case SOLD_OUT:
            default:
                confirmBtnColor = DANGER;
                confirmBtnIcon = "⚠️";
                confirmBtnText = "Force Booking";
                break;
        }

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(16, 20, 16, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setBackground(createBackground(Color.web("#f9fafb"), 0, 0, 12, 12));
        footer.setBorder(new Border(new BorderStroke(
            Color.web("#e5e7eb"),
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            new BorderWidths(1, 0, 0, 0) // top border only
        )));

        // Go Back button
        Button backBtn = new Button("Go Back");
        backBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        backBtn.setTextFill(WARM_TEXT);
        backBtn.setBackground(createBackground(WARM_WHITE, 8));
        backBtn.setBorder(createBorder(MODAL_BORDER, 8));
        backBtn.setPadding(new Insets(10, 20, 10, 20));
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> closeDialog(false));

        // Confirm button
        Button confirmBtn = new Button(confirmBtnIcon + " " + confirmBtnText);
        confirmBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        confirmBtn.setTextFill(Color.WHITE);
        confirmBtn.setBackground(createBackground(confirmBtnColor, 8));
        confirmBtn.setBorder(Border.EMPTY);
        confirmBtn.setPadding(new Insets(10, 20, 10, 20));
        confirmBtn.setCursor(Cursor.HAND);
        confirmBtn.setOnAction(e -> closeDialog(true));

        footer.getChildren().addAll(backBtn, confirmBtn);
        return footer;
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog(boolean confirmed) {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
        }
        if (confirmed && onConfirm != null) {
            String comment = commentArea != null ? commentArea.getText() : "";
            onConfirm.accept(comment);
        } else if (!confirmed && onCancel != null) {
            onCancel.run();
        }
    }

    /**
     * Gets the item name from the line.
     */
    private String getItemName() {
        if (line == null || line.getItem() == null) return "Unknown Item";
        return line.getItem().getName();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STATIC CONVENIENCE METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Shows a cancel confirmation dialog.
     */
    public static void showCancelConfirmation(DocumentLine line, Consumer<String> onConfirm) {
        new ConfirmActionModal(ActionType.CANCEL, line, onConfirm, null).show();
    }

    /**
     * Shows a delete confirmation dialog.
     */
    public static void showDeleteConfirmation(DocumentLine line, Consumer<String> onConfirm) {
        new ConfirmActionModal(ActionType.DELETE, line, onConfirm, null).show();
    }

    /**
     * Shows a restore confirmation dialog.
     */
    public static void showRestoreConfirmation(DocumentLine line, Consumer<String> onConfirm) {
        new ConfirmActionModal(ActionType.RESTORE, line, onConfirm, null).show();
    }

    /**
     * Shows a sold out force booking confirmation dialog.
     */
    public static void showSoldOutConfirmation(DocumentLine line, Runnable onConfirm) {
        new ConfirmActionModal(ActionType.SOLD_OUT, line, comment -> onConfirm.run(), null).show();
    }
}
