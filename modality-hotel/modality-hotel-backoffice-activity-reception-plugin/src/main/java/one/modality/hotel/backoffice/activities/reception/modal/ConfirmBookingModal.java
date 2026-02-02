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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

/**
 * Modal dialog for confirming a pre-booked reservation.
 * Changes the booking status from pre-booked to confirmed.
 *
 * @author David Hello
 * @author Claude Code
 */
public class ConfirmBookingModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Document document;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(true);
    private Runnable onSuccessCallback;

    // Guest info
    private final String guestName;
    private final String roomInfo;
    private final Integer totalAmount;
    private final Integer depositAmount;

    public ConfirmBookingModal(DataSourceModel dataSourceModel, Document document,
                                String roomName, String roomType) {
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

        // Get amounts
        this.totalAmount = document.getPriceNet();
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

        // Info box
        VBox infoBox = buildInfoBox();

        container.getChildren().addAll(header, details, infoBox);

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\u2713"); // Checkmark
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #198754;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.Confirm);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label subtitle = new Label("Confirm this pre-booked reservation?");
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

        // Total amount
        String totalText = totalAmount != null ? "€" + totalAmount : "€0";
        HBox totalRow = buildDetailRow(ReceptionI18nKeys.Total, totalText);

        // Deposit
        String depositText = depositAmount != null && depositAmount > 0 ?
                "€" + depositAmount : "No deposit";
        HBox depositRow = buildDetailRow(ReceptionI18nKeys.Deposit, depositText);

        details.getChildren().addAll(nameRow, roomRow, totalRow, depositRow);

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

    private VBox buildInfoBox() {
        VBox info = new VBox(ReceptionStyles.SPACING_XS);
        info.setPadding(new Insets(12));
        info.setStyle("-fx-background-color: #e7f1ff; -fx-background-radius: 8; -fx-border-color: #b6d4fe; -fx-border-radius: 8;");

        Label infoIcon = new Label("\u2139"); // Info icon
        infoIcon.setStyle("-fx-text-fill: #084298;");

        Label infoText = new Label("Once confirmed, this booking will appear in the Arriving tab on the expected arrival date.");
        infoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #084298;");
        infoText.setWrapText(true);

        HBox infoRow = new HBox(ReceptionStyles.SPACING_SM);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getChildren().addAll(infoIcon, infoText);

        info.getChildren().add(infoRow);

        return info;
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
        updatedDoc.setConfirmed(true);

        updateStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error confirming booking: " + error.getMessage());
                    dialogCallback.closeDialog();
                })
                .onSuccess(result -> {
                    Console.log("Booking confirmed: " + guestName);
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dialogCallback.closeDialog();
                });
    }

    @Override
    public String getPrimaryButtonText() {
        return "Confirm Booking";
    }
}
