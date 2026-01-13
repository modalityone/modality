package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

/**
 * Modal dialog for cancelling a booking.
 * Shows booking details and option to refund deposit.
 *
 * @author David Hello
 * @author Claude Code
 */
public class CancelModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Document document;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(true);
    private Runnable onSuccessCallback;

    // Guest info
    private final String guestName;
    private final String roomInfo;
    private final Integer depositAmount;

    // UI components
    private CheckBox refundDepositCheckbox;

    public CancelModal(DataSourceModel dataSourceModel, Document document, String roomName, String roomType) {
        this.dataSourceModel = dataSourceModel;
        this.document = document;

        // Build guest name
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        this.guestName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();

        // Build room info
        if (roomName != null && roomType != null) {
            this.roomInfo = roomName + " (" + roomType + ")";
        } else if (roomName != null) {
            this.roomInfo = roomName;
        } else if (roomType != null) {
            this.roomInfo = roomType;
        } else {
            this.roomInfo = "Not assigned";
        }

        // Get deposit amount
        this.depositAmount = document.getPriceDeposit();
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(400);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Booking details
        VBox details = buildDetails();

        // Refund option (if deposit exists)
        if (depositAmount != null && depositAmount > 0) {
            VBox refundSection = buildRefundSection();
            container.getChildren().addAll(header, details, refundSection);
        } else {
            container.getChildren().addAll(header, details);
        }

        // Warning
        VBox warning = buildWarning();
        container.getChildren().add(warning);

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\u2716"); // X mark
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #dc3545;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.CancelBooking);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label subtitle = new Label("Are you sure you want to cancel this booking?");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private VBox buildDetails() {
        VBox details = new VBox(ReceptionStyles.SPACING_SM);
        details.setPadding(new Insets(16));
        details.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Guest name
        HBox nameRow = buildDetailRow(ReceptionI18nKeys.Guest, guestName);

        // Room
        HBox roomRow = buildDetailRow(ReceptionI18nKeys.Room, roomInfo);

        details.getChildren().addAll(nameRow, roomRow);

        // Add deposit info if exists
        if (depositAmount != null && depositAmount > 0) {
            HBox depositRow = buildDetailRow(ReceptionI18nKeys.Deposit, "€" + depositAmount);
            details.getChildren().add(depositRow);
        }

        return details;
    }

    private HBox buildDetailRow(String labelKey, String value) {
        HBox row = new HBox(ReceptionStyles.SPACING_SM);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = I18nControls.newLabel(labelKey);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
        label.setMinWidth(80);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        row.getChildren().addAll(label, spacer, valueLabel);

        return row;
    }

    private VBox buildRefundSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(16, 0, 0, 0));

        refundDepositCheckbox = new CheckBox();
        Label checkboxLabel = I18nControls.newLabel(ReceptionI18nKeys.RefundDeposit);
        checkboxLabel.setStyle("-fx-font-size: 13px;");

        HBox checkboxRow = new HBox(ReceptionStyles.SPACING_SM);
        checkboxRow.setAlignment(Pos.CENTER_LEFT);
        checkboxRow.getChildren().addAll(refundDepositCheckbox, checkboxLabel);

        Label helpText = I18nControls.newLabel(ReceptionI18nKeys.RefundDepositHelp);
        helpText.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        helpText.setWrapText(true);

        section.getChildren().addAll(checkboxRow, helpText);

        return section;
    }

    private VBox buildWarning() {
        VBox warning = new VBox(ReceptionStyles.SPACING_XS);
        warning.setPadding(new Insets(12));
        warning.setStyle("-fx-background-color: #f8d7da; -fx-background-radius: 8; -fx-border-color: #f5c2c7; -fx-border-radius: 8;");

        Label warningIcon = new Label("\u26a0");
        warningIcon.setStyle("-fx-text-fill: #842029;");

        Label warningText = new Label("This action cannot be undone. The booking will be marked as cancelled.");
        warningText.setStyle("-fx-font-size: 12px; -fx-text-fill: #842029;");
        warningText.setWrapText(true);

        HBox warningRow = new HBox(ReceptionStyles.SPACING_SM);
        warningRow.setAlignment(Pos.CENTER_LEFT);
        warningRow.getChildren().addAll(warningIcon, warningText);

        warning.getChildren().add(warningRow);

        return warning;
    }

    @Override
    public BooleanProperty canProceedProperty() {
        return canProceed;
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @Override
    public void performAction(DialogCallback dialogCallback) {
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        Document updatedDoc = updateStore.updateEntity(document);
        updatedDoc.setCancelled(true);

        // TODO: If refundDepositCheckbox is selected and deposit > 0, create refund MoneyTransfer

        updateStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error cancelling booking: " + error.getMessage());
                    dialogCallback.closeDialog();
                })
                .onSuccess(result -> {
                    Console.log("Booking cancelled: " + guestName);
                    if (refundDepositCheckbox != null && refundDepositCheckbox.isSelected()) {
                        Console.log("Refund requested for deposit: " + depositAmount);
                        // TODO: Create refund MoneyTransfer
                    }
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dialogCallback.closeDialog();
                });
    }

    @Override
    public String getPrimaryButtonText() {
        return "Cancel Booking";
    }

    @Override
    public String getCancelButtonText() {
        return "Keep Booking";
    }
}
