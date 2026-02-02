package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDate;

/**
 * Modal dialog for viewing guest details.
 * Shows comprehensive information about a guest's booking.
 *
 * @author David Hello
 * @author Claude Code
 */
public class GuestDetailsModal implements ReceptionDialogManager.ManagedDialog {

    private final Document document;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(true);
    private Runnable onSuccessCallback;

    // Guest info
    private final String guestName;
    private final String email;
    private final String phone;
    private final String eventName;
    private final String roomInfo;
    private final String status;
    private final LocalDate arrivalDate;
    private final LocalDate departureDate;
    private final Integer totalAmount;
    private final Integer paidAmount;
    private final Integer balance;
    private final String request;

    public GuestDetailsModal(Document document, String roomName, String roomType,
                              LocalDate arrivalDate, LocalDate departureDate,
                              Integer paidAmount, String status) {
        this.document = document;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.paidAmount = paidAmount != null ? paidAmount : 0;
        this.status = status;

        // Build guest name
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        this.guestName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();

        // Contact info
        this.email = document.getStringFieldValue("person_email");
        this.phone = document.getStringFieldValue("person_phone");

        // Event name
        if (document.getEvent() != null) {
            this.eventName = document.getEvent().getName();
        } else {
            this.eventName = "Independent";
        }

        // Build room info
        if (roomName != null && roomType != null) {
            this.roomInfo = roomName + " (" + roomType + ")";
        } else if (roomName != null) {
            this.roomInfo = roomName;
        } else if (roomType != null) {
            this.roomInfo = roomType;
        } else {
            this.roomInfo = "Offsite";
        }

        // Amounts
        Integer priceNet = document.getPriceNet();
        this.totalAmount = priceNet != null ? priceNet : 0;
        this.balance = this.totalAmount - this.paidAmount;

        // Special request
        this.request = document.getRequest();
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(480);
        container.setMaxWidth(520);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header with name and status
        VBox header = buildHeader();

        // Contact info section
        VBox contactSection = buildContactSection();

        // Stay info section
        VBox staySection = buildStaySection();

        // Payment section
        VBox paymentSection = buildPaymentSection();

        // Special requests section (if any)
        if (request != null && !request.trim().isEmpty()) {
            VBox requestSection = buildRequestSection();
            container.getChildren().addAll(header, contactSection, new Separator(),
                    staySection, new Separator(), paymentSection, new Separator(), requestSection);
        } else {
            container.getChildren().addAll(header, contactSection, new Separator(),
                    staySection, new Separator(), paymentSection);
        }

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(0, 0, 16, 0));

        // Name and status row
        HBox nameRow = new HBox(ReceptionStyles.SPACING_MD);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(guestName);
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600;");

        Label statusBadge = ReceptionStyles.createStatusBadge(status);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        nameRow.getChildren().addAll(nameLabel, spacer, statusBadge);

        // Event name
        Label eventLabel = new Label(eventName);
        eventLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(nameRow, eventLabel);

        return header;
    }

    private VBox buildContactSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label sectionTitle = I18nControls.newLabel(ReceptionI18nKeys.ContactInfo);
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox details = new VBox(ReceptionStyles.SPACING_XS);
        details.setPadding(new Insets(8, 0, 0, 0));

        // Email
        if (email != null && !email.isEmpty()) {
            HBox emailRow = buildDetailRow(ReceptionI18nKeys.Email, email);
            details.getChildren().add(emailRow);
        }

        // Phone
        if (phone != null && !phone.isEmpty()) {
            HBox phoneRow = buildDetailRow(ReceptionI18nKeys.Phone, phone);
            details.getChildren().add(phoneRow);
        }

        if (details.getChildren().isEmpty()) {
            Label noContact = new Label("No contact information");
            noContact.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
            details.getChildren().add(noContact);
        }

        section.getChildren().addAll(sectionTitle, details);

        return section;
    }

    private VBox buildStaySection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label sectionTitle = I18nControls.newLabel(ReceptionI18nKeys.StayInfo);
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox details = new VBox(ReceptionStyles.SPACING_XS);
        details.setPadding(new Insets(8, 0, 0, 0));

        // Room
        HBox roomRow = buildDetailRow(ReceptionI18nKeys.Room, roomInfo);
        details.getChildren().add(roomRow);

        // Arrival date
        String arrivalText = arrivalDate != null ? arrivalDate.toString() : "Not set";
        HBox arrivalRow = buildDetailRow(ReceptionI18nKeys.ExpectedDate, arrivalText);
        details.getChildren().add(arrivalRow);

        // Departure date
        String departureText = departureDate != null ? departureDate.toString() : "Not set";
        HBox departureRow = buildDetailRow(ReceptionI18nKeys.DepartureDate, departureText);
        details.getChildren().add(departureRow);

        // Number of nights
        if (arrivalDate != null && departureDate != null) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(arrivalDate, departureDate);
            HBox nightsRow = buildDetailRow(ReceptionI18nKeys.NightsStayed, String.valueOf(nights));
            details.getChildren().add(nightsRow);
        }

        section.getChildren().addAll(sectionTitle, details);

        return section;
    }

    private VBox buildPaymentSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label sectionTitle = I18nControls.newLabel(ReceptionI18nKeys.PaymentDetails);
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        VBox details = new VBox(ReceptionStyles.SPACING_XS);
        details.setPadding(new Insets(8, 0, 0, 0));

        // Total
        HBox totalRow = buildDetailRow(ReceptionI18nKeys.Total, "€" + totalAmount);
        details.getChildren().add(totalRow);

        // Paid
        HBox paidRow = buildDetailRow(ReceptionI18nKeys.Paid, "€" + paidAmount);
        details.getChildren().add(paidRow);

        // Balance
        HBox balanceRow = buildDetailRow(ReceptionI18nKeys.Balance, "€" + balance);
        Label balanceValue = (Label) balanceRow.getChildren().get(2);
        if (balance > 0) {
            balanceValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #dc3545;");
        } else if (balance == 0) {
            balanceValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #198754;");
        }
        details.getChildren().add(balanceRow);

        section.getChildren().addAll(sectionTitle, details);

        return section;
    }

    private VBox buildRequestSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label sectionTitle = I18nControls.newLabel(ReceptionI18nKeys.Notes);
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #495057;");

        Label requestLabel = new Label(request);
        requestLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057;");
        requestLabel.setWrapText(true);
        requestLabel.setPadding(new Insets(8, 12, 8, 12));
        requestLabel.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 6; -fx-font-size: 13px;");

        section.getChildren().addAll(sectionTitle, requestLabel);

        return section;
    }

    private HBox buildDetailRow(String labelKey, String value) {
        HBox row = new HBox(ReceptionStyles.SPACING_SM);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = I18nControls.newLabel(labelKey);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
        label.setMinWidth(100);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        row.getChildren().addAll(label, spacer, valueLabel);

        return row;
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
        // View-only modal, just close
        dialogCallback.closeDialog();
    }

    @Override
    public String getPrimaryButtonText() {
        return "Close";
    }

    @Override
    public String getCancelButtonText() {
        return ""; // No cancel button for view-only modal
    }
}
